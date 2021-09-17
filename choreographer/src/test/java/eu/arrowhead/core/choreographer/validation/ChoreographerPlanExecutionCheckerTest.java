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
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;

import eu.arrowhead.common.database.entity.ChoreographerAction;
import eu.arrowhead.common.database.entity.ChoreographerExecutor;
import eu.arrowhead.common.database.entity.ChoreographerPlan;
import eu.arrowhead.common.database.entity.ChoreographerStep;
import eu.arrowhead.common.dto.internal.ChoreographerSessionStatus;
import eu.arrowhead.common.dto.shared.ChoreographerRunPlanRequestDTO;
import eu.arrowhead.common.dto.shared.ChoreographerRunPlanResponseDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryFormListDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryResultDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryResultListDTO;
import eu.arrowhead.common.dto.shared.ServiceRegistryResponseDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.common.verifier.NetworkAddressVerifier;
import eu.arrowhead.core.choreographer.database.service.ChoreographerExecutorDBService;
import eu.arrowhead.core.choreographer.database.service.ChoreographerPlanDBService;
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
	
	//=================================================================================================
	// methods
	
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
		
		final ChoreographerRunPlanResponseDTO response = testObject.checkPlanForExecution(1, false);
		
		Assert.assertEquals((Long) 1L, response.getPlanId());
		Assert.assertEquals(ChoreographerSessionStatus.ABORTED, response.getStatus());
		Assert.assertEquals(1, response.getErrorMessages().size());
		Assert.assertEquals("test", response.getErrorMessages().get(0));
		
		verify(planDBService, times(1)).getPlanById(1);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCheckPlanForExecutionExecutorNotFound() {
		final ChoreographerPlan plan = new ChoreographerPlan("plan");
		final ChoreographerAction action = new ChoreographerAction("action", null);
		action.setPlan(plan);
		final ChoreographerStep step = new ChoreographerStep("step", action, "service", 1, 1, "{}", null, 1);

		when(planDBService.getPlanById(anyLong())).thenReturn(plan);
		when(planDBService.collectStepsFromPlan(any(ChoreographerPlan.class))).thenReturn(List.of(step));
		when(executorDBService.getExecutorsByServiceDefinitionAndVersion(anyString(), anyInt(), anyInt())).thenReturn(List.of());
		final ServiceQueryResultDTO result = new ServiceQueryResultDTO();
		result.getServiceQueryData().add(new ServiceRegistryResponseDTO());
		when(driver.multiQueryServiceRegistry(any(ServiceQueryFormListDTO.class))).thenReturn(new ServiceQueryResultListDTO(List.of(result)));
		
		final ChoreographerRunPlanResponseDTO response = testObject.checkPlanForExecution(1, true);
		
		Assert.assertEquals((Long) 1L, response.getPlanId());
		Assert.assertEquals(ChoreographerSessionStatus.ABORTED, response.getStatus());
		Assert.assertEquals(1, response.getErrorMessages().size());
		Assert.assertTrue(response.getErrorMessages().get(0).startsWith("Executor not found for step: "));
		
		verify(planDBService, times(1)).getPlanById(1);
		verify(planDBService, times(1)).collectStepsFromPlan(plan);
		verify(executorDBService, times(1)).getExecutorsByServiceDefinitionAndVersion("service", 1, 1);
		verify(driver, times(1)).multiQueryServiceRegistry(any(ServiceQueryFormListDTO.class));
		verify(executorSelector, never()).select(anyString(), anyInt(), anyInt(), isNull());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCheckPlanForExecutionProviderNotFound() {
		final ChoreographerPlan plan = new ChoreographerPlan("plan");
		final ChoreographerAction action = new ChoreographerAction("action", null);
		action.setPlan(plan);
		final ChoreographerStep step = new ChoreographerStep("step", action, "service", 1, 1, "{}", null, 1);

		when(planDBService.getPlanById(anyLong())).thenReturn(plan);
		when(planDBService.collectStepsFromPlan(any(ChoreographerPlan.class))).thenReturn(List.of(step));
		when(executorDBService.getExecutorsByServiceDefinitionAndVersion(anyString(), anyInt(), anyInt())).thenReturn(List.of(new ChoreographerExecutor()));
		final ServiceQueryResultDTO result = new ServiceQueryResultDTO();
		when(driver.multiQueryServiceRegistry(any(ServiceQueryFormListDTO.class))).thenReturn(new ServiceQueryResultListDTO(List.of(result)));
		
		final ChoreographerRunPlanResponseDTO response = testObject.checkPlanForExecution(1, false);
		
		Assert.assertEquals((Long) 1L, response.getPlanId());
		Assert.assertEquals(ChoreographerSessionStatus.ABORTED, response.getStatus());
		Assert.assertEquals(1, response.getErrorMessages().size());
		Assert.assertTrue(response.getErrorMessages().get(0).startsWith("Provider not found for step: "));
		
		verify(planDBService, times(1)).getPlanById(1);
		verify(planDBService, times(1)).collectStepsFromPlan(plan);
		verify(executorDBService, times(1)).getExecutorsByServiceDefinitionAndVersion("service", 1, 1);
		verify(driver, times(1)).multiQueryServiceRegistry(any(ServiceQueryFormListDTO.class));
		verify(executorSelector, never()).select(anyString(), anyInt(), anyInt(), isNull());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCheckPlanForExecutionSRConnectionProblem() {
		final ChoreographerPlan plan = new ChoreographerPlan("plan");
		final ChoreographerAction action = new ChoreographerAction("action", null);
		action.setPlan(plan);
		final ChoreographerStep step = new ChoreographerStep("step", action, "service", 1, 1, "{}", null, 1);

		when(planDBService.getPlanById(anyLong())).thenReturn(plan);
		when(planDBService.collectStepsFromPlan(any(ChoreographerPlan.class))).thenReturn(List.of(step));
		when(executorDBService.getExecutorsByServiceDefinitionAndVersion(anyString(), anyInt(), anyInt())).thenReturn(List.of(new ChoreographerExecutor()));
		when(driver.multiQueryServiceRegistry(any(ServiceQueryFormListDTO.class))).thenThrow(ArrowheadException.class);
		
		final ChoreographerRunPlanResponseDTO response = testObject.checkPlanForExecution(1, false);
		
		Assert.assertEquals((Long) 1L, response.getPlanId());
		Assert.assertEquals(ChoreographerSessionStatus.ABORTED, response.getStatus());
		Assert.assertEquals(1, response.getErrorMessages().size());
		Assert.assertTrue(response.getErrorMessages().get(0).startsWith("Something happened when connecting to the Service Registry: "));
		
		verify(planDBService, times(1)).getPlanById(1);
		verify(planDBService, times(1)).collectStepsFromPlan(plan);
		verify(executorDBService, times(1)).getExecutorsByServiceDefinitionAndVersion("service", 1, 1);
		verify(driver, times(1)).multiQueryServiceRegistry(any(ServiceQueryFormListDTO.class));
		verify(executorSelector, never()).select(anyString(), anyInt(), anyInt(), isNull());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCheckPlanForExecutionDependencyNotFound() {
		final ChoreographerPlan plan = new ChoreographerPlan("plan");
		final ChoreographerAction action = new ChoreographerAction("action", null);
		action.setPlan(plan);
		final ChoreographerStep step = new ChoreographerStep("step", action, "service", 1, 1, "{}", null, 1);

		when(planDBService.getPlanById(anyLong())).thenReturn(plan);
		when(planDBService.collectStepsFromPlan(any(ChoreographerPlan.class))).thenReturn(List.of(step));
		when(executorDBService.getExecutorsByServiceDefinitionAndVersion(anyString(), anyInt(), anyInt())).thenReturn(List.of(new ChoreographerExecutor()));
		final ServiceQueryResultDTO result = new ServiceQueryResultDTO();
		result.getServiceQueryData().add(new ServiceRegistryResponseDTO());
		when(driver.multiQueryServiceRegistry(any(ServiceQueryFormListDTO.class))).thenReturn(new ServiceQueryResultListDTO(List.of(result)));
		when(executorSelector.select(anyString(), anyInt(), anyInt(), isNull())).thenReturn(null);
		
		final ChoreographerRunPlanResponseDTO response = testObject.checkPlanForExecution(1, true);
		
		Assert.assertEquals((Long) 1L, response.getPlanId());
		Assert.assertEquals(ChoreographerSessionStatus.ABORTED, response.getStatus());
		Assert.assertEquals(1, response.getErrorMessages().size());
		Assert.assertTrue(response.getErrorMessages().get(0).startsWith("Executor not found for step: "));
		
		verify(planDBService, times(1)).getPlanById(1);
		verify(planDBService, times(1)).collectStepsFromPlan(plan);
		verify(executorDBService, times(1)).getExecutorsByServiceDefinitionAndVersion("service", 1, 1);
		verify(driver, times(1)).multiQueryServiceRegistry(any(ServiceQueryFormListDTO.class));
		verify(executorSelector, times(1)).select(anyString(), anyInt(), anyInt(), isNull());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCheckPlanForExecutionOk() {
		final ChoreographerPlan plan = new ChoreographerPlan("plan");
		final ChoreographerAction action = new ChoreographerAction("action", null);
		action.setPlan(plan);
		final ChoreographerStep step = new ChoreographerStep("step", action, "service", 1, 1, "{}", null, 1);

		when(planDBService.getPlanById(anyLong())).thenReturn(plan);
		when(planDBService.collectStepsFromPlan(any(ChoreographerPlan.class))).thenReturn(List.of(step));
		when(executorDBService.getExecutorsByServiceDefinitionAndVersion(anyString(), anyInt(), anyInt())).thenReturn(List.of(new ChoreographerExecutor()));
		final ServiceQueryResultDTO result = new ServiceQueryResultDTO();
		result.getServiceQueryData().add(new ServiceRegistryResponseDTO());
		when(driver.multiQueryServiceRegistry(any(ServiceQueryFormListDTO.class))).thenReturn(new ServiceQueryResultListDTO(List.of(result)));
		when(executorSelector.select(anyString(), anyInt(), anyInt(), isNull())).thenReturn(new ChoreographerExecutor());
		
		final ChoreographerRunPlanResponseDTO response = testObject.checkPlanForExecution(1, true);
		
		Assert.assertNull(response);
		
		verify(planDBService, times(1)).getPlanById(1);
		verify(planDBService, times(1)).collectStepsFromPlan(plan);
		verify(executorDBService, times(1)).getExecutorsByServiceDefinitionAndVersion("service", 1, 1);
		verify(driver, times(1)).multiQueryServiceRegistry(any(ServiceQueryFormListDTO.class));
		verify(executorSelector, times(1)).select(anyString(), anyInt(), anyInt(), isNull());
	}
}