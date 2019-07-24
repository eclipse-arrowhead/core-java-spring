package eu.arrowhead.core.orchestrator;

import java.util.List;
import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponents;

import eu.arrowhead.common.ApplicationInitListener;
import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.core.CoreSystemService;

@Component
public class OrchestratorApplicationInitListener extends ApplicationInitListener {

	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	@Override
	protected List<CoreSystemService> getRequiredCoreSystemServiceUris() {
		return List.of(CoreSystemService.AUTH_TOKEN_GENERATION_SERVICE); // TODO: add all necessary services
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
		final UriComponents queryUri = createQueryByIdUri(scheme);
		context.put(CommonConstants.SR_QUERY_BY_ID_URI, queryUri);
		
		context.put(CommonConstants.REQUIRED_URI_LIST, getRequiredCoreSystemServiceUris());
		
	}
	
	//-------------------------------------------------------------------------------------------------
	private UriComponents createQueryByIdUri(final String scheme) {
		logger.debug("createQueryUri started...");
				
		final String registerUriStr = CommonConstants.SERVICE_REGISTRY_URI + CommonConstants.OP_SERVICE_REGISTRY_QUERY_BY_ID_URI;
		return Utilities.createURI(
				scheme, 
				coreSystemRegistrationProperties.getServiceRegistryAddress(), 
				coreSystemRegistrationProperties.getServiceRegistryPort(), 
				registerUriStr
				);
	}
}