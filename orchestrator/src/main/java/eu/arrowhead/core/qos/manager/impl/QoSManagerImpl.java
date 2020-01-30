package eu.arrowhead.core.qos.manager.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.entity.QoSReservation;
import eu.arrowhead.common.database.entity.ServiceRegistry;
import eu.arrowhead.common.dto.shared.OrchestrationFormRequestDTO;
import eu.arrowhead.common.dto.shared.OrchestrationResultDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.core.qos.database.service.QoSReservationDBService;
import eu.arrowhead.core.qos.manager.QoSManager;

public class QoSManagerImpl implements QoSManager {
	
	//=================================================================================================
	// members

	@Autowired
	private QoSReservationDBService qosReservationDBService;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@Override
	public List<OrchestrationResultDTO> filterReservedProviders(final List<OrchestrationResultDTO> orList, final OrchestrationFormRequestDTO request) {
		Assert.notNull(orList, "'orList' is null.");
		Assert.notNull(request, "'request' is null.");
		final SystemRequestDTO requesterSystem = request.getRequesterSystem();
		Assert.notNull(requesterSystem, "Requester system is null.");
		Assert.isTrue(!Utilities.isEmpty(requesterSystem.getSystemName()),  "Requester system's name is null.");
		Assert.isTrue(!Utilities.isEmpty(requesterSystem.getAddress()),  "Requester system's address is null.");
		
		if (orList.isEmpty()) {
			return orList;
		}
		
		final List<QoSReservation> reservations = qosReservationDBService.getAllReservationsExceptMine(requesterSystem.getSystemName(), requesterSystem.getAddress(), requesterSystem.getPort());
		final List<OrchestrationResultDTO> result = new ArrayList<>();
		for (final OrchestrationResultDTO dto : orList) {
			if (!isReserved(dto, reservations)) {
				result.add(dto);
			}
		}
		
		return result;
	}
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	private boolean isReserved(final OrchestrationResultDTO dto, final List<QoSReservation> reservations) {
		for (final QoSReservation reservation : reservations) {
			final ServiceRegistry reservedService = reservation.getReservedService();
			if (reservedService.getSystem().getId() == dto.getProvider().getId() && reservedService.getServiceDefinition().getId() == dto.getService().getId()) {
				return true;
			}
		}
		
		return false;
	}
}