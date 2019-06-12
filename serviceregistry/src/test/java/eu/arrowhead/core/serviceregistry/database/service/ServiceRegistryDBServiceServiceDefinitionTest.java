package eu.arrowhead.core.serviceregistry.database.service;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.test.context.junit4.SpringRunner;

import eu.arrowhead.common.database.entity.ServiceDefinition;
import eu.arrowhead.common.database.repository.ServiceDefinitionRepository;
import eu.arrowhead.common.exception.InvalidParameterException;

@RunWith (SpringRunner.class)
public class ServiceRegistryDBServiceServiceDefinitionTest {
	
	//=================================================================================================
	// members
	
	@InjectMocks
	private ServiceRegistryDBService serviceRegistryDBService; 
		
	@Mock
	private ServiceDefinitionRepository serviceDefinitionRepository;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	//Tests of getServiceDefinitionById
	
	@Test (expected = InvalidParameterException.class)
	public void getServiceDefinitionByIdTest() {
		when(serviceDefinitionRepository.findById(anyLong())).thenReturn(Optional.ofNullable(null));
		serviceRegistryDBService.getServiceDefinitionById(1);
	}
	//-------------------------------------------------------------------------------------------------
	//Tests of getServiceDefinitionEntries
	
	@Test (expected = InvalidParameterException.class)
	public void getServiceDefinitionEntriesTest( ) {
		serviceRegistryDBService.getServiceDefinitionEntries(0, 10, Direction.ASC, "notValid");
	}
	
	//-------------------------------------------------------------------------------------------------
	//Tests of createServiceDefinition
	
	@Test (expected = InvalidParameterException.class)
	public void createServiceDefinitionTestWithNullInput() {
		serviceRegistryDBService.createServiceDefinition(null);
	}
	
	@Test (expected = InvalidParameterException.class)
	public void createServiceDefinitionTestWithBlankStringInput() {
		serviceRegistryDBService.createServiceDefinition("       ");
	}
	
	@Test (expected = InvalidParameterException.class)
	public void createServiceDefinitionTestOfUniqueKeyViolation() {
		final String testDefinition = "alreadyexiststest";
		final Optional<ServiceDefinition> serviceDefinitionEntry = Optional.of(new ServiceDefinition(testDefinition));
		when(serviceDefinitionRepository.findByServiceDefinition(eq(testDefinition))).thenReturn(serviceDefinitionEntry);
		serviceRegistryDBService.createServiceDefinition(testDefinition);
	}
	
	@Test (expected = InvalidParameterException.class)
	public void createServiceDefinitionTestCaseInsensitivityOfUniqueKeyViolation() {
		final String testDefinition = "alreadyexiststest";
		final Optional<ServiceDefinition> serviceDefinitionEntry = Optional.of(new ServiceDefinition(testDefinition));
		when(serviceDefinitionRepository.findByServiceDefinition(eq(testDefinition))).thenReturn(serviceDefinitionEntry);
		serviceRegistryDBService.createServiceDefinition(testDefinition.toUpperCase());
	}
	
	@Test (expected = InvalidParameterException.class)
	public void createServiceDefinitionTestLeadingTrailingSpaceSensitivityOfUniqueKeyViolation() {
		final String testDefinition = "alreadyexiststest";
		final Optional<ServiceDefinition> serviceDefinitionEntry = Optional.of(new ServiceDefinition(testDefinition));
		when(serviceDefinitionRepository.findByServiceDefinition(eq(testDefinition))).thenReturn(serviceDefinitionEntry);
		serviceRegistryDBService.createServiceDefinition("  " + testDefinition + "  ");
	}
	
	//-------------------------------------------------------------------------------------------------
	//Tests of updateServiceDefinitionById
	
	@Test (expected = InvalidParameterException.class)
	public void updateServiceDefinitionByIdTestWithNullInput() {
		serviceRegistryDBService.updateServiceDefinitionById(0, null);
	}
	
	@Test (expected = InvalidParameterException.class)
	public void updateServiceDefinitionByIdTestWithBlankStringInput() {
		serviceRegistryDBService.updateServiceDefinitionById(0, "   ");
	}
	
	@Test (expected = InvalidParameterException.class)
	public void updateServiceDefinitionByIdTestOfUniqueKeyViolation() {		
		final String testDefinition0 = "testdefinition0";
		final long testId0 = 0;
		final ServiceDefinition serviceDefinition0 = new ServiceDefinition(testDefinition0);
		serviceDefinition0.setId(testId0);
		final Optional<ServiceDefinition> serviceDefinitionEntry0 = Optional.of(serviceDefinition0);
		when(serviceDefinitionRepository.findById(eq(testId0))).thenReturn(serviceDefinitionEntry0);
		when(serviceDefinitionRepository.findByServiceDefinition(eq(testDefinition0))).thenReturn(serviceDefinitionEntry0);
		
		final String testDefinition1 = "testdefinition1";
		final long testId1 = 1;
		final ServiceDefinition serviceDefinition1 = new ServiceDefinition(testDefinition1);
		serviceDefinition1.setId(testId1);
		final Optional<ServiceDefinition> serviceDefinitionEntry1 = Optional.of(serviceDefinition1);
		when(serviceDefinitionRepository.findById(eq(testId1))).thenReturn(serviceDefinitionEntry1);
		when(serviceDefinitionRepository.findByServiceDefinition(eq(testDefinition1))).thenReturn(serviceDefinitionEntry1);
		
		serviceRegistryDBService.updateServiceDefinitionById(testId0, testDefinition1);
	}
	
	@Test (expected = InvalidParameterException.class)
	public void updateServiceDefinitionByIdTestCaseInsensitivityOfUniqueKeyViolation() {
		final String testDefinition0 = "testdefinition0";
		final long testId0 = 0;
		final ServiceDefinition serviceDefinition0 = new ServiceDefinition(testDefinition0);
		serviceDefinition0.setId(testId0);
		final Optional<ServiceDefinition> serviceDefinitionEntry0 = Optional.of(serviceDefinition0);
		when(serviceDefinitionRepository.findById(eq(testId0))).thenReturn(serviceDefinitionEntry0);
		when(serviceDefinitionRepository.findByServiceDefinition(eq(testDefinition0))).thenReturn(serviceDefinitionEntry0);
		
		final String testDefinition1 = "testdefinition1";
		final long testId1 = 1;
		final ServiceDefinition serviceDefinition1 = new ServiceDefinition(testDefinition1);
		serviceDefinition1.setId(testId1);
		final Optional<ServiceDefinition> serviceDefinitionEntry1 = Optional.of(serviceDefinition1);
		when(serviceDefinitionRepository.findById(eq(testId1))).thenReturn(serviceDefinitionEntry1);
		when(serviceDefinitionRepository.findByServiceDefinition(eq(testDefinition1))).thenReturn(serviceDefinitionEntry1);
		
		serviceRegistryDBService.updateServiceDefinitionById(testId0, testDefinition1.toUpperCase());
	}
	
	@Test (expected = InvalidParameterException.class)
	public void updateServiceDefinitionByIdTestLeadingTrailingSpaceSensitivityOfUniqueKeyViolation() {
		final String testDefinition0 = "testdefinition0";
		final long testId0 = 0;
		final ServiceDefinition serviceDefinition0 = new ServiceDefinition(testDefinition0);
		serviceDefinition0.setId(testId0);
		final Optional<ServiceDefinition> serviceDefinitionEntry0 = Optional.of(serviceDefinition0);
		when(serviceDefinitionRepository.findById(eq(testId0))).thenReturn(serviceDefinitionEntry0);
		when(serviceDefinitionRepository.findByServiceDefinition(eq(testDefinition0))).thenReturn(serviceDefinitionEntry0);
		
		final String testDefinition1 = "testdefinition1";
		final long testId1 = 1;
		final ServiceDefinition serviceDefinition1 = new ServiceDefinition(testDefinition1);
		serviceDefinition1.setId(testId1);
		final Optional<ServiceDefinition> serviceDefinitionEntry1 = Optional.of(serviceDefinition1);
		when(serviceDefinitionRepository.findById(eq(testId1))).thenReturn(serviceDefinitionEntry1);
		when(serviceDefinitionRepository.findByServiceDefinition(eq(testDefinition1))).thenReturn(serviceDefinitionEntry1);
		
		serviceRegistryDBService.updateServiceDefinitionById(testId0, "  " + testDefinition1 + "  ");
	}
	
	//-------------------------------------------------------------------------------------------------
	//Tests of removeServiceDefinitionById
	
	@Test (expected = InvalidParameterException.class)
	public void removeServiceDefinitionByIdTest() {
		when(serviceDefinitionRepository.existsById(anyLong())).thenReturn(false);
		serviceRegistryDBService.removeServiceDefinitionById(0);
	}	
}
