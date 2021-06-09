package eu.arrowhead.core.qos.service.event;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.LinkedBlockingQueue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;

import eu.arrowhead.common.dto.shared.EventDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.InvalidParameterException;

@RunWith(SpringRunner.class)
public class EventWatcherServiceTest {

	//=================================================================================================
	// members

	@InjectMocks
	private EventWatcherService eventWatcherService;

	@Mock
	private LinkedBlockingQueue<EventDTO> eventQueue;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testPutEventToQueueNullEvent() {

		eventWatcherService.putEventToQueue(null);

		verify(eventQueue, never()).add(any());
	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testPutEventToQueueEventQueueAddThrowsException() {

		doThrow(new IllegalStateException()).when(eventQueue).add(any());

		eventWatcherService.putEventToQueue(new EventDTO());

		verify(eventQueue, times(1)).add(any());
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testPutEventToQueueOk() {

		when(eventQueue.add(any())).thenReturn(true);

		eventWatcherService.putEventToQueue(new EventDTO());

		verify(eventQueue, times(1)).add(any());
	}
}
