/********************************************************************************
 * Copyright (c) 2019 AITIA
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   AITIA - implementation
 *   Arrowhead Consortia - conceptualization
 ********************************************************************************/

package eu.arrowhead.core.serviceregistry.database.service;

import static org.junit.Assert.fail;
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
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.database.entity.System;
import eu.arrowhead.common.database.repository.SystemRepository;
import eu.arrowhead.common.exception.InvalidParameterException;

@RunWith (SpringRunner.class)
public class ServiceRegistryDBServiceSystemTest {
	
	//=================================================================================================
	// members
	
	@InjectMocks
	private ServiceRegistryDBService serviceRegistryDBService; 
	
	@Mock
	private SystemRepository systemRepository;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void getSystemByIdTest() {
		when(systemRepository.findById(anyLong())).thenReturn(Optional.ofNullable(null));
		
		serviceRegistryDBService.getSystemById(1);		
	}	
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void getSystemEntriesInvalidSortFieldValueTest() {
		final int validatedPage = 1;
		final int validatedSize = 1;
		final Direction validatedDirection = Direction.ASC;
		final String invalidSortField = "invalidSortField";
		
		serviceRegistryDBService.getSystemEntries(validatedPage, validatedSize, validatedDirection, invalidSortField);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getSystemEntriesNullSortFieldValueTest() {
		final int validatedPage = 1;
		final int validatedSize = 1;
		final Direction validatedDirection = Direction.ASC;
		final String validatedSortField = CoreCommonConstants.COMMON_FIELD_NAME_ID;
		
		final String nullSortField = null;
		
		final String systemName0 = "testSystemName0";
		final String address0 = "testAddress0";
		final int port0 = 0;
		final String authenticationInfo0 = null;
		
		final System system = new System(systemName0, address0, port0, authenticationInfo0);
		
		final List<System> systemsList = new ArrayList<>();
		systemsList.add(system);
		final Page<System> systems  = new PageImpl<System>(systemsList);
		
		when(systemRepository.findAll(PageRequest.of(validatedPage, validatedSize, validatedDirection, validatedSortField))).thenReturn(systems);
		
		try {
			serviceRegistryDBService.getSystemEntries(validatedPage, validatedSize, validatedDirection, nullSortField);
		} catch (final InvalidParameterException ex) {
			fail();
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void getSystemEntriesEmptySortFieldValueTest() {
		final int validatedPage = 1;
		final int validatedSize = 1;
		final Direction validatedDirection = Direction.ASC;
		final String validatedSortField = CoreCommonConstants.COMMON_FIELD_NAME_ID;
		
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
		
		try {
			serviceRegistryDBService.getSystemEntries(validatedPage, validatedSize, validatedDirection, invalidSortField);
		} catch (final InvalidParameterException ex) {
			fail();
		}
	
	}
		
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void createSystemSystemNameNullTest() {
		serviceRegistryDBService.createSystem(null, "x", 1, "x");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void createSystemAddressNullTest() {
		serviceRegistryDBService.createSystem("x", null, 1, "x");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void createSystemSystemNameEmptyStringTest() {
		serviceRegistryDBService.createSystem("", "x", 1, "x");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void createSystemSystemNameWithDotTest() {
		serviceRegistryDBService.createSystem("x.y", "x", 1, "x");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void createSystemAddressEmptyStringTest() {
		serviceRegistryDBService.createSystem("x", "", 1, "x");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void uniqueKeyViolationTest() {
		final String systemName = "alreadyexiststest";
		final String address = "alreadyexiststest";
		final int port = 1;
		final Optional<System> system = Optional.of(new System(systemName, address, port, null));
		
		when(systemRepository.findBySystemNameAndAddressAndPort(eq(systemName), eq(address), eq(port))).thenReturn(system);
		
		serviceRegistryDBService.createSystem(systemName, address, port, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void uniqueKeyViolationNoEffectOfWhiteSpaceTest() {
		final String systemName = "alreadyexiststest";
		final String address = "alreadyexiststest";
		final int port = 1;
		final Optional<System> system = Optional.of(new System(systemName, address, port, null));
		
		when(systemRepository.findBySystemNameAndAddressAndPort(eq(systemName), eq(address), eq(port))).thenReturn(system);
		
		serviceRegistryDBService.createSystem(" "+systemName+" ", " "+address+" ", port, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void uniqueKeyViolationNoEffectOfCaseDifferenceTest() {
		final String systemName = "alreadyexiststest";
		final String address = "alreadyexiststest";
		final int port = 1;
		final Optional<System> system = Optional.of(new System(systemName, address, port, null));

		when(systemRepository.findBySystemNameAndAddressAndPort(eq(systemName), eq(address), eq(port))).thenReturn(system);
		
		serviceRegistryDBService.createSystem(systemName.toUpperCase(), address.toUpperCase(), port, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void updateSystemByIdInvalidIdTest() {
		final String systemName0 = "testSystemName0";
		final String address0 = "testAddress0";
		final int port0 = 0;
		final long testId0 = 0;
		final String authenticationInfo0 = null;
		
		serviceRegistryDBService.updateSystem(testId0, systemName0, address0, port0, authenticationInfo0);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void updateSystemByIdBelowValidPortTest() {
		final String systemName0 = "testSystemName0";
		final String address0 = "testAddress0";
		final int port0 = CommonConstants.SYSTEM_PORT_RANGE_MIN - 1;
		final long testId0 = 1;
		final String authenticationInfo0 = null;
		
		serviceRegistryDBService.updateSystem(testId0, systemName0, address0, port0, authenticationInfo0);
	}	
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void updateSystemByIdAboveValidPortTest() {
		final String systemName0 = "testSystemName0";
		final String address0 = "testAddress0";
		final int port0 = CommonConstants.SYSTEM_PORT_RANGE_MAX + 1;
		final long testId0 = 1;
		final String authenticationInfo0 = null;
		
		serviceRegistryDBService.updateSystem(testId0, systemName0, address0, port0, authenticationInfo0);
	}	
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void updateSystemByIdNullSystemNameTest() {
		final String systemName0 = null;
		final String address0 = "testAddress0";
		final int port0 = CommonConstants.SYSTEM_PORT_RANGE_MAX - 1;
		final long testId0 = 1;
		final String authenticationInfo0 = null;
		
		serviceRegistryDBService.updateSystem(testId0, systemName0, address0, port0, authenticationInfo0);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void updateSystemByIdEmptySystemNameTest() {
		final String systemName0 = "         ";
		final String address0 = "testAddress0";
		final int port0 = 1;
		final long testId0 = 1;
		final String authenticationInfo0 = null;
		
		serviceRegistryDBService.updateSystem(testId0, systemName0, address0, port0, authenticationInfo0);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void updateSystemByIdSystemNameWithDotTest() {
		final String systemName0 = "test.name";
		final String address0 = "testAddress0";
		final int port0 = 1;
		final long testId0 = 1;
		final String authenticationInfo0 = null;
		
		serviceRegistryDBService.updateSystem(testId0, systemName0, address0, port0, authenticationInfo0);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void updateSystemByIdNullAddressTest() {
		final String systemName0 = "testSystem0";
		final String address0 = null;
		final int port0 = CommonConstants.SYSTEM_PORT_RANGE_MAX - 1;
		final long testId0 = 1;
		final String authenticationInfo0 = null;
		
		serviceRegistryDBService.updateSystem(testId0, systemName0, address0, port0, authenticationInfo0);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void updateSystemByIdEmptyAddressTest() {
		final String systemName0 = "testSystem0";
		final String address0 = "              ";
		final int port0 = 1;
		final long testId0 = 1;
		final String authenticationInfo0 = null;
		
		serviceRegistryDBService.updateSystem(testId0, systemName0, address0, port0, authenticationInfo0);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void mergeSystemByIdInvalidIdTest() {
		final String systemName0 = "testSystemName0";
		final String address0 = "testAddress0";
		final int port0 = 1;
		final long testId0 = 0;
		final String authenticationInfo0 = null;
		
		serviceRegistryDBService.mergeSystem(testId0, systemName0, address0, port0, authenticationInfo0);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void mergeSystemByIdBelowValidPortTest() {
		final String systemName0 = "testSystemName0";
		final String address0 = "testAddress0";
		final int port0 = CommonConstants.SYSTEM_PORT_RANGE_MIN - 1;
		final long testId0 = 1;
		final String authenticationInfo0 = null;
		
		serviceRegistryDBService.mergeSystem(testId0, systemName0, address0, port0, authenticationInfo0);
	}	
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void mergeSystemByIdAboveValidPortTest() {
		final String systemName0 = "testSystemName0";
		final String address0 = "testAddress0";
		final int port0 = CommonConstants.SYSTEM_PORT_RANGE_MAX + 1;
		final long testId0 = 1;
		final String authenticationInfo0 = null;
		
		serviceRegistryDBService.mergeSystem(testId0, systemName0, address0, port0, authenticationInfo0);
	}	
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void mergeSystemByIdNullPortTest() {
		final String systemName0 = "testSystemName0";
		final String address0 = "testAddress0";
		final Integer port0 = null;
		final long testId0 = 1;
		final String authenticationInfo0 = null;
		final System system = new System(systemName0, address0, 1, authenticationInfo0);
		final Optional<System> systemOptional = Optional.of(system);

		when(systemRepository.findById(eq(testId0))).thenReturn(systemOptional);
		when(systemRepository.saveAndFlush(eq(system))).thenReturn(system);
		
		try {
			serviceRegistryDBService.mergeSystem(testId0, systemName0, address0, port0, authenticationInfo0);
		} catch (final IllegalArgumentException ex) {
			fail();
		}	
	}	
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void mergeSystemByIdNullSystemNameTest() {
		final String systemName0 = null;
		final String address0 = "testAddress0";
		final int port0 = CommonConstants.SYSTEM_PORT_RANGE_MAX - 1;
		final long testId0 = 1;
		final String authenticationInfo0 = null;
		final System system = new System(systemName0, address0, port0, authenticationInfo0);
		final Optional<System> systemOptional = Optional.of(system);

		when(systemRepository.findById(eq(testId0))).thenReturn(systemOptional);
		when(systemRepository.saveAndFlush(eq(system))).thenReturn(system);
		
		try {
			serviceRegistryDBService.mergeSystem(testId0, systemName0, address0, port0, authenticationInfo0);
		} catch (final InvalidParameterException ex) {
			fail();
		}	
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void mergeSystemByIdEmptySystemNameTest() {
		final String systemName0 = "         ";
		final String address0 = "testAddress0";
		final int port0 = 1;
		final long testId0 = 1;
		final String authenticationInfo0 = null;
		final System system = new System(systemName0, address0, port0, authenticationInfo0);
		final Optional<System> systemOptional = Optional.of(system);

		when(systemRepository.findById(eq(testId0))).thenReturn(systemOptional);
		when(systemRepository.saveAndFlush(eq(system))).thenReturn(system);
		
		try {
			serviceRegistryDBService.mergeSystem(testId0, systemName0, address0, port0, authenticationInfo0);
		} catch (final InvalidParameterException ex) {
			fail();
		}		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void mergeSystemByIdSystemNameWithDotTest() {
		final String systemName0 = "test.address";
		final String address0 = "testAddress0";
		final int port0 = 1;
		final long testId0 = 1;
		final String authenticationInfo0 = null;
		
		serviceRegistryDBService.mergeSystem(testId0, systemName0, address0, port0, authenticationInfo0);		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void mergeSystemByIdNullAddressTest() {
		final String systemName0 = "testSystem0";
		final String address0 = null;
		final int port0 = CommonConstants.SYSTEM_PORT_RANGE_MAX - 1;
		final long testId0 = 1;
		final String authenticationInfo0 = null;
		final System system = new System(systemName0, address0, port0, authenticationInfo0);
		final Optional<System> systemOptional = Optional.of(system);

		when(systemRepository.findById(eq(testId0))).thenReturn(systemOptional);
		when(systemRepository.saveAndFlush(eq(system))).thenReturn(system);
		
		try {
			serviceRegistryDBService.mergeSystem(testId0, systemName0, address0, port0, authenticationInfo0);
		} catch (final InvalidParameterException ex) {
			fail();
		}		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void mergeSystemByIdEmptyAddressTest() {
		final String systemName0 = "testSystem0";
		final String address0 = "              ";
		final int port0 = 1;
		final long testId0 = 1;
		final String authenticationInfo0 = null;
		final System system = new System(systemName0, address0, port0, authenticationInfo0);
		final Optional<System> systemOptional = Optional.of(system);

		when(systemRepository.findById(eq(testId0))).thenReturn(systemOptional);
		when(systemRepository.saveAndFlush(eq(system))).thenReturn(system);
		
		try {
			serviceRegistryDBService.mergeSystem(testId0, systemName0, address0, port0, authenticationInfo0);
		} catch (final InvalidParameterException ex) {
			fail();
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void mergeSystemByIdSingleParameterNameTest() {
		final String systemName0 = "testSystemName0";
		final String address0 = "testAddress0";
		final int port0 = 1;
		final long testId0 = 1;
		final String authenticationInfo0 = null;
		final System system = new System(systemName0, address0, port0, authenticationInfo0);
		final Optional<System> systemOptional = Optional.of(system);

		when(systemRepository.findById(eq(testId0))).thenReturn(systemOptional);
		when(systemRepository.saveAndFlush(eq(system))).thenReturn(system);
		
		try {
			serviceRegistryDBService.mergeSystem(testId0, "mergeTestName", null, null, null);
		} catch (final IllegalArgumentException ex) {
			fail();
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void mergeSystemByIdSingleParameterAddressTest() {
		final String systemName0 = "testSystemName0";
		final String address0 = "testAddress0";
		final int port0 = 1;
		final long testId0 = 1;
		final String authenticationInfo0 = null;
		final System system = new System(systemName0, address0, port0, authenticationInfo0);
		final Optional<System> systemOptional = Optional.of(system);

		when(systemRepository.findById(eq(testId0))).thenReturn(systemOptional);
		when(systemRepository.saveAndFlush(eq(system))).thenReturn(system);
		
		try {
			serviceRegistryDBService.mergeSystem(testId0, null, "mergeTestAddress", null, null);
		} catch (final IllegalArgumentException ex) {
			fail();
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void mergeSystemByIdSingleParameterPortTest() {
		final String systemName0 = "testSystemName0";
		final String address0 = "testAddress0";
		final int port0 = 1;
		final long testId0 = 1;
		final String authenticationInfo0 = null;
		final System system = new System(systemName0, address0, port0, authenticationInfo0);
		final Optional<System> systemOptional = Optional.of(system);

		when(systemRepository.findById(eq(testId0))).thenReturn(systemOptional);
		when(systemRepository.saveAndFlush(eq(system))).thenReturn(system);
		
		try {
			serviceRegistryDBService.mergeSystem(testId0, null, null, 1, null);
		} catch (final IllegalArgumentException ex) {
			fail();
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void mergeSystemByIdSingleParameterAuthInfoTest() {
		final String systemName0 = "testSystemName0";
		final String address0 = "testAddress0";
		final int port0 = 1;
		final long testId0 = 1;
		final String authenticationInfo0 = null;
		final System system = new System(systemName0, address0, port0, authenticationInfo0);
		final Optional<System> systemOptional = Optional.of(system);

		when(systemRepository.findById(eq(testId0))).thenReturn(systemOptional);
		when(systemRepository.saveAndFlush(eq(system))).thenReturn(system);
		
		try {
			serviceRegistryDBService.mergeSystem(testId0, null, null, null, "testAuthenticationInfo");
		} catch (final IllegalArgumentException ex) {
			fail();
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void mergeSystemByIdAllNullParametersTest() {
		final String systemName0 = "testSystemName0";
		final String address0 = "testAddress0";
		final int port0 = 1;
		final long testId0 = 1;
		final String authenticationInfo0 = null;
		final System system = new System(systemName0, address0, port0, authenticationInfo0);
		final Optional<System> systemOptional = Optional.of(system);

		when(systemRepository.findById(eq(testId0))).thenReturn(systemOptional);
		when(systemRepository.saveAndFlush(eq(system))).thenReturn(system);
		
		try {
			serviceRegistryDBService.mergeSystem(testId0, null, null, null, null);
		} catch (final IllegalArgumentException ex) {
			fail();
		}
	}
	
	//-------------------------------------------------------------------------------------------------	
	@Test (expected = InvalidParameterException.class)
	public void removeSystemByIdTest() {
		when(systemRepository.existsById(anyLong())).thenReturn(false);
		
		serviceRegistryDBService.removeSystemById(0);
	}
}