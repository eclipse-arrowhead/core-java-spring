package eu.arrowhead.core.gatekeeper.service.matchmaking;

import org.springframework.util.Assert;

import eu.arrowhead.common.database.entity.Relay;

public class GetFirstGatekeeperMatchmaker implements GatekeeperMatchmakingAlgorithm {

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	/** 
	 * This algorithm returns the first Gatekeeper Relay, no matter if it is GATEKEEPER_RELAY or GENERAL_RELAY type
	 */
	@Override
	public Relay doMatchmaking(final GatekeeperMatchmakingParameters parameters) {
		Assert.notNull(parameters, "GatekeeperMatchmakingParameters is null");
		Assert.notNull(parameters.getCloud(), "Cloud is null");
		Assert.isTrue(parameters.getCloud().getGatekeeperRelays() != null && !parameters.getCloud().getGatekeeperRelays().isEmpty(), "GatekeeperRelaysList is null or empty.");
		
		return parameters.getCloud().getGatekeeperRelays().iterator().next().getRelay();
	}
	
}
