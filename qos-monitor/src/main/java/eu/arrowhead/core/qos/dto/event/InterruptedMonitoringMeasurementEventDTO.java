package eu.arrowhead.core.qos.dto.event;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.springframework.util.Assert;

import eu.arrowhead.core.qos.QosMonitorConstants;
import eu.arrowhead.core.qos.service.event.QosMonitorEventType;

public class InterruptedMonitoringMeasurementEventDTO implements Serializable{

	//=================================================================================================
	// members

	private static final long serialVersionUID = 1971997139168881765L;


	private QosMonitorEventType eventType;
	private String payload;
	private Map<String, String> metaData;
	private ZonedDateTime timeStamp;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public InterruptedMonitoringMeasurementEventDTO() {}

	//-------------------------------------------------------------------------------------------------
	public QosMonitorEventType getEventType() {	return eventType; }
	public String getPayload() { return payload; }
	public Map<String, String> getMetaData() { return metaData; }
	public ZonedDateTime getTimeStamp() { return timeStamp; }

	//-------------------------------------------------------------------------------------------------
	public void setEventType(final QosMonitorEventType eventType) { this.eventType = eventType; }
	public void setPayload(final String payload) { this.payload = payload; }
	public void setMetaData( final Map<String,String> metaData ) { this.metaData = metaData; }
	public void setTimeStamp(final ZonedDateTime timeStamp) { this.timeStamp = timeStamp; }

	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	@PostConstruct
	private void validateFields() {

		Assert.isTrue(eventType.equals(QosMonitorEventType.INTERUPTED_MONITORING_MEASUREMENT), "Event type must be: INTERUPTED_MONITORING_MEASUREMENT");
		Assert.isTrue(payload.equalsIgnoreCase(QosMonitorConstants.INTERRUPTED_MONITORING_MEASUREMENT_EVENT_PAYLOAD_SCHEMA), "Payload must be: " + QosMonitorConstants.INTERRUPTED_MONITORING_MEASUREMENT_EVENT_PAYLOAD_SCHEMA);
		Assert.isTrue(metaData.containsKey(QosMonitorConstants.PROCESS_ID_KEY), "Meta data must contain: " + QosMonitorConstants.PROCESS_ID_KEY);
		Assert.isTrue( (metaData.keySet().size() < QosMonitorConstants.INTERRUPTED_MONITORING_MEASUREMENT_EVENT_PAYLOAD_METADATA_MAX_SIZE + 1), "Meta data keys quantity is not valid");
	}
}
