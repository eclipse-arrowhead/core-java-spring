package eu.arrowhead.core.qos.thread;

import java.io.IOException;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.security.PublicKey;
import java.util.Locale;
import java.util.Map;

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

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
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

import eu.arrowhead.common.dto.internal.CloudResponseDTO;
import eu.arrowhead.common.dto.internal.RelayResponseDTO;
import eu.arrowhead.core.qos.database.service.RelayTestDBService;
import eu.arrowhead.relay.gateway.ConsumerSideRelayInfo;
import eu.arrowhead.relay.gateway.ControlRelayInfo;
import eu.arrowhead.relay.gateway.GatewayRelayClient;
import eu.arrowhead.relay.gateway.ProviderSideRelayInfo;

@RunWith(SpringRunner.class)
public class ReceiverSideRelayTestThreadTest {

	//=================================================================================================
	// members
	
	private SenderSideRelayTestThread testingObject;
	private GatewayRelayClient relayClient;
	private RelayTestDBService relayTestDBService;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorAppContextNull() {
		new ReceiverSideRelayTestThread(null, null, null, null, null, null, (byte) 0, 0, 0);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorRelayClientNull() {
		new ReceiverSideRelayTestThread(getTestApplicationContext(), null, null, null, null, null, (byte) 0, 0, 0);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorRelaySessionNull() {
		new ReceiverSideRelayTestThread(getTestApplicationContext(), getTestClient(true), null, null, null, null, (byte) 0, 0, 0);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorRelaySessionClosed() {
		new ReceiverSideRelayTestThread(getTestApplicationContext(), getTestClient(true), getTestSession(), null, null, null, (byte) 0, 0, 0);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorRequesterCloudNull() {
		new ReceiverSideRelayTestThread(getTestApplicationContext(), getTestClient(false), getTestSession(), null, null, null, (byte) 0, 0, 0);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorRelayNull() {
		new ReceiverSideRelayTestThread(getTestApplicationContext(), getTestClient(false), getTestSession(), new CloudResponseDTO(), null, null, (byte) 0, 0, 0);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorPublicKeyNull() {
		new ReceiverSideRelayTestThread(getTestApplicationContext(), getTestClient(false), getTestSession(), new CloudResponseDTO(), new RelayResponseDTO(), null, (byte) 0, 0, 0);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorPublicKeyEmpty() {
		new ReceiverSideRelayTestThread(getTestApplicationContext(), getTestClient(false), getTestSession(), new CloudResponseDTO(), new RelayResponseDTO(), "", (byte) 0, 0, 0);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorNoIterationNotPositive() {
		new ReceiverSideRelayTestThread(getTestApplicationContext(), getTestClient(false), getTestSession(), new CloudResponseDTO(), new RelayResponseDTO(), "key", (byte) 0, 0, 0);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorTestMessageSizeNotPositive() {
		new ReceiverSideRelayTestThread(getTestApplicationContext(), getTestClient(false), getTestSession(), new CloudResponseDTO(), new RelayResponseDTO(), "key", (byte) 10, 0, 0);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorTimeoutNotPositive() {
		new ReceiverSideRelayTestThread(getTestApplicationContext(), getTestClient(false), getTestSession(), new CloudResponseDTO(), new RelayResponseDTO(), "key", (byte) 10, 2048, 0);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testConstructorOk() {
		final CloudResponseDTO requesterCloud = new CloudResponseDTO();
		requesterCloud.setOperator("aitia");
		requesterCloud.setName("testcloud");
		final RelayResponseDTO relay = new RelayResponseDTO();
		relay.setAddress("localhost");
		relay.setPort(1234);
		final String publicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAwms8AvBuIxqPjXmyGnqds1EIkvX/kjl+kW9a0SObsp1n/u567vbpYSa+ESZNg4KrxAHJjA8M1TvpGkq4LLrJkEUkC2WNxq3qbWQbseZrIDSpcn6C7gHObJOLjRSpGTSlRHZfncRs1h+MLApVhf6qf611mZNDgN5AqaMtBbB3UzArE3CgO0jiKzBgZGyT9RSKccjlsO6amBgZrLBY0+x6VXPJK71hwZ7/1Y2CHGsgSb20/g2P82qLYf91Eht33u01rcptsETsvGrsq6SqIKtHtmWkYMW1lWB7p2mwFpAft8llUpHewRRAU1qsKYAI6myc/sPmQuQul+4yESMSBu3KyQIDAQAB";
		
		final ReceiverSideRelayTestThread thread =  new ReceiverSideRelayTestThread(getTestApplicationContext(), getTestClient(false), getTestSession(), requesterCloud, relay, publicKey, (byte) 10, 2048, 30000);
		Assert.assertEquals("TEST-RECEIVER-testcloud.aitia|localhost:1234", thread.getName());
	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testInitQueueIdNull() {
		final String publicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAwms8AvBuIxqPjXmyGnqds1EIkvX/kjl+kW9a0SObsp1n/u567vbpYSa+ESZNg4KrxAHJjA8M1TvpGkq4LLrJkEUkC2WNxq3qbWQbseZrIDSpcn6C7gHObJOLjRSpGTSlRHZfncRs1h+MLApVhf6qf611mZNDgN5AqaMtBbB3UzArE3CgO0jiKzBgZGyT9RSKccjlsO6amBgZrLBY0+x6VXPJK71hwZ7/1Y2CHGsgSb20/g2P82qLYf91Eht33u01rcptsETsvGrsq6SqIKtHtmWkYMW1lWB7p2mwFpAft8llUpHewRRAU1qsKYAI6myc/sPmQuQul+4yESMSBu3KyQIDAQAB";
		
		final ReceiverSideRelayTestThread thread =  new ReceiverSideRelayTestThread(getTestApplicationContext(), getTestClient(false), getTestSession(), new CloudResponseDTO(),  new RelayResponseDTO(), publicKey, (byte) 10,
																				    2048, 30000);
		
		thread.init(null, null, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testInitQueueIdEmpty() {
		final String publicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAwms8AvBuIxqPjXmyGnqds1EIkvX/kjl+kW9a0SObsp1n/u567vbpYSa+ESZNg4KrxAHJjA8M1TvpGkq4LLrJkEUkC2WNxq3qbWQbseZrIDSpcn6C7gHObJOLjRSpGTSlRHZfncRs1h+MLApVhf6qf611mZNDgN5AqaMtBbB3UzArE3CgO0jiKzBgZGyT9RSKccjlsO6amBgZrLBY0+x6VXPJK71hwZ7/1Y2CHGsgSb20/g2P82qLYf91Eht33u01rcptsETsvGrsq6SqIKtHtmWkYMW1lWB7p2mwFpAft8llUpHewRRAU1qsKYAI6myc/sPmQuQul+4yESMSBu3KyQIDAQAB";
		
		final ReceiverSideRelayTestThread thread =  new ReceiverSideRelayTestThread(getTestApplicationContext(), getTestClient(false), getTestSession(), new CloudResponseDTO(),  new RelayResponseDTO(), publicKey, (byte) 10,
																					2048, 30000);
		
		thread.init(" ", null, null);
	}

	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testInitSenderNull() {
		final String publicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAwms8AvBuIxqPjXmyGnqds1EIkvX/kjl+kW9a0SObsp1n/u567vbpYSa+ESZNg4KrxAHJjA8M1TvpGkq4LLrJkEUkC2WNxq3qbWQbseZrIDSpcn6C7gHObJOLjRSpGTSlRHZfncRs1h+MLApVhf6qf611mZNDgN5AqaMtBbB3UzArE3CgO0jiKzBgZGyT9RSKccjlsO6amBgZrLBY0+x6VXPJK71hwZ7/1Y2CHGsgSb20/g2P82qLYf91Eht33u01rcptsETsvGrsq6SqIKtHtmWkYMW1lWB7p2mwFpAft8llUpHewRRAU1qsKYAI6myc/sPmQuQul+4yESMSBu3KyQIDAQAB";
		
		final ReceiverSideRelayTestThread thread =  new ReceiverSideRelayTestThread(getTestApplicationContext(), getTestClient(false), getTestSession(), new CloudResponseDTO(),  new RelayResponseDTO(), publicKey, (byte) 10,
																					2048, 30000);
		
		thread.init("queueId", null, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testInitControlSenderNull() {
		final String publicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAwms8AvBuIxqPjXmyGnqds1EIkvX/kjl+kW9a0SObsp1n/u567vbpYSa+ESZNg4KrxAHJjA8M1TvpGkq4LLrJkEUkC2WNxq3qbWQbseZrIDSpcn6C7gHObJOLjRSpGTSlRHZfncRs1h+MLApVhf6qf611mZNDgN5AqaMtBbB3UzArE3CgO0jiKzBgZGyT9RSKccjlsO6amBgZrLBY0+x6VXPJK71hwZ7/1Y2CHGsgSb20/g2P82qLYf91Eht33u01rcptsETsvGrsq6SqIKtHtmWkYMW1lWB7p2mwFpAft8llUpHewRRAU1qsKYAI6myc/sPmQuQul+4yESMSBu3KyQIDAQAB";
		
		final ReceiverSideRelayTestThread thread =  new ReceiverSideRelayTestThread(getTestApplicationContext(), getTestClient(false), getTestSession(), new CloudResponseDTO(),  new RelayResponseDTO(), publicKey, (byte) 10,
																					2048, 30000);
		
		final MessageProducer producer = getTestProducer();
		thread.init("queueId", producer, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitOk() {
		final String publicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAwms8AvBuIxqPjXmyGnqds1EIkvX/kjl+kW9a0SObsp1n/u567vbpYSa+ESZNg4KrxAHJjA8M1TvpGkq4LLrJkEUkC2WNxq3qbWQbseZrIDSpcn6C7gHObJOLjRSpGTSlRHZfncRs1h+MLApVhf6qf611mZNDgN5AqaMtBbB3UzArE3CgO0jiKzBgZGyT9RSKccjlsO6amBgZrLBY0+x6VXPJK71hwZ7/1Y2CHGsgSb20/g2P82qLYf91Eht33u01rcptsETsvGrsq6SqIKtHtmWkYMW1lWB7p2mwFpAft8llUpHewRRAU1qsKYAI6myc/sPmQuQul+4yESMSBu3KyQIDAQAB";
		
		final ReceiverSideRelayTestThread thread =  new ReceiverSideRelayTestThread(getTestApplicationContext(), getTestClient(false), getTestSession(), new CloudResponseDTO(),  new RelayResponseDTO(), publicKey, (byte) 10,
																					2048, 30000);
		
		final MessageProducer producer = getTestProducer();
		
		Assert.assertFalse(thread.isInitialized());
		thread.init("queueId", producer, producer);
		Assert.assertTrue(thread.isInitialized());
	}
	
	//=================================================================================================
	// assistant methods
	
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
			@SuppressWarnings("unchecked") public <T> T getBean(final Class<T> requiredType) throws BeansException { return (T) relayTestDBService; }
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
	private MessageProducer getTestProducer() {
		return new MessageProducer() {
			public void setTimeToLive(long timeToLive) throws JMSException {}
			public void setPriority(int defaultPriority) throws JMSException {}
			public void setDisableMessageTimestamp(boolean value) throws JMSException {}
			public void setDisableMessageID(boolean value) throws JMSException {}
			public void setDeliveryMode(int deliveryMode) throws JMSException {}
			public void setDeliveryDelay(long deliveryDelay) throws JMSException {}
			public void send(Destination destination, Message message, int deliveryMode, int priority, long timeToLive, CompletionListener completionListener) throws JMSException {}
			public void send(Message message, int deliveryMode, int priority, long timeToLive, CompletionListener completionListener) throws JMSException {}
			public void send(Destination destination, Message message, int deliveryMode, int priority, long timeToLive) throws JMSException {}
			public void send(Message message, int deliveryMode, int priority, long timeToLive) throws JMSException {}
			public void send(Destination destination, Message message, CompletionListener completionListener) throws JMSException {}
			public void send(Message message, CompletionListener completionListener) throws JMSException {}
			public void send(Destination destination, Message message) throws JMSException {}
			public void send(Message message) throws JMSException {}
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
}