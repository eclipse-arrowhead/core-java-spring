package eu.arrowhead.core.gatekeeper.service;

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
import eu.arrowhead.common.dto.internal.CloudAccessResponseDTO;
import eu.arrowhead.common.dto.internal.DTOConverter;
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
import eu.arrowhead.common.dto.internal.RelayRequestDTO;
import eu.arrowhead.common.dto.internal.RelayType;
import eu.arrowhead.common.dto.internal.SystemAddressSetRelayResponseDTO;
import eu.arrowhead.common.dto.shared.CloudRequestDTO;
import eu.arrowhead.common.dto.shared.ErrorMessageDTO;
import eu.arrowhead.common.dto.shared.ErrorWrapperDTO;
import eu.arrowhead.common.dto.shared.OrchestrationFlags.Flag;
import eu.arrowhead.common.dto.shared.OrchestrationFormRequestDTO;
import eu.arrowhead.common.dto.shared.OrchestrationResponseDTO;
import eu.arrowhead.common.dto.shared.OrchestrationResultDTO;
import eu.arrowhead.common.dto.shared.PreferredProviderDataDTO;
import eu.arrowhead.common.dto.shared.ServiceInterfaceResponseDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryResultDTO;
import eu.arrowhead.common.dto.shared.ServiceRegistryResponseDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
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

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public GSDQueryResultDTO initGSDPoll(final GSDQueryFormDTO gsdForm) throws InterruptedException {
		logger.debug("initGSDPoll started...");
		
		Assert.notNull(gsdForm, "GSDQueryFormDTO is null.");
		Assert.notNull(gsdForm.getRequestedService(), "requestedService is null.");
		Assert.notNull(gsdForm.getRequestedService().getServiceDefinitionRequirement(), "serviceDefinitionRequirement is null.");
		
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
		
		final GSDPollRequestDTO gsdPollRequestDTO = new GSDPollRequestDTO(gsdForm.getRequestedService(), getOwnCloud(), gatewayIsPresent);
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
	public GSDPollResponseDTO doGSDPoll(final GSDPollRequestDTO request) {
		logger.debug("doGSDPoll started...");
		
		validateGSDPollRequestDTO(request);
				
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
		
		// Cross checking Service Registry and Authorization results
		final Set<String> availableInterfaces = new HashSet<>();
		int numOfProviders = 0;
		
		for (final ServiceRegistryResponseDTO srEntryDTO : srQueryResult.getServiceQueryData()) {
			final long providerId = srEntryDTO.getProvider().getId();
			if (authorizedProviderIdsWithInterfaceIdList.containsKey(providerId)) {
				for (final ServiceInterfaceResponseDTO interfaceDTO : srEntryDTO.getInterfaces()) {
					if (authorizedProviderIdsWithInterfaceIdList.get(providerId).contains(interfaceDTO.getId())) {
						availableInterfaces.add(interfaceDTO.getInterfaceName());
					}
				}
				
				numOfProviders++;	
			}		
		}
		
		final Cloud ownCloud = commonDBService.getOwnCloud(true); // gatekeeper works only secure mode
		
		return new GSDPollResponseDTO(DTOConverter.convertCloudToCloudResponseDTO(ownCloud), request.getRequestedService().getServiceDefinitionRequirement(), List.copyOf(availableInterfaces), 
									  numOfProviders, request.getRequestedService().getMetadataRequirements());
	}
	
	//-------------------------------------------------------------------------------------------------
	public ICNResultDTO initICN(final ICNRequestFormDTO form) {
		logger.debug("initICN started...");
		
		validateICNForm(form);
		
		final Cloud targetCloud = gatekeeperDBService.getCloudById(form.getTargetCloudId());
		final CloudRequestDTO requesterCloud = getOwnCloud();
		final List<RelayRequestDTO> preferredRelays = getPreferredRelays(targetCloud);
		final List<RelayRequestDTO> knownRelays = getKnownRelays();
		final ICNProposalRequestDTO proposal = new ICNProposalRequestDTO(form.getRequestedService(), requesterCloud, form.getRequesterSystem(), form.getPreferredSystems(), preferredRelays,
																		 knownRelays, form.getNegotiationFlags(), gatewayIsPresent);
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
		final GatewayConsumerConnectionRequestDTO connectionRequest = new GatewayConsumerConnectionRequestDTO(DTOConverter.convertRelayResponseDTOToRelayRequestDTO(icnResponse.getRelay()),
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
		
		final PreferredProviderDataDTO[] preferredProviders = getPreferredProviders(request.getPreferredSystems());
		final OrchestrationFormRequestDTO orchestrationForm = new OrchestrationFormRequestDTO.Builder(request.getRequesterSystem())
																							 .requesterCloud(request.getRequesterCloud())
																							 .requestedService(request.getRequestedService())
																							 .flags(request.getNegotiationFlags())
																							 .flag(Flag.EXTERNAL_SERVICE_REQUEST, true)
																							 .preferredProviders(preferredProviders)
																							 .build();
		if (gatewayIsMandatory) {
			// changing the requesterSystem for the sake of proper token generation
			orchestrationForm.getRequesterSystem().setSystemName(CoreSystem.GATEWAY.name().toLowerCase());
		}
		
		OrchestrationResponseDTO orchestrationResponse = gatekeeperDriver.queryOrchestrator(orchestrationForm);
		if (orchestrationResponse.getResponse().isEmpty()) { // no results
			return new ICNProposalResponseDTO();
		}
		
		orchestrationResponse = gatekeeperDriver.queryAuthorizationBasedOnOchestrationResponse(request.getRequesterCloud(), orchestrationResponse);
		if (orchestrationResponse.getResponse().isEmpty()) { // no accessible results
			return new ICNProposalResponseDTO();
		}

		if (!gatewayIsMandatory) {
			return new ICNProposalResponseDTO(orchestrationResponse.getResponse());
		} 

		// gateway is used so we need a relay
		final Relay selectedRelay = selectRelay(request);
		if (selectedRelay == null) {
			throw new AuthException("No common communication relay was found.");
		}
		
		// in gateway mode we have to select one provider even if matchmaking is not enabled because we have to build an expensive connection between the consumer and the provider
		final OrchestrationResultDTO selectedResult = icnProviderMatchmaker.doMatchmaking(orchestrationResponse.getResponse(), new ICNProviderMatchmakingParameters(request.getPreferredSystems()));
		
		final SystemRequestDTO providerSystem = DTOConverter.convertSystemResponseDTOToSystemRequestDTO(selectedResult.getProvider());
		final GatewayProviderConnectionRequestDTO connectionRequest	= new GatewayProviderConnectionRequestDTO(DTOConverter.convertRelayToRelayRequestDTO(selectedRelay), request.getRequesterSystem(),
																											  providerSystem, request.getRequesterCloud(), getOwnCloud(),
																											  request.getRequestedService().getServiceDefinitionRequirement(), 
																											  request.getConsumerGatewayPublicKey());
		final GatewayProviderConnectionResponseDTO response = gatekeeperDriver.connectProvider(connectionRequest);
		
		return new ICNProposalResponseDTO(selectedResult, DTOConverter.convertRelayToRelayResponseDTO(selectedRelay), response);
	}
	
	public CloudAccessResponseDTO initAccessTypesCollection(final List<CloudRequestDTO> request) {
		//TODO
		return null;
	}
	
	//-------------------------------------------------------------------------------------------------
	public AccessTypeRelayResponseDTO returnAccessType() {
		return new AccessTypeRelayResponseDTO(!gatewayIsMandatory);
	}
	
	//-------------------------------------------------------------------------------------------------
	public SystemAddressSetRelayResponseDTO initSystemAddressCollection(final CloudRequestDTO request) {
		validateCloudRequest(request);
		
		final Cloud cloud = gatekeeperDBService.getCloudByOperatorAndName(request.getOperator(), request.getName());
		return gatekeeperDriver.sendSystemAddressCollectionRequest(cloud);
	}

	//-------------------------------------------------------------------------------------------------
	public SystemAddressSetRelayResponseDTO doSystemAddressCollection() {
		final Set<String> addresses = new HashSet<>();
		
		final ServiceQueryResultDTO results = gatekeeperDriver.sendServiceRegistryQueryAll();
		for (ServiceRegistryResponseDTO sr : results.getServiceQueryData()) {
			addresses.add(sr.getProvider().getAddress());
		}
		
		return new SystemAddressSetRelayResponseDTO(addresses);
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
	private List<Cloud> getCloudsByCloudRequestDTOs(final List<CloudRequestDTO> cloudDTOs) {
		logger.debug("getCloudsByCloudRequestDTOs started...");
		
		final List<Cloud> clouds = new ArrayList<>();
		for (final CloudRequestDTO dto : cloudDTOs) {
			final Cloud cloud = gatekeeperDBService.getCloudByOperatorAndName(dto.getOperator(), dto.getName());
			clouds.add(cloud);
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
			result.add(new RelayRequestDTO(relay.getAddress(), relay.getPort(), relay.getSecure(), relay.getExclusive(), relay.getType().name()));
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
		final Cloud requesterCloud = gatekeeperDBService.getCloudByOperatorAndName(request.getRequesterCloud().getOperator(), request.getRequesterCloud().getName());
		final RelayMatchmakingParameters relayMMParams = new RelayMatchmakingParameters(requesterCloud);
		relayMMParams.setPreferredGatewayRelays(request.getPreferredGatewayRelays());
		relayMMParams.setKnownGatewayRelays(request.getKnownGatewayRelays());
		
		return gatewayMatchmaker.doMatchmaking(relayMMParams);
	}
}