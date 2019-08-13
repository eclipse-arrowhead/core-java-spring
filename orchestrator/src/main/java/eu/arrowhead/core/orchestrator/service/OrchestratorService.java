package eu.arrowhead.core.orchestrator.service;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.entity.OrchestratorStore;
import eu.arrowhead.common.database.entity.ServiceDefinition;
import eu.arrowhead.common.dto.CloudRequestDTO;
import eu.arrowhead.common.dto.CloudResponseDTO;
import eu.arrowhead.common.dto.DTOConverter;
import eu.arrowhead.common.dto.DTOUtilities;
import eu.arrowhead.common.dto.GSDPollRequestDTO;
import eu.arrowhead.common.dto.GSDQueryResultDTO;
import eu.arrowhead.common.dto.ICNResponseDTO;
import eu.arrowhead.common.dto.OrchestrationFlags;
import eu.arrowhead.common.dto.OrchestrationFlags.Flag;
import eu.arrowhead.common.dto.OrchestrationFormRequestDTO;
import eu.arrowhead.common.dto.OrchestrationResponseDTO;
import eu.arrowhead.common.dto.OrchestrationResultDTO;
import eu.arrowhead.common.dto.OrchestratorWarnings;
import eu.arrowhead.common.dto.PreferredProviderDataDTO;
import eu.arrowhead.common.dto.ServiceInterfaceResponseDTO;
import eu.arrowhead.common.dto.ServiceQueryFormDTO;
import eu.arrowhead.common.dto.ServiceQueryResultDTO;
import eu.arrowhead.common.dto.ServiceRegistryResponseDTO;
import eu.arrowhead.common.dto.SystemRequestDTO;
import eu.arrowhead.common.dto.SystemResponseDTO;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.core.orchestrator.database.service.OrchestratorStoreDBService;
import eu.arrowhead.core.orchestrator.matchmaking.InterCloudCloudMatchmakingAlgorithm;
import eu.arrowhead.core.orchestrator.matchmaking.InterCloudCloudMatchmakingParameters;
import eu.arrowhead.core.orchestrator.matchmaking.InterCloudProviderMatchmakingAlgorithm;
import eu.arrowhead.core.orchestrator.matchmaking.InterCloudProviderMatchmakingParameters;
import eu.arrowhead.core.orchestrator.matchmaking.IntraCloudProviderMatchmakingAlgorithm;
import eu.arrowhead.core.orchestrator.matchmaking.IntraCloudProviderMatchmakingParameters;

@Service
public class OrchestratorService {
	
	//=================================================================================================
	// members
	
	private static final Logger logger = LogManager.getLogger(OrchestratorService.class);
	private static final String NULL_PARAMETER_ERROR_MESSAGE = " is null.";
	private static final String NULL_OR_BLANK_PARAMETER_ERROR_MESSAGE = " is null or blank.";
	private static final String LESS_THAN_ONE_ERROR_MESSAGE= " must be greater than zero.";
	private static final String MORE_THAN_ONE_ERROR_MESSAGE= " must not have more then one element.";
	
	private static final int EXPIRING_TIME_IN_MINUTES = 2;
	
	@Autowired
	private OrchestratorDriver orchestratorDriver;
	
	@Autowired
	private OrchestratorStoreDBService orchestratorStoreDBService;
	
	@Resource(name = CommonConstants.INTRA_CLOUD_PROVIDER_MATCHMAKER)
	private IntraCloudProviderMatchmakingAlgorithm intraCloudProviderMatchmaker;
	
	@Resource(name = CommonConstants.INTER_CLOUD_PROVIDER_MATCHMAKER)
	private InterCloudProviderMatchmakingAlgorithm interCloudProviderMatchmaker;
	
	@Resource(name = CommonConstants.INTER_CLOUD_CLOUD_MATCHMAKER)
	private InterCloudCloudMatchmakingAlgorithm interCloudClouderMatchmaker;
	
	@Value(CommonConstants.$ORCHESTRATOR_IS_GATEKEEPER_PRESENT_WD)
	private boolean gateKeeperIsPresent;
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	/**
	 * This method represents the orchestration process where the requester System is NOT in the local Cloud. This means that the Gatekeeper made sure
	 * that this request from the remote Orchestrator can be satisfied in this Cloud. (Gatekeeper polled the Service Registry and Authorization
	 * Systems.)
	 */
	@SuppressWarnings("squid:S1612")
	public OrchestrationResponseDTO externalServiceRequest(final OrchestrationFormRequestDTO request) {
		logger.debug("externalServiceRequest started ...");
		checkServiceRequestForm(request, false);
		
		// Querying the Service Registry to get the list of Provider Systems
		final OrchestrationFlags flags = request.getOrchestrationFlags();
		final ServiceQueryResultDTO queryResult = orchestratorDriver.queryServiceRegistry(request.getRequestedService(), flags.get(Flag.METADATA_SEARCH), flags.get(Flag.PING_PROVIDERS));
		
		List<ServiceRegistryResponseDTO> queryData = queryResult.getServiceQueryData();
	    // If necessary, removing the non-preferred providers from the SR response. (If necessary, matchmaking is done after this at the request sender Cloud.)
		if (flags.get(Flag.ONLY_PREFERRED)) {  
			// This request contains only local preferred systems, since this request came from another cloud, but the unboxing is necessary
			final List<PreferredProviderDataDTO> localProviders = request.getPreferredProviders().stream().filter(p -> p.isLocal()).collect(Collectors.toList());
			queryData = removeNonPreferred(queryData, localProviders);
		}

		logger.debug("externalServiceRequest finished with {} service providers.", queryData.size());
		
		return compileOrchestrationResponse(queryData, request);
	}

	//-------------------------------------------------------------------------------------------------	
	public OrchestrationResponseDTO triggerInterCloud(
			final OrchestrationFormRequestDTO request) {
		logger.debug("triggerInterCloud started ...");
		
		// necessary, because we want to use a flag value when we call the check method
		if (request == null) {
			throw new InvalidParameterException("request" + NULL_PARAMETER_ERROR_MESSAGE);
		}
		
		final OrchestrationFlags flags = request.getOrchestrationFlags();
		checkServiceRequestForm(request, isInterCloudOrchestrationPossible(flags));
		
		final List<CloudRequestDTO> preferredClouds = new ArrayList<>();
		for (PreferredProviderDataDTO provider : request.getPreferredProviders()) {
		  if (provider.isGlobal() && !preferredClouds.contains(provider.getProviderCloud())) {
		    preferredClouds.add(provider.getProviderCloud());
		  }
		}
	    
		final GSDQueryResultDTO result = OrchestratorDriver.doGlobalServiceDiscovery(
				new GSDPollRequestDTO(
						request.getRequestedService(), request.getRequesterCloud(), gateKeeperIsPresent));

		final InterCloudCloudMatchmakingParameters iCCMparams = new InterCloudCloudMatchmakingParameters(result, preferredClouds, flags.get(Flag.ONLY_PREFERRED));
		final CloudResponseDTO targetCloud = interCloudClouderMatchmaker.doMatchmaking(iCCMparams);
        if (targetCloud == null || Utilities.isEmpty(targetCloud.getName())) {
            
        	// Return empty response
            return new OrchestrationResponseDTO();
 		}
		
		final ICNResponseDTO icnResponseDTO = orchestratorDriver.doInterCloudNegotiations(request, DTOConverter.convertCloudResponseDTOToCloudRequestDTO(targetCloud));
        if (icnResponseDTO == null || icnResponseDTO.getResponse().isEmpty()) {
           
        	// Return empty response
           return new OrchestrationResponseDTO();
		}
		
        for (OrchestrationResultDTO orchestrationResultDTO : icnResponseDTO.getResponse()) {
        	orchestrationResultDTO.getWarnings().add(OrchestratorWarnings.FROM_OTHER_CLOUD);
		}
		
       if (flags.get(Flag.MATCHMAKING)) {
            
        	final InterCloudProviderMatchmakingParameters iCPMparams = new InterCloudProviderMatchmakingParameters(icnResponseDTO, request.getPreferredProviders(), true);		            
	           
            return interCloudProviderMatchmaker.doMatchmaking(iCPMparams);
		}	


       return new OrchestrationResponseDTO(icnResponseDTO.getResponse());
	}

	//-------------------------------------------------------------------------------------------------	
	public OrchestrationResponseDTO orchestrationFromStore(
			final OrchestrationFormRequestDTO orchestrationFormRequestDTO) {
		logger.debug("orchestrationFromStore started ...");
		
		return orchestrationFromStoreWithSystemIdParameter(orchestrationFormRequestDTO, null);
	}

	//-------------------------------------------------------------------------------------------------	
	public OrchestrationResponseDTO orchestrationFromStoreWithSystemIdParameter(
			final OrchestrationFormRequestDTO orchestrationFormRequestDTO,
			final Long systemId) {
		logger.debug("orchestrationFromStoreWithSystemParameter started ...");		
		
		if (orchestrationFormRequestDTO == null) {
			throw new InvalidParameterException("request" + NULL_PARAMETER_ERROR_MESSAGE);
		}
		orchestrationFormRequestDTO.validateCrossParameterConstraints();
		
		final List<OrchestratorStore> entryList;
		final List<ServiceRegistryResponseDTO> crossCheckedEntryList;
		
		if ( systemId != null) {
			
			if (systemId < 1) {
				throw new InvalidParameterException("systemId " + LESS_THAN_ONE_ERROR_MESSAGE);
			}
			
			final SystemRequestDTO consumerSystemRequestDTO = DTOConverter.convertSystemResponseDTOToSystemRequestDTO(orchestratorDriver.queryServiceRegistryBySystemId(systemId));
			
			entryList = orchestratorStoreDBService.getAllTopPriorityOrchestratorStoreEntriesByConsumerSystemId(systemId);
			crossCheckedEntryList = crossCheckTopPriorityEntries(entryList, orchestrationFormRequestDTO, consumerSystemRequestDTO);
			
			return compileOrchestrationResponse(crossCheckedEntryList, orchestrationFormRequestDTO);
		
		}else {
			
			entryList = getOrchestrationStoreEntries( orchestrationFormRequestDTO.getRequesterSystem(), orchestrationFormRequestDTO.getRequestedService());	
			crossCheckedEntryList = crossCheckStoreEntries(entryList, orchestrationFormRequestDTO);
			
			for (final OrchestratorStore orchestratorStore : entryList) {
				
				if (!orchestratorStore.isForeign()) {
					
					final Long providerSystemId = orchestratorStore.getProviderSystemId();
					for (ServiceRegistryResponseDTO serviceRegistryResponseDTO : crossCheckedEntryList) {
						
						if (serviceRegistryResponseDTO.getProvider().getId() == providerSystemId) {
							
							return compileOrchestrationResponse(List.of(serviceRegistryResponseDTO), orchestrationFormRequestDTO);							
						}
					}		
				}else {

					final PreferredProviderDataDTO preferredProviderDataDTO = DTOConverter.convertForeignOrchestratorStoreResponseDTOToPreferredProviderDataDTO(orchestratorStoreDBService.getForeignResponseDTO(orchestratorStore));					
					orchestrationFormRequestDTO.setPreferredProviders(List.of(preferredProviderDataDTO));
					
		            final ICNResponseDTO icnResponseDTO = orchestratorDriver.doInterCloudNegotiations(orchestrationFormRequestDTO, preferredProviderDataDTO.getProviderCloud());		            
		            if (icnResponseDTO != null && !icnResponseDTO.getResponse().isEmpty()) {
			            
		            	final InterCloudProviderMatchmakingParameters params = new InterCloudProviderMatchmakingParameters(icnResponseDTO, orchestrationFormRequestDTO.getPreferredProviders(), true);		            
				           
			            return interCloudProviderMatchmaker.doMatchmaking(params);
					}	
				}				
			}
		}
		
		//return empty response if orchestration was not sucessful
		return new OrchestrationResponseDTO();
	}

	//-------------------------------------------------------------------------------------------------	
	/**
	 * Represents the regular orchestration process where the requester system is in the local Cloud. In this process the
     * <i>Orchestrator Store</i> is ignored, and the Orchestrator first tries to find a provider for the requested service in the local Cloud.
     * If that fails but the <i>enableInterCloud</i> flag is set to true, the Orchestrator tries to find a provider in other Clouds.
	 */
	public OrchestrationResponseDTO dynamicOrchestration(final OrchestrationFormRequestDTO request) {
		logger.debug("dynamicOrchestration started ...");

		// necessary, because we want to use a flag value when we call the check method
		if (request == null) {
			throw new InvalidParameterException("request" + NULL_PARAMETER_ERROR_MESSAGE);
		}
		
		final OrchestrationFlags flags = request.getOrchestrationFlags();
		checkServiceRequestForm(request, isInterCloudOrchestrationPossible(flags));
		
		// Querying the Service Registry to get the list of Provider Systems
		final ServiceQueryResultDTO queryResult = orchestratorDriver.queryServiceRegistry(request.getRequestedService(), flags.get(Flag.METADATA_SEARCH), flags.get(Flag.PING_PROVIDERS));
		List<ServiceRegistryResponseDTO> queryData = queryResult.getServiceQueryData();
		if (queryData.isEmpty()) {
			if (isInterCloudOrchestrationPossible(flags)) {
				// no result in the local Service Registry => we try with other clouds
				logger.debug("dynamicOrchestration: no result in Service Registry => moving to Inter-Cloud orchestration.");
				return triggerInterCloud(request);
			} else {
				return new OrchestrationResponseDTO(); // empty response
			}
		}
		
	    // Cross-checking the SR response with the Authorization
		queryData = orchestratorDriver.queryAuthorization(request.getRequesterSystem(), queryData);
		if (queryData.isEmpty()) {
			if (isInterCloudOrchestrationPossible(flags)) {
				// no result after authorization => we try with other clouds
				logger.debug("dynamicOrchestration: no provider give access to requester system => moving to Inter-Cloud orchestration.");
				return triggerInterCloud(request);
			} else {
				return new OrchestrationResponseDTO(); // empty response
			}
		}
		
		final List<PreferredProviderDataDTO> localProviders = request.getPreferredProviders().stream().filter(p -> p.isLocal()).collect(Collectors.toList());

		// If necessary, removing the non-preferred providers from the SR response. 
		if (flags.get(Flag.ONLY_PREFERRED)) {
			queryData = removeNonPreferred(queryData, localProviders);
			if (queryData.isEmpty()) {
				if (isInterCloudOrchestrationPossible(flags)) {
					// no result that contains any of the local preferred providers => if there are preferred providers from other clouds we can try with those clouds
					final List<PreferredProviderDataDTO> nonLocalProviders = request.getPreferredProviders().stream().filter(p -> p.isGlobal()).collect(Collectors.toList());
					if (!nonLocalProviders.isEmpty()) {
						logger.debug("dynamicOrchestration: no local preferred provider give access to requester system => moving to Inter-Cloud orchestration.");
						return triggerInterCloud(request);
					} else { // nothing we can do
						logger.debug("dynamicOrchestration: no preferred provider give access to requester system => orchestration failed");
						return new OrchestrationResponseDTO(); // empty response
					}
				} else {
					return new OrchestrationResponseDTO(); // empty response
				}
			}
		}

		// If matchmaking is requested, we pick out 1 ServiceRegistryEntry entity from the list.
		if (flags.get(Flag.MATCHMAKING)) {
			final IntraCloudProviderMatchmakingParameters params = new IntraCloudProviderMatchmakingParameters(localProviders);
			// set additional parameters here if you use a different matchmaking algorithm
			final ServiceRegistryResponseDTO selected = intraCloudProviderMatchmaker.doMatchmaking(queryData, params);
			queryData.clear();
			queryData.add(selected);
		}

		// all the filtering is done
		logger.debug("dynamicOrchestration finished with {} service providers.", queryData.size());
		
		return compileOrchestrationResponse(queryData, request);
	}
	
	//-------------------------------------------------------------------------------------------------
	public OrchestrationResponseDTO storeOchestrationProcessResponse(long systemId) {
		logger.debug("storeOchestrationProcessResponse started ...");
		
		final SystemResponseDTO validConsumerSystemResponseDTO  =  validateSystemId(systemId);
		final SystemRequestDTO systemRequestDTO = DTOConverter.convertSystemResponseDTOToSystemRequestDTO(validConsumerSystemResponseDTO);
		
	    final OrchestrationFormRequestDTO orchestrationFormRequestDTO = new OrchestrationFormRequestDTO.Builder(systemRequestDTO).build();

	    return orchestrationFromStoreWithSystemIdParameter(orchestrationFormRequestDTO, systemId);

	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private boolean isInterCloudOrchestrationPossible(final OrchestrationFlags flags) {
		return gateKeeperIsPresent && flags.get(Flag.ENABLE_INTER_CLOUD);
	}
	
	//-------------------------------------------------------------------------------------------------
	private void checkServiceRequestForm(final OrchestrationFormRequestDTO request, final boolean cloudCheckInProviders) {
		logger.debug("checkExternalServiceRequestForm started ...");
		
		if (request == null) {
			throw new InvalidParameterException("request" + NULL_PARAMETER_ERROR_MESSAGE);
		}
		
		request.validateCrossParameterConstraints();
		
		// Requested service
		checkRequestedServiceForm(request.getRequestedService());
		
		// Preferred Providers
		checkPreferredProviders(request.getPreferredProviders(), cloudCheckInProviders);
	}

	//-------------------------------------------------------------------------------------------------
	private void checkRequestedServiceForm(final ServiceQueryFormDTO form) {
		logger.debug("checkRequestedServiceForm started ...");
		
		if (form == null) {
			throw new InvalidParameterException("form" + NULL_PARAMETER_ERROR_MESSAGE);
		}
		
		if (Utilities.isEmpty(form.getServiceDefinitionRequirement())) {
			throw new InvalidParameterException("Service definition requirement" + NULL_OR_BLANK_PARAMETER_ERROR_MESSAGE);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private void checkPreferredProviders(final List<PreferredProviderDataDTO> providers, final boolean checkCloudInfo) {
		if (providers != null) {
			for (final PreferredProviderDataDTO provider : providers) {
				checkSystemRequestDTO(provider.getProviderSystem(), false);
				if (checkCloudInfo && provider.getProviderCloud() != null) {
					checkCloudRequestDTO(provider.getProviderCloud());
				}
			}
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private void checkSystemRequestDTO(final SystemRequestDTO system, final boolean portRangeCheck) {
		logger.debug("checkSystemRequestDTO started...");
		
		if (system == null) {
			throw new InvalidParameterException("System" + NULL_PARAMETER_ERROR_MESSAGE);
		}
		
		if (Utilities.isEmpty(system.getSystemName())) {
			throw new InvalidParameterException("System name" + NULL_OR_BLANK_PARAMETER_ERROR_MESSAGE);
		}
		
		if (Utilities.isEmpty(system.getAddress())) {
			throw new InvalidParameterException("System address" + NULL_OR_BLANK_PARAMETER_ERROR_MESSAGE);
		}
		
		if (system.getPort() == null) {
			throw new InvalidParameterException("System port" + NULL_PARAMETER_ERROR_MESSAGE);
		}
		
		final int validatedPort = system.getPort().intValue();
		if (portRangeCheck && (validatedPort < CommonConstants.SYSTEM_PORT_RANGE_MIN || validatedPort > CommonConstants.SYSTEM_PORT_RANGE_MAX)) {
			throw new InvalidParameterException("System port must be between " + CommonConstants.SYSTEM_PORT_RANGE_MIN + " and " + CommonConstants.SYSTEM_PORT_RANGE_MAX + ".");
		}
	}

	//-------------------------------------------------------------------------------------------------
	private void checkCloudRequestDTO(final CloudRequestDTO cloud) {
		logger.debug("checkCloudRequestDTO started...");
		
		if (cloud == null) {
			throw new InvalidParameterException("Cloud" + NULL_PARAMETER_ERROR_MESSAGE);
		}
		
		if (Utilities.isEmpty(cloud.getOperator())) {
			throw new InvalidParameterException("Cloud operator" + NULL_OR_BLANK_PARAMETER_ERROR_MESSAGE);
		}
		
		if (Utilities.isEmpty(cloud.getName())) {
			throw new InvalidParameterException("Cloud name" + NULL_OR_BLANK_PARAMETER_ERROR_MESSAGE);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private List<ServiceRegistryResponseDTO> removeNonPreferred(final List<ServiceRegistryResponseDTO> srList, final List<PreferredProviderDataDTO> preferredProviders) {
		logger.debug("removeNonPreferred started...");
		
		final List<ServiceRegistryResponseDTO> result = new ArrayList<>();
		for (final ServiceRegistryResponseDTO srResult : srList) {
			for (final PreferredProviderDataDTO preferredProvider : preferredProviders) {
				if (DTOUtilities.equalsSystemInResponseAndRequest(srResult.getProvider(), preferredProvider.getProviderSystem())) {
					result.add(srResult);
				}
			}
		}
		
		logger.debug("removeNonPreferred returns with {} entries.", result.size());
		
		return result;
	}
	
	
	//-------------------------------------------------------------------------------------------------
	private OrchestrationResponseDTO compileOrchestrationResponse(final List<ServiceRegistryResponseDTO> srList, final OrchestrationFormRequestDTO request) {
		logger.debug("compileOrchestrationResponse started...");
		
		List<OrchestrationResultDTO> orList = new ArrayList<>(srList.size());
		for (final ServiceRegistryResponseDTO entry : srList) {
			final OrchestrationResultDTO result = new OrchestrationResultDTO(entry.getProvider(), entry.getServiceDefinition(), entry.getServiceUri(), entry.getSecure(), entry.getMetadata(), 
																			 entry.getInterfaces(), entry.getVersion());
			
			if (request.getOrchestrationFlags().get(Flag.OVERRIDE_STORE)) {
				final List<OrchestratorWarnings> warnings = calculateOrchestratorWarnings(entry);
				result.setWarnings(warnings);
			}
			orList.add(result);
		}
		
	    // Generate the authorization tokens if it is requested based on the service security (modifies the orList)
	    orList = orchestratorDriver.generateAuthTokens(request, orList);
		
	    logger.debug("compileOrchestrationResponse creates {} orchestration forms", orList.size());

		return new OrchestrationResponseDTO(orList);
	}

	//-------------------------------------------------------------------------------------------------
	private List<OrchestratorWarnings> calculateOrchestratorWarnings(final ServiceRegistryResponseDTO entry) {
		logger.debug("calculateOrchestratorWarnings started...");
		final ZonedDateTime now = ZonedDateTime.now();
		
		final List<OrchestratorWarnings> result = new ArrayList<>(2);
		if (Utilities.isEmpty(entry.getEndOfValidity())) {
			result.add(OrchestratorWarnings.TTL_UNKNOWN);
		} else {
			final ZonedDateTime endOfValidity = Utilities.parseUTCStringToLocalZonedDateTime(entry.getEndOfValidity());
			if (endOfValidity.isBefore(now)) {
				result.add(OrchestratorWarnings.TTL_EXPIRED);
			} else if (endOfValidity.plusMinutes(EXPIRING_TIME_IN_MINUTES).isBefore(now)) {
		        // EXPIRING_TIME_IN_MINUTES minutes is an arbitrarily chosen value for the Time To Live measure, which got its value when the SR was queried.
				// The provider presumably will stop offering this service in somewhat less than EXPIRING_TIME_IN_MINUTES minutes.
				result.add(OrchestratorWarnings.TTL_EXPIRING);
			}
		}
		
		return result;
	}
	
	//-------------------------------------------------------------------------------------------------
	private SystemResponseDTO validateSystemId(final long systemId) {
		logger.debug("validateSystemId started...");
		
		if (systemId < 1) {
			throw new InvalidParameterException("SystemId " + LESS_THAN_ONE_ERROR_MESSAGE);
		}
		
		return orchestratorDriver.queryServiceRegistryBySystemId(systemId);

	}
	
	//-------------------------------------------------------------------------------------------------
	private long validateSystemRequestDTO(final SystemRequestDTO consumerSystemRequestDTO) {
		logger.debug("validateSystemId started...");
		
		if (consumerSystemRequestDTO == null) {
			throw new InvalidParameterException("SystemRequestDTO " + NULL_PARAMETER_ERROR_MESSAGE);
		}
		
		final SystemResponseDTO systemResponseDTO = orchestratorDriver.queryServiceRegistryBySystemRequestDTO(consumerSystemRequestDTO);
		
		
		return systemResponseDTO.getId();
	}
	
	//-------------------------------------------------------------------------------------------------
	private List<OrchestratorStore> getOrchestrationStoreEntries(SystemRequestDTO requesterSystem,
			ServiceQueryFormDTO requestedService) {
		logger.debug("getOrchestrationStoreEntries started...");
		
		final List<OrchestratorStore> retrievedList ;
		
		if (requesterSystem == null) {
			throw new InvalidParameterException("ConsumerSystem " + NULL_PARAMETER_ERROR_MESSAGE);
		}
		final long consumerSystemId = validateSystemRequestDTO(requesterSystem);
		
		if ( requestedService == null) {
			
			retrievedList = orchestratorStoreDBService.getAllTopPriorityOrchestratorStoreEntriesByConsumerSystemId(consumerSystemId);
			
		}else {
			
			if (Utilities.isEmpty(requestedService.getServiceDefinitionRequirement() )) {
				throw new InvalidParameterException("ServiceDefinitionRequirement " + NULL_OR_BLANK_PARAMETER_ERROR_MESSAGE);
			}
			
			final String serviceDefinitionName = requestedService.getServiceDefinitionRequirement().trim().toLowerCase();
			
			if (requestedService.getInterfaceRequirements() == null ||
					requestedService.getInterfaceRequirements().isEmpty() || 
					Utilities.isEmpty(requestedService.getInterfaceRequirements().get(0))) {
				throw new InvalidParameterException("InterfaceRequirement " + NULL_OR_BLANK_PARAMETER_ERROR_MESSAGE);
			}
			
			if (requestedService.getInterfaceRequirements().size() != 1) {
				throw new InvalidParameterException("InterfaceRequirement " + MORE_THAN_ONE_ERROR_MESSAGE);
			}
			final String serviceInterfaceName =  requestedService.getInterfaceRequirements().get(0).trim();
			
			retrievedList = orchestratorStoreDBService.getOrchestratorStoresByConsumerIdAndServiceDefinitionAndServiceInterface(consumerSystemId, serviceDefinitionName, serviceInterfaceName);
		}
		
		return retrievedList;
	}
	
	//-------------------------------------------------------------------------------------------------
	private List<ServiceRegistryResponseDTO> crossCheckTopPriorityEntries(
			final List<OrchestratorStore> entryList, 
			final OrchestrationFormRequestDTO orchestrationFormRequestDTO,
			final SystemRequestDTO consumerSystem) {
		logger.debug("crossCheckTopPriorityEntries started...");
		
		final OrchestrationFlags flags = orchestrationFormRequestDTO.getOrchestrationFlags();	   
	    
	    final List<ServiceRegistryResponseDTO> filteredServiceQueryResultDTOList = new ArrayList<>();
	    
	    final List<OrchestratorStore> onlyLocalEntryList = filterEntryListByForeign(entryList);
	    
	    if (onlyLocalEntryList.isEmpty()) {
			
	    	return List.of();
		}
	    
	    final Map<Long, String> serviceDefinitionsIdsMap = mapServiceDefinitionsToServiceDefinitionIds( onlyLocalEntryList); 
	    final Map<Long, List<String>> serviceDefinitionIdInterfaceIdsMap = mapInterfacesToServiceDefinitions( onlyLocalEntryList);
	    final Map<Long, List<String>> providerIdInterfaceIdsMap = mapInterfacesToProviders( onlyLocalEntryList);
	    
	    for (Entry<Long, String> entry : serviceDefinitionsIdsMap.entrySet()) {
	    	
	    	final ServiceQueryFormDTO serviceQueryFormDTO = new ServiceQueryFormDTO();
	    	
	    	serviceQueryFormDTO.setServiceDefinitionRequirement(entry.getValue());
		    final List<String> interfaceRequirements = serviceDefinitionIdInterfaceIdsMap.get(entry.getKey());
		    serviceQueryFormDTO.setInterfaceRequirements(interfaceRequirements);
		    
		    final ServiceQueryResultDTO queryResult = orchestratorDriver.queryServiceRegistry(serviceQueryFormDTO, flags.get(Flag.METADATA_SEARCH), flags.get(Flag.PING_PROVIDERS));
		    final List<ServiceRegistryResponseDTO> filteredQueryResultByInterfaces = filterQueryResultByInterfaces(providerIdInterfaceIdsMap, queryResult);	
		    final List<ServiceRegistryResponseDTO> filteredQueryResultByAuthorization = orchestratorDriver.queryAuthorization(consumerSystem, filteredQueryResultByInterfaces);	
		    
		    filteredServiceQueryResultDTOList.addAll(filteredQueryResultByAuthorization);
	    }
	    
		return filteredServiceQueryResultDTOList;
	}
	
	//-------------------------------------------------------------------------------------------------
	private List<OrchestratorStore> filterEntryListByForeign(List<OrchestratorStore> entryList) {
		logger.debug(" filterEntryListByForeign started...");
		
		if (entryList == null || entryList.isEmpty()) {
			return List.of();
		}
		
		return entryList.stream().filter(e -> !e.isForeign()).collect(Collectors.toList()); 
		
	}

	//-------------------------------------------------------------------------------------------------
	private List<ServiceRegistryResponseDTO> crossCheckStoreEntries(List<OrchestratorStore> entryList,
			OrchestrationFormRequestDTO orchestrationFormRequestDTO) {
		logger.debug("crossCheckStoreEntries started...");
		
		final OrchestrationFlags flags = orchestrationFormRequestDTO.getOrchestrationFlags();
		
	    final List<OrchestratorStore> onlyLocalEntryList = filterEntryListByForeign(entryList);    
	    if (onlyLocalEntryList.isEmpty()) {
			
	    	return List.of();
		}
		
    	final ServiceQueryFormDTO serviceQueryFormDTO = orchestrationFormRequestDTO.getRequestedService();
    	serviceQueryFormDTO.setInterfaceRequirements(List.of(orchestrationFormRequestDTO.getRequestedService().getInterfaceRequirements().get(0)));
    	
		final ServiceQueryResultDTO serviceQueryResultDTO = orchestratorDriver.queryServiceRegistry(serviceQueryFormDTO,  flags.get(Flag.METADATA_SEARCH), flags.get(Flag.PING_PROVIDERS)); 
		
		return orchestratorDriver.queryAuthorization(orchestrationFormRequestDTO.getRequesterSystem(), serviceQueryResultDTO.getServiceQueryData());
    
	}
	
	//-------------------------------------------------------------------------------------------------
	private Map<Long, List<String>> mapInterfacesToServiceDefinitions(List<OrchestratorStore> entryList) {
		logger.debug("mapIntrefacesToServiceDefinitions started...");
		
		final Map<Long, List<String>> serviceDefinitionsInterfacesMap = new HashMap<>();
		
		for (OrchestratorStore orchestratorStore : entryList) {
			final Long serviceDefinitionId = orchestratorStore.getServiceDefinition().getId();
			final String interfaceName = orchestratorStore.getServiceInterface().getInterfaceName();
			
			if (serviceDefinitionsInterfacesMap.containsKey(serviceDefinitionId)) {
				
				
				if (!serviceDefinitionsInterfacesMap.get(serviceDefinitionId).contains(interfaceName)) {
					
					serviceDefinitionsInterfacesMap.get(serviceDefinitionId).add(interfaceName);
				}
			
			}else {
				
				final List<String> serviceInterfaceNameList = new ArrayList<>();
				serviceInterfaceNameList.add(orchestratorStore.getServiceInterface().getInterfaceName());
				serviceDefinitionsInterfacesMap.put(serviceDefinitionId, serviceInterfaceNameList);
				
			}
		}
		
		return serviceDefinitionsInterfacesMap;
	}
	
	//-------------------------------------------------------------------------------------------------
	private Map<Long, List<String>> mapInterfacesToProviders(List<OrchestratorStore> entryList) {
		logger.debug("mapIntrefacesToProviders started...");
		
		final Map<Long, List<String>> providersInterfacesMap = new HashMap<>();
		
		for (OrchestratorStore orchestratorStore : entryList) {
			Assert.isTrue(!orchestratorStore.isForeign(), "Provider is foreign");
			
			final Long providerSystemId = orchestratorStore.getProviderSystemId();
			
			
			if (providersInterfacesMap.containsKey(providerSystemId)) {
				
				providersInterfacesMap.get(providerSystemId ).add(orchestratorStore.getServiceInterface().getInterfaceName());
			
			}else {
				
				final List<String> serviceInterfaceList = new ArrayList<>();
				serviceInterfaceList.add(orchestratorStore.getServiceInterface().getInterfaceName());
				providersInterfacesMap.put(providerSystemId, serviceInterfaceList);
				
			}
		}
		
		return providersInterfacesMap;
	}

	//-------------------------------------------------------------------------------------------------
	private Map<Long, String> mapServiceDefinitionsToServiceDefinitionIds(List<OrchestratorStore> entryList) {
		logger.debug("mapServiceDefinitionsToServiceDefinitionIds started...");
		
		final Map<Long, String> serviceDefinitionsIdToStringMap = new HashMap<>();
		
		for (OrchestratorStore orchestratorStore : entryList) {
			final ServiceDefinition serviceDefinition = orchestratorStore.getServiceDefinition();
			
			if (!serviceDefinitionsIdToStringMap.containsKey(serviceDefinition.getId())) {
				
				serviceDefinitionsIdToStringMap.put(serviceDefinition.getId(), serviceDefinition.getServiceDefinition());
			
			}
		}
		
		return serviceDefinitionsIdToStringMap;
	}

	//-------------------------------------------------------------------------------------------------
	private List<ServiceRegistryResponseDTO> filterQueryResultByInterfaces(
			Map<Long, List<String>> providerIdInterfaceIdsMap, ServiceQueryResultDTO queryResult) {
		logger.debug("filterQueryResultByInterfaces started...");
		
		final List<ServiceRegistryResponseDTO> filterdResults = new ArrayList<>();
		
		final List<ServiceRegistryResponseDTO> result = queryResult.getServiceQueryData();
		
		for (final ServiceRegistryResponseDTO serviceRegistryResponseDTO : result) {
			
			final Long providerIdFromResult = serviceRegistryResponseDTO.getProvider().getId();
			final List<ServiceInterfaceResponseDTO> interfaceListFromResult = serviceRegistryResponseDTO.getInterfaces();
			final List<ServiceInterfaceResponseDTO> filteredInterfaceList = new ArrayList<>();
			
			if (providerIdInterfaceIdsMap.containsKey(providerIdFromResult)) {
				
				final List<String> interfaceListFromRequest = providerIdInterfaceIdsMap.get(providerIdFromResult);
				for (ServiceInterfaceResponseDTO interfaceResponseDTO : interfaceListFromResult) {

					if (interfaceListFromRequest.contains(interfaceResponseDTO.getInterfaceName())) {
						filteredInterfaceList.add(interfaceResponseDTO);
					}
				} 
			}
			
			if (!filteredInterfaceList.isEmpty()) {
				
				serviceRegistryResponseDTO.setInterfaces(filteredInterfaceList);
				filterdResults.add(serviceRegistryResponseDTO);
			}
			
		}
		
		return filterdResults;
	}
}
