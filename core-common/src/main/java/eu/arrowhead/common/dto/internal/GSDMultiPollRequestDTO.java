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

public class GSDMultiPollRequestDTO implements Serializable {

	//=================================================================================================
	// members
	
	private static final long serialVersionUID = 4436215706689929893L;

	private List<? extends ServiceQueryFormDTO> requestedServices;
	private CloudRequestDTO requesterCloud;	
	private boolean gatewayIsPresent = false;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public GSDMultiPollRequestDTO() {} 	
	
	//-------------------------------------------------------------------------------------------------
	public GSDMultiPollRequestDTO(final List<? extends ServiceQueryFormDTO> requestedServices, final CloudRequestDTO requesterCloud, final boolean gatewayIsPresent) {
		this.requestedServices = requestedServices;
		this.requesterCloud = requesterCloud;
		this.gatewayIsPresent = gatewayIsPresent;
	}

	//-------------------------------------------------------------------------------------------------	
	public List<? extends ServiceQueryFormDTO> getRequestedServices() { return requestedServices; }
	public CloudRequestDTO getRequesterCloud() { return requesterCloud; } 
	public boolean isGatewayIsPresent() { return gatewayIsPresent; }

	//-------------------------------------------------------------------------------------------------	
	public void setRequestedServices(final List<? extends ServiceQueryFormDTO> requestedServices) { this.requestedServices = requestedServices; }
	public void setRequesterCloud(final CloudRequestDTO requesterCloud) { this.requesterCloud = requesterCloud; }
	public void setGatewayIsPresent(final boolean gatewayIsPresent) { this.gatewayIsPresent = gatewayIsPresent; }
	
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