package eu.arrowhead.core.gatekeeper.service;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.mockito.ArgumentMatchers.anyString;

import java.util.List;
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
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.entity.Cloud;
import eu.arrowhead.common.dto.internal.ServiceRegistryListResponseDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.http.HttpService;

@RunWith(SpringRunner.class)
public class GatekeeperDriverQoSTest {
		
	//=================================================================================================
	// members
	
	@InjectMocks
	private GatekeeperDriver testingObject;
	
	@Mock
	private Map<String,Object> arrowheadContext;
	
	@Mock
	private HttpService httpService;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testSendServiceRegistryQueryAllNoUri() {
		when(arrowheadContext.containsKey(CoreCommonConstants.SR_QUERY_ALL)).thenReturn(false);
		
		testingObject.sendServiceRegistryQueryAll();
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testSendServiceRegistryQueryAllWrongUriType() {
		when(arrowheadContext.containsKey(CoreCommonConstants.SR_QUERY_ALL)).thenReturn(true);
		when(arrowheadContext.get(CoreCommonConstants.SR_QUERY_ALL)).thenReturn("uri");
		
		testingObject.sendServiceRegistryQueryAll();
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testSendServiceRegistryQueryAllOk() {
		final UriComponents uri = Utilities.createURI(CommonConstants.HTTPS, "localhost", 1234, "abc");
		
		when(arrowheadContext.containsKey(CoreCommonConstants.SR_QUERY_ALL)).thenReturn(true);
		when(arrowheadContext.get(CoreCommonConstants.SR_QUERY_ALL)).thenReturn(uri);
		when(httpService.sendRequest(uri, HttpMethod.GET, ServiceRegistryListResponseDTO.class)).thenReturn(new ResponseEntity<>(new ServiceRegistryListResponseDTO(), HttpStatus.OK));
		
		testingObject.sendServiceRegistryQueryAll();
		
		verify(httpService, times(1)).sendRequest(uri, HttpMethod.GET, ServiceRegistryListResponseDTO.class);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendAccessTypesCollectionRequestNullCloudName() throws InterruptedException {
		testingObject.sendAccessTypesCollectionRequest(List.of(generateCloudEntity(null, "test-operator")));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendAccessTypesCollectionRequestBlankCloudName() throws InterruptedException {
		testingObject.sendAccessTypesCollectionRequest(List.of(generateCloudEntity("  ", "test-operator")));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendAccessTypesCollectionRequestNullCloudOperator() throws InterruptedException {
		testingObject.sendAccessTypesCollectionRequest(List.of(generateCloudEntity("test-name", null)));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendAccessTypesCollectionRequestBlankCloudOperator() throws InterruptedException {
		testingObject.sendAccessTypesCollectionRequest(List.of(generateCloudEntity("test-name", "  ")));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendSystemAddressCollectionRequestNullCloudName() {
		testingObject.sendSystemAddressCollectionRequest(generateCloudEntity(null, "test-operator"));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendSystemAddressCollectionRequestBlankCloudName() {
		testingObject.sendSystemAddressCollectionRequest(generateCloudEntity("   ", "test-operator"));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendSystemAddressCollectionRequestNullCloudOperator() {
		testingObject.sendSystemAddressCollectionRequest(generateCloudEntity("test-name", null));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendSystemAddressCollectionRequestBlankCloudOperator() {
		testingObject.sendSystemAddressCollectionRequest(generateCloudEntity("test-name", "  "));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testQueryQoSMonitorPublicKeyUriNotFound() {
		when(arrowheadContext.containsKey(anyString())).thenReturn(false);
		
		testingObject.queryQoSMonitorPublicKey();
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testQueryQoSMonitorPublicKeyInvalidUri() {
		when(arrowheadContext.containsKey(anyString())).thenReturn(true);
		when(arrowheadContext.get(anyString())).thenReturn("not an object");
		
		testingObject.queryQoSMonitorPublicKey();
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testQueryQoSMonitorPublicKeyOk() {
		final UriComponents uri = Utilities.createURI(CommonConstants.HTTPS, "localhost", 1234, CommonConstants.QOS_MONITOR_URI + CommonConstants.OP_QOS_MONITOR_KEY_URI);
		
		when(arrowheadContext.containsKey(anyString())).thenReturn(true);
		when(arrowheadContext.get(anyString())).thenReturn(uri);
		when(httpService.sendRequest(uri, HttpMethod.GET, String.class)).thenReturn(new ResponseEntity<>("public key", HttpStatus.OK));
		
		testingObject.queryQoSMonitorPublicKey();
		
		verify(httpService, times(1)).sendRequest(uri, HttpMethod.GET, String.class);
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------		
	private Cloud generateCloudEntity(final String name, final String operator) {
		final Cloud cloud = new Cloud();
		cloud.setName(name);
		cloud.setOperator(operator);
		return cloud;
	}
}
