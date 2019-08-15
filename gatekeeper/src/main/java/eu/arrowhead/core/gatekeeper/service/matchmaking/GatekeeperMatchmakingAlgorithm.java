package eu.arrowhead.core.gatekeeper.service.matchmaking;

import eu.arrowhead.common.database.entity.Relay;

public interface GatekeeperMatchmakingAlgorithm {

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public Relay doMatchmaking(final GatekeeperMatchmakingParameters parameters);
}
