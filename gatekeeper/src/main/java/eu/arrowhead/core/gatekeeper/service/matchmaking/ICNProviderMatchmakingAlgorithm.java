package eu.arrowhead.core.gatekeeper.service.matchmaking;

import java.util.List;

import eu.arrowhead.common.dto.internal.OrchestrationResultDTO;

public interface ICNProviderMatchmakingAlgorithm {

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public OrchestrationResultDTO doMatchmaking(final List<OrchestrationResultDTO> orList, final ICNProviderMatchmakingParameters params);
}