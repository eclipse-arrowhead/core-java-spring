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

package eu.arrowhead.core.gateway.thread;

import java.io.IOException;
import java.security.PublicKey;
import java.time.ZonedDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.SSLProperties;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.internal.GatewayProviderConnectionRequestDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.core.gateway.service.ActiveSessionDTO;
import eu.arrowhead.core.gateway.thread.GatewayHTTPUtils.Answer;
import eu.arrowhead.relay.gateway.GatewayRelayClient;

public class ProviderSideSocketThreadHandler implements MessageListener {
	
	//=================================================================================================
	// members
	
	private static final Logger logger = LogManager.getLogger(ProviderSideSocketThreadHandler.class);
	
	private final GatewayRelayClient relayClient;
	private final Session relaySession;
	private final GatewayProviderConnectionRequestDTO connectionRequest;
	private final PublicKey consumerGatewayPublicKey;
	private final int timeout;
	private final int maxRequestPerSocket;
	private final ConcurrentMap<String,ActiveSessionDTO> activeSessions;
	private final ConcurrentMap<String,ProviderSideSocketThreadHandler> activeProviderSideSocketThreadHandlers;
	private final SSLProperties sslProperties;
	
	private String queueId;
	private MessageProducer sender;
	private MessageProducer senderControl;
	private MessageConsumer consumer;
	private MessageConsumer consumerControl;
	
	private SSLSocketFactory socketFactory;
	private ProviderSideSocketThread oldThread;
	private ProviderSideSocketThread currentThread;
	private int noRequest = 0;
	
	private boolean firstMessage = true;
	private boolean countRequests = false;
	
	private boolean initialized = false;
	private boolean communicationStarted = false;
	
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("unchecked")
	public ProviderSideSocketThreadHandler(final ApplicationContext appContext, final GatewayRelayClient relayClient, final Session relaySession, final GatewayProviderConnectionRequestDTO connectionRequest,
										   final int timeout, final int maxRequestPerSocket) {
		Assert.notNull(appContext, "appContext is null.");
		Assert.notNull(relayClient, "relayClient is null.");
		Assert.notNull(relaySession, "relaySession is null.");
		Assert.isTrue(!relayClient.isConnectionClosed(relaySession), "relaySession is closed.");
		
		validateConnectionRequest(connectionRequest);
		
		this.relayClient = relayClient;
		this.relaySession = relaySession;
		this.connectionRequest = connectionRequest;
		this.timeout = timeout;
		this.maxRequestPerSocket = maxRequestPerSocket;
		this.consumerGatewayPublicKey = Utilities.getPublicKeyFromBase64EncodedString(this.connectionRequest.getConsumerGWPublicKey());
		this.activeSessions = appContext.getBean(CoreCommonConstants.GATEWAY_ACTIVE_SESSION_MAP, ConcurrentHashMap.class);
		this.activeProviderSideSocketThreadHandlers = appContext.getBean(CoreCommonConstants.GATEWAY_ACTIVE_PROVIDER_SIDE_SOCKET_THREAD_HANDLER_MAP, ConcurrentHashMap.class);
		this.sslProperties = appContext.getBean(SSLProperties.class);
	}
	
	//-------------------------------------------------------------------------------------------------
	public void init(final String queueId, final MessageProducer sender, final MessageProducer senderControl,
					 final MessageConsumer consumer, final MessageConsumer consumerControl) {
		logger.debug("Provider handler init started...");
		
		Assert.isTrue(!Utilities.isEmpty(queueId), "Queue id is null or blank.");
		Assert.notNull(sender, "sender is null.");
		Assert.notNull(senderControl, "senderControl is null.");
		Assert.notNull(consumer, "consumer is null.");
		Assert.notNull(consumerControl, "consumerControl is null.");
		
		this.queueId = queueId;
		this.sender = sender;
		this.senderControl = senderControl;
		this.consumer = consumer;
		this.consumerControl = consumerControl;
		
		final SSLContext sslContext = SSLContextFactory.createGatewaySSLContext(sslProperties);
		socketFactory = sslContext.getSocketFactory();
		currentThread = new ProviderSideSocketThread(relayClient, relaySession, socketFactory, connectionRequest, consumerGatewayPublicKey, timeout, sender);
		try {
			currentThread.init();
			currentThread.start();

			this.initialized = true;
		} catch (final IOException ex) {
			logger.debug("Problem occurs in gateway communication: {}", ex.getMessage());
			logger.debug("Stacktrace:", ex);

			throw new ArrowheadException("Error occured when initialize relay communication.", HttpStatus.SC_BAD_GATEWAY, ex);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	public boolean isInitialized() {
		return initialized;
	}
	
	//-------------------------------------------------------------------------------------------------
	public boolean isCommunicationStarted() {
		return communicationStarted;
	}
	
	//-------------------------------------------------------------------------------------------------
	public ZonedDateTime getLastInteractionTime() {
		return this.currentThread.getLastInteractionTime();
	}

	//-------------------------------------------------------------------------------------------------
	@Override
	public void onMessage(final Message message) {
		logger.debug("onMessage started...");
		
		if (!initialized) {
			throw new IllegalStateException("Handler is not initialized.");
		}
		
		try {
			if (isControlMessage(message)) {
				relayClient.handleCloseControlMessage(message, null); // just verifying the control msg, but not close the connection
				close();
			} else {
				Assert.notNull(currentThread.getOutputStream(), "Output stream is null.");
				currentThread.setNowAsLastInteractionTime();
				final byte[] bytes = relayClient.getBytesFromMessage(message, consumerGatewayPublicKey);
				communicationStarted = true;
				
				if (firstMessage) { // need to decide whether use request counter or not
					initCountRequestsFlag(bytes);
					firstMessage = false;
				}
				
				incrementRequestCounter();
				replaceThreadIfNecessary();
				currentThread.getOutputStream().write(bytes);
			}
		} catch (final JMSException | ArrowheadException | IOException ex) {
			logger.debug("Problem occurs in gateway communication: {}", ex.getMessage());
			logger.debug("Stacktrace:", ex);
			close();
		}
	}

	//-------------------------------------------------------------------------------------------------
	public void close() {
		logger.debug("Provider handler close started...");
		
		if (activeSessions != null && queueId != null) {
			activeSessions.remove(queueId);
		}
		
		if (oldThread != null) {
			oldThread.closeAndInterrupt();
		}
		
		if (currentThread != null) {
			currentThread.closeAndInterrupt();
		}		
		
		//Unsubscribe from the request (REQ-...) queue
		try {
			unsubscribeFromRequestQueues();
		} catch (final JMSException ex) {
			logger.debug("Error while unsubscribing from request queues: {}", ex.getMessage());
			logger.debug("Stacktrace:", ex);
		}
		
		//Attempt to destroy destinations (RESP-... queues) which this provider side gateway writes on and the connection after
		boolean canCloseRelayConnection = false;
		try {
			canCloseRelayConnection = destroyResponseQueues();
		} catch (final JMSException ex) {
			logger.debug("Error while closing relay destination: {}", ex.getMessage());
			logger.debug("Stacktrace:", ex);
		}
		
		if (!canCloseRelayConnection) {
			logger.debug("Relay connection is not closeable yet");
			
		} else {
			relayClient.closeConnection(relaySession);
			if (relayClient.isConnectionClosed(relaySession) && activeProviderSideSocketThreadHandlers != null && queueId != null) {
				activeProviderSideSocketThreadHandlers.remove(queueId);
				logger.debug("Relay connection has been closed");
			}			
		}			
	}

	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private boolean isControlMessage(final Message message) throws JMSException {
		logger.debug("isControlMessage started...");
		
		final Destination destination = message.getJMSDestination();
		final Queue queue = (Queue) destination;
		
		return queue.getQueueName().endsWith(GatewayRelayClient.CONTROL_QUEUE_SUFFIX);
	}

	//-------------------------------------------------------------------------------------------------
	private void validateConnectionRequest(final GatewayProviderConnectionRequestDTO connectionRequest) {
		logger.debug("validateConnectionRequest started...");
		
		Assert.notNull(connectionRequest, "connectionRequest is null.");
		Assert.notNull(connectionRequest.getProvider(), "provider is null.");
		Assert.isTrue(!Utilities.isEmpty(connectionRequest.getProvider().getSystemName()), "provider name is null or blank.");
		Assert.isTrue(!Utilities.isEmpty(connectionRequest.getProvider().getAddress()), "provider address is null or blank.");
		Assert.notNull(connectionRequest.getProvider().getPort(), "provider port is null.");
		final int port = connectionRequest.getProvider().getPort().intValue();
		Assert.isTrue(port > CommonConstants.SYSTEM_PORT_RANGE_MIN && port < CommonConstants.SYSTEM_PORT_RANGE_MAX, "provider port is invalid.");
		Assert.isTrue(!Utilities.isEmpty(connectionRequest.getProvider().getAuthenticationInfo()), "provider authentication info is null or blank.");
		Assert.isTrue(!Utilities.isEmpty(connectionRequest.getServiceDefinition()), "Service definition is null or blank.");
		Assert.isTrue(!Utilities.isEmpty(connectionRequest.getConsumerGWPublicKey()), "Consumer gateway public key is null or blank.");
	}
	
	//-------------------------------------------------------------------------------------------------
	private void replaceThreadIfNecessary() {
		logger.debug("Provider handler replaceThread started...");
		
		if (noRequest > maxRequestPerSocket || currentThread.isInterrupted()) {
			// new thread needed because we reach the threshold or current thread is interrupted
			
			if (!currentThread.isInterrupted()) {
				// threshold reached
				
				if (oldThread != null) {
					logger.debug("Releasing thread: {}", oldThread.getName());
					oldThread.closeAndInterrupt();
				}
				
				oldThread = currentThread;
			}
			
			currentThread = new ProviderSideSocketThread(relayClient, relaySession, socketFactory, connectionRequest, consumerGatewayPublicKey, timeout, sender);
			logger.debug("Creating thread: {}", currentThread.getName());
			try {
				currentThread.init();
				noRequest = 1;
				logger.debug("Request counter reset");
				currentThread.start();
				logger.debug("Thread started: {}", currentThread.getName());
			} catch (final IOException ex) {
				logger.debug("Problem occurs in gateway communication: {}", ex.getMessage());
				logger.debug("Stacktrace:", ex);
				
				close();
			}
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private void incrementRequestCounter() {
		if (countRequests) {
			noRequest++;
			logger.debug("Request counter incremented to: {}", noRequest);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private void initCountRequestsFlag(final byte[] bytes) {
		logger.debug("initCountRequestsFlag started...");
		
		// we only need to count requests if the parameter bytes contains a non-chunked HTTP request
		
		final Answer isHttp = GatewayHTTPUtils.isStartOfAHttpRequest(bytes);
		
		if (isHttp == Answer.YES) {
			final Answer isChunked = GatewayHTTPUtils.isChunkedHttpRequest(bytes);
			
			// if the message is chunked we don't use request counting
			switch (isChunked) {
			case CAN_BE: 
				logger.error("Invalid answer: CAN_BE");
				// break is intentionally left here
			case YES:
				logger.debug("Request counter is off");
				countRequests = false;
				break;
			case NO: 
				logger.debug("Request counter is on");
				countRequests = true;
			}
		} else {
			logger.debug("Request counter is off");
			countRequests = false; // not HTTP
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private void unsubscribeFromRequestQueues() throws JMSException {
		if (relaySession != null) {
			relayClient.unsubscribeFromQueues(consumer, consumerControl);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private boolean destroyResponseQueues() throws JMSException {
		logger.debug("destroyResponseQueues started...");
		
		if (relaySession != null) {
			return relayClient.destroyQueues(relaySession, sender, senderControl);
		}		
		return false;
	}
}