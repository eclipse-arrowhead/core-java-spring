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

package eu.arrowhead.core.gatekeeper.service;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.core.CoreSystem;
import eu.arrowhead.common.database.entity.Cloud;
import eu.arrowhead.common.database.entity.CloudGatewayRelay;
import eu.arrowhead.common.database.entity.Relay;
import eu.arrowhead.common.database.service.CommonDBService;
import eu.arrowhead.common.dto.internal.AccessTypeRelayResponseDTO;
import eu.arrowhead.common.dto.internal.CloudAccessListResponseDTO;
import eu.arrowhead.common.dto.internal.CloudAccessResponseDTO;
import eu.arrowhead.common.dto.internal.DTOConverter;
import eu.arrowhead.common.dto.internal.GSDMultiPollRequestDTO;
import eu.arrowhead.common.dto.internal.GSDMultiPollResponseDTO;
import eu.arrowhead.common.dto.internal.GSDMultiQueryFormDTO;
import eu.arrowhead.common.dto.internal.GSDMultiQueryResultDTO;
import eu.arrowhead.common.dto.internal.GSDPollRequestDTO;
import eu.arrowhead.common.dto.internal.GSDPollResponseDTO;
import eu.arrowhead.common.dto.internal.GSDQueryFormDTO;
import eu.arrowhead.common.dto.internal.GSDQueryResultDTO;
import eu.arrowhead.common.dto.internal.GatewayConsumerConnectionRequestDTO;
import eu.arrowhead.common.dto.internal.GatewayProviderConnectionRequestDTO;
import eu.arrowhead.common.dto.internal.GatewayProviderConnectionResponseDTO;
import eu.arrowhead.common.dto.internal.ICNProposalRequestDTO;
import eu.arrowhead.common.dto.internal.ICNProposalResponseDTO;
import eu.arrowhead.common.dto.internal.ICNRequestFormDTO;
import eu.arrowhead.common.dto.internal.ICNResultDTO;
import eu.arrowhead.common.dto.internal.QoSIntraPingMeasurementResponseDTO;
import eu.arrowhead.common.dto.internal.QoSMonitorSenderConnectionRequestDTO;
import eu.arrowhead.common.dto.internal.QoSRelayTestProposalRequestDTO;
import eu.arrowhead.common.dto.internal.QoSRelayTestProposalResponseDTO;
import eu.arrowhead.common.dto.internal.QoSReservationRequestDTO;
import eu.arrowhead.common.dto.internal.QoSReservationResponseDTO;
import eu.arrowhead.common.dto.internal.QoSTemporaryLockRequestDTO;
import eu.arrowhead.common.dto.internal.QoSTemporaryLockResponseDTO;
import eu.arrowhead.common.dto.internal.RelayRequestDTO;
import eu.arrowhead.common.dto.internal.RelayResponseDTO;
import eu.arrowhead.common.dto.internal.RelayType;
import eu.arrowhead.common.dto.internal.ServiceRegistryListResponseDTO;
import eu.arrowhead.common.dto.internal.SystemAddressSetRelayResponseDTO;
import eu.arrowhead.common.dto.shared.CloudRequestDTO;
import eu.arrowhead.common.dto.shared.ErrorMessageDTO;
import eu.arrowhead.common.dto.shared.ErrorWrapperDTO;
import eu.arrowhead.common.dto.shared.OrchestrationFlags.Flag;
import eu.arrowhead.common.dto.shared.OrchestrationFormRequestDTO;
import eu.arrowhead.common.dto.shared.OrchestrationResponseDTO;
import eu.arrowhead.common.dto.shared.OrchestrationResultDTO;
import eu.arrowhead.common.dto.shared.OrchestratorWarnings;
import eu.arrowhead.common.dto.shared.PreferredProviderDataDTO;
import eu.arrowhead.common.dto.shared.QoSMeasurementAttributesFormDTO;
import eu.arrowhead.common.dto.shared.ServiceInterfaceResponseDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryFormDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryFormListDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryResultDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryResultListDTO;
import eu.arrowhead.common.dto.shared.ServiceRegistryResponseDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.AuthException;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.common.exception.UnavailableServerException;
import eu.arrowhead.core.gatekeeper.database.service.GatekeeperDBService;
import eu.arrowhead.core.gatekeeper.service.matchmaking.ICNProviderMatchmakingAlgorithm;
import eu.arrowhead.core.gatekeeper.service.matchmaking.ICNProviderMatchmakingParameters;
import eu.arrowhead.core.gatekeeper.service.matchmaking.RelayMatchmakingAlgorithm;
import eu.arrowhead.core.gatekeeper.service.matchmaking.RelayMatchmakingParameters;

@Service
public class GatekeeperService {
	
	//=================================================================================================
	// members
	
	private final Logger logger = LogManager.getLogger(GatekeeperService.class);
	
	@Value(CoreCommonConstants.$GATEKEEPER_IS_GATEWAY_PRESENT_WD)
	private boolean gatewayIsPresent;
		
	@Value(CoreCommonConstants.$GATEKEEPER_IS_GATEWAY_MANDATORY_WD)
	private boolean gatewayIsMandatory;
	
	@Autowired
	private CommonDBService commonDBService;
	
	@Autowired
	private GatekeeperDBService gatekeeperDBService;
	
	@Autowired
	private GatekeeperDriver gatekeeperDriver;
	
	@Resource(name = CoreCommonConstants.ICN_PROVIDER_MATCHMAKER)
	private ICNProviderMatchmakingAlgorithm icnProviderMatchmaker;
	
	@Resource(name = CoreCommonConstants.GATEWAY_MATCHMAKER)
	private RelayMatchmakingAlgorithm gatewayMatchmaker;
	
	private final long qosReservationPufferSeconds = 5;
	
	private final String CLOUD_HAS_NO_RELAY_WARNING_MESSAGE = "The following cloud does not have a relay: ";

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public GSDQueryResultDTO initGSDPoll(final GSDQueryFormDTO gsdForm) throws InterruptedException {
		logger.debug("initGSDPoll started...");
		
		Assert.notNull(gsdForm, "GSDQueryFormDTO is null.");
		Assert.notNull(gsdForm.getRequestedService(), "requestedService is null.");
		Assert.isTrue(!Utilities.isEmpty(gsdForm.getRequestedService().getServiceDefinitionRequirement()), "serviceDefinitionRequirement is empty");

		List<Cloud> cloudsToContact;
		if (gsdForm.getPreferredClouds() == null || gsdForm.getPreferredClouds().isEmpty()) {
			// If no preferred clouds were given, then send GSD poll requests to the neighbor Clouds
			final List<Cloud> neighborClouds = gatekeeperDBService.getNeighborClouds();
			if (neighborClouds.isEmpty()) {
				throw new InvalidParameterException("initGSDPoll failed: Neither preferred clouds were given, nor neighbor clouds registered");
			} else {
				cloudsToContact = neighborClouds;
			}			
		} else {
			// If preferred clouds were given, then send GSD poll requests only to those Clouds
			final List<Cloud> preferredClouds = getCloudsByCloudRequestDTOs(gsdForm.getPreferredClouds());
			
			if (preferredClouds.isEmpty()) {
				throw new InvalidParameterException("initGSDPoll failed: Given preferred clouds are not exists");
			} else {
				cloudsToContact = preferredClouds;
			}
		}
		
		final GSDPollRequestDTO gsdPollRequestDTO = new GSDPollRequestDTO(gsdForm.getRequestedService(), getOwnCloud(), gatewayIsPresent, gsdForm.getNeedQoSMeasurements());
		final List<ErrorWrapperDTO> gsdPollAnswers = gatekeeperDriver.sendGSDPollRequest(cloudsToContact, gsdPollRequestDTO);
		
		final List<GSDPollResponseDTO> successfulResponses = new ArrayList<>();
		final List<ErrorMessageDTO> errorMessageResponses = new ArrayList<>();
		int unsuccessfulRequests = 0;
		for (final ErrorWrapperDTO gsdAnswer : gsdPollAnswers) {
			if (gsdAnswer.isError()) {		
				errorMessageResponses.add((ErrorMessageDTO) gsdAnswer);
				unsuccessfulRequests++;
			} else {
				final GSDPollResponseDTO gsdResponse = (GSDPollResponseDTO) gsdAnswer;
				if (gsdResponse.getProviderCloud() == null) {
					unsuccessfulRequests++;
				} else {
					// Changing the cloud details to the local informations based on operator and name
					final Cloud providerCloudWithLocalDetails = gatekeeperDBService.getCloudByOperatorAndName(gsdResponse.getProviderCloud().getOperator(), gsdResponse.getProviderCloud().getName());
					gsdResponse.setProviderCloud(DTOConverter.convertCloudToCloudResponseDTO(providerCloudWithLocalDetails));
					
					successfulResponses.add(gsdResponse);		
				}
			}						
		}
		
		if (successfulResponses.isEmpty() && !errorMessageResponses.isEmpty()) {
			Utilities.createExceptionFromErrorMessageDTO(errorMessageResponses.get(0));
		}
		
		return new GSDQueryResultDTO(successfulResponses, unsuccessfulRequests);
	}
	
	//-------------------------------------------------------------------------------------------------
	public GSDMultiQueryResultDTO initMultiGSDPoll(final GSDMultiQueryFormDTO gsdForm) throws InterruptedException {
		logger.debug("initMultiGSDPoll started...");
		
		Assert.notNull(gsdForm, "GSDMultiQueryFormDTO is null.");
		Assert.isTrue(!Utilities.isEmpty(gsdForm.getRequestedServices()), "requestedServices list is null or empty.");
		for (final ServiceQueryFormDTO serviceReq : gsdForm.getRequestedServices()) {
			Assert.isTrue(!Utilities.isEmpty(serviceReq.getServiceDefinitionRequirement()), "serviceDefinitionRequirement is null or empty.");
		}
		
		List<Cloud> cloudsToContact;
		if (gsdForm.getPreferredClouds() == null || gsdForm.getPreferredClouds().isEmpty()) {
			// If no preferred clouds were given, then send GSD poll requests to the neighbor Clouds
			final List<Cloud> neighborClouds = gatekeeperDBService.getNeighborClouds();
			if (neighborClouds.isEmpty()) {
				throw new InvalidParameterException("initMultiGSDPoll failed: Neither preferred clouds were given, nor neighbor clouds registered.");
			} else {
				cloudsToContact = neighborClouds;
			}			
		} else {
			// If preferred clouds were given, then send GSD poll requests only to those Clouds
			final List<Cloud> preferredClouds = getCloudsByCloudRequestDTOs(gsdForm.getPreferredClouds());
			
			if (preferredClouds.isEmpty()) {
				throw new InvalidParameterException("initMultiGSDPoll failed: Given preferred clouds are not exists.");
			} else {
				cloudsToContact = preferredClouds;
			}
		}
		
		final GSDMultiPollRequestDTO gsdPollRequestDTO = new GSDMultiPollRequestDTO(gsdForm.getRequestedServices(), getOwnCloud(), gatewayIsPresent);
		final List<ErrorWrapperDTO> gsdPollAnswers = gatekeeperDriver.sendMultiGSDPollRequest(cloudsToContact, gsdPollRequestDTO);
		
		final List<GSDMultiPollResponseDTO> successfulResponses = new ArrayList<>();
		int unsuccessfulRequests = 0;
		for (final ErrorWrapperDTO gsdAnswer : gsdPollAnswers) {
			if (gsdAnswer.isError()) {		
				unsuccessfulRequests++;
			} else {
				final GSDMultiPollResponseDTO gsdResponse = (GSDMultiPollResponseDTO) gsdAnswer;
				if (gsdResponse.getProviderCloud() == null) {
					unsuccessfulRequests++;
				} else {
					// Changing the cloud details to the local informations based on operator and name
					final Cloud providerCloudWithLocalDetails = gatekeeperDBService.getCloudByOperatorAndName(gsdResponse.getProviderCloud().getOperator(), gsdResponse.getProviderCloud().getName());
					gsdResponse.setProviderCloud(DTOConverter.convertCloudToCloudResponseDTO(providerCloudWithLocalDetails));
					
					successfulResponses.add(gsdResponse);		
				}
			}						
		}
		
		return new GSDMultiQueryResultDTO(successfulResponses, unsuccessfulRequests);
	}
	
	//-------------------------------------------------------------------------------------------------
	public GSDPollResponseDTO doGSDPoll(final GSDPollRequestDTO request) {
		logger.debug("doGSDPoll started...");
		
		validateGSDPollRequestDTO(request);
		
		// Check whether need for QoS can be fulfilled or not
		if (request.getNeedQoSMeasurements() && !gatekeeperDriver.checkQoSEnabled()) {
			return new GSDPollResponseDTO();
		}
				
		// Querying Service Registry core system
		final ServiceQueryResultDTO srQueryResult = gatekeeperDriver.sendServiceRegistryQuery(request.getRequestedService());
		
		if (srQueryResult.getServiceQueryData() == null || srQueryResult.getServiceQueryData().isEmpty()) {
			return new GSDPollResponseDTO();
		}
		
		// Querying Authorization core system
		final Map<Long,List<Long>> authorizedProviderIdsWithInterfaceIdList = gatekeeperDriver.sendInterCloudAuthorizationCheckQuery(srQueryResult.getServiceQueryData(), request.getRequesterCloud(), 
																																 	 request.getRequestedService().getServiceDefinitionRequirement());
		if (authorizedProviderIdsWithInterfaceIdList.isEmpty()) {
			return new GSDPollResponseDTO();
		}
		
		// Cross checking Service Registry and Authorization results and add QoS Measurements
		final Set<String> availableInterfaces = new HashSet<>();
		int numOfProviders = 0;
		final List<QoSMeasurementAttributesFormDTO> qosMeasurements = new ArrayList<>();
		final Set<String> reservedProviderIdAndServiceIdPairs = getReservedProviderIdAndServiceIdPairs();
		
		for (final ServiceRegistryResponseDTO srEntryDTO : srQueryResult.getServiceQueryData()) {
			final long providerId = srEntryDTO.getProvider().getId();
			final long serviceId = srEntryDTO.getServiceDefinition().getId();
			if (authorizedProviderIdsWithInterfaceIdList.containsKey(providerId) 
					&& !reservedProviderIdAndServiceIdPairs.contains(providerId + "-" + serviceId)) {
				
				final Set<String> providerInterfaces = new HashSet<>();
				for (final ServiceInterfaceResponseDTO interfaceDTO : srEntryDTO.getInterfaces()) {
					if (authorizedProviderIdsWithInterfaceIdList.get(providerId).contains(interfaceDTO.getId())) {
						providerInterfaces.add(interfaceDTO.getInterfaceName());
					}
				}
				
				if (!request.getNeedQoSMeasurements()) {
					availableInterfaces.addAll(providerInterfaces);
					numOfProviders++;
				} else {
					try {
						final QoSIntraPingMeasurementResponseDTO pingMeasurement = gatekeeperDriver.getQoSIntraPingMeasurementsForLocalSystem(providerId);
						if (pingMeasurement.getId() == null) {
							logger.debug("No measurement available for Provider during doGSDPoll. Provider skipped.");
						} else {
							qosMeasurements.add(new QoSMeasurementAttributesFormDTO(srEntryDTO,
																					pingMeasurement.isAvailable(),
																					pingMeasurement.getLastAccessAt(),
																					pingMeasurement.getMinResponseTime(),
																					pingMeasurement.getMaxResponseTime(),
																					pingMeasurement.getMeanResponseTimeWithTimeout(),
																					pingMeasurement.getMeanResponseTimeWithoutTimeout(),
																					pingMeasurement.getJitterWithTimeout(),
																					pingMeasurement.getJitterWithoutTimeout(),
																					pingMeasurement.getSent(),
																					pingMeasurement.getReceived(),
																					pingMeasurement.getSentAll(),
																					pingMeasurement.getReceivedAll(),
																					pingMeasurement.getLostPerMeasurementPercent()));
							availableInterfaces.addAll(providerInterfaces);
							numOfProviders++;							
						}
					} catch (final ArrowheadException ex) {
						logger.debug("Exception occured during doGSDPoll - QoS details request. Provider skipped.");
						logger.debug(ex.getMessage());
					}					
				}
				
			}		
		}
		
		final Cloud ownCloud = commonDBService.getOwnCloud(true); // gatekeeper works only secure mode
		
		return new GSDPollResponseDTO(DTOConverter.convertCloudToCloudResponseDTO(ownCloud), request.getRequestedService().getServiceDefinitionRequirement(), List.copyOf(availableInterfaces), 
									  numOfProviders, qosMeasurements, request.getRequestedService().getMetadataRequirements(), gatewayIsMandatory);
	}
	
	//-------------------------------------------------------------------------------------------------
	public GSDMultiPollResponseDTO doMultiGSDPoll(final GSDMultiPollRequestDTO request) { 
		logger.debug("doMultiGSDPoll started...");
		
		validateMultiGSDPollRequestDTO(request);
		
		final List<String> providedServiceDefinitions = new ArrayList<>();
		
		// Querying Service Registry core system
		final ServiceQueryResultListDTO srQueryResult = gatekeeperDriver.sendServiceRegistryMultiQuery(new ServiceQueryFormListDTO(request.getRequestedServices()));
		
		//TODO: maybe make specific auth endpoint for this
		for (int i = 0; i < srQueryResult.getResults().size(); ++i) {
			final ServiceQueryResultDTO resultDTO = srQueryResult.getResults().get(i);
			
			if (resultDTO.getServiceQueryData().isEmpty()) {
				// no providers => skip
				continue;
			}
			
			final ServiceQueryFormDTO queryFormDTO = request.getRequestedServices().get(i);
			
			// Querying Authorization core system
			final Map<Long,List<Long>> authorizedProviderIdsWithInterfaceIdList = gatekeeperDriver.sendInterCloudAuthorizationCheckQuery(resultDTO.getServiceQueryData(), request.getRequesterCloud(), queryFormDTO.getServiceDefinitionRequirement());

			if (!authorizedProviderIdsWithInterfaceIdList.isEmpty()) {
				providedServiceDefinitions.add(queryFormDTO.getServiceDefinitionRequirement());
			}
		}
		
		final Cloud ownCloud = commonDBService.getOwnCloud(true); // gatekeeper works only secure mode
		
		return new GSDMultiPollResponseDTO(DTOConverter.convertCloudToCloudResponseDTO(ownCloud), providedServiceDefinitions);
	}
	
	//-------------------------------------------------------------------------------------------------
	public ICNResultDTO initICN(final ICNRequestFormDTO form) { 
		logger.debug("initICN started...");
		
		validateICNForm(form);
		
		final Cloud targetCloud = gatekeeperDBService.getCloudById(form.getTargetCloudId());
		final CloudRequestDTO requesterCloud = getOwnCloud();
		List<RelayRequestDTO> preferredRelays = getPreferredRelays(targetCloud);
		if (form.getNegotiationFlags().get(Flag.ENABLE_QOS)) {
			preferredRelays.retainAll(form.getPreferredGatewayRelays());
			if (preferredRelays.isEmpty()) {
				preferredRelays = form.getPreferredGatewayRelays();
			}
		}
		final List<RelayRequestDTO> knownRelays = getKnownRelays();
		final ICNProposalRequestDTO proposal = new ICNProposalRequestDTO(form.getRequestedService(), requesterCloud, form.getRequesterSystem(), form.getPreferredSystems(), preferredRelays,
																		 knownRelays, form.getNegotiationFlags(), gatewayIsPresent, form.getCommands());
		String consumerGWPublicKey = null;
		if (gatewayIsPresent) {
			consumerGWPublicKey = gatekeeperDriver.queryGatewayPublicKey();
			proposal.setConsumerGatewayPublicKey(consumerGWPublicKey);
		}
		
		final ICNProposalResponseDTO icnResponse = gatekeeperDriver.sendICNProposal(targetCloud, proposal);
		
		if (icnResponse.getResponse().isEmpty()) {
			return new ICNResultDTO();
		}
 		
		if (!icnResponse.isUseGateway()) {
			// just send back the response
			return new ICNResultDTO(icnResponse.getResponse());
		}
		
		// initializing gateway connection
		final OrchestrationResultDTO result = icnResponse.getResponse().get(0); // in gateway mode there is only one result in the response object
		final Relay commonRelay = findCorrespondingRelay(icnResponse.getRelay());
		final GatewayConsumerConnectionRequestDTO connectionRequest = new GatewayConsumerConnectionRequestDTO(DTOConverter.convertRelayToRelayRequestDTO(commonRelay),
																											  icnResponse.getConnectionInfo().getQueueId(), 
																											  icnResponse.getConnectionInfo().getPeerName(),
																											  icnResponse.getConnectionInfo().getProviderGWPublicKey(), form.getRequesterSystem(),
																											  DTOConverter.convertSystemResponseDTOToSystemRequestDTO(result.getProvider()) ,
																											  requesterCloud, getCloudRequestDTO(targetCloud),
																											  form.getRequestedService().getServiceDefinitionRequirement());
		try {
			final int serverPort = gatekeeperDriver.connectConsumer(connectionRequest);
			
			// change provider in result to make sure consumer will connect via the gateway
			result.getProvider().setSystemName(CoreSystem.GATEWAY.name().toLowerCase());
			result.getProvider().setAddress(gatekeeperDriver.getGatewayHost());
			result.getProvider().setPort(serverPort);
			result.getProvider().setAuthenticationInfo(consumerGWPublicKey);
			result.getWarnings().add(OrchestratorWarnings.VIA_GATEWAY);
			
			return new ICNResultDTO(List.of(result));
		} catch (final UnavailableServerException ex) {
			logger.error("Error while connect to consumer via gateway: {}", ex.getMessage());
			logger.debug("Stacktrace:", ex);
			
			return new ICNResultDTO();
		}
	}

	//-------------------------------------------------------------------------------------------------
	public ICNProposalResponseDTO doICN(final ICNProposalRequestDTO request) {
		logger.debug("doICN started...");
		
		validateICNProposalRequest(request);
		
		// Check whether need for QoS can be fulfilled or not
		if (request.getNegotiationFlags().getOrDefault(Flag.ENABLE_QOS, false) && !gatekeeperDriver.checkQoSEnabled()) {
			return new ICNProposalResponseDTO();
		}
		
		final PreferredProviderDataDTO[] preferredProviders = getPreferredProviders(request.getPreferredSystems());
		boolean needMatchmaking = request.getCommands().containsKey(OrchestrationFormRequestDTO.QOS_COMMAND_EXCLUSIVITY) ?
								  true : request.getNegotiationFlags().getOrDefault(Flag.MATCHMAKING, false);
		final OrchestrationFormRequestDTO orchestrationForm = new OrchestrationFormRequestDTO.Builder(request.getRequesterSystem())
																							 .requesterCloud(request.getRequesterCloud())
																							 .requestedService(request.getRequestedService())
																							 .flags(request.getNegotiationFlags())
																							 .flag(Flag.EXTERNAL_SERVICE_REQUEST, true)
																							 .flag(Flag.MATCHMAKING, needMatchmaking)
																							 .commands(request.getCommands())
																							 .preferredProviders(preferredProviders)
																							 .build();
		if (gatewayIsMandatory) {
			// changing the requesterSystem and requesterCloud for the sake of proper token generation
			orchestrationForm.getRequesterSystem().setSystemName(CoreSystem.GATEWAY.name().toLowerCase());
			orchestrationForm.setRequesterCloud(null); // means own cloud
		}
		
		OrchestrationResponseDTO orchestrationResponse = gatekeeperDriver.queryOrchestrator(orchestrationForm);
		if (orchestrationResponse.getResponse().isEmpty()) { // no results
			return new ICNProposalResponseDTO();
		}
		
		final boolean needReservation = request.getNegotiationFlags().getOrDefault(Flag.ENABLE_QOS, false) && request.getCommands().containsKey(OrchestrationFormRequestDTO.QOS_COMMAND_EXCLUSIVITY);
		
		if (needReservation) {
			// filter out reserved providers and lock remained results temporary 
			final QoSTemporaryLockResponseDTO lockedResults = gatekeeperDriver.sendQoSTemporaryLockRequest(new QoSTemporaryLockRequestDTO(request.getRequesterSystem(),
																																		  orchestrationResponse.getResponse()));
			orchestrationResponse.setResponse(lockedResults.getResponse());
			if (orchestrationResponse.getResponse().isEmpty()) { // no usable results
				return new ICNProposalResponseDTO();
			}
		}
		
		orchestrationResponse = gatekeeperDriver.queryAuthorizationBasedOnOrchestrationResponse(request.getRequesterCloud(), orchestrationResponse);
		if (orchestrationResponse.getResponse().isEmpty()) { // no accessible results
			return new ICNProposalResponseDTO();
		}		

		if (request.getNegotiationFlags().getOrDefault(Flag.ENABLE_QOS, false) && !needReservation) {
			orchestrationResponse = fillOrchestrationResultsWithLocalQoSMeasurements(orchestrationResponse);
			if (orchestrationResponse.getResponse().isEmpty()) { // no usable results
				return new ICNProposalResponseDTO();
			}
		}
		
		if (!gatewayIsMandatory) {
			if (!needReservation) {
				// filter out reserved providers
				orchestrationResponse = filterOutReservedProviders(orchestrationResponse);
				if (orchestrationResponse.getResponse().isEmpty()) { // no usable results
					return new ICNProposalResponseDTO();
				}
				return new ICNProposalResponseDTO(orchestrationResponse.getResponse());
			} else {
				// If QoS enabled, then preferred systems defined in the request already contains only pre-verified systems by QoS Manager of requester cloud,
				// therefore we have to pick and reserve one from there.
				final OrchestrationResultDTO selectedResult = icnProviderMatchmaker.doMatchmaking(orchestrationResponse.getResponse(),
																								  new ICNProviderMatchmakingParameters(request.getPreferredSystems(), true));
				if (selectedResult == null) {
					return new ICNProposalResponseDTO();
				}
				
				gatekeeperDriver.sendQoSConfirmReservationRequest(new QoSReservationRequestDTO(selectedResult, orchestrationForm.getRequesterSystem(), orchestrationResponse.getResponse()));
				return new ICNProposalResponseDTO(List.of(selectedResult));				
			}
		} 

		// gateway is used so we need a relay
		
		if (request.getNegotiationFlags().getOrDefault(Flag.ENABLE_QOS, false)) {
			// If QoS enabled, then preferred relays defined in the request already contains only pre-verified relays by QoS Manager of requester cloud.
			// As the requester cloud couldn't have relay measurements aside from the ones already in the preferred gateway relay we can select a relay only from there.
			request.setKnownGatewayRelays(request.getPreferredGatewayRelays());
		}
		final Relay selectedRelay = selectRelay(request);
		if (selectedRelay == null) {
			throw new AuthException("No common communication relay was found.");
		}
		
		// In gateway mode we have to select one provider even if matchmaking is not enabled because we have to build an expensive connection between the consumer and the provider.
		final OrchestrationResultDTO selectedResult;
		
		if (!needReservation) {
			// filter out reserved providers
			orchestrationResponse = filterOutReservedProviders(orchestrationResponse);
		}
		
		if (orchestrationResponse.getResponse().isEmpty()) { // no usable results
			return new ICNProposalResponseDTO();
		}
		if (!request.getNegotiationFlags().getOrDefault(Flag.ENABLE_QOS, false)) {
			selectedResult = icnProviderMatchmaker.doMatchmaking(orchestrationResponse.getResponse(), new ICNProviderMatchmakingParameters(request.getPreferredSystems(), false));			
		} else {
			// If QoS enabled, then preferred systems defined in the request already contains only pre-verified systems by QoS Manager of requester cloud. If no preferred system remain after the filters, then
			// therefore we have to pick one from there.
			selectedResult = icnProviderMatchmaker.doMatchmaking(orchestrationResponse.getResponse(), new ICNProviderMatchmakingParameters(request.getPreferredSystems(), true));
		}
		
		if (selectedResult == null) {
			return new ICNProposalResponseDTO();
		}
		
		final SystemRequestDTO providerSystem = DTOConverter.convertSystemResponseDTOToSystemRequestDTO(selectedResult.getProvider());
		final GatewayProviderConnectionRequestDTO connectionRequest	= new GatewayProviderConnectionRequestDTO(DTOConverter.convertRelayToRelayRequestDTO(selectedRelay), request.getRequesterSystem(),
																											  providerSystem, request.getRequesterCloud(), getOwnCloud(),
																											  request.getRequestedService().getServiceDefinitionRequirement(), 
																											  request.getConsumerGatewayPublicKey());
		final GatewayProviderConnectionResponseDTO response = gatekeeperDriver.connectProvider(connectionRequest);
		
		if (needReservation) {
			gatekeeperDriver.sendQoSConfirmReservationRequest(new QoSReservationRequestDTO(selectedResult, orchestrationForm.getRequesterSystem(), orchestrationResponse.getResponse()));
		}
		
		return new ICNProposalResponseDTO(selectedResult, DTOConverter.convertRelayToRelayResponseDTO(selectedRelay), response);
	}
	
	//-------------------------------------------------------------------------------------------------
	public CloudAccessListResponseDTO initAccessTypesCollection(final List<CloudRequestDTO> request) throws InterruptedException {
		final List<Cloud> cloudList = new ArrayList<>();
		for (final CloudRequestDTO cloudRequestDTO : request) {
			validateCloudRequest(cloudRequestDTO);
			final Cloud cloud = gatekeeperDBService.getCloudByOperatorAndName(cloudRequestDTO.getOperator(), cloudRequestDTO.getName());
			if (!cloud.getOwnCloud() && cloud.getNeighbor()) {
				if(cloud.getGatekeeperRelays().isEmpty()) {
					logger.info(CLOUD_HAS_NO_RELAY_WARNING_MESSAGE + cloudRequestDTO.getName() + "." + cloudRequestDTO.getOperator());
				}else {
					cloudList.add(cloud);
				}
			}
		}
		
		final List<ErrorWrapperDTO> atcAnswers = gatekeeperDriver.sendAccessTypesCollectionRequest(cloudList);
		final List<CloudAccessResponseDTO> successfulResponses = new ArrayList<>();
		final List<ErrorMessageDTO> errorMessageResponses = new ArrayList<>();
		for (final ErrorWrapperDTO answer : atcAnswers) {
			if (answer.isError()) {
				errorMessageResponses.add((ErrorMessageDTO) answer);
			} else {
				successfulResponses.add((CloudAccessResponseDTO) answer);
			}
		}
		
		if (successfulResponses.isEmpty() && !errorMessageResponses.isEmpty()) {
			Utilities.createExceptionFromErrorMessageDTO(errorMessageResponses.get(0));
		}
		
		final CloudAccessListResponseDTO successfulListResponse = new CloudAccessListResponseDTO();
		successfulListResponse.setData(successfulResponses);
		successfulListResponse.setCount(successfulResponses.size());
		
		return successfulListResponse;
	}
	
	//-------------------------------------------------------------------------------------------------
	public AccessTypeRelayResponseDTO returnAccessType() {
		logger.debug("returnAccessType started...");
		
		return new AccessTypeRelayResponseDTO(!gatewayIsMandatory);
	}
	
	//-------------------------------------------------------------------------------------------------
	public SystemAddressSetRelayResponseDTO initSystemAddressCollection(final CloudRequestDTO request) {
		logger.debug("initSystemAddressCollection started...");
		validateCloudRequest(request);
		
		final Cloud cloud = gatekeeperDBService.getCloudByOperatorAndName(request.getOperator(), request.getName());
		return gatekeeperDriver.sendSystemAddressCollectionRequest(cloud);
	}

	//-------------------------------------------------------------------------------------------------
	public SystemAddressSetRelayResponseDTO doSystemAddressCollection() {
		logger.debug("doSystemAddressCollection started...");
		final Set<String> addresses = new HashSet<>();
		
		final ServiceRegistryListResponseDTO results = gatekeeperDriver.sendServiceRegistryQueryAll();
		for (final ServiceRegistryResponseDTO sr : results.getData()) {
			addresses.add(sr.getProvider().getAddress());
		}
		
		return new SystemAddressSetRelayResponseDTO(addresses);
	}
	
	//-------------------------------------------------------------------------------------------------
	public void initRelayTest(final QoSRelayTestProposalRequestDTO request) {
		logger.debug("initRelayTest started...");
		
		validateQoSRelayTestProposalRequestDTO(request, false);
		
		final String qosMonitorPublicKey = gatekeeperDriver.queryQoSMonitorPublicKey();
		request.setSenderQoSMonitorPublicKey(qosMonitorPublicKey);
		request.setRequesterCloud(getOwnCloudRequestDTO());
		
		final Cloud targetCloud = gatekeeperDBService.getCloudByOperatorAndName(request.getTargetCloud().getOperator(), request.getTargetCloud().getName());
		final QoSRelayTestProposalResponseDTO response = gatekeeperDriver.sendQoSRelayTestProposal(request, targetCloud);
		
		final QoSMonitorSenderConnectionRequestDTO connectionRequest = new QoSMonitorSenderConnectionRequestDTO(request.getTargetCloud(), request.getRelay(), response.getQueueId(),
																												response.getPeerName(), response.getReceiverQoSMonitorPublicKey());
		gatekeeperDriver.initRelayTest(connectionRequest);
	}
	
	//-------------------------------------------------------------------------------------------------
	public QoSRelayTestProposalResponseDTO joinRelayTest(final QoSRelayTestProposalRequestDTO request) {
		logger.debug("joinRelayTest started...");
		
		validateQoSRelayTestProposalRequestDTO(request, true);
		
		return gatekeeperDriver.joinRelayTest(request);
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private void validateGSDPollRequestDTO(final GSDPollRequestDTO gsdPollRequest) {
		logger.debug("validateGSDPollRequestDTO started...");
		
		if (gsdPollRequest == null) {
			throw new InvalidParameterException("GSDPollRequestDTO is null");
		}
		
		if (gatewayIsMandatory && !gsdPollRequest.isGatewayIsPresent()) {
			throw new InvalidParameterException("Requester cloud must have gateway available");
		}
		
		if (gsdPollRequest.getRequestedService() == null) {
			throw new InvalidParameterException("RequestedService is null");
		}
		
		if (Utilities.isEmpty(gsdPollRequest.getRequestedService().getServiceDefinitionRequirement())) {
			throw new InvalidParameterException("serviceDefinitionRequirement is empty");
		}
		
		if (gsdPollRequest.getRequesterCloud() == null) {
			throw new InvalidParameterException("RequesterCloud is empty");
		}
		
		final boolean operatorIsEmpty = Utilities.isEmpty(gsdPollRequest.getRequesterCloud().getOperator());
		final boolean nameIsEmpty = Utilities.isEmpty(gsdPollRequest.getRequesterCloud().getName());
		
		if (operatorIsEmpty || nameIsEmpty) {
			String exceptionMsg = "GSDPollRequestDTO.CloudRequestDTO is invalid due to the following reasons:";
			exceptionMsg = operatorIsEmpty ? exceptionMsg + " operator is empty, " : exceptionMsg;
			exceptionMsg = nameIsEmpty ? exceptionMsg + " name is empty, " : exceptionMsg;
			exceptionMsg = exceptionMsg.substring(0, exceptionMsg.length() - 1);
			
			throw new InvalidParameterException(exceptionMsg);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private void validateMultiGSDPollRequestDTO(final GSDMultiPollRequestDTO gsdPollRequest) {
		logger.debug("validateMultiGSDPollRequestDTO started...");
		
		if (gsdPollRequest == null) {
			throw new InvalidParameterException("GSDMultiPollRequestDTO is null");
		}
		
		if (gatewayIsMandatory && !gsdPollRequest.isGatewayIsPresent()) {
			throw new InvalidParameterException("Requester cloud must have gateway available");
		}
		
		if (Utilities.isEmpty(gsdPollRequest.getRequestedServices())) {
			throw new InvalidParameterException("RequestedServices list is null or empty");
		}
		
		for (final ServiceQueryFormDTO reqService : gsdPollRequest.getRequestedServices()) {
			if (Utilities.isEmpty(reqService.getServiceDefinitionRequirement())) {
				throw new InvalidParameterException("serviceDefinitionRequirement is empty");
			}
		}
		
		if (gsdPollRequest.getRequesterCloud() == null) {
			throw new InvalidParameterException("RequesterCloud is empty");
		}
		
		final boolean operatorIsEmpty = Utilities.isEmpty(gsdPollRequest.getRequesterCloud().getOperator());
		final boolean nameIsEmpty = Utilities.isEmpty(gsdPollRequest.getRequesterCloud().getName());
		
		if (operatorIsEmpty || nameIsEmpty) {
			String exceptionMsg = "GSDMultiPollRequestDTO.CloudRequestDTO is invalid due to the following reasons:";
			exceptionMsg = operatorIsEmpty ? exceptionMsg + " operator is empty," : exceptionMsg;
			exceptionMsg = nameIsEmpty ? exceptionMsg + " name is empty," : exceptionMsg;
			exceptionMsg = exceptionMsg.substring(0, exceptionMsg.length() - 1);
			
			throw new InvalidParameterException(exceptionMsg);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private List<Cloud> getCloudsByCloudRequestDTOs(final List<CloudRequestDTO> cloudDTOs) {
		logger.debug("getCloudsByCloudRequestDTOs started...");
		
		final List<Cloud> clouds = new ArrayList<>();
		for (final CloudRequestDTO dto : cloudDTOs) {
			try {
				final Cloud cloud = gatekeeperDBService.getCloudByOperatorAndName(dto.getOperator(), dto.getName());
				clouds.add(cloud);
			} catch (final InvalidParameterException ex) { // ignore non existent clouds
				logger.debug(ex.getMessage(), ex);
			}
		}
		
		return clouds;
	}
	
	//-------------------------------------------------------------------------------------------------
	private void validateICNForm(final ICNRequestFormDTO form) {
		logger.debug("validateICNForm started...");
		
		if (form == null) {
			throw new InvalidParameterException("ICN form is null");
		}
		
		if (form.getRequestedService() == null) {
			throw new InvalidParameterException("Requested service is null");
		}
		
		if (Utilities.isEmpty(form.getRequestedService().getServiceDefinitionRequirement())) {
			throw new InvalidParameterException("Requested service definition is null or blank");
		}
		
		final Long id = form.getTargetCloudId();
		if (id == null || id < 1) {
			throw new InvalidParameterException("Invalid id: " + id);
		}
		
		validateSystemRequest(form.getRequesterSystem());
		
		for (final SystemRequestDTO preferredSystem : form.getPreferredSystems()) {
			validateSystemRequest(preferredSystem);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private void validateICNProposalRequest(final ICNProposalRequestDTO request) {
		logger.debug("validateICNProposalRequest started...");
		
		if (request == null) {
			throw new InvalidParameterException("ICN proposal request is null");
		}
		
		if (gatewayIsMandatory && !request.getGatewayIsPresent()) {
			throw new AuthException("Services are only available via gateway."); 
		}
		
		if (gatewayIsMandatory && request.getPreferredGatewayRelays().isEmpty() && request.getKnownGatewayRelays().isEmpty()) {
			throw new AuthException("Services are only available via relay.");
		}
		
		if (request.getRequestedService() == null) {
			throw new InvalidParameterException("Requested service is null.");
		}
		
		if (Utilities.isEmpty(request.getRequestedService().getServiceDefinitionRequirement())) {
			throw new InvalidParameterException("Requested service definition is null or blank.");
		}
		
		validateSystemRequest(request.getRequesterSystem());
		validateCloudRequest(request.getRequesterCloud());
		
		for (final SystemRequestDTO preferredSystem : request.getPreferredSystems()) {
			validateSystemRequest(preferredSystem);
		}
		
		for (final RelayRequestDTO preferredRelay : request.getPreferredGatewayRelays()) {
			validateRelayRequest(preferredRelay);
		}
		
		for (final RelayRequestDTO knownRelay : request.getKnownGatewayRelays()) {
			validateRelayRequest(knownRelay);
		}
		
		if (gatewayIsMandatory && Utilities.isEmpty(request.getConsumerGatewayPublicKey())) {
			throw new InvalidParameterException("Consumer gateway public key is null or blank.");
		}
	}

	//-------------------------------------------------------------------------------------------------
	private void validateSystemRequest(final SystemRequestDTO system) {
		logger.debug("validateSystemRequest started...");
		
		if (system == null) {
			throw new InvalidParameterException("System is null");
		}
		
		if (Utilities.isEmpty(system.getSystemName())) {
			throw new InvalidParameterException("System name is null or blank");
		}
		
		if (Utilities.isEmpty(system.getAddress())) {
			throw new InvalidParameterException("System address is null or blank");
		}
		
		if (system.getPort() == null) {
			throw new InvalidParameterException("System port is null");
		}
		
		final int validatedPort = system.getPort().intValue();
		if (validatedPort < CommonConstants.SYSTEM_PORT_RANGE_MIN || validatedPort > CommonConstants.SYSTEM_PORT_RANGE_MAX) {
			throw new InvalidParameterException("System port must be between " + CommonConstants.SYSTEM_PORT_RANGE_MIN + " and " + CommonConstants.SYSTEM_PORT_RANGE_MAX + ".");
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private void validateCloudRequest(final CloudRequestDTO cloud) {
		logger.debug("validateCloudRequest started...");
		
		if (cloud == null) {
			throw new InvalidParameterException("Cloud is null");
		}
		
		if (Utilities.isEmpty(cloud.getOperator())) {
			throw new InvalidParameterException("Cloud operator is null or blank");
		}
		
		if (Utilities.isEmpty(cloud.getName())) {
			throw new InvalidParameterException("Cloud name is null or empty");
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private void validateRelayRequest(final RelayRequestDTO relay) {
		logger.debug("validateRelayRequest started...");
		
		if (relay == null) {
			throw new InvalidParameterException("relay is null");
		}
			
		if (Utilities.isEmpty(relay.getAddress())) {
			throw new InvalidParameterException("Relay address is null or blank");
		}
		
		if (relay.getPort() == null) {
			throw new InvalidParameterException("Relay port is null");
		}
		
		final int validatedPort = relay.getPort().intValue();
		if (validatedPort < CommonConstants.SYSTEM_PORT_RANGE_MIN || validatedPort > CommonConstants.SYSTEM_PORT_RANGE_MAX) {
			throw new InvalidParameterException("Relay port must be between " + CommonConstants.SYSTEM_PORT_RANGE_MIN + " and " + CommonConstants.SYSTEM_PORT_RANGE_MAX + ".");
		}
		
		if (Utilities.isEmpty(relay.getType())) {
			throw new InvalidParameterException("Relay type is null or blank");
		}
		
		final RelayType type = Utilities.convertStringToRelayType(relay.getType());
		if (type == null || type == RelayType.GATEKEEPER_RELAY) {
			throw new InvalidParameterException("Relay type is invalid");
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private CloudRequestDTO getOwnCloud() {
		logger.debug("getOwnCloud started...");
		
		final Cloud ownCloud = commonDBService.getOwnCloud(true); // gatekeeper works only secure mode
		return getCloudRequestDTO(ownCloud);
	}
	
	//-------------------------------------------------------------------------------------------------
	private CloudRequestDTO getCloudRequestDTO(final Cloud cloud) {
		logger.debug("getCloudRequestDTO started...");
		Assert.notNull(cloud, "cloud is null.");
		
		final CloudRequestDTO result = new CloudRequestDTO();
		result.setOperator(cloud.getOperator());
		result.setName(cloud.getName());
		result.setAuthenticationInfo(cloud.getAuthenticationInfo());
		
		return result;
	}
	
	//-------------------------------------------------------------------------------------------------
	private List<RelayRequestDTO> getPreferredRelays(final Cloud targetCloud) {
		logger.debug("getPreferredRelays started...");

		final Set<CloudGatewayRelay> gatewayRelays = targetCloud.getGatewayRelays();
		final List<RelayRequestDTO> result = new ArrayList<>(gatewayRelays.size());
		
		for (final CloudGatewayRelay relayConn : gatewayRelays) {
			final Relay relay = relayConn.getRelay();
			result.add(new RelayRequestDTO(relay.getAddress(), relay.getPort(), relay.getAuthenticationInfo(), relay.getSecure(), relay.getExclusive(), relay.getType().name()));
		}
		
		return result;
	}
	
	//-------------------------------------------------------------------------------------------------
	private List<RelayRequestDTO> getKnownRelays() {
		logger.debug("getKnownRelays started...");
		
		return DTOConverter.convertRelayListToRelayRequestDTOList(gatekeeperDBService.getPublicGatewayRelays());
	}
	
	//-------------------------------------------------------------------------------------------------
	private PreferredProviderDataDTO[] getPreferredProviders(final List<SystemRequestDTO> preferredSystems) {
		logger.debug("getPreferredProviders started...");
		
		final PreferredProviderDataDTO[] result = new PreferredProviderDataDTO[preferredSystems.size()];
		for (int i = 0; i < preferredSystems.size(); ++i) {
			result[i] = new PreferredProviderDataDTO();
			result[i].setProviderSystem(preferredSystems.get(i));
		}
		
		return result;
	}
	
	//-------------------------------------------------------------------------------------------------
	private Relay selectRelay(final ICNProposalRequestDTO request) {
		logger.debug("selectRelay started...");
		
		final Cloud requesterCloud = gatekeeperDBService.getCloudByOperatorAndName(request.getRequesterCloud().getOperator(), request.getRequesterCloud().getName());
		final RelayMatchmakingParameters relayMMParams = new RelayMatchmakingParameters(requesterCloud);
		relayMMParams.setPreferredGatewayRelays(request.getPreferredGatewayRelays());
		relayMMParams.setKnownGatewayRelays(request.getKnownGatewayRelays());
		
		return gatewayMatchmaker.doMatchmaking(relayMMParams);
	}
	
	//-------------------------------------------------------------------------------------------------
	private void validateQoSRelayTestProposalRequestDTO(final QoSRelayTestProposalRequestDTO request, final boolean checkAdditionalFields) {
		logger.debug("validateQoSRelayTestProposalRequestDTO started...");
		
		if (request == null) {
			throw new InvalidParameterException("Relay test proposal is null.");
		}
		
		validateCloudRequest(request.getTargetCloud());
		validateRelayRequest(request.getRelay());
		
		if (checkAdditionalFields) {
			validateCloudRequest(request.getRequesterCloud());
		}
		
		if (checkAdditionalFields && Utilities.isEmpty(request.getSenderQoSMonitorPublicKey())) {
			throw new InvalidParameterException("Sender QoS Monitor's public key is null or blank.");
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private CloudRequestDTO getOwnCloudRequestDTO() {
		logger.debug("getOwnCloudRequestDTO started...");
		
		final Cloud requesterCloud = commonDBService.getOwnCloud(true);
		final CloudRequestDTO requesterCloudDTO = new CloudRequestDTO();
		requesterCloudDTO.setName(requesterCloud.getName());
		requesterCloudDTO.setOperator(requesterCloud.getOperator());
		
		return requesterCloudDTO;
	}
	
	//-------------------------------------------------------------------------------------------------
	private Set<String> getReservedProviderIdAndServiceIdPairs() {
		logger.debug("getReservedProviderIdAndSystemIdPairs started...");
		
		final Set<String> reservedProviderIdAndSystemIdPairs = new HashSet<>();
		final List<QoSReservationResponseDTO> qosReservations = gatekeeperDriver.getQoSReservationList();
		final ZonedDateTime now = ZonedDateTime.now();
		
		for (final QoSReservationResponseDTO reservation : qosReservations) {
			if (Utilities.parseUTCStringToLocalZonedDateTime(reservation.getReservedTo()).toEpochSecond() - now.toEpochSecond() > qosReservationPufferSeconds) {
				reservedProviderIdAndSystemIdPairs.add(reservation.getReservedProviderId() + "-" + reservation.getReservedServiceId());
			}
		}
		return reservedProviderIdAndSystemIdPairs;
	}
	
	//-------------------------------------------------------------------------------------------------
	private OrchestrationResponseDTO filterOutReservedProviders(final OrchestrationResponseDTO response) {
		logger.debug("filterOutReservedProviders started...");
		
		final Set<String> reservedProviderIdAndServiceIdPairs = getReservedProviderIdAndServiceIdPairs();
		final List<OrchestrationResultDTO> filteredResults = new ArrayList<>();
		for (final OrchestrationResultDTO orchestrationResult : response.getResponse()) {
			if (!reservedProviderIdAndServiceIdPairs.contains(orchestrationResult.getProvider().getId() + "-" + orchestrationResult.getService().getId())) {
				filteredResults.add(orchestrationResult);
			}
		}
		
		return new OrchestrationResponseDTO(filteredResults);
	}
	
	//-------------------------------------------------------------------------------------------------
	private  OrchestrationResponseDTO fillOrchestrationResultsWithLocalQoSMeasurements(final OrchestrationResponseDTO response) {
		logger.debug("fillOrchestrationResultWithLocalQoSMeasurements started...");
		
		final List<OrchestrationResultDTO> updatedResults = new ArrayList<>();
		for (final OrchestrationResultDTO orchestrationResult : response.getResponse()) {
			try {				
				final QoSIntraPingMeasurementResponseDTO pingMeasurement = gatekeeperDriver.getQoSIntraPingMeasurementsForLocalSystem(orchestrationResult.getProvider().getId());
				if (pingMeasurement.hasRecord()) {
					orchestrationResult.setQosMeasurements(new QoSMeasurementAttributesFormDTO(null,
																							   pingMeasurement.isAvailable(),
																							   pingMeasurement.getLastAccessAt(),
																							   pingMeasurement.getMinResponseTime(),
																							   pingMeasurement.getMaxResponseTime(),
																							   pingMeasurement.getMeanResponseTimeWithTimeout(),
																							   pingMeasurement.getMeanResponseTimeWithoutTimeout(),
																							   pingMeasurement.getJitterWithTimeout(),
																							   pingMeasurement.getJitterWithoutTimeout(),
																							   pingMeasurement.getSent(),
																							   pingMeasurement.getReceived(),
																							   pingMeasurement.getSentAll(),
																							   pingMeasurement.getReceivedAll(),
																							   pingMeasurement.getLostPerMeasurementPercent()));
					updatedResults.add(orchestrationResult);
				} else {
					logger.debug("No measurement available. Provider skipped with id:" + orchestrationResult.getProvider().getId());
				}
			} catch (final ArrowheadException ex) {
				logger.debug("Exception occured during doICN - QoS details request. Provider skipped with id:" + orchestrationResult.getProvider().getId());
				logger.debug(ex.getMessage());
			}
		}
		return new OrchestrationResponseDTO(updatedResults);
	}
	
	//-------------------------------------------------------------------------------------------------
	private Relay findCorrespondingRelay(final RelayResponseDTO relayDTO) {
		logger.debug("findCorrespondingRelay started...");
		
		if (Utilities.isEmpty(relayDTO.getAuthenticationInfo())) {
			return gatekeeperDBService.getRelayByAddressAndPort(relayDTO.getAddress(), relayDTO.getPort());
		}
		
		return gatekeeperDBService.getRelayByAuthenticationInfo(relayDTO.getAuthenticationInfo());
	}
}