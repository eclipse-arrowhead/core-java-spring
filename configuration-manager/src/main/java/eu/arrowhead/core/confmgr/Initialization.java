/********************************************************************************
* Copyright (c) 2021 Bosch.IO GmbH[ and others]
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0.
*
* SPDX-License-Identifier: EPL-2.0
********************************************************************************/

package eu.arrowhead.core.confmgr;

import java.io.IOException;
import java.security.PublicKey;
import java.util.Base64;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import eu.arrowhead.core.confmgr.arrowhead.ArrowheadServiceRegistryClient;
import eu.arrowhead.core.confmgr.arrowhead.model.request.ServiceRegistryRequestDTO;
import eu.arrowhead.core.confmgr.arrowhead.model.request.SystemRequestDTO;
import eu.arrowhead.core.confmgr.hawkbit.HawkbitDmfConsumer;
import eu.arrowhead.core.confmgr.properties.SystemProperties;
import lombok.extern.log4j.Log4j2;

/**
 * This class runs during the startup of the configuration system. It contains
 * all logic for initialization, like the registration of the configuration
 * system in Arrowhead.
 */
@Log4j2
@Component
public class Initialization implements ApplicationRunner {
    private final HawkbitDmfConsumer hawkbitDmfConsumer;
    private final ArrowheadServiceRegistryClient arrowheadServiceRegistryClient;
    private final SystemProperties systemProperties;
    private final PublicKey publicKey;

    @Autowired
    public Initialization(ArrowheadServiceRegistryClient arrowheadServiceRegistryClient, HawkbitDmfConsumer hawkbitDmfConsumer, SystemProperties systemProperties, PublicKey publicKey) {
        this.arrowheadServiceRegistryClient = arrowheadServiceRegistryClient;
        this.hawkbitDmfConsumer = hawkbitDmfConsumer;
        this.systemProperties = systemProperties;
        this.publicKey = publicKey;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        registerOwnSystemInArrowhead();
        connectToHawkBit();
    }

    private void registerOwnSystemInArrowhead() {
        String publicKeyString = Base64.getEncoder().encodeToString(this.publicKey.getEncoded());

        ServiceRegistryRequestDTO requestDTO = ServiceRegistryRequestDTO.builder()
                .serviceDefinition(this.systemProperties.getProvidedServiceDefinition())
                .providerSystem(SystemRequestDTO.builder()
                        .systemName(this.systemProperties.getName())
                        .address(this.systemProperties.getAddress())
                        .port(this.systemProperties.getPort())
                        .authenticationInfo(publicKeyString)
                        .build())
                .serviceUri(this.systemProperties.getProvidedServiceUri())
                .secure(ServiceRegistryRequestDTO.SecurityLevel.TOKEN)
                .version(this.systemProperties.getProvidedServiceVersion())
                .interfaces(Collections.singletonList(this.systemProperties.getProvidedServiceInterface()))
                .build();

        try {
            log.info("Registering own system in Arrowhead");
            this.arrowheadServiceRegistryClient.registerService(requestDTO);
        } catch (WebClientResponseException e) {
            if (HttpStatus.BAD_REQUEST.equals(e.getStatusCode())) {
                log.warn("Own system is already registered in Arrowhead");
            } else {
                log.error("Error during registration of own system in Arrowhead", e);
            }
        } catch (Exception e) {
            log.error("Error during registration of own system in Arrowhead", e);
        }
    }

    private void connectToHawkBit() {
        try {
            this.hawkbitDmfConsumer.subscribeToDownloadEvents();
        } catch (IOException e) {
            log.error("Could not subscribe to Hawkbit DMF API");
        }
    }

}
