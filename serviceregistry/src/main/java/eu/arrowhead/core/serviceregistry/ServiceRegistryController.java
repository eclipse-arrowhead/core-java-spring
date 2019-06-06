package eu.arrowhead.core.serviceregistry;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
import eu.arrowhead.common.Defaults;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.service.ServiceRegistryDBService;
import eu.arrowhead.common.dto.ServiceDefinitionRequestDTO;
import eu.arrowhead.common.dto.ServiceDefinitionResponseDTO;
import eu.arrowhead.common.dto.ServiceDefinitionsListResponseDTO;
import eu.arrowhead.common.dto.SystemListResponseDTO;
import eu.arrowhead.common.dto.SystemRequestDTO;
import eu.arrowhead.common.dto.SystemResponseDTO;
import eu.arrowhead.common.exception.BadPayloadException;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@CrossOrigin(maxAge = Defaults.CORS_MAX_AGE, allowCredentials = Defaults.CORS_ALLOW_CREDENTIALS, 
			 allowedHeaders = { HttpHeaders.ORIGIN, HttpHeaders.CONTENT_TYPE, HttpHeaders.ACCEPT, HttpHeaders.AUTHORIZATION }
)
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
	private static final String SYSTEM_ID_NOT_VALID_ERROR_MESSAGE = " System id must be greater then 0. ";
	private static final String SYSTEM_NAME_NULL_ERROR_MESSAGE = " System name must have value ";
	private static final String SYSTEM_ADDRESS_NULL_ERROR_MESSAGE = " System address must have value ";
	private static final String SYSTEM_PORT_NULL_ERROR_MESSAGE = " System port must have value ";
	
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
			throw new BadPayloadException(SYSTEM_ID_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, CommonConstants.SERVICEREGISTRY_URI + SYSTEM_BY_ID_URI);
		}
		
		return serviceRegistryDBService.getSystemById(systemId);			
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
			@RequestParam(name = CommonConstants.REQUEST_PARAM_DIRECTION, defaultValue = Defaults.DEFAULT_REQUEST_PARAM_DIRECTION_VALUE) final Direction direction,
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
		
		return serviceRegistryDBService.getSystemEntries(validatedPage, validatedSize, direction, sortField);			
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
	@PostMapping(path = SYSTEMS_URI, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(org.springframework.http.HttpStatus.CREATED)
	@ResponseBody public SystemResponseDTO addSystem(@RequestBody final SystemRequestDTO request) {
		checkSystemRequest(request);
		
		return callCreateSystem(request);
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
	@PutMapping(path = SYSTEMS_BY_ID_URI, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public SystemResponseDTO updateSystem(@PathVariable(value = SYSTEM_BY_ID_PATH_VARIABLE) final long systemId, @RequestBody final SystemRequestDTO request) {
		checkSystemPutRequest(request, systemId);
		
		return callUpdateSystem(request, systemId);
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
	@PatchMapping(path = SYSTEMS_BY_ID_URI, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public SystemResponseDTO mergeSystem(@PathVariable(value = SYSTEM_BY_ID_PATH_VARIABLE) final long systemId, @RequestBody final SystemRequestDTO request) {
		checkSystemMergeRequest(request, systemId);
		
		return callMergeSystem(request, systemId);
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
			throw new BadPayloadException(SYSTEM_ID_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, CommonConstants.SERVICEREGISTRY_URI + SYSTEMS_BY_ID_URI);
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
	@GetMapping(path =SERVICES_URI, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public ServiceDefinitionsListResponseDTO getServiceDefinitions(
			@RequestParam(name = CommonConstants.REQUEST_PARAM_PAGE, required = false) final Integer page,
			@RequestParam(name = CommonConstants.REQUEST_PARAM_ITEM_PER_PAGE, required = false) final Integer size,
			@RequestParam(name = CommonConstants.REQUEST_PARAM_DIRECTION, defaultValue = Defaults.DEFAULT_REQUEST_PARAM_DIRECTION_VALUE) final String direction,
			@RequestParam(name = CommonConstants.REQUEST_PARAM_SORT_FIELD, defaultValue = CommonConstants.COMMON_FIELD_NAME_ID) final String sortField) {
		logger.debug("New Service Definition get request recieved with page: {} and item_per page: {}", page, size);
		int validatedPage;
		int validatedSize;
		Direction validatedDirection;
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
		if (sortField.isBlank()) {
			throw new BadPayloadException("sortField is blank", HttpStatus.SC_BAD_REQUEST, CommonConstants.SERVICEREGISTRY_URI + SERVICES_URI);
		}
		final ServiceDefinitionsListResponseDTO serviceDefinitionEntries = serviceRegistryDBService.getServiceDefinitionEntriesResponse(validatedPage, validatedSize, validatedDirection, sortField);
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
	@GetMapping(path =SERVICES_BY_ID_URI, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public ServiceDefinitionResponseDTO  getServiceDefinitionById(@PathVariable(value = CommonConstants.COMMON_FIELD_NAME_ID) final long id) {
		logger.debug("New Service Definition get request recieved with id: {}", id);
		if (id < 1) {
			throw new BadPayloadException(SYSTEM_ID_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, CommonConstants.SERVICEREGISTRY_URI + SERVICES_BY_ID_URI);
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
	@PostMapping(path =SERVICES_URI, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(value = org.springframework.http.HttpStatus.CREATED)
	@ResponseBody public ServiceDefinitionResponseDTO addServiceDefinition(@RequestBody final ServiceDefinitionRequestDTO serviceDefinitionRequestDTO) {
		final String serviceDefinition = serviceDefinitionRequestDTO.getServiceDefinition();
		logger.debug("New Service Definition registration request recieved with definition: {}", serviceDefinition);
		if (serviceDefinition == null || serviceDefinition.isBlank()) {
			throw new BadPayloadException("serviceDefinition is null or blank", HttpStatus.SC_BAD_REQUEST, CommonConstants.SERVICEREGISTRY_URI + SERVICES_URI);
		}
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
	@PutMapping(path =SERVICES_BY_ID_URI, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public ServiceDefinitionResponseDTO putUpdateServiceDefinition(@PathVariable(value = CommonConstants.COMMON_FIELD_NAME_ID) final long id
			, @RequestBody final ServiceDefinitionRequestDTO serviceDefinitionRequestDTO) {
		final String serviceDefinition = serviceDefinitionRequestDTO.getServiceDefinition();
		logger.debug("New Service Definition update request recieved with id: {}, definition: {}", id, serviceDefinition);
		if (id < 1) {
			throw new BadPayloadException(SYSTEM_ID_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, CommonConstants.SERVICEREGISTRY_URI + SERVICES_BY_ID_URI);
		}		
		if (serviceDefinition == null || serviceDefinition.isBlank()) {
			throw new BadPayloadException("serviceDefinition is null or blank", HttpStatus.SC_BAD_REQUEST, CommonConstants.SERVICEREGISTRY_URI + SERVICES_BY_ID_URI);
		}
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
	@PatchMapping(path =SERVICES_BY_ID_URI, consumes = MediaType.APPLICATION_JSON_VALUE, produces = "application/json")
	@ResponseBody public ServiceDefinitionResponseDTO patchUpdateServiceDefinition(@PathVariable(value = CommonConstants.COMMON_FIELD_NAME_ID) final long id
			, @RequestBody final ServiceDefinitionRequestDTO serviceDefinitionRequestDTO) {
		//Currently ServiceDefinition has only one updatable field, therefore PUT and PATCH do the same
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
			throw new BadPayloadException(SYSTEM_ID_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, CommonConstants.SERVICEREGISTRY_URI + SERVICES_BY_ID_URI);
		}
		serviceRegistryDBService.removeServiceDefinitionById(id);
		logger.debug("Service definition with id: '{}' succesfully deleted", id);
	}
		
	//=================================================================================================
	// assistant methods
	

	//-------------------------------------------------------------------------------------------------

	private SystemResponseDTO callCreateSystem(final SystemRequestDTO request) {
		logger.debug(" callCreateSystem started ...");
		
		checkSystemRequest(request);
		
		final String systemName = request.getSystemName();
		final String address = request.getAddress();
		final int  port = request.getPort();
		final String authenticationInfo = request.getAuthenticationInfo();
		
		return serviceRegistryDBService.createSystemResponse( systemName, address, port, authenticationInfo);
	}
	
	//-------------------------------------------------------------------------------------------------
	
	private SystemResponseDTO callUpdateSystem(final SystemRequestDTO request, final long systemId) {
		logger.debug(" callUpdateSystem started ...");
		
		checkSystemPutRequest(request, systemId);
		
		final long validatedSystemId = systemId;		

		final String validatedSystemName = request.getSystemName().toLowerCase();
		final String validatedAddress = request.getAddress().toLowerCase();
		final int  validatedPort = request.getPort();
		final String validatedAuthenticationInfo = request.getAuthenticationInfo()!=null?request.getAuthenticationInfo():"";
		
		return serviceRegistryDBService.updateSystemResponse(validatedSystemId, validatedSystemName, validatedAddress, validatedPort, validatedAuthenticationInfo);
	}
	
	//-------------------------------------------------------------------------------------------------
	
	private SystemResponseDTO callMergeSystem(final SystemRequestDTO request, final long systemId) {		
		logger.debug(" callMergeSystem started ...");
		
		checkSystemMergeRequest(request, systemId);
		
		final long validatedSystemId = systemId;		
	
		final String validatedSystemName = request.getSystemName() != null ? request.getSystemName().toLowerCase():"";
		final String validatedAddress = request.getAddress() != null ? request.getAddress().toLowerCase():"";
		final Integer  validatedPort = request.getPort();
		final String validatedAuthenticationInfo = request.getAuthenticationInfo()!=null?request.getAuthenticationInfo():"";
		
		return serviceRegistryDBService.mergeSystemResponse(validatedSystemId, validatedSystemName, validatedAddress, validatedPort, validatedAuthenticationInfo);
	}

	//-------------------------------------------------------------------------------------------------
	
	private void checkSystemMergeRequest(final SystemRequestDTO request, final long systemId) {
		logger.debug(" checkSystemPatchRequest started ...");
		
		if ( systemId <= 0) {
			throw new BadPayloadException(SYSTEM_ID_NOT_VALID_ERROR_MESSAGE , HttpStatus.SC_BAD_REQUEST, CommonConstants.SERVICEREGISTRY_URI + SYSTEMS_BY_ID_URI);
		}
		
		boolean needChange = false;
		
		if (!Utilities.isEmpty(request.getAddress())) {
			needChange = true;
		}
		
		if (!Utilities.isEmpty(request.getSystemName())) {
			needChange = true;
		}
		
		if (request.getPort() != null ) {
			
			final Integer validatedPort = request.getPort();
			if (validatedPort < CommonConstants.SYSTEM_PORT_RANGE_MIN || validatedPort > CommonConstants.SYSTEM_PORT_RANGE_MAX) {
				throw new BadPayloadException("Port must be between "+ 
						CommonConstants.SYSTEM_PORT_RANGE_MIN + " and " + 
						CommonConstants.SYSTEM_PORT_RANGE_MAX +"", HttpStatus.SC_BAD_REQUEST, CommonConstants.SERVICEREGISTRY_URI + SYSTEMS_BY_ID_URI);
			}
			
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
		logger.debug(" checkSystemPutRequest started ...");
		
		if ( systemId <= 0) {
			throw new BadPayloadException(SYSTEM_ID_NOT_VALID_ERROR_MESSAGE , HttpStatus.SC_BAD_REQUEST, CommonConstants.SERVICEREGISTRY_URI + SYSTEMS_BY_ID_URI);
		}
		
		checkSystemRequest(request);
			
	}

	//-------------------------------------------------------------------------------------------------
	
	private void checkSystemRequest(final SystemRequestDTO request) {
		logger.debug(" checkSystemRequest started ...");
		
		if (Utilities.isEmpty(request.getSystemName())) {
			throw new BadPayloadException( SYSTEM_NAME_NULL_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, CommonConstants.SERVICEREGISTRY_URI + SYSTEMS_BY_ID_URI);
		}
		
		if (Utilities.isEmpty(request.getAddress())) {
			throw new BadPayloadException( SYSTEM_ADDRESS_NULL_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, CommonConstants.SERVICEREGISTRY_URI + SYSTEMS_BY_ID_URI);
		}
		
		if (request.getPort() == null) {
			throw new BadPayloadException( SYSTEM_PORT_NULL_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, CommonConstants.SERVICEREGISTRY_URI + SYSTEMS_BY_ID_URI);
		}
		
		final int validatedPort = request.getPort();
		if (validatedPort < CommonConstants.SYSTEM_PORT_RANGE_MIN || validatedPort > CommonConstants.SYSTEM_PORT_RANGE_MAX) {
			throw new BadPayloadException("Port must be between "+ 
					CommonConstants.SYSTEM_PORT_RANGE_MIN + " and " + 
					CommonConstants.SYSTEM_PORT_RANGE_MAX +"", HttpStatus.SC_BAD_REQUEST, CommonConstants.SERVICEREGISTRY_URI + SYSTEMS_BY_ID_URI);
		}
	}

	 
}	
