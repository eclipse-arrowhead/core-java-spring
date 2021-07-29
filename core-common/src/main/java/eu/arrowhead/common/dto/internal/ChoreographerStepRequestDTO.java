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

import java.io.Serializable;
import java.util.List;

public class ChoreographerStepRequestDTO implements Serializable {
	
	//=================================================================================================
	// members

	private static final long serialVersionUID = -8100852327039160839L;

	private String name;
    private String serviceName;
    private String metadata;
    private String parameters;
    private int quantity;
    private List<String> nextStepNames;

    //=================================================================================================
	// methods
    
    //-------------------------------------------------------------------------------------------------
	public ChoreographerStepRequestDTO() {}

    //-------------------------------------------------------------------------------------------------
	public ChoreographerStepRequestDTO(final String name, final String serviceName, final List<String> nextStepNames, final int quantity) {
        this(name, serviceName, null, null, nextStepNames, quantity);
    }

    //-------------------------------------------------------------------------------------------------
    public ChoreographerStepRequestDTO(final String name, final String serviceName, final String metadata, final String parameters, final List<String> nextStepNames, final int quantity) {
        this.name = name;
        this.serviceName = serviceName;
        this.metadata = metadata;
        this.parameters = parameters;
        this.nextStepNames = nextStepNames;
        this.quantity = quantity;
    }

    //-------------------------------------------------------------------------------------------------
	public String getName() { return name; }
	public String getServiceName() { return serviceName; }
	public List<String> getNextStepNames() { return nextStepNames; }
    public String getMetadata() { return metadata; }
    public String getParameters() { return parameters; }
    public int getQuantity() { return quantity; }

    //-------------------------------------------------------------------------------------------------
	public void setName(final String name) { this.name = name; }
    public void setServiceName(final String serviceName) { this.serviceName = serviceName; }
    public void setNextStepNames(final List<String> nextStepNames) { this.nextStepNames = nextStepNames; }
    public void setMetadata(String metadata) { this.metadata = metadata; }
    public void setParameters(String parameters) { this.parameters = parameters; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}