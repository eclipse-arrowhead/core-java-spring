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
import eu.arrowhead.core.hbconfmgr.websocket.WebSocketSender;
import eu.arrowhead.core.hbconfmgr.websocket.model.DeviceMessage;
import lombok.extern.log4j.Log4j2;

/**
 * This is the web socket service that implements business logic specifically
 * for the configuration system.
 */
@Log4j2
@Service
public class WebSocketService {
	
    private final WebSocketSender wsSender;

    @Autowired
    public WebSocketService(final WebSocketSender wsSender) throws IOException {
        this.wsSender = wsSender;
    }

    public void sendDownloadEventMessage(final DownloadRequestInboundMessage message) throws IOException, DeviceNotConnectedException {
        log.debug("Message {}", message);
        
        final String clientId = message.getHeaders().getThingId();

        final DeviceMessage deviceMessage = DeviceMessage.builder()
            .type(MessageTypeInbound.EVENT.toString())
            .message(message)
            .build();

        this.wsSender.sendMessage(clientId, deviceMessage);
    }

    public void sendThingDeletedMessage(final ThingDeletedInboundMessage message) throws IOException, DeviceNotConnectedException {
        log.debug("Message {}", message);
        
        final String clientId = message.getHeaders().getThingId();

        final DeviceMessage deviceMessage = DeviceMessage.builder()
            .type(MessageTypeInbound.THING_DELETED.toString())
            .message(message)
            .build();

        this.wsSender.sendMessage(clientId, deviceMessage);
    }

    public void sendCancelDownloadMessage(final CancelDownloadInboundMessage message) throws IOException, DeviceNotConnectedException {
        log.debug("Message {}", message);

        final String clientId = message.getHeaders().getThingId();

        final DeviceMessage deviceMessage = DeviceMessage.builder()
            .type(MessageTypeInbound.EVENT.toString())
            .message(message)
            .build();

        this.wsSender.sendMessage(clientId, deviceMessage);
    }
}