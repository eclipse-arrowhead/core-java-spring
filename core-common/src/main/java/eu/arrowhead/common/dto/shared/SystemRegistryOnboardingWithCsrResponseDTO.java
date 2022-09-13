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

public class SystemRegistryOnboardingWithCsrResponseDTO extends SystemRegistryOnboardingResponseDTO implements Serializable {


	//=================================================================================================
    // members

	private static final long serialVersionUID = -5751934090192200480L;
	
	//=================================================================================================
	// methods
	
    //-------------------------------------------------------------------------------------------------
	public SystemRegistryOnboardingWithCsrResponseDTO() {}

    //-------------------------------------------------------------------------------------------------
	public SystemRegistryOnboardingWithCsrResponseDTO(final long id, final SystemResponseDTO system,
                                                      final DeviceResponseDTO provider, final String endOfValidity,
                                                      final Map<String, String> metadata, final int version, final String createdAt,
                                                      final String updatedAt,
                                                      final CertificateCreationResponseDTO certificateResponse) {
        super(id, system, provider, endOfValidity, metadata, version, createdAt, updatedAt, certificateResponse);
    }

    // this class exist to keep the structure of <operation>RequestDTO, <operation>ResponseDTO
}