package eu.arrowhead.core.orchestrator.matchmaking;

import java.util.List;

import eu.arrowhead.common.dto.shared.OrchestrationResultDTO;

public interface IntraCloudProviderMatchmakingAlgorithm {

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public OrchestrationResultDTO doMatchmaking(final List<OrchestrationResultDTO> orList, final IntraCloudProviderMatchmakingParameters params);
}