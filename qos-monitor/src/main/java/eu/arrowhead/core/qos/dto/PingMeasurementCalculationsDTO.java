package eu.arrowhead.core.qos.dto;

import java.io.Serializable;

public class PingMeasurementCalculationsDTO implements Serializable{

	//=================================================================================================
	// members

	private static final long serialVersionUID = -1645860899758857162L;

	private boolean available;
	private int maxResponseTime;
	private int minResponseTime;
	private int meanResponseTimeWithTimeout;
	private int meanResponseTimeWithOutTimeout;
	private int jitterWithTimeout;
	private int jitterWithoutTimeout;
	private int sentInThisPing;
	private int receivedInThisPing;
	private int lostPerMeasurementPercent;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public PingMeasurementCalculationsDTO() {}

	//-------------------------------------------------------------------------------------------------
	public PingMeasurementCalculationsDTO(final boolean available, final int maxResponseTime, final int minResponseTime,
			final int meanResponseTimeWithTimeout, final int meanResponseTimeWithOutTimeout, final int jitterWithTimeout,
			final int jitterWithoutTimeout, final int sentInThisPing, final int receivedInThisPing, final int lostPerMeasurementPercent) {

		this.available = available;
		this.maxResponseTime = maxResponseTime;
		this.minResponseTime = minResponseTime;
		this.meanResponseTimeWithTimeout = meanResponseTimeWithTimeout;
		this.meanResponseTimeWithOutTimeout = meanResponseTimeWithOutTimeout;
		this.jitterWithTimeout = jitterWithTimeout;
		this.jitterWithoutTimeout = jitterWithoutTimeout;
		this.sentInThisPing = sentInThisPing;
		this.receivedInThisPing = receivedInThisPing;
		this.lostPerMeasurementPercent = lostPerMeasurementPercent;
	}

	//-------------------------------------------------------------------------------------------------
	public boolean isAvailable() { return available; }
	public int getMaxResponseTime() { return maxResponseTime; }
	public int getMinResponseTime() { return minResponseTime; }
	public int getMeanResponseTimeWithTimeout() { return meanResponseTimeWithTimeout; }
	public int getMeanResponseTimeWithOutTimeout() { return meanResponseTimeWithOutTimeout; }
	public int getJitterWithTimeout() { return jitterWithTimeout; }
	public int getJitterWithoutTimeout() { return jitterWithoutTimeout; }
	public int getSentInThisPing() { return sentInThisPing; }
	public int getReceivedInThisPing() { return receivedInThisPing; }
	public int getLostPerMeasurementPercent() { return lostPerMeasurementPercent; }

	//-------------------------------------------------------------------------------------------------
	public void setAvailable(final boolean available) { this.available = available; }
	public void setMaxResponseTime(final int maxResponseTime) { this.maxResponseTime = maxResponseTime; }
	public void setMinResponseTime(final int minResponseTime) { this.minResponseTime = minResponseTime; }
	public void setMeanResponseTimeWithTimeout(final int meanResponseTimeWithTimeout) { this.meanResponseTimeWithTimeout = meanResponseTimeWithTimeout; }
	public void setMeanResponseTimeWithOutTimeout(final int meanResponseTimeWithOutTimeout) { this.meanResponseTimeWithOutTimeout = meanResponseTimeWithOutTimeout; }
	public void setJitterWithTimeout(final int jitterWithTimeout) { this.jitterWithTimeout = jitterWithTimeout; }
	public void setJitterWithoutTimeout(final int jitterWithoutTimeout) { this.jitterWithoutTimeout = jitterWithoutTimeout; }
	public void setSentInThisPing(final int sentInThisPing) { this.sentInThisPing = sentInThisPing; }
	public void setReceivedInThisPing(final int receivedInThisPing) { this.receivedInThisPing = receivedInThisPing; }
	public void setLostPerMeasurementPercent(final int lostPerMeasurementPercent) { this.lostPerMeasurementPercent = lostPerMeasurementPercent; }

}
