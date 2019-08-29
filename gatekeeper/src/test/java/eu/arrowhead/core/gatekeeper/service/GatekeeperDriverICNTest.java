package eu.arrowhead.core.gatekeeper.service;

import static org.mockito.ArgumentMatchers.any;
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
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.core.CoreSystemService;
import eu.arrowhead.common.database.entity.Cloud;
import eu.arrowhead.common.database.entity.Relay;
import eu.arrowhead.common.dto.AuthorizationInterCloudCheckRequestDTO;
import eu.arrowhead.common.dto.AuthorizationInterCloudCheckResponseDTO;
import eu.arrowhead.common.dto.CloudRequestDTO;
import eu.arrowhead.common.dto.ICNProposalRequestDTO;
import eu.arrowhead.common.dto.ICNProposalResponseDTO;
import eu.arrowhead.common.dto.IdIdListDTO;
import eu.arrowhead.common.dto.OrchestrationFormRequestDTO;
import eu.arrowhead.common.dto.OrchestrationResponseDTO;
import eu.arrowhead.common.dto.OrchestrationResultDTO;
import eu.arrowhead.common.dto.RelayType;
import eu.arrowhead.common.dto.ServiceDefinitionResponseDTO;
import eu.arrowhead.common.dto.ServiceInterfaceResponseDTO;
import eu.arrowhead.common.dto.ServiceSecurityType;
import eu.arrowhead.common.dto.SystemResponseDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.TimeoutException;
import eu.arrowhead.common.http.HttpService;
import eu.arrowhead.core.gatekeeper.relay.GatekeeperRelayClient;
import eu.arrowhead.core.gatekeeper.relay.GatekeeperRelayResponse;
import eu.arrowhead.core.gatekeeper.relay.GeneralAdvertisementResult;
import eu.arrowhead.core.gatekeeper.service.matchmaking.RelayMatchmakingAlgorithm;
import eu.arrowhead.core.gatekeeper.service.matchmaking.RelayMatchmakingParameters;

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

		when(relayClient.createConnection(any(String.class), anyInt())).thenThrow(JMSException.class);
		
		testingObject.sendICNProposal(new Cloud(), new ICNProposalRequestDTO());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = TimeoutException.class)
	public void testSendICNProposalNoAcknowledgement() throws JMSException {
		final Relay relay = new Relay("localhost", 12345, false, false, RelayType.GATEKEEPER_RELAY);
		when(gatekeeperMatchmaker.doMatchmaking(any(RelayMatchmakingParameters.class))).thenReturn(relay);
		
		when(relayClient.createConnection(any(String.class), anyInt())).thenReturn(getTestSession());
		when(relayClient.publishGeneralAdvertisement(any(Session.class), any(String.class), any(String.class))).thenReturn(null);
		
		final Cloud targetCloud = new Cloud("aitia", "testcloud2", true, true, false, "abcd");
		testingObject.sendICNProposal(targetCloud, new ICNProposalRequestDTO());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = TimeoutException.class)
	public void testSendICNProposalNoResponse() throws JMSException {
		final Relay relay = new Relay("localhost", 12345, false, false, RelayType.GATEKEEPER_RELAY);
		when(gatekeeperMatchmaker.doMatchmaking(any(RelayMatchmakingParameters.class))).thenReturn(relay);
		
		when(relayClient.createConnection(any(String.class), anyInt())).thenReturn(getTestSession());
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
		
		when(relayClient.createConnection(any(String.class), anyInt())).thenReturn(getTestSession());
		final GeneralAdvertisementResult gaResult = new GeneralAdvertisementResult(getTestMessageConsumer(), "gatekeeper.testcloud1.aitia.arrowhead.eu", getDummyPublicKey(), "1234");
		when(relayClient.publishGeneralAdvertisement(any(Session.class), any(String.class), any(String.class))).thenReturn(gaResult);
		final GatekeeperRelayResponse response = new GatekeeperRelayResponse("1234", CommonConstants.RELAY_MESSAGE_TYPE_ICN_PROPOSAL, new ICNProposalResponseDTO());
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
		when(arrowheadContext.containsKey(CoreSystemService.ORCHESTRATION_SERVICE.getServiceDefinition() + CommonConstants.URI_SUFFIX)).thenReturn(false);
		testingObject.queryOrchestrator(new OrchestrationFormRequestDTO());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testQueryOrchestratorURIWrongType() {
		final String key = CoreSystemService.ORCHESTRATION_SERVICE.getServiceDefinition() + CommonConstants.URI_SUFFIX;
		when(arrowheadContext.containsKey(key)).thenReturn(true);
		when(arrowheadContext.get(key)).thenReturn("abcd");
		testingObject.queryOrchestrator(new OrchestrationFormRequestDTO());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class) 
	public void testQueryAuthorizationBasedOnOchestrationResponseRequestCloudNull() {
		testingObject.queryAuthorizationBasedOnOchestrationResponse(null, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class) 
	public void testQueryAuthorizationBasedOnOchestrationResponseOrchestrationResponseNull() {
		testingObject.queryAuthorizationBasedOnOchestrationResponse(new CloudRequestDTO(), null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class) 
	public void testQueryAuthorizationBasedOnOchestrationResponseOrchestrationResponseEmpty() {
		testingObject.queryAuthorizationBasedOnOchestrationResponse(new CloudRequestDTO(), new OrchestrationResponseDTO());
	}

	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class) 
	public void testQueryAuthorizationBasedOnOchestrationResponseURINotFound() {
		when(arrowheadContext.containsKey(CoreSystemService.AUTH_CONTROL_INTER_SERVICE.getServiceDefinition() + CommonConstants.URI_SUFFIX)).thenReturn(false);
		testingObject.queryAuthorizationBasedOnOchestrationResponse(new CloudRequestDTO(), new OrchestrationResponseDTO(List.of(new OrchestrationResultDTO())));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class) 
	public void testQueryAuthorizationBasedOnOchestrationResponseURIWrongType() {
		final String key = CoreSystemService.AUTH_CONTROL_INTER_SERVICE.getServiceDefinition() + CommonConstants.URI_SUFFIX;
		when(arrowheadContext.containsKey(key)).thenReturn(true);
		when(arrowheadContext.get(key)).thenReturn("1234");
		testingObject.queryAuthorizationBasedOnOchestrationResponse(new CloudRequestDTO(), new OrchestrationResponseDTO(List.of(new OrchestrationResultDTO())));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test 
	public void testQueryAuthorizationBasedOnOchestrationResponseNoAccess() {
		final String key = CoreSystemService.AUTH_CONTROL_INTER_SERVICE.getServiceDefinition() + CommonConstants.URI_SUFFIX;
		when(arrowheadContext.containsKey(key)).thenReturn(true);
		when(arrowheadContext.get(key)).thenReturn(Utilities.createURI(CommonConstants.HTTPS, "localhost", 1234, "/a"));
		
		final ResponseEntity<AuthorizationInterCloudCheckResponseDTO> response = new ResponseEntity<>(new AuthorizationInterCloudCheckResponseDTO(), HttpStatus.OK);
		when(httpService.sendRequest(any(UriComponents.class), eq(HttpMethod.POST), eq(AuthorizationInterCloudCheckResponseDTO.class), any(AuthorizationInterCloudCheckRequestDTO.class)))
																																												.thenReturn(response);
		
		final OrchestrationResponseDTO authorizedResponse = testingObject.queryAuthorizationBasedOnOchestrationResponse(new CloudRequestDTO(), new OrchestrationResponseDTO(getTestOrchestrationResults()));
		Assert.assertTrue(authorizedResponse.getResponse().isEmpty());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test 
	public void testQueryAuthorizationBasedOnOchestrationResponseFiltered() {
		final String key = CoreSystemService.AUTH_CONTROL_INTER_SERVICE.getServiceDefinition() + CommonConstants.URI_SUFFIX;
		when(arrowheadContext.containsKey(key)).thenReturn(true);
		when(arrowheadContext.get(key)).thenReturn(Utilities.createURI(CommonConstants.HTTPS, "localhost", 1234, "/a"));
		
		final ResponseEntity<AuthorizationInterCloudCheckResponseDTO> response = new ResponseEntity<>(getTestAuthorizationCheckResponse(), HttpStatus.OK);
		when(httpService.sendRequest(any(UriComponents.class), eq(HttpMethod.POST), eq(AuthorizationInterCloudCheckResponseDTO.class), any(AuthorizationInterCloudCheckRequestDTO.class)))
																																												.thenReturn(response);
		
		final OrchestrationResponseDTO orchestrationResponse = new OrchestrationResponseDTO(getTestOrchestrationResults());
		Assert.assertEquals(2, orchestrationResponse.getResponse().size());
		final OrchestrationResponseDTO authorizedResponse = testingObject.queryAuthorizationBasedOnOchestrationResponse(new CloudRequestDTO(), orchestrationResponse);
		Assert.assertEquals(1, authorizedResponse.getResponse().size());
		Assert.assertEquals(2, authorizedResponse.getResponse().get(0).getProvider().getId());
		Assert.assertEquals(1, authorizedResponse.getResponse().get(0).getInterfaces().size());
		Assert.assertEquals(100, authorizedResponse.getResponse().get(0).getInterfaces().get(0).getId());
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
	public Session getTestSession() {
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
	public MessageConsumer getTestMessageConsumer() {
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
	public List<OrchestrationResultDTO> getTestOrchestrationResults() {
		final List<OrchestrationResultDTO> result = new ArrayList<>(2);
		
		final ServiceDefinitionResponseDTO service = new ServiceDefinitionResponseDTO(22, "test-service", null, null);
		final ServiceInterfaceResponseDTO intfs1 = new ServiceInterfaceResponseDTO(100, "HTTP-SECURE-JSON", null, null);
		final ServiceInterfaceResponseDTO intfs2 = new ServiceInterfaceResponseDTO(101, "HTTP-SECURE-XML", null, null);
		
		final SystemResponseDTO provider1 = new SystemResponseDTO(1, "testSystem1", "localhost", 1234, "abcd", null, null);
		final List<ServiceInterfaceResponseDTO> intfsList1 = new ArrayList<>(1);
		intfsList1.add(intfs1);
		final OrchestrationResultDTO dto1 = new OrchestrationResultDTO(provider1, service, "/", ServiceSecurityType.CERTIFICATE, null, intfsList1, 1);
		result.add(dto1);
		
		final SystemResponseDTO provider2 = new SystemResponseDTO(2, "testSystem2", "localhost", 11234, "abcd", null, null);
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
}