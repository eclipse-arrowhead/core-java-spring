/********************************************************************************
* Copyright (c) 2021 Bosch.IO GmbH[ and others]
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0.
*
* SPDX-License-Identifier: EPL-2.0
********************************************************************************/

package eu.arrowhead.core.confmgr.websocket;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import eu.arrowhead.core.confmgr.websocket.model.DeviceMessage;

@Component
public class WebsocketSender {
    private final Map<String, WebSocketSession> webSocketSessionMap;
    private final ObjectMapper objectMapper;

    @Autowired
    public WebsocketSender(Map<String, WebSocketSession> webSocketSessionMap) {
        this.webSocketSessionMap = webSocketSessionMap;
        this.objectMapper = new ObjectMapper();
    }

    public void sendMessage(String thingId, DeviceMessage message) throws IOException, DeviceNotConnectedException {
        if(this.webSocketSessionMap.containsKey(thingId)){
            String body = this.objectMapper.writeValueAsString(message);
            TextMessage wsMessage = new TextMessage(body);
            
            WebSocketSession session = this.webSocketSessionMap.get(thingId);
            
            session.sendMessage(wsMessage);
        } else {
            throw new DeviceNotConnectedException("The device" + thingId + "is currently not connected.");
        }

    }
}
