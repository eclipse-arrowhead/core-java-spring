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

import eu.arrowhead.common.dto.shared.CloudRequestDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryFormDTO;

public class GSDQueryFormDTO implements Serializable {

	//=================================================================================================
	// members

	private static final long serialVersionUID = 1906800805302171428L;
	
	private ServiceQueryFormDTO requestedService;
	private List<CloudRequestDTO> preferredClouds;
	private boolean needQoSMeasurements;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------	
	public GSDQueryFormDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public GSDQueryFormDTO(final ServiceQueryFormDTO requestedService, final List<CloudRequestDTO> preferredClouds,
						   final boolean needQoSMeasurements) {
		this.requestedService = requestedService;
		this.preferredClouds = preferredClouds;
		this.needQoSMeasurements = needQoSMeasurements;
	}

	//-------------------------------------------------------------------------------------------------
	public ServiceQueryFormDTO getRequestedService() { return requestedService; }
	public List<CloudRequestDTO> getPreferredClouds() { return preferredClouds; }
	public boolean getNeedQoSMeasurements() { return needQoSMeasurements; }

	//-------------------------------------------------------------------------------------------------
	public void setRequestedService(final ServiceQueryFormDTO requestedService) { this.requestedService = requestedService; }
	public void setPreferredClouds(final List<CloudRequestDTO> preferredClouds) { this.preferredClouds = preferredClouds; }
	public void setNeedQoSMeasurements (final boolean needQoSMeasurements) { this.needQoSMeasurements = needQoSMeasurements; }
	
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