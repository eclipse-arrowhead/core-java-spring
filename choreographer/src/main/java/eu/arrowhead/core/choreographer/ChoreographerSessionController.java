/********************************************************************************
 * Copyright (c) 2021 AITIA
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

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.CoreDefaults;
import eu.arrowhead.common.CoreUtilities;
import eu.arrowhead.common.CoreUtilities.ValidatedPageParams;
import eu.arrowhead.common.Defaults;
import eu.arrowhead.common.dto.shared.ChoreographerSessionListResponseDTO;
import eu.arrowhead.common.dto.shared.ChoreographerSessionStepListResponseDTO;
import eu.arrowhead.common.dto.shared.ChoreographerWorklogListResponseDTO;
import eu.arrowhead.core.choreographer.database.service.ChoreographerSessionDBService;
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
public class ChoreographerSessionController {
	
	//=================================================================================================
	// members
	
	@Autowired
	private ChoreographerSessionDBService sessionDBService;
	
	private static final String REQUEST_PARAM_PLAN_ID = "plan_id";
	private static final String REQUEST_PARAM_SESSION_ID = "session_id";
	private static final String REQUEST_PARAM_STATUS = "status";
	private static final String REQUEST_PARAM_PLAN_NAME = "plan_name";
	private static final String REQUEST_PARAM_ACTION_NAME = "action_name";
	private static final String REQUEST_PARAM_STEP_NAME = "step_name";
	
	private static final String GET_SESSION_HTTP_200_MESSAGE = "Sessions returned.";
    private static final String GET_SESSION_HTTP_400_MESSAGE = "Could not retrieve Sessions.";
    private static final String GET_SESSION_STEPS_HTTP_200_MESSAGE = "Session steps returned.";
    private static final String GET_SESSION_STEPS_HTTP_400_MESSAGE = "Could not retrieve sessions steps.";
    private static final String GET_WORKLOG_HTTP_200_MESSAGE = "Worklogs returned.";
    private static final String GET_WORKLOG_HTTP_400_MESSAGE = "Could not retrieve worklogs.";
    
	private final Logger logger = LogManager.getLogger(ChoreographerSessionController.class);	

	//=================================================================================================
	// methods
    
    //-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return requested session entries by the given parameters", response = ChoreographerSessionListResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = GET_SESSION_HTTP_200_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = GET_SESSION_HTTP_400_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
	@GetMapping(path = CommonConstants.CHOREOGRAPHER_SESSION_MGMT_URI, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public ChoreographerSessionListResponseDTO getSessions(@RequestParam(name = CoreCommonConstants.REQUEST_PARAM_PAGE, required = false) final Integer page,
															             @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_ITEM_PER_PAGE, required = false) final Integer size,
															             @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_DIRECTION, defaultValue = CoreDefaults.DEFAULT_REQUEST_PARAM_DIRECTION_VALUE) final String direction,
															             @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_SORT_FIELD, defaultValue = CoreCommonConstants.COMMON_FIELD_NAME_ID) final String sortField,
															             @RequestParam(name = REQUEST_PARAM_PLAN_ID, required = false) final Long planId,
															             @RequestParam(name = REQUEST_PARAM_STATUS, required = false) final String status) {
		logger.debug("getSessions started...");
		
		final ValidatedPageParams validatedPageParams = CoreUtilities.validatePageParameters(page, size, direction,
																							 CommonConstants.CHOREOGRAPHER_URI + CommonConstants.CHOREOGRAPHER_SESSION_MGMT_URI);
		return sessionDBService.getSessionsResponse(validatedPageParams.getValidatedPage(), validatedPageParams.getValidatedSize(), validatedPageParams.getValidatedDirection(),
													sortField, planId, status);
	}

	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return requested session step entries by the given parameters", response = ChoreographerSessionStepListResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = GET_SESSION_STEPS_HTTP_200_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = GET_SESSION_STEPS_HTTP_400_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
	@GetMapping(path = CommonConstants.CHOREOGRAPHER_SESSION_STEPS_MGMT_URI, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public ChoreographerSessionStepListResponseDTO getSessionSteps(@RequestParam(name = CoreCommonConstants.REQUEST_PARAM_PAGE, required = false) final Integer page,
																	             @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_ITEM_PER_PAGE, required = false) final Integer size,
																	             @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_DIRECTION, defaultValue = CoreDefaults.DEFAULT_REQUEST_PARAM_DIRECTION_VALUE) final String direction,
																	             @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_SORT_FIELD, defaultValue = CoreCommonConstants.COMMON_FIELD_NAME_ID) final String sortField,
																	             @RequestParam(name = REQUEST_PARAM_SESSION_ID, required = false) final Long sessionId,
																	             @RequestParam(name = REQUEST_PARAM_STATUS, required = false) final String status) {
		logger.debug("getSessionSteps started...");
		
		final ValidatedPageParams validatedPageParams = CoreUtilities.validatePageParameters(page, size, direction,
				 																			 CommonConstants.CHOREOGRAPHER_URI + CommonConstants.CHOREOGRAPHER_SESSION_STEPS_MGMT_URI);
		return sessionDBService.getSessionStepsResponse(validatedPageParams.getValidatedPage(), validatedPageParams.getValidatedSize(), validatedPageParams.getValidatedDirection(),
														sortField, sessionId, status);
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return requested worklog entries by the given parameters", response = ChoreographerWorklogListResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = GET_WORKLOG_HTTP_200_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = GET_WORKLOG_HTTP_400_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
	@GetMapping(path = CommonConstants.CHOREOGRAPHER_WORKLOG_MGMT_URI, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public ChoreographerWorklogListResponseDTO getWorklog(@RequestParam(name = CoreCommonConstants.REQUEST_PARAM_PAGE, required = false) final Integer page,
															            @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_ITEM_PER_PAGE, required = false) final Integer size,
															            @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_DIRECTION, defaultValue = CoreDefaults.DEFAULT_REQUEST_PARAM_DIRECTION_VALUE) final String direction,
															            @RequestParam(name = CoreCommonConstants.REQUEST_PARAM_SORT_FIELD, defaultValue = CoreCommonConstants.COMMON_FIELD_NAME_ID) final String sortField,
															            @RequestParam(name = REQUEST_PARAM_SESSION_ID, required = false) final Long sessionId,
															            @RequestParam(name = REQUEST_PARAM_PLAN_NAME, required = false) final String planName,
															            @RequestParam(name = REQUEST_PARAM_ACTION_NAME, required = false) final String actionName,
															            @RequestParam(name = REQUEST_PARAM_STEP_NAME, required = false) final String stepName) {
		logger.debug("getWorklog started...");
		
		final ValidatedPageParams validatedPageParams = CoreUtilities.validatePageParameters(page, size, direction,
				 																			 CommonConstants.CHOREOGRAPHER_URI + CommonConstants.CHOREOGRAPHER_WORKLOG_MGMT_URI);
		return sessionDBService.getWorklogsResponse(validatedPageParams.getValidatedPage(), validatedPageParams.getValidatedSize(), validatedPageParams.getValidatedDirection(), sortField,
													sessionId, planName, actionName, stepName);
	}
}