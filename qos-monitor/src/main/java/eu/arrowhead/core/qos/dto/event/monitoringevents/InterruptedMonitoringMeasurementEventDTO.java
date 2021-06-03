package eu.arrowhead.core.qos.dto.event.monitoringevents;

import java.io.Serializable;

import eu.arrowhead.common.dto.shared.QosMonitorEventType;

public class InterruptedMonitoringMeasurementEventDTO extends MeasurementMonitoringEvent implements Serializable{

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
