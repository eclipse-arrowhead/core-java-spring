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
import eu.arrowhead.common.Defaults;
import eu.arrowhead.common.dto.shared.SystemRegistryOnboardingWithCsrRequestDTO;
import eu.arrowhead.common.dto.shared.SystemRegistryOnboardingWithCsrResponseDTO;
import eu.arrowhead.common.dto.shared.SystemRegistryOnboardingWithNameRequestDTO;
import eu.arrowhead.common.dto.shared.SystemRegistryOnboardingWithNameResponseDTO;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@Api(tags = {CoreCommonConstants.SWAGGER_TAG_ALL})
@CrossOrigin(maxAge = Defaults.CORS_MAX_AGE, allowCredentials = Defaults.CORS_ALLOW_CREDENTIALS,
        allowedHeaders = {HttpHeaders.ORIGIN, HttpHeaders.CONTENT_TYPE, HttpHeaders.ACCEPT, HttpHeaders.AUTHORIZATION}
)
@RestController
@RequestMapping(CommonConstants.SYSTEMREGISTRY_URI + CommonConstants.ONBOARDING_URI)
public class SystemRegistryOnboardingController {

    //=================================================================================================
    // members

    private static final String SYSTEMREGISTRY_REGISTER_DESCRIPTION = "Registers a system";
    private static final String SYSTEMREGISTRY_REGISTER_201_MESSAGE = "System registered";
    private static final String SYSTEMREGISTRY_REGISTER_400_MESSAGE = "Could not register system";

    private final Logger logger = LogManager.getLogger(SystemRegistryOnboardingController.class);

    private final SystemRegistryDBService systemRegistryDBService;
    private final Validation validation;

    //=================================================================================================
    // methods

    //-------------------------------------------------------------------------------------------------
    @Autowired
    public SystemRegistryOnboardingController(final SystemRegistryDBService systemRegistryDBService) {
    	this.systemRegistryDBService = systemRegistryDBService;
    	this.validation = new Validation();
    }
    
    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = SYSTEMREGISTRY_REGISTER_DESCRIPTION, response = SystemRegistryOnboardingWithNameResponseDTO.class, tags =
            {CoreCommonConstants.SWAGGER_TAG_CLIENT, CoreCommonConstants.SWAGGER_TAG_ONBOARDING})
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_CREATED, message = SYSTEMREGISTRY_REGISTER_201_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = SYSTEMREGISTRY_REGISTER_400_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @ResponseStatus(value = org.springframework.http.HttpStatus.CREATED)
    @PostMapping(path = CommonConstants.OP_SYSTEMREGISTRY_ONBOARDING_WITH_NAME_URI,
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public SystemRegistryOnboardingWithNameResponseDTO onboardDevice(final HttpServletRequest httpServletRequest,
                                                                     @RequestBody final SystemRegistryOnboardingWithNameRequestDTO request) {
        logger.debug("New onboarding with name and system registration request received");
        validation.checkOnboardingRequest(request, getOrigin(CommonConstants.OP_SYSTEMREGISTRY_ONBOARDING_WITH_NAME_URI));

        final String host = httpServletRequest.getRemoteHost();
        final String address = httpServletRequest.getRemoteAddr();
        final var response = systemRegistryDBService.onboardAndRegisterSystemRegistry(request, host, address);
        logger.debug("{} successfully registers its system {}", request.getSystem().getSystemName(), request.getSystem());

        return response;
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = SYSTEMREGISTRY_REGISTER_DESCRIPTION, response = SystemRegistryOnboardingWithCsrResponseDTO.class, tags =
            {CoreCommonConstants.SWAGGER_TAG_CLIENT, CoreCommonConstants.SWAGGER_TAG_ONBOARDING})
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_CREATED, message = SYSTEMREGISTRY_REGISTER_201_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = SYSTEMREGISTRY_REGISTER_400_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @ResponseStatus(value = org.springframework.http.HttpStatus.CREATED)
    @PostMapping(path = CommonConstants.OP_SYSTEMREGISTRY_ONBOARDING_WITH_CSR_URI,
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public SystemRegistryOnboardingWithCsrResponseDTO onboardDevice(@RequestBody final SystemRegistryOnboardingWithCsrRequestDTO request) {
        logger.debug("New onboarding with csr and system registration request received");
        validation.checkOnboardingRequest(request, getOrigin(CommonConstants.OP_SYSTEMREGISTRY_ONBOARDING_WITH_CSR_URI));

        final var response = systemRegistryDBService.onboardAndRegisterSystemRegistry(request);
        logger.debug("{} successfully registers its system {}", request.getSystem().getSystemName(), request.getSystem());

        return response;
    }

    //=================================================================================================
    // assistant methods

    //-------------------------------------------------------------------------------------------------
	private String getBaseOrigin() {
        return CommonConstants.SYSTEMREGISTRY_URI + CommonConstants.ONBOARDING_URI;
    }

    //-------------------------------------------------------------------------------------------------
	private String getOrigin(final String postfix) {
        Assert.notNull(postfix, "Internal error: Origin postfix not provided");
        return getBaseOrigin() + postfix;
    }
}