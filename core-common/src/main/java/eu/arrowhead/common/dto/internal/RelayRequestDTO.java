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

public class RelayRequestDTO implements Serializable {

	//=================================================================================================
	// members
	
	private static final long serialVersionUID = -3457288931415932626L;

	private String address;
	private Integer port;
	private boolean secure = false;
	private boolean exclusive = false;
	private String type;
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public RelayRequestDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public RelayRequestDTO(final String address, final Integer port, final boolean secure, final boolean exclusive, final String type) {
		this.address = address;
		this.port = port;
		this.secure = secure;
		this.exclusive = exclusive;
		this.type = type;
	}

	//-------------------------------------------------------------------------------------------------
	public String getAddress() { return address; }
	public Integer getPort() { return port; }
	public boolean isSecure() { return secure; }
	public boolean isExclusive() { return exclusive; }
	public String getType() { return type; }

	//-------------------------------------------------------------------------------------------------
	public void setAddress(final String address) { this.address = address; }
	public void setPort(final Integer port) { this.port = port; }
	public void setSecure(final boolean secure) { this.secure = secure; }
	public void setExclusive(final boolean exclusive) { this.exclusive = exclusive; }
	public void setType(final String type) { this.type = type; }

	//-------------------------------------------------------------------------------------------------
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((address == null) ? 0 : address.hashCode());
		result = prime * result + (exclusive ? 1231 : 1237);
		result = prime * result + ((port == null) ? 0 : port.hashCode());
		result = prime * result + (secure ? 1231 : 1237);
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
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
		RelayRequestDTO other = (RelayRequestDTO) obj;
		if (address == null) {
			if (other.address != null) {
				return false;
			}
		} else if (!address.equals(other.address)) {
			return false;
		}
		if (exclusive != other.exclusive) {
			return false;
		}
		if (port == null) {
			if (other.port != null) {
				return false;
			}
		} else if (!port.equals(other.port)) {
			return false;
		}
		if (secure != other.secure) {
			return false;
		}
		if (type == null) {
			if (other.type != null) {
				return false;
			}
		} else if (!type.equals(other.type)) {
			return false;
		}
		return true;
	}		
}