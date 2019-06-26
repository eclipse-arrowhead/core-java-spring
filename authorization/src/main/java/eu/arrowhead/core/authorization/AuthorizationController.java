package eu.arrowhead.core.authorization;

import java.security.PublicKey;
import java.util.Base64;
import java.util.Map;

import javax.annotation.Resource;

import java.util.HashSet;
import java.util.Set;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
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
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.core.authorization.token.TokenGenerationService;
import eu.arrowhead.common.dto.IntraCloudAuthorizationCheckRequestDTO;
import eu.arrowhead.common.dto.IntraCloudAuthorizationCheckResponseDTO;
import eu.arrowhead.common.dto.IntraCloudAuthorizationListResponseDTO;
import eu.arrowhead.common.dto.IntraCloudAuthorizationRequestDTO;
import eu.arrowhead.common.dto.IntraCloudAuthorizationResponseDTO;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.core.authorization.database.service.AuthorizationDBService;
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
	
	private static final String PATH_VARIABLE_ID = "id";
	private static final String ID_NOT_VALID_ERROR_MESSAGE = "Id must be greater than 0.";
	
	private static final String ECHO_URI = "/echo";
	
	private static final String TOKEN_URI = "/token";
	private static final String TOKEN_DESCRIPTION = "Generates tokens for a consumer which can be used to access the specified service of the specified providers";
	private static final String TOKEN_HTTP_200_MESSAGE = "Tokens returned";
	private static final String TOKEN_HTTP_400_MESSAGE = "Could not generate tokens";
	
	private static final String PUBLIC_KEY_URI = "/publickey";
	private static final String PUBLIC_KEY_DESCRIPTION = "Returns the public key of the Authorization core service as a Base64 encoded text";
	private static final String PUBLIC_KEY_200_MESSAGE = "Public key returned";
	
	private static final String INTRA_CLOUD_AUTHORIZATION_MGMT_URI = CommonConstants.MGMT_URI + "/intracloud";
	private static final String INTRA_CLOUD_AUTHORIZATION_MGMT_BY_ID_URI = INTRA_CLOUD_AUTHORIZATION_MGMT_URI + "/{" + PATH_VARIABLE_ID + "}";
	private static final String GET_INTRA_CLOUD_AUTHORIZATION_MGMT_HTTP_200_MESSAGE = "IntraCloudAuthorization returned";
	private static final String GET_INTRA_CLOUD_AUTHORIZATION_MGMT_HTTP_400_MESSAGE = "Could not retrieve IntraCloudAuthorization";
	private static final String DELETE_INTRA_CLOUD_AUTHORIZATION_MGMT_HTTP_200_MESSAGE = "IntraCloudAuthorization removed";
	private static final String DELETE_INTRA_CLOUD_AUTHORIZATION_MGMT_HTTP_400_MESSAGE = "Could not remove IntraCloudAuthorization";
	private static final String POST_INTRA_CLOUD_AUTHORIZATION_MGMT_HTTP_201_MESSAGE = "IntraCloudAuthorizations created";
	private static final String POST_INTRA_CLOUD_AUTHORIZATION_MGMT_HTTP_400_MESSAGE = "Could not create IntraCloudAuthorization";
	
	private static final String INTRA_CLOUD_AUTHORIZATION_CHECK_URI = "/intracloud/check";
	private static final String POST_INTRA_CLOUD_AUTHORIZATION_HTTP_200_MESSAGE = "IntraCloudAuthorization result returned";
	private static final String POST_INTRA_CLOUD_AUTHORIZATION_HTTP_400_MESSAGE = "Could not check IntraCloudAuthorization";
	
	private final Logger logger = LogManager.getLogger(AuthorizationController.class);
	
	@Autowired
	private AuthorizationDBService authorizationDBService;
	

	@Autowired
	private TokenGenerationService tokenGenerationService;
	
	@Resource(name = CommonConstants.ARROWHEAD_CONTEXT)
	private Map<String,Object> arrowheadContext;
	
	@Value(CommonConstants.$SERVER_SSL_ENABLED_WD)
	private boolean secure;
	
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
	@ApiOperation(value = "Return requested IntraCloudAuthorization entries by the given parameters", response = IntraCloudAuthorizationListResponseDTO.class)
	@ApiResponses (value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = GET_INTRA_CLOUD_AUTHORIZATION_MGMT_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = GET_INTRA_CLOUD_AUTHORIZATION_MGMT_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path = INTRA_CLOUD_AUTHORIZATION_MGMT_URI, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public IntraCloudAuthorizationListResponseDTO getIntraCloudAuthorizations(
			@RequestParam(name = CommonConstants.REQUEST_PARAM_PAGE, required = false) final Integer page,
			@RequestParam(name = CommonConstants.REQUEST_PARAM_ITEM_PER_PAGE, required = false) final Integer size,
			@RequestParam(name = CommonConstants.REQUEST_PARAM_DIRECTION, defaultValue = Defaults.DEFAULT_REQUEST_PARAM_DIRECTION_VALUE) final String direction,
			@RequestParam(name = CommonConstants.REQUEST_PARAM_SORT_FIELD, defaultValue = CommonConstants.COMMON_FIELD_NAME_ID) final String sortField) {
		logger.debug("New IntraCloudAuthorization get request recieved with page: {} and item_per page: {}", page, size);
		
		int validatedPage;
		int validatedSize;
		if (page == null && size == null) {
			validatedPage = -1;
			validatedSize = -1;
		} else {
			if (page == null || size == null) {
				throw new BadPayloadException("Defined page or size could not be with undefined size or page.", HttpStatus.SC_BAD_REQUEST, CommonConstants.AUTHORIZATIOIN_URI + INTRA_CLOUD_AUTHORIZATION_MGMT_URI);
			} else {
				validatedPage = page;
				validatedSize = size;
			}
		}
		final Direction validatedDirection = calculateDirection(direction, CommonConstants.AUTHORIZATIOIN_URI + INTRA_CLOUD_AUTHORIZATION_MGMT_URI);
		
		final IntraCloudAuthorizationListResponseDTO intraCloudAuthorizationEntriesResponse = authorizationDBService.getIntraCloudAuthorizationEntriesResponse(validatedPage, validatedSize, validatedDirection, sortField);
		logger.debug("IntraCloudAuthorizations  with page: {} and item_per page: {} retrieved successfully", page, size);
		return intraCloudAuthorizationEntriesResponse;
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return requested IntraCloudAuthorization entry", response = IntraCloudAuthorizationResponseDTO.class)
	@ApiResponses (value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = GET_INTRA_CLOUD_AUTHORIZATION_MGMT_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = GET_INTRA_CLOUD_AUTHORIZATION_MGMT_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path = INTRA_CLOUD_AUTHORIZATION_MGMT_BY_ID_URI, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public IntraCloudAuthorizationResponseDTO getIntraCloudAuthorizationById(@PathVariable(value = PATH_VARIABLE_ID) final long id) {
		logger.debug("New IntraCloudAuthorization get request recieved with id: {}", id);
		
		if (id < 1) {
			throw new BadPayloadException(ID_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, CommonConstants.AUTHORIZATIOIN_URI + INTRA_CLOUD_AUTHORIZATION_MGMT_BY_ID_URI);
		}
		
		final IntraCloudAuthorizationResponseDTO intraCloudAuthorizationEntryByIdResponse = authorizationDBService.getIntraCloudAuthorizationEntryByIdResponse(id);
		logger.debug("IntraCloudAuthorization entry with id: {} successfully retrieved", id);
		return intraCloudAuthorizationEntryByIdResponse;
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Remove the requested IntraCloudAuthorization entry")
	@ApiResponses (value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = DELETE_INTRA_CLOUD_AUTHORIZATION_MGMT_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = DELETE_INTRA_CLOUD_AUTHORIZATION_MGMT_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@DeleteMapping(path = INTRA_CLOUD_AUTHORIZATION_MGMT_BY_ID_URI)
	public void removeIntraCloudAuthorizationById(@PathVariable(value = PATH_VARIABLE_ID) final long id) {
		logger.debug("New IntraCloudAuthorization delete request recieved with id: {}", id);
		
		if (id < 1) {
			throw new BadPayloadException(ID_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, CommonConstants.AUTHORIZATIOIN_URI + INTRA_CLOUD_AUTHORIZATION_MGMT_BY_ID_URI);
		}
		
		authorizationDBService.removeIntraCloudAuthorizationEntryById(id);
		logger.debug("IntraCloudAuthorization with id: '{}' successfully deleted", id);
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Create the requested IntraCloudAuthorization entries", response = IntraCloudAuthorizationListResponseDTO.class)
	@ApiResponses (value = {
			@ApiResponse(code = HttpStatus.SC_CREATED, message = POST_INTRA_CLOUD_AUTHORIZATION_MGMT_HTTP_201_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = POST_INTRA_CLOUD_AUTHORIZATION_MGMT_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@PostMapping(path = INTRA_CLOUD_AUTHORIZATION_MGMT_URI, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(value = org.springframework.http.HttpStatus.CREATED)
	@ResponseBody public IntraCloudAuthorizationListResponseDTO registerIntraCloudAuthorization(@RequestBody final IntraCloudAuthorizationRequestDTO request) {
		logger.debug("New IntraCloudAuthorization registration request recieved");
		
		final boolean isConsumerIdInvalid = request.getConsumerId() == null || request.getConsumerId() < 1;
		final boolean isProviderListEmpty = request.getProviderIds() == null || request.getProviderIds().isEmpty();
		final boolean isServiceDefinitionListEmpty = request.getServiceDefinitionIds() == null || request.getServiceDefinitionIds().isEmpty();
		if (isConsumerIdInvalid || isProviderListEmpty || isServiceDefinitionListEmpty) {
			String exceptionMsg = "Payload is invalid due to the following reasons:";
			exceptionMsg = isConsumerIdInvalid ? exceptionMsg + " invalid consumer id," : exceptionMsg;
			exceptionMsg = isProviderListEmpty ? exceptionMsg + " providerId list is empty," : exceptionMsg;
			exceptionMsg = isServiceDefinitionListEmpty ? exceptionMsg + " serviceDefinitionList is empty," : exceptionMsg;
			exceptionMsg = exceptionMsg.substring(0, exceptionMsg.length()-1);
			throw new BadPayloadException(exceptionMsg, HttpStatus.SC_BAD_REQUEST, CommonConstants.AUTHORIZATIOIN_URI + INTRA_CLOUD_AUTHORIZATION_MGMT_URI);
		}
		if (request.getProviderIds().size() > 1 && request.getServiceDefinitionIds().size() > 1) {
			throw new BadPayloadException("providerIds list or serviceDefinitionIds list should contain only one element, but both contain more",
					HttpStatus.SC_BAD_REQUEST, CommonConstants.AUTHORIZATIOIN_URI + INTRA_CLOUD_AUTHORIZATION_MGMT_URI);
		}
		
		final Set<Long> providerIdSet = new HashSet<>();
		for (final Long id : request.getProviderIds()) {
			if (id != null && id > 0) {
				providerIdSet.add(id);
			} else {
				logger.debug("Invalid provider system id: {}", id);
			}
		}
		final Set<Long> serviceIdSet = new HashSet<>();
		for (final Long id : request.getServiceDefinitionIds()) {
			if (id != null && id > 0) {
				serviceIdSet.add(id);
			} else {
				logger.debug("Invalid ServiceDefinition id: {}", id);
			}
		}
		
		final IntraCloudAuthorizationListResponseDTO response = authorizationDBService.createBulkIntraCloudAuthorizationResponse(request.getConsumerId(), providerIdSet, serviceIdSet);
		logger.debug("registerIntraCloudAuthorization has been finished");
		return response;
		
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Checks whether the consumer System can use a Service from a list of provider Systems", response = IntraCloudAuthorizationCheckResponseDTO.class)
	@ApiResponses (value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = POST_INTRA_CLOUD_AUTHORIZATION_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = POST_INTRA_CLOUD_AUTHORIZATION_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@PostMapping(path = INTRA_CLOUD_AUTHORIZATION_CHECK_URI)
	@ResponseBody public IntraCloudAuthorizationCheckResponseDTO checkIntraCloudAuthorizationRequest(@RequestBody final IntraCloudAuthorizationCheckRequestDTO request) {
		logger.debug("New IntraCloudAuthorization check request recieved");
		
		final boolean isConsumerIdInvalid = request.getConsumerId() == null || request.getConsumerId() < 1;
		final boolean isServiceDefinitionIdInvalid = request.getServiceDefinitionId() == null || request.getServiceDefinitionId() < 1;
		final boolean isProviderListEmpty = request.getProviderIds() == null || request.getProviderIds().isEmpty();
		if (isConsumerIdInvalid || isServiceDefinitionIdInvalid || isProviderListEmpty) {
			String exceptionMsg = "Payload is invalid due to the following reasons:";
			exceptionMsg = isConsumerIdInvalid ? exceptionMsg + " invalid consumer id," : exceptionMsg;
			exceptionMsg = isServiceDefinitionIdInvalid ? exceptionMsg + " invalid serviceDefinition id," : exceptionMsg;
			exceptionMsg = isProviderListEmpty ? exceptionMsg + " providerId list is empty," : exceptionMsg;
			exceptionMsg = exceptionMsg.substring(0, exceptionMsg.length()-1);
			throw new BadPayloadException(exceptionMsg, HttpStatus.SC_BAD_REQUEST, CommonConstants.AUTHORIZATIOIN_URI + INTRA_CLOUD_AUTHORIZATION_CHECK_URI);
		}
		
		final IntraCloudAuthorizationCheckResponseDTO response = authorizationDBService.checkIntraCloudAuthorizationRequest(request.getConsumerId(), request.getServiceDefinitionId(), request.getProviderIds());
		logger.debug("checkIntraCloudAuthorizationRequest has been finished");
		return response;
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private Direction calculateDirection(final String direction, final String origin) {
		logger.debug("calculateDirection started ...");
		final String directionStr = direction != null ? direction.toUpperCase() : "";
		Direction validatedDirection;
		switch (directionStr) {
			case CommonConstants.SORT_ORDER_ASCENDING:
				validatedDirection = Direction.ASC;
				break;
			case CommonConstants.SORT_ORDER_DESCENDING:
				validatedDirection = Direction.DESC;
				break;
			default:
				throw new BadPayloadException("Invalid sort direction flag", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		return validatedDirection;
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
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = PUBLIC_KEY_DESCRIPTION, response = String.class)
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = PUBLIC_KEY_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path = PUBLIC_KEY_URI)
	public String getPublicKey() {
		return acquireAndConvertPublicKey();
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
			throw new BadPayloadException("Provider list is null or empty", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		for (final SystemRequestDTO provider : request.getProviders()) {
			checkSystemRequest(provider, origin, true);
		}
		
		if (Utilities.isEmpty(request.getService())) {
			throw new BadPayloadException("Service is null or blank", HttpStatus.SC_BAD_REQUEST, origin);
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
	
	//-------------------------------------------------------------------------------------------------
	private String acquireAndConvertPublicKey() {
		final String origin = CommonConstants.AUTHORIZATION_URI + PUBLIC_KEY_URI;
		
		if (!secure) {
			throw new ArrowheadException("Authorization core service runs in insecure mode.", HttpStatus.SC_INTERNAL_SERVER_ERROR, origin);
		}
		
		if (!arrowheadContext.containsKey(CommonConstants.SERVER_PUBLIC_KEY)) {
			throw new ArrowheadException("Public key is not available.", HttpStatus.SC_INTERNAL_SERVER_ERROR, origin);
		}
		
		final PublicKey publicKey = (PublicKey) arrowheadContext.get(CommonConstants.SERVER_PUBLIC_KEY);
		
		return Base64.getEncoder().encodeToString(publicKey.getEncoded());
	}
}