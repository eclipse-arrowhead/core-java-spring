/********************************************************************************
 * Copyright (c) 2020 AITIA
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

package eu.arrowhead.core.qos.quartz.task;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.util.UriComponents;

import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.entity.QoSIntraMeasurement;
import eu.arrowhead.common.database.entity.QoSIntraPingMeasurement;
import eu.arrowhead.common.database.entity.QoSIntraPingMeasurementLog;
import eu.arrowhead.common.database.entity.System;
import eu.arrowhead.common.dto.internal.DTOConverter;
import eu.arrowhead.common.dto.internal.ServiceRegistryListResponseDTO;
import eu.arrowhead.common.dto.shared.AddressType;
import eu.arrowhead.common.dto.shared.QoSMeasurementType;
import eu.arrowhead.common.dto.shared.ServiceDefinitionResponseDTO;
import eu.arrowhead.common.dto.shared.ServiceInterfaceResponseDTO;
import eu.arrowhead.common.dto.shared.ServiceRegistryResponseDTO;
import eu.arrowhead.common.dto.shared.ServiceSecurityType;
import eu.arrowhead.common.dto.shared.SystemResponseDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.common.http.HttpService;
import eu.arrowhead.core.qos.database.service.QoSDBService;
import eu.arrowhead.core.qos.dto.IcmpPingResponse;
import eu.arrowhead.core.qos.dto.PingMeasurementCalculationsDTO;
import eu.arrowhead.core.qos.measurement.properties.PingMeasurementProperties;
import eu.arrowhead.core.qos.service.PingService;

@RunWith(SpringRunner.class)
public class PingTaskTest {

	//=================================================================================================
	// members
	@InjectMocks
	private final PingTask pingTask = new PingTask();

	@Mock
	private PingService pingService;

	@Mock
	private QoSDBService qoSDBService;

	@Mock
	private HttpService httpService;

	@Mock
	private PingMeasurementProperties pingMeasurementProperties;

	@Mock
	private Map<String,Object> arrowheadContext;

	@Mock
	private JobExecutionContext jobExecutionContext;

	private Logger logger;

	final static int TIME_TO_REPEAT_PING = 3;

	private static final String NULL_OR_BLANK_PARAMETER_ERROR_MESSAGE = " is null or blank.";

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@Before
	public void setUp() throws Exception {
		logger = mock(Logger.class);
		ReflectionTestUtils.setField(pingTask, "logger", logger);

		when(pingMeasurementProperties.getLogMeasurementsToDB()).thenReturn(true);
		when(pingMeasurementProperties.getLogMeasurementsDetailsToDB()).thenReturn(true);
	}

	//=================================================================================================
	// Tests of execute

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testExecuteMeasurementIsInDB() {

		final UriComponents uri = Utilities.createURI("HTTPS", "localhost", 12345, "serviceregistry");

		final ServiceRegistryListResponseDTO serviceRegistryResponse = getServiceRegistryListResponseDTOForTest();
		final ResponseEntity<ServiceRegistryListResponseDTO> httpResponse = new ResponseEntity<ServiceRegistryListResponseDTO>(serviceRegistryResponse, HttpStatus.OK);

		final List<IcmpPingResponse> responseList = getResponseListForTest();

		final QoSIntraMeasurement measurement = getQoSIntraMeasurementForTest();

		final QoSIntraPingMeasurement pingMeasurement = getQosIntraPingMeasurementForTest();
		final QoSIntraPingMeasurementLog measurementLog = getOrCreateMeasurementLogForTest();

		final ArgumentCaptor<String> debugValueCapture = ArgumentCaptor.forClass(String.class);
		doNothing().when(logger).debug( debugValueCapture.capture());

		when(arrowheadContext.containsKey(CoreCommonConstants.SERVER_STANDALONE_MODE)).thenReturn(false);
		when(arrowheadContext.containsKey(CoreCommonConstants.SR_QUERY_ALL)).thenReturn(true);
		when(arrowheadContext.get(CoreCommonConstants.SR_QUERY_ALL)).thenReturn(uri);

		when(httpService.sendRequest(uri, HttpMethod.GET, ServiceRegistryListResponseDTO.class)).thenReturn(httpResponse);

		when(pingMeasurementProperties.getTimeout()).thenReturn(5000);

		when(pingService.getPingResponseList(anyString())).thenReturn(responseList);

		when(qoSDBService.getOrCreateIntraMeasurement(any(SystemResponseDTO.class), any())).thenReturn(measurement);

		//in handleMeasurement
		when(qoSDBService.getIntraPingMeasurementByMeasurement(any(QoSIntraMeasurement.class))).thenReturn(Optional.of(pingMeasurement));
		//when(qoSDBService.createPingMeasurement(any(), any(), any())).thenReturn(pingMeasurement);
		doNothing().when(qoSDBService).updateIntraPingMeasurement(any(), any(), any(), any());
		when(qoSDBService.logIntraMeasurementToDB(any(), any(), any())).thenReturn(measurementLog);
		doNothing().when(qoSDBService).logIntraMeasurementDetailsToDB(any(), any(), any());

		try {

			pingTask.execute(jobExecutionContext);

		} catch (final JobExecutionException ex) {
			fail();
		}

		verify(logger, atLeastOnce()).debug(any(String.class));
		final List<String> debugMessages = debugValueCapture.getAllValues();
		assertNotNull(debugMessages);

		verify(arrowheadContext, times(1)).containsKey(CoreCommonConstants.SERVER_STANDALONE_MODE);
		verify(arrowheadContext, times(1)).containsKey(CoreCommonConstants.SR_QUERY_ALL);
		verify(arrowheadContext, times(1)).get(CoreCommonConstants.SR_QUERY_ALL);
		verify(httpService, times(1)).sendRequest(any(), any(), any());
		verify(pingMeasurementProperties, times(1)).getTimeout();
		verify(pingService, atLeastOnce()).getPingResponseList(anyString());
		verify(qoSDBService, atLeastOnce()).getOrCreateIntraMeasurement(any(), any());

		verify(qoSDBService, atLeastOnce()).getIntraPingMeasurementByMeasurement(any());
		verify(qoSDBService, atLeastOnce()).updateIntraPingMeasurement(any(), any(), any(), any());
		verify(qoSDBService, atLeastOnce()).logIntraMeasurementToDB(any(), any(), any());
		verify(qoSDBService, atLeastOnce()).logIntraMeasurementDetailsToDB(any(), any(), any());
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testExecuteMeasurementIsNotInDB() {

		final UriComponents uri = Utilities.createURI("HTTPS", "localhost", 12345, "serviceregistry");

		final ServiceRegistryListResponseDTO serviceRegistryResponse = getServiceRegistryListResponseDTOForTest();
		final ResponseEntity<ServiceRegistryListResponseDTO> httpResponse = new ResponseEntity<ServiceRegistryListResponseDTO>(serviceRegistryResponse, HttpStatus.OK);

		final List<IcmpPingResponse> responseList = getResponseListForTest();

		final QoSIntraMeasurement measurement = getQoSIntraMeasurementForTest();

		final QoSIntraPingMeasurementLog measurementLog = getOrCreateMeasurementLogForTest();

		final ArgumentCaptor<String> debugValueCapture = ArgumentCaptor.forClass(String.class);
		doNothing().when(logger).debug( debugValueCapture.capture());

		when(arrowheadContext.containsKey(CoreCommonConstants.SERVER_STANDALONE_MODE)).thenReturn(false);
		when(arrowheadContext.containsKey(CoreCommonConstants.SR_QUERY_ALL)).thenReturn(true);
		when(arrowheadContext.get(CoreCommonConstants.SR_QUERY_ALL)).thenReturn(uri);

		when(httpService.sendRequest(uri, HttpMethod.GET, ServiceRegistryListResponseDTO.class)).thenReturn(httpResponse);

		when(pingMeasurementProperties.getTimeout()).thenReturn(5000);

		when(pingService.getPingResponseList(anyString())).thenReturn(responseList);

		when(qoSDBService.getOrCreateIntraMeasurement(any(SystemResponseDTO.class), any())).thenReturn(measurement);

		//in handleMeasurement
		when(qoSDBService.getIntraPingMeasurementByMeasurement(any(QoSIntraMeasurement.class))).thenReturn(Optional.ofNullable(null));
		doNothing().when(qoSDBService).createIntraPingMeasurement(any(), any(), any());
		//doNothing().when(qoSDBService).updatePingMeasurement(any(), any(), any(), any());
		when(qoSDBService.logIntraMeasurementToDB(any(), any(), any())).thenReturn(measurementLog);
		doNothing().when(qoSDBService).logIntraMeasurementDetailsToDB(any(), any(), any());

		try {

			pingTask.execute(jobExecutionContext);

		} catch (final JobExecutionException ex) {
			fail();
		}

		verify(logger, atLeastOnce()).debug(any(String.class));
		final List<String> debugMessages = debugValueCapture.getAllValues();
		assertNotNull(debugMessages);

		verify(arrowheadContext, times(1)).containsKey(CoreCommonConstants.SERVER_STANDALONE_MODE);
		verify(arrowheadContext, times(1)).containsKey(CoreCommonConstants.SR_QUERY_ALL);
		verify(arrowheadContext, times(1)).get(CoreCommonConstants.SR_QUERY_ALL);
		verify(httpService, times(1)).sendRequest(any(), any(), any());
		verify(pingMeasurementProperties, times(1)).getTimeout();
		verify(pingService, atLeastOnce()).getPingResponseList(anyString());
		verify(qoSDBService, atLeastOnce()).getOrCreateIntraMeasurement(any(), any());

		verify(qoSDBService, atLeastOnce()).getIntraPingMeasurementByMeasurement(any());
		verify(qoSDBService, atLeastOnce()).createIntraPingMeasurement(any(), any(), any());
		verify(qoSDBService, atLeastOnce()).logIntraMeasurementToDB(any(), any(), any());
		verify(qoSDBService, atLeastOnce()).logIntraMeasurementDetailsToDB(any(), any(), any());
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testExecuteWhenMultiplySystemHasSameAddressPingOnlyCalledOnce() {

		final UriComponents uri = Utilities.createURI("HTTPS", "localhost", 12345, "serviceregistry");

		final ServiceRegistryListResponseDTO serviceRegistryResponse = getServiceRegistryListResponseDTOForTest();
		final ResponseEntity<ServiceRegistryListResponseDTO> httpResponse = new ResponseEntity<ServiceRegistryListResponseDTO>(serviceRegistryResponse, HttpStatus.OK);

		final List<IcmpPingResponse> responseList = getResponseListForTest();

		final QoSIntraMeasurement measurement = getQoSIntraMeasurementForTest();

		final QoSIntraPingMeasurementLog measurementLog = getOrCreateMeasurementLogForTest();

		final ArgumentCaptor<String> debugValueCapture = ArgumentCaptor.forClass(String.class);
		doNothing().when(logger).debug( debugValueCapture.capture());

		when(arrowheadContext.containsKey(CoreCommonConstants.SERVER_STANDALONE_MODE)).thenReturn(false);
		when(arrowheadContext.containsKey(CoreCommonConstants.SR_QUERY_ALL)).thenReturn(true);
		when(arrowheadContext.get(CoreCommonConstants.SR_QUERY_ALL)).thenReturn(uri);

		when(httpService.sendRequest(uri, HttpMethod.GET, ServiceRegistryListResponseDTO.class)).thenReturn(httpResponse);

		when(pingMeasurementProperties.getTimeout()).thenReturn(5000);

		when(pingService.getPingResponseList(anyString())).thenReturn(responseList);

		when(qoSDBService.getOrCreateIntraMeasurement(any(SystemResponseDTO.class), any())).thenReturn(measurement);

		//in handleMeasurement
		when(qoSDBService.getIntraPingMeasurementByMeasurement(any(QoSIntraMeasurement.class))).thenReturn(Optional.ofNullable(null));
		doNothing().when(qoSDBService).createIntraPingMeasurement(any(), any(), any());
		//doNothing().when(qoSDBService).updatePingMeasurement(any(), any(), any(), any());
		when(qoSDBService.logIntraMeasurementToDB(any(), any(), any())).thenReturn(measurementLog);
		doNothing().when(qoSDBService).logIntraMeasurementDetailsToDB(any(), any(), any());

		try {

			pingTask.execute(jobExecutionContext);

		} catch (final JobExecutionException ex) {
			fail();
		}

		verify(logger, atLeastOnce()).debug(any(String.class));
		final List<String> debugMessages = debugValueCapture.getAllValues();
		assertNotNull(debugMessages);

		verify(arrowheadContext, times(1)).containsKey(CoreCommonConstants.SERVER_STANDALONE_MODE);
		verify(arrowheadContext, times(1)).containsKey(CoreCommonConstants.SR_QUERY_ALL);
		verify(arrowheadContext, times(1)).get(CoreCommonConstants.SR_QUERY_ALL);
		verify(httpService, times(1)).sendRequest(any(), any(), any());
		verify(pingMeasurementProperties, times(1)).getTimeout();
		verify(pingService, times(1)).getPingResponseList(anyString());
		verify(qoSDBService, atLeastOnce()).getOrCreateIntraMeasurement(any(), any());

		verify(qoSDBService, atLeastOnce()).getIntraPingMeasurementByMeasurement(any());
		verify(qoSDBService, atLeastOnce()).createIntraPingMeasurement(any(), any(), any());
		verify(qoSDBService, atLeastOnce()).logIntraMeasurementToDB(any(), any(), any());
		verify(qoSDBService, atLeastOnce()).logIntraMeasurementDetailsToDB(any(), any(), any());
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testExecuteWhenMultiplySystemHasDifferentAddressPingCalledMultiplyTimes() {

		final int NUMBER_OF_PROVIDERS = 10;

		final UriComponents uri = Utilities.createURI("HTTPS", "localhost", 12345, "serviceregistry");

		final ServiceRegistryListResponseDTO serviceRegistryResponse = getServiceRegistryListResponseDTOWithDifferentProviderAddressForTest(10);
		final ResponseEntity<ServiceRegistryListResponseDTO> httpResponse = new ResponseEntity<ServiceRegistryListResponseDTO>(serviceRegistryResponse, HttpStatus.OK);

		final List<IcmpPingResponse> responseList = getResponseListForTest();

		final QoSIntraMeasurement measurement = getQoSIntraMeasurementForTest();

		final QoSIntraPingMeasurementLog measurementLog = getOrCreateMeasurementLogForTest();

		final ArgumentCaptor<String> debugValueCapture = ArgumentCaptor.forClass(String.class);
		doNothing().when(logger).debug( debugValueCapture.capture());

		when(arrowheadContext.containsKey(CoreCommonConstants.SERVER_STANDALONE_MODE)).thenReturn(false);
		when(arrowheadContext.containsKey(CoreCommonConstants.SR_QUERY_ALL)).thenReturn(true);
		when(arrowheadContext.get(CoreCommonConstants.SR_QUERY_ALL)).thenReturn(uri);

		when(httpService.sendRequest(uri, HttpMethod.GET, ServiceRegistryListResponseDTO.class)).thenReturn(httpResponse);

		when(pingMeasurementProperties.getTimeout()).thenReturn(5000);

		when(pingService.getPingResponseList(anyString())).thenReturn(responseList);

		when(qoSDBService.getOrCreateIntraMeasurement(any(SystemResponseDTO.class), any())).thenReturn(measurement);

		//in handleMeasurement
		when(qoSDBService.getIntraPingMeasurementByMeasurement(any(QoSIntraMeasurement.class))).thenReturn(Optional.ofNullable(null));
		doNothing().when(qoSDBService).createIntraPingMeasurement(any(), any(), any());
		//doNothing().when(qoSDBService).updatePingMeasurement(any(), any(), any(), any());
		when(qoSDBService.logIntraMeasurementToDB(any(), any(), any())).thenReturn(measurementLog);
		doNothing().when(qoSDBService).logIntraMeasurementDetailsToDB(any(), any(), any());

		try {

			pingTask.execute(jobExecutionContext);

		} catch (final JobExecutionException ex) {
			fail();
		}

		verify(logger, atLeastOnce()).debug(any(String.class));
		final List<String> debugMessages = debugValueCapture.getAllValues();
		assertNotNull(debugMessages);

		verify(arrowheadContext, times(1)).containsKey(CoreCommonConstants.SERVER_STANDALONE_MODE);
		verify(arrowheadContext, times(1)).containsKey(CoreCommonConstants.SR_QUERY_ALL);
		verify(arrowheadContext, times(1)).get(CoreCommonConstants.SR_QUERY_ALL);
		verify(httpService, times(1)).sendRequest(any(), any(), any());
		verify(pingMeasurementProperties, times(NUMBER_OF_PROVIDERS)).getTimeout();
		verify(pingService, times(NUMBER_OF_PROVIDERS)).getPingResponseList(anyString());
		verify(qoSDBService, atLeastOnce()).getOrCreateIntraMeasurement(any(), any());

		verify(qoSDBService, atLeastOnce()).getIntraPingMeasurementByMeasurement(any());
		verify(qoSDBService, atLeastOnce()).createIntraPingMeasurement(any(), any(), any());
		verify(qoSDBService, atLeastOnce()).logIntraMeasurementToDB(any(), any(), any());
		verify(qoSDBService, atLeastOnce()).logIntraMeasurementDetailsToDB(any(), any(), any());
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testExecuteServerIsInStandalonMode() {

		final UriComponents uri = Utilities.createURI("HTTPS", "localhost", 12345, "serviceregistry");

		final ServiceRegistryListResponseDTO serviceRegistryResponse = getServiceRegistryListResponseDTOForTest();
		final ResponseEntity<ServiceRegistryListResponseDTO> httpResponse = new ResponseEntity<ServiceRegistryListResponseDTO>(serviceRegistryResponse, HttpStatus.OK);

		final List<IcmpPingResponse> responseList = getResponseListForTest();

		final QoSIntraMeasurement measurement = getQoSIntraMeasurementForTest();

		final QoSIntraPingMeasurementLog measurementLog = getOrCreateMeasurementLogForTest();

		final ArgumentCaptor<String> debugValueCapture = ArgumentCaptor.forClass(String.class);
		doNothing().when(logger).debug( debugValueCapture.capture());

		when(arrowheadContext.containsKey(CoreCommonConstants.SERVER_STANDALONE_MODE)).thenReturn(true);
		when(arrowheadContext.containsKey(CoreCommonConstants.SR_QUERY_ALL)).thenReturn(true);
		when(arrowheadContext.get(CoreCommonConstants.SR_QUERY_ALL)).thenReturn(uri);

		when(httpService.sendRequest(uri, HttpMethod.GET, ServiceRegistryListResponseDTO.class)).thenReturn(httpResponse);

		when(pingMeasurementProperties.getTimeout()).thenReturn(5000);

		when(pingService.getPingResponseList(anyString())).thenReturn(responseList);

		when(qoSDBService.getOrCreateIntraMeasurement(any(SystemResponseDTO.class), any())).thenReturn(measurement);

		//in handleMeasurement
		when(qoSDBService.getIntraPingMeasurementByMeasurement(any(QoSIntraMeasurement.class))).thenReturn(Optional.ofNullable(null));
		doNothing().when(qoSDBService).createIntraPingMeasurement(any(), any(), any());
		//doNothing().when(qoSDBService).updatePingMeasurement(any(), any(), any(), any());
		when(qoSDBService.logIntraMeasurementToDB(any(), any(), any())).thenReturn(measurementLog);
		doNothing().when(qoSDBService).logIntraMeasurementDetailsToDB(any(), any(), any());

		try {

			pingTask.execute(jobExecutionContext);

		} catch (final JobExecutionException ex) {
			fail();
		}

		verify(logger, atLeastOnce()).debug(any(String.class));
		final List<String> debugMessages = debugValueCapture.getAllValues();
		assertNotNull(debugMessages);

		verify(arrowheadContext, times(1)).containsKey(CoreCommonConstants.SERVER_STANDALONE_MODE);
		verify(arrowheadContext, times(0)).containsKey(CoreCommonConstants.SR_QUERY_ALL);
		verify(arrowheadContext, times(0)).get(CoreCommonConstants.SR_QUERY_ALL);
		verify(httpService, times(0)).sendRequest(any(), any(), any());
		verify(pingMeasurementProperties, times(0)).getTimeout();
		verify(pingService, times(0)).getPingResponseList(anyString());
		verify(qoSDBService,times(0)).getOrCreateIntraMeasurement(any(), any());

		verify(qoSDBService, times(0)).getIntraPingMeasurementByMeasurement(any());
		verify(qoSDBService, times(0)).createIntraPingMeasurement(any(), any(), any());
		verify(qoSDBService, times(0)).logIntraMeasurementToDB(any(), any(), any());
		verify(qoSDBService, times(0)).logIntraMeasurementDetailsToDB(any(), any(), any());
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testExecuteNoSystemsToMeasure() {

		final UriComponents uri = Utilities.createURI("HTTPS", "localhost", 12345, "serviceregistry");

		final ServiceRegistryListResponseDTO serviceRegistryResponse = new ServiceRegistryListResponseDTO();
		final ResponseEntity<ServiceRegistryListResponseDTO> httpResponse = new ResponseEntity<ServiceRegistryListResponseDTO>(serviceRegistryResponse, HttpStatus.OK);

		final List<IcmpPingResponse> responseList = getResponseListForTest();

		final QoSIntraMeasurement measurement = getQoSIntraMeasurementForTest();

		final QoSIntraPingMeasurementLog measurementLog = getOrCreateMeasurementLogForTest();

		final ArgumentCaptor<String> debugValueCapture = ArgumentCaptor.forClass(String.class);
		doNothing().when(logger).debug( debugValueCapture.capture());

		when(arrowheadContext.containsKey(CoreCommonConstants.SERVER_STANDALONE_MODE)).thenReturn(false);
		when(arrowheadContext.containsKey(CoreCommonConstants.SR_QUERY_ALL)).thenReturn(true);
		when(arrowheadContext.get(CoreCommonConstants.SR_QUERY_ALL)).thenReturn(uri);

		when(httpService.sendRequest(uri, HttpMethod.GET, ServiceRegistryListResponseDTO.class)).thenReturn(httpResponse);

		when(pingMeasurementProperties.getTimeout()).thenReturn(5000);

		when(pingService.getPingResponseList(anyString())).thenReturn(responseList);

		when(qoSDBService.getOrCreateIntraMeasurement(any(SystemResponseDTO.class), any())).thenReturn(measurement);

		//in handleMeasurement
		when(qoSDBService.getIntraPingMeasurementByMeasurement(any(QoSIntraMeasurement.class))).thenReturn(Optional.ofNullable(null));
		doNothing().when(qoSDBService).createIntraPingMeasurement(any(), any(), any());
		//doNothing().when(qoSDBService).updatePingMeasurement(any(), any(), any(), any());
		when(qoSDBService.logIntraMeasurementToDB(any(), any(), any())).thenReturn(measurementLog);
		doNothing().when(qoSDBService).logIntraMeasurementDetailsToDB(any(), any(), any());

		try {

			pingTask.execute(jobExecutionContext);

		} catch (final JobExecutionException ex) {
			fail();
		}

		verify(logger, atLeastOnce()).debug(any(String.class));
		final List<String> debugMessages = debugValueCapture.getAllValues();
		assertNotNull(debugMessages);

		verify(arrowheadContext, times(1)).containsKey(CoreCommonConstants.SERVER_STANDALONE_MODE);
		verify(arrowheadContext, times(1)).containsKey(CoreCommonConstants.SR_QUERY_ALL);
		verify(arrowheadContext, times(1)).get(CoreCommonConstants.SR_QUERY_ALL);
		verify(httpService, times(1)).sendRequest(any(), any(), any());
		verify(pingMeasurementProperties, times(0)).getTimeout();
		verify(pingService, times(0)).getPingResponseList(anyString());
		verify(qoSDBService,times(0)).getOrCreateIntraMeasurement(any(), any());

		verify(qoSDBService, times(0)).getIntraPingMeasurementByMeasurement(any());
		verify(qoSDBService, times(0)).createIntraPingMeasurement(any(), any(), any());
		verify(qoSDBService, times(0)).logIntraMeasurementToDB(any(), any(), any());
		verify(qoSDBService, times(0)).logIntraMeasurementDetailsToDB(any(), any(), any());
	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testExecuteServiceRegistryUriNotInContext() {

		final UriComponents uri = Utilities.createURI("HTTPS", "localhost", 12345, "serviceregistry");

		final ServiceRegistryListResponseDTO serviceRegistryResponse = getServiceRegistryListResponseDTOForTest();
		final ResponseEntity<ServiceRegistryListResponseDTO> httpResponse = new ResponseEntity<ServiceRegistryListResponseDTO>(serviceRegistryResponse, HttpStatus.OK);

		final List<IcmpPingResponse> responseList = getResponseListForTest();

		final QoSIntraMeasurement measurement = getQoSIntraMeasurementForTest();

		final QoSIntraPingMeasurementLog measurementLog = getOrCreateMeasurementLogForTest();

		final ArgumentCaptor<String> debugValueCapture = ArgumentCaptor.forClass(String.class);
		doNothing().when(logger).debug( debugValueCapture.capture());

		when(arrowheadContext.containsKey(CoreCommonConstants.SERVER_STANDALONE_MODE)).thenReturn(false);
		when(arrowheadContext.containsKey(CoreCommonConstants.SR_QUERY_ALL)).thenReturn(false);
		when(arrowheadContext.get(CoreCommonConstants.SR_QUERY_ALL)).thenReturn(uri);

		when(httpService.sendRequest(uri, HttpMethod.GET, ServiceRegistryListResponseDTO.class)).thenReturn(httpResponse);

		when(pingMeasurementProperties.getTimeout()).thenReturn(5000);

		when(pingService.getPingResponseList(anyString())).thenReturn(responseList);

		when(qoSDBService.getOrCreateIntraMeasurement(any(SystemResponseDTO.class), any())).thenReturn(measurement);

		//in handleMeasurement
		when(qoSDBService.getIntraPingMeasurementByMeasurement(any(QoSIntraMeasurement.class))).thenReturn(Optional.ofNullable(null));
		doNothing().when(qoSDBService).createIntraPingMeasurement(any(), any(), any());
		//doNothing().when(qoSDBService).updatePingMeasurement(any(), any(), any(), any());
		when(qoSDBService.logIntraMeasurementToDB(any(), any(), any())).thenReturn(measurementLog);
		doNothing().when(qoSDBService).logIntraMeasurementDetailsToDB(any(), any(), any());

		try {

			pingTask.execute(jobExecutionContext);

		} catch (final JobExecutionException ex) {
			fail();
		} catch (final ArrowheadException ex) {

			assertTrue(ex.getMessage().contains("QoS Mointor can't find Service Registry Query All URI."));

			verify(logger, atLeastOnce()).debug(any(String.class));
			final List<String> debugMessages = debugValueCapture.getAllValues();
			assertNotNull(debugMessages);

			verify(arrowheadContext, times(1)).containsKey(CoreCommonConstants.SERVER_STANDALONE_MODE);
			verify(arrowheadContext, times(1)).containsKey(CoreCommonConstants.SR_QUERY_ALL);
			verify(arrowheadContext, times(0)).get(CoreCommonConstants.SR_QUERY_ALL);
			verify(httpService, times(0)).sendRequest(any(), any(), any());
			verify(pingMeasurementProperties, times(0)).getTimeout();
			verify(pingService, times(0)).getPingResponseList(anyString());
			verify(qoSDBService,times(0)).getOrCreateIntraMeasurement(any(), any());

			verify(qoSDBService, times(0)).getIntraPingMeasurementByMeasurement(any());
			verify(qoSDBService, times(0)).createIntraPingMeasurement(any(), any(), any());
			verify(qoSDBService, times(0)).logIntraMeasurementToDB(any(), any(), any());
			verify(qoSDBService, times(0)).logIntraMeasurementDetailsToDB(any(), any(), any());

			throw ex;
		}

	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testExecuteServiceRegistryUriContextValueNotUriType() {

		final UriComponents uri = Utilities.createURI("HTTPS", "localhost", 12345, "serviceregistry");

		final ServiceRegistryListResponseDTO serviceRegistryResponse = getServiceRegistryListResponseDTOForTest();
		final ResponseEntity<ServiceRegistryListResponseDTO> httpResponse = new ResponseEntity<ServiceRegistryListResponseDTO>(serviceRegistryResponse, HttpStatus.OK);

		final List<IcmpPingResponse> responseList = getResponseListForTest();

		final QoSIntraMeasurement measurement = getQoSIntraMeasurementForTest();

		final QoSIntraPingMeasurementLog measurementLog = getOrCreateMeasurementLogForTest();

		final ArgumentCaptor<String> debugValueCapture = ArgumentCaptor.forClass(String.class);
		doNothing().when(logger).debug( debugValueCapture.capture());

		when(arrowheadContext.containsKey(CoreCommonConstants.SERVER_STANDALONE_MODE)).thenReturn(false);
		when(arrowheadContext.containsKey(CoreCommonConstants.SR_QUERY_ALL)).thenReturn(true);
		when(arrowheadContext.get(CoreCommonConstants.SR_QUERY_ALL)).thenThrow(ClassCastException.class);

		when(httpService.sendRequest(uri, HttpMethod.GET, ServiceRegistryListResponseDTO.class)).thenReturn(httpResponse);

		when(pingMeasurementProperties.getTimeout()).thenReturn(5000);

		when(pingService.getPingResponseList(anyString())).thenReturn(responseList);

		when(qoSDBService.getOrCreateIntraMeasurement(any(SystemResponseDTO.class), any())).thenReturn(measurement);

		//in handleMeasurement
		when(qoSDBService.getIntraPingMeasurementByMeasurement(any(QoSIntraMeasurement.class))).thenReturn(Optional.ofNullable(null));
		doNothing().when(qoSDBService).createIntraPingMeasurement(any(), any(), any());
		//doNothing().when(qoSDBService).updatePingMeasurement(any(), any(), any(), any());
		when(qoSDBService.logIntraMeasurementToDB(any(), any(), any())).thenReturn(measurementLog);
		doNothing().when(qoSDBService).logIntraMeasurementDetailsToDB(any(), any(), any());

		try {

			pingTask.execute(jobExecutionContext);

		} catch (final JobExecutionException ex) {
			fail();
		} catch (final ArrowheadException ex) {

			assertTrue(ex.getMessage().contains("QoS Mointor can't find Service Registry Query All URI."));

			verify(logger, atLeastOnce()).debug(any(String.class));
			final List<String> debugMessages = debugValueCapture.getAllValues();
			assertNotNull(debugMessages);

			verify(arrowheadContext, times(1)).containsKey(CoreCommonConstants.SERVER_STANDALONE_MODE);
			verify(arrowheadContext, times(1)).containsKey(CoreCommonConstants.SR_QUERY_ALL);
			verify(arrowheadContext, times(1)).get(CoreCommonConstants.SR_QUERY_ALL);
			verify(httpService, times(0)).sendRequest(any(), any(), any());
			verify(pingMeasurementProperties, times(0)).getTimeout();
			verify(pingService, times(0)).getPingResponseList(anyString());
			verify(qoSDBService,times(0)).getOrCreateIntraMeasurement(any(), any());

			verify(qoSDBService, times(0)).getIntraPingMeasurementByMeasurement(any());
			verify(qoSDBService, times(0)).createIntraPingMeasurement(any(), any(), any());
			verify(qoSDBService, times(0)).logIntraMeasurementToDB(any(), any(), any());
			verify(qoSDBService, times(0)).logIntraMeasurementDetailsToDB(any(), any(), any());

			throw ex;
		}

	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testExecuteSystemToMeasureHasNullAddressParameter() {

		final UriComponents uri = Utilities.createURI("HTTPS", "localhost", 12345, "serviceregistry");

		final ServiceRegistryListResponseDTO serviceRegistryResponse = getServiceRegistryListResponseDTOWithNullSystemAddressForTest();

		final ResponseEntity<ServiceRegistryListResponseDTO> httpResponse = new ResponseEntity<ServiceRegistryListResponseDTO>(serviceRegistryResponse, HttpStatus.OK);

		final List<IcmpPingResponse> responseList = getResponseListForTest();

		final QoSIntraMeasurement measurement = getQoSIntraMeasurementForTest();

		final QoSIntraPingMeasurementLog measurementLog = getOrCreateMeasurementLogForTest();

		final ArgumentCaptor<String> debugValueCapture = ArgumentCaptor.forClass(String.class);
		doNothing().when(logger).debug( debugValueCapture.capture());

		when(arrowheadContext.containsKey(CoreCommonConstants.SERVER_STANDALONE_MODE)).thenReturn(false);
		when(arrowheadContext.containsKey(CoreCommonConstants.SR_QUERY_ALL)).thenReturn(true);
		when(arrowheadContext.get(CoreCommonConstants.SR_QUERY_ALL)).thenReturn(uri);

		when(httpService.sendRequest(uri, HttpMethod.GET, ServiceRegistryListResponseDTO.class)).thenReturn(httpResponse);

		when(pingMeasurementProperties.getTimeout()).thenReturn(5000);

		when(pingService.getPingResponseList(anyString())).thenReturn(responseList);

		when(qoSDBService.getOrCreateIntraMeasurement(any(SystemResponseDTO.class), any())).thenReturn(measurement);

		//in handleMeasurement
		when(qoSDBService.getIntraPingMeasurementByMeasurement(any(QoSIntraMeasurement.class))).thenReturn(Optional.ofNullable(null));
		doNothing().when(qoSDBService).createIntraPingMeasurement(any(), any(), any());
		//doNothing().when(qoSDBService).updatePingMeasurement(any(), any(), any(), any());
		when(qoSDBService.logIntraMeasurementToDB(any(), any(), any())).thenReturn(measurementLog);
		doNothing().when(qoSDBService).logIntraMeasurementDetailsToDB(any(), any(), any());

		try {

			pingTask.execute(jobExecutionContext);

		} catch (final JobExecutionException ex) {
			fail();
		} catch (final InvalidParameterException ex) {

			assertTrue(ex.getMessage().contains("System.address" + NULL_OR_BLANK_PARAMETER_ERROR_MESSAGE));

			verify(logger, atLeastOnce()).debug(any(String.class));
			final List<String> debugMessages = debugValueCapture.getAllValues();
			assertNotNull(debugMessages);

			verify(arrowheadContext, times(1)).containsKey(CoreCommonConstants.SERVER_STANDALONE_MODE);
			verify(arrowheadContext, times(1)).containsKey(CoreCommonConstants.SR_QUERY_ALL);
			verify(arrowheadContext, times(1)).get(CoreCommonConstants.SR_QUERY_ALL);
			verify(httpService, times(1)).sendRequest(any(), any(), any());
			verify(pingMeasurementProperties, times(0)).getTimeout();
			verify(pingService, times(0)).getPingResponseList(anyString());
			verify(qoSDBService,times(0)).getOrCreateIntraMeasurement(any(), any());

			verify(qoSDBService, times(0)).getIntraPingMeasurementByMeasurement(any());
			verify(qoSDBService, times(0)).createIntraPingMeasurement(any(), any(), any());
			verify(qoSDBService, times(0)).logIntraMeasurementToDB(any(), any(), any());
			verify(qoSDBService, times(0)).logIntraMeasurementDetailsToDB(any(), any(), any());

			throw ex;
		}

	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testExecuteSystemToMeasureHasEmptyAddressParameter() {

		final UriComponents uri = Utilities.createURI("HTTPS", "localhost", 12345, "serviceregistry");

		final ServiceRegistryListResponseDTO serviceRegistryResponse = getServiceRegistryListResponseDTOWithEmptySystemAddressForTest();

		final ResponseEntity<ServiceRegistryListResponseDTO> httpResponse = new ResponseEntity<ServiceRegistryListResponseDTO>(serviceRegistryResponse, HttpStatus.OK);

		final List<IcmpPingResponse> responseList = getResponseListForTest();

		final QoSIntraMeasurement measurement = getQoSIntraMeasurementForTest();

		final QoSIntraPingMeasurementLog measurementLog = getOrCreateMeasurementLogForTest();

		final ArgumentCaptor<String> debugValueCapture = ArgumentCaptor.forClass(String.class);
		doNothing().when(logger).debug( debugValueCapture.capture());

		when(arrowheadContext.containsKey(CoreCommonConstants.SERVER_STANDALONE_MODE)).thenReturn(false);
		when(arrowheadContext.containsKey(CoreCommonConstants.SR_QUERY_ALL)).thenReturn(true);
		when(arrowheadContext.get(CoreCommonConstants.SR_QUERY_ALL)).thenReturn(uri);

		when(httpService.sendRequest(uri, HttpMethod.GET, ServiceRegistryListResponseDTO.class)).thenReturn(httpResponse);

		when(pingMeasurementProperties.getTimeout()).thenReturn(5000);

		when(pingService.getPingResponseList(anyString())).thenReturn(responseList);

		when(qoSDBService.getOrCreateIntraMeasurement(any(SystemResponseDTO.class), any())).thenReturn(measurement);

		//in handleMeasurement
		when(qoSDBService.getIntraPingMeasurementByMeasurement(any(QoSIntraMeasurement.class))).thenReturn(Optional.ofNullable(null));
		doNothing().when(qoSDBService).createIntraPingMeasurement(any(), any(), any());
		//doNothing().when(qoSDBService).updatePingMeasurement(any(), any(), any(), any());
		when(qoSDBService.logIntraMeasurementToDB(any(), any(), any())).thenReturn(measurementLog);
		doNothing().when(qoSDBService).logIntraMeasurementDetailsToDB(any(), any(), any());

		try {

			pingTask.execute(jobExecutionContext);

		} catch (final JobExecutionException ex) {
			fail();
		} catch (final InvalidParameterException ex) {

			assertTrue(ex.getMessage().contains("System.address" + NULL_OR_BLANK_PARAMETER_ERROR_MESSAGE));

			verify(logger, atLeastOnce()).debug(any(String.class));
			final List<String> debugMessages = debugValueCapture.getAllValues();
			assertNotNull(debugMessages);

			verify(arrowheadContext, times(1)).containsKey(CoreCommonConstants.SERVER_STANDALONE_MODE);
			verify(arrowheadContext, times(1)).containsKey(CoreCommonConstants.SR_QUERY_ALL);
			verify(arrowheadContext, times(1)).get(CoreCommonConstants.SR_QUERY_ALL);
			verify(httpService, times(1)).sendRequest(any(), any(), any());
			verify(pingMeasurementProperties, times(0)).getTimeout();
			verify(pingService, times(0)).getPingResponseList(anyString());
			verify(qoSDBService,times(0)).getOrCreateIntraMeasurement(any(), any());

			verify(qoSDBService, times(0)).getIntraPingMeasurementByMeasurement(any());
			verify(qoSDBService, times(0)).createIntraPingMeasurement(any(), any(), any());
			verify(qoSDBService, times(0)).logIntraMeasurementToDB(any(), any(), any());
			verify(qoSDBService, times(0)).logIntraMeasurementDetailsToDB(any(), any(), any());

			throw ex;
		}

	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testExecuteGetPingResponseListThrowsException() {

		final UriComponents uri = Utilities.createURI("HTTPS", "localhost", 12345, "serviceregistry");

		final ServiceRegistryListResponseDTO serviceRegistryResponse = getServiceRegistryListResponseDTOForTest();

		final ResponseEntity<ServiceRegistryListResponseDTO> httpResponse = new ResponseEntity<ServiceRegistryListResponseDTO>(serviceRegistryResponse, HttpStatus.OK);

		final QoSIntraMeasurement measurement = getQoSIntraMeasurementForTest();

		final QoSIntraPingMeasurementLog measurementLog = getOrCreateMeasurementLogForTest();

		final ArgumentCaptor<String> debugValueCapture = ArgumentCaptor.forClass(String.class);
		doNothing().when(logger).debug( debugValueCapture.capture());

		when(arrowheadContext.containsKey(CoreCommonConstants.SERVER_STANDALONE_MODE)).thenReturn(false);
		when(arrowheadContext.containsKey(CoreCommonConstants.SR_QUERY_ALL)).thenReturn(true);
		when(arrowheadContext.get(CoreCommonConstants.SR_QUERY_ALL)).thenReturn(uri);

		when(httpService.sendRequest(uri, HttpMethod.GET, ServiceRegistryListResponseDTO.class)).thenReturn(httpResponse);

		when(pingMeasurementProperties.getTimeout()).thenReturn(5000);

		when(pingService.getPingResponseList(anyString())).thenThrow(ArrowheadException.class);

		when(qoSDBService.getOrCreateIntraMeasurement(any(SystemResponseDTO.class), any())).thenReturn(measurement);

		//in handleMeasurement
		when(qoSDBService.getIntraPingMeasurementByMeasurement(any(QoSIntraMeasurement.class))).thenReturn(Optional.ofNullable(null));
		doNothing().when(qoSDBService).createIntraPingMeasurement(any(), any(), any());
		//doNothing().when(qoSDBService).updatePingMeasurement(any(), any(), any(), any());
		when(qoSDBService.logIntraMeasurementToDB(any(), any(), any())).thenReturn(measurementLog);
		doNothing().when(qoSDBService).logIntraMeasurementDetailsToDB(any(), any(), any());

		try {

			pingTask.execute(jobExecutionContext);

		} catch (final JobExecutionException ex) {
			fail();
		} catch (final ArrowheadException ex) {

			verify(logger, atLeastOnce()).debug(any(String.class));
			final List<String> debugMessages = debugValueCapture.getAllValues();
			assertNotNull(debugMessages);

			verify(arrowheadContext, times(1)).containsKey(CoreCommonConstants.SERVER_STANDALONE_MODE);
			verify(arrowheadContext, times(1)).containsKey(CoreCommonConstants.SR_QUERY_ALL);
			verify(arrowheadContext, times(1)).get(CoreCommonConstants.SR_QUERY_ALL);
			verify(httpService, times(1)).sendRequest(any(), any(), any());
			verify(pingService, times(1)).getPingResponseList(anyString());
			verify(pingMeasurementProperties, times(0)).getTimeout();
			verify(qoSDBService,times(0)).getOrCreateIntraMeasurement(any(), any());

			verify(qoSDBService, times(0)).getIntraPingMeasurementByMeasurement(any());
			verify(qoSDBService, times(0)).createIntraPingMeasurement(any(), any(), any());
			verify(qoSDBService, times(0)).logIntraMeasurementToDB(any(), any(), any());
			verify(qoSDBService, times(0)).logIntraMeasurementDetailsToDB(any(), any(), any());

			throw ex;
		}

	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testExecuteGetMeasurementThrowsException() {

		final UriComponents uri = Utilities.createURI("HTTPS", "localhost", 12345, "serviceregistry");

		final ServiceRegistryListResponseDTO serviceRegistryResponse = getServiceRegistryListResponseDTOForTest();

		final ResponseEntity<ServiceRegistryListResponseDTO> httpResponse = new ResponseEntity<ServiceRegistryListResponseDTO>(serviceRegistryResponse, HttpStatus.OK);

		final List<IcmpPingResponse> responseList = getResponseListForTest();

		//final QoSIntraMeasurement measurement = getQoSIntraMeasurementForTest();

		final QoSIntraPingMeasurementLog measurementLog = getOrCreateMeasurementLogForTest();

		final ArgumentCaptor<String> debugValueCapture = ArgumentCaptor.forClass(String.class);
		doNothing().when(logger).debug( debugValueCapture.capture());

		when(arrowheadContext.containsKey(CoreCommonConstants.SERVER_STANDALONE_MODE)).thenReturn(false);
		when(arrowheadContext.containsKey(CoreCommonConstants.SR_QUERY_ALL)).thenReturn(true);
		when(arrowheadContext.get(CoreCommonConstants.SR_QUERY_ALL)).thenReturn(uri);

		when(httpService.sendRequest(uri, HttpMethod.GET, ServiceRegistryListResponseDTO.class)).thenReturn(httpResponse);

		when(pingMeasurementProperties.getTimeout()).thenReturn(5000);

		when(pingService.getPingResponseList(anyString())).thenReturn(responseList);

		when(qoSDBService.getOrCreateIntraMeasurement(any(SystemResponseDTO.class), any())).thenThrow(ArrowheadException.class);

		//in handleMeasurement
		when(qoSDBService.getIntraPingMeasurementByMeasurement(any(QoSIntraMeasurement.class))).thenReturn(Optional.ofNullable(null));
		doNothing().when(qoSDBService).createIntraPingMeasurement(any(), any(), any());
		//doNothing().when(qoSDBService).updatePingMeasurement(any(), any(), any(), any());
		when(qoSDBService.logIntraMeasurementToDB(any(), any(), any())).thenReturn(measurementLog);
		doNothing().when(qoSDBService).logIntraMeasurementDetailsToDB(any(), any(), any());

		try {

			pingTask.execute(jobExecutionContext);

		} catch (final JobExecutionException ex) {
			fail();
		} catch (final ArrowheadException ex) {

			verify(logger, atLeastOnce()).debug(any(String.class));
			final List<String> debugMessages = debugValueCapture.getAllValues();
			assertNotNull(debugMessages);

			verify(arrowheadContext, times(1)).containsKey(CoreCommonConstants.SERVER_STANDALONE_MODE);
			verify(arrowheadContext, times(1)).containsKey(CoreCommonConstants.SR_QUERY_ALL);
			verify(arrowheadContext, times(1)).get(CoreCommonConstants.SR_QUERY_ALL);
			verify(httpService, times(1)).sendRequest(any(), any(), any());
			verify(pingService, times(1)).getPingResponseList(anyString());
			verify(qoSDBService,times(1)).getOrCreateIntraMeasurement(any(), any());
			verify(pingMeasurementProperties, times(0)).getTimeout();

			verify(qoSDBService, times(0)).getIntraPingMeasurementByMeasurement(any());
			verify(qoSDBService, times(0)).createIntraPingMeasurement(any(), any(), any());
			verify(qoSDBService, times(0)).logIntraMeasurementToDB(any(), any(), any());
			verify(qoSDBService, times(0)).logIntraMeasurementDetailsToDB(any(), any(), any());

			throw ex;
		}

	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testExecuteWithEmptyResponseList() {

		final UriComponents uri = Utilities.createURI("HTTPS", "localhost", 12345, "serviceregistry");

		final ServiceRegistryListResponseDTO serviceRegistryResponse = getServiceRegistryListResponseDTOForTest();

		final ResponseEntity<ServiceRegistryListResponseDTO> httpResponse = new ResponseEntity<ServiceRegistryListResponseDTO>(serviceRegistryResponse, HttpStatus.OK);

		final List<IcmpPingResponse> responseList = getEmptyResponseListForTest();

		final QoSIntraMeasurement measurement = getQoSIntraMeasurementForTest();

		final QoSIntraPingMeasurementLog measurementLog = getOrCreateMeasurementLogForTest();

		final ArgumentCaptor<String> debugValueCapture = ArgumentCaptor.forClass(String.class);
		doNothing().when(logger).debug( debugValueCapture.capture());

		when(arrowheadContext.containsKey(CoreCommonConstants.SERVER_STANDALONE_MODE)).thenReturn(false);
		when(arrowheadContext.containsKey(CoreCommonConstants.SR_QUERY_ALL)).thenReturn(true);
		when(arrowheadContext.get(CoreCommonConstants.SR_QUERY_ALL)).thenReturn(uri);

		when(httpService.sendRequest(uri, HttpMethod.GET, ServiceRegistryListResponseDTO.class)).thenReturn(httpResponse);

		when(pingMeasurementProperties.getTimeout()).thenReturn(5000);

		when(pingService.getPingResponseList(anyString())).thenReturn(responseList);

		when(qoSDBService.getOrCreateIntraMeasurement(any(SystemResponseDTO.class), any())).thenReturn(measurement);

		//in handleMeasurement
		when(qoSDBService.getIntraPingMeasurementByMeasurement(any(QoSIntraMeasurement.class))).thenReturn(Optional.ofNullable(null));
		doNothing().when(qoSDBService).createIntraPingMeasurement(any(), any(), any());
		//doNothing().when(qoSDBService).updatePingMeasurement(any(), any(), any(), any());
		when(qoSDBService.logIntraMeasurementToDB(any(), any(), any())).thenReturn(measurementLog);
		doNothing().when(qoSDBService).logIntraMeasurementDetailsToDB(any(), any(), any());

		try {

			pingTask.execute(jobExecutionContext);

		} catch (final JobExecutionException ex) {
			fail();
		} catch (final IllegalArgumentException ex) {

			assertTrue(ex.getMessage().contains("Sent in this Ping value must be greater than zero"));

			verify(logger, atLeastOnce()).debug(any(String.class));
			final List<String> debugMessages = debugValueCapture.getAllValues();
			assertNotNull(debugMessages);

			verify(arrowheadContext, times(1)).containsKey(CoreCommonConstants.SERVER_STANDALONE_MODE);
			verify(arrowheadContext, times(1)).containsKey(CoreCommonConstants.SR_QUERY_ALL);
			verify(arrowheadContext, times(1)).get(CoreCommonConstants.SR_QUERY_ALL);
			verify(httpService, times(1)).sendRequest(any(), any(), any());
			verify(pingService, times(1)).getPingResponseList(anyString());
			verify(qoSDBService,times(1)).getOrCreateIntraMeasurement(any(), any());
			verify(pingMeasurementProperties, times(0)).getTimeout();

			verify(qoSDBService, times(0)).getIntraPingMeasurementByMeasurement(any());
			verify(qoSDBService, times(0)).createIntraPingMeasurement(any(), any(), any());
			verify(qoSDBService, times(0)).logIntraMeasurementToDB(any(), any(), any());
			verify(qoSDBService, times(0)).logIntraMeasurementDetailsToDB(any(), any(), any());

			throw ex;
		}

	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testExecuteCalculationsDTOIsPresent() {

		final UriComponents uri = Utilities.createURI("HTTPS", "localhost", 12345, "serviceregistry");

		final ServiceRegistryListResponseDTO serviceRegistryResponse = getServiceRegistryListResponseDTOForTest();
		final ResponseEntity<ServiceRegistryListResponseDTO> httpResponse = new ResponseEntity<ServiceRegistryListResponseDTO>(serviceRegistryResponse, HttpStatus.OK);

		final List<IcmpPingResponse> responseList = getResponseListForTest();

		final QoSIntraMeasurement measurement = getQoSIntraMeasurementForTest();

		final QoSIntraPingMeasurementLog measurementLog = getOrCreateMeasurementLogForTest();

		final ArgumentCaptor<PingMeasurementCalculationsDTO> calculationsValueCapture = ArgumentCaptor.forClass(PingMeasurementCalculationsDTO.class);

		final ArgumentCaptor<String> debugValueCapture = ArgumentCaptor.forClass(String.class);
		doNothing().when(logger).debug( debugValueCapture.capture());

		when(arrowheadContext.containsKey(CoreCommonConstants.SERVER_STANDALONE_MODE)).thenReturn(false);
		when(arrowheadContext.containsKey(CoreCommonConstants.SR_QUERY_ALL)).thenReturn(true);
		when(arrowheadContext.get(CoreCommonConstants.SR_QUERY_ALL)).thenReturn(uri);

		when(httpService.sendRequest(uri, HttpMethod.GET, ServiceRegistryListResponseDTO.class)).thenReturn(httpResponse);

		when(pingMeasurementProperties.getTimeout()).thenReturn(5000);
		when(pingMeasurementProperties.getAvailableFromSuccessPercent()).thenReturn(1);

		when(pingService.getPingResponseList(anyString())).thenReturn(responseList);

		when(qoSDBService.getOrCreateIntraMeasurement(any(SystemResponseDTO.class), any())).thenReturn(measurement);

		//in handleMeasurement
		when(qoSDBService.getIntraPingMeasurementByMeasurement(any(QoSIntraMeasurement.class))).thenReturn(Optional.ofNullable(null));
		doNothing().when(qoSDBService).createIntraPingMeasurement(any(), calculationsValueCapture.capture(), any());
		//doNothing().when(qoSDBService).updatePingMeasurement(any(), any(), any(), any());
		when(qoSDBService.logIntraMeasurementToDB(any(), any(), any())).thenReturn(measurementLog);
		doNothing().when(qoSDBService).logIntraMeasurementDetailsToDB(any(), any(), any());

		try {

			pingTask.execute(jobExecutionContext);

		} catch (final JobExecutionException ex) {
			fail();
		}

		final PingMeasurementCalculationsDTO calculations = calculationsValueCapture.getValue();
		assertNotNull(calculations);

		verify(logger, atLeastOnce()).debug(any(String.class));
		final List<String> debugMessages = debugValueCapture.getAllValues();
		assertNotNull(debugMessages);

		verify(arrowheadContext, times(1)).containsKey(CoreCommonConstants.SERVER_STANDALONE_MODE);
		verify(arrowheadContext, times(1)).containsKey(CoreCommonConstants.SR_QUERY_ALL);
		verify(arrowheadContext, times(1)).get(CoreCommonConstants.SR_QUERY_ALL);
		verify(httpService, times(1)).sendRequest(any(), any(), any());
		verify(pingMeasurementProperties, times(1)).getTimeout();
		verify(pingService, atLeastOnce()).getPingResponseList(anyString());
		verify(qoSDBService, atLeastOnce()).getOrCreateIntraMeasurement(any(), any());

		verify(qoSDBService, atLeastOnce()).getIntraPingMeasurementByMeasurement(any());
		verify(qoSDBService, atLeastOnce()).createIntraPingMeasurement(any(), any(), any());
		verify(qoSDBService, atLeastOnce()).logIntraMeasurementToDB(any(), any(), any());
		verify(qoSDBService, atLeastOnce()).logIntraMeasurementDetailsToDB(any(), any(), any());
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testExecuteAvailableCalculation() {

		final UriComponents uri = Utilities.createURI("HTTPS", "localhost", 12345, "serviceregistry");

		final ServiceRegistryListResponseDTO serviceRegistryResponse = getServiceRegistryListResponseDTOForTest();
		final ResponseEntity<ServiceRegistryListResponseDTO> httpResponse = new ResponseEntity<ServiceRegistryListResponseDTO>(serviceRegistryResponse, HttpStatus.OK);

		final List<IcmpPingResponse> responseList = getResponseListForTest();

		final QoSIntraMeasurement measurement = getQoSIntraMeasurementForTest();

		final QoSIntraPingMeasurementLog measurementLog = getOrCreateMeasurementLogForTest();

		final ArgumentCaptor<PingMeasurementCalculationsDTO> calculationsValueCapture = ArgumentCaptor.forClass(PingMeasurementCalculationsDTO.class);

		final ArgumentCaptor<String> debugValueCapture = ArgumentCaptor.forClass(String.class);
		doNothing().when(logger).debug( debugValueCapture.capture());

		when(arrowheadContext.containsKey(CoreCommonConstants.SERVER_STANDALONE_MODE)).thenReturn(false);
		when(arrowheadContext.containsKey(CoreCommonConstants.SR_QUERY_ALL)).thenReturn(true);
		when(arrowheadContext.get(CoreCommonConstants.SR_QUERY_ALL)).thenReturn(uri);

		when(httpService.sendRequest(uri, HttpMethod.GET, ServiceRegistryListResponseDTO.class)).thenReturn(httpResponse);

		when(pingMeasurementProperties.getTimeout()).thenReturn(5000);
		when(pingMeasurementProperties.getAvailableFromSuccessPercent()).thenReturn(32);

		when(pingService.getPingResponseList(anyString())).thenReturn(responseList);

		when(qoSDBService.getOrCreateIntraMeasurement(any(SystemResponseDTO.class), any())).thenReturn(measurement);

		//in handleMeasurement
		when(qoSDBService.getIntraPingMeasurementByMeasurement(any(QoSIntraMeasurement.class))).thenReturn(Optional.ofNullable(null));
		doNothing().when(qoSDBService).createIntraPingMeasurement(any(), calculationsValueCapture.capture(), any());
		//doNothing().when(qoSDBService).updatePingMeasurement(any(), any(), any(), any());
		when(qoSDBService.logIntraMeasurementToDB(any(), any(), any())).thenReturn(measurementLog);
		doNothing().when(qoSDBService).logIntraMeasurementDetailsToDB(any(), any(), any());

		try {

			pingTask.execute(jobExecutionContext);

		} catch (final JobExecutionException ex) {
			fail();
		}

		final PingMeasurementCalculationsDTO calculations = calculationsValueCapture.getValue();
		assertNotNull(calculations);
		assertTrue(calculations.isAvailable());

		verify(logger, atLeastOnce()).debug(any(String.class));
		final List<String> debugMessages = debugValueCapture.getAllValues();
		assertNotNull(debugMessages);

		verify(arrowheadContext, times(1)).containsKey(CoreCommonConstants.SERVER_STANDALONE_MODE);
		verify(arrowheadContext, times(1)).containsKey(CoreCommonConstants.SR_QUERY_ALL);
		verify(arrowheadContext, times(1)).get(CoreCommonConstants.SR_QUERY_ALL);
		verify(httpService, times(1)).sendRequest(any(), any(), any());
		verify(pingMeasurementProperties, times(1)).getTimeout();
		verify(pingService, atLeastOnce()).getPingResponseList(anyString());
		verify(qoSDBService, atLeastOnce()).getOrCreateIntraMeasurement(any(), any());

		verify(qoSDBService, atLeastOnce()).getIntraPingMeasurementByMeasurement(any());
		verify(qoSDBService, atLeastOnce()).createIntraPingMeasurement(any(), any(), any());
		verify(qoSDBService, atLeastOnce()).logIntraMeasurementToDB(any(), any(), any());
		verify(qoSDBService, atLeastOnce()).logIntraMeasurementDetailsToDB(any(), any(), any());
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testExecuteAvailableCalculationWith100PercentNotOK() {

		final UriComponents uri = Utilities.createURI("HTTPS", "localhost", 12345, "serviceregistry");

		final ServiceRegistryListResponseDTO serviceRegistryResponse = getServiceRegistryListResponseDTOForTest();
		final ResponseEntity<ServiceRegistryListResponseDTO> httpResponse = new ResponseEntity<ServiceRegistryListResponseDTO>(serviceRegistryResponse, HttpStatus.OK);

		final List<IcmpPingResponse> responseList = get100LongResponseListWithIncementingResponseTimeAnd10PercentLossForTest();

		final QoSIntraMeasurement measurement = getQoSIntraMeasurementForTest();

		final QoSIntraPingMeasurementLog measurementLog = getOrCreateMeasurementLogForTest();

		final ArgumentCaptor<PingMeasurementCalculationsDTO> calculationsValueCapture = ArgumentCaptor.forClass(PingMeasurementCalculationsDTO.class);

		final ArgumentCaptor<String> debugValueCapture = ArgumentCaptor.forClass(String.class);
		doNothing().when(logger).debug( debugValueCapture.capture());

		when(arrowheadContext.containsKey(CoreCommonConstants.SERVER_STANDALONE_MODE)).thenReturn(false);
		when(arrowheadContext.containsKey(CoreCommonConstants.SR_QUERY_ALL)).thenReturn(true);
		when(arrowheadContext.get(CoreCommonConstants.SR_QUERY_ALL)).thenReturn(uri);

		when(httpService.sendRequest(uri, HttpMethod.GET, ServiceRegistryListResponseDTO.class)).thenReturn(httpResponse);

		when(pingMeasurementProperties.getTimeout()).thenReturn(5000);
		when(pingMeasurementProperties.getAvailableFromSuccessPercent()).thenReturn(100);

		when(pingService.getPingResponseList(anyString())).thenReturn(responseList);

		when(qoSDBService.getOrCreateIntraMeasurement(any(SystemResponseDTO.class), any())).thenReturn(measurement);

		//in handleMeasurement
		when(qoSDBService.getIntraPingMeasurementByMeasurement(any(QoSIntraMeasurement.class))).thenReturn(Optional.ofNullable(null));
		doNothing().when(qoSDBService).createIntraPingMeasurement(any(), calculationsValueCapture.capture(), any());
		//doNothing().when(qoSDBService).updatePingMeasurement(any(), any(), any(), any());
		when(qoSDBService.logIntraMeasurementToDB(any(), any(), any())).thenReturn(measurementLog);
		doNothing().when(qoSDBService).logIntraMeasurementDetailsToDB(any(), any(), any());

		try {

			pingTask.execute(jobExecutionContext);

		} catch (final JobExecutionException ex) {
			fail();
		}

		final PingMeasurementCalculationsDTO calculations = calculationsValueCapture.getValue();
		assertNotNull(calculations);
		assertTrue(!calculations.isAvailable());

		verify(logger, atLeastOnce()).debug(any(String.class));
		final List<String> debugMessages = debugValueCapture.getAllValues();
		assertNotNull(debugMessages);

		verify(arrowheadContext, times(1)).containsKey(CoreCommonConstants.SERVER_STANDALONE_MODE);
		verify(arrowheadContext, times(1)).containsKey(CoreCommonConstants.SR_QUERY_ALL);
		verify(arrowheadContext, times(1)).get(CoreCommonConstants.SR_QUERY_ALL);
		verify(httpService, times(1)).sendRequest(any(), any(), any());
		verify(pingMeasurementProperties, times(1)).getTimeout();
		verify(pingService, atLeastOnce()).getPingResponseList(anyString());
		verify(qoSDBService, atLeastOnce()).getOrCreateIntraMeasurement(any(), any());

		verify(qoSDBService, atLeastOnce()).getIntraPingMeasurementByMeasurement(any());
		verify(qoSDBService, atLeastOnce()).createIntraPingMeasurement(any(), any(), any());
		verify(qoSDBService, atLeastOnce()).logIntraMeasurementToDB(any(), any(), any());
		verify(qoSDBService, atLeastOnce()).logIntraMeasurementDetailsToDB(any(), any(), any());
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testExecuteAvailableCalculationWith100PercentOK() {

		final UriComponents uri = Utilities.createURI("HTTPS", "localhost", 12345, "serviceregistry");

		final ServiceRegistryListResponseDTO serviceRegistryResponse = getServiceRegistryListResponseDTOForTest();
		final ResponseEntity<ServiceRegistryListResponseDTO> httpResponse = new ResponseEntity<ServiceRegistryListResponseDTO>(serviceRegistryResponse, HttpStatus.OK);

		final List<IcmpPingResponse> responseList = get100LongResponseListWithIncementingResponseTimeAnd0PercentLossForTest();

		final QoSIntraMeasurement measurement = getQoSIntraMeasurementForTest();

		final QoSIntraPingMeasurementLog measurementLog = getOrCreateMeasurementLogForTest();

		final ArgumentCaptor<PingMeasurementCalculationsDTO> calculationsValueCapture = ArgumentCaptor.forClass(PingMeasurementCalculationsDTO.class);

		final ArgumentCaptor<String> debugValueCapture = ArgumentCaptor.forClass(String.class);
		doNothing().when(logger).debug( debugValueCapture.capture());

		when(arrowheadContext.containsKey(CoreCommonConstants.SERVER_STANDALONE_MODE)).thenReturn(false);
		when(arrowheadContext.containsKey(CoreCommonConstants.SR_QUERY_ALL)).thenReturn(true);
		when(arrowheadContext.get(CoreCommonConstants.SR_QUERY_ALL)).thenReturn(uri);

		when(httpService.sendRequest(uri, HttpMethod.GET, ServiceRegistryListResponseDTO.class)).thenReturn(httpResponse);

		when(pingMeasurementProperties.getTimeout()).thenReturn(5000);
		when(pingMeasurementProperties.getAvailableFromSuccessPercent()).thenReturn(100);

		when(pingService.getPingResponseList(anyString())).thenReturn(responseList);

		when(qoSDBService.getOrCreateIntraMeasurement(any(SystemResponseDTO.class), any())).thenReturn(measurement);

		//in handleMeasurement
		when(qoSDBService.getIntraPingMeasurementByMeasurement(any(QoSIntraMeasurement.class))).thenReturn(Optional.ofNullable(null));
		doNothing().when(qoSDBService).createIntraPingMeasurement(any(), calculationsValueCapture.capture(), any());
		//doNothing().when(qoSDBService).updatePingMeasurement(any(), any(), any(), any());
		when(qoSDBService.logIntraMeasurementToDB(any(), any(), any())).thenReturn(measurementLog);
		doNothing().when(qoSDBService).logIntraMeasurementDetailsToDB(any(), any(), any());

		try {

			pingTask.execute(jobExecutionContext);

		} catch (final JobExecutionException ex) {
			fail();
		}

		final PingMeasurementCalculationsDTO calculations = calculationsValueCapture.getValue();
		assertNotNull(calculations);
		assertTrue(calculations.isAvailable());

		verify(logger, atLeastOnce()).debug(any(String.class));
		final List<String> debugMessages = debugValueCapture.getAllValues();
		assertNotNull(debugMessages);

		verify(arrowheadContext, times(1)).containsKey(CoreCommonConstants.SERVER_STANDALONE_MODE);
		verify(arrowheadContext, times(1)).containsKey(CoreCommonConstants.SR_QUERY_ALL);
		verify(arrowheadContext, times(1)).get(CoreCommonConstants.SR_QUERY_ALL);
		verify(httpService, times(1)).sendRequest(any(), any(), any());
		verify(pingMeasurementProperties, times(1)).getTimeout();
		verify(pingService, atLeastOnce()).getPingResponseList(anyString());
		verify(qoSDBService, atLeastOnce()).getOrCreateIntraMeasurement(any(), any());

		verify(qoSDBService, atLeastOnce()).getIntraPingMeasurementByMeasurement(any());
		verify(qoSDBService, atLeastOnce()).createIntraPingMeasurement(any(), any(), any());
		verify(qoSDBService, atLeastOnce()).logIntraMeasurementToDB(any(), any(), any());
		verify(qoSDBService, atLeastOnce()).logIntraMeasurementDetailsToDB(any(), any(), any());
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testExecuteAvailableCalculationWith1PercentOK() {

		final UriComponents uri = Utilities.createURI("HTTPS", "localhost", 12345, "serviceregistry");

		final ServiceRegistryListResponseDTO serviceRegistryResponse = getServiceRegistryListResponseDTOForTest();
		final ResponseEntity<ServiceRegistryListResponseDTO> httpResponse = new ResponseEntity<ServiceRegistryListResponseDTO>(serviceRegistryResponse, HttpStatus.OK);

		final List<IcmpPingResponse> responseList = get100LongResponseListWith99PercentLossForTest();

		final QoSIntraMeasurement measurement = getQoSIntraMeasurementForTest();

		final QoSIntraPingMeasurementLog measurementLog = getOrCreateMeasurementLogForTest();

		final ArgumentCaptor<PingMeasurementCalculationsDTO> calculationsValueCapture = ArgumentCaptor.forClass(PingMeasurementCalculationsDTO.class);

		final ArgumentCaptor<String> debugValueCapture = ArgumentCaptor.forClass(String.class);
		doNothing().when(logger).debug( debugValueCapture.capture());

		when(arrowheadContext.containsKey(CoreCommonConstants.SERVER_STANDALONE_MODE)).thenReturn(false);
		when(arrowheadContext.containsKey(CoreCommonConstants.SR_QUERY_ALL)).thenReturn(true);
		when(arrowheadContext.get(CoreCommonConstants.SR_QUERY_ALL)).thenReturn(uri);

		when(httpService.sendRequest(uri, HttpMethod.GET, ServiceRegistryListResponseDTO.class)).thenReturn(httpResponse);

		when(pingMeasurementProperties.getTimeout()).thenReturn(5000);
		when(pingMeasurementProperties.getAvailableFromSuccessPercent()).thenReturn(1);

		when(pingService.getPingResponseList(anyString())).thenReturn(responseList);

		when(qoSDBService.getOrCreateIntraMeasurement(any(SystemResponseDTO.class), any())).thenReturn(measurement);

		//in handleMeasurement
		when(qoSDBService.getIntraPingMeasurementByMeasurement(any(QoSIntraMeasurement.class))).thenReturn(Optional.ofNullable(null));
		doNothing().when(qoSDBService).createIntraPingMeasurement(any(), calculationsValueCapture.capture(), any());
		//doNothing().when(qoSDBService).updatePingMeasurement(any(), any(), any(), any());
		when(qoSDBService.logIntraMeasurementToDB(any(), any(), any())).thenReturn(measurementLog);
		doNothing().when(qoSDBService).logIntraMeasurementDetailsToDB(any(), any(), any());

		try {

			pingTask.execute(jobExecutionContext);

		} catch (final JobExecutionException ex) {
			fail();
		}

		final PingMeasurementCalculationsDTO calculations = calculationsValueCapture.getValue();
		assertNotNull(calculations);
		assertTrue(calculations.isAvailable());

		verify(logger, atLeastOnce()).debug(any(String.class));
		final List<String> debugMessages = debugValueCapture.getAllValues();
		assertNotNull(debugMessages);

		verify(arrowheadContext, times(1)).containsKey(CoreCommonConstants.SERVER_STANDALONE_MODE);
		verify(arrowheadContext, times(1)).containsKey(CoreCommonConstants.SR_QUERY_ALL);
		verify(arrowheadContext, times(1)).get(CoreCommonConstants.SR_QUERY_ALL);
		verify(httpService, times(1)).sendRequest(any(), any(), any());
		verify(pingMeasurementProperties, times(1)).getTimeout();
		verify(pingService, atLeastOnce()).getPingResponseList(anyString());
		verify(qoSDBService, atLeastOnce()).getOrCreateIntraMeasurement(any(), any());

		verify(qoSDBService, atLeastOnce()).getIntraPingMeasurementByMeasurement(any());
		verify(qoSDBService, atLeastOnce()).createIntraPingMeasurement(any(), any(), any());
		verify(qoSDBService, atLeastOnce()).logIntraMeasurementToDB(any(), any(), any());
		verify(qoSDBService, atLeastOnce()).logIntraMeasurementDetailsToDB(any(), any(), any());
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testExecuteAvailableCalculationWith1PercentNotOK() {

		final UriComponents uri = Utilities.createURI("HTTPS", "localhost", 12345, "serviceregistry");

		final ServiceRegistryListResponseDTO serviceRegistryResponse = getServiceRegistryListResponseDTOForTest();
		final ResponseEntity<ServiceRegistryListResponseDTO> httpResponse = new ResponseEntity<ServiceRegistryListResponseDTO>(serviceRegistryResponse, HttpStatus.OK);

		final List<IcmpPingResponse> responseList = get100LongResponseListWith99Point9PercentLossForTest();

		final QoSIntraMeasurement measurement = getQoSIntraMeasurementForTest();

		final QoSIntraPingMeasurementLog measurementLog = getOrCreateMeasurementLogForTest();

		final ArgumentCaptor<PingMeasurementCalculationsDTO> calculationsValueCapture = ArgumentCaptor.forClass(PingMeasurementCalculationsDTO.class);

		final ArgumentCaptor<String> debugValueCapture = ArgumentCaptor.forClass(String.class);
		doNothing().when(logger).debug( debugValueCapture.capture());

		when(arrowheadContext.containsKey(CoreCommonConstants.SERVER_STANDALONE_MODE)).thenReturn(false);
		when(arrowheadContext.containsKey(CoreCommonConstants.SR_QUERY_ALL)).thenReturn(true);
		when(arrowheadContext.get(CoreCommonConstants.SR_QUERY_ALL)).thenReturn(uri);

		when(httpService.sendRequest(uri, HttpMethod.GET, ServiceRegistryListResponseDTO.class)).thenReturn(httpResponse);

		when(pingMeasurementProperties.getTimeout()).thenReturn(5000);
		when(pingMeasurementProperties.getAvailableFromSuccessPercent()).thenReturn(1);

		when(pingService.getPingResponseList(anyString())).thenReturn(responseList);

		when(qoSDBService.getOrCreateIntraMeasurement(any(SystemResponseDTO.class), any())).thenReturn(measurement);

		//in handleMeasurement
		when(qoSDBService.getIntraPingMeasurementByMeasurement(any(QoSIntraMeasurement.class))).thenReturn(Optional.ofNullable(null));
		doNothing().when(qoSDBService).createIntraPingMeasurement(any(), calculationsValueCapture.capture(), any());
		//doNothing().when(qoSDBService).updatePingMeasurement(any(), any(), any(), any());
		when(qoSDBService.logIntraMeasurementToDB(any(), any(), any())).thenReturn(measurementLog);
		doNothing().when(qoSDBService).logIntraMeasurementDetailsToDB(any(), any(), any());

		try {

			pingTask.execute(jobExecutionContext);

		} catch (final JobExecutionException ex) {
			fail();
		}

		final PingMeasurementCalculationsDTO calculations = calculationsValueCapture.getValue();
		assertNotNull(calculations);
		assertTrue(calculations.isAvailable());

		verify(logger, atLeastOnce()).debug(any(String.class));
		final List<String> debugMessages = debugValueCapture.getAllValues();
		assertNotNull(debugMessages);

		verify(arrowheadContext, times(1)).containsKey(CoreCommonConstants.SERVER_STANDALONE_MODE);
		verify(arrowheadContext, times(1)).containsKey(CoreCommonConstants.SR_QUERY_ALL);
		verify(arrowheadContext, times(1)).get(CoreCommonConstants.SR_QUERY_ALL);
		verify(httpService, times(1)).sendRequest(any(), any(), any());
		verify(pingMeasurementProperties, times(1)).getTimeout();
		verify(pingService, atLeastOnce()).getPingResponseList(anyString());
		verify(qoSDBService, atLeastOnce()).getOrCreateIntraMeasurement(any(), any());

		verify(qoSDBService, atLeastOnce()).getIntraPingMeasurementByMeasurement(any());
		verify(qoSDBService, atLeastOnce()).createIntraPingMeasurement(any(), any(), any());
		verify(qoSDBService, atLeastOnce()).logIntraMeasurementToDB(any(), any(), any());
		verify(qoSDBService, atLeastOnce()).logIntraMeasurementDetailsToDB(any(), any(), any());
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testExecuteAvailableCalculationWithNotAvailable() {

		final UriComponents uri = Utilities.createURI("HTTPS", "localhost", 12345, "serviceregistry");

		final ServiceRegistryListResponseDTO serviceRegistryResponse = getServiceRegistryListResponseDTOForTest();
		final ResponseEntity<ServiceRegistryListResponseDTO> httpResponse = new ResponseEntity<ServiceRegistryListResponseDTO>(serviceRegistryResponse, HttpStatus.OK);

		final List<IcmpPingResponse> responseList = getResponseListForTest();

		final QoSIntraMeasurement measurement = getQoSIntraMeasurementForTest();

		final QoSIntraPingMeasurementLog measurementLog = getOrCreateMeasurementLogForTest();

		final ArgumentCaptor<PingMeasurementCalculationsDTO> calculationsValueCapture = ArgumentCaptor.forClass(PingMeasurementCalculationsDTO.class);

		final ArgumentCaptor<String> debugValueCapture = ArgumentCaptor.forClass(String.class);
		doNothing().when(logger).debug( debugValueCapture.capture());

		when(arrowheadContext.containsKey(CoreCommonConstants.SERVER_STANDALONE_MODE)).thenReturn(false);
		when(arrowheadContext.containsKey(CoreCommonConstants.SR_QUERY_ALL)).thenReturn(true);
		when(arrowheadContext.get(CoreCommonConstants.SR_QUERY_ALL)).thenReturn(uri);

		when(httpService.sendRequest(uri, HttpMethod.GET, ServiceRegistryListResponseDTO.class)).thenReturn(httpResponse);

		when(pingMeasurementProperties.getTimeout()).thenReturn(5000);
		when(pingMeasurementProperties.getAvailableFromSuccessPercent()).thenReturn(34);

		when(pingService.getPingResponseList(anyString())).thenReturn(responseList);

		when(qoSDBService.getOrCreateIntraMeasurement(any(SystemResponseDTO.class), any())).thenReturn(measurement);

		//in handleMeasurement
		when(qoSDBService.getIntraPingMeasurementByMeasurement(any(QoSIntraMeasurement.class))).thenReturn(Optional.ofNullable(null));
		doNothing().when(qoSDBService).createIntraPingMeasurement(any(), calculationsValueCapture.capture(), any());
		//doNothing().when(qoSDBService).updatePingMeasurement(any(), any(), any(), any());
		when(qoSDBService.logIntraMeasurementToDB(any(), any(), any())).thenReturn(measurementLog);
		doNothing().when(qoSDBService).logIntraMeasurementDetailsToDB(any(), any(), any());

		try {

			pingTask.execute(jobExecutionContext);

		} catch (final JobExecutionException ex) {
			fail();
		}

		final PingMeasurementCalculationsDTO calculations = calculationsValueCapture.getValue();
		assertNotNull(calculations);
		assertTrue(!calculations.isAvailable());

		verify(logger, atLeastOnce()).debug(any(String.class));
		final List<String> debugMessages = debugValueCapture.getAllValues();
		assertNotNull(debugMessages);

		verify(arrowheadContext, times(1)).containsKey(CoreCommonConstants.SERVER_STANDALONE_MODE);
		verify(arrowheadContext, times(1)).containsKey(CoreCommonConstants.SR_QUERY_ALL);
		verify(arrowheadContext, times(1)).get(CoreCommonConstants.SR_QUERY_ALL);
		verify(httpService, times(1)).sendRequest(any(), any(), any());
		verify(pingMeasurementProperties, times(1)).getTimeout();
		verify(pingService, atLeastOnce()).getPingResponseList(anyString());
		verify(qoSDBService, atLeastOnce()).getOrCreateIntraMeasurement(any(), any());

		verify(qoSDBService, atLeastOnce()).getIntraPingMeasurementByMeasurement(any());
		verify(qoSDBService, atLeastOnce()).createIntraPingMeasurement(any(), any(), any());
		verify(qoSDBService, atLeastOnce()).logIntraMeasurementToDB(any(), any(), any());
		verify(qoSDBService, atLeastOnce()).logIntraMeasurementDetailsToDB(any(), any(), any());
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testExecuteMinResponseTime() {

		final UriComponents uri = Utilities.createURI("HTTPS", "localhost", 12345, "serviceregistry");

		final ServiceRegistryListResponseDTO serviceRegistryResponse = getServiceRegistryListResponseDTOForTest();
		final ResponseEntity<ServiceRegistryListResponseDTO> httpResponse = new ResponseEntity<ServiceRegistryListResponseDTO>(serviceRegistryResponse, HttpStatus.OK);

		final List<IcmpPingResponse> responseList = get100LongResponseListWithIncementingResponseTimeAnd10PercentLossForTest();

		final QoSIntraMeasurement measurement = getQoSIntraMeasurementForTest();

		final QoSIntraPingMeasurementLog measurementLog = getOrCreateMeasurementLogForTest();

		final ArgumentCaptor<PingMeasurementCalculationsDTO> calculationsValueCapture = ArgumentCaptor.forClass(PingMeasurementCalculationsDTO.class);

		final ArgumentCaptor<String> debugValueCapture = ArgumentCaptor.forClass(String.class);
		doNothing().when(logger).debug( debugValueCapture.capture());

		when(arrowheadContext.containsKey(CoreCommonConstants.SERVER_STANDALONE_MODE)).thenReturn(false);
		when(arrowheadContext.containsKey(CoreCommonConstants.SR_QUERY_ALL)).thenReturn(true);
		when(arrowheadContext.get(CoreCommonConstants.SR_QUERY_ALL)).thenReturn(uri);

		when(httpService.sendRequest(uri, HttpMethod.GET, ServiceRegistryListResponseDTO.class)).thenReturn(httpResponse);

		when(pingMeasurementProperties.getTimeout()).thenReturn(5000);

		when(pingService.getPingResponseList(anyString())).thenReturn(responseList);

		when(qoSDBService.getOrCreateIntraMeasurement(any(SystemResponseDTO.class), any())).thenReturn(measurement);

		//in handleMeasurement
		when(qoSDBService.getIntraPingMeasurementByMeasurement(any(QoSIntraMeasurement.class))).thenReturn(Optional.ofNullable(null));
		doNothing().when(qoSDBService).createIntraPingMeasurement(any(), calculationsValueCapture.capture(), any());
		//doNothing().when(qoSDBService).updatePingMeasurement(any(), any(), any(), any());
		when(qoSDBService.logIntraMeasurementToDB(any(), any(), any())).thenReturn(measurementLog);
		doNothing().when(qoSDBService).logIntraMeasurementDetailsToDB(any(), any(), any());

		try {

			pingTask.execute(jobExecutionContext);

		} catch (final JobExecutionException ex) {
			fail();
		}

		final PingMeasurementCalculationsDTO calculations = calculationsValueCapture.getValue();
		assertNotNull(calculations);
		assertTrue(1 == calculations.getMinResponseTime());

		verify(logger, atLeastOnce()).debug(any(String.class));
		final List<String> debugMessages = debugValueCapture.getAllValues();
		assertNotNull(debugMessages);

		verify(arrowheadContext, times(1)).containsKey(CoreCommonConstants.SERVER_STANDALONE_MODE);
		verify(arrowheadContext, times(1)).containsKey(CoreCommonConstants.SR_QUERY_ALL);
		verify(arrowheadContext, times(1)).get(CoreCommonConstants.SR_QUERY_ALL);
		verify(httpService, times(1)).sendRequest(any(), any(), any());
		verify(pingMeasurementProperties, times(1)).getTimeout();
		verify(pingService, atLeastOnce()).getPingResponseList(anyString());
		verify(qoSDBService, atLeastOnce()).getOrCreateIntraMeasurement(any(), any());

		verify(qoSDBService, atLeastOnce()).getIntraPingMeasurementByMeasurement(any());
		verify(qoSDBService, atLeastOnce()).createIntraPingMeasurement(any(), any(), any());
		verify(qoSDBService, atLeastOnce()).logIntraMeasurementToDB(any(), any(), any());
		verify(qoSDBService, atLeastOnce()).logIntraMeasurementDetailsToDB(any(), any(), any());
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testExecuteMaxResponseTime() {

		final UriComponents uri = Utilities.createURI("HTTPS", "localhost", 12345, "serviceregistry");

		final ServiceRegistryListResponseDTO serviceRegistryResponse = getServiceRegistryListResponseDTOForTest();
		final ResponseEntity<ServiceRegistryListResponseDTO> httpResponse = new ResponseEntity<ServiceRegistryListResponseDTO>(serviceRegistryResponse, HttpStatus.OK);

		final List<IcmpPingResponse> responseList = get100LongResponseListWithIncementingResponseTimeAnd10PercentLossForTest();

		final QoSIntraMeasurement measurement = getQoSIntraMeasurementForTest();

		final QoSIntraPingMeasurementLog measurementLog = getOrCreateMeasurementLogForTest();

		final ArgumentCaptor<PingMeasurementCalculationsDTO> calculationsValueCapture = ArgumentCaptor.forClass(PingMeasurementCalculationsDTO.class);

		final ArgumentCaptor<String> debugValueCapture = ArgumentCaptor.forClass(String.class);
		doNothing().when(logger).debug( debugValueCapture.capture());

		when(arrowheadContext.containsKey(CoreCommonConstants.SERVER_STANDALONE_MODE)).thenReturn(false);
		when(arrowheadContext.containsKey(CoreCommonConstants.SR_QUERY_ALL)).thenReturn(true);
		when(arrowheadContext.get(CoreCommonConstants.SR_QUERY_ALL)).thenReturn(uri);

		when(httpService.sendRequest(uri, HttpMethod.GET, ServiceRegistryListResponseDTO.class)).thenReturn(httpResponse);

		when(pingMeasurementProperties.getTimeout()).thenReturn(5000);

		when(pingService.getPingResponseList(anyString())).thenReturn(responseList);

		when(qoSDBService.getOrCreateIntraMeasurement(any(SystemResponseDTO.class), any())).thenReturn(measurement);

		//in handleMeasurement
		when(qoSDBService.getIntraPingMeasurementByMeasurement(any(QoSIntraMeasurement.class))).thenReturn(Optional.ofNullable(null));
		doNothing().when(qoSDBService).createIntraPingMeasurement(any(), calculationsValueCapture.capture(), any());
		//doNothing().when(qoSDBService).updatePingMeasurement(any(), any(), any(), any());
		when(qoSDBService.logIntraMeasurementToDB(any(), any(), any())).thenReturn(measurementLog);
		doNothing().when(qoSDBService).logIntraMeasurementDetailsToDB(any(), any(), any());

		try {

			pingTask.execute(jobExecutionContext);

		} catch (final JobExecutionException ex) {
			fail();
		}

		final PingMeasurementCalculationsDTO calculations = calculationsValueCapture.getValue();
		assertNotNull(calculations);
		assertTrue(90 == calculations.getMaxResponseTime());

		verify(logger, atLeastOnce()).debug(any(String.class));
		final List<String> debugMessages = debugValueCapture.getAllValues();
		assertNotNull(debugMessages);

		verify(arrowheadContext, times(1)).containsKey(CoreCommonConstants.SERVER_STANDALONE_MODE);
		verify(arrowheadContext, times(1)).containsKey(CoreCommonConstants.SR_QUERY_ALL);
		verify(arrowheadContext, times(1)).get(CoreCommonConstants.SR_QUERY_ALL);
		verify(httpService, times(1)).sendRequest(any(), any(), any());
		verify(pingMeasurementProperties, times(1)).getTimeout();
		verify(pingService, atLeastOnce()).getPingResponseList(anyString());
		verify(qoSDBService, atLeastOnce()).getOrCreateIntraMeasurement(any(), any());

		verify(qoSDBService, atLeastOnce()).getIntraPingMeasurementByMeasurement(any());
		verify(qoSDBService, atLeastOnce()).createIntraPingMeasurement(any(), any(), any());
		verify(qoSDBService, atLeastOnce()).logIntraMeasurementToDB(any(), any(), any());
		verify(qoSDBService, atLeastOnce()).logIntraMeasurementDetailsToDB(any(), any(), any());
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testExecuteMeanResponseTimeCalculations() {

		final UriComponents uri = Utilities.createURI("HTTPS", "localhost", 12345, "serviceregistry");

		final ServiceRegistryListResponseDTO serviceRegistryResponse = getServiceRegistryListResponseDTOForTest();
		final ResponseEntity<ServiceRegistryListResponseDTO> httpResponse = new ResponseEntity<ServiceRegistryListResponseDTO>(serviceRegistryResponse, HttpStatus.OK);

		final List<IcmpPingResponse> responseList = get100LongResponseListWithIncementingResponseTimeAnd10PercentLossForTest();

		final QoSIntraMeasurement measurement = getQoSIntraMeasurementForTest();

		final QoSIntraPingMeasurementLog measurementLog = getOrCreateMeasurementLogForTest();

		final ArgumentCaptor<PingMeasurementCalculationsDTO> calculationsValueCapture = ArgumentCaptor.forClass(PingMeasurementCalculationsDTO.class);

		final ArgumentCaptor<String> debugValueCapture = ArgumentCaptor.forClass(String.class);
		doNothing().when(logger).debug( debugValueCapture.capture());

		when(arrowheadContext.containsKey(CoreCommonConstants.SERVER_STANDALONE_MODE)).thenReturn(false);
		when(arrowheadContext.containsKey(CoreCommonConstants.SR_QUERY_ALL)).thenReturn(true);
		when(arrowheadContext.get(CoreCommonConstants.SR_QUERY_ALL)).thenReturn(uri);

		when(httpService.sendRequest(uri, HttpMethod.GET, ServiceRegistryListResponseDTO.class)).thenReturn(httpResponse);

		when(pingMeasurementProperties.getTimeout()).thenReturn(5000);

		when(pingService.getPingResponseList(anyString())).thenReturn(responseList);

		when(qoSDBService.getOrCreateIntraMeasurement(any(SystemResponseDTO.class), any())).thenReturn(measurement);

		//in handleMeasurement
		when(qoSDBService.getIntraPingMeasurementByMeasurement(any(QoSIntraMeasurement.class))).thenReturn(Optional.ofNullable(null));
		doNothing().when(qoSDBService).createIntraPingMeasurement(any(), calculationsValueCapture.capture(), any());
		//doNothing().when(qoSDBService).updatePingMeasurement(any(), any(), any(), any());
		when(qoSDBService.logIntraMeasurementToDB(any(), any(), any())).thenReturn(measurementLog);
		doNothing().when(qoSDBService).logIntraMeasurementDetailsToDB(any(), any(), any());

		try {

			pingTask.execute(jobExecutionContext);

		} catch (final JobExecutionException ex) {
			fail();
		}

		final PingMeasurementCalculationsDTO calculations = calculationsValueCapture.getValue();
		assertNotNull(calculations);
		assertTrue(46 == calculations.getMeanResponseTimeWithoutTimeout());
		assertTrue(541 == calculations.getMeanResponseTimeWithTimeout());

		verify(logger, atLeastOnce()).debug(any(String.class));
		final List<String> debugMessages = debugValueCapture.getAllValues();
		assertNotNull(debugMessages);

		verify(arrowheadContext, times(1)).containsKey(CoreCommonConstants.SERVER_STANDALONE_MODE);
		verify(arrowheadContext, times(1)).containsKey(CoreCommonConstants.SR_QUERY_ALL);
		verify(arrowheadContext, times(1)).get(CoreCommonConstants.SR_QUERY_ALL);
		verify(httpService, times(1)).sendRequest(any(), any(), any());
		verify(pingMeasurementProperties, times(1)).getTimeout();
		verify(pingService, atLeastOnce()).getPingResponseList(anyString());
		verify(qoSDBService, atLeastOnce()).getOrCreateIntraMeasurement(any(), any());

		verify(qoSDBService, atLeastOnce()).getIntraPingMeasurementByMeasurement(any());
		verify(qoSDBService, atLeastOnce()).createIntraPingMeasurement(any(), any(), any());
		verify(qoSDBService, atLeastOnce()).logIntraMeasurementToDB(any(), any(), any());
		verify(qoSDBService, atLeastOnce()).logIntraMeasurementDetailsToDB(any(), any(), any());
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testExecuteJitterCalculations() {

		final UriComponents uri = Utilities.createURI("HTTPS", "localhost", 12345, "serviceregistry");

		final ServiceRegistryListResponseDTO serviceRegistryResponse = getServiceRegistryListResponseDTOForTest();
		final ResponseEntity<ServiceRegistryListResponseDTO> httpResponse = new ResponseEntity<ServiceRegistryListResponseDTO>(serviceRegistryResponse, HttpStatus.OK);

		final List<IcmpPingResponse> responseList = get100LongResponseListWithIncementingResponseTimeAnd10PercentLossForTest();

		final QoSIntraMeasurement measurement = getQoSIntraMeasurementForTest();

		final QoSIntraPingMeasurementLog measurementLog = getOrCreateMeasurementLogForTest();

		final ArgumentCaptor<PingMeasurementCalculationsDTO> calculationsValueCapture = ArgumentCaptor.forClass(PingMeasurementCalculationsDTO.class);

		final ArgumentCaptor<String> debugValueCapture = ArgumentCaptor.forClass(String.class);
		doNothing().when(logger).debug( debugValueCapture.capture());

		when(arrowheadContext.containsKey(CoreCommonConstants.SERVER_STANDALONE_MODE)).thenReturn(false);
		when(arrowheadContext.containsKey(CoreCommonConstants.SR_QUERY_ALL)).thenReturn(true);
		when(arrowheadContext.get(CoreCommonConstants.SR_QUERY_ALL)).thenReturn(uri);

		when(httpService.sendRequest(uri, HttpMethod.GET, ServiceRegistryListResponseDTO.class)).thenReturn(httpResponse);

		when(pingMeasurementProperties.getTimeout()).thenReturn(5000);

		when(pingService.getPingResponseList(anyString())).thenReturn(responseList);

		when(qoSDBService.getOrCreateIntraMeasurement(any(SystemResponseDTO.class), any())).thenReturn(measurement);

		//in handleMeasurement
		when(qoSDBService.getIntraPingMeasurementByMeasurement(any(QoSIntraMeasurement.class))).thenReturn(Optional.ofNullable(null));
		doNothing().when(qoSDBService).createIntraPingMeasurement(any(), calculationsValueCapture.capture(), any());
		//doNothing().when(qoSDBService).updatePingMeasurement(any(), any(), any(), any());
		when(qoSDBService.logIntraMeasurementToDB(any(), any(), any())).thenReturn(measurementLog);
		doNothing().when(qoSDBService).logIntraMeasurementDetailsToDB(any(), any(), any());

		try {

			pingTask.execute(jobExecutionContext);

		} catch (final JobExecutionException ex) {
			fail();
		}

		final PingMeasurementCalculationsDTO calculations = calculationsValueCapture.getValue();
		assertNotNull(calculations);
		assertTrue(1487 == calculations.getJitterWithTimeout());
		assertTrue(26 == calculations.getJitterWithoutTimeout());

		verify(logger, atLeastOnce()).debug(any(String.class));
		final List<String> debugMessages = debugValueCapture.getAllValues();
		assertNotNull(debugMessages);

		verify(arrowheadContext, times(1)).containsKey(CoreCommonConstants.SERVER_STANDALONE_MODE);
		verify(arrowheadContext, times(1)).containsKey(CoreCommonConstants.SR_QUERY_ALL);
		verify(arrowheadContext, times(1)).get(CoreCommonConstants.SR_QUERY_ALL);
		verify(httpService, times(1)).sendRequest(any(), any(), any());
		verify(pingMeasurementProperties, times(1)).getTimeout();
		verify(pingService, atLeastOnce()).getPingResponseList(anyString());
		verify(qoSDBService, atLeastOnce()).getOrCreateIntraMeasurement(any(), any());

		verify(qoSDBService, atLeastOnce()).getIntraPingMeasurementByMeasurement(any());
		verify(qoSDBService, atLeastOnce()).createIntraPingMeasurement(any(), any(), any());
		verify(qoSDBService, atLeastOnce()).logIntraMeasurementToDB(any(), any(), any());
		verify(qoSDBService, atLeastOnce()).logIntraMeasurementDetailsToDB(any(), any(), any());
	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testExecuteGetPingMeasurementByMeasurementThrowsException() {

		final UriComponents uri = Utilities.createURI("HTTPS", "localhost", 12345, "serviceregistry");

		final ServiceRegistryListResponseDTO serviceRegistryResponse = getServiceRegistryListResponseDTOForTest();

		final ResponseEntity<ServiceRegistryListResponseDTO> httpResponse = new ResponseEntity<ServiceRegistryListResponseDTO>(serviceRegistryResponse, HttpStatus.OK);

		final List<IcmpPingResponse> responseList = getResponseListForTest();

		final QoSIntraMeasurement measurement = getQoSIntraMeasurementForTest();

		final QoSIntraPingMeasurementLog measurementLog = getOrCreateMeasurementLogForTest();

		final ArgumentCaptor<String> debugValueCapture = ArgumentCaptor.forClass(String.class);
		doNothing().when(logger).debug( debugValueCapture.capture());

		when(arrowheadContext.containsKey(CoreCommonConstants.SERVER_STANDALONE_MODE)).thenReturn(false);
		when(arrowheadContext.containsKey(CoreCommonConstants.SR_QUERY_ALL)).thenReturn(true);
		when(arrowheadContext.get(CoreCommonConstants.SR_QUERY_ALL)).thenReturn(uri);

		when(httpService.sendRequest(uri, HttpMethod.GET, ServiceRegistryListResponseDTO.class)).thenReturn(httpResponse);

		when(pingMeasurementProperties.getTimeout()).thenReturn(5000);

		when(pingService.getPingResponseList(anyString())).thenReturn(responseList);

		when(qoSDBService.getOrCreateIntraMeasurement(any(SystemResponseDTO.class), any())).thenReturn(measurement);

		//in handleMeasurement
		when(qoSDBService.getIntraPingMeasurementByMeasurement(any(QoSIntraMeasurement.class))).thenThrow(ArrowheadException.class);
		doNothing().when(qoSDBService).createIntraPingMeasurement(any(), any(), any());
		//doNothing().when(qoSDBService).updatePingMeasurement(any(), any(), any(), any());
		when(qoSDBService.logIntraMeasurementToDB(any(), any(), any())).thenReturn(measurementLog);
		doNothing().when(qoSDBService).logIntraMeasurementDetailsToDB(any(), any(), any());

		try {

			pingTask.execute(jobExecutionContext);

		} catch (final JobExecutionException ex) {
			fail();
		} catch (final ArrowheadException ex) {

			verify(logger, atLeastOnce()).debug(any(String.class));
			final List<String> debugMessages = debugValueCapture.getAllValues();
			assertNotNull(debugMessages);

			verify(arrowheadContext, times(1)).containsKey(CoreCommonConstants.SERVER_STANDALONE_MODE);
			verify(arrowheadContext, times(1)).containsKey(CoreCommonConstants.SR_QUERY_ALL);
			verify(arrowheadContext, times(1)).get(CoreCommonConstants.SR_QUERY_ALL);
			verify(httpService, times(1)).sendRequest(any(), any(), any());
			verify(pingService, times(1)).getPingResponseList(anyString());
			verify(qoSDBService,times(1)).getOrCreateIntraMeasurement(any(), any());
			verify(pingMeasurementProperties, atLeastOnce()).getTimeout();

			verify(qoSDBService, times(1)).getIntraPingMeasurementByMeasurement(any());
			verify(qoSDBService, times(0)).createIntraPingMeasurement(any(), any(), any());
			verify(qoSDBService, times(0)).logIntraMeasurementToDB(any(), any(), any());
			verify(qoSDBService, times(0)).logIntraMeasurementDetailsToDB(any(), any(), any());

			throw ex;
		}

	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testExecuteCreatePingMeasurementThrowsException() {

		final UriComponents uri = Utilities.createURI("HTTPS", "localhost", 12345, "serviceregistry");

		final ServiceRegistryListResponseDTO serviceRegistryResponse = getServiceRegistryListResponseDTOForTest();

		final ResponseEntity<ServiceRegistryListResponseDTO> httpResponse = new ResponseEntity<ServiceRegistryListResponseDTO>(serviceRegistryResponse, HttpStatus.OK);

		final List<IcmpPingResponse> responseList = getResponseListForTest();

		final QoSIntraMeasurement measurement = getQoSIntraMeasurementForTest();

		final QoSIntraPingMeasurementLog measurementLog = getOrCreateMeasurementLogForTest();

		final ArgumentCaptor<String> debugValueCapture = ArgumentCaptor.forClass(String.class);
		doNothing().when(logger).debug( debugValueCapture.capture());

		when(arrowheadContext.containsKey(CoreCommonConstants.SERVER_STANDALONE_MODE)).thenReturn(false);
		when(arrowheadContext.containsKey(CoreCommonConstants.SR_QUERY_ALL)).thenReturn(true);
		when(arrowheadContext.get(CoreCommonConstants.SR_QUERY_ALL)).thenReturn(uri);

		when(httpService.sendRequest(uri, HttpMethod.GET, ServiceRegistryListResponseDTO.class)).thenReturn(httpResponse);

		when(pingMeasurementProperties.getTimeout()).thenReturn(5000);

		when(pingService.getPingResponseList(anyString())).thenReturn(responseList);

		when(qoSDBService.getOrCreateIntraMeasurement(any(SystemResponseDTO.class), any())).thenReturn(measurement);

		//in handleMeasurement
		when(qoSDBService.getIntraPingMeasurementByMeasurement(any(QoSIntraMeasurement.class))).thenReturn(Optional.ofNullable(null));
		doThrow(ArrowheadException.class).when(qoSDBService).createIntraPingMeasurement(any(), any(), any());
		//doNothing().when(qoSDBService).updatePingMeasurement(any(), any(), any(), any());
		when(qoSDBService.logIntraMeasurementToDB(any(), any(), any())).thenReturn(measurementLog);
		doNothing().when(qoSDBService).logIntraMeasurementDetailsToDB(any(), any(), any());

		try {

			pingTask.execute(jobExecutionContext);

		} catch (final JobExecutionException ex) {
			fail();
		} catch (final ArrowheadException ex) {

			verify(logger, atLeastOnce()).debug(any(String.class));
			final List<String> debugMessages = debugValueCapture.getAllValues();
			assertNotNull(debugMessages);

			verify(arrowheadContext, times(1)).containsKey(CoreCommonConstants.SERVER_STANDALONE_MODE);
			verify(arrowheadContext, times(1)).containsKey(CoreCommonConstants.SR_QUERY_ALL);
			verify(arrowheadContext, times(1)).get(CoreCommonConstants.SR_QUERY_ALL);
			verify(httpService, times(1)).sendRequest(any(), any(), any());
			verify(pingService, times(1)).getPingResponseList(anyString());
			verify(qoSDBService,times(1)).getOrCreateIntraMeasurement(any(), any());
			verify(pingMeasurementProperties, atLeastOnce()).getTimeout();

			verify(qoSDBService, times(1)).getIntraPingMeasurementByMeasurement(any());
			verify(qoSDBService, times(1)).createIntraPingMeasurement(any(), any(), any());
			verify(qoSDBService, times(0)).logIntraMeasurementToDB(any(), any(), any());
			verify(qoSDBService, times(0)).logIntraMeasurementDetailsToDB(any(), any(), any());

			throw ex;
		}

	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testExecuteLogMeasurementToDBThrowsException() {

		final UriComponents uri = Utilities.createURI("HTTPS", "localhost", 12345, "serviceregistry");

		final ServiceRegistryListResponseDTO serviceRegistryResponse = getServiceRegistryListResponseDTOForTest();

		final ResponseEntity<ServiceRegistryListResponseDTO> httpResponse = new ResponseEntity<ServiceRegistryListResponseDTO>(serviceRegistryResponse, HttpStatus.OK);

		final List<IcmpPingResponse> responseList = getResponseListForTest();

		final QoSIntraMeasurement measurement = getQoSIntraMeasurementForTest();

		final ArgumentCaptor<String> debugValueCapture = ArgumentCaptor.forClass(String.class);
		doNothing().when(logger).debug( debugValueCapture.capture());

		when(arrowheadContext.containsKey(CoreCommonConstants.SERVER_STANDALONE_MODE)).thenReturn(false);
		when(arrowheadContext.containsKey(CoreCommonConstants.SR_QUERY_ALL)).thenReturn(true);
		when(arrowheadContext.get(CoreCommonConstants.SR_QUERY_ALL)).thenReturn(uri);

		when(httpService.sendRequest(uri, HttpMethod.GET, ServiceRegistryListResponseDTO.class)).thenReturn(httpResponse);

		when(pingMeasurementProperties.getTimeout()).thenReturn(5000);

		when(pingService.getPingResponseList(anyString())).thenReturn(responseList);

		when(qoSDBService.getOrCreateIntraMeasurement(any(SystemResponseDTO.class), any())).thenReturn(measurement);

		//in handleMeasurement
		when(qoSDBService.getIntraPingMeasurementByMeasurement(any(QoSIntraMeasurement.class))).thenReturn(Optional.ofNullable(null));
		doNothing().when(qoSDBService).createIntraPingMeasurement(any(), any(), any());
		//doNothing().when(qoSDBService).updatePingMeasurement(any(), any(), any(), any());
		when(qoSDBService.logIntraMeasurementToDB(any(), any(), any())).thenThrow(ArrowheadException.class);
		doNothing().when(qoSDBService).logIntraMeasurementDetailsToDB(any(), any(), any());

		try {

			pingTask.execute(jobExecutionContext);

		} catch (final JobExecutionException ex) {
			fail();
		} catch (final ArrowheadException ex) {

			verify(logger, atLeastOnce()).debug(any(String.class));
			final List<String> debugMessages = debugValueCapture.getAllValues();
			assertNotNull(debugMessages);

			verify(arrowheadContext, times(1)).containsKey(CoreCommonConstants.SERVER_STANDALONE_MODE);
			verify(arrowheadContext, times(1)).containsKey(CoreCommonConstants.SR_QUERY_ALL);
			verify(arrowheadContext, times(1)).get(CoreCommonConstants.SR_QUERY_ALL);
			verify(httpService, times(1)).sendRequest(any(), any(), any());
			verify(pingService, times(1)).getPingResponseList(anyString());
			verify(qoSDBService,times(1)).getOrCreateIntraMeasurement(any(), any());
			verify(pingMeasurementProperties, atLeastOnce()).getTimeout();

			verify(qoSDBService, times(1)).getIntraPingMeasurementByMeasurement(any());
			verify(qoSDBService, times(1)).createIntraPingMeasurement(any(), any(), any());
			verify(qoSDBService, times(1)).logIntraMeasurementToDB(any(), any(), any());
			verify(qoSDBService, times(0)).logIntraMeasurementDetailsToDB(any(), any(), any());

			throw ex;
		}

	}


	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testExecuteLogMeasurementDetailsToDBThrowsException() {

		final UriComponents uri = Utilities.createURI("HTTPS", "localhost", 12345, "serviceregistry");

		final ServiceRegistryListResponseDTO serviceRegistryResponse = getServiceRegistryListResponseDTOForTest();

		final ResponseEntity<ServiceRegistryListResponseDTO> httpResponse = new ResponseEntity<ServiceRegistryListResponseDTO>(serviceRegistryResponse, HttpStatus.OK);

		final List<IcmpPingResponse> responseList = getResponseListForTest();

		final QoSIntraMeasurement measurement = getQoSIntraMeasurementForTest();

		final QoSIntraPingMeasurementLog measurementLog = getOrCreateMeasurementLogForTest();

		final ArgumentCaptor<String> debugValueCapture = ArgumentCaptor.forClass(String.class);
		doNothing().when(logger).debug( debugValueCapture.capture());

		when(arrowheadContext.containsKey(CoreCommonConstants.SERVER_STANDALONE_MODE)).thenReturn(false);
		when(arrowheadContext.containsKey(CoreCommonConstants.SR_QUERY_ALL)).thenReturn(true);
		when(arrowheadContext.get(CoreCommonConstants.SR_QUERY_ALL)).thenReturn(uri);

		when(httpService.sendRequest(uri, HttpMethod.GET, ServiceRegistryListResponseDTO.class)).thenReturn(httpResponse);

		when(pingMeasurementProperties.getTimeout()).thenReturn(5000);

		when(pingService.getPingResponseList(anyString())).thenReturn(responseList);

		when(qoSDBService.getOrCreateIntraMeasurement(any(SystemResponseDTO.class), any())).thenReturn(measurement);

		//in handleMeasurement
		when(qoSDBService.getIntraPingMeasurementByMeasurement(any(QoSIntraMeasurement.class))).thenReturn(Optional.ofNullable(null));
		doNothing().when(qoSDBService).createIntraPingMeasurement(any(), any(), any());
		//doNothing().when(qoSDBService).updatePingMeasurement(any(), any(), any(), any());
		when(qoSDBService.logIntraMeasurementToDB(any(), any(), any())).thenReturn(measurementLog);
		doThrow(ArrowheadException.class).when(qoSDBService).logIntraMeasurementDetailsToDB(any(), any(), any());

		try {

			pingTask.execute(jobExecutionContext);

		} catch (final JobExecutionException ex) {
			fail();
		} catch (final ArrowheadException ex) {

			verify(logger, atLeastOnce()).debug(any(String.class));
			final List<String> debugMessages = debugValueCapture.getAllValues();
			assertNotNull(debugMessages);

			verify(arrowheadContext, times(1)).containsKey(CoreCommonConstants.SERVER_STANDALONE_MODE);
			verify(arrowheadContext, times(1)).containsKey(CoreCommonConstants.SR_QUERY_ALL);
			verify(arrowheadContext, times(1)).get(CoreCommonConstants.SR_QUERY_ALL);
			verify(httpService, times(1)).sendRequest(any(), any(), any());
			verify(pingService, times(1)).getPingResponseList(anyString());
			verify(qoSDBService,times(1)).getOrCreateIntraMeasurement(any(), any());
			verify(pingMeasurementProperties, atLeastOnce()).getTimeout();

			verify(qoSDBService, times(1)).getIntraPingMeasurementByMeasurement(any());
			verify(qoSDBService, times(1)).createIntraPingMeasurement(any(), any(), any());
			verify(qoSDBService, times(1)).logIntraMeasurementToDB(any(), any(), any());
			verify(qoSDBService, times(1)).logIntraMeasurementDetailsToDB(any(), any(), any());

			throw ex;
		}

	}

	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	public ServiceRegistryListResponseDTO getServiceRegistryListResponseDTOForTest() {

		final int sizeOfResponse = 3;
		final ServiceRegistryListResponseDTO serviceRegistryListResponseDTO = new ServiceRegistryListResponseDTO();
		final List<ServiceRegistryResponseDTO> responseList = new ArrayList<>(sizeOfResponse);

		for (int i = 0; i < sizeOfResponse; i++) {
			responseList.add(getServiceRegistryResponseDTOForTest());
		}

		serviceRegistryListResponseDTO.setCount(sizeOfResponse);
		serviceRegistryListResponseDTO.setData(responseList);

		return serviceRegistryListResponseDTO;
	}

	//-------------------------------------------------------------------------------------------------
	public ServiceRegistryListResponseDTO getServiceRegistryListResponseDTOWithDifferentProviderAddressForTest(final int numberOfProviders) {

		final int sizeOfResponse = numberOfProviders;
		final ServiceRegistryListResponseDTO serviceRegistryListResponseDTO = new ServiceRegistryListResponseDTO();
		final List<ServiceRegistryResponseDTO> responseList = new ArrayList<>(sizeOfResponse);

		for (int i = 0; i < sizeOfResponse; i++) {
			final ServiceRegistryResponseDTO response = getServiceRegistryResponseDTOForTest();
			response.getProvider().setAddress("address" + i);

			responseList.add(response);
		}

		serviceRegistryListResponseDTO.setCount(sizeOfResponse);
		serviceRegistryListResponseDTO.setData(responseList);

		return serviceRegistryListResponseDTO;
	}

	//-------------------------------------------------------------------------------------------------
	public ServiceRegistryListResponseDTO getServiceRegistryListResponseDTOWithNullSystemAddressForTest() {

		final int sizeOfResponse = 3;
		final ServiceRegistryListResponseDTO serviceRegistryListResponseDTO = new ServiceRegistryListResponseDTO();
		final List<ServiceRegistryResponseDTO> responseList = new ArrayList<>(sizeOfResponse);

		for (int i = 0; i < sizeOfResponse; i++) {
			responseList.add(getServiceRegistryResponseDTOWithNullSystemAddressForTest());
		}

		serviceRegistryListResponseDTO.setCount(sizeOfResponse);
		serviceRegistryListResponseDTO.setData(responseList);

		return serviceRegistryListResponseDTO;
	}

	//-------------------------------------------------------------------------------------------------
	public ServiceRegistryListResponseDTO getServiceRegistryListResponseDTOWithEmptySystemAddressForTest() {

		final int sizeOfResponse = 3;
		final ServiceRegistryListResponseDTO serviceRegistryListResponseDTO = new ServiceRegistryListResponseDTO();
		final List<ServiceRegistryResponseDTO> responseList = new ArrayList<>(sizeOfResponse);

		for (int i = 0; i < sizeOfResponse; i++) {
			responseList.add(getServiceRegistryResponseDTOWithEmptySystemAddressForTest());
		}

		serviceRegistryListResponseDTO.setCount(sizeOfResponse);
		serviceRegistryListResponseDTO.setData(responseList);

		return serviceRegistryListResponseDTO;
	}

	//-------------------------------------------------------------------------------------------------
	public ServiceRegistryResponseDTO getServiceRegistryResponseDTOForTest() {

		final ServiceRegistryResponseDTO serviceRegistryResponseDTO = new ServiceRegistryResponseDTO();

		serviceRegistryResponseDTO.setId(1L);
		serviceRegistryResponseDTO.setServiceDefinition(getServiceDefinitionResponseDTOForTest());
		serviceRegistryResponseDTO.setProvider(getSystemResponseDTOForTest());
		serviceRegistryResponseDTO.setServiceUri("serviceUri");
		serviceRegistryResponseDTO.setEndOfValidity(null);
		serviceRegistryResponseDTO.setSecure(ServiceSecurityType.CERTIFICATE);
		serviceRegistryResponseDTO.setMetadata(null);
		serviceRegistryResponseDTO.setVersion(1);
		serviceRegistryResponseDTO.setInterfaces(List.of(getServiceInterfaceResponseDTOForTest()));
		serviceRegistryResponseDTO.setCreatedAt(Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()));
		serviceRegistryResponseDTO.setUpdatedAt(Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()));

		return serviceRegistryResponseDTO;
	}

	//-------------------------------------------------------------------------------------------------
	public ServiceRegistryResponseDTO getServiceRegistryResponseDTOWithNullSystemAddressForTest() {

		final ServiceRegistryResponseDTO serviceRegistryResponseDTO = new ServiceRegistryResponseDTO();

		serviceRegistryResponseDTO.setId(1L);
		serviceRegistryResponseDTO.setServiceDefinition(getServiceDefinitionResponseDTOForTest());
		serviceRegistryResponseDTO.setProvider(getSystemResponseDTOWithNullAddressForTest());
		serviceRegistryResponseDTO.setServiceUri("serviceUri");
		serviceRegistryResponseDTO.setEndOfValidity(null);
		serviceRegistryResponseDTO.setSecure(ServiceSecurityType.CERTIFICATE);
		serviceRegistryResponseDTO.setMetadata(null);
		serviceRegistryResponseDTO.setVersion(1);
		serviceRegistryResponseDTO.setInterfaces(List.of(getServiceInterfaceResponseDTOForTest()));
		serviceRegistryResponseDTO.setCreatedAt(Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()));
		serviceRegistryResponseDTO.setUpdatedAt(Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()));

		return serviceRegistryResponseDTO;
	}

	//-------------------------------------------------------------------------------------------------
	public ServiceRegistryResponseDTO getServiceRegistryResponseDTOWithEmptySystemAddressForTest() {

		final ServiceRegistryResponseDTO serviceRegistryResponseDTO = new ServiceRegistryResponseDTO();

		serviceRegistryResponseDTO.setId(1L);
		serviceRegistryResponseDTO.setServiceDefinition(getServiceDefinitionResponseDTOForTest());
		serviceRegistryResponseDTO.setProvider(getSystemResponseDTOWithEmptyAddressForTest());
		serviceRegistryResponseDTO.setServiceUri("serviceUri");
		serviceRegistryResponseDTO.setEndOfValidity(null);
		serviceRegistryResponseDTO.setSecure(ServiceSecurityType.CERTIFICATE);
		serviceRegistryResponseDTO.setMetadata(null);
		serviceRegistryResponseDTO.setVersion(1);
		serviceRegistryResponseDTO.setInterfaces(List.of(getServiceInterfaceResponseDTOForTest()));
		serviceRegistryResponseDTO.setCreatedAt(Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()));
		serviceRegistryResponseDTO.setUpdatedAt(Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()));

		return serviceRegistryResponseDTO;
	}

	//-------------------------------------------------------------------------------------------------
	public ServiceDefinitionResponseDTO getServiceDefinitionResponseDTOForTest() {

		final ServiceDefinitionResponseDTO serviceDefinitionResponseDTO = new ServiceDefinitionResponseDTO();

		serviceDefinitionResponseDTO.setId(1L);
		serviceDefinitionResponseDTO.setServiceDefinition("serviceDefinition");
		serviceDefinitionResponseDTO.setCreatedAt(Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()));
		serviceDefinitionResponseDTO.setUpdatedAt(Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()));

		return serviceDefinitionResponseDTO;
	}

	//-------------------------------------------------------------------------------------------------
	private System getSystemForTest() {

		final System system = new System(
				"testSystem",
				"address",
				AddressType.HOSTNAME,
				12345,
				"authenticationInfo",
				"");

		return system;
	}

	//-------------------------------------------------------------------------------------------------
	private System getSystemWithNullAddressForTest() {

		final System system = new System(
				"testSystem",
				null,
				null,
				12345,
				"authenticationInfo",
				"");

		return system;
	}

	//-------------------------------------------------------------------------------------------------
	private SystemResponseDTO getSystemResponseDTOWithNullAddressForTest() {

		final SystemResponseDTO systemResponseDTO = DTOConverter.convertSystemToSystemResponseDTO(getSystemWithNullAddressForTest());

		return systemResponseDTO;
	}

	//-------------------------------------------------------------------------------------------------
	private System getSystemWithEmptyAddressForTest() {

		final System system = new System(
				"testSystem",
				"   ",
				null,
				12345,
				"authenticationInfo",
				"");

		return system;
	}

	//-------------------------------------------------------------------------------------------------
	private SystemResponseDTO getSystemResponseDTOWithEmptyAddressForTest() {

		final SystemResponseDTO systemResponseDTO = DTOConverter.convertSystemToSystemResponseDTO(getSystemWithEmptyAddressForTest());

		return systemResponseDTO;
	}

	//-------------------------------------------------------------------------------------------------
	private SystemResponseDTO getSystemResponseDTOForTest() {

		final SystemResponseDTO systemResponseDTO = DTOConverter.convertSystemToSystemResponseDTO(getSystemForTest());

		return systemResponseDTO;
	}

	//-------------------------------------------------------------------------------------------------
	private ServiceInterfaceResponseDTO getServiceInterfaceResponseDTOForTest() {

		final ServiceInterfaceResponseDTO serviceInterfaceResponseDTO = new ServiceInterfaceResponseDTO();
		serviceInterfaceResponseDTO.setId(1L);
		serviceInterfaceResponseDTO.setInterfaceName("interfaceName");
		serviceInterfaceResponseDTO.setCreatedAt(Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()));
		serviceInterfaceResponseDTO.setUpdatedAt(Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()));

		return serviceInterfaceResponseDTO;
	}

	//-------------------------------------------------------------------------------------------------
	private List<IcmpPingResponse> getResponseListForTest() {

		final List<IcmpPingResponse> responseList = new ArrayList<>();
		for (int i = 0; i < TIME_TO_REPEAT_PING; i++) {

			if (i%2 == 0) {
				final IcmpPingResponse pingResponse = getIcmpPingResponse();
				pingResponse.setSuccessFlag(false);
				pingResponse.setTimeoutFlag(true);
				pingResponse.setRtt(0);
				pingResponse.setDuration(0);

				responseList.add(pingResponse);
			}else {
				responseList.add(getIcmpPingResponse());
			}
		}

		return responseList;
	}

	//-------------------------------------------------------------------------------------------------
	private List<IcmpPingResponse> get100LongResponseListWithIncementingResponseTimeAnd10PercentLossForTest() {

		final List<IcmpPingResponse> responseList = new ArrayList<>();
		for (int i = 1; i < 91; i++) {

			final IcmpPingResponse pingResponse = getIcmpPingResponse();
			pingResponse.setSuccessFlag(true);
			pingResponse.setTimeoutFlag(false);
			pingResponse.setRtt(i);
			pingResponse.setDuration(i);

			responseList.add(pingResponse);
		}

		for (int i = 0; i < 10; i++) {

			final IcmpPingResponse pingResponse = getIcmpPingResponse();
			pingResponse.setSuccessFlag(false);
			pingResponse.setTimeoutFlag(true);

			responseList.add(pingResponse);
		}

		return responseList;
	}

	//-------------------------------------------------------------------------------------------------
	private List<IcmpPingResponse> get100LongResponseListWith99PercentLossForTest() {

		final List<IcmpPingResponse> responseList = new ArrayList<>();
		
		final IcmpPingResponse pingResponse = getIcmpPingResponse();
		pingResponse.setSuccessFlag(true);
		pingResponse.setTimeoutFlag(false);
		pingResponse.setRtt(1);
		pingResponse.setDuration(1);

		responseList.add(pingResponse);

		for (int i = 0; i < 100; i++) {

			final IcmpPingResponse pingResponseInLoop= getIcmpPingResponse();
			pingResponse.setSuccessFlag(false);
			pingResponse.setTimeoutFlag(true);

			responseList.add(pingResponseInLoop);
		}

		return responseList;
	}

	//-------------------------------------------------------------------------------------------------
	private List<IcmpPingResponse> get100LongResponseListWith99Point9PercentLossForTest() {

		final List<IcmpPingResponse> responseList = new ArrayList<>();
		
		final IcmpPingResponse pingResponse = getIcmpPingResponse();
		pingResponse.setSuccessFlag(true);
		pingResponse.setTimeoutFlag(false);
		pingResponse.setRtt(1);
		pingResponse.setDuration(1);

		responseList.add(pingResponse);

		for (int i = 0; i < 200; i++) {

			final IcmpPingResponse pingResponseInLoop= getIcmpPingResponse();
			pingResponse.setSuccessFlag(false);
			pingResponse.setTimeoutFlag(true);

			responseList.add(pingResponseInLoop);
		}

		return responseList;
	}
	//-------------------------------------------------------------------------------------------------
	private List<IcmpPingResponse> get100LongResponseListWithIncementingResponseTimeAnd0PercentLossForTest() {

		final List<IcmpPingResponse> responseList = new ArrayList<>();
		for (int i = 1; i < 101; i++) {

			final IcmpPingResponse pingResponse = getIcmpPingResponse();
			pingResponse.setSuccessFlag(true);
			pingResponse.setTimeoutFlag(false);
			pingResponse.setRtt(i);
			pingResponse.setDuration(i);

			responseList.add(pingResponse);
		}

		return responseList;
	}
	//-------------------------------------------------------------------------------------------------
	private List<IcmpPingResponse> getEmptyResponseListForTest() {

		final List<IcmpPingResponse> responseList = List.of();

		return responseList;
	}

	//-------------------------------------------------------------------------------------------------
	private IcmpPingResponse getIcmpPingResponse() {

		final IcmpPingResponse pingResponse = new IcmpPingResponse();
		pingResponse.setSuccessFlag(true);
		pingResponse.setTimeoutFlag(false);
		pingResponse.setRtt(1);
		pingResponse.setDuration(1L);
		pingResponse.setTtl(64);
		pingResponse.setSize(32);
		pingResponse.setHost("localhost");

		return pingResponse;
	}

	//-------------------------------------------------------------------------------------------------
	private QoSIntraMeasurement getQoSIntraMeasurementForTest() {

		final System system = getSystemForTest();
		final QoSIntraMeasurement measurement = new QoSIntraMeasurement(
				system, 
				QoSMeasurementType.PING, 
				ZonedDateTime.now());

		return measurement;
	}

	//-------------------------------------------------------------------------------------------------
	private QoSIntraPingMeasurement getQosIntraPingMeasurementForTest() {

		final QoSIntraMeasurement measurement = getQoSIntraMeasurementForTest();

		final QoSIntraPingMeasurement pingMeasurement = new QoSIntraPingMeasurement();

		pingMeasurement.setMeasurement(measurement);
		pingMeasurement.setAvailable(true);
		pingMeasurement.setMaxResponseTime(1);
		pingMeasurement.setMinResponseTime(1);
		pingMeasurement.setMeanResponseTimeWithoutTimeout(1);
		pingMeasurement.setMeanResponseTimeWithTimeout(1);
		pingMeasurement.setJitterWithoutTimeout(1);
		pingMeasurement.setJitterWithTimeout(1);
		pingMeasurement.setLostPerMeasurementPercent(0);
		pingMeasurement.setCountStartedAt(ZonedDateTime.now());
		pingMeasurement.setLastAccessAt(ZonedDateTime.now());
		pingMeasurement.setSent(35);
		pingMeasurement.setSentAll(35);
		pingMeasurement.setReceived(35);
		pingMeasurement.setReceivedAll(35);

		return pingMeasurement;
	}

	//-------------------------------------------------------------------------------------------------
	private QoSIntraPingMeasurementLog getOrCreateMeasurementLogForTest() {

		final QoSIntraPingMeasurementLog measurementLog = new QoSIntraPingMeasurementLog();
		measurementLog.setAvailable(true);

		return measurementLog;
	}

}
