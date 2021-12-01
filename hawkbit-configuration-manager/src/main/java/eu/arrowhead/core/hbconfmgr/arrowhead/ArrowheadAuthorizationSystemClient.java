/********************************************************************************
* Copyright (c) 2021 Bosch.IO GmbH[ and others]
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0.
*
* SPDX-License-Identifier: EPL-2.0
********************************************************************************/

package eu.arrowhead.core.hbconfmgr.arrowhead;

import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.netty.handler.ssl.SslContext;
import lombok.extern.log4j.Log4j2;
import reactor.netty.http.client.HttpClient;


/**
 * Client class to directly interact with the arrowhead authorization system.
 * Used solely to request its public key for JWT validation.
 */
@Log4j2
public class ArrowheadAuthorizationSystemClient {

    private static final String URI_PUBLIC_KEY = "/authorization/publickey";

    private final WebClient webClient;
    private final String baseUrl;

    /**
     * Initializes a new rest client to communicate with the Arrowhead Authorization System.
     * Insecure (HTTP) and secure (HTTPS) communication is supported, depending on the baseUrl.
     *
     * @param baseUrl specifies the base url where arrowhead is available
     */
    public ArrowheadAuthorizationSystemClient(final String baseUrl) {
        log.debug("Initialize ArrowheadAuthorizationSystemClient with baseUrl {}", baseUrl);
        
        this.webClient = WebClient.create(baseUrl);
        this.baseUrl = baseUrl;
    }

    /**
     * Initializes a new rest client to communicate with the Arrowhead Authorization System with customized SSL
     * handling. Insecure (HTTP) and secure (HTTPS) communication is supported, depending on the baseUrl.
     *
     * @param baseUrl    specifies the base url where arrowhead is available
     * @param sslContext specifies the ssl context the web client should use
     */
    public ArrowheadAuthorizationSystemClient(final String baseUrl, final SslContext sslContext) {
        log.debug("Initialize ArrowheadAuthorizationSystemClient with baseUrl {} and customized sslContext", baseUrl);
        
        final HttpClient httpClient = HttpClient.create()
                .secure(sslContextSpec -> sslContextSpec.sslContext(sslContext));
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
        this.baseUrl = baseUrl;
    }

    /**
     * Returns the public key of the Authorization core service as a (Base64 encoded) text.
     * <p>This service is necessary for providers if they want to utilize the token based security.
     * A more detailed description of this REST endpoint can be found in Github under the
     * <a href="https://github.com/eclipse-arrowhead/core-java-spring#get-public-key">client endpoint description for
     * getting the public key.</a>
     *
     * @return a string containing the (Base64 encoded) public key
     * @throws WebClientResponseException if the status code is 4xx or 5xx
     */
    public String getPublicKey() throws WebClientResponseException {
        log.debug("Start HTTP GET request against {} on uri: {}", baseUrl, URI_PUBLIC_KEY);
        
        final String key = this.webClient
                		 .get()
                		 .uri(URI_PUBLIC_KEY)
                		 .retrieve()
                		 .bodyToMono(String.class)
                		 .doOnNext(response -> log.debug("Finished HTTP GET request with response: {}", response))
                		 .block();

        try {
        	final ObjectMapper mapper = new ObjectMapper();
			return mapper.readValue(key, String.class);
		} catch (final JsonProcessingException e) {
			log.error("Invalid authorization public key", e);
			
			return null;
		}
    }
}