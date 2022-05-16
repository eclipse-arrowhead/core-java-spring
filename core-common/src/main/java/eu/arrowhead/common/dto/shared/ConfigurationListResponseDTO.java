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
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@JsonInclude(Include.NON_NULL)
public class ConfigurationListResponseDTO implements Serializable {

    //=================================================================================================
	// members

    private static final long serialVersionUID = 2134548237626671292L;

    private long count;
    private List<ConfigurationResponseDTO> data;
    
    
    //=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------	
    public ConfigurationListResponseDTO() {
        count = 0;
        data = new ArrayList<ConfigurationResponseDTO>();
    }

    //-------------------------------------------------------------------------------------------------	
    public ConfigurationListResponseDTO(final long count, final List<ConfigurationResponseDTO> data) {
        this.count = count;
        this.data = data;
    }

    //-------------------------------------------------------------------------------------------------
    public long getCount() { return count; }
    public List<ConfigurationResponseDTO> getData() { return data; }
    
    //-------------------------------------------------------------------------------------------------
    public void setCount(final long count) { this.count = count; }
    public void setData(final List<ConfigurationResponseDTO> data) { this.data = data; }
    
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