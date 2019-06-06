package eu.arrowhead.common.database.service;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.anyLong;

import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.test.context.junit4.SpringRunner;

import eu.arrowhead.common.database.entity.ServiceDefinition;
import eu.arrowhead.common.database.repository.ServiceDefinitionRepository;
import eu.arrowhead.common.database.repository.ServiceRegistryRepository;
import eu.arrowhead.common.exception.InvalidParameterException;

@RunWith (SpringRunner.class)
public class ServiceRegistryDBServiceTest {
	
	//=================================================================================================
	// members
	
	@InjectMocks
	ServiceRegistryDBService serviceRegistryDBService; 
	
	@Mock
	ServiceRegistryRepository serviceRegistryRepository;
	
	@Mock
	ServiceDefinitionRepository serviceDefinitionRepository;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	
	@Test (expected = InvalidParameterException.class)
	public void getServiceDefinitionByIdTest() {
		when(serviceDefinitionRepository.findById(anyLong())).thenReturn(Optional.ofNullable(null));
		serviceRegistryDBService.getServiceDefinitionById(1);
	}
	//-------------------------------------------------------------------------------------------------
	
	@Test (expected = InvalidParameterException.class)
	public void getServiceDefinitionEntriesTest( ) {
		serviceRegistryDBService.getServiceDefinitionEntries(0, 10, Direction.ASC, "notValid");
	}
	
	//-------------------------------------------------------------------------------------------------
	
	@Test
	public void createServiceDefinitionTest() {
		//Testing with null input
		boolean isNullDefintionThrowInvalidParameterException = false;
		try {
			serviceRegistryDBService.createServiceDefinition(null);
		} catch (final InvalidParameterException ex) {
			isNullDefintionThrowInvalidParameterException = true;
		}
		assertTrue(isNullDefintionThrowInvalidParameterException);
		
		//Testing with blank String input
		boolean isBlankDefintionThrowInvalidParameterException = false;
		try {
			serviceRegistryDBService.createServiceDefinition("       ");
		} catch (final InvalidParameterException ex) {
			isBlankDefintionThrowInvalidParameterException = true;
		}
		assertTrue(isBlankDefintionThrowInvalidParameterException);
		
		//Testing unique key violation
		final String testDefinition = "alreadyexiststest";
		final Optional<ServiceDefinition> serviceDefinitionEntry = Optional.of(new ServiceDefinition(testDefinition));
		when(serviceDefinitionRepository.findByServiceDefinition(eq(testDefinition))).thenReturn(serviceDefinitionEntry);
		boolean isExistingDefinitionThrowInvalidParameterException = false;
		try {
			serviceRegistryDBService.createServiceDefinition(testDefinition);
		} catch (final InvalidParameterException ex) {
			isExistingDefinitionThrowInvalidParameterException = true;
		}
		assertTrue(isExistingDefinitionThrowInvalidParameterException);
		
		//Testing case insensitivity of unique key violation
		boolean isExistingUpperCaseDefinitionThrowInvalidParameterException = false;
		try {
			serviceRegistryDBService.createServiceDefinition(testDefinition.toUpperCase());
		} catch (final InvalidParameterException ex) {
			isExistingUpperCaseDefinitionThrowInvalidParameterException = true;
		}
		assertTrue(isExistingUpperCaseDefinitionThrowInvalidParameterException);
		
		//Testing leading-trailing space sensitivity of unique key violation
		boolean isExistingDefinitionWithSpacesThrowInvalidParameterException = false;
		try {
			serviceRegistryDBService.createServiceDefinition("  " + testDefinition + "  ");
		} catch (final InvalidParameterException ex) {
			isExistingDefinitionWithSpacesThrowInvalidParameterException = true;
		}
		assertTrue(isExistingDefinitionWithSpacesThrowInvalidParameterException);
	}
	
	//-------------------------------------------------------------------------------------------------
	
	@Test
	public void updateServiceDefinitionByIdTest() {
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
		
		
		//Testing with null input
		boolean isNullDefintionThrowInvalidParameterException = false;
		try {
			serviceRegistryDBService.updateServiceDefinitionById(testId0, null);
		} catch (final InvalidParameterException ex) {
			isNullDefintionThrowInvalidParameterException = true;
		}
		assertTrue(isNullDefintionThrowInvalidParameterException);
		
		//Testing with blank String input
		boolean isBlankDefintionThrowInvalidParameterException = false;
		try {
			serviceRegistryDBService.updateServiceDefinitionById(testId0, "   ");
		} catch (final InvalidParameterException ex) {
			isBlankDefintionThrowInvalidParameterException = true;
		}
		assertTrue(isBlankDefintionThrowInvalidParameterException);
		
		//Testing unique key violation
		boolean isExistingDefinitionThrowInvalidParameterException = false;
		try {
			serviceRegistryDBService.updateServiceDefinitionById(testId0, testDefinition1);
		} catch (final InvalidParameterException ex) {
			isExistingDefinitionThrowInvalidParameterException = true;
		}
		assertTrue(isExistingDefinitionThrowInvalidParameterException);
		
		//Testing case insensitivity of unique key violation
		boolean isExistingUpperCaseDefinitionThrowInvalidParameterException = false;
		try {
			serviceRegistryDBService.updateServiceDefinitionById(testId0, testDefinition1.toUpperCase());
		} catch (final InvalidParameterException ex) {
			isExistingUpperCaseDefinitionThrowInvalidParameterException = true;
		}
		assertTrue(isExistingUpperCaseDefinitionThrowInvalidParameterException);
		
		//Testing leading-trailing space sensitivity of unique key violation
		boolean isExistingDefinitionWithSpacesThrowInvalidParameterException = false;
		try {
			serviceRegistryDBService.updateServiceDefinitionById(testId0, "  " + testDefinition1 + "  ");
		} catch (final InvalidParameterException ex) {
			isExistingDefinitionWithSpacesThrowInvalidParameterException = true;
		}
		assertTrue(isExistingDefinitionWithSpacesThrowInvalidParameterException);
	}
	
	@Test (expected = InvalidParameterException.class)
	public void removeServiceDefinitionByIdTest() {
		when(serviceDefinitionRepository.existsById(anyLong())).thenReturn(false);
		serviceRegistryDBService.removeServiceDefinitionById(0);
	}
	
	//-------------------------------------------------------------------------------------------------
		
	@Test (expected = InvalidParameterException.class)
	public void testGetServiceReqistryEntries() {
		serviceRegistryDBService.getServiceReqistryEntries(0, 10, Direction.ASC, "notValid");
	}
	
	//-------------------------------------------------------------------------------------------------
	
	@Test (expected = InvalidParameterException.class)
	public void removeServiceRegistryEntryByIdTest() {
		when(serviceDefinitionRepository.existsById(anyLong())).thenReturn(false);
		serviceRegistryDBService.removeServiceRegistryEntryById(0);
	}
}
