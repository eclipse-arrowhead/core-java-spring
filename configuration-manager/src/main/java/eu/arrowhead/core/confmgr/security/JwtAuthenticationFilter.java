/********************************************************************************
* Copyright (c) 2021 Bosch.IO GmbH[ and others]
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0.
*
* SPDX-License-Identifier: EPL-2.0
********************************************************************************/

package eu.arrowhead.core.confmgr.security;

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

import eu.arrowhead.core.confmgr.service.ArrowheadService;
import lombok.extern.log4j.Log4j2;

/**
 * Implementation of JWT authentication filter for Spring Security. It supports bearer tokens (JWT) in the standard
 * HTTP authentication header "Authorization". A subject is authenticated if it provides a valid JWT with all required
 * claims according to {@link #validateJwtAndRetrieveClaims(String, PublicKey, PrivateKey)}.
 */
@Log4j2
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /**
     * The value of the header "Authorization" must start with "Bearer " according to
     * <a href=https://tools.ietf.org/html/rfc6750>RFC6750</a>.
     */
    private static final String AUTHENTICATION_SCHEME_BEARER = "Bearer ";

    private final ArrowheadService arrowheadService;
    private final PrivateKey configurationSystemPrivateKey;

    /**
     * A filter for validation JWT.
     *
     * @param arrowheadService required to receive the public key for JWT validation
     * @param configurationSystemPrivateKey the private key for JWT encryption
     */
    public JwtAuthenticationFilter(ArrowheadService arrowheadService, PrivateKey configurationSystemPrivateKey) {
        this.arrowheadService = arrowheadService;
        this.configurationSystemPrivateKey = configurationSystemPrivateKey;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authorizationHeader == null || !authorizationHeader.startsWith(AUTHENTICATION_SCHEME_BEARER)) {
            log.debug("No bearer token found");
            chain.doFilter(request, response);
            return;
        }
        String clientJwtString = authorizationHeader.replaceFirst(AUTHENTICATION_SCHEME_BEARER, "");

        try {

            SecurityContext context = SecurityContextHolder.createEmptyContext();
            String authorizationSystemPublicKeyString = arrowheadService.receiveAuthorizationSystemPublicKey();
            PublicKey authorizationSystemPublicKey = loadPublicKeyFromString(authorizationSystemPublicKeyString);

            JwtClaims clientJwtClaims = validateJwtAndRetrieveClaims(clientJwtString, authorizationSystemPublicKey,
                    this.configurationSystemPrivateKey);
            JwtAuthenticationToken authentication = new JwtAuthenticationToken(clientJwtClaims, true);
            context.setAuthentication(authentication);

            SecurityContextHolder.setContext(context);

        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            log.error("Public key from JWT provider couldn't be loaded", e);
        } catch (InvalidJwtException  e) {
            log.info("The provided JWT is not valid", e);
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
    private JwtClaims validateJwtAndRetrieveClaims(String clientJwt, PublicKey publicKey, PrivateKey privateKey)
            throws InvalidJwtException {
        JwtConsumer jwtConsumer = new JwtConsumerBuilder()
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

    private PublicKey loadPublicKeyFromString(String publicKeyString) throws NoSuchAlgorithmException,
            InvalidKeySpecException {
        KeyFactory kf = KeyFactory.getInstance("RSA");
        byte[] keyBytes = Base64.getDecoder().decode(publicKeyString);
        return kf.generatePublic(new X509EncodedKeySpec(keyBytes));
    }

}