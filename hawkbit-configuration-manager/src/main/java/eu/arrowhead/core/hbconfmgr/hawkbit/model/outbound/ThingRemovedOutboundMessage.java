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

import javax.validation.constraints.NotNull;

import eu.arrowhead.core.hbconfmgr.hawkbit.model.MessageHeader;
import lombok.Builder;
import lombok.Data;


/**
 * Transfer object, encapsulating THING_REMOVED requests, sent by the HawkBit client.
 */
@Builder
@Data
public class ThingRemovedOutboundMessage {

    @NotNull
    private Headers headers;
    
    @Builder
    @Data
    public static class Headers {
        private final String type = MessageTypeOutbound.THING_REMOVED.toString();
        private String thingId;
        private String tenant;

        public Map<String, Object> asMap() {
            final HashMap<String, Object> headers = new HashMap<>();
            if (this.type != null) {
            	headers.put(MessageHeader.TYPE.toString(), this.type);
            }
            if (this.thingId != null) {
            	headers.put(MessageHeader.THING_ID.toString(), this.thingId);
            }
            if (this.tenant != null) {
            	headers.put(MessageHeader.TENANT.toString(), this.tenant);
            }
            
            return headers;
        }
    }
}