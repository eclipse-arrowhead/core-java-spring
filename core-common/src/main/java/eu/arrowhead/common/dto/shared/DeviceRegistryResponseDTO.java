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
public class DeviceRegistryResponseDTO implements Serializable {
	
	//=================================================================================================
	// members

	private static final long serialVersionUID = -635438605292398404L;
	
	private long id;
	private DeviceResponseDTO device;
	private String endOfValidity;
	private Map<String,String> metadata;
	private int version;
	private String createdAt;
	private String updatedAt;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public DeviceRegistryResponseDTO() {}

	//-------------------------------------------------------------------------------------------------
	public DeviceRegistryResponseDTO(final long id, final DeviceResponseDTO device, final String endOfValidity, final Map<String, String> metadata, final int version, final String createdAt, final String updatedAt) {
		this.id = id;
		this.device = device;
		this.endOfValidity = endOfValidity;
		this.metadata = metadata;
		this.version = version;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	//-------------------------------------------------------------------------------------------------
	public long getId() { return id; }
	public DeviceResponseDTO getDevice() { return device; }
	public String getEndOfValidity() { return endOfValidity; }
	public Map<String,String> getMetadata() { return metadata; }
	public int getVersion() { return version; }
	public String getCreatedAt() { return createdAt; }
	public String getUpdatedAt() { return updatedAt; }
	
	//-------------------------------------------------------------------------------------------------
	public void setId(final long id) { this.id = id; }
	public void setDevice(final DeviceResponseDTO device) { this.device = device; }
	public void setEndOfValidity(final String endOfValidity) { this.endOfValidity = endOfValidity; }
	public void setMetadata(final Map<String,String> metadata) { this.metadata = metadata; }
	public void setVersion(final int version) { this.version = version; }
	public void setCreatedAt(final String createdAt) { this.createdAt = createdAt; }
	public void setUpdatedAt(final String updatedAt) { this.updatedAt = updatedAt; }

	//-------------------------------------------------------------------------------------------------
	@Override
	public boolean equals(final Object o) {
		if (this == o) { return true; }
		if (o == null || getClass() != o.getClass()) { return false; }
		final DeviceRegistryResponseDTO that = (DeviceRegistryResponseDTO) o;
		return id == that.id &&
				version == that.version &&
				device.equals(that.device) &&
				Objects.equals(endOfValidity, that.endOfValidity) &&
				Objects.equals(metadata, that.metadata) &&
				createdAt.equals(that.createdAt) &&
				updatedAt.equals(that.updatedAt);
	}

	//-------------------------------------------------------------------------------------------------
	@Override
	public int hashCode() {
		return Objects.hash(id, device, endOfValidity, metadata, version, createdAt, updatedAt);
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