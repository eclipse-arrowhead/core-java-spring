/********************************************************************************
 * Copyright (c) 2020 Evopro
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Evopro - implementation
 *   Arrowhead Consortia - conceptualization
 ********************************************************************************/

package eu.arrowhead.core.certificate_authority;

import java.security.Security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import eu.arrowhead.common.ApplicationInitListener;

@Component
public class CertificateAuthorityApplicationInitListener extends ApplicationInitListener {

    @Override
    protected void customInit(ContextRefreshedEvent event) {
        logger.debug("customInit started...");

        Security.addProvider(new BouncyCastleProvider());
    }

}
