package eu.arrowhead.common.dto.shared;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Map;

public class StartedMonitoringMeasurementEventDTO implements Serializable {

	//=================================================================================================
	// members

	private static final long serialVersionUID = -5448189680057980316L;

	private QosMonitorEventType eventType;
	private String payload;
	private Map<String, String> metadata;
	private ZonedDateTime timeStamp;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public StartedMonitoringMeasurementEventDTO() {}

	//-------------------------------------------------------------------------------------------------
	public QosMonitorEventType getEventType() {	return eventType; }
	public String getPayload() { return payload; }
	public Map<String, String> getMetadata() { return metadata; }
	public ZonedDateTime getTimeStamp() { return timeStamp; }

	//-------------------------------------------------------------------------------------------------
	public void setEventType(final QosMonitorEventType eventType) { this.eventType = eventType; }
	public void setPayload(final String payload) { this.payload = payload; }
	public void setMetadata( final Map<String,String> metadata ) { this.metadata = metadata; }
	public void setTimeStamp(final ZonedDateTime timeStamp) { this.timeStamp = timeStamp; }

}
