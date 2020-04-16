package eu.arrowhead.core.mscv.controller;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.CoreDefaults;
import eu.arrowhead.common.Defaults;
import eu.arrowhead.common.dto.shared.mscv.DomainDto;
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

import static eu.arrowhead.core.mscv.Constants.PARAMETER_NAME;
import static eu.arrowhead.core.mscv.Constants.PARAMETER_NAME_PATH;

@Api(tags = {CoreCommonConstants.SWAGGER_TAG_ALL})
@CrossOrigin(maxAge = Defaults.CORS_MAX_AGE, allowCredentials = Defaults.CORS_ALLOW_CREDENTIALS,
        allowedHeaders = {HttpHeaders.ORIGIN, HttpHeaders.CONTENT_TYPE, HttpHeaders.ACCEPT}
)
@RestController
@RequestMapping(CommonConstants.MSCV_URI + CoreCommonConstants.MGMT_URI)
public class DomainMgmtController {

    private static final String DOMAIN_URI = "/domain";
    private static final String QUALIFY_DOMAIN_URI = DOMAIN_URI + PARAMETER_NAME_PATH;

    private static final String CREATE_DOMAIN_URI = DOMAIN_URI;
    private static final String CREATE_DOMAIN_DESCRIPTION = "Create new MSCV domain";
    private static final String CREATE_DOMAIN_SUCCESS = "New MSCV domain created";
    private static final String CREATE_DOMAIN_BAD_REQUEST = "Unable to create new MSCV domain";

    private static final String READ_DOMAIN_URI = QUALIFY_DOMAIN_URI;
    private static final String READ_DOMAIN_DESCRIPTION = "Get MSCV domain by name";
    private static final String READ_DOMAIN_SUCCESS = "MSCV domain returned";
    private static final String READ_DOMAIN_BAD_REQUEST = "Unable to return MSCV domain";

    private static final String READ_ALL_DOMAIN_URI = DOMAIN_URI;
    private static final String READ_ALL_DOMAIN_DESCRIPTION = "Get all MSCV domains";
    private static final String READ_ALL_DOMAIN_SUCCESS = "All MSCV domains returned";
    private static final String READ_ALL_DOMAIN_BAD_REQUEST = "Unable to return MSCV domains";

    private static final String UPDATE_DOMAIN_URI = QUALIFY_DOMAIN_URI;
    private static final String UPDATE_DOMAIN_DESCRIPTION = "Update MSCV domain";
    private static final String UPDATE_DOMAIN_SUCCESS = "MSCV domain updated";
    private static final String UPDATE_DOMAIN_BAD_REQUEST = "Unable to update MSCV domain";

    private static final String DELETE_DOMAIN_URI = QUALIFY_DOMAIN_URI;
    private static final String DELETE_DOMAIN_DESCRIPTION = "Delete MSCV domain";
    private static final String DELETE_DOMAIN_SUCCESS = "MSCV domain deleted";
    private static final String DELETE_DOMAIN_BAD_REQUEST = "Unable to delete MSCV domain";

    private final Logger logger = LogManager.getLogger();
    private final MscvCrudService crudService;

    @Autowired
    public DomainMgmtController(final MscvCrudService crudService) {
        super();
        this.crudService = crudService;
    }

    //=================================================================================================
    // methods
    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = CREATE_DOMAIN_DESCRIPTION, response = DomainDto.class, tags = {CoreCommonConstants.SWAGGER_TAG_MGMT})
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = CREATE_DOMAIN_SUCCESS),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = CREATE_DOMAIN_BAD_REQUEST),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @PostMapping(CREATE_DOMAIN_URI)
    @ResponseBody
    public DomainDto create(@RequestBody final DomainDto dto) {
        logger.debug("create started ...");
        // TODO implement
        return null;
    }
    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = READ_DOMAIN_DESCRIPTION, response = DomainDto.class, tags = {CoreCommonConstants.SWAGGER_TAG_MGMT})
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = READ_DOMAIN_SUCCESS),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = READ_DOMAIN_BAD_REQUEST),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @GetMapping(READ_DOMAIN_URI)
    @ResponseBody
    public DomainDto read(@PathVariable(PARAMETER_NAME) final String name) {
        logger.debug("read started ...");
        // TODO implement
        return null;
    }
    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = READ_ALL_DOMAIN_DESCRIPTION, response = DomainDto.class, tags = {CoreCommonConstants.SWAGGER_TAG_MGMT})
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = READ_ALL_DOMAIN_SUCCESS),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = READ_ALL_DOMAIN_BAD_REQUEST),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @GetMapping(READ_ALL_DOMAIN_URI)
    @ResponseBody
    public DomainDto readAll(
            @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_PAGE, required = false) final Integer page,
            @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_ITEM_PER_PAGE, required = false) final Integer size,
            @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_DIRECTION, defaultValue = CoreDefaults.DEFAULT_REQUEST_PARAM_DIRECTION_VALUE) final String direction,
            @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_SORT_FIELD, defaultValue = CoreCommonConstants.COMMON_FIELD_NAME_ID) final String sortField) {
        logger.debug("readAll started ...");
        // TODO implement
        return null;
    }
    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = UPDATE_DOMAIN_DESCRIPTION, response = DomainDto.class, tags = {CoreCommonConstants.SWAGGER_TAG_MGMT})
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = UPDATE_DOMAIN_SUCCESS),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = UPDATE_DOMAIN_BAD_REQUEST),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @PutMapping(UPDATE_DOMAIN_URI)
    @ResponseBody
    public DomainDto update(@PathVariable(PARAMETER_NAME) final String name, @RequestBody final DomainDto dto) {
        logger.debug("update started ...");
        // TODO implement
        return null;
    }
    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = DELETE_DOMAIN_DESCRIPTION, response = DomainDto.class, tags = {CoreCommonConstants.SWAGGER_TAG_MGMT})
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = DELETE_DOMAIN_SUCCESS),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = DELETE_DOMAIN_BAD_REQUEST),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @DeleteMapping(DELETE_DOMAIN_URI)
    @ResponseBody
    public void delete(@PathVariable(PARAMETER_NAME) final String name) {
        logger.debug("delete started ...");
        // TODO implement
    }
}
