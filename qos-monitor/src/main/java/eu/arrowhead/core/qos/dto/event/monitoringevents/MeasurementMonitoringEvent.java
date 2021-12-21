package eu.arrowhead.core.qos.dto.event.monitoringevents;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Map;

import eu.arrowhead.common.dto.shared.QosMonitorEventType;

@SuppressWarnings("serial")
public abstract class MeasurementMonitoringEvent implements Serializable {

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
