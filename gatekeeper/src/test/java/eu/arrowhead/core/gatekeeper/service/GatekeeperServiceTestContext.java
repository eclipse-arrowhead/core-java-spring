package eu.arrowhead.core.gatekeeper.service;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class GatekeeperServiceTestContext {

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@Bean
	@Primary // This bean is primary only in test context
	public GatekeeperService mockGatekeeperService() {
		return Mockito.mock(GatekeeperService.class);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Bean
	@Primary // This bean is primary only in test context
	public GatekeeperDriver mockGatekeeperDriver() {
		return Mockito.mock(GatekeeperDriver.class);
	}
}