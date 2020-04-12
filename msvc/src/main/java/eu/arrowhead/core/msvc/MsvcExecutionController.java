package eu.arrowhead.core.msvc;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.Defaults;
import eu.arrowhead.core.msvc.database.service.MsvcExecutionService;
import eu.arrowhead.core.msvc.database.view.VerificationExecutionView;
import eu.arrowhead.core.msvc.http.ExecutionRequest;
import eu.arrowhead.core.msvc.http.ExecutionResponse;
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

import static eu.arrowhead.common.CommonConstants.OP_MSVC_EXECUTE;

@Api(tags = {CoreCommonConstants.SWAGGER_TAG_CLIENT})
@CrossOrigin(maxAge = Defaults.CORS_MAX_AGE, allowCredentials = Defaults.CORS_ALLOW_CREDENTIALS,
        allowedHeaders = {HttpHeaders.ORIGIN, HttpHeaders.CONTENT_TYPE, HttpHeaders.ACCEPT}
)
@RestController
@RequestMapping(CommonConstants.MSVC_URI)
public class MsvcExecutionController {

    //=================================================================================================
    // members

    private static final String EXECUTION_HTTP_201_MESSAGE = "Execution started";
    private static final String EXECUTION_HTTP_400_MESSAGE = "Request parameter missing";

    private final Logger logger = LogManager.getLogger(MsvcExecutionController.class);
    private final MsvcExecutionService executionService;

    @Autowired
    public MsvcExecutionController(final MsvcExecutionService executionService) {
        this.executionService = executionService;
    }

    //=================================================================================================
    // methods

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = "Return an echo message with the purpose of testing the core device availability", response = String.class,
            tags = {CoreCommonConstants.SWAGGER_TAG_CLIENT})
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = CoreCommonConstants.SWAGGER_HTTP_200_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @GetMapping(path = CommonConstants.ECHO_URI)
    public String echoOnboarding() {
        return "Got it!";
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = "Execute verification list against target", response = ExecutionResponse.class,
            tags = {CoreCommonConstants.SWAGGER_TAG_CLIENT})
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_CREATED, message = EXECUTION_HTTP_201_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = EXECUTION_HTTP_400_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @PostMapping(OP_MSVC_EXECUTE)
    @ResponseBody
    public VerificationExecutionView execute(@RequestBody final ExecutionRequest executionRequest) {
        logger.debug("execute started ...");
        return executionService.executeByIdAndTarget(executionRequest.getExecutionListId(), null);
    }
}