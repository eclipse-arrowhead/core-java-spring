/********************************************************************************
* Copyright (c) 2021 Bosch.IO GmbH[ and others]
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0.
*
* SPDX-License-Identifier: EPL-2.0
********************************************************************************/

package eu.arrowhead.core.hbconfmgr.hawkbit;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.fridujo.rabbitmq.mock.MockConnection;
import com.github.fridujo.rabbitmq.mock.MockConnectionFactory;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Envelope;

import eu.arrowhead.core.hbconfmgr.hawkbit.model.inbound.CancelDownloadInboundMessage;
import eu.arrowhead.core.hbconfmgr.hawkbit.model.inbound.DownloadRequestInboundMessage;
import eu.arrowhead.core.hbconfmgr.hawkbit.model.inbound.ThingDeletedInboundMessage;
import eu.arrowhead.core.hbconfmgr.service.WebSocketService;
import eu.arrowhead.core.hbconfmgr.websocket.DeviceNotConnectedException;

public class HawkbitDmfConsumerTest {
    
	private HawkbitDmfConsumer consumer;
    private MockConnection mockConnection;
    private WebSocketService mock_wsService;

    @BeforeEach
    public void init() {
        mockConnection = new MockConnectionFactory().newConnection();

        mock_wsService = mock(WebSocketService.class);
        consumer = new HawkbitDmfConsumer(mockConnection.createChannel(), mock_wsService);
    }

    @Test
    public void testDownloadRequestInboundGeneration() throws IOException, DeviceNotConnectedException {
        final AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder().contentType("application/json")
                .headers(Map.ofEntries(Map.entry("type", "EVENT"), Map.entry("thingId", "device7"),
                        Map.entry("topic", "DOWNLOAD_AND_INSTALL"), Map.entry("tenant", "tenant1")))
                .build();

        final String body = "{\n" + "    \"actionId\": 8,\n" + "    \"targetSecurityToken\": \"aslkhju4kjasdz9uas\",\n"
                + "    \"softwareModules\": [{\n" + "            \"moduleId\": 1,\n"
                + "            \"moduleType\": \"OS\",\n" + "            \"moduleVersion\": \"1.0\",\n"
                + "            \"artifacts\": [{\n" + "                    \"filename\": \"os.zip\",\n"
                + "                    \"urls\": {\n"
                + "                        \"COAP\": \"coap://coap.local/os.zip\",\n"
                + "                        \"HTTP\": \"http://localhost/os.zip\",\n"
                + "                        \"HTTPS\": \"https://localhost/os.zip\"\n" + "                    },\n"
                + "                    \"hashes\": {\n"
                + "                        \"md5\": \"8c9693121361daf0d992df4eeb714ebb\",\n"
                + "                        \"sha1\": \"e5ee90a0f8d0570b8bbdcaa23aa9a6b737c0c826\"\n"
                + "                    },\n" + "                    \"size\": 539475\n" + "                }\n"
                + "            ],\n" + "            \"metadata\": [{\n"
                + "                    \"key\": \"Description\",\n"
                + "                    \"value\": \"This is the new operating system\"\n" + "                }\n"
                + "            ]\n" + "        }\n" + "    ]\n" + "}";

        final DownloadRequestInboundMessage.Body assertBody = DownloadRequestInboundMessage.Body.builder().actionId(8L)
                .targetSecurityToken("aslkhju4kjasdz9uas")
                .softwareModules(Collections.singletonList(DownloadRequestInboundMessage.Body.SoftwareModule.builder()
                        .moduleId(1L).moduleType("OS").moduleVersion("1.0")
                        .artifacts(Collections.singletonList(DownloadRequestInboundMessage.Body.Artifact.builder()
                                .filename("os.zip")
                                .urls(DownloadRequestInboundMessage.Body.Urls.builder().COAP("coap://coap.local/os.zip")
                                        .HTTP("http://localhost/os.zip").HTTPS("https://localhost/os.zip").build())
                                .hashes(DownloadRequestInboundMessage.Body.Hashes.builder()
                                        .md5("8c9693121361daf0d992df4eeb714ebb")
                                        .sha1("e5ee90a0f8d0570b8bbdcaa23aa9a6b737c0c826").build())
                                .size(539475L).build()))
                        .metadata(Collections.singletonList(DownloadRequestInboundMessage.Body.Metadata.builder()
                                .key("Description").value("This is the new operating system").build()))
                        .build()))
                .build();

        final DownloadRequestInboundMessage.Headers assertHeaders = DownloadRequestInboundMessage.Headers.builder().type("EVENT")
                .thingId("device7").topic("DOWNLOAD_AND_INSTALL").tenant("tenant1").build();

        final DownloadRequestInboundMessage assertMessage = DownloadRequestInboundMessage.builder().body(assertBody).deliveryTag(1l).headers(assertHeaders).build();

        consumer.handleDelivery("tag", new Envelope(1L, false, "exchange", "routingKey"), properties, body.getBytes());

        verify(mock_wsService).sendDownloadEventMessage(assertMessage);
    }

    @Test
    public void testCancelDownload() throws IOException, DeviceNotConnectedException {
        final AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder().contentType("application/json")
                .headers(Map.ofEntries(Map.entry("type", "EVENT"), Map.entry("thingId", "device7"),
                        Map.entry("topic", "CANCEL_DOWNLOAD"), Map.entry("tenant", "tenant1")))
                .build();

        final String rawMessage = "{"
            + "\"actionId\": 16"
        + "}";

        final CancelDownloadInboundMessage cancelDownloadInboundMessage = CancelDownloadInboundMessage.builder()
            .body(
                CancelDownloadInboundMessage.Body.builder()
                    .actionId(16L)
                    .build()
            )
            .headers(
                CancelDownloadInboundMessage.Headers.builder()
                    .type("EVENT")
                    .tenant("tenant1")
                    .thingId("device7")
                    .topic("CANCEL_DOWNLOAD")
                    .build()
            )
            .build();

        consumer.handleDelivery("tag", new Envelope(1L, false, "exchange", "routingKey"), properties, rawMessage.getBytes());

        verify(mock_wsService).sendCancelDownloadMessage(cancelDownloadInboundMessage);
    }

    @Test
    public void testThingDeleted() throws IOException, DeviceNotConnectedException {
        final AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder().contentType("application/json")
                .headers(Map.ofEntries(Map.entry("type", "THING_DELETED"), Map.entry("thingId", "device7"),
                        Map.entry("topic", "THING_DELETED"), Map.entry("tenant", "tenant1")))
                .build();

        final ThingDeletedInboundMessage thingDeletedInboundMessage = ThingDeletedInboundMessage.builder()
            .headers(
                ThingDeletedInboundMessage.Headers.builder()
                    .thingId("device7")
                    .build()
            )
            .build();

        consumer.handleDelivery("tag", new Envelope(1L, false, "exchange", "routingKey"), properties, null);

        verify(mock_wsService).sendThingDeletedMessage(thingDeletedInboundMessage);
    }
}