package eu.arrowhead.core.datamanager.service;

import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.entity.Subscription;
import eu.arrowhead.common.dto.internal.DTOConverter;
import eu.arrowhead.common.dto.shared.EventPublishRequestDTO;
import eu.arrowhead.common.dto.shared.SubscriptionRequestDTO;
import eu.arrowhead.common.dto.shared.SubscriptionResponseDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.common.dto.shared.SystemResponseDTO;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.core.datamanager.database.service.DataManagerDBService;

@Service
public class DataManagerService {

	//=================================================================================================
	// members
	
	private static final String NULL_PARAMETER_ERROR_MESSAGE = " is null.";
	private static final String NULL_OR_BLANK_PARAMETER_ERROR_MESSAGE = " is null or blank.";
	private static final String INVALID_TYPE_ERROR_MESSAGE = " is not valid.";
	private static final String IS_AFTER_TOLERATED_DIFF_ERROR_MESSAGE = " is further in the future than the tolerated time difference";
	private static final String IS_BEFORE_TOLERATED_DIFF_ERROR_MESSAGE = " is further in the past than the tolerated time difference";
	private static final String LESS_THAN_ONE_ERROR_MESSAGE = " is less than one.";
	
	private static final Logger logger = LogManager.getLogger(DataManagerService.class);
	
	@Value( CoreCommonConstants.$TIME_STAMP_TOLERANCE_SECONDS_WD )
	private long timeStampTolerance;
	
	@Autowired
	private DataManagerDriver dataManagerDriver;
	
	@Autowired
	private DataManagerDBService dataManagerDBService;
	
	//=================================================================================================
	// methods
/*
	//-------------------------------------------------------------------------------------------------
	public void subscribe( final SubscriptionRequestDTO request) {
		logger.debug("subscribe started ...");
		
		checkSubscriptionRequestDTO( request );
		
		final SystemRequestDTO subscriber = request.getSubscriberSystem();
		
		final Set<SystemResponseDTO> authorizedPublishers = dataManagerDriver.getAuthorizedPublishers(subscriber);
		
		dataManagerDBService.registerSubscription(request, authorizedPublishers);
	}

	//-------------------------------------------------------------------------------------------------
	public void unsubscribe( final String eventType, final String subscriberName, final String subscriberAddress, final int subscriberPort) {
		logger.debug("unsubscribe started ...");
			
		checkUnsubscribeParameters(eventType, subscriberName, subscriberAddress, subscriberPort);
		
		final SystemRequestDTO subscriberSystem = new SystemRequestDTO();
		subscriberSystem.setSystemName( subscriberName );
		subscriberSystem.setAddress( subscriberAddress );
		subscriberSystem.setPort(  subscriberPort );
		
		dataManagerDBService.deleteSubscription( eventType, subscriberSystem );
		
	}

	//-------------------------------------------------------------------------------------------------
	public void publishResponse(final EventPublishRequestDTO request) {
		logger.debug("publishResponse started ...");
		
		checkPublishRequestDTO( request );
		
		final Set<Subscription> involvedSubscriptions = dataManagerDBService.getInvolvedSubscriptions(request);
		if ( involvedSubscriptions.isEmpty() ) {
			
			return;
		}
		
		filterInvolvedSubscriptionsBySubscriptionParameters(involvedSubscriptions, request);
		if ( involvedSubscriptions.isEmpty() ) {
			
			return;
		}
		
		dataManagerDriver.publishEvent(request, involvedSubscriptions);

	}

	//-------------------------------------------------------------------------------------------------
	public void publishSubscriberAuthorizationUpdateResponse(final EventPublishRequestDTO request) {
		logger.debug("publishSubscriberAuthorizationUpdateResponse started ...");
		
		validateAuthorizationUpdateEventType( request.getEventType() );
		final Long subscriberSystemId = validateAuthorizationUpdatePayload( request.getPayload() );
		
		final List<Subscription> involvedSubscriptions = dataManagerDBService.getInvolvedSubscriptionsBySubscriberSystemId( subscriberSystemId );
		if ( involvedSubscriptions.isEmpty() ) {
			
			return;
		}		

		final SystemRequestDTO subscriber = DTOConverter.convertSystemToSystemRequestDTO( involvedSubscriptions.get(0).getSubscriberSystem() );		
		final Set<SystemResponseDTO> authorizedPublishers = dataManagerDriver.getAuthorizedPublishers(subscriber);
		
		dataManagerDBService.updateSubscriberAuthorization( involvedSubscriptions, authorizedPublishers );
		
	}
	
	//-------------------------------------------------------------------------------------------------
	public SubscriptionResponseDTO updateSubscriptionResponse(final long id, final SubscriptionRequestDTO subscriptionRequestDTO) {
		logger.debug("updateSubscriptionResponse started ...");
		
		return DTOConverter.convertSubscriptionToSubscriptionResponseDTO( updateSubscription( id, subscriptionRequestDTO ) );

	}	
	
	//-------------------------------------------------------------------------------------------------
	public Subscription updateSubscription(final long id, final SubscriptionRequestDTO request) {
		logger.debug("updateSubscription started ...");
		
		checkSubscriptionRequestDTO( request );
		
		final SystemRequestDTO subscriber = request.getSubscriberSystem();	
		final Set<SystemResponseDTO> authorizedPublishers = dataManagerDriver.getAuthorizedPublishers(subscriber);

		return dataManagerDBService.updateSubscription(id, request, authorizedPublishers);
	}
	

	//=================================================================================================
	// assistant methods

	
	//-------------------------------------------------------------------------------------------------
	private void checkPublishRequestDTO( final EventPublishRequestDTO request ) {
		logger.debug("checkPublishRequestDTO started...");
		
		if (request == null) {
			throw new InvalidParameterException("EventPublishRequestDTO" + NULL_PARAMETER_ERROR_MESSAGE);
		}
		
		if (Utilities.isEmpty(request.getEventType())) {
			throw new InvalidParameterException("EventType" + NULL_OR_BLANK_PARAMETER_ERROR_MESSAGE);
		}
		
		if (Utilities.isEmpty(request.getPayload())) {
			throw new InvalidParameterException("Payload" + NULL_OR_BLANK_PARAMETER_ERROR_MESSAGE);
		}
		
		ckeckTimeStamp( request.getTimeStamp() );	
		checkSystemRequestDTO( request.getSource() );
		
	}
	
	//-------------------------------------------------------------------------------------------------
	private void ckeckTimeStamp( final String timeStampString ) {
		logger.debug("ckeckTimeStamp started...");
		
		if (Utilities.isEmpty(timeStampString)) {
			throw new InvalidParameterException("TimeStamp" + NULL_OR_BLANK_PARAMETER_ERROR_MESSAGE);
		}
		
		
		final ZonedDateTime now = ZonedDateTime.now();
		final ZonedDateTime timeStamp;

		try {
			
			timeStamp = Utilities.parseUTCStringToLocalZonedDateTime(timeStampString);
		
		} catch (final DateTimeParseException ex) {
			
			throw new InvalidParameterException("TimeStamp" + INVALID_TYPE_ERROR_MESSAGE);
		}
		
		if (timeStamp.isAfter(now.plusSeconds( timeStampTolerance ))) {
			
			throw new InvalidParameterException("TimeStamp" + IS_AFTER_TOLERATED_DIFF_ERROR_MESSAGE);
		}
		
		if (timeStamp.isBefore(now.minusSeconds( timeStampTolerance ))) {
			
			throw new InvalidParameterException("TimeStamp" + IS_BEFORE_TOLERATED_DIFF_ERROR_MESSAGE);
		}
		
	}

	//-------------------------------------------------------------------------------------------------
	private void checkSubscriptionRequestDTO(final SubscriptionRequestDTO request) {
		logger.debug("checkSubscriptionRequestDTO started...");
		
		if (request == null) {
			throw new InvalidParameterException("SubscriptionRequestDTO" + NULL_PARAMETER_ERROR_MESSAGE);
		}
		
		if (Utilities.isEmpty(request.getEventType())) {
			throw new InvalidParameterException("EventType" + NULL_OR_BLANK_PARAMETER_ERROR_MESSAGE);
		}
		
		if (Utilities.isEmpty(request.getNotifyUri())) {
			throw new InvalidParameterException("NotifyUri" + NULL_OR_BLANK_PARAMETER_ERROR_MESSAGE);
		}
		
		if (request.getFilterMetaData() == null && request.getMatchMetaData()) {
			throw new InvalidParameterException("FilterMetaData should not be null if MatchMetaData is true");
		}
		
		if (request.getFilterMetaData() != null && request.getFilterMetaData().isEmpty() && request.getMatchMetaData()) {
			throw new InvalidParameterException("FilterMetaData should not be empty if MatchMetaData is true");
		}
		
		checkSystemRequestDTO( request.getSubscriberSystem());
		validateDateLimits( request );

	}	
	
	//-------------------------------------------------------------------------------------------------
	private void checkSystemRequestDTO(final SystemRequestDTO system) {
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
		
		if (system.getPort() < 1) {
			throw new InvalidParameterException("System port" + LESS_THAN_ONE_ERROR_MESSAGE );
		}
	}	
	
	//-------------------------------------------------------------------------------------------------
	private void validateDateLimits( SubscriptionRequestDTO request ) {
		logger.debug("validateDateLimits started...");
		
		final ZonedDateTime now  = ZonedDateTime.now();
		
		final ZonedDateTime start = Utilities.parseUTCStringToLocalZonedDateTime( request.getStartDate() );
		final ZonedDateTime end = Utilities.parseUTCStringToLocalZonedDateTime( request.getEndDate() );
		
		if ( start != null ) {
			
			if ( !start.isAfter( now.minusSeconds( timeStampTolerance )) ) {
				
				throw new InvalidParameterException("Start Date" + IS_BEFORE_TOLERATED_DIFF_ERROR_MESSAGE);
			}
		}
		
		if ( end != null ) {
			
			if ( !end.isAfter( now.minusSeconds( timeStampTolerance )) ) {
				
				throw new InvalidParameterException("End Date" + IS_BEFORE_TOLERATED_DIFF_ERROR_MESSAGE);
			}
		}
		
		if ( start != null && end != null ) {
			
			if ( end.isBefore( start ) || !end.isAfter( start ) ) {
				
				throw new InvalidParameterException("Start Date sould be before End Date");

			}			
		}
		
	}
	
	//-------------------------------------------------------------------------------------------------
	private void validateAuthorizationUpdateEventType(final String eventType) {
		logger.debug("validateAuthorizationUpdateEventType started...");
		
		if (Utilities.isEmpty( eventType )) {
			throw new InvalidParameterException("EventType" + NULL_OR_BLANK_PARAMETER_ERROR_MESSAGE);
		}
		
		if (!eventType.equalsIgnoreCase( CoreCommonConstants.EVENT_TYPE_SUBSCRIBER_AUTH_UPDATE)) {
			throw new InvalidParameterException("EventType" + INVALID_TYPE_ERROR_MESSAGE);
		}
		
	}

	//-------------------------------------------------------------------------------------------------
	private Long validateAuthorizationUpdatePayload(final String payload) {
		logger.debug("validateAuthorizationUpdatePayload started...");
		
		try {
			final Long id = Long.parseLong(payload);
			
			return id;
		} catch (final NumberFormatException ex) {
			
			throw new InvalidParameterException("Payload" + INVALID_TYPE_ERROR_MESSAGE);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	// This method may CHANGE the content of - Set<Subscription> involvedSubscriptions
	private void filterInvolvedSubscriptionsBySubscriptionParameters(final Set<Subscription> involvedSubscriptions, final EventPublishRequestDTO request) {
		
		
	}

	//-------------------------------------------------------------------------------------------------
	// This method may CHANGE the content of - Set<Subscription> involvedSubscriptions
	private void filterInvolvedSubscriptionsByMetaData(final Set<Subscription> involvedSubscriptions, final EventPublishRequestDTO request) {
		logger.debug("filterInvolvedSubscriptionsByMetaData started...");
		
	}

	//-------------------------------------------------------------------------------------------------
	// This method may CHANGE the content of - Set<Subscription> involvedSubscriptions
	private void filterInvolvedSubscriptionsByEndDate(final Set<Subscription> involvedSubscriptions, final EventPublishRequestDTO request) {

	}

	//-------------------------------------------------------------------------------------------------

	//-------------------------------------------------------------------------------------------------
	private void checkUnsubscribeParameters( final String eventType, final String subscriberName, final String subscriberAddress, final int subscriberPort) {
		logger.debug("checkUnsubscribeParameters started...");
		
	}
	*/
}
