package eu.arrowhead.core.qos.service.event;

import javax.annotation.Resource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import eu.arrowhead.common.dto.shared.EventDTO;
import eu.arrowhead.common.dto.shared.FinishedMonitoringMeasurementEventDTO;
import eu.arrowhead.common.dto.shared.InterruptedMonitoringMeasurementEventDTO;
import eu.arrowhead.common.dto.shared.QosMonitorEventType;
import eu.arrowhead.common.dto.shared.ReceivedMonitoringRequestEventDTO;
import eu.arrowhead.common.dto.shared.StartedMonitoringMeasurementEventDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.core.qos.dto.event.EventDTOConverter;
import eu.arrowhead.core.qos.service.event.queue.FinishedMonitoringMeasurementEventQueue;
import eu.arrowhead.core.qos.service.event.queue.InteruptedMonitoringMeasurementEventQueue;
import eu.arrowhead.core.qos.service.event.queue.ReceivedMonitoringRequestEventQueue;
import eu.arrowhead.core.qos.service.event.queue.StartedMonitoringMeasurementEventQueue;

@Service
public class EventWatcherService {

	//=================================================================================================
	// members

	private static final String NULL_ERROR_MESSAGE = " is null";
	private static final String NOT_SUPPORTED_EVENT_TYPE = " is not a supported event type. ";

	@Resource
	private ReceivedMonitoringRequestEventQueue receivedMonitoringRequestEventQueue;

	@Resource
	private StartedMonitoringMeasurementEventQueue startedMonitoringMeasurementEventQueue;

	@Resource
	private FinishedMonitoringMeasurementEventQueue finishedMonitoringMeasurementEventQueue;

	@Resource
	private InteruptedMonitoringMeasurementEventQueue interuptedMonitoringMeasurementEventQueue;

	protected Logger logger = LogManager.getLogger(EventWatcherService.class);

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------	
	public void putEventToQueue(final EventDTO event) {
		logger.debug("putEventToQueue started...");

		if (event == null) {
			throw new InvalidParameterException("Event" + NULL_ERROR_MESSAGE);
		}

		try {
			final QosMonitorEventType eventType = QosMonitorEventType.valueOf(event.getEventType());

			switch (eventType) {
			case RECEIVED_MONITORING_REQUEST:
				final ReceivedMonitoringRequestEventDTO validReceivedRequestEvent = EventDTOConverter.convertToReceivedMonitoringRequestEvent(event);
				receivedMonitoringRequestEventQueue.put(validReceivedRequestEvent);
				break;
			case STARTED_MONITORING_MEASUREMENT:
				final StartedMonitoringMeasurementEventDTO validStartedEvent = EventDTOConverter.convertToStartedMonitoringMeasurementEvent(event);
				startedMonitoringMeasurementEventQueue.put(validStartedEvent);
				break;
			case FINISHED_MONITORING_MEASUREMENT:
				final FinishedMonitoringMeasurementEventDTO validFinishEvent = EventDTOConverter.convertToFinishedMonitoringMeasurementEvent(event);
				finishedMonitoringMeasurementEventQueue.put(validFinishEvent);
				break;
			case INTERUPTED_MONITORING_MEASUREMENT:
				final InterruptedMonitoringMeasurementEventDTO validInteruptEvent = EventDTOConverter.convertToInteruptedMonitoringMeasurementEvent(event);
				interuptedMonitoringMeasurementEventQueue.put(validInteruptEvent);
				break;
			default:
				throw new InvalidParameterException(eventType + NOT_SUPPORTED_EVENT_TYPE);
			}

		} catch (final ArrowheadException ex) {
			logger.info(ex);

			throw ex;
		} catch (final InterruptedException ex) {
			logger.info(ex);

			throw new ArrowheadException(ex.getMessage());
		}

	}

	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------

}
