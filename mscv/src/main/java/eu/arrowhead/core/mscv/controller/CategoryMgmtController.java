package eu.arrowhead.core.mscv.controller;

import java.util.Optional;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.CoreDefaults;
import eu.arrowhead.common.CoreUtilities;
import eu.arrowhead.common.Defaults;
import eu.arrowhead.common.database.entity.mscv.MipCategory;
import eu.arrowhead.common.dto.shared.ErrorMessageDTO;
import eu.arrowhead.common.dto.shared.mscv.CategoryDto;
import eu.arrowhead.common.dto.shared.mscv.CategoryListResponseDto;
import eu.arrowhead.core.mscv.Constants;
import eu.arrowhead.core.mscv.MscvDtoConverter;
import eu.arrowhead.core.mscv.Validation;
import eu.arrowhead.core.mscv.service.CategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static eu.arrowhead.core.mscv.Constants.PARAMETER_NAME;
import static eu.arrowhead.core.mscv.Constants.PARAMETER_NAME_PATH;
import static eu.arrowhead.core.mscv.MscvUtilities.notFoundException;

@Api(tags = {CoreCommonConstants.SWAGGER_TAG_ALL, CoreCommonConstants.SWAGGER_TAG_MGMT, Constants.SWAGGER_TAG_CATEGORY_MGMT})
@CrossOrigin(maxAge = Defaults.CORS_MAX_AGE, allowCredentials = Defaults.CORS_ALLOW_CREDENTIALS,
        allowedHeaders = {HttpHeaders.ORIGIN, HttpHeaders.CONTENT_TYPE, HttpHeaders.ACCEPT}
)
@RestController
@RequestMapping(value = CommonConstants.MSCV_URI + CoreCommonConstants.MGMT_URI,
        consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
public class CategoryMgmtController {

    private static final String CATEGORY_URI = "/category";
    private static final String QUALIFY_CATEGORY_URI = CATEGORY_URI + PARAMETER_NAME_PATH;

    private static final String CREATE_CATEGORY_URI = CATEGORY_URI;
    private static final String CREATE_CATEGORY_DESCRIPTION = "Create new MSCV category";
    private static final String CREATE_CATEGORY_OK = "Category exists already";
    private static final String CREATE_CATEGORY_SUCCESS = "New MSCV category created";
    private static final String CREATE_CATEGORY_BAD_REQUEST = "Unable to create new MSCV category";

    private static final String READ_CATEGORY_URI = QUALIFY_CATEGORY_URI;
    private static final String READ_CATEGORY_DESCRIPTION = "Get MSCV category by name";
    private static final String READ_CATEGORY_SUCCESS = "MSCV category returned";
    private static final String READ_CATEGORY_NOT_FOUND = "MSCV category not found";
    private static final String READ_CATEGORY_BAD_REQUEST = "Unable to return MSCV category";

    private static final String READ_ALL_CATEGORY_URI = CATEGORY_URI;
    private static final String READ_ALL_CATEGORY_DESCRIPTION = "Get all MSCV categories";
    private static final String READ_ALL_CATEGORY_SUCCESS = "All MSCV categories returned";
    private static final String READ_ALL_CATEGORY_BAD_REQUEST = "Unable to return MSCV categories";

    private static final String UPDATE_CATEGORY_URI = QUALIFY_CATEGORY_URI;
    private static final String UPDATE_CATEGORY_DESCRIPTION = "Update MSCV category";
    private static final String UPDATE_CATEGORY_SUCCESS = "MSCV category updated";
    private static final String UPDATE_CATEGORY_NOT_FOUND = "MSCV category not found";
    private static final String UPDATE_CATEGORY_BAD_REQUEST = "Unable to update MSCV category";

    private static final String DELETE_CATEGORY_URI = QUALIFY_CATEGORY_URI;
    private static final String DELETE_CATEGORY_DESCRIPTION = "Delete MSCV category";
    private static final String DELETE_CATEGORY_SUCCESS = "MSCV category deleted";
    private static final String DELETE_CATEGORY_BAD_REQUEST = "Unable to delete MSCV category";

    private final Logger logger = LogManager.getLogger();
    private final CategoryService crudService;
    private final Validation validation;

    @Autowired
    public CategoryMgmtController(final CategoryService crudService) {
        this.crudService = crudService;
        validation = new Validation();
    }

    //=================================================================================================
    // methods
    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = CREATE_CATEGORY_DESCRIPTION, response = CategoryDto.class)
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = CREATE_CATEGORY_OK),
            @ApiResponse(code = HttpStatus.SC_CREATED, message = CREATE_CATEGORY_SUCCESS),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = CREATE_CATEGORY_BAD_REQUEST, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE, response = ErrorMessageDTO.class)
    })
    @PostMapping(CREATE_CATEGORY_URI)
    @ResponseBody
    public ResponseEntity<CategoryDto> create(@RequestBody final CategoryDto dto) {
        logger.debug("create started ...");
        final String origin = createMgmtOrigin(CREATE_CATEGORY_URI);
        final MipCategory category;

        validation.verify(dto, origin);
        category = MscvDtoConverter.convert(dto);

        synchronized (crudService) {
            if (crudService.exists(category)) {
                return ResponseEntity.ok(dto);
            } else {
                final MipCategory created = crudService.create(category);
                final CategoryDto result = MscvDtoConverter.convert(created);
                return ResponseEntity.status(HttpStatus.SC_CREATED).body(result);
            }
        }
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = READ_CATEGORY_DESCRIPTION, response = CategoryDto.class)
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = READ_CATEGORY_SUCCESS),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = READ_CATEGORY_BAD_REQUEST, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_NOT_FOUND, message = READ_CATEGORY_NOT_FOUND, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE, response = ErrorMessageDTO.class)
    })
    @GetMapping(READ_CATEGORY_URI)
    @ResponseBody
    public CategoryDto read(@PathVariable(PARAMETER_NAME) final String name) {
        logger.debug("read started ...");
        final String origin = createMgmtOrigin(READ_CATEGORY_URI);
        validation.verifyName(name, origin);

        final Optional<MipCategory> optionalMipCategory = crudService.find(name);
        final MipCategory category = optionalMipCategory.orElseThrow(notFoundException("Category", origin));

        return MscvDtoConverter.convert(category);
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = READ_ALL_CATEGORY_DESCRIPTION, response = CategoryListResponseDto.class)
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = READ_ALL_CATEGORY_SUCCESS),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = READ_ALL_CATEGORY_BAD_REQUEST, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE, response = ErrorMessageDTO.class)
    })
    @GetMapping(READ_ALL_CATEGORY_URI)
    @ResponseBody
    public CategoryListResponseDto readAll(
            @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_PAGE, required = false) final Integer page,
            @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_ITEM_PER_PAGE, required = false) final Integer size,
            @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_DIRECTION, defaultValue = CoreDefaults.DEFAULT_REQUEST_PARAM_DIRECTION_VALUE) final String direction,
            @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_SORT_FIELD, defaultValue = CoreCommonConstants.COMMON_FIELD_NAME_ID) final String sortField) {
        logger.debug("readAll started ...");
        final CoreUtilities.ValidatedPageParams pageParameters = CoreUtilities
                .validatePageParameters(page, size, direction, createMgmtOrigin(READ_ALL_CATEGORY_URI));

        final Page<MipCategory> categories = crudService.pageAll(pageParameters.createPageable(sortField));
        return new CategoryListResponseDto(categories.map(x -> new CategoryDto(x.getName(), x.getAbbreviation())));
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = UPDATE_CATEGORY_DESCRIPTION, response = CategoryDto.class)
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = UPDATE_CATEGORY_SUCCESS),
            @ApiResponse(code = HttpStatus.SC_NOT_FOUND, message = UPDATE_CATEGORY_NOT_FOUND),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = UPDATE_CATEGORY_BAD_REQUEST, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE, response = ErrorMessageDTO.class)
    })
    @PutMapping(UPDATE_CATEGORY_URI)
    @ResponseBody
    public CategoryDto update(@PathVariable(PARAMETER_NAME) final String name, @RequestBody final CategoryDto dto) {
        logger.debug("update started ...");
        final String origin = createMgmtOrigin(UPDATE_CATEGORY_URI);
        validation.verifyName(name, origin);
        validation.verify(dto, origin);

        final Optional<MipCategory> optionalMipCategory = crudService.find(name);
        final MipCategory oldCategory = optionalMipCategory.orElseThrow(notFoundException("Category", origin));
        final MipCategory newCategory = crudService.replace(oldCategory, MscvDtoConverter.convert(dto));

        return MscvDtoConverter.convert(newCategory);
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = DELETE_CATEGORY_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_NO_CONTENT, message = DELETE_CATEGORY_SUCCESS),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = DELETE_CATEGORY_BAD_REQUEST, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE, response = ErrorMessageDTO.class)
    })
    @DeleteMapping(DELETE_CATEGORY_URI)
    @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
    public void delete(@PathVariable(PARAMETER_NAME) final String name) {
        logger.debug("delete started ...");
        final String origin = createMgmtOrigin(DELETE_CATEGORY_URI);
        validation.verifyName(name, origin);
        crudService.delete(name);
    }

    private String createMgmtOrigin(final String path) {
        return CommonConstants.MSCV_URI + CoreCommonConstants.MGMT_URI + path;
    }

}
