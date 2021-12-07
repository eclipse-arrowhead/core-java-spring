/********************************************************************************
* Copyright (c) 2021 Bosch.IO GmbH[ and others]
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0.
*
* SPDX-License-Identifier: EPL-2.0
********************************************************************************/

package eu.arrowhead.core.confmgr.config;

import java.io.IOException;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManagerFactory;
import javax.servlet.ServletContextListener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import eu.arrowhead.core.confmgr.CleanUp;
import eu.arrowhead.core.confmgr.arrowhead.ArrowheadAuthorizationSystemClient;
import eu.arrowhead.core.confmgr.arrowhead.ArrowheadServiceRegistryClient;
import eu.arrowhead.core.confmgr.properties.SystemProperties;
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

    @Value("${arrowheadAuthorizationSystem.pubKeyServiceDefinition}")
    private String authorizationSystemPubKeyServiceDefinition;

    @Value("${arrowheadServiceRegistry.protocol}")
    private String serviceRegistryProtocol;

    @Value("${arrowheadServiceRegistry.address}")
    private String serviceRegistryAddress;

    @Value("${arrowheadServiceRegistry.port}")
    private String serviceRegistryPort;

    @Autowired
    private SystemProperties systemProperties;

    @Bean
    public ArrowheadAuthorizationSystemClient arrowheadAuthorizationSystemClient(
            KeyManagerFactory keyManagerFactory,
            TrustManagerFactory trustManagerFactory) throws IOException {
        SslContext sslContext = loadSslContext(keyManagerFactory, trustManagerFactory);
        return new ArrowheadAuthorizationSystemClient(sslContext);
    }

    @Bean
    public ArrowheadServiceRegistryClient arrowheadServiceRegistryClient(
            KeyManagerFactory keyManagerFactory,
            TrustManagerFactory trustManagerFactory) throws IOException {
        String baseUrl = this.serviceRegistryProtocol + this.serviceRegistryAddress + ":" + this.serviceRegistryPort;
        log.debug("Registering bean for ArrowheadServiceRegistryClient with baseUrl {} and custom ssl context", baseUrl);

        SslContext sslContext = loadSslContext(keyManagerFactory, trustManagerFactory);
        return new ArrowheadServiceRegistryClient(baseUrl, authorizationSystemPubKeyServiceDefinition, sslContext);
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
    private SslContext loadSslContext(KeyManagerFactory keyManagerFactory, TrustManagerFactory trustManagerFactory)
            throws IOException {
        return SslContextBuilder
                .forClient()
                .clientAuth(ClientAuth.REQUIRE)
                .keyManager(keyManagerFactory)
                .trustManager(trustManagerFactory)
                .build();
    }

    @Bean
    @Autowired
    ServletListenerRegistrationBean<ServletContextListener> servletListener(ArrowheadServiceRegistryClient arrowheadServiceRegistryClient) {
        ServletListenerRegistrationBean<ServletContextListener> srb
        = new ServletListenerRegistrationBean<>();
        srb.setListener(new CleanUp(arrowheadServiceRegistryClient, systemProperties));
        return srb;
    }
}
