/********************************************************************************
 * Copyright (c) 2019 AITIA
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   AITIA - implementation
 *   Arrowhead Consortia - conceptualization
 ********************************************************************************/

package eu.arrowhead.core.serviceregistry;

import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import eu.arrowhead.common.ApplicationInitListener;
import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreDefaults;
import eu.arrowhead.common.core.CoreSystem;
import eu.arrowhead.common.core.CoreSystemService;
import eu.arrowhead.common.database.entity.System;
import eu.arrowhead.common.database.service.CommonDBService;
import eu.arrowhead.common.dto.shared.ServiceRegistryRequestDTO;
import eu.arrowhead.common.dto.shared.ServiceSecurityType;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.DataNotFoundException;
import eu.arrowhead.core.serviceregistry.database.service.ServiceRegistryDBService;

@Component
public class ServiceRegistryApplicationInitListener extends ApplicationInitListener {

    //=================================================================================================
    // members

    @Autowired
    private CommonDBService commonDBService;

    @Autowired
    private ServiceRegistryDBService serviceRegistryDBService;

    //=================================================================================================
    // assistant methods

    //-------------------------------------------------------------------------------------------------
    @Override
    protected void customInit(final ContextRefreshedEvent event) {
        logger.debug("customInit started...");
        if (!isOwnCloudRegistered()) {
            registerOwnCloud(event.getApplicationContext());
        }

        try {
            final String name = coreSystemRegistrationProperties.getCoreSystem().name().toLowerCase();
            final List<System> oldSystems = serviceRegistryDBService.getSystemByName(name);
            if (!oldSystems.isEmpty()) {
                for (final System system : oldSystems) {
                    removeServiceRegistryEntries(system);
                    serviceRegistryDBService.removeSystemById(system.getId());
                }
            }

            final String authInfo = sslProperties.isSslEnabled() ? Base64.getEncoder().encodeToString(publicKey.getEncoded()) : null;
            final SystemRequestDTO systemRequestDTO = new SystemRequestDTO(name, coreSystemRegistrationProperties.getCoreSystemDomainName(),
                                                                           coreSystemRegistrationProperties.getCoreSystemDomainPort(), authInfo, null);

            final ServiceSecurityType securityType = sslProperties.isSslEnabled() ? ServiceSecurityType.CERTIFICATE : ServiceSecurityType.NOT_SECURE;
            final String serviceInterface = sslProperties.isSslEnabled() ? CommonConstants.HTTP_SECURE_JSON : CommonConstants.HTTP_INSECURE_JSON;

            for (final CoreSystemService service : CoreSystem.SERVICEREGISTRY.getServices()) {
                final ServiceRegistryRequestDTO registryRequest = new ServiceRegistryRequestDTO();
                registryRequest.setProviderSystem(systemRequestDTO);
                registryRequest.setServiceDefinition(service.getServiceDefinition());
                registryRequest.setInterfaces(List.of(serviceInterface));
                registryRequest.setServiceUri(service.getServiceUri());
                registryRequest.setSecure(securityType.name());

                serviceRegistryDBService.registerServiceResponse(registryRequest);
            }
        } catch (final ArrowheadException ex) {
            logger.error("Can't registrate {} as a system.", coreSystemRegistrationProperties.getCoreSystem().name());
            logger.debug("Stacktrace", ex);
        }
        
        try {
        	serviceRegistryDBService.calculateSystemAddressTypeIfNecessary();
        } catch (final ArrowheadException ex) {
        	logger.warn("Problem occurs during calculating system address types: {}", ex.getMessage());
        	logger.debug("Stacktrace", ex);
        }
    }

    //-------------------------------------------------------------------------------------------------
    private void removeServiceRegistryEntries(final System system) {
        for (final CoreSystemService service : CoreSystem.SERVICEREGISTRY.getServices()) {
        	try {
				serviceRegistryDBService.removeServiceRegistry(service.getServiceDefinition(), system.getSystemName(), system.getAddress(), system.getPort(), service.getServiceUri());
			} catch (final Exception ex) {
        		// ignore
			}
        }
    }

    //-------------------------------------------------------------------------------------------------
    private boolean isOwnCloudRegistered() {
        logger.debug("isOwnCloudRegistered started...");
        try {
            commonDBService.getOwnCloud(sslProperties.isSslEnabled());
            return true;
        } catch (final DataNotFoundException ex) {
            return false;
        }
    }

    //-------------------------------------------------------------------------------------------------
    private void registerOwnCloud(final ApplicationContext appContext) {
        logger.debug("registerOwnCloud started...");

        if (!standaloneMode) {
            String name = CoreDefaults.DEFAULT_OWN_CLOUD_NAME;
            String operator = CoreDefaults.DEFAULT_OWN_CLOUD_OPERATOR;

            if (sslProperties.isSslEnabled()) {
                @SuppressWarnings("unchecked") final Map<String, Object> context = appContext.getBean(CommonConstants.ARROWHEAD_CONTEXT, Map.class);
                final String serverCN = (String) context.get(CommonConstants.SERVER_COMMON_NAME);
                final String[] serverFields = serverCN.split("\\.");
                name = serverFields[1];
                operator = serverFields[2];
            }

            commonDBService.insertOwnCloud(operator, name, sslProperties.isSslEnabled(), null);
            logger.info("{}.{} own cloud is registered in {} mode.", name, operator, getModeString());
        }
    }
}