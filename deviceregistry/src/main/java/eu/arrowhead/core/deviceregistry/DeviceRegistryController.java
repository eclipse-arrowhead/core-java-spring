package eu.arrowhead.core.deviceregistry;

import eu.arrowhead.common.*;
import eu.arrowhead.common.dto.internal.DeviceListResponseDTO;
import eu.arrowhead.common.dto.internal.DeviceRegistryListResponseDTO;
import eu.arrowhead.common.dto.shared.*;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.core.deviceregistry.database.service.DeviceRegistryDBService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeParseException;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Api(tags = {CoreCommonConstants.SWAGGER_TAG_ALL})
@CrossOrigin(maxAge = Defaults.CORS_MAX_AGE, allowCredentials = Defaults.CORS_ALLOW_CREDENTIALS,
        allowedHeaders = {HttpHeaders.ORIGIN, HttpHeaders.CONTENT_TYPE, HttpHeaders.ACCEPT, HttpHeaders.AUTHORIZATION}
)
@RestController
@RequestMapping(CommonConstants.DEVICE_REGISTRY_URI)
public class DeviceRegistryController
{

    //=================================================================================================
    // members

    private static final String GET_DEVICE_BY_ID_HTTP_200_MESSAGE = "Device by requested deviceId returned";
    private static final String GET_DEVICE_BY_ID_HTTP_400_MESSAGE = "No Such Device by requested deviceId";
    private static final String PATH_VARIABLE_ID = "deviceId";
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

    private static final String GET_DEVICE_REGISTRY_HTTP_200_MESSAGE = "Device Registry entries returned";
    private static final String GET_DEVICE_REGISTRY_HTTP_400_MESSAGE = "Could not retrieve device registry entries";
    private static final String DELETE_DEVICE_REGISTRY_HTTP_200_MESSAGE = "Device Registry entry removed";
    private static final String DELETE_DEVICE_REGISTRY_HTTP_400_MESSAGE = "Could not remove device registry entry";

    private static final String DEVICE_BY_ID_URI = CoreCommonConstants.MGMT_URI + "/device/{" + PATH_VARIABLE_ID + "}";
    private static final String DEVICES_URI = CoreCommonConstants.MGMT_URI + "/devices";
    private static final String DEVICES_BY_ID_URI = CoreCommonConstants.MGMT_URI + "/devices/{" + PATH_VARIABLE_ID + "}";

    private static final String DEVICE_REGISTRY_REGISTER_DESCRIPTION = "Registers a device";
    private static final String DEVICE_REGISTRY_REGISTER_201_MESSAGE = "Device registered";
    private static final String DEVICE_REGISTRY_REGISTER_400_MESSAGE = "Could not register device";
    private static final String DEVICE_REGISTRY_UNREGISTER_DESCRIPTION = "Remove a registered device";
    private static final String DEVICE_REGISTRY_UNREGISTER_200_MESSAGE = "Registered device removed";
    private static final String DEVICE_REGISTRY_UNREGISTER_400_MESSAGE = "Could not remove device";
    private static final String DEVICE_REGISTRY_QUERY_DESCRIPTION = "Return Device Registry data that fits the specification";
    private static final String DEVICE_REGISTRY_QUERY_200_MESSAGE = "Device Registry data returned";
    private static final String DEVICE_REGISTRY_QUERY_400_MESSAGE = "Could not query Device Registry";
    private static final String DEVICE_REGISTRY_QUERY_BY_DEVICE_ID_DESCRIPTION = "Return device by requested deviceId";
    private static final String DEVICE_REGISTRY_QUERY_BY_DEVICE_ID_200_MESSAGE = "Device data by deviceId returned";
    private static final String DEVICE_REGISTRY_QUERY_BY_DEVICE_ID_400_MESSAGE = "Could not query Device Registry by Consumer device deviceId";
    private static final String DEVICE_REGISTRY_QUERY_BY_DEVICE_DTO_DESCRIPTION = "Return Device by requested dto";
    private static final String DEVICE_REGISTRY_QUERY_BY_DEVICE_DTO_200_MESSAGE = "Consumer Device data by requestDTO returned";
    private static final String DEVICE_REGISTRY_QUERY_BY_DEVICE_DTO_400_MESSAGE = "Could not query Device Registry by Consumer device requestDTO";
    private static final String DEVICE_REGISTRY_UPDATE_DESCRIPTION = "Update a device";
    private static final String DEVICE_REGISTRY_UPDATE_200_MESSAGE = "Device updated";
    private static final String DEVICE_REGISTRY_UPDATE_400_MESSAGE = "Could not update device";
    private static final String DEVICE_REGISTRY_MERGE_DESCRIPTION = "Merge/Patch a device";
    private static final String DEVICE_REGISTRY_MERGE_200_MESSAGE = "Device merged";
    private static final String DEVICE_REGISTRY_MERGE_400_MESSAGE = "Could not merge device";

    private static final String ID_NOT_VALID_ERROR_MESSAGE = "Id must be greater than 0. ";
    private static final String DEVICE_NAME_NULL_ERROR_MESSAGE = " Device name must have value ";
    private static final String DEVICE_ADDRESS_NULL_ERROR_MESSAGE = " Device address must have value ";
    private static final String DEVICE_MAC_ADDRESS_NULL_ERROR_MESSAGE = " Device MAC address must have value ";

    private static final String PATH_VARIABLE_DEVICE_NAME = "deviceName";
    private static final String DEVICE_REGISTRY_MGMT_BY_ID_URI = CoreCommonConstants.MGMT_URI + "/{" + PATH_VARIABLE_ID + "}";
    private static final String DEVICE_REGISTRY_MGMT_BY_DEVICE_NAME_URI = CoreCommonConstants.MGMT_URI + "/devicename" + "/{" + PATH_VARIABLE_DEVICE_NAME + "}";

    private static final String MAC_ADDRESS_PATTERN = "^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$";
    private final Pattern macAddressPattern = Pattern.compile(MAC_ADDRESS_PATTERN);

    private final Logger logger = LogManager.getLogger(DeviceRegistryController.class);
    private final DeviceRegistryDBService deviceRegistryDBService;

    @Autowired
    public DeviceRegistryController(final DeviceRegistryDBService deviceRegistryDBService) {
        this.deviceRegistryDBService = deviceRegistryDBService;
    }


    //=================================================================================================
    // methods

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = "Return an echo message with the purpose of testing the core device availability", response = String.class, tags = {CoreCommonConstants.SWAGGER_TAG_CLIENT})
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = CoreCommonConstants.SWAGGER_HTTP_200_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @GetMapping(path = CommonConstants.ECHO_URI)
    public String echoDevice() {
        return "Got it!";
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = "Return device by deviceId", response = DeviceResponseDTO.class, tags = {CoreCommonConstants.SWAGGER_TAG_MGMT})
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = GET_DEVICE_BY_ID_HTTP_200_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = GET_DEVICE_BY_ID_HTTP_400_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @GetMapping(DEVICE_BY_ID_URI)
    @ResponseBody
    public DeviceResponseDTO getDeviceById(@PathVariable(value = PATH_VARIABLE_ID) final long deviceId) {
        logger.debug("getDeviceById started ...");

        if (deviceId < 1) {
            throw new BadPayloadException(ID_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, CommonConstants.DEVICE_REGISTRY_URI + DEVICE_BY_ID_URI);
        }

        return deviceRegistryDBService.getDeviceById(deviceId);
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = "Return devices by request parameters", response = DeviceListResponseDTO.class, tags = {CoreCommonConstants.SWAGGER_TAG_MGMT})
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = GET_DEVICES_HTTP_200_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = GET_DEVICES_HTTP_400_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @GetMapping(DEVICES_URI)
    @ResponseBody
    public DeviceListResponseDTO getDevices(
            @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_PAGE, required = false) final Integer page,
            @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_ITEM_PER_PAGE, required = false) final Integer size,
            @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_DIRECTION, defaultValue = CoreDefaults.DEFAULT_REQUEST_PARAM_DIRECTION_VALUE) final String direction,
            @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_SORT_FIELD, defaultValue = CoreCommonConstants.COMMON_FIELD_NAME_ID) final String sortField) {
        logger.debug("getDevices started ...");

        final CoreUtilities.ValidatedPageParams pageParameters = CoreUtilities.validatePageParameters(page, size, direction, CommonConstants.DEVICE_REGISTRY_URI + DEVICES_URI);
        return deviceRegistryDBService.getDeviceEntries(pageParameters, sortField);
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = "Return created device", response = DeviceResponseDTO.class, tags = {CoreCommonConstants.SWAGGER_TAG_MGMT})
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_CREATED, message = POST_DEVICE_HTTP_201_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = POST_DEVICE_HTTP_400_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @PostMapping(path = DEVICES_URI, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
    @ResponseBody
    public DeviceResponseDTO addDevice(@RequestBody final DeviceRequestDTO dto) {
        logger.debug("New device registration request received with name: {}", dto.getDeviceName());

        checkDeviceRequest(dto, CommonConstants.DEVICE_REGISTRY_URI + DEVICES_URI);

        final DeviceResponseDTO responseDTO = deviceRegistryDBService.createDeviceDto(dto.getDeviceName(), dto.getAddress(), dto.getMacAddress(), dto.getAuthenticationInfo());
        logger.debug("{} successfully registered.", responseDTO);

        return responseDTO;
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = "Return updated device", response = DeviceResponseDTO.class, tags = {CoreCommonConstants.SWAGGER_TAG_MGMT})
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_CREATED, message = PUT_DEVICE_HTTP_200_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = PUT_DEVICE_HTTP_400_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @PutMapping(path = DEVICES_BY_ID_URI, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public DeviceResponseDTO updateDevice(@PathVariable(value = PATH_VARIABLE_ID) final long deviceId, @RequestBody final DeviceRequestDTO dto) {
        logger.debug("New device update request received with deviceId: {}, definition: {}", deviceId, dto);

        final String origin = CommonConstants.DEVICE_REGISTRY_URI + DEVICE_BY_ID_URI;
        if (deviceId < 1) {
            throw new BadPayloadException(ID_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
        }

        checkDevicePutRequest(dto, deviceId);

        final DeviceResponseDTO deviceResponseDTO = deviceRegistryDBService
                .updateDeviceByIdResponse(deviceId, dto.getDeviceName(), dto.getAddress(), dto.getMacAddress(), dto.getAuthenticationInfo());
        logger.debug("Device with deviceId: '{}' successfully updated with definition '{}'.", deviceId, deviceResponseDTO);

        return deviceResponseDTO;
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = "Return device updated by fields", response = DeviceResponseDTO.class, tags = {CoreCommonConstants.SWAGGER_TAG_MGMT})
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_CREATED, message = PATCH_DEVICE_HTTP_200_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = PATCH_DEVICE_HTTP_400_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @PatchMapping(path = DEVICES_BY_ID_URI, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public DeviceResponseDTO mergeDevice(@PathVariable(value = PATH_VARIABLE_ID) final long deviceId, @RequestBody final DeviceRequestDTO request) {
        logger.debug("mergeDevice started...");

        checkDeviceMergeRequest(request, deviceId);
        return deviceRegistryDBService.mergeDevice(deviceId, request);
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = "Remove device", tags = {CoreCommonConstants.SWAGGER_TAG_MGMT})
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = DELETE_DEVICE_HTTP_200_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = DELETE_DEVICE_HTTP_400_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @DeleteMapping(path = DEVICES_BY_ID_URI)
    public void removeDevice(@PathVariable(value = PATH_VARIABLE_ID) final long id) {
        logger.debug("New Device delete request received with deviceId: {}", id);

        if (id < 1) {
            throw new BadPayloadException(ID_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, CommonConstants.DEVICE_REGISTRY_URI + DEVICES_BY_ID_URI);
        }

        deviceRegistryDBService.removeDeviceById(id);
        logger.debug("Device with deviceId: '{}' successfully deleted", id);
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = "Return requested device by the given parameters", response = DeviceListResponseDTO.class, tags = {CoreCommonConstants.SWAGGER_TAG_MGMT})
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = GET_DEVICES_HTTP_200_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = GET_DEVICES_HTTP_400_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @GetMapping(path = DEVICES_URI, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public DeviceListResponseDTO getDeviceListByRequestParameters(
            @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_PAGE, required = false) final Integer page,
            @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_ITEM_PER_PAGE, required = false) final Integer size,
            @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_DIRECTION, defaultValue = CoreDefaults.DEFAULT_REQUEST_PARAM_DIRECTION_VALUE) final String direction,
            @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_SORT_FIELD, defaultValue = CoreCommonConstants.COMMON_FIELD_NAME_ID) final String sortField) {
        logger.debug("New DeviceList get request received with page: {} and item_per page: {}", page, size);

        final CoreUtilities.ValidatedPageParams pageParameters = CoreUtilities.validatePageParameters(page, size, direction, CommonConstants.DEVICE_REGISTRY_URI + DEVICES_URI);
        final DeviceListResponseDTO deviceListResponseDTO = deviceRegistryDBService.getDeviceEntries(pageParameters, sortField);
        logger.debug("DeviceList with page: {} and item_per page: {} successfully retrieved", page, size);

        return deviceListResponseDTO;
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = "Return requested device registry entries by the given parameters", response = DeviceRegistryListResponseDTO.class, tags = {CoreCommonConstants.SWAGGER_TAG_MGMT})
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = GET_DEVICE_REGISTRY_HTTP_200_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = GET_DEVICE_REGISTRY_HTTP_400_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @GetMapping(path = CoreCommonConstants.MGMT_URI)
    @ResponseBody
    public DeviceRegistryListResponseDTO getDeviceRegistryEntries(
            @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_PAGE, required = false) final Integer page,
            @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_ITEM_PER_PAGE, required = false) final Integer size,
            @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_DIRECTION, defaultValue = CoreDefaults.DEFAULT_REQUEST_PARAM_DIRECTION_VALUE) final String direction,
            @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_SORT_FIELD, defaultValue = CoreCommonConstants.COMMON_FIELD_NAME_ID) final String sortField) {
        logger.debug("New Device Registry get request received with page: {} and item_per page: {}", page, size);

        final CoreUtilities.ValidatedPageParams params = CoreUtilities.validatePageParameters(page, size, direction, CommonConstants.DEVICE_REGISTRY_URI + CoreCommonConstants.MGMT_URI);
        final DeviceRegistryListResponseDTO systemRegistryEntriesResponse = deviceRegistryDBService
                .getDeviceRegistryEntries(params, sortField);
        logger.debug("Device Registry entries with page: {} and item_per page: {} successfully retrieved", page, size);

        return systemRegistryEntriesResponse;
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = "Return requested device registry entry", response = DeviceRegistryResponseDTO.class, tags = {CoreCommonConstants.SWAGGER_TAG_MGMT})
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = GET_DEVICE_REGISTRY_HTTP_200_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = GET_DEVICE_REGISTRY_HTTP_400_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @GetMapping(path = DEVICE_REGISTRY_MGMT_BY_ID_URI)
    @ResponseBody
    public DeviceRegistryResponseDTO getDeviceRegistryEntryById(@PathVariable(value = PATH_VARIABLE_ID) final long id) {
        logger.debug("New Device Registry get request received with deviceId: {}", id);

        if (id < 1) {
            throw new BadPayloadException(ID_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, CommonConstants.DEVICE_REGISTRY_URI + DEVICE_REGISTRY_MGMT_BY_ID_URI);
        }
        final DeviceRegistryResponseDTO systemRegistryEntryByIdResponse = deviceRegistryDBService.getDeviceRegistryById(id);
        logger.debug("Device Registry entry with deviceId: {} successfully retrieved", id);

        return systemRegistryEntryByIdResponse;
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = "Return requested device registry entries by device definition based on the given parameters", response = DeviceRegistryListResponseDTO.class,
            tags = {CoreCommonConstants.SWAGGER_TAG_MGMT})
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = GET_DEVICE_REGISTRY_HTTP_200_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = GET_DEVICE_REGISTRY_HTTP_400_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @GetMapping(path = DEVICE_REGISTRY_MGMT_BY_DEVICE_NAME_URI)
    @ResponseBody
    public DeviceRegistryListResponseDTO getDeviceRegistryEntriesByDeviceName(
            @PathVariable(value = PATH_VARIABLE_DEVICE_NAME) final String deviceName,
            @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_PAGE, required = false) final Integer page,
            @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_ITEM_PER_PAGE, required = false) final Integer size,
            @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_DIRECTION, defaultValue = CoreDefaults.DEFAULT_REQUEST_PARAM_DIRECTION_VALUE) final String direction,
            @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_SORT_FIELD, defaultValue = CoreCommonConstants.COMMON_FIELD_NAME_ID) final String sortField) {
        logger.debug("New Device Registry get by Device Definition request received with page: {} and item_per page: {}", page, size);

        final String origin = CommonConstants.DEVICE_REGISTRY_URI + DEVICE_REGISTRY_MGMT_BY_DEVICE_NAME_URI;
        if (Utilities.isEmpty(deviceName)) {
            throw new BadPayloadException("Device name cannot be empty.", HttpStatus.SC_BAD_REQUEST, origin);
        }

        final CoreUtilities.ValidatedPageParams pageParameters = CoreUtilities.validatePageParameters(page, size, direction, origin);
        final DeviceRegistryListResponseDTO systemRegistryEntries = deviceRegistryDBService
                .getDeviceRegistryEntriesByDeviceName(deviceName, pageParameters, sortField);
        logger.debug("Device Registry entries with page: {} and item_per page: {} successfully retrieved", page, size);

        return systemRegistryEntries;
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = "Remove the specified device registry entry", tags = {CoreCommonConstants.SWAGGER_TAG_MGMT})
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = DELETE_DEVICE_REGISTRY_HTTP_200_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = DELETE_DEVICE_REGISTRY_HTTP_400_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @DeleteMapping(path = DEVICE_REGISTRY_MGMT_BY_ID_URI)
    public void removeDeviceRegistryEntryById(@PathVariable(value = PATH_VARIABLE_ID) final long id) {
        logger.debug("New Device Registry delete request received with deviceId: {}", id);

        if (id < 1) {
            throw new BadPayloadException(ID_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, CommonConstants.DEVICE_REGISTRY_URI + DEVICE_REGISTRY_MGMT_BY_ID_URI);
        }

        deviceRegistryDBService.removeDeviceRegistryEntryById(id);
        logger.debug("Device Registry with deviceId: '{}' successfully deleted", id);
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = DEVICE_REGISTRY_REGISTER_DESCRIPTION, response = DeviceRegistryResponseDTO.class, tags = {CoreCommonConstants.SWAGGER_TAG_CLIENT})
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_CREATED, message = DEVICE_REGISTRY_REGISTER_201_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = DEVICE_REGISTRY_REGISTER_400_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @ResponseStatus(value = org.springframework.http.HttpStatus.CREATED)
    @PostMapping(path = {CommonConstants.OP_DEVICE_REGISTRY_REGISTER_URI, CoreCommonConstants.MGMT_URI},
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public DeviceRegistryResponseDTO registerDevice(@RequestBody final DeviceRegistryRequestDTO request) {
        logger.debug("New device registration request received");
        checkDeviceRegistryRequest(request, CommonConstants.DEVICE_REGISTRY_URI + CommonConstants.OP_DEVICE_REGISTRY_REGISTER_URI);

        final DeviceRegistryResponseDTO response = deviceRegistryDBService.registerDeviceRegistry(request);
        logger.debug("{} successfully registers its device {}", request.getDevice().getDeviceName(), request.getDevice());

        return response;
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = DEVICE_REGISTRY_UPDATE_DESCRIPTION, response = DeviceRegistryResponseDTO.class, tags = {CoreCommonConstants.SWAGGER_TAG_MGMT})
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = DEVICE_REGISTRY_UPDATE_200_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = DEVICE_REGISTRY_UPDATE_400_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @PutMapping(path = DEVICE_REGISTRY_MGMT_BY_ID_URI, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody
    DeviceRegistryResponseDTO updateDeviceRegistry(@PathVariable(value = PATH_VARIABLE_ID) final long id, @RequestBody final DeviceRegistryRequestDTO request) {
        logger.debug("New device registry update request received");
        checkDeviceRegistryUpdateRequest(id, request, CoreCommonConstants.MGMT_URI);

        final DeviceRegistryResponseDTO response = deviceRegistryDBService.updateDeviceRegistryById(id, request);
        logger.debug("Device Registry entry {} is successfully updated with device {} and device {}", id, request.getDevice().getDeviceName(), request.getDevice());

        return response;
    }


    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = DEVICE_REGISTRY_MERGE_DESCRIPTION, response = DeviceRegistryResponseDTO.class, tags = {CoreCommonConstants.SWAGGER_TAG_MGMT})
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = DEVICE_REGISTRY_MERGE_200_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = DEVICE_REGISTRY_MERGE_400_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @PatchMapping(path = DEVICE_REGISTRY_MGMT_BY_ID_URI, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody
    DeviceRegistryResponseDTO mergeDeviceRegistry(@PathVariable(value = PATH_VARIABLE_ID) final long id, @RequestBody final DeviceRegistryRequestDTO request) {
        logger.debug("New device registry merge request received");
        checkDeviceRegistryMergeRequest(id, request, CoreCommonConstants.MGMT_URI);

        final DeviceRegistryResponseDTO response = deviceRegistryDBService.mergeDeviceRegistryById(id, request);
        logger.debug("Device Registry entry {} is successfully merged witch device {} and device {}", id, response.getDevice(), request.getDevice());

        return response;
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = DEVICE_REGISTRY_UNREGISTER_DESCRIPTION, tags = {CoreCommonConstants.SWAGGER_TAG_CLIENT})
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = DEVICE_REGISTRY_UNREGISTER_200_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = DEVICE_REGISTRY_UNREGISTER_400_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @DeleteMapping(path = CommonConstants.OP_DEVICE_REGISTRY_UNREGISTER_URI)
    public void unregisterDevice(@RequestParam(CommonConstants.OP_DEVICE_REGISTRY_UNREGISTER_REQUEST_PARAM_PROVIDER_DEVICE_NAME) final String deviceName,
                                 @RequestParam(CommonConstants.OP_DEVICE_REGISTRY_UNREGISTER_REQUEST_PARAM_PROVIDER_MAC_ADDRESS) final String macAddress) {
        logger.debug("Device removal request received");
        checkUnregisterDeviceParameters(deviceName, macAddress);

        deviceRegistryDBService.removeDeviceRegistryByNameAndMacAddress(deviceName, macAddress);
        logger.debug("{} successfully removed", deviceName);
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = DEVICE_REGISTRY_QUERY_DESCRIPTION, response = DeviceQueryResultDTO.class, tags = {CoreCommonConstants.SWAGGER_TAG_CLIENT})
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = DEVICE_REGISTRY_QUERY_200_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = DEVICE_REGISTRY_QUERY_400_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @PostMapping(path = CommonConstants.OP_DEVICE_REGISTRY_QUERY_URI, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public DeviceQueryResultDTO queryRegistry(@RequestBody final DeviceQueryFormDTO form) {
        logger.debug("Device query request received");

        if (Utilities.isEmpty(form.getDeviceNameRequirements())) {
            throw new BadPayloadException("Device definition requirement is null or blank", HttpStatus.SC_BAD_REQUEST, CommonConstants.DEVICE_REGISTRY_URI +
                    CommonConstants.OP_DEVICE_REGISTRY_QUERY_URI);
        }

        final DeviceQueryResultDTO result = deviceRegistryDBService.queryRegistry(form);
        logger.debug("Return {} providers for device {}", result.getDeviceQueryData().size(), form.getDeviceNameRequirements());

        return result;
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = DEVICE_REGISTRY_QUERY_BY_DEVICE_ID_DESCRIPTION, response = DeviceResponseDTO.class, tags = {CoreCommonConstants.SWAGGER_TAG_PRIVATE})
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = DEVICE_REGISTRY_QUERY_BY_DEVICE_ID_200_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = DEVICE_REGISTRY_QUERY_BY_DEVICE_ID_400_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @GetMapping(path = CoreCommonConstants.OP_DEVICE_REGISTRY_QUERY_BY_DEVICE_ID_URI, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public DeviceResponseDTO queryRegistryByDeviceId(@PathVariable(value = PATH_VARIABLE_ID) final long deviceId) {
        logger.debug("Device query by device deviceId request received");

        if (deviceId < 1) {
            throw new BadPayloadException(ID_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, CommonConstants.DEVICE_REGISTRY_URI +
                    CoreCommonConstants.OP_DEVICE_REGISTRY_QUERY_BY_DEVICE_ID_URI);
        }

        final DeviceResponseDTO result = deviceRegistryDBService.getDeviceById(deviceId);

        logger.debug("Return device by deviceId: {}", deviceId);
        return result;
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = DEVICE_REGISTRY_QUERY_BY_DEVICE_DTO_DESCRIPTION, response = DeviceResponseDTO.class, tags = {CoreCommonConstants.SWAGGER_TAG_PRIVATE})
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = DEVICE_REGISTRY_QUERY_BY_DEVICE_DTO_200_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = DEVICE_REGISTRY_QUERY_BY_DEVICE_DTO_400_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @PostMapping(path = CoreCommonConstants.OP_DEVICE_REGISTRY_QUERY_BY_DEVICE_DTO_URI, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public DeviceResponseDTO queryRegistryByDeviceDTO(@RequestBody final DeviceRequestDTO request) {
        logger.debug("Device query by systemRequestDTO request received");

        checkDeviceRequest(request, CommonConstants.DEVICE_REGISTRY_URI + CoreCommonConstants.OP_DEVICE_REGISTRY_QUERY_BY_DEVICE_ID_URI);

        final String systemName = request.getDeviceName();
        final String macAddress = request.getMacAddress();

        final DeviceResponseDTO result = deviceRegistryDBService.getDeviceDtoByNameAndMacAddress(systemName, macAddress);

        logger.debug("Return device by name: {}, macAddress: {}", systemName, macAddress);
        return result;
    }

    //=================================================================================================
    // assistant methods

    //-------------------------------------------------------------------------------------------------
    private void checkDeviceMergeRequest(final DeviceRequestDTO request, final long deviceId) {
        logger.debug("checkDevicePatchRequest started...");
        final String origin = CommonConstants.DEVICE_REGISTRY_URI + DEVICES_BY_ID_URI;

        if (deviceId <= 0) {
            throw new BadPayloadException(ID_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, CommonConstants.DEVICE_REGISTRY_URI + DEVICES_BY_ID_URI);
        }

        boolean needChange = false;
        if (Utilities.notEmpty(request.getAddress())) {
            needChange = true;
        }

        if (Utilities.notEmpty(request.getDeviceName())) {
            needChange = true;
        }

        if (Utilities.notEmpty(request.getMacAddress())) {
            needChange = true;
        }

        if (Utilities.notEmpty(request.getAuthenticationInfo())) {
            needChange = true;
        }

        if (!needChange) {
            throw new BadPayloadException("Patch request is empty.", HttpStatus.SC_BAD_REQUEST, origin);
        }
    }

    //-------------------------------------------------------------------------------------------------
    private void checkDevicePutRequest(final DeviceRequestDTO request, final long deviceId) {
        logger.debug("checkDevicePutRequest started...");
        final String origin = CommonConstants.DEVICE_REGISTRY_URI + DEVICES_BY_ID_URI;

        if (deviceId <= 0) {
            throw new BadPayloadException(ID_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
        }

        checkDeviceRequest(request, origin);
    }

    //-------------------------------------------------------------------------------------------------
    private void checkDeviceRequest(final DeviceRequestDTO request, final String origin) {
        logger.debug("checkDeviceRequest started...");

        if (request == null) {
            throw new BadPayloadException("Device is null.", HttpStatus.SC_BAD_REQUEST, origin);
        }

        if (Utilities.isEmpty(request.getDeviceName())) {
            throw new BadPayloadException(DEVICE_NAME_NULL_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
        }

        if (Utilities.isEmpty(request.getAddress())) {
            throw new BadPayloadException(DEVICE_ADDRESS_NULL_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
        }
        if (Utilities.isEmpty(request.getMacAddress())) {
            throw new BadPayloadException(DEVICE_MAC_ADDRESS_NULL_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
        }
    }

    //-------------------------------------------------------------------------------------------------
    private void checkDeviceRegistryRequest(final DeviceRegistryRequestDTO request, final String origin) {
        logger.debug("checkDeviceRegistryRequest started...");

        checkDeviceRequest(request.getDevice(), origin);

        if (!Utilities.isEmpty(request.getEndOfValidity())) {
            try {
                Utilities.parseUTCStringToLocalZonedDateTime(request.getEndOfValidity().trim());
            } catch (final DateTimeParseException ex) {
                throw new BadPayloadException("End of validity is specified in the wrong format. Please provide UTC time using " + Utilities.getDatetimePattern() + " pattern.",
                        HttpStatus.SC_BAD_REQUEST, origin);
            }
        }
    }

    //-------------------------------------------------------------------------------------------------
    private void checkUnregisterDeviceParameters(final String systemName, final String macAddress)
    {
        // parameters can't be null, but can be empty
        logger.debug("checkUnregisterDeviceParameters started...");

        final String origin = CommonConstants.DEVICE_REGISTRY_URI + CommonConstants.OP_DEVICE_REGISTRY_UNREGISTER_URI;

        if (Utilities.isEmpty(systemName))
        {
            throw new BadPayloadException("Name of the device is blank", HttpStatus.SC_BAD_REQUEST, origin);
        }

        if (Utilities.isEmpty(macAddress))
        {
            throw new BadPayloadException("MAC Address of the device is blank", HttpStatus.SC_BAD_REQUEST, origin);
        }

        final Matcher matcher = macAddressPattern.matcher(macAddress);
        if (matcher.matches())
        {
            throw new BadPayloadException("Unrecognized format of MAC Address", HttpStatus.SC_BAD_REQUEST, origin);
        }
    }

    //-------------------------------------------------------------------------------------------------
    private void checkDeviceRegistryUpdateRequest(final long id, final DeviceRegistryRequestDTO request, final String origin) {
        logger.debug("checkDeviceRegistryUpdateRequest started...");

        if (id <= 0) {
            throw new BadPayloadException(ID_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
        }

        checkDeviceRegistryRequest(request, origin);
    }

    //-------------------------------------------------------------------------------------------------
    @SuppressWarnings("squid:S3776")
    private void checkDeviceRegistryMergeRequest(final long id, final DeviceRegistryRequestDTO request, final String origin) {
        logger.debug("checkDeviceRegistryMergeRequest started...");

        if (id <= 0) {
            throw new BadPayloadException(ID_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
        }

        boolean needChange = false;

        if (Objects.nonNull(request.getDevice())) {
            final DeviceRequestDTO device = request.getDevice();

            if (Utilities.notEmpty(device.getDeviceName())) {
                needChange = true;
            } else if (Utilities.notEmpty(device.getAddress())) {
                needChange = true;
            } else if (Utilities.notEmpty(device.getMacAddress())) {
                needChange = true;
            } else if (Utilities.notEmpty(device.getAuthenticationInfo())) {
                needChange = true;
            }
        }

        if (request.getEndOfValidity() != null) {
            needChange = true;
        }

        if (request.getMetadata() != null) {
            needChange = true;
        }

        if (request.getVersion() <= 0) {
            needChange = true;
        }

        if (!needChange) {
            throw new BadPayloadException("Patch request is empty.", HttpStatus.SC_BAD_REQUEST, origin);
        }
    }
}