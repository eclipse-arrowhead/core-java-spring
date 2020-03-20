package eu.arrowhead.core.onboarding;

import eu.arrowhead.common.ApplicationInitListener;
import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.OrchestrationDriver;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.core.CoreSystemService;
import eu.arrowhead.common.dto.internal.AuthorizationIntraCloudListResponseDTO;
import eu.arrowhead.common.dto.internal.AuthorizationIntraCloudRequestDTO;
import eu.arrowhead.common.dto.shared.ServiceInterfaceResponseDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryResultDTO;
import eu.arrowhead.common.dto.shared.ServiceRegistryResponseDTO;
import eu.arrowhead.common.dto.shared.SystemResponseDTO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponents;

import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class OnboardingApplicationInitListener extends ApplicationInitListener {

    private final Logger logger = LogManager.getLogger(OnboardingApplicationInitListener.class);
    private final OrchestrationDriver driver;

    @Autowired
    public OnboardingApplicationInitListener(final OrchestrationDriver driver) {this.driver = driver;}

    //=================================================================================================
    // members

    //-------------------------------------------------------------------------------------------------
    @Override
    protected void customInit(final ContextRefreshedEvent event) {
        final String scheme;
        if (sslProperties.isSslEnabled()) {
            logger.debug("AuthInfo: {}" + Base64.getEncoder().encodeToString(publicKey.getEncoded()));
            scheme = CommonConstants.HTTPS;
        }
        else {
            scheme = CommonConstants.HTTP;
        }

        logger.info("Searching for authorization system");
        final ServiceRegistryResponseDTO authEntry = lookup(CoreSystemService.AUTH_CONTROL_INTRA_SERVICE, CommonConstants.AUTHORIZATION_URI);
        final SystemResponseDTO authSystem = authEntry.getProvider();

        final UriComponents authMgmtUri = Utilities.createURI(scheme, authSystem.getAddress(), authSystem.getPort(),
                                                              CommonConstants.AUTHORIZATION_URI + OnboardingConstants.AUTHORIZATION_INTRA_CLOUD_MGMT_URI);

        logger.debug("Searching for own system entry");
        final ServiceRegistryResponseDTO onboardingEntry = lookup(CoreSystemService.ONBOARDING_WITH_CERTIFICATE_AND_CSR);
        final SystemResponseDTO onboardingSystem = onboardingEntry.getProvider();

        logger.debug("Authorization system intracloud management uri created: {}", authMgmtUri.toUriString());
        lookupAndAuthorize(authMgmtUri, onboardingSystem, CoreSystemService.CERTIFICATE_AUTHORITY_SIGN_SERVICE, CommonConstants.CERTIFICATE_AUTHRORITY_URI);
        lookupAndAuthorize(authMgmtUri, onboardingSystem, CoreSystemService.ORCHESTRATION_SERVICE, CommonConstants.ORCHESTRATOR_URI);
        lookupAndAuthorize(authMgmtUri, onboardingSystem, CoreSystemService.DEVICE_REGISTRY_REGISTER_SERVICE, CommonConstants.DEVICE_REGISTRY_URI);
        lookupAndAuthorize(authMgmtUri, onboardingSystem, CoreSystemService.SYSTEM_REGISTRY_REGISTER_SERVICE, CommonConstants.SYSTEM_REGISTRY_URI);
        lookupAndAuthorize(authMgmtUri, onboardingSystem, CoreSystemService.SERVICE_REGISTRY_REGISTER_SERVICE, CommonConstants.SERVICE_REGISTRY_URI);
    }

    @Override
    protected List<CoreSystemService> getRequiredCoreSystemServiceUris() {
        return List.of(CoreSystemService.CERTIFICATE_AUTHORITY_SIGN_SERVICE,
                       CoreSystemService.ORCHESTRATION_SERVICE,
                       CoreSystemService.DEVICE_REGISTRY_REGISTER_SERVICE,
                       CoreSystemService.SYSTEM_REGISTRY_REGISTER_SERVICE,
                       CoreSystemService.SERVICE_REGISTRY_REGISTER_SERVICE);
    }

    private void lookupAndAuthorize(final UriComponents authMgmtUri,
                                    final SystemResponseDTO consumer,
                                    final CoreSystemService service,
                                    final String prefix) {
        final ServiceRegistryResponseDTO serviceEntry = lookup(service, prefix);
        final SystemResponseDTO systemEntry = serviceEntry.getProvider();

        logger.debug("Creating authorization rule");
        final var authRequest = new AuthorizationIntraCloudRequestDTO();
        authRequest.setConsumerId(consumer.getId());
        authRequest.setProviderIds(Collections.singletonList(systemEntry.getId()));
        authRequest.setInterfaceIds(serviceEntry.getInterfaces().stream().map(ServiceInterfaceResponseDTO::getId).collect(Collectors.toList()));
        authRequest.setServiceDefinitionIds(Collections.singletonList(serviceEntry.getServiceDefinition().getId()));
        httpService.sendRequest(authMgmtUri, HttpMethod.POST, AuthorizationIntraCloudListResponseDTO.class, authRequest);
    }

    private ServiceRegistryResponseDTO lookup(final CoreSystemService coreSystemService) {
        logger.info("Looking up {}", coreSystemService.getServiceDefinition());
        final ServiceQueryResultDTO onboardingQueryResultDTO = driver.getService(coreSystemService);
        return onboardingQueryResultDTO.getServiceQueryData().get(0);
    }

    private ServiceRegistryResponseDTO lookup(final CoreSystemService coreSystemService, final String prefix) {
        final ServiceRegistryResponseDTO responseDTO = lookup(coreSystemService);
        driver.pingService(responseDTO.getProvider(), prefix);
        return responseDTO;
    }
}