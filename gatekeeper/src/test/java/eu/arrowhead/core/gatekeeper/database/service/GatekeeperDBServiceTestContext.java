package eu.arrowhead.core.gatekeeper.database.service;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class GatekeeperDBServiceTestContext {
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@Bean
	@Primary // This bean is primary only in test context
	public GatekeeperDBService mockGatekeeperDBService() {
		return Mockito.mock(GatekeeperDBService.class);
	}

}
