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

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ChoreographerStepResponseDTO implements Serializable {

	//=================================================================================================
	// members
	
	private static final long serialVersionUID = 3665162578177568728L;

	private long id;
    private String name;
    private String serviceName;
    private String metadata;
    private String parameters;
    private int quantity;
    private List<ChoreographerNextStepResponseDTO> nextSteps;
    private String createdAt;
    private String updatedAt;

    //=================================================================================================
    // methods
	
    //-------------------------------------------------------------------------------------------------
	public ChoreographerStepResponseDTO() {}

    //-------------------------------------------------------------------------------------------------
    public ChoreographerStepResponseDTO(final long id, final String name, final String serviceName, final String metadata, final String parameters,
                                        final List<ChoreographerNextStepResponseDTO> nextSteps, final int quantity, final String createdAt, final String updatedAt) {
        this.id = id;
        this.name = name;
        this.serviceName = serviceName;
        this.metadata = metadata;
        this.parameters = parameters;
        this.nextSteps = nextSteps;
        this.quantity = quantity;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    //-------------------------------------------------------------------------------------------------
	public long getId() { return id; }
	public String getName() { return name; }
	public String getServiceName() { return serviceName; }
    public String getMetadata() { return metadata; }
    public String getParameters() { return parameters; }
    public List<ChoreographerNextStepResponseDTO> getNextSteps() { return nextSteps; }
    public int getQuantity() { return quantity; }
    public String getCreatedAt() { return createdAt; }
	public String getUpdatedAt() { return updatedAt; }

    //-------------------------------------------------------------------------------------------------
	public void setId(final long id) { this.id = id; }
    public void setName(final String name) { this.name = name; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }
    public void setMetadata(String metadata) { this.metadata = metadata; }
    public void setParameters(String parameters) { this.parameters = parameters; }
    public void setNextSteps(final List<ChoreographerNextStepResponseDTO> nextSteps) { this.nextSteps = nextSteps; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setCreatedAt(final String createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(final String updatedAt) { this.updatedAt = updatedAt; }
}