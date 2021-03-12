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

package eu.arrowhead.core.eventhandler.service;

import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.util.UriComponents;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.core.CoreSystemService;
import eu.arrowhead.common.database.entity.Subscription;
import eu.arrowhead.common.dto.internal.AuthorizationSubscriptionCheckRequestDTO;
import eu.arrowhead.common.dto.internal.AuthorizationSubscriptionCheckResponseDTO;
import eu.arrowhead.common.dto.internal.EventPublishStartDTO;
import eu.arrowhead.common.dto.shared.EventPublishRequestDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.common.dto.shared.SystemResponseDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.common.http.HttpService;
import eu.arrowhead.core.eventhandler.publish.PublishingQueue;

@Component
public class EventHandlerDriver {	

	//=================================================================================================
	// members
	
	private static final String NULL_PARAMETER_ERROR_MESSAGE = " is null.";
	private static final String NULL_OR_BLANK_PARAMETER_ERROR_MESSAGE = " is null or blank.";
	private static final String EMPTY_PARAMETER_ERROR_MESSAGE = " is empty.";
	private static final String INVALID_TYPE_ERROR_MESSAGE = " is not valid.";
	private static final String IS_AFTER_TOLERATED_DIFF_ERROR_MESSAGE = " is further in the future than the tolerated time difference";
	private static final String IS_BEFORE_TOLERATED_DIFF_ERROR_MESSAGE = " is further in the past than the tolerated time difference";
	private static final String LESS_THAN_ONE_ERROR_MESSAGE = " is less than one.";
	
	private static final String AUTH_SUBSCRIPTION_CHECK_URI_KEY = CoreSystemService.AUTH_CONTROL_SUBSCRIPTION_SERVICE.getServiceDefinition() + CoreCommonConstants.URI_SUFFIX;
	
	private static final Logger logger = LogManager.getLogger(EventHandlerDriver.class);
	
	@Autowired
	private HttpService httpService;

	@Resource(name = CommonConstants.ARROWHEAD_CONTEXT)
	private Map<String,Object> arrowheadContext;
	
	@Resource(name = CoreCommonConstants.EVENT_PUBLISHING_QUEUE)
	private PublishingQueue publishingQueue;
	
	@Value(CoreCommonConstants.$TIME_STAMP_TOLERANCE_SECONDS_WD)
	private long timeStampTolerance;
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------	
	public Set<SystemResponseDTO> getAuthorizedPublishers(final SystemRequestDTO subscriberSystem) {
		logger.debug("getAuthorizedPublishers started...");
		
		Assert.notNull(subscriberSystem, "subscriberSystem is null.");
		
		final UriComponents checkUri = getAuthSubscriptionCheckUri();
		final AuthorizationSubscriptionCheckRequestDTO payload = new AuthorizationSubscriptionCheckRequestDTO(subscriberSystem, null);
		final ResponseEntity<AuthorizationSubscriptionCheckResponseDTO> response = httpService.sendRequest(checkUri, HttpMethod.POST, AuthorizationSubscriptionCheckResponseDTO.class, payload);		
		
		return response.getBody().getPublishers();
	}
	
	//-------------------------------------------------------------------------------------------------
	public void publishEvent(final EventPublishRequestDTO request, final Set<Subscription> involvedSubscriptions) {
		logger.debug("publishEvent started...");
		
		checkPublishRequestDTO(request);
		checkInvolvedSubscriptions(involvedSubscriptions);
		
		final EventPublishStartDTO eventPublishStartDTO = new EventPublishStartDTO(request, involvedSubscriptions);
		try {
			publishingQueue.put(eventPublishStartDTO);
		} catch (final Exception ex) {
			logger.debug("publishEvent finished with exception : " + ex);
		}
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private UriComponents getAuthSubscriptionCheckUri() {
		logger.debug("getAuthSubscriptionCheckUri started...");
		
		if (arrowheadContext.containsKey(AUTH_SUBSCRIPTION_CHECK_URI_KEY)) {
			try {
				return (UriComponents) arrowheadContext.get(AUTH_SUBSCRIPTION_CHECK_URI_KEY);
			} catch (final ClassCastException ex) {
				throw new ArrowheadException("EventHandler can't find subscription authorization check URI.");
			}
		}
		
		throw new ArrowheadException("EventHandler can't find subscription authorization check URI.");
	}

	//-------------------------------------------------------------------------------------------------
	private void checkPublishRequestDTO(final EventPublishRequestDTO request) {
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
		
		checkTimeStamp(request.getTimeStamp());	
		checkSystemRequestDTO(request.getSource());
	}
	
	//-------------------------------------------------------------------------------------------------
	private void checkTimeStamp(final String timeStampString) {
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
		
		if (timeStamp.isAfter(now.plusSeconds(timeStampTolerance))) {
			throw new InvalidParameterException("TimeStamp" + IS_AFTER_TOLERATED_DIFF_ERROR_MESSAGE);
		}
		
		if (timeStamp.isBefore(now.minusSeconds(timeStampTolerance))) {
			throw new InvalidParameterException("TimeStamp" + IS_BEFORE_TOLERATED_DIFF_ERROR_MESSAGE);
		}
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
	private void checkInvolvedSubscriptions(final Set<Subscription> involvedSubscriptions) {
		if (involvedSubscriptions == null) {
			throw new InvalidParameterException("involvedSubscriptions" + NULL_PARAMETER_ERROR_MESSAGE);
		}
		
		if (involvedSubscriptions.isEmpty()) {
			throw new InvalidParameterException("involvedSubscriptions" + EMPTY_PARAMETER_ERROR_MESSAGE);
		}
	}
}