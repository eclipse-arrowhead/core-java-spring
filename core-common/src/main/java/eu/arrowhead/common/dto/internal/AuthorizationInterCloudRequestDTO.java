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

public class AuthorizationInterCloudRequestDTO implements Serializable {

	//=================================================================================================
	// members
	
	private static final long serialVersionUID = 5349360773532348565L;
	
	private Long cloudId; 
	private List<Long> providerIdList;
	private List<Long> serviceDefinitionIdList;
	private List<Long> interfaceIdList;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public AuthorizationInterCloudRequestDTO() {}
	
	//-------------------------------------------------------------------------------------------------	
	public AuthorizationInterCloudRequestDTO(final Long cloudId, final List<Long> providerIdList, final List<Long> serviceDefinitionIdList, final List<Long> interfaceIdList) {
		this.cloudId = cloudId;
		this.providerIdList = providerIdList;
		this.serviceDefinitionIdList = serviceDefinitionIdList;
		this.interfaceIdList = interfaceIdList;
	}
	
	//-------------------------------------------------------------------------------------------------
	public Long getCloudId() { return cloudId; }
	public List<Long> getProviderIdList() { return providerIdList; }
	public List<Long> getServiceDefinitionIdList() { return serviceDefinitionIdList; }
	public List<Long> getInterfaceIdList() { return interfaceIdList; }
	
	//-------------------------------------------------------------------------------------------------
	public void setCloudId(final Long cloudId) { this.cloudId = cloudId; }
	public void setProviderIdList(final List<Long> providerIdList) { this.providerIdList = providerIdList; }
	public void setServiceDefinitionIdList(final List<Long> serviceDefinitionIdList) { this.serviceDefinitionIdList = serviceDefinitionIdList; }
	public void setInterfaceIdList(final List<Long> interfaceIdList) { this.interfaceIdList = interfaceIdList; }
	
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