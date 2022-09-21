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

public class ChoreographerExecutorRequestDTO implements Serializable {

	//=================================================================================================
    // members

	private static final long serialVersionUID = 519659101385691422L;
	
	private SystemRequestDTO system;
    private String baseUri;
    private String serviceDefinitionName;
    private Integer minVersion;
    private Integer maxVersion;

    //=================================================================================================
    // methods

    //-------------------------------------------------------------------------------------------------
	public SystemRequestDTO getSystem() { return system; }
	public String getBaseUri() { return baseUri; }
	public String getServiceDefinitionName() { return serviceDefinitionName; }
	public Integer getMinVersion() { return minVersion; }
	public Integer getMaxVersion() { return maxVersion; }

	//-------------------------------------------------------------------------------------------------
	public void setSystem(final SystemRequestDTO system) { this.system = system; }
	public void setBaseUri(final String baseUri) { this.baseUri = baseUri; }
	public void setServiceDefinitionName(final String serviceDefinitionName) { this.serviceDefinitionName = serviceDefinitionName; }
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