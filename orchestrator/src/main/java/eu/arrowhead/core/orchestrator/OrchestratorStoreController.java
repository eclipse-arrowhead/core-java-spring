package eu.arrowhead.core.orchestrator;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.Defaults;
import eu.arrowhead.common.dto.OrchestratorStoreListResponseDTO;
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
@RequestMapping(CommonConstants.ORCHESTRATOR_URI)
public class OrchestratorStoreController {

	//=================================================================================================
	// members
	
	private static final String ECHO_URI = "/echo";
	
	private static final String PATH_VARIABLE_ID = "id";
	private static final String ORCHESTRATOR_STORE_MGMT_BY_ID_URI = CommonConstants.ORCHESTRATOR_STORE_MGMT_URI + "/{" + PATH_VARIABLE_ID + "}";
	private static final String ORCHESTRATOR_STORE_MGMT_ALL_TOP_PRIORITY = CommonConstants.ORCHESTRATOR_STORE_MGMT_URI + "/all_topPriority";
	private static final String GET_ORCHESTRATOR_STORE_MGMT_BY_ID_HTTP_200_MESSAGE = "System by requested id returned";
	private static final String GET_ORCHESTRATOR_STORE_MGMT_BY_ID_HTTP_400_MESSAGE = "No Such System by requested id";
	private static final String GET_ORCHESTRATOR_STORE_MGMT_HTTP_200_MESSAGE = "Systems by requested parameters returned";
	private static final String GET_ORCHESTRATOR_STORE_MGMT_HTTP_400_MESSAGE = "No Such System by requested parameters";
	
	private static final String NOT_VALID_PARAMETERS_ERROR_MESSAGE = "Not valid request parameters.";
	private static final String ID_NOT_VALID_ERROR_MESSAGE = "Id must be greater than 0. ";
	
	private final Logger logger = LogManager.getLogger(OrchestratorStoreController.class);
	
	@Value(CommonConstants.$IS_GATEKEEPER_PRESENT_WD)
	private boolean gateKeeperIsPresent;
	
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
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return requested OrchestratorStore entries by the given parameters", response = OrchestratorStoreListResponseDTO.class)
	@ApiResponses (value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = GET_ORCHESTRATOR_STORE_MGMT_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = GET_ORCHESTRATOR_STORE_MGMT_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path = CommonConstants.ORCHESTRATOR_STORE_MGMT_URI, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public OrchestratorStoreListResponseDTO getOrchestratorStores(
			@RequestParam(name = CommonConstants.REQUEST_PARAM_PAGE, required = false) final Integer page,
			@RequestParam(name = CommonConstants.REQUEST_PARAM_ITEM_PER_PAGE, required = false) final Integer size,
			@RequestParam(name = CommonConstants.REQUEST_PARAM_DIRECTION, defaultValue = Defaults.DEFAULT_REQUEST_PARAM_DIRECTION_VALUE) final String direction,
			@RequestParam(name = CommonConstants.REQUEST_PARAM_SORT_FIELD, defaultValue = CommonConstants.COMMON_FIELD_NAME_ID) final String sortField) {
		logger.debug("New OrchestratorStore get request recieved with page: {} and item_per page: {}", page, size);
		
		int validatedPage;
		int validatedSize;
		if (page == null && size == null) {
			validatedPage = -1;
			validatedSize = -1;
		} else {
			if (page == null || size == null) {
				throw new BadPayloadException("Defined page or size could not be with undefined size or page.", HttpStatus.SC_BAD_REQUEST, CommonConstants.ORCHESTRATOR_STORE_MGMT_URI);
			} else {
				validatedPage = page;
				validatedSize = size;
			}
		}
		final Direction validatedDirection = calculateDirection(direction, CommonConstants.ORCHESTRATOR_STORE_MGMT_URI);
		
		final OrchestratorStoreListResponseDTO intraCloudAuthorizationEntriesResponse = orchestratorStoreDBService.getOrchestratorStoreEntriesResponse(validatedPage, validatedSize, validatedDirection, sortField);
		logger.debug("OrchestratorStores  with page: {} and item_per page: {} retrieved successfully", page, size);
		return intraCloudAuthorizationEntriesResponse;
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return requested OrchestratorStore entries by the given parameters", response = OrchestratorStoreListResponseDTO.class)
	@ApiResponses (value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = GET_ORCHESTRATOR_STORE_MGMT_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = GET_ORCHESTRATOR_STORE_MGMT_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path = ORCHESTRATOR_STORE_MGMT_ALL_TOP_PRIORITY, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public OrchestratorStoreListResponseDTO getAllTopPriorityOrchestratorStores(
			@RequestParam(name = CommonConstants.REQUEST_PARAM_PAGE, required = false) final Integer page,
			@RequestParam(name = CommonConstants.REQUEST_PARAM_ITEM_PER_PAGE, required = false) final Integer size,
			@RequestParam(name = CommonConstants.REQUEST_PARAM_DIRECTION, defaultValue = Defaults.DEFAULT_REQUEST_PARAM_DIRECTION_VALUE) final String direction,
			@RequestParam(name = CommonConstants.REQUEST_PARAM_SORT_FIELD, defaultValue = CommonConstants.COMMON_FIELD_NAME_ID) final String sortField) {
		logger.debug("New OrchestratorStore get request recieved with page: {} and item_per page: {}", page, size);
		
		int validatedPage;
		int validatedSize;
		if (page == null && size == null) {
			validatedPage = -1;
			validatedSize = -1;
		} else {
			if (page == null || size == null) {
				throw new BadPayloadException("Defined page or size could not be with undefined size or page.", HttpStatus.SC_BAD_REQUEST, CommonConstants.ORCHESTRATOR_STORE_MGMT_URI);
			} else {
				validatedPage = page;
				validatedSize = size;
			}
		}
		final Direction validatedDirection = calculateDirection(direction, CommonConstants.ORCHESTRATOR_STORE_MGMT_URI);
		
		final OrchestratorStoreListResponseDTO intraCloudAuthorizationEntriesResponse = orchestratorStoreDBService.getAllTopPriorityOrchestratorStoreEntriesResponse(validatedPage, validatedSize, validatedDirection, sortField);
		logger.debug("OrchestratorStores  with page: {} and item_per page: {} retrieved successfully", page, size);
		return intraCloudAuthorizationEntriesResponse;
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private Direction calculateDirection(final String direction, final String origin) {
		logger.debug("calculateDirection started ...");
		
		final String directionStr = direction != null ? direction.toUpperCase() : "";
		Direction validatedDirection;
		switch (directionStr) {
			case CommonConstants.SORT_ORDER_ASCENDING:
				validatedDirection = Direction.ASC;
				break;
			case CommonConstants.SORT_ORDER_DESCENDING:
				validatedDirection = Direction.DESC;
				break;
			default:
				throw new BadPayloadException("Invalid sort direction flag", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		return validatedDirection;
	}
}
