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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.Serializable;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChoreographerActionResponseDTO implements Serializable {

	//=================================================================================================
	// members
	
	private static final long serialVersionUID = -3263344826750594965L;
	
	private long id;
    private String name;
    private boolean firstAction;
    private String nextActionName;
    private List<ChoreographerStepResponseDTO> steps;
    private List<String> firstStepNames;
    private String createdAt;
    private String updatedAt;

    //=================================================================================================
	// methods
    
    //-------------------------------------------------------------------------------------------------
	public ChoreographerActionResponseDTO() {}

    //-------------------------------------------------------------------------------------------------
	public ChoreographerActionResponseDTO(final long id, final String name, final boolean firstAction, final String nextActionName, final List<ChoreographerStepResponseDTO> steps, final List<String> firstStepNames, final String createdAt,
                                          final String updatedAt) {
        this.id = id;
        this.name = name;
        this.firstAction = firstAction;
        this.nextActionName = nextActionName;
        this.steps = steps;
        this.firstStepNames = firstStepNames;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    //-------------------------------------------------------------------------------------------------
	public long getId() { return id; }
	public String getName() { return name; }
	public boolean isFirstAction() { return firstAction; }
	public String getNextActionName() { return nextActionName; }
	public List<ChoreographerStepResponseDTO> getSteps() { return steps; }
    public List<String> getFirstStepNames() { return firstStepNames; }
    public String getCreatedAt() { return createdAt; }
	public String getUpdatedAt() { return updatedAt; }

    //-------------------------------------------------------------------------------------------------
	public void setId(final long id) { this.id = id; }
    public void setName(final String name) { this.name = name; }
    public void setNextActionName(final String nextActionName) { this.nextActionName = nextActionName; }
    public void setSteps(final List<ChoreographerStepResponseDTO> steps) { this.steps = steps; }
    public void setFirstStepNames(final List<String> firstStepNames) { this.firstStepNames = firstStepNames; }
    public void setCreatedAt(final String createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(final String updatedAt) { this.updatedAt = updatedAt; }
	public void setFirstAction(final boolean firstAction) { this.firstAction = firstAction; }
	
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