package eu.arrowhead.common.dto.internal;

import java.io.Serializable;
import java.time.ZonedDateTime;

import org.springframework.util.Assert;

import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.shared.SystemResponseDTO;

public class QoSMeasurementAttributesFormDTO implements Serializable {

	//=================================================================================================
	// members
	
	private static final long serialVersionUID = -7212471265314450784L;
	
	private SystemResponseDTO system;
	private ZonedDateTime lastAccessAt;
	private Integer minResponseTime;
	private Integer maxResponseTime;
	private Integer meanResponseTimeWithTimeout;
	private Integer meanResponseTimeWithoutTimeout;
	private Integer jitterWithTimeout;
	private Integer jitterWithoutTimeout;
	private Integer lostPerMeasurementPercent;	
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public QoSMeasurementAttributesFormDTO() {}

	//-------------------------------------------------------------------------------------------------
	public QoSMeasurementAttributesFormDTO(final SystemResponseDTO system, final ZonedDateTime lastAccessAt, final Integer minResponseTime, final Integer maxResponseTime,
										   final Integer meanResponseTimeWithTimeout, final Integer meanResponseTimeWithoutTimeout, final Integer jitterWithTimeout,
										   final Integer jitterWithoutTimeout, final Integer lostPerMeasurementPercent) {
		Assert.notNull(system, "System is null");
		Assert.isTrue(Utilities.isEmpty(system.getSystemName()), "System name is null or empty");
		Assert.isTrue(Utilities.isEmpty(system.getAddress()), "System address is null or empty");
		
		this.system = system;
		this.lastAccessAt = lastAccessAt;
		this.minResponseTime = minResponseTime;
		this.maxResponseTime = maxResponseTime;
		this.meanResponseTimeWithTimeout = meanResponseTimeWithTimeout;
		this.meanResponseTimeWithoutTimeout = meanResponseTimeWithoutTimeout;
		this.jitterWithTimeout = jitterWithTimeout;
		this.jitterWithoutTimeout = jitterWithoutTimeout;
		this.lostPerMeasurementPercent = lostPerMeasurementPercent;
	}

	//-------------------------------------------------------------------------------------------------
	public SystemResponseDTO getSystem() { return system; }
	public ZonedDateTime getLastAccessAt() { return lastAccessAt; }
	public Integer getMinResponseTime() { return minResponseTime; }
	public Integer getMaxResponseTime() { return maxResponseTime; }
	public Integer getMeanResponseTimeWithTimeout() { return meanResponseTimeWithTimeout; }
	public Integer getMeanResponseTimeWithoutTimeout() { return meanResponseTimeWithoutTimeout; }
	public Integer getJitterWithTimeout() { return jitterWithTimeout; }
	public Integer getJitterWithoutTimeout() { return jitterWithoutTimeout; }
	public Integer getLostPerMeasurementPercent() { return lostPerMeasurementPercent; }

	//-------------------------------------------------------------------------------------------------
	public void setSystem(final SystemResponseDTO system) { this.system = system; }
	public void setLastAccessAt(final ZonedDateTime lastAccessAt) { this.lastAccessAt = lastAccessAt; }
	public void setMinResponseTime(final Integer minResponseTime) { this.minResponseTime = minResponseTime; }
	public void setMaxResponseTime(final Integer maxResponseTime) { this.maxResponseTime = maxResponseTime; }
	public void setMeanResponseTimeWithTimeout(final Integer meanResponseTimeWithTimeout) { this.meanResponseTimeWithTimeout = meanResponseTimeWithTimeout; }
	public void setMeanResponseTimeWithoutTimeout(final Integer meanResponseTimeWithoutTimeout) { this.meanResponseTimeWithoutTimeout = meanResponseTimeWithoutTimeout; }
	public void setJitterWithTimeout(final Integer jitterWithTimeout) { this.jitterWithTimeout = jitterWithTimeout; }
	public void setJitterWithoutTimeout(final Integer jitterWithoutTimeout) { this.jitterWithoutTimeout = jitterWithoutTimeout; }
	public void setLostPerMeasurementPercent(final Integer lostPerMeasurementPercent) { this.lostPerMeasurementPercent = lostPerMeasurementPercent; }
}
