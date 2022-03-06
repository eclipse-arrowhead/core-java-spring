/********************************************************************************
* Copyright (c) 2021 Bosch.IO GmbH[ and others]
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0.
*
* SPDX-License-Identifier: EPL-2.0
********************************************************************************/

package eu.arrowhead.core.hbconfmgr;

import java.io.IOException;
import java.security.PublicKey;
import java.util.Base64;
import java.util.Collections;

import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import eu.arrowhead.core.hbconfmgr.arrowhead.ArrowheadServiceRegistryClient;
import eu.arrowhead.core.hbconfmgr.arrowhead.model.request.ServiceRegistryRequestDTO;
import eu.arrowhead.core.hbconfmgr.arrowhead.model.request.SystemRequestDTO;
import eu.arrowhead.core.hbconfmgr.hawkbit.HawkbitDmfConsumer;
import eu.arrowhead.core.hbconfmgr.properties.SystemProperties;
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
    public Initialization(final ArrowheadServiceRegistryClient arrowheadServiceRegistryClient, final HawkbitDmfConsumer hawkbitDmfConsumer, final SystemProperties systemProperties, final PublicKey publicKey) {
        this.arrowheadServiceRegistryClient = arrowheadServiceRegistryClient;
        this.hawkbitDmfConsumer = hawkbitDmfConsumer;
        this.systemProperties = systemProperties;
        this.publicKey = publicKey;
    }

    @Override
    public void run(final ApplicationArguments args) throws Exception {
        registerOwnSystemInArrowhead();
        connectToHawkBit();
        log.info("Arrowhead HawkBit Configuration Manager initialization done.");
    }
    
	@PreDestroy
    public void destroy() {
		try {
			this.arrowheadServiceRegistryClient.unregisterService(this.systemProperties.getProvidedServiceDefinition(), this.systemProperties.getName(), this.systemProperties.getAddress(), this.systemProperties.getPort(),
															      this.systemProperties.getProvidedServiceUri());
		} catch (final WebClientResponseException e) {
	        log.error("Error during unregistration of own system in Arrowhead", e);
		}
    }

    private void registerOwnSystemInArrowhead() {
        final String publicKeyString = Base64.getEncoder().encodeToString(this.publicKey.getEncoded());

        final ServiceRegistryRequestDTO requestDTO = ServiceRegistryRequestDTO.builder()
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
        } catch (final WebClientResponseException e) {
            if (HttpStatus.BAD_REQUEST.equals(e.getStatusCode())) {
            	log.warn("Own system is already registered in Arrowhead");
            	this.arrowheadServiceRegistryClient.unregisterService(this.systemProperties.getProvidedServiceDefinition(), this.systemProperties.getName(), this.systemProperties.getAddress(), this.systemProperties.getPort(),
            														  this.systemProperties.getProvidedServiceUri());
            	this.arrowheadServiceRegistryClient.registerService(requestDTO);
            } else {
                log.error("Error during registration of own system in Arrowhead", e);
            }
        } catch (final Exception e) {
            log.error("Error during registration of own system in Arrowhead", e);
        }
    }

    private void connectToHawkBit() {
        try {
            this.hawkbitDmfConsumer.subscribeToDownloadEvents();
        } catch (final IOException e) {
            log.error("Could not subscribe to Hawkbit DMF API");
        }
    }
}