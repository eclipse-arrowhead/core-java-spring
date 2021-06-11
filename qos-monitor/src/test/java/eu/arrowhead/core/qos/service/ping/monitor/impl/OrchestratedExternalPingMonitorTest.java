package eu.arrowhead.core.qos.service.ping.monitor.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import eu.arrowhead.common.dto.shared.OrchestrationResultDTO;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.core.qos.service.QoSMonitorDriver;
import eu.arrowhead.core.qos.service.ping.monitor.PingEventProcessor;

@RunWith(SpringRunner.class)
public class OrchestratedExternalPingMonitorTest {

	//=================================================================================================
	// members
	@InjectMocks
	private OrchestratedExternalPingMonitor monitor;

	@Mock
	private QoSMonitorDriver driver;

	@Mock
	private PingEventProcessor processor;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@Before
	public void setUp() throws Exception {

		final OrchestrationResultDTO cachedPingMonitorProvider = new OrchestrationResultDTO();

		ReflectionTestUtils.setField(monitor, "cachedPingMonitorProvider", cachedPingMonitorProvider);

	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testPingAdressAddressInNull() {

		final String address = null;
		try {

			monitor.ping(address);

		} catch (final InvalidParameterException ex) {

			verify(driver, never()).requestExternalPingMonitorService(any(),any());
			verify(processor, never()).processEvents(any(),anyLong());

			throw ex;
		}

	}
}
