package eu.arrowhead.core.qos.service.ping.monitor;

import org.springframework.beans.factory.annotation.Autowired;

import eu.arrowhead.core.qos.measurement.properties.PingMeasurementProperties;

public abstract class AbstractPingMonitor implements PingMonitorManager {

	//=================================================================================================
	// members

	//-------------------------------------------------------------------------------------------------
	@Autowired
	protected PingMeasurementProperties pingMeasurementProperties;

}
