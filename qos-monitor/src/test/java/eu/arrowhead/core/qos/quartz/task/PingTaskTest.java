package eu.arrowhead.core.qos.quartz.task;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.logging.log4j.Logger;
import org.icmp4j.IcmpPingResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.util.UriComponents;

import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.entity.QoSIntraMeasurement;
import eu.arrowhead.common.database.entity.QoSIntraPingMeasurement;
import eu.arrowhead.common.database.entity.QoSIntraPingMeasurementLog;
import eu.arrowhead.common.database.entity.System;
import eu.arrowhead.common.dto.internal.DTOConverter;
import eu.arrowhead.common.dto.internal.ServiceRegistryListResponseDTO;
import eu.arrowhead.common.dto.shared.QoSMeasurementType;
import eu.arrowhead.common.dto.shared.ServiceDefinitionResponseDTO;
import eu.arrowhead.common.dto.shared.ServiceInterfaceResponseDTO;
import eu.arrowhead.common.dto.shared.ServiceRegistryResponseDTO;
import eu.arrowhead.common.dto.shared.ServiceSecurityType;
import eu.arrowhead.common.dto.shared.SystemResponseDTO;
import eu.arrowhead.common.http.HttpService;
import eu.arrowhead.core.qos.database.service.QoSDBService;
import eu.arrowhead.core.qos.measurement.properties.PingMeasurementProperties;
import eu.arrowhead.core.qos.service.PingService;

@RunWith(SpringRunner.class)
public class PingTaskTest {

	//=================================================================================================
	// members
	@InjectMocks
	private final PingTask pingTask = new PingTask();

	@Mock
	private PingService pingService;

	@Mock
	private QoSDBService qoSDBService;

	@Mock
	private HttpService httpService;

	@Mock
	private PingMeasurementProperties pingMeasurementProperties;

	@Mock
	private Map<String,Object> arrowheadContext;

	@Mock
	private JobExecutionContext jobExecutionContext;

	private Logger logger;

	final static int TIME_TO_REPEAT_PING = 3;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@Before
	public void setUp() throws Exception {
		logger = mock(Logger.class);		
		ReflectionTestUtils.setField(pingTask, "logger", logger);
	}

	//=================================================================================================
	// Tests of execute
	@Test
	public void testExecuteMeasurementIsInDB() {

		final UriComponents uri = Utilities.createURI("HTTPS", "localhost", 12345, "serviceregistry");

		final ServiceRegistryListResponseDTO serviceRegistryResponse = getServiceRegistryListResponseDTOForTest();
		final ResponseEntity<ServiceRegistryListResponseDTO> httpResponse = new ResponseEntity<ServiceRegistryListResponseDTO>(serviceRegistryResponse, HttpStatus.OK);

		final List<IcmpPingResponse> responseList = getResponseListForTest();

		final QoSIntraMeasurement measurement = getQoSIntraMeasurementForTest();

		final QoSIntraPingMeasurement pingMeasurement = getQosIntraPingMeasurementForTest();
		final QoSIntraPingMeasurementLog measurementLog = getMeasurementLogForTest();

		final ArgumentCaptor<String> debugValueCapture = ArgumentCaptor.forClass(String.class);
		doNothing().when(logger).debug( debugValueCapture.capture());

		when(arrowheadContext.containsKey(CoreCommonConstants.SERVER_STANDALONE_MODE)).thenReturn(false);
		when(arrowheadContext.containsKey(CoreCommonConstants.SR_QUERY_ALL)).thenReturn(true);
		when(arrowheadContext.get(CoreCommonConstants.SR_QUERY_ALL)).thenReturn(uri);

		when(httpService.sendRequest(uri, HttpMethod.GET, ServiceRegistryListResponseDTO.class)).thenReturn(httpResponse);

		when(pingMeasurementProperties.getTimeToRepeat()).thenReturn(TIME_TO_REPEAT_PING);
		when(pingMeasurementProperties.getPacketSize()).thenReturn(32);
		when(pingMeasurementProperties.getTimeout()).thenReturn(5000);
		when(pingMeasurementProperties.getRest()).thenReturn(1000);

		when(pingService.getPingResponseList(anyString())).thenReturn(responseList);

		when(qoSDBService.getMeasurement(any(SystemResponseDTO.class))).thenReturn(measurement);

		//in handleMeasurement
		when(qoSDBService.getPingMeasurementByMeasurement(any(QoSIntraMeasurement.class))).thenReturn(Optional.of(pingMeasurement));
		//when(qoSDBService.createPingMeasurement(any(), any(), any())).thenReturn(pingMeasurement);
		doNothing().when(qoSDBService).updatePingMeasurement(any(), any(), any(), any());
		when(qoSDBService.logMeasurementToDB(any(), any(), any())).thenReturn(measurementLog);
		doNothing().when(qoSDBService).logMeasurementDetailsToDB(any(), any(), any());

		try {

			pingTask.execute(jobExecutionContext);

		} catch (final JobExecutionException ex) {
			assertTrue(false);
		}

		verify(logger, atLeastOnce()).debug(any(String.class));
		final List<String> debugMessages = debugValueCapture.getAllValues();
		assertNotNull(debugMessages);
	}

	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	public ServiceRegistryListResponseDTO getServiceRegistryListResponseDTOForTest() {

		final int sizeOfResponse = 3;
		final ServiceRegistryListResponseDTO serviceRegistryListResponseDTO = new ServiceRegistryListResponseDTO();
		final List<ServiceRegistryResponseDTO> responseList = new ArrayList<>(sizeOfResponse);

		for (int i = 0; i < sizeOfResponse; i++) {
			responseList.add(getServiceRegistryResponseDTOForTest());
		}

		serviceRegistryListResponseDTO.setCount(sizeOfResponse);
		serviceRegistryListResponseDTO.setData(responseList);

		return serviceRegistryListResponseDTO;
	}

	//-------------------------------------------------------------------------------------------------
	public ServiceRegistryResponseDTO getServiceRegistryResponseDTOForTest() {

		final ServiceRegistryResponseDTO serviceRegistryResponseDTO = new ServiceRegistryResponseDTO();

		serviceRegistryResponseDTO.setId(1L);
		serviceRegistryResponseDTO.setServiceDefinition(getServiceDefinitionResponseDTOForTest());
		serviceRegistryResponseDTO.setProvider(getSystemResponseDTOForTest());
		serviceRegistryResponseDTO.setServiceUri("serviceUri");
		serviceRegistryResponseDTO.setEndOfValidity(null);
		serviceRegistryResponseDTO.setSecure(ServiceSecurityType.CERTIFICATE);
		serviceRegistryResponseDTO.setMetadata(null);
		serviceRegistryResponseDTO.setVersion(1);
		serviceRegistryResponseDTO.setInterfaces(List.of(getServiceInterfaceResponseDTOForTest()));
		serviceRegistryResponseDTO.setCreatedAt(Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()));
		serviceRegistryResponseDTO.setUpdatedAt(Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()));

		return serviceRegistryResponseDTO;
	}

	//-------------------------------------------------------------------------------------------------
	public ServiceDefinitionResponseDTO getServiceDefinitionResponseDTOForTest() {

		final ServiceDefinitionResponseDTO serviceDefinitionResponseDTO = new ServiceDefinitionResponseDTO();

		serviceDefinitionResponseDTO.setId(1L);
		serviceDefinitionResponseDTO.setServiceDefinition("serviceDefinition");
		serviceDefinitionResponseDTO.setCreatedAt(Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()));
		serviceDefinitionResponseDTO.setUpdatedAt(Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()));

		return serviceDefinitionResponseDTO;
	}

	//-------------------------------------------------------------------------------------------------
	private System getSystemForTest() {

		final System system = new System(
				"testSystem",
				"address",
				12345,
				"authenticationInfo");

		return system;
	}

	//-------------------------------------------------------------------------------------------------
	private SystemResponseDTO getSystemResponseDTOForTest() {

		final SystemResponseDTO systemResponseDTO = DTOConverter.convertSystemToSystemResponseDTO(getSystemForTest());

		return systemResponseDTO;
	}

	//-------------------------------------------------------------------------------------------------
	private ServiceInterfaceResponseDTO getServiceInterfaceResponseDTOForTest() {

		final ServiceInterfaceResponseDTO serviceInterfaceResponseDTO = new ServiceInterfaceResponseDTO();
		serviceInterfaceResponseDTO.setId(1L);
		serviceInterfaceResponseDTO.setInterfaceName("interfaceName");
		serviceInterfaceResponseDTO.setCreatedAt(Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()));
		serviceInterfaceResponseDTO.setUpdatedAt(Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now()));

		return serviceInterfaceResponseDTO;
	}

	//-------------------------------------------------------------------------------------------------
	private List<IcmpPingResponse> getResponseListForTest() {

		final List<IcmpPingResponse> responseList = new ArrayList<>();
		for (int i = 0; i < TIME_TO_REPEAT_PING; i++) {
			responseList.add(getIcmpPingResponse());
		}

		return responseList;
	}

	//-------------------------------------------------------------------------------------------------
	private IcmpPingResponse getIcmpPingResponse() {

		final IcmpPingResponse pingResponse = new IcmpPingResponse();
		pingResponse.setSuccessFlag(true);
		pingResponse.setTimeoutFlag(false);
		pingResponse.setRtt(1);
		pingResponse.setDuration(1L);
		pingResponse.setTtl(64);
		pingResponse.setSize(32);
		pingResponse.setHost("localhost");

		return pingResponse;
	}

	//-------------------------------------------------------------------------------------------------
	private QoSIntraMeasurement getQoSIntraMeasurementForTest() {

		final System system = getSystemForTest();
		final QoSIntraMeasurement measurement = new QoSIntraMeasurement(
				system, 
				QoSMeasurementType.PING, 
				ZonedDateTime.now());

		return measurement;
	}

	//-------------------------------------------------------------------------------------------------
	private QoSIntraPingMeasurement getQosIntraPingMeasurementForTest() {

		final QoSIntraMeasurement measurement = getQoSIntraMeasurementForTest();

		final QoSIntraPingMeasurement pingMeasurement = new QoSIntraPingMeasurement();

		pingMeasurement.setMeasurement(measurement);
		pingMeasurement.setAvailable(true);
		pingMeasurement.setMaxResponseTime(1);
		pingMeasurement.setMinResponseTime(1);
		pingMeasurement.setMeanResponseTimeWithoutTimeout(1);
		pingMeasurement.setMeanResponseTimeWithTimeout(1);
		pingMeasurement.setJitterWithoutTimeout(1);
		pingMeasurement.setJitterWithTimeout(1);
		pingMeasurement.setLostPerMeasurementPercent(0);
		pingMeasurement.setCountStartedAt(ZonedDateTime.now());
		pingMeasurement.setLastAccessAt(ZonedDateTime.now());
		pingMeasurement.setSent(35);
		pingMeasurement.setSentAll(35);
		pingMeasurement.setReceived(35);
		pingMeasurement.setReceivedAll(35);

		return pingMeasurement;
	}

	//-------------------------------------------------------------------------------------------------
	private QoSIntraPingMeasurementLog getMeasurementLogForTest() {

		final QoSIntraPingMeasurementLog measurementLog = new QoSIntraPingMeasurementLog();
		measurementLog.setAvailable(true);

		return measurementLog;
	}

}
