package eu.arrowhead.core.authorization.token;

import java.security.InvalidParameterException;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jose4j.jwt.JwtClaims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.entity.Cloud;
import eu.arrowhead.common.database.service.CommonDBService;
import eu.arrowhead.common.dto.CloudRequestDTO;
import eu.arrowhead.common.dto.SystemRequestDTO;
import eu.arrowhead.common.dto.TokenGenerationRequestDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.DataNotFoundException;

// works only in secure mode
@Component
public class TokenGenerationService {

	//=================================================================================================
	// members

	private final Logger logger = LogManager.getLogger(TokenGenerationService.class);
	
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
		
		//TODO: continue
		
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
		if (consumerCloud != null) {
			sb.append(".").append(consumerCloud.getName().trim()).append(".").append(consumerCloud.getOperator().trim());
		} else {
			// need to use own cloud information
			if (Utilities.isEmpty(ownCloudName)) {
				initOwnCloud();
			}
			
			sb.append(".").append(ownCloudName).append(".").append(ownCloudOperator);
		}
		
		return sb.toString().toLowerCase();
	}
	
	//-------------------------------------------------------------------------------------------------
	private void initOwnCloud() {
		try {
			final Cloud ownCloud = commonDBService.getOwnCloud(sslEnabled);
			ownCloudName = ownCloud.getName();
			ownCloudOperator = ownCloud.getOperator();
		} catch (final DataNotFoundException ex) {
			logger.warn(ex.getMessage());
			final String serverCN = (String) arrowheadContext.get(CommonConstants.SERVER_COMMON_NAME);
			final String[] serverFields = serverCN.split("\\.");
			// when the gatekeeper initializes it will write own cloud in the database using its own common name to identify the cloudname and operator
			// the corresponding part of the CN is the same in the Authorization's CN too, so we can use that 
			ownCloudName = serverFields[1]; 
			ownCloudOperator = serverFields[2];
		}
	}
}