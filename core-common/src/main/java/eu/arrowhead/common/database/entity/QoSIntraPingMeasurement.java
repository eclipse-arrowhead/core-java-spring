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
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

@Entity
@Table(name = "qos_intra_ping_measurement")
public class QoSIntraPingMeasurement {

	//=================================================================================================
	// members

	public static final List<String> SORTABLE_FIELDS_BY = List.of( "id", "updatedAt", "createdAt"); //NOSONAR

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "measurementId", referencedColumnName = "id", nullable = false, unique = true)
	private QoSIntraMeasurement measurement;

	@Column(name = "available", nullable = false)
	private boolean available = false;

	@Column(name = "last_access_at", nullable = true)
	private ZonedDateTime lastAccessAt;

	@Column(name = "min_response_time", nullable = true)
	private Integer minResponseTime;

	@Column(name = "max_response_time", nullable = true)
	private Integer maxResponseTime;

	@Column(name = "mean_response_time_with_timeout", nullable = true)
	private Integer meanResponseTimeWithTimeout;

	@Column(name = "mean_response_time_without_timeout", nullable = true)
	private Integer meanResponseTimeWithoutTimeout;

	@Column(name = "jitter_with_timeout", nullable = true)
	private Integer jitterWithTimeout;

	@Column(name = "jitter_without_timeout", nullable = true)
	private Integer jitterWithoutTimeout;

	@Column(name = "lost_per_measurement_percent", nullable = false)
	private Integer lostPerMeasurementPercent;

	@Column(name = "sent", nullable = false)
	private long sent;

	@Column(name = "received", nullable = false)
	private long received;

	@Column(name = "count_started_at", nullable = true)
	private ZonedDateTime countStartedAt;

	@Column(name = "sent_all", nullable = false)
	private long sentAll;

	@Column(name = "received_all", nullable = false)
	private long receivedAll;

	@Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
	private ZonedDateTime createdAt;

	@Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
	private ZonedDateTime updatedAt;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public QoSIntraPingMeasurement() {}

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
	public QoSIntraMeasurement getMeasurement() { return measurement; }
	public boolean isAvailable() { return available; }
	public ZonedDateTime getLastAccessAt() { return lastAccessAt; }
	public Integer getMinResponseTime() { return minResponseTime; }
	public Integer getMaxResponseTime() { return maxResponseTime; }
	public Integer getMeanResponseTimeWithTimeout() { return meanResponseTimeWithTimeout; }
	public Integer getMeanResponseTimeWithoutTimeout() { return meanResponseTimeWithoutTimeout; }
	public Integer getJitterWithTimeout() {return jitterWithTimeout;}
	public Integer getJitterWithoutTimeout() { return jitterWithoutTimeout; }
	public Integer getLostPerMeasurementPercent() { return lostPerMeasurementPercent; }
	public long getSent() { return sent; }
	public long getReceived() { return received; }
	public ZonedDateTime getCountStartedAt() { return countStartedAt; }
	public long getSentAll() { return sentAll; }
	public long getReceivedAll() { return receivedAll; }
	public ZonedDateTime getCreatedAt() { return createdAt; }
	public ZonedDateTime getUpdatedAt() { return updatedAt; }

	//-------------------------------------------------------------------------------------------------
	public void setId(final long id) { this.id = id; }
	public void setMeasurement(final QoSIntraMeasurement measurement) { this.measurement = measurement; }
	public void setAvailable(final boolean available) { this.available = available; }
	public void setLastAccessAt(final ZonedDateTime lastAccessAt) { this.lastAccessAt = lastAccessAt; }
	public void setMinResponseTime(final Integer minResponseTime) { this.minResponseTime = minResponseTime; }
	public void setMaxResponseTime(final Integer maxResponseTime) { this.maxResponseTime = maxResponseTime; }
	public void setMeanResponseTimeWithoutTimeout(final Integer meanResponseTimeWithoutTimeout) { this.meanResponseTimeWithoutTimeout = meanResponseTimeWithoutTimeout; }
	public void setMeanResponseTimeWithTimeout(final Integer meanResponseTimeWithTimeout) { this.meanResponseTimeWithTimeout = meanResponseTimeWithTimeout; }
	public void setJitterWithTimeout(final Integer jitterWithTimeout) { this.jitterWithTimeout = jitterWithTimeout; }
	public void setJitterWithoutTimeout(final Integer jitterWithoutTimeout) { this.jitterWithoutTimeout = jitterWithoutTimeout; }
	public void setLostPerMeasurementPercent(final Integer lostPerMeasurementPercent) { this.lostPerMeasurementPercent = lostPerMeasurementPercent; }
	public void setSent(final long sent) { this.sent = sent; }
	public void setReceived(final long received) { this.received = received; }
	public void setCountStartedAt(final ZonedDateTime countStartedAt) { this.countStartedAt = countStartedAt; }
	public void setSentAll(final long sentAll) { this.sentAll = sentAll; }
	public void setReceivedAll(final long receivedAll) { this.receivedAll = receivedAll; }
	public void setCreatedAt(final ZonedDateTime createdAt) { this.createdAt = createdAt; }
	public void setUpdatedAt(final ZonedDateTime updatedAt) {	this.updatedAt = updatedAt; }
}
