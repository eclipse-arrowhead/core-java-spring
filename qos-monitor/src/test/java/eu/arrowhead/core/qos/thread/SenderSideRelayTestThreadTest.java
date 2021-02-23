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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.security.PublicKey;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
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
import eu.arrowhead.core.qos.database.service.RelayTestDBService;
import eu.arrowhead.relay.gateway.ConsumerSideRelayInfo;
import eu.arrowhead.relay.gateway.ControlRelayInfo;
import eu.arrowhead.relay.gateway.GatewayRelayClient;
import eu.arrowhead.relay.gateway.ProviderSideRelayInfo;

@RunWith(SpringRunner.class)
public class SenderSideRelayTestThreadTest {
	
	//=================================================================================================
	// members
	
	private SenderSideRelayTestThread testingObject;
	private GatewayRelayClient relayClient;
	private RelayTestDBService relayTestDBService;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Before
	public void setUp() {
		final String publicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAwms8AvBuIxqPjXmyGnqds1EIkvX/kjl+kW9a0SObsp1n/u567vbpYSa+ESZNg4KrxAHJjA8M1TvpGkq4LLrJkEUkC2WNxq3qbWQbseZrIDSpcn6C7gHObJOLjRSpGTSlRHZfncRs1h+MLApVhf6qf611mZNDgN5AqaMtBbB3UzArE3CgO0jiKzBgZGyT9RSKccjlsO6amBgZrLBY0+x6VXPJK71hwZ7/1Y2CHGsgSb20/g2P82qLYf91Eht33u01rcptsETsvGrsq6SqIKtHtmWkYMW1lWB7p2mwFpAft8llUpHewRRAU1qsKYAI6myc/sPmQuQul+4yESMSBu3KyQIDAQAB";
		
		relayClient = Mockito.mock(GatewayRelayClient.class);
		relayTestDBService = Mockito.mock(RelayTestDBService.class);
		testingObject = new SenderSideRelayTestThread(getTestApplicationContext(), relayClient, getTestSession(), new CloudResponseDTO(), new RelayResponseDTO(), publicKey, "queueId", (byte) 2, 2048, 1000);
		final MessageProducer producer = getTestProducer();
		testingObject.init(producer, producer);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorAppContextNull() {
		new SenderSideRelayTestThread(null, null, null, null, null, null, null, (byte) 0, 0, 0);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorRelayClientNull() {
		new SenderSideRelayTestThread(getTestApplicationContext(), null, null, null, null, null, null, (byte) 0, 0, 0);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorRelaySessionNull() {
		new SenderSideRelayTestThread(getTestApplicationContext(), getTestClient(true), null, null, null, null, null, (byte) 0, 0, 0);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorRelaySessionClosed() {
		new SenderSideRelayTestThread(getTestApplicationContext(), getTestClient(true), getTestSession(), null, null, null, null, (byte) 0, 0, 0);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorTargetCloudNull() {
		new SenderSideRelayTestThread(getTestApplicationContext(), getTestClient(false), getTestSession(), null, null, null, null, (byte) 0, 0, 0);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorRelayNull() {
		new SenderSideRelayTestThread(getTestApplicationContext(), getTestClient(false), getTestSession(), new CloudResponseDTO(), null, null, null, (byte) 0, 0, 0);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorPublicKeyNull() {
		new SenderSideRelayTestThread(getTestApplicationContext(), getTestClient(false), getTestSession(), new CloudResponseDTO(), new RelayResponseDTO(), null, null, (byte) 0, 0, 0);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorPublicKeyEmpty() {
		new SenderSideRelayTestThread(getTestApplicationContext(), getTestClient(false), getTestSession(), new CloudResponseDTO(), new RelayResponseDTO(), "", null, (byte) 0, 0, 0);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorQueueIdNull() {
		new SenderSideRelayTestThread(getTestApplicationContext(), getTestClient(false), getTestSession(), new CloudResponseDTO(), new RelayResponseDTO(), "key", null, (byte) 0, 0, 0);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorQueueIdEmpty() {
		new SenderSideRelayTestThread(getTestApplicationContext(), getTestClient(false), getTestSession(), new CloudResponseDTO(), new RelayResponseDTO(), "key", " ", (byte) 0, 0, 0);
	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorNoIterationNotPositive() {
		new SenderSideRelayTestThread(getTestApplicationContext(), getTestClient(false), getTestSession(), new CloudResponseDTO(), new RelayResponseDTO(), "key", "queueId", (byte) 0, 0, 0);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorTestMessageSizeNotPositive() {
		new SenderSideRelayTestThread(getTestApplicationContext(), getTestClient(false), getTestSession(), new CloudResponseDTO(), new RelayResponseDTO(), "key", "queueId", (byte) 10, 0, 0);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorTimeoutNotPositive() {
		new SenderSideRelayTestThread(getTestApplicationContext(), getTestClient(false), getTestSession(), new CloudResponseDTO(), new RelayResponseDTO(), "key", "queueId", (byte) 10, 2048, 0);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testConstructorOk() {
		final CloudResponseDTO targetCloud = new CloudResponseDTO();
		targetCloud.setOperator("aitia");
		targetCloud.setName("testcloud");
		final RelayResponseDTO relay = new RelayResponseDTO();
		relay.setAddress("localhost");
		relay.setPort(1234);
		final String publicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAwms8AvBuIxqPjXmyGnqds1EIkvX/kjl+kW9a0SObsp1n/u567vbpYSa+ESZNg4KrxAHJjA8M1TvpGkq4LLrJkEUkC2WNxq3qbWQbseZrIDSpcn6C7gHObJOLjRSpGTSlRHZfncRs1h+MLApVhf6qf611mZNDgN5AqaMtBbB3UzArE3CgO0jiKzBgZGyT9RSKccjlsO6amBgZrLBY0+x6VXPJK71hwZ7/1Y2CHGsgSb20/g2P82qLYf91Eht33u01rcptsETsvGrsq6SqIKtHtmWkYMW1lWB7p2mwFpAft8llUpHewRRAU1qsKYAI6myc/sPmQuQul+4yESMSBu3KyQIDAQAB";
		
		final SenderSideRelayTestThread thread = new SenderSideRelayTestThread(getTestApplicationContext(), getTestClient(false), getTestSession(), targetCloud, relay, publicKey, "queueId", (byte) 10, 2048, 30000);
		Assert.assertEquals("TEST-SENDER-testcloud.aitia|localhost:1234", thread.getName());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testInitSenderNull() {
		final String publicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAwms8AvBuIxqPjXmyGnqds1EIkvX/kjl+kW9a0SObsp1n/u567vbpYSa+ESZNg4KrxAHJjA8M1TvpGkq4LLrJkEUkC2WNxq3qbWQbseZrIDSpcn6C7gHObJOLjRSpGTSlRHZfncRs1h+MLApVhf6qf611mZNDgN5AqaMtBbB3UzArE3CgO0jiKzBgZGyT9RSKccjlsO6amBgZrLBY0+x6VXPJK71hwZ7/1Y2CHGsgSb20/g2P82qLYf91Eht33u01rcptsETsvGrsq6SqIKtHtmWkYMW1lWB7p2mwFpAft8llUpHewRRAU1qsKYAI6myc/sPmQuQul+4yESMSBu3KyQIDAQAB";
		
		final SenderSideRelayTestThread thread = new SenderSideRelayTestThread(getTestApplicationContext(), getTestClient(false), getTestSession(), new CloudResponseDTO(), new RelayResponseDTO(), publicKey, "queueId",
																			   (byte) 10, 2048, 30000);
		
		thread.init(null, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testInitControlSenderNull() {
		final String publicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAwms8AvBuIxqPjXmyGnqds1EIkvX/kjl+kW9a0SObsp1n/u567vbpYSa+ESZNg4KrxAHJjA8M1TvpGkq4LLrJkEUkC2WNxq3qbWQbseZrIDSpcn6C7gHObJOLjRSpGTSlRHZfncRs1h+MLApVhf6qf611mZNDgN5AqaMtBbB3UzArE3CgO0jiKzBgZGyT9RSKccjlsO6amBgZrLBY0+x6VXPJK71hwZ7/1Y2CHGsgSb20/g2P82qLYf91Eht33u01rcptsETsvGrsq6SqIKtHtmWkYMW1lWB7p2mwFpAft8llUpHewRRAU1qsKYAI6myc/sPmQuQul+4yESMSBu3KyQIDAQAB";
		
		final SenderSideRelayTestThread thread = new SenderSideRelayTestThread(getTestApplicationContext(), getTestClient(false), getTestSession(), new CloudResponseDTO(), new RelayResponseDTO(), publicKey, "queueId",
																			   (byte) 10, 2048, 30000);
		final MessageProducer producer = getTestProducer();
		
		thread.init(producer, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitOk() {
		final String publicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAwms8AvBuIxqPjXmyGnqds1EIkvX/kjl+kW9a0SObsp1n/u567vbpYSa+ESZNg4KrxAHJjA8M1TvpGkq4LLrJkEUkC2WNxq3qbWQbseZrIDSpcn6C7gHObJOLjRSpGTSlRHZfncRs1h+MLApVhf6qf611mZNDgN5AqaMtBbB3UzArE3CgO0jiKzBgZGyT9RSKccjlsO6amBgZrLBY0+x6VXPJK71hwZ7/1Y2CHGsgSb20/g2P82qLYf91Eht33u01rcptsETsvGrsq6SqIKtHtmWkYMW1lWB7p2mwFpAft8llUpHewRRAU1qsKYAI6myc/sPmQuQul+4yESMSBu3KyQIDAQAB";
		
		final SenderSideRelayTestThread thread = new SenderSideRelayTestThread(getTestApplicationContext(), getTestClient(false), getTestSession(), new CloudResponseDTO(), new RelayResponseDTO(), publicKey, "queueId",
																			   (byte) 10, 2048, 30000);
		final MessageProducer producer = getTestProducer();
		
		Assert.assertFalse(thread.isInitialized());
		thread.init(producer, producer);
		Assert.assertTrue(thread.isInitialized());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testOnMessageSwitchControlMessage() {
		when(relayClient.isConnectionClosed(any(Session.class))).thenReturn(false);
		testingObject.onMessage(getTestTextMessage("SWITCH", "bla-CONTROL"));
		
		final boolean interrupted = (boolean) ReflectionTestUtils.getField(testingObject, "interrupted");
		Assert.assertTrue(interrupted);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testOnMessageCloseControlMessage() {
		when(relayClient.isConnectionClosed(any(Session.class))).thenReturn(false);
		doNothing().when(relayClient).closeConnection(any(Session.class));
		
		testingObject.onMessage(getTestTextMessage("CLOSE", "bla-CONTROL"));

		@SuppressWarnings({ "unchecked", "rawtypes" })
		final BlockingQueue<Object> blockingQueue = (BlockingQueue) ReflectionTestUtils.getField(testingObject, "blockingQueue");
		verify(relayClient, times(1)).closeConnection(any(Session.class));
		Assert.assertEquals(1, blockingQueue.size());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testOnMessageSenderFlagFalseThenEcho() throws JMSException {
		final byte[] someBytes = new byte[] { 1, 2 , 3};
		when(relayClient.isConnectionClosed(any(Session.class))).thenReturn(false);
		when(relayClient.getBytesFromMessage(any(Message.class), any(PublicKey.class))).thenReturn(someBytes);
		doNothing().when(relayClient).sendBytes(any(Session.class), any(MessageProducer.class), any(PublicKey.class), any(byte[].class));
		
		ReflectionTestUtils.setField(testingObject, "senderFlag", false);
		testingObject.onMessage(getTestTextMessage("does not matter", "blabla"));

		verify(relayClient, times(1)).getBytesFromMessage(any(Message.class), any(PublicKey.class));
		verify(relayClient, times(1)).sendBytes(any(Session.class), any(MessageProducer.class), any(PublicKey.class), eq(someBytes));
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("unchecked")
	@Test
	public void testOnMessageSenderFlagTrueNormal() throws JMSException {
		final byte[] someBytes = new byte[] { 0, 1 , 2};
		when(relayClient.isConnectionClosed(any(Session.class))).thenReturn(false);
		when(relayClient.getBytesFromMessage(any(Message.class), any(PublicKey.class))).thenReturn(someBytes);
		
		ReflectionTestUtils.setField(testingObject, "senderFlag", true);
		@SuppressWarnings("rawtypes")
		final Map<Byte,long[]> testResults = (Map) ReflectionTestUtils.getField(testingObject, "testResults");
		testResults.put((byte)0, new long[] { System.currentTimeMillis(), 0 });
		
		testingObject.onMessage(getTestTextMessage("does not matter", "blabla"));

		@SuppressWarnings("rawtypes")
		final BlockingQueue<Byte> blockingQueue = (BlockingQueue) ReflectionTestUtils.getField(testingObject, "blockingQueue");

		verify(relayClient, times(1)).getBytesFromMessage(any(Message.class), any(PublicKey.class));
		verify(relayClient, never()).sendBytes(any(Session.class), any(MessageProducer.class), any(PublicKey.class), eq(someBytes));
		verify(relayTestDBService, never()).storeMeasurements(any(CloudResponseDTO.class), any(RelayResponseDTO.class), any(Map.class));
		Assert.assertTrue(testResults.get((byte)0)[1] > 0);
		Assert.assertEquals(1, blockingQueue.size());
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("unchecked")
	@Test
	public void testOnMessageSenderFlagTrueTimeout() throws JMSException {
		final byte[] someBytes = new byte[] { 0, 1 , 2};
		when(relayClient.isConnectionClosed(any(Session.class))).thenReturn(false);
		when(relayClient.getBytesFromMessage(any(Message.class), any(PublicKey.class))).thenReturn(someBytes);
		
		ReflectionTestUtils.setField(testingObject, "senderFlag", true);
		@SuppressWarnings("rawtypes")
		final Map<Byte,long[]> testResults = (Map) ReflectionTestUtils.getField(testingObject, "testResults");
		testResults.put((byte)0, new long[] { 10, 0 });
		
		testingObject.onMessage(getTestTextMessage("does not matter", "blabla"));

		@SuppressWarnings("rawtypes")
		final BlockingQueue<Byte> blockingQueue = (BlockingQueue) ReflectionTestUtils.getField(testingObject, "blockingQueue");

		verify(relayClient, times(1)).getBytesFromMessage(any(Message.class), any(PublicKey.class));
		verify(relayClient, never()).sendBytes(any(Session.class), any(MessageProducer.class), any(PublicKey.class), eq(someBytes));
		verify(relayTestDBService, never()).storeMeasurements(any(CloudResponseDTO.class), any(RelayResponseDTO.class), any(Map.class));
		Assert.assertEquals(0, blockingQueue.size());
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("unchecked")
	@Test
	public void testOnMessageSenderFlagTrueLastIteration() throws JMSException {
		final byte[] someBytes = new byte[] { 1, 2 , 3};
		when(relayClient.isConnectionClosed(any(Session.class))).thenReturn(false);
		when(relayClient.getBytesFromMessage(any(Message.class), any(PublicKey.class))).thenReturn(someBytes);
		doNothing().when(relayClient).sendSwitchControlMessage(any(Session.class), any(MessageProducer.class), anyString());
		doNothing().when(relayTestDBService).storeMeasurements(any(CloudResponseDTO.class), any(RelayResponseDTO.class), any(Map.class));
		
		ReflectionTestUtils.setField(testingObject, "senderFlag", true);
		@SuppressWarnings("rawtypes")
		final Map<Byte,long[]> testResults = (Map) ReflectionTestUtils.getField(testingObject, "testResults");
		testResults.put((byte)1, new long[] { System.currentTimeMillis(), 0 });
		
		testingObject.onMessage(getTestTextMessage("does not matter", "blabla"));

		@SuppressWarnings("rawtypes")
		final BlockingQueue<Byte> blockingQueue = (BlockingQueue) ReflectionTestUtils.getField(testingObject, "blockingQueue");

		verify(relayClient, times(1)).getBytesFromMessage(any(Message.class), any(PublicKey.class));
		verify(relayClient, never()).sendBytes(any(Session.class), any(MessageProducer.class), any(PublicKey.class), eq(someBytes));
		verify(relayClient, times(1)).sendSwitchControlMessage(any(Session.class), any(MessageProducer.class), anyString());
		verify(relayTestDBService, times(1)).storeMeasurements(any(CloudResponseDTO.class), any(RelayResponseDTO.class), any(Map.class));
		Assert.assertEquals(1, blockingQueue.size());
		Assert.assertEquals(false, (boolean) ReflectionTestUtils.getField(testingObject, "senderFlag"));
		Assert.assertEquals(true, (boolean) ReflectionTestUtils.getField(testingObject, "resultsSaved"));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalStateException.class)
	public void testRunThreadNotInitialized() {
		final String publicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAwms8AvBuIxqPjXmyGnqds1EIkvX/kjl+kW9a0SObsp1n/u567vbpYSa+ESZNg4KrxAHJjA8M1TvpGkq4LLrJkEUkC2WNxq3qbWQbseZrIDSpcn6C7gHObJOLjRSpGTSlRHZfncRs1h+MLApVhf6qf611mZNDgN5AqaMtBbB3UzArE3CgO0jiKzBgZGyT9RSKccjlsO6amBgZrLBY0+x6VXPJK71hwZ7/1Y2CHGsgSb20/g2P82qLYf91Eht33u01rcptsETsvGrsq6SqIKtHtmWkYMW1lWB7p2mwFpAft8llUpHewRRAU1qsKYAI6myc/sPmQuQul+4yESMSBu3KyQIDAQAB";
		
		final SenderSideRelayTestThread thread = new SenderSideRelayTestThread(getTestApplicationContext(), getTestClient(false), getTestSession(), new CloudResponseDTO(), new RelayResponseDTO(), publicKey, "queueId",
																			   (byte) 10, 2048, 30000);
		thread.run();
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRunNoTimeout() throws JMSException, InterruptedException {
		doNothing().doThrow(ArrowheadException.class).when(relayClient).sendBytes(any(Session.class), any(MessageProducer.class), any(PublicKey.class), any(byte[].class));
		doNothing().when(relayTestDBService).logErrorIntoMeasurementsTable(any(CloudResponseDTO.class), any(RelayResponseDTO.class), anyInt(), anyString(), any(Throwable.class));

		@SuppressWarnings({ "unchecked", "rawtypes" })
		final BlockingQueue<Byte> blockingQueue = (BlockingQueue) ReflectionTestUtils.getField(testingObject, "blockingQueue");
		blockingQueue.put((byte)0);


		testingObject.run();
		
		@SuppressWarnings({ "unchecked", "rawtypes" })
		final Map<Byte,long[]> testResults = (Map) ReflectionTestUtils.getField(testingObject, "testResults");
		final long[] times = testResults.get((byte) 0);
		Assert.assertTrue(times[1] - times[0] < 1001);
		verify(relayClient, times(2)).sendBytes(any(Session.class), any(MessageProducer.class), any(PublicKey.class), any(byte[].class));
		verify(relayTestDBService, times(1)).logErrorIntoMeasurementsTable(any(CloudResponseDTO.class), any(RelayResponseDTO.class), anyInt(), eq(null), any(Throwable.class));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRunNoTimeoutButNotWaitedMessage() throws JMSException, InterruptedException {
		doNothing().doThrow(ArrowheadException.class).when(relayClient).sendBytes(any(Session.class), any(MessageProducer.class), any(PublicKey.class), any(byte[].class));
		doNothing().when(relayTestDBService).logErrorIntoMeasurementsTable(any(CloudResponseDTO.class), any(RelayResponseDTO.class), anyInt(), anyString(), any(Throwable.class));

		@SuppressWarnings({ "unchecked", "rawtypes" })
		final BlockingQueue<Byte> blockingQueue = (BlockingQueue) ReflectionTestUtils.getField(testingObject, "blockingQueue");
		blockingQueue.put((byte)2); // this one skipped
		blockingQueue.put((byte)0); 

		testingObject.run();
		
		@SuppressWarnings({ "unchecked", "rawtypes" })
		final Map<Byte,long[]> testResults = (Map) ReflectionTestUtils.getField(testingObject, "testResults");
		final long[] times = testResults.get((byte) 0);
		Assert.assertTrue(times[1] - times[0] < 1001);
		verify(relayClient, times(2)).sendBytes(any(Session.class), any(MessageProducer.class), any(PublicKey.class), any(byte[].class));
		verify(relayTestDBService, times(1)).logErrorIntoMeasurementsTable(any(CloudResponseDTO.class), any(RelayResponseDTO.class), anyInt(), eq(null), any(Throwable.class));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRunTimeout() throws JMSException {
		doNothing().doThrow(ArrowheadException.class).when(relayClient).sendBytes(any(Session.class), any(MessageProducer.class), any(PublicKey.class), any(byte[].class));
		doNothing().when(relayTestDBService).logErrorIntoMeasurementsTable(any(CloudResponseDTO.class), any(RelayResponseDTO.class), anyInt(), anyString(), any(Throwable.class));
		
		testingObject.run();
		
		@SuppressWarnings({ "unchecked", "rawtypes" })
		final Map<Byte,long[]> testResults = (Map) ReflectionTestUtils.getField(testingObject, "testResults");
		final long[] times = testResults.get((byte) 0);
		Assert.assertEquals(1001, times[1] - times[0]);
		verify(relayClient, times(2)).sendBytes(any(Session.class), any(MessageProducer.class), any(PublicKey.class), any(byte[].class));
		verify(relayTestDBService, times(1)).logErrorIntoMeasurementsTable(any(CloudResponseDTO.class), any(RelayResponseDTO.class), anyInt(), eq(null), any(Throwable.class));
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("unchecked")
	@Test
	public void testRunTimeoutLastMessage() throws JMSException, InterruptedException {
		doNothing().when(relayClient).sendBytes(any(Session.class), any(MessageProducer.class), any(PublicKey.class), any(byte[].class));
		doNothing().when(relayClient).sendSwitchControlMessage(any(Session.class), any(MessageProducer.class), anyString());
		
		@SuppressWarnings("rawtypes")
		final BlockingQueue<Byte> blockingQueue = (BlockingQueue) ReflectionTestUtils.getField(testingObject, "blockingQueue");
		blockingQueue.put((byte)0); 
		
		doAnswer(new Answer<Object>() {
			public Object answer(final InvocationOnMock invocation) throws InterruptedException {
				blockingQueue.put((byte)-1);
				return null;
			}
		}).when(relayTestDBService).storeMeasurements(any(CloudResponseDTO.class), any(RelayResponseDTO.class), any(Map.class)); // to make sure the last element of the blocking queue appears AFTER the timeout
		
		testingObject.run();
		
		@SuppressWarnings("rawtypes")
		final Map<Byte,long[]> testResults = (Map) ReflectionTestUtils.getField(testingObject, "testResults");
		final long[] times = testResults.get((byte) 1);
		Assert.assertEquals(1001, times[1] - times[0]);
		verify(relayClient, times(2)).sendBytes(any(Session.class), any(MessageProducer.class), any(PublicKey.class), any(byte[].class));
		verify(relayClient, times(1)).sendSwitchControlMessage(any(Session.class), any(MessageProducer.class), anyString());
		verify(relayTestDBService, times(1)).storeMeasurements(any(CloudResponseDTO.class), any(RelayResponseDTO.class), any(Map.class));
		Assert.assertEquals(false, (boolean) ReflectionTestUtils.getField(testingObject, "senderFlag"));
		Assert.assertEquals(true, (boolean) ReflectionTestUtils.getField(testingObject, "resultsSaved"));
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("unchecked")
	@Test
	public void testRunOk() throws JMSException, InterruptedException {
		doNothing().when(relayClient).sendBytes(any(Session.class), any(MessageProducer.class), any(PublicKey.class), any(byte[].class));

		@SuppressWarnings({ "rawtypes" })
		final BlockingQueue<Byte> blockingQueue = (BlockingQueue) ReflectionTestUtils.getField(testingObject, "blockingQueue");
		blockingQueue.put((byte)0);
		blockingQueue.put((byte)1);
		blockingQueue.put((byte)-1);
		
		testingObject.run();
		
		verify(relayClient, times(2)).sendBytes(any(Session.class), any(MessageProducer.class), any(PublicKey.class), any(byte[].class));
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
	
	//-------------------------------------------------------------------------------------------------
	private TextMessage getTestTextMessage(final String message, final String queueName) {
		return new TextMessage() {
			public void setStringProperty(String name, String value) throws JMSException {}
			public void setShortProperty(String name, short value) throws JMSException {}
			public void setObjectProperty(String name, Object value) throws JMSException {}
			public void setLongProperty(String name, long value) throws JMSException {}
			public void setJMSType(String type) throws JMSException {}
			public void setJMSTimestamp(long timestamp) throws JMSException {}
			public void setJMSReplyTo(Destination replyTo) throws JMSException {}
			public void setJMSRedelivered(boolean redelivered) throws JMSException {}
			public void setJMSPriority(int priority) throws JMSException {}
			public void setJMSMessageID(String id) throws JMSException {}
			public void setJMSExpiration(long expiration) throws JMSException {}
			public void setJMSDestination(Destination destination) throws JMSException {}
			public void setJMSDeliveryTime(long deliveryTime) throws JMSException {}
			public void setJMSDeliveryMode(int deliveryMode) throws JMSException {}
			public void setJMSCorrelationIDAsBytes(byte[] correlationID) throws JMSException {}
			public void setJMSCorrelationID(String correlationID) throws JMSException {}
			public void setIntProperty(String name, int value) throws JMSException {}
			public void setFloatProperty(String name, float value) throws JMSException {}
			public void setDoubleProperty(String name, double value) throws JMSException {}
			public void setByteProperty(String name, byte value) throws JMSException {}
			public void setBooleanProperty(String name, boolean value) throws JMSException {}
			public boolean propertyExists(String name) throws JMSException { return false; }
			@SuppressWarnings("rawtypes") public boolean isBodyAssignableTo(Class c) throws JMSException { return false; }
			public String getStringProperty(String name) throws JMSException { return null; }
			public short getShortProperty(String name) throws JMSException { return 0; }
			@SuppressWarnings("rawtypes") public Enumeration getPropertyNames() throws JMSException { return null; }
			public Object getObjectProperty(String name) throws JMSException { return null; }
			public long getLongProperty(String name) throws JMSException { return 0; }
			public String getJMSType() throws JMSException { return null; }
			public long getJMSTimestamp() throws JMSException {	return 0; }
			public Destination getJMSReplyTo() throws JMSException { return null; }
			public boolean getJMSRedelivered() throws JMSException { return false; }
			public int getJMSPriority() throws JMSException { return 0; }
			public String getJMSMessageID() throws JMSException { return null; }
			public long getJMSExpiration() throws JMSException { return 0; }
			public Destination getJMSDestination() throws JMSException { return getTestQueue(queueName); }
			public long getJMSDeliveryTime() throws JMSException { return 0; }
			public int getJMSDeliveryMode() throws JMSException { return 0; }
			public byte[] getJMSCorrelationIDAsBytes() throws JMSException { return null; }
			public String getJMSCorrelationID() throws JMSException { return null; }
			public int getIntProperty(String name) throws JMSException { return 0; }
			public float getFloatProperty(String name) throws JMSException { return 0; }
			public double getDoubleProperty(String name) throws JMSException { return 0; }
			public byte getByteProperty(String name) throws JMSException { return 0; }
			public boolean getBooleanProperty(String name) throws JMSException { return false; }
			public <T> T getBody(Class<T> c) throws JMSException { return null; }
			public void clearProperties() throws JMSException {}
			public void clearBody() throws JMSException {}
			public void acknowledge() throws JMSException {}
			public void setText(String string) throws JMSException {}
			public String getText() throws JMSException { return message; }
		};
	}
	
	//-------------------------------------------------------------------------------------------------
	private Queue getTestQueue(final String queueName) {
		return new Queue() {
			public String getQueueName() throws JMSException { return queueName; }
		};
	}
}