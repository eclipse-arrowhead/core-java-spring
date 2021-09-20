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
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class IdIdListDTO implements Serializable {
	
	//=================================================================================================
	// members

	private static final long serialVersionUID = 4894395293592007699L;
	
	private Long id;
	private List<Long> idList;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public IdIdListDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public IdIdListDTO(final Long id, final List<Long> idList) {
		this.id = id;
		this.idList = idList;
	}

	//-------------------------------------------------------------------------------------------------
	public Long getId() { return id; }
	public List<Long> getIdList() { return idList; }

	//-------------------------------------------------------------------------------------------------
	public void setId(final Long id) { this.id = id; }
	public void setIdList(final List<Long> idList) { this.idList = idList; }
	
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