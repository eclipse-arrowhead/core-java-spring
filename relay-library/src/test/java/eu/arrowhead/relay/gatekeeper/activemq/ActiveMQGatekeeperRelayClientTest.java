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

package eu.arrowhead.relay.gatekeeper.activemq;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.apache.activemq.ActiveMQSession;
import org.apache.activemq.advisory.DestinationSource;
import org.apache.activemq.command.ActiveMQQueue;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.test.util.ReflectionTestUtils;

import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.SSLProperties;
import eu.arrowhead.common.dto.internal.DecryptedMessageDTO;
import eu.arrowhead.common.dto.internal.GSDPollRequestDTO;
import eu.arrowhead.common.dto.internal.GSDPollResponseDTO;
import eu.arrowhead.common.dto.internal.GeneralAdvertisementMessageDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.AuthException;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.relay.RelayCryptographer;
import eu.arrowhead.relay.activemq.RelayActiveMQConnectionFactory;
import eu.arrowhead.relay.gatekeeper.GatekeeperRelayRequest;
import eu.arrowhead.relay.gatekeeper.GatekeeperRelayResponse;
import eu.arrowhead.relay.gatekeeper.GeneralAdvertisementResult;

public class ActiveMQGatekeeperRelayClientTest {

	//=================================================================================================
	// members

	private static final String aPublicKeyStr = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA1aaeuv1I4bF5dxMIvUvLMxjRn309kdJewIIH08DfL17/LSssD70ZaLz0yxNfbPPQpFK8LMK+HQHDiGZH5yp4qJDuEgfmUrqWibnBIBc/K3Ob45lQy0zdFVtFsVJYBFVymQwgxJT6th0hI3RGLbCJMzbmpDzT7g0IDsN+64tMyi08ZCPrqk99uzYgioSSWNb9bhG2Z9646b3oiY5utQWRhP/2z/t6vVJHtRYeyaXPl6Z2M/5KnjpSvpSeZQhNrw+Is1DEE5DHiEjfQFWrLwDOqPKDrvmFyIlJ7P7OCMax6dIlSB7GEQSSP+j4eIxDWgjm+Pv/c02UVDc0x3xX/UGtNwIDAQAB";
	private static final String topicName ="General-M5QTZXM9G9AnpPHWT6WennWu";

	private ActiveMQGatekeeperRelayClient testingObject;
	
	private PublicKey publicKey;
	private RelayCryptographer cryptographer;
	private RelayActiveMQConnectionFactory connectionFactory;
	final Map<ActiveMQSession,List<ActiveMQQueue>> staleQueues = new HashMap<>();
	private final Set<Connection> staleConnections = new HashSet<>();
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Before
	public void setUp() {
		publicKey = Mockito.mock(PublicKey.class);
		cryptographer = Mockito.mock(RelayCryptographer.class);
		connectionFactory = Mockito.mock(RelayActiveMQConnectionFactory.class);
		
		testingObject = new ActiveMQGatekeeperRelayClient("serverCN", publicKey, getTestPrivateKey(), new SSLProperties(), 60000);
		ReflectionTestUtils.setField(testingObject, "cryptographer", cryptographer);
		ReflectionTestUtils.setField(testingObject, "connectionFactory", connectionFactory);
		ReflectionTestUtils.setField(testingObject, "staleQueues", staleQueues);
		ReflectionTestUtils.setField(testingObject, "staleConnections", staleConnections);
	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorServerCommonNameNull() {
		try {
			new ActiveMQGatekeeperRelayClient(null, null, null, null, 0);
		} catch (final Exception ex) {
			Assert.assertEquals("Common name is null or blank.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorServerCommonNameEmpty() {
		try {
			new ActiveMQGatekeeperRelayClient("", null, null, null, 0);
		} catch (final Exception ex) {
			Assert.assertEquals("Common name is null or blank.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorPublicKeyNull() {
		try {
			new ActiveMQGatekeeperRelayClient("serverCN", null, null, null, 0);
		} catch (final Exception ex) {
			Assert.assertEquals("Public key is null.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorPrivateKeyNull() {
		try {
			new ActiveMQGatekeeperRelayClient("serverCN", getTestPublicKey(), null, null, 0);
		} catch (final Exception ex) {
			Assert.assertEquals("Private key is null.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorSSLPropertiesNull() {
		try {
			new ActiveMQGatekeeperRelayClient("serverCN", getTestPublicKey(), getTestPrivateKey(), null, 0);
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
	public void testCloseConnectionOk1() throws JMSException {
		final ActiveMQConnection conn = Mockito.mock(ActiveMQConnection.class);
		final ActiveMQSession session = Mockito.mock(ActiveMQSession.class);
		
		doNothing().when(session).close();
		when(session.getConnection()).thenReturn(conn);
		doNothing().when(conn).close();
		
		testingObject.closeConnection(session);
		
		assertFalse(staleConnections.contains(conn));
		verify(session, times(1)).close();
		verify(session, times(1)).getConnection();
		verify(conn, times(1)).close();
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCloseConnectionOk2() throws JMSException {
		final ActiveMQConnection conn = Mockito.mock(ActiveMQConnection.class);
		final ActiveMQSession session = Mockito.mock(ActiveMQSession.class);
		
		staleQueues.put(session, new ArrayList<>());
		
		doNothing().when(session).close();
		when(session.getConnection()).thenReturn(conn);
		doNothing().when(conn).close();
		
		testingObject.closeConnection(session);
		
		assertTrue(staleConnections.contains(conn));
		verify(session, never()).close();
		verify(session, times(1)).getConnection();
		verify(conn, never()).close();
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
	public void testSubscribeGeneralAdvertisementTopicSessionNull() throws Exception {
		try {
			testingObject.subscribeGeneralAdvertisementTopic(null);
		} catch (final Exception ex) {
			Assert.assertEquals("session is null.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testSubscribeGeneralAdvertisementTopicOk() throws JMSException {
		final Topic topic = Mockito.mock(Topic.class);
		final Session session = Mockito.mock(Session.class);
		final MessageConsumer consumer = Mockito.mock(MessageConsumer.class);
		
		when(session.createTopic(topicName)).thenReturn(topic);
		when(session.createConsumer(topic)).thenReturn(consumer);
		
		final MessageConsumer result = testingObject.subscribeGeneralAdvertisementTopic(session);
		
		Assert.assertEquals(consumer, result);
		
		verify(session, times(1)).createTopic(topicName);
		verify(session, times(1)).createConsumer(topic);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testGetGeneralAdvertisementMessageMessageNull() throws Exception {
		try {
			testingObject.getGeneralAdvertisementMessage(null);
		} catch (final Exception ex) {
			Assert.assertEquals("message is null.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = JMSException.class)
	public void testGetGeneralAdvertisementMessageNotText() throws Exception {
		final Message msg = Mockito.mock(Message.class);
		
		try {
			testingObject.getGeneralAdvertisementMessage(msg);
		} catch (final Exception ex) {
			Assert.assertTrue(ex.getMessage().startsWith("Invalid message class: "));
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetGeneralAdvertisementMessageNullText() throws Exception {
		final TextMessage msg = Mockito.mock(TextMessage.class);
		
		when(msg.getText()).thenReturn(null);
		
		final GeneralAdvertisementMessageDTO result = testingObject.getGeneralAdvertisementMessage(msg);
		
		Assert.assertNull(result);
		
		verify(msg, times(1)).getText();
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetGeneralAdvertisementMessageEmptyText() throws Exception {
		final TextMessage msg = Mockito.mock(TextMessage.class);
		
		when(msg.getText()).thenReturn("");
		
		final GeneralAdvertisementMessageDTO result = testingObject.getGeneralAdvertisementMessage(msg);
		
		Assert.assertNull(result);
		
		verify(msg, times(1)).getText();
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetGeneralAdvertisementMessageForSomeoneElse() throws Exception {
		final String text = "{ \"recipientCN\": \"other\" }";
		final TextMessage msg = Mockito.mock(TextMessage.class);
		
		when(msg.getText()).thenReturn(text);
		
		final GeneralAdvertisementMessageDTO result = testingObject.getGeneralAdvertisementMessage(msg);
		
		Assert.assertNull(result);
		
		verify(msg, times(1)).getText();
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetGeneralAdvertisementMessageOk() throws Exception {
		final String text = "{ \"recipientCN\": \"serverCN\", \"sessionId\": \"1234\", \"senderPublicKey\": \"key\" }";
		final TextMessage msg = Mockito.mock(TextMessage.class);
		final DecryptedMessageDTO decrypted = new DecryptedMessageDTO();
		decrypted.setSessionId("4321");
		
		
		when(msg.getText()).thenReturn(text);
		when(cryptographer.decodeMessage("1234", "key")).thenReturn(decrypted);
		
		final GeneralAdvertisementMessageDTO result = testingObject.getGeneralAdvertisementMessage(msg);
		
		Assert.assertEquals("4321", result.getSessionId());
		Assert.assertEquals("serverCN", result.getRecipientCN());
		Assert.assertEquals("key", result.getSenderPublicKey());
		
		verify(msg, times(1)).getText();
		verify(cryptographer, times(1)).decodeMessage("1234", "key");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendAcknowledgementAndReturnRequestSessionNull() throws Exception {
		try {
			testingObject.sendAcknowledgementAndReturnRequest(null, null);
		} catch (final Exception ex) {
			Assert.assertEquals("Session is null.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendAcknowledgementAndReturnRequestMessageNull() throws Exception {
		try {
			testingObject.sendAcknowledgementAndReturnRequest(getTestSession(), null);
		} catch (final Exception ex) {
			Assert.assertEquals("Message is null.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendAcknowledgementAndReturnRequestPublicKeyNull() throws Exception {
		try {
			testingObject.sendAcknowledgementAndReturnRequest(getTestSession(), new GeneralAdvertisementMessageDTO());
		} catch (final Exception ex) {
			Assert.assertEquals("Public key is null or blank.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendAcknowledgementAndReturnRequestPublicKeyEmpty() throws Exception {
		final GeneralAdvertisementMessageDTO msg = new GeneralAdvertisementMessageDTO();
		msg.setSenderPublicKey("");
		
		try {
			testingObject.sendAcknowledgementAndReturnRequest(getTestSession(), msg);
		} catch (final Exception ex) {
			Assert.assertEquals("Public key is null or blank.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendAcknowledgementAndReturnRequestSessionIdNull() throws Exception {
		final GeneralAdvertisementMessageDTO msg = new GeneralAdvertisementMessageDTO();
		msg.setSenderPublicKey("1234");
		
		try {
			testingObject.sendAcknowledgementAndReturnRequest(getTestSession(), msg);
		} catch (final Exception ex) {
			Assert.assertEquals("Session id is null or blank.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendAcknowledgementAndReturnRequestSessionIdEmpty() throws Exception {
		final GeneralAdvertisementMessageDTO msg = new GeneralAdvertisementMessageDTO();
		msg.setSenderPublicKey("1234");
		msg.setSessionId("");
		
		try {
			testingObject.sendAcknowledgementAndReturnRequest(getTestSession(), msg);
		} catch (final Exception ex) {
			Assert.assertEquals("Session id is null or blank.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = JMSException.class)
	public void testSendAcknowledgementAndReturnRequestException1() throws Exception {
		final GeneralAdvertisementMessageDTO msg = new GeneralAdvertisementMessageDTO();
		msg.setSenderPublicKey(aPublicKeyStr);
		msg.setSessionId("1234");
		
		final Session session = Mockito.mock(Session.class);
		final Queue requestQueue = Mockito.mock(Queue.class);
		final MessageConsumer consumer = Mockito.mock(MessageConsumer.class);
		
		when(session.createQueue(anyString())).thenReturn(requestQueue).thenThrow(new JMSException("test"));
		when(session.createConsumer(requestQueue)).thenReturn(consumer);
		doNothing().when(consumer).close();
		
		try {
			testingObject.sendAcknowledgementAndReturnRequest(session, msg);
		} catch (final Exception ex) {
			Assert.assertEquals("test", ex.getMessage());
			
			verify(session, times(2)).createQueue(anyString());
			verify(session, times(1)).createConsumer(requestQueue);
			verify(consumer, times(1)).close();
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testSendAcknowledgementAndReturnRequestException2() throws Exception {
		final GeneralAdvertisementMessageDTO msg = new GeneralAdvertisementMessageDTO();
		msg.setSenderPublicKey(aPublicKeyStr);
		msg.setSessionId("1234");
		
		final Session session = Mockito.mock(Session.class);
		final Queue requestQueue = Mockito.mock(Queue.class);
		final Queue responseQueue = Mockito.mock(Queue.class);
		final MessageConsumer consumer = Mockito.mock(MessageConsumer.class);
		final MessageProducer producer = Mockito.mock(MessageProducer.class);
		
		when(session.createQueue(anyString())).thenReturn(requestQueue, responseQueue);
		when(session.createConsumer(requestQueue)).thenReturn(consumer);
		when(session.createProducer(responseQueue)).thenReturn(producer);
		when(cryptographer.encodeRelayMessage(eq("ack"), eq("1234"), isNull(), any(PublicKey.class))).thenThrow(new ArrowheadException("test"));
		doNothing().when(consumer).close();
		doNothing().when(producer).close();
		
		try {
			testingObject.sendAcknowledgementAndReturnRequest(session, msg);
		} catch (final Exception ex) {
			Assert.assertEquals("test", ex.getMessage());
			
			verify(session, times(2)).createQueue(anyString());
			verify(session, times(1)).createConsumer(requestQueue);
			verify(session, times(1)).createProducer(responseQueue);
			verify(cryptographer, times(1)).encodeRelayMessage(eq("ack"), eq("1234"), isNull(), any(PublicKey.class));
			verify(consumer, times(1)).close();
			verify(producer, times(1)).close();
			verify(session, never()).createTextMessage(anyString());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testSendAcknowledgementAndReturnRequestNoRequestInTime() throws Exception {
		final GeneralAdvertisementMessageDTO msg = new GeneralAdvertisementMessageDTO();
		msg.setSenderPublicKey(aPublicKeyStr);
		msg.setSessionId("1234");
		
		final Session session = Mockito.mock(Session.class);
		final Queue requestQueue = Mockito.mock(Queue.class);
		final Queue responseQueue = Mockito.mock(Queue.class);
		final MessageConsumer consumer = Mockito.mock(MessageConsumer.class);
		final MessageProducer producer = Mockito.mock(MessageProducer.class);
		final TextMessage ackMsg = Mockito.mock(TextMessage.class);
		
		when(session.createQueue(anyString())).thenReturn(requestQueue, responseQueue);
		when(session.createConsumer(requestQueue)).thenReturn(consumer);
		when(session.createProducer(responseQueue)).thenReturn(producer);
		when(cryptographer.encodeRelayMessage(eq("ack"), eq("1234"), isNull(), any(PublicKey.class))).thenReturn("encoded");
		when(session.createTextMessage("encoded")).thenReturn(ackMsg);
		doNothing().when(producer).send(ackMsg);
		when(consumer.receive(anyLong())).thenReturn(null);
		doNothing().when(consumer).close();
		doNothing().when(producer).close();
		
		final GatekeeperRelayRequest result = testingObject.sendAcknowledgementAndReturnRequest(session, msg);
		
		Assert.assertNull(result);
		
		verify(session, times(2)).createQueue(anyString());
		verify(session, times(1)).createConsumer(requestQueue);
		verify(session, times(1)).createProducer(responseQueue);
		verify(cryptographer, times(1)).encodeRelayMessage(eq("ack"), eq("1234"), isNull(), any(PublicKey.class));
		verify(session, times(1)).createTextMessage("encoded");
		verify(producer, times(1)).send(ackMsg);
		verify(consumer, times(1)).receive(anyLong());
		verify(consumer, times(1)).close();
		verify(producer, times(1)).close();
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = JMSException.class)
	public void testSendAcknowledgementAndReturnRequestInvalidMessageType() throws Exception {
		final GeneralAdvertisementMessageDTO msg = new GeneralAdvertisementMessageDTO();
		msg.setSenderPublicKey(aPublicKeyStr);
		msg.setSessionId("1234");
		
		final Session session = Mockito.mock(Session.class);
		final Queue requestQueue = Mockito.mock(Queue.class);
		final Queue responseQueue = Mockito.mock(Queue.class);
		final MessageConsumer consumer = Mockito.mock(MessageConsumer.class);
		final MessageProducer producer = Mockito.mock(MessageProducer.class);
		final TextMessage ackMsg = Mockito.mock(TextMessage.class);
		final Message request = Mockito.mock(Message.class);
		
		when(session.createQueue(anyString())).thenReturn(requestQueue, responseQueue);
		when(session.createConsumer(requestQueue)).thenReturn(consumer);
		when(session.createProducer(responseQueue)).thenReturn(producer);
		when(cryptographer.encodeRelayMessage(eq("ack"), eq("1234"), isNull(), any(PublicKey.class))).thenReturn("encoded");
		when(session.createTextMessage("encoded")).thenReturn(ackMsg);
		doNothing().when(producer).send(ackMsg);
		when(consumer.receive(anyLong())).thenReturn(request);
		doNothing().when(consumer).close();
		doNothing().when(producer).close();
		
		try {
			testingObject.sendAcknowledgementAndReturnRequest(session, msg);
		} catch (final Exception ex) {
			Assert.assertTrue(ex.getMessage().startsWith("Invalid message class: "));
			
			verify(session, times(2)).createQueue(anyString());
			verify(session, times(1)).createConsumer(requestQueue);
			verify(session, times(1)).createProducer(responseQueue);
			verify(cryptographer, times(1)).encodeRelayMessage(eq("ack"), eq("1234"), isNull(), any(PublicKey.class));
			verify(session, times(1)).createTextMessage("encoded");
			verify(producer, times(1)).send(ackMsg);
			verify(consumer, times(1)).receive(anyLong());
			verify(consumer, times(1)).close();
			verify(producer, times(1)).close();
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = AuthException.class)
	public void testSendAcknowledgementAndReturnRequestInvalidRequestType() throws Exception {
		final GeneralAdvertisementMessageDTO msg = new GeneralAdvertisementMessageDTO();
		msg.setSenderPublicKey(aPublicKeyStr);
		msg.setSessionId("1234");
		
		final Session session = Mockito.mock(Session.class);
		final Queue requestQueue = Mockito.mock(Queue.class);
		final Queue responseQueue = Mockito.mock(Queue.class);
		final MessageConsumer consumer = Mockito.mock(MessageConsumer.class);
		final MessageProducer producer = Mockito.mock(MessageProducer.class);
		final TextMessage ackMsg = Mockito.mock(TextMessage.class);
		final TextMessage request = Mockito.mock(TextMessage.class);
		
		final DecryptedMessageDTO decrypted = new DecryptedMessageDTO();
		decrypted.setMessageType("invalid");
		
		when(session.createQueue(anyString())).thenReturn(requestQueue, responseQueue);
		when(session.createConsumer(requestQueue)).thenReturn(consumer);
		when(session.createProducer(responseQueue)).thenReturn(producer);
		when(cryptographer.encodeRelayMessage(eq("ack"), eq("1234"), isNull(), any(PublicKey.class))).thenReturn("encoded");
		when(session.createTextMessage("encoded")).thenReturn(ackMsg);
		doNothing().when(producer).send(ackMsg);
		when(consumer.receive(anyLong())).thenReturn(request);
		when(request.getText()).thenReturn("abcd");
		when(cryptographer.decodeMessage(eq("abcd"), any(PublicKey.class))).thenReturn(decrypted);
		doNothing().when(consumer).close();
		doNothing().when(producer).close();
		
		try {
			testingObject.sendAcknowledgementAndReturnRequest(session, msg);
		} catch (final Exception ex) {
			Assert.assertEquals("Unauthorized message on queue.", ex.getMessage());
			
			verify(session, times(2)).createQueue(anyString());
			verify(session, times(1)).createConsumer(requestQueue);
			verify(session, times(1)).createProducer(responseQueue);
			verify(cryptographer, times(1)).encodeRelayMessage(eq("ack"), eq("1234"), isNull(), any(PublicKey.class));
			verify(session, times(1)).createTextMessage("encoded");
			verify(producer, times(1)).send(ackMsg);
			verify(consumer, times(1)).receive(anyLong());
			verify(request, times(1)).getText();
			verify(cryptographer, times(1)).decodeMessage(eq("abcd"), any(PublicKey.class));
			verify(consumer, times(1)).close();
			verify(producer, times(1)).close();
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = AuthException.class)
	public void testSendAcknowledgementAndReturnRequestInvalidSessionId() throws Exception {
		final GeneralAdvertisementMessageDTO msg = new GeneralAdvertisementMessageDTO();
		msg.setSenderPublicKey(aPublicKeyStr);
		msg.setSessionId("1234");
		
		final Session session = Mockito.mock(Session.class);
		final Queue requestQueue = Mockito.mock(Queue.class);
		final Queue responseQueue = Mockito.mock(Queue.class);
		final MessageConsumer consumer = Mockito.mock(MessageConsumer.class);
		final MessageProducer producer = Mockito.mock(MessageProducer.class);
		final TextMessage ackMsg = Mockito.mock(TextMessage.class);
		final TextMessage request = Mockito.mock(TextMessage.class);
		
		final DecryptedMessageDTO decrypted = new DecryptedMessageDTO();
		decrypted.setMessageType(CoreCommonConstants.RELAY_MESSAGE_TYPE_GSD_POLL);
		decrypted.setSessionId("5678");
		
		when(session.createQueue(anyString())).thenReturn(requestQueue, responseQueue);
		when(session.createConsumer(requestQueue)).thenReturn(consumer);
		when(session.createProducer(responseQueue)).thenReturn(producer);
		when(cryptographer.encodeRelayMessage(eq("ack"), eq("1234"), isNull(), any(PublicKey.class))).thenReturn("encoded");
		when(session.createTextMessage("encoded")).thenReturn(ackMsg);
		doNothing().when(producer).send(ackMsg);
		when(consumer.receive(anyLong())).thenReturn(request);
		when(request.getText()).thenReturn("abcd");
		when(cryptographer.decodeMessage(eq("abcd"), any(PublicKey.class))).thenReturn(decrypted);
		doNothing().when(consumer).close();
		doNothing().when(producer).close();
		
		try {
			testingObject.sendAcknowledgementAndReturnRequest(session, msg);
		} catch (final Exception ex) {
			Assert.assertEquals("Unauthorized message on queue.", ex.getMessage());
			
			verify(session, times(2)).createQueue(anyString());
			verify(session, times(1)).createConsumer(requestQueue);
			verify(session, times(1)).createProducer(responseQueue);
			verify(cryptographer, times(1)).encodeRelayMessage(eq("ack"), eq("1234"), isNull(), any(PublicKey.class));
			verify(session, times(1)).createTextMessage("encoded");
			verify(producer, times(1)).send(ackMsg);
			verify(consumer, times(1)).receive(anyLong());
			verify(request, times(1)).getText();
			verify(cryptographer, times(1)).decodeMessage(eq("abcd"), any(PublicKey.class));
			verify(consumer, times(1)).close();
			verify(producer, times(1)).close();
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testSendAcknowledgementAndReturnRequestInvalidPayload() throws Exception {
		final GeneralAdvertisementMessageDTO msg = new GeneralAdvertisementMessageDTO();
		msg.setSenderPublicKey(aPublicKeyStr);
		msg.setSessionId("1234");
		
		final Session session = Mockito.mock(Session.class);
		final Queue requestQueue = Mockito.mock(Queue.class);
		final Queue responseQueue = Mockito.mock(Queue.class);
		final MessageConsumer consumer = Mockito.mock(MessageConsumer.class);
		final MessageProducer producer = Mockito.mock(MessageProducer.class);
		final TextMessage ackMsg = Mockito.mock(TextMessage.class);
		final TextMessage request = Mockito.mock(TextMessage.class);
		
		final DecryptedMessageDTO decrypted = new DecryptedMessageDTO();
		decrypted.setMessageType(CoreCommonConstants.RELAY_MESSAGE_TYPE_GSD_POLL);
		decrypted.setSessionId("1234");
		decrypted.setPayload("invalid");
		
		when(session.createQueue(anyString())).thenReturn(requestQueue, responseQueue);
		when(session.createConsumer(requestQueue)).thenReturn(consumer);
		when(session.createProducer(responseQueue)).thenReturn(producer);
		when(cryptographer.encodeRelayMessage(eq("ack"), eq("1234"), isNull(), any(PublicKey.class))).thenReturn("encoded");
		when(session.createTextMessage("encoded")).thenReturn(ackMsg);
		doNothing().when(producer).send(ackMsg);
		when(consumer.receive(anyLong())).thenReturn(request);
		when(request.getText()).thenReturn("abcd");
		when(cryptographer.decodeMessage(eq("abcd"), any(PublicKey.class))).thenReturn(decrypted);
		doNothing().when(consumer).close();
		doNothing().when(producer).close();
		
		try {
			testingObject.sendAcknowledgementAndReturnRequest(session, msg);
		} catch (final Exception ex) {
			Assert.assertTrue(ex.getMessage().startsWith("Can't convert payload with type "));
			
			verify(session, times(2)).createQueue(anyString());
			verify(session, times(1)).createConsumer(requestQueue);
			verify(session, times(1)).createProducer(responseQueue);
			verify(cryptographer, times(1)).encodeRelayMessage(eq("ack"), eq("1234"), isNull(), any(PublicKey.class));
			verify(session, times(1)).createTextMessage("encoded");
			verify(producer, times(1)).send(ackMsg);
			verify(consumer, times(1)).receive(anyLong());
			verify(request, times(1)).getText();
			verify(cryptographer, times(1)).decodeMessage(eq("abcd"), any(PublicKey.class));
			verify(consumer, times(1)).close();
			verify(producer, times(1)).close();
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testSendAcknowledgementAndReturnRequestOk() throws Exception {
		final GeneralAdvertisementMessageDTO msg = new GeneralAdvertisementMessageDTO();
		msg.setSenderPublicKey(aPublicKeyStr);
		msg.setSessionId("1234");
		
		final Session session = Mockito.mock(Session.class);
		final Queue requestQueue = Mockito.mock(Queue.class);
		final Queue responseQueue = Mockito.mock(Queue.class);
		final MessageConsumer consumer = Mockito.mock(MessageConsumer.class);
		final MessageProducer producer = Mockito.mock(MessageProducer.class);
		final TextMessage ackMsg = Mockito.mock(TextMessage.class);
		final TextMessage request = Mockito.mock(TextMessage.class);
		
		final DecryptedMessageDTO decrypted = new DecryptedMessageDTO();
		decrypted.setMessageType(CoreCommonConstants.RELAY_MESSAGE_TYPE_GSD_POLL);
		decrypted.setSessionId("1234");
		decrypted.setPayload("{ \"requestedService\": { \"serviceDefinitionRequirement\": \"service\" }}");
		
		when(session.createQueue(anyString())).thenReturn(requestQueue, responseQueue);
		when(session.createConsumer(requestQueue)).thenReturn(consumer);
		when(session.createProducer(responseQueue)).thenReturn(producer);
		when(cryptographer.encodeRelayMessage(eq("ack"), eq("1234"), isNull(), any(PublicKey.class))).thenReturn("encoded");
		when(session.createTextMessage("encoded")).thenReturn(ackMsg);
		doNothing().when(producer).send(ackMsg);
		when(consumer.receive(anyLong())).thenReturn(request);
		when(request.getText()).thenReturn("abcd");
		when(cryptographer.decodeMessage(eq("abcd"), any(PublicKey.class))).thenReturn(decrypted);
		doNothing().when(consumer).close();
		
		final GatekeeperRelayRequest result = testingObject.sendAcknowledgementAndReturnRequest(session, msg);
		
		Assert.assertEquals("1234", result.getSessionId());
		Assert.assertEquals(producer, result.getAnswerSender());
		Assert.assertEquals("service", result.getGSDPollRequest().getRequestedService().getServiceDefinitionRequirement());
		
		verify(session, times(2)).createQueue(anyString());
		verify(session, times(1)).createConsumer(requestQueue);
		verify(session, times(1)).createProducer(responseQueue);
		verify(cryptographer, times(1)).encodeRelayMessage(eq("ack"), eq("1234"), isNull(), any(PublicKey.class));
		verify(session, times(1)).createTextMessage("encoded");
		verify(producer, times(1)).send(ackMsg);
		verify(consumer, times(1)).receive(anyLong());
		verify(request, times(1)).getText();
		verify(cryptographer, times(1)).decodeMessage(eq("abcd"), any(PublicKey.class));
		verify(consumer, times(1)).close();
		verify(producer, never()).close();
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendResponseRequestNull() throws Exception {
		try {
			testingObject.sendResponse(null, null, null);
		} catch (final Exception ex) {
			Assert.assertEquals("Request is null.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendResponseSenderNull() throws Exception {
		final MessageProducer producer = Mockito.mock(MessageProducer.class);
		final GatekeeperRelayRequest request = new GatekeeperRelayRequest(producer, publicKey, "1234", "type", "payload");
		ReflectionTestUtils.setField(request, "answerSender", null);

		try {
			testingObject.sendResponse(null, request, null);
		} catch (final Exception ex) {
			Assert.assertEquals("Sender is null.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendResponseSessionNull() throws Exception {
		final MessageProducer producer = Mockito.mock(MessageProducer.class);
		final GatekeeperRelayRequest request = new GatekeeperRelayRequest(producer, publicKey, "1234", "type", "payload");
		
		doNothing().when(producer).close();

		try {
			testingObject.sendResponse(null, request, null);
		} catch (final Exception ex) {
			Assert.assertEquals("Session is null.", ex.getMessage());
			
			verify(producer, times(1)).close();
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendResponsePublicKeyNull() throws Exception {
		final Session session = Mockito.mock(Session.class);
		final MessageProducer producer = Mockito.mock(MessageProducer.class);
		final GatekeeperRelayRequest request = new GatekeeperRelayRequest(producer, publicKey, "1234", "type", "payload");
		ReflectionTestUtils.setField(request, "peerPublicKey", null);
		
		doNothing().when(producer).close();

		try {
			testingObject.sendResponse(session, request, null);
		} catch (final Exception ex) {
			Assert.assertEquals("Peer public key is null.", ex.getMessage());
			
			verify(producer, times(1)).close();
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendResponseSessionIdNull() throws Exception {
		final Session session = Mockito.mock(Session.class);
		final MessageProducer producer = Mockito.mock(MessageProducer.class);
		final GatekeeperRelayRequest request = new GatekeeperRelayRequest(producer, publicKey, "1234", "type", "payload");
		ReflectionTestUtils.setField(request, "sessionId", null);
		
		doNothing().when(producer).close();

		try {
			testingObject.sendResponse(session, request, null);
		} catch (final Exception ex) {
			Assert.assertEquals("Session id is null or blank.", ex.getMessage());
			
			verify(producer, times(1)).close();
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendResponseSessionIdEmpty() throws Exception {
		final Session session = Mockito.mock(Session.class);
		final MessageProducer producer = Mockito.mock(MessageProducer.class);
		final GatekeeperRelayRequest request = new GatekeeperRelayRequest(producer, publicKey, "1234", "type", "payload");
		ReflectionTestUtils.setField(request, "sessionId", "");
		
		doNothing().when(producer).close();

		try {
			testingObject.sendResponse(session, request, null);
		} catch (final Exception ex) {
			Assert.assertEquals("Session id is null or blank.", ex.getMessage());
			
			verify(producer, times(1)).close();
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendResponseMessageTypeNull() throws Exception {
		final Session session = Mockito.mock(Session.class);
		final MessageProducer producer = Mockito.mock(MessageProducer.class);
		final GatekeeperRelayRequest request = new GatekeeperRelayRequest(producer, publicKey, "1234", "type", "payload");
		ReflectionTestUtils.setField(request, "messageType", null);
		
		doNothing().when(producer).close();

		try {
			testingObject.sendResponse(session, request, null);
		} catch (final Exception ex) {
			Assert.assertEquals("Message type is null or blank.", ex.getMessage());
			
			verify(producer, times(1)).close();
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendResponseMessageTypeEmpty() throws Exception {
		final Session session = Mockito.mock(Session.class);
		final MessageProducer producer = Mockito.mock(MessageProducer.class);
		final GatekeeperRelayRequest request = new GatekeeperRelayRequest(producer, publicKey, "1234", "type", "payload");
		ReflectionTestUtils.setField(request, "messageType", "");
		
		doNothing().when(producer).close();

		try {
			testingObject.sendResponse(session, request, null);
		} catch (final Exception ex) {
			Assert.assertEquals("Message type is null or blank.", ex.getMessage());
			
			verify(producer, times(1)).close();
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendResponsePayloadNull() throws Exception {
		final Session session = Mockito.mock(Session.class);
		final MessageProducer producer = Mockito.mock(MessageProducer.class);
		final GatekeeperRelayRequest request = new GatekeeperRelayRequest(producer, publicKey, "1234", "type", "payload");
		
		doNothing().when(producer).close();

		try {
			testingObject.sendResponse(session, request, null);
		} catch (final Exception ex) {
			Assert.assertEquals("Payload is null.", ex.getMessage());
			
			verify(producer, times(1)).close();
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testSendResponseInvalidMessageType() throws Exception {
		final Session session = Mockito.mock(Session.class);
		final MessageProducer producer = Mockito.mock(MessageProducer.class);
		final GatekeeperRelayRequest request = new GatekeeperRelayRequest(producer, publicKey, "1234", "type", "payload");
		
		doNothing().when(producer).close();

		try {
			testingObject.sendResponse(session, request, "payload");
		} catch (final Exception ex) {
			Assert.assertEquals("Invalid message type: type", ex.getMessage());
			
			verify(producer, times(1)).close();
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testSendResponseMessageTypeAndPayloadMismatch() throws Exception {
		final Session session = Mockito.mock(Session.class);
		final MessageProducer producer = Mockito.mock(MessageProducer.class);
		final GatekeeperRelayRequest request = new GatekeeperRelayRequest(producer, publicKey, "1234", CoreCommonConstants.RELAY_MESSAGE_TYPE_GSD_POLL, "payload");
		
		doNothing().when(producer).close();

		try {
			testingObject.sendResponse(session, request, "payload");
		} catch (final Exception ex) {
			Assert.assertEquals("The specified payload is not a valid response to the specified request.", ex.getMessage());
			
			verify(producer, times(1)).close();
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testSendResponseOk() throws Exception {
		final Session session = Mockito.mock(Session.class);
		final MessageProducer producer = Mockito.mock(MessageProducer.class);
		final GatekeeperRelayRequest request = new GatekeeperRelayRequest(producer, publicKey, "1234", CoreCommonConstants.RELAY_MESSAGE_TYPE_GSD_POLL, "payload");
		final GSDPollResponseDTO responsePayload = new GSDPollResponseDTO();
		final TextMessage response = Mockito.mock(TextMessage.class);
		
		when(cryptographer.encodeRelayMessage(CoreCommonConstants.RELAY_MESSAGE_TYPE_GSD_POLL, "1234", responsePayload, publicKey)).thenReturn("encoded");
		when(session.createTextMessage("encoded")).thenReturn(response);
		doNothing().when(producer).send(response);
		doNothing().when(producer).close();

		testingObject.sendResponse(session, request, responsePayload);

		verify(cryptographer, times(1)).encodeRelayMessage(CoreCommonConstants.RELAY_MESSAGE_TYPE_GSD_POLL, "1234", responsePayload, publicKey);
		verify(session, times(1)).createTextMessage("encoded");
		verify(producer, times(1)).send(response);
		verify(producer, times(1)).close();
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testPublishGeneralAdvertisementSessionNull() throws Exception {
		try {
			testingObject.publishGeneralAdvertisement(null, null, null);
		} catch (final Exception ex) {
			Assert.assertEquals("session is null.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testPublishGeneralAdvertisementRecipientCNNull() throws Exception {
		try {
			testingObject.publishGeneralAdvertisement(getTestSession(), null, null);
		} catch (final Exception ex) {
			Assert.assertEquals("recipientCN is null or blank.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testPublishGeneralAdvertisementRecipientCNEmpty() throws Exception {
		try {
			testingObject.publishGeneralAdvertisement(getTestSession(), "", null);
		} catch (final Exception ex) {
			Assert.assertEquals("recipientCN is null or blank.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testPublishGeneralAdvertisementRecipientPublicKeyNull() throws Exception {
		try {
			testingObject.publishGeneralAdvertisement(getTestSession(), "recipient-cn", null);
		} catch (final Exception ex) {
			Assert.assertEquals("recipientPublicKey is null or blank.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testPublishGeneralAdvertisementRecipientPublicKeyEmpty() throws Exception {
		try {
			testingObject.publishGeneralAdvertisement(getTestSession(), "recipient-cn", "");
		} catch (final Exception ex) {
			Assert.assertEquals("recipientPublicKey is null or blank.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testPublishGeneralAdvertisementException1() throws Exception {
		when(cryptographer.encodeSessionId(anyString(), any(PublicKey.class))).thenThrow(new ArrowheadException("test"));
		
		try {
			testingObject.publishGeneralAdvertisement(getTestSession(), "recipient-cn", aPublicKeyStr);
		} catch (final Exception ex) {
			Assert.assertEquals("test", ex.getMessage());
			
			verify(cryptographer, times(1)).encodeSessionId(anyString(), any(PublicKey.class));
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = JMSException.class)
	public void testPublishGeneralAdvertisementException2() throws Exception {
		final Session session = Mockito.mock(Session.class);
		final TextMessage advertisement = Mockito.mock(TextMessage.class);
		final Topic topic = Mockito.mock(Topic.class);
		final MessageProducer producer = Mockito.mock(MessageProducer.class);
		final MessageConsumer consumer = Mockito.mock(MessageConsumer.class);
		
		when(cryptographer.encodeSessionId(anyString(), any(PublicKey.class))).thenReturn("encodedSessionId");
		when(publicKey.getEncoded()).thenReturn(new byte[] { 1, 2, 3, 4 });
		when(session.createTextMessage(anyString())).thenReturn(advertisement);
		when(session.createTopic(topicName)).thenReturn(topic);
		when(session.createProducer(topic)).thenReturn(producer);
		when(session.createQueue(anyString())).thenThrow(new JMSException("test"));
		doNothing().when(producer).close();
		
		try {
			testingObject.publishGeneralAdvertisement(session, "recipient-cn", aPublicKeyStr);
		} catch (final Exception ex) {
			Assert.assertEquals("test", ex.getMessage());
			
			verify(cryptographer, times(1)).encodeSessionId(anyString(), any(PublicKey.class));
			verify(publicKey, times(1)).getEncoded();
			verify(session, times(1)).createTextMessage(anyString());
			verify(session, times(1)).createTopic(topicName);
			verify(session, times(1)).createProducer(topic);
			verify(session, times(1)).createQueue(anyString());
			verify(producer, times(1)).close();
			verify(consumer, never()).close();
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testPublishGeneralAdvertisementNoAcknowledgement() throws Exception {
		final Session session = Mockito.mock(Session.class);
		final TextMessage advertisement = Mockito.mock(TextMessage.class);
		final Topic topic = Mockito.mock(Topic.class);
		final Queue respQueue = Mockito.mock(Queue.class);
		final MessageProducer producer = Mockito.mock(MessageProducer.class);
		final MessageConsumer consumer = Mockito.mock(MessageConsumer.class);
		
		when(cryptographer.encodeSessionId(anyString(), any(PublicKey.class))).thenReturn("encodedSessionId");
		when(publicKey.getEncoded()).thenReturn(new byte[] { 1, 2, 3, 4 });
		when(session.createTextMessage(anyString())).thenReturn(advertisement);
		when(session.createTopic(topicName)).thenReturn(topic);
		when(session.createProducer(topic)).thenReturn(producer);
		when(session.createQueue(anyString())).thenReturn(respQueue);
		when(session.createConsumer(respQueue)).thenReturn(consumer);
		doNothing().when(producer).send(advertisement);
		when(consumer.receive(anyLong())).thenReturn(null);
		doNothing().when(producer).close();
		doNothing().when(consumer).close();
		
		final GeneralAdvertisementResult result = testingObject.publishGeneralAdvertisement(session, "recipient-cn", aPublicKeyStr);
		
		Assert.assertNull(result);

		verify(cryptographer, times(1)).encodeSessionId(anyString(), any(PublicKey.class));
		verify(publicKey, times(1)).getEncoded();
		verify(session, times(1)).createTextMessage(anyString());
		verify(session, times(1)).createTopic(topicName);
		verify(session, times(1)).createProducer(topic);
		verify(session, times(1)).createQueue(anyString());
		verify(session, times(1)).createConsumer(respQueue);
		verify(producer, times(1)).send(advertisement);
		verify(consumer, times(1)).receive(anyLong());
		verify(producer, times(1)).close();
		verify(consumer, times(1)).close();
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = JMSException.class)
	public void testPublishGeneralAdvertisementInvalidMessageClass() throws Exception {
		final Session session = Mockito.mock(Session.class);
		final TextMessage advertisement = Mockito.mock(TextMessage.class);
		final Topic topic = Mockito.mock(Topic.class);
		final Queue respQueue = Mockito.mock(Queue.class);
		final MessageProducer producer = Mockito.mock(MessageProducer.class);
		final MessageConsumer consumer = Mockito.mock(MessageConsumer.class);
		final Message ackMsg = Mockito.mock(Message.class);
		
		when(cryptographer.encodeSessionId(anyString(), any(PublicKey.class))).thenReturn("encodedSessionId");
		when(publicKey.getEncoded()).thenReturn(new byte[] { 1, 2, 3, 4 });
		when(session.createTextMessage(anyString())).thenReturn(advertisement);
		when(session.createTopic(topicName)).thenReturn(topic);
		when(session.createProducer(topic)).thenReturn(producer);
		when(session.createQueue(anyString())).thenReturn(respQueue);
		when(session.createConsumer(respQueue)).thenReturn(consumer);
		doNothing().when(producer).send(advertisement);
		when(consumer.receive(anyLong())).thenReturn(ackMsg);
		doNothing().when(producer).close();
		doNothing().when(consumer).close();
		
		try {
			testingObject.publishGeneralAdvertisement(session, "recipient-cn", aPublicKeyStr);
		} catch (final Exception ex) {
			Assert.assertTrue(ex.getMessage().startsWith("Invalid message class: "));
			
			verify(cryptographer, times(1)).encodeSessionId(anyString(), any(PublicKey.class));
			verify(publicKey, times(1)).getEncoded();
			verify(session, times(1)).createTextMessage(anyString());
			verify(session, times(1)).createTopic(topicName);
			verify(session, times(1)).createProducer(topic);
			verify(session, times(1)).createQueue(anyString());
			verify(session, times(1)).createConsumer(respQueue);
			verify(producer, times(1)).send(advertisement);
			verify(consumer, times(1)).receive(anyLong());
			verify(producer, times(1)).close();
			verify(consumer, times(1)).close();
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = AuthException.class)
	public void testPublishGeneralAdvertisementInvalidMessageType() throws Exception {
		final Session session = Mockito.mock(Session.class);
		final TextMessage advertisement = Mockito.mock(TextMessage.class);
		final Topic topic = Mockito.mock(Topic.class);
		final Queue respQueue = Mockito.mock(Queue.class);
		final MessageProducer producer = Mockito.mock(MessageProducer.class);
		final MessageConsumer consumer = Mockito.mock(MessageConsumer.class);
		final TextMessage ackMsg = Mockito.mock(TextMessage.class);
		final DecryptedMessageDTO decoded = new DecryptedMessageDTO();
		decoded.setMessageType("not ack");
		
		when(cryptographer.encodeSessionId(anyString(), any(PublicKey.class))).thenReturn("encodedSessionId");
		when(publicKey.getEncoded()).thenReturn(new byte[] { 1, 2, 3, 4 });
		when(session.createTextMessage(anyString())).thenReturn(advertisement);
		when(session.createTopic(topicName)).thenReturn(topic);
		when(session.createProducer(topic)).thenReturn(producer);
		when(session.createQueue(anyString())).thenReturn(respQueue);
		when(session.createConsumer(respQueue)).thenReturn(consumer);
		doNothing().when(producer).send(advertisement);
		when(consumer.receive(anyLong())).thenReturn(ackMsg);
		when(ackMsg.getText()).thenReturn("encoded");
		when(cryptographer.decodeMessage(eq("encoded"), any(PublicKey.class))).thenReturn(decoded);
		doNothing().when(producer).close();
		doNothing().when(consumer).close();
		
		try {
			testingObject.publishGeneralAdvertisement(session, "recipient-cn", aPublicKeyStr);
		} catch (final Exception ex) {
			Assert.assertEquals("Unauthorized message on queue.", ex.getMessage());
			
			verify(cryptographer, times(1)).encodeSessionId(anyString(), any(PublicKey.class));
			verify(publicKey, times(1)).getEncoded();
			verify(session, times(1)).createTextMessage(anyString());
			verify(session, times(1)).createTopic(topicName);
			verify(session, times(1)).createProducer(topic);
			verify(session, times(1)).createQueue(anyString());
			verify(session, times(1)).createConsumer(respQueue);
			verify(producer, times(1)).send(advertisement);
			verify(consumer, times(1)).receive(anyLong());
			verify(cryptographer, times(1)).decodeMessage(eq("encoded"), any(PublicKey.class));
			verify(ackMsg, times(1)).getText();
			verify(producer, times(1)).close();
			verify(consumer, times(1)).close();
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = AuthException.class)
	public void testPublishGeneralAdvertisementInvalidSessionId() throws Exception {
		final Session session = Mockito.mock(Session.class);
		final TextMessage advertisement = Mockito.mock(TextMessage.class);
		final Topic topic = Mockito.mock(Topic.class);
		final Queue respQueue = Mockito.mock(Queue.class);
		final MessageProducer producer = Mockito.mock(MessageProducer.class);
		final MessageConsumer consumer = Mockito.mock(MessageConsumer.class);
		final TextMessage ackMsg = Mockito.mock(TextMessage.class);
		final DecryptedMessageDTO decoded = new DecryptedMessageDTO();
		decoded.setMessageType(CoreCommonConstants.RELAY_MESSAGE_TYPE_ACK);
		decoded.setSessionId("invalid");
		
		when(cryptographer.encodeSessionId(anyString(), any(PublicKey.class))).thenReturn("encodedSessionId");
		when(publicKey.getEncoded()).thenReturn(new byte[] { 1, 2, 3, 4 });
		when(session.createTextMessage(anyString())).thenReturn(advertisement);
		when(session.createTopic(topicName)).thenReturn(topic);
		when(session.createProducer(topic)).thenReturn(producer);
		when(session.createQueue(anyString())).thenReturn(respQueue);
		when(session.createConsumer(respQueue)).thenReturn(consumer);
		doNothing().when(producer).send(advertisement);
		when(consumer.receive(anyLong())).thenReturn(ackMsg);
		when(ackMsg.getText()).thenReturn("encoded");
		when(cryptographer.decodeMessage(eq("encoded"), any(PublicKey.class))).thenReturn(decoded);
		doNothing().when(producer).close();
		doNothing().when(consumer).close();
		
		try {
			testingObject.publishGeneralAdvertisement(session, "recipient-cn", aPublicKeyStr);
		} catch (final Exception ex) {
			Assert.assertEquals("Unauthorized message on queue.", ex.getMessage());
			
			verify(cryptographer, times(1)).encodeSessionId(anyString(), any(PublicKey.class));
			verify(publicKey, times(1)).getEncoded();
			verify(session, times(1)).createTextMessage(anyString());
			verify(session, times(1)).createTopic(topicName);
			verify(session, times(1)).createProducer(topic);
			verify(session, times(1)).createQueue(anyString());
			verify(session, times(1)).createConsumer(respQueue);
			verify(producer, times(1)).send(advertisement);
			verify(consumer, times(1)).receive(anyLong());
			verify(cryptographer, times(1)).decodeMessage(eq("encoded"), any(PublicKey.class));
			verify(ackMsg, times(1)).getText();
			verify(producer, times(1)).close();
			verify(consumer, times(1)).close();
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testPublishGeneralAdvertisementOk() throws Exception {
		final Session session = Mockito.mock(Session.class);
		final TextMessage advertisement = Mockito.mock(TextMessage.class);
		final Topic topic = Mockito.mock(Topic.class);
		final Queue respQueue = Mockito.mock(Queue.class);
		final MessageProducer producer = Mockito.mock(MessageProducer.class);
		final MessageConsumer consumer = Mockito.mock(MessageConsumer.class);
		final TextMessage ackMsg = Mockito.mock(TextMessage.class);
		final DecryptedMessageDTO decoded = Mockito.mock(DecryptedMessageDTO.class);
		final ArgumentCaptor<String> sessionIdCaptor = ArgumentCaptor.forClass(String.class);
		
		when(cryptographer.encodeSessionId(sessionIdCaptor.capture(), any(PublicKey.class))).thenReturn("encodedSessionId");
		when(publicKey.getEncoded()).thenReturn(new byte[] { 1, 2, 3, 4 });
		when(session.createTextMessage(anyString())).thenReturn(advertisement);
		when(session.createTopic(topicName)).thenReturn(topic);
		when(session.createProducer(topic)).thenReturn(producer);
		when(session.createQueue(anyString())).thenReturn(respQueue);
		when(session.createConsumer(respQueue)).thenReturn(consumer);
		doNothing().when(producer).send(advertisement);
		when(consumer.receive(anyLong())).thenReturn(ackMsg);
		when(ackMsg.getText()).thenReturn("encoded");
		when(cryptographer.decodeMessage(eq("encoded"), any(PublicKey.class))).thenReturn(decoded);
		when(decoded.getMessageType()).thenReturn(CoreCommonConstants.RELAY_MESSAGE_TYPE_ACK);
		when(decoded.getSessionId()).thenAnswer(new Answer<String>() {
			public String answer(final InvocationOnMock invocation) throws Throwable {
				return sessionIdCaptor.getValue();
			}
		});
		doNothing().when(producer).close();
		
		final GeneralAdvertisementResult result = testingObject.publishGeneralAdvertisement(session, "recipient-cn", aPublicKeyStr);

		Assert.assertEquals(consumer, result.getAnswerReceiver());
		Assert.assertEquals(sessionIdCaptor.getValue(), result.getSessionId());
		
		verify(cryptographer, times(1)).encodeSessionId(anyString(), any(PublicKey.class));
		verify(publicKey, times(1)).getEncoded();
		verify(session, times(1)).createTextMessage(anyString());
		verify(session, times(1)).createTopic(topicName);
		verify(session, times(1)).createProducer(topic);
		verify(session, times(1)).createQueue(anyString());
		verify(session, times(1)).createConsumer(respQueue);
		verify(producer, times(1)).send(advertisement);
		verify(consumer, times(1)).receive(anyLong());
		verify(cryptographer, times(1)).decodeMessage(eq("encoded"), any(PublicKey.class));
		verify(decoded, times(1)).getMessageType();
		verify(decoded, times(1)).getSessionId();
		verify(ackMsg, times(1)).getText();
		verify(producer, times(1)).close();
		verify(consumer, never()).close();
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendRequestAndReturnResponseAdvertisementResponseNull() throws Exception {
		try {
			testingObject.sendRequestAndReturnResponse(null, null, null);
		} catch (final Exception ex) {
			Assert.assertEquals("advResponse is null.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendRequestAndReturnResponseReceiverNull() throws Exception {
		final MessageConsumer consumer = Mockito.mock(MessageConsumer.class);
		final GeneralAdvertisementResult advResponse = new GeneralAdvertisementResult(consumer, "peer-cn", getTestPublicKey(), "1234");
		ReflectionTestUtils.setField(advResponse, "anwserReceiver", null);
		
		try {
			testingObject.sendRequestAndReturnResponse(null, advResponse, null);
		} catch (final Exception ex) {
			Assert.assertEquals("Receiver is null.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendRequestAndReturnResponseSessionNull() throws Exception {
		final MessageConsumer consumer = Mockito.mock(MessageConsumer.class);
		final GeneralAdvertisementResult advResponse = new GeneralAdvertisementResult(consumer, "peer-cn", getTestPublicKey(), "1234");

		doNothing().when(consumer).close();
		
		try {
			testingObject.sendRequestAndReturnResponse(null, advResponse, null);
		} catch (final Exception ex) {
			Assert.assertEquals("Session is null.", ex.getMessage());
			
			verify(consumer, times(1)).close();
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendRequestAndReturnResponsePeerCommonNameNull() throws Exception {
		final MessageConsumer consumer = Mockito.mock(MessageConsumer.class);
		final GeneralAdvertisementResult advResponse = new GeneralAdvertisementResult(consumer, "peer-cn", getTestPublicKey(), "1234");
		ReflectionTestUtils.setField(advResponse, "peerCN", null);

		doNothing().when(consumer).close();
		
		try {
			testingObject.sendRequestAndReturnResponse(getTestSession(), advResponse, null);
		} catch (final Exception ex) {
			Assert.assertEquals("Peer common name is null or blank.", ex.getMessage());
			
			verify(consumer, times(1)).close();
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendRequestAndReturnResponsePeerCommonNameEmpty() throws Exception {
		final MessageConsumer consumer = Mockito.mock(MessageConsumer.class);
		final GeneralAdvertisementResult advResponse = new GeneralAdvertisementResult(consumer, "peer-cn", getTestPublicKey(), "1234");
		ReflectionTestUtils.setField(advResponse, "peerCN", "");

		doNothing().when(consumer).close();
		
		try {
			testingObject.sendRequestAndReturnResponse(getTestSession(), advResponse, null);
		} catch (final Exception ex) {
			Assert.assertEquals("Peer common name is null or blank.", ex.getMessage());
			
			verify(consumer, times(1)).close();
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendRequestAndReturnResponsePeerPublicKeyNull() throws Exception {
		final MessageConsumer consumer = Mockito.mock(MessageConsumer.class);
		final GeneralAdvertisementResult advResponse = new GeneralAdvertisementResult(consumer, "peer-cn", getTestPublicKey(), "1234");
		ReflectionTestUtils.setField(advResponse, "peerPublicKey", null);

		doNothing().when(consumer).close();
		
		try {
			testingObject.sendRequestAndReturnResponse(getTestSession(), advResponse, null);
		} catch (final Exception ex) {
			Assert.assertEquals("Peer public key is null.", ex.getMessage());
			
			verify(consumer, times(1)).close();
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendRequestAndReturnResponseSessionIdNull() throws Exception {
		final MessageConsumer consumer = Mockito.mock(MessageConsumer.class);
		final GeneralAdvertisementResult advResponse = new GeneralAdvertisementResult(consumer, "peer-cn", getTestPublicKey(), "1234");
		ReflectionTestUtils.setField(advResponse, "sessionId", null);

		doNothing().when(consumer).close();
		
		try {
			testingObject.sendRequestAndReturnResponse(getTestSession(), advResponse, null);
		} catch (final Exception ex) {
			Assert.assertEquals("Session id is null or blank.", ex.getMessage());
			
			verify(consumer, times(1)).close();
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendRequestAndReturnResponseSessionIdEmpty() throws Exception {
		final MessageConsumer consumer = Mockito.mock(MessageConsumer.class);
		final GeneralAdvertisementResult advResponse = new GeneralAdvertisementResult(consumer, "peer-cn", getTestPublicKey(), "1234");
		ReflectionTestUtils.setField(advResponse, "sessionId", "");

		doNothing().when(consumer).close();
		
		try {
			testingObject.sendRequestAndReturnResponse(getTestSession(), advResponse, null);
		} catch (final Exception ex) {
			Assert.assertEquals("Session id is null or blank.", ex.getMessage());
			
			verify(consumer, times(1)).close();
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendRequestAndReturnResponsePayloadNull() throws Exception {
		final MessageConsumer consumer = Mockito.mock(MessageConsumer.class);
		final GeneralAdvertisementResult advResponse = new GeneralAdvertisementResult(consumer, "peer-cn", getTestPublicKey(), "1234");

		doNothing().when(consumer).close();
		
		try {
			testingObject.sendRequestAndReturnResponse(getTestSession(), advResponse, null);
		} catch (final Exception ex) {
			Assert.assertEquals("Payload is null.", ex.getMessage());
			
			verify(consumer, times(1)).close();
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testSendRequestAndReturnResponseInvalidMessageType() throws Exception {
		final Session session = Mockito.mock(Session.class);
		final Queue requestQueue = Mockito.mock(Queue.class);
		final MessageConsumer consumer = Mockito.mock(MessageConsumer.class);
		final MessageProducer producer = Mockito.mock(MessageProducer.class);
		final GeneralAdvertisementResult advResponse = new GeneralAdvertisementResult(consumer, "peer-cn", getTestPublicKey(), "1234");

		when(session.createQueue(anyString())).thenReturn(requestQueue);
		when(session.createProducer(requestQueue)).thenReturn(producer);
		doNothing().when(consumer).close();
		doNothing().when(producer).close();
		
		try {
			testingObject.sendRequestAndReturnResponse(session, advResponse, "invalid");
		} catch (final Exception ex) {
			Assert.assertTrue(ex.getMessage().startsWith("Invalid message DTO: "));
			
			verify(session, times(1)).createQueue(anyString());
			verify(session, times(1)).createProducer(requestQueue);
			verify(consumer, times(1)).close();
			verify(producer, times(1)).close();
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testSendRequestAndReturnResponseNoResponseInTime() throws Exception {
		final Session session = Mockito.mock(Session.class);
		final Queue requestQueue = Mockito.mock(Queue.class);
		final MessageConsumer consumer = Mockito.mock(MessageConsumer.class);
		final MessageProducer producer = Mockito.mock(MessageProducer.class);
		final PublicKey peerPublicKey = getTestPublicKey();
		final GeneralAdvertisementResult advResponse = new GeneralAdvertisementResult(consumer, "peer-cn", peerPublicKey, "1234");
		final GSDPollRequestDTO pollRequest = new GSDPollRequestDTO();
		final TextMessage requestMsg = Mockito.mock(TextMessage.class);

		when(session.createQueue(anyString())).thenReturn(requestQueue);
		when(session.createProducer(requestQueue)).thenReturn(producer);
		when(cryptographer.encodeRelayMessage(CoreCommonConstants.RELAY_MESSAGE_TYPE_GSD_POLL, "1234", pollRequest, peerPublicKey)).thenReturn("encoded");
		when(session.createTextMessage("encoded")).thenReturn(requestMsg);
		doNothing().when(producer).send(requestMsg);
		when(consumer.receive(anyLong())).thenReturn(null);
		doNothing().when(consumer).close();
		doNothing().when(producer).close();
		
		final GatekeeperRelayResponse result = testingObject.sendRequestAndReturnResponse(session, advResponse, pollRequest);
		
		Assert.assertNull(result);
		
		verify(session, times(1)).createQueue(anyString());
		verify(session, times(1)).createProducer(requestQueue);
		verify(cryptographer, times(1)).encodeRelayMessage(CoreCommonConstants.RELAY_MESSAGE_TYPE_GSD_POLL, "1234", pollRequest, peerPublicKey);
		verify(session, times(1)).createTextMessage("encoded");
		verify(producer, times(1)).send(requestMsg);
		verify(consumer, times(1)).receive(anyLong());
		verify(consumer, times(1)).close();
		verify(producer, times(1)).close();
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = JMSException.class)
	public void testSendRequestAndReturnResponseInvalidMessageClass() throws Exception {
		final Session session = Mockito.mock(Session.class);
		final Queue requestQueue = Mockito.mock(Queue.class);
		final MessageConsumer consumer = Mockito.mock(MessageConsumer.class);
		final MessageProducer producer = Mockito.mock(MessageProducer.class);
		final PublicKey peerPublicKey = getTestPublicKey();
		final GeneralAdvertisementResult advResponse = new GeneralAdvertisementResult(consumer, "peer-cn", peerPublicKey, "1234");
		final GSDPollRequestDTO pollRequest = new GSDPollRequestDTO();
		final TextMessage requestMsg = Mockito.mock(TextMessage.class);
		final Message responseMsg = Mockito.mock(Message.class);

		when(session.createQueue(anyString())).thenReturn(requestQueue);
		when(session.createProducer(requestQueue)).thenReturn(producer);
		when(cryptographer.encodeRelayMessage(CoreCommonConstants.RELAY_MESSAGE_TYPE_GSD_POLL, "1234", pollRequest, peerPublicKey)).thenReturn("encoded");
		when(session.createTextMessage("encoded")).thenReturn(requestMsg);
		doNothing().when(producer).send(requestMsg);
		when(consumer.receive(anyLong())).thenReturn(responseMsg);
		doNothing().when(consumer).close();
		doNothing().when(producer).close();
		
		try {
			testingObject.sendRequestAndReturnResponse(session, advResponse, pollRequest);
		} catch (final Exception ex) {
			Assert.assertTrue(ex.getMessage().startsWith("Invalid message class: "));
			
			verify(session, times(1)).createQueue(anyString());
			verify(session, times(1)).createProducer(requestQueue);
			verify(cryptographer, times(1)).encodeRelayMessage(CoreCommonConstants.RELAY_MESSAGE_TYPE_GSD_POLL, "1234", pollRequest, peerPublicKey);
			verify(session, times(1)).createTextMessage("encoded");
			verify(producer, times(1)).send(requestMsg);
			verify(consumer, times(1)).receive(anyLong());
			verify(consumer, times(1)).close();
			verify(producer, times(1)).close();
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = AuthException.class)
	public void testSendRequestAndReturnResponseInvalidResponseMessageType() throws Exception {
		final Session session = Mockito.mock(Session.class);
		final Queue requestQueue = Mockito.mock(Queue.class);
		final MessageConsumer consumer = Mockito.mock(MessageConsumer.class);
		final MessageProducer producer = Mockito.mock(MessageProducer.class);
		final PublicKey peerPublicKey = getTestPublicKey();
		final GeneralAdvertisementResult advResponse = new GeneralAdvertisementResult(consumer, "peer-cn", peerPublicKey, "1234");
		final GSDPollRequestDTO pollRequest = new GSDPollRequestDTO();
		final TextMessage requestMsg = Mockito.mock(TextMessage.class);
		final TextMessage responseMsg = Mockito.mock(TextMessage.class);
		final DecryptedMessageDTO decoded = new DecryptedMessageDTO();
		decoded.setMessageType("invalid");

		when(session.createQueue(anyString())).thenReturn(requestQueue);
		when(session.createProducer(requestQueue)).thenReturn(producer);
		when(cryptographer.encodeRelayMessage(CoreCommonConstants.RELAY_MESSAGE_TYPE_GSD_POLL, "1234", pollRequest, peerPublicKey)).thenReturn("encoded");
		when(session.createTextMessage("encoded")).thenReturn(requestMsg);
		doNothing().when(producer).send(requestMsg);
		when(consumer.receive(anyLong())).thenReturn(responseMsg);
		when(responseMsg.getText()).thenReturn("encodedResponse");
		when(cryptographer.decodeMessage("encodedResponse", peerPublicKey)).thenReturn(decoded);
		doNothing().when(consumer).close();
		doNothing().when(producer).close();
		
		try {
			testingObject.sendRequestAndReturnResponse(session, advResponse, pollRequest);
		} catch (final Exception ex) {
			Assert.assertEquals("Unauthorized message on queue.", ex.getMessage());
			
			verify(session, times(1)).createQueue(anyString());
			verify(session, times(1)).createProducer(requestQueue);
			verify(cryptographer, times(1)).encodeRelayMessage(CoreCommonConstants.RELAY_MESSAGE_TYPE_GSD_POLL, "1234", pollRequest, peerPublicKey);
			verify(session, times(1)).createTextMessage("encoded");
			verify(producer, times(1)).send(requestMsg);
			verify(consumer, times(1)).receive(anyLong());
			verify(responseMsg, times(1)).getText();
			verify(cryptographer, times(1)).decodeMessage("encodedResponse", peerPublicKey);
			verify(consumer, times(1)).close();
			verify(producer, times(1)).close();
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = AuthException.class)
	public void testSendRequestAndReturnResponseInvalidResponseSessionId() throws Exception {
		final Session session = Mockito.mock(Session.class);
		final Queue requestQueue = Mockito.mock(Queue.class);
		final MessageConsumer consumer = Mockito.mock(MessageConsumer.class);
		final MessageProducer producer = Mockito.mock(MessageProducer.class);
		final PublicKey peerPublicKey = getTestPublicKey();
		final GeneralAdvertisementResult advResponse = new GeneralAdvertisementResult(consumer, "peer-cn", peerPublicKey, "1234");
		final GSDPollRequestDTO pollRequest = new GSDPollRequestDTO();
		final TextMessage requestMsg = Mockito.mock(TextMessage.class);
		final TextMessage responseMsg = Mockito.mock(TextMessage.class);
		final DecryptedMessageDTO decoded = new DecryptedMessageDTO();
		decoded.setMessageType(CoreCommonConstants.RELAY_MESSAGE_TYPE_GSD_POLL);
		decoded.setSessionId("5678");

		when(session.createQueue(anyString())).thenReturn(requestQueue);
		when(session.createProducer(requestQueue)).thenReturn(producer);
		when(cryptographer.encodeRelayMessage(CoreCommonConstants.RELAY_MESSAGE_TYPE_GSD_POLL, "1234", pollRequest, peerPublicKey)).thenReturn("encoded");
		when(session.createTextMessage("encoded")).thenReturn(requestMsg);
		doNothing().when(producer).send(requestMsg);
		when(consumer.receive(anyLong())).thenReturn(responseMsg);
		when(responseMsg.getText()).thenReturn("encodedResponse");
		when(cryptographer.decodeMessage("encodedResponse", peerPublicKey)).thenReturn(decoded);
		doNothing().when(consumer).close();
		doNothing().when(producer).close();
		
		try {
			testingObject.sendRequestAndReturnResponse(session, advResponse, pollRequest);
		} catch (final Exception ex) {
			Assert.assertEquals("Unauthorized message on queue.", ex.getMessage());
			
			verify(session, times(1)).createQueue(anyString());
			verify(session, times(1)).createProducer(requestQueue);
			verify(cryptographer, times(1)).encodeRelayMessage(CoreCommonConstants.RELAY_MESSAGE_TYPE_GSD_POLL, "1234", pollRequest, peerPublicKey);
			verify(session, times(1)).createTextMessage("encoded");
			verify(producer, times(1)).send(requestMsg);
			verify(consumer, times(1)).receive(anyLong());
			verify(responseMsg, times(1)).getText();
			verify(cryptographer, times(1)).decodeMessage("encodedResponse", peerPublicKey);
			verify(consumer, times(1)).close();
			verify(producer, times(1)).close();
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testSendRequestAndReturnResponseErrorInErrorPayload() throws Exception {
		final Session session = Mockito.mock(Session.class);
		final Queue requestQueue = Mockito.mock(Queue.class);
		final MessageConsumer consumer = Mockito.mock(MessageConsumer.class);
		final MessageProducer producer = Mockito.mock(MessageProducer.class);
		final PublicKey peerPublicKey = getTestPublicKey();
		final GeneralAdvertisementResult advResponse = new GeneralAdvertisementResult(consumer, "peer-cn", peerPublicKey, "1234");
		final GSDPollRequestDTO pollRequest = new GSDPollRequestDTO();
		final TextMessage requestMsg = Mockito.mock(TextMessage.class);
		final TextMessage responseMsg = Mockito.mock(TextMessage.class);
		final DecryptedMessageDTO decoded = new DecryptedMessageDTO();
		decoded.setMessageType(CoreCommonConstants.RELAY_MESSAGE_TYPE_GSD_POLL);
		decoded.setSessionId("1234");
		decoded.setPayload("\"errorCode\"");

		when(session.createQueue(anyString())).thenReturn(requestQueue);
		when(session.createProducer(requestQueue)).thenReturn(producer);
		when(cryptographer.encodeRelayMessage(CoreCommonConstants.RELAY_MESSAGE_TYPE_GSD_POLL, "1234", pollRequest, peerPublicKey)).thenReturn("encoded");
		when(session.createTextMessage("encoded")).thenReturn(requestMsg);
		doNothing().when(producer).send(requestMsg);
		when(consumer.receive(anyLong())).thenReturn(responseMsg);
		when(responseMsg.getText()).thenReturn("encodedResponse");
		when(cryptographer.decodeMessage("encodedResponse", peerPublicKey)).thenReturn(decoded);
		doNothing().when(consumer).close();
		doNothing().when(producer).close();
		
		try {
			testingObject.sendRequestAndReturnResponse(session, advResponse, pollRequest);
		} catch (final Exception ex) {
			Assert.assertTrue(ex.getMessage().startsWith("Can't convert payload with type "));
			
			verify(session, times(1)).createQueue(anyString());
			verify(session, times(1)).createProducer(requestQueue);
			verify(cryptographer, times(1)).encodeRelayMessage(CoreCommonConstants.RELAY_MESSAGE_TYPE_GSD_POLL, "1234", pollRequest, peerPublicKey);
			verify(session, times(1)).createTextMessage("encoded");
			verify(producer, times(1)).send(requestMsg);
			verify(consumer, times(1)).receive(anyLong());
			verify(responseMsg, times(1)).getText();
			verify(cryptographer, times(1)).decodeMessage("encodedResponse", peerPublicKey);
			verify(consumer, times(1)).close();
			verify(producer, times(1)).close();
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testSendRequestAndReturnResponseUnknownError() throws Exception {
		final Session session = Mockito.mock(Session.class);
		final Queue requestQueue = Mockito.mock(Queue.class);
		final MessageConsumer consumer = Mockito.mock(MessageConsumer.class);
		final MessageProducer producer = Mockito.mock(MessageProducer.class);
		final PublicKey peerPublicKey = getTestPublicKey();
		final GeneralAdvertisementResult advResponse = new GeneralAdvertisementResult(consumer, "peer-cn", peerPublicKey, "1234");
		final GSDPollRequestDTO pollRequest = new GSDPollRequestDTO();
		final TextMessage requestMsg = Mockito.mock(TextMessage.class);
		final TextMessage responseMsg = Mockito.mock(TextMessage.class);
		final DecryptedMessageDTO decoded = new DecryptedMessageDTO();
		decoded.setMessageType(CoreCommonConstants.RELAY_MESSAGE_TYPE_GSD_POLL);
		decoded.setSessionId("1234");
		decoded.setPayload("{\"errorCode\": 500}");

		when(session.createQueue(anyString())).thenReturn(requestQueue);
		when(session.createProducer(requestQueue)).thenReturn(producer);
		when(cryptographer.encodeRelayMessage(CoreCommonConstants.RELAY_MESSAGE_TYPE_GSD_POLL, "1234", pollRequest, peerPublicKey)).thenReturn("encoded");
		when(session.createTextMessage("encoded")).thenReturn(requestMsg);
		doNothing().when(producer).send(requestMsg);
		when(consumer.receive(anyLong())).thenReturn(responseMsg);
		when(responseMsg.getText()).thenReturn("encodedResponse");
		when(cryptographer.decodeMessage("encodedResponse", peerPublicKey)).thenReturn(decoded);
		doNothing().when(consumer).close();
		doNothing().when(producer).close();
		
		try {
			testingObject.sendRequestAndReturnResponse(session, advResponse, pollRequest);
		} catch (final Exception ex) {
			Assert.assertTrue(ex.getMessage().startsWith("Unknown error occurred at "));
			
			verify(session, times(1)).createQueue(anyString());
			verify(session, times(1)).createProducer(requestQueue);
			verify(cryptographer, times(1)).encodeRelayMessage(CoreCommonConstants.RELAY_MESSAGE_TYPE_GSD_POLL, "1234", pollRequest, peerPublicKey);
			verify(session, times(1)).createTextMessage("encoded");
			verify(producer, times(1)).send(requestMsg);
			verify(consumer, times(1)).receive(anyLong());
			verify(responseMsg, times(1)).getText();
			verify(cryptographer, times(1)).decodeMessage("encodedResponse", peerPublicKey);
			verify(consumer, times(1)).close();
			verify(producer, times(1)).close();
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = BadPayloadException.class)
	public void testSendRequestAndReturnResponseHandleErrorResponseOk() throws Exception {
		final Session session = Mockito.mock(Session.class);
		final Queue requestQueue = Mockito.mock(Queue.class);
		final MessageConsumer consumer = Mockito.mock(MessageConsumer.class);
		final MessageProducer producer = Mockito.mock(MessageProducer.class);
		final PublicKey peerPublicKey = getTestPublicKey();
		final GeneralAdvertisementResult advResponse = new GeneralAdvertisementResult(consumer, "peer-cn", peerPublicKey, "1234");
		final GSDPollRequestDTO pollRequest = new GSDPollRequestDTO();
		final TextMessage requestMsg = Mockito.mock(TextMessage.class);
		final TextMessage responseMsg = Mockito.mock(TextMessage.class);
		final DecryptedMessageDTO decoded = new DecryptedMessageDTO();
		decoded.setMessageType(CoreCommonConstants.RELAY_MESSAGE_TYPE_GSD_POLL);
		decoded.setSessionId("1234");
		decoded.setPayload("{\"errorCode\": 400, \"exceptionType\": \"BAD_PAYLOAD\", \"errorMessage\": \"test\"}");

		when(session.createQueue(anyString())).thenReturn(requestQueue);
		when(session.createProducer(requestQueue)).thenReturn(producer);
		when(cryptographer.encodeRelayMessage(CoreCommonConstants.RELAY_MESSAGE_TYPE_GSD_POLL, "1234", pollRequest, peerPublicKey)).thenReturn("encoded");
		when(session.createTextMessage("encoded")).thenReturn(requestMsg);
		doNothing().when(producer).send(requestMsg);
		when(consumer.receive(anyLong())).thenReturn(responseMsg);
		when(responseMsg.getText()).thenReturn("encodedResponse");
		when(cryptographer.decodeMessage("encodedResponse", peerPublicKey)).thenReturn(decoded);
		doNothing().when(consumer).close();
		doNothing().when(producer).close();
		
		try {
			testingObject.sendRequestAndReturnResponse(session, advResponse, pollRequest);
		} catch (final Exception ex) {
			Assert.assertEquals("test", ex.getMessage());
			
			verify(session, times(1)).createQueue(anyString());
			verify(session, times(1)).createProducer(requestQueue);
			verify(cryptographer, times(1)).encodeRelayMessage(CoreCommonConstants.RELAY_MESSAGE_TYPE_GSD_POLL, "1234", pollRequest, peerPublicKey);
			verify(session, times(1)).createTextMessage("encoded");
			verify(producer, times(1)).send(requestMsg);
			verify(consumer, times(1)).receive(anyLong());
			verify(responseMsg, times(1)).getText();
			verify(cryptographer, times(1)).decodeMessage("encodedResponse", peerPublicKey);
			verify(consumer, times(1)).close();
			verify(producer, times(1)).close();
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testSendRequestAndReturnResponseInvalidResponsePayload() throws Exception {
		final Session session = Mockito.mock(Session.class);
		final Queue requestQueue = Mockito.mock(Queue.class);
		final MessageConsumer consumer = Mockito.mock(MessageConsumer.class);
		final MessageProducer producer = Mockito.mock(MessageProducer.class);
		final PublicKey peerPublicKey = getTestPublicKey();
		final GeneralAdvertisementResult advResponse = new GeneralAdvertisementResult(consumer, "peer-cn", peerPublicKey, "1234");
		final GSDPollRequestDTO pollRequest = new GSDPollRequestDTO();
		final TextMessage requestMsg = Mockito.mock(TextMessage.class);
		final TextMessage responseMsg = Mockito.mock(TextMessage.class);
		final DecryptedMessageDTO decoded = new DecryptedMessageDTO();
		decoded.setMessageType(CoreCommonConstants.RELAY_MESSAGE_TYPE_GSD_POLL);
		decoded.setSessionId("1234");
		decoded.setPayload("invalid");

		when(session.createQueue(anyString())).thenReturn(requestQueue);
		when(session.createProducer(requestQueue)).thenReturn(producer);
		when(cryptographer.encodeRelayMessage(CoreCommonConstants.RELAY_MESSAGE_TYPE_GSD_POLL, "1234", pollRequest, peerPublicKey)).thenReturn("encoded");
		when(session.createTextMessage("encoded")).thenReturn(requestMsg);
		doNothing().when(producer).send(requestMsg);
		when(consumer.receive(anyLong())).thenReturn(responseMsg);
		when(responseMsg.getText()).thenReturn("encodedResponse");
		when(cryptographer.decodeMessage("encodedResponse", peerPublicKey)).thenReturn(decoded);
		doNothing().when(consumer).close();
		doNothing().when(producer).close();
		
		try {
			testingObject.sendRequestAndReturnResponse(session, advResponse, pollRequest);
		} catch (final Exception ex) {
			Assert.assertTrue(ex.getMessage().startsWith("Can't convert payload with type "));
			
			verify(session, times(1)).createQueue(anyString());
			verify(session, times(1)).createProducer(requestQueue);
			verify(cryptographer, times(1)).encodeRelayMessage(CoreCommonConstants.RELAY_MESSAGE_TYPE_GSD_POLL, "1234", pollRequest, peerPublicKey);
			verify(session, times(1)).createTextMessage("encoded");
			verify(producer, times(1)).send(requestMsg);
			verify(consumer, times(1)).receive(anyLong());
			verify(responseMsg, times(1)).getText();
			verify(cryptographer, times(1)).decodeMessage("encodedResponse", peerPublicKey);
			verify(consumer, times(1)).close();
			verify(producer, times(1)).close();
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testSendRequestAndReturnResponseOk() throws Exception {
		final Session session = Mockito.mock(Session.class);
		final Queue requestQueue = Mockito.mock(Queue.class);
		final MessageConsumer consumer = Mockito.mock(MessageConsumer.class);
		final MessageProducer producer = Mockito.mock(MessageProducer.class);
		final PublicKey peerPublicKey = getTestPublicKey();
		final GeneralAdvertisementResult advResponse = new GeneralAdvertisementResult(consumer, "peer-cn", peerPublicKey, "1234");
		final GSDPollRequestDTO pollRequest = new GSDPollRequestDTO();
		final TextMessage requestMsg = Mockito.mock(TextMessage.class);
		final TextMessage responseMsg = Mockito.mock(TextMessage.class);
		final DecryptedMessageDTO decoded = new DecryptedMessageDTO();
		decoded.setMessageType(CoreCommonConstants.RELAY_MESSAGE_TYPE_GSD_POLL);
		decoded.setSessionId("1234");
		decoded.setPayload("{\"requiredServiceDefinition\": \"service\"}");

		when(session.createQueue(anyString())).thenReturn(requestQueue);
		when(session.createProducer(requestQueue)).thenReturn(producer);
		when(cryptographer.encodeRelayMessage(CoreCommonConstants.RELAY_MESSAGE_TYPE_GSD_POLL, "1234", pollRequest, peerPublicKey)).thenReturn("encoded");
		when(session.createTextMessage("encoded")).thenReturn(requestMsg);
		doNothing().when(producer).send(requestMsg);
		when(consumer.receive(anyLong())).thenReturn(responseMsg);
		when(responseMsg.getText()).thenReturn("encodedResponse");
		when(cryptographer.decodeMessage("encodedResponse", peerPublicKey)).thenReturn(decoded);
		doNothing().when(consumer).close();
		doNothing().when(producer).close();
		
		final GatekeeperRelayResponse result = testingObject.sendRequestAndReturnResponse(session, advResponse, pollRequest);
		
		Assert.assertEquals("1234", result.getSessionId());
		Assert.assertEquals(CoreCommonConstants.RELAY_MESSAGE_TYPE_GSD_POLL, result.getMessageType());
		Assert.assertEquals("service", result.getGSDPollResponse().getRequiredServiceDefinition());
		
		verify(session, times(1)).createQueue(anyString());
		verify(session, times(1)).createProducer(requestQueue);
		verify(cryptographer, times(1)).encodeRelayMessage(CoreCommonConstants.RELAY_MESSAGE_TYPE_GSD_POLL, "1234", pollRequest, peerPublicKey);
		verify(session, times(1)).createTextMessage("encoded");
		verify(producer, times(1)).send(requestMsg);
		verify(consumer, times(1)).receive(anyLong());
		verify(responseMsg, times(1)).getText();
		verify(cryptographer, times(1)).decodeMessage("encodedResponse", peerPublicKey);
		verify(consumer, times(1)).close();
		verify(producer, times(1)).close();
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testDestroyStaleQueuesAndConnectionsOk() throws JMSException {
		final ActiveMQSession sessionA = Mockito.mock(ActiveMQSession.class);
		final ActiveMQConnection connectionA = Mockito.mock(ActiveMQConnection.class);
		final DestinationSource destinationSourceA = new DestinationSource(connectionA);
		final ActiveMQQueue queueA = Mockito.mock(ActiveMQQueue.class);
		final ActiveMQSession sessionB = Mockito.mock(ActiveMQSession.class);
		final ActiveMQConnection connectionB = Mockito.mock(ActiveMQConnection.class);
		final DestinationSource destinationSourceB = new DestinationSource(connectionB);
		final ActiveMQQueue queueB1 = Mockito.mock(ActiveMQQueue.class);
		final ActiveMQQueue queueB2 = Mockito.mock(ActiveMQQueue.class);
		destinationSourceB.getQueues().add(queueB2);
		final Connection connectionC = Mockito.mock(Connection.class);
		
		
		staleQueues.put(sessionA, List.of(queueA));
		staleQueues.put(sessionB, List.of(queueB1, queueB2));
		staleConnections.add(connectionC);
		
		when(sessionA.getConnection()).thenReturn(connectionA);
		when(connectionA.getDestinationSource()).thenReturn(destinationSourceA);
		when(sessionB.getConnection()).thenReturn(connectionB);
		when(connectionB.getDestinationSource()).thenReturn(destinationSourceB);
		doThrow(new JMSException("test")).when(connectionB).destroyDestination(eq(queueB2));
		
		testingObject.destroyStaleQueuesAndConnections();
		
		assertFalse(staleQueues.containsKey(sessionA));
		assertTrue(staleQueues.containsKey(sessionB));
		assertFalse(staleQueues.get(sessionB).contains(queueB1));
		assertTrue(staleQueues.get(sessionB).contains(queueB2));
		assertFalse(staleConnections.contains(connectionA));
		assertFalse(staleConnections.contains(connectionB));
		assertFalse(staleConnections.contains(connectionC));
	}
	
	//=================================================================================================
	// assistant methods
	
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
}