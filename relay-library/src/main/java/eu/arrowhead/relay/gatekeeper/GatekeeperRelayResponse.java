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

import org.springframework.util.Assert;

import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.internal.AccessTypeRelayResponseDTO;
import eu.arrowhead.common.dto.internal.GSDMultiPollResponseDTO;
import eu.arrowhead.common.dto.internal.GSDPollResponseDTO;
import eu.arrowhead.common.dto.internal.ICNProposalResponseDTO;
import eu.arrowhead.common.dto.internal.QoSRelayTestProposalResponseDTO;
import eu.arrowhead.common.dto.internal.SystemAddressSetRelayResponseDTO;
import eu.arrowhead.common.exception.DataNotFoundException;

public class GatekeeperRelayResponse {
	
	//=================================================================================================
	// members
	
	private final String sessionId;
	private final String messageType;
	
	private final Object payload;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public GatekeeperRelayResponse(final String sessionId, final String messageType, final Object payload) {
		Assert.isTrue(!Utilities.isEmpty(sessionId), "Session id is null or blank.");
		Assert.isTrue(!Utilities.isEmpty(messageType), "Message type is null or blank.");
		Assert.notNull(payload, "Payload is null.");
		
		this.sessionId = sessionId;
		this.messageType = messageType;
		this.payload = payload;
	}

	//-------------------------------------------------------------------------------------------------
	public String getSessionId() { return sessionId; }
	public String getMessageType() { return messageType; }
	
	//-------------------------------------------------------------------------------------------------
	public GSDPollResponseDTO getGSDPollResponse() {
		if (payload instanceof GSDPollResponseDTO) {
			return (GSDPollResponseDTO) payload;
		}
		
		throw new DataNotFoundException("The response is not a result of a GSD poll.");
	}
	
	//-------------------------------------------------------------------------------------------------
	public GSDMultiPollResponseDTO getGSDMultiPollResponse() {
		if (payload instanceof GSDMultiPollResponseDTO) {
			return (GSDMultiPollResponseDTO) payload;
		}
		
		throw new DataNotFoundException("The response is not a result of a multi GSD poll.");
	}
	
	//-------------------------------------------------------------------------------------------------
	public ICNProposalResponseDTO getICNProposalResponse() {
		if (payload instanceof ICNProposalResponseDTO) {
			return (ICNProposalResponseDTO) payload;
		}
		
		throw new DataNotFoundException("The response is not a result of an ICN proposal.");
	}
	
	//-------------------------------------------------------------------------------------------------
	public AccessTypeRelayResponseDTO getAccessTypeResponse() {
		if (payload instanceof AccessTypeRelayResponseDTO) {
			return (AccessTypeRelayResponseDTO) payload;
		}
		
		throw new DataNotFoundException("The response is not a result of an access type request.");
	}
	
	//-------------------------------------------------------------------------------------------------
	public SystemAddressSetRelayResponseDTO getSystemAddressSetResponse() {
		if (payload instanceof SystemAddressSetRelayResponseDTO) {
			return (SystemAddressSetRelayResponseDTO) payload;
		}
		
		throw new DataNotFoundException("The response is not a result of a system addresses request.");
	}
	
	//-------------------------------------------------------------------------------------------------
	public QoSRelayTestProposalResponseDTO getQoSRelayTestProposalResponse() {
		if (payload instanceof QoSRelayTestProposalResponseDTO) {
			return (QoSRelayTestProposalResponseDTO) payload;
		}
		
		throw new DataNotFoundException("The response is not a result of a relay test request.");
	}
}