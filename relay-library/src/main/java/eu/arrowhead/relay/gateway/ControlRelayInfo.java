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

public class ControlRelayInfo {

	//=================================================================================================
	// members
	
	private final MessageProducer controlRequestMessageSender;
	private final MessageProducer controlResponseMessageSender;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public ControlRelayInfo(final MessageProducer controlRequestMessageSender, final MessageProducer controlResponseMessageSender) {
		Assert.notNull(controlRequestMessageSender, "controlRequestMessageSender is null.");
		Assert.notNull(controlResponseMessageSender, "controlResponseMessageSender is null.");
		
		this.controlRequestMessageSender = controlRequestMessageSender;
		this.controlResponseMessageSender = controlResponseMessageSender;
	}

	//-------------------------------------------------------------------------------------------------
	public MessageProducer getControlRequestMessageSender() { return controlRequestMessageSender; }
	public MessageProducer getControlResponseMessageSender() { return controlResponseMessageSender; }
}