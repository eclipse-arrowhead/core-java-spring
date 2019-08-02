package eu.arrowhead.core.gatekeeper.relay.activemq;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.Assert;
import org.springframework.web.util.UriComponents;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.DecryptedMessageDTO;
import eu.arrowhead.common.dto.GeneralAdvertisementMessageDTO;
import eu.arrowhead.common.relay.RelayCryptographer;
import eu.arrowhead.core.gatekeeper.relay.GatekeeperRelayClient;

public class ActiveMQGatekeeperRelayClient implements GatekeeperRelayClient {
	
	//=================================================================================================
	// members
	
	private static final String TCP = "tcp";
	private static final String GENERATED_TOPIC_SUFFIX = "M5QTZXM9G9AnpPHWT6WennWu";
	private static final String GENERAL_TOPIC_NAME = "General-" + GENERATED_TOPIC_SUFFIX;
	private static final int CLIENT_ID_LENGTH = 16;
	private static final int SESSION_ID_LENGTH = 48;

	private static final Logger logger = LogManager.getLogger(ActiveMQGatekeeperRelayClient.class);

	private final String serverCommonName;
	private final PublicKey publicKey;
	private final RelayCryptographer cryptographer;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public ActiveMQGatekeeperRelayClient(final String serverCommonName, final PublicKey publicKey, final PrivateKey privateKey) {
		Assert.isTrue(!Utilities.isEmpty(serverCommonName), "Common name is null or blank.");
		Assert.notNull(publicKey, "Public key is null.");
		Assert.notNull(privateKey, "Private key is null.");
		
		this.serverCommonName = serverCommonName;
		this.cryptographer = new RelayCryptographer(privateKey);
		this.publicKey = publicKey;
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
		
		throw new JMSException("Invalid message type: " + msg.getClass().getSimpleName());
	}

	//-------------------------------------------------------------------------------------------------
	@Override
	public void publishGeneralAdvertisement(final Session session, final String recipientCN, final String recipientPublicKey, final String senderCN) throws JMSException {
		logger.debug("publishGeneralAdvertisement started...");
		
		Assert.notNull(session, "session is null.");
		Assert.isTrue(!Utilities.isEmpty(senderCN), "senderCN is null or blank.");
		Assert.isTrue(!Utilities.isEmpty(recipientCN), "recipientCN is null or blank.");
		Assert.isTrue(!Utilities.isEmpty(recipientPublicKey), "recipientPublicKey is null or blank.");

		final String sessionId = createSessionId(recipientPublicKey);
		final String senderPublicKey = Base64.getEncoder().encodeToString(publicKey.getEncoded());
		final GeneralAdvertisementMessageDTO messageDTO = new GeneralAdvertisementMessageDTO(senderCN, senderPublicKey, recipientCN, sessionId);
		final TextMessage textMessage = session.createTextMessage(Utilities.toJson(messageDTO));

		final Topic topic = session.createTopic(GENERAL_TOPIC_NAME);
		final MessageProducer producer = session.createProducer(topic);

		producer.send(textMessage);
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private String createSessionId(final String recipientPublicKey) {
		logger.debug("createSessionId started...");
		
		final String sessionId = RandomStringUtils.randomAlphanumeric(SESSION_ID_LENGTH);
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
}