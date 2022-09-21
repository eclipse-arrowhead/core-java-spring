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
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@JsonInclude(Include.NON_NULL)
public class SystemResponseDTO implements Serializable {

	//=================================================================================================
	// members
	
	private static final long serialVersionUID = 2768149485270495101L;
	
	private long id;
	private String systemName;
	private String address;
	private int port;
	private String authenticationInfo;
	private Map<String,String> metadata;
	private String createdAt;
	private String updatedAt;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public SystemResponseDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public SystemResponseDTO(final long systemId, final String systemName, final String address, final int port, final String authenticationInfo, final Map<String,String> metadata, final String createdAt, final String updatedAt) {
		this.id = systemId;
		this.systemName = systemName;
		this.address = address;
		this.port = port;
		this.authenticationInfo = authenticationInfo;
		this.metadata = metadata;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	//-------------------------------------------------------------------------------------------------
	public long getId() { return id;	}
	public String getSystemName() { return systemName; }
	public String getAddress() { return address; }
	public int getPort() { return port; }
	public String getAuthenticationInfo() {	return authenticationInfo; }
	public Map<String,String> getMetadata() { return metadata; }
	public String getUpdatedAt() { return updatedAt; }
	public String getCreatedAt() { return createdAt; }
	
	//-------------------------------------------------------------------------------------------------
	public void setId(final long sytemId) { this.id = sytemId; }
	public void setSystemName(final String systemName) { this.systemName = systemName; }
	public void setAddress(final String address) { this.address = address; }
	public void setPort(final int port) { this.port = port; }
	public void setAuthenticationInfo(final String authenticationInfo) { this.authenticationInfo = authenticationInfo; }
	public void setMetadata(final Map<String,String> metadata) { this.metadata = metadata; }
	public void setCreatedAt(final String createdAt) { this.createdAt = createdAt; }
	public void setUpdatedAt(final String updatedAt) { this.updatedAt = updatedAt; }
	
	//-------------------------------------------------------------------------------------------------
	@Override
	public int hashCode() {
		return Objects.hash(address, port, systemName);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		
		if (obj == null) {
			return false;
		}
		
		if (getClass() != obj.getClass()) {
			return false;
		}
		
		final SystemResponseDTO other = (SystemResponseDTO) obj;
		
		return Objects.equals(address, other.address) && Objects.equals(port, other.port) && Objects.equals(systemName, other.systemName);
	}

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