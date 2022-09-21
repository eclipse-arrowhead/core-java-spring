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

package eu.arrowhead.common.dto.internal;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.arrowhead.common.dto.shared.ChoreographerExecutorResponseDTO;

import java.io.Serializable;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChoreographerExecutorListResponseDTO implements Serializable {

    //=================================================================================================
    // members

    private static final long serialVersionUID = -2899097877922460370L;

    private List<ChoreographerExecutorResponseDTO> data;
    private long count;

    //=================================================================================================
    // methods

    //-------------------------------------------------------------------------------------------------
    public ChoreographerExecutorListResponseDTO() {}

    //-------------------------------------------------------------------------------------------------
    public ChoreographerExecutorListResponseDTO(final List<ChoreographerExecutorResponseDTO> data, final long count) {
        this.data = data;
        this.count = count;
    }

    //-------------------------------------------------------------------------------------------------
    public List<ChoreographerExecutorResponseDTO> getData() { return data; }
    public long getCount() { return count; }

    //-------------------------------------------------------------------------------------------------
    public void setData(final List<ChoreographerExecutorResponseDTO> data) { this.data = data; }
    public void setCount(final long count) { this.count = count; }
    
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