/********************************************************************************
 * Copyright (c) 2020 AITIA
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   AITIA - implementation
 *   Arrowhead Consortia - conceptualization
 ********************************************************************************/

package eu.arrowhead.core.choreographer;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.CoreDefaults;
import eu.arrowhead.common.CoreUtilities;
import eu.arrowhead.common.Defaults;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.CoreUtilities.ValidatedPageParams;
import eu.arrowhead.common.database.entity.ChoreographerSession;
import eu.arrowhead.common.dto.internal.ChoreographerPlanRequestDTO;
import eu.arrowhead.common.dto.internal.ChoreographerRunPlanRequestDTO;
import eu.arrowhead.common.dto.internal.ChoreographerStartSessionDTO;
import eu.arrowhead.common.dto.shared.ChoreographerSessionRunningStepDataDTO;
import eu.arrowhead.common.dto.shared.ChoreographerPlanResponseDTO;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.core.choreographer.database.service.ChoreographerDBService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpHeaders;

import org.springframework.http.MediaType;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static eu.arrowhead.common.CommonConstants.OP_CHOREOGRAPHER_NOTIFY_STEP_DONE;

@Api(tags = { CoreCommonConstants.SWAGGER_TAG_ALL })
@CrossOrigin(maxAge = Defaults.CORS_MAX_AGE, allowCredentials = Defaults.CORS_ALLOW_CREDENTIALS,
			 allowedHeaders = { HttpHeaders.ORIGIN, HttpHeaders.CONTENT_TYPE, HttpHeaders.ACCEPT, HttpHeaders.AUTHORIZATION }
)
@RestController
@RequestMapping(CommonConstants.CHOREOGRAPHER_URI)
public class ChoreographerController {


	//=================================================================================================
	// members

    private static final String PATH_VARIABLE_ID = "id";
    private static final String ID_NOT_VALID_ERROR_MESSAGE = "ID must be greater than 0.";

    private static final String PLAN_MGMT_URI = CoreCommonConstants.MGMT_URI + "/plan";
    private static final String PLAN_MGMT_BY_ID_URI = PLAN_MGMT_URI + "/{" + PATH_VARIABLE_ID + "}";
    private static final String SESSION_MGMT_URI = CoreCommonConstants.MGMT_URI + "/session";

    private static final String START_SESSION_MGMT_URI = SESSION_MGMT_URI + "/start";
    private static final String STEP_FINISHED_MGMT_URI = SESSION_MGMT_URI + "/stepFinished";

    private static final String GET_PlAN_MGMT_HTTP_200_MESSAGE = "Step returned.";
    private static final String GET_PLAN_MGMT_HTTP_400_MESSAGE = "Could not retrieve Step.";

    private static final String POST_PLAN_MGMT_HTTP_201_MESSAGE = "Plan created with given service definition and first Action.";
    private static final String POST_PLAN_MGMT_HTTP_400_MESSAGE = "Could not create Plan.";

    private static final String DELETE_PLAN_HTTP_200_MESSAGE = "Plan successfully removed.";
    private static final String DELETE_PLAN_HTTP_400_MESSAGE = "Could not remove Plan.";

    private static final String START_SESSION_HTTP_200_MESSAGE = "Initiated running plan with given id.";
    private static final String START_PLAN_HTTP_400_MESSAGE = "Could not start Plan with given ID.";

    private static final String STEP_FINISHED_HTTP_200_MESSAGE = "Choreographer notified that the running step is done.";
    private static final String STEP_FINISHED_HTTP_400_MESSAGE = "Could not notify Choreographer that the running step is done.";


    private final Logger logger = LogManager.getLogger(ChoreographerController.class);

    @Autowired
    private ChoreographerDBService choreographerDBService;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private JmsTemplate jmsTemplate;
    
    //=================================================================================================
	// methods

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = "Return an echo message with the purpose of testing the core service availability", response = String.class, tags = { CoreCommonConstants.SWAGGER_TAG_CLIENT })
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = CoreCommonConstants.SWAGGER_HTTP_200_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @GetMapping(path = CommonConstants.ECHO_URI)
    public String echoService() {
        return "Got it!";
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = "Register one or more Plans.",
    			  tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_CREATED, message = POST_PLAN_MGMT_HTTP_201_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = POST_PLAN_MGMT_HTTP_400_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @PostMapping(path = PLAN_MGMT_URI, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = org.springframework.http.HttpStatus.CREATED)
    @ResponseBody public void registerPlan(@RequestBody final ChoreographerPlanRequestDTO request) {
        checkPlanRequest(request, CommonConstants.CHOREOGRAPHER_URI + PLAN_MGMT_URI);
        choreographerDBService.createPlan(request.getName(), request.getFirstActionName(), request.getActions());
    }

    //-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Remove the requested Plan entry.", tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = DELETE_PLAN_HTTP_200_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = DELETE_PLAN_HTTP_400_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @DeleteMapping(path = PLAN_MGMT_BY_ID_URI)
    public void removeActionPlanById(@PathVariable(value = PATH_VARIABLE_ID) final long id) {
        logger.debug("New Plan delete request received with id of " + id + ".");

        if (id < 1) {
            throw new BadPayloadException(ID_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, CommonConstants.CHOREOGRAPHER_URI + PLAN_MGMT_BY_ID_URI);
        }

        choreographerDBService.removePlanEntryById(id);
        logger.debug("Plan with id: " + id + " successfully deleted!");
    }

    //-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return the requested Plan entry.", response = ChoreographerPlanResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
    @ApiResponses (value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = GET_PlAN_MGMT_HTTP_200_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = GET_PLAN_MGMT_HTTP_400_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @GetMapping(path = PLAN_MGMT_BY_ID_URI, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody public ChoreographerPlanResponseDTO getActionPlanById(@PathVariable(value = PATH_VARIABLE_ID) final long id) {
        logger.debug("New ChoreographerActionStep get request received with id: " + id + ".");

        if (id < 1) {
            throw new BadPayloadException(ID_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, CommonConstants.CHOREOGRAPHER_URI + PLAN_MGMT_BY_ID_URI);
        }

        final ChoreographerPlanResponseDTO choreographerActionPlanEntryByIdResponse = choreographerDBService.getPlanByIdResponse(id);
        logger.debug("ChoreographerActionPlan entry with id: " + " successfully retrieved!");

        return choreographerActionPlanEntryByIdResponse;
    }

    //-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return requested Plan entries by the given parameters.", response = List.class, tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
    @ApiResponses (value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = GET_PlAN_MGMT_HTTP_200_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = GET_PLAN_MGMT_HTTP_400_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @GetMapping(path = PLAN_MGMT_URI, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody public List<ChoreographerPlanResponseDTO> getChoreographerActionPlans(
            @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_PAGE, required = false) final Integer page,
            @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_ITEM_PER_PAGE, required = false) final Integer size,
            @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_DIRECTION, defaultValue = CoreDefaults.DEFAULT_REQUEST_PARAM_DIRECTION_VALUE) final String direction,
            @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_SORT_FIELD, defaultValue = CoreCommonConstants.COMMON_FIELD_NAME_ID) final String sortField) {
        logger.debug("New Plan get request received with page: {} and item_per page: {}.", page, size);

        final ValidatedPageParams validatedPageParams = CoreUtilities.validatePageParameters(page, size, direction, sortField);
        final List<ChoreographerPlanResponseDTO> choreographerActionPlanEntriesResponse = choreographerDBService.getPlanEntriesResponse(validatedPageParams.getValidatedPage(),
        																																						 validatedPageParams.getValidatedSize(),
        																																						 validatedPageParams.getValidatedDirection(),
        																																						 sortField);
        logger.debug("Plan with page: {} and item_per page: {} retrieved successfully", page, size);

        return choreographerActionPlanEntriesResponse;
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = "Initiate the start of one or more plans.",
            tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_CREATED, message = START_SESSION_HTTP_200_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = START_PLAN_HTTP_400_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @PostMapping(path = START_SESSION_MGMT_URI, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = org.springframework.http.HttpStatus.CREATED)
    @ResponseBody public void startPlan(@RequestBody final List<ChoreographerRunPlanRequestDTO> requests) {
        for (final ChoreographerRunPlanRequestDTO request : requests) {
            logger.debug("startPlan started...");
            ChoreographerSession session = choreographerDBService.initiateSession(request.getId());

            logger.debug("Sending a message to start-session.");
            jmsTemplate.convertAndSend("start-session", new ChoreographerStartSessionDTO(session.getId(), request.getId()));
        }
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = "Notify the Choreographer that a step is done in a session.",
        tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_CREATED, message = STEP_FINISHED_HTTP_200_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = STEP_FINISHED_HTTP_400_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @PostMapping(path = STEP_FINISHED_MGMT_URI, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = org.springframework.http.HttpStatus.OK)
    @ResponseBody public void stepFinished(@RequestBody final List<ChoreographerSessionRunningStepDataDTO> requests) {
        for (final ChoreographerSessionRunningStepDataDTO request : requests) {
            logger.debug("notifyStepDone started...");
            logger.debug("Sending message to session-step-done.");
            jmsTemplate.convertAndSend("session-step-done", request);
        }
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = "Notify the Choreographer that a step is done in a session.",
            tags = { CoreCommonConstants.SWAGGER_TAG_CLIENT })
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_CREATED, message = STEP_FINISHED_HTTP_200_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = STEP_FINISHED_HTTP_400_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @PostMapping(path = OP_CHOREOGRAPHER_NOTIFY_STEP_DONE, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = org.springframework.http.HttpStatus.OK)
    @ResponseBody public void notifyStepDone(@RequestBody final ChoreographerSessionRunningStepDataDTO request) {
        logger.debug("notifyStepDone started...");
        logger.debug("Sending message to session-step-done.");
        jmsTemplate.convertAndSend("session-step-done", request);
    }


    //=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
    private void checkPlanRequest(final ChoreographerPlanRequestDTO request, final String origin) {
        logger.debug("checkPlanRequest started...");

        if (Utilities.isEmpty(request.getName())) {
            throw new BadPayloadException("Plan name is null or blank.", HttpStatus.SC_BAD_REQUEST, origin);
        }
    }
}