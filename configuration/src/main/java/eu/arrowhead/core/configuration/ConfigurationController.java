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

import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.Base64;
import java.util.List;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.logging.LogLevel;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.CoreDefaults;
import eu.arrowhead.common.CoreUtilities;
import eu.arrowhead.common.Defaults;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.CoreUtilities.ValidatedPageParams;
import eu.arrowhead.common.core.CoreSystem;
import eu.arrowhead.common.database.entity.Logs;
import eu.arrowhead.common.database.service.CommonDBService;
import eu.arrowhead.common.dto.internal.LogEntryListResponseDTO;
import eu.arrowhead.common.dto.shared.ConfigurationListResponseDTO;
import eu.arrowhead.common.dto.shared.ConfigurationRequestDTO;
import eu.arrowhead.common.dto.shared.ConfigurationResponseDTO;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.common.exception.DataNotFoundException;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.common.verifier.CommonNamePartVerifier;
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
	private static final String PATH_VARIABLE_NAME = "systemName";

	private static final String PUT_CONFIG_MGMT_HTTP_200_MESSAGE = "Configuration updated";
	private static final String PUT_CONFIG_MGMT_HTTP_400_MESSAGE = "Could not store configuration data";
	
	private static final String OP_NOT_VALID_ERROR_MESSAGE = " Illegal operation. ";
	private static final String NOT_FOUND_ERROR_MESSAGE = " Resource not found. ";
	private static final String SYSTEM_NAME_WRONG_FORMAT_ERROR_MESSAGE = "System name has invalid format. System names only contain letters (english alphabet), numbers and dash (-), and have to start with a letter (also cannot end with dash).";

	private static final String CONFIG_MGMT_URI =  CoreCommonConstants.MGMT_URI + "/config";
	private static final String CONFIG_BY_NAME_MGMT_URI = CONFIG_MGMT_URI + "/{" + PATH_VARIABLE_NAME + "}";
	
	private final Logger logger = LogManager.getLogger(ConfigurationController.class);

	@Autowired
	private ConfigurationDBService configurationDBService;
	
	@Autowired
	private CommonDBService commonDBService;
	
	@Autowired
	private CommonNamePartVerifier cnVerifier;
	
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
				
		final String origin = CommonConstants.CONFIGURATION_URI + CoreCommonConstants.OP_QUERY_LOG_ENTRIES;
		final ValidatedPageParams validParameters = CoreUtilities.validatePageParameters(page, size, direction, origin);
		final List<LogLevel> logLevels = CoreUtilities.getLogLevels(logLevel, origin);
		
		try {
			final ZonedDateTime _from = Utilities.parseUTCStringToLocalZonedDateTime(from);
			final ZonedDateTime _to = Utilities.parseUTCStringToLocalZonedDateTime(to);
			
			if (_from != null && _to != null && _to.isBefore(_from)) {
				throw new BadPayloadException("Invalid time interval", HttpStatus.SC_BAD_REQUEST, origin);
			}

			final LogEntryListResponseDTO response = commonDBService.getLogEntriesResponse(validParameters.getValidatedPage(), validParameters.getValidatedSize(), validParameters.getValidatedDirection(), sortField, CoreSystem.CONFIGURATION, 
																						   logLevels, _from, _to, loggerStr);
			
			logger.debug("Log entries  with page: {} and item_per page: {} retrieved successfully", page, size);
			return response;
		} catch (final DateTimeParseException ex) {
			throw new BadPayloadException("Invalid time parameter", HttpStatus.SC_BAD_REQUEST, origin, ex);
		}
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
	@ResponseBody public ResponseEntity<byte[]> rawconfGet(@PathVariable(value="systemName", required = true) String systemName) {
		if (Utilities.isEmpty(systemName)) {
			throw new InvalidParameterException(OP_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, CommonConstants.OP_CONFIGURATION_RAWCONF);
		}
		systemName = systemName.toLowerCase().trim();
		
		final ConfigurationResponseDTO ret = configurationDBService.getConfigForSystem(systemName);
		if (ret == null) {
			throw new DataNotFoundException(NOT_FOUND_ERROR_MESSAGE, HttpStatus.SC_NOT_FOUND, CommonConstants.OP_CONFIGURATION_RAWCONF + "/" + systemName);
		}
		final HttpHeaders headers = new HttpHeaders();
		headers.set("Content-Type", ret.getContentType());
		headers.set("Content-Disposition", "attachment; filename=" + ret.getFileName());
	
		final ResponseEntity<byte[]> responseEntity = new ResponseEntity<>(Base64.getDecoder().decode(ret.getData()), headers, org.springframework.http.HttpStatus.OK);	
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
	@ResponseBody public ConfigurationResponseDTO confGet(@PathVariable(value="systemName", required = true) String systemName) {
		if (Utilities.isEmpty(systemName)) {
			throw new InvalidParameterException(OP_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, CommonConstants.OP_CONFIGURATION_CONF);
		}
		systemName = systemName.toLowerCase().trim();
		
		final ConfigurationResponseDTO ret = configurationDBService.getConfigForSystem(systemName);
		if (ret == null) {
			throw new DataNotFoundException(NOT_FOUND_ERROR_MESSAGE, HttpStatus.SC_NOT_FOUND, CommonConstants.OP_CONFIGURATION_CONF + "/" + systemName);
		}
		
		return ret;
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Interface to get an other application's configuration", response = ConfigurationResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_CLIENT })
	@ApiResponses (value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = CoreCommonConstants.SWAGGER_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_NOT_FOUND, message = CoreCommonConstants.SWAGGER_HTTP_404_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path= CommonConstants.OP_CONFIGURATION_CONF_BY_PROXY + "/{systemName}", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public ConfigurationResponseDTO confGetByProxy(@PathVariable(value="systemName", required = true) String systemName) {
		if (Utilities.isEmpty(systemName)) {
			throw new InvalidParameterException(OP_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, CommonConstants.OP_CONFIGURATION_CONF_BY_PROXY);
		}
		systemName = systemName.toLowerCase().trim();
		
		final ConfigurationResponseDTO ret = configurationDBService.getConfigForSystem(systemName);
		if (ret == null) {
			throw new DataNotFoundException(NOT_FOUND_ERROR_MESSAGE, HttpStatus.SC_NOT_FOUND, CommonConstants.OP_CONFIGURATION_CONF_BY_PROXY + "/" + systemName);
		}
		
		return ret;
	}

	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Interface to list all configuration files", response = ConfigurationListResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
	@ApiResponses (value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = CoreCommonConstants.SWAGGER_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path=CONFIG_MGMT_URI, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public ConfigurationListResponseDTO confListGet() {
		final ConfigurationListResponseDTO ret = configurationDBService.getAllConfigurations();

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
		logger.debug("New configurationStore put request received with name: {}", systemName);
		final String origin = CommonConstants.CONFIGURATION_URI + CONFIG_BY_NAME_MGMT_URI;	
		
		if (Utilities.isEmpty(systemName)) {
			throw new InvalidParameterException(OP_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
		}

		validateConfigRequestDTO(systemName.toLowerCase().trim(), config, origin);
		final ConfigurationResponseDTO configResponse = configurationDBService.setConfigForSystem(systemName.toLowerCase().trim(), config);
		
		return configResponse;
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Stores/updates an other application's configuration", response = ConfigurationResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_CLIENT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = PUT_CONFIG_MGMT_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = PUT_CONFIG_MGMT_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@PutMapping(path = CommonConstants.OP_CONFIGURATION_SAVE_CONF_BY_PROXY, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public ConfigurationResponseDTO storeConfigurationForSystemByProxy(@RequestBody final ConfigurationRequestDTO config) {
		logger.debug("New storeConfigurationForSystemByProxy put request received.");
		final String origin = CommonConstants.CONFIGURATION_URI + CommonConstants.OP_CONFIGURATION_SAVE_CONF_BY_PROXY;	
		
		validateConfigRequestDTO(config.getSystemName(), config, origin);
		final ConfigurationResponseDTO configResponse = configurationDBService.setConfigForSystem(config.getSystemName(), config);
		
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
		logger.debug("New configurationStore delete request received with name: {}", systemName);
		final String origin = CommonConstants.CONFIGURATION_URI + CONFIG_BY_NAME_MGMT_URI;	
		
		if (Utilities.isEmpty(systemName)) {
			throw new InvalidParameterException(OP_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
		}

		final ConfigurationResponseDTO configResponse = configurationDBService.deleteConfigForSystem(systemName.toLowerCase().trim());
		
		return configResponse;
	}

	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	private void validateConfigRequestDTO(final String systemName, final ConfigurationRequestDTO dto, final String origin) {
		logger.debug("validateConfigRequestDTO started...");

		try {
			Assert.notNull(systemName, "systemName is null.");
			Assert.notNull(dto.getSystemName(), "systemName parameter is missing.");
			Assert.notNull(dto.getFileName(), "filename is missing.");
			Assert.notNull(dto.getContentType(), "contentType is missing.");
			Assert.notNull(dto.getData(), "data is missing.");

			Assert.isTrue(!Utilities.isEmpty(systemName), "systemName is not specified.");
			Assert.isTrue(!Utilities.isEmpty(dto.getSystemName()), "SystemName parameter is empty.");
			Assert.isTrue(!Utilities.isEmpty(dto.getFileName()), "fileName is empty.");
			Assert.isTrue(!Utilities.isEmpty(dto.getContentType()), "contentTyupe is empty.");
			Assert.isTrue(!Utilities.isEmpty(dto.getData()), "data is empty.");

			Assert.isTrue(systemName.equals(dto.getSystemName().toLowerCase().trim()), "request and parameter systemName mismatch");
			
			Assert.isTrue(cnVerifier.isValid(systemName), SYSTEM_NAME_WRONG_FORMAT_ERROR_MESSAGE);
		} catch (final Exception e) {
			throw new BadPayloadException("Bad payload: " + e.getMessage());
		}
	}
}