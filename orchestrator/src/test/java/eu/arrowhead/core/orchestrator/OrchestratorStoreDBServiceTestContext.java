package eu.arrowhead.core.orchestrator;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import eu.arrowhead.core.orchestrator.database.service.OrchestratorStoreDBService;

@Configuration
public class OrchestratorStoreDBServiceTestContext {

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@Bean
	@Primary // This bean is primary only in test context
	public OrchestratorStoreDBService mockOrchestratorStoreDBService() {
		return Mockito.mock(OrchestratorStoreDBService.class);
	}
}
