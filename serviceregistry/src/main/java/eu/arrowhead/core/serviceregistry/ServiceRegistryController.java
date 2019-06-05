package eu.arrowhead.core.serviceregistry;

import java.util.NoSuchElementException;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.ResponseEntity;
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
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.entity.ServiceDefinition;
import eu.arrowhead.common.database.entity.System;
import eu.arrowhead.common.database.service.ServiceRegistryDBService;
import eu.arrowhead.common.dto.DTOConverter;
import eu.arrowhead.common.dto.ServiceDefinitionRequestDTO;
import eu.arrowhead.common.dto.ServiceDefinitionResponseDTO;
import eu.arrowhead.common.dto.ServiceDefinitionsListResponseDTO;
import eu.arrowhead.common.dto.SystemListResponseDTO;
import eu.arrowhead.common.dto.SystemRequestDTO;
import eu.arrowhead.common.dto.SystemResponseDTO;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.common.exception.DataNotFoundException;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping(CommonConstants.SERVICEREGISTRY_URI)
public class ServiceRegistryController {

	//=================================================================================================
	// members
	
	private static final String ECHO_URI = "/echo";
	
	private static final String GET_SYSTEM_BY_ID_HTTP_200_MESSAGE = "System by requested id returned";
	private static final String GET_SYSTEM_BY_ID_HTTP_400_MESSAGE = "No Such System by requested id";
	private static final String GET_SYSTEM_BY_ID_HTTP_417_MESSAGE = "Not a valid System id";
	private static final String SYSTEM_BY_ID_PATH_VARIABLE = "id";
	private static final String SYSTEM_BY_ID_URI = CommonConstants.MGMT_URI+"/system/{" + SYSTEM_BY_ID_PATH_VARIABLE + "}";
	private static final String SYSTEMS_URI = CommonConstants.MGMT_URI+"/systems";
	private static final String SYSTEMS_BY_ID_URI = CommonConstants.MGMT_URI+"/systems/{" + SYSTEM_BY_ID_PATH_VARIABLE + "}";
	private static final String GET_SYSTEMS_HTTP_200_MESSAGE = "Systems returned";
	private static final String GET_SYSTEMS_HTTP_400_MESSAGE = " Invalid paraameters";
	private static final String GET_SYSTEMS_HTTP_417_MESSAGE = "Not valid request parameters";
	private static final String POST_SYSTEM_HTTP_201_MESSAGE = "System created";
	private static final String POST_SYSTEM_HTTP_400_MESSAGE = "Could not create system";
	private static final String POST_SYSTEM_HTTP_417_MESSAGE = "Not valid request parameters";
	private static final String PUT_SYSTEM_HTTP_200_MESSAGE = "System updated";
	private static final String PUT_SYSTEM_HTTP_400_MESSAGE = "Could not update system";
	private static final String PUT_SYSTEM_HTTP_417_MESSAGE = "Not valid request parameters";
	private static final String PATCH_SYSTEM_HTTP_200_MESSAGE = "System updated";
	private static final String PATCH_SYSTEM_HTTP_400_MESSAGE = "Could not update system";
	private static final String PATCH_SYSTEM_HTTP_417_MESSAGE = "Not valid request parameters";
	private static final String DELETE_SYSTEM_HTTP_200_MESSAGE = "System deleted";
	private static final String DELETE_SYSTEM_HTTP_400_MESSAGE = "Could not delete system";
	
	private static final String SERVICES_URI = CommonConstants.MGMT_URI + "/services";
	private static final String SERVICES_BY_ID_PATH_VARIABLE = "id";
	private static final String SERVICES_BY_ID_URI = SERVICES_URI + "/{" + SERVICES_BY_ID_PATH_VARIABLE + "}";
	private static final String GET_SERVICES_HTTP_200_MESSAGE = "Services returned";
	private static final String GET_SERVICES_HTTP_400_MESSAGE = "Could not retrive service definition";
	private static final String GET_SERVICES_HTTP_404_MESSAGE = "Service definition with given parameters not exists";
	private static final String POST_SERVICES_HTTP_201_MESSAGE = "Service definition created";
	private static final String POST_SERVICES_HTTP_400_MESSAGE = "Could not create service definition";
	private static final String PUT_SERVICES_HTTP_200_MESSAGE = "Service definition updated";
	private static final String PUT_SERVICES_HTTP_400_MESSAGE = "Could not update service definition";
	private static final String PUT_SERVICES_HTTP_404_MESSAGE = "Service definition with given parameters not exists";
	private static final String PATCH_SERVICES_HTTP_200_MESSAGE = "Service definition updated";
	private static final String PATCH_SERVICES_HTTP_400_MESSAGE = "Could not update service definition";
	private static final String PATCH_SERVICES_HTTP_404_MESSAGE = "Service definition with given parameters not exists";
	private static final String DELETE_SERVICES_HTTP_200_MESSAGE = "Service definition removed";
	private static final String DELETE_SERVICES_HTTP_400_MESSAGE = "Could not remove service definition";
	private static final String DELETE_SERVICES_HTTP_404_MESSAGE = "Service definition with given parameters not exists";
	
	private static final String NOT_VALID_PARAMETERS_ERROR_MESSAGE = "Not valid request parameters.";
	private static final String ID_MUST_BE_GREATER_THEN_ZERO_ERROR_MESSAGE ="System id must be greater then 0. ";

	
	private final Logger logger = LogManager.getLogger(ServiceRegistryController.class);

	@Autowired
	private ServiceRegistryDBService serviceRegistryDBService;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return an echo message with the purpose of testing the core service availability", response = String.class)
	@ApiResponses (value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = CommonConstants.SWAGGER_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path = ECHO_URI)
	public String echoService() {
		return "Got it!";
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return system by id", response = SystemResponseDTO.class)
	@ApiResponses (value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = GET_SYSTEM_BY_ID_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = GET_SYSTEM_BY_ID_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_EXPECTATION_FAILED, message = GET_SYSTEM_BY_ID_HTTP_417_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(SYSTEMS_BY_ID_URI)
	@ResponseBody public SystemResponseDTO getSystemById(@PathVariable(value = SYSTEM_BY_ID_PATH_VARIABLE) final long systemId) {		
		logger.debug("getSystemById started ...");
		
		if (systemId < 1) {
			throw new BadPayloadException(ID_MUST_BE_GREATER_THEN_ZERO_ERROR_MESSAGE, HttpStatus.SC_EXPECTATION_FAILED, CommonConstants.SERVICEREGISTRY_URI + SYSTEM_BY_ID_URI);
		}
		
		try {			
			
			final System system = serviceRegistryDBService.getSystemById(systemId);
			
			return DTOConverter.convertSystemToSystemResponseDTO(system);	
		
		} catch (final NoSuchElementException e) {
			throw new DataNotFoundException("No such System by id: " + systemId, HttpStatus.SC_BAD_REQUEST, CommonConstants.SERVICEREGISTRY_URI + SYSTEM_BY_ID_URI, e);
		}
		
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return systems by request parameters", response = SystemResponseDTO.class)
	@ApiResponses (value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = GET_SYSTEMS_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = GET_SYSTEMS_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_EXPECTATION_FAILED, message = GET_SYSTEMS_HTTP_417_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(SYSTEMS_URI)
	@ResponseBody public SystemListResponseDTO getSystems(
			@RequestParam(name = CommonConstants.REQUEST_PARAM_PAGE, required = false) final Integer page,
			@RequestParam(name = CommonConstants.REQUEST_PARAM_ITEM_PER_PAGE, required = false) final Integer size,
			@RequestParam(name = CommonConstants.REQUEST_PARAM_DIRECTION, defaultValue = CommonConstants.REQUEST_PARAM_DIRECTION_DEFAULT_VALUE) final String direction,
			@RequestParam(name = CommonConstants.REQUEST_PARAM_SORT_FIELD, defaultValue = CommonConstants.COMMON_FIELD_NAME_ID) final String sortField){		
		
		logger.debug("getSystems started ...");
		
		final int validatedPage;
		final int validatedSize;
		
		if ( page == null && size==null ) {
			
			validatedPage = -1;
			validatedSize = -1;
			
		} else {
			if ( page == null || size==null ) {
				
				throw new BadPayloadException(NOT_VALID_PARAMETERS_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, CommonConstants.SERVICEREGISTRY_URI + SYSTEMS_URI);
			
			} else {
				validatedPage = page;
				validatedSize = size;
			}			
		}
		
		try {			
			
			return DTOConverter.convertSystemEntryListToSystemListResponseDTO(serviceRegistryDBService.getSystemEntries(validatedPage, validatedSize, direction, sortField));	
		
		} catch ( final IllegalArgumentException e) {
			throw new BadPayloadException(NOT_VALID_PARAMETERS_ERROR_MESSAGE , HttpStatus.SC_BAD_REQUEST, CommonConstants.SERVICEREGISTRY_URI + SYSTEMS_URI, e);
		}
		
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return created system ", response = SystemResponseDTO.class)
	@ApiResponses (value = {
			@ApiResponse(code = HttpStatus.SC_CREATED, message = POST_SYSTEM_HTTP_201_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = POST_SYSTEM_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_EXPECTATION_FAILED, message = POST_SYSTEM_HTTP_417_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})	
	@PostMapping(path = SYSTEMS_URI, consumes = "application/json", produces = "application/json")
	@ResponseBody public SystemResponseDTO addSystem(@RequestBody final SystemRequestDTO request) {
		checkSystemRequest(request);
		
		try {
			
			return callCreateSystem(request);
		} catch (final Exception e) {
			throw new BadPayloadException(NOT_VALID_PARAMETERS_ERROR_MESSAGE , HttpStatus.SC_BAD_REQUEST, CommonConstants.SERVICEREGISTRY_URI + SYSTEMS_URI, e);
		}
		
	}
	
	//-------------------------------------------------------------------------------------------------

	@ApiOperation(value = "Return updated system ", response = SystemResponseDTO.class)
	@ApiResponses (value = {
			@ApiResponse(code = HttpStatus.SC_CREATED, message = PUT_SYSTEM_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = PUT_SYSTEM_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_EXPECTATION_FAILED, message = PUT_SYSTEM_HTTP_417_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})	
	@PutMapping(path = SYSTEMS_BY_ID_URI, consumes = "application/json", produces = "application/json")
	@ResponseBody public SystemResponseDTO updateSystem(@PathVariable(value = SYSTEM_BY_ID_PATH_VARIABLE) final long systemId, @RequestBody final SystemRequestDTO request) {
		checkSystemPutRequest(request, systemId);
		
		try {
			
			return callUpdateSystem(request, systemId);
		} catch (final Exception e) {
			throw new BadPayloadException(NOT_VALID_PARAMETERS_ERROR_MESSAGE , HttpStatus.SC_BAD_REQUEST, CommonConstants.SERVICEREGISTRY_URI + SYSTEMS_BY_ID_URI, e);
		}
	
	}

	//-------------------------------------------------------------------------------------------------

	@ApiOperation(value = "Return system  updated by fields", response = SystemResponseDTO.class)
	@ApiResponses (value = {
			@ApiResponse(code = HttpStatus.SC_CREATED, message = PATCH_SYSTEM_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = PATCH_SYSTEM_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_EXPECTATION_FAILED, message = PATCH_SYSTEM_HTTP_417_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})	
	@PatchMapping(path = SYSTEMS_BY_ID_URI, consumes = "application/json", produces = "application/json")
	@ResponseBody public SystemResponseDTO updateSystemByFields(@PathVariable(value = SYSTEM_BY_ID_PATH_VARIABLE) final long systemId, @RequestBody final SystemRequestDTO request) {
		checkSystemPatchRequest(request, systemId);
		
		try {
			
			return callNonNullableUpdateSystem(request, systemId);
		} catch (final Exception e) {
			throw new BadPayloadException(NOT_VALID_PARAMETERS_ERROR_MESSAGE , HttpStatus.SC_BAD_REQUEST, CommonConstants.SERVICEREGISTRY_URI + SYSTEMS_BY_ID_URI, e);
		}
	
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Remove system")
	@ApiResponses (value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = DELETE_SYSTEM_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = DELETE_SYSTEM_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@DeleteMapping(path =SYSTEMS_BY_ID_URI)
	public ResponseEntity<HttpStatus> removeSystem(@PathVariable(value = CommonConstants.COMMON_FIELD_NAME_ID) final long id) {
		logger.debug("New System delete request recieved with id: {}", id);
		if (id < 1) {
			throw new BadPayloadException(ID_MUST_BE_GREATER_THEN_ZERO_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, CommonConstants.SERVICEREGISTRY_URI + SYSTEMS_BY_ID_URI);
		}
		serviceRegistryDBService.removeSystemById(id);
		logger.debug("System with id: '{}' succesfully deleted", id);
		return new ResponseEntity<>(org.springframework.http.HttpStatus.OK);
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return requested service definitions by the given parameters", response = ServiceDefinitionsListResponseDTO.class)
	@ApiResponses (value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = GET_SERVICES_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = GET_SERVICES_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path =SERVICES_URI, produces = "application/json")
	@ResponseBody public ServiceDefinitionsListResponseDTO getBunchOfServiceDefinitions(
			@RequestParam(name = CommonConstants.REQUEST_PARAM_PAGE, required = false) final Integer page,
			@RequestParam(name = CommonConstants.REQUEST_PARAM_ITEM_PER_PAGE, required = false) final Integer size,
			@RequestParam(name = CommonConstants.REQUEST_PARAM_DIRECTION, defaultValue = CommonConstants.REQUEST_PARAM_DIRECTION_DEFAULT_VALUE) final String direction,
			@RequestParam(name = CommonConstants.REQUEST_PARAM_SORT_FIELD, defaultValue = CommonConstants.COMMON_FIELD_NAME_ID) final String sortField) {
		logger.debug("New Service Definition get request recieved with page: {} and item_per page: {}", page, size);
		int validatedPage;
		int validatedSize;
		Direction validatedDirection;
		final String validatedSortField = sortField.trim();
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
		switch (direction) {
			case "ASC":
				validatedDirection = Direction.ASC;
				break;
			case "DESC":
				validatedDirection = Direction.DESC;
				break;
			default:
				throw new BadPayloadException("Invalid sort direction flag", HttpStatus.SC_BAD_REQUEST, CommonConstants.SERVICEREGISTRY_URI + SERVICES_URI);
		}
		if (! ServiceDefinition.SORTABLE_FIELDS_BY.contains(validatedSortField)) {
			throw new BadPayloadException("Sortable field with reference '" + validatedSortField + "' is not available", HttpStatus.SC_BAD_REQUEST, CommonConstants.SERVICEREGISTRY_URI + SERVICES_URI);
		}
		final ServiceDefinitionsListResponseDTO serviceDefinitionEntries = serviceRegistryDBService.getAllServiceDefinitionEntriesResponse(validatedPage, validatedSize, validatedDirection, validatedSortField);
		logger.debug("Service definition  with page: {} and item_per page: {} succesfully retrived", page, size);
		return serviceDefinitionEntries;
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return requested service definition", response = ServiceDefinitionResponseDTO.class)
	@ApiResponses (value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = GET_SERVICES_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = GET_SERVICES_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_NOT_FOUND, message = GET_SERVICES_HTTP_404_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path =SERVICES_BY_ID_URI, produces = "application/json")
	@ResponseBody public ServiceDefinitionResponseDTO  getServiceDefinition(@PathVariable(value = CommonConstants.COMMON_FIELD_NAME_ID) final long id) {
		logger.debug("New Service Definition get request recieved with id: {}", id);
		if (id < 1) {
			throw new BadPayloadException(ID_MUST_BE_GREATER_THEN_ZERO_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, CommonConstants.SERVICEREGISTRY_URI + SERVICES_BY_ID_URI);
		}
		final ServiceDefinitionResponseDTO serviceDefinitionEntry = serviceRegistryDBService.getServiceDefinitionByIdResponse(id);
		logger.debug("Service definition with id: '{}' succesfully retrived", id);
		return serviceDefinitionEntry;
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return created service definition", response = ServiceDefinitionResponseDTO.class)
	@ApiResponses (value = {
			@ApiResponse(code = HttpStatus.SC_CREATED, message = POST_SERVICES_HTTP_201_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = POST_SERVICES_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@PostMapping(path =SERVICES_URI, consumes = "application/json", produces = "application/json")
	@ResponseStatus(value = org.springframework.http.HttpStatus.CREATED)
	@ResponseBody public ServiceDefinitionResponseDTO registerServiceDefinition(@RequestBody final ServiceDefinitionRequestDTO serviceDefinitionRequestDTO) {
		final String serviceDefinition = serviceDefinitionRequestDTO.getServiceDefinition();
		logger.debug("New Service Definition registration request recieved with definition: {}", serviceDefinition);
		if (serviceDefinition.isBlank()) {
			throw new BadPayloadException("serviceDefinition is blank", HttpStatus.SC_BAD_REQUEST, CommonConstants.SERVICEREGISTRY_URI + SERVICES_URI);
		}
		serviceDefinition.trim().toLowerCase();
		final ServiceDefinitionResponseDTO serviceDefinitionResponse = serviceRegistryDBService.createServiceDefinitionResponse(serviceDefinition);
		logger.debug("{} service definition succesfully registered.", serviceDefinition);
		return serviceDefinitionResponse;
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return updated service definition", response = ServiceDefinitionResponseDTO.class)
	@ApiResponses (value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = PUT_SERVICES_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = PUT_SERVICES_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_NOT_FOUND, message = PUT_SERVICES_HTTP_404_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@PutMapping(path =SERVICES_BY_ID_URI, consumes = "application/json", produces = "application/json")
	@ResponseBody public ServiceDefinitionResponseDTO putUpdateServiceDefinition(@PathVariable(value = CommonConstants.COMMON_FIELD_NAME_ID) final long id
			, @RequestBody final ServiceDefinitionRequestDTO serviceDefinitionRequestDTO) {
		final String serviceDefinition = serviceDefinitionRequestDTO.getServiceDefinition();
		logger.debug("New Service Definition update request recieved with id: {}, definition: {}", id, serviceDefinition);
		if (id < 1) {
			throw new BadPayloadException(ID_MUST_BE_GREATER_THEN_ZERO_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, CommonConstants.SERVICEREGISTRY_URI + SERVICES_BY_ID_URI);
		}		
		if (serviceDefinition.isBlank()) {
			throw new BadPayloadException("serviceDefinition is blank", HttpStatus.SC_BAD_REQUEST, CommonConstants.SERVICEREGISTRY_URI + SERVICES_BY_ID_URI);
		}
		serviceDefinition.trim().toLowerCase();
		final ServiceDefinitionResponseDTO serviceDefinitionResponse = serviceRegistryDBService.updateServiceDefinitionByIdResponse(id, serviceDefinition);
		logger.debug("Service definition with id: '{}' succesfully updated with definition '{}'.", id, serviceDefinition);
		return serviceDefinitionResponse;
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return updated service definition", response = ServiceDefinitionResponseDTO.class)
	@ApiResponses (value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = PATCH_SERVICES_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = PATCH_SERVICES_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_NOT_FOUND, message = PATCH_SERVICES_HTTP_404_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@PatchMapping(path =SERVICES_BY_ID_URI, consumes = "application/json", produces = "application/json")
	@ResponseBody public ServiceDefinitionResponseDTO patchUpdateServiceDefinition(@PathVariable(value = CommonConstants.COMMON_FIELD_NAME_ID) final long id
			, @RequestBody final ServiceDefinitionRequestDTO serviceDefinitionRequestDTO) {
		//Currently ServiceDefinition has only one updatable field, therefore PUT and PATH do the same
		return putUpdateServiceDefinition(id, serviceDefinitionRequestDTO);
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Remove service definition")
	@ApiResponses (value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = DELETE_SERVICES_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = DELETE_SERVICES_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_NOT_FOUND, message = DELETE_SERVICES_HTTP_404_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@DeleteMapping(path =SERVICES_BY_ID_URI)
	public void removeServiceDefinition(@PathVariable(value = CommonConstants.COMMON_FIELD_NAME_ID) final long id) {
		logger.debug("New Service Definition delete request recieved with id: {}", id);
		if (id < 1) {
			throw new BadPayloadException(ID_MUST_BE_GREATER_THEN_ZERO_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, CommonConstants.SERVICEREGISTRY_URI + SERVICES_BY_ID_URI);
		}
		serviceRegistryDBService.removeServiceDefinitionById(id);
		logger.debug("Service definition with id: '{}' succesfully deleted", id);
	}
		
	//=================================================================================================
	// assistant methods
	

	//-------------------------------------------------------------------------------------------------

	private SystemResponseDTO callCreateSystem(final SystemRequestDTO request) {
		
		final String validatedSystemName = request.getSystemName().toLowerCase();
		final String validatedAddress = request.getAddress().toLowerCase();
		final int  validatedPort = request.getPort();
		final String validatedAuthenticationInfo = request.getAuthenticationInfo()!=null?request.getAuthenticationInfo():"";
		
		if (validatedPort < CommonConstants.SYSTEM_PORT_RANGE_MIN || validatedPort > CommonConstants.SYSTEM_PORT_RANGE_MAX) {
			throw new BadPayloadException("Port must be between "+ 
					CommonConstants.SYSTEM_PORT_RANGE_MIN + " and " + 
					CommonConstants.SYSTEM_PORT_RANGE_MAX +"", HttpStatus.SC_BAD_REQUEST, CommonConstants.SERVICEREGISTRY_URI + SYSTEMS_BY_ID_URI);
		}
		
		return serviceRegistryDBService.createSystemResponse( validatedSystemName, validatedAddress, validatedPort, validatedAuthenticationInfo);
	}
	
	//-------------------------------------------------------------------------------------------------
	
	private SystemResponseDTO callUpdateSystem(final SystemRequestDTO request, final long systemId) {
		
		final long validatedSystemId = systemId;		

		final String validatedSystemName = request.getSystemName().toLowerCase();
		final String validatedAddress = request.getAddress().toLowerCase();
		final int  validatedPort = request.getPort();
		final String validatedAuthenticationInfo = request.getAuthenticationInfo()!=null?request.getAuthenticationInfo():"";
		
		return serviceRegistryDBService.updateSystemResponse(validatedSystemId, validatedSystemName, validatedAddress, validatedPort, validatedAuthenticationInfo);
	}
	
	//-------------------------------------------------------------------------------------------------
	
	private SystemResponseDTO callNonNullableUpdateSystem(final SystemRequestDTO request, final long systemId) {
		final long validatedSystemId = systemId;		
	
		final String validatedSystemName = request.getSystemName() != null ? request.getSystemName().toLowerCase():"";
		final String validatedAddress = request.getSystemName() != null ? request.getAddress().toLowerCase():"";
		final Integer  validatedPort = request.getPort();
		final String validatedAuthenticationInfo = request.getAuthenticationInfo()!=null?request.getAuthenticationInfo():"";
		
		return serviceRegistryDBService.updateNonNullableSystemResponse(validatedSystemId, validatedSystemName, validatedAddress, validatedPort, validatedAuthenticationInfo);
	}

	//-------------------------------------------------------------------------------------------------
	
	private void checkSystemPatchRequest(final SystemRequestDTO request, final long systemId) {
		
		if ( systemId <= 0) {
			throw new BadPayloadException(ID_MUST_BE_GREATER_THEN_ZERO_ERROR_MESSAGE , HttpStatus.SC_BAD_REQUEST, CommonConstants.SERVICEREGISTRY_URI + SYSTEMS_BY_ID_URI);
		}
		
		boolean needChange = false;
		
		if (!Utilities.isEmpty(request.getAddress())) {
			needChange = true;
		}
		
		if (!Utilities.isEmpty(request.getSystemName())) {
			needChange = true;
		}
		
		if (request.getPort() != null ) {
			needChange = true;
		}
		
		if (request.getAuthenticationInfo() != null ) {
			needChange = true;
		}
		if (!needChange) {
			throw new BadPayloadException("Patch request is empty." , HttpStatus.SC_BAD_REQUEST, CommonConstants.SERVICEREGISTRY_URI + SYSTEMS_BY_ID_URI);
		}
	}
	//-------------------------------------------------------------------------------------------------
	
	private void checkSystemPutRequest(final SystemRequestDTO request, final long systemId) {
		
		if ( systemId <= 0) {
			throw new BadPayloadException(ID_MUST_BE_GREATER_THEN_ZERO_ERROR_MESSAGE , HttpStatus.SC_BAD_REQUEST, CommonConstants.SERVICEREGISTRY_URI + SYSTEMS_BY_ID_URI);
		}
		
		if ( request.getAddress() == null || "".equalsIgnoreCase(request.getAddress().trim()) ) {
			throw new BadPayloadException("System address is null or empty." , HttpStatus.SC_BAD_REQUEST, CommonConstants.SERVICEREGISTRY_URI + SYSTEMS_BY_ID_URI);
		}
		if ( request.getPort() == null ) {
			throw new BadPayloadException("System port is null." , HttpStatus.SC_BAD_REQUEST, CommonConstants.SERVICEREGISTRY_URI + SYSTEMS_BY_ID_URI);
		}
		if ( request.getSystemName() == null || "".equalsIgnoreCase(request.getAddress().trim())) {
			throw new BadPayloadException("System name is null or empty." , HttpStatus.SC_BAD_REQUEST, CommonConstants.SERVICEREGISTRY_URI + SYSTEMS_BY_ID_URI);
		}
			
	}

	//-------------------------------------------------------------------------------------------------
	
	private void checkSystemRequest(final SystemRequestDTO request) {
		
		if (request.getAddress() == null || "".equalsIgnoreCase(request.getAddress().trim()) ) {
			throw new BadPayloadException("System address is null or empty." , HttpStatus.SC_BAD_REQUEST, CommonConstants.SERVICEREGISTRY_URI + SYSTEMS_URI);
		}
		if (request.getPort() == null ) {
			throw new BadPayloadException("System port is null." , HttpStatus.SC_BAD_REQUEST, CommonConstants.SERVICEREGISTRY_URI + SYSTEMS_URI);
		}
		if (request.getSystemName() == null || "".equalsIgnoreCase(request.getAddress().trim())) {
			throw new BadPayloadException("System name is null or empty." , HttpStatus.SC_BAD_REQUEST, CommonConstants.SERVICEREGISTRY_URI + SYSTEMS_URI);
		}
	}

	 
}	
