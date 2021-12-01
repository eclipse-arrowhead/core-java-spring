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

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Scope;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import eu.arrowhead.core.hbconfmgr.Constants;

/**
 * This class provides the clients for interaction with hawkBit via beans managed by the Spring container.
 */
@Configuration
public class HawkbitConfig {

    @Value(Constants.HAWKBIT_HOST)
    private String host;

    @Value(Constants.HAWKBIT_PORT)
    private int port;

    @Value(Constants.HAWKBIT_USER)
    private String username;

    @Value(Constants.HAWKBIT_PASSWORD)
    private String password;

    @Bean
    @DependsOn(Constants.GUARD_BEAN)
    public ConnectionFactory connectionFactory() {
        final ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost(this.host);
        connectionFactory.setPort(this.port);
        connectionFactory.setUsername(this.username);
        connectionFactory.setPassword(this.password);
        connectionFactory.setAutomaticRecoveryEnabled(true);
        
        return connectionFactory;
    }

    @Bean
    @DependsOn(Constants.GUARD_BEAN)
    public Connection getConnection(final ConnectionFactory connectionFactory) throws IOException, TimeoutException {
        return connectionFactory.newConnection();
    }

    @Bean
    @DependsOn(Constants.GUARD_BEAN)
    @Scope("prototype")
    @Autowired
    public Channel createChannel(final Connection connection) throws IOException {
        return connection.createChannel();
    }
}