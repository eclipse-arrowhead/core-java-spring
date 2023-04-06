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
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class OrchestratorStoreFlexibleResponseDTO implements Serializable {

	//=================================================================================================
	// members
	
	private static final long serialVersionUID = -1727435399723300744L;
	
	private long id;
	private SystemDescriberDTO consumerSystem;
	private SystemDescriberDTO providerSystem;
	private String serviceDefinition;
	private String serviceInterface;
	private Map<String,String> serviceMetadata;
	private int priority;
	private String createdAt;	
	private String updatedAt;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public OrchestratorStoreFlexibleResponseDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public OrchestratorStoreFlexibleResponseDTO(final long id, final SystemDescriberDTO consumerSystem, final SystemDescriberDTO providerSystem, final String serviceDefinition,
												final String serviceInterface, final Map<String,String> serviceMetadata, final int priority, final String createdAt, final String updatedAt) {
		this.id = id;
		this.consumerSystem = consumerSystem;
		this.providerSystem = providerSystem;
		this.serviceDefinition = serviceDefinition;
		this.serviceInterface = serviceInterface;
		this.serviceMetadata = serviceMetadata;
		this.priority = priority;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	//-------------------------------------------------------------------------------------------------
	public long getId() { return id; }
	public SystemDescriberDTO getConsumerSystem() { return consumerSystem; }
	public SystemDescriberDTO getProviderSystem() { return providerSystem; }
	public String getServiceDefinition() { return serviceDefinition; }
	public String getServiceInterface() { return serviceInterface; }
	public Map<String,String> getServiceMetadata() { return serviceMetadata; }
	public int getPriority() { return priority; }
	public String getCreatedAt() { return createdAt; }
	public String getUpdatedAt() { return updatedAt; }

	//-------------------------------------------------------------------------------------------------
	public void setId(final long id) { this.id = id; }
	public void setConsumerSystem(final SystemDescriberDTO consumerSystem) { this.consumerSystem = consumerSystem; }
	public void setProviderSystem(final SystemDescriberDTO providerSystem) { this.providerSystem = providerSystem; }
	public void setServiceDefinition(final String serviceDefinition) { this.serviceDefinition = serviceDefinition; }
	public void setServiceInterface(final String serviceInterface) { this.serviceInterface = serviceInterface; }
	public void setServiceMetadata(final Map<String,String> serviceMetadata) { this.serviceMetadata = serviceMetadata; }
	public void setPriority(final int priority) { this.priority = priority; }
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