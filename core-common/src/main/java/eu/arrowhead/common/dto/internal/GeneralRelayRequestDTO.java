/********************************************************************************
 * Copyright (c) 2020 AITIA
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
import java.util.List;

import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.arrowhead.common.CoreCommonConstants;

/**
 * DTO for relay communication when no input parameter is needed
 */
public class GeneralRelayRequestDTO implements Serializable {
	
	//=================================================================================================
	// members
	
	private static final long serialVersionUID = -1975553915960218770L;
	
	@JsonIgnore
	private static final transient List<String> supportedMessageTypes = List.of(CoreCommonConstants.RELAY_MESSAGE_TYPE_ACCESS_TYPE, 
																				CoreCommonConstants.RELAY_MESSAGE_TYPE_SYSTEM_ADDRESS_LIST);
	
	private String messageType;
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public GeneralRelayRequestDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public GeneralRelayRequestDTO(final String messageType) {
		Assert.isTrue(supportedMessageTypes.contains(messageType), "Unsupported message type: " + messageType);
		
		this.messageType = messageType;
	}

	//-------------------------------------------------------------------------------------------------
	public String getMessageType() { return messageType; }

	//-------------------------------------------------------------------------------------------------
	public void setMessageType(final String messageType) { this.messageType = messageType; }
	
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