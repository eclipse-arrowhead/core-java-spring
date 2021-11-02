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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChoreographerStepResponseDTO implements Serializable {

	//=================================================================================================
	// members
	
	private static final long serialVersionUID = 1643826656433118530L;
	
	private long id;
    private String name;
    private String serviceDefinition;
    private Integer minVersion;
    private Integer maxVersion;
    private Map<String,String> staticParameters;
    private int quantity;
    private ChoreographerServiceQueryFormDTO srTemplate;
    private List<String> nextStepNames;
    private String createdAt;
    private String updatedAt;

    //=================================================================================================
    // methods
	
    //-------------------------------------------------------------------------------------------------
	public ChoreographerStepResponseDTO() {}

    //-------------------------------------------------------------------------------------------------
    public ChoreographerStepResponseDTO(final long id, final String name, final String serviceDefinition, final Integer minVersion, final Integer maxVersion, final Map<String, String> staticParameters, final int quantity,
    									final ChoreographerServiceQueryFormDTO srTemplate, final List<String> nextStepNames, final String createdAt, final String updatedAt) {
        this.id = id;
        this.name = name;
        this.serviceDefinition = serviceDefinition;
        this.minVersion = minVersion;
        this.maxVersion = maxVersion;
        this.staticParameters = staticParameters;
        this.quantity = quantity;
        this.srTemplate = srTemplate;
        this.nextStepNames = nextStepNames;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    //-------------------------------------------------------------------------------------------------
	public long getId() { return id; }
	public String getName() { return name; }
	public String getServiceDefinition() { return serviceDefinition; }
	public Integer getMinVersion() { return minVersion; }
	public Integer getMaxVersion() { return maxVersion; }
	public Map<String,String> getStaticParameters() { return staticParameters; }
    public int getQuantity() { return quantity; }
    public ChoreographerServiceQueryFormDTO getSrTemplate() { return srTemplate; }
    public List<String> getNextStepNames() { return nextStepNames; }
    public String getCreatedAt() { return createdAt; }
	public String getUpdatedAt() { return updatedAt; }

	//-------------------------------------------------------------------------------------------------
	public void setId(final long id) { this.id = id; }
    public void setName(final String name) { this.name = name; }
    public void setServiceDefinition(final String serviceDefinition) { this.serviceDefinition = serviceDefinition; }
    public void setMinVersion(final Integer minVersion) { this.minVersion = minVersion; }
    public void setMaxVersion(final Integer maxVersion) { this.maxVersion = maxVersion; }
    public void setSrTemplate(final ChoreographerServiceQueryFormDTO srTemplate) { this.srTemplate = srTemplate; }
    public void setNextStepNames(final List<String> nextStepNames) { this.nextStepNames = nextStepNames; }
    public void setStaticParameters(final Map<String,String> staticParameters) { this.staticParameters = staticParameters; }
    public void setQuantity(final int quantity) { this.quantity = quantity; }
    public void setCreatedAt(final String createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(final String updatedAt) { this.updatedAt = updatedAt; }
    
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