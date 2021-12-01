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

import java.util.Optional;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.Validator;

import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import eu.arrowhead.core.hbconfmgr.arrowhead.model.request.ServiceQueryFormDTO;
import eu.arrowhead.core.hbconfmgr.arrowhead.model.request.ServiceRegistryRequestDTO;
import eu.arrowhead.core.hbconfmgr.arrowhead.model.response.ServiceQueryResultDTO;
import eu.arrowhead.core.hbconfmgr.arrowhead.model.response.ServiceRegistryResponseDTO;
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
    private static final String URI_QUERY_SERVICE = "/serviceregistry/query";
    
    private static final String UNREGISTER_REQUEST_PARAM_SYSTEM_NAME = "system_name";
    private static final String UNREGISTER_REQUEST_PARAM_ADDRESS = "address";
    private static final String UNREGISTER_REQUEST_PARAM_PORT = "port";
    private static final String UNREGISTER_REQUEST_PARAM_SERVICE_DEFINITION = "service_definition";
    private static final String UNREGISTER_REQUEST_PARAM_SERVICE_URI = "service_uri";
    
    private static final int SYSTEM_PORT_RANGE_MIN = 0;
    private static final int SYSTEM_PORT_RANGE_MAX = 65535;

    private final WebClient webClient;
    private final String baseUrl;

    /**
     * Initializes a new rest client to communicate with the Arrowhead Service Registry.
     * Insecure (HTTP) and secure (HTTPS) communication is supported, depending on the baseUrl.
     *
     * @param baseUrl specifies the base url where arrowhead is available
     */
    public ArrowheadServiceRegistryClient(final String baseUrl) {
        log.debug("Initialize ArrowheadServiceRegistryClient with baseUrl {}", baseUrl);
        
        this.webClient = WebClient.create(baseUrl);
        this.baseUrl = baseUrl;
    }

    /**
     * Initializes a new rest client to communicate with the Arrowhead Service Registry with customized SSL handling.
     * Insecure (HTTP) and secure (HTTPS) communication is supported, depending on the baseUrl.
     *
     * @param baseUrl    specifies the base url where arrowhead is available
     * @param sslContext specifies the ssl context the web client should use
     */
    public ArrowheadServiceRegistryClient(final String baseUrl, final SslContext sslContext) {
        log.debug("Initialize ArrowheadServiceRegistryClient with baseUrl {} and customized sslContext", baseUrl);
        
        final HttpClient httpClient = HttpClient.create()
                .secure(sslContextSpec -> sslContextSpec.sslContext(sslContext));
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
        this.baseUrl = baseUrl;
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
    public ServiceRegistryResponseDTO registerService(final ServiceRegistryRequestDTO serviceRegistryRequestDTO) throws ConstraintViolationException, WebClientResponseException {
        final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        final Set<ConstraintViolation<ServiceRegistryRequestDTO>> violations = validator.validate(serviceRegistryRequestDTO);
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
    
    public void unregisterService(final String serviceDefinition, final String providerName, final String providerAddress, final int providerPort, final String serviceUri) {
		if (isEmpty(serviceDefinition)) {
			throw new RuntimeException("Service definition is blank");
		}
		
		if (isEmpty(providerName)) {
			throw new RuntimeException("Name of the provider system is blank");
		}

		if (isEmpty(providerAddress)) {
			throw new RuntimeException("Address of the provider system is blank");
		}

		if (providerPort < SYSTEM_PORT_RANGE_MIN || providerPort > SYSTEM_PORT_RANGE_MAX) {
			throw new RuntimeException("Port must be between " + SYSTEM_PORT_RANGE_MIN + " and " + SYSTEM_PORT_RANGE_MAX + ".");
		}
		
		log.debug("Start HTTP DELETE request against {} on uri: {}", baseUrl, URI_UNREGISTER_SERVICE);
		
		this.webClient
			.delete()
			.uri(uriBuilder -> uriBuilder
				   .path(URI_UNREGISTER_SERVICE)
				   .queryParam(UNREGISTER_REQUEST_PARAM_SERVICE_DEFINITION, serviceDefinition)
				   .queryParam(UNREGISTER_REQUEST_PARAM_SYSTEM_NAME, providerName)
				   .queryParam(UNREGISTER_REQUEST_PARAM_ADDRESS, providerAddress)
				   .queryParam(UNREGISTER_REQUEST_PARAM_PORT, providerPort)
				   .queryParamIfPresent(UNREGISTER_REQUEST_PARAM_SERVICE_URI, Optional.ofNullable(serviceUri))
				   .build())
            .retrieve()
            .bodyToMono(Void.class)
            .doOnNext(response -> log.debug("Finished HTTP DELETE request with response: {}", response))
            .block();	   
    }
    
    public ServiceQueryResultDTO queryService(final ServiceQueryFormDTO form) throws ConstraintViolationException {
    	final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    	final Set<ConstraintViolation<ServiceQueryFormDTO>> violations = validator.validate(form);
    	if (!violations.isEmpty()) {
    		throw new ConstraintViolationException(violations);
    	}
    	
    	log.debug("Start HTTP POST request against {} on uri: {}", baseUrl, URI_QUERY_SERVICE);
    	
    	return this.webClient
    			   .post()
    			   .uri(URI_QUERY_SERVICE)
    			   .contentType(MediaType.APPLICATION_JSON)
    			   .bodyValue(form)
    			   .retrieve()
    			   .bodyToMono(ServiceQueryResultDTO.class)
    			   .doOnNext(response -> log.debug("Finished HTTP POST request with response: {}", response))
    			   .block();
    }

	//-------------------------------------------------------------------------------------------------
	private boolean isEmpty(final String str) {
		return str == null || str.trim().isEmpty();
	}
}