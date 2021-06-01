package eu.arrowhead.common.dto.shared.monitoringevents;

import java.time.ZonedDateTime;
import java.util.Map;

import eu.arrowhead.common.dto.shared.QosMonitorEventType;

public abstract class MeasurementMonitoringEvent {


	//=================================================================================================
	// members

	protected QosMonitorEventType eventType;
	private Map<String,String> metadata;
	private ZonedDateTime timeStamp;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public QosMonitorEventType getEventType() {	return eventType; }
	public Map<String,String> getMetadata() { return metadata; }
	public ZonedDateTime getTimeStamp() { return timeStamp; }

	//-------------------------------------------------------------------------------------------------
	public void setMetadata( final Map<String,String> metadata ) { this.metadata = metadata; }
	public void setTimeStamp(final ZonedDateTime timeStamp) { this.timeStamp = timeStamp; }


}
