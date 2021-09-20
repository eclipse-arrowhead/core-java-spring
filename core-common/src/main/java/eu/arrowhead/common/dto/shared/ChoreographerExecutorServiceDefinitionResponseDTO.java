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

public class ChoreographerExecutorServiceDefinitionResponseDTO implements Serializable {

	//=================================================================================================
    // members
    
	private static final long serialVersionUID = 739704041157654370L;
	
    private long id;
    private long executorId;
    private String serviceDefinitionName;
    private int minVersion;
    private int maxVersion;
    private String createdAt;
    private String updatedAt;

    //=================================================================================================
    // methods

    //-------------------------------------------------------------------------------------------------
    public ChoreographerExecutorServiceDefinitionResponseDTO() { }

    //-------------------------------------------------------------------------------------------------

    public ChoreographerExecutorServiceDefinitionResponseDTO(final long id, final long executorId, final String serviceDefinitionName, final int minVersion, final int maxVersion,
    														 final String createdAt, final String updatedAt) {
        this.id = id;
        this.executorId = executorId;
        this.serviceDefinitionName = serviceDefinitionName;
        this.minVersion = minVersion;
        this.maxVersion = maxVersion;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }


    //-------------------------------------------------------------------------------------------------
    public long getId() { return id; }    
    public long getExecutorId() { return executorId; }
	public String getServiceDefinitionName() { return serviceDefinitionName; }   
    public int getMinVersion() { return minVersion; }
	public int getMaxVersion() { return maxVersion; }
	public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }

    //-------------------------------------------------------------------------------------------------
    public void setId(final long id) { this.id = id; }
    public void setExecutorId(final long executorId) { this.executorId = executorId; }
	public void setServiceDefinitionName(final String serviceDefinitionName) { this.serviceDefinitionName = serviceDefinitionName; }    
    public void setMinVersion(final int minVersion) { this.minVersion = minVersion; }
	public void setMaxVersion(final int maxVersion) { this.maxVersion = maxVersion; }
	public void setCreatedAt(final String createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(final String updatedAt) { this.updatedAt = updatedAt; }

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