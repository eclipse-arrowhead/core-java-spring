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

import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.util.List;

import org.mockserver.configuration.ConfigurationProperties;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.MediaType;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.arrowhead.core.hbconfmgr.arrowhead.model.response.ServiceQueryResultDTO;
import eu.arrowhead.core.hbconfmgr.arrowhead.model.response.ServiceRegistryResponseDTO;
import eu.arrowhead.core.hbconfmgr.arrowhead.model.response.SystemResponseDTO;


public class InitArrowheadMockServers {

    private ClientAndServer serviceRegistryMockServer;
    private ClientAndServer authorizationSystemMockServer;

    public void setUp() throws JsonProcessingException {
        startServiceRegistryMockServer(true);
        startAuthorizationSystemMockServer(true);
    }

    public void setUp(final Boolean useCertificates) throws JsonProcessingException {
        startServiceRegistryMockServer(useCertificates);
        startAuthorizationSystemMockServer(useCertificates);
    }

    private void startServiceRegistryMockServer(final Boolean useCertificates) throws JsonProcessingException {
        if (useCertificates) {
            final String certificateAuthorityKeyPath = PropertiesExtractor.getProperty("mockserver.certificateAuthorityPrivateKey");
            final String certificateAuthorityCertificatePath = PropertiesExtractor.getProperty("mockserver.certificateAuthorityCertificate");
            final String serviceRegistryKeyPath = PropertiesExtractor.getProperty("mockserver.sreg.privateKeyPath");
            final String serviceRegistryCertPath = PropertiesExtractor.getProperty("mockserver.sreg.x509CertificatePath");

            ConfigurationProperties.certificateAuthorityPrivateKey(certificateAuthorityKeyPath);
            ConfigurationProperties.certificateAuthorityCertificate(certificateAuthorityCertificatePath);
            ConfigurationProperties.privateKeyPath(serviceRegistryKeyPath);
            ConfigurationProperties.x509CertificatePath(serviceRegistryCertPath);
        } else {
            ConfigurationProperties.certificateAuthorityPrivateKey("");
            ConfigurationProperties.certificateAuthorityCertificate("");
            ConfigurationProperties.privateKeyPath("");
            ConfigurationProperties.x509CertificatePath("");
        }

        serviceRegistryMockServer = ClientAndServer.startClientAndServer(8443);

        serviceRegistryMockServer.when(request().withPath("/serviceregistry/register"))
            .respond(response().withStatusCode(201));
        serviceRegistryMockServer.when(request().withPath("/serviceregistry/unregister"))
            .respond(response().withStatusCode(200));
        final ServiceRegistryResponseDTO response = ServiceRegistryResponseDTO.builder()
        																	  .provider(SystemResponseDTO.builder()
        																			  					 .address("127.0.0.1")
        																			  					 .port(8445)
        																			  					 .build())
        																	  .build();
        final ServiceQueryResultDTO result = ServiceQueryResultDTO.builder()
        														  .unfilteredHits(1)
        														  .serviceQueryData(List.of(response))
        														  .build();
        final ObjectMapper mapper = new ObjectMapper();
        serviceRegistryMockServer.when(request().withPath("/serviceregistry/query"))
            .respond(response().withStatusCode(200)
            				   .withContentType(MediaType.APPLICATION_JSON_UTF_8)
            		           .withBody(mapper.writeValueAsString(result)));
        
    }

    private void startAuthorizationSystemMockServer(final Boolean useCertificates) {
        if (useCertificates) {
            final String certificateAuthorityKeyPath = PropertiesExtractor.getProperty("mockserver.certificateAuthorityPrivateKey");
            final String certificateAuthorityCertificatePath = PropertiesExtractor.getProperty("mockserver.certificateAuthorityCertificate");
            final String authorizationSystemKeyPath = PropertiesExtractor.getProperty("mockserver.auth.privateKeyPath");
            final String authorizationSystemCertPath = PropertiesExtractor.getProperty("mockserver.auth.x509CertificatePath");

            ConfigurationProperties.certificateAuthorityPrivateKey(certificateAuthorityKeyPath);
            ConfigurationProperties.certificateAuthorityCertificate(certificateAuthorityCertificatePath);
            ConfigurationProperties.privateKeyPath(authorizationSystemKeyPath);
            ConfigurationProperties.x509CertificatePath(authorizationSystemCertPath);
        }

        final String authorizationSystemPubKey = PropertiesExtractor.getProperty("mockserver.auth.pubKeyResponse");

        authorizationSystemMockServer = ClientAndServer.startClientAndServer(8445);

        authorizationSystemMockServer.when(
            request()
                .withPath("/authorization/publickey")
        )
        .respond(
            response()
                .withBody("\"" + authorizationSystemPubKey + "\"")
        );
    }

    public void shutDown() {
        serviceRegistryMockServer.stop();
        authorizationSystemMockServer.stop();
    }

}
