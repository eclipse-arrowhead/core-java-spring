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

package eu.arrowhead.core.orchestrator.database.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.hibernate.HibernateException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.database.entity.OrchestratorStoreFlexible;
import eu.arrowhead.common.database.repository.OrchestratorStoreFlexibleRepository;
import eu.arrowhead.common.dto.internal.OrchestratorStoreFlexibleListResponseDTO;
import eu.arrowhead.common.dto.internal.OrchestratorStoreFlexibleRequestDTO;
import eu.arrowhead.common.dto.internal.SystemDescriberDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.common.verifier.CommonNamePartVerifier;
import eu.arrowhead.common.verifier.ServiceInterfaceNameVerifier;

@RunWith(SpringRunner.class)
public class OrchestratorStoreFlexibleDBServiceTest {

	//=================================================================================================
	// members
	
	@InjectMocks
	private OrchestratorStoreFlexibleDBService dbService; 
	
	@Mock
	private OrchestratorStoreFlexibleRepository orchestratorStoreFlexibleRepository;
	
	@Spy
	private CommonNamePartVerifier cnVerifier;
	
	@Spy
	private ServiceInterfaceNameVerifier interfaceNameVerifier;

		
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGetMatchedRulesByServiceDefinitionAndConsumerNameServiceDefinitionNull() {
		try {
			dbService.getMatchedRulesByServiceDefinitionAndConsumerName(null, null);
		} catch (final InvalidParameterException ex) {
			Assert.assertEquals("Service definition is empty", ex.getMessage());
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGetMatchedRulesByServiceDefinitionAndConsumerNameServiceDefinitionEmpty() {
		try {
			dbService.getMatchedRulesByServiceDefinitionAndConsumerName(" ", null);
		} catch (final InvalidParameterException ex) {
			Assert.assertEquals("Service definition is empty", ex.getMessage());
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGetMatchedRulesByServiceDefinitionAndConsumerNameConsumerNameNull() {
		try {
			dbService.getMatchedRulesByServiceDefinitionAndConsumerName("testService", null);
		} catch (final InvalidParameterException ex) {
			Assert.assertEquals("Consumer system name is empty", ex.getMessage());
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGetMatchedRulesByServiceDefinitionAndConsumerNameConsumerNameEmpty() {
		try {
			dbService.getMatchedRulesByServiceDefinitionAndConsumerName("testService", "");
		} catch (final InvalidParameterException ex) {
			Assert.assertEquals("Consumer system name is empty", ex.getMessage());
			throw ex;
		}
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetMatchedRulesByServiceDefinitionAndConsumerNameOk() {
		when(orchestratorStoreFlexibleRepository.findByServiceDefinitionNameAndConsumerSystemNameAndConsumerSystemMetadataIsNull(anyString(), anyString())).thenReturn(List.of(new OrchestratorStoreFlexible()));
		
		dbService.getMatchedRulesByServiceDefinitionAndConsumerName("testService", "consumer");
		
		verify(orchestratorStoreFlexibleRepository).findByServiceDefinitionNameAndConsumerSystemNameAndConsumerSystemMetadataIsNull("testservice", "consumer");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGetMatchedRulesByServiceDefinitionAndNonNullConsumerMetadataServiceDefinitionNull() {
		try {
			dbService.getMatchedRulesByServiceDefinitionAndNonNullConsumerMetadata(null);
		} catch (final InvalidParameterException ex) {
			Assert.assertEquals("Service definition is empty", ex.getMessage());
			throw ex;
		}		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGetMatchedRulesByServiceDefinitionAndNonNullConsumerMetadataServiceDefinitionEmpty() {
		try {
			dbService.getMatchedRulesByServiceDefinitionAndNonNullConsumerMetadata("");
		} catch (final InvalidParameterException ex) {
			Assert.assertEquals("Service definition is empty", ex.getMessage());
			throw ex;
		}		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetMatchedRulesByServiceDefinitionAndNonNullConsumerMetadataOk() {
		when(orchestratorStoreFlexibleRepository.findByServiceDefinitionNameAndConsumerSystemMetadataIsNotNull(anyString())).thenReturn(List.of(new OrchestratorStoreFlexible()));
		
		dbService.getMatchedRulesByServiceDefinitionAndNonNullConsumerMetadata("testService");
		
		verify(orchestratorStoreFlexibleRepository).findByServiceDefinitionNameAndConsumerSystemMetadataIsNotNull("testservice");
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetOrchestratorStoreFlexibleEntriesResponse() {
		final int page = 0;
		final int size = 25;
		final Direction direction = Direction.ASC;
		final String sortField = "id";
		
		final OrchestratorStoreFlexible entity = new OrchestratorStoreFlexible("consumer", "provider", null, null, null, "interface", "serviceDef", null);
		
		final ArgumentCaptor<PageRequest> pageRequestCaptor = ArgumentCaptor.forClass(PageRequest.class);
		when(orchestratorStoreFlexibleRepository.findAll(pageRequestCaptor.capture())).thenReturn(new PageImpl<OrchestratorStoreFlexible>(List.of(entity)));
		
		final OrchestratorStoreFlexibleListResponseDTO response = dbService.getOrchestratorStoreFlexibleEntriesResponse(page, size, direction, sortField);
		
		final PageRequest captured = pageRequestCaptor.getValue();
		assertEquals(page, captured.getPageNumber());
		assertEquals(size, captured.getPageSize());
		assertTrue(captured.getSort().getOrderFor(sortField).isAscending());
		assertEquals("consumer", response.getData().get(0).getConsumerSystem().getSystemName());
		assertEquals("provider", response.getData().get(0).getProviderSystem().getSystemName());
		assertEquals("interface", response.getData().get(0).getServiceInterface());
		assertEquals("serviceDef", response.getData().get(0).getServiceDefinition());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetOrchestratorStoreFlexibleEntries_Ok_1() {
		final int page = 0;
		final int size = 25;
		final Direction direction = Direction.ASC;
		final String sortField = "id";
		
		final ArgumentCaptor<PageRequest> pageRequestCaptor = ArgumentCaptor.forClass(PageRequest.class);
		when(orchestratorStoreFlexibleRepository.findAll(pageRequestCaptor.capture())).thenReturn(new PageImpl<OrchestratorStoreFlexible>(List.of()));
		
		dbService.getOrchestratorStoreFlexibleEntries(page, size, direction, sortField);
		
		final PageRequest captured = pageRequestCaptor.getValue();
		assertEquals(page, captured.getPageNumber());
		assertEquals(size, captured.getPageSize());
		assertTrue(captured.getSort().getOrderFor(sortField).isAscending());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetOrchestratorStoreFlexibleEntries_Ok_2() {
		final int page = -1;
		final int size = -1;
		final Direction direction = null;
		final String sortField = "";
		
		final ArgumentCaptor<PageRequest> pageRequestCaptor = ArgumentCaptor.forClass(PageRequest.class);
		when(orchestratorStoreFlexibleRepository.findAll(pageRequestCaptor.capture())).thenReturn(new PageImpl<OrchestratorStoreFlexible>(List.of()));
		
		dbService.getOrchestratorStoreFlexibleEntries(page, size, direction, sortField);
		
		final PageRequest captured = pageRequestCaptor.getValue();
		assertEquals(0, captured.getPageNumber());
		assertEquals(Integer.MAX_VALUE, captured.getPageSize());
		assertTrue(captured.getSort().getOrderFor("id").isAscending());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGetOrchestratorStoreFlexibleEntries_InvalidSortField() {
		final int page = 0;
		final int size = 25;
		final Direction direction = Direction.ASC;
		final String sortField = "invalid";
		
		when(orchestratorStoreFlexibleRepository.findAll(any(PageRequest.class))).thenReturn(new PageImpl<OrchestratorStoreFlexible>(List.of()));		
		dbService.getOrchestratorStoreFlexibleEntries(page, size, direction, sortField);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testGetOrchestratorStoreFlexibleEntries_DBException() {
		when(orchestratorStoreFlexibleRepository.findAll(any(PageRequest.class))).thenThrow(new HibernateException("test"));
		
		try {
			dbService.getOrchestratorStoreFlexibleEntries(anyInt(), anyInt(), any(), anyString());			
		} catch (final ArrowheadException ex) {
			assertEquals(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG, ex.getMessage());
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("unchecked")
	@Test
	public void testCreateOrchestratorStoreFlexibleResponse() {
		final OrchestratorStoreFlexibleRequestDTO request = new OrchestratorStoreFlexibleRequestDTO(new SystemDescriberDTO("consumer", Map.of("k1", "v1", "k2", "v2")),
																									   new SystemDescriberDTO("provider", Map.of("k3", "v3", "k4", "v4")),
																									   "serviceDef",
																									   "HTTP-SECURE-JSON",
																									   Map.of("k5", "v5", "k6", "v6"),
																									   5);
		
		final ArgumentCaptor<List<OrchestratorStoreFlexible>> captor = ArgumentCaptor.forClass(List.class);
		when(orchestratorStoreFlexibleRepository.saveAll(captor.capture())).thenReturn(List.of(new OrchestratorStoreFlexible()));
		doNothing().when(orchestratorStoreFlexibleRepository).flush();
		dbService.createOrchestratorStoreFlexibleResponse(List.of(request));
		
		final List<OrchestratorStoreFlexible> captured = captor.getValue();
		assertEquals("consumer", captured.get(0).getConsumerSystemName());
		assertTrue(captured.get(0).getConsumerSystemMetadata().contains("k1=v1"));
		assertTrue(captured.get(0).getConsumerSystemMetadata().contains("k2=v2"));
		assertEquals("provider", captured.get(0).getProviderSystemName());
		assertTrue(captured.get(0).getProviderSystemMetadata().contains("k3=v3"));
		assertTrue(captured.get(0).getProviderSystemMetadata().contains("k4=v4"));
		assertEquals("servicedef", captured.get(0).getServiceDefinitionName());
		assertEquals("HTTP-SECURE-JSON", captured.get(0).getServiceInterfaceName());
		assertTrue(captured.get(0).getServiceMetadata().contains("k5=v5"));
		assertTrue(captured.get(0).getServiceMetadata().contains("k6=v6"));
		assertEquals(5, captured.get(0).getPriority());
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("unchecked")
	@Test
	public void testCreateOrchestratorStoreFlexible_Ok_1() {
		final String consumerSystemName = "consumer";
		final String providerSystemName = "provider";
		final String consumerSystemMetadata = null;
		final String providerSystemMetadata = null;
		final String serviceMetadata = null;
		final String serviceInterfaceName = "HTTP-SECURE-JSON";
		final String serviceDefinitionName = "service";
		final Integer priority = null;
		
		final ArgumentCaptor<List<OrchestratorStoreFlexible>> captor = ArgumentCaptor.forClass(List.class);
		when(orchestratorStoreFlexibleRepository.saveAll(captor.capture())).thenReturn(List.of(new OrchestratorStoreFlexible()));
		doNothing().when(orchestratorStoreFlexibleRepository).flush();
		dbService.createOrchestratorStoreFlexible(consumerSystemName, providerSystemName, consumerSystemMetadata, providerSystemMetadata, serviceMetadata, serviceInterfaceName, serviceDefinitionName, priority);
		
		final List<OrchestratorStoreFlexible> captured = captor.getValue();
		assertEquals(consumerSystemName, captured.get(0).getConsumerSystemName());
		assertEquals(providerSystemName, captured.get(0).getProviderSystemName());
		assertTrue(consumerSystemMetadata == null);
		assertTrue(providerSystemMetadata == null);
		assertEquals(serviceInterfaceName, captured.get(0).getServiceInterfaceName());
		assertEquals(serviceDefinitionName, captured.get(0).getServiceDefinitionName());
		assertEquals(Integer.MAX_VALUE, captured.get(0).getPriority());
		
		verify(cnVerifier, times(1)).isValid(eq(consumerSystemName));
		verify(cnVerifier, times(1)).isValid(eq(providerSystemName));
		verify(interfaceNameVerifier, times(1)).isValid(eq(serviceInterfaceName));
		verify(orchestratorStoreFlexibleRepository, times(1)).flush();
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("unchecked")
	@Test
	public void testCreateOrchestratorStoreFlexible_Ok_2() {
		final String consumerSystemName = "  consumer  ";
		final String providerSystemName = "  provider  ";
		final String consumerSystemMetadata = null;
		final String providerSystemMetadata = null;
		final String serviceMetadata = null;
		final String serviceInterfaceName = "  HTTP-SECURE-JSON  ";
		final String serviceDefinitionName = "  service  ";
		final Integer priority = null;
		
		final ArgumentCaptor<List<OrchestratorStoreFlexible>> captor = ArgumentCaptor.forClass(List.class);
		when(orchestratorStoreFlexibleRepository.saveAll(captor.capture())).thenReturn(List.of(new OrchestratorStoreFlexible()));
		doNothing().when(orchestratorStoreFlexibleRepository).flush();
		dbService.createOrchestratorStoreFlexible(consumerSystemName, providerSystemName, consumerSystemMetadata, providerSystemMetadata, serviceMetadata, serviceInterfaceName, serviceDefinitionName, priority);
		
		final List<OrchestratorStoreFlexible> captured = captor.getValue();
		assertEquals(consumerSystemName.trim(), captured.get(0).getConsumerSystemName());
		assertEquals(providerSystemName.trim(), captured.get(0).getProviderSystemName());
		assertTrue(consumerSystemMetadata == null);
		assertTrue(providerSystemMetadata == null);
		assertEquals(serviceInterfaceName.trim(), captured.get(0).getServiceInterfaceName());
		assertEquals(serviceDefinitionName.trim(), captured.get(0).getServiceDefinitionName());
		assertEquals(Integer.MAX_VALUE, captured.get(0).getPriority());
		
		verify(cnVerifier, times(1)).isValid(eq(consumerSystemName.trim()));
		verify(cnVerifier, times(1)).isValid(eq(providerSystemName.trim()));
		verify(interfaceNameVerifier, times(1)).isValid(eq(serviceInterfaceName.trim()));
		verify(orchestratorStoreFlexibleRepository, times(1)).flush();
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("unchecked")
	@Test
	public void testCreateOrchestratorStoreFlexible_Ok_3() {
		final String consumerSystemName = null;
		final String providerSystemName = null;
		final String consumerSystemMetadata = "consumerMeta";
		final String providerSystemMetadata = "providerMeta";
		final String serviceMetadata = "serviceMeta";
		final String serviceInterfaceName = null;
		final String serviceDefinitionName = "service";
		final Integer priority = 5;
		
		final ArgumentCaptor<List<OrchestratorStoreFlexible>> captor = ArgumentCaptor.forClass(List.class);
		when(orchestratorStoreFlexibleRepository.saveAll(captor.capture())).thenReturn(List.of(new OrchestratorStoreFlexible()));
		doNothing().when(orchestratorStoreFlexibleRepository).flush();
		dbService.createOrchestratorStoreFlexible(consumerSystemName, providerSystemName, consumerSystemMetadata, providerSystemMetadata, serviceMetadata, serviceInterfaceName, serviceDefinitionName, priority);
		
		final List<OrchestratorStoreFlexible> captured = captor.getValue();
		assertTrue(captured.get(0).getConsumerSystemName() == null);
		assertTrue(captured.get(0).getProviderSystemName() == null);
		assertEquals(consumerSystemMetadata, captured.get(0).getConsumerSystemMetadata());
		assertEquals(providerSystemMetadata, captured.get(0).getProviderSystemMetadata());
		assertTrue(captured.get(0).getServiceInterfaceName() == null);
		assertEquals(serviceDefinitionName, captured.get(0).getServiceDefinitionName());
		assertEquals(priority.intValue(), captured.get(0).getPriority());
		
		verify(cnVerifier, never()).isValid(any());
		verify(interfaceNameVerifier, never()).isValid(any());
		verify(orchestratorStoreFlexibleRepository, times(1)).flush();
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCreateOrchestratorStoreFlexible_InvalidConsumerDescriptor() {
		final String consumerSystemName = null;
		final String providerSystemName = "providerSystem";
		final String consumerSystemMetadata = null;
		final String providerSystemMetadata = "providerMeta";
		final String serviceMetadata = "serviceMeta";
		final String serviceInterfaceName = null;
		final String serviceDefinitionName = "service";
		final Integer priority = 5;
		
		try {
			dbService.createOrchestratorStoreFlexible(consumerSystemName, providerSystemName, consumerSystemMetadata, providerSystemMetadata, serviceMetadata, serviceInterfaceName, serviceDefinitionName, priority);			
		} catch (final InvalidParameterException ex) {
			assertEquals("consumerSystemName and consumerSystemMetadata are both empty", ex.getMessage());
			verify(cnVerifier, never()).isValid(any());
			verify(interfaceNameVerifier, never()).isValid(any());
			verify(orchestratorStoreFlexibleRepository, never()).saveAll(any());
			verify(orchestratorStoreFlexibleRepository, never()).flush();
			throw ex;
		}		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCreateOrchestratorStoreFlexible_InvalidProviderDescriptor() {
		final String consumerSystemName = "consumer";
		final String providerSystemName = null;
		final String consumerSystemMetadata = "consumerMeta";
		final String providerSystemMetadata = null;
		final String serviceMetadata = "serviceMeta";
		final String serviceInterfaceName = null;
		final String serviceDefinitionName = "service";
		final Integer priority = 5;
		
		try {
			dbService.createOrchestratorStoreFlexible(consumerSystemName, providerSystemName, consumerSystemMetadata, providerSystemMetadata, serviceMetadata, serviceInterfaceName, serviceDefinitionName, priority);			
		} catch (final InvalidParameterException ex) {
			assertEquals("providerSystemName and providerSystemMetadata are both empty", ex.getMessage());
			verify(cnVerifier, times(1)).isValid(eq(consumerSystemName));
			verify(interfaceNameVerifier, never()).isValid(any());
			verify(orchestratorStoreFlexibleRepository, never()).saveAll(any());
			verify(orchestratorStoreFlexibleRepository, never()).flush();
			throw ex;
		}		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCreateOrchestratorStoreFlexible_EmptyServiceDefinition() {
		final String consumerSystemName = "consumerSystem";
		final String providerSystemName = "providerSystem";
		final String consumerSystemMetadata = null;
		final String providerSystemMetadata = null;
		final String serviceMetadata = "serviceMeta";
		final String serviceInterfaceName = "HTTP-SECURE-JSON";
		final String serviceDefinitionName = "";
		final Integer priority = 5;
		
		try {
			dbService.createOrchestratorStoreFlexible(consumerSystemName, providerSystemName, consumerSystemMetadata, providerSystemMetadata, serviceMetadata, serviceInterfaceName, serviceDefinitionName, priority);			
		} catch (final InvalidParameterException ex) {
			assertEquals("serviceDefinitionName is empty", ex.getMessage());
			verify(cnVerifier, times(1)).isValid(eq(consumerSystemName.toLowerCase()));
			verify(cnVerifier, times(1)).isValid(eq(providerSystemName.toLowerCase()));
			verify(interfaceNameVerifier, never()).isValid(any());
			verify(orchestratorStoreFlexibleRepository, never()).saveAll(any());
			verify(orchestratorStoreFlexibleRepository, never()).flush();
			throw ex;
		}		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCreateOrchestratorStoreFlexible_InvalidServiceDefinition() {
		ReflectionTestUtils.setField(dbService, "useStrictServiceDefinitionVerifier", true);
		final String consumerSystemName = "consumerSystem";
		final String providerSystemName = "providerSystem";
		final String consumerSystemMetadata = null;
		final String providerSystemMetadata = null;
		final String serviceMetadata = "serviceMeta";
		final String serviceInterfaceName = "HTTP-SECURE-JSON";
		final String serviceDefinitionName = "-Invalid";
		final Integer priority = 5;
		
		try {
			dbService.createOrchestratorStoreFlexible(consumerSystemName, providerSystemName, consumerSystemMetadata, providerSystemMetadata, serviceMetadata, serviceInterfaceName, serviceDefinitionName, priority);			
		} catch (final InvalidParameterException ex) {
			assertEquals("Service definition has invalid format. Service definition only contains letters (english alphabet), numbers and dash (-), and has to start with a letter (also cannot ends with dash).", ex.getMessage());
			verify(cnVerifier, times(1)).isValid(eq(consumerSystemName.toLowerCase()));
			verify(cnVerifier, times(1)).isValid(eq(providerSystemName.toLowerCase()));
			verify(cnVerifier, times(1)).isValid(eq(serviceDefinitionName.toLowerCase()));
			verify(interfaceNameVerifier, never()).isValid(any());
			verify(orchestratorStoreFlexibleRepository, never()).saveAll(any());
			verify(orchestratorStoreFlexibleRepository, never()).flush();
			throw ex;
		}		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testCreateOrchestratorStoreFlexible_DBException() {
		final String consumerSystemName = "consumerSystem";
		final String providerSystemName = "providerSystem";
		final String consumerSystemMetadata = null;
		final String providerSystemMetadata = null;
		final String serviceMetadata = "serviceMeta";
		final String serviceInterfaceName = "HTTP-SECURE-JSON";
		final String serviceDefinitionName = "service";
		final Integer priority = 5;
		
		when(orchestratorStoreFlexibleRepository.saveAll(any())).thenThrow(new HibernateException("test"));
		
		try {
			dbService.createOrchestratorStoreFlexible(consumerSystemName, providerSystemName, consumerSystemMetadata, providerSystemMetadata, serviceMetadata, serviceInterfaceName, serviceDefinitionName, priority);			
		} catch (final ArrowheadException ex) {
			assertEquals(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG, ex.getMessage());
			verify(cnVerifier, times(1)).isValid(eq(consumerSystemName.toLowerCase()));
			verify(cnVerifier, times(1)).isValid(eq(providerSystemName.toLowerCase()));
			verify(interfaceNameVerifier, times(1)).isValid(any());
			verify(orchestratorStoreFlexibleRepository, times(1)).saveAll(any());
			verify(orchestratorStoreFlexibleRepository, never()).flush();
			throw ex;
		}		
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("unchecked")
	@Test
	public void testCreateOrchestratorStoreFlexible_ListInput() {
		final String consumerSystemName = "consumer";
		final String providerSystemName = "provider";
		final String consumerSystemMetadata = null;
		final String providerSystemMetadata = null;
		final String serviceMetadata = null;
		final String serviceInterfaceName = "HTTP-SECURE-JSON";
		final String serviceDefinitionName = "service";
		final Integer priority = null;
		
		final ArgumentCaptor<List<OrchestratorStoreFlexible>> captor = ArgumentCaptor.forClass(List.class);
		when(orchestratorStoreFlexibleRepository.saveAll(captor.capture())).thenReturn(List.of(new OrchestratorStoreFlexible()));
		doNothing().when(orchestratorStoreFlexibleRepository).flush();
		dbService.createOrchestratorStoreFlexible(List.of(new OrchestratorStoreFlexible(consumerSystemName, providerSystemName, consumerSystemMetadata, providerSystemMetadata, serviceMetadata, serviceInterfaceName, serviceDefinitionName, priority)));
		
		final List<OrchestratorStoreFlexible> captured = captor.getValue();
		assertEquals(consumerSystemName, captured.get(0).getConsumerSystemName());
		assertEquals(providerSystemName, captured.get(0).getProviderSystemName());
		assertTrue(consumerSystemMetadata == null);
		assertTrue(providerSystemMetadata == null);
		assertEquals(serviceInterfaceName, captured.get(0).getServiceInterfaceName());
		assertEquals(serviceDefinitionName, captured.get(0).getServiceDefinitionName());
		assertEquals(Integer.MAX_VALUE, captured.get(0).getPriority());
		
		verify(cnVerifier, times(1)).isValid(eq(consumerSystemName));
		verify(cnVerifier, times(1)).isValid(eq(providerSystemName));
		verify(interfaceNameVerifier, times(1)).isValid(eq(serviceInterfaceName));
		verify(orchestratorStoreFlexibleRepository, times(1)).flush();
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testDeleteOrchestratorStoreFlexibleById_Ok_1() {
		final long id = 5;
		final ArgumentCaptor<Long> captor = ArgumentCaptor.forClass(Long.class);
		when(orchestratorStoreFlexibleRepository.existsById(captor.capture())).thenReturn(true);
		doNothing().when(orchestratorStoreFlexibleRepository).deleteById(captor.capture());
		doNothing().when(orchestratorStoreFlexibleRepository).flush();
		
		dbService.deleteOrchestratorStoreFlexibleById(id);
		
		final List<Long> captured = captor.getAllValues();
		assertEquals(id, captured.get(0).longValue());
		assertEquals(id, captured.get(1).longValue());
		
		verify(orchestratorStoreFlexibleRepository, times(1)).existsById(eq(id));
		verify(orchestratorStoreFlexibleRepository, times(1)).deleteById(eq(id));
		verify(orchestratorStoreFlexibleRepository, times(1)).flush();
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testDeleteOrchestratorStoreFlexibleById_Ok_2() {
		final long id = 5;
		final ArgumentCaptor<Long> captor = ArgumentCaptor.forClass(Long.class);
		when(orchestratorStoreFlexibleRepository.existsById(captor.capture())).thenReturn(false);
		
		dbService.deleteOrchestratorStoreFlexibleById(id);
		
		assertEquals(id, captor.getValue().longValue());
		
		verify(orchestratorStoreFlexibleRepository, times(1)).existsById(eq(id));
		verify(orchestratorStoreFlexibleRepository, never()).deleteById(any());
		verify(orchestratorStoreFlexibleRepository, never()).flush();
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testDeleteOrchestratorStoreFlexibleById_DBException() {
		final long id = 5;
		when(orchestratorStoreFlexibleRepository.existsById(anyLong())).thenThrow(new HibernateException("test"));
		
		try {
			dbService.deleteOrchestratorStoreFlexibleById(id);			
		} catch (final ArrowheadException ex) {
			assertEquals(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG, ex.getMessage());
			verify(orchestratorStoreFlexibleRepository, times(1)).existsById(eq(id));
			verify(orchestratorStoreFlexibleRepository, never()).deleteById(any());
			verify(orchestratorStoreFlexibleRepository, never()).flush();
			throw ex;
		}		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testDeleteAllOrchestratorStoreFlexible_Ok() {
		doNothing().when(orchestratorStoreFlexibleRepository).deleteAll();
		doNothing().when(orchestratorStoreFlexibleRepository).flush();
		
		dbService.deleteAllOrchestratorStoreFlexible();
		
		verify(orchestratorStoreFlexibleRepository, times(1)).deleteAll();
		verify(orchestratorStoreFlexibleRepository, times(1)).flush();
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testDeleteAllOrchestratorStoreFlexible_DBException() {
		doThrow(new HibernateException("test")).when(orchestratorStoreFlexibleRepository).deleteAll();
		
		try {
			dbService.deleteAllOrchestratorStoreFlexible();
			
		} catch (final ArrowheadException ex) {
			assertEquals(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG, ex.getMessage());
			verify(orchestratorStoreFlexibleRepository, times(1)).deleteAll();
			verify(orchestratorStoreFlexibleRepository, never()).flush();
			throw ex;
		}		
	}
}