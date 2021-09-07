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

public class DeviceRegistryOnboardingWithCsrResponseDTO extends DeviceRegistryOnboardingResponseDTO implements Serializable {


	//=================================================================================================
    // members
	
	private static final long serialVersionUID = -33413025504583391L;
	
	//=================================================================================================
	// methods

    //-------------------------------------------------------------------------------------------------
	public DeviceRegistryOnboardingWithCsrResponseDTO() {}

    //-------------------------------------------------------------------------------------------------
	public DeviceRegistryOnboardingWithCsrResponseDTO(final long id, final DeviceResponseDTO device, final String endOfValidity,
                                                      final Map<String, String> metadata, final int version, final String createdAt,
                                                      final String updatedAt,
                                                      final CertificateCreationResponseDTO certificateResponse) {
        super(id, device, endOfValidity, metadata, version, createdAt, updatedAt, certificateResponse);
    }

    // this class exist to keep the structure of <operation>RequestDTO, <operation>ResponseDTO
}