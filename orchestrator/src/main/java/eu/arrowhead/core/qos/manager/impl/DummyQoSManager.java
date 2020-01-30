package eu.arrowhead.core.qos.manager.impl;

import java.util.List;

import eu.arrowhead.common.dto.shared.OrchestrationFormRequestDTO;
import eu.arrowhead.common.dto.shared.OrchestrationResultDTO;
import eu.arrowhead.core.qos.manager.QoSManager;

public class DummyQoSManager implements QoSManager {
	
	//=================================================================================================
	// methods


	//-------------------------------------------------------------------------------------------------
	@Override
	public List<OrchestrationResultDTO> filterReservedProviders(final List<OrchestrationResultDTO> orList, final OrchestrationFormRequestDTO request) {
		return orList;
	}
}