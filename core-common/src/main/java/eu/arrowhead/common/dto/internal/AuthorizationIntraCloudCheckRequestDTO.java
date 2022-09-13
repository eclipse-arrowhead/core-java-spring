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

import eu.arrowhead.common.dto.shared.SystemRequestDTO;

public class AuthorizationIntraCloudCheckRequestDTO implements Serializable {
	
	//=================================================================================================
	// members
	
	private static final long serialVersionUID = -1007358634594911298L;
	
	private SystemRequestDTO consumer;
	private Long serviceDefinitionId;
	private List<IdIdListDTO> providerIdsWithInterfaceIds;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public AuthorizationIntraCloudCheckRequestDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public AuthorizationIntraCloudCheckRequestDTO(final SystemRequestDTO consumer, final Long serviceDefinitionId, final List<IdIdListDTO> providerIdsWithInterfaceIds) {
		this.consumer = consumer;
		this.serviceDefinitionId = serviceDefinitionId;
		this.providerIdsWithInterfaceIds = providerIdsWithInterfaceIds;
	}

	//-------------------------------------------------------------------------------------------------
	public SystemRequestDTO getConsumer() { return consumer; }
	public Long getServiceDefinitionId() { return serviceDefinitionId; }
	public List<IdIdListDTO> getProviderIdsWithInterfaceIds() { return providerIdsWithInterfaceIds; }

	//-------------------------------------------------------------------------------------------------
	public void setConsumer(final SystemRequestDTO consumer) { this.consumer = consumer; }
	public void setServiceDefinitionId(final Long serviceDefinitionId) { this.serviceDefinitionId = serviceDefinitionId; }
	public void setProviderIdsWithInterfaceIds(final List<IdIdListDTO> providerIdsWithInterfaceIds) { this.providerIdsWithInterfaceIds = providerIdsWithInterfaceIds; }
	
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