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

import javax.servlet.http.HttpServletRequest;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.Defaults;
import eu.arrowhead.common.dto.shared.DeviceRegistryOnboardingWithCsrRequestDTO;
import eu.arrowhead.common.dto.shared.DeviceRegistryOnboardingWithCsrResponseDTO;
import eu.arrowhead.common.dto.shared.DeviceRegistryOnboardingWithNameRequestDTO;
import eu.arrowhead.common.dto.shared.DeviceRegistryOnboardingWithNameResponseDTO;
import eu.arrowhead.core.deviceregistry.database.service.DeviceRegistryDBService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(tags = {CoreCommonConstants.SWAGGER_TAG_ALL})
@CrossOrigin(maxAge = Defaults.CORS_MAX_AGE, allowCredentials = Defaults.CORS_ALLOW_CREDENTIALS,
        allowedHeaders = {HttpHeaders.ORIGIN, HttpHeaders.CONTENT_TYPE, HttpHeaders.ACCEPT, HttpHeaders.AUTHORIZATION}
)
@RestController
@RequestMapping(CommonConstants.DEVICEREGISTRY_URI + CommonConstants.ONBOARDING_URI)
public class DeviceRegistryOnboardingController {

	//=================================================================================================
	// members
	
    private static final String DEVICEREGISTRY_REGISTER_DESCRIPTION = "Onboards and registers a device";
    private static final String DEVICEREGISTRY_REGISTER_201_MESSAGE = "Device registered";
    private static final String DEVICEREGISTRY_REGISTER_400_MESSAGE = "Could not register device";

    private final Logger logger = LogManager.getLogger(DeviceRegistryOnboardingController.class);
    private final DeviceRegistryDBService deviceRegistryDBService;
    private final Validation validation;

    //=================================================================================================
    // methods
    
    //-------------------------------------------------------------------------------------------------
	@Autowired
    public DeviceRegistryOnboardingController(final DeviceRegistryDBService deviceRegistryDBService) {
        this.deviceRegistryDBService = deviceRegistryDBService;
        this.validation = new Validation();
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = DEVICEREGISTRY_REGISTER_DESCRIPTION, response = DeviceRegistryOnboardingWithNameResponseDTO.class, tags =
            {CoreCommonConstants.SWAGGER_TAG_CLIENT, CoreCommonConstants.SWAGGER_TAG_ONBOARDING})
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_CREATED, message = DEVICEREGISTRY_REGISTER_201_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = DEVICEREGISTRY_REGISTER_400_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @ResponseStatus(value = org.springframework.http.HttpStatus.CREATED)
    @PostMapping(path = CommonConstants.OP_DEVICEREGISTRY_ONBOARDING_WITH_NAME_URI,
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public DeviceRegistryOnboardingWithNameResponseDTO onboardDevice(final HttpServletRequest httpServletRequest,
                                                                     @RequestBody final DeviceRegistryOnboardingWithNameRequestDTO request) {
        logger.debug("New onboarding with name and device registration request received");
        validation.checkOnboardingRequest(request, getOrigin(CommonConstants.OP_DEVICEREGISTRY_ONBOARDING_WITH_NAME_URI));

        final String host = httpServletRequest.getRemoteHost();
        final String address = httpServletRequest.getRemoteAddr();
        final var response = deviceRegistryDBService.onboardAndRegisterDeviceRegistry(request, host, address);
        logger.debug("{} successfully registers its device {}", request.getDevice().getDeviceName(), request.getDevice());

        return response;
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = DEVICEREGISTRY_REGISTER_DESCRIPTION, response = DeviceRegistryOnboardingWithCsrResponseDTO.class, tags =
            {CoreCommonConstants.SWAGGER_TAG_CLIENT, CoreCommonConstants.SWAGGER_TAG_ONBOARDING})
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_CREATED, message = DEVICEREGISTRY_REGISTER_201_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = DEVICEREGISTRY_REGISTER_400_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @ResponseStatus(value = org.springframework.http.HttpStatus.CREATED)
    @PostMapping(path = CommonConstants.OP_DEVICEREGISTRY_ONBOARDING_WITH_CSR_URI,
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public DeviceRegistryOnboardingWithCsrResponseDTO onboardDevice(final HttpServletRequest httpServletRequest,
                                                                    @RequestBody final DeviceRegistryOnboardingWithCsrRequestDTO request) {
        logger.debug("New onboarding with csr and device registration request received");
        validation.checkOnboardingRequest(request, getOrigin(CommonConstants.OP_DEVICEREGISTRY_ONBOARDING_WITH_CSR_URI));

        final var response = deviceRegistryDBService.onboardAndRegisterDeviceRegistry(request);
        logger.debug("{} successfully registers its device {}", request.getDevice().getDeviceName(), request.getDevice());

        return response;
    }

    //=================================================================================================
    // assistant methods
    
    //-------------------------------------------------------------------------------------------------
	private String getBaseOrigin() {
        return CommonConstants.DEVICEREGISTRY_URI + CommonConstants.ONBOARDING_URI;
    }

    //-------------------------------------------------------------------------------------------------
	private String getOrigin(final String postfix) {
        Assert.notNull(postfix, "Internal error: Origin postfix not provided");
        return getBaseOrigin() + postfix;
    }
}