/********************************************************************************
* Copyright (c) 2021 Bosch.IO GmbH[ and others]
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0.
*
* SPDX-License-Identifier: EPL-2.0
********************************************************************************/

package eu.arrowhead.core.hbconfmgr.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.arrowhead.core.hbconfmgr.arrowhead.ArrowheadAuthorizationSystemClient;

/**
 * This is the Arrowhead service that implements business logic specifically for the configuration system.
 */
@Service
public class ArrowheadService {

    private final ArrowheadAuthorizationSystemClient authorizationSystemClient;

    @Autowired
    public ArrowheadService(final ArrowheadAuthorizationSystemClient arrowheadAuthorizationSystemClient) {
        this.authorizationSystemClient = arrowheadAuthorizationSystemClient;
    }

    /**
     * Returns the public key of the Arrowhead authorization system. The public key is used for validating JWT issued
     * by the authorization system itself.
     * <p>
     * The result of this method is cached, the configuration for caching can be found in <i>resources/ehcache.xml</i>.
     *
     * @return public key of Arrowhead authorization system
     */
    public String receiveAuthorizationSystemPublicKey() {
        return this.authorizationSystemClient.getPublicKey();
    }
}