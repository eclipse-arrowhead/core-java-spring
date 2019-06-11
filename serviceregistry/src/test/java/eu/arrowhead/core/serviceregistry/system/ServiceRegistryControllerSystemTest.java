package eu.arrowhead.core.serviceregistry.system;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;

import eu.arrowhead.common.database.service.ServiceRegistryDBService;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.core.serviceregistry.ServiceRegistryController;

@RunWith (SpringRunner.class)
public class ServiceRegistryControllerSystemTest {
	
	//=================================================================================================
	// members

	@InjectMocks
	ServiceRegistryController serviceRegistryController;
	
	@Mock
	ServiceRegistryDBService serviceRegistryDBService;
	
	//=================================================================================================
	// methods
		
	//-------------------------------------------------------------------------------------------------	
	
	@Test (expected = BadPayloadException.class)
	public void getSystemsTestWithNullPageButDefinedSizeInput() {
		serviceRegistryController.getSystems(null, 5, null, null);
	}

	
}
