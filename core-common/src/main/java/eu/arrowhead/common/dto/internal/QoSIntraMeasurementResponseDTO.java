package eu.arrowhead.common.dto.internal;

import java.io.Serializable;
import java.time.ZonedDateTime;

import eu.arrowhead.common.dto.shared.QoSMeasurementType;
import eu.arrowhead.common.dto.shared.SystemResponseDTO;

public class QoSIntraMeasurementResponseDTO implements Serializable {

	//=================================================================================================
	// members

	private static final long serialVersionUID = -8158447252910402563L;

	private long id;
	private SystemResponseDTO system;
	private QoSMeasurementType measurementType;
	private ZonedDateTime lastMeasurementAt;
	private ZonedDateTime createdAt;
	private ZonedDateTime updatedAt;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public QoSIntraMeasurementResponseDTO() {}

	//-------------------------------------------------------------------------------------------------
	public QoSIntraMeasurementResponseDTO(final long id, final SystemResponseDTO system, final QoSMeasurementType measurementType, final ZonedDateTime lastMeasurementAt, 
										  final ZonedDateTime createdAt, final ZonedDateTime updatedAt) {

		this.id = id;
		this.system = system;
		this.measurementType = measurementType;
		this.lastMeasurementAt = lastMeasurementAt;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	//-------------------------------------------------------------------------------------------------
	public long getId() { return id; }
	public SystemResponseDTO getSystem() { return system; }
	public QoSMeasurementType getMeasurementType() { return measurementType; }
	public ZonedDateTime getLastMeasurementAt() { return lastMeasurementAt; }
	public ZonedDateTime getCreatedAt() { return createdAt; }
	public ZonedDateTime getUpdatedAt() { return updatedAt; }

	//-------------------------------------------------------------------------------------------------
	public void setId(final long id) { this.id = id; }
	public void setSystem(final SystemResponseDTO system) { this.system = system; }
	public void setMeasurementType(final QoSMeasurementType measurementType) { this.measurementType = measurementType; }
	public void setLastMeasurementAt(final ZonedDateTime lastMeasurementAt) { this.lastMeasurementAt = lastMeasurementAt;	}
	public void setCreatedAt(final ZonedDateTime createdAt) { this.createdAt = createdAt; }
	public void setUpdatedAt(final ZonedDateTime updatedAt) { this.updatedAt = updatedAt; }

}
