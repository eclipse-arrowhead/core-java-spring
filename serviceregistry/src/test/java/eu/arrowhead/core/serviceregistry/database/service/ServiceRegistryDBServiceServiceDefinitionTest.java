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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import eu.arrowhead.common.database.entity.ServiceDefinition;
import eu.arrowhead.common.database.repository.ServiceDefinitionRepository;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.common.verifier.CommonNamePartVerifier;

@RunWith (SpringRunner.class)
public class ServiceRegistryDBServiceServiceDefinitionTest {
	
	//=================================================================================================
	// members
	
	@InjectMocks
	private ServiceRegistryDBService serviceRegistryDBService; 
		
	@Mock
	private ServiceDefinitionRepository serviceDefinitionRepository;
	
	@Mock
	private CommonNamePartVerifier cnVerifier;
	
	private CommonNamePartVerifier realVerifier = new CommonNamePartVerifier();
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Before
	public void setUp() {
		when(cnVerifier.isValid(any(String.class))).thenAnswer(new Answer<Boolean>() {
			@Override
			public Boolean answer(final InvocationOnMock invocation) throws Throwable {
				return realVerifier.isValid(invocation.getArgument(0));
			}
		});
	}
	
	//=================================================================================================
	// Tests of getServiceDefinitionById
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void getServiceDefinitionByIdTestWithNotExistingId() {
		when(serviceDefinitionRepository.findById(anyLong())).thenReturn(Optional.ofNullable(null));
		
		serviceRegistryDBService.getServiceDefinitionById(1);
	}
	
	//=================================================================================================
	//Tests of getServiceDefinitionEntries
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void getServiceDefinitionEntriesTestWithInvalidSortField() {
		serviceRegistryDBService.getServiceDefinitionEntries(0, 10, Direction.ASC, "notValid");
	}
	
	//=================================================================================================
	//Tests of createServiceDefinition
	
	@Test(expected = InvalidParameterException.class)
	public void createServiceDefinitionTestWithNullInput() {
		serviceRegistryDBService.createServiceDefinition(null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void createServiceDefinitionTestWithBlankStringInput() {
		serviceRegistryDBService.createServiceDefinition("       ");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void createServiceDefinitionTestWithWrongInputFlagTrue() {
		ReflectionTestUtils.setField(serviceRegistryDBService, "useStrictServiceDefinitionVerifier", true);
		try {
			serviceRegistryDBService.createServiceDefinition("invalid_service_definition");
		} catch (final InvalidParameterException ex) {
			Assert.assertEquals("Service definition has invalid format. Name must match with the following regular expression: " + CommonNamePartVerifier.COMMON_NAME_PART_PATTERN_STRING, ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void createServiceDefinitionTestWithWrongInputFlagFalse() {
		when(serviceDefinitionRepository.findByServiceDefinition(any(String.class))).thenReturn(Optional.empty());
		when(serviceDefinitionRepository.saveAndFlush(any(ServiceDefinition.class))).thenReturn(new ServiceDefinition("invalid_service_definition"));
		
		serviceRegistryDBService.createServiceDefinition("invalid_service_definition");
		
		verify(serviceDefinitionRepository, times(1)).findByServiceDefinition(any(String.class));
		verify(serviceDefinitionRepository, times(1)).saveAndFlush(any(ServiceDefinition.class));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void createServiceDefinitionTestOfUniqueKeyViolation() {
		final String testDefinition = "alreadyexiststest";
		final Optional<ServiceDefinition> serviceDefinitionEntry = Optional.of(new ServiceDefinition(testDefinition));
		
		when(serviceDefinitionRepository.findByServiceDefinition(eq(testDefinition))).thenReturn(serviceDefinitionEntry);
		
		serviceRegistryDBService.createServiceDefinition(testDefinition);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void createServiceDefinitionTestCaseInsensitivityOfUniqueKeyViolation() {
		final String testDefinition = "alreadyexiststest";
		final Optional<ServiceDefinition> serviceDefinitionEntry = Optional.of(new ServiceDefinition(testDefinition));
		
		when(serviceDefinitionRepository.findByServiceDefinition(eq(testDefinition))).thenReturn(serviceDefinitionEntry);
		
		serviceRegistryDBService.createServiceDefinition(testDefinition.toUpperCase());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void createServiceDefinitionTestLeadingTrailingSpaceSensitivityOfUniqueKeyViolation() {
		final String testDefinition = "alreadyexiststest";
		final Optional<ServiceDefinition> serviceDefinitionEntry = Optional.of(new ServiceDefinition(testDefinition));
		
		when(serviceDefinitionRepository.findByServiceDefinition(eq(testDefinition))).thenReturn(serviceDefinitionEntry);
		
		serviceRegistryDBService.createServiceDefinition("  " + testDefinition + "  ");
	}
	
	//=================================================================================================
	// Tests of updateServiceDefinitionById
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void updateServiceDefinitionByIdTestWithNullInput() {
		serviceRegistryDBService.updateServiceDefinitionById(1, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void updateServiceDefinitionByIdTestWithBlankStringInput() {
		serviceRegistryDBService.updateServiceDefinitionById(1, "   ");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void updateServiceDefinitionTestWithWrongInputFlagTrue() {
		ReflectionTestUtils.setField(serviceRegistryDBService, "useStrictServiceDefinitionVerifier", true);
		try {
			serviceRegistryDBService.updateServiceDefinitionById(1, "invalid_service_definition");
		} catch (final InvalidParameterException ex) {
			Assert.assertEquals("Service definition has invalid format. Name must match with the following regular expression: " + CommonNamePartVerifier.COMMON_NAME_PART_PATTERN_STRING, ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void updateServiceDefinitionTestWithWrongInputFlagFalse() {
		when(serviceDefinitionRepository.findById(1L)).thenReturn(Optional.of(new ServiceDefinition("other name")));
		when(serviceDefinitionRepository.findByServiceDefinition(any(String.class))).thenReturn(Optional.empty());
		when(serviceDefinitionRepository.saveAndFlush(any(ServiceDefinition.class))).thenReturn(new ServiceDefinition("invalid_service_definition"));
		
		serviceRegistryDBService.updateServiceDefinitionById(1, "invalid_service_definition");
		
		verify(serviceDefinitionRepository, times(1)).findById(1L);
		verify(serviceDefinitionRepository, times(1)).findByServiceDefinition(any(String.class));
		verify(serviceDefinitionRepository, times(1)).saveAndFlush(any(ServiceDefinition.class));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void updateServiceDefinitionByIdTestWithNotExistingId() {
		when(serviceDefinitionRepository.findById(anyLong())).thenReturn(Optional.ofNullable(null));
		
		serviceRegistryDBService.updateServiceDefinitionById(1, "test");;
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void updateServiceDefinitionByIdTestOfUniqueKeyViolation() {		
		final String testDefinition2 = "testdefinition2";
		final long testId2 = 2;
		final ServiceDefinition serviceDefinition2 = new ServiceDefinition(testDefinition2);
		serviceDefinition2.setId(testId2);
		final Optional<ServiceDefinition> serviceDefinitionEntry2 = Optional.of(serviceDefinition2);
		final String testDefinition1 = "testdefinition1";
		final long testId1 = 1;
		final ServiceDefinition serviceDefinition1 = new ServiceDefinition(testDefinition1);
		serviceDefinition1.setId(testId1);
		final Optional<ServiceDefinition> serviceDefinitionEntry1 = Optional.of(serviceDefinition1);

		when(serviceDefinitionRepository.findById(eq(testId2))).thenReturn(serviceDefinitionEntry2);
		when(serviceDefinitionRepository.findByServiceDefinition(eq(testDefinition1))).thenReturn(serviceDefinitionEntry1);
		
		serviceRegistryDBService.updateServiceDefinitionById(testId2, testDefinition1);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void updateServiceDefinitionByIdTestCaseInsensitivityOfUniqueKeyViolation() {
		final String testDefinition0 = "testdefinition0";
		final long testId0 = 0;
		final ServiceDefinition serviceDefinition0 = new ServiceDefinition(testDefinition0);
		serviceDefinition0.setId(testId0);
		final Optional<ServiceDefinition> serviceDefinitionEntry0 = Optional.of(serviceDefinition0);
		final String testDefinition1 = "testdefinition1";
		final long testId1 = 1;
		final ServiceDefinition serviceDefinition1 = new ServiceDefinition(testDefinition1);
		serviceDefinition1.setId(testId1);
		final Optional<ServiceDefinition> serviceDefinitionEntry1 = Optional.of(serviceDefinition1);

		when(serviceDefinitionRepository.findById(eq(testId0))).thenReturn(serviceDefinitionEntry0);
		when(serviceDefinitionRepository.findByServiceDefinition(eq(testDefinition0))).thenReturn(serviceDefinitionEntry0);
		when(serviceDefinitionRepository.findById(eq(testId1))).thenReturn(serviceDefinitionEntry1);
		when(serviceDefinitionRepository.findByServiceDefinition(eq(testDefinition1))).thenReturn(serviceDefinitionEntry1);
		
		serviceRegistryDBService.updateServiceDefinitionById(testId0, testDefinition1.toUpperCase());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void updateServiceDefinitionByIdTestLeadingTrailingSpaceSensitivityOfUniqueKeyViolation() {
		final String testDefinition0 = "testdefinition0";
		final long testId0 = 0;
		final ServiceDefinition serviceDefinition0 = new ServiceDefinition(testDefinition0);
		serviceDefinition0.setId(testId0);
		final Optional<ServiceDefinition> serviceDefinitionEntry0 = Optional.of(serviceDefinition0);
		final String testDefinition1 = "testdefinition1";
		final long testId1 = 1;
		final ServiceDefinition serviceDefinition1 = new ServiceDefinition(testDefinition1);
		serviceDefinition1.setId(testId1);
		final Optional<ServiceDefinition> serviceDefinitionEntry1 = Optional.of(serviceDefinition1);

		when(serviceDefinitionRepository.findById(eq(testId0))).thenReturn(serviceDefinitionEntry0);
		when(serviceDefinitionRepository.findByServiceDefinition(eq(testDefinition0))).thenReturn(serviceDefinitionEntry0);
		when(serviceDefinitionRepository.findById(eq(testId1))).thenReturn(serviceDefinitionEntry1);
		when(serviceDefinitionRepository.findByServiceDefinition(eq(testDefinition1))).thenReturn(serviceDefinitionEntry1);
		
		serviceRegistryDBService.updateServiceDefinitionById(testId0, "  " + testDefinition1 + "  ");
	}
	
	//=================================================================================================
	// Tests of removeServiceDefinitionById
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void removeServiceDefinitionByIdTest() {
		when(serviceDefinitionRepository.existsById(anyLong())).thenReturn(false);
		
		serviceRegistryDBService.removeServiceDefinitionById(0);
	}	
}