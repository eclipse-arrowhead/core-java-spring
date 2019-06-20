package eu.arrowhead.core.authorization.token;

import java.security.InvalidParameterException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import eu.arrowhead.common.dto.CloudRequestDTO;
import eu.arrowhead.common.dto.SystemRequestDTO;
import eu.arrowhead.common.dto.TokenGenerationRequestDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.AuthException;
import eu.arrowhead.common.exception.DataNotFoundException;

// works only in secure mode
@Service
public class TokenGenerationService {

	//=================================================================================================
	// members

	private final Logger logger = LogManager.getLogger(TokenGenerationService.class);
	
	private static final String JWT_CONTENT_TYPE = "JWT";
	
	@Value(CommonConstants.$SERVER_SSL_ENABLED_WD)
	private boolean sslEnabled;
	
	@Resource(name = CommonConstants.ARROWHEAD_CONTEXT)
	private Map<String,Object> arrowheadContext;
	
	@Autowired
	private CommonDBService commonDBService;
	
	private String ownCloudName;
	private String ownCloudOperator;

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public Map<SystemRequestDTO,String> generateTokens(final TokenGenerationRequestDTO request) {
		logger.debug("generateTokens started...");
		
		if (!sslEnabled) {
			throw new ArrowheadException("Token generation is not possible in insecure mode.");
		}
		
		Assert.notNull(request, "Request is null.");
		checkTokenGenerationRequest(request);
		
		final Map<SystemRequestDTO,PublicKey> publicKeys = getProviderPublicKeys(request.getProviders());
		final Map<SystemRequestDTO,String> result = new HashMap<>(publicKeys.size());
		final String consumerInfo = generateConsumerInfo(request.getConsumer(), request.getConsumerCloud());
		
		String signedJWT;
		try {
			signedJWT = generateSignedJWT(consumerInfo, request.getService(), request.getDuration());
		} catch (final JoseException ex) {
			logger.error("Problem occured when trying to sign JWT token", ex);
			throw new ArrowheadException("Token generation failed");
		}
		
		for (final Entry<SystemRequestDTO,PublicKey> providerEntry : publicKeys.entrySet()) {
			try {
				final String encryptedJWT = encryptSignedJWT(signedJWT, providerEntry.getValue());
				result.put(providerEntry.getKey(), encryptedJWT);
			} catch (final JoseException ex) {
				logger.error("Problem occured when trying to encrypt signed JWT token", ex);
			}
			
		}
		
		if (result.isEmpty()) {
			throw new ArrowheadException("Token generation failed for all the provider systems.");
		}
		
		return result;
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
		
		for (final SystemRequestDTO provider : request.getProviders()) {
			checkSystemDTO(provider);
		}
		
		if (Utilities.isEmpty(request.getService())) {
			throw new InvalidParameterException("Service is null or blank.");
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
			sb.append(".").append(consumerCloud.getName().trim()).append(".").append(consumerCloud.getOperator().trim());
		} else {
			// need to use own cloud information
			if (Utilities.isEmpty(ownCloudName)) {
				initOwnCloudInfo();
			}
			
			sb.append(".").append(ownCloudName).append(".").append(ownCloudOperator);
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
			// when the gatekeeper initializes it will write own cloud in the database using its own common name to identify the cloudname and operator
			// the corresponding part of the CN is the same in the Authorization's CN too, so we can use that 
			ownCloudName = serverFields[1]; 
			ownCloudOperator = serverFields[2];
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private Map<SystemRequestDTO,PublicKey> getProviderPublicKeys(final List<SystemRequestDTO> providers) {
		logger.debug("getProviderPublicKeys started...");
		final Map<SystemRequestDTO,PublicKey> result = new HashMap<>();
		
		for (final SystemRequestDTO provider : providers) {
			try {
				final PublicKey publicKey = Utilities.getPublicKeyFromBase64EncodedString(provider.getAuthenticationInfo());
				result.put(provider, publicKey);
			} catch (final IllegalArgumentException | AuthException ex) {
				logger.error("The stored auth info for the system (" + provider.getSystemName() + ") is not a proper RSA public key spec, or it is incorrectly encoded, or missing. " +
							 "The public key can not be decoded from it.");
			}
		}
		
		if (result.isEmpty())  { // means no provider contains valid key
			throw new ArrowheadException("Token generation failed for all the provider systems.");
		}
		
		return result;
	}
	
	//-------------------------------------------------------------------------------------------------
	private String generateSignedJWT(final String consumerInfo, final String service, final Integer duration) throws JoseException {
		final JwtClaims claims = generateTokenPayload(consumerInfo, service, duration);
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
	private JwtClaims generateTokenPayload(final String consumerInfo, final String service, final Integer duration) {
		final JwtClaims claims = new JwtClaims();
		claims.setGeneratedJwtId();
		claims.setIssuer(CommonConstants.CORE_SYSTEM_AUTHORIZATION);
		claims.setIssuedAtToNow();
		claims.setNotBeforeMinutesInThePast(1);
		if (duration != null) {
			claims.setExpirationTimeMinutesInTheFuture(duration.floatValue());
		}
		claims.setStringClaim(CommonConstants.JWT_CLAIM_CONSUMER_ID, consumerInfo);
		claims.setStringClaim(CommonConstants.JWT_CLAIM_SERVICE_ID, service);
		
		return claims;
	}
	
	//-------------------------------------------------------------------------------------------------
	private String encryptSignedJWT(final String signedJWT, final PublicKey providerKey) throws JoseException {
		final JsonWebEncryption jwe = new JsonWebEncryption();
		jwe.setAlgorithmHeaderValue(CommonConstants.JWE_KEY_MANAGEMENT_ALG);
		jwe.setEncryptionMethodHeaderParameter(CommonConstants.JWE_ENCRYPTION_ALG);
		jwe.setKey(providerKey);
		jwe.setContentTypeHeaderValue(JWT_CONTENT_TYPE);
		jwe.setPayload(signedJWT);
		
		return jwe.getCompactSerialization();
	}
}