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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.Serializable;
import java.util.List;

public class ChoreographerStepListResponseDTO implements Serializable {
	
	//=================================================================================================
	// members
	
	private static final long serialVersionUID = -4221321354484821514L;

	private List<ChoreographerStepResponseDTO> data;
    private long count;

    //=================================================================================================
    // methods
	
    //-------------------------------------------------------------------------------------------------
	public ChoreographerStepListResponseDTO() {}

    //-------------------------------------------------------------------------------------------------
	public ChoreographerStepListResponseDTO(final List<ChoreographerStepResponseDTO> data, final long count) {
        this.data = data;
        this.count = count;
    }

    //-------------------------------------------------------------------------------------------------
	public List<ChoreographerStepResponseDTO> getData() { return data; }
	public long getCount() { return count; }

    //-------------------------------------------------------------------------------------------------
	public void setData(final List<ChoreographerStepResponseDTO> data) { this.data = data; }
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