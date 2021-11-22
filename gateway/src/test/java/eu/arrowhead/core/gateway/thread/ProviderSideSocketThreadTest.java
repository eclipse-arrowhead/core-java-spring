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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.UnknownHostException;
import java.security.PublicKey;
import java.time.ZonedDateTime;

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
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.internal.GatewayProviderConnectionRequestDTO;
import eu.arrowhead.common.dto.internal.RelayRequestDTO;
import eu.arrowhead.common.dto.internal.RelayType;
import eu.arrowhead.common.dto.shared.CloudRequestDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.relay.gateway.GatewayRelayClient;

@RunWith(SpringRunner.class)
public class ProviderSideSocketThreadTest {
	
	//=================================================================================================
	// members
	
	private GatewayRelayClient relayClient;
	private SSLSocketFactory socketFactory;
	
	private ProviderSideSocketThread testingObject;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Before
	public void setUp() {
		relayClient = mock(GatewayRelayClient.class, "relayClient");
		socketFactory = mock(SSLSocketFactory.class, "socketFactory");
		
		when(relayClient.isConnectionClosed(any(Session.class))).thenReturn(false);
		
		final GatewayProviderConnectionRequestDTO connectionRequest = getTestGatewayProviderConnectionRequestDTO();
		testingObject = new ProviderSideSocketThread(relayClient, getTestSession(), socketFactory, connectionRequest, getTestConsumerGWPublicKey(), 60000, getTestMessageProducer());
	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorRelayClientNull() {
		new ProviderSideSocketThread(null, null, null, null, null, 0, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorRelaySessionNull() {
		new ProviderSideSocketThread(relayClient, null, null, null, null, 0, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorRelaySessionClosed() {
		when(relayClient.isConnectionClosed(any(Session.class))).thenReturn(true);

		new ProviderSideSocketThread(relayClient, getTestSession(), null, null, null, 0, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorSocketFactoryNull() {
		when(relayClient.isConnectionClosed(any(Session.class))).thenReturn(false);
		
		new ProviderSideSocketThread(relayClient, getTestSession(), null, null, null, 0, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorConsumerGatewayPublicKeyNull() {
		when(relayClient.isConnectionClosed(any(Session.class))).thenReturn(false);
		
		new ProviderSideSocketThread(relayClient, getTestSession(), socketFactory, null, null, 0, null);
	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorSenderNull() {
		when(relayClient.isConnectionClosed(any(Session.class))).thenReturn(false);
		
		new ProviderSideSocketThread(relayClient, getTestSession(), socketFactory, null, getTestConsumerGWPublicKey(), 0, null);
	}

	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorConnectionRequestNull() {
		when(relayClient.isConnectionClosed(any(Session.class))).thenReturn(false);
		
		new ProviderSideSocketThread(relayClient, getTestSession(), socketFactory, null, getTestConsumerGWPublicKey(), 0, getTestMessageProducer());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorConnectionRequestProviderNull() {
		when(relayClient.isConnectionClosed(any(Session.class))).thenReturn(false);
		
		final GatewayProviderConnectionRequestDTO connectionRequest = getTestGatewayProviderConnectionRequestDTO();
		connectionRequest.setProvider(null);
		
		new ProviderSideSocketThread(relayClient, getTestSession(), socketFactory, connectionRequest, getTestConsumerGWPublicKey(), 0, getTestMessageProducer());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorConnectionRequestProviderNameNull() {
		when(relayClient.isConnectionClosed(any(Session.class))).thenReturn(false);
		
		final GatewayProviderConnectionRequestDTO connectionRequest = getTestGatewayProviderConnectionRequestDTO();
		connectionRequest.getProvider().setSystemName(null);
		
		new ProviderSideSocketThread(relayClient, getTestSession(), socketFactory, connectionRequest, getTestConsumerGWPublicKey(), 0, getTestMessageProducer());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorConnectionRequestProviderNameEmpty() {
		when(relayClient.isConnectionClosed(any(Session.class))).thenReturn(false);
		
		final GatewayProviderConnectionRequestDTO connectionRequest = getTestGatewayProviderConnectionRequestDTO();
		connectionRequest.getProvider().setSystemName(" ");
		
		new ProviderSideSocketThread(relayClient, getTestSession(), socketFactory, connectionRequest, getTestConsumerGWPublicKey(), 0, getTestMessageProducer());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorConnectionRequestProviderAddressNull() {
		when(relayClient.isConnectionClosed(any(Session.class))).thenReturn(false);
		
		final GatewayProviderConnectionRequestDTO connectionRequest = getTestGatewayProviderConnectionRequestDTO();
		connectionRequest.getProvider().setAddress(null);
		
		new ProviderSideSocketThread(relayClient, getTestSession(), socketFactory, connectionRequest, getTestConsumerGWPublicKey(), 0, getTestMessageProducer());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorConnectionRequestProviderAddressEmpty() {
		when(relayClient.isConnectionClosed(any(Session.class))).thenReturn(false);
		
		final GatewayProviderConnectionRequestDTO connectionRequest = getTestGatewayProviderConnectionRequestDTO();
		connectionRequest.getProvider().setAddress("\r\n");
		
		new ProviderSideSocketThread(relayClient, getTestSession(), socketFactory, connectionRequest, getTestConsumerGWPublicKey(), 0, getTestMessageProducer());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorConnectionRequestProviderPortNull() {
		when(relayClient.isConnectionClosed(any(Session.class))).thenReturn(false);
		
		final GatewayProviderConnectionRequestDTO connectionRequest = getTestGatewayProviderConnectionRequestDTO();
		connectionRequest.getProvider().setPort(null);
		
		new ProviderSideSocketThread(relayClient, getTestSession(), socketFactory, connectionRequest, getTestConsumerGWPublicKey(), 0, getTestMessageProducer());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorConnectionRequestProviderPortTooLow() {
		when(relayClient.isConnectionClosed(any(Session.class))).thenReturn(false);
		
		final GatewayProviderConnectionRequestDTO connectionRequest = getTestGatewayProviderConnectionRequestDTO();
		connectionRequest.getProvider().setPort(-2);
		
		new ProviderSideSocketThread(relayClient, getTestSession(), socketFactory, connectionRequest, getTestConsumerGWPublicKey(), 0, getTestMessageProducer());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorConnectionRequestProviderPortTooHigh() {
		when(relayClient.isConnectionClosed(any(Session.class))).thenReturn(false);
		
		final GatewayProviderConnectionRequestDTO connectionRequest = getTestGatewayProviderConnectionRequestDTO();
		connectionRequest.getProvider().setPort(1111111);
		
		new ProviderSideSocketThread(relayClient, getTestSession(), socketFactory, connectionRequest, getTestConsumerGWPublicKey(), 0, getTestMessageProducer());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorConnectionRequestProviderAuthenticationInfoNull() {
		when(relayClient.isConnectionClosed(any(Session.class))).thenReturn(false);
		
		final GatewayProviderConnectionRequestDTO connectionRequest = getTestGatewayProviderConnectionRequestDTO();
		connectionRequest.getProvider().setAuthenticationInfo(null);
		
		new ProviderSideSocketThread(relayClient, getTestSession(), socketFactory, connectionRequest, getTestConsumerGWPublicKey(), 0, getTestMessageProducer());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorConnectionRequestProviderAuthenticationInfoEmpty() {
		when(relayClient.isConnectionClosed(any(Session.class))).thenReturn(false);
		
		final GatewayProviderConnectionRequestDTO connectionRequest = getTestGatewayProviderConnectionRequestDTO();
		connectionRequest.getProvider().setAuthenticationInfo("");
		
		new ProviderSideSocketThread(relayClient, getTestSession(), socketFactory, connectionRequest, getTestConsumerGWPublicKey(), 0, getTestMessageProducer());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorConnectionRequestServiceDefinitionNull() {
		when(relayClient.isConnectionClosed(any(Session.class))).thenReturn(false);
		
		final GatewayProviderConnectionRequestDTO connectionRequest = getTestGatewayProviderConnectionRequestDTO();
		connectionRequest.setServiceDefinition(null);
		
		new ProviderSideSocketThread(relayClient, getTestSession(), socketFactory, connectionRequest, getTestConsumerGWPublicKey(), 0, getTestMessageProducer());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorConnectionRequestServiceDefinitionEmpty() {
		when(relayClient.isConnectionClosed(any(Session.class))).thenReturn(false);
		
		final GatewayProviderConnectionRequestDTO connectionRequest = getTestGatewayProviderConnectionRequestDTO();
		connectionRequest.setServiceDefinition("\t\t\t");
		
		new ProviderSideSocketThread(relayClient, getTestSession(), socketFactory, connectionRequest, getTestConsumerGWPublicKey(), 0, getTestMessageProducer());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorConnectionRequestConsumerGWPublicKeyNull() {
		when(relayClient.isConnectionClosed(any(Session.class))).thenReturn(false);
		
		final GatewayProviderConnectionRequestDTO connectionRequest = getTestGatewayProviderConnectionRequestDTO();
		connectionRequest.setConsumerGWPublicKey(null);
		
		new ProviderSideSocketThread(relayClient, getTestSession(), socketFactory, connectionRequest, getTestConsumerGWPublicKey(), 0, getTestMessageProducer());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorConnectionRequestConsumerGWPublicKeyEmpty() {
		when(relayClient.isConnectionClosed(any(Session.class))).thenReturn(false);
		
		final GatewayProviderConnectionRequestDTO connectionRequest = getTestGatewayProviderConnectionRequestDTO();
		connectionRequest.setConsumerGWPublicKey(" ");
		
		new ProviderSideSocketThread(relayClient, getTestSession(), socketFactory, connectionRequest, getTestConsumerGWPublicKey(), 0, getTestMessageProducer());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testConstructorOk() {
		Assert.assertTrue(testingObject.getName().startsWith("provider.test-service"));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitOk() throws UnknownHostException, IOException {
		boolean initialized = (Boolean) ReflectionTestUtils.getField(testingObject, "initialized");
		Assert.assertFalse(initialized);
		
		when(socketFactory.createSocket(anyString(), anyInt())).thenReturn(getDummySSLSocket(new byte[0]));
		
		Assert.assertNull(testingObject.getLastInteractionTime());
		testingObject.init();
		
		initialized = (Boolean) ReflectionTestUtils.getField(testingObject, "initialized");
		Assert.assertTrue(initialized);
		Assert.assertNotNull(testingObject.getLastInteractionTime());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalStateException.class)
	public void testRunNotInitialized() {
		testingObject.run();
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRunWhenInternalExceptionThrown() throws JMSException, UnknownHostException, IOException {
		doThrow(JMSException.class).when(relayClient).sendBytes(any(Session.class), any(MessageProducer.class), any(PublicKey.class), any(byte[].class));
		when(socketFactory.createSocket(anyString(), anyInt())).thenReturn(getDummySSLSocket(new byte[] { 1, 2, 3, 4 }));
		
		testingObject.init();
		boolean interrupted = (boolean) ReflectionTestUtils.getField(testingObject, "interrupted");
		Assert.assertFalse(interrupted);
		
		testingObject.run();
		
		verify(relayClient, times(1)).sendBytes(any(Session.class), any(MessageProducer.class), any(PublicKey.class), any(byte[].class));

		// because of the exception
		interrupted = (boolean) ReflectionTestUtils.getField(testingObject, "interrupted");
		Assert.assertTrue(interrupted);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRunWhenInputStreamIsClosed() throws JMSException, UnknownHostException, IOException, InterruptedException {
		final byte[] bytes = new byte[2000];
		for (int i = 0; i < bytes.length; ++i) {
			bytes[i] = (byte) (i % 127);
		}
		
		doNothing().when(relayClient).sendBytes(any(Session.class), any(MessageProducer.class), any(PublicKey.class), any(byte[].class));
		when(socketFactory.createSocket(anyString(), anyInt())).thenReturn(getDummySSLSocket(bytes));
		
		testingObject.init();
		Thread.sleep(0, 1);
		final ZonedDateTime afterInit = testingObject.getLastInteractionTime();
		boolean interrupted = (boolean) ReflectionTestUtils.getField(testingObject, "interrupted");
		Assert.assertFalse(interrupted);
		
		testingObject.run();
		final ZonedDateTime afterRun = testingObject.getLastInteractionTime();
		
		verify(relayClient, times(2)).sendBytes(any(Session.class), any(MessageProducer.class), any(PublicKey.class), any(byte[].class));

		// because of input stream is empty
		interrupted = (boolean) ReflectionTestUtils.getField(testingObject, "interrupted");
		Assert.assertTrue(interrupted);
		Assert.assertTrue(afterRun.isAfter(afterInit));
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
			public void setTimeToLive(final long timeToLive) throws JMSException {}
			public void setPriority(final int defaultPriority) throws JMSException {}
			public void setDisableMessageTimestamp(final boolean value) throws JMSException {}
			public void setDisableMessageID(final boolean value) throws JMSException {}
			public void setDeliveryMode(final int deliveryMode) throws JMSException {	}
			public void setDeliveryDelay(final long deliveryDelay) throws JMSException {}
			public void send(final Destination destination, final Message message, final int deliveryMode, final int priority, final long timeToLive, final CompletionListener completionListener) throws JMSException {}
			public void send(final Message message, final int deliveryMode, final int priority, final long timeToLive, final CompletionListener completionListener) throws JMSException {}
			public void send(final Destination destination, final Message message, final int deliveryMode, final int priority, final long timeToLive) throws JMSException {}
			public void send(final Message message, final int deliveryMode, final int priority, final long timeToLive) throws JMSException {}
			public void send(final Destination destination, final Message message, final CompletionListener completionListener) throws JMSException {}
			public void send(final Message message, final CompletionListener completionListener) throws JMSException {}
			public void send(final Destination destination, final Message message) throws JMSException {}
			public void send(final Message message) throws JMSException {}
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
	private PublicKey getTestConsumerGWPublicKey() {
		final String publicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAq5Jq4tOeFoLqxOqtYcujbCNZina3iuV9+/o8D1R9D0HvgnmlgPlqWwjDSxV7m7SGJpuc/rRXJ85OzqV3rwRHO8A8YWXiabj8EdgEIyqg4SOgTN7oZ7MQUisTpwtWn9K14se4dHt/YE9mUW4en19p/yPUDwdw3ECMJHamy/O+Mh6rbw6AFhYvz6F5rXYB8svkenOuG8TSBFlRkcjdfqQqtl4xlHgmlDNWpHsQ3eFAO72mKQjm2ZhWI1H9CLrJf1NQs2GnKXgHBOM5ET61fEHWN8axGGoSKfvTed5vhhX7l5uwxM+AKQipLNNKjEaQYnyX3TL9zL8I7y+QkhzDa7/5kQIDAQAB";
		
		return Utilities.getPublicKeyFromBase64EncodedString(publicKey);
	}
	
	//-------------------------------------------------------------------------------------------------
	private SSLSocket getDummySSLSocket(final byte[] input) {
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
				return new ByteArrayOutputStream();
			}
			
			@Override
			public InputStream getInputStream() throws IOException {
				return new ByteArrayInputStream(input);
			}
		};
	}
}