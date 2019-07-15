package eu.arrowhead.core.orchestrator;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.Defaults;
import eu.arrowhead.common.dto.OrchestratorStoreListResponseDTO;
import eu.arrowhead.common.dto.OrchestratorStoreModifyPriorityRequestDTO;
import eu.arrowhead.common.dto.OrchestratorStoreRequestByIdDTO;
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
	private static final String ORCHESTRATOR_STORE_MGMT_ALL_TOP_PRIORITY = CommonConstants.ORCHESTRATOR_STORE_MGMT_URI + "/all_top_priority";
	private static final String ORCHESTRATOR_STORE_MGMT_MODIFY = CommonConstants.ORCHESTRATOR_STORE_MGMT_URI + "/modify_priorities";
	private static final String GET_ORCHESTRATOR_STORE_MGMT_BY_ID_HTTP_200_MESSAGE = "OrchestratorStore by requested id returned";
	private static final String GET_ORCHESTRATOR_STORE_MGMT_BY_ID_HTTP_400_MESSAGE = "No Such OrchestratorStore by requested id";
	private static final String GET_ORCHESTRATOR_STORE_MGMT_HTTP_200_MESSAGE = "OrchestratorStores by requested parameters returned";
	private static final String GET_ORCHESTRATOR_STORE_MGMT_HTTP_400_MESSAGE = "No Such OrchestratorStore by requested parameters";
	private static final String GET_TOP_PRIORITY_ORCHESTRATOR_STORE_MGMT_HTTP_200_MESSAGE = "Top Priotity OrchestratorStores returned";
	private static final String GET_TOP_PRIORITY_ORCHESTRATOR_STORE_MGMT_HTTP_400_MESSAGE = "Could not find Top Priotity OrchestratorStores";
	private static final String PUT_ORCHESTRATOR_STORE_MGMT_HTTP_200_MESSAGE = "OrchestratorStores by requested parameters returned";
	private static final String PUT_ORCHESTRATOR_STORE_MGMT_HTTP_400_MESSAGE = "No Such OrchestratorStore by requested parameters";
	private static final String POST_ORCHESTRATOR_STORE_MGMT_HTTP_200_MESSAGE = "OrchestratorStores by requested parameters created";
	private static final String POST_ORCHESTRATOR_STORE_MGMT_HTTP_400_MESSAGE = "Could not create OrchestratorStore by requested parameters";
	private static final String DELETE_ORCHESTRATOR_STORE_MGMT_HTTP_200_MESSAGE = "OrchestratorStore removed";
	private static final String DELETE_ORCHESTRATOR_STORE_MGMT_HTTP_400_MESSAGE = "Could not remove OrchestratorStore";
	private static final String POST_ORCHESTRATOR_STORE_MGMT_MODIFY_HTTP_200_MESSAGE = "OrchestratorStores by requested parameters modified";
	private static final String POST_ORCHESTRATOR_STORE_MGMT_MODIFY_HTTP_400_MESSAGE = "Could not modify OrchestratorStore by requested parameters";
	
	private static final String ID_NOT_VALID_ERROR_MESSAGE = "Id must be greater than 0. ";
	private static final String NULL_PARAMETERS_ERROR_MESSAGE = " is null.";
	private static final String EMPTY_PARAMETERS_ERROR_MESSAGE = " is empty.";
	private static final String MODIFY_PRIORITY_MAP_PRIORITY_DUPLICATION_ERROR_MESSAGE = "PriorityMap has duplicated priority value";
	
	private final Logger logger = LogManager.getLogger(OrchestratorStoreController.class);
	
	@Value(CommonConstants.$IS_GATEKEEPER_PRESENT_WD)
	private boolean gateKeeperIsPresent;
	
	@Value(CommonConstants.$SERVICE_REGISTRY_ADDRESS_WD)
	private String serviceRegistryAddress;
	
	@Value(CommonConstants.$SERVICE_REGISTRY_PORT_WD)
	private Integer serviceRegistryPort;
	
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
	@GetMapping(path = CommonConstants.ORCHESTRATOR_STORE_MGMT_URI + ECHO_URI)
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
		logger.debug("getOrchestratorStores started ...");
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
		
		final OrchestratorStoreListResponseDTO orchestratorStoreListResponse = orchestratorStoreDBService.getOrchestratorStoreEntriesResponse(validatedPage, validatedSize, validatedDirection, sortField);
		logger.debug("OrchestratorStores  with page: {} and item_per page: {} retrieved successfully", page, size);
		return orchestratorStoreListResponse;
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return requested OrchestratorStore entries by the given parameters", response = OrchestratorStoreListResponseDTO.class)
	@ApiResponses (value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = GET_TOP_PRIORITY_ORCHESTRATOR_STORE_MGMT_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = GET_TOP_PRIORITY_ORCHESTRATOR_STORE_MGMT_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path = ORCHESTRATOR_STORE_MGMT_ALL_TOP_PRIORITY, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public OrchestratorStoreListResponseDTO getAllTopPriorityOrchestratorStores(
			@RequestParam(name = CommonConstants.REQUEST_PARAM_PAGE, required = false) final Integer page,
			@RequestParam(name = CommonConstants.REQUEST_PARAM_ITEM_PER_PAGE, required = false) final Integer size,
			@RequestParam(name = CommonConstants.REQUEST_PARAM_DIRECTION, defaultValue = Defaults.DEFAULT_REQUEST_PARAM_DIRECTION_VALUE) final String direction,
			@RequestParam(name = CommonConstants.REQUEST_PARAM_SORT_FIELD, defaultValue = CommonConstants.COMMON_FIELD_NAME_ID) final String sortField) {
		logger.debug("getAllTopPriorityOrchestratorStores started ...");
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
		
		final OrchestratorStoreListResponseDTO orchestratorStoreResponse = orchestratorStoreDBService.getAllTopPriorityOrchestratorStoreEntriesResponse(validatedPage, validatedSize, validatedDirection, sortField);
		logger.debug("OrchestratorStores  with page: {} and item_per page: {} retrieved successfully", page, size);
		return orchestratorStoreResponse;
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return requested OrchestratorStore entries specified by the consumer (and the service).", response = OrchestratorStoreListResponseDTO.class)
	@ApiResponses (value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = PUT_ORCHESTRATOR_STORE_MGMT_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = PUT_ORCHESTRATOR_STORE_MGMT_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@PutMapping(path = CommonConstants.ORCHESTRATOR_STORE_MGMT_URI, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public OrchestratorStoreListResponseDTO getOrchestratorStoresByConsumer( @RequestParam(name = CommonConstants.REQUEST_PARAM_PAGE, required = false) final Integer page,
			@RequestParam(name = CommonConstants.REQUEST_PARAM_ITEM_PER_PAGE, required = false) final Integer size,
			@RequestParam(name = CommonConstants.REQUEST_PARAM_DIRECTION, defaultValue = Defaults.DEFAULT_REQUEST_PARAM_DIRECTION_VALUE) final String direction,
			@RequestParam(name = CommonConstants.REQUEST_PARAM_SORT_FIELD, defaultValue = CommonConstants.COMMON_FIELD_NAME_ID) final String sortField,
			@RequestBody final OrchestratorStoreRequestByIdDTO request) {
		logger.debug("getOrchestratorStoresByConsumer started ...");
		
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
		
		checkOrchestratorStoreRequestDTOForByConsumerIdAndServiceDefinitionId(request, CommonConstants.ORCHESTRATOR_STORE_MGMT_URI );
		
		final OrchestratorStoreListResponseDTO orchestratorStoreResponse = orchestratorStoreDBService.getOrchestratorStoresByConsumerResponse(
				validatedPage, 
				validatedSize, 
				validatedDirection, 
				sortField,
				request.getConsumerSystemId(),
				request.getServiceDefinitionId());
		
		logger.debug("OrchestratorStores  with ConsumerSystemId : {} and ServiceDefinitionId : {} and  page: {} and item_per page: {} retrieved successfully", request.getConsumerSystemId(), request.getServiceDefinitionId(), page, size);
		return orchestratorStoreResponse;
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Create requested OrchestratorStore entries.", response = OrchestratorStoreListResponseDTO.class)
	@ApiResponses (value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = POST_ORCHESTRATOR_STORE_MGMT_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = POST_ORCHESTRATOR_STORE_MGMT_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@PostMapping(path = CommonConstants.ORCHESTRATOR_STORE_MGMT_URI, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public OrchestratorStoreListResponseDTO addOrchestratorStoreEntries( @RequestBody final List<OrchestratorStoreRequestByIdDTO> request) {
		logger.debug("getOrchestratorStoresByConsumer started ...");
		
		checkOrchestratorStoreRequestByIdDTOList(request, CommonConstants.ORCHESTRATOR_STORE_MGMT_URI );
		
		final OrchestratorStoreListResponseDTO orchestratorStoreResponse = orchestratorStoreDBService.createOrchestratorStoresByIdResponse(
				request);
		
		logger.debug(orchestratorStoreResponse.getCount() + " out of " + request.size() + " OrchestratorStore entries created successfully");
		return orchestratorStoreResponse;
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Remove OrchestratorStore")
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = DELETE_ORCHESTRATOR_STORE_MGMT_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = DELETE_ORCHESTRATOR_STORE_MGMT_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@DeleteMapping(path = ORCHESTRATOR_STORE_MGMT_BY_ID_URI)
	public void removeOrchestratorStore(@PathVariable(value = PATH_VARIABLE_ID) final long id) {
		logger.debug("New OrchestratorStore delete request recieved with id: {}", id);
		
		if (id < 1) {
			throw new BadPayloadException(ID_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, ORCHESTRATOR_STORE_MGMT_BY_ID_URI);
		}
		
		orchestratorStoreDBService.removeOrchestratorStoreById(id);
		logger.debug("OrchestratorStore with id: '{}' successfully deleted", id);
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Modify prioities of OrchestratorStore entries.")
	@ApiResponses (value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = POST_ORCHESTRATOR_STORE_MGMT_MODIFY_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = POST_ORCHESTRATOR_STORE_MGMT_MODIFY_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@PostMapping(path = ORCHESTRATOR_STORE_MGMT_MODIFY, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public void modifyPriorities( @RequestBody final OrchestratorStoreModifyPriorityRequestDTO request) {
		logger.debug("modifyPriorities started ...");
		
		checkOrchestratorStoreModifyPriorityRequestDTO(request, ORCHESTRATOR_STORE_MGMT_MODIFY );
		
		orchestratorStoreDBService.modifyOrchestratorStorePriorityResponse(request);
		
		logger.debug("Priorities modified successfully");
	}
	
	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------	
	private void checkOrchestratorStoreRequestDTOForByConsumerIdAndServiceDefinitionId(final OrchestratorStoreRequestByIdDTO request, final String origin) {
		logger.debug("checkOrchestratorStoreRequestDTOForByConsumerRequest started ...");
		
		
		
		if (request == null) {
			throw new BadPayloadException("Request "+ NULL_PARAMETERS_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		if (request.getConsumerSystemId() == null) {
			throw new BadPayloadException(""+ NULL_PARAMETERS_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
		}else {
			if (request.getConsumerSystemId() < 1) {
				throw new BadPayloadException("ConsumerSystemId : " + ID_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
			}
		}
		
		if (request.getServiceDefinitionId() == null) {
			throw new BadPayloadException("ServiceDefinitionId " + NULL_PARAMETERS_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
		}else {
			if (request.getConsumerSystemId() < 1) {
				throw new BadPayloadException("ServiceDefinitionId : " + ID_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
			}
		}
		
	}
	
	//-------------------------------------------------------------------------------------------------	
	private void checkOrchestratorStoreRequestByIdDTOList(final List<OrchestratorStoreRequestByIdDTO> request, final String origin) {
		logger.debug("checkOrchestratorStoreRequestDTOList started ...");
		
		if (request == null) {
			throw new BadPayloadException("Request "+ NULL_PARAMETERS_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		if ( request.isEmpty()) {
			throw new BadPayloadException("Request "+ EMPTY_PARAMETERS_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		for (final OrchestratorStoreRequestByIdDTO orchestratorStoreRequestByIdDTO : request) {
			if (orchestratorStoreRequestByIdDTO == null) {
				throw new BadPayloadException("OrchestratorStoreRequestByIdDTO "+ NULL_PARAMETERS_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
			}
			
			if (orchestratorStoreRequestByIdDTO.getPriority() == null) {
				throw new BadPayloadException("OrchestratorStoreRequestByIdDTO.Priority "+ NULL_PARAMETERS_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
			}
		}

	}
	
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

	//-------------------------------------------------------------------------------------------------
	private void checkOrchestratorStoreModifyPriorityRequestDTO(final OrchestratorStoreModifyPriorityRequestDTO request,
			final String origin) {
		
		if (request == null) {
			throw new BadPayloadException("Request "+ NULL_PARAMETERS_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		if ( request.getPriorityMap().isEmpty()) {
			throw new BadPayloadException("PriorityMap "+ EMPTY_PARAMETERS_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		final Map<Long, Integer> priorityMap = request.getPriorityMap();
		
		for (final Long consumerSystemId : priorityMap.keySet()) {
			if (consumerSystemId == null) {
				throw new BadPayloadException("PriorityMap.ConsumerSystemId "+ NULL_PARAMETERS_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
			}
			if (priorityMap.get(consumerSystemId) == null) {
				throw new BadPayloadException("PriorityMap.PriorityValue "+ NULL_PARAMETERS_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
			}
		}
		
		final int mapSize = priorityMap.size();
		final Collection<Integer> valuesSet = priorityMap.values();
		if (mapSize != valuesSet.size()) {
			throw new BadPayloadException(MODIFY_PRIORITY_MAP_PRIORITY_DUPLICATION_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
		}

	}

}