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
import eu.arrowhead.common.dto.shared.CertificateType;
import eu.arrowhead.common.dto.shared.SystemQueryFormDTO;
import eu.arrowhead.common.dto.shared.SystemQueryResultDTO;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
@RequestMapping(CommonConstants.SYSTEM_REGISTRY_URI)
public class SystemRegistryController {

    //=================================================================================================
    // members

    private static final String PATH_VARIABLE_ID = "id";
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

    private final Logger logger = LogManager.getLogger(SystemRegistryController.class);

    private final SystemRegistryDBService systemRegistryDBService;
    private final Validation validation;

    @Autowired
    public SystemRegistryController(final SystemRegistryDBService systemRegistryDBService) {
        this.systemRegistryDBService = systemRegistryDBService;
        this.validation = new Validation();
    }


    //=================================================================================================
    // methods

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = "Return an echo message with the purpose of testing the core system availability", response = String.class, tags = {CoreCommonConstants.SWAGGER_TAG_CLIENT})
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
    @ApiOperation(value = SYSTEM_REGISTRY_REGISTER_DESCRIPTION, response = SystemRegistryResponseDTO.class, tags = {CoreCommonConstants.SWAGGER_TAG_CLIENT})
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_CREATED, message = SYSTEM_REGISTRY_REGISTER_201_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = SYSTEM_REGISTRY_REGISTER_400_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @ResponseStatus(value = org.springframework.http.HttpStatus.CREATED)
    @PostMapping(path = CommonConstants.OP_SYSTEM_REGISTRY_REGISTER_URI,
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public SystemRegistryResponseDTO registerSystem(@RequestBody final SystemRegistryRequestDTO request) {
        logger.debug("New system registration request received");
        validation.checkSystemRegistryRequest(request, getOrigin(CommonConstants.OP_SYSTEM_REGISTRY_REGISTER_URI),false);

        final SystemRegistryResponseDTO response = systemRegistryDBService.registerSystemRegistry(request);
        logger.debug("{} successfully registers its system {}", request.getSystem().getSystemName(), request.getSystem());

        return response;
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = SYSTEM_REGISTRY_UNREGISTER_DESCRIPTION, tags = {CoreCommonConstants.SWAGGER_TAG_CLIENT})
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = SYSTEM_REGISTRY_UNREGISTER_200_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = SYSTEM_REGISTRY_UNREGISTER_400_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @DeleteMapping(path = CommonConstants.OP_SYSTEM_REGISTRY_UNREGISTER_URI)
    public void unregisterSystem(@RequestParam(CommonConstants.OP_SYSTEM_REGISTRY_UNREGISTER_REQUEST_PARAM_PROVIDER_SYSTEM_NAME) final String systemName,
                                 @RequestParam(CommonConstants.OP_SYSTEM_REGISTRY_UNREGISTER_REQUEST_PARAM_PROVIDER_ADDRESS) final String address,
                                 @RequestParam(CommonConstants.OP_SYSTEM_REGISTRY_UNREGISTER_REQUEST_PARAM_PROVIDER_PORT) final int port) {
        logger.debug("System removal request received");
        validation.checkUnregisterSystemParameters(systemName, address, port, getOrigin(CommonConstants.OP_SYSTEM_REGISTRY_UNREGISTER_URI));

        systemRegistryDBService.removeSystemRegistryByNameAndAddressAndPort(systemName, address, port);
        logger.debug("{} successfully removed", systemName);
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = SYSTEM_REGISTRY_QUERY_DESCRIPTION, response = SystemQueryResultDTO.class, tags = {CoreCommonConstants.SWAGGER_TAG_CLIENT})
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = SYSTEM_REGISTRY_QUERY_200_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = SYSTEM_REGISTRY_QUERY_400_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @PostMapping(path = CommonConstants.OP_SYSTEM_REGISTRY_QUERY_URI, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public SystemQueryResultDTO queryRegistry(@RequestBody final SystemQueryFormDTO form) {
        logger.debug("System query request received");

        validation.checkSystemNameRequirements(form.getSystemNameRequirements(), getOrigin(CommonConstants.OP_SYSTEM_REGISTRY_QUERY_URI));

        final SystemQueryResultDTO result = systemRegistryDBService.queryRegistry(form);
        logger.debug("Return {} providers for system {}", result.getSystemQueryData().size(), form.getSystemNameRequirements());

        return result;
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = SYSTEM_REGISTRY_QUERY_BY_SYSTEM_ID_DESCRIPTION, response = SystemResponseDTO.class, tags = {CoreCommonConstants.SWAGGER_TAG_PRIVATE})
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = SYSTEM_REGISTRY_QUERY_BY_SYSTEM_ID_200_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = SYSTEM_REGISTRY_QUERY_BY_SYSTEM_ID_400_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @GetMapping(path = CoreCommonConstants.OP_SYSTEM_REGISTRY_QUERY_BY_SYSTEM_ID_URI, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public SystemResponseDTO queryRegistryBySystemId(@PathVariable(value = PATH_VARIABLE_ID) final long systemId) {
        logger.debug("System query by system id request received");

        final SystemResponseDTO result = systemRegistryDBService.getSystemById(systemId);

        logger.debug("Return system by id: {}", systemId);
        return result;
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = SYSTEM_REGISTRY_QUERY_BY_SYSTEM_DTO_DESCRIPTION, response = SystemResponseDTO.class, tags = {CoreCommonConstants.SWAGGER_TAG_PRIVATE})
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = SYSTEM_REGISTRY_QUERY_BY_SYSTEM_DTO_200_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = SYSTEM_REGISTRY_QUERY_BY_SYSTEM_DTO_400_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @PostMapping(path = CoreCommonConstants.OP_SYSTEM_REGISTRY_QUERY_BY_SYSTEM_DTO_URI, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public SystemResponseDTO queryRegistryBySystemDTO(@RequestBody final SystemRequestDTO request) {
        logger.debug("System query by systemRequestDTO request received");

        validation.checkSystemRequest(request, CommonConstants.SYSTEM_REGISTRY_URI + CoreCommonConstants.OP_SYSTEM_REGISTRY_QUERY_BY_SYSTEM_ID_URI, false);

        final String systemName = request.getSystemName();
        final String address = request.getAddress();
        final int port = request.getPort();

        final SystemResponseDTO result = systemRegistryDBService.getSystemDtoByNameAndAddressAndPort(systemName, address, port);

        logger.debug("Return system by name: {}, address: {}, port: {}", systemName, address, port);
        return result;
    }

    //=================================================================================================
    // assistant methods


    //=================================================================================================
    // assistant methods
    private String getBaseOrigin() {
        return CommonConstants.SYSTEM_REGISTRY_URI;
    }

    private String getOrigin(final String postfix) {
        Assert.notNull(postfix, "Internal error: Origin postfix not provided");
        return getBaseOrigin() + postfix;
    }

}