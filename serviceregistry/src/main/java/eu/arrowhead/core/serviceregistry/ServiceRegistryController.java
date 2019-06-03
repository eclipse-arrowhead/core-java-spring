package eu.arrowhead.core.serviceregistry;

import java.util.NoSuchElementException;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.database.entity.System;
import eu.arrowhead.common.database.service.ServiceRegistryDBService;
import eu.arrowhead.common.dto.DTOConverter;
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
	private static final String SYSTEM_BY_ID_URI = "/mgmt/system/{" + SYSTEM_BY_ID_PATH_VARIABLE + "}";
	private static final String SYSTEMS_URI = "/mgmt/systems";
	private static final String GET_SYSTEMS_HTTP_200_MESSAGE = "Systems returned";
	private static final String GET_SYSTEMS_HTTP_400_MESSAGE = " Invalid paraameters";
	private static final String GET_SYSTEMS_HTTP_417_MESSAGE = "Not valid request parameters";
	private static final String POST_SYSTEM_HTTP_200_MESSAGE = "System created";
	private static final String POST_SYSTEM_HTTP_400_MESSAGE = "Could not create system";
	private static final String POST_SYSTEM_HTTP_417_MESSAGE = "Not valid request parameters";
	
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
	@GetMapping(SYSTEM_BY_ID_URI)
	@ResponseBody public SystemResponseDTO getSystemById(@PathVariable(value = SYSTEM_BY_ID_PATH_VARIABLE) final long systemId) {		
		logger.debug("getSystemById started ...");
		
		if (systemId < 1) {
			throw new BadPayloadException("System id must be greater then 0. ", HttpStatus.SC_EXPECTATION_FAILED, CommonConstants.SERVICEREGISTRY_URI + SYSTEM_BY_ID_URI);
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
				
				throw new BadPayloadException(" Invalid paraameters", HttpStatus.SC_BAD_REQUEST, CommonConstants.SERVICEREGISTRY_URI + SYSTEMS_URI);
			
			} else {
				validatedPage = page;
				validatedSize = size;
			}			
		}
		
		try {			
			
			return DTOConverter.convertSystemEntryListToSystemListResponseDTO(serviceRegistryDBService.getSystemEntries(validatedPage, validatedSize, direction, sortField));	
		
		} catch ( final IllegalArgumentException e) {
			throw new BadPayloadException("Not valid request parameters." , HttpStatus.SC_BAD_REQUEST, CommonConstants.SERVICEREGISTRY_URI + SYSTEMS_URI, e);
		}
		
	}
	
	//-------------------------------------------------------------------------------------------------

	@ApiOperation(value = "Return created system ", response = SystemResponseDTO.class)
	@ApiResponses (value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = POST_SYSTEM_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = POST_SYSTEM_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_EXPECTATION_FAILED, message = POST_SYSTEM_HTTP_417_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})	
	@PostMapping(path = SYSTEMS_URI, consumes = "application/json", produces = "application/json")
	@ResponseBody public SystemResponseDTO addSystem(@RequestBody final SystemRequestDTO request) {
		checkSystemRequest(request);
		
		try {
			final System system = serviceRegistryDBService.createSystem(request);
			return DTOConverter.convertSystemToSystemResponseDTO(system);
		} catch (final Exception e) {
			throw new BadPayloadException("Not valid request parameters." , HttpStatus.SC_BAD_REQUEST, CommonConstants.SERVICEREGISTRY_URI + SYSTEMS_URI, e);
		}
		
			
		
	}

	//=================================================================================================
	// assistant methods
	
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