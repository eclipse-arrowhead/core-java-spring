package eu.arrowhead.core.qos.service.ping.monitor.impl;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;

import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.core.qos.measurement.properties.PingMeasurementProperties;

@RunWith(SpringRunner.class)
public class DummyPingMonitorTest {

	//=================================================================================================
	// members
	@InjectMocks
	private DummyPingMonitor dummyPingMonitor;

	@Mock
	protected PingMeasurementProperties pingMeasurementProperties;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testPingAdressAddressInNull() {

		final String address = null;
		try {

			dummyPingMonitor.ping(address);

		} catch (final InvalidParameterException ex) {

			verify(pingMeasurementProperties, never()).getTimeToRepeat();

			throw ex;
		}

	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testPingAdressAddressInEmpty() {

		final String address = "";
		try {

			dummyPingMonitor.ping(address);

		} catch (final InvalidParameterException ex) {

			verify(pingMeasurementProperties, never()).getTimeToRepeat();

			throw ex;
		}

	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testPingOk() {

		final String address = "localhost";

		dummyPingMonitor.ping(address);
		verify(pingMeasurementProperties, times(1)).getTimeToRepeat();

	}
}
