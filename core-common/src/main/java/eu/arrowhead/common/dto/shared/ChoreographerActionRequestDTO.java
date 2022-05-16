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

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ChoreographerActionRequestDTO implements Serializable {
	
	//=================================================================================================
	// members

	private static final long serialVersionUID = 305775285238451287L;

	private String name;
    private String nextActionName;
    private List<String> firstStepNames;
    private List<ChoreographerStepRequestDTO> steps;

    //=================================================================================================
	// methods
    
    //-------------------------------------------------------------------------------------------------
    public ChoreographerActionRequestDTO() {}

    //-------------------------------------------------------------------------------------------------
    public ChoreographerActionRequestDTO(final String name, final String nextActionName, final List<String> firstStepNames, final List<ChoreographerStepRequestDTO> steps) {
        this.name = name;
        this.nextActionName = nextActionName;
        this.firstStepNames = firstStepNames;
        this.steps = steps;
    }

    //-------------------------------------------------------------------------------------------------
	public String getName() { return name; }
	public String getNextActionName() { return nextActionName; }
	public List<ChoreographerStepRequestDTO> getSteps() { return steps; }
    public List<String> getFirstStepNames() { return firstStepNames; }

    //-------------------------------------------------------------------------------------------------
	public void setName(final String name) { this.name = name; }
    public void setNextActionName(final String nextActionName) { this.nextActionName = nextActionName; }
    public void setSteps(final List<ChoreographerStepRequestDTO> steps) { this.steps = steps; }
    public void setFirstStepNames(final List<String> firstStepNames) { this.firstStepNames = firstStepNames; }
    
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