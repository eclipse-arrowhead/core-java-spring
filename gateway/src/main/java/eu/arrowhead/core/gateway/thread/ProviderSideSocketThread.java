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
import java.net.UnknownHostException;
import java.security.PublicKey;
import java.time.ZonedDateTime;
import java.util.ServiceConfigurationError;

import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.Assert;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.internal.GatewayProviderConnectionRequestDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.relay.gateway.GatewayRelayClient;

class ProviderSideSocketThread extends Thread {

	//=================================================================================================
	// member
	
	private static final int BUFFER_SIZE = 1024;
	
	private static final Logger logger = LogManager.getLogger(ProviderSideSocketThread.class);
	
	private final GatewayRelayClient relayClient;
	private final Session relaySession;
	private final SSLSocketFactory socketFactory;
	private final GatewayProviderConnectionRequestDTO connectionRequest;
	private final PublicKey consumerGatewayPublicKey;
	private final int timeout;
	private final MessageProducer sender;
	
	private SSLSocket sslProviderSocket;
	private OutputStream outputStream;

	private ZonedDateTime lastInteraction;
	private boolean initialized = false;
	private boolean interrupted = false;

	//-------------------------------------------------------------------------------------------------
	public ProviderSideSocketThread(final GatewayRelayClient relayClient, final Session relaySession, final SSLSocketFactory socketFactory, final GatewayProviderConnectionRequestDTO connectionRequest,
									final PublicKey consumerGatewayPublicKey, final int timeout, final MessageProducer sender) {
		Assert.notNull(relayClient, "relayClient is null.");
		Assert.notNull(relaySession, "relaySession is null.");
		Assert.isTrue(!relayClient.isConnectionClosed(relaySession), "relaySession is closed.");
		Assert.notNull(socketFactory, "socketFactory is null.");
		Assert.notNull(consumerGatewayPublicKey, "consumerGatewayPublicKey is null.");
		Assert.notNull(sender, "sender is null.");
		
		validateConnectionRequest(connectionRequest);
		
		this.relayClient = relayClient;
		this.relaySession = relaySession;
		this.socketFactory = socketFactory;
		this.connectionRequest = connectionRequest;
		this.consumerGatewayPublicKey = consumerGatewayPublicKey;
		this.timeout = timeout;
		this.sender = sender;
		
		setName(connectionRequest.getProvider().getSystemName() + "." + connectionRequest.getServiceDefinition() + "-" + System.currentTimeMillis());
	}
	
	//-------------------------------------------------------------------------------------------------
	public void init() throws UnknownHostException, IOException {
		logger.debug("{}: init started...", getName());

		sslProviderSocket = (SSLSocket) socketFactory.createSocket(connectionRequest.getProvider().getAddress(), connectionRequest.getProvider().getPort().intValue());
		sslProviderSocket.setSoTimeout(timeout);
		outputStream = sslProviderSocket.getOutputStream();
		initialized = true;
		lastInteraction = ZonedDateTime.now();
	}
	
	//-------------------------------------------------------------------------------------------------
	@Override
	public void interrupt() {
		super.interrupt();
		
		interrupted = true;
	}

	//-------------------------------------------------------------------------------------------------
	public OutputStream getOutputStream() {
		return outputStream;
	}
	
	//-------------------------------------------------------------------------------------------------
	public ZonedDateTime getLastInteractionTime() {
		return this.lastInteraction;
	}
	
	//-------------------------------------------------------------------------------------------------
	public void setNowAsLastInteractionTime() {
		this.lastInteraction = ZonedDateTime.now();
	}

	//-------------------------------------------------------------------------------------------------
	@Override
	public void run() {
		logger.debug("{}: run started...", getName());
		
		if (!initialized) {
			throw new IllegalStateException("Thread is not initialized.");
		}
		
		try {
			final InputStream inProvider = sslProviderSocket.getInputStream();

			while (true) {
				if (interrupted) {
					close();
					return;
				}
				
				final byte[] buffer = new byte[BUFFER_SIZE];
				final int size = inProvider.read(buffer);
				
				if (size < 0) { // end of stream
					closeAndInterrupt();
				} else {
					lastInteraction = ZonedDateTime.now();
					final byte[] data = new byte[size];
					System.arraycopy(buffer, 0, data, 0, size);
					relayClient.sendBytes(relaySession, sender, consumerGatewayPublicKey, data);
				}
			}
		} catch (final IOException | JMSException | ArrowheadException | ServiceConfigurationError | IllegalArgumentException ex) {
			logger.debug("{} : Problem occurs in gateway communication: {}", getName(), ex.getMessage());
			logger.debug("Stacktrace:", ex);
			closeAndInterrupt();
		}
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
	private void close() {
		logger.debug("{}: close started...", getName());
		
		if (sslProviderSocket != null) {
			try {
				sslProviderSocket.close();
			} catch (final IOException ex) {
				logger.debug("{}: Error while closing socket: {}", getName(), ex.getMessage());
				logger.debug("Stacktrace:", ex);
			}
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	public void closeAndInterrupt() {
		close();
		interrupt();
	}
}