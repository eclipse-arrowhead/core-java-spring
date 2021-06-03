package eu.arrowhead.core.qos.service.event;

import java.util.concurrent.LinkedBlockingQueue;

import javax.annotation.Resource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import eu.arrowhead.common.dto.shared.EventDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.core.qos.QosMonitorConstants;

@Service
public class EventWatcherService {

	//=================================================================================================
	// members

	private static final String NULL_ERROR_MESSAGE = " is null";

	@Resource( name = QosMonitorConstants.EVENT_QUEUE)
	private LinkedBlockingQueue<EventDTO> eventQueue;

	private Logger logger = LogManager.getLogger(EventWatcherService.class);

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------	
	public void putEventToQueue(final EventDTO event) {
		logger.debug("putEventToQueue started...");

		if (event == null) {
			throw new InvalidParameterException("Event" + NULL_ERROR_MESSAGE);
		}

		try {

			eventQueue.add(event);

		} catch (final Exception ex) {
			logger.debug(ex);

			throw new ArrowheadException(ex.getMessage());
		}

	}

}
