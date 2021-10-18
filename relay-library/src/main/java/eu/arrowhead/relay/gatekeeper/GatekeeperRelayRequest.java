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

import java.security.PublicKey;

import javax.jms.MessageProducer;

import org.springframework.util.Assert;

import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.internal.GSDMultiPollRequestDTO;
import eu.arrowhead.common.dto.internal.GSDPollRequestDTO;
import eu.arrowhead.common.dto.internal.GeneralRelayRequestDTO;
import eu.arrowhead.common.dto.internal.ICNProposalRequestDTO;
import eu.arrowhead.common.dto.internal.QoSRelayTestProposalRequestDTO;
import eu.arrowhead.common.exception.DataNotFoundException;

public class GatekeeperRelayRequest {

	//=================================================================================================
	// members
	
	// data needed to answer 
	private final MessageProducer answerSender;
	private final PublicKey peerPublicKey;
	private final String sessionId;
	private final String messageType;
	
	private final Object payload;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public GatekeeperRelayRequest(final MessageProducer answerSender, final PublicKey peerPublicKey, final String sessionId, final String messageType, final Object payload) {
		Assert.notNull(answerSender, "Sender is null.");
		Assert.notNull(peerPublicKey, "Peer public key is null.");
		Assert.isTrue(!Utilities.isEmpty(sessionId), "Session id is null or blank.");
		Assert.isTrue(!Utilities.isEmpty(messageType), "Message type is null or blank.");
		Assert.notNull(payload, "Payload is null.");
		
		this.answerSender = answerSender;
		this.peerPublicKey = peerPublicKey;
		this.sessionId = sessionId;
		this.messageType = messageType;
		this.payload = payload;
	}

	//-------------------------------------------------------------------------------------------------
	public MessageProducer getAnswerSender() { return answerSender; }
	public PublicKey getPeerPublicKey() { return peerPublicKey; }
	public String getSessionId() { return sessionId; }
	public String getMessageType() { return messageType; }

	//-------------------------------------------------------------------------------------------------
	public GSDPollRequestDTO getGSDPollRequest() {
		if (payload instanceof GSDPollRequestDTO) {
			return (GSDPollRequestDTO) payload;
		}
		
		throw new DataNotFoundException("The request is not a GSD poll.");
	}
	
	//-------------------------------------------------------------------------------------------------
	public GSDMultiPollRequestDTO getGSDMultiPollRequest() {
		if (payload instanceof GSDMultiPollRequestDTO) {
			return (GSDMultiPollRequestDTO) payload;
		}
		
		throw new DataNotFoundException("The request is not a multi GSD poll.");
	}
	
	//-------------------------------------------------------------------------------------------------
	public ICNProposalRequestDTO getICNProposalRequest() {
		if (payload instanceof ICNProposalRequestDTO) {
			return (ICNProposalRequestDTO) payload;
		}
		
		throw new DataNotFoundException("The request is not an ICN proposal.");
	}
	
	//-------------------------------------------------------------------------------------------------
	public GeneralRelayRequestDTO getGeneralRelayRequest() {
		if (payload instanceof GeneralRelayRequestDTO) {
			return (GeneralRelayRequestDTO) payload;
		}
		
		throw new DataNotFoundException("The request is not a general request.");
	}
	
	//-------------------------------------------------------------------------------------------------
	public QoSRelayTestProposalRequestDTO getQoSRelayTestProposalRequest() {
		if (payload instanceof QoSRelayTestProposalRequestDTO) {
			return (QoSRelayTestProposalRequestDTO) payload;
		}
		
		throw new DataNotFoundException("The request is not a relay test proposal.");
	}
 }