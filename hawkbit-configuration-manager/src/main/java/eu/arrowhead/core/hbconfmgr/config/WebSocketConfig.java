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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Scope;
import org.springframework.web.socket.WebSocketSession;

import eu.arrowhead.core.hbconfmgr.Constants;

@Configuration
public class WebSocketConfig {
    @Bean
    @DependsOn(Constants.GUARD_BEAN)
    @Scope("singleton")
    public Map<String, WebSocketSession> getWebSocketSessionMap() {
        return new ConcurrentHashMap<>();
    }
}