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
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.Validator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;

import eu.arrowhead.core.hbconfmgr.Constants;
import eu.arrowhead.core.hbconfmgr.hawkbit.model.outbound.ThingCreatedOutboundMessage;
import eu.arrowhead.core.hbconfmgr.hawkbit.model.outbound.ThingRemovedOutboundMessage;
import eu.arrowhead.core.hbconfmgr.hawkbit.model.outbound.UpdateActionStatusOutboundMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;


/**
 * Handles all outbound interaction with Eclipse HawkBit via its DMF API.
 */
@Log4j2
@Component
public class HawkbitDmfOutboundClient {

    private final Channel channel;
    private final ObjectMapper objectMapper;
    private final Validator validator;

    /**
     * Initialize a new hawkBit client that operates on the hawkBit device management federation (DMF) API. It is
     * used for upstream communication (sending messages to hawkBit). Further information to the upstream DMF API can
     * be found in the <a href=https://www.eclipse.org/hawkbit/apis/dmf_api/>DMF API documentation</a> under
     * <i>"Messages sent to hawkBit (Client -> hawkBit)"</i>
     *
     * @param channel used for communication with hawkBit
     */
    @Autowired
    public HawkbitDmfOutboundClient(final Channel channel) {
        this.channel = channel;
        this.objectMapper = new ObjectMapper();
        this.validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    /**
     * Send a message to hawkBit on the device management federation (DMF) API to create a new thing. A description of
     * this API can be found in the <a href=https://www.eclipse.org/hawkbit/apis/dmf_api/>DMF API documentation</a>
     * under <i>"Messages sent to hawkBit (Client -> hawkBit)"</i> -> <i>"THING_CREATED"</i>.
     *
     * @param message the message to publish with body and headers
     * @throws ConstraintViolationException if message is not valid
     * @throws IOException                  if there is a connection problem with hawkBit
     */
    public void createThing(final ThingCreatedOutboundMessage message) throws ConstraintViolationException, IOException {
        log.debug("Validating ThingCreatedOutboundMessage");
        
        final Set<ConstraintViolation<ThingCreatedOutboundMessage>> violations = validator.validate(message);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        final byte[] byteBody = this.objectMapper.writeValueAsBytes(message.getBody());
        final AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder()
                .contentType(Constants.APPLICATION_JSON)
                .headers(message.getHeaders().asMap())
                .replyTo(HawkbitDmfConstants.RECEIVING_EXCHANGE)
                .build();
        
        log.debug("Sending ThingCreatedOutboundMessage to exchange {}", HawkbitDmfConstants.SENDING_EXCHANGE);
        this.channel.basicPublish(HawkbitDmfConstants.SENDING_EXCHANGE, HawkbitDmfConstants.SENDING_ROUTING_KEY, properties, byteBody);
    }

    /**
     * Send a message to hawkBit on the device management federation (DMF) API to create a new thing. A description of
     * this API can be found in the <a href=https://www.eclipse.org/hawkbit/apis/dmf_api/>DMF API documentation</a>
     * under <i>"Messages sent to hawkBit (Client -> hawkBit)"</i> -> <i>"UPDATE_ACTION_STATUS"</i>.
     *
     * @param message the message to publish with body and headers
     * @throws ConstraintViolationException if message is not valid
     * @throws IOException                  if there is a connection problem with hawkBit
     */
    public void updateActionStatus(final UpdateActionStatusOutboundMessage message) throws IOException {
        log.debug("Validating UpdateActionStatusOutboundMessage");
        
        final Set<ConstraintViolation<UpdateActionStatusOutboundMessage>> violations = validator.validate(message);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        final byte[] byteBody = this.objectMapper.writeValueAsBytes(message.getBody());
        final AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder()
                .contentType(Constants.APPLICATION_JSON)
                .headers(message.getHeaders().asMap())
                .build();
        
        log.debug("Sending UpdateActionStatusOutboundMessage to exchange {}", HawkbitDmfConstants.SENDING_EXCHANGE);
        this.channel.basicPublish(HawkbitDmfConstants.SENDING_EXCHANGE, HawkbitDmfConstants.SENDING_ROUTING_KEY, properties, byteBody);
    }

    /**
     * Send a message to hawkBit via the device management federation (DMF) API to remove a thing. A description of
     * this API can be found in the <a href=https://www.eclipse.org/hawkbit/apis/dmf_api/>DMF API documentation</a>
     * under <i>"Messages sent to hawkBit (Client -> hawkBit)"</i> -> <i>"THING_REMOVED"</i>.
     * @param message the actual message to be published
     * @throws IOException
     */
    public void removeThing(final ThingRemovedOutboundMessage message) throws IOException {
        final Set<ConstraintViolation<ThingRemovedOutboundMessage>> violations = validator.validate(message);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        final byte[] byteBody = "".getBytes();
        final AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder()
                .contentType(Constants.APPLICATION_JSON)
                .headers(message.getHeaders().asMap())
                .build();

        this.channel.basicPublish(HawkbitDmfConstants.SENDING_EXCHANGE, HawkbitDmfConstants.SENDING_ROUTING_KEY, properties, byteBody);
    }
}