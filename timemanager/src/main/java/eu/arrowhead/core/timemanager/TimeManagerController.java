/********************************************************************************
 * Copyright (c) 2021 {Lulea University of Technology}
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 
 *
 * Contributors: 
 *   {Lulea University of Technology} - implementation
 *   Arrowhead Consortia - conceptualization 
 ********************************************************************************/
package eu.arrowhead.core.timemanager;

import java.util.List;
import java.util.ArrayList;
import java.util.Vector;
import java.util.Iterator;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.dto.shared.TimeManagerTimeResponseDTO;
import eu.arrowhead.core.timemanager.service.TimeManagerDriver;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;


@Api(tags = { CoreCommonConstants.SWAGGER_TAG_ALL })
@RestController
@RequestMapping(CommonConstants.TIMEMANAGER_URI)
public class TimeManagerController {

	private static final String TIME_TZ = "${time.timezone}";

	private final Logger logger = LogManager.getLogger(TimeManagerController.class);

	@Value(TIME_TZ)
    private String serverTimeZone;

	//=================================================================================================
	// members
	
	private static final String OP_NOT_VALID_ERROR_MESSAGE = " Illegal operation. ";
	private static final String NOT_FOUND_ERROR_MESSAGE = " Resource not found. ";
	
	@Autowired
	private TimeManagerDriver timeManagerDriver;

	//@Autowired
	//private TimeManagerDBService timeManagerDBService;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return an echo message with the purpose of testing the core service availability", response = String.class, tags = { CoreCommonConstants.SWAGGER_TAG_CLIENT })
	@ApiResponses (value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = CoreCommonConstants.SWAGGER_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE)
	})
	@GetMapping(path = CommonConstants.ECHO_URI)
	@ResponseBody public String echoService() {
		return "Got it!";
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Interface to the Time service", response = TimeManagerTimeResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_CLIENT })
	@ApiResponses (value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = CoreCommonConstants.SWAGGER_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path= CommonConstants.OP_TIMEMANAGER_TIME, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public TimeManagerTimeResponseDTO timeGet(
			) {
		logger.debug("timeGet");

        TimeManagerTimeResponseDTO resp = new TimeManagerTimeResponseDTO(serverTimeZone, timeManagerDriver.isTimeTrusted());
		return resp;
	}

	
	//=================================================================================================
	// assistant methods
	
	
	//=================================================================================================

}
