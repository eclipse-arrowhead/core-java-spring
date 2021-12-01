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
import org.springframework.security.authentication.AbstractAuthenticationToken;

public class JwtAuthenticationToken extends AbstractAuthenticationToken {
    private static final long serialVersionUID = 1L;

    public static final String CLIENT_IDENTIFIER_CLAIM = "cid";

    private final String clientIdentifier;
    private final JwtClaims jwtClaims;

    /**
     * Creates a JWT (JSON Web Token) authentication token that can be used in the context of Spring Security.
     *
     * @param jwtClaims the claims of the JWT itself
     * @param authenticated if the JWT is already validated and therefore authenticated
     */
    public JwtAuthenticationToken(final JwtClaims jwtClaims, final boolean authenticated) {
        super(null);
        this.jwtClaims = jwtClaims;
        this.clientIdentifier = String.valueOf(jwtClaims.getClaimValue(CLIENT_IDENTIFIER_CLAIM));
        super.setAuthenticated(authenticated);
    }

    /**
     *
     * @return the client identifier (cid claim) of the JWT
     */
    @Override
    public String getName() {
        return this.clientIdentifier;
    }

    @Override
    public Object getPrincipal() {
        return this.jwtClaims;
    }

    @Override
    public Object getCredentials() {
        return this.jwtClaims.toString();
    }
}