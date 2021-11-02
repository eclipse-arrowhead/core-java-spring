/********************************************************************************
 * Copyright (c) 2020 AITIA
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

public class ChoreographerPlanRequestDTO implements Serializable {
	
	//=================================================================================================
	// members

	private static final long serialVersionUID = 7757993756490969411L;

	private String name;
	private String firstActionName;
    private List<ChoreographerActionRequestDTO> actions;

    //=================================================================================================
	// methods
    
    //-------------------------------------------------------------------------------------------------
	public ChoreographerPlanRequestDTO() {}

    //-------------------------------------------------------------------------------------------------
    public ChoreographerPlanRequestDTO(final String name, final String firstActionName, final List<ChoreographerActionRequestDTO> actions) {
        this.name = name;
        this.firstActionName = firstActionName;
        this.actions = actions;
    }

    //-------------------------------------------------------------------------------------------------
	public String getName() { return name; }
	public List<ChoreographerActionRequestDTO> getActions() { return actions; }
    public String getFirstActionName() { return firstActionName; }

    //-------------------------------------------------------------------------------------------------
	public void setName(final String name) { this.name = name; }
    public void setActions(final List<ChoreographerActionRequestDTO> actions) { this.actions = actions; }
    public void setFirstActionName(final String firstActionName) { this.firstActionName = firstActionName; }
    
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