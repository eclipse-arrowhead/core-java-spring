/********************************************************************************
* Copyright (c) 2021 Bosch.IO GmbH[ and others]
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0.
*
* SPDX-License-Identifier: EPL-2.0
********************************************************************************/

package eu.arrowhead.core.hbconfmgr.hawkbit.model.outbound;

import java.util.HashMap;
import java.util.Map;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import eu.arrowhead.core.hbconfmgr.hawkbit.model.MessageHeader;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * Transfer object, encapsulating THING_CREATED requests, sent by the HawkBit client.
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ThingCreatedOutboundMessage {

    @Valid
    @NotNull
    private Body body;

    @Valid
    @NotNull
    private Headers headers;

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    @Builder
    public static class Body {
        private String name;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    @Builder
    public static class Headers {
        @NotNull
        private final String type = MessageTypeOutbound.THING_CREATED.toString();

        @NotNull
        private String thingId;

        private String sender;

        private String tenant;

        public Map<String, Object> asMap() {
            final HashMap<String, Object> headers = new HashMap<>();
            if (this.type != null) {
            	headers.put(MessageHeader.TYPE.toString(), this.type);
            }
            if (this.thingId != null) {
            	headers.put(MessageHeader.THING_ID.toString(), this.thingId);
            }
            if (this.sender != null) {
            	headers.put(MessageHeader.SENDER.toString(), this.sender);
            }
            if (this.tenant != null) {
            	headers.put(MessageHeader.TENANT.toString(), this.tenant);
            }
            
            return headers;
        }
    }
}