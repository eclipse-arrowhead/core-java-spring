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

import static org.junit.Assert.assertThrows;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import com.github.fridujo.rabbitmq.mock.MockConnection;
import com.github.fridujo.rabbitmq.mock.MockConnectionFactory;

import eu.arrowhead.core.hbconfmgr.hawkbit.model.outbound.ThingCreatedOutboundMessage;
import eu.arrowhead.core.hbconfmgr.hawkbit.util.HawkbitDmfMockServer;


public class HawkbitFailureTest {

    private HawkbitDmfMockServer dmfMockServer;

    @Test
    public void testMissingExchange() throws IOException {
        final MockConnection mockConnection = new MockConnectionFactory().newConnection();

        final HawkbitDmfOutboundClient hawkbitClient = new HawkbitDmfOutboundClient(mockConnection.createChannel());

        dmfMockServer = new HawkbitDmfMockServer(mockConnection.createChannel());
        
        final ThingCreatedOutboundMessage message = ThingCreatedOutboundMessage.builder()
            .body(
                ThingCreatedOutboundMessage.Body.builder()
                    .name("some.device")
                    .build()
            )
            .headers(
                ThingCreatedOutboundMessage.Headers.builder()
                    .sender("sender")
                    .tenant("tenant")
                    .thingId("thingId")
                    .build()    
            )
            .build();

        hawkbitClient.createThing(message);
            
        dmfMockServer.stageFailure();

        assertThrows(IllegalArgumentException.class, () -> {
            hawkbitClient.createThing(message);
        });
    }
}