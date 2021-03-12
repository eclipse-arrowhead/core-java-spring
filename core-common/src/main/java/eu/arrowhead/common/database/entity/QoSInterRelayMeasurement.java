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
import eu.arrowhead.common.dto.shared.QoSMeasurementStatus;
import eu.arrowhead.common.dto.shared.QoSMeasurementType;

@Entity
@Table(name = "qos_inter_relay_measurement", uniqueConstraints = @UniqueConstraint(columnNames = {"cloudId", "relayId", "measurementType"}))
public class QoSInterRelayMeasurement {

	//=================================================================================================
	// members
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "cloudId", referencedColumnName = "id", nullable = false)
	private Cloud cloud;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "relayId", referencedColumnName = "id", nullable = false)
	private Relay relay;
	
	@Column(nullable = false, columnDefinition = "varchar(" + CoreDefaults.VARCHAR_BASIC + ")")
	@Enumerated(EnumType.STRING)
	private QoSMeasurementType measurementType;	
	
	@Column(nullable = false, columnDefinition = "varchar(" + CoreDefaults.VARCHAR_BASIC + ")")
	@Enumerated(EnumType.STRING)
	private QoSMeasurementStatus status;	
	
	@Column(nullable = false)
	private ZonedDateTime lastMeasurementAt;
	
	@Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
	private ZonedDateTime createdAt;
	
	@Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
	private ZonedDateTime updatedAt;
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public QoSInterRelayMeasurement() {}
	
	//-------------------------------------------------------------------------------------------------
	public QoSInterRelayMeasurement(final Cloud cloud, final Relay relay, final QoSMeasurementType measurementType, final ZonedDateTime lastMeasurementAt) {
		this.cloud = cloud;
		this.relay = relay;
		this.measurementType = measurementType;
		this.lastMeasurementAt = lastMeasurementAt;
	}
	
	//-------------------------------------------------------------------------------------------------
	@PrePersist
	public void onCreate() {
		this.status = QoSMeasurementStatus.NEW;
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
	public Cloud getCloud() { return cloud; }
	public Relay getRelay() { return relay; }
	public QoSMeasurementType getMeasurementType() { return measurementType; }
	public QoSMeasurementStatus getStatus() { return status; }
	public ZonedDateTime getLastMeasurementAt() { return lastMeasurementAt; }
	public ZonedDateTime getCreatedAt() { return createdAt; }
	public ZonedDateTime getUpdatedAt() { return updatedAt; }

	//-------------------------------------------------------------------------------------------------
	public void setId(final long id) { this.id = id; }
	public void setCloud(final Cloud cloud) { this.cloud = cloud; }
	public void setRelay(final Relay relay) { this.relay = relay; }
	public void setMeasurementType(final QoSMeasurementType measurementType) { this.measurementType = measurementType; }
	public void setStatus(final QoSMeasurementStatus status) { this.status = status; }
	public void setLastMeasurementAt(final ZonedDateTime lastMeasurementAt) { this.lastMeasurementAt = lastMeasurementAt; }
	public void setCreatedAt(final ZonedDateTime createdAt) { this.createdAt = createdAt; }
	public void setUpdatedAt(final ZonedDateTime updatedAt) { this.updatedAt = updatedAt; }	
}
