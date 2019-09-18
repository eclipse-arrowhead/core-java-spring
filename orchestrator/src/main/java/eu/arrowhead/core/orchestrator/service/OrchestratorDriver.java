package eu.arrowhead.core.orchestrator.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.util.UriComponents;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.core.CoreSystemService;
import eu.arrowhead.common.dto.internal.AuthorizationIntraCloudCheckRequestDTO;
import eu.arrowhead.common.dto.internal.AuthorizationIntraCloudCheckResponseDTO;
import eu.arrowhead.common.dto.internal.DTOConverter;
import eu.arrowhead.common.dto.internal.GSDQueryFormDTO;
import eu.arrowhead.common.dto.internal.GSDQueryResultDTO;
import eu.arrowhead.common.dto.internal.ICNRequestFormDTO;
import eu.arrowhead.common.dto.internal.ICNResultDTO;
import eu.arrowhead.common.dto.internal.IdIdListDTO;
import eu.arrowhead.common.dto.internal.TokenDataDTO;
import eu.arrowhead.common.dto.internal.TokenGenerationProviderDTO;
import eu.arrowhead.common.dto.internal.TokenGenerationRequestDTO;
import eu.arrowhead.common.dto.internal.TokenGenerationResponseDTO;
import eu.arrowhead.common.dto.shared.OrchestrationFormRequestDTO;
import eu.arrowhead.common.dto.shared.OrchestrationResultDTO;
import eu.arrowhead.common.dto.shared.ServiceInterfaceResponseDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryFormDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryResultDTO;
import eu.arrowhead.common.dto.shared.ServiceRegistryResponseDTO;
import eu.arrowhead.common.dto.shared.ServiceSecurityType;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.common.dto.shared.SystemResponseDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.http.HttpService;

@Component
public class OrchestratorDriver {

	//=================================================================================================
	// members
	
	private static final String AUTH_TOKEN_GENERATION_URI_KEY = CoreSystemService.AUTH_TOKEN_GENERATION_SERVICE.getServiceDefinition() + CoreCommonConstants.URI_SUFFIX;
	private static final String AUTH_INTRA_CHECK_URI_KEY = CoreSystemService.AUTH_CONTROL_INTRA_SERVICE.getServiceDefinition() + CoreCommonConstants.URI_SUFFIX;
	private static final String GATEKEEPER_INIT_GSD_URI_KEY = CoreSystemService.GATEKEEPER_GLOBAL_SERVICE_DISCOVERY.getServiceDefinition() + CoreCommonConstants.URI_SUFFIX;
	private static final String GATEKEEPER_INIT_ICN_URI_KEY = CoreSystemService.GATEKEEPER_INTER_CLOUD_NEGOTIATION.getServiceDefinition() + CoreCommonConstants.URI_SUFFIX;
	
	private static final Logger logger = LogManager.getLogger(OrchestratorDriver.class);
	
	@Autowired
	private HttpService httpService;

	@Resource(name = CommonConstants.ARROWHEAD_CONTEXT)
	private Map<String,Object> arrowheadContext;
	
	@Value(CoreCommonConstants.$AUTH_TOKEN_TTL_IN_MINUTES_WD)
	private int tokenDuration;
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	// The two boolean parameters override the corresponding settings in the form
	public ServiceQueryResultDTO queryServiceRegistry(final ServiceQueryFormDTO form, final boolean metadataSearch, final boolean pingProviders) {
		logger.debug("queryServiceRegistry started...");
		Assert.notNull(form, "Form is null.");
		
		// overriding settings
		form.setPingProviders(pingProviders);
		if (!metadataSearch) {
			form.setMetadataRequirements(null);
		}
		
		final UriComponents queryUri = getQueryUri();
		final ResponseEntity<ServiceQueryResultDTO> response = httpService.sendRequest(queryUri, HttpMethod.POST, ServiceQueryResultDTO.class, form);
		
		return response.getBody();
	}
	
	//-------------------------------------------------------------------------------------------------
	public SystemResponseDTO queryServiceRegistryBySystemId(final long consumerSystemId) {
		logger.debug("queryByIdServiceRegistry started...");
		Assert.isTrue(consumerSystemId > 0, "ConsumerSystemId is less than 1.");
		
		final UriComponents queryBySystemIdUri = getQueryBySystemIdUri().expand(Map.of(CoreCommonConstants.COMMON_FIELD_NAME_ID, String.valueOf(consumerSystemId)));
		final ResponseEntity<SystemResponseDTO> response = httpService.sendRequest(queryBySystemIdUri, HttpMethod.GET, SystemResponseDTO.class);
		
		return response.getBody();
	}
	
	//-------------------------------------------------------------------------------------------------
	public SystemResponseDTO queryServiceRegistryBySystemRequestDTO(final SystemRequestDTO consumerSystemRequestDTO) {
		logger.debug("queryServiceRegistryBySystemRequestDTO started...");
		Assert.notNull(consumerSystemRequestDTO, "ConsumerSystemRequestDTO is null.");

		final UriComponents queryBySystemDTOUri = getQueryBySystemDTOUri();
		final ResponseEntity<SystemResponseDTO> response = httpService.sendRequest(queryBySystemDTOUri, HttpMethod.POST, SystemResponseDTO.class, consumerSystemRequestDTO);
		
		return response.getBody();
	}
	//-------------------------------------------------------------------------------------------------
	public List<OrchestrationResultDTO> generateAuthTokens(final OrchestrationFormRequestDTO request, final List<OrchestrationResultDTO> orList) {
		logger.debug("generateAuthTokens started...");
		
		Assert.notNull(request, "Request is null.");
		Assert.notNull(orList, "Orchestration result list is null.");
		
		final List<TokenGenHelper> tokenGenHelperList = convertOrchestrationResultListToTokenGenHelperList(orList);
		if (tokenGenHelperList.isEmpty()) {
			return orList;
		}
		
		final UriComponents tokenGenerationUri = getAuthTokenGenerationUri();
		for (final TokenGenHelper helper : tokenGenHelperList) {
			final TokenGenerationRequestDTO payload = new TokenGenerationRequestDTO(request.getRequesterSystem(), request.getRequesterCloud(), helper.getProviders(), helper.getService(),
																				    tokenDuration > 0 ? tokenDuration : null);
			final ResponseEntity<TokenGenerationResponseDTO> response = httpService.sendRequest(tokenGenerationUri, HttpMethod.POST, TokenGenerationResponseDTO.class, payload);
			
			final TokenGenerationResponseDTO tokenGenerationResult = response.getBody();
			updateOrchestrationResultsWithTokenData(orList, helper.getService(), tokenGenerationResult.getTokenData());
		}
		
		return orList;
	}
	
	//-------------------------------------------------------------------------------------------------
	public List<ServiceRegistryResponseDTO> queryAuthorization(final SystemRequestDTO consumer, final List<ServiceRegistryResponseDTO> providers) {
		logger.debug("queryAuthorization started...");
		
		Assert.notNull(consumer, "consumer is null.");
		Assert.notNull(providers, "providers list is null.");
		
		if (!providers.isEmpty()) {
			final UriComponents checkUri = getAuthIntraCheckUri();
			final long serviceDefinitionId = providers.get(0).getServiceDefinition().getId();
			final List<IdIdListDTO> providerIdsWithInterfaceIds = convertSRResultsToProviderIdListWithInterfaceIds(providers);
			final AuthorizationIntraCloudCheckRequestDTO payload = new AuthorizationIntraCloudCheckRequestDTO(consumer, serviceDefinitionId, providerIdsWithInterfaceIds);
			final ResponseEntity<AuthorizationIntraCloudCheckResponseDTO> response = httpService.sendRequest(checkUri, HttpMethod.POST, AuthorizationIntraCloudCheckResponseDTO.class, payload);
			
			filterProviderListUsingAuthorizationResult(providers, response.getBody());
		}
		
		return providers;
	}
	
	//-------------------------------------------------------------------------------------------------
	public ICNResultDTO doInterCloudNegotiation(final ICNRequestFormDTO icnForm) {
		logger.debug("doInterCloudNegotiation started...");
		Assert.notNull(icnForm, "ICNResultDTO is null.");
		
		final UriComponents icnUri = getGatekeeperICNUri();
		final ResponseEntity<ICNResultDTO> response = httpService.sendRequest(icnUri, HttpMethod.POST, ICNResultDTO.class, icnForm);
		
		return response.getBody();
	}

	
	//-------------------------------------------------------------------------------------------------
	public GSDQueryResultDTO doGlobalServiceDiscovery(final GSDQueryFormDTO gsdForm ) {
		logger.debug("doGlobalServiceDiscovery started...");
		Assert.notNull(gsdForm, "GSDPollRequestDTO is null.");
		
		final UriComponents gsdUri = getGatekeeperGSDUri();
		final ResponseEntity<GSDQueryResultDTO> response = httpService.sendRequest(gsdUri, HttpMethod.POST, GSDQueryResultDTO.class, gsdForm);
		
		return response.getBody();
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private UriComponents getQueryUri() {
		logger.debug("getQueryUri started...");
		
		if (arrowheadContext.containsKey(CoreCommonConstants.SR_QUERY_URI)) {
			try {
				return (UriComponents) arrowheadContext.get(CoreCommonConstants.SR_QUERY_URI);
			} catch (final ClassCastException ex) {
				throw new ArrowheadException("Orchestrator can't find Service Registry Query URI.");
			}
		}
		
		throw new ArrowheadException("Orchestrator can't find Service Registry Query URI.");
	}
	
	//-------------------------------------------------------------------------------------------------
	private UriComponents getQueryBySystemIdUri() {
		logger.debug("getQueryByIdUri started...");
		
		if (arrowheadContext.containsKey(CoreCommonConstants.SR_QUERY_BY_SYSTEM_ID_URI)) {
			try {
				return (UriComponents) arrowheadContext.get(CoreCommonConstants.SR_QUERY_BY_SYSTEM_ID_URI);
			} catch (final ClassCastException ex) {
				throw new ArrowheadException("Orchestrator can't find Service Registry Query By Id URI.");
			}
		}
		
		throw new ArrowheadException("Orchestrator can't find Service Registry Query By Id URI.");
	}
	
	//-------------------------------------------------------------------------------------------------
	private UriComponents getQueryBySystemDTOUri() {
		logger.debug("getQueryBySystemDTOUri started...");
		
		if (arrowheadContext.containsKey(CoreCommonConstants.SR_QUERY_BY_SYSTEM_DTO_URI)) {
			try {
				return (UriComponents) arrowheadContext.get(CoreCommonConstants.SR_QUERY_BY_SYSTEM_DTO_URI);
			} catch (final ClassCastException ex) {
				throw new ArrowheadException("Orchestrator can't find Service Registry Query By DTO URI.");
			}
		}
		
		throw new ArrowheadException("Orchestrator can't find Service Registry Query By DTO URI.");
	}

	//-------------------------------------------------------------------------------------------------
	private UriComponents getAuthTokenGenerationUri() {
		logger.debug("getAuthTokenGenerationUri started...");
		
		if (arrowheadContext.containsKey(AUTH_TOKEN_GENERATION_URI_KEY)) {
			try {
				return (UriComponents) arrowheadContext.get(AUTH_TOKEN_GENERATION_URI_KEY);
			} catch (final ClassCastException ex) {
				throw new ArrowheadException("Orchestrator can't find token generation URI.");
			}
		}
		
		throw new ArrowheadException("Orchestrator can't find token generation URI.");
	}
	
	//-------------------------------------------------------------------------------------------------
	private UriComponents getAuthIntraCheckUri() {
		logger.debug("getAuthIntraCheckUri started...");
		
		if (arrowheadContext.containsKey(AUTH_INTRA_CHECK_URI_KEY)) {
			try {
				return (UriComponents) arrowheadContext.get(AUTH_INTRA_CHECK_URI_KEY);
			} catch (final ClassCastException ex) {
				throw new ArrowheadException("Orchestrator can't find authorization check URI.");
			}
		}
		
		throw new ArrowheadException("Orchestrator can't find authorization check URI.");
	}
	
	//-------------------------------------------------------------------------------------------------
	private UriComponents getGatekeeperGSDUri() {
		logger.debug("getGatekeeperGSDUri started...");
		
		if (arrowheadContext.containsKey(GATEKEEPER_INIT_GSD_URI_KEY)) {
			try {
				return (UriComponents) arrowheadContext.get(GATEKEEPER_INIT_GSD_URI_KEY);
			} catch (final ClassCastException ex) {
				throw new ArrowheadException("Orchestrator can't find gatekeeper init_gsd URI.");
			}
		}
		
		throw new ArrowheadException("Orchestrator can't find gatekeeper init_gsd URI.");
	}
	
	//-------------------------------------------------------------------------------------------------
	private UriComponents getGatekeeperICNUri() {
		logger.debug("getGatekeeperICNUri started...");
		
		if (arrowheadContext.containsKey(GATEKEEPER_INIT_ICN_URI_KEY)) {
			try {
				return (UriComponents) arrowheadContext.get(GATEKEEPER_INIT_ICN_URI_KEY);
			} catch (final ClassCastException ex) {
				throw new ArrowheadException("Orchestrator can't find gatekeeper init_icn URI.");
			}
		}
		
		throw new ArrowheadException("Orchestrator can't find gatekeeper init_icn URI.");
	}
	
	//-------------------------------------------------------------------------------------------------
	private List<TokenGenHelper> convertOrchestrationResultListToTokenGenHelperList(final List<OrchestrationResultDTO> orList) {
		logger.debug("convertOrchestrationResultListToTokenGenHelperList started...");
		
		final Map<String,TokenGenHelper> serviceMap = new HashMap<>();
		
		for (final OrchestrationResultDTO result : orList) {
			if (result.getSecure() == ServiceSecurityType.TOKEN) {
				final String serviceDefinition = result.getService().getServiceDefinition();
				
				TokenGenHelper helper;
				if (serviceMap.containsKey(serviceDefinition)) {
					helper = serviceMap.get(serviceDefinition);
				} else {
					helper = new TokenGenHelper(serviceDefinition);
					serviceMap.put(serviceDefinition, helper);
				}
				
				helper.getProviders().add(createTokenGenerationProvider(result));
			}
		}
		
		return new ArrayList<>(serviceMap.values());
	}
	
	//-------------------------------------------------------------------------------------------------
	private TokenGenerationProviderDTO createTokenGenerationProvider(final OrchestrationResultDTO result) {
		logger.debug("createTokenGenerationProvider started...");
		
		return new TokenGenerationProviderDTO(DTOConverter.convertSystemResponseDTOToSystemRequestDTO(result.getProvider()),
											  convertServiceInterfaceListToServiceInterfaceNameList(result.getInterfaces()));
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S1612")
	private List<String> convertServiceInterfaceListToServiceInterfaceNameList(final List<ServiceInterfaceResponseDTO> intfs) {
		logger.debug("convertServiceInterfaceListToServiceInterfaceNameList started...");
		
		if (intfs == null) {
			return List.of();
		}
		
		return intfs.stream().map(dto -> dto.getInterfaceName()).collect(Collectors.toList());
	}
	
	//-------------------------------------------------------------------------------------------------
	private void updateOrchestrationResultsWithTokenData(final List<OrchestrationResultDTO> orList, final String serviceDefinition, final List<TokenDataDTO> tokenDataList) {
		logger.debug("updateOrchestrationResultsWithTokenData started...");
		
		for (final OrchestrationResultDTO result : orList) {
			if (result.getService().getServiceDefinition().equals(serviceDefinition)) {
				for (final TokenDataDTO tokenData : tokenDataList) {
					if (systemEquals(result.getProvider(), tokenData.getProviderName(), tokenData.getProviderAddress(), tokenData.getProviderPort())) {
						result.setAuthorizationTokens(tokenData.getTokens());
					}
				}
			}
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private boolean systemEquals(final SystemResponseDTO system, final String systemName, final String systemAddress, final int systemPort) {
		return system.getSystemName().equals(systemName) && system.getAddress().equals(systemAddress) && system.getPort() == systemPort;
	}
	
	//-------------------------------------------------------------------------------------------------
	private List<IdIdListDTO> convertSRResultsToProviderIdListWithInterfaceIds(final List<ServiceRegistryResponseDTO> providers) {
		logger.debug("convertSRResultsToProviderIdListWithInterfaceIds started...");
		
		final List<IdIdListDTO> result = new ArrayList<>(providers.size());
		for (final ServiceRegistryResponseDTO srEntry : providers) {
			final IdIdListDTO dto = new IdIdListDTO(srEntry.getProvider().getId(), convertServiceInterfaceListToServiceInterfaceIdList(srEntry.getInterfaces()));
			result.add(dto);
		}
		
		return result;
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
	// method may change providers list
	private void filterProviderListUsingAuthorizationResult(final List<ServiceRegistryResponseDTO> providers, final AuthorizationIntraCloudCheckResponseDTO authResult) {
		logger.debug("filterProviderListUsingAuthorizationResult started...");
		
		if (authResult.getAuthorizedProviderIdsWithInterfaceIds().isEmpty()) {
			// consumer has no access to any of the specified providers
			providers.clear();
		} else {
			final Map<Long,List<Long>> authMap = convertAuthorizationResultsToMap(authResult.getAuthorizedProviderIdsWithInterfaceIds());
			for (final Iterator<ServiceRegistryResponseDTO> it = providers.iterator(); it.hasNext();) {
				final ServiceRegistryResponseDTO srEntry = it.next();
				if (authMap.containsKey(srEntry.getProvider().getId())) {
					final List<Long> authorizedInterfaceIds = authMap.get(srEntry.getProvider().getId());
					srEntry.getInterfaces().removeIf(e -> !authorizedInterfaceIds.contains(e.getId()));
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
	
	//=================================================================================================
	// nested classes

	//-------------------------------------------------------------------------------------------------
	private static class TokenGenHelper {
		
		//=================================================================================================
		// members
		
		private final String service;
		private final List<TokenGenerationProviderDTO> providers = new ArrayList<>();
		
		//=================================================================================================
		// methods
		
		//-------------------------------------------------------------------------------------------------
		public TokenGenHelper(final String service) {
			this.service = service;
		}

		//-------------------------------------------------------------------------------------------------
		public String getService() { return service; }
		public List<TokenGenerationProviderDTO> getProviders() { return providers; 	}
	}
}