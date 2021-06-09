package eu.arrowhead.core.qos.service.event;

import java.util.concurrent.LinkedBlockingQueue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;

import eu.arrowhead.common.dto.shared.EventDTO;
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
	}
}
