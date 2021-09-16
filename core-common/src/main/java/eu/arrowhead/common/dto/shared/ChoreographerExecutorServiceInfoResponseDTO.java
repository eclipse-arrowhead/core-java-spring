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
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ChoreographerExecutorServiceInfoResponseDTO implements Serializable {
	
	//=================================================================================================
	// members
	
	private static final long serialVersionUID = 5217709257980451694L;
	
	private String serviceDefinition;
	private int minVersion;
	private int maxVersion;
	private List<ChoreographerServiceQueryFormDTO> dependencies;
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------	
	public ChoreographerExecutorServiceInfoResponseDTO() {}
	
	//-------------------------------------------------------------------------------------------------	
	public ChoreographerExecutorServiceInfoResponseDTO(final String serviceDefinition, final int minVersion, final int maxVersion, final List<ChoreographerServiceQueryFormDTO> dependencies) {
		this.serviceDefinition = serviceDefinition;
		this.minVersion = minVersion;
		this.maxVersion = maxVersion;
		this.dependencies = dependencies;
	}
	
	//-------------------------------------------------------------------------------------------------
	public String getServiceDefinition() { return serviceDefinition; }
	public int getMinVersion() { return minVersion; }
	public int getMaxVersion() { return maxVersion; }
	public List<ChoreographerServiceQueryFormDTO> getDependencies() { return dependencies; }
	
	//-------------------------------------------------------------------------------------------------
	public void setServiceDefinition(final String serviceDefinition) { this.serviceDefinition = serviceDefinition; }
	public void setMinVersion(final int minVersion) { this.minVersion = minVersion; }
	public void setMaxVersion(final int maxVersion) { this.maxVersion = maxVersion; }
	public void setDependencies(final List<ChoreographerServiceQueryFormDTO> dependencies) { this.dependencies = dependencies; }
	
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