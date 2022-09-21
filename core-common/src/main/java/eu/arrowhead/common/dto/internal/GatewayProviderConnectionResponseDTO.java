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

package eu.arrowhead.common.dto.internal;

import java.io.Serializable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class GatewayProviderConnectionResponseDTO implements Serializable {

	//=================================================================================================
	// members
	
	private static final long serialVersionUID = -2685072058660027564L;
	
	private String queueId;
	private String peerName;
	private String providerGWPublicKey;
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public GatewayProviderConnectionResponseDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public GatewayProviderConnectionResponseDTO(final String queueId, final String peerName, final String providerGWPublicKey) {
		this.queueId = queueId;
		this.peerName = peerName;
		this.providerGWPublicKey = providerGWPublicKey;
	}

	//-------------------------------------------------------------------------------------------------
	public String getQueueId() { return queueId; }
	public String getPeerName() { return peerName; }
	public String getProviderGWPublicKey() { return providerGWPublicKey; }

	//-------------------------------------------------------------------------------------------------
	public void setQueueId(final String queueId) { this.queueId = queueId; }
	public void setPeerName(final String peerName) { this.peerName = peerName; }
	public void setProviderGWPublicKey(final String providerGWPublicKey) { this.providerGWPublicKey = providerGWPublicKey; }
	
	//-------------------------------------------------------------------------------------------------
	@Override
	public String toString() {
		try {
			return new ObjectMapper().writeValueAsString(this);
		} catch (final JsonProcessingException ex) {
			return "toString failure";
		}
	}
}