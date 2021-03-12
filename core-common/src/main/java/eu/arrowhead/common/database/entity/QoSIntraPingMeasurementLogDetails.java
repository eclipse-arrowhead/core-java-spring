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
@Table(name = "qos_intra_ping_measurement_log_details")
public class QoSIntraPingMeasurementLogDetails {

	//=================================================================================================
	// members

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "measurement_log_id", referencedColumnName = "id", nullable = false, unique = true)
	private QoSIntraPingMeasurementLog measurementLog;

	@Column(name = "measurement_sequenece_number", nullable = false)
	private int measurementSequeneceNumber;

	@Column(name = "success_flag", nullable = false)
	private boolean successFlag = false;

	@Column(name = "timeout_flag", nullable = false)
	private boolean timeoutFlag = false;

	@Column(name = "error_message", nullable = true)
	private String errorMessage;

	@Column(name = "throwable", nullable = true)
	private String throwable;

	@Column(name = "size_", nullable = true)
	private Integer size;

	@Column(name = "rtt", nullable = true)
	private Integer rtt;

	@Column(name = "ttl", nullable = true)
	private Integer ttl;

	@Column(name = "duration", nullable = true)
	private Integer duration;

	@Column(name = "measured_at", nullable = false)
	private ZonedDateTime measuredAt;

	@Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
	private ZonedDateTime createdAt;

	@Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
	private ZonedDateTime updatedAt;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public QoSIntraPingMeasurementLogDetails() {}

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
	public QoSIntraPingMeasurementLog getMeasurementLog() { return measurementLog; }
	public int getMeasurementSequeneceNumber() { return measurementSequeneceNumber; }
	public boolean isSuccessFlag() { return successFlag; }
	public boolean isTimeoutFlag() { return timeoutFlag; }
	public String getErrorMessage() { return errorMessage; }
	public String getThrowable() { return throwable; }
	public Integer getSize() { return size; }
	public Integer getRtt() { return rtt; }
	public Integer getTtl() { return ttl; }
	public Integer getDuration() { return duration; }
	public ZonedDateTime getMeasuredAt() { return measuredAt; }

	//-------------------------------------------------------------------------------------------------
	public void setMeasurementLog(final QoSIntraPingMeasurementLog measurementLog) { this.measurementLog = measurementLog; }
	public void setMeasurementSequeneceNumber(final int measurementSequeneceNumber) { this.measurementSequeneceNumber = measurementSequeneceNumber; }
	public void setSuccessFlag(final boolean successFlag) { this.successFlag = successFlag; }
	public void setTimeoutFlag(final boolean timeoutFlag) { this.timeoutFlag = timeoutFlag; }
	public void setErrorMessage(final String errorMessage) { this.errorMessage = errorMessage; }
	public void setThrowable(final String throwable) { this.throwable = throwable; }
	public void setSize(final Integer size) { this.size = size; }
	public void setRtt(final Integer rtt) { this.rtt = rtt; }
	public void setTtl(final Integer ttl) { this.ttl = ttl; }
	public void setDuration(final Integer duration) { this.duration = duration; }
	public void setMeasuredAt(final ZonedDateTime measuredAt) { this.measuredAt = measuredAt; }

}
