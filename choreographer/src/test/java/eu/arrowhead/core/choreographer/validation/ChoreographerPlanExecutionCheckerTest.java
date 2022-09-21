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

package eu.arrowhead.core.choreographer.validation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.entity.ChoreographerAction;
import eu.arrowhead.common.database.entity.ChoreographerExecutor;
import eu.arrowhead.common.database.entity.ChoreographerPlan;
import eu.arrowhead.common.database.entity.ChoreographerStep;
import eu.arrowhead.common.dto.shared.ChoreographerRunPlanRequestDTO;
import eu.arrowhead.common.dto.shared.ChoreographerRunPlanResponseDTO;
import eu.arrowhead.common.dto.shared.ChoreographerSessionStatus;
import eu.arrowhead.common.dto.shared.ServiceQueryFormListDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.common.verifier.NetworkAddressVerifier;
import eu.arrowhead.core.choreographer.database.service.ChoreographerExecutorDBService;
import eu.arrowhead.core.choreographer.database.service.ChoreographerPlanDBService;
import eu.arrowhead.core.choreographer.executor.ExecutorData;
import eu.arrowhead.core.choreographer.executor.ExecutorSelector;
import eu.arrowhead.core.choreographer.service.ChoreographerDriver;

@RunWith(SpringRunner.class)
public class ChoreographerPlanExecutionCheckerTest {
	
	//=================================================================================================
	// members
	
	@InjectMocks
	private ChoreographerPlanExecutionChecker testObject;
	
	@Mock
	private NetworkAddressVerifier networkAddressVerifier;
	
	@Mock
	private ChoreographerPlanDBService planDBService;
	
	@Mock
	private ChoreographerExecutorDBService executorDBService;
	
	@Mock
	private ChoreographerDriver driver;
	
	@Mock
	private ExecutorSelector executorSelector;
	
	private final long maxIteration = 2;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Before
	public void setUp() {
		ReflectionTestUtils.setField(testObject, "maxIteration", maxIteration);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCheckPlanForExecutionRequestNull() {
		final ChoreographerRunPlanResponseDTO response = testObject.checkPlanForExecution(null);
		
		Assert.assertNull(response.getPlanId());
		Assert.assertEquals(ChoreographerSessionStatus.ABORTED, response.getStatus());
		Assert.assertEquals(1, response.getErrorMessages().size());
		Assert.assertEquals("Request is null.", response.getErrorMessages().get(0));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCheckPlanForExecutionBasicChecks1() {
		doThrow(InvalidParameterException.class).when(networkAddressVerifier).verify(anyString());
		
		final ChoreographerRunPlanRequestDTO request = new ChoreographerRunPlanRequestDTO();
		request.setNotifyAddress("1.2.3.4.5");
		request.setNotifyProtocol("FTP");
		request.setNotifyPort(-2);
		request.setNotifyPath(null);
		
		final ChoreographerRunPlanResponseDTO response = testObject.checkPlanForExecution(request);
		
		Assert.assertNull(response.getPlanId());
		Assert.assertEquals(ChoreographerSessionStatus.ABORTED, response.getStatus());
		Assert.assertEquals(4, response.getErrorMessages().size());
		Assert.assertEquals("Plan id is not valid.", response.getErrorMessages().get(0));
		Assert.assertEquals("Invalid notify protocol.", response.getErrorMessages().get(1));
		Assert.assertTrue(response.getErrorMessages().get(2).startsWith("Invalid notify address. "));
		Assert.assertTrue(response.getErrorMessages().get(3).startsWith("Notify port must be between "));
		Assert.assertEquals("", request.getNotifyPath());
		
		verify(networkAddressVerifier, times(1)).verify("1.2.3.4.5");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCheckPlanForExecutionBasicChecks2() {
		doNothing().when(networkAddressVerifier).verify(anyString());
		
		final ChoreographerRunPlanRequestDTO request = new ChoreographerRunPlanRequestDTO();
		request.setPlanId(0L);
		request.setNotifyAddress("localhost");
		request.setNotifyPort(100000);
		request.setNotifyPath("/abc");
		
		final ChoreographerRunPlanResponseDTO response = testObject.checkPlanForExecution(request);
		
		Assert.assertEquals((Long) 0L, response.getPlanId());
		Assert.assertEquals(ChoreographerSessionStatus.ABORTED, response.getStatus());
		Assert.assertEquals(2, response.getErrorMessages().size());
		Assert.assertEquals("Plan id is not valid.", response.getErrorMessages().get(0));
		Assert.assertTrue(response.getErrorMessages().get(1).startsWith("Notify port must be between "));
		Assert.assertEquals("/abc", request.getNotifyPath());
		
		verify(networkAddressVerifier, times(1)).verify("localhost");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCheckPlanForExecutionPlanNotFound() {
		when(planDBService.getPlanById(anyLong())).thenThrow(new InvalidParameterException("test"));
		
		final ChoreographerRunPlanResponseDTO response = testObject.checkPlanForExecution(false, 1, 6, false);
		
		Assert.assertEquals((Long) 1L, response.getPlanId());
		Assert.assertEquals(ChoreographerSessionStatus.ABORTED, response.getStatus());
		Assert.assertEquals(1, response.getErrorMessages().size());
		Assert.assertEquals("test", response.getErrorMessages().get(0));
		
		verify(planDBService, times(1)).getPlanById(1);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCheckPlanForExecutionZeroQuantity() {
		final long planId = 5L;
		final ChoreographerPlan plan = new ChoreographerPlan("plan");
		plan.setId(planId);
		final ChoreographerAction action = new ChoreographerAction("action", null);
		action.setPlan(plan);
		final ChoreographerStep step = new ChoreographerStep("step", action, "service", 1, 1, "{}", null, 1);

		when(planDBService.getPlanById(anyLong())).thenReturn(plan);
		when(planDBService.collectStepsFromPlan(anyLong())).thenReturn(List.of(step));
		when(executorDBService.getExecutorsByServiceDefinitionAndVersion(anyString(), anyInt(), anyInt())).thenReturn(List.of(new ChoreographerExecutor()));
		when(driver.searchForServices(any(ServiceQueryFormListDTO.class), eq(false))).thenReturn(Map.of());
		
		final ChoreographerRunPlanResponseDTO response = testObject.checkPlanForExecution(false, 1, 0, false);
		
		Assert.assertEquals((Long) 1L, response.getPlanId());
		Assert.assertEquals(ChoreographerSessionStatus.ABORTED, response.getStatus());
		Assert.assertEquals(1, response.getErrorMessages().size());
		Assert.assertEquals("Quantity must be greater than 0.", response.getErrorMessages().get(0));
		
		verify(planDBService, times(1)).getPlanById(1);
		
		verify(planDBService, times(1)).getPlanById(1);
		verify(planDBService, times(1)).collectStepsFromPlan(planId);
		verify(executorDBService, times(1)).getExecutorsByServiceDefinitionAndVersion("service", 1, 1);
		verify(driver, times(1)).searchForServices(any(ServiceQueryFormListDTO.class), eq(false));
		verify(executorSelector, never()).select(anyString(), anyInt(), anyInt(), isNull(), eq(false));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCheckPlanForExecutionGreateQuantityThanAllowed() {
		final long planId = 5L;
		final ChoreographerPlan plan = new ChoreographerPlan("plan");
		plan.setId(planId);
		final ChoreographerAction action = new ChoreographerAction("action", null);
		action.setPlan(plan);
		final ChoreographerStep step = new ChoreographerStep("step", action, "service", 1, 1, "{}", null, 1);

		when(planDBService.getPlanById(anyLong())).thenReturn(plan);
		when(planDBService.collectStepsFromPlan(anyLong())).thenReturn(List.of(step));
		when(executorDBService.getExecutorsByServiceDefinitionAndVersion(anyString(), anyInt(), anyInt())).thenReturn(List.of(new ChoreographerExecutor()));
		when(driver.searchForServices(any(ServiceQueryFormListDTO.class), eq(false))).thenReturn(Map.of());
		
		final ChoreographerRunPlanResponseDTO response = testObject.checkPlanForExecution(false, 1, 3, false);
		
		Assert.assertEquals((Long) 1L, response.getPlanId());
		Assert.assertEquals(ChoreographerSessionStatus.ABORTED, response.getStatus());
		Assert.assertEquals(1, response.getErrorMessages().size());
		Assert.assertEquals("Quantity could not be greater than 2.", response.getErrorMessages().get(0));
		
		verify(planDBService, times(1)).getPlanById(1);
		
		verify(planDBService, times(1)).getPlanById(1);
		verify(planDBService, times(1)).collectStepsFromPlan(planId);
		verify(executorDBService, times(1)).getExecutorsByServiceDefinitionAndVersion("service", 1, 1);
		verify(driver, times(1)).searchForServices(any(ServiceQueryFormListDTO.class), eq(false));
		verify(executorSelector, never()).select(anyString(), anyInt(), anyInt(), isNull(), eq(false));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCheckPlanForExecutionExecutorNotFound() {
		final long planId = 5L;
		final ChoreographerPlan plan = new ChoreographerPlan("plan");
		plan.setId(planId);
		final ChoreographerAction action = new ChoreographerAction("action", null);
		action.setPlan(plan);
		final ChoreographerStep step = new ChoreographerStep("step", action, "service", 1, 1, "{}", null, 1);

		when(planDBService.getPlanById(anyLong())).thenReturn(plan);
		when(planDBService.collectStepsFromPlan(anyLong())).thenReturn(List.of(step));
		when(executorDBService.getExecutorsByServiceDefinitionAndVersion(anyString(), anyInt(), anyInt())).thenReturn(List.of());
		when(driver.searchForServices(any(ServiceQueryFormListDTO.class), eq(false))).thenReturn(Map.of());
		
		final ChoreographerRunPlanResponseDTO response = testObject.checkPlanForExecution(false, 1, 2, true);
		
		Assert.assertEquals((Long) 1L, response.getPlanId());
		Assert.assertEquals(ChoreographerSessionStatus.ABORTED, response.getStatus());
		Assert.assertEquals(1, response.getErrorMessages().size());
		Assert.assertTrue(response.getErrorMessages().get(0).startsWith("Executor not found for step: "));
		
		verify(planDBService, times(1)).getPlanById(1);
		verify(planDBService, times(1)).collectStepsFromPlan(planId);
		verify(executorDBService, times(1)).getExecutorsByServiceDefinitionAndVersion("service", 1, 1);
		verify(driver, times(1)).searchForServices(any(ServiceQueryFormListDTO.class), eq(false));
		verify(executorSelector, never()).select(anyString(), anyInt(), anyInt(), isNull(), eq(false));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCheckPlanForExecutionProviderNotFound() {
		final long planId = 5L;
		final ChoreographerPlan plan = new ChoreographerPlan("plan");
		plan.setId(planId);
		final ChoreographerAction action = new ChoreographerAction("action", null);
		action.setPlan(plan);
		final ChoreographerStep step = new ChoreographerStep("step", action, "service", 1, 1, "{}", null, 1);

		when(planDBService.getPlanById(anyLong())).thenReturn(plan);
		when(planDBService.collectStepsFromPlan(anyLong())).thenReturn(List.of(step));
		when(executorDBService.getExecutorsByServiceDefinitionAndVersion(anyString(), anyInt(), anyInt())).thenReturn(List.of(new ChoreographerExecutor()));
		when(driver.searchForServices(any(ServiceQueryFormListDTO.class), eq(true))).thenReturn(Map.of(0, List.of()));
		
		final ChoreographerRunPlanResponseDTO response = testObject.checkPlanForExecution(true, 1, 2, false);
		
		Assert.assertEquals((Long) 1L, response.getPlanId());
		Assert.assertEquals(ChoreographerSessionStatus.ABORTED, response.getStatus());
		Assert.assertEquals(1, response.getErrorMessages().size());
		Assert.assertTrue(response.getErrorMessages().get(0).startsWith("Provider not found for step: "));
		
		verify(planDBService, times(1)).getPlanById(1);
		verify(planDBService, times(1)).collectStepsFromPlan(planId);
		verify(executorDBService, times(1)).getExecutorsByServiceDefinitionAndVersion("service", 1, 1);
		verify(driver, times(1)).searchForServices(any(ServiceQueryFormListDTO.class), eq(true));
		verify(executorSelector, never()).select(anyString(), anyInt(), anyInt(), isNull(), eq(true));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCheckPlanForExecutionConnectionProblem() {
		final long planId = 5L;
		final ChoreographerPlan plan = new ChoreographerPlan("plan");
		plan.setId(planId);
		final ChoreographerAction action = new ChoreographerAction("action", null);
		action.setPlan(plan);
		final ChoreographerStep step = new ChoreographerStep("step", action, "service", 1, 1, "{}", null, 1);

		when(planDBService.getPlanById(anyLong())).thenReturn(plan);
		when(planDBService.collectStepsFromPlan(anyLong())).thenReturn(List.of(step));
		when(executorDBService.getExecutorsByServiceDefinitionAndVersion(anyString(), anyInt(), anyInt())).thenReturn(List.of(new ChoreographerExecutor()));
		when(driver.searchForServices(any(ServiceQueryFormListDTO.class), eq(false))).thenThrow(ArrowheadException.class);
		
		final ChoreographerRunPlanResponseDTO response = testObject.checkPlanForExecution(false, 1, 2, false);
		
		Assert.assertEquals((Long) 1L, response.getPlanId());
		Assert.assertEquals(ChoreographerSessionStatus.ABORTED, response.getStatus());
		Assert.assertEquals(1, response.getErrorMessages().size());
		Assert.assertTrue(response.getErrorMessages().get(0).startsWith("Something happened when connecting to other core services: "));
		
		verify(planDBService, times(1)).getPlanById(1);
		verify(planDBService, times(1)).collectStepsFromPlan(planId);
		verify(executorDBService, times(1)).getExecutorsByServiceDefinitionAndVersion("service", 1, 1);
		verify(driver, times(1)).searchForServices(any(ServiceQueryFormListDTO.class), eq(false));
		verify(executorSelector, never()).select(anyString(), anyInt(), anyInt(), isNull(), eq(false));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCheckPlanForExecutionDependencyNotFound() {
		final long planId = 5L;
		final ChoreographerPlan plan = new ChoreographerPlan("plan");
		plan.setId(planId);
		final ChoreographerAction action = new ChoreographerAction("action", null);
		action.setPlan(plan);
		final ChoreographerStep step = new ChoreographerStep("step", action, "service", 1, 1, "{}", null, 1);

		when(planDBService.getPlanById(anyLong())).thenReturn(plan);
		when(planDBService.collectStepsFromPlan(anyLong())).thenReturn(List.of(step));
		when(executorDBService.getExecutorsByServiceDefinitionAndVersion(anyString(), anyInt(), anyInt())).thenReturn(List.of(new ChoreographerExecutor()));
		when(driver.searchForServices(any(ServiceQueryFormListDTO.class), eq(false))).thenReturn(Map.of(0, List.of("#OWN_CLOUD#")));
		when(executorSelector.select(anyString(), anyInt(), anyInt(), isNull(), eq(false))).thenReturn(null);
		
		final ChoreographerRunPlanResponseDTO response = testObject.checkPlanForExecution(false, 1, 2, true);
		
		Assert.assertEquals((Long) 1L, response.getPlanId());
		Assert.assertEquals(ChoreographerSessionStatus.ABORTED, response.getStatus());
		Assert.assertEquals(1, response.getErrorMessages().size());
		Assert.assertTrue(response.getErrorMessages().get(0).startsWith("Executor not found for step: "));
		
		verify(planDBService, times(1)).getPlanById(1);
		verify(planDBService, times(1)).collectStepsFromPlan(planId);
		verify(executorDBService, times(1)).getExecutorsByServiceDefinitionAndVersion("service", 1, 1);
		verify(driver, times(1)).searchForServices(any(ServiceQueryFormListDTO.class), eq(false));
		verify(executorSelector, times(1)).select(anyString(), anyInt(), anyInt(), isNull(), eq(false));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCheckPlanForExecutionOkNoInterCloud() {
		final long planId = 5L;
		final ChoreographerPlan plan = new ChoreographerPlan("plan");
		plan.setId(planId);
		final ChoreographerAction action = new ChoreographerAction("action", null);
		action.setPlan(plan);
		final ChoreographerStep step = new ChoreographerStep("step", action, "service", 1, 1, "{}", null, 1);

		when(planDBService.getPlanById(anyLong())).thenReturn(plan);
		when(planDBService.collectStepsFromPlan(anyLong())).thenReturn(List.of(step));
		when(executorDBService.getExecutorsByServiceDefinitionAndVersion(anyString(), anyInt(), anyInt())).thenReturn(List.of(new ChoreographerExecutor()));
		when(driver.searchForServices(any(ServiceQueryFormListDTO.class), eq(false))).thenReturn(Map.of(0, List.of("#OWN_CLOUD#")));
		when(executorSelector.select(anyString(), anyInt(), anyInt(), isNull(), eq(false))).thenReturn(new ExecutorData(new ChoreographerExecutor(), new SystemRequestDTO(), List.of(), false));
		
		final ChoreographerRunPlanResponseDTO response = testObject.checkPlanForExecution(false, 1, 2, true);
		
		Assert.assertTrue(Utilities.isEmpty(response.getErrorMessages()));
		Assert.assertFalse(response.getNeedInterCloud());
		
		verify(planDBService, times(1)).getPlanById(1);
		verify(planDBService, times(1)).collectStepsFromPlan(planId);
		verify(executorDBService, times(1)).getExecutorsByServiceDefinitionAndVersion("service", 1, 1);
		verify(driver, times(1)).searchForServices(any(ServiceQueryFormListDTO.class), eq(false));
		verify(executorSelector, times(1)).select(anyString(), anyInt(), anyInt(), isNull(), eq(false));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCheckPlanForExecutionOkNeedInterCloud1() {
		final long planId = 5L;
		final ChoreographerPlan plan = new ChoreographerPlan("plan");
		plan.setId(planId);
		final ChoreographerAction action = new ChoreographerAction("action", null);
		action.setPlan(plan);
		final ChoreographerStep step = new ChoreographerStep("step", action, "service", 1, 1, "{}", null, 1);

		when(planDBService.getPlanById(anyLong())).thenReturn(plan);
		when(planDBService.collectStepsFromPlan(anyLong())).thenReturn(List.of(step));
		when(executorDBService.getExecutorsByServiceDefinitionAndVersion(anyString(), anyInt(), anyInt())).thenReturn(List.of(new ChoreographerExecutor()));
		when(driver.searchForServices(any(ServiceQueryFormListDTO.class), eq(true))).thenReturn(Map.of(0, List.of("operator/name")));
		when(executorSelector.select(anyString(), anyInt(), anyInt(), isNull(), eq(true))).thenReturn(new ExecutorData(new ChoreographerExecutor(), new SystemRequestDTO(), List.of(), false));
		
		final ChoreographerRunPlanResponseDTO response = testObject.checkPlanForExecution(true, 1, 2, true);
		
		Assert.assertTrue(Utilities.isEmpty(response.getErrorMessages()));
		Assert.assertTrue(response.getNeedInterCloud());
		
		verify(planDBService, times(1)).getPlanById(1);
		verify(planDBService, times(1)).collectStepsFromPlan(planId);
		verify(executorDBService, times(1)).getExecutorsByServiceDefinitionAndVersion("service", 1, 1);
		verify(driver, times(1)).searchForServices(any(ServiceQueryFormListDTO.class), eq(true));
		verify(executorSelector, times(1)).select(anyString(), anyInt(), anyInt(), isNull(), eq(true));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCheckPlanForExecutionOkNeedInterCloud2() {
		final long planId = 5L;
		final ChoreographerPlan plan = new ChoreographerPlan("plan");
		plan.setId(planId);
		final ChoreographerAction action = new ChoreographerAction("action", null);
		action.setPlan(plan);
		final ChoreographerStep step = new ChoreographerStep("step", action, "service", 1, 1, "{}", null, 1);

		when(planDBService.getPlanById(anyLong())).thenReturn(plan);
		when(planDBService.collectStepsFromPlan(anyLong())).thenReturn(List.of(step));
		when(executorDBService.getExecutorsByServiceDefinitionAndVersion(anyString(), anyInt(), anyInt())).thenReturn(List.of(new ChoreographerExecutor()));
		when(driver.searchForServices(any(ServiceQueryFormListDTO.class), eq(true))).thenReturn(Map.of(0, List.of("#OWN_CLOUD#")));
		when(executorSelector.select(anyString(), anyInt(), anyInt(), isNull(), eq(true))).thenReturn(new ExecutorData(new ChoreographerExecutor(), new SystemRequestDTO(), List.of(), true));
		
		final ChoreographerRunPlanResponseDTO response = testObject.checkPlanForExecution(true, 1, 2, true);
		
		Assert.assertTrue(Utilities.isEmpty(response.getErrorMessages()));
		Assert.assertTrue(response.getNeedInterCloud());
		
		verify(planDBService, times(1)).getPlanById(1);
		verify(planDBService, times(1)).collectStepsFromPlan(planId);
		verify(executorDBService, times(1)).getExecutorsByServiceDefinitionAndVersion("service", 1, 1);
		verify(driver, times(1)).searchForServices(any(ServiceQueryFormListDTO.class), eq(true));
		verify(executorSelector, times(1)).select(anyString(), anyInt(), anyInt(), isNull(), eq(true));
	}
}