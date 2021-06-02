package eu.arrowhead.core.qos.dto.event.monitoringevents;

import java.io.Serializable;

import eu.arrowhead.common.dto.shared.QosMonitorEventType;

public class ReceivedMonitoringRequestEventDTO extends MeasurementMonitoringEvent implements Serializable{

	//=================================================================================================
	// members

	private static final long serialVersionUID = -2664998954640775578L;

	private String payload;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public ReceivedMonitoringRequestEventDTO() {
		this.eventType = QosMonitorEventType.RECEIVED_MONITORING_REQUEST;
	}

	//-------------------------------------------------------------------------------------------------
	public String getPayload() { return payload; }

	//-------------------------------------------------------------------------------------------------
	public void setPayload(final String payload) { this.payload = payload; }

}
