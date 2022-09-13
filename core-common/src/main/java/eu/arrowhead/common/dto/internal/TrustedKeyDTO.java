/********************************************************************************
 * Copyright (c) 2020 Evopro
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Evopro - implementation
 *   Arrowhead Consortia - conceptualization
 ********************************************************************************/

package eu.arrowhead.common.dto.internal;

import java.io.Serializable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TrustedKeyDTO implements Serializable {

	//=================================================================================================
	// members

	private static final long serialVersionUID = -4534679840370918655L;
		
	private long id;
    private String createdAt;
    private String description;
    
    //=================================================================================================
	// methods

    //-------------------------------------------------------------------------------------------------
	public TrustedKeyDTO() {}

    //-------------------------------------------------------------------------------------------------
	public TrustedKeyDTO(final long id, final String createdAt, final String description) { 
        this.id = id;
        this.createdAt = createdAt;
        this.description = description;
    }

    //-------------------------------------------------------------------------------------------------
	public String getCreatedAt() { return createdAt; }
	public long getId() { return id; }
	public String getDescription() { return description; }

    //-------------------------------------------------------------------------------------------------
	public void setCreatedAt(final String createdAt) { this.createdAt = createdAt; }
    public void setId(final long id) { this.id = id; }
    public void setDescription(final String description) { this.description = description; }
    
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