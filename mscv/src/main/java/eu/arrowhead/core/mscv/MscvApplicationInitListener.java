package eu.arrowhead.core.mscv;

import java.security.Security;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import eu.arrowhead.common.ApplicationInitListener;
import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.core.CoreSystemService;
import eu.arrowhead.common.drivers.DriverUtilities;
import eu.arrowhead.common.dto.internal.AuthorizationIntraCloudListResponseDTO;
import eu.arrowhead.common.dto.internal.AuthorizationIntraCloudRequestDTO;
import eu.arrowhead.common.dto.shared.ServiceInterfaceResponseDTO;
import eu.arrowhead.common.dto.shared.ServiceRegistryResponseDTO;
import eu.arrowhead.common.dto.shared.SystemResponseDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponents;

@Component
public class MscvApplicationInitListener extends ApplicationInitListener {

    private static final int MAX_RETRIES = 3;
    private static final long SLEEP_PERIOD = 15_000L;
    private final Logger logger = LogManager.getLogger(MscvApplicationInitListener.class);
    private final DriverUtilities driver;

    @Autowired
    public MscvApplicationInitListener(final DriverUtilities driver) {this.driver = driver;}

    //=================================================================================================
    // members

    //-------------------------------------------------------------------------------------------------
    @Override
    protected void customInit(final ContextRefreshedEvent event) {
        Security.addProvider(new BouncyCastleProvider());

        if (sslProperties.isSslEnabled()) {
            logger.debug("AuthInfo: {}", Base64.getEncoder().encodeToString(publicKey.getEncoded()));
        }

        logger.info("Searching for authorization system");
        final UriComponents authIntraService = driver.findUriByServiceRegistry(CoreSystemService.AUTH_CONTROL_INTRA_SERVICE);
        final UriComponents authMgmtUri = driver.createCustomUri(authIntraService, CommonConstants.AUTHORIZATION_URI,
                                                                 CoreCommonConstants.MGMT_URI + "/intracloud");
        logger.debug("Authorization system intracloud management uri created: {}", authMgmtUri.toUriString());


        logger.info("Searching for own system entry");
        final ServiceRegistryResponseDTO mscvEntry =
                driver.findByServiceRegistry(CoreSystemService.MSCV_VERIFICATION_SERVICE, false);
        final SystemResponseDTO mscvSystem = mscvEntry.getProvider();

        for (CoreSystemService coreSystemService : getRequiredCoreSystemServiceUris()) {
            lookupAndAuthorize(authMgmtUri, mscvSystem, coreSystemService);
        }
    }

    private void lookupAndAuthorize(final UriComponents authMgmtUri,
                                    final SystemResponseDTO consumer,
                                    final CoreSystemService service) {
        final ServiceRegistryResponseDTO serviceEntry = driver.findByServiceRegistry(service, false);
        final SystemResponseDTO systemEntry = serviceEntry.getProvider();

        int retryCount = 0;
        boolean success = false;

        logger.info("Creating authorization rule for {}", service);

        final var authRequest = new AuthorizationIntraCloudRequestDTO();
        authRequest.setConsumerId(consumer.getId());
        authRequest.setProviderIds(Collections.singletonList(systemEntry.getId()));
        authRequest.setInterfaceIds(
                serviceEntry.getInterfaces().stream().map(ServiceInterfaceResponseDTO::getId).collect(Collectors.toList()));
        authRequest.setServiceDefinitionIds(Collections.singletonList(serviceEntry.getServiceDefinition().getId()));
        while (!success) {
            try {
                httpService.sendRequest(authMgmtUri, HttpMethod.POST, AuthorizationIntraCloudListResponseDTO.class, authRequest);
                success = true;
            } catch (final ArrowheadException e) {
                if (retryCount++ > MAX_RETRIES) {
                    throw e;
                } else {
                    logger.info("Unable to retrieve service {}: {}. Retrying in {}ms", service.getServiceDefinition(), e.getMessage(),
                                SLEEP_PERIOD);
                    sleep();
                }
            }
        }
    }

    private void sleep() {
        try {
            Thread.sleep(SLEEP_PERIOD);
        } catch (InterruptedException e) {
            logger.warn(e.getMessage());
        }
    }

    @Override
    protected List<CoreSystemService> getRequiredCoreSystemServiceUris() {
        return List.of(CoreSystemService.EVENT_SUBSCRIBE_SERVICE,
                       CoreSystemService.DEVICEREGISTRY_REGISTER_SERVICE,
                       CoreSystemService.DEVICEREGISTRY_UNREGISTER_SERVICE,
                       CoreSystemService.SYSTEMREGISTRY_REGISTER_SERVICE,
                       CoreSystemService.SYSTEMREGISTRY_UNREGISTER_SERVICE,
                       CoreSystemService.SERVICEREGISTRY_REGISTER_SERVICE,
                       CoreSystemService.SERVICEREGISTRY_UNREGISTER_SERVICE);
    }
}