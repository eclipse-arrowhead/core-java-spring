/********************************************************************************
* Copyright (c) 2021 Bosch.IO GmbH[ and others]
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0.
*
* SPDX-License-Identifier: EPL-2.0
********************************************************************************/

package eu.arrowhead.core.hbconfmgr.hawkbit.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class HawkbitDmfMockServer {

    @Getter
    private final Channel channel;

    @Getter
    private final List<Message> messages;

    public HawkbitDmfMockServer(final Channel channel) throws IOException {
        this.channel = channel;
        this.messages = new ArrayList<>();
        this.initializeMockServer();
    }

    public void clearMessages() {
        messages.clear();
    }

    public void publish(final AMQP.BasicProperties properties, final String body) throws IOException {
        final byte[] byteBody = body.getBytes(StandardCharsets.UTF_8);
        this.channel.basicPublish("configuration_system.direct.exchange", "", properties, byteBody);
    }

    private void initializeMockServer() throws IOException {
        final String queueName = this.channel.queueDeclare().getQueue();
        this.channel.exchangeDeclare("dmf.exchange", BuiltinExchangeType.DIRECT);
        this.channel.queueBind(queueName, "dmf.exchange", "");
        this.channel.basicConsume(queueName, new DefaultConsumer(this.channel) {
            @Override
            public void handleDelivery(final String consumerTag, final Envelope envelope, final AMQP.BasicProperties properties, final byte[] body) {
                final String bodyString = new String(body, StandardCharsets.UTF_8);
                log.debug("Received amqp message: consumerTag {}, envelope {}, properties {}, body {}",
                        consumerTag, envelope, properties, bodyString);
                messages.add(new Message(envelope, properties, bodyString));
            }
        });
    }

    public void stageFailure() throws IOException {
        this.channel.exchangeDelete("dmf.exchange", false);
    }
}