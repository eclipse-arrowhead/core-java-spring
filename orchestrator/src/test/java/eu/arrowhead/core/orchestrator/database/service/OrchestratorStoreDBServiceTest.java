package eu.arrowhead.core.orchestrator.database.service;

import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;

import eu.arrowhead.common.database.repository.OrchestratorStoreRepository;

@RunWith (SpringRunner.class)
public class OrchestratorStoreDBServiceTest {

	//=================================================================================================
	// members
	
	@InjectMocks
	OrchestratorStoreDBService orchestratorStoreDBService; 
	
	@Mock
	OrchestratorStoreRepository orchestratorStoreRepository;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------

}
