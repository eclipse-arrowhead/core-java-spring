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
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceConfigurationError;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

import org.apache.http.HttpStatus;
import org.apache.http.util.ByteArrayBuffer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;

import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.SSLProperties;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.core.gateway.service.ActiveSessionDTO;
import eu.arrowhead.core.gateway.thread.GatewayHTTPUtils.Answer;
import eu.arrowhead.relay.gateway.GatewayRelayClient;

public class ConsumerSideServerSocketThread extends Thread implements MessageListener {
	
	//=================================================================================================
	// members

	private static final int BUFFER_SIZE = 1024;
	
	private static final Logger logger = LogManager.getLogger(ConsumerSideServerSocketThread.class);

	private final int port;
	private final GatewayRelayClient relayClient;
	private final Session relaySession;
	private final PublicKey providerGatewayPublicKey;
	private final String queueId;
	private final int timeout;
	private final ConcurrentMap<String,ActiveSessionDTO> activeSessions;
	private final ConcurrentLinkedQueue<Integer> availablePorts;
	private final SSLProperties sslProperties;

	private MessageProducer sender;
	private SSLServerSocket sslServerSocket;
	private SSLSocket sslConsumerSocket;
	private OutputStream outConsumer;
	private boolean interrupted = false;
	private boolean initialized = false;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("unchecked")
	public ConsumerSideServerSocketThread(final ApplicationContext appContext, final int port, final GatewayRelayClient relayClient, final Session relaySession, final String providerGatewayPublicKey,
									      final String queueId, final int timeout, final String consumerName, final String serviceDefinition) {
		super();
		Assert.notNull(appContext, "appContext is null.");
		Assert.notNull(relayClient, "relayClient is null.");
		Assert.notNull(relaySession, "relaySession is null.");
		Assert.isTrue(!relayClient.isConnectionClosed(relaySession), "relaySession is closed.");
		Assert.isTrue(!Utilities.isEmpty(providerGatewayPublicKey), "provider gateway public key is null or blank.");
		Assert.isTrue(!Utilities.isEmpty(queueId), "queue id is null or blank.");
		Assert.isTrue(!Utilities.isEmpty(consumerName), "consumer name is null or blank.");
		Assert.isTrue(!Utilities.isEmpty(serviceDefinition), "service definition is null or blank.");

		this.port = port;
		this.relayClient = relayClient;
		this.relaySession = relaySession;
		this.queueId = queueId;
		this.timeout = timeout;
		this.providerGatewayPublicKey = Utilities.getPublicKeyFromBase64EncodedString(providerGatewayPublicKey);
		this.activeSessions = appContext.getBean(CoreCommonConstants.GATEWAY_ACTIVE_SESSION_MAP, ConcurrentHashMap.class);
		this.availablePorts = appContext.getBean(CoreCommonConstants.GATEWAY_AVAILABLE_PORTS_QUEUE, ConcurrentLinkedQueue.class);
		this.sslProperties = appContext.getBean(SSLProperties.class);
		
		setName(consumerName + "." + serviceDefinition);
	}
	
	//-------------------------------------------------------------------------------------------------
	public void init(final MessageProducer sender) {
		logger.debug("init started...");
		
		Assert.notNull(sender, "sender is null.");
		
		this.sender = sender;
		
		try {
			final SSLContext sslContext = SSLContextFactory.createGatewaySSLContext(sslProperties);
			final SSLServerSocketFactory serverSocketFactory = sslContext.getServerSocketFactory();
			sslServerSocket = (SSLServerSocket) serverSocketFactory.createServerSocket(port);
			sslServerSocket.setNeedClientAuth(true);
			sslServerSocket.setSoTimeout(timeout);
			this.initialized = true;
		} catch (final Throwable ex) {
			logger.debug("Problem occurs in initializing gateway communication: {}", ex.getMessage());
			logger.debug("Stacktrace:", ex);
			close();
			
			throw new ArrowheadException("Problem occurs in initializing gateway communication: " + ex.getMessage(), HttpStatus.SC_BAD_GATEWAY, ex);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	public boolean isInitialized() {
		return initialized;
	}

	//-------------------------------------------------------------------------------------------------
	@Override
	public void onMessage(final Message message) {
		logger.debug("onMessage started...");
		
		Assert.notNull(outConsumer, "Output stream is null.");
		try {
			if (isControlMessage(message)) {
				relayClient.handleCloseControlMessage(message, relaySession);
				closeAndInterrupt();
			} else {
				final byte[] bytes = relayClient.getBytesFromMessage(message, providerGatewayPublicKey);
				logger.debug("FROM PROVIDER:" + new String(bytes, StandardCharsets.ISO_8859_1));
				outConsumer.write(bytes);
			}
		} catch (final JMSException | ArrowheadException | IOException ex) {
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

		try {
			sslConsumerSocket = (SSLSocket) sslServerSocket.accept();
			final InputStream inConsumer = sslConsumerSocket.getInputStream();
			outConsumer = sslConsumerSocket.getOutputStream();
			
			final GatewayHTTPRequestCache requestCache = new GatewayHTTPRequestCache(BUFFER_SIZE);
			final List<byte[]> byteArrayCache = new ArrayList<>();
			boolean contentDetected = false;
			boolean useHttpCache = false;
			
			while (true) {
				if (interrupted) {
					close();
					return;
				}
				
				final byte[] buffer = new byte[BUFFER_SIZE];
				final int size = inConsumer.read(buffer);
				
				if (size < 0) { // end of stream
					logger.debug("End of stream");
					closeAndInterrupt();
				} else {
					final byte[] data = new byte[size];
					System.arraycopy(buffer, 0, data, 0, size);
					logger.debug("FROM CONSUMER:" + new String(data, StandardCharsets.ISO_8859_1));
					
					if (!contentDetected) {
						logger.debug("Content detection section start...");
						byteArrayCache.add(data);
						final Answer canUseHttpCache = canUseHttpCache(byteArrayCache);
						contentDetected = canUseHttpCache != Answer.CAN_BE;
						useHttpCache = canUseHttpCache == Answer.YES;
						logger.debug("Content detected: {}, cache usage: {}", contentDetected, useHttpCache);

						switch (canUseHttpCache) {
						case YES: 
							logger.debug("Moving {} byte array(s) from byte array cache to HTTP cache", byteArrayCache.size() - 1);
							for (int i = 0; i < byteArrayCache.size() - 1; ++i) { // don't add the last one, because that one is added later
								requestCache.addBytes(byteArrayCache.get(i));
							}
							byteArrayCache.clear(); // clean
							logger.debug("Byte array cache cleared.");
							break;
						case NO:
							// send the whole cache content here
							logger.debug("Sending {} byte array(s) via relay as one message...", byteArrayCache.size());
							relayClient.sendBytes(relaySession, sender, providerGatewayPublicKey, concatenateByteArrays(byteArrayCache)); 
							break;
						case CAN_BE: 
							// no further action is necessary
						}
					}
					
					if (contentDetected) { 
						if (useHttpCache) {
							requestCache.addBytes(data);
							final byte[] requestBytes = requestCache.getHTTPRequestBytes();
							if (requestBytes != null) {
								// requestBytes contains a whole HTTP request
								logger.debug("Sending HTTP request via relay");
								relayClient.sendBytes(relaySession, sender, providerGatewayPublicKey, requestBytes);
							} else {
								// else waiting for more bytes
								logger.debug("Waiting for more bytes");
							}
						} else { // HTTP cache is not used
							if (byteArrayCache.size() > 0) { // means content detected in this iteration => content of data is already sent with the rest of the cache content
								logger.debug("Byte array cache content is already sent via relay, so no further action is necessary.");
								byteArrayCache.clear(); // clean cache (not used again as we already determined the content)
								logger.debug("Byte array cache cleared.");
							} else {
								logger.debug("Sending byte array via relay...");
								relayClient.sendBytes(relaySession, sender, providerGatewayPublicKey, data);
							}
						}
					}
				}
			}
		} catch (final IOException | JMSException | ArrowheadException | ServiceConfigurationError | IllegalArgumentException ex) {
			logger.debug("Problem occurs in gateway communication: {}", ex.getMessage());
			logger.debug("Stacktrace:", ex);
			closeAndInterrupt();
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
	private void close() {
		logger.debug("close started...");
		
		if (activeSessions != null && queueId != null) {
			activeSessions.remove(queueId);
		}
		
		if (sslConsumerSocket != null) {
			try {
				sslConsumerSocket.close();
			} catch (final IOException ex) {
				logger.debug("Error while closing socket: {}", ex.getMessage());
				logger.debug("Stacktrace:", ex);
			}
		}
		
		if (sslServerSocket != null) {
			try {
				sslServerSocket.close();
			} catch (final IOException ex) {
				logger.debug("Error while closing socket: {}", ex.getMessage());
				logger.debug("Stacktrace:", ex);
			}
		}
		
		if (availablePorts != null) {
			availablePorts.offer(port);
		}
		
		relayClient.closeConnection(relaySession);
	}
	
	//-------------------------------------------------------------------------------------------------
	private void closeAndInterrupt() {
		close();
		interrupt();
	}
	
	//-------------------------------------------------------------------------------------------------
	private Answer canUseHttpCache(final List<byte[]> byteArrays) {
		logger.debug("canUseHttpCache started...");
		
		final byte[] message = concatenateByteArrays(byteArrays);
		
		final Answer isHttp = GatewayHTTPUtils.isStartOfAHttpRequest(message);
		
		if (isHttp == Answer.YES) {
			final Answer isChunked = GatewayHTTPUtils.isChunkedHttpRequest(message);
			
			// if the message is chunked we can't use cache, so we have to 'negate' the result
			switch (isChunked) {
			case YES: 
				logger.debug("HTTP Cache answer: NO");
				return Answer.NO;
			case CAN_BE:
				logger.debug("HTTP Cache answer: CAN_BE");
				return Answer.CAN_BE;
			case NO: 
				logger.debug("HTTP Cache answer: YES");
				return Answer.YES;
			}
		}
	
		logger.debug("HTTP Cache answer: {}", isHttp.name());
		return isHttp; // NO or CAN_BE
	}
	
	//-------------------------------------------------------------------------------------------------
	private byte[] concatenateByteArrays(final List<byte[]> byteArrays) {
		logger.debug("concatenateByteArrays started...");
		
		final ByteArrayBuffer buffer = new ByteArrayBuffer(calculateLengthSum(byteArrays));
		for (final byte[] array : byteArrays) {
			buffer.append(array, 0, array.length);
		}
		
		return buffer.buffer();
	}
	
	//-------------------------------------------------------------------------------------------------
	private int calculateLengthSum(final List<byte[]> byteArrays) {
		logger.debug("calculateLengthSum started...");

		int result = 0;
		for (final byte[] array : byteArrays) {
			result += array.length;
		}
		
		return result;
	}
}