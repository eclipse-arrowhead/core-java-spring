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

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * Transfer object, encapsulating Cancel Download requests, received by the HawkBit consumer.
 */
@Builder
@Data
public class CancelDownloadInboundMessage implements InboundMessage {
	
    private Headers headers;
    private Body body;
    
    @Builder
    @Data
    public static class Headers {
        private String type;
        private String thingId;
        private String topic;
        private String tenant;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @Data
    public static class Body {
        private Long actionId;
    }
}