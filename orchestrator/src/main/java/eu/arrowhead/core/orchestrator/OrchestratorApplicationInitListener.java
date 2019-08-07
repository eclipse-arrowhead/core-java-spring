package eu.arrowhead.core.orchestrator;

import java.util.List;
import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponents;

import eu.arrowhead.common.ApplicationInitListener;
import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.core.CoreSystemService;
import eu.arrowhead.core.orchestrator.matchmaking.DefaultInterCloudProviderMatchmaker;
import eu.arrowhead.core.orchestrator.matchmaking.InterCloudProviderMatchmakingAlgorithm;
import eu.arrowhead.core.orchestrator.matchmaking.IntraCloudProviderMatchmakingAlgorithm;
import eu.arrowhead.core.orchestrator.matchmaking.RandomIntraCloudProviderMatchmaker;

@Component
public class OrchestratorApplicationInitListener extends ApplicationInitListener {
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Bean(CommonConstants.INTRA_CLOUD_PROVIDER_MATCHMAKER)
	public IntraCloudProviderMatchmakingAlgorithm getIntraCloudProviderMatchmaker() {
		return new RandomIntraCloudProviderMatchmaker();
	}
	
	//-------------------------------------------------------------------------------------------------
	@Bean(CommonConstants.INTER_CLOUD_PROVIDER_MATCHMAKER)
	public InterCloudProviderMatchmakingAlgorithm getInterCloudProviderMatchmaker() {
		return new DefaultInterCloudProviderMatchmaker();
	}

	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	@Override
	protected List<CoreSystemService> getRequiredCoreSystemServiceUris() {
		return List.of(CoreSystemService.AUTH_TOKEN_GENERATION_SERVICE, CoreSystemService.AUTH_CONTROL_INTRA_SERVICE); // TODO: add all necessary services
	}
	
	//-------------------------------------------------------------------------------------------------
	@Override
	protected void customInit(final ContextRefreshedEvent event) {				
		logger.debug("customInit started...");
		
		if (standaloneMode) {
			return;
		}
		
		final ApplicationContext appContext = event.getApplicationContext();
		
		@SuppressWarnings("unchecked")
		final Map<String,Object> context = appContext.getBean(CommonConstants.ARROWHEAD_CONTEXT, Map.class);
		
		final String scheme = sslProperties.isSslEnabled() ? CommonConstants.HTTPS : CommonConstants.HTTP;
		final UriComponents querySystemByIdUri = createQuerySystemByIdUri(scheme);
		context.put(CommonConstants.SR_QUERY_BY_SYSTEM_ID_URI, querySystemByIdUri);
		
		final UriComponents querySystemByDTOUri = createQuerySystemByDTOUri(scheme);
		context.put(CommonConstants.SR_QUERY_BY_SYSTEM_DTO_URI, querySystemByDTOUri);
		
		context.put(CommonConstants.REQUIRED_URI_LIST, getRequiredCoreSystemServiceUris());
		
	}
	
	//-------------------------------------------------------------------------------------------------
	private UriComponents createQuerySystemByIdUri(final String scheme) {
		logger.debug("createQuerySystemByIdUri started...");
				
		final String registyUriStr = CommonConstants.SERVICE_REGISTRY_URI + CommonConstants.OP_SERVICE_REGISTRY_QUERY_BY_SYSTEM_ID_URI;
		
		return Utilities.createURI(
				scheme, 
				coreSystemRegistrationProperties.getServiceRegistryAddress(), 
				coreSystemRegistrationProperties.getServiceRegistryPort(), 
				registyUriStr
				);
	}
	
	//-------------------------------------------------------------------------------------------------
	private UriComponents createQuerySystemByDTOUri(final String scheme) {
		logger.debug("createQuerySystemByDTOUri started...");
				
		final String registerUriStr = CommonConstants.SERVICE_REGISTRY_URI + CommonConstants.OP_SERVICE_REGISTRY_QUERY_BY_SYSTEM_DTO_URI;
		
		return Utilities.createURI(
				scheme, 
				coreSystemRegistrationProperties.getServiceRegistryAddress(), 
				coreSystemRegistrationProperties.getServiceRegistryPort(), 
				registerUriStr
				);
	}
}
