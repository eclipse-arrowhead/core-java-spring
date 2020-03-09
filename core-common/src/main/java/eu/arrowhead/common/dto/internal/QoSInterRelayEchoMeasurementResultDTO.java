package eu.arrowhead.common.dto.internal;

import java.io.Serializable;
import java.time.ZonedDateTime;

import eu.arrowhead.common.dto.shared.QoSMeasurementType;
import eu.arrowhead.common.dto.shared.SystemResponseDTO;

public class QoSInterRelayEchoMeasurementResultDTO implements Serializable {

	//=================================================================================================
	// members
		
	private static final long serialVersionUID = -8493306412834725919L;
	
	private CloudResponseDTO cloud;
	private SystemResponseDTO system;
	private RelayResponseDTO relay;
	private QoSMeasurementType measurementType;
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
	private long sentAll;
	private long receivedAll;
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public QoSInterRelayEchoMeasurementResultDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public QoSInterRelayEchoMeasurementResultDTO(final CloudResponseDTO cloud, final SystemResponseDTO system, final RelayResponseDTO relay, final QoSMeasurementType measurementType,
												 final ZonedDateTime lastAccessAt, final Integer minResponseTime, final Integer maxResponseTime, final Integer meanResponseTimeWithTimeout,
												 final Integer meanResponseTimeWithoutTimeout, final Integer jitterWithTimeout, final Integer jitterWithoutTimeout, 
												 final Integer lostPerMeasurementPercent, final long sent, final long received, final long sentAll, final long receivedAll) {
		this.cloud = cloud;
		this.system = system;
		this.relay = relay;
		this.measurementType = measurementType;
		this.lastAccessAt = lastAccessAt;
		this.minResponseTime = minResponseTime;
		this.maxResponseTime = maxResponseTime;
		this.meanResponseTimeWithTimeout = meanResponseTimeWithTimeout;
		this.meanResponseTimeWithoutTimeout = meanResponseTimeWithoutTimeout;
		this.jitterWithTimeout = jitterWithTimeout;
		this.jitterWithoutTimeout = jitterWithoutTimeout;
		this.lostPerMeasurementPercent = lostPerMeasurementPercent;
		this.sent = sent;
		this.received = received;
		this.sentAll = sentAll;
		this.receivedAll = receivedAll;
	}

	//-------------------------------------------------------------------------------------------------
	public CloudResponseDTO getCloud() { return cloud; }
	public SystemResponseDTO getSystem() { return system; }
	public RelayResponseDTO getRelay() { return relay; }
	public QoSMeasurementType getMeasurementType() { return measurementType; }
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
	public long getSentAll() { return sentAll; }
	public long getReceivedAll() { return receivedAll; }

	//-------------------------------------------------------------------------------------------------
	public void setCloud(CloudResponseDTO cloud) { this.cloud = cloud; }
	public void setSystem(SystemResponseDTO system) { this.system = system; }
	public void setRelay(RelayResponseDTO relay) { this.relay = relay; }
	public void setMeasurementType(QoSMeasurementType measurementType) { this.measurementType = measurementType; }
	public void setLastAccessAt(ZonedDateTime lastAccessAt) { this.lastAccessAt = lastAccessAt; }
	public void setMinResponseTime(Integer minResponseTime) { this.minResponseTime = minResponseTime; }
	public void setMaxResponseTime(Integer maxResponseTime) { this.maxResponseTime = maxResponseTime; }
	public void setMeanResponseTimeWithTimeout(Integer meanResponseTimeWithTimeout) { this.meanResponseTimeWithTimeout = meanResponseTimeWithTimeout; }
	public void setMeanResponseTimeWithoutTimeout(Integer meanResponseTimeWithoutTimeout) { this.meanResponseTimeWithoutTimeout = meanResponseTimeWithoutTimeout; }
	public void setJitterWithTimeout(Integer jitterWithTimeout) { this.jitterWithTimeout = jitterWithTimeout; }
	public void setJitterWithoutTimeout(Integer jitterWithoutTimeout) { this.jitterWithoutTimeout = jitterWithoutTimeout; }
	public void setLostPerMeasurementPercent(Integer lostPerMeasurementPercent) { this.lostPerMeasurementPercent = lostPerMeasurementPercent; }
	public void setSent(long sent) { this.sent = sent; }
	public void setReceived(long received) { this.received = received; }
	public void setSentAll(long sentAll) { this.sentAll = sentAll; }
	public void setReceivedAll(long receivedAll) { this.receivedAll = receivedAll; }	
}
