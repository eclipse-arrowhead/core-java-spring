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
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@JsonInclude(Include.NON_NULL)
public class DeviceResponseDTO implements Serializable {

	//=================================================================================================
	// members

	private static final long serialVersionUID = 3919207845374510215L;

	private long id;
	private String deviceName;
	private String address;
	private String macAddress;
	private String authenticationInfo;
	private String createdAt;
	private String updatedAt;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public DeviceResponseDTO() {}

	//-------------------------------------------------------------------------------------------------
	public DeviceResponseDTO(final long deviceId, final String deviceName, final String address, final String macAddress, final String authenticationInfo, final String createdAt, final String upDatedAt) {
		this.id = deviceId;
		this.deviceName = deviceName;
		this.address = address;
		this.macAddress = macAddress;
		this.authenticationInfo = authenticationInfo;
		this.createdAt = createdAt;
		this.updatedAt = upDatedAt;
	}

	//-------------------------------------------------------------------------------------------------
	public long getId() { return id;	}
	public String getDeviceName() { return deviceName; }
	public String getAddress() { return address; }
	public String getMacAddress() { return macAddress; }
	public String getAuthenticationInfo() {	return authenticationInfo; }
	public String getUpdatedAt() { return updatedAt; }
	public String getCreatedAt() { return createdAt; }
	
	//-------------------------------------------------------------------------------------------------
	public void setId(final long sytemId) { this.id = sytemId; }
	public void setDeviceName(final String deviceName) { this.deviceName = deviceName; }
	public void setAddress(final String address) { this.address = address; }
	public void setMacAddress(final String macAddress) { this.macAddress = macAddress; }
	public void setAuthenticationInfo(final String authenticationInfo) { this.authenticationInfo = authenticationInfo; }
	public void setCreatedAt(final String createdAt) { this.createdAt = createdAt; }
	public void setUpdatedAt(final String updatedAt) { this.updatedAt = updatedAt; }
	
	//-------------------------------------------------------------------------------------------------
	@Override
	public int hashCode() {
		return Objects.hash(address, macAddress, deviceName);
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
		final DeviceResponseDTO other = (DeviceResponseDTO) obj;
		
		return Objects.equals(address, other.address) &&
				Objects.equals(macAddress, other.macAddress) &&
				Objects.equals(deviceName, other.deviceName);
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