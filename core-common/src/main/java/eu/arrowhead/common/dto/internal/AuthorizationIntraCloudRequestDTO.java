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

public class AuthorizationIntraCloudRequestDTO implements Serializable {
	
	//=================================================================================================
	// members
	
	private static final long serialVersionUID = 1322804880331971340L;
	
	private Long consumerId;
	private List<Long> providerIds;
	private List<Long> serviceDefinitionIds;
	private List<Long> interfaceIds;
	
	//=================================================================================================
	// methods
		
	//-------------------------------------------------------------------------------------------------
	public AuthorizationIntraCloudRequestDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public AuthorizationIntraCloudRequestDTO(final Long consumerId, final List<Long> providerIds, final List<Long> serviceDefinitionIds, final List<Long> interfaceIds) {
		this.consumerId = consumerId;
		this.providerIds = providerIds;
		this.serviceDefinitionIds = serviceDefinitionIds;
		this.interfaceIds = interfaceIds;
	}
	
	//-------------------------------------------------------------------------------------------------
	public Long getConsumerId() { return consumerId; }
	public List<Long> getProviderIds() { return providerIds; }
	public List<Long> getServiceDefinitionIds() { return serviceDefinitionIds; }
	public List<Long> getInterfaceIds() { return interfaceIds; }

	//-------------------------------------------------------------------------------------------------
	public void setConsumerId(final Long consumerId) { this.consumerId = consumerId; }
	public void setProviderIds(final List<Long> providerIds) { this.providerIds = providerIds; }
	public void setServiceDefinitionIds(final List<Long> serviceDefinitionIds) { this.serviceDefinitionIds = serviceDefinitionIds; }	
	public void setInterfaceIds(final List<Long> interfaceIds) { this.interfaceIds = interfaceIds; }
	
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