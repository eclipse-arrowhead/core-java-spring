package eu.arrowhead.common.dto.internal;

import java.io.Serializable;

import org.springframework.util.Assert;

import eu.arrowhead.common.Utilities;

public class GeneralAdvertisementMessageDTO implements Serializable {
	
	//=================================================================================================
	// members
	
	private static final long serialVersionUID = 2917521789843318807L;
	
	private String senderCN;
	private String senderPublicKey;
	private String recipientCN;
	private String sessionId;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public GeneralAdvertisementMessageDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public GeneralAdvertisementMessageDTO(final String senderCN, final String senderPublicKey, final String recipientCN, final String sessionId) {
		Assert.isTrue(!Utilities.isEmpty(senderCN), "senderCN is null or blank.");
		Assert.isTrue(!Utilities.isEmpty(recipientCN), "recipientCN is null or blank.");
		Assert.isTrue(!Utilities.isEmpty(sessionId), "sessionId is null or blank.");
		
		this.senderCN = senderCN;
		this.senderPublicKey = senderPublicKey;
		this.recipientCN = recipientCN;
		this.sessionId = sessionId;
	}

	//-------------------------------------------------------------------------------------------------
	public String getSenderCN() { return senderCN; }
	public String getSenderPublicKey() { return senderPublicKey; }
	public String getRecipientCN() { return recipientCN; }
	public String getSessionId() { return sessionId; }
	
	//-------------------------------------------------------------------------------------------------
	public void setSenderCN(final String senderCN) { this.senderCN = senderCN; }
	public void setSenderPublicKey(final String senderPublicKey) { this.senderPublicKey = senderPublicKey; }
	public void setRecipientCN(final String recipientCN) { this.recipientCN = recipientCN; }
	public void setSessionId(final String sessionId) { this.sessionId = sessionId; }
}