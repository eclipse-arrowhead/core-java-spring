package eu.arrowhead.core.qos.dto.event.monitoringevents;

import java.util.List;

import eu.arrowhead.common.dto.shared.IcmpPingResponseDTO;
import eu.arrowhead.common.dto.shared.QosMonitorEventType;

public class FinishedMonitoringMeasurementEventDTO extends MeasurementMonitoringEvent {

	//=================================================================================================
	// members

	private static final long serialVersionUID = -2664998954640775578L;

	private List<IcmpPingResponseDTO> payload;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public FinishedMonitoringMeasurementEventDTO() {
		this.eventType = QosMonitorEventType.FINISHED_MONITORING_MEASUREMENT;
	}

	//-------------------------------------------------------------------------------------------------

	public List<IcmpPingResponseDTO> getPayload() { return payload; }

	//-------------------------------------------------------------------------------------------------
	public void setPayload(final List<IcmpPingResponseDTO> payload) { this.payload = payload; }

}
