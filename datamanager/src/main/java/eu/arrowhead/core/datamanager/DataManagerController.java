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

import java.util.List;
import java.util.ArrayList;
import java.util.Vector;
import java.util.Iterator;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
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
import eu.arrowhead.common.dto.shared.SenML;
import eu.arrowhead.common.dto.shared.DataManagerSystemsResponseDTO;
import eu.arrowhead.common.dto.shared.DataManagerServicesResponseDTO;
import eu.arrowhead.common.dto.shared.DataManagerOperationDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.common.exception.DataNotFoundException;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.core.datamanager.database.service.DataManagerDBService;
import eu.arrowhead.core.datamanager.service.ProxyService;
import eu.arrowhead.core.datamanager.service.ProxyElement;
import eu.arrowhead.core.datamanager.service.HistorianService;
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
	
	private final Logger logger = LogManager.getLogger(DataManagerController.class);

	@Autowired
	private ProxyService proxyService;

	@Autowired
	private HistorianService historianService;

	@Autowired
	private DataManagerDBService dataManagerDBService;
	
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
	@ApiOperation(value = "Interface to the Historian service", response = DataManagerSystemsResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_CLIENT })
	@ApiResponses (value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = CoreCommonConstants.SWAGGER_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path= CommonConstants.OP_DATAMANAGER_HISTORIAN, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public DataManagerSystemsResponseDTO historianGet(
			) {
		logger.debug("historianGet");

		DataManagerSystemsResponseDTO ret = new DataManagerSystemsResponseDTO();

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
	@ResponseBody public DataManagerServicesResponseDTO historianSystemGet(
		@PathVariable(value="systemName", required=true) String systemName
		) {
		logger.debug("historianSystemGet for {}", systemName);

    if(Utilities.isEmpty(systemName)) {
      throw new InvalidParameterException(OP_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, CommonConstants.OP_DATAMANAGER_HISTORIAN);
    }

		DataManagerServicesResponseDTO ret = new DataManagerServicesResponseDTO();
		final ArrayList<String> services = historianService.getServicesFromSystem(systemName);
		ret.setServices(services);

		return ret;
	}

	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Interface to get sensor data from a service", response = SenML.class, responseContainer="Vector", tags = { CoreCommonConstants.SWAGGER_TAG_CLIENT })
	@ApiResponses (value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = CoreCommonConstants.SWAGGER_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = CoreCommonConstants.SWAGGER_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_NOT_FOUND, message = CoreCommonConstants.SWAGGER_HTTP_404_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(value= CommonConstants.OP_DATAMANAGER_HISTORIAN + "/{system}/{service}", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public Vector<SenML> historianServiceGet(
		@PathVariable(value="system", required=true) String systemName,
		@PathVariable(value="service", required=true) String serviceName,
		@RequestParam MultiValueMap<String, String> params
		) {

    if(Utilities.isEmpty(systemName) || Utilities.isEmpty(serviceName)) {
      throw new InvalidParameterException(OP_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, CommonConstants.OP_DATAMANAGER_HISTORIAN);
    }
		logger.debug("historianServiceGet for {}/{}", systemName, serviceName);

		double from=-1, to=-1;
		int count = 1;

		Vector<String> signals = new Vector<String>();
		Vector<Integer> signalCounts = new Vector<Integer>();
		Iterator<String> it = params.keySet().iterator();
		int signalCountId = 0;
		while(it.hasNext()){
			String par = it.next();
			if (par.equals("count")) {
				try {
					count = Integer.parseInt(params.getFirst(par));
				} catch(NumberFormatException nfe) {
					throw new InvalidParameterException(OP_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, CommonConstants.OP_DATAMANAGER_HISTORIAN + "/" + systemName + "/" + serviceName);
				}
			} else if (par.equals("sig"+signalCountId)) {
				signals.add(params.getFirst(par));
        		int signalXCount = 1;
        		if (params.getFirst("sig"+signalCountId+"count") != null) {
          			try {
            			signalXCount = Integer.parseInt(params.getFirst("sig"+signalCountId+"count"));
          			} catch(NumberFormatException nfe) {
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
				}  catch(NumberFormatException nfe) {
					throw new InvalidParameterException(OP_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, CommonConstants.OP_DATAMANAGER_HISTORIAN + "/" + systemName + "/" + serviceName);
				}
			} else if (par.equals("to")) {
				try {
					to = Double.parseDouble(params.getFirst(par));
				}  catch(NumberFormatException nfe) {
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
	@ApiOperation(value = "Interface to put sensor data from a service", tags = { CoreCommonConstants.SWAGGER_TAG_CLIENT })
	@ApiResponses (value = {
		@ApiResponse(code = HttpStatus.SC_OK, message = CoreCommonConstants.SWAGGER_HTTP_200_MESSAGE),
		@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = CoreCommonConstants.SWAGGER_HTTP_400_MESSAGE),
		@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
		@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@PutMapping(value= CommonConstants.OP_DATAMANAGER_HISTORIAN + "/{systemName}/{serviceName}", consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public void historianServicePut(
	@PathVariable(value="systemName", required=true) String systemName,
	@PathVariable(value="serviceName", required=true) String serviceName,
	@RequestBody Vector<SenML> message
	) {
        if(Utilities.isEmpty(systemName) || Utilities.isEmpty(serviceName)) {
            throw new InvalidParameterException(OP_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, CommonConstants.OP_DATAMANAGER_HISTORIAN);
        }
		logger.debug("historianServicePut for {}/{}", systemName, serviceName);

		validateSenMLMessage(systemName, serviceName, message);

		historianService.createEndpoint(systemName, serviceName);

		SenML head = message.firstElement();
		if(head.getBt() == null) {
			head.setBt((double)System.currentTimeMillis() / 1000);
		}

		validateSenMLContent(message);

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
		logger.debug("proxyGet ...");

		DataManagerSystemsResponseDTO ret = new DataManagerSystemsResponseDTO();

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
	@ResponseBody public DataManagerServicesResponseDTO proxySystemGet(
			@PathVariable(value="systemName", required=true) String systemName
		) {
        if(Utilities.isEmpty(systemName)) {
            throw new InvalidParameterException(OP_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, CommonConstants.OP_DATAMANAGER_PROXY);
        }
		logger.debug("proxySystemGet for {}", systemName);

		DataManagerServicesResponseDTO ret = new DataManagerServicesResponseDTO();
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
	@ResponseBody public Vector<SenML> proxyServiceGet(
			@PathVariable(value="systemName", required=true) String systemName,
			@PathVariable(value="serviceName", required=true) String serviceName
			) {
            if(Utilities.isEmpty(systemName) || Utilities.isEmpty(serviceName)) {
                throw new InvalidParameterException(OP_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, CommonConstants.OP_DATAMANAGER_PROXY);
            }
			logger.debug("proxyServiceGet for {}/{}", systemName, serviceName);

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
	@ResponseBody public void proxyServicePut(
			@PathVariable(value="systemName", required=true) String systemName,
			@PathVariable(value="serviceName", required=true) String serviceName,
			@RequestBody Vector<SenML> message
			) {
            if(Utilities.isEmpty(systemName) || Utilities.isEmpty(serviceName) || message == null) {
                throw new InvalidParameterException(OP_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, CommonConstants.OP_DATAMANAGER_PROXY);
            }
			logger.debug("proxyServicePut for {}/{}", systemName, serviceName);

			validateSenMLMessage(systemName, serviceName, message);

			SenML head = message.firstElement();
			if(head.getBt() == null) {
				head.setBt((double)System.currentTimeMillis() / 1000);
			}

			validateSenMLContent(message);

			final ProxyElement pe = proxyService.getEndpointFromService(systemName, serviceName);
			if (pe == null) {
				final boolean ret = proxyService.addEndpointForService(new ProxyElement(systemName, serviceName));
				if (ret==true){
					proxyService.updateEndpointFromService(systemName, serviceName, message);
				} else { 
					throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR);
				}
			} else {
				proxyService.updateEndpointFromService(systemName, serviceName, message);
			}
		}


	//=================================================================================================
	// assistant methods
	
	
	//=================================================================================================
  private void validateSenMLMessage(String systemName, String serviceName, Vector<SenML> message) {
	try {
    	Assert.notNull(systemName, "systemName is null.");
    	Assert.notNull(serviceName, "serviceName is null.");
    	Assert.notNull(message, "message is null.");
    	Assert.isTrue(!message.isEmpty(), "message is empty");

    	SenML head = (SenML)message.get(0);
		Assert.notNull(head.getBn(), "bn is null.");
	} catch(Exception e){
		throw new BadPayloadException("Missing mandatory field");
	}
  }

  //-------------------------------------------------------------------------------------------------
  public void validateSenMLContent(final Vector<SenML> message) {
	try {

    	/* check that bn, bt and bu are included only once, and in the first object */
    	Iterator<SenML> entry = message.iterator();
    	int bnc=0, btc=0, buc=0;
    	while (entry.hasNext()) {
      		SenML element = entry.next();
      		if (element.getBn() != null) {
        		bnc++;
    	  	}
      		if (element.getBt() != null) {
        		btc++;
      		}
      		if (element.getBu() != null) {
        		buc++;
      		}
    	}

    	/* bu can only exist once. bt can only exist one, bu can exist 0 or 1 times */
    	Assert.isTrue(!(bnc != 1 || btc != 1 || buc > 1), "invalid bn/bt/bu");

    	/* bn must exist in [0] */
    	SenML element = (SenML)message.get(0);
    	Assert.notNull(element.getBn(), "bn is missing");

    	/* bt must exist in [0] */
    	Assert.notNull(element.getBt(), "bt is missing");

    	/* bt cannot be negative */
    	Assert.isTrue(element.getBt() >= 0.0, "a negative base time is not allowed");

    	/* bu must exist in [0], if it exists */
    	Assert.isTrue(!(element.getBu() == null && buc == 1), "invalid use of bu");

    	/* check that v, bv, sv, etc are included only once per object */
    	entry = message.iterator();
    	while (entry.hasNext()) {
      		element = (SenML)entry.next();

      		int valueCount = 0;
      		if (element.getV() != null) {
        		valueCount++;
      		}
      		if (element.getVs() != null) {
        		valueCount++;
      		}
      		if (element.getVd() != null) {
        		valueCount++;
      		}
      		if (element.getVb() != null) {
        		valueCount++;
			}

      		Assert.isTrue(!(valueCount > 1 && element.getS() == null), "too many value tags");
		}
	} catch(Exception e) {
		throw new BadPayloadException("Illegal request");
	}

  }	
}
