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
	public MessageProducer getControlResponseMessageSender() { return controlMessageSender; }
}