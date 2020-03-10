package eu.arrowhead.core.qos.database.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.List;

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

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
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
import eu.arrowhead.relay.gateway.GatewayRelayClient;

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
	
	@Autowired
	private ApplicationContext appContext;

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
	@Ignore //TODO: delete this after fixing test
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
		when(relayClient.isConnectionClosed(any(Session.class))).thenReturn(false);
		when(relayClient.initializeConsumerSideRelay(any(Session.class), any(MessageListener.class), anyString(), anyString())).thenThrow(JMSException.class);
		
		when(threadFactory.createSenderSideThread(eq(relayClient), any(Session.class), any(CloudResponseDTO.class), any(RelayResponseDTO.class), anyString(),
				anyString())).thenReturn(getSenderSideTestThreadDoNothing()); // mockito error here 
		
		relayTestService.initRelayTest(request);
		
		verify(qosMonitorDriver, times(1)).queryGatekeeperCloudInfo(anyString(), anyString());
		verify(relayClient, times(1)).createConnection(anyString(), anyInt(), anyBoolean());
		verify(threadFactory, times(1)).createSenderSideThread(any(GatewayRelayClient.class), any(Session.class), any(CloudResponseDTO.class), any(RelayResponseDTO.class), anyString(),
															   anyString());
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
	private SenderSideRelayTestThread getSenderSideTestThreadDoNothing() {
		return new SenderSideRelayTestThread(appContext, relayClient, getTestSession(), new CloudResponseDTO(), new RelayResponseDTO(), "key", "queueId", (byte) 1, 1, 1) {
			public void run() {
				// do nothing
			}
		};
	}
}