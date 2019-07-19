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
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.CloudRequestDTO;
import eu.arrowhead.common.dto.OrchestrationFlags.Flag;
import eu.arrowhead.common.dto.OrchestrationFormRequestDTO;
import eu.arrowhead.common.dto.OrchestrationResponseDTO;
import eu.arrowhead.common.dto.PreferredProviderDataDTO;
import eu.arrowhead.common.dto.SystemRequestDTO;
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
	
	private static final String POST_ORCHESTRATOR_HTTP_200_MESSAGE = "OrchestratorStores by requested parameters modified";
	private static final String POST_ORCHESTRATOR_HTTP_400_MESSAGE = "Could not modify OrchestratorStore by requested parameters";
	
	private static final String NULL_PARAMETER_ERROR_MESSAGE = " is null.";
	private static final String NULL_OR_BLANK_PARAMETER_ERROR_MESSAGE = " is null or blank.";
	private static final String GATEKEEPER_IS_NOT_PRESENT_ERROR_MESSAGE = " can not be served. Orchestrator runs in NO GATEKEEPER mode.";
	
	private final Logger logger = LogManager.getLogger(OrchestratorController.class);
	
	@Value(CommonConstants.$ORCHESTRATOR_IS_GATEKEEPER_PRESENT_WD)
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
	@GetMapping(path = CommonConstants.ECHO_URI)
	public String echoService() {
		return "Got it!";
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Start Orchestration process.", response = OrchestrationResponseDTO.class)
	@ApiResponses (value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = POST_ORCHESTRATOR_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = POST_ORCHESTRATOR_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@PostMapping(path = CommonConstants.OP_ORCH_PROCESS, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public OrchestrationResponseDTO orchestrationProcess(@RequestBody final OrchestrationFormRequestDTO request) {
		logger.debug("orchestrationProcess started ...");
		
		final String origin = CommonConstants.ORCHESTRATOR_URI + CommonConstants.OP_ORCH_PROCESS;
		checkOrchestratorFormRequestDTO(request, origin);
		
	    if (request.getOrchestrationFlags().getOrDefault(Flag.EXTERNAL_SERVICE_REQUEST, false)) {
	    	if (!gateKeeperIsPresent) {
	    		throw new BadPayloadException("External service request" + GATEKEEPER_IS_NOT_PRESENT_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
			}
	    	
	    	return orchestratorService.externalServiceRequest(request);
	    } else if (request.getOrchestrationFlags().getOrDefault(Flag.TRIGGER_INTER_CLOUD, false)) {
	    	if (!gateKeeperIsPresent) {
	    		throw new BadPayloadException("Forced inter cloud service request" + GATEKEEPER_IS_NOT_PRESENT_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
			}
	    	
	    	return orchestratorService.triggerInterCloud(request);
	    } else if (!request.getOrchestrationFlags().getOrDefault(Flag.OVERRIDE_STORE, false)) { // overrideStore == false
	    	return orchestratorService.orchestrationFromStore(request);
	    } else {
	    	return orchestratorService.dynamicOrchestration(request);
	    }
	}

	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------	
	private void checkOrchestratorFormRequestDTO(final OrchestrationFormRequestDTO request, final String origin) {
		if (request == null) {
			throw new BadPayloadException("Request" + NULL_PARAMETER_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		request.validateCrossParameterConstraints();

		// Requester system
		checkSystemRequestDTO(request.getRequesterSystem(), origin);

		// Requester cloud
		if (request.getRequesterCloud() != null) {
			checkCloudRequestDTO(request.getRequesterCloud(), origin);
		}
		
		// Requested service
		if (request.getRequestedService() != null && Utilities.isEmpty(request.getRequestedService().getServiceDefinitionRequirement())) {
			throw new BadPayloadException("Requested service definition requirement" + NULL_OR_BLANK_PARAMETER_ERROR_MESSAGE , HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		// Preferred Providers
		if (request.getPreferredProviders() != null) {
			for (final PreferredProviderDataDTO provider : request.getPreferredProviders()) {
				checkSystemRequestDTO(provider.getProviderSystem(), origin);
				if (provider.getProviderCloud() != null) {
					checkCloudRequestDTO(provider.getProviderCloud(), origin);
				}
			}
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private void checkSystemRequestDTO(final SystemRequestDTO system, final String origin) {
		logger.debug("checkSystemRequestDTO started...");
		
		if (system == null) {
			throw new BadPayloadException("System" + NULL_PARAMETER_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		if (Utilities.isEmpty(system.getSystemName())) {
			throw new BadPayloadException("System name" + NULL_OR_BLANK_PARAMETER_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		if (Utilities.isEmpty(system.getAddress())) {
			throw new BadPayloadException("System address" + NULL_OR_BLANK_PARAMETER_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		if (system.getPort() == null) {
			throw new BadPayloadException("System port" + NULL_PARAMETER_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		final int validatedPort = system.getPort().intValue();
		if (validatedPort < CommonConstants.SYSTEM_PORT_RANGE_MIN || validatedPort > CommonConstants.SYSTEM_PORT_RANGE_MAX) {
			throw new BadPayloadException("System port must be between " + CommonConstants.SYSTEM_PORT_RANGE_MIN + " and " + CommonConstants.SYSTEM_PORT_RANGE_MAX + ".",
										  HttpStatus.SC_BAD_REQUEST, origin);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private void checkCloudRequestDTO(final CloudRequestDTO cloud, final String origin) {
		logger.debug("checkCloudRequestDTO started...");
		
		if (cloud == null) {
			throw new BadPayloadException("Cloud" + NULL_PARAMETER_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		if (Utilities.isEmpty(cloud.getOperator())) {
			throw new BadPayloadException("Cloud operator" + NULL_OR_BLANK_PARAMETER_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		if (Utilities.isEmpty(cloud.getName())) {
			throw new BadPayloadException("Cloud name" + NULL_OR_BLANK_PARAMETER_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
		}
	}
}