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

package eu.arrowhead.core.gateway.thread;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.util.concurrent.ConcurrentHashMap;
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
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.SSLProperties;
import eu.arrowhead.common.dto.internal.GatewayProviderConnectionRequestDTO;
import eu.arrowhead.common.dto.internal.RelayRequestDTO;
import eu.arrowhead.common.dto.internal.RelayType;
import eu.arrowhead.common.dto.shared.CloudRequestDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.relay.gateway.GatewayRelayClient;

@RunWith(SpringRunner.class)
public class ProviderSideSocketThreadHandlerTest {
	
	//=================================================================================================
	// members
	
	private static final String simpleRequest = "DELETE / HTTP/1.1\r\n" + 
												"Accept: text/plain\r\n" + 
												"User-Agent: Apache-HttpClient/4.5.8 (Java/11.0.3)\r\n" + 
												"\r\n";
	
	private ApplicationContext appContext;
	private GatewayRelayClient relayClient;
	
	private ProviderSideSocketThreadHandler testingObject;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Before
	public void setUp() {
		relayClient = mock(GatewayRelayClient.class, "relayClient");
		appContext = mock(ApplicationContext.class, "appContext");
		
		when(relayClient.isConnectionClosed(any(Session.class))).thenReturn(false);
		when(appContext.getBean(CoreCommonConstants.GATEWAY_ACTIVE_SESSION_MAP, ConcurrentHashMap.class)).thenReturn(new ConcurrentHashMap<>());
		when(appContext.getBean(CoreCommonConstants.GATEWAY_ACTIVE_PROVIDER_SIDE_SOCKET_THREAD_HANDLER_MAP, ConcurrentHashMap.class)).thenReturn(new ConcurrentHashMap<>());
		when(appContext.getBean(SSLProperties.class)).thenReturn(getTestSSLPropertiesForThread());
		
		final GatewayProviderConnectionRequestDTO connectionRequest = getTestGatewayProviderConnectionRequestDTO();
		testingObject = new ProviderSideSocketThreadHandler(appContext, relayClient, getTestSession(), connectionRequest, 60000, 3);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorAppContextNull() {
		new ProviderSideSocketThreadHandler(null, null, null, null, 0, 0);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorRelayClientNull() {
		new ProviderSideSocketThreadHandler(appContext, null, null, null, 0, 0);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorRelaySessionNull() {
		new ProviderSideSocketThreadHandler(appContext, relayClient, null, null, 0, 0);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorRelaySessionClosed() {
		when(relayClient.isConnectionClosed(any(Session.class))).thenReturn(true);

		new ProviderSideSocketThreadHandler(appContext, relayClient, getTestSession(), null, 0, 0);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorConnectionRequestNull() {
		when(relayClient.isConnectionClosed(any(Session.class))).thenReturn(false);
		
		new ProviderSideSocketThreadHandler(appContext, relayClient, getTestSession(), null, 0, 0);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorConnectionRequestProviderNull() {
		when(relayClient.isConnectionClosed(any(Session.class))).thenReturn(false);
		
		final GatewayProviderConnectionRequestDTO connectionRequest = getTestGatewayProviderConnectionRequestDTO();
		connectionRequest.setProvider(null);
		
		new ProviderSideSocketThreadHandler(appContext, relayClient, getTestSession(), connectionRequest, 0, 0);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorConnectionRequestProviderNameNull() {
		when(relayClient.isConnectionClosed(any(Session.class))).thenReturn(false);
		
		final GatewayProviderConnectionRequestDTO connectionRequest = getTestGatewayProviderConnectionRequestDTO();
		connectionRequest.getProvider().setSystemName(null);
		
		new ProviderSideSocketThreadHandler(appContext, relayClient, getTestSession(), connectionRequest, 0, 0);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorConnectionRequestProviderNameEmpty() {
		when(relayClient.isConnectionClosed(any(Session.class))).thenReturn(false);
		
		final GatewayProviderConnectionRequestDTO connectionRequest = getTestGatewayProviderConnectionRequestDTO();
		connectionRequest.getProvider().setSystemName(" ");
		
		new ProviderSideSocketThreadHandler(appContext, relayClient, getTestSession(), connectionRequest, 0, 0);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorConnectionRequestProviderAddressNull() {
		when(relayClient.isConnectionClosed(any(Session.class))).thenReturn(false);
		
		final GatewayProviderConnectionRequestDTO connectionRequest = getTestGatewayProviderConnectionRequestDTO();
		connectionRequest.getProvider().setAddress(null);
		
		new ProviderSideSocketThreadHandler(appContext, relayClient, getTestSession(), connectionRequest, 0, 0);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorConnectionRequestProviderAddressEmpty() {
		when(relayClient.isConnectionClosed(any(Session.class))).thenReturn(false);
		
		final GatewayProviderConnectionRequestDTO connectionRequest = getTestGatewayProviderConnectionRequestDTO();
		connectionRequest.getProvider().setAddress("\r\n");
		
		new ProviderSideSocketThreadHandler(appContext, relayClient, getTestSession(), connectionRequest, 0, 0);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorConnectionRequestProviderPortNull() {
		when(relayClient.isConnectionClosed(any(Session.class))).thenReturn(false);
		
		final GatewayProviderConnectionRequestDTO connectionRequest = getTestGatewayProviderConnectionRequestDTO();
		connectionRequest.getProvider().setPort(null);
		
		new ProviderSideSocketThreadHandler(appContext, relayClient, getTestSession(), connectionRequest, 0, 0);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorConnectionRequestProviderPortTooLow() {
		when(relayClient.isConnectionClosed(any(Session.class))).thenReturn(false);
		
		final GatewayProviderConnectionRequestDTO connectionRequest = getTestGatewayProviderConnectionRequestDTO();
		connectionRequest.getProvider().setPort(-2);
		
		new ProviderSideSocketThreadHandler(appContext, relayClient, getTestSession(), connectionRequest, 0, 0);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorConnectionRequestProviderPortTooHigh() {
		when(relayClient.isConnectionClosed(any(Session.class))).thenReturn(false);
		
		final GatewayProviderConnectionRequestDTO connectionRequest = getTestGatewayProviderConnectionRequestDTO();
		connectionRequest.getProvider().setPort(1111111);
		
		new ProviderSideSocketThreadHandler(appContext, relayClient, getTestSession(), connectionRequest, 0, 0);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorConnectionRequestProviderAuthenticationInfoNull() {
		when(relayClient.isConnectionClosed(any(Session.class))).thenReturn(false);
		
		final GatewayProviderConnectionRequestDTO connectionRequest = getTestGatewayProviderConnectionRequestDTO();
		connectionRequest.getProvider().setAuthenticationInfo(null);
		
		new ProviderSideSocketThreadHandler(appContext, relayClient, getTestSession(), connectionRequest, 0, 0);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorConnectionRequestProviderAuthenticationInfoEmpty() {
		when(relayClient.isConnectionClosed(any(Session.class))).thenReturn(false);
		
		final GatewayProviderConnectionRequestDTO connectionRequest = getTestGatewayProviderConnectionRequestDTO();
		connectionRequest.getProvider().setAuthenticationInfo("");
		
		new ProviderSideSocketThreadHandler(appContext, relayClient, getTestSession(), connectionRequest, 0, 0);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorConnectionRequestServiceDefinitionNull() {
		when(relayClient.isConnectionClosed(any(Session.class))).thenReturn(false);
		
		final GatewayProviderConnectionRequestDTO connectionRequest = getTestGatewayProviderConnectionRequestDTO();
		connectionRequest.setServiceDefinition(null);
		
		new ProviderSideSocketThreadHandler(appContext, relayClient, getTestSession(), connectionRequest, 0, 0);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorConnectionRequestServiceDefinitionEmpty() {
		when(relayClient.isConnectionClosed(any(Session.class))).thenReturn(false);
		
		final GatewayProviderConnectionRequestDTO connectionRequest = getTestGatewayProviderConnectionRequestDTO();
		connectionRequest.setServiceDefinition("\t\t\t");
		
		new ProviderSideSocketThreadHandler(appContext, relayClient, getTestSession(), connectionRequest, 0, 0);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorConnectionRequestConsumerGWPublicKeyNull() {
		when(relayClient.isConnectionClosed(any(Session.class))).thenReturn(false);
		
		final GatewayProviderConnectionRequestDTO connectionRequest = getTestGatewayProviderConnectionRequestDTO();
		connectionRequest.setConsumerGWPublicKey(null);
		
		new ProviderSideSocketThreadHandler(appContext, relayClient, getTestSession(), connectionRequest, 0, 0);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorConnectionRequestConsumerGWPublicKeyEmpty() {
		when(relayClient.isConnectionClosed(any(Session.class))).thenReturn(false);
		
		final GatewayProviderConnectionRequestDTO connectionRequest = getTestGatewayProviderConnectionRequestDTO();
		connectionRequest.setConsumerGWPublicKey(" ");
		
		new ProviderSideSocketThreadHandler(appContext, relayClient, getTestSession(), connectionRequest, 0, 0);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testConstructorOk() {
		verify(appContext, times(1)).getBean(SSLProperties.class);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testInitQueueIdNull() {
		testingObject.init(null, getTestMessageProducer(), getTestMessageProducer(), getTestMessageConsumer(), getTestMessageConsumer());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testInitQueueIdEmpty() {
		testingObject.init(" ", getTestMessageProducer(), getTestMessageProducer(), getTestMessageConsumer(), getTestMessageConsumer());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testInitSenderNull() {
		testingObject.init("queueId", null, getTestMessageProducer(), getTestMessageConsumer(), getTestMessageConsumer());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testInitSenderControlNull() {
		testingObject.init("queueId", getTestMessageProducer(), null, getTestMessageConsumer(), getTestMessageConsumer());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testInitConsumerNull() {
		testingObject.init("queueId", getTestMessageProducer(), getTestMessageProducer(), null, getTestMessageConsumer());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testInitConsumerControlNull() {
		testingObject.init("queueId", getTestMessageProducer(), getTestMessageProducer(), getTestMessageConsumer(), null);
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitOk() throws InterruptedException {
		Assert.assertTrue(!testingObject.isInitialized());
		
		final boolean[] started = { false };
		new Thread() {
			@Override
			public void run() {
				final SSLProperties props = getTestSSLPropertiesForTestServerThread();
				final SSLContext sslContext = SSLContextFactory.createGatewaySSLContext(props);
				final SSLServerSocketFactory serverSocketFactory = sslContext.getServerSocketFactory();
				try {
					final SSLServerSocket dummyServerSocket = (SSLServerSocket) serverSocketFactory.createServerSocket(22062);
					started[0] = true;
					final Socket socket = dummyServerSocket.accept();
					socket.close();
				} catch (final IOException ex) {
					ex.printStackTrace();
				}
			}
		}.start();

		while (!started[0]) {
			Thread.sleep(1000);
		}
		Thread.sleep(1000);

		final GatewayProviderConnectionRequestDTO connectionRequest = (GatewayProviderConnectionRequestDTO) ReflectionTestUtils.getField(testingObject, "connectionRequest");
		connectionRequest.getProvider().setPort(22062);
		testingObject.init("queueId", getTestMessageProducer(), getTestMessageProducer(), getTestMessageConsumer(), getTestMessageConsumer());
		
		Assert.assertTrue(testingObject.isInitialized());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalStateException.class)
	public void testOnMessageHandlerNotInitialized() {
		testingObject.onMessage(null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testOnMessageCloseControlMessage() throws JMSException {
		ReflectionTestUtils.setField(testingObject, "initialized", true);
		final String queueId = "bla" + GatewayRelayClient.CONTROL_QUEUE_SUFFIX;
		ReflectionTestUtils.setField(testingObject, "queueId", queueId);
		ReflectionTestUtils.setField(testingObject, "sender", getTestMessageProducer());
		ReflectionTestUtils.setField(testingObject, "senderControl", getTestMessageProducer());
		ReflectionTestUtils.setField(testingObject, "consumer", getTestMessageConsumer());
		ReflectionTestUtils.setField(testingObject, "consumerControl", getTestMessageConsumer());
		@SuppressWarnings("unchecked")
		final ConcurrentMap<String,ProviderSideSocketThreadHandler> activeProviderSideSocketThreadHandlers = (ConcurrentMap<String,ProviderSideSocketThreadHandler>) ReflectionTestUtils.getField(testingObject, "activeProviderSideSocketThreadHandlers");
		activeProviderSideSocketThreadHandlers.put(queueId, testingObject);
		final ActiveMQTextMessage message = new ActiveMQTextMessage();
		message.setJMSDestination(new ActiveMQQueue(queueId));
		
		doNothing().when(relayClient).handleCloseControlMessage(any(Message.class), any(Session.class));
		doNothing().when(relayClient).closeConnection(any(Session.class));
		when(relayClient.destroyQueues(any(Session.class), any(MessageProducer.class), any(MessageProducer.class))).thenReturn(true);
		when(relayClient.isConnectionClosed(any(Session.class))).thenReturn(true);
		
		testingObject.onMessage(message);
		
		verify(relayClient, times(1)).unsubscribeFromQueues(any(MessageConsumer.class), any(MessageConsumer.class));
		verify(relayClient, times(1)).closeConnection(any(Session.class));
		Assert.assertFalse(activeProviderSideSocketThreadHandlers.containsKey(queueId));
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("unchecked")
	@Test
	public void testOnMessageCloseControlMessageNotCloseable() throws JMSException {
		ReflectionTestUtils.setField(testingObject, "initialized", true);
		final String queueId = "bla" + GatewayRelayClient.CONTROL_QUEUE_SUFFIX;
		ReflectionTestUtils.setField(testingObject, "queueId", queueId);
		ReflectionTestUtils.setField(testingObject, "sender", getTestMessageProducer());
		ReflectionTestUtils.setField(testingObject, "senderControl", getTestMessageProducer());
		ReflectionTestUtils.setField(testingObject, "consumer", getTestMessageConsumer());
		ReflectionTestUtils.setField(testingObject, "consumerControl", getTestMessageConsumer());
		final ConcurrentMap<String,ProviderSideSocketThreadHandler> activeProviderSideSocketThreadHandlers = (ConcurrentMap<String,ProviderSideSocketThreadHandler>) ReflectionTestUtils.getField(testingObject, "activeProviderSideSocketThreadHandlers");
		activeProviderSideSocketThreadHandlers.put(queueId, testingObject);
		final ActiveMQTextMessage message = new ActiveMQTextMessage();
		message.setJMSDestination(new ActiveMQQueue(queueId));
		
		doNothing().when(relayClient).handleCloseControlMessage(any(Message.class), any(Session.class));
		doNothing().when(relayClient).closeConnection(any(Session.class));
		when(relayClient.destroyQueues(any(Session.class), any(MessageProducer.class), any(MessageProducer.class))).thenReturn(false);
		when(relayClient.isConnectionClosed(any(Session.class))).thenReturn(false);
		
		testingObject.onMessage(message);
		
		verify(relayClient, times(1)).unsubscribeFromQueues(any(MessageConsumer.class), any(MessageConsumer.class));
		verify(relayClient, never()).closeConnection(any(Session.class));
		Assert.assertTrue(activeProviderSideSocketThreadHandlers.containsKey(queueId));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testOnMessageExceptionThrown() throws JMSException {
		ReflectionTestUtils.setField(testingObject, "initialized", true);
		final String queueId = "bla" + GatewayRelayClient.CONTROL_QUEUE_SUFFIX;
		ReflectionTestUtils.setField(testingObject, "queueId", queueId);
		ReflectionTestUtils.setField(testingObject, "sender", getTestMessageProducer());
		ReflectionTestUtils.setField(testingObject, "senderControl", getTestMessageProducer());
		ReflectionTestUtils.setField(testingObject, "consumer", getTestMessageConsumer());
		ReflectionTestUtils.setField(testingObject, "consumerControl", getTestMessageConsumer());
		@SuppressWarnings("unchecked")
		final ConcurrentMap<String,ProviderSideSocketThreadHandler> activeProviderSideSocketThreadHandlers = (ConcurrentMap<String,ProviderSideSocketThreadHandler>) ReflectionTestUtils.getField(testingObject, "activeProviderSideSocketThreadHandlers");
		activeProviderSideSocketThreadHandlers.put(queueId, testingObject);
		final ActiveMQTextMessage message = new ActiveMQTextMessage();
		message.setJMSDestination(new ActiveMQQueue(queueId));
		
		doThrow(new JMSException("test")).when(relayClient).handleCloseControlMessage(any(Message.class), any(Session.class));
		when(relayClient.destroyQueues(any(Session.class), any(MessageProducer.class), any(MessageProducer.class))).thenReturn(true);
		when(relayClient.isConnectionClosed(any(Session.class))).thenReturn(true);
		
		testingObject.onMessage(message);
		
		verify(relayClient, times(1)).unsubscribeFromQueues(any(MessageConsumer.class), any(MessageConsumer.class));
		verify(relayClient, times(1)).closeConnection(any(Session.class));
		Assert.assertFalse(activeProviderSideSocketThreadHandlers.containsKey(queueId));
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("unchecked")
	@Test
	public void testOnMessageExceptionThrownNotCloseable() throws JMSException {
		ReflectionTestUtils.setField(testingObject, "initialized", true);
		final String queueId = "bla" + GatewayRelayClient.CONTROL_QUEUE_SUFFIX;
		ReflectionTestUtils.setField(testingObject, "queueId", queueId);
		ReflectionTestUtils.setField(testingObject, "sender", getTestMessageProducer());
		ReflectionTestUtils.setField(testingObject, "senderControl", getTestMessageProducer());
		ReflectionTestUtils.setField(testingObject, "consumer", getTestMessageConsumer());
		ReflectionTestUtils.setField(testingObject, "consumerControl", getTestMessageConsumer());
		final ConcurrentMap<String,ProviderSideSocketThreadHandler> activeProviderSideSocketThreadHandlers = (ConcurrentMap<String,ProviderSideSocketThreadHandler>) ReflectionTestUtils.getField(testingObject, "activeProviderSideSocketThreadHandlers");
		activeProviderSideSocketThreadHandlers.put(queueId, testingObject);
		final ActiveMQTextMessage message = new ActiveMQTextMessage();
		message.setJMSDestination(new ActiveMQQueue(queueId));
		
		doThrow(new JMSException("test")).when(relayClient).handleCloseControlMessage(any(Message.class), any(Session.class));
		when(relayClient.destroyQueues(any(Session.class), any(MessageProducer.class), any(MessageProducer.class))).thenReturn(false);
		when(relayClient.isConnectionClosed(any(Session.class))).thenReturn(false);
		
		testingObject.onMessage(message);
		
		verify(relayClient, times(1)).unsubscribeFromQueues(any(MessageConsumer.class), any(MessageConsumer.class));
		verify(relayClient, never()).closeConnection(any(Session.class));
		Assert.assertTrue(activeProviderSideSocketThreadHandlers.containsKey(queueId));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testOnMessageNormalOutputStreamNull() throws JMSException {
		final ProviderSideSocketThread currentThread = mock(ProviderSideSocketThread.class, "currentThread");
		when(currentThread.getOutputStream()).thenReturn(null);
		ReflectionTestUtils.setField(testingObject, "currentThread", currentThread);
		ReflectionTestUtils.setField(testingObject, "initialized", true);
		
		final ActiveMQTextMessage message = new ActiveMQTextMessage();
		message.setJMSDestination(new ActiveMQQueue("bla"));
		
		testingObject.onMessage(message);
		
		verify(currentThread).getOutputStream();
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testOnMessageNormalMessage() throws JMSException {
		final ProviderSideSocketThread currentThread = mock(ProviderSideSocketThread.class, "currentThread");
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(10);
		when(currentThread.getOutputStream()).thenReturn(outputStream);
		ReflectionTestUtils.setField(testingObject, "currentThread", currentThread);
		ReflectionTestUtils.setField(testingObject, "initialized", true);
		
		final ActiveMQTextMessage message = new ActiveMQTextMessage();
		message.setJMSDestination(new ActiveMQQueue("bla"));
		
		when(relayClient.getBytesFromMessage(any(Message.class), any(PublicKey.class))).thenReturn(new byte[] { 10, 9, 8, 7, 6, 5, 4, 3, 2, 1 });
		
		Assert.assertFalse(testingObject.isCommunicationStarted());
		testingObject.onMessage(message);
		
		Assert.assertArrayEquals(new byte[] { 10, 9, 8, 7, 6, 5, 4, 3, 2, 1 }, outputStream.toByteArray());
		Assert.assertTrue(testingObject.isCommunicationStarted());
		verify(currentThread, times(1)).setNowAsLastInteractionTime();
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testOnMessageNormalHttpMessage() throws JMSException {
		final ProviderSideSocketThread currentThread = mock(ProviderSideSocketThread.class, "currentThread");
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(1024);
		when(currentThread.getOutputStream()).thenReturn(outputStream);
		ReflectionTestUtils.setField(testingObject, "currentThread", currentThread);
		ReflectionTestUtils.setField(testingObject, "initialized", true);
		
		final ActiveMQTextMessage message = new ActiveMQTextMessage();
		message.setJMSDestination(new ActiveMQQueue("bla"));
		
		when(relayClient.getBytesFromMessage(any(Message.class), any(PublicKey.class))).thenReturn(simpleRequest.getBytes(StandardCharsets.ISO_8859_1));
		
		int noRequest = (Integer) ReflectionTestUtils.getField(testingObject, "noRequest");
		Assert.assertEquals(0, noRequest);
		Assert.assertFalse(testingObject.isCommunicationStarted());
		
		testingObject.onMessage(message);
		
		final boolean countRequests = (Boolean) ReflectionTestUtils.getField(testingObject, "countRequests");
		noRequest = (Integer) ReflectionTestUtils.getField(testingObject, "noRequest");
		
		Assert.assertTrue(countRequests);
		Assert.assertEquals(1, noRequest);
		Assert.assertEquals(simpleRequest, new String(outputStream.toByteArray(), StandardCharsets.ISO_8859_1));
		Assert.assertTrue(testingObject.isCommunicationStarted());
		verify(currentThread, times(1)).setNowAsLastInteractionTime();
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testOnMessageNormalHttpMessageThreadChange() throws JMSException, UnknownHostException, IOException {
		final ProviderSideSocketThread currentThread = mock(ProviderSideSocketThread.class, "currentThread");
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(1024);
		when(currentThread.getOutputStream()).thenReturn(outputStream);
		ReflectionTestUtils.setField(testingObject, "currentThread", currentThread);
		ReflectionTestUtils.setField(testingObject, "sender", getTestMessageProducer());
		ReflectionTestUtils.setField(testingObject, "initialized", true);
		
		
		final SSLSocketFactory socketFactory = mock(SSLSocketFactory.class, "socketFactory");
		when(socketFactory.createSocket(anyString(), anyInt())).thenReturn(getDummySSLSocket());
		ReflectionTestUtils.setField(testingObject, "socketFactory", socketFactory);
		
		final ActiveMQTextMessage message = new ActiveMQTextMessage();
		message.setJMSDestination(new ActiveMQQueue("bla"));
		
		when(relayClient.getBytesFromMessage(any(Message.class), any(PublicKey.class))).thenReturn(simpleRequest.getBytes(StandardCharsets.ISO_8859_1));
		
		int noRequest = (Integer) ReflectionTestUtils.getField(testingObject, "noRequest");
		Assert.assertEquals(0, noRequest);
		Assert.assertFalse(testingObject.isCommunicationStarted());
		
		testingObject.onMessage(message);
		final boolean countRequests = (Boolean) ReflectionTestUtils.getField(testingObject, "countRequests");
		Assert.assertTrue(countRequests);
		noRequest = (Integer) ReflectionTestUtils.getField(testingObject, "noRequest");
		Assert.assertEquals(1, noRequest);
		Assert.assertTrue(testingObject.isCommunicationStarted());
		
		testingObject.onMessage(message);
		noRequest = (Integer) ReflectionTestUtils.getField(testingObject, "noRequest");
		Assert.assertEquals(2, noRequest);
		Assert.assertTrue(testingObject.isCommunicationStarted());
		
		testingObject.onMessage(message);
		noRequest = (Integer) ReflectionTestUtils.getField(testingObject, "noRequest");
		Assert.assertEquals(3, noRequest);
		Assert.assertTrue(testingObject.isCommunicationStarted());
		
		testingObject.onMessage(message);
		noRequest = (Integer) ReflectionTestUtils.getField(testingObject, "noRequest");
		Assert.assertEquals(1, noRequest);
		
		final ProviderSideSocketThread oldThread = (ProviderSideSocketThread) ReflectionTestUtils.getField(testingObject, "oldThread");
		Assert.assertNotNull(oldThread);
		Assert.assertEquals(currentThread, oldThread);
		Assert.assertTrue(testingObject.isCommunicationStarted());
		verify(currentThread, times(4)).setNowAsLastInteractionTime();
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private Session getTestSession() {
		return new Session() {

			//-------------------------------------------------------------------------------------------------
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
	private GatewayProviderConnectionRequestDTO getTestGatewayProviderConnectionRequestDTO() {
		final RelayRequestDTO relay = new RelayRequestDTO("localhost", 1234, null, false, false, RelayType.GATEWAY_RELAY.name());
		final SystemRequestDTO consumer = new SystemRequestDTO();
		consumer.setSystemName("consumer");
		consumer.setAddress("abc.de");
		consumer.setPort(22001);
		consumer.setAuthenticationInfo("consAuth");
		final SystemRequestDTO provider = new SystemRequestDTO();
		provider.setSystemName("provider");
		provider.setAddress("127.0.0.1");
		provider.setPort(22002);
		provider.setAuthenticationInfo("provAuth");
		final CloudRequestDTO consumerCloud = new CloudRequestDTO();
		consumerCloud.setName("testcloud1");
		consumerCloud.setOperator("aitia");
		final CloudRequestDTO providerCloud = new CloudRequestDTO();
		providerCloud.setName("testcloud2");
		providerCloud.setOperator("elte");
		
		final String publicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAq5Jq4tOeFoLqxOqtYcujbCNZina3iuV9+/o8D1R9D0HvgnmlgPlqWwjDSxV7m7SGJpuc/rRXJ85OzqV3rwRHO8A8YWXiabj8EdgEIyqg4SOgTN7oZ7MQUisTpwtWn9K14se4dHt/YE9mUW4en19p/yPUDwdw3ECMJHamy/O+Mh6rbw6AFhYvz6F5rXYB8svkenOuG8TSBFlRkcjdfqQqtl4xlHgmlDNWpHsQ3eFAO72mKQjm2ZhWI1H9CLrJf1NQs2GnKXgHBOM5ET61fEHWN8axGGoSKfvTed5vhhX7l5uwxM+AKQipLNNKjEaQYnyX3TL9zL8I7y+QkhzDa7/5kQIDAQAB";
		
		return new GatewayProviderConnectionRequestDTO(relay, consumer, provider, consumerCloud, providerCloud, "test-service", publicKey);
	}
	
	//-------------------------------------------------------------------------------------------------
	private MessageProducer getTestMessageProducer() {
		return new MessageProducer() {
			
			//-------------------------------------------------------------------------------------------------
			public void setTimeToLive(long timeToLive) throws JMSException {}
			public void setPriority(int defaultPriority) throws JMSException {}
			public void setDisableMessageTimestamp(boolean value) throws JMSException {}
			public void setDisableMessageID(boolean value) throws JMSException {}
			public void setDeliveryMode(int deliveryMode) throws JMSException {	}
			public void setDeliveryDelay(long deliveryDelay) throws JMSException {}
			public void send(Destination destination, Message message, int deliveryMode, int priority, long timeToLive, CompletionListener completionListener) throws JMSException {}
			public void send(Message message, int deliveryMode, int priority, long timeToLive, CompletionListener completionListener) throws JMSException {}
			public void send(Destination destination, Message message, int deliveryMode, int priority, long timeToLive) throws JMSException {}
			public void send(Message message, int deliveryMode, int priority, long timeToLive) throws JMSException {}
			public void send(Destination destination, Message message, CompletionListener completionListener) throws JMSException {}
			public void send(Message message, CompletionListener completionListener) throws JMSException {}
			public void send(Destination destination, Message message) throws JMSException {}
			public void send(Message message) throws JMSException {}
			public long getTimeToLive() throws JMSException { return 0; }
			public int getPriority() throws JMSException { return 0; }
			public boolean getDisableMessageTimestamp() throws JMSException { return false;	}
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
			public void setMessageListener(final MessageListener listener) throws JMSException {}
			public Message receiveNoWait() throws JMSException { return null; }
			public Message receive(final long timeout) throws JMSException { return null; }
			public Message receive() throws JMSException { return null; }
			public String getMessageSelector() throws JMSException { return null; }
			public MessageListener getMessageListener() throws JMSException { return null; }
			public void close() throws JMSException {}
		};
	}
	
	//-------------------------------------------------------------------------------------------------
	private SSLProperties getTestSSLPropertiesForThread() {
		final SSLProperties sslProps = new SSLProperties();
		ReflectionTestUtils.setField(sslProps, "sslEnabled", true);
		ReflectionTestUtils.setField(sslProps, "keyStoreType", "PKCS12");
		final Resource keystore = new ClassPathResource("certificates/gateway.p12");
		ReflectionTestUtils.setField(sslProps, "keyStore", keystore);
		ReflectionTestUtils.setField(sslProps, "keyStorePassword", "123456");
		final Resource truststore = new ClassPathResource("certificates/truststore.p12");
		ReflectionTestUtils.setField(sslProps, "trustStore", truststore);
		ReflectionTestUtils.setField(sslProps, "trustStorePassword", "123456");
		
		return sslProps;
	}
	
	//-------------------------------------------------------------------------------------------------
	private SSLProperties getTestSSLPropertiesForTestServerThread() {
		final SSLProperties sslProps = new SSLProperties();
		ReflectionTestUtils.setField(sslProps, "sslEnabled", true);
		ReflectionTestUtils.setField(sslProps, "keyStoreType", "PKCS12");
		final Resource keystore = new ClassPathResource("certificates/authorization.p12");
		ReflectionTestUtils.setField(sslProps, "keyStore", keystore);
		ReflectionTestUtils.setField(sslProps, "keyStorePassword", "123456");
		final Resource truststore = new ClassPathResource("certificates/truststore.p12");
		ReflectionTestUtils.setField(sslProps, "trustStore", truststore);
		ReflectionTestUtils.setField(sslProps, "trustStorePassword", "123456");
		
		return sslProps;
	}
	
	//-------------------------------------------------------------------------------------------------
	private SSLSocket getDummySSLSocket() {
		return new SSLSocket() {
			public void startHandshake() throws IOException {}
			public void setWantClientAuth(final boolean arg0) {}
			public void setUseClientMode(final boolean arg0) {}
			public void setNeedClientAuth(final boolean arg0) {}
			public void setEnabledProtocols(final String[] arg0) {}
			public void setEnabledCipherSuites(final String[] arg0) {}
			public void setEnableSessionCreation(final boolean arg0) {}
			public void removeHandshakeCompletedListener(final HandshakeCompletedListener arg0) {}
			public boolean getWantClientAuth() { return false; }
			public boolean getUseClientMode() { return false; }
			public String[] getSupportedProtocols() { return null; }
			public String[] getSupportedCipherSuites() { return null; }
			public SSLSession getSession() { return null; }
			public boolean getNeedClientAuth() { return false; }
			public String[] getEnabledProtocols() { return null; }
			public String[] getEnabledCipherSuites() { return null; }
			public boolean getEnableSessionCreation() { return false; }
			public void addHandshakeCompletedListener(final HandshakeCompletedListener arg0) {} 
			
			@Override
			public OutputStream getOutputStream() throws IOException {
				return new ByteArrayOutputStream(1024);
			}
			
			@Override
			public InputStream getInputStream() throws IOException {
				return new ByteArrayInputStream(new byte[0]);
			}
		};
	}
}