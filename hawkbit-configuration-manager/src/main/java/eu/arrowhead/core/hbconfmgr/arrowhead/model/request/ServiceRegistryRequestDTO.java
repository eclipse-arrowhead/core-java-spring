/********************************************************************************
* Copyright (c) 2021 Bosch.IO GmbH[ and others]
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0.
*
* SPDX-License-Identifier: EPL-2.0
********************************************************************************/

package eu.arrowhead.core.hbconfmgr.arrowhead.model.request;

import java.util.List;
import java.util.Map;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * DTO class for Service Registry requests. Carries Arrowhead-specific information,
 * necessary for the registration of the configuration system wrapper in the Service Registry.
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ServiceRegistryRequestDTO {

    @NotNull
    private String serviceDefinition;

    @Valid
    @NotNull
    private SystemRequestDTO providerSystem;

    @NotNull
    private String serviceUri;

    private String endOfValidity;

    private Enum<SecurityLevel> secure;

    private Map<String, String> metadata;

    private Integer version;

    @NotEmpty
    private List<String> interfaces;

    public enum SecurityLevel {
        NOT_SECURE, CERTIFICATE, TOKEN
    }
}