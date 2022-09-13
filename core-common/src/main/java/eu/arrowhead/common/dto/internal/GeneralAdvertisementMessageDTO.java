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

import org.springframework.util.Assert;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.arrowhead.common.Utilities;

public class GeneralAdvertisementMessageDTO implements Serializable {
	
	//=================================================================================================
	// members
	
	private static final long serialVersionUID = 2917521789843318807L;
	
	private String senderCN;
	private String senderPublicKey;
	private String recipientCN;
	private String sessionId;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public GeneralAdvertisementMessageDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public GeneralAdvertisementMessageDTO(final String senderCN, final String senderPublicKey, final String recipientCN, final String sessionId) {
		Assert.isTrue(!Utilities.isEmpty(senderCN), "senderCN is null or blank.");
		Assert.isTrue(!Utilities.isEmpty(recipientCN), "recipientCN is null or blank.");
		Assert.isTrue(!Utilities.isEmpty(sessionId), "sessionId is null or blank.");
		
		this.senderCN = senderCN;
		this.senderPublicKey = senderPublicKey;
		this.recipientCN = recipientCN;
		this.sessionId = sessionId;
	}

	//-------------------------------------------------------------------------------------------------
	public String getSenderCN() { return senderCN; }
	public String getSenderPublicKey() { return senderPublicKey; }
	public String getRecipientCN() { return recipientCN; }
	public String getSessionId() { return sessionId; }
	
	//-------------------------------------------------------------------------------------------------
	public void setSenderCN(final String senderCN) { this.senderCN = senderCN; }
	public void setSenderPublicKey(final String senderPublicKey) { this.senderPublicKey = senderPublicKey; }
	public void setRecipientCN(final String recipientCN) { this.recipientCN = recipientCN; }
	public void setSessionId(final String sessionId) { this.sessionId = sessionId; }
	
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