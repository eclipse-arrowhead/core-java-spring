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

package eu.arrowhead.core.systemregistry;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.CoreDefaults;
import eu.arrowhead.common.CoreUtilities;
import eu.arrowhead.common.Defaults;
import eu.arrowhead.common.dto.internal.DeviceListResponseDTO;
import eu.arrowhead.common.dto.internal.SystemListResponseDTO;
import eu.arrowhead.common.dto.internal.SystemRegistryListResponseDTO;
import eu.arrowhead.common.dto.shared.DeviceRequestDTO;
import eu.arrowhead.common.dto.shared.DeviceResponseDTO;
import eu.arrowhead.common.dto.shared.SystemRegistryRequestDTO;
import eu.arrowhead.common.dto.shared.SystemRegistryResponseDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.common.dto.shared.SystemResponseDTO;
import eu.arrowhead.core.systemregistry.database.service.SystemRegistryDBService;
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
@RequestMapping(CommonConstants.SYSTEM_REGISTRY_URI + CoreCommonConstants.MGMT_URI)
public class SystemRegistryManagementController {

    //=================================================================================================
    // members

    private static final String GET_SYSTEM_BY_ID_HTTP_200_MESSAGE = "System by requested id returned";
    private static final String GET_SYSTEM_BY_ID_HTTP_400_MESSAGE = "No Such System by requested id";
    private static final String PATH_VARIABLE_ID = "id";
    private static final String SYSTEM_BY_ID_URI = "/system/{" + PATH_VARIABLE_ID + "}";
    private static final String SYSTEMS_URI = "/systems";
    private static final String SYSTEMS_BY_ID_URI = "/systems/{" + PATH_VARIABLE_ID + "}";
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
    private static final String DEVICE_BY_ID_URI = "/device/{" + PATH_VARIABLE_ID + "}";
    private static final String DEVICES_URI = "/devices";
    private static final String POST_DEVICE_HTTP_201_MESSAGE = "Device created";
    private static final String POST_DEVICE_HTTP_400_MESSAGE = "Could not create device";
    private static final String PUT_DEVICE_HTTP_200_MESSAGE = "Device updated";
    private static final String PUT_DEVICE_HTTP_400_MESSAGE = "Could not update device";

    private static final String DELETE_SYSTEMS_HTTP_200_MESSAGE = "System definition removed";
    private static final String DELETE_SYSTEMS_HTTP_400_MESSAGE = "Could not remove system definition";

    private static final String SYSTEM_REGISTRY_UPDATE_DESCRIPTION = "Update a system";
    private static final String SYSTEM_REGISTRY_UPDATE_200_MESSAGE = "System updated";
    private static final String SYSTEM_REGISTRY_UPDATE_400_MESSAGE = "Could not update system";
    private static final String SYSTEM_REGISTRY_MERGE_DESCRIPTION = "Merge/Patch a system";
    private static final String SYSTEM_REGISTRY_MERGE_200_MESSAGE = "System merged";
    private static final String SYSTEM_REGISTRY_MERGE_400_MESSAGE = "Could not merge system";

    private static final String SYSTEM_REGISTRY_MGMT_BY_ID_URI = "/{" + PATH_VARIABLE_ID + "}";
    private static final String PATH_VARIABLE_SYSTEM_NAME = "systemName";
    private static final String SYSTEM_REGISTRY_MGMT_BY_SYSTEM_NAME_URI = "/systemname" + "/{" + PATH_VARIABLE_SYSTEM_NAME + "}";
    private static final String GET_SYSTEM_REGISTRY_HTTP_200_MESSAGE = "System Registry entries returned";
    private static final String GET_SYSTEM_REGISTRY_HTTP_400_MESSAGE = "Could not retrieve system registry entries";
    private static final String DELETE_SYSTEM_REGISTRY_HTTP_200_MESSAGE = "System Registry entry removed";
    private static final String DELETE_SYSTEM_REGISTRY_HTTP_400_MESSAGE = "Could not remove system registry entry";

    private final Logger logger = LogManager.getLogger(SystemRegistryManagementController.class);

    private final SystemRegistryDBService systemRegistryDBService;
    private final Validation validation;

    @Autowired
    public SystemRegistryManagementController(final SystemRegistryDBService systemRegistryDBService) {
        this.systemRegistryDBService = systemRegistryDBService;
        this.validation = new Validation();
    }


    //=================================================================================================
    // methods

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = "Return system by id", response = SystemResponseDTO.class, tags = {CoreCommonConstants.SWAGGER_TAG_MGMT})
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = GET_SYSTEM_BY_ID_HTTP_200_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = GET_SYSTEM_BY_ID_HTTP_400_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @GetMapping(SYSTEM_BY_ID_URI)
    @ResponseBody
    public SystemResponseDTO getSystemById(@PathVariable(value = PATH_VARIABLE_ID) final long systemId) {
        logger.debug("getSystemById started ...");

        validation.checkId(systemId, getOrigin(SYSTEM_BY_ID_URI));

        return systemRegistryDBService.getSystemById(systemId);
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = "Return systems by request parameters", response = SystemListResponseDTO.class, tags = {CoreCommonConstants.SWAGGER_TAG_MGMT})
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = GET_SYSTEMS_HTTP_200_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = GET_SYSTEMS_HTTP_400_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @GetMapping(SYSTEMS_URI)
    @ResponseBody
    public SystemListResponseDTO getSystems(
            @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_PAGE, required = false) final Integer page,
            @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_ITEM_PER_PAGE, required = false) final Integer size,
            @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_DIRECTION, defaultValue = CoreDefaults.DEFAULT_REQUEST_PARAM_DIRECTION_VALUE) final String direction,
            @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_SORT_FIELD, defaultValue = CoreCommonConstants.COMMON_FIELD_NAME_ID) final String sortField) {
        logger.debug("getSystems started ...");

        final CoreUtilities.ValidatedPageParams pageParameters = CoreUtilities
                .validatePageParameters(page, size, direction, CommonConstants.SYSTEM_REGISTRY_URI + DEVICES_URI);
        return systemRegistryDBService.getSystemEntries(pageParameters, sortField);
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = "Return created system", response = SystemResponseDTO.class, tags = {CoreCommonConstants.SWAGGER_TAG_MGMT})
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_CREATED, message = POST_SYSTEM_HTTP_201_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = POST_SYSTEM_HTTP_400_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @PostMapping(path = SYSTEMS_URI, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
    @ResponseBody
    public SystemResponseDTO addSystem(@RequestBody final SystemRequestDTO request) {
        return callCreateSystem(request);
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = "Return updated system", response = SystemResponseDTO.class, tags = {CoreCommonConstants.SWAGGER_TAG_MGMT})
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_CREATED, message = PUT_SYSTEM_HTTP_200_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = PUT_SYSTEM_HTTP_400_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @PutMapping(path = SYSTEMS_BY_ID_URI, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public SystemResponseDTO updateSystem(@PathVariable(value = PATH_VARIABLE_ID) final long systemId, @RequestBody final SystemRequestDTO request) {
        return callUpdateSystem(request, systemId);
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = "Return system updated by fields", response = SystemResponseDTO.class, tags = {CoreCommonConstants.SWAGGER_TAG_MGMT})
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_CREATED, message = PATCH_SYSTEM_HTTP_200_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = PATCH_SYSTEM_HTTP_400_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @PatchMapping(path = SYSTEMS_BY_ID_URI, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public SystemResponseDTO mergeSystem(@PathVariable(value = PATH_VARIABLE_ID) final long systemId, @RequestBody final SystemRequestDTO request) {
        return callMergeSystem(request, systemId);
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = "Remove system", tags = {CoreCommonConstants.SWAGGER_TAG_MGMT})
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = DELETE_SYSTEM_HTTP_200_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = DELETE_SYSTEM_HTTP_400_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @DeleteMapping(path = SYSTEMS_BY_ID_URI)
    public void removeSystem(@PathVariable(value = PATH_VARIABLE_ID) final long systemId) {
        logger.debug("New System delete request received with id: {}", systemId);

        validation.checkId(systemId, getOrigin(SYSTEM_BY_ID_URI));

        systemRegistryDBService.removeSystemById(systemId);
        logger.debug("System with id: '{}' successfully deleted", systemId);
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = "Return device by id", response = DeviceResponseDTO.class, tags = {CoreCommonConstants.SWAGGER_TAG_MGMT})
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = GET_DEVICE_BY_ID_HTTP_200_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = GET_DEVICE_BY_ID_HTTP_400_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @GetMapping(SYSTEMS_BY_ID_URI)
    @ResponseBody
    public DeviceResponseDTO getDeviceById(@PathVariable(value = PATH_VARIABLE_ID) final long deviceId) {
        logger.debug("getDeviceById started ...");

        validation.checkId(deviceId, getOrigin(SYSTEM_BY_ID_URI));

        return systemRegistryDBService.getDeviceById(deviceId);
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = "Return requested device by the given parameters", response = DeviceListResponseDTO.class, tags = {CoreCommonConstants.SWAGGER_TAG_MGMT})
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = GET_SYSTEMS_HTTP_200_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = GET_SYSTEMS_HTTP_400_MESSAGE),
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

        final CoreUtilities.ValidatedPageParams pageParameters = CoreUtilities
                .validatePageParameters(page, size, direction, CommonConstants.SYSTEM_REGISTRY_URI + DEVICES_URI);
        final DeviceListResponseDTO deviceListResponseDTO = systemRegistryDBService.getDeviceEntries(pageParameters, sortField);
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
    @PostMapping(path = DEVICES_URI, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = org.springframework.http.HttpStatus.CREATED)
    @ResponseBody
    public DeviceResponseDTO addDevice(@RequestBody final DeviceRequestDTO dto) {
        logger.debug("New device registration request received with name: {}", dto.getDeviceName());

        validation.validateDevice(dto, DEVICES_URI);

        final DeviceResponseDTO responseDTO = systemRegistryDBService
                .createDeviceDto(dto.getDeviceName(), dto.getAddress(), dto.getMacAddress(), dto.getAuthenticationInfo());
        logger.debug("{} successfully registered.", responseDTO);

        return responseDTO;
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = "Return updated device", response = DeviceResponseDTO.class, tags = {CoreCommonConstants.SWAGGER_TAG_MGMT})
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = PUT_DEVICE_HTTP_200_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = PUT_DEVICE_HTTP_400_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @PutMapping(path = DEVICE_BY_ID_URI, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public DeviceResponseDTO putUpdateDevice(@PathVariable(value = PATH_VARIABLE_ID) final long id,
                                             @RequestBody final DeviceRequestDTO dto) {
        logger.debug("New device update request received with id: {}, definition: {}", id, dto);

        validation.checkId(id, getOrigin(DEVICE_BY_ID_URI));
        validation.validateDevice(dto, getOrigin(DEVICE_BY_ID_URI));

        final DeviceResponseDTO deviceResponseDTO = systemRegistryDBService
                .updateDeviceByIdResponse(id, dto.getDeviceName(), dto.getAddress(), dto.getMacAddress(), dto.getAuthenticationInfo());
        logger.debug("Device with id: '{}' successfully updated with definition '{}'.", id, deviceResponseDTO);

        return deviceResponseDTO;
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = "Remove device", tags = {CoreCommonConstants.SWAGGER_TAG_MGMT})
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = DELETE_SYSTEMS_HTTP_200_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = DELETE_SYSTEMS_HTTP_400_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @DeleteMapping(path = DEVICE_BY_ID_URI)
    public void removeDevice(@PathVariable(value = PATH_VARIABLE_ID) final long id) {
        logger.debug("New System Definition delete request received with id: {}", id);

        validation.checkId(id, getOrigin(DEVICE_BY_ID_URI));

        systemRegistryDBService.removeDeviceById(id);
        logger.debug("Device with id: '{}' successfully deleted", id);
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = "Return requested system registry entries by the given parameters", response = SystemRegistryListResponseDTO.class, tags = {CoreCommonConstants.SWAGGER_TAG_MGMT})
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = GET_SYSTEM_REGISTRY_HTTP_200_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = GET_SYSTEM_REGISTRY_HTTP_400_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @GetMapping
    @ResponseBody
    public SystemRegistryListResponseDTO getSystemRegistryEntries(
            @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_PAGE, required = false) final Integer page,
            @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_ITEM_PER_PAGE, required = false) final Integer size,
            @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_DIRECTION, defaultValue = CoreDefaults.DEFAULT_REQUEST_PARAM_DIRECTION_VALUE) final String direction,
            @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_SORT_FIELD, defaultValue = CoreCommonConstants.COMMON_FIELD_NAME_ID) final String sortField) {
        logger.debug("New System Registry get request received with page: {} and item_per page: {}", page, size);

        final CoreUtilities.ValidatedPageParams params = CoreUtilities
                .validatePageParameters(page, size, direction, CommonConstants.SYSTEM_REGISTRY_URI + CoreCommonConstants.MGMT_URI);
        final SystemRegistryListResponseDTO systemRegistryEntriesResponse = systemRegistryDBService
                .getSystemRegistryEntries(params, sortField);
        logger.debug("System Registry entries with page: {} and item_per page: {} successfully retrieved", page, size);

        return systemRegistryEntriesResponse;
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = "Return requested system registry entry", response = SystemRegistryResponseDTO.class, tags = {CoreCommonConstants.SWAGGER_TAG_MGMT})
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = GET_SYSTEM_REGISTRY_HTTP_200_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = GET_SYSTEM_REGISTRY_HTTP_400_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @GetMapping(path = SYSTEM_REGISTRY_MGMT_BY_ID_URI)
    @ResponseBody
    public SystemRegistryResponseDTO getSystemRegistryEntryById(@PathVariable(value = PATH_VARIABLE_ID) final long id) {
        logger.debug("New System Registry get request received with id: {}", id);

        validation.checkId(id, getOrigin(SYSTEM_REGISTRY_MGMT_BY_ID_URI));

        final SystemRegistryResponseDTO systemRegistryEntryByIdResponse = systemRegistryDBService.getSystemRegistryById(id);
        logger.debug("System Registry entry with id: {} successfully retrieved", id);

        return systemRegistryEntryByIdResponse;
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = "Return requested system registry entries by system definition based on the given parameters", response = SystemRegistryListResponseDTO.class,
            tags = {CoreCommonConstants.SWAGGER_TAG_MGMT})
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = GET_SYSTEM_REGISTRY_HTTP_200_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = GET_SYSTEM_REGISTRY_HTTP_400_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @GetMapping(path = SYSTEM_REGISTRY_MGMT_BY_SYSTEM_NAME_URI)
    @ResponseBody
    public SystemRegistryListResponseDTO getSystemRegistryEntriesBySystemName(
            @PathVariable(value = PATH_VARIABLE_SYSTEM_NAME) final String systemName,
            @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_PAGE, required = false) final Integer page,
            @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_ITEM_PER_PAGE, required = false) final Integer size,
            @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_DIRECTION, defaultValue = CoreDefaults.DEFAULT_REQUEST_PARAM_DIRECTION_VALUE) final String direction,
            @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_SORT_FIELD, defaultValue = CoreCommonConstants.COMMON_FIELD_NAME_ID) final String sortField) {
        logger.debug("New System Registry get by System Definition request received with page: {} and item_per page: {}", page, size);

        final String origin = CommonConstants.SYSTEM_REGISTRY_URI + SYSTEM_REGISTRY_MGMT_BY_SYSTEM_NAME_URI;
        validation.checkSystemName(systemName, origin);

        final CoreUtilities.ValidatedPageParams pageParameters = CoreUtilities.validatePageParameters(page, size, direction, origin);
        final SystemRegistryListResponseDTO systemRegistryEntries = systemRegistryDBService
                .getSystemRegistryEntriesBySystemName(systemName, pageParameters, sortField);
        logger.debug("System Registry entries with page: {} and item_per page: {} successfully retrieved", page, size);

        return systemRegistryEntries;
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = "Remove the specified system registry entry", tags = {CoreCommonConstants.SWAGGER_TAG_MGMT})
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = DELETE_SYSTEM_REGISTRY_HTTP_200_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = DELETE_SYSTEM_REGISTRY_HTTP_400_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @DeleteMapping(path = SYSTEM_REGISTRY_MGMT_BY_ID_URI)
    public void removeSystemRegistryEntryById(@PathVariable(value = PATH_VARIABLE_ID) final long id) {
        logger.debug("New System Registry delete request received with id: {}", id);

        validation.checkId(id, SYSTEM_REGISTRY_MGMT_BY_ID_URI);

        systemRegistryDBService.removeSystemRegistryEntryById(id);
        logger.debug("System Registry with id: '{}' successfully deleted", id);
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = SYSTEM_REGISTRY_UPDATE_DESCRIPTION, response = SystemRegistryResponseDTO.class, tags = {CoreCommonConstants.SWAGGER_TAG_MGMT})
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = SYSTEM_REGISTRY_UPDATE_200_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = SYSTEM_REGISTRY_UPDATE_400_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @PutMapping(path = SYSTEM_REGISTRY_MGMT_BY_ID_URI, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody
    SystemRegistryResponseDTO updateSystemRegistry(@PathVariable(value = PATH_VARIABLE_ID) final long id, @RequestBody final SystemRegistryRequestDTO request) {
        logger.debug("New system registry update request received");
        validation.checkSystemRegistryUpdateRequest(id, request, SYSTEM_REGISTRY_MGMT_BY_ID_URI);

        final SystemRegistryResponseDTO response = systemRegistryDBService.updateSystemRegistryById(id, request);
        logger.debug("System Registry entry {} is successfully updated with system {} and system {}", id, request.getSystem().getSystemName(),
                     request.getSystem());

        return response;
    }


    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = SYSTEM_REGISTRY_MERGE_DESCRIPTION, response = SystemRegistryResponseDTO.class, tags = {CoreCommonConstants.SWAGGER_TAG_MGMT})
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = SYSTEM_REGISTRY_MERGE_200_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = SYSTEM_REGISTRY_MERGE_400_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @PatchMapping(path = SYSTEM_REGISTRY_MGMT_BY_ID_URI, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody
    SystemRegistryResponseDTO mergeSystemRegistry(@PathVariable(value = PATH_VARIABLE_ID) final long id, @RequestBody final SystemRegistryRequestDTO request) {
        logger.debug("New system registry merge request received");
        validation.checkSystemRegistryMergeRequest(id, request, CoreCommonConstants.MGMT_URI);

        final SystemRegistryResponseDTO response = systemRegistryDBService.mergeSystemRegistryById(id, request);
        logger.debug("System Registry entry {} is successfully merged witch system {} and system {}", id, response.getSystem(), request.getSystem());

        return response;
    }

    //=================================================================================================
    // assistant methods

    //-------------------------------------------------------------------------------------------------
    private SystemResponseDTO callCreateSystem(final SystemRequestDTO request) {
        logger.debug("callCreateSystem started...");

        validation.checkSystemRequest(request, getOrigin(SYSTEMS_URI), true);

        final String systemName = request.getSystemName();
        final String address = request.getAddress();
        final int port = request.getPort();
        final String authenticationInfo = request.getAuthenticationInfo();

        return systemRegistryDBService.createSystemDto(systemName, address, port, authenticationInfo);
    }

    //-------------------------------------------------------------------------------------------------
    private SystemResponseDTO callUpdateSystem(final SystemRequestDTO request, final long systemId) {
        logger.debug("callUpdateSystem started...");

        validation.checkSystemPutRequest(request, systemId, getOrigin(SYSTEMS_URI));

        final String validatedSystemName = request.getSystemName().toLowerCase();
        final String validatedAddress = request.getAddress().toLowerCase();
        final int validatedPort = request.getPort();
        final String validatedAuthenticationInfo = request.getAuthenticationInfo();

        return systemRegistryDBService.updateSystemDto(systemId, validatedSystemName, validatedAddress, validatedPort, validatedAuthenticationInfo);
    }

    //-------------------------------------------------------------------------------------------------
    private SystemResponseDTO callMergeSystem(final SystemRequestDTO request, final long systemId) {
        logger.debug("callMergeSystem started...");

        validation.checkSystemMergeRequest(request, systemId, getOrigin(SYSTEMS_URI));

        final String validatedSystemName = request.getSystemName() != null ? request.getSystemName().toLowerCase() : "";
        final String validatedAddress = request.getAddress() != null ? request.getAddress().toLowerCase() : "";
        final Integer validatedPort = request.getPort();
        final String validatedAuthenticationInfo = request.getAuthenticationInfo();

        return systemRegistryDBService.mergeSystemResponse(systemId, validatedSystemName, validatedAddress, validatedPort, validatedAuthenticationInfo);
    }

    //=================================================================================================
    // assistant methods
    private String getBaseOrigin() {
        return CommonConstants.SYSTEM_REGISTRY_URI + CoreCommonConstants.MGMT_URI;
    }

    private String getOrigin(final String postfix) {
        Assert.notNull(postfix, "Internal error: Origin postfix not provided");
        return getBaseOrigin() + postfix;
    }
}