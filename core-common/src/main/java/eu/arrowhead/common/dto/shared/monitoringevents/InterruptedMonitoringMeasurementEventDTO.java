package eu.arrowhead.common.dto.shared.monitoringevents;

import java.io.Serializable;

import eu.arrowhead.common.dto.shared.QosMonitorEventType;

public class InterruptedMonitoringMeasurementEventDTO extends MeasurementMonitoringEvent implements Serializable{

	//=================================================================================================
	// members

	private static final long serialVersionUID = 1971997139168881765L;

	private String payload;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public InterruptedMonitoringMeasurementEventDTO() {
		this.eventType = QosMonitorEventType.INTERUPTED_MONITORING_MEASUREMENT;
	}

	//-------------------------------------------------------------------------------------------------
	public String getPayload() { return payload; }

	//-------------------------------------------------------------------------------------------------
	public void setPayload(final String payload) { this.payload = payload; }

}
