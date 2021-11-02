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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

import javax.jms.BytesMessage;
import javax.jms.CompletionListener;
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

import org.apache.activemq.command.ActiveMQTextMessage;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import eu.arrowhead.common.SSLProperties;
import eu.arrowhead.common.dto.internal.GeneralAdvertisementMessageDTO;

public class GatekeeperRelayClientUsingCachedSessionsTest {
	
	//=================================================================================================
	// methods
	
	private GatekeeperRelayClientUsingCachedSessions testingObject;
	private final GatekeeperRelayClient client = Mockito.mock(GatekeeperRelayClient.class);

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Before
	public void setUp() {
		testingObject = new GatekeeperRelayClientUsingCachedSessions("common-name", getTestPublicKey(), getTestPrivateKey(), new SSLProperties(), 60000);
		ReflectionTestUtils.setField(testingObject, "client", client);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorServerCommonNameNull() {
		try {
			new GatekeeperRelayClientUsingCachedSessions(null, null, null, null, 0);
		} catch (final Exception ex) {
			Assert.assertEquals("Common name is null or blank.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorServerCommonNameEmpty() {
		try {
			new GatekeeperRelayClientUsingCachedSessions("", null, null, null, 0);
		} catch (final Exception ex) {
			Assert.assertEquals("Common name is null or blank.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorPublicKeyNull() {
		try {
			new GatekeeperRelayClientUsingCachedSessions("common-name", null, null, null, 0);
		} catch (final Exception ex) {
			Assert.assertEquals("Public key is null.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorPrivateKeyNull() {
		try {
			new GatekeeperRelayClientUsingCachedSessions("common-name", getTestPublicKey(), null, null, 0);
		} catch (final Exception ex) {
			Assert.assertEquals("Private key is null.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorSSLPropertiesNull() {
		try {
			new GatekeeperRelayClientUsingCachedSessions("common-name", getTestPublicKey(), getTestPrivateKey(), null, 0);
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
	@SuppressWarnings("unchecked")
	@Test
	public void testCreateConnectionNewConnection() throws Exception {
		when(client.createConnection("localhost", 1234, true)).thenReturn(getTestSession());
		when(client.isConnectionClosed(any(Session.class))).thenReturn(false);
		
		final ConcurrentMap<String,Session> cache = (ConcurrentMap<String,Session>) ReflectionTestUtils.getField(GatekeeperRelayClientUsingCachedSessions.class, "sessionCache");
		Assert.assertEquals(0, cache.size());
		
		testingObject.createConnection("localhost", 1234, true);
		
		Assert.assertEquals(1, cache.size());
		
		verify(client, times(1)).createConnection("localhost", 1234, true);
		verify(client, times(1)).isConnectionClosed(any(Session.class));
		
		cache.clear();
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("unchecked")
	@Test
	public void testCreateConnectionStoredConnectionClosed() throws Exception {
		when(client.createConnection("localhost", 1234, true)).thenReturn(getTestSession());
		when(client.isConnectionClosed(any(Session.class))).thenReturn(true);
		
		final ConcurrentMap<String,Session> cache = (ConcurrentMap<String,Session>) ReflectionTestUtils.getField(GatekeeperRelayClientUsingCachedSessions.class, "sessionCache");
		final Session storedSession = getTestSession();
		cache.put("localhost:1234", storedSession);
		Assert.assertEquals(1, cache.size());
		
		final Session session = testingObject.createConnection("localhost", 1234, true);
		
		Assert.assertEquals(1, cache.size());
		Assert.assertNotEquals(storedSession, session);
		Assert.assertEquals(cache.get("localhost:1234"), session);
		
		verify(client, times(1)).isConnectionClosed(any(Session.class));
		verify(client, times(1)).createConnection("localhost", 1234, true);
		
		cache.clear();
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("unchecked")
	@Test
	public void testCreateConnectionStoredConnection() throws Exception {
		when(client.isConnectionClosed(any(Session.class))).thenReturn(false);
		
		final ConcurrentMap<String,Session> cache = (ConcurrentMap<String,Session>) ReflectionTestUtils.getField(GatekeeperRelayClientUsingCachedSessions.class, "sessionCache");
		final Session storedSession = getTestSession();
		cache.put("localhost:1234", storedSession);
		Assert.assertEquals(1, cache.size());
		
		final Session session = testingObject.createConnection("localhost", 1234, true);
		
		Assert.assertEquals(1, cache.size());
		Assert.assertEquals(storedSession, session);
		
		verify(client, times(1)).isConnectionClosed(any(Session.class));
		verify(client, never()).createConnection("localhost", 1234, true);
		
		cache.clear();
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCloseConnectionSessionNull() {
		testingObject.closeConnection(null);
		
		verify(client, never()).closeConnection(any(Session.class));
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("unchecked")
	@Test
	public void testCloseConnection() throws Exception {
		doNothing().when(client).closeConnection(any(Session.class));
		
		final ConcurrentMap<String,Session> cache = (ConcurrentMap<String,Session>) ReflectionTestUtils.getField(GatekeeperRelayClientUsingCachedSessions.class, "sessionCache");
		final Session storedSession = getTestSession();
		cache.put("localhost:1234", storedSession);
		Assert.assertEquals(1, cache.size());
		
		testingObject.closeConnection(storedSession);
		
		Assert.assertEquals(0, cache.size());

		verify(client, times(1)).closeConnection(any(Session.class));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testIsConnectionClosed() {
		when(client.isConnectionClosed(any(Session.class))).thenReturn(false);
		
		final Session testSession = getTestSession();
		testingObject.isConnectionClosed(testSession);
		
		verify(client, times(1)).isConnectionClosed(testSession);
	}
 	
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testSubscribeGeneralAdvertisementTopic() throws JMSException {
		when(client.subscribeGeneralAdvertisementTopic(any(Session.class))).thenReturn(null);
		
		final Session testSession = getTestSession();
		testingObject.subscribeGeneralAdvertisementTopic(testSession);
		
		verify(client, times(1)).subscribeGeneralAdvertisementTopic(testSession);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetGeneralAdvertisementMessage() throws JMSException {
		when(client.getGeneralAdvertisementMessage(any(Message.class))).thenReturn(null);
		
		final ActiveMQTextMessage msg = new ActiveMQTextMessage();
		testingObject.getGeneralAdvertisementMessage(msg);
		
		verify(client, times(1)).getGeneralAdvertisementMessage(msg);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testSendAcknowledgementAndReturnRequest() throws JMSException {
		when(client.sendAcknowledgementAndReturnRequest(any(Session.class), any(GeneralAdvertisementMessageDTO.class))).thenReturn(null);
		
		final Session testSession = getTestSession();
		final GeneralAdvertisementMessageDTO msgDTO = new GeneralAdvertisementMessageDTO();
		testingObject.sendAcknowledgementAndReturnRequest(testSession, msgDTO);
		
		verify(client, times(1)).sendAcknowledgementAndReturnRequest(testSession, msgDTO);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testSendResponse() throws JMSException {
		doNothing().when(client).sendResponse(any(Session.class), any(GatekeeperRelayRequest.class), any(Object.class));
		
		final Session testSession = getTestSession();
		final Object payload = new Object();
		final GatekeeperRelayRequest request = new GatekeeperRelayRequest(getTestMessageProducer(), getTestPublicKey(), "1234", "type", payload);
		testingObject.sendResponse(testSession, request, payload);
		
		verify(client, times(1)).sendResponse(testSession, request, payload);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testPublishGeneralAdvertisement() throws JMSException {
		when(client.publishGeneralAdvertisement(any(Session.class), anyString(), anyString())).thenReturn(null);
		
		final Session testSession = getTestSession();
		testingObject.publishGeneralAdvertisement(testSession, "recipient", "abcd");
		
		verify(client, times(1)).publishGeneralAdvertisement(testSession, "recipient", "abcd");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testSendRequestAndReturnResponse() throws JMSException {
		when(client.sendRequestAndReturnResponse(any(Session.class), any(GeneralAdvertisementResult.class), any(Object.class))).thenReturn(null);
		
		final Session testSession = getTestSession();
		final GeneralAdvertisementResult result = new GeneralAdvertisementResult(getTestMessageConsumer(), "peer-cn", getTestPublicKey(), "sessionId");
		final Object payload = new Object();
		testingObject.sendRequestAndReturnResponse(testSession, result, payload);
		
		verify(client, times(1)).sendRequestAndReturnResponse(testSession, result, payload);
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("unchecked")
	@Test
	public void testGetCachedSessions() {
		final ConcurrentMap<String,Session> cache = (ConcurrentMap<String,Session>) ReflectionTestUtils.getField(GatekeeperRelayClientUsingCachedSessions.class, "sessionCache");
		final Session storedSession = getTestSession();
		cache.put("localhost:1234", storedSession);
		
		final List<Session> resultList = testingObject.getCachedSessions();
		
		Assert.assertEquals(1, resultList.size());
		Assert.assertEquals(storedSession, resultList.get(0));
		
		cache.clear();
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
	public Session getTestSession() {
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
}