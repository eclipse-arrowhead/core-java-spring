package eu.arrowhead.core.qos.service.ping.monitor;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.arrowhead.common.dto.shared.EventDTO;
import eu.arrowhead.common.dto.shared.FinishedMonitoringMeasurementEventDTO;
import eu.arrowhead.common.dto.shared.QosMonitorEventType;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.core.qos.QosMonitorConstants;
import eu.arrowhead.core.qos.dto.IcmpPingDTOConverter;
import eu.arrowhead.core.qos.dto.IcmpPingResponse;
import eu.arrowhead.core.qos.dto.event.EventDTOConverter;

@Service
public class PingEventProcessor {
	//=================================================================================================
	// members

	private static final long SLEEP_PERIOD = TimeUnit.SECONDS.toMillis(1);

	@Resource(name = QosMonitorConstants.EVENT_BUFFER)
	private ConcurrentHashMap<UUID, PingEventBufferElement> eventBuffer;

	private final Logger logger = LogManager.getLogger(PingEventProcessor.class);

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------	
	public List<IcmpPingResponse> processEvents(final UUID id, final long timeOut) {
		logger.debug("processEvents started...");

		Assert.notNull(id, "Event id could not be null.");
		Assert.isTrue(timeOut > 0, "TimeOut should be greater then zero");

		boolean hasReceived = false;
		boolean hasStarted = false;
		boolean hasFinished = false;

		PingEventBufferElement partialResult = null;

		while ( System.currentTimeMillis() < timeOut) {

			final PingEventBufferElement element = eventBuffer.get(id);
			if (id == null) {

				rest();
				continue;

			}else if (element.getEventlist().isEmpty()){

				rest();
				continue;

			}

			final List<EventDTO> eventList = element.getEventlist();
			for (final EventDTO eventDTO : eventList) {

				if (eventDTO.getEventType().equalsIgnoreCase(QosMonitorEventType.INTERUPTED_MONITORING_MEASUREMENT.name())) {

					handleInterruptEvent(id, eventDTO);
				}

				if (eventDTO.getEventType().equalsIgnoreCase(QosMonitorEventType.RECEIVED_MONITORING_REQUEST.name())) {

					hasReceived = handleReceivedRequestEvent(id, eventDTO);
				}

				if (eventDTO.getEventType().equalsIgnoreCase(QosMonitorEventType.STARTED_MONITORING_MEASUREMENT.name())) {

					hasStarted = handleStartedEvent(id, eventDTO);
				}

				if (eventDTO.getEventType().equalsIgnoreCase(QosMonitorEventType.FINISHED_MONITORING_MEASUREMENT.name())) {

					final FinishedMonitoringMeasurementEventDTO result = EventDTOConverter.convertToFinishedMonitoringMeasurementEvent(eventDTO);
					if (hasReceived && hasStarted && hasFinished) {

						eventBuffer.remove(id);

						return IcmpPingDTOConverter.convertPingMeasurementResult(result.getPayload());

					}else {
						partialResult = element;
					}
				}
			}



		}

		logger.debug("External ping measurement finsched whith partial results: " + partialResult.toString());
		throw new ArrowheadException("Timeout on external ping measurement : " + id);
	}


	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	private void rest() {
		try {
			Thread.sleep(SLEEP_PERIOD);
		} catch (final InterruptedException e) {
			logger.warn(e.getMessage());
		}
	}

	//-------------------------------------------------------------------------------------------------
	private void handleInterruptEvent(final UUID id, final EventDTO event) {
		logger.debug("handleInterruptEvent started");

		//TODO validateEvent(event);
		eventBuffer.remove(id);

		logger.debug(id.toString() + " : external ping measurement process finished with interrupt. ");

		throw new ArrowheadException("External ping measurement process interuppted: " + event.getMetaData().get(QosMonitorConstants.INTERRUPTED_MONITORING_MEASUREMENT_EVENT_PAYLOAD_METADATA_EXCEPTION_KEY));
	}

	//-------------------------------------------------------------------------------------------------
	private boolean handleReceivedRequestEvent(final UUID id, final EventDTO event) {
		logger.debug("handleReceivedRequestEvent started");

		//TODO validateEvent(event);

		final boolean hasReceived = true;
		logger.debug(id.toString() + " : external ping measurement process finished with interrupt. ");

		return hasReceived;
	}

	//-------------------------------------------------------------------------------------------------
	private boolean handleStartedEvent(final UUID id, final EventDTO event) {
		logger.debug("handleReceivedRequestEvent started");

		//TODO validateEvent(event);

		final boolean hasReceived = true;
		logger.debug(id.toString() + " : external ping measurement process finished with interrupt. ");

		return hasReceived;
	}
}
