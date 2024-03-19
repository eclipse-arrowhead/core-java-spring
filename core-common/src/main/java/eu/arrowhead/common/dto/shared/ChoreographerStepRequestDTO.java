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
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.arrowhead.common.dto.internal.ChoreographerSessionStepStartCondition;

public class ChoreographerStepRequestDTO implements Serializable {
	
	//=================================================================================================
	// members

	private static final long serialVersionUID = 3848344337979806809L;
	
	private String name;
    private Map<String,String> staticParameters;
    private Integer quantity;
    private ChoreographerServiceQueryFormDTO serviceRequirement;
    private List<String> nextStepNames;

    private ChoreographerSessionStepStartCondition startCondition;
    private String path;
    private String threshold;

    //=================================================================================================
	// methods

    //-------------------------------------------------------------------------------------------------
	public String getName() { return name; }
	public Map<String,String> getStaticParameters() { return staticParameters; }
	public Integer getQuantity() { return quantity; }
	public ChoreographerServiceQueryFormDTO getServiceRequirement() { return serviceRequirement; }
	public List<String> getNextStepNames() { return nextStepNames; }

    public ChoreographerSessionStepStartCondition getStartCondition() {return this.startCondition;}
    public String getThreshold() {return this.threshold;}
    public String getPath() {return this.path;}

    //-------------------------------------------------------------------------------------------------
	public void setName(final String name) { this.name = name; }
	public void setStaticParameters(final Map<String,String> staticParameters) { this.staticParameters = staticParameters; }
	public void setQuantity(final Integer quantity) { this.quantity = quantity; }
	public void setServiceRequirement(final ChoreographerServiceQueryFormDTO serviceRequirement) { this.serviceRequirement = serviceRequirement; }
    public void setNextStepNames(final List<String> nextStepNames) { this.nextStepNames = nextStepNames; }

    public void setStartCondition(final ChoreographerSessionStepStartCondition startCondition) {this.startCondition = startCondition;}
    public void setThreshold(final String threshold) { this.threshold = threshold;}
    public void setPath(final String path) { this.path = path;}
    
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