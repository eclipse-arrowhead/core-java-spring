package eu.arrowhead.core.gateway.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Map;

import javax.jms.JMSException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.dto.CloudRequestDTO;
import eu.arrowhead.common.dto.GatewayProviderConnectionRequestDTO;
import eu.arrowhead.common.dto.RelayRequestDTO;
import eu.arrowhead.common.dto.RelayType;
import eu.arrowhead.common.dto.SystemRequestDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.core.gateway.relay.GatewayRelayClient;

@RunWith(SpringRunner.class)
public class GatewayServiceTest {

	//=================================================================================================
	// members
	
	@InjectMocks
	private GatewayService testingObject;
	
	@Mock
	private Map<String,Object> arrowheadContext;
	
	private GatewayRelayClient relayClient;

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Before
	public void setUp() {
		relayClient = mock(GatewayRelayClient.class, "relayClient");
		ReflectionTestUtils.setField(testingObject, "relayClient", relayClient);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testOnApplicationEventNoCommonName() {
		when(arrowheadContext.containsKey(CommonConstants.SERVER_COMMON_NAME)).thenReturn(false);
		testingObject.onApplicationEvent(null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ClassCastException.class)
	public void testOnApplicationEventCommonNameWrongType() {
		when(arrowheadContext.containsKey(CommonConstants.SERVER_COMMON_NAME)).thenReturn(true);
		when(arrowheadContext.get(CommonConstants.SERVER_COMMON_NAME)).thenReturn(new Object());
		testingObject.onApplicationEvent(null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testOnApplicationEventNoPublicKey() {
		when(arrowheadContext.containsKey(CommonConstants.SERVER_COMMON_NAME)).thenReturn(true);
		when(arrowheadContext.get(CommonConstants.SERVER_COMMON_NAME)).thenReturn("gateway.testcloud2.aitia.arrowhead.eu");
		when(arrowheadContext.containsKey(CommonConstants.SERVER_PUBLIC_KEY)).thenReturn(false);
		testingObject.onApplicationEvent(null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ClassCastException.class)
	public void testOnApplicationEventPublicKeyWrongType() {
		when(arrowheadContext.containsKey(CommonConstants.SERVER_COMMON_NAME)).thenReturn(true);
		when(arrowheadContext.get(CommonConstants.SERVER_COMMON_NAME)).thenReturn("gateway.testcloud2.aitia.arrowhead.eu");
		when(arrowheadContext.containsKey(CommonConstants.SERVER_PUBLIC_KEY)).thenReturn(true);
		when(arrowheadContext.get(CommonConstants.SERVER_PUBLIC_KEY)).thenReturn("not a public key");
		testingObject.onApplicationEvent(null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("serial")
	@Test(expected = ArrowheadException.class)
	public void testOnApplicationEventNoPrivateKey() {
		when(arrowheadContext.containsKey(CommonConstants.SERVER_COMMON_NAME)).thenReturn(true);
		when(arrowheadContext.get(CommonConstants.SERVER_COMMON_NAME)).thenReturn("gateway.testcloud2.aitia.arrowhead.eu");
		when(arrowheadContext.containsKey(CommonConstants.SERVER_PUBLIC_KEY)).thenReturn(true);
		when(arrowheadContext.get(CommonConstants.SERVER_PUBLIC_KEY)).thenReturn(new PublicKey() {
			public String getFormat() { return null; }
			public byte[] getEncoded() { return null; }
			public String getAlgorithm() { return null; }
		});
		when(arrowheadContext.containsKey(CommonConstants.SERVER_PRIVATE_KEY)).thenReturn(false);
		testingObject.onApplicationEvent(null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("serial")
	@Test(expected = ClassCastException.class)
	public void testOnApplicationEventPrivateKeyWrongType() {
		when(arrowheadContext.containsKey(CommonConstants.SERVER_COMMON_NAME)).thenReturn(true);
		when(arrowheadContext.get(CommonConstants.SERVER_COMMON_NAME)).thenReturn("gateway.testcloud2.aitia.arrowhead.eu");
		when(arrowheadContext.containsKey(CommonConstants.SERVER_PUBLIC_KEY)).thenReturn(true);
		when(arrowheadContext.get(CommonConstants.SERVER_PUBLIC_KEY)).thenReturn(new PublicKey() {
			public String getFormat() { return null; }
			public byte[] getEncoded() { return null; }
			public String getAlgorithm() { return null; }
		});
		when(arrowheadContext.containsKey(CommonConstants.SERVER_PRIVATE_KEY)).thenReturn(true);
		when(arrowheadContext.get(CommonConstants.SERVER_PRIVATE_KEY)).thenReturn("not a private key");
		testingObject.onApplicationEvent(null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("serial")
	@Test
	public void testOnApplicationEventEverythingOK() {
		when(arrowheadContext.containsKey(CommonConstants.SERVER_COMMON_NAME)).thenReturn(true);
		when(arrowheadContext.get(CommonConstants.SERVER_COMMON_NAME)).thenReturn("gateway.testcloud2.aitia.arrowhead.eu");
		when(arrowheadContext.containsKey(CommonConstants.SERVER_PUBLIC_KEY)).thenReturn(true);
		when(arrowheadContext.get(CommonConstants.SERVER_PUBLIC_KEY)).thenReturn(new PublicKey() {
			public String getFormat() { return null; }
			public byte[] getEncoded() { return null; }
			public String getAlgorithm() { return null; }
		});
		when(arrowheadContext.containsKey(CommonConstants.SERVER_PRIVATE_KEY)).thenReturn(true);
		when(arrowheadContext.get(CommonConstants.SERVER_PRIVATE_KEY)).thenReturn(new PrivateKey() {
			public String getFormat() { return null; }
			public byte[] getEncoded() { return null; }
			public String getAlgorithm() { return null;	}
		});
		testingObject.onApplicationEvent(null);
		final Object relayClient = ReflectionTestUtils.getField(testingObject, "relayClient");
		Assert.assertNotNull(relayClient);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testConnectProviderRequestNull() {
		testingObject.connectProvider(null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testConnectProviderRelayNull() {
		final GatewayProviderConnectionRequestDTO request = getTestGatewayProviderConnectionRequestDTO();
		request.setRelay(null);
		
		testingObject.connectProvider(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testConnectProviderRelayAddressNull() {
		final GatewayProviderConnectionRequestDTO request = getTestGatewayProviderConnectionRequestDTO();
		request.getRelay().setAddress(null);
		
		testingObject.connectProvider(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testConnectProviderRelayAddressEmpty() {
		final GatewayProviderConnectionRequestDTO request = getTestGatewayProviderConnectionRequestDTO();
		request.getRelay().setAddress(" ");
		
		testingObject.connectProvider(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testConnectProviderRelayPortNull() {
		final GatewayProviderConnectionRequestDTO request = getTestGatewayProviderConnectionRequestDTO();
		request.getRelay().setPort(null);
		
		testingObject.connectProvider(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testConnectProviderRelayPortTooLow() {
		final GatewayProviderConnectionRequestDTO request = getTestGatewayProviderConnectionRequestDTO();
		request.getRelay().setPort(-192);
		
		testingObject.connectProvider(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testConnectProviderRelayPortTooHigh() {
		final GatewayProviderConnectionRequestDTO request = getTestGatewayProviderConnectionRequestDTO();
		request.getRelay().setPort(192426);
		
		testingObject.connectProvider(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testConnectProviderRelayTypeNull() {
		final GatewayProviderConnectionRequestDTO request = getTestGatewayProviderConnectionRequestDTO();
		request.getRelay().setType(null);
		
		testingObject.connectProvider(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testConnectProviderRelayTypeEmpty() {
		final GatewayProviderConnectionRequestDTO request = getTestGatewayProviderConnectionRequestDTO();
		request.getRelay().setType("\r\t");
		
		testingObject.connectProvider(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testConnectProviderRelayTypeInvalid1() {
		final GatewayProviderConnectionRequestDTO request = getTestGatewayProviderConnectionRequestDTO();
		request.getRelay().setType("invalid");
		
		testingObject.connectProvider(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testConnectProviderRelayTypeInvalid2() {
		final GatewayProviderConnectionRequestDTO request = getTestGatewayProviderConnectionRequestDTO();
		request.getRelay().setType(RelayType.GATEKEEPER_RELAY.name());
		
		testingObject.connectProvider(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testConnectProviderConsumerNull() {
		final GatewayProviderConnectionRequestDTO request = getTestGatewayProviderConnectionRequestDTO();
		request.setConsumer(null);
		
		testingObject.connectProvider(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testConnectProviderConsumerNameNull() {
		final GatewayProviderConnectionRequestDTO request = getTestGatewayProviderConnectionRequestDTO();
		request.getConsumer().setSystemName(null);
		
		testingObject.connectProvider(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testConnectProviderConsumerNameEmpty() {
		final GatewayProviderConnectionRequestDTO request = getTestGatewayProviderConnectionRequestDTO();
		request.getConsumer().setSystemName("");
		
		testingObject.connectProvider(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testConnectProviderConsumerAddressNull() {
		final GatewayProviderConnectionRequestDTO request = getTestGatewayProviderConnectionRequestDTO();
		request.getConsumer().setAddress(null);
		
		testingObject.connectProvider(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testConnectProviderConsumerAddressEmpty() {
		final GatewayProviderConnectionRequestDTO request = getTestGatewayProviderConnectionRequestDTO();
		request.getConsumer().setAddress(" ");
		
		testingObject.connectProvider(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testConnectProviderConsumerPortNull() {
		final GatewayProviderConnectionRequestDTO request = getTestGatewayProviderConnectionRequestDTO();
		request.getConsumer().setPort(null);
		
		testingObject.connectProvider(request);
	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testConnectProviderConsumerPortTooLow() {
		final GatewayProviderConnectionRequestDTO request = getTestGatewayProviderConnectionRequestDTO();
		request.getConsumer().setPort(-192);
		
		testingObject.connectProvider(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testConnectProviderConsumerPortTooHigh() {
		final GatewayProviderConnectionRequestDTO request = getTestGatewayProviderConnectionRequestDTO();
		request.getConsumer().setPort(192426);
		
		testingObject.connectProvider(request);
	}
	
	// we skip the provider check tests because it uses the same method than consumer check
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testConnectProviderConsumerCloudNull() {
		final GatewayProviderConnectionRequestDTO request = getTestGatewayProviderConnectionRequestDTO();
		request.setConsumerCloud(null);
		
		testingObject.connectProvider(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testConnectProviderConsumerCloudOperatorNull() {
		final GatewayProviderConnectionRequestDTO request = getTestGatewayProviderConnectionRequestDTO();
		request.getConsumerCloud().setOperator(null);
		
		testingObject.connectProvider(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testConnectProviderConsumerCloudOperatorEmpty() {
		final GatewayProviderConnectionRequestDTO request = getTestGatewayProviderConnectionRequestDTO();
		request.getConsumerCloud().setOperator(" ");
		
		testingObject.connectProvider(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testConnectProviderConsumerCloudNameNull() {
		final GatewayProviderConnectionRequestDTO request = getTestGatewayProviderConnectionRequestDTO();
		request.getConsumerCloud().setName(null);
		
		testingObject.connectProvider(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testConnectProviderConsumerCloudNameEmpty() {
		final GatewayProviderConnectionRequestDTO request = getTestGatewayProviderConnectionRequestDTO();
		request.getConsumerCloud().setName("");
		
		testingObject.connectProvider(request);
	}
	
	// we skip the provider cloud check tests because it uses the same method than consumer cloud check
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testConnectProviderServiceDefinitionNull() {
		final GatewayProviderConnectionRequestDTO request = getTestGatewayProviderConnectionRequestDTO();
		request.setServiceDefinition(null);
		
		testingObject.connectProvider(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testConnectProviderServiceDefinitionEmpty() {
		final GatewayProviderConnectionRequestDTO request = getTestGatewayProviderConnectionRequestDTO();
		request.setServiceDefinition("");
		
		testingObject.connectProvider(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testConnectProviderConsumerGWPublicKeyNull() {
		final GatewayProviderConnectionRequestDTO request = getTestGatewayProviderConnectionRequestDTO();
		request.setConsumerGWPublicKey(null);
		
		testingObject.connectProvider(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testConnectProviderConsumerGWPublicKeyEmpty() {
		final GatewayProviderConnectionRequestDTO request = getTestGatewayProviderConnectionRequestDTO();
		request.setConsumerGWPublicKey("\n\t\r");
		
		testingObject.connectProvider(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testConnectProviderCannotConnectRelay() throws JMSException {
		final GatewayProviderConnectionRequestDTO request = getTestGatewayProviderConnectionRequestDTO();
		when(relayClient.createConnection(any(String.class), anyInt())).thenThrow(new JMSException("test"));
		
		testingObject.connectProvider(request);
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private GatewayProviderConnectionRequestDTO getTestGatewayProviderConnectionRequestDTO() {
		final RelayRequestDTO relay = new RelayRequestDTO("localhost", 1234, false, false, RelayType.GATEWAY_RELAY.name());
		final SystemRequestDTO consumer = new SystemRequestDTO();
		consumer.setSystemName("consumer");
		consumer.setAddress("abc.de");
		consumer.setPort(22001);
		consumer.setAuthenticationInfo("consAuth");
		final SystemRequestDTO provider = new SystemRequestDTO();
		provider.setSystemName("provider");
		provider.setAddress("fgh.de");
		provider.setPort(22002);
		provider.setAuthenticationInfo("provAuth");
		final CloudRequestDTO consumerCloud = new CloudRequestDTO();
		consumerCloud.setName("testcloud1");
		consumerCloud.setOperator("aitia");
		final CloudRequestDTO providerCloud = new CloudRequestDTO();
		providerCloud.setName("testcloud2");
		providerCloud.setOperator("elte");
		
		return new GatewayProviderConnectionRequestDTO(relay, consumer, provider, consumerCloud, providerCloud, "test-service", "consumerGWPublicKey");
	}


}