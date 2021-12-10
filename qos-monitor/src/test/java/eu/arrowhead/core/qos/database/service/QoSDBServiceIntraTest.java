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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
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
import org.springframework.data.domain.Sort.Direction;
import org.springframework.test.context.junit4.SpringRunner;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.database.entity.QoSIntraMeasurement;
import eu.arrowhead.common.database.entity.QoSIntraPingMeasurement;
import eu.arrowhead.common.database.entity.QoSIntraPingMeasurementLog;
import eu.arrowhead.common.database.entity.QoSIntraPingMeasurementLogDetails;
import eu.arrowhead.common.database.entity.System;
import eu.arrowhead.common.database.repository.QoSIntraMeasurementPingRepository;
import eu.arrowhead.common.database.repository.QoSIntraMeasurementRepository;
import eu.arrowhead.common.database.repository.QoSIntraPingMeasurementLogDetailsRepository;
import eu.arrowhead.common.database.repository.QoSIntraPingMeasurementLogRepository;
import eu.arrowhead.common.database.repository.SystemRepository;
import eu.arrowhead.common.dto.internal.DTOConverter;
import eu.arrowhead.common.dto.shared.AddressType;
import eu.arrowhead.common.dto.shared.QoSMeasurementType;
import eu.arrowhead.common.dto.shared.SystemResponseDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.core.qos.dto.IcmpPingResponse;
import eu.arrowhead.core.qos.dto.PingMeasurementCalculationsDTO;

@RunWith(SpringRunner.class)
public class QoSDBServiceIntraTest {

	//=================================================================================================
	// members
	@InjectMocks
	private QoSDBService qoSDBService;

	@Mock
	private QoSIntraMeasurementRepository qoSIntraMeasurementRepository;

	@Mock
	private QoSIntraMeasurementPingRepository qoSIntraMeasurementPingRepository;

	@Mock
	private QoSIntraPingMeasurementLogRepository qoSIntraPingMeasurementLogRepository;

	@Mock
	private QoSIntraPingMeasurementLogDetailsRepository qoSIntraPingMeasurementLogDetailsRepository;

	@Mock
	private SystemRepository systemRepository;

	private static final String LESS_THAN_ONE_ERROR_MESSAGE= " must be greater than zero.";
	private static final String NOT_AVAILABLE_SORTABLE_FIELD_ERROR_MESSAGE = " sortable field is not available.";
	private static final String NOT_IN_DB_ERROR_MESSAGE = " is not available in database";
	private static final String EMPTY_OR_NULL_ERROR_MESSAGE = " is empty or null";
	private static final String NULL_ERROR_MESSAGE = " is null";

	//=================================================================================================
	// methods

	//=================================================================================================
	// Tests of updateCountStartedAt

	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testUpdateCountStartedAt() {

		final ZonedDateTime testStartedAt = ZonedDateTime.now().minusMinutes(1);
		final List<QoSIntraPingMeasurement> measurementList = getQosIntraPingMeasurementListForTest();
		final ArgumentCaptor<List> valueCapture = ArgumentCaptor.forClass(List.class);

		when(qoSIntraMeasurementPingRepository.findAll()).thenReturn(measurementList);
		when(qoSIntraMeasurementPingRepository.saveAll(valueCapture.capture())).thenReturn(List.of());
		doNothing().when(qoSIntraMeasurementPingRepository).flush();

		qoSDBService.updateIntraCountStartedAt();

		verify(qoSIntraMeasurementPingRepository, times(1)).findAll();
		verify(qoSIntraMeasurementPingRepository, times(1)).saveAll(any());
		verify(qoSIntraMeasurementPingRepository, times(1)).flush();

		final List<QoSIntraPingMeasurement> captured = valueCapture.getValue();
		assertEquals(measurementList.size(), captured.size());
		for (final QoSIntraPingMeasurement qoSIntraPingMeasurement : captured) {

			assertEquals(0, qoSIntraPingMeasurement.getSent());
			assertEquals(0, qoSIntraPingMeasurement.getReceived());
			assertTrue(qoSIntraPingMeasurement.getCountStartedAt().isAfter(testStartedAt));
		}
	}

	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test(expected = ArrowheadException.class)
	public void testUpdateCountStartedAtFlushThrowDatabaseException() {

		final ZonedDateTime testStartedAt = ZonedDateTime.now().minusMinutes(1);;
		final List<QoSIntraPingMeasurement> measurementList = getQosIntraPingMeasurementListForTest();
		final ArgumentCaptor<List> valueCapture = ArgumentCaptor.forClass(List.class);

		when(qoSIntraMeasurementPingRepository.findAll()).thenReturn(measurementList);
		when(qoSIntraMeasurementPingRepository.saveAll(valueCapture.capture())).thenReturn(List.of());
		doThrow(HibernateException.class).when(qoSIntraMeasurementPingRepository).flush();

		try {

			qoSDBService.updateIntraCountStartedAt();

		} catch (final Exception ex) {

			assertEquals(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG, ex.getMessage());

			verify(qoSIntraMeasurementPingRepository, times(1)).findAll();
			verify(qoSIntraMeasurementPingRepository, times(1)).saveAll(any());
			verify(qoSIntraMeasurementPingRepository, times(1)).flush();

			final List<QoSIntraPingMeasurement> captured = valueCapture.getValue();
			assertEquals(measurementList.size(), captured.size());

			for (final QoSIntraPingMeasurement qoSIntraPingMeasurement : captured) {

				assertEquals(0, qoSIntraPingMeasurement.getSent());
				assertEquals(0, qoSIntraPingMeasurement.getReceived());
				assertTrue(qoSIntraPingMeasurement.getCountStartedAt().isAfter(testStartedAt));
			}

			throw ex;
		}

	}

	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test(expected = ArrowheadException.class)
	public void testUpdateCountStartedAtSaveAllThrowDatabaseException() {

		final ZonedDateTime testStartedAt = ZonedDateTime.now().minusMinutes(1);
		final List<QoSIntraPingMeasurement> measurementList = getQosIntraPingMeasurementListForTest();
		final ArgumentCaptor<List> valueCapture = ArgumentCaptor.forClass(List.class);

		when(qoSIntraMeasurementPingRepository.findAll()).thenReturn(measurementList);
		when(qoSIntraMeasurementPingRepository.saveAll(valueCapture.capture())).thenThrow(HibernateException.class);
		doNothing().when(qoSIntraMeasurementPingRepository).flush();

		try {

			qoSDBService.updateIntraCountStartedAt();

		} catch (final Exception ex) {

			assertEquals(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG, ex.getMessage());

			verify(qoSIntraMeasurementPingRepository, times(1)).findAll();
			verify(qoSIntraMeasurementPingRepository, times(1)).saveAll(any());
			verify(qoSIntraMeasurementPingRepository, times(0)).flush();

			final List<QoSIntraPingMeasurement> captured = valueCapture.getValue();
			assertEquals(measurementList.size(), captured.size());

			for (final QoSIntraPingMeasurement qoSIntraPingMeasurement : captured) {

				assertEquals(0, qoSIntraPingMeasurement.getSent());
				assertEquals(0, qoSIntraPingMeasurement.getReceived());
				assertTrue(qoSIntraPingMeasurement.getCountStartedAt().isAfter(testStartedAt));
			}

			throw ex;
		}

	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testUpdateCountStartedAtFindAllThrowDatabaseException() {

		final List<QoSIntraPingMeasurement> measurementList = getQosIntraPingMeasurementListForTest();

		when(qoSIntraMeasurementPingRepository.findAll()).thenThrow(HibernateException.class);
		when(qoSIntraMeasurementPingRepository.saveAll(any())).thenReturn(measurementList);
		doNothing().when(qoSIntraMeasurementPingRepository).flush();

		try {

			qoSDBService.updateIntraCountStartedAt();

		} catch (final Exception ex) {

			assertEquals(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG, ex.getMessage());

			verify(qoSIntraMeasurementPingRepository, times(1)).findAll();
			verify(qoSIntraMeasurementPingRepository, times(0)).saveAll(any());
			verify(qoSIntraMeasurementPingRepository, times(0)).flush();

			throw ex;
		}

	}

	//=================================================================================================
	// Tests of createMeasurement

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCreateMeasurement() {

		final ZonedDateTime aroundNow = ZonedDateTime.now();
		final System system = getSystemForTest();
		final QoSIntraMeasurement measurement = getQoSIntraMeasurementForTest();

		final ArgumentCaptor<QoSIntraMeasurement> valueCapture = ArgumentCaptor.forClass(QoSIntraMeasurement.class);

		when(qoSIntraMeasurementRepository.saveAndFlush(valueCapture.capture())).thenReturn(measurement);

		qoSDBService.createIntraMeasurement(system, QoSMeasurementType.PING, aroundNow);

		verify(qoSIntraMeasurementRepository, times(1)).saveAndFlush(any());

		final QoSIntraMeasurement captured = valueCapture.getValue();
		assertEquals(system.getId(), captured.getSystem().getId());
		assertEquals(QoSMeasurementType.PING, captured.getMeasurementType());

	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testCreateMeasurementSaveAndFlushThrowException() {

		final ZonedDateTime aroundNow = ZonedDateTime.now();
		final System system = getSystemForTest();

		final ArgumentCaptor<QoSIntraMeasurement> valueCapture = ArgumentCaptor.forClass(QoSIntraMeasurement.class);

		when(qoSIntraMeasurementRepository.saveAndFlush(valueCapture.capture())).thenThrow(HibernateException.class);

		qoSDBService.createIntraMeasurement(system, QoSMeasurementType.PING, aroundNow);

		verify(qoSIntraMeasurementRepository, times(1)).saveAndFlush(any());

		final QoSIntraMeasurement captured = valueCapture.getValue();
		assertEquals(system.getId(), captured.getSystem().getId());
		assertEquals(QoSMeasurementType.PING, captured.getMeasurementType());

	}

	//=================================================================================================
	// Tests of getMeasurement

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetMeasurement() {

		final SystemResponseDTO systemResponseDTO = getSystemResponseDTOForTest();
		final System system = getSystemForTest();
		final QoSIntraMeasurement measurement = getQoSIntraMeasurementForTest();

		when(systemRepository.findBySystemNameAndAddressAndPort(anyString(), anyString(), anyInt())).thenReturn(Optional.of(system));
		when(qoSIntraMeasurementRepository.findBySystemAndMeasurementType(any(), any())).thenReturn(Optional.of(measurement));

		qoSDBService.getOrCreateIntraMeasurement(systemResponseDTO, QoSMeasurementType.PING);

		verify(systemRepository, times(1)).findBySystemNameAndAddressAndPort(anyString(), anyString(), anyInt());
		verify(qoSIntraMeasurementRepository, times(1)).findBySystemAndMeasurementType(any(), any());

	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testGetMeasurementRequestedSystemNotInDB() {

		final SystemResponseDTO systemResponseDTO = getSystemResponseDTOForTest();
		final QoSIntraMeasurement measurement = getQoSIntraMeasurementForTest();

		when(systemRepository.findBySystemNameAndAddressAndPort(anyString(), anyString(), anyInt())).thenReturn(Optional.ofNullable(null));
		when(qoSIntraMeasurementRepository.findBySystemAndMeasurementType(any(), any())).thenReturn(Optional.of(measurement));

		try {

			qoSDBService.getOrCreateIntraMeasurement(systemResponseDTO, QoSMeasurementType.PING);

		} catch (final Exception ex) {

			assertEquals("Requested system" + NOT_IN_DB_ERROR_MESSAGE, ex.getMessage());

			verify(systemRepository, times(1)).findBySystemNameAndAddressAndPort(anyString(), anyString(), anyInt());
			verify(qoSIntraMeasurementRepository, times(0)).findBySystemAndMeasurementType(any(), any());

			throw ex;
		}
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetMeasurementMeasurementNotInDB() {

		final SystemResponseDTO systemResponseDTO = getSystemResponseDTOForTest();
		final System system = getSystemForTest();

		when(systemRepository.findBySystemNameAndAddressAndPort(anyString(), anyString(), anyInt())).thenReturn(Optional.of(system));
		when(qoSIntraMeasurementRepository.findBySystemAndMeasurementType(any(), any())).thenReturn(Optional.ofNullable(null));

		final QoSIntraMeasurement measurement = getQoSIntraMeasurementForTest();
		when(qoSIntraMeasurementRepository.saveAndFlush(any())).thenReturn(measurement);

		qoSDBService.getOrCreateIntraMeasurement(systemResponseDTO, QoSMeasurementType.PING);

		verify(systemRepository, times(1)).findBySystemNameAndAddressAndPort(anyString(), anyString(), anyInt());
		verify(qoSIntraMeasurementRepository, times(1)).findBySystemAndMeasurementType(any(), any());
		verify(qoSIntraMeasurementRepository, times(1)).saveAndFlush(any());
	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGetMeasurementNullRequestParameter() {

		final System system = getSystemForTest();
		final SystemResponseDTO systemResponseDTO = null;
		final QoSIntraMeasurement measurement = getQoSIntraMeasurementForTest();

		when(systemRepository.findBySystemNameAndAddressAndPort(anyString(), anyString(), anyInt())).thenReturn(Optional.of(system));
		when(qoSIntraMeasurementRepository.findBySystemAndMeasurementType(any(), any())).thenReturn(Optional.of(measurement));

		try {

			qoSDBService.getOrCreateIntraMeasurement(systemResponseDTO, QoSMeasurementType.PING);

		} catch (final Exception ex) {

			assertTrue(ex.getMessage().contains("SystemRequestDTO" + NULL_ERROR_MESSAGE));

			verify(systemRepository, times(0)).findBySystemNameAndAddressAndPort(anyString(), anyString(), anyInt());
			verify(qoSIntraMeasurementRepository, times(0)).findBySystemAndMeasurementType(any(), any());

			throw ex;
		}
	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGetMeasurementSystemNameIsNullSystemResponseDTOParameter() {

		final System system = getSystemForTest();
		final SystemResponseDTO systemResponseDTO = getSystemResponseDTOForTest();
		systemResponseDTO.setSystemName(null);
		final QoSIntraMeasurement measurement = getQoSIntraMeasurementForTest();

		when(systemRepository.findBySystemNameAndAddressAndPort(anyString(), anyString(), anyInt())).thenReturn(Optional.of(system));
		when(qoSIntraMeasurementRepository.findBySystemAndMeasurementType(any(), any())).thenReturn(Optional.of(measurement));

		try {

			qoSDBService.getOrCreateIntraMeasurement(systemResponseDTO, QoSMeasurementType.PING);

		} catch (final Exception ex) {

			assertTrue(ex.getMessage().contains("System name" + EMPTY_OR_NULL_ERROR_MESSAGE));

			verify(systemRepository, times(0)).findBySystemNameAndAddressAndPort(anyString(), anyString(), anyInt());
			verify(qoSIntraMeasurementRepository, times(0)).findBySystemAndMeasurementType(any(), any());

			throw ex;
		}
	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGetMeasurementSystemNameIsEmptySystemResponseDTOParameter() {

		final System system = getSystemForTest();
		final SystemResponseDTO systemResponseDTO = getSystemResponseDTOForTest();
		systemResponseDTO.setSystemName("   ");
		final QoSIntraMeasurement measurement = getQoSIntraMeasurementForTest();

		when(systemRepository.findBySystemNameAndAddressAndPort(anyString(), anyString(), anyInt())).thenReturn(Optional.of(system));
		when(qoSIntraMeasurementRepository.findBySystemAndMeasurementType(any(), any())).thenReturn(Optional.of(measurement));

		try {

			qoSDBService.getOrCreateIntraMeasurement(systemResponseDTO, QoSMeasurementType.PING);

		} catch (final Exception ex) {

			assertTrue(ex.getMessage().contains("System name" + EMPTY_OR_NULL_ERROR_MESSAGE));

			verify(systemRepository, times(0)).findBySystemNameAndAddressAndPort(anyString(), anyString(), anyInt());
			verify(qoSIntraMeasurementRepository, times(0)).findBySystemAndMeasurementType(any(), any());

			throw ex;
		}
	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGetMeasurementSystemAddressIsNullSystemResponseDTOParameter() {

		final System system = getSystemForTest();
		final SystemResponseDTO systemResponseDTO = getSystemResponseDTOForTest();
		systemResponseDTO.setAddress(null);
		final QoSIntraMeasurement measurement = getQoSIntraMeasurementForTest();

		when(systemRepository.findBySystemNameAndAddressAndPort(anyString(), anyString(), anyInt())).thenReturn(Optional.of(system));
		when(qoSIntraMeasurementRepository.findBySystemAndMeasurementType(any(), any())).thenReturn(Optional.of(measurement));

		try {

			qoSDBService.getOrCreateIntraMeasurement(systemResponseDTO, QoSMeasurementType.PING);

		} catch (final Exception ex) {

			assertTrue(ex.getMessage().contains("System address" + EMPTY_OR_NULL_ERROR_MESSAGE));

			verify(systemRepository, times(0)).findBySystemNameAndAddressAndPort(anyString(), anyString(), anyInt());
			verify(qoSIntraMeasurementRepository, times(0)).findBySystemAndMeasurementType(any(), any());

			throw ex;
		}
	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGetMeasurementSystemAddressIsEmptySystemResponseDTOParameter() {

		final System system = getSystemForTest();
		final SystemResponseDTO systemResponseDTO = getSystemResponseDTOForTest();
		systemResponseDTO.setAddress("   ");
		final QoSIntraMeasurement measurement = getQoSIntraMeasurementForTest();

		when(systemRepository.findBySystemNameAndAddressAndPort(anyString(), anyString(), anyInt())).thenReturn(Optional.of(system));
		when(qoSIntraMeasurementRepository.findBySystemAndMeasurementType(any(), any())).thenReturn(Optional.of(measurement));

		try {

			qoSDBService.getOrCreateIntraMeasurement(systemResponseDTO, QoSMeasurementType.PING);

		} catch (final Exception ex) {

			assertTrue(ex.getMessage().contains("System address" + EMPTY_OR_NULL_ERROR_MESSAGE));

			verify(systemRepository, times(0)).findBySystemNameAndAddressAndPort(anyString(), anyString(), anyInt());
			verify(qoSIntraMeasurementRepository, times(0)).findBySystemAndMeasurementType(any(), any());

			throw ex;
		}
	}

	//=================================================================================================
	// Tests of createMeasurement

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCreatePingMeasurement() {

		final QoSIntraPingMeasurement pingMeasurement = getQosIntraPingMeasurementForTest();

		final QoSIntraMeasurement measurementParam = getQoSIntraMeasurementForTest();
		final PingMeasurementCalculationsDTO calculations = getCalculationsForTest();
		final ZonedDateTime aroundNow = ZonedDateTime.now();

		when(qoSIntraMeasurementPingRepository.saveAndFlush(any())).thenReturn(pingMeasurement);

		qoSDBService.createIntraPingMeasurement(measurementParam, calculations, aroundNow);

		verify(qoSIntraMeasurementPingRepository, times(1)).saveAndFlush(any());

	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCreatePingMeasurementWithNullMeasurementParameter() {

		final QoSIntraPingMeasurement pingMeasurement = getQosIntraPingMeasurementForTest();

		final QoSIntraMeasurement measurementParam = null;
		final PingMeasurementCalculationsDTO calculations = getCalculationsForTest();
		final ZonedDateTime aroundNow = ZonedDateTime.now();

		when(qoSIntraMeasurementPingRepository.saveAndFlush(any())).thenReturn(pingMeasurement);

		try {

			qoSDBService.createIntraPingMeasurement(measurementParam, calculations, aroundNow);

		} catch (final Exception ex) {

			assertTrue(ex.getMessage().contains("QoSIntraMeasurement" + NULL_ERROR_MESSAGE));
			verify(qoSIntraMeasurementPingRepository, times(0)).saveAndFlush(any());
			throw ex;
		}

	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCreatePingMeasurementWithNullCalculationsParameter() {

		final QoSIntraPingMeasurement pingMeasurement = getQosIntraPingMeasurementForTest();

		final QoSIntraMeasurement measurementParam = getQoSIntraMeasurementForTest();
		final PingMeasurementCalculationsDTO calculations = null;//getCalculationsForTest();
		final ZonedDateTime aroundNow = ZonedDateTime.now();

		when(qoSIntraMeasurementPingRepository.saveAndFlush(any())).thenReturn(pingMeasurement);

		try {

			qoSDBService.createIntraPingMeasurement(measurementParam, calculations, aroundNow);

		} catch (final Exception ex) {

			assertTrue(ex.getMessage().contains("PingMeasurementCalculationsDTO" + NULL_ERROR_MESSAGE));
			verify(qoSIntraMeasurementPingRepository, times(0)).saveAndFlush(any());
			throw ex;
		}

	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCreatePingMeasurementWithNullAroundNowParameter() {

		final QoSIntraPingMeasurement pingMeasurement = getQosIntraPingMeasurementForTest();

		final QoSIntraMeasurement measurementParam = getQoSIntraMeasurementForTest();
		final PingMeasurementCalculationsDTO calculations = getCalculationsForTest();
		final ZonedDateTime aroundNow = null;//ZonedDateTime.now();

		when(qoSIntraMeasurementPingRepository.saveAndFlush(any())).thenReturn(pingMeasurement);

		try {

			qoSDBService.createIntraPingMeasurement(measurementParam, calculations, aroundNow);

		} catch (final Exception ex) {

			assertTrue(ex.getMessage().contains("ZonedDateTime" + NULL_ERROR_MESSAGE));
			verify(qoSIntraMeasurementPingRepository, times(0)).saveAndFlush(any());
			throw ex;
		}

	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testCreatePingMeasurementSaveAndFlushThrowsDatabseException() {

		final QoSIntraMeasurement measurementParam = getQoSIntraMeasurementForTest();
		final PingMeasurementCalculationsDTO calculations = getCalculationsForTest();
		final ZonedDateTime aroundNow = ZonedDateTime.now();

		when(qoSIntraMeasurementPingRepository.saveAndFlush(any())).thenThrow(HibernateException.class);

		try {

			qoSDBService.createIntraPingMeasurement(measurementParam, calculations, aroundNow);

		} catch (final Exception ex) {

			assertTrue(ex.getMessage().contains(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG));
			verify(qoSIntraMeasurementPingRepository, times(1)).saveAndFlush(any());
			throw ex;
		}

	}

	//=================================================================================================
	// Tests of getPingMeasurementByMeasurement

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetPingMeasurementByMeasurement() {

		final QoSIntraPingMeasurement pingMeasurement = getQosIntraPingMeasurementForTest();

		final QoSIntraMeasurement measurementParam = getQoSIntraMeasurementForTest();

		when(qoSIntraMeasurementPingRepository.findByMeasurement(any())).thenReturn(Optional.of(pingMeasurement));

		qoSDBService.getIntraPingMeasurementByMeasurement(measurementParam);

		verify(qoSIntraMeasurementPingRepository, times(1)).findByMeasurement(any());

	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGetPingMeasurementByMeasurementWithNullMeasurementParameter() {

		final QoSIntraPingMeasurement pingMeasurement = getQosIntraPingMeasurementForTest();

		final QoSIntraMeasurement measurementParam = null;//getQoSIntraMeasurementForTest();

		when(qoSIntraMeasurementPingRepository.findByMeasurement(any())).thenReturn(Optional.of(pingMeasurement));

		try {

			qoSDBService.getIntraPingMeasurementByMeasurement(measurementParam);

		} catch (final Exception ex) {

			assertTrue(ex.getMessage().contains("QoSIntraMeasurement" + NULL_ERROR_MESSAGE));
			verify(qoSIntraMeasurementPingRepository, times(0)).findByMeasurement(any());
			throw ex;
		}

	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testGetPingMeasurementByMeasurementFindByMeasurementThrowsException() {

		final QoSIntraMeasurement measurementParam = getQoSIntraMeasurementForTest();

		when(qoSIntraMeasurementPingRepository.findByMeasurement(any())).thenThrow(HibernateException.class);

		try {

			qoSDBService.getIntraPingMeasurementByMeasurement(measurementParam);

		} catch (final Exception ex) {

			assertTrue(ex.getMessage().contains(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG));
			verify(qoSIntraMeasurementPingRepository, times(1)).findByMeasurement(any());
			throw ex;
		}

	}

	//=================================================================================================
	// Tests of logMeasurementToDB

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testLogMeasurementToDB() {

		final String address = "address";
		final ZonedDateTime aroundNow = ZonedDateTime.now();
		final PingMeasurementCalculationsDTO calculations = getCalculationsForTest();

		final QoSIntraPingMeasurementLog measurementLog = new QoSIntraPingMeasurementLog();

		when(qoSIntraPingMeasurementLogRepository.saveAndFlush(any())).thenReturn(measurementLog);

		qoSDBService.logIntraMeasurementToDB(address, calculations, aroundNow);

		verify(qoSIntraPingMeasurementLogRepository, times(1)).saveAndFlush(any());

	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testLogMeasurementToDBWithNullAddressParameter() {

		final String address = null;//"address";
		final ZonedDateTime aroundNow = ZonedDateTime.now();
		final PingMeasurementCalculationsDTO calculations = getCalculationsForTest();

		final QoSIntraPingMeasurementLog measurementLog = new QoSIntraPingMeasurementLog();

		when(qoSIntraPingMeasurementLogRepository.saveAndFlush(any())).thenReturn(measurementLog);

		try {

			qoSDBService.logIntraMeasurementToDB(address, calculations, aroundNow);

		} catch (final Exception ex) {

			assertTrue(ex.getMessage().contains("Address" + EMPTY_OR_NULL_ERROR_MESSAGE));
			verify(qoSIntraPingMeasurementLogRepository, times(0)).saveAndFlush(any());

			throw ex;
		}

	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testLogMeasurementToDBWithEmptyAddressParameter() {

		final String address = "   ";//"address";
		final ZonedDateTime aroundNow = ZonedDateTime.now();
		final PingMeasurementCalculationsDTO calculations = getCalculationsForTest();

		final QoSIntraPingMeasurementLog measurementLog = new QoSIntraPingMeasurementLog();

		when(qoSIntraPingMeasurementLogRepository.saveAndFlush(any())).thenReturn(measurementLog);

		try {

			qoSDBService.logIntraMeasurementToDB(address, calculations, aroundNow);

		} catch (final Exception ex) {

			assertTrue(ex.getMessage().contains("Address" + EMPTY_OR_NULL_ERROR_MESSAGE));
			verify(qoSIntraPingMeasurementLogRepository, times(0)).saveAndFlush(any());

			throw ex;
		}

	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testLogMeasurementToDBWithNullAroundNowParameter() {

		final String address = "address";
		final ZonedDateTime aroundNow = null;//ZonedDateTime.now();
		final PingMeasurementCalculationsDTO calculations = getCalculationsForTest();

		final QoSIntraPingMeasurementLog measurementLog = new QoSIntraPingMeasurementLog();

		when(qoSIntraPingMeasurementLogRepository.saveAndFlush(any())).thenReturn(measurementLog);

		try {

			qoSDBService.logIntraMeasurementToDB(address, calculations, aroundNow);

		} catch (final Exception ex) {

			assertTrue(ex.getMessage().contains("ZonedDateTime" + NULL_ERROR_MESSAGE));
			verify(qoSIntraPingMeasurementLogRepository, times(0)).saveAndFlush(any());

			throw ex;
		}

	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testLogMeasurementToDBWithNullCalculationsParameter() {

		final String address = "address";
		final ZonedDateTime aroundNow = ZonedDateTime.now();
		final PingMeasurementCalculationsDTO calculations = null;//getCalculationsForTest();

		final QoSIntraPingMeasurementLog measurementLog = new QoSIntraPingMeasurementLog();

		when(qoSIntraPingMeasurementLogRepository.saveAndFlush(any())).thenReturn(measurementLog);

		try {

			qoSDBService.logIntraMeasurementToDB(address, calculations, aroundNow);

		} catch (final Exception ex) {

			assertTrue(ex.getMessage().contains("PingMeasurementCalculationsDTO" + NULL_ERROR_MESSAGE));
			verify(qoSIntraPingMeasurementLogRepository, times(0)).saveAndFlush(any());

			throw ex;
		}

	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testLogMeasurementToDBSaveAndFlushThrowException() {

		final String address = "address";
		final ZonedDateTime aroundNow = ZonedDateTime.now();
		final PingMeasurementCalculationsDTO calculations = getCalculationsForTest();

		when(qoSIntraPingMeasurementLogRepository.saveAndFlush(any())).thenThrow(HibernateException.class);

		try {

			qoSDBService.logIntraMeasurementToDB(address, calculations, aroundNow);

		} catch (final Exception ex) {

			assertTrue(ex.getMessage().contains(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG));
			verify(qoSIntraPingMeasurementLogRepository, times(1)).saveAndFlush(any());

			throw ex;
		}

	}

	//=================================================================================================
	// Tests of logMeasurementDetailsToDB

	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testLogMeasurementDetailsToDB() {

		final ZonedDateTime aroundNow = ZonedDateTime.now();
		final List<IcmpPingResponse> responseList = getResponseListForTest();
		final QoSIntraPingMeasurementLog measurementLogSaved = new QoSIntraPingMeasurementLog();

		final ArgumentCaptor<List> valueCapture = ArgumentCaptor.forClass(List.class);
		final List<QoSIntraPingMeasurementLogDetails> measurementLogDetailsList = List.of(new QoSIntraPingMeasurementLogDetails());

		when(qoSIntraPingMeasurementLogDetailsRepository.saveAll(valueCapture.capture())).thenReturn(measurementLogDetailsList);
		doNothing().when(qoSIntraPingMeasurementLogDetailsRepository).flush();

		qoSDBService.logIntraMeasurementDetailsToDB(measurementLogSaved, responseList, aroundNow);

		verify(qoSIntraPingMeasurementLogDetailsRepository, times(1)).saveAll(any());
		verify(qoSIntraPingMeasurementLogDetailsRepository, times(1)).flush();

		final List<QoSIntraPingMeasurementLogDetails> captured = valueCapture.getValue();
		assertEquals(responseList.size(), captured.size());
		for (final QoSIntraPingMeasurementLogDetails qoSIntraPingMeasurementLogDetails : captured) {

			assertNotNull(qoSIntraPingMeasurementLogDetails.getMeasurementLog());
			assertNotNull(qoSIntraPingMeasurementLogDetails.isSuccessFlag());
			assertNotNull(qoSIntraPingMeasurementLogDetails.getMeasuredAt());
			
		}

	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testLogMeasurementDetailsToDBWithNullResponseListParameter() {

		final ZonedDateTime aroundNow = ZonedDateTime.now();
		final List<IcmpPingResponse> responseList = null;//getResponseListForTest();
		final QoSIntraPingMeasurementLog measurementLogSaved = new QoSIntraPingMeasurementLog();

		final List<QoSIntraPingMeasurementLogDetails> measurementLogDetailsList = List.of(new QoSIntraPingMeasurementLogDetails());

		when(qoSIntraPingMeasurementLogDetailsRepository.saveAll(any())).thenReturn(measurementLogDetailsList);
		doNothing().when(qoSIntraPingMeasurementLogDetailsRepository).flush();

		try {

			qoSDBService.logIntraMeasurementDetailsToDB(measurementLogSaved, responseList, aroundNow);

		} catch (final Exception ex) {

			assertTrue(ex.getMessage().contains("List<IcmpPingResponse>" + EMPTY_OR_NULL_ERROR_MESSAGE));
			verify(qoSIntraPingMeasurementLogDetailsRepository, times(0)).saveAll(any());
			verify(qoSIntraPingMeasurementLogDetailsRepository, times(0)).flush();

			throw ex;
		}

	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testLogMeasurementDetailsToDBWithEmptyResponseListParameter() {

		final ZonedDateTime aroundNow = ZonedDateTime.now();
		final List<IcmpPingResponse> responseList = List.of();//getResponseListForTest();
		final QoSIntraPingMeasurementLog measurementLogSaved = new QoSIntraPingMeasurementLog();

		final List<QoSIntraPingMeasurementLogDetails> measurementLogDetailsList = List.of(new QoSIntraPingMeasurementLogDetails());

		when(qoSIntraPingMeasurementLogDetailsRepository.saveAll(any())).thenReturn(measurementLogDetailsList);
		doNothing().when(qoSIntraPingMeasurementLogDetailsRepository).flush();

		try {

			qoSDBService.logIntraMeasurementDetailsToDB(measurementLogSaved, responseList, aroundNow);

		} catch (final Exception ex) {

			assertTrue(ex.getMessage().contains("List<IcmpPingResponse>" + EMPTY_OR_NULL_ERROR_MESSAGE));
			verify(qoSIntraPingMeasurementLogDetailsRepository, times(0)).saveAll(any());
			verify(qoSIntraPingMeasurementLogDetailsRepository, times(0)).flush();

			throw ex;
		}

	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testLogMeasurementDetailsToDBWithNullMeasurementLogSavedParameter() {

		final ZonedDateTime aroundNow = ZonedDateTime.now();
		final List<IcmpPingResponse> responseList = getResponseListForTest();
		final QoSIntraPingMeasurementLog measurementLogSaved = null;//new QoSIntraPingMeasurementLog();

		final List<QoSIntraPingMeasurementLogDetails> measurementLogDetailsList = List.of(new QoSIntraPingMeasurementLogDetails());

		when(qoSIntraPingMeasurementLogDetailsRepository.saveAll(any())).thenReturn(measurementLogDetailsList);
		doNothing().when(qoSIntraPingMeasurementLogDetailsRepository).flush();

		try {

			qoSDBService.logIntraMeasurementDetailsToDB(measurementLogSaved, responseList, aroundNow);

		} catch (final Exception ex) {

			assertTrue(ex.getMessage().contains("QoSIntraPingMeasurementLog" + NULL_ERROR_MESSAGE));
			verify(qoSIntraPingMeasurementLogDetailsRepository, times(0)).saveAll(any());
			verify(qoSIntraPingMeasurementLogDetailsRepository, times(0)).flush();

			throw ex;
		}

	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testLogMeasurementDetailsToDBWithNullAroundNowParameter() {

		final ZonedDateTime aroundNow = null;//ZonedDateTime.now();
		final List<IcmpPingResponse> responseList = getResponseListForTest();
		final QoSIntraPingMeasurementLog measurementLogSaved = new QoSIntraPingMeasurementLog();

		final List<QoSIntraPingMeasurementLogDetails> measurementLogDetailsList = List.of(new QoSIntraPingMeasurementLogDetails());

		when(qoSIntraPingMeasurementLogDetailsRepository.saveAll(any())).thenReturn(measurementLogDetailsList);
		doNothing().when(qoSIntraPingMeasurementLogDetailsRepository).flush();

		try {

			qoSDBService.logIntraMeasurementDetailsToDB(measurementLogSaved, responseList, aroundNow);

		} catch (final Exception ex) {

			assertTrue(ex.getMessage().contains("ZonedDateTime" + NULL_ERROR_MESSAGE));
			verify(qoSIntraPingMeasurementLogDetailsRepository, times(0)).saveAll(any());
			verify(qoSIntraPingMeasurementLogDetailsRepository, times(0)).flush();

			throw ex;
		}

	}

	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test(expected = ArrowheadException.class)
	public void testLogMeasurementDetailsToDBSaveAllThrowsException() {

		final ZonedDateTime aroundNow = ZonedDateTime.now();
		final List<IcmpPingResponse> responseList = getResponseListForTest();
		final QoSIntraPingMeasurementLog measurementLogSaved = new QoSIntraPingMeasurementLog();

		final ArgumentCaptor<List> valueCapture = ArgumentCaptor.forClass(List.class);

		when(qoSIntraPingMeasurementLogDetailsRepository.saveAll(valueCapture.capture())).thenThrow(HibernateException.class);
		doNothing().when(qoSIntraPingMeasurementLogDetailsRepository).flush();

		try {

			qoSDBService.logIntraMeasurementDetailsToDB(measurementLogSaved, responseList, aroundNow);

		} catch (final Exception ex) {

			assertTrue(ex.getMessage().contains(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG));
			verify(qoSIntraPingMeasurementLogDetailsRepository, times(1)).saveAll(any());
			verify(qoSIntraPingMeasurementLogDetailsRepository, times(0)).flush();

			final List<QoSIntraPingMeasurementLogDetails> captured = valueCapture.getValue();
			assertEquals(responseList.size(), captured.size());
			for (final QoSIntraPingMeasurementLogDetails qoSIntraPingMeasurementLogDetails : captured) {

				assertNotNull(qoSIntraPingMeasurementLogDetails.getMeasurementLog());
				assertNotNull(qoSIntraPingMeasurementLogDetails.isSuccessFlag());
				assertNotNull(qoSIntraPingMeasurementLogDetails.getMeasuredAt());
				
			}

			throw ex;
		}

	}

	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test(expected = ArrowheadException.class)
	public void testLogMeasurementDetailsToDBFlushThrowsException() {

		final ZonedDateTime aroundNow = ZonedDateTime.now();
		final List<IcmpPingResponse> responseList = getResponseListForTest();
		final QoSIntraPingMeasurementLog measurementLogSaved = new QoSIntraPingMeasurementLog();

		final ArgumentCaptor<List> valueCapture = ArgumentCaptor.forClass(List.class);
		final List<QoSIntraPingMeasurementLogDetails> measurementLogDetailsList = List.of(new QoSIntraPingMeasurementLogDetails());

		when(qoSIntraPingMeasurementLogDetailsRepository.saveAll(valueCapture.capture())).thenReturn(measurementLogDetailsList);
		doThrow(HibernateException.class).when(qoSIntraPingMeasurementLogDetailsRepository).flush();

		try {

			qoSDBService.logIntraMeasurementDetailsToDB(measurementLogSaved, responseList, aroundNow);

		} catch (final Exception ex) {

			assertTrue(ex.getMessage().contains(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG));
			verify(qoSIntraPingMeasurementLogDetailsRepository, times(1)).saveAll(any());
			verify(qoSIntraPingMeasurementLogDetailsRepository, times(1)).flush();

			final List<QoSIntraPingMeasurementLogDetails> captured = valueCapture.getValue();
			assertEquals(responseList.size(), captured.size());
			for (final QoSIntraPingMeasurementLogDetails qoSIntraPingMeasurementLogDetails : captured) {

				assertNotNull(qoSIntraPingMeasurementLogDetails.getMeasurementLog());
				assertNotNull(qoSIntraPingMeasurementLogDetails.isSuccessFlag());
				assertNotNull(qoSIntraPingMeasurementLogDetails.getMeasuredAt());
				
			}

			throw ex;
		}

	}

	//=================================================================================================
	// Tests of updatePingMeasurement

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testUpdatePingMeasurement() {

		final ZonedDateTime aroundNow = ZonedDateTime.now();
		final QoSIntraMeasurement measurement = getQoSIntraMeasurementForTest();
		final PingMeasurementCalculationsDTO calculations = getCalculationsForTest();
		calculations.setAvailable(true);

		final QoSIntraPingMeasurement pingMeasurement = getQosIntraPingMeasurementForTest();
		pingMeasurement.setAvailable(true);

		final ArgumentCaptor<QoSIntraPingMeasurement> valueCapture = ArgumentCaptor.forClass(QoSIntraPingMeasurement.class);

		when(qoSIntraMeasurementPingRepository.saveAndFlush(valueCapture.capture())).thenReturn(pingMeasurement);

		qoSDBService.updateIntraPingMeasurement(measurement, calculations, pingMeasurement, aroundNow);

		verify(qoSIntraMeasurementPingRepository, times(1)).saveAndFlush(any());

		final QoSIntraPingMeasurement captured = valueCapture.getValue();
		assertTrue( calculations.isAvailable() == captured.isAvailable());

	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testUpdatePingMeasurementSaveAndFlushThrowsException() {

		final ZonedDateTime aroundNow = ZonedDateTime.now();
		final QoSIntraMeasurement measurement = getQoSIntraMeasurementForTest();
		final PingMeasurementCalculationsDTO calculations = getCalculationsForTest();
		calculations.setAvailable(true);

		final QoSIntraPingMeasurement pingMeasurement = getQosIntraPingMeasurementForTest();
		pingMeasurement.setAvailable(true);

		final ArgumentCaptor<QoSIntraPingMeasurement> valueCapture = ArgumentCaptor.forClass(QoSIntraPingMeasurement.class);

		when(qoSIntraMeasurementPingRepository.saveAndFlush(valueCapture.capture())).thenThrow(HibernateException.class);

		try {

			qoSDBService.updateIntraPingMeasurement(measurement, calculations, pingMeasurement, aroundNow);

		} catch (final Exception ex) {

			assertTrue(ex.getMessage().contains(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG));
			verify(qoSIntraMeasurementPingRepository, times(1)).saveAndFlush(any());

			final QoSIntraPingMeasurement captured = valueCapture.getValue();
			assertTrue( calculations.isAvailable() == captured.isAvailable());

			throw ex;
		}

	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testUpdatePingMeasurementWithNullMeasurementParameter() {

		final ZonedDateTime aroundNow = ZonedDateTime.now();
		final QoSIntraMeasurement measurement = null;//getQoSIntraMeasurementForTest();
		final PingMeasurementCalculationsDTO calculations = getCalculationsForTest();
		final QoSIntraPingMeasurement pingMeasurement = getQosIntraPingMeasurementForTest();

		when(qoSIntraMeasurementPingRepository.saveAndFlush(any())).thenReturn(pingMeasurement);

		try {

			qoSDBService.updateIntraPingMeasurement(measurement, calculations, pingMeasurement, aroundNow);

		} catch (final Exception ex) {

			assertTrue(ex.getMessage().contains("QoSIntraMeasurement" + NULL_ERROR_MESSAGE));
			verify(qoSIntraMeasurementPingRepository, times(0)).saveAndFlush(any());

			throw ex;
		}
	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testUpdatePingMeasurementWithNullCalculationsParameter() {

		final ZonedDateTime aroundNow = ZonedDateTime.now();
		final QoSIntraMeasurement measurement = getQoSIntraMeasurementForTest();
		final PingMeasurementCalculationsDTO calculations = null;//getCalculationsForTest();
		final QoSIntraPingMeasurement pingMeasurement = getQosIntraPingMeasurementForTest();

		when(qoSIntraMeasurementPingRepository.saveAndFlush(any())).thenReturn(pingMeasurement);

		try {

			qoSDBService.updateIntraPingMeasurement(measurement, calculations, pingMeasurement, aroundNow);

		} catch (final Exception ex) {

			assertTrue(ex.getMessage().contains("PingMeasurementCalculationsDTO" + NULL_ERROR_MESSAGE));
			verify(qoSIntraMeasurementPingRepository, times(0)).saveAndFlush(any());

			throw ex;
		}
	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testUpdatePingMeasurementWithNullPingMeasurementParameter() {

		final ZonedDateTime aroundNow = ZonedDateTime.now();
		final QoSIntraMeasurement measurement = getQoSIntraMeasurementForTest();
		final PingMeasurementCalculationsDTO calculations = getCalculationsForTest();
		final QoSIntraPingMeasurement pingMeasurement = null;//getQosIntraPingMeasurementForTest();

		when(qoSIntraMeasurementPingRepository.saveAndFlush(any())).thenReturn(pingMeasurement);

		try {

			qoSDBService.updateIntraPingMeasurement(measurement, calculations, pingMeasurement, aroundNow);

		} catch (final Exception ex) {

			assertTrue(ex.getMessage().contains("QoSIntraPingMeasurement" + NULL_ERROR_MESSAGE));
			verify(qoSIntraMeasurementPingRepository, times(0)).saveAndFlush(any());

			throw ex;
		}
	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testUpdatePingMeasurementWithNullAroundNowParameter() {

		final ZonedDateTime aroundNow = null;//ZonedDateTime.now();
		final QoSIntraMeasurement measurement = getQoSIntraMeasurementForTest();
		final PingMeasurementCalculationsDTO calculations = getCalculationsForTest();
		final QoSIntraPingMeasurement pingMeasurement = getQosIntraPingMeasurementForTest();

		when(qoSIntraMeasurementPingRepository.saveAndFlush(any())).thenReturn(pingMeasurement);

		try {

			qoSDBService.updateIntraPingMeasurement(measurement, calculations, pingMeasurement, aroundNow);

		} catch (final Exception ex) {

			assertTrue(ex.getMessage().contains("ZonedDateTime" + NULL_ERROR_MESSAGE));
			verify(qoSIntraMeasurementPingRepository, times(0)).saveAndFlush(any());

			throw ex;
		}
	}

	//=================================================================================================
	// Tests of updateMeasurement

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testUpdateMeasurement() {

		final ZonedDateTime aroundNow = ZonedDateTime.now();
		final QoSIntraMeasurement measurement = getQoSIntraMeasurementForTest();

		final ArgumentCaptor<QoSIntraMeasurement> valueCapture = ArgumentCaptor.forClass( QoSIntraMeasurement.class);

		when(qoSIntraMeasurementRepository.saveAndFlush(valueCapture.capture())).thenReturn(measurement);

		qoSDBService.updateIntraMeasurement(aroundNow, measurement);

		verify(qoSIntraMeasurementRepository, times(1)).saveAndFlush(any());

		final QoSIntraMeasurement captured = valueCapture.getValue();
		assertTrue( aroundNow == captured.getLastMeasurementAt());

	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testUpdateMeasurementSaveAndFlushThrowsException() {

		final ZonedDateTime aroundNow = ZonedDateTime.now();
		final QoSIntraMeasurement measurement = getQoSIntraMeasurementForTest();

		final ArgumentCaptor<QoSIntraMeasurement> valueCapture = ArgumentCaptor.forClass( QoSIntraMeasurement.class);

		when(qoSIntraMeasurementRepository.saveAndFlush(valueCapture.capture())).thenThrow(HibernateException.class);

		try {

			qoSDBService.updateIntraMeasurement(aroundNow, measurement);

		} catch (final Exception ex) {

			assertTrue(ex.getMessage().contains(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG));
			verify(qoSIntraMeasurementRepository, times(1)).saveAndFlush(any());

			final QoSIntraMeasurement captured = valueCapture.getValue();
			assertTrue( aroundNow == captured.getLastMeasurementAt());

			throw ex;
		}

	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testUpdateMeasurementWithNullMeasurementParameter() {

		final ZonedDateTime aroundNow = ZonedDateTime.now();
		final QoSIntraMeasurement measurement = null;//getQoSIntraMeasurementForTest();

		when(qoSIntraMeasurementRepository.saveAndFlush(any())).thenReturn(measurement);

		try {

			qoSDBService.updateIntraMeasurement(aroundNow, measurement);

		} catch (final Exception ex) {

			assertTrue(ex.getMessage().contains("QoSIntraMeasurement" + NULL_ERROR_MESSAGE));
			verify(qoSIntraMeasurementRepository, times(0)).saveAndFlush(any());

			throw ex;
		}

	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testUpdateMeasurementWithNullAroundNowParameter() {

		final ZonedDateTime aroundNow = null;//ZonedDateTime.now();
		final QoSIntraMeasurement measurement = getQoSIntraMeasurementForTest();

		when(qoSIntraMeasurementRepository.saveAndFlush(any())).thenReturn(measurement);

		try {

			qoSDBService.updateIntraMeasurement(aroundNow, measurement);

		} catch (final Exception ex) {

			assertTrue(ex.getMessage().contains("ZonedDateTime" + NULL_ERROR_MESSAGE));
			verify(qoSIntraMeasurementRepository, times(0)).saveAndFlush(any());

			throw ex;
		}

	}

	//=================================================================================================
	// Tests of getPingMeasurementResponse

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetPingMeasurementResponse() {

		final int page = 0;
		final int size = 0;
		final Direction direction = Direction.ASC;
		final String sortField = CommonConstants.COMMON_FIELD_NAME_ID;

		final Page<QoSIntraPingMeasurement> pageResponse =  getPageOfPingMeasurementForTest();

		when(qoSIntraMeasurementPingRepository.findAll(any(PageRequest.class))).thenReturn(pageResponse);

		qoSDBService.getIntraPingMeasurementResponse(page, size, direction, sortField);

		verify(qoSIntraMeasurementPingRepository, times(1)).findAll(any(PageRequest.class));

	}

	//=================================================================================================
	// Tests of getPingMeasurementPage

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetPingMeasurementPage() {

		final int page = 0;
		final int size = 0;
		final Direction direction = Direction.ASC;
		final String sortField = CommonConstants.COMMON_FIELD_NAME_ID;

		final Page<QoSIntraPingMeasurement> pageResponse =  getPageOfPingMeasurementForTest();

		when(qoSIntraMeasurementPingRepository.findAll(any(PageRequest.class))).thenReturn(pageResponse);

		qoSDBService.getIntraPingMeasurementPage(page, size, direction, sortField);

		verify(qoSIntraMeasurementPingRepository, times(1)).findAll(any(PageRequest.class));

	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGetPingMeasurementPageWithInvalidSortField() {

		final int page = 0;
		final int size = 0;
		final Direction direction = Direction.ASC;
		final String sortField = "invalid_sort_field";

		final Page<QoSIntraPingMeasurement> pageResponse =  getPageOfPingMeasurementForTest();

		when(qoSIntraMeasurementPingRepository.findAll(any(PageRequest.class))).thenReturn(pageResponse);

		try {

			qoSDBService.getIntraPingMeasurementPage(page, size, direction, sortField);

		} catch (final Exception ex) {

			assertTrue(ex.getMessage().contains(NOT_AVAILABLE_SORTABLE_FIELD_ERROR_MESSAGE));
			verify(qoSIntraMeasurementPingRepository, times(0)).findAll(any(PageRequest.class));

			throw ex;
		}

	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testGetPingMeasurementPagefindAllThrowsException() {

		final int page = 0;
		final int size = 0;
		final Direction direction = Direction.ASC;
		final String sortField = CommonConstants.COMMON_FIELD_NAME_ID;

		when(qoSIntraMeasurementPingRepository.findAll(any(PageRequest.class))).thenThrow(HibernateException.class);

		try {

			qoSDBService.getIntraPingMeasurementPage(page, size, direction, sortField);

		} catch (final Exception ex) {

			assertTrue(ex.getMessage().contains(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG));
			verify(qoSIntraMeasurementPingRepository, times(1)).findAll(any(PageRequest.class));

			throw ex;
		}

	}

	//=================================================================================================
	// Tests of getPingMeasurementBySystemId

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetPingMeasurementBySystemId() {

		final long id = 1L;
		final System system = getSystemForTest();
		final QoSIntraMeasurement measurement = getQoSIntraMeasurementForTest();

		final QoSIntraPingMeasurement pingMeasurement = getQosIntraPingMeasurementForTest();

		when(systemRepository.findById(anyLong())).thenReturn(Optional.of(system));
		when(qoSIntraMeasurementRepository.findBySystemAndMeasurementType(any(), any())).thenReturn(Optional.of(measurement));
		when(qoSIntraMeasurementPingRepository.findByMeasurement(any())).thenReturn(Optional.of(pingMeasurement));

		qoSDBService.getIntraPingMeasurementBySystemId(id);

		verify(systemRepository, times(1)).findById(anyLong());
		verify(qoSIntraMeasurementRepository, times(1)).findBySystemAndMeasurementType(any(), any());
		verify(qoSIntraMeasurementPingRepository, times(1)).findByMeasurement(any());

	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetPingMeasurementBySystemIdNoPingMeasurementInDB() {

		final long id = 1L;
		final System system = getSystemForTest();
		final QoSIntraMeasurement measurement = getQoSIntraMeasurementForTest();


		when(systemRepository.findById(anyLong())).thenReturn(Optional.of(system));
		when(qoSIntraMeasurementRepository.findBySystemAndMeasurementType(any(), any())).thenReturn(Optional.of(measurement));
		when(qoSIntraMeasurementPingRepository.findByMeasurement(any())).thenReturn(Optional.ofNullable(null));

		final QoSIntraPingMeasurement response = qoSDBService.getIntraPingMeasurementBySystemId(id);

		assertNull(response);

		verify(systemRepository, times(1)).findById(anyLong());
		verify(qoSIntraMeasurementRepository, times(1)).findBySystemAndMeasurementType(any(), any());
		verify(qoSIntraMeasurementPingRepository, times(1)).findByMeasurement(any());

	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetPingMeasurementBySystemIdNoMeasurementInDB() {

		final long id = 1L;
		final System system = getSystemForTest();
		final QoSIntraPingMeasurement pingMeasurement = getQosIntraPingMeasurementForTest();

		when(systemRepository.findById(anyLong())).thenReturn(Optional.of(system));
		when(qoSIntraMeasurementRepository.findBySystemAndMeasurementType(any(), any())).thenReturn(Optional.ofNullable(null));
		when(qoSIntraMeasurementPingRepository.findByMeasurement(any())).thenReturn(Optional.of(pingMeasurement));

		final QoSIntraPingMeasurement response = qoSDBService.getIntraPingMeasurementBySystemId(id);

		assertNull(response);

		verify(systemRepository, times(1)).findById(anyLong());
		verify(qoSIntraMeasurementRepository, times(1)).findBySystemAndMeasurementType(any(), any());
		verify(qoSIntraMeasurementPingRepository, times(0)).findByMeasurement(any());

	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGetPingMeasurementBySystemIdNoSystemInDB() {

		final long id = 1L;
		final QoSIntraMeasurement measurement = getQoSIntraMeasurementForTest();
		final QoSIntraPingMeasurement pingMeasurement = getQosIntraPingMeasurementForTest();

		when(systemRepository.findById(anyLong())).thenReturn(Optional.ofNullable(null));
		when(qoSIntraMeasurementRepository.findBySystemAndMeasurementType(any(), any())).thenReturn(Optional.of(measurement));
		when(qoSIntraMeasurementPingRepository.findByMeasurement(any())).thenReturn(Optional.of(pingMeasurement));

		try {

			qoSDBService.getIntraPingMeasurementBySystemId(id);

		} catch (final Exception ex) {

			assertTrue(ex.getMessage().contains("Requested system" + NOT_IN_DB_ERROR_MESSAGE));
			verify(systemRepository, times(1)).findById(anyLong());
			verify(qoSIntraMeasurementRepository, times(0)).findBySystemAndMeasurementType(any(), any());
			verify(qoSIntraMeasurementPingRepository, times(0)).findByMeasurement(any());

			throw ex;
		}

	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGetPingMeasurementBySystemIdWithInvalidIdParameter() {

		final long id = -1L;
		final System system = getSystemForTest();
		final QoSIntraMeasurement measurement = getQoSIntraMeasurementForTest();
		final QoSIntraPingMeasurement pingMeasurement = getQosIntraPingMeasurementForTest();

		when(systemRepository.findById(anyLong())).thenReturn(Optional.of(system));
		when(qoSIntraMeasurementRepository.findBySystemAndMeasurementType(any(), any())).thenReturn(Optional.of(measurement));
		when(qoSIntraMeasurementPingRepository.findByMeasurement(any())).thenReturn(Optional.of(pingMeasurement));

		try {

			qoSDBService.getIntraPingMeasurementBySystemId(id);

		} catch (final Exception ex) {

			assertTrue(ex.getMessage().contains("SystemId" + LESS_THAN_ONE_ERROR_MESSAGE));
			verify(systemRepository, times(0)).findById(anyLong());
			verify(qoSIntraMeasurementRepository, times(0)).findBySystemAndMeasurementType(any(), any());
			verify(qoSIntraMeasurementPingRepository, times(0)).findByMeasurement(any());

			throw ex;
		}

	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testGetPingMeasurementBySystemIdFindByIdThrowsException() {

		final long id = 1L;
		final QoSIntraMeasurement measurement = getQoSIntraMeasurementForTest();
		final QoSIntraPingMeasurement pingMeasurement = getQosIntraPingMeasurementForTest();

		when(systemRepository.findById(anyLong())).thenThrow(HibernateException.class);
		when(qoSIntraMeasurementRepository.findBySystemAndMeasurementType(any(), any())).thenReturn(Optional.of(measurement));
		when(qoSIntraMeasurementPingRepository.findByMeasurement(any())).thenReturn(Optional.of(pingMeasurement));

		try {

			qoSDBService.getIntraPingMeasurementBySystemId(id);

		} catch (final Exception ex) {

			assertTrue(ex.getMessage().contains(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG));
			verify(systemRepository, times(1)).findById(anyLong());
			verify(qoSIntraMeasurementRepository, times(0)).findBySystemAndMeasurementType(any(), any());
			verify(qoSIntraMeasurementPingRepository, times(0)).findByMeasurement(any());

			throw ex;
		}

	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testGetPingMeasurementBySystemIdFindBySystemAndMeasurementTypeThrowsException() {

		final long id = 1L;
		final System system = getSystemForTest();
		final QoSIntraPingMeasurement pingMeasurement = getQosIntraPingMeasurementForTest();

		when(systemRepository.findById(anyLong())).thenReturn(Optional.of(system));
		when(qoSIntraMeasurementRepository.findBySystemAndMeasurementType(any(), any())).thenThrow(HibernateException.class);
		when(qoSIntraMeasurementPingRepository.findByMeasurement(any())).thenReturn(Optional.of(pingMeasurement));

		try {

			qoSDBService.getIntraPingMeasurementBySystemId(id);

		} catch (final Exception ex) {

			assertTrue(ex.getMessage().contains(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG));
			verify(systemRepository, times(1)).findById(anyLong());
			verify(qoSIntraMeasurementRepository, times(1)).findBySystemAndMeasurementType(any(), any());
			verify(qoSIntraMeasurementPingRepository, times(0)).findByMeasurement(any());

			throw ex;
		}

	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testGetPingMeasurementBySystemIdFindByMeasurementThrowsException() {

		final long id = 1L;
		final System system = getSystemForTest();
		final QoSIntraMeasurement measurement = getQoSIntraMeasurementForTest();

		when(systemRepository.findById(anyLong())).thenReturn(Optional.of(system));
		when(qoSIntraMeasurementRepository.findBySystemAndMeasurementType(any(), any())).thenReturn(Optional.of(measurement));
		when(qoSIntraMeasurementPingRepository.findByMeasurement(any())).thenThrow(HibernateException.class);

		try {

			qoSDBService.getIntraPingMeasurementBySystemId(id);

		} catch (final Exception ex) {

			assertTrue(ex.getMessage().contains(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG));
			verify(systemRepository, times(1)).findById(anyLong());
			verify(qoSIntraMeasurementRepository, times(1)).findBySystemAndMeasurementType(any(), any());
			verify(qoSIntraMeasurementPingRepository, times(1)).findByMeasurement(any());

			throw ex;
		}

	}

	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	private List<QoSIntraPingMeasurement> getQosIntraPingMeasurementListForTest() {

		final int sizeOfMeasurementList = 3;
		final List<QoSIntraPingMeasurement> qoSIntraPingMeasurementList = new ArrayList<>(sizeOfMeasurementList);

		for (int i = 0; i < sizeOfMeasurementList; i++) {

			qoSIntraPingMeasurementList.add(getQosIntraPingMeasurementForTest());
		}

		return qoSIntraPingMeasurementList;
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
	private QoSIntraMeasurement getQoSIntraMeasurementForTest() {

		final System system = getSystemForTest();
		final QoSIntraMeasurement measurement = new QoSIntraMeasurement(
				system, 
				QoSMeasurementType.PING, 
				ZonedDateTime.now());

		return measurement;
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
	private SystemResponseDTO getSystemResponseDTOForTest() {

		return DTOConverter.convertSystemToSystemResponseDTO(getSystemForTest());
	}

	//-------------------------------------------------------------------------------------------------
	private List<IcmpPingResponse> getResponseListForTest() {

		final int sizeOfList = 3;
		final List<IcmpPingResponse> responseList = new ArrayList<>(3);

		for (int i = 0; i < sizeOfList; i++) {
			responseList.add(getIcmpResponseForTest());
		}

		return responseList;
	}

	//-------------------------------------------------------------------------------------------------
	private IcmpPingResponse getIcmpResponseForTest() {

		final IcmpPingResponse response = new IcmpPingResponse();
		response.setDuration(1);
		response.setRtt(1);
		response.setSuccessFlag(true);
		response.setTimeoutFlag(false);
		response.setSize(32);
		response.setTtl(64);

		return response;
	}

	//-------------------------------------------------------------------------------------------------
	private PingMeasurementCalculationsDTO getCalculationsForTest() {

		final PingMeasurementCalculationsDTO calculations = new PingMeasurementCalculationsDTO(
				true,//available,
				1,//maxResponseTime,
				1,//minResponseTime,
				1,//meanResponseTimeWithTimeout,
				1,//meanResponseTimeWithoutTimeout,
				1,//jitterWithTimeout,
				1,//jitterWithoutTimeout,
				35,//sentInThisPing,
				35,//receivedInThisPing,
				0,
				ZonedDateTime.now());//lostPerMeasurementPercent);

		return calculations;
	}

	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("unused")
	private PageImpl<QoSIntraMeasurement> getPageOfMeasurementForTest() {

		final List<QoSIntraMeasurement> meausrementList = List.of(getQoSIntraMeasurementForTest());

		return new PageImpl<QoSIntraMeasurement>(meausrementList);
	}

	//-------------------------------------------------------------------------------------------------
	private PageImpl<QoSIntraPingMeasurement> getPageOfPingMeasurementForTest() {

		final List<QoSIntraPingMeasurement> pingMeausrementList = getQosIntraPingMeasurementListForTest();

		return new PageImpl<QoSIntraPingMeasurement>(pingMeausrementList);
	}
}
