package eu.arrowhead.core.onboarding.database.service;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.OrchestrationDriver;
import eu.arrowhead.common.SecurityUtilities;
import eu.arrowhead.common.core.CoreSystemService;
import eu.arrowhead.common.dto.internal.CertificateSigningRequestDTO;
import eu.arrowhead.common.dto.internal.CertificateSigningResponseDTO;
import eu.arrowhead.common.dto.shared.CertificateCreationRequestDTO;
import eu.arrowhead.common.dto.shared.CertificateResponseDTO;
import eu.arrowhead.common.dto.shared.OnboardingResponseDTO;
import eu.arrowhead.common.dto.shared.OnboardingWithCsrRequestDTO;
import eu.arrowhead.common.dto.shared.OnboardingWithCsrResponseDTO;
import eu.arrowhead.common.dto.shared.OnboardingWithNameRequestDTO;
import eu.arrowhead.common.dto.shared.OnboardingWithNameResponseDTO;
import eu.arrowhead.common.dto.shared.OrchestrationResultDTO;
import eu.arrowhead.common.dto.shared.ServiceEndpoint;
import eu.arrowhead.common.dto.shared.ServiceRegistryResponseDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.http.HttpService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponents;

import java.security.KeyPair;
import java.util.Objects;

@Service
public class OnboardingDBService {

    //=================================================================================================
    // members
    private final Logger logger = LogManager.getLogger(OnboardingDBService.class);
    private final OrchestrationDriver orchestration;
    private final HttpService httpService;
    private final SecurityUtilities securityUtilities;

    @Autowired
    public OnboardingDBService(final OrchestrationDriver orchestration,
                               final HttpService httpService,
                               final SecurityUtilities securityUtilities) {
        this.orchestration = orchestration;
        this.httpService = httpService;
        this.securityUtilities = securityUtilities;
    }

    //=================================================================================================
    // methods

    //-------------------------------------------------------------------------------------------------
    public OnboardingWithNameResponseDTO onboarding(final OnboardingWithNameRequestDTO onboardingRequest) {
        logger.debug("onboarding started...");

        final CertificateCreationRequestDTO creationRequestDTO = onboardingRequest.getCreationRequestDTO();
        final KeyPair keyPair = securityUtilities.extractKeyPair(creationRequestDTO);
        final String certificateSigningRequest;

        try {
            certificateSigningRequest = securityUtilities.createEncodedCSR(creationRequestDTO.getCommonName(), keyPair);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new ArrowheadException("Unable to create certificate signing request: " + e.getMessage());
        }

        final CertificateSigningResponseDTO signingResponseDTO = executeCertificateSigningRequest(certificateSigningRequest);
        return enrichOnboardingResponse(new OnboardingWithNameResponseDTO(), signingResponseDTO);
    }

    //-------------------------------------------------------------------------------------------------
    public OnboardingWithCsrResponseDTO onboarding(final OnboardingWithCsrRequestDTO onboardingRequest) {
        logger.debug("onboarding started...");
        final CertificateSigningResponseDTO signingResponseDTO =
                executeCertificateSigningRequest(onboardingRequest.getCertificateSigningRequest());
        return enrichOnboardingResponse(new OnboardingWithCsrResponseDTO(), signingResponseDTO);
    }

    //=================================================================================================
    // assistant methods

    //-------------------------------------------------------------------------------------------------
    private CertificateSigningResponseDTO executeCertificateSigningRequest(final String certificateSigningRequest) {

        final ServiceRegistryResponseDTO orchService = getOrchestratorService();
        final OrchestrationResultDTO caOrchResult = contactOrchestrationService(orchService, CommonConstants.CORE_SERVICE_CERTIFICATE_AUTHORITY_SIGN);

        final UriComponents caUri = orchestration.createUri(caOrchResult.getProvider(),
                                                            CommonConstants.CERTIFICATE_AUTHRORITY_URI + CommonConstants.OP_CA_SIGN_CERTIFICATE_URI);

        final CertificateSigningRequestDTO csrDTO = new CertificateSigningRequestDTO(certificateSigningRequest);
        final ResponseEntity<CertificateSigningResponseDTO> httpResponse = httpService
                .sendRequest(caUri, HttpMethod.POST, CertificateSigningResponseDTO.class, csrDTO);

        final CertificateSigningResponseDTO csrResult = httpResponse.getBody();

        if (Objects.isNull(csrResult) || csrResult.getCertificateChain().isEmpty()) {
            throw new ArrowheadException("Unable to contact Certificate Authority");
        }

        return csrResult;
    }

    private <T extends OnboardingResponseDTO> T enrichOnboardingResponse(final T responseDTO,
                                                                         final CertificateSigningResponseDTO csrResult) {
        final ServiceRegistryResponseDTO orchService = getOrchestratorService();

        final CertificateResponseDTO certificateResponseDTO = new CertificateResponseDTO();
        certificateResponseDTO.setCertificate(csrResult.getCertificateChain().get(2));

        responseDTO.setRootCertificate(csrResult.getCertificateChain().get(0));
        responseDTO.setIntermediateCertificate(csrResult.getCertificateChain().get(1));
        responseDTO.setOnboardingCertificate(certificateResponseDTO);

        final OrchestrationResultDTO drOrchResult = contactOrchestrationService(orchService, CommonConstants.CORE_SERVICE_DEVICE_REGISTRY_REGISTER);
        final OrchestrationResultDTO sysrOrchResult = contactOrchestrationService(orchService, CommonConstants.CORE_SERVICE_SYSTEM_REGISTRY_REGISTER);
        final OrchestrationResultDTO srOrchResult = contactOrchestrationService(orchService, CommonConstants.CORE_SERVICE_SERVICE_REGISTRY_REGISTER);
        final OrchestrationResultDTO orchOrchResult = contactOrchestrationService(orchService, CommonConstants.CORE_SERVICE_ORCH_PROCESS);

        responseDTO.setDeviceRegistry(new ServiceEndpoint(CoreSystemService.DEVICE_REGISTRY_REGISTER_SERVICE, orchestration.createUri(drOrchResult).toUri()));
        responseDTO.setSystemRegistry(new ServiceEndpoint(CoreSystemService.SYSTEM_REGISTRY_REGISTER_SERVICE, orchestration.createUri(sysrOrchResult).toUri()));
        responseDTO.setServiceRegistry(new ServiceEndpoint(CoreSystemService.SERVICE_REGISTRY_REGISTER_SERVICE, orchestration.createUri(srOrchResult).toUri()));
        responseDTO.setOrchestrationService(new ServiceEndpoint(CoreSystemService.ORCHESTRATION_SERVICE, orchestration.createUri(orchOrchResult).toUri()));

        return responseDTO;
    }

    //-------------------------------------------------------------------------------------------------
    private ServiceRegistryResponseDTO getOrchestratorService() {
        return orchestration.getOrchestratorService().getServiceQueryData().get(0);
    }

    //-------------------------------------------------------------------------------------------------
    private OrchestrationResultDTO contactOrchestrationService(final ServiceRegistryResponseDTO orchService, final String serviceDefinition) {
        return orchestration.contactOrchestrationService(orchService, serviceDefinition).getResponse().get(0);
    }
}
