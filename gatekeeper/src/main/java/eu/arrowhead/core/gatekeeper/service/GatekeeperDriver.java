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
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.core.CoreSystemService;
import eu.arrowhead.common.database.entity.Cloud;
import eu.arrowhead.common.database.entity.Relay;
import eu.arrowhead.common.dto.AuthorizationInterCloudCheckRequestDTO;
import eu.arrowhead.common.dto.AuthorizationInterCloudCheckResponseDTO;
import eu.arrowhead.common.dto.CloudRequestDTO;
import eu.arrowhead.common.dto.ErrorWrapperDTO;
import eu.arrowhead.common.dto.GSDPollRequestDTO;
import eu.arrowhead.common.dto.ICNProposalRequestDTO;
import eu.arrowhead.common.dto.ICNProposalResponseDTO;
import eu.arrowhead.common.dto.IdIdListDTO;
import eu.arrowhead.common.dto.OrchestrationFormRequestDTO;
import eu.arrowhead.common.dto.OrchestrationResponseDTO;
import eu.arrowhead.common.dto.OrchestrationResultDTO;
import eu.arrowhead.common.dto.ServiceInterfaceResponseDTO;
import eu.arrowhead.common.dto.ServiceQueryFormDTO;
import eu.arrowhead.common.dto.ServiceQueryResultDTO;
import eu.arrowhead.common.dto.ServiceRegistryResponseDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.TimeoutException;
import eu.arrowhead.common.http.HttpService;
import eu.arrowhead.core.gatekeeper.relay.GatekeeperRelayClient;
import eu.arrowhead.core.gatekeeper.relay.GatekeeperRelayResponse;
import eu.arrowhead.core.gatekeeper.relay.GeneralAdvertisementResult;
import eu.arrowhead.core.gatekeeper.relay.GatekeeperRelayClientFactory;
import eu.arrowhead.core.gatekeeper.service.matchmaking.GatekeeperMatchmakingAlgorithm;
import eu.arrowhead.core.gatekeeper.service.matchmaking.GatekeeperMatchmakingParameters;

@Component
public class GatekeeperDriver {
	
	//=================================================================================================
	// members
	
	private static final String AUTH_INTER_CHECK_URI_KEY = CoreSystemService.AUTH_CONTROL_INTER_SERVICE.getServiceDefinition() + CommonConstants.URI_SUFFIX;
	private static final String ORCHESTRATION_PROCESS_URI_KEY = CoreSystemService.ORCHESTRATION_SERVICE.getServiceDefinition() + CommonConstants.URI_SUFFIX;
	
	@Resource(name = CommonConstants.GATEKEEPER_MATCHMAKER)
	private GatekeeperMatchmakingAlgorithm gatekeeperMatchmaker;
	
	@Resource(name = CommonConstants.ARROWHEAD_CONTEXT)
	private Map<String,Object> arrowheadContext;
	
	@Autowired
	private HttpService httpService;
	
	@Value(CommonConstants.$HTTP_CLIENT_SOCKET_TIMEOUT_WD)
	private long timeout;

	private GatekeeperRelayClient relayClient;
	
	private final Logger logger = LogManager.getLogger(GatekeeperDriver.class);
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------	
	@EventListener
	@Order(15) // to make sure GatekeeperApplicationInitListener finished before this method is called (the common name and the keys are added to the context in the init listener)
	public void onApplicationEvent(final ContextRefreshedEvent event) {
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
	
		relayClient = GatekeeperRelayClientFactory.createGatekeeperRelayClient(serverCN, publicKey, privateKey, timeout);	
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
	public ServiceQueryResultDTO sendServiceReistryQuery(final ServiceQueryFormDTO gueryForm) {
		logger.debug("sendServiceReistryQuery started...");		
		Assert.notNull(gueryForm, "gueryForm is null.");
		
		final UriComponents queryUri = getServiceRegistryQueryUri();
		final ResponseEntity<ServiceQueryResultDTO> response = httpService.sendRequest(queryUri, HttpMethod.POST, ServiceQueryResultDTO.class, gueryForm);
		
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
		
		final Relay relay = gatekeeperMatchmaker.doMatchmaking(new GatekeeperMatchmakingParameters(targetCloud));
		try {
			final Session session = relayClient.createConnection(relay.getAddress(), relay.getPort());
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
	public OrchestrationResponseDTO queryAuthorizationBasedOnOchestrationResponse(final CloudRequestDTO requesterCloud, final OrchestrationResponseDTO orchestrationResponse) {
		logger.debug("queryAuthorizationBasedOnOchestrationResponse started...");
		
		Assert.notNull(requesterCloud, "Requester cloud is null.");
		Assert.isTrue(orchestrationResponse != null && orchestrationResponse.getResponse() != null && !orchestrationResponse.getResponse().isEmpty(), "Ochestration response is null or empty.");
		
		final UriComponents authInterCheckUri = getAuthInterCheckUri();
		final AuthorizationInterCloudCheckRequestDTO authRequest = createAuthorizationRequestFromOrchestrationResponse(requesterCloud, orchestrationResponse);
		final ResponseEntity<AuthorizationInterCloudCheckResponseDTO> response = httpService.sendRequest(authInterCheckUri, HttpMethod.POST, AuthorizationInterCloudCheckResponseDTO.class, authRequest);

		filterOrchestrationResponseWithAuthData(orchestrationResponse, response.getBody());
		
		return orchestrationResponse;
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------		
	private Map<Cloud,Relay> getOneGatekeeperRelayPerCloud(final List<Cloud> clouds) {
		logger.debug("getOneGatekeeperRelayPerCloud started...");
		
		final Map<Cloud,Relay> realyPerCloud = new HashMap<>();
		for (final Cloud cloud : clouds) {
			final Relay relay = gatekeeperMatchmaker.doMatchmaking(new GatekeeperMatchmakingParameters(cloud));
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
		
		if (arrowheadContext.containsKey(CommonConstants.SR_QUERY_URI)) {
			try {
				return (UriComponents) arrowheadContext.get(CommonConstants.SR_QUERY_URI);
			} catch (final ClassCastException ex) {
				throw new ArrowheadException("Gatekeeper can't find Service Registry Query URI.");
			}
		}
		
		throw new ArrowheadException("Gatekeeper can't find Service Registry Query URI.");
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
}