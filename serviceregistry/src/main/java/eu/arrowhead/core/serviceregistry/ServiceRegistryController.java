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

package eu.arrowhead.core.serviceregistry;

import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
import eu.arrowhead.common.CoreUtilities.ValidatedPageParams;
import eu.arrowhead.common.Defaults;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.core.CoreSystem;
import eu.arrowhead.common.core.CoreSystemService;
import eu.arrowhead.common.database.entity.Logs;
import eu.arrowhead.common.database.service.CommonDBService;
import eu.arrowhead.common.dto.internal.KeyValuesDTO;
import eu.arrowhead.common.dto.internal.LogEntryListResponseDTO;
import eu.arrowhead.common.dto.internal.ServiceDefinitionRequestDTO;
import eu.arrowhead.common.dto.internal.ServiceDefinitionsListResponseDTO;
import eu.arrowhead.common.dto.internal.ServiceInterfaceRequestDTO;
import eu.arrowhead.common.dto.internal.ServiceInterfacesListResponseDTO;
import eu.arrowhead.common.dto.internal.ServiceRegistryGroupedResponseDTO;
import eu.arrowhead.common.dto.internal.ServiceRegistryListResponseDTO;
import eu.arrowhead.common.dto.internal.SystemListResponseDTO;
import eu.arrowhead.common.dto.shared.ServiceDefinitionResponseDTO;
import eu.arrowhead.common.dto.shared.ServiceInterfaceResponseDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryFormDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryFormListDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryResultDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryResultListDTO;
import eu.arrowhead.common.dto.shared.ServiceRegistryRequestDTO;
import eu.arrowhead.common.dto.shared.ServiceRegistryResponseDTO;
import eu.arrowhead.common.dto.shared.ServiceSecurityType;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.common.dto.shared.SystemResponseDTO;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.common.processor.NetworkAddressDetector;
import eu.arrowhead.common.processor.NetworkAddressPreProcessor;
import eu.arrowhead.common.processor.model.AddressDetectionResult;
import eu.arrowhead.common.verifier.CommonNamePartVerifier;
import eu.arrowhead.common.verifier.NetworkAddressVerifier;
import eu.arrowhead.common.verifier.ServiceInterfaceNameVerifier;
import eu.arrowhead.core.serviceregistry.database.service.ServiceRegistryDBService;
import eu.arrowhead.core.serviceregistry.service.ServiceRegistryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(tags = { CoreCommonConstants.SWAGGER_TAG_ALL })
@CrossOrigin(maxAge = Defaults.CORS_MAX_AGE, allowCredentials = Defaults.CORS_ALLOW_CREDENTIALS,
		allowedHeaders = { HttpHeaders.ORIGIN, HttpHeaders.CONTENT_TYPE, HttpHeaders.ACCEPT, HttpHeaders.AUTHORIZATION }
)
@RestController
@RequestMapping(CommonConstants.SERVICEREGISTRY_URI)
public class ServiceRegistryController {

	//=================================================================================================
	// members

	private static final String GET_SYSTEM_BY_ID_HTTP_200_MESSAGE = "System by requested id returned";
	private static final String GET_SYSTEM_BY_ID_HTTP_400_MESSAGE = "No Such System by requested id";
	private static final String PATH_VARIABLE_ID = "id";
	private static final String SYSTEM_BY_ID_URI = CoreCommonConstants.MGMT_URI + "/system/{" + PATH_VARIABLE_ID + "}";
	private static final String SYSTEMS_URI = CoreCommonConstants.MGMT_URI + "/systems";
	private static final String SYSTEMS_BY_ID_URI = CoreCommonConstants.MGMT_URI + "/systems/{" + PATH_VARIABLE_ID + "}";
	private static final String GET_SYSTEMS_HTTP_200_MESSAGE = "Systems returned";
	private static final String GET_SYSTEMS_HTTP_400_MESSAGE = " Invalid parameters";
	private static final String POST_SYSTEM_HTTP_201_MESSAGE = "System created";
	private static final String POST_SYSTEM_HTTP_400_MESSAGE = "Could not create system";
	private static final String PUT_SYSTEM_HTTP_200_MESSAGE = "System updated";
	private static final String PUT_SYSTEM_HTTP_400_MESSAGE = "Could not update system";
	private static final String PATCH_SYSTEM_HTTP_200_MESSAGE = "System updated";
	private static final String PATCH_SYSTEM_HTTP_400_MESSAGE = "Could not update system";
	private static final String DELETE_SYSTEM_HTTP_200_MESSAGE = "System deleted";
	private static final String DELETE_SYSTEM_HTTP_400_MESSAGE = "Could not delete system";

	private static final String SERVICES_URI = CoreCommonConstants.MGMT_URI + "/services";
	private static final String SERVICES_BY_ID_URI = SERVICES_URI + "/{" + PATH_VARIABLE_ID + "}";
	private static final String GET_SERVICES_HTTP_200_MESSAGE = "Services returned";
	private static final String GET_SERVICES_HTTP_400_MESSAGE = "Could not retrieve service definition";
	private static final String POST_SERVICES_HTTP_201_MESSAGE = "Service definition created";
	private static final String POST_SERVICES_HTTP_400_MESSAGE = "Could not create service definition";
	private static final String PUT_SERVICES_HTTP_200_MESSAGE = "Service definition updated";
	private static final String PUT_SERVICES_HTTP_400_MESSAGE = "Could not update service definition";
	private static final String PATCH_SERVICES_HTTP_200_MESSAGE = "Service definition updated";
	private static final String PATCH_SERVICES_HTTP_400_MESSAGE = "Could not update service definition";
	private static final String DELETE_SERVICES_HTTP_200_MESSAGE = "Service definition removed";
	private static final String DELETE_SERVICES_HTTP_400_MESSAGE = "Could not remove service definition";

	private static final String SERVICE_INTERFACES_URI = CoreCommonConstants.MGMT_URI + "/interfaces";
	private static final String SERVICE_INTERFACES_BY_ID_URI = SERVICE_INTERFACES_URI + "/{" + PATH_VARIABLE_ID + "}";
	private static final String GET_SERVICE_INTERFACES_HTTP_200_MESSAGE = "Service Interfaces returned";
	private static final String GET_SERVICE_INTERFACES_HTTP_400_MESSAGE = "Could not retrieve service interface";
	private static final String POST_SERVICE_INTERFACES_HTTP_201_MESSAGE = "Service interface created";
	private static final String POST_SERVICE_INTERFACES_HTTP_400_MESSAGE = "Could not create service interface";
	private static final String PUT_SERVICE_INTERFACES_HTTP_200_MESSAGE = "Service interface updated";
	private static final String PUT_SERVICE_INTERFACES_HTTP_400_MESSAGE = "Could not update service interface";
	private static final String PATCH_SERVICE_INTERFACES_HTTP_200_MESSAGE = "Service interface updated";
	private static final String PATCH_SERVICE_INTERFACES_HTTP_400_MESSAGE = "Could not update service interface";
	private static final String DELETE_SERVICE_INTERFACES_HTTP_200_MESSAGE = "Service interface removed";
	private static final String DELETE_SERVICE_INTERFACES_HTTP_400_MESSAGE = "Could not remove service interface";

	private static final String SERVICEREGISTRY_REGISTER_DESCRIPTION = "Registers a service";
	private static final String SERVICEREGISTRY_REGISTER_201_MESSAGE = "Service registered";
	private static final String SERVICEREGISTRY_REGISTER_400_MESSAGE = "Could not register service";
	private static final String SERVICEREGISTRY_UNREGISTER_DESCRIPTION = "Remove a registered service";
	private static final String SERVICEREGISTRY_UNREGISTER_200_MESSAGE = "Registered service removed";
	private static final String SERVICEREGISTRY_UNREGISTER_400_MESSAGE = "Could not remove service";
	private static final String SERVICEREGISTRY_QUERY_DESCRIPTION = "Return Service Registry data that fits the specification";
	private static final String SERVICEREGISTRY_QUERY_200_MESSAGE = "Service Registry data returned";
	private static final String SERVICEREGISTRY_QUERY_400_MESSAGE = "Could not query Service Registry";
	private static final String SERVICEREGISTRY_QUERY_BY_SYSTEM_ID_DESCRIPTION = "Return system by requested id";
	private static final String SERVICEREGISTRY_QUERY_BY_SYSTEM_ID_200_MESSAGE = "System data by id returned";
	private static final String SERVICEREGISTRY_QUERY_BY_SYSTEM_ID_400_MESSAGE = "Could not query Service Registry by Consumer system id";
	private static final String SERVICEREGISTRY_QUERY_BY_SYSTEM_DTO_DESCRIPTION = "Return System by requested dto";
	private static final String SERVICEREGISTRY_QUERY_BY_SYSTEM_DTO_200_MESSAGE = "Consumer System data by requestDTO returned";
	private static final String SERVICEREGISTRY_QUERY_BY_SYSTEM_DTO_400_MESSAGE = "Could not query Service Registry by Consumer system requestDTO";
	private static final String SERVICEREGISTRY_UPDATE_DESCRIPTION = "Update a service";
	private static final String SERVICEREGISTRY_UPDATE_200_MESSAGE = "Service updated";
	private static final String SERVICEREGISTRY_UPDATE_400_MESSAGE = "Could not update service";
	private static final String SERVICEREGISTRY_MERGE_DESCRIPTION = "Merge/Patch a service";
	private static final String SERVICEREGISTRY_MERGE_200_MESSAGE = "Service merged";
	private static final String SERVICEREGISTRY_MERGE_400_MESSAGE = "Could not merge service";	

	private static final String NOT_VALID_PARAMETERS_ERROR_MESSAGE = "Not valid request parameters.";
	private static final String ID_NOT_VALID_ERROR_MESSAGE = "Id must be greater than 0. ";
	private static final String SYSTEM_NAME_NULL_ERROR_MESSAGE = " System name must have value ";
	private static final String SYSTEM_NAME_WRONG_FORMAT_ERROR_MESSAGE = "System name has invalid format. System names only contain maximum 63 character of letters (english alphabet), numbers and dash (-), and have to start with a letter (also cannot end with dash).";
	private static final String SYSTEM_PORT_NULL_ERROR_MESSAGE = " System port must have value ";
	private static final String SERVICE_DEFINITION_WRONG_FORMAT_ERROR_MESSAGE = "Service definition has invalid format. Service definition only contains maximum 63 character of letters (english alphabet), numbers and dash (-), and has to start with a letter (also cannot ends with dash).";
	private static final String SERVICE_DEFINITION_REQUIREMENT_WRONG_FORMAT_ERROR_MESSAGE = "Service definition requirement has invalid format. Service definition only contains maximum 63 character of letters (english alphabet), numbers and dash (-), and has to start with a letter (also cannot ends with dash).";

	private static final String SERVICEREGISTRY_MGMT_BY_ID_URI = CoreCommonConstants.MGMT_URI + "/{" + PATH_VARIABLE_ID + "}";
	private static final String PATH_VARIABLE_SERVICE_DEFINITION = "serviceDefinition";
	private static final String SERVICEREGISTRY_MGMT_BY_SERVICE_DEFINITION_URI = CoreCommonConstants.MGMT_URI + "/servicedef" + "/{" + PATH_VARIABLE_SERVICE_DEFINITION + "}";	
	private static final String SERVICEREGISTRY_MGMT_GROUPED_URI = CoreCommonConstants.MGMT_URI + "/grouped";
	private static final String GET_SERVICEREGISTRY_HTTP_200_MESSAGE = "Service Registry entries returned";
	private static final String GET_SERVICEREGISTRY_HTTP_400_MESSAGE = "Could not retrieve service registry entries";
	private static final String DELETE_SERVICEREGISTRY_HTTP_200_MESSAGE = "Service Registry entry removed";
	private static final String DELETE_SERVICEREGISTRY_HTTP_400_MESSAGE = "Could not remove service registry entry";
	
	private static final String GET_CONFIG_HTTP_200_MESSAGE = "Configuration returned";

	private final Logger logger = LogManager.getLogger(ServiceRegistryController.class);

	@Autowired
	private ServiceRegistryDBService serviceRegistryDBService;
	
	@Autowired
	private ServiceRegistryService serviceRegistryService;

	@Autowired
	private CommonDBService commonDBService;	

	@Autowired
	private ServiceInterfaceNameVerifier interfaceNameVerifier;

	@Autowired
	private CommonNamePartVerifier cnVerifier;
	
	@Autowired
	private NetworkAddressPreProcessor networkAddressPreProcessor;
	
	@Autowired
	private NetworkAddressVerifier networkAddressVerifier;
	
	@Autowired
	private NetworkAddressDetector networkAddressDetector;
	
	@Value(CoreCommonConstants.$USE_STRICT_SERVICE_DEFINITION_VERIFIER_WD)
	private boolean useStrictServiceDefinitionVerifier;
	
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
	@ApiOperation(value = "Return some configuration of ServiceRegistry", response = KeyValuesDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_PRIVATE })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = GET_CONFIG_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path = CoreCommonConstants.OP_SERVICEREGISTRY_PULL_CONFIG_URI, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public KeyValuesDTO pullConfig() {
		logger.debug("pullConfig started ...");
		return serviceRegistryService.getPublicConfig();
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
				
		final String origin = CommonConstants.SERVICEREGISTRY_URI + CoreCommonConstants.OP_QUERY_LOG_ENTRIES;
		final ValidatedPageParams validParameters = CoreUtilities.validatePageParameters(page, size, direction, origin);
		final List<LogLevel> logLevels = CoreUtilities.getLogLevels(logLevel, origin);
		
		try {
			final ZonedDateTime _from = Utilities.parseUTCStringToLocalZonedDateTime(from);
			final ZonedDateTime _to = Utilities.parseUTCStringToLocalZonedDateTime(to);
			
			if (_from != null && _to != null && _to.isBefore(_from)) {
				throw new BadPayloadException("Invalid time interval", HttpStatus.SC_BAD_REQUEST, origin);
			}
			
			final LogEntryListResponseDTO response = commonDBService.getLogEntriesResponse(validParameters.getValidatedPage(), validParameters.getValidatedSize(), validParameters.getValidatedDirection(), sortField, CoreSystem.SERVICEREGISTRY, 
																						   logLevels, _from, _to, loggerStr);
			
			logger.debug("Log entries  with page: {} and item_per page: {} retrieved successfully", page, size);
			return response;
		} catch (final DateTimeParseException ex) {
			throw new BadPayloadException("Invalid time parameter", HttpStatus.SC_BAD_REQUEST, origin, ex);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return system by id", response = SystemResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = GET_SYSTEM_BY_ID_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = GET_SYSTEM_BY_ID_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(SYSTEMS_BY_ID_URI)
	@ResponseBody public SystemResponseDTO getSystemById(@PathVariable(value = PATH_VARIABLE_ID) final long systemId) {
		logger.debug("getSystemById started ...");

		if (systemId < 1) {
			throw new BadPayloadException(ID_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, CommonConstants.SERVICEREGISTRY_URI + SYSTEM_BY_ID_URI);
		}

		return serviceRegistryDBService.getSystemById(systemId);
	}

	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return systems by request parameters", response = SystemListResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = GET_SYSTEMS_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = GET_SYSTEMS_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(SYSTEMS_URI)
	@ResponseBody public SystemListResponseDTO getSystems(
			@RequestParam(name = CoreCommonConstants.REQUEST_PARAM_PAGE, required = false) final Integer page,
			@RequestParam(name = CoreCommonConstants.REQUEST_PARAM_ITEM_PER_PAGE, required = false) final Integer size,
			@RequestParam(name = CoreCommonConstants.REQUEST_PARAM_DIRECTION, defaultValue = CoreDefaults.DEFAULT_REQUEST_PARAM_DIRECTION_VALUE) final String direction,
			@RequestParam(name = CoreCommonConstants.REQUEST_PARAM_SORT_FIELD, defaultValue = CoreCommonConstants.COMMON_FIELD_NAME_ID) final String sortField) {
		logger.debug("getSystems started ...");

		final int validatedPage;
		final int validatedSize;
		if (page == null && size == null) {
			validatedPage = -1;
			validatedSize = -1;
		} else {
			if (page == null || size == null) {
				throw new BadPayloadException(NOT_VALID_PARAMETERS_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, CommonConstants.SERVICEREGISTRY_URI + SYSTEMS_URI);
			} else {
				validatedPage = page;
				validatedSize = size;
			}
		}

		final Direction validatedDirection = CoreUtilities.calculateDirection(direction, CommonConstants.SERVICEREGISTRY_URI + SYSTEMS_URI);

		return serviceRegistryDBService.getSystemEntries(validatedPage, validatedSize, validatedDirection, sortField);
	}

	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return created system ", response = SystemResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_CREATED, message = POST_SYSTEM_HTTP_201_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = POST_SYSTEM_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@PostMapping(path = SYSTEMS_URI, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(org.springframework.http.HttpStatus.CREATED)
	@ResponseBody public SystemResponseDTO addSystem(@RequestBody final SystemRequestDTO request) {
		return callCreateSystem(null, request, CommonConstants.SERVICEREGISTRY_URI + SYSTEMS_URI);
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return created application system ", response = SystemResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_CLIENT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_CREATED, message = POST_SYSTEM_HTTP_201_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = POST_SYSTEM_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@PostMapping(path = CommonConstants.OP_SERVICEREGISTRY_REGISTER_SYSTEM_URI, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(org.springframework.http.HttpStatus.CREATED)
	@ResponseBody public SystemResponseDTO registerSystem(final HttpServletRequest servletRequest, @RequestBody final SystemRequestDTO dto) {
		return callCreateSystem(servletRequest, dto, CommonConstants.SERVICEREGISTRY_URI + CommonConstants.OP_SERVICEREGISTRY_REGISTER_SYSTEM_URI);
	}

	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return updated system ", response = SystemResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_CREATED, message = PUT_SYSTEM_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = PUT_SYSTEM_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@PutMapping(path = SYSTEMS_BY_ID_URI, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public SystemResponseDTO updateSystem(@PathVariable(value = PATH_VARIABLE_ID) final long systemId, @RequestBody final SystemRequestDTO request) {
		return callUpdateSystem(request, systemId);
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return system  updated by fields", response = SystemResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_CREATED, message = PATCH_SYSTEM_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = PATCH_SYSTEM_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@PatchMapping(path = SYSTEMS_BY_ID_URI, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public SystemResponseDTO mergeSystem(@PathVariable(value = PATH_VARIABLE_ID) final long systemId, @RequestBody final SystemRequestDTO request) {
		return callMergeSystem(request, systemId);
	}

	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Remove system", tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = DELETE_SYSTEM_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = DELETE_SYSTEM_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@DeleteMapping(path = SYSTEMS_BY_ID_URI)
	public void removeSystem(@PathVariable(value = PATH_VARIABLE_ID) final long id) {
		logger.debug("New System delete request received with id: {}", id);

		if (id < 1) {
			throw new BadPayloadException(ID_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, CommonConstants.SERVICEREGISTRY_URI + SYSTEMS_BY_ID_URI);
		}

		serviceRegistryDBService.removeSystemById(id);
		logger.debug("System with id: '{}' successfully deleted", id);
	}

	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Unregister the given system", tags = { CoreCommonConstants.SWAGGER_TAG_CLIENT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = DELETE_SYSTEM_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = DELETE_SYSTEM_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@DeleteMapping(path = CommonConstants.OP_SERVICEREGISTRY_UNREGISTER_SYSTEM_URI)
	@ResponseBody public void unregisterSystem(final HttpServletRequest servletRequest,
											   @RequestParam(CommonConstants.OP_SERVICEREGISTRY_UNREGISTER_REQUEST_PARAM_SYSTEM_NAME) final String systemName,
											   @RequestParam(name = CommonConstants.OP_SERVICEREGISTRY_UNREGISTER_REQUEST_PARAM_ADDRESS, required = false) final String address,
											   @RequestParam(name = CommonConstants.OP_SERVICEREGISTRY_UNREGISTER_REQUEST_PARAM_PORT) final int port) {
		logger.debug("System removal request received");
		
		final String checkedAddress = checkUnregisterSystemParameters(servletRequest, systemName, address, port);
		serviceRegistryDBService.removeSystemByNameAndAddressAndPort(systemName, checkedAddress, port);		
		logger.debug("{} successfully removed itself", systemName);
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return systems by request parameters", response = SystemListResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_PRIVATE })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = GET_SYSTEMS_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = GET_SYSTEMS_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path = CommonConstants.OP_SERVICEREGISTRY_PULL_SYSTEMS_URI, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public SystemListResponseDTO pullSystems(@RequestParam(name = CoreCommonConstants.REQUEST_PARAM_PAGE, required = false) final Integer page,
														   @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_ITEM_PER_PAGE, required = false) final Integer size,
														   @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_DIRECTION, defaultValue = CoreDefaults.DEFAULT_REQUEST_PARAM_DIRECTION_VALUE) final String direction,
														   @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_SORT_FIELD, defaultValue = CoreCommonConstants.COMMON_FIELD_NAME_ID) final String sortField) {
		logger.debug("pullSystems started ...");
		
		final int validatedPage;
		final int validatedSize;
		if (page == null && size == null) {
			validatedPage = -1;
			validatedSize = -1;
		} else {
			if (page == null || size == null) {
				throw new BadPayloadException(NOT_VALID_PARAMETERS_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, CommonConstants.SERVICEREGISTRY_URI + CommonConstants.OP_SERVICEREGISTRY_PULL_SYSTEMS_URI);
			} else {
				validatedPage = page;
				validatedSize = size;
			}			
		}
		
		final Direction validatedDirection = CoreUtilities.calculateDirection(direction, CommonConstants.SERVICEREGISTRY_URI + CommonConstants.OP_SERVICEREGISTRY_PULL_SYSTEMS_URI);
		
		return serviceRegistryDBService.getSystemEntries(validatedPage, validatedSize, validatedDirection, sortField);			
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return requested service definitions by the given parameters", response = ServiceDefinitionsListResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = GET_SERVICES_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = GET_SERVICES_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path = SERVICES_URI, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public ServiceDefinitionsListResponseDTO getServiceDefinitions(
			@RequestParam(name = CoreCommonConstants.REQUEST_PARAM_PAGE, required = false) final Integer page,
			@RequestParam(name = CoreCommonConstants.REQUEST_PARAM_ITEM_PER_PAGE, required = false) final Integer size,
			@RequestParam(name = CoreCommonConstants.REQUEST_PARAM_DIRECTION, defaultValue = CoreDefaults.DEFAULT_REQUEST_PARAM_DIRECTION_VALUE) final String direction,
			@RequestParam(name = CoreCommonConstants.REQUEST_PARAM_SORT_FIELD, defaultValue = CoreCommonConstants.COMMON_FIELD_NAME_ID) final String sortField) {
		logger.debug("New Service Definition get request received with page: {} and item_per page: {}", page, size);

		int validatedPage;
		int validatedSize;
		if (page == null && size == null) {
			validatedPage = -1;
			validatedSize = -1;
		} else {
			if (page == null || size == null) {
				throw new BadPayloadException("Defined page or size could not be with undefined size or page.", HttpStatus.SC_BAD_REQUEST, CommonConstants.SERVICEREGISTRY_URI + SERVICES_URI);
			} else {
				validatedPage = page;
				validatedSize = size;
			}
		}

		final Direction validatedDirection = CoreUtilities.calculateDirection(direction, CommonConstants.SERVICEREGISTRY_URI + SERVICES_URI);
		final ServiceDefinitionsListResponseDTO serviceDefinitionEntries = serviceRegistryDBService.getServiceDefinitionEntriesResponse(validatedPage, validatedSize, validatedDirection, sortField);
		logger.debug("Service definition  with page: {} and item_per page: {} successfully retrieved", page, size);

		return serviceDefinitionEntries;
	}

	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return requested service definition", response = ServiceDefinitionResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = GET_SERVICES_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = GET_SERVICES_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path = SERVICES_BY_ID_URI, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public ServiceDefinitionResponseDTO getServiceDefinitionById(@PathVariable(value = PATH_VARIABLE_ID) final long id) {
		logger.debug("New Service Definition get request received with id: {}", id);

		if (id < 1) {
			throw new BadPayloadException(ID_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, CommonConstants.SERVICEREGISTRY_URI + SERVICES_BY_ID_URI);
		}

		final ServiceDefinitionResponseDTO serviceDefinitionEntry = serviceRegistryDBService.getServiceDefinitionByIdResponse(id);
		logger.debug("Service definition with id: '{}' successfully retrieved", id);

		return serviceDefinitionEntry;
	}

	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return created service definition", response = ServiceDefinitionResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_CREATED, message = POST_SERVICES_HTTP_201_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = POST_SERVICES_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@PostMapping(path = SERVICES_URI, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(value = org.springframework.http.HttpStatus.CREATED)
	@ResponseBody public ServiceDefinitionResponseDTO addServiceDefinition(@RequestBody final ServiceDefinitionRequestDTO serviceDefinitionRequestDTO) {
		final String serviceDefinition = serviceDefinitionRequestDTO.getServiceDefinition();
		logger.debug("New Service Definition registration request received with definition: {}", serviceDefinition);

		if (Utilities.isEmpty(serviceDefinition)) {
			throw new BadPayloadException("Service definition is null or blank", HttpStatus.SC_BAD_REQUEST, CommonConstants.SERVICEREGISTRY_URI + SERVICES_URI);
		}
		
		if (useStrictServiceDefinitionVerifier && !cnVerifier.isValid(serviceDefinition)) {
			throw new BadPayloadException(SERVICE_DEFINITION_WRONG_FORMAT_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, CommonConstants.SERVICEREGISTRY_URI + SERVICES_URI);
		}

		for (final CoreSystemService coreSystemService : CoreSystemService.values()) {
			if (coreSystemService.getServiceDefinition().equalsIgnoreCase(serviceDefinition.trim())) {
				throw new BadPayloadException("Service definition '" + serviceDefinition + "' is a reserved arrowhead core system service.", HttpStatus.SC_BAD_REQUEST, CommonConstants.SERVICEREGISTRY_URI + SERVICES_URI);
			}
		}

		final ServiceDefinitionResponseDTO serviceDefinitionResponse = serviceRegistryDBService.createServiceDefinitionResponse(serviceDefinition.toLowerCase().trim());
		logger.debug("{} service definition successfully registered.", serviceDefinition);

		return serviceDefinitionResponse;
	}

	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return updated service definition", response = ServiceDefinitionResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = PUT_SERVICES_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = PUT_SERVICES_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@PutMapping(path = SERVICES_BY_ID_URI, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public ServiceDefinitionResponseDTO putUpdateServiceDefinition(@PathVariable(value = PATH_VARIABLE_ID) final long id,
																				 @RequestBody final ServiceDefinitionRequestDTO serviceDefinitionRequestDTO) {
		final String serviceDefinition = serviceDefinitionRequestDTO.getServiceDefinition();
		logger.debug("New Service Definition update request received with id: {}, definition: {}", id, serviceDefinition);

		if (id < 1) {
			throw new BadPayloadException(ID_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, CommonConstants.SERVICEREGISTRY_URI + SERVICES_BY_ID_URI);
		}

		if (Utilities.isEmpty(serviceDefinition)) {
			throw new BadPayloadException("Service definition is null or blank", HttpStatus.SC_BAD_REQUEST, CommonConstants.SERVICEREGISTRY_URI + SERVICES_BY_ID_URI);
		}
		
		if (useStrictServiceDefinitionVerifier && !cnVerifier.isValid(serviceDefinition)) {
			throw new BadPayloadException(SERVICE_DEFINITION_WRONG_FORMAT_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, CommonConstants.SERVICEREGISTRY_URI + SERVICES_URI);
		}

		for (final CoreSystemService coreSystemService : CoreSystemService.values()) {
			if (coreSystemService.getServiceDefinition().equalsIgnoreCase(serviceDefinition.trim())) {
				throw new BadPayloadException("serviceDefinition '" + serviceDefinition + "' is a reserved arrowhead core system service.", HttpStatus.SC_BAD_REQUEST, CommonConstants.SERVICEREGISTRY_URI + SERVICES_URI);
			}
		}

		final ServiceDefinitionResponseDTO serviceDefinitionResponse = serviceRegistryDBService.updateServiceDefinitionByIdResponse(id, serviceDefinition.toLowerCase().trim());
		logger.debug("Service definition with id: '{}' successfully updated with definition '{}'.", id, serviceDefinition);

		return serviceDefinitionResponse;
	}

	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return updated service definition", response = ServiceDefinitionResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = PATCH_SERVICES_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = PATCH_SERVICES_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@PatchMapping(path = SERVICES_BY_ID_URI, consumes = MediaType.APPLICATION_JSON_VALUE, produces = "application/json")
	@ResponseBody public ServiceDefinitionResponseDTO patchUpdateServiceDefinition(@PathVariable(value = PATH_VARIABLE_ID) final long id,
																				   @RequestBody final ServiceDefinitionRequestDTO serviceDefinitionRequestDTO) {
		// Currently ServiceDefinition has only one updateable field, therefore PUT and PATCH do the same
		
		return putUpdateServiceDefinition(id, serviceDefinitionRequestDTO);
	}

	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Remove service definition", tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = DELETE_SERVICES_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = DELETE_SERVICES_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@DeleteMapping(path = SERVICES_BY_ID_URI)
	public void removeServiceDefinition(@PathVariable(value = PATH_VARIABLE_ID) final long id) {
		logger.debug("New Service Definition delete request received with id: {}", id);

		if (id < 1) {
			throw new BadPayloadException(ID_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, CommonConstants.SERVICEREGISTRY_URI + SERVICES_BY_ID_URI);
		}

		serviceRegistryDBService.removeServiceDefinitionById(id);
		logger.debug("Service definition with id: '{}' successfully deleted", id);
	}

	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return requested service registry entries by the given parameters", response = ServiceRegistryListResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = GET_SERVICEREGISTRY_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = GET_SERVICEREGISTRY_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path = CoreCommonConstants.MGMT_URI)
	@ResponseBody public ServiceRegistryListResponseDTO getServiceRegistryEntries(
			@RequestParam(name = CoreCommonConstants.REQUEST_PARAM_PAGE, required = false) final Integer page,
			@RequestParam(name = CoreCommonConstants.REQUEST_PARAM_ITEM_PER_PAGE, required = false) final Integer size,
			@RequestParam(name = CoreCommonConstants.REQUEST_PARAM_DIRECTION, defaultValue = CoreDefaults.DEFAULT_REQUEST_PARAM_DIRECTION_VALUE) final String direction,
			@RequestParam(name = CoreCommonConstants.REQUEST_PARAM_SORT_FIELD, defaultValue = CoreCommonConstants.COMMON_FIELD_NAME_ID) final String sortField) {
		logger.debug("New Service Registry get request received with page: {} and item_per page: {}", page, size);

		int validatedPage;
		int validatedSize;
		if (page == null && size == null) {
			validatedPage = -1;
			validatedSize = -1;
		} else {
			if (page == null || size == null) {
				throw new BadPayloadException("Defined page or size could not be with undefined size or page.", HttpStatus.SC_BAD_REQUEST, CommonConstants.SERVICEREGISTRY_URI +
						CoreCommonConstants.MGMT_URI);
			} else {
				validatedPage = page;
				validatedSize = size;
			}
		}

		final Direction validatedDirection = CoreUtilities.calculateDirection(direction, CommonConstants.SERVICEREGISTRY_URI + CoreCommonConstants.MGMT_URI);
		final ServiceRegistryListResponseDTO serviceRegistryEntriesResponse = serviceRegistryDBService.getServiceRegistryEntriesResponse(validatedPage, validatedSize, validatedDirection, sortField);
		logger.debug("Service Registry entries with page: {} and item_per page: {} successfully retrieved", page, size);

		return serviceRegistryEntriesResponse;
	}

	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return requested service registry entry", response = ServiceRegistryResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = GET_SERVICEREGISTRY_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = GET_SERVICEREGISTRY_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path =  SERVICEREGISTRY_MGMT_BY_ID_URI)
	@ResponseBody public ServiceRegistryResponseDTO getServiceRegistryEntryById(@PathVariable(value = PATH_VARIABLE_ID) final long id) {
		logger.debug("New Service Registry get request received with id: {}", id);

		if (id < 1) {
			throw new BadPayloadException(ID_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, CommonConstants.SERVICEREGISTRY_URI + SERVICEREGISTRY_MGMT_BY_ID_URI);
		}
		final ServiceRegistryResponseDTO serviceRegistryEntryByIdResponse = serviceRegistryDBService.getServiceRegistryEntryByIdResponse(id);
		logger.debug("Service Registry entry with id: {} successfully retrieved", id);

		return serviceRegistryEntryByIdResponse;
	}

	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return requested service registry entries by service definition based on the given parameters", response = ServiceRegistryListResponseDTO.class,
			tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = GET_SERVICEREGISTRY_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = GET_SERVICEREGISTRY_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path =  SERVICEREGISTRY_MGMT_BY_SERVICE_DEFINITION_URI)
	@ResponseBody public ServiceRegistryListResponseDTO getServiceRegistryEntriesByServiceDefinition(
			@PathVariable(value = PATH_VARIABLE_SERVICE_DEFINITION) final String serviceDefinition,
			@RequestParam(name = CoreCommonConstants.REQUEST_PARAM_PAGE, required = false) final Integer page,
			@RequestParam(name = CoreCommonConstants.REQUEST_PARAM_ITEM_PER_PAGE, required = false) final Integer size,
			@RequestParam(name = CoreCommonConstants.REQUEST_PARAM_DIRECTION, defaultValue = CoreDefaults.DEFAULT_REQUEST_PARAM_DIRECTION_VALUE) final String direction,
			@RequestParam(name = CoreCommonConstants.REQUEST_PARAM_SORT_FIELD, defaultValue = CoreCommonConstants.COMMON_FIELD_NAME_ID) final String sortField) {
		logger.debug("New Service Registry get by Service Definition request received with page: {} and item_per page: {}", page, size);

		if (Utilities.isEmpty(serviceDefinition)) {
			throw new BadPayloadException("Service definition cannot be empty.", HttpStatus.SC_BAD_REQUEST, CommonConstants.SERVICEREGISTRY_URI + SERVICEREGISTRY_MGMT_BY_SERVICE_DEFINITION_URI);
		}

		int validatedPage;
		int validatedSize;
		if (page == null && size == null) {
			validatedPage = -1;
			validatedSize = -1;
		} else {
			if (page == null || size == null) {
				throw new BadPayloadException("Defined page or size could not be with undefined size or page.", HttpStatus.SC_BAD_REQUEST, CommonConstants.SERVICEREGISTRY_URI +
											  SERVICEREGISTRY_MGMT_BY_SERVICE_DEFINITION_URI);
			} else {
				validatedPage = page;
				validatedSize = size;
			}
		}

		final Direction validatedDirection = CoreUtilities.calculateDirection(direction, CommonConstants.SERVICEREGISTRY_URI + SERVICEREGISTRY_MGMT_BY_SERVICE_DEFINITION_URI);
		final ServiceRegistryListResponseDTO serviceRegistryEntries = serviceRegistryDBService.getServiceRegistryEntriesByServiceDefinitionResponse(serviceDefinition, validatedPage, validatedSize,
				validatedDirection, sortField);
		logger.debug("Service Registry entries with page: {} and item_per page: {} successfully retrieved", page, size);

		return serviceRegistryEntries;
	}

	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return all service registry entries grouped for frontend usage", response = ServiceRegistryGroupedResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = GET_SERVICEREGISTRY_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = GET_SERVICEREGISTRY_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path = SERVICEREGISTRY_MGMT_GROUPED_URI)
	@ResponseBody public ServiceRegistryGroupedResponseDTO getServiceRegistryGroupedData() {
		logger.debug("New get request for grouped service registry data");

		final ServiceRegistryGroupedResponseDTO serviceRegistryGroupedResponseDTO = serviceRegistryDBService.getServiceRegistryDataForServiceRegistryGroupedResponse();
		logger.debug("Grouped service registry data successfully retrieved");

		return serviceRegistryGroupedResponseDTO;
	}

	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Remove the specified service registry entry", tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = DELETE_SERVICEREGISTRY_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = DELETE_SERVICEREGISTRY_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@DeleteMapping(path = SERVICEREGISTRY_MGMT_BY_ID_URI)
	public void removeServiceRegistryEntryById(@PathVariable(value = PATH_VARIABLE_ID) final long id) {
		logger.debug("New Service Registry delete request received with id: {}", id);

		if (id < 1) {
			throw new BadPayloadException(ID_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, CommonConstants.SERVICEREGISTRY_URI + SERVICEREGISTRY_MGMT_BY_ID_URI);
		}

		serviceRegistryDBService.removeServiceRegistryEntryById(id);
		logger.debug("Service Registry with id: '{}' successfully deleted", id);
	}

	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = SERVICEREGISTRY_REGISTER_DESCRIPTION, response = ServiceRegistryResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_CLIENT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_CREATED, message = SERVICEREGISTRY_REGISTER_201_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = SERVICEREGISTRY_REGISTER_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@ResponseStatus(value = org.springframework.http.HttpStatus.CREATED)
	@PostMapping(path = CommonConstants.OP_SERVICEREGISTRY_REGISTER_URI, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public ServiceRegistryResponseDTO registerService(final HttpServletRequest servletRequest, @RequestBody final ServiceRegistryRequestDTO dto) {
		logger.debug("New service registration request received");
		checkServiceRegistryRequest(servletRequest, dto, false);

		final ServiceRegistryResponseDTO response = serviceRegistryDBService.registerServiceResponse(dto);
		logger.debug("{} successfully registers its service {}", dto.getProviderSystem().getSystemName(), dto.getServiceDefinition());

		return response;
	}

	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = SERVICEREGISTRY_REGISTER_DESCRIPTION, response = ServiceRegistryResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_CREATED, message = SERVICEREGISTRY_REGISTER_201_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = SERVICEREGISTRY_REGISTER_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@ResponseStatus(value = org.springframework.http.HttpStatus.CREATED)
	@PostMapping(path = CoreCommonConstants.MGMT_URI, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ServiceRegistryResponseDTO addServiceRegistry(@RequestBody final ServiceRegistryRequestDTO request) {
		logger.debug("New service registration request received");
		checkServiceRegistryRequest(null, request, CoreCommonConstants.MGMT_URI, true);

		final ServiceRegistryResponseDTO response = serviceRegistryDBService.registerServiceResponse(request);
		logger.debug("{}'s service {} is successfully registered", request.getProviderSystem().getSystemName(), request.getServiceDefinition());

		return response;
	}

	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = SERVICEREGISTRY_UPDATE_DESCRIPTION, response = ServiceRegistryResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = SERVICEREGISTRY_UPDATE_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = SERVICEREGISTRY_UPDATE_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@PutMapping(path = SERVICEREGISTRY_MGMT_BY_ID_URI, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ServiceRegistryResponseDTO updateServiceRegistry(@PathVariable(value = PATH_VARIABLE_ID) final long id, @RequestBody final ServiceRegistryRequestDTO request) {
		logger.debug("New service registry update request received");
		checkServiceRegistryUpdateRequest(id, request, CoreCommonConstants.MGMT_URI);

		final ServiceRegistryResponseDTO response = serviceRegistryDBService.updateServiceByIdResponse(id, request);
		logger.debug("Service Registry entry {} is successfully updated with system {} and service {}", id, request.getProviderSystem().getSystemName(), request.getServiceDefinition());

		return response;
	}

	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = SERVICEREGISTRY_MERGE_DESCRIPTION, response = ServiceRegistryResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = SERVICEREGISTRY_MERGE_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = SERVICEREGISTRY_MERGE_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@PatchMapping(path = SERVICEREGISTRY_MGMT_BY_ID_URI, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ServiceRegistryResponseDTO mergeServiceRegistry(@PathVariable(value = PATH_VARIABLE_ID) final long id, @RequestBody final ServiceRegistryRequestDTO request) {
		logger.debug("New service registry merge request received");
		checkServiceRegistryMergeRequest(id, request, CoreCommonConstants.MGMT_URI);

		final ServiceRegistryResponseDTO response = serviceRegistryDBService.mergeServiceByIdResponse(id, request);
		logger.debug("Service Registry entry {} is successfully merged witch system {} and service {}", id, response.getProvider().getSystemName(), request.getServiceDefinition());

		return response;
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = SERVICEREGISTRY_UNREGISTER_DESCRIPTION, tags = { CoreCommonConstants.SWAGGER_TAG_CLIENT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = SERVICEREGISTRY_UNREGISTER_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = SERVICEREGISTRY_UNREGISTER_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@DeleteMapping(path = CommonConstants.OP_SERVICEREGISTRY_UNREGISTER_URI)
	public void unregisterService(final HttpServletRequest servletRequest, 
								  @RequestParam(CommonConstants.OP_SERVICEREGISTRY_UNREGISTER_REQUEST_PARAM_SERVICE_DEFINITION) final String serviceDefinition,
								  @RequestParam(CommonConstants.OP_SERVICEREGISTRY_UNREGISTER_REQUEST_PARAM_SYSTEM_NAME) final String providerName,
								  @RequestParam(name = CommonConstants.OP_SERVICEREGISTRY_UNREGISTER_REQUEST_PARAM_ADDRESS, required = false) final String providerAddress,
								  @RequestParam(CommonConstants.OP_SERVICEREGISTRY_UNREGISTER_REQUEST_PARAM_PORT) final int providerPort,
								  @RequestParam(CommonConstants.OP_SERVICEREGISTRY_UNREGISTER_REQUEST_PARAM_SERVICE_URI) final String serviceUri) {
		logger.debug("Service removal request received");
		final String checkedAddress = checkUnregisterServiceParameters(servletRequest, serviceDefinition, providerName, providerAddress, providerPort);

		serviceRegistryDBService.removeServiceRegistry(serviceDefinition, providerName, checkedAddress, providerPort, serviceUri);
		logger.debug("{} successfully removed its service {} ({})", providerName, serviceDefinition, serviceUri);
	}

	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = SERVICEREGISTRY_QUERY_DESCRIPTION, response = ServiceQueryResultDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_CLIENT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = SERVICEREGISTRY_QUERY_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = SERVICEREGISTRY_QUERY_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@PostMapping(path = CommonConstants.OP_SERVICEREGISTRY_QUERY_URI, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public ServiceQueryResultDTO queryRegistry(@RequestBody final ServiceQueryFormDTO form) {
		logger.debug("Service query request received");

		if (Utilities.isEmpty(form.getServiceDefinitionRequirement())) {
			throw new BadPayloadException("Service definition requirement is null or blank", HttpStatus.SC_BAD_REQUEST, CommonConstants.SERVICEREGISTRY_URI + CommonConstants.OP_SERVICEREGISTRY_QUERY_URI);
		}
		
		if (useStrictServiceDefinitionVerifier && !cnVerifier.isValid(form.getServiceDefinitionRequirement())) {
			throw new BadPayloadException(SERVICE_DEFINITION_REQUIREMENT_WRONG_FORMAT_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, CommonConstants.SERVICEREGISTRY_URI + CommonConstants.OP_SERVICEREGISTRY_QUERY_URI);
		}

		final ServiceQueryResultDTO result = serviceRegistryDBService.queryRegistry(form);
		logger.debug("Return {} providers for service {}", result.getServiceQueryData().size(), form.getServiceDefinitionRequirement());

		return result;
	}

	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = SERVICEREGISTRY_QUERY_DESCRIPTION, response = ServiceQueryResultDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_CLIENT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = SERVICEREGISTRY_QUERY_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = SERVICEREGISTRY_QUERY_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@PostMapping(path = CoreCommonConstants.OP_SERVICEREGISTRY_MULTI_QUERY_URI, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public ServiceQueryResultListDTO multiQueryRegistry(@RequestBody final ServiceQueryFormListDTO forms) { 
		logger.debug("Service multi query request received");
		
		checkQueryFormList(forms);
		
		final ServiceQueryResultListDTO result = serviceRegistryDBService.multiQueryRegistry(forms);
		logger.debug("Return providers for multiple services");
		
		return result;
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = SERVICEREGISTRY_QUERY_BY_SYSTEM_ID_DESCRIPTION, response = SystemResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_PRIVATE })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = SERVICEREGISTRY_QUERY_BY_SYSTEM_ID_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = SERVICEREGISTRY_QUERY_BY_SYSTEM_ID_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path = CoreCommonConstants.OP_SERVICEREGISTRY_QUERY_BY_SYSTEM_ID_URI, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public SystemResponseDTO queryRegistryBySystemId(@PathVariable(value = PATH_VARIABLE_ID) final long systemId) {
		logger.debug("Service query by system id request received");

		if (systemId < 1) {
			throw new BadPayloadException(ID_NOT_VALID_ERROR_MESSAGE , HttpStatus.SC_BAD_REQUEST, CommonConstants.SERVICEREGISTRY_URI +
										  CoreCommonConstants.OP_SERVICEREGISTRY_QUERY_BY_SYSTEM_ID_URI);
		}

		final SystemResponseDTO result = serviceRegistryDBService.getSystemById(systemId);

		logger.debug("Return system by id: {}", systemId);
		return result;
	}

	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = SERVICEREGISTRY_QUERY_BY_SYSTEM_DTO_DESCRIPTION, response = SystemResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_PRIVATE })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = SERVICEREGISTRY_QUERY_BY_SYSTEM_DTO_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = SERVICEREGISTRY_QUERY_BY_SYSTEM_DTO_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@PostMapping(path = CoreCommonConstants.OP_SERVICEREGISTRY_QUERY_BY_SYSTEM_DTO_URI, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public SystemResponseDTO queryRegistryBySystemDTO(@RequestBody final SystemRequestDTO request) {
		logger.debug("Service query by systemRequestDTO request received");

		checkSystemRequest(null, request, CommonConstants.SERVICEREGISTRY_URI + CoreCommonConstants.OP_SERVICEREGISTRY_QUERY_BY_SYSTEM_DTO_URI, false);

		final String systemName = request.getSystemName();
		final String address = request.getAddress();
		final int port = request.getPort();

		final SystemResponseDTO result = serviceRegistryDBService.getSystemByNameAndAddressAndPortResponse(systemName, address, port);

		logger.debug("Return system by name: {}, address: {}, port: {}", systemName, address, port);
		return result;
	}

	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return all service registry entries", response = ServiceRegistryListResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_PRIVATE })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = GET_SERVICEREGISTRY_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = GET_SERVICEREGISTRY_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path = CoreCommonConstants.OP_SERVICEREGISTRY_QUERY_ALL_SERVICE_URI)
	@ResponseBody public ServiceRegistryListResponseDTO getServiceRegistryEntries() {
		logger.debug("New Service Registry get request recieved");

		final int page = 0;
		final int size = Integer.MAX_VALUE;
		final Direction direction = Direction.ASC;
		final String sortField = CommonConstants.COMMON_FIELD_NAME_ID;

		final ServiceRegistryListResponseDTO serviceRegistryEntriesResponse = serviceRegistryDBService.getServiceRegistryEntriesResponse(page, size, direction, sortField);
		logger.debug("Service Registry entries with page: {} and item_per page: {} successfully retrieved", page, size);

		return serviceRegistryEntriesResponse;
	}

	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return requested service interfaces by the given parameters", response = ServiceInterfacesListResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = GET_SERVICE_INTERFACES_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = GET_SERVICE_INTERFACES_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path = SERVICE_INTERFACES_URI, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public ServiceInterfacesListResponseDTO getServiceInterfaces(
			@RequestParam(name = CoreCommonConstants.REQUEST_PARAM_PAGE, required = false) final Integer page,
			@RequestParam(name = CoreCommonConstants.REQUEST_PARAM_ITEM_PER_PAGE, required = false) final Integer size,
			@RequestParam(name = CoreCommonConstants.REQUEST_PARAM_DIRECTION, defaultValue = CoreDefaults.DEFAULT_REQUEST_PARAM_DIRECTION_VALUE) final String direction,
			@RequestParam(name = CoreCommonConstants.REQUEST_PARAM_SORT_FIELD, defaultValue = CoreCommonConstants.COMMON_FIELD_NAME_ID) final String sortField) {
		logger.debug("New Service Interface get request received with page: {} and item_per page: {}", page, size);

		int validatedPage;
		int validatedSize;
		if (page == null && size == null) {
			validatedPage = -1;
			validatedSize = -1;
		} else {
			if (page == null || size == null) {
				throw new BadPayloadException("Defined page or size could not be with undefined size or page.", HttpStatus.SC_BAD_REQUEST, CommonConstants.SERVICEREGISTRY_URI + SERVICE_INTERFACES_URI);
			} else {
				validatedPage = page;
				validatedSize = size;
			}
		}

		final Direction validatedDirection = CoreUtilities.calculateDirection(direction, CommonConstants.SERVICEREGISTRY_URI + SERVICE_INTERFACES_URI);
		final ServiceInterfacesListResponseDTO serviceInterfaceEntries = serviceRegistryDBService.getServiceInterfaceEntriesResponse(validatedPage, validatedSize, validatedDirection, sortField);
		logger.debug("Service interface  with page: {} and item_per page: {} successfully retrieved", page, size);

		return serviceInterfaceEntries;
	}

	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return requested service interface", response = ServiceInterfaceResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = GET_SERVICE_INTERFACES_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = GET_SERVICE_INTERFACES_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path = SERVICE_INTERFACES_BY_ID_URI, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public ServiceInterfaceResponseDTO  getServiceInterfaceById(@PathVariable(value = PATH_VARIABLE_ID) final long id) {
		logger.debug("New Service Interface get request received with id: {}", id);

		if (id < 1) {
			throw new BadPayloadException(ID_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, CommonConstants.SERVICEREGISTRY_URI + SERVICE_INTERFACES_BY_ID_URI);
		}

		final ServiceInterfaceResponseDTO serviceInterfaceEntry = serviceRegistryDBService.getServiceInterfaceByIdResponse(id);
		logger.debug("Service interface with id: '{}' successfully retrieved", id);

		return serviceInterfaceEntry;
	}

	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return created service interface", response = ServiceInterfaceResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_CREATED, message = POST_SERVICE_INTERFACES_HTTP_201_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = POST_SERVICE_INTERFACES_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@PostMapping(path = SERVICE_INTERFACES_URI, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(value = org.springframework.http.HttpStatus.CREATED)
	@ResponseBody public ServiceInterfaceResponseDTO addServiceInterface(@RequestBody final ServiceInterfaceRequestDTO serviceInterfaceRequestDTO) {
		final String interfaceName = serviceInterfaceRequestDTO.getInterfaceName();
		logger.debug("New Service Interface registration request received with interface: {}", interfaceName);

		if (Utilities.isEmpty(interfaceName)) {
			throw new BadPayloadException("Service interface is null or blank", HttpStatus.SC_BAD_REQUEST, CommonConstants.SERVICEREGISTRY_URI + SERVICE_INTERFACES_URI);
		}

		if (!interfaceNameVerifier.isValid(interfaceName)) {
			throw new BadPayloadException("Specified interface name is not valid: " + interfaceName, HttpStatus.SC_BAD_REQUEST, CommonConstants.SERVICEREGISTRY_URI + SERVICE_INTERFACES_URI);
		}

		final ServiceInterfaceResponseDTO serviceInterfaceResponse = serviceRegistryDBService.createServiceInterfaceResponse(interfaceName);
		logger.debug("{} service interface successfully registered.", interfaceName);

		return serviceInterfaceResponse;
	}

	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return updated service interface", response = ServiceInterfaceResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = PUT_SERVICE_INTERFACES_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = PUT_SERVICE_INTERFACES_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@PutMapping(path = SERVICE_INTERFACES_BY_ID_URI, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public ServiceInterfaceResponseDTO putUpdateServiceInterface(@PathVariable(value = PATH_VARIABLE_ID) final long id,
																			   @RequestBody final ServiceInterfaceRequestDTO serviceInterfaceRequestDTO) {
		final String interfaceName = serviceInterfaceRequestDTO.getInterfaceName();
		logger.debug("New Service Interface update request received with id: {}, interface: {}", id, interfaceName);

		if (id < 1) {
			throw new BadPayloadException(ID_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, CommonConstants.SERVICEREGISTRY_URI + SERVICE_INTERFACES_BY_ID_URI);
		}

		if (Utilities.isEmpty(interfaceName)) {
			throw new BadPayloadException("serviceInterface is null or blank", HttpStatus.SC_BAD_REQUEST, CommonConstants.SERVICEREGISTRY_URI + SERVICE_INTERFACES_BY_ID_URI);
		}

		if (!interfaceNameVerifier.isValid(interfaceName)) {
			throw new BadPayloadException("Specified interface name is not valid: " + interfaceName, HttpStatus.SC_BAD_REQUEST, CommonConstants.SERVICEREGISTRY_URI + SERVICE_INTERFACES_URI);
		}

		final ServiceInterfaceResponseDTO serviceInterfaceResponse = serviceRegistryDBService.updateServiceInterfaceByIdResponse(id, interfaceName);
		logger.debug("Service interface with id: '{}' successfully updated with interface '{}'.", id, interfaceName);

		return serviceInterfaceResponse;
	}

	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return updated service interface", response = ServiceInterfaceResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = PATCH_SERVICE_INTERFACES_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = PATCH_SERVICE_INTERFACES_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@PatchMapping(path = SERVICE_INTERFACES_BY_ID_URI, consumes = MediaType.APPLICATION_JSON_VALUE, produces = "application/json")
	@ResponseBody public ServiceInterfaceResponseDTO patchUpdateServiceInterface(@PathVariable(value = PATH_VARIABLE_ID) final long id,
																				 @RequestBody final ServiceInterfaceRequestDTO serviceInterfaceRequestDTO) {
		// Currently ServiceInterface has only one updateable field, therefore PUT and PATCH do the same
		return putUpdateServiceInterface(id, serviceInterfaceRequestDTO);
	}

	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Remove service interface", tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = DELETE_SERVICE_INTERFACES_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = DELETE_SERVICE_INTERFACES_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@DeleteMapping(path = SERVICE_INTERFACES_BY_ID_URI)
	public void removeServiceInterface(@PathVariable(value = PATH_VARIABLE_ID) final long id) {
		logger.debug("New Service Interface delete request received with id: {}", id);

		if (id < 1) {
			throw new BadPayloadException(ID_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, CommonConstants.SERVICEREGISTRY_URI + SERVICE_INTERFACES_BY_ID_URI);
		}

		serviceRegistryDBService.removeServiceInterfaceById(id);
		logger.debug("Service interface with id: '{}' successfully deleted", id);
	}

	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return service registry entries by system id", response = ServiceRegistryListResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_PRIVATE })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = GET_SERVICEREGISTRY_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = GET_SERVICEREGISTRY_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path = CoreCommonConstants.OP_SERVICE_REGISTRY_QUERY_SERVICES_BY_SYSTEM_ID_URI, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public ServiceRegistryListResponseDTO getServiceRegistryEntriesBySystemId(@PathVariable(value = PATH_VARIABLE_ID) final long systemId) {
		logger.debug("Service query by system id '{}' request received", systemId);

		if (systemId < 1) {
			throw new BadPayloadException(ID_NOT_VALID_ERROR_MESSAGE , HttpStatus.SC_BAD_REQUEST, CommonConstants.SERVICEREGISTRY_URI +
					CoreCommonConstants.OP_SERVICE_REGISTRY_QUERY_SERVICES_BY_SYSTEM_ID_URI);
		}

		final ServiceRegistryListResponseDTO response = serviceRegistryDBService.getServiceRegistryEntriesBySystemIdResponse(systemId);

		logger.debug("Service Registry Entries by system id are successfully retrieved");
		return response;
	}

	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return service registry entries by service definition list", response = ServiceRegistryListResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_PRIVATE })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = GET_SERVICEREGISTRY_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = GET_SERVICEREGISTRY_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@PostMapping(path = CoreCommonConstants.OP_SERVICE_REGISTRY_QUERY_SERVICES_BY_SERVICE_DEFINITION_LIST_URI, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public ServiceRegistryListResponseDTO queryServiceRegistryEntriesByServiceDefinitionList(@RequestBody final List<String> request) {
		logger.debug("Service query by service definition list request received");
		validateServiceDefinitionListRequest(request, CommonConstants.SERVICEREGISTRY_URI + CoreCommonConstants.OP_SERVICE_REGISTRY_QUERY_SERVICES_BY_SERVICE_DEFINITION_LIST_URI);

		final ServiceRegistryListResponseDTO response = serviceRegistryDBService.getServiceRegistryEntriesByServiceDefinitonListResponse(request);
		logger.debug("Service Registry Entries by service definition list are successfully retrieved");
		return response;
	}


	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	private SystemResponseDTO callCreateSystem(final HttpServletRequest servletRequest, final SystemRequestDTO dto, final String origin) {
		logger.debug("callCreateSystem started...");

		checkSystemRequest(servletRequest, dto, origin, true);

		final String systemName = dto.getSystemName().toLowerCase().trim();
		final String address = dto.getAddress().toLowerCase().trim();
		final int port = dto.getPort();
		final String authenticationInfo = dto.getAuthenticationInfo();
		final Map<String,String> metadata = dto.getMetadata();

		return serviceRegistryDBService.createSystemResponse(systemName, address, port, authenticationInfo, metadata);
	}

	//-------------------------------------------------------------------------------------------------
	private SystemResponseDTO callUpdateSystem(final SystemRequestDTO dto, final long systemId) {
		logger.debug("callUpdateSystem started...");

		checkSystemPutRequest(dto, systemId);

		final String validatedSystemName = dto.getSystemName().toLowerCase().trim();
		final String validatedAddress = dto.getAddress().toLowerCase().trim();
		final int validatedPort = dto.getPort();
		final String validatedAuthenticationInfo = dto.getAuthenticationInfo();
		final Map<String,String> metadata = dto.getMetadata();

		return serviceRegistryDBService.updateSystemResponse(systemId, validatedSystemName, validatedAddress, validatedPort, validatedAuthenticationInfo, metadata);
	}

	//-------------------------------------------------------------------------------------------------
	private SystemResponseDTO callMergeSystem(final SystemRequestDTO request, final long systemId) {
		logger.debug("callMergeSystem started...");

		checkSystemMergeRequest(request, systemId);

		final String validatedSystemName = request.getSystemName() != null ? request.getSystemName().toLowerCase() : "";
		final String validatedAddress = request.getAddress() != null ? request.getAddress().toLowerCase().toLowerCase().trim() : "";
		final Integer validatedPort = request.getPort();
		final String validatedAuthenticationInfo = request.getAuthenticationInfo();
		final Map<String,String> metadata = request.getMetadata();

		return serviceRegistryDBService.mergeSystemResponse(systemId, validatedSystemName, validatedAddress, validatedPort, validatedAuthenticationInfo, metadata);
	}

	//-------------------------------------------------------------------------------------------------
	private void checkSystemMergeRequest(final SystemRequestDTO request, final long systemId) {
		logger.debug("checkSystemPatchRequest started...");

		if (systemId <= 0) {
			throw new BadPayloadException(ID_NOT_VALID_ERROR_MESSAGE , HttpStatus.SC_BAD_REQUEST, CommonConstants.SERVICEREGISTRY_URI + SYSTEMS_BY_ID_URI);
		}

		boolean needChange = false;
		if (!Utilities.isEmpty(request.getAddress())) {
			needChange = true;
			
			try {			
				networkAddressVerifier.verify(networkAddressPreProcessor.normalize(request.getAddress()));
			} catch (final InvalidParameterException ex) {
				throw new BadPayloadException(ex.getMessage(), HttpStatus.SC_BAD_REQUEST, CommonConstants.SERVICEREGISTRY_URI + SYSTEMS_BY_ID_URI);
			}
		}		

		if (!Utilities.isEmpty(request.getSystemName())) {
			needChange = true;
			for (final CoreSystem coreSystem : CoreSystem.values()) {
				if (coreSystem.name().equalsIgnoreCase(request.getSystemName().trim())) {
					throw new BadPayloadException("System name '" + request.getSystemName() + "' is a reserved arrowhead core system name.", HttpStatus.SC_BAD_REQUEST, CommonConstants.SERVICEREGISTRY_URI + SYSTEMS_BY_ID_URI);
				}
			}
			
			if (!cnVerifier.isValid(request.getSystemName())) {
				throw new BadPayloadException(SYSTEM_NAME_WRONG_FORMAT_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, CommonConstants.SERVICEREGISTRY_URI + SYSTEMS_BY_ID_URI);
			}
		}

		if (request.getPort() != null) {
			final int validatedPort = request.getPort();
			if (validatedPort < CommonConstants.SYSTEM_PORT_RANGE_MIN || validatedPort > CommonConstants.SYSTEM_PORT_RANGE_MAX) {
				throw new BadPayloadException("Port must be between " + CommonConstants.SYSTEM_PORT_RANGE_MIN + " and " + CommonConstants.SYSTEM_PORT_RANGE_MAX +".", HttpStatus.SC_BAD_REQUEST,
											  CommonConstants.SERVICEREGISTRY_URI + SYSTEMS_BY_ID_URI);
			}

			needChange = true;
		}

		if (request.getAuthenticationInfo() != null) {
			needChange = true;
		}

		if (request.getMetadata() != null) {
			needChange = true;
		}
		
		if (!needChange) {
			throw new BadPayloadException("Patch request is empty." , HttpStatus.SC_BAD_REQUEST, CommonConstants.SERVICEREGISTRY_URI + SYSTEMS_BY_ID_URI);
		}
	}

	//-------------------------------------------------------------------------------------------------
	private void checkSystemPutRequest(final SystemRequestDTO dto, final long systemId) {
		logger.debug("checkSystemPutRequest started...");

		if (systemId <= 0) {
			throw new BadPayloadException(ID_NOT_VALID_ERROR_MESSAGE , HttpStatus.SC_BAD_REQUEST, CommonConstants.SERVICEREGISTRY_URI + SYSTEMS_BY_ID_URI);
		}

		checkSystemRequest(null, dto, CommonConstants.SERVICEREGISTRY_URI + SYSTEMS_BY_ID_URI, true);
	}

	//-------------------------------------------------------------------------------------------------
	private void checkSystemRequest(final HttpServletRequest servletRequest, final SystemRequestDTO dto, final String origin, final boolean checkReservedCoreSystemNames) {
		logger.debug("checkSystemRequest started...");

		if (dto == null) {
			throw new BadPayloadException("System is null.", HttpStatus.SC_BAD_REQUEST, origin);
		}

		if (Utilities.isEmpty(dto.getSystemName())) {
			throw new BadPayloadException(SYSTEM_NAME_NULL_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
		}

		if (checkReservedCoreSystemNames) {
			for (final CoreSystem coreSystem : CoreSystem.values()) {
				if (coreSystem.name().equalsIgnoreCase(dto.getSystemName().trim())) {
					throw new BadPayloadException("System name '" + dto.getSystemName() + "' is a reserved arrowhead core system name.", HttpStatus.SC_BAD_REQUEST, origin);
				}
			}
		}

		if (!cnVerifier.isValid(dto.getSystemName())) {
			throw new BadPayloadException(SYSTEM_NAME_WRONG_FORMAT_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
		}

		if (dto.getPort() == null) {
			throw new BadPayloadException(SYSTEM_PORT_NULL_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
		}

		final int validatedPort = dto.getPort();
		if (validatedPort < CommonConstants.SYSTEM_PORT_RANGE_MIN || validatedPort > CommonConstants.SYSTEM_PORT_RANGE_MAX) {
			throw new BadPayloadException("Port must be between " + CommonConstants.SYSTEM_PORT_RANGE_MIN + " and " + CommonConstants.SYSTEM_PORT_RANGE_MAX + ".", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		try {			
			networkAddressVerifier.verify(networkAddressPreProcessor.normalize(dto.getAddress()));
		} catch (final InvalidParameterException ex) {
			final AddressDetectionResult detectionResult = networkAddressDetector.detect(servletRequest);
			if (detectionResult.isSkipped()) {
				throw new BadPayloadException(ex.getMessage() + " " + detectionResult.getDetectionMessage(), HttpStatus.SC_BAD_REQUEST, origin);				
			}
			if (!detectionResult.isDetectionSuccess()) {
				throw new BadPayloadException(ex.getMessage() + " " + detectionResult.getDetectionMessage(), HttpStatus.SC_BAD_REQUEST, origin);
			} else {
				dto.setAddress(detectionResult.getDetectedAddress());
			}
		}
	}

	//-------------------------------------------------------------------------------------------------
	private void checkServiceRegistryRequest(final HttpServletRequest servletRequest, final ServiceRegistryRequestDTO dto, final boolean checkReservedCoreSystemNames) {
		logger.debug("checkServiceRegistryRequest started...");

		final String origin = CommonConstants.SERVICEREGISTRY_URI + CommonConstants.OP_SERVICEREGISTRY_REGISTER_URI;

		checkServiceRegistryRequest(servletRequest, dto, origin, checkReservedCoreSystemNames);
	}

	//-------------------------------------------------------------------------------------------------
	private void checkServiceRegistryRequest(final HttpServletRequest servlerRequest, final ServiceRegistryRequestDTO dto, final String origin, final boolean checkReservedCoreSystemNames) {
		logger.debug("checkServiceRegistryRequest started...");

		if (Utilities.isEmpty(dto.getServiceDefinition())) {
			throw new BadPayloadException("Service definition is null or blank", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		if (useStrictServiceDefinitionVerifier && !cnVerifier.isValid(dto.getServiceDefinition())) {
			throw new BadPayloadException(SERVICE_DEFINITION_WRONG_FORMAT_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
		}
	
		checkSystemRequest(servlerRequest, dto.getProviderSystem(), origin, checkReservedCoreSystemNames);
		
		if (dto.getServiceUri() == null) {
			dto.setServiceUri("");
		}
		
		if (!Utilities.isEmpty(dto.getEndOfValidity())) {
			try {
				Utilities.parseUTCStringToLocalZonedDateTime(dto.getEndOfValidity().trim());
			} catch (final DateTimeParseException ex) {
				throw new BadPayloadException("End of validity is specified in the wrong format. Please provide UTC time using ISO-8601 format.",
						HttpStatus.SC_BAD_REQUEST, origin);
			}
		}

		ServiceSecurityType securityType = null;
		if (dto.getSecure() != null) {
			for (final ServiceSecurityType type : ServiceSecurityType.values()) {
				if (type.name().equalsIgnoreCase(dto.getSecure())) {
					securityType = type;
					break;
				}
			}

			if (securityType == null) {
				throw new BadPayloadException("Security type is not valid.", HttpStatus.SC_BAD_REQUEST, origin);
			}
		} else {
			securityType = ServiceSecurityType.NOT_SECURE;
		}

		if (securityType != ServiceSecurityType.NOT_SECURE && dto.getProviderSystem().getAuthenticationInfo() == null) {
			throw new BadPayloadException("Security type is in conflict with the availability of the authentication info.", HttpStatus.SC_BAD_REQUEST, origin);
		}

		if (dto.getInterfaces() == null || dto.getInterfaces().isEmpty()) {
			throw new BadPayloadException("Interfaces list is null or empty.", HttpStatus.SC_BAD_REQUEST, origin);
		}

		for (final String intf : dto.getInterfaces()) {
			if (!interfaceNameVerifier.isValid(intf)) {
				throw new BadPayloadException("Specified interface name is not valid: " + intf, HttpStatus.SC_BAD_REQUEST, origin);
			}
		}
	}

	//-------------------------------------------------------------------------------------------------
	private String checkUnregisterSystemParameters(final HttpServletRequest servletRequest, final String systemName, final String address, final int port) {
		// parameters can't be null, but can be empty
		logger.debug("checkUnregisterSystemParameters started...");		
		
		final String origin = CommonConstants.SERVICEREGISTRY_URI + CommonConstants.OP_SERVICEREGISTRY_UNREGISTER_SYSTEM_URI;
		if (Utilities.isEmpty(systemName)) {
			throw new BadPayloadException("Name of the application system is blank", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		if (!cnVerifier.isValid(systemName)) {
			throw new BadPayloadException(SYSTEM_NAME_WRONG_FORMAT_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
		}

		if (port < CommonConstants.SYSTEM_PORT_RANGE_MIN || port > CommonConstants.SYSTEM_PORT_RANGE_MAX) {
			throw new BadPayloadException("Port must be between " + CommonConstants.SYSTEM_PORT_RANGE_MIN + " and " + CommonConstants.SYSTEM_PORT_RANGE_MAX + ".", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		String checkedAddress = address;
		try {
			networkAddressVerifier.verify(networkAddressPreProcessor.normalize(checkedAddress));
		} catch (final InvalidParameterException ex) {
			final AddressDetectionResult detectionResult = networkAddressDetector.detect(servletRequest);
			if (detectionResult.isSkipped()) {
				throw new BadPayloadException(ex.getMessage() + " " + detectionResult.getDetectionMessage(), HttpStatus.SC_BAD_REQUEST, origin);				
			}
			if (!detectionResult.isDetectionSuccess()) {
				throw new BadPayloadException(ex.getMessage() + " " + detectionResult.getDetectionMessage(), HttpStatus.SC_BAD_REQUEST, origin);
			} else {
				checkedAddress = detectionResult.getDetectedAddress();
			}
		}
		
		return checkedAddress;
	}
	
	//-------------------------------------------------------------------------------------------------
	private String checkUnregisterServiceParameters(final HttpServletRequest servletRequest, final String serviceDefinition, final String providerName, final String providerAddress, final int providerPort) {
		// parameters can't be null, but can be empty
		logger.debug("checkUnregisterServiceParameters started...");

		final String origin = CommonConstants.SERVICEREGISTRY_URI + CommonConstants.OP_SERVICEREGISTRY_UNREGISTER_URI;
		if (Utilities.isEmpty(serviceDefinition)) {
			throw new BadPayloadException("Service definition is blank", HttpStatus.SC_BAD_REQUEST, origin);
		}

		if (useStrictServiceDefinitionVerifier && !cnVerifier.isValid(serviceDefinition)) {
			throw new BadPayloadException(SERVICE_DEFINITION_WRONG_FORMAT_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		if (Utilities.isEmpty(providerName)) {
			throw new BadPayloadException("Name of the provider system is blank", HttpStatus.SC_BAD_REQUEST, origin);
		}

		if (!cnVerifier.isValid(providerName)) {
			throw new BadPayloadException(SYSTEM_NAME_WRONG_FORMAT_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
		}

		if (providerPort < CommonConstants.SYSTEM_PORT_RANGE_MIN || providerPort > CommonConstants.SYSTEM_PORT_RANGE_MAX) {
			throw new BadPayloadException("Port must be between " + CommonConstants.SYSTEM_PORT_RANGE_MIN + " and " + CommonConstants.SYSTEM_PORT_RANGE_MAX + ".", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		String checkedAddress = providerAddress;
		try {
			networkAddressVerifier.verify(networkAddressPreProcessor.normalize(checkedAddress));
		} catch (final InvalidParameterException ex) {
			final AddressDetectionResult detectionResult = networkAddressDetector.detect(servletRequest);
			if (detectionResult.isSkipped()) {
				throw new BadPayloadException(ex.getMessage() + " " + detectionResult.getDetectionMessage(), HttpStatus.SC_BAD_REQUEST, origin);				
			}
			if (!detectionResult.isDetectionSuccess()) {
				throw new BadPayloadException(ex.getMessage() + " " + detectionResult.getDetectionMessage(), HttpStatus.SC_BAD_REQUEST, origin);	
			} else {
				checkedAddress = detectionResult.getDetectedAddress();
			}
		}		
		
		return checkedAddress;
	}

	//-------------------------------------------------------------------------------------------------
	private void checkServiceRegistryUpdateRequest(final long id, final ServiceRegistryRequestDTO request, final String origin) {
		logger.debug("checkServiceRegistryUpdateRequest started...");

		if (id <= 0) {
			throw new BadPayloadException(ID_NOT_VALID_ERROR_MESSAGE , HttpStatus.SC_BAD_REQUEST, origin);
		}

		checkServiceRegistryRequest(null, request, origin, true);

	}

	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S3776")
	private void checkServiceRegistryMergeRequest(final long id, final ServiceRegistryRequestDTO request, final String origin) {
		logger.debug("checkServiceRegistryMergeRequest started...");

		if (id <= 0) {
			throw new BadPayloadException(ID_NOT_VALID_ERROR_MESSAGE , HttpStatus.SC_BAD_REQUEST, origin);
		}

		boolean needChange = false;
		if (request.getProviderSystem() != null && !Utilities.isEmpty(request.getProviderSystem().getAddress())) {
			needChange = true;
			
			try {
				networkAddressVerifier.verify(networkAddressPreProcessor.normalize(request.getProviderSystem().getAddress()));
			} catch (final Exception ex) {
				throw new BadPayloadException(ex.getMessage(), HttpStatus.SC_BAD_REQUEST, origin);
			}
		}

		if (request.getProviderSystem() != null && !Utilities.isEmpty(request.getProviderSystem().getSystemName())) {
			if (!cnVerifier.isValid(request.getProviderSystem().getSystemName())) {
				throw new BadPayloadException(SYSTEM_NAME_WRONG_FORMAT_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
			}
			
			needChange = true;
		}

		if (request.getProviderSystem() != null && request.getProviderSystem().getPort() != null) {
			final int validatedPort = request.getProviderSystem().getPort();
			if (validatedPort < CommonConstants.SYSTEM_PORT_RANGE_MIN || validatedPort > CommonConstants.SYSTEM_PORT_RANGE_MAX) {
				throw new BadPayloadException("Port must be between " + CommonConstants.SYSTEM_PORT_RANGE_MIN + " and " + CommonConstants.SYSTEM_PORT_RANGE_MAX +".", HttpStatus.SC_BAD_REQUEST,
						origin);
			}

			needChange = true;
		}

		if (request.getProviderSystem() != null && request.getProviderSystem().getAuthenticationInfo() != null) {
			needChange = true;
		}

		if (request.getProviderSystem() != null && request.getProviderSystem().getMetadata() != null) {
			needChange = true;
		}
		
		if (request.getEndOfValidity() != null) {
			needChange = true;
		}

		if (request.getMetadata() != null) {
			needChange = true;
		}

		if (request.getInterfaces() != null) {
			needChange = true;
		}

		if (request.getSecure() != null) {
			needChange = true;
		}

		if (request.getServiceDefinition() != null) {
			if (useStrictServiceDefinitionVerifier && !cnVerifier.isValid(request.getServiceDefinition())) {
				throw new BadPayloadException(SERVICE_DEFINITION_WRONG_FORMAT_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
			}
			
			needChange = true;
		}

		if (request.getServiceUri() != null) {
			needChange = true;
		}

		if (request.getVersion() != null) {
			needChange = true;
		}

		if (!needChange) {
			throw new BadPayloadException("Patch request is empty." , HttpStatus.SC_BAD_REQUEST, origin);
		}
	}

	//-------------------------------------------------------------------------------------------------
	private void validateServiceDefinitionListRequest(final List<String> request, final String origin) {
		logger.debug("checkServiceDefinitionListRequest started...");

		if (request == null) {
			throw new BadPayloadException("Service definition list is null.", HttpStatus.SC_BAD_REQUEST, origin);
		}

		for (final String def : request) {
			if (Utilities.isEmpty(def)) {
				throw new BadPayloadException("Service definition is null or empty", HttpStatus.SC_BAD_REQUEST, origin);
			}
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private void checkQueryFormList(final ServiceQueryFormListDTO forms) {
		logger.debug("checkQueryFormList started...");
		
		if (forms == null || forms.getForms() == null || forms.getForms().isEmpty()) {
			throw new BadPayloadException("Form list is null", HttpStatus.SC_BAD_REQUEST, CommonConstants.SERVICEREGISTRY_URI + CoreCommonConstants.OP_SERVICEREGISTRY_MULTI_QUERY_URI);
		}
		
		for (final ServiceQueryFormDTO form : forms.getForms()) {
			if (form == null) {
				throw new BadPayloadException("A form is null", HttpStatus.SC_BAD_REQUEST, CommonConstants.SERVICEREGISTRY_URI + CoreCommonConstants.OP_SERVICEREGISTRY_MULTI_QUERY_URI);
			}
			
			if (Utilities.isEmpty(form.getServiceDefinitionRequirement())) {
				throw new BadPayloadException("Service definition requirement is null or blank", HttpStatus.SC_BAD_REQUEST, CommonConstants.SERVICEREGISTRY_URI + CoreCommonConstants.OP_SERVICEREGISTRY_MULTI_QUERY_URI);
			}
			
			if (useStrictServiceDefinitionVerifier && !cnVerifier.isValid(form.getServiceDefinitionRequirement())) {
				throw new BadPayloadException(SERVICE_DEFINITION_REQUIREMENT_WRONG_FORMAT_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, CommonConstants.SERVICEREGISTRY_URI + CoreCommonConstants.OP_SERVICEREGISTRY_MULTI_QUERY_URI);
			}
		}
	}
}