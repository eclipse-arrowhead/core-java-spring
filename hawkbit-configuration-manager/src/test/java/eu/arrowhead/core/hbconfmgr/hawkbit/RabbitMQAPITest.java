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
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.fridujo.rabbitmq.mock.MockConnectionFactory;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Connection;

import eu.arrowhead.core.hbconfmgr.hawkbit.model.inbound.DownloadRequestInboundMessage;
import eu.arrowhead.core.hbconfmgr.hawkbit.util.HawkbitDmfMockServer;
import eu.arrowhead.core.hbconfmgr.service.WebSocketService;
import eu.arrowhead.core.hbconfmgr.websocket.DeviceNotConnectedException;

public class RabbitMQAPITest {
    
	private HawkbitDmfConsumer consumer;
	private WebSocketService mock_wsService;
	private HawkbitDmfMockServer mockServer;
	private Connection mockConnection;

    @BeforeEach
    public void init() throws IOException {
        mock_wsService = mock(WebSocketService.class);
        
        mockConnection = new MockConnectionFactory().newConnection();

        mockServer = new HawkbitDmfMockServer(mockConnection.createChannel());
        consumer = new HawkbitDmfConsumer(mockConnection.createChannel(), mock_wsService);
    }

    @Test
    public void testAMQPInterface() throws IOException, DeviceNotConnectedException {
            
        consumer.subscribeToDownloadEvents();

        final AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder()
            .contentType("application/json")
            .headers(Map.ofEntries(
                Map.entry("type", "EVENT"),
                Map.entry("thingId", "device7"),
                Map.entry("topic", "DOWNLOAD_AND_INSTALL"),
                Map.entry("tenant", "tenant1")
            ))
            .build();
        final String body = "{\n" +
                "    \"actionId\": 8,\n" +
                "    \"targetSecurityToken\": \"aslkhju4kjasdz9uas\",\n" +
                "    \"softwareModules\": [{\n" +
                "            \"moduleId\": 1,\n" +
                "            \"moduleType\": \"OS\",\n" +
                "            \"moduleVersion\": \"1.0\",\n" +
                "            \"artifacts\": [{\n" +
                "                    \"filename\": \"os.zip\",\n" +
                "                    \"urls\": {\n" +
                "                        \"COAP\": \"coap://coap.local/os.zip\",\n" +
                "                        \"HTTP\": \"http://localhost/os.zip\",\n" +
                "                        \"HTTPS\": \"https://localhost/os.zip\"\n" +
                "                    },\n" +
                "                    \"hashes\": {\n" +
                "                        \"md5\": \"8c9693121361daf0d992df4eeb714ebb\",\n" +
                "                        \"sha1\": \"e5ee90a0f8d0570b8bbdcaa23aa9a6b737c0c826\"\n" +
                "                    },\n" +
                "                    \"size\": 539475\n" +
                "                }\n" +
                "            ],\n" +
                "            \"metadata\": [{\n" +
                "                    \"key\": \"Description\",\n" +
                "                    \"value\": \"This is the new operating system\"\n" +
                "                }\n" +
                "            ]\n" +
                "        }\n" +
                "    ]\n" +
                "}";
        mockServer.publish(properties, body);

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
                        .sha1("e5ee90a0f8d0570b8bbdcaa23aa9a6b737c0c826")
                        .build())
                    .size(539475L).build()))
                .metadata(Collections.singletonList(DownloadRequestInboundMessage.Body.Metadata.builder()
                        .key("Description").value("This is the new operating system").build()))
                .build()))
            .build();

        final DownloadRequestInboundMessage.Headers assertHeaders = DownloadRequestInboundMessage.Headers.builder().type("EVENT")
            .thingId("device7").topic("DOWNLOAD_AND_INSTALL").tenant("tenant1").build();

        final DownloadRequestInboundMessage assertMessage = DownloadRequestInboundMessage.builder().body(assertBody).deliveryTag(1l).headers(assertHeaders).build();
        
        verify(mock_wsService, timeout(1000)).sendDownloadEventMessage(assertMessage);
    }
}