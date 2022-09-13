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

import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class OrchestratorStoreFlexibleRequestDTO {
	
	//=================================================================================================
	// members
	
	private SystemDescriberDTO consumerSystem;
	private SystemDescriberDTO providerSystem;
	private String serviceDefinitionName;
	private String serviceInterfaceName;
	private Map<String,String> serviceMetadata;
	private Integer priority;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public OrchestratorStoreFlexibleRequestDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public OrchestratorStoreFlexibleRequestDTO(final SystemDescriberDTO consumerSystem, final SystemDescriberDTO providerSystem, final String serviceDefinitionName,
											   final String serviceInterfaceName, final  Map<String,String> serviceMetadata, final Integer priority) {
		this.consumerSystem = consumerSystem;
		this.providerSystem = providerSystem;
		this.serviceDefinitionName = serviceDefinitionName;
		this.serviceInterfaceName = serviceInterfaceName;
		this.serviceMetadata = serviceMetadata;
		this.priority = priority;
	}

	//-------------------------------------------------------------------------------------------------
	public SystemDescriberDTO getConsumerSystem() { return consumerSystem; }
	public SystemDescriberDTO getProviderSystem() { return providerSystem; }
	public String getServiceDefinitionName() { return serviceDefinitionName; }
	public String getServiceInterfaceName() { return serviceInterfaceName; }
	public Map<String,String> getServiceMetadata() { return serviceMetadata; }
	public Integer getPriority() { return priority; }

	//-------------------------------------------------------------------------------------------------
	public void setConsumerSystem(final SystemDescriberDTO consumerSystem) { this.consumerSystem = consumerSystem; }
	public void setProviderSystem(final SystemDescriberDTO providerSystem) { this.providerSystem = providerSystem; }
	public void setServiceDefinitionName(final String serviceDefinitionName) { this.serviceDefinitionName = serviceDefinitionName; }
	public void setServiceInterfaceName(final String serviceInterfaceName) { this.serviceInterfaceName = serviceInterfaceName; }
	public void setServiceMetadata(final Map<String,String> serviceMetadata) { this.serviceMetadata = serviceMetadata; }
	public void setPriority(final Integer priority) { this.priority = priority; }
	
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