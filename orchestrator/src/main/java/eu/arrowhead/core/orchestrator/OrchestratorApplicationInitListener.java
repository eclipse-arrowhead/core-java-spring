package eu.arrowhead.core.orchestrator;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponents;

import eu.arrowhead.common.ApplicationInitListener;
import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.core.CoreSystemService;
import eu.arrowhead.core.orchestrator.matchmaking.CloudMatchmakingAlgorithm;
import eu.arrowhead.core.orchestrator.matchmaking.DefaultCloudMatchmaker;
import eu.arrowhead.core.orchestrator.matchmaking.DefaultInterCloudProviderMatchmaker;
import eu.arrowhead.core.orchestrator.matchmaking.InterCloudProviderMatchmakingAlgorithm;
import eu.arrowhead.core.orchestrator.matchmaking.IntraCloudProviderMatchmakingAlgorithm;
import eu.arrowhead.core.orchestrator.matchmaking.RandomIntraCloudProviderMatchmaker;

@Component
public class OrchestratorApplicationInitListener extends ApplicationInitListener {
	
	//=================================================================================================
	// members
	
	@Value(CoreCommonConstants.$ORCHESTRATOR_IS_GATEKEEPER_PRESENT_WD)
	private boolean gatekeeperIsPresent;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Bean(CoreCommonConstants.INTRA_CLOUD_PROVIDER_MATCHMAKER)
	public IntraCloudProviderMatchmakingAlgorithm getIntraCloudProviderMatchmaker() {
		return new RandomIntraCloudProviderMatchmaker();
	}
	
	//-------------------------------------------------------------------------------------------------
	@Bean(CoreCommonConstants.INTER_CLOUD_PROVIDER_MATCHMAKER)
	public InterCloudProviderMatchmakingAlgorithm getInterCloudProviderMatchmaker() {
		return new DefaultInterCloudProviderMatchmaker();
	}
	
	//-------------------------------------------------------------------------------------------------
	@Bean(CoreCommonConstants.CLOUD_MATCHMAKER)
	public CloudMatchmakingAlgorithm getCloudMatchmaker() {
		return new DefaultCloudMatchmaker();
	}

	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	@Override
	protected List<CoreSystemService> getRequiredCoreSystemServiceUris() {
		if (gatekeeperIsPresent) {
			return List.of(CoreSystemService.AUTH_TOKEN_GENERATION_SERVICE, CoreSystemService.AUTH_CONTROL_INTRA_SERVICE, CoreSystemService.GATEKEEPER_GLOBAL_SERVICE_DISCOVERY,
						   CoreSystemService.GATEKEEPER_INTER_CLOUD_NEGOTIATION);
		}
		
		return List.of(CoreSystemService.AUTH_TOKEN_GENERATION_SERVICE,	CoreSystemService.AUTH_CONTROL_INTRA_SERVICE);
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
		context.put(CoreCommonConstants.SR_QUERY_BY_SYSTEM_ID_URI, querySystemByIdUri);
		
		final UriComponents querySystemByDTOUri = createQuerySystemByDTOUri(scheme);
		context.put(CoreCommonConstants.SR_QUERY_BY_SYSTEM_DTO_URI, querySystemByDTOUri);
	}
	
	//-------------------------------------------------------------------------------------------------
	private UriComponents createQuerySystemByIdUri(final String scheme) {
		logger.debug("createQuerySystemByIdUri started...");
				
		final String registyUriStr = CommonConstants.SERVICE_REGISTRY_URI + CoreCommonConstants.OP_SERVICE_REGISTRY_QUERY_BY_SYSTEM_ID_URI;
		
		return Utilities.createURI(scheme, coreSystemRegistrationProperties.getServiceRegistryAddress(), coreSystemRegistrationProperties.getServiceRegistryPort(),	registyUriStr);
	}
	
	//-------------------------------------------------------------------------------------------------
	private UriComponents createQuerySystemByDTOUri(final String scheme) {
		logger.debug("createQuerySystemByDTOUri started...");
				
		final String registyUriStr = CommonConstants.SERVICE_REGISTRY_URI + CoreCommonConstants.OP_SERVICE_REGISTRY_QUERY_BY_SYSTEM_DTO_URI;
		
		return Utilities.createURI(scheme, coreSystemRegistrationProperties.getServiceRegistryAddress(), coreSystemRegistrationProperties.getServiceRegistryPort(), registyUriStr);
	}
}