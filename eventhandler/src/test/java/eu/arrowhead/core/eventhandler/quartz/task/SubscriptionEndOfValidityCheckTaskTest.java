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

package eu.arrowhead.core.eventhandler.quartz.task;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.ZonedDateTime;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import eu.arrowhead.common.database.entity.EventType;
import eu.arrowhead.common.database.entity.Subscription;
import eu.arrowhead.common.database.entity.System;
import eu.arrowhead.core.eventhandler.database.service.EventHandlerDBService;


@RunWith (SpringRunner.class)
public class SubscriptionEndOfValidityCheckTaskTest {
	
	//=================================================================================================
	// members
	
	@InjectMocks
	SubscriptionEndOfValidityCheckTask subscriptionEndOfValidityCheckTask = new SubscriptionEndOfValidityCheckTask();
	
	@Mock
	EventHandlerDBService eventHandlerDBService; 

	private Logger logger;
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
    @Before
    public void setUp() throws Exception {
		logger = mock(Logger.class);		
		ReflectionTestUtils.setField(subscriptionEndOfValidityCheckTask, "logger", logger);
    }
    
	//=================================================================================================
	// Tests of checkSubscriptionEndOfValidity
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCheckSubscriptionEndOfValidityOK() {
		final Subscription subscription = createSubscriptionForDBMock(1, "eventType", "subscriberName");
		final Page<Subscription> subscriptionPage = new PageImpl<>(List.of(subscription));
		
		final ArgumentCaptor<String> debugValueCapture = ArgumentCaptor.forClass(String.class);
		doNothing().when(logger).debug( debugValueCapture.capture());
		when(eventHandlerDBService.getSubscriptions(anyInt(), anyInt(), any(Direction.class), any(String.class))).thenReturn(subscriptionPage);
		doNothing().when(eventHandlerDBService).removeSubscriptionEntries(any());		

		final List<Subscription> response = subscriptionEndOfValidityCheckTask.checkSubscriptionEndOfValidity();
		
		verify(logger, atLeastOnce()).debug(any(String.class));
		final List<String> debugMessages = debugValueCapture.getAllValues();
		assertNotNull(debugMessages);
		assertNotNull(response);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCheckSubscriptionEndOfValidityIfEndDateIsNotInFutureThenToBeRemovedNotNull() {
		final Subscription subscription = createSubscriptionForDBMock(1, "eventType", "subscriberName");
		subscription.setEndDate(ZonedDateTime.now().minusYears(1000));
		final Page<Subscription> subscriptionPage = new PageImpl<>(List.of(subscription));
		
		final ArgumentCaptor<String> debugValueCapture = ArgumentCaptor.forClass(String.class);
		final ArgumentCaptor<String> removeMessageValueCapture = ArgumentCaptor.forClass(String.class);
		doNothing().when(logger).debug(removeMessageValueCapture.capture(), any(Subscription.class));
		doNothing().when(logger).debug(debugValueCapture.capture());
		when(eventHandlerDBService.getSubscriptions(anyInt(), anyInt(), any(Direction.class), any(String.class))).thenReturn(subscriptionPage);
		doNothing().when(eventHandlerDBService).removeSubscriptionEntries(any());	

		final List<Subscription> response = subscriptionEndOfValidityCheckTask.checkSubscriptionEndOfValidity();
		
		verify(logger, atLeastOnce()).debug(any(String.class));
		verify(logger, atLeastOnce()).debug(any(String.class), any(Subscription.class));
		final List<String> debugMessages = debugValueCapture.getAllValues();
		assertNotNull(debugMessages);
		final String removeMessage = removeMessageValueCapture.getValue();
		assertNotNull(removeMessage);
		assertNotNull(response);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCheckSubscriptionEndOfValidityNoSubscriptionsLogsAndReturn() {
		final Subscription subscription = createSubscriptionForDBMock(1, "eventType", "subscriberName");
		subscription.setEndDate(ZonedDateTime.now().minusYears(1000));
		final Page<Subscription> subscriptionPage = new PageImpl<>(List.of());
		
		final ArgumentCaptor<String> debugValueCapture = ArgumentCaptor.forClass(String.class);
		final ArgumentCaptor<String> removeMessageValueCapture = ArgumentCaptor.forClass(String.class);
		doNothing().when(logger).debug(removeMessageValueCapture.capture(), any( Subscription.class));
		doNothing().when(logger).debug(debugValueCapture.capture());
		when(eventHandlerDBService.getSubscriptions(anyInt(), anyInt(), any(Direction.class), any(String.class))).thenReturn(subscriptionPage);
		doNothing().when(eventHandlerDBService).removeSubscriptionEntries(any());
		
		final List<Subscription> response = subscriptionEndOfValidityCheckTask.checkSubscriptionEndOfValidity();
		
		verify(logger, times(2)).debug(any(String.class));
		verify(logger, never()).debug(any(String.class), any(Subscription.class));
		
		final List<String> debugMessages = debugValueCapture.getAllValues();
		assertNotNull(debugMessages);
		assertTrue("Subscription database is empty".equalsIgnoreCase(debugMessages.get(1)));
		assertNotNull(response);
		assertTrue(response.isEmpty());
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------	
	private Subscription createSubscriptionForDBMock(final int i, final String eventType, final String subscriberName) {
		final Subscription subscription = new Subscription(createEventTypeForDBMock(eventType), createSystemForDBMock(subscriberName), null, "notifyUri", false, false, null, null);
		subscription.setId(i);
		subscription.setCreatedAt(ZonedDateTime.now());
		subscription.setUpdatedAt(ZonedDateTime.now());
		
		return subscription;
	}

	//-------------------------------------------------------------------------------------------------	
	private EventType createEventTypeForDBMock(final String eventType) {
		final EventType eventTypeFromDB = new EventType(eventType);
		eventTypeFromDB.setId(1L);
		eventTypeFromDB.setCreatedAt(ZonedDateTime.now());
		eventTypeFromDB.setUpdatedAt(ZonedDateTime.now());
		
		return eventTypeFromDB;		
	}

	//-------------------------------------------------------------------------------------------------	
	private System createSystemForDBMock(final String systemName) {
		final System system = new System();
		system.setId(1L);
		system.setSystemName(systemName);
		system.setAddress("localhost");
		system.setPort(12345);	
		system.setCreatedAt(ZonedDateTime.now());
		system.setUpdatedAt(ZonedDateTime.now());
		
		return system;
	}
}