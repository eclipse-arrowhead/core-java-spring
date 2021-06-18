package eu.arrowhead.core.qos.service.ping.monitor;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import javax.annotation.Resource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import eu.arrowhead.common.dto.shared.EventDTO;
import eu.arrowhead.common.dto.shared.QosMonitorEventType;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.core.qos.QosMonitorConstants;
import eu.arrowhead.core.qos.dto.event.EventDTOConverter;
import eu.arrowhead.core.qos.dto.event.monitoringevents.FinishedMonitoringMeasurementEventDTO;
import eu.arrowhead.core.qos.dto.event.monitoringevents.InterruptedMonitoringMeasurementEventDTO;
import eu.arrowhead.core.qos.dto.event.monitoringevents.MeasurementMonitoringEvent;
import eu.arrowhead.core.qos.dto.event.monitoringevents.ReceivedMonitoringRequestEventDTO;
import eu.arrowhead.core.qos.dto.event.monitoringevents.StartedMonitoringMeasurementEventDTO;

public class PingEventCollectorTask implements Runnable {

	//=================================================================================================
	// members

	private static final String NOT_SUPPORTED_EVENT_TYPE = " is not a supported event type. ";
	private static final String REPLACING_MEASUREMENT_EVENT = " - measurement , duplicate event. Overwriting : ";

	private static final long clearingInterval = 
			1000/*Mills to Sec*/
			* 60/*Sec to Min*/
			* 10/*Average measurement max time*/;

	private boolean interrupted = false;
	private long lastBufferCleanAt;

	@Resource(name = QosMonitorConstants.EVENT_QUEUE)
	private LinkedBlockingQueue<EventDTO> eventQueue;

	@Resource(name = QosMonitorConstants.EVENT_BUFFER)
	private ConcurrentHashMap<UUID, PingEventBufferElement> eventBuffer;

	@Autowired
	private PingEventBufferCleaner bufferCleaner;

	private final Logger logger = LogManager.getLogger(PingEventCollectorTask.class);

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------	
	@Override
	public void run() {
		logger.debug("PingEventCollectorTask run started...");

		interrupted = Thread.currentThread().isInterrupted();
		clearBuffer();

		while (!interrupted) {
			logger.debug("PingEventCollectorTask run loop started...");

			try {
				putEventToBuffer(eventQueue.take());

				if (lastBufferCleanAt + clearingInterval < System.currentTimeMillis()) {

					clearBuffer();
					lastBufferCleanAt = System.currentTimeMillis();
				}

			} catch (final InterruptedException ex) {

				logger.debug("PingEventCollectorTask run interrupted");
				interrupted = true;

			}catch (final Throwable ex) {

				logger.debug(ex.getMessage());
			}
		}
	}

	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	private void clearBuffer() {
		logger.debug("clearBuffer started...");

		bufferCleaner.clearBuffer();
	}

	//-------------------------------------------------------------------------------------------------
	private void addEventToBufferElement (final PingEventBufferElement element, final int position, final MeasurementMonitoringEvent event) {
		logger.debug("addEventToBufferElement started...");

		if(element.getEventArray()[position] != null) {
			logger.warn(element.getId() + REPLACING_MEASUREMENT_EVENT + element.getEventArray()[position].toString());
		}

		element.addEvent(position, event);

	}

	//-------------------------------------------------------------------------------------------------
	private void putEventToBuffer(final EventDTO event) {
		logger.debug("putEventToBuffer started...");

		final UUID id = UUID.fromString(event.getMetaData().get(QosMonitorConstants.PROCESS_ID_KEY));

		PingEventBufferElement element = eventBuffer.get(id);
		if (element == null) {
			element = new PingEventBufferElement(id);
		}

		try {
			final QosMonitorEventType eventType = QosMonitorEventType.valueOf(event.getEventType());

			switch (eventType) {
			case RECEIVED_MONITORING_REQUEST:
				final ReceivedMonitoringRequestEventDTO validReceivedRequestEvent = EventDTOConverter.convertToReceivedMonitoringRequestEvent(event);

				addEventToBufferElement(element, QosMonitorConstants.RECEIVED_MONITORING_REQUEST_EVENT_POSITION, validReceivedRequestEvent);
				break;
			case STARTED_MONITORING_MEASUREMENT:
				final StartedMonitoringMeasurementEventDTO validStartedEvent = EventDTOConverter.convertToStartedMonitoringMeasurementEvent(event);

				addEventToBufferElement(element, QosMonitorConstants.STARTED_MONITORING_MEASUREMENT_EVENT_POSITION, validStartedEvent);
				break;
			case FINISHED_MONITORING_MEASUREMENT:
				final FinishedMonitoringMeasurementEventDTO validFinishEvent = EventDTOConverter.convertToFinishedMonitoringMeasurementEvent(event);

				addEventToBufferElement(element, QosMonitorConstants.FINISHED_MONITORING_MEASUREMENT_EVENT_POSITION, validFinishEvent);
				break;
			case INTERRUPTED_MONITORING_MEASUREMENT:
				final InterruptedMonitoringMeasurementEventDTO validInterruptEvent = EventDTOConverter.convertToInterruptedMonitoringMeasurementEvent(event);

				addEventToBufferElement(element, QosMonitorConstants.INTERRUPTED_MONITORING_MEASUREMENT_EVENT_POSITION, validInterruptEvent);
				break;
			default:
				throw new InvalidParameterException(eventType + NOT_SUPPORTED_EVENT_TYPE);
			}

		} catch (final ArrowheadException | IllegalArgumentException ex) {

			logger.debug(ex.getMessage());

		}

		eventBuffer.put(id, element);

	}

}
