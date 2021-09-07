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
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TrustedKeysResponseDTO implements Serializable {

	//=================================================================================================
	// members
	
	private static final long serialVersionUID = -460285598669355060L;
	
	private long count;
    private List<TrustedKeyDTO> trustedKeys;

    //=================================================================================================
	// methods
    
    //-------------------------------------------------------------------------------------------------
	public TrustedKeysResponseDTO(final List<TrustedKeyDTO> trustedKeyDTOs, final int count) {
        this.count = count;
        setTrustedKeys(trustedKeyDTOs);
    }

    //-------------------------------------------------------------------------------------------------
	public TrustedKeysResponseDTO() {
        setTrustedKeys(new ArrayList<>());
    }

    //-------------------------------------------------------------------------------------------------
	public List<TrustedKeyDTO> getTrustedKeys() { return trustedKeys; }
	public long getCount() { return count; }

    //-------------------------------------------------------------------------------------------------
	public void setTrustedKeys(final List<TrustedKeyDTO> trustedKeys) { this.trustedKeys = trustedKeys; }
    public void setCount(final long count) { this.count = count; }
    
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