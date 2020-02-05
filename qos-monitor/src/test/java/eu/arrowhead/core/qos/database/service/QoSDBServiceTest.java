package eu.arrowhead.core.qos.database.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
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
import org.springframework.test.context.junit4.SpringRunner;

import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.database.entity.QoSIntraMeasurement;
import eu.arrowhead.common.database.entity.QoSIntraPingMeasurement;
import eu.arrowhead.common.database.entity.System;
import eu.arrowhead.common.database.repository.QoSIntraMeasurementPingRepository;
import eu.arrowhead.common.database.repository.QoSIntraMeasurementRepository;
import eu.arrowhead.common.database.repository.QoSIntraPingMeasurementLogDetailsRepository;
import eu.arrowhead.common.database.repository.QoSIntraPingMeasurementLogRepository;
import eu.arrowhead.common.database.repository.SystemRepository;
import eu.arrowhead.common.dto.internal.DTOConverter;
import eu.arrowhead.common.dto.shared.QoSMeasurementType;
import eu.arrowhead.common.dto.shared.SystemResponseDTO;
import eu.arrowhead.common.exception.ArrowheadException;

@RunWith(SpringRunner.class)
public class QoSDBServiceTest {

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
	private static final String NOT_AVAILABLE_SORTABLE_FIELD_ERROR_MESSAGE = " sortable field  is not available.";
	private static final String NOT_IN_DB_ERROR_MESSAGE = " is not available in database";

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

		qoSDBService.updateCountStartedAt();

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

			qoSDBService.updateCountStartedAt();

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

			qoSDBService.updateCountStartedAt();

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
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test(expected = ArrowheadException.class)
	public void testUpdateCountStartedAtFindAllThrowDatabaseException() {

		final List<QoSIntraPingMeasurement> measurementList = getQosIntraPingMeasurementListForTest();

		when(qoSIntraMeasurementPingRepository.findAll()).thenThrow(HibernateException.class);
		when(qoSIntraMeasurementPingRepository.saveAll(any())).thenReturn(measurementList);
		doNothing().when(qoSIntraMeasurementPingRepository).flush();

		try {

			qoSDBService.updateCountStartedAt();

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

		qoSDBService.createMeasurement(system, QoSMeasurementType.PING, aroundNow);

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

		qoSDBService.createMeasurement(system, QoSMeasurementType.PING, aroundNow);

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

		qoSDBService.getMeasurement(systemResponseDTO);

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

			qoSDBService.getMeasurement(systemResponseDTO);

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

		qoSDBService.getMeasurement(systemResponseDTO);

		verify(systemRepository, times(1)).findBySystemNameAndAddressAndPort(anyString(), anyString(), anyInt());
		verify(qoSIntraMeasurementRepository, times(1)).findBySystemAndMeasurementType(any(), any());
		verify(qoSIntraMeasurementRepository, times(1)).saveAndFlush(any());
	}

	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	private List<QoSIntraPingMeasurement> getQosIntraPingMeasurementListForTest() {

		final int sizeOfMeasurementList = 3;
		final List<QoSIntraPingMeasurement> qoSIntraPingMeasurementList = new ArrayList<>(sizeOfMeasurementList);

		final QoSIntraMeasurement measurement = getQoSIntraMeasurementForTest();

		for (int i = 0; i < sizeOfMeasurementList; i++) {

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

			qoSIntraPingMeasurementList.add(pingMeasurement);
		}

		return qoSIntraPingMeasurementList;
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
				12345,
				"authenticationInfo");

		return system;
	}

	//-------------------------------------------------------------------------------------------------
	private SystemResponseDTO getSystemResponseDTOForTest() {

		return DTOConverter.convertSystemToSystemResponseDTO(getSystemForTest());
	}
}
