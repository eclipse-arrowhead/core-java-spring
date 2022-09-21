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

package eu.arrowhead.relay.gateway.activemq;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateException;

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

import org.apache.activemq.command.ActiveMQObjectMessage;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.apache.activemq.command.ActiveMQTopic;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.SSLProperties;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.exception.AuthException;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.core.gateway.GatewayMain;
import eu.arrowhead.relay.RelayCryptographer;
import eu.arrowhead.relay.gateway.ConsumerSideRelayInfo;
import eu.arrowhead.relay.gateway.ControlRelayInfo;
import eu.arrowhead.relay.gateway.GatewayRelayClientFactory;
import eu.arrowhead.relay.gateway.ProviderSideRelayInfo;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = GatewayMain.class)
public class ActiveMQGatewayRelayClientTest {
	
	//=================================================================================================
	// members

	private PublicKey myPublicKey;
	private PublicKey otherPublicKey;
	private PrivateKey otherPrivateKey;
	
	private ActiveMQGatewayRelayClient testObject;
	
	@Autowired
	private SSLProperties sslProps;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Before
	public void setUp() throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
		final InputStream myPublicKeyInputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("certificates/gateway.pub");
		myPublicKey = Utilities.getPublicKeyFromPEMFile(myPublicKeyInputStream);
		
		final InputStream otherPublicKeyInputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("certificates/authorization.pub");
		otherPublicKey = Utilities.getPublicKeyFromPEMFile(otherPublicKeyInputStream);
		
		final KeyStore keystore = KeyStore.getInstance("PKCS12");
		keystore.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("certificates/gateway.p12"), "123456".toCharArray());
		final PrivateKey clientPrivateKey = Utilities.getPrivateKey(keystore, "123456");
		
		testObject = new ActiveMQGatewayRelayClient("gateway.testcloud2.aitia.arrowhead.eu", clientPrivateKey, sslProps);
		
		final KeyStore keystore2 = KeyStore.getInstance("PKCS12");
		keystore2.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("certificates/authorization.p12"), "123456".toCharArray());
		otherPrivateKey = Utilities.getPrivateKey(keystore2, "123456");
	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorServerCommonNameNull() {
		GatewayRelayClientFactory.createGatewayRelayClient(null, null, null);
	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorServerCommonNameEmpty() {
		GatewayRelayClientFactory.createGatewayRelayClient(" ", null, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorPrivateKeyNull() {
		GatewayRelayClientFactory.createGatewayRelayClient("gateway.testcloud2.aitia.arrowhead.eu", null, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorSSLPropertiesNull() {
		GatewayRelayClientFactory.createGatewayRelayClient("gateway.testcloud2.aitia.arrowhead.eu", otherPrivateKey, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testCreateConnectionHostNull() throws JMSException {
		testObject.createConnection(null, 42, false);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testCreateConnectionHostEmpty() throws JMSException {
		testObject.createConnection("\n", 42, false);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testCreateConnectionPortTooLow() throws JMSException {
		testObject.createConnection("localhost", -42, false);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testCreateConnectionPortTooHigh() throws JMSException {
		testObject.createConnection("localhost", 420000, false);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = JMSException.class)
	public void testCreateConnectionfailed() throws JMSException {
		testObject.createConnection("invalid.address.dafafasdasdfgf.qq", 42, false);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testInitializeProviderSideRelaySessionNull() throws JMSException {
		testObject.initializeProviderSideRelay(null, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testInitializeProviderSideRelayMessageListenerNull() throws JMSException {
		testObject.initializeProviderSideRelay(getTestSession(), null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitializeProviderSideRelayOK() throws JMSException {
		final ProviderSideRelayInfo result = testObject.initializeProviderSideRelay(getTestSession(), getTestMessageListener());
		
		Assert.assertEquals(48, result.getQueueId().length());
		Assert.assertEquals("gateway.testcloud2.aitia.arrowhead.eu", result.getPeerName());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testInitializeConsumerSideRelaySessionNull() throws JMSException {
		testObject.initializeConsumerSideRelay(null, null, null, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testInitializeConsumerSideRelayMessageListenerNull() throws JMSException {
		testObject.initializeConsumerSideRelay(getTestSession(), null, null, null);
	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testInitializeConsumerSideRelayPeerNameNull() throws JMSException {
		testObject.initializeConsumerSideRelay(getTestSession(), getTestMessageListener(), null, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testInitializeConsumerSideRelayPeerNameEmpty() throws JMSException {
		testObject.initializeConsumerSideRelay(getTestSession(), getTestMessageListener(), "", null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testInitializeConsumerSideRelayQueueIdNull() throws JMSException {
		testObject.initializeConsumerSideRelay(getTestSession(), getTestMessageListener(), "gateway.testcloud1.aitia.arrowhead.eu", null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testInitializeConsumerSideRelayQueueIdBlank() throws JMSException {
		testObject.initializeConsumerSideRelay(getTestSession(), getTestMessageListener(), "gateway.testcloud1.aitia.arrowhead.eu", "   ");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitializeConsumerSideRelayOk() throws JMSException {
		final ConsumerSideRelayInfo result = testObject.initializeConsumerSideRelay(getTestSession(), getTestMessageListener(), "gateway.testcloud1.aitia.arrowhead.eu", "12sfsdfsdfasddasd234");
		
		Assert.assertNotNull(result);
		Assert.assertNotNull(result.getMessageSender());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testInitializeControlRelaySessionNull() throws JMSException {
		testObject.initializeControlRelay(null, "gateway.testcloud1.aitia.arrowhead.eu", "12sfsdfsdfasddasd234");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testInitializeControlRelayPeerNameNull() throws JMSException {
		testObject.initializeControlRelay(getTestSession(), null, "12sfsdfsdfasddasd234");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testInitializeControlRelayPeerNameEmpty() throws JMSException {
		testObject.initializeControlRelay(getTestSession(), "", "12sfsdfsdfasddasd234");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testInitializeControlRelayQueueIdNull() throws JMSException {
		testObject.initializeControlRelay(getTestSession(), "gateway.testcloud1.aitia.arrowhead.eu", null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testInitializeControlRelayQueueIdBlank() throws JMSException {
		testObject.initializeControlRelay(getTestSession(), "gateway.testcloud1.aitia.arrowhead.eu", "   ");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitializeControlRelayOk() throws JMSException {
		final ControlRelayInfo result = testObject.initializeControlRelay(getTestSession(), "gateway.testcloud1.aitia.arrowhead.eu", "12sfsdfsdfasddasd234");
		
		Assert.assertNotNull(result);
		Assert.assertNotNull(result.getControlRequestMessageSender());
		Assert.assertNotNull(result.getControlResponseMessageSender());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendBytesSessionNull() throws JMSException {
		testObject.sendBytes(null, null, null, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendBytesSenderNull() throws JMSException {
		testObject.sendBytes(getTestSession(), null, null, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendBytesPeerPublicKeyNull() throws JMSException {
		testObject.sendBytes(getTestSession(), getTestMessageProducer(), null, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendBytesBytesNull() throws JMSException {
		testObject.sendBytes(getTestSession(), getTestMessageProducer(), otherPublicKey, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendBytesBytesEmpty() throws JMSException {
		testObject.sendBytes(getTestSession(), getTestMessageProducer(), otherPublicKey, new byte[0]);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testSendBytesOk() throws JMSException {
		try {
			testObject.sendBytes(getTestSession(), getTestMessageProducer(), otherPublicKey, new byte[] { 1, 2, 3, 4});
		} catch (final Exception ex) {
			Assert.fail();
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testGetBytesFromMessageMessageNull() throws JMSException {
		testObject.getBytesFromMessage(null, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testGetBytesFromMessagePeerPublicKeyNull() throws JMSException {
		testObject.getBytesFromMessage(new ActiveMQTextMessage(), null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = JMSException.class)
	public void testGetBytesFromMessageInvalidMessageClass() throws JMSException {
		testObject.getBytesFromMessage(new ActiveMQObjectMessage(), otherPublicKey);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = AuthException.class)
	public void testGetBytesFromMessageInvalidMessageType() throws JMSException {
		final RelayCryptographer cryptographer = new RelayCryptographer(otherPrivateKey);
		final String encoded = cryptographer.encodeRelayMessage(CoreCommonConstants.RELAY_MESSAGE_TYPE_ACK, "abc", null, myPublicKey);
		final ActiveMQTextMessage msg = new ActiveMQTextMessage();
		msg.setText(encoded);
		
		testObject.getBytesFromMessage(msg, otherPublicKey);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetBytesFromMessageOk() throws JMSException {
		final byte[] input = {1, 2, 3, 4}; 
		final RelayCryptographer cryptographer = new RelayCryptographer(otherPrivateKey);
		final String encoded = cryptographer.encodeBytes(input, myPublicKey);
		final ActiveMQTextMessage msg = new ActiveMQTextMessage();
		msg.setText(encoded);
		
		final byte[] result = testObject.getBytesFromMessage(msg, otherPublicKey);
		
		Assert.assertArrayEquals(input, result);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendCloseControlMessageSessionNull() throws JMSException {
		testObject.sendCloseControlMessage(null, null, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendCloseControlMessageSenderNull() throws JMSException {
		testObject.sendCloseControlMessage(getTestSession(), null, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendCloseControlMessageQueueIdNull() throws JMSException {
		testObject.sendCloseControlMessage(getTestSession(), getTestMessageProducer(), null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendCloseControlMessageQueueIdEmpty() throws JMSException {
		testObject.sendCloseControlMessage(getTestSession(), getTestMessageProducer(), "\n");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = JMSException.class)
	public void testSendCloseControlMessageInvalidDestination() throws JMSException {
		testObject.sendCloseControlMessage(getTestSession(), getTestMessageProducer(false), "abcd");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = AuthException.class)
	public void testSendCloseControlMessageInvalidQueue() throws JMSException {
		testObject.sendCloseControlMessage(getTestSession(), getTestMessageProducer(), "efgh");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testSendCloseControlMessageOK() throws JMSException {
		try {
			testObject.sendCloseControlMessage(getTestSession(), getTestMessageProducer(), "abcd");
		} catch (final Exception ex) {
			Assert.fail();
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testHandleCloseControlMessageMessageNull() throws JMSException {
		testObject.handleCloseControlMessage(null, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = JMSException.class)
	public void testHandleCloseControlMessageInvalidMessageClass() throws JMSException {
		testObject.handleCloseControlMessage(new ActiveMQObjectMessage(), getTestSession());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testHandleCloseControlMessageInvalidCommand() throws JMSException {
		final ActiveMQTextMessage msg = new ActiveMQTextMessage();
		msg.setText("EXIT abcd");
		
		testObject.handleCloseControlMessage(msg, getTestSession());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testHandleCloseControlMessageMissingQueueId() throws JMSException {
		final ActiveMQTextMessage msg = new ActiveMQTextMessage();
		msg.setText("CLOSE");
		
		testObject.handleCloseControlMessage(msg, getTestSession());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = JMSException.class)
	public void testHandleCloseControlMessageInvalidDestinationClass() throws JMSException {
		final ActiveMQTextMessage msg = new ActiveMQTextMessage();
		msg.setText("CLOSE abcd");
		msg.setJMSDestination(new ActiveMQTopic());
		
		testObject.handleCloseControlMessage(msg, getTestSession());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = AuthException.class)
	public void testHandleCloseControlMessageUnauthorized() throws JMSException {
		final ActiveMQTextMessage msg = new ActiveMQTextMessage();
		msg.setText("CLOSE efgh");
		msg.setJMSDestination(new ActiveMQQueue("blabla-abcd-CONTROL"));
		
		testObject.handleCloseControlMessage(msg, getTestSession());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testHandleCloseControlMessageOk() throws JMSException {
		final ActiveMQTextMessage msg = new ActiveMQTextMessage();
		msg.setText("CLOSE abcd");
		msg.setJMSDestination(new ActiveMQQueue("blabla-abcd-CONTROL"));
		
		try {
			testObject.handleCloseControlMessage(msg, getTestSession());
		} catch (final Exception ex) {
			Assert.fail();
		}
	}

	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private MessageListener getTestMessageListener() {
		return new MessageListener() {
			public void onMessage(final Message message) {}
		};
	}
	
	//-------------------------------------------------------------------------------------------------
	private MessageProducer getTestMessageProducer() {
		return getTestMessageProducer(true);
	}
	
	//-------------------------------------------------------------------------------------------------
	private MessageProducer getTestMessageProducer(final boolean queue) {
		return new MessageProducer() {
			
			//-------------------------------------------------------------------------------------------------
			public Destination getDestination() throws JMSException { 
				return queue ? new ActiveMQQueue("blabla-abcd-CONTROL") : new ActiveMQTopic();
			}
			
			//-------------------------------------------------------------------------------------------------
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
			public boolean getDisableMessageTimestamp() throws JMSException { return false;	}
			public boolean getDisableMessageID() throws JMSException { return false; }
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
	private Session getTestSession() {
		return new Session() {
			
			//-------------------------------------------------------------------------------------------------
			public MessageProducer createProducer(final Destination destination) throws JMSException { 
				return getTestMessageProducer();
			}
			
			//-------------------------------------------------------------------------------------------------
			public MessageConsumer createConsumer(final Destination destination) throws JMSException { 
				return getTestMessageConsumer();
			}
			
			//-------------------------------------------------------------------------------------------------
			public BytesMessage createBytesMessage() throws JMSException { return null;	}
			public MapMessage createMapMessage() throws JMSException { return null; }
			public Message createMessage() throws JMSException { return null; }
			public ObjectMessage createObjectMessage() throws JMSException { return null; }
			public ObjectMessage createObjectMessage(final Serializable object) throws JMSException { return null; }
			public StreamMessage createStreamMessage() throws JMSException { return null; }
			public TextMessage createTextMessage() throws JMSException { return null; }
			public TextMessage createTextMessage(final String text) throws JMSException {	return null; }
			public boolean getTransacted() throws JMSException { return false; }
			public int getAcknowledgeMode() throws JMSException { return 0;	}
			public void commit() throws JMSException {}
			public void rollback() throws JMSException {}
			public void close() throws JMSException {}
			public void recover() throws JMSException {}
			public MessageListener getMessageListener() throws JMSException { return null; }
			public void setMessageListener(final MessageListener listener) throws JMSException {}
			public void run() {}
			public MessageConsumer createConsumer(final Destination destination, final String messageSelector) throws JMSException { return null; }
			public MessageConsumer createConsumer(final Destination destination, final String messageSelector, final boolean noLocal) throws JMSException { return null; }
			public MessageConsumer createSharedConsumer(final Topic topic, final String sharedSubscriptionName) throws JMSException { return null; }
			public MessageConsumer createSharedConsumer(final Topic topic, final String sharedSubscriptionName, final String messageSelector) throws JMSException { return null; }
			public Queue createQueue(final String queueName) throws JMSException { return null; }
			public Topic createTopic(final String topicName) throws JMSException { return null; }
			public TopicSubscriber createDurableSubscriber(final Topic topic, final String name) throws JMSException { return null; }
			public TopicSubscriber createDurableSubscriber(final Topic topic, final String name, final String messageSelector, final boolean noLocal) throws JMSException {	return null; }
			public MessageConsumer createDurableConsumer(final Topic topic, final String name) throws JMSException { return null; }
			public MessageConsumer createDurableConsumer(final Topic topic, final String name, final String messageSelector, final boolean noLocal) throws JMSException { return null; }
			public MessageConsumer createSharedDurableConsumer(final Topic topic, final String name) throws JMSException { return null; }
			public MessageConsumer createSharedDurableConsumer(final Topic topic, final String name, final String messageSelector) throws JMSException { return null; }
			public QueueBrowser createBrowser(final Queue queue) throws JMSException { return null; }
			public QueueBrowser createBrowser(final Queue queue, final String messageSelector) throws JMSException { return null; }
			public TemporaryQueue createTemporaryQueue() throws JMSException { return null; }
			public TemporaryTopic createTemporaryTopic() throws JMSException { return null;	}
			public void unsubscribe(final String name) throws JMSException {}
		};
	}
}