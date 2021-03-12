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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.entity.EventType;
import eu.arrowhead.common.database.entity.Subscription;
import eu.arrowhead.common.database.entity.System;
import eu.arrowhead.common.dto.shared.EventPublishRequestDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.common.http.HttpService;

@RunWith(SpringRunner.class)
public class PublishRequestExecutorTest {
	
	//=================================================================================================
	// members
	
	private PublishRequestExecutor testingObject;
	
	private  ThreadPoolExecutor threadPool;
	
	private Logger logger;
	
	@Mock
	private HttpService httpService;
	
	private final int numberOfSubscribers = 17;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
    @Before
    public void setUp() throws Exception {
		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();
		final Set<Subscription> involvedSubscriptions = createLargeSetOfSubscriptions(numberOfSubscribers);
		testingObject = new PublishRequestExecutor(request, involvedSubscriptions, httpService);
       
		threadPool = mock(ThreadPoolExecutor.class, "threadPool");
		ReflectionTestUtils.setField(testingObject, "threadPool", threadPool);
		
		logger = mock(Logger.class);
		ReflectionTestUtils.setField(testingObject, "logger", logger);
    }
	
	//=================================================================================================
	// Tests of execute
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testExecuteOk() {
		final Subscription subscription0 = createSubscriptionForDBMock(1 , "eventType1", "subscriberName1");
		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();
		final PublishEventTask publishEventTask = new PublishEventTask(subscription0, request, httpService);
		
		doNothing().when(threadPool).execute(any());
		
		testingObject.execute();
		
		verify(threadPool, times(numberOfSubscribers)).execute(any());
		final Subscription subscriptionInTask = (Subscription) ReflectionTestUtils.getField(publishEventTask, "subscription");
		assertNotNull(subscriptionInTask);
		verify(threadPool, times(1)).shutdownNow();
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testExecuteThrowExceptionThenContinueOk() {
		final Subscription subscription0 = createSubscriptionForDBMock(1 , "eventType1", "subscriberName1");
		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();
		final PublishEventTask publishEventTask = new PublishEventTask(subscription0, request, httpService);
		
		final ArgumentCaptor<String> valueCapture = ArgumentCaptor.forClass(String.class);
		doThrow(RejectedExecutionException.class).doNothing().when(threadPool).execute(any());
		doNothing().when(logger).error(valueCapture.capture(), any(ZonedDateTime.class));
		
		testingObject.execute();
		
		verify(threadPool, times(numberOfSubscribers)).execute(any());
		final Subscription subscriptionInTask = (Subscription) ReflectionTestUtils.getField(publishEventTask, "subscription");
		assertNotNull(subscriptionInTask);
		verify(threadPool, times(1)).shutdownNow();
		verify(logger, times(1)).error(any(String.class), any(ZonedDateTime.class));
		final String logMessage = valueCapture.getValue();
		assertNotNull(logMessage);
		assertTrue("PublishEventTask execution rejected at {}".equalsIgnoreCase(logMessage));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testExecuteInvalidFieldInvolvedSubscriptionsNullCauseNoExecution() {
		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();
		ReflectionTestUtils.setField(testingObject, "publishRequestDTO", request);
		ReflectionTestUtils.setField(testingObject, "involvedSubscriptions", null);

		doNothing().when(threadPool).execute(any());
		
		try {
			testingObject.execute();
		} catch (final Exception ex) {
			verify(threadPool, never()).execute(any());
			assertTrue(ex.getMessage().contains( "involvedSubscriptions is null"));
			throw ex;
		}
	}	
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testExecuteInvalidFieldRequestNullCauseNoExecution() {
		final Set<Subscription> involvedSubscriptions = createLargeSetOfSubscriptions(numberOfSubscribers);
		ReflectionTestUtils.setField(testingObject, "publishRequestDTO", null);
		ReflectionTestUtils.setField(testingObject, "involvedSubscriptions", involvedSubscriptions);

		doNothing().when(threadPool).execute(any());
		
		try {
			testingObject.execute();
		} catch (final Exception ex) {
			verify(threadPool, never()).execute(any());
			assertTrue(ex.getMessage().contains( "publishRequestDTO is null" ));
			throw ex;
		}
	}	
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testExecuteInvalidFieldHttpServiceNullCauseNoExecution() {
		final Set<Subscription> involvedSubscriptions = createLargeSetOfSubscriptions(numberOfSubscribers);
		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();

		ReflectionTestUtils.setField(testingObject, "publishRequestDTO", request);
		ReflectionTestUtils.setField(testingObject, "involvedSubscriptions", involvedSubscriptions);
		ReflectionTestUtils.setField(testingObject, "httpService", null);
		
		doNothing().when(threadPool).execute(any());
		
		try {
			testingObject.execute();
		} catch (final Exception ex) {
			verify(threadPool, never()).execute(any());
			assertTrue(ex.getMessage().contains( "httpService is null"));
			throw ex;
		}
	}
	
	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------	
	private Set<Subscription> createLargeSetOfSubscriptions(final int size) {
		final Set<Subscription> involvedSubscriptions = new HashSet<Subscription>();
		for (int i = 0; i < size; ++i) {
			involvedSubscriptions.add(createSubscriptionForDBMock(i + 1, "eventType" + i, "subscriberName")); 
		}
		
		return involvedSubscriptions;
	}

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
	private SystemRequestDTO getSystemRequestDTO() {
		final SystemRequestDTO systemRequestDTO = new SystemRequestDTO();
		systemRequestDTO.setSystemName("systemName");
		systemRequestDTO.setAddress("localhost");
		systemRequestDTO.setPort(12345);	
		
		return systemRequestDTO;
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

	//-------------------------------------------------------------------------------------------------		
	private EventPublishRequestDTO getEventPublishRequestDTOForTest() {
		return new EventPublishRequestDTO("eventType", getSystemRequestDTO(), null, "payload", Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now().plusSeconds(1)));
	}
}