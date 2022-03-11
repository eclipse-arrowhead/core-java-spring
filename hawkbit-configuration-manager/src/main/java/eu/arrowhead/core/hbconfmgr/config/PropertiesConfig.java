/********************************************************************************
* Copyright (c) 2021 Bosch.IO GmbH[ and others]
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0.
*
* SPDX-License-Identifier: EPL-2.0
********************************************************************************/

package eu.arrowhead.core.hbconfmgr.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import eu.arrowhead.core.hbconfmgr.properties.SystemProperties;

/**
 * This class instantiates properties classes, populates them from Spring properties files (like application.yml)
 * and provides them as beans managed by the Spring container.
 */
@Configuration
public class PropertiesConfig {
	
	private static final String SYSTEM = "system";

    /**
     * This makes use of Spring properties files (like application.yml), loads all properties under the
     * prefix "system" into the newly instantiated class {@link SystemProperties} and provides it as a bean
     * managed by the Spring container.
     *
     * @return a populated and validated instance of {@link SystemProperties}
     */
    @Bean
    @ConfigurationProperties(prefix = SYSTEM)
    public SystemProperties systemProperties() {
        return new SystemProperties();
    }

}
