package eu.arrowhead.core.serviceregistry.database.service;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.test.context.junit4.SpringRunner;

import eu.arrowhead.common.database.repository.ServiceRegistryRepository;
import eu.arrowhead.common.exception.InvalidParameterException;

@RunWith(SpringRunner.class)
public class ServiceRegistryDBServiceServiceRegistryTest {
	
	//=================================================================================================
	// members
	
	@InjectMocks
	ServiceRegistryDBService serviceRegistryDBService; 
	
	@Mock
	ServiceRegistryRepository serviceRegistryRepository;
	
	//=================================================================================================
	// methods
		
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGetAllServiceReqistryEntriesWithInvalidSortField() {
		serviceRegistryDBService.getServiceReqistryEntries(0, 10, Direction.ASC, "notValid");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testRemoveServiceRegistryEntryByIdWithNonExistentId() {
		when(serviceRegistryRepository.existsById(anyLong())).thenReturn(false);
		serviceRegistryDBService.removeServiceRegistryEntryById(1);
	}
}