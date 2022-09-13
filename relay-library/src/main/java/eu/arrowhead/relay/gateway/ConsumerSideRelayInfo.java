/********************************************************************************
 * Copyright (c) 2019 AITIA
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   AITIA - implementation
 *   Arrowhead Consortia - conceptualization
 ********************************************************************************/

package eu.arrowhead.relay.gateway;

import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;

import org.springframework.util.Assert;

public class ConsumerSideRelayInfo {

	//=================================================================================================
	// members
	
	private final MessageProducer messageSender;
	private final MessageProducer controlMessageSender;
	private final MessageConsumer messageConsumer;
	private final MessageConsumer controlMessageConsumer;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public ConsumerSideRelayInfo(final MessageProducer messageSender, final MessageProducer controlMessageSender,
								 final MessageConsumer messageConsumer, final MessageConsumer controlMessageConsumer) {
		Assert.notNull(messageSender, "messageSender is null.");
		Assert.notNull(controlMessageSender, "controlMessageSender is null.");
		Assert.notNull(messageConsumer, "messageConsumer is null.");
		Assert.notNull(controlMessageConsumer, "controlMessageConsumer is null.");
		
		this.messageSender = messageSender;
		this.controlMessageSender = controlMessageSender;
		this.messageConsumer = messageConsumer;
		this.controlMessageConsumer = controlMessageConsumer;
	}

	//-------------------------------------------------------------------------------------------------
	public MessageProducer getMessageSender() { return messageSender; }
	public MessageProducer getControlResponseMessageSender() { return controlMessageSender; }
	public MessageConsumer getMessageConsumer() { return messageConsumer; }
	public MessageConsumer getControlRequestMessageConsumer() { return controlMessageConsumer; }

}