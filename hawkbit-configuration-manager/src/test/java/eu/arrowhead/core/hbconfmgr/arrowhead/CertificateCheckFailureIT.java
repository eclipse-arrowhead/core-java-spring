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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.channels.ClosedChannelException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import com.fasterxml.jackson.core.JsonProcessingException;

import eu.arrowhead.core.hbconfmgr.config.ArrowheadConfig;
import eu.arrowhead.core.hbconfmgr.config.InitArrowheadMockServers;


@ActiveProfiles("test")
public class CertificateCheckFailureIT {
    private static InitArrowheadMockServers initMockServer;
    

    @BeforeAll
    public static void beforeAll() throws JsonProcessingException {
        CertificateCheckFailureIT.initMockServer = new InitArrowheadMockServers();
        CertificateCheckFailureIT.initMockServer.setUp(false);
    }

    @Test
    public void testMissingServerCertificatesServiceRegistry() {
    	try {
    		ArrowheadConfig config = new ArrowheadConfig();
    		ReflectionTestUtils.setField(config, "serviceRegistryAddress", "localhost");
    		ReflectionTestUtils.setField(config, "serviceRegistryPort", 8443);
    		config.init();
    	} catch (final Exception e) {
    		assertEquals(ClosedChannelException.class, e.getCause().getClass());
    	}
    }

    @AfterAll
    public static void afterAll() {
        CertificateCheckFailureIT.initMockServer.shutDown();
    }
}