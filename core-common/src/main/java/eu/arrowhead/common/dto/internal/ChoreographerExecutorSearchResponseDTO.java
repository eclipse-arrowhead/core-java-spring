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

import eu.arrowhead.common.dto.shared.ChoreographerExecutorResponseDTO;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ChoreographerExecutorSearchResponseDTO implements Serializable {

    //=================================================================================================
    // members

    private static final long serialVersionUID = 3067385511834702704L;

    private List<ChoreographerExecutorResponseDTO> data;

    //=================================================================================================
    // methods

    //-------------------------------------------------------------------------------------------------
    public ChoreographerExecutorSearchResponseDTO() {}

    //-------------------------------------------------------------------------------------------------
    public ChoreographerExecutorSearchResponseDTO(final List<ChoreographerExecutorResponseDTO> data) {
        this.data = data;
    }

    //-------------------------------------------------------------------------------------------------
    public List<ChoreographerExecutorResponseDTO> getData() { return data; }

    //-------------------------------------------------------------------------------------------------
    public void setData(final List<ChoreographerExecutorResponseDTO> data) { this.data = data; }
    
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