package eu.arrowhead.core.mscv.controller;

import java.util.Optional;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.CoreDefaults;
import eu.arrowhead.common.CoreUtilities;
import eu.arrowhead.common.Defaults;
import eu.arrowhead.common.database.entity.mscv.SshTarget;
import eu.arrowhead.common.database.entity.mscv.Target;
import eu.arrowhead.common.dto.shared.ErrorMessageDTO;
import eu.arrowhead.common.dto.shared.mscv.OS;
import eu.arrowhead.common.dto.shared.mscv.SshTargetDto;
import eu.arrowhead.common.dto.shared.mscv.TargetDto;
import eu.arrowhead.common.dto.shared.mscv.TargetListResponseDto;
import eu.arrowhead.common.dto.shared.mscv.TargetLoginRequest;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.core.mscv.MscvDtoConverter;
import eu.arrowhead.core.mscv.MscvUtilities;
import eu.arrowhead.core.mscv.Validation;
import eu.arrowhead.core.mscv.service.MscvException;
import eu.arrowhead.core.mscv.service.TargetService;
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

import static eu.arrowhead.common.CommonConstants.OP_MSCV_LOGIN_URI;
import static eu.arrowhead.core.mscv.Constants.PARAMETER_ADDRESS;
import static eu.arrowhead.core.mscv.Constants.PARAMETER_ADDRESS_PATH;
import static eu.arrowhead.core.mscv.Constants.PARAMETER_NAME;
import static eu.arrowhead.core.mscv.Constants.PARAMETER_OS;
import static eu.arrowhead.core.mscv.Constants.PARAMETER_PORT;
import static eu.arrowhead.core.mscv.Constants.PARAMETER_PORT_PATH;
import static eu.arrowhead.core.mscv.Constants.PATH_ADDRESS;
import static eu.arrowhead.core.mscv.Constants.PATH_PORT;
import static eu.arrowhead.core.mscv.Constants.SWAGGER_TAG_TARGET_MGMT;
import static eu.arrowhead.core.mscv.MscvUtilities.notFoundException;
import static eu.arrowhead.core.mscv.Validation.LOGIN_TARGET_NOT_FOUND;

@Api(tags = {CoreCommonConstants.SWAGGER_TAG_ALL, CoreCommonConstants.SWAGGER_TAG_MGMT, SWAGGER_TAG_TARGET_MGMT})
@CrossOrigin(maxAge = Defaults.CORS_MAX_AGE, allowCredentials = Defaults.CORS_ALLOW_CREDENTIALS,
        allowedHeaders = {HttpHeaders.ORIGIN, HttpHeaders.CONTENT_TYPE, HttpHeaders.ACCEPT}
)
@RestController
@RequestMapping(value = CommonConstants.MSCV_URI + CoreCommonConstants.MGMT_URI,
        consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
public class TargetMgmtController {

    private static final String TARGET_URI = "/target";
    private static final String QUALIFY_TARGET_URI = TARGET_URI +
            PATH_ADDRESS + PARAMETER_ADDRESS_PATH +
            PATH_PORT + PARAMETER_PORT_PATH;

    private static final String CREATE_TARGET_URI = TARGET_URI;
    private static final String CREATE_TARGET_DESCRIPTION = "Create new MSCV target";
    private static final String CREATE_TARGET_OK = "MSCV target exists already";
    private static final String CREATE_TARGET_SUCCESS = "New MSCV target created";
    private static final String CREATE_TARGET_BAD_REQUEST = "Unable to create new MSCV target";

    private static final String READ_TARGET_URI = QUALIFY_TARGET_URI;
    private static final String READ_TARGET_DESCRIPTION = "Get MSCV target by address and port";
    private static final String READ_TARGET_SUCCESS = "MSCV target returned";
    private static final String READ_TARGET_BAD_REQUEST = "Unable to return MSCV target";
    private static final String READ_TARGET_NOT_FOUND = "No such MSCV target exists";

    private static final String READ_ALL_TARGET_URI = TARGET_URI;
    private static final String READ_ALL_TARGET_DESCRIPTION = "Search for MSCV targets";
    private static final String READ_ALL_TARGET_SUCCESS = "All MSCV targets returned";
    private static final String READ_ALL_TARGET_BAD_REQUEST = "Unable to return MSCV targets";

    private static final String UPDATE_TARGET_URI = QUALIFY_TARGET_URI;
    private static final String UPDATE_TARGET_DESCRIPTION = "Update MSCV target overriding all values";
    private static final String UPDATE_TARGET_SUCCESS = "MSCV target updated";
    private static final String UPDATE_TARGET_BAD_REQUEST = "Unable to update MSCV target";

    private static final String DELETE_TARGET_URI = QUALIFY_TARGET_URI;
    private static final String DELETE_TARGET_DESCRIPTION = "Delete MSCV target";
    private static final String DELETE_TARGET_SUCCESS = "MSCV target deleted";
    private static final String DELETE_TARGET_BAD_REQUEST = "Unable to delete MSCV target";

    private static final String LOGIN_TARGET_URI = TARGET_URI + OP_MSCV_LOGIN_URI;
    private static final String LOGIN_TARGET_DESCRIPTION = "Instruct MSCV to remote login to a target with a username and password";
    private static final String LOGIN_TARGET_SUCCESS = "MSCV target login success";
    private static final String LOGIN_TARGET_BAD_REQUEST = "Unable to login to MSCV target";

    private static final String VERIFY_LOGIN_TARGET_URI = TARGET_URI + "/verify";
    private static final String VERIFY_LOGIN_TARGET_DESCRIPTION = "Instruct MSCV to remote login to a target with the SSH key";

    private final Logger logger = LogManager.getLogger();
    private final Validation validation = new Validation();

    private final TargetService targetService;

    @Autowired
    public TargetMgmtController(final TargetService targetService) {
        super();
        this.targetService = targetService;
    }

    //=================================================================================================
    // methods
    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = CREATE_TARGET_DESCRIPTION, response = SshTargetDto.class)
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = CREATE_TARGET_OK),
            @ApiResponse(code = HttpStatus.SC_CREATED, message = CREATE_TARGET_SUCCESS),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = CREATE_TARGET_BAD_REQUEST, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE, response = ErrorMessageDTO.class)
    })
    @PostMapping(CREATE_TARGET_URI)
    @ResponseBody
    public ResponseEntity<SshTargetDto> create(@RequestBody final SshTargetDto dto) {
        logger.debug("create started ...");
        validation.verify(dto, createOrigin(CREATE_TARGET_URI));

        final SshTarget sshTarget = MscvDtoConverter.convert(dto);

        if (targetService.exists(sshTarget)) {
            return ResponseEntity.ok(dto);
        } else {
            final SshTarget created = targetService.create(sshTarget);
            final SshTargetDto result = MscvDtoConverter.convert(created);
            return ResponseEntity.status(HttpStatus.SC_CREATED).body(result);
        }
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = READ_TARGET_DESCRIPTION, response = SshTargetDto.class)
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = READ_TARGET_SUCCESS),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = READ_TARGET_BAD_REQUEST, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_NOT_FOUND, message = READ_TARGET_NOT_FOUND, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE, response = ErrorMessageDTO.class)
    })
    @GetMapping(READ_TARGET_URI)
    @ResponseBody
    public SshTargetDto read(@PathVariable(PARAMETER_ADDRESS) final String address,
                             @PathVariable(PARAMETER_PORT) final Integer port) {
        logger.debug("read started ...");
        final String origin = createOrigin(READ_TARGET_URI);
        validation.verifyAddress(address, origin);
        validation.verifyPort(port, origin);

        final Optional<SshTarget> optionalSshTarget = targetService.find(address, port);
        final SshTarget sshTarget = optionalSshTarget.orElseThrow(notFoundException(READ_TARGET_NOT_FOUND, origin));

        return new SshTargetDto(sshTarget.getName(), sshTarget.getOs(), sshTarget.getAddress(), sshTarget.getPort());
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = READ_ALL_TARGET_DESCRIPTION, response = TargetListResponseDto.class)
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = READ_ALL_TARGET_SUCCESS),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = READ_ALL_TARGET_BAD_REQUEST, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE, response = ErrorMessageDTO.class)
    })
    @GetMapping(READ_ALL_TARGET_URI)
    @ResponseBody
    public TargetListResponseDto readAll(
            @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_PAGE, required = false) final Integer page,
            @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_ITEM_PER_PAGE, required = false) final Integer size,
            @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_DIRECTION, defaultValue = CoreDefaults.DEFAULT_REQUEST_PARAM_DIRECTION_VALUE) final String direction,
            @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_SORT_FIELD, defaultValue = CoreCommonConstants.COMMON_FIELD_NAME_ID) final String sortField,
            @ApiParam(value = "Filter mode. Match all or any filter.")
            @RequestParam(name = "mode", defaultValue = "ALL") final ExampleMatcher.MatchMode mode,
            @ApiParam(value = "Filter for name. Partial match ignoring case.")
            @RequestParam(name = PARAMETER_NAME, required = false) final String name,
            @ApiParam(value = "Filter for operating system.", example = "LINUX")
            @RequestParam(name = PARAMETER_OS, required = false) final OS os,
            @ApiParam(value = "Filter for address. Partial match ignoring case.")
            @RequestParam(name = PARAMETER_ADDRESS, required = false) final String address,
            @ApiParam(value = "Filter for port.", example = "22")
            @RequestParam(name = PARAMETER_PORT, required = false) final Integer port) {
        logger.debug("readAll started ...");
        final CoreUtilities.ValidatedPageParams pageParameters = CoreUtilities
                .validatePageParameters(page, size, direction, createOrigin(READ_ALL_TARGET_URI));

        final var probe = new SshTarget(name, os, address, port);
        final Example<SshTarget> example = Example.of(probe, validation.exampleMatcher(mode));

        final Page<SshTarget> targets = targetService.pageByExample(example, pageParameters.createPageable(sortField));
        return new TargetListResponseDto(targets.map((x) -> new SshTargetDto(x.getName(), x.getOs(), x.getAddress(), x.getPort())));
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = UPDATE_TARGET_DESCRIPTION, response = SshTargetDto.class)
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = UPDATE_TARGET_SUCCESS),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = UPDATE_TARGET_BAD_REQUEST, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE, response = ErrorMessageDTO.class)
    })
    @PutMapping(UPDATE_TARGET_URI)
    @ResponseBody
    public SshTargetDto update(@PathVariable(PARAMETER_ADDRESS) final String address,
                               @PathVariable(PARAMETER_PORT) final Integer port,
                               @RequestBody final SshTargetDto dto) {
        logger.debug("update started ...");
        final String origin = createOrigin(UPDATE_TARGET_URI);
        validation.verifyAddress(address, origin);
        validation.verifyPort(port, origin);
        validation.verify(dto, origin);

        final Optional<SshTarget> optionalSshTarget = targetService.find(address, port);
        final SshTarget oldTarget = optionalSshTarget.orElseThrow(notFoundException("Target", origin));
        final SshTarget newTarget = targetService.replace(oldTarget, MscvDtoConverter.convert(dto));
        return new SshTargetDto(newTarget.getName(), newTarget.getOs(), newTarget.getAddress(), newTarget.getPort());
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = DELETE_TARGET_DESCRIPTION)
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_NO_CONTENT, message = DELETE_TARGET_SUCCESS),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = DELETE_TARGET_BAD_REQUEST, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE, response = ErrorMessageDTO.class)
    })
    @DeleteMapping(DELETE_TARGET_URI)
    @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
    public void delete(@PathVariable(PARAMETER_ADDRESS) final String address,
                       @PathVariable(PARAMETER_PORT) final Integer port) {
        logger.debug("delete started ...");
        final String origin = createOrigin(DELETE_TARGET_URI);
        validation.verifyAddress(address, origin);
        validation.verifyPort(port, origin);
        targetService.delete(address, port);
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = LOGIN_TARGET_DESCRIPTION, response = Void.class)
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = LOGIN_TARGET_SUCCESS, response = Void.class),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = LOGIN_TARGET_BAD_REQUEST, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_NOT_FOUND, message = LOGIN_TARGET_NOT_FOUND, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE, response = ErrorMessageDTO.class)
    })
    @PostMapping(LOGIN_TARGET_URI)
    @ResponseBody
    public void login(@RequestBody final TargetLoginRequest request) {
        try {
            logger.debug("login started ...");
            final String origin = createOrigin(LOGIN_TARGET_URI);
            validation.verify(request, origin);

            SshTarget sshTarget = MscvDtoConverter.convert(request.getTarget());
            sshTarget = targetService.findOrCreate(sshTarget);

            final MscvUtilities.Tuple2<String, String> credentials = MscvUtilities.decodeCredentials(request.getCredentials(), origin);
            targetService.login(sshTarget, credentials.getT1(), credentials.getT2());
        } catch (MscvException e) {
            throw new ArrowheadException(e.getMessage(), HttpStatus.SC_INTERNAL_SERVER_ERROR, createOrigin(LOGIN_TARGET_URI));
        }
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = VERIFY_LOGIN_TARGET_DESCRIPTION, response = Boolean.class)
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = LOGIN_TARGET_SUCCESS, response = Boolean.class),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = LOGIN_TARGET_BAD_REQUEST, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_NOT_FOUND, message = LOGIN_TARGET_NOT_FOUND, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE, response = ErrorMessageDTO.class)
    })
    @PostMapping(VERIFY_LOGIN_TARGET_URI)
    @ResponseBody
    public boolean verifyPasswordlessLogin(@RequestBody final TargetDto dto) {
        logger.debug("verifyPasswordlessLogin started ...");
        final String origin = createOrigin(VERIFY_LOGIN_TARGET_URI);
        validation.verify(dto, origin);

        final Optional<Target> optionalTarget = targetService.find(dto.getName(), dto.getOs());
        final Target target = optionalTarget.orElseThrow(notFoundException(LOGIN_TARGET_NOT_FOUND, origin));

        return targetService.verifyPasswordlessLogin(target);
    }

    private String createOrigin(final String path) {
        return CommonConstants.MSCV_URI + CoreCommonConstants.MGMT_URI + path;
    }
}
