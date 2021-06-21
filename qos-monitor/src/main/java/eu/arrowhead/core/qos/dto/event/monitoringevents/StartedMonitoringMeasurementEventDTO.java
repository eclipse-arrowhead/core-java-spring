package eu.arrowhead.core.qos.dto.event.monitoringevents;

import java.util.StringJoiner;

import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.shared.QosMonitorEventType;
import eu.arrowhead.core.qos.QosMonitorConstants;

public class StartedMonitoringMeasurementEventDTO extends MeasurementMonitoringEvent {

	//=================================================================================================
	// members

	private static final long serialVersionUID = -5448189680057980316L;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public StartedMonitoringMeasurementEventDTO() {
		this.eventType = QosMonitorEventType.STARTED_MONITORING_MEASUREMENT;
	}

	//-------------------------------------------------------------------------------------------------
	@Override
	public String toString() {
		return new StringJoiner(", ", StartedMonitoringMeasurementEventDTO.class.getSimpleName() + "[", "]")
				.add("event type = " + getEventType().name())
				.add("timeStamp = " + Utilities.convertZonedDateTimeToUTCString(getTimeStamp()))
				.add("processID = " + 
						getMetadata().get(QosMonitorConstants.PROCESS_ID_KEY) != null ? getMetadata().get(QosMonitorConstants.PROCESS_ID_KEY) : " - ")
				.toString();
	}
}
