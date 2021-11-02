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

package eu.arrowhead.core.gatekeeper.service;

import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import eu.arrowhead.common.database.entity.Cloud;
import eu.arrowhead.common.database.entity.CloudGatekeeperRelay;
import eu.arrowhead.common.database.entity.Relay;
import eu.arrowhead.common.dto.internal.GSDPollRequestDTO;
import eu.arrowhead.common.dto.internal.GSDPollResponseDTO;
import eu.arrowhead.common.dto.internal.RelayType;
import eu.arrowhead.common.dto.shared.ErrorWrapperDTO;
import eu.arrowhead.relay.gatekeeper.GatekeeperRelayClient;

@RunWith(SpringRunner.class)
public class GSDPollRequestExecutorTest {

	//=================================================================================================
	// members
	
	private GSDPollRequestExecutor testingObject;

	private GatekeeperRelayClient relayClient;
	
	private final BlockingQueue<ErrorWrapperDTO> queue = new LinkedBlockingQueue<>(1);
	
	private ThreadPoolExecutor threadPool;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Before
	public void setUp() {		
		relayClient = mock(GatekeeperRelayClient.class, "relayClient");
		testingObject = new GSDPollRequestExecutor(queue, relayClient, new GSDPollRequestDTO(), createGatekeeperRelayPerCloudMap());
		threadPool = mock(ThreadPoolExecutor.class, "threadPool");
		ReflectionTestUtils.setField(testingObject, "threadPool",  threadPool);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testExecuteWithThrowingRejectedExecutionException() throws InterruptedException, JMSException {
		doThrow(RejectedExecutionException.class).when(threadPool).execute(any());
		when(relayClient.createConnection(any(), anyInt(), anyBoolean())).thenReturn(getTestSession());
		
		testingObject.execute();
		
		final GSDPollResponseDTO gsdPollResponseDTO = (GSDPollResponseDTO) queue.take();
		assertNull(gsdPollResponseDTO.getProviderCloud());
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private Map<Cloud, Relay> createGatekeeperRelayPerCloudMap() {
		final Cloud cloud = new Cloud();
		cloud.setId(1);
		cloud.setOperator("test-operator");
		cloud.setName("test-name");
		cloud.setSecure(true);
		cloud.setNeighbor(true);
		cloud.setOwnCloud(false);
		cloud.setAuthenticationInfo("test-cloud-auth-info");
		
		final Relay relay = new Relay();
		relay.setId(1);
		relay.setAddress("test-address");
		relay.setPort(1000);
		relay.setSecure(true);
		relay.setExclusive(false);
		relay.setType(RelayType.GATEKEEPER_RELAY);
		
		final CloudGatekeeperRelay conn = new CloudGatekeeperRelay(cloud, relay);
		cloud.getGatekeeperRelays().add(conn);
		relay.getCloudGatekeepers().add(conn);
		
		return Map.of(cloud, relay);
	}
	
	//-------------------------------------------------------------------------------------------------
	private Session getTestSession() {
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
}