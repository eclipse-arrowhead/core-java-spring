package eu.arrowhead.common.dto.internal;

import java.io.Serializable;

public class DecryptedMessageDTO implements Serializable {
	
	//=================================================================================================
	// members
	
	private static final long serialVersionUID = -6568005268815512990L;
	
	private String messageType;
	private String sessionId;
	private String payload;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public String getMessageType() { return messageType; }
	public String getSessionId() { return sessionId; }
	public String getPayload() { return payload; }
	
	//-------------------------------------------------------------------------------------------------
	public void setMessageType(final String messageType) { this.messageType = messageType; }
	public void setSessionId(final String sessionId) { this.sessionId = sessionId; }
	public void setPayload(final String payload) { this.payload = payload; }
}