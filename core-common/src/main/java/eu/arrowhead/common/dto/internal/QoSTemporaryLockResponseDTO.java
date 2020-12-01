package eu.arrowhead.common.dto.internal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import eu.arrowhead.common.dto.shared.OrchestrationResultDTO;

public class QoSTemporaryLockResponseDTO implements Serializable {
	
	//=================================================================================================
	// members
	
	private static final long serialVersionUID = 2279611306510520063L;
	
	protected List<OrchestrationResultDTO> response = new ArrayList<>();
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public QoSTemporaryLockResponseDTO() {}

	//-------------------------------------------------------------------------------------------------
	public QoSTemporaryLockResponseDTO(final List<OrchestrationResultDTO> response) {
		this.response = response != null ? response : List.of();
	}

	//-------------------------------------------------------------------------------------------------
	public List<OrchestrationResultDTO> getResponse() { return response; }

	//-------------------------------------------------------------------------------------------------
	public void setResponse(final List<OrchestrationResultDTO> response) {
		if (response != null) {
			this.response = response;
		}
	}

}
