package eu.arrowhead.core.gatekeeper.relay;

import java.security.PublicKey;

import javax.jms.MessageConsumer;

import org.springframework.util.Assert;

import eu.arrowhead.common.Utilities;

public class GeneralAdvertisementResult {

	//=================================================================================================
	// members
	
	// data needed to send a request
	private final String sessionId;
	private final String peerCN;
	private final PublicKey peerPublicKey;
	private final MessageConsumer answerReceiver;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public GeneralAdvertisementResult(final MessageConsumer answerReceiver, final String peerCN, final PublicKey peerPublicKey, final String sessionId) {
		Assert.notNull(answerReceiver, "Receiver is null.");
		Assert.isTrue(!Utilities.isEmpty(peerCN), "Peer common name is null or blank.");
		Assert.notNull(peerPublicKey, "Peer public key is null.");
		Assert.isTrue(!Utilities.isEmpty(sessionId), "Session id is null or blank.");
		
		this.answerReceiver = answerReceiver;
		this.peerCN = peerCN;
		this.peerPublicKey = peerPublicKey;
		this.sessionId = sessionId;
	}

	//-------------------------------------------------------------------------------------------------
	public String getSessionId() { return sessionId; }
	public String getPeerCN() { return peerCN; }
	public PublicKey getPeerPublicKey() { return peerPublicKey; }
	public MessageConsumer getAnswerReceiver() { return answerReceiver; }
}