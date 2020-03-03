package eu.arrowhead.common.dto.internal;

import java.io.Serializable;
import java.time.ZonedDateTime;

import eu.arrowhead.common.dto.shared.QoSMeasurementStatus;
import eu.arrowhead.common.dto.shared.QoSMeasurementType;

public class QoSInterRelayMeasurementResponseDTO implements Serializable {
	
	//=================================================================================================
	// members
	
	private static final long serialVersionUID = 7895501675620501224L;
	
	private long id;
	private CloudResponseDTO cloud;
	private RelayResponseDTO relay;
	private QoSMeasurementType measurementType;
	private QoSMeasurementStatus status;
	private ZonedDateTime lastMeasurementAt;
	private ZonedDateTime createdAt;
	private ZonedDateTime updatedAt;
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public QoSInterRelayMeasurementResponseDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public QoSInterRelayMeasurementResponseDTO(final long id, final CloudResponseDTO cloud, final RelayResponseDTO relay, final QoSMeasurementType measurementType, final QoSMeasurementStatus status,
											   final ZonedDateTime lastMeasurementAt,
											   final ZonedDateTime createdAt, final ZonedDateTime updatedAt) {
		this.id = id;
		this.cloud = cloud;
		this.relay = relay;
		this.measurementType = measurementType;
		this.status = status;
		this.lastMeasurementAt = lastMeasurementAt;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	//-------------------------------------------------------------------------------------------------
	public long getId() { return id; }
	public CloudResponseDTO getCloud() { return cloud; }
	public RelayResponseDTO getRelay() { return relay; }
	public QoSMeasurementType getMeasurementType() { return measurementType; }
	public QoSMeasurementStatus getStatus() { return status; }
	public ZonedDateTime getLastMeasurementAt() { return lastMeasurementAt; }
	public ZonedDateTime getCreatedAt() { return createdAt; }
	public ZonedDateTime getUpdatedAt() { return updatedAt; }

	//-------------------------------------------------------------------------------------------------
	public void setId(final long id) { this.id = id; }
	public void setCloud(final CloudResponseDTO cloud) { this.cloud = cloud; }
	public void setRelay(final RelayResponseDTO relay) { this.relay = relay; }
	public void setMeasurementType(final QoSMeasurementType measurementType) { this.measurementType = measurementType; } 
	public void setStatus(final QoSMeasurementStatus status) { this.status = status; }
	public void setLastMeasurementAt(final ZonedDateTime lastMeasurementAt) { this.lastMeasurementAt = lastMeasurementAt; }
	public void setCreatedAt(final ZonedDateTime createdAt) { this.createdAt = createdAt; }
	public void setUpdatedAt(final ZonedDateTime updatedAt) { this.updatedAt = updatedAt; }	
}
