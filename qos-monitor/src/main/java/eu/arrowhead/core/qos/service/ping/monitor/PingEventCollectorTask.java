package eu.arrowhead.core.qos.service.ping.monitor;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import javax.annotation.Resource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.Assert;

import eu.arrowhead.common.dto.shared.EventDTO;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.core.qos.QosMonitorConstants;

public class PingEventCollectorTask implements Runnable {

	//=================================================================================================
	// members

	private final long clearingInterval = 1000 * 60 * 10;

	private boolean interrupted = false;
	private long lastBufferCleanAt;

	@Resource(name = QosMonitorConstants.EVENT_QUEUE)
	private LinkedBlockingQueue<EventDTO> eventQueue;

	@Resource(name = QosMonitorConstants.EVENT_BUFFER)
	private ConcurrentHashMap<UUID, PingEventBufferElement> eventBuffer;

	private final Logger logger = LogManager.getLogger(PingEventCollectorTask.class);

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------	
	@Override
	public void run() {

		interrupted = Thread.currentThread().isInterrupted();
		clearBuffer();

		while (!interrupted) {
			logger.debug("PingEventCollectorTask run loop started...");
			try {
				putEventToBuffer(eventQueue.take());

				if (lastBufferCleanAt + clearingInterval < System.currentTimeMillis()) {

					lastBufferCleanAt = System.currentTimeMillis();
					clearBuffer();
				}

			} catch (final InterruptedException ex) {

				logger.debug("PingEventCollectorTask run intrrupted");
				interrupted = false;
			}
		}
	}

	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	private void clearBuffer() {
		logger.debug("clearBuffer started...");

		
	}

	//-------------------------------------------------------------------------------------------------
	private void validateEvent(final EventDTO event) {
		logger.debug("validateEvent started...");

		try {
			Assert.notNull(event, "Event is null. ");
			Assert.notNull(event.getMetaData(), "Event metadata is null. ");
			Assert.notNull(event.getEventType(), "Event type is null. ");
			Assert.notNull(event.getTimeStamp(), "Event timeStamp is null. ");
			Assert.notNull(event.getMetaData().get(QosMonitorConstants.PROCESS_ID_KEY), "Event processId is null. ");
		} catch (final IllegalArgumentException ex) {

			throw new InvalidParameterException(ex.getMessage());
		}

		try {
			UUID.fromString(event.getMetaData().get(QosMonitorConstants.PROCESS_ID_KEY));
		} catch (final IllegalArgumentException ex) {

			throw new InvalidParameterException("Cloud not parse Event processId to UUID");
		}
	}
	//-------------------------------------------------------------------------------------------------
	private void putEventToBuffer(final EventDTO event) {
		logger.debug("putEventToBuffer started...");

		validateEvent(event);

		final UUID id = UUID.fromString(event.getMetaData().get(QosMonitorConstants.PROCESS_ID_KEY));

		PingEventBufferElement element = eventBuffer.get(id);
		if (element != null) {
			element.addEvent(event);
		}else {
			element = new PingEventBufferElement(id);
			element.addEvent(event);
		}

		eventBuffer.put(id, element);

	}

}
