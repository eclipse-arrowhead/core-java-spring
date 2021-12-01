/********************************************************************************
* Copyright (c) 2021 Bosch.IO GmbH[ and others]
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0.
*
* SPDX-License-Identifier: EPL-2.0
********************************************************************************/

package eu.arrowhead.core.hbconfmgr.config;

import java.io.IOException;
import java.util.ServiceConfigurationError;

import javax.annotation.PostConstruct;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManagerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import eu.arrowhead.core.hbconfmgr.Constants;
import eu.arrowhead.core.hbconfmgr.arrowhead.ArrowheadAuthorizationSystemClient;
import eu.arrowhead.core.hbconfmgr.arrowhead.ArrowheadServiceRegistryClient;
import eu.arrowhead.core.hbconfmgr.arrowhead.model.request.ServiceQueryFormDTO;
import eu.arrowhead.core.hbconfmgr.arrowhead.model.response.ServiceQueryResultDTO;
import eu.arrowhead.core.hbconfmgr.arrowhead.model.response.ServiceRegistryResponseDTO;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import lombok.extern.log4j.Log4j2;

/**
 * This class provides the clients for interaction with Arrowhead via beans managed by the Spring container.
 */
@Log4j2
@Configuration
public class ArrowheadConfig {
	
	private static final int RETRIES = 3;
	private static final int PERIOD = 5000;
	
    @Value(Constants.SSL_ENABLED)
    private boolean sslEnabled;
    
    @Autowired
    private KeyManagerFactory keyManagerFactory;
    
    @Autowired
    private TrustManagerFactory trustManagerFactory;
    
    private final String authorizationSystemProtocol = Constants.HTTPS + "://";
    private String authorizationSystemAddress;
    private int authorizationSystemPort;
    
    private final String serviceRegistryProtocol = Constants.HTTPS + "://";;
    
    @Value(Constants.SERVICE_REGISTRY_ADDRESS)
    private String serviceRegistryAddress;

    @Value(Constants.SERVICE_REGISTRY_PORT)
    private int serviceRegistryPort;

    private SslContext sslContext;
    
    @PostConstruct
    public void init() throws IOException, InterruptedException {
    	final ArrowheadServiceRegistryClient srClient = arrowheadServiceRegistryClient();
		for (int i = 0; i <= RETRIES; ++i) {
			try {
				final ServiceQueryFormDTO form = ServiceQueryFormDTO.builder()
																	.serviceDefinitionRequirement(Constants.CORE_SERVICE_AUTH_PUBLIC_KEY)
																	.build();
				
				final ServiceQueryResultDTO response = srClient.queryService(form);
				
				if (!response.getServiceQueryData().isEmpty()) {
					final ServiceRegistryResponseDTO authResponse = response.getServiceQueryData().get(0);
					this.authorizationSystemAddress = authResponse.getProvider().getAddress();
					this.authorizationSystemPort = authResponse.getProvider().getPort();
					return;
				} else if (i >= RETRIES) {
					log.error("Authorization system is not accessible.");
					throw new ServiceConfigurationError("HawkBit Configuration Manager cannot work without the Authorization core system.");
				} else {
					log.info("Authorization system is unavailable at the moment, retrying in {} seconds...", PERIOD / 1000);
					Thread.sleep(PERIOD);
				}
			} catch (final Exception e) {
				if (i >= RETRIES) {
					log.error("Service Registry is not accessible.", e);
					throw e;
				} else {
					log.info("Service Registry is unavailable at the moment, retrying in {} seconds...", PERIOD / 1000);
					Thread.sleep(PERIOD);
				}
			}
		}
    }

    @Bean
    public ArrowheadAuthorizationSystemClient arrowheadAuthorizationSystemClient() throws IOException {
        final String baseUrl = this.authorizationSystemProtocol + this.authorizationSystemAddress + ":" + this.authorizationSystemPort;
        log.debug("Registering bean for ArrowheadAuthorizationSystemClient with baseUrl {} and custom ssl context", baseUrl);

        final SslContext sslContext = loadSslContext();
        return new ArrowheadAuthorizationSystemClient(baseUrl, sslContext);
    }

    @Bean
    public ArrowheadServiceRegistryClient arrowheadServiceRegistryClient() throws IOException {
        final String baseUrl = this.serviceRegistryProtocol + this.serviceRegistryAddress + ":" + this.serviceRegistryPort;
        log.debug("Registering bean for ArrowheadServiceRegistryClient with baseUrl {} and custom ssl context", baseUrl);

        final SslContext sslContext = loadSslContext();
        return new ArrowheadServiceRegistryClient(baseUrl, sslContext);
    }

    /**
     * Load the ssl context with a defined client certificate and a defined server certificate.
     * This allows to use self signed certificates.
     *
     * @param keyManagerFactory   contains the client private and public certificates
     * @param trustManagerFactory contains the server public certificate
     * @return a ssl context for a {@link reactor.netty.http.client.HttpClient HttpClient}
     * @throws SSLException if the ssl context could not be loaded correctly
     */
    private SslContext loadSslContext() throws IOException {
    	if (sslContext == null) {
    		sslContext = SslContextBuilder
    							.forClient()
    							.clientAuth(ClientAuth.REQUIRE)
    							.keyManager(keyManagerFactory)
    							.trustManager(trustManagerFactory)
    							.build();
    	}
    	
        return sslContext; 
    }
}