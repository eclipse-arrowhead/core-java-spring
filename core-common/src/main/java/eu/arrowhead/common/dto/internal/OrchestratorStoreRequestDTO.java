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

import eu.arrowhead.common.dto.shared.CloudRequestDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;

public class OrchestratorStoreRequestDTO implements Serializable {

	//=================================================================================================
	// members

	private static final long serialVersionUID = 6496923524186210327L;
	
	private String serviceDefinitionName;
	private Long consumerSystemId;
	private SystemRequestDTO providerSystem;
	private CloudRequestDTO cloud;
	private String serviceInterfaceName;
	private Integer priority;	
	private Map<String,String> attribute;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public OrchestratorStoreRequestDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public OrchestratorStoreRequestDTO(final String serviceDefinitionName, final Long consumerSystemId, final SystemRequestDTO providerSystem, final CloudRequestDTO cloud,
									   final String serviceInterfaceName, final Integer priority, final Map<String,String> attribute) {
		this.serviceDefinitionName = serviceDefinitionName;
		this.consumerSystemId = consumerSystemId;
		this.providerSystem = providerSystem;
		this.cloud = cloud;
		this.serviceInterfaceName = serviceInterfaceName;
		this.priority = priority;
		this.attribute = attribute;
	}
	
	//-------------------------------------------------------------------------------------------------
	public String getServiceDefinitionName() {return serviceDefinitionName;}
	public Long getConsumerSystemId() {return consumerSystemId;}
	public SystemRequestDTO getProviderSystem() {return providerSystem;}
	public CloudRequestDTO getCloud() {return cloud;}
	public String getServiceInterfaceName() {return serviceInterfaceName;} 
	public Integer getPriority() {return priority;}
	public Map<String,String> getAttribute() {return attribute;}	

	//-------------------------------------------------------------------------------------------------
	public void setServiceDefinitionName(final String serviceDefinitionName) {this.serviceDefinitionName = serviceDefinitionName;}
	public void setConsumerSystemId(final Long consumerSystemId) {this.consumerSystemId = consumerSystemId;}
	public void setProviderSystem(final SystemRequestDTO providerSystemDTO) {this.providerSystem = providerSystemDTO;}
	public void setCloud(final CloudRequestDTO cloudDTO) {this.cloud = cloudDTO;}
	public void setServiceInterfaceName(final String serviceInterfaceName) {this.serviceInterfaceName = serviceInterfaceName; }
	public void setPriority(final Integer priority) {this.priority = priority;}
	public void setAttribute(final Map<String,String> attribute) {this.attribute = attribute;}
	
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