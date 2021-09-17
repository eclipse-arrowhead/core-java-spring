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

package eu.arrowhead.common.dto.shared;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@JsonInclude(Include.NON_NULL)
public class ServiceQueryResultListDTO implements Serializable {
	
	//=================================================================================================
	// members
	
	private static final long serialVersionUID = 2195971861730818293L;
	
	private List<ServiceQueryResultDTO> results = new ArrayList<>();
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public ServiceQueryResultListDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public ServiceQueryResultListDTO(final List<ServiceQueryResultDTO> results) {
		if (results != null) {
			this.results = results;
		}
	}

	//-------------------------------------------------------------------------------------------------
	public List<ServiceQueryResultDTO> getResults() { return results; }

	//-------------------------------------------------------------------------------------------------
	public void setResults(final List<ServiceQueryResultDTO> results) {
		if (results != null) {
			this.results = results;
		}
	}
	
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