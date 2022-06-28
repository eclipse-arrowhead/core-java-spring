/********************************************************************************
* Copyright (c) 2021 Bosch.IO GmbH[ and others]
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0.
*
* SPDX-License-Identifier: EPL-2.0
********************************************************************************/

package eu.arrowhead.core.hbconfmgr.hawkbit.model.inbound;

import lombok.Builder;
import lombok.Data;

/**
 * Transfer object, encapsulating Thing Deleted messages, received by the HawkBit consumer.
 */
@Builder
@Data
public class ThingDeletedInboundMessage implements InboundMessage {
    private Headers headers;

    @Builder
    @Data
    public static class Headers {
        private String thingId;
    }
}