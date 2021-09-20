/********************************************************************************
 * Copyright (c) 2021 {Lulea University of Technology}
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   {Lulea University of Technology} - implementation
 *   Arrowhead Consortia - conceptualization
 ********************************************************************************/

package eu.arrowhead.common.dto.shared;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@JsonInclude(Include.NON_NULL)
public class ConfigurationResponseDTO implements Serializable {

    //=================================================================================================
	// members

    private static final long serialVersionUID = 1231548247623961591L;

    private long id;
    private String systemName;
    private String fileName;
    private String contentType;
    private String data;
    private String createdAt;
    private String updatedAt;
    
    //=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------	
    public ConfigurationResponseDTO() {}

    //-------------------------------------------------------------------------------------------------
    public ConfigurationResponseDTO(final long id, final String systemName, final String fileName, final String contentType, final String data, final String createdAt, final String updatedAt) {
        this.id = id;
        this.systemName = systemName;
        this.fileName = fileName;
        this.contentType = contentType;
        this.data = data;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    //-------------------------------------------------------------------------------------------------
    public long getId() { return id; }
    public String getSystemName() { return systemName; }
    public String getFileName() { return fileName; }
    public String getContentType() { return contentType; }
    public String getData() { return data; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }
    
    //-------------------------------------------------------------------------------------------------
    public void setId(final long id) { this.id = id; }
    public void setSystemName(final String systemName) { this.systemName = systemName; }
    public void setFileName(final String fileName) { this.fileName = fileName; }
    public void setContentType(final String contentType) { this.contentType = contentType; }
    public void setData(final String data) { this.data = data; }
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