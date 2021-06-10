package eu.arrowhead.core.qos.service.ping.monitor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;

import eu.arrowhead.common.exception.ArrowheadException;

@RunWith(SpringRunner.class)
public class PingEventProcessorTest {

	//=================================================================================================
	// members
	@InjectMocks
	private PingEventProcessor pingEventProcessor;

	@Mock
	private ConcurrentHashMap<UUID, PingEventBufferElement> eventBuffer;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testProcessEventsProcessIdNull() {

		final UUID processId = null;
		final long measurementExpiryTime = 1L;

		pingEventProcessor.processEvents(processId, measurementExpiryTime);

		verify(eventBuffer, never()).get(any());
	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testProcessEventsMeasurementExpiryTimeIsLessThenOne() {

		final UUID processId = UUID.randomUUID();
		final long measurementExpiryTime = 0L;

		pingEventProcessor.processEvents(processId, measurementExpiryTime);

		verify(eventBuffer, never()).get(any());
	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testProcessEventsMeasurementExpiryTimeIsInNotInTheFuture() {

		final UUID processId = UUID.randomUUID();
		final long measurementExpiryTime = System.currentTimeMillis();

		pingEventProcessor.processEvents(processId, measurementExpiryTime);

		verify(eventBuffer, never()).get(any());
	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testProcessEventsNoEventWithinMeasurementExpiryTime() {

		final UUID processId = UUID.randomUUID();
		final long measurementExpiryTime = System.currentTimeMillis() + 10000;

		final PingEventBufferElement bufferElement = null;

		when(eventBuffer.get(any())).thenReturn(bufferElement);

		pingEventProcessor.processEvents(processId, measurementExpiryTime);

		verify(eventBuffer, atLeastOnce()).get(any());
		verify(eventBuffer, never()).remove(any());
	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testProcessEventsInterrruptedEvent() {

		final UUID processId = UUID.randomUUID();
		final long measurementExpiryTime = System.currentTimeMillis() + 10000;

		final PingEventBufferElement bufferElement = new PingEventBufferElement(processId);
		

		when(eventBuffer.get(any())).thenReturn(bufferElement);

		pingEventProcessor.processEvents(processId, measurementExpiryTime);

		verify(eventBuffer, atLeastOnce()).get(any());
		verify(eventBuffer, never()).remove(any());
	}
}
