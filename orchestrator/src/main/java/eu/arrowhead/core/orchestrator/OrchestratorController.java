package eu.arrowhead.core.orchestrator;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.Defaults;
import eu.arrowhead.common.dto.OrchestratorFormRequestDTO;
import eu.arrowhead.common.dto.OrchestratorFormResponseDTO;
import eu.arrowhead.common.dto.ServiceRegistryListResponseDTO;
import eu.arrowhead.common.exception.BadPayloadException;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@CrossOrigin(maxAge = Defaults.CORS_MAX_AGE, allowCredentials = Defaults.CORS_ALLOW_CREDENTIALS, 
allowedHeaders = { HttpHeaders.ORIGIN, HttpHeaders.CONTENT_TYPE, HttpHeaders.ACCEPT, HttpHeaders.AUTHORIZATION }
)
@RestController
@RequestMapping(CommonConstants.ORCHESTRATOR_URI)
public class OrchestratorController {

	//=================================================================================================
	// members
	
	private static final String ECHO_URI = "/echo";
	
	private static final String POST_ORCHESTRATOR_HTTP_200_MESSAGE = "OrchestratorStores by requested parameters modified";
	private static final String POST_ORCHESTRATOR_HTTP_400_MESSAGE = "Could not modify OrchestratorStore by requested parameters";
	
	private static final String ID_NOT_VALID_ERROR_MESSAGE = "Id must be greater than 0. ";
	private static final String NULL_PARAMETERS_ERROR_MESSAGE = " is null.";
	private static final String EMPTY_PARAMETERS_ERROR_MESSAGE = " is empty.";
	private static final String GATEKEEPER_IS_NOT_PRESENT_ERROR_MESSAGE = " can not be served. Orchestrator runs in NO GATEKEEPER mode.";
	
	private final Logger logger = LogManager.getLogger(OrchestratorController.class);
	
	@Value(CommonConstants.$IS_GATEKEEPER_PRESENT_WD)
	private boolean gateKeeperIsPresent;
	
	@Autowired
	private OrchestratorService orchestratorService;
	
	//=================================================================================================
	// methods

	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return an echo message with the purpose of testing the core service availability", response = String.class)
	@ApiResponses (value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = CommonConstants.SWAGGER_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path = ECHO_URI)
	public String echoService() {
		return "Got it!";
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Start Orchestration prosess.", response = OrchestratorFormResponseDTO.class)
	@ApiResponses (value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = POST_ORCHESTRATOR_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = POST_ORCHESTRATOR_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public ServiceRegistryListResponseDTO orchestrationProcess( @RequestBody final OrchestratorFormRequestDTO request) {
		logger.debug("orchestrationProcess started ...");
		
		final OrchestratorFormRequestDTO validatedOrchestratorFormRequestDTO = validateOrchestratorFormRequestDTO(request, CommonConstants.ORCHESTRATOR_URI);
		
	    if (validatedOrchestratorFormRequestDTO.getOrchestrationFlags().getOrDefault("externalServiceRequest", false)) {
	      
	    	if (!gateKeeperIsPresent) {
	    		throw new BadPayloadException("ExternalServiceRequest " + GATEKEEPER_IS_NOT_PRESENT_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, CommonConstants.ORCHESTRATOR_URI);
			}
	    	
	    	return orchestratorService.externalServiceRequest(validatedOrchestratorFormRequestDTO);
	    
	    } else if (validatedOrchestratorFormRequestDTO.getOrchestrationFlags().getOrDefault("triggerInterCloud", false)) {
	      
	    	if (!gateKeeperIsPresent) {
	    		throw new BadPayloadException("TriggerInterCloud " + GATEKEEPER_IS_NOT_PRESENT_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, CommonConstants.ORCHESTRATOR_URI);
			}
	    	
	    	return orchestratorService.triggerInterCloud(validatedOrchestratorFormRequestDTO);
	    
	    } else if (!validatedOrchestratorFormRequestDTO.getOrchestrationFlags().getOrDefault("overrideStore", false)) { //overrideStore == false
	      
	    	return orchestratorService.orchestrationFromStore(validatedOrchestratorFormRequestDTO);
	    
	    } else {
	      
	    	return orchestratorService.dynamicOrchestration(validatedOrchestratorFormRequestDTO);
	    }
		
	}

	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------	
	private OrchestratorFormRequestDTO validateOrchestratorFormRequestDTO(final OrchestratorFormRequestDTO request, final String origin) {
		
		if (request == null) {
			throw new BadPayloadException("Request "+ NULL_PARAMETERS_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		if (request.getRequesterSystem() == null) {
			throw new BadPayloadException("RequesterSystem "+ NULL_PARAMETERS_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		final OrchestratorFormRequestDTO validOrchestratorFormRequestDTO = new OrchestratorFormRequestDTO();
		
		validOrchestratorFormRequestDTO.setRequesterSystem(request.getRequesterSystem());
		
		//TODO Implement additional validation here
		
		
		return validOrchestratorFormRequestDTO;
	}
}