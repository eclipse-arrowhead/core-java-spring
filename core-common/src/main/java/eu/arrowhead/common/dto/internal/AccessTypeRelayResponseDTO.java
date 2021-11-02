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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AccessTypeRelayResponseDTO implements Serializable {
	
	//=================================================================================================
	// members
	
	private static final long serialVersionUID = -4308914768098206657L;
	
	private boolean directAccess;
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public AccessTypeRelayResponseDTO() {}

	//-------------------------------------------------------------------------------------------------
	public AccessTypeRelayResponseDTO(final boolean directAccess) {
		this.directAccess = directAccess;
	}
	
	//-------------------------------------------------------------------------------------------------
	public boolean isDirectAccess() { return directAccess; }

	//-------------------------------------------------------------------------------------------------
	public void setDirectAccess(final boolean directAccess) { this.directAccess = directAccess; }
	
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