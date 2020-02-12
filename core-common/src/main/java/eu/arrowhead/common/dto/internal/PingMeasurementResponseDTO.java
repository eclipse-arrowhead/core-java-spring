package eu.arrowhead.common.dto.internal;

import java.io.Serializable;
import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class PingMeasurementResponseDTO  implements Serializable {

	//=================================================================================================
	// members

	private static final long serialVersionUID = 4436693629791850816L;

	private Long id;
	private QoSIntraMeasurementResponseDTO measurement;
	private boolean available = false;
	private ZonedDateTime lastAccessAt;
	private Integer minResponseTime;
	private Integer maxResponseTime;
	private Integer meanResponseTimeWithTimeout;
	private Integer meanResponseTimeWithoutTimeout;
	private Integer jitterWithTimeout;
	private Integer jitterWithoutTimeout;
	private Integer lostPerMeasurementPercent;
	private long sent;
	private long received;
	private ZonedDateTime countStartedAt;
	private long sentAll;
	private long receivedAll;
	private ZonedDateTime createdAt;
	private ZonedDateTime updatedAt;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public PingMeasurementResponseDTO() {}

	//-------------------------------------------------------------------------------------------------
	public Long getId() { return id; }
	public QoSIntraMeasurementResponseDTO getMeasurement() { return measurement; }
	public boolean isAvailable() {return available; }
	public ZonedDateTime getLastAccessAt() { return lastAccessAt; }
	public Integer getMinResponseTime() { return minResponseTime; }
	public Integer getMaxResponseTime() { return maxResponseTime; }
	public Integer getMeanResponseTimeWithTimeout() { return meanResponseTimeWithTimeout; }
	public Integer getMeanResponseTimeWithoutTimeout() { return meanResponseTimeWithoutTimeout; }
	public Integer getJitterWithTimeout() { return jitterWithTimeout; }
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
	@JsonIgnore
	public boolean hasRecord() {
		return id != null;
	}

	//-------------------------------------------------------------------------------------------------
	public void setId(final Long id) { this.id = id; }
	public void setMeasurement(final QoSIntraMeasurementResponseDTO measurement) { this.measurement = measurement; }
	public void setAvailable(final boolean available) { this.available = available; }
	public void setLastAccessAt(final ZonedDateTime lastAccessAt) { this.lastAccessAt = lastAccessAt; }
	public void setMinResponseTime(final Integer minResponseTime) { this.minResponseTime = minResponseTime; }
	public void setMaxResponseTime(final Integer maxResponseTime) { this.maxResponseTime = maxResponseTime; }
	public void setMeanResponseTimeWithTimeout(final Integer meanResponseTimeWithTimeout) { this.meanResponseTimeWithTimeout = meanResponseTimeWithTimeout; }
	public void setMeanResponseTimeWithoutTimeout(final Integer meanResponseTimeWithoutTimeout) { this.meanResponseTimeWithoutTimeout = meanResponseTimeWithoutTimeout; }
	public void setJitterWithTimeout(final Integer jitterWithTimeout) { this.jitterWithTimeout = jitterWithTimeout; }
	public void setJitterWithoutTimeout(final Integer jitterWithoutTimeout) { this.jitterWithoutTimeout = jitterWithoutTimeout; }
	public void setLostPerMeasurementPercent(final Integer lostPerMeasurementPercent) { this.lostPerMeasurementPercent = lostPerMeasurementPercent; }
	public void setSent(final long sent) { this.sent = sent; }
	public void setReceived(final long received) { this.received = received; }
	public void setCountStartedAt(final ZonedDateTime countStartedAt) { this.countStartedAt = countStartedAt; }
	public void setSentAll(final long sentAll) { this.sentAll = sentAll; }
	public void setReceivedAll(final long receivedAll) { this.receivedAll = receivedAll; }
	public void setCreatedAt(final ZonedDateTime createdAt) { this.createdAt = createdAt; }
	public void setUpdatedAt(final ZonedDateTime updatedAt) { this.updatedAt = updatedAt; }
}
