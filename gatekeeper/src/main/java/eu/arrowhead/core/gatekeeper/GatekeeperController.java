package eu.arrowhead.core.gatekeeper;

import java.util.List;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.Defaults;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.Utilities.ValidatedPageParams;
import eu.arrowhead.common.dto.RelayRequestDTO;
import eu.arrowhead.common.dto.RelayResponseDTO;
import eu.arrowhead.common.dto.RelayResponseListDTO;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.core.gatekeeper.database.service.GatekeeperDBService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@CrossOrigin(maxAge = Defaults.CORS_MAX_AGE, allowCredentials = Defaults.CORS_ALLOW_CREDENTIALS, 
allowedHeaders = { HttpHeaders.ORIGIN, HttpHeaders.CONTENT_TYPE, HttpHeaders.ACCEPT, HttpHeaders.AUTHORIZATION }
)
@RestController(CommonConstants.GATEKEEPER_URI)
public class GatekeeperController {
	
	//=================================================================================================
	// members
	
	private static final String PATH_VARIABLE_ID = "id";
	private static final String PATH_VARIABLE_ADDRESS = "address";
	private static final String PATH_VARIABLE_PORT = "port";
	private static final String ID_NOT_VALID_ERROR_MESSAGE = "Id must be greater than 0.";
	
	private static final String BROKERS_MGMT_URI =  CommonConstants.MGMT_URI + "/brokers";
	private static final String BROKERS_BY_ID_MGMT_URI = BROKERS_MGMT_URI + "/{" + PATH_VARIABLE_ID + "}";	
	private static final String BROKERS_BY_ADDRESS_AND_PORT_MGMT_URI = BROKERS_MGMT_URI + "/{" + PATH_VARIABLE_ADDRESS + "}" + "/{" + PATH_VARIABLE_PORT + "}";
	
	private static final String GET_BROKERS_MGMT_HTTP_200_MESSAGE = "Broker entries returned";
	private static final String GET_BROKERS_MGMT_HTTP_400_MESSAGE = "Could not retrieve Broker entries";
	private static final String POST_BROKERS_MGMT_HTTP_200_MESSAGE = "Broker entries created";
	private static final String POST_BROKERS_MGMT_HTTP_400_MESSAGE = "Could not create Broker entries";
	private static final String PUT_BROKERS_MGMT_HTTP_200_MESSAGE = "Broker entry updated";
	private static final String PUT_BROKERS_MGMT_HTTP_400_MESSAGE = "Could not update Broker entry";
	private static final String DELETE_BROKERS_MGMT_HTTP_200_MESSAGE = "Broker entry removed";
	private static final String DELETE_BROKERS_MGMT_HTTP_400_MESSAGE = "Could not remove Broker entry";
	
	private final Logger logger = LogManager.getLogger(GatekeeperController.class);
	
	@Autowired
	private GatekeeperDBService gatekeeperDBService;
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return an echo message with the purpose of testing the core service availability", response = String.class)
	@ApiResponses (value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = CommonConstants.SWAGGER_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path = CommonConstants.ECHO_URI)
	public String echoService() {
		return "Got it!";
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return requested Broker entries by the given parameters", response = RelayResponseListDTO.class)
	@ApiResponses (value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = GET_BROKERS_MGMT_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = GET_BROKERS_MGMT_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path = BROKERS_MGMT_URI, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public RelayResponseListDTO getRelays(
			@RequestParam(name = CommonConstants.REQUEST_PARAM_PAGE, required = false) final Integer page,
			@RequestParam(name = CommonConstants.REQUEST_PARAM_ITEM_PER_PAGE, required = false) final Integer size,
			@RequestParam(name = CommonConstants.REQUEST_PARAM_DIRECTION, defaultValue = Defaults.DEFAULT_REQUEST_PARAM_DIRECTION_VALUE) final String direction,
			@RequestParam(name = CommonConstants.REQUEST_PARAM_SORT_FIELD, defaultValue = CommonConstants.COMMON_FIELD_NAME_ID) final String sortField) {
		logger.debug("New brokers get request recieved with page: {} and item_per page: {}", page, size);
		
		final ValidatedPageParams validParameters = Utilities.validatePageParameters(page, size, direction, CommonConstants.GATEKEEPER_URI + BROKERS_MGMT_URI);
		
		final RelayResponseListDTO relaysResponse = gatekeeperDBService.getRelaysResponse(validParameters.getValidatedPage(), validParameters.getValidatedSize(), validParameters.getValidatedDirecion(), sortField);
		
		logger.debug("Brokers  with page: {} and item_per page: {} retrieved successfully", page, size);
		return relaysResponse;
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return requested Broker entry", response = RelayResponseDTO.class)
	@ApiResponses (value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = GET_BROKERS_MGMT_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = GET_BROKERS_MGMT_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path = BROKERS_BY_ID_MGMT_URI, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public RelayResponseDTO getRelayById(@PathVariable(value = PATH_VARIABLE_ID) final long id) {
		logger.debug("New Broker get request recieved with id: {}", id);
		final String origin = CommonConstants.GATEKEEPER_URI + BROKERS_BY_ID_MGMT_URI;
		
		if (id < 1) {
			throw new BadPayloadException(ID_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		final RelayResponseDTO relayResponse = gatekeeperDBService.getRelayByIdResponse(id);
		
		logger.debug("Broker entry with id: {} successfully retrieved", id);
		return relayResponse;
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return requested Broker entry", response = RelayResponseDTO.class)
	@ApiResponses (value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = GET_BROKERS_MGMT_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = GET_BROKERS_MGMT_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path = BROKERS_BY_ADDRESS_AND_PORT_MGMT_URI, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public RelayResponseDTO getRelayByAddressAndPort(@PathVariable(value = PATH_VARIABLE_ADDRESS) final String address, @PathVariable(value = PATH_VARIABLE_PORT) final int port) {
		logger.debug("New Broker get request recieved with address: '{}', and port: '{}'", address, port);
		final String origin = CommonConstants.GATEKEEPER_URI + BROKERS_BY_ADDRESS_AND_PORT_MGMT_URI;
		
		if (Utilities.isEmpty(address)) {
			throw new BadPayloadException("Address is empty", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		if (isPortOutOfValidRange(port)) {
			throw new BadPayloadException("Port should be between " + CommonConstants.SYSTEM_PORT_RANGE_MIN + " and " + CommonConstants.SYSTEM_PORT_RANGE_MAX,
										  HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		final RelayResponseDTO relayResponse = gatekeeperDBService.getRelayByAddressAndPortResponse(address, port);
		
		logger.debug("Broker entry with address: '{}', and port: '{}' successfully retrieved", address, port);
		return relayResponse;
		
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return created Broker entries", response = RelayResponseListDTO.class)
	@ApiResponses (value = {
			@ApiResponse(code = HttpStatus.SC_CREATED, message = POST_BROKERS_MGMT_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = POST_BROKERS_MGMT_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@ResponseStatus(value = org.springframework.http.HttpStatus.CREATED)
	@PostMapping(path = BROKERS_MGMT_URI, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public RelayResponseListDTO registerRelays(@RequestBody final List<RelayRequestDTO> dtoList) {
		logger.debug("New Broker post request recieved");
		final String origin = CommonConstants.GATEKEEPER_URI + BROKERS_MGMT_URI;
		
		if (dtoList == null || dtoList.isEmpty()) {
			throw new BadPayloadException("List of RelayRequestDTO is empty", HttpStatus.SC_BAD_REQUEST, origin);
		}
		for (final RelayRequestDTO dto : dtoList) {
			validateRelayRequestDTO(dto, origin);
		}
		
		final RelayResponseListDTO relayResponseListDTO = gatekeeperDBService.registerBulkRelaysResponse(dtoList);
		
		logger.debug("registerRelays has been finished");
		return relayResponseListDTO;
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return updated Broker entry", response = RelayResponseDTO.class)
	@ApiResponses (value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = PUT_BROKERS_MGMT_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = PUT_BROKERS_MGMT_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@PutMapping(path = BROKERS_BY_ID_MGMT_URI, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public RelayResponseDTO updateRelayById(@PathVariable(value = PATH_VARIABLE_ID) final long id, @RequestBody final RelayRequestDTO dto) {
		logger.debug("New Broker put request recieved with id: {}", id);
		final String origin = CommonConstants.GATEKEEPER_URI + BROKERS_BY_ID_MGMT_URI;
		
		if (id < 1) {
			throw new BadPayloadException(ID_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
		}
		validateRelayRequestDTO(dto, origin);
		
		final RelayResponseDTO relayResponse = gatekeeperDBService.updateRelayByIdResponse(id, dto.getAddress(), dto.getPort(), dto.isSecure(), dto.isExclusive(), Utilities.convertStringToRelayType(dto.getType()));
		
		logger.debug("Broker with id '{}' is successfully updated", id);
		return relayResponse;
	}

	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Remove requested Broker entry")
	@ApiResponses (value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = DELETE_BROKERS_MGMT_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = DELETE_BROKERS_MGMT_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@DeleteMapping(path = BROKERS_BY_ID_MGMT_URI)
	public void removeRelayById(@PathVariable(value = PATH_VARIABLE_ID) final long id) {
		logger.debug("New Broker delete request recieved with id: {}", id);
		
		if (id < 1) {
			throw new BadPayloadException(ID_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, CommonConstants.GATEKEEPER_URI + BROKERS_BY_ID_MGMT_URI);
		}
		
		gatekeeperDBService.removeRelayById(id);
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------	
	private boolean isPortOutOfValidRange(final int port) {
		logger.debug("isPortOutOfValidRange started...");
		return port < CommonConstants.SYSTEM_PORT_RANGE_MIN || port > CommonConstants.SYSTEM_PORT_RANGE_MAX;
	}
	
	//-------------------------------------------------------------------------------------------------
	private void validateRelayRequestDTO(final RelayRequestDTO dto, final String origin) {
		logger.debug("validateRelayRequestDTO started...");
		
		if (dto == null) {
			throw new BadPayloadException("Address is empty", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		final boolean isAddressInvalid = Utilities.isEmpty(dto.getAddress());
		final boolean isPortInvalid = dto.getPort() == null || isPortOutOfValidRange(dto.getPort());
		final boolean isTypeInvalid = Utilities.convertStringToRelayType(dto.getType()) == null;
		if (isAddressInvalid || isPortInvalid || isTypeInvalid) {
			String exceptionMsg = "RelayRequestDTO is invalid due to the following reasons:";
			exceptionMsg = isAddressInvalid ? exceptionMsg + " address is empty, " : exceptionMsg;
			exceptionMsg = isPortInvalid ? exceptionMsg + " port is null or should be between " + CommonConstants.SYSTEM_PORT_RANGE_MIN + " and " + CommonConstants.SYSTEM_PORT_RANGE_MAX  + ",": exceptionMsg;
			exceptionMsg = isTypeInvalid ? exceptionMsg + " type '" + dto.getType() + "' is not valid," : exceptionMsg;
			exceptionMsg = exceptionMsg.substring(0, exceptionMsg.length() - 1);
			
			throw new BadPayloadException(exceptionMsg, HttpStatus.SC_BAD_REQUEST, origin);
		}		
	}
}