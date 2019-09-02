package eu.arrowhead.core.gateway.relay;

import java.security.PublicKey;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;

import eu.arrowhead.common.relay.RelayClient;

public interface GatewayRelayClient extends RelayClient {
	
	//=================================================================================================
	// members
	
	public static final String REQUEST_QUEUE_PREFIX = "REQ-";
	public static final String RESPONSE_QUEUE_PREFIX = "RESP-";
	public static final String CONTROL_QUEUE_SUFFIX = "-CONTROL";

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public ProviderSideRelayInfo initializeProviderSideRelay(final Session session, final MessageListener listener) throws JMSException;
	public ConsumerSideRelayInfo initializeConsumerSideRelay(final Session session, final MessageListener listener, final String peerName, final String queueId) throws JMSException;
	public ControlRelayInfo initializeControlRelay(final Session session, final String peerName, final String queueId) throws JMSException;
	
	public void sendBytes(final Session session, final MessageProducer sender, final PublicKey peerPublicKey, final byte[] bytes) throws JMSException;
	public byte[] getBytesFromMessage(final Message msg, final PublicKey peerPublicKey) throws JMSException;
	
	public void sendCloseControlMessage(final Session session, final MessageProducer sender, final String queueId) throws JMSException;
	public void handleCloseControlMessage(final Message msg, final Session session) throws JMSException;
}