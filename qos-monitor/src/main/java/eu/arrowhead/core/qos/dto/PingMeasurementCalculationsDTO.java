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

package eu.arrowhead.core.qos.dto;

import java.io.Serializable;
import java.time.ZonedDateTime;

public class PingMeasurementCalculationsDTO implements Serializable {

	//=================================================================================================
	// members

	private static final long serialVersionUID = -1645860899758857162L;

	private boolean available;
	private Integer maxResponseTime;
	private Integer minResponseTime;
	private Integer meanResponseTimeWithTimeout;
	private Integer meanResponseTimeWithoutTimeout;
	private Integer jitterWithTimeout;
	private Integer jitterWithoutTimeout;
	private Integer sentInThisPing;
	private Integer receivedInThisPing;
	private Integer lostPerMeasurementPercent;
	private ZonedDateTime measuredAt;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public PingMeasurementCalculationsDTO() {}

	//-------------------------------------------------------------------------------------------------
	public PingMeasurementCalculationsDTO(final boolean available, final Integer maxResponseTime, final Integer minResponseTime,
			final Integer meanResponseTimeWithTimeout, final Integer meanResponseTimeWithoutTimeout, final Integer jitterWithTimeout,
			final Integer jitterWithoutTimeout, final Integer sentInThisPing, final Integer receivedInThisPing, final Integer lostPerMeasurementPercent,
			final ZonedDateTime measuredAt) {

		this.available = available;
		this.maxResponseTime = maxResponseTime;
		this.minResponseTime = minResponseTime;
		this.meanResponseTimeWithTimeout = meanResponseTimeWithTimeout;
		this.meanResponseTimeWithoutTimeout = meanResponseTimeWithoutTimeout;
		this.jitterWithTimeout = jitterWithTimeout;
		this.jitterWithoutTimeout = jitterWithoutTimeout;
		this.sentInThisPing = sentInThisPing;
		this.receivedInThisPing = receivedInThisPing;
		this.lostPerMeasurementPercent = lostPerMeasurementPercent;
		this.measuredAt = measuredAt;
	}

	//-------------------------------------------------------------------------------------------------
	public boolean isAvailable() { return available; }
	public Integer getMaxResponseTime() { return maxResponseTime; }
	public Integer getMinResponseTime() { return minResponseTime; }
	public Integer getMeanResponseTimeWithTimeout() { return meanResponseTimeWithTimeout; }
	public Integer getMeanResponseTimeWithoutTimeout() { return meanResponseTimeWithoutTimeout; }
	public Integer getJitterWithTimeout() { return jitterWithTimeout; }
	public Integer getJitterWithoutTimeout() { return jitterWithoutTimeout; }
	public Integer getSentInThisPing() { return sentInThisPing; }
	public Integer getReceivedInThisPing() { return receivedInThisPing; }
	public Integer getLostPerMeasurementPercent() { return lostPerMeasurementPercent; }
	public ZonedDateTime getMeasuredAt() { return measuredAt; }

	//-------------------------------------------------------------------------------------------------
	public void setAvailable(final boolean available) { this.available = available; }
	public void setMaxResponseTime(final Integer maxResponseTime) { this.maxResponseTime = maxResponseTime; }
	public void setMinResponseTime(final Integer minResponseTime) { this.minResponseTime = minResponseTime; }
	public void setMeanResponseTimeWithTimeout(final Integer meanResponseTimeWithTimeout) { this.meanResponseTimeWithTimeout = meanResponseTimeWithTimeout; }
	public void setMeanResponseTimeWithoutTimeout(final Integer meanResponseTimeWithoutTimeout) { this.meanResponseTimeWithoutTimeout = meanResponseTimeWithoutTimeout; }
	public void setJitterWithTimeout(final Integer jitterWithTimeout) { this.jitterWithTimeout = jitterWithTimeout; }
	public void setJitterWithoutTimeout(final Integer jitterWithoutTimeout) { this.jitterWithoutTimeout = jitterWithoutTimeout; }
	public void setSentInThisPing(final Integer sentInThisPing) { this.sentInThisPing = sentInThisPing; }
	public void setReceivedInThisPing(final Integer receivedInThisPing) { this.receivedInThisPing = receivedInThisPing; }
	public void setLostPerMeasurementPercent(final Integer lostPerMeasurementPercent) { this.lostPerMeasurementPercent = lostPerMeasurementPercent; }
	public void setMeasuredAt(final ZonedDateTime measuredAt) { this.measuredAt = measuredAt; }

}