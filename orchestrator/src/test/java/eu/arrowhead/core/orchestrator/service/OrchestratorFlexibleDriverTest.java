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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import eu.arrowhead.common.database.entity.OrchestratorStoreFlexible;
import eu.arrowhead.common.dto.shared.OrchestrationFlags.Flag;
import eu.arrowhead.common.dto.shared.OrchestrationFormRequestDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryFormDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryResultDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryResultListDTO;
import eu.arrowhead.common.dto.shared.ServiceRegistryResponseDTO;
import eu.arrowhead.common.dto.shared.ServiceSecurityType;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.common.dto.shared.SystemResponseDTO;
import eu.arrowhead.common.exception.ArrowheadException;
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
		final SystemRequestDTO requesterSystem = new SystemRequestDTO();
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
		final SystemRequestDTO requesterSystem = new SystemRequestDTO();
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
			final ServiceQueryFormDTO requestedService = new ServiceQueryFormDTO();
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
			final ServiceQueryFormDTO requestedService = new ServiceQueryFormDTO();
			requestedService.setServiceDefinitionRequirement("testservice");
			request.setRequestedService(requestedService);
			ReflectionTestUtils.setField(request, "orchestrationFlags", null);
			testObject.queryServiceRegistry(request, List.of(new OrchestratorStoreFlexible()));
		} catch (final IllegalArgumentException ex) {
			Assert.assertEquals("Flags object is null", ex.getMessage());
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCreateServiceQueryFormNoMetadataMerge() {
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO();
		final ServiceQueryFormDTO requestedService = new ServiceQueryFormDTO();
		requestedService.setServiceDefinitionRequirement("testservice");
		requestedService.setMetadataRequirements(Map.of("a", "3", "b ", " 10"));
		requestedService.setInterfaceRequirements(List.of("HTTP-SECURE-JSON", "HTTP-SECURE-XML"));
		requestedService.setSecurityRequirements(List.of(ServiceSecurityType.CERTIFICATE));
		requestedService.setMaxVersionRequirement(10);
		requestedService.setMinVersionRequirement(5);
		requestedService.setVersionRequirement(7);
		request.setRequestedService(requestedService);
		request.getOrchestrationFlags().put(Flag.METADATA_SEARCH, false);
		request.getOrchestrationFlags().put(Flag.PING_PROVIDERS, true);
		
		final OrchestratorStoreFlexible rule = new OrchestratorStoreFlexible();
		rule.setServiceMetadata(" a= 2 ");
		rule.setServiceInterfaceName("HTTP-SECURE-JSON");
		
		final ServiceQueryFormDTO result = (ServiceQueryFormDTO) ReflectionTestUtils.invokeMethod(testObject, "createServiceQueryForm", request, rule);
		
		Assert.assertEquals(requestedService.getServiceDefinitionRequirement(), result.getServiceDefinitionRequirement());
		Assert.assertEquals(1, result.getMetadataRequirements().size());
		Assert.assertTrue(result.getMetadataRequirements().containsKey("a"));
		Assert.assertEquals("2", result.getMetadataRequirements().get("a"));
		Assert.assertTrue(result.getPingProviders());
		Assert.assertEquals(requestedService.getMinVersionRequirement(), result.getMinVersionRequirement());
		Assert.assertEquals(requestedService.getMaxVersionRequirement(), result.getMaxVersionRequirement());
		Assert.assertEquals(requestedService.getVersionRequirement(), result.getVersionRequirement());
		Assert.assertEquals(1, result.getInterfaceRequirements().size());
		Assert.assertEquals("HTTP-SECURE-JSON", result.getInterfaceRequirements().get(0));
		Assert.assertEquals(1, result.getSecurityRequirements().size());
		Assert.assertEquals(ServiceSecurityType.CERTIFICATE, result.getSecurityRequirements().get(0));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCreateServiceQueryFormMetadataMerge() {
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO();
		final ServiceQueryFormDTO requestedService = new ServiceQueryFormDTO();
		requestedService.setServiceDefinitionRequirement("testservice");
		requestedService.setMetadataRequirements(Map.of("a", "3", "b ", " 10"));
		requestedService.setInterfaceRequirements(List.of("HTTP-SECURE-JSON", "HTTP-SECURE-XML"));
		requestedService.setSecurityRequirements(List.of(ServiceSecurityType.CERTIFICATE));
		requestedService.setMaxVersionRequirement(10);
		requestedService.setMinVersionRequirement(5);
		requestedService.setVersionRequirement(7);
		request.setRequestedService(requestedService);
		request.getOrchestrationFlags().put(Flag.METADATA_SEARCH, true);
		request.getOrchestrationFlags().put(Flag.PING_PROVIDERS, true);
		
		final OrchestratorStoreFlexible rule = new OrchestratorStoreFlexible();
		rule.setServiceMetadata(" a= 2 ");
		rule.setServiceInterfaceName("HTTP-SECURE-JSON");
		
		final ServiceQueryFormDTO result = (ServiceQueryFormDTO) ReflectionTestUtils.invokeMethod(testObject, "createServiceQueryForm", request, rule);
		
		Assert.assertEquals(requestedService.getServiceDefinitionRequirement(), result.getServiceDefinitionRequirement());
		Assert.assertEquals(2, result.getMetadataRequirements().size());
		Assert.assertTrue(result.getMetadataRequirements().containsKey("a"));
		Assert.assertEquals("3", result.getMetadataRequirements().get("a"));
		Assert.assertTrue(result.getMetadataRequirements().containsKey("b"));
		Assert.assertEquals("10", result.getMetadataRequirements().get("b"));
		Assert.assertTrue(result.getPingProviders());
		Assert.assertEquals(requestedService.getMinVersionRequirement(), result.getMinVersionRequirement());
		Assert.assertEquals(requestedService.getMaxVersionRequirement(), result.getMaxVersionRequirement());
		Assert.assertEquals(requestedService.getVersionRequirement(), result.getVersionRequirement());
		Assert.assertEquals(1, result.getInterfaceRequirements().size());
		Assert.assertEquals("HTTP-SECURE-JSON", result.getInterfaceRequirements().get(0));
		Assert.assertEquals(1, result.getSecurityRequirements().size());
		Assert.assertEquals(ServiceSecurityType.CERTIFICATE, result.getSecurityRequirements().get(0));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testQueryServiceRegistryNotEnoughResult() {
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO();
		final ServiceQueryFormDTO requestedService = new ServiceQueryFormDTO();
		requestedService.setServiceDefinitionRequirement("testservice");
		request.setRequestedService(requestedService);
		
		when(driver.multiQueryServiceRegistry(any())).thenReturn(new ServiceQueryResultListDTO(List.of()));
		
		try {
			testObject.queryServiceRegistry(request, List.of(new OrchestratorStoreFlexible()));
		} catch (final ArrowheadException ex) {
			verify(driver).multiQueryServiceRegistry(any());
			Assert.assertEquals("Service Registry does not handle all query forms.", ex.getMessage());
			throw ex;
		}
	}
	
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testQueryServiceRegistryOk() {
		final OrchestrationFormRequestDTO request = new OrchestrationFormRequestDTO();
		final ServiceQueryFormDTO requestedService = new ServiceQueryFormDTO();
		requestedService.setServiceDefinitionRequirement("testservice");
		request.setRequestedService(requestedService);
		
		final ServiceQueryResultDTO queryResultDTO = new ServiceQueryResultDTO();
		when(driver.multiQueryServiceRegistry(any())).thenReturn(new ServiceQueryResultListDTO(List.of(queryResultDTO)));
		
		final OrchestratorStoreFlexible rule = new OrchestratorStoreFlexible();
		rule.setId(231);
		final List<Pair<OrchestratorStoreFlexible,ServiceQueryResultDTO>> result = testObject.queryServiceRegistry(request, List.of(rule));
		verify(driver).multiQueryServiceRegistry(any());
		Assert.assertEquals(1, result.size());
		Assert.assertEquals(231, result.get(0).getKey().getId());
		Assert.assertEquals(0, result.get(0).getValue().getServiceQueryData().size());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testFilterSRResultsByProviderRequirementsNullQueryData() {
		try {
			testObject.filterSRResultsByProviderRequirements(null, null);
		} catch (final IllegalArgumentException ex) {
			Assert.assertEquals("Query data is null.", ex.getMessage());
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testFilterSRResultsByProviderRequirementsEmptyQueryData() {
		final List<ServiceRegistryResponseDTO> result = testObject.filterSRResultsByProviderRequirements(List.of(), null);
		Assert.assertEquals(0, result.size());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testFilterSRResultsByProviderRequirementsPairNull() {
		try {
			final List<Pair<OrchestratorStoreFlexible,ServiceQueryResultDTO>> queryData = new ArrayList<>(1);
			queryData.add(null);
			testObject.filterSRResultsByProviderRequirements(queryData, null);
		} catch (final IllegalArgumentException ex) {
			Assert.assertEquals("Query data is null.", ex.getMessage());
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testFilterSRResultsByProviderRequirementsRuleNull() {
		try {
			final List<Pair<OrchestratorStoreFlexible,ServiceQueryResultDTO>> queryData = new ArrayList<>(1);
			queryData.add(new ImmutablePair<OrchestratorStoreFlexible,ServiceQueryResultDTO>(null, null));
			testObject.filterSRResultsByProviderRequirements(queryData, null);
		} catch (final IllegalArgumentException ex) {
			Assert.assertEquals("Rule is null", ex.getMessage());
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testFilterSRResultsByProviderRequirementsSRResultNull() {
		try {
			final List<Pair<OrchestratorStoreFlexible,ServiceQueryResultDTO>> queryData = new ArrayList<>(1);
			queryData.add(new ImmutablePair<OrchestratorStoreFlexible,ServiceQueryResultDTO>(new OrchestratorStoreFlexible(), null));
			testObject.filterSRResultsByProviderRequirements(queryData, null);
		} catch (final IllegalArgumentException ex) {
			Assert.assertEquals("Service Registry query result is null", ex.getMessage());
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testFilterSRResultsByProviderRequirementsNoProviders1() {
		final List<Pair<OrchestratorStoreFlexible,ServiceQueryResultDTO>> queryData = new ArrayList<>(1);
		final ServiceQueryResultDTO queryResult = new ServiceQueryResultDTO();
		queryResult.setServiceQueryData(null);
		queryData.add(new ImmutablePair<OrchestratorStoreFlexible,ServiceQueryResultDTO>(new OrchestratorStoreFlexible(), queryResult));
		final List<ServiceRegistryResponseDTO> result = testObject.filterSRResultsByProviderRequirements(queryData, null);
		
		Assert.assertEquals(0, result.size());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testFilterSRResultsByProviderRequirementsNoProviders2() {
		final List<Pair<OrchestratorStoreFlexible,ServiceQueryResultDTO>> queryData = new ArrayList<>(1);
		final ServiceQueryResultDTO queryResult = new ServiceQueryResultDTO();
		queryData.add(new ImmutablePair<OrchestratorStoreFlexible,ServiceQueryResultDTO>(new OrchestratorStoreFlexible(), queryResult));
		final List<ServiceRegistryResponseDTO> result = testObject.filterSRResultsByProviderRequirements(queryData, null);
		
		Assert.assertEquals(0, result.size());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testFilterSRResultsByProviderRequirementsNameMismatch() {
		final OrchestratorStoreFlexible rule = new OrchestratorStoreFlexible();
		rule.setProviderSystemName("testprovider");
		
		final ServiceQueryResultDTO queryResult = new ServiceQueryResultDTO();
		final ServiceRegistryResponseDTO serviceRegistryResponseDTO = new ServiceRegistryResponseDTO();
		final SystemResponseDTO provider = new SystemResponseDTO();
		provider.setSystemName("otherprovider");
		serviceRegistryResponseDTO.setProvider(provider);
		queryResult.setServiceQueryData(List.of(serviceRegistryResponseDTO));
		
		final List<Pair<OrchestratorStoreFlexible,ServiceQueryResultDTO>> queryData = new ArrayList<>(1);
		queryData.add(new ImmutablePair<OrchestratorStoreFlexible,ServiceQueryResultDTO>(rule, queryResult));
		
		final List<ServiceRegistryResponseDTO> result = testObject.filterSRResultsByProviderRequirements(queryData, null);
		
		Assert.assertEquals(0, result.size());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testFilterSRResultsByProviderRequirementsMetadataMismatch() {
		final OrchestratorStoreFlexible rule = new OrchestratorStoreFlexible();
		rule.setProviderSystemName("testprovider");
		rule.setProviderSystemMetadata("a=2");
		
		final ServiceQueryResultDTO queryResult = new ServiceQueryResultDTO();
		final ServiceRegistryResponseDTO serviceRegistryResponseDTO = new ServiceRegistryResponseDTO();
		final SystemResponseDTO provider = new SystemResponseDTO();
		provider.setSystemName("testprovider");
		provider.setAddress("localhost");
		provider.setPort(1234);
		provider.setMetadata(Map.of("a", "10"));
		serviceRegistryResponseDTO.setProvider(provider);
		queryResult.setServiceQueryData(List.of(serviceRegistryResponseDTO));
		
		final List<Pair<OrchestratorStoreFlexible,ServiceQueryResultDTO>> queryData = new ArrayList<>(1);
		queryData.add(new ImmutablePair<OrchestratorStoreFlexible,ServiceQueryResultDTO>(rule, queryResult));
		
		final List<ServiceRegistryResponseDTO> result = testObject.filterSRResultsByProviderRequirements(queryData, null);
		
		Assert.assertEquals(0, result.size());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testFilterSRResultsByProviderRequirementsNoDuplicates() {
		final OrchestratorStoreFlexible rule = new OrchestratorStoreFlexible();
		rule.setProviderSystemName("testprovider");
		final OrchestratorStoreFlexible rule2 = new OrchestratorStoreFlexible();
		rule2.setProviderSystemMetadata("a=10");
		
		final ServiceQueryResultDTO queryResult = new ServiceQueryResultDTO();
		final ServiceRegistryResponseDTO serviceRegistryResponseDTO = new ServiceRegistryResponseDTO();
		final SystemResponseDTO provider = new SystemResponseDTO();
		provider.setSystemName("testprovider");
		provider.setAddress("localhost");
		provider.setPort(1234);
		provider.setMetadata(Map.of("a", "10"));
		serviceRegistryResponseDTO.setProvider(provider);
		queryResult.setServiceQueryData(List.of(serviceRegistryResponseDTO));
		
		final List<Pair<OrchestratorStoreFlexible,ServiceQueryResultDTO>> queryData = new ArrayList<>(1);
		queryData.add(new ImmutablePair<OrchestratorStoreFlexible,ServiceQueryResultDTO>(rule, queryResult));
		queryData.add(new ImmutablePair<OrchestratorStoreFlexible,ServiceQueryResultDTO>(rule2, queryResult));
		
		final List<ServiceRegistryResponseDTO> result = testObject.filterSRResultsByProviderRequirements(queryData, null);
		
		Assert.assertEquals(1, result.size());
	}
}