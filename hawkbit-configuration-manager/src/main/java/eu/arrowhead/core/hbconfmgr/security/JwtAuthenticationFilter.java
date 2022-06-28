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

import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jwe.ContentEncryptionAlgorithmIdentifiers;
import org.jose4j.jwe.KeyManagementAlgorithmIdentifiers;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import eu.arrowhead.core.hbconfmgr.service.ArrowheadService;
import lombok.extern.log4j.Log4j2;

/**
 * Implementation of JWT authentication filter for Spring Security. It supports bearer tokens (JWT) in the standard
 * HTTP authentication header "Authorization". A subject is authenticated if it provides a valid JWT with all required
 * claims according to {@link #validateJwtAndRetrieveClaims(String, PublicKey, PrivateKey)}.
 */
@Log4j2
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String RSA = "RSA";

	/**
     * The value of the header "Authorization" must start with "Bearer " according to
     * <a href=https://tools.ietf.org/html/rfc6750>RFC6750</a>.
     */
    private static final String AUTHENTICATION_SCHEME_BEARER = "Bearer ";

    private final ArrowheadService arrowheadService;
    private final PrivateKey hawkbitConfigurationSystemPrivateKey;
    private PublicKey authorizationSystemPublicKey;

    /**
     * A filter for validation JWT.
     *
     * @param arrowheadService required to receive the public key for JWT validation
     * @param configurationSystemPrivateKey the private key for JWT encryption
     */
    public JwtAuthenticationFilter(final ArrowheadService arrowheadService, final PrivateKey configurationSystemPrivateKey) {
        this.arrowheadService = arrowheadService;
        this.hawkbitConfigurationSystemPrivateKey = configurationSystemPrivateKey;
        init();
    }
    
    private void init() {
    	try {
    		final String authorizationSystemPublicKeyString = arrowheadService.receiveAuthorizationSystemPublicKey();
    		this.authorizationSystemPublicKey = loadPublicKeyFromString(authorizationSystemPublicKeyString);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            log.error("Public key from JWT provider couldn't be loaded", e);
        }
    }

    @Override
    protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response, final FilterChain chain) throws IOException, ServletException {
        final String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (this.authorizationSystemPublicKey == null || authorizationHeader == null || !authorizationHeader.startsWith(AUTHENTICATION_SCHEME_BEARER)) {
            log.debug("No bearer token found");
            chain.doFilter(request, response);
            return;
        }
        final String clientJwtString = authorizationHeader.replaceFirst(AUTHENTICATION_SCHEME_BEARER, "");

        try {

            final SecurityContext context = SecurityContextHolder.createEmptyContext();
            final JwtClaims clientJwtClaims = validateJwtAndRetrieveClaims(clientJwtString, this.authorizationSystemPublicKey, this.hawkbitConfigurationSystemPrivateKey);
            final JwtAuthenticationToken authentication = new JwtAuthenticationToken(clientJwtClaims, true);
            context.setAuthentication(authentication);

            SecurityContextHolder.setContext(context);
        } catch (final InvalidJwtException  e) {
            log.error("The provided JWT is not valid", e);
        } finally {
            chain.doFilter(request, response);
        }
    }

    /**
     * Validates a JWT and retrieves it claims.
     *
     * @param clientJwt  the JWT that should be validated
     * @param publicKey  the public key for verification of the JWT
     * @param privateKey the private key for encryption of the JWT
     * @return on success: all claims of the JWT
     * @throws InvalidJwtException if the JWT is not valid
     */
    private JwtClaims validateJwtAndRetrieveClaims(final String clientJwt, final PublicKey publicKey, final PrivateKey privateKey) throws InvalidJwtException {
        final JwtConsumer jwtConsumer = new JwtConsumerBuilder()
                .registerValidator(new CidValidator())
                .setRequireJwtId()
                .setRequireNotBefore()
                .setEnableRequireEncryption()
                .setEnableRequireIntegrity()
                .setDecryptionKey(privateKey)
                .setVerificationKey(publicKey)
                .setJwsAlgorithmConstraints(new AlgorithmConstraints(
                        AlgorithmConstraints.ConstraintType.WHITELIST,
                        AlgorithmIdentifiers.RSA_USING_SHA512))
                .setJweAlgorithmConstraints(new AlgorithmConstraints(
                        AlgorithmConstraints.ConstraintType.WHITELIST,
                        KeyManagementAlgorithmIdentifiers.RSA_OAEP_256))
                .setJweContentEncryptionAlgorithmConstraints(new AlgorithmConstraints(
                        AlgorithmConstraints.ConstraintType.WHITELIST,
                        ContentEncryptionAlgorithmIdentifiers.AES_256_CBC_HMAC_SHA_512))
                .build();
        
        return jwtConsumer.processToClaims(clientJwt);
    }

    private PublicKey loadPublicKeyFromString(final String publicKeyString) throws NoSuchAlgorithmException, InvalidKeySpecException {
        final KeyFactory kf = KeyFactory.getInstance(RSA);
        final byte[] keyBytes = Base64.getDecoder().decode(publicKeyString);
        return kf.generatePublic(new X509EncodedKeySpec(keyBytes));
    }
}