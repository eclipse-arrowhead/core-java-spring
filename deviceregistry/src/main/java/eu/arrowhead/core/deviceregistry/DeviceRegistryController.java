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
import eu.arrowhead.common.Defaults;
import eu.arrowhead.common.SecurityUtilities;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.shared.CertificateType;
import eu.arrowhead.common.dto.shared.DeviceQueryFormDTO;
import eu.arrowhead.common.dto.shared.DeviceQueryResultDTO;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@Api(tags = {CoreCommonConstants.SWAGGER_TAG_ALL})
@CrossOrigin(maxAge = Defaults.CORS_MAX_AGE, allowCredentials = Defaults.CORS_ALLOW_CREDENTIALS,
        allowedHeaders = {HttpHeaders.ORIGIN, HttpHeaders.CONTENT_TYPE, HttpHeaders.ACCEPT, HttpHeaders.AUTHORIZATION}
)
@RestController
@RequestMapping(CommonConstants.DEVICEREGISTRY_URI)
public class DeviceRegistryController {

	//=================================================================================================
	// members

    private static final String DEVICEREGISTRY_REGISTER_DESCRIPTION = "Registers a device";
    private static final String DEVICEREGISTRY_REGISTER_201_MESSAGE = "Device registered";
    private static final String DEVICEREGISTRY_REGISTER_400_MESSAGE = "Could not register device";
    private static final String DEVICEREGISTRY_UNREGISTER_DESCRIPTION = "Remove a registered device";
    private static final String DEVICEREGISTRY_UNREGISTER_200_MESSAGE = "Registered device removed";
    private static final String DEVICEREGISTRY_UNREGISTER_400_MESSAGE = "Could not remove device";
    private static final String DEVICEREGISTRY_QUERY_DESCRIPTION = "Return Device Registry data that fits the specification";
    private static final String DEVICEREGISTRY_QUERY_200_MESSAGE = "Device Registry data returned";
    private static final String DEVICEREGISTRY_QUERY_400_MESSAGE = "Could not query Device Registry";
    private static final String DEVICEREGISTRY_QUERY_BY_DEVICE_ID_DESCRIPTION = "Return device by requested deviceId";
    private static final String DEVICEREGISTRY_QUERY_BY_DEVICE_ID_200_MESSAGE = "Device data by deviceId returned";
    private static final String DEVICEREGISTRY_QUERY_BY_DEVICE_ID_400_MESSAGE = "Could not query Device Registry by Consumer device deviceId";
    private static final String DEVICEREGISTRY_QUERY_BY_DEVICE_DTO_DESCRIPTION = "Return Device by requested dto";
    private static final String DEVICEREGISTRY_QUERY_BY_DEVICE_DTO_200_MESSAGE = "Consumer Device data by requestDTO returned";
    private static final String DEVICEREGISTRY_QUERY_BY_DEVICE_DTO_400_MESSAGE = "Could not query Device Registry by Consumer device requestDTO";

    private final Logger logger = LogManager.getLogger(DeviceRegistryController.class);
    private final DeviceRegistryDBService deviceRegistryDBService;
    private final SecurityUtilities securityUtilities;
    private final Validation validation;

    //=================================================================================================
    // methods

    //-------------------------------------------------------------------------------------------------
    @Autowired
    public DeviceRegistryController(final DeviceRegistryDBService deviceRegistryDBService,
    		final SecurityUtilities securityUtilities) {
    	this.deviceRegistryDBService = deviceRegistryDBService;
    	this.securityUtilities = securityUtilities;
    	this.validation = new Validation();
    }

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
    @ApiOperation(value = DEVICEREGISTRY_REGISTER_DESCRIPTION, response = DeviceRegistryResponseDTO.class, tags = {CoreCommonConstants.SWAGGER_TAG_CLIENT})
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_CREATED, message = DEVICEREGISTRY_REGISTER_201_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = DEVICEREGISTRY_REGISTER_400_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @ResponseStatus(value = org.springframework.http.HttpStatus.CREATED)
    @PostMapping(path = {CommonConstants.OP_DEVICEREGISTRY_REGISTER_URI, CoreCommonConstants.MGMT_URI},
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public DeviceRegistryResponseDTO registerDevice(final HttpServletRequest httpServletRequest,
                                                    @RequestBody final DeviceRegistryRequestDTO request) {
        logger.debug("New device registration request received");
        securityUtilities.authenticateCertificate(httpServletRequest, CertificateType.AH_DEVICE);
        validation.checkDeviceRegistryRequest(request, getOrigin(CommonConstants.OP_DEVICEREGISTRY_REGISTER_URI));
        final DeviceRegistryResponseDTO response = deviceRegistryDBService.registerDeviceRegistry(request);
        logger.debug("{} successfully registers its device {}", request.getDevice().getDeviceName(), request.getDevice());

        return response;
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = DEVICEREGISTRY_UNREGISTER_DESCRIPTION, tags = {CoreCommonConstants.SWAGGER_TAG_CLIENT})
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = DEVICEREGISTRY_UNREGISTER_200_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = DEVICEREGISTRY_UNREGISTER_400_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @DeleteMapping(path = CommonConstants.OP_DEVICEREGISTRY_UNREGISTER_URI)
    public void unregisterDevice(final HttpServletRequest httpServletRequest,
                                 @RequestParam(CommonConstants.OP_DEVICEREGISTRY_UNREGISTER_REQUEST_PARAM_PROVIDER_DEVICE_NAME) final String deviceName,
                                 @RequestParam(CommonConstants.OP_DEVICEREGISTRY_UNREGISTER_REQUEST_PARAM_PROVIDER_MAC_ADDRESS) final String macAddress) {
        logger.debug("Device removal request received");
        securityUtilities.authenticateCertificate(httpServletRequest, CertificateType.AH_ONBOARDING);
        validation.checkUnregisterDeviceParameters(deviceName, macAddress);
        deviceRegistryDBService.removeDeviceRegistryByNameAndMacAddress(deviceName, macAddress);
        logger.debug("{} successfully removed", deviceName);
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = DEVICEREGISTRY_QUERY_DESCRIPTION, response = DeviceQueryResultDTO.class, tags = {CoreCommonConstants.SWAGGER_TAG_CLIENT})
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = DEVICEREGISTRY_QUERY_200_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = DEVICEREGISTRY_QUERY_400_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @PostMapping(path = CommonConstants.OP_DEVICEREGISTRY_QUERY_URI, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public DeviceQueryResultDTO queryRegistry(final HttpServletRequest httpServletRequest,
                                              @RequestBody final DeviceQueryFormDTO form) {
        logger.debug("Device query request received");
        securityUtilities.authenticateCertificate(httpServletRequest, CertificateType.AH_SYSTEM);

        if (Utilities.isEmpty(form.getDeviceNameRequirements())) {
            throw new BadPayloadException("Device definition requirement is null or blank", HttpStatus.SC_BAD_REQUEST, getOrigin(
                    CommonConstants.OP_DEVICEREGISTRY_QUERY_URI));
        }

        final DeviceQueryResultDTO result = deviceRegistryDBService.queryRegistry(form);
        logger.debug("Return {} providers for device {}", result.getDeviceQueryData().size(), form.getDeviceNameRequirements());

        return result;
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = DEVICEREGISTRY_QUERY_BY_DEVICE_ID_DESCRIPTION, response = DeviceResponseDTO.class, tags = {CoreCommonConstants.SWAGGER_TAG_PRIVATE})
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = DEVICEREGISTRY_QUERY_BY_DEVICE_ID_200_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = DEVICEREGISTRY_QUERY_BY_DEVICE_ID_400_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @GetMapping(path = CoreCommonConstants.OP_DEVICEREGISTRY_QUERY_BY_DEVICE_ID_URI, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public DeviceResponseDTO queryRegistryByDeviceId(final HttpServletRequest httpServletRequest,
                                                     @PathVariable(value = Constants.PATH_VARIABLE_ID) final long deviceId) {
        logger.debug("Device query by device deviceId request received");
        securityUtilities.authenticateCertificate(httpServletRequest, CertificateType.AH_SYSTEM);
        validation.checkId(deviceId, getOrigin(CoreCommonConstants.OP_DEVICEREGISTRY_QUERY_BY_DEVICE_ID_URI));
        final DeviceResponseDTO result = deviceRegistryDBService.getDeviceById(deviceId);

        logger.debug("Return device by deviceId: {}", deviceId);
        return result;
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = DEVICEREGISTRY_QUERY_BY_DEVICE_DTO_DESCRIPTION, response = DeviceResponseDTO.class, tags = {CoreCommonConstants.SWAGGER_TAG_PRIVATE})
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = DEVICEREGISTRY_QUERY_BY_DEVICE_DTO_200_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = DEVICEREGISTRY_QUERY_BY_DEVICE_DTO_400_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @PostMapping(path = CoreCommonConstants.OP_DEVICEREGISTRY_QUERY_BY_DEVICE_DTO_URI, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public DeviceResponseDTO queryRegistryByDeviceDTO(final HttpServletRequest httpServletRequest,
                                                      @RequestBody final DeviceRequestDTO request) {
        logger.debug("Device query by DeviceRequestDTO request received");
        securityUtilities.authenticateCertificate(httpServletRequest, CertificateType.AH_SYSTEM);
        validation.checkDeviceRequest(request, getOrigin(CoreCommonConstants.OP_DEVICEREGISTRY_QUERY_BY_DEVICE_ID_URI));

        final String deviceName = request.getDeviceName();
        final String macAddress = request.getMacAddress();

        final DeviceResponseDTO result = deviceRegistryDBService.getDeviceDtoByNameAndMacAddress(deviceName, macAddress);

        logger.debug("Return device by name: {}, macAddress: {}", deviceName, macAddress);
        return result;
    }

    //=================================================================================================
    // assistant methods

    //-------------------------------------------------------------------------------------------------
	private String getBaseOrigin() {
        return CommonConstants.DEVICEREGISTRY_URI;
    }

    //-------------------------------------------------------------------------------------------------
	private String getOrigin(final String postfix) {
        Assert.notNull(postfix, "Internal error: Origin postfix not provided");
        return getBaseOrigin() + postfix;
    }
}