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
import static org.mockito.ArgumentMatchers.eq;
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
import org.mockito.Mock;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.util.UriComponents;

import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.entity.EventType;
import eu.arrowhead.common.database.entity.Subscription;
import eu.arrowhead.common.database.entity.System;
import eu.arrowhead.common.dto.shared.EventDTO;
import eu.arrowhead.common.dto.shared.EventPublishRequestDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.common.http.HttpService;

@RunWith(SpringRunner.class)
public class PublishEventTaskTest {
	
	//=================================================================================================
	// members
	
	private PublishEventTask testingObject;
	
	private Logger logger;
	
	@Mock
	private HttpService httpService;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
    @Before
    public void setUp() throws Exception {
		final EventPublishRequestDTO request = getEventPublishRequestDTOForTest();
		final Subscription subscription = createSubscriptionForDBMock(1, "eventType", "subscriberName");

		testingObject = new PublishEventTask(subscription, request, httpService);
		
		logger = mock(Logger.class);		
		ReflectionTestUtils.setField(testingObject, "logger", logger);
    }
	
	//=================================================================================================
	// Tests of run
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRunOk() {
		final ArgumentCaptor<String> debugValueCapture = ArgumentCaptor.forClass(String.class);
		final ArgumentCaptor<String> traceValueCapture = ArgumentCaptor.forClass(String.class);
		final ArgumentCaptor<UriComponents> uriValueCapture = ArgumentCaptor.forClass(UriComponents.class);
		
		doNothing().when(logger).debug(debugValueCapture.capture());
		doNothing().when(logger).trace(traceValueCapture.capture());

		when(httpService.sendRequest(uriValueCapture.capture(),	eq(HttpMethod.POST), eq(Void.class), any(EventDTO.class))).thenReturn(new ResponseEntity<Void>(HttpStatus.OK));
		testingObject.run();
			
		verify(logger, times(2)).debug(any(String.class));
		verify(httpService, times(1)).sendRequest(any(UriComponents.class), eq(HttpMethod.POST), eq(Void.class), any(EventDTO.class));
		
		final List<String> debugMessages = debugValueCapture.getAllValues();
		final UriComponents uri = uriValueCapture.getValue();
		
		assertNotNull(debugMessages);
		assertTrue("PublishEventTask.run started...".equalsIgnoreCase(debugMessages.get(0)));
		assertTrue("getSubscriptionUri started...".equalsIgnoreCase(debugMessages.get(1)));
		
		assertNotNull(uri);
		assertTrue("http://localhost:12345/notifyUri".equalsIgnoreCase(uri.toUriString()));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRunIfSubscriberAuthInfoIsFilledUriSchemeIsHttps() {
		final Subscription subscriptionWithAuthInfo = createSubscriptionForDBMock(1, "eventType", "subscriberName");
		subscriptionWithAuthInfo.getSubscriberSystem().setAuthenticationInfo("authenticationInfo");
		
		ReflectionTestUtils.setField(testingObject, "subscription", subscriptionWithAuthInfo);
		
		final ArgumentCaptor<String> debugValueCapture = ArgumentCaptor.forClass(String.class);
		final ArgumentCaptor<String> traceValueCapture = ArgumentCaptor.forClass(String.class);
		final ArgumentCaptor<UriComponents> uriValueCapture = ArgumentCaptor.forClass(UriComponents.class);
		
		doNothing().when(logger).debug(debugValueCapture.capture());
		doNothing().when(logger).trace(traceValueCapture.capture());

		when(httpService.sendRequest(uriValueCapture.capture(), eq(HttpMethod.POST), eq(Void.class), any(EventDTO.class))).thenReturn(new ResponseEntity<Void>(HttpStatus.OK));
		testingObject.run();
			
		verify(logger, times(2)).debug(any(String.class));
		verify(httpService, times(1)).sendRequest(any(UriComponents.class),	eq(HttpMethod.POST), eq(Void.class), any(EventDTO.class));
		
		final List<String> debugMessages = debugValueCapture.getAllValues();
		final UriComponents uri = uriValueCapture.getValue();
		
		assertNotNull(debugMessages);
		assertTrue("PublishEventTask.run started...".equalsIgnoreCase(debugMessages.get(0)));
		assertTrue("getSubscriptionUri started...".equalsIgnoreCase(debugMessages.get(1)));
		
		assertNotNull(uri);
		assertTrue("https".equalsIgnoreCase(uri.getScheme()));
		assertTrue("https://localhost:12345/notifyUri".equalsIgnoreCase(uri.toUriString()));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRunIfSubscriptionIsNullMethodLogsAndReturn() {
		final Subscription subscription = null;
		
		ReflectionTestUtils.setField(testingObject, "subscription", subscription);
		
		final ArgumentCaptor<String> debugValueCapture = ArgumentCaptor.forClass(String.class);
		final ArgumentCaptor<Object> errorMessageValueCapture = ArgumentCaptor.forClass(Object.class);
		final ArgumentCaptor<String> traceValueCapture = ArgumentCaptor.forClass(String.class);

		doNothing().when(logger).debug(debugValueCapture.capture());
		doNothing().when(logger).debug(debugValueCapture.capture(), errorMessageValueCapture.capture());
		doNothing().when(logger).trace(traceValueCapture.capture());

		testingObject.run();
		
		verify(httpService, never() ).sendRequest(any(UriComponents.class), eq(HttpMethod.POST), eq(Void.class), any(EventDTO.class));
		verify(logger, never()).trace(any(String.class));
		verify(logger, times(1)).debug(any(String.class));
		verify(logger, times(1)).debug(any(String.class), any(Object.class));
		
		final List<String> debugMessages = debugValueCapture.getAllValues();
		final String errorMessage = (String) errorMessageValueCapture.getValue();
		
		assertNotNull(debugMessages);
		assertTrue("PublishEventTask.run started...".equalsIgnoreCase(debugMessages.get(0)));
		assertTrue("Exception:".equalsIgnoreCase(debugMessages.get(1)));
		assertTrue("subscription is null".equalsIgnoreCase(errorMessage));
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRunIfSubscriptionSubscriberSystemIsNullMethodLogsAndReturn() {
		final Subscription subscription = createSubscriptionForDBMock(1, "eventType", "subscriberName");
		subscription.setSubscriberSystem(null);

		ReflectionTestUtils.setField(testingObject, "subscription", subscription);
		
		final ArgumentCaptor<String> debugValueCapture = ArgumentCaptor.forClass(String.class);
		final ArgumentCaptor<Object> errorMessageValueCapture = ArgumentCaptor.forClass(Object.class);
		final ArgumentCaptor<String> traceValueCapture = ArgumentCaptor.forClass(String.class);

		doNothing().when(logger).debug(debugValueCapture.capture());
		doNothing().when(logger).debug(debugValueCapture.capture(), errorMessageValueCapture.capture());
		doNothing().when(logger).trace(traceValueCapture.capture());

		testingObject.run();
		
		verify(httpService, never()).sendRequest(any(UriComponents.class), eq(HttpMethod.POST), eq(Void.class), any(EventDTO.class));
		verify(logger, never()).trace(any(String.class));
		verify(logger, times(1)).debug(any(String.class));
		verify(logger, times(1)).debug(any(String.class), any(Object.class));
		
		final List<String> debugMessages = debugValueCapture.getAllValues();
		final String errorMessage = (String) errorMessageValueCapture.getValue();
		
		assertNotNull(debugMessages);
		assertTrue("PublishEventTask.run started...".equalsIgnoreCase(debugMessages.get(0)));
		assertTrue("Exception:".equalsIgnoreCase(debugMessages.get(1)));
		assertTrue("subscription.SubscriberSystem is null".equalsIgnoreCase(errorMessage));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRunIfHttpServiceIsNullMethodLogsAndReturn() {
		final HttpService nullHttpService = null;
		
		ReflectionTestUtils.setField(testingObject, "httpService", nullHttpService);
		
		final ArgumentCaptor<String> debugValueCapture = ArgumentCaptor.forClass(String.class);
		final ArgumentCaptor<Object> errorMessageValueCapture = ArgumentCaptor.forClass(Object.class);
		final ArgumentCaptor<String> traceValueCapture = ArgumentCaptor.forClass(String.class);

		doNothing().when(logger).debug(debugValueCapture.capture());
		doNothing().when(logger).debug(debugValueCapture.capture(), errorMessageValueCapture.capture());
		doNothing().when(logger).trace(traceValueCapture.capture());

		testingObject.run();
		
		verify(httpService, never()).sendRequest(any(UriComponents.class),	eq(HttpMethod.POST), eq(Void.class), any(EventDTO.class));
		verify(logger, never()).trace(any(String.class));
		verify(logger, times(1)).debug(any(String.class));
		verify(logger, times(1)).debug(any(String.class), any(Object.class));
		
		final List<String> debugMessages = debugValueCapture.getAllValues();
		final String errorMessage = (String) errorMessageValueCapture.getValue();
		
		assertNotNull(debugMessages);
		assertTrue("PublishEventTask.run started...".equalsIgnoreCase(debugMessages.get(0)));
		assertTrue("Exception:".equalsIgnoreCase(debugMessages.get(1)));
		assertTrue("httpService is null".equalsIgnoreCase(errorMessage));
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
		return new EventPublishRequestDTO(
				"eventType", 
				getSystemRequestDTO(), // source, 
				null, // metaData, 
				"payload", 
				Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now().plusSeconds(1)));
	}
}