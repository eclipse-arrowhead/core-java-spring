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
@Table(name = "qos_inter_relay_echo_measurement_log")
public class QoSInterRelayEchoMeasurementLog {

	//=================================================================================================
	// members

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "measurementId", referencedColumnName = "id", nullable = false, unique = true)
	private QoSInterRelayMeasurement measurement;

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

	@Column(name = "measured_at",nullable = false)
	private ZonedDateTime measuredAt;

	@Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
	private ZonedDateTime createdAt;

	@Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
	private ZonedDateTime updatedAt;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public QoSInterRelayEchoMeasurementLog() {}

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
	public QoSInterRelayMeasurement getMeasurement() { return measurement; }
	public Integer getMinResponseTime() { return minResponseTime; }
	public Integer getMaxResponseTime() { return maxResponseTime; }
	public Integer getMeanResponseTimeWithTimeout() { return meanResponseTimeWithTimeout; }
	public Integer getMeanResponseTimeWithoutTimeout() { return meanResponseTimeWithoutTimeout; }
	public Integer getJitterWithTimeout() { return jitterWithTimeout; }
	public Integer getJitterWithoutTimeout() { return jitterWithoutTimeout; }
	public Integer getLostPerMeasurementPercent() { return lostPerMeasurementPercent; }
	public long getSent() { return sent; }
	public long getReceived() { return received; }
	public ZonedDateTime getMeasuredAt() { return measuredAt; }

	//-------------------------------------------------------------------------------------------------
	public void setMeasurement(final QoSInterRelayMeasurement measurement) { this.measurement = measurement; }
	public void setMinResponseTime(final Integer minResponseTime) { this.minResponseTime = minResponseTime; }
	public void setMaxResponseTime(final Integer maxResponseTime) { this.maxResponseTime = maxResponseTime; }
	public void setMeanResponseTimeWithTimeout(final Integer meanResponseTimeWithTimeout) { this.meanResponseTimeWithTimeout = meanResponseTimeWithTimeout; }
	public void setMeanResponseTimeWithoutTimeout(final Integer meanResponseTimeWithoutTimeout) { this.meanResponseTimeWithoutTimeout = meanResponseTimeWithoutTimeout; }
	public void setJitterWithTimeout(final Integer jitterWithTimeout) { this.jitterWithTimeout = jitterWithTimeout; }
	public void setJitterWithoutTimeout(final Integer jitterWithoutTimeout) { this.jitterWithoutTimeout = jitterWithoutTimeout; }
	public void setLostPerMeasurementPercent(final Integer lostPerMeasurementPercent) { this.lostPerMeasurementPercent = lostPerMeasurementPercent; }
	public void setSent(final long sent) { this.sent = sent; }
	public void setReceived(final long received) { this.received = received; }
	public void setMeasuredAt(final ZonedDateTime measuredAt) { this.measuredAt = measuredAt; }

}
