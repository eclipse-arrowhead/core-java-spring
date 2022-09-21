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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@JsonInclude(Include.NON_NULL)
public class SystemRegistryOnboardingWithCsrRequestDTO extends SystemRegistryRequestDTO implements Serializable {

    //=================================================================================================
    // members

    private static final long serialVersionUID = -635438605292398404L;
    
    private String certificateSigningRequest;

    //=================================================================================================
    // methods

    //-------------------------------------------------------------------------------------------------
	public SystemRegistryOnboardingWithCsrRequestDTO() {}

    //-------------------------------------------------------------------------------------------------
	public SystemRegistryOnboardingWithCsrRequestDTO(final SystemRequestDTO system, final DeviceRequestDTO provider, final String endOfValidity,
                                                     final String certificateSigningRequest) {
        super(system, provider, endOfValidity);
        this.certificateSigningRequest = certificateSigningRequest;
    }

    //-------------------------------------------------------------------------------------------------
	public SystemRegistryOnboardingWithCsrRequestDTO(final SystemRequestDTO system, final DeviceRequestDTO provider, final String endOfValidity,
                                                     final Map<String, String> metadata, final Integer version, final String certificateSigningRequest) {
        super(system, provider, endOfValidity, metadata, version);
        this.certificateSigningRequest = certificateSigningRequest;
    }

    //-------------------------------------------------------------------------------------------------
    public String getCertificateSigningRequest() { return certificateSigningRequest; }

    //-------------------------------------------------------------------------------------------------
	public void setCertificateSigningRequest(final String certificateSigningRequest) { this.certificateSigningRequest = certificateSigningRequest; }

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