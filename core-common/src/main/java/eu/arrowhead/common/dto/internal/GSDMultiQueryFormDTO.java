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

import eu.arrowhead.common.dto.shared.CloudRequestDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryFormDTO;

public class GSDMultiQueryFormDTO implements Serializable {

	//=================================================================================================
	// members

	private static final long serialVersionUID = -3718839388044509453L;
	
	private List<? extends ServiceQueryFormDTO> requestedServices;
	private List<CloudRequestDTO> preferredClouds;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------	
	public GSDMultiQueryFormDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public GSDMultiQueryFormDTO(final List<? extends ServiceQueryFormDTO> requestedServices, final List<CloudRequestDTO> preferredClouds) {
		this.requestedServices = requestedServices;
		this.preferredClouds = preferredClouds;
	}

	//-------------------------------------------------------------------------------------------------
	public List<? extends ServiceQueryFormDTO> getRequestedServices() { return requestedServices; }
	public List<CloudRequestDTO> getPreferredClouds() { return preferredClouds; }

	//-------------------------------------------------------------------------------------------------
	public void setRequestedServices(final List<? extends ServiceQueryFormDTO> requestedServices) { this.requestedServices = requestedServices; }
	public void setPreferredClouds(final List<CloudRequestDTO> preferredClouds) { this.preferredClouds = preferredClouds; }
	
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