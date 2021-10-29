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

package eu.arrowhead.relay.gatekeeper;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;

import eu.arrowhead.common.dto.internal.GeneralAdvertisementMessageDTO;
import eu.arrowhead.relay.RelayClient;

public interface GatekeeperRelayClient extends RelayClient {

	//=================================================================================================
	// methods
	
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
	
	//-------------------------------------------------------------------------------------------------
	public void destroyStaleQueuesAndConnections();
}