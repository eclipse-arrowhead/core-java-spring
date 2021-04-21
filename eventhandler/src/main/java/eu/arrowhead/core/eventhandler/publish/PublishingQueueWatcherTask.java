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

package eu.arrowhead.core.eventhandler.publish;

import java.security.InvalidParameterException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.entity.Subscription;
import eu.arrowhead.common.dto.internal.EventPublishStartDTO;
import eu.arrowhead.common.dto.shared.EventPublishRequestDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.common.http.HttpService;

public class PublishingQueueWatcherTask extends Thread {

	//=================================================================================================
	// members
	
	private static final String NULL_PARAMETER_ERROR_MESSAGE = " is null.";
	private static final String NULL_OR_BLANK_PARAMETER_ERROR_MESSAGE = " is null or blank.";
	private static final String INVALID_TYPE_ERROR_MESSAGE = " is not valid.";
	private static final String IS_AFTER_TOLERATED_DIFF_ERROR_MESSAGE = " is further in the future than the tolerated time difference";
	private static final String IS_BEFORE_TOLERATED_DIFF_ERROR_MESSAGE = " is further in the past than the tolerated time difference";
	private static final String LESS_THAN_ONE_ERROR_MESSAGE = " is less than one.";
	
	private boolean interrupted = false;
	
	private final Logger logger = LogManager.getLogger(PublishingQueueWatcherTask.class);
	
	@Resource(name = CoreCommonConstants.EVENT_PUBLISHING_QUEUE)
	private PublishingQueue publishingQueue;
	
	@Value(CoreCommonConstants.$TIME_STAMP_TOLERANCE_SECONDS_WD)
	private long timeStampTolerance;
	
	@Value(CoreCommonConstants.$EVENTHANDLER_MAX_EXPRESS_SUBSCRIBERS_WD)
	private int maxExpressSubscribers;
	
	@Resource(name = CoreCommonConstants.EVENT_PUBLISHING_EXPRESS_EXECUTOR)
	private PublishRequestFixedExecutor expressExecutor;
	
	@Autowired
	private HttpService httpService;
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------	
	@Override
	public void run() {
		logger.debug("PublishingQueueWatcherTask.run started...");
		
		interrupted = Thread.currentThread().isInterrupted();
		
		while (!interrupted) {
			try {
				publishEventFromQueue();
			} catch (final InterruptedException ex) {
				interrupted = true;
			} catch (final Throwable ex) {
				logger.debug(ex.getMessage());
			}
		}
		
		expressExecutor.shutdownExecutionNow();
	}

	//-------------------------------------------------------------------------------------------------	
	public void destroy() {
		logger.debug("PublishingQueueWatcherTask.destroy started...");
		
		interrupted = true;
	}
	
	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------	
	private void publishEventFromQueue() throws InterruptedException {
		logger.debug("PublishingQueueWatcherTask.publishEventFromQueue started...");
		
		final EventPublishStartDTO eventPublishStartDTO = publishingQueue.take();
		validateEventPublishStartDTO(eventPublishStartDTO);
		
		final EventPublishRequestDTO request = eventPublishStartDTO.getRequest();
		final Set<Subscription> involvedSubscriptions = eventPublishStartDTO.getInvolvedSubscriptions();
		
		if (involvedSubscriptions.size() < maxExpressSubscribers) {
			expressExecutor.execute(request, involvedSubscriptions);
		} else { 
			final PublishRequestExecutor publishRequestExecutor = new PublishRequestExecutor(request, involvedSubscriptions, httpService);			
			publishRequestExecutor.execute();
		}
	}

	//-------------------------------------------------------------------------------------------------	
	private void validateEventPublishStartDTO(final EventPublishStartDTO eventPublishStartDTO) {
		logger.debug("PublishingQueueWatcherTask.validateEventPublishStartDTO started...");
		
		if (eventPublishStartDTO == null) {
			throw new InvalidParameterException("EventPublishStartDTO" + NULL_PARAMETER_ERROR_MESSAGE);
		}
		
		checkPublishRequestDTO(eventPublishStartDTO.getRequest());
		checkInvolvedSubscriptions(eventPublishStartDTO.getInvolvedSubscriptions());
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
		
		ckeckTimeStamp(request.getTimeStamp());	
		checkSystemRequestDTO(request.getSource());
	}
	
	//-------------------------------------------------------------------------------------------------
	private void ckeckTimeStamp(final String timeStampString) {
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
			throw new InvalidParameterException("involvedSubscriptions" + NULL_OR_BLANK_PARAMETER_ERROR_MESSAGE);
		}
	}
}