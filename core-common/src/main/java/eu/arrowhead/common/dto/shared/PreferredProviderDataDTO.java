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

import com.fasterxml.jackson.annotation.JsonIgnore;

public class PreferredProviderDataDTO implements Serializable {
	
	//=================================================================================================
	// members
	
	private static final long serialVersionUID = -6954138371746479685L;
	
	private SystemRequestDTO providerSystem;
	private CloudRequestDTO providerCloud;
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public SystemRequestDTO getProviderSystem() { return providerSystem; }
	public CloudRequestDTO getProviderCloud() { return providerCloud; }
	
	//-------------------------------------------------------------------------------------------------
	public void setProviderSystem(final SystemRequestDTO providerSystem) { this.providerSystem = providerSystem; }
	public void setProviderCloud(final CloudRequestDTO providerCloud) { this.providerCloud = providerCloud; }
	
	//-------------------------------------------------------------------------------------------------
	@JsonIgnore
	public boolean isLocal() {
		return providerSystem != null && providerCloud == null;
	}
	
	//-------------------------------------------------------------------------------------------------
	@JsonIgnore
	public boolean isGlobal() {
		return providerCloud != null;
	}
	
	//-------------------------------------------------------------------------------------------------
	@JsonIgnore
	public boolean isValid() {
		return isLocal() || isGlobal();
	}
	
	//-------------------------------------------------------------------------------------------------
	@Override
	public String toString() {
	  return "PreferredProvider{" + "providerSystem=" + providerSystem + ", providerCloud=" + providerCloud + '}';
	}
	
	//-------------------------------------------------------------------------------------------------
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((providerCloud == null) ? 0 : providerCloud.hashCode());
		result = prime * result + ((providerSystem == null) ? 0 : providerSystem.hashCode());
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
		PreferredProviderDataDTO other = (PreferredProviderDataDTO) obj;
		if (providerCloud == null) {
			if (other.providerCloud != null) {
				return false;
			}
		} else if (!providerCloud.equals(other.providerCloud)) {
			return false;
		}
		if (providerSystem == null) {
			if (other.providerSystem != null) {
				return false;
			}
		} else if (!providerSystem.equals(other.providerSystem)) {
			return false;
		}
		return true;
	}	
}
