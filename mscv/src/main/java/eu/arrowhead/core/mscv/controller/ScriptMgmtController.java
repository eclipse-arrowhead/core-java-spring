package eu.arrowhead.core.mscv.controller;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.CoreDefaults;
import eu.arrowhead.common.Defaults;
import eu.arrowhead.common.dto.shared.ErrorMessageDTO;
import eu.arrowhead.common.dto.shared.mscv.Layer;
import eu.arrowhead.common.dto.shared.mscv.OS;
import eu.arrowhead.common.dto.shared.mscv.ScriptDto;
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

import static eu.arrowhead.core.mscv.Constants.PARAMETER_LAYER;
import static eu.arrowhead.core.mscv.Constants.PARAMETER_LAYER_PATH;
import static eu.arrowhead.core.mscv.Constants.PARAMETER_MIP_ID;
import static eu.arrowhead.core.mscv.Constants.PARAMETER_MIP_ID_PATH;
import static eu.arrowhead.core.mscv.Constants.PARAMETER_OS;
import static eu.arrowhead.core.mscv.Constants.PARAMETER_OS_PATH;
import static eu.arrowhead.core.mscv.Constants.PATH_LAYER;
import static eu.arrowhead.core.mscv.Constants.PATH_MIP_ID;
import static eu.arrowhead.core.mscv.Constants.PATH_OS;

@Api(tags = {CoreCommonConstants.SWAGGER_TAG_ALL})
@CrossOrigin(maxAge = Defaults.CORS_MAX_AGE, allowCredentials = Defaults.CORS_ALLOW_CREDENTIALS,
        allowedHeaders = {HttpHeaders.ORIGIN, HttpHeaders.CONTENT_TYPE, HttpHeaders.ACCEPT}
)
@RestController
@RequestMapping(CommonConstants.MSCV_URI + CoreCommonConstants.MGMT_URI)
public class ScriptMgmtController {

    private static final String SCRIPT_URI = "/script";
    private static final String QUALIFY_SCRIPT_URI = SCRIPT_URI +
            PATH_MIP_ID + PARAMETER_MIP_ID_PATH +
            PATH_OS + PARAMETER_OS_PATH +
            PATH_LAYER + PARAMETER_LAYER_PATH;

    private static final String CREATE_SCRIPT_URI = SCRIPT_URI;
    private static final String CREATE_SCRIPT_DESCRIPTION = "Create new MSCV script";
    private static final String CREATE_SCRIPT_SUCCESS = "New MSCV script created";
    private static final String CREATE_SCRIPT_BAD_REQUEST = "Unable to create new MSCV script";

    private static final String READ_SCRIPT_URI = QUALIFY_SCRIPT_URI;
    private static final String READ_SCRIPT_DESCRIPTION = "Get MSCV script by mip id, layer and os";
    private static final String READ_SCRIPT_SUCCESS = "MSCV script returned";
    private static final String READ_SCRIPT_BAD_REQUEST = "Unable to return MSCV script";

    private static final String READ_ALL_SCRIPT_URI = SCRIPT_URI;
    private static final String READ_ALL_SCRIPT_DESCRIPTION = "Get all MSCV scripts";
    private static final String READ_ALL_SCRIPT_SUCCESS = "All MSCV scripts returned";
    private static final String READ_ALL_SCRIPT_BAD_REQUEST = "Unable to return MSCV scripts";

    private static final String UPDATE_SCRIPT_URI = QUALIFY_SCRIPT_URI;
    private static final String UPDATE_SCRIPT_DESCRIPTION = "Update MSCV script";
    private static final String UPDATE_SCRIPT_SUCCESS = "MSCV script updated";
    private static final String UPDATE_SCRIPT_BAD_REQUEST = "Unable to update MSCV script";

    private static final String DELETE_SCRIPT_URI = QUALIFY_SCRIPT_URI;
    private static final String DELETE_SCRIPT_DESCRIPTION = "Delete MSCV script";
    private static final String DELETE_SCRIPT_SUCCESS = "MSCV script deleted";
    private static final String DELETE_SCRIPT_BAD_REQUEST = "Unable to delete MSCV script";

    private final Logger logger = LogManager.getLogger();
    private final MscvCrudService crudService;

    @Autowired
    public ScriptMgmtController(final MscvCrudService crudService) {
        super();
        this.crudService = crudService;
    }

    //=================================================================================================
    // methods
    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = CREATE_SCRIPT_DESCRIPTION, response = ScriptDto.class, tags = {CoreCommonConstants.SWAGGER_TAG_MGMT})
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = CREATE_SCRIPT_SUCCESS),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = CREATE_SCRIPT_BAD_REQUEST, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE, response = ErrorMessageDTO.class)
    })
    @PostMapping(CREATE_SCRIPT_URI)
    @ResponseBody
    public ScriptDto create(@RequestBody final ScriptDto dto) {
        logger.debug("create started ...");
        // TODO implement
        return null;
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = READ_SCRIPT_DESCRIPTION, response = ScriptDto.class, tags = {CoreCommonConstants.SWAGGER_TAG_MGMT})
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = READ_SCRIPT_SUCCESS),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = READ_SCRIPT_BAD_REQUEST, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE, response = ErrorMessageDTO.class)
    })
    @GetMapping(READ_SCRIPT_URI)
    @ResponseBody
    public ScriptDto read(@PathVariable(PARAMETER_MIP_ID) final Long mipId,
                          @PathVariable(PARAMETER_LAYER) final OS os,
                          @PathVariable(PARAMETER_OS) final Layer layer) {
        logger.debug("read started ...");
        // TODO implement
        return null;
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = READ_ALL_SCRIPT_DESCRIPTION, response = ScriptDto.class, tags = {CoreCommonConstants.SWAGGER_TAG_MGMT})
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = READ_ALL_SCRIPT_SUCCESS),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = READ_ALL_SCRIPT_BAD_REQUEST, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE, response = ErrorMessageDTO.class)
    })
    @GetMapping(READ_ALL_SCRIPT_URI)
    @ResponseBody
    public ScriptDto readAll(
            @RequestParam(name = PARAMETER_MIP_ID, required = false) final Long mipId,
            @RequestParam(name = PARAMETER_LAYER, required = false) final Layer layer,
            @RequestParam(name = PARAMETER_OS, required = false) final OS os,
            @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_PAGE, required = false) final Integer page,
            @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_ITEM_PER_PAGE, required = false) final Integer size,
            @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_DIRECTION, defaultValue = CoreDefaults.DEFAULT_REQUEST_PARAM_DIRECTION_VALUE) final String direction,
            @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_SORT_FIELD, defaultValue = CoreCommonConstants.COMMON_FIELD_NAME_ID) final String sortField) {
        logger.debug("readAll started ...");
        // TODO implement
        return null;
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = UPDATE_SCRIPT_DESCRIPTION, response = ScriptDto.class, tags = {CoreCommonConstants.SWAGGER_TAG_MGMT})
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = UPDATE_SCRIPT_SUCCESS),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = UPDATE_SCRIPT_BAD_REQUEST, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE, response = ErrorMessageDTO.class)
    })
    @PutMapping(UPDATE_SCRIPT_URI)
    @ResponseBody
    public ScriptDto update(@PathVariable(PARAMETER_MIP_ID) final Long mipId,
                            @PathVariable(PARAMETER_LAYER) final OS os,
                            @PathVariable(PARAMETER_OS) final Layer layer,
                            @RequestBody final ScriptDto dto) {
        logger.debug("update started ...");
        // TODO implement
        return null;
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = DELETE_SCRIPT_DESCRIPTION, tags = {CoreCommonConstants.SWAGGER_TAG_MGMT})
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = DELETE_SCRIPT_SUCCESS),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = DELETE_SCRIPT_BAD_REQUEST, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE, response = ErrorMessageDTO.class)
    })
    @DeleteMapping(DELETE_SCRIPT_URI)
    @ResponseBody
    public void delete(@PathVariable(PARAMETER_MIP_ID) final Long mipId,
                       @PathVariable(PARAMETER_LAYER) final OS os,
                       @PathVariable(PARAMETER_OS) final Layer layer) {
        logger.debug("delete started ...");
        // TODO implement
    }
}
