/********************************************************************************
 * Copyright (c) 2020 FHB
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   FHB - implementation
 *   Arrowhead Consortia - conceptualization
 ********************************************************************************/

package eu.arrowhead.common.dto.internal;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.arrowhead.common.dto.shared.SystemRegistryResponseDTO;

@JsonInclude(Include.NON_NULL)
public class SystemRegistryListResponseDTO implements Serializable {

	//=================================================================================================
	// members

	private static final long serialVersionUID = 3892383727230105100L;

	private List<SystemRegistryResponseDTO> data;
	private long count;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public SystemRegistryListResponseDTO() {}

	//-------------------------------------------------------------------------------------------------
	public SystemRegistryListResponseDTO(final List<SystemRegistryResponseDTO> data) {
		this(data, data.size());
	}

	//-------------------------------------------------------------------------------------------------
	public SystemRegistryListResponseDTO(final List<SystemRegistryResponseDTO> data, final long count) {
		this.data = data;
		this.count = count;
	}

	//-------------------------------------------------------------------------------------------------
	public List<SystemRegistryResponseDTO> getData() { return data; }
	public long getCount() { return count; }
	
	//-------------------------------------------------------------------------------------------------
	public void setData(final List<SystemRegistryResponseDTO> data) { this.data = data; }
	public void setCount(final long count) { this.count = count; }
	
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