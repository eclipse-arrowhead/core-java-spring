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
import java.util.Map;

import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@JsonInclude(Include.NON_NULL)
public abstract class SystemRegistryOnboardingResponseDTO extends SystemRegistryResponseDTO implements Serializable {

    //=================================================================================================
    // members

    private static final long serialVersionUID = 1L;
    
    private CertificateCreationResponseDTO certificateResponse;

    //=================================================================================================
    // methods

    //-------------------------------------------------------------------------------------------------
	public SystemRegistryOnboardingResponseDTO() {}

    //-------------------------------------------------------------------------------------------------
	public SystemRegistryOnboardingResponseDTO(final long id, final SystemResponseDTO system, final DeviceResponseDTO provider,
                                               final String endOfValidity, final Map<String, String> metadata, final int version, final String createdAt,
                                               final String updatedAt, final CertificateCreationResponseDTO certificateResponse) {
        super(id, system, provider, endOfValidity, metadata, version, createdAt, updatedAt);
        this.certificateResponse = certificateResponse;
    }

    //-------------------------------------------------------------------------------------------------
    public CertificateCreationResponseDTO getCertificateResponse() { return certificateResponse; }

    //-------------------------------------------------------------------------------------------------
	public void setCertificateResponse(final CertificateCreationResponseDTO certificateResponse) { this.certificateResponse = certificateResponse; }

	//-------------------------------------------------------------------------------------------------
	@Override
	public String toString() {
		try {
			return new ObjectMapper().writeValueAsString(this);
		} catch (final JsonProcessingException ex) {
			return "toString failure";
		}
	}

    //-------------------------------------------------------------------------------------------------
	public void load(final SystemRegistryResponseDTO dto) {
        Assert.notNull(dto, "SystemRegistryResponseDTO must not be null");

        this.setId(dto.getId());
        this.setProvider(dto.getProvider());
        this.setSystem(dto.getSystem());
        this.setMetadata(dto.getMetadata());
        this.setEndOfValidity(dto.getEndOfValidity());
        this.setCreatedAt(dto.getCreatedAt());
        this.setUpdatedAt(dto.getUpdatedAt());
        this.setVersion(dto.getVersion());
    }
}