package eu.arrowhead.core.mscv.controller;

import java.util.Optional;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.CoreDefaults;
import eu.arrowhead.common.CoreUtilities;
import eu.arrowhead.common.Defaults;
import eu.arrowhead.common.database.entity.mscv.MipDomain;
import eu.arrowhead.common.dto.shared.ErrorMessageDTO;
import eu.arrowhead.common.dto.shared.mscv.DomainDto;
import eu.arrowhead.common.dto.shared.mscv.DomainListResponseDto;
import eu.arrowhead.core.mscv.Constants;
import eu.arrowhead.core.mscv.Validation;
import eu.arrowhead.core.mscv.service.DomainService;
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

@Api(tags = {CoreCommonConstants.SWAGGER_TAG_ALL, CoreCommonConstants.SWAGGER_TAG_MGMT, Constants.SWAGGER_TAG_DOMAIN_MGMT})
@CrossOrigin(maxAge = Defaults.CORS_MAX_AGE, allowCredentials = Defaults.CORS_ALLOW_CREDENTIALS,
        allowedHeaders = {HttpHeaders.ORIGIN, HttpHeaders.CONTENT_TYPE, HttpHeaders.ACCEPT}
)
@RestController
@RequestMapping(value = CommonConstants.MSCV_URI + CoreCommonConstants.MGMT_URI,
        consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
public class DomainMgmtController {

    private static final String DOMAIN_URI = "/domain";
    private static final String QUALIFY_DOMAIN_URI = DOMAIN_URI + PARAMETER_NAME_PATH;

    private static final String CREATE_DOMAIN_URI = DOMAIN_URI;
    private static final String CREATE_DOMAIN_DESCRIPTION = "Create new MSCV domain";
    private static final String CREATE_DOMAIN_OK = "MSCV Domain exists already";
    private static final String CREATE_DOMAIN_SUCCESS = "New MSCV domain created";
    private static final String CREATE_DOMAIN_BAD_REQUEST = "Unable to create new MSCV domain";

    private static final String READ_DOMAIN_URI = QUALIFY_DOMAIN_URI;
    private static final String READ_DOMAIN_DESCRIPTION = "Get MSCV domain by name";
    private static final String READ_DOMAIN_SUCCESS = "MSCV domain returned";
    private static final String READ_DOMAIN_NOT_FOUND = "MSCV domain not found";
    private static final String READ_DOMAIN_BAD_REQUEST = "Unable to return MSCV domain";

    private static final String READ_ALL_DOMAIN_URI = DOMAIN_URI;
    private static final String READ_ALL_DOMAIN_DESCRIPTION = "Get all MSCV domains";
    private static final String READ_ALL_DOMAIN_SUCCESS = "All MSCV domains returned";
    private static final String READ_ALL_DOMAIN_BAD_REQUEST = "Unable to return MSCV domains";

    private static final String UPDATE_DOMAIN_URI = QUALIFY_DOMAIN_URI;
    private static final String UPDATE_DOMAIN_DESCRIPTION = "Update MSCV domain";
    private static final String UPDATE_DOMAIN_SUCCESS = "MSCV domain updated";
    private static final String UPDATE_DOMAIN_NOT_FOUND = "MSCV domain not found";
    private static final String UPDATE_DOMAIN_BAD_REQUEST = "Unable to update MSCV domain";

    private static final String DELETE_DOMAIN_URI = QUALIFY_DOMAIN_URI;
    private static final String DELETE_DOMAIN_DESCRIPTION = "Delete MSCV domain";
    private static final String DELETE_DOMAIN_SUCCESS = "MSCV domain deleted";
    private static final String DELETE_DOMAIN_BAD_REQUEST = "Unable to delete MSCV domain";

    private final Logger logger = LogManager.getLogger();
    private final DomainService crudService;
    private final Validation validation;

    @Autowired
    public DomainMgmtController(final DomainService crudService) {
        this.crudService = crudService;
        validation = new Validation();
    }

    //=================================================================================================
    // methods
    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = CREATE_DOMAIN_DESCRIPTION, response = DomainDto.class)
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = CREATE_DOMAIN_OK),
            @ApiResponse(code = HttpStatus.SC_CREATED, message = CREATE_DOMAIN_SUCCESS),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = CREATE_DOMAIN_BAD_REQUEST, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE, response = ErrorMessageDTO.class)
    })
    @PostMapping(CREATE_DOMAIN_URI)
    @ResponseBody
    public ResponseEntity<DomainDto> create(@RequestBody final DomainDto dto) {
        logger.debug("create started ...");
        final String origin = createMgmtOrigin(CREATE_DOMAIN_URI);
        final MipDomain domain;

        validation.verify(dto, origin);
        domain = new MipDomain(dto.getName());

        synchronized (crudService) {
            if (crudService.exists(domain)) {
                return ResponseEntity.ok(dto);
            } else {
                final MipDomain created = crudService.create(domain);
                final DomainDto result = new DomainDto(created.getName());
                return ResponseEntity.status(HttpStatus.SC_CREATED).body(result);
            }
        }
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = READ_DOMAIN_DESCRIPTION, response = DomainDto.class)
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = READ_DOMAIN_SUCCESS),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = READ_DOMAIN_BAD_REQUEST, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE, response = ErrorMessageDTO.class)
    })
    @GetMapping(READ_DOMAIN_URI)
    @ResponseBody
    public DomainDto read(@PathVariable(PARAMETER_NAME) final String name) {
        logger.debug("read started ...");
        final String origin = createMgmtOrigin(READ_DOMAIN_URI);
        validation.verifyName(name, origin);

        final Optional<MipDomain> optionalMipDomain = crudService.find(name);
        final MipDomain domain = optionalMipDomain.orElseThrow(notFoundException("Domain", origin));

        return new DomainDto(domain.getName());
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = READ_ALL_DOMAIN_DESCRIPTION, response = DomainDto.class)
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = READ_ALL_DOMAIN_SUCCESS),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = READ_ALL_DOMAIN_BAD_REQUEST, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE, response = ErrorMessageDTO.class)
    })
    @GetMapping(READ_ALL_DOMAIN_URI)
    @ResponseBody
    public DomainListResponseDto readAll(
            @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_PAGE, required = false) final Integer page,
            @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_ITEM_PER_PAGE, required = false) final Integer size,
            @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_DIRECTION, defaultValue = CoreDefaults.DEFAULT_REQUEST_PARAM_DIRECTION_VALUE) final String direction,
            @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_SORT_FIELD, defaultValue = CoreCommonConstants.COMMON_FIELD_NAME_ID) final String sortField) {
        logger.debug("readAll started ...");
        final CoreUtilities.ValidatedPageParams pageParameters = CoreUtilities
                .validatePageParameters(page, size, direction, createMgmtOrigin(READ_ALL_DOMAIN_URI));

        final Page<MipDomain> domains = crudService.pageAll(pageParameters.createPageable(sortField));
        return new DomainListResponseDto(domains.map(x -> new DomainDto(x.getName())));
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = UPDATE_DOMAIN_DESCRIPTION, response = DomainDto.class)
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = UPDATE_DOMAIN_SUCCESS),
            @ApiResponse(code = HttpStatus.SC_NOT_FOUND, message = UPDATE_DOMAIN_NOT_FOUND),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = UPDATE_DOMAIN_BAD_REQUEST, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE, response = ErrorMessageDTO.class)
    })
    @PutMapping(UPDATE_DOMAIN_URI)
    @ResponseBody
    public DomainDto update(@PathVariable(PARAMETER_NAME) final String name, @RequestBody final DomainDto dto) {
        logger.debug("update started ...");
        final String origin = createMgmtOrigin(UPDATE_DOMAIN_URI);
        validation.verifyName(name, origin);
        validation.verify(dto, origin);

        final Optional<MipDomain> optionalMipDomain = crudService.find(name);
        final MipDomain oldDomain = optionalMipDomain.orElseThrow(notFoundException("Domain", origin));
        final MipDomain newDomain = crudService.replace(oldDomain, new MipDomain(dto.getName()));

        return new DomainDto(newDomain.getName());
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = DELETE_DOMAIN_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_NO_CONTENT, message = DELETE_DOMAIN_SUCCESS),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = DELETE_DOMAIN_BAD_REQUEST, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE, response = ErrorMessageDTO.class)
    })
    @DeleteMapping(DELETE_DOMAIN_URI)
    @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
    public void delete(@PathVariable(PARAMETER_NAME) final String name) {
        logger.debug("delete started ...");
        final String origin = createMgmtOrigin(DELETE_DOMAIN_URI);
        validation.verifyName(name, origin);
        crudService.delete(name);
    }


    private String createMgmtOrigin(final String path) {
        return CommonConstants.MSCV_URI + CoreCommonConstants.MGMT_URI + path;
    }
}
