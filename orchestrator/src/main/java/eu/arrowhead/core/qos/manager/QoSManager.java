package eu.arrowhead.core.qos.manager;

import java.util.List;

import eu.arrowhead.common.database.entity.QoSReservation;
import eu.arrowhead.common.dto.internal.GSDPollResponseDTO;
import eu.arrowhead.common.dto.shared.OrchestrationFormRequestDTO;
import eu.arrowhead.common.dto.shared.OrchestrationResultDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;

public interface QoSManager {

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	// verification-related
	public List<OrchestrationResultDTO> verifyIntraCloudServices(final List<OrchestrationResultDTO> orList, final OrchestrationFormRequestDTO request);
	public List<GSDPollResponseDTO> preVerifyInterCloudServices(final List<GSDPollResponseDTO> gsdList, final OrchestrationFormRequestDTO request);
	public List<OrchestrationResultDTO> verifyInterCloudServices(final List<OrchestrationResultDTO> orList, final OrchestrationFormRequestDTO request);
	
	//-------------------------------------------------------------------------------------------------
	// reservation-related
	public List<QoSReservation> fetchAllReservation();
	public List<OrchestrationResultDTO> filterReservedProviders(final List<OrchestrationResultDTO> orList, final SystemRequestDTO requester);
	public List<OrchestrationResultDTO> reserveProvidersTemporarily(final List<OrchestrationResultDTO> orList, final SystemRequestDTO requester); // returns with the temp locked results
	public void confirmReservation(final OrchestrationResultDTO selected, final List<OrchestrationResultDTO> orList, final SystemRequestDTO requester);
	
}