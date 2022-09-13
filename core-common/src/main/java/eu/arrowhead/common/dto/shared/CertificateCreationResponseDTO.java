/********************************************************************************
 * Copyright (c) 2020 FHB
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   FHB - implementation
 *   Arrowhead Consortia - conceptualization
 ********************************************************************************/

package eu.arrowhead.common.dto.shared;

import java.io.Serializable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CertificateCreationResponseDTO implements Serializable {

	//=================================================================================================
    // members

	private static final long serialVersionUID = 6712338957176937559L;

	private String certificate;
    private CertificateType certificateType;
    private KeyPairDTO keyPairDTO;
    private String certificateFormat;

    //=================================================================================================
    // constructors

    //-------------------------------------------------------------------------------------------------
	public CertificateCreationResponseDTO() {}

    //-------------------------------------------------------------------------------------------------
	public CertificateCreationResponseDTO(final String certificate, final CertificateType certificateType, final String certificateFormat) {
        this.certificate = certificate;
        this.certificateType = certificateType;
        this.certificateFormat = certificateFormat;
    }

    //-------------------------------------------------------------------------------------------------
	public CertificateCreationResponseDTO(final String certificate,
                                          final CertificateType certificateType,
                                          final KeyPairDTO keyPairDTO,
                                          final String certificateFormat) {
        this.certificate = certificate;
        this.certificateType = certificateType;
        this.keyPairDTO = keyPairDTO;
        this.certificateFormat = certificateFormat;
    }

    //=================================================================================================
    // methods

    //-------------------------------------------------------------------------------------------------
	public String getCertificate() { return certificate; }
	public String getCertificateFormat() { return certificateFormat; }
	public CertificateType getCertificateType() { return certificateType; }
	public KeyPairDTO getKeyPairDTO() { return keyPairDTO; }

    //-------------------------------------------------------------------------------------------------
	public void setCertificate(final String certificate) { this.certificate = certificate; }
    public void setCertificateFormat(final String certificateFormat) { this.certificateFormat = certificateFormat; }
    public void setCertificateType(final CertificateType certificateType) { this.certificateType = certificateType; }
    public void setKeyPairDTO(final KeyPairDTO keyPairDTO) { this.keyPairDTO = keyPairDTO; }

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
