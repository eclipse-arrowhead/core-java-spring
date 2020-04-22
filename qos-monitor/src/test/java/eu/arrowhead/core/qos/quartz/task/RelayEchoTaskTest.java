package eu.arrowhead.core.qos.quartz.task;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.security.PublicKey;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.SSLProperties;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.entity.QoSInterRelayMeasurement;
import eu.arrowhead.common.dto.internal.CloudAccessListResponseDTO;
import eu.arrowhead.common.dto.internal.CloudAccessResponseDTO;
import eu.arrowhead.common.dto.internal.CloudWithRelaysAndPublicRelaysListResponseDTO;
import eu.arrowhead.common.dto.internal.CloudWithRelaysAndPublicRelaysResponseDTO;
import eu.arrowhead.common.dto.internal.DTOConverter;
import eu.arrowhead.common.dto.internal.QoSRelayTestProposalRequestDTO;
import eu.arrowhead.common.dto.internal.RelayResponseDTO;
import eu.arrowhead.common.dto.internal.RelayType;
import eu.arrowhead.common.dto.shared.QoSMeasurementType;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.core.qos.database.service.QoSDBService;
import eu.arrowhead.core.qos.service.QoSMonitorDriver;

@RunWith(SpringRunner.class)
public class RelayEchoTaskTest {
	
	//=================================================================================================
	// members
	@InjectMocks
	private final RelayEchoTask relayEchoTask = new RelayEchoTask();
	
	@Mock
	private QoSMonitorDriver qosMonitorDriver;
	
	@Mock
	private QoSDBService qosDBService;
	
	@Mock
	private SSLProperties sslProperties;
	
	@Mock
	private Map<String,Object> arrowheadContext;
	
	@Mock
	private JobExecutionContext jobExecutionContext;
	
	private final boolean relayTaskEnabled = true;
	
	private PublicKey publicKey;

	private Logger logger;
	
	private long cloudIdCounter = 0;
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@Before
	public void setUp() throws Exception {
		logger = mock(Logger.class);
		ReflectionTestUtils.setField(relayEchoTask, "logger", logger);
		ReflectionTestUtils.setField(relayEchoTask, "relayTaskEnabled", relayTaskEnabled);
		publicKey = getPublicKey();
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testExecuteServerStandAloneMode() {
		when(arrowheadContext.containsKey(CoreCommonConstants.SERVER_STANDALONE_MODE)).thenReturn(true);
		
		try {
			relayEchoTask.execute(jobExecutionContext);
		} catch (final JobExecutionException ex) {
			fail();
		}

		verify(logger, times(2)).debug(any(String.class));		
		verify(qosMonitorDriver, never()).queryGatekeeperAllCloud();
		verify(qosMonitorDriver, never()).queryGatekeeperCloudAccessTypes(any());
		verify(qosDBService, never()).getInterRelayMeasurement(any(), any(), any());
		verify(qosMonitorDriver, never()).requestGatekeeperInitRelayTest(any());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testExecuteSSLDisabled() {
		when(arrowheadContext.containsKey(CoreCommonConstants.SERVER_STANDALONE_MODE)).thenReturn(false);
		when(sslProperties.isSslEnabled()).thenReturn(Boolean.FALSE);
		
		try {
			relayEchoTask.execute(jobExecutionContext);
		} catch (final JobExecutionException ex) {
			fail();
		}

		verify(logger, times(2)).debug(any(String.class));		
		verify(qosMonitorDriver, never()).queryGatekeeperAllCloud();
		verify(qosMonitorDriver, never()).queryGatekeeperCloudAccessTypes(any());
		verify(qosDBService, never()).getInterRelayMeasurement(any(), any(), any());
		verify(qosMonitorDriver, never()).requestGatekeeperInitRelayTest(any());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testExecuteTaskDisabled() {
		ReflectionTestUtils.setField(relayEchoTask, "relayTaskEnabled", false);
		when(arrowheadContext.containsKey(CoreCommonConstants.SERVER_STANDALONE_MODE)).thenReturn(false);
		when(sslProperties.isSslEnabled()).thenReturn(Boolean.TRUE);
		
		try {
			relayEchoTask.execute(jobExecutionContext);
		} catch (final JobExecutionException ex) {
			fail();
		}

		verify(logger, times(2)).debug(any(String.class));		
		verify(qosMonitorDriver, never()).queryGatekeeperAllCloud();
		verify(qosMonitorDriver, never()).queryGatekeeperCloudAccessTypes(any());
		verify(qosDBService, never()).getInterRelayMeasurement(any(), any(), any());
		verify(qosMonitorDriver, never()).requestGatekeeperInitRelayTest(any());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testExecutePublicKeyNotAvailable() {
		when(arrowheadContext.containsKey(CoreCommonConstants.SERVER_STANDALONE_MODE)).thenReturn(false);
		when(sslProperties.isSslEnabled()).thenReturn(Boolean.TRUE);
		when(arrowheadContext.containsKey(CommonConstants.SERVER_PUBLIC_KEY)).thenReturn(false);
		
		try {
			relayEchoTask.execute(jobExecutionContext);
		} catch (final JobExecutionException ex) {
			fail();
		} catch (final Exception ex) {
			verify(logger, times(2)).debug(any(String.class));		
			verify(qosMonitorDriver, never()).queryGatekeeperAllCloud();
			verify(qosMonitorDriver, never()).queryGatekeeperCloudAccessTypes(any());
			verify(qosDBService, never()).getInterRelayMeasurement(any(), any(), any());
			verify(qosMonitorDriver, never()).requestGatekeeperInitRelayTest(any());
			throw ex;
		}		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testExecuteHaveNoNeighborClouds() {
		when(arrowheadContext.containsKey(CoreCommonConstants.SERVER_STANDALONE_MODE)).thenReturn(false);
		when(sslProperties.isSslEnabled()).thenReturn(Boolean.TRUE);
		when(arrowheadContext.containsKey(CommonConstants.SERVER_PUBLIC_KEY)).thenReturn(true);
		when(arrowheadContext.get(CommonConstants.SERVER_PUBLIC_KEY)).thenReturn(publicKey);
		when(qosMonitorDriver.queryGatekeeperAllCloud()).thenReturn(getOwnCloudInListDTO());
		
		try {
			relayEchoTask.execute(jobExecutionContext);
		} catch (final JobExecutionException ex) {
			fail();
		}
		
		verify(logger, times(5)).debug(any(String.class));		
		verify(qosMonitorDriver, times(1)).queryGatekeeperAllCloud();
		verify(qosMonitorDriver, times(1)).queryGatekeeperCloudAccessTypes(any());
		verify(qosDBService, never()).getInterRelayMeasurement(any(), any(), any());
		verify(qosMonitorDriver, never()).requestGatekeeperInitRelayTest(any());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testExecuteNeighborCloudHaveNoGatekeeperRelays() {
		when(arrowheadContext.containsKey(CoreCommonConstants.SERVER_STANDALONE_MODE)).thenReturn(false);
		when(sslProperties.isSslEnabled()).thenReturn(Boolean.TRUE);
		when(arrowheadContext.containsKey(CommonConstants.SERVER_PUBLIC_KEY)).thenReturn(true);
		when(arrowheadContext.get(CommonConstants.SERVER_PUBLIC_KEY)).thenReturn(publicKey);
		final CloudWithRelaysAndPublicRelaysListResponseDTO allCloud = getOwnCloudInListDTO();
		final CloudWithRelaysAndPublicRelaysResponseDTO neighborCloud = getNeighborCloud(0, 0, 0);
		allCloud.getData().add(neighborCloud);
		allCloud.setCount(allCloud.getData().size());
		when(qosMonitorDriver.queryGatekeeperAllCloud()).thenReturn(allCloud);
		when(qosMonitorDriver.queryGatekeeperCloudAccessTypes(any())).thenReturn(new CloudAccessListResponseDTO(List.of(new CloudAccessResponseDTO(neighborCloud.getName(),
																																				   neighborCloud.getOperator(),
																																				   false)), 1));
		try {
			relayEchoTask.execute(jobExecutionContext);
		} catch (final JobExecutionException ex) {
			fail();
		}
		
		verify(logger, times(5)).debug(any(String.class));
		verify(logger, times(1)).info(any(String.class));
		verify(qosMonitorDriver, times(1)).queryGatekeeperAllCloud();
		verify(qosMonitorDriver, times(1)).queryGatekeeperCloudAccessTypes(any());
		verify(qosDBService, never()).getInterRelayMeasurement(any(), any(), any());
		verify(qosMonitorDriver, never()).requestGatekeeperInitRelayTest(any());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testExecuteNeighborCloudHaveNoGatewayAndPublicRelays() {
		when(arrowheadContext.containsKey(CoreCommonConstants.SERVER_STANDALONE_MODE)).thenReturn(false);
		when(sslProperties.isSslEnabled()).thenReturn(Boolean.TRUE);
		when(arrowheadContext.containsKey(CommonConstants.SERVER_PUBLIC_KEY)).thenReturn(true);
		when(arrowheadContext.get(CommonConstants.SERVER_PUBLIC_KEY)).thenReturn(publicKey);
		final CloudWithRelaysAndPublicRelaysListResponseDTO allCloud = getOwnCloudInListDTO();
		final CloudWithRelaysAndPublicRelaysResponseDTO neighborCloud = getNeighborCloud(1, 0, 0);
		allCloud.getData().add(neighborCloud);
		allCloud.setCount(allCloud.getData().size());
		when(qosMonitorDriver.queryGatekeeperAllCloud()).thenReturn(allCloud);
		when(qosMonitorDriver.queryGatekeeperCloudAccessTypes(any())).thenReturn(new CloudAccessListResponseDTO(List.of(new CloudAccessResponseDTO(neighborCloud.getName(),
																																				   neighborCloud.getOperator(),
																																				   false)), 1));
		try {
			relayEchoTask.execute(jobExecutionContext);
		} catch (final JobExecutionException ex) {
			fail();
		}
		
		verify(logger, times(5)).debug(any(String.class));
		verify(logger, times(1)).info(any(String.class));
		verify(qosMonitorDriver, times(1)).queryGatekeeperAllCloud();
		verify(qosMonitorDriver, times(1)).queryGatekeeperCloudAccessTypes(any());
		verify(qosDBService, never()).getInterRelayMeasurement(any(), any(), any());
		verify(qosMonitorDriver, never()).requestGatekeeperInitRelayTest(any());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testExecuteSecondNeighborCloudHaveNoGatewayRelayMeasurement() {
		when(arrowheadContext.containsKey(CoreCommonConstants.SERVER_STANDALONE_MODE)).thenReturn(false);
		when(sslProperties.isSslEnabled()).thenReturn(Boolean.TRUE);
		when(arrowheadContext.containsKey(CommonConstants.SERVER_PUBLIC_KEY)).thenReturn(true);
		when(arrowheadContext.get(CommonConstants.SERVER_PUBLIC_KEY)).thenReturn(publicKey);
		
		final CloudWithRelaysAndPublicRelaysListResponseDTO allCloud = getOwnCloudInListDTO();
		final CloudWithRelaysAndPublicRelaysResponseDTO neighborCloudWithMeasuerment = getNeighborCloud(1, 1, 0);
		allCloud.getData().add(neighborCloudWithMeasuerment);
		final CloudWithRelaysAndPublicRelaysResponseDTO neighborCloudWithoutMeasuerment = getNeighborCloud(1, 1, 0);
		allCloud.getData().add(neighborCloudWithoutMeasuerment);
		allCloud.setCount(allCloud.getData().size());
		when(qosMonitorDriver.queryGatekeeperAllCloud()).thenReturn(allCloud);
		when(qosMonitorDriver.queryGatekeeperCloudAccessTypes(any())).thenReturn(new CloudAccessListResponseDTO(List.of(new CloudAccessResponseDTO(neighborCloudWithMeasuerment.getName(),
																																				   neighborCloudWithMeasuerment.getOperator(),
																																				   false),
																														new CloudAccessResponseDTO(neighborCloudWithoutMeasuerment.getName(),
																																				   neighborCloudWithoutMeasuerment.getOperator(),
																																				   false)), 1));
		when(qosDBService.getInterRelayMeasurement(eq(neighborCloudWithMeasuerment), eq(neighborCloudWithMeasuerment.getGatewayRelays().get(0)), eq(QoSMeasurementType.RELAY_ECHO)))
		 				 .thenReturn(Optional.of(new QoSInterRelayMeasurement(DTOConverter.convertCloudResponseDTOToCloud(neighborCloudWithMeasuerment),
		 						 											  DTOConverter.convertRelayResponseDTOToRelay(neighborCloudWithMeasuerment.getGatewayRelays().get(0)),
		 						 											  QoSMeasurementType.RELAY_ECHO, ZonedDateTime.now().minusHours(10))));
		when(qosDBService.getInterRelayMeasurement(eq(neighborCloudWithoutMeasuerment), eq(neighborCloudWithoutMeasuerment.getGatewayRelays().get(0)), eq(QoSMeasurementType.RELAY_ECHO)))
						 .thenReturn(Optional.empty());
		
		final ArgumentCaptor<QoSRelayTestProposalRequestDTO> valueCapture = ArgumentCaptor.forClass(QoSRelayTestProposalRequestDTO.class);		
		doNothing().when(qosMonitorDriver).requestGatekeeperInitRelayTest(valueCapture.capture());
		
		try {
			relayEchoTask.execute(jobExecutionContext);
		} catch (final JobExecutionException ex) {
			fail();
		}
		
		verify(logger, times(5)).debug(any(String.class));
		verify(qosMonitorDriver, times(1)).queryGatekeeperAllCloud();
		verify(qosMonitorDriver, times(1)).queryGatekeeperCloudAccessTypes(any());
		verify(qosDBService, times(2)).getInterRelayMeasurement(any(), any(), any());
		verify(qosMonitorDriver, times(1)).requestGatekeeperInitRelayTest(any());
		
		final QoSRelayTestProposalRequestDTO valueCaptured = valueCapture.getValue();
		assertEquals(neighborCloudWithoutMeasuerment.getOperator(), valueCaptured.getTargetCloud().getOperator());
		assertEquals(neighborCloudWithoutMeasuerment.getName(), valueCaptured.getTargetCloud().getName());
		assertEquals(neighborCloudWithoutMeasuerment.getGatewayRelays().get(0).getAddress(), valueCaptured.getRelay().getAddress());
		assertEquals(neighborCloudWithoutMeasuerment.getGatewayRelays().get(0).getPort(), valueCaptured.getRelay().getPort().intValue());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testExecuteSecondNeighborCloudHaveTheOldestGatewayRelayMeasurement() {
		when(arrowheadContext.containsKey(CoreCommonConstants.SERVER_STANDALONE_MODE)).thenReturn(false);
		when(sslProperties.isSslEnabled()).thenReturn(Boolean.TRUE);
		when(arrowheadContext.containsKey(CommonConstants.SERVER_PUBLIC_KEY)).thenReturn(true);
		when(arrowheadContext.get(CommonConstants.SERVER_PUBLIC_KEY)).thenReturn(publicKey);
		
		final CloudWithRelaysAndPublicRelaysListResponseDTO allCloud = getOwnCloudInListDTO();
		final CloudWithRelaysAndPublicRelaysResponseDTO neighborCloudWithMeasuerment = getNeighborCloud(1, 1, 0);
		allCloud.getData().add(neighborCloudWithMeasuerment);
		final CloudWithRelaysAndPublicRelaysResponseDTO neighborCloudWithOldestMeasuerment = getNeighborCloud(1, 1, 0);
		allCloud.getData().add(neighborCloudWithOldestMeasuerment);
		allCloud.setCount(allCloud.getData().size());
		when(qosMonitorDriver.queryGatekeeperAllCloud()).thenReturn(allCloud);
		when(qosMonitorDriver.queryGatekeeperCloudAccessTypes(any())).thenReturn(new CloudAccessListResponseDTO(List.of(new CloudAccessResponseDTO(neighborCloudWithMeasuerment.getName(),
																																				   neighborCloudWithMeasuerment.getOperator(),
																																				   false),
																														new CloudAccessResponseDTO(neighborCloudWithOldestMeasuerment.getName(),
																																neighborCloudWithOldestMeasuerment.getOperator(),
																																				   false)), 1));
		when(qosDBService.getInterRelayMeasurement(eq(neighborCloudWithMeasuerment), eq(neighborCloudWithMeasuerment.getGatewayRelays().get(0)), eq(QoSMeasurementType.RELAY_ECHO)))
		 				 .thenReturn(Optional.of(new QoSInterRelayMeasurement(DTOConverter.convertCloudResponseDTOToCloud(neighborCloudWithMeasuerment),
		 						 											  DTOConverter.convertRelayResponseDTOToRelay(neighborCloudWithMeasuerment.getGatewayRelays().get(0)),
		 						 											  QoSMeasurementType.RELAY_ECHO, ZonedDateTime.now().minusHours(10))));
		when(qosDBService.getInterRelayMeasurement(eq(neighborCloudWithOldestMeasuerment), eq(neighborCloudWithOldestMeasuerment.getGatewayRelays().get(0)), eq(QoSMeasurementType.RELAY_ECHO)))
						 .thenReturn(Optional.of(new QoSInterRelayMeasurement(DTOConverter.convertCloudResponseDTOToCloud(neighborCloudWithMeasuerment),
								 											  DTOConverter.convertRelayResponseDTOToRelay(neighborCloudWithMeasuerment.getGatewayRelays().get(0)),
								 											  QoSMeasurementType.RELAY_ECHO, ZonedDateTime.now().minusHours(20))));
		
		final ArgumentCaptor<QoSRelayTestProposalRequestDTO> valueCapture = ArgumentCaptor.forClass(QoSRelayTestProposalRequestDTO.class);		
		doNothing().when(qosMonitorDriver).requestGatekeeperInitRelayTest(valueCapture.capture());
		
		try {
			relayEchoTask.execute(jobExecutionContext);
		} catch (final JobExecutionException ex) {
			fail();
		}
		
		verify(logger, times(5)).debug(any(String.class));
		verify(qosMonitorDriver, times(1)).queryGatekeeperAllCloud();
		verify(qosMonitorDriver, times(1)).queryGatekeeperCloudAccessTypes(any());
		verify(qosDBService, times(2)).getInterRelayMeasurement(any(), any(), any());
		verify(qosMonitorDriver, times(1)).requestGatekeeperInitRelayTest(any());
		
		final QoSRelayTestProposalRequestDTO valueCaptured = valueCapture.getValue();
		assertEquals(neighborCloudWithOldestMeasuerment.getOperator(), valueCaptured.getTargetCloud().getOperator());
		assertEquals(neighborCloudWithOldestMeasuerment.getName(), valueCaptured.getTargetCloud().getName());
		assertEquals(neighborCloudWithOldestMeasuerment.getGatewayRelays().get(0).getAddress(), valueCaptured.getRelay().getAddress());
		assertEquals(neighborCloudWithOldestMeasuerment.getGatewayRelays().get(0).getPort(), valueCaptured.getRelay().getPort().intValue());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testExecuteSecondNeighborCloudHaveNoPublicRelayMeasurement() {
		when(arrowheadContext.containsKey(CoreCommonConstants.SERVER_STANDALONE_MODE)).thenReturn(false);
		when(sslProperties.isSslEnabled()).thenReturn(Boolean.TRUE);
		when(arrowheadContext.containsKey(CommonConstants.SERVER_PUBLIC_KEY)).thenReturn(true);
		when(arrowheadContext.get(CommonConstants.SERVER_PUBLIC_KEY)).thenReturn(publicKey);
		
		final CloudWithRelaysAndPublicRelaysListResponseDTO allCloud = getOwnCloudInListDTO();
		final CloudWithRelaysAndPublicRelaysResponseDTO neighborCloudWithMeasuerment = getNeighborCloud(1, 0, 1);
		allCloud.getData().add(neighborCloudWithMeasuerment);
		final CloudWithRelaysAndPublicRelaysResponseDTO neighborCloudWithoutMeasuerment = getNeighborCloud(1, 0, 1);
		allCloud.getData().add(neighborCloudWithoutMeasuerment);
		allCloud.setCount(allCloud.getData().size());
		when(qosMonitorDriver.queryGatekeeperAllCloud()).thenReturn(allCloud);
		when(qosMonitorDriver.queryGatekeeperCloudAccessTypes(any())).thenReturn(new CloudAccessListResponseDTO(List.of(new CloudAccessResponseDTO(neighborCloudWithMeasuerment.getName(),
																																				   neighborCloudWithMeasuerment.getOperator(),
																																				   false),
																														new CloudAccessResponseDTO(neighborCloudWithoutMeasuerment.getName(),
																																				   neighborCloudWithoutMeasuerment.getOperator(),
																																				   false)), 1));
		when(qosDBService.getInterRelayMeasurement(eq(neighborCloudWithMeasuerment), eq(neighborCloudWithMeasuerment.getPublicRelays().get(0)), eq(QoSMeasurementType.RELAY_ECHO)))
		 				 .thenReturn(Optional.of(new QoSInterRelayMeasurement(DTOConverter.convertCloudResponseDTOToCloud(neighborCloudWithMeasuerment),
		 						 											  DTOConverter.convertRelayResponseDTOToRelay(neighborCloudWithMeasuerment.getPublicRelays().get(0)),
		 						 											  QoSMeasurementType.RELAY_ECHO, ZonedDateTime.now().minusHours(10))));
		when(qosDBService.getInterRelayMeasurement(eq(neighborCloudWithoutMeasuerment), eq(neighborCloudWithoutMeasuerment.getPublicRelays().get(0)), eq(QoSMeasurementType.RELAY_ECHO)))
						 .thenReturn(Optional.empty());
		
		final ArgumentCaptor<QoSRelayTestProposalRequestDTO> valueCapture = ArgumentCaptor.forClass(QoSRelayTestProposalRequestDTO.class);		
		doNothing().when(qosMonitorDriver).requestGatekeeperInitRelayTest(valueCapture.capture());
		
		try {
			relayEchoTask.execute(jobExecutionContext);
		} catch (final JobExecutionException ex) {
			fail();
		}
		
		verify(logger, times(5)).debug(any(String.class));
		verify(qosMonitorDriver, times(1)).queryGatekeeperAllCloud();
		verify(qosMonitorDriver, times(1)).queryGatekeeperCloudAccessTypes(any());
		verify(qosDBService, times(2)).getInterRelayMeasurement(any(), any(), any());
		verify(qosMonitorDriver, times(1)).requestGatekeeperInitRelayTest(any());
		
		final QoSRelayTestProposalRequestDTO valueCaptured = valueCapture.getValue();
		assertEquals(neighborCloudWithoutMeasuerment.getOperator(), valueCaptured.getTargetCloud().getOperator());
		assertEquals(neighborCloudWithoutMeasuerment.getName(), valueCaptured.getTargetCloud().getName());
		assertEquals(neighborCloudWithoutMeasuerment.getPublicRelays().get(0).getAddress(), valueCaptured.getRelay().getAddress());
		assertEquals(neighborCloudWithoutMeasuerment.getPublicRelays().get(0).getPort(), valueCaptured.getRelay().getPort().intValue());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testExecuteSecondNeighborCloudHaveTheOldestPublicRelayMeasurement() {
		when(arrowheadContext.containsKey(CoreCommonConstants.SERVER_STANDALONE_MODE)).thenReturn(false);
		when(sslProperties.isSslEnabled()).thenReturn(Boolean.TRUE);
		when(arrowheadContext.containsKey(CommonConstants.SERVER_PUBLIC_KEY)).thenReturn(true);
		when(arrowheadContext.get(CommonConstants.SERVER_PUBLIC_KEY)).thenReturn(publicKey);
		
		final CloudWithRelaysAndPublicRelaysListResponseDTO allCloud = getOwnCloudInListDTO();
		final CloudWithRelaysAndPublicRelaysResponseDTO neighborCloudWithMeasuerment = getNeighborCloud(1, 0, 1);
		allCloud.getData().add(neighborCloudWithMeasuerment);
		final CloudWithRelaysAndPublicRelaysResponseDTO neighborCloudWithOldestMeasuerment = getNeighborCloud(1, 0, 1);
		allCloud.getData().add(neighborCloudWithOldestMeasuerment);
		allCloud.setCount(allCloud.getData().size());
		when(qosMonitorDriver.queryGatekeeperAllCloud()).thenReturn(allCloud);
		when(qosMonitorDriver.queryGatekeeperCloudAccessTypes(any())).thenReturn(new CloudAccessListResponseDTO(List.of(new CloudAccessResponseDTO(neighborCloudWithMeasuerment.getName(),
																																				   neighborCloudWithMeasuerment.getOperator(),
																																				   false),
																														new CloudAccessResponseDTO(neighborCloudWithOldestMeasuerment.getName(),
																																neighborCloudWithOldestMeasuerment.getOperator(),
																																				   false)), 1));
		when(qosDBService.getInterRelayMeasurement(eq(neighborCloudWithMeasuerment), eq(neighborCloudWithMeasuerment.getPublicRelays().get(0)), eq(QoSMeasurementType.RELAY_ECHO)))
		 				 .thenReturn(Optional.of(new QoSInterRelayMeasurement(DTOConverter.convertCloudResponseDTOToCloud(neighborCloudWithMeasuerment),
		 						 											  DTOConverter.convertRelayResponseDTOToRelay(neighborCloudWithMeasuerment.getPublicRelays().get(0)),
		 						 											  QoSMeasurementType.RELAY_ECHO, ZonedDateTime.now().minusHours(10))));
		when(qosDBService.getInterRelayMeasurement(eq(neighborCloudWithOldestMeasuerment), eq(neighborCloudWithOldestMeasuerment.getPublicRelays().get(0)), eq(QoSMeasurementType.RELAY_ECHO)))
						 .thenReturn(Optional.of(new QoSInterRelayMeasurement(DTOConverter.convertCloudResponseDTOToCloud(neighborCloudWithMeasuerment),
								 											  DTOConverter.convertRelayResponseDTOToRelay(neighborCloudWithMeasuerment.getPublicRelays().get(0)),
								 											  QoSMeasurementType.RELAY_ECHO, ZonedDateTime.now().minusHours(20))));
		
		final ArgumentCaptor<QoSRelayTestProposalRequestDTO> valueCapture = ArgumentCaptor.forClass(QoSRelayTestProposalRequestDTO.class);		
		doNothing().when(qosMonitorDriver).requestGatekeeperInitRelayTest(valueCapture.capture());
		
		try {
			relayEchoTask.execute(jobExecutionContext);
		} catch (final JobExecutionException ex) {
			fail();
		}
		
		verify(logger, times(5)).debug(any(String.class));
		verify(qosMonitorDriver, times(1)).queryGatekeeperAllCloud();
		verify(qosMonitorDriver, times(1)).queryGatekeeperCloudAccessTypes(any());
		verify(qosDBService, times(2)).getInterRelayMeasurement(any(), any(), any());
		verify(qosMonitorDriver, times(1)).requestGatekeeperInitRelayTest(any());
		
		final QoSRelayTestProposalRequestDTO valueCaptured = valueCapture.getValue();
		assertEquals(neighborCloudWithOldestMeasuerment.getOperator(), valueCaptured.getTargetCloud().getOperator());
		assertEquals(neighborCloudWithOldestMeasuerment.getName(), valueCaptured.getTargetCloud().getName());
		assertEquals(neighborCloudWithOldestMeasuerment.getPublicRelays().get(0).getAddress(), valueCaptured.getRelay().getAddress());
		assertEquals(neighborCloudWithOldestMeasuerment.getPublicRelays().get(0).getPort(), valueCaptured.getRelay().getPort().intValue());
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private PublicKey getPublicKey() {
		final InputStream publicKeyInputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("certificates/qos_monitor.pub");
		return Utilities.getPublicKeyFromPEMFile(publicKeyInputStream);
	}
	
	//-------------------------------------------------------------------------------------------------
	private CloudWithRelaysAndPublicRelaysListResponseDTO getOwnCloudInListDTO() {
		cloudIdCounter++;
		final String nowStr = Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now());
		final List<CloudWithRelaysAndPublicRelaysResponseDTO> data = new ArrayList<>();
		data.add(new CloudWithRelaysAndPublicRelaysResponseDTO(cloudIdCounter, "test-own-op", "test-own-n", true, false, true, null, nowStr, nowStr, null, null, null)); //own cloud
		
		return new CloudWithRelaysAndPublicRelaysListResponseDTO(data, data.size());
	}
	
	//-------------------------------------------------------------------------------------------------
	private CloudWithRelaysAndPublicRelaysResponseDTO getNeighborCloud(final int numOfGKRelays, final int numOfGWRelays, final int numOfPublicRelays) {
		cloudIdCounter++;
		int relayIdCounter = 0;
		final String nowStr = Utilities.convertZonedDateTimeToUTCString(ZonedDateTime.now());
		
		final List<RelayResponseDTO> gatekeeperRelays = new ArrayList<>(numOfGKRelays);
		for (int i = 0; i < numOfGKRelays; i++) {
			relayIdCounter++;
			gatekeeperRelays.add(new RelayResponseDTO(relayIdCounter, relayIdCounter + "-addr", relayIdCounter * 1000, true, false, RelayType.GATEKEEPER_RELAY, nowStr, nowStr));
		}
		
		final List<RelayResponseDTO> gatewayRelays = new ArrayList<>(numOfGWRelays);
		for (int i = 0; i < numOfGWRelays; i++) {
			relayIdCounter++;
			gatewayRelays.add(new RelayResponseDTO(relayIdCounter, relayIdCounter + "-addr", relayIdCounter * 1000, true, true, RelayType.GATEWAY_RELAY, nowStr, nowStr));
		}
		final List<RelayResponseDTO> publicRelays = new ArrayList<>(numOfPublicRelays);
		for (int i = 0; i < numOfPublicRelays; i++) {
			relayIdCounter++;
			publicRelays.add(new RelayResponseDTO(relayIdCounter, relayIdCounter + "-addr", relayIdCounter * 1000, true, false, RelayType.GENERAL_RELAY, nowStr, nowStr));
		}
		
		return new CloudWithRelaysAndPublicRelaysResponseDTO(cloudIdCounter, "test-op-" + cloudIdCounter, "test-n-" + cloudIdCounter, true, true, false, "dsgsdfg" + cloudIdCounter,
														 nowStr, nowStr, gatekeeperRelays, gatewayRelays, publicRelays);
	}
}
