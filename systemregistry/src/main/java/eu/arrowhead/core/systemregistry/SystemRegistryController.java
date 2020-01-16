package eu.arrowhead.core.systemregistry;

import eu.arrowhead.common.*;
import eu.arrowhead.common.core.CoreSystem;
import eu.arrowhead.common.dto.internal.*;
import eu.arrowhead.common.dto.shared.*;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.core.systemregistry.database.service.SystemRegistryDBService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeParseException;

@Api(tags = { CoreCommonConstants.SWAGGER_TAG_ALL })
@CrossOrigin(maxAge = Defaults.CORS_MAX_AGE, allowCredentials = Defaults.CORS_ALLOW_CREDENTIALS, 
			 allowedHeaders = { HttpHeaders.ORIGIN, HttpHeaders.CONTENT_TYPE, HttpHeaders.ACCEPT, HttpHeaders.AUTHORIZATION }
)
@RestController
@RequestMapping(CommonConstants.SYSTEM_REGISTRY_URI)
public class SystemRegistryController
{

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

	private static final String GET_DEVICE_BY_ID_HTTP_200_MESSAGE = "Device by requested id returned";
	private static final String GET_DEVICE_BY_ID_HTTP_400_MESSAGE = "No Such Device by requested id";
	private static final String DEVICE_BY_ID_URI = CoreCommonConstants.MGMT_URI + "/device/{" + PATH_VARIABLE_ID + "}";
	private static final String DEVICES_URI = CoreCommonConstants.MGMT_URI + "/devices";
	private static final String DEVICES_BY_ID_URI = CoreCommonConstants.MGMT_URI + "/devices/{" + PATH_VARIABLE_ID + "}";
	private static final String GET_DEVICES_HTTP_200_MESSAGE = "Devices returned";
	private static final String GET_DEVICES_HTTP_400_MESSAGE = " Invalid parameters";
	private static final String POST_DEVICE_HTTP_201_MESSAGE = "Device created";
	private static final String POST_DEVICE_HTTP_400_MESSAGE = "Could not create device";
	private static final String PUT_DEVICE_HTTP_200_MESSAGE = "Device updated";
	private static final String PUT_DEVICE_HTTP_400_MESSAGE = "Could not update device";
	private static final String PATCH_DEVICE_HTTP_200_MESSAGE = "Device updated";
	private static final String PATCH_DEVICE_HTTP_400_MESSAGE = "Could not update device";
	private static final String DELETE_DEVICE_HTTP_200_MESSAGE = "Device deleted";
	private static final String DELETE_DEVICE_HTTP_400_MESSAGE = "Could not delete device";

	private static final String POST_SYSTEMS_HTTP_201_MESSAGE = "System definition created";
	private static final String POST_SYSTEMS_HTTP_400_MESSAGE = "Could not create system definition";
	private static final String PUT_SYSTEMS_HTTP_200_MESSAGE = "System definition updated";
	private static final String PUT_SYSTEMS_HTTP_400_MESSAGE = "Could not update system definition";
	private static final String PATCH_SYSTEMS_HTTP_200_MESSAGE = "System definition updated";
	private static final String PATCH_SYSTEMS_HTTP_400_MESSAGE = "Could not update system definition";
	private static final String DELETE_SYSTEMS_HTTP_200_MESSAGE = "System definition removed";
	private static final String DELETE_SYSTEMS_HTTP_400_MESSAGE = "Could not remove system definition";
	
	private static final String SYSTEM_REGISTRY_REGISTER_DESCRIPTION = "Registers a system";
	private static final String SYSTEM_REGISTRY_REGISTER_201_MESSAGE = "System registered";
	private static final String SYSTEM_REGISTRY_REGISTER_400_MESSAGE = "Could not register system";
	private static final String SYSTEM_REGISTRY_UNREGISTER_DESCRIPTION = "Remove a registered system";
	private static final String SYSTEM_REGISTRY_UNREGISTER_200_MESSAGE = "Registered system removed";
	private static final String SYSTEM_REGISTRY_UNREGISTER_400_MESSAGE = "Could not remove system";
	private static final String SYSTEM_REGISTRY_QUERY_DESCRIPTION = "Return System Registry data that fits the specification";
	private static final String SYSTEM_REGISTRY_QUERY_200_MESSAGE = "System Registry data returned";
	private static final String SYSTEM_REGISTRY_QUERY_400_MESSAGE = "Could not query System Registry";
	private static final String SYSTEM_REGISTRY_QUERY_BY_SYSTEM_ID_DESCRIPTION = "Return system by requested id";
	private static final String SYSTEM_REGISTRY_QUERY_BY_SYSTEM_ID_200_MESSAGE = "System data by id returned";
	private static final String SYSTEM_REGISTRY_QUERY_BY_SYSTEM_ID_400_MESSAGE = "Could not query System Registry by Consumer system id";
	private static final String SYSTEM_REGISTRY_QUERY_BY_SYSTEM_DTO_DESCRIPTION = "Return System by requested dto";
	private static final String SYSTEM_REGISTRY_QUERY_BY_SYSTEM_DTO_200_MESSAGE = "Consumer System data by requestDTO returned";
	private static final String SYSTEM_REGISTRY_QUERY_BY_SYSTEM_DTO_400_MESSAGE = "Could not query System Registry by Consumer system requestDTO";
	private static final String SYSTEM_REGISTRY_UPDATE_DESCRIPTION = "Update a system";
	private static final String SYSTEM_REGISTRY_UPDATE_200_MESSAGE = "System updated";
	private static final String SYSTEM_REGISTRY_UPDATE_400_MESSAGE = "Could not update system";
	private static final String SYSTEM_REGISTRY_MERGE_DESCRIPTION = "Merge/Patch a system";
	private static final String SYSTEM_REGISTRY_MERGE_200_MESSAGE = "System merged";
	private static final String SYSTEM_REGISTRY_MERGE_400_MESSAGE = "Could not merge system";	
	
	private static final String NOT_VALID_PARAMETERS_ERROR_MESSAGE = "Not valid request parameters.";
	private static final String ID_NOT_VALID_ERROR_MESSAGE = "Id must be greater than 0. ";
	private static final String SYSTEM_NAME_NULL_ERROR_MESSAGE = " System name must have value ";
	private static final String SYSTEM_ADDRESS_NULL_ERROR_MESSAGE = " System address must have value ";
	private static final String SYSTEM_PORT_NULL_ERROR_MESSAGE = " System port must have value ";
	
	private static final String SYSTEM_REGISTRY_MGMT_BY_ID_URI = CoreCommonConstants.MGMT_URI + "/{" + PATH_VARIABLE_ID + "}";
	private static final String PATH_VARIABLE_SERVICE_DEFINITION = "systemDefinition";
	private static final String SYSTEM_REGISTRY_MGMT_BY_SERVICE_DEFINITION_URI = CoreCommonConstants.MGMT_URI + "/systemdef" + "/{" + PATH_VARIABLE_SERVICE_DEFINITION + "}";	
	private static final String SYSTEM_REGISTRY_MGMT_GROUPED_URI = CoreCommonConstants.MGMT_URI + "/grouped";
	private static final String GET_SYSTEM_REGISTRY_HTTP_200_MESSAGE = "System Registry entries returned";
	private static final String GET_SYSTEM_REGISTRY_HTTP_400_MESSAGE = "Could not retrieve system registry entries";
	private static final String DELETE_SYSTEM_REGISTRY_HTTP_200_MESSAGE = "System Registry entry removed";
	private static final String DELETE_SYSTEM_REGISTRY_HTTP_400_MESSAGE = "Could not remove system registry entry";
	
	private final Logger logger = LogManager.getLogger(SystemRegistryController.class);

	@Autowired
	private SystemRegistryDBService systemRegistryDBService;
	

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return an echo message with the purpose of testing the core system availability", response = String.class, tags = { CoreCommonConstants.SWAGGER_TAG_CLIENT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = CoreCommonConstants.SWAGGER_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path = CommonConstants.ECHO_URI)
	public String echoSystem() {
		return "Got it!";
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return system by id", response = SystemResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = GET_SYSTEM_BY_ID_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = GET_SYSTEM_BY_ID_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(SYSTEM_BY_ID_URI)
	@ResponseBody public SystemResponseDTO getSystemById(@PathVariable(value = PATH_VARIABLE_ID) final long systemId) {		
		logger.debug("getSystemById started ...");
		
		if (systemId < 1) {
			throw new BadPayloadException(ID_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, CommonConstants.SYSTEM_REGISTRY_URI + SYSTEM_BY_ID_URI);
		}

		return systemRegistryDBService.getSystemById(systemId);
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

		final CoreUtilities.ValidatedPageParams pageParameters = CoreUtilities.validatePageParameters(page, size, direction, CommonConstants.SYSTEM_REGISTRY_URI + DEVICES_URI);
		return systemRegistryDBService.getSystemEntries(pageParameters, sortField);
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return created system", response = SystemResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_CREATED, message = POST_SYSTEM_HTTP_201_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = POST_SYSTEM_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})	
	@PostMapping(path = SYSTEMS_URI, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(org.springframework.http.HttpStatus.CREATED)
	@ResponseBody public SystemResponseDTO addSystem(@RequestBody final SystemRequestDTO request) {
		return callCreateSystem(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return updated system", response = SystemResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
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
	@ApiOperation(value = "Return system updated by fields", response = SystemResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
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
			throw new BadPayloadException(ID_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, CommonConstants.SYSTEM_REGISTRY_URI + SYSTEMS_BY_ID_URI);
		}
		
		systemRegistryDBService.removeSystemById(id);
		logger.debug("System with id: '{}' successfully deleted", id);
	}

	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return device by id", response = DeviceResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = GET_DEVICE_BY_ID_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = GET_DEVICE_BY_ID_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(SYSTEMS_BY_ID_URI)
	@ResponseBody public DeviceResponseDTO getDeviceById(@PathVariable(value = PATH_VARIABLE_ID) final long deviceId) {
		logger.debug("getDeviceById started ...");

		if (deviceId < 1) {
			throw new BadPayloadException(ID_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, CommonConstants.SYSTEM_REGISTRY_URI + SYSTEM_BY_ID_URI);
		}

		return systemRegistryDBService.getDeviceById(deviceId);
	}

	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return requested device by the given parameters", response = DeviceListResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = GET_SYSTEMS_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = GET_SYSTEMS_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path = DEVICES_URI, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public DeviceListResponseDTO getDeviceListByRequestParameters(
			@RequestParam(name = CoreCommonConstants.REQUEST_PARAM_PAGE, required = false) final Integer page,
			@RequestParam(name = CoreCommonConstants.REQUEST_PARAM_ITEM_PER_PAGE, required = false) final Integer size,
			@RequestParam(name = CoreCommonConstants.REQUEST_PARAM_DIRECTION, defaultValue = CoreDefaults.DEFAULT_REQUEST_PARAM_DIRECTION_VALUE) final String direction,
			@RequestParam(name = CoreCommonConstants.REQUEST_PARAM_SORT_FIELD, defaultValue = CoreCommonConstants.COMMON_FIELD_NAME_ID) final String sortField) {
		logger.debug("New DeviceList get request received with page: {} and item_per page: {}", page, size);

		final CoreUtilities.ValidatedPageParams pageParameters = CoreUtilities.validatePageParameters(page, size, direction, CommonConstants.SYSTEM_REGISTRY_URI + DEVICES_URI);
		final DeviceListResponseDTO deviceListResponseDTO = systemRegistryDBService.getDeviceEntries(pageParameters, sortField);
		logger.debug("DeviceList with page: {} and item_per page: {} successfully retrieved", page, size);
		
		return deviceListResponseDTO;
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return created device", response = DeviceResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_CREATED, message = POST_DEVICE_HTTP_201_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = POST_DEVICE_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@PostMapping(path = DEVICES_URI, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(value = org.springframework.http.HttpStatus.CREATED)
	@ResponseBody public DeviceResponseDTO addDevice(@RequestBody final DeviceRequestDTO dto) {
		logger.debug("New device registration request received with name: {}", dto.getDeviceName());

		validateDevice(dto);

		final DeviceResponseDTO responseDTO = systemRegistryDBService.createDevice(dto.getDeviceName(), dto.getAddress(), dto.getMacAddress(), dto.getAuthenticationInfo());
		logger.debug("{} successfully registered.", responseDTO);
		
		return responseDTO;
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return updated device", response = DeviceResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = PUT_DEVICE_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = PUT_DEVICE_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@PutMapping(path = DEVICE_BY_ID_URI, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public DeviceResponseDTO putUpdateDevice(@PathVariable(value = PATH_VARIABLE_ID) final long id,
																				 @RequestBody final DeviceRequestDTO dto) {
		logger.debug("New device update request received with id: {}, definition: {}", id, dto);
		
		if (id < 1) {
			throw new BadPayloadException(ID_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, CommonConstants.SYSTEM_REGISTRY_URI + DEVICE_BY_ID_URI);
		}

		validateDevice(dto);

		final DeviceResponseDTO deviceResponseDTO = systemRegistryDBService.updateDeviceByIdResponse(id, dto.getDeviceName(), dto.getAddress(), dto.getMacAddress(), dto.getAuthenticationInfo());
		logger.debug("Device with id: '{}' successfully updated with definition '{}'.", id, deviceResponseDTO);
		
		return deviceResponseDTO;
	}

	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Remove device", tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = DELETE_SYSTEMS_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = DELETE_SYSTEMS_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@DeleteMapping(path = DEVICE_BY_ID_URI)
	public void removeDevice(@PathVariable(value = PATH_VARIABLE_ID) final long id) {
		logger.debug("New System Definition delete request received with id: {}", id);
		
		if (id < 1) {
			throw new BadPayloadException(ID_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, CommonConstants.SYSTEM_REGISTRY_URI + DEVICE_BY_ID_URI);
		}
		
		systemRegistryDBService.removeDeviceById(id);
		logger.debug("Device with id: '{}' successfully deleted", id);
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return requested system registry entries by the given parameters", response = SystemRegistryListResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = GET_SYSTEM_REGISTRY_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = GET_SYSTEM_REGISTRY_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path = CoreCommonConstants.MGMT_URI)
	@ResponseBody public SystemRegistryListResponseDTO getSystemRegistryEntries(
			@RequestParam(name = CoreCommonConstants.REQUEST_PARAM_PAGE, required = false) final Integer page,
			@RequestParam(name = CoreCommonConstants.REQUEST_PARAM_ITEM_PER_PAGE, required = false) final Integer size,
			@RequestParam(name = CoreCommonConstants.REQUEST_PARAM_DIRECTION, defaultValue = CoreDefaults.DEFAULT_REQUEST_PARAM_DIRECTION_VALUE) final String direction,
			@RequestParam(name = CoreCommonConstants.REQUEST_PARAM_SORT_FIELD, defaultValue = CoreCommonConstants.COMMON_FIELD_NAME_ID) final String sortField) {
		logger.debug("New System Registry get request received with page: {} and item_per page: {}", page, size);

		final CoreUtilities.ValidatedPageParams params = CoreUtilities.validatePageParameters(page, size, direction, CommonConstants.SYSTEM_REGISTRY_URI + CoreCommonConstants.MGMT_URI);
		final SystemRegistryListResponseDTO systemRegistryEntriesResponse = systemRegistryDBService
				.getSystemRegistryEntriesResponse(params, sortField);
		logger.debug("System Registry entries with page: {} and item_per page: {} successfully retrieved", page, size);
		
		return systemRegistryEntriesResponse;
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return requested system registry entry", response = SystemRegistryResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = GET_SYSTEM_REGISTRY_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = GET_SYSTEM_REGISTRY_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path =  SYSTEM_REGISTRY_MGMT_BY_ID_URI)
	@ResponseBody public SystemRegistryResponseDTO getSystemRegistryEntryById(@PathVariable(value = PATH_VARIABLE_ID) final long id) {
		logger.debug("New System Registry get request received with id: {}", id);
		
		if (id < 1) {
			throw new BadPayloadException(ID_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, CommonConstants.SYSTEM_REGISTRY_URI + SYSTEM_REGISTRY_MGMT_BY_ID_URI);
		}
		final SystemRegistryResponseDTO systemRegistryEntryByIdResponse = systemRegistryDBService.getSystemRegistryEntryByIdResponse(id);
		logger.debug("System Registry entry with id: {} successfully retrieved", id);
		
		return systemRegistryEntryByIdResponse;
	}	
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return requested system registry entries by system definition based on the given parameters", response = SystemRegistryListResponseDTO.class,
				  tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = GET_SYSTEM_REGISTRY_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = GET_SYSTEM_REGISTRY_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path =  SYSTEM_REGISTRY_MGMT_BY_SERVICE_DEFINITION_URI)
	@ResponseBody public SystemRegistryListResponseDTO getSystemRegistryEntriesBySystemName(
			@PathVariable(value = PATH_VARIABLE_SERVICE_DEFINITION) final String systemName,
			@RequestParam(name = CoreCommonConstants.REQUEST_PARAM_PAGE, required = false) final Integer page,
			@RequestParam(name = CoreCommonConstants.REQUEST_PARAM_ITEM_PER_PAGE, required = false) final Integer size,
			@RequestParam(name = CoreCommonConstants.REQUEST_PARAM_DIRECTION, defaultValue = CoreDefaults.DEFAULT_REQUEST_PARAM_DIRECTION_VALUE) final String direction,
			@RequestParam(name = CoreCommonConstants.REQUEST_PARAM_SORT_FIELD, defaultValue = CoreCommonConstants.COMMON_FIELD_NAME_ID) final String sortField) {
		logger.debug("New System Registry get by System Definition request received with page: {} and item_per page: {}", page, size);
		
		if (Utilities.isEmpty(systemName)) {
			throw new BadPayloadException("System name cannot be empty.", HttpStatus.SC_BAD_REQUEST, CommonConstants.SYSTEM_REGISTRY_URI + SYSTEM_REGISTRY_MGMT_BY_SERVICE_DEFINITION_URI);
		}

		// TODO get system registry entries with given system name

		int validatedPage;
		int validatedSize;
		if (page == null && size == null) {
			validatedPage = -1;
			validatedSize = -1;
		} else {
			if (page == null || size == null) {
				throw new BadPayloadException("Defined page or size could not be with undefined size or page.", HttpStatus.SC_BAD_REQUEST, CommonConstants.SYSTEM_REGISTRY_URI +
											  SYSTEM_REGISTRY_MGMT_BY_SERVICE_DEFINITION_URI);
			} else {
				validatedPage = page;
				validatedSize = size;
			}
		}
		
		final Direction validatedDirection = CoreUtilities.calculateDirection(direction, CommonConstants.SYSTEM_REGISTRY_URI + SYSTEM_REGISTRY_MGMT_BY_SERVICE_DEFINITION_URI);
		final SystemRegistryListResponseDTO systemRegistryEntries = systemRegistryDBService
				.getSystemRegistryEntriesBySystemDefinitionResponse(systemDefinition, validatedPage, validatedSize,
																	  validatedDirection, sortField);
		logger.debug("System Registry entries with page: {} and item_per page: {} successfully retrieved", page, size);
		
		return systemRegistryEntries;
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Remove the specified system registry entry", tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = DELETE_SYSTEM_REGISTRY_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = DELETE_SYSTEM_REGISTRY_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@DeleteMapping(path = SYSTEM_REGISTRY_MGMT_BY_ID_URI)
	public void removeSystemRegistryEntryById(@PathVariable(value = PATH_VARIABLE_ID) final long id) {
		logger.debug("New System Registry delete request received with id: {}", id);
		
		if (id < 1) {
			throw new BadPayloadException(ID_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, CommonConstants.SYSTEM_REGISTRY_URI + SYSTEM_REGISTRY_MGMT_BY_ID_URI);
		}
		
		systemRegistryDBService.removeSystemRegistryEntryById(id);
		logger.debug("System Registry with id: '{}' successfully deleted", id);
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = SYSTEM_REGISTRY_REGISTER_DESCRIPTION, response = SystemRegistryResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_CLIENT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_CREATED, message = SYSTEM_REGISTRY_REGISTER_201_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = SYSTEM_REGISTRY_REGISTER_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@ResponseStatus(value = org.springframework.http.HttpStatus.CREATED)
	@PostMapping(path = CommonConstants.OP_SYSTEM_REGISTRY_REGISTER_URI, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public SystemRegistryResponseDTO registerSystem(@RequestBody final SystemRegistryRequestDTO request) {
		logger.debug("New system registration request received");
		checkSystemRegistryRequest(request, false);
		
		final SystemRegistryResponseDTO response = systemRegistryDBService.registerSystemResponse(request);
		logger.debug("{} successfully registers its system {}", request.getProviderSystem().getSystemName(), request.getSystemDefinition());
	
		return response;
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = SYSTEM_REGISTRY_REGISTER_DESCRIPTION, response = SystemRegistryResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_CREATED, message = SYSTEM_REGISTRY_REGISTER_201_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = SYSTEM_REGISTRY_REGISTER_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@ResponseStatus(value = org.springframework.http.HttpStatus.CREATED)
	@PostMapping(path = CoreCommonConstants.MGMT_URI, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody SystemRegistryResponseDTO addSystemRegistry(@RequestBody final SystemRegistryRequestDTO request) {
		logger.debug("New system registration request received");
		checkSystemRegistryRequest(request, CoreCommonConstants.MGMT_URI, true);
		
		final SystemRegistryResponseDTO response = systemRegistryDBService.registerSystemResponse(request);
		logger.debug("{}'s system {} is successfully registered", request.getProviderSystem().getSystemName(), request.getSystemDefinition());
	
		return response;
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = SYSTEM_REGISTRY_UPDATE_DESCRIPTION, response = SystemRegistryResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = SYSTEM_REGISTRY_UPDATE_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = SYSTEM_REGISTRY_UPDATE_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@PutMapping(path = SYSTEM_REGISTRY_MGMT_BY_ID_URI, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody SystemRegistryResponseDTO updateSystemRegistry(@PathVariable(value = PATH_VARIABLE_ID) final long id, @RequestBody final SystemRegistryRequestDTO request) { 
		logger.debug("New system registry update request received");
		checkSystemRegistryUpdateRequest(id, request, CoreCommonConstants.MGMT_URI);
		
		final SystemRegistryResponseDTO response = systemRegistryDBService.updateSystemByIdResponse(id, request);
		logger.debug("System Registry entry {} is successfully updated with system {} and system {}", id, request.getProviderSystem().getSystemName(), request.getSystemDefinition());
	
		return response;
	}
	

	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = SYSTEM_REGISTRY_MERGE_DESCRIPTION, response = SystemRegistryResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = SYSTEM_REGISTRY_MERGE_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = SYSTEM_REGISTRY_MERGE_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@PatchMapping(path = SYSTEM_REGISTRY_MGMT_BY_ID_URI, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody SystemRegistryResponseDTO mergeSystemRegistry(@PathVariable(value = PATH_VARIABLE_ID) final long id, @RequestBody final SystemRegistryRequestDTO request) { 
		logger.debug("New system registry merge request received");
		checkSystemRegistryMergeRequest(id, request, CoreCommonConstants.MGMT_URI);
		
		final SystemRegistryResponseDTO response = systemRegistryDBService.mergeSystemByIdResponse(id, request);
		logger.debug("System Registry entry {} is successfully merged witch system {} and system {}", id, response.getProvider().getSystemName(), request.getSystemDefinition());
	
		return response;
	}

	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = SYSTEM_REGISTRY_UNREGISTER_DESCRIPTION, tags = { CoreCommonConstants.SWAGGER_TAG_CLIENT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = SYSTEM_REGISTRY_UNREGISTER_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = SYSTEM_REGISTRY_UNREGISTER_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@DeleteMapping(path = CommonConstants.OP_SYSTEM_REGISTRY_UNREGISTER_URI)
	public void unregisterSystem(@RequestParam(CommonConstants.OP_SYSTEM_REGISTRY_UNREGISTER_REQUEST_PARAM_SERVICE_DEFINITION) final String systemName,
								  @RequestParam(CommonConstants.OP_SYSTEM_REGISTRY_UNREGISTER_REQUEST_PARAM_PROVIDER_SYSTEM_NAME) final String providerName,
								  @RequestParam(CommonConstants.OP_SYSTEM_REGISTRY_UNREGISTER_REQUEST_PARAM_PROVIDER_ADDRESS) final String providerAddress,
								  @RequestParam(CommonConstants.OP_SYSTEM_REGISTRY_UNREGISTER_REQUEST_PARAM_PROVIDER_PORT) final int providerPort) {
		logger.debug("System removal request received");
		checkUnregisterSystemParameters(systemName, providerName, providerAddress, providerPort);
		
		systemRegistryDBService.removeSystemRegistry(systemName, providerName, providerAddress, providerPort);
		logger.debug("{} successfully removed its system {}", providerName, systemName);
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = SYSTEM_REGISTRY_QUERY_DESCRIPTION, response = SystemQueryResultDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_CLIENT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = SYSTEM_REGISTRY_QUERY_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = SYSTEM_REGISTRY_QUERY_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@PostMapping(path = CommonConstants.OP_SYSTEM_REGISTRY_QUERY_URI, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public SystemQueryResultDTO queryRegistry(@RequestBody final SystemQueryFormDTO form) {
		logger.debug("System query request received");
		
		if (Utilities.isEmpty(form.getSystemDefinitionRequirement())) {
			throw new BadPayloadException("System definition requirement is null or blank" , HttpStatus.SC_BAD_REQUEST, CommonConstants.SYSTEM_REGISTRY_URI +
										  CommonConstants.OP_SYSTEM_REGISTRY_QUERY_URI);
		}
		
		final SystemQueryResultDTO result = systemRegistryDBService.queryRegistry(form);
		logger.debug("Return {} providers for system {}", result.getSystemQueryData().size(), form.getSystemDefinitionRequirement());
		
		return result;
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = SYSTEM_REGISTRY_QUERY_BY_SYSTEM_ID_DESCRIPTION, response = SystemResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_PRIVATE })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = SYSTEM_REGISTRY_QUERY_BY_SYSTEM_ID_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = SYSTEM_REGISTRY_QUERY_BY_SYSTEM_ID_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path = CoreCommonConstants.OP_SYSTEM_REGISTRY_QUERY_BY_SYSTEM_ID_URI, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public SystemResponseDTO queryRegistryBySystemId(@PathVariable(value = PATH_VARIABLE_ID) final long systemId) {
		logger.debug("System query by system id request received");
		
		if (systemId < 1) {
			throw new BadPayloadException(ID_NOT_VALID_ERROR_MESSAGE , HttpStatus.SC_BAD_REQUEST, CommonConstants.SYSTEM_REGISTRY_URI +
										  CoreCommonConstants.OP_SYSTEM_REGISTRY_QUERY_BY_SYSTEM_ID_URI);
		}
		
		final SystemResponseDTO result = systemRegistryDBService.getSystemById(systemId);

		logger.debug("Return system by id: {}", systemId);
		return result;
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = SYSTEM_REGISTRY_QUERY_BY_SYSTEM_DTO_DESCRIPTION, response = SystemResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_PRIVATE })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = SYSTEM_REGISTRY_QUERY_BY_SYSTEM_DTO_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = SYSTEM_REGISTRY_QUERY_BY_SYSTEM_DTO_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@PostMapping(path = CoreCommonConstants.OP_SYSTEM_REGISTRY_QUERY_BY_SYSTEM_DTO_URI, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public SystemResponseDTO queryRegistryBySystemDTO(@RequestBody final SystemRequestDTO request) {
		logger.debug("System query by systemRequestDTO request received");

		checkSystemRequest(request, CommonConstants.SYSTEM_REGISTRY_URI + CoreCommonConstants.OP_SYSTEM_REGISTRY_QUERY_BY_SYSTEM_ID_URI, false);
		
		final String systemName = request.getSystemName();
		final String address = request.getAddress();
		final int port = request.getPort();
		
		final SystemResponseDTO result = systemRegistryDBService.getSystemByNameAndAddressAndPortResponse(systemName, address, port);

		logger.debug("Return system by name: {}, address: {}, port: {}", systemName, address, port);
		return result;
	}
		
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private SystemResponseDTO callCreateSystem(final SystemRequestDTO request) {
		logger.debug("callCreateSystem started...");
		
		checkSystemRequest(request, CommonConstants.SYSTEM_REGISTRY_URI + SYSTEMS_URI, true);
		
		final String systemName = request.getSystemName();
		final String address = request.getAddress();
		final int port = request.getPort();
		final String authenticationInfo = request.getAuthenticationInfo();
		
		return systemRegistryDBService.createSystemResponse(systemName, address, port, authenticationInfo);
	}
	
	//-------------------------------------------------------------------------------------------------
	private SystemResponseDTO callUpdateSystem(final SystemRequestDTO request, final long systemId) {
		logger.debug("callUpdateSystem started...");
		
		checkSystemPutRequest(request, systemId);
		
		final String validatedSystemName = request.getSystemName().toLowerCase();
		final String validatedAddress = request.getAddress().toLowerCase();
		final int validatedPort = request.getPort();
		final String validatedAuthenticationInfo = request.getAuthenticationInfo();
		
		return systemRegistryDBService.updateSystemResponse(systemId, validatedSystemName, validatedAddress, validatedPort, validatedAuthenticationInfo);
	}
	
	//-------------------------------------------------------------------------------------------------
	private SystemResponseDTO callMergeSystem(final SystemRequestDTO request, final long systemId) {		
		logger.debug("callMergeSystem started...");
		
		checkSystemMergeRequest(request, systemId);
		
		final String validatedSystemName = request.getSystemName() != null ? request.getSystemName().toLowerCase() : "";
		final String validatedAddress = request.getAddress() != null ? request.getAddress().toLowerCase() : "";
		final Integer validatedPort = request.getPort();
		final String validatedAuthenticationInfo = request.getAuthenticationInfo();
		
		return systemRegistryDBService.mergeSystemResponse(systemId, validatedSystemName, validatedAddress, validatedPort, validatedAuthenticationInfo);
	}

	//-------------------------------------------------------------------------------------------------
	private void checkSystemMergeRequest(final SystemRequestDTO request, final long systemId) {
		logger.debug("checkSystemPatchRequest started...");
		
		if (systemId <= 0) {
			throw new BadPayloadException(ID_NOT_VALID_ERROR_MESSAGE , HttpStatus.SC_BAD_REQUEST, CommonConstants.SYSTEM_REGISTRY_URI + SYSTEMS_BY_ID_URI);
		}
		
		boolean needChange = false;
		if (!Utilities.isEmpty(request.getAddress())) {
			needChange = true;
		}
		
		
		if (!Utilities.isEmpty(request.getSystemName())) {
			needChange = true;
			for (final CoreSystem coreSysteam : CoreSystem.values()) {
				if (coreSysteam.name().equalsIgnoreCase(request.getSystemName().trim())) {
					throw new BadPayloadException("System name '" + request.getSystemName() + "' is a reserved arrowhead core system name.", HttpStatus.SC_BAD_REQUEST, CommonConstants.SYSTEM_REGISTRY_URI + SYSTEMS_BY_ID_URI);
				}
			}
		}
		
		if (request.getPort() != null) {
			final int validatedPort = request.getPort();
			if (validatedPort < CommonConstants.SYSTEM_PORT_RANGE_MIN || validatedPort > CommonConstants.SYSTEM_PORT_RANGE_MAX) {
				throw new BadPayloadException("Port must be between " + CommonConstants.SYSTEM_PORT_RANGE_MIN + " and " + CommonConstants.SYSTEM_PORT_RANGE_MAX +".", HttpStatus.SC_BAD_REQUEST,
											  CommonConstants.SYSTEM_REGISTRY_URI + SYSTEMS_BY_ID_URI);
			}
			
			needChange = true;
		}
		
		if (request.getAuthenticationInfo() != null) {
			needChange = true;
		}
		
		if (!needChange) {
			throw new BadPayloadException("Patch request is empty." , HttpStatus.SC_BAD_REQUEST, CommonConstants.SYSTEM_REGISTRY_URI + SYSTEMS_BY_ID_URI);
		}
	}

	//-------------------------------------------------------------------------------------------------
	private void checkSystemPutRequest(final SystemRequestDTO request, final long systemId) {
		logger.debug("checkSystemPutRequest started...");
		
		if (systemId <= 0) {
			throw new BadPayloadException(ID_NOT_VALID_ERROR_MESSAGE , HttpStatus.SC_BAD_REQUEST, CommonConstants.SYSTEM_REGISTRY_URI + SYSTEMS_BY_ID_URI);
		}
		
		checkSystemRequest(request, CommonConstants.SYSTEM_REGISTRY_URI + SYSTEMS_BY_ID_URI, true);
	}

	//-------------------------------------------------------------------------------------------------
	private void checkSystemRequest(final SystemRequestDTO request, final String origin, final boolean checkReservedCoreSystemNames) {
		logger.debug("checkSystemRequest started...");
		
		if (request == null) {
			throw new BadPayloadException("System is null.", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		if (Utilities.isEmpty(request.getSystemName())) {
			throw new BadPayloadException(SYSTEM_NAME_NULL_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		if (checkReservedCoreSystemNames) {
			for (final CoreSystem coreSysteam : CoreSystem.values()) {
				if (coreSysteam.name().equalsIgnoreCase(request.getSystemName().trim())) {
					throw new BadPayloadException("System name '" + request.getSystemName() + "' is a reserved arrowhead core system name.", HttpStatus.SC_BAD_REQUEST, origin);
				}
			}			
		}
		
		if (Utilities.isEmpty(request.getAddress())) {
			throw new BadPayloadException(SYSTEM_ADDRESS_NULL_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		if (request.getPort() == null) {
			throw new BadPayloadException(SYSTEM_PORT_NULL_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		final int validatedPort = request.getPort();
		if (validatedPort < CommonConstants.SYSTEM_PORT_RANGE_MIN || validatedPort > CommonConstants.SYSTEM_PORT_RANGE_MAX) {
			throw new BadPayloadException("Port must be between " + CommonConstants.SYSTEM_PORT_RANGE_MIN + " and " + CommonConstants.SYSTEM_PORT_RANGE_MAX + ".", HttpStatus.SC_BAD_REQUEST, origin);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private void checkSystemRegistryRequest(final SystemRegistryRequestDTO request, final boolean checkReservedCoreSystemNames) {
		logger.debug("checkSystemRegistryRequest started...");

		final String origin = CommonConstants.SYSTEM_REGISTRY_URI + CommonConstants.OP_SYSTEM_REGISTRY_REGISTER_URI;

		checkSystemRegistryRequest(request, origin, checkReservedCoreSystemNames);
	}
	
	//-------------------------------------------------------------------------------------------------
	private void checkSystemRegistryRequest(final SystemRegistryRequestDTO request, final String origin, final boolean checkReservedCoreSystemNames) {
		logger.debug("checkSystemRegistryRequest started...");
	
		if (Utilities.isEmpty(request.getSystemDefinition())) {
			throw new BadPayloadException("System definition is null or blank", HttpStatus.SC_BAD_REQUEST, origin);
		}
	
		checkSystemRequest(request.getProviderSystem(), origin, checkReservedCoreSystemNames);
		
		if (!Utilities.isEmpty(request.getEndOfValidity())) {
			try {
				Utilities.parseUTCStringToLocalZonedDateTime(request.getEndOfValidity().trim());
			} catch (final DateTimeParseException ex) {
				throw new BadPayloadException("End of validity is specified in the wrong format. Please provide UTC time using " + Utilities.getDatetimePattern() + " pattern.",
											  HttpStatus.SC_BAD_REQUEST, origin);
			}
		}
		
		SystemSecurityType securityType = null;
		if (request.getSecure() != null) {
			for (final SystemSecurityType type : SystemSecurityType.values()) {
				if (type.name().equalsIgnoreCase(request.getSecure())) {
					securityType = type;
					break;
				}
			}
			
			if (securityType == null) {
				throw new BadPayloadException("Security type is not valid.", HttpStatus.SC_BAD_REQUEST, origin); 
			}
		} else {
			securityType = SystemSecurityType.NOT_SECURE;
		}
		
		if (securityType != SystemSecurityType.NOT_SECURE && request.getProviderSystem().getAuthenticationInfo() == null) {
			throw new BadPayloadException("Security type is in conflict with the availability of the authentication info.", HttpStatus.SC_BAD_REQUEST, origin); 
		}
		
		if (request.getInterfaces() == null || request.getInterfaces().isEmpty()) {
			throw new BadPayloadException("Interfaces list is null or empty.", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		for (final String intf : request.getInterfaces()) {
			if (!interfaceNameVerifier.isValid(intf)) {
				throw new BadPayloadException("Specified interface name is not valid: " + intf, HttpStatus.SC_BAD_REQUEST, origin);
			}
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private void checkUnregisterSystemParameters(final String systemDefinition, final String providerName, final String providerAddress, final int providerPort) {
		// parameters can't be null, but can be empty
		logger.debug("checkUnregisterSystemParameters started...");
		
		final String origin = CommonConstants.SYSTEM_REGISTRY_URI + CommonConstants.OP_SYSTEM_REGISTRY_UNREGISTER_URI;
		if (Utilities.isEmpty(systemDefinition)) {
			throw new BadPayloadException("System definition is blank", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		if (Utilities.isEmpty(providerName)) {
			throw new BadPayloadException("Name of the provider system is blank", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		if (Utilities.isEmpty(providerAddress)) {
			throw new BadPayloadException("Address of the provider system is blank", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		if (providerPort < CommonConstants.SYSTEM_PORT_RANGE_MIN || providerPort > CommonConstants.SYSTEM_PORT_RANGE_MAX) {
			throw new BadPayloadException("Port must be between " + CommonConstants.SYSTEM_PORT_RANGE_MIN + " and " + CommonConstants.SYSTEM_PORT_RANGE_MAX + ".", HttpStatus.SC_BAD_REQUEST, origin);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private void checkSystemRegistryUpdateRequest(final long id, final SystemRegistryRequestDTO request, final String origin) {
		logger.debug("checkSystemRegistryUpdateRequest started...");
		
		if (id <= 0) {
			throw new BadPayloadException(ID_NOT_VALID_ERROR_MESSAGE , HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		checkSystemRegistryRequest(request, origin, true);
		
	}

	//-------------------------------------------------------------------------------------------------
	private void validateDevice(@RequestBody final DeviceRequestDTO deviceRequestDto)
	{
		if (Utilities.isEmpty(deviceRequestDto.getDeviceName())) {
			throw new BadPayloadException("Device name is null or blank", HttpStatus.SC_BAD_REQUEST, CommonConstants.DEVICE_REGISTRY_URI + DEVICES_URI);
		}

		if (Utilities.isEmpty(deviceRequestDto.getAddress())) {
			throw new BadPayloadException("Device address is null or blank", HttpStatus.SC_BAD_REQUEST, CommonConstants.DEVICE_REGISTRY_URI + DEVICES_URI);
		}

		if (Utilities.isEmpty(deviceRequestDto.getMacAddress())) {
			throw new BadPayloadException("Device MAC address is null or blank", HttpStatus.SC_BAD_REQUEST, CommonConstants.DEVICE_REGISTRY_URI + DEVICES_URI);
		}
	}

	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S3776")
	private void checkSystemRegistryMergeRequest(final long id, final SystemRegistryRequestDTO request, final String origin) {
		logger.debug("checkSystemRegistryMergeRequest started...");
		
		if (id <= 0) {
			throw new BadPayloadException(ID_NOT_VALID_ERROR_MESSAGE , HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		boolean needChange = false;
		if (request.getProviderSystem() != null && !Utilities.isEmpty(request.getProviderSystem().getAddress())) {
			needChange = true;
		}
		
		if (request.getProviderSystem() != null && !Utilities.isEmpty(request.getProviderSystem().getSystemName())) {
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
		
		if (request.getSystemDefinition() != null) {
			needChange = true;
		}
		
		if (request.getSystemUri() != null) {
			needChange = true;
		}
		
		if (request.getVersion() != null) {
			needChange = true;
		}
		
		if (!needChange) {
			throw new BadPayloadException("Patch request is empty." , HttpStatus.SC_BAD_REQUEST, origin);
		}
	}
}