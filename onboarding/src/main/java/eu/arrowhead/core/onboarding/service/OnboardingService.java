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

package eu.arrowhead.core.onboarding.service;

import java.security.KeyPair;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.util.UriComponents;

import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.SecurityUtilities;
import eu.arrowhead.common.core.CoreSystemService;
import eu.arrowhead.common.drivers.CertificateAuthorityDriver;
import eu.arrowhead.common.drivers.DriverUtilities;
import eu.arrowhead.common.drivers.OrchestrationDriver;
import eu.arrowhead.common.dto.internal.CertificateSigningRequestDTO;
import eu.arrowhead.common.dto.internal.CertificateSigningResponseDTO;
import eu.arrowhead.common.dto.shared.CertificateCreationRequestDTO;
import eu.arrowhead.common.dto.shared.CertificateCreationResponseDTO;
import eu.arrowhead.common.dto.shared.CertificateType;
import eu.arrowhead.common.dto.shared.OnboardingResponseDTO;
import eu.arrowhead.common.dto.shared.OnboardingWithCsrRequestDTO;
import eu.arrowhead.common.dto.shared.OnboardingWithCsrResponseDTO;
import eu.arrowhead.common.dto.shared.OnboardingWithNameRequestDTO;
import eu.arrowhead.common.dto.shared.OnboardingWithNameResponseDTO;
import eu.arrowhead.common.dto.shared.ServiceEndpoint;
import eu.arrowhead.common.exception.ArrowheadException;

@Service
public class OnboardingService {

    //=================================================================================================
    // members
	
    private final Logger logger = LogManager.getLogger(OnboardingService.class);
    private final OrchestrationDriver orchestrationDriver;
    private final CertificateAuthorityDriver caDriver;
    private final SecurityUtilities securityUtilities;

    //=================================================================================================
    // methods
    
    //-------------------------------------------------------------------------------------------------
    @Autowired
    public OnboardingService(final OrchestrationDriver orchestrationDriver,
    		final CertificateAuthorityDriver caDriver,
    		final SecurityUtilities securityUtilities) {
    	this.orchestrationDriver = orchestrationDriver;
    	this.caDriver = caDriver;
    	this.securityUtilities = securityUtilities;
    }

    //-------------------------------------------------------------------------------------------------
    public OnboardingWithNameResponseDTO onboarding(final OnboardingWithNameRequestDTO onboardingRequest, final String host, final String address)
            throws DriverUtilities.DriverException {
        logger.debug("onboarding started...");
        Assert.notNull(onboardingRequest, "OnboardingWithNameRequestDTO must not be null");
        Assert.notNull(onboardingRequest.getCreationRequestDTO(), "CreationRequestDTO must not be null");

        final CertificateCreationRequestDTO creationRequestDTO = onboardingRequest.getCreationRequestDTO();
        final KeyPair keyPair = securityUtilities.extractOrGenerateKeyPair(creationRequestDTO);
        final String certificateSigningRequest;

        try {
            certificateSigningRequest = securityUtilities.createCertificateSigningRequest(creationRequestDTO.getCommonName(), keyPair,
                                                                                          CertificateType.AH_ONBOARDING, host, address);
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
            throw new ArrowheadException("Unable to create certificate signing request: " + e.getMessage());
        }

        final var signingResponseDTO = executeCertificateSigningRequest(certificateSigningRequest);
        final var responseDto = new OnboardingWithNameResponseDTO();
        enrichCaResult(responseDto, signingResponseDTO);
        enrichEndpoints(responseDto);

        final CertificateCreationResponseDTO onboardingCertificate = responseDto.getOnboardingCertificate();
        onboardingCertificate.setKeyPairDTO(securityUtilities.encodeKeyPair(keyPair));

        return responseDto;
    }

    //-------------------------------------------------------------------------------------------------
    public OnboardingWithCsrResponseDTO onboarding(final OnboardingWithCsrRequestDTO onboardingRequest)
            throws DriverUtilities.DriverException {
        logger.debug("onboarding started...");
        Assert.notNull(onboardingRequest, "OnboardingWithCsrRequestDTO must not be null");
        Assert.notNull(onboardingRequest.getCertificateSigningRequest(), "CertificateSigningRequest must not be null");

        final var signingResponseDTO = executeCertificateSigningRequest(onboardingRequest.getCertificateSigningRequest());
        final var responseDto = new OnboardingWithCsrResponseDTO();
        enrichCaResult(responseDto, signingResponseDTO);
        enrichEndpoints(responseDto);

        final var onboardingCertificate = responseDto.getOnboardingCertificate();
        securityUtilities.extractAndSetPublicKey(onboardingCertificate);

        return responseDto;
    }

    //=================================================================================================
    // assistant methods

    //-------------------------------------------------------------------------------------------------
    private CertificateSigningResponseDTO executeCertificateSigningRequest(final String certificateSigningRequest) throws DriverUtilities.DriverException {
        logger.debug("Contact CertificateAuthority ...");
        final var csrDTO = new CertificateSigningRequestDTO(certificateSigningRequest);
        return caDriver.signCertificate(csrDTO);
    }

    //-------------------------------------------------------------------------------------------------
	private <T extends OnboardingResponseDTO> void enrichCaResult(final T responseDTO, final CertificateSigningResponseDTO csrResult) {
        logger.debug("Processing response from Certificate Authority ...");
        final CertificateCreationResponseDTO certificateResponseDTO = new CertificateCreationResponseDTO();
        certificateResponseDTO.setCertificate(csrResult.getCertificateChain().get(0));
        certificateResponseDTO.setCertificateFormat(CoreCommonConstants.CERTIFICATE_FORMAT);
        certificateResponseDTO.setCertificateType(CertificateType.AH_ONBOARDING);

        responseDTO.setOnboardingCertificate(certificateResponseDTO);
        responseDTO.setIntermediateCertificate(csrResult.getCertificateChain().get(1));
        responseDTO.setRootCertificate(csrResult.getCertificateChain().get(2));
    }

    //-------------------------------------------------------------------------------------------------
	private void enrichEndpoints(final OnboardingWithNameResponseDTO responseDTO) throws DriverUtilities.DriverException {
        orchestrateAndSet(responseDTO::setDeviceRegistry, CoreSystemService.DEVICEREGISTRY_ONBOARDING_WITH_NAME_SERVICE);
        orchestrateAndSet(responseDTO::setSystemRegistry, CoreSystemService.SYSTEMREGISTRY_ONBOARDING_WITH_NAME_SERVICE);
        orchestrateAndSet(responseDTO::setServiceRegistry, CoreSystemService.SERVICEREGISTRY_REGISTER_SERVICE);
        orchestrateAndSet(responseDTO::setOrchestrationService, CoreSystemService.ORCHESTRATION_SERVICE);
    }

    //-------------------------------------------------------------------------------------------------
	private void enrichEndpoints(final OnboardingWithCsrResponseDTO responseDTO) throws DriverUtilities.DriverException {
        orchestrateAndSet(responseDTO::setDeviceRegistry, CoreSystemService.DEVICEREGISTRY_ONBOARDING_WITH_CSR_SERVICE);
        orchestrateAndSet(responseDTO::setSystemRegistry, CoreSystemService.SYSTEMREGISTRY_ONBOARDING_WITH_CSR_SERVICE);
        orchestrateAndSet(responseDTO::setServiceRegistry, CoreSystemService.SERVICEREGISTRY_REGISTER_SERVICE);
        orchestrateAndSet(responseDTO::setOrchestrationService, CoreSystemService.ORCHESTRATION_SERVICE);
    }

    //-------------------------------------------------------------------------------------------------
	private void orchestrateAndSet(final Consumer<ServiceEndpoint> consumer,
                                   final CoreSystemService coreSystemService) throws DriverUtilities.DriverException {
        logger.debug("Orchestrating '{}' ...", coreSystemService.getServiceDefinition());
        final UriComponents service = orchestrationDriver.findCoreSystemService(coreSystemService);
        consumer.accept(new ServiceEndpoint(coreSystemService, service.toUri()));
    }
}