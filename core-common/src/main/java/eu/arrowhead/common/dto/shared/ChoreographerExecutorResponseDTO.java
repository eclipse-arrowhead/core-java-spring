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

package eu.arrowhead.common.dto.shared;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class  ChoreographerExecutorResponseDTO implements Serializable {

	//=================================================================================================
    // members
    
	private static final long serialVersionUID = -8660896921520588874L;

    private long id;
    private String name;
    private String address;
    private int port;
    private String baseUri;
    private List<ChoreographerExecutorServiceDefinitionResponseDTO> serviceDefinitions;
    private String createdAt;
    private String updatedAt;

    //=================================================================================================
    // methods

    //-------------------------------------------------------------------------------------------------
    public ChoreographerExecutorResponseDTO() {}
    
    //-------------------------------------------------------------------------------------------------
    public ChoreographerExecutorResponseDTO(final long id, final String name, final String address, final int port, final String baseUri,
    										final List<ChoreographerExecutorServiceDefinitionResponseDTO> serviceDefinitions,
    										final String createdAt, final String updatedAt) {
		this.id = id;
		this.name = name;
		this.address = address;
		this.port = port;
		this.baseUri = baseUri;
		this.serviceDefinitions = serviceDefinitions;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

    //-------------------------------------------------------------------------------------------------
    public long getId() { return id; }    
	public String getName() { return name; }
	public String getAddress() { return address; }
	public int getPort() { return port; }
	public String getBaseUri() { return baseUri; }
    public List<ChoreographerExecutorServiceDefinitionResponseDTO> getServiceDefinitions() { return serviceDefinitions; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }

    //-------------------------------------------------------------------------------------------------
    public void setId(final long id) { this.id = id; }    
	public void setName(final String name) { this.name = name; }
	public void setAddress(final String address) { this.address = address; }
	public void setPort(final int port) { this.port = port; }
	public void setBaseUri(final String baseUri) { this.baseUri = baseUri; }
    public void setServiceDefinitions(final List<ChoreographerExecutorServiceDefinitionResponseDTO> serviceDefinitions) { this.serviceDefinitions = serviceDefinitions; }
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