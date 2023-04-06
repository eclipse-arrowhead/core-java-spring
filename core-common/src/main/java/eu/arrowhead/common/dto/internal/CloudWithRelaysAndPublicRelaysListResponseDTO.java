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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CloudWithRelaysAndPublicRelaysListResponseDTO implements Serializable {

	//=================================================================================================
	// members

	private static final long serialVersionUID = 7672271505929868952L;

	private List<CloudWithRelaysAndPublicRelaysResponseDTO> data;
	private long count;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public CloudWithRelaysAndPublicRelaysListResponseDTO() {}

	//-------------------------------------------------------------------------------------------------
	public CloudWithRelaysAndPublicRelaysListResponseDTO(final List<CloudWithRelaysAndPublicRelaysResponseDTO> data, final long count) {
		this.data = data;
		this.count = count;
	}

	//-------------------------------------------------------------------------------------------------
	public List<CloudWithRelaysAndPublicRelaysResponseDTO> getData() { return data; }
	public long getCount() { return count; }

	//-------------------------------------------------------------------------------------------------
	public void setData(final List<CloudWithRelaysAndPublicRelaysResponseDTO> data) { this.data = data; }
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