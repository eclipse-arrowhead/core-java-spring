package eu.arrowhead.common.dto.shared.monitoringevents;

import java.io.Serializable;

import eu.arrowhead.common.dto.shared.QosMonitorEventType;

public class StartedMonitoringMeasurementEventDTO extends MeasurementMonitoringEvent implements Serializable {

	//=================================================================================================
	// members

	private static final long serialVersionUID = -5448189680057980316L;

	private String payload;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public StartedMonitoringMeasurementEventDTO() {
		this.eventType = QosMonitorEventType.STARTED_MONITORING_MEASUREMENT;
	}

	//-------------------------------------------------------------------------------------------------
	public String getPayload() { return payload; }

	//-------------------------------------------------------------------------------------------------
	public void setPayload(final String payload) { this.payload = payload; }

}
