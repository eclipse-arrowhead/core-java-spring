/********************************************************************************
 * Copyright (c) 2020 FHB
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   FHB - implementation
 *   Arrowhead Consortia - conceptualization
 ********************************************************************************/

package eu.arrowhead.core.deviceregistry;

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
import eu.arrowhead.common.dto.internal.DeviceListResponseDTO;
import eu.arrowhead.common.dto.internal.DeviceRegistryListResponseDTO;
import eu.arrowhead.common.dto.internal.LogEntryListResponseDTO;
import eu.arrowhead.common.dto.shared.DeviceRegistryRequestDTO;
import eu.arrowhead.common.dto.shared.DeviceRegistryResponseDTO;
import eu.arrowhead.common.dto.shared.DeviceRequestDTO;
import eu.arrowhead.common.dto.shared.DeviceResponseDTO;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.core.deviceregistry.database.service.DeviceRegistryDBService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.logging.LogLevel;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
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

@Api(tags = {CoreCommonConstants.SWAGGER_TAG_ALL})
@CrossOrigin(maxAge = Defaults.CORS_MAX_AGE, allowCredentials = Defaults.CORS_ALLOW_CREDENTIALS,
        allowedHeaders = {HttpHeaders.ORIGIN, HttpHeaders.CONTENT_TYPE, HttpHeaders.ACCEPT, HttpHeaders.AUTHORIZATION}
)
@RestController
@RequestMapping(CommonConstants.DEVICEREGISTRY_URI + CoreCommonConstants.MGMT_URI)
public class DeviceRegistryManagementController {

	//=================================================================================================
	// members
	
	private static final String DEVICEREGISTRY_LOGS_URI = "/logs";
    private static final String DEVICEREGISTRY_BY_ID_URI = "/{" + Constants.PATH_VARIABLE_ID + "}";
    private static final String DEVICEREGISTRY_BY_DEVICE_NAME_URI = "/devicename" + "/{" + Constants.PATH_VARIABLE_DEVICE_NAME + "}";

    private static final String GET_DEVICEREGISTRY_HTTP_200_MESSAGE = "Device Registry entries returned";
    private static final String GET_DEVICEREGISTRY_HTTP_400_MESSAGE = "Could not retrieve device registry entries";
    private static final String PUT_DEVICEREGISTRY_DESCRIPTION = "Update a device";
    private static final String PUT_DEVICEREGISTRY_HTTP_200_MESSAGE = "Device updated";
    private static final String PUT_DEVICEREGISTRY_HTTP_400_MESSAGE = "Could not update device";
    private static final String PATCH_DEVICEREGISTRY_DESCRIPTION = "Merge/Patch a device";
    private static final String PATCH_DEVICEREGISTRY_HTTP_200_MESSAGE = "Device merged";
    private static final String PATCH_DEVICEREGISTRY_HTTP_400_MESSAGE = "Could not merge device";
    private static final String GET_DEVICE_BY_ID_HTTP_200_MESSAGE = "Device by requested deviceId returned";
    private static final String GET_DEVICE_BY_ID_HTTP_400_MESSAGE = "No Such Device by requested deviceId";
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
    private static final String DELETE_DEVICE_REGISTRY_HTTP_200_MESSAGE = "Device Registry entry removed";
    private static final String DELETE_DEVICE_REGISTRY_HTTP_400_MESSAGE = "Could not remove device registry entry";
    
    private final Logger logger = LogManager.getLogger(DeviceRegistryManagementController.class);
    private final DeviceRegistryDBService deviceRegistryDBService;
    private final CommonDBService commonDBService;
    private final Validation validation;

    //=================================================================================================
    // methods
    
    //-------------------------------------------------------------------------------------------------
    @Autowired
    public DeviceRegistryManagementController(final DeviceRegistryDBService deviceRegistryDBService, final CommonDBService commonDBService) {
    	this.deviceRegistryDBService = deviceRegistryDBService;
    	this.commonDBService = commonDBService;
    	this.validation = new Validation();
    }
    
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return requested log entries by the given parameters", response = LogEntryListResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = CoreCommonConstants.QUERY_LOG_ENTRIES_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = CoreCommonConstants.QUERY_LOG_ENTRIES_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path = DEVICEREGISTRY_LOGS_URI, produces = MediaType.APPLICATION_JSON_VALUE)
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
				
		final String origin = CommonConstants.DEVICEREGISTRY_URI + CoreCommonConstants.OP_QUERY_LOG_ENTRIES;
		final ValidatedPageParams validParameters = CoreUtilities.validatePageParameters(page, size, direction, origin);
		final List<LogLevel> logLevels = CoreUtilities.getLogLevels(logLevel, origin);
		
		try {
			final ZonedDateTime _from = Utilities.parseUTCStringToLocalZonedDateTime(from);
			final ZonedDateTime _to = Utilities.parseUTCStringToLocalZonedDateTime(to);
			
			if (_from != null && _to != null && _to.isBefore(_from)) {
				throw new BadPayloadException("Invalid time interval", HttpStatus.SC_BAD_REQUEST, origin);
			}

			final LogEntryListResponseDTO response = commonDBService.getLogEntriesResponse(validParameters.getValidatedPage(), validParameters.getValidatedSize(), validParameters.getValidatedDirection(), sortField, CoreSystem.DEVICEREGISTRY, 
																						   logLevels, _from, _to, loggerStr);
			
			logger.debug("Log entries  with page: {} and item_per page: {} retrieved successfully", page, size);
			return response;
		} catch (final DateTimeParseException ex) {
			throw new BadPayloadException("Invalid time parameter", HttpStatus.SC_BAD_REQUEST, origin, ex);
		}
	}
    
    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = "Return device by deviceId", response = DeviceResponseDTO.class, tags = {CoreCommonConstants.SWAGGER_TAG_MGMT})
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = GET_DEVICE_BY_ID_HTTP_200_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = GET_DEVICE_BY_ID_HTTP_400_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @GetMapping(Constants.DEVICE_BY_ID_URI)
    @ResponseBody
    public DeviceResponseDTO getDeviceById(@PathVariable(value = Constants.PATH_VARIABLE_ID) final long deviceId) {
        logger.debug("getDeviceById started ...");
        validation.checkId(deviceId, getOrigin(Constants.DEVICE_BY_ID_URI));
        return deviceRegistryDBService.getDeviceById(deviceId);
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = "Return devices by given request parameters", response = DeviceListResponseDTO.class, tags = {CoreCommonConstants.SWAGGER_TAG_MGMT})
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = GET_DEVICES_HTTP_200_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = GET_DEVICES_HTTP_400_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @GetMapping(Constants.DEVICES_URI)
    @ResponseBody
    public DeviceListResponseDTO getDevices(
            @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_PAGE, required = false) final Integer page,
            @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_ITEM_PER_PAGE, required = false) final Integer size,
            @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_DIRECTION, defaultValue = CoreDefaults.DEFAULT_REQUEST_PARAM_DIRECTION_VALUE) final String direction,
            @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_SORT_FIELD, defaultValue = CoreCommonConstants.COMMON_FIELD_NAME_ID) final String sortField) {
        logger.debug("New DeviceList get request received with page: {} and item_per page: {}", page, size);

        final CoreUtilities.ValidatedPageParams pageParameters = CoreUtilities
                .validatePageParameters(page, size, direction, getOrigin(Constants.DEVICES_URI));
        final DeviceListResponseDTO deviceListResponseDTO = deviceRegistryDBService.getDeviceEntries(pageParameters, sortField);
        logger.debug("DeviceList with page: {} and item_per page: {} successfully retrieved", page, size);

        return deviceListResponseDTO;
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = "Return created device", response = DeviceResponseDTO.class, tags = {CoreCommonConstants.SWAGGER_TAG_MGMT})
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_CREATED, message = POST_DEVICE_HTTP_201_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = POST_DEVICE_HTTP_400_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @PostMapping(path = Constants.DEVICES_URI, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
    @ResponseBody
    public DeviceResponseDTO addDevice(@RequestBody final DeviceRequestDTO dto) {
        logger.debug("New device registration request received with name: {}", dto.getDeviceName());

        validation.checkDeviceRequest(dto, getOrigin(Constants.DEVICES_URI));

        final DeviceResponseDTO responseDTO = deviceRegistryDBService
                .createDeviceDto(dto.getDeviceName(), dto.getAddress(), dto.getMacAddress(), dto.getAuthenticationInfo());
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
    @PutMapping(path = Constants.DEVICES_BY_ID_URI, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public DeviceResponseDTO updateDevice(@PathVariable(value = Constants.PATH_VARIABLE_ID) final long deviceId, @RequestBody final DeviceRequestDTO dto) {
        logger.debug("New device update request received with deviceId: {}, definition: {}", deviceId, dto);
        validation.checkDevicePutRequest(dto, deviceId, getOrigin(Constants.DEVICE_BY_ID_URI));

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
    @PatchMapping(path = Constants.DEVICES_BY_ID_URI, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public DeviceResponseDTO mergeDevice(@PathVariable(value = Constants.PATH_VARIABLE_ID) final long deviceId, @RequestBody final DeviceRequestDTO request) {
        logger.debug("mergeDevice started...");

        validation.checkDeviceMergeRequest(request, deviceId, getOrigin(Constants.DEVICES_BY_ID_URI));
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
    @DeleteMapping(path = Constants.DEVICES_BY_ID_URI)
    public void removeDevice(@PathVariable(value = Constants.PATH_VARIABLE_ID) final long id) {
        logger.debug("New Device delete request received with deviceId: {}", id);

        validation.checkId(id, getOrigin(Constants.DEVICES_BY_ID_URI));

        deviceRegistryDBService.removeDeviceById(id);
        logger.debug("Device with deviceId: '{}' successfully deleted", id);
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = "Return requested device registry entries by the given parameters", response = DeviceRegistryListResponseDTO.class, tags = {CoreCommonConstants.SWAGGER_TAG_MGMT})
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = GET_DEVICEREGISTRY_HTTP_200_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = GET_DEVICEREGISTRY_HTTP_400_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @GetMapping
    @ResponseBody
    public DeviceRegistryListResponseDTO getDeviceRegistryEntries(
            @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_PAGE, required = false) final Integer page,
            @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_ITEM_PER_PAGE, required = false) final Integer size,
            @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_DIRECTION, defaultValue = CoreDefaults.DEFAULT_REQUEST_PARAM_DIRECTION_VALUE) final String direction,
            @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_SORT_FIELD, defaultValue = CoreCommonConstants.COMMON_FIELD_NAME_ID) final String sortField) {
        logger.debug("New Device Registry get request received with page: {} and item_per page: {}", page, size);

        final CoreUtilities.ValidatedPageParams params = CoreUtilities
                .validatePageParameters(page, size, direction, getBaseOrigin());
        final DeviceRegistryListResponseDTO deviceRegistryEntriesResponse = deviceRegistryDBService
                .getDeviceRegistryEntries(params, sortField);
        logger.debug("Device Registry entries with page: {} and item_per page: {} successfully retrieved", page, size);

        return deviceRegistryEntriesResponse;
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = "Return requested device registry entry", response = DeviceRegistryResponseDTO.class, tags = {CoreCommonConstants.SWAGGER_TAG_MGMT})
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = GET_DEVICEREGISTRY_HTTP_200_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = GET_DEVICEREGISTRY_HTTP_400_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @GetMapping(path = DEVICEREGISTRY_BY_ID_URI)
    @ResponseBody
    public DeviceRegistryResponseDTO getDeviceRegistryEntryById(@PathVariable(value = Constants.PATH_VARIABLE_ID) final long id) {
        logger.debug("New Device Registry get request received with deviceId: {}", id);

        validation.checkId(id, getOrigin(DEVICEREGISTRY_BY_ID_URI));
        final DeviceRegistryResponseDTO deviceRegistryEntryByIdResponse = deviceRegistryDBService.getDeviceRegistryById(id);
        logger.debug("Device Registry entry with deviceId: {} successfully retrieved", id);

        return deviceRegistryEntryByIdResponse;
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = "Return requested device registry entries by device definition based on the given parameters", response = DeviceRegistryListResponseDTO.class,
            tags = {CoreCommonConstants.SWAGGER_TAG_MGMT})
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = GET_DEVICEREGISTRY_HTTP_200_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = GET_DEVICEREGISTRY_HTTP_400_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @GetMapping(path = DEVICEREGISTRY_BY_DEVICE_NAME_URI)
    @ResponseBody
    public DeviceRegistryListResponseDTO getDeviceRegistryEntriesByDeviceName(
            @PathVariable(value = Constants.PATH_VARIABLE_DEVICE_NAME) final String deviceName,
            @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_PAGE, required = false) final Integer page,
            @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_ITEM_PER_PAGE, required = false) final Integer size,
            @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_DIRECTION, defaultValue = CoreDefaults.DEFAULT_REQUEST_PARAM_DIRECTION_VALUE) final String direction,
            @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_SORT_FIELD, defaultValue = CoreCommonConstants.COMMON_FIELD_NAME_ID) final String sortField) {
        logger.debug("New Device Registry get by Device Definition request received with page: {} and item_per page: {}", page, size);

        final String origin = getOrigin(DEVICEREGISTRY_BY_DEVICE_NAME_URI);
        if (Utilities.isEmpty(deviceName)) {
            throw new BadPayloadException("Device name cannot be empty.", HttpStatus.SC_BAD_REQUEST, origin);
        }

        final CoreUtilities.ValidatedPageParams pageParameters = CoreUtilities.validatePageParameters(page, size, direction, origin);
        final DeviceRegistryListResponseDTO deviceRegistryEntries = deviceRegistryDBService
                .getDeviceRegistryEntriesByDeviceName(deviceName, pageParameters, sortField);
        logger.debug("Device Registry entries with page: {} and item_per page: {} successfully retrieved", page, size);

        return deviceRegistryEntries;
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = "Remove the specified device registry entry", tags = {CoreCommonConstants.SWAGGER_TAG_MGMT})
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = DELETE_DEVICE_REGISTRY_HTTP_200_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = DELETE_DEVICE_REGISTRY_HTTP_400_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @DeleteMapping(path = DEVICEREGISTRY_BY_ID_URI)
    public void removeDeviceRegistryEntryById(@PathVariable(value = Constants.PATH_VARIABLE_ID) final long id) {
        logger.debug("New Device Registry delete request received with deviceId: {}", id);
        validation.checkId(id, getOrigin(DEVICEREGISTRY_BY_ID_URI));
        deviceRegistryDBService.removeDeviceRegistryEntryById(id);
        logger.debug("Device Registry with deviceId: '{}' successfully deleted", id);
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = PUT_DEVICEREGISTRY_DESCRIPTION, response = DeviceRegistryResponseDTO.class, tags = {CoreCommonConstants.SWAGGER_TAG_MGMT})
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = PUT_DEVICEREGISTRY_HTTP_200_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = PUT_DEVICEREGISTRY_HTTP_400_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @PutMapping(path = DEVICEREGISTRY_BY_ID_URI, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody
    DeviceRegistryResponseDTO updateDeviceRegistry(@PathVariable(value = Constants.PATH_VARIABLE_ID) final long id,
                                                   @RequestBody final DeviceRegistryRequestDTO request) {
        logger.debug("New device registry update request received");
        validation.checkDeviceRegistryUpdateRequest(id, request, getOrigin(DEVICEREGISTRY_BY_ID_URI));

        final DeviceRegistryResponseDTO response = deviceRegistryDBService.updateDeviceRegistryById(id, request);
        logger.debug("Device Registry entry {} is successfully updated with device {} and device {}", id, request.getDevice().getDeviceName(),
                     request.getDevice());

        return response;
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = PATCH_DEVICEREGISTRY_DESCRIPTION, response = DeviceRegistryResponseDTO.class, tags = {CoreCommonConstants.SWAGGER_TAG_MGMT})
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = PATCH_DEVICEREGISTRY_HTTP_200_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = PATCH_DEVICEREGISTRY_HTTP_400_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @PatchMapping(path = DEVICEREGISTRY_BY_ID_URI, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody
    DeviceRegistryResponseDTO mergeDeviceRegistry(@PathVariable(value = Constants.PATH_VARIABLE_ID) final long id,
                                                  @RequestBody final DeviceRegistryRequestDTO request) {
        logger.debug("New device registry merge request received");
        validation.checkDeviceRegistryMergeRequest(id, request, getOrigin(DEVICEREGISTRY_BY_ID_URI));

        final DeviceRegistryResponseDTO response = deviceRegistryDBService.mergeDeviceRegistryById(id, request);
        logger.debug("Device Registry entry {} is successfully merged witch device {} and device {}", id, response.getDevice(), request.getDevice());

        return response;
    }

    //=================================================================================================
    // assistant methods
    
    //-------------------------------------------------------------------------------------------------
	private String getBaseOrigin() {
        return CommonConstants.DEVICEREGISTRY_URI + CoreCommonConstants.MGMT_URI;
    }

    //-------------------------------------------------------------------------------------------------
	private String getOrigin(final String postfix) {
        Assert.notNull(postfix, "Internal error: Origin postfix not provided");
        return getBaseOrigin() + postfix;
    }
}