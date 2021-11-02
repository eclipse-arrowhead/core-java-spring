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

import javax.jms.CompletionListener;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;

import org.junit.Assert;
import org.junit.Test;

import eu.arrowhead.common.dto.internal.GSDMultiPollRequestDTO;
import eu.arrowhead.common.dto.internal.GSDPollRequestDTO;
import eu.arrowhead.common.dto.internal.GeneralRelayRequestDTO;
import eu.arrowhead.common.dto.internal.ICNProposalRequestDTO;
import eu.arrowhead.common.dto.internal.QoSRelayTestProposalRequestDTO;
import eu.arrowhead.common.exception.DataNotFoundException;

public class GatekeeperRelayRequestTest {
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorSenderNull() {
		try {
			new GatekeeperRelayRequest(null, null, null, null, null);
		} catch (final Exception ex) {
			Assert.assertEquals("Sender is null.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorPublicKeyNull() {
		try {
			new GatekeeperRelayRequest(getTestMessageProducer(), null, null, null, null);
		} catch (final Exception ex) {
			Assert.assertEquals("Peer public key is null.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorSessionIdNull() {
		try {
			new GatekeeperRelayRequest(getTestMessageProducer(), getTestPublicKey(), null, null, null);
		} catch (final Exception ex) {
			Assert.assertEquals("Session id is null or blank.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorSessionIdEmpty() {
		try {
			new GatekeeperRelayRequest(getTestMessageProducer(), getTestPublicKey(), "", null, null);
		} catch (final Exception ex) {
			Assert.assertEquals("Session id is null or blank.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorMessageTypeNull() {
		try {
			new GatekeeperRelayRequest(getTestMessageProducer(), getTestPublicKey(), "sessionId", null, null);
		} catch (final Exception ex) {
			Assert.assertEquals("Message type is null or blank.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorMessageTypeEmpty() {
		try {
			new GatekeeperRelayRequest(getTestMessageProducer(), getTestPublicKey(), "sessionId", "", null);
		} catch (final Exception ex) {
			Assert.assertEquals("Message type is null or blank.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorPayloadNull() {
		try {
			new GatekeeperRelayRequest(getTestMessageProducer(), getTestPublicKey(), "sessionId", "type", null);
		} catch (final Exception ex) {
			Assert.assertEquals("Payload is null.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetGSDPollRequestOk() {
		final GSDPollRequestDTO payload = new GSDPollRequestDTO();
		final GatekeeperRelayRequest request = new GatekeeperRelayRequest(getTestMessageProducer(), getTestPublicKey(), "sessionId", "type", payload);
		
		final GSDPollRequestDTO result = request.getGSDPollRequest();
		
		Assert.assertEquals(payload, result);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = DataNotFoundException.class)
	public void testGetGSDPollRequestProblem() {
		try {
			final GatekeeperRelayRequest request = new GatekeeperRelayRequest(getTestMessageProducer(), getTestPublicKey(), "sessionId", "type", new Object());
			request.getGSDPollRequest();
		} catch (final Exception ex) {
			Assert.assertEquals("The request is not a GSD poll.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetGSDMultiPollRequestOk() {
		final GSDMultiPollRequestDTO payload = new GSDMultiPollRequestDTO();
		final GatekeeperRelayRequest request = new GatekeeperRelayRequest(getTestMessageProducer(), getTestPublicKey(), "sessionId", "type", payload);
		
		final GSDMultiPollRequestDTO result = request.getGSDMultiPollRequest();
		
		Assert.assertEquals(payload, result);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = DataNotFoundException.class)
	public void testGetGSDMultiPollRequestProblem() {
		try {
			final GatekeeperRelayRequest request = new GatekeeperRelayRequest(getTestMessageProducer(), getTestPublicKey(), "sessionId", "type", new Object());
			request.getGSDMultiPollRequest();
		} catch (final Exception ex) {
			Assert.assertEquals("The request is not a multi GSD poll.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetICNProposalRequestOk() {
		final ICNProposalRequestDTO payload = new ICNProposalRequestDTO();
		final GatekeeperRelayRequest request = new GatekeeperRelayRequest(getTestMessageProducer(), getTestPublicKey(), "sessionId", "type", payload);
		
		final ICNProposalRequestDTO result = request.getICNProposalRequest();
		
		Assert.assertEquals(payload, result);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = DataNotFoundException.class)
	public void testGetICNProposalRequestProblem() {
		try {
			final GatekeeperRelayRequest request = new GatekeeperRelayRequest(getTestMessageProducer(), getTestPublicKey(), "sessionId", "type", new Object());
			request.getICNProposalRequest();
		} catch (final Exception ex) {
			Assert.assertEquals("The request is not an ICN proposal.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetGeneralRelayRequestOk() {
		final GeneralRelayRequestDTO payload = new GeneralRelayRequestDTO();
		final GatekeeperRelayRequest request = new GatekeeperRelayRequest(getTestMessageProducer(), getTestPublicKey(), "sessionId", "type", payload);
		
		final GeneralRelayRequestDTO result = request.getGeneralRelayRequest();
		
		Assert.assertEquals(payload, result);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = DataNotFoundException.class)
	public void testGetGeneralRelayRequestProblem() {
		try {
			final GatekeeperRelayRequest request = new GatekeeperRelayRequest(getTestMessageProducer(), getTestPublicKey(), "sessionId", "type", new Object());
			request.getGeneralRelayRequest();
		} catch (final Exception ex) {
			Assert.assertEquals("The request is not a general request.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetQoSRelayTestProposalRequestOk() {
		final QoSRelayTestProposalRequestDTO payload = new QoSRelayTestProposalRequestDTO();
		final GatekeeperRelayRequest request = new GatekeeperRelayRequest(getTestMessageProducer(), getTestPublicKey(), "sessionId", "type", payload);
		
		final QoSRelayTestProposalRequestDTO result = request.getQoSRelayTestProposalRequest();

		Assert.assertEquals(payload, result);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = DataNotFoundException.class)
	public void testGetQoSRelayTestProposalRequestProblem() {
		try {
			final GatekeeperRelayRequest request = new GatekeeperRelayRequest(getTestMessageProducer(), getTestPublicKey(), "sessionId", "type", new Object());
			request.getQoSRelayTestProposalRequest();
		} catch (final Exception ex) {
			Assert.assertEquals("The request is not a relay test proposal.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private MessageProducer getTestMessageProducer() {
		return new MessageProducer() {
			public void setTimeToLive(final long timeToLive) throws JMSException {}
			public void setPriority(final int defaultPriority) throws JMSException {}
			public void setDisableMessageTimestamp(final boolean value) throws JMSException {}
			public void setDisableMessageID(final boolean value) throws JMSException {}
			public void setDeliveryMode(final int deliveryMode) throws JMSException {}
			public void setDeliveryDelay(final long deliveryDelay) throws JMSException {}
			public void send(final Destination destination, final Message message, final int deliveryMode, final int priority, final long timeToLive, final CompletionListener completionListener) throws JMSException {}
			public void send(final Message message, final int deliveryMode, final int priority, final long timeToLive, final CompletionListener completionListener) throws JMSException {}
			public void send(final Destination destination, final Message message, final int deliveryMode, final int priority, final long timeToLive) throws JMSException {}
			public void send(final Message message, final int deliveryMode, final int priority, final long timeToLive) throws JMSException {}
			public void send(final Destination destination, final Message message, final CompletionListener completionListener) throws JMSException {}
			public void send(final Message message, final CompletionListener completionListener) throws JMSException {}
			public void send(final Destination destination, final Message message) throws JMSException {}
			public void send(final Message message) throws JMSException {}
			public long getTimeToLive() throws JMSException { return 0;	}
			public int getPriority() throws JMSException { return 0; }
			public boolean getDisableMessageTimestamp() throws JMSException { return false; }
			public boolean getDisableMessageID() throws JMSException { return false; }
			public Destination getDestination() throws JMSException { return null; }
			public int getDeliveryMode() throws JMSException { return 0; }
			public long getDeliveryDelay() throws JMSException { return 0; }
			public void close() throws JMSException {}
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