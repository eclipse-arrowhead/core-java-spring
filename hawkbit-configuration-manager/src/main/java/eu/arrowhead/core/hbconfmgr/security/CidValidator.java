/********************************************************************************
* Copyright (c) 2021 Bosch.IO GmbH[ and others]
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0.
*
* SPDX-License-Identifier: EPL-2.0
********************************************************************************/

package eu.arrowhead.core.hbconfmgr.security;

import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.consumer.ErrorCodeValidator;
import org.jose4j.jwt.consumer.ErrorCodes;
import org.jose4j.jwt.consumer.JwtContext;

/**
 * The cid validator checks that the claim "cid" is available in a JWT.
 */
public class CidValidator implements ErrorCodeValidator {

    @Override
    public Error validate(final JwtContext jwtContext) {
        final JwtClaims jwtClaims = jwtContext.getJwtClaims();
        final Object clientIdentifier = jwtClaims.getClaimValue(JwtAuthenticationToken.CLIENT_IDENTIFIER_CLAIM);
        if (clientIdentifier == null ) {
            return new Error(ErrorCodes.MISCELLANEOUS, "No client identifier (cid) claim is present.");
        }

        return null;
    }
}