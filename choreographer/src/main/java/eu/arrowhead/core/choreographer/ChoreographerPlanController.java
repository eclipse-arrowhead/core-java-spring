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


import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.logging.LogLevel;
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
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.core.CoreSystem;
import eu.arrowhead.common.database.entity.ChoreographerPlan;
import eu.arrowhead.common.database.entity.ChoreographerSession;
import eu.arrowhead.common.database.entity.Logs;
import eu.arrowhead.common.database.service.CommonDBService;
import eu.arrowhead.common.dto.internal.ChoreographerStartSessionDTO;
import eu.arrowhead.common.dto.internal.LogEntryListResponseDTO;
import eu.arrowhead.common.dto.shared.ChoreographerCheckPlanResponseDTO;
import eu.arrowhead.common.dto.shared.ChoreographerPlanListResponseDTO;
import eu.arrowhead.common.dto.shared.ChoreographerPlanRequestDTO;
import eu.arrowhead.common.dto.shared.ChoreographerPlanResponseDTO;
import eu.arrowhead.common.dto.shared.ChoreographerRunPlanRequestByClientDTO;
import eu.arrowhead.common.dto.shared.ChoreographerRunPlanRequestDTO;
import eu.arrowhead.common.dto.shared.ChoreographerRunPlanResponseDTO;
import eu.arrowhead.common.dto.shared.ChoreographerSessionStatus;
import eu.arrowhead.common.dto.shared.ErrorMessageDTO;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.core.choreographer.database.service.ChoreographerPlanDBService;
import eu.arrowhead.core.choreographer.database.service.ChoreographerSessionDBService;
import eu.arrowhead.core.choreographer.service.ChoreographerService;
import eu.arrowhead.core.choreographer.validation.ChoreographerPlanExecutionChecker;
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

	private static final String FALSE = "false";
    private static final String REQUEST_PARRAM_ALLOW_INTER_CLOUD = "allowInterCloud";
    private static final String PATH_VARIABLE_ID = "id";
    private static final int DEFAULT_SESSION_QUANTITY = 1;
    private static final String ID_NOT_VALID_ERROR_MESSAGE = "ID must be greater than 0.";
    private static final String MANUAL_ABORT_MESSAGE = "Manual abort";

    private static final String PLAN_MGMT_URI = CoreCommonConstants.MGMT_URI + "/plan";
    private static final String PLAN_MGMT_BY_ID_URI = PLAN_MGMT_URI + "/{" + PATH_VARIABLE_ID + "}";
    private static final String SESSION_MGMT_URI = CoreCommonConstants.MGMT_URI + "/session";
    private static final String START_SESSION_MGMT_URI = SESSION_MGMT_URI + "/start";
    private static final String ABORT_SESSION_MGMT_BY_ID_URI = SESSION_MGMT_URI + "/abort" + "/{" + PATH_VARIABLE_ID + "}";
    private static final String CHECK_PLAN_MGMT_BY_ID_URI = CoreCommonConstants.MGMT_URI + "/check-plan/{" + PATH_VARIABLE_ID + "}";

    private static final String GET_PlAN_MGMT_HTTP_200_MESSAGE = "Plan returned.";
    private static final String GET_PLAN_MGMT_HTTP_400_MESSAGE = "Could not retrieve plan.";

    private static final String GET_CHECK_PLAN_MGMT_HTTP_200_MESSAGE = "Check report returned.";
    private static final String GET_CHECK_PLAN_MGMT_HTTP_400_MESSAGE = "Could not retrieve check report.";

    private static final String POST_PLAN_MGMT_HTTP_201_MESSAGE = "Plan created with given service definition and first Action.";
    private static final String POST_PLAN_MGMT_HTTP_400_MESSAGE = "Could not create Plan.";

    private static final String DELETE_PLAN_HTTP_200_MESSAGE = "Plan successfully removed.";
    private static final String DELETE_PLAN_HTTP_400_MESSAGE = "Could not remove Plan.";

    private static final String START_SESSION_HTTP_200_MESSAGE = "Initiated plan execution with given id(s).";
    private static final String START_SESSION_BY_CLIENT_HTTP_200_MESSAGE = "Initiated plan execution with given id(s) or name(s).";
    private static final String START_PLAN_HTTP_400_MESSAGE = "Could not start plan.";
    
    private static final String ABORT_SESSION_HTTP_200_MESSAGE = "Initiated session abortion with given id.";
    private static final String ABORT_SESSION_HTTP_400_MESSAGE = "Could not abort session with given id.";

    private final Logger logger = LogManager.getLogger(ChoreographerPlanController.class);

    @Autowired
    private ChoreographerPlanDBService planDBService;
    
    @Autowired
    private ChoreographerSessionDBService sessionDBService;
    
    @Autowired
    private ChoreographerPlanValidator planValidator;
    
    @Autowired
    private CommonDBService commonDBService;
    

    @Autowired
    private ChoreographerPlanExecutionChecker planChecker;
    
    @Autowired
    private ChoreographerService choreographerService;

    @Autowired
    private JmsTemplate jms;
    
	@Value(CoreCommonConstants.$CHOREOGRAPHER_IS_GATEKEEPER_PRESENT_WD)
	private boolean gatekeeperIsPresent;
    
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
	@ApiOperation(value = "Return requested log entries by the given parameters", response = LogEntryListResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = CoreCommonConstants.QUERY_LOG_ENTRIES_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = CoreCommonConstants.QUERY_LOG_ENTRIES_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path = CoreCommonConstants.OP_QUERY_LOG_ENTRIES, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public LogEntryListResponseDTO getLogEntries(
			@RequestParam(name = CoreCommonConstants.REQUEST_PARAM_PAGE, required = false) final Integer page,
			@RequestParam(name = CoreCommonConstants.REQUEST_PARAM_ITEM_PER_PAGE, required = false) final Integer size,
			@RequestParam(name = CoreCommonConstants.REQUEST_PARAM_DIRECTION, defaultValue = CoreDefaults.DEFAULT_REQUEST_PARAM_DIRECTION_VALUE) final String direction,
			@RequestParam(name = CoreCommonConstants.REQUEST_PARAM_SORT_FIELD, defaultValue = Logs.FIELD_NAME_ID) final String sortField,
			@RequestParam(name = CoreCommonConstants.REQUEST_PARAM_LOG_LEVEL, required = false) final String logLevel,
			@RequestParam(name = CoreCommonConstants.REQUEST_PARAM_FROM, required = false) final String from,
			@RequestParam(name = CoreCommonConstants.REQUEST_PARAM_TO, required = false) final String to,
			@RequestParam(name = CoreCommonConstants.REQUEST_PARAM_LOGGER, required = false) final String loggerStr) { 
		logger.debug("New getLogEntries GET request received with page: {} and item_per page: {}", page, size);
				
		final String origin = CommonConstants.CHOREOGRAPHER_URI + CoreCommonConstants.OP_QUERY_LOG_ENTRIES;
		final ValidatedPageParams validParameters = CoreUtilities.validatePageParameters(page, size, direction, origin);
		final List<LogLevel> logLevels = CoreUtilities.getLogLevels(logLevel, origin);
		
		try {
			final ZonedDateTime _from = Utilities.parseUTCStringToLocalZonedDateTime(from);
			final ZonedDateTime _to = Utilities.parseUTCStringToLocalZonedDateTime(to);
			
			if (_from != null && _to != null && _to.isBefore(_from)) {
				throw new BadPayloadException("Invalid time interval", HttpStatus.SC_BAD_REQUEST, origin);
			}

			final LogEntryListResponseDTO response = commonDBService.getLogEntriesResponse(validParameters.getValidatedPage(), validParameters.getValidatedSize(), validParameters.getValidatedDirection(), sortField, CoreSystem.CHOREOGRAPHER, 
																						   logLevels, _from, _to, loggerStr);
			
			logger.debug("Log entries  with page: {} and item_per page: {} retrieved successfully", page, size);
			return response;
		} catch (final DateTimeParseException ex) {
			throw new BadPayloadException("Invalid time parameter", HttpStatus.SC_BAD_REQUEST, origin, ex);
		}
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
        final ChoreographerPlanListResponseDTO planEntriesResponse = planDBService.getPlanEntriesResponse(validatedPageParams.getValidatedPage(),
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
        logger.debug("New Plan GET request received with id: {}.", id);

        if (id < 1) {
            throw new BadPayloadException(ID_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, CommonConstants.CHOREOGRAPHER_URI + PLAN_MGMT_BY_ID_URI);
        }

        final ChoreographerPlanResponseDTO planEntryResponse = planDBService.getPlanByIdResponse(id);
        logger.debug("Plan entry with id: {} successfully retrieved!", id);

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
        logger.debug("New Plan delete request received with id of {}.", id);

        if (id < 1) {
            throw new BadPayloadException(ID_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, CommonConstants.CHOREOGRAPHER_URI + PLAN_MGMT_BY_ID_URI);
        }

        planDBService.removePlanEntryById(id);
        logger.debug("Plan with id: {} successfully deleted!", id);
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

        return planDBService.createPlanResponse(validatedPlan);
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = "Initiate the start of one or more plans.",
            tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = START_SESSION_HTTP_200_MESSAGE, responseContainer = "List", response = ChoreographerRunPlanResponseDTO.class),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = START_PLAN_HTTP_400_MESSAGE, response= ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE, response= ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE, response= ErrorMessageDTO.class)
    })
    @PostMapping(path = START_SESSION_MGMT_URI, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody public List<ChoreographerRunPlanResponseDTO> startPlans(@RequestBody final List<ChoreographerRunPlanRequestDTO> requests) { 
    	logger.debug("startPlans started...");
    	
    	if (requests == null || requests.isEmpty()) {
    		throw new BadPayloadException("No plan specified to start.", HttpStatus.SC_BAD_REQUEST, CommonConstants.CHOREOGRAPHER_URI + START_SESSION_MGMT_URI);
    	}
    	
    	final List<ChoreographerRunPlanResponseDTO> results = new ArrayList<>(requests.size());
        for (final ChoreographerRunPlanRequestDTO request : requests) {
        	request.setAllowInterCloud(gatekeeperIsPresent && request.isAllowInterCloud()); // change inter-cloud flag based on gatekeeper presence in the cloud 
        	final ChoreographerRunPlanResponseDTO response = planChecker.checkPlanForExecution(request);
           
        	if (!Utilities.isEmpty(response.getErrorMessages())) {
        		results.add(response);
        	} else {
        		final ChoreographerSession session = sessionDBService.initiateSession(request.getPlanId(), request.getQuantity(), createNotifyUri(request));
        		results.add(new ChoreographerRunPlanResponseDTO(request.getPlanId(), session.getId(), session.getQuantityGoal(), response.getNeedInterCloud()));
			   
        		logger.debug("Sending a message to {}.", ChoreographerService.START_SESSION_DESTINATION);
        		jms.convertAndSend(ChoreographerService.START_SESSION_DESTINATION, new ChoreographerStartSessionDTO(session.getId(), request.getPlanId(), request.isAllowInterCloud(), request.getChooseOptimalExecutor()));
        	}
        }
           
        return results;
    }
    
    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = "Initiate the abortion of the specified session.", response = Void.class, tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
    @ApiResponses (value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = ABORT_SESSION_HTTP_200_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = ABORT_SESSION_HTTP_400_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @DeleteMapping(path = ABORT_SESSION_MGMT_BY_ID_URI)
    public void abortSession(@PathVariable final Long id) {
    	logger.debug("New abort session request received with id: {}.", id);
    	
    	if (id < 1) {
            throw new BadPayloadException(ID_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, CommonConstants.CHOREOGRAPHER_URI + ABORT_SESSION_MGMT_BY_ID_URI);
        }
    	
    	final ChoreographerSession session = sessionDBService.getSessionById(id);
    	if (session.getStatus() == ChoreographerSessionStatus.DONE) {
			throw new BadPayloadException("Session with id " + id + " couldn't be aborted due to its DONE status");
		}
    	
    	choreographerService.abortSession(id, null, MANUAL_ABORT_MESSAGE);
    }
    
    //-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return the check report of the specified plan.", response = ChoreographerCheckPlanResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
    @ApiResponses (value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = GET_CHECK_PLAN_MGMT_HTTP_200_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = GET_CHECK_PLAN_MGMT_HTTP_400_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @GetMapping(path = CHECK_PLAN_MGMT_BY_ID_URI, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody public ChoreographerCheckPlanResponseDTO checkPlan(@PathVariable(value = PATH_VARIABLE_ID) final long id,
            														 @RequestParam(name = REQUEST_PARRAM_ALLOW_INTER_CLOUD, defaultValue = FALSE) final boolean allowIntercloud) { 
        logger.debug("New check plan GET request received with id: {}.", id);

        if (id < 1) {
            throw new BadPayloadException(ID_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, CommonConstants.CHOREOGRAPHER_URI + CHECK_PLAN_MGMT_BY_ID_URI);
        }

        final ChoreographerRunPlanResponseDTO result = planChecker.checkPlanForExecution(gatekeeperIsPresent && allowIntercloud, id, DEFAULT_SESSION_QUANTITY);
        logger.debug("Check report for plan with id: {} successfully retrieved!", id);

        return new ChoreographerCheckPlanResponseDTO(id, result.getErrorMessages(), result.getNeedInterCloud());
    }
	
	//-------------------------------------------------------------------------------------------------
    @ApiOperation(value = "Initiate the start of one or more plans.", tags = { CoreCommonConstants.SWAGGER_TAG_CLIENT })
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = START_SESSION_BY_CLIENT_HTTP_200_MESSAGE, responseContainer = "List", response = ChoreographerRunPlanResponseDTO.class),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = START_PLAN_HTTP_400_MESSAGE, response= ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE, response= ErrorMessageDTO.class),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE, response= ErrorMessageDTO.class)
    })
    @PostMapping(path = CommonConstants.OP_CHOREOGRAPHER_CLIENT_SERVICE_SESSION_START_URI, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody public List<ChoreographerRunPlanResponseDTO> startPlansByClient(@RequestBody final List<ChoreographerRunPlanRequestByClientDTO> requests) { 
    	logger.debug("startPlans started...");
    	
    	if (requests == null || requests.isEmpty()) {
    		throw new BadPayloadException("No plan specified to start.", HttpStatus.SC_BAD_REQUEST, CommonConstants.CHOREOGRAPHER_URI + CommonConstants.OP_CHOREOGRAPHER_CLIENT_SERVICE_SESSION_START_URI);
    	}
    	
    	final List<ChoreographerRunPlanResponseDTO> results = new ArrayList<>(requests.size());
        for (final ChoreographerRunPlanRequestByClientDTO request : requests) {
        	request.setAllowInterCloud(gatekeeperIsPresent && request.isAllowInterCloud()); // change inter-cloud flag based on gatekeeper presence in the cloud
        	if (request.getPlanId() == null) {
        		request.setPlanId(findPlan(request.getName())); // try to find plan id by name
        	}
        	
        	final ChoreographerRunPlanResponseDTO response = planChecker.checkPlanForExecution(request);
           
        	if (!Utilities.isEmpty(response.getErrorMessages())) {
        		results.add(response);
        	} else {
        		final ChoreographerSession session = sessionDBService.initiateSession(request.getPlanId(), request.getQuantity(), createNotifyUri(request));
        		results.add(new ChoreographerRunPlanResponseDTO(request.getPlanId(), session.getId(), session.getQuantityGoal(), response.getNeedInterCloud()));
			   
        		logger.debug("Sending a message to {}.", ChoreographerService.START_SESSION_DESTINATION);
        		jms.convertAndSend(ChoreographerService.START_SESSION_DESTINATION, new ChoreographerStartSessionDTO(session.getId(), request.getPlanId(), request.isAllowInterCloud(), request.getChooseOptimalExecutor()));
        	}
        }
           
        return results;
    }
	
	//-------------------------------------------------------------------------------------------------
    @ApiOperation(value = "Initiate the abortion of the specified session.", response = Void.class, tags = { CoreCommonConstants.SWAGGER_TAG_CLIENT })
    @ApiResponses (value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = ABORT_SESSION_HTTP_200_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = ABORT_SESSION_HTTP_400_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @DeleteMapping(path = CommonConstants.OP_CHOREOGRAPHER_CLIENT_SERVICE_SESSION_ABORT_URI)
    public void abortSessionByClient(@PathVariable final Long id) {
    	logger.debug("New abort session request received with id: {}.", id);
    	
    	if (id < 1) {
            throw new BadPayloadException(ID_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, CommonConstants.CHOREOGRAPHER_URI + CommonConstants.OP_CHOREOGRAPHER_CLIENT_SERVICE_SESSION_ABORT_URI);
        }
    	
    	final ChoreographerSession session = sessionDBService.getSessionById(id);
    	if (session.getStatus() == ChoreographerSessionStatus.DONE) {
			throw new BadPayloadException("Session with id " + id + " couldn't be aborted due to its DONE status");
		}
    	
    	choreographerService.abortSession(id, null, MANUAL_ABORT_MESSAGE);
    }

    //=================================================================================================
	// assistant methods
    
    //-------------------------------------------------------------------------------------------------
	private String createNotifyUri(final ChoreographerRunPlanRequestDTO request) {
		return Utilities.isEmpty(request.getNotifyAddress()) ? null
															 : request.getNotifyProtocol() + "://" + request.getNotifyAddress() + ":" + request.getNotifyPort() + "/" + request.getNotifyPath();
	}
	
	//-------------------------------------------------------------------------------------------------
	private Long findPlan(final String name) {
		if (Utilities.isEmpty(name)) {
			return null;
		}
		
		final Optional<ChoreographerPlan> plan = planDBService.getPlanByName(name);
		
		return plan.isPresent() ? plan.get().getId() : null;
	}
}