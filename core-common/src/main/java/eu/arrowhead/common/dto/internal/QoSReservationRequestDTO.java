package eu.arrowhead.common.dto.internal;

import java.util.List;

import eu.arrowhead.common.dto.shared.OrchestrationResultDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;

public class QoSReservationRequestDTO extends QoSTemporaryLockRequestDTO {

	//=================================================================================================
	// members
	
	private static final long serialVersionUID = -6108159091117829133L;
	
	private OrchestrationResultDTO selected;

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public QoSReservationRequestDTO(final OrchestrationResultDTO selected, final SystemRequestDTO requester, final List<OrchestrationResultDTO> orList) {
		super(requester, orList);
		this.selected = selected;
	}

	//-------------------------------------------------------------------------------------------------
	public OrchestrationResultDTO getSelected() { return selected; }
	
	//-------------------------------------------------------------------------------------------------
	public void setSelected(final OrchestrationResultDTO selected) { this.selected = selected; }
	
	
}
