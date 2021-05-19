package eu.arrowhead.core.qos.dto.event;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.util.Assert;

import eu.arrowhead.common.dto.shared.IcmpPingResponseDTO;
import eu.arrowhead.core.qos.QosMonitorConstants;
import eu.arrowhead.core.qos.service.event.QosMonitorEventType;

public class FinishedMonitoringMeasurementEventDTO implements Serializable {

	//=================================================================================================
	// members

	private static final long serialVersionUID = -2664998954640775578L;

	private QosMonitorEventType eventType;
	private List<IcmpPingResponseDTO> payload;
	private Map<String,String> metaData;
	private ZonedDateTime timeStamp;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public FinishedMonitoringMeasurementEventDTO() {}

	//-------------------------------------------------------------------------------------------------
	public QosMonitorEventType getEventType() {	return eventType; }
	public List<IcmpPingResponseDTO> getPayload() { return payload; }
	public Map<String,String> getMetaData() { return metaData; }
	public ZonedDateTime getTimeStamp() { return timeStamp; }

	//-------------------------------------------------------------------------------------------------
	public void setEventType(final QosMonitorEventType eventType) { this.eventType = eventType; }
	public void setPayload(final List<IcmpPingResponseDTO> payload) { this.payload = payload; }
	public void setMetaData( final Map<String,String> metaData ) { this.metaData = metaData; }
	public void setTimeStamp(final ZonedDateTime timeStamp) { this.timeStamp = timeStamp; }

	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	@PostConstruct
	private void validateFields() {

		Assert.isTrue(eventType.equals(QosMonitorEventType.FINISHED_MONITORING_MEASUREMENT), "Event type must be: FINISHED_MONITORING_MEASUREMENT");
		Assert.isTrue(metaData.containsKey(QosMonitorConstants.PROCESS_ID_KEY), "Meta data must contain: " + QosMonitorConstants.PROCESS_ID_KEY);
		Assert.isTrue(metaData.keySet().size() == QosMonitorConstants.FINISHED_MONITORING_MEASUREMENT_EVENT_PAYLOAD_METADATA_SIZE, "Meta data keys quantity is not valid");
	}
}
