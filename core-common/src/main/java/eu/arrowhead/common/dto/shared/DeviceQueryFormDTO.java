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
import java.util.HashMap;
import java.util.Map;

import org.springframework.util.Assert;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DeviceQueryFormDTO implements Serializable {

	//=================================================================================================
	// members

	private static final long serialVersionUID = -2699823381165019336L;

	private String deviceNameRequirements;
	private String addressRequirement;
	private String macAddressRequirement;
	private Map<String,String> metadataRequirements; // if specified the whole content of the map must match
	private Integer versionRequirement; // if specified version must match
	private Integer minVersionRequirement; // if specified version must be equals or higher; ignored if versionRequirement is specified
	private Integer maxVersionRequirement; // if specified version must be equals or lower; ignored if versionRequirement is specified

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public DeviceQueryFormDTO() {}

	//-------------------------------------------------------------------------------------------------
	public String getDeviceNameRequirements() { return deviceNameRequirements; }
	public String getAddressRequirement() { return addressRequirement; }
	public String getMacAddressRequirement() { return macAddressRequirement; }
	public Map<String,String> getMetadataRequirements() { return metadataRequirements; }
	public Integer getVersionRequirement() { return versionRequirement; }
	public Integer getMinVersionRequirement() { return minVersionRequirement; }
	public Integer getMaxVersionRequirement() { return maxVersionRequirement; }

	//-------------------------------------------------------------------------------------------------
	public void setDeviceNameRequirements(final String deviceNameRequirements) { this.deviceNameRequirements = deviceNameRequirements; }
	public void setAddressRequirement(final String addressRequirement) { this.addressRequirement = addressRequirement; }
	public void setMacAddressRequirement(final String macAddressRequirement) { this.macAddressRequirement = macAddressRequirement; }
	public void setMetadataRequirements(final Map<String,String> metadataRequirements) { this.metadataRequirements = metadataRequirements; }
	public void setVersionRequirement(final Integer versionRequirement) { this.versionRequirement = versionRequirement; }
	public void setMinVersionRequirement(final Integer minVersionRequirement) { this.minVersionRequirement = minVersionRequirement; }
	public void setMaxVersionRequirement(final Integer maxVersionRequirement) { this.maxVersionRequirement = maxVersionRequirement; }

	//-------------------------------------------------------------------------------------------------
	@Override
	public String toString() {
		try {
			return new ObjectMapper().writeValueAsString(this);
		} catch (final JsonProcessingException ex) {
			return "toString failure";
		}
	}

	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	private DeviceQueryFormDTO(final Builder builder) {
		this.deviceNameRequirements = builder.deviceNameRequirements;
		this.addressRequirement = builder.addressRequirements;
		this.macAddressRequirement = builder.macAddressRequirement;
		this.metadataRequirements = builder.metadataRequirements;
		this.versionRequirement = builder.versionRequirement;
		this.minVersionRequirement = builder.minVersionRequirement;
		this.maxVersionRequirement = builder.maxVersionRequirement;
	}

	//=================================================================================================
	// nested classes
	
	//-------------------------------------------------------------------------------------------------
	public static class Builder {
		
		//=================================================================================================
		// members

		private final String deviceNameRequirements;
		private String addressRequirements;
		private String macAddressRequirement;
		private Map<String,String> metadataRequirements;
		private Integer versionRequirement; 
		private Integer minVersionRequirement; 
		private Integer maxVersionRequirement; 

		//=================================================================================================
		// methods
		
		//-------------------------------------------------------------------------------------------------
		public Builder(final String deviceNameRequirements) {
			Assert.isTrue(deviceNameRequirements != null && !deviceNameRequirements.isBlank(), "deviceNameRequirements is null or blank.");
			this.deviceNameRequirements = deviceNameRequirements;
		}


		//-------------------------------------------------------------------------------------------------
		public Builder address(final String address) {
			this.addressRequirements = address;
			return this;
		}

		//-------------------------------------------------------------------------------------------------
		public Builder macAddress(final String macAddress) {
			this.macAddressRequirement = macAddress;
			return this;
		}

		//-------------------------------------------------------------------------------------------------
		public Builder metadata(final Map<String,String> metadataRequirements) {
			this.metadataRequirements = metadataRequirements;
			return this;
		}

		//-------------------------------------------------------------------------------------------------
		public Builder metadata(final String key, final String value) {
			Assert.isTrue(key != null && !key.isBlank(), "Key is null or blank");
			
			if (this.metadataRequirements == null) {
				this.metadataRequirements = new HashMap<>();
			}
			this.metadataRequirements.put(key, value);
			
			return this;
		}
		
		//-------------------------------------------------------------------------------------------------
		public Builder version(final Integer versionRequirement) {
			this.versionRequirement = versionRequirement;
			return this;
		}
		
		//-------------------------------------------------------------------------------------------------
		public Builder version(final Integer minVersionRequirement, final Integer maxVersionRequirement) {
			this.minVersionRequirement = minVersionRequirement;
			this.maxVersionRequirement = maxVersionRequirement;
			return this;
		}

		//-------------------------------------------------------------------------------------------------
		public DeviceQueryFormDTO build() {
			return new DeviceQueryFormDTO(this);
		}
	}
}