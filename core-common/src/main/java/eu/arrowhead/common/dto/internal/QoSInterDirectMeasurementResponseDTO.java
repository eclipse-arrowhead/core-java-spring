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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.arrowhead.common.dto.shared.QoSMeasurementType;

public class QoSInterDirectMeasurementResponseDTO implements Serializable {
	
	//=================================================================================================
	// members
	
	private static final long serialVersionUID = 6034649772909565335L;
	
	private Long id;
	private CloudResponseDTO cloud;
	private String address;
	private QoSMeasurementType measurementType;
	private String lastMeasurementAt;
	private String createdAt;
	private String updatedAt;
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public QoSInterDirectMeasurementResponseDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public QoSInterDirectMeasurementResponseDTO(final Long id, final CloudResponseDTO cloud, final String address, final QoSMeasurementType measurementType,
												final String lastMeasurementAt, final String createdAt, final String updatedAt) {
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
	public String getLastMeasurementAt() { return lastMeasurementAt; }
	public String getCreatedAt() { return createdAt; }
	public String getUpdatedAt() { return updatedAt; }

	//-------------------------------------------------------------------------------------------------
	public void setId(final Long id) { this.id = id; }
	public void setCloud(final CloudResponseDTO cloud) { this.cloud = cloud; }
	public void setAddress(final String address) { this.address = address; }
	public void setMeasurementType(final QoSMeasurementType measurementType) { this.measurementType = measurementType; }
	public void setLastMeasurementAt(final String lastMeasurementAt) { this.lastMeasurementAt = lastMeasurementAt; }
	public void setCreatedAt(final String createdAt) { this.createdAt = createdAt; }
	public void setUpdatedAt(final String updatedAt) { this.updatedAt = updatedAt; }
	
	//-------------------------------------------------------------------------------------------------
	@JsonIgnore
	public boolean hasRecord() {
		return id != null;
	}
	
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