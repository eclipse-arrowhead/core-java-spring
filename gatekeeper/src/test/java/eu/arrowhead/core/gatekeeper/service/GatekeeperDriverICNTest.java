/********************************************************************************
 * Copyright (c) 2019 AITIA
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   AITIA - implementation
 *   Arrowhead Consortia - conceptualization
 ********************************************************************************/

package eu.arrowhead.core.gatekeeper.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.jms.BytesMessage;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.Session;
import javax.jms.StreamMessage;
import javax.jms.TemporaryQueue;
import javax.jms.TemporaryTopic;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicSubscriber;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.util.UriComponents;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.core.CoreSystemService;
import eu.arrowhead.common.database.entity.Cloud;
import eu.arrowhead.common.database.entity.Relay;
import eu.arrowhead.common.dto.internal.AuthorizationInterCloudCheckRequestDTO;
import eu.arrowhead.common.dto.internal.AuthorizationInterCloudCheckResponseDTO;
import eu.arrowhead.common.dto.internal.GatewayConsumerConnectionRequestDTO;
import eu.arrowhead.common.dto.internal.GatewayProviderConnectionRequestDTO;
import eu.arrowhead.common.dto.internal.ICNProposalRequestDTO;
import eu.arrowhead.common.dto.internal.ICNProposalResponseDTO;
import eu.arrowhead.common.dto.internal.IdIdListDTO;
import eu.arrowhead.common.dto.internal.RelayRequestDTO;
import eu.arrowhead.common.dto.internal.RelayType;
import eu.arrowhead.common.dto.shared.CloudRequestDTO;
import eu.arrowhead.common.dto.shared.OrchestrationFormRequestDTO;
import eu.arrowhead.common.dto.shared.OrchestrationResponseDTO;
import eu.arrowhead.common.dto.shared.OrchestrationResultDTO;
import eu.arrowhead.common.dto.shared.ServiceDefinitionResponseDTO;
import eu.arrowhead.common.dto.shared.ServiceInterfaceResponseDTO;
import eu.arrowhead.common.dto.shared.ServiceSecurityType;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.common.dto.shared.SystemResponseDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.TimeoutException;
import eu.arrowhead.common.http.HttpService;
import eu.arrowhead.core.gatekeeper.service.matchmaking.RelayMatchmakingAlgorithm;
import eu.arrowhead.core.gatekeeper.service.matchmaking.RelayMatchmakingParameters;
import eu.arrowhead.relay.gatekeeper.GatekeeperRelayClient;
import eu.arrowhead.relay.gatekeeper.GatekeeperRelayResponse;
import eu.arrowhead.relay.gatekeeper.GeneralAdvertisementResult;

@RunWith(SpringRunner.class)
public class GatekeeperDriverICNTest {

	//=================================================================================================
	// members
	
	@InjectMocks
	private GatekeeperDriver testingObject;
	
	@Mock
	private RelayMatchmakingAlgorithm gatekeeperMatchmaker;
	
	@Mock
	private Map<String,Object> arrowheadContext;
	
	@Mock
	private HttpService httpService;
	
	private GatekeeperRelayClient relayClient;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Before
	public void setUp() {
		relayClient = mock(GatekeeperRelayClient.class, "relayClient");
		ReflectionTestUtils.setField(testingObject, "relayClient", relayClient);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendICNProposalTargetCloudNull() {
		testingObject.sendICNProposal(null, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendICNProposalRequestNull() {
		testingObject.sendICNProposal(new Cloud(), null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testSendICNProposalRelayProblem() throws JMSException {
		final Relay relay = new Relay("localhost", 12345, false, false, RelayType.GATEKEEPER_RELAY);
		when(gatekeeperMatchmaker.doMatchmaking(any(RelayMatchmakingParameters.class))).thenReturn(relay);
		when(relayClient.createConnection(any(String.class), anyInt(), anyBoolean())).thenThrow(JMSException.class);
		
		testingObject.sendICNProposal(new Cloud(), new ICNProposalRequestDTO());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = TimeoutException.class)
	public void testSendICNProposalNoAcknowledgement() throws JMSException {
		final Relay relay = new Relay("localhost", 12345, false, false, RelayType.GATEKEEPER_RELAY);
		when(gatekeeperMatchmaker.doMatchmaking(any(RelayMatchmakingParameters.class))).thenReturn(relay);
		when(relayClient.createConnection(any(String.class), anyInt(), anyBoolean())).thenReturn(getTestSession());
		when(relayClient.publishGeneralAdvertisement(any(Session.class), any(String.class), any(String.class))).thenReturn(null);
		
		final Cloud targetCloud = new Cloud("aitia", "testcloud2", true, true, false, "abcd");
		testingObject.sendICNProposal(targetCloud, new ICNProposalRequestDTO());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = TimeoutException.class)
	public void testSendICNProposalNoResponse() throws JMSException {
		final Relay relay = new Relay("localhost", 12345, false, false, RelayType.GATEKEEPER_RELAY);
		when(gatekeeperMatchmaker.doMatchmaking(any(RelayMatchmakingParameters.class))).thenReturn(relay);
		when(relayClient.createConnection(any(String.class), anyInt(), anyBoolean())).thenReturn(getTestSession());
		final GeneralAdvertisementResult gaResult = new GeneralAdvertisementResult(getTestMessageConsumer(), "gatekeeper.testcloud1.aitia.arrowhead.eu", getDummyPublicKey(), "1234");
		when(relayClient.publishGeneralAdvertisement(any(Session.class), any(String.class), any(String.class))).thenReturn(gaResult);
		when(relayClient.sendRequestAndReturnResponse(any(Session.class), any(GeneralAdvertisementResult.class), any())).thenReturn(null);
		
		final Cloud targetCloud = new Cloud("aitia", "testcloud2", true, true, false, "abcd");
		testingObject.sendICNProposal(targetCloud, new ICNProposalRequestDTO());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testSendICNProposalEverythingOK() throws JMSException {
		final Relay relay = new Relay("localhost", 12345, false, false, RelayType.GATEKEEPER_RELAY);
		when(gatekeeperMatchmaker.doMatchmaking(any(RelayMatchmakingParameters.class))).thenReturn(relay);
		when(relayClient.createConnection(any(String.class), anyInt(), anyBoolean())).thenReturn(getTestSession());
		final GeneralAdvertisementResult gaResult = new GeneralAdvertisementResult(getTestMessageConsumer(), "gatekeeper.testcloud1.aitia.arrowhead.eu", getDummyPublicKey(), "1234");
		when(relayClient.publishGeneralAdvertisement(any(Session.class), any(String.class), any(String.class))).thenReturn(gaResult);
		final GatekeeperRelayResponse response = new GatekeeperRelayResponse("1234", CoreCommonConstants.RELAY_MESSAGE_TYPE_ICN_PROPOSAL, new ICNProposalResponseDTO());
		when(relayClient.sendRequestAndReturnResponse(any(Session.class), any(GeneralAdvertisementResult.class), any())).thenReturn(response);
		
		final Cloud targetCloud = new Cloud("aitia", "testcloud2", true, true, false, "abcd");
		final ICNProposalResponseDTO result = testingObject.sendICNProposal(targetCloud, new ICNProposalRequestDTO());
		
		Assert.assertNotNull(result);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testQueryOrchestratorFormNull() {
		testingObject.queryOrchestrator(null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testQueryOrchestratorURINotFound() {
		when(arrowheadContext.containsKey(CoreSystemService.ORCHESTRATION_SERVICE.getServiceDefinition() + CoreCommonConstants.URI_SUFFIX)).thenReturn(false);
		
		testingObject.queryOrchestrator(new OrchestrationFormRequestDTO());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testQueryOrchestratorURIWrongType() {
		final String key = CoreSystemService.ORCHESTRATION_SERVICE.getServiceDefinition() + CoreCommonConstants.URI_SUFFIX;
		when(arrowheadContext.containsKey(key)).thenReturn(true);
		when(arrowheadContext.get(key)).thenReturn("abcd");
		
		testingObject.queryOrchestrator(new OrchestrationFormRequestDTO());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class) 
	public void testQueryAuthorizationBasedOnOchestrationResponseRequestCloudNull() {
		testingObject.queryAuthorizationBasedOnOrchestrationResponse(null, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class) 
	public void testQueryAuthorizationBasedOnOchestrationResponseOrchestrationResponseNull() {
		testingObject.queryAuthorizationBasedOnOrchestrationResponse(new CloudRequestDTO(), null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class) 
	public void testQueryAuthorizationBasedOnOchestrationResponseOrchestrationResponseEmpty() {
		testingObject.queryAuthorizationBasedOnOrchestrationResponse(new CloudRequestDTO(), new OrchestrationResponseDTO());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class) 
	public void testQueryAuthorizationBasedOnOchestrationResponseURINotFound() {
		when(arrowheadContext.containsKey(CoreSystemService.AUTH_CONTROL_INTER_SERVICE.getServiceDefinition() + CoreCommonConstants.URI_SUFFIX)).thenReturn(false);
		
		testingObject.queryAuthorizationBasedOnOrchestrationResponse(new CloudRequestDTO(), new OrchestrationResponseDTO(List.of(new OrchestrationResultDTO())));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class) 
	public void testQueryAuthorizationBasedOnOchestrationResponseURIWrongType() {
		final String key = CoreSystemService.AUTH_CONTROL_INTER_SERVICE.getServiceDefinition() + CoreCommonConstants.URI_SUFFIX;
		when(arrowheadContext.containsKey(key)).thenReturn(true);
		when(arrowheadContext.get(key)).thenReturn("1234");
		
		testingObject.queryAuthorizationBasedOnOrchestrationResponse(new CloudRequestDTO(), new OrchestrationResponseDTO(List.of(new OrchestrationResultDTO())));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test 
	public void testQueryAuthorizationBasedOnOchestrationResponseNoAccess() {
		final String key = CoreSystemService.AUTH_CONTROL_INTER_SERVICE.getServiceDefinition() + CoreCommonConstants.URI_SUFFIX;
		when(arrowheadContext.containsKey(key)).thenReturn(true);
		when(arrowheadContext.get(key)).thenReturn(Utilities.createURI(CommonConstants.HTTPS, "localhost", 1234, "/a"));
		final ResponseEntity<AuthorizationInterCloudCheckResponseDTO> response = new ResponseEntity<>(new AuthorizationInterCloudCheckResponseDTO(), HttpStatus.OK);
		when(httpService.sendRequest(any(UriComponents.class), eq(HttpMethod.POST), eq(AuthorizationInterCloudCheckResponseDTO.class), any(AuthorizationInterCloudCheckRequestDTO.class)))
																																												.thenReturn(response);
		
		final OrchestrationResponseDTO authorizedResponse = testingObject.queryAuthorizationBasedOnOrchestrationResponse(new CloudRequestDTO(), new OrchestrationResponseDTO(getTestOrchestrationResults()));
		
		Assert.assertTrue(authorizedResponse.getResponse().isEmpty());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test 
	public void testQueryAuthorizationBasedOnOchestrationResponseFiltered() {
		final String key = CoreSystemService.AUTH_CONTROL_INTER_SERVICE.getServiceDefinition() + CoreCommonConstants.URI_SUFFIX;
		when(arrowheadContext.containsKey(key)).thenReturn(true);
		when(arrowheadContext.get(key)).thenReturn(Utilities.createURI(CommonConstants.HTTPS, "localhost", 1234, "/a"));
		final ResponseEntity<AuthorizationInterCloudCheckResponseDTO> response = new ResponseEntity<>(getTestAuthorizationCheckResponse(), HttpStatus.OK);
		when(httpService.sendRequest(any(UriComponents.class), eq(HttpMethod.POST), eq(AuthorizationInterCloudCheckResponseDTO.class), any(AuthorizationInterCloudCheckRequestDTO.class)))
																																												.thenReturn(response);
		
		final OrchestrationResponseDTO orchestrationResponse = new OrchestrationResponseDTO(getTestOrchestrationResults());
		Assert.assertEquals(2, orchestrationResponse.getResponse().size());
		
		final OrchestrationResponseDTO authorizedResponse = testingObject.queryAuthorizationBasedOnOrchestrationResponse(new CloudRequestDTO(), orchestrationResponse);
		
		Assert.assertEquals(1, authorizedResponse.getResponse().size());
		Assert.assertEquals(2, authorizedResponse.getResponse().get(0).getProvider().getId());
		Assert.assertEquals(1, authorizedResponse.getResponse().get(0).getInterfaces().size());
		Assert.assertEquals(100, authorizedResponse.getResponse().get(0).getInterfaces().get(0).getId());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testQueryGatewayPublicKeyURINotFound() {
		when(arrowheadContext.containsKey(CoreSystemService.GATEWAY_PUBLIC_KEY_SERVICE.getServiceDefinition() + CoreCommonConstants.URI_SUFFIX)).thenReturn(false);
		
		testingObject.queryGatewayPublicKey();
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testQueryGatewayPublicKeyURIWrongType() {
		when(arrowheadContext.containsKey(CoreSystemService.GATEWAY_PUBLIC_KEY_SERVICE.getServiceDefinition() + CoreCommonConstants.URI_SUFFIX)).thenReturn(true);
		when(arrowheadContext.get(CoreSystemService.GATEWAY_PUBLIC_KEY_SERVICE.getServiceDefinition() + CoreCommonConstants.URI_SUFFIX)).thenReturn("not an URI");
		
		testingObject.queryGatewayPublicKey();
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConnectProviderRequestNull() {
		testingObject.connectProvider(null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConnectProviderRelayNull() {
		final GatewayProviderConnectionRequestDTO request = getTestGatewayProviderConnectionRequestDTO();
		request.setRelay(null);
		
		testingObject.connectProvider(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConnectProviderRelayAddressNull() {
		final GatewayProviderConnectionRequestDTO request = getTestGatewayProviderConnectionRequestDTO();
		request.getRelay().setAddress(null);
		
		testingObject.connectProvider(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConnectProviderRelayAddressEmpty() {
		final GatewayProviderConnectionRequestDTO request = getTestGatewayProviderConnectionRequestDTO();
		request.getRelay().setAddress(" ");
		
		testingObject.connectProvider(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConnectProviderRelayPortNull() {
		final GatewayProviderConnectionRequestDTO request = getTestGatewayProviderConnectionRequestDTO();
		request.getRelay().setPort(null);
		
		testingObject.connectProvider(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConnectProviderRelayPortTooLow() {
		final GatewayProviderConnectionRequestDTO request = getTestGatewayProviderConnectionRequestDTO();
		request.getRelay().setPort(-192);
		
		testingObject.connectProvider(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConnectProviderRelayPortTooHigh() {
		final GatewayProviderConnectionRequestDTO request = getTestGatewayProviderConnectionRequestDTO();
		request.getRelay().setPort(192426);
		
		testingObject.connectProvider(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConnectProviderRelayTypeNull() {
		final GatewayProviderConnectionRequestDTO request = getTestGatewayProviderConnectionRequestDTO();
		request.getRelay().setType(null);
		
		testingObject.connectProvider(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConnectProviderRelayTypeEmpty() {
		final GatewayProviderConnectionRequestDTO request = getTestGatewayProviderConnectionRequestDTO();
		request.getRelay().setType("\r\t");
		
		testingObject.connectProvider(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConnectProviderRelayTypeInvalid1() {
		final GatewayProviderConnectionRequestDTO request = getTestGatewayProviderConnectionRequestDTO();
		request.getRelay().setType("invalid");
		
		testingObject.connectProvider(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConnectProviderRelayTypeInvalid2() {
		final GatewayProviderConnectionRequestDTO request = getTestGatewayProviderConnectionRequestDTO();
		request.getRelay().setType(RelayType.GATEKEEPER_RELAY.name());
		
		testingObject.connectProvider(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConnectProviderConsumerNull() {
		final GatewayProviderConnectionRequestDTO request = getTestGatewayProviderConnectionRequestDTO();
		request.setConsumer(null);
		
		testingObject.connectProvider(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConnectProviderConsumerNameNull() {
		final GatewayProviderConnectionRequestDTO request = getTestGatewayProviderConnectionRequestDTO();
		request.getConsumer().setSystemName(null);
		
		testingObject.connectProvider(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConnectProviderConsumerNameEmpty() {
		final GatewayProviderConnectionRequestDTO request = getTestGatewayProviderConnectionRequestDTO();
		request.getConsumer().setSystemName("");
		
		testingObject.connectProvider(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConnectProviderConsumerAddressNull() {
		final GatewayProviderConnectionRequestDTO request = getTestGatewayProviderConnectionRequestDTO();
		request.getConsumer().setAddress(null);
		
		testingObject.connectProvider(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConnectProviderConsumerAddressEmpty() {
		final GatewayProviderConnectionRequestDTO request = getTestGatewayProviderConnectionRequestDTO();
		request.getConsumer().setAddress(" ");
		
		testingObject.connectProvider(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConnectProviderConsumerPortNull() {
		final GatewayProviderConnectionRequestDTO request = getTestGatewayProviderConnectionRequestDTO();
		request.getConsumer().setPort(null);
		
		testingObject.connectProvider(request);
	}

	// we skip the port interval check tests because it uses the same method than relay check
	// we skip the provider check tests because it uses the same method than consumer check
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConnectProviderConsumerCloudNull() {
		final GatewayProviderConnectionRequestDTO request = getTestGatewayProviderConnectionRequestDTO();
		request.setConsumerCloud(null);
		
		testingObject.connectProvider(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConnectProviderConsumerCloudOperatorNull() {
		final GatewayProviderConnectionRequestDTO request = getTestGatewayProviderConnectionRequestDTO();
		request.getConsumerCloud().setOperator(null);
		
		testingObject.connectProvider(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConnectProviderConsumerCloudOperatorEmpty() {
		final GatewayProviderConnectionRequestDTO request = getTestGatewayProviderConnectionRequestDTO();
		request.getConsumerCloud().setOperator(" ");
		
		testingObject.connectProvider(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConnectProviderConsumerCloudNameNull() {
		final GatewayProviderConnectionRequestDTO request = getTestGatewayProviderConnectionRequestDTO();
		request.getConsumerCloud().setName(null);
		
		testingObject.connectProvider(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConnectProviderConsumerCloudNameEmpty() {
		final GatewayProviderConnectionRequestDTO request = getTestGatewayProviderConnectionRequestDTO();
		request.getConsumerCloud().setName("");
		
		testingObject.connectProvider(request);
	}
	
	// we skip the provider cloud check tests because it uses the same method than consumer cloud check
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConnectProviderServiceDefinitionNull() {
		final GatewayProviderConnectionRequestDTO request = getTestGatewayProviderConnectionRequestDTO();
		request.setServiceDefinition(null);
		
		testingObject.connectProvider(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConnectProviderServiceDefinitionEmpty() {
		final GatewayProviderConnectionRequestDTO request = getTestGatewayProviderConnectionRequestDTO();
		request.setServiceDefinition("");
		
		testingObject.connectProvider(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConnectProviderConsumerGWPublicKeyNull() {
		final GatewayProviderConnectionRequestDTO request = getTestGatewayProviderConnectionRequestDTO();
		request.setConsumerGWPublicKey(null);
		
		testingObject.connectProvider(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConnectProviderConsumerGWPublicKeyEmpty() {
		final GatewayProviderConnectionRequestDTO request = getTestGatewayProviderConnectionRequestDTO();
		request.setConsumerGWPublicKey("\n\t\r");
		
		testingObject.connectProvider(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testConnectProviderURINotFound() {
		final GatewayProviderConnectionRequestDTO request = getTestGatewayProviderConnectionRequestDTO();
		when(arrowheadContext.containsKey(CoreSystemService.GATEWAY_PROVIDER_SERVICE.getServiceDefinition() + CoreCommonConstants.URI_SUFFIX)).thenReturn(false);
		
		testingObject.connectProvider(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testConnectProviderURIWrongType() {
		final GatewayProviderConnectionRequestDTO request = getTestGatewayProviderConnectionRequestDTO();
		when(arrowheadContext.containsKey(CoreSystemService.GATEWAY_PROVIDER_SERVICE.getServiceDefinition() + CoreCommonConstants.URI_SUFFIX)).thenReturn(true);
		when(arrowheadContext.get(CoreSystemService.GATEWAY_PROVIDER_SERVICE.getServiceDefinition() + CoreCommonConstants.URI_SUFFIX)).thenReturn("not a valid URI");
		
		testingObject.connectProvider(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConnectConsumerRequestNull() {
		testingObject.connectConsumer(null);
	}

	// we skip the relay check tests because it uses the same method than in connectProvider
	// we skip the consumer check tests because it uses the same method than in connectProvider
	// we skip the provider check tests because it uses the same method than in connectProvider
	// we skip the consumer cloud check tests because it uses the same method than in connectProvider
	// we skip the provider cloud check tests because it uses the same method than in connectProvider
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConnectConsumerServiceDefinitionNull() {
		final GatewayConsumerConnectionRequestDTO request = getTestGatewayConsumerConnectionRequestDTO();
		request.setServiceDefinition(null);
		
		testingObject.connectConsumer(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConnectConsumerServiceDefinitionEmpty() {
		final GatewayConsumerConnectionRequestDTO request = getTestGatewayConsumerConnectionRequestDTO();
		request.setServiceDefinition("          ");
		
		testingObject.connectConsumer(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConnectConsumerProviderGWPublicKeyNull() {
		final GatewayConsumerConnectionRequestDTO request = getTestGatewayConsumerConnectionRequestDTO();
		request.setProviderGWPublicKey(null);
		
		testingObject.connectConsumer(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConnectConsumerProviderGWPublicKeyEmpty() {
		final GatewayConsumerConnectionRequestDTO request = getTestGatewayConsumerConnectionRequestDTO();
		request.setProviderGWPublicKey("");
		
		testingObject.connectConsumer(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConnectConsumerQueueIdNull() {
		final GatewayConsumerConnectionRequestDTO request = getTestGatewayConsumerConnectionRequestDTO();
		request.setQueueId(null);
		
		testingObject.connectConsumer(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConnectConsumerQueueIdEmpty() {
		final GatewayConsumerConnectionRequestDTO request = getTestGatewayConsumerConnectionRequestDTO();
		request.setQueueId("");
		
		testingObject.connectConsumer(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConnectConsumerPeerNameNull() {
		final GatewayConsumerConnectionRequestDTO request = getTestGatewayConsumerConnectionRequestDTO();
		request.setPeerName(null);
		
		testingObject.connectConsumer(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testConnectConsumerPeerNameEmpty() {
		final GatewayConsumerConnectionRequestDTO request = getTestGatewayConsumerConnectionRequestDTO();
		request.setPeerName("   ");
		
		testingObject.connectConsumer(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testConnectConsumerURINotFound() {
		final GatewayConsumerConnectionRequestDTO request = getTestGatewayConsumerConnectionRequestDTO();
		when(arrowheadContext.containsKey(CoreSystemService.GATEWAY_CONSUMER_SERVICE.getServiceDefinition() + CoreCommonConstants.URI_SUFFIX)).thenReturn(false);
		
		testingObject.connectConsumer(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testConnectConsumerURIWrongType() {
		final GatewayConsumerConnectionRequestDTO request = getTestGatewayConsumerConnectionRequestDTO();
		when(arrowheadContext.containsKey(CoreSystemService.GATEWAY_CONSUMER_SERVICE.getServiceDefinition() + CoreCommonConstants.URI_SUFFIX)).thenReturn(true);
		when(arrowheadContext.get(CoreSystemService.GATEWAY_CONSUMER_SERVICE.getServiceDefinition() + CoreCommonConstants.URI_SUFFIX)).thenReturn("not URI");
		
		testingObject.connectConsumer(request);
	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testGetGatewayHostURINotFound() {
		when(arrowheadContext.containsKey(CoreSystemService.GATEWAY_PUBLIC_KEY_SERVICE.getServiceDefinition() + CoreCommonConstants.URI_SUFFIX)).thenReturn(false);
		
		testingObject.getGatewayHost();
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testGetGatewayHostURIWrongType() {
		when(arrowheadContext.containsKey(CoreSystemService.GATEWAY_PUBLIC_KEY_SERVICE.getServiceDefinition() + CoreCommonConstants.URI_SUFFIX)).thenReturn(true);
		when(arrowheadContext.get(CoreSystemService.GATEWAY_PUBLIC_KEY_SERVICE.getServiceDefinition() + CoreCommonConstants.URI_SUFFIX)).thenReturn("not an URI");
		
		testingObject.getGatewayHost();
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("serial")
	private PublicKey getDummyPublicKey() {
		return new PublicKey() {
			
			//-------------------------------------------------------------------------------------------------
			public String getFormat() { return null; }
			public byte[] getEncoded() { return null; }
			public String getAlgorithm() { return null; }
		};
	}

	//-------------------------------------------------------------------------------------------------
	private Session getTestSession() {
		return new Session() {

			//-------------------------------------------------------------------------------------------------
			public void close() throws JMSException {}
			public Queue createQueue(final String queueName) throws JMSException { return null;	}
			public Topic createTopic(final String topicName) throws JMSException { return null;	}
			public MessageConsumer createConsumer(final Destination destination) throws JMSException { return null; }
			public MessageProducer createProducer(final Destination destination) throws JMSException { return null;	}
			public TextMessage createTextMessage(final String text) throws JMSException { return null; }
			public BytesMessage createBytesMessage() throws JMSException { return null; }
			public MapMessage createMapMessage() throws JMSException { return null; }
			public Message createMessage() throws JMSException { return null; }
			public ObjectMessage createObjectMessage() throws JMSException { return null; }
			public ObjectMessage createObjectMessage(final Serializable object) throws JMSException { return null; }
			public StreamMessage createStreamMessage() throws JMSException { return null; }
			public TextMessage createTextMessage() throws JMSException { return null; }
			public boolean getTransacted() throws JMSException { return false; 	}
			public int getAcknowledgeMode() throws JMSException { return 0; }
			public void commit() throws JMSException {}
			public void rollback() throws JMSException {}
			public void recover() throws JMSException {}
			public MessageListener getMessageListener() throws JMSException { return null; }
			public void setMessageListener(final MessageListener listener) throws JMSException {}
			public void run() {}
			public MessageConsumer createConsumer(final Destination destination, final String messageSelector) throws JMSException { return null; }
			public MessageConsumer createConsumer(final Destination destination, final String messageSelector, final boolean noLocal) throws JMSException { return null; }
			public MessageConsumer createSharedConsumer(final Topic topic, final String sharedSubscriptionName) throws JMSException { return null; }
			public MessageConsumer createSharedConsumer(final Topic topic, final String sharedSubscriptionName, final String messageSelector) throws JMSException { return null; }
			public TopicSubscriber createDurableSubscriber(final Topic topic, final String name) throws JMSException { return null; }
			public TopicSubscriber createDurableSubscriber(final Topic topic, final String name, final String messageSelector, final boolean noLocal) throws JMSException { return null; }
			public MessageConsumer createDurableConsumer(final Topic topic, final String name) throws JMSException { return null; }
			public MessageConsumer createDurableConsumer(final Topic topic, final String name, final String messageSelector, final boolean noLocal) throws JMSException { return null; }
			public MessageConsumer createSharedDurableConsumer(final Topic topic, final String name) throws JMSException { return null; }
			public MessageConsumer createSharedDurableConsumer(final Topic topic, final String name, final String messageSelector) throws JMSException { return null;	}
			public QueueBrowser createBrowser(final Queue queue) throws JMSException { return null; }
			public QueueBrowser createBrowser(final Queue queue, final String messageSelector) throws JMSException { return null; }
			public TemporaryQueue createTemporaryQueue() throws JMSException { return null; }
			public TemporaryTopic createTemporaryTopic() throws JMSException { return null;	}
			public void unsubscribe(final String name) throws JMSException {}

		};
	}
	
	//-------------------------------------------------------------------------------------------------
	private MessageConsumer getTestMessageConsumer() {
		return new MessageConsumer() {

			//-------------------------------------------------------------------------------------------------
			public Message receive(final long timeout) throws JMSException { return null; }
			public void close() throws JMSException {}
			public String getMessageSelector() throws JMSException { return null; }
			public MessageListener getMessageListener() throws JMSException { return null; }
			public void setMessageListener(final MessageListener listener) throws JMSException {}
			public Message receive() throws JMSException { return null; }
			public Message receiveNoWait() throws JMSException { return null; }
		};
	}
	
	//-------------------------------------------------------------------------------------------------
	private List<OrchestrationResultDTO> getTestOrchestrationResults() {
		final List<OrchestrationResultDTO> result = new ArrayList<>(2);
		
		final ServiceDefinitionResponseDTO service = new ServiceDefinitionResponseDTO(22, "test-service", null, null);
		final ServiceInterfaceResponseDTO intfs1 = new ServiceInterfaceResponseDTO(100, "HTTP-SECURE-JSON", null, null);
		final ServiceInterfaceResponseDTO intfs2 = new ServiceInterfaceResponseDTO(101, "HTTP-SECURE-XML", null, null);
		
		final SystemResponseDTO provider1 = new SystemResponseDTO(1, "testSystem1", "localhost", 1234, "abcd", null, null, null);
		final List<ServiceInterfaceResponseDTO> intfsList1 = new ArrayList<>(1);
		intfsList1.add(intfs1);
		final OrchestrationResultDTO dto1 = new OrchestrationResultDTO(provider1, service, "/", ServiceSecurityType.CERTIFICATE, null, intfsList1, 1);
		result.add(dto1);
		
		final SystemResponseDTO provider2 = new SystemResponseDTO(2, "testSystem2", "localhost", 11234, "abcd", null, null, null);
		final List<ServiceInterfaceResponseDTO> intfsList2 = new ArrayList<>(2);
		intfsList2.add(intfs1);
		intfsList2.add(intfs2);
		final OrchestrationResultDTO dto2 = new OrchestrationResultDTO(provider2, service, "/", ServiceSecurityType.CERTIFICATE, null, intfsList2, 1);
		result.add(dto2);
		
		return result;
	}
	
	//-------------------------------------------------------------------------------------------------
	private AuthorizationInterCloudCheckResponseDTO getTestAuthorizationCheckResponse() {
		final AuthorizationInterCloudCheckResponseDTO result = new AuthorizationInterCloudCheckResponseDTO();
		result.setAuthorizedProviderIdsWithInterfaceIds(List.of(new IdIdListDTO(2L, List.of(100L))));
		
		return result;
	}
	
	//-------------------------------------------------------------------------------------------------
	private GatewayProviderConnectionRequestDTO getTestGatewayProviderConnectionRequestDTO() {
		final RelayRequestDTO relay = new RelayRequestDTO("localhost", 1234, null, false, false, RelayType.GATEWAY_RELAY.name());
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
	
	//-------------------------------------------------------------------------------------------------
	private GatewayConsumerConnectionRequestDTO getTestGatewayConsumerConnectionRequestDTO() {
		final RelayRequestDTO relay = new RelayRequestDTO("localhost", 1234, null, false, false, RelayType.GATEWAY_RELAY.name());
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
		
		return new GatewayConsumerConnectionRequestDTO(relay, "queueId", "peerName", "providerGWPublicKey", consumer, provider, consumerCloud, providerCloud, "test-service");
	}
}