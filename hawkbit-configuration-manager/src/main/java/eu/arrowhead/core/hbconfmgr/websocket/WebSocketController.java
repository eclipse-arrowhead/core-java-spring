/********************************************************************************
* Copyright (c) 2021 Bosch.IO GmbH[ and others]
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0.
*
* SPDX-License-Identifier: EPL-2.0
********************************************************************************/

package eu.arrowhead.core.hbconfmgr.websocket;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.arrowhead.core.hbconfmgr.service.HawkbitService;
import eu.arrowhead.core.hbconfmgr.websocket.model.ActionUpdateStatusMapper;
import eu.arrowhead.core.hbconfmgr.websocket.model.UpdateActionRequestDTO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Controller
public class WebSocketController extends AbstractWebSocketHandler {
	
    private final HawkbitService hawkbitService;
    private final ObjectMapper objectMapper;
    private final Map<String, WebSocketSession> webSocketSessionMap;

    @Autowired
    public WebSocketController(final HawkbitService hawkbitService, final Map<String, WebSocketSession> webSocketSessionMap) {
        this.objectMapper = new ObjectMapper();
        this.hawkbitService = hawkbitService;
        this.webSocketSessionMap = webSocketSessionMap;
    }

    @Override
    public void afterConnectionEstablished(final WebSocketSession session) throws IOException {
        if (session.getPrincipal() != null) {
            final String clientId = session.getPrincipal().getName();
            this.hawkbitService.createDevice(clientId);
            this.webSocketSessionMap.put(clientId, session);
        } else {
            session.close();
        }
    }

    @Override
    public void afterConnectionClosed(final WebSocketSession session, final CloseStatus status) {
        if (session.getPrincipal() != null) {
            this.webSocketSessionMap.remove(session.getPrincipal().getName());
        }
    }

    @Override
    protected void handleTextMessage(final WebSocketSession session, final TextMessage message) {
        try {
            final UpdateActionRequestDTO request = objectMapper.readValue(message.getPayload(), UpdateActionRequestDTO.class);
            this.hawkbitService.updateActionStatus(ActionUpdateStatusMapper.mapToActionUpdateStatus(request));
        } catch (final Exception e) {
            log.error("Web socket text message could not be handled", e);
        }
    }
}