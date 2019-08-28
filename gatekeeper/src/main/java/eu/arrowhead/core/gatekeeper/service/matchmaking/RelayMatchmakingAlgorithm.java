package eu.arrowhead.core.gatekeeper.service.matchmaking;

import eu.arrowhead.common.database.entity.Relay;

public interface RelayMatchmakingAlgorithm {

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public Relay doMatchmaking(final RelayMatchmakingParameters parameters);
}