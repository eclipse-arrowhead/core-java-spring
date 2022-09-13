package eu.arrowhead.core.mscv.controller;

import java.io.IOException;
import java.security.spec.InvalidKeySpecException;
import java.util.Optional;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.Defaults;
import eu.arrowhead.common.database.entity.mscv.Target;
import eu.arrowhead.common.database.view.mscv.VerificationExecutionView;
import eu.arrowhead.common.dto.shared.ErrorMessageDTO;
import eu.arrowhead.common.dto.shared.mscv.ClientExecutionRequest;
import eu.arrowhead.common.dto.shared.mscv.ExecutionRequest;
import eu.arrowhead.common.dto.shared.mscv.PublicKeyResponse;
import eu.arrowhead.common.dto.shared.mscv.TargetLoginRequest;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.core.mscv.MscvUtilities;
import eu.arrowhead.core.mscv.Validation;
import eu.arrowhead.core.mscv.security.KeyPairFileStorage;
import eu.arrowhead.core.mscv.service.MscvException;
import eu.arrowhead.core.mscv.service.TargetService;
import eu.arrowhead.core.mscv.service.VerificationExecutionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import static eu.arrowhead.common.CommonConstants.OP_MSCV_EXECUTE_URI;
import static eu.arrowhead.common.CommonConstants.OP_MSCV_LOGIN_URI;
import static eu.arrowhead.common.CommonConstants.OP_MSCV_PUBLIC_KEY_URI;
import static eu.arrowhead.core.mscv.MscvUtilities.notFoundException;
import static eu.arrowhead.core.mscv.Validation.LOGIN_TARGET_NOT_FOUND;

@Api(tags = {CoreCommonConstants.SWAGGER_TAG_ALL})
@CrossOrigin(maxAge = Defaults.CORS_MAX_AGE, allowCredentials = Defaults.CORS_ALLOW_CREDENTIALS,
        allowedHeaders = {HttpHeaders.ORIGIN, HttpHeaders.CONTENT_TYPE, HttpHeaders.ACCEPT}
)
@RestController
@RequestMapping(value = CommonConstants.MSCV_URI,
        consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
public class ExecutionController {

    //=================================================================================================
    // members

    private static final String EXECUTION_HTTP_202_MESSAGE = "Execution started";
    private static final String EXECUTION_HTTP_400_MESSAGE = "Request parameter missing";

    private static final String LOGIN_SUCCESS = "Execution started";
    private static final String LOGIN_BAD_REQUEST = "Request parameter missing";

    private final Logger logger = LogManager.getLogger(ExecutionController.class);
    private final VerificationExecutionService executionService;
    private final TargetService targetService;
    private final KeyPairFileStorage keyPairStorage;
    private final Validation validation;

    @Autowired
    public ExecutionController(final VerificationExecutionService executionService,
                               final TargetService targetService,
                               final KeyPairFileStorage keyPairStorage) {
        this.executionService = executionService;
        this.targetService = targetService;
        this.keyPairStorage = keyPairStorage;
        this.validation = new Validation();
    }

    //=================================================================================================
    // methods

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = "Return the Base64 encoded public key of the server as well as the SSH identity line", response = PublicKeyResponse.class,
            tags = {CoreCommonConstants.SWAGGER_TAG_CLIENT})
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = CoreCommonConstants.SWAGGER_HTTP_200_MESSAGE, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE, response = ErrorMessageDTO.class)
    })
    @GetMapping(OP_MSCV_PUBLIC_KEY_URI)
    public PublicKeyResponse getPublicKey() {
        try {
            return keyPairStorage.createPublicKeyResponse();
        } catch (IOException | InvalidKeySpecException e) {
            throw new ArrowheadException(e.getMessage(), HttpStatus.SC_INTERNAL_SERVER_ERROR, createOrigin(OP_MSCV_PUBLIC_KEY_URI));
        }
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = "Request remote login to the client to setup key-based login", response = Void.class,
            tags = {CoreCommonConstants.SWAGGER_TAG_CLIENT})
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = LOGIN_SUCCESS, response = Void.class),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = LOGIN_BAD_REQUEST, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_NOT_FOUND, message = LOGIN_TARGET_NOT_FOUND, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE, response = ErrorMessageDTO.class)
    })
    @PostMapping(OP_MSCV_LOGIN_URI)
    @ResponseBody
    public void login(@RequestBody final TargetLoginRequest request) {
        try {
            logger.debug("login started ...");
            final String origin = createOrigin(OP_MSCV_LOGIN_URI);
            validation.verify(request, origin);

            final MscvUtilities.Tuple2<String, String> credentials = MscvUtilities.decodeCredentials(request.getCredentials(), origin);

            final Optional<Target> optionalSshTarget = targetService.find(request.getTarget().getName(), request.getTarget().getOs());
            final Target target = optionalSshTarget.orElseThrow(notFoundException(LOGIN_TARGET_NOT_FOUND, origin));

            targetService.login(target, credentials.getT1(), credentials.getT2());
        } catch (MscvException e) {
            throw new ArrowheadException(e.getMessage(), HttpStatus.SC_INTERNAL_SERVER_ERROR, createOrigin(OP_MSCV_LOGIN_URI));
        }
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = "Request verification of client", response = VerificationExecutionView.class,
            tags = {CoreCommonConstants.SWAGGER_TAG_CLIENT})
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_ACCEPTED, message = EXECUTION_HTTP_202_MESSAGE, response = VerificationExecutionView.class),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = EXECUTION_HTTP_400_MESSAGE, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE, response = ErrorMessageDTO.class)
    })
    @PostMapping(OP_MSCV_EXECUTE_URI)
    @ResponseBody
    public ResponseEntity<VerificationExecutionView> execute(@RequestBody final ClientExecutionRequest executionRequest) {
        logger.debug("execute started ...");
        validation.verify(executionRequest, createOrigin(OP_MSCV_EXECUTE_URI));
        final VerificationExecutionView verificationExecutionView = executionService
                .executeWithDefaultList(executionRequest.getTarget(), executionRequest.getLayer());
        return ResponseEntity.status(HttpStatus.SC_ACCEPTED).body(verificationExecutionView);
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = "Request verification against target", response = VerificationExecutionView.class,
            tags = {CoreCommonConstants.SWAGGER_TAG_MGMT})
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_ACCEPTED, message = EXECUTION_HTTP_202_MESSAGE, response = VerificationExecutionView.class),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = EXECUTION_HTTP_400_MESSAGE, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE, response = ErrorMessageDTO.class)
    })
    @PostMapping(CoreCommonConstants.MGMT_URI + OP_MSCV_EXECUTE_URI)
    @ResponseBody
    public ResponseEntity<VerificationExecutionView> execute(@RequestBody final ExecutionRequest executionRequest) {
        logger.debug("execute started ...");
        final VerificationExecutionView verificationExecutionView = executionService
                .executeByIdAndTarget(executionRequest.getExecutionListId(), executionRequest.getTargetId());
        return ResponseEntity.status(HttpStatus.SC_ACCEPTED).body(verificationExecutionView);
    }

    private String createOrigin(final String path) {
        return CommonConstants.MSCV_URI + path;
    }

}