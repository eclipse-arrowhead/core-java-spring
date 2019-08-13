package eu.arrowhead.core.orchestrator.matchmaking;

import eu.arrowhead.common.dto.CloudResponseDTO;

public interface InterCloudCloudMatchmakingAlgorithm {

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public CloudResponseDTO doMatchmaking(final InterCloudCloudMatchmakingParameters params);
}
