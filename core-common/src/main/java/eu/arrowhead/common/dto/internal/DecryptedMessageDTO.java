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

public class DecryptedMessageDTO implements Serializable {
	
	//=================================================================================================
	// members
	
	private static final long serialVersionUID = -6568005268815512990L;
	
	private String messageType;
	private String sessionId;
	private String payload;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public String getMessageType() { return messageType; }
	public String getSessionId() { return sessionId; }
	public String getPayload() { return payload; }
	
	//-------------------------------------------------------------------------------------------------
	public void setMessageType(final String messageType) { this.messageType = messageType; }
	public void setSessionId(final String sessionId) { this.sessionId = sessionId; }
	public void setPayload(final String payload) { this.payload = payload; }
	
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