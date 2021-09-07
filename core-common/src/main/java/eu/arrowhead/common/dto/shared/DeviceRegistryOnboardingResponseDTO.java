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
public abstract class DeviceRegistryOnboardingResponseDTO extends DeviceRegistryResponseDTO implements Serializable {

    //=================================================================================================
    // members

    private static final long serialVersionUID = -635438605292398404L;
    
    private CertificateCreationResponseDTO certificateResponse;

    //=================================================================================================
    // methods

    //-------------------------------------------------------------------------------------------------
	public DeviceRegistryOnboardingResponseDTO() {}

    //-------------------------------------------------------------------------------------------------
	public DeviceRegistryOnboardingResponseDTO(final long id, final DeviceResponseDTO device, final String endOfValidity,
                                               final Map<String, String> metadata, final int version, final String createdAt, final String updatedAt,
                                               final CertificateCreationResponseDTO certificateResponse) {
        super(id, device, endOfValidity, metadata, version, createdAt, updatedAt);
        this.certificateResponse = certificateResponse;
    }

    //-------------------------------------------------------------------------------------------------
	public void load(final DeviceRegistryResponseDTO dto) {
        Assert.notNull(dto, "DeviceRegistryResponseDTO must not be null");

        setId(dto.getId());
        setDevice(dto.getDevice());
        setEndOfValidity(dto.getEndOfValidity());
        setMetadata(dto.getMetadata());
        setVersion(dto.getVersion());
        setCreatedAt(dto.getCreatedAt());
        setUpdatedAt(dto.getUpdatedAt());
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
}