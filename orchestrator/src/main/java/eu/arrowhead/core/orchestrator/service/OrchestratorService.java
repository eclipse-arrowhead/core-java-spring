package eu.arrowhead.core.orchestrator.service;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.entity.System;
import eu.arrowhead.common.database.repository.SystemRepository;
import eu.arrowhead.common.dto.CloudRequestDTO;
import eu.arrowhead.common.dto.DTOConverter;
import eu.arrowhead.common.dto.OrchestrationFlags;
import eu.arrowhead.common.dto.OrchestrationFlags.Flag;
import eu.arrowhead.common.dto.OrchestrationFormRequestDTO;
import eu.arrowhead.common.dto.OrchestrationResponseDTO;
import eu.arrowhead.common.dto.OrchestrationResultDTO;
import eu.arrowhead.common.dto.OrchestratorWarnings;
import eu.arrowhead.common.dto.PreferredProviderDataDTO;
import eu.arrowhead.common.dto.ServiceQueryFormDTO;
import eu.arrowhead.common.dto.ServiceQueryResultDTO;
import eu.arrowhead.common.dto.ServiceRegistryResponseDTO;
import eu.arrowhead.common.dto.SystemRequestDTO;
import eu.arrowhead.common.dto.SystemResponseDTO;
import eu.arrowhead.common.exception.InvalidParameterException;

@Service
public class OrchestratorService {
	
	//=================================================================================================
	// members
	
	private static final Logger logger = LogManager.getLogger(OrchestratorService.class);
	private static final String NULL_PARAMETER_ERROR_MESSAGE = " is null.";
	private static final String NULL_OR_BLANK_PARAMETER_ERROR_MESSAGE = " is null or blank.";
	private static final String LESS_THAN_ONE_ERROR_MESSAGE= " must be greater than zero.";
	private static final String NOT_IN_DB_ERROR_MESSAGE = " is not available in database";
	
	private static final int EXPIRING_TIME_IN_MINUTES = 2;
	
	@Autowired
	private OrchestratorDriver orchestratorDriver;
	
	@Autowired
	private SystemRepository systemRepository;
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	/**
	 * This method represents the orchestration process where the requester System is NOT in the local Cloud. This means that the Gatekeeper made sure
	 * that this request from the remote Orchestrator can be satisfied in this Cloud. (Gatekeeper polled the Service Registry and Authorization
	 * Systems.)
	 */
	public OrchestrationResponseDTO externalServiceRequest(final OrchestrationFormRequestDTO request) {
		logger.debug("externalServiceRequest started ...");
		checkExternalServiceRequestForm(request);
		
		// Querying the Service Registry to get the list of Provider Systems
		final OrchestrationFlags flags = request.getOrchestrationFlags();
		final ServiceQueryResultDTO queryResult = orchestratorDriver.queryServiceRegistry(request.getRequestedService(), flags.get(Flag.METADATA_SEARCH), flags.get(Flag.PING_PROVIDERS));
		
		List<ServiceRegistryResponseDTO> queryData = queryResult.getServiceQueryData();
	    // If necessary, removing the non-preferred providers from the SR response. (If necessary, matchmaking is done after this at the request sender Cloud.)
		if (flags.get(Flag.ONLY_PREFERRED)) {  
			// This request contains only local preferred systems, since this request came from another cloud, but the de-boxing is necessary
			final Set<PreferredProviderDataDTO> localProviders = request.getPreferredProviders().stream().filter(p -> p.isLocal()).collect(Collectors.toSet());
			queryData = removeNonPreferred(queryData, localProviders);
		}

		logger.debug("externalServiceRequest finished with {} service providers.", queryData.size());
		
		return compileOrchestrationResponse(queryData, request);
	}

	//-------------------------------------------------------------------------------------------------	
	public OrchestrationResponseDTO triggerInterCloud(
			final OrchestrationFormRequestDTO orchestratorFormRequestDTO) {
		logger.debug("triggerInterCloud started ...");
		
		//TODO implement method logic here
		return null;
	}

	//-------------------------------------------------------------------------------------------------	
	public OrchestrationResponseDTO orchestrationFromStore(
			final OrchestrationFormRequestDTO orchestratorFormRequestDTO) {
		logger.debug("orchestrationFromStore started ...");
		

		//TODO implement additional method logic here
		return null;
	}

	//-------------------------------------------------------------------------------------------------	
	public OrchestrationResponseDTO dynamicOrchestration(
			final OrchestrationFormRequestDTO orchestratorFormRequestDTO) {
		logger.debug("dynamicOrchestration started ...");
		
		//TODO implement method logic here
		return null;
	}
	
	//-------------------------------------------------------------------------------------------------
	public OrchestrationResponseDTO storeOchestrationProcessResponse(long systemId) {
		logger.debug("storeOchestrationProcessResponse started ...");
		
		final SystemResponseDTO validConsumerSystemResponseDTO  =  validateSystemId(systemId);
		final SystemRequestDTO systemRequestDTO = DTOConverter.convertSystemResponseDTOToSystemRequestDTO(validConsumerSystemResponseDTO);
		
	    final OrchestrationFormRequestDTO orchestrationFormRequestDTO = new OrchestrationFormRequestDTO.Builder(systemRequestDTO).build();
	    orchestrationFormRequestDTO.validateCrossParameterConstraints();

	    return orchestrationFromStore(orchestrationFormRequestDTO);

	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private void checkExternalServiceRequestForm(final OrchestrationFormRequestDTO request) {
		logger.debug("checkExternalServiceRequestForm started ...");
		
		if (request == null) {
			throw new InvalidParameterException("request" + NULL_PARAMETER_ERROR_MESSAGE);
		}
		
		request.validateCrossParameterConstraints();
		
		// Requested service
		checkRequestedServiceForm(request.getRequestedService());
		
		// Preferred Providers
		checkPreferredProviders(request.getPreferredProviders(), false);
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
	private List<ServiceRegistryResponseDTO> removeNonPreferred(final List<ServiceRegistryResponseDTO> srList, final Set<PreferredProviderDataDTO> preferredProviders) {
		logger.debug("removeNonPreferred started...");
		
		final List<ServiceRegistryResponseDTO> result = new ArrayList<>();
		for (final ServiceRegistryResponseDTO srResult : srList) {
			for (final PreferredProviderDataDTO preferredProvider : preferredProviders) {
				if (equalsSystemInResponseAndRequest(srResult.getProvider(), preferredProvider.getProviderSystem())) {
					result.add(srResult);
				}
			}
		}
		
		logger.debug("removeNonPreferred returns with {} entries.", result.size());
		
		return result;
	}
	
	//-------------------------------------------------------------------------------------------------
	private boolean equalsSystemInResponseAndRequest(final SystemResponseDTO response, final SystemRequestDTO request) {
		logger.debug("equalsSystemInResponseAndRequest started...");
		
		return response.getSystemName().equalsIgnoreCase(request.getSystemName()) &&
			   response.getAddress().equalsIgnoreCase(request.getAddress()) &&
			   response.getPort() == request.getPort().intValue();
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
		
	    logger.debug("compileOrchestrationResponse creates {} orchestration form", orList.size());

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
		
		final SystemResponseDTO systemResponseDTO = orchestratorDriver.queryServiceRegistryByIdSystemId(systemId);
		
		
		return systemResponseDTO;
	}

}