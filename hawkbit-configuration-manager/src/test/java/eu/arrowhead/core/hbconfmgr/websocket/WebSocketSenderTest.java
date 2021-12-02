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

import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import eu.arrowhead.core.hbconfmgr.hawkbit.model.inbound.MessageTypeInbound;
import eu.arrowhead.core.hbconfmgr.hawkbit.model.inbound.ThingDeletedInboundMessage;
import eu.arrowhead.core.hbconfmgr.websocket.model.DeviceMessage;

public class WebSocketSenderTest {
    
	private WebSocketSender wsSender;
	private WebSocketSession wsSession;

    @BeforeEach
    public void init() {
        final ConcurrentHashMap<String, WebSocketSession> webSocketSessionMap = new ConcurrentHashMap<>();
        
        wsSession = mock(WebSocketSession.class);
        webSocketSessionMap.put("testIdX", wsSession);

        wsSender = new WebSocketSender(webSocketSessionMap);
    }

    @Test
    public void testSendMessageWithMissingSessionWebSocketSessionMap() throws IOException {
        final DeviceMessage deviceMessage = DeviceMessage.builder()
            .type(MessageTypeInbound.EVENT.toString())
            .message(
                ThingDeletedInboundMessage.builder()
                    .headers(
                        ThingDeletedInboundMessage.Headers.builder()
                            .thingId("testIdY")
                            .build()
                    )
                    .build()
            )
            .build();

        assertThrows(DeviceNotConnectedException.class, () -> {
            wsSender.sendMessage("testIdY", deviceMessage);
        });
    }

    @Test
    public void testSendMessageWithExistingSession() throws IOException,
            DeviceNotConnectedException {
        final DeviceMessage deviceMessage = DeviceMessage.builder()
            .type(MessageTypeInbound.EVENT.toString())
            .message(
                ThingDeletedInboundMessage.builder()
                    .headers(
                        ThingDeletedInboundMessage.Headers.builder()
                            .thingId("testIdX")
                            .build()
                    )
                    .build()
            )
            .build();
        
        final String expectedBody = "{"
            + "\"type\":\"EVENT\","
            + "\"message\":{"
            +     "\"headers\":{"
            +         "\"thingId\":\"testIdX\""
            +     "}"
            + "}"
        + "}";

        final TextMessage expectedWSMessage = new TextMessage(expectedBody);

        wsSender.sendMessage("testIdX", deviceMessage);

        verify(wsSession).sendMessage(expectedWSMessage);
    }    
}