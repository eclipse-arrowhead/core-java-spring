package eu.arrowhead.common.dto.internal;

import java.io.Serializable;
import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.arrowhead.common.dto.shared.QoSMeasurementType;

public class QoSInterDirectMeasurementResponseDTO implements Serializable {
	
	//=================================================================================================
	// members
	
	private static final long serialVersionUID = 6034649772909565335L;
	
	private Long id;
	private CloudResponseDTO cloud;
	private String address;
	private QoSMeasurementType measurementType;
	private ZonedDateTime lastMeasurementAt;
	private ZonedDateTime createdAt;
	private ZonedDateTime updatedAt;
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public QoSInterDirectMeasurementResponseDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public QoSInterDirectMeasurementResponseDTO(final Long id, final CloudResponseDTO cloud, final String address, final QoSMeasurementType measurementType,
												final ZonedDateTime lastMeasurementAt, final ZonedDateTime createdAt, final ZonedDateTime updatedAt) {
		this.id = id;
		this.cloud = cloud;
		this.address = address;
		this.measurementType = measurementType;
		this.lastMeasurementAt = lastMeasurementAt;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	//-------------------------------------------------------------------------------------------------
	public Long getId() { return id; }
	public CloudResponseDTO getCloud() { return cloud; }
	public String getAddress() { return address; }
	public QoSMeasurementType getMeasurementType() { return measurementType; }
	public ZonedDateTime getLastMeasurementAt() { return lastMeasurementAt; }
	public ZonedDateTime getCreatedAt() { return createdAt; }
	public ZonedDateTime getUpdatedAt() { return updatedAt; }

	//-------------------------------------------------------------------------------------------------
	public void setId(final Long id) { this.id = id; }
	public void setCloud(final CloudResponseDTO cloud) { this.cloud = cloud; }
	public void setAddress(final String address) { this.address = address; }
	public void setMeasurementType(final QoSMeasurementType measurementType) { this.measurementType = measurementType; }
	public void setLastMeasurementAt(final ZonedDateTime lastMeasurementAt) { this.lastMeasurementAt = lastMeasurementAt; }
	public void setCreatedAt(final ZonedDateTime createdAt) { this.createdAt = createdAt; }
	public void setUpdatedAt(final ZonedDateTime updatedAt) { this.updatedAt = updatedAt; }
	
	//-------------------------------------------------------------------------------------------------
	@JsonIgnore
	public boolean hasRecord() {
		return id != null;
	}
}
