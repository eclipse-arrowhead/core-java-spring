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

package eu.arrowhead.relay.gateway.activemq;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQSession;
import org.apache.activemq.ActiveMQSslConnectionFactory;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.Assert;
import org.springframework.web.util.UriComponents;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.SSLProperties;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.internal.DecryptedMessageDTO;
import eu.arrowhead.common.exception.AuthException;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.relay.RelayCryptographer;
import eu.arrowhead.relay.gateway.ConsumerSideRelayInfo;
import eu.arrowhead.relay.gateway.ControlRelayInfo;
import eu.arrowhead.relay.gateway.GatewayRelayClient;
import eu.arrowhead.relay.gateway.ProviderSideRelayInfo;

public class ActiveMQGatewayRelayClient implements GatewayRelayClient {

	//=================================================================================================
	// members
	
	private static final String TCP = "tcp";
	private static final String SSL = "ssl";
	private static final String CLOSE_COMMAND = "CLOSE ";
	private static final String SWITCH_COMMAND = "SWITCH ";
	
	private static final int CLIENT_ID_LENGTH = 16;
	private static final int QUEUE_ID_LENGTH = 48;

	private static final Logger logger = LogManager.getLogger(ActiveMQGatewayRelayClient.class);
	
	private final String serverCommonName;
	private final SSLProperties sslProps;
	private final RelayCryptographer cryptographer;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public ActiveMQGatewayRelayClient(final String serverCommonName, final PrivateKey privateKey, final SSLProperties sslProps) {
		Assert.isTrue(!Utilities.isEmpty(serverCommonName), "Common name is null or blank.");
		Assert.notNull(privateKey, "Private key is null.");
		Assert.notNull(sslProps, "SSL properties object is null.");
		
		this.serverCommonName = serverCommonName;
		this.cryptographer = new RelayCryptographer(privateKey);
		this.sslProps = sslProps;
	}

	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings({ "squid:S2095", "resource" })
	@Override
	public Session createConnection(final String host, final int port, final boolean secure) throws JMSException {
		logger.debug("createConnection started...");
		
		Assert.isTrue(!Utilities.isEmpty(host), "Host is null or blank.");
		Assert.isTrue(port > CommonConstants.SYSTEM_PORT_RANGE_MIN && port < CommonConstants.SYSTEM_PORT_RANGE_MAX, "Port is invalid.");
		
		final Connection connection = secure ? createSSLConnection(host, port) : createTCPConnection(host, port);
		
		try {
			connection.start();
			
			return connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		} catch (final JMSException ex) {
			connection.close();
			throw ex;
		}
	}


	//-------------------------------------------------------------------------------------------------
	@Override
	public void closeConnection(final Session session) {
		logger.debug("closeConnection started...");
		if (session != null) {
			try {
				session.close();
				if (session instanceof ActiveMQSession) {
					final ActiveMQSession amqs = (ActiveMQSession) session;
					amqs.getConnection().close();
				}
			} catch (final JMSException ex) {
				logger.debug(ex.getMessage());
				logger.trace("Stacktrace:", ex);
			}
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Override
	public boolean isConnectionClosed(final Session session) {
		if (session instanceof ActiveMQSession) {
			return ((ActiveMQSession) session).isClosed();
		}
		
		return true;
	}

	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S2095")
	@Override
	public ProviderSideRelayInfo initializeProviderSideRelay(final Session session, final MessageListener listener) throws JMSException {
		logger.debug("initializeProviderSideRelay started...");

		Assert.notNull(session, "session is null.");
		Assert.notNull(listener, "listener is null.");
		
		final String queueId = RandomStringUtils.randomAlphanumeric(QUEUE_ID_LENGTH);
		final String requestQueueName = REQUEST_QUEUE_PREFIX + serverCommonName + "-" + queueId;
		final String requestControlQueueName = requestQueueName + CONTROL_QUEUE_SUFFIX;
		final String responseQueueName = RESPONSE_QUEUE_PREFIX + serverCommonName + "-" + queueId;
		final String responseControlQueueName = responseQueueName + CONTROL_QUEUE_SUFFIX;
		
		final Queue requestQueue = session.createQueue(requestQueueName);
		final Queue requestControlQueue = session.createQueue(requestControlQueueName);
		
		final MessageConsumer consumer = session.createConsumer(requestQueue);
		consumer.setMessageListener(listener);
		
		final MessageConsumer controlConsumer = session.createConsumer(requestControlQueue);
		controlConsumer.setMessageListener(listener);
		
		final Queue responseQueue = session.createQueue(responseQueueName);
		final Queue responseControlQueue = session.createQueue(responseControlQueueName);
		
		final MessageProducer messageSender = session.createProducer(responseQueue);
		final MessageProducer controlMessageSender = session.createProducer(responseControlQueue);

		return new ProviderSideRelayInfo(serverCommonName, queueId, messageSender, controlMessageSender);
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S2095")
	@Override
	public ConsumerSideRelayInfo initializeConsumerSideRelay(final Session session, final MessageListener listener, final String peerName, final String queueId) throws JMSException {
		logger.debug("initializeConsumerSideRelay started...");
		
		Assert.notNull(session, "session is null.");
		Assert.notNull(listener, "listener is null.");
		Assert.isTrue(!Utilities.isEmpty(peerName), "peerName is null or blank.");
		Assert.isTrue(!Utilities.isEmpty(queueId), "queueId is null or blank.");
		
		final String requestQueueName = REQUEST_QUEUE_PREFIX + peerName + "-" + queueId;
		final String requestControlQueueName = requestQueueName + CONTROL_QUEUE_SUFFIX;
		final String responseQueueName = RESPONSE_QUEUE_PREFIX + peerName + "-" + queueId;
		final String responseControlQueueName = responseQueueName + CONTROL_QUEUE_SUFFIX;
		
		final Queue responseQueue = session.createQueue(responseQueueName);
		final Queue responseControlQueue = session.createQueue(responseControlQueueName);
		
		final MessageConsumer consumer = session.createConsumer(responseQueue);
		consumer.setMessageListener(listener);
		
		final MessageConsumer controlConsumer = session.createConsumer(responseControlQueue);
		controlConsumer.setMessageListener(listener);
		
		final Queue requestQueue = session.createQueue(requestQueueName);
		final Queue requestControlQueue = session.createQueue(requestControlQueueName);
		
		final MessageProducer messageSender = session.createProducer(requestQueue);
		final MessageProducer controlMessageSender = session.createProducer(requestControlQueue);

		return new ConsumerSideRelayInfo(messageSender, controlMessageSender);
	}
	
	
	//-------------------------------------------------------------------------------------------------
	@Override
	public ControlRelayInfo initializeControlRelay(final Session session, final String peerName, final String queueId) throws JMSException {
		logger.debug("initializeControlRelay started...");
		
		Assert.notNull(session, "session is null.");
		Assert.isTrue(!Utilities.isEmpty(peerName), "peerName is null or blank.");
		Assert.isTrue(!Utilities.isEmpty(queueId), "queueId is null or blank.");
		
		final String requestControlQueueName = REQUEST_QUEUE_PREFIX + peerName + "-" + queueId + CONTROL_QUEUE_SUFFIX;
		final Queue requestControlQueue = session.createQueue(requestControlQueueName);	
		final MessageProducer controlRequestMessageSender = session.createProducer(requestControlQueue);
		
		final String responsetControlQueueName = RESPONSE_QUEUE_PREFIX + peerName + "-" + queueId + CONTROL_QUEUE_SUFFIX;
		final Queue responseControlQueue = session.createQueue(responsetControlQueueName);
		final MessageProducer controlResponseMessageSender = session.createProducer(responseControlQueue);
		
		return new ControlRelayInfo(controlRequestMessageSender, controlResponseMessageSender);
	}

	//-------------------------------------------------------------------------------------------------
	@Override
	public void sendBytes(final Session session, final MessageProducer sender, final PublicKey peerPublicKey, final byte[] bytes) throws JMSException {
		logger.debug("sendBytes started...");
		
		Assert.notNull(session, "session is null.");
		Assert.notNull(sender, "sender is null.");
		Assert.notNull(peerPublicKey, "peerPublicKey is null.");
		Assert.isTrue(bytes != null && bytes.length > 0, "bytes array is null or empty.");
		
		final String encryptedBytes = cryptographer.encodeBytes(bytes, peerPublicKey);
		final TextMessage msg = session.createTextMessage(encryptedBytes);
		
		sender.send(msg);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Override
	public byte[] getBytesFromMessage(final Message msg, final PublicKey peerPublicKey) throws JMSException {
		logger.debug("getBytesFromMessage started...");
		
		Assert.notNull(msg, "Message is null.");
		Assert.notNull(peerPublicKey, "peerPublicKey is null.");
		
		if (msg instanceof TextMessage) {
			final TextMessage tmsg = (TextMessage) msg;
			final DecryptedMessageDTO decryptedMessage = cryptographer.decodeMessage(tmsg.getText(), peerPublicKey);
			if (!CoreCommonConstants.RELAY_MESSAGE_TYPE_RAW.equals(decryptedMessage.getMessageType())) {
				throw new AuthException("Unauthorized message on queue.");
			}
			
			return Base64.getDecoder().decode(decryptedMessage.getPayload());
		}
		
		throw new JMSException("Invalid message class: " + msg.getClass().getSimpleName());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Override
	public void sendCloseControlMessage(final Session session, final MessageProducer sender, final String queueId) throws JMSException {
		logger.debug("sendCloseControlMessage started...");
		
		Assert.notNull(session, "session is null.");
		Assert.notNull(sender, "sender is null.");
		Assert.isTrue(!Utilities.isEmpty(queueId), "queueId is null or blank.");
		
		if (sender.getDestination() instanceof Queue) {
			final Queue controlQueue = (Queue) sender.getDestination();
			final String suffix = "-" + queueId + CONTROL_QUEUE_SUFFIX;
			if (!controlQueue.getQueueName().endsWith(suffix)) {
				throw new AuthException("Sender can't send control messages.");
			}
			
			final TextMessage msg = session.createTextMessage(CLOSE_COMMAND + queueId);
			
			sender.send(msg);
		} else {
			throw new JMSException("Invalid destination class: " + sender.getDestination().getClass().getSimpleName());
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Override
	public void handleCloseControlMessage(final Message msg, final Session session) throws JMSException {
		logger.debug("handleCloseControlMessage started...");
		
		Assert.notNull(msg, "Message is null.");
		Assert.notNull(session, "session is null.");
		
		if (msg instanceof TextMessage) {
			final TextMessage tmsg = (TextMessage) msg;
			final String queueId = parseCloseCommand(tmsg.getText());
			if (tmsg.getJMSDestination() instanceof Queue) {
				final Queue controlQueue = (Queue) tmsg.getJMSDestination();
				final String suffix = "-" + queueId + CONTROL_QUEUE_SUFFIX;
				if (!controlQueue.getQueueName().endsWith(suffix)) {
					throw new AuthException("Unauthorized close command: " + tmsg.getText());
				}
				
				session.close();
			} else {
				throw new JMSException("Invalid destination class: " + tmsg.getJMSDestination().getClass().getSimpleName());
			}
		} else {
			throw new JMSException("Invalid message class: " + msg.getClass().getSimpleName());
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Override
	public void sendSwitchControlMessage(final Session session, final MessageProducer sender, final String queueId) throws JMSException {
		logger.debug("sendSwitchControlMessage started...");
		
		Assert.notNull(session, "session is null.");
		Assert.notNull(sender, "sender is null.");
		Assert.isTrue(!Utilities.isEmpty(queueId), "queueId is null or blank.");
		
		if (sender.getDestination() instanceof Queue) {
			final Queue controlQueue = (Queue) sender.getDestination();
			final String suffix = "-" + queueId + CONTROL_QUEUE_SUFFIX;
			if (!controlQueue.getQueueName().endsWith(suffix)) {
				throw new AuthException("Sender can't send control messages.");
			}
			
			final TextMessage msg = session.createTextMessage(SWITCH_COMMAND + queueId);
			
			sender.send(msg);
		} else {
			throw new JMSException("Invalid destination class: " + sender.getDestination().getClass().getSimpleName());
		}
	}

	//-------------------------------------------------------------------------------------------------
	@Override
	public void validateSwitchControlMessage(final Message msg) throws JMSException {
		logger.debug("isSwitchControlMessage started...");
		
		Assert.notNull(msg, "Message is null.");
		
		if (msg instanceof TextMessage) {
			final TextMessage tmsg = (TextMessage) msg;
			final String queueId = parseSwitchCommand(tmsg.getText());
			if (tmsg.getJMSDestination() instanceof Queue) {
				final Queue controlQueue = (Queue) tmsg.getJMSDestination();
				final String suffix = "-" + queueId + CONTROL_QUEUE_SUFFIX;
				if (!controlQueue.getQueueName().endsWith(suffix)) {
					throw new AuthException("Unauthorized switch command: " + tmsg.getText());
				}
			} else {
				throw new JMSException("Invalid destination class: " + tmsg.getJMSDestination().getClass().getSimpleName());
			}
		} else {
			throw new JMSException("Invalid message class: " + msg.getClass().getSimpleName());
		}
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private Connection createTCPConnection(final String host, final int port) throws JMSException {
		final UriComponents uri = Utilities.createURI(TCP, host, port, null);
		final ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(uri.toUri());
		connectionFactory.setClientID(RandomStringUtils.randomAlphanumeric(CLIENT_ID_LENGTH));
		final Connection connection = connectionFactory.createConnection();
		connectionFactory.setClientID(null);

		return connection;
	}
	
	//-------------------------------------------------------------------------------------------------
	private Connection createSSLConnection(final String host, final int port) throws JMSException {
		final UriComponents uri = Utilities.createURI(SSL, host, port, null);
		final ActiveMQSslConnectionFactory connectionFactory = new ActiveMQSslConnectionFactory(uri.toUri());
		try {
			connectionFactory.setClientID(RandomStringUtils.randomAlphanumeric(CLIENT_ID_LENGTH));
			connectionFactory.setKeyStoreType(sslProps.getKeyStoreType());
			connectionFactory.setKeyStore(sslProps.getKeyStore().getURI().toString());
			connectionFactory.setKeyStorePassword(sslProps.getKeyStorePassword());
			connectionFactory.setKeyStoreKeyPassword(sslProps.getKeyPassword());
			connectionFactory.setTrustStoreType(sslProps.getKeyStoreType());
			connectionFactory.setTrustStore(sslProps.getTrustStore().getURI().toString());
			connectionFactory.setTrustStorePassword(sslProps.getTrustStorePassword());
			
			final Connection connection = connectionFactory.createConnection();
			connectionFactory.setClientID(null);
			
			return connection;
		} catch (final JMSException ex) {
			throw ex;
		} catch (final Exception ex) {
			logger.debug(ex.getMessage());
			logger.debug("Stacktrace: ", ex);
			throw new JMSException("Error while creating SSL connection: " + ex.getMessage());
		}
	}

	
	//-------------------------------------------------------------------------------------------------
	private String parseCloseCommand(final String command) {
		final String cmd = command.trim();
		
		if (!cmd.startsWith(CLOSE_COMMAND)) {
			throw new InvalidParameterException("Invalid command: " + cmd);
		}
		
		final String[] parts = cmd.split(" ");
		if (parts.length < 2) {
			throw new InvalidParameterException("Missing queue id");
		}
		
		return parts[1].trim();
	}
	
	//-------------------------------------------------------------------------------------------------
	private String parseSwitchCommand(final String command) {
		final String cmd = command.trim();
		
		if (!cmd.startsWith(SWITCH_COMMAND)) {
			throw new InvalidParameterException("Invalid command: " + cmd);
		}
		
		final String[] parts = cmd.split(" ");
		if (parts.length < 2) {
			throw new InvalidParameterException("Missing queue id");
		}
		
		return parts[1].trim();
	}
}