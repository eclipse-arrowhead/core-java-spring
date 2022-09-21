/********************************************************************************
 * Copyright (c) 2021 AITIA
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

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class ConsumerSideRelayInfoTest {

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorMessageSenderNull() {
		try {
			new ConsumerSideRelayInfo(null, null, null, null);
		} catch (final Exception ex) {
			Assert.assertEquals("messageSender is null.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorControlMessageSenderNull() {
		final MessageProducer producer = Mockito.mock(MessageProducer.class);
		
		try {
			new ConsumerSideRelayInfo(producer, null, null, null);
		} catch (final Exception ex) {
			Assert.assertEquals("controlMessageSender is null.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorMessageConsumerNull() {
		final MessageProducer producer = Mockito.mock(MessageProducer.class);
		
		try {
			new ConsumerSideRelayInfo(producer, producer, null, null);
		} catch (final Exception ex) {
			Assert.assertEquals("messageConsumer is null.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorControlMessageConsumerNull() {
		final MessageProducer producer = Mockito.mock(MessageProducer.class);
		final MessageConsumer consumer = Mockito.mock(MessageConsumer.class);
		
		try {
			new ConsumerSideRelayInfo(producer, producer, consumer, null);
		} catch (final Exception ex) {
			Assert.assertEquals("controlMessageConsumer is null.", ex.getMessage());
			
			throw ex;
		}
	}
}