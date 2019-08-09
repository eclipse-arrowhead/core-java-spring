package eu.arrowhead.core.gatekeeper.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.database.entity.Cloud;
import eu.arrowhead.common.database.entity.Relay;
import eu.arrowhead.common.dto.GSDPollRequestDTO;
import eu.arrowhead.common.dto.GSDPollResponseDTO;
import eu.arrowhead.common.dto.GSDQueryFormDTO;
import eu.arrowhead.common.dto.GSDQueryResultDTO;
import eu.arrowhead.common.dto.ICNProposalRequestDTO;
import eu.arrowhead.common.dto.ICNProposalResponseDTO;
import eu.arrowhead.core.gatekeeper.database.service.GatekeeperDBService;
import eu.arrowhead.core.gatekeeper.service.matchmaking.GatekeeperMatchmakingAlgorithm;
import eu.arrowhead.core.gatekeeper.service.matchmaking.GatekeeperMatchmakingParameters;

@Service
public class GatekeeperService {

	//=================================================================================================
	// members
	
	@Autowired
	private GatekeeperDBService gatekeeperDBService;
	
	@Resource(name = CommonConstants.GATEKEEPER_MATCHMAKER)
	private GatekeeperMatchmakingAlgorithm gatekeeperMatchmakeer;
	
	private final Logger logger = LogManager.getLogger(GatekeeperService.class);
	
	//=================================================================================================
	// methods
	
	public GSDQueryResultDTO initGSDPoll(final GSDQueryFormDTO gsdForm) {
		logger.debug("initGSDPoll started...");
		
		Assert.notNull(gsdForm, "GSDQueryFormDTO is null.");
		Assert.notNull(gsdForm.getRequestedService(), "requestedService is null.");
		Assert.notNull(gsdForm.getRequestedService().getServiceDefinitionRequirement(), "serviceDefinitionRequirement is null.");
		
		if (gsdForm.getCloudIdBoundaries() == null || gsdForm.getCloudIdBoundaries().isEmpty()) {
			// If no boundaries were given regarding to the clouds, then send GSD poll requests to the neighbor Clouds
			
			final List<Cloud> neighborClouds = gatekeeperDBService.getNeighborClouds();
			final Map<Cloud, Relay> realyPerCloud = getOneGatekeeperRealyPerCloud(neighborClouds);
			
		} else {
			// If boundaries were given regarding to the clouds, then send GSD poll requests only to those Clouds
			
			List<Cloud> clouds = gatekeeperDBService.getCloudsByIds(gsdForm.getCloudIdBoundaries());
			final Map<Cloud, Relay> realyPerCloud = getOneGatekeeperRealyPerCloud(clouds);
			
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
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------	
	
	private Map<Cloud, Relay> getOneGatekeeperRealyPerCloud(final List<Cloud> clouds) {
		logger.debug("collectGatekeeperURIs started...");
		
		final Map<Cloud, Relay> realyPerCloud = new HashMap<>();
		for (final Cloud cloud : clouds) {
			final Relay relay = gatekeeperMatchmakeer.doMatchmaking(new GatekeeperMatchmakingParameters(cloud));
			realyPerCloud.put(cloud, relay);
		}
		
		return realyPerCloud;
	}
}