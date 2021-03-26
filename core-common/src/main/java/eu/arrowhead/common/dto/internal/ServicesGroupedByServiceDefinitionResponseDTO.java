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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import eu.arrowhead.common.dto.shared.ServiceRegistryResponseDTO;

@JsonInclude(Include.NON_NULL)
public class ServicesGroupedByServiceDefinitionResponseDTO implements Serializable {

	//=================================================================================================
	// members
	
	private static final long serialVersionUID = 8564324985631020025L;
	
	private long serviceDefinitionId;
	private String serviceDefinition;
	private List<ServiceRegistryResponseDTO> providerServices;
			
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public ServicesGroupedByServiceDefinitionResponseDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public ServicesGroupedByServiceDefinitionResponseDTO(final long serviceDefinitionId, final String serviceDefinition, final List<ServiceRegistryResponseDTO> providerServices) {
		this.serviceDefinitionId = serviceDefinitionId;
		this.serviceDefinition = serviceDefinition;
		this.providerServices = providerServices;
	}

	//-------------------------------------------------------------------------------------------------
	public long getServiceDefinitionId() { return serviceDefinitionId; }
	public String getServiceDefinition() { return serviceDefinition; }
	public List<ServiceRegistryResponseDTO> getProviderServices() { return providerServices; }
	
	//-------------------------------------------------------------------------------------------------
	public void setServiceDefinitionId(final long serviceDefinitionId) { this.serviceDefinitionId = serviceDefinitionId; }
	public void setServiceDefinition(final String serviceDefinition) { this.serviceDefinition = serviceDefinition; }
	public void setProviderServices(final List<ServiceRegistryResponseDTO> providerServices) { this.providerServices = providerServices; }	
}