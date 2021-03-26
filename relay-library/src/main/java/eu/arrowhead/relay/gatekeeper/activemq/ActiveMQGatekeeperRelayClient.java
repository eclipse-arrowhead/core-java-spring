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

package eu.arrowhead.relay.gatekeeper.activemq;

import java.io.IOException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQSession;
import org.apache.activemq.ActiveMQSslConnectionFactory;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.Assert;
import org.springframework.web.util.UriComponents;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.SSLProperties;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.internal.AccessTypeRelayResponseDTO;
import eu.arrowhead.common.dto.internal.DecryptedMessageDTO;
import eu.arrowhead.common.dto.internal.GSDPollRequestDTO;
import eu.arrowhead.common.dto.internal.GSDPollResponseDTO;
import eu.arrowhead.common.dto.internal.GeneralAdvertisementMessageDTO;
import eu.arrowhead.common.dto.internal.GeneralRelayRequestDTO;
import eu.arrowhead.common.dto.internal.ICNProposalRequestDTO;
import eu.arrowhead.common.dto.internal.ICNProposalResponseDTO;
import eu.arrowhead.common.dto.internal.QoSRelayTestProposalRequestDTO;
import eu.arrowhead.common.dto.internal.QoSRelayTestProposalResponseDTO;
import eu.arrowhead.common.dto.internal.SystemAddressSetRelayResponseDTO;
import eu.arrowhead.common.dto.shared.ErrorMessageDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.AuthException;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.relay.RelayCryptographer;
import eu.arrowhead.relay.gatekeeper.GatekeeperRelayClient;
import eu.arrowhead.relay.gatekeeper.GatekeeperRelayRequest;
import eu.arrowhead.relay.gatekeeper.GatekeeperRelayResponse;
import eu.arrowhead.relay.gatekeeper.GeneralAdvertisementResult;

public class ActiveMQGatekeeperRelayClient implements GatekeeperRelayClient {
	
	//=================================================================================================
	// members
	
	private static final String TCP = "tcp";
	private static final String SSL = "ssl";
	private static final String GENERATED_TOPIC_SUFFIX = "M5QTZXM9G9AnpPHWT6WennWu";
	private static final String GENERAL_TOPIC_NAME = "General-" + GENERATED_TOPIC_SUFFIX;
	private static final String REQUEST_QUEUE_PREFIX = "REQ-";
	private static final String RESPONSE_QUEUE_PREFIX = "RESP-";
	private static final String ERROR_CODE = "\"errorCode\"";
	
	private static final List<String> supportedRequestTypes = List.of(CoreCommonConstants.RELAY_MESSAGE_TYPE_GSD_POLL, CoreCommonConstants.RELAY_MESSAGE_TYPE_ICN_PROPOSAL,
																	  CoreCommonConstants.RELAY_MESSAGE_TYPE_ACCESS_TYPE, CoreCommonConstants.RELAY_MESSAGE_TYPE_SYSTEM_ADDRESS_LIST,
																	  CoreCommonConstants.RELAY_MESSAGE_TYPE_QOS_RELAY_TEST);	
	
	private static final int CLIENT_ID_LENGTH = 16;
	private static final int SESSION_ID_LENGTH = 48;

	private static final Logger logger = LogManager.getLogger(ActiveMQGatekeeperRelayClient.class);
	private static final ObjectMapper mapper = new ObjectMapper();
	
	private final String serverCommonName;
	private final PublicKey publicKey;
	private final SSLProperties sslProps;
	private final RelayCryptographer cryptographer;
	private final long timeout;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public ActiveMQGatekeeperRelayClient(final String serverCommonName, final PublicKey publicKey, final PrivateKey privateKey, final SSLProperties sslProps, final long timeout) {
		Assert.isTrue(!Utilities.isEmpty(serverCommonName), "Common name is null or blank.");
		Assert.notNull(publicKey, "Public key is null.");
		Assert.notNull(privateKey, "Private key is null.");
		Assert.notNull(sslProps, "SSL properties object is null.");
		
		this.serverCommonName = serverCommonName;
		this.cryptographer = new RelayCryptographer(privateKey);
		this.publicKey = publicKey;
		this.sslProps = sslProps;
		this.timeout = timeout;
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
	@Override
	public MessageConsumer subscribeGeneralAdvertisementTopic(final Session session) throws JMSException {
		logger.debug("subscribeGeneralAdvertisementTopic started...");
		
		Assert.notNull(session, "session is null.");
		
		final Topic topic = session.createTopic(GENERAL_TOPIC_NAME);
		return session.createConsumer(topic);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Override
	public GeneralAdvertisementMessageDTO getGeneralAdvertisementMessage(final Message msg) throws JMSException {
		logger.debug("getGeneralAdvertisementMessage started...");
		
		Assert.notNull(msg, "message is null.");
		
		if (msg instanceof TextMessage) {
			final TextMessage tmsg = (TextMessage) msg;
			final GeneralAdvertisementMessageDTO originalDTO = Utilities.fromJson(tmsg.getText(), GeneralAdvertisementMessageDTO.class);
			if (isForMe(originalDTO.getRecipientCN())) {
				return decryptGeneralAdvertisementMessage(originalDTO);
			}
			
			return null; // message to someone else
		}
		
		throw new JMSException("Invalid message class: " + msg.getClass().getSimpleName());
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S2095")
	@Override
	public GatekeeperRelayRequest sendAcknowledgementAndReturnRequest(final Session session, final GeneralAdvertisementMessageDTO gaMsg) throws JMSException {
		logger.debug("sendAcknowledgementAndReturnRequest started...");
		
		Assert.notNull(session, "Session is null.");
		Assert.notNull(gaMsg, "Message is null.");
		Assert.isTrue(!Utilities.isEmpty(gaMsg.getSenderPublicKey()), "Public key is null or blank.");
		Assert.isTrue(!Utilities.isEmpty(gaMsg.getSessionId()), "Session id is null or blank.");
		final PublicKey peerPublicKey = Utilities.getPublicKeyFromBase64EncodedString(gaMsg.getSenderPublicKey());
		
		MessageConsumer messageConsumer = null;
		MessageProducer messageProducer = null;
		
		try {
			final Queue requestQueue = session.createQueue(REQUEST_QUEUE_PREFIX + serverCommonName + "-" + gaMsg.getSessionId());
			messageConsumer = session.createConsumer(requestQueue);
			
			final Queue responseQueue = session.createQueue(RESPONSE_QUEUE_PREFIX + serverCommonName + "-" + gaMsg.getSessionId());
			messageProducer = session.createProducer(responseQueue);
			
			final String encodedMessage = cryptographer.encodeRelayMessage(CoreCommonConstants.RELAY_MESSAGE_TYPE_ACK, gaMsg.getSessionId(), null, peerPublicKey); // no payload
			final TextMessage ackMsg = session.createTextMessage(encodedMessage);
			messageProducer.send(ackMsg);
			
			// waiting for the request
			final Message reqMsg = messageConsumer.receive(timeout);
			
			if (reqMsg == null) { // timeout
				closeThese(messageProducer, messageConsumer);
				
				return null; // no request arrived
			}
			
			if (reqMsg instanceof TextMessage) {
				final TextMessage tmsg = (TextMessage) reqMsg;
				final DecryptedMessageDTO decryptedMessageDTO = cryptographer.decodeMessage(tmsg.getText(), peerPublicKey);
				validateRequest(gaMsg.getSessionId(), decryptedMessageDTO);
				final Object payload = extractPayload(decryptedMessageDTO, true);
				final GatekeeperRelayRequest request = new GatekeeperRelayRequest(messageProducer, peerPublicKey, gaMsg.getSessionId(), decryptedMessageDTO.getMessageType(), payload);
				messageConsumer.close();
				
				return request;
			}
			
			throw new JMSException("Invalid message class: " + reqMsg.getClass().getSimpleName());
		} catch (final JMSException | ArrowheadException ex) {
			closeThese(messageProducer, messageConsumer);
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Override
	public void sendResponse(final Session session, final GatekeeperRelayRequest request, final Object responsePayload) throws JMSException {
		logger.debug("sendResponse started...");
		
		Assert.notNull(session, "session is null.");
		Assert.notNull(request, "request is null.");
		Assert.notNull(request.getAnswerSender(), "Sender is null.");
		
		try {
			Assert.notNull(request.getPeerPublicKey(), "Peer public key is null.");
			Assert.isTrue(!Utilities.isEmpty(request.getSessionId()), "Session id is null or blank.");
			Assert.isTrue(!Utilities.isEmpty(request.getMessageType()), "Message type is null or blank.");
			Assert.notNull(responsePayload, "Payload is null.");
			
			final Class<?> responseClass = getMessageDTOClass(request.getMessageType(), false);
			if (!responseClass.isInstance(responsePayload) && !ErrorMessageDTO.class.isInstance(responsePayload)) {
				throw new InvalidParameterException("The specified payload is not a valid response to the specified request.");
			}
			
			final String encryptedResponse = cryptographer.encodeRelayMessage(request.getMessageType(), request.getSessionId(), responsePayload, request.getPeerPublicKey());
			final TextMessage respMsg = session.createTextMessage(encryptedResponse);
			request.getAnswerSender().send(respMsg);
		} finally {
			request.getAnswerSender().close();
		}
	}

	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S2095")
	@Override
	public GeneralAdvertisementResult publishGeneralAdvertisement(final Session session, final String recipientCN, final String recipientPublicKey) throws JMSException {
		logger.debug("publishGeneralAdvertisement started...");
		
		Assert.notNull(session, "session is null.");
		Assert.isTrue(!Utilities.isEmpty(recipientCN), "recipientCN is null or blank.");
		Assert.isTrue(!Utilities.isEmpty(recipientPublicKey), "recipientPublicKey is null or blank.");
		final PublicKey peerPublicKey = Utilities.getPublicKeyFromBase64EncodedString(recipientPublicKey);

		final String sessionId = createSessionId();
		final String encryptedSessionId = encryptSessionId(sessionId, peerPublicKey);
		final String senderPublicKey = Base64.getEncoder().encodeToString(publicKey.getEncoded());
		final GeneralAdvertisementMessageDTO messageDTO = new GeneralAdvertisementMessageDTO(serverCommonName, senderPublicKey, recipientCN, encryptedSessionId);
		final TextMessage textMessage = session.createTextMessage(Utilities.toJson(messageDTO));

		MessageProducer producer = null;
		MessageConsumer messageConsumer = null;
		
		try {
			final Topic topic = session.createTopic(GENERAL_TOPIC_NAME);
			producer = session.createProducer(topic);
			
			final Queue responseQueue = session.createQueue(RESPONSE_QUEUE_PREFIX + recipientCN + "-" + sessionId);
			messageConsumer = session.createConsumer(responseQueue);
			
			producer.send(textMessage);
			
			// waiting for acknowledgement
			final Message ackMsg = messageConsumer.receive(timeout);
			
			if (ackMsg == null) {
				closeThese(producer, messageConsumer);
				
				return null;
			}
			
			if (ackMsg instanceof TextMessage) {
				final TextMessage tmsg = (TextMessage) ackMsg;
				final DecryptedMessageDTO decryptedMessageDTO = cryptographer.decodeMessage(tmsg.getText(), peerPublicKey);
				validateAcknowledgement(sessionId, decryptedMessageDTO);
				producer.close();
				
				return new GeneralAdvertisementResult(messageConsumer, recipientCN, peerPublicKey, sessionId);
			}
			
			throw new JMSException("Invalid message class: " + ackMsg.getClass().getSimpleName());
		} catch (final JMSException | ArrowheadException ex) {
			closeThese(producer, messageConsumer);
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	public GatekeeperRelayResponse sendRequestAndReturnResponse(final Session session, final GeneralAdvertisementResult advResponse, final Object requestPayload) throws JMSException {
		logger.debug("sendRequestAndReturnResponse started...");

		Assert.notNull(session, "session is null.");
		Assert.notNull(advResponse, "advResponse is null.");
		Assert.notNull(advResponse.getAnswerReceiver(), "Receiver is null.");
		
		MessageProducer messageProducer = null;
		try {
			Assert.isTrue(!Utilities.isEmpty(REQUEST_QUEUE_PREFIX), "Peer common name is null or blank.");
			Assert.notNull(advResponse.getPeerPublicKey(), "Peer public key is null.");
			Assert.isTrue(!Utilities.isEmpty(advResponse.getSessionId()), "Session id is null or blank.");
			Assert.notNull(requestPayload, "Payload is null.");

			final Queue requestQueue = session.createQueue(REQUEST_QUEUE_PREFIX + advResponse.getPeerCN() + "-" + advResponse.getSessionId());
			messageProducer = session.createProducer(requestQueue);
			
			final String messageType = getMessageType(requestPayload);
			final String encryptedRequest = cryptographer.encodeRelayMessage(messageType, advResponse.getSessionId(), requestPayload, advResponse.getPeerPublicKey());
			final TextMessage message = session.createTextMessage(encryptedRequest);
			messageProducer.send(message);
			
			// waiting for the response
			final Message ansMsg = advResponse.getAnswerReceiver().receive(timeout);
			
			if (ansMsg == null) { // timeout
				return null; // no response arrived in time
			}
			
			if (ansMsg instanceof TextMessage) {
				final TextMessage tmsg = (TextMessage) ansMsg;
				final DecryptedMessageDTO decryptedMessageDTO = cryptographer.decodeMessage(tmsg.getText(), advResponse.getPeerPublicKey());
				validateResponse(advResponse.getSessionId(), messageType, decryptedMessageDTO);
				
				if (isErrorResponse(decryptedMessageDTO)) {
					final ErrorMessageDTO errorMessage = extractErrorPayload(decryptedMessageDTO);
					handleError(errorMessage);
				} else {
					final Object payload = extractPayload(decryptedMessageDTO, false);
					
					return new GatekeeperRelayResponse(decryptedMessageDTO.getSessionId(), decryptedMessageDTO.getMessageType(), payload);
				}
			}

			throw new JMSException("Invalid message class: " + ansMsg.getClass().getSimpleName());
		} finally {
			closeThese(messageProducer, advResponse.getAnswerReceiver());
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
	private String createSessionId() {
		return RandomStringUtils.randomAlphanumeric(SESSION_ID_LENGTH);
	}
	
	//-------------------------------------------------------------------------------------------------
	private String encryptSessionId(final String sessionId, final PublicKey recipientPublicKey) {
		logger.debug("encryptSessionId started...");
		
		return cryptographer.encodeSessionId(sessionId, recipientPublicKey);
	}
	
	//-------------------------------------------------------------------------------------------------
	private boolean isForMe(final String recipientCN) {
		return serverCommonName.equalsIgnoreCase(recipientCN);
	}
	
	//-------------------------------------------------------------------------------------------------
	private GeneralAdvertisementMessageDTO decryptGeneralAdvertisementMessage(final GeneralAdvertisementMessageDTO originalDTO) {
		logger.debug("decryptGeneralAdvertisementMessage started...");
		
		final DecryptedMessageDTO decodedSessionId = cryptographer.decodeMessage(originalDTO.getSessionId(), originalDTO.getSenderPublicKey());
		originalDTO.setSessionId(decodedSessionId.getSessionId());
		
		return originalDTO;
	}
	
	//-------------------------------------------------------------------------------------------------
	private Object extractPayload(final DecryptedMessageDTO decryptedMessageDTO, final boolean request) {
		logger.debug("extractPayload started...");
		
		final Class<?> clazz = getMessageDTOClass(decryptedMessageDTO.getMessageType(), request);
		try {
			return mapper.readValue(decryptedMessageDTO.getPayload(), clazz);
		} catch (final IOException ex) {
			logger.error("Can't convert payload with type {}", clazz.getSimpleName());
			logger.debug(ex);
			throw new ArrowheadException("Can't convert payload with type " + clazz.getSimpleName());
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private Class<?> getMessageDTOClass(final String messageType, final boolean request) {
		logger.debug("getMessageDTOClass started...");
		
		switch (messageType) {
		case CoreCommonConstants.RELAY_MESSAGE_TYPE_GSD_POLL: 
			return request ? GSDPollRequestDTO.class : GSDPollResponseDTO.class;
		case CoreCommonConstants.RELAY_MESSAGE_TYPE_ICN_PROPOSAL:
			return request ? ICNProposalRequestDTO.class : ICNProposalResponseDTO.class;
		case CoreCommonConstants.RELAY_MESSAGE_TYPE_ACCESS_TYPE:
			return request ? GeneralRelayRequestDTO.class : AccessTypeRelayResponseDTO.class;
		case CoreCommonConstants.RELAY_MESSAGE_TYPE_SYSTEM_ADDRESS_LIST:
			return request ? GeneralRelayRequestDTO.class : SystemAddressSetRelayResponseDTO.class;
		case CoreCommonConstants.RELAY_MESSAGE_TYPE_QOS_RELAY_TEST:
			return request ? QoSRelayTestProposalRequestDTO.class : QoSRelayTestProposalResponseDTO.class;
		default:
			throw new ArrowheadException("Invalid message type: " + messageType);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private String getMessageType(final Object requestPayload) {
		logger.debug("getMessageType started...");
		
		if (requestPayload instanceof GSDPollRequestDTO) {
			return CoreCommonConstants.RELAY_MESSAGE_TYPE_GSD_POLL;
		}
		
		if (requestPayload instanceof ICNProposalRequestDTO) {
			return CoreCommonConstants.RELAY_MESSAGE_TYPE_ICN_PROPOSAL;
		}
		
		if (requestPayload instanceof GeneralRelayRequestDTO) {
			return ((GeneralRelayRequestDTO)requestPayload).getMessageType();
		}
		
		if (requestPayload instanceof QoSRelayTestProposalRequestDTO) {
			return CoreCommonConstants.RELAY_MESSAGE_TYPE_QOS_RELAY_TEST;
		}
		
		throw new ArrowheadException("Invalid message DTO: " + requestPayload.getClass().getSimpleName());
	}
	
	//-------------------------------------------------------------------------------------------------
	private void validateSessionId(final String expectedSessionId, final String actualSessionId) {
		logger.debug("validateSessionId started...");
		
		if (!Objects.equals(expectedSessionId, actualSessionId)) {
			throw new AuthException("Unauthorized message on queue.");
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private void validateRequest(final String sessionId, final DecryptedMessageDTO msg) {
		logger.debug("validateRequest started...");
		
		if (!supportedRequestTypes.contains(msg.getMessageType().toLowerCase())) {
			throw new AuthException("Unauthorized message on queue.");
		}
		
		validateSessionId(sessionId, msg.getSessionId());
	}
	
	//-------------------------------------------------------------------------------------------------
	private void validateAcknowledgement(final String sessionId, final DecryptedMessageDTO msg) {
		logger.debug("validateAcknowledgement started...");

		validateResponse(sessionId, CoreCommonConstants.RELAY_MESSAGE_TYPE_ACK, msg);
	}
	
	//-------------------------------------------------------------------------------------------------
	private void validateResponse(final String sessionId, final String messageType, final DecryptedMessageDTO msg) {
		logger.debug("validateResponse started...");

		if (!messageType.equals(msg.getMessageType())) {
			throw new AuthException("Unauthorized message on queue.");
		}
		
		validateSessionId(sessionId, msg.getSessionId());
	}
	
	//-------------------------------------------------------------------------------------------------
	private boolean isErrorResponse(final DecryptedMessageDTO msg) {
		return msg.getPayload().contains(ERROR_CODE);
	}
	
	//-------------------------------------------------------------------------------------------------
	private ErrorMessageDTO extractErrorPayload(final DecryptedMessageDTO decryptedMessageDTO) {
		logger.debug("extractErrorPayload started...");
		
		try {
			return mapper.readValue(decryptedMessageDTO.getPayload(), ErrorMessageDTO.class);
		} catch (final IOException ex) {
			logger.error("Can't convert payload with type {}", ErrorMessageDTO.class.getSimpleName());
			logger.debug(ex);
			throw new ArrowheadException("Can't convert payload with type " + ErrorMessageDTO.class.getSimpleName());
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private void handleError(final ErrorMessageDTO dto) {
		if (dto.getExceptionType() == null) {
			logger.error("Request failed, error message: {}", dto.getErrorMessage());
		    throw new ArrowheadException("Unknown error occurred at " + ActiveMQGatekeeperRelayClient.class.getSimpleName() + ". Check log for possibly more information.");
		}
		
		logger.error("Request returned with {}: {}", dto.getExceptionType(), dto.getErrorMessage());
		Utilities.createExceptionFromErrorMessageDTO(dto);
	}
	
	//-------------------------------------------------------------------------------------------------
	private void closeThese(final AutoCloseable... closeables) {
		logger.debug("closeThese started...");
		
		for (final AutoCloseable closeable : closeables) {
			try {
				if (closeable != null) {
					closeable.close();
				}
			} catch (final Exception ex) {
				logger.debug(ex.getMessage());
				logger.trace(ex);
			}
		}
	}
}