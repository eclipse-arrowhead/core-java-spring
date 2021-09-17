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

public abstract class OnboardingResponseDTO implements Serializable {

    //=================================================================================================
    // members

    private static final long serialVersionUID = 1L;
    
    private ServiceEndpoint deviceRegistry;
    private ServiceEndpoint systemRegistry;
    private ServiceEndpoint serviceRegistry;
    private ServiceEndpoint orchestrationService;

    private CertificateCreationResponseDTO onboardingCertificate;
    private String intermediateCertificate;
    private String rootCertificate;

    //=================================================================================================
	// methods

    //-------------------------------------------------------------------------------------------------
	public OnboardingResponseDTO() {}

    //-------------------------------------------------------------------------------------------------
	public OnboardingResponseDTO(final ServiceEndpoint deviceRegistry, final ServiceEndpoint systemRegistry, final ServiceEndpoint serviceRegistry,
                                 final ServiceEndpoint orchestrationService, final CertificateCreationResponseDTO onboardingCertificate,
                                 final String intermediateCertificate, final String rootCertificate) {
        this.deviceRegistry = deviceRegistry;
        this.systemRegistry = systemRegistry;
        this.serviceRegistry = serviceRegistry;
        this.orchestrationService = orchestrationService;
        this.onboardingCertificate = onboardingCertificate;
        this.intermediateCertificate = intermediateCertificate;
        this.rootCertificate = rootCertificate;
    }

    //-------------------------------------------------------------------------------------------------
    public ServiceEndpoint getDeviceRegistry() { return deviceRegistry; }
    public ServiceEndpoint getSystemRegistry() { return systemRegistry; }
    public ServiceEndpoint getServiceRegistry() { return serviceRegistry; }
    public ServiceEndpoint getOrchestrationService() { return orchestrationService; }
    public CertificateCreationResponseDTO getOnboardingCertificate() { return onboardingCertificate; }
    public String getIntermediateCertificate() { return intermediateCertificate; }
    public String getRootCertificate() { return rootCertificate; }

    //-------------------------------------------------------------------------------------------------
    public void setDeviceRegistry(final ServiceEndpoint deviceRegistry) { this.deviceRegistry = deviceRegistry; }
    public void setSystemRegistry(final ServiceEndpoint systemRegistry) { this.systemRegistry = systemRegistry; }
    public void setServiceRegistry(final ServiceEndpoint serviceRegistry) { this.serviceRegistry = serviceRegistry; }
    public void setOrchestrationService(final ServiceEndpoint orchestrationService) { this.orchestrationService = orchestrationService; }
    public void setOnboardingCertificate(final CertificateCreationResponseDTO onboardingCertificate) { this.onboardingCertificate = onboardingCertificate; }
    public void setIntermediateCertificate(final String intermediateCertificate) { this.intermediateCertificate = intermediateCertificate; }
    public void setRootCertificate(final String rootCertificate) { this.rootCertificate = rootCertificate; }

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