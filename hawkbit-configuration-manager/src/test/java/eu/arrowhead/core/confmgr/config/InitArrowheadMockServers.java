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
import org.mockserver.model.MediaType;


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

        serviceRegistryMockServer.when(request().withPath("/serviceregistry/query"))
            .respond(response().withStatusCode(200).withContentType(MediaType.APPLICATION_JSON).withBody("{  \"serviceQueryData\": [    {      \"id\": 11,      \"serviceDefinition\": {        \"id\": 5,        \"serviceDefinition\": \"auth-public-key\",        \"createdAt\": \"2021-12-06 15:56:14\",        \"updatedAt\": \"2021-12-06 15:56:14\"      },      \"provider\": {        \"id\": 3,        \"systemName\": \"authorization\",        \"address\": \"localhost\",        \"port\": 8445,        \"authenticationInfo\": \"MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAtwYicVE/GaL0dp35sJ68IxspkRtpfe5ZNhYrdEXWKofyY7Vmnc3S8INSdUC7k699GN3szaFloRudiYG2G+RZrSqJHm5DZHBSMFg27ruG3PiwIpGIOr8AsfUmzNa8EM9MAd35QmVC2YjlQ6/UEOgY7wzHe1gLYn8QleLqHT0z/rRRMAY7FcVtAofTINzdFkJ4jWkwaDG10oldWV08LLzTs9V/FSaaqEWYodCwTMVf/x9pD1PLMIB0nWER8c/FOifswsVW9+a++HRxji+m05wDXb48OnbPMrpEk+FHXmxLyzvxu03kmtDzH6DNYzonXwGxeOaEPDN9j6P6TLuKwaEf8QIDAQAB\",        \"createdAt\": \"2021-12-06 15:56:13\",        \"updatedAt\": \"2021-12-06 15:56:13\"      },      \"serviceUri\": \"/authorization/publickey\",      \"secure\": \"CERTIFICATE\",      \"version\": 1,      \"interfaces\": [        {          \"id\": 1,          \"interfaceName\": \"HTTP-SECURE-JSON\",          \"createdAt\": \"2021-12-06 14:54:58\",          \"updatedAt\": \"2021-12-06 14:54:58\"        }      ],      \"createdAt\": \"2021-12-06 17:43:19\",      \"updatedAt\": \"2021-12-06 17:43:19\"    }  ],  \"unfilteredHits\": 1}"));
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
