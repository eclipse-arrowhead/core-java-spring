package eu.arrowhead.core.qos.service.ping.monitor;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.shared.EventDTO;
import eu.arrowhead.common.dto.shared.QosMonitorEventType;
import eu.arrowhead.core.qos.QosMonitorConstants;
import eu.arrowhead.core.qos.dto.event.EventDTOConverter;
import eu.arrowhead.core.qos.dto.event.monitoringevents.MeasurementMonitoringEvent;

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
	public void testRunPingEventCollectorTaskWithReceivedEventOk() {
		ReflectionTestUtils.setField(pingEventCollectorTask, "interrupted", false);

		final ArgumentCaptor<PingEventBufferElement> bufferElementValueCapture = ArgumentCaptor.forClass(PingEventBufferElement.class);
		final EventDTO event = getValidReceivedMeasurementRequestEventDTOForTest();

		try {
			when(eventQueue.take()).thenReturn(event).thenThrow(InterruptedException.class);
		} catch (final InterruptedException ex) {
			fail();
		}

		final PingEventBufferElement formerElement = null;
		doNothing().when(bufferCleaner).clearBuffer();
		when(eventBuffer.put(any(), bufferElementValueCapture.capture())).thenReturn(formerElement);

		pingEventCollectorTask.run();

		try {
			verify(eventQueue, times(2)).take();
		} catch (final InterruptedException ex) {
			fail();
		}

		verify(bufferCleaner, atLeastOnce()).clearBuffer();
		verify(eventBuffer, times(1)).put(any(), any());

		final PingEventBufferElement bufferElement = bufferElementValueCapture.getValue();

		assertNotNull(bufferElement);
		assertNotNull(bufferElement.getEventArray());

		final MeasurementMonitoringEvent[] eventArray= bufferElement.getEventArray();
		assertNotNull(eventArray[QosMonitorConstants.RECEIVED_MONITORING_REQUEST_EVENT_POSITION]);
		assertNull(eventArray[QosMonitorConstants.STARTED_MONITORING_MEASUREMENT_EVENT_POSITION]);
		assertNull(eventArray[QosMonitorConstants.FINISHED_MONITORING_MEASUREMENT_EVENT_POSITION]);
		assertNull(eventArray[QosMonitorConstants.INTERRUPTED_MONITORING_MEASUREMENT_EVENT_POSITION]);

		final Boolean runInterupted = (Boolean) ReflectionTestUtils.getField(pingEventCollectorTask, "interrupted");
		assertTrue(runInterupted);
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRunPingEventCollectorTaskWithStartEventOk() {
		ReflectionTestUtils.setField(pingEventCollectorTask, "interrupted", false);

		final ArgumentCaptor<PingEventBufferElement> bufferElementValueCapture = ArgumentCaptor.forClass(PingEventBufferElement.class);
		final EventDTO event = getValidStartEventDTOForTest();

		try {
			when(eventQueue.take()).thenReturn(event).thenThrow(InterruptedException.class);
		} catch (final InterruptedException ex) {
			fail();
		}

		final PingEventBufferElement formerElement = null;
		doNothing().when(bufferCleaner).clearBuffer();
		when(eventBuffer.put(any(), bufferElementValueCapture.capture())).thenReturn(formerElement);

		pingEventCollectorTask.run();

		try {
			verify(eventQueue, times(2)).take();
		} catch (final InterruptedException ex) {
			fail();
		}

		verify(bufferCleaner, atLeastOnce()).clearBuffer();
		verify(eventBuffer, times(1)).put(any(), any());

		final PingEventBufferElement bufferElement = bufferElementValueCapture.getValue();

		assertNotNull(bufferElement);
		assertNotNull(bufferElement.getEventArray());

		final MeasurementMonitoringEvent[] eventArray= bufferElement.getEventArray();
		assertNull(eventArray[QosMonitorConstants.RECEIVED_MONITORING_REQUEST_EVENT_POSITION]);
		assertNotNull(eventArray[QosMonitorConstants.STARTED_MONITORING_MEASUREMENT_EVENT_POSITION]);
		assertNull(eventArray[QosMonitorConstants.FINISHED_MONITORING_MEASUREMENT_EVENT_POSITION]);
		assertNull(eventArray[QosMonitorConstants.INTERRUPTED_MONITORING_MEASUREMENT_EVENT_POSITION]);

		final Boolean runInterupted = (Boolean) ReflectionTestUtils.getField(pingEventCollectorTask, "interrupted");
		assertTrue(runInterupted);
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRunPingEventCollectorTaskWithFinishedEventOk() {
		ReflectionTestUtils.setField(pingEventCollectorTask, "interrupted", false);

		final ArgumentCaptor<PingEventBufferElement> bufferElementValueCapture = ArgumentCaptor.forClass(PingEventBufferElement.class);
		final EventDTO event = getValidFinishedEventDTOForTest();

		try {
			when(eventQueue.take()).thenReturn(event).thenThrow(InterruptedException.class);
		} catch (final InterruptedException ex) {
			fail();
		}

		final PingEventBufferElement formerElement = null;
		doNothing().when(bufferCleaner).clearBuffer();
		when(eventBuffer.put(any(), bufferElementValueCapture.capture())).thenReturn(formerElement);

		pingEventCollectorTask.run();

		try {
			verify(eventQueue, times(2)).take();
		} catch (final InterruptedException ex) {
			fail();
		}

		verify(bufferCleaner, atLeastOnce()).clearBuffer();
		verify(eventBuffer, times(1)).put(any(), any());

		final PingEventBufferElement bufferElement = bufferElementValueCapture.getValue();

		assertNotNull(bufferElement);
		assertNotNull(bufferElement.getEventArray());

		final MeasurementMonitoringEvent[] eventArray= bufferElement.getEventArray();
		assertNull(eventArray[QosMonitorConstants.RECEIVED_MONITORING_REQUEST_EVENT_POSITION]);
		assertNull(eventArray[QosMonitorConstants.STARTED_MONITORING_MEASUREMENT_EVENT_POSITION]);
		assertNotNull(eventArray[QosMonitorConstants.FINISHED_MONITORING_MEASUREMENT_EVENT_POSITION]);
		assertNull(eventArray[QosMonitorConstants.INTERRUPTED_MONITORING_MEASUREMENT_EVENT_POSITION]);

		final Boolean runInterupted = (Boolean) ReflectionTestUtils.getField(pingEventCollectorTask, "interrupted");
		assertTrue(runInterupted);
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRunPingEventCollectorTaskWithInterruptedEventOk() {
		ReflectionTestUtils.setField(pingEventCollectorTask, "interrupted", false);

		final ArgumentCaptor<PingEventBufferElement> bufferElementValueCapture = ArgumentCaptor.forClass(PingEventBufferElement.class);
		final EventDTO event = getValidInterruptedDTOForTest();

		try {
			when(eventQueue.take()).thenReturn(event).thenThrow(InterruptedException.class);
		} catch (final InterruptedException ex) {
			fail();
		}

		final PingEventBufferElement formerElement = null;
		doNothing().when(bufferCleaner).clearBuffer();
		when(eventBuffer.put(any(), bufferElementValueCapture.capture())).thenReturn(formerElement);

		pingEventCollectorTask.run();

		try {
			verify(eventQueue, times(2)).take();
		} catch (final InterruptedException ex) {
			fail();
		}

		verify(bufferCleaner, atLeastOnce()).clearBuffer();
		verify(eventBuffer, times(1)).put(any(), any());

		final PingEventBufferElement bufferElement = bufferElementValueCapture.getValue();

		assertNotNull(bufferElement);
		assertNotNull(bufferElement.getEventArray());

		final MeasurementMonitoringEvent[] eventArray= bufferElement.getEventArray();
		assertNull(eventArray[QosMonitorConstants.RECEIVED_MONITORING_REQUEST_EVENT_POSITION]);
		assertNull(eventArray[QosMonitorConstants.STARTED_MONITORING_MEASUREMENT_EVENT_POSITION]);
		assertNull(eventArray[QosMonitorConstants.FINISHED_MONITORING_MEASUREMENT_EVENT_POSITION]);
		assertNotNull(eventArray[QosMonitorConstants.INTERRUPTED_MONITORING_MEASUREMENT_EVENT_POSITION]);

		final Boolean runInterupted = (Boolean) ReflectionTestUtils.getField(pingEventCollectorTask, "interrupted");
		assertTrue(runInterupted);
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRunPingEventCollectorTaskAddEventToOccupiedPosition() {
		ReflectionTestUtils.setField(pingEventCollectorTask, "interrupted", false);

		final ArgumentCaptor<PingEventBufferElement> bufferElementValueCapture = ArgumentCaptor.forClass(PingEventBufferElement.class);
		final EventDTO event = getValidInterruptedDTOForTest();

		try {
			when(eventQueue.take()).thenReturn(event).thenThrow(InterruptedException.class);
		} catch (final InterruptedException ex) {
			fail();
		}

		final PingEventBufferElement formerElement = new PingEventBufferElement(UUID.fromString(event.getMetaData().get(QosMonitorConstants.PROCESS_ID_KEY)));
		formerElement.addEvent(
				QosMonitorConstants.INTERRUPTED_MONITORING_MEASUREMENT_EVENT_POSITION,
				EventDTOConverter.convertToInterruptedMonitoringMeasurementEvent(event));

		doNothing().when(bufferCleaner).clearBuffer();
		when(eventBuffer.get(any())).thenReturn(formerElement);
		when(eventBuffer.put(any(), bufferElementValueCapture.capture())).thenReturn(formerElement);

		pingEventCollectorTask.run();

		try {
			verify(eventQueue, times(2)).take();
		} catch (final InterruptedException ex) {
			fail();
		}

		verify(bufferCleaner, atLeastOnce()).clearBuffer();
		verify(eventBuffer, times(1)).put(any(), any());

		final PingEventBufferElement bufferElement = bufferElementValueCapture.getValue();

		assertNotNull(bufferElement);
		assertNotNull(bufferElement.getEventArray());

		final MeasurementMonitoringEvent[] eventArray= bufferElement.getEventArray();
		assertNull(eventArray[QosMonitorConstants.RECEIVED_MONITORING_REQUEST_EVENT_POSITION]);
		assertNull(eventArray[QosMonitorConstants.STARTED_MONITORING_MEASUREMENT_EVENT_POSITION]);
		assertNull(eventArray[QosMonitorConstants.FINISHED_MONITORING_MEASUREMENT_EVENT_POSITION]);
		assertNotNull(eventArray[QosMonitorConstants.INTERRUPTED_MONITORING_MEASUREMENT_EVENT_POSITION]);

		final Boolean runInterupted = (Boolean) ReflectionTestUtils.getField(pingEventCollectorTask, "interrupted");
		assertTrue(runInterupted);
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRunPingEventCollectorTaskInvalidEventDTO() {

		ReflectionTestUtils.setField(pingEventCollectorTask, "interrupted", false);

		final ArgumentCaptor<PingEventBufferElement> bufferElementValueCapture = ArgumentCaptor.forClass(PingEventBufferElement.class);
		final EventDTO event = getInValidEventTypeEventDTOForTest();

		try {
			when(eventQueue.take()).thenReturn(event).thenThrow(InterruptedException.class);
		} catch (final InterruptedException ex) {
			fail();
		}

		final PingEventBufferElement formerElement = null;
		doNothing().when(bufferCleaner).clearBuffer();
		when(eventBuffer.put(any(), bufferElementValueCapture.capture())).thenReturn(formerElement);

		pingEventCollectorTask.run();

		try {
			verify(eventQueue, times(2)).take();
		} catch (final InterruptedException ex) {
			fail();
		}

		verify(bufferCleaner, atLeastOnce()).clearBuffer();
		verify(eventBuffer, times(1)).put(any(), any());

		final PingEventBufferElement bufferElement = bufferElementValueCapture.getValue();

		assertNotNull(bufferElement);
		assertNotNull(bufferElement.getEventArray());

		final MeasurementMonitoringEvent[] eventArray= bufferElement.getEventArray();
		assertNull(eventArray[QosMonitorConstants.RECEIVED_MONITORING_REQUEST_EVENT_POSITION]);
		assertNull(eventArray[QosMonitorConstants.STARTED_MONITORING_MEASUREMENT_EVENT_POSITION]);
		assertNull(eventArray[QosMonitorConstants.FINISHED_MONITORING_MEASUREMENT_EVENT_POSITION]);
		assertNull(eventArray[QosMonitorConstants.INTERRUPTED_MONITORING_MEASUREMENT_EVENT_POSITION]);

		final Boolean runInterupted = (Boolean) ReflectionTestUtils.getField(pingEventCollectorTask, "interrupted");
		assertTrue(runInterupted);
	}

	//=================================================================================================
	// assistant methods

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
	private EventDTO getValidStartEventDTOForTest() {

		final EventDTO event = new EventDTO();
		event.setEventType(QosMonitorEventType.STARTED_MONITORING_MEASUREMENT.name());
		event.setMetaData(getValidMeasuermentEventDTOMetadtaProcessIdForTest());
		event.setPayload(getValidMeasuermentEventDTOEmptyPayloadForTest());
		event.setTimeStamp(Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()));

		return event;
	}

	//-------------------------------------------------------------------------------------------------
	private EventDTO getValidFinishedEventDTOForTest() {

		final EventDTO event = new EventDTO();
		event.setEventType(QosMonitorEventType.FINISHED_MONITORING_MEASUREMENT.name());
		event.setMetaData(getValidMeasuermentEventDTOMetadtaProcessIdForTest());
		event.setPayload(getValidMeasuermentEventDTOEmptyPayloadForTest());
		event.setTimeStamp(Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()));

		return event;
	}

	//-------------------------------------------------------------------------------------------------
	private EventDTO getValidInterruptedDTOForTest() {

		final EventDTO event = new EventDTO();
		event.setEventType(QosMonitorEventType.INTERRUPTED_MONITORING_MEASUREMENT.name());
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

}
