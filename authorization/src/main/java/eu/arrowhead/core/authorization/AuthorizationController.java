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

package eu.arrowhead.core.authorization;

import java.security.PublicKey;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.logging.LogLevel;
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
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.CoreDefaults;
import eu.arrowhead.common.CoreUtilities;
import eu.arrowhead.common.Defaults;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.CoreUtilities.ValidatedPageParams;
import eu.arrowhead.common.core.CoreSystem;
import eu.arrowhead.common.database.entity.Logs;
import eu.arrowhead.common.database.service.CommonDBService;
import eu.arrowhead.common.dto.internal.AuthorizationInterCloudCheckRequestDTO;
import eu.arrowhead.common.dto.internal.AuthorizationInterCloudCheckResponseDTO;
import eu.arrowhead.common.dto.internal.AuthorizationInterCloudListResponseDTO;
import eu.arrowhead.common.dto.internal.AuthorizationInterCloudRequestDTO;
import eu.arrowhead.common.dto.internal.AuthorizationInterCloudResponseDTO;
import eu.arrowhead.common.dto.internal.AuthorizationIntraCloudCheckRequestDTO;
import eu.arrowhead.common.dto.internal.AuthorizationIntraCloudCheckResponseDTO;
import eu.arrowhead.common.dto.internal.AuthorizationIntraCloudListResponseDTO;
import eu.arrowhead.common.dto.internal.AuthorizationIntraCloudRequestDTO;
import eu.arrowhead.common.dto.internal.AuthorizationIntraCloudResponseDTO;
import eu.arrowhead.common.dto.internal.AuthorizationSubscriptionCheckRequestDTO;
import eu.arrowhead.common.dto.internal.AuthorizationSubscriptionCheckResponseDTO;
import eu.arrowhead.common.dto.internal.IdIdListDTO;
import eu.arrowhead.common.dto.internal.LogEntryListResponseDTO;
import eu.arrowhead.common.dto.internal.TokenDataDTO;
import eu.arrowhead.common.dto.internal.TokenGenerationMultiServiceResponseDTO;
import eu.arrowhead.common.dto.internal.TokenGenerationProviderDTO;
import eu.arrowhead.common.dto.internal.TokenGenerationRequestDTO;
import eu.arrowhead.common.dto.internal.TokenGenerationResponseDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.common.verifier.ServiceInterfaceNameVerifier;
import eu.arrowhead.core.authorization.database.service.AuthorizationDBService;
import eu.arrowhead.core.authorization.token.TokenGenerationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(tags = { CoreCommonConstants.SWAGGER_TAG_ALL })
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
	
	private static final String TOKEN_DESCRIPTION = "Generates tokens for a consumer which can be used to access the specified service of the specified providers";
	private static final String TOKEN_MULTI_SERVICE_DESCRIPTION = "Generates tokens for different services and different consumers which can be used to access the specified services of the specified providers";
	private static final String TOKEN_HTTP_200_MESSAGE = "Tokens returned";
	private static final String TOKEN_HTTP_400_MESSAGE = "Could not generate tokens";
	
	private static final String PUBLIC_KEY_DESCRIPTION = "Returns the public key of the Authorization core service as a Base64 encoded JSON string";
	private static final String PUBLIC_KEY_200_MESSAGE = "Public key returned";
	
	private static final String AUTHORIZATION_INTRA_CLOUD_MGMT_URI = CoreCommonConstants.MGMT_URI + "/intracloud";
	private static final String AUTHORIZATION_INTRA_CLOUD_MGMT_BY_ID_URI = AUTHORIZATION_INTRA_CLOUD_MGMT_URI + "/{" + PATH_VARIABLE_ID + "}";
	private static final String GET_AUTHORIZATION_INTRA_CLOUD_MGMT_HTTP_200_MESSAGE = "AuthorizationIntraCloud returned";
	private static final String GET_AUTHORIZATION_INTRA_CLOUD_MGMT_HTTP_400_MESSAGE = "Could not retrieve AuthorizationIntraCloud";
	private static final String DELETE_AUTHORIZATION_INTRA_CLOUD_MGMT_HTTP_200_MESSAGE = "AuthorizationIntraCloud removed";
	private static final String DELETE_AUTHORIZATION_INTRA_CLOUD_MGMT_HTTP_400_MESSAGE = "Could not remove AuthorizationIntraCloud";
	private static final String POST_AUTHORIZATION_INTRA_CLOUD_MGMT_HTTP_201_MESSAGE = "AuthorizationIntraClouds created";
	private static final String POST_AUTHORIZATION_INTRA_CLOUD_MGMT_HTTP_400_MESSAGE = "Could not create AuthorizationIntraCloud";
	
	private static final String POST_AUTHORIZATION_INTRA_CLOUD_HTTP_200_MESSAGE = "AuthorizationIntraCloud result returned";
	private static final String POST_AUTHORIZATION_INTRA_CLOUD_HTTP_400_MESSAGE = "Could not check AuthorizationIntraCloud";
	
	private static final String AUTHORIZATION_INTER_CLOUD_MGMT_URI = CoreCommonConstants.MGMT_URI + "/intercloud";
	private static final String AUTHORIZATION_INTER_CLOUD_MGMT_BY_ID_URI = AUTHORIZATION_INTER_CLOUD_MGMT_URI + "/{" + PATH_VARIABLE_ID + "}";
	private static final String GET_AUTHORIZATION_INTER_CLOUD_HTTP_200_MESSAGE = "AuthorizationInterCloud returned";
	private static final String GET_AUTHORIZATION_INTER_CLOUD_HTTP_400_MESSAGE = "Could not retrieve AuthorizationInterCloud";
	private static final String DELETE_AUTHORIZATION_INTER_CLOUD_HTTP_200_MESSAGE = "AuthorizationInterCloud removed";
	private static final String DELETE_AUTHORIZATION_INTER_CLOUD_HTTP_400_MESSAGE = "Could not remove AuthorizationInterCloud";
	private static final String POST_AUTHORIZATION_INTER_CLOUD_HTTP_201_MESSAGE = "AuthorizationInterClouds created";
	private static final String POST_AUTHORIZATION_INTER_CLOUD_MGMT_HTTP_400_MESSAGE = "Could not create AuthorizationInterCloud";
	
	private static final String POST_AUTHORIZATION_INTER_CLOUD_HTTP_200_MESSAGE = "AuthorizationInterCloud result returned";
	private static final String POST_AUTHORIZATION_INTER_CLOUD_HTTP_400_MESSAGE = "Could not check AuthorizationInterCloud";
	
	private final Logger logger = LogManager.getLogger(AuthorizationController.class);
	
	@Autowired
	private AuthorizationDBService authorizationDBService;	
	
	@Autowired
	private CommonDBService commonDBService;

	@Autowired
	private TokenGenerationService tokenGenerationService;
	
	@Autowired
	private ServiceInterfaceNameVerifier interfaceNameVerifier;  
	
	@Resource(name = CommonConstants.ARROWHEAD_CONTEXT)
	private Map<String,Object> arrowheadContext;
	
	@Value(CommonConstants.$SERVER_SSL_ENABLED_WD)
	private boolean secure;
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return an echo message with the purpose of testing the core service availability", response = String.class, tags = { CoreCommonConstants.SWAGGER_TAG_CLIENT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = CoreCommonConstants.SWAGGER_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path = CommonConstants.ECHO_URI)
	public String echoService() {
		return "Got it!";
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return requested log entries by the given parameters", response = LogEntryListResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = CoreCommonConstants.QUERY_LOG_ENTRIES_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = CoreCommonConstants.QUERY_LOG_ENTRIES_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path = CoreCommonConstants.OP_QUERY_LOG_ENTRIES, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public LogEntryListResponseDTO getLogEntries(
			@RequestParam(name = CoreCommonConstants.REQUEST_PARAM_PAGE, required = false) final Integer page,
			@RequestParam(name = CoreCommonConstants.REQUEST_PARAM_ITEM_PER_PAGE, required = false) final Integer size,
			@RequestParam(name = CoreCommonConstants.REQUEST_PARAM_DIRECTION, defaultValue = CoreDefaults.DEFAULT_REQUEST_PARAM_DIRECTION_VALUE) final String direction,
			@RequestParam(name = CoreCommonConstants.REQUEST_PARAM_SORT_FIELD, defaultValue = Logs.FIELD_NAME_ID) final String sortField,
			@RequestParam(name = CoreCommonConstants.REQUEST_PARAM_LOG_LEVEL, required = false) final String logLevel,
			@RequestParam(name = CoreCommonConstants.REQUEST_PARAM_FROM, required = false) final String from,
			@RequestParam(name = CoreCommonConstants.REQUEST_PARAM_TO, required = false) final String to,
			@RequestParam(name = CoreCommonConstants.REQUEST_PARAM_LOGGER, required = false) final String loggerStr) { 
		logger.debug("New getLogEntries GET request received with page: {} and item_per page: {}", page, size);
				
		final String origin = CommonConstants.AUTHORIZATION_URI + CoreCommonConstants.OP_QUERY_LOG_ENTRIES;
		final ValidatedPageParams validParameters = CoreUtilities.validatePageParameters(page, size, direction, origin);
		final List<LogLevel> logLevels = CoreUtilities.getLogLevels(logLevel, origin);
		
		try {
			final ZonedDateTime _from = Utilities.parseUTCStringToLocalZonedDateTime(from);
			final ZonedDateTime _to = Utilities.parseUTCStringToLocalZonedDateTime(to);
			
			if (_from != null && _to != null && _to.isBefore(_from)) {
				throw new BadPayloadException("Invalid time interval", HttpStatus.SC_BAD_REQUEST, origin);
			}
			
			final LogEntryListResponseDTO response = commonDBService.getLogEntriesResponse(validParameters.getValidatedPage(), validParameters.getValidatedSize(), validParameters.getValidatedDirection(), sortField, CoreSystem.AUTHORIZATION, 
																						   logLevels, _from, _to, loggerStr);
			
			logger.debug("Log entries  with page: {} and item_per page: {} retrieved successfully", page, size);
			return response;
		} catch (final DateTimeParseException ex) {
			throw new BadPayloadException("Invalid time parameter", HttpStatus.SC_BAD_REQUEST, origin, ex);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return requested AuthorizationIntraCloud entries by the given parameters", response = AuthorizationIntraCloudListResponseDTO.class,
				  tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = GET_AUTHORIZATION_INTRA_CLOUD_MGMT_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = GET_AUTHORIZATION_INTRA_CLOUD_MGMT_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path = AUTHORIZATION_INTRA_CLOUD_MGMT_URI, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public AuthorizationIntraCloudListResponseDTO getAuthorizationIntraClouds(
			@RequestParam(name = CoreCommonConstants.REQUEST_PARAM_PAGE, required = false) final Integer page,
			@RequestParam(name = CoreCommonConstants.REQUEST_PARAM_ITEM_PER_PAGE, required = false) final Integer size,
			@RequestParam(name = CoreCommonConstants.REQUEST_PARAM_DIRECTION, defaultValue = CoreDefaults.DEFAULT_REQUEST_PARAM_DIRECTION_VALUE) final String direction,
			@RequestParam(name = CoreCommonConstants.REQUEST_PARAM_SORT_FIELD, defaultValue = CoreCommonConstants.COMMON_FIELD_NAME_ID) final String sortField) {
		logger.debug("New AuthorizationIntraCloud get request recieved with page: {} and item_per page: {}", page, size);
		
		int validatedPage;
		int validatedSize;
		if (page == null && size == null) {
			validatedPage = -1;
			validatedSize = -1;
		} else {
			if (page == null || size == null) {
				throw new BadPayloadException("Defined page or size could not be with undefined size or page.", HttpStatus.SC_BAD_REQUEST, CommonConstants.AUTHORIZATION_URI +
											  AUTHORIZATION_INTRA_CLOUD_MGMT_URI);
			} else {
				validatedPage = page;
				validatedSize = size;
			}
		}
		
		final Direction validatedDirection = CoreUtilities.calculateDirection(direction, CommonConstants.AUTHORIZATION_URI + AUTHORIZATION_INTRA_CLOUD_MGMT_URI);
		final AuthorizationIntraCloudListResponseDTO authorizationIntraCloudEntriesResponse = authorizationDBService.getAuthorizationIntraCloudEntriesResponse(validatedPage, validatedSize,
																																							   validatedDirection, sortField);
		logger.debug("AuthorizationIntraClouds  with page: {} and item_per page: {} retrieved successfully", page, size);
		
		return authorizationIntraCloudEntriesResponse;
	}
		
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return requested AuthorizationIntraCloud entry", response = AuthorizationIntraCloudResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = GET_AUTHORIZATION_INTRA_CLOUD_MGMT_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = GET_AUTHORIZATION_INTRA_CLOUD_MGMT_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path = AUTHORIZATION_INTRA_CLOUD_MGMT_BY_ID_URI, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public AuthorizationIntraCloudResponseDTO getAuthorizationIntraCloudById(@PathVariable(value = PATH_VARIABLE_ID) final long id) {
		logger.debug("New AuthorizationIntraCloud get request recieved with id: {}", id);
		
		if (id < 1) {
			throw new BadPayloadException(ID_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, CommonConstants.AUTHORIZATION_URI + AUTHORIZATION_INTRA_CLOUD_MGMT_BY_ID_URI);
		}
		
		final AuthorizationIntraCloudResponseDTO authorizationIntraCloudEntryByIdResponse = authorizationDBService.getAuthorizationIntraCloudEntryByIdResponse(id);
		logger.debug("AuthorizationIntraCloud entry with id: {} successfully retrieved", id);
		
		return authorizationIntraCloudEntryByIdResponse;
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Remove the requested AuthorizationIntraCloud entry", tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = DELETE_AUTHORIZATION_INTRA_CLOUD_MGMT_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = DELETE_AUTHORIZATION_INTRA_CLOUD_MGMT_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@DeleteMapping(path = AUTHORIZATION_INTRA_CLOUD_MGMT_BY_ID_URI)
	public void removeAuthorizationIntraCloudById(@PathVariable(value = PATH_VARIABLE_ID) final long id) {
		logger.debug("New AuthorizationIntraCloud delete request recieved with id: {}", id);
		
		if (id < 1) {
			throw new BadPayloadException(ID_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, CommonConstants.AUTHORIZATION_URI + AUTHORIZATION_INTRA_CLOUD_MGMT_BY_ID_URI);
		}
		
		authorizationDBService.removeAuthorizationIntraCloudEntryById(id);
		logger.debug("AuthorizationIntraCloud with id: '{}' successfully deleted", id);
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Create the requested AuthorizationIntraCloud entries", response = AuthorizationIntraCloudListResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_CREATED, message = POST_AUTHORIZATION_INTRA_CLOUD_MGMT_HTTP_201_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = POST_AUTHORIZATION_INTRA_CLOUD_MGMT_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@PostMapping(path = AUTHORIZATION_INTRA_CLOUD_MGMT_URI, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(value = org.springframework.http.HttpStatus.CREATED)
	@ResponseBody public AuthorizationIntraCloudListResponseDTO registerAuthorizationIntraCloud(@RequestBody final AuthorizationIntraCloudRequestDTO request) {
		logger.debug("New AuthorizationIntraCloud registration request recieved");
		
		final boolean isConsumerIdInvalid = request.getConsumerId() == null || request.getConsumerId() < 1;
		final boolean isProviderListEmpty = request.getProviderIds() == null || request.getProviderIds().isEmpty();
		final boolean isServiceDefinitionListEmpty = request.getServiceDefinitionIds() == null || request.getServiceDefinitionIds().isEmpty();
		final boolean isInterfaceListEmpty = request.getInterfaceIds() == null || request.getInterfaceIds().isEmpty();
		
		if (isConsumerIdInvalid || isProviderListEmpty || isServiceDefinitionListEmpty || isInterfaceListEmpty) {
			String exceptionMsg = "Payload is invalid due to the following reasons:";
			exceptionMsg = isConsumerIdInvalid ? exceptionMsg + " invalid consumer id," : exceptionMsg;
			exceptionMsg = isProviderListEmpty ? exceptionMsg + " providerId list is empty," : exceptionMsg;
			exceptionMsg = isServiceDefinitionListEmpty ? exceptionMsg + " serviceDefinitionList is empty," : exceptionMsg;
			exceptionMsg = isInterfaceListEmpty ? exceptionMsg + " interfaceList is empty," : exceptionMsg;
			exceptionMsg = exceptionMsg.substring(0, exceptionMsg.length() - 1);
			
			throw new BadPayloadException(exceptionMsg, HttpStatus.SC_BAD_REQUEST, CommonConstants.AUTHORIZATION_URI + AUTHORIZATION_INTRA_CLOUD_MGMT_URI);
		}
		
		if (request.getProviderIds().size() > 1 && request.getServiceDefinitionIds().size() > 1) {
			throw new BadPayloadException("providerIds list or serviceDefinitionIds list should contain only one element, but both contain more", HttpStatus.SC_BAD_REQUEST,
										  CommonConstants.AUTHORIZATION_URI + AUTHORIZATION_INTRA_CLOUD_MGMT_URI);
		}
		
		if (request.getServiceDefinitionIds().size() > 1 && request.getInterfaceIds().size() > 1) {
			throw new BadPayloadException("serviceDefinitionIds list or interfaceIds list should contain only one element, but both contain more", HttpStatus.SC_BAD_REQUEST,
										  CommonConstants.AUTHORIZATION_URI + AUTHORIZATION_INTRA_CLOUD_MGMT_URI);
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
		
		final Set<Long> interfaceIdSet = new HashSet<>();
		for (final Long id : request.getInterfaceIds()) {
			if (id != null && id > 0) {
				interfaceIdSet.add(id);
			} else {
				logger.debug("Invalid ServiceInterface id: {}", id);
			}
		}
		
		final AuthorizationIntraCloudListResponseDTO response = authorizationDBService.createBulkAuthorizationIntraCloudResponse(request.getConsumerId(), providerIdSet, serviceIdSet, interfaceIdSet);
		logger.debug("registerAuthorizationIntraCloud has been finished");
		
		return response;
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return requested AuthorizationInterCloud entries by the given parameters", response = AuthorizationInterCloudListResponseDTO.class,
				  tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = GET_AUTHORIZATION_INTER_CLOUD_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_OK, message = GET_AUTHORIZATION_INTER_CLOUD_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path = AUTHORIZATION_INTER_CLOUD_MGMT_URI, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public AuthorizationInterCloudListResponseDTO getAuthorizationInterClouds(
			@RequestParam(name = CoreCommonConstants.REQUEST_PARAM_PAGE, required = false) final Integer page,
			@RequestParam(name = CoreCommonConstants.REQUEST_PARAM_ITEM_PER_PAGE, required = false) final Integer size,
			@RequestParam(name = CoreCommonConstants.REQUEST_PARAM_DIRECTION, defaultValue = CoreDefaults.DEFAULT_REQUEST_PARAM_DIRECTION_VALUE) final String direction,
			@RequestParam(name = CoreCommonConstants.REQUEST_PARAM_SORT_FIELD, defaultValue = CoreCommonConstants.COMMON_FIELD_NAME_ID) final String sortField) {
		logger.debug("New AuthorizationInterCloud get request recieved with page: {} and item_per page: {}", page, size);
		
		int validatedPage;
		int validatedSize;
		if (page == null && size == null) {
			validatedPage = -1;
			validatedSize = -1;
		} else {
			if (page == null || size == null) {
				throw new BadPayloadException("Defined page or size could not be with undefined size or page.", HttpStatus.SC_BAD_REQUEST, CommonConstants.AUTHORIZATION_URI +
											  AUTHORIZATION_INTER_CLOUD_MGMT_URI);
			} else {
				validatedPage = page;
				validatedSize = size;
			}
		}
		
		final Direction validatedDirection = CoreUtilities.calculateDirection(direction, CommonConstants.AUTHORIZATION_URI + AUTHORIZATION_INTER_CLOUD_MGMT_URI);
		final AuthorizationInterCloudListResponseDTO authorizationInterCloudEntriesResponse = authorizationDBService.getAuthorizationInterCloudEntriesResponse(validatedPage, validatedSize,
																																							   validatedDirection, sortField);
		logger.debug("AuthorizationInterClouds  with page: {} and item_per page: {} succesfully retrived", page, size);
		
		return authorizationInterCloudEntriesResponse;
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return requested AuthorizationInterCloud entry", response = AuthorizationInterCloudResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = GET_AUTHORIZATION_INTER_CLOUD_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_OK, message = GET_AUTHORIZATION_INTER_CLOUD_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path = AUTHORIZATION_INTER_CLOUD_MGMT_BY_ID_URI, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public AuthorizationInterCloudResponseDTO getAuthorizationInterCloudById(@PathVariable(value = PATH_VARIABLE_ID) final long id) {
		logger.debug("New AuthorizationInterCloud get request recieved with id: {}", id);
		
		if (id < 1) {
			throw new BadPayloadException(ID_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, CommonConstants.AUTHORIZATION_URI + AUTHORIZATION_INTER_CLOUD_MGMT_BY_ID_URI);
		}
		
		final AuthorizationInterCloudResponseDTO authorizationInterCloudEntryByIdResponse = authorizationDBService.getAuthorizationInterCloudEntryByIdResponse(id);
		logger.debug("AuthorizationInterCloud entry with id: {} successfully retrieved", id);
		
		return authorizationInterCloudEntryByIdResponse;
	}

	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Create the requested AuthorizationInterCloud entries", response = AuthorizationInterCloudListResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_CREATED, message = POST_AUTHORIZATION_INTER_CLOUD_HTTP_201_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = POST_AUTHORIZATION_INTER_CLOUD_MGMT_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@PostMapping(path = AUTHORIZATION_INTER_CLOUD_MGMT_URI, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(value = org.springframework.http.HttpStatus.CREATED)
	@ResponseBody public AuthorizationInterCloudListResponseDTO registerAuthorizationInterCloud(@RequestBody final AuthorizationInterCloudRequestDTO request) {
		logger.debug("New AuthorizationInterCloud registration request recieved");
		
		final boolean isCloudIdNotValid = request.getCloudId() == null || request.getCloudId() < 1;
		final boolean isProviderListNotValid = request.getProviderIdList() == null || request.getProviderIdList().isEmpty();
		final boolean isServiceDefinitionListNotValid = request.getServiceDefinitionIdList() == null || request.getServiceDefinitionIdList().isEmpty() ;
		final boolean isInterfaceListNotValid = request.getInterfaceIdList() == null || request.getInterfaceIdList().isEmpty();
		
		if (isCloudIdNotValid || isProviderListNotValid || isServiceDefinitionListNotValid || isInterfaceListNotValid) {
			String exceptionMsg = isCloudIdNotValid ? "Cloud Id is not valid," : "";
			exceptionMsg = isProviderListNotValid ? exceptionMsg + " ProviderList is null or blank," : exceptionMsg; 
			exceptionMsg = isServiceDefinitionListNotValid ? exceptionMsg + " ServiceDefinitionList is null or blank," :  exceptionMsg;
			exceptionMsg = isInterfaceListNotValid ? exceptionMsg + " InterfaceList is null or blank," : exceptionMsg;
			exceptionMsg = exceptionMsg.substring(0, exceptionMsg.length() - 1).trim();
			
			throw new BadPayloadException(exceptionMsg, HttpStatus.SC_BAD_REQUEST, CommonConstants.AUTHORIZATION_URI + AUTHORIZATION_INTER_CLOUD_MGMT_URI);
		}
		
		if (request.getProviderIdList().size() > 1 && request.getServiceDefinitionIdList().size() > 1) {
			throw new BadPayloadException("providerId list or serviceDefinitionId list should contain only one element, but both contain more", HttpStatus.SC_BAD_REQUEST,
										  CommonConstants.AUTHORIZATION_URI + AUTHORIZATION_INTER_CLOUD_MGMT_URI);
		}
		
		if (request.getServiceDefinitionIdList().size() > 1 && request.getInterfaceIdList().size() > 1) {
			throw new BadPayloadException("serviceDefinitionId list or interfaceId list should contain only one element, but both contain more", HttpStatus.SC_BAD_REQUEST,
										  CommonConstants.AUTHORIZATION_URI + AUTHORIZATION_INTER_CLOUD_MGMT_URI);
		}
		
		final long validatedCloudId = request.getCloudId();
		
		final Set<Long> providerIdSet = new HashSet<>();
		for (final Long id : request.getProviderIdList()) {
			if (id != null && id > 0) {
				providerIdSet.add(id);
			} else {
				logger.debug("Invalid provider system id: {}", id);
			}
		}
		
		final Set<Long> serviceIdSet = new HashSet<>();
		for (final Long id : request.getServiceDefinitionIdList()) {
			if (id != null && id > 0) {
				serviceIdSet.add(id);
			} else {
				logger.debug("Invalid service id: {}", id);
			}
		}
		
		final Set<Long> interfaceIdSet = new HashSet<>();
		for (final Long id : request.getInterfaceIdList()) {
			if (id != null && id > 0) {
				interfaceIdSet.add(id);
			} else {
				logger.debug("Invalid interace id: {}", id);
			}
		}
		
		final AuthorizationInterCloudListResponseDTO response = authorizationDBService.createBulkAuthorizationInterCloudResponse(validatedCloudId, providerIdSet, serviceIdSet, interfaceIdSet);
		logger.debug("registerAuthorizationInterCloud has been finished");
		
		return response;
	}

	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Remove the requested AuthorizationInterCloud entry", tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = DELETE_AUTHORIZATION_INTER_CLOUD_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = DELETE_AUTHORIZATION_INTER_CLOUD_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@DeleteMapping(path = AUTHORIZATION_INTER_CLOUD_MGMT_BY_ID_URI)
	public void removeAuthorizationInterCloudById(@PathVariable(value = PATH_VARIABLE_ID) final long id) {
		logger.debug("New AuthorizationInterCloud delete request recieved with id: {}", id);
		
		if (id < 1) {
			throw new BadPayloadException(ID_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, CommonConstants.AUTHORIZATION_URI + AUTHORIZATION_INTER_CLOUD_MGMT_BY_ID_URI);
		}
		
		authorizationDBService.removeAuthorizationInterCloudEntryById(id);
		logger.debug("AuthorizationInterCloud with id: '{}' successfully deleted", id);
	}

	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Checks whether the consumer System can use a Service from a list of provider Systems", response = AuthorizationIntraCloudCheckResponseDTO.class,
				  tags = { CoreCommonConstants.SWAGGER_TAG_PRIVATE })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = POST_AUTHORIZATION_INTRA_CLOUD_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = POST_AUTHORIZATION_INTRA_CLOUD_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@PostMapping(path = CommonConstants.OP_AUTH_INTRA_CHECK_URI, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public AuthorizationIntraCloudCheckResponseDTO checkAuthorizationIntraCloudRequest(@RequestBody final AuthorizationIntraCloudCheckRequestDTO request) {
		logger.debug("New AuthorizationIntraCloud check request recieved");
		
		final String origin = CommonConstants.AUTHORIZATION_URI + CommonConstants.OP_AUTH_INTRA_CHECK_URI;
		final SystemRequestDTO consumer = request.getConsumer();
		if (consumer == null) {
			throw new BadPayloadException("Consumer is null", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		checkSystemRequest(consumer, origin, false);
		
		final boolean isServiceDefinitionIdInvalid = request.getServiceDefinitionId() == null || request.getServiceDefinitionId() < 1;
		final boolean isProviderListEmpty = request.getProviderIdsWithInterfaceIds() == null || request.getProviderIdsWithInterfaceIds().isEmpty();
		
		if (isServiceDefinitionIdInvalid || isProviderListEmpty) {
			String exceptionMsg = "Payload is invalid due to the following reasons:";
			exceptionMsg = isServiceDefinitionIdInvalid ? exceptionMsg + " invalid serviceDefinition id," : exceptionMsg;
			exceptionMsg = isProviderListEmpty ? exceptionMsg + " providerId list is empty," : exceptionMsg;
			exceptionMsg = exceptionMsg.substring(0, exceptionMsg.length() - 1);
			
			throw new BadPayloadException(exceptionMsg, HttpStatus.SC_BAD_REQUEST, origin);
		}		
		
		final Set<IdIdListDTO> providerIdsWithInterfaceIdsSet = new HashSet<>();
		
		final Set<Long> providerIdCheck = new HashSet<>();
		for (final IdIdListDTO providerWithInterfaces : request.getProviderIdsWithInterfaceIds()) {
			if (providerWithInterfaces.getId() != null && providerWithInterfaces.getId() > 0 && !providerIdCheck.contains(providerWithInterfaces.getId())) {
				providerIdCheck.add(providerWithInterfaces.getId());
				
				final Set<Long> interfaceIdCheck = new HashSet<>();
				for (final Long interfaceId : providerWithInterfaces.getIdList()) {
					if (interfaceId != null && interfaceId > 0 && !interfaceIdCheck.contains(interfaceId)) {
						interfaceIdCheck.add(interfaceId);
					} else {
						logger.debug("Invalid or duplicated interface id: {} with provider id: {}", interfaceId, providerWithInterfaces.getId()); 
					}
				}
				
				providerWithInterfaces.getIdList().clear();
				providerWithInterfaces.getIdList().addAll(interfaceIdCheck);
				providerIdsWithInterfaceIdsSet.add(providerWithInterfaces);
			} else {
				logger.debug("Invalid or duplicated provider system id: {}", providerWithInterfaces.getId());
			}
		}
		
		final AuthorizationIntraCloudCheckResponseDTO response = authorizationDBService.checkAuthorizationIntraCloudRequest(consumer.getSystemName(), consumer.getAddress(), consumer.getPort(),
																															request.getServiceDefinitionId(), providerIdsWithInterfaceIdsSet);
		logger.debug("checkAuthorizationIntraCloudRequest has been finished");
		
		return response;
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Checks whether the subscriber System can recieve events from a list of publisher Systems", response = AuthorizationSubscriptionCheckResponseDTO.class,
				  tags = { CoreCommonConstants.SWAGGER_TAG_PRIVATE })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = POST_AUTHORIZATION_INTRA_CLOUD_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = POST_AUTHORIZATION_INTRA_CLOUD_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@PostMapping(path = CommonConstants.OP_AUTH_SUBSCRIPTION_CHECK_URI, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public AuthorizationSubscriptionCheckResponseDTO checkAuthorizationSubscriptionRequest(@RequestBody final AuthorizationSubscriptionCheckRequestDTO request) {
		logger.debug("New AuthorizationEventHandler check request recieved");
		
		final String origin = CommonConstants.AUTHORIZATION_URI + CommonConstants.OP_AUTH_SUBSCRIPTION_CHECK_URI;
		final SystemRequestDTO consumer = request.getConsumer();
		if (consumer == null) {
			throw new BadPayloadException("Consumer is null", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		checkSystemRequest(consumer, origin, false);
		
		if (request.getPublishers() != null && !request.getPublishers().isEmpty()) {
			for (final SystemRequestDTO publisher : request.getPublishers()) {
				checkSystemRequest(publisher, origin, false);
			}
		}
		
		final AuthorizationSubscriptionCheckResponseDTO response = authorizationDBService.checkAuthorizationSubscriptionRequest(consumer.getSystemName(), consumer.getAddress(), consumer.getPort(),
																																request.getPublishers());
		logger.debug("checkAuthorizationEventHandlerRequest has been finished");
		
		return response;
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Checks whether a Cloud is authorized to use a Service", response = AuthorizationInterCloudCheckResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_PRIVATE })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = POST_AUTHORIZATION_INTER_CLOUD_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = POST_AUTHORIZATION_INTER_CLOUD_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@PostMapping(path = CommonConstants.OP_AUTH_INTER_CHECK_URI, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public AuthorizationInterCloudCheckResponseDTO checkAuthorizationInterCloudRequest(@RequestBody final AuthorizationInterCloudCheckRequestDTO request) {
		logger.debug("New AuthorizationInterCloud check request recieved");
		
		final boolean isCloudInvalid = request.getCloud() == null ;
		final boolean isCloudOperatorInvalid = isCloudInvalid || Utilities.isEmpty(request.getCloud().getOperator());
		final boolean isCloudNameInvalid = isCloudInvalid || Utilities.isEmpty(request.getCloud().getName());
		final boolean isServiceDefinitionInvalid = Utilities.isEmpty(request.getServiceDefinition());
		final boolean isProvidersWithInterfacesListInvalid = request.getProviderIdsWithInterfaceIds() == null || request.getProviderIdsWithInterfaceIds().isEmpty();
		
		if (isCloudOperatorInvalid || isCloudNameInvalid || isServiceDefinitionInvalid || isProvidersWithInterfacesListInvalid ) {
			String exceptionMsg = "Payload is invalid due to the following reasons:";
			exceptionMsg = isCloudOperatorInvalid ? exceptionMsg + " cloud operator is empty, " : exceptionMsg;
			exceptionMsg = isCloudNameInvalid ? exceptionMsg + " cloud name is empty, " : exceptionMsg;
			exceptionMsg = isServiceDefinitionInvalid ? exceptionMsg + " serviceDefinition is empty, " : exceptionMsg;
			exceptionMsg = isProvidersWithInterfacesListInvalid ? exceptionMsg + " invalid providerIdsWithInterfaceIds list," : exceptionMsg;
			exceptionMsg = exceptionMsg.substring(0, exceptionMsg.length() - 1);
			
			throw new BadPayloadException(exceptionMsg, HttpStatus.SC_BAD_REQUEST, CommonConstants.AUTHORIZATION_URI + CommonConstants.OP_AUTH_INTER_CHECK_URI);
		}
		
		final AuthorizationInterCloudCheckResponseDTO response = authorizationDBService.checkAuthorizationInterCloudResponse(request.getCloud().getOperator(), request.getCloud().getName(),
																															 request.getServiceDefinition(), request.getProviderIdsWithInterfaceIds());
		logger.debug("checkAuthorizationInterCloudRequest has been finished");
		
		return response;
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = TOKEN_DESCRIPTION, response = TokenGenerationResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_PRIVATE })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = TOKEN_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = TOKEN_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@PostMapping(path = CommonConstants.OP_AUTH_TOKEN_URI, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public TokenGenerationResponseDTO generateTokens(@RequestBody final TokenGenerationRequestDTO request) {
		logger.debug("New token generation request received");
		checkTokenGenerationRequest(request);
		
		final TokenGenerationResponseDTO response = tokenGenerationService.generateTokensResponse(request);
		logger.debug("{} token(s) are generated for {}", calculateNumberOfTokens(response.getTokenData()), request.getConsumer().getSystemName());
		
		return response;
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = TOKEN_MULTI_SERVICE_DESCRIPTION, response = TokenGenerationMultiServiceResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_PRIVATE })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = TOKEN_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = TOKEN_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@PostMapping(path = CommonConstants.OP_AUTH_TOKEN_MULTI_SERVICE_URI, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public TokenGenerationMultiServiceResponseDTO generateMultiServiceTokens(@RequestBody final List<TokenGenerationRequestDTO> requestList) {
		logger.debug("New multi-service token generation request received");
		for (final TokenGenerationRequestDTO request : requestList) {
			checkTokenGenerationRequest(request);			
		}
		
		final TokenGenerationMultiServiceResponseDTO response = tokenGenerationService.generateMultiServiceTokensResponse(requestList);
		logger.debug("Multi-service token generation request has been finished");
		
		return response;
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = PUBLIC_KEY_DESCRIPTION, response = String.class, tags = { CoreCommonConstants.SWAGGER_TAG_CLIENT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = PUBLIC_KEY_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path = CommonConstants.OP_AUTH_KEY_URI, produces = MediaType.APPLICATION_JSON_VALUE)
	public String getPublicKey() {
		return "\"" + acquireAndConvertPublicKey() + "\"";
	}

	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private void checkTokenGenerationRequest(final TokenGenerationRequestDTO request) {
		logger.debug("checkTokenGenerationRequest started...");
		
		final String origin = CommonConstants.AUTHORIZATION_URI + CommonConstants.OP_AUTH_TOKEN_URI;
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
		
		for (final TokenGenerationProviderDTO provider : request.getProviders()) {
			checkTokenGenerationProviderDTO(provider, origin);
		}
		
		if (Utilities.isEmpty(request.getService())) {
			throw new BadPayloadException("Service is null or blank", HttpStatus.SC_BAD_REQUEST, origin);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private void checkTokenGenerationProviderDTO(final TokenGenerationProviderDTO provider, final String origin) {
		logger.debug("checkTokenGenerationProviderDTO started...");
		
		checkSystemRequest(provider.getProvider(), origin, true);
		
		if (provider.getServiceInterfaces() == null || provider.getServiceInterfaces().isEmpty()) {
			throw new BadPayloadException("Service interface list is null or empty", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		for (final String intf : provider.getServiceInterfaces()) {
			if (!interfaceNameVerifier.isValid(intf)) {
				throw new BadPayloadException("Specified interface name is not valid: " + intf, HttpStatus.SC_BAD_REQUEST, origin);
			}
		}
		
		if (provider.getTokenDuration() <= 0) {
			provider.setTokenDuration(-1);
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
		final String origin = CommonConstants.AUTHORIZATION_URI + CommonConstants.OP_AUTH_KEY_URI;
		
		if (!secure) {
			throw new ArrowheadException("Authorization core service runs in insecure mode.", HttpStatus.SC_INTERNAL_SERVER_ERROR, origin);
		}
		
		if (!arrowheadContext.containsKey(CommonConstants.SERVER_PUBLIC_KEY)) {
			throw new ArrowheadException("Public key is not available.", HttpStatus.SC_INTERNAL_SERVER_ERROR, origin);
		}
		
		final PublicKey publicKey = (PublicKey) arrowheadContext.get(CommonConstants.SERVER_PUBLIC_KEY);
		
		return Base64.getEncoder().encodeToString(publicKey.getEncoded());
	}
	
	//-------------------------------------------------------------------------------------------------
	private int calculateNumberOfTokens(final List<TokenDataDTO> tokenData) {
		return tokenData.stream().map(td -> td.getTokens().size()).collect(Collectors.summingInt(Integer::intValue));
	}
}