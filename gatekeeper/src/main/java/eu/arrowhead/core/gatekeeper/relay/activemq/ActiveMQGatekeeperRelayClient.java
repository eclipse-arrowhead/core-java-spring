package eu.arrowhead.core.gatekeeper.relay.activemq;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

import javax.jms.Connection;
import javax.jms.JMSException;
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
import eu.arrowhead.common.dto.GeneralAdvertisementMessageDTO;
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
	
	private PublicKey publicKey;
	private PrivateKey privateKey;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public ActiveMQGatekeeperRelayClient(final PublicKey publicKey, final PrivateKey privateKey) {
		if (publicKey != null) {
			Assert.notNull(privateKey, "Need both public and private keys.");
		}
		if (privateKey != null) {
			Assert.notNull(publicKey, "Need both public and private keys.");
		}
		
		this.publicKey = publicKey;
		this.privateKey = privateKey;
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
	public void publishGeneralAdvertisement(final Session session, final String recipientCN, final String recipientPublicKey, final String senderCN) throws JMSException {
		logger.debug("publishGeneralAdvertisement started...");
		
		Assert.notNull(session, "session is null.");
		Assert.isTrue(!Utilities.isEmpty(senderCN), "senderCN is null or blank.");
		Assert.isTrue(!Utilities.isEmpty(recipientCN), "recipientCN is null or blank.");
		if (publicKey != null) {
			Assert.isTrue(!Utilities.isEmpty(recipientPublicKey), "recipientPublicKey is null or blank.");
		}

		final String sessionId = createSessionId(recipientPublicKey);
		final String senderPublicKey = publicKey != null ?  Base64.getEncoder().encodeToString(publicKey.getEncoded()) : null;
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
		String sessionId = RandomStringUtils.randomAlphanumeric(SESSION_ID_LENGTH);
		
		if (publicKey != null) {
			//TODO: encoding session id using JOSE
		}
		
		return sessionId;
	}
}