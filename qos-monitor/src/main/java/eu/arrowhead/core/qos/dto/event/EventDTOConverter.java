package eu.arrowhead.core.qos.dto.event;

import java.io.IOException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.Assert;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.shared.EventDTO;
import eu.arrowhead.common.dto.shared.IcmpPingResponseDTO;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.core.qos.service.event.QosMonitorEventType;

public class EventDTOConverter {

	//=================================================================================================
	// members

	private static final Logger logger = LogManager.getLogger(EventDTOConverter.class);

	private static final ObjectMapper mapper = new ObjectMapper();

	static {
		mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
	}

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public static InterruptedMonitoringMeasurementEventDTO convertToInteruptedMonitoringMeasurementEvent(final EventDTO event) {
		logger.debug("convertToInteruptedMonitoringMeasurementEvent started...");

		Assert.isTrue(!Utilities.isEmpty(event.getPayload()), "Event payload is empty");
		Assert.isTrue(!Utilities.isEmpty(event.getTimeStamp()), "Event timeStamp is empty");

		final InterruptedMonitoringMeasurementEventDTO validEvent = new InterruptedMonitoringMeasurementEventDTO();
		validEvent.setEventType(QosMonitorEventType.INTERUPTED_MONITORING_MEASUREMENT);
		validEvent.setMetaData(event.getMetaData());
		validEvent.setPayload(event.getPayload());
		validEvent.setTimeStamp(Utilities.parseUTCStringToLocalZonedDateTime(event.getTimeStamp()));

		return validEvent;
	}

	//-------------------------------------------------------------------------------------------------
	public static FinishedMonitoringMeasurementEventDTO convertToFinishedMonitoringMeasurementEvent(final EventDTO event) {
		logger.debug("convertToFinishedMonitoringMeasurementEvent started...");

		Assert.isTrue(!Utilities.isEmpty(event.getPayload()), "Event payload is empty");
		Assert.isTrue(!Utilities.isEmpty(event.getTimeStamp()), "Event timeStamp is empty");

		final FinishedMonitoringMeasurementEventDTO validEvent = new FinishedMonitoringMeasurementEventDTO();
		validEvent.setEventType(QosMonitorEventType.FINISHED_MONITORING_MEASUREMENT);
		validEvent.setMetaData(event.getMetaData());
		validEvent.setPayload(convertToIcmpPingResponse(event.getPayload()));
		validEvent.setTimeStamp(Utilities.parseUTCStringToLocalZonedDateTime(event.getTimeStamp()));

		return validEvent;
	}

	//-------------------------------------------------------------------------------------------------
	public static List<IcmpPingResponseDTO> convertToIcmpPingResponse(final String payload) {
		logger.debug("convertToIcmpPingResponse started...");

		Assert.isTrue(!Utilities.isEmpty(payload), "Payload is empty");

		try {
			final List<IcmpPingResponseDTO> validResponse = mapper.readValue(payload, new TypeReference<List<IcmpPingResponseDTO>>(){});

			return validResponse;

		} catch (final IOException e) {

			throw new InvalidParameterException("Invalid IcmpPingResponse");
		}
	}

	//-------------------------------------------------------------------------------------------------
	public static StartedMonitoringMeasurementEventDTO convertToStartedMonitoringMeasurementEvent(final EventDTO event) {
		logger.debug("convertToStartedMonitoringMeasurementEvent started...");

		Assert.isTrue(!Utilities.isEmpty(event.getPayload()), "Event payload is empty");
		Assert.isTrue(!Utilities.isEmpty(event.getTimeStamp()), "Event timeStamp is empty");

		final StartedMonitoringMeasurementEventDTO validEvent = new StartedMonitoringMeasurementEventDTO();
		validEvent.setEventType(QosMonitorEventType.STARTED_MONITORING_MEASUREMENT);
		validEvent.setMetaData(event.getMetaData());
		validEvent.setPayload(event.getPayload());
		validEvent.setTimeStamp(Utilities.parseUTCStringToLocalZonedDateTime(event.getTimeStamp()));

		return validEvent;
	}

	//-------------------------------------------------------------------------------------------------
	public static ReceivedMonitoringRequestEventDTO convertToReceivedMonitoringRequestEvent(final EventDTO event) {
		logger.debug("convertToReceivedMonitoringRequestEvent started...");

		Assert.isTrue(!Utilities.isEmpty(event.getPayload()), "Event payload is empty");
		Assert.isTrue(!Utilities.isEmpty(event.getTimeStamp()), "Event timeStamp is empty");

		final ReceivedMonitoringRequestEventDTO validEvent = new ReceivedMonitoringRequestEventDTO();
		validEvent.setEventType(QosMonitorEventType.RECEIVED_MONITORING_REQUEST);
		validEvent.setMetaData(event.getMetaData());
		validEvent.setPayload(event.getPayload());
		validEvent.setTimeStamp(Utilities.parseUTCStringToLocalZonedDateTime(event.getTimeStamp()));

		return validEvent;
	}

	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private EventDTOConverter() {
		throw new UnsupportedOperationException();
	}
}
