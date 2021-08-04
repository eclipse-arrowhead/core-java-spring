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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.Defaults;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.shared.ChoreographerExecutorRequestDTO;
import eu.arrowhead.common.dto.shared.ChoreographerExecutorResponseDTO;
import eu.arrowhead.common.dto.shared.SystemResponseDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.common.processor.NetworkAddressDetector;
import eu.arrowhead.common.processor.model.AddressDetectionResult;
import eu.arrowhead.common.verifier.CommonNamePartVerifier;
import eu.arrowhead.common.verifier.NetworkAddressVerifier;
import eu.arrowhead.core.choreographer.database.service.ChoreographerExecutorDBService;
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
@RequestMapping(CommonConstants.CHOREOGRAPHER_EXECUTOR_URI)
public class ChoreographerExecutorController {
	
	//=================================================================================================
	// members
	
	@Autowired
	private ChoreographerExecutorDBService executorDBService;
	
	@Autowired
	private ChoreographerExecutorService executorService;
	
	@Autowired
	private CommonNamePartVerifier cnVerifier;
	
	@Autowired
	private NetworkAddressDetector networkAddressDetector;
	
	private static final String POST_EXECUTOR_HTTP_201_MESSAGE = "Executor created.";
    private static final String POST_EXECUTOR_HTTP_400_MESSAGE = "Could not create executor.";
    
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
    @PostMapping(path = CoreCommonConstants.MGMT_URI, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
    @ResponseBody public ChoreographerExecutorResponseDTO addExecutor(@RequestBody final ChoreographerExecutorRequestDTO request) {
    	logger.debug("addExecutor started...");
    	
    	checkExecutorRequestDTO(request, CommonConstants.CHOREOGRAPHER_EXECUTOR_URI + CoreCommonConstants.MGMT_URI, null);
    	final SystemResponseDTO system = executorService.registerExecutorSystem(request.getSystem());
    	return executorDBService.createExecutorResponse(system.getSystemName(), system.getAddress(), system.getPort(), request.getBaseUri(), request.getServiceDefinitionName(),
    													request.getMinVersion(), request.getMaxVersion());
    }

	//TODO removeExecutorById
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
    	
    	final String origin = CommonConstants.CHOREOGRAPHER_EXECUTOR_URI + CoreCommonConstants.MGMT_URI;
    	checkExecutorRequestDTO(request, origin, servletRequest);
    	
    	SystemResponseDTO system = null;
    	try {
    		system = executorService.registerExecutorSystem(request.getSystem());
			
		} catch (final ArrowheadException ex) {
			if (ex.getMessage().contains(NetworkAddressVerifier.ERROR_MSG_PREFIX)) {
				detectNetworkAddress(servletRequest, request, ex.getMessage(), origin);
				system = executorService.registerExecutorSystem(request.getSystem());
			}
		}
    	
    	return executorDBService.createExecutorResponse(system.getSystemName(), system.getAddress(), system.getPort(), request.getBaseUri(), request.getServiceDefinitionName(),
														request.getMinVersion(), request.getMaxVersion());
    }
    
	//TODO unregisterExecutor    
    
    //-------------------------------------------------------------------------------------------------
	private void checkExecutorRequestDTO(final ChoreographerExecutorRequestDTO dto, final String origin, final HttpServletRequest servletRequest) {
		logger.debug("checkExecutorRequestDTO started...");

		// Check SystemRequestDTO only for nulls. (Verification will be done by ServiceRegistry)
		if (dto == null) {
			throw new BadPayloadException("dto is null.", HttpStatus.SC_BAD_REQUEST, origin);
		}

		if (Utilities.isEmpty(dto.getSystem().getSystemName())) {
			throw new BadPayloadException("System name is empty.", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		if (Utilities.isEmpty(dto.getSystem().getAddress())) {
			if (servletRequest == null) {
				throw new BadPayloadException("System address is empty.", HttpStatus.SC_BAD_REQUEST, origin);
			} else {
				detectNetworkAddress(servletRequest, dto, "System address is empty.", origin);				
			}
		}

		if (dto.getSystem().getPort() == null) {
			throw new BadPayloadException("System port is null.", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		// check others
		if (!cnVerifier.isValid(dto.getServiceDefinitionName())) {
			throw new BadPayloadException("Service definition has invalid format. Name must match with the following regular expression: " + CommonNamePartVerifier.COMMON_NAME_PART_PATTERN_STRING, HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		if (dto.getMinVersion() == null) {
			throw new BadPayloadException("minVersion is null.", HttpStatus.SC_BAD_REQUEST, origin);
		}
		if (dto.getMaxVersion() == null) {
			throw new BadPayloadException("maxVersion is null.", HttpStatus.SC_BAD_REQUEST, origin);
		}
		if (dto.getMinVersion() > dto.getMaxVersion()) {
			throw new InvalidParameterException("minVersion cannot be higher than maxVersion.");
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private void detectNetworkAddress(final HttpServletRequest servletRequest, final ChoreographerExecutorRequestDTO dto, final String errorMsgPrefix, final String origin) {
		logger.debug("detectNetworkAddress started...");
		
		final AddressDetectionResult result = networkAddressDetector.detect(servletRequest);
		if (result.isSkipped() || !result.isDetectionSuccess()) {
			throw new BadPayloadException(errorMsgPrefix + " " + result.getDetectionMessage(), HttpStatus.SC_BAD_REQUEST, origin);				
		} else {
			dto.getSystem().setAddress(result.getDetectedAddress());
		}
	}
}
