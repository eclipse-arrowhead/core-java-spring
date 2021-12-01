/********************************************************************************
* Copyright (c) 2021 Bosch.IO GmbH[ and others]
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0.
*
* SPDX-License-Identifier: EPL-2.0
********************************************************************************/

package eu.arrowhead.core.hbconfmgr.arrowhead.model.response;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * DTO class for the Service Registry responses. Carries necessary information about the successful registration
 * of the configuration system in the Service Registry.
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ServiceRegistryResponseDTO {

    private long id;
    private ServiceDefinitionResponseDTO serviceDefinition;
    private SystemResponseDTO provider;
    private String serviceUri;
    private String endOfValidity;
    private String secure;
    private Map<String, String> metadata;
    private Integer version;
    private List<ServiceInterfaceResponseDTO> interfaces;
    private String createdAt;
    private String updatedAt;
}