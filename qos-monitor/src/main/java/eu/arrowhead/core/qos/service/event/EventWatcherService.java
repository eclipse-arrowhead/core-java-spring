package eu.arrowhead.core.qos.service.event;

import javax.annotation.Resource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.shared.EventDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.core.qos.dto.event.FinishedMonitoringMeasurementEventDTO;
import eu.arrowhead.core.qos.dto.event.InteruptedMonitoringMeasurementEventDTO;
import eu.arrowhead.core.qos.dto.event.ReceivedMonitoringRequestEventDTO;
import eu.arrowhead.core.qos.dto.event.StartedMonitoringMeasurementEventDTO;
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
				final ReceivedMonitoringRequestEventDTO validReceivedMonitoringRequestEvent = validateReceivedMonitoringRequestEvent(event);
				receivedMonitoringRequestEventQueue.put(validReceivedMonitoringRequestEvent);
				break;
			case STARTED_MONITORING_MEASUREMENT:
				final StartedMonitoringMeasurementEventDTO validStartedMonitoringMeasurementEvent = validateStartedMonitoringMeasurementEvent(event);
				startedMonitoringMeasurementEventQueue.put(validStartedMonitoringMeasurementEvent);
				break;
			case FINISHED_MONITORING_MEASUREMENT:
				final FinishedMonitoringMeasurementEventDTO validFinishedMonitoringMeasurementEvent = validateFinishedMonitoringMeasurementEvent(event);
				finishedMonitoringMeasurementEventQueue.put(validFinishedMonitoringMeasurementEvent);
				break;
			case INTERUPTED_MONITORING_MEASUREMENT:
				final InteruptedMonitoringMeasurementEventDTO validInteruptedMonitoringMeasurementEvent = validateInteruptedMonitoringMeasurementEvent(event);
				interuptedMonitoringMeasurementEventQueue.put(validInteruptedMonitoringMeasurementEvent);
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
	private InteruptedMonitoringMeasurementEventDTO validateInteruptedMonitoringMeasurementEvent(final EventDTO event) {
		logger.debug("validateInteruptedMonitoringMeasurementEvent started...");

		Assert.isTrue(!Utilities.isEmpty(event.getPayload()), "Event payload is empty");
		Assert.isTrue(!Utilities.isEmpty(event.getTimeStamp()), "Event timeStamp is empty");

		final InteruptedMonitoringMeasurementEventDTO validEvent = new InteruptedMonitoringMeasurementEventDTO();
		validEvent.setEventType(QosMonitorEventType.INTERUPTED_MONITORING_MEASUREMENT.name());
		validEvent.setMetaData(event.getMetaData());
		validEvent.setPayload(event.getPayload());
		validEvent.setTimeStamp(event.getTimeStamp());

		return validEvent;
	}

	//-------------------------------------------------------------------------------------------------
	private FinishedMonitoringMeasurementEventDTO validateFinishedMonitoringMeasurementEvent(final EventDTO event) {
		logger.debug("validateFinishedMonitoringMeasurementEvent started...");

		return null;
	}

	//-------------------------------------------------------------------------------------------------
	private StartedMonitoringMeasurementEventDTO validateStartedMonitoringMeasurementEvent(final EventDTO event) {
		logger.debug("validateStartedMonitoringMeasurementEvent started...");

		return null;
	}

	//-------------------------------------------------------------------------------------------------
	private ReceivedMonitoringRequestEventDTO validateReceivedMonitoringRequestEvent(final EventDTO event) {
		logger.debug("validateReceivedMonitoringRequestEvent started...");

		return null;
	}
}
