/********************************************************************************
 * Copyright (c) 2021 {Lulea University of Technology}
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 
 *
 * Contributors: 
 *   {Lulea University of Technology} - implementation
 *   Arrowhead Consortia - conceptualization 
 ********************************************************************************/
package eu.arrowhead.core.timemanager;

import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.shared.TimeManagerTimeResponseDTO;
import eu.arrowhead.core.timemanager.service.TimeManagerDriver;


@Component
@EnableScheduling
public class TimeWSHandler extends TextWebSocketHandler {
 
    //=================================================================================================
    // members

    private final Logger logger = LogManager.getLogger(TimeWSHandler.class);
    
    private final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();
 
    @Value("${server.ssl.enabled}")
    private boolean sslEnabled;

    @Value("${time.timezone}")
    private String serverTimeZone;

    @Autowired
    TimeManagerDriver timeManagerDriver;

    //=================================================================================================
    // methods

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        super.afterConnectionEstablished(session);
    }
 
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
        super.afterConnectionClosed(session, status);
    }
 
    @Scheduled(fixedDelay = 1000 * 30, initialDelay = 1000)
    public void sendOutTimeMessages() {
        sessions.forEach(webSocketSession -> {
            try {
                TimeManagerTimeResponseDTO response = new TimeManagerTimeResponseDTO(serverTimeZone, timeManagerDriver.isTimeTrusted());
                TextMessage msg = new TextMessage(Utilities.toJson(response).getBytes(StandardCharsets.UTF_8));
                webSocketSession.sendMessage(msg);
            } catch (IOException e) {
                logger.error("Error occurred.", e);
            }
        });
        
    }
}

