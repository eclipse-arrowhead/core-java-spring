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

package eu.arrowhead.common.dto.shared;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ServiceRegistryRequestDTO implements Serializable {
	
	//=================================================================================================
	// members
	
	public static final String KEY_RECOMMENDED_ORCHESTRATION_TIME = "recommendedOrchestrationTime"; // in seconds
	
	private static final long serialVersionUID = -3805773665976065056L;
	
	private String serviceDefinition;
	private SystemRequestDTO providerSystem;
	private String serviceUri;
	private String endOfValidity;
	private String secure;
	private Map<String,String> metadata;
	private Integer version;
	private List<String> interfaces;

	//=================================================================================================
	// constructors

	//-------------------------------------------------------------------------------------------------
	public ServiceRegistryRequestDTO() {}

	//-------------------------------------------------------------------------------------------------
	public ServiceRegistryRequestDTO(final String serviceDefinition, final SystemRequestDTO providerSystem, final String serviceUri, final String endOfValidity, final String secure, final List<String> interfaces) {
		this.serviceDefinition = serviceDefinition;
		this.providerSystem = providerSystem;
		this.serviceUri = serviceUri;
		this.endOfValidity = endOfValidity;
		this.secure = secure;
		this.interfaces = interfaces;
	}

	//-------------------------------------------------------------------------------------------------
	public ServiceRegistryRequestDTO(final String serviceDefinition, final SystemRequestDTO providerSystem, final String serviceUri, final String endOfValidity, final String secure, final Map<String, String> metadata, final Integer version,
									 final List<String> interfaces) {
		this.serviceDefinition = serviceDefinition;
		this.providerSystem = providerSystem;
		this.serviceUri = serviceUri;
		this.endOfValidity = endOfValidity;
		this.secure = secure;
		this.metadata = metadata;
		this.version = version;
		this.interfaces = interfaces;
	}

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public String getServiceDefinition() { return serviceDefinition; }
	public SystemRequestDTO getProviderSystem() { return providerSystem; }
	public String getServiceUri() { return serviceUri; }
	public String getEndOfValidity() { return endOfValidity; }
	public String getSecure() { return secure; }
	public Map<String,String> getMetadata() { return metadata; }
	public Integer getVersion() { return version; }
	public List<String> getInterfaces() { return interfaces; }
	
	//-------------------------------------------------------------------------------------------------
	public void setServiceDefinition(final String serviceDefinition) { this.serviceDefinition = serviceDefinition; }
	public void setProviderSystem(final SystemRequestDTO providerSystem) { this.providerSystem = providerSystem; }
	public void setServiceUri(final String serviceUri) { this.serviceUri = serviceUri; }
	public void setEndOfValidity(final String endOfValidity) { this.endOfValidity = endOfValidity; }
	public void setSecure(final String secure) { this.secure = secure; }
	public void setMetadata(final Map<String,String> metadata) { this.metadata = metadata; }
	public void setVersion(final Integer version) { this.version = version; }
	public void setInterfaces(final List<String> interfaces) { this.interfaces = interfaces; }

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