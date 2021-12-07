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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import eu.arrowhead.core.confmgr.config.InitArrowheadMockServers;


@SpringBootTest
@ActiveProfiles("test")
public class ArrowheadAuthorizationSystemIT {
    private static InitArrowheadMockServers initMockServer;

    @Autowired
    ArrowheadAuthorizationSystemClient authClient;

    @BeforeAll
    public static void beforeAll() {
        ArrowheadAuthorizationSystemIT.initMockServer = new InitArrowheadMockServers();
        ArrowheadAuthorizationSystemIT.initMockServer.setUp(true);
    }

    @Test
    public void testGetPubKeyEndpointServiceAndRequestPublicKey() throws WebClientResponseException, AuthorizationSystemClientUninitializedException {
        String pubKey = authClient.getPublicKey();

        assertEquals("MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAqIZpQa/yCE8JBYtLz+XkoXpHYUsTFHuntQIPV6pPgcX07OagpFWW+LIRWD4jXPc+ndXOVD7XmimnkAOXGYD+1GF+glBSCKVcjOM55S5LLoJbV9J77H1B/NhKQX6kZDxP94jRPql7YmMlo8ge3NHjc/isNh2mX8yS4LnjjXXdhv2Ggn0fcItgAAYd2CLpds/obrsue6hWD4G1T/TUTjgoYR3JiPh+XTOOQJXWTDMA5ay81/TGTLJ+PAc8Fp2GP+AED2QVmPO+PPuk4RjTCxS7L63aH0HW2o1ibf4kndKbsg5XwOEP56+IwEutGqctzQw4y3luLHXOBSxsfWcnyoMeDwIDAQAB", pubKey);
    }

    @AfterAll
    public static void afterAll() {
        ArrowheadAuthorizationSystemIT.initMockServer.shutDown();
    }
}
