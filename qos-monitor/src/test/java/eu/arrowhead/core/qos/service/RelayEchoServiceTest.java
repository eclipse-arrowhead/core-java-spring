package eu.arrowhead.core.qos.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;

import eu.arrowhead.common.database.entity.Cloud;
import eu.arrowhead.common.database.entity.QoSInterRelayEchoMeasurement;
import eu.arrowhead.common.database.entity.QoSInterRelayMeasurement;
import eu.arrowhead.common.database.entity.Relay;
import eu.arrowhead.common.dto.internal.CloudWithRelaysResponseDTO;
import eu.arrowhead.common.dto.internal.QoSInterRelayEchoMeasurementListResponseDTO;
import eu.arrowhead.common.dto.internal.RelayType;
import eu.arrowhead.common.dto.shared.CloudRequestDTO;
import eu.arrowhead.common.dto.shared.QoSMeasurementType;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.core.qos.database.service.QoSDBService;

@RunWith(SpringRunner.class)
public class RelayEchoServiceTest {

	//=================================================================================================
	// members
	
	@InjectMocks
	private RelayEchoService relayEchoService;
	
	@Mock
	private QoSMonitorDriver gosMonitorDriver;
	
	@Mock
	private QoSDBService qosDBService;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGetInterRelayEchoMeasurementsNullCloudResponse() {
		relayEchoService.getInterRelayEchoMeasurements(null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGetInterRelayEchoMeasurementsNullCloudOperator() {
		final CloudRequestDTO cloud = new CloudRequestDTO();
		cloud.setOperator(null);
		cloud.setName("test-n");
		relayEchoService.getInterRelayEchoMeasurements(cloud);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGetInterRelayEchoMeasurementsBlankCloudOperator() {
		final CloudRequestDTO cloud = new CloudRequestDTO();
		cloud.setOperator("  ");
		cloud.setName("test-n");
		relayEchoService.getInterRelayEchoMeasurements(cloud);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGetInterRelayEchoMeasurementsNullCloudName() {
		final CloudRequestDTO cloud = new CloudRequestDTO();
		cloud.setOperator("test-op");
		cloud.setName(null);
		relayEchoService.getInterRelayEchoMeasurements(cloud);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGetInterRelayEchoMeasurementsBlankCloudName() {
		final CloudRequestDTO cloud = new CloudRequestDTO();
		cloud.setOperator("test-op");
		cloud.setName("  ");
		relayEchoService.getInterRelayEchoMeasurements(cloud);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetInterRelayEchoMeasurementsOk() {
		final CloudRequestDTO cloudReq = new CloudRequestDTO();
		cloudReq.setOperator("test-op");
		cloudReq.setName("test-n");
		
		final Cloud cloud = new Cloud("test-op", "test-n", true, true, false, "ydfbgsdh");
		cloud.setId(1l);
		cloud.setCreatedAt(ZonedDateTime.now());
		cloud.setUpdatedAt(ZonedDateTime.now());
		final Relay measuredRelay = new Relay("1.1.1.1", 10000, true, false, RelayType.GATEWAY_RELAY);
		measuredRelay.setId(4l);
		measuredRelay.setCreatedAt(ZonedDateTime.now());
		measuredRelay.setUpdatedAt(ZonedDateTime.now());
		
		final QoSInterRelayMeasurement measurementFinished = new QoSInterRelayMeasurement(cloud, measuredRelay, QoSMeasurementType.RELAY_ECHO, ZonedDateTime.now());
		final QoSInterRelayMeasurement measurementNew= new QoSInterRelayMeasurement(cloud, new Relay("2.2.2.2", 20000, true, false, RelayType.GATEWAY_RELAY),
				  																	QoSMeasurementType.RELAY_ECHO, null);
		
		when(gosMonitorDriver.queryGatekeeperCloudInfo(anyString(), anyString())).thenReturn(new CloudWithRelaysResponseDTO());
		when(qosDBService.getInterRelayMeasurementByCloud(any())).thenReturn(List.of(measurementFinished, measurementNew));
		final QoSInterRelayEchoMeasurement relayMeasurementFinished = new QoSInterRelayEchoMeasurement();
		relayMeasurementFinished.setMeasurement(measurementFinished);
		when(qosDBService.getInterRelayEchoMeasurementByMeasurement(eq(measurementFinished))).thenReturn(Optional.of(relayMeasurementFinished));
		when(qosDBService.getInterRelayEchoMeasurementByMeasurement(eq(measurementNew))).thenReturn(Optional.empty());
		
		final QoSInterRelayEchoMeasurementListResponseDTO result = relayEchoService.getInterRelayEchoMeasurements(cloudReq);
		
		assertEquals(1, result.getData().size());
		assertEquals(measuredRelay.getId(), result.getData().get(0).getMeasurement().getRelay().getId());
	}
}
