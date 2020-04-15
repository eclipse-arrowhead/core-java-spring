package eu.arrowhead.core.qos.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.when;

import java.util.List;

import org.icmp4j.IcmpPingResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;

import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.core.qos.database.service.QoSDBService;
import eu.arrowhead.core.qos.measurement.properties.PingMeasurementProperties;

@RunWith(SpringRunner.class)
public class PingServiceTest {

	//=================================================================================================
	// members
	
	@InjectMocks
	private PingService pingService;
	
	@Mock
	private PingMeasurementProperties pingMeasurementProperties;
	
	@Mock
	private QoSDBService qosDBService;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGetPingResponseListNullAddress() {
		pingService.getPingResponseList(null);
	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGetPingResponseListBlankAddress() {
		pingService.getPingResponseList("    ");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetPingResponseListInvalidAddress() {
		when(pingMeasurementProperties.getTimeToRepeat()).thenReturn(1);
		when(pingMeasurementProperties.getTimeout()).thenReturn(2000);
		when(pingMeasurementProperties.getPacketSize()).thenReturn(32);
		
		final List<IcmpPingResponse> responseList = pingService.getPingResponseList("invalid"); //IcmpPingUtil.executePingRequest(request) throws RuntimeException
		assertEquals(1, responseList.size());
		assertFalse(responseList.get(0).getSuccessFlag());
	}
}
