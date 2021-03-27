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
package eu.arrowhead.core.configuration;

import java.util.List;
import java.util.ArrayList;
import java.util.Vector;
import java.util.Iterator;
import java.util.Base64;
import java.security.MessageDigest;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.util.MultiValueMap;
import org.springframework.util.Assert;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.CoreDefaults;
import eu.arrowhead.common.CoreUtilities;
import eu.arrowhead.common.CoreUtilities.ValidatedPageParams;
import eu.arrowhead.common.Defaults;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.shared.ConfigurationRequestDTO;
import eu.arrowhead.common.dto.shared.ConfigurationResponseDTO;
import eu.arrowhead.common.dto.shared.ConfigurationListResponseDTO;
import eu.arrowhead.common.dto.shared.ConfigurationSystemsListResponseDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.common.exception.DataNotFoundException;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.core.configuration.database.service.ConfigurationDBService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;


@Api(tags = { CoreCommonConstants.SWAGGER_TAG_ALL })
@CrossOrigin(maxAge = Defaults.CORS_MAX_AGE, allowCredentials = Defaults.CORS_ALLOW_CREDENTIALS, 
allowedHeaders = { HttpHeaders.ORIGIN, HttpHeaders.CONTENT_TYPE, HttpHeaders.ACCEPT, HttpHeaders.AUTHORIZATION }
)
@RestController
@RequestMapping(CommonConstants.CONFIGURATION_URI)
public class ConfigurationController {
	
	//=================================================================================================
	// members
	private static final String PATH_VARIABLE_NAME = "name";

	private static final String PUT_CONFIG_MGMT_HTTP_200_MESSAGE = "Configuration updated";
	private static final String PUT_CONFIG_MGMT_HTTP_400_MESSAGE = "Could not store configration data";
	
	private static final String OP_NOT_VALID_ERROR_MESSAGE = " Illegal operation. ";
	private static final String NOT_FOUND_ERROR_MESSAGE = " Resource not found. ";

	private static final String CONFIG_MGMT_URI =  CoreCommonConstants.MGMT_URI + "/config";
	private static final String CONFIG_BY_NAME_MGMT_URI = CONFIG_MGMT_URI + "/{" + PATH_VARIABLE_NAME + "}";
	
	private final Logger logger = LogManager.getLogger(ConfigurationController.class);

	@Autowired
	private ConfigurationDBService configurationDBService;
	
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
	@ApiOperation(value = "Interface to get a configuration file", response = ConfigurationResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_CLIENT })
	@ApiResponses (value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = CoreCommonConstants.SWAGGER_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_NOT_FOUND, message = CoreCommonConstants.SWAGGER_HTTP_404_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path= CommonConstants.OP_CONFIGURATION_RAWCONF + "/{systemName}")
	@ResponseBody public ResponseEntity<byte[]> rawconfGet(
			@PathVariable(value="systemName", required=true) String systemName
			) {

			if(Utilities.isEmpty(systemName)) {
				throw new InvalidParameterException(OP_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, CommonConstants.OP_CONFIGURATION_RAWCONF);
			}
			logger.debug("rawconfGet for {}", systemName);
			
			ConfigurationResponseDTO ret = configurationDBService.getConfigForSystem(systemName);
			if(ret == null) {
				throw new DataNotFoundException(NOT_FOUND_ERROR_MESSAGE, HttpStatus.SC_NOT_FOUND, CommonConstants.OP_CONFIGURATION_RAWCONF + "/" + systemName);
			}
			HttpHeaders headers = new HttpHeaders();
			headers.set("Content-Type", ret.getContentType());
    		headers.set("Content-Disposition", "attachment; filename=" + ret.getFileName());
		
			ResponseEntity<byte[]> responseEntity = new ResponseEntity<>(Base64.getDecoder().decode(ret.getData()), headers, org.springframework.http.HttpStatus.OK);	
    		return responseEntity;
	}

	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Interface to get a configuration", response = ConfigurationResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_CLIENT })
	@ApiResponses (value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = CoreCommonConstants.SWAGGER_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_NOT_FOUND, message = CoreCommonConstants.SWAGGER_HTTP_404_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path= CommonConstants.OP_CONFIGURATION_CONF + "/{systemName}", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public ConfigurationResponseDTO confGet(
			@PathVariable(value="systemName", required=true) String systemName
			) {

			if(Utilities.isEmpty(systemName)) {
				throw new InvalidParameterException(OP_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, CommonConstants.OP_CONFIGURATION_RAWCONF);
			}
			logger.debug("confGet for {}", systemName);
			
			ConfigurationResponseDTO ret = configurationDBService.getConfigForSystem(systemName);
			if(ret == null) {
				throw new DataNotFoundException(NOT_FOUND_ERROR_MESSAGE, HttpStatus.SC_NOT_FOUND, CommonConstants.OP_CONFIGURATION_CONF + "/" + systemName);
			}
			
			return ret;
	}

	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Interface to list all configuration files", response = ConfigurationListResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
	@ApiResponses (value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = CoreCommonConstants.SWAGGER_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_NOT_FOUND, message = CoreCommonConstants.SWAGGER_HTTP_404_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path=CONFIG_MGMT_URI, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public ConfigurationListResponseDTO confListGet(
			) {
				logger.debug("confList");

				ConfigurationListResponseDTO ret = configurationDBService.getAllConfigurations();

				return ret;
			}

	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Store new configuration", response = ConfigurationResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = PUT_CONFIG_MGMT_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = PUT_CONFIG_MGMT_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@PutMapping(path = CONFIG_BY_NAME_MGMT_URI, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public ConfigurationResponseDTO storeConfigurationForSystem(@PathVariable(value = PATH_VARIABLE_NAME) final String systemName, @RequestBody final ConfigurationRequestDTO config) {
		logger.debug("New configurationStore put request recieved with name: {}", systemName);
		final String origin = CommonConstants.CONFIGURATION_URI + CONFIG_BY_NAME_MGMT_URI;	
		
		validateConfigRequestDTO(systemName, config, origin);
		final ConfigurationResponseDTO configResponse = configurationDBService.setConfigForSystem(systemName, config);
		
		logger.debug("System '{}' is successfully updated with new config {}", systemName, config.toString());
		return configResponse;
	}

	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Delete configuration", response = ConfigurationResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = PUT_CONFIG_MGMT_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = PUT_CONFIG_MGMT_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@DeleteMapping(path = CONFIG_BY_NAME_MGMT_URI, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public ConfigurationResponseDTO deleteConfigurationForSystem(@PathVariable(value = PATH_VARIABLE_NAME) final String systemName) {
		logger.debug("New configurationStore delete request recieved with name: {}", systemName);
		final String origin = CommonConstants.CONFIGURATION_URI + CONFIG_BY_NAME_MGMT_URI;	
		
		final ConfigurationResponseDTO configResponse = configurationDBService.deleteConfigForSystem(systemName);
		
		logger.debug("System '{}' is successfully deleted", systemName);
		return configResponse;
	}

	//=================================================================================================
	// assistant methods

	private void validateConfigRequestDTO(final String systemName, final ConfigurationRequestDTO dto, final String origin) {
		logger.debug("validateConfigRequestDTO started...");

		try {
			Assert.notNull(systemName, "systemName is null.");
			Assert.notNull(dto.getSystemName(), "systemName is null.");
			Assert.notNull(dto.getFileName(), "filename is null.");
			Assert.notNull(dto.getContentType(), "contentType is null.");
			Assert.notNull(dto.getData(), "data is null.");
		} catch(Exception e){
			throw new BadPayloadException("Bad payload");
		}
	}
	
}
