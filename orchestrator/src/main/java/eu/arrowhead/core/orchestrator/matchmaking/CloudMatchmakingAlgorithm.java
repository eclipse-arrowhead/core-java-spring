package eu.arrowhead.core.orchestrator.matchmaking;

import eu.arrowhead.common.dto.CloudResponseDTO;

public interface CloudMatchmakingAlgorithm {

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public CloudResponseDTO doMatchmaking(final CloudMatchmakingParameters params);
}