package eu.arrowhead.core.serviceregistry;

import static org.junit.Assert.assertTrue;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.junit4.SpringRunner;

import eu.arrowhead.common.database.entity.ServiceDefinition;
import eu.arrowhead.common.database.service.ServiceRegistryDBService;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.common.exception.InvalidParameterException;

@RunWith (SpringRunner.class)
public class ServiceRegistryControllerServiceDefinitionTest {
	
	//=================================================================================================
	// members

	@InjectMocks
	ServiceRegistryController serviceRegistryController;
	
	@Mock
	ServiceRegistryDBService serviceRegistryDBService;
	
	//=================================================================================================
	// methods
		
	//-------------------------------------------------------------------------------------------------
	//Tests of getServiceDefinitions
	
	@Test (expected = BadPayloadException.class)
	public void getServiceDefinitionsTestWithNullPageButDefinedSizeInput() {
		serviceRegistryController.getServiceDefinitions(null, 5, null, null);
	}
	
	@Test (expected = BadPayloadException.class)
	public void getServiceDefinitionsTestWithDefinedPageButNullSizeInput() {
		serviceRegistryController.getServiceDefinitions(0, null, null, null);
	}
	
	@Test (expected = BadPayloadException.class)
	public void getServiceDefinitionsTestWithInvalidSortDirectionFlagInput() {
		serviceRegistryController.getServiceDefinitions(null, null, "invalid", null);
	}
	
	
	
	@Test (expected = BadPayloadException.class)
	public void getServiceDefinitionsTestWithBlankSortFiedInput() {
		serviceRegistryController.getServiceDefinitions(null, null, "ASC", "   ");		
	}	

	//=================================================================================================
	// assistant methods
	
	private Page<ServiceDefinition> createServiceDefinitionPageForDBMocking(int amountOfEntry) {
		List<ServiceDefinition> serviceDefinitionList = new ArrayList<>();
		for (int i = 0; i < amountOfEntry; i++) {
			ServiceDefinition serviceDefinition = new ServiceDefinition("mockedService" + i);
			serviceDefinition.setId(i);
			ZonedDateTime timeStamp = ZonedDateTime.now();
			serviceDefinition.setCreatedAt(timeStamp);
			serviceDefinition.setUpdatedAt(timeStamp);
			serviceDefinitionList.add(serviceDefinition);
		}
		Page<ServiceDefinition> entries = new PageImpl<ServiceDefinition>(serviceDefinitionList);
		return entries;
	}
}
