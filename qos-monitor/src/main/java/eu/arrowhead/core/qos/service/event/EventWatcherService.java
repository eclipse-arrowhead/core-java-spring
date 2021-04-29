package eu.arrowhead.core.qos.service.event;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Resource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.shared.EventDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.core.qos.dto.IcmpPingResponse;
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

	private static final ObjectMapper mapper = new ObjectMapper();

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

		Assert.isTrue(!Utilities.isEmpty(event.getPayload()), "Event payload is empty");
		Assert.isTrue(!Utilities.isEmpty(event.getTimeStamp()), "Event timeStamp is empty");

		try {
			final QosMonitorEventType eventType = QosMonitorEventType.valueOf(event.getEventType());

			switch (eventType) {
			case RECEIVED_MONITORING_REQUEST:
				final ReceivedMonitoringRequestEventDTO validReceivedMonitoringRequestEvent = convertToReceivedMonitoringRequestEvent(event);
				receivedMonitoringRequestEventQueue.put(validReceivedMonitoringRequestEvent);
				break;
			case STARTED_MONITORING_MEASUREMENT:
				final StartedMonitoringMeasurementEventDTO validStartedMonitoringMeasurementEvent = convertToStartedMonitoringMeasurementEvent(event);
				startedMonitoringMeasurementEventQueue.put(validStartedMonitoringMeasurementEvent);
				break;
			case FINISHED_MONITORING_MEASUREMENT:
				final FinishedMonitoringMeasurementEventDTO validFinishedMonitoringMeasurementEvent = convertToFinishedMonitoringMeasurementEvent(event);
				finishedMonitoringMeasurementEventQueue.put(validFinishedMonitoringMeasurementEvent);
				break;
			case INTERUPTED_MONITORING_MEASUREMENT:
				final InteruptedMonitoringMeasurementEventDTO validInteruptedMonitoringMeasurementEvent = convertToInteruptedMonitoringMeasurementEvent(event);
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

	//TODO Move converters to separate class
	private InteruptedMonitoringMeasurementEventDTO convertToInteruptedMonitoringMeasurementEvent(final EventDTO event) {
		logger.debug("convertToInteruptedMonitoringMeasurementEvent started...");

		final InteruptedMonitoringMeasurementEventDTO validEvent = new InteruptedMonitoringMeasurementEventDTO();
		validEvent.setEventType(QosMonitorEventType.INTERUPTED_MONITORING_MEASUREMENT);
		validEvent.setMetaData(event.getMetaData());
		validEvent.setPayload(event.getPayload());
		validEvent.setTimeStamp(Utilities.parseUTCStringToLocalZonedDateTime(event.getTimeStamp()));

		return validEvent;
	}

	//-------------------------------------------------------------------------------------------------
	private FinishedMonitoringMeasurementEventDTO convertToFinishedMonitoringMeasurementEvent(final EventDTO event) {
		logger.debug("convertToFinishedMonitoringMeasurementEvent started...");

		final FinishedMonitoringMeasurementEventDTO validEvent = new FinishedMonitoringMeasurementEventDTO();
		validEvent.setEventType(QosMonitorEventType.FINISHED_MONITORING_MEASUREMENT);
		validEvent.setMetaData(event.getMetaData());
		validEvent.setPayload(convertToIcmpPingResponse(event.getPayload()));
		validEvent.setTimeStamp(Utilities.parseUTCStringToLocalZonedDateTime(event.getTimeStamp()));

		return validEvent;
	}

	//-------------------------------------------------------------------------------------------------
	private List<IcmpPingResponse> convertToIcmpPingResponse(final String payload) {
		logger.debug("convertToIcmpPingResponse started...");

		try {
			final List<IcmpPingResponse> validResponse = Arrays.asList(mapper.readValue(payload, IcmpPingResponse.class));

			return validResponse;

		} catch (final IOException e) {

			throw new InvalidParameterException("Invalid IcmpPingResponse");
		}
	}

	//-------------------------------------------------------------------------------------------------
	private StartedMonitoringMeasurementEventDTO convertToStartedMonitoringMeasurementEvent(final EventDTO event) {
		logger.debug("convertToStartedMonitoringMeasurementEvent started...");

		final StartedMonitoringMeasurementEventDTO validEvent = new StartedMonitoringMeasurementEventDTO();
		validEvent.setEventType(QosMonitorEventType.STARTED_MONITORING_MEASUREMENT);
		validEvent.setMetaData(event.getMetaData());
		validEvent.setPayload(event.getPayload());
		validEvent.setTimeStamp(Utilities.parseUTCStringToLocalZonedDateTime(event.getTimeStamp()));

		return validEvent;
	}

	//-------------------------------------------------------------------------------------------------
	private ReceivedMonitoringRequestEventDTO convertToReceivedMonitoringRequestEvent(final EventDTO event) {
		logger.debug("convertToReceivedMonitoringRequestEvent started...");

		final ReceivedMonitoringRequestEventDTO validEvent = new ReceivedMonitoringRequestEventDTO();
		validEvent.setEventType(QosMonitorEventType.RECEIVED_MONITORING_REQUEST);
		validEvent.setMetaData(event.getMetaData());
		validEvent.setPayload(event.getPayload());
		validEvent.setTimeStamp(Utilities.parseUTCStringToLocalZonedDateTime(event.getTimeStamp()));

		return validEvent;
	}
}
