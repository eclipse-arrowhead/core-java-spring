package eu.arrowhead.core.choreographer;

import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.entity.ChoreographerActionStep;
import eu.arrowhead.common.dto.AuthorizationInterCloudListResponseDTO;
import eu.arrowhead.common.dto.SystemResponseDTO;
import eu.arrowhead.common.dto.choreographer.ChoreographerActionPlanRequestDTO;
import eu.arrowhead.common.dto.choreographer.ChoreographerActionPlanResponseDTO;
import eu.arrowhead.common.dto.choreographer.ChoreographerActionStepResponseDTO;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.core.choreographer.database.service.ChoreographerDBService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.Defaults;

import java.util.List;

@CrossOrigin(maxAge = Defaults.CORS_MAX_AGE, allowCredentials = Defaults.CORS_ALLOW_CREDENTIALS, 
allowedHeaders = { HttpHeaders.ORIGIN, HttpHeaders.CONTENT_TYPE, HttpHeaders.ACCEPT, HttpHeaders.AUTHORIZATION }
)
@RestController
public class ChoreographerController {

    private static final String PATH_VARIABLE_ID = "id";
    private static final String ID_NOT_VALID_ERROR_MESSAGE = "ID must be greater than 0.";

    private static final String CHOREOGRAPHER_ACTION_PLAN_MGMT_URI = CommonConstants.MGMT_URI + "/actionplan";
    private static final String CHOREOGRAPHER_ACTION_PLAN_MGMT_BY_ID_URI = CHOREOGRAPHER_ACTION_PLAN_MGMT_URI + "/{" + PATH_VARIABLE_ID + "}";

    private static final String GET_CHOREOGRAPHER_ACTION_PlAN_MGMT_HTTP_200_MESSAGE = "ChoreographerActionStep returned.";
    private static final String GET_CHOREOGRAPHER_ACTION_PLAN_MGMT_HTTP_400_MESSAGE = "Could not retrieve ChoreographerActionStep.";
    private static final String POST_CHOREOGRAPHER_ACTION_PLAN_WITH_SERVICE_DEFINITIONS_MGMT_HTTP_201_MESSAGE = "ChoreographerActionStep created with given service definitions.";
    private static final String POST_CHOREOGRAPHER_ACTION_PLAN_WITH_SERVICE_DEFINITIONS_MGMT_HTTP_400_MESSAGE = "Could not create ChoreographerActionStep.";
    private static final String DELETE_CHOREOGRAPHER_ACTION_PLAN_HTTP_200_MESSAGE = "ChoreographerActionPlan successfully removed.";
    private static final String DELETE_CHOREOGRAPHER_ACTION_PLAN_HTTP_400_MESSAGE = "Could not remove ChoreographerActionPlan.";

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

    @ApiOperation(value = "Registers an Action Plan.")
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_CREATED, message = POST_CHOREOGRAPHER_ACTION_PLAN_WITH_SERVICE_DEFINITIONS_MGMT_HTTP_201_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = POST_CHOREOGRAPHER_ACTION_PLAN_WITH_SERVICE_DEFINITIONS_MGMT_HTTP_400_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @PostMapping(path = CHOREOGRAPHER_ACTION_PLAN_MGMT_URI, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = org.springframework.http.HttpStatus.CREATED)
    @ResponseBody
    public void registerActionPlans(@RequestBody final ChoreographerActionPlanRequestDTO request) {
        choreographerDBService.createChoreographerActionPlan(request.getActionPlanName(), request.getActions());
    }

    @ApiOperation(value = "Remove the requested ChoreographerActionPlan entry.")
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = DELETE_CHOREOGRAPHER_ACTION_PLAN_HTTP_200_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = DELETE_CHOREOGRAPHER_ACTION_PLAN_HTTP_400_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @DeleteMapping(path = CHOREOGRAPHER_ACTION_PLAN_MGMT_BY_ID_URI)
    public void removeActionPlanById(@PathVariable(value = PATH_VARIABLE_ID) final long id) {
        logger.debug("New ChoreographerActionStep delete request received with id of " + id + ".");

        if (id < 1) {
            throw new BadPayloadException(ID_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, CommonConstants.AUTHORIZATION_URI + CHOREOGRAPHER_ACTION_PLAN_MGMT_BY_ID_URI);
        }

        choreographerDBService.removeActionPlanEntryById(id);
        logger.debug("ChoreographerActionStep with id: " + id + " successfully deleted!");
    }

    @ApiOperation(value = "Return the requested ChoreographerActionPlan entry.")
    @ApiResponses (value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = GET_CHOREOGRAPHER_ACTION_PlAN_MGMT_HTTP_200_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = GET_CHOREOGRAPHER_ACTION_PLAN_MGMT_HTTP_400_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @GetMapping(path = CHOREOGRAPHER_ACTION_PLAN_MGMT_BY_ID_URI, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody public ChoreographerActionPlanResponseDTO getActionPlanById(@PathVariable(value = PATH_VARIABLE_ID) final long id) {
        logger.debug("New ChoreographerActionStep get request received with id: " + id + ".");

        if (id < 1) {
            throw new BadPayloadException(ID_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, CommonConstants.AUTHORIZATION_URI + CHOREOGRAPHER_ACTION_PLAN_MGMT_BY_ID_URI);
        }

        ChoreographerActionPlanResponseDTO choreographerActionPlanEntryByIdResponse = choreographerDBService.getChoreographerActionPlanByIdResponse(id);
        logger.debug("ChoreographerActionPlan entry with id: " + " successfully retrieved!");

        return  choreographerActionPlanEntryByIdResponse;
    }

    @ApiOperation(value = "Return requested ChoreographerActionPlan entries by the given parameters.")
    @ApiResponses (value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = GET_CHOREOGRAPHER_ACTION_PlAN_MGMT_HTTP_200_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = GET_CHOREOGRAPHER_ACTION_PLAN_MGMT_HTTP_400_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @GetMapping(path = CHOREOGRAPHER_ACTION_PLAN_MGMT_URI, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody public List<ChoreographerActionPlanResponseDTO> getChoreographerActionPlans(
            @RequestParam(name = CommonConstants.REQUEST_PARAM_PAGE, required = false) final Integer page,
            @RequestParam(name = CommonConstants.REQUEST_PARAM_ITEM_PER_PAGE, required = false) final Integer size,
            @RequestParam(name = CommonConstants.REQUEST_PARAM_DIRECTION, defaultValue = Defaults.DEFAULT_REQUEST_PARAM_DIRECTION_VALUE) final String direction,
            @RequestParam(name = CommonConstants.REQUEST_PARAM_SORT_FIELD, defaultValue = CommonConstants.COMMON_FIELD_NAME_ID) final String sortField) {
        logger.debug("New AuthorizationIntraCloud get request received with page: {} and item_per page: {}.", page, size);

        int validatedPage;
        int validatedSize;
        if (page == null && size == null) {
            validatedPage = -1;
            validatedSize = -1;
        } else {
            if (page == null || size == null) {
                throw new BadPayloadException("Defined page or size could not be with undefined size or page.", HttpStatus.SC_BAD_REQUEST, CommonConstants.CHOREOGRAPHER_URI +
                        CHOREOGRAPHER_ACTION_PLAN_MGMT_URI);
            } else {
                validatedPage = page;
                validatedSize = size;
            }
        }
        Sort.Direction validatedDirection = Utilities.calculateDirection(direction, CommonConstants.CHOREOGRAPHER_URI + CHOREOGRAPHER_ACTION_PLAN_MGMT_URI);

        List<ChoreographerActionPlanResponseDTO> choreographerActionPlanEntriesResponse = choreographerDBService.getChoreographerActionPlanEntriesResponse(validatedPage, validatedSize,
                validatedDirection, sortField);
        logger.debug("ChoreographerActionPlans with page: {} and item_per page: {} retrieved successfully", page, size);

        return choreographerActionPlanEntriesResponse;
    }
}