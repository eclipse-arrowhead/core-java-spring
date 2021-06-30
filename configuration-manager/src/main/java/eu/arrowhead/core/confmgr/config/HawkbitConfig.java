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

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * This class provides the clients for interaction with hawkBit via beans managed by the Spring container.
 */
@Configuration
public class HawkbitConfig {

    @Value("${hawkbit.host}")
    private String host;

    @Value("${hawkbit.port}")
    private Integer port;

    @Value("${hawkbit.username}")
    private String username;

    @Value("${hawkbit.password}")
    private String password;

    @Bean
    public ConnectionFactory connectionFactory() {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setUsername(this.username);
        connectionFactory.setPassword(this.password);
        connectionFactory.setHost(this.host);
        connectionFactory.setPort(this.port);
        connectionFactory.setAutomaticRecoveryEnabled(true);
        return connectionFactory;
    }

    @Bean
    public Connection getConnection(ConnectionFactory connectionFactory) throws IOException, TimeoutException {
        return connectionFactory.newConnection();
    }

    @Bean
    @Scope("prototype")
    @Autowired
    public Channel createChannel(Connection connection) throws IOException {
        return connection.createChannel();
    }
}
