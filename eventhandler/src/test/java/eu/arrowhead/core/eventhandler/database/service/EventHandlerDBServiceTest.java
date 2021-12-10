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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
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
import java.util.Optional;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

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
import eu.arrowhead.common.dto.shared.EventPublishRequestDTO;
import eu.arrowhead.common.dto.shared.SubscriptionListResponseDTO;
import eu.arrowhead.common.dto.shared.SubscriptionRequestDTO;
import eu.arrowhead.common.dto.shared.SubscriptionResponseDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.common.dto.shared.SystemResponseDTO;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.common.processor.SpecialNetworkAddressTypeDetector;
import eu.arrowhead.core.eventhandler.service.EventHandlerDriver;
import eu.arrowhead.core.eventhandler.service.EventHandlerService;

@RunWith(SpringRunner.class)
public class EventHandlerDBServiceTest {
	
	//=================================================================================================
	// members
	
	@InjectMocks
	private EventHandlerDBService eventHandlerDBService;
	
	@Mock
	private EventHandlerService eventHandlerService;
	
	@Mock
	private EventHandlerDriver eventHandlerDriver;
	
	@Mock
	private SubscriptionRepository subscriptionRepository;
	
	@Mock
	private SubscriptionPublisherConnectionRepository subscriptionPublisherConnectionRepository;
	
	@Mock
	private EventTypeRepository eventTypeRepository;
	
	@Mock
	private SystemRepository systemRepository;
	
	@Spy
	private SpecialNetworkAddressTypeDetector networkAddressTypeDetector;

	//=================================================================================================
	// methods
	
	//=================================================================================================
	// Tests of getSubscriptionByIdResponse
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetSubscriptionByIdResponseOK() {
		when(subscriptionRepository.findById(anyLong())).thenReturn(Optional.of(createSubscriptionForDBMock(1, "eventType", "subscriberName")));
		
		final SubscriptionResponseDTO response = eventHandlerDBService.getSubscriptionByIdResponse(1L);
		
		verify(subscriptionRepository, times(1)).findById(anyLong());
		assertNotNull(response);
		assertNotNull(response.getSubscriberSystem());
		assertNotNull(response.getEventType());
		assertNotNull(response.getNotifyUri());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGetSubscriptionByIdResponseInvalidParameterId() {
		try {
			eventHandlerDBService.getSubscriptionByIdResponse(-1L);
		} catch (final Exception ex) {
			Assert.assertTrue(ex.getMessage().contains("SubscriberSystemId must be greater than zero."));
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGetSubscriptionByIdResponseInvalidParameterSubscriptionByIdNotInDB() {
		when(subscriptionRepository.findById(anyLong())).thenReturn(Optional.ofNullable(null));
		
		try {
			eventHandlerDBService.getSubscriptionByIdResponse(1L);
		} catch (final Exception ex) {
			Assert.assertTrue(ex.getMessage().contains("not exists" ));
			throw ex;
		}
	}
	
	//=================================================================================================
	//Tests of getSubscriptionById
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetSubscriptionByIdOK() {
		when(subscriptionRepository.findById(anyLong())).thenReturn(Optional.of(createSubscriptionForDBMock(1, "eventType", "subscriberName")));
		
		final Subscription response = eventHandlerDBService.getSubscriptionById(1L);
		
		verify(subscriptionRepository, times(1)).findById(anyLong());
		assertNotNull(response);
		assertNotNull(response.getSubscriberSystem());
		assertNotNull(response.getEventType());
		assertNotNull(response.getNotifyUri());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGetSubscriptionByIdInvalidParameterId() {
		try {
			eventHandlerDBService.getSubscriptionById(-1L);
		} catch (final Exception ex) {
			Assert.assertTrue(ex.getMessage().contains("SubscriberSystemId must be greater than zero."));
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGetSubscriptionByIdInvalidParameterSubscriptionByIdNotInDB() {
		when(subscriptionRepository.findById(anyLong())).thenReturn(Optional.ofNullable(null));
		
		try {
			eventHandlerDBService.getSubscriptionById(1L);
		} catch (final Exception ex) {
			Assert.assertTrue(ex.getMessage().contains("not exists"));
			throw ex;
		}
	}

	//=================================================================================================
	// Tests of getSubscriptionsResponse
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetSubscriptionsResponseOK() {
		final int page = 0;
		final int size = 0;
		final Direction direction = Direction.ASC;
		final String sortField = "id";
		
		final Subscription subscription = createSubscriptionForDBMock(1, "eventType", "subscriberName");
		final Page<Subscription> subscriptionPage = new PageImpl<>(List.of(subscription));
		when(subscriptionRepository.findAll(any(PageRequest.class))).thenReturn(subscriptionPage);
		
		final SubscriptionListResponseDTO response = eventHandlerDBService.getSubscriptionsResponse(page, size, direction, sortField);
		
		verify(subscriptionRepository, times(1)).findAll(any(PageRequest.class));
		assertNotNull(response);
		assertNotNull(response.getData());
		assertFalse(response.getData().isEmpty());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetSubscriptionsResponsePageLessThanZeroOK() {
		final int page = -1;
		final int size = 0;
		final Direction direction = Direction.ASC;
		final String sortField = "id";
		
		final Subscription subscription = createSubscriptionForDBMock(1, "eventType", "subscriberName");
		final Page<Subscription> subscriptionPage = new PageImpl<>(List.of(subscription));
		when(subscriptionRepository.findAll(any(PageRequest.class))).thenReturn(subscriptionPage);
		
		final SubscriptionListResponseDTO response = eventHandlerDBService.getSubscriptionsResponse(page, size, direction, sortField);
		
		verify(subscriptionRepository, times(1)).findAll(any(PageRequest.class));
		assertNotNull(response);
		assertNotNull(response.getData());
		assertFalse(response.getData().isEmpty());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetSubscriptionsResponseSizeLessThanZeroOK() {
		final int page = 0;
		final int size = -1;
		final Direction direction = Direction.ASC;
		final String sortField = "id";
		
		final Subscription subscription = createSubscriptionForDBMock(1, "eventType", "subscriberName");
		final Page<Subscription> subscriptionPage = new PageImpl<>(List.of(subscription));
		when(subscriptionRepository.findAll(any(PageRequest.class))).thenReturn(subscriptionPage);
		
		final SubscriptionListResponseDTO response = eventHandlerDBService.getSubscriptionsResponse(page, size, direction, sortField);
		
		verify(subscriptionRepository, times(1)).findAll(any(PageRequest.class));
		assertNotNull(response);
		assertNotNull(response.getData());
		assertFalse(response.getData().isEmpty());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetSubscriptionsResponseDirectionNullOK() {
		final int page = 0;
		final int size = 0;
		final Direction direction = null;
		final String sortField = "id";
		
		final Subscription subscription = createSubscriptionForDBMock(1, "eventType", "subscriberName");
		final Page<Subscription> subscriptionPage = new PageImpl<>(List.of(subscription));
		when(subscriptionRepository.findAll(any(PageRequest.class))).thenReturn(subscriptionPage);
		
		final SubscriptionListResponseDTO response = eventHandlerDBService.getSubscriptionsResponse(page, size, direction, sortField);
		
		verify(subscriptionRepository, times(1)).findAll(any(PageRequest.class));
		assertNotNull(response);
		assertNotNull(response.getData());
		assertFalse(response.getData().isEmpty());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGetSubscriptionsResponseInvalidParameterSortField() {
		final int page = 0;
		final int size = 0;
		final Direction direction = Direction.ASC;
		final String sortField = "invalidSortField";
		
		try {
			eventHandlerDBService.getSubscriptionsResponse(page, size, direction, sortField);
		} catch (final Exception ex) {
			verify(subscriptionRepository, never()).findAll(any(PageRequest.class));
			Assert.assertTrue(ex.getMessage().contains(" sortable field is not available." ));
			throw ex;
		}
	}
	
	//=================================================================================================
	// Tests of getSubscriptions
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetSubscriptionsOK() {
		final int page = 0;
		final int size = 0;
		final Direction direction = Direction.ASC;
		final String sortField = "id";
		
		final Subscription subscription = createSubscriptionForDBMock(1, "eventType", "subscriberName");
		final Page<Subscription> subscriptionPage = new PageImpl<>(List.of(subscription));
		when(subscriptionRepository.findAll(any(PageRequest.class))).thenReturn(subscriptionPage);
		
		final Page<Subscription> response = eventHandlerDBService.getSubscriptions(page, size, direction, sortField);
		
		verify(subscriptionRepository, times(1)).findAll(any(PageRequest.class));
		assertNotNull(response);
		assertNotNull(response.getContent());
		assertFalse(response.getContent().isEmpty());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetSubscriptionsPageLessThanZeroOK() {
		final int page = -1;
		final int size = 0;
		final Direction direction = Direction.ASC;
		final String sortField = "id";
		
		final Subscription subscription = createSubscriptionForDBMock(1, "eventType", "subscriberName");
		final Page<Subscription> subscriptionPage = new PageImpl<>(List.of(subscription));
		when(subscriptionRepository.findAll(any(PageRequest.class))).thenReturn(subscriptionPage);
		
		final Page<Subscription> response = eventHandlerDBService.getSubscriptions(page, size, direction, sortField);
		
		verify(subscriptionRepository, times(1)).findAll(any(PageRequest.class));
		assertNotNull(response);
		assertNotNull(response.getContent());
		assertFalse(response.getContent().isEmpty());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetSubscriptionsSizeLessThanZeroOK() {
		final int page = 0;
		final int size = -1;
		final Direction direction = Direction.ASC;
		final String sortField = "id";
		
		final Subscription subscription = createSubscriptionForDBMock(1, "eventType", "subscriberName");
		final Page<Subscription> subscriptionPage = new PageImpl<>(List.of(subscription));
		when(subscriptionRepository.findAll(any(PageRequest.class))).thenReturn(subscriptionPage);
		
		final Page<Subscription> response = eventHandlerDBService.getSubscriptions(page, size, direction, sortField);
		
		verify(subscriptionRepository, times(1)).findAll(any(PageRequest.class));
		assertNotNull(response);
		assertNotNull(response.getContent());
		assertFalse(response.getContent().isEmpty());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetSubscriptionsDirectionNullOK() {
		final int page = 0;
		final int size = 0;
		final Direction direction = null;
		final String sortField = "id";
		
		final Subscription subscription = createSubscriptionForDBMock(1, "eventType", "subscriberName");
		final Page<Subscription> subscriptionPage = new PageImpl<>(List.of(subscription));
		when(subscriptionRepository.findAll(any(PageRequest.class))).thenReturn(subscriptionPage);
		
		final Page<Subscription> response = eventHandlerDBService.getSubscriptions(page, size, direction, sortField);
		
		verify(subscriptionRepository, times(1)).findAll(any(PageRequest.class));
		assertNotNull(response);
		assertNotNull(response.getContent());
		assertFalse(response.getContent().isEmpty());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGetSubscriptionsInvalidParameterSortField() {
		final int page = 0;
		final int size = 0;
		final Direction direction = null;
		final String sortField = "invalidSortField";
		
		final Subscription subscription = createSubscriptionForDBMock(1, "eventType", "subscriberName");
		final Page<Subscription> subscriptionPage = new PageImpl<>(List.of(subscription));
		
		when(subscriptionRepository.findAll(any(PageRequest.class))).thenReturn(subscriptionPage);
		
		try {
			eventHandlerDBService.getSubscriptions(page, size, direction, sortField);
		} catch (final Exception ex) {
			verify(subscriptionRepository, never()).findAll(any(PageRequest.class));
			Assert.assertTrue(ex.getMessage().contains(" sortable field is not available."));
			throw ex;
		}
	}
	
	//=================================================================================================
	// Tests of getSubscriptionBySubscriptionRequestDTO
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetSubscriptionBySubscriptionRequestDTOWithEventTypeInDBOK() {
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		final System system = createSystemForDBMock("systemName");
		final EventType eventType = createEventTypeForDBMock("eventType");
		final Subscription subscription = createSubscriptionForDBMock(1, "eventType", "subscriberName");

		when(systemRepository.findBySystemNameAndAddressAndPort(any(), any(), anyInt())).thenReturn(Optional.of(system));
		when(eventTypeRepository.findByEventTypeName(any())).thenReturn(Optional.ofNullable(eventType));
		when(eventTypeRepository.saveAndFlush(any())).thenReturn(eventType);
		when(subscriptionRepository.findByEventTypeAndSubscriberSystem(any(), any())).thenReturn(Optional.of(subscription));
		
		final Subscription response = eventHandlerDBService.getSubscriptionBySubscriptionRequestDTO(request);
		
		verify(systemRepository, times(1)).findBySystemNameAndAddressAndPort(any(), any(), anyInt());
		verify(eventTypeRepository, times(1)).findByEventTypeName(any());
		verify(eventTypeRepository, times(0)).saveAndFlush(any());
		verify(subscriptionRepository, times(1)).findByEventTypeAndSubscriberSystem(any(), any());
		assertNotNull(response);
		assertNotNull(response.getSubscriberSystem());
		assertNotNull(response.getEventType());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetSubscriptionBySubscriptionRequestDTOWithEventTypeNotInDBOK() {
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		final System system = createSystemForDBMock("systemName");
		final EventType eventType = createEventTypeForDBMock("eventType");
		final Subscription subscription = createSubscriptionForDBMock(1, "eventType", "subscriberName");

		when(systemRepository.findBySystemNameAndAddressAndPort(any(), any(), anyInt())).thenReturn(Optional.of(system));
		when(eventTypeRepository.findByEventTypeName(any())).thenReturn(Optional.ofNullable(null));
		when(eventTypeRepository.saveAndFlush(any())).thenReturn(eventType);
		when(subscriptionRepository.findByEventTypeAndSubscriberSystem(any(), any())).thenReturn(Optional.of(subscription));
		
		final Subscription response = eventHandlerDBService.getSubscriptionBySubscriptionRequestDTO(request);
		
		verify(systemRepository, times(1)).findBySystemNameAndAddressAndPort(any(), any(), anyInt());
		verify(eventTypeRepository, times(1)).findByEventTypeName(any());
		verify(eventTypeRepository, times(1)).saveAndFlush(any());
		verify(subscriptionRepository,times(1)).findByEventTypeAndSubscriberSystem(any(), any());
		assertNotNull(response);
		assertNotNull(response.getSubscriberSystem());
		assertNotNull(response.getEventType());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class )
	public void testGetSubscriptionBySubscriptionRequestDTOInvalidParameterRequestNull() {
		final SubscriptionRequestDTO request = null;
		final System system = createSystemForDBMock("systemName");
		final EventType eventType = createEventTypeForDBMock("eventType");
		final Subscription subscription = createSubscriptionForDBMock(1, "eventType", "subscriberName");

		when(systemRepository.findBySystemNameAndAddressAndPort(any(), any(), anyInt())).thenReturn(Optional.of(system));
		when(eventTypeRepository.findByEventTypeName(any())).thenReturn(Optional.ofNullable(null));
		when(eventTypeRepository.saveAndFlush(any())).thenReturn(eventType);
		when(subscriptionRepository.findByEventTypeAndSubscriberSystem(any(), any())).thenReturn(Optional.of(subscription));
		
		try {
			eventHandlerDBService.getSubscriptionBySubscriptionRequestDTO(request);
		} catch (final Exception ex) {
			verify(systemRepository, never()).findBySystemNameAndAddressAndPort(any(), any(), anyInt());
			verify(eventTypeRepository, never()).findByEventTypeName(any());
			verify(eventTypeRepository, never()).saveAndFlush(any());
			verify(subscriptionRepository, never()).findByEventTypeAndSubscriberSystem(any(), any());			
			Assert.assertTrue(ex.getMessage().contains("SubscriptionRequestDTO is null"));
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class )
	public void testGetSubscriptionBySubscriptionRequestDTOInvalidParameterSubscriberSystemNull() {
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		request.setSubscriberSystem(null);
		
		final System system = createSystemForDBMock("systemName");
		final EventType eventType = createEventTypeForDBMock("eventType");
		final Subscription subscription = createSubscriptionForDBMock(1, "eventType", "subscriberName");

		when(systemRepository.findBySystemNameAndAddressAndPort(any(), any(), anyInt())).thenReturn(Optional.of(system));
		when(eventTypeRepository.findByEventTypeName(any())).thenReturn(Optional.ofNullable(null));
		when(eventTypeRepository.saveAndFlush(any())).thenReturn(eventType);
		when(subscriptionRepository.findByEventTypeAndSubscriberSystem(any(), any())).thenReturn(Optional.of(subscription));
		
		try {
			eventHandlerDBService.getSubscriptionBySubscriptionRequestDTO(request);
		} catch (final Exception ex) {
			verify(systemRepository, never()).findBySystemNameAndAddressAndPort(any(), any(), anyInt());
			verify(eventTypeRepository, never()).findByEventTypeName(any());
			verify(eventTypeRepository, never()).saveAndFlush(any());
			verify(subscriptionRepository, never()).findByEventTypeAndSubscriberSystem(any(), any());			
			Assert.assertTrue(ex.getMessage().contains("SystemRequestDTO is null"));
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class )
	public void testGetSubscriptionBySubscriptionRequestDTOInvalidParameterSubscriberSystemSystemNameNull() {
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		request.getSubscriberSystem().setSystemName(null);
		
		final System system = createSystemForDBMock("systemName");
		final EventType eventType = createEventTypeForDBMock("eventType");
		final Subscription subscription = createSubscriptionForDBMock(1, "eventType", "subscriberName");

		when(systemRepository.findBySystemNameAndAddressAndPort(any(), any(), anyInt())).thenReturn(Optional.of(system));
		when(eventTypeRepository.findByEventTypeName(any())).thenReturn(Optional.ofNullable(null));
		when(eventTypeRepository.saveAndFlush(any())).thenReturn(eventType);
		when(subscriptionRepository.findByEventTypeAndSubscriberSystem(any(), any())).thenReturn(Optional.of(subscription));
		
		try {
			eventHandlerDBService.getSubscriptionBySubscriptionRequestDTO(request);
		} catch (final Exception ex) {
			verify(systemRepository, never()).findBySystemNameAndAddressAndPort(any(), any(), anyInt());
			verify(eventTypeRepository, never()).findByEventTypeName(any());
			verify(eventTypeRepository, never()).saveAndFlush(any());
			verify(subscriptionRepository, never()).findByEventTypeAndSubscriberSystem(any(), any());			
			Assert.assertTrue(ex.getMessage().contains("System name is empty or null"));
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class )
	public void testGetSubscriptionBySubscriptionRequestDTOInvalidParameterSubscriberSystemSystemNameEmpty() {
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		request.getSubscriberSystem().setSystemName("   ");
		
		final System system = createSystemForDBMock("systemName");
		final EventType eventType = createEventTypeForDBMock("eventType");
		final Subscription subscription = createSubscriptionForDBMock(1, "eventType", "subscriberName");

		when(systemRepository.findBySystemNameAndAddressAndPort(any(), any(), anyInt())).thenReturn(Optional.of(system));
		when(eventTypeRepository.findByEventTypeName(any())).thenReturn(Optional.ofNullable(null));
		when(eventTypeRepository.saveAndFlush(any())).thenReturn(eventType);
		when(subscriptionRepository.findByEventTypeAndSubscriberSystem(any(), any())).thenReturn(Optional.of(subscription));
		
		try {
			eventHandlerDBService.getSubscriptionBySubscriptionRequestDTO(request);
		} catch (final Exception ex) {
			verify(systemRepository, never()).findBySystemNameAndAddressAndPort(any(), any(), anyInt());
			verify(eventTypeRepository, never()).findByEventTypeName(any());
			verify(eventTypeRepository, never()).saveAndFlush(any());
			verify(subscriptionRepository, never()).findByEventTypeAndSubscriberSystem(any(), any());			
			Assert.assertTrue(ex.getMessage().contains("System name is empty or null"));
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class )
	public void testGetSubscriptionBySubscriptionRequestDTOInvalidParameterSubscriberSystemSystemAddressNull() {
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		request.getSubscriberSystem().setAddress(null);
		
		final System system = createSystemForDBMock("systemName");
		final EventType eventType = createEventTypeForDBMock("eventType");
		final Subscription subscription = createSubscriptionForDBMock(1, "eventType", "subscriberName");

		when(systemRepository.findBySystemNameAndAddressAndPort(any(), any(), anyInt())).thenReturn(Optional.of(system));
		when(eventTypeRepository.findByEventTypeName(any())).thenReturn(Optional.ofNullable(null));
		when(eventTypeRepository.saveAndFlush(any())).thenReturn(eventType);
		when(subscriptionRepository.findByEventTypeAndSubscriberSystem(any(), any())).thenReturn(Optional.of(subscription));
		
		try {
			eventHandlerDBService.getSubscriptionBySubscriptionRequestDTO(request);
		} catch (final Exception ex) {
			verify(systemRepository, never()).findBySystemNameAndAddressAndPort(any(), any(), anyInt());
			verify(eventTypeRepository, never()).findByEventTypeName(any());
			verify(eventTypeRepository, never()).saveAndFlush(any());
			verify(subscriptionRepository, never()).findByEventTypeAndSubscriberSystem(any(), any());			
			Assert.assertTrue(ex.getMessage().contains("System address is empty or null"));
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class )
	public void testGetSubscriptionBySubscriptionRequestDTOInvalidParameterSubscriberSystemSystemAddressEmpty() {
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		request.getSubscriberSystem().setAddress("   ");
		
		final System system = createSystemForDBMock("systemName");
		final EventType eventType = createEventTypeForDBMock("eventType");
		final Subscription subscription = createSubscriptionForDBMock(1, "eventType", "subscriberName");

		when(systemRepository.findBySystemNameAndAddressAndPort(any(), any(), anyInt())).thenReturn(Optional.of(system));
		when(eventTypeRepository.findByEventTypeName(any())).thenReturn(Optional.ofNullable(null));
		when(eventTypeRepository.saveAndFlush(any())).thenReturn(eventType);
		when(subscriptionRepository.findByEventTypeAndSubscriberSystem(any(), any())).thenReturn(Optional.of(subscription));
		
		try {
			eventHandlerDBService.getSubscriptionBySubscriptionRequestDTO(request);
		} catch (final Exception ex) {
			verify(systemRepository, never()).findBySystemNameAndAddressAndPort(any(), any(), anyInt());
			verify(eventTypeRepository, never()).findByEventTypeName(any());
			verify(eventTypeRepository, never()).saveAndFlush(any());
			verify(subscriptionRepository, never()).findByEventTypeAndSubscriberSystem(any(), any());			
			Assert.assertTrue(ex.getMessage().contains("System address is empty or null"));
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class )
	public void testGetSubscriptionBySubscriptionRequestDTOInvalidParameterSubscriberSystemSystemPortNull() {
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		request.getSubscriberSystem().setPort(null);
		
		final System system = createSystemForDBMock("systemName");
		final EventType eventType = createEventTypeForDBMock("eventType");
		final Subscription subscription = createSubscriptionForDBMock(1, "eventType", "subscriberName");

		when(systemRepository.findBySystemNameAndAddressAndPort(any(), any(), anyInt())).thenReturn(Optional.of(system));
		when(eventTypeRepository.findByEventTypeName(any())).thenReturn(Optional.ofNullable(null));
		when(eventTypeRepository.saveAndFlush(any())).thenReturn(eventType);
		when(subscriptionRepository.findByEventTypeAndSubscriberSystem(any(), any())).thenReturn(Optional.of(subscription));
		
		try {
			eventHandlerDBService.getSubscriptionBySubscriptionRequestDTO(request);
		} catch (final Exception ex) {
			verify(systemRepository, never()).findBySystemNameAndAddressAndPort(any(), any(), anyInt());
			verify(eventTypeRepository, never()).findByEventTypeName(any());
			verify(eventTypeRepository, never()).saveAndFlush(any());
			verify(subscriptionRepository, never()).findByEventTypeAndSubscriberSystem(any(), any());			
			Assert.assertTrue(ex.getMessage().contains("System port is null"));
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class )
	public void testGetSubscriptionBySubscriptionRequestDTOInvalidParameterSubscriberSystemNotInDB() {
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		final EventType eventType = createEventTypeForDBMock("eventType");
		final Subscription subscription = createSubscriptionForDBMock(1, "eventType", "subscriberName");

		when(systemRepository.findBySystemNameAndAddressAndPort(any(), any(), anyInt())).thenReturn(Optional.ofNullable(null));
		when(eventTypeRepository.findByEventTypeName(any())).thenReturn(Optional.ofNullable(null));
		when(eventTypeRepository.saveAndFlush(any())).thenReturn(eventType);
		when(subscriptionRepository.findByEventTypeAndSubscriberSystem(any(), any())).thenReturn(Optional.of(subscription));
		
		try {
			eventHandlerDBService.getSubscriptionBySubscriptionRequestDTO(request);
		} catch (final Exception ex) {
			verify(systemRepository, times(1)).findBySystemNameAndAddressAndPort(any(), any(), anyInt());
			verify(eventTypeRepository, never()).findByEventTypeName(any());
			verify(eventTypeRepository, never()).saveAndFlush(any());
			verify(subscriptionRepository, never()).findByEventTypeAndSubscriberSystem(any(), any());			
			Assert.assertTrue(ex.getMessage().contains(" is not available in database"));
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class )
	public void testGetSubscriptionBySubscriptionRequestDTOInvalidParameterEventTypeNull() {
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		request.setEventType(null);
		
		final System system = createSystemForDBMock("systemName");
		final EventType eventType = createEventTypeForDBMock("eventType");
		final Subscription subscription = createSubscriptionForDBMock(1, "eventType", "subscriberName");

		when(systemRepository.findBySystemNameAndAddressAndPort(any(), any(), anyInt())).thenReturn(Optional.of(system));
		when(eventTypeRepository.findByEventTypeName(any())).thenReturn(Optional.ofNullable(null));
		when(eventTypeRepository.saveAndFlush(any())).thenReturn(eventType);
		when(subscriptionRepository.findByEventTypeAndSubscriberSystem(any(), any())).thenReturn(Optional.of(subscription));
		
		try {
			eventHandlerDBService.getSubscriptionBySubscriptionRequestDTO(request);
		} catch (final Exception ex) {
			verify(systemRepository, times(1)).findBySystemNameAndAddressAndPort(any(), any(), anyInt());
			verify(eventTypeRepository, never()).findByEventTypeName(any());
			verify(eventTypeRepository, never()).saveAndFlush(any());
			verify(subscriptionRepository, never()).findByEventTypeAndSubscriberSystem(any(), any());			
			Assert.assertTrue(ex.getMessage().contains("EventType is empty or null"));
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class )
	public void testGetSubscriptionBySubscriptionRequestDTOInvalidParameterEventTypeEmpty() {
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		request.setEventType("   ");
		
		final System system = createSystemForDBMock("systemName");
		final EventType eventType = createEventTypeForDBMock("eventType");
		final Subscription subscription = createSubscriptionForDBMock(1, "eventType", "subscriberName");

		when(systemRepository.findBySystemNameAndAddressAndPort(any(), any(), anyInt())).thenReturn(Optional.of(system));
		when(eventTypeRepository.findByEventTypeName(any())).thenReturn(Optional.ofNullable(null));
		when(eventTypeRepository.saveAndFlush(any())).thenReturn(eventType);
		when(subscriptionRepository.findByEventTypeAndSubscriberSystem(any(), any())).thenReturn(Optional.of(subscription));
		
		try {
			eventHandlerDBService.getSubscriptionBySubscriptionRequestDTO(request);
		} catch (final Exception ex) {
			verify(systemRepository, times(1)).findBySystemNameAndAddressAndPort(any(), any(), anyInt());
			verify(eventTypeRepository, never()).findByEventTypeName(any());
			verify(eventTypeRepository, never()).saveAndFlush(any());
			verify(subscriptionRepository, never()).findByEventTypeAndSubscriberSystem(any(), any());			
			Assert.assertTrue(ex.getMessage().contains("EventType is empty or null"));
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class )
	public void testGetSubscriptionBySubscriptionRequestDTOInvalidParameterNotifyUriNull() {
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		request.setNotifyUri(null);
		
		final System system = createSystemForDBMock("systemName");
		final EventType eventType = createEventTypeForDBMock("eventType");
		final Subscription subscription = createSubscriptionForDBMock(1, "eventType", "subscriberName");

		when(systemRepository.findBySystemNameAndAddressAndPort(any(), any(), anyInt())).thenReturn(Optional.of(system));
		when(eventTypeRepository.findByEventTypeName(any())).thenReturn(Optional.ofNullable(null));
		when(eventTypeRepository.saveAndFlush(any())).thenReturn(eventType);
		when(subscriptionRepository.findByEventTypeAndSubscriberSystem(any(), any())).thenReturn(Optional.of(subscription));
		
		try {
			eventHandlerDBService.getSubscriptionBySubscriptionRequestDTO(request);
		} catch (final Exception ex) {
			verify(systemRepository, times(1)).findBySystemNameAndAddressAndPort(any(), any(), anyInt());
			verify(eventTypeRepository, times(1)).findByEventTypeName(any());
			verify(eventTypeRepository, times(1)).saveAndFlush(any());
			verify(subscriptionRepository, never()).findByEventTypeAndSubscriberSystem(any(), any());			
			Assert.assertTrue(ex.getMessage().contains("NotifyUri is empty or null"));
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class )
	public void testGetSubscriptionBySubscriptionRequestDTOInvalidParameterNotifyUriEmpty() {
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		request.setNotifyUri("   ");
		
		final System system = createSystemForDBMock("systemName");
		final EventType eventType = createEventTypeForDBMock("eventType");
		final Subscription subscription = createSubscriptionForDBMock(1, "eventType", "subscriberName");

		when(systemRepository.findBySystemNameAndAddressAndPort(any(), any(), anyInt())).thenReturn(Optional.of(system));
		when(eventTypeRepository.findByEventTypeName(any())).thenReturn(Optional.ofNullable(null));
		when(eventTypeRepository.saveAndFlush(any())).thenReturn(eventType);
		when(subscriptionRepository.findByEventTypeAndSubscriberSystem(any(), any())).thenReturn(Optional.of(subscription));
		
		try {
			eventHandlerDBService.getSubscriptionBySubscriptionRequestDTO(request);
		} catch (final Exception ex) {
			verify(systemRepository, times(1)).findBySystemNameAndAddressAndPort(any(), any(), anyInt());
			verify(eventTypeRepository, times(1)).findByEventTypeName(any());
			verify(eventTypeRepository, times(1)).saveAndFlush(any());
			verify(subscriptionRepository, never()).findByEventTypeAndSubscriberSystem(any(), any());			
			Assert.assertTrue(ex.getMessage().contains("NotifyUri is empty or null"));
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class )
	public void testGetSubscriptionBySubscriptionRequestDTOInvalidParameterWrongStartDateFormat() {
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		request.setStartDate(".....");
		
		final System system = createSystemForDBMock("systemName");
		final EventType eventType = createEventTypeForDBMock("eventType");
		final Subscription subscription = createSubscriptionForDBMock(1, "eventType", "subscriberName");

		when(systemRepository.findBySystemNameAndAddressAndPort(any(), any(), anyInt())).thenReturn(Optional.of(system));
		when(eventTypeRepository.findByEventTypeName(any())).thenReturn(Optional.ofNullable(null));
		when(eventTypeRepository.saveAndFlush(any())).thenReturn(eventType);
		when(subscriptionRepository.findByEventTypeAndSubscriberSystem(any(), any())).thenReturn(Optional.of(subscription));
		
		try {
			eventHandlerDBService.getSubscriptionBySubscriptionRequestDTO(request);
		} catch (final Exception ex) {
			verify(systemRepository, times(1)).findBySystemNameAndAddressAndPort(any(), any(), anyInt());
			verify(eventTypeRepository, times(1)).findByEventTypeName(any());
			verify(eventTypeRepository, times(1)).saveAndFlush(any());
			verify(subscriptionRepository, never()).findByEventTypeAndSubscriberSystem(any(), any());			
			Assert.assertTrue(ex.getMessage().contains("StartDate is not valid."));
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class )
	public void testGetSubscriptionBySubscriptionRequestDTOInvalidParameterWrongEndDateFormat() {
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		request.setEndDate(".....");
		
		final System system = createSystemForDBMock("systemName");
		final EventType eventType = createEventTypeForDBMock("eventType");
		final Subscription subscription = createSubscriptionForDBMock(1, "eventType", "subscriberName");

		when(systemRepository.findBySystemNameAndAddressAndPort(any(), any(), anyInt())).thenReturn(Optional.of(system));
		when(eventTypeRepository.findByEventTypeName(any())).thenReturn(Optional.ofNullable(null));
		when(eventTypeRepository.saveAndFlush(any())).thenReturn(eventType);
		when(subscriptionRepository.findByEventTypeAndSubscriberSystem(any(), any())).thenReturn(Optional.of(subscription));
		
		try {
			eventHandlerDBService.getSubscriptionBySubscriptionRequestDTO(request);
		} catch (final Exception ex) {
			verify(systemRepository, times(1)).findBySystemNameAndAddressAndPort(any(), any(), anyInt());
			verify(eventTypeRepository, times(1)).findByEventTypeName(any());
			verify(eventTypeRepository, times(1)).saveAndFlush(any());
			verify(subscriptionRepository, never()).findByEventTypeAndSubscriberSystem(any(), any());			
			Assert.assertTrue(ex.getMessage().contains("EndDate is not valid."));
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class )
	public void testGetSubscriptionBySubscriptionRequestDTOInvalidParameterStartDateInPast() {
		ReflectionTestUtils.setField(eventHandlerService, "timeStampTolerance", 120);
		
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		request.setStartDate("1019-09-27T09:40:34Z");
		
		final System system = createSystemForDBMock("systemName");
		final EventType eventType = createEventTypeForDBMock("eventType");
		final Subscription subscription = createSubscriptionForDBMock(1, "eventType", "subscriberName");

		when(systemRepository.findBySystemNameAndAddressAndPort(any(), any(), anyInt())).thenReturn(Optional.of(system));
		when(eventTypeRepository.findByEventTypeName(any())).thenReturn(Optional.ofNullable(null));
		when(eventTypeRepository.saveAndFlush(any())).thenReturn(eventType);
		when(subscriptionRepository.findByEventTypeAndSubscriberSystem(any(), any())).thenReturn(Optional.of(subscription));
		
		try {
			eventHandlerDBService.getSubscriptionBySubscriptionRequestDTO(request);
		} catch (final Exception ex) {
			verify(systemRepository, times(1)).findBySystemNameAndAddressAndPort(any(), any(), anyInt());
			verify(eventTypeRepository, times(1)).findByEventTypeName(any());
			verify(eventTypeRepository, times(1)).saveAndFlush(any());
			verify(subscriptionRepository, never()).findByEventTypeAndSubscriberSystem(any(), any());			
			Assert.assertTrue(ex.getMessage().contains("Start Date is further in the past than the tolerated time difference"));
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class )
	public void testGetSubscriptionBySubscriptionRequestDTOInvalidParameterEndDateInPast() {
		ReflectionTestUtils.setField(eventHandlerService, "timeStampTolerance", 120);
		
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		request.setEndDate("1019-09-27T09:40:34Z");
		
		final System system = createSystemForDBMock("systemName");
		final EventType eventType = createEventTypeForDBMock("eventType");
		final Subscription subscription = createSubscriptionForDBMock(1, "eventType", "subscriberName");

		when(systemRepository.findBySystemNameAndAddressAndPort(any(), any(), anyInt())).thenReturn(Optional.of(system));
		when(eventTypeRepository.findByEventTypeName(any())).thenReturn(Optional.ofNullable(null));
		when(eventTypeRepository.saveAndFlush(any())).thenReturn(eventType);
		when(subscriptionRepository.findByEventTypeAndSubscriberSystem(any(), any())).thenReturn(Optional.of(subscription));
		
		try {
			eventHandlerDBService.getSubscriptionBySubscriptionRequestDTO(request);
		} catch (final Exception ex) {
			verify(systemRepository, times(1)).findBySystemNameAndAddressAndPort(any(), any(), anyInt());
			verify(eventTypeRepository, times(1)).findByEventTypeName(any());
			verify(eventTypeRepository, times(1)).saveAndFlush(any());
			verify(subscriptionRepository, never()).findByEventTypeAndSubscriberSystem(any(), any());			
			Assert.assertTrue(ex.getMessage().contains("End Date is further in the past than the tolerated time difference"));
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class )
	public void testGetSubscriptionBySubscriptionRequestDTOInvalidParameterEndDateIsBeforeStartDate() {
		ReflectionTestUtils.setField(eventHandlerService, "timeStampTolerance", 120);
		
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		request.setStartDate("3019-09-27T09:40:35Z");
		request.setEndDate("3019-09-27T09:40:34Z");
		
		final System system = createSystemForDBMock("systemName");
		final EventType eventType = createEventTypeForDBMock("eventType");
		final Subscription subscription = createSubscriptionForDBMock(1, "eventType", "subscriberName");

		when(systemRepository.findBySystemNameAndAddressAndPort(any(), any(), anyInt())).thenReturn(Optional.of(system));
		when(eventTypeRepository.findByEventTypeName(any())).thenReturn(Optional.ofNullable(null));
		when(eventTypeRepository.saveAndFlush(any())).thenReturn(eventType);
		when(subscriptionRepository.findByEventTypeAndSubscriberSystem(any(), any())).thenReturn(Optional.of(subscription));
		
		try {
			eventHandlerDBService.getSubscriptionBySubscriptionRequestDTO(request);
		} catch (final Exception ex) {
			verify(systemRepository, times(1)).findBySystemNameAndAddressAndPort(any(), any(), anyInt());
			verify(eventTypeRepository, times(1)).findByEventTypeName(any());
			verify(eventTypeRepository, times(1)).saveAndFlush(any());
			verify(subscriptionRepository, never()).findByEventTypeAndSubscriberSystem(any(), any());			
			Assert.assertTrue(ex.getMessage().contains("Start Date should be before End Date"));
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class )
	public void testGetSubscriptionBySubscriptionRequestDTOInvalidParameterStartDateIsNotBeforeEndDate() {
		ReflectionTestUtils.setField(eventHandlerService, "timeStampTolerance", 120);
		
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		request.setStartDate("3019-09-27T09:40:34Z");
		request.setEndDate("3019-09-27T09:40:34Z");
		
		final System system = createSystemForDBMock("systemName");
		final EventType eventType = createEventTypeForDBMock("eventType");
		final Subscription subscription = createSubscriptionForDBMock(1, "eventType", "subscriberName");

		when(systemRepository.findBySystemNameAndAddressAndPort(any(), any(), anyInt())).thenReturn(Optional.of(system));
		when(eventTypeRepository.findByEventTypeName(any())).thenReturn(Optional.ofNullable(null));
		when(eventTypeRepository.saveAndFlush(any())).thenReturn(eventType);
		when(subscriptionRepository.findByEventTypeAndSubscriberSystem(any(), any())).thenReturn(Optional.of(subscription));
		
		try {
			eventHandlerDBService.getSubscriptionBySubscriptionRequestDTO(request);
		} catch (final Exception ex) {
			verify(systemRepository, times(1)).findBySystemNameAndAddressAndPort(any(), any(), anyInt());
			verify(eventTypeRepository, times(1)).findByEventTypeName(any());
			verify(eventTypeRepository, times(1)).saveAndFlush(any());
			verify(subscriptionRepository, times(0)).findByEventTypeAndSubscriberSystem(any(), any());			
			Assert.assertTrue(ex.getMessage().contains("Start Date should be before End Date"));
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class )
	public void testGetSubscriptionBySubscriptionRequestDTOInvalidParameterMatchMetaDataTrueButFilterMetaDataIsNull() {
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		request.setMatchMetaData(true);
		request.setFilterMetaData(null);
		
		final System system = createSystemForDBMock("systemName");
		final EventType eventType = createEventTypeForDBMock("eventType");
		final Subscription subscription = createSubscriptionForDBMock(1, "eventType", "subscriberName");

		when(systemRepository.findBySystemNameAndAddressAndPort(any(), any(), anyInt())).thenReturn(Optional.of(system));
		when(eventTypeRepository.findByEventTypeName(any())).thenReturn(Optional.ofNullable(null));
		when(eventTypeRepository.saveAndFlush(any())).thenReturn(eventType);
		when(subscriptionRepository.findByEventTypeAndSubscriberSystem(any(), any())).thenReturn(Optional.of(subscription));
		
		try {
			eventHandlerDBService.getSubscriptionBySubscriptionRequestDTO(request);
		} catch (final Exception ex) {
			verify(systemRepository, times(1)).findBySystemNameAndAddressAndPort(any(), any(), anyInt());
			verify(eventTypeRepository, times(1)).findByEventTypeName(any());
			verify(eventTypeRepository, times(1)).saveAndFlush(any());
			verify(subscriptionRepository, never()).findByEventTypeAndSubscriberSystem(any(), any());			
			Assert.assertTrue(ex.getMessage().contains("If MatchMetaData is true filterMetaData should not be null or empty"));
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class )
	public void testGetSubscriptionBySubscriptionRequestDTOInvalidParameterMatchMetaDataTrueButFilterMetaDataIsEmpty() {
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		request.setMatchMetaData(true);
		request.setFilterMetaData(Map.of());
		
		final System system = createSystemForDBMock("systemName");
		final EventType eventType = createEventTypeForDBMock("eventType");
		final Subscription subscription = createSubscriptionForDBMock(1, "eventType", "subscriberName");

		when(systemRepository.findBySystemNameAndAddressAndPort(any(), any(), anyInt())).thenReturn(Optional.of(system));
		when(eventTypeRepository.findByEventTypeName(any())).thenReturn(Optional.ofNullable(null));
		when(eventTypeRepository.saveAndFlush(any())).thenReturn(eventType);
		when(subscriptionRepository.findByEventTypeAndSubscriberSystem(any(), any())).thenReturn(Optional.of(subscription));
		
		try {
			eventHandlerDBService.getSubscriptionBySubscriptionRequestDTO(request);
		} catch (final Exception ex) {
			verify(systemRepository, times(1)).findBySystemNameAndAddressAndPort(any(), any(), anyInt());
			verify(eventTypeRepository, times(1)).findByEventTypeName(any());
			verify(eventTypeRepository, times(1)).saveAndFlush(any());
			verify(subscriptionRepository, never()).findByEventTypeAndSubscriberSystem(any(), any());			
			Assert.assertTrue(ex.getMessage().contains("If MatchMetaData is true filterMetaData should not be null or empty"));
			throw ex;
		}
	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class )
	public void testGetSubscriptionBySubscriptionRequestDTOInvalidParameterSubscriptionNotExsistInDB() {
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		final System system = createSystemForDBMock("systemName");
		final EventType eventType = createEventTypeForDBMock("eventType");
		final Subscription subscription = null;

		when(systemRepository.findBySystemNameAndAddressAndPort(any(), any(), anyInt())).thenReturn(Optional.of(system));
		when(eventTypeRepository.findByEventTypeName(any())).thenReturn(Optional.ofNullable(null));
		when(eventTypeRepository.saveAndFlush(any())).thenReturn(eventType);
		when(subscriptionRepository.findByEventTypeAndSubscriberSystem(any(), any())).thenReturn(Optional.ofNullable(subscription));
		
		try {
			eventHandlerDBService.getSubscriptionBySubscriptionRequestDTO(request);
		} catch (final Exception ex) {
			verify(systemRepository, times(1)).findBySystemNameAndAddressAndPort(any(), any(), anyInt());
			verify(eventTypeRepository, times(1)).findByEventTypeName(any());
			verify(eventTypeRepository, times(1)).saveAndFlush(any());
			verify(subscriptionRepository, times(1)).findByEventTypeAndSubscriberSystem(any(), any());			
			Assert.assertTrue(ex.getMessage().contains("' not exists"));
			throw ex;
		}
	}
	
	//=================================================================================================
	// Tests of deleteSubscriptionResponse
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testDeleteSubscriptionResponseOK() {
		final long id = 1L;
		final Subscription subscription = createSubscriptionForDBMock(1, "eventType", "subscriberName");
		final Set<SubscriptionPublisherConnection> involvedPublisherSystems = Set.of();
		
		when(subscriptionRepository.findById(anyLong())).thenReturn(Optional.of(subscription));
		when(subscriptionPublisherConnectionRepository.findBySubscriptionEntry(any())).thenReturn(involvedPublisherSystems);
		doNothing().when(subscriptionPublisherConnectionRepository).deleteInBatch(any());
		doNothing().when(subscriptionRepository).refresh(any());
		doNothing().when(subscriptionRepository).delete(any());
		
		eventHandlerDBService.deleteSubscriptionResponse(id);
		
		verify(subscriptionRepository, times(1)).findById(anyLong());
		verify(subscriptionPublisherConnectionRepository, times(1)).findBySubscriptionEntry(any());
		verify(subscriptionPublisherConnectionRepository, times(1)).deleteInBatch(any());
		verify(subscriptionRepository, times(1)).refresh(any());
		verify(subscriptionRepository, times(1)).delete(any());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testDeleteSubscriptionResponseInvalidParameterId() {
		final long id = -1L;
		final Subscription subscription = createSubscriptionForDBMock(1, "eventType", "subscriberName");
		final Set<SubscriptionPublisherConnection> involvedPublisherSystems = Set.of();
		
		when(subscriptionRepository.findById(anyLong())).thenReturn(Optional.of(subscription));
		when(subscriptionPublisherConnectionRepository.findBySubscriptionEntry(any())).thenReturn(involvedPublisherSystems);
		doNothing().when(subscriptionPublisherConnectionRepository).deleteInBatch(any());
		doNothing().when(subscriptionRepository).refresh(any());
		doNothing().when(subscriptionRepository).delete(any());
		
		try {
			eventHandlerDBService.deleteSubscriptionResponse(id);
		} catch (final Exception ex) {
			verify(subscriptionRepository, never()).findById(anyLong());
			verify(subscriptionPublisherConnectionRepository, never()).findBySubscriptionEntry(any());
			verify(subscriptionPublisherConnectionRepository, never()).deleteInBatch(any());
			verify(subscriptionRepository, never()).refresh(any());
			verify(subscriptionRepository, never()).delete(any());
			Assert.assertTrue(ex.getMessage().contains("SubscriberSystemId must be greater than zero."));
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testDeleteSubscriptionResponseSubscriptionNotInDBOK() {
		final long id = 1L;
		final Subscription subscription = null;
		final Set<SubscriptionPublisherConnection> involvedPublisherSystems = Set.of();
		
		when(subscriptionRepository.findById(anyLong())).thenReturn(Optional.ofNullable(subscription));
		when(subscriptionPublisherConnectionRepository.findBySubscriptionEntry(any())).thenReturn(involvedPublisherSystems);
		doNothing().when(subscriptionPublisherConnectionRepository).deleteInBatch(any());
		doNothing().when(subscriptionRepository).refresh(any());
		doNothing().when(subscriptionRepository).delete(any());
		
		eventHandlerDBService.deleteSubscriptionResponse(id);
		
		verify(subscriptionRepository, times(1)).findById(anyLong());
		verify(subscriptionPublisherConnectionRepository, never()).findBySubscriptionEntry(any());
		verify(subscriptionPublisherConnectionRepository, never()).deleteInBatch(any());
		verify(subscriptionRepository, never()).refresh(any());
		verify(subscriptionRepository, never()).delete(any());
	}
	
	//=================================================================================================
	// Tests of registerSubscription
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRegisterSubscriptionOK() {
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		final Set<SystemResponseDTO> authorizedPublishers = Set.of();
		final Subscription subscription = createSubscriptionForDBMock(1, "eventType", "subscriberName");
		final System system = createSystemForDBMock("systemName");
		final EventType eventType = createEventTypeForDBMock("eventType");

		when(systemRepository.findBySystemNameAndAddressAndPort(any(), any(), anyInt())).thenReturn(Optional.of(system));
		when(eventTypeRepository.findByEventTypeName(any())).thenReturn(Optional.ofNullable(eventType));
		when(eventTypeRepository.saveAndFlush(any())).thenReturn(eventType);
		when(subscriptionRepository.findByEventTypeAndSubscriberSystem(any(), any())).thenReturn(Optional.ofNullable(null));
		when(subscriptionRepository.save(any())).thenReturn(subscription);
		when(subscriptionPublisherConnectionRepository.saveAll(any())).thenReturn(List.of());
		doNothing().when(subscriptionPublisherConnectionRepository).flush();
		when(subscriptionRepository.saveAndFlush(any())).thenReturn(subscription);
		
		eventHandlerDBService.registerSubscription(request, authorizedPublishers);
		
		verify(systemRepository, times(1)).findBySystemNameAndAddressAndPort(any(), any(), anyInt());
		verify(eventTypeRepository, times(1)).findByEventTypeName(any());
		verify(eventTypeRepository, never()).saveAndFlush(any());
		verify(subscriptionRepository, times(1)).findByEventTypeAndSubscriberSystem(any(), any());			
		verify(subscriptionRepository, times(1)).save(any());
		verify(subscriptionPublisherConnectionRepository, times(1)).saveAll(any());
		verify(subscriptionPublisherConnectionRepository, times(1)).flush();
		verify(subscriptionRepository, times(1)).saveAndFlush(any());
	}
	
	//-------------------------------------------------------------------------------------------------
	// skipped	@Tests validateSubscriptionRequestDTO(request) -  same method as in getSubscriptionBySubscriptionRequestDTO tests
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testRegisterSubscriptionInvalidParameterSubscriptionAllreadyInDB() {
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		final Set<SystemResponseDTO> authorizedPublishers = Set.of();
		final Subscription subscription = createSubscriptionForDBMock(1, "eventType", "subscriberName");

		final System system = createSystemForDBMock("systemName");
		final EventType eventType = createEventTypeForDBMock("eventType");

		when(systemRepository.findBySystemNameAndAddressAndPort(any(), any(), anyInt())).thenReturn(Optional.of(system));
		when(eventTypeRepository.findByEventTypeName(any())).thenReturn(Optional.ofNullable(eventType));
		when(eventTypeRepository.saveAndFlush(any())).thenReturn(eventType);
		when(subscriptionRepository.findByEventTypeAndSubscriberSystem(any(), any())).thenReturn(Optional.ofNullable(subscription));
		when(subscriptionRepository.save(any())).thenReturn(subscription);
		when(subscriptionPublisherConnectionRepository.saveAll(any())).thenReturn(List.of());
		doNothing().when(subscriptionPublisherConnectionRepository).flush();
		when(subscriptionRepository.saveAndFlush(any())).thenReturn(subscription);
		
		try {
			eventHandlerDBService.registerSubscription(request, authorizedPublishers);
		} catch (final Exception ex) {
			verify(systemRepository, times(1)).findBySystemNameAndAddressAndPort(any(), any(), anyInt());
			verify(eventTypeRepository, times(1)).findByEventTypeName(any());
			verify(eventTypeRepository, never()).saveAndFlush(any());
			verify(subscriptionRepository, times(1)).findByEventTypeAndSubscriberSystem(any(), any());			
			verify(subscriptionRepository, never()).save(any());
			verify(subscriptionPublisherConnectionRepository, never()).saveAll(any());
			verify(subscriptionPublisherConnectionRepository, never()).flush();
			verify(subscriptionRepository, never()).saveAndFlush(any());
			Assert.assertTrue(ex.getMessage().contains("Subscription violates unique constraint rules"));
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testRegisterSubscriptionOnlyPredefinedPublishersIsTrueOK() {
		final SystemRequestDTO predifinedPublisher = getSystemRequestDTO();
		predifinedPublisher.setSystemName("predifinedsystemname");
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		request.setSources(Set.of(predifinedPublisher));
		final Set<SystemResponseDTO> authorizedPublishers = Set.of(getSystemResponseDTO("systemName"), getSystemResponseDTO("predifinedsystemname"));
		final Subscription subscription = createSubscriptionForDBMock(1, "eventType", "subscriberName");
		subscription.setOnlyPredefinedPublishers(true);

		final System system = createSystemForDBMock("predifinedsystemname");
		final EventType eventType = createEventTypeForDBMock("eventType");

		final ArgumentCaptor<Set> valueCapture = ArgumentCaptor.forClass(Set.class);
		when(systemRepository.findBySystemNameAndAddressAndPort(any(), any(), anyInt())).thenReturn(Optional.of(system));
		when(eventTypeRepository.findByEventTypeName(any())).thenReturn(Optional.ofNullable(eventType));
		when(eventTypeRepository.saveAndFlush(any())).thenReturn(eventType);
		when(subscriptionRepository.findByEventTypeAndSubscriberSystem(any(), any())).thenReturn(Optional.ofNullable(null));
		when(subscriptionRepository.save(any())).thenReturn(subscription);
		when(subscriptionPublisherConnectionRepository.saveAll(valueCapture.capture())).thenReturn(List.of());
		doNothing().when(subscriptionPublisherConnectionRepository).flush();
		when(subscriptionRepository.saveAndFlush(any())).thenReturn(subscription);
		
		eventHandlerDBService.registerSubscription(request, authorizedPublishers);
		
		verify(systemRepository, times(2)).findBySystemNameAndAddressAndPort(any(), any(), anyInt());
		verify(eventTypeRepository, times(1)).findByEventTypeName(any());
		verify(eventTypeRepository, never()).saveAndFlush(any());
		verify(subscriptionRepository, times(1)).findByEventTypeAndSubscriberSystem(any(), any());			
		verify(subscriptionRepository, times(1)).save(any());
		verify(subscriptionPublisherConnectionRepository, times(1)).saveAll(any());
		verify(subscriptionPublisherConnectionRepository, times(1)).flush();
		verify(subscriptionRepository, times(1)).saveAndFlush(any());
		
		final Set<SubscriptionPublisherConnection> publisherConnections = valueCapture.getValue();
		assertNotNull(publisherConnections);
		assertTrue(publisherConnections.size() == 1);
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testRegisterSubscriptionOnlyPredefinedPublishersIsFalseOK() {
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		request.setSources(Set.of());
		final Set<SystemResponseDTO> authorizedPublishers = Set.of(getSystemResponseDTO("systemName"), getSystemResponseDTO("predifinedsystemname"));
		final Subscription subscription = createSubscriptionForDBMock(1, "eventType", "subscriberName");
		subscription.setOnlyPredefinedPublishers(false);

		final System system = createSystemForDBMock("predifinedsystemname");
		final EventType eventType = createEventTypeForDBMock("eventType");

		final ArgumentCaptor<Set> valueCapture = ArgumentCaptor.forClass(Set.class);
		when(systemRepository.findBySystemNameAndAddressAndPort(any(), any(), anyInt())).thenReturn(Optional.of(system));
		when(eventTypeRepository.findByEventTypeName(any())).thenReturn(Optional.ofNullable(eventType));
		when(eventTypeRepository.saveAndFlush(any())).thenReturn(eventType);
		when(subscriptionRepository.findByEventTypeAndSubscriberSystem(any(), any())).thenReturn(Optional.ofNullable(null));
		when(subscriptionRepository.save(any())).thenReturn(subscription);
		when(subscriptionPublisherConnectionRepository.saveAll(valueCapture.capture())).thenReturn(List.of());
		doNothing().when(subscriptionPublisherConnectionRepository).flush();
		when(subscriptionRepository.saveAndFlush(any())).thenReturn(subscription);
		
		eventHandlerDBService.registerSubscription(request, authorizedPublishers);
		
		verify(systemRepository, times(1)).findBySystemNameAndAddressAndPort(any(), any(), anyInt());
		verify(eventTypeRepository, times(1)).findByEventTypeName(any());
		verify(eventTypeRepository, never()).saveAndFlush(any());
		verify(subscriptionRepository, times(1)).findByEventTypeAndSubscriberSystem(any(), any());			
		verify(subscriptionRepository, times(1)).save(any());
		verify(subscriptionPublisherConnectionRepository, times(1)).saveAll(any());
		verify(subscriptionPublisherConnectionRepository, times(1)).flush();
		verify(subscriptionRepository, times(1)).saveAndFlush(any());
		
		final Set<SubscriptionPublisherConnection> publisherConnections = valueCapture.getValue();
		assertNotNull(publisherConnections);
		assertTrue(publisherConnections.size() == 2);
	}
	
	//=================================================================================================
	// Tests of forceRegisterSubscription
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testForceRegisterSubscriptionRequestNull() {
		try {
			eventHandlerDBService.forceRegisterSubscription(null, Set.of());
		} catch (final InvalidParameterException ex) {
			assertEquals("SubscriptionRequestDTO is null", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testForceRegisterSubscriptionNew() {
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		final Set<SystemResponseDTO> authorizedPublishers = Set.of();
		final Subscription subscription = createSubscriptionForDBMock(1, "eventType", "subscriberName");
		final System system = createSystemForDBMock("systemName");
		final EventType eventType = createEventTypeForDBMock("eventType");

		when(subscriptionRepository.findByEventTypeAndSubscriberSystem(any(EventType.class), any(System.class))).thenReturn(Optional.empty());
		when(systemRepository.findBySystemNameAndAddressAndPort(any(), any(), anyInt())).thenReturn(Optional.of(system));
		when(eventTypeRepository.findByEventTypeName(any())).thenReturn(Optional.ofNullable(eventType));
		when(eventTypeRepository.saveAndFlush(any())).thenReturn(eventType);
		when(subscriptionRepository.save(any())).thenReturn(subscription);
		when(subscriptionPublisherConnectionRepository.saveAll(any())).thenReturn(List.of());
		doNothing().when(subscriptionPublisherConnectionRepository).flush();
		when(subscriptionRepository.saveAndFlush(any())).thenReturn(subscription);
		
		eventHandlerDBService.forceRegisterSubscription(request, authorizedPublishers);
		
		verify(subscriptionRepository, times(2)).findByEventTypeAndSubscriberSystem(any(EventType.class), any(System.class));
		verify(subscriptionRepository, never()).delete(any(Subscription.class));
		
		verify(systemRepository, times(2)).findBySystemNameAndAddressAndPort(any(), any(), anyInt());
		verify(eventTypeRepository, times(2)).findByEventTypeName(any());
		verify(eventTypeRepository, never()).saveAndFlush(any());
		verify(subscriptionRepository, times(1)).save(any());
		verify(subscriptionPublisherConnectionRepository, times(1)).saveAll(any());
		verify(subscriptionPublisherConnectionRepository, times(1)).flush();
		verify(subscriptionRepository, times(1)).saveAndFlush(any());
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("unchecked")
	@Test
	public void testForceRegisterSubscriptionExisting() {
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		final Set<SystemResponseDTO> authorizedPublishers = Set.of();
		final Subscription subscription = createSubscriptionForDBMock(1, "eventType", "subscriberName");
		final System system = createSystemForDBMock("systemName");
		final EventType eventType = createEventTypeForDBMock("eventType");

		when(subscriptionRepository.findByEventTypeAndSubscriberSystem(any(EventType.class), any(System.class))).thenReturn(Optional.of(new Subscription()), Optional.empty());
		when(subscriptionPublisherConnectionRepository.findBySubscriptionEntry(any(Subscription.class))).thenReturn(Set.of());
		doNothing().when(subscriptionPublisherConnectionRepository).deleteAllInBatch();
		doNothing().when(subscriptionRepository).refresh(any(Subscription.class));
		doNothing().when(subscriptionRepository).delete(any(Subscription.class));
		
		when(systemRepository.findBySystemNameAndAddressAndPort(any(), any(), anyInt())).thenReturn(Optional.of(system));
		when(eventTypeRepository.findByEventTypeName(any())).thenReturn(Optional.ofNullable(eventType));
		when(eventTypeRepository.saveAndFlush(any())).thenReturn(eventType);
		when(subscriptionRepository.save(any())).thenReturn(subscription);
		when(subscriptionPublisherConnectionRepository.saveAll(any())).thenReturn(List.of());
		doNothing().when(subscriptionPublisherConnectionRepository).flush();
		when(subscriptionRepository.saveAndFlush(any())).thenReturn(subscription);
		
		eventHandlerDBService.forceRegisterSubscription(request, authorizedPublishers);
		
		verify(subscriptionRepository, times(2)).findByEventTypeAndSubscriberSystem(any(EventType.class), any(System.class));
		verify(subscriptionRepository, times(1)).delete(any(Subscription.class));
		
		verify(systemRepository, times(2)).findBySystemNameAndAddressAndPort(any(), any(), anyInt());
		verify(eventTypeRepository, times(2)).findByEventTypeName(any());
		verify(eventTypeRepository, never()).saveAndFlush(any());
		verify(subscriptionRepository, times(1)).save(any());
		verify(subscriptionPublisherConnectionRepository, times(1)).saveAll(any());
		verify(subscriptionPublisherConnectionRepository, times(2)).flush();
		verify(subscriptionRepository, times(1)).saveAndFlush(any());
	}
	
	//=================================================================================================
	// Tests of updateSubscription
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testUpdateSubscriptionOK() {
		final long id = 1L;
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		final Set<SystemResponseDTO> authorizedPublishers = Set.of();
		final Subscription subscriptionToUpdate = createSubscriptionForDBMock(1, "eventType", "subscriberToUpdateName");
		final Subscription subscriptionForUpdate = createSubscriptionForDBMock(1, "eventType", "subscriberForUpdateName");
		final Set<SubscriptionPublisherConnection> involvedPublisherSystems = Set.of();
		
		final System systemForUpdate = createSystemForDBMock("systemNameForUpdate");
		final EventType eventType = createEventTypeForDBMock("eventType");
		
		when(subscriptionRepository.findById(anyLong())).thenReturn(Optional.of(subscriptionToUpdate));
		when(subscriptionPublisherConnectionRepository.findBySubscriptionEntry(any())).thenReturn(involvedPublisherSystems);
		when(systemRepository.findBySystemNameAndAddressAndPort(any(), any(), anyInt())).thenReturn(Optional.of(systemForUpdate));
		when(eventTypeRepository.findByEventTypeName(any())).thenReturn(Optional.ofNullable(eventType));
		when(eventTypeRepository.saveAndFlush(any())).thenReturn(eventType);
		when(subscriptionRepository.findByEventTypeAndSubscriberSystem(any(), any())).thenReturn(Optional.ofNullable(null));
		doNothing().when(subscriptionPublisherConnectionRepository).deleteInBatch(any());
		doNothing().when(subscriptionRepository).refresh(any());
		when(subscriptionPublisherConnectionRepository.saveAll(any())).thenReturn(List.of());
		doNothing().when(subscriptionPublisherConnectionRepository).flush();
		when(subscriptionRepository.saveAndFlush(any())).thenReturn(subscriptionForUpdate);
			
		final Subscription response = eventHandlerDBService.updateSubscription(id, request, authorizedPublishers);
		
		verify(subscriptionRepository, times(1)).findById(anyLong());			
		verify(subscriptionPublisherConnectionRepository, times(1)).findBySubscriptionEntry(any());
		verify(systemRepository, times(1)).findBySystemNameAndAddressAndPort(any(), any(), anyInt());
		verify(eventTypeRepository, times(1)).findByEventTypeName(any());
		verify(eventTypeRepository, never()).saveAndFlush(any());
		verify(subscriptionRepository, never()).findByEventTypeAndSubscriberSystem(any(), any());			
		verify(subscriptionPublisherConnectionRepository, times(1)).deleteInBatch(any());
		verify(subscriptionRepository, times(1)).refresh(any());
		verify(subscriptionPublisherConnectionRepository, times(1)).saveAll(any());
		verify(subscriptionPublisherConnectionRepository, times(1)).flush();
		verify(subscriptionRepository, times(1)).saveAndFlush(any());
		assertNotNull(response);
		assertTrue("subscriberForUpdateName".equalsIgnoreCase(response.getSubscriberSystem().getSystemName()));
	}
	
	//-------------------------------------------------------------------------------------------------
	// skipped	@Tests validateSubscriptionRequestDTO(request) -  same method as in getSubscriptionBySubscriptionRequestDTO tests
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testUpdateSubscriptionOnlyPredefinedPublishersIsTrueOK() {
		final long id = 1L;
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		request.setSources(Set.of(getSystemRequestDTO()));
		final Set<SystemResponseDTO> authorizedPublishers = Set.of();
		final Set<SubscriptionPublisherConnection> involvedPublisherSystems = Set.of();
		final Subscription subscriptionToUpdate = createSubscriptionForDBMock(1, "eventType", "subscriberToUpdateName");		
		final Subscription subscriptionForUpdate = createSubscriptionForDBMock(1, "eventType", "subscriberForUpdateName");
		subscriptionForUpdate.setOnlyPredefinedPublishers(true);	
		
		final System systemForUpdate = createSystemForDBMock("systemNameForUpdate");
		final EventType eventType = createEventTypeForDBMock("eventType");
		
		when(subscriptionRepository.findById(anyLong())).thenReturn(Optional.of(subscriptionToUpdate));
		when(subscriptionPublisherConnectionRepository.findBySubscriptionEntry(any())).thenReturn(involvedPublisherSystems);
		when(systemRepository.findBySystemNameAndAddressAndPort(any(), any(), anyInt())).thenReturn(Optional.of(systemForUpdate));
		when(eventTypeRepository.findByEventTypeName(any())).thenReturn(Optional.ofNullable(eventType));
		when(eventTypeRepository.saveAndFlush(any())).thenReturn(eventType);
		when(subscriptionRepository.findByEventTypeAndSubscriberSystem(any(), any())).thenReturn(Optional.ofNullable(null));
		doNothing().when(subscriptionPublisherConnectionRepository).deleteInBatch(any());
		doNothing().when(subscriptionRepository).refresh(any());
		when(subscriptionPublisherConnectionRepository.saveAll(any())).thenReturn(List.of());
		doNothing().when(subscriptionPublisherConnectionRepository).flush();
		when(subscriptionRepository.saveAndFlush(any())).thenReturn(subscriptionForUpdate);
			
		final Subscription response = eventHandlerDBService.updateSubscription(id, request, authorizedPublishers);
		
		verify(subscriptionRepository, times(1)).findById(anyLong());			
		verify(subscriptionPublisherConnectionRepository, times(1)).findBySubscriptionEntry(any());
		verify(systemRepository, times(2)).findBySystemNameAndAddressAndPort(any(), any(), anyInt());
		verify(eventTypeRepository, times(1)).findByEventTypeName(any());
		verify(eventTypeRepository, never()).saveAndFlush(any());
		verify(subscriptionRepository, never()).findByEventTypeAndSubscriberSystem(any(), any());			
		verify(subscriptionPublisherConnectionRepository, times(1)).deleteInBatch(any());
		verify(subscriptionRepository, times(1)).refresh(any());
		verify(subscriptionPublisherConnectionRepository, times(1)).saveAll(any());
		verify(subscriptionPublisherConnectionRepository, times(1)).flush();
		verify(subscriptionRepository, times(1)).saveAndFlush(any());
		assertNotNull(response);
		assertTrue("subscriberForUpdateName".equalsIgnoreCase(response.getSubscriberSystem().getSystemName()));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testUpdateSubscriptionOnlyPredefinedPublishersIsFalseOK() {
		final long id = 1L;
		final SubscriptionRequestDTO request = getSubscriptionRequestDTOForTest();
		request.setSources(Set.of());
		final Set<SystemResponseDTO> authorizedPublishers = Set.of();
		final Set<SubscriptionPublisherConnection> involvedPublisherSystems = Set.of();
		final Subscription subscriptionToUpdate = createSubscriptionForDBMock(1, "eventType", "subscriberToUpdateName");		
		final Subscription subscriptionForUpdate = createSubscriptionForDBMock(1, "eventType", "subscriberForUpdateName");
		subscriptionForUpdate.setOnlyPredefinedPublishers(false);
		
		final System systemForUpdate = createSystemForDBMock("systemNameForUpdate");
		final EventType eventType = createEventTypeForDBMock("eventType");
		
		when(subscriptionRepository.findById(anyLong())).thenReturn(Optional.of(subscriptionToUpdate));
		when(subscriptionPublisherConnectionRepository.findBySubscriptionEntry(any())).thenReturn(involvedPublisherSystems);
		when(systemRepository.findBySystemNameAndAddressAndPort(any(), any(), anyInt())).thenReturn(Optional.of(systemForUpdate));
		when(eventTypeRepository.findByEventTypeName(any())).thenReturn(Optional.ofNullable(eventType));
		when(eventTypeRepository.saveAndFlush(any())).thenReturn(eventType);
		when(subscriptionRepository.findByEventTypeAndSubscriberSystem(any(), any())).thenReturn(Optional.ofNullable(null));
		doNothing().when(subscriptionPublisherConnectionRepository).deleteInBatch(any());
		doNothing().when(subscriptionRepository).refresh(any());
		when(subscriptionPublisherConnectionRepository.saveAll(any())).thenReturn(List.of());
		doNothing().when(subscriptionPublisherConnectionRepository).flush();
		when(subscriptionRepository.saveAndFlush(any())).thenReturn(subscriptionForUpdate);
			
		final Subscription response = eventHandlerDBService.updateSubscription(id, request, authorizedPublishers);
		
		verify(subscriptionRepository, times(1)).findById(anyLong());			
		verify(subscriptionPublisherConnectionRepository, times(1)).findBySubscriptionEntry(any());
		verify(systemRepository, times(1)).findBySystemNameAndAddressAndPort(any(), any(), anyInt());
		verify(eventTypeRepository, times(1)).findByEventTypeName(any());
		verify(eventTypeRepository, never()).saveAndFlush(any());
		verify(subscriptionRepository, never()).findByEventTypeAndSubscriberSystem(any(), any());			
		verify(subscriptionPublisherConnectionRepository, times(1)).deleteInBatch(any());
		verify(subscriptionRepository, times(1)).refresh(any());
		verify(subscriptionPublisherConnectionRepository, times(1)).saveAll(any());
		verify(subscriptionPublisherConnectionRepository, times(1)).flush();
		verify(subscriptionRepository, times(1)).saveAndFlush(any());
		assertNotNull(response);
		assertTrue("subscriberForUpdateName".equalsIgnoreCase(response.getSubscriberSystem().getSystemName()));
	}
	
	//=================================================================================================
	// Tests of getInvolvedSubscriptions
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetInvolvedSubscriptionsOK() {
		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();
		final EventType eventType = createEventTypeForDBMock("eventType");
		final System system = createSystemForDBMock("systemName");
		final Subscription subscription = createSubscriptionForDBMock(1, "eventType", "subscriberName");

		when(eventTypeRepository.findByEventTypeName(any())).thenReturn(Optional.ofNullable(eventType));
		when(eventTypeRepository.saveAndFlush(any())).thenReturn(eventType);
		when(subscriptionRepository.findAllByEventType(any())).thenReturn(Set.of(subscription));
		when(systemRepository.findBySystemNameAndAddressAndPort(any(), any(), anyInt())).thenReturn(Optional.of(system));
		when(subscriptionPublisherConnectionRepository.findAllBySystemAndAuthorized(any(), anyBoolean())).thenReturn(List.of());
		
		eventHandlerDBService.getInvolvedSubscriptions(request);
		
		verify(eventTypeRepository, times(1)).findByEventTypeName(any());
		verify(eventTypeRepository, never()).saveAndFlush(any());
		verify(subscriptionRepository, times(1)).findAllByEventType(any());			
		verify(systemRepository, times(1)).findBySystemNameAndAddressAndPort(any(), any(), anyInt());
		verify(subscriptionPublisherConnectionRepository, times(1)).findAllBySystemAndAuthorized(any(), anyBoolean());
	}
	
	//=================================================================================================
	// Tests of getInvolvedSubscriptionsBySubscriberSystemId
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetInvolvedSubscriptionsBySubscriberSystemIdOK() {
		final long id = 1L;
		final System system = createSystemForDBMock("systemName");
		final Subscription subscription = createSubscriptionForDBMock(1, "eventType", "subscriberName");

		when(systemRepository.findById(anyLong())).thenReturn(Optional.of(system));
		when(subscriptionRepository.findAllBySubscriberSystem(any())).thenReturn(List.of(subscription));
		
		eventHandlerDBService.getInvolvedSubscriptionsBySubscriberSystemId(id);
		
		verify(systemRepository, times(1)).findById(anyLong());
		verify(subscriptionRepository, times(1)).findAllBySubscriberSystem(any());			
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGetInvolvedSubscriptionsBySubscriberSystemIdInvalidParameterIdNull() {
		final Long id = null;
		final System system = createSystemForDBMock("systemName");
		final Subscription subscription = createSubscriptionForDBMock(1, "eventType", "subscriberName");

		when(systemRepository.findById(anyLong())).thenReturn(Optional.ofNullable(system));
		when(subscriptionRepository.findAllBySubscriberSystem(any())).thenReturn(List.of(subscription));
		
		try {
			eventHandlerDBService.getInvolvedSubscriptionsBySubscriberSystemId(id);
		} catch (final Exception ex) {
			verify(systemRepository, never()).findById(anyLong());
			verify(subscriptionRepository, never()).findAllBySubscriberSystem(any());			
			assertTrue(ex.getMessage().contains("SubscriberSystemId is null"));
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGetInvolvedSubscriptionsBySubscriberSystemIdInvalidParameterIdInvalId() {
		final Long id = 0L;
		final System system = createSystemForDBMock("systemName");
		final Subscription subscription = createSubscriptionForDBMock(1, "eventType", "subscriberName");

		when(systemRepository.findById(anyLong())).thenReturn(Optional.ofNullable(system));
		when(subscriptionRepository.findAllBySubscriberSystem(any())).thenReturn(List.of(subscription));
		
		try {
			eventHandlerDBService.getInvolvedSubscriptionsBySubscriberSystemId(id);
		} catch (final Exception ex) {
			verify(systemRepository, never()).findById(anyLong());
			verify(subscriptionRepository, never()).findAllBySubscriberSystem(any());			
			assertTrue(ex.getMessage().contains("SubscriberSystemId must be greater than zero."));
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGetInvolvedSubscriptionsBySubscriberSystemIdInvalidParameterSubscriberSystemNotInDB() {
		final long id = 1L;
		final System system = null;
		final Subscription subscription = createSubscriptionForDBMock(1, "eventType", "subscriberName");

		when(systemRepository.findById(anyLong())).thenReturn(Optional.ofNullable(system));
		when(subscriptionRepository.findAllBySubscriberSystem(any())).thenReturn(List.of(subscription));
		
		try {
			eventHandlerDBService.getInvolvedSubscriptionsBySubscriberSystemId(id);
		} catch (final Exception ex) {
			verify(systemRepository, times(1)).findById(anyLong());
			verify(subscriptionRepository, never()).findAllBySubscriberSystem(any());			
			assertTrue(ex.getMessage().contains("SubscriberSystem is not available in database"));
			throw ex;
		}
	}
	
	//=================================================================================================
	// Tests of updateSubscriberAuthorization
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testUpdateSubscriberAuthorizationOK() {
		final Subscription subscription = createSubscriptionForDBMock(1, "eventType", "subscriberName");
		final List<Subscription> involvedSubscriptions = List.of(subscription);
		final Set<SystemResponseDTO> authorizedPublishers = getSystemResponseDTOSet(7);
		final Set<SubscriptionPublisherConnection> involvedPublisherSystems = Set.of();

		when(subscriptionRepository.findById(anyLong())).thenReturn(Optional.of(subscription));
		when(subscriptionPublisherConnectionRepository.findBySubscriptionEntry(any())).thenReturn(involvedPublisherSystems);
		doNothing().when(subscriptionPublisherConnectionRepository).deleteInBatch(any());
		doNothing().when(subscriptionRepository).refresh(any());
		when(subscriptionPublisherConnectionRepository.saveAll(any())).thenReturn(List.of());
		doNothing().when(subscriptionPublisherConnectionRepository).flush();
		
		eventHandlerDBService.updateSubscriberAuthorization(involvedSubscriptions, authorizedPublishers);
		
		verify(subscriptionPublisherConnectionRepository, times(1)).findBySubscriptionEntry(any());
		verify(subscriptionPublisherConnectionRepository, times(1)).deleteInBatch(any());
		verify(subscriptionRepository, times(1)).refresh(any());
		verify(subscriptionPublisherConnectionRepository, times(1)).saveAll(any());
		verify(subscriptionPublisherConnectionRepository, times(1)).flush();
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testUpdateSubscriberAuthorizationOnlyPredefinedPublishersIsTrueOK() {
		final Subscription subscription = createSubscriptionForDBMock(1, "eventType", "subscriberName");
		subscription.setOnlyPredefinedPublishers(true);
		
		final List<Subscription> involvedSubscriptions = List.of(subscription);
		final Set<SystemResponseDTO> authorizedPublishers = getSystemResponseDTOSet(7);
		final Set<SubscriptionPublisherConnection> involvedPublisherSystems = Set.of();

		when(subscriptionRepository.findById(anyLong())).thenReturn(Optional.of(subscription));
		when(subscriptionPublisherConnectionRepository.findBySubscriptionEntry(any())).thenReturn(involvedPublisherSystems);
		doNothing().when(subscriptionPublisherConnectionRepository).deleteInBatch(any());
		doNothing().when(subscriptionRepository).refresh(any());
		when(subscriptionPublisherConnectionRepository.saveAll(any())).thenReturn(List.of());
		doNothing().when(subscriptionPublisherConnectionRepository).flush();
		
		eventHandlerDBService.updateSubscriberAuthorization(involvedSubscriptions, authorizedPublishers);
		
		verify(subscriptionPublisherConnectionRepository, times(1)).findBySubscriptionEntry(any());
		verify(subscriptionPublisherConnectionRepository, never()).deleteInBatch(any());
		verify(subscriptionRepository, never()).refresh(any());
		verify(subscriptionPublisherConnectionRepository, times(1)).saveAll(any());
		verify(subscriptionPublisherConnectionRepository, times(1)).flush();
	}
	
	//=================================================================================================
	// Tests of removeSubscriptionEntries
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRemoveSubscriptionEntriesOK() {
		final Subscription subscription = createSubscriptionForDBMock(1, "eventType", "subscriberName");
		final List<Subscription> request = List.of(subscription);
		
		doNothing().when(subscriptionRepository).deleteInBatch(any());
		doNothing().when(subscriptionRepository).flush();
		
		eventHandlerDBService.removeSubscriptionEntries(request);
		
		verify(subscriptionRepository,times(1)).deleteInBatch(any());
		verify(subscriptionRepository,times(1)).flush();
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRemoveSubscriptionEntriesRequestNullOK() {
		final List<Subscription> request = null;
		
		doNothing().when(subscriptionRepository).deleteInBatch(any());
		doNothing().when(subscriptionRepository).flush();
		
		eventHandlerDBService.removeSubscriptionEntries(request);
		
		verify(subscriptionRepository, never()).deleteInBatch(any());
		verify(subscriptionRepository, never()).flush();
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRemoveSubscriptionEntriesRequestEmptyOK() {
		final List<Subscription> request = List.of();
		
		doNothing().when(subscriptionRepository).deleteInBatch(any());
		doNothing().when(subscriptionRepository).flush();
		
		eventHandlerDBService.removeSubscriptionEntries(request);
		
		verify(subscriptionRepository, never()).deleteInBatch(any());
		verify(subscriptionRepository, never()).flush();
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------	
	private Subscription createSubscriptionForDBMock(final int i, final String eventType, final String subscriberName) {
		final Subscription subscription = new Subscription(createEventTypeForDBMock(eventType),	createSystemForDBMock(subscriberName), null, "notifyUri", false, false,	null, null);
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
	private SubscriptionRequestDTO getSubscriptionRequestDTOForTest() {
		return new SubscriptionRequestDTO(
				"eventType", 
				getSystemRequestDTO(), 
				null, // filterMetaData
				"notifyUri", 
				false, // matchMetaData
				null, // startDate
				null, // endDate, 
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
		return new EventPublishRequestDTO(
				"eventType", 
				getSystemRequestDTO(), // source, 
				null, // metaData, 
				"payload", 
				Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now().plusSeconds(1)));
	}
}