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
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.arrowhead.common.dto.shared.ServiceDefinitionResponseDTO;
import eu.arrowhead.common.dto.shared.ServiceInterfaceResponseDTO;
import eu.arrowhead.common.dto.shared.SystemResponseDTO;

public class OrchestratorStoreResponseDTO implements Serializable {
	
	//=================================================================================================
	// members

	private static final long serialVersionUID = -6099079027561380554L;

	private long id;
	private ServiceDefinitionResponseDTO serviceDefinition;	
	private SystemResponseDTO consumerSystem;
	private boolean foreign;
	private SystemResponseDTO providerSystem;
	private CloudResponseDTO providerCloud;
	private ServiceInterfaceResponseDTO serviceInterface;
	private int priority;	
	private Map<String,String> attribute;	
	private String createdAt;	
	private String updatedAt;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public OrchestratorStoreResponseDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public OrchestratorStoreResponseDTO(final long id, final ServiceDefinitionResponseDTO serviceDefinition, final SystemResponseDTO consumerSystem, final boolean foreign,
									    final SystemResponseDTO providerSystem, final CloudResponseDTO providerCloud, final ServiceInterfaceResponseDTO serviceInterface, final int priority,
									    final Map<String,String> attribute, final String createdAt,	final String updatedAt) {
		this.id = id;
		this.serviceDefinition = serviceDefinition;
		this.consumerSystem = consumerSystem;
		this.foreign = foreign;
		this.providerSystem = providerSystem;
		this.providerCloud = providerCloud;
		this.serviceInterface = serviceInterface;
		this.priority = priority;
		this.attribute = attribute;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	//-------------------------------------------------------------------------------------------------
	public long getId() { return id; }
	public ServiceDefinitionResponseDTO getServiceDefinition() { return serviceDefinition; }
	public SystemResponseDTO getConsumerSystem() { return consumerSystem; }
	public boolean getForeign() {return foreign; }
	public SystemResponseDTO getProviderSystem() { return providerSystem; }
	public CloudResponseDTO getProviderCloud() { return providerCloud; }
	public ServiceInterfaceResponseDTO getServiceInterface() { return serviceInterface; }
	public int getPriority() { return priority; }
	public Map<String,String> getAttribute() { return attribute; }
	public String getCreatedAt() { return createdAt; }
	public String getUpdatedAt() { return updatedAt; }

	//-------------------------------------------------------------------------------------------------
	public void setId(final long id) { this.id = id; }
	public void setServiceDefinition(final ServiceDefinitionResponseDTO serviceDefinition) { this.serviceDefinition = serviceDefinition; }
	public void setConsumerSystem(final SystemResponseDTO consumerSystem) { this.consumerSystem = consumerSystem; }
	public void setForeign(final boolean foreign) { this.foreign = foreign; }
	public void setProviderSystem(final SystemResponseDTO providerSystem) { this.providerSystem = providerSystem; }
	public void setProviderCloud(final CloudResponseDTO providerCloud) { this.providerCloud = providerCloud; }
	public void setServiceInterface(final ServiceInterfaceResponseDTO serviceInterface) { this.serviceInterface = serviceInterface; }
	public void setPriority(final int priority) { this.priority = priority; }
	public void setAttribute(final Map<String,String> attribute) { this.attribute = attribute; }
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