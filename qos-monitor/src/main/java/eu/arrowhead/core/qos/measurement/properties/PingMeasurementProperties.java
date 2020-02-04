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
	private int pocketSize;
	private int rest;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public PingMeasurementProperties() {}

	//-------------------------------------------------------------------------------------------------
	public PingMeasurementProperties(final int timeToRepeat, final int timeout, final int pocketSize, final int rest) {

		this.timeToRepeat = timeToRepeat;
		this.timeout = timeout;
		this.pocketSize = pocketSize;
		this.rest = rest;
	}

	//-------------------------------------------------------------------------------------------------
	public int getTimeToRepeat() { return timeToRepeat; }
	public int getTimeout() { return timeout; }
	public int getPocketSize() { return pocketSize; }
	public int getRest() { return rest; }

	//-------------------------------------------------------------------------------------------------
	public void setTimeToRepeat(final int timeToRepeat) { this.timeToRepeat = timeToRepeat; }
	public void setTimeout(final int timeout) { this.timeout = timeout; }
	public void setPocketSize(final int pocketSize) { this.pocketSize = pocketSize; }
	public void setRest(final int rest) { this.rest = rest; }

}
