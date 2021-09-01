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

package eu.arrowhead.core.choreographer.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import eu.arrowhead.common.CoreSystemRegistrationProperties;
import eu.arrowhead.common.core.CoreSystem;
import eu.arrowhead.common.database.entity.ChoreographerAction;
import eu.arrowhead.common.database.entity.ChoreographerExecutor;
import eu.arrowhead.common.database.entity.ChoreographerPlan;
import eu.arrowhead.common.database.entity.ChoreographerSession;
import eu.arrowhead.common.database.entity.ChoreographerSessionStep;
import eu.arrowhead.common.database.entity.ChoreographerStep;
import eu.arrowhead.common.dto.internal.ChoreographerSessionStatus;
import eu.arrowhead.common.dto.internal.ChoreographerSessionStepStatus;
import eu.arrowhead.common.dto.internal.ChoreographerStartSessionDTO;
import eu.arrowhead.common.dto.internal.TokenDataDTO;
import eu.arrowhead.common.dto.internal.TokenGenerationDetailedResponseDTO;
import eu.arrowhead.common.dto.internal.TokenGenerationMultiServiceResponseDTO;
import eu.arrowhead.common.dto.shared.ChoreographerExecuteStepRequestDTO;
import eu.arrowhead.common.dto.shared.ChoreographerExecutedStepResultDTO;
import eu.arrowhead.common.dto.shared.OrchestrationFormRequestDTO;
import eu.arrowhead.common.dto.shared.OrchestrationResponseDTO;
import eu.arrowhead.common.dto.shared.OrchestrationResultDTO;
import eu.arrowhead.common.dto.shared.ServiceDefinitionResponseDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryFormDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.common.dto.shared.SystemResponseDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.core.choreographer.database.service.ChoreographerPlanDBService;
import eu.arrowhead.core.choreographer.database.service.ChoreographerSessionDBService;
import eu.arrowhead.core.choreographer.exception.ChoreographerSessionException;
import eu.arrowhead.core.choreographer.executor.ExecutorData;
import eu.arrowhead.core.choreographer.executor.ExecutorSelector;

@RunWith(SpringRunner.class)
public class ChoreographerServiceTest {
	
	//=================================================================================================
	// members
	
	@InjectMocks
	private ChoreographerService testObject;
	
	@Mock
    private ChoreographerPlanDBService planDBService;
    
	@Mock
    private ChoreographerSessionDBService sessionDBService;

	@Mock
    private ChoreographerDriver driver;
    
	@Mock
    private SessionDataStorage sessionDataStorage;
    
	@Mock
    private ExecutorSelector executorSelector;

	@Mock
    protected CoreSystemRegistrationProperties registrationProperties;
    
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Before
	public void setUp() {
		when(registrationProperties.getCoreSystemName()).thenReturn(CoreSystem.CHOREOGRAPHER.name());
		
		testObject.init();
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInit() {
		final SystemRequestDTO requesterSystem = (SystemRequestDTO) ReflectionTestUtils.getField(testObject, "requesterSystem");
		Assert.assertNotNull(requesterSystem);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testReceiveStartSessionMessageInputNull() {
		try {
			testObject.receiveStartSessionMessage(null);
		} catch (final Exception ex) {
			Assert.assertEquals("Payload is null.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ChoreographerSessionException.class)
	public void testReceiveStartSessionMessageExceptionWrapping() {
		final ChoreographerStartSessionDTO payload = new ChoreographerStartSessionDTO(1, 2);

		when(planDBService.getPlanById(2)).thenThrow(new ArrowheadException("test"));

		try {
			testObject.receiveStartSessionMessage(payload);
		} catch (final Exception ex) {
			verify(planDBService, times(1)).getPlanById(2);
			
			Assert.assertEquals(ArrowheadException.class, ex.getCause().getClass());
			Assert.assertEquals("test", ex.getCause().getMessage());
			
			throw ex;
		}

	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ChoreographerSessionException.class)
	public void testReceiveStartSessionMessageExecutorNotFound() {
		final ChoreographerStartSessionDTO payload = new ChoreographerStartSessionDTO(1, 2);
		final ChoreographerPlan plan = new ChoreographerPlan("plan");
		final ChoreographerAction action = new ChoreographerAction("action", null);
		action.setPlan(plan);
		final ChoreographerStep step = new ChoreographerStep();
		step.setName("step");
		step.setAction(action);
		step.setServiceDefinition("service");
		step.setMinVersion(1);
		step.setMinVersion(2);
		
		when(planDBService.getPlanById(2)).thenReturn(plan);
		doNothing().when(sessionDBService).worklog(any(String.class), anyLong(), anyString(), isNull());
		when(sessionDBService.changeSessionStatus(eq(1L), eq(ChoreographerSessionStatus.RUNNING), isNull())).thenReturn(new ChoreographerSession());
		when(planDBService.collectStepsFromPlan(any(ChoreographerPlan.class))).thenReturn(List.of(step));
		when(sessionDataStorage.put(eq(1L), any(SessionExecutorCache.class))).thenReturn(null);
		when(executorSelector.selectAndInit(eq(1L), any(ChoreographerStep.class), anySet(), eq(true))).thenReturn(null);
		
		try {
			testObject.receiveStartSessionMessage(payload);
		} catch (final Exception ex) {
			verify(planDBService, times(1)).getPlanById(2);
			verify(sessionDBService, times(1)).worklog(any(String.class), anyLong(), anyString(), isNull());
			verify(sessionDBService, times(1)).changeSessionStatus(eq(1L), eq(ChoreographerSessionStatus.RUNNING), isNull());
			verify(planDBService, times(1)).collectStepsFromPlan(any(ChoreographerPlan.class));
			verify(sessionDataStorage, times(1)).put(eq(1L), any(SessionExecutorCache.class));
			verify(executorSelector, times(1)).selectAndInit(eq(1L), any(ChoreographerStep.class), anySet(), eq(true));
			
			Assert.assertEquals("Can't find properly working executor for step: plan.action.step", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ChoreographerSessionException.class)
	public void testReceiveStartSessionMessageManualSessionStepRegistering() {
		final ChoreographerStartSessionDTO payload = new ChoreographerStartSessionDTO(1, 2);
		final ChoreographerPlan plan = new ChoreographerPlan("plan");
		final ChoreographerAction action = new ChoreographerAction("action", null);
		action.setPlan(plan);
		final ChoreographerStep step = new ChoreographerStep();
		step.setId(1);
		step.setName("step");
		step.setAction(action);
		step.setServiceDefinition("service");
		step.setMinVersion(1);
		step.setMinVersion(2);
		
		final ChoreographerStep step2 = new ChoreographerStep();
		step2.setId(2);
		step2.setName("step2");
		step2.setAction(action);
		step2.setServiceDefinition("service");
		step2.setMinVersion(1);
		step2.setMinVersion(2);
		
		when(planDBService.getPlanById(2)).thenReturn(plan);
		doNothing().doThrow(new ArrowheadException("test")).when(sessionDBService).worklog(any(String.class), anyLong(), anyString(), isNull()); // second call throws an exception to end the test
		when(sessionDBService.changeSessionStatus(eq(1L), eq(ChoreographerSessionStatus.RUNNING), isNull())).thenReturn(new ChoreographerSession());
		when(planDBService.collectStepsFromPlan(any(ChoreographerPlan.class))).thenReturn(List.of(step, step2));
		when(sessionDataStorage.put(eq(1L), any(SessionExecutorCache.class))).thenReturn(null);
		when(executorSelector.selectAndInit(eq(1L), any(ChoreographerStep.class), anySet(), eq(true))).thenReturn(new ExecutorData(new ChoreographerExecutor(), List.of()));
		when(sessionDBService.registerSessionStep(eq(1L), eq(2L), anyLong())).thenReturn(new ChoreographerSessionStep());
		
		try {
			testObject.receiveStartSessionMessage(payload);
		} catch (final Exception ex) {
			verify(planDBService, times(1)).getPlanById(2);
			verify(sessionDBService, times(2)).worklog(any(String.class), anyLong(), anyString(), isNull());
			verify(sessionDBService, times(1)).changeSessionStatus(eq(1L), eq(ChoreographerSessionStatus.RUNNING), isNull());
			verify(planDBService, times(1)).collectStepsFromPlan(any(ChoreographerPlan.class));
			verify(sessionDataStorage, times(1)).put(eq(1L), any(SessionExecutorCache.class));
			verify(executorSelector, times(1)).selectAndInit(eq(1L), any(ChoreographerStep.class), anySet(), eq(true));
			verify(sessionDBService, times(1)).registerSessionStep(eq(1L), eq(2L), anyLong());
			
			Assert.assertEquals("test", ex.getCause().getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ChoreographerSessionException.class)
	public void testReceiveStartSessionMessageCatchMethodFromParallelStream() {
		final ChoreographerStartSessionDTO payload = new ChoreographerStartSessionDTO(1, 2);
		final ChoreographerPlan plan = new ChoreographerPlan("plan");
		final ChoreographerAction action = new ChoreographerAction("action", null);
		plan.setFirstAction(action);
		action.setPlan(plan);
		final ChoreographerStep step = new ChoreographerStep();
		step.setName("step");
		step.setAction(action);
		step.setServiceDefinition("service");
		step.setMinVersion(1);
		step.setMinVersion(2);
		
		when(planDBService.getPlanById(2)).thenReturn(plan);
		doNothing().when(sessionDBService).worklog(any(String.class), anyLong(), anyString(), isNull());
		when(sessionDBService.changeSessionStatus(eq(1L), eq(ChoreographerSessionStatus.RUNNING), isNull())).thenReturn(new ChoreographerSession());
		when(planDBService.collectStepsFromPlan(any(ChoreographerPlan.class))).thenReturn(List.of(step));
		when(sessionDataStorage.put(eq(1L), any(SessionExecutorCache.class))).thenReturn(null);
		when(executorSelector.selectAndInit(eq(1L), any(ChoreographerStep.class), anySet(), eq(true))).thenReturn(new ExecutorData(new ChoreographerExecutor(), List.of()));
		when(planDBService.getFirstSteps(any(ChoreographerAction.class))).thenReturn(List.of(step));
		when(sessionDBService.changeSessionStepStatus(eq(1L), eq(step), eq(ChoreographerSessionStepStatus.RUNNING), anyString())).thenThrow(new ArrowheadException("test"));
		
		try {
			testObject.receiveStartSessionMessage(payload);
		} catch (final Exception ex) {
			verify(planDBService, times(1)).getPlanById(2);
			verify(sessionDBService, times(2)).worklog(any(String.class), anyLong(), anyString(), isNull());
			verify(sessionDBService, times(1)).changeSessionStatus(eq(1L), eq(ChoreographerSessionStatus.RUNNING), isNull());
			verify(planDBService, times(1)).collectStepsFromPlan(any(ChoreographerPlan.class));
			verify(sessionDataStorage, times(1)).put(eq(1L), any(SessionExecutorCache.class));
			verify(executorSelector, times(1)).selectAndInit(eq(1L), any(ChoreographerStep.class), anySet(), eq(true));
			verify(sessionDBService, never()).registerSessionStep(eq(1L), anyLong(), anyLong());
			verify(planDBService, times(1)).getFirstSteps(any(ChoreographerAction.class));
			verify(sessionDBService, times(1)).changeSessionStepStatus(eq(1L), eq(step), eq(ChoreographerSessionStepStatus.RUNNING), anyString());

			Assert.assertEquals(1L, ((ChoreographerSessionException)ex).getSessionId());
			Assert.assertEquals("test", ex.getCause().getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ChoreographerSessionException.class)
	public void testReceiveStartSessionMessageProblemDuringMainServiceOrchestration() {
		final ChoreographerStartSessionDTO payload = new ChoreographerStartSessionDTO(1, 2);
		final ChoreographerPlan plan = new ChoreographerPlan("plan");
		final ChoreographerAction action = new ChoreographerAction("action", null);
		plan.setFirstAction(action);
		action.setPlan(plan);
		final ChoreographerStep step = new ChoreographerStep();
		step.setName("step");
		step.setAction(action);
		step.setServiceDefinition("service");
		step.setMinVersion(1);
		step.setMinVersion(2);
		
		when(planDBService.getPlanById(2)).thenReturn(plan);
		doNothing().when(sessionDBService).worklog(any(String.class), anyLong(), anyString(), isNull());
		when(sessionDBService.changeSessionStatus(eq(1L), eq(ChoreographerSessionStatus.RUNNING), isNull())).thenReturn(new ChoreographerSession());
		when(planDBService.collectStepsFromPlan(any(ChoreographerPlan.class))).thenReturn(List.of(step));
		when(sessionDataStorage.put(eq(1L), any(SessionExecutorCache.class))).thenReturn(null);
		when(executorSelector.selectAndInit(eq(1L), any(ChoreographerStep.class), anySet(), eq(true))).thenReturn(new ExecutorData(new ChoreographerExecutor(), List.of()));
		when(planDBService.getFirstSteps(any(ChoreographerAction.class))).thenReturn(List.of(step));
		when(sessionDBService.changeSessionStepStatus(eq(1L), eq(step), eq(ChoreographerSessionStepStatus.RUNNING), anyString())).thenReturn(new ChoreographerSessionStep());
		when(driver.queryOrchestrator(any(OrchestrationFormRequestDTO.class))).thenThrow(new ArrowheadException("orch problem"));
		
		try {
			testObject.receiveStartSessionMessage(payload);
		} catch (final Exception ex) {
			verify(planDBService, times(1)).getPlanById(2);
			verify(sessionDBService, times(2)).worklog(any(String.class), anyLong(), anyString(), isNull());
			verify(sessionDBService, times(1)).changeSessionStatus(eq(1L), eq(ChoreographerSessionStatus.RUNNING), isNull());
			verify(planDBService, times(1)).collectStepsFromPlan(any(ChoreographerPlan.class));
			verify(sessionDataStorage, times(1)).put(eq(1L), any(SessionExecutorCache.class));
			verify(executorSelector, times(1)).selectAndInit(eq(1L), any(ChoreographerStep.class), anySet(), eq(true));
			verify(sessionDBService, never()).registerSessionStep(eq(1L), anyLong(), anyLong());
			verify(planDBService, times(1)).getFirstSteps(any(ChoreographerAction.class));
			verify(sessionDBService, times(1)).changeSessionStepStatus(eq(1L), eq(step), eq(ChoreographerSessionStepStatus.RUNNING), anyString());
			verify(driver, times(1)).queryOrchestrator(any(OrchestrationFormRequestDTO.class));

			Assert.assertEquals(1L, ((ChoreographerSessionException)ex).getSessionId());
			Assert.assertEquals("orch problem", ex.getCause().getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ChoreographerSessionException.class)
	public void testReceiveStartSessionMessageMainServiceOrchestrationProviderNotFound() {
		final ChoreographerStartSessionDTO payload = new ChoreographerStartSessionDTO(1, 2);
		final ChoreographerPlan plan = new ChoreographerPlan("plan");
		final ChoreographerAction action = new ChoreographerAction("action", null);
		plan.setFirstAction(action);
		action.setPlan(plan);
		final ChoreographerStep step = new ChoreographerStep();
		step.setName("step");
		step.setAction(action);
		step.setServiceDefinition("service");
		step.setMinVersion(1);
		step.setMinVersion(2);
		
		when(planDBService.getPlanById(2)).thenReturn(plan);
		doNothing().when(sessionDBService).worklog(any(String.class), anyLong(), anyString(), isNull());
		when(sessionDBService.changeSessionStatus(eq(1L), eq(ChoreographerSessionStatus.RUNNING), isNull())).thenReturn(new ChoreographerSession());
		when(planDBService.collectStepsFromPlan(any(ChoreographerPlan.class))).thenReturn(List.of(step));
		when(sessionDataStorage.put(eq(1L), any(SessionExecutorCache.class))).thenReturn(null);
		when(executorSelector.selectAndInit(eq(1L), any(ChoreographerStep.class), anySet(), eq(true))).thenReturn(new ExecutorData(new ChoreographerExecutor(), List.of()));
		when(planDBService.getFirstSteps(any(ChoreographerAction.class))).thenReturn(List.of(step));
		when(sessionDBService.changeSessionStepStatus(eq(1L), eq(step), eq(ChoreographerSessionStepStatus.RUNNING), anyString())).thenReturn(new ChoreographerSessionStep());
		when(driver.queryOrchestrator(any(OrchestrationFormRequestDTO.class))).thenReturn(new OrchestrationResponseDTO(List.of()));
		
		try {
			testObject.receiveStartSessionMessage(payload);
		} catch (final Exception ex) {
			verify(planDBService, times(1)).getPlanById(2);
			verify(sessionDBService, times(2)).worklog(any(String.class), anyLong(), anyString(), isNull());
			verify(sessionDBService, times(1)).changeSessionStatus(eq(1L), eq(ChoreographerSessionStatus.RUNNING), isNull());
			verify(planDBService, times(1)).collectStepsFromPlan(any(ChoreographerPlan.class));
			verify(sessionDataStorage, times(1)).put(eq(1L), any(SessionExecutorCache.class));
			verify(executorSelector, times(1)).selectAndInit(eq(1L), any(ChoreographerStep.class), anySet(), eq(true));
			verify(sessionDBService, never()).registerSessionStep(eq(1L), anyLong(), anyLong());
			verify(planDBService, times(1)).getFirstSteps(any(ChoreographerAction.class));
			verify(sessionDBService, times(1)).changeSessionStepStatus(eq(1L), eq(step), eq(ChoreographerSessionStepStatus.RUNNING), anyString());
			verify(driver, times(1)).queryOrchestrator(any(OrchestrationFormRequestDTO.class));

			Assert.assertEquals(1L, ((ChoreographerSessionException)ex).getSessionId());
			Assert.assertEquals("No providers found for step: plan.action.step", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ChoreographerSessionException.class)
	public void testReceiveStartSessionMessageChangeExecutorToTheCached() {
		final ChoreographerStartSessionDTO payload = new ChoreographerStartSessionDTO(1, 2);
		final ChoreographerPlan plan = new ChoreographerPlan("plan");
		final ChoreographerAction action = new ChoreographerAction("action", null);
		plan.setFirstAction(action);
		action.setPlan(plan);
		final ChoreographerStep step = new ChoreographerStep();
		step.setName("step");
		step.setAction(action);
		step.setServiceDefinition("service");
		step.setMinVersion(1);
		step.setMaxVersion(2);
		
		final ChoreographerExecutor oldExecutor = new ChoreographerExecutor();
		oldExecutor.setId(1);
		final ChoreographerSessionStep sessionStep = new ChoreographerSessionStep();
		sessionStep.setId(10);
		sessionStep.setExecutor(oldExecutor);

		final ChoreographerExecutor newExecutor = new ChoreographerExecutor();
		newExecutor.setId(2);
		final SessionExecutorCache cache = new SessionExecutorCache();
		cache.put("service", 1, 2, new ExecutorData(newExecutor, List.of()));
		
		when(planDBService.getPlanById(2)).thenReturn(plan);
		doNothing().when(sessionDBService).worklog(any(String.class), anyLong(), anyString(), isNull());
		when(sessionDBService.changeSessionStatus(eq(1L), eq(ChoreographerSessionStatus.RUNNING), isNull())).thenReturn(new ChoreographerSession());
		when(planDBService.collectStepsFromPlan(any(ChoreographerPlan.class))).thenReturn(List.of(step));
		when(sessionDataStorage.put(eq(1L), any(SessionExecutorCache.class))).thenReturn(null);
		when(executorSelector.selectAndInit(eq(1L), any(ChoreographerStep.class), anySet(), eq(true))).thenReturn(new ExecutorData(oldExecutor, List.of()));
		when(planDBService.getFirstSteps(any(ChoreographerAction.class))).thenReturn(List.of(step));
		when(sessionDBService.changeSessionStepStatus(eq(1L), eq(step), eq(ChoreographerSessionStepStatus.RUNNING), anyString())).thenReturn(sessionStep);
		when(driver.queryOrchestrator(any(OrchestrationFormRequestDTO.class))).thenReturn(new OrchestrationResponseDTO(List.of(new OrchestrationResultDTO())));
		when(sessionDataStorage.get(eq(1L))).thenReturn(cache);
		when(sessionDBService.changeSessionStepExecutor(10L, 2L)).thenThrow(new ArrowheadException("early end")); // just for finish the test earlier
		
		try {
			testObject.receiveStartSessionMessage(payload);
		} catch (final Exception ex) {
			verify(planDBService, times(1)).getPlanById(2);
			verify(sessionDBService, times(2)).worklog(any(String.class), anyLong(), anyString(), isNull());
			verify(sessionDBService, times(1)).changeSessionStatus(eq(1L), eq(ChoreographerSessionStatus.RUNNING), isNull());
			verify(planDBService, times(1)).collectStepsFromPlan(any(ChoreographerPlan.class));
			verify(sessionDataStorage, times(1)).put(eq(1L), any(SessionExecutorCache.class));
			verify(executorSelector, times(1)).selectAndInit(eq(1L), any(ChoreographerStep.class), anySet(), eq(true));
			verify(sessionDBService, never()).registerSessionStep(eq(1L), anyLong(), anyLong());
			verify(planDBService, times(1)).getFirstSteps(any(ChoreographerAction.class));
			verify(sessionDBService, times(1)).changeSessionStepStatus(eq(1L), eq(step), eq(ChoreographerSessionStepStatus.RUNNING), anyString());
			verify(driver, times(1)).queryOrchestrator(any(OrchestrationFormRequestDTO.class));
			verify(sessionDataStorage, times(1)).get(eq(1L));
			verify(sessionDBService, times(1)).changeSessionStepExecutor(10L, 2L);

			Assert.assertEquals(1L, ((ChoreographerSessionException)ex).getSessionId());
			Assert.assertEquals("early end", ex.getCause().getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ChoreographerSessionException.class)
	public void testReceiveStartSessionMessagePreconditionOrchestrationFailedNoReplacement() {
		final ChoreographerStartSessionDTO payload = new ChoreographerStartSessionDTO(1, 2);
		final ChoreographerPlan plan = new ChoreographerPlan("plan");
		final ChoreographerAction action = new ChoreographerAction("action", null);
		plan.setFirstAction(action);
		action.setPlan(plan);
		final ChoreographerStep step = new ChoreographerStep();
		step.setName("step");
		step.setAction(action);
		step.setServiceDefinition("service");
		step.setMinVersion(1);
		step.setMaxVersion(2);
		
		final ChoreographerSession session = new ChoreographerSession();
		session.setId(1);
		final ChoreographerExecutor executor = new ChoreographerExecutor();
		executor.setId(1);
		final ChoreographerSessionStep sessionStep = new ChoreographerSessionStep();
		sessionStep.setId(10);
		sessionStep.setSession(session);
		sessionStep.setExecutor(executor);

		final SessionExecutorCache cache = new SessionExecutorCache();
		final ExecutorData executorData = new ExecutorData(executor, List.of(new ServiceQueryFormDTO()));
		cache.put("service", 1, 2, executorData);
		
		when(planDBService.getPlanById(2)).thenReturn(plan);
		doNothing().when(sessionDBService).worklog(any(String.class), anyLong(), anyString(), isNull());
		when(sessionDBService.changeSessionStatus(eq(1L), eq(ChoreographerSessionStatus.RUNNING), isNull())).thenReturn(session);
		when(planDBService.collectStepsFromPlan(any(ChoreographerPlan.class))).thenReturn(List.of(step));
		when(sessionDataStorage.put(eq(1L), any(SessionExecutorCache.class))).thenReturn(null);
		when(executorSelector.selectAndInit(eq(1L), any(ChoreographerStep.class), anySet(), eq(true))).thenReturn(executorData);
		when(planDBService.getFirstSteps(any(ChoreographerAction.class))).thenReturn(List.of(step));
		when(sessionDBService.changeSessionStepStatus(eq(1L), eq(step), eq(ChoreographerSessionStepStatus.RUNNING), anyString())).thenReturn(sessionStep);
		when(driver.queryOrchestrator(any(OrchestrationFormRequestDTO.class))).thenReturn(new OrchestrationResponseDTO(List.of(new OrchestrationResultDTO()))).thenThrow(new ArrowheadException("orch problem"));
		when(sessionDataStorage.get(eq(1L))).thenReturn(cache);
		when(executorSelector.selectAndInit(eq(1L), any(ChoreographerStep.class), anySet(), eq(false))).thenReturn(null);
		
		try {
			testObject.receiveStartSessionMessage(payload);
		} catch (final Exception ex) {
			verify(planDBService, times(1)).getPlanById(2);
			verify(sessionDBService, times(2)).worklog(any(String.class), anyLong(), anyString(), isNull());
			verify(sessionDBService, times(1)).changeSessionStatus(eq(1L), eq(ChoreographerSessionStatus.RUNNING), isNull());
			verify(planDBService, times(1)).collectStepsFromPlan(any(ChoreographerPlan.class));
			verify(sessionDataStorage, times(1)).put(eq(1L), any(SessionExecutorCache.class));
			verify(executorSelector, times(1)).selectAndInit(eq(1L), any(ChoreographerStep.class), anySet(), eq(true));
			verify(sessionDBService, never()).registerSessionStep(eq(1L), anyLong(), anyLong());
			verify(planDBService, times(1)).getFirstSteps(any(ChoreographerAction.class));
			verify(sessionDBService, times(1)).changeSessionStepStatus(eq(1L), eq(step), eq(ChoreographerSessionStepStatus.RUNNING), anyString());
			verify(driver, times(2)).queryOrchestrator(any(OrchestrationFormRequestDTO.class));
			verify(sessionDataStorage, times(2)).get(eq(1L));
			verify(sessionDBService, never()).changeSessionStepExecutor(eq(10L), anyLong());
			verify(executorSelector, times(1)).selectAndInit(eq(1L), any(ChoreographerStep.class), anySet(), eq(false));

			Assert.assertEquals(0, cache.getExecutorCache().size());
			Assert.assertEquals(1, cache.getExclusions().size());
			Assert.assertEquals((Long) 1L, (Long) cache.getExclusions().iterator().next());
			
			Assert.assertEquals(1L, ((ChoreographerSessionException)ex).getSessionId());
			Assert.assertEquals((Long) 10L, ((ChoreographerSessionException)ex).getSessionStepId());
			Assert.assertEquals("Can't find properly working executor for step: plan.action.step", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ChoreographerSessionException.class)
	public void testReceiveStartSessionMessagePreconditionOrchestrationNoProviderNoReplacement() { 
		final ChoreographerStartSessionDTO payload = new ChoreographerStartSessionDTO(1, 2);
		final ChoreographerPlan plan = new ChoreographerPlan("plan");
		final ChoreographerAction action = new ChoreographerAction("action", null);
		plan.setFirstAction(action);
		action.setPlan(plan);
		final ChoreographerStep step = new ChoreographerStep();
		step.setName("step");
		step.setAction(action);
		step.setServiceDefinition("service");
		step.setMinVersion(1);
		step.setMaxVersion(2);
		
		final ChoreographerSession session = new ChoreographerSession();
		session.setId(1);
		final ChoreographerExecutor executor = new ChoreographerExecutor();
		executor.setId(1);
		final ChoreographerSessionStep sessionStep = new ChoreographerSessionStep();
		sessionStep.setId(10);
		sessionStep.setSession(session);
		sessionStep.setExecutor(executor);

		final SessionExecutorCache cache = new SessionExecutorCache();
		final ExecutorData executorData = new ExecutorData(executor, List.of(new ServiceQueryFormDTO()));
		cache.put("service", 1, 2, executorData);
		
		when(planDBService.getPlanById(2)).thenReturn(plan);
		doNothing().when(sessionDBService).worklog(any(String.class), anyLong(), anyString(), isNull());
		when(sessionDBService.changeSessionStatus(eq(1L), eq(ChoreographerSessionStatus.RUNNING), isNull())).thenReturn(session);
		when(planDBService.collectStepsFromPlan(any(ChoreographerPlan.class))).thenReturn(List.of(step));
		when(sessionDataStorage.put(eq(1L), any(SessionExecutorCache.class))).thenReturn(null);
		when(executorSelector.selectAndInit(eq(1L), any(ChoreographerStep.class), anySet(), eq(true))).thenReturn(executorData);
		when(planDBService.getFirstSteps(any(ChoreographerAction.class))).thenReturn(List.of(step));
		when(sessionDBService.changeSessionStepStatus(eq(1L), eq(step), eq(ChoreographerSessionStepStatus.RUNNING), anyString())).thenReturn(sessionStep);
		when(driver.queryOrchestrator(any(OrchestrationFormRequestDTO.class))).thenReturn(new OrchestrationResponseDTO(List.of(new OrchestrationResultDTO())), new OrchestrationResponseDTO(List.of()));
		when(sessionDataStorage.get(eq(1L))).thenReturn(cache);
		when(executorSelector.selectAndInit(eq(1L), any(ChoreographerStep.class), anySet(), eq(false))).thenReturn(null);
		
		try {
			testObject.receiveStartSessionMessage(payload);
		} catch (final Exception ex) {
			verify(planDBService, times(1)).getPlanById(2);
			verify(sessionDBService, times(2)).worklog(any(String.class), anyLong(), anyString(), isNull());
			verify(sessionDBService, times(1)).changeSessionStatus(eq(1L), eq(ChoreographerSessionStatus.RUNNING), isNull());
			verify(planDBService, times(1)).collectStepsFromPlan(any(ChoreographerPlan.class));
			verify(sessionDataStorage, times(1)).put(eq(1L), any(SessionExecutorCache.class));
			verify(executorSelector, times(1)).selectAndInit(eq(1L), any(ChoreographerStep.class), anySet(), eq(true));
			verify(sessionDBService, never()).registerSessionStep(eq(1L), anyLong(), anyLong());
			verify(planDBService, times(1)).getFirstSteps(any(ChoreographerAction.class));
			verify(sessionDBService, times(1)).changeSessionStepStatus(eq(1L), eq(step), eq(ChoreographerSessionStepStatus.RUNNING), anyString());
			verify(driver, times(2)).queryOrchestrator(any(OrchestrationFormRequestDTO.class));
			verify(sessionDataStorage, times(2)).get(eq(1L));
			verify(sessionDBService, never()).changeSessionStepExecutor(eq(10L), anyLong());
			verify(executorSelector, times(1)).selectAndInit(eq(1L), any(ChoreographerStep.class), anySet(), eq(false));

			Assert.assertEquals(0, cache.getExecutorCache().size());
			Assert.assertEquals(1, cache.getExclusions().size());
			Assert.assertEquals((Long) 1L, (Long) cache.getExclusions().iterator().next());
			
			Assert.assertEquals(1L, ((ChoreographerSessionException)ex).getSessionId());
			Assert.assertEquals((Long) 10L, ((ChoreographerSessionException)ex).getSessionStepId());
			Assert.assertEquals("Can't find properly working executor for step: plan.action.step", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ChoreographerSessionException.class)
	public void testReceiveStartSessionMessagePreconditionOrchestrationNoProviderThenUsingReplacement() { 
		final ChoreographerStartSessionDTO payload = new ChoreographerStartSessionDTO(1, 2);
		final ChoreographerPlan plan = new ChoreographerPlan("plan");
		final ChoreographerAction action = new ChoreographerAction("action", null);
		plan.setFirstAction(action);
		action.setPlan(plan);
		final ChoreographerStep step = new ChoreographerStep();
		step.setName("step");
		step.setAction(action);
		step.setServiceDefinition("service");
		step.setMinVersion(1);
		step.setMaxVersion(2);
		
		final ChoreographerSession session = new ChoreographerSession();
		session.setId(1);
		final ChoreographerExecutor executor = new ChoreographerExecutor();
		executor.setId(1);
		final ChoreographerSessionStep sessionStep = new ChoreographerSessionStep();
		sessionStep.setId(10);
		sessionStep.setSession(session);
		sessionStep.setExecutor(executor);

		final SessionExecutorCache cache = new SessionExecutorCache();
		final ExecutorData executorData = new ExecutorData(executor, List.of(new ServiceQueryFormDTO()));
		cache.put("service", 1, 2, executorData);
		final ChoreographerExecutor executor2 = new ChoreographerExecutor();
		executor2.setId(2);
		executor2.setName("executor2");
		executor2.setAddress("localhost");
		executor2.setPort(1234);
		final ExecutorData executorData2 = new ExecutorData(executor2, List.of(new ServiceQueryFormDTO()));

		final OrchestrationResultDTO orchResult = new OrchestrationResultDTO();
		orchResult.setAuthorizationTokens(Map.of("key", "value"));
		
		when(planDBService.getPlanById(2)).thenReturn(plan);
		doNothing().when(sessionDBService).worklog(any(String.class), anyLong(), anyString(), isNull());
		when(sessionDBService.changeSessionStatus(eq(1L), eq(ChoreographerSessionStatus.RUNNING), isNull())).thenReturn(session);
		when(planDBService.collectStepsFromPlan(any(ChoreographerPlan.class))).thenReturn(List.of(step));
		when(sessionDataStorage.put(eq(1L), any(SessionExecutorCache.class))).thenReturn(null);
		when(executorSelector.selectAndInit(eq(1L), any(ChoreographerStep.class), anySet(), eq(true))).thenReturn(executorData);
		when(planDBService.getFirstSteps(any(ChoreographerAction.class))).thenReturn(List.of(step));
		when(sessionDBService.changeSessionStepStatus(eq(1L), eq(step), eq(ChoreographerSessionStepStatus.RUNNING), anyString())).thenReturn(sessionStep);
		when(driver.queryOrchestrator(any(OrchestrationFormRequestDTO.class))).thenReturn(new OrchestrationResponseDTO(List.of(new OrchestrationResultDTO())), new OrchestrationResponseDTO(List.of()), new OrchestrationResponseDTO(List.of(orchResult)));
		when(sessionDataStorage.get(eq(1L))).thenReturn(cache);
		when(executorSelector.selectAndInit(eq(1L), any(ChoreographerStep.class), anySet(), eq(false))).thenReturn(executorData2);
		when(sessionDBService.changeSessionStepExecutor(10L, 2L)).thenReturn(sessionStep);
		when(driver.queryServiceRegistryBySystem(anyString(), anyString(), anyInt())).thenThrow(new ArrowheadException("early end")); // just for finishing the test early
		
		try {
			testObject.receiveStartSessionMessage(payload);
		} catch (final Exception ex) {
			verify(planDBService, times(1)).getPlanById(2);
			verify(sessionDBService, times(2)).worklog(any(String.class), anyLong(), anyString(), isNull());
			verify(sessionDBService, times(1)).changeSessionStatus(eq(1L), eq(ChoreographerSessionStatus.RUNNING), isNull());
			verify(planDBService, times(1)).collectStepsFromPlan(any(ChoreographerPlan.class));
			verify(sessionDataStorage, times(1)).put(eq(1L), any(SessionExecutorCache.class));
			verify(executorSelector, times(1)).selectAndInit(eq(1L), any(ChoreographerStep.class), anySet(), eq(true));
			verify(sessionDBService, never()).registerSessionStep(eq(1L), anyLong(), anyLong());
			verify(planDBService, times(1)).getFirstSteps(any(ChoreographerAction.class));
			verify(sessionDBService, times(1)).changeSessionStepStatus(eq(1L), eq(step), eq(ChoreographerSessionStepStatus.RUNNING), anyString());
			verify(driver, times(3)).queryOrchestrator(any(OrchestrationFormRequestDTO.class));
			verify(sessionDataStorage, times(2)).get(eq(1L));
			verify(executorSelector, times(1)).selectAndInit(eq(1L), any(ChoreographerStep.class), anySet(), eq(false));
			verify(sessionDBService, times(1)).changeSessionStepExecutor(10L, 2L);
			verify(driver, times(1)).queryServiceRegistryBySystem(anyString(), anyString(), anyInt());

			Assert.assertEquals(1, cache.getExecutorCache().size());
			Assert.assertEquals(1, cache.getExclusions().size());
			Assert.assertEquals((Long) 1L, (Long) cache.getExclusions().iterator().next());
			
			Assert.assertEquals(1L, ((ChoreographerSessionException)ex).getSessionId());
			Assert.assertEquals("early end", ex.getCause().getMessage());
			
			throw ex;
		}
	}
	
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testReceiveStartSessionMessageOkNoTokens() { 
		final ChoreographerStartSessionDTO payload = new ChoreographerStartSessionDTO(1, 2);
		final ChoreographerPlan plan = new ChoreographerPlan("plan");
		final ChoreographerAction action = new ChoreographerAction("action", null);
		plan.setFirstAction(action);
		action.setPlan(plan);
		final ChoreographerStep step = new ChoreographerStep();
		step.setName("step");
		step.setAction(action);
		step.setServiceDefinition("service");
		step.setMinVersion(1);
		step.setMaxVersion(2);
		
		final ChoreographerSession session = new ChoreographerSession();
		session.setId(1);
		final ChoreographerExecutor executor = new ChoreographerExecutor();
		executor.setId(1);
		executor.setName("executor");
		executor.setAddress("localhost");
		executor.setPort(1234);
		executor.setBaseUri("");

		final ChoreographerSessionStep sessionStep = new ChoreographerSessionStep();
		sessionStep.setId(10);
		sessionStep.setSession(session);
		sessionStep.setExecutor(executor);

		final SessionExecutorCache cache = new SessionExecutorCache();
		final ExecutorData executorData = new ExecutorData(executor, List.of());
		cache.put("service", 1, 2, executorData);

		when(planDBService.getPlanById(2)).thenReturn(plan);
		doNothing().when(sessionDBService).worklog(any(String.class), anyLong(), anyString(), isNull());
		when(sessionDBService.changeSessionStatus(eq(1L), eq(ChoreographerSessionStatus.RUNNING), isNull())).thenReturn(session);
		when(planDBService.collectStepsFromPlan(any(ChoreographerPlan.class))).thenReturn(List.of(step));
		when(sessionDataStorage.put(eq(1L), any(SessionExecutorCache.class))).thenReturn(null);
		when(executorSelector.selectAndInit(eq(1L), any(ChoreographerStep.class), anySet(), eq(true))).thenReturn(executorData);
		when(planDBService.getFirstSteps(any(ChoreographerAction.class))).thenReturn(List.of(step));
		when(sessionDBService.changeSessionStepStatus(eq(1L), eq(step), eq(ChoreographerSessionStepStatus.RUNNING), anyString())).thenReturn(sessionStep);
		when(driver.queryOrchestrator(any(OrchestrationFormRequestDTO.class))).thenReturn(new OrchestrationResponseDTO(List.of(new OrchestrationResultDTO())));
		when(sessionDataStorage.get(eq(1L))).thenReturn(cache);
		doNothing().when(driver).startExecutor(anyString(), anyInt(), anyString(), any(ChoreographerExecuteStepRequestDTO.class));
		
		testObject.receiveStartSessionMessage(payload);
		
		verify(planDBService, times(1)).getPlanById(2);
		verify(sessionDBService, times(2)).worklog(any(String.class), anyLong(), anyString(), isNull());
		verify(sessionDBService, times(1)).changeSessionStatus(eq(1L), eq(ChoreographerSessionStatus.RUNNING), isNull());
		verify(planDBService, times(1)).collectStepsFromPlan(any(ChoreographerPlan.class));
		verify(sessionDataStorage, times(1)).put(eq(1L), any(SessionExecutorCache.class));
		verify(executorSelector, times(1)).selectAndInit(eq(1L), any(ChoreographerStep.class), anySet(), eq(true));
		verify(sessionDBService, never()).registerSessionStep(eq(1L), anyLong(), anyLong());
		verify(planDBService, times(1)).getFirstSteps(any(ChoreographerAction.class));
		verify(sessionDBService, times(1)).changeSessionStepStatus(eq(1L), eq(step), eq(ChoreographerSessionStepStatus.RUNNING), anyString());
		verify(driver, times(1)).queryOrchestrator(any(OrchestrationFormRequestDTO.class));
		verify(sessionDataStorage, times(2)).get(eq(1L));
		verify(sessionDBService, never()).changeSessionStepExecutor(eq(10L), anyLong());
		verify(driver, never()).queryServiceRegistryBySystem(anyString(), anyString(), anyInt());
		verify(driver, never()).generateMultiServiceAuthorizationTokens(anyList());
		verify(driver, times(1)).startExecutor(anyString(), anyInt(), anyString(), any(ChoreographerExecuteStepRequestDTO.class));
		
		Assert.assertEquals(1, cache.getExecutorCache().size());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ChoreographerSessionException.class)
	public void testReceiveStartSessionMessageNewTokenNotFound() { 
		final ChoreographerStartSessionDTO payload = new ChoreographerStartSessionDTO(1, 2);
		final ChoreographerPlan plan = new ChoreographerPlan("plan");
		final ChoreographerAction action = new ChoreographerAction("action", null);
		plan.setFirstAction(action);
		action.setPlan(plan);
		final ChoreographerStep step = new ChoreographerStep();
		step.setName("step");
		step.setAction(action);
		step.setServiceDefinition("service");
		step.setMinVersion(1);
		step.setMaxVersion(2);
		
		final ChoreographerSession session = new ChoreographerSession();
		session.setId(1);
		final ChoreographerExecutor executor = new ChoreographerExecutor();
		executor.setId(1);
		executor.setName("executor");
		executor.setAddress("localhost");
		executor.setPort(1234);
		executor.setBaseUri("");

		final ChoreographerSessionStep sessionStep = new ChoreographerSessionStep();
		sessionStep.setId(10);
		sessionStep.setSession(session);
		sessionStep.setExecutor(executor);

		final SessionExecutorCache cache = new SessionExecutorCache();
		final ExecutorData executorData = new ExecutorData(executor, List.of());
		cache.put("service", 1, 2, executorData);

		final SystemResponseDTO provider = new SystemResponseDTO();
		provider.setId(14);
		provider.setSystemName("provider");
		provider.setAddress("localhost");
		provider.setPort(1235);
		provider.setAuthenticationInfo("ijkl");
		
		final OrchestrationResultDTO orchResult = new OrchestrationResultDTO();
		orchResult.setService(new ServiceDefinitionResponseDTO(22, "service", null, null));
		orchResult.setProvider(provider);
		orchResult.setAuthorizationTokens(Map.of("HTTP-SECURE-JSON", "abcd"));
		
		final SystemResponseDTO executorSystem = provider;
		executorSystem.setId(12);
		executorSystem.setSystemName("executor");
		executorSystem.setAddress("localhost");
		executorSystem.setPort(1234);
		executorSystem.setAuthenticationInfo("efgh");
		
		when(planDBService.getPlanById(2)).thenReturn(plan);
		doNothing().when(sessionDBService).worklog(any(String.class), anyLong(), anyString(), isNull());
		when(sessionDBService.changeSessionStatus(eq(1L), eq(ChoreographerSessionStatus.RUNNING), isNull())).thenReturn(session);
		when(planDBService.collectStepsFromPlan(any(ChoreographerPlan.class))).thenReturn(List.of(step));
		when(sessionDataStorage.put(eq(1L), any(SessionExecutorCache.class))).thenReturn(null);
		when(executorSelector.selectAndInit(eq(1L), any(ChoreographerStep.class), anySet(), eq(true))).thenReturn(executorData);
		when(planDBService.getFirstSteps(any(ChoreographerAction.class))).thenReturn(List.of(step));
		when(sessionDBService.changeSessionStepStatus(eq(1L), eq(step), eq(ChoreographerSessionStepStatus.RUNNING), anyString())).thenReturn(sessionStep);
		when(driver.queryOrchestrator(any(OrchestrationFormRequestDTO.class))).thenReturn(new OrchestrationResponseDTO(List.of(orchResult)));
		when(sessionDataStorage.get(eq(1L))).thenReturn(cache);
		when(driver.queryServiceRegistryBySystem("executor", "localhost", 1234)).thenReturn(executorSystem);
		when(driver.generateMultiServiceAuthorizationTokens(anyList())).thenReturn(new TokenGenerationMultiServiceResponseDTO(List.of()));
		
		try {
			testObject.receiveStartSessionMessage(payload);
		} catch (final Exception ex) {
			verify(planDBService, times(1)).getPlanById(2);
			verify(sessionDBService, times(2)).worklog(any(String.class), anyLong(), anyString(), isNull());
			verify(sessionDBService, times(1)).changeSessionStatus(eq(1L), eq(ChoreographerSessionStatus.RUNNING), isNull());
			verify(planDBService, times(1)).collectStepsFromPlan(any(ChoreographerPlan.class));
			verify(sessionDataStorage, times(1)).put(eq(1L), any(SessionExecutorCache.class));
			verify(executorSelector, times(1)).selectAndInit(eq(1L), any(ChoreographerStep.class), anySet(), eq(true));
			verify(sessionDBService, never()).registerSessionStep(eq(1L), anyLong(), anyLong());
			verify(planDBService, times(1)).getFirstSteps(any(ChoreographerAction.class));
			verify(sessionDBService, times(1)).changeSessionStepStatus(eq(1L), eq(step), eq(ChoreographerSessionStepStatus.RUNNING), anyString());
			verify(driver, times(1)).queryOrchestrator(any(OrchestrationFormRequestDTO.class));
			verify(sessionDataStorage, times(2)).get(eq(1L));
			verify(sessionDBService, never()).changeSessionStepExecutor(eq(10L), anyLong());
			verify(driver, times(1)).queryServiceRegistryBySystem("executor", "localhost", 1234);
			verify(driver, times(1)).generateMultiServiceAuthorizationTokens(anyList());
			verify(driver, never()).startExecutor(anyString(), anyInt(), anyString(), any(ChoreographerExecuteStepRequestDTO.class));
			
			Assert.assertEquals(1L, ((ChoreographerSessionException)ex).getSessionId());
			Assert.assertEquals("Missing regenerated token at step plan.action.step for service service", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testReceiveStartSessionMessageOkWithToken() { 
		final ChoreographerStartSessionDTO payload = new ChoreographerStartSessionDTO(1, 2);
		final ChoreographerPlan plan = new ChoreographerPlan("plan");
		final ChoreographerAction action = new ChoreographerAction("action", null);
		plan.setFirstAction(action);
		action.setPlan(plan);
		final ChoreographerStep step = new ChoreographerStep();
		step.setName("step");
		step.setAction(action);
		step.setServiceDefinition("service");
		step.setMinVersion(1);
		step.setMaxVersion(2);
		
		final ChoreographerSession session = new ChoreographerSession();
		session.setId(1);
		final ChoreographerExecutor executor = new ChoreographerExecutor();
		executor.setId(1);
		executor.setName("executor");
		executor.setAddress("localhost");
		executor.setPort(1234);
		executor.setBaseUri("");

		final ChoreographerSessionStep sessionStep = new ChoreographerSessionStep();
		sessionStep.setId(10);
		sessionStep.setSession(session);
		sessionStep.setExecutor(executor);

		final SessionExecutorCache cache = new SessionExecutorCache();
		final ExecutorData executorData = new ExecutorData(executor, List.of());
		cache.put("service", 1, 2, executorData);

		final SystemResponseDTO provider = new SystemResponseDTO();
		provider.setId(14);
		provider.setSystemName("provider");
		provider.setAddress("localhost");
		provider.setPort(1235);
		provider.setAuthenticationInfo("ijkl");
		
		final OrchestrationResultDTO orchResult = new OrchestrationResultDTO();
		orchResult.setService(new ServiceDefinitionResponseDTO(22, "service", null, null));
		orchResult.setProvider(provider);
		orchResult.setAuthorizationTokens(Map.of("HTTP-SECURE-JSON", "abcd"));
		
		final SystemResponseDTO executorSystem = new SystemResponseDTO();
		executorSystem.setId(12);
		executorSystem.setSystemName("executor");
		executorSystem.setAddress("localhost");
		executorSystem.setPort(1234);
		executorSystem.setAuthenticationInfo("efgh");
		
		final TokenDataDTO data = new TokenDataDTO();
		data.setProviderName("provider");
		data.setProviderAddress("localhost");
		data.setProviderPort(1235);
		data.setTokens(Map.of("HTTP-SECURE-JSON", "new token"));
		final TokenGenerationDetailedResponseDTO tokenDetails = new TokenGenerationDetailedResponseDTO();
		tokenDetails.setService("service");
		tokenDetails.setTokenData(List.of(data));
		
		when(planDBService.getPlanById(2)).thenReturn(plan);
		doNothing().when(sessionDBService).worklog(any(String.class), anyLong(), anyString(), isNull());
		when(sessionDBService.changeSessionStatus(eq(1L), eq(ChoreographerSessionStatus.RUNNING), isNull())).thenReturn(session);
		when(planDBService.collectStepsFromPlan(any(ChoreographerPlan.class))).thenReturn(List.of(step));
		when(sessionDataStorage.put(eq(1L), any(SessionExecutorCache.class))).thenReturn(null);
		when(executorSelector.selectAndInit(eq(1L), any(ChoreographerStep.class), anySet(), eq(true))).thenReturn(executorData);
		when(planDBService.getFirstSteps(any(ChoreographerAction.class))).thenReturn(List.of(step));
		when(sessionDBService.changeSessionStepStatus(eq(1L), eq(step), eq(ChoreographerSessionStepStatus.RUNNING), anyString())).thenReturn(sessionStep);
		when(driver.queryOrchestrator(any(OrchestrationFormRequestDTO.class))).thenReturn(new OrchestrationResponseDTO(List.of(orchResult)));
		when(sessionDataStorage.get(eq(1L))).thenReturn(cache);
		when(driver.queryServiceRegistryBySystem("executor", "localhost", 1234)).thenReturn(executorSystem);
		when(driver.generateMultiServiceAuthorizationTokens(anyList())).thenReturn(new TokenGenerationMultiServiceResponseDTO(List.of(tokenDetails)));
		doNothing().when(driver).startExecutor(anyString(), anyInt(), anyString(), any(ChoreographerExecuteStepRequestDTO.class));
		
		testObject.receiveStartSessionMessage(payload);
		
		verify(planDBService, times(1)).getPlanById(2);
		verify(sessionDBService, times(2)).worklog(any(String.class), anyLong(), anyString(), isNull());
		verify(sessionDBService, times(1)).changeSessionStatus(eq(1L), eq(ChoreographerSessionStatus.RUNNING), isNull());
		verify(planDBService, times(1)).collectStepsFromPlan(any(ChoreographerPlan.class));
		verify(sessionDataStorage, times(1)).put(eq(1L), any(SessionExecutorCache.class));
		verify(executorSelector, times(1)).selectAndInit(eq(1L), any(ChoreographerStep.class), anySet(), eq(true));
		verify(sessionDBService, never()).registerSessionStep(eq(1L), anyLong(), anyLong());
		verify(planDBService, times(1)).getFirstSteps(any(ChoreographerAction.class));
		verify(sessionDBService, times(1)).changeSessionStepStatus(eq(1L), eq(step), eq(ChoreographerSessionStepStatus.RUNNING), anyString());
		verify(driver, times(1)).queryOrchestrator(any(OrchestrationFormRequestDTO.class));
		verify(sessionDataStorage, times(2)).get(eq(1L));
		verify(sessionDBService, never()).changeSessionStepExecutor(eq(10L), anyLong());
		verify(driver, times(1)).queryServiceRegistryBySystem("executor", "localhost", 1234);
		verify(driver, times(1)).generateMultiServiceAuthorizationTokens(anyList());
		
		final ArgumentCaptor<ChoreographerExecuteStepRequestDTO> captor = ArgumentCaptor.forClass(ChoreographerExecuteStepRequestDTO.class);
		verify(driver, times(1)).startExecutor(anyString(), anyInt(), anyString(), captor.capture());
		
		Assert.assertEquals(1, cache.getExecutorCache().size());
		final ChoreographerExecuteStepRequestDTO executorPayload = captor.getValue();
		Assert.assertEquals("new token", executorPayload.getMainOrchestrationResult().getAuthorizationTokens().get("HTTP-SECURE-JSON"));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testReceiveSessionStepDoneMessagePayloadNull() {
		try {
			testObject.receiveSessionStepDoneMessage(null);
		} catch (final Exception ex) {
			Assert.assertEquals("Payload is null.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testReceiveSessionStepDoneMessageSessionIdNull() {
		try {
			testObject.receiveSessionStepDoneMessage(new ChoreographerExecutedStepResultDTO());
		} catch (final Exception ex) {
			Assert.assertEquals("Invalid session id.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testReceiveSessionStepDoneMessageSessionIdInvalid() {
		try {
			final ChoreographerExecutedStepResultDTO payload = new ChoreographerExecutedStepResultDTO();
			payload.setSessionId(-1L);
			
			testObject.receiveSessionStepDoneMessage(payload);
		} catch (final Exception ex) {
			Assert.assertEquals("Invalid session id.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testReceiveSessionStepDoneMessageSessionStepIdNull() {
		try {
			final ChoreographerExecutedStepResultDTO payload = new ChoreographerExecutedStepResultDTO();
			payload.setSessionId(1L);
			
			testObject.receiveSessionStepDoneMessage(payload);
		} catch (final Exception ex) {
			Assert.assertEquals("Invalid session step id.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testReceiveSessionStepDoneMessageSessionStepIdInvalid() {
		try {
			final ChoreographerExecutedStepResultDTO payload = new ChoreographerExecutedStepResultDTO();
			payload.setSessionId(1L);
			payload.setSessionStepId(-1L);
			
			testObject.receiveSessionStepDoneMessage(payload);
		} catch (final Exception ex) {
			Assert.assertEquals("Invalid session step id.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//TODO: continue (line 444)
}