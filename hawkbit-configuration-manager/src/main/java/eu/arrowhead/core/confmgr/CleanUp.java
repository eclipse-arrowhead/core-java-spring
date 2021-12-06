/********************************************************************************
* Copyright (c) 2021 Bosch.IO GmbH[ and others]
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0.
*
* SPDX-License-Identifier: EPL-2.0
********************************************************************************/

package eu.arrowhead.core.confmgr;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.arrowhead.core.confmgr.arrowhead.ArrowheadServiceRegistryClient;
import eu.arrowhead.core.confmgr.properties.SystemProperties;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class CleanUp implements ServletContextListener {
    private final ArrowheadServiceRegistryClient arrowheadServiceRegistryClient;
    private final SystemProperties systemProperties;

    @Autowired
    public CleanUp(ArrowheadServiceRegistryClient arrowheadServiceRegistryClient, SystemProperties systemProperties) {
        this.arrowheadServiceRegistryClient = arrowheadServiceRegistryClient;
        this.systemProperties = systemProperties;
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        log.info("Unregister service from Local Cloud");
        arrowheadServiceRegistryClient.unregisterService(this.systemProperties.getAddress(), this.systemProperties.getPort(), this.systemProperties.getProvidedServiceDefinition(), this.systemProperties.getName());
    }
}
