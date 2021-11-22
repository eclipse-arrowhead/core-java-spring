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
import java.util.Objects;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RelayRequestDTO implements Serializable {

	//=================================================================================================
	// members
	
	private static final long serialVersionUID = 386079402099860014L;
	
	private String address;
	private Integer port;
	private String authenticationInfo;
	private boolean secure = false;
	private boolean exclusive = false;
	private String type;
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public RelayRequestDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public RelayRequestDTO(final String address, final Integer port, final String authenticationInfo, final boolean secure, final boolean exclusive, final String type) {
		this.address = address;
		this.port = port;
		this.authenticationInfo = authenticationInfo;
		this.secure = secure;
		this.exclusive = exclusive;
		this.type = type;
	}

	//-------------------------------------------------------------------------------------------------
	public String getAddress() { return address; }
	public Integer getPort() { return port; }
	public String getAuthenticationInfo() { return authenticationInfo; }
	public boolean isSecure() { return secure; }
	public boolean isExclusive() { return exclusive; }
	public String getType() { return type; }

	//-------------------------------------------------------------------------------------------------
	public void setAddress(final String address) { this.address = address; }
	public void setPort(final Integer port) { this.port = port; }
	public void setAuthenticationInfo(final String authenticationInfo) { this.authenticationInfo = authenticationInfo; }
	public void setSecure(final boolean secure) { this.secure = secure; }
	public void setExclusive(final boolean exclusive) { this.exclusive = exclusive; }
	public void setType(final String type) { this.type = type; }

	//-------------------------------------------------------------------------------------------------
	@Override
	public int hashCode() {
		return Objects.hash(address, authenticationInfo, exclusive, port, secure, type);
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
		
		final RelayRequestDTO other = (RelayRequestDTO) obj;
		
		return Objects.equals(address, other.address) &&
			   Objects.equals(authenticationInfo, other.authenticationInfo) &&
			   exclusive == other.exclusive &&
			   Objects.equals(port, other.port) &&
			   secure == other.secure &&
			   Objects.equals(type, other.type);
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