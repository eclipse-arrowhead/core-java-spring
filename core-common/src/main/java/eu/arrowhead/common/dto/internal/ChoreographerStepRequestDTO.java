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

package eu.arrowhead.common.dto.internal;

import eu.arrowhead.common.dto.shared.ChoreographerOFRRequestDTO;

import java.io.Serializable;
import java.util.List;

public class ChoreographerStepRequestDTO implements Serializable {
	
	//=================================================================================================
	// members

	private static final long serialVersionUID = -8100852327039160839L;

	private String name;
    private String metadata;
    private String parameters;
    private int quantity;
    private ChoreographerOFRRequestDTO usedService;
    private List<ChoreographerOFRRequestDTO> preconditions;
    private List<String> nextStepNames;

    //=================================================================================================
	// methods

    //-------------------------------------------------------------------------------------------------
	public ChoreographerStepRequestDTO() {}

    //-------------------------------------------------------------------------------------------------
	public ChoreographerStepRequestDTO(final String name, final String serviceName, final List<String> nextStepNames, final int quantity) {
        this(name, null, null, nextStepNames, quantity);
    }

    //-------------------------------------------------------------------------------------------------
    public ChoreographerStepRequestDTO(final String name, final String metadata, final String parameters, final List<String> nextStepNames, final int quantity) {
        this.name = name;
        this.metadata = metadata;
        this.parameters = parameters;
        this.nextStepNames = nextStepNames;
        this.quantity = quantity;
    }

    //-------------------------------------------------------------------------------------------------
    public ChoreographerStepRequestDTO(String name, String metadata, String parameters, int quantity, ChoreographerOFRRequestDTO usedService, List<ChoreographerOFRRequestDTO> preconditions, List<String> nextStepNames) {
        this.name = name;
        this.metadata = metadata;
        this.parameters = parameters;
        this.quantity = quantity;
        this.usedService = usedService;
        this.preconditions = preconditions;
        this.nextStepNames = nextStepNames;
    }

    //-------------------------------------------------------------------------------------------------
	public String getName() { return name; }
	public List<String> getNextStepNames() { return nextStepNames; }
    public String getMetadata() { return metadata; }
    public String getParameters() { return parameters; }
    public int getQuantity() { return quantity; }
    public ChoreographerOFRRequestDTO getUsedService() { return usedService; }
    public List<ChoreographerOFRRequestDTO> getPreconditions() { return preconditions; }

    //-------------------------------------------------------------------------------------------------
	public void setName(final String name) { this.name = name; }
    public void setNextStepNames(final List<String> nextStepNames) { this.nextStepNames = nextStepNames; }
    public void setMetadata(String metadata) { this.metadata = metadata; }
    public void setParameters(String parameters) { this.parameters = parameters; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setUsedService(ChoreographerOFRRequestDTO usedService) { this.usedService = usedService; }
    public void setPreconditions(List<ChoreographerOFRRequestDTO> preconditions) { this.preconditions = preconditions; }
}