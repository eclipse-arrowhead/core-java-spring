package eu.arrowhead.core.qos.dto.event;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.springframework.util.Assert;

import eu.arrowhead.core.qos.service.event.QosMonitorEventType;

public class StartedMonitoringMeasurementEventDTO implements Serializable {

	//=================================================================================================
	// members

	private static final long serialVersionUID = -5448189680057980316L;

	private QosMonitorEventType eventType;
	private String payload;
	private Map<String, UUID> metaData;
	private ZonedDateTime timeStamp;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public StartedMonitoringMeasurementEventDTO() {}

	//-------------------------------------------------------------------------------------------------
	public QosMonitorEventType getEventType() {	return eventType; }
	public String getPayload() { return payload; }
	public Map<String, UUID> getMetaData() { return metaData; }
	public ZonedDateTime getTimeStamp() { return timeStamp; }

	//-------------------------------------------------------------------------------------------------
	public void setEventType(final QosMonitorEventType eventType) { this.eventType = eventType; }
	public void setPayload(final String payload) { this.payload = payload; }
	public void setMetaData( final Map<String,UUID> metaData ) { this.metaData = metaData; }
	public void setTimeStamp(final ZonedDateTime timeStamp) { this.timeStamp = timeStamp; }

	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	@PostConstruct
	private void validateFields() {

		Assert.isTrue(eventType.equals(QosMonitorEventType.STARTED_MONITORING_MEASUREMENT), "Event type must be: STARTED_MONITORING_MEASUREMENT");
		Assert.isTrue(payload.equalsIgnoreCase("STARTED"), "Payload must be: STARTED");
		Assert.isTrue(metaData.containsKey("processID"), "Meta data must contain: processID");
		Assert.isTrue(metaData.keySet().size() == 1, "Meta data must contain a single key");
	}

}
