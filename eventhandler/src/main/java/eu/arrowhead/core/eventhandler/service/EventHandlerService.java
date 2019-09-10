package eu.arrowhead.core.eventhandler.service;

import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.entity.EventType;
import eu.arrowhead.common.database.entity.Subscription;
import eu.arrowhead.common.dto.AuthorizationSubscriptionCheckResponseDTO;
import eu.arrowhead.common.dto.DTOConverter;
import eu.arrowhead.common.dto.SubscriptionRequestDTO;
import eu.arrowhead.common.dto.SubscriptionResponseDTO;
import eu.arrowhead.common.dto.SystemRequestDTO;
import eu.arrowhead.common.dto.SystemResponseDTO;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.common.dto.EventPublishRequestDTO;
import eu.arrowhead.common.dto.EventPublishResponseDTO;
import eu.arrowhead.core.eventhandler.database.service.EventHandlerDBService;

@Service
public class EventHandlerService {

	//=================================================================================================
	// members
	
	private static final String NULL_PARAMETER_ERROR_MESSAGE = " is null.";
	private static final String NULL_OR_BLANK_PARAMETER_ERROR_MESSAGE = " is null or blank.";
	private static final String LESS_THAN_ONE_ERROR_MESSAGE= " must be greater than zero.";
	private static final String MORE_THAN_ONE_ERROR_MESSAGE= " must not have more than one element.";
	private static final String INVALID_TYPE_ERROR_MESSAGE = " is not valid.";
	
	private static final Logger logger = LogManager.getLogger(EventHandlerService.class);
	
	
	@Autowired
	private EventHandlerDriver eventHandlerDriver;
	
	@Autowired
	private EventHandlerDBService eventHandlerDBService;
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public SubscriptionResponseDTO subscriptionRequest( final SubscriptionRequestDTO request) {
		logger.debug("subscriptionRequest started ...");
		
		if (request == null) {
			
			throw new InvalidParameterException("Request" + NULL_PARAMETER_ERROR_MESSAGE);
		}
		
		checkSystemRequestDTO(request.getSubscriberSystem(), true);
		final SystemRequestDTO subscriber = request.getSubscriberSystem();
		
		final Set<SystemResponseDTO> authorizedPublishers = eventHandlerDriver.getAuthorizedPublishers(subscriber);
		
		//final AuthorizationSubscriptionCheckResponseDTO 
		return DTOConverter.convertSubscriptionToSubscriptionResponseDTO(eventHandlerDBService.registerSubscription(request, authorizedPublishers));
	}

	//-------------------------------------------------------------------------------------------------
	public void unSubscriptionRequest( final SubscriptionRequestDTO request) {
		logger.debug("unSubscriptionRequest started ...");
		
		// TODO Implement additional method logic here 
		return ;
	}

	//-------------------------------------------------------------------------------------------------
	public EventPublishResponseDTO publishRequest(final EventPublishRequestDTO request) {
		logger.debug("publishRequest started ...");
		
		checkSystemRequestDTO(request.getSource(), false);
		
		final Set<Subscription> involvedSubscriptions = eventHandlerDBService.getInvolvedSubscriptions(request);
		
		eventHandlerDriver.publishEvent(request, involvedSubscriptions);
		
		return new EventPublishResponseDTO(); //always return empty response
	}
	
	//-------------------------------------------------------------------------------------------------
	public void publishSubscriberAuthorizationUpdateRequest(final EventPublishRequestDTO request) {
		logger.debug("publishSubscriberAuthorizationUpdateRequest started ...");
		
		validateAuthorizationUpdateEventType( request.getEventType() );
		final Long subscriberSystemId = validateAuthorizationUpdatePayload( request.getPayload() );
		
		final List<Subscription> involvedSubscriptions = eventHandlerDBService.getInvolvedSubscriptionsBySubscriberSystemId( subscriberSystemId );
				
		final SystemRequestDTO subscriber = DTOConverter.convertSystemToSystemRequestDTO( involvedSubscriptions.get(0).getSubscriberSystem() );		
		final Set<SystemResponseDTO> authorizedPublishers = eventHandlerDriver.getAuthorizedPublishers(subscriber);
		
		eventHandlerDBService.updateSubscriberAuthorization( involvedSubscriptions, authorizedPublishers );
		
	}

	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	private void checkSystemRequestDTO(final SystemRequestDTO system, final boolean portRangeCheck) {
		logger.debug("checkSystemRequestDTO started...");
		
		if (system == null) {
			throw new InvalidParameterException("System" + NULL_PARAMETER_ERROR_MESSAGE);
		}
		
		if (Utilities.isEmpty(system.getSystemName())) {
			throw new InvalidParameterException("System name" + NULL_OR_BLANK_PARAMETER_ERROR_MESSAGE);
		}
		
		if (Utilities.isEmpty(system.getAddress())) {
			throw new InvalidParameterException("System address" + NULL_OR_BLANK_PARAMETER_ERROR_MESSAGE);
		}
		
		if (system.getPort() == null) {
			throw new InvalidParameterException("System port" + NULL_PARAMETER_ERROR_MESSAGE);
		}
		
		final int validatedPort = system.getPort().intValue();
		if (portRangeCheck && (validatedPort < CommonConstants.SYSTEM_PORT_RANGE_MIN || validatedPort > CommonConstants.SYSTEM_PORT_RANGE_MAX)) {
			throw new InvalidParameterException("System port must be between " + CommonConstants.SYSTEM_PORT_RANGE_MIN + " and " + CommonConstants.SYSTEM_PORT_RANGE_MAX + ".");
		}
	}	
	
	//-------------------------------------------------------------------------------------------------
	private void validateAuthorizationUpdateEventType(final String eventType) {
		logger.debug("validateAuthorizationUpdateEventType started...");
		
		if (Utilities.isEmpty( eventType )) {
			throw new InvalidParameterException("EventType" + NULL_OR_BLANK_PARAMETER_ERROR_MESSAGE);
		}
		
		if (!eventType.equalsIgnoreCase( CommonConstants.EVENT_TYPE_SUBSCRIBER_AUTH_UPDATE)) {
			throw new InvalidParameterException("EventType" + INVALID_TYPE_ERROR_MESSAGE);
		}
		
	}

	//-------------------------------------------------------------------------------------------------
	private Long validateAuthorizationUpdatePayload(final String payload) {
		logger.debug("validateAuthorizationUpdatePayload started...");
		
		try {
			final Long id = Long.parseLong(payload);
			
			return id;
		} catch (NumberFormatException ex) {
			
			throw new InvalidParameterException("Payload" + INVALID_TYPE_ERROR_MESSAGE);
		}
	}

}
