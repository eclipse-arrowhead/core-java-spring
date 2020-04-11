package eu.arrowhead.core.datamanager;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import eu.arrowhead.core.datamanager.database.service.DataManagerDBService;

@Configuration
public class DataManagerDBServiceTestContext {
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@Bean
	@Primary // This bean is primary only in test context
	public DataManagerDBService mockDataManagerDBService() {
		return Mockito.mock(DataManagerDBService.class);
	}
}
