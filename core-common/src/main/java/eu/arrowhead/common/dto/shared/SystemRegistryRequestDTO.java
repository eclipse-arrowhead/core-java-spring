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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@JsonInclude(Include.NON_NULL)
public class SystemRegistryRequestDTO implements Serializable {
	
	//=================================================================================================
	// members

	private static final long serialVersionUID = -635438605292398404L;
	
	private SystemRequestDTO system;
	private DeviceRequestDTO provider;
	private String endOfValidity;
	private Map<String,String> metadata;
	private Integer version;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public SystemRegistryRequestDTO() {}

	//-------------------------------------------------------------------------------------------------
	public SystemRegistryRequestDTO(final SystemRequestDTO system, final DeviceRequestDTO provider, final String endOfValidity) {
		this.system = system;
		this.provider = provider;
		this.endOfValidity = endOfValidity;
	}

	//-------------------------------------------------------------------------------------------------
	public SystemRegistryRequestDTO(final SystemRequestDTO system, final DeviceRequestDTO provider, final String endOfValidity,
									final Map<String, String> metadata, final Integer version) {
		this.system = system;
		this.provider = provider;
		this.endOfValidity = endOfValidity;
		this.metadata = metadata;
		this.version = version;
	}

	//-------------------------------------------------------------------------------------------------
	public SystemRequestDTO getSystem() { return system; }
	public DeviceRequestDTO getProvider() { return provider; }
	public String getEndOfValidity() { return endOfValidity; }
	public Map<String,String> getMetadata() { return metadata; }
	public Integer getVersion() { return version; }

	//-------------------------------------------------------------------------------------------------
	public void setSystem(final SystemRequestDTO system) { this.system = system; }
	public void setProvider(final DeviceRequestDTO provider) { this.provider = provider; }
	public void setEndOfValidity(final String endOfValidity) { this.endOfValidity = endOfValidity; }
	public void setMetadata(final Map<String,String> metadata) { this.metadata = metadata; }
	public void setVersion(final Integer version) { this.version = version; }

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