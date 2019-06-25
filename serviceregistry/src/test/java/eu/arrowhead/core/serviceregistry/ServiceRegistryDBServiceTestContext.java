package eu.arrowhead.core.serviceregistry;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import eu.arrowhead.core.serviceregistry.database.service.ServiceRegistryDBService;

@Configuration
public class ServiceRegistryDBServiceTestContext {
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@Bean
	@Primary // This bean is primary only in test context
	public ServiceRegistryDBService mockServiceRegistryDBService() {
		return Mockito.mock(ServiceRegistryDBService.class);
	}
}