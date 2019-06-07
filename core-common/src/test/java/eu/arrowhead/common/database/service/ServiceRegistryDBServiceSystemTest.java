package eu.arrowhead.common.database.service;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;

import eu.arrowhead.common.database.entity.System;
import eu.arrowhead.common.database.repository.SystemRepository;
import eu.arrowhead.common.exception.InvalidParameterException;

@RunWith (SpringRunner.class)
public class ServiceRegistryDBServiceSystemTest {
	//=================================================================================================
	// members
	
	@InjectMocks
	ServiceRegistryDBService serviceRegistryDBService; 
	
	@Mock
	SystemRepository systemRepository;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	
	@Test
	public void crateSystemSystemNameNullTest() {

		boolean isNullDefintionThrowInvalidParameterException = false;
		try {
			serviceRegistryDBService.createSystem(null, "x", 1, "x");
		} catch (final InvalidParameterException ex) {
			isNullDefintionThrowInvalidParameterException = true;
		}
		assertTrue(isNullDefintionThrowInvalidParameterException);		
	}
	
	//-------------------------------------------------------------------------------------------------
	
	@Test
	public void crateSystemAdrressNullTest() {

		boolean isNullDefintionThrowInvalidParameterException = false;
		try {
			serviceRegistryDBService.createSystem("x", null, 1, "x");
		} catch (final InvalidParameterException ex) {
			isNullDefintionThrowInvalidParameterException = true;
		}
		assertTrue(isNullDefintionThrowInvalidParameterException);		
	}
	
	//-------------------------------------------------------------------------------------------------
	
	@Test
	public void crateSystemSystemNameEmptyStringTest() {

		boolean isEmptyStringThrowInvalidParameterException = false;
		try {
			serviceRegistryDBService.createSystem("", "x", 1, "x");
		} catch (final InvalidParameterException ex) {
			isEmptyStringThrowInvalidParameterException = true;
		}
		assertTrue(isEmptyStringThrowInvalidParameterException);		
	}
	
	//-------------------------------------------------------------------------------------------------
	
	@Test
	public void crateSystemAdrressEmptyStringTest() {

		boolean isEmptyStringThrowInvalidParameterException = false;
		try {
			serviceRegistryDBService.createSystem("x", "", 1, "x");
		} catch (final InvalidParameterException ex) {
			isEmptyStringThrowInvalidParameterException = true;
		}
		assertTrue(isEmptyStringThrowInvalidParameterException);		
	}
	
	//-------------------------------------------------------------------------------------------------
	
	@Test
	public void uniqueKeyViolationTest() {
		//Testing unique key violation
		final String systemName = "alreadyexiststest";
		final String address = "alreadyexiststest";
		final int port = 1;
		
		final Optional<System> system = Optional.of(new System(systemName, address, port, null));
		when(systemRepository.findBySystemNameAndAddressAndPort(eq(systemName), eq(address), eq(port))).thenReturn(system);
		boolean isExistingDefinitionThrowInvalidParameterException = false;
		try {
			serviceRegistryDBService.createSystem(systemName, address, port, null);
		} catch (final InvalidParameterException ex) {
			isExistingDefinitionThrowInvalidParameterException = true;
		}
		assertTrue(isExistingDefinitionThrowInvalidParameterException);
	}
	
	//-------------------------------------------------------------------------------------------------
	
	@Test
	public void uniqueKeyViolationNoEffectOfWhiteSpaceTest() {
		//Testing unique key violation
		final String systemName = "alreadyexiststest";
		final String address = "alreadyexiststest";
		final int port = 1;
		
		final Optional<System> system = Optional.of(new System(systemName, address, port, null));
		when(systemRepository.findBySystemNameAndAddressAndPort(eq(systemName), eq(address), eq(port))).thenReturn(system);
		boolean isExistingDefinitionThrowInvalidParameterException = false;
		try {
			serviceRegistryDBService.createSystem(" "+systemName+" ", " "+address+" ", port, null);
		} catch (final InvalidParameterException ex) {
			isExistingDefinitionThrowInvalidParameterException = true;
		}
		assertTrue(isExistingDefinitionThrowInvalidParameterException);
	}
	
	//-------------------------------------------------------------------------------------------------
	
	@Test
	public void uniqueKeyViolationNoEffectOfCaseDifferenceTest() {
		//Testing unique key violation
		final String systemName = "alreadyexiststest";
		final String address = "alreadyexiststest";
		final int port = 1;
		
		final Optional<System> system = Optional.of(new System(systemName, address, port, null));
		when(systemRepository.findBySystemNameAndAddressAndPort(eq(systemName), eq(address), eq(port))).thenReturn(system);
		boolean isExistingDefinitionThrowInvalidParameterException = false;
		try {
			serviceRegistryDBService.createSystem(systemName.toUpperCase(), address.toUpperCase(), port, null);
		} catch (final InvalidParameterException ex) {
			isExistingDefinitionThrowInvalidParameterException = true;
		}
		assertTrue(isExistingDefinitionThrowInvalidParameterException);
	}
	
	//-------------------------------------------------------------------------------------------------
	
	@Test
	public void updateSystemByIdTest() {
		final String systemName0 = "alreadyexiststestsystemname0";
		final String address0 = "alreadyexiststestaddress0";
		final int port0 = 0;
		final long testId0 = 0;
		
		final System system0 = new System(systemName0, address0, port0, null);
		final Optional<System> systemOptional0 = Optional.of(system0);
		when(systemRepository.findById(eq(testId0))).thenReturn(systemOptional0);
		
		final String systemName1 = "alreadyexiststestsystemname1";
		final String address1 = "alreadyexiststestaddress1";
		final int port1 = 1;
		final long testId1 = 1;
		
		final System system1 = new System(systemName1, address1, port1, null);
		final Optional<System> systemOptional1 = Optional.of(system1);
		when(systemRepository.findById(eq(testId1))).thenReturn(systemOptional1);
		
	}	
}