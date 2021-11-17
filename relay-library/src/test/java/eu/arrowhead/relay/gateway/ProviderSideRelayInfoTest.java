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

public class ProviderSideRelayInfoTest {

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorPeerNameNull() {
		try {
			new ProviderSideRelayInfo(null, null, null, null, null, null);
		} catch (final Exception ex) {
			Assert.assertEquals("peerName is null or empty.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorPeerNameEmpty() {
		try {
			new ProviderSideRelayInfo("", null, null, null, null, null);
		} catch (final Exception ex) {
			Assert.assertEquals("peerName is null or empty.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorQueueIdNull() {
		try {
			new ProviderSideRelayInfo("peerName", null, null, null, null, null);
		} catch (final Exception ex) {
			Assert.assertEquals("queueId is null or empty.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorQueueIdEmpty() {
		try {
			new ProviderSideRelayInfo("peerName", "", null, null, null, null);
		} catch (final Exception ex) {
			Assert.assertEquals("queueId is null or empty.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorMessageSenderNull() {
		try {
			new ProviderSideRelayInfo("peerName", "queueId", null, null, null, null);
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
			new ProviderSideRelayInfo("peerName", "queueId", producer, null, null, null);
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
			new ProviderSideRelayInfo("peerName", "queueId", producer, producer, null, null);
		} catch (final Exception ex) {
			Assert.assertEquals("messageConsumer is null.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorConstrolMessageConsumerNull() {
		final MessageProducer producer = Mockito.mock(MessageProducer.class);
		final MessageConsumer consumer = Mockito.mock(MessageConsumer.class);
		
		try {
			new ProviderSideRelayInfo("peerName", "queueId", producer, producer, consumer, null);
		} catch (final Exception ex) {
			Assert.assertEquals("controlMessageConsumer is null.", ex.getMessage());
			
			throw ex;
		}
	}
}