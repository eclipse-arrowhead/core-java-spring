package eu.arrowhead.core.qos.dto.event.monitoringevents;

import java.util.StringJoiner;

import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.shared.QosMonitorEventType;
import eu.arrowhead.core.qos.QosMonitorConstants;

public class InterruptedMonitoringMeasurementEventDTO extends MeasurementMonitoringEvent {

	//=================================================================================================
	// members

	private static final long serialVersionUID = 1971997139168881765L;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public InterruptedMonitoringMeasurementEventDTO() {
		this.eventType = QosMonitorEventType.INTERRUPTED_MONITORING_MEASUREMENT;
	}

	//-------------------------------------------------------------------------------------------------
	@Override
	public String toString() {
		return new StringJoiner(", ", InterruptedMonitoringMeasurementEventDTO.class.getSimpleName() + "[", "]")
				.add("event type = " + getEventType().name())
				.add("timeStamp = " + Utilities.convertZonedDateTimeToUTCString(getTimeStamp()))
				.add("processID = " + 
						getMetadata().get(QosMonitorConstants.PROCESS_ID_KEY) != null ? getMetadata().get(QosMonitorConstants.PROCESS_ID_KEY) : " - ")
				.add("exception = " + 
						getMetadata().get(QosMonitorConstants.INTERRUPTED_MONITORING_MEASUREMENT_EVENT_PAYLOAD_METADATA_EXCEPTION_KEY) != null ? getMetadata().get(QosMonitorConstants.INTERRUPTED_MONITORING_MEASUREMENT_EVENT_PAYLOAD_METADATA_EXCEPTION_KEY) : " - ")
				.add("root cause = " + 
						getMetadata().get(QosMonitorConstants.INTERRUPTED_MONITORING_MEASUREMENT_EVENT_PAYLOAD_METADATA_ROOT_CAUSE_KEY) != null ? getMetadata().get(QosMonitorConstants.INTERRUPTED_MONITORING_MEASUREMENT_EVENT_PAYLOAD_METADATA_ROOT_CAUSE_KEY) : " - ")
				.toString();
	}
}
