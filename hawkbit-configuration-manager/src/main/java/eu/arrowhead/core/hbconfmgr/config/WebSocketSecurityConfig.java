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

import java.security.PrivateKey;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import eu.arrowhead.core.hbconfmgr.security.JwtAuthenticationFilter;
import eu.arrowhead.core.hbconfmgr.service.ArrowheadService;
import eu.arrowhead.core.hbconfmgr.websocket.WebSocketController;

/**
 * This is the configuration for web sockets.
 * <p>Web socket support can be enabled/disabled with the annotation {@link EnableWebSocket @EnableWebSocket}</p>
 * <p>Web socket security can be enabled/disabled with the annotation {@link EnableWebSecurity @EnableWebSecurity}</p>
 */
@Configuration
@EnableWebSecurity
@EnableWebSocket
public class WebSocketSecurityConfig extends WebSecurityConfigurerAdapter implements WebSocketConfigurer {

    private final ArrowheadService arrowheadService;
    private final PrivateKey privateKey;
    private final WebSocketController webSocketController;

    @Autowired
    public WebSocketSecurityConfig(final ArrowheadService arrowheadService, final PrivateKey privateKey, final WebSocketController webSocketController) {
        this.arrowheadService = arrowheadService;
        this.privateKey = privateKey;
        this.webSocketController = webSocketController;
    }

    /**
     * Registers the web socket controller as web socket handler.
     */
    @Override
    public void registerWebSocketHandlers(final WebSocketHandlerRegistry registry) {
        registry.addHandler(this.webSocketController, "/");
    }

    /**
     * Configuration of the HTTP security for the whole configuration system, including web socket connections.
     * Many default security settings are disabled as the clients are connecting non-interactive to the configuration
     * system and don't support all default security settings.
     */
    @Override
    protected void configure(final HttpSecurity http) throws Exception {
        http
                // Disable form login, as only non-interactive clients connect
                .formLogin().disable()
                // Disable HTTP basic authentication, as only JWT authentication is supported
                .httpBasic().disable()
                // CORS protection is not required, as only non-interactive clients connect
                .cors().disable()
                // CSRF protection is not required,  as only non-interactive clients connect
                .csrf(AbstractHttpConfigurer::disable)
                // Register custom JWT authentication filter, so JWT authentication is supported
                .addFilterBefore(new JwtAuthenticationFilter(this.arrowheadService, this.privateKey), BasicAuthenticationFilter.class)
                // Disable Spring Sessions, as sessions should not be supported
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Require full authentication on every request
                .authorizeRequests().anyRequest().fullyAuthenticated();
    }
}