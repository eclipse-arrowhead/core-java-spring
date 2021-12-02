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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.security.PublicKey;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import eu.arrowhead.common.database.entity.CloudGatekeeperRelay;
import eu.arrowhead.common.database.entity.Relay;
import eu.arrowhead.common.dto.internal.QoSMonitorSenderConnectionRequestDTO;
import eu.arrowhead.common.dto.internal.QoSRelayTestProposalRequestDTO;
import eu.arrowhead.common.dto.internal.QoSRelayTestProposalResponseDTO;
import eu.arrowhead.common.dto.internal.RelayRequestDTO;
import eu.arrowhead.common.dto.internal.RelayType;
import eu.arrowhead.common.dto.internal.ServiceRegistryListResponseDTO;
import eu.arrowhead.common.dto.shared.CloudRequestDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.common.exception.TimeoutException;
import eu.arrowhead.common.http.HttpService;
import eu.arrowhead.core.gatekeeper.service.matchmaking.RelayMatchmakingAlgorithm;
import eu.arrowhead.core.gatekeeper.service.matchmaking.RelayMatchmakingParameters;
import eu.arrowhead.relay.gatekeeper.GatekeeperRelayClient;
import eu.arrowhead.relay.gatekeeper.GatekeeperRelayResponse;
import eu.arrowhead.relay.gatekeeper.GeneralAdvertisementResult;

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
	
	@Mock
	private GatekeeperRelayClient relayClient;
	
	@Mock
	private RelayMatchmakingAlgorithm gatekeeperMatchmaker;

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
		final UriComponents uri = Utilities.createURI(CommonConstants.HTTPS, "localhost", 1234, CommonConstants.QOSMONITOR_URI + CommonConstants.OP_QOSMONITOR_KEY_URI);
		
		when(arrowheadContext.containsKey(anyString())).thenReturn(true);
		when(arrowheadContext.get(anyString())).thenReturn(uri);
		when(httpService.sendRequest(uri, HttpMethod.GET, String.class)).thenReturn(new ResponseEntity<>("\"public key\"", HttpStatus.OK));
		
		testingObject.queryQoSMonitorPublicKey();
		
		verify(httpService, times(1)).sendRequest(uri, HttpMethod.GET, String.class);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testSendQoSRelayTestProposalRequestNull() {
		testingObject.sendQoSRelayTestProposal(null, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendQoSRelayTestProposalRelayNull() {
		final QoSRelayTestProposalRequestDTO request = new QoSRelayTestProposalRequestDTO();
		
		testingObject.sendQoSRelayTestProposal(request, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendQoSRelayTestProposalRelayAddressNull() {
		final RelayRequestDTO relay = new RelayRequestDTO();
		
		final QoSRelayTestProposalRequestDTO request = new QoSRelayTestProposalRequestDTO();
		request.setRelay(relay);
		
		testingObject.sendQoSRelayTestProposal(request, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendQoSRelayTestProposalRelayAddressEmpty() {
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("");
		
		final QoSRelayTestProposalRequestDTO request = new QoSRelayTestProposalRequestDTO();
		request.setRelay(relay);
		
		testingObject.sendQoSRelayTestProposal(request, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendQoSRelayTestProposalRelayPort() {
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("localhost");
		
		final QoSRelayTestProposalRequestDTO request = new QoSRelayTestProposalRequestDTO();
		request.setRelay(relay);
		
		testingObject.sendQoSRelayTestProposal(request, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendQoSRelayTestProposalRelayPortTooLow() {
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("localhost");
		relay.setPort(-3);
		
		final QoSRelayTestProposalRequestDTO request = new QoSRelayTestProposalRequestDTO();
		request.setRelay(relay);
		
		testingObject.sendQoSRelayTestProposal(request, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendQoSRelayTestProposalRelayPortTooHigh() {
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("localhost");
		relay.setPort(300000);
		
		final QoSRelayTestProposalRequestDTO request = new QoSRelayTestProposalRequestDTO();
		request.setRelay(relay);
		
		testingObject.sendQoSRelayTestProposal(request, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendQoSRelayTestProposalRelayTypeNull() {
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("localhost");
		relay.setPort(3000);
		
		final QoSRelayTestProposalRequestDTO request = new QoSRelayTestProposalRequestDTO();
		request.setRelay(relay);
		
		testingObject.sendQoSRelayTestProposal(request, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendQoSRelayTestProposalRelayTypeEmpty() {
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("localhost");
		relay.setPort(3000);
		relay.setType("");
		
		final QoSRelayTestProposalRequestDTO request = new QoSRelayTestProposalRequestDTO();
		request.setRelay(relay);
		
		testingObject.sendQoSRelayTestProposal(request, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendQoSRelayTestProposalRelayTypeInvalid() {
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("localhost");
		relay.setPort(3000);
		relay.setType("invalid");
		
		final QoSRelayTestProposalRequestDTO request = new QoSRelayTestProposalRequestDTO();
		request.setRelay(relay);
		
		testingObject.sendQoSRelayTestProposal(request, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendQoSRelayTestProposalRelayTypeGatekeeper() {
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("localhost");
		relay.setPort(3000);
		relay.setType("GATEKEEPER_RELAY");
		
		final QoSRelayTestProposalRequestDTO request = new QoSRelayTestProposalRequestDTO();
		request.setRelay(relay);
		
		testingObject.sendQoSRelayTestProposal(request, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testSendQoSRelayTestProposalSenderQoSMonitorPublicKeyNull() {
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("localhost");
		relay.setPort(3000);
		relay.setType("GATEWAY_RELAY");
		
		final QoSRelayTestProposalRequestDTO request = new QoSRelayTestProposalRequestDTO();
		request.setRelay(relay);
		
		testingObject.sendQoSRelayTestProposal(request, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testSendQoSRelayTestProposalSenderQoSMonitorPublicKeyEmpty() {
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("localhost");
		relay.setPort(3000);
		relay.setType("GATEWAY_RELAY");
		
		final QoSRelayTestProposalRequestDTO request = new QoSRelayTestProposalRequestDTO();
		request.setRelay(relay);
		request.setSenderQoSMonitorPublicKey("");
		
		testingObject.sendQoSRelayTestProposal(request, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendQoSRelayTestProposalTargetCloudNull() {
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("localhost");
		relay.setPort(3000);
		relay.setType("GATEWAY_RELAY");
		
		final QoSRelayTestProposalRequestDTO request = new QoSRelayTestProposalRequestDTO();
		request.setRelay(relay);
		request.setSenderQoSMonitorPublicKey("valid public key");
		
		testingObject.sendQoSRelayTestProposal(request, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendQoSRelayTestProposalTargetCloudOperatorNull() {
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("localhost");
		relay.setPort(3000);
		relay.setType("GATEWAY_RELAY");
		
		final QoSRelayTestProposalRequestDTO request = new QoSRelayTestProposalRequestDTO();
		request.setRelay(relay);
		request.setSenderQoSMonitorPublicKey("valid public key");
		
		final Cloud cloud = new Cloud();
		
		testingObject.sendQoSRelayTestProposal(request, cloud);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendQoSRelayTestProposalTargetCloudOperatorEmpty() {
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("localhost");
		relay.setPort(3000);
		relay.setType("GATEWAY_RELAY");
		
		final QoSRelayTestProposalRequestDTO request = new QoSRelayTestProposalRequestDTO();
		request.setRelay(relay);
		request.setSenderQoSMonitorPublicKey("valid public key");
		
		final Cloud cloud = new Cloud();
		cloud.setOperator(" ");
		
		testingObject.sendQoSRelayTestProposal(request, cloud);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendQoSRelayTestProposalTargetCloudNameNull() {
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("localhost");
		relay.setPort(3000);
		relay.setType("GATEWAY_RELAY");
		
		final QoSRelayTestProposalRequestDTO request = new QoSRelayTestProposalRequestDTO();
		request.setRelay(relay);
		request.setSenderQoSMonitorPublicKey("valid public key");
		
		final Cloud cloud = new Cloud();
		cloud.setOperator("aitia");
		
		testingObject.sendQoSRelayTestProposal(request, cloud);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendQoSRelayTestProposalTargetCloudNameEmpty() {
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("localhost");
		relay.setPort(3000);
		relay.setType("GATEWAY_RELAY");
		
		final QoSRelayTestProposalRequestDTO request = new QoSRelayTestProposalRequestDTO();
		request.setRelay(relay);
		request.setSenderQoSMonitorPublicKey("valid public key");
		
		final Cloud cloud = new Cloud();
		cloud.setOperator("aitia");
		cloud.setName(" ");
		
		testingObject.sendQoSRelayTestProposal(request, cloud);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendQoSRelayTestProposalTargetCloudGatekeeperRelaysNull() {
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("localhost");
		relay.setPort(3000);
		relay.setType("GATEWAY_RELAY");
		
		final QoSRelayTestProposalRequestDTO request = new QoSRelayTestProposalRequestDTO();
		request.setRelay(relay);
		request.setSenderQoSMonitorPublicKey("valid public key");
		
		final Cloud cloud = new Cloud();
		cloud.setOperator("aitia");
		cloud.setName("testcloud");
		
		testingObject.sendQoSRelayTestProposal(request, cloud);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSendQoSRelayTestProposalTargetCloudGatekeeperRelaysEmpty() {
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("localhost");
		relay.setPort(3000);
		relay.setType("GATEWAY_RELAY");
		
		final QoSRelayTestProposalRequestDTO request = new QoSRelayTestProposalRequestDTO();
		request.setRelay(relay);
		request.setSenderQoSMonitorPublicKey("valid public key");
		
		final Cloud cloud = new Cloud();
		cloud.setOperator("aitia");
		cloud.setName("testcloud");
		cloud.setGatekeeperRelays(Set.of());
		
		testingObject.sendQoSRelayTestProposal(request, cloud);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testSendQoSRelayTestProposalRelayConnectionProblem() throws JMSException {
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("localhost");
		relay.setPort(3000);
		relay.setType("GATEWAY_RELAY");
		
		final QoSRelayTestProposalRequestDTO request = new QoSRelayTestProposalRequestDTO();
		request.setRelay(relay);
		request.setSenderQoSMonitorPublicKey("valid public key");
		
		final Cloud cloud = new Cloud();
		cloud.setOperator("aitia");
		cloud.setName("testcloud");
		cloud.setGatekeeperRelays(Set.of(new CloudGatekeeperRelay()));
		
		when(gatekeeperMatchmaker.doMatchmaking(any(RelayMatchmakingParameters.class))).thenReturn(new Relay("localhost", 6123, true, false, RelayType.GENERAL_RELAY));
		when(relayClient.createConnection(anyString(), anyInt(), anyBoolean())).thenThrow(JMSException.class);
		
		testingObject.sendQoSRelayTestProposal(request, cloud);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = TimeoutException.class)
	public void testSendQoSRelayTestProposalPublishGeneralAdvertisementTimeout() throws JMSException {
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("localhost");
		relay.setPort(3000);
		relay.setType("GATEWAY_RELAY");
		
		final QoSRelayTestProposalRequestDTO request = new QoSRelayTestProposalRequestDTO();
		request.setRelay(relay);
		request.setSenderQoSMonitorPublicKey("valid public key");
		
		final Cloud cloud = new Cloud();
		cloud.setOperator("aitia");
		cloud.setName("testcloud");
		cloud.setGatekeeperRelays(Set.of(new CloudGatekeeperRelay()));
		cloud.setAuthenticationInfo("authInfo");
		
		when(gatekeeperMatchmaker.doMatchmaking(any(RelayMatchmakingParameters.class))).thenReturn(new Relay("localhost", 6123, true, false, RelayType.GENERAL_RELAY));
		when(relayClient.createConnection(anyString(), anyInt(), anyBoolean())).thenReturn(getDummySession());
		when(relayClient.publishGeneralAdvertisement(any(Session.class), anyString(), anyString())).thenReturn(null);
		
		testingObject.sendQoSRelayTestProposal(request, cloud);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = TimeoutException.class)
	public void testSendQoSRelayTestProposalResponseTimeout() throws JMSException {
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("localhost");
		relay.setPort(3000);
		relay.setType("GATEWAY_RELAY");
		
		final QoSRelayTestProposalRequestDTO request = new QoSRelayTestProposalRequestDTO();
		request.setRelay(relay);
		request.setSenderQoSMonitorPublicKey("valid public key");
		
		final Cloud cloud = new Cloud();
		cloud.setOperator("aitia");
		cloud.setName("testcloud");
		cloud.setGatekeeperRelays(Set.of(new CloudGatekeeperRelay()));
		cloud.setAuthenticationInfo("authInfo");
		
		when(gatekeeperMatchmaker.doMatchmaking(any(RelayMatchmakingParameters.class))).thenReturn(new Relay("localhost", 6123, true, false, RelayType.GENERAL_RELAY));
		when(relayClient.createConnection(anyString(), anyInt(), anyBoolean())).thenReturn(getDummySession());
		when(relayClient.publishGeneralAdvertisement(any(Session.class), anyString(), anyString())).thenReturn(new GeneralAdvertisementResult(getDummyMessageConsumer(), "peerCN",
																																			  getDummyPublicKey(), "sessionId"));
		when(relayClient.sendRequestAndReturnResponse(any(Session.class), any(GeneralAdvertisementResult.class), any())).thenReturn(null);
		
		testingObject.sendQoSRelayTestProposal(request, cloud);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testSendQoSRelayTestProposalOk() throws JMSException {
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("localhost");
		relay.setPort(3000);
		relay.setType("GATEWAY_RELAY");
		
		final QoSRelayTestProposalRequestDTO request = new QoSRelayTestProposalRequestDTO();
		request.setRelay(relay);
		request.setSenderQoSMonitorPublicKey("valid public key");
		
		final Cloud cloud = new Cloud();
		cloud.setOperator("aitia");
		cloud.setName("testcloud");
		cloud.setGatekeeperRelays(Set.of(new CloudGatekeeperRelay()));
		cloud.setAuthenticationInfo("authInfo");
		
		when(gatekeeperMatchmaker.doMatchmaking(any(RelayMatchmakingParameters.class))).thenReturn(new Relay("localhost", 6123, true, false, RelayType.GENERAL_RELAY));
		when(relayClient.createConnection(anyString(), anyInt(), anyBoolean())).thenReturn(getDummySession());
		when(relayClient.publishGeneralAdvertisement(any(Session.class), anyString(), anyString())).thenReturn(new GeneralAdvertisementResult(getDummyMessageConsumer(), "peerCN",
																																			  getDummyPublicKey(), "sessionId"));
		when(relayClient.sendRequestAndReturnResponse(any(Session.class), any(GeneralAdvertisementResult.class), any())).thenReturn(new GatekeeperRelayResponse("sessionId",
																					CoreCommonConstants.RELAY_MESSAGE_TYPE_QOS_RELAY_TEST, new QoSRelayTestProposalResponseDTO()));
		
		testingObject.sendQoSRelayTestProposal(request, cloud);
		
		verify(gatekeeperMatchmaker, times(1)).doMatchmaking(any(RelayMatchmakingParameters.class));
		verify(relayClient, times(1)).createConnection(anyString(), anyInt(), anyBoolean());
		verify(relayClient, times(1)).publishGeneralAdvertisement(any(Session.class), anyString(), anyString());
		verify(relayClient, times(1)).sendRequestAndReturnResponse(any(Session.class), any(GeneralAdvertisementResult.class), any());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testInitRelayTestRequestNull() {
		testingObject.initRelayTest(null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testInitRelayTestTargetCloudNull() {
		final QoSMonitorSenderConnectionRequestDTO request = new QoSMonitorSenderConnectionRequestDTO();
		
		testingObject.initRelayTest(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testInitRelayTestTargetCloudOperatorNull() {
		final CloudRequestDTO targetCloud = new CloudRequestDTO();
		
		final QoSMonitorSenderConnectionRequestDTO request = new QoSMonitorSenderConnectionRequestDTO();
		request.setTargetCloud(targetCloud);
		
		testingObject.initRelayTest(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testInitRelayTestTargetCloudOperatorEmpty() {
		final CloudRequestDTO targetCloud = new CloudRequestDTO();
		targetCloud.setOperator(" ");
		
		final QoSMonitorSenderConnectionRequestDTO request = new QoSMonitorSenderConnectionRequestDTO();
		request.setTargetCloud(targetCloud);
		
		testingObject.initRelayTest(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testInitRelayTestTargetCloudNameNull() {
		final CloudRequestDTO targetCloud = new CloudRequestDTO();
		targetCloud.setOperator("aitia");
		
		final QoSMonitorSenderConnectionRequestDTO request = new QoSMonitorSenderConnectionRequestDTO();
		request.setTargetCloud(targetCloud);
		
		testingObject.initRelayTest(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testInitRelayTestTargetCloudNameEmpty() {
		final CloudRequestDTO targetCloud = new CloudRequestDTO();
		targetCloud.setOperator("aitia");
		targetCloud.setName(" ");
		
		final QoSMonitorSenderConnectionRequestDTO request = new QoSMonitorSenderConnectionRequestDTO();
		request.setTargetCloud(targetCloud);
		
		testingObject.initRelayTest(request);
	}
	
	// skip relay validation tests because it is the same method that was used in sendQoSRelayTestProposal()
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testInitRelayTestQueueIdNull() {
		final CloudRequestDTO targetCloud = new CloudRequestDTO();
		targetCloud.setOperator("aitia");
		targetCloud.setName("testcloud");
		
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("localhost");
		relay.setPort(3000);
		relay.setType("GATEWAY_RELAY");
		
		final QoSMonitorSenderConnectionRequestDTO request = new QoSMonitorSenderConnectionRequestDTO();
		request.setTargetCloud(targetCloud);
		request.setRelay(relay);
		
		testingObject.initRelayTest(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testInitRelayTestQueueIdEmpty() {
		final CloudRequestDTO targetCloud = new CloudRequestDTO();
		targetCloud.setOperator("aitia");
		targetCloud.setName("testcloud");
		
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("localhost");
		relay.setPort(3000);
		relay.setType("GATEWAY_RELAY");
		
		final QoSMonitorSenderConnectionRequestDTO request = new QoSMonitorSenderConnectionRequestDTO();
		request.setTargetCloud(targetCloud);
		request.setRelay(relay);
		request.setQueueId(" ");
		
		testingObject.initRelayTest(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testInitRelayTestPeerNameNull() {
		final CloudRequestDTO targetCloud = new CloudRequestDTO();
		targetCloud.setOperator("aitia");
		targetCloud.setName("testcloud");
		
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("localhost");
		relay.setPort(3000);
		relay.setType("GATEWAY_RELAY");
		
		final QoSMonitorSenderConnectionRequestDTO request = new QoSMonitorSenderConnectionRequestDTO();
		request.setTargetCloud(targetCloud);
		request.setRelay(relay);
		request.setQueueId("queueId");
		
		testingObject.initRelayTest(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testInitRelayTestPeerNameEmpty() {
		final CloudRequestDTO targetCloud = new CloudRequestDTO();
		targetCloud.setOperator("aitia");
		targetCloud.setName("testcloud");
		
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("localhost");
		relay.setPort(3000);
		relay.setType("GATEWAY_RELAY");
		
		final QoSMonitorSenderConnectionRequestDTO request = new QoSMonitorSenderConnectionRequestDTO();
		request.setTargetCloud(targetCloud);
		request.setRelay(relay);
		request.setQueueId("queueId");
		request.setPeerName(" ");
		
		testingObject.initRelayTest(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testInitRelayTestReceiverQoSMonitorPublicKeyNull() {
		final CloudRequestDTO targetCloud = new CloudRequestDTO();
		targetCloud.setOperator("aitia");
		targetCloud.setName("testcloud");
		
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("localhost");
		relay.setPort(3000);
		relay.setType("GATEWAY_RELAY");
		
		final QoSMonitorSenderConnectionRequestDTO request = new QoSMonitorSenderConnectionRequestDTO();
		request.setTargetCloud(targetCloud);
		request.setRelay(relay);
		request.setQueueId("queueId");
		request.setPeerName("peer");
		
		testingObject.initRelayTest(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testInitRelayTestReceiverQoSMonitorPublicKeyEmpty() {
		final CloudRequestDTO targetCloud = new CloudRequestDTO();
		targetCloud.setOperator("aitia");
		targetCloud.setName("testcloud");
		
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("localhost");
		relay.setPort(3000);
		relay.setType("GATEWAY_RELAY");
		
		final QoSMonitorSenderConnectionRequestDTO request = new QoSMonitorSenderConnectionRequestDTO();
		request.setTargetCloud(targetCloud);
		request.setRelay(relay);
		request.setQueueId("queueId");
		request.setPeerName("peer");
		request.setReceiverQoSMonitorPublicKey(" ");
		
		testingObject.initRelayTest(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testInitRelayTestUriNotFound() {
		final CloudRequestDTO targetCloud = new CloudRequestDTO();
		targetCloud.setOperator("aitia");
		targetCloud.setName("testcloud");
		
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("localhost");
		relay.setPort(3000);
		relay.setType("GATEWAY_RELAY");
		
		final QoSMonitorSenderConnectionRequestDTO request = new QoSMonitorSenderConnectionRequestDTO();
		request.setTargetCloud(targetCloud);
		request.setRelay(relay);
		request.setQueueId("queueId");
		request.setPeerName("peer");
		request.setReceiverQoSMonitorPublicKey("valid key");
		
		when(arrowheadContext.containsKey(anyString())).thenReturn(false);
		
		testingObject.initRelayTest(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testInitRelayTestInvalidUri() {
		final CloudRequestDTO targetCloud = new CloudRequestDTO();
		targetCloud.setOperator("aitia");
		targetCloud.setName("testcloud");
		
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("localhost");
		relay.setPort(3000);
		relay.setType("GATEWAY_RELAY");
		
		final QoSMonitorSenderConnectionRequestDTO request = new QoSMonitorSenderConnectionRequestDTO();
		request.setTargetCloud(targetCloud);
		request.setRelay(relay);
		request.setQueueId("queueId");
		request.setPeerName("peer");
		request.setReceiverQoSMonitorPublicKey("valid key");
		
		when(arrowheadContext.containsKey(anyString())).thenReturn(true);
		when(arrowheadContext.get(anyString())).thenReturn("not an URI object");
		
		testingObject.initRelayTest(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitRelayTestOk() {
		final CloudRequestDTO targetCloud = new CloudRequestDTO();
		targetCloud.setOperator("aitia");
		targetCloud.setName("testcloud");
		
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("localhost");
		relay.setPort(3000);
		relay.setType("GATEWAY_RELAY");
		
		final QoSMonitorSenderConnectionRequestDTO request = new QoSMonitorSenderConnectionRequestDTO();
		request.setTargetCloud(targetCloud);
		request.setRelay(relay);
		request.setQueueId("queueId");
		request.setPeerName("peer");
		request.setReceiverQoSMonitorPublicKey("valid key");
		
		final UriComponents uri = Utilities.createURI(CommonConstants.HTTPS, "localhost", 1234, "ignored");
		
		when(arrowheadContext.containsKey(anyString())).thenReturn(true);
		when(arrowheadContext.get(anyString())).thenReturn(uri);
		when(httpService.sendRequest(any(UriComponents.class), eq(HttpMethod.POST), eq(Void.class), any(QoSMonitorSenderConnectionRequestDTO.class))).thenReturn(new ResponseEntity<>(HttpStatus.OK));
		
		testingObject.initRelayTest(request);
		
		verify(httpService, times(1)).sendRequest(any(UriComponents.class), eq(HttpMethod.POST), eq(Void.class), any(QoSMonitorSenderConnectionRequestDTO.class));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testJoinRelayTestRequestNull() {
		testingObject.joinRelayTest(null);
	}
	
	// skip relay validation tests because it is the same method that was used in sendQoSRelayTestProposal()

	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testJoinRelayTestSenderQoSMonitorPublicKeyNull() {
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("localhost");
		relay.setPort(3000);
		relay.setType("GATEWAY_RELAY");
		
		final QoSRelayTestProposalRequestDTO request = new QoSRelayTestProposalRequestDTO();
		request.setRelay(relay);
		
		testingObject.joinRelayTest(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testJoinRelayTestSenderQoSMonitorPublicKeyEmpty() {
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("localhost");
		relay.setPort(3000);
		relay.setType("GATEWAY_RELAY");
		
		final QoSRelayTestProposalRequestDTO request = new QoSRelayTestProposalRequestDTO();
		request.setRelay(relay);
		request.setSenderQoSMonitorPublicKey(" ");
		
		testingObject.joinRelayTest(request);
	}
	
	// skip cloud validation tests because it is the same method that was used in initRelayTest()
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testJoinRelayTestUriNotFound() {
		final CloudRequestDTO requesterCloud = new CloudRequestDTO();
		requesterCloud.setOperator("aitia");
		requesterCloud.setName("testcloud");
		
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("localhost");
		relay.setPort(3000);
		relay.setType("GATEWAY_RELAY");
		
		final QoSRelayTestProposalRequestDTO request = new QoSRelayTestProposalRequestDTO();
		request.setRequesterCloud(requesterCloud);
		request.setRelay(relay);
		request.setSenderQoSMonitorPublicKey("valid key");
		
		when(arrowheadContext.containsKey(anyString())).thenReturn(false);
		
		testingObject.joinRelayTest(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testJoinRelayTestInvalidUri() {
		final CloudRequestDTO requesterCloud = new CloudRequestDTO();
		requesterCloud.setOperator("aitia");
		requesterCloud.setName("testcloud");
		
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("localhost");
		relay.setPort(3000);
		relay.setType("GATEWAY_RELAY");
		
		final QoSRelayTestProposalRequestDTO request = new QoSRelayTestProposalRequestDTO();
		request.setRequesterCloud(requesterCloud);
		request.setRelay(relay);
		request.setSenderQoSMonitorPublicKey("valid key");
		
		when(arrowheadContext.containsKey(anyString())).thenReturn(true);
		when(arrowheadContext.get(anyString())).thenReturn("not an URI object");
		
		testingObject.joinRelayTest(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testJoinRelayTestOk() {
		final CloudRequestDTO requesterCloud = new CloudRequestDTO();
		requesterCloud.setOperator("aitia");
		requesterCloud.setName("testcloud");
		
		final RelayRequestDTO relay = new RelayRequestDTO();
		relay.setAddress("localhost");
		relay.setPort(3000);
		relay.setType("GATEWAY_RELAY");
		
		final QoSRelayTestProposalRequestDTO request = new QoSRelayTestProposalRequestDTO();
		request.setRequesterCloud(requesterCloud);
		request.setRelay(relay);
		request.setSenderQoSMonitorPublicKey("valid key");
		
		final UriComponents uri = Utilities.createURI(CommonConstants.HTTPS, "localhost", 1234, "ignored");
		
		when(arrowheadContext.containsKey(anyString())).thenReturn(true);
		when(arrowheadContext.get(anyString())).thenReturn(uri);
		when(httpService.sendRequest(any(UriComponents.class), eq(HttpMethod.POST), eq(QoSRelayTestProposalResponseDTO.class), any(QoSRelayTestProposalRequestDTO.class))).
																							thenReturn(new ResponseEntity<>(new QoSRelayTestProposalResponseDTO(), HttpStatus.OK));
		
		testingObject.joinRelayTest(request);
		
		verify(httpService, times(1)).sendRequest(any(UriComponents.class), eq(HttpMethod.POST), eq(QoSRelayTestProposalResponseDTO.class), any(QoSRelayTestProposalRequestDTO.class));
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
	
	//-------------------------------------------------------------------------------------------------
	private Session getDummySession() {
		return new Session() {
			public void close() throws JMSException {}
			public Queue createQueue(final String queueName) throws JMSException { return null; } 
			public Topic createTopic(final String topicName) throws JMSException { return null; }
			public MessageConsumer createConsumer(final Destination destination) throws JMSException { return null; }
			public MessageProducer createProducer(final Destination destination) throws JMSException { return null; }
			public TextMessage createTextMessage(final String text) throws JMSException { return null; }
			public BytesMessage createBytesMessage() throws JMSException { return null; }
			public MapMessage createMapMessage() throws JMSException { return null; }
			public Message createMessage() throws JMSException { return null; }
			public ObjectMessage createObjectMessage() throws JMSException { return null; }
			public ObjectMessage createObjectMessage(final Serializable object) throws JMSException {	return null; }
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
	private MessageConsumer getDummyMessageConsumer() {
		return new MessageConsumer() {
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
	@SuppressWarnings("serial")
	private PublicKey getDummyPublicKey() {
		return new PublicKey() {
			public String getFormat() { return null; }
			public byte[] getEncoded() { return null; }
			public String getAlgorithm() { return null; }
		};
	}
}