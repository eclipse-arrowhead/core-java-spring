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

public class CertificateSigningRequestDTO implements Serializable {

	//=================================================================================================
	// members

	private static final long serialVersionUID = -1496033564429311716L;
	
	@NotBlank(message = "The encodedCSR is mandatory")
    private String encodedCSR;
    private String validAfter;
    private String validBefore;

    //=================================================================================================
	// methods
    
    //-------------------------------------------------------------------------------------------------
	public CertificateSigningRequestDTO() {}

    //-------------------------------------------------------------------------------------------------
	public CertificateSigningRequestDTO(final String encodedCSR) {
        this.encodedCSR = encodedCSR;
    }

    //-------------------------------------------------------------------------------------------------
	public CertificateSigningRequestDTO(final String encodedCSR, final String validAfter, final String validBefore) {
        this.encodedCSR = encodedCSR;
        this.validAfter = validAfter;
        this.validBefore = validBefore;
    }

    //-------------------------------------------------------------------------------------------------
	public String getEncodedCSR() { return encodedCSR; }
	public String getValidAfter() { return validAfter; }
	public String getValidBefore() { return validBefore; }

    //-------------------------------------------------------------------------------------------------
	public void setEncodedCSR(final String encodedCSR) { this.encodedCSR = encodedCSR; }
    public void setValidAfter(final String validAfter) { this.validAfter = validAfter; }
    public void setValidBefore(final String validBefore) { this.validBefore = validBefore; }
    
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