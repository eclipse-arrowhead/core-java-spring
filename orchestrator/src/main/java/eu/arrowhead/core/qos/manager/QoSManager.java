package eu.arrowhead.core.qos.manager;

import java.util.List;

import eu.arrowhead.common.dto.shared.OrchestrationFormRequestDTO;
import eu.arrowhead.common.dto.shared.OrchestrationResultDTO;

public interface QoSManager {

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	// verification-related
	
	//-------------------------------------------------------------------------------------------------
	// reservation-related
	public List<OrchestrationResultDTO> filterReservedProviders(final List<OrchestrationResultDTO> orList, final OrchestrationFormRequestDTO request);
}