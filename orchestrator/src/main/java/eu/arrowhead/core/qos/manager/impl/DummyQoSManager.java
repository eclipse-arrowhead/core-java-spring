package eu.arrowhead.core.qos.manager.impl;

import java.util.List;

import eu.arrowhead.common.dto.shared.OrchestrationFormRequestDTO;
import eu.arrowhead.common.dto.shared.OrchestrationResultDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.core.qos.manager.QoSManager;

public class DummyQoSManager implements QoSManager {
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@Override
	public List<OrchestrationResultDTO> filterReservedProviders(final List<OrchestrationResultDTO> orList, final SystemRequestDTO requester) {
		return orList;
	}

	//-------------------------------------------------------------------------------------------------
	@Override
	public List<OrchestrationResultDTO> reserveProvidersTemporarily(final List<OrchestrationResultDTO> orList, final SystemRequestDTO requester) {
		return orList;
	}

	//-------------------------------------------------------------------------------------------------
	@Override
	public void confirmReservation(final OrchestrationResultDTO selected, final List<OrchestrationResultDTO> orList, final SystemRequestDTO requester) {
		// intentionally do nothing
	}

	//-------------------------------------------------------------------------------------------------
	@Override
	public List<OrchestrationResultDTO> verifyServices(final List<OrchestrationResultDTO> orList, final OrchestrationFormRequestDTO request) {
		return orList;
	}
}