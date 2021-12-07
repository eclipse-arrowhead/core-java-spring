/********************************************************************************
* Copyright (c) 2021 Bosch.IO GmbH[ and others]
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0.
*
* SPDX-License-Identifier: EPL-2.0
********************************************************************************/

package eu.arrowhead.core.confmgr.arrowhead;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.Validator;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import eu.arrowhead.core.confmgr.arrowhead.model.request.PublicKeyEndpointRequestDTO;
import eu.arrowhead.core.confmgr.arrowhead.model.request.ServiceRegistryRequestDTO;
import eu.arrowhead.core.confmgr.arrowhead.model.response.PublicKeyEndpointResponseDTO;
import eu.arrowhead.core.confmgr.arrowhead.model.response.ServiceRegistryResponseDTO;
import io.netty.handler.ssl.SslContext;
import lombok.extern.log4j.Log4j2;
import reactor.netty.http.client.HttpClient;


/**
 * Client class to establish a connection with the Arrowhead Service Registry REST API and transmit requests.
 */
@Log4j2
public class ArrowheadServiceRegistryClient {

    private static final String URI_REGISTER_SERVICE = "/serviceregistry/register";
    private static final String URI_UNREGISTER_SERVICE = "/serviceregistry/unregister";
    private static final String URI_QUERY = "/serviceregistry/query";

    private final WebClient webClient;
    private final String baseUrl;
    private final String authorizationSystemPubKeyServiceDefinition;

    /**
     * Initializes a new rest client to communicate with the Arrowhead Service Registry.
     * Insecure (HTTP) and secure (HTTPS) communication is supported, depending on the baseUrl.
     *
     * @param baseUrl specifies the base url where arrowhead is available
     */
    public ArrowheadServiceRegistryClient(String baseUrl, String authorizationSystemPubKeyServiceDefinition) {
        log.debug("Initialize ArrowheadServiceRegistryClient with baseUrl {}", baseUrl);
        this.webClient = WebClient.create(baseUrl);
        this.baseUrl = baseUrl;
        this.authorizationSystemPubKeyServiceDefinition = authorizationSystemPubKeyServiceDefinition;
    }

    /**
     * Initializes a new rest client to communicate with the Arrowhead Service Registry with customized SSL handling.
     * Insecure (HTTP) and secure (HTTPS) communication is supported, depending on the baseUrl.
     *
     * @param baseUrl    specifies the base url where arrowhead is available
     * @param sslContext specifies the ssl context the web client should use
     */
    public ArrowheadServiceRegistryClient(String baseUrl, String authorizationSystemPubKeyServiceDefinition, SslContext sslContext) {
        log.debug("Initialize ArrowheadServiceRegistryClient with baseUrl {} and customized sslContext", baseUrl);
        HttpClient httpClient = HttpClient.create()
                .secure(sslContextSpec -> sslContextSpec.sslContext(sslContext));
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
        this.baseUrl = baseUrl;
        this.authorizationSystemPubKeyServiceDefinition = authorizationSystemPubKeyServiceDefinition;
    }

    /**
     * Registers a service. A provider is allowed to register only its own services.
     * It means that provider system name and certificate common name must match for successful registration.
     * A more detailed description of this REST endpoint can be found in Github under the
     * <a href="https://github.com/arrowhead-f/core-java-spring#register">client endpoint description for
     * registering a service.</a>
     *
     * @param serviceRegistryRequestDTO contains the information about the service that should be registered
     * @return a string containing the full response body
     * @throws ConstraintViolationException if the serviceRegistryRequestDTO is not valid
     * @throws WebClientResponseException   if the status code is 4xx or 5xx
     */
    public ServiceRegistryResponseDTO registerService(ServiceRegistryRequestDTO serviceRegistryRequestDTO)
            throws ConstraintViolationException, WebClientResponseException {

        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        Set<ConstraintViolation<ServiceRegistryRequestDTO>> violations = validator.validate(serviceRegistryRequestDTO);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        log.debug("Start HTTP POST request against {} on uri: {}", baseUrl, URI_REGISTER_SERVICE);
        return this.webClient
                .post()
                .uri(URI_REGISTER_SERVICE)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(serviceRegistryRequestDTO)
                .retrieve()
                .bodyToMono(ServiceRegistryResponseDTO.class)
                .doOnNext(response -> log.debug("Finished HTTP POST request with response: {}", response))
                .block();
    }

    public void unregisterService(String address, int port, String serviceDefinition, String systemName) {
        log.debug("Start HTTP DELETE request against {} on uri: {}", baseUrl, URI_REGISTER_SERVICE);
        ResponseEntity<Void> result = this.webClient
            .delete()
            .uri(URI_UNREGISTER_SERVICE, uriBuilder -> uriBuilder
                .queryParam("address", address)
                .queryParam("port", port)
                .queryParam("service_definition", serviceDefinition)
                .queryParam("system_name", systemName)
                .build())
            .retrieve()
            .toBodilessEntity()
            .block();
        log.debug(result);
    }

    public PublicKeyEndpointResponseDTO getPublicKeyEndpoint() {
        PublicKeyEndpointRequestDTO publicKeyEndpointRequestDTO = new PublicKeyEndpointRequestDTO();
        publicKeyEndpointRequestDTO.setServiceDefinitionRequirement(authorizationSystemPubKeyServiceDefinition);

        return this.webClient
                .post()
                .uri(URI_QUERY)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(publicKeyEndpointRequestDTO)
                .retrieve()
                .bodyToMono(PublicKeyEndpointResponseDTO.class)
                .doOnNext(response -> log.info("Finished HTTP POST request with response: {}", response))
                .block();
    }

}
