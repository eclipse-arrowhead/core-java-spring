package eu.arrowhead.core.orchestrator.matchmaking;

import java.util.List;

import eu.arrowhead.common.dto.shared.ServiceRegistryResponseDTO;

public interface IntraCloudProviderMatchmakingAlgorithm {

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public ServiceRegistryResponseDTO doMatchmaking(final List<ServiceRegistryResponseDTO> srList, final IntraCloudProviderMatchmakingParameters params);
}