package eu.arrowhead.core.qos.dto.event.monitoringevents;

import eu.arrowhead.common.dto.shared.QosMonitorEventType;

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

}
