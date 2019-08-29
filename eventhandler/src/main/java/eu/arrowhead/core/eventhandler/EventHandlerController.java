package eu.arrowhead.core.eventhandler;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.Defaults;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.EventFilterRequestDTO;
import eu.arrowhead.common.dto.EventFilterResponseDTO;
import eu.arrowhead.common.dto.SystemRequestDTO;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.core.eventhandler.service.EventHandlerService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@CrossOrigin(maxAge = Defaults.CORS_MAX_AGE, allowCredentials = Defaults.CORS_ALLOW_CREDENTIALS, 
allowedHeaders = { HttpHeaders.ORIGIN, HttpHeaders.CONTENT_TYPE, HttpHeaders.ACCEPT, HttpHeaders.AUTHORIZATION }
)
@RestController(CommonConstants.EVENTHANDLER_URI)
public class EventHandlerController {
	
	//=================================================================================================
	// members
	
	private static final String POST_EVENTHANDLER_SUBSCRIPTION_DESCRIPTION = "Subcribtion to the events specified in requested eventFilter ";
	private static final String POST_EVENTHANDLER_SUBSCRIPTION_HTTP_200_MESSAGE = "Successful subscription.";
	private static final String POST_EVENTHANDLER_SUBSCRIPTION_HTTP_400_MESSAGE = "Unsuccessful subscription.";

	private static final String NULL_PARAMETER_ERROR_MESSAGE = " is null.";
	private static final String NULL_OR_BLANK_PARAMETER_ERROR_MESSAGE = " is null or blank.";
	private static final String ID_NOT_VALID_ERROR_MESSAGE = " Id must be greater than 0. ";
	
	private final Logger logger = LogManager.getLogger(EventHandlerController.class);
	
	@Autowired
	EventHandlerService eventHandlerService;
	
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
	@ApiOperation(value = POST_EVENTHANDLER_SUBSCRIPTION_DESCRIPTION, response = EventFilterResponseDTO.class)
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = POST_EVENTHANDLER_SUBSCRIPTION_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = POST_EVENTHANDLER_SUBSCRIPTION_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@PostMapping(path = CommonConstants.OP_EVENTHANDLER_SUBSCRIPTION, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public EventFilterResponseDTO subscription(@RequestBody final EventFilterRequestDTO request) {
		logger.debug("subscription started ...");
		
		final String origin = CommonConstants.EVENTHANDLER_URI + CommonConstants.OP_EVENTHANDLER_SUBSCRIPTION;
		checkEventFilterRequestDTO(request, origin);
		
	    return eventHandlerService.subscriptionRequest(request);
	}
	
	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------	
	private void checkEventFilterRequestDTO( final EventFilterRequestDTO request, final String origin) {
		logger.debug("checkEventFilterRequestDTO started ...");
		
		if (request == null) {
			throw new BadPayloadException("Request" + NULL_PARAMETER_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		checkSystemRequestDTO(request.getSubscriberSystem(), origin);
		
		if ( Utilities.isEmpty( request.getEventType() )) {
			throw new BadPayloadException("Request.EventType " + NULL_OR_BLANK_PARAMETER_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);	
		}
		
		if ( Utilities.isEmpty( request.getNotifyUri() )) {
			throw new BadPayloadException("Request.NotifyUri " + NULL_OR_BLANK_PARAMETER_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);	
		}
		
		if (request.getMatchMetaData() == null) {
			throw new BadPayloadException("Request.MatchMetaData" + NULL_PARAMETER_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		if ( request.getMatchMetaData() && ( request.getFilterMetaData() == null || request.getFilterMetaData().isEmpty() )) {
			
			throw new BadPayloadException("Request.MatchMetaData is true but Request.FilterMetaData " + NULL_PARAMETER_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
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
}