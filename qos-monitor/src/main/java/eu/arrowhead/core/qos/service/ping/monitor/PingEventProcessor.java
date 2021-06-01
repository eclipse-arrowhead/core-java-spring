package eu.arrowhead.core.qos.service.ping.monitor;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.arrowhead.common.dto.shared.monitoringevents.FinishedMonitoringMeasurementEventDTO;
import eu.arrowhead.common.dto.shared.monitoringevents.InterruptedMonitoringMeasurementEventDTO;
import eu.arrowhead.common.dto.shared.monitoringevents.MeasurementMonitoringEvent;
import eu.arrowhead.common.dto.shared.monitoringevents.ReceivedMonitoringRequestEventDTO;
import eu.arrowhead.common.dto.shared.monitoringevents.StartedMonitoringMeasurementEventDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.core.qos.QosMonitorConstants;
import eu.arrowhead.core.qos.dto.IcmpPingDTOConverter;
import eu.arrowhead.core.qos.dto.IcmpPingResponse;

@Service
public class PingEventProcessor {
	//=================================================================================================
	// members

	private static final long SLEEP_PERIOD = TimeUnit.SECONDS.toMillis(1);
	private static final String SUCCESS_FINISH_PHRASE = " successfully finished ";
	private static final String INTERRRUPTED_FINISH_PHRASE = " get interrupted";
	private static final String TIMEOUT_FINISH_PHRASE = " timed out";

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

		ReceivedMonitoringRequestEventDTO temporalReceivedRequestEvent = null;
		StartedMonitoringMeasurementEventDTO temporalStartedMonitoringEvent = null;

		while ( System.currentTimeMillis() < timeOut) {

			final PingEventBufferElement element = eventBuffer.get(id);
			if (element == null) {

				rest();
				continue;

			}else if (Arrays.asList(element.getEventlist()).isEmpty()){

				rest();
				continue;

			}

			final List<MeasurementMonitoringEvent> eventList = Arrays.asList(element.getEventlist());

			if (eventList.get(QosMonitorConstants.RECEIVED_MONITORING_REQUEST_EVENT_POSITION) != null) {

				temporalReceivedRequestEvent = (ReceivedMonitoringRequestEventDTO) eventList.get(QosMonitorConstants.RECEIVED_MONITORING_REQUEST_EVENT_POSITION);

			}

			if (eventList.get(QosMonitorConstants.STARTED_MONITORING_MEASUREMENT_EVENT_POSITION) != null) {

				temporalStartedMonitoringEvent = (StartedMonitoringMeasurementEventDTO) eventList.get(QosMonitorConstants.STARTED_MONITORING_MEASUREMENT_EVENT_POSITION);

			}

			if (eventList.get(QosMonitorConstants.FINISHED_MONITORING_MEASUREMENT_EVENT_POSITION) != null) {
				final FinishedMonitoringMeasurementEventDTO event = (FinishedMonitoringMeasurementEventDTO) eventList.get(QosMonitorConstants.FINISHED_MONITORING_MEASUREMENT_EVENT_POSITION);

				logAssistantEvents(temporalReceivedRequestEvent, temporalStartedMonitoringEvent, SUCCESS_FINISH_PHRASE, id.toString());
				logger.debug(id + " - external ping measurement process" + SUCCESS_FINISH_PHRASE + "with valid FINISHED_MONITORING_MEASUREMENT_EVENT: " + event.toString());

				eventBuffer.remove(id);
				return IcmpPingDTOConverter.convertPingMeasurementResult(event.getPayload());

			} else if (eventList.get(QosMonitorConstants.INTERRUPTED_MONITORING_MEASUREMENT_EVENT_POSITION) != null) {

				eventBuffer.remove(id);
				final InterruptedMonitoringMeasurementEventDTO event = (InterruptedMonitoringMeasurementEventDTO) eventList.get(QosMonitorConstants.INTERRUPTED_MONITORING_MEASUREMENT_EVENT_POSITION);

				logAssistantEvents(temporalReceivedRequestEvent, temporalStartedMonitoringEvent, INTERRRUPTED_FINISH_PHRASE, id.toString());

				throw new ArrowheadException(id + " - external ping measurement process get interrupted by : " + event.toString());

			}else{

				rest();
				continue;

			}

		}

		logAssistantEvents(temporalReceivedRequestEvent, temporalStartedMonitoringEvent, TIMEOUT_FINISH_PHRASE, id.toString());
		throw new ArrowheadException("Timeout on external ping measurement : " + id.toString());
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
	private void logAssistantEvents(final ReceivedMonitoringRequestEventDTO temporalReceivedRequestEvent,
									final StartedMonitoringMeasurementEventDTO temporalStartedMonitoringEvent,
									final String finishPhrase,
									final String id) {

		if (temporalReceivedRequestEvent != null) {
			logger.debug(id + " - external ping measurement process" + finishPhrase + "with valid RECEIVED_MONITORING_REQUEST_EVENT: " + temporalReceivedRequestEvent.toString());
		}else {
			logger.debug(id + " - external ping measurement process" + finishPhrase + "without valid RECEIVED_MONITORING_REQUEST_EVENT. ");
		}

		if (temporalStartedMonitoringEvent != null) {
			logger.debug(id + " - external ping measurement process" + finishPhrase + "with valid STARTED_MONITORING_MEASUREMENT_EVENT: " + temporalStartedMonitoringEvent.toString());
		}else {
			logger.debug(id + " - external ping measurement process" + finishPhrase + "without valid STARTED_MONITORING_MEASUREMENT_EVENT. ");
		}
	}

}
