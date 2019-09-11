package eu.arrowhead.core.orchestrator;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import eu.arrowhead.core.orchestrator.database.service.OrchestratorStoreDBService;
import eu.arrowhead.core.orchestrator.service.OrchestratorService;

@Configuration
public class OrchestratorServiceTestContext {

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@Bean
	@Primary // This bean is primary only in test context
	public OrchestratorStoreDBService mockOrchestratorStoreDBService() {
		return Mockito.mock(OrchestratorStoreDBService.class);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Bean
	@Primary // This bean is primary only in test context
	public OrchestratorService mockOrchestratorService() {
		return Mockito.mock(OrchestratorService.class);
	}
}