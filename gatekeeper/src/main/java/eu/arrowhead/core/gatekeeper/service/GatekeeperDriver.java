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

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

import javax.annotation.Resource;
import javax.jms.JMSException;
import javax.jms.Session;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.util.UriComponents;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.SSLProperties;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.core.CoreSystemService;
import eu.arrowhead.common.database.entity.Cloud;
import eu.arrowhead.common.database.entity.Relay;
import eu.arrowhead.common.dto.internal.AuthorizationInterCloudCheckRequestDTO;
import eu.arrowhead.common.dto.internal.AuthorizationInterCloudCheckResponseDTO;
import eu.arrowhead.common.dto.internal.GSDMultiPollRequestDTO;
import eu.arrowhead.common.dto.internal.GSDPollRequestDTO;
import eu.arrowhead.common.dto.internal.GatewayConsumerConnectionRequestDTO;
import eu.arrowhead.common.dto.internal.GatewayProviderConnectionRequestDTO;
import eu.arrowhead.common.dto.internal.GatewayProviderConnectionResponseDTO;
import eu.arrowhead.common.dto.internal.GeneralRelayRequestDTO;
import eu.arrowhead.common.dto.internal.ICNProposalRequestDTO;
import eu.arrowhead.common.dto.internal.ICNProposalResponseDTO;
import eu.arrowhead.common.dto.internal.IdIdListDTO;
import eu.arrowhead.common.dto.internal.QoSIntraPingMeasurementResponseDTO;
import eu.arrowhead.common.dto.internal.QoSMonitorSenderConnectionRequestDTO;
import eu.arrowhead.common.dto.internal.QoSRelayTestProposalRequestDTO;
import eu.arrowhead.common.dto.internal.QoSRelayTestProposalResponseDTO;
import eu.arrowhead.common.dto.internal.QoSReservationListResponseDTO;
import eu.arrowhead.common.dto.internal.QoSReservationRequestDTO;
import eu.arrowhead.common.dto.internal.QoSReservationResponseDTO;
import eu.arrowhead.common.dto.internal.QoSTemporaryLockRequestDTO;
import eu.arrowhead.common.dto.internal.QoSTemporaryLockResponseDTO;
import eu.arrowhead.common.dto.internal.RelayRequestDTO;
import eu.arrowhead.common.dto.internal.RelayType;
import eu.arrowhead.common.dto.internal.ServiceRegistryListResponseDTO;
import eu.arrowhead.common.dto.internal.SystemAddressSetRelayResponseDTO;
import eu.arrowhead.common.dto.shared.CloudRequestDTO;
import eu.arrowhead.common.dto.shared.ErrorWrapperDTO;
import eu.arrowhead.common.dto.shared.OrchestrationFormRequestDTO;
import eu.arrowhead.common.dto.shared.OrchestrationResponseDTO;
import eu.arrowhead.common.dto.shared.OrchestrationResultDTO;
import eu.arrowhead.common.dto.shared.ServiceInterfaceResponseDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryFormDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryFormListDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryResultDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryResultListDTO;
import eu.arrowhead.common.dto.shared.ServiceRegistryResponseDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.common.exception.TimeoutException;
import eu.arrowhead.common.http.HttpService;
import eu.arrowhead.core.gatekeeper.quartz.RelaySupervisor;
import eu.arrowhead.core.gatekeeper.service.matchmaking.RelayMatchmakingAlgorithm;
import eu.arrowhead.core.gatekeeper.service.matchmaking.RelayMatchmakingParameters;
import eu.arrowhead.relay.gatekeeper.GatekeeperRelayClient;
import eu.arrowhead.relay.gatekeeper.GatekeeperRelayClientFactory;
import eu.arrowhead.relay.gatekeeper.GatekeeperRelayResponse;
import eu.arrowhead.relay.gatekeeper.GeneralAdvertisementResult;

@Component
public class GatekeeperDriver {
	
	//=================================================================================================
	// members
	
	private static final String AUTH_INTER_CHECK_URI_KEY = CoreSystemService.AUTH_CONTROL_INTER_SERVICE.getServiceDefinition() + CoreCommonConstants.URI_SUFFIX;
	private static final String ORCHESTRATION_PROCESS_URI_KEY = CoreSystemService.ORCHESTRATION_SERVICE.getServiceDefinition() + CoreCommonConstants.URI_SUFFIX;
	private static final String ORCHESTRATOR_QOS_ENABLED_URI_KEY = CoreSystemService.ORCHESTRATION_QOS_ENABLED_SERVICE.getServiceDefinition() + CoreCommonConstants.URI_SUFFIX;
	private static final String ORCHESTRATOR_QOS_RESERVATIONS_URI_KEY = CoreSystemService.ORCHESTRATION_QOS_RESERVATIONS_SERVICE.getServiceDefinition() + CoreCommonConstants.URI_SUFFIX;
	private static final String ORCHESTRATOR_QOS_TEMPORARY_LOCK_URI_KEY = CoreSystemService.ORCHESTRATION_QOS_TEMPORARY_LOCK_SERVICE.getServiceDefinition() + CoreCommonConstants.URI_SUFFIX;
	private static final String GATEWAY_PUBLIC_KEY_URI_KEY = CoreSystemService.GATEWAY_PUBLIC_KEY_SERVICE.getServiceDefinition() + CoreCommonConstants.URI_SUFFIX;
	private static final String GATEWAY_CONNECT_PROVIDER_URI_KEY = CoreSystemService.GATEWAY_PROVIDER_SERVICE.getServiceDefinition() + CoreCommonConstants.URI_SUFFIX;
	private static final String GATEWAY_CONNECT_CONSUMER_URI_KEY = CoreSystemService.GATEWAY_CONSUMER_SERVICE.getServiceDefinition() + CoreCommonConstants.URI_SUFFIX;
	private static final String QOSMONITOR_PUBLIC_KEY_URI_KEY = CoreSystemService.QOSMONITOR_PUBLIC_KEY_SERVICE.getServiceDefinition() + CoreCommonConstants.URI_SUFFIX;
	private static final String QOSMONITOR_JOIN_RELAY_TEST_URI_KEY = CoreSystemService.QOSMONITOR_JOIN_RELAY_TEST_SERVICE.getServiceDefinition() + CoreCommonConstants.URI_SUFFIX;
	private static final String QOSMONITOR_INIT_RELAY_TEST_URI_KEY = CoreSystemService.QOSMONITOR_INIT_RELAY_TEST_SERVICE.getServiceDefinition() + CoreCommonConstants.URI_SUFFIX;
	private static final String QOSMONITOR_INTRA_PING_MEASUREMENTS_URI_KEY = CoreSystemService.QOSMONITOR_INTRA_PING_MEASUREMENT_SERVICE.getServiceDefinition() + CoreCommonConstants.URI_SUFFIX;
	
	@Resource(name = CoreCommonConstants.GATEKEEPER_MATCHMAKER)
	private RelayMatchmakingAlgorithm gatekeeperMatchmaker;
	
	@Resource(name = CommonConstants.ARROWHEAD_CONTEXT)
	private Map<String,Object> arrowheadContext;
	
	@Autowired
	private HttpService httpService;
	
	@Autowired
	private SSLProperties sslProps;
	
	@Value(CommonConstants.$HTTP_CLIENT_SOCKET_TIMEOUT_WD)
	private long timeout;

	private GatekeeperRelayClient relayClient;
	
	@Autowired
	private GSDMultiPollRequestExecutorFactory multiGSDExecutorFactory;
	
	private final Logger logger = LogManager.getLogger(GatekeeperDriver.class);
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------	
	@EventListener
	@Order(15) // to make sure GatekeeperApplicationInitListener finished before this method is called (the common name and the keys are added to the context in the init listener)
	public void onApplicationEvent(final ContextRefreshedEvent event) {
		logger.debug("onApplicationEvent started...");
		
		if (!arrowheadContext.containsKey(CommonConstants.SERVER_COMMON_NAME)) {
			throw new ArrowheadException("Server's certificate not found.");
		}
		final String serverCN = (String) arrowheadContext.get(CommonConstants.SERVER_COMMON_NAME);
		
		if (!arrowheadContext.containsKey(CommonConstants.SERVER_PUBLIC_KEY)) {
			throw new ArrowheadException("Server's public key is not found.");
		}
		final PublicKey publicKey = (PublicKey) arrowheadContext.get(CommonConstants.SERVER_PUBLIC_KEY);
		
		if (!arrowheadContext.containsKey(CommonConstants.SERVER_PRIVATE_KEY)) {
			throw new ArrowheadException("Server's private key is not found.");
		}
		final PrivateKey privateKey = (PrivateKey) arrowheadContext.get(CommonConstants.SERVER_PRIVATE_KEY);
	
		relayClient = GatekeeperRelayClientFactory.createGatekeeperRelayClient(serverCN, publicKey, privateKey, sslProps, timeout, RelaySupervisor.getRegistry());
	}
	
	//-------------------------------------------------------------------------------------------------
	public List<ErrorWrapperDTO> sendGSDPollRequest(final List<Cloud> cloudsToContact, final GSDPollRequestDTO gsdPollRequestDTO) throws InterruptedException {
		logger.debug("sendGSDPollRequest started...");		
		Assert.isTrue(cloudsToContact != null && !cloudsToContact.isEmpty(), "cloudsToContact list is null or empty");
		Assert.notNull(gsdPollRequestDTO, "gsdPollRequestDTO is null");
		Assert.notNull(gsdPollRequestDTO.getRequestedService(), "requestedService is null");
		Assert.isTrue(!Utilities.isEmpty(gsdPollRequestDTO.getRequestedService().getServiceDefinitionRequirement()), "serviceDefinitionRequirement is empty");
		Assert.notNull(gsdPollRequestDTO.getRequesterCloud(), "requesterCloud is null");
		
		final int numOfCloudsToContact = cloudsToContact.size();
		final BlockingQueue<ErrorWrapperDTO> queue = new LinkedBlockingQueue<>(numOfCloudsToContact);		

		final GSDPollRequestExecutor gsdPollRequestExecutor = new GSDPollRequestExecutor(queue, relayClient, gsdPollRequestDTO, getOneGatekeeperRelayPerCloud(cloudsToContact));
		gsdPollRequestExecutor.execute();
		
		final List<ErrorWrapperDTO> gsdPollAnswers = new ArrayList<>(numOfCloudsToContact);
		for (int i = 0; i < numOfCloudsToContact; ++i) {
			try {
				gsdPollAnswers.add(queue.take());
			} catch (final InterruptedException ex) {
				logger.trace("Thread {} is interrupted...", Thread.currentThread().getName());
				gsdPollRequestExecutor.shutdownExecutionNow();
				throw ex;
			}
		} 
		
		return gsdPollAnswers;
	}
	
	//-------------------------------------------------------------------------------------------------
	public List<ErrorWrapperDTO> sendMultiGSDPollRequest(final List<Cloud> cloudsToContact, final GSDMultiPollRequestDTO gsdPollRequestDTO) throws InterruptedException { 
		logger.debug("sendMultiGSDPollRequest started...");		
		Assert.isTrue(!Utilities.isEmpty(cloudsToContact), "cloudsToContact list is null or empty");
		Assert.notNull(gsdPollRequestDTO, "gsdPollRequestDTO is null");
		Assert.isTrue(!Utilities.isEmpty(gsdPollRequestDTO.getRequestedServices()), "requestedServices list is null or empty");
		for (final ServiceQueryFormDTO serviceReq : gsdPollRequestDTO.getRequestedServices()) {
			Assert.isTrue(!Utilities.isEmpty(serviceReq.getServiceDefinitionRequirement()), "serviceDefinitionRequirement is null or empty");
		}
		Assert.notNull(gsdPollRequestDTO.getRequesterCloud(), "requesterCloud is null");
		
		final int numOfCloudsToContact = cloudsToContact.size();
		final BlockingQueue<ErrorWrapperDTO> queue = new LinkedBlockingQueue<>(numOfCloudsToContact);		

		final GSDMultiPollRequestExecutor gsdPollRequestExecutor = multiGSDExecutorFactory.newExecutor(queue, relayClient, gsdPollRequestDTO, getOneGatekeeperRelayPerCloud(cloudsToContact));
		gsdPollRequestExecutor.execute();
		
		final List<ErrorWrapperDTO> gsdPollAnswers = new ArrayList<>(numOfCloudsToContact);
		for (int i = 0; i < numOfCloudsToContact; ++i) {
			try {
				gsdPollAnswers.add(queue.take());
			} catch (final InterruptedException ex) {
				logger.trace("Thread {} is interrupted...", Thread.currentThread().getName());
				gsdPollRequestExecutor.shutdownExecutionNow();
				throw ex;
			}
		} 
		
		return gsdPollAnswers;
	}
	
	//-------------------------------------------------------------------------------------------------
	public ServiceQueryResultDTO sendServiceRegistryQuery(final ServiceQueryFormDTO queryForm) {
		logger.debug("sendServiceReistryQuery started...");		
		Assert.notNull(queryForm, "queryForm is null.");
		
		final UriComponents queryUri = getServiceRegistryQueryUri();
		final ResponseEntity<ServiceQueryResultDTO> response = httpService.sendRequest(queryUri, HttpMethod.POST, ServiceQueryResultDTO.class, queryForm);
		
		return response.getBody();
	}
	
    //-------------------------------------------------------------------------------------------------
    public ServiceQueryResultListDTO sendServiceRegistryMultiQuery(final ServiceQueryFormListDTO forms) { 
        logger.debug("sendServiceRegistryMultiQuery started...");
        Assert.notNull(forms, "ServiceQueryFormListDTO is null.");

        final UriComponents uri = getMultiQueryServiceRegistryUri();
        return httpService.sendRequest(uri, HttpMethod.POST, ServiceQueryResultListDTO.class, forms).getBody();
    }
	
	//-------------------------------------------------------------------------------------------------
	public ServiceRegistryListResponseDTO sendServiceRegistryQueryAll() {
		logger.debug("sendServiceRegistryQueryAll started...");		
		
		final UriComponents queryUri = getServiceRegistryQueryAllUri();
		final ResponseEntity<ServiceRegistryListResponseDTO> response = httpService.sendRequest(queryUri, HttpMethod.GET, ServiceRegistryListResponseDTO.class);
		
		return response.getBody();
	}
	
	//-------------------------------------------------------------------------------------------------
	public Map<Long,List<Long>> sendInterCloudAuthorizationCheckQuery(final List<ServiceRegistryResponseDTO> serviceQueryData, final CloudRequestDTO cloud, final String serviceDefinition) {
		logger.debug("sendInterCloudAuthorizationCheckQuery started...");		
		Assert.notNull(serviceQueryData, "serviceQueryData is null.");
		Assert.notNull(cloud, "cloud is null.");
		Assert.isTrue(!Utilities.isEmpty(serviceDefinition), "serviceDefinition is null or empty");
		
		final List<IdIdListDTO> providerIdsWithInterfaceIds = new ArrayList<>();
		for (final ServiceRegistryResponseDTO srEntryDTO : serviceQueryData) {
			final List<Long> interfaceIds = new ArrayList<>();
			for (final ServiceInterfaceResponseDTO interfaceDTO : srEntryDTO.getInterfaces()) {
				interfaceIds.add(interfaceDTO.getId());
			}
 			
			providerIdsWithInterfaceIds.add(new IdIdListDTO(srEntryDTO.getProvider().getId(), interfaceIds));
		}
		
		final AuthorizationInterCloudCheckRequestDTO interCloudCheckRequestDTO = new AuthorizationInterCloudCheckRequestDTO(cloud, serviceDefinition, providerIdsWithInterfaceIds);
		final UriComponents checkUri = getAuthInterCheckUri();
		final ResponseEntity<AuthorizationInterCloudCheckResponseDTO> response = httpService.sendRequest(checkUri, HttpMethod.POST, AuthorizationInterCloudCheckResponseDTO.class, interCloudCheckRequestDTO);
		
		return convertAuthorizationResultsToMap(response.getBody().getAuthorizedProviderIdsWithInterfaceIds());
	}
	
	//-------------------------------------------------------------------------------------------------
	public ICNProposalResponseDTO sendICNProposal(final Cloud targetCloud, final ICNProposalRequestDTO request) {
		logger.debug("sendICNProposal started...");
		
		Assert.notNull(targetCloud, "Target cloud is null.");
		Assert.notNull(request, "Request is null.");
		
		final Relay relay = gatekeeperMatchmaker.doMatchmaking(new RelayMatchmakingParameters(targetCloud));
		
		try {
			final Session session = relayClient.createConnection(relay.getAddress(), relay.getPort(), relay.getSecure());
			final String recipientCommonName = getRecipientCommonName(targetCloud);
			final GeneralAdvertisementResult advResult = relayClient.publishGeneralAdvertisement(session, recipientCommonName, targetCloud.getAuthenticationInfo());
			if (advResult == null) {
				throw new TimeoutException(recipientCommonName + " does not acknowledge request in time", HttpStatus.SC_GATEWAY_TIMEOUT, "ICN Proposal to " + recipientCommonName);
			}
			
			final GatekeeperRelayResponse relayResponse = relayClient.sendRequestAndReturnResponse(session, advResult, request);
			if (relayResponse == null) {
				throw new TimeoutException(recipientCommonName + " does not respond in time", HttpStatus.SC_GATEWAY_TIMEOUT, "ICN Proposal to " + recipientCommonName);
			}
			
			return relayResponse.getICNProposalResponse();
		} catch (final JMSException ex) {
			logger.debug("Error while sending ICN proposal via relay: {}", ex.getMessage());
			logger.debug("Exception:", ex);			
			throw new ArrowheadException("Error while sending ICN proposal via relay.", ex);
			
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	public OrchestrationResponseDTO queryOrchestrator(final OrchestrationFormRequestDTO form) {
		logger.debug("queryOrchestrator started...");
		
		Assert.notNull(form, "form is null.");
		
		final UriComponents orchestrationProcessUri = getOrchestrationProcessUri();
		final ResponseEntity<OrchestrationResponseDTO> response = httpService.sendRequest(orchestrationProcessUri, HttpMethod.POST, OrchestrationResponseDTO.class, form);
		
		return response.getBody();
	}
	
	//-------------------------------------------------------------------------------------------------
	public OrchestrationResponseDTO queryAuthorizationBasedOnOrchestrationResponse(final CloudRequestDTO requesterCloud, final OrchestrationResponseDTO orchestrationResponse) {
		logger.debug("queryAuthorizationBasedOnOchestrationResponse started...");
		
		Assert.notNull(requesterCloud, "Requester cloud is null.");
		Assert.isTrue(orchestrationResponse != null && orchestrationResponse.getResponse() != null && !orchestrationResponse.getResponse().isEmpty(), "Ochestration response is null or empty.");
		
		final UriComponents authInterCheckUri = getAuthInterCheckUri();
		final AuthorizationInterCloudCheckRequestDTO authRequest = createAuthorizationRequestFromOrchestrationResponse(requesterCloud, orchestrationResponse);
		final ResponseEntity<AuthorizationInterCloudCheckResponseDTO> response = httpService.sendRequest(authInterCheckUri, HttpMethod.POST, AuthorizationInterCloudCheckResponseDTO.class, authRequest);

		filterOrchestrationResponseWithAuthData(orchestrationResponse, response.getBody());
		
		return orchestrationResponse;
	}
	
	//-------------------------------------------------------------------------------------------------
	public String queryGatewayPublicKey() {
		logger.debug("queryPublicKey started...");
		
		final UriComponents publicKeyUri = getGatewayPublicKeyUri();
		final ResponseEntity<String> response = httpService.sendRequest(publicKeyUri, HttpMethod.GET, String.class);
		final String encodedKey = Utilities.fromJson(response.getBody(), String.class);
		
		return encodedKey;
	}
	
	//-------------------------------------------------------------------------------------------------
	public GatewayProviderConnectionResponseDTO connectProvider(final GatewayProviderConnectionRequestDTO request) {
		logger.debug("connectProvider started...");

		Assert.notNull(request, "request is null.");
		validateRelay(request.getRelay());
		validateSystem(request.getConsumer());
		validateSystem(request.getProvider());
		validateCloud(request.getConsumerCloud());
		validateCloud(request.getProviderCloud());
		Assert.isTrue(!Utilities.isEmpty(request.getServiceDefinition()), "service definition is null or blank.");
		Assert.isTrue(!Utilities.isEmpty(request.getConsumerGWPublicKey()), "consumer gateway public key is null or blank.");
		
		final UriComponents connectProviderUri = getGatewayConnectProviderUri();
		final ResponseEntity<GatewayProviderConnectionResponseDTO> response = httpService.sendRequest(connectProviderUri, HttpMethod.POST, GatewayProviderConnectionResponseDTO.class, request);
		
		return response.getBody();
	}
	
	//-------------------------------------------------------------------------------------------------
	public int connectConsumer(final GatewayConsumerConnectionRequestDTO request) {
		logger.debug("connectConsumer started...");

		Assert.notNull(request, "request is null.");
		validateRelay(request.getRelay());
		validateSystem(request.getConsumer());
		validateSystem(request.getProvider());
		validateCloud(request.getConsumerCloud());
		validateCloud(request.getProviderCloud());
		Assert.isTrue(!Utilities.isEmpty(request.getServiceDefinition()), "service definition is null or blank.");
		Assert.isTrue(!Utilities.isEmpty(request.getProviderGWPublicKey()), "provider gateway public key is null or blank.");
		Assert.isTrue(!Utilities.isEmpty(request.getQueueId()), "queue id is null or blank.");
		Assert.isTrue(!Utilities.isEmpty(request.getPeerName()), "peer name is null or blank.");
		
		final UriComponents connectConsumerUri = getGatewayConnectConsumerUri();
		final ResponseEntity<Integer> response = httpService.sendRequest(connectConsumerUri, HttpMethod.POST, Integer.class, request);
		
		return response.getBody();
	}
	
	//-------------------------------------------------------------------------------------------------
	public String getGatewayHost() {
		logger.debug("getGatewayHost started...");
		
		return getGatewayPublicKeyUri().getHost();
	}

	//-------------------------------------------------------------------------------------------------
	public List<ErrorWrapperDTO> sendAccessTypesCollectionRequest(final List<Cloud> clouds) throws InterruptedException {
		logger.debug("sendAccessTypesCollectionRequest started...");
		Assert.isTrue(clouds != null && !clouds.isEmpty(), "cloud list is null or empty");
		for (final Cloud cloud : clouds) {
			validateCloud(cloud);
		}
		
		final int numOfCloudsToContact = clouds.size();
		final BlockingQueue<ErrorWrapperDTO> queue = new LinkedBlockingQueue<>(numOfCloudsToContact);	
		
		final AccessTypesCollectionRequestExecutor atcRequestExecutor = new AccessTypesCollectionRequestExecutor(queue, relayClient, getOneGatekeeperRelayPerCloud(clouds));
		atcRequestExecutor.execute();
		
		final List<ErrorWrapperDTO> atcAnswers = new ArrayList<>();
		for (int i = 0; i < numOfCloudsToContact; ++i) {
			try {
				atcAnswers.add(queue.take());
			} catch (final InterruptedException ex) {
				logger.trace("Thread {} is interrupted...", Thread.currentThread().getName());
				atcRequestExecutor.shutdownExecutionNow();
				throw ex;
			}
		}
		
		return atcAnswers;
	}
	
	//-------------------------------------------------------------------------------------------------
	public SystemAddressSetRelayResponseDTO sendSystemAddressCollectionRequest(final Cloud targetCloud) {
		logger.debug("sendSystemAddressCollectionRequest started...");
		validateCloud(targetCloud);
		
		final Relay relay = gatekeeperMatchmaker.doMatchmaking(new RelayMatchmakingParameters(targetCloud));
		try {
			final Session session = relayClient.createConnection(relay.getAddress(), relay.getPort(), relay.getSecure());
			final String recipientCommonName = getRecipientCommonName(targetCloud);
			final GeneralAdvertisementResult advResult = relayClient.publishGeneralAdvertisement(session, recipientCommonName, targetCloud.getAuthenticationInfo());
			if (advResult == null) {
				throw new TimeoutException(recipientCommonName + " does not acknowledge request in time", HttpStatus.SC_GATEWAY_TIMEOUT, "SystemAddressCollectionRequest to " + recipientCommonName);
			}
			final GatekeeperRelayResponse relayResponse = relayClient.sendRequestAndReturnResponse(session, advResult, new GeneralRelayRequestDTO(CoreCommonConstants.RELAY_MESSAGE_TYPE_SYSTEM_ADDRESS_LIST));
			if (relayResponse == null) {
				throw new TimeoutException(recipientCommonName + " does not respond in time", HttpStatus.SC_GATEWAY_TIMEOUT, "SystemAddressCollectionRequest to " + recipientCommonName);
			}
			
			return relayResponse.getSystemAddressSetResponse();
		} catch (final JMSException ex) {
			logger.debug("Error while sending SystemAddressCollectionRequest via relay: {}", ex.getMessage());
			logger.debug("Exception:", ex);
			
			throw new ArrowheadException("Error while sending SystemAddressCollectionRequest via relay.", ex);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	public String queryQoSMonitorPublicKey() {
		logger.debug("queryQoSMonitorPublicKey started...");
		
		final UriComponents publicKeyUri = getQoSMonitorPublicKeyUri();
		final ResponseEntity<String> response = httpService.sendRequest(publicKeyUri, HttpMethod.GET, String.class);
		final String encodedKey = Utilities.fromJson(response.getBody(), String.class);
		
		return encodedKey;
	}
	
	//-------------------------------------------------------------------------------------------------
	public QoSRelayTestProposalResponseDTO sendQoSRelayTestProposal(final QoSRelayTestProposalRequestDTO request, final Cloud targetCloud) {
		logger.debug("sendQoSRelayTestProposal");
		
		validateQoSRelayTestProposalRequestDTO(request);
		validateCloud(targetCloud);
		
		final Relay gatekeeperRelay = gatekeeperMatchmaker.doMatchmaking(new RelayMatchmakingParameters(targetCloud));
		try {
			final Session session = relayClient.createConnection(gatekeeperRelay.getAddress(), gatekeeperRelay.getPort(), gatekeeperRelay.getSecure());
			final String recipientCommonName = getRecipientCommonName(targetCloud);
			final GeneralAdvertisementResult advResult = relayClient.publishGeneralAdvertisement(session, recipientCommonName, targetCloud.getAuthenticationInfo());
			if (advResult == null) {
				throw new TimeoutException(recipientCommonName + " does not acknowledge request in time", HttpStatus.SC_GATEWAY_TIMEOUT, "QoSRelayTestProposalRequestDTO to " + recipientCommonName);
			}
			final GatekeeperRelayResponse relayResponse = relayClient.sendRequestAndReturnResponse(session, advResult, request);
			if (relayResponse == null) {
				throw new TimeoutException(recipientCommonName + " does not respond in time", HttpStatus.SC_GATEWAY_TIMEOUT, "QoSRelayTestProposalRequestDTO to " + recipientCommonName);
			}
			
			return relayResponse.getQoSRelayTestProposalResponse();
		} catch (final JMSException ex) {
			logger.debug("Error while sending QoSRelayTestProposalRequestDTO via relay: {}", ex.getMessage());
			logger.debug("Exception:", ex);
			
			throw new ArrowheadException("Error while sending QoSRelayTestProposalRequestDTO via relay.", ex);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	public QoSRelayTestProposalResponseDTO joinRelayTest(final QoSRelayTestProposalRequestDTO request) {
		logger.debug("joinRelayTest started...");

		validateQoSRelayTestProposalRequestDTO(request);
		validateCloud(request.getRequesterCloud());

		final UriComponents uri = getQoSMonitorJoinRelayTestUri();
		final ResponseEntity<QoSRelayTestProposalResponseDTO> response = httpService.sendRequest(uri, HttpMethod.POST, QoSRelayTestProposalResponseDTO.class, request);
		
		return response.getBody();
	}
	
	//-------------------------------------------------------------------------------------------------
	public void initRelayTest(final QoSMonitorSenderConnectionRequestDTO request) {
		logger.debug("initRelayTest started...");
		
		validateQosMonitorSenderConnectionRequestDTO(request);
		
		final UriComponents uri = getQoSMonitorInitRelayTestUri();
		httpService.sendRequest(uri, HttpMethod.POST, Void.class, request);
	}
	
	//-------------------------------------------------------------------------------------------------
	public boolean checkQoSEnabled() {
		logger.debug("checkQoSEnabled started...");
		
		final UriComponents uri = getOrchestratorIsQoSEnabledUri();
		final ResponseEntity<String> response = httpService.sendRequest(uri, HttpMethod.GET, String.class);
		return Boolean.valueOf(response.getBody());
	}
	
	//-------------------------------------------------------------------------------------------------
	public QoSIntraPingMeasurementResponseDTO getQoSIntraPingMeasurementsForLocalSystem(final long systemId) {
		logger.debug("getQoSMeasurementsForLocalSystem started...");
		
		final UriComponents uri = getQoSMonitorIntraPingMeasurementUri(systemId);
		final ResponseEntity<QoSIntraPingMeasurementResponseDTO> response = httpService.sendRequest(uri, HttpMethod.GET, QoSIntraPingMeasurementResponseDTO.class);
		return response.getBody();
	}
	
	//-------------------------------------------------------------------------------------------------
	public List<QoSReservationResponseDTO> getQoSReservationList() {
		logger.debug("getQoSReservationList started...");
		
		final UriComponents uri = getOrchestratorQoSReservationsUri();
		final ResponseEntity<QoSReservationListResponseDTO> response = httpService.sendRequest(uri, HttpMethod.GET, QoSReservationListResponseDTO.class);
		return response.getBody().getData();
	}
	
	//-------------------------------------------------------------------------------------------------
	public QoSTemporaryLockResponseDTO sendQoSTemporaryLockRequest(final QoSTemporaryLockRequestDTO request) {
		logger.debug("sendQoSTemporaryLockRequest started...");
		
		validateQoSReservationRequestDTO(request);
		
		final UriComponents uri = getOrchestratorQoSTemporaryLockUri();
		final ResponseEntity<QoSTemporaryLockResponseDTO> response = httpService.sendRequest(uri, HttpMethod.POST, QoSTemporaryLockResponseDTO.class, request);
		return response.getBody();
	}
	
	//-------------------------------------------------------------------------------------------------
	public void sendQoSConfirmReservationRequest(final QoSReservationRequestDTO request) {
		logger.debug("sendQoSConfirmReservationRequest started...");

		validateQoSReservationRequestDTO(request);
		
		final UriComponents uri = getOrchestratorQoSReservationsUri();
		httpService.sendRequest(uri, HttpMethod.POST, Void.class, request);
	}
	
	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------		
	private Map<Cloud,Relay> getOneGatekeeperRelayPerCloud(final List<Cloud> clouds) {
		logger.debug("getOneGatekeeperRelayPerCloud started...");
		
		final Map<Cloud,Relay> realyPerCloud = new HashMap<>();
		for (final Cloud cloud : clouds) {
			final Relay relay = gatekeeperMatchmaker.doMatchmaking(new RelayMatchmakingParameters(cloud));
			realyPerCloud.put(cloud, relay);
		}
		
		return realyPerCloud;
	}
	
	//-------------------------------------------------------------------------------------------------
	private String getRecipientCommonName(final Cloud cloud) {
		return "gatekeeper." + Utilities.getCloudCommonName(cloud.getOperator(), cloud.getName()); 
	}
	
	//-------------------------------------------------------------------------------------------------
	private UriComponents getServiceRegistryQueryUri() {
		logger.debug("getServiceRegistryQueryUri started...");
		
		if (arrowheadContext.containsKey(CoreCommonConstants.SR_QUERY_URI)) {
			try {
				return (UriComponents) arrowheadContext.get(CoreCommonConstants.SR_QUERY_URI);
			} catch (final ClassCastException ex) {
				throw new ArrowheadException("Gatekeeper can't find Service Registry Query URI.");
			}
		}
		
		throw new ArrowheadException("Gatekeeper can't find Service Registry Query URI.");
	}
	
    //-------------------------------------------------------------------------------------------------
    private UriComponents getMultiQueryServiceRegistryUri() {
        logger.debug("getMultiQueryServiceRegistryUri started...");

        if (arrowheadContext.containsKey(CoreCommonConstants.SR_MULTI_QUERY_URI)) {
            try {
                return (UriComponents) arrowheadContext.get(CoreCommonConstants.SR_MULTI_QUERY_URI);
            } catch (final ClassCastException ex) {
                throw new ArrowheadException("Gatekeeper can't find Service Registry multi-query URI.");
            }
        }

        throw new ArrowheadException("Gatekeeper can't find Service Registry multi-query URI.");
    }
	
	//-------------------------------------------------------------------------------------------------
	private UriComponents getServiceRegistryQueryAllUri() {
		logger.debug("getServiceRegistryQueryAllUri started...");
		
		if (arrowheadContext.containsKey(CoreCommonConstants.SR_QUERY_ALL)) {
			try {
				return (UriComponents) arrowheadContext.get(CoreCommonConstants.SR_QUERY_ALL);
			} catch (final ClassCastException ex) {
				throw new ArrowheadException("Gatekeeper can't find Service Registry query/all URI.");
			}
		}
		
		throw new ArrowheadException("Gatekeeper can't find Service Registry query/all URI.");
	}
	
	//-------------------------------------------------------------------------------------------------
	private UriComponents getAuthInterCheckUri() {
		logger.debug("getAuthInterCheckUri started...");
		
		if (arrowheadContext.containsKey(AUTH_INTER_CHECK_URI_KEY)) {
			try {
				return (UriComponents) arrowheadContext.get(AUTH_INTER_CHECK_URI_KEY);
			} catch (final ClassCastException ex) {
				throw new ArrowheadException("Gatekeeper can't find authorization check URI.");
			}
		}
		
		throw new ArrowheadException("Gatekeeper can't find authorization check URI.");
	}
	
	//-------------------------------------------------------------------------------------------------
	private UriComponents getOrchestrationProcessUri() {
		logger.debug("getOrchestrationProcessUri started...");
		
		if (arrowheadContext.containsKey(ORCHESTRATION_PROCESS_URI_KEY)) {
			try {
				return (UriComponents) arrowheadContext.get(ORCHESTRATION_PROCESS_URI_KEY);
			} catch (final ClassCastException ex) {
				throw new ArrowheadException("Gatekeeper can't find orchestration process URI.");
			}
		}
		
		throw new ArrowheadException("Gatekeeper can't find orchestration process URI.");
	}
	
	//-------------------------------------------------------------------------------------------------
	private UriComponents getGatewayPublicKeyUri() {
		logger.debug("getGatewayPublicKeyUri started...");
		
		if (arrowheadContext.containsKey(GATEWAY_PUBLIC_KEY_URI_KEY)) {
			try {
				return (UriComponents) arrowheadContext.get(GATEWAY_PUBLIC_KEY_URI_KEY);
			} catch (final ClassCastException ex) {
				throw new ArrowheadException("Gatekeeper can't find gateway public key URI.");
			}
		}
		
		throw new ArrowheadException("Gatekeeper can't find gateway public key URI.");
	}
	
	//-------------------------------------------------------------------------------------------------
	private UriComponents getGatewayConnectProviderUri() {
		logger.debug("getGatewayConnectProviderUri started...");
		
		if (arrowheadContext.containsKey(GATEWAY_CONNECT_PROVIDER_URI_KEY)) {
			try {
				return (UriComponents) arrowheadContext.get(GATEWAY_CONNECT_PROVIDER_URI_KEY);
			} catch (final ClassCastException ex) {
				throw new ArrowheadException("Gatekeeper can't find gateway connect provider URI.");
			}
		}
		
		throw new ArrowheadException("Gatekeeper can't find gateway connect provider URI.");
	}
	
	//-------------------------------------------------------------------------------------------------
	private UriComponents getGatewayConnectConsumerUri() {
		logger.debug("getGatewayConnectConsumerUri started...");
		
		if (arrowheadContext.containsKey(GATEWAY_CONNECT_CONSUMER_URI_KEY)) {
			try {
				return (UriComponents) arrowheadContext.get(GATEWAY_CONNECT_CONSUMER_URI_KEY);
			} catch (final ClassCastException ex) {
				throw new ArrowheadException("Gatekeeper can't find gateway connect consumer URI.");
			}
		}
		
		throw new ArrowheadException("Gatekeeper can't find gateway connect consumer URI.");
	}
	
	//-------------------------------------------------------------------------------------------------
	private AuthorizationInterCloudCheckRequestDTO createAuthorizationRequestFromOrchestrationResponse(final CloudRequestDTO requesterCloud, final OrchestrationResponseDTO orchestrationResponse) {
		final String serviceDefinition = orchestrationResponse.getResponse().get(0).getService().getServiceDefinition(); // response is not empty
		final List<IdIdListDTO> providerIdsWithInterfaceIds = new ArrayList<>(orchestrationResponse.getResponse().size());
		for (final OrchestrationResultDTO response : orchestrationResponse.getResponse()) {
			providerIdsWithInterfaceIds.add(new IdIdListDTO(response.getProvider().getId(), convertServiceInterfaceListToServiceInterfaceIdList(response.getInterfaces())));
		}
		
		return new AuthorizationInterCloudCheckRequestDTO(requesterCloud, serviceDefinition, providerIdsWithInterfaceIds); 
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S1612")
	private List<Long> convertServiceInterfaceListToServiceInterfaceIdList(final List<ServiceInterfaceResponseDTO> intfs) {
		logger.debug("convertServiceInterfaceListToServiceInterfaceIdList started...");
		
		if (intfs == null) {
			return List.of();
		}
		
		return intfs.stream().map(dto -> dto.getId()).collect(Collectors.toList());
	}

	//-------------------------------------------------------------------------------------------------
	// method may change orchestration response
	private void filterOrchestrationResponseWithAuthData(final OrchestrationResponseDTO orchestrationResponse, final AuthorizationInterCloudCheckResponseDTO authResult) {
		logger.debug("filterOrchestrationResponseWithAuthData started...");
		
		final List<OrchestrationResultDTO> results = orchestrationResponse.getResponse();
		if (authResult.getAuthorizedProviderIdsWithInterfaceIds() == null || authResult.getAuthorizedProviderIdsWithInterfaceIds().isEmpty()) {
			// consumer has no access to any of the specified providers
			results.clear();
		} else {
			final Map<Long,List<Long>> authMap = convertAuthorizationResultsToMap(authResult.getAuthorizedProviderIdsWithInterfaceIds());
			for (final Iterator<OrchestrationResultDTO> it = results.iterator(); it.hasNext();) {
				final OrchestrationResultDTO result = it.next();
				if (authMap.containsKey(result.getProvider().getId())) {
					final List<Long> authorizedInterfaceIds = authMap.get(result.getProvider().getId());
					result.getInterfaces().removeIf(e -> !authorizedInterfaceIds.contains(e.getId()));
				} else {
					it.remove();
				}
			}
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private Map<Long,List<Long>> convertAuthorizationResultsToMap(final List<IdIdListDTO> authorizedProviderIdsWithInterfaceIds) {
		logger.debug("convertAuthorizationResultsToMap started...");

		return authorizedProviderIdsWithInterfaceIds.stream().collect(Collectors.toMap(e -> e.getId(), 
																					   e -> e.getIdList()));
	}
	
	//-------------------------------------------------------------------------------------------------
	private void validateRelay(final RelayRequestDTO relay) {
		logger.debug("validateRelay started...");
		
		Assert.notNull(relay, "relay is null.");
		Assert.isTrue(!Utilities.isEmpty(relay.getAddress()), "relay address is null or blank");
		Assert.notNull(relay.getPort(), "relay port is null");
		validateSystemPortRange(relay.getPort());
		Assert.isTrue(!Utilities.isEmpty(relay.getType()), "relay type is null or blank");
		final RelayType relayType = Utilities.convertStringToRelayType(relay.getType());
		if (relayType == null || relayType == RelayType.GATEKEEPER_RELAY) {
			throw new IllegalArgumentException("Relay type is invalid");
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private void validateSystem(final SystemRequestDTO system) {
		logger.debug("validateSystem started...");
		
		Assert.notNull(system, "system is null");
		Assert.isTrue(!Utilities.isEmpty(system.getSystemName()), "system name is null or blank");
		Assert.isTrue(!Utilities.isEmpty(system.getAddress()), "system address is null or blank");
		Assert.notNull(system.getPort(), "system port is null");
		validateSystemPortRange(system.getPort());
	}
	
	//-------------------------------------------------------------------------------------------------
	private void validateCloud(final CloudRequestDTO cloud) {
		logger.debug("validateCloud started...");
		
		Assert.notNull(cloud, "Cloud is null");
		Assert.isTrue(!Utilities.isEmpty(cloud.getOperator()), "cloud operator is null or blank");
		Assert.isTrue(!Utilities.isEmpty(cloud.getName()), "cloud name is null or blank");		
	}
	
	//-------------------------------------------------------------------------------------------------
	private void validateCloud(final Cloud cloud) {
		logger.debug("validateCloud started...");
		
		Assert.notNull(cloud, "Cloud is null");
		Assert.isTrue(!Utilities.isEmpty(cloud.getOperator()), "cloud operator is null or blank");
		Assert.isTrue(!Utilities.isEmpty(cloud.getName()), "cloud name is null or blank");	
		Assert.isTrue(cloud.getGatekeeperRelays() != null && !cloud.getGatekeeperRelays().isEmpty(), "GatekeeperRelaysList of cloud is null or empty.");		
	}
	
	//-------------------------------------------------------------------------------------------------
	private void validateSystemPortRange(final int port) {
		logger.debug("validateSystemPortRange started...");
		
		if (port < CommonConstants.SYSTEM_PORT_RANGE_MIN || port > CommonConstants.SYSTEM_PORT_RANGE_MAX) {
			throw new IllegalArgumentException("port must be between " + CommonConstants.SYSTEM_PORT_RANGE_MIN + " and " + CommonConstants.SYSTEM_PORT_RANGE_MAX + ".");
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private UriComponents getQoSMonitorPublicKeyUri() {
		logger.debug("getQosMonitorPublicKeyUri started...");
		
		if (arrowheadContext.containsKey(QOSMONITOR_PUBLIC_KEY_URI_KEY)) {
			try {
				return (UriComponents) arrowheadContext.get(QOSMONITOR_PUBLIC_KEY_URI_KEY);
			} catch (final ClassCastException ex) {
				throw new ArrowheadException("Gatekeeper can't find QoS Monitor public key URI.");
			}
		}
		
		throw new ArrowheadException("Gatekeeper can't find QoSMonitor public key URI.");
	}
	
	//-------------------------------------------------------------------------------------------------
	private UriComponents getQoSMonitorJoinRelayTestUri() {
		logger.debug("getQoSMonitorJoinRelayTestUri started...");
		
		if (arrowheadContext.containsKey(QOSMONITOR_JOIN_RELAY_TEST_URI_KEY)) {
			try {
				return (UriComponents) arrowheadContext.get(QOSMONITOR_JOIN_RELAY_TEST_URI_KEY);
			} catch (final ClassCastException ex) {
				throw new ArrowheadException("Gatekeeper can't find QoS Monitor join relay test URI.");
			}
		}
		
		throw new ArrowheadException("Gatekeeper can't find QoSMonitor join relay test URI.");
	}
	
	//-------------------------------------------------------------------------------------------------
	private UriComponents getQoSMonitorInitRelayTestUri() {
		logger.debug("getQoSMonitorInitRelayTestUri started...");
		
		if (arrowheadContext.containsKey(QOSMONITOR_INIT_RELAY_TEST_URI_KEY)) {
			try {
				return (UriComponents) arrowheadContext.get(QOSMONITOR_INIT_RELAY_TEST_URI_KEY);
			} catch (final ClassCastException ex) {
				throw new ArrowheadException("Gatekeeper can't find QoS Monitor init relay test URI.");
			}
		}
		
		throw new ArrowheadException("Gatekeeper can't find QoSMonitor init relay test URI.");
	}
	
	//-------------------------------------------------------------------------------------------------
	private UriComponents getOrchestratorIsQoSEnabledUri() {
		logger.debug("getOrchestratorIsQoSEnabledUri started...");

		if (arrowheadContext.containsKey(ORCHESTRATOR_QOS_ENABLED_URI_KEY)) {
			try {
				return (UriComponents) arrowheadContext.get(ORCHESTRATOR_QOS_ENABLED_URI_KEY);
			} catch (final ClassCastException ex) {
				throw new ArrowheadException("Gatekeeper can't find Orchestrator QoS Enabled URI.");
			}
		}
		
		throw new ArrowheadException("Gatekeeper can't find Orchestrator QoS Enabled URI.");
	}
	
	//-------------------------------------------------------------------------------------------------
	private UriComponents getQoSMonitorIntraPingMeasurementUri(final long systemId) {
		logger.debug("getQoSMonitorIntraPingMeasurementUri started...");
		
		if (arrowheadContext.containsKey(QOSMONITOR_INTRA_PING_MEASUREMENTS_URI_KEY)) {
			try {
				final UriComponents uri = (UriComponents) arrowheadContext.get(QOSMONITOR_INTRA_PING_MEASUREMENTS_URI_KEY);
				return uri.expand(systemId);
			} catch (final ClassCastException ex) {
				throw new ArrowheadException("Gatekeeper can't find QoS Monitor intra ping measurements URI.");
			}
		}
		
		throw new ArrowheadException("Gatekeeper can't find QoS Monitor intra ping measurements URI.");
	}
	
	//-------------------------------------------------------------------------------------------------
	private UriComponents getOrchestratorQoSReservationsUri() {
		logger.debug("getOrchestratorQoSReservationsUri started...");
		
		if (arrowheadContext.containsKey(ORCHESTRATOR_QOS_RESERVATIONS_URI_KEY)) {
			try {
				return (UriComponents) arrowheadContext.get(ORCHESTRATOR_QOS_RESERVATIONS_URI_KEY);
			} catch (final ClassCastException ex) {
				throw new ArrowheadException("Gatekeeper can't find Orchestrator QoS reservations URI.");
			}
		}
		
		throw new ArrowheadException("Gatekeeper can't find Orchestrator QoS reservations URI.");
	}
	
	//-------------------------------------------------------------------------------------------------
	private UriComponents getOrchestratorQoSTemporaryLockUri() {
		logger.debug("getOrchestratorQoSReservationsUri started...");
		
		if (arrowheadContext.containsKey(ORCHESTRATOR_QOS_TEMPORARY_LOCK_URI_KEY)) {
			try {
				return (UriComponents) arrowheadContext.get(ORCHESTRATOR_QOS_TEMPORARY_LOCK_URI_KEY);
			} catch (final ClassCastException ex) {
				throw new ArrowheadException("Gatekeeper can't find Orchestrator QoS temporary lock URI.");
			}
		}
		
		throw new ArrowheadException("Gatekeeper can't find Orchestrator QoS temporary lock URI.");
	}
	
	//-------------------------------------------------------------------------------------------------
	private void validateQoSRelayTestProposalRequestDTO(final QoSRelayTestProposalRequestDTO request) {
		logger.debug("validateQoSRelayTestProposalRequestDTO started...");
		
		if (request == null) {
			throw new InvalidParameterException("Relay test proposal is null.");
		}
		
		// don't have to check the target cloud here
		validateRelay(request.getRelay());
		
		if (Utilities.isEmpty(request.getSenderQoSMonitorPublicKey())) {
			throw new InvalidParameterException("Sender QoS Monitor's public key is null or blank.");
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private void validateQosMonitorSenderConnectionRequestDTO(final QoSMonitorSenderConnectionRequestDTO request) {
		logger.debug("validateQosMonitorSenderConnectionRequestDTO started...");
		
		if (request == null) {
			throw new InvalidParameterException("Connection request is null.");
		}
		
		validateCloud(request.getTargetCloud());
		validateRelay(request.getRelay());
		
		if (Utilities.isEmpty(request.getQueueId())) {
			throw new InvalidParameterException("Queue id is null or blank.");
		}
		
		if (Utilities.isEmpty(request.getPeerName())) {
			throw new InvalidParameterException("Peer name is null or blank.");
		}
		if (Utilities.isEmpty(request.getReceiverQoSMonitorPublicKey())) {
			throw new InvalidParameterException("Receiver QoS Monitor's public key is null or blank.");
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private void validateQoSReservationRequestDTO(final QoSTemporaryLockRequestDTO request) {
		logger.debug("validateQoSReservationRequestDTO started...");
		
		if (request == null) {
			throw new InvalidParameterException("QoSReservationRequestDTO is null");
		}
		
		if (request.getRequester() == null) {
			throw new InvalidParameterException("Requester system is null");
		}
		
		if (Utilities.isEmpty(request.getRequester().getSystemName())) {
			throw new InvalidParameterException("Requester system name is null or empty");
		}
		
		if (Utilities.isEmpty(request.getRequester().getAddress())) {
			throw new InvalidParameterException("Requester system address is null or empty");
		}
		
		if (request.getRequester().getPort() == null) {
			throw new InvalidParameterException("Requester system port is null");
		}
		
		if (request instanceof QoSReservationRequestDTO) {
			final QoSReservationRequestDTO req = (QoSReservationRequestDTO) request;
			if (req.getSelected() == null) {
				throw new InvalidParameterException("Selected ORCH result is null");
			}
			
			if (req.getSelected().getProvider() == null) {
				throw new InvalidParameterException("Selected provider is null");
			}
			
			if (req.getSelected().getService() == null) {
				throw new InvalidParameterException("Selected service is null");
			}
		}
	}
	
	//=================================================================================================
	// nested classes
	
	//-------------------------------------------------------------------------------------------------
	@Component
	static class GSDMultiPollRequestExecutorFactory {
		
		//=================================================================================================
		// methods
		
		//-------------------------------------------------------------------------------------------------
		public GSDMultiPollRequestExecutor newExecutor(final BlockingQueue<ErrorWrapperDTO> queue, final GatekeeperRelayClient relayClient, final GSDMultiPollRequestDTO gsdPollRequestDTO, final Map<Cloud,Relay> gatekeeperRelayPerCloud) {
			return new GSDMultiPollRequestExecutor(queue, relayClient, gsdPollRequestDTO, gatekeeperRelayPerCloud);
		}
	}
}