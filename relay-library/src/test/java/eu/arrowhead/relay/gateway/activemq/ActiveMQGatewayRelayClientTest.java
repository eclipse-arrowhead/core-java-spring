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

package eu.arrowhead.relay.gateway.activemq;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.Session;
import javax.jms.StreamMessage;
import javax.jms.TemporaryQueue;
import javax.jms.TemporaryTopic;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicSubscriber;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQMessageConsumer;
import org.apache.activemq.ActiveMQMessageProducer;
import org.apache.activemq.ActiveMQSession;
import org.apache.activemq.command.ActiveMQDestination;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.SSLProperties;
import eu.arrowhead.common.dto.internal.DecryptedMessageDTO;
import eu.arrowhead.common.exception.AuthException;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.relay.RelayCryptographer;
import eu.arrowhead.relay.activemq.RelayActiveMQConnectionFactory;
import eu.arrowhead.relay.gateway.ConsumerSideRelayInfo;
import eu.arrowhead.relay.gateway.ControlRelayInfo;
import eu.arrowhead.relay.gateway.ProviderSideRelayInfo;

public class ActiveMQGatewayRelayClientTest {

	//=================================================================================================
	// members

	private ActiveMQGatewayRelayClient testingObject;
	
	private RelayCryptographer cryptographer;
	private RelayActiveMQConnectionFactory connectionFactory;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Before
	public void setUp() {
		cryptographer = Mockito.mock(RelayCryptographer.class);
		connectionFactory = Mockito.mock(RelayActiveMQConnectionFactory.class);
		
		testingObject = new ActiveMQGatewayRelayClient("serverCN", getTestPrivateKey(), new SSLProperties());
		ReflectionTestUtils.setField(testingObject, "cryptographer", cryptographer);
		ReflectionTestUtils.setField(testingObject, "connectionFactory", connectionFactory);
	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorServerCommonNameNull() {
		try {
			new ActiveMQGatewayRelayClient(null, null, null);
		} catch (final Exception ex) {
			Assert.assertEquals("Common name is null or blank.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorServerCommonNameEmpty() {
		try {
			new ActiveMQGatewayRelayClient("", null, null);
		} catch (final Exception ex) {
			Assert.assertEquals("Common name is null or blank.", ex.getMessage());
			
			throw ex;
		}
	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorPrivateKeyNull() {
		try {
			new ActiveMQGatewayRelayClient("serverCN", null, null);
		} catch (final Exception ex) {
			Assert.assertEquals("Private key is null.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorSSLPropertiesNull() {
		try {
			new ActiveMQGatewayRelayClient("serverCN", getTestPrivateKey(), null);
		} catch (final Exception ex) {
			Assert.assertEquals("SSL properties object is null.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testCreateConnectionHostNull() throws Exception {
		try {
			testingObject.createConnection(null, 0, false);
		} catch (final Exception ex) {
			Assert.assertEquals("Host is null or blank.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testCreateConnectionHostEmpty() throws Exception {
		try {
			testingObject.createConnection("", 0, false);
		} catch (final Exception ex) {
			Assert.assertEquals("Host is null or blank.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testCreateConnectionPortTooLow() throws Exception {
		try {
			testingObject.createConnection("localhost", -1, false);
		} catch (final Exception ex) {
			Assert.assertEquals("Port is invalid.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testCreateConnectionPortTooHigh() throws Exception {
		try {
			testingObject.createConnection("localhost", 100000, false);
		} catch (final Exception ex) {
			Assert.assertEquals("Port is invalid.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = JMSException.class)
	public void testCreateConnectionException() throws Exception {
		final Connection conn = Mockito.mock(Connection.class);
		
		doNothing().when(connectionFactory).setHost("localhost");
		doNothing().when(connectionFactory).setPort(1234);
		when(connectionFactory.createConnection(false)).thenReturn(conn);
		doThrow(new JMSException("test")).when(conn).start();
		doNothing().when(conn).close();
		
		try {
			testingObject.createConnection("localhost", 1234, false);
		} catch (final Exception ex) {
			Assert.assertEquals("test", ex.getMessage());
			
			verify(connectionFactory, times(1)).setHost("localhost");
			verify(connectionFactory, times(1)).setPort(1234);
			verify(connectionFactory, times(1)).createConnection(false);
			verify(conn, times(1)).start();
			verify(conn, never()).createSession(anyBoolean(), anyInt());
			verify(conn, times(1)).close();
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCreateConnectionOk() throws Exception {
		final Connection conn = Mockito.mock(Connection.class);
		
		doNothing().when(connectionFactory).setHost("localhost");
		doNothing().when(connectionFactory).setPort(1234);
		when(connectionFactory.createConnection(false)).thenReturn(conn);
		doNothing().when(conn).start();
		when(conn.createSession(false, Session.AUTO_ACKNOWLEDGE)).thenReturn(getTestSession());
		
		final Session session = testingObject.createConnection("localhost", 1234, false);

		Assert.assertNotNull(session);
		
		verify(connectionFactory, times(1)).setHost("localhost");
		verify(connectionFactory, times(1)).setPort(1234);
		verify(connectionFactory, times(1)).createConnection(false);
		verify(conn, times(1)).start();
		verify(conn, times(1)).createSession(anyBoolean(), anyInt());
		verify(conn, never()).close();
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCloseConnectionOk() throws JMSException {
		final ActiveMQConnection conn = Mockito.mock(ActiveMQConnection.class);
		final ActiveMQSession session = Mockito.mock(ActiveMQSession.class);
		
		doNothing().when(session).close();
		when(session.getConnection()).thenReturn(conn);
		doNothing().when(conn).close();
		
		testingObject.closeConnection(session);
		
		verify(session, times(1)).close();
		verify(session, times(1)).getConnection();
		verify(conn, times(1)).close();
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCloseConnectionException() throws JMSException {
		final ActiveMQConnection conn = Mockito.mock(ActiveMQConnection.class);
		final ActiveMQSession session = Mockito.mock(ActiveMQSession.class);
		
		doNothing().when(session).close();
		when(session.getConnection()).thenReturn(conn);
		doThrow(JMSException.class).when(conn).close();
		
		testingObject.closeConnection(session);
		
		verify(session, times(1)).close();
		verify(session, times(1)).getConnection();
		verify(conn, times(1)).close();
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testIsConnectionClosed1() {
		final boolean result = testingObject.isConnectionClosed(getTestSession());
		Assert.assertTrue(result);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testIsConnectionClosed2() {
		final ActiveMQSession session = Mockito.mock(ActiveMQSession.class);
		
		when(session.isClosed()).thenReturn(false);
		
		final boolean result = testingObject.isConnectionClosed(session);
		
		Assert.assertFalse(result);
		verify(session, times(1)).isClosed();
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testInitializeProviderSideRelaySessionNull() throws Exception {
		try {
			testingObject.initializeProviderSideRelay(null, null);
		} catch (final Exception ex) {
			Assert.assertEquals("session is null.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testInitializeProviderSideRelayListenerNull() throws Exception {
		try {
			testingObject.initializeProviderSideRelay(getTestSession(), null);
		} catch (final Exception ex) {
			Assert.assertEquals("listener is null.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitializeProviderSideRelayOk() throws Exception {
		final Session session = Mockito.mock(Session.class);
		final MessageListener listener = Mockito.mock(MessageListener.class);
		final Queue reqQ = Mockito.mock(Queue.class);
		final Queue reqCQ = Mockito.mock(Queue.class);
		final Queue resQ = Mockito.mock(Queue.class);
		final Queue resCQ = Mockito.mock(Queue.class);
		final MessageConsumer consumer = Mockito.mock(MessageConsumer.class);
		final MessageConsumer controlConsumer = Mockito.mock(MessageConsumer.class);
		final MessageProducer producer = Mockito.mock(MessageProducer.class);
		final MessageProducer controlProducer = Mockito.mock(MessageProducer.class);
		
		when(session.createQueue(anyString())).thenReturn(reqQ, reqCQ, resQ, resCQ);
		when(session.createConsumer(reqQ)).thenReturn(consumer);
		doNothing().when(consumer).setMessageListener(listener);
		when(session.createConsumer(reqCQ)).thenReturn(controlConsumer);
		doNothing().when(controlConsumer).setMessageListener(listener);
		when(session.createProducer(resQ)).thenReturn(producer);
		when(session.createProducer(resCQ)).thenReturn(controlProducer);
		
		final ProviderSideRelayInfo result = testingObject.initializeProviderSideRelay(session, listener);
		
		Assert.assertEquals("serverCN", result.getPeerName());
		Assert.assertEquals(producer, result.getMessageSender());
		Assert.assertEquals(controlProducer, result.getControlMessageSender());
		
		verify(session, times(4)).createQueue(anyString());
		verify(session, times(1)).createConsumer(reqQ);
		verify(consumer, times(1)).setMessageListener(listener);
		verify(session, times(1)).createConsumer(reqCQ);
		verify(controlConsumer, times(1)).setMessageListener(listener);
		verify(session, times(1)).createProducer(resQ);
		verify(session, times(1)).createProducer(resCQ);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testInitializeConsumerSideRelaySessionNull() throws Exception {
		try {
			testingObject.initializeConsumerSideRelay(null, null, null, null);
		} catch (final Exception ex) {
			Assert.assertEquals("session is null.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testInitializeConsumerSideRelayListenerNull() throws Exception {
		try {
			testingObject.initializeConsumerSideRelay(getTestSession(), null, null, null);
		} catch (final Exception ex) {
			Assert.assertEquals("listener is null.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testInitializeConsumerSideRelayPeerNameNull() throws Exception {
		final MessageListener listener = Mockito.mock(MessageListener.class);
		
		try {
			testingObject.initializeConsumerSideRelay(getTestSession(), listener, null, null);
		} catch (final Exception ex) {
			Assert.assertEquals("peerName is null or blank.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testInitializeConsumerSideRelayPeerNameEmpty() throws Exception {
		final MessageListener listener = Mockito.mock(MessageListener.class);
		
		try {
			testingObject.initializeConsumerSideRelay(getTestSession(), listener, "", null);
		} catch (final Exception ex) {
			Assert.assertEquals("peerName is null or blank.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testInitializeConsumerSideRelayQueueIdNull() throws Exception {
		final MessageListener listener = Mockito.mock(MessageListener.class);
		
		try {
			testingObject.initializeConsumerSideRelay(getTestSession(), listener, "peerName", null);
		} catch (final Exception ex) {
			Assert.assertEquals("queueId is null or blank.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testInitializeConsumerSideRelayQueueIdEmpty() throws Exception {
		final MessageListener listener = Mockito.mock(MessageListener.class);
		
		try {
			testingObject.initializeConsumerSideRelay(getTestSession(), listener, "peerName", "");
		} catch (final Exception ex) {
			Assert.assertEquals("queueId is null or blank.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitializeConsumerSideRelayOk() throws Exception {
		final Session session = Mockito.mock(Session.class);
		final MessageListener listener = Mockito.mock(MessageListener.class);
		final Queue resQ = Mockito.mock(Queue.class);
		final Queue resCQ = Mockito.mock(Queue.class);
		final Queue reqQ = Mockito.mock(Queue.class);
		final Queue reqCQ = Mockito.mock(Queue.class);
		final MessageConsumer consumer = Mockito.mock(MessageConsumer.class);
		final MessageConsumer controlConsumer = Mockito.mock(MessageConsumer.class);
		final MessageProducer producer = Mockito.mock(MessageProducer.class);
		final MessageProducer controlProducer = Mockito.mock(MessageProducer.class);
		
		when(session.createQueue(anyString())).thenReturn(resQ, resCQ, reqQ, reqCQ);
		when(session.createConsumer(resQ)).thenReturn(consumer);
		doNothing().when(consumer).setMessageListener(listener);
		when(session.createConsumer(resCQ)).thenReturn(controlConsumer);
		doNothing().when(controlConsumer).setMessageListener(listener);
		when(session.createProducer(reqQ)).thenReturn(producer);
		when(session.createProducer(reqCQ)).thenReturn(controlProducer);
		
		final ConsumerSideRelayInfo result = testingObject.initializeConsumerSideRelay(session, listener, "peerName", "1234");
		
		Assert.assertEquals(producer, result.getMessageSender());
		Assert.assertEquals(controlProducer, result.getControlResponseMessageSender());
		
		verify(session, times(4)).createQueue(anyString());
		verify(session, times(1)).createConsumer(resQ);
		verify(consumer, times(1)).setMessageListener(listener);
		verify(session, times(1)).createConsumer(resCQ);
		verify(controlConsumer, times(1)).setMessageListener(listener);
		verify(session, times(1)).createProducer(reqQ);
		verify(session, times(1)).createProducer(reqCQ);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testInitializeControlRelaySessionNull() throws Exception {
		try {
			testingObject.initializeControlRelay(null, null, null);
		} catch (final Exception ex) {
			Assert.assertEquals("session is null.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testInitializeControlRelayPeerNameNull() throws Exception {
		try {
			testingObject.initializeControlRelay(getTestSession(), null, null);
		} catch (final Exception ex) {
			Assert.assertEquals("peerName is null or blank.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testInitializeControlRelayPeerNameEmpty() throws Exception {
		try {
			testingObject.initializeControlRelay(getTestSession(), "", null);
		} catch (final Exception ex) {
			Assert.assertEquals("peerName is null or blank.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testInitializeControlRelayQueueIdNull() throws Exception {
		try {
			testingObject.initializeControlRelay(getTestSession(), "peerName", null);
		} catch (final Exception ex) {
			Assert.assertEquals("queueId is null or blank.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testInitializeControlRelayQueueIdEmpty() throws Exception {
		try {
			testingObject.initializeControlRelay(getTestSession(), "peerName", "");
		} catch (final Exception ex) {
			Assert.assertEquals("queueId is null or blank.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitializeControlRelayOk() throws Exception {
		final Session session = Mockito.mock(Session.class);
		final Queue reqCQ = Mockito.mock(Queue.class);
		final Queue resCQ = Mockito.mock(Queue.class);
		final MessageProducer controlReqProducer = Mockito.mock(MessageProducer.class);
		final MessageProducer controlResProducer = Mockito.mock(MessageProducer.class);
		
		when(session.createQueue(anyString())).thenReturn(reqCQ, resCQ);
		when(session.createProducer(reqCQ)).thenReturn(controlReqProducer);
		when(session.createProducer(resCQ)).thenReturn(controlResProducer);
		
		final ControlRelayInfo result = testingObject.initializeControlRelay(session, "peerName", "1234");
		
		Assert.assertEquals(controlReqProducer, result.getControlRequestMessageSender());
		Assert.assertEquals(controlResProducer, result.getControlResponseMessageSender());
		
		verify(session, times(2)).createQueue(anyString());
		verify(session, times(1)).createProducer(reqCQ);
		verify(session, times(1)).createProducer(resCQ);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendBytesSessionNull() throws Exception {
		try {
			testingObject.sendBytes(null, null, null, null);
		} catch (final Exception ex) {
			Assert.assertEquals("session is null.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendBytesSenderNull() throws Exception {
		try {
			testingObject.sendBytes(getTestSession(), null, null, null);
		} catch (final Exception ex) {
			Assert.assertEquals("sender is null.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendBytesPeerPublicKeyNull() throws Exception {
		final MessageProducer sender = Mockito.mock(MessageProducer.class);
		
		try {
			testingObject.sendBytes(getTestSession(), sender, null, null);
		} catch (final Exception ex) {
			Assert.assertEquals("peerPublicKey is null.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendBytesBytesArrayNull() throws Exception {
		final MessageProducer sender = Mockito.mock(MessageProducer.class);
		
		try {
			testingObject.sendBytes(getTestSession(), sender, getTestPublicKey(), null);
		} catch (final Exception ex) {
			Assert.assertEquals("bytes array is null or empty.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendBytesBytesArrayEmpty() throws Exception {
		final MessageProducer sender = Mockito.mock(MessageProducer.class);
		
		try {
			testingObject.sendBytes(getTestSession(), sender, getTestPublicKey(), new byte[0]);
		} catch (final Exception ex) {
			Assert.assertEquals("bytes array is null or empty.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testSendBytesOk() throws Exception {
		final Session session = Mockito.mock(Session.class);
		final MessageProducer sender = Mockito.mock(MessageProducer.class);
		final byte[] bytes = new byte[] { 1, 2, 3, 4 };
		final PublicKey testPublicKey = getTestPublicKey();
		final TextMessage msg = Mockito.mock(TextMessage.class);
		
		when(cryptographer.encodeBytes(bytes, testPublicKey)).thenReturn("encoded");
		when(session.createTextMessage("encoded")).thenReturn(msg);
		doNothing().when(sender).send(msg);
		
		testingObject.sendBytes(session, sender, testPublicKey, bytes);
		
		verify(cryptographer, times(1)).encodeBytes(bytes, testPublicKey);
		verify(session, times(1)).createTextMessage("encoded");
		verify(sender, times(1)).send(msg);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testGetBytesFromMessageMessageNull() throws Exception {
		try {
			testingObject.getBytesFromMessage(null, null);
		} catch (final Exception ex) {
			Assert.assertEquals("Message is null.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testGetBytesFromMessagePeerPublicKeyNull() throws Exception {
		final Message msg = Mockito.mock(Message.class);
		
		try {
			testingObject.getBytesFromMessage(msg, null);
		} catch (final Exception ex) {
			Assert.assertEquals("peerPublicKey is null.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = JMSException.class)
	public void testGetBytesFromMessageInvalidMessageClass() throws Exception {
		final Message msg = Mockito.mock(Message.class);
		
		try {
			testingObject.getBytesFromMessage(msg, getTestPublicKey());
		} catch (final Exception ex) {
			Assert.assertTrue(ex.getMessage().startsWith("Invalid message class: "));
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = AuthException.class)
	public void testGetBytesFromMessageInvalidMessageType() throws Exception {
		final TextMessage msg = Mockito.mock(TextMessage.class);
		final PublicKey peerPublicKey = getTestPublicKey();
		final DecryptedMessageDTO decoded = new DecryptedMessageDTO();
		decoded.setMessageType("invalid");
		
		when(msg.getText()).thenReturn("encoded");
		when(cryptographer.decodeMessage("encoded", peerPublicKey)).thenReturn(decoded);
		
		try {
			testingObject.getBytesFromMessage(msg, peerPublicKey);
		} catch (final Exception ex) {
			Assert.assertEquals("Unauthorized message on queue.", ex.getMessage());
			
			verify(msg, times(1)).getText();
			verify(cryptographer, times(1)).decodeMessage("encoded", peerPublicKey);
			
			throw ex;
		}
	}
			
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetBytesFromMessageOk() throws Exception {
		final TextMessage msg = Mockito.mock(TextMessage.class);
		final PublicKey peerPublicKey = getTestPublicKey();
		final DecryptedMessageDTO decoded = new DecryptedMessageDTO();
		decoded.setMessageType(CoreCommonConstants.RELAY_MESSAGE_TYPE_RAW);
		decoded.setPayload("AQIDBA==");
		
		when(msg.getText()).thenReturn("encoded");
		when(cryptographer.decodeMessage("encoded", peerPublicKey)).thenReturn(decoded);
		
		final byte[] result = testingObject.getBytesFromMessage(msg, peerPublicKey);
		
		Assert.assertArrayEquals(new byte[] { 1, 2, 3, 4 }, result);
		
		verify(msg, times(1)).getText();
		verify(cryptographer, times(1)).decodeMessage("encoded", peerPublicKey);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendCloseControlMessageSessionNull() throws Exception {
		try {
			testingObject.sendCloseControlMessage(null, null, null);
		} catch (final Exception ex) {
			Assert.assertEquals("session is null.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendCloseControlMessageSenderNull() throws Exception {
		try {
			testingObject.sendCloseControlMessage(getTestSession(), null, null);
		} catch (final Exception ex) {
			Assert.assertEquals("sender is null.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendCloseControlMessageQueueIdNull() throws Exception {
		final MessageProducer sender = Mockito.mock(MessageProducer.class);
		
		try {
			testingObject.sendCloseControlMessage(getTestSession(), sender, null);
		} catch (final Exception ex) {
			Assert.assertEquals("queueId is null or blank.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendCloseControlMessageQueueIdEmpty() throws Exception {
		final MessageProducer sender = Mockito.mock(MessageProducer.class);
		
		try {
			testingObject.sendCloseControlMessage(getTestSession(), sender, "");
		} catch (final Exception ex) {
			Assert.assertEquals("queueId is null or blank.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = JMSException.class)
	public void testSendCloseControlMessageInvalidDestination() throws Exception {
		final MessageProducer sender = Mockito.mock(MessageProducer.class);
		final Topic topic = Mockito.mock(Topic.class);
		
		when(sender.getDestination()).thenReturn(topic);
		
		try {
			testingObject.sendCloseControlMessage(getTestSession(), sender, "1234");
		} catch (final Exception ex) {
			Assert.assertTrue(ex.getMessage().startsWith("Invalid destination class: "));
			
			verify(sender, times(2)).getDestination();
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = AuthException.class)
	public void testSendCloseControlMessageWrongDestination() throws Exception {
		final MessageProducer sender = Mockito.mock(MessageProducer.class);
		final Queue queue = Mockito.mock(Queue.class);
		
		when(sender.getDestination()).thenReturn(queue);
		when(queue.getQueueName()).thenReturn("wrong");
		
		try {
			testingObject.sendCloseControlMessage(getTestSession(), sender, "1234");
		} catch (final Exception ex) {
			Assert.assertEquals("Sender can't send control messages.", ex.getMessage());
			
			verify(sender, times(2)).getDestination();
			verify(queue, times(1)).getQueueName();
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testSendCloseControlMessageOk() throws Exception {
		final Session session = Mockito.mock(Session.class);
		final MessageProducer sender = Mockito.mock(MessageProducer.class);
		final Queue queue = Mockito.mock(Queue.class);
		final TextMessage msg = Mockito.mock(TextMessage.class);
		
		when(sender.getDestination()).thenReturn(queue);
		when(queue.getQueueName()).thenReturn("something-1234-CONTROL");
		when(session.createTextMessage("CLOSE 1234")).thenReturn(msg);
		doNothing().when(sender).send(msg);
		
		testingObject.sendCloseControlMessage(session, sender, "1234");

		verify(sender, times(2)).getDestination();
		verify(queue, times(1)).getQueueName();
		verify(session, times(1)).createTextMessage("CLOSE 1234");
		verify(sender, times(1)).send(msg);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testHandleCloseControlMessageMessageNull() throws Exception {
		try {
			testingObject.handleCloseControlMessage(null, null);
		} catch (final Exception ex) {
			Assert.assertEquals("Message is null.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = JMSException.class)
	public void testHandleCloseControlMessageInvalidMessageClass() throws Exception {
		final Message msg = Mockito.mock(Message.class);
		
		try {
			testingObject.handleCloseControlMessage(msg, getTestSession());
		} catch (final Exception ex) {
			Assert.assertTrue(ex.getMessage().startsWith("Invalid message class: "));
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testHandleCloseControlMessageInvalidCommand() throws Exception {
		final TextMessage msg = Mockito.mock(TextMessage.class);
		
		when(msg.getText()).thenReturn("WRONG");
		
		try {
			testingObject.handleCloseControlMessage(msg, getTestSession());
		} catch (final Exception ex) {
			Assert.assertTrue(ex.getMessage().startsWith("Invalid command: "));
			
			verify(msg, times(1)).getText();
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testHandleCloseControlMessageMissingQueueId() throws Exception {
		final TextMessage msg = Mockito.mock(TextMessage.class);
		
		when(msg.getText()).thenReturn("CLOSE ");
		
		try {
			testingObject.handleCloseControlMessage(msg, getTestSession());
		} catch (final Exception ex) {
			Assert.assertEquals("Missing queue id", ex.getMessage());
			
			verify(msg, times(1)).getText();
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = JMSException.class)
	public void testHandleCloseControlMessageInvalidDestination() throws Exception {
		final TextMessage msg = Mockito.mock(TextMessage.class);
		final Topic topic = Mockito.mock(Topic.class);
		
		when(msg.getText()).thenReturn("CLOSE 1234");
		when(msg.getJMSDestination()).thenReturn(topic);
		
		try {
			testingObject.handleCloseControlMessage(msg, getTestSession());
		} catch (final Exception ex) {
			Assert.assertTrue(ex.getMessage().startsWith("Invalid destination class: "));
			
			verify(msg, times(1)).getText();
			verify(msg, times(2)).getJMSDestination();
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = AuthException.class)
	public void testHandleCloseControlMessageWrongDestination() throws Exception {
		final TextMessage msg = Mockito.mock(TextMessage.class);
		final Queue queue = Mockito.mock(Queue.class);
		
		when(msg.getText()).thenReturn("CLOSE 1234");
		when(msg.getJMSDestination()).thenReturn(queue);
		when(queue.getQueueName()).thenReturn("something");
		
		try {
			testingObject.handleCloseControlMessage(msg, getTestSession());
		} catch (final Exception ex) {
			Assert.assertTrue(ex.getMessage().startsWith("Unauthorized close command: "));
			
			verify(msg, times(2)).getText();
			verify(msg, times(2)).getJMSDestination();
			verify(queue, times(1)).getQueueName();
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testHandleCloseControlMessageOk() throws Exception {
		final Session session = Mockito.mock(Session.class);
		final TextMessage msg = Mockito.mock(TextMessage.class);
		final Queue queue = Mockito.mock(Queue.class);
		
		when(msg.getText()).thenReturn("CLOSE 1234");
		when(msg.getJMSDestination()).thenReturn(queue);
		when(queue.getQueueName()).thenReturn("something-1234-CONTROL");
		doNothing().when(session).close();
		
		testingObject.handleCloseControlMessage(msg, session);
			
		verify(msg, times(1)).getText();
		verify(msg, times(2)).getJMSDestination();
		verify(queue, times(1)).getQueueName();
		verify(session, times(1)).close();
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendSwitchControlMessageSessionNull() throws Exception {
		try {
			testingObject.sendSwitchControlMessage(null, null, null);
		} catch (final Exception ex) {
			Assert.assertEquals("session is null.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendSwitchControlMessageSenderNull() throws Exception {
		try {
			testingObject.sendSwitchControlMessage(getTestSession(), null, null);
		} catch (final Exception ex) {
			Assert.assertEquals("sender is null.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendSwitchControlMessageQueueIdNull() throws Exception {
		final MessageProducer sender = Mockito.mock(MessageProducer.class);
		
		try {
			testingObject.sendSwitchControlMessage(getTestSession(), sender, null);
		} catch (final Exception ex) {
			Assert.assertEquals("queueId is null or blank.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendSwitchControlMessageQueueIdEmpty() throws Exception {
		final MessageProducer sender = Mockito.mock(MessageProducer.class);
		
		try {
			testingObject.sendSwitchControlMessage(getTestSession(), sender, "");
		} catch (final Exception ex) {
			Assert.assertEquals("queueId is null or blank.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = JMSException.class)
	public void testSendSwitchControlMessageInvalidDestination() throws Exception {
		final MessageProducer sender = Mockito.mock(MessageProducer.class);
		final Topic topic = Mockito.mock(Topic.class);
		
		when(sender.getDestination()).thenReturn(topic);
		
		try {
			testingObject.sendSwitchControlMessage(getTestSession(), sender, "1234");
		} catch (final Exception ex) {
			Assert.assertTrue(ex.getMessage().startsWith("Invalid destination class: "));
			
			verify(sender, times(2)).getDestination();
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = AuthException.class)
	public void testSendSwitchControlMessageWrongDestination() throws Exception {
		final MessageProducer sender = Mockito.mock(MessageProducer.class);
		final Queue queue = Mockito.mock(Queue.class);
		
		when(sender.getDestination()).thenReturn(queue);
		when(queue.getQueueName()).thenReturn("wrong");
		
		try {
			testingObject.sendSwitchControlMessage(getTestSession(), sender, "1234");
		} catch (final Exception ex) {
			Assert.assertEquals("Sender can't send control messages.", ex.getMessage());
			
			verify(sender, times(2)).getDestination();
			verify(queue, times(1)).getQueueName();
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testSendSwitchControlMessageOk() throws Exception {
		final Session session = Mockito.mock(Session.class);
		final MessageProducer sender = Mockito.mock(MessageProducer.class);
		final Queue queue = Mockito.mock(Queue.class);
		final TextMessage msg = Mockito.mock(TextMessage.class);
		
		when(sender.getDestination()).thenReturn(queue);
		when(queue.getQueueName()).thenReturn("something-1234-CONTROL");
		when(session.createTextMessage("SWITCH 1234")).thenReturn(msg);
		doNothing().when(sender).send(msg);
		
		testingObject.sendSwitchControlMessage(session, sender, "1234");

		verify(sender, times(2)).getDestination();
		verify(queue, times(1)).getQueueName();
		verify(session, times(1)).createTextMessage("SWITCH 1234");
		verify(sender, times(1)).send(msg);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testValidateSwitchControlMessageMessageNull() throws Exception {
		try {
			testingObject.validateSwitchControlMessage(null);
		} catch (final Exception ex) {
			Assert.assertEquals("Message is null.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = JMSException.class)
	public void testValidateSwitchControlMessageInvalidMessageClass() throws Exception {
		final Message msg = Mockito.mock(Message.class);
		
		try {
			testingObject.validateSwitchControlMessage(msg);
		} catch (final Exception ex) {
			Assert.assertTrue(ex.getMessage().startsWith("Invalid message class: "));
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testValidateSwitchControlMessageInvalidCommand() throws Exception {
		final TextMessage msg = Mockito.mock(TextMessage.class);
		
		when(msg.getText()).thenReturn("WRONG");
		
		try {
			testingObject.validateSwitchControlMessage(msg);
		} catch (final Exception ex) {
			Assert.assertTrue(ex.getMessage().startsWith("Invalid command: "));
			
			verify(msg, times(1)).getText();
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testValidateSwitchControlMessageMissingQueueId() throws Exception {
		final TextMessage msg = Mockito.mock(TextMessage.class);
		
		when(msg.getText()).thenReturn("SWITCH ");
		
		try {
			testingObject.validateSwitchControlMessage(msg);
		} catch (final Exception ex) {
			Assert.assertEquals("Missing queue id", ex.getMessage());
			
			verify(msg, times(1)).getText();
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = JMSException.class)
	public void testValidateSwitchControlMessageInvalidDestination() throws Exception {
		final TextMessage msg = Mockito.mock(TextMessage.class);
		final Topic topic = Mockito.mock(Topic.class);
		
		when(msg.getText()).thenReturn("SWITCH 1234");
		when(msg.getJMSDestination()).thenReturn(topic);
		
		try {
			testingObject.validateSwitchControlMessage(msg);
		} catch (final Exception ex) {
			Assert.assertTrue(ex.getMessage().startsWith("Invalid destination class: "));
			
			verify(msg, times(1)).getText();
			verify(msg, times(2)).getJMSDestination();
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = AuthException.class)
	public void testValidateSwitchControlMessageWrongDestination() throws Exception {
		final TextMessage msg = Mockito.mock(TextMessage.class);
		final Queue queue = Mockito.mock(Queue.class);
		
		when(msg.getText()).thenReturn("SWITCH 1234");
		when(msg.getJMSDestination()).thenReturn(queue);
		when(queue.getQueueName()).thenReturn("something");
		
		try {
			testingObject.validateSwitchControlMessage(msg);
		} catch (final Exception ex) {
			Assert.assertTrue(ex.getMessage().startsWith("Unauthorized switch command: "));
			
			verify(msg, times(2)).getText();
			verify(msg, times(2)).getJMSDestination();
			verify(queue, times(1)).getQueueName();
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testValidateSwitchControlMessageOk() throws Exception {
		final TextMessage msg = Mockito.mock(TextMessage.class);
		final Queue queue = Mockito.mock(Queue.class);
		
		when(msg.getText()).thenReturn("SWITCH 1234");
		when(msg.getJMSDestination()).thenReturn(queue);
		when(queue.getQueueName()).thenReturn("something-1234-CONTROL");
		
		testingObject.validateSwitchControlMessage(msg);
			
		verify(msg, times(1)).getText();
		verify(msg, times(2)).getJMSDestination();
		verify(queue, times(1)).getQueueName();
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testUnsubsrcibeFromQueuesOk() throws JMSException {
		final MessageConsumer consumer = Mockito.mock(ActiveMQMessageConsumer.class);
		final MessageConsumer consumerControl  = Mockito.mock(ActiveMQMessageConsumer.class);
		
		testingObject.unsubscribeFromQueues(consumer, consumerControl);
		
		verify(consumer, times(1)).close();
		verify(consumerControl, times(1)).close();
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = JMSException.class)
	public void testUnsubsrcibeFromQueuesInvalidClass1() throws JMSException {
		final MessageConsumer consumer = Mockito.mock(MessageConsumer.class);
		final MessageConsumer consumerControl  = Mockito.mock(ActiveMQMessageConsumer.class);
		
		try {
			testingObject.unsubscribeFromQueues(consumer, consumerControl);			
		} catch (final JMSException ex) {
			verify(consumer, never()).close();
			verify(consumerControl, never()).close();
			throw ex;
		}		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = JMSException.class)
	public void testUnsubsrcibeFromQueuesInvalidClass2() throws JMSException {
		final MessageConsumer consumer = Mockito.mock(ActiveMQMessageConsumer.class);
		final MessageConsumer consumerControl  = Mockito.mock(MessageConsumer.class);
		
		try {
			testingObject.unsubscribeFromQueues(consumer, consumerControl);			
		} catch (final JMSException ex) {
			verify(consumer, never()).close();
			verify(consumerControl, never()).close();
			throw ex;
		}		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testDestroyQueuesOk() throws JMSException {
		final ActiveMQSession session = Mockito.mock(ActiveMQSession.class);
		final ActiveMQMessageProducer producer = Mockito.mock(ActiveMQMessageProducer.class);
		final ActiveMQMessageProducer producerControl = Mockito.mock(ActiveMQMessageProducer.class);
		
		final ActiveMQConnection connection = Mockito.mock(ActiveMQConnection.class);
		when(session.getConnection()).thenReturn(connection);
		when(producer.getDestination()).thenReturn(getDestination());
		when(producerControl.getDestination()).thenReturn(getDestination());
		
		final boolean result = testingObject.destroyQueues(session, producer, producerControl);
		
		assertTrue(result);
		verify(connection, times(2)).destroyDestination(any(ActiveMQDestination.class));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testDestroyQueuesUnsuccessful1() throws JMSException {
		final ActiveMQSession session = Mockito.mock(ActiveMQSession.class);
		final ActiveMQMessageProducer producer = Mockito.mock(ActiveMQMessageProducer.class);
		final ActiveMQMessageProducer producerControl = Mockito.mock(ActiveMQMessageProducer.class);
		
		final ActiveMQConnection connection = Mockito.mock(ActiveMQConnection.class);
		when(session.getConnection()).thenReturn(connection);
		when(producer.getDestination()).thenReturn(getDestination());
		doThrow(new JMSException("test")).when(connection).destroyDestination(any(ActiveMQDestination.class));
		
		final boolean result = testingObject.destroyQueues(session, producer, producerControl);
		
		assertFalse(result);
		verify(connection, times(1)).destroyDestination(any(ActiveMQDestination.class));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testDestroyQueuesUnsuccessful2() throws JMSException {
		final ActiveMQSession session = Mockito.mock(ActiveMQSession.class);
		final ActiveMQMessageProducer producer = Mockito.mock(ActiveMQMessageProducer.class);
		final ActiveMQMessageProducer producerControl = Mockito.mock(ActiveMQMessageProducer.class);
		
		final ActiveMQConnection connection = Mockito.mock(ActiveMQConnection.class);
		when(session.getConnection()).thenReturn(connection);
		when(producer.getDestination()).thenReturn(getDestination());
		when(producerControl.getDestination()).thenReturn(getDestination());
		doNothing().doThrow(new JMSException("test")).when(connection).destroyDestination(any(ActiveMQDestination.class));
		
		final boolean result = testingObject.destroyQueues(session, producer, producerControl);
		
		assertFalse(result);
		verify(connection, times(2)).destroyDestination(any(ActiveMQDestination.class));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = JMSException.class)
	public void testDestroyQueuesInvalidClass1() throws JMSException {
		final Session session = Mockito.mock(Session.class);
		final MessageProducer producer = Mockito.mock(ActiveMQMessageProducer.class);
		final MessageProducer producerControl = Mockito.mock(ActiveMQMessageProducer.class);
		testingObject.destroyQueues(session, producer, producerControl);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = JMSException.class)
	public void testDestroyQueuesInvalidClass2() throws JMSException {
		final Session session = Mockito.mock(ActiveMQSession.class);
		final MessageProducer producer = Mockito.mock(MessageProducer.class);
		final MessageProducer producerControl = Mockito.mock(ActiveMQMessageProducer.class);
		testingObject.destroyQueues(session, producer, producerControl);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = JMSException.class)
	public void testDestroyQueuesInvalidClass3() throws JMSException {
		final Session session = Mockito.mock(ActiveMQSession.class);
		final MessageProducer producer = Mockito.mock(ActiveMQMessageProducer.class);
		final MessageProducer producerControl = Mockito.mock(MessageProducer.class);
		testingObject.destroyQueues(session, producer, producerControl);
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("serial")
	private PrivateKey getTestPrivateKey() {
		return new PrivateKey() {
			public String getFormat() { return null; }
			public byte[] getEncoded() { return null; }
			public String getAlgorithm() { return null; }
		};
	}
	
	//-------------------------------------------------------------------------------------------------
	private Session getTestSession() {
		return new Session() {
			public void close() throws JMSException {}
			public Queue createQueue(final String queueName) throws JMSException { return null;	}
			public Topic createTopic(final String topicName) throws JMSException { return null;	}
			public MessageConsumer createConsumer(final Destination destination) throws JMSException { return null; }
			public MessageProducer createProducer(final Destination destination) throws JMSException { return null;	}
			public TextMessage createTextMessage(final String text) throws JMSException { return null; }
			public BytesMessage createBytesMessage() throws JMSException { return null; }
			public MapMessage createMapMessage() throws JMSException { return null; }
			public Message createMessage() throws JMSException { return null; }
			public ObjectMessage createObjectMessage() throws JMSException { return null; }
			public ObjectMessage createObjectMessage(final Serializable object) throws JMSException { return null; }
			public StreamMessage createStreamMessage() throws JMSException { return null; }
			public TextMessage createTextMessage() throws JMSException { return null; }
			public boolean getTransacted() throws JMSException { return false; 	}
			public int getAcknowledgeMode() throws JMSException { return 0; }
			public void commit() throws JMSException {}
			public void rollback() throws JMSException {}
			public void recover() throws JMSException {}
			public MessageListener getMessageListener() throws JMSException { return null; }
			public void setMessageListener(final MessageListener listener) throws JMSException {}
			public void run() {}
			public MessageConsumer createConsumer(final Destination destination, final String messageSelector) throws JMSException { return null; }
			public MessageConsumer createConsumer(final Destination destination, final String messageSelector, final boolean noLocal) throws JMSException { return null; }
			public MessageConsumer createSharedConsumer(final Topic topic, final String sharedSubscriptionName) throws JMSException { return null; }
			public MessageConsumer createSharedConsumer(final Topic topic, final String sharedSubscriptionName, final String messageSelector) throws JMSException { return null; }
			public TopicSubscriber createDurableSubscriber(final Topic topic, final String name) throws JMSException { return null; }
			public TopicSubscriber createDurableSubscriber(final Topic topic, final String name, final String messageSelector, final boolean noLocal) throws JMSException { return null; }
			public MessageConsumer createDurableConsumer(final Topic topic, final String name) throws JMSException { return null; }
			public MessageConsumer createDurableConsumer(final Topic topic, final String name, final String messageSelector, final boolean noLocal) throws JMSException { return null; }
			public MessageConsumer createSharedDurableConsumer(final Topic topic, final String name) throws JMSException { return null; }
			public MessageConsumer createSharedDurableConsumer(final Topic topic, final String name, final String messageSelector) throws JMSException { return null;	}
			public QueueBrowser createBrowser(final Queue queue) throws JMSException { return null; }
			public QueueBrowser createBrowser(final Queue queue, final String messageSelector) throws JMSException { return null; }
			public TemporaryQueue createTemporaryQueue() throws JMSException { return null; }
			public TemporaryTopic createTemporaryTopic() throws JMSException { return null;	}
			public void unsubscribe(final String name) throws JMSException {}
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
	
	//-------------------------------------------------------------------------------------------------
	private ActiveMQDestination getDestination() {
		return new ActiveMQDestination() {
			public byte getDataStructureType() { return 0; }
			protected String getQualifiedPrefix() { return null; }
			public byte getDestinationType() { return 0; }
		}; 
	}
}