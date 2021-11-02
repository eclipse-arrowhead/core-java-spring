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
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class GSDMultiQueryResultDTO implements Serializable {

	//=================================================================================================
	// members

	private static final long serialVersionUID = -1814905338509389537L;
	
	private List<GSDMultiPollResponseDTO> results;
	private int unsuccessfulRequests;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public GSDMultiQueryResultDTO() {}
	
	//-------------------------------------------------------------------------------------------------	
	public GSDMultiQueryResultDTO(final List<GSDMultiPollResponseDTO> results, final int unsuccessfulRequests) {
		this.results = results;
		this.unsuccessfulRequests = unsuccessfulRequests;
	}
	
	//-------------------------------------------------------------------------------------------------	
	public List<GSDMultiPollResponseDTO> getResults() { return results; }
	public int getUnsuccessfulRequests() { return unsuccessfulRequests; }
	
	//-------------------------------------------------------------------------------------------------	
	public void setResults(final List<GSDMultiPollResponseDTO> results) { this.results = results; }
	public void setUnsuccessfulRequests(final int unsuccessfulRequests) { this.unsuccessfulRequests = unsuccessfulRequests; }
	
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