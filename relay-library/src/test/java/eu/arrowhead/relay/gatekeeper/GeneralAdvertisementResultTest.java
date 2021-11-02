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

package eu.arrowhead.relay.gatekeeper;

import java.security.PublicKey;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;

import org.junit.Assert;
import org.junit.Test;

public class GeneralAdvertisementResultTest {
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorReceiverNull() {
		try {
			new GeneralAdvertisementResult(null, null, null, null);
		} catch (final Exception ex) {
			Assert.assertEquals("Receiver is null.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorPeerCNNull() {
		try {
			new GeneralAdvertisementResult(getTestMessageConsumer(), null, null, null);
		} catch (final Exception ex) {
			Assert.assertEquals("Peer common name is null or blank.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorPeerCNEmpty() {
		try {
			new GeneralAdvertisementResult(getTestMessageConsumer(), "", null, null);
		} catch (final Exception ex) {
			Assert.assertEquals("Peer common name is null or blank.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorPeerPublicKeyNull() {
		try {
			new GeneralAdvertisementResult(getTestMessageConsumer(), "peer-cn", null, null);
		} catch (final Exception ex) {
			Assert.assertEquals("Peer public key is null.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorSessionIdNull() {
		try {
			new GeneralAdvertisementResult(getTestMessageConsumer(), "peer-cn", getTestPublicKey(), null);
		} catch (final Exception ex) {
			Assert.assertEquals("Session id is null or blank.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorSessionIdEmpty() {
		try {
			new GeneralAdvertisementResult(getTestMessageConsumer(), "peer-cn", getTestPublicKey(), "");
		} catch (final Exception ex) {
			Assert.assertEquals("Session id is null or blank.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private MessageConsumer getTestMessageConsumer() {
		return new MessageConsumer() {
			public Message receive(final long timeout) throws JMSException { return null; }
			public void close() throws JMSException {}
			public String getMessageSelector() throws JMSException { return null; }
			public MessageListener getMessageListener() throws JMSException { return null; }
			public void setMessageListener(final MessageListener listener) throws JMSException {}
			public Message receive() throws JMSException { return null; }
			public Message receiveNoWait() throws JMSException { return null; }
		};
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("serial")
	private PublicKey getTestPublicKey() {
		return new PublicKey() {
			public String getFormat() { return null; }
			public byte[] getEncoded() { return null; }
			public String getAlgorithm() { return null;	}
		};
	}
}