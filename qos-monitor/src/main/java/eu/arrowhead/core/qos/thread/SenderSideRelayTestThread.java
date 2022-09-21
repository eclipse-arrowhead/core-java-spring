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

import java.security.PublicKey;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.commons.lang3.RandomUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;

import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.internal.CloudResponseDTO;
import eu.arrowhead.common.dto.internal.RelayResponseDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.core.qos.database.service.RelayTestDBService;
import eu.arrowhead.relay.gateway.GatewayRelayClient;

public class SenderSideRelayTestThread extends Thread implements MessageListener {
	
	//=================================================================================================
	// members
	
	private static final String CLOSE_MESSAGE_PREFIX = "CLOSE";
	private static final int CLOSE_RETRY_PERIOD = 5000; // in milliseconds

	private static final Logger logger = LogManager.getLogger(SenderSideRelayTestThread.class);
	private static final Byte AWAKE_MESSAGE_ID = -1;
	private static final long MAX_WAITING_BEFORE_TERMINATE = 30; // in minutes
	
	private boolean interrupted = false;
	private boolean initialized = false;
	
	private final RelayTestDBService relayTestDBService;
	private final CloudResponseDTO targetCloud;
	private final RelayResponseDTO relay;

	private final GatewayRelayClient relayClient;
	private final Session relaySession;
	private final PublicKey receiverQoSMonitorPublicKey;
	
	private final byte noIteration;
	private final int testMessageSize;
	private final long timeout; // in milliseconds
	
	private boolean senderFlag = true;
	private final Map<Byte,long[]> testResults = new ConcurrentHashMap<>();
	private boolean resultsSaved = false;
	
	private final String queueId;
	private MessageProducer sender;
	private MessageProducer controlSender;
	private MessageConsumer receiver;
	private MessageConsumer controlReceiver;
	
	private final BlockingQueue<Byte> blockingQueue = new LinkedBlockingQueue<>();

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public SenderSideRelayTestThread(final ApplicationContext appContext, final GatewayRelayClient relayClient, final Session relaySession, final CloudResponseDTO targetCloud,
								   	   final RelayResponseDTO relay, final String receiverQoSMonitorPublicKey, final String queueId, final byte noIteration, final int testMessageSize,
								   	   final long timeout) {
		Assert.notNull(appContext, "appContext is null.");
		Assert.notNull(relayClient, "relayClient is null.");
		Assert.notNull(relaySession, "relaySession is null.");
		Assert.isTrue(!relayClient.isConnectionClosed(relaySession), "relaySession is closed.");
		Assert.notNull(targetCloud, "targetCloud is null.");
		Assert.notNull(relay, "relay is null");
		Assert.isTrue(!Utilities.isEmpty(receiverQoSMonitorPublicKey), "public key is null or blank.");
		Assert.isTrue(!Utilities.isEmpty(queueId), "queueId is null or blank.");
		Assert.isTrue(noIteration > 0, "Number of iteration must be positive.");
		Assert.isTrue(testMessageSize > 0, "Test message's size must be positive.");
		Assert.isTrue(timeout > 0, "Timeout must be positive.");
		
		this.relayTestDBService = appContext.getBean(RelayTestDBService.class);
		this.relayClient = relayClient;
		this.relaySession = relaySession;
		this.targetCloud = targetCloud;
		this.relay = relay;
		this.receiverQoSMonitorPublicKey = Utilities.getPublicKeyFromBase64EncodedString(receiverQoSMonitorPublicKey);
		this.queueId = queueId;
		this.noIteration = noIteration;
		this.testMessageSize = testMessageSize;
		this.timeout = timeout;
		
		setName("TEST-SENDER-" + targetCloud.getName() + "." + targetCloud.getOperator() + "|" + relay.getAddress() + ":" + relay.getPort());
	}
	
	//-------------------------------------------------------------------------------------------------
	public void init(final MessageProducer sender, final MessageProducer controlSender, final MessageConsumer receiver, final MessageConsumer controlReceiver) {
		logger.debug("init started...");
		
		Assert.notNull(sender, "sender is null.");
		Assert.notNull(controlSender, "controlSender is null.");
		Assert.notNull(receiver, "receiver is null.");
		Assert.notNull(controlReceiver, "controlReceiver is null.");
		
		this.sender = sender;
		this.controlSender = controlSender;
		this.receiver = receiver;
		this.controlReceiver = controlReceiver;
		this.initialized = true;
	}
	
	//-------------------------------------------------------------------------------------------------
	public boolean isInitialized() {
		return initialized;
	}

	//-------------------------------------------------------------------------------------------------
	@Override
	public void onMessage(final Message message) {
		logger.debug("onMessage started...");

		try {
			if (isControlMessage(message)) {
				handleControlMessage(message);
			} else {
				final byte[] bytes = relayClient.getBytesFromMessage(message, receiverQoSMonitorPublicKey);
				if (senderFlag) {
					final long end = System.currentTimeMillis();
					final byte id = bytes[0];
					if (testResults.containsKey(id) && testResults.get(id)[1] <= 0) {
						testResults.get(id)[1] = end;
						if (end - testResults.get(id)[0] < timeout) {
							try {
								blockingQueue.put(id);
							} catch (final InterruptedException ex) {
								// never happens
								logger.debug(ex.getMessage());
								logger.debug("Stacktrace:", ex);
								closeAndInterrupt();
							}
							
							final boolean lastMessage = id == noIteration - 1;
							if (lastMessage) {
								saveResultsAndSwitchRoles();
							}
						}
					}
				} else {
					relayClient.sendBytes(relaySession, sender, receiverQoSMonitorPublicKey, bytes);
				}
			}
		} catch (final JMSException | ArrowheadException ex) {
			logger.debug("Problem occurs in gateway communication: {}", ex.getMessage());
			logger.debug("Stacktrace:", ex);
			closeAndInterrupt();
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Override
	public void interrupt() {
		logger.debug("interrupt started...");
		
		super.interrupt();
		interrupted = true;
	}
	
	//-------------------------------------------------------------------------------------------------
	public void setInterrupted(final boolean interrupted) { this.interrupted = interrupted; }
	
	//-------------------------------------------------------------------------------------------------
	@Override
	public void run() {
		logger.debug("run started...");

		if (!initialized) {
			throw new IllegalStateException("Thread is not initialized.");
		}
		
		testRun();
		
		if (interrupted) {
			close();
			return;
		}
		
		try {
			blockingQueue.poll(MAX_WAITING_BEFORE_TERMINATE, TimeUnit.MINUTES); // waiting for the other side to finish test run
		} catch (final InterruptedException ex) {
			logger.debug(ex.getMessage());
			logger.debug("Stacktrace:", ex);
			return;
		} finally {
			close();
		}
	}

	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private void testRun() {
		for (byte b = 0; b < noIteration; ++b) {
			if (interrupted) {
				close();
				return;
			}
			
			final byte[] generatedBytes = generateTestMessage();
			final byte[] testMessage = new byte[testMessageSize];
			testMessage[0] = b; // id of the message
			System.arraycopy(generatedBytes, 0, testMessage, 1, generatedBytes.length);
			final long[] times = new long[2];
			final long start = System.currentTimeMillis();
			times[0] = start;
			testResults.put(b, times);
			try {
				final boolean lastMessage = b == noIteration - 1;
				relayClient.sendBytes(relaySession, sender, receiverQoSMonitorPublicKey, testMessage);
				
				while (true) {
					final Byte result = blockingQueue.poll(timeout, TimeUnit.MILLISECONDS); // wait for the echo before new iteration
					if (result == null) { // means timeout
						times[1] = start + timeout + 1;
						if (lastMessage) {
							saveResultsAndSwitchRoles();
						}
						break;
					} else if (result.byteValue() == b) { // means the correct answer is arrived 
						break;
					} else if (result.byteValue() < 0) {
						// means an awake message is arrived, which is not expected at this point => put back just to finish the thread
						logger.debug("Unexpected awake message arrived while waiting for message " + b);
						blockingQueue.put(AWAKE_MESSAGE_ID);
						break;
					}
				}
			} catch (final JMSException | ArrowheadException | InterruptedException ex) {
				logger.debug("Problem occurs in gateway communication: {}", ex.getMessage());
				logger.debug("Stacktrace:", ex);
				relayTestDBService.logErrorIntoMeasurementsTable(targetCloud, relay, b, ex.getMessage(), ex);
				closeAndInterrupt();
			}
		}
	}

	//-------------------------------------------------------------------------------------------------
	private byte[] generateTestMessage() {
		return RandomUtils.nextBytes(testMessageSize - 1);
	}

	//-------------------------------------------------------------------------------------------------
	private void close() {
		logger.debug("close started...");
		
		//Unsubscribe from the response (RESP-...) queue
		try {
			relayClient.unsubscribeFromQueues(receiver, controlReceiver);
		} catch (final JMSException ex) {
			logger.debug("Error while unsubscribing from response queues: {}", ex.getMessage());
			logger.debug("Stacktrace:", ex);
		}
		
		//Destroy destinations (REQ-... queues) which this sender side QoS writes on and the connection after		
		boolean relayConnectionClosed = relayClient.isConnectionClosed(relaySession);
		boolean requestQueuesClosed = false;
		
		while (!relayConnectionClosed) {
			
			if (!requestQueuesClosed) {
				try {
					requestQueuesClosed = relayClient.destroyQueues(relaySession, sender, controlSender);
				} catch (final JMSException ex) {
					logger.debug("Error while closing relay destination: {}", ex.getMessage());
					logger.debug("Stacktrace:", ex);
				}				
			}
			
			if (!requestQueuesClosed) {
				logger.debug("Relay connection is not closeable yet");
				await(CLOSE_RETRY_PERIOD);
				
			} else {
				relayClient.closeConnection(relaySession);
				if (!relayClient.isConnectionClosed(relaySession)) {
					logger.debug("Could not close relay connection");
					await(CLOSE_RETRY_PERIOD);
					
				} else {
					relayConnectionClosed = true;
					logger.debug("Relay connection has been closed");					
				}
			}			
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private void closeAndInterrupt() {
		close();
		interrupt();
	}
	
	//-------------------------------------------------------------------------------------------------
	private boolean isControlMessage(final Message message) throws JMSException {
		logger.debug("isControlMessage started...");
		
		final Destination destination = message.getJMSDestination();
		final Queue queue = (Queue) destination;
		
		return queue.getQueueName().endsWith(GatewayRelayClient.CONTROL_QUEUE_SUFFIX);
	}
	
	//-------------------------------------------------------------------------------------------------
	private boolean isCloseMessage(final Message message) throws JMSException {
		logger.debug("isCloseMessage started...");
		
		if (message instanceof TextMessage) {
			final TextMessage textMessage = (TextMessage) message;
			return textMessage.getText().startsWith(CLOSE_MESSAGE_PREFIX);
		}
		
		return false;
	}
	
	//-------------------------------------------------------------------------------------------------
	private void handleControlMessage(final Message message) throws JMSException {
		logger.debug("handleControlMessage started...");
		
		if (isCloseMessage(message)) {
			relayClient.handleCloseControlMessage(message, null); // just verifying the control msg, but not close the connection
			try {
				blockingQueue.put(AWAKE_MESSAGE_ID);
			} catch (final InterruptedException ex) {
				// never happens
				logger.debug(ex.getMessage());
				logger.debug("Stacktrace:", ex);
			}
			closeAndInterrupt();
		} else { // SWITCH control message
			// not supposed to receive this kind of messages
			throw new ArrowheadException("Unexpected message on relay: SWITCH");
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private synchronized void saveResultsAndSwitchRoles() throws JMSException {
		if (!resultsSaved) {
			relayTestDBService.storeMeasurements(targetCloud, relay, testResults);
			resultsSaved = true;
			senderFlag = false;
			relayClient.sendSwitchControlMessage(relaySession, controlSender, queueId);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private void await(final long milisec) {
		try {
			Thread.sleep(milisec);
		} catch (final InterruptedException ex) {
			logger.debug("Thread.sleep() is interrupted");
		}
	}
}