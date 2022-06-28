package eu.arrowhead.core.mscv.controller;

import java.util.Optional;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.CoreDefaults;
import eu.arrowhead.common.CoreUtilities;
import eu.arrowhead.common.Defaults;
import eu.arrowhead.common.database.entity.mscv.Standard;
import eu.arrowhead.common.dto.shared.ErrorMessageDTO;
import eu.arrowhead.common.dto.shared.mscv.StandardDto;
import eu.arrowhead.common.dto.shared.mscv.StandardListResponseDto;
import eu.arrowhead.core.mscv.Constants;
import eu.arrowhead.core.mscv.MscvDtoConverter;
import eu.arrowhead.core.mscv.Validation;
import eu.arrowhead.core.mscv.service.StandardService;
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

import static eu.arrowhead.core.mscv.Constants.PARAMETER_IDENTIFICATION;
import static eu.arrowhead.core.mscv.Constants.PARAMETER_IDENTIFICATION_PATH;
import static eu.arrowhead.core.mscv.MscvUtilities.notFoundException;

@Api(tags = {CoreCommonConstants.SWAGGER_TAG_ALL, CoreCommonConstants.SWAGGER_TAG_MGMT, Constants.SWAGGER_TAG_STANDARD_MGMT})
@CrossOrigin(maxAge = Defaults.CORS_MAX_AGE, allowCredentials = Defaults.CORS_ALLOW_CREDENTIALS,
        allowedHeaders = {HttpHeaders.ORIGIN, HttpHeaders.CONTENT_TYPE, HttpHeaders.ACCEPT}
)
@RestController
@RequestMapping(value = CommonConstants.MSCV_URI + CoreCommonConstants.MGMT_URI,
        consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
public class StandardMgmtController {

    private static final String STANDARD_URI = "/standard";
    private static final String QUALIFY_STANDARD_URI = STANDARD_URI + PARAMETER_IDENTIFICATION_PATH;

    private static final String CREATE_STANDARD_URI = STANDARD_URI;
    private static final String CREATE_STANDARD_DESCRIPTION = "Create new MSCV standard";
    private static final String CREATE_STANDARD_OK = "Standard exists already";
    private static final String CREATE_STANDARD_SUCCESS = "New MSCV standard created";
    private static final String CREATE_STANDARD_BAD_REQUEST = "Unable to create new MSCV standard";

    private static final String READ_STANDARD_URI = QUALIFY_STANDARD_URI;
    private static final String READ_STANDARD_DESCRIPTION = "Get MSCV standard by identification";
    private static final String READ_STANDARD_SUCCESS = "MSCV standard returned";
    private static final String READ_STANDARD_NOT_FOUND = "MSCV standard not found";
    private static final String READ_STANDARD_BAD_REQUEST = "Unable to return MSCV standard";

    private static final String READ_ALL_STANDARD_URI = STANDARD_URI;
    private static final String READ_ALL_STANDARD_DESCRIPTION = "Get all MSCV standards";
    private static final String READ_ALL_STANDARD_SUCCESS = "All MSCV categories returned";
    private static final String READ_ALL_STANDARD_BAD_REQUEST = "Unable to return MSCV categories";

    private static final String UPDATE_STANDARD_URI = QUALIFY_STANDARD_URI;
    private static final String UPDATE_STANDARD_DESCRIPTION = "Update MSCV standard";
    private static final String UPDATE_STANDARD_SUCCESS = "MSCV standard updated";
    private static final String UPDATE_STANDARD_NOT_FOUND = "MSCV standard not found";
    private static final String UPDATE_STANDARD_BAD_REQUEST = "Unable to update MSCV standard";

    private static final String DELETE_STANDARD_URI = QUALIFY_STANDARD_URI;
    private static final String DELETE_STANDARD_DESCRIPTION = "Delete MSCV standard";
    private static final String DELETE_STANDARD_SUCCESS = "MSCV standard deleted";
    private static final String DELETE_STANDARD_BAD_REQUEST = "Unable to delete MSCV standard";

    private final Logger logger = LogManager.getLogger();
    private final StandardService crudService;
    private final Validation validation;

    @Autowired
    public StandardMgmtController(final StandardService crudService) {
        this.crudService = crudService;
        validation = new Validation();
    }

    //=================================================================================================
    // methods
    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = CREATE_STANDARD_DESCRIPTION, response = StandardDto.class)
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = CREATE_STANDARD_OK),
            @ApiResponse(code = HttpStatus.SC_CREATED, message = CREATE_STANDARD_SUCCESS),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = CREATE_STANDARD_BAD_REQUEST, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE, response = ErrorMessageDTO.class)
    })
    @PostMapping(CREATE_STANDARD_URI)
    @ResponseBody
    public ResponseEntity<StandardDto> create(@RequestBody final StandardDto dto) {
        logger.debug("create started ...");
        final String origin = createMgmtOrigin(CREATE_STANDARD_URI);
        final Standard standard;

        validation.verify(dto, origin);
        standard = MscvDtoConverter.convert(dto);

        if (crudService.exists(standard)) {
            return ResponseEntity.ok(dto);
        } else {
            final Standard created = crudService.create(standard);
            final StandardDto result = MscvDtoConverter.convert(created);
            return ResponseEntity.status(HttpStatus.SC_CREATED).body(result);
        }
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = READ_STANDARD_DESCRIPTION, response = StandardDto.class)
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = READ_STANDARD_SUCCESS),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = READ_STANDARD_BAD_REQUEST, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_NOT_FOUND, message = READ_STANDARD_NOT_FOUND, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE, response = ErrorMessageDTO.class)
    })
    @GetMapping(READ_STANDARD_URI)
    @ResponseBody
    public StandardDto read(@PathVariable(PARAMETER_IDENTIFICATION) final String identification) {
        logger.debug("read started ...");
        final String origin = createMgmtOrigin(READ_STANDARD_URI);
        validation.verifyIdentification(identification, origin);

        final Optional<Standard> optionalStandard = crudService.findByIdentification(identification);
        final Standard standard = optionalStandard.orElseThrow(notFoundException("Standard", origin));

        return MscvDtoConverter.convert(standard);
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = READ_ALL_STANDARD_DESCRIPTION, response = StandardListResponseDto.class)
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = READ_ALL_STANDARD_SUCCESS),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = READ_ALL_STANDARD_BAD_REQUEST, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE, response = ErrorMessageDTO.class)
    })
    @GetMapping(READ_ALL_STANDARD_URI)
    @ResponseBody
    public StandardListResponseDto readAll(
            @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_PAGE, required = false) final Integer page,
            @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_ITEM_PER_PAGE, required = false) final Integer size,
            @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_DIRECTION, defaultValue = CoreDefaults.DEFAULT_REQUEST_PARAM_DIRECTION_VALUE) final String direction,
            @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_SORT_FIELD, defaultValue = CoreCommonConstants.COMMON_FIELD_NAME_ID) final String sortField) {
        logger.debug("readAll started ...");
        final CoreUtilities.ValidatedPageParams pageParameters = CoreUtilities
                .validatePageParameters(page, size, direction, createMgmtOrigin(READ_ALL_STANDARD_URI));

        final Page<Standard> categories = crudService.pageAll(pageParameters.createPageable(sortField));
        return new StandardListResponseDto(categories.map(MscvDtoConverter::convert));
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = UPDATE_STANDARD_DESCRIPTION, response = StandardDto.class)
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = UPDATE_STANDARD_SUCCESS),
            @ApiResponse(code = HttpStatus.SC_NOT_FOUND, message = UPDATE_STANDARD_NOT_FOUND),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = UPDATE_STANDARD_BAD_REQUEST, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE, response = ErrorMessageDTO.class)
    })
    @PutMapping(UPDATE_STANDARD_URI)
    @ResponseBody
    public StandardDto update(@PathVariable(PARAMETER_IDENTIFICATION) final String identification, @RequestBody final StandardDto dto) {
        logger.debug("update started ...");
        final String origin = createMgmtOrigin(UPDATE_STANDARD_URI);
        validation.verifyIdentification(identification, origin);
        validation.verify(dto, origin);

        final Optional<Standard> optionalStandard = crudService.findByIdentification(identification);
        final Standard oldStandard = optionalStandard.orElseThrow(notFoundException("Standard", origin));
        final Standard newStandard = crudService.replace(oldStandard, MscvDtoConverter.convert(dto));

        return MscvDtoConverter.convert(newStandard);
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = DELETE_STANDARD_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_NO_CONTENT, message = DELETE_STANDARD_SUCCESS),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = DELETE_STANDARD_BAD_REQUEST, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE, response = ErrorMessageDTO.class)
    })
    @DeleteMapping(DELETE_STANDARD_URI)
    @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
    public void delete(@PathVariable(PARAMETER_IDENTIFICATION) final String identification) {
        logger.debug("delete started ...");
        final String origin = createMgmtOrigin(DELETE_STANDARD_URI);
        validation.verifyIdentification(identification, origin);
        crudService.delete(identification);
    }

    private String createMgmtOrigin(final String path) {
        return CommonConstants.MSCV_URI + CoreCommonConstants.MGMT_URI + path;
    }

}
