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
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.security.PublicKey;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

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

import eu.arrowhead.common.dto.internal.GSDPollRequestDTO;
import eu.arrowhead.common.dto.internal.GSDPollResponseDTO;
import eu.arrowhead.common.dto.shared.ErrorMessageDTO;
import eu.arrowhead.common.dto.shared.ErrorWrapperDTO;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.common.exception.ExceptionType;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.relay.gatekeeper.GatekeeperRelayClient;
import eu.arrowhead.relay.gatekeeper.GeneralAdvertisementResult;

@RunWith(SpringRunner.class)
public class GSDPollTaskTest {
	
	//=================================================================================================
	// members
	
	private GSDPollTask testingObject;

	private GatekeeperRelayClient relayClient;
	
	private final BlockingQueue<ErrorWrapperDTO> queue = new LinkedBlockingQueue<>(1);;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Before
	public void setUp() {
		relayClient = mock(GatekeeperRelayClient.class, "relayClient");
		testingObject = new GSDPollTask(relayClient, getTestSession(), "test-cn", "test-key", new GSDPollRequestDTO(), queue);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRunWithNullGeneralAdvertisementResult() throws JMSException, InterruptedException {
		when(relayClient.publishGeneralAdvertisement(any(), any(), any())).thenReturn(null);
		
		testingObject.run();
		
		final GSDPollResponseDTO gsdPollResponseDTO = (GSDPollResponseDTO) queue.take();
		assertNull(gsdPollResponseDTO.getProviderCloud());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRunWithNullGatekeeperRelayResponse() throws JMSException, InterruptedException {
		when(relayClient.publishGeneralAdvertisement(any(), any(), any())).thenReturn(new GeneralAdvertisementResult(getTestMessageConsumer(), "peer-cn", getDummyPublicKey(), "session-id"));
		when(relayClient.sendRequestAndReturnResponse(any(), any(), any())).thenReturn(null);
		
		testingObject.run();
		
		final GSDPollResponseDTO gsdPollResponseDTO = (GSDPollResponseDTO) queue.take();
		assertNull(gsdPollResponseDTO.getProviderCloud());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRunWithThrowingJMSExceptionByRelayClient1() throws JMSException, InterruptedException {
		doThrow(JMSException.class).when(relayClient).publishGeneralAdvertisement(any(), any(), any());
		
		testingObject.run();
		
		final GSDPollResponseDTO gsdPollResponseDTO = (GSDPollResponseDTO) queue.take();
		assertNull(gsdPollResponseDTO.getProviderCloud());
	}
	
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRunWithThrowingJMSExceptionByRelayClient2() throws JMSException, InterruptedException {
		when(relayClient.publishGeneralAdvertisement(any(), any(), any())).thenReturn(new GeneralAdvertisementResult(getTestMessageConsumer(), "peer-cn", getDummyPublicKey(), "session-id"));
		doThrow(JMSException.class).when(relayClient).sendRequestAndReturnResponse(any(), any(), any());
		
		testingObject.run();
		
		final GSDPollResponseDTO gsdPollResponseDTO = (GSDPollResponseDTO) queue.take();
		assertNull(gsdPollResponseDTO.getProviderCloud());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRunWithThrowingInvalidParameterExceptionByRelayClient1() throws JMSException, InterruptedException {
		doThrow(new InvalidParameterException("test")).when(relayClient).publishGeneralAdvertisement(any(), any(), any());
		
		testingObject.run();
		
		final ErrorMessageDTO gsdPollResponseDTO = (ErrorMessageDTO) queue.take();
		assertTrue(gsdPollResponseDTO.getExceptionType() == ExceptionType.INVALID_PARAMETER);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRunWithThrowingInvalidParameterExceptionExceptionByRelayClient2() throws JMSException, InterruptedException {
		when(relayClient.publishGeneralAdvertisement(any(), any(), any())).thenReturn(new GeneralAdvertisementResult(getTestMessageConsumer(), "peer-cn", getDummyPublicKey(), "session-id"));
		doThrow(new InvalidParameterException("test")).when(relayClient).sendRequestAndReturnResponse(any(), any(), any());
		
		testingObject.run();
		
		final ErrorMessageDTO gsdPollResponseDTO = (ErrorMessageDTO) queue.take();
		assertTrue(gsdPollResponseDTO.getExceptionType() == ExceptionType.INVALID_PARAMETER);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRunWithThrowingBadPayloadExceptionByRelayClient1() throws JMSException, InterruptedException {
		doThrow(new BadPayloadException("test")).when(relayClient).publishGeneralAdvertisement(any(), any(), any());
		
		testingObject.run();
		
		final ErrorMessageDTO gsdPollResponseDTO = (ErrorMessageDTO) queue.take();
		assertTrue(gsdPollResponseDTO.getExceptionType() == ExceptionType.BAD_PAYLOAD);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRunWithThrowingBadPayloadExceptionByRelayClient2() throws JMSException, InterruptedException {
		when(relayClient.publishGeneralAdvertisement(any(), any(), any())).thenReturn(new GeneralAdvertisementResult(getTestMessageConsumer(), "peer-cn", getDummyPublicKey(), "session-id"));
		doThrow(new BadPayloadException("test")).when(relayClient).sendRequestAndReturnResponse(any(), any(), any());
		
		testingObject.run();
		
		final ErrorMessageDTO gsdPollResponseDTO = (ErrorMessageDTO) queue.take();
		assertTrue(gsdPollResponseDTO.getExceptionType() == ExceptionType.BAD_PAYLOAD);
	}
	
	//=================================================================================================
	// assistant methods
	
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
	
	//-------------------------------------------------------------------------------------------------
	public MessageConsumer getTestMessageConsumer() {
		return new MessageConsumer() {
			public Message receive(final long timeout) throws JMSException { return null; }
			public void close() throws JMSException {}
			public String getMessageSelector() throws JMSException { return null; }
			public MessageListener getMessageListener() throws JMSException { return null; }
			public void setMessageListener(final MessageListener listener) throws JMSException {}
			public Message receive() throws JMSException { return null; }
			public Message receiveNoWait() throws JMSException { return null; }
		};
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("serial")
	private PublicKey getDummyPublicKey() {
		return new PublicKey() {
			public String getFormat() { return null; }
			public byte[] getEncoded() { return null; }
			public String getAlgorithm() { return null; }
		};
	}
}