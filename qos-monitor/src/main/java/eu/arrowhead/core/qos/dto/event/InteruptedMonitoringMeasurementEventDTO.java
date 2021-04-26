package eu.arrowhead.core.qos.dto.event;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.springframework.util.Assert;

import eu.arrowhead.core.qos.service.event.QosMonitorEventType;

public class InteruptedMonitoringMeasurementEventDTO implements Serializable{

	//=================================================================================================
	// members

	private static final long serialVersionUID = 1971997139168881765L;


	private QosMonitorEventType eventType;
	private String payload;
	private Map<String, UUID> metaData;
	private ZonedDateTime timeStamp;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public InteruptedMonitoringMeasurementEventDTO() {}

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

		Assert.isTrue(eventType.equals(QosMonitorEventType.INTERUPTED_MONITORING_MEASUREMENT), "Event type must be: INTERUPTED_MONITORING_MEASUREMENT");
		Assert.isTrue(payload.equalsIgnoreCase("INTERUPTED"), "Payload must be: INTERUPTED");
		Assert.isTrue(metaData.containsKey("processID"), "Meta data must contain: processID");
	}
}
