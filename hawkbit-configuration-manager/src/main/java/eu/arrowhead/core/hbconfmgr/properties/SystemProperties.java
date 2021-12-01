/********************************************************************************
* Copyright (c) 2021 Bosch.IO GmbH[ and others]
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0.
*
* SPDX-License-Identifier: EPL-2.0
********************************************************************************/

package eu.arrowhead.core.hbconfmgr.properties;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.Data;

/**
 * This class provides all properties of the configuration system itself.
 */
@Data
public class SystemProperties {

    /**
     * This is the address of the configuration system itself, e.g. "192.168.1.1" or
     * "subdomain.domain.tld".
     * <p>It must be not blank (validated with {@link NotBlank @NotBlank}).
     */
    @NotBlank
    private String address;

    /**
     * This is the port of the configuration system itself, e.g. 8447.
     * <p>It must be not null (validated with {@link NotNull @NotNull}).
     */
    @NotNull
    private Integer port;

    /**
     * This is the name of the configuration system itself, e.g. "HAWKBITCONFIGURATIONSYSTEM".
     * <p>In the context of Arrowhead the system (provider) name and certificate common name must match.
     * <p>It must be not blank (validated with {@link NotBlank @NotBlank}).
     */
    @NotBlank
    private String name;

    /**
     * This is the protocol of the provided service from the configuration system itself, e.g. "HTTP-SECURE-JSON".
     * <p>It must be not blank (validated with {@link NotBlank @NotBlank}).
     */
    @NotBlank
    private String providedServiceInterface;

    /**
     * This is the definition of the provided service from the configuration system itself, e.g. "definition5".
     * <p>It must be not blank (validated with {@link NotBlank @NotBlank}).
     */
    @NotBlank
    private String providedServiceDefinition;

    /**
     * This is the uri of the provided service from the configuration system itself, e.g "/".
     * <p>It must be not blank (validated with {@link NotBlank @NotBlank}).
     */
    @NotBlank
    private String providedServiceUri;

    /**
     * This is the version of the provided service from the configuration system itself, e.g. 2.
     * <p>It must be not null (validated with {@link NotNull @NotNull}).
     */
    @NotNull
    private Integer providedServiceVersion;
}