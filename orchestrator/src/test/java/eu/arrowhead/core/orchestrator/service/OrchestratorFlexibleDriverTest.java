/********************************************************************************
 * Copyright (c) 2021 AITIA
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

package eu.arrowhead.core.orchestrator.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import eu.arrowhead.common.database.entity.OrchestratorStoreFlexible;
import eu.arrowhead.common.dto.shared.OrchestrationFormRequestDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryFormDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryResultDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.common.dto.shared.SystemResponseDTO;
import eu.arrowhead.core.orchestrator.database.service.OrchestratorStoreFlexibleDBService;

@RunWith(SpringRunner.class)
public class OrchestratorFlexibleDriverTest {
	
	//=================================================================================================
	//  members
	
	@InjectMocks
	private OrchestratorFlexibleDriver testObject;
	
	@Mock
	private OrchestratorStoreFlexibleDBService dbService;
	
	@Mock
	private OrchestratorDriver driver;

	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testQueryConsumerSystemRequestNull() {
		try {
			testObject.queryConsumerSystem(null);
		} catch (final IllegalArgumentException ex) {
			Assert.assertEquals("Request is null.", ex.getMessage());
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testQueryConsumerSystemOk() {
		when(driver.queryServiceRegistryBySystemRequestDTO(any(SystemRequestDTO.class))).thenReturn(new SystemResponseDTO());
		
		testObject.queryConsumerSystem(new SystemRequestDTO());
		
		verify(driver).queryServiceRegistryBySystemRequestDTO(any(SystemRequestDTO.class));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testCollectAndSortMatchingRulesOriginalRequestNull() {
		try {
			testObject.collectAndSortMatchingRules(null, null);
		} catch (final IllegalArgumentException ex) {
			Assert.assertEquals("Original request is null.", ex.getMessage());
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testCollectAndSortMatchingRulesRequestedServiceNull() {
		try {
			testObject.collectAndSortMatchingRules(new OrchestrationFormRequestDTO(), null);
		} catch (final IllegalArgumentException ex) {
			Assert.assertEquals("Requested service is null.", ex.getMessage());
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testCollectAndSortMatchingRulesConsumerSystemNull() {
		try {
			final OrchestrationFormRequestDTO originalRequest = new OrchestrationFormRequestDTO();
			originalRequest.setRequestedService(new ServiceQueryFormDTO());
			testObject.collectAndSortMatchingRules(originalRequest, null);
		} catch (final IllegalArgumentException ex) {
			Assert.assertEquals("Consumer system is null.", ex.getMessage());
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testCollectAndSortMatchingRulesConsumerSystemNameNull() {
		try {
			final OrchestrationFormRequestDTO originalRequest = new OrchestrationFormRequestDTO();
			originalRequest.setRequestedService(new ServiceQueryFormDTO());
			testObject.collectAndSortMatchingRules(originalRequest, new SystemResponseDTO());
		} catch (final IllegalArgumentException ex) {
			Assert.assertEquals("Consumer system name is null or blank.", ex.getMessage());
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testCollectAndSortMatchingRulesConsumerSystemNameEmpty() {
		try {
			final OrchestrationFormRequestDTO originalRequest = new OrchestrationFormRequestDTO();
			originalRequest.setRequestedService(new ServiceQueryFormDTO());
			final SystemResponseDTO consumerSystem = new SystemResponseDTO();
			consumerSystem.setSystemName(" ");
			testObject.collectAndSortMatchingRules(originalRequest, consumerSystem);
		} catch (final IllegalArgumentException ex) {
			Assert.assertEquals("Consumer system name is null or blank.", ex.getMessage());
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCollectAndSortMatchingRulesInterfaceFilterDropAll() {
		final OrchestrationFormRequestDTO originalRequest = new OrchestrationFormRequestDTO();
		final ServiceQueryFormDTO requestedService = new ServiceQueryFormDTO.Builder("testservice")
																			.interfaces("INVALID-INTERFACE")
																			.build();
		originalRequest.setRequestedService(requestedService);
		originalRequest.setRequesterSystem(new SystemRequestDTO());
		final SystemResponseDTO consumerSystem = new SystemResponseDTO();
		consumerSystem.setSystemName("testconsumer");

		final List<OrchestratorStoreFlexible> nameBasedRules = List.of(new OrchestratorStoreFlexible("testconsumer", null, null, null, null, "HTTP-SECURE-JSON", "testservice", 1));
		when(dbService.getMatchedRulesByServiceDefinitionAndConsumerName("testservice", "testconsumer")).thenReturn(nameBasedRules);
		when(dbService.getMatchedRulesByServiceDefinitionAndNonNullConsumerMetadata("testservice")).thenReturn(List.of());
		
		final List<OrchestratorStoreFlexible> result = testObject.collectAndSortMatchingRules(originalRequest, consumerSystem);
		Assert.assertTrue(result.isEmpty());
		verify(dbService).getMatchedRulesByServiceDefinitionAndConsumerName("testservice", "testconsumer");
		verify(dbService).getMatchedRulesByServiceDefinitionAndNonNullConsumerMetadata("testservice");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCollectAndSortMatchingRulesMetadataBasedMatchAdditionalNameMatchFailed() {
		final OrchestrationFormRequestDTO originalRequest = new OrchestrationFormRequestDTO();
		final ServiceQueryFormDTO requestedService = new ServiceQueryFormDTO.Builder("testservice")
																			.interfaces("http-secure-json")
																			.build();
		originalRequest.setRequestedService(requestedService);
		originalRequest.setRequesterSystem(new SystemRequestDTO());
		final SystemResponseDTO consumerSystem = new SystemResponseDTO();
		consumerSystem.setSystemName("testconsumer");
		consumerSystem.setMetadata(Map.of("a", "1 ", "b", "2"));

		when(dbService.getMatchedRulesByServiceDefinitionAndConsumerName("testservice", "testconsumer")).thenReturn(List.of());
		final List<OrchestratorStoreFlexible> metadataBasedRules = List.of(new OrchestratorStoreFlexible("otherconsumer", null, "a=1", null, null, "HTTP-SECURE-JSON", "testservice", 1));
		when(dbService.getMatchedRulesByServiceDefinitionAndNonNullConsumerMetadata("testservice")).thenReturn(metadataBasedRules);
		
		final List<OrchestratorStoreFlexible> result = testObject.collectAndSortMatchingRules(originalRequest, consumerSystem);
		Assert.assertTrue(result.isEmpty());
		verify(dbService).getMatchedRulesByServiceDefinitionAndConsumerName("testservice", "testconsumer");
		verify(dbService).getMatchedRulesByServiceDefinitionAndNonNullConsumerMetadata("testservice");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCollectAndSortMatchingRulesMetadataBasedMatchFailed() {
		final OrchestrationFormRequestDTO originalRequest = new OrchestrationFormRequestDTO();
		final ServiceQueryFormDTO requestedService = new ServiceQueryFormDTO.Builder("testservice")
																			.interfaces("http-secure-json")
																			.build();
		originalRequest.setRequestedService(requestedService);
		SystemRequestDTO requesterSystem = new SystemRequestDTO();
		requesterSystem.setMetadata(Map.of("c ", "3", "b", "10"));
		originalRequest.setRequesterSystem(requesterSystem);
		final SystemResponseDTO consumerSystem = new SystemResponseDTO();
		consumerSystem.setSystemName("testconsumer");
		consumerSystem.setMetadata(Map.of("a", "1 ", "b", "2"));

		when(dbService.getMatchedRulesByServiceDefinitionAndConsumerName("testservice", "testconsumer")).thenReturn(List.of());
		final List<OrchestratorStoreFlexible> metadataBasedRules = List.of(new OrchestratorStoreFlexible(null, null, "b=2", null, null, "HTTP-SECURE-JSON", "testservice", 1));
		when(dbService.getMatchedRulesByServiceDefinitionAndNonNullConsumerMetadata("testservice")).thenReturn(metadataBasedRules);
		
		final List<OrchestratorStoreFlexible> result = testObject.collectAndSortMatchingRules(originalRequest, consumerSystem);
		Assert.assertTrue(result.isEmpty());
		verify(dbService).getMatchedRulesByServiceDefinitionAndConsumerName("testservice", "testconsumer");
		verify(dbService).getMatchedRulesByServiceDefinitionAndNonNullConsumerMetadata("testservice");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCollectAndSortMatchingRulesOk() {
		final OrchestrationFormRequestDTO originalRequest = new OrchestrationFormRequestDTO();
		final ServiceQueryFormDTO requestedService = new ServiceQueryFormDTO.Builder("testservice")
																			.interfaces("http-secure-json")
																			.build();
		originalRequest.setRequestedService(requestedService);
		SystemRequestDTO requesterSystem = new SystemRequestDTO();
		requesterSystem.setMetadata(Map.of("c ", "3", "b", "10"));
		originalRequest.setRequesterSystem(requesterSystem);
		final SystemResponseDTO consumerSystem = new SystemResponseDTO();
		consumerSystem.setSystemName("testconsumer");
		consumerSystem.setMetadata(Map.of("a", "1 ", "b", "2"));

		final OrchestratorStoreFlexible rule1 = new OrchestratorStoreFlexible("testconsumer", null, null, null, null, null, "testservice", 2);
		rule1.setId(1);
		final OrchestratorStoreFlexible rule2 = new OrchestratorStoreFlexible(null, null, "b=10", null, null, "HTTP-SECURE-JSON", "testservice", 1);
		rule2.setId(2);
		when(dbService.getMatchedRulesByServiceDefinitionAndConsumerName("testservice", "testconsumer")).thenReturn(List.of(rule1));
		when(dbService.getMatchedRulesByServiceDefinitionAndNonNullConsumerMetadata("testservice")).thenReturn(List.of(rule2));
		
		final List<OrchestratorStoreFlexible> result = testObject.collectAndSortMatchingRules(originalRequest, consumerSystem);
		Assert.assertEquals(2, result.size());
		Assert.assertEquals(1, result.get(0).getPriority());
		Assert.assertEquals(2, result.get(1).getPriority());
		Assert.assertEquals("testconsumer", result.get(1).getConsumerSystemName());
		verify(dbService).getMatchedRulesByServiceDefinitionAndConsumerName("testservice", "testconsumer");
		verify(dbService).getMatchedRulesByServiceDefinitionAndNonNullConsumerMetadata("testservice");
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testRuleComparator() {
		final Comparator<OrchestratorStoreFlexible> comparator = (Comparator) ReflectionTestUtils.getField(testObject, "ruleComparator");
		
		final OrchestratorStoreFlexible rule1 = new OrchestratorStoreFlexible();
		rule1.setId(1);
		rule1.setPriority(10);
		
		final OrchestratorStoreFlexible rule2 = new OrchestratorStoreFlexible();
		rule2.setId(2);
		rule2.setPriority(5);

		final OrchestratorStoreFlexible rule3 = new OrchestratorStoreFlexible();
		rule3.setId(3);
		rule3.setPriority(1);
		
		final OrchestratorStoreFlexible rule4 = new OrchestratorStoreFlexible();
		rule4.setId(4);
		rule4.setPriority(1);
		
		final List<OrchestratorStoreFlexible> list = new ArrayList<>(4);
		list.add(rule1);
		list.add(rule2);
		list.add(rule3);
		list.add(rule4);

		Collections.sort(list, comparator);
		
		Assert.assertEquals(4, list.size());
		Assert.assertEquals(3, list.get(0).getId());
		Assert.assertEquals(4, list.get(1).getId());
		Assert.assertEquals(2, list.get(2).getId());
		Assert.assertEquals(1, list.get(3).getId());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testQueryServiceRegistryRequestNull() {
		try {
			testObject.queryServiceRegistry(null, null);
		} catch (final IllegalArgumentException ex) {
			Assert.assertEquals("Request is null", ex.getMessage());
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testQueryServiceRegistryRulesListNull() {
		try {
			testObject.queryServiceRegistry(new OrchestrationFormRequestDTO(), null);
		} catch (final IllegalArgumentException ex) {
			Assert.assertEquals("Rules list is null", ex.getMessage());
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testQueryServiceRegistryRulesListEmpty() {
		final List<Pair<OrchestratorStoreFlexible,ServiceQueryResultDTO>> result = testObject.queryServiceRegistry(new OrchestrationFormRequestDTO(), List.of());
		Assert.assertTrue(result.isEmpty());
	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testQueryServiceRegistryRuleNull() {
		try {
			final List<OrchestratorStoreFlexible> rules = new ArrayList<>(1);
			rules.add(null);
			testObject.queryServiceRegistry(new OrchestrationFormRequestDTO(), rules);
		} catch (final IllegalArgumentException ex) {
			Assert.assertEquals("Rule is null", ex.getMessage());
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testQueryServiceRegistryRequestedServiceNull() {
		try {
			testObject.queryServiceRegistry(new OrchestrationFormRequestDTO(), List.of(new OrchestratorStoreFlexible()));
		} catch (final IllegalArgumentException ex) {
			Assert.assertEquals("Requested service is null", ex.getMessage());
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testQueryServiceRegistryRequestedServiceDefinitionNull() {
		try {
			final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO();
			request.setRequestedService(new ServiceQueryFormDTO());
			testObject.queryServiceRegistry(request, List.of(new OrchestratorStoreFlexible()));
		} catch (final IllegalArgumentException ex) {
			Assert.assertEquals("Requested service definition is null or blank", ex.getMessage());
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testQueryServiceRegistryRequestedServiceDefinitionEmpty() {
		try {
			final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO();
			ServiceQueryFormDTO requestedService = new ServiceQueryFormDTO();
			requestedService.setServiceDefinitionRequirement(" ");
			request.setRequestedService(requestedService);
			testObject.queryServiceRegistry(request, List.of(new OrchestratorStoreFlexible()));
		} catch (final IllegalArgumentException ex) {
			Assert.assertEquals("Requested service definition is null or blank", ex.getMessage());
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testQueryServiceRegistryFlagsObjectNull() {
		try {
			final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO();
			ServiceQueryFormDTO requestedService = new ServiceQueryFormDTO();
			requestedService.setServiceDefinitionRequirement("testservice");
			request.setRequestedService(requestedService);
			ReflectionTestUtils.setField(request, "orchestrationFlags", null);
			testObject.queryServiceRegistry(request, List.of(new OrchestratorStoreFlexible()));
		} catch (final IllegalArgumentException ex) {
			Assert.assertEquals("Flags object is null", ex.getMessage());
			throw ex;
		}
	}
}