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

package eu.arrowhead.core.eventhandler.database.service;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.entity.EventType;
import eu.arrowhead.common.database.entity.Subscription;
import eu.arrowhead.common.database.entity.SubscriptionPublisherConnection;
import eu.arrowhead.common.database.entity.System;
import eu.arrowhead.common.database.repository.EventTypeRepository;
import eu.arrowhead.common.database.repository.SubscriptionPublisherConnectionRepository;
import eu.arrowhead.common.database.repository.SubscriptionRepository;
import eu.arrowhead.common.database.repository.SystemRepository;
import eu.arrowhead.common.dto.internal.DTOConverter;
import eu.arrowhead.common.dto.shared.AddressType;
import eu.arrowhead.common.dto.shared.EventPublishRequestDTO;
import eu.arrowhead.common.dto.shared.SubscriptionListResponseDTO;
import eu.arrowhead.common.dto.shared.SubscriptionRequestDTO;
import eu.arrowhead.common.dto.shared.SubscriptionResponseDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.common.dto.shared.SystemResponseDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.common.processor.SpecialNetworkAddressTypeDetector;

@Service
public class EventHandlerDBService {
	
	//=================================================================================================
	// members
	
	private static final String LESS_THAN_ONE_ERROR_MESSAGE= " must be greater than zero.";
	private static final String NOT_AVAILABLE_SORTABLE_FIELD_ERROR_MESSAGE = " sortable field is not available.";
	private static final String NOT_IN_DB_ERROR_MESSAGE = " is not available in database";
	private static final String EMPTY_OR_NULL_ERROR_MESSAGE = " is empty or null";
	private static final String NULL_ERROR_MESSAGE = " is null";
	private static final String VIOLATES_UNIQUE_CONSTRAINT = " violates unique constraint rules";
	private static final String IS_BEFORE_TOLERATED_DIFF_ERROR_MESSAGE = " is further in the past than the tolerated time difference";
	private static final String INVALID_TYPE_ERROR_MESSAGE = " is not valid.";

	private static final Logger logger = LogManager.getLogger(EventHandlerDBService.class);
	
	@Autowired
	private SubscriptionRepository subscriptionRepository;
	
	@Autowired
	private SubscriptionPublisherConnectionRepository subscriptionPublisherConnectionRepository;
	
	@Autowired
	private EventTypeRepository eventTypeRepository;
	
	@Autowired
	private SystemRepository systemRepository;
	
	@Autowired
	private SpecialNetworkAddressTypeDetector networkAddressTypeDetector;
	
	@Value(CommonConstants.$SERVER_SSL_ENABLED_WD)
	private boolean secure;

	@Value(CoreCommonConstants.$TIME_STAMP_TOLERANCE_SECONDS_WD)
	private long timeStampTolerance;
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public SubscriptionListResponseDTO getSubscriptionsResponse(final int page, final int size,	final Direction direction, final String sortField) {
		logger.debug("getSubscriptionsResponse started ...");
		
		final Page<Subscription> entries = getSubscriptions(page, size, direction, sortField);

		return DTOConverter.convertSubscriptionPageToSubscriptionListResponseDTO(entries);
	}

	//-------------------------------------------------------------------------------------------------
	public Page<Subscription> getSubscriptions(final int page, final int size, final Direction direction, final String sortField) {
		logger.debug("getSubscriptions started ...");
		
		final int validatedPage = page < 0 ? 0 : page;
		final int validatedSize = size < 1 ? Integer.MAX_VALUE : size;
		final Direction validatedDirection = direction == null ? Direction.ASC : direction;
		final String validatedSortField = Utilities.isEmpty(sortField) ? CommonConstants.COMMON_FIELD_NAME_ID : sortField.trim();
		
		if (!Subscription.SORTABLE_FIELDS_BY.contains(validatedSortField)) {
			throw new InvalidParameterException(validatedSortField + NOT_AVAILABLE_SORTABLE_FIELD_ERROR_MESSAGE);
		}
		
		try {
			return subscriptionRepository.findAll(PageRequest.of(validatedPage, validatedSize, validatedDirection, validatedSortField));
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	public List<Subscription> getSubscriptionsList() {
		logger.debug("getSubscriptionsList started ...");
		
		final Page<Subscription> subscriptions;
		
		final int validatedPage = 0;
		final int validatedSize = Integer.MAX_VALUE ;
		final Direction validatedDirection = Direction.ASC;
		final String validatedSortField = CommonConstants.COMMON_FIELD_NAME_ID;
		
		try {
			subscriptions = subscriptionRepository.findAll(PageRequest.of(validatedPage, validatedSize, validatedDirection, validatedSortField));
		
			if (subscriptions == null || subscriptions.getContent() == null) {
				return List.of();
			}
			
			return subscriptions.getContent();
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}

	//-------------------------------------------------------------------------------------------------
	public SubscriptionResponseDTO getSubscriptionByIdResponse(final long id) {
		logger.debug("getSubscriptionByIdResponse started ...");
		
		return DTOConverter.convertSubscriptionToSubscriptionResponseDTO(getSubscriptionById(id));
	}

	//-------------------------------------------------------------------------------------------------
	public Subscription getSubscriptionById(final long id) {
		logger.debug("getSubscriptionById started ...");
		
		if (id < 1) {
			throw new InvalidParameterException("SubscriberSystemId" + LESS_THAN_ONE_ERROR_MESSAGE);
		}
		
		try {
			final Optional<Subscription> subcriptionOptional = subscriptionRepository.findById(id);
			if (subcriptionOptional.isPresent()) {
				return subcriptionOptional.get();
			} else {
				throw new InvalidParameterException("Subscription with id of '" + id + "' not exists");
			}
		} catch (final InvalidParameterException ex) {
			throw ex;
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	public Subscription getSubscriptionBySubscriptionRequestDTO(final SubscriptionRequestDTO subscriptionRequestDTO) {
		logger.debug("getSubscriptionBySubscriptionRequestDTO started ...");

		final Subscription subscription = validateSubscriptionRequestDTO(subscriptionRequestDTO);

		try {
			final Optional<Subscription> subcriptionOptional = subscriptionRepository.findByEventTypeAndSubscriberSystem(subscription.getEventType(), subscription.getSubscriberSystem());
			if (subcriptionOptional.isPresent()) {
				return subcriptionOptional.get();
			} else {
				throw new InvalidParameterException("Subscription with name  '" + subscription.getSubscriberSystem().getSystemName() + "' and eventType '" +
													subscription.getEventType().getEventTypeName() + "' not exists");
			}
		} catch (final InvalidParameterException ex) {
			throw ex;
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Transactional(rollbackFor = ArrowheadException.class)
	public void deleteSubscriptionResponse(final long id) {
		logger.debug("deleteSubscriptionResponse started ...");
		
		if (id < 1) {
			throw new InvalidParameterException("SubscriberSystemId" + LESS_THAN_ONE_ERROR_MESSAGE);
		}
		
		try {
			final Optional<Subscription> subcriptionOptional = subscriptionRepository.findById(id);
			if (subcriptionOptional.isPresent()) {
				final Subscription subscriptionEntry = subcriptionOptional.get();
				final Set<SubscriptionPublisherConnection> involvedPublisherSystems = subscriptionPublisherConnectionRepository.findBySubscriptionEntry(subscriptionEntry);
				
				subscriptionPublisherConnectionRepository.deleteInBatch(involvedPublisherSystems);
				subscriptionRepository.refresh(subscriptionEntry);			
				subscriptionRepository.delete(subscriptionEntry);
				subscriptionRepository.flush();
			}
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Transactional(rollbackFor = ArrowheadException.class)
	public void deleteSubscription(final String eventType, final SystemRequestDTO subscriberSystem) {
		logger.debug("deleteSubscriptionResponse started ...");

		EventType validEventType = null;
		try {
			validEventType = validateEventTypeIsInDB(eventType);
		} catch (final Exception ex) {
			logger.debug(ex.getMessage());
		}

		if (validEventType == null) {
			return;
		}
		
		final System validSubscriber = validateSystemRequestDTO(subscriberSystem); 
		
		try {
			final Optional<Subscription> subcriptionOptional = subscriptionRepository.findByEventTypeAndSubscriberSystem(validEventType, validSubscriber);
			if (subcriptionOptional.isPresent()) {
				final Subscription subscriptionEntry = subcriptionOptional.get();
				final Set<SubscriptionPublisherConnection> involvedPublisherSystems = subscriptionPublisherConnectionRepository.findBySubscriptionEntry(subscriptionEntry);
				
				subscriptionPublisherConnectionRepository.deleteInBatch(involvedPublisherSystems);
				subscriptionPublisherConnectionRepository.flush();
				subscriptionRepository.refresh(subscriptionEntry);			
				subscriptionRepository.delete(subscriptionEntry);
				subscriptionRepository.flush();
			}
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Transactional(rollbackFor = ArrowheadException.class)
	public void registerSubscription(final SubscriptionRequestDTO request, final Set<SystemResponseDTO> authorizedPublishers) {
		logger.debug("registerSubscription started ...");
		
		final Subscription subscription = validateSubscriptionRequestDTO(request);
		checkSubscriptionUniqueConstraints(subscription);
		
		try {
			final Subscription subscriptionEntry = subscriptionRepository.save(subscription);
			addAndSaveSubscriptionEntryPublisherConnections(subscriptionEntry, request, authorizedPublishers);
			subscriptionRepository.saveAndFlush(subscriptionEntry);
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Transactional(rollbackFor = ArrowheadException.class)
	public void forceRegisterSubscription(final SubscriptionRequestDTO request, final Set<SystemResponseDTO> authorizedPublishers) {
		logger.debug("forceRegisterSubscription started ...");
		
		if (request == null) {
			throw new InvalidParameterException("SubscriptionRequestDTO" + NULL_ERROR_MESSAGE);
		}
		
		deleteSubscription(request.getEventType(), request.getSubscriberSystem());
		registerSubscription(request, authorizedPublishers);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Transactional(rollbackFor = ArrowheadException.class)
	public Subscription updateSubscription(final long id, final SubscriptionRequestDTO request, final Set<SystemResponseDTO> authorizedPublishers) {
		logger.debug("updateSubscription started ...");
		
		try {
			final Subscription subscriptionToUpdate = getSubscriptionById(id);
			final long originalEventTypeId = subscriptionToUpdate.getEventType().getId();
			final long originalSubscriberSystemId = subscriptionToUpdate.getSubscriberSystem().getId();
			
			final Set<SubscriptionPublisherConnection> involvedPublisherSystems = subscriptionPublisherConnectionRepository.findBySubscriptionEntry(subscriptionToUpdate);
			final Subscription subscriptionForUpdate = validateSubscriptionRequestDTO(request);
			
			if (originalEventTypeId != subscriptionForUpdate.getEventType().getId() || originalSubscriberSystemId != subscriptionForUpdate.getSubscriberSystem().getId()) {
				checkSubscriptionUniqueConstraintsByEventTypeAndSubscriber(subscriptionForUpdate.getEventType(), subscriptionForUpdate.getSubscriberSystem());
			}		
			
			subscriptionPublisherConnectionRepository.deleteInBatch(involvedPublisherSystems);
			subscriptionRepository.refresh(subscriptionToUpdate);		
			
			subscriptionToUpdate.setEventType(subscriptionForUpdate.getEventType());
			subscriptionToUpdate.setSubscriberSystem(subscriptionForUpdate.getSubscriberSystem());
			subscriptionToUpdate.setFilterMetaData(subscriptionForUpdate.getFilterMetaData());
			subscriptionToUpdate.setMatchMetaData(subscriptionForUpdate.isMatchMetaData());
			subscriptionToUpdate.setNotifyUri(subscriptionForUpdate.getNotifyUri());
			subscriptionToUpdate.setOnlyPredefinedPublishers(subscriptionForUpdate.isOnlyPredefinedPublishers());
			subscriptionToUpdate.setStartDate(subscriptionForUpdate.getStartDate());
			subscriptionToUpdate.setEndDate(subscriptionForUpdate.getEndDate());
						
			addAndSaveSubscriptionEntryPublisherConnections(subscriptionToUpdate, request, authorizedPublishers);
		
			return subscriptionRepository.saveAndFlush(subscriptionToUpdate);
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}

	//-------------------------------------------------------------------------------------------------
	public Set<Subscription> getInvolvedSubscriptions(final EventPublishRequestDTO request) {
		logger.debug("getInvolvedSubscriptions started ...");
		
		final EventType validEventType = validateEventType(request.getEventType());
		
		try {
			final Set<Subscription> involvedSubscriptions = subscriptionRepository.findAllByEventType(validEventType);
			final System validProviderSystem = validateSystemRequestDTO(request.getSource());
			
			return filterInvolvedSubscriptionsByAuthorizedProviders(involvedSubscriptions, validProviderSystem);
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	public List<Subscription> getInvolvedSubscriptionsBySubscriberSystemId(final Long subscriberSystemId) {
		logger.debug("getInvolvedSubscriptionsBySubscriberSystemId started ...");
		
		if (subscriberSystemId == null) {
			throw new InvalidParameterException("SubscriberSystemId" + NULL_ERROR_MESSAGE);
		}
		
		if (subscriberSystemId < 1) {
			throw new InvalidParameterException("SubscriberSystemId" + LESS_THAN_ONE_ERROR_MESSAGE);
		}
		
		try {
			final Optional<System> subscriberSystemOptional = systemRepository.findById(subscriberSystemId);
			if (subscriberSystemOptional.isEmpty()) {
				throw new InvalidParameterException("SubscriberSystem" + NOT_IN_DB_ERROR_MESSAGE);
			}
			
			return subscriptionRepository.findAllBySubscriberSystem(subscriberSystemOptional.get());
		} catch (final InvalidParameterException ex) {
			throw ex;
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Transactional(rollbackFor = ArrowheadException.class)	
	public void updateSubscriberAuthorization(final List<Subscription> involvedSubscriptions, final Set<SystemResponseDTO> authorizedPublishers) {
		logger.debug("updateSubscriberAuthorization started ...");
		
		for (final Subscription subscriptionEntry : involvedSubscriptions) {
			final Optional<Subscription> subcriptionOptional = subscriptionRepository.findById(subscriptionEntry.getId());
			if (subcriptionOptional.isPresent()) {
				final Subscription subscription = subcriptionOptional.get();
				
				updateSubscriptionEntryPublisherConnections(subscription, authorizedPublishers);
			} else {
				logger.debug("SubscriberSystem" + NOT_IN_DB_ERROR_MESSAGE);
			}
			
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Transactional(rollbackFor = ArrowheadException.class)	
	public void removeSubscriptionEntries(final List<Subscription> toBeRemoved) {
		logger.debug( "removeSubscriptionEntries started..." );
		
		if (toBeRemoved == null || toBeRemoved.isEmpty()) {
			return;
		}
		
		try {
			subscriptionRepository.deleteInBatch(toBeRemoved);
			subscriptionRepository.flush();
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}
	
	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	private Subscription validateSubscriptionRequestDTO(final SubscriptionRequestDTO request) {
		logger.debug("validatesubscriptionRequestDTO started ...");

		if (request == null) {
			throw new InvalidParameterException("SubscriptionRequestDTO" + NULL_ERROR_MESSAGE);
		}

		final System validSubscriberSystem = validateSystemRequestDTO(request.getSubscriberSystem());
		final EventType validEventType = validateEventType(request.getEventType());
		final String validNotifyUri = validateNotifyUri(request.getNotifyUri());
		
		final Subscription subscription = new Subscription();
		subscription.setSubscriberSystem(validSubscriberSystem);
		subscription.setEventType(validEventType);
		subscription.setNotifyUri(validNotifyUri);
		subscription.setFilterMetaData(Utilities.map2Text(request.getFilterMetaData()));
		subscription.setOnlyPredefinedPublishers(request.getSources() != null && !request.getSources().isEmpty());
		subscription.setMatchMetaData(request.getMatchMetaData());
		if (subscription.isMatchMetaData() && (subscription.getFilterMetaData() == null || subscription.getFilterMetaData().isEmpty())) {
			throw new InvalidParameterException("If MatchMetaData is true filterMetaData should not be null or empty");
		}
		
		if (request.getStartDate() != null) {
			try {
				subscription.setStartDate(Utilities.parseUTCStringToLocalZonedDateTime(request.getStartDate()));
			} catch (final Exception ex) {
				throw new InvalidParameterException("StartDate" + INVALID_TYPE_ERROR_MESSAGE);
			}
		} else {
			request.setStartDate(null);
		}
		
		if (request.getEndDate() != null) {
			try {
				subscription.setEndDate(Utilities.parseUTCStringToLocalZonedDateTime(request.getEndDate()));
			} catch (final Exception ex) {
				throw new InvalidParameterException("EndDate" + INVALID_TYPE_ERROR_MESSAGE);
			}
		} else {
			request.setEndDate(null);
		}
		
		validateDateLimits(subscription);
		
		return subscription;
	}
	
	//-------------------------------------------------------------------------------------------------
	private void validateDateLimits(final Subscription subscription) {
		logger.debug("validateDateLimits started...");
		
		final ZonedDateTime now  = ZonedDateTime.now();
		
		final ZonedDateTime start = subscription.getStartDate();
		final ZonedDateTime end = subscription.getEndDate();
		
		if (start != null) {
			if (!start.isAfter(now.minusSeconds(timeStampTolerance))) {
				throw new InvalidParameterException("Start Date" + IS_BEFORE_TOLERATED_DIFF_ERROR_MESSAGE);
			}
		}
		
		if (end != null) {
			if (!end.isAfter(now.minusSeconds(timeStampTolerance))) {
				throw new InvalidParameterException("End Date" + IS_BEFORE_TOLERATED_DIFF_ERROR_MESSAGE);
			}
		}
		
		if (start != null && end != null) {
			if (end.isBefore(start) || !end.isAfter(start)) {
				throw new InvalidParameterException("Start Date should be before End Date");
			}			
		}
	}

	//-------------------------------------------------------------------------------------------------
	private System validateSystemRequestDTO(final SystemRequestDTO systemRequestDTO) {
		logger.debug("validateSystemRequestDTO started...");

		if (systemRequestDTO == null) {
			throw new InvalidParameterException("SystemRequestDTO" + NULL_ERROR_MESSAGE);
		}
		
		if (Utilities.isEmpty(systemRequestDTO.getSystemName())) {
			throw new InvalidParameterException("System name" + EMPTY_OR_NULL_ERROR_MESSAGE);
		}
		
		if (Utilities.isEmpty(systemRequestDTO.getAddress())) {
			throw new InvalidParameterException("System address" + EMPTY_OR_NULL_ERROR_MESSAGE);
		}		
		
		if (systemRequestDTO.getPort() == null) {
			throw new InvalidParameterException("System port" + NULL_ERROR_MESSAGE);
		}
		
		final String address = systemRequestDTO.getAddress().trim().toLowerCase();
		final String systemName = systemRequestDTO.getSystemName().trim().toLowerCase();
		final int port = systemRequestDTO.getPort();
		
		final Optional<System> systemOptional;
		try {
			systemOptional = systemRepository.findBySystemNameAndAddressAndPort(systemName, address, port);
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
		
		if (systemOptional.isEmpty()) {
			throw new InvalidParameterException("System by systemName: " + systemName + ", address: " + address + ", port: " + port + NOT_IN_DB_ERROR_MESSAGE);
		}
		
		return systemOptional.get();
	}	

	//-------------------------------------------------------------------------------------------------
	private EventType validateEventType(final String eventType) {
		logger.debug("validateEventType started...");
		
		if (Utilities.isEmpty(eventType)) {
			throw new InvalidParameterException("EventType" + EMPTY_OR_NULL_ERROR_MESSAGE);			
		}
		
		try {
			final String validEventTypeName = eventType.toUpperCase().trim();
			final Optional<EventType> eventTypeOptional = eventTypeRepository.findByEventTypeName(validEventTypeName);
			if (eventTypeOptional.isEmpty()) {
				return eventTypeRepository.saveAndFlush(new EventType(validEventTypeName));
			}
			
			return eventTypeOptional.get();		
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private EventType validateEventTypeIsInDB(final String eventType) {
		logger.debug("validateEventTypeIsInDB started...");
		
		if (Utilities.isEmpty(eventType)) {
			throw new InvalidParameterException("EventType" + EMPTY_OR_NULL_ERROR_MESSAGE);			
		}
		
		try {
			final String validEventTypeName = eventType.toUpperCase().trim();
			final Optional<EventType> eventTypeOptional = eventTypeRepository.findByEventTypeName(validEventTypeName);
			if (eventTypeOptional.isEmpty()) {
				throw new InvalidParameterException("EventType" + NOT_IN_DB_ERROR_MESSAGE);
			}
			
			return eventTypeOptional.get();		
		} catch (final InvalidParameterException ex) {
			throw ex;
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}

	//-------------------------------------------------------------------------------------------------
	private Set<System> getAllowedPublisherSystems(final Set<SystemRequestDTO> sources) {
		logger.debug("getAllowedPublisherSystems started...");
		
		final Set<System> allowedSystems = new HashSet<>();
		for (final SystemRequestDTO source : sources) {
			allowedSystems.add(validateSystemRequestDTO(source));
		}
		
		return allowedSystems;
	}
	
	//-------------------------------------------------------------------------------------------------
	private void addAndSaveSubscriptionEntryPublisherConnections(final Subscription subscriptionEntry, final SubscriptionRequestDTO request, final Set<SystemResponseDTO> authorizedPublishers) {
		logger.debug("addAndSaveSubscriptionEntryPublisherConnections started...");
		
		if (subscriptionEntry.isOnlyPredefinedPublishers()) {
			final Set<System> allowedPublisherSystems = getAllowedPublisherSystems(request.getSources());
			for (final System system : allowedPublisherSystems) {
				final SystemResponseDTO allowedPublisher = DTOConverter.convertSystemToSystemResponseDTO(system);
				final SubscriptionPublisherConnection conn = new SubscriptionPublisherConnection(subscriptionEntry, system);
				conn.setAuthorized(false);
				
				for (final SystemResponseDTO authorizedPublisher : authorizedPublishers) {
					if (authorizedPublisher.equals(allowedPublisher)) {
						conn.setAuthorized(true);
						break;
					}
				}
				
				subscriptionEntry.getPublisherConnections().add(conn);
			}
		} else {
			for (final SystemResponseDTO systemResponseDTO : authorizedPublishers) {
				final AddressType addressType = networkAddressTypeDetector.detectAddressType(systemResponseDTO.getAddress());
				final System system = new System(systemResponseDTO.getSystemName(),	systemResponseDTO.getAddress(), addressType, systemResponseDTO.getPort(), systemResponseDTO.getAuthenticationInfo(), Utilities.map2Text(systemResponseDTO.getMetadata()));
				system.setId(systemResponseDTO.getId());
						
				final SubscriptionPublisherConnection conn = new SubscriptionPublisherConnection(subscriptionEntry, system);
				conn.setAuthorized(true);
				
				subscriptionEntry.getPublisherConnections().add(conn);
			}
		}

		try {
			subscriptionPublisherConnectionRepository.saveAll(subscriptionEntry.getPublisherConnections());
			subscriptionPublisherConnectionRepository.flush();
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private void updateSubscriptionEntryPublisherConnections(final Subscription subscriptionEntry, final Set<SystemResponseDTO> authorizedPublishers) {
		logger.debug("updateSubscriptionEntryPublisherConnections started...");
		
		if (subscriptionEntry.isOnlyPredefinedPublishers()) {
			final Set<SubscriptionPublisherConnection> involvedPublisherSystems = subscriptionPublisherConnectionRepository.findBySubscriptionEntry(subscriptionEntry);
			for (final SubscriptionPublisherConnection conn  : involvedPublisherSystems) {
				final SystemResponseDTO allowedPublisher = DTOConverter.convertSystemToSystemResponseDTO(conn.getSystem());
				
				for (final SystemResponseDTO authorizedPublisher : authorizedPublishers) {
					if (authorizedPublisher.equals(allowedPublisher)) {
						conn.setAuthorized(true);
					} else {
						conn.setAuthorized(false);
					}
				}
			}
			
			try {
				subscriptionPublisherConnectionRepository.saveAll(involvedPublisherSystems);
				subscriptionPublisherConnectionRepository.flush();
			} catch (final Exception ex) {
				logger.debug(ex.getMessage(), ex);
				throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
			}
		} else {
			try {
				final Set<SubscriptionPublisherConnection> involvedPublisherSystems = subscriptionPublisherConnectionRepository.findBySubscriptionEntry(subscriptionEntry);
				subscriptionPublisherConnectionRepository.deleteInBatch(involvedPublisherSystems);
				subscriptionRepository.refresh(subscriptionEntry);			
			} catch (final Exception ex) {
				logger.debug(ex.getMessage(), ex);
				throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
			}
			
			for (final SystemResponseDTO systemResponseDTO : authorizedPublishers) {
				final AddressType addressType = networkAddressTypeDetector.detectAddressType(systemResponseDTO.getAddress());
				final System system = new System(systemResponseDTO.getSystemName(),	systemResponseDTO.getAddress(), addressType, systemResponseDTO.getPort(), systemResponseDTO.getAuthenticationInfo(), Utilities.map2Text(systemResponseDTO.getMetadata()));
				system.setId(systemResponseDTO.getId());
						
				final SubscriptionPublisherConnection conn = new SubscriptionPublisherConnection(subscriptionEntry, system);
				conn.setAuthorized(true);
				
				subscriptionEntry.getPublisherConnections().add(conn);
			}
			
			try {
				subscriptionPublisherConnectionRepository.saveAll(subscriptionEntry.getPublisherConnections());
				subscriptionPublisherConnectionRepository.flush();
			} catch (final Exception ex) {
				logger.debug(ex.getMessage(), ex);
				throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
			}
		}
	}

	//-------------------------------------------------------------------------------------------------
	private Set<Subscription> filterInvolvedSubscriptionsByAuthorizedProviders(final Set<Subscription> involvedSubscriptions, final System validProviderSystem) {
		logger.debug("filterInvolvedSubscriptionsByAuthorizedProviders started...");
		
		final List<SubscriptionPublisherConnection> involvedConnections = subscriptionPublisherConnectionRepository.findAllBySystemAndAuthorized(validProviderSystem, true);
		final Set<Subscription> involvedSubscriptionsFromConnections = new HashSet<>();
		
		for (final SubscriptionPublisherConnection spConnection : involvedConnections) {
			final Subscription subscription = spConnection.getSubscriptionEntry();
			if (involvedSubscriptions.contains(subscription)  && !involvedSubscriptionsFromConnections.contains(subscription)) {
				involvedSubscriptionsFromConnections.add(subscription);
			}
		}
		
		return involvedSubscriptionsFromConnections;
	}
	
	//-------------------------------------------------------------------------------------------------
	private void checkSubscriptionUniqueConstraints(final Subscription subscription) {
		logger.debug("checkSubscriptionUniqueConstrains started...");
		
		checkSubscriptionUniqueConstraintsByEventTypeAndSubscriber(subscription.getEventType(), subscription.getSubscriberSystem());
	}
	
	//-------------------------------------------------------------------------------------------------
	private void checkSubscriptionUniqueConstraintsByEventTypeAndSubscriber(final EventType eventTypeForUpdate, final System subscriberSystemForUpdate) {
		logger.debug("checkSubscriptionUniqueConstrainsByEventTypeAndSubscriber started...");
		
		final Optional<Subscription> subcriptionOptional;
		try {
			subcriptionOptional = subscriptionRepository.findByEventTypeAndSubscriberSystem(eventTypeForUpdate, subscriberSystemForUpdate);
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
		
		if (subcriptionOptional.isPresent()) {
			throw new InvalidParameterException("Subscription" + VIOLATES_UNIQUE_CONSTRAINT);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private String validateNotifyUri(final String notifyUri) {
		logger.debug("validateNotifyUri started...");
		
		if (Utilities.isEmpty(notifyUri)) {
			throw new InvalidParameterException("NotifyUri" + EMPTY_OR_NULL_ERROR_MESSAGE);
		}
		
		return notifyUri;
	}
}
