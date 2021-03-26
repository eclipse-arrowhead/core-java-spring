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

import org.springframework.util.Assert;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

public class SystemQueryFormDTO implements Serializable {

	//=================================================================================================
	// members

	private static final long serialVersionUID = -2699823381165019336L;

	private String systemNameRequirements;
	private String deviceNameRequirements;
	private Map<String,String> metadataRequirements; // if specified the whole content of the map must match
	private Integer versionRequirement; // if specified version must match
	private Integer minVersionRequirement; // if specified version must be equals or higher; ignored if versionRequirement is specified
	private Integer maxVersionRequirement; // if specified version must be equals or lower; ignored if versionRequirement is specified

	private boolean pingProviders = false;


	//=================================================================================================
	// methods
	//-------------------------------------------------------------------------------------------------

	public SystemQueryFormDTO() {}
	//-------------------------------------------------------------------------------------------------

	public String getSystemNameRequirements() { return systemNameRequirements; }
	public String getDeviceNameRequirements() { return deviceNameRequirements; }
	public Map<String,String> getMetadataRequirements() { return metadataRequirements; }
	public Integer getVersionRequirement() { return versionRequirement; }
	public Integer getMinVersionRequirement() { return minVersionRequirement; }
	public Integer getMaxVersionRequirement() { return maxVersionRequirement; }
	public boolean getPingProviders() { return pingProviders; }
	//-------------------------------------------------------------------------------------------------

	public void setSystemNameRequirements(final String systemNameRequirements) { this.systemNameRequirements = systemNameRequirements; }
	public void setDeviceNameRequirements(final String deviceNameRequirements) { this.deviceNameRequirements = deviceNameRequirements; }
	public void setMetadataRequirements(final Map<String,String> metadataRequirements) { this.metadataRequirements = metadataRequirements; }
	public void setVersionRequirement(final Integer versionRequirement) { this.versionRequirement = versionRequirement; }
	public void setMinVersionRequirement(final Integer minVersionRequirement) { this.minVersionRequirement = minVersionRequirement; }
	public void setMaxVersionRequirement(final Integer maxVersionRequirement) { this.maxVersionRequirement = maxVersionRequirement; }
	public void setPingProviders(final boolean pingProviders) { this.pingProviders = pingProviders; }

	@Override
	public String toString() {
		return new StringJoiner(", ", SystemQueryFormDTO.class.getSimpleName() + "[", "]")
				.add("systemNameRequirements='" + systemNameRequirements + "'")
				.add("deviceNameRequirements='" + deviceNameRequirements + "'")
				.add("metadataRequirements=" + metadataRequirements)
				.add("versionRequirement=" + versionRequirement)
				.add("minVersionRequirement=" + minVersionRequirement)
				.add("maxVersionRequirement=" + maxVersionRequirement)
				.add("pingProviders=" + pingProviders)
				.toString();
	}
	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------

	private SystemQueryFormDTO(final Builder builder) {
		this.systemNameRequirements = builder.systemNameRequirements;
		this.deviceNameRequirements = builder.deviceNameRequirements;
		this.metadataRequirements = builder.metadataRequirements;
		this.versionRequirement = builder.versionRequirement;
		this.minVersionRequirement = builder.minVersionRequirement;
		this.maxVersionRequirement = builder.maxVersionRequirement;
		this.pingProviders = builder.pingProviders;
	}
	//=================================================================================================
	// nested classes
	//-------------------------------------------------------------------------------------------------
	public static class Builder {


		//=================================================================================================
		// members

		private final String systemNameRequirements;
		private String deviceNameRequirements;
		private Map<String,String> metadataRequirements;
		private Integer versionRequirement; 
		private Integer minVersionRequirement; 
		private Integer maxVersionRequirement; 
		
		private boolean pingProviders = false;

		//=================================================================================================
		// methods
		
		//-------------------------------------------------------------------------------------------------
		public Builder(final String systemNameRequirements) {
			Assert.isTrue(systemNameRequirements != null && !systemNameRequirements.isBlank(), "systemNameRequirements is null or blank.");
			this.systemNameRequirements = systemNameRequirements;
		}


		//-------------------------------------------------------------------------------------------------
		public Builder deviceName(final String deviceNameRequirements) {
			this.deviceNameRequirements = deviceNameRequirements;
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
		public Builder pingProviders(final boolean pingProviders) {
			this.pingProviders = pingProviders;
			return this;
		}
		
		//-------------------------------------------------------------------------------------------------
		public SystemQueryFormDTO build() {
			return new SystemQueryFormDTO(this);
		}
	}
}