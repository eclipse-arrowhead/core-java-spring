package eu.arrowhead.core.qos.database.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.security.PublicKey;
import java.util.List;
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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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
import eu.arrowhead.common.dto.internal.CloudWithRelaysResponseDTO;
import eu.arrowhead.common.dto.internal.QoSMonitorSenderConnectionRequestDTO;
import eu.arrowhead.common.dto.internal.RelayRequestDTO;
import eu.arrowhead.common.dto.internal.RelayResponseDTO;
import eu.arrowhead.common.dto.internal.RelayType;
import eu.arrowhead.common.dto.shared.CloudRequestDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.core.qos.service.QoSMonitorDriver;
import eu.arrowhead.core.qos.service.RelayTestService;
import eu.arrowhead.core.qos.thread.RelayTestThreadFactory;
import eu.arrowhead.core.qos.thread.SenderSideRelayTestThread;
import eu.arrowhead.relay.gateway.ConsumerSideRelayInfo;
import eu.arrowhead.relay.gateway.ControlRelayInfo;
import eu.arrowhead.relay.gateway.GatewayRelayClient;
import eu.arrowhead.relay.gateway.ProviderSideRelayInfo;

@RunWith(SpringRunner.class)
public class RelayTestServiceTest {

	//=================================================================================================
	// members
	
	@InjectMocks
	private RelayTestService relayTestService;
	
	@Mock
	private QoSMonitorDriver qosMonitorDriver;
	
	@Mock
	private GatewayRelayClient relayClient;
	
	@Mock
	private RelayTestThreadFactory threadFactory;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testInitRelayTestRequestNull() {
		relayTestService.initRelayTest(null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testInitRelayTestRelayRequestNull() {
		final QoSMonitorSenderConnectionRequestDTO request = new QoSMonitorSenderConnectionRequestDTO();
		
		relayTestService.initRelayTest(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testInitRelayTestRelayAddressNull() {
		final RelayRequestDTO relay = new RelayRequestDTO();
		
		final QoSMonitorSenderConnectionRequestDTO request = new QoSMonitorSenderConnectionRequestDTO();
		request.setRelay(relay);
		
		relayTestService.initRelayTest(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testInitRelayTestRelayAddressEmpty() {
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress(" ");
		
		final QoSMonitorSenderConnectionRequestDTO request = new QoSMonitorSenderConnectionRequestDTO();
		request.setRelay(relay);
		
		relayTestService.initRelayTest(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testInitRelayTestRelayPortNull() {
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("localhost");
		
		final QoSMonitorSenderConnectionRequestDTO request = new QoSMonitorSenderConnectionRequestDTO();
		request.setRelay(relay);
		
		relayTestService.initRelayTest(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testInitRelayTestRelayPortTooLow() {
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("localhost");
		relay.setPort(-42);
		
		final QoSMonitorSenderConnectionRequestDTO request = new QoSMonitorSenderConnectionRequestDTO();
		request.setRelay(relay);
		
		relayTestService.initRelayTest(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testInitRelayTestRelayPortTooHigh() {
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("localhost");
		relay.setPort(420000);
		
		final QoSMonitorSenderConnectionRequestDTO request = new QoSMonitorSenderConnectionRequestDTO();
		request.setRelay(relay);
		
		relayTestService.initRelayTest(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testInitRelayTestRelayTypeNull() {
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("localhost");
		relay.setPort(4200);
		
		final QoSMonitorSenderConnectionRequestDTO request = new QoSMonitorSenderConnectionRequestDTO();
		request.setRelay(relay);
		
		relayTestService.initRelayTest(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testInitRelayTestRelayTypeEmpty() {
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("localhost");
		relay.setPort(4200);
		relay.setType(" ");
		
		final QoSMonitorSenderConnectionRequestDTO request = new QoSMonitorSenderConnectionRequestDTO();
		request.setRelay(relay);
		
		relayTestService.initRelayTest(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testInitRelayTestRelayTypeInvalid() {
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("localhost");
		relay.setPort(4200);
		relay.setType("invalid");
		
		final QoSMonitorSenderConnectionRequestDTO request = new QoSMonitorSenderConnectionRequestDTO();
		request.setRelay(relay);
		
		relayTestService.initRelayTest(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testInitRelayTestRelayTypeGatekeeper() {
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("localhost");
		relay.setPort(4200);
		relay.setType("GATEKEEPER_RELAY");
		
		final QoSMonitorSenderConnectionRequestDTO request = new QoSMonitorSenderConnectionRequestDTO();
		request.setRelay(relay);
		
		relayTestService.initRelayTest(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testInitRelayTestTargetCloudNull() {
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("localhost");
		relay.setPort(4200);
		relay.setType("GATEWAY_RELAY");
		
		final QoSMonitorSenderConnectionRequestDTO request = new QoSMonitorSenderConnectionRequestDTO();
		request.setRelay(relay);
		
		relayTestService.initRelayTest(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testInitRelayTestTargetCloudOperatorNull() {
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("localhost");
		relay.setPort(4200);
		relay.setType("GATEWAY_RELAY");
		
		final CloudRequestDTO targetCloud = new CloudRequestDTO();
		
		final QoSMonitorSenderConnectionRequestDTO request = new QoSMonitorSenderConnectionRequestDTO();
		request.setRelay(relay);
		request.setTargetCloud(targetCloud);
		
		relayTestService.initRelayTest(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testInitRelayTestTargetCloudOperatorEmpty() {
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("localhost");
		relay.setPort(4200);
		relay.setType("GATEWAY_RELAY");
		
		final CloudRequestDTO targetCloud = new CloudRequestDTO();
		targetCloud.setOperator(" ");
		
		final QoSMonitorSenderConnectionRequestDTO request = new QoSMonitorSenderConnectionRequestDTO();
		request.setRelay(relay);
		request.setTargetCloud(targetCloud);
		
		relayTestService.initRelayTest(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testInitRelayTestTargetCloudNameNull() {
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("localhost");
		relay.setPort(4200);
		relay.setType("GATEWAY_RELAY");
		
		final CloudRequestDTO targetCloud = new CloudRequestDTO();
		targetCloud.setOperator("aitia");
		
		final QoSMonitorSenderConnectionRequestDTO request = new QoSMonitorSenderConnectionRequestDTO();
		request.setRelay(relay);
		request.setTargetCloud(targetCloud);
		
		relayTestService.initRelayTest(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testInitRelayTestTargetCloudNameEmpty() {
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("localhost");
		relay.setPort(4200);
		relay.setType("GATEWAY_RELAY");
		
		final CloudRequestDTO targetCloud = new CloudRequestDTO();
		targetCloud.setOperator("aitia");
		targetCloud.setName(" ");
		
		final QoSMonitorSenderConnectionRequestDTO request = new QoSMonitorSenderConnectionRequestDTO();
		request.setRelay(relay);
		request.setTargetCloud(targetCloud);
		
		relayTestService.initRelayTest(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testInitRelayTestQueueIdNull() {
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("localhost");
		relay.setPort(4200);
		relay.setType("GATEWAY_RELAY");
		
		final CloudRequestDTO targetCloud = new CloudRequestDTO();
		targetCloud.setOperator("aitia");
		targetCloud.setName("testcloud");
		
		final QoSMonitorSenderConnectionRequestDTO request = new QoSMonitorSenderConnectionRequestDTO();
		request.setRelay(relay);
		request.setTargetCloud(targetCloud);
		
		relayTestService.initRelayTest(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testInitRelayTestQueueIdEmpty() {
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("localhost");
		relay.setPort(4200);
		relay.setType("GATEWAY_RELAY");
		
		final CloudRequestDTO targetCloud = new CloudRequestDTO();
		targetCloud.setOperator("aitia");
		targetCloud.setName("testcloud");
		
		final QoSMonitorSenderConnectionRequestDTO request = new QoSMonitorSenderConnectionRequestDTO();
		request.setRelay(relay);
		request.setTargetCloud(targetCloud);
		request.setQueueId(" ");
		
		relayTestService.initRelayTest(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testInitRelayTestPeerNameNull() {
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("localhost");
		relay.setPort(4200);
		relay.setType("GATEWAY_RELAY");
		
		final CloudRequestDTO targetCloud = new CloudRequestDTO();
		targetCloud.setOperator("aitia");
		targetCloud.setName("testcloud");
		
		final QoSMonitorSenderConnectionRequestDTO request = new QoSMonitorSenderConnectionRequestDTO();
		request.setRelay(relay);
		request.setTargetCloud(targetCloud);
		request.setQueueId("queueId");
		
		relayTestService.initRelayTest(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testInitRelayTestPeerNameEmpty() {
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("localhost");
		relay.setPort(4200);
		relay.setType("GATEWAY_RELAY");
		
		final CloudRequestDTO targetCloud = new CloudRequestDTO();
		targetCloud.setOperator("aitia");
		targetCloud.setName("testcloud");
		
		final QoSMonitorSenderConnectionRequestDTO request = new QoSMonitorSenderConnectionRequestDTO();
		request.setRelay(relay);
		request.setTargetCloud(targetCloud);
		request.setQueueId("queueId");
		request.setPeerName("");
		
		relayTestService.initRelayTest(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testInitRelayTestReceiverPublicKeyNull() {
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("localhost");
		relay.setPort(4200);
		relay.setType("GATEWAY_RELAY");
		
		final CloudRequestDTO targetCloud = new CloudRequestDTO();
		targetCloud.setOperator("aitia");
		targetCloud.setName("testcloud");
		
		final QoSMonitorSenderConnectionRequestDTO request = new QoSMonitorSenderConnectionRequestDTO();
		request.setRelay(relay);
		request.setTargetCloud(targetCloud);
		request.setQueueId("queueId");
		request.setPeerName("peer");
		
		relayTestService.initRelayTest(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testInitRelayTestReceiverPublicKeyEmpty() {
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("localhost");
		relay.setPort(4200);
		relay.setType("GATEWAY_RELAY");
		
		final CloudRequestDTO targetCloud = new CloudRequestDTO();
		targetCloud.setOperator("aitia");
		targetCloud.setName("testcloud");
		
		final QoSMonitorSenderConnectionRequestDTO request = new QoSMonitorSenderConnectionRequestDTO();
		request.setRelay(relay);
		request.setTargetCloud(targetCloud);
		request.setQueueId("queueId");
		request.setPeerName("peer");
		request.setReceiverQoSMonitorPublicKey(" ");
		
		relayTestService.initRelayTest(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testInitRelayTestCantFindTargetCloud() {
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("localhost");
		relay.setPort(4200);
		relay.setType("GATEWAY_RELAY");
		
		final CloudRequestDTO targetCloud = new CloudRequestDTO();
		targetCloud.setOperator("aitia");
		targetCloud.setName("testcloud");
		
		final QoSMonitorSenderConnectionRequestDTO request = new QoSMonitorSenderConnectionRequestDTO();
		request.setRelay(relay);
		request.setTargetCloud(targetCloud);
		request.setQueueId("queueId");
		request.setPeerName("peer");
		request.setReceiverQoSMonitorPublicKey("valid key");
		
		when(qosMonitorDriver.queryGatekeeperCloudInfo(anyString(), anyString())).thenThrow(InvalidParameterException.class);
		
		relayTestService.initRelayTest(request);
		
		verify(qosMonitorDriver, times(1)).queryGatekeeperCloudInfo(anyString(), anyString());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testInitRelayTestCantFindRelay1() {
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("localhost");
		relay.setPort(4200);
		relay.setType("GATEWAY_RELAY");
		
		final CloudRequestDTO targetCloud = new CloudRequestDTO();
		targetCloud.setOperator("aitia");
		targetCloud.setName("testcloud");
		
		final QoSMonitorSenderConnectionRequestDTO request = new QoSMonitorSenderConnectionRequestDTO();
		request.setRelay(relay);
		request.setTargetCloud(targetCloud);
		request.setQueueId("queueId");
		request.setPeerName("peer");
		request.setReceiverQoSMonitorPublicKey("valid key");
		
		when(qosMonitorDriver.queryGatekeeperCloudInfo(anyString(), anyString())).thenReturn(new CloudWithRelaysResponseDTO());
		
		relayTestService.initRelayTest(request);

		verify(qosMonitorDriver, times(1)).queryGatekeeperCloudInfo(anyString(), anyString());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testInitRelayTestCantFindRelay2() {
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("localhost");
		relay.setPort(4200);
		relay.setType("GATEWAY_RELAY");
		
		final CloudRequestDTO targetCloud = new CloudRequestDTO();
		targetCloud.setOperator("aitia");
		targetCloud.setName("testcloud");
		
		final QoSMonitorSenderConnectionRequestDTO request = new QoSMonitorSenderConnectionRequestDTO();
		request.setRelay(relay);
		request.setTargetCloud(targetCloud);
		request.setQueueId("queueId");
		request.setPeerName("peer");
		request.setReceiverQoSMonitorPublicKey("valid key");
		
		final CloudWithRelaysResponseDTO cloudResponse = new CloudWithRelaysResponseDTO();
		cloudResponse.setGatewayRelays(List.of(new RelayResponseDTO(1, "localhost", 4201, true, false, RelayType.GATEWAY_RELAY, null, null)));
		when(qosMonitorDriver.queryGatekeeperCloudInfo(anyString(), anyString())).thenReturn(cloudResponse);
		
		relayTestService.initRelayTest(request);
		
		verify(qosMonitorDriver, times(1)).queryGatekeeperCloudInfo(anyString(), anyString());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testInitRelayTestCantCreateConnection() throws JMSException {
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("localhost");
		relay.setPort(4200);
		relay.setType("GATEWAY_RELAY");
		
		final CloudRequestDTO targetCloud = new CloudRequestDTO();
		targetCloud.setOperator("aitia");
		targetCloud.setName("testcloud");
		
		final QoSMonitorSenderConnectionRequestDTO request = new QoSMonitorSenderConnectionRequestDTO();
		request.setRelay(relay);
		request.setTargetCloud(targetCloud);
		request.setQueueId("queueId");
		request.setPeerName("peer");
		request.setReceiverQoSMonitorPublicKey("valid key");
		
		final CloudWithRelaysResponseDTO cloudResponse = new CloudWithRelaysResponseDTO();
		cloudResponse.setGatewayRelays(List.of(new RelayResponseDTO(1, "localhost", 4200, true, false, RelayType.GATEWAY_RELAY, null, null)));

		when(qosMonitorDriver.queryGatekeeperCloudInfo(anyString(), anyString())).thenReturn(cloudResponse);
		when(relayClient.createConnection(anyString(), anyInt(), anyBoolean())).thenThrow(JMSException.class);
		
		relayTestService.initRelayTest(request);
		
		verify(qosMonitorDriver, times(1)).queryGatekeeperCloudInfo(anyString(), anyString());
		verify(relayClient, times(1)).createConnection(anyString(), anyInt(), anyBoolean());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testInitRelayTestRelayInitFailed() throws JMSException {
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("localhost");
		relay.setPort(4200);
		relay.setType("GATEWAY_RELAY");
		
		final CloudRequestDTO targetCloud = new CloudRequestDTO();
		targetCloud.setOperator("aitia");
		targetCloud.setName("testcloud");
		
		final QoSMonitorSenderConnectionRequestDTO request = new QoSMonitorSenderConnectionRequestDTO();
		request.setRelay(relay);
		request.setTargetCloud(targetCloud);
		request.setQueueId("queueId");
		request.setPeerName("peer");
		request.setReceiverQoSMonitorPublicKey("valid key");
		
		final CloudWithRelaysResponseDTO cloudResponse = new CloudWithRelaysResponseDTO();
		cloudResponse.setGatewayRelays(List.of(new RelayResponseDTO(1, "localhost", 4200, true, false, RelayType.GATEWAY_RELAY, null, null)));

		when(qosMonitorDriver.queryGatekeeperCloudInfo(anyString(), anyString())).thenReturn(cloudResponse);
		when(relayClient.createConnection(anyString(), anyInt(), anyBoolean())).thenReturn(getTestSession());
		final SenderSideRelayTestThread testThreadDoNothing = getSenderSideTestThreadDoNothing();
		when(threadFactory.createSenderSideThread(any(Session.class), any(CloudResponseDTO.class), any(RelayResponseDTO.class), anyString(), anyString()))
																																	.thenReturn(testThreadDoNothing);  
		when(relayClient.initializeConsumerSideRelay(any(Session.class), any(MessageListener.class), anyString(), anyString())).thenThrow(JMSException.class);
		
		
		relayTestService.initRelayTest(request);
		
		verify(qosMonitorDriver, times(1)).queryGatekeeperCloudInfo(anyString(), anyString());
		verify(relayClient, times(1)).createConnection(anyString(), anyInt(), anyBoolean());
		verify(threadFactory, times(1)).createSenderSideThread(any(Session.class), any(CloudResponseDTO.class), any(RelayResponseDTO.class), anyString(), anyString());
		verify(relayClient, times(1)).initializeConsumerSideRelay(any(Session.class), any(MessageListener.class), anyString(), anyString());
		verify(relayClient, times(1)).closeConnection(any(Session.class));
	}
	
	//TODO: cont 
	
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
	private GatewayRelayClient getTestClient() {
		return new GatewayRelayClient() {
			public boolean isConnectionClosed(final Session session) { return false; }
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
	
	//-------------------------------------------------------------------------------------------------
	private SenderSideRelayTestThread getSenderSideTestThreadDoNothing() {
		return new SenderSideRelayTestThread(getTestApplicationContext(), getTestClient(), getTestSession(), new CloudResponseDTO(), new RelayResponseDTO(), "key", "queueId", (byte) 1, 1, 1) {
			public void run() {
				// do nothing
			}
		};
	}
}