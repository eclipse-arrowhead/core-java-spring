package eu.arrowhead.core.eventhandler.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.arrowhead.common.dto.DTOConverter;
import eu.arrowhead.common.dto.SubscriptionRequestDTO;
import eu.arrowhead.common.dto.SubscriptionResponseDTO;
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
				 
		return DTOConverter.convertSubscriptionToSubscriptionResponseDTO(eventHandlerDBService.subscription(request));
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
				
		// TODO Implement additional method logic here 
		return null;
	}
}
