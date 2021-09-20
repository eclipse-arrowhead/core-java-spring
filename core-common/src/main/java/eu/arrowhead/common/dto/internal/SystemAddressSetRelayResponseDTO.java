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
import java.util.HashSet;
import java.util.Set;

import org.springframework.util.Assert;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SystemAddressSetRelayResponseDTO implements Serializable {
	
	//=================================================================================================
	// members
	
	private static final long serialVersionUID = 3040647144187918101L;
	
	private Set<String> addresses = new HashSet<>();

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public SystemAddressSetRelayResponseDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public SystemAddressSetRelayResponseDTO(final Set<String> addresses) {
		Assert.notNull(addresses, "'addresses' is null.");
		
		this.addresses = addresses; 
	}

	//-------------------------------------------------------------------------------------------------
	public Set<String> getAddresses() { return addresses; }

	//-------------------------------------------------------------------------------------------------
	public void setAddresses(final Set<String> addresses) { this.addresses = addresses; }
	
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