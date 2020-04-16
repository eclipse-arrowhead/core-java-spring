package eu.arrowhead.core.mscv.controller;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.CoreDefaults;
import eu.arrowhead.common.Defaults;
import eu.arrowhead.common.dto.shared.mscv.CategoryDto;
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
public class CategoryMgmtController {

    private static final String CATEGORY_URI = "/category";
    private static final String QUALIFY_CATEGORY_URI = CATEGORY_URI + PARAMETER_NAME_PATH;

    private static final String CREATE_CATEGORY_URI = CATEGORY_URI;
    private static final String CREATE_CATEGORY_DESCRIPTION = "Create new MSCV category";
    private static final String CREATE_CATEGORY_SUCCESS = "New MSCV category created";
    private static final String CREATE_CATEGORY_BAD_REQUEST = "Unable to create new MSCV category";

    private static final String READ_CATEGORY_URI = QUALIFY_CATEGORY_URI;
    private static final String READ_CATEGORY_DESCRIPTION = "Get MSCV category by name";
    private static final String READ_CATEGORY_SUCCESS = "MSCV category returned";
    private static final String READ_CATEGORY_BAD_REQUEST = "Unable to return MSCV category";

    private static final String READ_ALL_CATEGORY_URI = CATEGORY_URI;
    private static final String READ_ALL_CATEGORY_DESCRIPTION = "Get all MSCV categories";
    private static final String READ_ALL_CATEGORY_SUCCESS = "All MSCV categories returned";
    private static final String READ_ALL_CATEGORY_BAD_REQUEST = "Unable to return MSCV categories";

    private static final String UPDATE_CATEGORY_URI = QUALIFY_CATEGORY_URI;
    private static final String UPDATE_CATEGORY_DESCRIPTION = "Update MSCV category";
    private static final String UPDATE_CATEGORY_SUCCESS = "MSCV category updated";
    private static final String UPDATE_CATEGORY_BAD_REQUEST = "Unable to update MSCV category";

    private static final String DELETE_CATEGORY_URI = QUALIFY_CATEGORY_URI;
    private static final String DELETE_CATEGORY_DESCRIPTION = "Delete MSCV category";
    private static final String DELETE_CATEGORY_SUCCESS = "MSCV category deleted";
    private static final String DELETE_CATEGORY_BAD_REQUEST = "Unable to delete MSCV category";

    private final Logger logger = LogManager.getLogger();
    private final MscvCrudService crudService;

    @Autowired
    public CategoryMgmtController(final MscvCrudService crudService) {
        super();
        this.crudService = crudService;
    }

    //=================================================================================================
    // methods
    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = CREATE_CATEGORY_DESCRIPTION, response = CategoryDto.class, tags = {CoreCommonConstants.SWAGGER_TAG_MGMT})
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = CREATE_CATEGORY_SUCCESS),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = CREATE_CATEGORY_BAD_REQUEST),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @PostMapping(CREATE_CATEGORY_URI)
    @ResponseBody
    public CategoryDto create(@RequestBody final CategoryDto dto) {
        logger.debug("create started ...");
        // TODO implement
        return null;
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = READ_CATEGORY_DESCRIPTION, response = CategoryDto.class, tags = {CoreCommonConstants.SWAGGER_TAG_MGMT})
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = READ_CATEGORY_SUCCESS),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = READ_CATEGORY_BAD_REQUEST),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @GetMapping(READ_CATEGORY_URI)
    @ResponseBody
    public CategoryDto read(@PathVariable(PARAMETER_NAME) final String name) {
        logger.debug("read started ...");
        // TODO implement
        return null;
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = READ_ALL_CATEGORY_DESCRIPTION, response = CategoryDto.class, tags = {CoreCommonConstants.SWAGGER_TAG_MGMT})
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = READ_ALL_CATEGORY_SUCCESS),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = READ_ALL_CATEGORY_BAD_REQUEST),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @GetMapping(READ_ALL_CATEGORY_URI)
    @ResponseBody
    public CategoryDto readAll(
            @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_PAGE, required = false) final Integer page,
            @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_ITEM_PER_PAGE, required = false) final Integer size,
            @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_DIRECTION, defaultValue = CoreDefaults.DEFAULT_REQUEST_PARAM_DIRECTION_VALUE) final String direction,
            @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_SORT_FIELD, defaultValue = CoreCommonConstants.COMMON_FIELD_NAME_ID) final String sortField) {
        logger.debug("readAll started ...");
        // TODO implement
        return null;
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = UPDATE_CATEGORY_DESCRIPTION, response = CategoryDto.class, tags = {CoreCommonConstants.SWAGGER_TAG_MGMT})
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = UPDATE_CATEGORY_SUCCESS),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = UPDATE_CATEGORY_BAD_REQUEST),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @PutMapping(UPDATE_CATEGORY_URI)
    @ResponseBody
    public CategoryDto update(@PathVariable(PARAMETER_NAME) final String name, @RequestBody final CategoryDto dto) {
        logger.debug("update started ...");
        // TODO implement
        return null;
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = DELETE_CATEGORY_DESCRIPTION, response = CategoryDto.class, tags = {CoreCommonConstants.SWAGGER_TAG_MGMT})
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = DELETE_CATEGORY_SUCCESS),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = DELETE_CATEGORY_BAD_REQUEST),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @DeleteMapping(DELETE_CATEGORY_URI)
    @ResponseBody
    public void delete(@PathVariable(PARAMETER_NAME) final String name) {
        logger.debug("delete started ...");
        // TODO implement
    }
}
