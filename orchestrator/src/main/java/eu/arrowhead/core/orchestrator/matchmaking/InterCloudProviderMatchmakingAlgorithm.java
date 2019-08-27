package eu.arrowhead.core.orchestrator.matchmaking;

import eu.arrowhead.common.dto.OrchestrationResponseDTO;

public interface InterCloudProviderMatchmakingAlgorithm {
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public OrchestrationResponseDTO doMatchmaking(final InterCloudProviderMatchmakingParameters params);
}