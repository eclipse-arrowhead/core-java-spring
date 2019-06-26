package eu.arrowhead.core.authorization;

import java.util.List;
import java.util.Set;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort.Direction;
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
import org.springframework.web.bind.annotation.RestController;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.Defaults;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.InterCloudAuthorizationCheckRequestDTO;
import eu.arrowhead.common.dto.InterCloudAuthorizationCheckResponseDTO;
import eu.arrowhead.common.dto.InterCloudAuthorizationListResponseDTO;
import eu.arrowhead.common.dto.InterCloudAuthorizationRequestDTO;
import eu.arrowhead.common.dto.InterCloudAuthorizationResponseDTO;
import eu.arrowhead.common.dto.IntraCloudAuthorizationCheckRequestDTO;
import eu.arrowhead.common.dto.IntraCloudAuthorizationCheckResponseDTO;
import eu.arrowhead.common.dto.IntraCloudAuthorizationListResponseDTO;
import eu.arrowhead.common.dto.IntraCloudAuthorizationRequestDTO;
import eu.arrowhead.common.dto.IntraCloudAuthorizationResponseDTO;
import eu.arrowhead.common.dto.SystemRequestDTO;
import eu.arrowhead.common.dto.TokenGenerationRequestDTO;
import eu.arrowhead.common.dto.TokenGenerationResponseDTO;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.core.authorization.database.service.AuthorizationDBService;
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
	
	private static final String PATH_VARIABLE_ID = "id";
	private static final String ID_NOT_VALID_ERROR_MESSAGE = "Id must be greater then 0.";
	
	private static final String ECHO_URI = "/echo";
	
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
	
	private static final String INTER_CLOUD_AUTHORIZATION_MGMT_URI = CommonConstants.MGMT_URI + "/intercloud";
	private static final String INTER_CLOUD_AUTHORIZATION_MGMT_BY_ID_URI = INTER_CLOUD_AUTHORIZATION_MGMT_URI + "/{" + PATH_VARIABLE_ID + "}";
	private static final String GET_INTER_CLOUD_AUTHORIZATION_HTTP_200_MESSAGE = "InterCloudAuthorization returned";
	private static final String GET_INTER_CLOUD_AUTHORIZATION_HTTP_400_MESSAGE = "Could not retrieve InterCloudAuthorization";
	private static final String DELETE_INTER_CLOUD_AUTHORIZATION_HTTP_200_MESSAGE = "InterCloudAuthorization removed";
	private static final String DELETE_INTER_CLOUD_AUTHORIZATION_HTTP_400_MESSAGE = "Could not remove InterCloudAuthorization";
	private static final String POST_INTER_CLOUD_AUTHORIZATION_HTTP_201_MESSAGE = "InterCloudAuthorizations created";
	private static final String POST_INTER_CLOUD_AUTHORIZATION_MGMT_HTTP_400_MESSAGE = "Could not create InterCloudAuthorization";
	
	private static final String INTER_CLOUD_AUTHORIZATION_CHECK_URI = "/intercloud/check";
	private static final String POST_INTER_CLOUD_AUTHORIZATION_HTTP_200_MESSAGE = "InterCloudAuthorization result returned";
	private static final String POST_INTER_CLOUD_AUTHORIZATION_HTTP_400_MESSAGE = "Could not check InterCloudAuthorization";
	
	private static final String TOKEN_URI = "/token";
	private static final String TOKEN_DESCRIPTION = "Generates tokens for a consumer which can be used to access the specified service of the specified providers";
	private static final String TOKEN_HTTP_200_MESSAGE = "Tokens returned";
	private static final String TOKEN_HTTP_400_MESSAGE = "Could not generate tokens";
	
	private final Logger logger = LogManager.getLogger(AuthorizationController.class);
	
	@Autowired
	private AuthorizationDBService authorizationDBService;
		
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
				throw new BadPayloadException("Defined page or size could not be with undefined size or page.", HttpStatus.SC_BAD_REQUEST, CommonConstants.AUTHORIZATION_URI + INTRA_CLOUD_AUTHORIZATION_MGMT_URI);
			} else {
				validatedPage = page;
				validatedSize = size;
			}
		}
		final Direction validatedDirection = calculateDirection(direction, CommonConstants.AUTHORIZATION_URI + INTRA_CLOUD_AUTHORIZATION_MGMT_URI);
		
		final IntraCloudAuthorizationListResponseDTO intraCloudAuthorizationEntriesResponse = authorizationDBService.getIntraCloudAuthorizationEntriesResponse(validatedPage, validatedSize, validatedDirection, sortField);
		logger.debug("IntraCloudAuthorizations  with page: {} and item_per page: {} succesfully retrived", page, size);
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
			throw new BadPayloadException(ID_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, CommonConstants.AUTHORIZATION_URI + INTRA_CLOUD_AUTHORIZATION_MGMT_BY_ID_URI);
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
			throw new BadPayloadException(ID_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, CommonConstants.AUTHORIZATION_URI + INTRA_CLOUD_AUTHORIZATION_MGMT_BY_ID_URI);
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
			exceptionMsg = isConsumerIdInvalid ? exceptionMsg +" 'invalid consumer id'" : exceptionMsg;
			exceptionMsg = isProviderListEmpty ? exceptionMsg + " 'providerId list is empty'" : exceptionMsg;
			exceptionMsg = isServiceDefinitionListEmpty ? exceptionMsg + " 'serviceDefinitionList is empty'" : exceptionMsg;
			throw new BadPayloadException(exceptionMsg, HttpStatus.SC_BAD_REQUEST, CommonConstants.AUTHORIZATION_URI + INTRA_CLOUD_AUTHORIZATION_MGMT_URI);
		}
		
		final IntraCloudAuthorizationListResponseDTO response = authorizationDBService.createBulkIntraCloudAuthorizationResponse(request.getConsumerId(), request.getProviderIds(), request.getServiceDefinitionIds());
		logger.debug("registerIntraCloudAuthorization has been finished");
		return response;
		
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
	@ApiOperation(value = "Return requested InterCloudAuthorization entries by the given parameters", response = InterCloudAuthorizationListResponseDTO.class)
	@ApiResponses (value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = GET_INTER_CLOUD_AUTHORIZATION_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_OK, message = GET_INTER_CLOUD_AUTHORIZATION_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path = INTER_CLOUD_AUTHORIZATION_MGMT_URI, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public InterCloudAuthorizationListResponseDTO getInterCloudAuthorizations(
			@RequestParam(name = CommonConstants.REQUEST_PARAM_PAGE, required = false) final Integer page,
			@RequestParam(name = CommonConstants.REQUEST_PARAM_ITEM_PER_PAGE, required = false) final Integer size,
			@RequestParam(name = CommonConstants.REQUEST_PARAM_DIRECTION, defaultValue = Defaults.DEFAULT_REQUEST_PARAM_DIRECTION_VALUE) final String direction,
			@RequestParam(name = CommonConstants.REQUEST_PARAM_SORT_FIELD, defaultValue = CommonConstants.COMMON_FIELD_NAME_ID) final String sortField) {
		logger.debug("New InterCloudAuthorization get request recieved with page: {} and item_per page: {}", page, size);
		
		int validatedPage;
		int validatedSize;
		if (page == null && size == null) {
			validatedPage = -1;
			validatedSize = -1;
		} else {
			if (page == null || size == null) {
				throw new BadPayloadException("Defined page or size could not be with undefined size or page.", HttpStatus.SC_BAD_REQUEST, CommonConstants.AUTHORIZATION_URI + INTER_CLOUD_AUTHORIZATION_MGMT_URI);
			} else {
				validatedPage = page;
				validatedSize = size;
			}
		}
		final Direction validatedDirection = calculateDirection(direction, CommonConstants.AUTHORIZATION_URI + INTER_CLOUD_AUTHORIZATION_MGMT_URI);
		
		final InterCloudAuthorizationListResponseDTO interCloudAuthorizationEntriesResponse = authorizationDBService.getInterCloudAuthorizationEntriesResponse(validatedPage, validatedSize, validatedDirection, sortField);
		logger.debug("InterCloudAuthorizations  with page: {} and item_per page: {} succesfully retrived", page, size);
		return interCloudAuthorizationEntriesResponse;
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return requested InterCloudAuthorization entry", response = InterCloudAuthorizationResponseDTO.class)
	@ApiResponses (value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = GET_INTER_CLOUD_AUTHORIZATION_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_OK, message = GET_INTER_CLOUD_AUTHORIZATION_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path = INTER_CLOUD_AUTHORIZATION_MGMT_BY_ID_URI, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public InterCloudAuthorizationResponseDTO getInterCloudAuthorizationById(@PathVariable(value = PATH_VARIABLE_ID) final long id) {
		logger.debug("New InterCloudAuthorization get request recieved with id: {}", id);
		
		if (id < 1) {
			throw new BadPayloadException(ID_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, CommonConstants.AUTHORIZATION_URI + INTER_CLOUD_AUTHORIZATION_MGMT_BY_ID_URI);
		}
		
		final InterCloudAuthorizationResponseDTO interCloudAuthorizationEntryByIdResponse = authorizationDBService.getInterCloudAuthorizationEntryByIdResponse(id);
		logger.debug("InterCloudAuthorization entry with id: {} successfully retrieved", id);
		return interCloudAuthorizationEntryByIdResponse;
	}

	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Create the requested InterCloudAuthorization entries")
	@ApiResponses (value = {
			@ApiResponse(code = HttpStatus.SC_CREATED, message = POST_INTER_CLOUD_AUTHORIZATION_HTTP_201_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = POST_INTER_CLOUD_AUTHORIZATION_MGMT_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@PostMapping(path = INTER_CLOUD_AUTHORIZATION_MGMT_URI, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(value = org.springframework.http.HttpStatus.CREATED)
	@ResponseBody public InterCloudAuthorizationListResponseDTO addInterCloudAuthorization(@RequestBody final InterCloudAuthorizationRequestDTO request) {
		logger.debug("New InterCloudAuthorization registration request recieved");
		
		final boolean isCloudIdNotValid = request.getCloudId() == null || request.getCloudId() < 1  ;
		final boolean isServiceDefinitionNotValid = request.getServiceDefinitionIdList() == null || request.getServiceDefinitionIdList().isEmpty() ;
		if (isCloudIdNotValid || isServiceDefinitionNotValid) {
			String exceptionMsg = isCloudIdNotValid ? "Cloud Id is not valid," : "";
			exceptionMsg = isServiceDefinitionNotValid ? exceptionMsg + " ServiceDefinition is null or blank," :  exceptionMsg ;
			
			exceptionMsg = exceptionMsg.substring(0, exceptionMsg.length() -1);
			throw new BadPayloadException(exceptionMsg, HttpStatus.SC_BAD_REQUEST, CommonConstants.AUTHORIZATION_URI + INTER_CLOUD_AUTHORIZATION_MGMT_URI);
		}
		
		final long validatedCloudId = request.getCloudId();
		final Set<Long> serviceDefinitionIdSet = convertServiceDefinitionIdListToSet(request.getServiceDefinitionIdList(), INTER_CLOUD_AUTHORIZATION_MGMT_URI);
		
		final InterCloudAuthorizationListResponseDTO response = authorizationDBService.createInterCloudAuthorizationResponse(
				validatedCloudId,
				serviceDefinitionIdSet);
		logger.debug("registerInterCloudAuthorization has been finished");
		return response;
		
	}

	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Remove the requested InterCloudAuthorization entry")
	@ApiResponses (value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = DELETE_INTER_CLOUD_AUTHORIZATION_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_OK, message = DELETE_INTER_CLOUD_AUTHORIZATION_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@DeleteMapping(path = INTER_CLOUD_AUTHORIZATION_MGMT_BY_ID_URI)
	public void removeInterCloudAuthorizationById(@PathVariable(value = PATH_VARIABLE_ID) final long id) {
		logger.debug("New InterCloudAuthorization delete request recieved with id: {}", id);
		
		if (id < 1) {
			throw new BadPayloadException(ID_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, CommonConstants.AUTHORIZATION_URI + INTER_CLOUD_AUTHORIZATION_MGMT_BY_ID_URI);
		}
		
		authorizationDBService.removeInterCloudAuthorizationEntryById(id);
		logger.debug("InterCloudAuthorization with id: '{}' successfully deleted", id);

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
			exceptionMsg = isConsumerIdInvalid ? exceptionMsg + " 'invalid consumer id'" : exceptionMsg;
			exceptionMsg = isServiceDefinitionIdInvalid ? exceptionMsg + " 'invalid serviceDefinition id'" : exceptionMsg;
			exceptionMsg = isProviderListEmpty ? exceptionMsg + " 'providerId list is empty'" : exceptionMsg;
			throw new BadPayloadException(exceptionMsg, HttpStatus.SC_BAD_REQUEST, CommonConstants.AUTHORIZATION_URI + INTRA_CLOUD_AUTHORIZATION_CHECK_URI);
		}
		
		final IntraCloudAuthorizationCheckResponseDTO response = authorizationDBService.checkIntraCloudAuthorizationRequestResponse(request.getConsumerId(), request.getServiceDefinitionId(), request.getProviderIds());
		logger.debug("checkIntraCloudAuthorizationRequest has been finished");
		return response;
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Checks whether the consumer System can use a Service from a list of provider Systems", response = InterCloudAuthorizationCheckResponseDTO.class)
	@ApiResponses (value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = POST_INTER_CLOUD_AUTHORIZATION_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = POST_INTER_CLOUD_AUTHORIZATION_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@PostMapping(path = INTER_CLOUD_AUTHORIZATION_CHECK_URI)
	@ResponseBody public InterCloudAuthorizationCheckResponseDTO checkInterCloudAuthorizationRequest(@RequestBody final InterCloudAuthorizationCheckRequestDTO request) {
		logger.debug("New InterCloudAuthorization check request recieved");
		
		final boolean isCloudIdInvalid = request.getCloudId() == null || request.getCloudId() < 1;
		final boolean isServiceDefinitionIdInvalid = request.getServiceDefinitionId() == null || request.getServiceDefinitionId() < 1;
		if (isCloudIdInvalid || isServiceDefinitionIdInvalid ) {
			String exceptionMsg = "Payload is invalid due to the following reasons:";
			exceptionMsg = isCloudIdInvalid ? exceptionMsg + " 'invalid consumer id' ," : exceptionMsg;
			exceptionMsg = isServiceDefinitionIdInvalid ? exceptionMsg + " 'invalid serviceDefinition id' ," : exceptionMsg;
			exceptionMsg = exceptionMsg.substring(0, exceptionMsg.length() -1);
			
			throw new BadPayloadException(exceptionMsg, HttpStatus.SC_BAD_REQUEST, CommonConstants.AUTHORIZATION_URI + INTER_CLOUD_AUTHORIZATION_CHECK_URI);
		}
		
		final long validCloudId = request.getCloudId();
		final long validServiceDefinitionId = request.getServiceDefinitionId(); 
		
		final InterCloudAuthorizationCheckResponseDTO response = authorizationDBService.createInterCloudAuthorizationResponse(validCloudId, validServiceDefinitionId);
		logger.debug("checkInterCloudAuthorizationRequest has been finished");
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
	private Set<Long> convertServiceDefinitionIdListToSet(final List<Long> serviceDefinitionIdList, final String origin) {
		try {
			return Set.copyOf(serviceDefinitionIdList);
		}catch (final NullPointerException ex) {
			throw new BadPayloadException("ServiceDefinition Id list element is null", HttpStatus.SC_BAD_REQUEST, origin);
		}
	}
}
