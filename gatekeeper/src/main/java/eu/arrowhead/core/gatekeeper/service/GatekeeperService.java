package eu.arrowhead.core.gatekeeper.service;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.entity.Cloud;
import eu.arrowhead.common.database.entity.CloudGatewayRelay;
import eu.arrowhead.common.database.entity.Relay;
import eu.arrowhead.common.database.service.CommonDBService;
import eu.arrowhead.common.dto.CloudRequestDTO;
import eu.arrowhead.common.dto.GSDPollRequestDTO;
import eu.arrowhead.common.dto.GSDPollResponseDTO;
import eu.arrowhead.common.dto.GSDQueryFormDTO;
import eu.arrowhead.common.dto.GSDQueryResultDTO;
import eu.arrowhead.common.dto.ICNProposalRequestDTO;
import eu.arrowhead.common.dto.ICNProposalResponseDTO;
import eu.arrowhead.common.dto.ICNRequestFormDTO;
import eu.arrowhead.common.dto.ICNResultDTO;
import eu.arrowhead.common.dto.OrchestrationFlags.Flag;
import eu.arrowhead.common.dto.OrchestrationFormRequestDTO;
import eu.arrowhead.common.dto.OrchestrationResponseDTO;
import eu.arrowhead.common.dto.PreferredProviderDataDTO;
import eu.arrowhead.common.dto.RelayRequestDTO;
import eu.arrowhead.common.dto.RelayType;
import eu.arrowhead.common.dto.SystemRequestDTO;
import eu.arrowhead.core.gatekeeper.database.service.GatekeeperDBService;

@Service
public class GatekeeperService {
	
	//=================================================================================================
	// members
	
	private final Logger logger = LogManager.getLogger(GatekeeperService.class);
	
	@Value(CommonConstants.$GATEKEEPER_IS_GATEWAY_PRESENT_WD)
	private boolean gatewayIsPresent;
	
	@Autowired
	private CommonDBService commonDBService;
	
	@Autowired
	private GatekeeperDBService gatekeeperDBService;
	
	@Autowired
	private GatekeeperDriver gatekeeperDriver;

	//=================================================================================================
	// methods

	public GSDQueryResultDTO initGSDPoll(final GSDQueryFormDTO gsdForm) {
		logger.debug("initGSDPoll started...");
		
		Assert.notNull(gsdForm, "GSDQueryFormDTO is null.");
		Assert.notNull(gsdForm.getRequestedService(), "requestedService is null.");
		Assert.notNull(gsdForm.getRequestedService().getServiceDefinitionRequirement(), "serviceDefinitionRequirement is null.");
		
		List<Cloud> cloudsToContact;
		
		if (gsdForm.getPreferredCloudIds() == null || gsdForm.getPreferredCloudIds().isEmpty()) {
			// If no preferred clouds were given, then send GSD poll requests to the neighbor Clouds
			
			final List<Cloud> neighborClouds = gatekeeperDBService.getNeighborClouds();
			
			if (neighborClouds.isEmpty()) {
				throw new InvalidParameterException("initGSDPoll failed: Neither preferred clouds were given, nor neighbor clouds registered");
			} else {
				cloudsToContact = neighborClouds;
			}			
			
		} else {
			// If preferred clouds were given, then send GSD poll requests only to those Clouds
			
			final List<Cloud> preferredClouds = gatekeeperDBService.getCloudsByIds(gsdForm.getPreferredCloudIds());
			
			if (preferredClouds.isEmpty()) {
				throw new InvalidParameterException("initGSDPoll failed: Given preferred clouds are not exists");
			} else {
				cloudsToContact = preferredClouds;
			}
			
		}
		
		final GSDPollRequestDTO gsdPollRequestDTO = new GSDPollRequestDTO(gsdForm.getRequestedService(), getRequesterCloud(), gatewayIsPresent);
		
		final List<GSDPollResponseDTO> gsdPollResponses = gatekeeperDriver.sendGSDPollRequest(cloudsToContact, gsdPollRequestDTO);
		
		final List<GSDPollResponseDTO> validResponses = new ArrayList<>();
		int emptyResponses = 0;
		for (final GSDPollResponseDTO gsdResponse : gsdPollResponses) {
			if (gsdResponse.getProviderCloud() == null) {
				emptyResponses++;
			} else {
				validResponses.add(gsdResponse);		
			}
		}
		
		return new GSDQueryResultDTO(validResponses, emptyResponses);
	}
	
	//-------------------------------------------------------------------------------------------------
	public GSDPollResponseDTO doGSDPoll(final GSDPollRequestDTO request) {
		//TODO: implement
		
		return null;
	}
	
	//-------------------------------------------------------------------------------------------------
	public ICNResultDTO initICN(final ICNRequestFormDTO form) {
		logger.debug("initICN started...");
		
		validateICNForm(form);
		
		final Cloud targetCloud = gatekeeperDBService.getCloudById(form.getTargetCloudId());
		final CloudRequestDTO requesterCloud = getRequesterCloud();
		final List<RelayRequestDTO> preferredRelays = getPreferredRelays(targetCloud);
		final ICNProposalRequestDTO proposal = new ICNProposalRequestDTO(form.getRequestedService(), requesterCloud, form.getRequesterSystem(), form.getPreferredSystems(), preferredRelays,
																		 form.getNegotiationFlags(), form.isUseGateway());
		
		final ICNProposalResponseDTO icnResponse = gatekeeperDriver.sendICNProposal(targetCloud, proposal);
		
		if (!form.isUseGateway()) {
			// just send back the response
			return new ICNResultDTO(icnResponse.getResponse());
		}
		
		//TODO: gateway-related code
		
		return null;
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
		
		if (request.isUseGateway()) {
			//TODO: we have to change the requesterSystem name  
		}
		
		OrchestrationResponseDTO orchestrationResponse = gatekeeperDriver.queryOrchestrator(orchestrationForm);
		if (orchestrationResponse.getResponse().isEmpty()) { // no results
			return new ICNProposalResponseDTO();
		}
		
		orchestrationResponse = gatekeeperDriver.queryAuthorizationBasedOnOchestrationResponse(request.getRequesterCloud(), orchestrationResponse);
		if (orchestrationResponse.getResponse().isEmpty()) { // no accessible results
			return new ICNProposalResponseDTO();
		}

		if (!request.isUseGateway()) {
			return new ICNProposalResponseDTO(orchestrationResponse.getResponse());
		} 
		
		//TODO: implement gateway-related code
		
		return null;
	}
	
	//=================================================================================================
	// assistant methods
	
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
		
		if (request.getRequestedService() == null) {
			throw new InvalidParameterException("Requested service is null");
		}
		
		if (Utilities.isEmpty(request.getRequestedService().getServiceDefinitionRequirement())) {
			throw new InvalidParameterException("Requested service definition is null or blank");
		}
		
		validateSystemRequest(request.getRequesterSystem());
		validateCloudRequest(request.getRequesterCloud());
		
		for (final SystemRequestDTO preferredSystem : request.getPreferredSystems()) {
			validateSystemRequest(preferredSystem);
		}
		
		for (final RelayRequestDTO preferredRelay : request.getPreferredGatewayRelays()) {
			validateRelayRequest(preferredRelay);
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
			throw new InvalidParameterException("Realy type is null or blank");
		}
		
		final RelayType type = Utilities.convertStringToRelayType(relay.getType());
		if (type == null || type == RelayType.GATEKEEPER_RELAY) {
			throw new InvalidParameterException("Realy type is invalid");
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private CloudRequestDTO getRequesterCloud() {
		logger.debug("getRequesterCloud started...");
		
		final Cloud ownCloud = commonDBService.getOwnCloud(true); // gatekeeper works only secure mode
		final CloudRequestDTO result = new CloudRequestDTO();
		result.setOperator(ownCloud.getOperator());
		result.setName(ownCloud.getName());
		result.setAuthenticationInfo(ownCloud.getAuthenticationInfo());
		
		return result;
	}
	
	//-------------------------------------------------------------------------------------------------
	private List<RelayRequestDTO> getPreferredRelays(final Cloud targetCloud) {
		logger.debug("getPreferredRelays started...");

		final Set<CloudGatewayRelay> gatewayRelays = targetCloud.getGatewayRelays();
		final List<RelayRequestDTO> result = new ArrayList<RelayRequestDTO>(gatewayRelays.size());
		
		for (final CloudGatewayRelay relayConn : gatewayRelays) {
			final Relay relay = relayConn.getRelay();
			result.add(new RelayRequestDTO(relay.getAddress(), relay.getPort(), relay.getSecure(), relay.getExclusive(), relay.getType().name()));
		}
		
		return result;
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
}