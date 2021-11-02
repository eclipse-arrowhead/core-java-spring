/********************************************************************************
 * Copyright (c) 2020 AITIA
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   AITIA - implementation
 *   Arrowhead Consortia - conceptualization
 ********************************************************************************/

package eu.arrowhead.common.dto.internal;

import java.io.Serializable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.arrowhead.common.dto.shared.QoSMeasurementType;
import eu.arrowhead.common.dto.shared.SystemResponseDTO;

public class QoSIntraMeasurementResponseDTO implements Serializable {

	//=================================================================================================
	// members

	private static final long serialVersionUID = -8158447252910402563L;

	private long id;
	private SystemResponseDTO system;
	private QoSMeasurementType measurementType;
	private String lastMeasurementAt;
	private String createdAt;
	private String updatedAt;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public QoSIntraMeasurementResponseDTO() {}

	//-------------------------------------------------------------------------------------------------
	public QoSIntraMeasurementResponseDTO(final long id, final SystemResponseDTO system, final QoSMeasurementType measurementType, final String lastMeasurementAt, 
										  final String createdAt, final String updatedAt) {

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
	public String getLastMeasurementAt() { return lastMeasurementAt; }
	public String getCreatedAt() { return createdAt; }
	public String getUpdatedAt() { return updatedAt; }

	//-------------------------------------------------------------------------------------------------
	public void setId(final long id) { this.id = id; }
	public void setSystem(final SystemResponseDTO system) { this.system = system; }
	public void setMeasurementType(final QoSMeasurementType measurementType) { this.measurementType = measurementType; }
	public void setLastMeasurementAt(final String lastMeasurementAt) { this.lastMeasurementAt = lastMeasurementAt;	}
	public void setCreatedAt(final String createdAt) { this.createdAt = createdAt; }
	public void setUpdatedAt(final String updatedAt) { this.updatedAt = updatedAt; }

	//-------------------------------------------------------------------------------------------------
	@Override
	public String toString() {
		try {
			return new ObjectMapper().writeValueAsString(this);
		} catch (final JsonProcessingException ex) {
			return "toString failure";
		}
	}
}