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

import javax.validation.ConstraintViolationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import eu.arrowhead.core.hbconfmgr.Constants;
import eu.arrowhead.core.hbconfmgr.hawkbit.HawkbitDmfOutboundClient;
import eu.arrowhead.core.hbconfmgr.hawkbit.model.outbound.ThingCreatedOutboundMessage;
import eu.arrowhead.core.hbconfmgr.hawkbit.model.outbound.ThingRemovedOutboundMessage;
import eu.arrowhead.core.hbconfmgr.hawkbit.model.outbound.UpdateActionStatusOutboundMessage;
import eu.arrowhead.core.hbconfmgr.model.HawkbitActionUpdateStatus;
import lombok.extern.log4j.Log4j2;

/**
 * This is the hawkBit service that implements business logic specifically for
 * the configuration system.
 */
@Log4j2
@Service
public class HawkbitService {

    private final HawkbitDmfOutboundClient hawkbitDmfClient;
    private final String hawkbitTenant;

    @Autowired
    public HawkbitService(@Value(Constants.HAWKBIT_TENANT) final String hawkbitTenant, final HawkbitDmfOutboundClient hawkbitDmfClient) {
        this.hawkbitDmfClient = hawkbitDmfClient;
        this.hawkbitTenant = hawkbitTenant;
    }

    /**
     * Create a new device in HawkBit.
     *
     * @param deviceId the id of the device that should be create
     */
    public void createDevice(final String deviceId) {
        try {
            final ThingCreatedOutboundMessage message = ThingCreatedOutboundMessage.builder()
                .body(
                    ThingCreatedOutboundMessage.Body.builder()
                        .name(deviceId)
                        .build()
                    )
                .headers(
                    ThingCreatedOutboundMessage.Headers.builder()
                        .thingId(deviceId)
                        .tenant(hawkbitTenant)
                        .build()
                    )
                .build();
            this.hawkbitDmfClient.createThing(message);
        } catch (final IOException e) {
            log.error("Creating new device was not possible", e);
        }
    }

    public void removeDevice(final String deviceId) {
        final ThingRemovedOutboundMessage message = ThingRemovedOutboundMessage.builder()
            .headers(
                ThingRemovedOutboundMessage.Headers.builder()
                    .thingId(deviceId)
                    .tenant(hawkbitTenant)
                    .build()
            )
            .build();

        try {
            this.hawkbitDmfClient.removeThing(message);
        } catch (final IOException e) {
            log.error("Deleting the device was not possible", e);
        }
    }

    /**
     * Update the status of an action in hawkBit.
     *
     * @param actionUpdateStatus contains the information for the new status of an
     *                           action
     * @throws ConstraintViolationException if a specification for sending a message
     *                                      to hawkBit is not adhered
     * @throws IOException                  if there is a connection problem with
     *                                      hawkBit
     */
    public void updateActionStatus(final HawkbitActionUpdateStatus actionUpdateStatus) throws ConstraintViolationException, IOException {
        final UpdateActionStatusOutboundMessage message = UpdateActionStatusOutboundMessage.builder()
                .body(UpdateActionStatusOutboundMessage.UpdateActionStatusOutboundMessageBody.builder()
                        .actionId(actionUpdateStatus.getActionId())
                        .actionStatus(actionUpdateStatus.getActionStatus())
                        .softwareModuleId(actionUpdateStatus.getSoftwareModuleId())
                        .message(actionUpdateStatus.getMessage()).build())
                .headers(UpdateActionStatusOutboundMessage.UpdateActionStatusOutboundMessageHeaders.builder()
                        .tenant(hawkbitTenant).build())
                .build();
        this.hawkbitDmfClient.updateActionStatus(message);
    }
}