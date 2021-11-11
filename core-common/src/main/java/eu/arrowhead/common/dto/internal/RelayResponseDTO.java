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

public class RelayResponseDTO implements Serializable {

	private static final long serialVersionUID = 2230272524199062694L;
	
	private long id;
	private String address;
	private int port;
	private String authenticationInfo;
	private boolean secure = false;
	private boolean exclusive = false;
	private RelayType type;
	private String createdAt;
	private String updatedAt;
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public RelayResponseDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public RelayResponseDTO(final long id, final String address, final int port, final String authenticationInfo, final boolean secure, final boolean exclusive, final RelayType type, final String createdAt, final String updatedAt) {
		this.id = id;
		this.address = address;
		this.port = port;
		this.authenticationInfo = authenticationInfo;
		this.secure = secure;
		this.exclusive = exclusive;
		this.type = type;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	//-------------------------------------------------------------------------------------------------
	public long getId() { return id; }
	public String getAddress() { return address; }
	public int getPort() { return port; }
	public String getAuthenticationInfo() { return authenticationInfo; }
	public boolean isSecure() { return secure; }
	public boolean isExclusive() { return exclusive; }
	public RelayType getType() { return type; }
	public String getCreatedAt() { return createdAt; }
	public String getUpdatedAt() { return updatedAt; }

	//-------------------------------------------------------------------------------------------------
	public void setId(final long id) { this.id = id; }
	public void setAddress(final String address) { this.address = address; }
	public void setPort(final int port) { this.port = port; }
	public void setAuthenticationInfo(final String authenticationInfo) { this.authenticationInfo = authenticationInfo; }
	public void setSecure(final boolean secure) { this.secure = secure; }
	public void setExclusive(final boolean exclusive) { this.exclusive = exclusive; }
	public void setType(final RelayType type) { this.type = type; }
	public void setCreatedAt(final String createdAt) { this.createdAt = createdAt; }
	public void setUpdatedAt(final String updatedAt) { this.updatedAt = updatedAt; }

	//-------------------------------------------------------------------------------------------------
	@Override
	public int hashCode() {
		return Objects.hash(address, authenticationInfo, id, port);
	}

	//-------------------------------------------------------------------------------------------------
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		
		if (obj == null) {
			return false;
		}
		
		if (getClass() != obj.getClass()) {
			return false;
		}
		
		RelayResponseDTO other = (RelayResponseDTO) obj;
		
		return Objects.equals(address, other.address) &&
			   Objects.equals(authenticationInfo, other.authenticationInfo) &&
			   id == other.id &&
			   port == other.port;
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