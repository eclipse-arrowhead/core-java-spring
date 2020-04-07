package eu.arrowhead.core.qos.database.service;

import static org.junit.Assert.assertEquals;
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
import org.springframework.test.context.junit4.SpringRunner;

import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.entity.Cloud;
import eu.arrowhead.common.database.entity.QoSInterRelayEchoMeasurement;
import eu.arrowhead.common.database.entity.QoSInterRelayMeasurement;
import eu.arrowhead.common.database.entity.Relay;
import eu.arrowhead.common.database.repository.QoSInterRelayEchoMeasurementLogRepository;
import eu.arrowhead.common.database.repository.QoSInterRelayEchoMeasurementRepository;
import eu.arrowhead.common.database.repository.QoSInterRelayMeasurementRepository;
import eu.arrowhead.common.dto.internal.CloudResponseDTO;
import eu.arrowhead.common.dto.internal.DTOConverter;
import eu.arrowhead.common.dto.internal.RelayResponseDTO;
import eu.arrowhead.common.dto.internal.RelayType;
import eu.arrowhead.common.dto.shared.QoSMeasurementStatus;
import eu.arrowhead.common.dto.shared.QoSMeasurementType;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.core.qos.dto.PingMeasurementCalculationsDTO;
import eu.arrowhead.core.qos.dto.RelayEchoMeasurementCalculationsDTO;

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
	
	private static final String EMPTY_OR_NULL_ERROR_MESSAGE = " is empty or null";
	private static final String NULL_ERROR_MESSAGE = " is null";
	
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
		Optional<QoSInterRelayMeasurement> result = qosDBService.getInterRelayMeasurement(cloudDTO, relayDTO, QoSMeasurementType.RELAY_ECHO);
		
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
		final RelayResponseDTO relayResp = new RelayResponseDTO(6L, "1.1.1.1", 10000, true, false, RelayType.GATEWAY_RELAY, nowStr, nowStr);
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
		final RelayResponseDTO relayResp = new RelayResponseDTO(6L, "1.1.1.1", 10000, true, false, RelayType.GATEWAY_RELAY, nowStr, nowStr);
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
		final RelayResponseDTO relayResp = new RelayResponseDTO(6L, "1.1.1.1", 10000, true, false, RelayType.GATEWAY_RELAY, nowStr, nowStr);
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
		final RelayResponseDTO relayResp = new RelayResponseDTO(6L, "1.1.1.1", 10000, true, false, RelayType.GATEWAY_RELAY, nowStr, nowStr);
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
		final RelayResponseDTO relayResp = new RelayResponseDTO(6L, "1.1.1.1", 10000, true, false, RelayType.GATEWAY_RELAY, nowStr, nowStr);
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
		final RelayResponseDTO relayResp = new RelayResponseDTO(6L, "1.1.1.1", 10000, true, false, RelayType.GATEWAY_RELAY, nowStr, nowStr);
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
		final RelayResponseDTO relayResp = new RelayResponseDTO(6L, "1.1.1.1", 10000, true, false, RelayType.GATEKEEPER_RELAY, nowStr, nowStr);
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
		final QoSMeasurementStatus originalStatus = QoSMeasurementStatus.FINISHED;
		final QoSMeasurementStatus changedStatus = QoSMeasurementStatus.PENDING;
		final QoSInterRelayMeasurement measurement = getQoSInterRelayMeasurementForTest(QoSMeasurementType.RELAY_ECHO, originalStatus);
		
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
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private List<QoSInterRelayEchoMeasurement> getQoSInterRelayEchoMeasurementListForTest(final int size) {
		List<QoSInterRelayEchoMeasurement> list = new ArrayList<>(size);
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
