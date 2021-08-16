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
import org.springframework.web.bind.annotation.RestController;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.Defaults;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.shared.ChoreographerExecutedStepResultDTO;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.core.choreographer.service.ChoreographerService;
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
    private JmsTemplate jms;
    
    //=================================================================================================
	// methods
 
    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = "Notify the Choreographer that a step is done in a session.", tags = { CoreCommonConstants.SWAGGER_TAG_CLIENT })
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = STEP_DONE_HTTP_200_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = STEP_DONE_HTTP_400_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @PostMapping(path = CommonConstants.OP_CHOREOGRAPHER_NOTIFY_STEP_DONE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody public void notifyStepDone(@RequestBody final ChoreographerExecutedStepResultDTO payload) {
        logger.debug("notifyStepDone started...");
        
        validate(payload);
        
        logger.debug("Sending message to {}.", ChoreographerService.SESSION_STEP_DONE_DESTINATION);
        jms.convertAndSend(ChoreographerService.SESSION_STEP_DONE_DESTINATION, payload);
    }
    
    //=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	private void validate(final ChoreographerExecutedStepResultDTO payload) {
		logger.debug("validate started...");
		
		final String origin = CommonConstants.CHOREOGRAPHER_URI + CommonConstants.OP_CHOREOGRAPHER_NOTIFY_STEP_DONE;
		
		if (payload == null) {
			throw new BadPayloadException("Payload is null.", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		if (payload.getSessionId() == null || payload.getSessionId() <= 0) {
			throw new BadPayloadException("Invalid session id.", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		if (payload.getSessionStepId() == null || payload.getSessionStepId() <= 0) {
			throw new BadPayloadException("Invalid session step id.", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		if (payload.getStatus() == null) {
			throw new BadPayloadException("Missing status.", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		if (payload.getStatus().isError() && Utilities.isEmpty(payload.getMessage())) {
			throw new BadPayloadException("Message is null or blank.", HttpStatus.SC_BAD_REQUEST, origin);
		}
	}
}