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

package eu.arrowhead.core.authorization.token;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jose4j.jwe.JsonWebEncryption;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.lang.JoseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.entity.Cloud;
import eu.arrowhead.common.database.service.CommonDBService;
import eu.arrowhead.common.dto.internal.DTOConverter;
import eu.arrowhead.common.dto.internal.TokenGenerationDetailedResponseDTO;
import eu.arrowhead.common.dto.internal.TokenGenerationMultiServiceResponseDTO;
import eu.arrowhead.common.dto.internal.TokenGenerationProviderDTO;
import eu.arrowhead.common.dto.internal.TokenGenerationRequestDTO;
import eu.arrowhead.common.dto.internal.TokenGenerationResponseDTO;
import eu.arrowhead.common.dto.shared.CloudRequestDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.AuthException;
import eu.arrowhead.common.exception.DataNotFoundException;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.common.verifier.ServiceInterfaceNameVerifier;

// works only in secure mode
@Service
public class TokenGenerationService {

	//=================================================================================================
	// members

	private final Logger logger = LogManager.getLogger(TokenGenerationService.class);
	
	private static final String DOT = ".";
	private static final String JWT_CONTENT_TYPE = "JWT";
	
	@Value(CommonConstants.$SERVER_SSL_ENABLED_WD)
	private boolean sslEnabled;
	
	@Resource(name = CommonConstants.ARROWHEAD_CONTEXT)
	private Map<String,Object> arrowheadContext;
	
	@Autowired
	private CommonDBService commonDBService;
	
	@Autowired
	private ServiceInterfaceNameVerifier interfaceNameVerifier;
	
	private String ownCloudName;
	private String ownCloudOperator;

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public Map<SystemRequestDTO,Map<String,String>> generateTokens(final TokenGenerationRequestDTO request) {
		logger.debug("generateTokens started...");
		
		if (!sslEnabled) {
			throw new ArrowheadException("Token generation is not possible in insecure mode.");
		}
		
		Assert.notNull(request, "Request is null.");
		checkTokenGenerationRequest(request);
		
		final Map<SystemRequestDTO,PublicKey> publicKeys = getProviderPublicKeys(request.getProviders());
		final Map<SystemRequestDTO,Map<String,String>> result = new HashMap<>(publicKeys.size());
		final String consumerInfo = generateConsumerInfo(request.getConsumer(), request.getConsumerCloud());
		
		for (final TokenGenerationProviderDTO provider : request.getProviders()) {
			final SystemRequestDTO providerSystem = provider.getProvider();
			if (publicKeys.containsKey(providerSystem)) {
				for (final String intf : provider.getServiceInterfaces()) {
					String signedJWT;
					try {
						signedJWT = generateSignedJWT(consumerInfo, request.getService(), intf, provider.getTokenDuration() < 0 ? null: provider.getTokenDuration());
					} catch (final JoseException ex) {
						logger.error("Problem occured when trying to sign JWT token", ex);
						throw new ArrowheadException("Token generation failed"); // if there is a problem here, all calling cause the same problem 
					}
					
					try {
						final String encryptedJWT = encryptSignedJWT(signedJWT, publicKeys.get(providerSystem));
						
						Map<String,String> tokens = result.get(providerSystem);
						if (tokens == null) {
							tokens = new HashMap<>();
							result.put(providerSystem, tokens);
						}
						tokens.put(intf, encryptedJWT);
					} catch (final JoseException ex) {
						logger.error("Problem occured when trying to encrypt signed JWT token", ex);
					}
				}
			}
		}
		
		if (result.isEmpty()) {
			throw new ArrowheadException("Token generation failed for all the provider systems.");
		}
		
		return result;
	}
	
	//-------------------------------------------------------------------------------------------------
	public TokenGenerationResponseDTO generateTokensResponse(final TokenGenerationRequestDTO request) {
		logger.debug("generateTokensResponse started...");
		final Map<SystemRequestDTO,Map<String,String>> tokenMap = generateTokens(request);
		return DTOConverter.convertTokenMapToTokenGenerationResponseDTO(tokenMap);
	}
	
	//-------------------------------------------------------------------------------------------------
	public TokenGenerationMultiServiceResponseDTO generateMultiServiceTokensResponse(final List<TokenGenerationRequestDTO> requestList) {
		logger.debug("generateMultiServiceTokensResponse started...");
		
		final List<TokenGenerationDetailedResponseDTO> data = new ArrayList<>();
		for (final TokenGenerationRequestDTO request : requestList) {
			final TokenGenerationDetailedResponseDTO tokenDetails = new TokenGenerationDetailedResponseDTO();
			tokenDetails.setService(request.getService());
			tokenDetails.setConsumerName(request.getConsumer().getSystemName());
			tokenDetails.setConsumerAdress(request.getConsumer().getAddress());
			tokenDetails.setConsumerPort(request.getConsumer().getPort());
			tokenDetails.setTokenData(generateTokensResponse(request).getTokenData());
			data.add(tokenDetails);
		}
		
		return new TokenGenerationMultiServiceResponseDTO(data);
	}

	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private void checkTokenGenerationRequest(final TokenGenerationRequestDTO request) {
		logger.debug("checkTokenGenerationRequest started...");
		if (request.getConsumer() == null) {
			throw new InvalidParameterException("Consumer is null.");
		}
		
		checkSystemDTO(request.getConsumer());
		
		if (request.getConsumerCloud() != null && Utilities.isEmpty(request.getConsumerCloud().getOperator())) {
			throw new InvalidParameterException("Consumer cloud's operator is null or blank");
		}
		
		if (request.getConsumerCloud() != null && Utilities.isEmpty(request.getConsumerCloud().getName())) {
			throw new InvalidParameterException("Consumer cloud's name is null or blank");
		}
		
		if (request.getProviders() == null || request.getProviders().isEmpty()) {
			throw new InvalidParameterException("Provider list is null or empty.");
		}
		
		for (final TokenGenerationProviderDTO provider : request.getProviders()) {
			checkTokenGeneratorionProviderDTO(provider);
		}
		
		if (Utilities.isEmpty(request.getService())) {
			throw new InvalidParameterException("Service is null or blank.");
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private void checkTokenGeneratorionProviderDTO(final TokenGenerationProviderDTO provider) {
		logger.debug("checkTokenGenerationProviderDTO started...");
		
		if (provider.getProvider() == null) {
			throw new InvalidParameterException("Provider is null");
		}
		
		checkSystemDTO(provider.getProvider());
		
		if (provider.getServiceInterfaces() == null || provider.getServiceInterfaces().isEmpty()) {
			throw new InvalidParameterException("Service interface list is null or empty.");
		}
		
		for (final String intf : provider.getServiceInterfaces()) {
			if (!interfaceNameVerifier.isValid(intf)) {
				throw new InvalidParameterException("Specified interface name is not valid: " + intf + DOT);
			}
		}
	}

	//-------------------------------------------------------------------------------------------------
	private void checkSystemDTO(final SystemRequestDTO dto) {
		logger.debug("checkSystemDTO started...");
		
		if (Utilities.isEmpty(dto.getSystemName())) {
			throw new InvalidParameterException("System name is null or blank.");
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private String generateConsumerInfo(final SystemRequestDTO consumer, final CloudRequestDTO consumerCloud) {
		logger.debug("generateConsumerInfo started...");
		
		final StringBuilder sb = new StringBuilder(consumer.getSystemName());
		if (consumerCloud != null) {
			sb.append(DOT).append(consumerCloud.getName().trim()).append(DOT).append(consumerCloud.getOperator().trim());
		} else {
			// need to use own cloud information
			if (Utilities.isEmpty(ownCloudName)) {
				initOwnCloudInfo();
			}
			
			sb.append(DOT).append(ownCloudName).append(DOT).append(ownCloudOperator);
		}
		
		return sb.toString().toLowerCase();
	}
	
	//-------------------------------------------------------------------------------------------------
	private void initOwnCloudInfo() {
		logger.debug("initOwnCloudInfo started...");
		
		try {
			final Cloud ownCloud = commonDBService.getOwnCloud(sslEnabled);
			ownCloudName = ownCloud.getName();
			ownCloudOperator = ownCloud.getOperator();
		} catch (final DataNotFoundException ex) {
			logger.warn(ex.getMessage());
			
			if (!arrowheadContext.containsKey(CommonConstants.SERVER_COMMON_NAME)) {
				throw new ArrowheadException("Server's certificate not found.");
			}
			
			final String serverCN = (String) arrowheadContext.get(CommonConstants.SERVER_COMMON_NAME);
			final String[] serverFields = serverCN.split("\\.");
			// when the Service Registry initializes it will write own cloud in the database using its own common name to identify the cloud's name and operator
			// the corresponding part of the CN is the same in the Authorization's CN too, so we can use that 
			ownCloudName = serverFields[1].toLowerCase().trim(); 
			ownCloudOperator = serverFields[2].toLowerCase().trim();
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private Map<SystemRequestDTO,PublicKey> getProviderPublicKeys(final List<TokenGenerationProviderDTO> providers) {
		logger.debug("getProviderPublicKeys started...");
		
		final Map<SystemRequestDTO,PublicKey> result = new HashMap<>();
		
		for (final TokenGenerationProviderDTO provider : providers) {
			try {
				final String authInfo = provider.getProvider().getAuthenticationInfo();
				final PublicKey publicKey = Utilities.getPublicKeyFromBase64EncodedString(authInfo);
				result.put(provider.getProvider(), publicKey);
			} catch (final IllegalArgumentException | AuthException ex) {
				logger.error("The stored auth info for the system (" + provider.getProvider().getSystemName() + ") is not a proper RSA public key spec, or it is incorrectly encoded, or missing. " +
							 "The public key can not be decoded from it.");
			}
		}
		
		if (result.isEmpty())  { // means no provider contains valid key
			throw new InvalidParameterException("Token generation failed for all the provider systems.");
		}
		
		return result;
	}
	
	//-------------------------------------------------------------------------------------------------
	private String generateSignedJWT(final String consumerInfo, final String service, final String intf, final Integer duration) throws JoseException {
		logger.debug("generateSignedJWT started...");
		
		final JwtClaims claims = generateTokenPayload(consumerInfo, service, intf, duration);
		final JsonWebSignature jws = new JsonWebSignature();
		jws.setPayload(claims.toJson());
		
		if (!arrowheadContext.containsKey(CommonConstants.SERVER_PRIVATE_KEY)) {
			throw new ArrowheadException("Server's private key is not found.");
		}
		
		final PrivateKey privateKey = (PrivateKey) arrowheadContext.get(CommonConstants.SERVER_PRIVATE_KEY);
		jws.setKey(privateKey);
		jws.setAlgorithmHeaderValue(CommonConstants.JWS_SIGN_ALG);
		
		return jws.getCompactSerialization();
	}
	
	//-------------------------------------------------------------------------------------------------
	private JwtClaims generateTokenPayload(final String consumerInfo, final String service, final String intf, final Integer duration) {
		logger.debug("generateTokenPayload started...");
		
		final JwtClaims claims = new JwtClaims();
		claims.setGeneratedJwtId();
		claims.setIssuer(CommonConstants.CORE_SYSTEM_AUTHORIZATION);
		claims.setIssuedAtToNow();
		claims.setNotBeforeMinutesInThePast(1);
		if (duration != null) {
			claims.setExpirationTimeMinutesInTheFuture(duration.floatValue() / CommonConstants.CONVERSION_SECOND_TO_MINUTE);
		}
		claims.setStringClaim(CommonConstants.JWT_CLAIM_CONSUMER_ID, consumerInfo);
		claims.setStringClaim(CommonConstants.JWT_CLAIM_SERVICE_ID, service.toLowerCase());
		claims.setStringClaim(CommonConstants.JWT_CLAIM_INTERFACE_ID, intf);

		return claims;
	}
	
	//-------------------------------------------------------------------------------------------------
	private String encryptSignedJWT(final String signedJWT, final PublicKey providerKey) throws JoseException {
		logger.debug("encryptSignedJWT started...");
		final JsonWebEncryption jwe = new JsonWebEncryption();
		jwe.setAlgorithmHeaderValue(CommonConstants.JWE_KEY_MANAGEMENT_ALG);
		jwe.setEncryptionMethodHeaderParameter(CommonConstants.JWE_ENCRYPTION_ALG);
		jwe.setKey(providerKey);
		jwe.setContentTypeHeaderValue(JWT_CONTENT_TYPE);
		jwe.setPayload(signedJWT);
		
		return jwe.getCompactSerialization();
	}
}