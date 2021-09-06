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
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.arrowhead.common.dto.shared.DeviceResponseDTO;

@JsonInclude(Include.NON_NULL)
public class DeviceListResponseDTO implements Serializable {

	//=================================================================================================
	// members

	private static final long serialVersionUID = -1661484009332215820L;

	private List<DeviceResponseDTO> data = new ArrayList<>();
	private long count;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public DeviceListResponseDTO() {}

	//-------------------------------------------------------------------------------------------------
	public DeviceListResponseDTO(final List<DeviceResponseDTO> deviceResponseDTOList, final int totalNumberOfSystems) {
		super();
		this.data = deviceResponseDTOList;
		this.count = totalNumberOfSystems;
	}
	
	//-------------------------------------------------------------------------------------------------
	public List<DeviceResponseDTO> getData() { return data; }
	public long getCount() { return count; }

	//-------------------------------------------------------------------------------------------------
	public void setData(final List<DeviceResponseDTO> data) { this.data = data; }
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