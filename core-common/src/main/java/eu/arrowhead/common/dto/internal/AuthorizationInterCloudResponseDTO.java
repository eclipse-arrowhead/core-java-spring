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

import eu.arrowhead.common.dto.shared.ServiceDefinitionResponseDTO;
import eu.arrowhead.common.dto.shared.ServiceInterfaceResponseDTO;
import eu.arrowhead.common.dto.shared.SystemResponseDTO;

public class AuthorizationInterCloudResponseDTO implements Serializable {

	//=================================================================================================
	// members
	
	private static final long serialVersionUID = 840542120891817637L;
	
	private long id;
	private CloudResponseDTO cloud;
	private SystemResponseDTO provider;
	private ServiceDefinitionResponseDTO serviceDefinition;
	private List<ServiceInterfaceResponseDTO> interfaces;
	private String createdAt;
	private String updatedAt;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public AuthorizationInterCloudResponseDTO() {}
	
	//-------------------------------------------------------------------------------------------------	
	public AuthorizationInterCloudResponseDTO(final long id, final CloudResponseDTO cloud, final SystemResponseDTO provider, final ServiceDefinitionResponseDTO serviceDefinition,
											  final List<ServiceInterfaceResponseDTO> interfaces, final String createdAt, final String updatedAt) {
		this.id = id;
		this.cloud = cloud;
		this.provider = provider;
		this.serviceDefinition = serviceDefinition;
		this.interfaces = interfaces;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}
	
	//-------------------------------------------------------------------------------------------------
	public long getId() { return id;}
	public CloudResponseDTO getCloud() { return cloud; }
	public SystemResponseDTO getProvider() { return provider; }
	public ServiceDefinitionResponseDTO getServiceDefinition() { return serviceDefinition;	}
	public List<ServiceInterfaceResponseDTO> getInterfaces() { return interfaces; }
	public String getCreatedAt() { return createdAt; }
	public String getUpdatedAt() { return updatedAt; }
	
	//-------------------------------------------------------------------------------------------------
	public void setId(final long id) { this.id = id; }
	public void setCloud(final CloudResponseDTO cloud) { this.cloud = cloud; }
	public void setProvider(final SystemResponseDTO provider) { this.provider = provider; }
	public void setServiceDefinition(final ServiceDefinitionResponseDTO serviceDefinition) { this.serviceDefinition = serviceDefinition; }
	public void setInterfaces(final List<ServiceInterfaceResponseDTO> interfaces) { this.interfaces = interfaces; }
	public void setCreatedAt(final String createdAt) { this.createdAt = createdAt; }
	public void setUpdatedAt(final String updatedAt) { this.updatedAt = updatedAt; }
	
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