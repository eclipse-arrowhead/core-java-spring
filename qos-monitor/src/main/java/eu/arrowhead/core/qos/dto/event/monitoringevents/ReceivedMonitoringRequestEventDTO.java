package eu.arrowhead.core.qos.dto.event.monitoringevents;

import java.util.StringJoiner;

import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.shared.QosMonitorEventType;
import eu.arrowhead.core.qos.QosMonitorConstants;

public class ReceivedMonitoringRequestEventDTO extends MeasurementMonitoringEvent {

	//=================================================================================================
	// members

	private static final long serialVersionUID = -2664998954640775578L;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public ReceivedMonitoringRequestEventDTO() {
		this.eventType = QosMonitorEventType.RECEIVED_MONITORING_REQUEST;
	}

	//-------------------------------------------------------------------------------------------------
	@Override
	public String toString() {
		return new StringJoiner(", ", ReceivedMonitoringRequestEventDTO.class.getSimpleName() + "[", "]")
				.add("event type = " + getEventType().name())
				.add("timeStamp = " + Utilities.convertZonedDateTimeToUTCString(getTimeStamp()))
				.add("processID = " + 
						getMetadata().get(QosMonitorConstants.PROCESS_ID_KEY) != null ? getMetadata().get(QosMonitorConstants.PROCESS_ID_KEY) : " - ")
				.toString();
	}
}
