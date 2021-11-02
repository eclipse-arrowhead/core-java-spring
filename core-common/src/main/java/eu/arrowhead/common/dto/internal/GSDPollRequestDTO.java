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

import eu.arrowhead.common.dto.shared.CloudRequestDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryFormDTO;

public class GSDPollRequestDTO implements Serializable {

	//=================================================================================================
	// members
	
	private static final long serialVersionUID = 3261316799155286413L;
	
	private ServiceQueryFormDTO requestedService;
	private CloudRequestDTO requesterCloud;	
	private boolean gatewayIsPresent = false;
	private boolean needQoSMeasurements = false;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public GSDPollRequestDTO() {} 	
	
	//-------------------------------------------------------------------------------------------------
	public GSDPollRequestDTO(final ServiceQueryFormDTO requestedService, final CloudRequestDTO requesterCloud, final boolean gatewayIsPresent,
							 final boolean needQoSMeasurements) {
		this.requestedService = requestedService;
		this.gatewayIsPresent = gatewayIsPresent;
		this.requesterCloud = requesterCloud;
		this.needQoSMeasurements = needQoSMeasurements;
	}

	//-------------------------------------------------------------------------------------------------	
	public ServiceQueryFormDTO getRequestedService() { return requestedService; }
	public CloudRequestDTO getRequesterCloud() { return requesterCloud; } 
	public boolean isGatewayIsPresent() { return gatewayIsPresent; }
	public boolean getNeedQoSMeasurements() { return needQoSMeasurements; }

	//-------------------------------------------------------------------------------------------------	
	public void setRequestedService(final ServiceQueryFormDTO requestedService) { this.requestedService = requestedService; }
	public void setRequesterCloud(final CloudRequestDTO requesterCloud) { this.requesterCloud = requesterCloud; }
	public void setGatewayIsPresent(final boolean gatewayIsPresent) { this.gatewayIsPresent = gatewayIsPresent; }
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