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

import javax.validation.constraints.NotBlank;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CertificateCheckRequestDTO implements Serializable {

	//=================================================================================================
	// members
	
	private static final long serialVersionUID = 3925861444397769287L;

	private int version;

    @NotBlank(message = "The certificate is mandatory")
    private String certificate;

    //=================================================================================================
	// methods
    
    //-------------------------------------------------------------------------------------------------
	public CertificateCheckRequestDTO() {}

    //-------------------------------------------------------------------------------------------------
	public CertificateCheckRequestDTO(final int version, final String certificate) {
        this.version = version;
        this.certificate = certificate;
    }

    //-------------------------------------------------------------------------------------------------
	public int getVersion() { return version; }
	public String getCertificate() { return certificate; }
	
    //-------------------------------------------------------------------------------------------------
	public void setVersion(final int version) { this.version = version; }
    public void setCertificate(final String certificate) { this.certificate = certificate; }
    
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