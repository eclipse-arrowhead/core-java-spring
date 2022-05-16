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

import javax.validation.constraints.NotBlank;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.Serializable;

public class TrustedKeyCheckRequestDTO implements Serializable {

	//=================================================================================================
	// members

	private static final long serialVersionUID = -3004992813887425759L;
	
	@NotBlank(message = "The publicKey is mandatory")
    private String publicKey;
	
	//=================================================================================================
	// methods

    //-------------------------------------------------------------------------------------------------
	public TrustedKeyCheckRequestDTO() {}

    //-------------------------------------------------------------------------------------------------
	public TrustedKeyCheckRequestDTO(final String publicKey) {
        this.publicKey = publicKey;
    }

    //-------------------------------------------------------------------------------------------------
	public String getPublicKey() { return publicKey; }

    //-------------------------------------------------------------------------------------------------
	public void setPublicKey(final String publicKey) { this.publicKey = publicKey; }
	
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