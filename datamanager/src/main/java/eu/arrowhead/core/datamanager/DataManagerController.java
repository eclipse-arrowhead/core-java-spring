package eu.arrowhead.core.datamanager;

import java.util.*; 
import java.util.Vector;

import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
//import org.springframework.http.HttpStatus;
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

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.CoreDefaults;
import eu.arrowhead.common.CoreUtilities;
import eu.arrowhead.common.CoreUtilities.ValidatedPageParams;
import eu.arrowhead.common.Defaults;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.shared.SenML;
//import eu.arrowhead.common.dto.shared.SigML;
import eu.arrowhead.common.dto.shared.DataManagerSystemsResponseDTO;
import eu.arrowhead.common.dto.shared.DataManagerServicesResponseDTO;
import eu.arrowhead.common.dto.shared.DataManagerOperationDTO;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.core.datamanager.database.service.DataManagerDBService;
import eu.arrowhead.core.datamanager.service.DataManagerService;
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
	private final Logger logger = LogManager.getLogger(DataManagerController.class);
	
	@Autowired
	DataManagerService dataManagerService;
	
	@Autowired
	ProxyService proxyService;

	@Autowired
	HistorianService historianService;

	@Autowired
	DataManagerDBService dataManagerDBService;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return an echo message with the purpose of testing the core service availability", response = String.class, tags = { CoreCommonConstants.SWAGGER_TAG_CLIENT })
	@ApiResponses (value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = CoreCommonConstants.SWAGGER_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path = CommonConstants.ECHO_URI)
	@ResponseBody public String echoService() {
		return "Got it!";
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Test interface to the Historian service", response = String.class, tags = { CoreCommonConstants.SWAGGER_TAG_CLIENT })
	@ApiResponses (value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = CoreCommonConstants.SWAGGER_HTTP_200_MESSAGE)
	})
	@GetMapping(value= "/historian")
	@ResponseBody public DataManagerSystemsResponseDTO historianSystems(
			) {
		DataManagerSystemsResponseDTO ret = new DataManagerSystemsResponseDTO();

		ArrayList<String> systems = historianService.getSystems();
		ret.setSystems(systems);
		return ret;
	}

	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Interface to get all services that s specific system has active in the Historian service", response = String.class, tags = { CoreCommonConstants.SWAGGER_TAG_CLIENT })
	@ApiResponses (value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = CoreCommonConstants.SWAGGER_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(value= "/historian/{systemName}")
	@ResponseBody public DataManagerServicesResponseDTO historianSystemGet(
		@PathVariable(value="systemName", required=true) String systemName
		) {
		logger.debug("DataManager:GET:Historian/"+systemName);

		DataManagerServicesResponseDTO ret = new DataManagerServicesResponseDTO();
		ArrayList<String> services = historianService.getServicesFromSystem(systemName);
		ret.setServices(services);

		return ret;
	}

	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Interface to interact with system object that is active in the Historian service", response = String.class, tags = { CoreCommonConstants.SWAGGER_TAG_CLIENT })
	@ApiResponses (value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = CoreCommonConstants.SWAGGER_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@PutMapping(value= "/historian/{systemName}", consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public ResponseEntity<DataManagerServicesResponseDTO> historianSystemPut(
			@PathVariable(value="systemName", required=true) String systemName,
			@RequestBody DataManagerOperationDTO req
		) {
		logger.debug("DataManager:PUT:Historian/"+systemName);

		String op = req.getOp();
		if(op.equals("list")) {
			DataManagerServicesResponseDTO ret = new DataManagerServicesResponseDTO();
			ArrayList<String> services = historianService.getServicesFromSystem(systemName);
			ret.setServices(services);
			return new ResponseEntity<DataManagerServicesResponseDTO>(ret, org.springframework.http.HttpStatus.OK);
		}
		return new ResponseEntity<DataManagerServicesResponseDTO>(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR);
	}

	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Interface to get sensor data from a service", response = String.class, tags = { CoreCommonConstants.SWAGGER_TAG_CLIENT })
	@ApiResponses (value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = CoreCommonConstants.SWAGGER_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_NOT_FOUND, message = CoreCommonConstants.SWAGGER_HTTP_404_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(value= "/historian/{system}/{service}")
	@ResponseBody public List<SenML> historianServiceGet(
		@PathVariable(value="system", required=true) String systemName,
		@PathVariable(value="service", required=true) String serviceName,
		@RequestParam MultiValueMap<String, String> params
		) {
		//logger.info("DataManager:Get:Historian/"+systemName+"/"+serviceName);

		int statusCode = 0;
		
		long from=-1, to=-1;
		int count = 1;

		Vector<String> signals = new Vector<String>();
		Iterator<String> it = params.keySet().iterator();
		int sigCnt = 0;
		while(it.hasNext()){
			String par = (String)it.next();
			if (par.equals("count")) {
				count = Integer.parseInt(params.getFirst(par));
			} else if (par.equals("sig"+sigCnt)) {
				signals.add(params.getFirst(par));
				sigCnt++;
			} else if (par.equals("from")) {
				from = Long.parseLong(params.getFirst(par));
			} else if (par.equals("to")) {
				to = Long.parseLong(params.getFirst(par));
			}
		}
		//logger.info("getData requested with count: " + count);

		List<SenML> ret = null;

		if(signals.size() == 0) {
			ret = historianService.fetchEndpoint(systemName, serviceName, from, to, count, null);
		} else {
			ret = historianService.fetchEndpoint(systemName, serviceName, from, to, count, signals);
		}

		if (ret == null)
			throw new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND);

		return ret;
	}


	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Interface to put sensor data from a service", response = String.class, tags = { CoreCommonConstants.SWAGGER_TAG_CLIENT })
	@ApiResponses (value = {
		@ApiResponse(code = HttpStatus.SC_OK, message = CoreCommonConstants.SWAGGER_HTTP_200_MESSAGE),
		@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
		@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@PutMapping(value= "/historian/{systemName}/{serviceName}", consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public void historianServicePut(
	@PathVariable(value="systemName", required=true) String systemName,
	@PathVariable(value="serviceName", required=true) String serviceName,
	@RequestBody Vector<SenML> sml
	) {
		logger.debug("DataManager:Put:Historian/"+systemName+"/"+serviceName);

		if (validateSenML(sml) == false) {
			throw new ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST);
		}

		historianService.createEndpoint(systemName, serviceName);

		SenML head = sml.firstElement();
		if(head.getBt() == null)
			head.setBt((double)System.currentTimeMillis() / 1000);

		boolean statusCode = historianService.updateEndpoint(systemName, serviceName, sml);
		if (statusCode == false)
			throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR);
	}


	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Start interface for the Proxy service", response = String.class, tags = { CoreCommonConstants.SWAGGER_TAG_CLIENT })
	@ApiResponses (value = {
		@ApiResponse(code = HttpStatus.SC_OK, message = CoreCommonConstants.SWAGGER_HTTP_200_MESSAGE),
		@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
		@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(value= "/proxy", produces=MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public DataManagerSystemsResponseDTO proxyServicesGet() {
		DataManagerSystemsResponseDTO ret = new DataManagerSystemsResponseDTO();

		List<String> systems = proxyService.getAllSystems();
		ret.setSystems(systems);

		return ret;
	}


	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Start interface for the Proxy service", response = String.class, tags = { CoreCommonConstants.SWAGGER_TAG_CLIENT })
	@ApiResponses (value = {
		@ApiResponse(code = HttpStatus.SC_OK, message = CoreCommonConstants.SWAGGER_HTTP_200_MESSAGE),
		@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
		@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@PutMapping(value= "/proxy", produces=MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public ResponseEntity<DataManagerSystemsResponseDTO> proxySystemsPut(
			@RequestBody DataManagerOperationDTO req
		) {
		logger.debug("DataManager:PUT:Proxy");
		String op = req.getOp();
		if(op.equals("list")) {
			DataManagerSystemsResponseDTO ret = new DataManagerSystemsResponseDTO();
			List<String> systems = proxyService.getAllSystems();
			ret.setSystems(systems);
			return new ResponseEntity<DataManagerSystemsResponseDTO>(ret, org.springframework.http.HttpStatus.OK);
		} else if (op.equals("delete")) {
		}

		throw new ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST);
	}


	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Interface to get a system's all services in the Proxy service", response = String.class, tags = { CoreCommonConstants.SWAGGER_TAG_CLIENT })
	@ApiResponses (value = {
		@ApiResponse(code = HttpStatus.SC_OK, message = CoreCommonConstants.SWAGGER_HTTP_200_MESSAGE),
		@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
		@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(value= "/proxy/{systemName}", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public DataManagerServicesResponseDTO proxySystemGet(
			@PathVariable(value="systemName", required=true) String systemName
		) {

		DataManagerServicesResponseDTO ret = new DataManagerServicesResponseDTO();
		ArrayList<String> services = proxyService.getEndpointsNames(systemName);
		ret.setServices(services);
		return ret;
	}

	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Interface to manage a system's services in the Proxy service", response = String.class, tags = { CoreCommonConstants.SWAGGER_TAG_CLIENT })
	@ApiResponses (value = {
		@ApiResponse(code = HttpStatus.SC_OK, message = CoreCommonConstants.SWAGGER_HTTP_200_MESSAGE),
		@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
		@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@PutMapping(value= "/proxy/{systemName}", consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public DataManagerServicesResponseDTO proxySystemPut(
			@PathVariable(value="systemName", required=true) String systemName,
			@RequestBody DataManagerOperationDTO req
		) {

		String op = req.getOp();
		if(op.equals("list")){
			ArrayList<String> services = proxyService.getEndpointsNames(systemName);

			DataManagerServicesResponseDTO ret = new DataManagerServicesResponseDTO();
			ret.setServices(services);
			return ret;
		} else if(op.equals("delete")) {
			String serviceName = req.getServiceName();
			String serviceType = req.getServiceType();
			logger.info("Delete Service: "+serviceName+" of type: "+serviceType+" for: " + systemName);
			proxyService.deleteEndpoint(serviceName);
			throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR);
		}

		throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR);
		}

	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Interface to get a system's last service data", response = String.class, tags = { CoreCommonConstants.SWAGGER_TAG_CLIENT })
	@ApiResponses (value = {
		@ApiResponse(code = HttpStatus.SC_OK, message = CoreCommonConstants.SWAGGER_HTTP_200_MESSAGE),
		@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE)
	})
	@GetMapping(value= "/proxy/{systemName}/{serviceName}")
	@ResponseBody public Vector<SenML> proxyServiceGet(
			@PathVariable(value="systemName", required=true) String systemName,
			@PathVariable(value="serviceName", required=true) String serviceName
			) {
			int statusCode = 0;

			ProxyElement pe = proxyService.getEndpoint(serviceName);
			if (pe == null) {
				logger.info("proxy GET to serviceName: " + serviceName + " not found");
				throw new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND);
			}

			return pe.msg;
			}

	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Interface to update a system's last data", response = String.class, tags = { CoreCommonConstants.SWAGGER_TAG_CLIENT })
	@ApiResponses (value = {
		@ApiResponse(code = HttpStatus.SC_OK, message = CoreCommonConstants.SWAGGER_HTTP_200_MESSAGE),
		@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = CoreCommonConstants.SWAGGER_HTTP_400_MESSAGE),
		@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
		@ApiResponse(code = HttpStatus.SC_CONFLICT, message = CoreCommonConstants.SWAGGER_HTTP_409_MESSAGE),
		@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@PutMapping(value= "/proxy/{systemName}/{serviceName}")
	@ResponseBody public void proxyPut(
			@PathVariable(value="systemName", required=true) String systemName,
			@PathVariable(value="serviceName", required=true) String serviceName,
			@RequestBody Vector<SenML> sml
			) {
		if (validateSenML(sml) == false) {
			throw new ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST);
		}

		ProxyElement pe = proxyService.getEndpoint(serviceName);
		if (pe == null) {
			boolean ret = proxyService.addEndpoint(new ProxyElement(systemName, serviceName));
			if (ret==true){
				proxyService.updateEndpoint(systemName, serviceName, sml);
				throw new ResponseStatusException(org.springframework.http.HttpStatus.CREATED);
			} else { 
				throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} else {
			proxyService.updateEndpoint(systemName, serviceName, sml);
		}
	}


	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	public boolean validateSenML(final Vector<SenML> sml){

	  /* check that bn, bt and bu are included only once, and in the first object */
	  Iterator entry = sml.iterator();
	  int bnc=0, btc=0, buc=0;
	  while (entry.hasNext()) {
	    SenML o = (SenML)entry.next();
	    if (o.getBn() != null)
	      bnc++;
	    if (o.getBt() != null)
	      btc++;
	    if (o.getBu() != null)
	      buc++;
	  }


	  /* bu can only exist once. bt can only exist one, bu can exist 0 or 1 times */
	  if (bnc != 1 || btc != 1 || buc > 1)
		  return false;

	  /* bn must exist in [0] */
	  SenML o = (SenML)sml.get(0);
	  if (o.getBn() == null)
		  return false;

	  /* bt must exist in [0] */
	  if (o.getBt() == null)
		  return false;

	  /* bu must exist in [0], if it exists */
	  if (o.getBu() == null && buc == 1)
		  return false;

	  /* check that v, bv, sv, etc are included only once per object */
	  entry = sml.iterator();
	  while (entry.hasNext()) {
	    o = (SenML)entry.next();

	    int value_count = 0;
	    if (o.getV() != null)
	      value_count++;
	    if (o.getVs() != null)
	      value_count++;
	    if (o.getVd() != null)
	      value_count++;
	    if (o.getVb() != null)
	      value_count++;

	    if(value_count > 1 && o.getS() == null)
	      return false;
	  } 

	  return true;
	}	
}

