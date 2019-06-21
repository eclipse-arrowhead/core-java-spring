package eu.arrowhead.core.authorization;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.Defaults;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.SystemRequestDTO;
import eu.arrowhead.common.dto.TokenGenerationRequestDTO;
import eu.arrowhead.common.dto.TokenGenerationResponseDTO;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.core.authorization.token.TokenGenerationService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@CrossOrigin(maxAge = Defaults.CORS_MAX_AGE, allowCredentials = Defaults.CORS_ALLOW_CREDENTIALS, 
			 allowedHeaders = { HttpHeaders.ORIGIN, HttpHeaders.CONTENT_TYPE, HttpHeaders.ACCEPT, HttpHeaders.AUTHORIZATION }
)
@RestController
@RequestMapping(CommonConstants.AUTHORIZATION_URI)
public class AuthorizationController {
	
	//=================================================================================================
	// members
	
	private static final String ECHO_URI = "/echo";
	
	private static final String TOKEN_URI = "/token";
	private static final String TOKEN_DESCRIPTION = "Generates tokens for a consumer which can be used to access the specified service of the specified providers";
	private static final String TOKEN_HTTP_200_MESSAGE = "Tokens returned";
	private static final String TOKEN_HTTP_400_MESSAGE = "Could not generate tokens";
	
	private final Logger logger = LogManager.getLogger(AuthorizationController.class);
	
	@Autowired
	private TokenGenerationService tokenGenerationService;
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return an echo message with the purpose of testing the core service availability", response = String.class)
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = CommonConstants.SWAGGER_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path = ECHO_URI)
	public String echoService() {
		return "Got it!";
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = TOKEN_DESCRIPTION, response = TokenGenerationResponseDTO.class)
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = TOKEN_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = TOKEN_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@PostMapping(path = TOKEN_URI)
	@ResponseBody public TokenGenerationResponseDTO generateTokens(@RequestBody final TokenGenerationRequestDTO request) {
		logger.debug("New token generation request received");
		checkTokenGenerationRequest(request);
		
		if (request.getDuration() != null && request.getDuration().intValue() <= 0) {
			request.setDuration(null);
		}

		final TokenGenerationResponseDTO response = tokenGenerationService.generateTokensResponse(request);
		logger.debug("{} token(s) are generated for {}", response.getTokenData().size(), request.getConsumer().getSystemName());
		
		return response;
	}

	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	private void checkTokenGenerationRequest(final TokenGenerationRequestDTO request) {
		logger.debug("checkTokenGenerationRequest started...");
		
		final String origin = CommonConstants.AUTHORIZATION_URI + TOKEN_URI;
		if (request.getConsumer() == null) {
			throw new BadPayloadException("Consumer system is null", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		checkSystemRequest(request.getConsumer(), origin, false);
		
		if (request.getConsumerCloud() != null && Utilities.isEmpty(request.getConsumerCloud().getOperator())) {
			throw new BadPayloadException("Consumer cloud's operator is null or blank", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		if (request.getConsumerCloud() != null && Utilities.isEmpty(request.getConsumerCloud().getName())) {
			throw new BadPayloadException("Consumer cloud's name is null or blank", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		if (request.getProviders() == null || request.getProviders().isEmpty()) {
			throw new BadPayloadException("Provider list is null or empty.", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		for (final SystemRequestDTO provider : request.getProviders()) {
			checkSystemRequest(provider, origin, true);
		}
		
		if (Utilities.isEmpty(request.getService())) {
			throw new BadPayloadException("Service is null or blank.", HttpStatus.SC_BAD_REQUEST, origin);
		}
	}

	//-------------------------------------------------------------------------------------------------
	private void checkSystemRequest(final SystemRequestDTO system, final String origin, final boolean mandatoryAuthInfo) {
		logger.debug("checkSystemRequest started...");
		
		if (Utilities.isEmpty(system.getSystemName())) {
			throw new BadPayloadException("System name is null or blank", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		if (Utilities.isEmpty(system.getAddress())) {
			throw new BadPayloadException("System address is null or blank", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		if (system.getPort() == null) {
			throw new BadPayloadException("System port is null", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		final int validatedPort = system.getPort().intValue();
		if (validatedPort < CommonConstants.SYSTEM_PORT_RANGE_MIN || validatedPort > CommonConstants.SYSTEM_PORT_RANGE_MAX) {
			throw new BadPayloadException("System port must be between " + CommonConstants.SYSTEM_PORT_RANGE_MIN + " and " + CommonConstants.SYSTEM_PORT_RANGE_MAX + ".",
										  HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		if (mandatoryAuthInfo && Utilities.isEmpty(system.getAuthenticationInfo())) {
			throw new BadPayloadException("System authentication info is null or blank", HttpStatus.SC_BAD_REQUEST, origin);
		}
	}
}