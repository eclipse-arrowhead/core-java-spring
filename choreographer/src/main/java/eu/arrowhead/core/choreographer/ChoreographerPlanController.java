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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
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

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.CoreDefaults;
import eu.arrowhead.common.CoreUtilities;
import eu.arrowhead.common.CoreUtilities.ValidatedPageParams;
import eu.arrowhead.common.Defaults;
import eu.arrowhead.common.database.entity.ChoreographerPlan;
import eu.arrowhead.common.database.entity.ChoreographerSession;
import eu.arrowhead.common.dto.internal.ChoreographerRunPlanRequestDTO;
import eu.arrowhead.common.dto.internal.ChoreographerStartSessionDTO;
import eu.arrowhead.common.dto.shared.ChoreographerPlanListResponseDTO;
import eu.arrowhead.common.dto.shared.ChoreographerPlanRequestDTO;
import eu.arrowhead.common.dto.shared.ChoreographerPlanResponseDTO;
import eu.arrowhead.common.dto.shared.ServiceRegistryResponseDTO;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.core.choreographer.database.service.ChoreographerDBService;
import eu.arrowhead.core.choreographer.service.ChoreographerDriver;
import eu.arrowhead.core.choreographer.validation.ChoreographerPlanValidator;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(tags = { CoreCommonConstants.SWAGGER_TAG_ALL })
@CrossOrigin(maxAge = Defaults.CORS_MAX_AGE, allowCredentials = Defaults.CORS_ALLOW_CREDENTIALS,
			 allowedHeaders = { HttpHeaders.ORIGIN, HttpHeaders.CONTENT_TYPE, HttpHeaders.ACCEPT, HttpHeaders.AUTHORIZATION }
)
@RestController
@RequestMapping(CommonConstants.CHOREOGRAPHER_URI)
public class ChoreographerPlanController {

	//=================================================================================================
	// members

    private static final String PATH_VARIABLE_ID = "id";
    private static final String ID_NOT_VALID_ERROR_MESSAGE = "ID must be greater than 0.";

    private static final String PLAN_MGMT_URI = CoreCommonConstants.MGMT_URI + "/plan";
    private static final String PLAN_MGMT_BY_ID_URI = PLAN_MGMT_URI + "/{" + PATH_VARIABLE_ID + "}";
    private static final String SESSION_MGMT_URI = CoreCommonConstants.MGMT_URI + "/session";

    private static final String START_SESSION_MGMT_URI = SESSION_MGMT_URI + "/start";

    private static final String GET_PlAN_MGMT_HTTP_200_MESSAGE = "Plan returned.";
    private static final String GET_PLAN_MGMT_HTTP_400_MESSAGE = "Could not retrieve plan.";

    private static final String POST_PLAN_MGMT_HTTP_201_MESSAGE = "Plan created with given service definition and first Action.";
    private static final String POST_PLAN_MGMT_HTTP_400_MESSAGE = "Could not create Plan.";

    private static final String DELETE_PLAN_HTTP_200_MESSAGE = "Plan successfully removed.";
    private static final String DELETE_PLAN_HTTP_400_MESSAGE = "Could not remove Plan.";

    private static final String START_SESSION_HTTP_200_MESSAGE = "Initiated plan execution with given id(s).";
    private static final String START_PLAN_HTTP_400_MESSAGE = "Could not start plan with given id(s).";

    private static final String CHOREOGRAPHER_INSUFFICIENT_PROVIDERS_FOR_PLAN_ERROR_MESSAGE = "Can't start plan because not every service definition has a corresponding provider.";
    private static final String CHOREOGRAPHER_INSUFFICIENT_EXECUTORS_FOR_PLAN_ERROR_MESSAGE = "Can't start plan because not every service definition has a corresponding executor.";

    private final Logger logger = LogManager.getLogger(ChoreographerPlanController.class);

    @Autowired
    private ChoreographerDBService choreographerDBService;
    
    @Autowired
    private ChoreographerPlanValidator planValidator;

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
	@ApiOperation(value = "Return requested Plan entries by the given parameters.", response = ChoreographerPlanListResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
    @ApiResponses (value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = GET_PlAN_MGMT_HTTP_200_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = GET_PLAN_MGMT_HTTP_400_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @GetMapping(path = PLAN_MGMT_URI, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody public ChoreographerPlanListResponseDTO getPlans(
            @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_PAGE, required = false) final Integer page,
            @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_ITEM_PER_PAGE, required = false) final Integer size,
            @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_DIRECTION, defaultValue = CoreDefaults.DEFAULT_REQUEST_PARAM_DIRECTION_VALUE) final String direction,
            @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_SORT_FIELD, defaultValue = CoreCommonConstants.COMMON_FIELD_NAME_ID) final String sortField) {
        logger.debug("New Plan GET request received with page: {} and item_per_page: {}.", page, size);

        final ValidatedPageParams validatedPageParams = CoreUtilities.validatePageParameters(page, size, direction, sortField);
        final ChoreographerPlanListResponseDTO planEntriesResponse = choreographerDBService.getPlanEntriesResponse(validatedPageParams.getValidatedPage(),
        																										   validatedPageParams.getValidatedSize(),
        																										   validatedPageParams.getValidatedDirection(),
        																										   sortField);
        logger.debug("Plan with page: {} and item_per_page: {} retrieved successfully", page, size);

        return planEntriesResponse;
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
        logger.debug("New Plan GET request received with id: " + id + ".");

        if (id < 1) {
            throw new BadPayloadException(ID_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, CommonConstants.CHOREOGRAPHER_URI + PLAN_MGMT_BY_ID_URI);
        }

        final ChoreographerPlanResponseDTO planEntryResponse = choreographerDBService.getPlanByIdResponse(id);
        logger.debug("Plan entry with id: " + " successfully retrieved!");

        return planEntryResponse;
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
        final ChoreographerPlanRequestDTO validatedPlan = planValidator.validatePlan(request, CommonConstants.CHOREOGRAPHER_URI + PLAN_MGMT_URI);

        return choreographerDBService.createPlanResponse(validatedPlan);
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
    // TODO: instead of this, we need a WS to verify if a plan is executable (find suitable executor to all steps)
//    @ApiOperation(value = "Return the ids of the suitable Executors entries by step id.", response = ServiceRegistryResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
//    @ApiResponses(value = {
//            @ApiResponse(code = HttpStatus.SC_OK, message = GET_EXECUTOR_HTTP_200_MESSAGE),
//            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = GET_EXECUTOR_HTTP_400_MESSAGE),
//            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
//            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
//    })
//    @GetMapping(path = EXECUTOR_MGMT_TEST_BY_STEP_ID)
//    @ResponseBody public ChoreographerSuitableExecutorResponseDTO getSuitableExecutorIds(@PathVariable(value = PATH_VARIABLE_ID) final long id) {
//        logger.debug("getSuitableExecutorIds started...");
//
//        if (id < 1) {
//            throw new BadPayloadException(ID_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, CommonConstants.CHOREOGRAPHER_URI + EXECUTOR_MGMT_BY_ID_URI);
//        }
//
//        return choreographerDBService.getSuitableExecutorIdsByStepId(id);
//    }

    //=================================================================================================
	// assistant methods

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
    //TODO: fix this
    private void checkIfPlanHasEveryRequiredExecutor(final ChoreographerRunPlanRequestDTO request, final String origin) {
//        ChoreographerPlan plan = choreographerDBService.getPlanById(request.getId());
//
//        final Set<ChoreographerAction> actions = plan.getActions();
//        for (ChoreographerAction action : actions) {
//            final Set<ChoreographerStep> steps = action.getStepEntries();
//            for (ChoreographerStep step : steps) {
//
//                if (choreographerDBService.getSuitableExecutorIdsByStepId(step.getId()).getSuitableExecutorIds().isEmpty()) {
//                    throw new BadPayloadException(CHOREOGRAPHER_INSUFFICIENT_EXECUTORS_FOR_PLAN_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
//                }
//            }
//        }
    }

    //-------------------------------------------------------------------------------------------------
    // TODO: fix this
    private Set<String> getServiceDefinitionsFromPlan(ChoreographerPlan plan) {
//        Set<String> serviceDefinitions = new HashSet<>();
//
//        final Set<ChoreographerAction> actions = plan.getActions();
//        for (ChoreographerAction action : actions) {
//            final Set<ChoreographerStep> steps = action.getStepEntries();
//            for (ChoreographerStep step : steps) {
//                final Set<ChoreographerStepDetail> stepDetails = step.getStepDetails();
//                for (ChoreographerStepDetail stepDetail : stepDetails) {
//                    serviceDefinitions.add(stepDetail.getServiceDefinition().toLowerCase());
//                }
//            }
//        }
//
//        return serviceDefinitions;
    	return null;
    }
}