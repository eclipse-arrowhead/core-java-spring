/********************************************************************************
 * Copyright (c) 2019 AITIA
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   AITIA - implementation
 *   Arrowhead Consortia - conceptualization
 ********************************************************************************/

package eu.arrowhead.core.orchestrator.service;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.entity.OrchestratorStore;
import eu.arrowhead.common.database.entity.OrchestratorStoreFlexible;
import eu.arrowhead.common.database.entity.ServiceDefinition;
import eu.arrowhead.common.dto.internal.CloudResponseDTO;
import eu.arrowhead.common.dto.internal.DTOConverter;
import eu.arrowhead.common.dto.internal.DTOUtilities;
import eu.arrowhead.common.dto.internal.GSDPollResponseDTO;
import eu.arrowhead.common.dto.internal.GSDQueryFormDTO;
import eu.arrowhead.common.dto.internal.GSDQueryResultDTO;
import eu.arrowhead.common.dto.internal.ICNRequestFormDTO;
import eu.arrowhead.common.dto.internal.ICNResultDTO;
import eu.arrowhead.common.dto.internal.OrchestratorStoreResponseDTO;
import eu.arrowhead.common.dto.internal.QoSReservationListResponseDTO;
import eu.arrowhead.common.dto.internal.QoSReservationRequestDTO;
import eu.arrowhead.common.dto.internal.QoSTemporaryLockRequestDTO;
import eu.arrowhead.common.dto.internal.QoSTemporaryLockResponseDTO;
import eu.arrowhead.common.dto.internal.RelayRequestDTO;
import eu.arrowhead.common.dto.internal.RelayResponseDTO;
import eu.arrowhead.common.dto.shared.CloudRequestDTO;
import eu.arrowhead.common.dto.shared.OrchestrationFlags;
import eu.arrowhead.common.dto.shared.OrchestrationFlags.Flag;
import eu.arrowhead.common.dto.shared.OrchestrationFormRequestDTO;
import eu.arrowhead.common.dto.shared.OrchestrationResponseDTO;
import eu.arrowhead.common.dto.shared.OrchestrationResultDTO;
import eu.arrowhead.common.dto.shared.OrchestratorWarnings;
import eu.arrowhead.common.dto.shared.PreferredProviderDataDTO;
import eu.arrowhead.common.dto.shared.QoSMeasurementAttributesFormDTO;
import eu.arrowhead.common.dto.shared.ServiceInterfaceResponseDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryFormDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryResultDTO;
import eu.arrowhead.common.dto.shared.ServiceRegistryResponseDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.common.dto.shared.SystemResponseDTO;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.core.orchestrator.database.service.OrchestratorStoreDBService;
import eu.arrowhead.core.orchestrator.matchmaking.CloudMatchmakingAlgorithm;
import eu.arrowhead.core.orchestrator.matchmaking.CloudMatchmakingParameters;
import eu.arrowhead.core.orchestrator.matchmaking.InterCloudProviderMatchmakingAlgorithm;
import eu.arrowhead.core.orchestrator.matchmaking.InterCloudProviderMatchmakingParameters;
import eu.arrowhead.core.orchestrator.matchmaking.IntraCloudProviderMatchmakingAlgorithm;
import eu.arrowhead.core.orchestrator.matchmaking.IntraCloudProviderMatchmakingParameters;
import eu.arrowhead.core.qos.manager.QoSManager;

@Service
public class OrchestratorService {
	
	//=================================================================================================
	// members
	
	private static final String NULL_PARAMETER_ERROR_MESSAGE = " is null.";
	private static final String NULL_OR_BLANK_PARAMETER_ERROR_MESSAGE = " is null or blank.";
	private static final String LESS_THAN_ONE_ERROR_MESSAGE= " must be greater than zero.";
	private static final String MORE_THAN_ONE_ERROR_MESSAGE= " must not have more than one element.";
	
	public static final int EXPIRING_TIME_IN_MINUTES = 2;
	private static final int extraServiceTimeSeconds = 5; // due to orchestration overhead
	
	private static final Logger logger = LogManager.getLogger(OrchestratorService.class);
	
	@Autowired
	private OrchestratorDriver orchestratorDriver;
	
	@Autowired
	private OrchestratorFlexibleDriver orchestratorFlexibleDriver;
	
	@Autowired
	private OrchestratorStoreDBService orchestratorStoreDBService;
	
	@Resource(name = CoreCommonConstants.INTRA_CLOUD_PROVIDER_MATCHMAKER)
	private IntraCloudProviderMatchmakingAlgorithm intraCloudProviderMatchmaker;
	
	@Resource(name = CoreCommonConstants.INTER_CLOUD_PROVIDER_MATCHMAKER)
	private InterCloudProviderMatchmakingAlgorithm interCloudProviderMatchmaker;
	
	@Resource(name = CoreCommonConstants.CLOUD_MATCHMAKER)
	private CloudMatchmakingAlgorithm cloudMatchmaker;
	
	@Resource(name = CoreCommonConstants.QOSMANAGER)
	private QoSManager qosManager;
	
	@Value(CoreCommonConstants.$ORCHESTRATOR_USE_FLEXIBLE_STORE_WD)
	private boolean useFlexibleStore;
	
	@Value(CoreCommonConstants.$ORCHESTRATOR_IS_GATEKEEPER_PRESENT_WD)
	private boolean gateKeeperIsPresent;
	
	@Value(CoreCommonConstants.$QOS_ENABLED_WD)
	private boolean qosEnabled;
	
	@Value(CoreCommonConstants.$QOS_MAX_RESERVATION_DURATION_WD)
	private int maxReservationDuration; // in seconds
	
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
		final ServiceQueryResultDTO queryResult = orchestratorDriver.queryServiceRegistry(request.getRequestedService(), flags);
		
		List<ServiceRegistryResponseDTO> queryData = queryResult.getServiceQueryData();
	    // If necessary, removing the non-preferred providers from the SR response. (If necessary, matchmaking is done after this at the request sender Cloud.)
		if (flags.get(Flag.ONLY_PREFERRED)) {  
			// This request contains only local preferred systems, since this request came from another cloud, but the unboxing is necessary
			final List<PreferredProviderDataDTO> localProviders = request.getPreferredProviders().stream().filter(p -> p.isLocal()).collect(Collectors.toList());
			queryData = removeNonPreferred(queryData, localProviders);
		}

		
		List<OrchestrationResultDTO> orList = compileOrchestrationResponse(queryData);
		orList = qosManager.filterReservedProviders(orList, request.getRequesterSystem()); // to reduce the number of results before token generation
		
		if (qosEnabled && flags.get(Flag.ENABLE_QOS)) {
			orList = calculateAndFilterOnServiceTime(orList, request);			
		}
		
		// Generate the authorization tokens if it is requested based on the service security (modifies the orList)
		List<OrchestrationResultDTO> orListWithTokens = orchestratorDriver.generateAuthTokens(request, orList);
	    
		orListWithTokens = qosManager.filterReservedProviders(orListWithTokens, request.getRequesterSystem()); // token generation can be slow, so we have to check for new reservations
	    
	    logger.debug("externalServiceRequest finished with {} service providers.", orList.size());

	    return new OrchestrationResponseDTO(orListWithTokens);
	}

	//-------------------------------------------------------------------------------------------------	
	public OrchestrationResponseDTO triggerInterCloud(final OrchestrationFormRequestDTO request) {
		logger.debug("triggerInterCloud started ...");
		
		if (request == null) {
			throw new InvalidParameterException("request" + NULL_PARAMETER_ERROR_MESSAGE);
		}
		
		// necessary, because we want to use a flag value when we call the check method
		final OrchestrationFlags flags = request.getOrchestrationFlags();
		checkServiceRequestForm(request, isInterCloudOrchestrationPossible(flags));
		
		final GSDQueryResultDTO gsdResult = callGSD(request, flags);
		if (gsdResult == null || gsdResult.getResults() == null || gsdResult.getResults().isEmpty()) {
			// Return empty response
			return new OrchestrationResponseDTO();
		}

		final Set<RelayResponseDTO> verifiedRelays = new HashSet<>();
		if (flags.get(Flag.ENABLE_QOS)) {
			// Pre-verification in order to choose an appropriate cloud
			final List<PreferredProviderDataDTO> verifiedProviders = new ArrayList<>();
			final List<GSDPollResponseDTO> verifiedResults = qosManager.preVerifyInterCloudServices(gsdResult.getResults(), request);
			for (final GSDPollResponseDTO result : verifiedResults) {
				for (final QoSMeasurementAttributesFormDTO measurement : result.getQosMeasurements()) {
					final PreferredProviderDataDTO preferredProviderData = new PreferredProviderDataDTO();
					preferredProviderData.setProviderSystem(DTOConverter.convertSystemResponseDTOToSystemRequestDTO(measurement.getServiceRegistryEntry().getProvider()));
					preferredProviderData.setProviderCloud(DTOConverter.convertCloudResponseDTOToCloudRequestDTO(result.getProviderCloud()));
					verifiedProviders.add(preferredProviderData);
					verifiedRelays.addAll(result.getVerifiedRelays());
				}
			}
			gsdResult.setResults(verifiedResults);
			if (flags.get(Flag.ONLY_PREFERRED)) {
				request.getPreferredProviders().retainAll(verifiedProviders);
			} else {
				request.setPreferredProviders(verifiedProviders);				
			}
		}
		
		final boolean onlyPreferredMatchmakingParam = flags.get(Flag.ENABLE_QOS) ? true : flags.get(Flag.ONLY_PREFERRED);
		final CloudMatchmakingParameters iCCMparams = new CloudMatchmakingParameters(gsdResult, getPreferredClouds(request.getPreferredProviders()), onlyPreferredMatchmakingParam);
		final CloudResponseDTO targetCloud = cloudMatchmaker.doMatchmaking(iCCMparams);
        if (targetCloud == null || Utilities.isEmpty(targetCloud.getName())) {
        	// Return empty response
            return new OrchestrationResponseDTO();
 		}	
        
        return callInterCloudNegotiation(request, targetCloud, flags, DTOConverter.convertRelayResponseDTOCollectionToRelayRequestDTOList(verifiedRelays));
	}

	//-------------------------------------------------------------------------------------------------	
	public OrchestrationResponseDTO topPriorityEntriesOrchestrationProcess(final OrchestrationFormRequestDTO orchestrationFormRequestDTO, final Long systemId) {
		logger.debug("topPriorityEntriesOrchestrationProcess started ...");		
		
		if (orchestrationFormRequestDTO == null) {
			throw new InvalidParameterException("request" + NULL_PARAMETER_ERROR_MESSAGE);
		}
		
		orchestrationFormRequestDTO.validateCrossParameterConstraints();
			
		if (systemId != null && systemId < 1) {
			throw new InvalidParameterException("systemId " + LESS_THAN_ONE_ERROR_MESSAGE);
		}
		
		final long validSystemId = (systemId != null ? systemId.longValue() : validateSystemRequestDTO(orchestrationFormRequestDTO.getRequesterSystem()));
		final SystemRequestDTO consumerSystemRequestDTO = orchestrationFormRequestDTO.getRequesterSystem();
		
		final List<OrchestratorStore> entryList = orchestratorStoreDBService.getAllTopPriorityOrchestratorStoreEntriesByConsumerSystemId(validSystemId);
		if (entryList == null || entryList.isEmpty()) {
			return new OrchestrationResponseDTO(); // empty response
		}
		
		final List<ServiceRegistryResponseDTO> crossCheckedEntryList = crossCheckTopPriorityEntries(entryList, orchestrationFormRequestDTO, consumerSystemRequestDTO);
		if (crossCheckedEntryList == null || crossCheckedEntryList.isEmpty()) {
			return new OrchestrationResponseDTO(); // empty response
		}
		
		List<OrchestrationResultDTO> orList = compileOrchestrationResponse(crossCheckedEntryList);
		orList = qosManager.filterReservedProviders(orList, orchestrationFormRequestDTO.getRequesterSystem()); // to reduce the number of results before token generation

	    // Generate the authorization tokens if it is requested based on the service security (modifies the orList)
	    orList = orchestratorDriver.generateAuthTokens(orchestrationFormRequestDTO, orList);
	
	    orList = qosManager.filterReservedProviders(orList, orchestrationFormRequestDTO.getRequesterSystem()); // token generation can be slow, so we have to check for new reservations

	    return new OrchestrationResponseDTO(orList);
	}
	
	//-------------------------------------------------------------------------------------------------
	public OrchestrationResponseDTO orchestrationFromStore(final OrchestrationFormRequestDTO orchestrationFormRequestDTO) { 
		logger.debug("orchestrationFromStore started ...");		
		
		if (useFlexibleStore) {
			return orchestrationFromFlexibleStore(orchestrationFormRequestDTO);
		} else {
			return orchestrationFromOriginalStore(orchestrationFormRequestDTO);
		}
	}

	//-------------------------------------------------------------------------------------------------	
	public OrchestrationResponseDTO orchestrationFromOriginalStore(final OrchestrationFormRequestDTO orchestrationFormRequestDTO) {
		logger.debug("orchestrationFromStore started ...");		
		
		if (orchestrationFormRequestDTO == null) {
			throw new InvalidParameterException("request" + NULL_PARAMETER_ERROR_MESSAGE);
		}
		
		if (orchestrationFormRequestDTO.getRequestedService() == null) {
			return topPriorityEntriesOrchestrationProcess(orchestrationFormRequestDTO, null);
		}
	
		orchestrationFormRequestDTO.validateCrossParameterConstraints();

		final List<OrchestratorStore> entryList = getOrchestrationStoreEntries(orchestrationFormRequestDTO.getRequesterSystem(), orchestrationFormRequestDTO.getRequestedService());	        
		if (entryList == null || entryList.isEmpty()) {
			return new OrchestrationResponseDTO(); // empty response
		}
		
		final List<ServiceRegistryResponseDTO> authorizedLocalServiceRegistryEntries = getAuthorizedServiceRegistryEntries(entryList, orchestrationFormRequestDTO);
        
		final OrchestrationResponseDTO result = getHighestPriorityCurrentlyWorkingStoreEntryFromEntryList(orchestrationFormRequestDTO, entryList, authorizedLocalServiceRegistryEntries);
		
		final List<OrchestrationResultDTO> orList = qosManager.filterReservedProviders(result.getResponse(), orchestrationFormRequestDTO.getRequesterSystem());
		
		return new OrchestrationResponseDTO(orList); 
	}

	//-------------------------------------------------------------------------------------------------	
	/**
	 * Represents the regular orchestration process where the requester system is in the local Cloud. In this process the
     * <i>Orchestrator Store</i> is ignored, and the Orchestrator first tries to find a provider for the requested service in the local Cloud.
     * If that fails but the <i>enableInterCloud</i> flag is set to true, the Orchestrator tries to find a provider in other Clouds.
	 */
	public OrchestrationResponseDTO dynamicOrchestration(final OrchestrationFormRequestDTO request, final boolean skipAuthorization) { 
		logger.debug("dynamicOrchestration started ...");

		// necessary, because we want to use a flag value when we call the check method
		if (request == null) {
			throw new InvalidParameterException("request" + NULL_PARAMETER_ERROR_MESSAGE);
		}
		
		final OrchestrationFlags flags = request.getOrchestrationFlags();
		checkServiceRequestForm(request, isInterCloudOrchestrationPossible(flags));
		
		// Querying the Service Registry to get the list of Provider Systems
		final ServiceQueryResultDTO queryResult = orchestratorDriver.queryServiceRegistry(request.getRequestedService(), flags);
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
		
		if (!skipAuthorization) {
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

		List<OrchestrationResultDTO> orList = compileOrchestrationResponse(queryData);
		orList = qosManager.filterReservedProviders(orList, request.getRequesterSystem());
		if (orList.isEmpty()) {
			if (isInterCloudOrchestrationPossible(flags)) {
				// no result after filter reserved providers => we try with other clouds
				logger.debug("dynamicOrchestration: no free provider in this cloud  => moving to Inter-Cloud orchestration.");
				return triggerInterCloud(request);
			} else {
				return new OrchestrationResponseDTO(); // empty response
			}
		}
		
		final boolean needReservation = qosEnabled && flags.get(Flag.ENABLE_QOS) && request.getCommands().containsKey(OrchestrationFormRequestDTO.QOS_COMMAND_EXCLUSIVITY);
		
		if (needReservation) {
			orList = qosManager.reserveProvidersTemporarily(orList, request.getRequesterSystem());
 		} 
		
		if (flags.get(Flag.ENABLE_QOS)) {
			orList = qosManager.verifyIntraCloudServices(orList, request);
			if (orList.isEmpty()) {
				if (isInterCloudOrchestrationPossible(flags)) {
					// no result after verify providers => we try with other clouds
					logger.debug("dynamicOrchestration: no verified provider in this cloud  => moving to Inter-Cloud orchestration.");
					return triggerInterCloud(request);
				} else {
					return new OrchestrationResponseDTO(); // empty response
				}
			}
		}
		
		if (!needReservation) {
			orList = qosManager.filterReservedProviders(orList, request.getRequesterSystem());
			if (orList.isEmpty()) {
				if (isInterCloudOrchestrationPossible(flags)) {
					// no result after filter reserved providers => we try with other clouds
					logger.debug("dynamicOrchestration: no free provider in this cloud  => moving to Inter-Cloud orchestration.");
					return triggerInterCloud(request);
				} else {
					return new OrchestrationResponseDTO(); // empty response
				}
			}
		}
		
		if (qosEnabled) {
			// Generate the authorization tokens if it is requested based on the service security (modifies the orList)
		    orList = orchestratorDriver.generateAuthTokens(request, orList);
			if (!needReservation) {
				orList = qosManager.filterReservedProviders(orList, request.getRequesterSystem());
				if (orList.isEmpty()) {
					if (isInterCloudOrchestrationPossible(flags)) {
						// no result after filter reserved providers => we try with other clouds
						logger.debug("dynamicOrchestration: no free provider in this cloud  => moving to Inter-Cloud orchestration.");
						return triggerInterCloud(request);
					} else {
						return new OrchestrationResponseDTO(); // empty response
					}

				}
			}
		}
		
		// If matchmaking is requested, we pick out 1 ServiceRegistryEntry entity from the list.
		if (flags.get(Flag.MATCHMAKING)) {
			final IntraCloudProviderMatchmakingParameters params = new IntraCloudProviderMatchmakingParameters(localProviders);
			// set additional parameters here if you use a different matchmaking algorithm
			final OrchestrationResultDTO selected = intraCloudProviderMatchmaker.doMatchmaking(orList, params);
			if (needReservation) {
				qosManager.confirmReservation(selected, orList, request.getRequesterSystem());
			}
			orList.clear();
			orList.add(selected);
		}

		// all the filtering is done
		logger.debug("dynamicOrchestration finished with {} service providers.", queryData.size());
		
		if (!qosEnabled) {
			// Generate the authorization tokens if it is requested based on the service security (modifies the orList)
			orList = orchestratorDriver.generateAuthTokens(request, orList);
		}

	    return new OrchestrationResponseDTO(orList);
	}
	
	//-------------------------------------------------------------------------------------------------
	public OrchestrationResponseDTO storeOchestrationProcessResponse(final long systemId) {
		logger.debug("storeOchestrationProcessResponse started ...");
		
		final SystemResponseDTO validConsumerSystemResponseDTO  =  validateSystemId(systemId);
		final SystemRequestDTO systemRequestDTO = DTOConverter.convertSystemResponseDTOToSystemRequestDTO(validConsumerSystemResponseDTO);
	    final OrchestrationFormRequestDTO orchestrationFormRequestDTO = new OrchestrationFormRequestDTO.Builder(systemRequestDTO).build();

	    return topPriorityEntriesOrchestrationProcess(orchestrationFormRequestDTO, systemId);
	}
	
	//-------------------------------------------------------------------------------------------------
	public QoSReservationListResponseDTO getAllQoSReservationResponse() {
		return DTOConverter.convertQoSReservationListToQoSReservationListResponseDTO(qosManager.fetchAllReservation());
	}

	//-------------------------------------------------------------------------------------------------
	public QoSTemporaryLockResponseDTO lockProvidersTemporarily(final QoSTemporaryLockRequestDTO request) {
		logger.debug("lockProvidersTemporarily started ...");
		
		checkQoSReservationRequestDTO(request);
		if (request.getOrList() == null || request.getOrList().isEmpty()) {
			return new QoSTemporaryLockResponseDTO();
		}
		
		return new QoSTemporaryLockResponseDTO(qosManager.reserveProvidersTemporarily(request.getOrList(), request.getRequester()));
	}
	
	//-------------------------------------------------------------------------------------------------
	public void confirmProviderReservation(final QoSReservationRequestDTO request) {
		logger.debug("confirmProviderReservation started ...");
		
		checkQoSReservationRequestDTO(request);
		qosManager.confirmReservation(request.getSelected(), request.getOrList(), request.getRequester());
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
		
		if (qosEnabled && request.getOrchestrationFlags().get(Flag.ENABLE_QOS) && request.getCommands().containsKey(OrchestrationFormRequestDTO.QOS_COMMAND_EXCLUSIVITY)) {
			try {
				final int exclusivityTime = Integer.parseInt(request.getCommands().get(OrchestrationFormRequestDTO.QOS_COMMAND_EXCLUSIVITY));
				if (exclusivityTime <= 0 || exclusivityTime > maxReservationDuration) {
					throw new InvalidParameterException("Exclusivity time must be specified in seconds. Valid interval: [1, " + maxReservationDuration + "].");
				}
			} catch (final NumberFormatException ex) {
				throw new InvalidParameterException("Exclusivity time is in the wrong format.");
			}
		}
		
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
	private void checkQoSReservationRequestDTO(final QoSTemporaryLockRequestDTO request) {
		logger.debug("checkQoSReservationRequestDTO started...");
		
		if (request == null) {
			throw new InvalidParameterException("QoSReservationRequestDTO is null");
		}
		
		if (request.getRequester() == null) {
			throw new InvalidParameterException("Requester system is null");
		}
		
		if (Utilities.isEmpty(request.getRequester().getSystemName())) {
			throw new InvalidParameterException("Requester system name is null or empty");
		}
		
		if (Utilities.isEmpty(request.getRequester().getAddress())) {
			throw new InvalidParameterException("Requester system address is null or empty");
		}
		
		if (request.getRequester().getPort() == null) {
			throw new InvalidParameterException("Requester system port is null");
		}
		
		if (request instanceof QoSReservationRequestDTO) {
			final QoSReservationRequestDTO req = (QoSReservationRequestDTO) request;
			if (req.getSelected() == null) {
				throw new InvalidParameterException("Selected ORCH result is null");
			}
			
			if (req.getSelected().getProvider() == null) {
				throw new InvalidParameterException("Selected provider is null");
			}
			
			if (req.getSelected().getService() == null) {
				throw new InvalidParameterException("Selected service is null");
			}
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
	private List<OrchestrationResultDTO> calculateAndFilterOnServiceTime(final List<OrchestrationResultDTO> orList, final OrchestrationFormRequestDTO request) {
		logger.debug("filterReservedProviders started ...");		
		Assert.notNull(orList, "'orList' is null.");
		
		if (orList.isEmpty()) {
			return orList;
		}
		
		final List<OrchestrationResultDTO> result = new ArrayList<>();
		for (final OrchestrationResultDTO dto : orList) {
			int calculatedServiceTime = OrchestratorUtils.calculateServiceTime(dto.getMetadata(), request.getCommands());
			
			if (calculatedServiceTime == 0 || dto.getWarnings().contains(OrchestratorWarnings.TTL_EXPIRED)) {
				continue;
			}
			
			if (calculatedServiceTime > 0 && !dto.getWarnings().contains(OrchestratorWarnings.TTL_EXPIRED)) {
				calculatedServiceTime += extraServiceTimeSeconds; // give some extra seconds because of orchestration overhead
				dto.getMetadata().put(OrchestratorDriver.KEY_CALCULATED_SERVICE_TIME_FRAME, String.valueOf(calculatedServiceTime));
				
				// adjust TTL warnings
				dto.getWarnings().remove(OrchestratorWarnings.TTL_UNKNOWN);
				if (!dto.getWarnings().contains(OrchestratorWarnings.TTL_EXPIRING) &&
					calculatedServiceTime <= EXPIRING_TIME_IN_MINUTES * CommonConstants.CONVERSION_SECOND_TO_MINUTE) {
					dto.getWarnings().add(OrchestratorWarnings.TTL_EXPIRING);
				}				
			}
			
			result.add(dto);
		}		
		return result;
	}
	
	
	//-------------------------------------------------------------------------------------------------
	private List<OrchestrationResultDTO> compileOrchestrationResponse(final List<ServiceRegistryResponseDTO> srList) {
		logger.debug("compileOrchestrationResponse started...");
		
		final List<OrchestrationResultDTO> orList = new ArrayList<>(srList.size());
		for (final ServiceRegistryResponseDTO entry : srList) {
			final OrchestrationResultDTO result = new OrchestrationResultDTO(entry.getProvider(), entry.getServiceDefinition(), entry.getServiceUri(), entry.getSecure(), entry.getMetadata(), 
																			 entry.getInterfaces(), entry.getVersion());

			if (result.getMetadata() == null) {
				result.setMetadata(new HashMap<>());
			}
			result.setWarnings(calculateOrchestratorWarnings(entry));
			
			orList.add(result);
		}
		
	    logger.debug("compileOrchestrationResponse creates {} orchestration forms", orList.size());

		return orList;
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
		if (systemResponseDTO == null) {
			throw new InvalidParameterException("SystemResponseDTO " + NULL_PARAMETER_ERROR_MESSAGE);
		}
		
		return systemResponseDTO.getId();
	}
	
	//-------------------------------------------------------------------------------------------------
	private List<OrchestratorStore> getOrchestrationStoreEntries(final SystemRequestDTO requesterSystem, final ServiceQueryFormDTO requestedService) {
		logger.debug("getOrchestrationStoreEntries started...");
		
		if (requesterSystem == null) {
			throw new InvalidParameterException("ConsumerSystem " + NULL_PARAMETER_ERROR_MESSAGE);
		}
		
		if (Utilities.isEmpty(requestedService.getServiceDefinitionRequirement())) {
			throw new InvalidParameterException("ServiceDefinitionRequirement " + NULL_OR_BLANK_PARAMETER_ERROR_MESSAGE);
		}
		
		final String serviceDefinitionName = requestedService.getServiceDefinitionRequirement().trim().toLowerCase();
		
		if (requestedService.getInterfaceRequirements() == null || requestedService.getInterfaceRequirements().isEmpty() || Utilities.isEmpty(requestedService.getInterfaceRequirements().get(0))) {
			throw new InvalidParameterException("InterfaceRequirement " + NULL_OR_BLANK_PARAMETER_ERROR_MESSAGE);
		}
		
		if (requestedService.getInterfaceRequirements().size() != 1) {
			throw new InvalidParameterException("InterfaceRequirement " + MORE_THAN_ONE_ERROR_MESSAGE);
		}
		
		final String serviceInterfaceName =  requestedService.getInterfaceRequirements().get(0).trim();
		
		final long consumerSystemId = validateSystemRequestDTO(requesterSystem);
		
		return orchestratorStoreDBService.getOrchestratorStoresByConsumerIdAndServiceDefinitionAndServiceInterface(consumerSystemId, serviceDefinitionName, serviceInterfaceName);
	}
	
	//-------------------------------------------------------------------------------------------------
	private List<ServiceRegistryResponseDTO> crossCheckTopPriorityEntries(final List<OrchestratorStore> entryList, final OrchestrationFormRequestDTO orchestrationFormRequestDTO,
																		  final SystemRequestDTO consumerSystem) {
		logger.debug("crossCheckTopPriorityEntries started...");
	    
		logger.debug("currently only local entries are allowed to be returned by topPriorityOrchestration");
	    final List<OrchestratorStore> onlyLocalEntryList = filterEntryListByForeign(entryList);
	    
	    if (onlyLocalEntryList.isEmpty()) {
	    	return List.of();
		}
	    
	    final Map<Long,String> serviceDefinitionsIdsMap = mapServiceDefinitionsToServiceDefinitionIds(onlyLocalEntryList); 
	    final Map<Long,List<String>> serviceDefinitionIdInterfaceMap = mapInterfacesToServiceDefinitions(onlyLocalEntryList);
	    final Map<Long,Set<String>> providerIdInterfaceMap = mapInterfacesToProviders(onlyLocalEntryList);
	    
	    final OrchestrationFlags flags = orchestrationFormRequestDTO.getOrchestrationFlags();	   
	    final List<ServiceRegistryResponseDTO> filteredServiceQueryResultDTOList = new ArrayList<>();
	    for (final Entry<Long, String> entry : serviceDefinitionsIdsMap.entrySet()) {
	    	final ServiceQueryFormDTO serviceQueryFormDTO = new ServiceQueryFormDTO();
	    	serviceQueryFormDTO.setServiceDefinitionRequirement(entry.getValue());
		    final List<String> interfaceRequirements = serviceDefinitionIdInterfaceMap.get(entry.getKey());
		    serviceQueryFormDTO.setInterfaceRequirements(interfaceRequirements);
		    
		    final ServiceQueryResultDTO queryResult = orchestratorDriver.queryServiceRegistry(serviceQueryFormDTO, flags);
		    final List<ServiceRegistryResponseDTO> filteredQueryResultByInterfaces = filterQueryResultByInterfaces(providerIdInterfaceMap, queryResult);	
		    final List<ServiceRegistryResponseDTO> filteredQueryResultByAuthorization = orchestratorDriver.queryAuthorization(consumerSystem, filteredQueryResultByInterfaces);	
		    
		    filteredServiceQueryResultDTOList.addAll(filteredQueryResultByAuthorization);
	    }
	    
		return filteredServiceQueryResultDTOList;
	}
	
	//-------------------------------------------------------------------------------------------------
	private List<OrchestratorStore> filterEntryListByForeign(final List<OrchestratorStore> entryList) {
		logger.debug(" filterEntryListByForeign started...");
		
		if (entryList == null || entryList.isEmpty()) {
			return List.of();
		}
		
		return entryList.stream().filter(e -> !e.isForeign()).collect(Collectors.toList()); 
	}

	//-------------------------------------------------------------------------------------------------
	private List<ServiceRegistryResponseDTO> getAuthorizedServiceRegistryEntries(final List<OrchestratorStore> entryList, final OrchestrationFormRequestDTO orchestrationFormRequestDTO) {
		logger.debug("crossCheckStoreEntries started...");
		
	    final List<OrchestratorStore> onlyLocalEntryList = filterEntryListByForeign(entryList);    
	    if (onlyLocalEntryList.isEmpty()) {
	    	return List.of();
		}
		
    	final ServiceQueryFormDTO serviceQueryFormDTO = orchestrationFormRequestDTO.getRequestedService();
    	serviceQueryFormDTO.setInterfaceRequirements(List.of(orchestrationFormRequestDTO.getRequestedService().getInterfaceRequirements().get(0)));
    	final OrchestrationFlags flags = orchestrationFormRequestDTO.getOrchestrationFlags();
		final ServiceQueryResultDTO serviceQueryResultDTO = orchestratorDriver.queryServiceRegistry(serviceQueryFormDTO, flags); 
		
		return orchestratorDriver.queryAuthorization(orchestrationFormRequestDTO.getRequesterSystem(), serviceQueryResultDTO.getServiceQueryData());
	}
	
	//-------------------------------------------------------------------------------------------------
	private Map<Long,List<String>> mapInterfacesToServiceDefinitions(final List<OrchestratorStore> entryList) {
		logger.debug("mapInterfacesToServiceDefinitions started...");
		
		final Map<Long,List<String>> serviceDefinitionsInterfacesMap = new HashMap<>();
		for (final OrchestratorStore orchestratorStore : entryList) {
			final Long serviceDefinitionId = orchestratorStore.getServiceDefinition().getId();
			final String interfaceName = orchestratorStore.getServiceInterface().getInterfaceName();
			
			serviceDefinitionsInterfacesMap.putIfAbsent(serviceDefinitionId, new ArrayList<>());
			if (!serviceDefinitionsInterfacesMap.get(serviceDefinitionId).contains(interfaceName)) {
				serviceDefinitionsInterfacesMap.get(serviceDefinitionId).add(interfaceName);
			}
		}
		
		return serviceDefinitionsInterfacesMap;
	}
	
	//-------------------------------------------------------------------------------------------------
	private Map<Long,Set<String>> mapInterfacesToProviders(final List<OrchestratorStore> entryList) {
		logger.debug("mapInterfacesToProviders started...");
		
		final Map<Long,Set<String>> providersInterfacesMap = new HashMap<>();
		
		for (final OrchestratorStore orchestratorStore : entryList) {
			Assert.isTrue(!orchestratorStore.isForeign(), "Provider is foreign");
			
			final Long providerSystemId = orchestratorStore.getProviderSystemId();
			providersInterfacesMap.putIfAbsent(providerSystemId, new HashSet<>());
			providersInterfacesMap.get(providerSystemId).add(orchestratorStore.getServiceInterface().getInterfaceName());
		}
		
		return providersInterfacesMap;
	}

	//-------------------------------------------------------------------------------------------------
	private Map<Long,String> mapServiceDefinitionsToServiceDefinitionIds(final List<OrchestratorStore> entryList) {
		logger.debug("mapServiceDefinitionsToServiceDefinitionIds started...");
		
		final Map<Long,String> serviceDefinitionsIdToStringMap = new HashMap<>();
		for (final OrchestratorStore orchestratorStore : entryList) {
			final ServiceDefinition serviceDefinition = orchestratorStore.getServiceDefinition();
			if (!serviceDefinitionsIdToStringMap.containsKey(serviceDefinition.getId())) {
				serviceDefinitionsIdToStringMap.put(serviceDefinition.getId(), serviceDefinition.getServiceDefinition());
			}
		}
		
		return serviceDefinitionsIdToStringMap;
	}

	//-------------------------------------------------------------------------------------------------
	private List<ServiceRegistryResponseDTO> filterQueryResultByInterfaces(final Map<Long, Set<String>> providerIdInterfaceIdsMap, final ServiceQueryResultDTO queryResult) {
		logger.debug("filterQueryResultByInterfaces started...");
		
		final List<ServiceRegistryResponseDTO> filteredResults = new ArrayList<>();
		
		final List<ServiceRegistryResponseDTO> result = queryResult.getServiceQueryData();
		for (final ServiceRegistryResponseDTO serviceRegistryResponseDTO : result) {
			final Long providerIdFromResult = serviceRegistryResponseDTO.getProvider().getId();
			final List<ServiceInterfaceResponseDTO> interfaceListFromResult = serviceRegistryResponseDTO.getInterfaces();
			final List<ServiceInterfaceResponseDTO> filteredInterfaceList = new ArrayList<>();
			
			if (providerIdInterfaceIdsMap.containsKey(providerIdFromResult)) {
				final Set<String> interfaceSetFromRequest = providerIdInterfaceIdsMap.get(providerIdFromResult);
				for (final ServiceInterfaceResponseDTO interfaceResponseDTO : interfaceListFromResult) {
					if (interfaceSetFromRequest.contains(interfaceResponseDTO.getInterfaceName())) {
						filteredInterfaceList.add(interfaceResponseDTO);
					}
				} 
			}
			
			if (!filteredInterfaceList.isEmpty()) {
				serviceRegistryResponseDTO.setInterfaces(filteredInterfaceList);
				filteredResults.add(serviceRegistryResponseDTO);
			}
		}
		
		return filteredResults;
	}
	
	//-------------------------------------------------------------------------------------------------
	private List<CloudRequestDTO> getPreferredClouds(final List<PreferredProviderDataDTO> preferredProviders) {
		logger.debug("getPreferredClouds started...");
		
		if (preferredProviders == null || preferredProviders.isEmpty()) {
			return List.of();
		}
		
		final List<CloudRequestDTO> preferredClouds = new ArrayList<>(preferredProviders.size());
		for (final PreferredProviderDataDTO provider : preferredProviders) {
			if (provider.isGlobal() && !preferredClouds.contains(provider.getProviderCloud())) {
				preferredClouds.add(provider.getProviderCloud());
			}
		}
		
		return preferredClouds;
	}
	
	//-------------------------------------------------------------------------------------------------
	private List<SystemRequestDTO> getPreferredSystems(final List<PreferredProviderDataDTO> preferredProviders, final CloudResponseDTO targetCloud) {
		logger.debug("getPreferredSystems started...");
		
		if (preferredProviders == null || preferredProviders.isEmpty()) {
			return List.of();
		}
		
		final List<SystemRequestDTO> preferredSystemsFromTargetCloud = new ArrayList<>(preferredProviders.size());
		for (final PreferredProviderDataDTO preferredProviderDataDTO : preferredProviders) {
			if (DTOUtilities.equalsCloudInResponseAndRequest(targetCloud, preferredProviderDataDTO.getProviderCloud())) {
				preferredSystemsFromTargetCloud.add(preferredProviderDataDTO.getProviderSystem());
			}
		}
		
		return preferredSystemsFromTargetCloud;
	}
	
	//-------------------------------------------------------------------------------------------------
	private void updateOrchestrationResultWarningWithForeignWarning(final List<OrchestrationResultDTO> response) {
		logger.debug("updateOrchestrationResultWarningWithForeignWarning started...");
		
		for (final OrchestrationResultDTO orchestrationResultDTO : response) {
			if (orchestrationResultDTO.getWarnings() == null) {
				orchestrationResultDTO.setWarnings(new ArrayList<>(1));
			}
			
			if (!orchestrationResultDTO.getWarnings().contains(OrchestratorWarnings.FROM_OTHER_CLOUD)) {
				orchestrationResultDTO.getWarnings().add(OrchestratorWarnings.FROM_OTHER_CLOUD);
			}
		}
	}

	//-------------------------------------------------------------------------------------------------
	private OrchestrationResponseDTO getHighestPriorityCurrentlyWorkingStoreEntryFromEntryList(final OrchestrationFormRequestDTO request, final List<OrchestratorStore> entryList,
																							   final List<ServiceRegistryResponseDTO> authorizedLocalServiceRegistryEntries) {
		logger.debug("getHighestPriorityCurrentlyWorkingStoreEntryFromEntryList started...");
		
        for (final OrchestratorStore orchestratorStore : entryList) {
        	if (!orchestratorStore.isForeign()) {
				final OrchestrationResponseDTO orchestrationResponseDTO = crossCheckLocalStoreEntry(orchestratorStore, request, authorizedLocalServiceRegistryEntries);
				if (orchestrationResponseDTO != null && !orchestrationResponseDTO.getResponse().isEmpty()) {
            		return orchestrationResponseDTO;
				}
        	} else {       		
				final OrchestrationResponseDTO orchestrationResponseDTO = crossCheckForeignStoreEntry(orchestratorStore, request);
				if (orchestrationResponseDTO != null && !orchestrationResponseDTO.getResponse().isEmpty()) {
            		return orchestrationResponseDTO;
				}
			}				
		}
        
        return new OrchestrationResponseDTO(); // empty response
	}

	//-------------------------------------------------------------------------------------------------
	private OrchestrationResponseDTO crossCheckLocalStoreEntry(final OrchestratorStore orchestratorStore, final OrchestrationFormRequestDTO request, 
															   final List<ServiceRegistryResponseDTO> authorizedLocalServiceRegistryEntries) {
		logger.debug("crossCheckLocalStoreEntry started ...");
		
		final Long providerSystemId = orchestratorStore.getProviderSystemId();
		for (final ServiceRegistryResponseDTO serviceRegistryResponseDTO : authorizedLocalServiceRegistryEntries) {
			if (serviceRegistryResponseDTO.getProvider().getId() == providerSystemId) {
				List<OrchestrationResultDTO> orList = compileOrchestrationResponse(List.of(serviceRegistryResponseDTO));
			    // Generate the authorization tokens if it is requested based on the service security (modifies the orList)
			    orList = orchestratorDriver.generateAuthTokens(request, orList);

			    return new OrchestrationResponseDTO(orList);
			}
		}
		
		return new OrchestrationResponseDTO(); // empty response
	}

	//-------------------------------------------------------------------------------------------------
	private OrchestrationResponseDTO crossCheckForeignStoreEntry(final OrchestratorStore orchestratorStore, final OrchestrationFormRequestDTO request) {		
		logger.debug("crossCheckForeignStoreEntry started ...");
		
		final OrchestratorStoreResponseDTO foreignStoreEntry = orchestratorStoreDBService.getForeignResponseDTO(orchestratorStore);      		
		final PreferredProviderDataDTO preferredProviderDataDTO = DTOConverter.convertForeignOrchestratorStoreResponseDTOToPreferredProviderDataDTO(foreignStoreEntry);									
		final List<SystemRequestDTO> preferredSystemsFromTargetCloud = List.of(DTOConverter.convertSystemResponseDTOToSystemRequestDTO(foreignStoreEntry.getProviderSystem()));
		final ServiceQueryFormDTO serviceQueryFormDTO = request.getRequestedService();
		final SystemRequestDTO systemRequestDTO = request.getRequesterSystem();
		final CloudResponseDTO cloud = foreignStoreEntry.getProviderCloud();
		final List<PreferredProviderDataDTO> preferredProviderDataDTOList = List.of(preferredProviderDataDTO);
		
		// orchestrationFromStore 
		final OrchestrationFlags flags = request.getOrchestrationFlags();
		flags.put(Flag.MATCHMAKING, true);
		request.setOrchestrationFlags(flags);
		
		return callInterCloudNegotiation(preferredSystemsFromTargetCloud, serviceQueryFormDTO, systemRequestDTO, cloud, request.getOrchestrationFlags(), preferredProviderDataDTOList,
										 request.getQosRequirements(), request.getCommands(), new ArrayList<>());
	}

	//-------------------------------------------------------------------------------------------------
	private GSDQueryResultDTO callGSD(final OrchestrationFormRequestDTO request, final OrchestrationFlags flags) {
		logger.debug("callGSD started ...");
		
		final List<CloudRequestDTO> preferredClouds = getPreferredClouds(request.getPreferredProviders());
		
		return orchestratorDriver.doGlobalServiceDiscovery(new GSDQueryFormDTO(request.getRequestedService(), preferredClouds, flags.getOrDefault(Flag.ENABLE_QOS, false)));
	}
	
	//-------------------------------------------------------------------------------------------------
	private OrchestrationResponseDTO callInterCloudNegotiation(final OrchestrationFormRequestDTO request, final CloudResponseDTO targetCloud, final OrchestrationFlags flags,
															   final List<RelayRequestDTO> preferredRelays) {
		logger.debug("callInterCloudNegotiation started ...");

		final List<SystemRequestDTO> preferredSystemsFromTargetCloud = getPreferredSystems(request.getPreferredProviders(), targetCloud);
		final ServiceQueryFormDTO serviceQueryFormDTO = request.getRequestedService();
		final SystemRequestDTO systemRequestDTO = request.getRequesterSystem();
		final List<PreferredProviderDataDTO> preferredProviderDataDTOList = request.getPreferredProviders();
		
		return callInterCloudNegotiation(preferredSystemsFromTargetCloud, serviceQueryFormDTO, systemRequestDTO, targetCloud, flags, preferredProviderDataDTOList, request.getQosRequirements(),
										 request.getCommands(), preferredRelays);
	}
	
	//-------------------------------------------------------------------------------------------------
	private OrchestrationResponseDTO callInterCloudNegotiation(final List<SystemRequestDTO> preferredSystemsFromTargetCloud, final ServiceQueryFormDTO serviceQueryFormDTO, 
															   final SystemRequestDTO systemRequestDTO,	final CloudResponseDTO targetCloud, final OrchestrationFlags flags, 
															   final List<PreferredProviderDataDTO> preferredProviderDataDTOList, final Map<String,String> qosRequirements,
															   final Map<String,String> commands, final List<RelayRequestDTO> preferredRelays) {
		logger.debug("callInterCloudNegotiation with detailed parameters started ...");

		final ICNRequestFormDTO icnRequest = new ICNRequestFormDTO(serviceQueryFormDTO,	targetCloud.getId(), systemRequestDTO, preferredSystemsFromTargetCloud,	preferredRelays, flags, commands);
		final ICNResultDTO icnResultDTO = orchestratorDriver.doInterCloudNegotiation(icnRequest);
        if (icnResultDTO == null || icnResultDTO.getResponse().isEmpty()) {
        	// Return empty response
           return new OrchestrationResponseDTO();
		}
		
        if (flags.getOrDefault(Flag.ENABLE_QOS, false)) {
        	if (commands.containsKey(OrchestrationFormRequestDTO.QOS_COMMAND_EXCLUSIVITY)) {
        		Assert.isTrue(icnResultDTO.getResponse().size() == 1, "Reservation was requested, but there are more provider after ICN");
        		// No need for QoS verification as the reserved provider can only come from the pre-verified preferred providers.
        	} else if (icnResultDTO.getResponse().size() == 1 &&
        			   icnResultDTO.getResponse().get(0).getWarnings().contains(OrchestratorWarnings.VIA_GATEWAY)) {
        		// No need for QoS verification as the provider via gateway can only come from the pre-verified preferred providers.
        	} else {				
				final List<OrchestrationResultDTO> verifiedResults = qosManager.verifyInterCloudServices(targetCloud, icnResultDTO.getResponse(), qosRequirements, commands);
				icnResultDTO.setResponse(verifiedResults);				
			}        	
		}
        
        updateOrchestrationResultWarningWithForeignWarning(icnResultDTO.getResponse());
		
        if (flags.get(Flag.MATCHMAKING)) {
    	    final InterCloudProviderMatchmakingParameters iCPMparams = new InterCloudProviderMatchmakingParameters(icnResultDTO, preferredProviderDataDTOList, flags.get(Flag.ONLY_PREFERRED));		
	           
            return interCloudProviderMatchmaker.doMatchmaking(iCPMparams);
		}	

        return new OrchestrationResponseDTO(icnResultDTO.getResponse());
	}
	
	//-------------------------------------------------------------------------------------------------
	// Please note that the current implementation does not support intercloud orchestration, QoS requirements and provider reservation 
	private OrchestrationResponseDTO orchestrationFromFlexibleStore(final OrchestrationFormRequestDTO request) {
		logger.debug("orchestrationFromFlexibleStore started ...");
		
		validateFlexibleStoreRequest(request);
		
		final OrchestrationFlags flags = request.getOrchestrationFlags();
		final List<PreferredProviderDataDTO> localProviders = request.getPreferredProviders().stream().filter(p -> p.isLocal()).collect(Collectors.toList());
		
		List<PreferredProviderDataDTO> onlyPreferredProviders = null;
		if (flags.getOrDefault(Flag.ONLY_PREFERRED, false)) {
			onlyPreferredProviders = localProviders; 
			if (onlyPreferredProviders.isEmpty()) { // never happened while intercloud is unsupported
				throw new InvalidParameterException("There is no valid (local) preferred provider, but \"" + Flag.ONLY_PREFERRED + "\" is set to true");
			}
		}
		
		// query system from Service Registry 
		final SystemResponseDTO consumerSystem = orchestratorFlexibleDriver.queryConsumerSystem(request.getRequesterSystem());

		// collect matching rules
		final List<OrchestratorStoreFlexible> rules = orchestratorFlexibleDriver.collectAndSortMatchingRules(request, consumerSystem);
		if (rules.isEmpty()) {
			return new OrchestrationResponseDTO();
		}
		
		// querying Service Registry
		final List<Pair<OrchestratorStoreFlexible,ServiceQueryResultDTO>> queryDataWithRules = orchestratorFlexibleDriver.queryServiceRegistry(request, rules);
		
		// filter Service Registry results by provider requirements (coming from the rules)
		final List<ServiceRegistryResponseDTO> queryData = orchestratorFlexibleDriver.filterSRResultsByProviderRequirements(queryDataWithRules, onlyPreferredProviders);
		if (queryData.isEmpty()) {
			return new OrchestrationResponseDTO();
		}
		
		// WE SKIP the authorization (for now, it is the responsibility of the PDE to make sure consumers have right to use the providers that the rules offer them)
		
		// convert Service Registry results to orchestration responses
		List<OrchestrationResultDTO> orList = compileOrchestrationResponse(queryData);

		// If matchmaking is requested, we pick out 1 ServiceRegistryEntry entity from the list.
		if (flags.get(Flag.MATCHMAKING)) {
			final IntraCloudProviderMatchmakingParameters params = new IntraCloudProviderMatchmakingParameters(localProviders);
			// set additional parameters here if you use a different matchmaking algorithm
			final OrchestrationResultDTO selected = intraCloudProviderMatchmaker.doMatchmaking(orList, params);
			orList.clear();
			orList.add(selected);
		}
		
		// all the filtering is done
		logger.debug("flexible store ochestration finished with {} service providers.", queryData.size());
		
		// Generate the authorization tokens if it is requested based on the service security (modifies the orList)
		orList = orchestratorDriver.generateAuthTokens(request, orList);

	    return new OrchestrationResponseDTO(orList);
	}

	//-------------------------------------------------------------------------------------------------
	private void validateFlexibleStoreRequest(final OrchestrationFormRequestDTO request) {
		logger.debug("orchestrationFormRequestDTO started ...");
		
		if (request == null) {
			throw new InvalidParameterException("Request" + NULL_PARAMETER_ERROR_MESSAGE);
		}
		
		// in this version of Flexible Store Orchestration some flags are not supported
		checkUnsupportedFlags(request.getOrchestrationFlags());
		
		checkSystemRequestDTO(request.getRequesterSystem(), false); // consumer
		checkServiceRequestForm(request, false);
	}

	//-------------------------------------------------------------------------------------------------
	private void checkUnsupportedFlags(final OrchestrationFlags flags) {
		logger.debug("checkUnsupportedFlags started ...");
		Assert.notNull(flags, "Flags map" + NULL_PARAMETER_ERROR_MESSAGE);
		
		if (flags.getOrDefault(Flag.ENABLE_INTER_CLOUD, false) || flags.getOrDefault(Flag.TRIGGER_INTER_CLOUD, false)) {
			throw new InvalidParameterException("Intercloud mode is not supported yet.");
		}
		
		if (flags.getOrDefault(Flag.ENABLE_QOS, false)) {
			throw new InvalidParameterException("Quality of Service requirements is not supported yet.");
		}
	}
}