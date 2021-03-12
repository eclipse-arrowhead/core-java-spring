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

package eu.arrowhead.common.database.entity;

import java.time.ZonedDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import eu.arrowhead.common.CoreDefaults;
import eu.arrowhead.common.dto.shared.QoSMeasurementType;

@Entity
@Table(name = "qos_inter_direct_measurement", uniqueConstraints = @UniqueConstraint(columnNames = {"cloudId", "address", "measurementType"}))
public class QoSInterDirectMeasurement {
	
	//=================================================================================================
	// members
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "cloudId", referencedColumnName = "id", nullable = false)
	private Cloud cloud;
	
	@Column(nullable = false)
	private String address;
	
	@Column(nullable = false, columnDefinition = "varchar(" + CoreDefaults.VARCHAR_BASIC + ")")
	@Enumerated(EnumType.STRING)
	private QoSMeasurementType measurementType;
	
	@Column(nullable = false)
	private ZonedDateTime lastMeasurementAt;
	
	@Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
	private ZonedDateTime createdAt;
	
	@Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
	private ZonedDateTime updatedAt;
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public QoSInterDirectMeasurement() {}
	
	//-------------------------------------------------------------------------------------------------
	public QoSInterDirectMeasurement(final Cloud cloud, final String address, final QoSMeasurementType measurementType, final ZonedDateTime lastMeasurementAt) {
		this.cloud = cloud;
		this.address = address;
		this.measurementType = measurementType;
		this.lastMeasurementAt = lastMeasurementAt;
	}
	
	//-------------------------------------------------------------------------------------------------
	@PrePersist
	public void onCreate() {
		this.createdAt = ZonedDateTime.now();
		this.updatedAt = this.createdAt;
	}
	
	//-------------------------------------------------------------------------------------------------
	@PreUpdate
	public void onUpdate() {
		this.updatedAt = ZonedDateTime.now();
	}

	//-------------------------------------------------------------------------------------------------
	public long getId() { return id; }
	public Cloud getCloud() {return cloud; }
	public String getAddress() { return address; }
	public QoSMeasurementType getMeasurementType() { return measurementType; }
	public ZonedDateTime getLastMeasurementAt() { return lastMeasurementAt; }
	public ZonedDateTime getCreatedAt() { return createdAt; }
	public ZonedDateTime getUpdatedAt() { return updatedAt; }

	//-------------------------------------------------------------------------------------------------
	public void setId(final long id) { this.id = id; }
	public void setCloud(final Cloud cloud) { this.cloud = cloud; }
	public void setAddress(final String address) { this.address = address; }
	public void setMeasurementType(final QoSMeasurementType measurementType) { this.measurementType = measurementType; }
	public void setLastMeasurementAt(final ZonedDateTime lastMeasurementAt) { this.lastMeasurementAt = lastMeasurementAt; }
	public void setCreatedAt(final ZonedDateTime createdAt) { this.createdAt = createdAt; }
	public void setUpdatedAt(final ZonedDateTime updatedAt) { this.updatedAt = updatedAt; }
}
