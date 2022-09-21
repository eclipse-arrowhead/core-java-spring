/********************************************************************************
 * Copyright (c) 2021 AITIA
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.security.PublicKey;
import java.util.List;
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

import eu.arrowhead.common.dto.internal.CloudResponseDTO;
import eu.arrowhead.common.dto.internal.GSDMultiPollRequestDTO;
import eu.arrowhead.common.dto.internal.GSDMultiPollResponseDTO;
import eu.arrowhead.common.dto.shared.ErrorMessageDTO;
import eu.arrowhead.common.dto.shared.ErrorWrapperDTO;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.common.exception.ExceptionType;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.relay.gatekeeper.GatekeeperRelayClient;
import eu.arrowhead.relay.gatekeeper.GatekeeperRelayResponse;
import eu.arrowhead.relay.gatekeeper.GeneralAdvertisementResult;

@RunWith(SpringRunner.class)
public class GSDMultiPollTaskTest {
	
	//=================================================================================================
	// members
	
	private GSDMultiPollTask testingObject;

	private GatekeeperRelayClient relayClient;
	
	private final BlockingQueue<ErrorWrapperDTO> queue = new LinkedBlockingQueue<>(1);;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Before
	public void setUp() {
		relayClient = mock(GatekeeperRelayClient.class, "relayClient");
		testingObject = new GSDMultiPollTask(relayClient, getTestSession(), "test-cn", "test-key", new GSDMultiPollRequestDTO(), queue);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorRelayClientNull() {
		try {
			new GSDMultiPollTask(null, null, null, null, null, null);
		} catch (final Exception ex) {
			assertEquals("relayClient is null", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorRelaySessionNull() {
		try {
			new GSDMultiPollTask(relayClient, null, null, null, null, null);
		} catch (final Exception ex) {
			assertEquals("session is null", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorRecipientCloudCNNull() {
		try {
			new GSDMultiPollTask(relayClient, getTestSession(), null, null, null, null);
		} catch (final Exception ex) {
			assertEquals("recipientCloudCN is empty", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorRecipientCloudCNEmpty() {
		try {
			new GSDMultiPollTask(relayClient, getTestSession(), "", null, null, null);
		} catch (final Exception ex) {
			assertEquals("recipientCloudCN is empty", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorRecipientCloudPublicKeyNull() {
		try {
			new GSDMultiPollTask(relayClient, getTestSession(), "test-cn", null, null, null);
		} catch (final Exception ex) {
			assertEquals("recipientCloudPublicKey is empty", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorRecipientCloudPublicKeyEmpty() {
		try {
			new GSDMultiPollTask(relayClient, getTestSession(), "test-cn", "", null, null);
		} catch (final Exception ex) {
			assertEquals("recipientCloudPublicKey is empty", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorRequestDTONull() {
		try {
			new GSDMultiPollTask(relayClient, getTestSession(), "test-cn", "test-key", null, null);
		} catch (final Exception ex) {
			assertEquals("requestDTO is null", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorQueueNull() {
		try {
			new GSDMultiPollTask(relayClient, getTestSession(), "test-cn", "test-key", new GSDMultiPollRequestDTO(), null);
		} catch (final Exception ex) {
			assertEquals("queue is null", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRunWithNullGeneralAdvertisementResult() throws JMSException, InterruptedException {
		when(relayClient.publishGeneralAdvertisement(any(Session.class), anyString(), anyString())).thenReturn(null);
		
		testingObject.run();
		
		final GSDMultiPollResponseDTO responseDTO = (GSDMultiPollResponseDTO) queue.take();
		assertNull(responseDTO.getProviderCloud());
		
		verify(relayClient, times(1)).publishGeneralAdvertisement(any(Session.class), anyString(), anyString());
		verify(relayClient, never()).sendRequestAndReturnResponse(any(Session.class), any(GeneralAdvertisementResult.class), any(Object.class));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRunWithNullGatekeeperRelayResponse() throws JMSException, InterruptedException {
		when(relayClient.publishGeneralAdvertisement(any(Session.class), anyString(), anyString())).thenReturn(new GeneralAdvertisementResult(getTestMessageConsumer(), "peer-cn", getDummyPublicKey(), "session-id"));
		when(relayClient.sendRequestAndReturnResponse(any(Session.class), any(GeneralAdvertisementResult.class), any(Object.class))).thenReturn(null);
		
		testingObject.run();
		
		final GSDMultiPollResponseDTO responseDTO = (GSDMultiPollResponseDTO) queue.take();
		assertNull(responseDTO.getProviderCloud());

		verify(relayClient, times(1)).publishGeneralAdvertisement(any(Session.class), anyString(), anyString());
		verify(relayClient, times(1)).sendRequestAndReturnResponse(any(Session.class), any(GeneralAdvertisementResult.class), any(Object.class));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRunWithThrowingJMSExceptionByRelayClient1() throws JMSException, InterruptedException {
		doThrow(JMSException.class).when(relayClient).publishGeneralAdvertisement(any(Session.class), anyString(), anyString());
		
		testingObject.run();
		
		final GSDMultiPollResponseDTO responseDTO = (GSDMultiPollResponseDTO) queue.take();
		assertNull(responseDTO.getProviderCloud());

		verify(relayClient, times(1)).publishGeneralAdvertisement(any(Session.class), anyString(), anyString());
		verify(relayClient, never()).sendRequestAndReturnResponse(any(Session.class), any(GeneralAdvertisementResult.class), any(Object.class));
	}
	
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRunWithThrowingJMSExceptionByRelayClient2() throws JMSException, InterruptedException {
		when(relayClient.publishGeneralAdvertisement(any(Session.class), anyString(), anyString())).thenReturn(new GeneralAdvertisementResult(getTestMessageConsumer(), "peer-cn", getDummyPublicKey(), "session-id"));
		doThrow(JMSException.class).when(relayClient).sendRequestAndReturnResponse(any(Session.class), any(GeneralAdvertisementResult.class), any(Object.class));
		
		testingObject.run();
		
		final GSDMultiPollResponseDTO responseDTO = (GSDMultiPollResponseDTO) queue.take();
		assertNull(responseDTO.getProviderCloud());

		verify(relayClient, times(1)).publishGeneralAdvertisement(any(Session.class), anyString(), anyString());
		verify(relayClient, times(1)).sendRequestAndReturnResponse(any(Session.class), any(GeneralAdvertisementResult.class), any(Object.class));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRunWithThrowingInvalidParameterExceptionByRelayClient1() throws JMSException, InterruptedException {
		doThrow(new InvalidParameterException("test")).when(relayClient).publishGeneralAdvertisement(any(Session.class), anyString(), anyString());
		
		testingObject.run();
		
		final ErrorMessageDTO responseDTO = (ErrorMessageDTO) queue.take();
		assertEquals(ExceptionType.INVALID_PARAMETER, responseDTO.getExceptionType());
		assertEquals("test", responseDTO.getErrorMessage());

		verify(relayClient, times(1)).publishGeneralAdvertisement(any(Session.class), anyString(), anyString());
		verify(relayClient, never()).sendRequestAndReturnResponse(any(Session.class), any(GeneralAdvertisementResult.class), any(Object.class));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRunWithThrowingInvalidParameterExceptionExceptionByRelayClient2() throws JMSException, InterruptedException {
		when(relayClient.publishGeneralAdvertisement(any(Session.class), anyString(), anyString())).thenReturn(new GeneralAdvertisementResult(getTestMessageConsumer(), "peer-cn", getDummyPublicKey(), "session-id"));
		doThrow(new InvalidParameterException("test")).when(relayClient).sendRequestAndReturnResponse(any(Session.class), any(GeneralAdvertisementResult.class), any(Object.class));
		
		testingObject.run();
		
		final ErrorMessageDTO responseDTO = (ErrorMessageDTO) queue.take();
		assertEquals(ExceptionType.INVALID_PARAMETER, responseDTO.getExceptionType());
		assertEquals("test", responseDTO.getErrorMessage());

		verify(relayClient, times(1)).publishGeneralAdvertisement(any(Session.class), anyString(), anyString());
		verify(relayClient, times(1)).sendRequestAndReturnResponse(any(Session.class), any(GeneralAdvertisementResult.class), any(Object.class));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRunWithThrowingBadPayloadExceptionByRelayClient1() throws JMSException, InterruptedException {
		doThrow(new BadPayloadException("test")).when(relayClient).publishGeneralAdvertisement(any(Session.class), anyString(), anyString());
		
		testingObject.run();
		
		final ErrorMessageDTO responseDTO = (ErrorMessageDTO) queue.take();
		assertEquals(ExceptionType.BAD_PAYLOAD, responseDTO.getExceptionType());
		assertEquals("test", responseDTO.getErrorMessage());

		verify(relayClient, times(1)).publishGeneralAdvertisement(any(Session.class), anyString(), anyString());
		verify(relayClient, never()).sendRequestAndReturnResponse(any(Session.class), any(GeneralAdvertisementResult.class), any(Object.class));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRunWithThrowingBadPayloadExceptionByRelayClient2() throws JMSException, InterruptedException {
		when(relayClient.publishGeneralAdvertisement(any(Session.class), anyString(), anyString())).thenReturn(new GeneralAdvertisementResult(getTestMessageConsumer(), "peer-cn", getDummyPublicKey(), "session-id"));
		doThrow(new BadPayloadException("test")).when(relayClient).sendRequestAndReturnResponse(any(Session.class), any(GeneralAdvertisementResult.class), any(Object.class));
		
		testingObject.run();
		
		final ErrorMessageDTO responseDTO = (ErrorMessageDTO) queue.take();
		assertEquals(ExceptionType.BAD_PAYLOAD, responseDTO.getExceptionType());
		assertEquals("test", responseDTO.getErrorMessage());

		verify(relayClient, times(1)).publishGeneralAdvertisement(any(Session.class), anyString(), anyString());
		verify(relayClient, times(1)).sendRequestAndReturnResponse(any(Session.class), any(GeneralAdvertisementResult.class), any(Object.class));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRunOk() throws JMSException, InterruptedException {
		final CloudResponseDTO providerCloud = new CloudResponseDTO();
		providerCloud.setOperator("operator");
		providerCloud.setName("name");
		final GSDMultiPollResponseDTO payload = new GSDMultiPollResponseDTO();
		payload.setProviderCloud(providerCloud);
		payload.setProvidedServiceDefinitions(List.of("service"));
		final GatekeeperRelayResponse response = new GatekeeperRelayResponse("abc", "multi_gsd_poll", payload);
		
		when(relayClient.publishGeneralAdvertisement(any(Session.class), anyString(), anyString())).thenReturn(new GeneralAdvertisementResult(getTestMessageConsumer(), "peer-cn", getDummyPublicKey(), "session-id"));
		when(relayClient.sendRequestAndReturnResponse(any(Session.class), any(GeneralAdvertisementResult.class), any(Object.class))).thenReturn(response);
		
		testingObject.run();
		
		final GSDMultiPollResponseDTO responseDTO = (GSDMultiPollResponseDTO) queue.take();
		assertEquals("operator", responseDTO.getProviderCloud().getOperator());
		assertEquals("name", responseDTO.getProviderCloud().getName());
		assertEquals("service", responseDTO.getProvidedServiceDefinitions().get(0));

		verify(relayClient, times(1)).publishGeneralAdvertisement(any(Session.class), anyString(), anyString());
		verify(relayClient, times(1)).sendRequestAndReturnResponse(any(Session.class), any(GeneralAdvertisementResult.class), any(Object.class));
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