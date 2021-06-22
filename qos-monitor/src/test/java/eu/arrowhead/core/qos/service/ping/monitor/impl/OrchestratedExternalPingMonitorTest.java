package eu.arrowhead.core.qos.service.ping.monitor.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.shared.IcmpPingRequestACK;
import eu.arrowhead.common.dto.shared.OrchestrationFormRequestDTO;
import eu.arrowhead.common.dto.shared.OrchestrationResponseDTO;
import eu.arrowhead.common.dto.shared.OrchestrationResultDTO;
import eu.arrowhead.common.dto.shared.ServiceSecurityType;
import eu.arrowhead.common.dto.shared.SystemResponseDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.core.qos.dto.IcmpPingResponse;
import eu.arrowhead.core.qos.dto.externalMonitor.ExternalMonitorOrchestrationRequestFactory;
import eu.arrowhead.core.qos.measurement.properties.PingMeasurementProperties;
import eu.arrowhead.core.qos.service.QoSMonitorDriver;
import eu.arrowhead.core.qos.service.ping.monitor.PingEventCollectorTask;
import eu.arrowhead.core.qos.service.ping.monitor.PingEventProcessor;

@RunWith(SpringRunner.class)
public class OrchestratedExternalPingMonitorTest {

	//=================================================================================================
	// members
	@InjectMocks
	private OrchestratedExternalPingMonitor monitor;

	@Mock
	protected PingMeasurementProperties pingMeasurementProperties;

	@Mock
	private QoSMonitorDriver driver;

	@Mock
	private PingEventProcessor processor;

	@Mock
	private ExternalMonitorOrchestrationRequestFactory orchestrationRequestFactory;

	@Mock
	private PingEventCollectorTask eventCollector;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@Before
	public void setUp() throws Exception {

		final OrchestrationResultDTO cachedPingMonitorProvider = getValidOrchestrationResultDTOForTests();

		ReflectionTestUtils.setField(monitor, "cachedPingMonitorProvider", cachedPingMonitorProvider);
		ReflectionTestUtils.setField(monitor, "initialized", true);
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

		when(pingMeasurementProperties.getTimeout()).thenReturn(1);
		when(pingMeasurementProperties.getTimeToRepeat()).thenReturn(32);
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

		when(pingMeasurementProperties.getTimeout()).thenReturn(1);
		when(pingMeasurementProperties.getTimeToRepeat()).thenReturn(32);
		when(driver.requestExternalPingMonitorService(any(), any())).thenReturn(ack);
		when(processor.processEvents(any(), anyLong())).thenReturn(response);

		try {

			monitor.ping(address);

		} catch (final Exception ex) {

			verify(pingMeasurementProperties, never()).getTimeout();
			verify(pingMeasurementProperties, never()).getTimeToRepeat();
			verify(driver, never()).requestExternalPingMonitorService(any(),any());
			verify(processor, never()).processEvents(any(),anyLong());

			final boolean initialized = (boolean) ReflectionTestUtils.getField(monitor, "initialized");
			assertFalse(initialized);

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

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testPingCachedProviderIsNull() {

		final OrchestrationResultDTO cachedPingMonitorProvider = null;
		ReflectionTestUtils.setField(monitor, "cachedPingMonitorProvider", cachedPingMonitorProvider);

		final String address = "localhost";

		final IcmpPingRequestACK ack = new IcmpPingRequestACK();
		ack.setAckOk("OK");
		ack.setExternalMeasurementUuid(UUID.randomUUID());

		final OrchestrationFormRequestDTO orchestrationForm = new OrchestrationFormRequestDTO();

		final List<IcmpPingResponse> response = List.of(new IcmpPingResponse());

		when(driver.requestExternalPingMonitorService(any(), any())).thenReturn(ack);
		when(processor.processEvents(any(), anyLong())).thenReturn(response);

		when(orchestrationRequestFactory.createExternalMonitorOrchestrationRequest()).thenReturn(orchestrationForm);
		when(driver.queryOrchestrator(any())).thenReturn(getValidOrchestrationResponseDTOForTests());
		doNothing().when(driver).unsubscribeFromPingMonitorEvents();
		doNothing().when(driver).subscribeToExternalPingMonitorEvents(any());

		final List<IcmpPingResponse> pingResult = monitor.ping(address);

		assertNotNull(pingResult);

		verify(pingMeasurementProperties, times(2)).getTimeout();
		verify(pingMeasurementProperties, times(2)).getTimeToRepeat();
		verify(driver, times(1)).requestExternalPingMonitorService(any(),any());
		verify(processor, times(1)).processEvents(any(),anyLong());
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testPingCachedProviderIsNullAndOrchestrationFormIsNull() {

		final OrchestrationResultDTO cachedPingMonitorProvider = null;
		ReflectionTestUtils.setField(monitor, "cachedPingMonitorProvider", cachedPingMonitorProvider);

		final String address = "localhost";

		final IcmpPingRequestACK ack = new IcmpPingRequestACK();
		ack.setAckOk("OK");
		ack.setExternalMeasurementUuid(UUID.randomUUID());

		final OrchestrationFormRequestDTO orchestrationForm = null;

		final List<IcmpPingResponse> response = List.of(new IcmpPingResponse());

		when(driver.requestExternalPingMonitorService(any(), any())).thenReturn(ack);
		when(processor.processEvents(any(), anyLong())).thenReturn(response);

		when(orchestrationRequestFactory.createExternalMonitorOrchestrationRequest()).thenReturn(orchestrationForm);
		when(driver.queryOrchestrator(any())).thenReturn(getValidOrchestrationResponseDTOForTests());
		doNothing().when(driver).unsubscribeFromPingMonitorEvents();
		doNothing().when(driver).subscribeToExternalPingMonitorEvents(any());

		final List<IcmpPingResponse> pingResult = monitor.ping(address);

		assertNull(pingResult);

		verify(pingMeasurementProperties, never()).getTimeout();
		verify(pingMeasurementProperties, never()).getTimeToRepeat();
		verify(driver, never()).requestExternalPingMonitorService(any(),any());
		verify(processor, never()).processEvents(any(),anyLong());
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testPingCachedProviderIsNullAndOrchestrationResponseIsNull() {

		final OrchestrationResultDTO cachedPingMonitorProvider = null;
		ReflectionTestUtils.setField(monitor, "cachedPingMonitorProvider", cachedPingMonitorProvider);

		final String address = "localhost";

		final IcmpPingRequestACK ack = new IcmpPingRequestACK();
		ack.setAckOk("OK");
		ack.setExternalMeasurementUuid(UUID.randomUUID());

		final OrchestrationFormRequestDTO orchestrationForm = new OrchestrationFormRequestDTO();

		final List<IcmpPingResponse> response =  List.of(new IcmpPingResponse());

		when(driver.requestExternalPingMonitorService(any(), any())).thenReturn(ack);
		when(processor.processEvents(any(), anyLong())).thenReturn(response);

		when(orchestrationRequestFactory.createExternalMonitorOrchestrationRequest()).thenReturn(orchestrationForm);
		when(driver.queryOrchestrator(any())).thenReturn(getNullOrchestrationResponseDTOForTests());
		doNothing().when(driver).unsubscribeFromPingMonitorEvents();
		doNothing().when(driver).subscribeToExternalPingMonitorEvents(any());

		final List<IcmpPingResponse> pingResult = monitor.ping(address);

		assertNull(pingResult);

		verify(pingMeasurementProperties, never()).getTimeout();
		verify(pingMeasurementProperties, never()).getTimeToRepeat();
		verify(driver, never()).requestExternalPingMonitorService(any(),any());
		verify(processor, never()).processEvents(any(),anyLong());
	}


	//-------------------------------------------------------------------------------------------------
	@Test
	public void testPingCachedProviderIsNullAndOrchestrationResponseIsEmpty() {

		final OrchestrationResultDTO cachedPingMonitorProvider = null;
		ReflectionTestUtils.setField(monitor, "cachedPingMonitorProvider", cachedPingMonitorProvider);

		final String address = "localhost";

		final IcmpPingRequestACK ack = new IcmpPingRequestACK();
		ack.setAckOk("OK");
		ack.setExternalMeasurementUuid(UUID.randomUUID());

		final OrchestrationFormRequestDTO orchestrationForm = new OrchestrationFormRequestDTO();

		final List<IcmpPingResponse> response =  List.of(new IcmpPingResponse());

		when(driver.requestExternalPingMonitorService(any(), any())).thenReturn(ack);
		when(processor.processEvents(any(), anyLong())).thenReturn(response);

		when(orchestrationRequestFactory.createExternalMonitorOrchestrationRequest()).thenReturn(orchestrationForm);
		when(driver.queryOrchestrator(any())).thenReturn(getEmptyOrchestrationResponseDTOForTests());
		doNothing().when(driver).unsubscribeFromPingMonitorEvents();
		doNothing().when(driver).subscribeToExternalPingMonitorEvents(any());

		final List<IcmpPingResponse> pingResult = monitor.ping(address);

		assertNull(pingResult);

		verify(pingMeasurementProperties, never()).getTimeout();
		verify(pingMeasurementProperties, never()).getTimeToRepeat();
		verify(driver, never()).requestExternalPingMonitorService(any(),any());
		verify(processor, never()).processEvents(any(),anyLong());
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testPingCachedProviderIsNullAndQueryOrchestratorThrowException() {

		final OrchestrationResultDTO cachedPingMonitorProvider = null;
		ReflectionTestUtils.setField(monitor, "cachedPingMonitorProvider", cachedPingMonitorProvider);

		final String address = "localhost";

		final IcmpPingRequestACK ack = new IcmpPingRequestACK();
		ack.setAckOk("OK");
		ack.setExternalMeasurementUuid(UUID.randomUUID());

		final OrchestrationFormRequestDTO orchestrationForm = new OrchestrationFormRequestDTO();

		final List<IcmpPingResponse> response =  List.of(new IcmpPingResponse());

		when(driver.requestExternalPingMonitorService(any(), any())).thenReturn(ack);
		when(processor.processEvents(any(), anyLong())).thenReturn(response);

		when(orchestrationRequestFactory.createExternalMonitorOrchestrationRequest()).thenReturn(orchestrationForm);
		when(driver.queryOrchestrator(any())).thenThrow(new ArrowheadException(""));
		doNothing().when(driver).unsubscribeFromPingMonitorEvents();
		doNothing().when(driver).subscribeToExternalPingMonitorEvents(any());

		final List<IcmpPingResponse> pingResult = monitor.ping(address);

		assertNull(pingResult);

		verify(pingMeasurementProperties, never()).getTimeout();
		verify(pingMeasurementProperties, never()).getTimeToRepeat();
		verify(driver, never()).requestExternalPingMonitorService(any(),any());
		verify(processor, never()).processEvents(any(),anyLong());
	}


	//-------------------------------------------------------------------------------------------------
	@Test
	public void testPingCachedProviderIsNullAndQueryOrchestratorThrowIllegalArgumentException() {

		final OrchestrationResultDTO cachedPingMonitorProvider = null;
		ReflectionTestUtils.setField(monitor, "cachedPingMonitorProvider", cachedPingMonitorProvider);

		final String address = "localhost";

		final IcmpPingRequestACK ack = new IcmpPingRequestACK();
		ack.setAckOk("OK");
		ack.setExternalMeasurementUuid(UUID.randomUUID());

		final OrchestrationFormRequestDTO orchestrationForm = new OrchestrationFormRequestDTO();

		final List<IcmpPingResponse> response =  List.of(new IcmpPingResponse());

		when(driver.requestExternalPingMonitorService(any(), any())).thenReturn(ack);
		when(processor.processEvents(any(), anyLong())).thenReturn(response);

		when(orchestrationRequestFactory.createExternalMonitorOrchestrationRequest()).thenReturn(orchestrationForm);
		when(driver.queryOrchestrator(any())).thenThrow(new IllegalArgumentException(""));
		doNothing().when(driver).unsubscribeFromPingMonitorEvents();
		doNothing().when(driver).subscribeToExternalPingMonitorEvents(any());

		final List<IcmpPingResponse> pingResult = monitor.ping(address);

		assertNull(pingResult);

		verify(pingMeasurementProperties, never()).getTimeout();
		verify(pingMeasurementProperties, never()).getTimeToRepeat();
		verify(driver, never()).requestExternalPingMonitorService(any(),any());
		verify(processor, never()).processEvents(any(),anyLong());
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testPingCachedProviderIsNullAndSubscribeThrowException() {

		final OrchestrationResultDTO cachedPingMonitorProvider = null;
		ReflectionTestUtils.setField(monitor, "cachedPingMonitorProvider", cachedPingMonitorProvider);

		final String address = "localhost";

		final IcmpPingRequestACK ack = new IcmpPingRequestACK();
		ack.setAckOk("OK");
		ack.setExternalMeasurementUuid(UUID.randomUUID());

		final OrchestrationFormRequestDTO orchestrationForm = new OrchestrationFormRequestDTO();

		final List<IcmpPingResponse> response = List.of(new IcmpPingResponse());

		when(driver.requestExternalPingMonitorService(any(), any())).thenReturn(ack);
		when(processor.processEvents(any(), anyLong())).thenReturn(response);

		when(orchestrationRequestFactory.createExternalMonitorOrchestrationRequest()).thenReturn(orchestrationForm);
		when(driver.queryOrchestrator(any())).thenReturn(getValidOrchestrationResponseDTOForTests());
		doNothing().when(driver).unsubscribeFromPingMonitorEvents();
		doThrow(new ArrowheadException("")).when(driver).subscribeToExternalPingMonitorEvents(any());

		final List<IcmpPingResponse> pingResult = monitor.ping(address);

		assertNull(pingResult);

		verify(pingMeasurementProperties, never()).getTimeout();
		verify(pingMeasurementProperties, never()).getTimeToRepeat();
		verify(driver, never()).requestExternalPingMonitorService(any(),any());
		verify(processor, never()).processEvents(any(),anyLong());
	}

	//Tests of init method
	//-------------------------------------------------------------------------------------------------

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitOk() {

		ReflectionTestUtils.setField(monitor, "initialized", false);

		final OrchestrationResultDTO cachedPingMonitorProvider = null;
		ReflectionTestUtils.setField(monitor, "cachedPingMonitorProvider", cachedPingMonitorProvider);

		final OrchestrationFormRequestDTO orchestrationForm = new OrchestrationFormRequestDTO();

		when(orchestrationRequestFactory.createExternalMonitorOrchestrationRequest()).thenReturn(orchestrationForm);
		when(driver.queryOrchestrator(any())).thenReturn(getValidOrchestrationResponseDTOForTests());
		doNothing().when(driver).unsubscribeFromPingMonitorEvents();
		doNothing().when(driver).subscribeToExternalPingMonitorEvents(any());

		doNothing().when(eventCollector).run();

		monitor.init();

		verify(orchestrationRequestFactory, times(1)).createExternalMonitorOrchestrationRequest();
		verify(driver, times(1)).queryOrchestrator(any());
		verify(driver, times(1)).unsubscribeFromPingMonitorEvents();
		verify(driver, times(1)).subscribeToExternalPingMonitorEvents(any());

		final boolean initialized = (boolean) ReflectionTestUtils.getField(monitor, "initialized");
		assertTrue(initialized);
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitAlreadyInitialized() {

		ReflectionTestUtils.setField(monitor, "initialized", true);

		final OrchestrationResultDTO cachedPingMonitorProvider = null;
		ReflectionTestUtils.setField(monitor, "cachedPingMonitorProvider", cachedPingMonitorProvider);

		final OrchestrationFormRequestDTO orchestrationForm = new OrchestrationFormRequestDTO();

		when(orchestrationRequestFactory.createExternalMonitorOrchestrationRequest()).thenReturn(orchestrationForm);
		when(driver.queryOrchestrator(any())).thenReturn(getValidOrchestrationResponseDTOForTests());
		doNothing().when(driver).unsubscribeFromPingMonitorEvents();
		doNothing().when(driver).subscribeToExternalPingMonitorEvents(any());

		doNothing().when(eventCollector).run();

		monitor.init();

		verify(orchestrationRequestFactory, never()).createExternalMonitorOrchestrationRequest();
		verify(driver, never()).queryOrchestrator(any());
		verify(driver, never()).unsubscribeFromPingMonitorEvents();
		verify(driver, never()).subscribeToExternalPingMonitorEvents(any());

		final boolean initialized = (boolean) ReflectionTestUtils.getField(monitor, "initialized");
		assertTrue(initialized);
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitDriverThrowsIllegalArgumentException() {

		ReflectionTestUtils.setField(monitor, "initialized", false);

		final OrchestrationResultDTO cachedPingMonitorProvider = null;
		ReflectionTestUtils.setField(monitor, "cachedPingMonitorProvider", cachedPingMonitorProvider);

		final OrchestrationFormRequestDTO orchestrationForm = null;

		when(orchestrationRequestFactory.createExternalMonitorOrchestrationRequest()).thenReturn(orchestrationForm);
		when(driver.queryOrchestrator(any())).thenThrow( new IllegalArgumentException());
		doNothing().when(driver).unsubscribeFromPingMonitorEvents();
		doNothing().when(driver).subscribeToExternalPingMonitorEvents(any());

		doNothing().when(eventCollector).run();

		monitor.init();

		verify(orchestrationRequestFactory, times(1)).createExternalMonitorOrchestrationRequest();
		verify(driver, times(1)).queryOrchestrator(any());
		verify(driver, never()).unsubscribeFromPingMonitorEvents();
		verify(driver, never()).subscribeToExternalPingMonitorEvents(any());

		final boolean initialized = (boolean) ReflectionTestUtils.getField(monitor, "initialized");
		assertTrue(initialized);
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitDriverThrowsArrowheadException() {

		ReflectionTestUtils.setField(monitor, "initialized", false);

		final OrchestrationResultDTO cachedPingMonitorProvider = null;
		ReflectionTestUtils.setField(monitor, "cachedPingMonitorProvider", cachedPingMonitorProvider);

		final OrchestrationFormRequestDTO orchestrationForm = new OrchestrationFormRequestDTO();

		when(orchestrationRequestFactory.createExternalMonitorOrchestrationRequest()).thenReturn(orchestrationForm);
		when(driver.queryOrchestrator(any())).thenThrow( new ArrowheadException(""));
		doNothing().when(driver).unsubscribeFromPingMonitorEvents();
		doNothing().when(driver).subscribeToExternalPingMonitorEvents(any());

		doNothing().when(eventCollector).run();

		monitor.init();

		verify(orchestrationRequestFactory, times(1)).createExternalMonitorOrchestrationRequest();
		verify(driver, times(1)).queryOrchestrator(any());
		verify(driver, never()).unsubscribeFromPingMonitorEvents();
		verify(driver, never()).subscribeToExternalPingMonitorEvents(any());

		final boolean initialized = (boolean) ReflectionTestUtils.getField(monitor, "initialized");
		assertTrue(initialized);
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitOrchestrationResultIsNull() {

		ReflectionTestUtils.setField(monitor, "initialized", false);

		final OrchestrationResultDTO cachedPingMonitorProvider = null;
		ReflectionTestUtils.setField(monitor, "cachedPingMonitorProvider", cachedPingMonitorProvider);

		final OrchestrationFormRequestDTO orchestrationForm = new OrchestrationFormRequestDTO();

		when(orchestrationRequestFactory.createExternalMonitorOrchestrationRequest()).thenReturn(orchestrationForm);
		when(driver.queryOrchestrator(any())).thenReturn(getNullOrchestrationResponseDTOForTests());
		doNothing().when(driver).unsubscribeFromPingMonitorEvents();
		doNothing().when(driver).subscribeToExternalPingMonitorEvents(any());

		doNothing().when(eventCollector).run();

		monitor.init();

		verify(orchestrationRequestFactory, times(1)).createExternalMonitorOrchestrationRequest();
		verify(driver, times(1)).queryOrchestrator(any());
		verify(driver, never()).unsubscribeFromPingMonitorEvents();
		verify(driver, never()).subscribeToExternalPingMonitorEvents(any());

		final boolean initialized = (boolean) ReflectionTestUtils.getField(monitor, "initialized");
		assertTrue(initialized);
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitOrchestrationResultIsIsEmpty() {

		ReflectionTestUtils.setField(monitor, "initialized", false);

		final OrchestrationResultDTO cachedPingMonitorProvider = null;
		ReflectionTestUtils.setField(monitor, "cachedPingMonitorProvider", cachedPingMonitorProvider);

		final OrchestrationFormRequestDTO orchestrationForm = new OrchestrationFormRequestDTO();

		when(orchestrationRequestFactory.createExternalMonitorOrchestrationRequest()).thenReturn(orchestrationForm);
		when(driver.queryOrchestrator(any())).thenReturn(getEmptyOrchestrationResponseDTOForTests());
		doNothing().when(driver).unsubscribeFromPingMonitorEvents();
		doNothing().when(driver).subscribeToExternalPingMonitorEvents(any());

		doNothing().when(eventCollector).run();

		monitor.init();

		verify(orchestrationRequestFactory, times(1)).createExternalMonitorOrchestrationRequest();
		verify(driver, times(1)).queryOrchestrator(any());
		verify(driver, never()).unsubscribeFromPingMonitorEvents();
		verify(driver, never()).subscribeToExternalPingMonitorEvents(any());

		final boolean initialized = (boolean) ReflectionTestUtils.getField(monitor, "initialized");
		assertTrue(initialized);
	}

	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	private OrchestrationResponseDTO getValidOrchestrationResponseDTOForTests() {

		final OrchestrationResponseDTO response = new OrchestrationResponseDTO();
		response.setResponse(List.of(getValidOrchestrationResultDTOForTests()));

		return response;
	}

	//-------------------------------------------------------------------------------------------------
	private OrchestrationResponseDTO getNullOrchestrationResponseDTOForTests() {

		return null;
	}

	//-------------------------------------------------------------------------------------------------
	private OrchestrationResponseDTO getEmptyOrchestrationResponseDTOForTests() {

		final OrchestrationResponseDTO response = new OrchestrationResponseDTO();
		response.setResponse(List.of());

		return response;
	}

	//-------------------------------------------------------------------------------------------------
	private OrchestrationResultDTO getValidOrchestrationResultDTOForTests() {

		final OrchestrationResultDTO result = new OrchestrationResultDTO();
		result.setProvider(getValidSystemResponseDTOForTests());
		result.setServiceUri("/theservice");
		result.setSecure(ServiceSecurityType.CERTIFICATE);

		return result;
	}

	//-------------------------------------------------------------------------------------------------
	private SystemResponseDTO getValidSystemResponseDTOForTests() {

		final SystemResponseDTO system = new SystemResponseDTO(
				0,
				"externalPingMonitor",
				"localhost",
				8888,
				null,
				Map.of("m","m"),
				Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()),
				Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()));

		return system;
	}

}
