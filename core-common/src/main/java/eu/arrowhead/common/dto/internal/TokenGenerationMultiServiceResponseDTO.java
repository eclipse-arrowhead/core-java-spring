package eu.arrowhead.common.dto.internal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TokenGenerationMultiServiceResponseDTO implements Serializable {

	//=================================================================================================
	// members
	
	private static final long serialVersionUID = -5744544456252438018L;
	
	private List<TokenGenerationDetailedResponseDTO> data = new ArrayList<>();

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public TokenGenerationMultiServiceResponseDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public TokenGenerationMultiServiceResponseDTO(final List<TokenGenerationDetailedResponseDTO> data) {
		this.data = data;
	}

	//-------------------------------------------------------------------------------------------------
	public List<TokenGenerationDetailedResponseDTO> getData() { return data; }
	
	//-------------------------------------------------------------------------------------------------
	public void setData(final List<TokenGenerationDetailedResponseDTO> data) { this.data = data; }
}
