package eu.arrowhead.core.choreographer;

import eu.arrowhead.common.database.entity.ChoreographerActionPlan;
import eu.arrowhead.common.dto.choreographer.ChoreographerActionPlanRequestDTO;
import eu.arrowhead.common.dto.choreographer.ChoreographerActionRequestDTO;
import eu.arrowhead.core.choreographer.database.service.ChoreographerDBService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.Defaults;

@CrossOrigin(maxAge = Defaults.CORS_MAX_AGE, allowCredentials = Defaults.CORS_ALLOW_CREDENTIALS, 
allowedHeaders = { HttpHeaders.ORIGIN, HttpHeaders.CONTENT_TYPE, HttpHeaders.ACCEPT, HttpHeaders.AUTHORIZATION }
)
@RestController
public class ChoreographerController {

    private static final String PATH_VARIABLE_ID = "id";
    private static final String ID_NOT_VALID_ERROR_MESSAGE = "ID must be greater than 0.";

    private static final String ACTION_STEP_MGMT_URI = CommonConstants.MGMT_URI + "/actionstep";
    private static final String CHOREOGRAPHER_ACTION_STEP_MGMT_BY_ID_URI = ACTION_STEP_MGMT_URI + "/{" + PATH_VARIABLE_ID + "}";

    private static final String GET_CHOREOGRAPHER_ACTION_STEP_MGMT_HTTP_200_MESSAGE = "ChoreographerActionStep returned.";
    private static final String GET_CHOREOGRAPHER_ACTION_STEP_MGMT_HTTP_400_MESSAGE = "Could not retrieve ChoreographerActionStep.";
    private static final String POST_CHOREOGRAPHER_ACTION_STEP_WITH_SERVICE_DEFINITIONS_MGMT_HTTP_201_MESSAGE = "ChoreographerActionStep created with given service definitions.";
    private static final String POST_CHOREOGRAPHER_ACTION_STEP_WITH_SERVICE_DEFINITIONS_MGMT_HTTP_400_MESSAGE = "Could not create ChoreographerActionStep.";

    private final Logger logger = LogManager.getLogger(ChoreographerController.class);

    @Autowired
    private ChoreographerDBService choreographerDBService;

    @ApiOperation(value = "Return an echo message with the purpose of testing the core service availability", response = String.class)
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = CommonConstants.SWAGGER_HTTP_200_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @GetMapping(path = CommonConstants.ECHO_URI)
    public String echoService() {
        return "Got it!";
    }

    @ApiOperation(value = "TEST")
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_CREATED, message = POST_CHOREOGRAPHER_ACTION_STEP_WITH_SERVICE_DEFINITIONS_MGMT_HTTP_201_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = POST_CHOREOGRAPHER_ACTION_STEP_WITH_SERVICE_DEFINITIONS_MGMT_HTTP_400_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @PostMapping(path = ACTION_STEP_MGMT_URI, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = org.springframework.http.HttpStatus.CREATED)
    @ResponseBody
    public void registerActionPlans(@RequestBody final ChoreographerActionPlanRequestDTO request) {
        choreographerDBService.createChoreographerActionPlan(request.getActionPlanName(), request.getActions());
    }
}