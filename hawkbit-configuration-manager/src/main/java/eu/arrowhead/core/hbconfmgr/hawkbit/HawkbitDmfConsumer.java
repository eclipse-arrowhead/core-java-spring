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

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import eu.arrowhead.core.hbconfmgr.hawkbit.model.EventType;
import eu.arrowhead.core.hbconfmgr.hawkbit.model.MessageHeader;
import eu.arrowhead.core.hbconfmgr.hawkbit.model.inbound.CancelDownloadInboundMessage;
import eu.arrowhead.core.hbconfmgr.hawkbit.model.inbound.DownloadRequestInboundMessage;
import eu.arrowhead.core.hbconfmgr.hawkbit.model.inbound.MessageTypeInbound;
import eu.arrowhead.core.hbconfmgr.hawkbit.model.inbound.ThingDeletedInboundMessage;
import eu.arrowhead.core.hbconfmgr.service.WebSocketService;
import eu.arrowhead.core.hbconfmgr.websocket.DeviceNotConnectedException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;

/**
 * Handles incoming messages on the connected HawkBit (RabbitMQ) queue.
 * Responsible for further message routing, depending on the message type and
 * topic.
 */
@Log4j2
@Component
public class HawkbitDmfConsumer extends DefaultConsumer {

    private final Channel channel;
    private final ObjectMapper objectMapper;

    private final WebSocketService wsService;

    /**
     * Initializes a new hawkBit consumer that operates on the hawkBit device
     * management federation (DMF) API. It is used for downstream communication
     * (receiving messages from hawkBit). Further information to the downstream DMF
     * API can be found in the <a
     * href=https://www.eclipse.org/hawkbit/apis/dmf_api/>DMF API documentation</a>
     * under <i>"Messages sent by hawkBit (hawkBit -> Client)"</i>
     *
     * @param channel used for communication with hawkBit
     */
    @Autowired
    public HawkbitDmfConsumer(final Channel channel, final WebSocketService wsService) {
        super(channel);
        this.channel = channel;
        this.objectMapper = new ObjectMapper();
        this.wsService = wsService;
    }

    /**
     * Subscribe to download event messages from hawkBit.
     * <p>
     * <font color="red">Important:</font> The handler is responsible to acknowledge
     * the successful retrieval and processing of the message via the method
     * {@link #acknowledgeMessage(long)}.
     *
     * @throws IOException if there is a connection problem with hawkBit
     */
    public void subscribeToDownloadEvents() throws IOException {
        this.initializeQueueOnHawkbit();
        this.consumeFromQueueOnHawkbit();
    }

    /**
     * Initialize the queue on hawkBit DMF API that is used from the configuration
     * system to receive messages.
     *
     * @throws IOException if there is a connection problem with hawkBit
     */
    private void initializeQueueOnHawkbit() throws IOException {
        this.channel.queueDeclare(HawkbitDmfConstants.RECEIVING_QUEUE, true, false, false, null);
        this.channel.exchangeDeclare(HawkbitDmfConstants.RECEIVING_EXCHANGE, BuiltinExchangeType.DIRECT, true, false, null);
        this.channel.queueBind(HawkbitDmfConstants.RECEIVING_QUEUE, HawkbitDmfConstants.RECEIVING_EXCHANGE, HawkbitDmfConstants.RECEIVING_ROUTING_KEY);
    }

    /**
     * Start consuming from the configuration system queue on the hawkBit DMF API.
     *
     * @throws IOException if there is a connection problem with hawkBit
     */
    private void consumeFromQueueOnHawkbit() throws IOException {
        this.channel.basicConsume(HawkbitDmfConstants.RECEIVING_QUEUE, false, this);
    }

    /**
     * Overrides the method
     * {@link DefaultConsumer#handleDelivery(String, Envelope, AMQP.BasicProperties, byte[])}
     * with an implementation to handle hawkBit DMF API messages.
     */
    @Override
    public void handleDelivery(final String consumerTag, final Envelope envelope, final AMQP.BasicProperties properties, final byte[] body) {
        try {
            log.debug("Received message");
            handleMessage(envelope, properties, body);
        } catch (final IOException e) {
            log.atError().withThrowable(e).log(
                    "Handling of message delivery failed. Message has the following properties: {} and body: {}", properties, body);
        }
    }

    /**
     * Handle a hawkBit message.
     *
     * @param envelope   envelope of the AMQP message
     * @param properties properties (including the headers) of the AMQP message
     * @param body       body of the AMQP message
     * @throws IOException if a problem occurs with the message itself, like a
     *                     missing header or a malformed body
     */
    private void handleMessage(final Envelope envelope, final AMQP.BasicProperties properties, final byte[] body) throws IOException {
        final Map<String, Object> headers = properties.getHeaders();
        if (headers == null || !headers.containsKey(MessageHeader.TYPE.toString())) {
            throw new IOException("Message doesn't contain header " + MessageHeader.TYPE.toString());
        }

        final MessageTypeInbound messageType = MessageTypeInbound.valueOf(String.valueOf(headers.get(MessageHeader.TYPE.toString())));
        switch (messageType) {
            case EVENT:
                handleEvent(envelope, properties, body);
                break;
            case THING_DELETED:
                handleThingDeleted(envelope, properties);
                break;
            case PING_RESPONSE:
            default:
                handleUnsupportedMessageType(envelope, messageType);
                break;
        }
    }

    /**
     * Handle a hawkBit message from type {@link MessageTypeInbound#EVENT}.
     *
     * @param envelope   envelope of the AMQP message
     * @param properties properties (including the headers) of the AMQP message
     * @param body       body of the AMQP message
     * @throws IOException if a problem occurs with the message itself, like a
     *                     missing header or a malformed body
     */
    private void handleEvent(final Envelope envelope, final AMQP.BasicProperties properties, final byte[] body) throws IOException {
        final Map<String, Object> headers = properties.getHeaders();
        if (headers == null || !headers.containsKey(MessageHeader.TOPIC.toString())) {
            throw new IOException("Message doesn't contain header " + MessageHeader.TOPIC.toString());
        }

        final EventType eventType = EventType.valueOf(String.valueOf(headers.get(MessageHeader.TOPIC.toString())));
        switch (eventType) {
            case DOWNLOAD:
            case DOWNLOAD_AND_INSTALL:
                handleEventWithTopicDownload(envelope, properties, body);
                break;
            case CANCEL_DOWNLOAD:
                handleEventWithTopicCancelDownload(envelope, properties, body);
                break;
            case MULTI_ACTION:
            case REQUEST_ATTRIBUTES_UPDATE:
            default:
                handleEventWithUnsupportedTopic(envelope, eventType);
                break;
        }
    }

    /**
     * Handle a hawkBit download request message.
     *
     * @param envelope   envelope of the AMQP message
     * @param properties properties (including the headers) of the AMQP message
     * @param body       body of the AMQP message
     * @throws IOException if a problem occurs during mapping the body to
     *                     {@link DownloadRequestInboundMessage.Body} with a json
     *                     object mapper
     */
    private void handleEventWithTopicDownload(final Envelope envelope, final AMQP.BasicProperties properties, final byte[] body) {
        final Map<String, Object> headers = properties.getHeaders();
        
        DownloadRequestInboundMessage message;
        try {
            message = DownloadRequestInboundMessage.builder()
                    .headers(DownloadRequestInboundMessage.Headers.builder()
                            .type(headers.get(MessageHeader.TYPE.toString()).toString())
                            .thingId(headers.get(MessageHeader.THING_ID.toString()).toString())
                            .topic(headers.get(MessageHeader.TOPIC.toString()).toString())
                            .tenant(headers.get(MessageHeader.TENANT.toString()).toString()).build())
                    .body(this.objectMapper.readValue(body, DownloadRequestInboundMessage.Body.class))
                    .deliveryTag(envelope.getDeliveryTag()).build();
            
            wsService.sendDownloadEventMessage(message);

            acknowledgeMessageDelivery(envelope.getDeliveryTag());
        } catch (final JsonParseException e) {
            log.error("Could not parse inbound message: {}", e);
        } catch (final JsonMappingException e) {
            log.error("Could not map inbound message: {}", e);
        } catch (final IOException e) {
            log.error("Could not send message to device {}, as it was not reachable.", headers.get(MessageHeader.THING_ID.toString()).toString());
        } catch (final DeviceNotConnectedException e) {
            log.error("Device {} is currently not connected to HawkBit.", headers.get(MessageHeader.THING_ID.toString()).toString());
        }
    }
    
    /**
     * Handle CANCEL_DOWNLOAD messages. Send ACK only if parsing of the message and further
     * transport via WebSocket was successful.
     * 
     * @param envelope AMQP envelope
     * @param properties AMQP properties
     * @param body the actual amqp message payload
     */
    private void handleEventWithTopicCancelDownload(final Envelope envelope, final AMQP.BasicProperties properties, final byte[] body) {
        final Map<String, Object> headers = properties.getHeaders();
 
        CancelDownloadInboundMessage message;
        try {
            message = CancelDownloadInboundMessage.builder()
                .headers(
                    CancelDownloadInboundMessage.Headers.builder()
                        .type(headers.get(MessageHeader.TYPE.toString()).toString())
                        .thingId(headers.get(MessageHeader.THING_ID.toString()).toString())
                        .topic(headers.get(MessageHeader.TOPIC.toString()).toString())
                        .tenant(headers.get(MessageHeader.TENANT.toString()).toString())
                        .build()
                    )
                .body(this.objectMapper.readValue(body, CancelDownloadInboundMessage.Body.class)).build();

            this.wsService.sendCancelDownloadMessage(message);

            acknowledgeMessageDelivery(envelope.getDeliveryTag());
        } catch (final JsonParseException e) {
            log.error("Could not parse inbound message: {}", e);
        } catch (final JsonMappingException e) {
            log.error("Could not map inbound message: {}", e);
        } catch (final IOException e) {
            log.error("Could not send message to device {}, as it was not reachable.", headers.get(MessageHeader.THING_ID.toString()).toString());
        } catch (final DeviceNotConnectedException e) {
            log.error("Device {} is currently not connected to HawkBit.", headers.get(MessageHeader.THING_ID.toString()).toString());
        }
    }

    /**
     * Handle THING_DELETED messages. Send ACK only if parsing of the message and further
     * transport via Websocket was successful
     * 
     * @param envelope AMQP envelope
     * @param properties AMQP properties
     * @param body the actual AMQP message payload
     */
    private void handleThingDeleted(final Envelope envelope, final AMQP.BasicProperties properties) {
        final Map<String, Object> headers = properties.getHeaders();
        
        ThingDeletedInboundMessage message;
        try {
            message = ThingDeletedInboundMessage.builder()
                .headers(
                    ThingDeletedInboundMessage.Headers.builder()
                        .thingId(headers.get(MessageHeader.THING_ID.toString()).toString())
                        .build()
                )
                .build();

            this.wsService.sendThingDeletedMessage(message);

            acknowledgeMessageDelivery(envelope.getDeliveryTag());
        } catch (final JsonParseException e) {
            log.error("Could not parse inbound message: {}", e);
        } catch (final JsonMappingException e) {
            log.error("Could not map inbound message: {}", e);
        } catch (final IOException e) {
            log.error("Could not send message to device {}, as it was not reachable.", headers.get(MessageHeader.THING_ID.toString()).toString());
        } catch (final DeviceNotConnectedException e) {
            log.error("Device {} is currently not connected to HawkBit.", headers.get(MessageHeader.THING_ID.toString()).toString());
        }
    }


    /**
     * Handle a hawkBit message with unsupported message type. Send a negative acknowledgement to hawkBit for the
     * message with no requeuing.
     *
     * @param envelope    envelope of the AMQP message
     * @param messageType message type of the AMQP message
     * @throws IOException if there is a connection problem with hawkBit
     */
    private void handleUnsupportedMessageType(final Envelope envelope, final MessageTypeInbound messageType) throws IOException {
        log.error("Message with unsupported message type {} was received, responding with negative acknowledgement and no requeuing", messageType.toString());
        
        super.getChannel().basicNack(envelope.getDeliveryTag(), false, false);
    }

    /**
     * Handle a hawkBit message from message type {@link MessageTypeInbound#EVENT} and with unsupported event type. Send a
     * negative acknowledgement to hawkBit for the message with no requeuing.
     *
     * @param envelope  envelope of the AMQP message
     * @param eventType event type of the AMQP message
     * @throws IOException if there is a connection problem with hawkBit
     */
    private void handleEventWithUnsupportedTopic(final Envelope envelope, final EventType eventType) throws IOException {
        log.error("Message with message type {} and unsupported event topic {} was received, responding with negative acknowledgement and no requeuing", MessageTypeInbound.EVENT.toString(), eventType.toString());
        
        super.getChannel().basicNack(envelope.getDeliveryTag(), false, false);
    }

    private void acknowledgeMessageDelivery(final Long deliveryTag) {
        try {
            super.getChannel().basicAck(deliveryTag, false);
        } catch (final IOException e) {
            log.error("Could not acknowledge message delivery to HawkBit.");
        }
    }
}