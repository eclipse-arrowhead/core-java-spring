package eu.arrowhead.core.serviceregistry.database.service;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.test.context.junit4.SpringRunner;

import eu.arrowhead.common.CommonConstants;
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
	@Test (expected = InvalidParameterException.class)
	public void getSystemByIdTest() {
		when(systemRepository.findById(anyLong())).thenReturn(Optional.ofNullable(null));
		serviceRegistryDBService.getSystemById(1);		
	}	
	
	//-------------------------------------------------------------------------------------------------
	@Test (expected = InvalidParameterException.class)
	public void getSystemEntriesInvalidSortfiledValueTest() {
	
	final int validatedPage = 1;
	final int validatedSize = 1;
	final Direction validatedDirection = Direction.ASC;
	final String validatedSortField = CommonConstants.COMMON_FIELD_NAME_ID;

	final String invalidSortField = "invalisSortField";
	
	final String systemName0 = "testSystemName0";
	final String address0 = "testAddress0";
	final int port0 = 0;
	final String authenticationInfo0 = null;
	
	final System system = new System(systemName0, address0, port0, authenticationInfo0);
	
	final List<System> systemsList = new ArrayList<>();
	systemsList.add(system);
	final Page<System> systems  = new PageImpl<System>(systemsList);
	when(systemRepository.findAll(PageRequest.of(validatedPage, validatedSize, validatedDirection, validatedSortField))).thenReturn(systems);
	

	serviceRegistryDBService.getSystemEntries(validatedPage, validatedSize, validatedDirection, invalidSortField);
	
	}
	
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getSystemEntriesNullSortfiledValueTest() {
		final int validatedPage = 1;
	final int validatedSize = 1;
	final Direction validatedDirection = Direction.ASC;
	final String validatedSortField = CommonConstants.COMMON_FIELD_NAME_ID;
	
	final String invalidSortField = null;
	
	final String systemName0 = "testSystemName0";
	final String address0 = "testAddress0";
	final int port0 = 0;
	final String authenticationInfo0 = null;
	
	final System system = new System(systemName0, address0, port0, authenticationInfo0);
	
	final List<System> systemsList = new ArrayList<>();
	systemsList.add(system);
	final Page<System> systems  = new PageImpl<System>(systemsList);
	when(systemRepository.findAll(PageRequest.of(validatedPage, validatedSize, validatedDirection, validatedSortField))).thenReturn(systems);
	
	boolean isNullDefintionThrowInvalidParameterException = true;
	try {
		serviceRegistryDBService.getSystemEntries(validatedPage, validatedSize, validatedDirection, invalidSortField);
	} catch (final InvalidParameterException ex) {
		isNullDefintionThrowInvalidParameterException = false;
	}
	assertTrue(isNullDefintionThrowInvalidParameterException);	
	
	}
	
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getSystemEntriesEmptySortfiledValueTest() {
		final int validatedPage = 1;
	final int validatedSize = 1;
	final Direction validatedDirection = Direction.ASC;
	final String validatedSortField = CommonConstants.COMMON_FIELD_NAME_ID;
	
	final String invalidSortField = "";
	
	final String systemName0 = "testSystemName0";
	final String address0 = "testAddress0";
	final int port0 = 0;
	final String authenticationInfo0 = null;
	
	final System system = new System(systemName0, address0, port0, authenticationInfo0);
	
	final List<System> systemsList = new ArrayList<>();
	systemsList.add(system);
	final Page<System> systems  = new PageImpl<System>(systemsList);
	when(systemRepository.findAll(PageRequest.of(validatedPage, validatedSize, validatedDirection, validatedSortField))).thenReturn(systems);
	
	boolean isNullDefintionThrowInvalidParameterException = true;
	try {
		serviceRegistryDBService.getSystemEntries(validatedPage, validatedSize, validatedDirection, invalidSortField);
	} catch (final InvalidParameterException ex) {
		isNullDefintionThrowInvalidParameterException = false;
	}
	assertTrue(isNullDefintionThrowInvalidParameterException);	
	
	}
		
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
	public void updateSystemByIdInvalidIdTest() {
		final String systemName0 = "testSystemName0";
		final String address0 = "testAddress0";
		final int port0 = 0;
		final long testId0 = 0;
		final String authenticationInfo0 = null;
		
		final System system = new System(systemName0, address0, port0, authenticationInfo0);
		final Optional<System> systemOptional = Optional.of(system);
		when(systemRepository.findById(eq(testId0))).thenReturn(systemOptional);
		when(systemRepository.saveAndFlush(eq(system))).thenReturn(system);
		
		boolean isInvalidIdThrowInvalidParameterException = false;
		try {
			serviceRegistryDBService.updateSystem(testId0, systemName0, address0, port0, authenticationInfo0);
		} catch (final IllegalArgumentException ex) {
			isInvalidIdThrowInvalidParameterException = true;
		}
		
		assertTrue(isInvalidIdThrowInvalidParameterException);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void updateSystemByIdBelowValidPortTest() {
		final String systemName0 = "testSystemName0";
		final String address0 = "testAddress0";
		final int port0 = CommonConstants.SYSTEM_PORT_RANGE_MIN - 1;
		final long testId0 = 1;
		final String authenticationInfo0 = null;
		
		final System system = new System(systemName0, address0, port0, authenticationInfo0);
		final Optional<System> systemOptional = Optional.of(system);
		when(systemRepository.findById(eq(testId0))).thenReturn(systemOptional);
		when(systemRepository.saveAndFlush(eq(system))).thenReturn(system);
		
		boolean isInvalidPortThrowInvalidParameterException = false;
		try {
			serviceRegistryDBService.updateSystem(testId0, systemName0, address0, port0, authenticationInfo0);
		} catch (final InvalidParameterException ex) {
			isInvalidPortThrowInvalidParameterException = true;
		}
		
		assertTrue(isInvalidPortThrowInvalidParameterException);		
	}	
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void updateSystemByIdAboveValidPortTest() {
		final String systemName0 = "testSystemName0";
		final String address0 = "testAddress0";
		final int port0 = CommonConstants.SYSTEM_PORT_RANGE_MAX + 1;
		final long testId0 = 1;
		final String authenticationInfo0 = null;
		
		final System system = new System(systemName0, address0, port0, authenticationInfo0);
		final Optional<System> systemOptional = Optional.of(system);
		when(systemRepository.findById(eq(testId0))).thenReturn(systemOptional);
		when(systemRepository.saveAndFlush(eq(system))).thenReturn(system);
		
		boolean isInvalidPortThrowInvalidParameterException = false;
		try {
			serviceRegistryDBService.updateSystem(testId0, systemName0, address0, port0, authenticationInfo0);
		} catch (final InvalidParameterException ex) {
			isInvalidPortThrowInvalidParameterException = true;
		}
		
		assertTrue(isInvalidPortThrowInvalidParameterException);		
	}	
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void updateSystemByNullSystemNameTest() {
		final String systemName0 = null;
		final String address0 = "testAddress0";
		final int port0 = CommonConstants.SYSTEM_PORT_RANGE_MAX - 1;
		final long testId0 = 1;
		final String authenticationInfo0 = null;
		
		final System system = new System(systemName0, address0, port0, authenticationInfo0);
		final Optional<System> systemOptional = Optional.of(system);
		when(systemRepository.findById(eq(testId0))).thenReturn(systemOptional);
		when(systemRepository.saveAndFlush(eq(system))).thenReturn(system);
		
		boolean isNullNameThrowInvalidParameterException = false;
		try {
			serviceRegistryDBService.updateSystem(testId0, systemName0, address0, port0, authenticationInfo0);
		} catch (final InvalidParameterException ex) {
			isNullNameThrowInvalidParameterException = true;
		}
		
		assertTrue(isNullNameThrowInvalidParameterException);		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void updateSystemByEmptySystemNameTest() {
		final String systemName0 = "         ";
		final String address0 = "testAddress0";
		final int port0 = 1;
		final long testId0 = 1;
		final String authenticationInfo0 = null;
		
		final System system = new System(systemName0, address0, port0, authenticationInfo0);
		final Optional<System> systemOptional = Optional.of(system);
		when(systemRepository.findById(eq(testId0))).thenReturn(systemOptional);
		when(systemRepository.saveAndFlush(eq(system))).thenReturn(system);
		
		boolean isEmptyNameThrowInvalidParameterException = false;
		try {
			serviceRegistryDBService.updateSystem(testId0, systemName0, address0, port0, authenticationInfo0);
		} catch (final InvalidParameterException ex) {
			isEmptyNameThrowInvalidParameterException = true;
		}
		
		assertTrue(isEmptyNameThrowInvalidParameterException);		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void updateSystemByNullAddressTest() {
		final String systemName0 = "testSystem0";
		final String address0 = null;
		final int port0 = CommonConstants.SYSTEM_PORT_RANGE_MAX - 1;
		final long testId0 = 1;
		final String authenticationInfo0 = null;
		
		final System system = new System(systemName0, address0, port0, authenticationInfo0);
		final Optional<System> systemOptional = Optional.of(system);
		when(systemRepository.findById(eq(testId0))).thenReturn(systemOptional);
		when(systemRepository.saveAndFlush(eq(system))).thenReturn(system);
		
		boolean isNullAddressThrowInvalidParameterException = false;
		try {
			serviceRegistryDBService.updateSystem(testId0, systemName0, address0, port0, authenticationInfo0);
		} catch (final InvalidParameterException ex) {
			isNullAddressThrowInvalidParameterException = true;
		}
		
		assertTrue(isNullAddressThrowInvalidParameterException);		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void updateSystemByEmptyAddressTest() {
		final String systemName0 = "testSystem0";
		final String address0 = "              ";
		final int port0 = 1;
		final long testId0 = 1;
		final String authenticationInfo0 = null;
		
		final System system = new System(systemName0, address0, port0, authenticationInfo0);
		final Optional<System> systemOptional = Optional.of(system);
		when(systemRepository.findById(eq(testId0))).thenReturn(systemOptional);
		when(systemRepository.saveAndFlush(eq(system))).thenReturn(system);
		
		boolean isEmptyAddressThrowInvalidParameterException = false;
		try {
			serviceRegistryDBService.updateSystem(testId0, systemName0, address0, port0, authenticationInfo0);
		} catch (final InvalidParameterException ex) {
			isEmptyAddressThrowInvalidParameterException = true;
		}
		
		assertTrue(isEmptyAddressThrowInvalidParameterException);		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void mergeSystemByIdInvalidIdTest() {
		final String systemName0 = "testSystemName0";
		final String address0 = "testAddress0";
		final int port0 = 1;
		final long testId0 = 0;
		final String authenticationInfo0 = null;
		
		final System system = new System(systemName0, address0, port0, authenticationInfo0);
		final Optional<System> systemOptional = Optional.of(system);
		when(systemRepository.findById(eq(testId0))).thenReturn(systemOptional);
		when(systemRepository.saveAndFlush(eq(system))).thenReturn(system);
		
		boolean isInvalidIdThrowInvalidParameterException = false;
		try {
			serviceRegistryDBService.mergeSystem(testId0, systemName0, address0, port0, authenticationInfo0);
		} catch (final IllegalArgumentException ex) {
			isInvalidIdThrowInvalidParameterException = true;
		}
		
		assertTrue(isInvalidIdThrowInvalidParameterException);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void mergeSystemByIdBelowValidPortTest() {
		final String systemName0 = "testSystemName0";
		final String address0 = "testAddress0";
		final int port0 = CommonConstants.SYSTEM_PORT_RANGE_MIN - 1;
		final long testId0 = 1;
		final String authenticationInfo0 = null;
		
		final System system = new System(systemName0, address0, port0, authenticationInfo0);
		final Optional<System> systemOptional = Optional.of(system);
		when(systemRepository.findById(eq(testId0))).thenReturn(systemOptional);
		when(systemRepository.saveAndFlush(eq(system))).thenReturn(system);
		
		boolean isInvalidIdThrowInvalidParameterException = false;
		try {
			serviceRegistryDBService.mergeSystem(testId0, systemName0, address0, port0, authenticationInfo0);
		} catch (final IllegalArgumentException ex) {
			isInvalidIdThrowInvalidParameterException = true;
		}
		
		assertTrue(isInvalidIdThrowInvalidParameterException);		
	}	
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void mergeSystemByIdAboveValidPortTest() {
		final String systemName0 = "testSystemName0";
		final String address0 = "testAddress0";
		final int port0 = CommonConstants.SYSTEM_PORT_RANGE_MAX + 1;
		final long testId0 = 1;
		final String authenticationInfo0 = null;
		
		final System system = new System(systemName0, address0, port0, authenticationInfo0);
		final Optional<System> systemOptional = Optional.of(system);
		when(systemRepository.findById(eq(testId0))).thenReturn(systemOptional);
		when(systemRepository.saveAndFlush(eq(system))).thenReturn(system);
		
		boolean isInvalidIdThrowInvalidParameterException = false;
		try {
			serviceRegistryDBService.mergeSystem(testId0, systemName0, address0, port0, authenticationInfo0);
		} catch (final IllegalArgumentException ex) {
			isInvalidIdThrowInvalidParameterException = true;
		}
		
		assertTrue(isInvalidIdThrowInvalidParameterException);		
	}	
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void mergeSystemByINullPortTest() {
		final String systemName0 = "testSystemName0";
		final String address0 = "testAddress0";
		final Integer port0 = null;
		final long testId0 = 1;
		final String authenticationInfo0 = null;
		
		final System system = new System(systemName0, address0, 1, authenticationInfo0);
		final Optional<System> systemOptional = Optional.of(system);
		when(systemRepository.findById(eq(testId0))).thenReturn(systemOptional);
		when(systemRepository.saveAndFlush(eq(system))).thenReturn(system);
		
		boolean isInvalidIdThrowInvalidParameterException = true;
		try {
			serviceRegistryDBService.mergeSystem(testId0, systemName0, address0, port0, authenticationInfo0);
		} catch (final IllegalArgumentException ex) {
			isInvalidIdThrowInvalidParameterException = false;
		}
		
		assertTrue(isInvalidIdThrowInvalidParameterException);		
	}	
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void mergeSystemByNullSystemNameTest() {
		final String systemName0 = null;
		final String address0 = "testAddress0";
		final int port0 = CommonConstants.SYSTEM_PORT_RANGE_MAX - 1;
		final long testId0 = 1;
		final String authenticationInfo0 = null;
		
		final System system = new System(systemName0, address0, port0, authenticationInfo0);
		final Optional<System> systemOptional = Optional.of(system);
		when(systemRepository.findById(eq(testId0))).thenReturn(systemOptional);
		when(systemRepository.saveAndFlush(eq(system))).thenReturn(system);
		
		boolean isNullNameThrowInvalidParameterException = true;
		try {
			serviceRegistryDBService.mergeSystem(testId0, systemName0, address0, port0, authenticationInfo0);
		} catch (final InvalidParameterException ex) {
			isNullNameThrowInvalidParameterException = false;
		}
		
		assertTrue(isNullNameThrowInvalidParameterException);		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void mergeSystemByEmptySystemNameTest() {
		final String systemName0 = "         ";
		final String address0 = "testAddress0";
		final int port0 = 1;
		final long testId0 = 1;
		final String authenticationInfo0 = null;
		
		final System system = new System(systemName0, address0, port0, authenticationInfo0);
		final Optional<System> systemOptional = Optional.of(system);
		when(systemRepository.findById(eq(testId0))).thenReturn(systemOptional);
		when(systemRepository.saveAndFlush(eq(system))).thenReturn(system);
		
		boolean isEmptyNameThrowInvalidParameterException = true;
		try {
			serviceRegistryDBService.mergeSystem(testId0, systemName0, address0, port0, authenticationInfo0);
		} catch (final InvalidParameterException ex) {
			isEmptyNameThrowInvalidParameterException = false;
		}
		
		assertTrue(isEmptyNameThrowInvalidParameterException);		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void mergeSystemByNullAddressTest() {
		final String systemName0 = "testSystem0";
		final String address0 = null;
		final int port0 = CommonConstants.SYSTEM_PORT_RANGE_MAX - 1;
		final long testId0 = 1;
		final String authenticationInfo0 = null;
		
		final System system = new System(systemName0, address0, port0, authenticationInfo0);
		final Optional<System> systemOptional = Optional.of(system);
		when(systemRepository.findById(eq(testId0))).thenReturn(systemOptional);
		when(systemRepository.saveAndFlush(eq(system))).thenReturn(system);
		
		boolean isNullAddressThrowInvalidParameterException = true;
		try {
			serviceRegistryDBService.mergeSystem(testId0, systemName0, address0, port0, authenticationInfo0);
		} catch (final InvalidParameterException ex) {
			isNullAddressThrowInvalidParameterException = false;
		}
		
		assertTrue(isNullAddressThrowInvalidParameterException);		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void mergeSystemByEmptyAddressTest() {
		final String systemName0 = "testSystem0";
		final String address0 = "              ";
		final int port0 = 1;
		final long testId0 = 1;
		final String authenticationInfo0 = null;
		
		final System system = new System(systemName0, address0, port0, authenticationInfo0);
		final Optional<System> systemOptional = Optional.of(system);
		when(systemRepository.findById(eq(testId0))).thenReturn(systemOptional);
		when(systemRepository.saveAndFlush(eq(system))).thenReturn(system);
		
		boolean isEmptyAddressThrowInvalidParameterException = true;
		try {
			serviceRegistryDBService.mergeSystem(testId0, systemName0, address0, port0, authenticationInfo0);
		} catch (final InvalidParameterException ex) {
			isEmptyAddressThrowInvalidParameterException = false;
		}
		
		assertTrue(isEmptyAddressThrowInvalidParameterException);		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void mergeSystemBySingleParameterNameTest() {
		final String systemName0 = "testSystemName0";
		final String address0 = "testAddress0";
		final int port0 = 1;
		final long testId0 = 1;
		final String authenticationInfo0 = null;
		
		final System system = new System(systemName0, address0, port0, authenticationInfo0);
		final Optional<System> systemOptional = Optional.of(system);
		when(systemRepository.findById(eq(testId0))).thenReturn(systemOptional);
		when(systemRepository.saveAndFlush(eq(system))).thenReturn(system);
		
		boolean isInvalidIdThrowInvalidParameterException = true;
		try {
			serviceRegistryDBService.mergeSystem(testId0, "mergeTestName", null, null, null);
		} catch (final IllegalArgumentException ex) {
			isInvalidIdThrowInvalidParameterException = false;
		}
		
		assertTrue(isInvalidIdThrowInvalidParameterException);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void mergeSystemBySingleParameterAddressTest() {
		final String systemName0 = "testSystemName0";
		final String address0 = "testAddress0";
		final int port0 = 1;
		final long testId0 = 1;
		final String authenticationInfo0 = null;
		
		final System system = new System(systemName0, address0, port0, authenticationInfo0);
		final Optional<System> systemOptional = Optional.of(system);
		when(systemRepository.findById(eq(testId0))).thenReturn(systemOptional);
		when(systemRepository.saveAndFlush(eq(system))).thenReturn(system);
		
		boolean isInvalidIdThrowInvalidParameterException = true;
		try {
			serviceRegistryDBService.mergeSystem(testId0, null, "mergeTestAddress", null, null);
		} catch (final IllegalArgumentException ex) {
			isInvalidIdThrowInvalidParameterException = false;
		}
		
		assertTrue(isInvalidIdThrowInvalidParameterException);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void mergeSystemBySingleParameterPortTest() {
		final String systemName0 = "testSystemName0";
		final String address0 = "testAddress0";
		final int port0 = 1;
		final long testId0 = 1;
		final String authenticationInfo0 = null;
		
		final System system = new System(systemName0, address0, port0, authenticationInfo0);
		final Optional<System> systemOptional = Optional.of(system);
		when(systemRepository.findById(eq(testId0))).thenReturn(systemOptional);
		when(systemRepository.saveAndFlush(eq(system))).thenReturn(system);
		
		boolean isInvalidIdThrowInvalidParameterException = true;
		try {
			serviceRegistryDBService.mergeSystem(testId0, null, null, 1, null);
		} catch (final IllegalArgumentException ex) {
			isInvalidIdThrowInvalidParameterException = false;
		}
		
		assertTrue(isInvalidIdThrowInvalidParameterException);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void mergeSystemBySingleParameterAuthInfoTest() {
		final String systemName0 = "testSystemName0";
		final String address0 = "testAddress0";
		final int port0 = 1;
		final long testId0 = 1;
		final String authenticationInfo0 = null;
		
		final System system = new System(systemName0, address0, port0, authenticationInfo0);
		final Optional<System> systemOptional = Optional.of(system);
		when(systemRepository.findById(eq(testId0))).thenReturn(systemOptional);
		when(systemRepository.saveAndFlush(eq(system))).thenReturn(system);
		
		boolean isInvalidIdThrowInvalidParameterException = true;
		try {
			serviceRegistryDBService.mergeSystem(testId0, null, null, null, "testAuthenticationInfo");
		} catch (final IllegalArgumentException ex) {
			isInvalidIdThrowInvalidParameterException = false;
		}
		
		assertTrue(isInvalidIdThrowInvalidParameterException);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void mergeSystemByAllNullParametersTest() {
		final String systemName0 = "testSystemName0";
		final String address0 = "testAddress0";
		final int port0 = 1;
		final long testId0 = 1;
		final String authenticationInfo0 = null;
		
		final System system = new System(systemName0, address0, port0, authenticationInfo0);
		final Optional<System> systemOptional = Optional.of(system);
		when(systemRepository.findById(eq(testId0))).thenReturn(systemOptional);
		when(systemRepository.saveAndFlush(eq(system))).thenReturn(system);
		
		boolean isInvalidIdThrowInvalidParameterException = true;
		try {
			serviceRegistryDBService.mergeSystem(testId0, null, null, null, null);
		} catch (final IllegalArgumentException ex) {
			isInvalidIdThrowInvalidParameterException = false;
		}
		
		assertTrue(isInvalidIdThrowInvalidParameterException);
	}
	
	//-------------------------------------------------------------------------------------------------	
	@Test (expected = InvalidParameterException.class)
	public void removeServiceByIdTest() {
		when(systemRepository.existsById(anyLong())).thenReturn(false);
		serviceRegistryDBService.removeSystemById(0);
	}
}