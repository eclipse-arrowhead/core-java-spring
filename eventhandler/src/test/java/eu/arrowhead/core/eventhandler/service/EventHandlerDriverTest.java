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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.core.CoreSystemService;
import eu.arrowhead.common.database.entity.EventType;
import eu.arrowhead.common.database.entity.Subscription;
import eu.arrowhead.common.database.entity.System;
import eu.arrowhead.common.dto.internal.AuthorizationSubscriptionCheckRequestDTO;
import eu.arrowhead.common.dto.internal.AuthorizationSubscriptionCheckResponseDTO;
import eu.arrowhead.common.dto.internal.DTOConverter;
import eu.arrowhead.common.dto.shared.EventPublishRequestDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.common.dto.shared.SystemResponseDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.common.http.HttpService;
import eu.arrowhead.core.eventhandler.publish.PublishingQueue;

@RunWith(SpringRunner.class)
public class EventHandlerDriverTest {

	//=================================================================================================
	// members
	
	@InjectMocks
	private EventHandlerDriver eventHandlerDriver;

	@Mock
	private HttpService httpService;
	
	@Mock
	private Map<String,Object> arrowheadContext;
	
	@Mock
	private PublishingQueue publishingQueue;
	
	@Mock
	private Logger logger;
	
	private static final String AUTH_SUBSCRIPTION_CHECK_URI_KEY = CoreSystemService.AUTH_CONTROL_SUBSCRIPTION_SERVICE.getServiceDefinition() + CoreCommonConstants.URI_SUFFIX;
	
	//=================================================================================================
	// methods
	
	//=================================================================================================
	// Tests of getAuthorizedPublishers
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetAuthorizedPublishersOk() {
		final SystemRequestDTO request = getSystemRequestDTO();
		final UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString("authSubscriptionCheckUri");
 		final UriComponents checkUri = uriBuilder.build();
		final SystemResponseDTO subscriber = getSystemResponseDTO("SubscriberSystemName");
		final Set<SystemResponseDTO> publishers = getSystemResponseDTOSet(7);
		final AuthorizationSubscriptionCheckResponseDTO httpResponse = new AuthorizationSubscriptionCheckResponseDTO(subscriber, publishers);
		
		when(httpService.sendRequest(eq(checkUri), eq(HttpMethod.POST), eq(AuthorizationSubscriptionCheckResponseDTO.class), any(AuthorizationSubscriptionCheckRequestDTO.class))).
																							thenReturn( new ResponseEntity<AuthorizationSubscriptionCheckResponseDTO>(httpResponse, HttpStatus.OK));
		when(arrowheadContext.containsKey(AUTH_SUBSCRIPTION_CHECK_URI_KEY)).thenReturn(true);
		when(arrowheadContext.get(AUTH_SUBSCRIPTION_CHECK_URI_KEY)).thenReturn(checkUri);
	
		final Set<SystemResponseDTO> response = eventHandlerDriver.getAuthorizedPublishers(request);
		
		verify(httpService, times(1)).sendRequest(eq(checkUri),	eq(HttpMethod.POST), eq(AuthorizationSubscriptionCheckResponseDTO.class), any(AuthorizationSubscriptionCheckRequestDTO.class));
		verify(arrowheadContext, times(1)).containsKey(anyString());
		verify(arrowheadContext, times(1)).get(anyString());
		assertNotNull(response);
		assertFalse(response.isEmpty());
		assertTrue(response.size() == 7);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testGetAuthorizedPublishersContexNotContainsKey() {
		final SystemRequestDTO request = getSystemRequestDTO();
		final UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString("authSubscriptionCheckUri");
 		final UriComponents checkUri = uriBuilder.build();
		final SystemResponseDTO subscriber = getSystemResponseDTO("SubscriberSystemName");
		final Set<SystemResponseDTO> publishers = getSystemResponseDTOSet(7);
		final AuthorizationSubscriptionCheckResponseDTO httpResponse = new AuthorizationSubscriptionCheckResponseDTO(subscriber, publishers);
		
		when(httpService.sendRequest(eq(checkUri), eq(HttpMethod.POST), eq(AuthorizationSubscriptionCheckResponseDTO.class), any(AuthorizationSubscriptionCheckRequestDTO.class))).
																								thenReturn(new ResponseEntity<AuthorizationSubscriptionCheckResponseDTO>(httpResponse, HttpStatus.OK));
		when(arrowheadContext.containsKey( AUTH_SUBSCRIPTION_CHECK_URI_KEY)).thenReturn(false);
		when(arrowheadContext.get( AUTH_SUBSCRIPTION_CHECK_URI_KEY)).thenReturn(checkUri);
	
		try {
			eventHandlerDriver.getAuthorizedPublishers(request);
		} catch (final Exception ex) {
			verify(httpService, never()).sendRequest(eq(checkUri), eq(HttpMethod.POST),	eq(AuthorizationSubscriptionCheckResponseDTO.class), any(AuthorizationSubscriptionCheckRequestDTO.class));
			verify(arrowheadContext, times(1)).containsKey(anyString());
			verify( arrowheadContext, never()).get(anyString());
			assertTrue(ex.getMessage().contains("EventHandler can't find subscription authorization check URI."));
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testGetAuthorizedPublishersCanNotCastContextValue() {
		final SystemRequestDTO request = getSystemRequestDTO();
		final UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString("authSubscriptionCheckUri");
 		final UriComponents checkUri = uriBuilder.build();
		final SystemResponseDTO subscriber = getSystemResponseDTO("SubscriberSystemName");
		final Set<SystemResponseDTO> publishers = getSystemResponseDTOSet(7);
		final AuthorizationSubscriptionCheckResponseDTO httpResponse = new AuthorizationSubscriptionCheckResponseDTO(subscriber, publishers);
		
		when(httpService.sendRequest(eq(checkUri), eq(HttpMethod.POST), eq(AuthorizationSubscriptionCheckResponseDTO.class), any(AuthorizationSubscriptionCheckRequestDTO.class))).
																								thenReturn(new ResponseEntity<AuthorizationSubscriptionCheckResponseDTO>(httpResponse, HttpStatus.OK));
		when(arrowheadContext.containsKey(AUTH_SUBSCRIPTION_CHECK_URI_KEY)).thenReturn(true);
		when(arrowheadContext.get(AUTH_SUBSCRIPTION_CHECK_URI_KEY)).thenReturn(Exception.class);
	
		try {
			eventHandlerDriver.getAuthorizedPublishers(request);
		} catch (final ClassCastException ex) {
			verify(httpService, never()).sendRequest(eq(checkUri), eq(HttpMethod.POST), eq(AuthorizationSubscriptionCheckResponseDTO.class), any(AuthorizationSubscriptionCheckRequestDTO.class));
			verify(arrowheadContext, times(1)).containsKey(anyString());
			verify(arrowheadContext, times(1)).get(anyString());
			assertTrue(ex.getMessage().contains("EventHandler can't find subscription authorization check URI."));
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testGetAuthorizedPublishersRequestNull() {
		final SystemRequestDTO request = null;
		final UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString("authSubscriptionCheckUri");
 		final UriComponents checkUri = uriBuilder.build();
		final SystemResponseDTO subscriber = getSystemResponseDTO("SubscriberSystemName");
		final Set<SystemResponseDTO> publishers = getSystemResponseDTOSet(7);
		final AuthorizationSubscriptionCheckResponseDTO httpResponse = new AuthorizationSubscriptionCheckResponseDTO(subscriber, publishers);
		
		when(httpService.sendRequest(eq(checkUri), eq(HttpMethod.POST), eq(AuthorizationSubscriptionCheckResponseDTO.class), any(AuthorizationSubscriptionCheckRequestDTO.class))).
																								thenReturn(new ResponseEntity<AuthorizationSubscriptionCheckResponseDTO>(httpResponse, HttpStatus.OK));
		when(arrowheadContext.containsKey(AUTH_SUBSCRIPTION_CHECK_URI_KEY)).thenReturn(true);
		when(arrowheadContext.get(AUTH_SUBSCRIPTION_CHECK_URI_KEY)).thenReturn(checkUri);
	
		try {
			eventHandlerDriver.getAuthorizedPublishers(request);
		} catch (final Exception ex) {
			verify(httpService, never()).sendRequest(eq(checkUri), eq(HttpMethod.POST), eq(AuthorizationSubscriptionCheckResponseDTO.class), any(AuthorizationSubscriptionCheckRequestDTO.class));
			verify(arrowheadContext, never()).containsKey(anyString());
			verify(arrowheadContext, never()).get(anyString());
			assertTrue(ex.getMessage().contains("subscriberSystem is null."));
			throw ex;
		}
	}
	
	//=================================================================================================
	// Tests of publishEvent
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testPublishEventOk() {
		ReflectionTestUtils.setField(eventHandlerDriver, "timeStampTolerance", 120);
		ReflectionTestUtils.setField(eventHandlerDriver, "httpService", httpService);
		
		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();
		final Set<Subscription> involvedSubscriptions = Set.of(createSubscriptionForDBMock(1, "eventType", "subscriberName"));

		try {
			doNothing().when(publishingQueue).put(any());
		} catch (final InterruptedException ex) {
			fail();
		}
		
		eventHandlerDriver.publishEvent(request, involvedSubscriptions);
		
		try {
			verify(publishingQueue, times(1)).put(any());
		} catch (final InterruptedException e) {
			fail();
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testPublishEventInalidParameterRequestNull() {
		ReflectionTestUtils.setField(eventHandlerDriver, "timeStampTolerance", 120);
		ReflectionTestUtils.setField(eventHandlerDriver, "httpService", httpService);
		
		final EventPublishRequestDTO request = null;
		final Set<Subscription> involvedSubscriptions = Set.of(createSubscriptionForDBMock(1, "eventType", "subscriberName"));

		try {
			doNothing().when(publishingQueue).put(any());
		} catch (final InterruptedException ex) {
			fail();
		}
		
		try {
			eventHandlerDriver.publishEvent(request, involvedSubscriptions);
		} catch (final Exception ex) {
			assertTrue( ex.getMessage().contains("EventPublishRequestDTO is null."));
			
			try {
				verify(publishingQueue, never()).put(any());
			} catch (final InterruptedException iex) {
				fail();
			}
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testPublishEventInalidParameterEventTypeNull() {
		ReflectionTestUtils.setField(eventHandlerDriver, "timeStampTolerance", 120);
		ReflectionTestUtils.setField(eventHandlerDriver, "httpService", httpService);
		
		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();
		request.setEventType(null);
		final Set<Subscription> involvedSubscriptions = Set.of(createSubscriptionForDBMock(1, "eventType", "subscriberName"));

		try {
			doNothing().when(publishingQueue).put(any());
		} catch (final InterruptedException ex) {
			fail();
		}
		
		try {
			eventHandlerDriver.publishEvent(request, involvedSubscriptions);
		} catch (final Exception ex) {
			assertTrue(ex.getMessage().contains("EventType is null or blank."));
			
			try {
				verify(publishingQueue, never()).put(any());
			} catch (final InterruptedException iex) {
				fail();
			}
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testPublishEventInalidParameterEventTypeEmpty() {
		ReflectionTestUtils.setField(eventHandlerDriver, "timeStampTolerance", 120);
		ReflectionTestUtils.setField(eventHandlerDriver, "httpService", httpService);
		
		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();
		request.setEventType("   ");
		final Set<Subscription> involvedSubscriptions = Set.of(createSubscriptionForDBMock(1, "eventType", "subscriberName"));

		try {
			doNothing().when(publishingQueue).put(any());
		} catch (final InterruptedException ex) {
			fail();
		}
		
		try {
			eventHandlerDriver.publishEvent(request, involvedSubscriptions);
		} catch (final Exception ex) {
			assertTrue(ex.getMessage().contains("EventType is null or blank."));
			
			try {
				verify(publishingQueue, never()).put(any());
			} catch (final InterruptedException iex) {
				fail();
			}
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testPublishEventInalidParameterPayloadNull() {
		ReflectionTestUtils.setField(eventHandlerDriver, "timeStampTolerance", 120);
		ReflectionTestUtils.setField(eventHandlerDriver, "httpService", httpService);
		
		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();
		request.setPayload(null);
		final Set<Subscription> involvedSubscriptions = Set.of(createSubscriptionForDBMock(1, "eventType", "subscriberName"));

		try {
			doNothing().when(publishingQueue).put(any());
		} catch (final InterruptedException ex) {
			fail();
		}
		
		try {
			eventHandlerDriver.publishEvent(request, involvedSubscriptions);
		} catch (final Exception ex) {
			assertTrue(ex.getMessage().contains("Payload is null or blank."));
			
			try {
				verify(publishingQueue, never()).put(any());
			} catch (final InterruptedException iex) {
				fail();
			}
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testPublishEventInalidParameterPayloadEmpty() {
		ReflectionTestUtils.setField(eventHandlerDriver, "timeStampTolerance", 120);
		ReflectionTestUtils.setField(eventHandlerDriver, "httpService", httpService);
		
		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();
		request.setPayload("   ");
		final Set<Subscription> involvedSubscriptions = Set.of(createSubscriptionForDBMock(1, "eventType", "subscriberName" ));

		try {
			doNothing().when(publishingQueue).put(any());
		} catch (final InterruptedException ex) {
			fail();
		}
		
		try {
			eventHandlerDriver.publishEvent(request, involvedSubscriptions);
		} catch (final Exception ex) {
			assertTrue(ex.getMessage().contains("Payload is null or blank."));
			
			try {
				verify(publishingQueue, never()).put(any());
			} catch (final InterruptedException iex) {
				fail();
			}
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testPublishEventInalidParameterTimeStampNull() {
		ReflectionTestUtils.setField(eventHandlerDriver, "timeStampTolerance", 120);
		ReflectionTestUtils.setField(eventHandlerDriver, "httpService", httpService);
		
		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();
		request.setTimeStamp(null);
		final Set<Subscription> involvedSubscriptions = Set.of(createSubscriptionForDBMock(1, "eventType", "subscriberName"));

		try {
			doNothing().when(publishingQueue).put(any());
		} catch (final InterruptedException ex) {
			fail();
		}
		
		try {
			eventHandlerDriver.publishEvent(request, involvedSubscriptions);
		} catch (final Exception ex) {
			assertTrue(ex.getMessage().contains("TimeStamp is null or blank."));
			
			try {
				verify(publishingQueue, never()).put(any());
			} catch (final InterruptedException iex) {
				fail();
			}
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testPublishEventInalidParameterTimeStampEmpty() {
		ReflectionTestUtils.setField(eventHandlerDriver, "timeStampTolerance", 120);
		ReflectionTestUtils.setField(eventHandlerDriver, "httpService", httpService);
		
		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();
		request.setTimeStamp("   ");
		final Set<Subscription> involvedSubscriptions = Set.of(createSubscriptionForDBMock(1, "eventType", "subscriberName"));

		try {
			doNothing().when(publishingQueue).put(any());
		} catch (final InterruptedException ex) {
			fail();
		}
		
		try {
			eventHandlerDriver.publishEvent(request, involvedSubscriptions);
		} catch (final Exception ex) {
			assertTrue(ex.getMessage().contains("TimeStamp is null or blank."));
			
			try {
				verify(publishingQueue, never()).put(any());
			} catch (final InterruptedException iex) {
				fail();
			}
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testPublishEventInalidParameterTimeStampWrongFormat() {
		ReflectionTestUtils.setField(eventHandlerDriver, "timeStampTolerance", 120);
		ReflectionTestUtils.setField(eventHandlerDriver, "httpService", httpService);
		
		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();
		request.setTimeStamp("2019_02_04 12:12:12");
		final Set<Subscription> involvedSubscriptions = Set.of(createSubscriptionForDBMock(1, "eventType", "subscriberName"));

		try {
			doNothing().when(publishingQueue).put(any());
		} catch (final InterruptedException ex) {
			fail();
		}
		
		try {
			eventHandlerDriver.publishEvent(request, involvedSubscriptions);
		} catch (final Exception ex) {
			assertTrue(ex.getMessage().contains("TimeStamp is not valid."));
			
			try {
				verify(publishingQueue, never()).put(any());
			} catch (final InterruptedException iex) {
				fail();
			}
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testPublishEventInalidParameterTimeStampInFuture() {
		ReflectionTestUtils.setField(eventHandlerDriver, "timeStampTolerance", 120);
		ReflectionTestUtils.setField(eventHandlerDriver, "httpService", httpService);
		
		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();
		request.setTimeStamp("3019-09-27T09:40:34Z");
		final Set<Subscription> involvedSubscriptions = Set.of(createSubscriptionForDBMock(1, "eventType", "subscriberName"));

		try {
			doNothing().when(publishingQueue).put(any());
		} catch (final InterruptedException ex) {
			fail();
		}
		
		try {
			eventHandlerDriver.publishEvent(request, involvedSubscriptions);
		} catch (final Exception ex) {
			assertTrue(ex.getMessage().contains("TimeStamp is further in the future than the tolerated time difference"));
			
			try {
				verify(publishingQueue, never()).put(any());
			} catch (final InterruptedException iex) {
				fail();
			}
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testPublishEventInalidParameterTimeStampInPast() {
		ReflectionTestUtils.setField(eventHandlerDriver, "timeStampTolerance", 120);
		ReflectionTestUtils.setField(eventHandlerDriver, "httpService", httpService);
		
		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();
		request.setTimeStamp("1019-09-27T09:40:34Z");
		final Set<Subscription> involvedSubscriptions = Set.of(createSubscriptionForDBMock(1, "eventType", "subscriberName"));

		try {
			doNothing().when(publishingQueue).put(any());
		} catch (final InterruptedException iex) {
			fail();
		}
		
		try {
			eventHandlerDriver.publishEvent(request, involvedSubscriptions);
		} catch (final Exception ex) {
			assertTrue( ex.getMessage().contains("TimeStamp is further in the past than the tolerated time difference"));
			
			try {
				verify(publishingQueue, never()).put(any());
			} catch (final InterruptedException iex) {
				fail();
			}
			
			throw ex;
		}
		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testPublishEventInalidParameterSourceNull() {
		ReflectionTestUtils.setField(eventHandlerDriver, "timeStampTolerance", 120);
		ReflectionTestUtils.setField(eventHandlerDriver, "httpService", httpService);
		
		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();
		request.setSource(null);
		final Set<Subscription> involvedSubscriptions = Set.of(createSubscriptionForDBMock(1, "eventType", "subscriberName"));

		try {
			
			doNothing().when(publishingQueue).put(any());
		} catch (final InterruptedException ex) {
			fail();
		}
		
		try {
			eventHandlerDriver.publishEvent(request, involvedSubscriptions);
		} catch (final Exception ex) {
			assertTrue(ex.getMessage().contains("System is null."));
			
			try {
				verify(publishingQueue, never()).put(any());
			} catch (final InterruptedException iex) {
				fail();
			}
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testPublishEventInalidParameterSourceSystemNameNull() {
		ReflectionTestUtils.setField(eventHandlerDriver, "timeStampTolerance", 120);
		ReflectionTestUtils.setField(eventHandlerDriver, "httpService", httpService);
		
		final SystemRequestDTO systemRequestDTO = getSystemRequestDTO();
		systemRequestDTO.setSystemName(null);
		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();
		request.setSource(systemRequestDTO);
		final Set<Subscription> involvedSubscriptions = Set.of(createSubscriptionForDBMock(1, "eventType", "subscriberName"));

		try {
			doNothing().when(publishingQueue).put(any());
		} catch (final InterruptedException ex) {
			fail();
		}
		
		try {
			eventHandlerDriver.publishEvent(request, involvedSubscriptions);
		} catch (final Exception ex) {
			assertTrue(ex.getMessage().contains("System name is null or blank."));
			
			try {
				verify(publishingQueue, never()).put(any());
			} catch (final InterruptedException iex) {
				fail();
			}
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testPublishEventInalidParameterSourceSystemNameEmpty() {
		ReflectionTestUtils.setField(eventHandlerDriver, "timeStampTolerance", 120);
		ReflectionTestUtils.setField(eventHandlerDriver, "httpService", httpService);
		
		final SystemRequestDTO systemRequestDTO = getSystemRequestDTO();
		systemRequestDTO.setSystemName("  ");
		
		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();
		request.setSource(systemRequestDTO);
		final Set<Subscription> involvedSubscriptions = Set.of(createSubscriptionForDBMock(1, "eventType", "subscriberName"));

		try {
			doNothing().when(publishingQueue).put(any());
		} catch (final InterruptedException ex) {
			fail();
		}
		
		try {
			eventHandlerDriver.publishEvent(request, involvedSubscriptions);
		} catch (final Exception ex) {
			assertTrue(ex.getMessage().contains("System name is null or blank."));
			
			try {
				verify(publishingQueue, never()).put(any());
			} catch (final InterruptedException iex) {
				fail();
			}
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testPublishEventInalidParameterSourceSystemAddressNull() {
		ReflectionTestUtils.setField(eventHandlerDriver, "timeStampTolerance", 120);
		ReflectionTestUtils.setField(eventHandlerDriver, "httpService", httpService);
		
		final SystemRequestDTO systemRequestDTO = getSystemRequestDTO();
		systemRequestDTO.setAddress(null);
		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();
		request.setSource(systemRequestDTO);
		final Set<Subscription> involvedSubscriptions = Set.of(createSubscriptionForDBMock(1, "eventType", "subscriberName"));

		try {
			doNothing().when(publishingQueue).put(any());
		} catch (final InterruptedException ex) {
			fail();
		}
		
		try {
			eventHandlerDriver.publishEvent(request, involvedSubscriptions);
		} catch (final Exception ex) {
			assertTrue(ex.getMessage().contains("System address is null or blank."));
			
			try {
				verify(publishingQueue, never()).put(any());
			} catch (final InterruptedException iex) {
				fail();
			}
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testPublishEventInalidParameterSourceSystemAddressEmpty() {
		ReflectionTestUtils.setField(eventHandlerDriver, "timeStampTolerance", 120);
		ReflectionTestUtils.setField(eventHandlerDriver, "httpService", httpService);
		
		final SystemRequestDTO systemRequestDTO = getSystemRequestDTO();
		systemRequestDTO.setAddress("  ");
		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();
		request.setSource(systemRequestDTO);
		final Set<Subscription> involvedSubscriptions = Set.of(createSubscriptionForDBMock(1, "eventType", "subscriberName"));

		try {
			doNothing().when(publishingQueue).put(any());
		} catch (final InterruptedException ex) {
			fail();
		}
		
		try {
			eventHandlerDriver.publishEvent(request, involvedSubscriptions);
		} catch (final Exception ex) {
			assertTrue(ex.getMessage().contains("System address is null or blank."));
			
			try {
				verify(publishingQueue, never()).put(any());
			} catch (final InterruptedException iex) {
				fail();
			}
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testPublishEventInalidParameterSourceSystemPortNull() {
		ReflectionTestUtils.setField(eventHandlerDriver, "timeStampTolerance", 120);
		ReflectionTestUtils.setField(eventHandlerDriver, "httpService", httpService);
		
		final SystemRequestDTO systemRequestDTO = getSystemRequestDTO();
		systemRequestDTO.setPort(null);
		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();
		request.setSource(systemRequestDTO);
		final Set<Subscription> involvedSubscriptions = Set.of(createSubscriptionForDBMock(1, "eventType", "subscriberName"));

		try {
			doNothing().when(publishingQueue).put(any());
		} catch (final InterruptedException ex) {
			fail();
		}
		
		try {
			eventHandlerDriver.publishEvent(request, involvedSubscriptions);
		} catch (final Exception ex) {
			assertTrue(ex.getMessage().contains("System port is null."));
			
			try {
				verify(publishingQueue, never()).put(any());
			} catch (final InterruptedException iex) {
				fail();
			}
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testPublishEventInalidParameterSourceSystemPortLessThanOne() {
		ReflectionTestUtils.setField(eventHandlerDriver, "timeStampTolerance", 120);
		ReflectionTestUtils.setField(eventHandlerDriver, "httpService", httpService);
		
		final SystemRequestDTO systemRequestDTO = getSystemRequestDTO();
		systemRequestDTO.setPort(0);
		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();
		request.setSource(systemRequestDTO);
		final Set<Subscription> involvedSubscriptions = Set.of(createSubscriptionForDBMock(1, "eventType", "subscriberName"));

		try {
			doNothing().when(publishingQueue).put(any());
		} catch (final InterruptedException ex) {
			fail();
		}
		
		try {
			eventHandlerDriver.publishEvent(request, involvedSubscriptions);
		} catch (final Exception ex) {
			assertTrue(ex.getMessage().contains("System port is less than one."));
			
			try {
				verify(publishingQueue, never()).put(any());
			} catch (final InterruptedException iex) {
				fail();
			}
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testPublishEventInalidParameterInvolvedSubscriptionsNull() {
		ReflectionTestUtils.setField(eventHandlerDriver, "timeStampTolerance", 120);
		ReflectionTestUtils.setField(eventHandlerDriver, "httpService", httpService);
		
		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();
		final Set<Subscription> involvedSubscriptions = null;

		try {
			doNothing().when(publishingQueue).put(any());
		} catch (final InterruptedException ex) {
			fail();
		}
		
		try {
			eventHandlerDriver.publishEvent(request, involvedSubscriptions);
		} catch (final Exception ex) {
			assertTrue(ex.getMessage().contains("involvedSubscriptions is null."));
			
			try {
				verify(publishingQueue, never()).put(any());
			} catch (final InterruptedException iex) {
				fail();
			}
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testPublishEventInalidParameterInvolvedSubscriptionsEmpty() {
		ReflectionTestUtils.setField(eventHandlerDriver, "timeStampTolerance", 120);
		ReflectionTestUtils.setField(eventHandlerDriver, "httpService", httpService);
		
		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();
		final Set<Subscription> involvedSubscriptions = Set.of();

		try {
			doNothing().when(publishingQueue).put(any());
		} catch (final InterruptedException ex) {
			fail();
		}
		
		try {
			eventHandlerDriver.publishEvent(request, involvedSubscriptions);
		} catch (final Exception ex) {
			assertTrue(ex.getMessage().contains("involvedSubscriptions is empty."));
			
			try {
				verify(publishingQueue, never()).put(any());
			} catch (final InterruptedException iex) {
				fail();
			}
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testPublishEventInterruptedExceptionOk() {
		ReflectionTestUtils.setField(eventHandlerDriver, "timeStampTolerance", 120);
		ReflectionTestUtils.setField(eventHandlerDriver, "httpService", httpService);
		
		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();
		final Set<Subscription> involvedSubscriptions = Set.of(createSubscriptionForDBMock(1, "eventType", "subscriberName"));

		final ArgumentCaptor<String> valueCapture = ArgumentCaptor.forClass(String.class);
		try {
			doThrow(InterruptedException.class).when(publishingQueue).put(any());
		} catch (final InterruptedException ex) {
			assertNotNull(ex);
		}
		
		doNothing().when(logger).debug(valueCapture.capture());
		
		eventHandlerDriver.publishEvent(request, involvedSubscriptions);
		
		try {
			verify(publishingQueue, times(1)).put(any());
		} catch (final InterruptedException ex) {
			fail();
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	@Ignore
	public void testPublishEventReturnsUnder10Milliseconds() {
		ReflectionTestUtils.setField(eventHandlerDriver, "timeStampTolerance", 120);
		ReflectionTestUtils.setField(eventHandlerDriver, "httpService", httpService);
		
		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();
		final Set<Subscription> involvedSubscriptionsLargeSet = createLargeSetOfSubscriptions(100000);
		final Set<Subscription> involvedSubscriptionsSmallSet = createLargeSetOfSubscriptions(10);
		
		final List<Integer> notValidTimeDifferents = new ArrayList<>();
		final List<Integer> validTimeDifferents = new ArrayList<>();
		for (int i = 0; i < 1000; ++i) {
			final long beforeLarge = java.lang.System.currentTimeMillis();
			eventHandlerDriver.publishEvent(request, involvedSubscriptionsLargeSet);
			final long afterLarge = java.lang.System.currentTimeMillis();
			final long diffInLargeSet = afterLarge - beforeLarge;
			
			final long beforeSmall = java.lang.System.currentTimeMillis();
			eventHandlerDriver.publishEvent(request, involvedSubscriptionsSmallSet);
			final long afterSmall = java.lang.System.currentTimeMillis();
			final long diffInSmallSet = afterSmall - beforeSmall;
			
			final int x = (int) (diffInLargeSet - diffInSmallSet);
			if (x > 10) {
				notValidTimeDifferents.add(x);
			} else {
				validTimeDifferents.add(x);
			}
		}
		
		assertTrue(notValidTimeDifferents.isEmpty() || notValidTimeDifferents.size() < 100);
		assertFalse(validTimeDifferents.isEmpty());
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
		
		return  eventTypeFromDB ;		
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
	private System createSystemForDBMock( final String systemName) {
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
}