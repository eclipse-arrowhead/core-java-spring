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

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.channels.ClosedChannelException;
import java.util.Collections;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import eu.arrowhead.core.hbconfmgr.arrowhead.ArrowheadAuthorizationSystemClient;
import eu.arrowhead.core.hbconfmgr.arrowhead.ArrowheadServiceRegistryClient;
import eu.arrowhead.core.hbconfmgr.arrowhead.model.request.ServiceRegistryRequestDTO;
import eu.arrowhead.core.hbconfmgr.arrowhead.model.request.SystemRequestDTO;
import eu.arrowhead.core.hbconfmgr.config.InitArrowheadMockServers;


@SpringBootTest
@ActiveProfiles("test")
public class CertificateCheckFailureIT {
    private static InitArrowheadMockServers initMockServer;

    @Autowired
    ArrowheadAuthorizationSystemClient authClient;

    @Autowired
    ArrowheadServiceRegistryClient srClient;

    @BeforeAll
    public static void beforeAll() {
        CertificateCheckFailureIT.initMockServer = new InitArrowheadMockServers();
        CertificateCheckFailureIT.initMockServer.setUp(false);
    }

    @Test
    public void testMissingServerCertificatesAuthorizationSystem() {
        Exception exception = assertThrows(Exception.class, () -> {
            authClient.getPublicKey();
        });

        assertEquals(ClosedChannelException.class, exception.getCause().getClass());
    }

    @Test
    public void testMissingServerCertificatesServiceRegistry() {
        ServiceRegistryRequestDTO requestDTO = ServiceRegistryRequestDTO.builder()
            .serviceDefinition("definition3")
            .providerSystem(SystemRequestDTO.builder()
                .systemName("conf-system")
                .address("192.168.1.1")
                .port(1234)
                .authenticationInfo("test")
                .build())
            .serviceUri("/")
            .secure(ServiceRegistryRequestDTO.SecurityLevel.TOKEN)
            .version(1)
            .interfaces(Collections.singletonList("HTTPS-SECURE-JSON"))
            .build();

        Exception exception = assertThrows(Exception.class, () -> {
            srClient.registerService(requestDTO);
        });

        assertEquals(ClosedChannelException.class, exception.getCause().getClass());
    }

    @AfterAll
    public static void afterAll() {
        CertificateCheckFailureIT.initMockServer.shutDown();
    }
}
