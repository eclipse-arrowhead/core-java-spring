/********************************************************************************
* Copyright (c) 2021 Bosch.IO GmbH[ and others]
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0.
*
* SPDX-License-Identifier: EPL-2.0
********************************************************************************/

package eu.arrowhead.core.confmgr.hawkbit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import com.github.fridujo.rabbitmq.mock.MockConnection;
import com.github.fridujo.rabbitmq.mock.MockConnectionFactory;
import com.rabbitmq.client.AMQP;

import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import eu.arrowhead.core.confmgr.hawkbit.model.ActionStatus;
import eu.arrowhead.core.confmgr.hawkbit.model.outbound.ThingCreatedOutboundMessage;
import eu.arrowhead.core.confmgr.hawkbit.model.outbound.UpdateActionStatusOutboundMessage;
import eu.arrowhead.core.confmgr.hawkbit.util.HawkbitDmfMockServer;
import eu.arrowhead.core.confmgr.hawkbit.util.Message;

public class HawkbitDmfOutboundClientTest {

    @Test
    public void givenCorrectMessage_whenCreateThing_thenThingIsCreated() throws IOException, JSONException {
        MockConnection mockConnection = new MockConnectionFactory().newConnection();
        HawkbitDmfMockServer mockServer = new HawkbitDmfMockServer(mockConnection.createChannel());
        HawkbitDmfOutboundClient client = new HawkbitDmfOutboundClient(mockConnection.createChannel());

        ThingCreatedOutboundMessage message = ThingCreatedOutboundMessage.builder()
                .body(ThingCreatedOutboundMessage.Body.builder()
                        .name("device3")
                        .build())
                .headers(ThingCreatedOutboundMessage.Headers.builder()
                        .tenant("tenant1")
                        .thingId("device3")
                        .sender("testSender")
                        .build())
                .build();

        client.createThing(message);

        await().until(() -> mockServer.getMessages().size() == 1);
        Message receivedMessage = mockServer.getMessages().get(0);
        assertThat(receivedMessage).isNotNull();
        assertThat(receivedMessage.getProperties()).isEqualTo(new AMQP.BasicProperties.Builder()
                .headers(Map.ofEntries(
                        Map.entry("type", "THING_CREATED"),
                        Map.entry("thingId", "device3"),
                        Map.entry("sender", "testSender"),
                        Map.entry("tenant", "tenant1")))
                .contentType("application/json")
                .replyTo("configuration_system.direct.exchange")
                .build());
        JSONAssert.assertEquals("{\n" +
                "    \"name\": \"device3\"\n" +
                "}", receivedMessage.getBody(), JSONCompareMode.NON_EXTENSIBLE);
    }

    @Test
    public void givenCorrectMessage_whenUpdateActionStatus_thenActionIsUpdated() throws IOException, JSONException {
        MockConnection mockConnection = new MockConnectionFactory().newConnection();
        HawkbitDmfMockServer mockServer = new HawkbitDmfMockServer(mockConnection.createChannel());
        HawkbitDmfOutboundClient client = new HawkbitDmfOutboundClient(mockConnection.createChannel());

        UpdateActionStatusOutboundMessage message = UpdateActionStatusOutboundMessage.builder()
                .body(UpdateActionStatusOutboundMessage.UpdateActionStatusOutboundMessageBody.builder()
                        .actionId(7L)
                        .actionStatus(ActionStatus.FINISHED)
                        .message(Collections.singletonList("Successfully applied update on device"))
                        .softwareModuleId(3L)
                        .build())
                .headers(UpdateActionStatusOutboundMessage.UpdateActionStatusOutboundMessageHeaders.builder()
                        .tenant("tenant1")
                        .build())
                .build();

        client.updateActionStatus(message);

        await().until(() -> mockServer.getMessages().size() == 1);
        Message receivedMessage = mockServer.getMessages().get(0);
        assertThat(receivedMessage).isNotNull();
        assertThat(receivedMessage.getProperties()).isEqualTo(new AMQP.BasicProperties.Builder()
                .headers(Map.ofEntries(
                        Map.entry("type", "EVENT"),
                        Map.entry("topic", "UPDATE_ACTION_STATUS"),
                        Map.entry("tenant", "tenant1")))
                .contentType("application/json")
                .build());
        JSONAssert.assertEquals("{\n" +
                "    \"actionId\": 7,\n" +
                "    \"softwareModuleId\": 3,\n" +
                "    \"actionStatus\": \"FINISHED\",\n" +
                "    \"message\": [\"Successfully applied update on device\"]\n" +
                "}", receivedMessage.getBody(), JSONCompareMode.NON_EXTENSIBLE);
    }

}
