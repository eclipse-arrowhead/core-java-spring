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

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;

import eu.arrowhead.common.database.entity.OrchestratorStoreFlexible;
import eu.arrowhead.common.database.repository.OrchestratorStoreFlexibleRepository;
import eu.arrowhead.common.exception.InvalidParameterException;

@RunWith(SpringRunner.class)
public class OrchestratorStoreFlexibleDBServiceTest {

	//=================================================================================================
	// members
	
	@InjectMocks
	private OrchestratorStoreFlexibleDBService orchestratorStoreFlexibleDBService; 
	
	@Mock
	private OrchestratorStoreFlexibleRepository orchestratorStoreFlexibleRepository;
		
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGetMatchedRulesByServiceDefinitionAndConsumerNameServiceDefinitionNull() {
		try {
			orchestratorStoreFlexibleDBService.getMatchedRulesByServiceDefinitionAndConsumerName(null, null);
		} catch (final InvalidParameterException ex) {
			Assert.assertEquals("Service definition is empty", ex.getMessage());
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGetMatchedRulesByServiceDefinitionAndConsumerNameServiceDefinitionEmpty() {
		try {
			orchestratorStoreFlexibleDBService.getMatchedRulesByServiceDefinitionAndConsumerName(" ", null);
		} catch (final InvalidParameterException ex) {
			Assert.assertEquals("Service definition is empty", ex.getMessage());
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGetMatchedRulesByServiceDefinitionAndConsumerNameConsumerNameNull() {
		try {
			orchestratorStoreFlexibleDBService.getMatchedRulesByServiceDefinitionAndConsumerName("testService", null);
		} catch (final InvalidParameterException ex) {
			Assert.assertEquals("Consumer system name is empty", ex.getMessage());
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGetMatchedRulesByServiceDefinitionAndConsumerNameConsumerNameEmpty() {
		try {
			orchestratorStoreFlexibleDBService.getMatchedRulesByServiceDefinitionAndConsumerName("testService", "");
		} catch (final InvalidParameterException ex) {
			Assert.assertEquals("Consumer system name is empty", ex.getMessage());
			throw ex;
		}
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetMatchedRulesByServiceDefinitionAndConsumerNameOk() {
		when(orchestratorStoreFlexibleRepository.findByServiceDefinitionNameAndConsumerSystemNameAndConsumerSystemMetadataIsNull(anyString(), anyString())).thenReturn(List.of(new OrchestratorStoreFlexible()));
		
		orchestratorStoreFlexibleDBService.getMatchedRulesByServiceDefinitionAndConsumerName("testService", "consumer");
		
		verify(orchestratorStoreFlexibleRepository).findByServiceDefinitionNameAndConsumerSystemNameAndConsumerSystemMetadataIsNull("testservice", "consumer");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGetMatchedRulesByServiceDefinitionAndNonNullConsumerMetadataServiceDefinitionNull() {
		try {
			orchestratorStoreFlexibleDBService.getMatchedRulesByServiceDefinitionAndNonNullConsumerMetadata(null);
		} catch (final InvalidParameterException ex) {
			Assert.assertEquals("Service definition is empty", ex.getMessage());
			throw ex;
		}		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGetMatchedRulesByServiceDefinitionAndNonNullConsumerMetadataServiceDefinitionEmpty() {
		try {
			orchestratorStoreFlexibleDBService.getMatchedRulesByServiceDefinitionAndNonNullConsumerMetadata("");
		} catch (final InvalidParameterException ex) {
			Assert.assertEquals("Service definition is empty", ex.getMessage());
			throw ex;
		}		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetMatchedRulesByServiceDefinitionAndNonNullConsumerMetadataOk() {
		when(orchestratorStoreFlexibleRepository.findByServiceDefinitionNameAndConsumerSystemMetadataIsNotNull(anyString())).thenReturn(List.of(new OrchestratorStoreFlexible()));
		
		orchestratorStoreFlexibleDBService.getMatchedRulesByServiceDefinitionAndNonNullConsumerMetadata("testService");
		
		verify(orchestratorStoreFlexibleRepository).findByServiceDefinitionNameAndConsumerSystemMetadataIsNotNull("testservice");
	}
}