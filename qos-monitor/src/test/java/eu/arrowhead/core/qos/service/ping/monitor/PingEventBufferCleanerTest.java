package eu.arrowhead.core.qos.service.ping.monitor;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.springframework.test.util.ReflectionTestUtils;

public class PingEventBufferCleanerTest {

	//=================================================================================================
	// members

	@InjectMocks
	private final PingEventBufferCleaner pingEventBufferCleaner = new PingEventBufferCleaner();

	private ConcurrentHashMap<UUID, PingEventBufferElement> eventBuffer;

	private Logger logger;

	//=================================================================================================
	// methods

	@SuppressWarnings("unchecked")
	//-------------------------------------------------------------------------------------------------
	@Before
	public void setUp() throws Exception {

		logger = mock(Logger.class);
		eventBuffer = mock(ConcurrentHashMap.class);

		ReflectionTestUtils.setField(pingEventBufferCleaner, "logger", logger);
		ReflectionTestUtils.setField(pingEventBufferCleaner, "eventBuffer", eventBuffer);

	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testClearBufferElementExpired() {

		final Collection<PingEventBufferElement> entries = new ArrayList<PingEventBufferElement>(1);
		final PingEventBufferElement element = new PingEventBufferElement(UUID.randomUUID());

		ReflectionTestUtils.setField(element, "createdAt", 0L);

		entries.add(element);

		when(eventBuffer.values()).thenReturn(entries);

		pingEventBufferCleaner.clearBuffer();

		verify(eventBuffer,  times(1)).values();
		verify(logger, times(2)).debug(anyString());
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testClearBufferElementNotExpired() {

		final Collection<PingEventBufferElement> entries = new ArrayList<PingEventBufferElement>(1);
		final PingEventBufferElement element = new PingEventBufferElement(UUID.randomUUID());

		entries.add(element);

		when(eventBuffer.values()).thenReturn(entries);

		pingEventBufferCleaner.clearBuffer();

		verify(eventBuffer,  times(1)).values();
		verify(logger, times(1)).debug(anyString());
	}

}
