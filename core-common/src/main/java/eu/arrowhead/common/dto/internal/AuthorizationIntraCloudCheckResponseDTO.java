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

import eu.arrowhead.common.dto.shared.SystemResponseDTO;

public class AuthorizationIntraCloudCheckResponseDTO implements Serializable {
	
	//=================================================================================================
	// members
	
	private static final long serialVersionUID = -7220577872110448998L;
	
	private SystemResponseDTO consumer;
	private long serviceDefinitionId;
	private List<IdIdListDTO> authorizedProviderIdsWithInterfaceIds;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public AuthorizationIntraCloudCheckResponseDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public AuthorizationIntraCloudCheckResponseDTO(final SystemResponseDTO consumer, final long serviceDefinitionId, final List<IdIdListDTO> authorizedProviderIdsWithInterfaceIds) {
		this.consumer = consumer;
		this.serviceDefinitionId = serviceDefinitionId;
		this.authorizedProviderIdsWithInterfaceIds = authorizedProviderIdsWithInterfaceIds;
	}

	//-------------------------------------------------------------------------------------------------
	public SystemResponseDTO getConsumer() { return consumer; }
	public Long getServiceDefinitionId() { return serviceDefinitionId; }
	public List<IdIdListDTO> getAuthorizedProviderIdsWithInterfaceIds() { return authorizedProviderIdsWithInterfaceIds; }
	
	//-------------------------------------------------------------------------------------------------
	public void setConsumer(final SystemResponseDTO consumer) { this.consumer = consumer; }
	public void setServiceDefinitionId(final Long serviceDefinitionId) { this.serviceDefinitionId = serviceDefinitionId; }
	public void setAuthorizedProviderIdsWithInterfaceIds(final List<IdIdListDTO> authorizedProviderIdsWithInterfaceIds) { this.authorizedProviderIdsWithInterfaceIds = authorizedProviderIdsWithInterfaceIds; }
	
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