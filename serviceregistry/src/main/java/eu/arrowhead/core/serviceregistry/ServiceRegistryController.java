package eu.arrowhead.core.serviceregistry;

import java.util.NoSuchElementException;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.database.entity.System;
import eu.arrowhead.common.database.service.ServiceRegistryDBService;
import eu.arrowhead.common.dto.DTOConverter;
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
	//TODO
	//implement List<SystemResponseDTO> getSystems(int page, int itemPerPage){...}
}