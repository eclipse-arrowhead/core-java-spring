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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
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
public class PublishRequestFixedExecutorTest {
	
	//=================================================================================================
	// members
	
	@InjectMocks
	private final PublishRequestFixedExecutor testingObject = new PublishRequestFixedExecutor();
	
	@Mock
	private PublishingQueue publishingQueue;
	
	@Mock
	private  ThreadPoolExecutor threadPool;
	
	@Mock
	private HttpService httpService;
	
	//=================================================================================================
	// methods
	
	//=================================================================================================
	// Tests of init
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitOk() {
		ReflectionTestUtils.setField(testingObject, "threadPool", null);
		ReflectionTestUtils.setField(testingObject, "maxExpressSubscribers", 10);
		
		final ThreadPoolExecutor threadPoolBeforeInit = (ThreadPoolExecutor) ReflectionTestUtils.getField(testingObject, "threadPool");
		assertNull(threadPoolBeforeInit);

		testingObject.init();
		
		final ThreadPoolExecutor threadPoolAfterInit = (ThreadPoolExecutor) ReflectionTestUtils.getField(testingObject, "threadPool");
		assertNotNull(threadPoolAfterInit);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitMaxExpressSubscribersIsLessThanMaxThreadPoolSize() {
		ReflectionTestUtils.setField(testingObject, "threadPool", null);
		ReflectionTestUtils.setField(testingObject, "maxExpressSubscribers", 10);
		
		final int maxThreadPoolSize = (int) ReflectionTestUtils.getField(testingObject, "MAX_THREAD_POOL_SIZE");
		final int maxExpressSubscribers = (int) ReflectionTestUtils.getField(testingObject, "maxExpressSubscribers");
		assertTrue(maxExpressSubscribers < maxThreadPoolSize);

		testingObject.init();
		
		final ThreadPoolExecutor threadPoolAfterInit = (ThreadPoolExecutor) ReflectionTestUtils.getField(testingObject, "threadPool");
		assertNotNull(threadPoolAfterInit);
		assertTrue(threadPoolAfterInit.getMaximumPoolSize() == maxExpressSubscribers);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitMaxExpressSubscribersIsMoreThanMaxThreadPoolSize() {
		ReflectionTestUtils.setField(testingObject, "threadPool", null);
		ReflectionTestUtils.setField(testingObject, "maxExpressSubscribers", 30);
		
		final int maxThreadPoolSize = (int) ReflectionTestUtils.getField(testingObject, "MAX_THREAD_POOL_SIZE");
		final int maxExpressSubscribers = (int) ReflectionTestUtils.getField(testingObject, "maxExpressSubscribers");
		assertTrue(maxExpressSubscribers > maxThreadPoolSize);

		testingObject.init();
		
		final ThreadPoolExecutor threadPoolAfterInit = (ThreadPoolExecutor) ReflectionTestUtils.getField(testingObject, "threadPool");
		assertNotNull(threadPoolAfterInit);
		assertTrue(threadPoolAfterInit.getMaximumPoolSize() == maxThreadPoolSize);
	}

	//=================================================================================================
	// Tests of execute
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testExecuteOk() {
		final int numberOfSubscribers = 20;
		ReflectionTestUtils.setField(testingObject, "threadPool", threadPool);
		ReflectionTestUtils.setField(testingObject, "maxExpressSubscribers", 10);
		
		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();
		final Set<Subscription> involvedSubscriptions = createLargeSetOfSubscriptions(numberOfSubscribers);
		
		testingObject.init();
		
		final ArgumentCaptor<PublishEventTask> valueCapture = ArgumentCaptor.forClass(PublishEventTask.class);
		doNothing().when(threadPool).execute(valueCapture.capture());
		
		testingObject.execute(request, involvedSubscriptions);
		
		verify(threadPool, times(numberOfSubscribers)).execute(any());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testExecuteIllegalArgumentSubscriptionNullCauseNoExecution() {
		ReflectionTestUtils.setField(testingObject, "threadPool", threadPool);
		ReflectionTestUtils.setField(testingObject, "maxExpressSubscribers", 10);
		
		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();
		
		final Subscription subscription0 = null;
		final Subscription subscription1 = createSubscriptionForDBMock(1 , "eventType", "subscriberName");
		final Set<Subscription> involvedSubscriptions = new HashSet<>();
		involvedSubscriptions.add(subscription0);
		involvedSubscriptions.add(subscription1);
		
		testingObject.init();
		
		final ArgumentCaptor<PublishEventTask> valueCapture = ArgumentCaptor.forClass(PublishEventTask.class);
		doNothing().when(threadPool).execute(valueCapture.capture());
		
		testingObject.execute(request, involvedSubscriptions);
		
		verify(threadPool, times(1)).execute(any());
		
		final PublishEventTask publishEventTask = valueCapture.getValue();
		final Subscription subscriptionInTask = (Subscription) ReflectionTestUtils.getField(publishEventTask, "subscription");
		assertNotNull(subscriptionInTask);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testExecuteIllegalArgumentSubscriptionNotifyUriNullCauseNoExecution() {
		ReflectionTestUtils.setField(testingObject, "threadPool", threadPool);
		ReflectionTestUtils.setField(testingObject, "maxExpressSubscribers", 10);
		
		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();
		final Subscription subscription0 = createSubscriptionForDBMock(1 , "eventType", "subscriberName");
		subscription0.setNotifyUri(null);
		final Subscription subscription1 = createSubscriptionForDBMock(11 , "eventType1", "subscriberName1");
		final Set<Subscription> involvedSubscriptions = new HashSet<>();
		involvedSubscriptions.add(subscription0);
		involvedSubscriptions.add(subscription1);
		
		testingObject.init();
		
		final ArgumentCaptor<PublishEventTask> valueCapture = ArgumentCaptor.forClass(PublishEventTask.class);
		doNothing().when(threadPool).execute(valueCapture.capture());
		
		testingObject.execute(request, involvedSubscriptions);
		
		verify(threadPool, times(1)).execute(any());
		
		final PublishEventTask publishEventTask = valueCapture.getValue();
		final Subscription subscriptionInTask = (Subscription) ReflectionTestUtils.getField(publishEventTask, "subscription");
		assertNotNull(subscriptionInTask);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testExecuteIllegalArgumentSubscriptionSubscriberSystemNullCauseNoExecution() {
		ReflectionTestUtils.setField(testingObject, "threadPool", threadPool);
		ReflectionTestUtils.setField(testingObject, "maxExpressSubscribers", 10);
		
		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();
		final Subscription subscription0 = createSubscriptionForDBMock(1 , "eventType", "subscriberName");
		subscription0.setSubscriberSystem(null);
		final Subscription subscription1 = createSubscriptionForDBMock(11 , "eventType1", "subscriberName1");
		final Set<Subscription> involvedSubscriptions = new HashSet<>();
		involvedSubscriptions.add(subscription0);
		involvedSubscriptions.add(subscription1);
		
		testingObject.init();
		
		final ArgumentCaptor<PublishEventTask> valueCapture = ArgumentCaptor.forClass(PublishEventTask.class);
		doNothing().when(threadPool).execute(valueCapture.capture());
		
		testingObject.execute(request, involvedSubscriptions);
		
		verify(threadPool, times(1)).execute(any());
		
		final PublishEventTask publishEventTask = valueCapture.getValue();
		final Subscription subscriptionInTask = (Subscription) ReflectionTestUtils.getField(publishEventTask, "subscription");
		
		assertNotNull(subscriptionInTask);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testExecuteIllegalArgumentSubscriptionSubscriberSystemSystemNameNullCoseNoExecution() {
		ReflectionTestUtils.setField(testingObject, "threadPool", threadPool);
		ReflectionTestUtils.setField(testingObject, "maxExpressSubscribers", 10);
		
		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();
		final Subscription subscription0 = createSubscriptionForDBMock(1 , "eventType", "subscriberName");
		subscription0.getSubscriberSystem().setSystemName(null);
		final Subscription subscription1 = createSubscriptionForDBMock(11 , "eventType1", "subscriberName1");
		final Set<Subscription> involvedSubscriptions = new HashSet<>();
		involvedSubscriptions.add(subscription0);
		involvedSubscriptions.add(subscription1);
		
		testingObject.init();
		
		final ArgumentCaptor<PublishEventTask> valueCapture = ArgumentCaptor.forClass(PublishEventTask.class);
		doNothing().when(threadPool).execute(valueCapture.capture());
		
		testingObject.execute(request, involvedSubscriptions);
		
		verify(threadPool, times(1)).execute(any());
		
		final PublishEventTask publishEventTask = valueCapture.getValue();
		final Subscription subscriptionInTask = (Subscription) ReflectionTestUtils.getField(publishEventTask, "subscription");
		assertNotNull(subscriptionInTask);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testExecuteIllegalArgumentSubscriptionSubscriberSystemSystemAddressNullCoseNoExecution() {
		ReflectionTestUtils.setField(testingObject, "threadPool", threadPool);
		ReflectionTestUtils.setField(testingObject, "maxExpressSubscribers", 10);
		
		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();
		final Subscription subscription0 = createSubscriptionForDBMock(1 , "eventType", "subscriberName");
		subscription0.getSubscriberSystem().setAddress(null);
		final Subscription subscription1 = createSubscriptionForDBMock(11 , "eventType1", "subscriberName1");
		final Set<Subscription> involvedSubscriptions = new HashSet<>();
		involvedSubscriptions.add(subscription0);
		involvedSubscriptions.add(subscription1);
		
		testingObject.init();
		
		final ArgumentCaptor<PublishEventTask> valueCapture = ArgumentCaptor.forClass(PublishEventTask.class);
		doNothing().when(threadPool).execute( valueCapture.capture());
		
		testingObject.execute(request, involvedSubscriptions);
		
		verify(threadPool, times(1)).execute(any());
		
		final PublishEventTask publishEventTask = valueCapture.getValue();
		final Subscription subscriptionInTask = (Subscription) ReflectionTestUtils.getField(publishEventTask, "subscription");
		assertNotNull(subscriptionInTask);
	}
	
	//=================================================================================================
	// Tests of shutdownExecutionNow
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testShutdownExecutionNowOK() {
		ReflectionTestUtils.setField(testingObject, "threadPool", null);
		ReflectionTestUtils.setField(testingObject, "maxExpressSubscribers", 10);
		
		final ThreadPoolExecutor threadPoolBeforeInit = (ThreadPoolExecutor) ReflectionTestUtils.getField(testingObject, "threadPool");
		assertNull(threadPoolBeforeInit);

		testingObject.init();
		
		final ThreadPoolExecutor threadPoolAfterInit = (ThreadPoolExecutor) ReflectionTestUtils.getField(testingObject, "threadPool");
		assertNotNull(threadPoolAfterInit);
		assertFalse(threadPoolAfterInit.isTerminated());
		
		testingObject.shutdownExecutionNow();
		
		final ThreadPoolExecutor threadPoolAfterShutDown = (ThreadPoolExecutor) ReflectionTestUtils.getField(testingObject, "threadPool");
		assertNotNull(threadPoolAfterShutDown);
		assertTrue(threadPoolAfterShutDown.isTerminated());
	}
	
	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------	
	private Set<Subscription> createLargeSetOfSubscriptions( final int size ) {
		final Set<Subscription> involvedSubscriptions = new HashSet<Subscription>();
		for (int i = 0; i < size; ++i) {
			involvedSubscriptions.add(createSubscriptionForDBMock(i + 1, "eventType" + i, "subscriberName")); 
		}
		
		return involvedSubscriptions;
	}

	//-------------------------------------------------------------------------------------------------	
	private Subscription createSubscriptionForDBMock(final int i, final String eventType, final String subscriberName) {
		final Subscription subscription = new Subscription(createEventTypeForDBMock(eventType),	createSystemForDBMock(subscriberName), null, "notifyUri", false, false, null, null);
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