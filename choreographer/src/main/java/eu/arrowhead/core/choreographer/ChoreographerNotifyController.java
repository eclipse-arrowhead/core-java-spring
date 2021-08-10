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

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.Defaults;
import eu.arrowhead.common.dto.shared.ChoreographerSessionRunningStepDataDTO;
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
public class ChoreographerNotifyController {

	//=================================================================================================
	// members

    private static final String STEP_DONE_HTTP_200_MESSAGE = "Choreographer notified that the running step is done.";
    private static final String STEP_DONE_HTTP_400_MESSAGE = "Could not notify Choreographer that the running step is done.";

    private final Logger logger = LogManager.getLogger(ChoreographerNotifyController.class);

    @Autowired
    private JmsTemplate jmsTemplate;
    
    //=================================================================================================
	// methods
 
    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = "Notify the Choreographer that a step is done in a session.", tags = { CoreCommonConstants.SWAGGER_TAG_CLIENT })
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_CREATED, message = STEP_DONE_HTTP_200_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = STEP_DONE_HTTP_400_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @PostMapping(path = CommonConstants.OP_CHOREOGRAPHER_NOTIFY_STEP_DONE, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = org.springframework.http.HttpStatus.OK)
    @ResponseBody public void notifyStepDone(@RequestBody final ChoreographerSessionRunningStepDataDTO request) {
        logger.debug("notifyStepDone started...");
        logger.debug("Sending message to session-step-done.");
        jmsTemplate.convertAndSend("session-step-done", request);
    }

    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = "Notify the Choreographer that an error happened during the execution of a step in a session.", tags = { CoreCommonConstants.SWAGGER_TAG_CLIENT })
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_CREATED, message = STEP_DONE_HTTP_200_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = STEP_DONE_HTTP_400_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @PostMapping(path = CommonConstants.OP_CHOREOGRAPHER_EXECUTOR_NOTIFY_STEP_ERROR, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = org.springframework.http.HttpStatus.OK)
    @ResponseBody public void notifyStepError(@RequestBody final ChoreographerSessionRunningStepDataDTO request) {
        logger.debug("notifyStepError started...");
        logger.debug("Sending message to session-step-error.");
        jmsTemplate.convertAndSend("session-step-error", request);
    }
}