package eu.arrowhead.relay.gateway;

import javax.jms.MessageProducer;

import org.springframework.util.Assert;

import eu.arrowhead.common.Utilities;

public class ProviderSideRelayInfo {
	
	//=================================================================================================
	// members
	
	private final String peerName;
	private final String queueId;
	
	private final MessageProducer messageSender;
	private final MessageProducer controlMessageSender;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public ProviderSideRelayInfo(final String peerName, final String queueId, final MessageProducer messageSender, final MessageProducer controlMessageSender) {
		Assert.isTrue(!Utilities.isEmpty(peerName), "peerName is null or empty.");
		Assert.isTrue(!Utilities.isEmpty(queueId), "queueId is null or empty.");
		Assert.notNull(messageSender, "messageSender is null.");
		Assert.notNull(controlMessageSender, "controlMessageSender is null.");
		
		this.peerName = peerName;
		this.queueId = queueId;
		this.messageSender = messageSender;
		this.controlMessageSender = controlMessageSender;
	}

	//-------------------------------------------------------------------------------------------------
	public String getPeerName() { return peerName; }
	public String getQueueId() { return queueId; }
	public MessageProducer getMessageSender() { return messageSender; }
	public MessageProducer getControlMessageSender() { return controlMessageSender; }
}