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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.entity.EventType;
import eu.arrowhead.common.database.entity.Subscription;
import eu.arrowhead.common.database.entity.System;
import eu.arrowhead.common.dto.internal.EventPublishStartDTO;
import eu.arrowhead.common.dto.shared.EventPublishRequestDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.common.http.HttpService;

@RunWith(SpringRunner.class)
public class PublishingQueueWatcherTaskTest {
	
	//=================================================================================================
	// members
	
	@InjectMocks
	private final PublishingQueueWatcherTask testingObject = new PublishingQueueWatcherTask();
	
	@Mock
	private PublishingQueue publishingQueue;
	
	@Mock
	private PublishRequestFixedExecutor expressExecutor;
	
	@Mock
	private HttpService httpService;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRunExpressExecutorOk() {
		ReflectionTestUtils.setField(testingObject, "interrupted", false);
		ReflectionTestUtils.setField(testingObject, "timeStampTolerance", 120);
		ReflectionTestUtils.setField(testingObject, "maxExpressSubscribers", 10);
		
		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();
		final Set<Subscription> involvedSubscriptions = createLargeSetOfSubscriptions(3);
		final EventPublishStartDTO publishStartDTO = new EventPublishStartDTO(request, involvedSubscriptions);
		
		try {
			when(publishingQueue.take()).thenReturn(publishStartDTO).thenThrow(InterruptedException.class);
		} catch (final InterruptedException ex) {
			fail();
		}
		
		doNothing().when(expressExecutor).shutdownExecutionNow();
		
		testingObject.run();
		
		try {
			verify(publishingQueue, times(2)).take();
		} catch (final InterruptedException ex) {
			fail();
		}
		
		verify(expressExecutor, times(1)).execute(any(), any());
		verify(expressExecutor, times(1)).shutdownExecutionNow();
		
		final Boolean runInterupted = (Boolean) ReflectionTestUtils.getField(testingObject, "interrupted");
		assertTrue(runInterupted);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRunExpressExecutorPublishStartRequestNullNoExecution() {
		ReflectionTestUtils.setField(testingObject, "interrupted", false);
		ReflectionTestUtils.setField(testingObject, "timeStampTolerance", 120);
		ReflectionTestUtils.setField(testingObject, "maxExpressSubscribers", 10);
		
		final EventPublishStartDTO publishStartDTO = null;
		try {
			when(publishingQueue.take()).thenReturn(publishStartDTO).thenThrow(InterruptedException.class);
		} catch (final InterruptedException ex) {
			fail();
		}
		
		doNothing().when(expressExecutor).shutdownExecutionNow();
		
		testingObject.run();
		
		try {
			verify(publishingQueue, times(2)).take();
		} catch (final InterruptedException ex) {
			fail();
		}
		
		verify(expressExecutor, never()).execute(any(), any());
		verify(expressExecutor, times(1)).shutdownExecutionNow();
		
		final Boolean runInterupted = (Boolean) ReflectionTestUtils.getField( testingObject, "interrupted");
		assertTrue(runInterupted);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRunExpressExecutorEventPublishRequestNullNoExecution() {
		ReflectionTestUtils.setField(testingObject, "interrupted", false);
		ReflectionTestUtils.setField(testingObject, "timeStampTolerance", 120);
		ReflectionTestUtils.setField(testingObject, "maxExpressSubscribers", 10);
		
		final EventPublishRequestDTO request = null ;
		final Set<Subscription> involvedSubscriptions = createLargeSetOfSubscriptions(3);
		final EventPublishStartDTO publishStartDTO = new EventPublishStartDTO(request, involvedSubscriptions);
		
		try {
			when(publishingQueue.take()).thenReturn(publishStartDTO).thenThrow(InterruptedException.class);
		} catch (final InterruptedException ex) {
			fail();
		}
		
		doNothing().when(expressExecutor).shutdownExecutionNow();
		
		testingObject.run();
		
		try {
			verify(publishingQueue, times(2)).take();
		} catch (final InterruptedException ex) {
			fail();
		}
		
		verify(expressExecutor, never()).execute(any(), any());
		verify(expressExecutor, times(1)).shutdownExecutionNow();
		
		final Boolean runInterupted = (Boolean) ReflectionTestUtils.getField( testingObject, "interrupted");
		assertTrue(runInterupted);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRunExpressExecutorEventPublishRequestEventTypeNullNoExecution() {
		ReflectionTestUtils.setField(testingObject, "interrupted", false);
		ReflectionTestUtils.setField(testingObject, "timeStampTolerance", 120);
		ReflectionTestUtils.setField(testingObject, "maxExpressSubscribers", 10);
		
		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();
		request.setEventType(null);
		final Set<Subscription> involvedSubscriptions = createLargeSetOfSubscriptions(3);
		final EventPublishStartDTO publishStartDTO = new EventPublishStartDTO(request, involvedSubscriptions);
		
		try {
			when(publishingQueue.take()).thenReturn(publishStartDTO).thenThrow(InterruptedException.class);
		} catch (final InterruptedException ex) {
			fail();
		}
		
		doNothing().when(expressExecutor).shutdownExecutionNow();
		
		testingObject.run();
		
		try {
			verify(publishingQueue, times(2)).take();
		} catch (final InterruptedException ex) {
			fail();
		}
		
		verify(expressExecutor, never()).execute(any(), any());
		verify(expressExecutor, times(1)).shutdownExecutionNow();
		
		final Boolean runInterupted = (Boolean) ReflectionTestUtils.getField( testingObject, "interrupted");
		assertTrue(runInterupted);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRunExpressExecutorEventPublishRequestEventTypeEmptyNoExecution() {
		ReflectionTestUtils.setField(testingObject, "interrupted", false);
		ReflectionTestUtils.setField(testingObject, "timeStampTolerance", 120);
		ReflectionTestUtils.setField(testingObject, "maxExpressSubscribers", 10);
		
		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();
		request.setEventType("   ");
		final Set<Subscription> involvedSubscriptions = createLargeSetOfSubscriptions(3);
		final EventPublishStartDTO publishStartDTO = new EventPublishStartDTO(request, involvedSubscriptions);
		
		try {
			when(publishingQueue.take()).thenReturn(publishStartDTO).thenThrow(InterruptedException.class);
		} catch (final InterruptedException ex) {
			fail();
		}
		
		doNothing().when(expressExecutor).shutdownExecutionNow();
		
		testingObject.run();
		
		try {
			verify(publishingQueue, times(2)).take();
		} catch (final InterruptedException ex) {
			fail();
		}
		
		verify(expressExecutor, never()).execute(any(), any());
		verify(expressExecutor, times(1)).shutdownExecutionNow();
		
		final Boolean runInterupted = (Boolean) ReflectionTestUtils.getField( testingObject, "interrupted");
		assertTrue(runInterupted);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRunExpressExecutorEventPublishRequestPayloadNullNoExecution() {
		ReflectionTestUtils.setField(testingObject, "interrupted", false);
		ReflectionTestUtils.setField(testingObject, "timeStampTolerance", 120);
		ReflectionTestUtils.setField(testingObject, "maxExpressSubscribers", 10);
		
		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();
		request.setPayload(null);
		final Set<Subscription> involvedSubscriptions = createLargeSetOfSubscriptions(3);
		final EventPublishStartDTO publishStartDTO = new EventPublishStartDTO(request, involvedSubscriptions);
		
		try {
			when(publishingQueue.take()).thenReturn(publishStartDTO).thenThrow(InterruptedException.class);
		} catch (final InterruptedException ex) {
			fail();
		}
		
		doNothing().when(expressExecutor).shutdownExecutionNow();
		
		testingObject.run();
		
		try {
			verify(publishingQueue, times(2)).take();
		} catch (final InterruptedException ex) {
			fail();
		}
		
		verify(expressExecutor, never()).execute(any(), any());
		verify(expressExecutor, times(1)).shutdownExecutionNow();
		
		final Boolean runInterupted = (Boolean) ReflectionTestUtils.getField( testingObject, "interrupted");
		assertTrue(runInterupted);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRunExpressExecutorEventPublishRequestPayloadEmptyNoExecution() {
		ReflectionTestUtils.setField(testingObject, "interrupted", false);
		ReflectionTestUtils.setField(testingObject, "timeStampTolerance", 120);
		ReflectionTestUtils.setField(testingObject, "maxExpressSubscribers", 10);
		
		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();
		request.setPayload("   ");
		final Set<Subscription> involvedSubscriptions = createLargeSetOfSubscriptions(3);
		final EventPublishStartDTO publishStartDTO = new EventPublishStartDTO(request, involvedSubscriptions);
		
		try {
			when(publishingQueue.take()).thenReturn(publishStartDTO).thenThrow(InterruptedException.class);
		} catch (final InterruptedException ex) {
			fail();
		}
		
		doNothing().when(expressExecutor).shutdownExecutionNow();
		
		testingObject.run();
		
		try {
			verify(publishingQueue, times(2)).take();
		} catch (final InterruptedException ex) {
			fail();
		}
		
		verify(expressExecutor, never()).execute(any(), any());
		verify(expressExecutor, times(1)).shutdownExecutionNow();
		
		final Boolean runInterupted = (Boolean) ReflectionTestUtils.getField( testingObject, "interrupted");
		assertTrue(runInterupted);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRunExpressExecutorEventPublishRequestTimeStampNullNoExecution() {
		ReflectionTestUtils.setField(testingObject, "interrupted", false);
		ReflectionTestUtils.setField(testingObject, "timeStampTolerance", 120);
		ReflectionTestUtils.setField(testingObject, "maxExpressSubscribers", 10);
		
		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();
		request.setTimeStamp(null);
		final Set<Subscription> involvedSubscriptions = createLargeSetOfSubscriptions(3);
		final EventPublishStartDTO publishStartDTO = new EventPublishStartDTO(request, involvedSubscriptions);
		
		try {
			when(publishingQueue.take()).thenReturn(publishStartDTO).thenThrow(InterruptedException.class);
		} catch (final InterruptedException ex) {
			fail();
		}
		
		doNothing().when(expressExecutor).shutdownExecutionNow();
		
		testingObject.run();
		
		try {
			verify(publishingQueue, times(2)).take();
		} catch (final InterruptedException ex) {
			fail();
		}
		
		verify(expressExecutor, never()).execute(any(), any());
		verify(expressExecutor, times(1)).shutdownExecutionNow();
		
		final Boolean runInterupted = (Boolean) ReflectionTestUtils.getField(testingObject, "interrupted");
		assertTrue(runInterupted);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRunExpressExecutorEventPublishRequestTimeStampEmptyNoExecution() {
		ReflectionTestUtils.setField(testingObject, "interrupted", false);
		ReflectionTestUtils.setField(testingObject, "timeStampTolerance", 120);
		ReflectionTestUtils.setField(testingObject, "maxExpressSubscribers", 10);
		
		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();
		request.setTimeStamp("   ");
		final Set<Subscription> involvedSubscriptions = createLargeSetOfSubscriptions( 3 );
		final EventPublishStartDTO publishStartDTO = new EventPublishStartDTO(request, involvedSubscriptions);
		
		try {
			when(publishingQueue.take()).thenReturn(publishStartDTO).thenThrow(InterruptedException.class);
		} catch (final InterruptedException ex) {
			fail();
		}
		
		doNothing().when(expressExecutor).shutdownExecutionNow();
		
		testingObject.run();
		
		try {
			verify(publishingQueue, times(2)).take();
		} catch (final InterruptedException ex) {
			fail();
		}
		
		verify(expressExecutor, never()).execute(any(), any());
		verify(expressExecutor, times(1)).shutdownExecutionNow();
		
		final Boolean runInterupted = (Boolean) ReflectionTestUtils.getField(testingObject, "interrupted");
		assertTrue(runInterupted);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRunExpressExecutorEventPublishRequestTimeStampInvalidFormatNoExecution() {
		ReflectionTestUtils.setField(testingObject, "interrupted", false);
		ReflectionTestUtils.setField(testingObject, "timeStampTolerance", 120);
		ReflectionTestUtils.setField(testingObject, "maxExpressSubscribers", 10);
		
		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();
		request.setTimeStamp("2019_10_07 10:58:00");
		final Set<Subscription> involvedSubscriptions = createLargeSetOfSubscriptions(3);
		final EventPublishStartDTO publishStartDTO = new EventPublishStartDTO(request, involvedSubscriptions);
		
		try {
			when(publishingQueue.take()).thenReturn(publishStartDTO).thenThrow(InterruptedException.class);
		} catch (final InterruptedException ex) {
			fail();
		}
		
		doNothing().when(expressExecutor).shutdownExecutionNow();
		
		testingObject.run();
		
		try {
			verify(publishingQueue, times(2)).take();
		} catch (final InterruptedException ex) {
			fail();
		}
		
		verify(expressExecutor, never()).execute(any(), any());
		verify(expressExecutor, times(1)).shutdownExecutionNow();
		
		final Boolean runInterupted = (Boolean) ReflectionTestUtils.getField(testingObject, "interrupted");
		assertTrue(runInterupted);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRunExpressExecutorEventPublishRequestTimeStampInFutureNoExecution() {
		ReflectionTestUtils.setField(testingObject, "interrupted", false);
		ReflectionTestUtils.setField(testingObject, "timeStampTolerance", 120);
		ReflectionTestUtils.setField(testingObject, "maxExpressSubscribers", 10);
		
		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();
		request.setTimeStamp("3019-10-07 10:58:00");
		final Set<Subscription> involvedSubscriptions = createLargeSetOfSubscriptions(3);
		final EventPublishStartDTO publishStartDTO = new EventPublishStartDTO(request, involvedSubscriptions);
		
		try {
			when(publishingQueue.take()).thenReturn(publishStartDTO).thenThrow(InterruptedException.class);
		} catch (final InterruptedException ex) {
			fail();
		}
		
		doNothing().when(expressExecutor).shutdownExecutionNow();
		
		testingObject.run();
		
		try {
			verify(publishingQueue, times(2)).take();
		} catch (final InterruptedException ex) {
			fail();
		}
		
		verify(expressExecutor, never()).execute(any(), any());
		verify(expressExecutor, times(1)).shutdownExecutionNow();
		
		final Boolean runInterupted = (Boolean) ReflectionTestUtils.getField(testingObject, "interrupted");
		assertTrue(runInterupted);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRunExpressExecutorEventPublishRequestTimeStampInPastNoExecution() {
		ReflectionTestUtils.setField(testingObject, "interrupted", false);
		ReflectionTestUtils.setField(testingObject, "timeStampTolerance", 120);
		ReflectionTestUtils.setField(testingObject, "maxExpressSubscribers", 10);
		
		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();
		request.setTimeStamp("1019-10-07 10:58:00");
		final Set<Subscription> involvedSubscriptions = createLargeSetOfSubscriptions(3);
		final EventPublishStartDTO publishStartDTO = new EventPublishStartDTO(request, involvedSubscriptions);
		
		try {
			when(publishingQueue.take()).thenReturn(publishStartDTO).thenThrow(InterruptedException.class);
		} catch (final InterruptedException ex) {
			fail();
		}
		
		doNothing().when(expressExecutor).shutdownExecutionNow();
		
		testingObject.run();
		
		try {
			verify(publishingQueue, times(2)).take();
		} catch (final InterruptedException ex) {
			fail();
		}
		
		verify(expressExecutor, never()).execute(any(), any());
		verify(expressExecutor, times(1)).shutdownExecutionNow();
		
		final Boolean runInterupted = (Boolean) ReflectionTestUtils.getField(testingObject, "interrupted");
		assertTrue(runInterupted);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRunExpressExecutorEventPublishRequestSourceNullNoExecution() {
		ReflectionTestUtils.setField(testingObject, "interrupted", false);
		ReflectionTestUtils.setField(testingObject, "timeStampTolerance", 120);
		ReflectionTestUtils.setField(testingObject, "maxExpressSubscribers", 10);
		
		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();
		request.setSource(null);
		final Set<Subscription> involvedSubscriptions = createLargeSetOfSubscriptions(3);
		final EventPublishStartDTO publishStartDTO = new EventPublishStartDTO(request, involvedSubscriptions);
		
		try {
			when(publishingQueue.take()).thenReturn(publishStartDTO).thenThrow(InterruptedException.class);
		} catch (final InterruptedException ex) {
			fail();
		}
		
		doNothing().when(expressExecutor).shutdownExecutionNow();
		
		testingObject.run();
		
		try {
			verify(publishingQueue, times(2)).take();
		} catch (final InterruptedException ex) {
			fail();
		}
		
		verify(expressExecutor, never()).execute(any(), any());
		verify(expressExecutor, times(1)).shutdownExecutionNow();
		
		final Boolean runInterupted = (Boolean) ReflectionTestUtils.getField(testingObject, "interrupted");
		assertTrue(runInterupted);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRunExpressExecutorEventPublishRequestSourceSystemNameNullNoExecution() {
		ReflectionTestUtils.setField(testingObject, "interrupted", false);
		ReflectionTestUtils.setField(testingObject, "timeStampTolerance", 120);
		ReflectionTestUtils.setField(testingObject, "maxExpressSubscribers", 10);
		
		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();
		request.getSource().setSystemName(null);
		final Set<Subscription> involvedSubscriptions = createLargeSetOfSubscriptions(3);
		final EventPublishStartDTO publishStartDTO = new EventPublishStartDTO(request, involvedSubscriptions);
		
		try {
			when(publishingQueue.take()).thenReturn(publishStartDTO).thenThrow(InterruptedException.class);
		} catch (final InterruptedException ex) {
			fail();
		}
		
		doNothing().when(expressExecutor).shutdownExecutionNow();
		
		testingObject.run();
		
		try {
			verify(publishingQueue, times(2)).take();
		} catch (final InterruptedException ex) {
			fail();
		}
		
		verify(expressExecutor, never()).execute(any(), any());
		verify(expressExecutor, times(1)).shutdownExecutionNow();
		
		final Boolean runInterupted = (Boolean) ReflectionTestUtils.getField(testingObject, "interrupted");
		assertTrue(runInterupted);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRunExpressExecutorEventPublishRequestSourceSystemNameEmptyNoExecution() {
		ReflectionTestUtils.setField(testingObject, "interrupted", false);
		ReflectionTestUtils.setField(testingObject, "timeStampTolerance", 120);
		ReflectionTestUtils.setField(testingObject, "maxExpressSubscribers", 10);
		
		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();
		request.getSource().setSystemName("   ");
		final Set<Subscription> involvedSubscriptions = createLargeSetOfSubscriptions(3);
		final EventPublishStartDTO publishStartDTO = new EventPublishStartDTO(request, involvedSubscriptions);
		
		try {
			when(publishingQueue.take()).thenReturn(publishStartDTO).thenThrow(InterruptedException.class);
		} catch (final InterruptedException ex) {
			fail();
		}
		
		doNothing().when(expressExecutor).shutdownExecutionNow();
		
		testingObject.run();
		
		try {
			verify(publishingQueue, times(2)).take();
		} catch (final InterruptedException ex) {
			fail();
		}
		
		verify(expressExecutor, never()).execute(any(), any());
		verify(expressExecutor, times(1)).shutdownExecutionNow();
		
		final Boolean runInterupted = (Boolean) ReflectionTestUtils.getField(testingObject, "interrupted");
		assertTrue(runInterupted);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRunExpressExecutorEventPublishRequestSourceSystemAddressNullNoExecution() {
		ReflectionTestUtils.setField(testingObject, "interrupted", false);
		ReflectionTestUtils.setField(testingObject, "timeStampTolerance", 120);
		ReflectionTestUtils.setField(testingObject, "maxExpressSubscribers", 10);
		
		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();
		request.getSource().setAddress(null);
		final Set<Subscription> involvedSubscriptions = createLargeSetOfSubscriptions(3);
		final EventPublishStartDTO publishStartDTO = new EventPublishStartDTO(request, involvedSubscriptions);
		
		try {
			when(publishingQueue.take()).thenReturn(publishStartDTO).thenThrow(InterruptedException.class);
		} catch (final InterruptedException ex) {
			fail();
		}
		
		doNothing().when(expressExecutor).shutdownExecutionNow();
		
		testingObject.run();
		
		try {
			verify(publishingQueue, times(2)).take();
		} catch (final InterruptedException ex) {
			fail();
		}
		
		verify(expressExecutor, never()).execute(any(), any());
		verify(expressExecutor, times(1)).shutdownExecutionNow();
		
		final Boolean runInterupted = (Boolean) ReflectionTestUtils.getField(testingObject, "interrupted");
		assertTrue(runInterupted);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRunExpressExecutorEventPublishRequestSourceSystemAddressEmptyNoExecution() {
		ReflectionTestUtils.setField(testingObject, "interrupted", false);
		ReflectionTestUtils.setField(testingObject, "timeStampTolerance", 120);
		ReflectionTestUtils.setField(testingObject, "maxExpressSubscribers", 10);
		
		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();
		request.getSource().setAddress("   ");
		final Set<Subscription> involvedSubscriptions = createLargeSetOfSubscriptions(3);
		final EventPublishStartDTO publishStartDTO = new EventPublishStartDTO(request, involvedSubscriptions);
		
		try {
			when(publishingQueue.take()).thenReturn(publishStartDTO).thenThrow(InterruptedException.class);
		} catch (final InterruptedException ex) {
			fail();
		}
		
		doNothing().when(expressExecutor).shutdownExecutionNow();
		
		testingObject.run();
		
		try {
			verify(publishingQueue, times(2)).take();
		} catch (final InterruptedException ex) {
			fail();
		}
		
		verify(expressExecutor, never()).execute(any(), any());
		verify(expressExecutor, times(1)).shutdownExecutionNow();
		
		final Boolean runInterupted = (Boolean) ReflectionTestUtils.getField(testingObject, "interrupted");
		assertTrue(runInterupted);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRunExpressExecutorEventPublishRequestSourceSystemPortNullNoExecution() {
		ReflectionTestUtils.setField(testingObject, "interrupted", false);
		ReflectionTestUtils.setField(testingObject, "timeStampTolerance", 120);
		ReflectionTestUtils.setField(testingObject, "maxExpressSubscribers", 10);
		
		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();
		request.getSource().setPort(null);
		final Set<Subscription> involvedSubscriptions = createLargeSetOfSubscriptions(3);
		final EventPublishStartDTO publishStartDTO = new EventPublishStartDTO(request, involvedSubscriptions);
		
		try {
			when(publishingQueue.take()).thenReturn(publishStartDTO).thenThrow(InterruptedException.class);
		} catch (final InterruptedException ex) {
			fail();
		}
		
		doNothing().when(expressExecutor).shutdownExecutionNow();
		
		testingObject.run();
		
		try {
			verify(publishingQueue, times(2)).take();
		} catch (final InterruptedException ex) {
			fail();
		}
		
		verify(expressExecutor, never()).execute(any(), any());
		verify(expressExecutor, times(1)).shutdownExecutionNow();
		
		final Boolean runInterupted = (Boolean) ReflectionTestUtils.getField(testingObject, "interrupted");
		assertTrue(runInterupted);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRunExpressExecutorEventPublishRequestSourceSystemPortLessThanOneNoExecution() {
		ReflectionTestUtils.setField(testingObject, "interrupted", false);
		ReflectionTestUtils.setField(testingObject, "timeStampTolerance", 120);
		ReflectionTestUtils.setField(testingObject, "maxExpressSubscribers", 10);
		
		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();
		request.getSource().setPort(0);
		final Set<Subscription> involvedSubscriptions = createLargeSetOfSubscriptions(3);
		final EventPublishStartDTO publishStartDTO = new EventPublishStartDTO(request, involvedSubscriptions);
		
		try {
			when(publishingQueue.take()).thenReturn(publishStartDTO).thenThrow(InterruptedException.class);
		} catch (final InterruptedException ex) {
			fail();
		}
		
		doNothing().when(expressExecutor).shutdownExecutionNow();
		
		testingObject.run();
		
		try {
			verify(publishingQueue, times(2)).take();
		} catch (final InterruptedException ex) {
			fail();
		}
		
		verify(expressExecutor, never()).execute(any(), any());
		verify(expressExecutor, times(1)).shutdownExecutionNow();
		
		final Boolean runInterupted = (Boolean) ReflectionTestUtils.getField(testingObject, "interrupted");
		assertTrue(runInterupted);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRunExpressExecutorInvolvedSubscriptionsNullOneNoExecution() {
		ReflectionTestUtils.setField(testingObject, "interrupted", false);
		ReflectionTestUtils.setField(testingObject, "timeStampTolerance", 120);
		ReflectionTestUtils.setField(testingObject, "maxExpressSubscribers", 10);
		
		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();		
		final Set<Subscription> involvedSubscriptions = null;
		final EventPublishStartDTO publishStartDTO = new EventPublishStartDTO(request, involvedSubscriptions);
		
		try {
			when(publishingQueue.take()).thenReturn(publishStartDTO).thenThrow(InterruptedException.class);
		} catch (final InterruptedException ex) {
			fail();
		}
		
		doNothing().when(expressExecutor).shutdownExecutionNow();
		
		testingObject.run();
		
		try {
			verify(publishingQueue, times(2)).take();
		} catch (final InterruptedException ex) {
			fail();
		}
		
		verify(expressExecutor, never()).execute(any(), any());
		verify(expressExecutor, times(1)).shutdownExecutionNow();
		
		final Boolean runInterupted = (Boolean) ReflectionTestUtils.getField(testingObject, "interrupted");
		assertTrue(runInterupted);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRunExpressExecutorInvolvedSubscriptionsEmptyOneNoExecution() {
		ReflectionTestUtils.setField(testingObject, "interrupted", false);
		ReflectionTestUtils.setField(testingObject, "timeStampTolerance", 120);
		ReflectionTestUtils.setField(testingObject, "maxExpressSubscribers", 10);
		
		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();		
		final Set<Subscription> involvedSubscriptions = Set.of();
		final EventPublishStartDTO publishStartDTO = new EventPublishStartDTO(request, involvedSubscriptions);
		
		try {
			when(publishingQueue.take()).thenReturn(publishStartDTO).thenThrow(InterruptedException.class);
		} catch (final InterruptedException ex) {
			fail();
		}
		
		doNothing().when(expressExecutor).shutdownExecutionNow();
		
		testingObject.run();
		
		try {
			verify(publishingQueue, times(2)).take();
		} catch (final InterruptedException ex) {
			fail();
		}
		
		verify(expressExecutor, never()).execute(any(), any());
		verify(expressExecutor, times(1)).shutdownExecutionNow();
		
		final Boolean runInterupted = (Boolean) ReflectionTestUtils.getField(testingObject, "interrupted");
		assertTrue(runInterupted);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRunPublishRequestExecutorOk() {
		ReflectionTestUtils.setField(testingObject, "interrupted", false);
		ReflectionTestUtils.setField(testingObject, "timeStampTolerance", 120);
		ReflectionTestUtils.setField(testingObject, "maxExpressSubscribers", 10);
		ReflectionTestUtils.setField(testingObject, "httpService",  httpService);
		
		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();
		final Set<Subscription> involvedSubscriptions = createLargeSetOfSubscriptions(13);
		final EventPublishStartDTO publishStartDTO = new EventPublishStartDTO(request, involvedSubscriptions);
		
		try {
			when(publishingQueue.take()).thenReturn(publishStartDTO).thenThrow(InterruptedException.class);
		} catch (final InterruptedException ex) {
			fail();
		}
		
		doNothing().when(expressExecutor).shutdownExecutionNow();
		
		testingObject.run();
		
		try {
			verify(publishingQueue, times(2)).take();
		} catch (final InterruptedException ex) {
			fail();
		}
		
		verify(expressExecutor, never()).execute(any(), any());
		verify(expressExecutor, times(1)).shutdownExecutionNow();
		
		final Boolean runInterupted = (Boolean) ReflectionTestUtils.getField(testingObject, "interrupted");
		assertTrue(runInterupted);
	}
	
	//-------------------------------------------------------------------------------------------------
	// testRunPublishRequestExecutor use same validation as expressExecutor
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRunInteruptedOk() {
		ReflectionTestUtils.setField(testingObject, "interrupted", false);
		ReflectionTestUtils.setField(testingObject, "timeStampTolerance", 120);
		ReflectionTestUtils.setField(testingObject, "maxExpressSubscribers", 10);
		
		try {
			when(publishingQueue.take()).thenThrow(InterruptedException.class);
		} catch (final InterruptedException ex) {
			fail();
		}
		
		doNothing().when(expressExecutor).shutdownExecutionNow();
		
		testingObject.run();
		
		verify(expressExecutor, times(1)).shutdownExecutionNow();
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