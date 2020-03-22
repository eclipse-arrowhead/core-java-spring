package eu.arrowhead.common;

import eu.arrowhead.common.core.CoreSystemService;
import eu.arrowhead.common.dto.shared.OrchestrationFlags;
import eu.arrowhead.common.dto.shared.OrchestrationFormRequestDTO;
import eu.arrowhead.common.dto.shared.OrchestrationResponseDTO;
import eu.arrowhead.common.dto.shared.OrchestrationResultDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryFormDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryResultDTO;
import eu.arrowhead.common.dto.shared.ServiceRegistryRequestDTO;
import eu.arrowhead.common.dto.shared.ServiceRegistryResponseDTO;
import eu.arrowhead.common.dto.shared.ServiceSecurityType;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.common.dto.shared.SystemResponseDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.http.HttpService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponents;

import java.util.Base64;
import java.util.List;
import java.util.Objects;

@Component
public class OrchestrationDriver {

    //=================================================================================================
    // members
    private final Logger logger = LogManager.getLogger(OrchestrationDriver.class);
    private final SSLProperties sslProperties;
    private final HttpService httpService;
    private final CoreSystemRegistrationProperties coreSystemRegistrationProperties;

    @Autowired
    public OrchestrationDriver(final SSLProperties sslProperties, final HttpService httpService,
                               final CoreSystemRegistrationProperties coreSystemRegistrationProperties) {
        this.sslProperties = sslProperties;
        this.httpService = httpService;
        this.coreSystemRegistrationProperties = coreSystemRegistrationProperties;
    }

    //=================================================================================================
    // methods

    //-------------------------------------------------------------------------------------------------
    public void pingService(final SystemResponseDTO system, final String uriPrefix) {
        logger.debug("getCertificateAuthorityService started...");

        final UriComponents echoUri = createUri(system, uriPrefix + CommonConstants.ECHO_URI);
        httpService.sendRequest(echoUri, HttpMethod.GET, String.class);
        logger.debug("Service at {} is accessible...", echoUri);
    }

    //-------------------------------------------------------------------------------------------------
    public ServiceQueryResultDTO getService(final CoreSystemService service) {
        logger.debug("getService started...");
        final SystemResponseDTO serviceRegistrySystem = new SystemResponseDTO();
        serviceRegistrySystem.setAddress(coreSystemRegistrationProperties.getServiceRegistryAddress());
        serviceRegistrySystem.setPort(coreSystemRegistrationProperties.getServiceRegistryPort());

        pingService(serviceRegistrySystem, CommonConstants.SERVICE_REGISTRY_URI);

        final UriComponents srQueryUri = createUri(serviceRegistrySystem,
                                                   CommonConstants.SERVICE_REGISTRY_URI + CommonConstants.OP_SERVICE_REGISTRY_QUERY_URI);

        final ServiceQueryFormDTO srQueryForm = new ServiceQueryFormDTO
                .Builder(service.getServiceDefinition())
                .build();

        final ResponseEntity<ServiceQueryResultDTO> httpResponse = httpService
                .sendRequest(srQueryUri, HttpMethod.POST, ServiceQueryResultDTO.class, srQueryForm);
        final ServiceQueryResultDTO srQueryResult = httpResponse.getBody();

        if (Objects.isNull(srQueryResult) || srQueryResult.getServiceQueryData().isEmpty()) {
            throw new ArrowheadException("Unable to find service");
        }

        return srQueryResult;
    }

    //-------------------------------------------------------------------------------------------------
    public ServiceQueryResultDTO getOrchestratorService() {
        logger.debug("getOrchestratorService started...");
        return getService(CoreSystemService.ORCHESTRATION_SERVICE);
    }

    //-------------------------------------------------------------------------------------------------
    public OrchestrationResponseDTO contactOrchestrationService(final ServiceRegistryResponseDTO orchService, final String serviceDefinition) {

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
                .interfaces(CommonConstants.HTTP_SECURE_JSON)
                .build();

        final OrchestrationFormRequestDTO orchForm = new OrchestrationFormRequestDTO
                .Builder(requester)
                .requestedService(requestForm)
                .flag(OrchestrationFlags.Flag.OVERRIDE_STORE, true)
                .build();

        final ResponseEntity<OrchestrationResponseDTO> httpResponse = httpService
                .sendRequest(orchUri, HttpMethod.POST, OrchestrationResponseDTO.class, orchForm);
        final OrchestrationResponseDTO orchQueryResult = httpResponse.getBody();

        if (Objects.isNull(orchQueryResult) || orchQueryResult.getResponse().isEmpty()) {
            throw new ArrowheadException("Unable to find " + serviceDefinition);
        }

        return orchQueryResult;
    }

    //=================================================================================================
    // support methods

    //-------------------------------------------------------------------------------------------------
    public UriComponents createUri(final SystemResponseDTO system, final String serviceUri) {
        final String scheme = sslProperties.isSslEnabled() ? CommonConstants.HTTPS : CommonConstants.HTTP;
        return Utilities.createURI(scheme,
                                   system.getAddress(),
                                   system.getPort(),
                                   serviceUri);
    }

    //-------------------------------------------------------------------------------------------------
    public UriComponents createUri(final OrchestrationResultDTO result) {
        final String scheme = sslProperties.isSslEnabled() ? CommonConstants.HTTPS : CommonConstants.HTTP;
        return Utilities.createURI(scheme,
                                   result.getProvider().getAddress(),
                                   result.getProvider().getPort(),
                                   result.getServiceUri());
    }
}
