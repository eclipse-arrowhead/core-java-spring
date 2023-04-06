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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
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
import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.junit.Assert;
import org.junit.Before;
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
	
	private static final String simpleRequest = "DELETE / HTTP/1.1\r\n" + 
												"Accept: text/plain\r\n" + 
												"User-Agent: Apache-HttpClient/4.5.8 (Java/11.0.3)\r\n" + 
												"\r\n";
	
	private static final String validFirstPart = "POST /car?token=eyJhbGciOiJSU0EtT0FFUC0yNTYiLCJlbmMiOiJBMjU2Q0JDLUhTNTEyIiwiY3R5IjoiSldUIn0.L4Iwcfrne6BD-DZcnyjRi4-8GT8XekzXLd3nNSLVXliIbB-lE76AgZnjs9ieQPRMqXDenrVY9SkuWqszEF2uqJP0rzxon-Xwr02yTAUlC1iPzoVomUnaQkpl3OsKEAkbfKgii9lZG2nqMtoAkCzxe8cgY6hu8Mpa6KihhTje9EpqNQhvbc1_vswHRlvd7dGNChY17JEhTMhlhbLvreEH7JUy24MeA7PLh2yWyPL0ouXoocdLzDh1weEgY5GEh_Ag4kpQ8Y9GnE4RULdrqvYF5zv28M_d4SmWvB2ISB5Z1qZyKcAhdT4hlF5stj5FwxMWr19m6-5neJ-QA5VviXIjxQ.kK5rWeM9dM8VZ7K3G_dGVw.gLCdSjagrxvnXgbVodHxYP7fRQogCFTtZVUqhKHOcRxJoHrbHx9tu1U2RAE0TMNo7ytcNez3DJa-Ahn8AX1MMqwlUOdx3wrSAPsn7spvTOWsSWDMpzZ2ASnpX3IQPc-9j1D1YWD3qCpBH-PeRzVXLUD1M1panI6WLZORrw-a52hsHrnBmoA87VBcL5pQ_jqfsMkJbpvsIKlNFZP9ZsgHmoqTnFNc5OGiHcm-gmVly64T_lPRh2S3Wjzjb0g47xs0irCIjTApKT4U2rG0M4Drg-5ns4C7l7lNSMd5NdLkwiZ_4ly7wu3RbWEonQQRR08-uFv-uXczWB_-FquhhN_LUF81hh4HP-FJDAEpUuMCNX0qTL0uwvTujnLfjZWopIjpw5Z3RG5bKrsBveN3FpI-0NlF48LhGXwx2Kbj98c-mm5_ZO5ck6GTmlIVQpU3H9WJ9NXNW6LnQYlUPtUtub8xFy9SNQZ47gwOQ1V18Ui1xAcO4FLF7DlmQysy0xyWbQH3ryVguL5uEsvVI6UaDCO-zRr5L7vIghlRLJvH2c1bpG-SfxgBZbdKvK"; 
	private static final String validSecondPart = "rApdWo-9_e-eC8B-Yi9u2WfdrShUq5s2OTUFNc2Up0YSG7g-v7I65RkqSIcSgYWBbtQc-cmOxgztPP1D83XzpWl-89-Wng0bI5brSxFhER4E2wjHANJOJ1bXYVVocyoGFrJ3Yf1nLElg0uJPEOMWDU0QsCimdtunfVt-deF9R4Iz2CoKatWzCS_3y8lDSglVFS2HBxkY8hOPhLzSh42ER8kQWZSAYWJc-Zc39fp_9ByuiAaVbNzfvz2dQI1GMVjBlcL_FWkdg2.MMy3Ajz0IJVqcOMt9GBtDAkQHGOBBmOfcmBd_rjC-3c HTTP/1.1\r\n" + 
												  "Accept: text/plain\r\n" + 
												  "Accept: application/json\r\n" + 
												  "Content-Type: application/json\r\n" + 
												  "Content-Length: 35\r\n" + 
												  "Host: 127.0.0.1:8000\r\n" + 
												  "Connection: Keep-Alive\r\n" + 
												  "User-Agent: Apache-HttpClient/4.5.8 (Java/11.0.3)\r\n" + 
												  "Accept-Encoding: gzip,deflate\r\n" + 
												  "\r\n" + 
												  "{\"brand\":\"opel - 0\",\"color\":\"blue\"}";
	
	private static final String chunkedSecondPart = "rApdWo-9_e-eC8B-Yi9u2WfdrShUq5s2OTUFNc2Up0YSG7g-v7I65RkqSIcSgYWBbtQc-cmOxgztPP1D83XzpWl-89-Wng0bI5brSxFhER4E2wjHANJOJ1bXYVVocyoGFrJ3Yf1nLElg0uJPEOMWDU0QsCimdtunfVt-deF9R4Iz2CoKatWzCS_3y8lDSglVFS2HBxkY8hOPhLzSh42ER8kQWZSAYWJc-Zc39fp_9ByuiAaVbNzfvz2dQI1GMVjBlcL_FWkdg2.MMy3Ajz0IJVqcOMt9GBtDAkQHGOBBmOfcmBd_rjC-3c HTTP/1.1\r\n" + 
													"Accept: text/plain\r\n" + 
													"Accept: application/json\r\n" + 
													"Content-Type: application/json\r\n" + 
													"Host: 127.0.0.1:8000\r\n" + 
													"Connection: Keep-Alive\r\n" + 
													"Transfer-Encoding: chunked\r\n" +
													"User-Agent: Apache-HttpClient/4.5.8 (Java/11.0.3)\r\n" +
													"\r\n" + 
													"10" +
													"abcdefghij";
	
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
		testingObject.init(null, getTestMessageProducer(), getTestMessageConsumer(), getTestMessageConsumer());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testInitSenderControlNull() {
		testingObject.init(getTestMessageProducer(), null, getTestMessageConsumer(), getTestMessageConsumer());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testInitConsumerNull() {
		testingObject.init(getTestMessageProducer(), getTestMessageProducer(), null, getTestMessageConsumer());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testInitConsumerControlNull() {
		testingObject.init(getTestMessageProducer(), getTestMessageProducer(), getTestMessageConsumer(), null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testInitSSLSocketInitializationFailed() {
		Assert.assertFalse(testingObject.isInitialized());
		
		ReflectionTestUtils.setField(testingObject, "sslProperties", null);
		testingObject.init(getTestMessageProducer(), getTestMessageProducer(), getTestMessageConsumer(), getTestMessageConsumer());
		
		Assert.assertFalse(testingObject.isInitialized());
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitOk() {
		Assert.assertTrue(!testingObject.isInitialized());
		
		ReflectionTestUtils.setField(testingObject, "port", 22010);
		testingObject.init(getTestMessageProducer(), getTestMessageProducer(), getTestMessageConsumer(), getTestMessageConsumer());
		
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
		
		when(relayClient.getBytesFromMessage(any(Message.class), any(PublicKey.class))).thenReturn(new byte[] { 10, 8, 6, 4, 2, 9, 7, 5, 3, 1 });
		
		testingObject.onMessage(message);
		
		final boolean interrupted = (boolean) ReflectionTestUtils.getField(testingObject, "interrupted");
		Assert.assertTrue(!interrupted);
		Assert.assertArrayEquals(new byte[] { 10, 8, 6, 4, 2, 9, 7, 5, 3, 1 }, outputStream.toByteArray());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalStateException.class)
	public void testRunNotInitialized() {
		testingObject.run();
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRunWhenInternalExceptionThrown() throws IOException, JMSException {
		when(appContext.getBean(SSLProperties.class)).thenReturn(getTestSSLPropertiesForThread());

		final String publicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAq5Jq4tOeFoLqxOqtYcujbCNZina3iuV9+/o8D1R9D0HvgnmlgPlqWwjDSxV7m7SGJpuc/rRXJ85OzqV3rwRHO8A8YWXiabj8EdgEIyqg4SOgTN7oZ7MQUisTpwtWn9K14se4dHt/YE9mUW4en19p/yPUDwdw3ECMJHamy/O+Mh6rbw6AFhYvz6F5rXYB8svkenOuG8TSBFlRkcjdfqQqtl4xlHgmlDNWpHsQ3eFAO72mKQjm2ZhWI1H9CLrJf1NQs2GnKXgHBOM5ET61fEHWN8axGGoSKfvTed5vhhX7l5uwxM+AKQipLNNKjEaQYnyX3TL9zL8I7y+QkhzDa7/5kQIDAQAB";
		final String queueId = "queueId";
		final ConsumerSideServerSocketThread thread = new ConsumerSideServerSocketThread(appContext, 22005, relayClient, getTestSession(), publicKey, queueId, 60000, "consumer", "test-service");

		final ConcurrentHashMap<String,ConsumerSideServerSocketThread> activeConsumerSideSocketThreads = new ConcurrentHashMap<>();
		activeConsumerSideSocketThreads.put(queueId, thread);
		ReflectionTestUtils.setField(thread, "activeConsumerSideSocketThreads", activeConsumerSideSocketThreads);
		ReflectionTestUtils.setField(thread, "sender", getTestMessageProducer());
		ReflectionTestUtils.setField(thread, "senderControl", getTestMessageProducer());
		final SSLServerSocket sslServerSocket = Mockito.mock(SSLServerSocket.class);
		ReflectionTestUtils.setField(thread, "sslServerSocket", sslServerSocket);
		ReflectionTestUtils.setField(thread, "initialized", true);
		when(sslServerSocket.accept()).thenThrow(IOException.class);
		when(relayClient.destroyQueues(any(Session.class), any(MessageProducer.class), any(MessageProducer.class))).thenReturn(true);
		when(relayClient.isConnectionClosed(any(Session.class))).thenReturn(true);
		
		thread.run();

		final boolean interrupted = (boolean) ReflectionTestUtils.getField(thread, "interrupted");
		Assert.assertTrue(interrupted);
		Assert.assertFalse(activeConsumerSideSocketThreads.contains(queueId));
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRunWhenOtherSideSendingSomeBytes() throws Exception {
		final boolean[] handshakeCompleted = { false };
		
		final HandshakeCompletedListener listener = new HandshakeCompletedListener() {
			@Override
			public void handshakeCompleted(final HandshakeCompletedEvent event) {
				handshakeCompleted[0] = true;
			}
		};
		
		final Thread consumerSide = new Thread() {
			@Override
			public void run() {
				for (int i = 0; i < 60; ++i) {
					try {
						Thread.sleep(1000);
						final SSLContext sslContext = SSLContextFactory.createGatewaySSLContext(getTestSSLPropertiesForTestOtherSocket());
						final SSLSocketFactory socketFactory = (SSLSocketFactory) sslContext.getSocketFactory();
						final SSLSocket sslConsumerSocket = (SSLSocket) socketFactory.createSocket("localhost", 22003);
						sslConsumerSocket.addHandshakeCompletedListener(listener);
						final OutputStream outConsumer = sslConsumerSocket.getOutputStream();
						outConsumer.write(new byte[] { 5, 6, 7, 8 });
						Thread.sleep(500);
						outConsumer.write(new byte[] { 5, 6, 7, 8 });
						int j = 0;
						while (!handshakeCompleted[0] && ++j < 10) {
							Thread.sleep(500);
						}
						sslConsumerSocket.close();
						break;
					} catch (final IOException | InterruptedException ex) {
						// exception happens when this side is not ready to accept the connection
						ex.printStackTrace();
					}
				}
			}
		};

		final ConcurrentHashMap<String,ConsumerSideServerSocketThread> activeConsumerSideSocketThreads = new ConcurrentHashMap<>();
		final String queueId = (String) ReflectionTestUtils.getField(testingObject, "queueId");
		activeConsumerSideSocketThreads.put(queueId, testingObject);
		ReflectionTestUtils.setField(testingObject, "activeConsumerSideSocketThreads", activeConsumerSideSocketThreads);
		when(relayClient.destroyQueues(any(Session.class), any(MessageProducer.class), any(MessageProducer.class))).thenReturn(true);
		when(relayClient.isConnectionClosed(any(Session.class))).thenReturn(true);
		
		testingObject.init(getTestMessageProducer(), getTestMessageProducer(), getTestMessageConsumer(), getTestMessageConsumer());
		consumerSide.start();
		testingObject.run();
	
		verify(relayClient, times(2)).sendBytes(any(Session.class), any(MessageProducer.class), any(PublicKey.class), any(byte[].class));
		
		final boolean interrupted = (boolean) ReflectionTestUtils.getField(testingObject, "interrupted");
		Assert.assertTrue(interrupted);
		Assert.assertFalse(activeConsumerSideSocketThreads.contains(queueId));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRunWhenOtherSideSendingSimpleHTTPRequest() throws Exception {
		final boolean[] handshakeCompleted = { false };
		
		final HandshakeCompletedListener listener = new HandshakeCompletedListener() {
			@Override
			public void handshakeCompleted(final HandshakeCompletedEvent event) {
				handshakeCompleted[0] = true;
			}
		};
		
		final Thread consumerSide = new Thread() {
			@Override
			public void run() {
				for (int i = 0; i < 60; ++i) {
					try {
						Thread.sleep(1000);
						final SSLContext sslContext = SSLContextFactory.createGatewaySSLContext(getTestSSLPropertiesForTestOtherSocket());
						final SSLSocketFactory socketFactory = (SSLSocketFactory) sslContext.getSocketFactory();
						final SSLSocket sslConsumerSocket = (SSLSocket) socketFactory.createSocket("localhost", 22003);
						sslConsumerSocket.addHandshakeCompletedListener(listener);
						final OutputStream outConsumer = sslConsumerSocket.getOutputStream();
						outConsumer.write(string2bytes(simpleRequest));
						int j = 0;
						while (!handshakeCompleted[0] && ++j < 10) {
							Thread.sleep(500);
						}
						sslConsumerSocket.close();
						break;
					} catch (final IOException | InterruptedException ex) {
						// exception happens when this side is not ready to accept the connection
						ex.printStackTrace();
					}
				}
			}
		};

		final ConcurrentHashMap<String,ConsumerSideServerSocketThread> activeConsumerSideSocketThreads = new ConcurrentHashMap<>();
		final String queueId = (String) ReflectionTestUtils.getField(testingObject, "queueId");
		activeConsumerSideSocketThreads.put(queueId, testingObject);
		ReflectionTestUtils.setField(testingObject, "activeConsumerSideSocketThreads", activeConsumerSideSocketThreads);
		when(relayClient.destroyQueues(any(Session.class), any(MessageProducer.class), any(MessageProducer.class))).thenReturn(true);
		when(relayClient.isConnectionClosed(any(Session.class))).thenReturn(true);
		
		testingObject.init(getTestMessageProducer(), getTestMessageProducer(), getTestMessageConsumer(), getTestMessageConsumer());
		consumerSide.start();
		testingObject.run();
	
		verify(relayClient, times(1)).sendBytes(any(Session.class), any(MessageProducer.class), any(PublicKey.class), any(byte[].class));
		
		final boolean interrupted = (boolean) ReflectionTestUtils.getField(testingObject, "interrupted");
		Assert.assertTrue(interrupted);
		Assert.assertFalse(activeConsumerSideSocketThreads.contains(queueId));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRunWhenOtherSideSendingTwoPartsHTTPRequest() throws Exception {
		final boolean[] handshakeCompleted = { false };
		
		final HandshakeCompletedListener listener = new HandshakeCompletedListener() {
			@Override
			public void handshakeCompleted(final HandshakeCompletedEvent event) {
				handshakeCompleted[0] = true;
			}
		};
		
		final Thread consumerSide = new Thread() {
			@Override
			public void run() {
				for (int i = 0; i < 60; ++i) {
					try {
						Thread.sleep(1000);
						final SSLContext sslContext = SSLContextFactory.createGatewaySSLContext(getTestSSLPropertiesForTestOtherSocket());
						final SSLSocketFactory socketFactory = (SSLSocketFactory) sslContext.getSocketFactory();
						final SSLSocket sslConsumerSocket = (SSLSocket) socketFactory.createSocket("localhost", 22003);
						sslConsumerSocket.addHandshakeCompletedListener(listener);
						final OutputStream outConsumer = sslConsumerSocket.getOutputStream();
						outConsumer.write(string2bytes(validFirstPart));
						Thread.sleep(100);
						outConsumer.write(string2bytes(validSecondPart));
						int j = 0;
						while (!handshakeCompleted[0] && ++j < 10) {
							Thread.sleep(500);
						}
						sslConsumerSocket.close();
						break;
					} catch (final IOException | InterruptedException ex) {
						// exception happens when this side is not ready to accept the connection
						ex.printStackTrace();
					}
				}
			}
		};

		final ConcurrentHashMap<String,ConsumerSideServerSocketThread> activeConsumerSideSocketThreads = new ConcurrentHashMap<>();
		final String queueId = (String) ReflectionTestUtils.getField(testingObject, "queueId");
		activeConsumerSideSocketThreads.put(queueId, testingObject);
		ReflectionTestUtils.setField(testingObject, "activeConsumerSideSocketThreads", activeConsumerSideSocketThreads);
		when(relayClient.destroyQueues(any(Session.class), any(MessageProducer.class), any(MessageProducer.class))).thenReturn(true);
		when(relayClient.isConnectionClosed(any(Session.class))).thenReturn(true);
		
		testingObject.init(getTestMessageProducer(), getTestMessageProducer(), getTestMessageConsumer(), getTestMessageConsumer());
		consumerSide.start();
		testingObject.run();
	
		verify(relayClient, times(1)).sendBytes(any(Session.class), any(MessageProducer.class), any(PublicKey.class), any(byte[].class));
		
		final boolean interrupted = (boolean) ReflectionTestUtils.getField(testingObject, "interrupted");
		Assert.assertTrue(interrupted);
		Assert.assertFalse(activeConsumerSideSocketThreads.contains(queueId));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRunWhenOtherSideSendingUnfinishedAndFinishedHTTPRequest() throws Exception {
		final boolean[] handshakeCompleted = { false };
		
		final HandshakeCompletedListener listener = new HandshakeCompletedListener() {
			@Override
			public void handshakeCompleted(final HandshakeCompletedEvent event) {
				handshakeCompleted[0] = true;
			}
		};
		
		final Thread consumerSide = new Thread() {
			@Override
			public void run() {
				for (int i = 0; i < 60; ++i) {
					try {
						Thread.sleep(1000);
						final SSLContext sslContext = SSLContextFactory.createGatewaySSLContext(getTestSSLPropertiesForTestOtherSocket());
						final SSLSocketFactory socketFactory = (SSLSocketFactory) sslContext.getSocketFactory();
						final SSLSocket sslConsumerSocket = (SSLSocket) socketFactory.createSocket("localhost", 22003);
						sslConsumerSocket.addHandshakeCompletedListener(listener);
						final OutputStream outConsumer = sslConsumerSocket.getOutputStream();
						outConsumer.write(string2bytes(validFirstPart));
						Thread.sleep(100);
						outConsumer.write(string2bytes(validSecondPart.substring(0, validSecondPart.length() - 5)));
						Thread.sleep(100);
						outConsumer.write(string2bytes(validFirstPart));
						Thread.sleep(100);
						outConsumer.write(string2bytes(validSecondPart));
					
						int j = 0;
						while (!handshakeCompleted[0] && ++j < 10) {
							Thread.sleep(500);
						}
						sslConsumerSocket.close();
						break;
					} catch (final IOException | InterruptedException ex) {
						// exception happens when this side is not ready to accept the connection
						ex.printStackTrace();
					}
				}
			}
		};

		final ConcurrentHashMap<String,ConsumerSideServerSocketThread> activeConsumerSideSocketThreads = new ConcurrentHashMap<>();
		final String queueId = (String) ReflectionTestUtils.getField(testingObject, "queueId");
		activeConsumerSideSocketThreads.put(queueId, testingObject);
		ReflectionTestUtils.setField(testingObject, "activeConsumerSideSocketThreads", activeConsumerSideSocketThreads);
		when(relayClient.destroyQueues(any(Session.class), any(MessageProducer.class), any(MessageProducer.class))).thenReturn(true);
		when(relayClient.isConnectionClosed(any(Session.class))).thenReturn(true);
		
		testingObject.init(getTestMessageProducer(), getTestMessageProducer(), getTestMessageConsumer(), getTestMessageConsumer());
		consumerSide.start();
		testingObject.run();
	
		verify(relayClient, times(2)).sendBytes(any(Session.class), any(MessageProducer.class), any(PublicKey.class), any(byte[].class));
		
		final boolean interrupted = (boolean) ReflectionTestUtils.getField(testingObject, "interrupted");
		Assert.assertTrue(interrupted);
		Assert.assertFalse(activeConsumerSideSocketThreads.contains(queueId));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRunWhenOtherSideSendingChunkedHTTPRequest() throws Exception {
		final boolean[] handshakeCompleted = { false };
		
		final HandshakeCompletedListener listener = new HandshakeCompletedListener() {
			@Override
			public void handshakeCompleted(final HandshakeCompletedEvent event) {
				handshakeCompleted[0] = true;
			}
		};
		
		final Thread consumerSide = new Thread() {
			@Override
			public void run() {
				for (int i = 0; i < 60; ++i) {
					try {
						Thread.sleep(1000);
						final SSLContext sslContext = SSLContextFactory.createGatewaySSLContext(getTestSSLPropertiesForTestOtherSocket());
						final SSLSocketFactory socketFactory = (SSLSocketFactory) sslContext.getSocketFactory();
						final SSLSocket sslConsumerSocket = (SSLSocket) socketFactory.createSocket("localhost", 22003);
						sslConsumerSocket.addHandshakeCompletedListener(listener);
						final OutputStream outConsumer = sslConsumerSocket.getOutputStream();
						outConsumer.write(string2bytes(validFirstPart));
						Thread.sleep(100);
						outConsumer.write(string2bytes(chunkedSecondPart));
						Thread.sleep(100);
						outConsumer.write(new byte[] { 1, 2, 3, 4, });
						Thread.sleep(100);
						outConsumer.write(new byte[] { 1, 2, 3, 4, });
					
						int j = 0;
						while (!handshakeCompleted[0] && ++j < 10) {
							Thread.sleep(500);
						}
						sslConsumerSocket.close();
						break;
					} catch (final IOException | InterruptedException ex) {
						// exception happens when this side is not ready to accept the connection
						ex.printStackTrace();
					}
				}
			}
		};


		final ConcurrentHashMap<String,ConsumerSideServerSocketThread> activeConsumerSideSocketThreads = new ConcurrentHashMap<>();
		final String queueId = (String) ReflectionTestUtils.getField(testingObject, "queueId");
		activeConsumerSideSocketThreads.put(queueId, testingObject);
		ReflectionTestUtils.setField(testingObject, "activeConsumerSideSocketThreads", activeConsumerSideSocketThreads);
		when(relayClient.destroyQueues(any(Session.class), any(MessageProducer.class), any(MessageProducer.class))).thenReturn(true);
		when(relayClient.isConnectionClosed(any(Session.class))).thenReturn(true);
		
		testingObject.init(getTestMessageProducer(), getTestMessageProducer(), getTestMessageConsumer(), getTestMessageConsumer());
		consumerSide.start();
		testingObject.run();
	
		verify(relayClient, times(3)).sendBytes(any(Session.class), any(MessageProducer.class), any(PublicKey.class), any(byte[].class));
		
		final boolean interrupted = (boolean) ReflectionTestUtils.getField(testingObject, "interrupted");
		Assert.assertTrue(interrupted);
		Assert.assertFalse(activeConsumerSideSocketThreads.contains(queueId));
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
	
	//-------------------------------------------------------------------------------------------------
	private byte[] string2bytes(final String str) {
		return str.getBytes(StandardCharsets.ISO_8859_1);
	}
}