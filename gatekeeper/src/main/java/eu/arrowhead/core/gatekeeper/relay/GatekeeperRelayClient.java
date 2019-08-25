package eu.arrowhead.core.gatekeeper.relay;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;

import eu.arrowhead.common.dto.GeneralAdvertisementMessageDTO;

public interface GatekeeperRelayClient {

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public Session createConnection(final String host, final int port) throws JMSException;
	public void closeConnection(final Session session);
	public boolean isConnectionClosed(final Session session);
	
	//-------------------------------------------------------------------------------------------------
	public MessageConsumer subscribeGeneralAdvertisementTopic(final Session session) throws JMSException;
	
	//-------------------------------------------------------------------------------------------------
	// returns null if message is for someone else
	public GeneralAdvertisementMessageDTO getGeneralAdvertisementMessage(final Message msg) throws JMSException; 
	
	//-------------------------------------------------------------------------------------------------
	// returns null if no request arrived in time
	public GatekeeperRelayRequest sendAcknowledgementAndReturnRequest(final Session session, final GeneralAdvertisementMessageDTO gaMsg) throws JMSException; 
	
	//-------------------------------------------------------------------------------------------------
	public void sendResponse(final Session session, final GatekeeperRelayRequest request, final Object responsePayload) throws JMSException;
	
	//-------------------------------------------------------------------------------------------------
	// returns null if no acknowledgement arrived in time
	public GeneralAdvertisementResult publishGeneralAdvertisement(final Session session, final String recipientCN, final String recipientPublicKey) throws JMSException;
	
	//-------------------------------------------------------------------------------------------------
	// returns null if no response arrived in time
	public GatekeeperRelayResponse sendRequestAndReturnResponse(final Session session, final GeneralAdvertisementResult advResponse, final Object requestPayload) throws JMSException;
}