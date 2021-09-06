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
	private String lastMeasurementAt;
	private String createdAt;
	private String updatedAt;
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public QoSInterRelayMeasurementResponseDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public QoSInterRelayMeasurementResponseDTO(final long id, final CloudResponseDTO cloud, final RelayResponseDTO relay, final QoSMeasurementType measurementType, final QoSMeasurementStatus status,
											   final String lastMeasurementAt, final String createdAt, final String updatedAt) {
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
	public String getLastMeasurementAt() { return lastMeasurementAt; }
	public String getCreatedAt() { return createdAt; }
	public String getUpdatedAt() { return updatedAt; }

	//-------------------------------------------------------------------------------------------------
	public void setId(final long id) { this.id = id; }
	public void setCloud(final CloudResponseDTO cloud) { this.cloud = cloud; }
	public void setRelay(final RelayResponseDTO relay) { this.relay = relay; }
	public void setMeasurementType(final QoSMeasurementType measurementType) { this.measurementType = measurementType; } 
	public void setStatus(final QoSMeasurementStatus status) { this.status = status; }
	public void setLastMeasurementAt(final String lastMeasurementAt) { this.lastMeasurementAt = lastMeasurementAt; }
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