package eu.arrowhead.core.orchestrator.matchmaking;

import eu.arrowhead.common.dto.internal.CloudResponseDTO;

public interface CloudMatchmakingAlgorithm {

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public CloudResponseDTO doMatchmaking(final CloudMatchmakingParameters params);
}