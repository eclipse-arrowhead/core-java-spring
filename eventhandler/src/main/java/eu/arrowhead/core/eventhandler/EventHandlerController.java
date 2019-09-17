package eu.arrowhead.core.eventhandler;

import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.Utilities.ValidatedPageParams;
import eu.arrowhead.common.dto.EventPublishRequestDTO;
import eu.arrowhead.common.dto.EventPublishResponseDTO;
import eu.arrowhead.common.dto.SubscriptionListResponseDTO;
import eu.arrowhead.common.dto.SubscriptionRequestDTO;
import eu.arrowhead.common.dto.SubscriptionResponseDTO;
import eu.arrowhead.common.dto.SystemRequestDTO;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.core.eventhandler.database.service.EventHandlerDBService;
import eu.arrowhead.core.eventhandler.service.EventHandlerService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Api;

@Api(tags = { CommonConstants.SWAGGER_TAG_ALL })
@CrossOrigin(maxAge = Defaults.CORS_MAX_AGE, allowCredentials = Defaults.CORS_ALLOW_CREDENTIALS, 
allowedHeaders = { HttpHeaders.ORIGIN, HttpHeaders.CONTENT_TYPE, HttpHeaders.ACCEPT, HttpHeaders.AUTHORIZATION }
)
@RestController
@RequestMapping(CommonConstants.EVENT_HANDLER_URI)
public class EventHandlerController {
	
	//=================================================================================================
	// members
	
	private static final String PATH_VARIABLE_ID = "id";

	private static final String EVENT_HANDLER_MGMT_URI =  CommonConstants.MGMT_URI + "/subscriptions";
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
	
	private static final String PUT_EVENT_HANDLER_SUBSCRIPTION_DESCRIPTION = "Unsubcribtion from the events specified in requested Subscription ";
	private static final String PUT_EVENT_HANDLER_SUBSCRIPTION_HTTP_200_MESSAGE = "Successful unsubscription.";
	private static final String PUT_EVENT_HANDLER_SUBSCRIPTION_HTTP_400_MESSAGE = "Unsuccessful unsubscription.";
	
	private static final String POST_EVENT_HANDLER_PUBLISH_DESCRIPTION = "Publish event"; 
	private static final String POST_EVENT_HANDLER_PUBLISH_HTTP_200_MESSAGE = "Publish event success"; 
	private static final String POST_EVENT_HANDLER_PUBLISH_HTTP_400_MESSAGE = "Publish event not success"; 

	private static final String POST_EVENT_HANDLER_PUBLISH_AUTH_UPDATE_DESCRIPTION = "Publish authorization change event "; 
	private static final String POST_EVENT_HANDLER_PUBLISH_AUTH_UPDATE_HTTP_200_MESSAGE = "Publish authorization change event success"; 
	private static final String POST_EVENT_HANDLER_PUBLISH_AUTH_UPDATE_HTTP_400_MESSAGE = "Publish authorization change event not success"; 
	
	private static final String NULL_PARAMETER_ERROR_MESSAGE = " is null.";
	private static final String NULL_OR_BLANK_PARAMETER_ERROR_MESSAGE = " is null or blank.";
	private static final String ID_NOT_VALID_ERROR_MESSAGE = " Id must be greater than 0. ";
	private static final String WRONG_FORMAT_ERROR_MESSAGE = " is in wrong format. ";
	private static final String IS_AFTER_TOLERATED_DIFF_ERROR_MESSAGE = " is further in the future then the tolerated time difference";
	private static final String IS_BEFORE_TOLERATED_DIFF_ERROR_MESSAGE = " is further in the past then the tolerated time difference";;

	@Value(CommonConstants.$TIME_STAMP_TOLERANCE_SECONDS_WD)
	private long timeStampTolerance;
	
	private final Logger logger = LogManager.getLogger(EventHandlerController.class);
	
	@Autowired
	EventHandlerService eventHandlerService;
	
	@Autowired
	EventHandlerDBService eventHandlerDBService;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return an echo message with the purpose of testing the core service availability", response = String.class, tags = { CommonConstants.SWAGGER_TAG_CLIENT })
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
	@ApiOperation(value = GET_EVENT_HANDLER_MGMT_DESCRIPTION, response = SubscriptionListResponseDTO.class, tags = { CommonConstants.SWAGGER_TAG_MGMT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = GET_EVENT_HANDLER_MGMT_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = GET_EVENT_HANDLER_MGMT_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path = EVENT_HANDLER_MGMT_URI, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public SubscriptionListResponseDTO getSubscriptions(
			@RequestParam(name = CommonConstants.REQUEST_PARAM_PAGE, required = false) final Integer page,
			@RequestParam(name = CommonConstants.REQUEST_PARAM_ITEM_PER_PAGE, required = false) final Integer size,
			@RequestParam(name = CommonConstants.REQUEST_PARAM_DIRECTION, defaultValue = Defaults.DEFAULT_REQUEST_PARAM_DIRECTION_VALUE) final String direction,
			@RequestParam(name = CommonConstants.REQUEST_PARAM_SORT_FIELD, defaultValue = CommonConstants.COMMON_FIELD_NAME_ID) final String sortField) {
		logger.debug("New getSubscriptions get request recieved with page: {} and item_per page: {}", page, size);
				
		final ValidatedPageParams validParameters = Utilities.validatePageParameters(page, size, direction, CommonConstants.EVENT_HANDLER_URI + EVENT_HANDLER_MGMT_URI);
		final SubscriptionListResponseDTO SubscriptionsResponse = eventHandlerDBService.getSubscriptionsRequest(validParameters.getValidatedPage(), validParameters.getValidatedSize(), 
																									validParameters.getValidatedDirecion(), sortField);
		
		logger.debug("Subscriptions  with page: {} and item_per page: {} retrieved successfully", page, size);
		return SubscriptionsResponse;
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = GET_EVENT_HANDLER_BY_ID_MGMT_DESCRIPTION, response = SubscriptionResponseDTO.class, tags = { CommonConstants.SWAGGER_TAG_MGMT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = GET_EVENT_HANDLER_BY_ID_MGMT_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = GET_EVENT_HANDLER_BY_ID_MGMT_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path = EVENTHANLER_BY_ID_MGMT_URI, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public SubscriptionResponseDTO getSubscriptionById(@PathVariable(value = PATH_VARIABLE_ID) final long id) {
		logger.debug("New getSubscriptionById get request recieved with id: {}", id);
		
		final String origin = CommonConstants.EVENT_HANDLER_URI + EVENTHANLER_BY_ID_MGMT_URI;
		if (id < 1) {
			throw new BadPayloadException(ID_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		final SubscriptionResponseDTO SubscriptionResponse = eventHandlerDBService.getSubscriptionByIdRequest(id);
		
		logger.debug("Subscription entry with id: {} successfully retrieved", id);
		
		return SubscriptionResponse;
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = DELETE_EVENT_HANDLER_MGMT_DESCRIPTION, tags = { CommonConstants.SWAGGER_TAG_MGMT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = DELETE_EVENT_HANDLER_MGMT_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = DELETE_EVENT_HANDLER_MGMT_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@DeleteMapping(path = EVENTHANLER_BY_ID_MGMT_URI, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public void deleteSubscription(@PathVariable(value = PATH_VARIABLE_ID) final long id) {
		logger.debug("New deleteSubscription delete request recieved with id: {}", id);
		
		final String origin = CommonConstants.EVENT_HANDLER_URI + EVENTHANLER_BY_ID_MGMT_URI;
		if (id < 1) {
			throw new BadPayloadException(ID_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		eventHandlerDBService.deleteSubscriptionRequest(id);
		
		logger.debug("Subscription entry with id: {} successfully deleted", id);
		
		return;
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = PUT_EVENT_HANDLER_MGMT_DESCRIPTION, response = SubscriptionResponseDTO.class, tags = { CommonConstants.SWAGGER_TAG_MGMT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = PUT_EVENT_HANDLER_MGMT_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = PUT_EVENT_HANDLER_MGMT_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@PutMapping(path = EVENTHANLER_BY_ID_MGMT_URI, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public SubscriptionResponseDTO updateSubscription(
			@PathVariable(value = PATH_VARIABLE_ID) final long id,
			@RequestBody final SubscriptionRequestDTO request) {
		logger.debug("New updateSubscription put request recieved with id: {}", id);
		
		final String origin = CommonConstants.EVENT_HANDLER_URI + EVENTHANLER_BY_ID_MGMT_URI;
		if (id < 1) {
			throw new BadPayloadException(ID_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		checkSubscriptionRequestDTO(request, origin);
		
		final SubscriptionResponseDTO response = eventHandlerService.updateSubscriptionRequest(id, request);
		
		logger.debug("Subscription entry with id: {} successfully updated", id);
		
		return response;
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = POST_EVENT_HANDLER_SUBSCRIPTION_DESCRIPTION, response = SubscriptionResponseDTO.class, tags = { CommonConstants.SWAGGER_TAG_CLIENT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = POST_EVENT_HANDLER_SUBSCRIPTION_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = POST_EVENT_HANDLER_SUBSCRIPTION_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@PostMapping(path = CommonConstants.OP_EVENT_HANDLER_SUBSCRIBE, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public SubscriptionResponseDTO subscription(@RequestBody final SubscriptionRequestDTO request) {
		logger.debug("subscription started ...");
		
		final String origin = CommonConstants.EVENT_HANDLER_URI + CommonConstants.OP_EVENT_HANDLER_SUBSCRIBE;
		checkSubscriptionRequestDTO(request, origin);
		
	    return eventHandlerService.subscriptionRequest(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = PUT_EVENT_HANDLER_SUBSCRIPTION_DESCRIPTION,  tags = { CommonConstants.SWAGGER_TAG_CLIENT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = PUT_EVENT_HANDLER_SUBSCRIPTION_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = PUT_EVENT_HANDLER_SUBSCRIPTION_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@PutMapping(path = CommonConstants.OP_EVENT_HANDLER_SUBSCRIBE, consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public void unSubscription(@RequestBody final SubscriptionRequestDTO request) {
		logger.debug("unSubscription started ...");
		
		final String origin = CommonConstants.EVENT_HANDLER_URI + CommonConstants.OP_EVENT_HANDLER_SUBSCRIBE;
		checkSubscriptionRequestDTO(request, origin);
		
	    eventHandlerService.unSubscriptionRequest(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = POST_EVENT_HANDLER_PUBLISH_DESCRIPTION, response = EventPublishResponseDTO.class,  tags = { CommonConstants.SWAGGER_TAG_CLIENT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = POST_EVENT_HANDLER_PUBLISH_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = POST_EVENT_HANDLER_PUBLISH_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@PostMapping(path = CommonConstants.OP_EVENT_HANDLER_PUBLISH, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public EventPublishResponseDTO publish(@RequestBody final EventPublishRequestDTO request) {
		logger.debug("publish started ...");
		
		final String origin = CommonConstants.EVENT_HANDLER_URI + CommonConstants.OP_EVENT_HANDLER_PUBLISH;
		checkEventPublishRequestDTO(request, origin);
		
		validateTimeStamp(request, origin);
		
	    return eventHandlerService.publishRequest(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = POST_EVENT_HANDLER_PUBLISH_AUTH_UPDATE_DESCRIPTION, tags = { CommonConstants.SWAGGER_TAG_PRIVATE })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = POST_EVENT_HANDLER_PUBLISH_AUTH_UPDATE_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = POST_EVENT_HANDLER_PUBLISH_AUTH_UPDATE_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@PostMapping(path = CommonConstants.OP_EVENT_HANDLER_PUBLISH_AUTH_UPDATE, consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public void publishSubscriberAuthorizationUpdate(@RequestBody final EventPublishRequestDTO request) {
		logger.debug("publishSubscriberAuthorizationUpdate started ...");
		
		final String origin = CommonConstants.EVENT_HANDLER_URI + CommonConstants.OP_EVENT_HANDLER_PUBLISH_AUTH_UPDATE;

		checkEventPublishRequestDTO(request, origin);
		
		validateTimeStamp(request, origin);
		
	    eventHandlerService.publishSubscriberAuthorizationUpdateRequest(request);
	}
	
	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------	
	private void checkSubscriptionRequestDTO( final SubscriptionRequestDTO request, final String origin) {
		logger.debug("checkSubscriptionRequestDTO started ...");
		
		if (request == null) {
			throw new BadPayloadException("Request" + NULL_PARAMETER_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		checkSystemRequestDTO(request.getSubscriberSystem(), origin);
		
		if ( Utilities.isEmpty( request.getEventType() )) {
			throw new BadPayloadException("Request.EventType" + NULL_OR_BLANK_PARAMETER_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);	
		}
		
		if ( Utilities.isEmpty( request.getNotifyUri() )) {
			throw new BadPayloadException("Request.NotifyUri" + NULL_OR_BLANK_PARAMETER_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);	
		}
		
		if (request.getMatchMetaData() == null) {
			throw new BadPayloadException("Request.MatchMetaData" + NULL_PARAMETER_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		if ( request.getMatchMetaData() && ( request.getFilterMetaData() == null || request.getFilterMetaData().isEmpty() )) {
			
			throw new BadPayloadException("Request.MatchMetaData is true but Request.FilterMetaData" + NULL_OR_BLANK_PARAMETER_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		if (request.getSources() != null && !request.getSources().isEmpty()) {
			for (final SystemRequestDTO systemRequestDTO : request.getSources()) {
				checkSystemRequestDTO(systemRequestDTO, origin);
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
	private void checkEventPublishRequestDTO(final EventPublishRequestDTO request, final String origin) {
		logger.debug("checkEventPublishRequestDTO started ...");
		
		if (request == null) {
			throw new BadPayloadException("Request" + NULL_PARAMETER_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		checkSystemRequestDTO(request.getSource(), origin);
		
		if ( Utilities.isEmpty( request.getEventType() )) {
			throw new BadPayloadException("Request.EventType" + NULL_OR_BLANK_PARAMETER_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		if ( Utilities.isEmpty( request.getPayload() )) {
			throw new BadPayloadException("Request.Payload" + NULL_OR_BLANK_PARAMETER_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
		}	
		
		// TODO implement additional method logic here
		
	}
	
	//-------------------------------------------------------------------------------------------------
	// This method may CHANGE the content of EventPublishRequestDTO
	private void validateTimeStamp(final EventPublishRequestDTO request, final String origin) {
		logger.debug("validateTimeStamp started ...");
		
		if ( Utilities.isEmpty( request.getTimeStamp() )) {
			
			request.setTimeStamp(Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()));
		
		} else	{
			
			final ZonedDateTime now = ZonedDateTime.now();
			final ZonedDateTime timeStamp;

			try {
				
				timeStamp = Utilities.parseUTCStringToLocalZonedDateTime(request.getTimeStamp());
			
			} catch (final DateTimeParseException ex) {
				
				throw new BadPayloadException("Request.TimeStamp" + WRONG_FORMAT_ERROR_MESSAGE + ex, HttpStatus.SC_BAD_REQUEST, origin);
			}
			
			if (timeStamp.isAfter(now.plusSeconds( timeStampTolerance ))) {
				
				throw new BadPayloadException("Request.TimeStamp" + IS_AFTER_TOLERATED_DIFF_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
			}
			
			if (timeStamp.isBefore(now.minusSeconds( timeStampTolerance ))) {
				
				throw new BadPayloadException("Request.TimeStamp" + IS_BEFORE_TOLERATED_DIFF_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
			}

		}
	}

}