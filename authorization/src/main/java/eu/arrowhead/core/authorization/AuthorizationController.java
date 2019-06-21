package eu.arrowhead.core.authorization;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.Defaults;
import eu.arrowhead.common.dto.IntraCloudAuthorizationListResponseDTO;
import eu.arrowhead.common.dto.IntraCloudAuthorizationRequestDTO;
import eu.arrowhead.common.dto.IntraCloudAuthorizationResponseDTO;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.core.authorization.database.service.AuthorizationDBService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@CrossOrigin(maxAge = Defaults.CORS_MAX_AGE, allowCredentials = Defaults.CORS_ALLOW_CREDENTIALS, 
			 allowedHeaders = { HttpHeaders.ORIGIN, HttpHeaders.CONTENT_TYPE, HttpHeaders.ACCEPT, HttpHeaders.AUTHORIZATION }
)
@RestController
@RequestMapping(CommonConstants.AUTHORIZATIOIN_URI)
public class AuthorizationController {
	
	//=================================================================================================
	// members
	
	private static final String PATH_VARIABLE_ID = "id";
	private static final String ID_NOT_VALID_ERROR_MESSAGE = "Id must be greater then 0.";
	
	private static final String ECHO_URI = "/echo";
	
	private static final String INTRA_CLOUD_AUTHORIZATION_MGMT_URI = CommonConstants.MGMT_URI + "/intracloud";
	private static final String INTRA_CLOUD_AUTHORIZATION_MGMT_BY_ID_URI = INTRA_CLOUD_AUTHORIZATION_MGMT_URI + "/{" + PATH_VARIABLE_ID + "}";
	private static final String GET_INTRA_CLOUD_AUTHORIZATION_HTTP_200_MESSAGE = "IntraCloudAuthorization returned";
	private static final String GET_INTRA_CLOUD_AUTHORIZATION_HTTP_400_MESSAGE = "Could not retrieve IntraCloudAuthorization";
	private static final String DELETE_INTRA_CLOUD_AUTHORIZATION_HTTP_200_MESSAGE = "IntraCloudAuthorization removed";
	private static final String DELETE_INTRA_CLOUD_AUTHORIZATION_HTTP_400_MESSAGE = "Could not remove IntraCloudAuthorization";
	
	private final Logger logger = LogManager.getLogger(AuthorizationController.class);
	
	@Autowired
	private AuthorizationDBService authorizationDBService;
	

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
	@ApiOperation(value = "Return requested IntraCloudAuthorization entries by the given parameters", response = IntraCloudAuthorizationListResponseDTO.class)
	@ApiResponses (value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = GET_INTRA_CLOUD_AUTHORIZATION_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_OK, message = GET_INTRA_CLOUD_AUTHORIZATION_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path = INTRA_CLOUD_AUTHORIZATION_MGMT_URI)
	@ResponseBody public IntraCloudAuthorizationListResponseDTO getIntraCloudAuthorizations(
			@RequestParam(name = CommonConstants.REQUEST_PARAM_PAGE, required = false) final Integer page,
			@RequestParam(name = CommonConstants.REQUEST_PARAM_ITEM_PER_PAGE, required = false) final Integer size,
			@RequestParam(name = CommonConstants.REQUEST_PARAM_DIRECTION, defaultValue = Defaults.DEFAULT_REQUEST_PARAM_DIRECTION_VALUE) final String direction,
			@RequestParam(name = CommonConstants.REQUEST_PARAM_SORT_FIELD, defaultValue = CommonConstants.COMMON_FIELD_NAME_ID) final String sortField) {
		logger.debug("New IntraCloudAuthorization get request recieved with page: {} and item_per page: {}", page, size);
		
		int validatedPage;
		int validatedSize;
		if (page == null && size == null) {
			validatedPage = -1;
			validatedSize = -1;
		} else {
			if (page == null || size == null) {
				throw new BadPayloadException("Defined page or size could not be with undefined size or page.", HttpStatus.SC_BAD_REQUEST, CommonConstants.AUTHORIZATIOIN_URI + INTRA_CLOUD_AUTHORIZATION_MGMT_URI);
			} else {
				validatedPage = page;
				validatedSize = size;
			}
		}
		final Direction validatedDirection = calculateDirection(direction, CommonConstants.AUTHORIZATIOIN_URI + INTRA_CLOUD_AUTHORIZATION_MGMT_URI);
		
		final IntraCloudAuthorizationListResponseDTO intraCloudAuthorizationEntriesResponse = authorizationDBService.getIntraCloudAuthorizationEntriesResponse(validatedPage, validatedSize, validatedDirection, sortField);
		logger.debug("IntraCloudAuthorizations  with page: {} and item_per page: {} succesfully retrived", page, size);
		return intraCloudAuthorizationEntriesResponse;
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return requested IntraCloudAuthorization entry", response = IntraCloudAuthorizationResponseDTO.class)
	@ApiResponses (value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = GET_INTRA_CLOUD_AUTHORIZATION_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_OK, message = GET_INTRA_CLOUD_AUTHORIZATION_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path = INTRA_CLOUD_AUTHORIZATION_MGMT_BY_ID_URI)
	@ResponseBody public IntraCloudAuthorizationResponseDTO getIntraCloudAuthorizationById(@PathVariable(value = PATH_VARIABLE_ID) final long id) {
		logger.debug("New IntraCloudAuthorization get request recieved with id: {}", id);
		
		if (id < 1) {
			throw new BadPayloadException(ID_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, CommonConstants.AUTHORIZATIOIN_URI + INTRA_CLOUD_AUTHORIZATION_MGMT_BY_ID_URI);
		}
		
		final IntraCloudAuthorizationResponseDTO intraCloudAuthorizationEntryByIdResponse = authorizationDBService.getIntraCloudAuthorizationEntryByIdResponse(id);
		logger.debug("IntraCloudAuthorization entry with id: {} successfully retrieved", id);
		return intraCloudAuthorizationEntryByIdResponse;
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Remove the requested IntraCloudAuthorization entry")
	@ApiResponses (value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = DELETE_INTRA_CLOUD_AUTHORIZATION_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_OK, message = DELETE_INTRA_CLOUD_AUTHORIZATION_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@DeleteMapping(path = INTRA_CLOUD_AUTHORIZATION_MGMT_BY_ID_URI)
	public void removeIntraCloudAuthorizationById(@PathVariable(value = PATH_VARIABLE_ID) final long id) {
		logger.debug("New IntraCloudAuthorization delete request recieved with id: {}", id);
		
		if (id < 1) {
			throw new BadPayloadException(ID_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, CommonConstants.AUTHORIZATIOIN_URI + INTRA_CLOUD_AUTHORIZATION_MGMT_BY_ID_URI);
		}
		
		authorizationDBService.removeIntraCloudAuthorizationEntryById(id);
		logger.debug("IntraCloudAuthorization with id: '{}' successfully deleted", id);
	}
	
	//-------------------------------------------------------------------------------------------------
	@PostMapping(path = INTRA_CLOUD_AUTHORIZATION_MGMT_URI)
	@ResponseBody public IntraCloudAuthorizationListResponseDTO registerIntraCloudAuthorization(@RequestBody final IntraCloudAuthorizationRequestDTO request) {
		logger.debug("New IntraCloudAuthorization registration request recieved");
		
		boolean isConsumerIdInvalid = request.getConsumerId() < 1 || request.getConsumerId() == null;
		boolean isProviderListEmpty = request.getProviderIds().isEmpty();
		boolean isServiceDefinitionListEmpty = request.getServiceDefinitionIds().isEmpty();
		if (isConsumerIdInvalid || isProviderListEmpty || isServiceDefinitionListEmpty) {
			String exceptionMsg = isConsumerIdInvalid ? "invalid consumer id" : "";
			exceptionMsg = isProviderListEmpty ? exceptionMsg + " providerId list is empty" : exceptionMsg;
			exceptionMsg = isServiceDefinitionListEmpty ? exceptionMsg + " serviceDefinitionList is empty" : exceptionMsg;
			throw new BadPayloadException(exceptionMsg, HttpStatus.SC_BAD_REQUEST, CommonConstants.AUTHORIZATIOIN_URI + INTRA_CLOUD_AUTHORIZATION_MGMT_URI);
		}
		
		for (long providerId : request.getProviderIds()) {
			for (long serviceDefinitionId : request.getServiceDefinitionIds()) {
				IntraCloudAuthorizationResponseDTO createIntraCloudAuthorizationResponse = authorizationDBService.createIntraCloudAuthorizationResponse(request.getConsumerId(), providerId, serviceDefinitionId);
				//TODO
			}
		}
		
		
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