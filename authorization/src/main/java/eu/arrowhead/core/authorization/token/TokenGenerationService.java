package eu.arrowhead.core.authorization.token;

import java.security.InvalidParameterException;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jose4j.jwt.JwtClaims;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.CloudRequestDTO;
import eu.arrowhead.common.dto.SystemRequestDTO;
import eu.arrowhead.common.dto.TokenGenerationRequestDTO;

@Component
public class TokenGenerationService {

	//=================================================================================================
	// members

	private final Logger logger = LogManager.getLogger(TokenGenerationService.class);

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public Map<SystemRequestDTO,String> generateTokens(final TokenGenerationRequestDTO request) {
		logger.debug("generateTokens started...");
		Assert.notNull(request, "Request is null.");
		checkTokenGenerationRequest(request);
		
		final String consumerInfo = generateConsumerInfo(request.getConsumer(), request.getConsumerCloud());
		for (final SystemRequestDTO provider : request.getProviders()) {
			final JwtClaims claims = new JwtClaims();
			claims.setGeneratedJwtId();
			claims.setIssuer(CommonConstants.CORE_SYSTEM_AUTHORIZATION);
			claims.setIssuedAtToNow();
			claims.setNotBeforeMinutesInThePast(1);
			if (request.getDuration() != null) {
				claims.setExpirationTimeMinutesInTheFuture(request.getDuration().floatValue());
			}
			
		}
		
		return null;
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
		final StringBuilder sb = new StringBuilder(consumer.getSystemName());
		
		//TODO: need own cloud information => which is a problem
		return null;
	}
}