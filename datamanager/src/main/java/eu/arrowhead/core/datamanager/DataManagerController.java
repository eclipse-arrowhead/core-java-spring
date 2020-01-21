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
import eu.arrowhead.common.dto.shared.EventPublishRequestDTO;
import eu.arrowhead.common.dto.shared.SubscriptionListResponseDTO;
import eu.arrowhead.common.dto.shared.SubscriptionRequestDTO;
import eu.arrowhead.common.dto.shared.SubscriptionResponseDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
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

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonElement;
import com.google.gson.Gson;

@Api(tags = { CoreCommonConstants.SWAGGER_TAG_ALL })
@CrossOrigin(maxAge = Defaults.CORS_MAX_AGE, allowCredentials = Defaults.CORS_ALLOW_CREDENTIALS, 
allowedHeaders = { HttpHeaders.ORIGIN, HttpHeaders.CONTENT_TYPE, HttpHeaders.ACCEPT, HttpHeaders.AUTHORIZATION }
)
@RestController
@RequestMapping(CommonConstants.DATAMANAGER_URI)
public class DataManagerController {
	
	//=================================================================================================
	// members
	
	/*private static final String PATH_VARIABLE_ID = "id";

	private static final String EVENT_HANDLER_MGMT_URI =  CoreCommonConstants.MGMT_URI + "/subscriptions";
	private static final String EVENTHANLER_BY_ID_MGMT_URI = EVENT_HANDLER_MGMT_URI + "/{" + PATH_VARIABLE_ID + "}";
	
	private static final String GET_EVENT_HANDLER_MGMT_DESCRIPTION = "Return requested Subscription entries by the given parameters";
	private static final String GET_EVENT_HANDLER_MGMT_HTTP_200_MESSAGE = "Subscription entries returned";
	private static final String GET_EVENT_HANDLER_MGMT_HTTP_400_MESSAGE = "Could not retrieve Subscription entries";
	
	private static final String GET_EVENT_HANDLER_BY_ID_MGMT_DESCRIPTION = "Return requested Subscription entry by the given id";
	private static final String GET_EVENT_HANDLER_BY_ID_MGMT_HTTP_200_MESSAGE = "Subscription entriy returned";
	private static final String GET_EVENT_HANDLER_BY_ID_MGMT_HTTP_400_MESSAGE = "Could not retrieve Subscription entry";
	
	private static final String DELETE_EVENT_HANDLER_MGMT_DESCRIPTION = "Delete requested Subscription entry by the given id";
	private static final String DELETE_EVENT_HANDLER_MGMT_HTTP_200_MESSAGE = "Subscription entriy deleted";
	private static final String DELETE_EVENT_HANDLER_MGMT_HTTP_400_MESSAGE = "Could not delete Subscription entry";
	
	private static final String PUT_EVENT_HANDLER_MGMT_DESCRIPTION = "Update requested Subscription entry by the given id and parameters";
	private static final String PUT_EVENT_HANDLER_MGMT_HTTP_200_MESSAGE = "Updated Subscription entry returned";
	private static final String PUT_EVENT_HANDLER_MGMT_HTTP_400_MESSAGE = "Could not update Subscription entry";	
	
	private static final String POST_EVENT_HANDLER_SUBSCRIPTION_DESCRIPTION = "Subcribtion to the events specified in requested Subscription ";
	private static final String POST_EVENT_HANDLER_SUBSCRIPTION_HTTP_200_MESSAGE = "Successful subscription.";
	private static final String POST_EVENT_HANDLER_SUBSCRIPTION_HTTP_400_MESSAGE = "Unsuccessful subscription.";
	
	private static final String DELETE_EVENT_HANDLER_SUBSCRIPTION_DESCRIPTION = "Unsubcribtion from the events specified in requested Subscription ";
	private static final String DELETE_EVENT_HANDLER_SUBSCRIPTION_HTTP_200_MESSAGE = "Successful unsubscription.";
	private static final String DELETE_EVENT_HANDLER_SUBSCRIPTION_HTTP_400_MESSAGE = "Unsuccessful unsubscription.";
	
	private static final String POST_EVENT_HANDLER_PUBLISH_DESCRIPTION = "Publish event"; 
	private static final String POST_EVENT_HANDLER_PUBLISH_HTTP_200_MESSAGE = "Publish event success"; 
	private static final String POST_EVENT_HANDLER_PUBLISH_HTTP_400_MESSAGE = "Publish event not success"; 

	private static final String POST_EVENT_HANDLER_PUBLISH_AUTH_UPDATE_DESCRIPTION = "Publish authorization change event "; 
	private static final String POST_EVENT_HANDLER_PUBLISH_AUTH_UPDATE_HTTP_200_MESSAGE = "Publish authorization change event success"; 
	private static final String POST_EVENT_HANDLER_PUBLISH_AUTH_UPDATE_HTTP_400_MESSAGE = "Publish authorization change event not success"; 
	
	private static final String NULL_PARAMETER_ERROR_MESSAGE = " is null.";
	private static final String NULL_OR_BLANK_PARAMETER_ERROR_MESSAGE = " is null or blank.";
	private static final String ID_NOT_VALID_ERROR_MESSAGE = " Id must be greater than 0. ";
	private static final String WRONG_FORMAT_ERROR_MESSAGE = " is in wrong format. ";*/

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
	@ResponseBody public String historianS(
			) {
		//System.out.println("DataManager::Historian/");
		Gson gson = new Gson();

		ArrayList<String> systems = HistorianService.getSystems();
		JsonObject answer = new JsonObject();
		JsonElement systemlist = gson.toJsonTree(systems);
		answer.add("systems", systemlist);

		String jsonStr = gson.toJson(answer);
		return jsonStr;
	}

	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Interface to get all services that s specific system has active in the Historian service", response = String.class, tags = { CoreCommonConstants.SWAGGER_TAG_CLIENT })
	@ApiResponses (value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = CoreCommonConstants.SWAGGER_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(value= "/historian/{systemName}")
	@ResponseBody public String historianSystemGet(
		@PathVariable(value="systemName", required=true) String systemName
		) {
		System.out.println("DataManager:GET:Historian/"+systemName);
		return historianSystemPut(systemName, "{\"op\": \"list\"}");
	}

	@PutMapping(value= "/historian/{systemName}", consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public String historianSystemPut(
			@PathVariable(value="systemName", required=true) String systemName,
			@RequestBody String requestBody
		) {
		System.out.println("DataManager:PUT:Historian/"+systemName);

		JsonParser parser= new JsonParser();
		JsonObject obj = null;
		try {
			obj = parser.parse(requestBody).getAsJsonObject();
		} catch(Exception je){
			throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, "malformed request");
		}

		String op = obj.get("op").getAsString();
		if(op.equals("list")) {
			//System.out.println("OP: list");
			ArrayList<String> services = HistorianService.getServicesFromSystem(systemName);
			//for (String srv: services) {
			//  System.out.println(":" +srv);
			//}
			Gson gson = new Gson();
			JsonObject answer = new JsonObject();
			JsonElement servicelist = gson.toJsonTree(services);
			answer.add("services", servicelist);
			String jsonStr = gson.toJson(answer);
			System.out.println(jsonStr);

			return jsonStr;
		} else if(op.equals("create")){
			//System.out.println("OP: CREATE");
			String srvName = obj.get("srvName").getAsString();
			String srvType = obj.get("srvType").getAsString();
			//System.out.println("Create SRV: "+srvName+" of type: "+srvType+" for: " + systemName);

			/* check if service already exists */
			ArrayList<String> services = HistorianService.getServicesFromSystem(systemName);
			for (String srv: services) {
				if(srv.equals(srvName)){
					logger.info("  service:" +srv + " already exists");
					Gson gson = new Gson();
					JsonObject answer = new JsonObject();
					answer.addProperty("createResult", "Already exists");
					String jsonStr = gson.toJson(answer);
					throw new ResponseStatusException(org.springframework.http.HttpStatus.CONFLICT, jsonStr);
				}
			}

			/* create the service */
			boolean ret = HistorianService.addServiceForSystem(systemName, srvName, srvType);
			if (ret==true){
				return "{\"x\": 0}"; //Response.status(Status.CREATED).entity("{}").type(MediaType.APPLICATION_JSON).build();
			} else {
				throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, "{\"x\": -1, \"xs\":\"Could not create service\"}");
			}

		}
		return "{\"x\": -1, \"xs\": \"Unknown command\"}"; //make this a real object!
	}

	@GetMapping(value= "/historian/{system}/{service}")//CommonConstants.DM_HISTORIAN_URI)
	@ResponseBody public String historianServiceGet(
		@PathVariable(value="system", required=true) String systemName,
		@PathVariable(value="service", required=true) String serviceName,
		@RequestParam MultiValueMap<String, String> params
		) {
		logger.info("DataManager:Get:Historian/"+systemName+"/"+serviceName);

		int statusCode = 0;
		
		long from=-1, to=-1;
		int count = 1;

		Vector<String> signals = new Vector<String>();
		Iterator<String> it = params.keySet().iterator();
		int sigCnt = 0;
		while(it.hasNext()){
			String par = (String)it.next();
			System.out.println("Key: " + par + " value: " + params.getFirst(par) + "("+("sig"+sigCnt)+")");
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
		logger.info("getData requested with count: " + count);

		Vector<SenML> ret = null;

		if(signals.size() == 0) {
			ret = HistorianService.fetchEndpoint(serviceName, from, to, count, null);
		} else {
			ret = HistorianService.fetchEndpoint(serviceName, from, to, count, signals);
		}

		if (ret == null)
			return "[\"bn\": \""+serviceName+"\"]";

		return ret.toString();
	}

	@PutMapping(value= "/historian/{systemName}/{serviceName}", consumes = MediaType.APPLICATION_JSON_VALUE)//CommonConstants.DM_HISTORIAN_URI)
	@ResponseBody public String historianServicePut(
	@PathVariable(value="systemName", required=true) String systemName,
	@PathVariable(value="serviceName", required=true) String serviceName,
	@RequestBody Vector<SenML> sml
	) {
		System.out.println("DataManager:Put:Historian/"+systemName+"/"+serviceName);
		//Iterator entry = sml.iterator();
		//while (entry.hasNext()) { 
            	  //System.out.println(entry.next().toString()); 
      		//} 

		boolean statusCode = HistorianService.createEndpoint(systemName, serviceName);
		logger.info("Historian PUT for system '"+systemName+"', service '"+serviceName+"'"); 

		if (validateSenML(sml) == false) {
			throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, "Invalid SenML");
		}

		SenML head = sml.firstElement();
		if(head.getBt() == null)
			head.setBt((double)System.currentTimeMillis() / 1000.0);

		String bu = head.getBu();
		for(SenML s: sml) {
			/*if (s.getBu() != null) {
				bu = s.getBu();
			}*/
			//if (s.getU() == null && s.getBu() != null) {
			//	s.setU(bu);
			//}
			//System.out.println("object" + s.toString());
			//if(s.getT() == null)
			//	s.setT(s.getBt());
			//else if (s.getT() < 268435456) //2**28
			//	s.setT(s.getT() + s.getBt()); // if it was a relative ts, make it absolut
		} 
		statusCode = HistorianService.updateEndpoint(serviceName, sml);

		String jsonret = "{\"x\": 0}";
		return jsonret;
	}

	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Test interface for the Proxy service", response = String.class, tags = { CoreCommonConstants.SWAGGER_TAG_CLIENT })
	@ApiResponses (value = {
		@ApiResponse(code = HttpStatus.SC_OK, message = CoreCommonConstants.SWAGGER_HTTP_200_MESSAGE),
		@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
		@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(value= "/proxy", produces=MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public String proxyS() {
		//System.out.println("DataManager::proxy/");
		Gson gson = new Gson();

		List<String> pes = ProxyService.getAllEndpoints();
		JsonObject answer = new JsonObject();
		JsonElement systemlist = gson.toJsonTree(pes);
		answer.add("systems", systemlist);

		String jsonStr = gson.toJson(answer);
		return jsonStr;
	}


	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Interface to get a system's all services in the Proxy service", response = String.class, tags = { CoreCommonConstants.SWAGGER_TAG_CLIENT })
	@ApiResponses (value = {
		@ApiResponse(code = HttpStatus.SC_OK, message = CoreCommonConstants.SWAGGER_HTTP_200_MESSAGE),
		@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
		@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(value= "/proxy/{systemName}", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public String proxySystemGet(
			@PathVariable(value="systemName", required=true) String systemName
		) {
		//System.out.println("DataManager:Get:proxy/"+systemName);

		List<ProxyElement> pes = ProxyService.getEndpoints(systemName);
		if (pes.size() == 0) {
			//logger.debug("proxy GET to systemName: " + systemName + " not found");
			throw new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "System not found");
		}

		ArrayList<String> systems= new ArrayList<String>();
		for (ProxyElement pe: pes) {
			systems.add(pe.serviceName);
		}

		Gson gson = new Gson();
		JsonObject answer = new JsonObject();
		JsonElement servicelist = gson.toJsonTree(systems);
		answer.add("services", servicelist);
		String jsonStr = gson.toJson(answer);
		return jsonStr;
	}

	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Interface to manage a system's services in the Proxy service", response = String.class, tags = { CoreCommonConstants.SWAGGER_TAG_CLIENT })
	@ApiResponses (value = {
		@ApiResponse(code = HttpStatus.SC_OK, message = CoreCommonConstants.SWAGGER_HTTP_200_MESSAGE),
		@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
		@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@PutMapping(value= "/proxy/{systemName}", consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public String proxySystemPut(
			@PathVariable(value="systemName", required=true) String systemName,
			@RequestBody String requestBody
		) {
		System.out.println("DataManager:Put:proxy/"+systemName);
		JsonParser parser= new JsonParser();
		JsonObject obj = null;
		try {
			obj = parser.parse(requestBody).getAsJsonObject();
		} catch(Exception je){
			throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, "malformed request");
		}

		String op = obj.get("op").getAsString();
		if(op.equals("list")){
			List<ProxyElement> pes = ProxyService.getEndpoints(systemName);
			if (pes.size() == 0) {
				logger.debug("proxy GET to systemName: " + systemName + " not found");
				throw new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "System not found");
			}

			ArrayList<String> systems= new ArrayList<String>();
			for (ProxyElement pe: pes) {
				systems.add(pe.serviceName);
			}

			Gson gson = new Gson();
			JsonObject answer = new JsonObject();
			JsonElement servicelist = gson.toJsonTree(systems);
			answer.add("services", servicelist);
			String jsonStr = gson.toJson(answer);
			return jsonStr;
		} else if(op.equals("create")){
			String srvName = obj.get("srvName").getAsString();
			String srvType = obj.get("srvType").getAsString();
			logger.info("Create Service: "+srvName+" of type: "+srvType+" for: " + systemName);

			/* check if service already exists */
			ArrayList<ProxyElement> services = ProxyService.getEndpoints(systemName);
			for (ProxyElement srv: services) {
				logger.info("PE: " + srv.serviceName);
				if(srv.serviceName.equals(srvName)){
					Gson gson = new Gson();
					JsonObject answer = new JsonObject();
					answer.addProperty("createResult", "Already exists");
					String jsonStr = gson.toJson(answer);
					//return Response.status(Status.CONFLICT).entity(jsonStr).type(MediaType.APPLICATION_JSON).build();
					throw new ResponseStatusException(org.springframework.http.HttpStatus.CONFLICT, "createResult: Already exists");
				}
			}
			//logger.info("Not found, create it: " + srvName);

			/* create the service */
			boolean ret = ProxyService.addEndpoint(new ProxyElement(systemName, srvName));
			if (ret==true){
				throw new ResponseStatusException(org.springframework.http.HttpStatus.CREATED, "createResult: Created");
			} else { 
				throw new ResponseStatusException(org.springframework.http.HttpStatus.CONFLICT, "createResult: Already exists");
			}
		} else if(op.equals("delete")){ //NOT SUPPORTED YET
			String srvName = obj.get("srvName").getAsString();
			String srvType = obj.get("srvType").getAsString();
			logger.info("Delete Service: "+srvName+" of type: "+srvType+" for: " + systemName);

			/* check if service already exists */
		}

		return "";
		}

	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Interface to get a system's last service data", response = String.class, tags = { CoreCommonConstants.SWAGGER_TAG_CLIENT })
	@ApiResponses (value = {
		@ApiResponse(code = HttpStatus.SC_OK, message = CoreCommonConstants.SWAGGER_HTTP_200_MESSAGE),
		@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
		@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(value= "/proxy/{systemName}/{serviceName}")//CommonConstants.DM_PROXY_URI)
	@ResponseBody public String proxyServiceGet(
			@PathVariable(value="systemName", required=true) String systemName,
			@PathVariable(value="serviceName", required=true) String serviceName
			) {
			System.out.println("DataManager:Get:Proxy/"+systemName+"/"+serviceName);

			int statusCode = 0;
			ProxyElement pe = ProxyService.getEndpoint(serviceName);
			if (pe == null) {
				logger.info("proxy GET to serviceName: " + serviceName + " not found");
				//return Response.status(Status.NOT_FOUND).build();
				throw new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Service not found");
			}

			Iterator i = pe.msg.iterator();
			String senml = "";
			/*while (i.hasNext()) {
				senml += ((i.next().toString())+"\n");
				System.out.println("\t"+senml);
			}*/
			return pe.msg.toString();
			}

	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Interface to update a system's last service data", response = String.class, tags = { CoreCommonConstants.SWAGGER_TAG_CLIENT })
	@ApiResponses (value = {
		@ApiResponse(code = HttpStatus.SC_OK, message = CoreCommonConstants.SWAGGER_HTTP_200_MESSAGE),
		@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
		@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@PutMapping(value= "/proxy/{systemName}/{serviceName}")//CommonConstants.DM_PROXY_URI)
	@ResponseBody public String proxyPut(
			@PathVariable(value="systemName", required=true) String systemName,
			@PathVariable(value="serviceName", required=true) String serviceName,
			@RequestBody Vector<SenML> sml
			) {
		ProxyElement pe = ProxyService.getEndpoint(serviceName);
		if (pe == null) {
			throw new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Service not found");
		}

		if (validateSenML(sml) == false) {
			throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, "Invalid SenML");
		}

		boolean statusCode = ProxyService.updateEndpoint(systemName, serviceName, sml);
		//logger.info("putData/SenML returned with status code: " + statusCode + " from: " + sml.get(0).getBn() + " at: " + sml.get(0).getBt());

		int ret = 0;
		if (statusCode == false)
			ret = 1;
		String jsonret = "{\"rc\": "+ret+"}";
		return jsonret;
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

	System.out.println("bnc: "+bnc+", btc: "+btc+", buc: "+buc);

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
	    System.out.println(o.toString()); 

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

