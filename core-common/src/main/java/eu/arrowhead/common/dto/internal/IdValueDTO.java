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

public class IdValueDTO implements Serializable {

	//=================================================================================================
	// members
	
	private static final long serialVersionUID = 4139505993669240877L;

	private long id;
	private String value;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public IdValueDTO() {}

	//-------------------------------------------------------------------------------------------------
	public IdValueDTO(final long id, final String value) {
		this.id = id;
		this.value = value;
	}

	//-------------------------------------------------------------------------------------------------
	public long getId() { return id; }
	public String getValue() { return value; }

	//-------------------------------------------------------------------------------------------------
	public void setId(final long id) { this.id = id; }
	public void setValue(final String value) { this.value = value; }
	
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