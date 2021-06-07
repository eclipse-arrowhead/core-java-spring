package eu.arrowhead.core.qos.dto.event.monitoringevents;

import eu.arrowhead.common.dto.shared.QosMonitorEventType;

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

}
