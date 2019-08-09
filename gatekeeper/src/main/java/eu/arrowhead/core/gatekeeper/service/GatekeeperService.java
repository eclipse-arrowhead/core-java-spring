package eu.arrowhead.core.gatekeeper.service;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.arrowhead.common.database.entity.Cloud;
import eu.arrowhead.common.dto.GSDPollRequestDTO;
import eu.arrowhead.common.dto.GSDPollResponseDTO;
import eu.arrowhead.common.dto.GSDQueryFormDTO;
import eu.arrowhead.common.dto.GSDQueryResultDTO;
import eu.arrowhead.common.dto.ICNProposalRequestDTO;
import eu.arrowhead.common.dto.ICNProposalResponseDTO;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.core.gatekeeper.database.service.GatekeeperDBService;

@Service
public class GatekeeperService {

	//=================================================================================================
	// members
	
	@Autowired
	private GatekeeperDBService gatekeeperDBService;
	
	private final Logger logger = LogManager.getLogger(GatekeeperService.class);
	
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
		
		
		return null; //TODO finalize implementation
	}
	
	//-------------------------------------------------------------------------------------------------
	public GSDPollResponseDTO doGSDPoll(final GSDPollRequestDTO request) {
		//TODO: implement
		
		return null;
	}

	//-------------------------------------------------------------------------------------------------
	public ICNProposalResponseDTO doICNProposal(final ICNProposalRequestDTO request) {
		//TODO: implement
		
		return null;
	}
}