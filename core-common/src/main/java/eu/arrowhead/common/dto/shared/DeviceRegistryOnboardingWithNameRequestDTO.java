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
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@JsonInclude(Include.NON_NULL)
public class DeviceRegistryOnboardingWithNameRequestDTO extends DeviceRegistryRequestDTO implements Serializable {

    //=================================================================================================
    // members

    private static final long serialVersionUID = -635438605292398404L;
    
    private CertificateCreationRequestDTO certificateCreationRequest;

    //=================================================================================================
    // methods

    //-------------------------------------------------------------------------------------------------
	public DeviceRegistryOnboardingWithNameRequestDTO() {}

    //-------------------------------------------------------------------------------------------------
    public DeviceRegistryOnboardingWithNameRequestDTO(final DeviceRequestDTO device, final String endOfValidity) {
        super(device, endOfValidity);
    }

    //-------------------------------------------------------------------------------------------------
    public DeviceRegistryOnboardingWithNameRequestDTO(final String deviceName, final String macAddress, final String endOfValidity) {
        super(new DeviceRequestDTO(deviceName, macAddress), endOfValidity);
        this.certificateCreationRequest = new CertificateCreationRequestDTO(deviceName);
    }

    //-------------------------------------------------------------------------------------------------
    public DeviceRegistryOnboardingWithNameRequestDTO(final DeviceRequestDTO device,
                                                      final String endOfValidity,
                                                      final Map<String, String> metadata,
                                                      final Integer version) {
        super(device, endOfValidity, metadata, version);
        this.certificateCreationRequest = new CertificateCreationRequestDTO(Objects.requireNonNull(device).getDeviceName());
    }

    //-------------------------------------------------------------------------------------------------
    public CertificateCreationRequestDTO getCertificateCreationRequest() { return certificateCreationRequest; }

    //-------------------------------------------------------------------------------------------------
	public void setCertificateCreationRequest(final CertificateCreationRequestDTO certificateCreationRequest) { this.certificateCreationRequest = certificateCreationRequest; }

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