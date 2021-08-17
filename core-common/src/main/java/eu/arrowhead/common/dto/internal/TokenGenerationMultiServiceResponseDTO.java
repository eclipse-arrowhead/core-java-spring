package eu.arrowhead.common.dto.internal;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class TokenGenerationMultiServiceResponseDTO implements Serializable {

	//=================================================================================================
	// members
	
	private static final long serialVersionUID = -5744544456252438018L;
	
	private Map<String,TokenGenerationResponseDTO> tokenMap = new HashMap<>();

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public TokenGenerationMultiServiceResponseDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public TokenGenerationMultiServiceResponseDTO(final Map<String,TokenGenerationResponseDTO> tokenMap) {
		this.tokenMap = tokenMap;
	}

	//-------------------------------------------------------------------------------------------------
	public Map<String,TokenGenerationResponseDTO> getTokenMap() { return tokenMap; }
	
	//-------------------------------------------------------------------------------------------------
	public void setTokenMap(final Map<String,TokenGenerationResponseDTO> tokenMap) { this.tokenMap = tokenMap; }
}
