package eu.arrowhead.core.gatekeeper.relay.activemq;

import java.io.IOException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
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
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.Assert;
import org.springframework.web.util.UriComponents;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.DecryptedMessageDTO;
import eu.arrowhead.common.dto.GSDPollRequestDTO;
import eu.arrowhead.common.dto.GSDPollResponseDTO;
import eu.arrowhead.common.dto.GeneralAdvertisementMessageDTO;
import eu.arrowhead.common.dto.ICNProposalRequestDTO;
import eu.arrowhead.common.dto.ICNProposalResponseDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.AuthException;
import eu.arrowhead.common.relay.RelayCryptographer;
import eu.arrowhead.core.gatekeeper.relay.GatekeeperRelayClient;
import eu.arrowhead.core.gatekeeper.relay.GatekeeperRelayRequest;
import eu.arrowhead.core.gatekeeper.relay.GatekeeperRelayResponse;
import eu.arrowhead.core.gatekeeper.relay.GeneralAdvertisementResult;

public class ActiveMQGatekeeperRelayClient implements GatekeeperRelayClient {
	
	//=================================================================================================
	// members
	
	private static final String TCP = "tcp";
	private static final String GENERATED_TOPIC_SUFFIX = "M5QTZXM9G9AnpPHWT6WennWu";
	private static final String GENERAL_TOPIC_NAME = "General-" + GENERATED_TOPIC_SUFFIX;
	private static final String REQUEST_QUEUE_PREFIX = "REQ-";
	private static final String RESPONSE_QUEUE_PREFIX = "RESP-";
	
	private static final int CLIENT_ID_LENGTH = 16;
	private static final int SESSION_ID_LENGTH = 48;

	private static final Logger logger = LogManager.getLogger(ActiveMQGatekeeperRelayClient.class);
	private static final ObjectMapper mapper = new ObjectMapper();
	
	private final String serverCommonName;
	private final PublicKey publicKey;
	private final RelayCryptographer cryptographer;
	private final long timeout;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public ActiveMQGatekeeperRelayClient(final String serverCommonName, final PublicKey publicKey, final PrivateKey privateKey, final long timeout) {
		Assert.isTrue(!Utilities.isEmpty(serverCommonName), "Common name is null or blank.");
		Assert.notNull(publicKey, "Public key is null.");
		Assert.notNull(privateKey, "Private key is null.");
		
		this.serverCommonName = serverCommonName;
		this.cryptographer = new RelayCryptographer(privateKey);
		this.publicKey = publicKey;
		this.timeout = timeout;
	}

	//-------------------------------------------------------------------------------------------------
	@Override
	public Session createConnection(final String host, final int port) throws JMSException {
		logger.debug("createConnection started...");
		
		Assert.isTrue(!Utilities.isEmpty(host), "Host is null or blank.");
		Assert.isTrue(port > CommonConstants.SYSTEM_PORT_RANGE_MIN && port < CommonConstants.SYSTEM_PORT_RANGE_MAX, "Port is invalid.");
		
		final UriComponents uri = Utilities.createURI(TCP, host, port, null);
		final ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(uri.toUri());
		connectionFactory.setClientID(RandomStringUtils.randomAlphanumeric(CLIENT_ID_LENGTH));
		final Connection connection = connectionFactory.createConnection();
		connectionFactory.setClientID(null);
		connection.start();
		
		return connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
	}

	//-------------------------------------------------------------------------------------------------
	@Override
	public void closeConnection(final Session session) {
		logger.debug("closeConnection started...");
		if (session != null) {
			try {
				session.close();
			} catch (final JMSException ex) {
				logger.debug(ex.getMessage());
				logger.trace(ex);
			}
		}
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
	@Override
	public GatekeeperRelayRequest sendAcknowledgementAndReturnRequest(final Session session, final GeneralAdvertisementMessageDTO gaMsg) throws JMSException {
		logger.debug("sendAcknowledgementAndReturnRequest started...");
		
		Assert.notNull(session, "Session is null.");
		Assert.notNull(gaMsg, "Message is null.");
		Assert.isTrue(!Utilities.isEmpty(gaMsg.getSenderPublicKey()), "Public key is null or blank.");
		Assert.isTrue(!Utilities.isEmpty(gaMsg.getSessionId()), "Session id is null or blank.");
		final PublicKey peerPublicKey = Utilities.getPublicKeyFromBase64EncodedString(gaMsg.getSenderPublicKey());
		
		final Queue requestQueue = session.createQueue(REQUEST_QUEUE_PREFIX + serverCommonName + "-" + gaMsg.getSessionId());
		final MessageConsumer messageConsumer = session.createConsumer(requestQueue);
		
		final Queue responseQueue = session.createQueue(RESPONSE_QUEUE_PREFIX + serverCommonName + "-" + gaMsg.getSessionId());
		final MessageProducer messageProducer = session.createProducer(responseQueue);
		
		final String encodedMessage = cryptographer.encodeRelayMessage(CommonConstants.RELAY_MESSAGE_TYPE_ACK, gaMsg.getSessionId(), null, peerPublicKey); // no payload
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
			validateSessionId(gaMsg.getSessionId(), decryptedMessageDTO.getSessionId());
			final Object payload = extractPayload(decryptedMessageDTO, true);
			final GatekeeperRelayRequest request = new GatekeeperRelayRequest(messageProducer, peerPublicKey, gaMsg.getSessionId(), decryptedMessageDTO.getMessageType(), payload);
			messageConsumer.close();
			
			return request;
		}

		closeThese(messageProducer, messageConsumer);
		
		throw new JMSException("Invalid message class: " + reqMsg.getClass().getSimpleName());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Override
	public void sendResponse(final Session session, final GatekeeperRelayRequest request, final Object responsePayload) throws JMSException {
		logger.debug("sendResponse started...");
		
		Assert.notNull(session, "session is null.");
		Assert.notNull(request, "request is null.");
		Assert.notNull(request.getAnswerSender(), "Sender is null.");
		Assert.notNull(request.getPeerPublicKey(), "Peer public key is null.");
		Assert.isTrue(!Utilities.isEmpty(request.getSessionId()), "Session id is null or blank.");
		Assert.isTrue(!Utilities.isEmpty(request.getMessageType()), "Message type is null or blank.");
		Assert.notNull(responsePayload, "Payload is null.");
		
		final String encryptedResponse = cryptographer.encodeRelayMessage(request.getMessageType(), request.getSessionId(), responsePayload, request.getPeerPublicKey());
		final TextMessage respMsg = session.createTextMessage(encryptedResponse);
		request.getAnswerSender().send(respMsg);
		request.getAnswerSender().close();
	}

	//-------------------------------------------------------------------------------------------------
	@Override
	public GeneralAdvertisementResult publishGeneralAdvertisement(final Session session, final String recipientCN, final String recipientPublicKey, final String senderCN) throws JMSException {
		logger.debug("publishGeneralAdvertisement started...");
		
		Assert.notNull(session, "session is null.");
		Assert.isTrue(!Utilities.isEmpty(senderCN), "senderCN is null or blank.");
		Assert.isTrue(!Utilities.isEmpty(recipientCN), "recipientCN is null or blank.");
		Assert.isTrue(!Utilities.isEmpty(recipientPublicKey), "recipientPublicKey is null or blank.");
		final PublicKey peerPublicKey = Utilities.getPublicKeyFromBase64EncodedString(recipientPublicKey);

		final String sessionId = createSessionId();
		final String encryptedSessionId = encryptSessionId(sessionId, peerPublicKey);
		final String senderPublicKey = Base64.getEncoder().encodeToString(publicKey.getEncoded());
		final GeneralAdvertisementMessageDTO messageDTO = new GeneralAdvertisementMessageDTO(senderCN, senderPublicKey, recipientCN, encryptedSessionId);
		final TextMessage textMessage = session.createTextMessage(Utilities.toJson(messageDTO));

		final Topic topic = session.createTopic(GENERAL_TOPIC_NAME);
		final MessageProducer producer = session.createProducer(topic);
		
		final Queue responseQueue = session.createQueue(RESPONSE_QUEUE_PREFIX + recipientCN + "-" + sessionId);
		final MessageConsumer messageConsumer = session.createConsumer(responseQueue);

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

		closeThese(producer, messageConsumer);
		
		throw new JMSException("Invalid message class: " + ackMsg.getClass().getSimpleName());

	}
	
	//-------------------------------------------------------------------------------------------------
	public GatekeeperRelayResponse sendRequestAndReturnResponse(final Session session, final GeneralAdvertisementResult advResponse, final Object requestPayload) throws JMSException {
		logger.debug("sendRequestAndReturnResponse started...");

		Assert.notNull(session, "session is null.");
		Assert.notNull(advResponse, "advResponse is null.");
		Assert.notNull(advResponse.getAnswerReceiver(), "Receiver is null.");
		Assert.notNull(advResponse.getPeerPublicKey(), "Peer public key is null.");
		Assert.isTrue(!Utilities.isEmpty(advResponse.getSessionId()), "Session id is null or blank.");
		Assert.notNull(requestPayload, "Payload is null.");

		final Queue requestQueue = session.createQueue(REQUEST_QUEUE_PREFIX + advResponse.getPeerCN() + "-" + advResponse.getSessionId());
		final MessageProducer messageProducer = session.createProducer(requestQueue);
		
		final String messageType = getMessageType(requestPayload);
		final String encryptedRequest = cryptographer.encodeRelayMessage(messageType, advResponse.getSessionId(), requestPayload, advResponse.getPeerPublicKey());
		final TextMessage message = session.createTextMessage(encryptedRequest);
		messageProducer.send(message);
		
		// waiting for the response
		final Message ansMsg = advResponse.getAnswerReceiver().receive(timeout);
		
		if (ansMsg == null) { // timeout
			closeThese(messageProducer, advResponse.getAnswerReceiver());
			
			return null; // no response arrived in time
		}
		
		if (ansMsg instanceof TextMessage) {
			final TextMessage tmsg = (TextMessage) ansMsg;
			final DecryptedMessageDTO decryptedMessageDTO = cryptographer.decodeMessage(tmsg.getText(), advResponse.getPeerPublicKey());
			validateResponse(advResponse.getSessionId(), messageType, decryptedMessageDTO);
			final Object payload = extractPayload(decryptedMessageDTO, false);
			final GatekeeperRelayResponse response = new GatekeeperRelayResponse(decryptedMessageDTO.getSessionId(), decryptedMessageDTO.getMessageType(), payload);
			
			closeThese(messageProducer, advResponse.getAnswerReceiver());
			
			return response;
		}

		closeThese(messageProducer, advResponse.getAnswerReceiver());
		
		throw new JMSException("Invalid message class: " + ansMsg.getClass().getSimpleName());
	}
	
	//=================================================================================================
	// assistant methods
	
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
		case CommonConstants.RELAY_MESSAGE_TYPE_GSD_POLL: 
			return request ? GSDPollRequestDTO.class : GSDPollResponseDTO.class;
		case CommonConstants.RELAY_MESSAGE_TYPE_ICN_PROPOSAL:
			return request ? ICNProposalRequestDTO.class : ICNProposalResponseDTO.class;
		default:
			throw new ArrowheadException("Invalid message type: " + messageType);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private String getMessageType(final Object requestPayload) {
		logger.debug("getMessageType started...");
		
		if (requestPayload instanceof GSDPollRequestDTO) {
			return CommonConstants.RELAY_MESSAGE_TYPE_GSD_POLL;
		}
		
		if (requestPayload instanceof ICNProposalRequestDTO) {
			return CommonConstants.RELAY_MESSAGE_TYPE_ICN_PROPOSAL;
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
	private void validateAcknowledgement(final String sessionId, final DecryptedMessageDTO msg) {
		logger.debug("validateAcknowledgement started...");

		validateResponse(sessionId, CommonConstants.RELAY_MESSAGE_TYPE_ACK, msg);
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
	private void closeThese(final AutoCloseable... closeables) {
		logger.debug("closeThese started...");
		
		for (final AutoCloseable closeable : closeables) {
			try {
				closeable.close();
			} catch (final Exception ex) {
				logger.debug(ex.getMessage());
				logger.trace(ex);
			}
		}
	}
}