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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.hibernate.HibernateException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.database.entity.System;
import eu.arrowhead.common.database.repository.SystemRepository;
import eu.arrowhead.common.dto.shared.AddressType;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.common.processor.NetworkAddressPreProcessor;
import eu.arrowhead.common.processor.SpecialNetworkAddressTypeDetector;
import eu.arrowhead.common.verifier.CommonNamePartVerifier;
import eu.arrowhead.common.verifier.NetworkAddressVerifier;

@RunWith (SpringRunner.class)
public class ServiceRegistryDBServiceSystemTest {
	
	//=================================================================================================
	// members
	
	@InjectMocks
	private ServiceRegistryDBService serviceRegistryDBService; 
	
	@Mock
	private SystemRepository systemRepository;
	
	@Spy
	private CommonNamePartVerifier cnVerifier;
	
	@Spy
	private NetworkAddressPreProcessor networkAddressPreProcessor;
	
	@Spy
	private SpecialNetworkAddressTypeDetector networkAddressTypeDetector;
	
	@Spy
	private NetworkAddressVerifier networkAddressVerifier;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Before
	public void setup() {
		ReflectionTestUtils.setField(networkAddressVerifier, "cnVerifier", cnVerifier);
	}
	
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
		
		final System system = new System(systemName0, address0, AddressType.HOSTNAME, port0, authenticationInfo0, "systemkey=systemvalue");
		
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
		
		final System system = new System(systemName0, address0, AddressType.HOSTNAME, port0, authenticationInfo0, "systemkey=systemvalue");
		
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
		serviceRegistryDBService.createSystem(null, "x", 1, "x", Map.of("systemkey", "systemvalue"));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void createSystemAddressNullTest() {
		serviceRegistryDBService.createSystem("x", null, 1, "x", Map.of("systemkey", "systemvalue"));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void createSystemSystemNameEmptyStringTest() {
		serviceRegistryDBService.createSystem("", "x", 1, "x", Map.of("systemkey", "systemvalue"));
	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void createSystemSystemNameWrongTest() {
		try {
			serviceRegistryDBService.createSystem("invalid_system", "x", 1, "x", Map.of("systemkey", "systemvalue"));
		} catch (final InvalidParameterException ex) {
			Assert.assertEquals("System name has invalid format. Name must match with the following regular expression: " + CommonNamePartVerifier.COMMON_NAME_PART_PATTERN_STRING, ex.getMessage());			
			
			throw ex;
		}
	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void createSystemAddressEmptyStringAndPreProcessingAndVerifyTest() {
		try {
			serviceRegistryDBService.createSystem("xyz", "", 1, "x", Map.of("systemkey", "systemvalue"));			
		} catch (final InvalidParameterException ex) {
			verify(networkAddressPreProcessor, times(1)).normalize(eq(""));
			verify(networkAddressVerifier, times(1)).verify(eq(""));
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void uniqueKeyViolationTest() {
		final String systemName = "alreadyexiststest";
		final String address = "alreadyexiststest";
		final int port = 1;
		final Optional<System> system = Optional.of(new System(systemName, address, AddressType.HOSTNAME, port, null, "systemkey=systemvalue"));
		
		when(systemRepository.findBySystemNameAndAddressAndPort(eq(systemName), eq(address), eq(port))).thenReturn(system);
		
		try {
			serviceRegistryDBService.createSystem(systemName, address, port, null, Map.of("systemkey", "systemvalue"));
		} catch (final Exception ex) {
			verify(networkAddressTypeDetector, times(1)).detectAddressType("alreadyexiststest");
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void uniqueKeyViolationNoEffectOfWhiteSpaceTest() {
		final String systemName = "alreadyexiststest";
		final String address = "alreadyexiststest";
		final int port = 1;
		final Optional<System> system = Optional.of(new System(systemName, address, AddressType.HOSTNAME, port, null, "systemkey=systemvalue"));
		
		when(systemRepository.findBySystemNameAndAddressAndPort(eq(systemName), eq(address), eq(port))).thenReturn(system);
		
		try {
			serviceRegistryDBService.createSystem(" "+systemName+" ", " "+address+" ", port, null, Map.of("systemkey", "systemvalue"));
		} catch (final Exception ex) {
			verify(networkAddressTypeDetector, times(1)).detectAddressType("alreadyexiststest");
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void uniqueKeyViolationNoEffectOfCaseDifferenceTest() {
		final String systemName = "alreadyexiststest";
		final String address = "alreadyexiststest";
		final int port = 1;
		final Optional<System> system = Optional.of(new System(systemName, address, AddressType.HOSTNAME, port, null, "systemkey=systemvalue"));

		when(systemRepository.findBySystemNameAndAddressAndPort(eq(systemName), eq(address), eq(port))).thenReturn(system);
		
		try {
			serviceRegistryDBService.createSystem(systemName.toUpperCase(), address.toUpperCase(), port, null, Map.of("systemkey", "systemvalue"));
		} catch (final Exception ex) {
			verify(networkAddressTypeDetector, times(1)).detectAddressType("alreadyexiststest");
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void updateSystemByIdInvalidIdTest() {
		final String systemName0 = "testSystemName0";
		final String address0 = "testAddress0";
		final int port0 = 0;
		final long testId0 = 0;
		final String authenticationInfo0 = null;
		
		serviceRegistryDBService.updateSystem(testId0, systemName0, address0, port0, authenticationInfo0, Map.of("systemkey", "systemvalue"));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void updateSystemByIdBelowValidPortTest() {
		final String systemName0 = "testSystemName0";
		final String address0 = "testAddress0";
		final int port0 = CommonConstants.SYSTEM_PORT_RANGE_MIN - 1;
		final long testId0 = 1;
		final String authenticationInfo0 = null;
		
		serviceRegistryDBService.updateSystem(testId0, systemName0, address0, port0, authenticationInfo0, Map.of("systemkey", "systemvalue"));
	}	
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void updateSystemByIdAboveValidPortTest() {
		final String systemName0 = "testSystemName0";
		final String address0 = "testAddress0";
		final int port0 = CommonConstants.SYSTEM_PORT_RANGE_MAX + 1;
		final long testId0 = 1;
		final String authenticationInfo0 = null;
		
		serviceRegistryDBService.updateSystem(testId0, systemName0, address0, port0, authenticationInfo0, Map.of("systemkey", "systemvalue"));
	}	
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void updateSystemByIdNullSystemNameTest() {
		final String systemName0 = null;
		final String address0 = "testAddress0";
		final int port0 = CommonConstants.SYSTEM_PORT_RANGE_MAX - 1;
		final long testId0 = 1;
		final String authenticationInfo0 = null;
		
		serviceRegistryDBService.updateSystem(testId0, systemName0, address0, port0, authenticationInfo0, Map.of("systemkey", "systemvalue"));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void updateSystemByIdEmptySystemNameTest() {
		final String systemName0 = "         ";
		final String address0 = "testAddress0";
		final int port0 = 1;
		final long testId0 = 1;
		final String authenticationInfo0 = null;
		
		serviceRegistryDBService.updateSystem(testId0, systemName0, address0, port0, authenticationInfo0, Map.of("systemkey", "systemvalue"));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void updateSystemByIdInvalidSystemNameTest() {
		final String systemName0 = "test_name";
		final String address0 = "testAddress0";
		final int port0 = 1;
		final long testId0 = 1;
		final String authenticationInfo0 = null;
		
		try {
			serviceRegistryDBService.updateSystem(testId0, systemName0, address0, port0, authenticationInfo0, Map.of("systemkey", "systemvalue"));
		} catch (final InvalidParameterException ex) {
			Assert.assertEquals("System name has invalid format. Name must match with the following regular expression: " + CommonNamePartVerifier.COMMON_NAME_PART_PATTERN_STRING, ex.getMessage());			
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void updateSystemByIdNullAddressTest() {
		final String systemName0 = "testSystem0";
		final String address0 = null;
		final int port0 = CommonConstants.SYSTEM_PORT_RANGE_MAX - 1;
		final long testId0 = 1;
		final String authenticationInfo0 = null;
		
		serviceRegistryDBService.updateSystem(testId0, systemName0, address0, port0, authenticationInfo0, Map.of("systemkey", "systemvalue"));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void updateSystemByIdEmptyAddressWithPreProcessingAndVerifyTest() {
		final String systemName0 = "testSystem0";
		final String address0 = "              ";
		final int port0 = 1;
		final long testId0 = 1;
		final String authenticationInfo0 = null;
		
		try {
			serviceRegistryDBService.updateSystem(testId0, systemName0, address0, port0, authenticationInfo0, Map.of("systemkey", "systemvalue"));			
		} catch (final InvalidParameterException ex) {
			verify(networkAddressPreProcessor, times(1)).normalize("              ");
			verify(networkAddressVerifier, times(1)).verify("");
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void updateSystemByIdUniqueConstraintViolationTest() {
		
		final String systemName0 = "testSystem0";
		final String address0 = "abc";
		final int port0 = 1;
		final long testId0 = 1;
		final String authenticationInfo0 = null;

		final System oldSystem = new System(systemName0, address0, null, 1234, authenticationInfo0, null);
		
		when(systemRepository.findById(1L)).thenReturn(Optional.of(oldSystem));
		when(systemRepository.findBySystemNameAndAddressAndPort("testsystem0", "abc", 1)).thenReturn(Optional.of(new System()));
		
		try {
			serviceRegistryDBService.updateSystem(testId0, systemName0, address0, port0, authenticationInfo0, Map.of("systemkey", "systemvalue"));			
		} catch (final InvalidParameterException ex) {
			verify(networkAddressPreProcessor, times(1)).normalize("abc");
			verify(networkAddressVerifier, times(1)).verify("abc");
			verify(networkAddressTypeDetector, times(1)).detectAddressType("abc");
			verify(systemRepository, times(1)).findById(1L);
			verify(systemRepository, times(1)).findBySystemNameAndAddressAndPort("testsystem0", "abc", 1);
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void mergeSystemByIdInvalidIdTest() {
		final String systemName0 = "testSystemName0";
		final String address0 = "testAddress0";
		final int port0 = 1;
		final long testId0 = 0;
		final String authenticationInfo0 = null;
		
		serviceRegistryDBService.mergeSystem(testId0, systemName0, address0, port0, authenticationInfo0, Map.of("systemkey", "systemvalue"));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void mergeSystemByIdBelowValidPortTest() {
		final String systemName0 = "testSystemName0";
		final String address0 = "testAddress0";
		final int port0 = CommonConstants.SYSTEM_PORT_RANGE_MIN - 1;
		final long testId0 = 1;
		final String authenticationInfo0 = null;
		
		serviceRegistryDBService.mergeSystem(testId0, systemName0, address0, port0, authenticationInfo0, Map.of("systemkey", "systemvalue"));
	}	
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void mergeSystemByIdAboveValidPortTest() {
		final String systemName0 = "testSystemName0";
		final String address0 = "testAddress0";
		final int port0 = CommonConstants.SYSTEM_PORT_RANGE_MAX + 1;
		final long testId0 = 1;
		final String authenticationInfo0 = null;
		
		serviceRegistryDBService.mergeSystem(testId0, systemName0, address0, port0, authenticationInfo0, Map.of("systemkey", "systemvalue"));
	}	
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void mergeSystemByIdNullPortTest() {
		final String systemName0 = "testSystemName0";
		final String address0 = "testAddress0";
		final Integer port0 = null;
		final long testId0 = 1;
		final String authenticationInfo0 = null;
		final System system = new System(systemName0, address0, AddressType.HOSTNAME, 1, authenticationInfo0, "systemkey=systemvalue");
		final Optional<System> systemOptional = Optional.of(system);

		when(systemRepository.findById(eq(testId0))).thenReturn(systemOptional);
		when(systemRepository.saveAndFlush(eq(system))).thenReturn(system);
		
		try {
			serviceRegistryDBService.mergeSystem(testId0, systemName0, address0, port0, authenticationInfo0, Map.of("systemkey", "systemvalue"));
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
		final System system = new System(systemName0, address0, AddressType.HOSTNAME, port0, authenticationInfo0, "systemkey=systemvalue");
		final Optional<System> systemOptional = Optional.of(system);

		when(systemRepository.findById(eq(testId0))).thenReturn(systemOptional);
		when(systemRepository.saveAndFlush(eq(system))).thenReturn(system);
		
		try {
			serviceRegistryDBService.mergeSystem(testId0, systemName0, address0, port0, authenticationInfo0, Map.of("systemkey", "systemvalue"));
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
		final System system = new System(systemName0, address0, AddressType.HOSTNAME, port0, authenticationInfo0, "systemkey=systemvalue");
		final Optional<System> systemOptional = Optional.of(system);

		when(systemRepository.findById(eq(testId0))).thenReturn(systemOptional);
		when(systemRepository.saveAndFlush(eq(system))).thenReturn(system);
		
		try {
			serviceRegistryDBService.mergeSystem(testId0, systemName0, address0, port0, authenticationInfo0, Map.of("systemkey", "systemvalue"));
		} catch (final InvalidParameterException ex) {
			fail();
		}		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void mergeSystemByIdWrongSystemNameTest() {
		final String systemName0 = "test.address";
		final String address0 = "testAddress0";
		final int port0 = 1;
		final long testId0 = 1;
		final String authenticationInfo0 = null;
		
		try {
			serviceRegistryDBService.mergeSystem(testId0, systemName0, address0, port0, authenticationInfo0, Map.of("systemkey", "systemvalue"));		
		} catch (final InvalidParameterException ex) {
			Assert.assertEquals("System name has invalid format. Name must match with the following regular expression: " + CommonNamePartVerifier.COMMON_NAME_PART_PATTERN_STRING, ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void mergeSystemByIdNullAddressTest() {
		final String systemName0 = "testSystem0";
		final String address0 = null;
		final int port0 = CommonConstants.SYSTEM_PORT_RANGE_MAX - 1;
		final long testId0 = 1;
		final String authenticationInfo0 = null;
		final System system = new System(systemName0, address0, null, port0, authenticationInfo0, "systemkey=systemvalue");
		final Optional<System> systemOptional = Optional.of(system);

		when(systemRepository.findById(eq(testId0))).thenReturn(systemOptional);
		when(systemRepository.saveAndFlush(eq(system))).thenReturn(system);
		
		try {
			serviceRegistryDBService.mergeSystem(testId0, systemName0, address0, port0, authenticationInfo0, Map.of("systemkey", "systemvalue"));
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
		final System system = new System(systemName0, address0, null, port0, authenticationInfo0, "systemkey=systemvalue");
		final Optional<System> systemOptional = Optional.of(system);

		when(systemRepository.findById(eq(testId0))).thenReturn(systemOptional);
		when(systemRepository.saveAndFlush(eq(system))).thenReturn(system);
		
		try {
			serviceRegistryDBService.mergeSystem(testId0, systemName0, address0, port0, authenticationInfo0, Map.of("systemkey", "systemvalue"));
		} catch (final InvalidParameterException ex) {
			fail();
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void mergeSystemByIdAddressPreProcessingAndVerifyTest() {
		final String systemName0 = "testSystem0";
		final String address0 = "address";
		final int port0 = 1;
		final long testId0 = 1;
		final String authenticationInfo0 = null;
		final System system = new System(systemName0, address0, AddressType.HOSTNAME, port0, authenticationInfo0, "systemkey=systemvalue");
		final Optional<System> systemOptional = Optional.of(system);

		when(systemRepository.findById(eq(testId0))).thenReturn(systemOptional);
		when(systemRepository.saveAndFlush(eq(system))).thenReturn(system);
		
		try {
			serviceRegistryDBService.mergeSystem(testId0, systemName0, address0, port0, authenticationInfo0, Map.of("systemkey", "systemvalue"));
		} catch (final InvalidParameterException ex) {
			fail();
		}
		
		verify(networkAddressPreProcessor, times(1)).normalize(eq("address"));
		verify(networkAddressVerifier, times(1)).verify(eq("address"));
		verify(networkAddressTypeDetector, times(1)).detectAddressType("address");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void mergeSystemByIdSingleParameterNameTest() {
		final String systemName0 = "testSystemName0";
		final String address0 = "testAddress0";
		final int port0 = 1;
		final long testId0 = 1;
		final String authenticationInfo0 = null;
		final System system = new System(systemName0, address0, AddressType.HOSTNAME, port0, authenticationInfo0, "systemkey=systemvalue");
		final Optional<System> systemOptional = Optional.of(system);

		when(systemRepository.findById(eq(testId0))).thenReturn(systemOptional);
		when(systemRepository.saveAndFlush(eq(system))).thenReturn(system);
		
		try {
			serviceRegistryDBService.mergeSystem(testId0, "mergeTestName", null, null, null, null);
		} catch (final IllegalArgumentException ex) {
			fail();
		}
		
		verify(networkAddressTypeDetector, never()).detectAddressType("address");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void mergeSystemByIdSingleParameterAddressTest() {
		final String systemName0 = "testSystemName0";
		final String address0 = "testAddress0";
		final int port0 = 1;
		final long testId0 = 1;
		final String authenticationInfo0 = null;
		final System system = new System(systemName0, address0, AddressType.HOSTNAME, port0, authenticationInfo0, "systemkey=systemvalue");
		final Optional<System> systemOptional = Optional.of(system);

		when(systemRepository.findById(eq(testId0))).thenReturn(systemOptional);
		when(systemRepository.saveAndFlush(eq(system))).thenReturn(system);
		
		try {
			serviceRegistryDBService.mergeSystem(testId0, null, "mergeTestAddress", null, null, null);
		} catch (final IllegalArgumentException ex) {
			fail();
		}
		
		verify(networkAddressTypeDetector, times(1)).detectAddressType("mergetestaddress");

	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void mergeSystemByIdSingleParameterPortTest() {
		final String systemName0 = "testSystemName0";
		final String address0 = "testAddress0";
		final int port0 = 1;
		final long testId0 = 1;
		final String authenticationInfo0 = null;
		final System system = new System(systemName0, address0, AddressType.HOSTNAME, port0, authenticationInfo0, "systemkey=systemvalue");
		final Optional<System> systemOptional = Optional.of(system);

		when(systemRepository.findById(eq(testId0))).thenReturn(systemOptional);
		when(systemRepository.saveAndFlush(eq(system))).thenReturn(system);
		
		try {
			serviceRegistryDBService.mergeSystem(testId0, null, null, 1, null, null);
		} catch (final IllegalArgumentException ex) {
			fail();
		}
		
		verify(networkAddressTypeDetector, never()).detectAddressType("address");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void mergeSystemByIdSingleParameterAuthInfoTest() {
		final String systemName0 = "testSystemName0";
		final String address0 = "testAddress0";
		final int port0 = 1;
		final long testId0 = 1;
		final String authenticationInfo0 = null;
		final System system = new System(systemName0, address0, AddressType.HOSTNAME, port0, authenticationInfo0, "systemkey=systemvalue");
		final Optional<System> systemOptional = Optional.of(system);

		when(systemRepository.findById(eq(testId0))).thenReturn(systemOptional);
		when(systemRepository.saveAndFlush(eq(system))).thenReturn(system);
		
		try {
			serviceRegistryDBService.mergeSystem(testId0, null, null, null, "testAuthenticationInfo", null);
		} catch (final IllegalArgumentException ex) {
			fail();
		}
		
		verify(networkAddressTypeDetector, never()).detectAddressType("address");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void mergeSystemByIdSingleParameterMetadataTest() {
		final String systemName0 = "testSystemName0";
		final String address0 = "testAddress0";
		final int port0 = 1;
		final long testId0 = 1;
		final String authenticationInfo0 = null;
		final System system = new System(systemName0, address0, AddressType.HOSTNAME, port0, authenticationInfo0, "systemkey=systemvalue");
		final Optional<System> systemOptional = Optional.of(system);

		when(systemRepository.findById(eq(testId0))).thenReturn(systemOptional);
		when(systemRepository.saveAndFlush(eq(system))).thenReturn(system);
		
		try {
			serviceRegistryDBService.mergeSystem(testId0, null, null, null, null, Map.of("systemkey", "systemvalue2"));
		} catch (final IllegalArgumentException ex) {
			fail();
		}
		
		verify(networkAddressTypeDetector, never()).detectAddressType("address");
	}

	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void mergeSystemByIdAllNullParametersTest() {
		final String systemName0 = "testSystemName0";
		final String address0 = "testAddress0";
		final int port0 = 1;
		final long testId0 = 1;
		final String authenticationInfo0 = null;
		final System system = new System(systemName0, address0, AddressType.HOSTNAME, port0, authenticationInfo0, "systemkey=systemvalue");
		final Optional<System> systemOptional = Optional.of(system);

		when(systemRepository.findById(eq(testId0))).thenReturn(systemOptional);
		when(systemRepository.saveAndFlush(eq(system))).thenReturn(system);
		
		try {
			serviceRegistryDBService.mergeSystem(testId0, null, null, null, null, null);
		} catch (final IllegalArgumentException ex) {
			fail();
		}
		
		verify(networkAddressTypeDetector, never()).detectAddressType("address");
	}
	
	//-------------------------------------------------------------------------------------------------	
	@Test (expected = InvalidParameterException.class)
	public void removeSystemByIdTest() {
		when(systemRepository.existsById(anyLong())).thenReturn(false);
		
		serviceRegistryDBService.removeSystemById(0);
	}
	
	//-------------------------------------------------------------------------------------------------	
	@Test
	public void removeSystemByNameAndAddressAndPortTestOk() {
		final long id = 5;
		final String name = "consumer" ;
		final String address = "address";
		final int port = 5550;
		final System entity = new System(name, address, AddressType.HOSTNAME, port, null, null);
		entity.setId(id);
		
		final ArgumentCaptor<String> strCaptor = ArgumentCaptor.forClass(String.class);
		final ArgumentCaptor<Integer> intCaptor = ArgumentCaptor.forClass(Integer.class);
		when(systemRepository.findBySystemNameAndAddressAndPort(strCaptor.capture(), strCaptor.capture(), intCaptor.capture())).thenReturn(Optional.of(entity));
		doNothing().when(systemRepository).deleteById(anyLong());
		doNothing().when(systemRepository).flush();
		
		serviceRegistryDBService.removeSystemByNameAndAddressAndPort(name, address, port);
		
		assertEquals(name, strCaptor.getAllValues().get(0));
		assertEquals(address, strCaptor.getAllValues().get(1));
		assertEquals(port, intCaptor.getValue().intValue());
		
		verify(systemRepository, times(1)).findBySystemNameAndAddressAndPort(eq(name), eq(address), eq(port));
		verify(systemRepository, times(1)).deleteById(eq(id));
		verify(systemRepository, times(1)).flush();
	}
	
	//-------------------------------------------------------------------------------------------------	
	@Test(expected = InvalidParameterException.class)
	public void removeSystemByNameAndAddressAndPortTestSystemNotExists() {
		final String name = "consumer" ;
		final String address = "address";
		final int port = 5550;
		
		final ArgumentCaptor<String> strCaptor = ArgumentCaptor.forClass(String.class);
		final ArgumentCaptor<Integer> intCaptor = ArgumentCaptor.forClass(Integer.class);
		when(systemRepository.findBySystemNameAndAddressAndPort(strCaptor.capture(), strCaptor.capture(), intCaptor.capture())).thenReturn(Optional.empty());
		doNothing().when(systemRepository).deleteById(anyLong());
		doNothing().when(systemRepository).flush();
		
		try {
			serviceRegistryDBService.removeSystemByNameAndAddressAndPort(name, address, port);			
		} catch (final InvalidParameterException ex) {
			assertEquals(name, strCaptor.getAllValues().get(0));
			assertEquals(address, strCaptor.getAllValues().get(1));
			assertEquals(port, intCaptor.getValue().intValue());
			
			verify(systemRepository, times(1)).findBySystemNameAndAddressAndPort(eq(name), eq(address), eq(port));
			verify(systemRepository, never()).deleteById(anyLong());
			verify(systemRepository, never()).flush();
			throw ex;
		}		
	}
	
	//-------------------------------------------------------------------------------------------------	
	@Test(expected = ArrowheadException.class)
	public void removeSystemByNameAndAddressAndPortTestDBException() {
		final String name = "consumer" ;
		final String address = "address";
		final int port = 5550;
		
		final ArgumentCaptor<String> strCaptor = ArgumentCaptor.forClass(String.class);
		final ArgumentCaptor<Integer> intCaptor = ArgumentCaptor.forClass(Integer.class);
		when(systemRepository.findBySystemNameAndAddressAndPort(strCaptor.capture(), strCaptor.capture(), intCaptor.capture())).thenThrow(new HibernateException("test"));
		
		try {
			serviceRegistryDBService.removeSystemByNameAndAddressAndPort(name, address, port);			
		} catch (final ArrowheadException ex) {
			assertEquals(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG, ex.getMessage());
			assertEquals(name, strCaptor.getAllValues().get(0));
			assertEquals(address, strCaptor.getAllValues().get(1));
			assertEquals(port, intCaptor.getValue().intValue());
			
			verify(systemRepository, times(1)).findBySystemNameAndAddressAndPort(eq(name), eq(address), eq(port));
			verify(systemRepository, never()).deleteById(anyLong());
			verify(systemRepository, never()).flush();
			throw ex;
		}		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCalculateSystemAddressTypeIfNecessaryOk() {
		final System system1 = new System();
		system1.setAddress("address");
		final System system2 = new System();
		system2.setAddress("address");
		
		when(systemRepository.findByAddressTypeIsNull()).thenReturn(List.of(system1, system2));
		when(systemRepository.saveAll(anyList())).thenReturn(List.of(system1, system2));
		doNothing().when(systemRepository).flush();
		
		serviceRegistryDBService.calculateSystemAddressTypeIfNecessary();
		
		verify(systemRepository, times(1)).findByAddressTypeIsNull();
		verify(networkAddressTypeDetector, times(2)).detectAddressType("address");
		verify(systemRepository, times(1)).saveAll(anyList());
		verify(systemRepository, times(1)).flush();
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testCalculateSystemAddressTypeIfNecessaryDbProblem() {
		when(systemRepository.findByAddressTypeIsNull()).thenThrow(new RuntimeException("test"));

		try {
			serviceRegistryDBService.calculateSystemAddressTypeIfNecessary();
		} catch (final Exception ex) {
			Assert.assertEquals("Database operation exception", ex.getMessage());
			
			verify(systemRepository, times(1)).findByAddressTypeIsNull();
			verify(networkAddressTypeDetector, never()).detectAddressType("address");
			
			throw ex;
		}
	}
}