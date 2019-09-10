package eu.arrowhead.core.eventhandler.database.service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.entity.System;
import eu.arrowhead.common.database.entity.EventType;
import eu.arrowhead.common.database.entity.Subscription;
import eu.arrowhead.common.database.entity.SubscriptionPublisherConnection;
import eu.arrowhead.common.database.repository.EventTypeRepository;
import eu.arrowhead.common.database.repository.SubscriptionPublisherConnectionRepository;
import eu.arrowhead.common.database.repository.SubscriptionRepository;
import eu.arrowhead.common.database.repository.SystemRepository;
import eu.arrowhead.common.dto.DTOConverter;
import eu.arrowhead.common.dto.DTOUtilities;
import eu.arrowhead.common.dto.EventPublishRequestDTO;
import eu.arrowhead.common.dto.EventPublishResponseDTO;
import eu.arrowhead.common.dto.IdIdListDTO;
import eu.arrowhead.common.dto.SubscriptionListResponseDTO;
import eu.arrowhead.common.dto.SubscriptionRequestDTO;
import eu.arrowhead.common.dto.SubscriptionResponseDTO;
import eu.arrowhead.common.dto.SystemRequestDTO;
import eu.arrowhead.common.dto.SystemResponseDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.InvalidParameterException;

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
	private SubscriptionPublisherConnectionRepository subscriptionPublisherConnectionRepository;
	
	@Autowired
	private EventTypeRepository eventTypeRepository;
	
	@Autowired
	private SystemRepository systemRepository;
	
	@Value(CommonConstants.$SERVER_SSL_ENABLED_WD)
	private boolean secure;

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
	public Subscription registerSubscription(final SubscriptionRequestDTO request,
			final Set<SystemResponseDTO> authorizedPublishers) {
		logger.debug("registerSubscription started ...");
		
		final Subscription subscription = validateSubscriptionRequestDTO(request);
		try {
			final Subscription subscriptionEntry = subscriptionRepository.save(subscription);
			addAndSaveSubscriptionEntryPublisherConnections(subscriptionEntry, request, authorizedPublishers);
			
			return subscriptionRepository.saveAndFlush(subscriptionEntry);
			
			
		}catch (final Exception ex) {
			
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	public Set<Subscription> getInvolvedSubscriptions(final EventPublishRequestDTO request) {
		logger.debug("getInvolvedSubscriptions started ...");
		
		final EventType validEventType = validateEventType(request.getEventType());
		
		if (!secure) {
			
			return subscriptionRepository.findAllByEventType(validEventType);
			
		}
		
		
		//TODO maybe handle this in separte method ...
		final Set<Subscription> involvedSubscriptions = subscriptionRepository.findAllByEventType(validEventType);
		final System validProviderSystem = validateSystemRequestDTO(request.getSource());
		
		return filterInvolvedSubscriptionsByAuthorizedProviders(involvedSubscriptions, validProviderSystem);
		
	}
	
	public void publishSubscriberAuthorizationUpdateRequest(final IdIdListDTO request) {
		logger.debug("publishSubscriberAuthorizationUpdateRequest started ...");
		
		//TODO implement method logic here
		
		//get involved subscriptions by id
		//get involved subscriptions filtered by onlyPreRequestedPublishers
		//for onlyPre... get connections and if publisher in idList update it to true if not in list update to false
		//for not onlyPre ... update or create connection for all publishers in idList  to true 
		//final Set<Subscription> involvedSubscriptions = subscriptionRepository.findAllBySubscriberSystemId();
		
	}
	
	//=================================================================================================
	//Assistant methods

	//-------------------------------------------------------------------------------------------------
	private Subscription validateSubscriptionRequestDTO(final SubscriptionRequestDTO request) {
		logger.debug("validatesubscriptionRequestDTO started ...");
		
		final System validSubscriberSystem = validateSystemRequestDTO(request.getSubscriberSystem());
		final EventType validEventType = validateEventType(request.getEventType());
		
		final Subscription subscription = new Subscription();
		subscription.setSubscriberSystem(validSubscriberSystem);
		subscription.setEventType(validEventType);
		subscription.setNotifyUri(request.getNotifyUri());
		subscription.setFilterMetaData(Utilities.map2Text(request.getFilterMetaData()));
		subscription.setOnlyPredefinedPublishers(request.getSources() != null && !request.getSources().isEmpty()); //TODO orginize to method
		subscription.setMatchMetaData(request.getMatchMetaData());
		//TODO validate dates by comparing to currentTime and threshold
		subscription.setStartDate(Utilities.parseUTCStringToLocalZonedDateTime(request.getStartDate()));
		subscription.setEndDate(Utilities.parseUTCStringToLocalZonedDateTime(request.getEndDate()));
		
		return subscription;
	}
	
	//-------------------------------------------------------------------------------------------------
	private System validateSystemRequestDTO(final SystemRequestDTO systemRequestDTO) {
		logger.debug("validateSystemRequestDTO started...");

		final String address = systemRequestDTO.getAddress().trim().toLowerCase();
		final String systemName = systemRequestDTO.getSystemName().trim().toLowerCase();
		final int port = systemRequestDTO.getPort();
		
		final Optional<System> systemOptional = systemRepository.findBySystemNameAndAddressAndPort(systemName, address, port);
		if (systemOptional.isEmpty()) {
			throw new InvalidParameterException("System by systemName: " + systemName + ", address: " + address + ", port: " + port + NOT_IN_DB_ERROR_MESSAGE);
		}
		
		return systemOptional.get();
	}	

	//-------------------------------------------------------------------------------------------------
	private EventType validateEventType(final String eventType) {
		logger.debug("validateEventType started...");
		
		final String validEventTypeName = eventType.toUpperCase().trim();//TODO create normalizer in Utilities
		final Optional<EventType> eventTypeOptional = eventTypeRepository.findByEventTypeName( validEventTypeName );
		if ( eventTypeOptional.isEmpty() ) {
			
			return eventTypeRepository.saveAndFlush(new EventType( validEventTypeName ));
		}
		
		return eventTypeOptional.get();
	}	

	//-------------------------------------------------------------------------------------------------
	private Set<System> getPreferredPublisherSystems(final Set<SystemRequestDTO> sources) {
		logger.debug("getPreferredPublisherSystems started...");
		
		final Set<System> preferredSystems = new HashSet<>();
		
		for (final SystemRequestDTO source : sources) {
			
			preferredSystems.add(validateSystemRequestDTO(source));
		}
		
		return preferredSystems;
	}
	
	//-------------------------------------------------------------------------------------------------
	private void addAndSaveSubscriptionEntryPublisherConnections(final Subscription subscriptionEntry,
			final SubscriptionRequestDTO request, final Set<SystemResponseDTO> authorizedPublishers) {
		logger.debug("addAndSaveSubscriptionEntryPublisherConnections started...");
		
		if (subscriptionEntry.isOnlyPredefinedPublishers()) {
			
			final Set<System> preferredPublisherSystems = getPreferredPublisherSystems( request.getSources());
			
			for (final System system : preferredPublisherSystems) {
				final SubscriptionPublisherConnection conn = new SubscriptionPublisherConnection(subscriptionEntry, system);
				conn.setAuthorized(false);
				for (final SystemResponseDTO systemResponseDTO : authorizedPublishers) {
					
					if (DTOUtilities.equalsSystemInResponseAndRequest(systemResponseDTO, DTOConverter.convertSystemToSystemRequestDTO(system))) {
						conn.setAuthorized(true);
						break;
					}
				}
				
				subscriptionEntry.getPublisherConnections().add(conn);
			}
		} else {
			
			for (final SystemResponseDTO systemResponseDTO : authorizedPublishers) {
				
				final System system = new System(
						systemResponseDTO.getSystemName(), 
						systemResponseDTO.getAddress(), 
						systemResponseDTO.getPort(), 
						systemResponseDTO.getAuthenticationInfo());
				system.setId(systemResponseDTO.getId());
						
				final SubscriptionPublisherConnection conn = new SubscriptionPublisherConnection(subscriptionEntry, system);
				conn.setAuthorized(true);
				
				subscriptionEntry.getPublisherConnections().add(conn);
			}
		}
		subscriptionPublisherConnectionRepository.saveAll(subscriptionEntry.getPublisherConnections());
	
	}

	//-------------------------------------------------------------------------------------------------
	private Set<Subscription> filterInvolvedSubscriptionsByAuthorizedProviders(final Set<Subscription> involvedSubscriptions,
			final System validProviderSystem) {
		logger.debug("filterInvolvedSubscriptionsByAuthorizedProviders started...");
		
		final Set<Subscription> involvedConnections = subscriptionPublisherConnectionRepository.findAllBySystemAndAuthorized(validProviderSystem, true);

		involvedConnections.retainAll(involvedSubscriptions);
		
		return involvedConnections;
	}

}