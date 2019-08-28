package eu.arrowhead.core.gateway.relay;

import javax.jms.MessageProducer;

import org.springframework.util.Assert;

public class ConsumerSideRelayInfo {

	//=================================================================================================
	// members
	
	private final MessageProducer messageSender;
	private final MessageProducer controlMessageSender;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public ConsumerSideRelayInfo(final MessageProducer messageSender, final MessageProducer controlMessageSender) {
		Assert.notNull(messageSender, "messageSender is null.");
		Assert.notNull(controlMessageSender, "controlMessageSender is null.");
		
		this.messageSender = messageSender;
		this.controlMessageSender = controlMessageSender;
	}

	//-------------------------------------------------------------------------------------------------
	public MessageProducer getMessageSender() { return messageSender; }
	public MessageProducer getControlMessageSender() { return controlMessageSender; }
}