package eu.arrowhead.core.qos.service.ping.monitor.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import eu.arrowhead.common.dto.shared.IcmpPingRequestACK;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.common.exception.UnavailableServerException;
import eu.arrowhead.core.qos.dto.IcmpPingResponse;
import eu.arrowhead.core.qos.measurement.properties.PingMeasurementProperties;
import eu.arrowhead.core.qos.service.QoSMonitorDriver;
import eu.arrowhead.core.qos.service.ping.monitor.PingEventCollectorTask;
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

	@Mock
	private PingEventCollectorTask eventCollector;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@Before
	public void setUp() throws Exception {

		ReflectionTestUtils.setField(monitor, "externalPingMonitorName", "externalmonitor");
		ReflectionTestUtils.setField(monitor, "externalPingMonitorAddress", "localhost");
		ReflectionTestUtils.setField(monitor, "externalPingMonitorPort", 8888);
		ReflectionTestUtils.setField(monitor, "externalPingMonitorPath", "/path");
		ReflectionTestUtils.setField(monitor, "pingMonitorSecure", true);

		ReflectionTestUtils.setField(monitor, "initialized", true);

		when(pingMeasurementProperties.getTimeout()).thenReturn(1);
		when(pingMeasurementProperties.getTimeToRepeat()).thenReturn(32);

	}

	//Tests of ping method
	//-------------------------------------------------------------------------------------------------

	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testPingAdressAddressIsNull() {

		final String address = null;
		try {

			monitor.ping(address);

		} catch (final InvalidParameterException ex) {

			verify(pingMeasurementProperties, never()).getTimeout();
			verify(pingMeasurementProperties, never()).getTimeToRepeat();
			verify(driver, never()).requestExternalPingMonitorService(any(),any());
			verify(processor, never()).processEvents(any(),anyLong());

			throw ex;
		}

	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testPingAdressAddressIsEmpty() {

		final String address = "";
		try {

			monitor.ping(address);

		} catch (final InvalidParameterException ex) {

			verify(pingMeasurementProperties, never()).getTimeout();
			verify(pingMeasurementProperties, never()).getTimeToRepeat();
			verify(driver, never()).requestExternalPingMonitorService(any(),any());
			verify(processor, never()).processEvents(any(),anyLong());

			throw ex;
		}

	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testPingOK() {

		final String address = "localhost";

		final IcmpPingRequestACK ack = new IcmpPingRequestACK();
		ack.setAckOk("OK");
		ack.setExternalMeasurementUuid(UUID.randomUUID());

		final List<IcmpPingResponse> response = List.of(new IcmpPingResponse());

		when(driver.requestExternalPingMonitorService(any(), any())).thenReturn(ack);
		when(processor.processEvents(any(), anyLong())).thenReturn(response);

		monitor.ping(address);

		verify(pingMeasurementProperties, times(2)).getTimeout();
		verify(pingMeasurementProperties, times(2)).getTimeToRepeat();
		verify(driver, times(1)).requestExternalPingMonitorService(any(),any());
		verify(processor, times(1)).processEvents(any(),anyLong());
	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testPingNotInitialized() {

		ReflectionTestUtils.setField(monitor, "initialized", false);

		final String address = "localhost";

		final IcmpPingRequestACK ack = new IcmpPingRequestACK();
		ack.setAckOk("OK");
		ack.setExternalMeasurementUuid(UUID.randomUUID());

		final List<IcmpPingResponse> response = List.of(new IcmpPingResponse());

		when(driver.requestExternalPingMonitorService(any(), any())).thenReturn(ack);
		when(processor.processEvents(any(), anyLong())).thenReturn(response);

		try {

			monitor.ping(address);

		} catch (final Exception ex) {

			verify(pingMeasurementProperties, never()).getTimeout();
			verify(pingMeasurementProperties, never()).getTimeToRepeat();
			verify(driver, never()).requestExternalPingMonitorService(any(),any());
			verify(processor, never()).processEvents(any(),anyLong());

			throw ex;
		}
	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testPingACKNull() {

		final String address = "localhost";

		final IcmpPingRequestACK ack = null;

		final List<IcmpPingResponse> response = List.of(new IcmpPingResponse());

		when(driver.requestExternalPingMonitorService(any(), any())).thenReturn(ack);
		when(processor.processEvents(any(), anyLong())).thenReturn(response);

		try {

			monitor.ping(address);

		} catch (final Exception ex) {

			verify(pingMeasurementProperties, times(2)).getTimeout();
			verify(pingMeasurementProperties, times(2)).getTimeToRepeat();
			verify(driver, times(1)).requestExternalPingMonitorService(any(),any());
			verify(processor, never()).processEvents(any(),anyLong());

			throw ex;
		}
	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testPingNullAckOK() {

		final String address = "localhost";

		final IcmpPingRequestACK ack = new IcmpPingRequestACK();
		ack.setAckOk(null);
		ack.setExternalMeasurementUuid(UUID.randomUUID());

		final List<IcmpPingResponse> response = List.of(new IcmpPingResponse());

		when(driver.requestExternalPingMonitorService(any(), any())).thenReturn(ack);
		when(processor.processEvents(any(), anyLong())).thenReturn(response);

		try {

			monitor.ping(address);

		} catch (final Exception ex) {

			verify(pingMeasurementProperties, times(2)).getTimeout();
			verify(pingMeasurementProperties, times(2)).getTimeToRepeat();
			verify(driver, times(1)).requestExternalPingMonitorService(any(),any());
			verify(processor, never()).processEvents(any(),anyLong());

			throw ex;
		}
	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testPingACKInvalidAckOK() {

		final String address = "localhost";

		final IcmpPingRequestACK ack = new IcmpPingRequestACK();
		ack.setAckOk("otherThenOK");
		ack.setExternalMeasurementUuid(UUID.randomUUID());

		final List<IcmpPingResponse> response = List.of(new IcmpPingResponse());

		when(driver.requestExternalPingMonitorService(any(), any())).thenReturn(ack);
		when(processor.processEvents(any(), anyLong())).thenReturn(response);

		try {

			monitor.ping(address);

		} catch (final Exception ex) {

			verify(pingMeasurementProperties, times(2)).getTimeout();
			verify(pingMeasurementProperties, times(2)).getTimeToRepeat();
			verify(driver, times(1)).requestExternalPingMonitorService(any(),any());
			verify(processor, never()).processEvents(any(),anyLong());

			throw ex;
		}
	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testPingACKProcessIdNull() {

		final String address = "localhost";

		final IcmpPingRequestACK ack = new IcmpPingRequestACK();
		ack.setAckOk("OK");
		ack.setExternalMeasurementUuid(null);

		final List<IcmpPingResponse> response = List.of(new IcmpPingResponse());

		when(driver.requestExternalPingMonitorService(any(), any())).thenReturn(ack);
		when(processor.processEvents(any(), anyLong())).thenReturn(response);

		try {

			monitor.ping(address);

		} catch (final Exception ex) {

			verify(pingMeasurementProperties, times(2)).getTimeout();
			verify(pingMeasurementProperties, times(2)).getTimeToRepeat();
			verify(driver, times(1)).requestExternalPingMonitorService(any(),any());
			verify(processor, never()).processEvents(any(),anyLong());

			throw ex;
		}
	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testPingDriverThrowsArrowheadException() {

		final String address = "localhost";

		final List<IcmpPingResponse> response = List.of(new IcmpPingResponse());

		when(driver.requestExternalPingMonitorService(any(), any())).thenThrow(new ArrowheadException(""));
		when(processor.processEvents(any(), anyLong())).thenReturn(response);

		try {

			monitor.ping(address);

		} catch (final Exception ex) {

			verify(pingMeasurementProperties, times(2)).getTimeout();
			verify(pingMeasurementProperties, times(2)).getTimeToRepeat();
			verify(driver, times(1)).requestExternalPingMonitorService(any(),any());
			verify(processor, never()).processEvents(any(),anyLong());

			throw ex;
		}
	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testPingDriverThrowsIllegalArgumentException() {

		final String address = "localhost";

		final List<IcmpPingResponse> response = List.of(new IcmpPingResponse());

		when(driver.requestExternalPingMonitorService(any(), any())).thenThrow(new IllegalArgumentException(""));
		when(processor.processEvents(any(), anyLong())).thenReturn(response);

		try {

			monitor.ping(address);

		} catch (final Exception ex) {

			verify(pingMeasurementProperties, times(2)).getTimeout();
			verify(pingMeasurementProperties, times(2)).getTimeToRepeat();
			verify(driver, times(1)).requestExternalPingMonitorService(any(),any());
			verify(processor, never()).processEvents(any(),anyLong());

			throw ex;
		}
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testPingProcessorThrowsIllegalArgumentException() {

		final String address = "localhost";

		final IcmpPingRequestACK ack = new IcmpPingRequestACK();
		ack.setAckOk("OK");
		ack.setExternalMeasurementUuid(UUID.randomUUID());

		when(driver.requestExternalPingMonitorService(any(), any())).thenReturn(ack);
		when(processor.processEvents(any(), anyLong())).thenThrow(new IllegalArgumentException());

		monitor.ping(address);

		verify(pingMeasurementProperties, times(2)).getTimeout();
		verify(pingMeasurementProperties, times(2)).getTimeToRepeat();
		verify(driver, times(1)).requestExternalPingMonitorService(any(),any());
		verify(processor, times(1)).processEvents(any(),anyLong());
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testPingProcessorThrowsArrowheadException() {

		final String address = "localhost";

		final IcmpPingRequestACK ack = new IcmpPingRequestACK();
		ack.setAckOk("OK");
		ack.setExternalMeasurementUuid(UUID.randomUUID());

		when(driver.requestExternalPingMonitorService(any(), any())).thenReturn(ack);
		when(processor.processEvents(any(), anyLong())).thenThrow(new ArrowheadException(""));

		monitor.ping(address);

		verify(pingMeasurementProperties, times(2)).getTimeout();
		verify(pingMeasurementProperties, times(2)).getTimeToRepeat();
		verify(driver, times(1)).requestExternalPingMonitorService(any(),any());
		verify(processor, times(1)).processEvents(any(),anyLong());
	}

	//Tests of init method
	//-------------------------------------------------------------------------------------------------

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitOk() {

		ReflectionTestUtils.setField(monitor, "initialized", false);

		doNothing().when(driver).checkPingMonitorProviderEchoUri(any());
		doNothing().when(driver).subscribeToExternalPingMonitorEvents(any());

		doNothing().when(eventCollector).run();

		monitor.init();

		verify(driver, times(1)).checkPingMonitorProviderEchoUri(any());
		verify(driver, times(1)).subscribeToExternalPingMonitorEvents(any());

		final boolean initialized = (boolean) ReflectionTestUtils.getField(monitor, "initialized");
		assertTrue(initialized);
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitAlreadyInitialized() {

		ReflectionTestUtils.setField(monitor, "initialized", true);

		doNothing().when(driver).checkPingMonitorProviderEchoUri(any());
		doNothing().when(driver).subscribeToExternalPingMonitorEvents(any());

		doNothing().when(eventCollector).run();

		monitor.init();

		verify(driver, never()).checkPingMonitorProviderEchoUri(any());
		verify(driver, never()).subscribeToExternalPingMonitorEvents(any());

		final boolean initialized = (boolean) ReflectionTestUtils.getField(monitor, "initialized");
		assertTrue(initialized);
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitOkWhenPingMonitorSystemNotNull() {

		ReflectionTestUtils.setField(monitor, "initialized", false);

		final SystemRequestDTO pingMonitor = new SystemRequestDTO();
		pingMonitor.setSystemName("pinger");
		pingMonitor.setAddress("localhost");
		pingMonitor.setPort(8888);

		ReflectionTestUtils.setField(monitor, "pingMonitorSystem", pingMonitor);

		doNothing().when(driver).checkPingMonitorProviderEchoUri(any());
		doNothing().when(driver).subscribeToExternalPingMonitorEvents(any());

		doNothing().when(eventCollector).run();

		monitor.init();

		verify(driver, times(1)).checkPingMonitorProviderEchoUri(any());
		verify(driver, times(1)).subscribeToExternalPingMonitorEvents(any());

		final boolean initialized = (boolean) ReflectionTestUtils.getField(monitor, "initialized");
		assertTrue(initialized);
	}

	//-------------------------------------------------------------------------------------------------
	@Test( expected = UnavailableServerException.class)
	public void testInitDriverCheckPingMonitorProviderEchoUriThrowsException() {

		ReflectionTestUtils.setField(monitor, "initialized", false);

		doThrow(new UnavailableServerException("")).when(driver).checkPingMonitorProviderEchoUri(any());
		doNothing().when(driver).subscribeToExternalPingMonitorEvents(any());

		doNothing().when(eventCollector).run();

		try {

			monitor.init();

		} catch (final Exception ex) {

			verify(driver, times(1)).checkPingMonitorProviderEchoUri(any());
			verify(driver, never()).subscribeToExternalPingMonitorEvents(any());

			final boolean initialized = (boolean) ReflectionTestUtils.getField(monitor, "initialized");
			assertFalse(initialized);

			throw ex;
		}

	}

	//-------------------------------------------------------------------------------------------------
	@Test( expected = UnavailableServerException.class)
	public void testInitDriverSubscribeToExternalPingMonitorEventsThrowsUnavailableException() {

		ReflectionTestUtils.setField(monitor, "initialized", false);

		doNothing().when(driver).checkPingMonitorProviderEchoUri(any());
		doThrow(new UnavailableServerException("")).when(driver).subscribeToExternalPingMonitorEvents(any());

		doNothing().when(eventCollector).run();

		try {

			monitor.init();

		} catch (final Exception ex) {

			verify(driver, times(1)).checkPingMonitorProviderEchoUri(any());
			verify(driver, times(1)).subscribeToExternalPingMonitorEvents(any());

			final boolean initialized = (boolean) ReflectionTestUtils.getField(monitor, "initialized");
			assertFalse(initialized);

			throw ex;
		}

	}

	//-------------------------------------------------------------------------------------------------
	@Test( expected = ArrowheadException.class)
	public void testInitDriverSubscribeToExternalPingMonitorEventsThrowsArrowheadException() {

		ReflectionTestUtils.setField(monitor, "initialized", false);

		doNothing().when(driver).checkPingMonitorProviderEchoUri(any());
		doThrow(new ArrowheadException("")).when(driver).subscribeToExternalPingMonitorEvents(any());

		doNothing().when(eventCollector).run();

		try {

			monitor.init();

		} catch (final Exception ex) {

			verify(driver, times(1)).checkPingMonitorProviderEchoUri(any());
			verify(driver, times(1)).subscribeToExternalPingMonitorEvents(any());

			final boolean initialized = (boolean) ReflectionTestUtils.getField(monitor, "initialized");
			assertFalse(initialized);

			throw ex;
		}

	}

}
