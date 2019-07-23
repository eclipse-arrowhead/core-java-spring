package eu.arrowhead.core.orchestrator.service;

import java.util.ArrayList;
import java.util.HashMap;
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
import eu.arrowhead.common.core.CoreSystemService;
import eu.arrowhead.common.dto.DTOConverter;
import eu.arrowhead.common.dto.OrchestrationFormRequestDTO;
import eu.arrowhead.common.dto.OrchestrationResultDTO;
import eu.arrowhead.common.dto.ServiceInterfaceResponseDTO;
import eu.arrowhead.common.dto.ServiceQueryFormDTO;
import eu.arrowhead.common.dto.ServiceQueryResultDTO;
import eu.arrowhead.common.dto.ServiceSecurityType;
import eu.arrowhead.common.dto.SystemResponseDTO;
import eu.arrowhead.common.dto.TokenDataDTO;
import eu.arrowhead.common.dto.TokenGenerationProviderDTO;
import eu.arrowhead.common.dto.TokenGenerationRequestDTO;
import eu.arrowhead.common.dto.TokenGenerationResponseDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.http.HttpService;

@Component
public class OrchestratorDriver {

	//=================================================================================================
	// members
	
	private static final String AUTH_TOKEN_GENERATION_URI_KEY = CoreSystemService.AUTH_TOKEN_GENERATION_SERVICE.getServiceDefinition() + CommonConstants.URI_SUFFIX;

	private static final Logger logger = LogManager.getLogger(OrchestratorDriver.class);
	
	@Autowired
	private HttpService httpService;

	@Resource(name = CommonConstants.ARROWHEAD_CONTEXT)
	private Map<String,Object> arrowheadContext;
	
	@Value(CommonConstants.$AUTH_TOKEN_TTL_IN_MINUTES_WD)
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
	public List<OrchestrationResultDTO> generateAuthTokens(final OrchestrationFormRequestDTO request, final List<OrchestrationResultDTO> orList) {
		logger.debug("generateAuthTokens started...");
		
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

	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private UriComponents getQueryUri() {
		logger.debug("getQueryUri started...");
		
		if (arrowheadContext.containsKey(CommonConstants.SR_QUERY_URI)) {
			try {
				return (UriComponents) arrowheadContext.get(CommonConstants.SR_QUERY_URI);
			} catch (final ClassCastException ex) {
				throw new ArrowheadException("Orchestrator can't find Service Registry Query URI.");
			}
		}
		
		throw new ArrowheadException("Orchestrator can't find Service Registry Query URI.");
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
	private List<String> convertServiceInterfaceListToServiceInterfaceNameList(final List<ServiceInterfaceResponseDTO> intfs) {
		logger.debug("convertServiceInterfaceListToServiceInterfaceNameList started...");
		
		if (intfs == null) {
			return null;
		}
		
		return intfs.stream().map(dto -> dto.getInterfaceName()).collect(Collectors.toList());
	}
	
	//-------------------------------------------------------------------------------------------------
	private void updateOrchestrationResultsWithTokenData(final List<OrchestrationResultDTO> orList, final String serviceDefinition, final List<TokenDataDTO> tokenDataList) {
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
		return system.getSystemName().equals(systemName) &&
			   system.getAddress().equals(systemAddress) &&
			   system.getPort() == systemPort;
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