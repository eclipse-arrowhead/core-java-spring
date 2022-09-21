/********************************************************************************
 * Copyright (c) 2020 {Lulea University of Technology}
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
package eu.arrowhead.core.datamanager;

import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.logging.LogLevel;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

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
import eu.arrowhead.common.dto.shared.DataManagerServicesResponseDTO;
import eu.arrowhead.common.dto.shared.DataManagerSystemsResponseDTO;
import eu.arrowhead.common.dto.shared.SenML;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.common.exception.DataNotFoundException;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.common.verifier.CommonNamePartVerifier;
import eu.arrowhead.core.datamanager.service.DataManagerDriver;
import eu.arrowhead.core.datamanager.service.HistorianService;
import eu.arrowhead.core.datamanager.service.ProxyElement;
import eu.arrowhead.core.datamanager.service.ProxyService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;


@Api(tags = { CoreCommonConstants.SWAGGER_TAG_ALL })
@CrossOrigin(maxAge = Defaults.CORS_MAX_AGE, allowCredentials = Defaults.CORS_ALLOW_CREDENTIALS, 
allowedHeaders = { HttpHeaders.ORIGIN, HttpHeaders.CONTENT_TYPE, HttpHeaders.ACCEPT, HttpHeaders.AUTHORIZATION }
)
@RestController
@RequestMapping(CommonConstants.DATAMANAGER_URI)
public class DataManagerController {
	
	//=================================================================================================
	// members
	
	private static final String OP_NOT_VALID_ERROR_MESSAGE = " Illegal operation. ";
	private static final String NOT_FOUND_ERROR_MESSAGE = " Resource not found. ";
	private static final String SYSTEM_NAME_WRONG_FORMAT_ERROR_MESSAGE = "System name has invalid format. System names only contain letters (english alphabet), numbers and dash (-), and have to start with a letter (also cannot end with dash).";
	private static final String SERVICE_DEFINITION_WRONG_FORMAT_ERROR_MESSAGE = "Service definition has invalid format. Service definition only contains letters (english alphabet), numbers and dash (-), and has to start with a letter (also cannot ends with dash).";
	
	private final Logger logger = LogManager.getLogger(DataManagerController.class);

	@Autowired
	private ProxyService proxyService;

	@Autowired
	private HistorianService historianService;
	
	@Autowired
	private CommonDBService commonDBService;

	@Autowired
	private DataManagerDriver dataManagerDriver;

	@Autowired
	private CommonNamePartVerifier cnVerifier;

	@Value(CoreCommonConstants.$USE_STRICT_SERVICE_DEFINITION_VERIFIER_WD)
	private boolean useStrictServiceDefinitionVerifier;
	
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
				
		final String origin = CommonConstants.DATAMANAGER_URI + CoreCommonConstants.OP_QUERY_LOG_ENTRIES;
		final ValidatedPageParams validParameters = CoreUtilities.validatePageParameters(page, size, direction, origin);
		final List<LogLevel> logLevels = CoreUtilities.getLogLevels(logLevel, origin);
		
		try {
			final ZonedDateTime _from = Utilities.parseUTCStringToLocalZonedDateTime(from);
			final ZonedDateTime _to = Utilities.parseUTCStringToLocalZonedDateTime(to);
			
			if (_from != null && _to != null && _to.isBefore(_from)) {
				throw new BadPayloadException("Invalid time interval", HttpStatus.SC_BAD_REQUEST, origin);
			}

			final LogEntryListResponseDTO response = commonDBService.getLogEntriesResponse(validParameters.getValidatedPage(), validParameters.getValidatedSize(), validParameters.getValidatedDirection(), sortField, CoreSystem.DATAMANAGER, 
																						   logLevels, _from, _to, loggerStr);
			
			logger.debug("Log entries  with page: {} and item_per page: {} retrieved successfully", page, size);
			return response;
		} catch (final DateTimeParseException ex) {
			throw new BadPayloadException("Invalid time parameter", HttpStatus.SC_BAD_REQUEST, origin, ex);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Interface to the Historian service", response = DataManagerSystemsResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_CLIENT })
	@ApiResponses (value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = CoreCommonConstants.SWAGGER_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path= CommonConstants.OP_DATAMANAGER_HISTORIAN, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public DataManagerSystemsResponseDTO historianGet() {
		logger.debug("historianGet");

		final DataManagerSystemsResponseDTO ret = new DataManagerSystemsResponseDTO();

		final ArrayList<String> systems = historianService.getSystems();
		ret.setSystems(systems);
		return ret;
	}

	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Interface to get all services that a specific system has active in the Historian service", response = DataManagerServicesResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_CLIENT })
	@ApiResponses (value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = CoreCommonConstants.SWAGGER_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = CoreCommonConstants.SWAGGER_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(value= CommonConstants.OP_DATAMANAGER_HISTORIAN + "/{systemName}", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public DataManagerServicesResponseDTO historianSystemGet(@PathVariable(value="systemName", required=true) final String systemName) {
		logger.debug("historianSystemGet for {}", systemName);

	    if (Utilities.isEmpty(systemName)) {
	    	throw new InvalidParameterException(OP_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, CommonConstants.OP_DATAMANAGER_HISTORIAN);
	    }
	    
	    if (!cnVerifier.isValid(systemName)) {
	    	throw new InvalidParameterException(SYSTEM_NAME_WRONG_FORMAT_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, CommonConstants.OP_DATAMANAGER_HISTORIAN);
	    }
		final DataManagerServicesResponseDTO ret = new DataManagerServicesResponseDTO();
		final ArrayList<String> services = historianService.getServicesFromSystem(systemName);
		ret.setServices(services);

		return ret;
	}

	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Interface to get data from a service", response = SenML.class, responseContainer="Vector", tags = { CoreCommonConstants.SWAGGER_TAG_CLIENT })
	@ApiResponses (value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = CoreCommonConstants.SWAGGER_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = CoreCommonConstants.SWAGGER_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_NOT_FOUND, message = CoreCommonConstants.SWAGGER_HTTP_404_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(value= CommonConstants.OP_DATAMANAGER_HISTORIAN + "/{system}/{service}", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public Vector<SenML> historianServiceGet(@PathVariable(value="system", required=true) final String systemName,
														   @PathVariable(value="service", required=true) final String serviceName,
														   @RequestParam final MultiValueMap<String, String> params) {

		if (Utilities.isEmpty(systemName) || Utilities.isEmpty(serviceName)) {
			throw new InvalidParameterException(OP_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, CommonConstants.OP_DATAMANAGER_HISTORIAN);
		}
		logger.debug("historianServiceGet for {}/{}", systemName, serviceName);
		
	    if (!cnVerifier.isValid(systemName)) {
	    	throw new InvalidParameterException(SYSTEM_NAME_WRONG_FORMAT_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, CommonConstants.OP_DATAMANAGER_HISTORIAN);
	    }

	    if (useStrictServiceDefinitionVerifier && !cnVerifier.isValid(serviceName)) {
	    	throw new InvalidParameterException(SERVICE_DEFINITION_WRONG_FORMAT_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, CommonConstants.OP_DATAMANAGER_HISTORIAN);
	    }

		double from=-1, to=-1;
		int count = 1;

		final Vector<String> signals = new Vector<String>();
		final Vector<Integer> signalCounts = new Vector<Integer>();
		final Iterator<String> it = params.keySet().iterator();
		int signalCountId = 0;
		while(it.hasNext()){
			final String par = it.next();
			if (par.equals("count")) {
				try {
					count = Integer.parseInt(params.getFirst(par));
				} catch(final NumberFormatException nfe) {
					throw new InvalidParameterException(OP_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, CommonConstants.OP_DATAMANAGER_HISTORIAN + "/" + systemName + "/" + serviceName);
				}
			} else if (par.equals("sig"+signalCountId)) {
				signals.add(params.getFirst(par));
        		int signalXCount = 1;
        		if (params.getFirst("sig"+signalCountId+"count") != null) {
          			try {
            			signalXCount = Integer.parseInt(params.getFirst("sig"+signalCountId+"count"));
          			} catch(final NumberFormatException nfe) {
            			throw new InvalidParameterException(OP_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, CommonConstants.OP_DATAMANAGER_HISTORIAN + "/" + systemName + "/" + serviceName);
          			}
        		}
        		if (signalXCount <= 0) {
            		throw new InvalidParameterException(OP_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, CommonConstants.OP_DATAMANAGER_HISTORIAN + "/" + systemName + "/" + serviceName);
        		}
				signalCounts.add(signalXCount);
				signalCountId++;
			} else if (par.equals("from")) {
				try {
					from = Double.parseDouble(params.getFirst(par));
				}  catch(final NumberFormatException nfe) {
					throw new InvalidParameterException(OP_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, CommonConstants.OP_DATAMANAGER_HISTORIAN + "/" + systemName + "/" + serviceName);
				}
			} else if (par.equals("to")) {
				try {
					to = Double.parseDouble(params.getFirst(par));
				}  catch(final NumberFormatException nfe) {
					throw new InvalidParameterException(OP_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, CommonConstants.OP_DATAMANAGER_HISTORIAN + "/" + systemName + "/" + serviceName);
				}
			}
		}

		if (count <= 0) {
      		throw new InvalidParameterException(OP_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, CommonConstants.OP_DATAMANAGER_HISTORIAN + "/" + systemName + "/" + serviceName);
		}

		Vector<SenML> ret = null;

		if(signals.isEmpty()){
			ret = historianService.fetchEndpoint(systemName, serviceName, from, to, count);
		} else {
			ret = historianService.fetchEndpoint(systemName, serviceName, from, to, signalCounts, signals);
		}

		if (ret == null) {
			throw new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND);
		}

		return ret;
	}


	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Interface to put data from a service", tags = { CoreCommonConstants.SWAGGER_TAG_CLIENT })
	@ApiResponses (value = {
		@ApiResponse(code = HttpStatus.SC_OK, message = CoreCommonConstants.SWAGGER_HTTP_200_MESSAGE),
		@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = CoreCommonConstants.SWAGGER_HTTP_400_MESSAGE),
		@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
		@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@PutMapping(value= CommonConstants.OP_DATAMANAGER_HISTORIAN + "/{systemName}/{serviceName}", consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public void historianServicePut(@PathVariable(value="systemName", required=true) final String systemName,
												  @PathVariable(value="serviceName", required=true) final String serviceName,
												  @RequestBody final Vector<SenML> message) {
        if (Utilities.isEmpty(systemName) || Utilities.isEmpty(serviceName)) {
            throw new InvalidParameterException(OP_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, CommonConstants.OP_DATAMANAGER_HISTORIAN);
        }
		logger.debug("historianServicePut for {}/{}", systemName, serviceName);
		
	    if (!cnVerifier.isValid(systemName)) {
	    	throw new InvalidParameterException(SYSTEM_NAME_WRONG_FORMAT_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, CommonConstants.OP_DATAMANAGER_HISTORIAN);
	    }

	    if (useStrictServiceDefinitionVerifier && !cnVerifier.isValid(serviceName)) {
	    	throw new InvalidParameterException(SERVICE_DEFINITION_WRONG_FORMAT_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, CommonConstants.OP_DATAMANAGER_HISTORIAN);
	    }
		
		dataManagerDriver.validateSenMLMessage(systemName, serviceName, message);

		historianService.createEndpoint(systemName, serviceName);

		final SenML head = message.firstElement();
		if (head.getBt() == null) {
			head.setBt((double)System.currentTimeMillis() / 1000);
		}

		dataManagerDriver.validateSenMLContent(message);

		final boolean statusCode = historianService.updateEndpoint(systemName, serviceName, message);
		if (!statusCode) {
			throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Start interface for the Proxy service", response = DataManagerSystemsResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_CLIENT })
	@ApiResponses (value = {
		@ApiResponse(code = HttpStatus.SC_OK, message = CoreCommonConstants.SWAGGER_HTTP_200_MESSAGE),
		@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
		@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(value= CommonConstants.OP_DATAMANAGER_PROXY, produces=MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public DataManagerSystemsResponseDTO proxyGet() {
		logger.debug("proxyGet");

		final DataManagerSystemsResponseDTO ret = new DataManagerSystemsResponseDTO();

		final List<String> systems = proxyService.getAllSystems();
		ret.setSystems(systems);

		return ret;
	}

	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Interface to get a system's all services in the Proxy service", response = DataManagerServicesResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_CLIENT })
	@ApiResponses (value = {
		@ApiResponse(code = HttpStatus.SC_OK, message = CoreCommonConstants.SWAGGER_HTTP_200_MESSAGE),
		@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
		@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(value= CommonConstants.OP_DATAMANAGER_PROXY + "/{systemName}", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public DataManagerServicesResponseDTO proxySystemGet(@PathVariable(value="systemName", required=true) final String systemName) {
        if (Utilities.isEmpty(systemName)) {
            throw new InvalidParameterException(OP_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, CommonConstants.OP_DATAMANAGER_PROXY);
        }
        logger.debug("proxySystemGet for {}", systemName);
        
        if (!cnVerifier.isValid(systemName)) {
        	throw new InvalidParameterException(SYSTEM_NAME_WRONG_FORMAT_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, CommonConstants.OP_DATAMANAGER_PROXY);
        }

		final DataManagerServicesResponseDTO ret = new DataManagerServicesResponseDTO();
		final ArrayList<String> services = proxyService.getEndpointsNamesFromSystem(systemName);
		ret.setServices(services);
		return ret;
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Interface to get a system's last service data", response = SenML.class, responseContainer="Vector", tags = { CoreCommonConstants.SWAGGER_TAG_CLIENT })
	@ApiResponses (value = {
		@ApiResponse(code = HttpStatus.SC_OK, message = CoreCommonConstants.SWAGGER_HTTP_200_MESSAGE),
		@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
		@ApiResponse(code = HttpStatus.SC_NOT_FOUND, message = CoreCommonConstants.SWAGGER_HTTP_404_MESSAGE),
		@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(value= CommonConstants.OP_DATAMANAGER_PROXY + "/{systemName}/{serviceName}", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public Vector<SenML> proxyServiceGet(@PathVariable(value="systemName", required=true) final String systemName,
													   @PathVariable(value="serviceName", required=true) final String serviceName) {
        if (Utilities.isEmpty(systemName) || Utilities.isEmpty(serviceName)) {
            throw new InvalidParameterException(OP_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, CommonConstants.OP_DATAMANAGER_PROXY);
        }
		logger.debug("proxyServiceGet for {}/{}", systemName, serviceName);
		
        if (!cnVerifier.isValid(systemName)) {
        	throw new InvalidParameterException(SYSTEM_NAME_WRONG_FORMAT_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, CommonConstants.OP_DATAMANAGER_PROXY);
        }
        
        if (useStrictServiceDefinitionVerifier && !cnVerifier.isValid(serviceName)) {
        	throw new InvalidParameterException(SERVICE_DEFINITION_WRONG_FORMAT_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, CommonConstants.OP_DATAMANAGER_PROXY);
        }

		final ProxyElement pe = proxyService.getEndpointFromService(systemName, serviceName);
		if (pe == null) {
			logger.debug("proxy GET to serviceName: " + serviceName + " not found");
			throw new DataNotFoundException(NOT_FOUND_ERROR_MESSAGE, HttpStatus.SC_NOT_FOUND, CommonConstants.OP_DATAMANAGER_PROXY + "/" + systemName + "/" + serviceName);
		}

		return pe.getMessage();
	}

	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Interface to update a system's last data", tags = { CoreCommonConstants.SWAGGER_TAG_CLIENT })
	@ApiResponses (value = {
		@ApiResponse(code = HttpStatus.SC_OK, message = CoreCommonConstants.SWAGGER_HTTP_200_MESSAGE),
		@ApiResponse(code = HttpStatus.SC_CREATED, message = CoreCommonConstants.SWAGGER_HTTP_201_MESSAGE),
		@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = CoreCommonConstants.SWAGGER_HTTP_400_MESSAGE),
		@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
		@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@PutMapping(value= CommonConstants.OP_DATAMANAGER_PROXY + "/{systemName}/{serviceName}", consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public void proxyServicePut(@PathVariable(value="systemName", required=true) final String systemName,
											  @PathVariable(value="serviceName", required=true) final String serviceName,
											  @RequestBody final Vector<SenML> message) {
        if (Utilities.isEmpty(systemName) || Utilities.isEmpty(serviceName) || message == null) {
            throw new InvalidParameterException(OP_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, CommonConstants.OP_DATAMANAGER_PROXY);
        }
		logger.debug("proxyServicePut for {}/{}", systemName, serviceName);
		
        if (!cnVerifier.isValid(systemName)) {
        	throw new InvalidParameterException(SYSTEM_NAME_WRONG_FORMAT_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, CommonConstants.OP_DATAMANAGER_PROXY);
        }
        
        if (useStrictServiceDefinitionVerifier && !cnVerifier.isValid(serviceName)) {
        	throw new InvalidParameterException(SERVICE_DEFINITION_WRONG_FORMAT_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, CommonConstants.OP_DATAMANAGER_PROXY);
        }

		dataManagerDriver.validateSenMLMessage(systemName, serviceName, message);

		final SenML head = message.firstElement();
		if(head.getBt() == null) {
			head.setBt((double)System.currentTimeMillis() / 1000);
		}

		dataManagerDriver.validateSenMLContent(message);

		final ProxyElement pe = proxyService.getEndpointFromService(systemName, serviceName);
		if (pe == null) {
			final boolean ret = proxyService.addEndpointForService(new ProxyElement(systemName, serviceName));
			if (ret == true) {
				proxyService.updateEndpointFromService(systemName, serviceName, message);
			} else { 
				throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} else {
			proxyService.updateEndpointFromService(systemName, serviceName, message);
		}
	}
}