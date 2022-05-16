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
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CertificateSigningResponseDTO implements Serializable {

    //=================================================================================================
	// members

	private static final long serialVersionUID = 8539314545661981641L;
	private long id;
    private List<String> certificateChain;

    //=================================================================================================
	// methods
    
    //-------------------------------------------------------------------------------------------------
	public CertificateSigningResponseDTO() {
        this.id = 0;
    }

    //-------------------------------------------------------------------------------------------------
	public CertificateSigningResponseDTO(final long id) {
        this.id = id;
    }

    //-------------------------------------------------------------------------------------------------
	public CertificateSigningResponseDTO(final long id, final List<String> certificateChain) {
        this.id = id;
        this.certificateChain = certificateChain;
    }

    //-------------------------------------------------------------------------------------------------
	public List<String> getCertificateChain() { return certificateChain; }
	public long getId() { return id; }

    //-------------------------------------------------------------------------------------------------
	public void setCertificateChain(final List<String> certificateChain) { this.certificateChain = certificateChain; }
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