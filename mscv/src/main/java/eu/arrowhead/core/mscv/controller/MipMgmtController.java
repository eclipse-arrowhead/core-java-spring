package eu.arrowhead.core.mscv.controller;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.CoreDefaults;
import eu.arrowhead.common.Defaults;
import eu.arrowhead.common.dto.shared.mscv.MipDto;
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
import static eu.arrowhead.core.mscv.Constants.PARAMETER_MIP_ID;
import static eu.arrowhead.core.mscv.Constants.PARAMETER_MIP_ID_PATH;
import static eu.arrowhead.core.mscv.Constants.PARAMETER_NAME;
import static eu.arrowhead.core.mscv.Constants.PARAMETER_NAME_PATH;
import static eu.arrowhead.core.mscv.Constants.PARAMETER_PORT_PATH;
import static eu.arrowhead.core.mscv.Constants.PATH_ADDRESS;
import static eu.arrowhead.core.mscv.Constants.PATH_PORT;

@Api(tags = {CoreCommonConstants.SWAGGER_TAG_ALL})
@CrossOrigin(maxAge = Defaults.CORS_MAX_AGE, allowCredentials = Defaults.CORS_ALLOW_CREDENTIALS,
        allowedHeaders = {HttpHeaders.ORIGIN, HttpHeaders.CONTENT_TYPE, HttpHeaders.ACCEPT}
)
@RestController
@RequestMapping(CommonConstants.MSCV_URI + CoreCommonConstants.MGMT_URI)
public class MipMgmtController {

    private static final String MIP_URI = "/mip";
    private static final String QUALIFY_MIP_URI = MIP_URI + PARAMETER_MIP_ID_PATH;

    private static final String CREATE_MIP_URI = MIP_URI;
    private static final String CREATE_MIP_DESCRIPTION = "Create new MSCV mip";
    private static final String CREATE_MIP_SUCCESS = "New MSCV mip created";
    private static final String CREATE_MIP_BAD_REQUEST = "Unable to create new MSCV mip";

    private static final String READ_MIP_URI = QUALIFY_MIP_URI;
    private static final String READ_MIP_DESCRIPTION = "Get MSCV mip by name";
    private static final String READ_MIP_SUCCESS = "MSCV mip returned";
    private static final String READ_MIP_BAD_REQUEST = "Unable to return MSCV mip";

    private static final String READ_ALL_MIP_URI = MIP_URI;
    private static final String READ_ALL_MIP_DESCRIPTION = "Get all MSCV mips";
    private static final String READ_ALL_MIP_SUCCESS = "All MSCV mips returned";
    private static final String READ_ALL_MIP_BAD_REQUEST = "Unable to return MSCV mips";

    private static final String UPDATE_MIP_URI = QUALIFY_MIP_URI;
    private static final String UPDATE_MIP_DESCRIPTION = "Update MSCV mip";
    private static final String UPDATE_MIP_SUCCESS = "MSCV mip updated";
    private static final String UPDATE_MIP_BAD_REQUEST = "Unable to update MSCV mip";

    private static final String DELETE_MIP_URI = QUALIFY_MIP_URI;
    private static final String DELETE_MIP_DESCRIPTION = "Delete MSCV mip";
    private static final String DELETE_MIP_SUCCESS = "MSCV mip deleted";
    private static final String DELETE_MIP_BAD_REQUEST = "Unable to delete MSCV mip";

    private final Logger logger = LogManager.getLogger();
    private final MscvCrudService crudService;

    @Autowired
    public MipMgmtController(final MscvCrudService crudService) {
        super();
        this.crudService = crudService;
    }

    //=================================================================================================
    // methods
    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = CREATE_MIP_DESCRIPTION, response = MipDto.class, tags = {CoreCommonConstants.SWAGGER_TAG_MGMT})
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = CREATE_MIP_SUCCESS),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = CREATE_MIP_BAD_REQUEST),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @PostMapping(CREATE_MIP_URI)
    @ResponseBody
    public MipDto create(@RequestBody final MipDto dto) {
        logger.debug("create started ...");
        // TODO implement
        return null;
    }
    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = READ_MIP_DESCRIPTION, response = MipDto.class, tags = {CoreCommonConstants.SWAGGER_TAG_MGMT})
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = READ_MIP_SUCCESS),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = READ_MIP_BAD_REQUEST),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @GetMapping(READ_MIP_URI)
    @ResponseBody
    public MipDto read(@PathVariable(PARAMETER_MIP_ID) final Long mipId) {
        logger.debug("read started ...");
        // TODO implement
        return null;
    }
    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = READ_ALL_MIP_DESCRIPTION, response = MipDto.class, tags = {CoreCommonConstants.SWAGGER_TAG_MGMT})
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = READ_ALL_MIP_SUCCESS),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = READ_ALL_MIP_BAD_REQUEST),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @GetMapping(READ_ALL_MIP_URI)
    @ResponseBody
    public MipDto readAll(
            @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_PAGE, required = false) final Integer page,
            @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_ITEM_PER_PAGE, required = false) final Integer size,
            @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_DIRECTION, defaultValue = CoreDefaults.DEFAULT_REQUEST_PARAM_DIRECTION_VALUE) final String direction,
            @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_SORT_FIELD, defaultValue = CoreCommonConstants.COMMON_FIELD_NAME_ID) final String sortField) {
        logger.debug("readAll started ...");
        // TODO implement
        return null;
    }
    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = UPDATE_MIP_DESCRIPTION, response = MipDto.class, tags = {CoreCommonConstants.SWAGGER_TAG_MGMT})
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = UPDATE_MIP_SUCCESS),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = UPDATE_MIP_BAD_REQUEST),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @PutMapping(UPDATE_MIP_URI)
    @ResponseBody
    public MipDto update(@PathVariable(PARAMETER_MIP_ID) final Long mipId, @RequestBody final MipDto dto) {
        logger.debug("update started ...");
        // TODO implement
        return null;
    }
    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = DELETE_MIP_DESCRIPTION, response = MipDto.class, tags = {CoreCommonConstants.SWAGGER_TAG_MGMT})
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = DELETE_MIP_SUCCESS),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = DELETE_MIP_BAD_REQUEST),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @DeleteMapping(DELETE_MIP_URI)
    @ResponseBody
    public void delete(@PathVariable(PARAMETER_MIP_ID) final Long mipId) {
        logger.debug("delete started ...");
        // TODO implement
    }
}
