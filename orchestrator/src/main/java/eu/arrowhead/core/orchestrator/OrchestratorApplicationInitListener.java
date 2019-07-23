package eu.arrowhead.core.orchestrator;

import java.util.List;

import org.springframework.stereotype.Component;

import eu.arrowhead.common.ApplicationInitListener;
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
}