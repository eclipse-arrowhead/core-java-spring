package eu.arrowhead.core.orchestrator;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import eu.arrowhead.common.ApplicationInitListener;
import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.core.CoreSystemService;
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

	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	@Override
	protected List<CoreSystemService> getRequiredCoreSystemServiceUris() {
		return List.of(CoreSystemService.AUTH_TOKEN_GENERATION_SERVICE, CoreSystemService.AUTH_CONTROL_INTRA_SERVICE); // TODO: add all necessary services
	}
}