package eu.arrowhead.core.orchestrator;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.Defaults;
import eu.arrowhead.common.dto.OrchestratorStoreResponseDTO;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.core.orchestrator.database.service.OrchestratorStoreDBService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@CrossOrigin(maxAge = Defaults.CORS_MAX_AGE, allowCredentials = Defaults.CORS_ALLOW_CREDENTIALS, 
allowedHeaders = { HttpHeaders.ORIGIN, HttpHeaders.CONTENT_TYPE, HttpHeaders.ACCEPT, HttpHeaders.AUTHORIZATION }
)
@RestController
@RequestMapping(CommonConstants.ORCHESTRATOR_STORE_MGMT_URI)
public class OrchestratorStoreController {

	//=================================================================================================
	// members
	
	private static final String ECHO_URI = "/echo";
	
	private static final String PATH_VARIABLE_ID = "id";
	private static final String ORCHESTRATOR_STORE_MGMT_BY_ID_URI = CommonConstants.ORCHESTRATOR_STORE_MGMT_URI + "/{" + PATH_VARIABLE_ID + "}";
	private static final String GET_ORCHESTRATOR_STORE_MGMT_BY_ID_HTTP_200_MESSAGE = "System by requested id returned";
	private static final String GET_ORCHESTRATOR_STORE_MGMT_BY_ID_HTTP_400_MESSAGE = "No Such System by requested id";
	
	private static final String NOT_VALID_PARAMETERS_ERROR_MESSAGE = "Not valid request parameters.";
	private static final String ID_NOT_VALID_ERROR_MESSAGE = "Id must be greater than 0. ";
	
	private final Logger logger = LogManager.getLogger(OrchestratorStoreController.class);
	
	@Autowired
	private OrchestratorStoreDBService orchestratorStoreDBService;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return an echo message with the purpose of testing the core service availability", response = String.class)
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = CommonConstants.SWAGGER_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path = ECHO_URI)
	public String echoService() {
		return "Got it!";
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return OrchestratorStore entry by id", response = OrchestratorStoreResponseDTO.class)
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = GET_ORCHESTRATOR_STORE_MGMT_BY_ID_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = GET_ORCHESTRATOR_STORE_MGMT_BY_ID_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(ORCHESTRATOR_STORE_MGMT_BY_ID_URI)
	@ResponseBody public OrchestratorStoreResponseDTO getOrchestratorStoreById(@PathVariable(value = PATH_VARIABLE_ID) final long orchestratorStoreId) {		
		logger.debug("getOrchestratorStoreById started ...");
		
		if (orchestratorStoreId < 1) {
			throw new BadPayloadException(ID_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, ORCHESTRATOR_STORE_MGMT_BY_ID_URI);
		}
		
		return orchestratorStoreDBService.getOrchestratorStoreById(orchestratorStoreId);			
	}
}
