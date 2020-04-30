package eu.arrowhead.core.mscv.controller;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.Defaults;
import eu.arrowhead.common.database.view.mscv.VerificationExecutionView;
import eu.arrowhead.common.dto.shared.ErrorMessageDTO;
import eu.arrowhead.core.mscv.Validation;
import eu.arrowhead.core.mscv.http.ClientExecutionRequest;
import eu.arrowhead.core.mscv.http.ClientExecutionResponse;
import eu.arrowhead.core.mscv.http.ExecutionRequest;
import eu.arrowhead.core.mscv.http.ExecutionResponse;
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
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import static eu.arrowhead.common.CommonConstants.OP_MSCV_EXECUTE_URI;

@Api(tags = {CoreCommonConstants.SWAGGER_TAG_ALL})
@CrossOrigin(maxAge = Defaults.CORS_MAX_AGE, allowCredentials = Defaults.CORS_ALLOW_CREDENTIALS,
        allowedHeaders = {HttpHeaders.ORIGIN, HttpHeaders.CONTENT_TYPE, HttpHeaders.ACCEPT}
)
@RestController
@RequestMapping(CommonConstants.MSCV_URI)
public class MscvExecutionController {

    //=================================================================================================
    // members

    private static final String EXECUTION_HTTP_201_MESSAGE = "Execution started";
    private static final String EXECUTION_HTTP_400_MESSAGE = "Request parameter missing";

    private final Logger logger = LogManager.getLogger(MscvExecutionController.class);
    private final VerificationExecutionService executionService;
    private final Validation validation;

    @Autowired
    public MscvExecutionController(final VerificationExecutionService executionService) {
        this.executionService = executionService;
        this.validation = new Validation();
    }

    //=================================================================================================
    // methods

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = "Return an echo message with the purpose of testing the core device availability", response = String.class,
            tags = {CoreCommonConstants.SWAGGER_TAG_CLIENT})
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = CoreCommonConstants.SWAGGER_HTTP_200_MESSAGE, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE, response = ErrorMessageDTO.class)
    })
    @GetMapping(path = CommonConstants.ECHO_URI)
    public String echoOnboarding() {
        return "Got it!";
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = "Request verification of client", response = ClientExecutionResponse.class,
            tags = {CoreCommonConstants.SWAGGER_TAG_CLIENT})
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_CREATED, message = EXECUTION_HTTP_201_MESSAGE, response = ClientExecutionResponse.class),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = EXECUTION_HTTP_400_MESSAGE, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE, response = ErrorMessageDTO.class)
    })
    @PostMapping(OP_MSCV_EXECUTE_URI)
    @ResponseBody
    public VerificationExecutionView execute(@RequestBody final ClientExecutionRequest executionRequest) {
        logger.debug("execute started ...");
        validation.verify(executionRequest, createOrigin(OP_MSCV_EXECUTE_URI));
        return executionService.executeWithDefaultList(executionRequest.getTarget(), executionRequest.getLayer());
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = "Execute verification list against target", response = ExecutionResponse.class,
            tags = {CoreCommonConstants.SWAGGER_TAG_MGMT})
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_CREATED, message = EXECUTION_HTTP_201_MESSAGE, response = ExecutionResponse.class),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = EXECUTION_HTTP_400_MESSAGE, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE, response = ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE, response = ErrorMessageDTO.class)
    })
    @PostMapping(CoreCommonConstants.MGMT_URI + OP_MSCV_EXECUTE_URI)
    @ResponseBody
    public VerificationExecutionView execute(@RequestBody final ExecutionRequest executionRequest) {
        logger.debug("execute started ...");
        return executionService.executeByIdAndTarget(executionRequest.getExecutionListId(), null);
    }

    private String createOrigin(final String path) {
        return CommonConstants.MSCV_URI + path;
    }

    private String createMgmtOrigin(final String path) {
        return CommonConstants.MSCV_URI + CoreCommonConstants.MGMT_URI + path;
    }
}