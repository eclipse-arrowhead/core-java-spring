package eu.arrowhead.core.qos.dto.event;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.springframework.util.Assert;

import eu.arrowhead.core.qos.QosMonitorConstants;
import eu.arrowhead.core.qos.service.event.QosMonitorEventType;

public class ReceivedMonitoringRequestEventDTO implements Serializable{

	//=================================================================================================
	// members

	private static final long serialVersionUID = -2664998954640775578L;

	private QosMonitorEventType eventType;
	private String payload;
	private Map<String, String> metaData;
	private ZonedDateTime timeStamp;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public ReceivedMonitoringRequestEventDTO() {}

	//-------------------------------------------------------------------------------------------------
	public QosMonitorEventType getEventType() {	return eventType; }
	public String getPayload() { return payload; }
	public Map<String, String> getMetaData() { return metaData; }
	public ZonedDateTime getTimeStamp() { return timeStamp; }

	//-------------------------------------------------------------------------------------------------
	public void setEventType(final QosMonitorEventType eventType) { this.eventType = eventType; }
	public void setPayload(final String payload) { this.payload = payload; }
	public void setMetaData( final Map<String, String> metaData ) { this.metaData = metaData; }
	public void setTimeStamp(final ZonedDateTime timeStamp) { this.timeStamp = timeStamp; }

	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	@PostConstruct
	private void validateFields() {

		Assert.isTrue(eventType.equals(QosMonitorEventType.RECEIVED_MONITORING_REQUEST), "Event type must be: RECEIVED_MONITORING_REQUEST");
		Assert.isTrue(payload.equalsIgnoreCase(QosMonitorConstants.RECEIVED_MONITORING_REQUEST_EVENT_PAYLOAD_SCHEMA), "Payload must be: " + QosMonitorConstants.RECEIVED_MONITORING_REQUEST_EVENT_PAYLOAD_SCHEMA);
		Assert.isTrue(metaData.containsKey(QosMonitorConstants.PROCESS_ID_KEY), "Meta data must contain: " + QosMonitorConstants.PROCESS_ID_KEY);
		Assert.isTrue(metaData.keySet().size() == QosMonitorConstants.RECEIVED_MONITORING_REQUEST_EVENT_PAYLOAD_METADATA_SIZE, "Meta data keys quantity is not valid");
	}
}
