package eu.arrowhead.core.qos.dto.event;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.springframework.util.Assert;

import eu.arrowhead.core.qos.dto.IcmpPingRequest;
import eu.arrowhead.core.qos.service.event.QosMonitorEventType;

public class FinishedMonitoringMeasurementEventDTO implements Serializable {

	//=================================================================================================
	// members

	private static final long serialVersionUID = -2664998954640775578L;

	private QosMonitorEventType eventType;
	private List<IcmpPingRequest> payload;
	private Map<String,UUID> metaData;
	private ZonedDateTime timeStamp;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public FinishedMonitoringMeasurementEventDTO() {}

	//-------------------------------------------------------------------------------------------------
	public QosMonitorEventType getEventType() {	return eventType; }
	public List<IcmpPingRequest> getPayload() { return payload; }
	public Map<String,UUID> getMetaData() { return metaData; }
	public ZonedDateTime getTimeStamp() { return timeStamp; }

	//-------------------------------------------------------------------------------------------------
	public void setEventType(final QosMonitorEventType eventType) { this.eventType = eventType; }
	public void setPayload(final List<IcmpPingRequest> payload) { this.payload = payload; }
	public void setMetaData( final Map<String,UUID> metaData ) { this.metaData = metaData; }
	public void setTimeStamp(final ZonedDateTime timeStamp) { this.timeStamp = timeStamp; }

	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	@PostConstruct
	private void validateFields() {

		Assert.isTrue(eventType.equals(QosMonitorEventType.FINISHED_MONITORING_MEASUREMENT), "Event type must be: FINISHED_MONITORING_MEASUREMENT");
		Assert.isTrue(metaData.containsKey("processID"), "Meta data must contain: processID");
		Assert.isTrue(metaData.keySet().size() == 1, "Meta data must contain a single key");
	}
}
