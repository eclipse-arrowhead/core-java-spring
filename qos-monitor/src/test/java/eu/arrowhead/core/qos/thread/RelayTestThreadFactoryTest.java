/********************************************************************************
 * Copyright (c) 2020 AITIA
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

package eu.arrowhead.core.qos.thread;

import java.io.IOException;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.security.PublicKey;
import java.util.Locale;
import java.util.Map;

import javax.jms.BytesMessage;
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

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;
import org.springframework.core.ResolvableType;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import eu.arrowhead.common.dto.internal.CloudResponseDTO;
import eu.arrowhead.common.dto.internal.RelayResponseDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.relay.gateway.ConsumerSideRelayInfo;
import eu.arrowhead.relay.gateway.ControlRelayInfo;
import eu.arrowhead.relay.gateway.GatewayRelayClient;
import eu.arrowhead.relay.gateway.ProviderSideRelayInfo;

@RunWith(SpringRunner.class)
public class RelayTestThreadFactoryTest {

	//=================================================================================================
	// members
	
	@InjectMocks
	private RelayTestThreadFactory threadFactory;
	
	//=================================================================================================
	// members
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testInitRelayClientNull() {
		threadFactory.init(null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitOk() {
		threadFactory.init(getTestClient(false));
		
		final Object relayClient = ReflectionTestUtils.getField(threadFactory, "relayClient");
		Assert.assertNotNull(relayClient);
		
		final boolean initialized = (boolean) ReflectionTestUtils.getField(threadFactory, "initialized");
		Assert.assertTrue(initialized);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testCreateSenderSideThreadFactoryNotInitialized() {
		threadFactory.createSenderSideThread(null, null, null, null, null);
	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testCreateSenderSideThreadRelaySessionNull() {
		ReflectionTestUtils.setField(threadFactory, "initialized", true);
		
		threadFactory.createSenderSideThread(null, null, null, null, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testCreateSenderSideThreadRelaySessionClosed() {
		ReflectionTestUtils.setField(threadFactory, "initialized", true);
		ReflectionTestUtils.setField(threadFactory, "relayClient", getTestClient(true));
		
		threadFactory.createSenderSideThread(getTestSession(), null, null, null, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testCreateSenderSideThreadTargetCloudNull() {
		ReflectionTestUtils.setField(threadFactory, "initialized", true);
		ReflectionTestUtils.setField(threadFactory, "relayClient", getTestClient(false));
		
		threadFactory.createSenderSideThread(getTestSession(), null, null, null, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testCreateSenderSideThreadRelayNull() {
		ReflectionTestUtils.setField(threadFactory, "initialized", true);
		ReflectionTestUtils.setField(threadFactory, "relayClient", getTestClient(false));
		
		threadFactory.createSenderSideThread(getTestSession(), new CloudResponseDTO(), null, null, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testCreateSenderSideThreadPublicKeyNull() {
		ReflectionTestUtils.setField(threadFactory, "initialized", true);
		ReflectionTestUtils.setField(threadFactory, "relayClient", getTestClient(false));
		
		threadFactory.createSenderSideThread(getTestSession(), new CloudResponseDTO(), new RelayResponseDTO(), null, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testCreateSenderSideThreadPublicKeyEmpty() {
		ReflectionTestUtils.setField(threadFactory, "initialized", true);
		ReflectionTestUtils.setField(threadFactory, "relayClient", getTestClient(false));
		
		threadFactory.createSenderSideThread(getTestSession(), new CloudResponseDTO(), new RelayResponseDTO(), " ", null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testCreateSenderSideThreadQueueIdNull() {
		ReflectionTestUtils.setField(threadFactory, "initialized", true);
		ReflectionTestUtils.setField(threadFactory, "relayClient", getTestClient(false));
		
		threadFactory.createSenderSideThread(getTestSession(), new CloudResponseDTO(), new RelayResponseDTO(), "key", null);
	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testCreateSenderSideThreadQueueIdEmpty() {
		ReflectionTestUtils.setField(threadFactory, "initialized", true);
		ReflectionTestUtils.setField(threadFactory, "relayClient", getTestClient(false));
		
		threadFactory.createSenderSideThread(getTestSession(), new CloudResponseDTO(), new RelayResponseDTO(), "key", " ");
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCreateSenderSideThreadOk() {
		ReflectionTestUtils.setField(threadFactory, "appContext", getTestApplicationContext());
		ReflectionTestUtils.setField(threadFactory, "initialized", true);
		ReflectionTestUtils.setField(threadFactory, "relayClient", getTestClient(false));
		ReflectionTestUtils.setField(threadFactory, "noIteration", (byte) 1);
		ReflectionTestUtils.setField(threadFactory, "timeout", 1);
		ReflectionTestUtils.setField(threadFactory, "testMessageSize", 1);

		final String publicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAwms8AvBuIxqPjXmyGnqds1EIkvX/kjl+kW9a0SObsp1n/u567vbpYSa+ESZNg4KrxAHJjA8M1TvpGkq4LLrJkEUkC2WNxq3qbWQbseZrIDSpcn6C7gHObJOLjRSpGTSlRHZfncRs1h+MLApVhf6qf611mZNDgN5AqaMtBbB3UzArE3CgO0jiKzBgZGyT9RSKccjlsO6amBgZrLBY0+x6VXPJK71hwZ7/1Y2CHGsgSb20/g2P82qLYf91Eht33u01rcptsETsvGrsq6SqIKtHtmWkYMW1lWB7p2mwFpAft8llUpHewRRAU1qsKYAI6myc/sPmQuQul+4yESMSBu3KyQIDAQAB";
		final SenderSideRelayTestThread thread = threadFactory.createSenderSideThread(getTestSession(), new CloudResponseDTO(), new RelayResponseDTO(), publicKey, "queueId");
		
		Assert.assertNotNull(thread);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testCreateReceiverSideThreadFactoryNotInitialized() {
		threadFactory.createReceiverSideThread(null, null, null, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testCreateReceiverSideThreadRelaySessionNull() {
		ReflectionTestUtils.setField(threadFactory, "initialized", true);
		
		threadFactory.createReceiverSideThread(null, null, null, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testCreateReceiverSideThreadRelaySessionClosed() {
		ReflectionTestUtils.setField(threadFactory, "initialized", true);
		ReflectionTestUtils.setField(threadFactory, "relayClient", getTestClient(true));
		
		threadFactory.createReceiverSideThread(getTestSession(), null, null, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testCreateReceiverSideThreadRequesterCloudNull() {
		ReflectionTestUtils.setField(threadFactory, "initialized", true);
		ReflectionTestUtils.setField(threadFactory, "relayClient", getTestClient(false));
		
		threadFactory.createReceiverSideThread(getTestSession(), null, null, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testCreateReceiverSideThreadRelayNull() {
		ReflectionTestUtils.setField(threadFactory, "initialized", true);
		ReflectionTestUtils.setField(threadFactory, "relayClient", getTestClient(false));
		
		threadFactory.createReceiverSideThread(getTestSession(), new CloudResponseDTO(), null, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testCreateReceiverSideThreadPublicKeyNull() {
		ReflectionTestUtils.setField(threadFactory, "initialized", true);
		ReflectionTestUtils.setField(threadFactory, "relayClient", getTestClient(false));
		
		threadFactory.createReceiverSideThread(getTestSession(), new CloudResponseDTO(), new RelayResponseDTO(), null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testCreateReceiverSideThreadPublicKeyEmpty() {
		ReflectionTestUtils.setField(threadFactory, "initialized", true);
		ReflectionTestUtils.setField(threadFactory, "relayClient", getTestClient(false));
		
		threadFactory.createReceiverSideThread(getTestSession(), new CloudResponseDTO(), new RelayResponseDTO(), " ");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCreateReceiverSideThreadOk() {
		ReflectionTestUtils.setField(threadFactory, "appContext", getTestApplicationContext());
		ReflectionTestUtils.setField(threadFactory, "initialized", true);
		ReflectionTestUtils.setField(threadFactory, "relayClient", getTestClient(false));
		ReflectionTestUtils.setField(threadFactory, "noIteration", (byte) 1);
		ReflectionTestUtils.setField(threadFactory, "timeout", 1);
		ReflectionTestUtils.setField(threadFactory, "testMessageSize", 1);

		final String publicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAwms8AvBuIxqPjXmyGnqds1EIkvX/kjl+kW9a0SObsp1n/u567vbpYSa+ESZNg4KrxAHJjA8M1TvpGkq4LLrJkEUkC2WNxq3qbWQbseZrIDSpcn6C7gHObJOLjRSpGTSlRHZfncRs1h+MLApVhf6qf611mZNDgN5AqaMtBbB3UzArE3CgO0jiKzBgZGyT9RSKccjlsO6amBgZrLBY0+x6VXPJK71hwZ7/1Y2CHGsgSb20/g2P82qLYf91Eht33u01rcptsETsvGrsq6SqIKtHtmWkYMW1lWB7p2mwFpAft8llUpHewRRAU1qsKYAI6myc/sPmQuQul+4yESMSBu3KyQIDAQAB";
		final ReceiverSideRelayTestThread thread = threadFactory.createReceiverSideThread(getTestSession(), new CloudResponseDTO(), new RelayResponseDTO(), publicKey);
		
		Assert.assertNotNull(thread);
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private GatewayRelayClient getTestClient(final boolean isClosed) {
		return new GatewayRelayClient() {
			public boolean isConnectionClosed(final Session session) { return isClosed; }
			public Session createConnection(final String host, final int port, final boolean secure) throws JMSException { return getTestSession();	}
			public void closeConnection(final Session session) {}
			public void validateSwitchControlMessage(final Message msg) throws JMSException {}
			public void sendSwitchControlMessage(final Session session, final MessageProducer sender, final String queueId) throws JMSException {}
			public void sendCloseControlMessage(final Session session, final MessageProducer sender, final String queueId) throws JMSException {}
			public void sendBytes(final Session session, final MessageProducer sender, final PublicKey peerPublicKey, final byte[] bytes) throws JMSException {}
			public ProviderSideRelayInfo initializeProviderSideRelay(final Session session, final MessageListener listener) throws JMSException { return null; }
			public ControlRelayInfo initializeControlRelay(final Session session, final String peerName, final String queueId) throws JMSException { return null; }
			public ConsumerSideRelayInfo initializeConsumerSideRelay(final Session session, final MessageListener listener, final String peerName, final String queueId) throws JMSException { return null; }
			public void handleCloseControlMessage(final Message msg, final Session session) throws JMSException {}
			public byte[] getBytesFromMessage(final Message msg, final PublicKey peerPublicKey) throws JMSException { return null; }
			public void unsubscribeFromQueues(final MessageConsumer consumer, final MessageConsumer consumerControl) throws JMSException {}
			public boolean destroyQueues(final Session session, final MessageProducer producer, final MessageProducer producerControl) throws JMSException { return false; }
		};
	}
	
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
	private ApplicationContext getTestApplicationContext() {
		return new ApplicationContext() {
			public Resource getResource(final String location) { return null; }
			public ClassLoader getClassLoader() { return null; }
			public Resource[] getResources(final String locationPattern) throws IOException { return null; }
			public void publishEvent(final Object event) {}
			public String getMessage(final String code, final Object[] args, final String defaultMessage, final Locale locale) { return null; }
			public String getMessage(final String code, final Object[] args, final Locale locale) throws NoSuchMessageException { return null; }
			public String getMessage(final MessageSourceResolvable resolvable, final Locale locale) throws NoSuchMessageException { return null; }
			public BeanFactory getParentBeanFactory() { return null; }
			public boolean containsLocalBean(final String name) { return false; }
			public boolean isTypeMatch(final String name, final Class<?> typeToMatch) throws NoSuchBeanDefinitionException { return false; }
			public boolean isTypeMatch(final String name, final ResolvableType typeToMatch) throws NoSuchBeanDefinitionException { return false; }
			public boolean isSingleton(final String name) throws NoSuchBeanDefinitionException { return false; }
			public boolean isPrototype(final String name) throws NoSuchBeanDefinitionException { return false; }
			public Class<?> getType(final String name) throws NoSuchBeanDefinitionException { return null; }
			public <T> ObjectProvider<T> getBeanProvider(final ResolvableType requiredType) { return null; }
			public <T> ObjectProvider<T> getBeanProvider(final Class<T> requiredType) { return null; }
			public <T> T getBean(final Class<T> requiredType, final Object... args) throws BeansException { return null; }
			public Object getBean(final String name, final Object... args) throws BeansException { return null;	}
			public <T> T getBean(final String name, final Class<T> requiredType) throws BeansException { return null; }
			public <T> T getBean(final Class<T> requiredType) throws BeansException { return null; }
			public Object getBean(final String name) throws BeansException { return null; }
			public String[] getAliases(final String name) { return null; }
			public boolean containsBean(final String name) { return false; }
			public Map<String,Object> getBeansWithAnnotation(final Class<? extends Annotation> annotationType) throws BeansException { return null; }
			public <T> Map<String,T> getBeansOfType(final Class<T> type, final boolean includeNonSingletons, final boolean allowEagerInit) throws BeansException { return null; }
			public <T> Map<String,T> getBeansOfType(final Class<T> type) throws BeansException { return null; }
			public String[] getBeanNamesForType(final Class<?> type, final boolean includeNonSingletons, final boolean allowEagerInit) { return null; }
			public String[] getBeanNamesForType(final Class<?> type) { return null;	}
			public String[] getBeanNamesForType(final ResolvableType type) { return null; }
			public String[] getBeanNamesForAnnotation(final Class<? extends Annotation> annotationType) { return null; }
			public String[] getBeanDefinitionNames() { return null;	}
			public int getBeanDefinitionCount() { return 0;	}
			public <A extends Annotation> A findAnnotationOnBean(final String beanName, final Class<A> annotationType) throws NoSuchBeanDefinitionException { return null; }
			public boolean containsBeanDefinition(final String beanName) { return false; }
			public Environment getEnvironment() { return null; }
			public long getStartupDate() { return 0; }
			public ApplicationContext getParent() { return null; }
			public String getId() { return null; }
			public String getDisplayName() { return null; }
			public AutowireCapableBeanFactory getAutowireCapableBeanFactory() throws IllegalStateException { return null; }
			public String getApplicationName() { return null; }
		};
	}
}