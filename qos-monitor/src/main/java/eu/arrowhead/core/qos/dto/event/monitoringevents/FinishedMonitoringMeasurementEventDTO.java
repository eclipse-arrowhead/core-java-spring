package eu.arrowhead.core.qos.dto.event.monitoringevents;

import java.util.List;
import java.util.StringJoiner;

import org.apache.commons.lang3.StringUtils;

import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.shared.IcmpPingResponseDTO;
import eu.arrowhead.common.dto.shared.QosMonitorEventType;
import eu.arrowhead.core.qos.QosMonitorConstants;

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

	//-------------------------------------------------------------------------------------------------
	@Override
	public String toString() {
		return new StringJoiner(", ", FinishedMonitoringMeasurementEventDTO.class.getSimpleName() + "[", "]")
				.add("event type = " + getEventType().name())
				.add("timeStamp = " + Utilities.convertZonedDateTimeToUTCString(getTimeStamp()))
				.add("processID = " + 
						getMetadata().get(QosMonitorConstants.PROCESS_ID_KEY) != null ? getMetadata().get(QosMonitorConstants.PROCESS_ID_KEY) : " - ")
				.add("payload = " + StringUtils.join(payload, "|"))
				.toString();
	}
}
