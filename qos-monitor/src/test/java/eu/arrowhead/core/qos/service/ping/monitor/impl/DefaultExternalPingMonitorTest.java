package eu.arrowhead.core.qos.service.ping.monitor.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;

import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.core.qos.measurement.properties.PingMeasurementProperties;
import eu.arrowhead.core.qos.service.QoSMonitorDriver;
import eu.arrowhead.core.qos.service.ping.monitor.PingEventProcessor;

@RunWith(SpringRunner.class)
public class DefaultExternalPingMonitorTest {

	//=================================================================================================
	// members
	@InjectMocks
	private DefaultExternalPingMonitor monitor;

	@Mock
	protected PingMeasurementProperties pingMeasurementProperties;

	@Mock
	private QoSMonitorDriver driver;

	@Mock
	private PingEventProcessor processor;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@Before
	public void setUp() throws Exception {

		when(pingMeasurementProperties.getTimeout()).thenReturn(1);
		when(pingMeasurementProperties.getTimeToRepeat()).thenReturn(32);

	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testPingAdressAddressInNull() {

		final String address = null;
		try {

			monitor.ping(address);

		} catch (final InvalidParameterException ex) {

			verify(pingMeasurementProperties, never()).getTimeout();
			verify(pingMeasurementProperties, never()).getTimeToRepeat();
			verify(driver, never()).requestExternalPingMonitorService(any(),any());
//			verify(processor, never()).processEvents(any(),any());

			throw ex;
		}

	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testPingAdressAddressInEmpty() {

		final String address = "";
		try {

			monitor.ping(address);

		} catch (final InvalidParameterException ex) {

			verify(pingMeasurementProperties, never()).getTimeout();
			verify(pingMeasurementProperties, never()).getTimeToRepeat();
			verify(driver, never()).requestExternalPingMonitorService(any(),any());
//			verify(processor, never()).processEvents(any(),any());

			throw ex;
		}

	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testPing() {

		final String address = "localhost";


	}

}
