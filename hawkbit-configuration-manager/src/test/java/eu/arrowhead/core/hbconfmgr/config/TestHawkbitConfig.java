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

import com.github.fridujo.rabbitmq.mock.MockConnectionFactory;
import com.rabbitmq.client.ConnectionFactory;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

/**
 * This class provides the clients for interaction with hawkBit via beans managed by the Spring container.
 */
@Configuration
@Profile("test")
public class TestHawkbitConfig {
    @Primary
    @Bean
    public ConnectionFactory hawkbitConnectionFactory() {
        final MockConnectionFactory mockConnectionFactory = new MockConnectionFactory();
        mockConnectionFactory.setAutomaticRecoveryEnabled(true);

        return mockConnectionFactory;
    }
}