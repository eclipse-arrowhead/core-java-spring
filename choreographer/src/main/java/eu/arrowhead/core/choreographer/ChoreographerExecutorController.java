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

import javax.servlet.http.HttpServletRequest;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
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
import eu.arrowhead.common.Defaults;
import eu.arrowhead.common.dto.shared.ChoreographerExecutorRequestDTO;
import eu.arrowhead.common.dto.shared.ChoreographerExecutorResponseDTO;
import eu.arrowhead.core.choreographer.service.ChoreographerExecutorService;
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
public class ChoreographerExecutorController {
	
	//=================================================================================================
	// members
	
	@Autowired
	private ChoreographerExecutorService executorService;
	
	private static final String POST_EXECUTOR_HTTP_201_MESSAGE = "Executor created.";
    private static final String POST_EXECUTOR_HTTP_400_MESSAGE = "Could not create executor.";
    private static final String DELETE_EXECUTOR_HTTP_200_MESSAGE = "Executor successfully removed.";
    private static final String DELETE_EXECUTOR_HTTP_400_MESSAGE = "Could not remove Executor.";
    
    private final Logger logger = LogManager.getLogger(ChoreographerExecutorController.class);
    
    //=================================================================================================
	// methods
    
    //-------------------------------------------------------------------------------------------------
    @ApiOperation(value = "Return created executor.", response = ChoreographerExecutorResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_CREATED, message = POST_EXECUTOR_HTTP_201_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = POST_EXECUTOR_HTTP_400_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @PostMapping(path = CommonConstants.CHOREOGRAPHER_EXECUTOR_MGMT_URI, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
    @ResponseBody public ChoreographerExecutorResponseDTO addExecutor(@RequestBody final ChoreographerExecutorRequestDTO request) {
    	logger.debug("addExecutor started...");
    	return executorService.addExecutorSystem(request, CommonConstants.CHOREOGRAPHER_URI + CommonConstants.CHOREOGRAPHER_EXECUTOR_MGMT_URI);
    }

	//-------------------------------------------------------------------------------------------------
    @ApiOperation(value = "Remove executor.", tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = DELETE_EXECUTOR_HTTP_200_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = DELETE_EXECUTOR_HTTP_400_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @DeleteMapping(CommonConstants.CHOREOGRAPHER_EXECUTOR_MGMT_BY_ID_URI)
    public void removeExecutor(@PathVariable final long id) {
        logger.debug("New Executor delete request received with id: {}", id);
        executorService.removeExecutorSystem(id, CommonConstants.CHOREOGRAPHER_URI + CommonConstants.CHOREOGRAPHER_EXECUTOR_MGMT_BY_ID_URI);
        logger.debug("Executor with id: '{}' successfully deleted", id);
    }
    
	//TODO getExecutors
	//TODO getExecutorById
	
	//-------------------------------------------------------------------------------------------------
    @ApiOperation(value = "Return registered executor.", response = ChoreographerExecutorResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_CLIENT })
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_CREATED, message = POST_EXECUTOR_HTTP_201_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = POST_EXECUTOR_HTTP_400_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @PostMapping(path = CommonConstants.OP_CHOREOGRAPHER_EXECUTOR_REGISTER, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
    @ResponseBody public ChoreographerExecutorResponseDTO registerExecutor(final HttpServletRequest servletRequest, @RequestBody final ChoreographerExecutorRequestDTO request) {
    	logger.debug("registerExecutor started...");
    	return executorService.registerExecutorSystem(request, CommonConstants.CHOREOGRAPHER_URI + CommonConstants.OP_CHOREOGRAPHER_EXECUTOR_REGISTER, servletRequest);
    }
    
	//-------------------------------------------------------------------------------------------------
    @ApiOperation(value = "Unregister executor.", tags = { CoreCommonConstants.SWAGGER_TAG_CLIENT })
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_OK, message = DELETE_EXECUTOR_HTTP_200_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = DELETE_EXECUTOR_HTTP_400_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
            @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
    })
    @DeleteMapping(CommonConstants.OP_CHOREOGRAPHER_EXECUTOR_UNREGISTER)
    public void unregisterExecutor(final HttpServletRequest servletRequest,
    							  @RequestParam(name = CommonConstants.OP_CHOREOGRAPHER_EXECUTOR_UNREGISTER_REQUEST_PARAM_ADDRESS, required = false) final String executorAddress,
						          @RequestParam(name = CommonConstants.OP_CHOREOGRAPHER_EXECUTOR_UNREGISTER_REQUEST_PARAM_PORT, required = true) final Integer executorPort,
						          @RequestParam(name = CommonConstants.OP_CHOREOGRAPHER_EXECUTOR_UNREGISTER_REQUEST_PARAM_BASE_URI, required = false) final String executorBaseUri) { //TODO junit
        logger.debug("New Executor delete request received with address={}, port={}, baseUri= {}", executorAddress, executorPort, executorBaseUri);        
        executorService.unregisterExecutorSystem(executorAddress, executorPort, executorBaseUri,  CommonConstants.CHOREOGRAPHER_URI + CommonConstants.OP_CHOREOGRAPHER_EXECUTOR_UNREGISTER, servletRequest);
        logger.debug("Executor successfully deleted");
    }
}
