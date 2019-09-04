package eu.arrowhead.core.eventhandler.database.service;

import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import eu.arrowhead.common.database.entity.Subscription;
import eu.arrowhead.common.database.repository.SubscriptionRepository;
import eu.arrowhead.common.database.repository.EventTypeRepository;
import eu.arrowhead.common.database.repository.SystemRepository;
import eu.arrowhead.common.dto.SubscriptionListResponseDTO;
import eu.arrowhead.common.dto.SubscriptionRequestDTO;
import eu.arrowhead.common.dto.SubscriptionResponseDTO;
import eu.arrowhead.common.dto.SystemRequestDTO;
import eu.arrowhead.common.dto.SystemResponseDTO;

@Service
public class EventHandlerDBService {
	//=================================================================================================
	// members
	
	private static final String LESS_THAN_ONE_ERROR_MESSAGE= " must be greater than zero.";
	private static final String NOT_AVAILABLE_SORTABLE_FIELD_ERROR_MESSAGE = "The following sortable field  is not available : ";
	private static final String NOT_IN_DB_ERROR_MESSAGE = " is not available in database";
	private static final String EMPTY_OR_NULL_ERROR_MESSAGE = " is empty or null";
	private static final String NULL_ERROR_MESSAGE = " is null";
	private static final String NOT_VALID_ERROR_MESSAGE = " is not valid.";
	private static final String NOT_FOREIGN_ERROR_MESSAGE = " is not foreign";
	
	private static final Logger logger = LogManager.getLogger(EventHandlerDBService.class);
	
	@Autowired
	private SubscriptionRepository subscriptionRepository;
	
	@Autowired
	private EventTypeRepository eventTypeRepository;
	
	@Autowired
	private SystemRepository systemRepository;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public SubscriptionListResponseDTO getSubscriptionsRequest(final int validatedPage, final int validatedSize,
			final Direction validatedDirecion, final String sortField) {
		logger.debug("getEventHandlersRequest started ...");
		
		// TODO implement additional method logic
		return null;
	}

	//-------------------------------------------------------------------------------------------------
	public SubscriptionResponseDTO getSubscriptionByIdRequest(final long id) {
		logger.debug("getSubscriptionByIdRequest started ...");
		
		// TODO implement additional method logic
		return null;
	}

	//-------------------------------------------------------------------------------------------------
	public void deleteSubscriptionRequest(final long id) {
		logger.debug("deleteSubscriptionRequest started ...");
		
		// TODO implement additional method logic
		return;
	}

	//-------------------------------------------------------------------------------------------------
	public SubscriptionResponseDTO updateSubscriptionRequest(final long id, final SubscriptionRequestDTO subscriptionRequestDTO) {
		logger.debug("updateSubscriptionRequest started ...");
		
		// TODO implement additional method logic
		return null;
	}	

	//-------------------------------------------------------------------------------------------------
	public Subscription subscription(final SubscriptionRequestDTO subscriptionFilterRequestDTO) {
		logger.debug("subscription started ...");
		
		final Subscription validSubscription = validateSubscriptionRequestDTO(subscriptionFilterRequestDTO);		
		
		return subscriptionRepository.saveAndFlush(validSubscription);

	}
	
	//-------------------------------------------------------------------------------------------------
	public Subscription registerSubscription(final SystemRequestDTO subscriber, final SubscriptionRequestDTO request,
			final boolean onlyPreferred, final Set<SystemResponseDTO> authorizedPublishers) {
		// TODO Auto-generated method stub
		return null;
	}
	
	//=================================================================================================
	//Assistant methods

	//-------------------------------------------------------------------------------------------------
	private Subscription validateSubscriptionRequestDTO(final SubscriptionRequestDTO subscriptionRequestDTO) {
		logger.debug("validatesubscriptionRequestDTO started ...");
		
		final Subscription subscription = new Subscription();
		
		final Set<String> systemResponseDTOJsonSet = new HashSet();
		for (final SystemRequestDTO systemRequestDTO : subscriptionRequestDTO.getSources()) {
			//TODO checkIf system is valid and is in db, then convert to system response then convert to Json then add to SET
		}
		// TODO implement additional method logic
		
		return null;
	}

}