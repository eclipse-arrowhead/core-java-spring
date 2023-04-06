/********************************************************************************
 * Copyright (c) 2021 AITIA
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ChoreographerExecutorServiceInfoRequestDTO implements Serializable {
	
	//=================================================================================================
	// members

	private static final long serialVersionUID = 8360159447969430679L;
	
	private String serviceDefinition;
	private Integer minVersion;
	private Integer maxVersion;
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------	
	public ChoreographerExecutorServiceInfoRequestDTO() {}
	
	//-------------------------------------------------------------------------------------------------	
	public ChoreographerExecutorServiceInfoRequestDTO(final String serviceDefinition, final Integer minVersion, final Integer maxVersion) {
		this.serviceDefinition = serviceDefinition;
		this.minVersion = minVersion;
		this.maxVersion = maxVersion;
	}
	
	//-------------------------------------------------------------------------------------------------
	public String getServiceDefinition() { return serviceDefinition; }
	public Integer getMinVersion() { return minVersion; }
	public Integer getMaxVersion() { return maxVersion; }
	
	//-------------------------------------------------------------------------------------------------
	public void setServiceDefinition(final String serviceDefinition) { this.serviceDefinition = serviceDefinition; }
	public void setMinVersion(final Integer minVersion) { this.minVersion = minVersion; }
	public void setMaxVersion(final Integer maxVersion) { this.maxVersion = maxVersion; }
	
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