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
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(name = "qos_reservation", uniqueConstraints = @UniqueConstraint(columnNames = { "reservedProviderId", "reservedServiceId" }) )
public class QoSReservation {
	
	//=================================================================================================
	// members
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	private long reservedProviderId;
	private long reservedServiceId;
	
	private String consumerSystemName;
	private String consumerAddress;
	private int consumerPort;
	
	private ZonedDateTime reservedTo;
	
	private boolean temporaryLock = false;

	@Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
	private ZonedDateTime createdAt;
	
	@Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
	private ZonedDateTime updatedAt;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public QoSReservation() {}
	
	//-------------------------------------------------------------------------------------------------
	public QoSReservation(final long reservedProviderId, final long reservedServiceId, final String consumerSystemName, final String consumerAddress, final int consumerPort,
						  final ZonedDateTime reservedTo, final boolean temporaryLock) {
		this.reservedProviderId = reservedProviderId;
		this.reservedServiceId = reservedServiceId;
		this.consumerSystemName = consumerSystemName;
		this.consumerAddress = consumerAddress;
		this.consumerPort = consumerPort;
		this.reservedTo = reservedTo;
		this.temporaryLock = temporaryLock;
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
	public long getReservedProviderId() { return reservedProviderId; }
	public long getReservedServiceId() { return reservedServiceId; }
	public String getConsumerSystemName() { return consumerSystemName; }
	public String getConsumerAddress() { return consumerAddress; }
	public int getConsumerPort() { return consumerPort; }
	public ZonedDateTime getReservedTo() { return reservedTo; }
	public boolean isTemporaryLock() { return temporaryLock; }
	public ZonedDateTime getCreatedAt() { return createdAt; }
	public ZonedDateTime getUpdatedAt() { return updatedAt; }

	//-------------------------------------------------------------------------------------------------
	public void setId(final long id) { this.id = id; }
	public void setReservedProviderId(final long reservedProviderId) { this.reservedProviderId = reservedProviderId; }
	public void setReservedServiceId(final int reservedServiceId) { this.reservedServiceId = reservedServiceId; }
	public void setConsumerSystemName(final String consumerSystemName) { this.consumerSystemName = consumerSystemName; }
	public void setConsumerAddress(final String consumerAddress) { this.consumerAddress = consumerAddress; }
	public void setConsumerPort(final int consumerPort) { this.consumerPort = consumerPort; }
	public void setReservedTo(final ZonedDateTime reservedTo) { this.reservedTo = reservedTo; }
	public void setTemporaryLock(final boolean temporaryLock) { this.temporaryLock = temporaryLock; }
	public void setCreatedAt(final ZonedDateTime createdAt) { this.createdAt = createdAt; }
	public void setUpdatedAt(final ZonedDateTime updatedAt) { this.updatedAt = updatedAt; }
}