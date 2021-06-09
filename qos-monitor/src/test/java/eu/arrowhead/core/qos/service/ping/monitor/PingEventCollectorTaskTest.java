package eu.arrowhead.core.qos.service.ping.monitor;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.entity.Subscription;
import eu.arrowhead.common.dto.internal.EventPublishStartDTO;
import eu.arrowhead.common.dto.internal.QoSIntraMeasurementResponseDTO;
import eu.arrowhead.common.dto.internal.QoSIntraPingMeasurementListResponseDTO;
import eu.arrowhead.common.dto.internal.QoSIntraPingMeasurementResponseDTO;
import eu.arrowhead.common.dto.shared.EventDTO;
import eu.arrowhead.common.dto.shared.EventPublishRequestDTO;
import eu.arrowhead.common.dto.shared.QoSMeasurementType;
import eu.arrowhead.common.dto.shared.QosMonitorEventType;
import eu.arrowhead.common.dto.shared.SystemResponseDTO;
import eu.arrowhead.core.qos.QosMonitorConstants;

@RunWith(SpringRunner.class)
public class PingEventCollectorTaskTest {

	//=================================================================================================
	// members
	@InjectMocks
	private final PingEventCollectorTask pingEventCollectorTask = new PingEventCollectorTask();

	@Mock
	private LinkedBlockingQueue<EventDTO> eventQueue;

	@Mock
	private ConcurrentHashMap<UUID, PingEventBufferElement> eventBuffer;

	@Mock
	private PingEventBufferCleaner bufferCleaner;

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRunExpressExecutorOk() {

		ReflectionTestUtils.setField(pingEventCollectorTask, "interrupted", false);

		final EventDTO event = getValidReceivedMeasurementRequestEventDTOForTest();

		try {
			when(eventQueue.take()).thenReturn(event).thenThrow(InterruptedException.class);
		} catch (final InterruptedException ex) {
			fail();
		}

		doNothing().when(bufferCleaner).clearBuffer();

		pingEventCollectorTask.run();

		try {
			verify(eventQueue, times(2)).take();
		} catch (final InterruptedException ex) {
			fail();
		}

		verify(bufferCleaner, atLeastOnce()).clearBuffer();

		final Boolean runInterupted = (Boolean) ReflectionTestUtils.getField(pingEventCollectorTask, "interrupted");
		assertTrue(runInterupted);
	}


	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	private EventDTO getValidEventDTOForTest() {

		return getValidReceivedMeasurementRequestEventDTOForTest();
	}

	//-------------------------------------------------------------------------------------------------
	private EventDTO getInvalidTimeStampEventDTOForTest() {

		final EventDTO event = new EventDTO();
		event.setEventType(QosMonitorEventType.RECEIVED_MONITORING_REQUEST.name());
		event.setMetaData(getValidMeasuermentEventDTOMetadtaProcessIdForTest());
		event.setPayload(getValidMeasuermentEventDTOEmptyPayloadForTest());
		event.setTimeStamp("12:22:34-12:12:12");

		return event;
	}

	//-------------------------------------------------------------------------------------------------
	private EventDTO getEmptyTimeStampEventDTOForTest() {

		final EventDTO event = new EventDTO();
		event.setEventType(QosMonitorEventType.RECEIVED_MONITORING_REQUEST.name());
		event.setMetaData(getValidMeasuermentEventDTOMetadtaProcessIdForTest());
		event.setPayload(getValidMeasuermentEventDTOEmptyPayloadForTest());
		event.setTimeStamp("");

		return event;
	}

	//-------------------------------------------------------------------------------------------------
	private EventDTO getNullTimeStampEventDTOForTest() {

		final EventDTO event = new EventDTO();
		event.setEventType(QosMonitorEventType.RECEIVED_MONITORING_REQUEST.name());
		event.setMetaData(getValidMeasuermentEventDTOMetadtaProcessIdForTest());
		event.setPayload(getValidMeasuermentEventDTOEmptyPayloadForTest());
		event.setTimeStamp(null);

		return event;
	}

	//-------------------------------------------------------------------------------------------------
	private EventDTO getNullPayloadEventDTOForTest() {

		final EventDTO event = new EventDTO();
		event.setEventType(QosMonitorEventType.RECEIVED_MONITORING_REQUEST.name());
		event.setMetaData(getValidMeasuermentEventDTOMetadtaProcessIdForTest());
		event.setPayload(null);
		event.setTimeStamp(Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()));

		return event;
	}

	//-------------------------------------------------------------------------------------------------
	private EventDTO getEmptyPayloadEventDTOForTest() {

		final EventDTO event = new EventDTO();
		event.setEventType(QosMonitorEventType.RECEIVED_MONITORING_REQUEST.name());
		event.setMetaData(getValidMeasuermentEventDTOMetadtaProcessIdForTest());
		event.setPayload("");
		event.setTimeStamp(Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()));

		return event;
	}

	//-------------------------------------------------------------------------------------------------
	private EventDTO getNullEventTypeEventDTOForTest() {

		final EventDTO event = new EventDTO();
		event.setEventType(null);
		event.setMetaData(getValidMeasuermentEventDTOMetadtaProcessIdForTest());
		event.setPayload(getValidMeasuermentEventDTOEmptyPayloadForTest());
		event.setTimeStamp(Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()));

		return event;
	}

	//-------------------------------------------------------------------------------------------------
	private EventDTO getEmptyEventTypeEventDTOForTest() {

		final EventDTO event = new EventDTO();
		event.setEventType("");
		event.setMetaData(getValidMeasuermentEventDTOMetadtaProcessIdForTest());
		event.setPayload(getValidMeasuermentEventDTOEmptyPayloadForTest());
		event.setTimeStamp(Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()));

		return event;
	}

	//-------------------------------------------------------------------------------------------------
	private EventDTO getInValidEventTypeEventDTOForTest() {

		final EventDTO event = new EventDTO();
		event.setEventType("UNKNOWN_MEAUSREMENT_EVENT");
		event.setMetaData(getValidMeasuermentEventDTOMetadtaProcessIdForTest());
		event.setPayload(getValidMeasuermentEventDTOEmptyPayloadForTest());
		event.setTimeStamp(Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()));

		return event;
	}

	//-------------------------------------------------------------------------------------------------
	private EventDTO getValidReceivedMeasurementRequestEventDTOForTest() {

		final EventDTO event = new EventDTO();
		event.setEventType(QosMonitorEventType.RECEIVED_MONITORING_REQUEST.name());
		event.setMetaData(getValidMeasuermentEventDTOMetadtaProcessIdForTest());
		event.setPayload(getValidMeasuermentEventDTOEmptyPayloadForTest());
		event.setTimeStamp(Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()));

		return event;
	}

	//-------------------------------------------------------------------------------------------------
	private Map<String, String> getValidMeasuermentEventDTOMetadtaProcessIdForTest() {

		return Map.of(QosMonitorConstants.PROCESS_ID_KEY, UUID.randomUUID().toString());

	}

	//-------------------------------------------------------------------------------------------------
	private String getValidMeasuermentEventDTOEmptyPayloadForTest() {

		return "[]";

	}
	//-------------------------------------------------------------------------------------------------
	private QoSIntraPingMeasurementListResponseDTO getIntraPingMeasurementListResponseDTOForTest() {

		final int responseSize = 3;
		final List<QoSIntraPingMeasurementResponseDTO> pingMeasurementList = new ArrayList<>(3);

		for (int i = 0; i < responseSize; i++) {
			pingMeasurementList.add(getIntraPingMeasurementResponseDTOForTest());
		}

		return new QoSIntraPingMeasurementListResponseDTO(pingMeasurementList, responseSize);
	}

	//-------------------------------------------------------------------------------------------------
	private QoSIntraPingMeasurementListResponseDTO getIntraPingMeasurementListResponseDTOForTest(final int responseSize) {

		final List<QoSIntraPingMeasurementResponseDTO> pingMeasurementList = new ArrayList<>(responseSize);

		for (int i = 0; i < responseSize; i++) {
			pingMeasurementList.add(getIntraPingMeasurementResponseDTOForTest());
		}

		return new QoSIntraPingMeasurementListResponseDTO(pingMeasurementList, responseSize);
	}

	//-------------------------------------------------------------------------------------------------
	private QoSIntraPingMeasurementResponseDTO getIntraPingMeasurementResponseDTOForTest() {

		final QoSIntraMeasurementResponseDTO qoSIntraMeasurementResponseDTO = getQoSIntraMeasurementResponseDTOForTest();

		final QoSIntraPingMeasurementResponseDTO pingMeasurementResponseDTO  = new QoSIntraPingMeasurementResponseDTO();
		pingMeasurementResponseDTO.setId(1L);
		pingMeasurementResponseDTO.setMeasurement(qoSIntraMeasurementResponseDTO);
		pingMeasurementResponseDTO.setAvailable(true);
		pingMeasurementResponseDTO.setLastAccessAt(Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()));
		pingMeasurementResponseDTO.setMinResponseTime(1);
		pingMeasurementResponseDTO.setMaxResponseTime(1);
		pingMeasurementResponseDTO.setMeanResponseTimeWithTimeout(1);
		pingMeasurementResponseDTO.setMeanResponseTimeWithoutTimeout(1);
		pingMeasurementResponseDTO.setJitterWithTimeout(0);
		pingMeasurementResponseDTO.setJitterWithoutTimeout(0);
		pingMeasurementResponseDTO.setLostPerMeasurementPercent(0);
		pingMeasurementResponseDTO.setSent(35);
		pingMeasurementResponseDTO.setReceived(35);
		pingMeasurementResponseDTO.setCountStartedAt(Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()));
		pingMeasurementResponseDTO.setSentAll(35);
		pingMeasurementResponseDTO.setReceivedAll(35);
		pingMeasurementResponseDTO.setCreatedAt(Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()));
		pingMeasurementResponseDTO.setUpdatedAt(Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()));

		return pingMeasurementResponseDTO;
	}

	//-------------------------------------------------------------------------------------------------
	private QoSIntraMeasurementResponseDTO getQoSIntraMeasurementResponseDTOForTest() {

		final SystemResponseDTO system = getSystemResponseDTOForTest();

		return new QoSIntraMeasurementResponseDTO(
				1,//measurement.getId(), 
				system, 
				QoSMeasurementType.PING,//measurement.getMeasurementType(), 
				Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()),//measurement.getLastMeasurementAt(), 
				Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()),//measurement.getCreatedAt(), 
				Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()));//measurement.getUpdatedAt());
	}

	//-------------------------------------------------------------------------------------------------
	private SystemResponseDTO getSystemResponseDTOForTest() {

		return new SystemResponseDTO(
				1L, 
				"testSystemName",//system.getSystemName(), 
				"localhost",//system.getAddress(), 
				12345,//system.getPort(), 
				"authinfo",//system.getAuthenticationInfo(),
				Map.of(),
				Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()),
				Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()));

	}
}
