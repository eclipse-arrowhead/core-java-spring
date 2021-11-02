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

package eu.arrowhead.core.choreographer.executor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;

import eu.arrowhead.common.database.entity.ChoreographerExecutor;
import eu.arrowhead.common.database.entity.ChoreographerSessionStep;
import eu.arrowhead.common.dto.shared.ChoreographerExecutorServiceInfoResponseDTO;
import eu.arrowhead.common.dto.shared.ChoreographerServiceQueryFormDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryFormListDTO;
import eu.arrowhead.common.dto.shared.SystemResponseDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.core.choreographer.database.service.ChoreographerExecutorDBService;
import eu.arrowhead.core.choreographer.database.service.ChoreographerSessionDBService;
import eu.arrowhead.core.choreographer.service.ChoreographerDriver;

@RunWith(SpringRunner.class)
public class ExecutorSelectorTest {

	//=================================================================================================
	// members
	
	@InjectMocks
	private ExecutorSelector testingObject;
	
	@Mock
	private ChoreographerExecutorDBService executorDBService;
	
	@Mock
	private ChoreographerSessionDBService sessionDBService;
	
	@Mock
	private ChoreographerDriver driver;
	
	@Mock
	private ExecutorPrioritizationStrategy prioritizationStrategy;
	
	@Mock
	private ExecutorMeasurementStrategy measurementStrategy;
	
	//=================================================================================================
	// methods
	
	// we only test the 9 parameters version of selectAndInit because the other two public methods are just specialization of this
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSelectAndInitServiceDefinitionNull() {
		try {
			testingObject.selectAndInit(null, null, null, null, null, null, false, false, false);
		} catch (final Exception ex) {
			Assert.assertEquals("serviceDefinition is empty", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSelectAndInitServiceDefinitionEmpty() {
		try {
			testingObject.selectAndInit(null, null, " ", null, null, null, false, false, false);
		} catch (final Exception ex) {
			Assert.assertEquals("serviceDefinition is empty", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSelectAndInitSessionIdNull() {
		try {
			testingObject.selectAndInit(null, null, "service", null, null, null, false, false, true);
		} catch (final Exception ex) {
			Assert.assertEquals("Invalid session id", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSelectAndInitSessionIdInvalid() {
		try {
			testingObject.selectAndInit(-1L, null, "service", null, null, null, false, false, true);
		} catch (final Exception ex) {
			Assert.assertEquals("Invalid session id", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSelectAndInitStepIdNull() {
		try {
			testingObject.selectAndInit(1L, null, "service", null, null, null, false, false, true);
		} catch (final Exception ex) {
			Assert.assertEquals("Invalid step id", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testSelectAndInitStepIdInvalid() {
		try {
			testingObject.selectAndInit(1L, -2L, "service", null, null, null, false, false, true);
		} catch (final Exception ex) {
			Assert.assertEquals("Invalid step id", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testSelectAndInitNoPotentialExecutors() {
		when(executorDBService.getExecutorsByServiceDefinitionAndVersion("service", 1, 10)).thenReturn(List.of());
		
		final ExecutorData executorData = testingObject.selectAndInit(1L, 2L, "service", 1, 10, null, false, false, true);
		
		Assert.assertNull(executorData);
		
		verify(executorDBService, times(1)).getExecutorsByServiceDefinitionAndVersion("service", 1, 10);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testSelectAndInitLockedExecutor() {
		final ChoreographerExecutor executor = new ChoreographerExecutor();
		executor.setLocked(true);
		
		when(executorDBService.getExecutorsByServiceDefinitionAndVersion("service", 1, 10)).thenReturn(List.of(executor));
		
		final ExecutorData executorData = testingObject.selectAndInit(1L, 2L, "service", 1, 10, null, false, false, true);
		
		Assert.assertNull(executorData);
		
		verify(executorDBService, times(1)).getExecutorsByServiceDefinitionAndVersion("service", 1, 10);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testSelectAndInitExcludedExecutor() {
		final ChoreographerExecutor executor = new ChoreographerExecutor();
		executor.setId(10);
		
		when(executorDBService.getExecutorsByServiceDefinitionAndVersion("service", 1, 10)).thenReturn(List.of(executor));
		
		final ExecutorData executorData = testingObject.selectAndInit(1L, 2L, "service", 1, 10, Set.of(10L), false, false, true);
		
		Assert.assertNull(executorData);
		
		verify(executorDBService, times(1)).getExecutorsByServiceDefinitionAndVersion("service", 1, 10);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testSelectAndInitServiceInfoNotAvailable() {
		final ChoreographerExecutor executor = new ChoreographerExecutor();
		executor.setId(10);
		executor.setName("executor");
		executor.setAddress("localhost");
		executor.setPort(12121);
		executor.setBaseUri("/abc");
		
		final List<ChoreographerExecutor> potentials = List.of(executor);
		
		when(executorDBService.getExecutorsByServiceDefinitionAndVersion("service", 1, 10)).thenReturn(potentials);
		when(driver.queryExecutorServiceInfo("localhost", 12121, "/abc", "service", 1, 10)).thenThrow(new ArrowheadException("connection error"));
		when(prioritizationStrategy.prioritize(anyList(), anyMap())).thenReturn(potentials);
		
		final ExecutorData executorData = testingObject.selectAndInit(1L, 2L, "service", 1, 10, Set.of(), false, false, true);
		
		Assert.assertNull(executorData);
		
		verify(executorDBService, times(1)).getExecutorsByServiceDefinitionAndVersion("service", 1, 10);
		verify(driver, times(1)).queryExecutorServiceInfo("localhost", 12121, "/abc", "service", 1, 10);
		verify(prioritizationStrategy, times(1)).prioritize(anyList(), anyMap());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testSelectAndInitExecutorNotFoundInDB() {
		final ChoreographerExecutor executor = new ChoreographerExecutor();
		executor.setId(10);
		executor.setName("executor");
		executor.setAddress("localhost");
		executor.setPort(12121);
		executor.setBaseUri("/abc");
		
		final List<ChoreographerExecutor> potentials = List.of(executor);
		
		when(executorDBService.getExecutorsByServiceDefinitionAndVersion("service", 1, 10)).thenReturn(potentials);
		when(driver.queryExecutorServiceInfo("localhost", 12121, "/abc", "service", 1, 10)).thenReturn(new ChoreographerExecutorServiceInfoResponseDTO("service", 1, 10, List.of()));
		when(prioritizationStrategy.prioritize(anyList(), anyMap())).thenReturn(potentials);
		when(executorDBService.getExecutorOptionalById(10)).thenReturn(Optional.empty());
		
		final ExecutorData executorData = testingObject.selectAndInit(1L, 2L, "service", 1, 10, Set.of(), false, false, true);
		
		Assert.assertNull(executorData);
		
		verify(executorDBService, times(1)).getExecutorsByServiceDefinitionAndVersion("service", 1, 10);
		verify(driver, times(1)).queryExecutorServiceInfo("localhost", 12121, "/abc", "service", 1, 10);
		verify(prioritizationStrategy, times(1)).prioritize(anyList(), anyMap());
		verify(executorDBService, times(1)).getExecutorOptionalById(10);
		verify(driver, never()).queryServiceRegistryBySystem(anyString(), anyString(), anyInt());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testSelectAndInitExecutorLocked() {
		final ChoreographerExecutor executor = new ChoreographerExecutor();
		executor.setId(10);
		executor.setName("executor");
		executor.setAddress("localhost");
		executor.setPort(12121);
		executor.setBaseUri("/abc");
		
		final ChoreographerExecutor executorNew = new ChoreographerExecutor();
		executorNew.setId(10);
		executorNew.setLocked(true);
		
		final List<ChoreographerExecutor> potentials = List.of(executor);
		
		when(executorDBService.getExecutorsByServiceDefinitionAndVersion("service", 1, 10)).thenReturn(potentials);
		when(driver.queryExecutorServiceInfo("localhost", 12121, "/abc", "service", 1, 10)).thenReturn(new ChoreographerExecutorServiceInfoResponseDTO("service", 1, 10, List.of()));
		when(prioritizationStrategy.prioritize(anyList(), anyMap())).thenReturn(potentials);
		when(executorDBService.getExecutorOptionalById(10)).thenReturn(Optional.of(executorNew));
		
		final ExecutorData executorData = testingObject.selectAndInit(1L, 2L, "service", 1, 10, Set.of(), false, false, true);
		
		Assert.assertNull(executorData);
		
		verify(executorDBService, times(1)).getExecutorsByServiceDefinitionAndVersion("service", 1, 10);
		verify(driver, times(1)).queryExecutorServiceInfo("localhost", 12121, "/abc", "service", 1, 10);
		verify(prioritizationStrategy, times(1)).prioritize(anyList(), anyMap());
		verify(executorDBService, times(1)).getExecutorOptionalById(10);
		verify(driver, never()).queryServiceRegistryBySystem(anyString(), anyString(), anyInt());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testSelectAndInitQuerySystemProblem() {
		final ChoreographerExecutor executor = new ChoreographerExecutor();
		executor.setId(10);
		executor.setName("executor");
		executor.setAddress("localhost");
		executor.setPort(12121);
		executor.setBaseUri("/abc");
		
		final List<ChoreographerExecutor> potentials = List.of(executor);
		
		when(executorDBService.getExecutorsByServiceDefinitionAndVersion("service", 1, 10)).thenReturn(potentials);
		when(driver.queryExecutorServiceInfo("localhost", 12121, "/abc", "service", 1, 10)).thenReturn(new ChoreographerExecutorServiceInfoResponseDTO("service", 1, 10, List.of()));
		when(prioritizationStrategy.prioritize(anyList(), anyMap())).thenReturn(potentials);
		when(executorDBService.getExecutorOptionalById(10)).thenReturn(Optional.of(executor));
		when(driver.queryServiceRegistryBySystem("executor", "localhost", 12121)).thenThrow(new ArrowheadException("not found"));
		
		try {
			testingObject.selectAndInit(1L, 2L, "service", 1, 10, Set.of(), false, false, true);
		} catch (final Exception ex) {
			Assert.assertEquals("not found", ex.getMessage());
			
			verify(executorDBService, times(1)).getExecutorsByServiceDefinitionAndVersion("service", 1, 10);
			verify(driver, times(1)).queryExecutorServiceInfo("localhost", 12121, "/abc", "service", 1, 10);
			verify(prioritizationStrategy, times(1)).prioritize(anyList(), anyMap());
			verify(executorDBService, times(1)).getExecutorOptionalById(10);
			verify(driver, times(1)).queryServiceRegistryBySystem("executor", "localhost", 12121);
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testSelectAndInitSearchDependenciesProblem() {
		final ChoreographerExecutor executor = new ChoreographerExecutor();
		executor.setId(10);
		executor.setName("executor");
		executor.setAddress("localhost");
		executor.setPort(12121);
		executor.setBaseUri("/abc");
		
		final List<ChoreographerExecutor> potentials = List.of(executor);
		
		when(executorDBService.getExecutorsByServiceDefinitionAndVersion("service", 1, 10)).thenReturn(potentials);
		when(driver.queryExecutorServiceInfo("localhost", 12121, "/abc", "service", 1, 10)).thenReturn(new ChoreographerExecutorServiceInfoResponseDTO("service", 1, 10, List.of(new ChoreographerServiceQueryFormDTO())));
		when(prioritizationStrategy.prioritize(anyList(), anyMap())).thenReturn(potentials);
		when(executorDBService.getExecutorOptionalById(10)).thenReturn(Optional.of(executor));
		when(driver.queryServiceRegistryBySystem("executor", "localhost", 12121)).thenReturn(new SystemResponseDTO());
		when(driver.searchForServices(any(ServiceQueryFormListDTO.class), eq(false))).thenThrow(new ArrowheadException("connection problem"));
		
		final ExecutorData executorData = testingObject.selectAndInit(1L, 2L, "service", 1, 10, Set.of(), false, false, true);
		
		Assert.assertNull(executorData);
			
		verify(executorDBService, times(1)).getExecutorsByServiceDefinitionAndVersion("service", 1, 10);
		verify(driver, times(1)).queryExecutorServiceInfo("localhost", 12121, "/abc", "service", 1, 10);
		verify(prioritizationStrategy, times(1)).prioritize(anyList(), anyMap());
		verify(executorDBService, times(1)).getExecutorOptionalById(10);
		verify(driver, times(1)).queryServiceRegistryBySystem("executor", "localhost", 12121);
		verify(driver, times(1)).searchForServices(any(ServiceQueryFormListDTO.class), eq(false));
		verify(sessionDBService, never()).registerSessionStep(anyLong(), anyLong(), anyLong());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testSelectAndInitSearchDependenciesCloudListEmpty() {
		final ChoreographerExecutor executor = new ChoreographerExecutor();
		executor.setId(10);
		executor.setName("executor");
		executor.setAddress("localhost");
		executor.setPort(12121);
		executor.setBaseUri("/abc");
		
		final List<ChoreographerExecutor> potentials = List.of(executor);
		
		when(executorDBService.getExecutorsByServiceDefinitionAndVersion("service", 1, 10)).thenReturn(potentials);
		when(driver.queryExecutorServiceInfo("localhost", 12121, "/abc", "service", 1, 10)).thenReturn(new ChoreographerExecutorServiceInfoResponseDTO("service", 1, 10, List.of(new ChoreographerServiceQueryFormDTO())));
		when(prioritizationStrategy.prioritize(anyList(), anyMap())).thenReturn(potentials);
		when(executorDBService.getExecutorOptionalById(10)).thenReturn(Optional.of(executor));
		when(driver.queryServiceRegistryBySystem("executor", "localhost", 12121)).thenReturn(new SystemResponseDTO());
		when(driver.searchForServices(any(ServiceQueryFormListDTO.class), eq(false))).thenReturn(Map.of(1, List.of()));
		
		final ExecutorData executorData = testingObject.selectAndInit(1L, 2L, "service", 1, 10, Set.of(), false, false, true);
		
		Assert.assertNull(executorData);
			
		verify(executorDBService, times(1)).getExecutorsByServiceDefinitionAndVersion("service", 1, 10);
		verify(driver, times(1)).queryExecutorServiceInfo("localhost", 12121, "/abc", "service", 1, 10);
		verify(prioritizationStrategy, times(1)).prioritize(anyList(), anyMap());
		verify(executorDBService, times(1)).getExecutorOptionalById(10);
		verify(driver, times(1)).queryServiceRegistryBySystem("executor", "localhost", 12121);
		verify(driver, times(1)).searchForServices(any(ServiceQueryFormListDTO.class), eq(false));
		verify(sessionDBService, never()).registerSessionStep(anyLong(), anyLong(), anyLong());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testSelectAndInitOkNoChooseOptimizeWithInit() {
		final ChoreographerExecutor executor = new ChoreographerExecutor();
		executor.setId(10);
		executor.setName("executor");
		executor.setAddress("localhost");
		executor.setPort(12121);
		executor.setBaseUri("/abc");
		
		final List<ChoreographerExecutor> potentials = List.of(executor);
		final SystemResponseDTO systemResponseDTO = new SystemResponseDTO();
		systemResponseDTO.setSystemName("executor");
		
		when(executorDBService.getExecutorsByServiceDefinitionAndVersion("service", 1, 10)).thenReturn(potentials);
		when(driver.queryExecutorServiceInfo("localhost", 12121, "/abc", "service", 1, 10)).thenReturn(new ChoreographerExecutorServiceInfoResponseDTO("service", 1, 10, List.of(new ChoreographerServiceQueryFormDTO())));
		when(prioritizationStrategy.prioritize(anyList(), anyMap())).thenReturn(potentials);
		when(executorDBService.getExecutorOptionalById(10)).thenReturn(Optional.of(executor));
		when(driver.queryServiceRegistryBySystem("executor", "localhost", 12121)).thenReturn(systemResponseDTO);
		when(driver.searchForServices(any(ServiceQueryFormListDTO.class), eq(false))).thenReturn(Map.of(1, List.of("#OWN_CLOUD#")));
		when(sessionDBService.registerSessionStep(1, 2, 10)).thenReturn(new ChoreographerSessionStep());
		
		final ExecutorData executorData = testingObject.selectAndInit(1L, 2L, "service", 1, 10, Set.of(), false, false, true);
		
		Assert.assertNotNull(executorData);
		Assert.assertFalse(executorData.getUseOtherClouds());
		Assert.assertEquals(10, executorData.getExecutor().getId());
		Assert.assertEquals("executor", executorData.getExecutorSystem().getSystemName());
			
		verify(executorDBService, times(1)).getExecutorsByServiceDefinitionAndVersion("service", 1, 10);
		verify(driver, times(1)).queryExecutorServiceInfo("localhost", 12121, "/abc", "service", 1, 10);
		verify(prioritizationStrategy, times(1)).prioritize(anyList(), anyMap());
		verify(executorDBService, times(1)).getExecutorOptionalById(10);
		verify(driver, times(1)).queryServiceRegistryBySystem("executor", "localhost", 12121);
		verify(driver, times(1)).searchForServices(any(ServiceQueryFormListDTO.class), eq(false));
		verify(sessionDBService, times(1)).registerSessionStep(1, 2, 10);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testSelectAndInitOkNoChooseOptimizeWithoutInit() {
		final ChoreographerExecutor executor = new ChoreographerExecutor();
		executor.setId(10);
		executor.setName("executor");
		executor.setAddress("localhost");
		executor.setPort(12121);
		executor.setBaseUri("/abc");
		
		final List<ChoreographerExecutor> potentials = List.of(executor);
		final SystemResponseDTO systemResponseDTO = new SystemResponseDTO();
		systemResponseDTO.setSystemName("executor");
		
		when(executorDBService.getExecutorsByServiceDefinitionAndVersion("service", 1, 10)).thenReturn(potentials);
		when(driver.queryExecutorServiceInfo("localhost", 12121, "/abc", "service", 1, 10)).thenReturn(new ChoreographerExecutorServiceInfoResponseDTO("service", 1, 10, List.of(new ChoreographerServiceQueryFormDTO())));
		when(prioritizationStrategy.prioritize(anyList(), anyMap())).thenReturn(potentials);
		when(executorDBService.getExecutorOptionalById(10)).thenReturn(Optional.of(executor));
		when(driver.queryServiceRegistryBySystem("executor", "localhost", 12121)).thenReturn(systemResponseDTO);
		when(driver.searchForServices(any(ServiceQueryFormListDTO.class), eq(false))).thenReturn(Map.of(1, List.of("#OWN_CLOUD#")));
		
		final ExecutorData executorData = testingObject.selectAndInit(1L, 2L, "service", 1, 10, Set.of(), false, false, false);
		
		Assert.assertNotNull(executorData);
		Assert.assertFalse(executorData.getUseOtherClouds());
		Assert.assertEquals(10, executorData.getExecutor().getId());
		Assert.assertEquals("executor", executorData.getExecutorSystem().getSystemName());
			
		verify(executorDBService, times(1)).getExecutorsByServiceDefinitionAndVersion("service", 1, 10);
		verify(driver, times(1)).queryExecutorServiceInfo("localhost", 12121, "/abc", "service", 1, 10);
		verify(prioritizationStrategy, times(1)).prioritize(anyList(), anyMap());
		verify(executorDBService, times(1)).getExecutorOptionalById(10);
		verify(driver, times(1)).queryServiceRegistryBySystem("executor", "localhost", 12121);
		verify(driver, times(1)).searchForServices(any(ServiceQueryFormListDTO.class), eq(false));
		verify(sessionDBService, never()).registerSessionStep(anyLong(), anyLong(), anyLong());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testSelectAndInitOkChooseOptimizeNoDependencies() {
		final ChoreographerExecutor executor = new ChoreographerExecutor();
		executor.setId(10);
		executor.setName("executor");
		executor.setAddress("localhost");
		executor.setPort(12121);
		executor.setBaseUri("/abc");
		
		final List<ChoreographerExecutor> potentials = List.of(executor);
		final SystemResponseDTO systemResponseDTO = new SystemResponseDTO();
		systemResponseDTO.setSystemName("executor");
		
		when(executorDBService.getExecutorsByServiceDefinitionAndVersion("service", 1, 10)).thenReturn(potentials);
		when(driver.queryExecutorServiceInfo("localhost", 12121, "/abc", "service", 1, 10)).thenReturn(new ChoreographerExecutorServiceInfoResponseDTO("service", 1, 10, List.of()));
		when(prioritizationStrategy.prioritize(anyList(), anyMap())).thenReturn(potentials);
		when(executorDBService.getExecutorOptionalById(10)).thenReturn(Optional.of(executor));
		when(driver.queryServiceRegistryBySystem("executor", "localhost", 12121)).thenReturn(systemResponseDTO);
		
		final ExecutorData executorData = testingObject.selectAndInit(1L, 2L, "service", 1, 10, Set.of(), true, true, false);
		
		Assert.assertNotNull(executorData);
		Assert.assertFalse(executorData.getUseOtherClouds());
		Assert.assertEquals(10, executorData.getExecutor().getId());
		Assert.assertEquals("executor", executorData.getExecutorSystem().getSystemName());
			
		verify(executorDBService, times(1)).getExecutorsByServiceDefinitionAndVersion("service", 1, 10);
		verify(driver, times(1)).queryExecutorServiceInfo("localhost", 12121, "/abc", "service", 1, 10);
		verify(prioritizationStrategy, times(1)).prioritize(anyList(), anyMap());
		verify(executorDBService, times(1)).getExecutorOptionalById(10);
		verify(driver, times(1)).queryServiceRegistryBySystem("executor", "localhost", 12121);
		verify(driver, never()).searchForServices(any(ServiceQueryFormListDTO.class), eq(true));
		verify(sessionDBService, never()).registerSessionStep(anyLong(), anyLong(), anyLong());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testSelectAndInitOkChooseOptimizeLocalResult() {
		final ChoreographerExecutor executor = new ChoreographerExecutor();
		executor.setId(10);
		executor.setName("executor");
		executor.setAddress("localhost");
		executor.setPort(12121);
		executor.setBaseUri("/abc");
		
		final List<ChoreographerExecutor> potentials = List.of(executor);
		final SystemResponseDTO systemResponseDTO = new SystemResponseDTO();
		systemResponseDTO.setSystemName("executor");
		
		when(executorDBService.getExecutorsByServiceDefinitionAndVersion("service", 1, 10)).thenReturn(potentials);
		when(driver.queryExecutorServiceInfo("localhost", 12121, "/abc", "service", 1, 10)).thenReturn(new ChoreographerExecutorServiceInfoResponseDTO("service", 1, 10, List.of(new ChoreographerServiceQueryFormDTO())));
		when(prioritizationStrategy.prioritize(anyList(), anyMap())).thenReturn(potentials);
		when(executorDBService.getExecutorOptionalById(10)).thenReturn(Optional.of(executor));
		when(driver.queryServiceRegistryBySystem("executor", "localhost", 12121)).thenReturn(systemResponseDTO);
		when(driver.searchForServices(any(ServiceQueryFormListDTO.class), eq(true))).thenReturn(Map.of(1, List.of("#OWN_CLOUD#")));
		
		final ExecutorData executorData = testingObject.selectAndInit(1L, 2L, "service", 1, 10, Set.of(), true, true, false);
		
		Assert.assertNotNull(executorData);
		Assert.assertFalse(executorData.getUseOtherClouds());
		Assert.assertEquals(10, executorData.getExecutor().getId());
		Assert.assertEquals("executor", executorData.getExecutorSystem().getSystemName());
			
		verify(executorDBService, times(1)).getExecutorsByServiceDefinitionAndVersion("service", 1, 10);
		verify(driver, times(1)).queryExecutorServiceInfo("localhost", 12121, "/abc", "service", 1, 10);
		verify(prioritizationStrategy, times(1)).prioritize(anyList(), anyMap());
		verify(executorDBService, times(1)).getExecutorOptionalById(10);
		verify(driver, times(1)).queryServiceRegistryBySystem("executor", "localhost", 12121);
		verify(driver, times(1)).searchForServices(any(ServiceQueryFormListDTO.class), eq(true));
		verify(sessionDBService, never()).registerSessionStep(anyLong(), anyLong(), anyLong());
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("unchecked")
	@Test
	public void testSelectAndInitOkChooseOptimizeWithInit() {
		final ChoreographerExecutor executor = new ChoreographerExecutor();
		executor.setId(10);
		executor.setName("executor");
		executor.setAddress("localhost");
		executor.setPort(12121);
		executor.setBaseUri("/abc");
		
		final ChoreographerExecutor executor2 = new ChoreographerExecutor();
		executor2.setId(11);
		executor2.setName("executor2");
		executor2.setAddress("localhost");
		executor2.setPort(22121);
		executor2.setBaseUri("/def");
		
		final List<ChoreographerExecutor> potentials = List.of(executor, executor2);
		final SystemResponseDTO systemResponseDTO = new SystemResponseDTO();
		systemResponseDTO.setSystemName("executor");
		final SystemResponseDTO systemResponseDTO2 = new SystemResponseDTO();
		systemResponseDTO2.setSystemName("executor2");
		
		when(executorDBService.getExecutorsByServiceDefinitionAndVersion("service", 1, 10)).thenReturn(potentials);
		when(driver.queryExecutorServiceInfo("localhost", 12121, "/abc", "service", 1, 10)).thenReturn(new ChoreographerExecutorServiceInfoResponseDTO("service", 1, 10, List.of(new ChoreographerServiceQueryFormDTO(), new ChoreographerServiceQueryFormDTO())));
		when(driver.queryExecutorServiceInfo("localhost", 22121, "/def", "service", 1, 10)).thenReturn(new ChoreographerExecutorServiceInfoResponseDTO("service", 1, 10, List.of(new ChoreographerServiceQueryFormDTO(), new ChoreographerServiceQueryFormDTO())));
		when(prioritizationStrategy.prioritize(anyList(), anyMap())).thenReturn(potentials);
		when(executorDBService.getExecutorOptionalById(10)).thenReturn(Optional.of(executor));
		when(executorDBService.getExecutorOptionalById(11)).thenReturn(Optional.of(executor2));
		when(driver.queryServiceRegistryBySystem("executor", "localhost", 12121)).thenReturn(systemResponseDTO);
		when(driver.queryServiceRegistryBySystem("executor2", "localhost", 22121)).thenReturn(systemResponseDTO2);
		when(driver.searchForServices(any(ServiceQueryFormListDTO.class), eq(true))).thenReturn(Map.of(1, List.of("op/cloud1"), 2, List.of("op/cloud2")),
																								Map.of(1, List.of("#OWN_CLOUD#"), 2, List.of("op/cloud1")));
		when(measurementStrategy.getMeasurement(anyMap())).thenReturn(42, 22);
		when(sessionDBService.registerSessionStep(1, 2, 11)).thenReturn(new ChoreographerSessionStep());
		
		final ExecutorData executorData = testingObject.selectAndInit(1L, 2L, "service", 1, 10, Set.of(), true, true, true);
		
		Assert.assertNotNull(executorData);
		Assert.assertTrue(executorData.getUseOtherClouds());
		Assert.assertEquals(11, executorData.getExecutor().getId());
		Assert.assertEquals("executor2", executorData.getExecutorSystem().getSystemName());
			
		verify(executorDBService, times(1)).getExecutorsByServiceDefinitionAndVersion("service", 1, 10);
		verify(driver, times(1)).queryExecutorServiceInfo("localhost", 12121, "/abc", "service", 1, 10);
		verify(driver, times(1)).queryExecutorServiceInfo("localhost", 22121, "/def", "service", 1, 10);
		verify(prioritizationStrategy, times(1)).prioritize(anyList(), anyMap());
		verify(executorDBService, times(1)).getExecutorOptionalById(10);
		verify(executorDBService, times(1)).getExecutorOptionalById(11);
		verify(driver, times(1)).queryServiceRegistryBySystem("executor", "localhost", 12121);
		verify(driver, times(1)).queryServiceRegistryBySystem("executor2", "localhost", 22121);
		verify(driver, times(2)).searchForServices(any(ServiceQueryFormListDTO.class), eq(true));
		verify(measurementStrategy, times(2)).getMeasurement(anyMap());
		verify(sessionDBService, times(1)).registerSessionStep(1, 2, 11);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testSelectAndInitOkChooseOptimizeWithoutInit() {
		final ChoreographerExecutor executor = new ChoreographerExecutor();
		executor.setId(10);
		executor.setName("executor");
		executor.setAddress("localhost");
		executor.setPort(12121);
		executor.setBaseUri("/abc");
		
		final ChoreographerExecutor executor2 = new ChoreographerExecutor();
		executor2.setId(11);
		executor2.setName("executor2");
		executor2.setAddress("localhost");
		executor2.setPort(22121);
		executor2.setBaseUri("/def");
		
		final List<ChoreographerExecutor> potentials = List.of(executor, executor2);
		final SystemResponseDTO systemResponseDTO = new SystemResponseDTO();
		systemResponseDTO.setSystemName("executor");
		final SystemResponseDTO systemResponseDTO2 = new SystemResponseDTO();
		systemResponseDTO2.setSystemName("executor2");
		
		when(executorDBService.getExecutorsByServiceDefinitionAndVersion("service", 1, 10)).thenReturn(potentials);
		when(driver.queryExecutorServiceInfo("localhost", 12121, "/abc", "service", 1, 10)).thenReturn(new ChoreographerExecutorServiceInfoResponseDTO("service", 1, 10, List.of(new ChoreographerServiceQueryFormDTO(), new ChoreographerServiceQueryFormDTO())));
		when(driver.queryExecutorServiceInfo("localhost", 22121, "/def", "service", 1, 10)).thenReturn(new ChoreographerExecutorServiceInfoResponseDTO("service", 1, 10, List.of()));
		when(prioritizationStrategy.prioritize(anyList(), anyMap())).thenReturn(potentials);
		when(executorDBService.getExecutorOptionalById(10)).thenReturn(Optional.of(executor));
		when(executorDBService.getExecutorOptionalById(11)).thenReturn(Optional.of(executor2));
		when(driver.queryServiceRegistryBySystem("executor", "localhost", 12121)).thenReturn(systemResponseDTO);
		when(driver.queryServiceRegistryBySystem("executor2", "localhost", 22121)).thenReturn(systemResponseDTO2);
		when(driver.searchForServices(any(ServiceQueryFormListDTO.class), eq(true))).thenReturn(Map.of(1, List.of("op/cloud1"), 2, List.of("op/cloud2")));
		when(measurementStrategy.getMeasurement(anyMap())).thenReturn(42);
		
		final ExecutorData executorData = testingObject.selectAndInit(1L, 2L, "service", 1, 10, Set.of(), true, true, false);
		
		Assert.assertNotNull(executorData);
		Assert.assertFalse(executorData.getUseOtherClouds());
		Assert.assertEquals(11, executorData.getExecutor().getId());
		Assert.assertEquals("executor2", executorData.getExecutorSystem().getSystemName());
			
		verify(executorDBService, times(1)).getExecutorsByServiceDefinitionAndVersion("service", 1, 10);
		verify(driver, times(1)).queryExecutorServiceInfo("localhost", 12121, "/abc", "service", 1, 10);
		verify(driver, times(1)).queryExecutorServiceInfo("localhost", 22121, "/def", "service", 1, 10);
		verify(prioritizationStrategy, times(1)).prioritize(anyList(), anyMap());
		verify(executorDBService, times(1)).getExecutorOptionalById(10);
		verify(executorDBService, times(1)).getExecutorOptionalById(11);
		verify(driver, times(1)).queryServiceRegistryBySystem("executor", "localhost", 12121);
		verify(driver, times(1)).queryServiceRegistryBySystem("executor2", "localhost", 22121);
		verify(driver, times(1)).searchForServices(any(ServiceQueryFormListDTO.class), eq(true));
		verify(measurementStrategy, times(1)).getMeasurement(anyMap());
		verify(sessionDBService, never()).registerSessionStep(anyLong(), anyLong(), anyLong());
	}
}