/********************************************************************************
 * Copyright (c) 2021 AITIA
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
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TokenGenerationMultiServiceResponseDTO implements Serializable {

	//=================================================================================================
	// members
	
	private static final long serialVersionUID = -5744544456252438018L;
	
	private List<TokenGenerationDetailedResponseDTO> data = new ArrayList<>();

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public TokenGenerationMultiServiceResponseDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public TokenGenerationMultiServiceResponseDTO(final List<TokenGenerationDetailedResponseDTO> data) {
		this.data = data;
	}

	//-------------------------------------------------------------------------------------------------
	public List<TokenGenerationDetailedResponseDTO> getData() { return data; }
	
	//-------------------------------------------------------------------------------------------------
	public void setData(final List<TokenGenerationDetailedResponseDTO> data) { this.data = data; }
	
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