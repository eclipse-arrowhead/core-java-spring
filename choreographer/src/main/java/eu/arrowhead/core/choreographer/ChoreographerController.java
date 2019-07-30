package eu.arrowhead.core.choreographer;

import eu.arrowhead.common.database.entity.ServiceDefinition;
import eu.arrowhead.common.dto.AuthorizationIntraCloudListResponseDTO;
import eu.arrowhead.common.dto.AuthorizationIntraCloudRequestDTO;
import eu.arrowhead.common.dto.choreographer.ChoreographerActionStepListResponseDTO;
import eu.arrowhead.common.dto.choreographer.ChoreographerActionStepRequestDTO;
import eu.arrowhead.common.dto.choreographer.ChoreographerActionStepResponseDTO;
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

import java.security.Provider;
import java.util.HashSet;
import java.util.Set;

@CrossOrigin(maxAge = Defaults.CORS_MAX_AGE, allowCredentials = Defaults.CORS_ALLOW_CREDENTIALS, 
allowedHeaders = { HttpHeaders.ORIGIN, HttpHeaders.CONTENT_TYPE, HttpHeaders.ACCEPT, HttpHeaders.AUTHORIZATION }
)
@RestController
public class ChoreographerController {

    private static final String ACTION_STEP_MGMT_URI = CommonConstants.MGMT_URI + "/actionstep";

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

    @ApiOperation(value = "Create the requested ChoreographerActionStep entries", response = ChoreographerActionStepListResponseDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_CREATED, message = POST_CHOREOGRAPHER_ACTION_STEP_WITH_SERVICE_DEFINITIONS_MGMT_HTTP_201_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = POST_CHOREOGRAPHER_ACTION_STEP_WITH_SERVICE_DEFINITIONS_MGMT_HTTP_400_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @PostMapping(path = ACTION_STEP_MGMT_URI, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = org.springframework.http.HttpStatus.CREATED)
    @ResponseBody
    public ChoreographerActionStepResponseDTO registerChoreographerActionStepWithServiceDefinition(@RequestBody final ChoreographerActionStepRequestDTO request) {
        Set<String> serviceNames = new HashSet<>();
        for (String serviceName : request.getUsedServices()) {
            serviceNames.add(serviceName);
        }

        logger.debug("registerAuthorizationIntraCloud has been finished");
        return choreographerDBService.createChoreographerActionStepWithServiceDefinitionResponse(request.getName(), serviceNames);
    }
}