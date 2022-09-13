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

package eu.arrowhead.relay.gateway;

import java.security.PublicKey;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;

import eu.arrowhead.relay.RelayClient;

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
	
	//-------------------------------------------------------------------------------------------------
	public void sendBytes(final Session session, final MessageProducer sender, final PublicKey peerPublicKey, final byte[] bytes) throws JMSException;
	public byte[] getBytesFromMessage(final Message msg, final PublicKey peerPublicKey) throws JMSException;
	
	//-------------------------------------------------------------------------------------------------
	public void sendCloseControlMessage(final Session session, final MessageProducer sender, final String queueId) throws JMSException;
	public void sendSwitchControlMessage(final Session session, final MessageProducer sender, final String queueId) throws JMSException;
	public void handleCloseControlMessage(final Message msg, final Session session) throws JMSException;
	public void validateSwitchControlMessage(final Message msg) throws JMSException;
	public void unsubscribeFromQueues(final MessageConsumer consumer, final MessageConsumer consumerControl) throws JMSException;
	public boolean destroyQueues(final Session session, final MessageProducer producer, final MessageProducer producerControl) throws JMSException;
}