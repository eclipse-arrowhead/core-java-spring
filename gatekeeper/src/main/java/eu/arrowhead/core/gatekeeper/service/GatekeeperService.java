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
import eu.arrowhead.common.dto.RelayRequestDTO;
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
		//TODO: continue
		
		return null;
	}

	//-------------------------------------------------------------------------------------------------
	public ICNProposalResponseDTO doICNProposal(final ICNProposalRequestDTO request) {
		//TODO: implement
		
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
	private CloudRequestDTO getRequesterCloud() {
		final Cloud ownCloud = commonDBService.getOwnCloud(true); // gatekeeper works only secure mode
		final CloudRequestDTO result = new CloudRequestDTO();
		result.setOperator(ownCloud.getOperator());
		result.setName(ownCloud.getName());
		result.setAuthenticationInfo(ownCloud.getAuthenticationInfo());
		
		return result;
	}
	
	//-------------------------------------------------------------------------------------------------
	private List<RelayRequestDTO> getPreferredRelays(final Cloud targetCloud) {
		final Set<CloudGatewayRelay> gatewayRelays = targetCloud.getGatewayRelays();
		final List<RelayRequestDTO> result = new ArrayList<RelayRequestDTO>(gatewayRelays.size());
		
		for (final CloudGatewayRelay relayConn : gatewayRelays) {
			final Relay relay = relayConn.getRelay();
			result.add(new RelayRequestDTO(relay.getAddress(), relay.getPort(), relay.getSecure(), relay.getExclusive(), relay.getType().name()));
		}
		
		return result;
	}
}