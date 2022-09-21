/********************************************************************************
 * Copyright (c) 2020 FHB
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   FHB - implementation
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
public class SystemRegistryResponseDTO implements Serializable {
	
	//=================================================================================================
	// members

	private static final long serialVersionUID = -635438605292398404L;
	
	private long id;
	private SystemResponseDTO system;
	private DeviceResponseDTO provider;
	private String endOfValidity;
	private Map<String,String> metadata;
	private int version;
	private String createdAt;
	private String updatedAt;

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public SystemRegistryResponseDTO() {}

	//-------------------------------------------------------------------------------------------------
	public SystemRegistryResponseDTO(final long id, final SystemResponseDTO system, final DeviceResponseDTO provider, final String endOfValidity,
									 final Map<String, String> metadata, final int version, final String createdAt, final String updatedAt) {
		this.id = id;
		this.system = system;
		this.provider = provider;
		this.endOfValidity = endOfValidity;
		this.metadata = metadata;
		this.version = version;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	//-------------------------------------------------------------------------------------------------
	public long getId() { return id; }
	public SystemResponseDTO getSystem() { return system; }
	public DeviceResponseDTO getProvider() { return provider; }
	public String getEndOfValidity() { return endOfValidity; }
	public Map<String,String> getMetadata() { return metadata; }
	public int getVersion() { return version; }
	public String getCreatedAt() { return createdAt; }
	public String getUpdatedAt() { return updatedAt; }
	
	//-------------------------------------------------------------------------------------------------
	public void setId(final long id) { this.id = id; }
	public void setSystem(final SystemResponseDTO system) { this.system = system; }
	public void setProvider(final DeviceResponseDTO provider) { this.provider = provider; }
	public void setEndOfValidity(final String endOfValidity) { this.endOfValidity = endOfValidity; }
	public void setMetadata(final Map<String,String> metadata) { this.metadata = metadata; }
	public void setVersion(final int version) { this.version = version; }
	public void setCreatedAt(final String createdAt) { this.createdAt = createdAt; }
	public void setUpdatedAt(final String updatedAt) { this.updatedAt = updatedAt; }

	//-------------------------------------------------------------------------------------------------
	@Override
	public boolean equals(final Object o) {
		if (this == o) { 
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final SystemRegistryResponseDTO that = (SystemRegistryResponseDTO) o;
		return id == that.id &&
				version == that.version &&
				system.equals(that.system) &&
				provider.equals(that.provider) &&
				endOfValidity.equals(that.endOfValidity) &&
				Objects.equals(metadata, that.metadata) &&
				createdAt.equals(that.createdAt) &&
				updatedAt.equals(that.updatedAt);
	}

	//-------------------------------------------------------------------------------------------------
	@Override
	public int hashCode() {
		return Objects.hash(id, system, provider, endOfValidity, metadata, version, createdAt, updatedAt);
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