package eu.arrowhead.core.gateway.thread;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.security.PublicKey;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

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
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.SSLProperties;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.relay.gateway.GatewayRelayClient;

@RunWith(SpringRunner.class)
public class ConsumerSideServerSocketThreadTest {

	//=================================================================================================
	// members
	
	private ApplicationContext appContext;
	private GatewayRelayClient relayClient;
	
	private ConsumerSideServerSocketThread testingObject;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Before
	public void setUp() {
		relayClient = mock(GatewayRelayClient.class, "relayClient");
		appContext = mock(ApplicationContext.class, "appContext");
		
		when(relayClient.isConnectionClosed(any(Session.class))).thenReturn(false);
		when(appContext.getBean(CoreCommonConstants.GATEWAY_ACTIVE_SESSION_MAP, ConcurrentHashMap.class)).thenReturn(new ConcurrentHashMap<>());
		when(appContext.getBean(CoreCommonConstants.GATEWAY_AVAILABLE_PORTS_QUEUE, ConcurrentLinkedQueue.class)).thenReturn(new ConcurrentLinkedQueue<>());
		when(appContext.getBean(SSLProperties.class)).thenReturn(getTestSSLPropertiesForThread());
		
		initTestingObject();
	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorAppContextNull() {
		new ConsumerSideServerSocketThread(null, 0, null, null, null, null, 0, null, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorRelayClientNull() {
		new ConsumerSideServerSocketThread(appContext, 0, null, null, null, null, 0, null, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorRelaySessionNull() {
		new ConsumerSideServerSocketThread(appContext, 22003, relayClient, null, null, null, 0, null, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorRelaySessionClosed() {
		when(relayClient.isConnectionClosed(any(Session.class))).thenReturn(true);
		
		new ConsumerSideServerSocketThread(appContext, 22003, relayClient, getTestSession(), null, null, 0, null, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorProviderGWPublicKeyNull() {
		new ConsumerSideServerSocketThread(appContext, 22003, relayClient, getTestSession(), null, null, 0, null, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorProviderGWPublicKeyEmpty() {
		new ConsumerSideServerSocketThread(appContext, 22003, relayClient, getTestSession(), " ", null, 0, null, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorQueueIdNull() {
		new ConsumerSideServerSocketThread(appContext, 22003, relayClient, getTestSession(), "providerGWPublicKey", null, 0, null, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorQueueIdEmpty() {
		new ConsumerSideServerSocketThread(appContext, 22003, relayClient, getTestSession(), "providerGWPublicKey", "", 0, null, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorConsumerNameNull() {
		new ConsumerSideServerSocketThread(appContext, 22003, relayClient, getTestSession(), "providerGWPublicKey", "", 60000, null, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorConsumerNameEmpty() {
		new ConsumerSideServerSocketThread(appContext, 22003, relayClient, getTestSession(), "providerGWPublicKey", "", 60000, "\t\r\n", null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorServiceDefinitionNull() {
		new ConsumerSideServerSocketThread(appContext, 22003, relayClient, getTestSession(), "providerGWPublicKey", "", 60000, "consumer", null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorServiceDefinitionEmpty() {
		new ConsumerSideServerSocketThread(appContext, 22003, relayClient, getTestSession(), "providerGWPublicKey", "", 60000, "consumer", "");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testInitSenderNull() {
		testingObject.init(null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testInitSSLSocketInitializationFailed() {
		Assert.assertFalse(testingObject.isInitialized());
		
		ReflectionTestUtils.setField(testingObject, "sslProperties", null);
		testingObject.init(getTestMessageProducer());
		
		Assert.assertFalse(testingObject.isInitialized());
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitOk() {
		Assert.assertTrue(!testingObject.isInitialized());
		
		ReflectionTestUtils.setField(testingObject, "port", 22004);
		testingObject.init(getTestMessageProducer());
		
		Assert.assertTrue(testingObject.isInitialized());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testOnMessageOutConsumerNull() {
		testingObject.onMessage(null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testOnMessageCloseControlMessage() throws JMSException {
		final ByteArrayOutputStream outputStream = new ByteArrayOutputStream(10);
		ReflectionTestUtils.setField(testingObject, "outConsumer", outputStream);
		final ActiveMQTextMessage message = new ActiveMQTextMessage();
		message.setJMSDestination(new ActiveMQQueue("bla" + GatewayRelayClient.CONTROL_QUEUE_SUFFIX));
		
		doNothing().when(relayClient).handleCloseControlMessage(any(Message.class), any(Session.class));
		
		testingObject.onMessage(message);
		
		final boolean interrupted = (boolean) ReflectionTestUtils.getField(testingObject, "interrupted");
		Assert.assertTrue(interrupted);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testOnMessageExceptionThrown() throws JMSException {
		final ByteArrayOutputStream outputStream = new ByteArrayOutputStream(10);
		ReflectionTestUtils.setField(testingObject, "outConsumer", outputStream);
		final ActiveMQTextMessage message = new ActiveMQTextMessage();
		message.setJMSDestination(new ActiveMQQueue("bla" + GatewayRelayClient.CONTROL_QUEUE_SUFFIX));
		
		doThrow(new JMSException("test")).when(relayClient).handleCloseControlMessage(any(Message.class), any(Session.class));
		
		testingObject.onMessage(message);
		
		final boolean interrupted = (boolean) ReflectionTestUtils.getField(testingObject, "interrupted");
		Assert.assertTrue(interrupted);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testOnMessageNormalMessage() throws JMSException {
		final ByteArrayOutputStream outputStream = new ByteArrayOutputStream(10);
		ReflectionTestUtils.setField(testingObject, "outConsumer", outputStream);
		final ActiveMQTextMessage message = new ActiveMQTextMessage();
		message.setJMSDestination(new ActiveMQQueue("bla"));
		
		when(relayClient.getBytesFromMessage(any(Message.class), any(PublicKey.class))).thenReturn(new byte[] { 10, 8, 6, 4,  2, 9, 7, 5, 3, 1 });
		
		testingObject.onMessage(message);
		
		final boolean interrupted = (boolean) ReflectionTestUtils.getField(testingObject, "interrupted");
		Assert.assertTrue(!interrupted);
		Assert.assertArrayEquals(new byte[] { 10, 8, 6, 4,  2, 9, 7, 5, 3, 1 }, outputStream.toByteArray());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalStateException.class)
	public void testRunNotInitialized() {
		testingObject.run();
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRunWhenInternalExceptionThrown() throws IOException {
		when(appContext.getBean(SSLProperties.class)).thenReturn(getTestSSLPropertiesForThread());

		final String publicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAq5Jq4tOeFoLqxOqtYcujbCNZina3iuV9+/o8D1R9D0HvgnmlgPlqWwjDSxV7m7SGJpuc/rRXJ85OzqV3rwRHO8A8YWXiabj8EdgEIyqg4SOgTN7oZ7MQUisTpwtWn9K14se4dHt/YE9mUW4en19p/yPUDwdw3ECMJHamy/O+Mh6rbw6AFhYvz6F5rXYB8svkenOuG8TSBFlRkcjdfqQqtl4xlHgmlDNWpHsQ3eFAO72mKQjm2ZhWI1H9CLrJf1NQs2GnKXgHBOM5ET61fEHWN8axGGoSKfvTed5vhhX7l5uwxM+AKQipLNNKjEaQYnyX3TL9zL8I7y+QkhzDa7/5kQIDAQAB";
		final ConsumerSideServerSocketThread thread = new ConsumerSideServerSocketThread(appContext, 22005, relayClient, getTestSession(), publicKey, "queueId", 60000, "consumer", "test-service");

		thread.init(getTestMessageProducer());
		
		final SSLServerSocket sslServerSocket = Mockito.mock(SSLServerSocket.class);
		when(sslServerSocket.accept()).thenThrow(IOException.class);
		ReflectionTestUtils.setField(thread, "sslServerSocket", sslServerSocket);
		
		thread.run();

		final boolean interrupted = (boolean) ReflectionTestUtils.getField(thread, "interrupted");
		Assert.assertTrue(interrupted);
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	@Ignore
	public void testRunWhenOtherSideCloseTheConnectionAfterSendingSomeBytes() throws Exception {
		doNothing().when(relayClient).sendBytes(any(Session.class), any(MessageProducer.class), any(PublicKey.class), any(byte[].class));
		
		testingObject.init(getTestMessageProducer());
		testingObject.start();
	
		final SSLContext sslContext = SSLContextFactory.createGatewaySSLContext(getTestSSLPropertiesForTestOtherSocket());
		final SSLSocketFactory socketFactory = (SSLSocketFactory) sslContext.getSocketFactory();
		final SSLSocket sslProviderSocket = (SSLSocket) socketFactory.createSocket("localhost", 22003);
		final OutputStream outProvider = sslProviderSocket.getOutputStream();
		outProvider.write(new byte[] { 5, 6, 7, 8 });
		Thread.sleep(1); // it's necessary: without it the test fails most of the time but not always
		sslProviderSocket.close();

		verify(relayClient).sendBytes(any(Session.class), any(MessageProducer.class), any(PublicKey.class), any(byte[].class));
		
		final boolean interrupted = (boolean) ReflectionTestUtils.getField(testingObject, "interrupted");
		Assert.assertTrue(interrupted);
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
	private SSLProperties getTestSSLPropertiesForTestOtherSocket() {
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
	private void initTestingObject() {
		final String publicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAq5Jq4tOeFoLqxOqtYcujbCNZina3iuV9+/o8D1R9D0HvgnmlgPlqWwjDSxV7m7SGJpuc/rRXJ85OzqV3rwRHO8A8YWXiabj8EdgEIyqg4SOgTN7oZ7MQUisTpwtWn9K14se4dHt/YE9mUW4en19p/yPUDwdw3ECMJHamy/O+Mh6rbw6AFhYvz6F5rXYB8svkenOuG8TSBFlRkcjdfqQqtl4xlHgmlDNWpHsQ3eFAO72mKQjm2ZhWI1H9CLrJf1NQs2GnKXgHBOM5ET61fEHWN8axGGoSKfvTed5vhhX7l5uwxM+AKQipLNNKjEaQYnyX3TL9zL8I7y+QkhzDa7/5kQIDAQAB";
		testingObject = new ConsumerSideServerSocketThread(appContext, 22003, relayClient, getTestSession(), publicKey, "queueId", 600000, "consumer", "test-service");
	}
}