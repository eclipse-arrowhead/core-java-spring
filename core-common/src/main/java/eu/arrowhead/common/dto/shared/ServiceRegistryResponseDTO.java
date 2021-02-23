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
import java.util.StringJoiner;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class ServiceRegistryResponseDTO implements Serializable {
	
	//=================================================================================================
	// members

	private static final long serialVersionUID = -635438605292398404L;
	
	private long id;
	private ServiceDefinitionResponseDTO serviceDefinition;
	private SystemResponseDTO provider;
	private String serviceUri;
	private String endOfValidity;
	private ServiceSecurityType secure;
	private Map<String,String> metadata;
	private int version;
	private List<ServiceInterfaceResponseDTO> interfaces;
	private String createdAt;
	private String updatedAt;

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public long getId() { return id; }
	public ServiceDefinitionResponseDTO getServiceDefinition() { return serviceDefinition; }
	public SystemResponseDTO getProvider() { return provider; }
	public String getServiceUri() { return serviceUri; }
	public String getEndOfValidity() { return endOfValidity; }
	public ServiceSecurityType getSecure() { return secure; }
	public Map<String,String> getMetadata() { return metadata; }
	public int getVersion() { return version; }
	public List<ServiceInterfaceResponseDTO> getInterfaces() { return interfaces; }
	public String getCreatedAt() { return createdAt; }
	public String getUpdatedAt() { return updatedAt; }
	
	//-------------------------------------------------------------------------------------------------
	public void setId(final long id) { this.id = id; }
	public void setServiceDefinition(final ServiceDefinitionResponseDTO serviceDefinition) { this.serviceDefinition = serviceDefinition; }
	public void setProvider(final SystemResponseDTO provider) { this.provider = provider; }
	public void setServiceUri(final String serviceUri) { this.serviceUri = serviceUri; }
	public void setEndOfValidity(final String endOfValidity) { this.endOfValidity = endOfValidity; }
	public void setSecure(final ServiceSecurityType secure) { this.secure = secure; }
	public void setMetadata(final Map<String,String> metadata) { this.metadata = metadata; }
	public void setVersion(final int version) { this.version = version; }
	public void setInterfaces(final List<ServiceInterfaceResponseDTO> interfaces) { this.interfaces = interfaces; }
	public void setCreatedAt(final String createdAt) { this.createdAt = createdAt; }
	public void setUpdatedAt(final String updatedAt) { this.updatedAt = updatedAt; }

	//-------------------------------------------------------------------------------------------------
	@Override
	public String toString() {
		return new StringJoiner(", ", ServiceRegistryResponseDTO.class.getSimpleName() + "[", "]")
				.add("id=" + id)
				.add("serviceDefinition=" + serviceDefinition)
				.add("provider=" + provider)
				.add("serviceUri='" + serviceUri + "'")
				.add("endOfValidity='" + endOfValidity + "'")
				.add("secure=" + secure)
				.add("metadata=" + metadata)
				.add("version=" + version)
				.add("interfaces=" + interfaces)
				.add("createdAt='" + createdAt + "'")
				.add("updatedAt='" + updatedAt + "'")
				.toString();
	}
}