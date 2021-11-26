/********************************************************************************
* Copyright (c) 2021 Bosch.IO GmbH[ and others]
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0.
*
* SPDX-License-Identifier: EPL-2.0
********************************************************************************/

package eu.arrowhead.core.hbconfmgr.service;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.arrowhead.core.hbconfmgr.hawkbit.model.inbound.CancelDownloadInboundMessage;
import eu.arrowhead.core.hbconfmgr.hawkbit.model.inbound.DownloadRequestInboundMessage;
import eu.arrowhead.core.hbconfmgr.hawkbit.model.inbound.MessageTypeInbound;
import eu.arrowhead.core.hbconfmgr.hawkbit.model.inbound.ThingDeletedInboundMessage;
import eu.arrowhead.core.hbconfmgr.websocket.DeviceNotConnectedException;
import eu.arrowhead.core.hbconfmgr.websocket.WebsocketSender;
import eu.arrowhead.core.hbconfmgr.websocket.model.DeviceMessage;
import lombok.extern.log4j.Log4j2;

/**
 * This is the web socket service that implements business logic specifically
 * for the configuration system.
 */
@Log4j2
@Service
public class WebSocketService {
    private final WebsocketSender wsSender;

    @Autowired
    public WebSocketService(WebsocketSender wsSender) throws IOException {
        this.wsSender = wsSender;
    }

    public void sendDownloadEventMessage(DownloadRequestInboundMessage message)
            throws IOException, DeviceNotConnectedException {
        log.info("Message {}", message);
        String clientId = message.getHeaders().getThingId();

        DeviceMessage deviceMessage = DeviceMessage.builder()
            .type(MessageTypeInbound.EVENT.toString())
            .message(message)
            .build();

        this.wsSender.sendMessage(clientId, deviceMessage);
    }

    public void sendThingDeletedMessage(ThingDeletedInboundMessage message) throws IOException,
            DeviceNotConnectedException {
        log.info("Message {}", message);
        String clientId = message.getHeaders().getThingId();

        DeviceMessage deviceMessage = DeviceMessage.builder()
            .type(MessageTypeInbound.THING_DELETED.toString())
            .message(message)
            .build();

        this.wsSender.sendMessage(clientId, deviceMessage);
    }

    public void sendCancelDownloadMessage(CancelDownloadInboundMessage message) throws IOException,
            DeviceNotConnectedException {
        log.info("Message {}", message);

        String clientId = message.getHeaders().getThingId();

        DeviceMessage deviceMessage = DeviceMessage.builder()
            .type(MessageTypeInbound.EVENT.toString())
            .message(message)
            .build();

        this.wsSender.sendMessage(clientId, deviceMessage);
    }
}
