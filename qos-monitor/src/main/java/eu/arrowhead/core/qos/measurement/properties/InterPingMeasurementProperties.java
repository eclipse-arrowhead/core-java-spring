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

package eu.arrowhead.core.qos.measurement.properties;

import javax.annotation.PostConstruct;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;

@Configuration
@ConfigurationProperties(prefix = "interping")
public class InterPingMeasurementProperties {

	//=================================================================================================
	// members

	private int timeToRepeat;
	private int timeout;
	private int packetSize;
	private int rest;
	private int availableFromSuccessPercent;
	private boolean logMeasurementsToDB;
	private boolean logMeasurementsDetailsToDB;
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public InterPingMeasurementProperties() {}

	//-------------------------------------------------------------------------------------------------
	public InterPingMeasurementProperties(final int timeToRepeat, final int timeout, final int pocketSize, 
			final int rest, final int availableFromSuccessPercent, final boolean logMeasurementsToDB, 
			final boolean logMeasurementsDetailsToDB) {

		this.timeToRepeat = timeToRepeat;
		this.timeout = timeout;
		this.packetSize = pocketSize;
		this.rest = rest;
		this.availableFromSuccessPercent = availableFromSuccessPercent;
		this.logMeasurementsToDB = logMeasurementsToDB;
		this.logMeasurementsDetailsToDB = logMeasurementsDetailsToDB;
	}

	//-------------------------------------------------------------------------------------------------
	public int getTimeToRepeat() { return timeToRepeat; }
	public int getTimeout() { return timeout; }
	public int getPacketSize() { return packetSize; }
	public int getRest() { return rest; }
	public int getAvailableFromSuccessPercent() { return availableFromSuccessPercent; }
	public boolean getLogMeasurementsToDB() { return logMeasurementsToDB; }
	public boolean getLogMeasurementsDetailsToDB() {return logMeasurementsDetailsToDB; }
	
	//-------------------------------------------------------------------------------------------------
	public void setTimeToRepeat(final int timeToRepeat) { this.timeToRepeat = timeToRepeat; }
	public void setTimeout(final int timeout) { this.timeout = timeout; }
	public void setPacketSize(final int packetSize) { this.packetSize = packetSize; }
	public void setRest(final int rest) { this.rest = rest; }
	public void setAvailableFromSuccessPercent(final int availableFromSuccessPercent) { this.availableFromSuccessPercent = availableFromSuccessPercent; }
	public void setLogMeasurementsToDB(final boolean logMeasurementsToDB) { this.logMeasurementsToDB = logMeasurementsToDB; }
	public void setLogMeasurementsDetailsToDB(final boolean logMeasurementsDetailsToDB) { this.logMeasurementsDetailsToDB = logMeasurementsDetailsToDB; }

	//-------------------------------------------------------------------------------------------------
	@PostConstruct
	private void validateFields() {

		Assert.isTrue(timeToRepeat > 0 && timeToRepeat <= 100, "Time to repeat must be greater than 0 and not greater than 100");
		Assert.isTrue(timeout >= 0 && timeout < 10000, "timeout must be greater than or equal 0 and not greater than 10 000");
		Assert.isTrue((packetSize == 32 || packetSize == 56 || packetSize == 64), "packetSize has to be 32 or 56 or 64");
		Assert.isTrue(rest >= 0 && rest <= 10000, "rest must be greater than 0 and not greater than 10 000");
		Assert.isTrue(availableFromSuccessPercent > 0 && availableFromSuccessPercent <= 100, "availableFromSuccessPercent must be greater than 0 and not greater than 100");
	}
}