package eu.arrowhead.core.qos.measurement.properties;

import javax.annotation.PostConstruct;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;

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

	//-------------------------------------------------------------------------------------------------
	@PostConstruct
	private void validateFields() {

		Assert.isTrue(timeToRepeat > 0 && timeToRepeat <= 100, "Time to repeat must be greater than 0 and not greater than 100");
		Assert.isTrue(timeout >= 0 && timeout < 10000, "timeout must be greater than or equal 0 and not greater than 10 000");
		Assert.isTrue((packetSize == 32 || packetSize == 56 || packetSize == 64), "packetSize has to be 32 or 56 or 64");
		Assert.isTrue(rest >= 0 && rest <= 10000, "rest must be greater than 0 and not greater than 10 000");
		Assert.isTrue(availableFromSuccessPercet > 0 && availableFromSuccessPercet <= 100, "availableFromSuccessPercet must be greater than 0 and not greater than 100");

	}
}
