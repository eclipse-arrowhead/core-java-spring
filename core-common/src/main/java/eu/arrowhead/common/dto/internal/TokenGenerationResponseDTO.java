package eu.arrowhead.common.dto.internal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class TokenGenerationResponseDTO implements Serializable {

	//=================================================================================================
	// members
	
	private static final long serialVersionUID = 2874487907056422383L;
	
	private List<TokenDataDTO> tokenData = new ArrayList<>();

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public List<TokenDataDTO> getTokenData() { return tokenData; }

	//-------------------------------------------------------------------------------------------------
	public void setTokenData(final List<TokenDataDTO> tokenData) { this.tokenData = tokenData; }
}