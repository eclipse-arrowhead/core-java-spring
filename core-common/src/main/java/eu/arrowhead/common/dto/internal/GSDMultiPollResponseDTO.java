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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.arrowhead.common.dto.shared.ErrorWrapperDTO;

public class GSDMultiPollResponseDTO implements Serializable, ErrorWrapperDTO {

	//=================================================================================================
	// members
		
	private static final long serialVersionUID = 8097505361768099032L;
	
	private CloudResponseDTO providerCloud;
	private List<String> providedServiceDefinitions;
	
	//=================================================================================================
	// methods
		
	//-------------------------------------------------------------------------------------------------	
	public GSDMultiPollResponseDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public GSDMultiPollResponseDTO(final CloudResponseDTO providerCloud, final List<String> providedServiceDefinitions) {
		this.providerCloud = providerCloud;
		this.providedServiceDefinitions = providedServiceDefinitions;
	}

	//-------------------------------------------------------------------------------------------------	
	public CloudResponseDTO getProviderCloud() { return providerCloud; }
	public List<String> getProvidedServiceDefinitions() { return providedServiceDefinitions; }

	//-------------------------------------------------------------------------------------------------	
	public void setProviderCloud(final CloudResponseDTO providerCloud) { this.providerCloud = providerCloud; }
	public void setProvidedServiceDefinitions(final List<String> providedServiceDefinitions) { this.providedServiceDefinitions = providedServiceDefinitions; }

	//-------------------------------------------------------------------------------------------------
	@JsonIgnore
	@Override
	public boolean isError() {
		return false;
	}
	
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