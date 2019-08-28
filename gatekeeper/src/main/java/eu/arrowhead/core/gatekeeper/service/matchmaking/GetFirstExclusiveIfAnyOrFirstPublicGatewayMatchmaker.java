package eu.arrowhead.core.gatekeeper.service.matchmaking;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import eu.arrowhead.common.database.entity.Relay;
import eu.arrowhead.core.gatekeeper.database.service.GatekeeperDBService;

public class GetFirstExclusiveIfAnyOrFirstPublicGatewayMatchmaker implements RelayMatchmakingAlgorithm {
	
	//=================================================================================================
	// members
	private static final Logger logger = LogManager.getLogger(GetFirstExclusiveIfAnyOrFirstPublicGatewayMatchmaker.class);
	
	@Autowired
	private GatekeeperDBService gatekeeperDBService;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	/** 
	 * This algorithm returns the first exclusive Gateway Relay if a cloud has any,
	 * otherwise returns the first public Gateway Relay (non-exclusive GATEWAY_RELAY or GENERAL_RELAY type)
	 * or returns null if matchmaking is not possible.
	 */
	@Override
	public Relay doMatchmaking(final RelayMatchmakingParameters parameters) {
		logger.debug("GetFirstExclusiveIfAnyOrFirstPublicGatewayMatchmaker.doMatchmaking started...");
		
		Assert.notNull(parameters, "RelayMatchmakingParameters is null");
		Assert.notNull(parameters.getCloud(), "Cloud is null");
		
		if (parameters.getCloud().getGatewayRelays() != null && !parameters.getCloud().getGatewayRelays().isEmpty()) {
			return parameters.getCloud().getGatewayRelays().iterator().next().getRelay();
		}
		
		final List<Relay> publicGatewayRelays = gatekeeperDBService.getPublicGatewayRelays();
		if (!publicGatewayRelays.isEmpty()) {
			return publicGatewayRelays.get(0);
		}
		
		return null;
	}

}
