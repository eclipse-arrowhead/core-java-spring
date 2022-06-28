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

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * Transfer object, encapsulating Download Requests, received by the HawkBit consumer.
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class DownloadRequestInboundMessage implements InboundMessage {

    private Body body;
    private Headers headers;
    private Long deliveryTag;

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    @Builder
    public static class Body {
        private Long actionId;
        private String targetSecurityToken;
        private List<SoftwareModule> softwareModules;

        @AllArgsConstructor
        @NoArgsConstructor
        @Data
        @Builder
        public static class SoftwareModule {
            private Long moduleId;
            private String moduleType;
            private String moduleVersion;
            private List<Artifact> artifacts;
            private List<Metadata> metadata;
        }

        @AllArgsConstructor
        @NoArgsConstructor
        @Data
        @Builder
        public static class Artifact {
            private String filename;
            private Urls urls;
            private Hashes hashes;
            private Long size;
            private Date lastModified;
        }

        @AllArgsConstructor
        @NoArgsConstructor
        @Data
        @Builder
        public static class Urls {
            @JsonProperty("COAP")
            private String COAP;
            @JsonProperty("HTTP")
            private String HTTP;
            @JsonProperty("HTTPS")
            private String HTTPS;
        }

        @AllArgsConstructor
        @NoArgsConstructor
        @Data
        @Builder
        public static class Hashes {
            private String md5;
            private String sha1;
        }

        @AllArgsConstructor
        @NoArgsConstructor
        @Data
        @Builder
        public static class Metadata {
            private String key;
            private String value;
        }
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    @Builder
    public static class Headers {
        private String type;
        private String thingId;
        private String topic;
        private String tenant;
    }
}