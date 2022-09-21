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

public class IssuedCertificatesResponseDTO implements Serializable {

	//=================================================================================================
	// members
	
	private static final long serialVersionUID = 4396879474104394462L;
	
	private long count;
    private List<IssuedCertificateDTO> issuedCertificates;

    //=================================================================================================
	// methods
    
    //-------------------------------------------------------------------------------------------------
	public IssuedCertificatesResponseDTO() {
        setIssuedCertificates(new ArrayList<>());
    }

    //-------------------------------------------------------------------------------------------------
	public IssuedCertificatesResponseDTO(final List<IssuedCertificateDTO> certificates, final long count) {
        setIssuedCertificates(certificates);
        this.count = count;
    }

    //-------------------------------------------------------------------------------------------------
	public List<IssuedCertificateDTO> getIssuedCertificates() { return issuedCertificates; }
	public long getCount() { return count; }

    //-------------------------------------------------------------------------------------------------
	public void setIssuedCertificates(final List<IssuedCertificateDTO> issuedCertificates) { this.issuedCertificates = issuedCertificates; }
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