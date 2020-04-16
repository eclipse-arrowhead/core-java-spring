package eu.arrowhead.core.mscv.controller;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.CoreDefaults;
import eu.arrowhead.common.Defaults;
import eu.arrowhead.common.dto.shared.mscv.SshTargetDto;
import eu.arrowhead.core.mscv.service.MscvCrudService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import static eu.arrowhead.core.mscv.Constants.PARAMETER_ADDRESS;
import static eu.arrowhead.core.mscv.Constants.PARAMETER_ADDRESS_PATH;
import static eu.arrowhead.core.mscv.Constants.PARAMETER_PORT;
import static eu.arrowhead.core.mscv.Constants.PARAMETER_PORT_PATH;
import static eu.arrowhead.core.mscv.Constants.PATH_ADDRESS;
import static eu.arrowhead.core.mscv.Constants.PATH_PORT;

@Api(tags = {CoreCommonConstants.SWAGGER_TAG_ALL})
@CrossOrigin(maxAge = Defaults.CORS_MAX_AGE, allowCredentials = Defaults.CORS_ALLOW_CREDENTIALS,
        allowedHeaders = {HttpHeaders.ORIGIN, HttpHeaders.CONTENT_TYPE, HttpHeaders.ACCEPT}
)
@RestController
@RequestMapping(CommonConstants.MSCV_URI + CoreCommonConstants.MGMT_URI)
public class TargetMgmtController {

    private static final String TARGET_URI = "/target";
    private static final String QUALIFY_TARGET_URI = TARGET_URI +
            PATH_ADDRESS + PARAMETER_ADDRESS_PATH +
            PATH_PORT + PARAMETER_PORT_PATH;

    private static final String CREATE_TARGET_URI = TARGET_URI;
    private static final String CREATE_TARGET_DESCRIPTION = "Create new MSCV target";
    private static final String CREATE_TARGET_SUCCESS = "New MSCV target created";
    private static final String CREATE_TARGET_BAD_REQUEST = "Unable to create new MSCV target";

    private static final String READ_TARGET_URI = QUALIFY_TARGET_URI;
    private static final String READ_TARGET_DESCRIPTION = "Get MSCV target by name";
    private static final String READ_TARGET_SUCCESS = "MSCV target returned";
    private static final String READ_TARGET_BAD_REQUEST = "Unable to return MSCV target";

    private static final String READ_ALL_TARGET_URI = TARGET_URI;
    private static final String READ_ALL_TARGET_DESCRIPTION = "Get all MSCV targets";
    private static final String READ_ALL_TARGET_SUCCESS = "All MSCV targets returned";
    private static final String READ_ALL_TARGET_BAD_REQUEST = "Unable to return MSCV targets";

    private static final String UPDATE_TARGET_URI = QUALIFY_TARGET_URI;
    private static final String UPDATE_TARGET_DESCRIPTION = "Update MSCV target";
    private static final String UPDATE_TARGET_SUCCESS = "MSCV target updated";
    private static final String UPDATE_TARGET_BAD_REQUEST = "Unable to update MSCV target";

    private static final String DELETE_TARGET_URI = QUALIFY_TARGET_URI;
    private static final String DELETE_TARGET_DESCRIPTION = "Delete MSCV target";
    private static final String DELETE_TARGET_SUCCESS = "MSCV target deleted";
    private static final String DELETE_TARGET_BAD_REQUEST = "Unable to delete MSCV target";

    private final Logger logger = LogManager.getLogger();
    private final MscvCrudService crudService;

    @Autowired
    public TargetMgmtController(final MscvCrudService crudService) {
        super();
        this.crudService = crudService;
    }

    //=================================================================================================
    // methods
    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = CREATE_TARGET_DESCRIPTION, response = SshTargetDto.class, tags = {CoreCommonConstants.SWAGGER_TAG_MGMT})
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = CREATE_TARGET_SUCCESS),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = CREATE_TARGET_BAD_REQUEST),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @PostMapping(CREATE_TARGET_URI)
    @ResponseBody
    public SshTargetDto create(@RequestBody final SshTargetDto dto) {
        logger.debug("create started ...");
        // TODO implement
        return null;
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = READ_TARGET_DESCRIPTION, response = SshTargetDto.class, tags = {CoreCommonConstants.SWAGGER_TAG_MGMT})
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = READ_TARGET_SUCCESS),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = READ_TARGET_BAD_REQUEST),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @GetMapping(READ_TARGET_URI)
    @ResponseBody
    public SshTargetDto read(@PathVariable(PARAMETER_ADDRESS) final String address,
                             @PathVariable(PARAMETER_PORT) final String port) {
        logger.debug("read started ...");
        // TODO implement
        return null;
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = READ_ALL_TARGET_DESCRIPTION, response = SshTargetDto.class, tags = {CoreCommonConstants.SWAGGER_TAG_MGMT})
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = READ_ALL_TARGET_SUCCESS),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = READ_ALL_TARGET_BAD_REQUEST),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @GetMapping(READ_ALL_TARGET_URI)
    @ResponseBody
    public SshTargetDto readAll(
            @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_PAGE, required = false) final Integer page,
            @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_ITEM_PER_PAGE, required = false) final Integer size,
            @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_DIRECTION, defaultValue = CoreDefaults.DEFAULT_REQUEST_PARAM_DIRECTION_VALUE) final String direction,
            @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_SORT_FIELD, defaultValue = CoreCommonConstants.COMMON_FIELD_NAME_ID) final String sortField) {
        logger.debug("readAll started ...");
        // TODO implement
        return null;
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = UPDATE_TARGET_DESCRIPTION, response = SshTargetDto.class, tags = {CoreCommonConstants.SWAGGER_TAG_MGMT})
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = UPDATE_TARGET_SUCCESS),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = UPDATE_TARGET_BAD_REQUEST),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @PutMapping(UPDATE_TARGET_URI)
    @ResponseBody
    public SshTargetDto update(@PathVariable(PARAMETER_ADDRESS) final String address,
                               @PathVariable(PARAMETER_PORT) final String port,
                               @RequestBody final SshTargetDto dto) {
        logger.debug("update started ...");
        // TODO implement
        return null;
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = DELETE_TARGET_DESCRIPTION, response = SshTargetDto.class, tags = {CoreCommonConstants.SWAGGER_TAG_MGMT})
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = DELETE_TARGET_SUCCESS),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = DELETE_TARGET_BAD_REQUEST),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @DeleteMapping(DELETE_TARGET_URI)
    @ResponseBody
    public void delete(@PathVariable(PARAMETER_ADDRESS) final String address,
                       @PathVariable(PARAMETER_PORT) final String port) {
        logger.debug("delete started ...");
        // TODO implement
    }
}
