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
import java.util.List;
import java.util.Map;

import javax.validation.ConstraintViolationException;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import eu.arrowhead.core.hbconfmgr.hawkbit.model.ActionStatus;
import eu.arrowhead.core.hbconfmgr.hawkbit.model.EventType;
import eu.arrowhead.core.hbconfmgr.hawkbit.model.MessageHeader;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * Transfer object, encapsulating UPDATE_ACTION_STATUS requests, sent by the HawkBit client.
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class UpdateActionStatusOutboundMessage {

    @Valid
    @NotNull
    private UpdateActionStatusOutboundMessageBody body;

    @Valid
    @NotNull
    private UpdateActionStatusOutboundMessageHeaders headers;

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    @Builder
    public static class UpdateActionStatusOutboundMessageBody {
        @NotNull
        private Long actionId;

        @NotNull
        private ActionStatus actionStatus;

        private Long softwareModuleId;

        private List<String> message;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    @Builder
    public static class UpdateActionStatusOutboundMessageHeaders {
        @NotNull
        private final String type = MessageTypeOutbound.EVENT.toString();

        @NotNull
        private final String topic = EventType.UPDATE_ACTION_STATUS.toString();

        private String tenant;

        public Map<String, Object> asMap() throws ConstraintViolationException {
            final HashMap<String, Object> headers = new HashMap<>();
            if (this.type != null) {
            	headers.put(MessageHeader.TYPE.toString(), this.type);
            }
            if (this.topic != null) {
            	headers.put(MessageHeader.TOPIC.toString(), this.topic);
            }
            if (this.tenant != null) {
            	headers.put(MessageHeader.TENANT.toString(), this.tenant);
            }
            
            return headers;
        }
    }
}