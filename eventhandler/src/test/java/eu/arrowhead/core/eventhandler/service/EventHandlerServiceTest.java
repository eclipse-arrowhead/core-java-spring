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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.entity.EventType;
import eu.arrowhead.common.database.entity.Subscription;
import eu.arrowhead.common.database.entity.System;
import eu.arrowhead.common.dto.internal.DTOConverter;
import eu.arrowhead.common.dto.shared.EventPublishRequestDTO;
import eu.arrowhead.common.dto.shared.SubscriptionRequestDTO;
import eu.arrowhead.common.dto.shared.SubscriptionResponseDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.common.dto.shared.SystemResponseDTO;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.core.eventhandler.database.service.EventHandlerDBService;
import eu.arrowhead.core.eventhandler.metadatafiltering.MetadataFilteringAlgorithm;

@RunWith(SpringRunner.class)
public class EventHandlerServiceTest {

	//=================================================================================================
	// members
	
	@InjectMocks
	private EventHandlerService eventHandlerService;
	
	@Mock
	private EventHandlerDBService eventHandlerDBService;
	
	@Mock
	private EventHandlerDriver eventHandlerDriver;
	
	@Mock
	private MetadataFilteringAlgorithm metadataFilter;

	//=================================================================================================
	// methods
	
	//=================================================================================================
	// Tests of subscribe
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testSubscribeOK() {
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		final Set<SystemResponseDTO> authorizedPublishers = getSystemResponseDTOSet(7);
		
		when(eventHandlerDriver.getAuthorizedPublishers(any())).thenReturn(authorizedPublishers);
		doNothing().when(eventHandlerDBService).forceRegisterSubscription(any(), any());
	
		eventHandlerService.subscribe(request);
		
		verify(eventHandlerDriver, times(1)).getAuthorizedPublishers(any());
		verify(eventHandlerDBService, times(1)).forceRegisterSubscription(any(), any());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testSubscribeInvalidRequestNull() {
		final SubscriptionRequestDTO request = null;
		final Set<SystemResponseDTO> authorizedPublishers = getSystemResponseDTOSet(7);
		
		when(eventHandlerDriver.getAuthorizedPublishers(any())).thenReturn(authorizedPublishers);
		doNothing().when(eventHandlerDBService).forceRegisterSubscription(any(), any());
	
		eventHandlerService.subscribe(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testSubscribeInvalidRequestParameterNullEventType() {
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		request.setEventType(null);
		final Set<SystemResponseDTO> authorizedPublishers = getSystemResponseDTOSet(7);
		
		when(eventHandlerDriver.getAuthorizedPublishers(any())).thenReturn(authorizedPublishers);
		doNothing().when(eventHandlerDBService).forceRegisterSubscription(any(), any());
	
		eventHandlerService.subscribe(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testSubscribeInvalidRequestParameterEmptyEventType() {
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		request.setEventType("   ");
		final Set<SystemResponseDTO> authorizedPublishers = getSystemResponseDTOSet(7);
		
		when(eventHandlerDriver.getAuthorizedPublishers(any())).thenReturn(authorizedPublishers);
		doNothing().when(eventHandlerDBService).forceRegisterSubscription(any(), any());
	
		eventHandlerService.subscribe(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testSubscribeInvalidRequestParameterNullNotifyUri() {
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		request.setNotifyUri(null);
		final Set<SystemResponseDTO> authorizedPublishers = getSystemResponseDTOSet(7);
		
		when(eventHandlerDriver.getAuthorizedPublishers(any())).thenReturn(authorizedPublishers);
		doNothing().when(eventHandlerDBService).forceRegisterSubscription(any(), any());
	
		eventHandlerService.subscribe(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testSubscribeInvalidRequestParameterEmptyNotifyUri() {
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		request.setNotifyUri("   ");
		final Set<SystemResponseDTO> authorizedPublishers = getSystemResponseDTOSet(7);
		
		when(eventHandlerDriver.getAuthorizedPublishers(any())).thenReturn(authorizedPublishers);
		doNothing().when(eventHandlerDBService).forceRegisterSubscription(any(), any());
	
		eventHandlerService.subscribe(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testSubscribeInvalidRequestParameterMatchMetaDataTrueAndFilterMetaDataNull() {
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		request.setMatchMetaData(true);
		request.setFilterMetaData(null);
		final Set<SystemResponseDTO> authorizedPublishers = getSystemResponseDTOSet(7);
		
		when(eventHandlerDriver.getAuthorizedPublishers(any())).thenReturn(authorizedPublishers);
		doNothing().when(eventHandlerDBService).forceRegisterSubscription(any(), any());
	
		eventHandlerService.subscribe(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testSubscribeInvalidRequestParameterMatchMetaDataTrueAndFilterMetaDataEmpty() {
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		request.setMatchMetaData(true);
		request.setFilterMetaData(Map.of());
		final Set<SystemResponseDTO> authorizedPublishers = getSystemResponseDTOSet(7);
		
		when(eventHandlerDriver.getAuthorizedPublishers(any())).thenReturn(authorizedPublishers);
		doNothing().when(eventHandlerDBService).forceRegisterSubscription(any(), any());
	
		eventHandlerService.subscribe(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testSubscribeInvalidRequestParameterStartDateInPast() {
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		final ZonedDateTime past = ZonedDateTime.now().minusMinutes(10);
		request.setStartDate(Utilities.convertZonedDateTimeToUTCString(past));
		final Set<SystemResponseDTO> authorizedPublishers = getSystemResponseDTOSet(7);
		
		when(eventHandlerDriver.getAuthorizedPublishers(any())).thenReturn(authorizedPublishers);
		doNothing().when(eventHandlerDBService).forceRegisterSubscription(any(), any());
	
		eventHandlerService.subscribe(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testSubscribeInvalidRequestParameterEndDateInPast() {
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		final ZonedDateTime past = ZonedDateTime.now().minusMinutes(10);
		request.setEndDate(Utilities.convertZonedDateTimeToUTCString(past));
		final Set<SystemResponseDTO> authorizedPublishers = getSystemResponseDTOSet(7);
		
		when(eventHandlerDriver.getAuthorizedPublishers(any())).thenReturn(authorizedPublishers);
		doNothing().when(eventHandlerDBService).forceRegisterSubscription(any(), any());
	
		eventHandlerService.subscribe(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testSubscribeInvalidRequestParameterEndDateIsBeforeStarDate() {
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		final ZonedDateTime end = ZonedDateTime.now().plusMinutes(5);
		final ZonedDateTime start = ZonedDateTime.now().plusMinutes(10);
		request.setEndDate(Utilities.convertZonedDateTimeToUTCString(end));
		request.setStartDate(Utilities.convertZonedDateTimeToUTCString(start));
		final Set<SystemResponseDTO> authorizedPublishers = getSystemResponseDTOSet(7);
		
		when(eventHandlerDriver.getAuthorizedPublishers(any())).thenReturn(authorizedPublishers);
		doNothing().when(eventHandlerDBService).forceRegisterSubscription(any(), any());
	
		try {
			eventHandlerService.subscribe(request);
		} catch (final Exception ex) {
			Assert.assertTrue(ex.getMessage().contains("Start Date sould be before End Date"));
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testSubscribeInvalidRequestParameterSubscriberSystemNull() {
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		request.setSubscriberSystem(null);
		final Set<SystemResponseDTO> authorizedPublishers = getSystemResponseDTOSet(7);
		
		when(eventHandlerDriver.getAuthorizedPublishers(any())).thenReturn(authorizedPublishers);
		doNothing().when(eventHandlerDBService).forceRegisterSubscription(any(), any());
	
		try {
			eventHandlerService.subscribe(request);
		} catch (final Exception ex) {
			Assert.assertTrue(ex.getMessage().contains("System is null."));
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testSubscribeInvalidRequestParameterSubscriberSystemSystemNameNull() {
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		request.getSubscriberSystem().setSystemName(null);
		final Set<SystemResponseDTO> authorizedPublishers = getSystemResponseDTOSet(7);
		
		when(eventHandlerDriver.getAuthorizedPublishers(any())).thenReturn(authorizedPublishers);
		doNothing().when(eventHandlerDBService).forceRegisterSubscription(any(), any());
	
		try {
			eventHandlerService.subscribe(request);
		} catch (final Exception ex) {
			Assert.assertTrue(ex.getMessage().contains("System name is null or blank."));
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testSubscribeInvalidRequestParameterSubscriberSystemSystemNameEmpty() {
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		request.getSubscriberSystem().setSystemName("  ");
		final Set<SystemResponseDTO> authorizedPublishers = getSystemResponseDTOSet(7);
		
		when(eventHandlerDriver.getAuthorizedPublishers(any())).thenReturn(authorizedPublishers);
		doNothing().when(eventHandlerDBService).forceRegisterSubscription(any(), any());
	
		try {
			eventHandlerService.subscribe(request);
		} catch (final Exception ex) {
			Assert.assertTrue(ex.getMessage().contains("System name is null or blank."));
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testSubscribeInvalidRequestParameterSubscriberSystemAddressNull() {
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		request.getSubscriberSystem().setAddress(null);
		final Set<SystemResponseDTO> authorizedPublishers = getSystemResponseDTOSet(7);
		
		when(eventHandlerDriver.getAuthorizedPublishers(any())).thenReturn(authorizedPublishers);
		doNothing().when(eventHandlerDBService).forceRegisterSubscription(any(), any());
	
		try {
			eventHandlerService.subscribe(request);
		} catch (final Exception ex) {
			Assert.assertTrue(ex.getMessage().contains("System address is null or blank."));
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testSubscribeInvalidRequestParameterSubscriberSystemAddressEmpty() {
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		request.getSubscriberSystem().setAddress("   ");
		final Set<SystemResponseDTO> authorizedPublishers = getSystemResponseDTOSet(7);
		
		when(eventHandlerDriver.getAuthorizedPublishers(any())).thenReturn(authorizedPublishers);
		doNothing().when(eventHandlerDBService).forceRegisterSubscription(any(), any());
	
		try {
			eventHandlerService.subscribe(request);
		} catch (final Exception ex) {
			Assert.assertTrue(ex.getMessage().contains("System address is null or blank."));
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testSubscribeInvalidRequestParameterSubscriberSystemPortNull() {
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		request.getSubscriberSystem().setPort(null);
		final Set<SystemResponseDTO> authorizedPublishers = getSystemResponseDTOSet(7);
		
		when(eventHandlerDriver.getAuthorizedPublishers(any())).thenReturn(authorizedPublishers);
		doNothing().when(eventHandlerDBService).forceRegisterSubscription(any(), any());
	
		try {
			eventHandlerService.subscribe(request);
		} catch (final Exception ex) {
			Assert.assertTrue(ex.getMessage().contains("System port is null."));
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testSubscribeInvalidRequestParameterSubscriberSystemPortLessThanOne() {
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		request.getSubscriberSystem().setPort(-1);
		final Set<SystemResponseDTO> authorizedPublishers = getSystemResponseDTOSet(7);
		
		when(eventHandlerDriver.getAuthorizedPublishers(any())).thenReturn(authorizedPublishers);
		doNothing().when(eventHandlerDBService).forceRegisterSubscription(any(), any());
	
		try {
			eventHandlerService.subscribe(request);
		} catch (final Exception ex) {
			Assert.assertTrue(ex.getMessage().contains("System port is less than one."));
			throw ex;
		}
	}
	
	//=================================================================================================
	// Tests of unsubscribe
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testUnsubscribeOK() {
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		doNothing().when(eventHandlerDBService).deleteSubscription(any(), any());
		
		eventHandlerService.unsubscribe(request.getEventType(), request.getSubscriberSystem().getSystemName(), request.getSubscriberSystem().getAddress(), request.getSubscriberSystem().getPort());
		
		verify(eventHandlerDBService, times(1)).deleteSubscription(any(), any());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testUnsubscribeInvalidRequestParameterEventTypeNull() {
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		request.setEventType(null);

		doNothing().when(eventHandlerDBService).deleteSubscription(any(), any());
		
		try {
			eventHandlerService.unsubscribe(request.getEventType(), request.getSubscriberSystem().getSystemName(), request.getSubscriberSystem().getAddress(), request.getSubscriberSystem().getPort());
		} catch (final Exception ex) {
			Assert.assertTrue(ex.getMessage().contains("EventType is null or blank."));
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testUnsubscribeInvalidRequestParameterEventTypeEmpty() {
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		request.setEventType("   ");

		doNothing().when(eventHandlerDBService).deleteSubscription(any(), any());
		
		try {
			eventHandlerService.unsubscribe(request.getEventType(), request.getSubscriberSystem().getSystemName(), request.getSubscriberSystem().getAddress(), request.getSubscriberSystem().getPort());
		} catch (final Exception ex) {
			Assert.assertTrue(ex.getMessage().contains("EventType is null or blank."));
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testUnsubscribeInvalidRequestParameterSubscriberNameNull() {
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		request.getSubscriberSystem().setSystemName(null);

		doNothing().when(eventHandlerDBService).deleteSubscription(any(), any());
		
		try {
			eventHandlerService.unsubscribe(request.getEventType(), request.getSubscriberSystem().getSystemName(), request.getSubscriberSystem().getAddress(), request.getSubscriberSystem().getPort());
		} catch (final Exception ex) {
			Assert.assertTrue(ex.getMessage().contains("SubscriberName is null or blank."));
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testUnsubscribeInvalidRequestParameterSubscriberNameEmpty() {
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		request.getSubscriberSystem().setSystemName("    ");

		doNothing().when(eventHandlerDBService).deleteSubscription(any(), any());
		
		try {
			eventHandlerService.unsubscribe(request.getEventType(), request.getSubscriberSystem().getSystemName(), request.getSubscriberSystem().getAddress(), request.getSubscriberSystem().getPort());
		} catch (final Exception ex) {
			Assert.assertTrue(ex.getMessage().contains("SubscriberName is null or blank."));
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testUnsubscribeInvalidRequestParameterSubscriberAddressNull() {
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		request.getSubscriberSystem().setAddress(null);

		doNothing().when(eventHandlerDBService).deleteSubscription(any(), any());
		
		try {
			eventHandlerService.unsubscribe(request.getEventType(), request.getSubscriberSystem().getSystemName(), request.getSubscriberSystem().getAddress(), request.getSubscriberSystem().getPort());
		} catch (final Exception ex) {
			Assert.assertTrue(ex.getMessage().contains("SubscriberAddress is null or blank."));
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testUnsubscribeInvalidRequestParameterSubscriberAddressEmpty() {
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		request.getSubscriberSystem().setAddress("    ");

		doNothing().when(eventHandlerDBService).deleteSubscription(any(), any());
		
		try {
			eventHandlerService.unsubscribe(request.getEventType(), request.getSubscriberSystem().getSystemName(), request.getSubscriberSystem().getAddress(), request.getSubscriberSystem().getPort());
		} catch (final Exception ex) {
			Assert.assertTrue(ex.getMessage().contains("SubscriberAddress is null or blank."));
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testUnsubscribeInvalidRequestParameterSubscriberPort() {
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		request.getSubscriberSystem().setPort(-1);

		doNothing().when(eventHandlerDBService).deleteSubscription(any(), any());
		
		try {
			eventHandlerService.unsubscribe(request.getEventType(), request.getSubscriberSystem().getSystemName(), request.getSubscriberSystem().getAddress(), request.getSubscriberSystem().getPort());
		} catch (final Exception ex) {
			Assert.assertTrue(ex.getMessage().contains("System port is less than one."));
			throw ex;
		}
	}
	
	//=================================================================================================
	// Tests of publishResponse
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testPublishResponseOK() {
		ReflectionTestUtils.setField(eventHandlerService, "timeStampTolerance", 120);
		
		final Set<Subscription> involvedSubscriptions = getSubscriptionSet(7);
		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();
		
		when(eventHandlerDBService.getInvolvedSubscriptions(any())).thenReturn(involvedSubscriptions);
		doNothing().when(eventHandlerDriver).publishEvent(any(),any());
		when(metadataFilter.doFiltering(any())).thenReturn(true);
		eventHandlerService.publishResponse(request);
		
		verify(eventHandlerDBService, times(1)).getInvolvedSubscriptions(any());
		verify(eventHandlerDriver, times(1)).publishEvent(any(), any());
		verify(metadataFilter, never()).doFiltering(any());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testPublishResponseInvalidRequestParameterRequestNull() {
		ReflectionTestUtils.setField(eventHandlerService, "timeStampTolerance", 120);
		
		final Set<Subscription> involvedSubscriptions = getSubscriptionSet(7);
		final EventPublishRequestDTO request = null;
		
		when(eventHandlerDBService.getInvolvedSubscriptions(any())).thenReturn(involvedSubscriptions);
		doNothing().when(eventHandlerDriver).publishEvent(any(),any());
		when(metadataFilter.doFiltering(any())).thenReturn(true);
		
		try {
			eventHandlerService.publishResponse(request);
		} catch (final Exception ex) {
			Assert.assertTrue(ex.getMessage().contains("EventPublishRequestDTO is null."));
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testPublishResponseInvalidRequestParameterEventTypeNull() {
		ReflectionTestUtils.setField(eventHandlerService, "timeStampTolerance", 120);
		
		final Set<Subscription> involvedSubscriptions = getSubscriptionSet(7);
		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();
		request.setEventType(null);
		
		when(eventHandlerDBService.getInvolvedSubscriptions(any())).thenReturn(involvedSubscriptions);
		doNothing().when(eventHandlerDriver).publishEvent(any(),any());
		when(metadataFilter.doFiltering(any())).thenReturn(true);
		
		try {
			eventHandlerService.publishResponse(request);
		} catch (final Exception ex) {
			Assert.assertTrue(ex.getMessage().contains("EventType is null or blank."));
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testPublishResponseInvalidRequestParameterEventTypeEmpty() {
		ReflectionTestUtils.setField(eventHandlerService, "timeStampTolerance", 120);

		final Set<Subscription> involvedSubscriptions = getSubscriptionSet(7);
		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();
		request.setEventType("   ");
		
		when(eventHandlerDBService.getInvolvedSubscriptions(any())).thenReturn(involvedSubscriptions);
		doNothing().when(eventHandlerDriver).publishEvent(any(),any());
		when(metadataFilter.doFiltering(any())).thenReturn(true);
		
		try {
			eventHandlerService.publishResponse(request);
		} catch (final Exception ex) {
			Assert.assertTrue(ex.getMessage().contains("EventType is null or blank."));
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testPublishResponseInvalidRequestParameterPayloadNull() {
		ReflectionTestUtils.setField(eventHandlerService, "timeStampTolerance", 120);
		
		final Set<Subscription> involvedSubscriptions = getSubscriptionSet(7);
		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();
		request.setPayload(null);
		
		when(eventHandlerDBService.getInvolvedSubscriptions(any())).thenReturn(involvedSubscriptions);
		doNothing().when(eventHandlerDriver).publishEvent(any(),any());
		when(metadataFilter.doFiltering(any())).thenReturn(true);
		
		try {
			eventHandlerService.publishResponse(request);
		} catch (final Exception ex) {
			Assert.assertTrue(ex.getMessage().contains("Payload is null or blank."));
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testPublishResponseInvalidRequestParameterPayloadEmpty() {
		ReflectionTestUtils.setField(eventHandlerService, "timeStampTolerance", 120);
		
		final Set<Subscription> involvedSubscriptions = getSubscriptionSet(7);
		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();
		request.setPayload("    ");
		
		when(eventHandlerDBService.getInvolvedSubscriptions(any())).thenReturn(involvedSubscriptions);
		doNothing().when(eventHandlerDriver).publishEvent(any(),any());
		when(metadataFilter.doFiltering(any())).thenReturn(true);
		
		try {
			eventHandlerService.publishResponse(request);
		} catch (final Exception ex) {
			Assert.assertTrue(ex.getMessage().contains("Payload is null or blank."));
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testPublishResponseInvalidRequestParameterTimeStampNull() {
		ReflectionTestUtils.setField(eventHandlerService, "timeStampTolerance", 120);
		
		final Set<Subscription> involvedSubscriptions = getSubscriptionSet(7);
		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();
		request.setTimeStamp(null);
		
		when(eventHandlerDBService.getInvolvedSubscriptions(any())).thenReturn(involvedSubscriptions);
		doNothing().when(eventHandlerDriver).publishEvent(any(),any());
		when(metadataFilter.doFiltering(any())).thenReturn(true);
		
		try {
			eventHandlerService.publishResponse(request);
		} catch (final Exception ex) {
			Assert.assertTrue(ex.getMessage().contains("TimeStamp is null or blank."));
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testPublishResponseInvalidRequestParameterTimeStampEmpty() {
		ReflectionTestUtils.setField(eventHandlerService, "timeStampTolerance", 120);
		
		final Set<Subscription> involvedSubscriptions = getSubscriptionSet(7);
		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();
		request.setTimeStamp("   ");
		
		when(eventHandlerDBService.getInvolvedSubscriptions(any())).thenReturn(involvedSubscriptions);
		doNothing().when(eventHandlerDriver).publishEvent(any(),any());
		when(metadataFilter.doFiltering(any())).thenReturn(true);
		
		try {
			eventHandlerService.publishResponse(request);
		} catch (final Exception ex) {
			Assert.assertTrue( ex.getMessage().contains("TimeStamp is null or blank."));
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testPublishResponseInvalidRequestParameterTimeStampWrongFormat() {
		ReflectionTestUtils.setField(eventHandlerService, "timeStampTolerance", 120);
		
		final Set<Subscription> involvedSubscriptions = getSubscriptionSet(7);
		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();
		request.setTimeStamp("2007.12.03T10:15:30+01:00");
		
		when(eventHandlerDBService.getInvolvedSubscriptions(any())).thenReturn(involvedSubscriptions);
		doNothing().when(eventHandlerDriver).publishEvent(any(),any());
		when(metadataFilter.doFiltering(any())).thenReturn(true);
		
		try {
			eventHandlerService.publishResponse(request);
		} catch (final Exception ex) {
			Assert.assertTrue(ex.getMessage().contains("TimeStamp is not valid."));
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testPublishResponseInvalidRequestParameterTimeStampInFuture() {
		ReflectionTestUtils.setField(eventHandlerService, "timeStampTolerance", 120);
		
		final Set<Subscription> involvedSubscriptions = getSubscriptionSet(7);
		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();
		request.setTimeStamp("3019-09-27T09:40:34Z");
		
		when(eventHandlerDBService.getInvolvedSubscriptions(any())).thenReturn(involvedSubscriptions);
		doNothing().when(eventHandlerDriver).publishEvent(any(),any());
		when(metadataFilter.doFiltering(any())).thenReturn(true);
		
		try {
			eventHandlerService.publishResponse(request);
		} catch (final Exception ex) {
			Assert.assertTrue(ex.getMessage().contains("TimeStamp is further in the future than the tolerated time difference"));
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testPublishResponseInvalidRequestParameterTimeStampInPast() {
		ReflectionTestUtils.setField(eventHandlerService, "timeStampTolerance", 120);
		
		final Set<Subscription> involvedSubscriptions = getSubscriptionSet(7);
		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();
		request.setTimeStamp("1019-09-27T09:40:34Z");
		
		when(eventHandlerDBService.getInvolvedSubscriptions(any())).thenReturn(involvedSubscriptions);
		doNothing().when(eventHandlerDriver).publishEvent(any(),any());
		when(metadataFilter.doFiltering(any())).thenReturn(true);
		
		try {
			eventHandlerService.publishResponse(request);
		} catch (final Exception ex) {
			Assert.assertTrue(ex.getMessage().contains( "TimeStamp is further in the past than the tolerated time difference"));
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testPublishResponseFilterStartDate() {
		ReflectionTestUtils.setField(eventHandlerService, "timeStampTolerance", 120);
		
		final Set<Subscription> involvedSubscriptions = getSubscriptionSetWithStartDateInPast(7);
		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();
		
		final ArgumentCaptor<Set> valueCapture = ArgumentCaptor.forClass(Set.class);
		when(eventHandlerDBService.getInvolvedSubscriptions(any())).thenReturn(involvedSubscriptions);
		doNothing().when(eventHandlerDriver).publishEvent(any(), valueCapture.capture());	
		when(metadataFilter.doFiltering(any())).thenReturn(true);
		
		eventHandlerService.publishResponse(request);
		
		final Set<Subscription> filteredInvolvedSubscriptions = valueCapture.getValue();
		assertEquals(1, filteredInvolvedSubscriptions.size());
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testPublishResponseFilterEndDate() {
		ReflectionTestUtils.setField(eventHandlerService, "timeStampTolerance", 120);
		
		final Set<Subscription> involvedSubscriptions = getSubscriptionSetWithEndDateInPast(7);
		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();
		
		final ArgumentCaptor<Set> valueCapture = ArgumentCaptor.forClass(Set.class);
		when(eventHandlerDBService.getInvolvedSubscriptions(any())).thenReturn(involvedSubscriptions);
		doNothing().when(eventHandlerDriver).publishEvent(any(), valueCapture.capture());	
		when(metadataFilter.doFiltering(any())).thenReturn(true);
		
		eventHandlerService.publishResponse(request);
		
		final Set<Subscription> filterednvolvedSubscriptions = valueCapture.getValue();
		assertEquals(1, filterednvolvedSubscriptions.size());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testPublishResponseMatchMetaDataEventMetaDataNull() {
		ReflectionTestUtils.setField(eventHandlerService, "timeStampTolerance", 120);
		
		final Set<Subscription> involvedSubscriptions = getSubscriptionSetWithMatchMetaData(7);
		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();
		request.setMetaData(null);

		when(eventHandlerDBService.getInvolvedSubscriptions(any())).thenReturn(involvedSubscriptions);
		when(metadataFilter.doFiltering(any())).thenReturn(true);
		
		eventHandlerService.publishResponse(request);
		
		verify(eventHandlerDriver, never()).publishEvent(any(), any());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testPublishResponseMatchMetaDataEventMetaDataEmpty() {
		ReflectionTestUtils.setField(eventHandlerService, "timeStampTolerance", 120);
		
		final Set<Subscription> involvedSubscriptions = getSubscriptionSetWithMatchMetaData(7);
		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();
		request.setMetaData(Map.of());
		
		when(eventHandlerDBService.getInvolvedSubscriptions(any())).thenReturn(involvedSubscriptions);
		when(metadataFilter.doFiltering(any())).thenReturn(true);
		
		eventHandlerService.publishResponse(request);
		
		verify(eventHandlerDriver, never()).publishEvent(any(), any());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testPublishResponseMetaDataNoMatch() {
		ReflectionTestUtils.setField(eventHandlerService, "timeStampTolerance", 120);
		
		final Set<Subscription> involvedSubscriptions = getSubscriptionSetWithMatchMetaData(7);
		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();
		request.setMetaData(Map.of("a", "1"));
		
		when(eventHandlerDBService.getInvolvedSubscriptions(any())).thenReturn(involvedSubscriptions);
		when(metadataFilter.doFiltering(any())).thenReturn(false);
		
		eventHandlerService.publishResponse(request);
		
		verify(eventHandlerDriver, never()).publishEvent(any(), any());
	}
	
	//=================================================================================================
	// Tests of publishSubscriberAuthorizationUpdateResponse
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testPublishSubscriberAuthorizationUpdateResponseOK() {
		final Set<SystemResponseDTO> authorizedPublishers = getSystemResponseDTOSet(7);
		final List<Subscription> involvedSubscriptions = List.copyOf(getSubscriptionSet(7));
		final EventPublishRequestDTO request = getSubscriberAuthorizationUpdateEventPublishRequestDTOForTest();
		
		when(eventHandlerDBService.getInvolvedSubscriptionsBySubscriberSystemId(any())).thenReturn(involvedSubscriptions);
		when(eventHandlerDriver.getAuthorizedPublishers(any())).thenReturn(authorizedPublishers);	
		doNothing().when(eventHandlerDBService).updateSubscriberAuthorization(any(), any());

		eventHandlerService.publishSubscriberAuthorizationUpdateResponse(request);
	
		verify(eventHandlerDBService, times(1)).getInvolvedSubscriptionsBySubscriberSystemId(any());
		verify(eventHandlerDriver, times(1)).getAuthorizedPublishers(any());
		verify(eventHandlerDBService, times(1)).updateSubscriberAuthorization(any(), any());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testPublishSubscriberAuthorizationUpdateResponseInvalidRequestParameterEventTypeNull() {
		final Set<SystemResponseDTO> authorizedPublishers = getSystemResponseDTOSet(7);
		final List<Subscription> involvedSubscriptions = List.copyOf(getSubscriptionSet(7));
		final EventPublishRequestDTO request = getSubscriberAuthorizationUpdateEventPublishRequestDTOForTest();
		request.setEventType(null);
		
		when(eventHandlerDBService.getInvolvedSubscriptionsBySubscriberSystemId(any())).thenReturn(involvedSubscriptions);
		when(eventHandlerDriver.getAuthorizedPublishers(any())).thenReturn(authorizedPublishers);	
		doNothing().when(eventHandlerDBService).updateSubscriberAuthorization(any(), any());

		try {
			eventHandlerService.publishSubscriberAuthorizationUpdateResponse(request);
		} catch (final Exception ex) {
			Assert.assertTrue(ex.getMessage().contains("EventType is null or blank."));
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testPublishSubscriberAuthorizationUpdateResponseInvalidRequestParameterEventTypeEmpty() {
		final Set<SystemResponseDTO> authorizedPublishers = getSystemResponseDTOSet(7);
		final List<Subscription> involvedSubscriptions = List.copyOf(getSubscriptionSet(7));
		final EventPublishRequestDTO request = getSubscriberAuthorizationUpdateEventPublishRequestDTOForTest();
		request.setEventType("    ");
		
		when(eventHandlerDBService.getInvolvedSubscriptionsBySubscriberSystemId(any())).thenReturn(involvedSubscriptions);
		when(eventHandlerDriver.getAuthorizedPublishers(any())).thenReturn(authorizedPublishers);	
		doNothing().when(eventHandlerDBService).updateSubscriberAuthorization(any(), any());

		try {
			eventHandlerService.publishSubscriberAuthorizationUpdateResponse(request);
		} catch (final Exception ex) {
			Assert.assertTrue(ex.getMessage().contains("EventType is null or blank."));
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testPublishSubscriberAuthorizationUpdateResponseInvalidRequestParameterEventTypeNotValid() {
		final Set<SystemResponseDTO> authorizedPublishers = getSystemResponseDTOSet(7);
		final List<Subscription> involvedSubscriptions = List.copyOf(getSubscriptionSet(7));
		final EventPublishRequestDTO request = getSubscriberAuthorizationUpdateEventPublishRequestDTOForTest();
		request.setEventType(CoreCommonConstants.EVENT_TYPE_SUBSCRIBER_AUTH_UPDATE + "-");
		
		when(eventHandlerDBService.getInvolvedSubscriptionsBySubscriberSystemId(any())).thenReturn(involvedSubscriptions);
		when(eventHandlerDriver.getAuthorizedPublishers(any())).thenReturn(authorizedPublishers);	
		doNothing().when(eventHandlerDBService).updateSubscriberAuthorization(any(), any());

		try {
			eventHandlerService.publishSubscriberAuthorizationUpdateResponse(request);
		} catch (final Exception ex) {
			Assert.assertTrue(ex.getMessage().contains("EventType is not valid."));
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testPublishSubscriberAuthorizationUpdateResponseInvalidRequestParameterPayloadNull() {
		final Set<SystemResponseDTO> authorizedPublishers = getSystemResponseDTOSet(7);
		final List<Subscription> involvedSubscriptions = List.copyOf(getSubscriptionSet(7));
		final EventPublishRequestDTO request = getSubscriberAuthorizationUpdateEventPublishRequestDTOForTest();
		request.setPayload(null);		
		
		when(eventHandlerDBService.getInvolvedSubscriptionsBySubscriberSystemId(any())).thenReturn(involvedSubscriptions);
		when(eventHandlerDriver.getAuthorizedPublishers(any())).thenReturn(authorizedPublishers);	
		doNothing().when(eventHandlerDBService).updateSubscriberAuthorization(any(), any());

		try {
			eventHandlerService.publishSubscriberAuthorizationUpdateResponse(request);
		} catch (final Exception ex) {
			Assert.assertTrue(ex.getMessage().contains("Payload is not valid."));
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testPublishSubscriberAuthorizationUpdateResponseInvalidRequestParameterPayloadEmpty() {
		final Set<SystemResponseDTO> authorizedPublishers = getSystemResponseDTOSet(7);
		final List<Subscription> involvedSubscriptions = List.copyOf(getSubscriptionSet(7));
		final EventPublishRequestDTO request = getSubscriberAuthorizationUpdateEventPublishRequestDTOForTest();
		request.setPayload("   ");		
		
		when(eventHandlerDBService.getInvolvedSubscriptionsBySubscriberSystemId(any())).thenReturn(involvedSubscriptions);
		when(eventHandlerDriver.getAuthorizedPublishers(any())).thenReturn(authorizedPublishers);	
		doNothing().when(eventHandlerDBService).updateSubscriberAuthorization(any(), any());

		try {
			eventHandlerService.publishSubscriberAuthorizationUpdateResponse(request);
		} catch (final Exception ex) {
			Assert.assertTrue(ex.getMessage().contains("Payload is not valid."));
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testPublishSubscriberAuthorizationUpdateResponseInvalidRequestParameterPayloadInvalid() {
		final Set<SystemResponseDTO> authorizedPublishers = getSystemResponseDTOSet(7);
		final List<Subscription> involvedSubscriptions = List.copyOf(getSubscriptionSet(7));
		final EventPublishRequestDTO request = getSubscriberAuthorizationUpdateEventPublishRequestDTOForTest();
		request.setPayload("1a");		
		
		when(eventHandlerDBService.getInvolvedSubscriptionsBySubscriberSystemId(any())).thenReturn(involvedSubscriptions);
		when(eventHandlerDriver.getAuthorizedPublishers(any())).thenReturn(authorizedPublishers);	
		doNothing().when(eventHandlerDBService).updateSubscriberAuthorization(any(), any());

		try {
			eventHandlerService.publishSubscriberAuthorizationUpdateResponse(request);
		} catch (final Exception ex) {
			Assert.assertTrue(ex.getMessage().contains("Payload is not valid."));
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testPublishSubscriberAuthorizationUpdateEmptyInvolvedSubscriptions() {
		final List<Subscription> involvedSubscriptions = List.of();
		final EventPublishRequestDTO request = getSubscriberAuthorizationUpdateEventPublishRequestDTOForTest();	
		
		when(eventHandlerDBService.getInvolvedSubscriptionsBySubscriberSystemId(any())).thenReturn(involvedSubscriptions);
		
		eventHandlerService.publishSubscriberAuthorizationUpdateResponse(request);
		
		verify(eventHandlerDriver, never()).getAuthorizedPublishers(any());
		verify(eventHandlerDBService, never()).updateSubscriberAuthorization(any(), any());
	}
	
	//=================================================================================================
	// Tests of updateSubscriptionResponse
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testUpdateSubscriptionResponseOK() {
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		final Set<SystemResponseDTO> authorizedPublishers = getSystemResponseDTOSet(7);
		
		when(eventHandlerDriver.getAuthorizedPublishers(any())).thenReturn(authorizedPublishers);
		when(eventHandlerDBService.updateSubscription(anyLong(), any(), any())).thenReturn(createSubscriptionForDBMock(1, "eventType", "subscriberName"));
		
		final SubscriptionResponseDTO response = eventHandlerService.updateSubscriptionResponse(1L, request);
		
		verify(eventHandlerDriver, times(1)).getAuthorizedPublishers(any());
		verify(eventHandlerDBService, times(1)).updateSubscription(anyLong(), any(), any());
		assertNotNull(response);
	}
	
	//=================================================================================================
	// Tests of updateSubscription
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testUpdateSubscriptionOK() {
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		final Set<SystemResponseDTO> authorizedPublishers = getSystemResponseDTOSet(7);
		
		when(eventHandlerDriver.getAuthorizedPublishers(any())).thenReturn(authorizedPublishers);
		when(eventHandlerDBService.updateSubscription(anyLong(), any(), any())).thenReturn(createSubscriptionForDBMock(1, "eventType", "subscriberName"));
		
		final Subscription response = eventHandlerService.updateSubscription(1L, request);
		
		verify(eventHandlerDriver, times(1)).getAuthorizedPublishers(any());
		verify(eventHandlerDBService, times(1)).updateSubscription(anyLong(), any(), any());
		assertNotNull(response);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testUpdateSubscriptionInvalidRequestParameterRequestNull() {
		final SubscriptionRequestDTO request = null;
		final Set<SystemResponseDTO> authorizedPublishers = getSystemResponseDTOSet(7);
		
		when(eventHandlerDriver.getAuthorizedPublishers(any())).thenReturn(authorizedPublishers);
		when(eventHandlerDBService.updateSubscription(anyLong(), any(), any())).thenReturn(createSubscriptionForDBMock(1, "eventType", "subscriberName"));
		
		try {
			eventHandlerService.updateSubscription(1L, request);
		} catch (final Exception ex) {
			Assert.assertTrue(ex.getMessage().contains("SubscriptionRequestDTO is null."));
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	// skipped other testUpdateSubscriptionInvalidRequestParameter - method using the same checkSubscriptionRequestDTO as tested at testSubcribe
	
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
		
		return  eventTypeFromDB ;		
	}

	//-------------------------------------------------------------------------------------------------	
	private SubscriptionRequestDTO getSubscriptionRequestDTOForTest() {
		return new SubscriptionRequestDTO(
				"eventType", 
				getSystemRequestDTO(), 
				null, // filterMetaData
				"notifyUri", 
				false, // matchMetaData
				null, // startDate
				null, // endDate 
				null); // sources
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
	private SystemResponseDTO getSystemResponseDTO(final String systemName) {
		return DTOConverter.convertSystemToSystemResponseDTO(createSystemForDBMock(systemName));
	}
	
	//-------------------------------------------------------------------------------------------------	
	private Set<SystemResponseDTO> getSystemResponseDTOSet(final int size) {
		final Set<SystemResponseDTO> systemResponseDTOSet = new HashSet<>();
		for (int i = 0; i < size; ++i) {
			systemResponseDTOSet.add(getSystemResponseDTO("systemName" + i));
		}
		
		return systemResponseDTOSet;
	}
	
	//-------------------------------------------------------------------------------------------------		
	private EventPublishRequestDTO getEventPublishRequestDTOForTest() {
		return new EventPublishRequestDTO("eventType", getSystemRequestDTO(), null, "payload", Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now().plusSeconds(1)));
	}
	
	//-------------------------------------------------------------------------------------------------		
	private EventPublishRequestDTO getSubscriberAuthorizationUpdateEventPublishRequestDTOForTest() {
		return new EventPublishRequestDTO(CoreCommonConstants.EVENT_TYPE_SUBSCRIBER_AUTH_UPDATE, getSystemRequestDTO(), null, "1", 
										  Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now().plusSeconds(1)));
	}
	
	//-------------------------------------------------------------------------------------------------		
	private Set<Subscription> getSubscriptionSet(final int size) {
		final Set<Subscription> subscriptionSet = new HashSet<>();
		for (int i = 0; i < size; ++i) {
			subscriptionSet.add(createSubscriptionForDBMock(i + 1, "eventType" + i , "subscriberName" + i));
		}
		
		return subscriptionSet;
	}
	
	//-------------------------------------------------------------------------------------------------		
	private Set<Subscription> getSubscriptionSetWithStartDateInPast(final int size) {
		final Set<Subscription> subscriptionSet = new HashSet<>();
		for (int i = 0; i < size; ++i) {
			final Subscription subscription = createSubscriptionForDBMock(i + 1, "eventType" + i , "subscriberName" + i);
			subscription.setStartDate(ZonedDateTime.now().minusMinutes(5));
			subscriptionSet.add(subscription);
			
		}
		
		final Subscription subscription = createSubscriptionForDBMock(size + 1, "eventType" + size , "subscriberName" + size);
		subscription.setStartDate(ZonedDateTime.now());
		subscriptionSet.add(subscription);
		
		return subscriptionSet;
	}
	
	//-------------------------------------------------------------------------------------------------		
	private Set<Subscription> getSubscriptionSetWithEndDateInPast(final int size) {
		final Set<Subscription> subscriptionSet = new HashSet<>();
		for (int i = 0; i < size; ++i) {
			final Subscription subscription = createSubscriptionForDBMock(i + 1, "eventType" + i , "subscriberName" + i);
			subscription.setEndDate(ZonedDateTime.now().minusMinutes(5));
			subscriptionSet.add(subscription);
		}
		
		final Subscription subscription = createSubscriptionForDBMock(size + 1, "eventType" + size , "subscriberName" + size);
		subscription.setEndDate(ZonedDateTime.now());
		subscriptionSet.add(subscription);
		
		return subscriptionSet;
	}
	
	//-------------------------------------------------------------------------------------------------		
	private Set<Subscription> getSubscriptionSetWithMatchMetaData(final int size) {
		final Set<Subscription> subscriptionSet = new HashSet<>();
		for (int i = 0; i < size; ++i) {
			final Subscription subscription = createSubscriptionForDBMock(i + 1, "eventType" + i , "subscriberName" + i);
			subscription.setMatchMetaData(true);
			subscription.setFilterMetaData(Utilities.map2Text(Map.of("1", "a")));
			subscriptionSet.add(subscription);
		}
		
		return subscriptionSet;
	}
}