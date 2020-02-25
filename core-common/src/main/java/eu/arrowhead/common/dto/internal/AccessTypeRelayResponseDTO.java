package eu.arrowhead.common.dto.internal;

import java.io.Serializable;

public class AccessTypeRelayResponseDTO implements Serializable {
	
	//=================================================================================================
	// members
	
	private static final long serialVersionUID = -4308914768098206657L;
	
	private boolean directAccess;
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public AccessTypeRelayResponseDTO() {}

	//-------------------------------------------------------------------------------------------------
	public AccessTypeRelayResponseDTO(final boolean directAccess) {
		this.directAccess = directAccess;
	}
	
	//-------------------------------------------------------------------------------------------------
	public boolean isDirectAccess() { return directAccess; }

	//-------------------------------------------------------------------------------------------------
	public void setDirectAccess(final boolean directAccess) { this.directAccess = directAccess; }
}