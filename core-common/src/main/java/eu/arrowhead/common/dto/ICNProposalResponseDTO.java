package eu.arrowhead.common.dto;

import java.io.Serializable;
import java.util.List;

public class ICNProposalResponseDTO extends OrchestrationResponseDTO implements Serializable {

	//=================================================================================================
	// members

	private static final long serialVersionUID = 1797708492119758160L;
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public ICNProposalResponseDTO() {
		super();
	}

	//-------------------------------------------------------------------------------------------------
	public ICNProposalResponseDTO(final List<OrchestrationResultDTO> response) {
		super(response);
	}
}