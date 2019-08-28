package eu.arrowhead.core.gateway.relay;

import java.io.Serializable;

import javax.jms.MessageProducer;

import org.springframework.util.Assert;

public class ConsumerSideRelayInfo implements Serializable {

	//=================================================================================================
	// members
	
	private static final long serialVersionUID = 7792982096033437783L;
	
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