package eu.arrowhead.core.qos.measurement.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "ping")
public class PingMeasurementProperties {

	//=================================================================================================
	// members

	private int timeToRepeat;
	private int timeout;
	private int packetSize;
	private int rest;
	private int availableFromSuccessPercet;
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public PingMeasurementProperties() {}

	//-------------------------------------------------------------------------------------------------
	public PingMeasurementProperties(final int timeToRepeat, final int timeout, final int pocketSize, final int rest, final int availableFromSuccessPercet) {

		this.timeToRepeat = timeToRepeat;
		this.timeout = timeout;
		this.packetSize = pocketSize;
		this.rest = rest;
		this.availableFromSuccessPercet = availableFromSuccessPercet;
	}

	//-------------------------------------------------------------------------------------------------
	public int getTimeToRepeat() { return timeToRepeat; }
	public int getTimeout() { return timeout; }
	public int getPacketSize() { return packetSize; }
	public int getRest() { return rest; }
	public int getAvailableFromSuccessPercet() { return availableFromSuccessPercet; }

	//-------------------------------------------------------------------------------------------------
	public void setTimeToRepeat(final int timeToRepeat) { this.timeToRepeat = timeToRepeat; }
	public void setTimeout(final int timeout) { this.timeout = timeout; }
	public void setPacketSize(final int packetSize) { this.packetSize = packetSize; }
	public void setRest(final int rest) { this.rest = rest; }
	public void setAvailableFromSuccessPercet(final int availableFromSuccessPercet) { this.availableFromSuccessPercet = availableFromSuccessPercet; }

}
