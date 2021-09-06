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
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class KeyValuesDTO implements Serializable {

	//=================================================================================================
	// members
	
	private static final long serialVersionUID = 5843236299108271323L;
	
	private Map<String,String> map = new HashMap<>();

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public KeyValuesDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public KeyValuesDTO(final Map<String, String> map) {
		this.map = map;
	}
	
	//-------------------------------------------------------------------------------------------------
	public Map<String, String> getMap() { return map; }
	
	//-------------------------------------------------------------------------------------------------
	public void setMap(final Map<String, String> map) { this.map = map; }
	
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