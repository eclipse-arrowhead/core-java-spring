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
import eu.arrowhead.common.database.entity.ChoreographerAction;
import eu.arrowhead.common.database.entity.ChoreographerPlan;
import eu.arrowhead.common.database.entity.ChoreographerSession;
import eu.arrowhead.common.database.entity.ChoreographerStep;
import eu.arrowhead.common.database.entity.ChoreographerStepDetail;
import eu.arrowhead.common.dto.internal.ChoreographerExecutorListResponseDTO;
import eu.arrowhead.common.dto.internal.ChoreographerExecutorSearchResponseDTO;
import eu.arrowhead.common.dto.internal.ChoreographerPlanRequestDTO;
import eu.arrowhead.common.dto.internal.ChoreographerRunPlanRequestDTO;
import eu.arrowhead.common.dto.internal.ChoreographerStartSessionDTO;
import eu.arrowhead.common.dto.internal.ChoreographerSuitableExecutorResponseDTO;
import eu.arrowhead.common.dto.shared.ChoreographerExecutorRequestDTO;
import eu.arrowhead.common.dto.shared.ChoreographerExecutorResponseDTO;
import eu.arrowhead.common.dto.shared.ChoreographerSessionRunningStepDataDTO;
import eu.arrowhead.common.dto.shared.ChoreographerPlanResponseDTO;
import eu.arrowhead.common.dto.shared.ServiceRegistryResponseDTO;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.core.choreographer.database.service.ChoreographerDBService;
import eu.arrowhead.core.choreographer.service.ChoreographerDriver;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Sort;
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static eu.arrowhead.common.CommonConstants.*;


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
    private static final String EXECUTOR_MGMT_URI = CoreCommonConstants.MGMT_URI + "/executor";
    private static final String EXECUTOR_MGMT_BY_ID_URI = EXECUTOR_MGMT_URI + "/{" + PATH_VARIABLE_ID + "}";
    private static final String EXECUTOR_MGMT_TEST_BY_STEP_ID = EXECUTOR_MGMT_URI + "/byStepId" + "/{" + PATH_VARIABLE_ID + "}";

    private static final String START_SESSION_MGMT_URI = SESSION_MGMT_URI + "/start";
    private static final String STEP_FINISHED_MGMT_URI = SESSION_MGMT_URI + "/notifyStepDone";

    private static final String EXECUTOR_SEARCH_MGMT_URI = EXECUTOR_MGMT_URI + "/searchExecutors";

    private static final String GET_PlAN_MGMT_HTTP_200_MESSAGE = "Step returned.";
    private static final String GET_PLAN_MGMT_HTTP_400_MESSAGE = "Could not retrieve Step.";
    private static final String GET_EXECUTOR_HTTP_200_MESSAGE = "Executor returned.";
    private static final String GET_EXECUTOR_HTTP_400_MESSAGE = "Could not retrieve Executor.";

    private static final String POST_PLAN_MGMT_HTTP_201_MESSAGE = "Plan created with given service definition and first Action.";
    private static final String POST_PLAN_MGMT_HTTP_400_MESSAGE = "Could not create Plan.";
    private static final String POST_EXECUTOR_HTTP_201_MESSAGE = "Executor created.";
    private static final String POST_EXECUTOR_HTTP_400_MESSAGE = "Could not create executor.";

    private static final String DELETE_PLAN_HTTP_200_MESSAGE = "Plan successfully removed.";
    private static final String DELETE_PLAN_HTTP_400_MESSAGE = "Could not remove Plan.";
    private static final String DELETE_EXECUTOR_HTTP_200_MESSAGE = "Executor successfully removed.";
    private static final String DELETE_EXECUTOR_HTTP_400_MESSAGE = "Could not remove Executor.";

    private static final String START_SESSION_HTTP_200_MESSAGE = "Initiated running plan with given id.";
    private static final String START_PLAN_HTTP_400_MESSAGE = "Could not start Plan with given ID.";

    private static final String STEP_FINISHED_HTTP_200_MESSAGE = "Choreographer notified that the running step is done.";
    private static final String STEP_FINISHED_HTTP_400_MESSAGE = "Could not notify Choreographer that the running step is done.";
    private static final String EXECUTOR_NAME_NULL_ERROR_MESSAGE = "Executor name can't be null.";
    private static final String EXECUTOR_ADDRESS_NULL_ERROR_MESSAGE = "Executor address can't be null.";
    private static final String EXECUTOR_PORT_NULL_ERROR_MESSAGE = "Executor port can't be null.";
    private static final String EXECUTOR_SD_NULL_ERROR_MESSAGE = "Executor service definition name can't be null.";
    private static final String EXECUTOR_VERSION_NULL_ERROR_MESSAGE = "Executor version number can't be null.";
    private static final String EXECUTOR_VERSION_MIN_VERSION_MAX_VERSION_AMBIGUOUS_ERROR_MESSAGE = "Minimum and maximum version requirements can only be used if version requirement is left blank. Version requirement can only be used if minimum and maximum version requirements are left blank.";
    private static final String EXECUTOR_MIN_VERSION_GREATER_THAN_MAX_VERSION_ERROR_MESSAGE = "Maximum version requirement must be greater or equal to the minimum version requirement.";
    private static final String CHOREOGRAPHER_INSUFFICIENT_PROVIDERS_FOR_PLAN_ERROR_MESSAGE = "Can't start plan because not every service definition has a corresponding provider.";
    private static final String CHOREOGRAPHER_INSUFFICIENT_EXECUTORS_FOR_PLAN_ERROR_MESSAGE = "Can't start plan because not every service definition has a corresponding executor.";

    private static final String EXECUTOR_REQUEST_PARAM_SERVICE_DEFINITION = "service-definition";
    private static final String EXECUTOR_REQUEST_PARAM_MIN_VERSION = "min_version";
    private static final String EXECUTOR_REQUEST_PARAM_MAX_VERSION = "max_version";
    private static final String EXECUTOR_REQUEST_PARAM_VERSION = "version";

    private final Logger logger = LogManager.getLogger(ChoreographerController.class);

    @Autowired
    private ChoreographerDBService choreographerDBService;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private ChoreographerDriver choreographerDriver;

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
    @ApiOperation(value = "Register a plan.",
    			  tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_CREATED, message = POST_PLAN_MGMT_HTTP_201_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = POST_PLAN_MGMT_HTTP_400_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @PostMapping(path = PLAN_MGMT_URI, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = org.springframework.http.HttpStatus.CREATED)
    @ResponseBody public ChoreographerPlanResponseDTO registerPlan(@RequestBody final ChoreographerPlanRequestDTO request) {
        checkPlanRequest(request, CommonConstants.CHOREOGRAPHER_URI + PLAN_MGMT_URI);

        return choreographerDBService.createPlanResponse(request.getName(), request.getFirstActionName(), request.getActions());
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
    public void removePlanById(@PathVariable(value = PATH_VARIABLE_ID) final long id) {
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
    @ResponseBody public ChoreographerPlanResponseDTO getPlanById(@PathVariable(value = PATH_VARIABLE_ID) final long id) {
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
    @ResponseBody public List<ChoreographerPlanResponseDTO> getPlans(
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

            checkIfPlanHasEveryRequiredProvider(request, CommonConstants.CHOREOGRAPHER_URI + START_SESSION_MGMT_URI);
            checkIfPlanHasEveryRequiredExecutor(request, CommonConstants.CHOREOGRAPHER_URI + START_SESSION_MGMT_URI);

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
    @ResponseBody public void notifyStepDoneMgmt(@RequestBody final List<ChoreographerSessionRunningStepDataDTO> requests) {
        for (final ChoreographerSessionRunningStepDataDTO request : requests) {
            logger.debug("notifyStepDone started...");
            logger.debug("Sending message to session-step-done.");
            jmsTemplate.convertAndSend("session-step-done", request);
        }
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = "Notify the Choreographer that a step is done in a session.", tags = { CoreCommonConstants.SWAGGER_TAG_CLIENT })
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

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = "Return created executor.", response = ChoreographerExecutorResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_CLIENT })
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_CREATED, message = POST_EXECUTOR_HTTP_201_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = POST_EXECUTOR_HTTP_400_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @PostMapping(path = OP_CHOREOGRAPHER_EXECUTOR_REGISTER, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
    @ResponseBody public ChoreographerExecutorResponseDTO registerExecutor(@RequestBody final ChoreographerExecutorRequestDTO request) {
        return callCreateExecutor(request);
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = "Remove executor.", tags = { CoreCommonConstants.SWAGGER_TAG_CLIENT })
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = DELETE_EXECUTOR_HTTP_200_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = DELETE_EXECUTOR_HTTP_400_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @DeleteMapping(path = OP_CHOREOGRAPHER_EXECUTOR_UNREGISTER)
    public void unregisterExecutor(@RequestParam(CommonConstants.OP_CHOREOGRAPHER_EXECUTOR_UNREGISTER_REQUEST_PARAM_ADDRESS) final String executorAddress,
                                   @RequestParam(CommonConstants.OP_CHOREOGRAPHER_EXECUTOR_UNREGISTER_REQUEST_PARAM_PORT) final int executorPort,
                                   @RequestParam(CommonConstants.OP_CHOREOGRAPHER_EXECUTOR_UNREGISTER_REQUEST_PARAM_BASE_URI) final String executorBaseUri) {
        logger.debug("Executor removal request received.");

        checkUnregisterExecutorParameters(executorAddress, executorPort, executorBaseUri);

        choreographerDBService.removeExecutor(executorAddress, executorPort, executorBaseUri);
        logger.debug("Removed Executor with address: {}, port: {} and baseURI: {}", executorAddress, executorPort, executorBaseUri);
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = "Notify the Choreographer that an error happened during the execution of a step in a session.", tags = { CoreCommonConstants.SWAGGER_TAG_CLIENT })
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_CREATED, message = STEP_FINISHED_HTTP_200_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = STEP_FINISHED_HTTP_400_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @PostMapping(path = OP_CHOREOGRAPHER_EXECUTOR_NOTIFY_STEP_ERROR, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = org.springframework.http.HttpStatus.OK)
    @ResponseBody public void notifyStepError(@RequestBody final ChoreographerSessionRunningStepDataDTO request) {
        logger.debug("notifyStepError started...");
        logger.debug("Sending message to session-step-error.");
        jmsTemplate.convertAndSend("session-step-error", request);
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = "Return created executor.", response = ChoreographerExecutorResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_CREATED, message = POST_EXECUTOR_HTTP_201_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = POST_EXECUTOR_HTTP_400_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @PostMapping(path = EXECUTOR_MGMT_URI, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
    @ResponseBody public ChoreographerExecutorResponseDTO addExecutor(@RequestBody final ChoreographerExecutorRequestDTO request) {
        return callCreateExecutor(request);
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = "Return requested executor entry.", response = ServiceRegistryResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = GET_EXECUTOR_HTTP_200_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = GET_EXECUTOR_HTTP_400_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @GetMapping(path = EXECUTOR_MGMT_BY_ID_URI)
    @ResponseBody public ChoreographerExecutorResponseDTO getExecutorEntryById(@PathVariable(value = PATH_VARIABLE_ID) final long id) {
        logger.debug("New Executor get request received with id: {}", id);

        if (id < 1) {
            throw new BadPayloadException(ID_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, CommonConstants.CHOREOGRAPHER_URI + EXECUTOR_MGMT_BY_ID_URI);
        }
        final ChoreographerExecutorResponseDTO executorEntryByIdResponse = choreographerDBService.getExecutorEntryByIdResponse(id);
        logger.debug("Executor entry with id: {} successfully retrieved", id);

        return executorEntryByIdResponse;
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = "Return the ids of the suitable Executors entries by step id.", response = ServiceRegistryResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = GET_EXECUTOR_HTTP_200_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = GET_EXECUTOR_HTTP_400_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @GetMapping(path = EXECUTOR_MGMT_TEST_BY_STEP_ID)
    @ResponseBody public ChoreographerSuitableExecutorResponseDTO getSuitableExecutorIds(@PathVariable(value = PATH_VARIABLE_ID) final long id) {
        logger.debug("getSuitableExecutorIds started...");

        if (id < 1) {
            throw new BadPayloadException(ID_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, CommonConstants.CHOREOGRAPHER_URI + EXECUTOR_MGMT_BY_ID_URI);
        }

        return choreographerDBService.getSuitableExecutorIdsByStepId(id);
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = "Return requested executor entries by the given parameters", response = ChoreographerExecutorListResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = GET_EXECUTOR_HTTP_200_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = GET_EXECUTOR_HTTP_400_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @GetMapping(path = EXECUTOR_MGMT_URI)
    @ResponseBody public ChoreographerExecutorListResponseDTO getExecutorEntries(
            @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_PAGE, required = false) final Integer page,
            @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_ITEM_PER_PAGE, required = false) final Integer size,
            @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_DIRECTION, defaultValue = CoreDefaults.DEFAULT_REQUEST_PARAM_DIRECTION_VALUE) final String direction,
            @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_SORT_FIELD, defaultValue = CoreCommonConstants.COMMON_FIELD_NAME_ID) final String sortField) {
        logger.debug("New Executor get request received with page: {} and item_per page: {}", page, size);

        int validatedPage;
        int validatedSize;
        if (page == null && size == null) {
            validatedPage = -1;
            validatedSize = -1;
        } else {
            if (page == null || size == null) {
                throw new BadPayloadException("Defined page or size could not be with undefined size or page.", HttpStatus.SC_BAD_REQUEST, CommonConstants.CHOREOGRAPHER_URI +
                        EXECUTOR_MGMT_URI);
            } else {
                validatedPage = page;
                validatedSize = size;
            }
        }

        final Sort.Direction validatedDirection = CoreUtilities.calculateDirection(direction, CommonConstants.CHOREOGRAPHER_URI + EXECUTOR_MGMT_URI);
        final ChoreographerExecutorListResponseDTO executorEntriesResponse = choreographerDBService.getExecutorEntriesResponse(validatedPage, validatedSize, validatedDirection, sortField);
        logger.debug("Service Registry entries with page: {} and item_per page: {} successfully retrieved", page, size);

        return executorEntriesResponse;
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = "Remove executor.", tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = DELETE_EXECUTOR_HTTP_200_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = DELETE_EXECUTOR_HTTP_400_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @DeleteMapping(path = EXECUTOR_MGMT_BY_ID_URI)
    public void removeExecutor(@PathVariable(value = PATH_VARIABLE_ID) final long id) {
        logger.debug("New Executor delete request received with id: {}", id);

        if (id < 1) {
            throw new BadPayloadException(ID_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, CommonConstants.CHOREOGRAPHER_URI + EXECUTOR_MGMT_BY_ID_URI);
        }

        choreographerDBService.removeExecutorEntryById(id);
        logger.debug("Executor with id: '{}' successfully deleted", id);
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = "Return list of executors suited to execute the task with the service definition and version requirements.",
            response = ChoreographerExecutorSearchResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = GET_EXECUTOR_HTTP_200_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = GET_EXECUTOR_HTTP_400_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @GetMapping(path = EXECUTOR_SEARCH_MGMT_URI)
    @ResponseBody public ChoreographerExecutorSearchResponseDTO getExecutorsByServiceDefinitionAndVersion(
            @RequestParam(name = EXECUTOR_REQUEST_PARAM_SERVICE_DEFINITION, required = true) final String serviceDefinition,
            @RequestParam(name = EXECUTOR_REQUEST_PARAM_MIN_VERSION, required = false) final Integer minVersion,
            @RequestParam(name = EXECUTOR_REQUEST_PARAM_MAX_VERSION, required = false) final Integer maxVersion,
            @RequestParam(name = EXECUTOR_REQUEST_PARAM_VERSION, required = false) final Integer version) {

        logger.debug("New Executor get request received with service definition: {}", serviceDefinition);

        if (minVersion != null && maxVersion != null && version == null) {
            if (maxVersion < minVersion) {
                throw new BadPayloadException(EXECUTOR_MIN_VERSION_GREATER_THAN_MAX_VERSION_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, CommonConstants.CHOREOGRAPHER_URI + EXECUTOR_SEARCH_MGMT_URI);
            }
            return choreographerDBService.getExecutorByServiceDefinitionAndMinMaxVersion(serviceDefinition, minVersion, maxVersion);
        } else if (version != null && minVersion == null && maxVersion == null) {
            return choreographerDBService.getExecutorByServiceDefinitionAndVersion(serviceDefinition, version);
        } else {
            throw new BadPayloadException(EXECUTOR_VERSION_MIN_VERSION_MAX_VERSION_AMBIGUOUS_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, CommonConstants.CHOREOGRAPHER_URI + EXECUTOR_SEARCH_MGMT_URI);
        }
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

    private ChoreographerExecutorResponseDTO callCreateExecutor(ChoreographerExecutorRequestDTO request) {
        logger.debug("callCreateExecutor started...");

        checkExecutorRequest(request, CommonConstants.CHOREOGRAPHER_URI + EXECUTOR_MGMT_URI);

        final String validatedName = request.getName();
        final String validatedAddress = request.getAddress();
        final int validatedPort = request.getPort();
        final String validatedBaseUri = request.getBaseUri();
        final String validatedServiceDefinitionName = request.getServiceDefinitionName();
        final int validatedVersion = request.getVersion();

        return choreographerDBService.createExecutorResponse(validatedName, validatedAddress, validatedPort, validatedBaseUri, validatedServiceDefinitionName, validatedVersion);
    }

    private void checkExecutorRequest(final ChoreographerExecutorRequestDTO request, final String origin) {
        logger.debug("checkExecutorRequest started...");

        if (request == null) {
            throw new BadPayloadException("Executor is null.", HttpStatus.SC_BAD_REQUEST, origin);
        }

        if (Utilities.isEmpty(request.getName())) {
            throw new BadPayloadException(EXECUTOR_NAME_NULL_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
        }

        if (Utilities.isEmpty(request.getAddress())) {
            throw new BadPayloadException(EXECUTOR_ADDRESS_NULL_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
        }

        if (request.getPort() == null) {
            throw new BadPayloadException(EXECUTOR_PORT_NULL_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
        }

        if (Utilities.isEmpty(request.getServiceDefinitionName())) {
            throw new BadPayloadException(EXECUTOR_SD_NULL_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
        }

        if (request.getVersion() == null) {
            throw new BadPayloadException(EXECUTOR_VERSION_NULL_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
        }

        final int validatedPort = request.getPort();
        if (validatedPort < CommonConstants.SYSTEM_PORT_RANGE_MIN || validatedPort > CommonConstants.SYSTEM_PORT_RANGE_MAX) {
            throw new BadPayloadException("Port must be between " + CommonConstants.SYSTEM_PORT_RANGE_MIN + " and " + CommonConstants.SYSTEM_PORT_RANGE_MAX + ".", HttpStatus.SC_BAD_REQUEST, origin);
        }
    }

    //-------------------------------------------------------------------------------------------------
    private void checkIfPlanHasEveryRequiredProvider (final ChoreographerRunPlanRequestDTO request, final String origin) {
        ChoreographerPlan plan = choreographerDBService.getPlanById(request.getId());

        Set<String> serviceDefinitionsFromPlan = getServiceDefinitionsFromPlan(plan);
        Set<String> serviceDefinitionsByProviders = new HashSet<>();

        for (ServiceRegistryResponseDTO dto : choreographerDriver.queryServiceRegistryByServiceDefinitionList(new ArrayList<>(serviceDefinitionsFromPlan)).getData()) {
            serviceDefinitionsByProviders.add(dto.getServiceDefinition().getServiceDefinition());
        }

        if (!serviceDefinitionsByProviders.equals(serviceDefinitionsFromPlan)) {
            throw new BadPayloadException(CHOREOGRAPHER_INSUFFICIENT_PROVIDERS_FOR_PLAN_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
        }
    }

    //-------------------------------------------------------------------------------------------------
    private void checkIfPlanHasEveryRequiredExecutor(final ChoreographerRunPlanRequestDTO request, final String origin) {
        ChoreographerPlan plan = choreographerDBService.getPlanById(request.getId());

        final Set<ChoreographerAction> actions = plan.getActions();
        for (ChoreographerAction action : actions) {
            final Set<ChoreographerStep> steps = action.getStepEntries();
            for (ChoreographerStep step : steps) {

                if (choreographerDBService.getSuitableExecutorIdsByStepId(step.getId()).getSuitableExecutorIds().isEmpty()) {
                    throw new BadPayloadException(CHOREOGRAPHER_INSUFFICIENT_EXECUTORS_FOR_PLAN_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
                }
            }
        }
    }

    //-------------------------------------------------------------------------------------------------
    private Set<String> getServiceDefinitionsFromPlan(ChoreographerPlan plan) {
        Set<String> serviceDefinitions = new HashSet<>();

        final Set<ChoreographerAction> actions = plan.getActions();
        for (ChoreographerAction action : actions) {
            final Set<ChoreographerStep> steps = action.getStepEntries();
            for (ChoreographerStep step : steps) {
                final Set<ChoreographerStepDetail> stepDetails = step.getStepDetails();
                for (ChoreographerStepDetail stepDetail : stepDetails) {
                    serviceDefinitions.add(stepDetail.getServiceDefinition().toLowerCase());
                }
            }
        }

        return serviceDefinitions;
    }

    //-------------------------------------------------------------------------------------------------
    private void checkUnregisterExecutorParameters(final String executorAddress, final int executorPort, final String executorBaseUri) {
        // parameters can't be null, but can be empty
        logger.debug("checkUnregisterExecutorParameters started...");

        final String origin = CommonConstants.CHOREOGRAPHER_URI + CommonConstants.OP_CHOREOGRAPHER_EXECUTOR_UNREGISTER;
        if (Utilities.isEmpty(executorAddress)) {
            throw new BadPayloadException("Executor address is blank.", HttpStatus.SC_BAD_REQUEST, origin);
        }

        if (Utilities.isEmpty(executorBaseUri)) {
            throw new BadPayloadException("The base URI of the Executor is blank.", HttpStatus.SC_BAD_REQUEST, origin);
        }

        if (executorPort < CommonConstants.SYSTEM_PORT_RANGE_MIN || executorPort > CommonConstants.SYSTEM_PORT_RANGE_MAX) {
            throw new BadPayloadException("Port must be between " + CommonConstants.SYSTEM_PORT_RANGE_MIN + " and " + CommonConstants.SYSTEM_PORT_RANGE_MAX + ".", HttpStatus.SC_BAD_REQUEST, origin);
        }
    }
}