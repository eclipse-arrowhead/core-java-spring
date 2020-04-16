package eu.arrowhead.core.mscv.controller;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.CoreDefaults;
import eu.arrowhead.common.Defaults;
import eu.arrowhead.common.dto.shared.mscv.StandardDto;
import eu.arrowhead.core.mscv.database.service.MscvCrudService;
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

import static eu.arrowhead.core.mscv.Constants.PARAMETER_ADDRESS_PATH;
import static eu.arrowhead.core.mscv.Constants.PARAMETER_IDENTIFICATION;
import static eu.arrowhead.core.mscv.Constants.PARAMETER_NAME;
import static eu.arrowhead.core.mscv.Constants.PARAMETER_IDENTIFICATION_PATH;
import static eu.arrowhead.core.mscv.Constants.PARAMETER_PORT_PATH;
import static eu.arrowhead.core.mscv.Constants.PATH_ADDRESS;
import static eu.arrowhead.core.mscv.Constants.PATH_PORT;

@Api(tags = {CoreCommonConstants.SWAGGER_TAG_ALL})
@CrossOrigin(maxAge = Defaults.CORS_MAX_AGE, allowCredentials = Defaults.CORS_ALLOW_CREDENTIALS,
        allowedHeaders = {HttpHeaders.ORIGIN, HttpHeaders.CONTENT_TYPE, HttpHeaders.ACCEPT}
)
@RestController
@RequestMapping(CommonConstants.MSCV_URI + CoreCommonConstants.MGMT_URI)
public class StandardMgmtController {

    private static final String STANDARD_URI = "/standard";
    private static final String QUALIFY_STANDARD_URI = STANDARD_URI + PARAMETER_IDENTIFICATION_PATH;

    private static final String CREATE_STANDARD_URI = STANDARD_URI;
    private static final String CREATE_STANDARD_DESCRIPTION = "Create new MSCV standard";
    private static final String CREATE_STANDARD_SUCCESS = "New MSCV standard created";
    private static final String CREATE_STANDARD_BAD_REQUEST = "Unable to create new MSCV standard";

    private static final String READ_STANDARD_URI = QUALIFY_STANDARD_URI;
    private static final String READ_STANDARD_DESCRIPTION = "Get MSCV standard by identification";
    private static final String READ_STANDARD_SUCCESS = "MSCV standard returned";
    private static final String READ_STANDARD_BAD_REQUEST = "Unable to return MSCV standard";

    private static final String READ_ALL_STANDARD_URI = STANDARD_URI;
    private static final String READ_ALL_STANDARD_DESCRIPTION = "Get all MSCV standards";
    private static final String READ_ALL_STANDARD_SUCCESS = "All MSCV standards returned";
    private static final String READ_ALL_STANDARD_BAD_REQUEST = "Unable to return MSCV standards";

    private static final String UPDATE_STANDARD_URI = QUALIFY_STANDARD_URI;
    private static final String UPDATE_STANDARD_DESCRIPTION = "Update MSCV standard by identification";
    private static final String UPDATE_STANDARD_SUCCESS = "MSCV standard updated";
    private static final String UPDATE_STANDARD_BAD_REQUEST = "Unable to update MSCV standard";

    private static final String DELETE_STANDARD_URI = QUALIFY_STANDARD_URI;
    private static final String DELETE_STANDARD_DESCRIPTION = "Delete MSCV standard by identification";
    private static final String DELETE_STANDARD_SUCCESS = "MSCV standard deleted";
    private static final String DELETE_STANDARD_BAD_REQUEST = "Unable to delete MSCV standard";

    private final Logger logger = LogManager.getLogger();
    private final MscvCrudService crudService;

    @Autowired
    public StandardMgmtController(final MscvCrudService crudService) {
        super();
        this.crudService = crudService;
    }

    //=================================================================================================
    // methods
    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = CREATE_STANDARD_DESCRIPTION, response = StandardDto.class, tags = {CoreCommonConstants.SWAGGER_TAG_MGMT})
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = CREATE_STANDARD_SUCCESS),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = CREATE_STANDARD_BAD_REQUEST),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @PostMapping(CREATE_STANDARD_URI)
    @ResponseBody
    public StandardDto create(@RequestBody final StandardDto dto) {
        logger.debug("create started ...");
        // TODO implement
        return null;
    }
    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = READ_STANDARD_DESCRIPTION, response = StandardDto.class, tags = {CoreCommonConstants.SWAGGER_TAG_MGMT})
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = READ_STANDARD_SUCCESS),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = READ_STANDARD_BAD_REQUEST),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @GetMapping(READ_STANDARD_URI)
    @ResponseBody
    public StandardDto read(@PathVariable(PARAMETER_IDENTIFICATION) final String identification) {
        logger.debug("read started ...");
        // TODO implement
        return null;
    }
    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = READ_ALL_STANDARD_DESCRIPTION, response = StandardDto.class, tags = {CoreCommonConstants.SWAGGER_TAG_MGMT})
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = READ_ALL_STANDARD_SUCCESS),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = READ_ALL_STANDARD_BAD_REQUEST),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @GetMapping(READ_ALL_STANDARD_URI)
    @ResponseBody
    public StandardDto readAll(
            @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_PAGE, required = false) final Integer page,
            @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_ITEM_PER_PAGE, required = false) final Integer size,
            @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_DIRECTION, defaultValue = CoreDefaults.DEFAULT_REQUEST_PARAM_DIRECTION_VALUE) final String direction,
            @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_SORT_FIELD, defaultValue = CoreCommonConstants.COMMON_FIELD_NAME_ID) final String sortField) {
        logger.debug("readAll started ...");
        // TODO implement
        return null;
    }
    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = UPDATE_STANDARD_DESCRIPTION, response = StandardDto.class, tags = {CoreCommonConstants.SWAGGER_TAG_MGMT})
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = UPDATE_STANDARD_SUCCESS),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = UPDATE_STANDARD_BAD_REQUEST),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @PutMapping(UPDATE_STANDARD_URI)
    @ResponseBody
    public StandardDto update(@PathVariable(PARAMETER_IDENTIFICATION) final String identification, @RequestBody final StandardDto dto) {
        logger.debug("update started ...");
        // TODO implement
        return null;
    }
    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = DELETE_STANDARD_DESCRIPTION, response = StandardDto.class, tags = {CoreCommonConstants.SWAGGER_TAG_MGMT})
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = DELETE_STANDARD_SUCCESS),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = DELETE_STANDARD_BAD_REQUEST),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @DeleteMapping(DELETE_STANDARD_URI)
    @ResponseBody
    public void delete(@PathVariable(PARAMETER_IDENTIFICATION) final String identification) {
        logger.debug("delete started ...");
        // TODO implement
    }
}
