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

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ChoreographerSuitableExecutorResponseDTO {

    //=================================================================================================
    // members

    private List<Long> suitableExecutorIds = new ArrayList<>();

    //=================================================================================================
    // methods

    //-------------------------------------------------------------------------------------------------
    public ChoreographerSuitableExecutorResponseDTO() {}

    //-------------------------------------------------------------------------------------------------
    public ChoreographerSuitableExecutorResponseDTO(final List<Long> suitableExecutorIds) {
        this.suitableExecutorIds = suitableExecutorIds;
    }

    //-------------------------------------------------------------------------------------------------
    public List<Long> getSuitableExecutorIds() { return suitableExecutorIds; }

    //-------------------------------------------------------------------------------------------------
    public void setSuitableExecutorIds(final List<Long> suitableExecutorIds) { this.suitableExecutorIds = suitableExecutorIds; }
    
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