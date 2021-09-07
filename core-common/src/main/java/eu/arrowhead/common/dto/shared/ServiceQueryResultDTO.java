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

package eu.arrowhead.common.dto.shared;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@JsonInclude(Include.NON_NULL)
public class ServiceQueryResultDTO implements Serializable {

	//=================================================================================================
	// members
	
	private static final long serialVersionUID = -1822444510232108526L;
	
	private List<ServiceRegistryResponseDTO> serviceQueryData = new ArrayList<>();
	private int unfilteredHits = 0;

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public List<ServiceRegistryResponseDTO> getServiceQueryData() { return serviceQueryData; }
	public int getUnfilteredHits() { return unfilteredHits; }

	//-------------------------------------------------------------------------------------------------
	public void setServiceQueryData(final List<ServiceRegistryResponseDTO> serviceQueryData) { this.serviceQueryData = serviceQueryData; }
	public void setUnfilteredHits(final int unfilteredHits) { this.unfilteredHits = unfilteredHits; }
	
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