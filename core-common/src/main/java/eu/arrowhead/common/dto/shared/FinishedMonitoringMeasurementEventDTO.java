package eu.arrowhead.common.dto.shared;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

public class FinishedMonitoringMeasurementEventDTO implements Serializable {

	//=================================================================================================
	// members

	private static final long serialVersionUID = -2664998954640775578L;

	private QosMonitorEventType eventType;
	private List<IcmpPingResponseDTO> payload;
	private Map<String,String> metadata;
	private ZonedDateTime timeStamp;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public FinishedMonitoringMeasurementEventDTO() {}

	//-------------------------------------------------------------------------------------------------
	public QosMonitorEventType getEventType() {	return eventType; }
	public List<IcmpPingResponseDTO> getPayload() { return payload; }
	public Map<String,String> getMetadata() { return metadata; }
	public ZonedDateTime getTimeStamp() { return timeStamp; }

	//-------------------------------------------------------------------------------------------------
	public void setEventType(final QosMonitorEventType eventType) { this.eventType = eventType; }
	public void setPayload(final List<IcmpPingResponseDTO> payload) { this.payload = payload; }
	public void setMetadata( final Map<String,String> metadata ) { this.metadata = metadata; }
	public void setTimeStamp(final ZonedDateTime timeStamp) { this.timeStamp = timeStamp; }

}
