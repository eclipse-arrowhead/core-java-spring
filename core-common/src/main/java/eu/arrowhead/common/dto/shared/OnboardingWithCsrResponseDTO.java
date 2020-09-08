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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.Serializable;

public class OnboardingWithCsrResponseDTO extends OnboardingResponseDTO implements Serializable {

    //=================================================================================================
    // members
    private static final long serialVersionUID = 1L;

    public OnboardingWithCsrResponseDTO() {
    }

    public OnboardingWithCsrResponseDTO(final ServiceEndpoint deviceRegistry, final ServiceEndpoint systemRegistry,
                                        final ServiceEndpoint serviceRegistry, final ServiceEndpoint orchestrationService,
                                        final CertificateCreationResponseDTO onboardingCertificate, final String intermediateCertificate,
                                        final String rootCertificate) {
        super(deviceRegistry, systemRegistry, serviceRegistry, orchestrationService, onboardingCertificate, intermediateCertificate, rootCertificate);
    }

    //-------------------------------------------------------------------------------------------------
    @Override
    public String toString() {
        try {
		return new ObjectMapper().writeValueAsString(this);
	} catch (final JsonProcessingException ex) {
		return "toString failure";
	}
    }

    // this class exist to keep the structure of <operation>RequestDTO, <operation>ResponseDTO
}
