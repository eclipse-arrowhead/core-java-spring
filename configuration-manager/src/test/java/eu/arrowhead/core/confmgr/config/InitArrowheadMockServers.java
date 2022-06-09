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

import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import org.mockserver.configuration.ConfigurationProperties;
import org.mockserver.integration.ClientAndServer;


public class InitArrowheadMockServers {

    ClientAndServer serviceRegistryMockServer;
    ClientAndServer authorizationSystemMockServer;

    public void setUp() {
        startServiceRegistryMockServer(true);
        startAuthorizationSystemMockServer(true);
    }

    public void setUp(Boolean useCertificates) {
        startServiceRegistryMockServer(useCertificates);
        startAuthorizationSystemMockServer(useCertificates);
    }

    private void startServiceRegistryMockServer(Boolean useCertificates) {
        if(useCertificates) {
            String certificateAuthorityKeyPath = PropertiesExtractor.getProperty("mockserver.certificateAuthorityPrivateKey");
            String certificateAuthorityCertificatePath = PropertiesExtractor.getProperty("mockserver.certificateAuthorityCertificate");
            String serviceRegistryKeyPath = PropertiesExtractor.getProperty("mockserver.sreg.privateKeyPath");
            String serviceRegistryCertPath = PropertiesExtractor.getProperty("mockserver.sreg.x509CertificatePath");

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
            .respond(response().withStatusCode(400));
    }

    private void startAuthorizationSystemMockServer(Boolean useCertificates) {
        if(useCertificates) {
            String certificateAuthorityKeyPath = PropertiesExtractor.getProperty("mockserver.certificateAuthorityPrivateKey");
            String certificateAuthorityCertificatePath = PropertiesExtractor.getProperty("mockserver.certificateAuthorityCertificate");
            String authorizationSystemKeyPath = PropertiesExtractor.getProperty("mockserver.auth.privateKeyPath");
            String authorizationSystemCertPath = PropertiesExtractor.getProperty("mockserver.auth.x509CertificatePath");

            ConfigurationProperties.certificateAuthorityPrivateKey(certificateAuthorityKeyPath);
            ConfigurationProperties.certificateAuthorityCertificate(certificateAuthorityCertificatePath);
            ConfigurationProperties.privateKeyPath(authorizationSystemKeyPath);
            ConfigurationProperties.x509CertificatePath(authorizationSystemCertPath);
        }

        String authorizationSystemPubKey = PropertiesExtractor.getProperty("mockserver.auth.pubKeyResponse");

        authorizationSystemMockServer = ClientAndServer.startClientAndServer(8445);

        authorizationSystemMockServer.when(
            request()
                .withPath("/authorization/publickey")
        )
        .respond(
            response()
                .withBody(authorizationSystemPubKey)
        );
    }

    public void shutDown() {
        serviceRegistryMockServer.stop();
        authorizationSystemMockServer.stop();
    }

}
