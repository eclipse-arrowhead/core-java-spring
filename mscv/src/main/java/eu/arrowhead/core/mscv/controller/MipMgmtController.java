package eu.arrowhead.core.mscv.controller;

import java.util.Objects;
import java.util.Optional;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.CoreDefaults;
import eu.arrowhead.common.CoreUtilities;
import eu.arrowhead.common.Defaults;
import eu.arrowhead.common.database.entity.mscv.Mip;
import eu.arrowhead.common.database.entity.mscv.MipCategory;
import eu.arrowhead.common.database.entity.mscv.MipDomain;
import eu.arrowhead.common.database.entity.mscv.Standard;
import eu.arrowhead.common.dto.shared.ErrorMessageDTO;
import eu.arrowhead.common.dto.shared.mscv.MipDto;
import eu.arrowhead.common.dto.shared.mscv.MipListResponseDto;
import eu.arrowhead.core.mscv.Constants;
import eu.arrowhead.core.mscv.MscvDtoConverter;
import eu.arrowhead.core.mscv.Validation;
import eu.arrowhead.core.mscv.service.MipService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
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
import static eu.arrowhead.core.mscv.Constants.PARAMETER_MIP_ABBREVIATION;
import static eu.arrowhead.core.mscv.Constants.PARAMETER_MIP_CATEGORY;
import static eu.arrowhead.core.mscv.Constants.PARAMETER_MIP_CATEGORY_ABBREVIATION;
import static eu.arrowhead.core.mscv.Constants.PARAMETER_MIP_CATEGORY_ABBREVIATION_PATH;
import static eu.arrowhead.core.mscv.Constants.PARAMETER_MIP_CATEGORY_PATH;
import static eu.arrowhead.core.mscv.Constants.PARAMETER_MIP_DOMAIN;
import static eu.arrowhead.core.mscv.Constants.PARAMETER_MIP_EXT_ID;
import static eu.arrowhead.core.mscv.Constants.PARAMETER_MIP_EXT_ID_PATH;
import static eu.arrowhead.core.mscv.Constants.PARAMETER_MIP_STANDARD;
import static eu.arrowhead.core.mscv.Constants.PARAMETER_NAME;
import static eu.arrowhead.core.mscv.Constants.PATH_MIP_CATEGORY;
import static eu.arrowhead.core.mscv.Constants.PATH_MIP_CATEGORY_ABBREVIATION;
import static eu.arrowhead.core.mscv.MscvUtilities.notFoundException;

@Api(tags = {CoreCommonConstants.SWAGGER_TAG_ALL, CoreCommonConstants.SWAGGER_TAG_MGMT, Constants.SWAGGER_TAG_MIP_MGMT})
@CrossOrigin(maxAge = Defaults.CORS_MAX_AGE, allowCredentials = Defaults.CORS_ALLOW_CREDENTIALS,
        allowedHeaders = {HttpHeaders.ORIGIN, HttpHeaders.CONTENT_TYPE, HttpHeaders.ACCEPT}
)
@RestController
@RequestMapping(value = CommonConstants.MSCV_URI + CoreCommonConstants.MGMT_URI,
        consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
public class MipMgmtController {

    private static final String MIP_URI = "/mip";
    private static final String QUALIFY_MIP_URI = MIP_URI + PARAMETER_MIP_EXT_ID_PATH +
            PATH_MIP_CATEGORY_ABBREVIATION + PARAMETER_MIP_CATEGORY_ABBREVIATION_PATH;

    private static final String CREATE_MIP_URI = MIP_URI;
    private static final String CREATE_MIP_DESCRIPTION = "Create new MSCV mip";
    private static final String CREATE_MIP_OK = "MSCV mip exists";
    private static final String CREATE_MIP_SUCCESS = "New MSCV mip created";
    private static final String CREATE_MIP_BAD_REQUEST = "Unable to create new MSCV mip";

    private static final String READ_MIP_URI = QUALIFY_MIP_URI;
    private static final String READ_MIP_EXT_ID_URI = MIP_URI + PARAMETER_MIP_EXT_ID_PATH + PATH_MIP_CATEGORY + PARAMETER_MIP_CATEGORY_PATH;
    private static final String READ_MIP_DESCRIPTION = "Get MSCV mip through external id and category abbreviation";
    private static final String READ_MIP_EXT_ID_DESCRIPTION = "Get MSCV mip through external id and category name";
    private static final String READ_MIP_NOT_FOUND = "MSCV mip not found";
    private static final String READ_MIP_SUCCESS = "MSCV mip returned";
    private static final String READ_MIP_BAD_REQUEST = "Unable to return MSCV mip";

    private static final String READ_ALL_MIP_URI = MIP_URI;
    private static final String READ_ALL_MIP_DESCRIPTION = "Get all MSCV mips";
    private static final String READ_ALL_MIP_SUCCESS = "All MSCV mips returned";
    private static final String READ_ALL_MIP_BAD_REQUEST = "Unable to return MSCV mips";

    private static final String UPDATE_MIP_URI = QUALIFY_MIP_URI;
    private static final String UPDATE_MIP_DESCRIPTION = "Update MSCV mip through external id and category abbreviation";
    private static final String UPDATE_MIP_SUCCESS = "MSCV mip updated";
    private static final String UPDATE_MIP_NOT_FOUND = "MSCV mip not found";
    private static final String UPDATE_MIP_BAD_REQUEST = "Unable to update MSCV mip";

    private static final String DELETE_MIP_URI = QUALIFY_MIP_URI;
    private static final String DELETE_MIP_DESCRIPTION = "Delete MSCV mip through external id and category abbreviation";
    private static final String DELETE_MIP_SUCCESS = "MSCV mip deleted";
    private static final String DELETE_MIP_BAD_REQUEST = "Unable to delete MSCV mip";

    private final Logger logger = LogManager.getLogger();
    private final MipService crudService;
    private final Validation validation;

    @Autowired
    public MipMgmtController(final MipService crudService) {
        super();
        this.crudService = crudService;
        validation = new Validation();
    }

    //=================================================================================================
    // methods
    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = CREATE_MIP_DESCRIPTION, response = MipDto.class)
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = CREATE_MIP_OK),
            @ApiResponse(code = HttpStatus.SC_CREATED, message = CREATE_MIP_SUCCESS),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = CREATE_MIP_BAD_REQUEST, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE, response = ErrorMessageDTO.class)
    })
    @PostMapping(CREATE_MIP_URI)
    @ResponseBody
    public ResponseEntity<MipDto> create(@RequestBody final MipDto dto) {
        logger.debug("create started ...");
        final String origin = createMgmtOrigin(CREATE_MIP_URI);
        final Mip mip;

        validation.verify(dto, origin);
        mip = MscvDtoConverter.convert(dto);

        synchronized (crudService) {
            if (crudService.exists(mip)) {
                return ResponseEntity.ok(dto);
            } else {
                final Mip created = crudService.create(mip);
                final MipDto result = MscvDtoConverter.convert(created);
                return ResponseEntity.status(HttpStatus.SC_CREATED).body(result);
            }
        }
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = READ_MIP_DESCRIPTION, response = MipDto.class)
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = READ_MIP_SUCCESS),
            @ApiResponse(code = HttpStatus.SC_NOT_FOUND, message = READ_MIP_NOT_FOUND),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = READ_MIP_BAD_REQUEST, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE, response = ErrorMessageDTO.class)
    })
    @GetMapping(READ_MIP_URI)
    @ResponseBody
    public MipDto read(@PathVariable(PARAMETER_MIP_EXT_ID) final Integer extId,
                       @PathVariable(PARAMETER_MIP_CATEGORY_ABBREVIATION) final String categoryAbbreviation) {
        logger.debug("read started ...");
        final String origin = createMgmtOrigin(READ_MIP_URI);
        validation.verifyExtId(extId, origin);
        validation.verifyAbbreviation(categoryAbbreviation, origin);

        final Optional<Mip> optionalMip = crudService.findByExternalIdAndCategoryAbbreviation(extId, categoryAbbreviation);
        final Mip mip = optionalMip.orElseThrow(notFoundException(READ_MIP_NOT_FOUND, origin));

        return MscvDtoConverter.convert(mip);
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = READ_MIP_EXT_ID_DESCRIPTION, response = MipDto.class)
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = READ_MIP_SUCCESS),
            @ApiResponse(code = HttpStatus.SC_NOT_FOUND, message = READ_MIP_NOT_FOUND),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = READ_MIP_BAD_REQUEST, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE, response = ErrorMessageDTO.class)
    })
    @GetMapping(READ_MIP_EXT_ID_URI)
    @ResponseBody
    public MipDto readExtId(@PathVariable(PARAMETER_MIP_EXT_ID) final Integer extId, @PathVariable(PARAMETER_MIP_CATEGORY) final String categoryName) {
        logger.debug("readExtId started ...");
        final String origin = createMgmtOrigin(READ_MIP_EXT_ID_URI);
        validation.verifyExtId(extId, origin);
        validation.verifyName(categoryName, origin);

        final Optional<Mip> optionalMip = crudService.findByExternalIdAndCategory(extId, categoryName);
        final Mip mip = optionalMip.orElseThrow(notFoundException("MIP", origin));

        return MscvDtoConverter.convert(mip);
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = READ_ALL_MIP_DESCRIPTION, response = MipListResponseDto.class)
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = READ_ALL_MIP_SUCCESS),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = READ_ALL_MIP_BAD_REQUEST, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE, response = ErrorMessageDTO.class)
    })
    @GetMapping(READ_ALL_MIP_URI)
    @ResponseBody
    public MipListResponseDto readAll(
            @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_PAGE, required = false) final Integer page,
            @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_ITEM_PER_PAGE, required = false) final Integer size,
            @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_DIRECTION, defaultValue = CoreDefaults.DEFAULT_REQUEST_PARAM_DIRECTION_VALUE) final String direction,
            @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_SORT_FIELD, defaultValue = CoreCommonConstants.COMMON_FIELD_NAME_ID) final String sortField,
            @ApiParam(value = "Filter mode. Match all or any filter.")
            @RequestParam(name = "mode", defaultValue = "ALL") final ExampleMatcher.MatchMode mode,
            @ApiParam(value = "Filter for name. Partial match ignoring case.")
            @RequestParam(name = PARAMETER_NAME, required = false) final String name,
            @ApiParam(value = "Filter for category name. Partial match ignoring case.")
            @RequestParam(name = PARAMETER_MIP_CATEGORY, required = false) final String categoryName,
            @ApiParam(value = "Filter for category abbreviation. Partial match ignoring case.")
            @RequestParam(name = PARAMETER_MIP_ABBREVIATION, required = false) final String abbreviation,
            @ApiParam(value = "Filter for domain name. Partial match ignoring case.")
            @RequestParam(name = PARAMETER_MIP_DOMAIN, required = false) final String domainName,
            @ApiParam(value = "Filter for standard. Partial match ignoring case.")
            @RequestParam(name = PARAMETER_MIP_STANDARD, required = false) final String standardName,
            @ApiParam(value = "Filter for standard identification. Partial match ignoring case.")
            @RequestParam(name = PARAMETER_IDENTIFICATION, required = false) final String identification) {
        logger.debug("readAll started ...");
        final CoreUtilities.ValidatedPageParams pageParameters = CoreUtilities
                .validatePageParameters(page, size, direction, createMgmtOrigin(READ_ALL_MIP_URI));

        final Mip probe = new Mip();

        if (Objects.nonNull(name)) { probe.setName(name); }
        if (Objects.nonNull(domainName)) { probe.setDomain(new MipDomain(domainName)); }
        if (Objects.nonNull(standardName) || Objects.nonNull(identification)) {
            probe.setStandard(new Standard(identification, standardName, null));
        }
        if (Objects.nonNull(categoryName) || Objects.nonNull(abbreviation)) {
            probe.setCategory(new MipCategory(categoryName, abbreviation));
        }

        final Example<Mip> example = Example.of(probe, validation.exampleMatcher(mode));

        final Page<Mip> mips = crudService.pageByExample(example, pageParameters.createPageable(sortField));
        return new MipListResponseDto(mips.map(MscvDtoConverter::convert));
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = UPDATE_MIP_DESCRIPTION, response = MipDto.class)
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = UPDATE_MIP_SUCCESS),
            @ApiResponse(code = HttpStatus.SC_NOT_FOUND, message = UPDATE_MIP_NOT_FOUND),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = UPDATE_MIP_BAD_REQUEST, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE, response = ErrorMessageDTO.class)
    })
    @PutMapping(UPDATE_MIP_URI)
    @ResponseBody
    public MipDto update(@PathVariable(PARAMETER_MIP_EXT_ID) final Integer extId,
                         @PathVariable(PARAMETER_MIP_CATEGORY_ABBREVIATION) final String categoryAbbreviation,
                         @RequestBody final MipDto dto) {
        logger.debug("update started ...");
        final String origin = createMgmtOrigin(UPDATE_MIP_URI);
        validation.verifyExtId(extId, origin);
        validation.verifyAbbreviation(categoryAbbreviation, origin);
        validation.verify(dto, origin);

        final Optional<Mip> optionalMip = crudService.findByExternalIdAndCategoryAbbreviation(extId, categoryAbbreviation);
        final Mip oldMip = optionalMip.orElseThrow(notFoundException("MIP", origin));
        final Mip newMip = crudService.replace(oldMip, MscvDtoConverter.convert(dto));

        return MscvDtoConverter.convert(newMip);
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = DELETE_MIP_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_NO_CONTENT, message = DELETE_MIP_SUCCESS),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = DELETE_MIP_BAD_REQUEST, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE, response = ErrorMessageDTO.class)
    })
    @DeleteMapping(DELETE_MIP_URI)
    @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
    public void delete(@PathVariable(PARAMETER_MIP_EXT_ID) final Integer extId,
                       @PathVariable(PARAMETER_MIP_CATEGORY_ABBREVIATION) final String categoryAbbreviation) {
        logger.debug("delete started ...");
        final String origin = createMgmtOrigin(DELETE_MIP_URI);
        validation.verifyExtId(extId, origin);
        validation.verifyAbbreviation(categoryAbbreviation, origin);
        crudService.delete(extId, categoryAbbreviation);
    }

    private String createMgmtOrigin(final String path) {
        return CommonConstants.MSCV_URI + CoreCommonConstants.MGMT_URI + path;
    }
}
