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

public class AddTrustedKeyResponseDTO implements Serializable {
	
	//=================================================================================================
	// members

	private static final long serialVersionUID = -8357854932110796927L;
	
	private long id;
    private String validAfter;
    private String validBefore;

    //=================================================================================================
	// methods
    
    //-------------------------------------------------------------------------------------------------
	public AddTrustedKeyResponseDTO(final long id) {
        this(id, null, null);
    }

    //-------------------------------------------------------------------------------------------------
	public AddTrustedKeyResponseDTO(final long id, final String validAfter, final String validBefore) {
        this.id = id;
        this.validAfter = validAfter;
        this.validBefore = validBefore;
    }

    //-------------------------------------------------------------------------------------------------
	public String getValidAfter() { return validAfter; }
	public String getValidBefore() { return validBefore; }
	public long getId() { return id; }

    //-------------------------------------------------------------------------------------------------
	public void setValidAfter(final String validAfter) { this.validAfter = validAfter; }
    public void setValidBefore(final String validBefore) { this.validBefore = validBefore; }
    public void setId(final long id) { this.id = id; }
    
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