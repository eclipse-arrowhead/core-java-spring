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

package eu.arrowhead.core.onboarding;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponents;

import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class OnboardingApplicationInitListener extends ApplicationInitListener {
	
	//=================================================================================================
	// members

    private static final long SLEEP_PERIOD = TimeUnit.SECONDS.toMillis(15);
    private static final int MAX_RETRIES = 10;

    private final Logger logger = LogManager.getLogger(OnboardingApplicationInitListener.class);
    private final DriverUtilities driver;


    //=================================================================================================
    // methods
    
    //-------------------------------------------------------------------------------------------------
	@Autowired
    public OnboardingApplicationInitListener(final DriverUtilities driver) {
		this.driver = driver;
	}
	
	//=================================================================================================
	// assistant methods

    //-------------------------------------------------------------------------------------------------
    @Override
    protected void customInit(final ContextRefreshedEvent event) {

        if (sslProperties.isSslEnabled()) {
            logger.debug("AuthInfo: {}", Base64.getEncoder().encodeToString(publicKey.getEncoded()));
        }
        
		@SuppressWarnings("unchecked")
		final Map<String,Object> context = event.getApplicationContext().getBean(CommonConstants.ARROWHEAD_CONTEXT, Map.class);
		standaloneMode = context.containsKey(CoreCommonConstants.SERVER_STANDALONE_MODE);

		if (standaloneMode) {
			return;
		}

        logger.info("Searching for authorization system");
        final UriComponents authIntraService = driver.findUriByServiceRegistry(CoreSystemService.AUTH_CONTROL_INTRA_SERVICE);
        final UriComponents authMgmtUri = driver.createCustomUri(authIntraService, CommonConstants.AUTHORIZATION_URI,
                                                                 OnboardingConstants.AUTHORIZATION_INTRA_CLOUD_MGMT_URI);
        logger.debug("Authorization system intracloud management uri created: {}", authMgmtUri.toUriString());


        logger.info("Searching for own system entry");
        final ServiceRegistryResponseDTO onboardingEntry = driver.findByServiceRegistry(CoreSystemService.ONBOARDING_WITH_CERTIFICATE_AND_CSR_SERVICE, false);
        final SystemResponseDTO onboardingSystem = onboardingEntry.getProvider();

        for (CoreSystemService coreSystemService : getRequiredCoreSystemServiceUris()) {
            lookupAndAuthorize(authMgmtUri, onboardingSystem, coreSystemService);
        }
    }

    //-------------------------------------------------------------------------------------------------
	@Override
    protected List<CoreSystemService> getRequiredCoreSystemServiceUris() {
        return List.of(CoreSystemService.CERTIFICATEAUTHORITY_SIGN_SERVICE,
                       CoreSystemService.ORCHESTRATION_SERVICE,
                       CoreSystemService.DEVICEREGISTRY_ONBOARDING_WITH_NAME_SERVICE,
                       CoreSystemService.DEVICEREGISTRY_ONBOARDING_WITH_CSR_SERVICE,
                       CoreSystemService.SYSTEMREGISTRY_ONBOARDING_WITH_NAME_SERVICE,
                       CoreSystemService.SYSTEMREGISTRY_ONBOARDING_WITH_CSR_SERVICE,
                       CoreSystemService.SERVICEREGISTRY_REGISTER_SERVICE);
    }

    //-------------------------------------------------------------------------------------------------
	private void lookupAndAuthorize(final UriComponents authMgmtUri,
                                    final SystemResponseDTO consumer,
                                    final CoreSystemService service) {
        final ServiceRegistryResponseDTO serviceEntry = driver.findByServiceRegistry(service, false);
        final SystemResponseDTO systemEntry = serviceEntry.getProvider();

        int retryCount = 0;

        logger.info("Creating authorization rule for {}", service);

        final var authRequest = new AuthorizationIntraCloudRequestDTO();
        authRequest.setConsumerId(consumer.getId());
        authRequest.setProviderIds(Collections.singletonList(systemEntry.getId()));
        authRequest.setInterfaceIds(serviceEntry.getInterfaces().stream().map(ServiceInterfaceResponseDTO::getId).collect(Collectors.toList()));
        authRequest.setServiceDefinitionIds(Collections.singletonList(serviceEntry.getServiceDefinition().getId()));
        try {
            httpService.sendRequest(authMgmtUri, HttpMethod.POST, AuthorizationIntraCloudListResponseDTO.class, authRequest);
        } catch (final ArrowheadException e) {
            if (retryCount++ > MAX_RETRIES) {
                throw e;
            } else {
                logger.info("Unable to retrieve service {}: {}. Retrying in {}ms", service.getServiceDefinition(), e.getMessage(), SLEEP_PERIOD);
                sleep();
            }
        }
    }

    //-------------------------------------------------------------------------------------------------
	private void sleep() {
        try {
            Thread.sleep(SLEEP_PERIOD);
        } catch (InterruptedException e) {
            logger.warn(e.getMessage());
        }
    }
}