/********************************************************************************
 * Copyright (c) 2019 AITIA
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   AITIA - implementation
 *   Arrowhead Consortia - conceptualization
 ********************************************************************************/

package eu.arrowhead.core.eventhandler;

import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.logging.LogLevel;
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
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.CoreDefaults;
import eu.arrowhead.common.CoreUtilities;
import eu.arrowhead.common.CoreUtilities.ValidatedPageParams;
import eu.arrowhead.common.core.CoreSystem;
import eu.arrowhead.common.database.entity.Logs;
import eu.arrowhead.common.database.service.CommonDBService;
import eu.arrowhead.common.Defaults;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.internal.LogEntryListResponseDTO;
import eu.arrowhead.common.dto.shared.EventPublishRequestDTO;
import eu.arrowhead.common.dto.shared.SubscriptionListResponseDTO;
import eu.arrowhead.common.dto.shared.SubscriptionRequestDTO;
import eu.arrowhead.common.dto.shared.SubscriptionResponseDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.core.eventhandler.database.service.EventHandlerDBService;
import eu.arrowhead.core.eventhandler.service.EventHandlerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(tags = { CoreCommonConstants.SWAGGER_TAG_ALL })
@CrossOrigin(maxAge = Defaults.CORS_MAX_AGE, allowCredentials = Defaults.CORS_ALLOW_CREDENTIALS, 
			 allowedHeaders = { HttpHeaders.ORIGIN, HttpHeaders.CONTENT_TYPE, HttpHeaders.ACCEPT, HttpHeaders.AUTHORIZATION }
)
@RestController
@RequestMapping(CommonConstants.EVENTHANDLER_URI)
public class EventHandlerController {
	
	//=================================================================================================
	// members
	
	private static final String PATH_VARIABLE_ID = "id";

	private static final String EVENTHANDLER_MGMT_URI =  CoreCommonConstants.MGMT_URI + "/subscriptions";
	private static final String EVENTHANDLER_BY_ID_MGMT_URI = EVENTHANDLER_MGMT_URI + "/{" + PATH_VARIABLE_ID + "}";
	
	private static final String GET_EVENTHANDLER_MGMT_DESCRIPTION = "Return requested Subscription entries by the given parameters";
	private static final String GET_EVENTHANDLER_MGMT_HTTP_200_MESSAGE = "Subscription entries returned";
	private static final String GET_EVENTHANDLER_MGMT_HTTP_400_MESSAGE = "Could not retrieve Subscription entries";
	
	private static final String GET_EVENTHANDLER_BY_ID_MGMT_DESCRIPTION = "Return requested Subscription entry by the given id";
	private static final String GET_EVENTHANDLER_BY_ID_MGMT_HTTP_200_MESSAGE = "Subscription entriy returned";
	private static final String GET_EVENTHANDLER_BY_ID_MGMT_HTTP_400_MESSAGE = "Could not retrieve Subscription entry";
	
	private static final String DELETE_EVENTHANDLER_MGMT_DESCRIPTION = "Delete requested Subscription entry by the given id";
	private static final String DELETE_EVENTHANDLER_MGMT_HTTP_200_MESSAGE = "Subscription entriy deleted";
	private static final String DELETE_EVENTHANDLER_MGMT_HTTP_400_MESSAGE = "Could not delete Subscription entry";
	
	private static final String PUT_EVENTHANDLER_MGMT_DESCRIPTION = "Update requested Subscription entry by the given id and parameters";
	private static final String PUT_EVENTHANDLER_MGMT_HTTP_200_MESSAGE = "Updated Subscription entry returned";
	private static final String PUT_EVENTHANDLER_MGMT_HTTP_400_MESSAGE = "Could not update Subscription entry";	
	
	private static final String POST_EVENTHANDLER_SUBSCRIPTION_DESCRIPTION = "Subscription to the events specified in requested Subscription ";
	private static final String POST_EVENTHANDLER_SUBSCRIPTION_HTTP_200_MESSAGE = "Successful subscription.";
	private static final String POST_EVENTHANDLER_SUBSCRIPTION_HTTP_400_MESSAGE = "Unsuccessful subscription.";
	
	private static final String DELETE_EVENTHANDLER_SUBSCRIPTION_DESCRIPTION = "Unsubscription from the events specified in requested Subscription ";
	private static final String DELETE_EVENTHANDLER_SUBSCRIPTION_HTTP_200_MESSAGE = "Successful unsubscription.";
	private static final String DELETE_EVENTHANDLER_SUBSCRIPTION_HTTP_400_MESSAGE = "Unsuccessful unsubscription.";
	
	private static final String POST_EVENTHANDLER_PUBLISH_DESCRIPTION = "Publish event"; 
	private static final String POST_EVENTHANDLER_PUBLISH_HTTP_200_MESSAGE = "Publish event success"; 
	private static final String POST_EVENTHANDLER_PUBLISH_HTTP_400_MESSAGE = "Publish event not success"; 

	private static final String POST_EVENTHANDLER_PUBLISH_AUTH_UPDATE_DESCRIPTION = "Publish authorization change event "; 
	private static final String POST_EVENTHANDLER_PUBLISH_AUTH_UPDATE_HTTP_200_MESSAGE = "Publish authorization change event success"; 
	private static final String POST_EVENTHANDLER_PUBLISH_AUTH_UPDATE_HTTP_400_MESSAGE = "Publish authorization change event not success"; 
	
	private static final String NULL_PARAMETER_ERROR_MESSAGE = " is null.";
	private static final String NULL_OR_BLANK_PARAMETER_ERROR_MESSAGE = " is null or blank.";
	private static final String ID_NOT_VALID_ERROR_MESSAGE = " Id must be greater than 0. ";
	private static final String WRONG_FORMAT_ERROR_MESSAGE = " is in wrong format. ";
	private static final String IS_AFTER_TOLERATED_DIFF_ERROR_MESSAGE = " is further in the future than the tolerated time difference";
	private static final String IS_BEFORE_TOLERATED_DIFF_ERROR_MESSAGE = " is further in the past than the tolerated time difference";

	@Value(CoreCommonConstants.$TIME_STAMP_TOLERANCE_SECONDS_WD)
	private long timeStampTolerance;
	
	private final Logger logger = LogManager.getLogger(EventHandlerController.class);
	
	@Autowired
	private EventHandlerService eventHandlerService;
	
	@Autowired
	private EventHandlerDBService eventHandlerDBService;
	
	@Autowired
	private CommonDBService commonDBService;
	
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
	public String echoService() {
		return "Got it!";
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return requested log entries by the given parameters", response = LogEntryListResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = CoreCommonConstants.QUERY_LOG_ENTRIES_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = CoreCommonConstants.QUERY_LOG_ENTRIES_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CoreCommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CoreCommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path = CoreCommonConstants.OP_QUERY_LOG_ENTRIES, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public LogEntryListResponseDTO getLogEntries(
			@RequestParam(name = CoreCommonConstants.REQUEST_PARAM_PAGE, required = false) final Integer page,
			@RequestParam(name = CoreCommonConstants.REQUEST_PARAM_ITEM_PER_PAGE, required = false) final Integer size,
			@RequestParam(name = CoreCommonConstants.REQUEST_PARAM_DIRECTION, defaultValue = CoreDefaults.DEFAULT_REQUEST_PARAM_DIRECTION_VALUE) final String direction,
			@RequestParam(name = CoreCommonConstants.REQUEST_PARAM_SORT_FIELD, defaultValue = Logs.FIELD_NAME_ID) final String sortField,
			@RequestParam(name = CoreCommonConstants.REQUEST_PARAM_LOG_LEVEL, required = false) final String logLevel,
			@RequestParam(name = CoreCommonConstants.REQUEST_PARAM_FROM, required = false) final String from,
			@RequestParam(name = CoreCommonConstants.REQUEST_PARAM_TO, required = false) final String to,
			@RequestParam(name = CoreCommonConstants.REQUEST_PARAM_LOGGER, required = false) final String loggerStr) { 
		logger.debug("New getLogEntries GET request received with page: {} and item_per page: {}", page, size);
				
		final String origin = CommonConstants.EVENTHANDLER_URI + CoreCommonConstants.OP_QUERY_LOG_ENTRIES;
		final ValidatedPageParams validParameters = CoreUtilities.validatePageParameters(page, size, direction, origin);
		final List<LogLevel> logLevels = CoreUtilities.getLogLevels(logLevel, origin);
		
		try {
			final ZonedDateTime _from = Utilities.parseUTCStringToLocalZonedDateTime(from);
			final ZonedDateTime _to = Utilities.parseUTCStringToLocalZonedDateTime(to);
			
			if (_from != null && _to != null && _to.isBefore(_from)) {
				throw new BadPayloadException("Invalid time interval", HttpStatus.SC_BAD_REQUEST, origin);
			}

			final LogEntryListResponseDTO response = commonDBService.getLogEntriesResponse(validParameters.getValidatedPage(), validParameters.getValidatedSize(), validParameters.getValidatedDirection(), sortField, CoreSystem.EVENTHANDLER, 
																						   logLevels, _from, _to, loggerStr);
			
			logger.debug("Log entries  with page: {} and item_per page: {} retrieved successfully", page, size);
			return response;
		} catch (final DateTimeParseException ex) {
			throw new BadPayloadException("Invalid time parameter", HttpStatus.SC_BAD_REQUEST, origin, ex);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = GET_EVENTHANDLER_MGMT_DESCRIPTION, response = SubscriptionListResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = GET_EVENTHANDLER_MGMT_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = GET_EVENTHANDLER_MGMT_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path = EVENTHANDLER_MGMT_URI, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public SubscriptionListResponseDTO getSubscriptions(
			@RequestParam(name = CoreCommonConstants.REQUEST_PARAM_PAGE, required = false) final Integer page,
			@RequestParam(name = CoreCommonConstants.REQUEST_PARAM_ITEM_PER_PAGE, required = false) final Integer size,
			@RequestParam(name = CoreCommonConstants.REQUEST_PARAM_DIRECTION, defaultValue = CoreDefaults.DEFAULT_REQUEST_PARAM_DIRECTION_VALUE) final String direction,
			@RequestParam(name = CoreCommonConstants.REQUEST_PARAM_SORT_FIELD, defaultValue = CommonConstants.COMMON_FIELD_NAME_ID) final String sortField) {
		logger.debug("New getSubscriptions get request recieved with page: {} and item_per page: {}", page, size);
				
		final ValidatedPageParams validParameters = CoreUtilities.validatePageParameters(page, size, direction, CommonConstants.EVENTHANDLER_URI + EVENTHANDLER_MGMT_URI);
		final SubscriptionListResponseDTO subscriptionsResponse = eventHandlerDBService.getSubscriptionsResponse(validParameters.getValidatedPage(), validParameters.getValidatedSize(), 
																												 validParameters.getValidatedDirection(),
																												 sortField);
		
		logger.debug("Subscriptions  with page: {} and item_per page: {} retrieved successfully", page, size);
		return subscriptionsResponse;
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = GET_EVENTHANDLER_BY_ID_MGMT_DESCRIPTION, response = SubscriptionResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = GET_EVENTHANDLER_BY_ID_MGMT_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = GET_EVENTHANDLER_BY_ID_MGMT_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path = EVENTHANDLER_BY_ID_MGMT_URI, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public SubscriptionResponseDTO getSubscriptionById(@PathVariable(value = PATH_VARIABLE_ID) final long id) {
		logger.debug("New getSubscriptionById get request recieved with id: {}", id);
		
		final String origin = CommonConstants.EVENTHANDLER_URI + EVENTHANDLER_BY_ID_MGMT_URI;
		if (id < 1) {
			throw new BadPayloadException(ID_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		final SubscriptionResponseDTO subscriptionResponse = eventHandlerDBService.getSubscriptionByIdResponse(id);
		
		logger.debug("Subscription entry with id: {} successfully retrieved", id);
		return subscriptionResponse;
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = DELETE_EVENTHANDLER_MGMT_DESCRIPTION, tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = DELETE_EVENTHANDLER_MGMT_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = DELETE_EVENTHANDLER_MGMT_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@DeleteMapping(path = EVENTHANDLER_BY_ID_MGMT_URI, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public void deleteSubscription(@PathVariable(value = PATH_VARIABLE_ID) final long id) {
		logger.debug("New deleteSubscription delete request recieved with id: {}", id);
		
		final String origin = CommonConstants.EVENTHANDLER_URI + EVENTHANDLER_BY_ID_MGMT_URI;
		if (id < 1) {
			throw new BadPayloadException(ID_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		eventHandlerDBService.deleteSubscriptionResponse(id);
		
		logger.debug("Subscription entry with id: {} successfully deleted", id);
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = PUT_EVENTHANDLER_MGMT_DESCRIPTION, response = SubscriptionResponseDTO.class, tags = { CoreCommonConstants.SWAGGER_TAG_MGMT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = PUT_EVENTHANDLER_MGMT_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = PUT_EVENTHANDLER_MGMT_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@PutMapping(path = EVENTHANDLER_BY_ID_MGMT_URI, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public SubscriptionResponseDTO updateSubscription(@PathVariable(value = PATH_VARIABLE_ID) final long id, @RequestBody final SubscriptionRequestDTO request) {
		logger.debug("New updateSubscription put request recieved with id: {}", id);
		
		final String origin = CommonConstants.EVENTHANDLER_URI + EVENTHANDLER_BY_ID_MGMT_URI;
		if (id < 1) {
			throw new BadPayloadException(ID_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		checkSubscriptionRequestDTO(request, origin);
		final SubscriptionResponseDTO response = eventHandlerService.updateSubscriptionResponse(id, request);
		
		logger.debug("Subscription entry with id: {} successfully updated", id);
		return response;
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = POST_EVENTHANDLER_SUBSCRIPTION_DESCRIPTION, tags = { CoreCommonConstants.SWAGGER_TAG_CLIENT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = POST_EVENTHANDLER_SUBSCRIPTION_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = POST_EVENTHANDLER_SUBSCRIPTION_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@PostMapping(path = CommonConstants.OP_EVENTHANDLER_SUBSCRIBE, consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public void subscribe(@RequestBody final SubscriptionRequestDTO request) {
		logger.debug("subscription started ...");
		
		final String origin = CommonConstants.EVENTHANDLER_URI + CommonConstants.OP_EVENTHANDLER_SUBSCRIBE;
		checkSubscriptionRequestDTO(request, origin);
		
	    eventHandlerService.subscribe(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = DELETE_EVENTHANDLER_SUBSCRIPTION_DESCRIPTION,  tags = { CoreCommonConstants.SWAGGER_TAG_CLIENT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = DELETE_EVENTHANDLER_SUBSCRIPTION_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = DELETE_EVENTHANDLER_SUBSCRIPTION_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@DeleteMapping(path = CommonConstants.OP_EVENTHANDLER_UNSUBSCRIBE)
	@ResponseBody public void unsubscribe(
			@RequestParam(CommonConstants.OP_EVENTHANDLER_UNSUBSCRIBE_REQUEST_PARAM_EVENT_TYPE) final String eventType,
			@RequestParam(CommonConstants.OP_EVENTHANDLER_UNSUBSCRIBE_REQUEST_PARAM_SUBSCRIBER_SYSTEM_NAME) final String subscriberName,
			@RequestParam(CommonConstants.OP_EVENTHANDLER_UNSUBSCRIBE_REQUEST_PARAM_SUBSCRIBER_ADDRESS) final String subscriberAddress,
			@RequestParam(CommonConstants.OP_EVENTHANDLER_UNSUBSCRIBE_REQUEST_PARAM_SUBSCRIBER_PORT) final int subscriberPort) {
		logger.debug("unSubscription started ...");
		
		final String origin = CommonConstants.EVENTHANDLER_URI + CommonConstants.OP_EVENTHANDLER_SUBSCRIBE;
		checkUnsubscribeParameters(eventType, subscriberName, subscriberAddress, subscriberPort, origin);
		
	    eventHandlerService.unsubscribe(eventType, subscriberName, subscriberAddress, subscriberPort);
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = POST_EVENTHANDLER_PUBLISH_DESCRIPTION, tags = { CoreCommonConstants.SWAGGER_TAG_CLIENT })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = POST_EVENTHANDLER_PUBLISH_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = POST_EVENTHANDLER_PUBLISH_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@PostMapping(path = CommonConstants.OP_EVENTHANDLER_PUBLISH, consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public void publish(@RequestBody final EventPublishRequestDTO request) {
		logger.debug("publish started ...");
		
		final String origin = CommonConstants.EVENTHANDLER_URI + CommonConstants.OP_EVENTHANDLER_PUBLISH;
		checkEventPublishRequestDTO(request, origin);
		validateTimeStamp(request, origin);
		
	    eventHandlerService.publishResponse(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = POST_EVENTHANDLER_PUBLISH_AUTH_UPDATE_DESCRIPTION, tags = { CoreCommonConstants.SWAGGER_TAG_PRIVATE })
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = POST_EVENTHANDLER_PUBLISH_AUTH_UPDATE_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = POST_EVENTHANDLER_PUBLISH_AUTH_UPDATE_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@PostMapping(path = CommonConstants.OP_EVENTHANDLER_PUBLISH_AUTH_UPDATE, consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public void publishSubscriberAuthorizationUpdate(@RequestBody final EventPublishRequestDTO request) {
		logger.debug("publishSubscriberAuthorizationUpdate started ...");
		
		final String origin = CommonConstants.EVENTHANDLER_URI + CommonConstants.OP_EVENTHANDLER_PUBLISH_AUTH_UPDATE;
		checkEventPublishRequestDTO(request, origin);
		validateTimeStamp(request, origin);
		
	    eventHandlerService.publishSubscriberAuthorizationUpdateResponse(request);
	}
	
	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------	
	private void checkSubscriptionRequestDTO(final SubscriptionRequestDTO request, final String origin) {
		logger.debug("checkSubscriptionRequestDTO started ...");
		
		if (request == null) {
			throw new BadPayloadException("Request" + NULL_PARAMETER_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		checkSystemRequestDTO(request.getSubscriberSystem(), origin);
		
		if (Utilities.isEmpty(request.getEventType())) {
			throw new BadPayloadException("Request.EventType" + NULL_OR_BLANK_PARAMETER_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);	
		}
		
		if (Utilities.isEmpty(request.getNotifyUri())) {
			throw new BadPayloadException("Request.NotifyUri" + NULL_OR_BLANK_PARAMETER_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);	
		}		
		
		if (request.getMatchMetaData() && (request.getFilterMetaData() == null || request.getFilterMetaData().isEmpty())) {
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
		
		if (Utilities.isEmpty( request.getEventType())) {
			throw new BadPayloadException("Request.EventType" + NULL_OR_BLANK_PARAMETER_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		if (Utilities.isEmpty( request.getPayload())) {
			throw new BadPayloadException("Request.Payload" + NULL_OR_BLANK_PARAMETER_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
		}	
	}
	
	//-------------------------------------------------------------------------------------------------
	// This method may CHANGE the content of EventPublishRequestDTO
	private void validateTimeStamp(final EventPublishRequestDTO request, final String origin) {
		logger.debug("validateTimeStamp started ...");
		
		if (Utilities.isEmpty(request.getTimeStamp())) {
			request.setTimeStamp(Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()));
		} else	{
			final ZonedDateTime now = ZonedDateTime.now();
			final ZonedDateTime timeStamp;

			try {
				timeStamp = Utilities.parseUTCStringToLocalZonedDateTime(request.getTimeStamp());
			} catch (final DateTimeParseException ex) {
				throw new BadPayloadException("Request.TimeStamp" + WRONG_FORMAT_ERROR_MESSAGE + ex, HttpStatus.SC_BAD_REQUEST, origin);
			}
			
			if (timeStamp.isAfter(now.plusSeconds(timeStampTolerance))) {
				throw new BadPayloadException("Request.TimeStamp" + IS_AFTER_TOLERATED_DIFF_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
			}
			
			if (timeStamp.isBefore(now.minusSeconds(timeStampTolerance))) {
				throw new BadPayloadException("Request.TimeStamp" + IS_BEFORE_TOLERATED_DIFF_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
			}
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private void checkUnsubscribeParameters(final String eventType, final String subscriberName, final String subscriberAddress, final int subscriberPort, final String origin) {
		logger.debug("checkUnsubscribeParameters started...");
		
		if (Utilities.isEmpty(eventType)) {
			throw new BadPayloadException("Event Type is blank", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		if (Utilities.isEmpty(subscriberName)) {
			throw new BadPayloadException("Name of the subscriber system is blank", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		if (Utilities.isEmpty(subscriberAddress)) {
			throw new BadPayloadException("Address of the subscriber system is blank", HttpStatus.SC_BAD_REQUEST, origin);
		}
	}
}