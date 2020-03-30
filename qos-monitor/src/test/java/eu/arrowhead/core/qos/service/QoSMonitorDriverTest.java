package eu.arrowhead.core.qos.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.util.UriComponents;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.internal.CloudWithRelaysResponseDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.http.HttpService;

@RunWith(SpringRunner.class)
public class QoSMonitorDriverTest {

	//=================================================================================================
	// members
	
	@InjectMocks
	private QoSMonitorDriver testingObject;
	
	@Mock
	private Map<String,Object> arrowheadContext;
	
	@Mock
	private HttpService httpService;

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testQueryGatekeeperCloudInfoOperatorNull() {
		testingObject.queryGatekeeperCloudInfo(null, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testQueryGatekeeperCloudInfoOperatorEmpty() {
		testingObject.queryGatekeeperCloudInfo("", null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testQueryGatekeeperCloudInfoNameNull() {
		testingObject.queryGatekeeperCloudInfo("aitia", null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testQueryGatekeeperCloudInfoNameEmpty() {
		testingObject.queryGatekeeperCloudInfo("aitia", " ");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testQueryGatekeeperCloudInfoUriNotFound() {
		when(arrowheadContext.containsKey(anyString())).thenReturn(false);
		
		testingObject.queryGatekeeperCloudInfo("aitia", "testcloud");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testQueryGatekeeperCloudInfoUriWrongType() {
		when(arrowheadContext.containsKey(anyString())).thenReturn(true);
		when(arrowheadContext.get(anyString())).thenReturn("not an uri object");
		
		testingObject.queryGatekeeperCloudInfo("aitia", "testcloud");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testQueryGatekeeperCloudInfoOk() {
		when(arrowheadContext.containsKey(anyString())).thenReturn(true);
		final UriComponents uri = Utilities.createURI(CommonConstants.HTTPS, "localhost", 1234, "not_important");
		when(arrowheadContext.get(anyString())).thenReturn(uri);
		when(httpService.sendRequest(any(UriComponents.class), eq(HttpMethod.GET), eq(CloudWithRelaysResponseDTO.class))).thenReturn(new ResponseEntity<>(new CloudWithRelaysResponseDTO(), HttpStatus.OK));
		
		testingObject.queryGatekeeperCloudInfo("aitia", "testcloud");
		
		verify(httpService, times(1)).sendRequest(any(UriComponents.class), eq(HttpMethod.GET), eq(CloudWithRelaysResponseDTO.class));
	}
}