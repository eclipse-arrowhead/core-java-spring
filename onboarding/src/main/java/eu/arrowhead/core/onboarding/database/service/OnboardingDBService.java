package eu.arrowhead.core.onboarding.database.service;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreSystemRegistrationProperties;
import eu.arrowhead.common.SSLProperties;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.core.CoreSystemService;
import eu.arrowhead.common.database.repository.SystemRepository;
import eu.arrowhead.common.dto.shared.OnboardingWithCsrRequestDTO;
import eu.arrowhead.common.dto.shared.OnboardingWithCsrResponseDTO;
import eu.arrowhead.common.dto.shared.OnboardingWithNameRequestDTO;
import eu.arrowhead.common.dto.shared.OnboardingWithNameResponseDTO;
import eu.arrowhead.common.dto.shared.OrchestrationFormRequestDTO;
import eu.arrowhead.common.dto.shared.OrchestrationResponseDTO;
import eu.arrowhead.common.dto.shared.OrchestrationResultDTO;
import eu.arrowhead.common.dto.shared.ServiceEndpoint;
import eu.arrowhead.common.dto.shared.ServiceQueryFormDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryResultDTO;
import eu.arrowhead.common.dto.shared.ServiceRegistryResponseDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.common.dto.shared.SystemResponseDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.http.HttpService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponents;

import java.util.Objects;

@Service
public class OnboardingDBService {
    private static final String COULD_NOT_DELETE_SYSTEM_ERROR_MESSAGE = "Could not delete System, with given parameters";
    private static final String PORT_RANGE_ERROR_MESSAGE = "Port must be between " + CommonConstants.SYSTEM_PORT_RANGE_MIN + " and " + CommonConstants.SYSTEM_PORT_RANGE_MAX + ".";
    //=================================================================================================
    // members
    private final Logger logger = LogManager.getLogger(OnboardingDBService.class);
    private final SystemRepository systemRepository;
    private final SSLProperties sslProperties;
    private final HttpService httpService;
    private final CoreSystemRegistrationProperties coreSystemRegistrationProperties;

    @Autowired
    public OnboardingDBService(final SystemRepository systemRepository, final SSLProperties sslProperties,
                               final HttpService httpService, final CoreSystemRegistrationProperties coreSystemRegistrationProperties) {
        this.systemRepository = systemRepository;
        this.sslProperties = sslProperties;
        this.httpService = httpService;
        this.coreSystemRegistrationProperties = coreSystemRegistrationProperties;
    }

    //=================================================================================================
    // methods

    //-------------------------------------------------------------------------------------------------
    public OnboardingWithNameResponseDTO onboarding(final OnboardingWithNameRequestDTO onboardingRequest) {
        logger.debug("onboarding started...");
        // TODO contact certificate authority
        return null;
    }

    //-------------------------------------------------------------------------------------------------
    public OnboardingWithCsrResponseDTO onboarding(final OnboardingWithCsrRequestDTO onboardingRequest) {
        logger.debug("onboarding started...");
        return executeCertificateSigningRequest(onboardingRequest.getCertificateSigningRequest());
    }

    //=================================================================================================
    // assistant methods


    //-------------------------------------------------------------------------------------------------
    private OnboardingWithCsrResponseDTO executeCertificateSigningRequest(String certificateSigningRequest) {

        final ServiceRegistryResponseDTO orchService = getOrchestratorService();
        // TODO replace with CoreSystemService constant
        final OrchestrationResultDTO caOrchResult = contactOrchestrationService(orchService, "ca-sign");

        final UriComponents caUri = createUri(caOrchResult.getProvider(),
                CommonConstants.CERTIFICATE_AUTHRORITY_URI + CommonConstants.OP_CA_SIGN_CERTIFICATE_URI);

        final CertificateSigningRequestDTO csrDTO = new CertificateSigningRequestDTO(certificateSigningRequest);
        final ResponseEntity<CertificateSigningResponseDTO> httpResponse = httpService.sendRequest(caUri, HttpMethod.POST, CertificateSigningResponseDTO.class, csrDTO);

        final CertificateSigningResponseDTO csrResult = httpResponse.getBody();

        if (Objects.isNull(csrResult) || csrResult.getServiceQueryData().isEmpty()) {
            throw new ArrowheadException("Unable to contact Certificate Authority");
        }

        final OnboardingWithCsrResponseDTO retValue = new OnboardingWithCsrResponseDTO();
        retValue.setRootCertificate(csrResult.get(0));
        retValue.setIntermediateCertificate(csrResult.get(1));
        retValue.setOnboardingCertificate(csrResult.get(2));

        final OrchestrationResultDTO drOrchResult = contactOrchestrationService(orchService, CommonConstants.CORE_SERVICE_DEVICE_REGISTRY_REGISTER);
        final OrchestrationResultDTO sysrOrchResult = contactOrchestrationService(orchService, CommonConstants.CORE_SERVICE_SYSTEM_REGISTRY_REGISTER);
        final OrchestrationResultDTO srOrchResult = contactOrchestrationService(orchService, CommonConstants.CORE_SERVICE_SERVICE_REGISTRY_REGISTER);

        final UriComponents drUri = createUri(drOrchResult.getProvider(),
                CommonConstants.DEVICE_REGISTRY_URI + CommonConstants.OP_DEVICE_REGISTRY_REGISTER_URI);
        final UriComponents sysrUri = createUri(drOrchResult.getProvider(),
                CommonConstants.SYSTEM_REGISTRY_URI + CommonConstants.OP_SYSTEM_REGISTRY_REGISTER_URI);
        final UriComponents srUri = createUri(drOrchResult.getProvider(),
                CommonConstants.SERVICE_REGISTRY_URI + CommonConstants.OP_SERVICE_REGISTRY_REGISTER_URI);

        retValue.setDeviceRegistry(new ServiceEndpoint(CoreSystemService.DEVICE_REGISTRY_REGISTER_SERVICE, drUri.toUri()));
        retValue.setDeviceRegistry(new ServiceEndpoint(CoreSystemService.SYSTEM_REGISTRY_REGISTER_SERVICE, sysrUri.toUri()));
        retValue.setDeviceRegistry(new ServiceEndpoint(CoreSystemService.SERVICE_REGISTRY_REGISTER_SERVICE, srUri.toUri()));

        return retValue;
    }

    //-------------------------------------------------------------------------------------------------
    private void pingService(final SystemResponseDTO system, final String uriPrefix) {
        logger.debug("getCertificateAuthorityService started...");

        final UriComponents echoUri = createUri(system, uriPrefix + CommonConstants.ECHO_URI);
        httpService.sendRequest(echoUri, HttpMethod.GET, String.class);
        logger.info("Service at {} is accessible...", echoUri);
    }

    //-------------------------------------------------------------------------------------------------
    private ServiceRegistryResponseDTO getOrchestratorService() {

        final SystemResponseDTO serviceRegistrySystem = new SystemResponseDTO();
        serviceRegistrySystem.setAddress(coreSystemRegistrationProperties.getServiceRegistryAddress());
        serviceRegistrySystem.setPort(coreSystemRegistrationProperties.getServiceRegistryPort());

        logger.debug("getOrchestratorService started...");
        pingService(serviceRegistrySystem, CommonConstants.SERVICE_REGISTRY_URI);

        final UriComponents srQueryUri = createUri(serviceRegistrySystem,
                CommonConstants.SERVICE_REGISTRY_URI + CommonConstants.OP_SERVICE_REGISTRY_QUERY_URI);

        final ServiceQueryFormDTO srQueryForm = new ServiceQueryFormDTO
                .Builder(CoreSystemService.ORCHESTRATION_SERVICE.getServiceDefinition())
                .build();

        final ResponseEntity<ServiceQueryResultDTO> httpResponse = httpService.sendRequest(srQueryUri, HttpMethod.POST, ServiceQueryResultDTO.class, srQueryForm);
        final ServiceQueryResultDTO srQueryResult = httpResponse.getBody();

        if (Objects.isNull(srQueryResult) || srQueryResult.getServiceQueryData().isEmpty()) {
            throw new ArrowheadException("Unable to find orchestrator service");
        }

        return srQueryResult.getServiceQueryData().get(0);
    }

    //-------------------------------------------------------------------------------------------------
    private OrchestrationResultDTO contactOrchestrationService(final ServiceRegistryResponseDTO orchService, final String serviceDefinition) {

        logger.debug("contactOrchestrationService started...");
        pingService(orchService.getProvider(), CommonConstants.ORCHESTRATOR_URI);

        final UriComponents orchUri = createUri(orchService.getProvider(),
                CommonConstants.ORCHESTRATOR_URI + CommonConstants.OP_ORCH_PROCESS);

        final SystemRequestDTO requester = new SystemRequestDTO();
        requester.setAddress(coreSystemRegistrationProperties.getCoreSystemDomainName());
        requester.setPort(coreSystemRegistrationProperties.getCoreSystemDomainPort());
        requester.setSystemName(coreSystemRegistrationProperties.getCoreSystemName());

        final ServiceQueryFormDTO requestForm = new ServiceQueryFormDTO
                .Builder(serviceDefinition)
                .build();

        final OrchestrationFormRequestDTO orchForm = new OrchestrationFormRequestDTO
                .Builder(requester)
                .requestedService(requestForm)
                .build();

        final ResponseEntity<OrchestrationResponseDTO> httpResponse = httpService.sendRequest(orchUri, HttpMethod.POST, OrchestrationResponseDTO.class, orchForm);
        final OrchestrationResponseDTO orchQueryResult = httpResponse.getBody();

        if (Objects.isNull(orchQueryResult) || orchQueryResult.getResponse().isEmpty()) {
            throw new ArrowheadException("Unable to find " + serviceDefinition);
        }

        return orchQueryResult.getResponse().get(0);
    }

    //-------------------------------------------------------------------------------------------------
    private UriComponents createUri(final SystemResponseDTO system, final String serviceUri) {
        final String scheme = sslProperties.isSslEnabled() ? CommonConstants.HTTPS : CommonConstants.HTTP;
        return Utilities.createURI(scheme,
                system.getAddress(),
                system.getPort(),
                serviceUri);
    }
}
