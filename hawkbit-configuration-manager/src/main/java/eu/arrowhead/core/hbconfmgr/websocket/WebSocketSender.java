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

import eu.arrowhead.core.hbconfmgr.Constants;
import eu.arrowhead.core.hbconfmgr.websocket.model.DeviceMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

@DependsOn(Constants.GUARD_BEAN)
@Component
public class WebSocketSender {
	
    private final Map<String, WebSocketSession> webSocketSessionMap;
    private final ObjectMapper objectMapper;

    @Autowired
    public WebSocketSender(final Map<String, WebSocketSession> webSocketSessionMap) {
        this.webSocketSessionMap = webSocketSessionMap;
        this.objectMapper = new ObjectMapper();
    }

    public void sendMessage(final String thingId, final DeviceMessage message) throws IOException, DeviceNotConnectedException {
        if (this.webSocketSessionMap.containsKey(thingId)) {
        	final String body = this.objectMapper.writeValueAsString(message);
        	final TextMessage wsMessage = new TextMessage(body);
        	
            final WebSocketSession session = this.webSocketSessionMap.get(thingId);
            session.sendMessage(wsMessage);
        } else {
            throw new DeviceNotConnectedException("The device " + thingId + " is currently not connected.");
        }
    }
}