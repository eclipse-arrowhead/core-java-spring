/********************************************************************************
 * Copyright (c) 2019 AITIA
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   AITIA - implementation
 *   Arrowhead Consortia - conceptualization
 ********************************************************************************/

package eu.arrowhead.relay;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

import javax.validation.constraints.NotNull;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jwa.AlgorithmConstraints.ConstraintType;
import org.jose4j.jwe.JsonWebEncryption;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.lang.JoseException;
import org.springframework.util.Assert;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.internal.DecryptedMessageDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.AuthException;

public class RelayCryptographer {

	//=================================================================================================
	// members
	
	private static final String JWT_CONTENT_TYPE = "JWT";
	private static final AlgorithmConstraints JWS_ALG_CONSTRAINTS = new AlgorithmConstraints(ConstraintType.WHITELIST, CommonConstants.JWS_SIGN_ALG);
	private static final AlgorithmConstraints JWE_ALG_CONSTRAINTS = new AlgorithmConstraints(ConstraintType.WHITELIST, CommonConstants.JWE_KEY_MANAGEMENT_ALG);
	private static final AlgorithmConstraints JWE_ENCRYPTION_CONSTRAINTS = new AlgorithmConstraints(ConstraintType.WHITELIST, CommonConstants.JWE_ENCRYPTION_ALG);
	
	private final Logger logger = LogManager.getLogger(RelayCryptographer.class);

	private final ObjectMapper mapper = new ObjectMapper();
	private final PrivateKey privateKey;

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public RelayCryptographer(final PrivateKey privateKey) {
		Assert.notNull(privateKey, "Private key is null.");
		
		this.privateKey = privateKey;
	}
	
	//-------------------------------------------------------------------------------------------------
	public String encodeSessionId(final String sessionId, final String recipientPublicKey) {
		logger.debug("encodeSessionId started...");
		
		return encodeSessionId(sessionId, Utilities.getPublicKeyFromBase64EncodedString(recipientPublicKey));
	}
	
	//-------------------------------------------------------------------------------------------------
	public String encodeSessionId(final String sessionId, final PublicKey recipientPublicKey) {
		logger.debug("encodeSessionId started...");

		Assert.isTrue(!Utilities.isEmpty(sessionId), "Session id is null or blank.");
		Assert.notNull(recipientPublicKey, "Recipient public key is null.");
		
		try {
			return generateJWTString(null, sessionId, null, recipientPublicKey);
		} catch (final JsonProcessingException ex) {
			// never happens because no payload 
			throw new AssertionError(ex);		
		} catch (final JoseException ex) {
			logger.error("Problem occured when trying to sign and/or encrypt JWT token", ex);
			throw new ArrowheadException("Encoding failed.");
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	public String encodeRelayMessage(final String messageType, final String sessionId, final Object payload, final String recipientPublicKey) {
		logger.debug("encodeRelayMessage started...");
		
		return encodeRelayMessage(messageType, sessionId, payload, Utilities.getPublicKeyFromBase64EncodedString(recipientPublicKey));
	}
	
	//-------------------------------------------------------------------------------------------------
	public String encodeRelayMessage(final String messageType, final String sessionId, final Object payload, final PublicKey recipientPublicKey) {
		logger.debug("encodeRelayMessage started...");
		
		Assert.isTrue(!Utilities.isEmpty(messageType), "Message type is null or blank.");
		Assert.isTrue(!Utilities.isEmpty(sessionId), "Session id is null or blank.");
		Assert.notNull(recipientPublicKey, "Recipient public key is null.");

		try {
			return generateJWTString(messageType, sessionId, payload, recipientPublicKey);
		} catch (final JsonProcessingException ex) {
			logger.error("Can't convert payload with type {}", payload.getClass().getSimpleName());
			logger.debug(ex);
			throw new ArrowheadException("Can't convert payload with type " + payload.getClass().getSimpleName());
		} catch (final JoseException ex) {
			logger.error("Problem occured when trying to sign and/or encrypt JWT token", ex);
			throw new ArrowheadException("Encoding failed.");
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	public String encodeBytes(final byte[] bytes, final String recipientPublicKey) {
		logger.debug("encodeBytes started...");
		
		return encodeBytes(bytes, Utilities.getPublicKeyFromBase64EncodedString(recipientPublicKey));
	}
	
	//-------------------------------------------------------------------------------------------------
	public String encodeBytes(final byte[] bytes, final PublicKey recipientPublicKey) {
		logger.debug("encodeBytes started...");
		
		Assert.isTrue(bytes != null && bytes.length > 0, "Byte array is null or empty.");
		Assert.notNull(recipientPublicKey, "Recipient public key is null.");
		
		try {
			return generateJWTString(CoreCommonConstants.RELAY_MESSAGE_TYPE_RAW, null, bytes, recipientPublicKey);
		} catch (final JsonProcessingException ex) {
			// never happens because we do not use ObjectMapper to convert byte array to string 
			throw new AssertionError(ex);		
		} catch (final JoseException ex) {
			logger.error("Problem occured when trying to sign and/or encrypt JWT token", ex);
			throw new ArrowheadException("Encoding failed.");
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	public DecryptedMessageDTO decodeMessage(final String encryptedMessage, final String senderPublicKey) {
		logger.debug("decodeMessage started...");

		return decodeMessage(encryptedMessage, Utilities.getPublicKeyFromBase64EncodedString(senderPublicKey));
	}
	
	//-------------------------------------------------------------------------------------------------
	public DecryptedMessageDTO decodeMessage(final String encryptedMessage, final PublicKey senderPublicKey) {
		logger.debug("decodeMessage started...");
		
		Assert.isTrue(!Utilities.isEmpty(encryptedMessage), "Encrypted message is null or blank.");
		Assert.notNull(senderPublicKey, "Sender public key is null.");
		
		final JwtClaims claims = validateAndDecodeMessage(encryptedMessage, senderPublicKey);
		
		return convertToUnsecuredMessage(claims);
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private String generateJWTString(final String messageType, final String sessionId, final Object payload, final PublicKey recipientPublicKey) throws JoseException, JsonProcessingException { 
		final String signedJWT = generateSignedJWT(messageType, sessionId, payload);
		
		return encryptSignedJWT(signedJWT, recipientPublicKey);
	}
	
	//-------------------------------------------------------------------------------------------------
	private String generateSignedJWT(final String messageType, final String sessionId, final Object payload) throws JoseException, JsonProcessingException {
		final JwtClaims claims = generateClaims(messageType, sessionId, payload);
		final JsonWebSignature jws = new JsonWebSignature();
		jws.setPayload(claims.toJson());
		jws.setKey(privateKey);
		jws.setAlgorithmHeaderValue(CommonConstants.JWS_SIGN_ALG);
		
		return jws.getCompactSerialization();
	}
	
	//-------------------------------------------------------------------------------------------------
	private JwtClaims generateClaims(final String messageType, final String sessionId, final Object payload) throws JsonProcessingException {
		final JwtClaims claims = new JwtClaims();
		claims.setGeneratedJwtId();
		claims.setIssuedAtToNow();

		if (messageType != null) {
			claims.setStringClaim(CommonConstants.JWT_CLAIM_MESSAGE_TYPE, messageType.trim());
		}
		
		if (sessionId != null) {
			claims.setStringClaim(CommonConstants.JWT_CLAIM_SESSION_ID, sessionId.trim());
		}
		
		if (payload != null) {
			claims.setStringClaim(CommonConstants.JWT_CLAIM_PAYLOAD, convertPayloadToString(payload));
		}

		return claims;
	}
	
	//-------------------------------------------------------------------------------------------------
	private String convertPayloadToString(@NotNull final Object payload) throws JsonProcessingException {
		if (payload instanceof byte[]) {
			return Base64.getEncoder().encodeToString((byte[]) payload);
		} else {
			return mapper.writeValueAsString(payload);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private String encryptSignedJWT(final String signedJWT, final PublicKey recipientPublicKey) throws JoseException {
		final JsonWebEncryption jwe = new JsonWebEncryption();
		jwe.setAlgorithmHeaderValue(CommonConstants.JWE_KEY_MANAGEMENT_ALG);
		jwe.setEncryptionMethodHeaderParameter(CommonConstants.JWE_ENCRYPTION_ALG);
		jwe.setKey(recipientPublicKey);
		jwe.setContentTypeHeaderValue(JWT_CONTENT_TYPE);
		jwe.setPayload(signedJWT);
		
		return jwe.getCompactSerialization();
	}
	
	//-------------------------------------------------------------------------------------------------
	private JwtClaims validateAndDecodeMessage(final String encryptedMessage, final PublicKey senderPublicKey) {
		logger.debug("validateAndDecodeMessage started...");

		final JwtConsumer jwtConsumer = new JwtConsumerBuilder().setRequireJwtId()
																.setEnableRequireEncryption()
																.setEnableRequireIntegrity()
																.setDecryptionKey(privateKey)
																.setVerificationKey(senderPublicKey)
																.setJwsAlgorithmConstraints(JWS_ALG_CONSTRAINTS)
																.setJweAlgorithmConstraints(JWE_ALG_CONSTRAINTS)
																.setJweContentEncryptionAlgorithmConstraints(JWE_ENCRYPTION_CONSTRAINTS)
																.build();

		try {
			return jwtConsumer.processToClaims(encryptedMessage);
		} catch (final InvalidJwtException ex) {
			logger.debug("Message processing is failed: {}", ex.getMessage());
			logger.debug(ex);
			throw new AuthException("Message processing is failed", ex);
		} 
	}
	
	//-------------------------------------------------------------------------------------------------
	private DecryptedMessageDTO convertToUnsecuredMessage(final JwtClaims claims) {
		logger.debug("convertToUnsecuredMessage started...");
		
		try {
			final DecryptedMessageDTO result = new DecryptedMessageDTO();
			
			if (claims.hasClaim(CommonConstants.JWT_CLAIM_MESSAGE_TYPE)) {
				result.setMessageType(claims.getStringClaimValue(CommonConstants.JWT_CLAIM_MESSAGE_TYPE));
			}
			
			if (claims.hasClaim(CommonConstants.JWT_CLAIM_SESSION_ID)) {
				result.setSessionId(claims.getStringClaimValue(CommonConstants.JWT_CLAIM_SESSION_ID));
			}
			
			if (claims.hasClaim(CommonConstants.JWT_CLAIM_PAYLOAD)) {
				result.setPayload(claims.getStringClaimValue(CommonConstants.JWT_CLAIM_PAYLOAD));
			}
			
			return result;
		} catch (final MalformedClaimException ex) {
			logger.debug("Message processing is failed: {}", ex.getMessage());
			logger.debug(ex);
			throw new AuthException("Message processing is failed", ex);
		} 
	}
}