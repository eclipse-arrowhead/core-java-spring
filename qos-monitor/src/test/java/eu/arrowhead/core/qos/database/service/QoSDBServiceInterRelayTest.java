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

package eu.arrowhead.core.qos.database.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.hibernate.HibernateException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit4.SpringRunner;

import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.entity.Cloud;
import eu.arrowhead.common.database.entity.QoSInterRelayEchoMeasurement;
import eu.arrowhead.common.database.entity.QoSInterRelayEchoMeasurementLog;
import eu.arrowhead.common.database.entity.QoSInterRelayMeasurement;
import eu.arrowhead.common.database.entity.Relay;
import eu.arrowhead.common.database.repository.QoSInterRelayEchoMeasurementLogRepository;
import eu.arrowhead.common.database.repository.QoSInterRelayEchoMeasurementRepository;
import eu.arrowhead.common.database.repository.QoSInterRelayMeasurementRepository;
import eu.arrowhead.common.dto.internal.CloudResponseDTO;
import eu.arrowhead.common.dto.internal.DTOConverter;
import eu.arrowhead.common.dto.internal.QoSMeasurementAttribute;
import eu.arrowhead.common.dto.internal.RelayResponseDTO;
import eu.arrowhead.common.dto.internal.RelayType;
import eu.arrowhead.common.dto.shared.QoSMeasurementStatus;
import eu.arrowhead.common.dto.shared.QoSMeasurementType;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.core.qos.dto.RelayEchoMeasurementCalculationsDTO;
import eu.arrowhead.core.qos.dto.RelayEchoMeasurementDetailsDTO;

@RunWith(SpringRunner.class)
public class QoSDBServiceInterRelayTest {

	//=================================================================================================
	// members
	
	@InjectMocks
	private QoSDBService qosDBService;
	
	@Mock
	private QoSInterRelayMeasurementRepository qosInterRelayMeasurementRepository;

	@Mock
	private QoSInterRelayEchoMeasurementRepository qosInterRelayEchoMeasurementRepository;

	@Mock
	private QoSInterRelayEchoMeasurementLogRepository qosInterRelayEchoMeasurementLogRepository;
	
	//=================================================================================================
	// methods
	
	//=================================================================================================
	// Tests of updateInterRelayCountStartedAt
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void updateInterRelayCountStartedAtTest() {
		final ZonedDateTime testStartedAt = ZonedDateTime.now().minusMinutes(1);
		final List<QoSInterRelayEchoMeasurement> measurementList = getQoSInterRelayEchoMeasurementListForTest(3);
		final ArgumentCaptor<List> valueCapture = ArgumentCaptor.forClass(List.class);

		when(qosInterRelayEchoMeasurementRepository.findAll()).thenReturn(measurementList);
		when(qosInterRelayEchoMeasurementRepository.saveAll(valueCapture.capture())).thenReturn(List.of());
		doNothing().when(qosInterRelayEchoMeasurementRepository).flush();

		qosDBService.updateInterRelayCountStartedAt();

		verify(qosInterRelayEchoMeasurementRepository, times(1)).findAll();
		verify(qosInterRelayEchoMeasurementRepository, times(1)).saveAll(any());
		verify(qosInterRelayEchoMeasurementRepository, times(1)).flush();

		final List<QoSInterRelayEchoMeasurement> captured = valueCapture.getValue();
		assertEquals(measurementList.size(), captured.size());
		for (final QoSInterRelayEchoMeasurement measurement : captured) {
			assertEquals(0, measurement.getSent());
			assertEquals(0, measurement.getReceived());
			assertTrue(measurement.getCountStartedAt().isAfter(testStartedAt));
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test(expected = ArrowheadException.class)
	public void updateInterRelayCountStartedAtFlushThrowDatabaseExceptionTest() {
		final ZonedDateTime testStartedAt = ZonedDateTime.now().minusMinutes(1);
		final List<QoSInterRelayEchoMeasurement> measurementList = getQoSInterRelayEchoMeasurementListForTest(3);
		final ArgumentCaptor<List> valueCapture = ArgumentCaptor.forClass(List.class);

		when(qosInterRelayEchoMeasurementRepository.findAll()).thenReturn(measurementList);
		when(qosInterRelayEchoMeasurementRepository.saveAll(valueCapture.capture())).thenReturn(List.of());
		doThrow(HibernateException.class).when(qosInterRelayEchoMeasurementRepository).flush();
		
		try {
			qosDBService.updateInterRelayCountStartedAt();			
		} catch (final Exception ex) {
			verify(qosInterRelayEchoMeasurementRepository, times(1)).findAll();
			verify(qosInterRelayEchoMeasurementRepository, times(1)).saveAll(any());
			verify(qosInterRelayEchoMeasurementRepository, times(1)).flush();
			
			final List<QoSInterRelayEchoMeasurement> captured = valueCapture.getValue();
			assertEquals(measurementList.size(), captured.size());
			for (final QoSInterRelayEchoMeasurement measurement : captured) {
				assertEquals(0, measurement.getSent());
				assertEquals(0, measurement.getReceived());
				assertTrue(measurement.getCountStartedAt().isAfter(testStartedAt));
			}
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test(expected = ArrowheadException.class)
	public void updateInterRelayCountStartedAtSaveAllThrowDatabaseExceptionTest() {
		final ZonedDateTime testStartedAt = ZonedDateTime.now().minusMinutes(1);
		final List<QoSInterRelayEchoMeasurement> measurementList = getQoSInterRelayEchoMeasurementListForTest(3);
		final ArgumentCaptor<List> valueCapture = ArgumentCaptor.forClass(List.class);

		when(qosInterRelayEchoMeasurementRepository.findAll()).thenReturn(measurementList);
		when(qosInterRelayEchoMeasurementRepository.saveAll(valueCapture.capture())).thenThrow(HibernateException.class);
		doNothing().when(qosInterRelayEchoMeasurementRepository).flush();
		
		try {
			qosDBService.updateInterRelayCountStartedAt();			
		} catch (final Exception ex) {
			verify(qosInterRelayEchoMeasurementRepository, times(1)).findAll();
			verify(qosInterRelayEchoMeasurementRepository, times(1)).saveAll(any());
			verify(qosInterRelayEchoMeasurementRepository, times(0)).flush();
			
			final List<QoSInterRelayEchoMeasurement> captured = valueCapture.getValue();
			assertEquals(measurementList.size(), captured.size());
			for (final QoSInterRelayEchoMeasurement measurement : captured) {
				assertEquals(0, measurement.getSent());
				assertEquals(0, measurement.getReceived());
				assertTrue(measurement.getCountStartedAt().isAfter(testStartedAt));
			}
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void updateInterRelayCountStartedAtFindAllThrowDatabaseExceptionTest() {
		final List<QoSInterRelayEchoMeasurement> measurementList = getQoSInterRelayEchoMeasurementListForTest(3);
		
		when(qosInterRelayEchoMeasurementRepository.findAll()).thenThrow(HibernateException.class);
		when(qosInterRelayEchoMeasurementRepository.saveAll(any())).thenReturn(measurementList);
		doNothing().when(qosInterRelayEchoMeasurementRepository).flush();
		
		try {
			qosDBService.updateInterRelayCountStartedAt();			
		} catch (final Exception ex) {
			verify(qosInterRelayEchoMeasurementRepository, times(1)).findAll();
			verify(qosInterRelayEchoMeasurementRepository, times(0)).saveAll(any());
			verify(qosInterRelayEchoMeasurementRepository, times(0)).flush();
			throw ex;
		}
	}
	
	//=================================================================================================
	// Tests of getInterRelayMeasurement
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getInterRelayMeasurementTest() {
		final QoSInterRelayMeasurement measurement = getQoSInterRelayMeasurementForTest(QoSMeasurementType.RELAY_ECHO, QoSMeasurementStatus.FINISHED);
		final CloudResponseDTO cloudDTO = DTOConverter.convertCloudToCloudResponseDTO(measurement.getCloud());
		final RelayResponseDTO relayDTO = DTOConverter.convertRelayToRelayResponseDTO(measurement.getRelay());
		
		when(qosInterRelayMeasurementRepository.findByCloudAndRelayAndMeasurementType(any(), any(), any())).thenReturn(Optional.of(measurement));
		final Optional<QoSInterRelayMeasurement> result = qosDBService.getInterRelayMeasurement(cloudDTO, relayDTO, QoSMeasurementType.RELAY_ECHO);
		
		verify(qosInterRelayMeasurementRepository, times(1)).findByCloudAndRelayAndMeasurementType(any(), any(), any());
		assertTrue(result.isPresent());
		assertEquals(measurement.getCloud().getName(), result.get().getCloud().getName());
		assertEquals(measurement.getRelay().getAddress(), result.get().getRelay().getAddress());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void getInterRelayMeasurementWithNullCloudTest() {
		final QoSInterRelayMeasurement measurement = getQoSInterRelayMeasurementForTest(QoSMeasurementType.RELAY_ECHO, QoSMeasurementStatus.FINISHED);
		final RelayResponseDTO relayDTO = DTOConverter.convertRelayToRelayResponseDTO(measurement.getRelay());
		
		when(qosInterRelayMeasurementRepository.findByCloudAndRelayAndMeasurementType(any(), any(), any())).thenReturn(Optional.of(measurement));
	
		try {
			qosDBService.getInterRelayMeasurement(null, relayDTO, QoSMeasurementType.RELAY_ECHO);			
		} catch (final Exception ex) {
			verify(qosInterRelayMeasurementRepository, times(0)).findByCloudAndRelayAndMeasurementType(any(), any(), any());
			throw ex;
		}
		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void getInterRelayMeasurementWithNullTypeTest() {
		final QoSInterRelayMeasurement measurement = getQoSInterRelayMeasurementForTest(QoSMeasurementType.RELAY_ECHO, QoSMeasurementStatus.FINISHED);
		final RelayResponseDTO relayDTO = DTOConverter.convertRelayToRelayResponseDTO(measurement.getRelay());
		
		when(qosInterRelayMeasurementRepository.findByCloudAndRelayAndMeasurementType(any(), any(), any())).thenReturn(Optional.of(measurement));
	
		try {
			qosDBService.getInterRelayMeasurement(null, relayDTO, QoSMeasurementType.RELAY_ECHO);			
		} catch (final Exception ex) {
			verify(qosInterRelayMeasurementRepository, times(0)).findByCloudAndRelayAndMeasurementType(any(), any(), any());
			throw ex;
		}
		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void getInterRelayMeasurementWithNullRelayTest() {
		final QoSInterRelayMeasurement measurement = getQoSInterRelayMeasurementForTest(QoSMeasurementType.RELAY_ECHO, QoSMeasurementStatus.FINISHED);
		final CloudResponseDTO cloudDTO = DTOConverter.convertCloudToCloudResponseDTO(measurement.getCloud());
		final RelayResponseDTO relayDTO = DTOConverter.convertRelayToRelayResponseDTO(measurement.getRelay());
		
		when(qosInterRelayMeasurementRepository.findByCloudAndRelayAndMeasurementType(any(), any(), any())).thenReturn(Optional.of(measurement));
	
		try {
			qosDBService.getInterRelayMeasurement(cloudDTO, relayDTO, null);			
		} catch (final Exception ex) {
			verify(qosInterRelayMeasurementRepository, times(0)).findByCloudAndRelayAndMeasurementType(any(), any(), any());
			throw ex;
		}
		
	}
	
	//=================================================================================================
	// Tests of createInterRelayMeasurement
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void createInterRelayMeasurementTest() {
		final QoSInterRelayMeasurement measurement = getQoSInterRelayMeasurementForTest(QoSMeasurementType.RELAY_ECHO, QoSMeasurementStatus.FINISHED);
		
		final ArgumentCaptor<QoSInterRelayMeasurement> valueCapture = ArgumentCaptor.forClass(QoSInterRelayMeasurement.class);
		when(qosInterRelayMeasurementRepository.saveAndFlush(valueCapture.capture())).thenReturn(measurement);
		
		qosDBService.createInterRelayMeasurement(measurement.getCloud(), measurement.getRelay(), measurement.getMeasurementType(), measurement.getLastMeasurementAt());
		
		verify(qosInterRelayMeasurementRepository, times(1)).saveAndFlush(any());
		final QoSInterRelayMeasurement captured = valueCapture.getValue();
		assertEquals(measurement.getCloud().getName(), captured.getCloud().getName());
		assertEquals(measurement.getRelay().getAddress(), captured.getRelay().getAddress());		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void createInterRelayMeasurementSaveAndFlushThrowDatabaseExceptionTest() {
		final QoSInterRelayMeasurement measurement = getQoSInterRelayMeasurementForTest(QoSMeasurementType.RELAY_ECHO, QoSMeasurementStatus.FINISHED);
		
		final ArgumentCaptor<QoSInterRelayMeasurement> valueCapture = ArgumentCaptor.forClass(QoSInterRelayMeasurement.class);
		when(qosInterRelayMeasurementRepository.saveAndFlush(valueCapture.capture())).thenThrow(HibernateException.class);
		
		try {
			qosDBService.createInterRelayMeasurement(measurement.getCloud(), measurement.getRelay(), measurement.getMeasurementType(), measurement.getLastMeasurementAt());			
		} catch (final Exception ex) {
			verify(qosInterRelayMeasurementRepository, times(1)).saveAndFlush(any());
			final QoSInterRelayMeasurement captured = valueCapture.getValue();
			assertEquals(measurement.getCloud().getName(), captured.getCloud().getName());
			assertEquals(measurement.getRelay().getAddress(), captured.getRelay().getAddress());		
			throw ex;
		}		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void createInterRelayMeasurementNullCloudTest() {
		final QoSInterRelayMeasurement measurement = getQoSInterRelayMeasurementForTest(QoSMeasurementType.RELAY_ECHO, QoSMeasurementStatus.FINISHED);
		try {
			qosDBService.createInterRelayMeasurement(null, measurement.getRelay(), measurement.getMeasurementType(), measurement.getLastMeasurementAt());			
		} catch (final Exception ex) {
			verify(qosInterRelayMeasurementRepository, times(0)).saveAndFlush(any());
			throw ex;
		}		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void createInterRelayMeasurementNullRelayTest() {
		final QoSInterRelayMeasurement measurement = getQoSInterRelayMeasurementForTest(QoSMeasurementType.RELAY_ECHO, QoSMeasurementStatus.FINISHED);
		try {
			qosDBService.createInterRelayMeasurement(measurement.getCloud(), null, measurement.getMeasurementType(), measurement.getLastMeasurementAt());			
		} catch (final Exception ex) {
			verify(qosInterRelayMeasurementRepository, times(0)).saveAndFlush(any());
			throw ex;
		}		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void createInterRelayMeasurementInvalidRelayTest() {
		final QoSInterRelayMeasurement measurement = getQoSInterRelayMeasurementForTest(QoSMeasurementType.RELAY_ECHO, QoSMeasurementStatus.FINISHED);
		measurement.getRelay().setType(RelayType.GATEKEEPER_RELAY);
		try {
			qosDBService.createInterRelayMeasurement(measurement.getCloud(), null, measurement.getMeasurementType(), measurement.getLastMeasurementAt());			
		} catch (final Exception ex) {
			verify(qosInterRelayMeasurementRepository, times(0)).saveAndFlush(any());
			throw ex;
		}		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void createInterRelayMeasurementNullTypeTest() {
		final QoSInterRelayMeasurement measurement = getQoSInterRelayMeasurementForTest(QoSMeasurementType.RELAY_ECHO, QoSMeasurementStatus.FINISHED);
		try {
			qosDBService.createInterRelayMeasurement(measurement.getCloud(), measurement.getRelay(), null, measurement.getLastMeasurementAt());			
		} catch (final Exception ex) {
			verify(qosInterRelayMeasurementRepository, times(0)).saveAndFlush(any());
			throw ex;
		}		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void createInterRelayMeasurementNullAroundNowTest() {
		final QoSInterRelayMeasurement measurement = getQoSInterRelayMeasurementForTest(QoSMeasurementType.RELAY_ECHO, QoSMeasurementStatus.FINISHED);
		try {
			qosDBService.createInterRelayMeasurement(measurement.getCloud(), measurement.getRelay(), measurement.getMeasurementType(), null);			
		} catch (final Exception ex) {
			verify(qosInterRelayMeasurementRepository, times(0)).saveAndFlush(any());
			throw ex;
		}		
	}
	
	//=================================================================================================
	// Tests of getOrCreateInterRelayMeasurement
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getOrCreateInterRelayMeasurementGetTest() {
		final String nowStr = Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now());
		final QoSInterRelayMeasurement measurement = getQoSInterRelayMeasurementForTest(QoSMeasurementType.RELAY_ECHO, QoSMeasurementStatus.FINISHED);
		final CloudResponseDTO cloudResp = new CloudResponseDTO(1L, "test-op", "test-n", true, true, false, "dvgdafvgadsg", nowStr, nowStr);
		final Cloud cloud = DTOConverter.convertCloudResponseDTOToCloud(cloudResp);
		final RelayResponseDTO relayResp = new RelayResponseDTO(6L, "1.1.1.1", 10000, null, true, false, RelayType.GATEWAY_RELAY, nowStr, nowStr);
		final Relay relay = DTOConverter.convertRelayResponseDTOToRelay(relayResp);
		measurement.setCloud(cloud);
		measurement.setRelay(relay);
		
		when(qosInterRelayMeasurementRepository.findByCloudAndRelayAndMeasurementType(any(), any(), any())).thenReturn(Optional.of(measurement));
		final QoSInterRelayMeasurement result = qosDBService.getOrCreateInterRelayMeasurement(cloudResp, relayResp, QoSMeasurementType.RELAY_ECHO);
		
		verify(qosInterRelayMeasurementRepository, times(1)).findByCloudAndRelayAndMeasurementType(eq(cloud), eq(relay), eq(QoSMeasurementType.RELAY_ECHO));
		assertEquals(cloudResp.getAuthenticationInfo(), result.getCloud().getAuthenticationInfo());
		assertEquals(QoSMeasurementStatus.PENDING, result.getStatus());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getOrCreateInterRelayMeasurementCreateTest() {
		final String nowStr = Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now());
		final QoSInterRelayMeasurement measurement = getQoSInterRelayMeasurementForTest(QoSMeasurementType.RELAY_ECHO, QoSMeasurementStatus.FINISHED);
		final CloudResponseDTO cloudResp = new CloudResponseDTO(1L, "test-op", "test-n", true, true, false, "dvgdafvgadsg", nowStr, nowStr);
		final Cloud cloud = DTOConverter.convertCloudResponseDTOToCloud(cloudResp);
		final RelayResponseDTO relayResp = new RelayResponseDTO(6L, "1.1.1.1", 10000, null, true, false, RelayType.GATEWAY_RELAY, nowStr, nowStr);
		final Relay relay = DTOConverter.convertRelayResponseDTOToRelay(relayResp);
		measurement.setCloud(cloud);
		measurement.setRelay(relay);
		
		when(qosInterRelayMeasurementRepository.findByCloudAndRelayAndMeasurementType(any(), any(), any())).thenReturn(Optional.empty());
		when(qosInterRelayMeasurementRepository.saveAndFlush(any())).thenReturn(measurement);
		final QoSInterRelayMeasurement result = qosDBService.getOrCreateInterRelayMeasurement(cloudResp, relayResp, QoSMeasurementType.RELAY_ECHO);
		
		verify(qosInterRelayMeasurementRepository, times(1)).findByCloudAndRelayAndMeasurementType(eq(cloud), eq(relay), eq(QoSMeasurementType.RELAY_ECHO));
		verify(qosInterRelayMeasurementRepository, times(1)).saveAndFlush(any());
		assertEquals(cloudResp.getAuthenticationInfo(), result.getCloud().getAuthenticationInfo());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void getOrCreateInterRelayMeasurementSaveAndFlushThrowDatabaseExceptionTest() {
		final String nowStr = Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now());
		final QoSInterRelayMeasurement measurement = getQoSInterRelayMeasurementForTest(QoSMeasurementType.RELAY_ECHO, QoSMeasurementStatus.FINISHED);
		final CloudResponseDTO cloudResp = new CloudResponseDTO(1L, "test-op", "test-n", true, true, false, "dvgdafvgadsg", nowStr, nowStr);
		final Cloud cloud = DTOConverter.convertCloudResponseDTOToCloud(cloudResp);
		final RelayResponseDTO relayResp = new RelayResponseDTO(6L, "1.1.1.1", 10000, null, true, false, RelayType.GATEWAY_RELAY, nowStr, nowStr);
		final Relay relay = DTOConverter.convertRelayResponseDTOToRelay(relayResp);
		measurement.setCloud(cloud);
		measurement.setRelay(relay);
		
		when(qosInterRelayMeasurementRepository.findByCloudAndRelayAndMeasurementType(any(), any(), any())).thenReturn(Optional.of(measurement));
		when(qosInterRelayMeasurementRepository.saveAndFlush(any())).thenThrow(HibernateException.class);
		
		try {
			qosDBService.getOrCreateInterRelayMeasurement(cloudResp, relayResp, QoSMeasurementType.RELAY_ECHO);			
		} catch (final Exception ex) {
			verify(qosInterRelayMeasurementRepository, times(1)).findByCloudAndRelayAndMeasurementType(eq(cloud), eq(relay), eq(QoSMeasurementType.RELAY_ECHO));
			verify(qosInterRelayMeasurementRepository, times(1)).saveAndFlush(any());
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void getOrCreateInterRelayMeasurementNullCloudTest() {
		final String nowStr = Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now());
		final QoSInterRelayMeasurement measurement = getQoSInterRelayMeasurementForTest(QoSMeasurementType.RELAY_ECHO, QoSMeasurementStatus.FINISHED);
		final CloudResponseDTO cloudResp = new CloudResponseDTO(1L, "test-op", "test-n", true, true, false, "dvgdafvgadsg", nowStr, nowStr);
		final Cloud cloud = DTOConverter.convertCloudResponseDTOToCloud(cloudResp);
		final RelayResponseDTO relayResp = new RelayResponseDTO(6L, "1.1.1.1", 10000, null, true, false, RelayType.GATEWAY_RELAY, nowStr, nowStr);
		final Relay relay = DTOConverter.convertRelayResponseDTOToRelay(relayResp);
		measurement.setCloud(cloud);
		measurement.setRelay(relay);
			
		try {
			qosDBService.getOrCreateInterRelayMeasurement(null, relayResp, QoSMeasurementType.RELAY_ECHO);			
		} catch (final Exception ex) {
			verify(qosInterRelayMeasurementRepository, times(0)).findByCloudAndRelayAndMeasurementType(eq(cloud), eq(relay), eq(QoSMeasurementType.RELAY_ECHO));
			verify(qosInterRelayMeasurementRepository, times(0)).saveAndFlush(any());
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void getOrCreateInterRelayMeasurementNullRelayTest() {
		final String nowStr = Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now());
		final QoSInterRelayMeasurement measurement = getQoSInterRelayMeasurementForTest(QoSMeasurementType.RELAY_ECHO, QoSMeasurementStatus.FINISHED);
		final CloudResponseDTO cloudResp = new CloudResponseDTO(1L, "test-op", "test-n", true, true, false, "dvgdafvgadsg", nowStr, nowStr);
		final Cloud cloud = DTOConverter.convertCloudResponseDTOToCloud(cloudResp);
		final RelayResponseDTO relayResp = new RelayResponseDTO(6L, "1.1.1.1", 10000, null, true, false, RelayType.GATEWAY_RELAY, nowStr, nowStr);
		final Relay relay = DTOConverter.convertRelayResponseDTOToRelay(relayResp);
		measurement.setCloud(cloud);
		measurement.setRelay(relay);
			
		try {
			qosDBService.getOrCreateInterRelayMeasurement(cloudResp, null, QoSMeasurementType.RELAY_ECHO);			
		} catch (final Exception ex) {
			verify(qosInterRelayMeasurementRepository, times(0)).findByCloudAndRelayAndMeasurementType(eq(cloud), eq(relay), eq(QoSMeasurementType.RELAY_ECHO));
			verify(qosInterRelayMeasurementRepository, times(0)).saveAndFlush(any());
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void getOrCreateInterRelayMeasurementNullTypeTest() {
		final String nowStr = Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now());
		final QoSInterRelayMeasurement measurement = getQoSInterRelayMeasurementForTest(QoSMeasurementType.RELAY_ECHO, QoSMeasurementStatus.FINISHED);
		final CloudResponseDTO cloudResp = new CloudResponseDTO(1L, "test-op", "test-n", true, true, false, "dvgdafvgadsg", nowStr, nowStr);
		final Cloud cloud = DTOConverter.convertCloudResponseDTOToCloud(cloudResp);
		final RelayResponseDTO relayResp = new RelayResponseDTO(6L, "1.1.1.1", 10000, null, true, false, RelayType.GATEWAY_RELAY, nowStr, nowStr);
		final Relay relay = DTOConverter.convertRelayResponseDTOToRelay(relayResp);
		measurement.setCloud(cloud);
		measurement.setRelay(relay);
			
		try {
			qosDBService.getOrCreateInterRelayMeasurement(cloudResp, relayResp, null);			
		} catch (final Exception ex) {
			verify(qosInterRelayMeasurementRepository, times(0)).findByCloudAndRelayAndMeasurementType(eq(cloud), eq(relay), eq(QoSMeasurementType.RELAY_ECHO));
			verify(qosInterRelayMeasurementRepository, times(0)).saveAndFlush(any());
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void getOrCreateInterRelayMeasurementInvalidRelayTest() {
		final String nowStr = Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now());
		final QoSInterRelayMeasurement measurement = getQoSInterRelayMeasurementForTest(QoSMeasurementType.RELAY_ECHO, QoSMeasurementStatus.FINISHED);
		final CloudResponseDTO cloudResp = new CloudResponseDTO(1L, "test-op", "test-n", true, true, false, "dvgdafvgadsg", nowStr, nowStr);
		final Cloud cloud = DTOConverter.convertCloudResponseDTOToCloud(cloudResp);
		final RelayResponseDTO relayResp = new RelayResponseDTO(6L, "1.1.1.1", 10000, null, true, false, RelayType.GATEKEEPER_RELAY, nowStr, nowStr);
		final Relay relay = DTOConverter.convertRelayResponseDTOToRelay(relayResp);
		measurement.setCloud(cloud);
		measurement.setRelay(relay);
			
		try {
			qosDBService.getOrCreateInterRelayMeasurement(cloudResp, relayResp, QoSMeasurementType.RELAY_ECHO);			
		} catch (final Exception ex) {
			verify(qosInterRelayMeasurementRepository, times(0)).findByCloudAndRelayAndMeasurementType(eq(cloud), eq(relay), eq(QoSMeasurementType.RELAY_ECHO));
			verify(qosInterRelayMeasurementRepository, times(0)).saveAndFlush(any());
			throw ex;
		}
	}
	
	//=================================================================================================
	// Tests of changeInterRelayMeasurementStatusById
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void changeInterRelayMeasurementStatusByIdTest() {
		final long id = 6L;
		final QoSMeasurementStatus originalStatus = QoSMeasurementStatus.FINISHED;
		final QoSMeasurementStatus changedStatus = QoSMeasurementStatus.PENDING;
		final QoSInterRelayMeasurement measurement = getQoSInterRelayMeasurementForTest(QoSMeasurementType.RELAY_ECHO, originalStatus);
		
		when(qosInterRelayMeasurementRepository.findById(eq(id))).thenReturn(Optional.of(measurement));
		final ArgumentCaptor<QoSInterRelayMeasurement> valueCapture = ArgumentCaptor.forClass(QoSInterRelayMeasurement.class);		
		when(qosInterRelayMeasurementRepository.saveAndFlush(valueCapture.capture())).thenReturn(any());
		
		qosDBService.changeInterRelayMeasurementStatusById(id, changedStatus);
		
		verify(qosInterRelayMeasurementRepository, times(1)).findById(eq(id));
		verify(qosInterRelayMeasurementRepository, times(1)).saveAndFlush(any());
		assertEquals(changedStatus, valueCapture.getValue().getStatus());
		assertEquals(measurement.getCloud(), valueCapture.getValue().getCloud());
		assertEquals(measurement.getRelay(), valueCapture.getValue().getRelay());
		assertEquals(measurement.getMeasurementType(), valueCapture.getValue().getMeasurementType());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void changeInterRelayMeasurementStatusByIdSaveAndFlushThrowDatabaseExceptionTest() {
		final long id = 6L;
		final QoSMeasurementStatus originalStatus = QoSMeasurementStatus.FINISHED;
		final QoSMeasurementStatus changedStatus = QoSMeasurementStatus.PENDING;
		final QoSInterRelayMeasurement measurement = getQoSInterRelayMeasurementForTest(QoSMeasurementType.RELAY_ECHO, originalStatus);
		
		when(qosInterRelayMeasurementRepository.findById(eq(id))).thenReturn(Optional.of(measurement));
		final ArgumentCaptor<QoSInterRelayMeasurement> valueCapture = ArgumentCaptor.forClass(QoSInterRelayMeasurement.class);		
		when(qosInterRelayMeasurementRepository.saveAndFlush(valueCapture.capture())).thenThrow(HibernateException.class);
		
		try {
			qosDBService.changeInterRelayMeasurementStatusById(id, changedStatus);			
		} catch (final Exception ex) {
			verify(qosInterRelayMeasurementRepository, times(1)).findById(eq(id));
			verify(qosInterRelayMeasurementRepository, times(1)).saveAndFlush(any());
			assertEquals(changedStatus, valueCapture.getValue().getStatus());
			assertEquals(measurement.getCloud(), valueCapture.getValue().getCloud());
			assertEquals(measurement.getRelay(), valueCapture.getValue().getRelay());
			assertEquals(measurement.getMeasurementType(), valueCapture.getValue().getMeasurementType());
			throw ex;
		}		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void changeInterRelayMeasurementStatusByIdMeasurementNotExistsTest() {
		final long id = 6L;
//		final QoSMeasurementStatus originalStatus = QoSMeasurementStatus.FINISHED;
		final QoSMeasurementStatus changedStatus = QoSMeasurementStatus.PENDING;
//		final QoSInterRelayMeasurement measurement = getQoSInterRelayMeasurementForTest(QoSMeasurementType.RELAY_ECHO, originalStatus);
		
		when(qosInterRelayMeasurementRepository.findById(eq(id))).thenReturn(Optional.empty());
		
		try {
			qosDBService.changeInterRelayMeasurementStatusById(id, changedStatus);			
		} catch (final Exception ex) {
			verify(qosInterRelayMeasurementRepository, times(1)).findById(any());
			verify(qosInterRelayMeasurementRepository, times(0)).saveAndFlush(any());
			throw ex;
		}		
	}
	
	//=================================================================================================
	// Tests of createInterRelayEchoMeasurement
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void createInterRelayEchoMeasurementTest() {
		final QoSInterRelayEchoMeasurement echoMeasurement = getQoSInterRelayEchoMeasurementforTest();
		final RelayEchoMeasurementCalculationsDTO calculations = getCalculationsForTest();
		when(qosInterRelayEchoMeasurementRepository.saveAndFlush(any())).thenReturn(echoMeasurement);
		
		qosDBService.createInterRelayEchoMeasurement(echoMeasurement.getMeasurement(), calculations, ZonedDateTime.now());	
		verify(qosInterRelayEchoMeasurementRepository, times(1)).saveAndFlush(any());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void createInterRelayEchoMeasurementSaveAndFlushThrowDatabaseExceptionTest() {
		final QoSInterRelayEchoMeasurement echoMeasurement = getQoSInterRelayEchoMeasurementforTest();
		final RelayEchoMeasurementCalculationsDTO calculations = getCalculationsForTest();
		when(qosInterRelayEchoMeasurementRepository.saveAndFlush(any())).thenThrow(HibernateException.class);
		
		try {
			qosDBService.createInterRelayEchoMeasurement(echoMeasurement.getMeasurement(), calculations, ZonedDateTime.now());				
		} catch (final Exception ex) {
			verify(qosInterRelayEchoMeasurementRepository, times(1)).saveAndFlush(any());
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void createInterRelayEchoMeasurementNullMeasurementTest() {
		final RelayEchoMeasurementCalculationsDTO calculations = getCalculationsForTest();
		final QoSInterRelayEchoMeasurement echoMeasurement = getQoSInterRelayEchoMeasurementforTest();
		when(qosInterRelayEchoMeasurementRepository.saveAndFlush(any())).thenReturn(echoMeasurement);
		
		try {
			qosDBService.createInterRelayEchoMeasurement(null, calculations, ZonedDateTime.now());				
		} catch (final Exception ex) {
			verify(qosInterRelayEchoMeasurementRepository, times(0)).saveAndFlush(any());
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void createInterRelayEchoMeasurementNullCalculationsTest() {
		final QoSInterRelayEchoMeasurement echoMeasurement = getQoSInterRelayEchoMeasurementforTest();
		when(qosInterRelayEchoMeasurementRepository.saveAndFlush(any())).thenReturn(echoMeasurement);
		
		try {
			qosDBService.createInterRelayEchoMeasurement(echoMeasurement.getMeasurement(), null, ZonedDateTime.now());				
		} catch (final Exception ex) {
			verify(qosInterRelayEchoMeasurementRepository, times(0)).saveAndFlush(any());
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void createInterRelayEchoMeasurementNullAroundNowTest() {
		final QoSInterRelayEchoMeasurement echoMeasurement = getQoSInterRelayEchoMeasurementforTest();
		final RelayEchoMeasurementCalculationsDTO calculations = getCalculationsForTest();
		when(qosInterRelayEchoMeasurementRepository.saveAndFlush(any())).thenReturn(echoMeasurement);
		
		try {
			qosDBService.createInterRelayEchoMeasurement(echoMeasurement.getMeasurement(), calculations, null);				
		} catch (final Exception ex) {
			verify(qosInterRelayEchoMeasurementRepository, times(0)).saveAndFlush(any());
			throw ex;
		}
	}
	
	//=================================================================================================
	// Tests of getInterRelayEchoMeasurementByMeasurement
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getInterRelayEchoMeasurementByMeasurementTest() {
		final QoSInterRelayEchoMeasurement echoMeasurement = getQoSInterRelayEchoMeasurementforTest();
		when(qosInterRelayEchoMeasurementRepository.findByMeasurement(any())).thenReturn(Optional.of(echoMeasurement));
		
		qosDBService.getInterRelayEchoMeasurementByMeasurement(echoMeasurement.getMeasurement());
		
		verify(qosInterRelayEchoMeasurementRepository, times(1)).findByMeasurement(eq(echoMeasurement.getMeasurement()));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void getInterRelayEchoMeasurementByMeasurementNullMeasurementTest() {
		try {
			qosDBService.getInterRelayEchoMeasurementByMeasurement(null);
		} catch (final Exception ex) {
			verify(qosInterRelayEchoMeasurementRepository, times(0)).findByMeasurement(any());
			throw ex;
		}		
	}
	
	//=================================================================================================
	// Tests of logInterRelayEchoMeasurementToDB
 	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void logInterRelayEchoMeasurementToDBTest() {
		final QoSInterRelayMeasurement measurement = getQoSInterRelayMeasurementForTest(QoSMeasurementType.RELAY_ECHO, QoSMeasurementStatus.FINISHED);
		final RelayEchoMeasurementDetailsDTO measurementDetails = getRelayEchoMeasurementDetailsDTOForTest();
		
		final ArgumentCaptor<List> valueCapture = ArgumentCaptor.forClass(List.class);
		when(qosInterRelayEchoMeasurementLogRepository.saveAll(valueCapture.capture())).thenReturn(List.of());
		doNothing().when(qosInterRelayEchoMeasurementLogRepository).flush();
		
		qosDBService.logInterRelayEchoMeasurementToDB(measurement, List.of(measurementDetails), ZonedDateTime.now());
		
		verify(qosInterRelayEchoMeasurementLogRepository, times(1)).saveAll(any());
		verify(qosInterRelayEchoMeasurementLogRepository, times(1)).flush();
		
		final List<QoSInterRelayEchoMeasurementLog> capturedValue = valueCapture.getValue();
		assertEquals(1, capturedValue.size());
		assertEquals(measurementDetails.getSize(), capturedValue.get(0).getSize());
		assertEquals(measurement.getCloud().getName(), capturedValue.get(0).getMeasurement().getCloud().getName());
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test(expected = ArrowheadException.class)
	public void logInterRelayEchoMeasurementToDBSaveAllThrowDatabaseExceptionTest() {
		final QoSInterRelayMeasurement measurement = getQoSInterRelayMeasurementForTest(QoSMeasurementType.RELAY_ECHO, QoSMeasurementStatus.FINISHED);
		final RelayEchoMeasurementDetailsDTO measurementDetails = getRelayEchoMeasurementDetailsDTOForTest();
		
		final ArgumentCaptor<List> valueCapture = ArgumentCaptor.forClass(List.class);
		when(qosInterRelayEchoMeasurementLogRepository.saveAll(valueCapture.capture())).thenThrow(HibernateException.class);
		doNothing().when(qosInterRelayEchoMeasurementLogRepository).flush();
		
		try {
			qosDBService.logInterRelayEchoMeasurementToDB(measurement, List.of(measurementDetails), ZonedDateTime.now());			
		} catch (final Exception ex) {
			verify(qosInterRelayEchoMeasurementLogRepository, times(1)).saveAll(any());
			verify(qosInterRelayEchoMeasurementLogRepository, times(0)).flush();
			
			final List<QoSInterRelayEchoMeasurementLog> capturedValue = valueCapture.getValue();
			assertEquals(1, capturedValue.size());
			assertEquals(measurementDetails.getSize(), capturedValue.get(0).getSize());
			assertEquals(measurement.getCloud().getName(), capturedValue.get(0).getMeasurement().getCloud().getName());
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test(expected = ArrowheadException.class)
	public void logInterRelayEchoMeasurementToDBFlushThrowDatabaseExceptionTest() {
		final QoSInterRelayMeasurement measurement = getQoSInterRelayMeasurementForTest(QoSMeasurementType.RELAY_ECHO, QoSMeasurementStatus.FINISHED);
		final RelayEchoMeasurementDetailsDTO measurementDetails = getRelayEchoMeasurementDetailsDTOForTest();
		
		final ArgumentCaptor<List> valueCapture = ArgumentCaptor.forClass(List.class);
		when(qosInterRelayEchoMeasurementLogRepository.saveAll(valueCapture.capture())).thenReturn(List.of());
		doThrow(HibernateException.class).when(qosInterRelayEchoMeasurementLogRepository).flush();
		
		try {
			qosDBService.logInterRelayEchoMeasurementToDB(measurement, List.of(measurementDetails), ZonedDateTime.now());			
		} catch (final Exception ex) {
			verify(qosInterRelayEchoMeasurementLogRepository, times(1)).saveAll(any());
			verify(qosInterRelayEchoMeasurementLogRepository, times(1)).flush();
			
			final List<QoSInterRelayEchoMeasurementLog> capturedValue = valueCapture.getValue();
			assertEquals(1, capturedValue.size());
			assertEquals(measurementDetails.getSize(), capturedValue.get(0).getSize());
			assertEquals(measurement.getCloud().getName(), capturedValue.get(0).getMeasurement().getCloud().getName());
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void logInterRelayEchoMeasurementToDBNullMeasurmentTest() {
		final RelayEchoMeasurementDetailsDTO measurementDetails = getRelayEchoMeasurementDetailsDTOForTest();
		
		try {
			qosDBService.logInterRelayEchoMeasurementToDB(null, List.of(measurementDetails), ZonedDateTime.now());			
		} catch (final Exception ex) {
			verify(qosInterRelayEchoMeasurementLogRepository, times(0)).saveAll(any());
			verify(qosInterRelayEchoMeasurementLogRepository, times(0)).flush();
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void logInterRelayEchoMeasurementToDBNullMeasurmentDetailsTest() {
		final QoSInterRelayMeasurement measurement = getQoSInterRelayMeasurementForTest(QoSMeasurementType.RELAY_ECHO, QoSMeasurementStatus.FINISHED);
		
		try {
			qosDBService.logInterRelayEchoMeasurementToDB(measurement, null, ZonedDateTime.now());			
		} catch (final Exception ex) {
			verify(qosInterRelayEchoMeasurementLogRepository, times(0)).saveAll(any());
			verify(qosInterRelayEchoMeasurementLogRepository, times(0)).flush();
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void logInterRelayEchoMeasurementToDBNullAroundNowTest() {
		final QoSInterRelayMeasurement measurement = getQoSInterRelayMeasurementForTest(QoSMeasurementType.RELAY_ECHO, QoSMeasurementStatus.FINISHED);
		final RelayEchoMeasurementDetailsDTO measurementDetails = getRelayEchoMeasurementDetailsDTOForTest();
		
		try {
			qosDBService.logInterRelayEchoMeasurementToDB(measurement, List.of(measurementDetails), null);			
		} catch (final Exception ex) {
			verify(qosInterRelayEchoMeasurementLogRepository, times(0)).saveAll(any());
			verify(qosInterRelayEchoMeasurementLogRepository, times(0)).flush();
			throw ex;
		}
	}
	
	//=================================================================================================
	// Tests of updateInterRelayEchoMeasurement
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void updateInterRelayEchoMeasurementTest() {
		final QoSInterRelayEchoMeasurement echoMeasurement = getQoSInterRelayEchoMeasurementforTest();
		final RelayEchoMeasurementCalculationsDTO calculations = getCalculationsForTest();
		final ArgumentCaptor<QoSInterRelayEchoMeasurement> valueCapture = ArgumentCaptor.forClass(QoSInterRelayEchoMeasurement.class);
		when(qosInterRelayEchoMeasurementRepository.saveAndFlush(valueCapture.capture())).thenReturn(any());
		
		qosDBService.updateInterRelayEchoMeasurement(echoMeasurement.getMeasurement(), calculations, echoMeasurement, ZonedDateTime.now());
		
		verify(qosInterRelayEchoMeasurementRepository, times(1)).saveAndFlush(eq(echoMeasurement));
		final QoSInterRelayEchoMeasurement capturedValue = valueCapture.getValue();
		assertEquals(echoMeasurement.getMeasurement().getCloud().getName(), capturedValue.getMeasurement().getCloud().getName());
		assertEquals(calculations.getMaxResponseTime(), capturedValue.getMaxResponseTime());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void updateInterRelayEchoMeasurementSaveAndFlushThrowDatabaseExceptionTest() {
		final QoSInterRelayEchoMeasurement echoMeasurement = getQoSInterRelayEchoMeasurementforTest();
		final RelayEchoMeasurementCalculationsDTO calculations = getCalculationsForTest();
		final ArgumentCaptor<QoSInterRelayEchoMeasurement> valueCapture = ArgumentCaptor.forClass(QoSInterRelayEchoMeasurement.class);
		when(qosInterRelayEchoMeasurementRepository.saveAndFlush(valueCapture.capture())).thenThrow(HibernateException.class);
		
		try {
			qosDBService.updateInterRelayEchoMeasurement(echoMeasurement.getMeasurement(), calculations, echoMeasurement, ZonedDateTime.now());			
		} catch (final Exception ex) {
			verify(qosInterRelayEchoMeasurementRepository, times(1)).saveAndFlush(eq(echoMeasurement));
			final QoSInterRelayEchoMeasurement capturedValue = valueCapture.getValue();
			assertEquals(echoMeasurement.getMeasurement().getCloud().getName(), capturedValue.getMeasurement().getCloud().getName());
			assertEquals(calculations.getMaxResponseTime(), capturedValue.getMaxResponseTime());
			throw ex;
		}		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void updateInterRelayEchoMeasurementNullMeasurementTest() {
		final QoSInterRelayEchoMeasurement echoMeasurement = getQoSInterRelayEchoMeasurementforTest();
		final RelayEchoMeasurementCalculationsDTO calculations = getCalculationsForTest();
		
		try {
			qosDBService.updateInterRelayEchoMeasurement(null, calculations, echoMeasurement, ZonedDateTime.now());			
		} catch (final Exception ex) {
			verify(qosInterRelayEchoMeasurementRepository, times(0)).saveAndFlush(any());
			throw ex;
		}		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void updateInterRelayEchoMeasurementNullCalculationsTest() {
		final QoSInterRelayEchoMeasurement echoMeasurement = getQoSInterRelayEchoMeasurementforTest();
		
		try {
			qosDBService.updateInterRelayEchoMeasurement(echoMeasurement.getMeasurement(), null, echoMeasurement, ZonedDateTime.now());			
		} catch (final Exception ex) {
			verify(qosInterRelayEchoMeasurementRepository, times(0)).saveAndFlush(any());
			throw ex;
		}		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void updateInterRelayEchoMeasurementNullEchoMeasurementTest() {
		final QoSInterRelayEchoMeasurement echoMeasurement = getQoSInterRelayEchoMeasurementforTest();
		final RelayEchoMeasurementCalculationsDTO calculations = getCalculationsForTest();
		
		try {
			qosDBService.updateInterRelayEchoMeasurement(echoMeasurement.getMeasurement(), calculations, null, ZonedDateTime.now());			
		} catch (final Exception ex) {
			verify(qosInterRelayEchoMeasurementRepository, times(0)).saveAndFlush(any());
			throw ex;
		}		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void updateInterRelayEchoMeasurementNullAroundNowTest() {
		final QoSInterRelayEchoMeasurement echoMeasurement = getQoSInterRelayEchoMeasurementforTest();
		final RelayEchoMeasurementCalculationsDTO calculations = getCalculationsForTest();
		
		try {
			qosDBService.updateInterRelayEchoMeasurement(echoMeasurement.getMeasurement(), calculations, echoMeasurement, null);			
		} catch (final Exception ex) {
			verify(qosInterRelayEchoMeasurementRepository, times(0)).saveAndFlush(any());
			throw ex;
		}		
	}
	
	//=================================================================================================
	// Tests of updateInterRelayMeasurement
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void updateInterRelayMeasurementTest() {
		final QoSInterRelayMeasurement measurement = getQoSInterRelayMeasurementForTest(QoSMeasurementType.RELAY_ECHO, QoSMeasurementStatus.FINISHED);
		final ArgumentCaptor<QoSInterRelayMeasurement> valueCapture = ArgumentCaptor.forClass(QoSInterRelayMeasurement.class);
		when(qosInterRelayMeasurementRepository.saveAndFlush(valueCapture.capture())).thenReturn(any());
		
		qosDBService.updateInterRelayMeasurement(ZonedDateTime.now(), measurement);
		verify(qosInterRelayMeasurementRepository, times(1)).saveAndFlush(eq(measurement));
		final QoSInterRelayMeasurement capturedValue = valueCapture.getValue();
		assertEquals(measurement.getCloud().getName(), capturedValue.getCloud().getName());
		assertEquals(measurement.getStatus(), capturedValue.getStatus());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void updateInterRelayMeasurementSaveAndFlushThrowDBExceptionTest() {
		final QoSInterRelayMeasurement measurement = getQoSInterRelayMeasurementForTest(QoSMeasurementType.RELAY_ECHO, QoSMeasurementStatus.FINISHED);
		final ArgumentCaptor<QoSInterRelayMeasurement> valueCapture = ArgumentCaptor.forClass(QoSInterRelayMeasurement.class);
		when(qosInterRelayMeasurementRepository.saveAndFlush(valueCapture.capture())).thenThrow(HibernateException.class);
		
		try {
			qosDBService.updateInterRelayMeasurement(ZonedDateTime.now(), measurement);			
		} catch (final Exception ex) {
			verify(qosInterRelayMeasurementRepository, times(1)).saveAndFlush(eq(measurement));
			final QoSInterRelayMeasurement capturedValue = valueCapture.getValue();
			assertEquals(measurement.getCloud().getName(), capturedValue.getCloud().getName());
			assertEquals(measurement.getStatus(), capturedValue.getStatus());
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void updateInterRelayMeasurementNullAroundNowTest() {
		final QoSInterRelayMeasurement measurement = getQoSInterRelayMeasurementForTest(QoSMeasurementType.RELAY_ECHO, QoSMeasurementStatus.FINISHED);
		try {
			qosDBService.updateInterRelayMeasurement(null, measurement);			
		} catch (final Exception ex) {
			verify(qosInterRelayMeasurementRepository, times(0)).saveAndFlush(any());
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void updateInterRelayMeasurementNullMeasurementTest() {
		try {
			qosDBService.updateInterRelayMeasurement(ZonedDateTime.now(), null);			
		} catch (final Exception ex) {
			verify(qosInterRelayMeasurementRepository, times(0)).saveAndFlush(any());
			throw ex;
		}
	}
	
	//=================================================================================================
	// Tests of getInterRelayEchoMeasurementsPage
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getInterRelayEchoMeasurementsPageTest() {
		final int size = 3;
		final List<QoSInterRelayEchoMeasurement> measurementList = getQoSInterRelayEchoMeasurementListForTest(size);
		when(qosInterRelayEchoMeasurementRepository.findAll(any(PageRequest.class))).thenReturn(new PageImpl<>(measurementList));
		final Page<QoSInterRelayEchoMeasurement> result = qosDBService.getInterRelayEchoMeasurementsPage(-1, -1, null, null);
		verify(qosInterRelayEchoMeasurementRepository, times(1)).findAll(any(PageRequest.class));
		assertEquals(size, result.getContent().size());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void getInterRelayEchoMeasurementsPageWithInvalidSortFieldTest() {
		try {
			qosDBService.getInterRelayEchoMeasurementsPage(-1, -1, null, "invalid");			
		} catch (final Exception ex) {
			verify(qosInterRelayEchoMeasurementRepository, times(0)).findAll(any(PageRequest.class));
			throw ex;
		}
	}
	
	
	//=================================================================================================
	// Tests of getInterRelayMeasurementByCloud
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getInterRelayMeasurementByCloudTest() {
		final QoSInterRelayMeasurement measurement = getQoSInterRelayMeasurementForTest(QoSMeasurementType.RELAY_ECHO, QoSMeasurementStatus.FINISHED);
		final CloudResponseDTO cloudResp = DTOConverter.convertCloudToCloudResponseDTO(measurement.getCloud());
		when(qosInterRelayMeasurementRepository.findByCloudAndMeasurementType(any(), eq(QoSMeasurementType.RELAY_ECHO))).thenReturn(List.of(measurement));
		
		qosDBService.getInterRelayMeasurementByCloud(cloudResp);
		verify(qosInterRelayMeasurementRepository, times(1)).findByCloudAndMeasurementType(eq(measurement.getCloud()), eq(QoSMeasurementType.RELAY_ECHO));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void getInterRelayMeasurementByCloudNullCloudTest() {
		try {
			qosDBService.getInterRelayMeasurementByCloud(null);			
		} catch (final Exception ex) {
			verify(qosInterRelayMeasurementRepository, times(0)).findByCloudAndMeasurementType(any(), any());
			throw ex;
		}
	}
	
	//=================================================================================================
	// Tests of getInterRelayEchoMeasurementByCloudAndRealy
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getInterRelayEchoMeasurementByCloudAndRealyTest() {
		final QoSInterRelayEchoMeasurement echoMeasurement = getQoSInterRelayEchoMeasurementforTest();
		final CloudResponseDTO cloudResp = DTOConverter.convertCloudToCloudResponseDTO(echoMeasurement.getMeasurement().getCloud());
		final RelayResponseDTO relayResp = DTOConverter.convertRelayToRelayResponseDTO(echoMeasurement.getMeasurement().getRelay());
		when(qosInterRelayMeasurementRepository.findByCloudAndRelayAndMeasurementType(any(), any(), any())).thenReturn(Optional.of(echoMeasurement.getMeasurement()));
		when(qosInterRelayEchoMeasurementRepository.findByMeasurement(eq(echoMeasurement.getMeasurement()))).thenReturn(Optional.of(echoMeasurement));
		
		final Optional<QoSInterRelayEchoMeasurement> result = qosDBService.getInterRelayEchoMeasurementByCloudAndRealy(cloudResp, relayResp);
		
		verify(qosInterRelayMeasurementRepository, times(1)).findByCloudAndRelayAndMeasurementType(any(), any(), any());
		verify(qosInterRelayEchoMeasurementRepository, times(1)).findByMeasurement(eq(echoMeasurement.getMeasurement()));
		assertTrue(result.isPresent());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getInterRelayEchoMeasurementByCloudAndRealyNoMeasurementInDBTest() {
		final QoSInterRelayEchoMeasurement echoMeasurement = getQoSInterRelayEchoMeasurementforTest();
		final CloudResponseDTO cloudResp = DTOConverter.convertCloudToCloudResponseDTO(echoMeasurement.getMeasurement().getCloud());
		final RelayResponseDTO relayResp = DTOConverter.convertRelayToRelayResponseDTO(echoMeasurement.getMeasurement().getRelay());
		when(qosInterRelayMeasurementRepository.findByCloudAndRelayAndMeasurementType(any(), any(), any())).thenReturn(Optional.empty());
		
		final Optional<QoSInterRelayEchoMeasurement> result = qosDBService.getInterRelayEchoMeasurementByCloudAndRealy(cloudResp, relayResp);
		
		verify(qosInterRelayMeasurementRepository, times(1)).findByCloudAndRelayAndMeasurementType(any(), any(), any());
		verify(qosInterRelayEchoMeasurementRepository, times(0)).findByMeasurement(any());
		assertTrue(result.isEmpty());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void getInterRelayEchoMeasurementByCloudAndRealyNullCloudTest() {
		final QoSInterRelayEchoMeasurement echoMeasurement = getQoSInterRelayEchoMeasurementforTest();
		final RelayResponseDTO relayResp = DTOConverter.convertRelayToRelayResponseDTO(echoMeasurement.getMeasurement().getRelay());
		
		try {
			qosDBService.getInterRelayEchoMeasurementByCloudAndRealy(null, relayResp);			
		} catch (final Exception ex) {
			verify(qosInterRelayMeasurementRepository, times(0)).findByCloudAndRelayAndMeasurementType(any(), any(), any());
			verify(qosInterRelayEchoMeasurementRepository, times(0)).findByMeasurement(any());
			throw ex;
		}		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void getInterRelayEchoMeasurementByCloudAndRealyNullRelayTest() {
		final QoSInterRelayEchoMeasurement echoMeasurement = getQoSInterRelayEchoMeasurementforTest();
		final CloudResponseDTO cloudResp = DTOConverter.convertCloudToCloudResponseDTO(echoMeasurement.getMeasurement().getCloud());
		
		try {
			qosDBService.getInterRelayEchoMeasurementByCloudAndRealy(cloudResp, null);			
		} catch (final Exception ex) {
			verify(qosInterRelayMeasurementRepository, times(0)).findByCloudAndRelayAndMeasurementType(any(), any(), any());
			verify(qosInterRelayEchoMeasurementRepository, times(0)).findByMeasurement(any());
			throw ex;
		}		
	}
	
	//=================================================================================================
	// Tests of getBestInterRelayEchoMeasurementByCloudAndAttribute
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getBestInterRelayEchoMeasurementByCloudAndAttributeTest() {
		final List<QoSInterRelayEchoMeasurement> echoMeasurementList = getQoSInterRelayEchoMeasurementListForTest(2);
		echoMeasurementList.get(0).setId(1L);
		echoMeasurementList.get(0).setMeanResponseTimeWithoutTimeout(35);
		echoMeasurementList.get(1).setId(2L);
		echoMeasurementList.get(1).setMeanResponseTimeWithoutTimeout(30);
		final CloudResponseDTO cloudResp = DTOConverter.convertCloudToCloudResponseDTO(echoMeasurementList.get(0).getMeasurement().getCloud());
		
		when(qosInterRelayMeasurementRepository.findByCloudAndMeasurementType(any(), any())).thenReturn(List.of(echoMeasurementList.get(0).getMeasurement(), echoMeasurementList.get(1).getMeasurement()));
		when(qosInterRelayEchoMeasurementRepository.findByMeasurement(eq(echoMeasurementList.get(0).getMeasurement()))).thenReturn(Optional.of(echoMeasurementList.get(0)));
		when(qosInterRelayEchoMeasurementRepository.findByMeasurement(eq(echoMeasurementList.get(1).getMeasurement()))).thenReturn(Optional.of(echoMeasurementList.get(1)));
		
		final QoSInterRelayEchoMeasurement result = qosDBService.getBestInterRelayEchoMeasurementByCloudAndAttribute(cloudResp, QoSMeasurementAttribute.MEAN_RESPONSE_TIME_WITHOUT_TIMEOUT);
		
		verify(qosInterRelayMeasurementRepository, times(1)).findByCloudAndMeasurementType(any(), any());
		verify(qosInterRelayEchoMeasurementRepository, times(1)).findByMeasurement(eq(echoMeasurementList.get(0).getMeasurement()));
		verify(qosInterRelayEchoMeasurementRepository, times(1)).findByMeasurement(eq(echoMeasurementList.get(1).getMeasurement()));
		assertEquals(echoMeasurementList.get(1).getId(), result.getId());
		assertEquals(echoMeasurementList.get(1).getMeanResponseTimeWithoutTimeout(), result.getMeanResponseTimeWithoutTimeout());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getBestInterRelayEchoMeasurementByCloudAndAttributeNoMeasurementsTest1() {
		final List<QoSInterRelayEchoMeasurement> echoMeasurementList = getQoSInterRelayEchoMeasurementListForTest(2);
		echoMeasurementList.get(0).setId(1L);
		echoMeasurementList.get(0).setMeanResponseTimeWithoutTimeout(35);
		echoMeasurementList.get(1).setId(2L);
		echoMeasurementList.get(1).setMeanResponseTimeWithoutTimeout(30);
		final CloudResponseDTO cloudResp = DTOConverter.convertCloudToCloudResponseDTO(echoMeasurementList.get(0).getMeasurement().getCloud());
		
		when(qosInterRelayMeasurementRepository.findByCloudAndMeasurementType(any(), any())).thenReturn(List.of());
		
		final QoSInterRelayEchoMeasurement result = qosDBService.getBestInterRelayEchoMeasurementByCloudAndAttribute(cloudResp, QoSMeasurementAttribute.MEAN_RESPONSE_TIME_WITHOUT_TIMEOUT);
		
		verify(qosInterRelayMeasurementRepository, times(1)).findByCloudAndMeasurementType(any(), any());
		verify(qosInterRelayEchoMeasurementRepository, times(0)).findByMeasurement(eq(echoMeasurementList.get(0).getMeasurement()));
		verify(qosInterRelayEchoMeasurementRepository, times(0)).findByMeasurement(eq(echoMeasurementList.get(1).getMeasurement()));
		assertNull(result);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getBestInterRelayEchoMeasurementByCloudAndAttributeNoMeasurementsTest2() {
		final List<QoSInterRelayEchoMeasurement> echoMeasurementList = getQoSInterRelayEchoMeasurementListForTest(2);
		echoMeasurementList.get(0).setId(1L);
		echoMeasurementList.get(0).setMeanResponseTimeWithoutTimeout(35);
		echoMeasurementList.get(1).setId(2L);
		echoMeasurementList.get(1).setMeanResponseTimeWithoutTimeout(30);
		final CloudResponseDTO cloudResp = DTOConverter.convertCloudToCloudResponseDTO(echoMeasurementList.get(0).getMeasurement().getCloud());
		
		when(qosInterRelayMeasurementRepository.findByCloudAndMeasurementType(any(), any())).thenReturn(List.of(echoMeasurementList.get(0).getMeasurement(), echoMeasurementList.get(1).getMeasurement()));
		when(qosInterRelayEchoMeasurementRepository.findByMeasurement(eq(echoMeasurementList.get(0).getMeasurement()))).thenReturn(Optional.empty());
		when(qosInterRelayEchoMeasurementRepository.findByMeasurement(eq(echoMeasurementList.get(1).getMeasurement()))).thenReturn(Optional.empty());
		
		final QoSInterRelayEchoMeasurement result = qosDBService.getBestInterRelayEchoMeasurementByCloudAndAttribute(cloudResp, QoSMeasurementAttribute.MEAN_RESPONSE_TIME_WITHOUT_TIMEOUT);
		
		verify(qosInterRelayMeasurementRepository, times(1)).findByCloudAndMeasurementType(any(), any());
		verify(qosInterRelayEchoMeasurementRepository, times(1)).findByMeasurement(eq(echoMeasurementList.get(0).getMeasurement()));
		verify(qosInterRelayEchoMeasurementRepository, times(1)).findByMeasurement(eq(echoMeasurementList.get(1).getMeasurement()));
		assertNull(result);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void getBestInterRelayEchoMeasurementByCloudAndAttributeNullCloudTest() {
		final List<QoSInterRelayEchoMeasurement> echoMeasurementList = getQoSInterRelayEchoMeasurementListForTest(2);
		echoMeasurementList.get(0).setId(1L);
		echoMeasurementList.get(0).setMeanResponseTimeWithoutTimeout(35);
		echoMeasurementList.get(1).setId(2L);
		echoMeasurementList.get(1).setMeanResponseTimeWithoutTimeout(30);
//		final CloudResponseDTO cloudResp = DTOConverter.convertCloudToCloudResponseDTO(echoMeasurementList.get(0).getMeasurement().getCloud());
		
		try {
			qosDBService.getBestInterRelayEchoMeasurementByCloudAndAttribute(null, QoSMeasurementAttribute.MEAN_RESPONSE_TIME_WITHOUT_TIMEOUT);			
		} catch (final Exception ex) {
			verify(qosInterRelayMeasurementRepository, times(0)).findByCloudAndMeasurementType(any(), any());
			verify(qosInterRelayEchoMeasurementRepository, times(0)).findByMeasurement(any());
			throw ex;
		}		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void getBestInterRelayEchoMeasurementByCloudAndAttributeNullAttributeTest() {
		final List<QoSInterRelayEchoMeasurement> echoMeasurementList = getQoSInterRelayEchoMeasurementListForTest(2);
		echoMeasurementList.get(0).setId(1L);
		echoMeasurementList.get(0).setMeanResponseTimeWithoutTimeout(35);
		echoMeasurementList.get(1).setId(2L);
		echoMeasurementList.get(1).setMeanResponseTimeWithoutTimeout(30);
		final CloudResponseDTO cloudResp = DTOConverter.convertCloudToCloudResponseDTO(echoMeasurementList.get(0).getMeasurement().getCloud());
		
		try {
			qosDBService.getBestInterRelayEchoMeasurementByCloudAndAttribute(cloudResp, null);			
		} catch (final Exception ex) {
			verify(qosInterRelayMeasurementRepository, times(0)).findByCloudAndMeasurementType(any(), any());
			verify(qosInterRelayEchoMeasurementRepository, times(0)).findByMeasurement(any());
			throw ex;
		}		
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private List<QoSInterRelayEchoMeasurement> getQoSInterRelayEchoMeasurementListForTest(final int size) {
		final List<QoSInterRelayEchoMeasurement> list = new ArrayList<>(size);
		for (int i = 0; i < size; ++i) {
			list.add(getQoSInterRelayEchoMeasurementforTest());
		}
		return list;
	}
	
	//-------------------------------------------------------------------------------------------------
	private QoSInterRelayEchoMeasurement getQoSInterRelayEchoMeasurementforTest() {
		final QoSInterRelayEchoMeasurement measurement = new QoSInterRelayEchoMeasurement();
		
		measurement.setMeasurement(getQoSInterRelayMeasurementForTest(QoSMeasurementType.RELAY_ECHO, QoSMeasurementStatus.FINISHED));
		measurement.setMaxResponseTime(1);
		measurement.setMinResponseTime(1);
		measurement.setMeanResponseTimeWithoutTimeout(1);
		measurement.setMeanResponseTimeWithTimeout(1);
		measurement.setJitterWithoutTimeout(1);
		measurement.setJitterWithTimeout(1);
		measurement.setLostPerMeasurementPercent(0);
		measurement.setCountStartedAt(ZonedDateTime.now());
		measurement.setLastAccessAt(ZonedDateTime.now());
		measurement.setSent(35);
		measurement.setSentAll(35);
		measurement.setReceived(35);
		measurement.setReceivedAll(35);

		return measurement;
	}

	//-------------------------------------------------------------------------------------------------
	private QoSInterRelayMeasurement getQoSInterRelayMeasurementForTest(final QoSMeasurementType type, final QoSMeasurementStatus status) {
		final Cloud cloud = new Cloud("test-operator", "test-name", true, true, false, "dakjsfug");
		cloud.setCreatedAt(ZonedDateTime.now());
		cloud.setUpdatedAt(ZonedDateTime.now());
		
		final Relay relay = new Relay("10.10.10.10", 10000, true, false, RelayType.GENERAL_RELAY);
		relay.setCreatedAt(ZonedDateTime.now());
		relay.setUpdatedAt(ZonedDateTime.now());
		
		final QoSInterRelayMeasurement measurement = new QoSInterRelayMeasurement(cloud, relay, type, ZonedDateTime.now());
		measurement.setStatus(status);
		return measurement;
	}
	
	//-------------------------------------------------------------------------------------------------
	private RelayEchoMeasurementDetailsDTO getRelayEchoMeasurementDetailsDTOForTest() {
		return new RelayEchoMeasurementDetailsDTO(1, false, null, null, 1, 1, ZonedDateTime.now());
	}
	
	//-------------------------------------------------------------------------------------------------
	private RelayEchoMeasurementCalculationsDTO getCalculationsForTest() {
		return new RelayEchoMeasurementCalculationsDTO(true,//available,
												  	   1,//maxResponseTime,
												  	   1,//minResponseTime,
												  	   1,//meanResponseTimeWithTimeout,
												  	   1,//meanResponseTimeWithoutTimeout,
												  	   1,//jitterWithTimeout,
												  	   1,//jitterWithoutTimeout,
												  	   35,//sentInThisPing,
												  	   35,//receivedInThisPing,
												  	   0,
												  	   ZonedDateTime.now());//lostPerMeasurementPercent)
	}
}
