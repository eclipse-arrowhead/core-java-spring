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
import java.util.StringJoiner;

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


    public OnboardingResponseDTO() {
    }

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

    //=================================================================================================
    // methods

    //-------------------------------------------------------------------------------------------------
    public ServiceEndpoint getDeviceRegistry() {
        return deviceRegistry;
    }

    //-------------------------------------------------------------------------------------------------
    public void setDeviceRegistry(final ServiceEndpoint deviceRegistry) {
        this.deviceRegistry = deviceRegistry;
    }

    //-------------------------------------------------------------------------------------------------
    public ServiceEndpoint getSystemRegistry() {
        return systemRegistry;
    }

    //-------------------------------------------------------------------------------------------------
    public void setSystemRegistry(final ServiceEndpoint systemRegistry) {
        this.systemRegistry = systemRegistry;
    }

    //-------------------------------------------------------------------------------------------------
    public ServiceEndpoint getServiceRegistry() {
        return serviceRegistry;
    }

    //-------------------------------------------------------------------------------------------------
    public void setServiceRegistry(final ServiceEndpoint serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    //-------------------------------------------------------------------------------------------------
    public ServiceEndpoint getOrchestrationService() {
        return orchestrationService;
    }

    //-------------------------------------------------------------------------------------------------
    public void setOrchestrationService(ServiceEndpoint orchestrationService) {
        this.orchestrationService = orchestrationService;
    }

    //-------------------------------------------------------------------------------------------------
    public CertificateCreationResponseDTO getOnboardingCertificate() {
        return onboardingCertificate;
    }

    //-------------------------------------------------------------------------------------------------
    public void setOnboardingCertificate(final CertificateCreationResponseDTO onboardingCertificate) {
        this.onboardingCertificate = onboardingCertificate;
    }

    //-------------------------------------------------------------------------------------------------
    public String getIntermediateCertificate() {
        return intermediateCertificate;
    }

    //-------------------------------------------------------------------------------------------------
    public void setIntermediateCertificate(final String intermediateCertificate) {
        this.intermediateCertificate = intermediateCertificate;
    }

    //-------------------------------------------------------------------------------------------------
    public String getRootCertificate() {
        return rootCertificate;
    }

    //-------------------------------------------------------------------------------------------------
    public void setRootCertificate(final String rootCertificate) {
        this.rootCertificate = rootCertificate;
    }


    @Override
    public String toString() {
        return new StringJoiner(", ", OnboardingResponseDTO.class.getSimpleName() + "[", "]")
                .add("deviceRegistry=" + deviceRegistry)
                .add("systemRegistry=" + systemRegistry)
                .add("serviceRegistry=" + serviceRegistry)
                .add("orchestrationService=" + orchestrationService)
                .add("onboardingCertificate=" + onboardingCertificate)
                .add("intermediateCertificate='" + intermediateCertificate + "'")
                .add("rootCertificate='" + rootCertificate + "'")
                .toString();
    }
}
