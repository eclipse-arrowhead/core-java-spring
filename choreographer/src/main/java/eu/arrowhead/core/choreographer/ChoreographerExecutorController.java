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
import eu.arrowhead.common.core.CoreSystem;
import eu.arrowhead.common.dto.shared.ChoreographerExecutorRequestDTO;
import eu.arrowhead.common.dto.shared.ChoreographerExecutorResponseDTO;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.common.processor.NetworkAddressDetector;
import eu.arrowhead.common.processor.NetworkAddressPreProcessor;
import eu.arrowhead.common.processor.model.AddressDetectionResult;
import eu.arrowhead.common.verifier.CommonNamePartVerifier;
import eu.arrowhead.common.verifier.NetworkAddressVerifier;
import eu.arrowhead.core.choreographer.database.service.ChoreographerExecutorDBService;
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
	private CommonNamePartVerifier cnVerifier;
	
	@Autowired
	private NetworkAddressPreProcessor networkAddressPreProcessor;
	
	@Autowired
	private NetworkAddressVerifier networkAddressVerifier; //TODO properties should come from ServiceRegistry or verify with a new SR service in order to allow the same formats (or register System by SR service)
	
	@Autowired
	private NetworkAddressDetector networkAddressDetector;
	
	private static final String POST_EXECUTOR_HTTP_201_MESSAGE = "Executor created.";
    private static final String POST_EXECUTOR_HTTP_400_MESSAGE = "Could not create executor.";
    
    private static final String SYSTEM_NAME_NULL_ERROR_MESSAGE = " System name must have value ";
	private static final String SYSTEM_NAME_WRONG_FORMAT_ERROR_MESSAGE = "System name has invalid format. System names only contain maximum 63 character of letters (english alphabet), numbers and dash (-), and have to start with a letter (also cannot end with dash).";
	private static final String SYSTEM_PORT_NULL_ERROR_MESSAGE = " System port must have value ";
	private static final String SERVICE_DEFINITION_WRONG_FORMAT_ERROR_MESSAGE = "Service definition has invalid format. Service definition only contains maximum 63 character of letters (english alphabet), numbers and dash (-), and has to start with a letter (also cannot ends with dash).";
	
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
    	checkExecutorRequestDTO(request, CommonConstants.CHOREOGRAPHER_EXECUTOR_URI + CoreCommonConstants.MGMT_URI, null);
        return executorDBService.createExecutorResponse(request);
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
    	checkExecutorRequestDTO(request, CommonConstants.CHOREOGRAPHER_EXECUTOR_URI + CoreCommonConstants.MGMT_URI, servletRequest);
        return executorDBService.createExecutorResponse(request);
    }
    
	//TODO unregisterExecutor    
    
    //-------------------------------------------------------------------------------------------------
	private void checkExecutorRequestDTO(final ChoreographerExecutorRequestDTO dto, final String origin, final HttpServletRequest servletRequest) {
		logger.debug("checkExecutorRequestDTO started...");

		// check system
		if (dto == null) {
			throw new BadPayloadException("dto is null.", HttpStatus.SC_BAD_REQUEST, origin);
		}

		if (Utilities.isEmpty(dto.getSystem().getSystemName())) {
			throw new BadPayloadException(SYSTEM_NAME_NULL_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
		}

		for (final CoreSystem coreSysteam : CoreSystem.values()) {
			if (coreSysteam.name().equalsIgnoreCase(dto.getSystem().getSystemName().trim())) {
				throw new BadPayloadException("System name '" + dto.getSystem().getSystemName() + "' is a reserved arrowhead core system name.", HttpStatus.SC_BAD_REQUEST, origin);
			}
		}

		if (!cnVerifier.isValid(dto.getSystem().getSystemName())) {
			throw new BadPayloadException(SYSTEM_NAME_WRONG_FORMAT_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
		}

		if (dto.getSystem().getPort() == null) {
			throw new BadPayloadException(SYSTEM_PORT_NULL_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
		}

		final int validatedPort = dto.getSystem().getPort();
		if (validatedPort < CommonConstants.SYSTEM_PORT_RANGE_MIN || validatedPort > CommonConstants.SYSTEM_PORT_RANGE_MAX) {
			throw new BadPayloadException("Port must be between " + CommonConstants.SYSTEM_PORT_RANGE_MIN + " and " + CommonConstants.SYSTEM_PORT_RANGE_MAX + ".", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		try {			
			networkAddressVerifier.verify(networkAddressPreProcessor.normalize(dto.getSystem().getAddress()));
		} catch (final InvalidParameterException ex) {
			if (servletRequest != null) {
				final AddressDetectionResult detectionResult = networkAddressDetector.detect(servletRequest);
				if (detectionResult.isSkipped()) {
					throw new BadPayloadException(ex.getMessage() + " " + detectionResult.getDetectionMessage(), HttpStatus.SC_BAD_REQUEST, origin);				
				}
				if (!detectionResult.isDetectionSuccess()) {
					throw new BadPayloadException(ex.getMessage() + " " + detectionResult.getDetectionMessage(), HttpStatus.SC_BAD_REQUEST, origin);
				} else {
					dto.getSystem().setAddress(detectionResult.getDetectedAddress());
				}				
			}
		}
		
		// check others
		if (!cnVerifier.isValid(dto.getServiceDefinitionName())) {
			throw new BadPayloadException(SERVICE_DEFINITION_WRONG_FORMAT_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		if (dto.getMinVersion() == null) {
			throw new BadPayloadException("minVersion is null", HttpStatus.SC_BAD_REQUEST, origin);
		}
		if (dto.getMaxVersion() == null) {
			throw new BadPayloadException("maxVersion is null", HttpStatus.SC_BAD_REQUEST, origin);
		}
		if (dto.getMinVersion() > dto.getMaxVersion()) {
			throw new InvalidParameterException("minVersion cannot be higher than maxVersion");
		}
	}	
}
