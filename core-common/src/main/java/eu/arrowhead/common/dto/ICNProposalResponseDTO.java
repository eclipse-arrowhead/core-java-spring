package eu.arrowhead.common.dto;

import java.io.Serializable;
import java.util.List;

public class ICNProposalResponseDTO extends OrchestrationResponseDTO implements Serializable {

	//=================================================================================================
	// members

	private static final long serialVersionUID = -1063781197847440272L;
	
	private boolean useGateway = false;
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public ICNProposalResponseDTO() {
		super();
	}

	//-------------------------------------------------------------------------------------------------
	public ICNProposalResponseDTO(final List<OrchestrationResultDTO> response, final boolean useGateway) {
		super(response);
		
		this.useGateway = useGateway;
	}

	//-------------------------------------------------------------------------------------------------
	public boolean isUseGateway() { return useGateway; }

	//-------------------------------------------------------------------------------------------------
	public void setUseGateway(final boolean useGateway) { this.useGateway = useGateway; }
}