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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;

import eu.arrowhead.common.database.entity.ChoreographerAction;
import eu.arrowhead.common.database.entity.ChoreographerExecutor;
import eu.arrowhead.common.database.entity.ChoreographerPlan;
import eu.arrowhead.common.database.entity.ChoreographerSession;
import eu.arrowhead.common.database.entity.ChoreographerSessionStep;
import eu.arrowhead.common.database.entity.ChoreographerStep;
import eu.arrowhead.common.database.entity.ChoreographerStepNextStepConnection;
import eu.arrowhead.common.dto.internal.ChoreographerSessionStepStatus;
import eu.arrowhead.common.dto.internal.ChoreographerStartSessionDTO;
import eu.arrowhead.common.dto.shared.ChoreographerAbortStepRequestDTO;
import eu.arrowhead.common.dto.shared.ChoreographerExecuteStepRequestDTO;
import eu.arrowhead.common.dto.shared.ChoreographerExecutedStepResultDTO;
import eu.arrowhead.common.dto.shared.ChoreographerExecutedStepStatus;
import eu.arrowhead.common.dto.shared.ChoreographerNotificationDTO;
import eu.arrowhead.common.dto.shared.ChoreographerServiceQueryFormDTO;
import eu.arrowhead.common.dto.shared.ChoreographerSessionStatus;
import eu.arrowhead.common.dto.shared.OrchestrationFormRequestDTO;
import eu.arrowhead.common.dto.shared.OrchestrationResponseDTO;
import eu.arrowhead.common.dto.shared.OrchestrationResultDTO;
import eu.arrowhead.common.dto.shared.OrchestratorWarnings;
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

	//=================================================================================================
	// methods
	
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
		final ChoreographerStartSessionDTO payload = new ChoreographerStartSessionDTO(1, 2, false, false);

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
		final ChoreographerStartSessionDTO payload = new ChoreographerStartSessionDTO(1, 2, false, false);
		final ChoreographerPlan plan = new ChoreographerPlan("plan");
		final ChoreographerAction action = new ChoreographerAction("action", null);
		action.setPlan(plan);
		final ChoreographerStep step = new ChoreographerStep();
		step.setName("step");
		step.setAction(action);
		step.setServiceDefinition("service");
		step.setMinVersion(1);
		step.setMaxVersion(2);
		final ChoreographerSession session = new ChoreographerSession();
		session.setId(1L);
		session.setPlan(plan);
		
		when(planDBService.getPlanById(2)).thenReturn(plan);
		when(sessionDBService.changeSessionStatus(eq(1L), eq(ChoreographerSessionStatus.RUNNING), isNull())).thenReturn(session);
		doNothing().when(sessionDBService).worklog(any(String.class), anyLong(), anyLong(), anyString(), isNull());
		when(sessionDBService.increaseExecutionNumber(eq(1L))).thenReturn(session);
		when(planDBService.collectStepsFromPlan(anyLong())).thenReturn(List.of(step));
		when(sessionDataStorage.put(eq(1L), any(SessionExecutorCache.class))).thenReturn(null);
		when(executorSelector.selectAndInit(eq(1L), any(ChoreographerStep.class), anySet(), eq(false), eq(false), eq(true))).thenReturn(null);
		
		try {
			testObject.receiveStartSessionMessage(payload);
		} catch (final Exception ex) {
			verify(planDBService, times(1)).getPlanById(2);
			verify(sessionDBService, times(1)).changeSessionStatus(eq(1L), eq(ChoreographerSessionStatus.RUNNING), isNull());
			verify(sessionDBService, times(1)).worklog(any(String.class), anyLong(), anyLong(), anyString(), isNull());
			verify(sessionDBService, times(1)).increaseExecutionNumber(eq(1L));
			verify(planDBService, times(1)).collectStepsFromPlan(anyLong());
			verify(sessionDataStorage, times(1)).put(eq(1L), any(SessionExecutorCache.class));
			verify(executorSelector, times(1)).selectAndInit(eq(1L), any(ChoreographerStep.class), anySet(), eq(false), eq(false), eq(true));
			
			Assert.assertEquals("Can't find properly working executor for step: plan.action.step", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ChoreographerSessionException.class)
	public void testReceiveStartSessionMessageExecutorNotFoundWithTrueFlags() {
		final ChoreographerStartSessionDTO payload = new ChoreographerStartSessionDTO(1, 2, true, true);
		final ChoreographerPlan plan = new ChoreographerPlan("plan");
		final ChoreographerAction action = new ChoreographerAction("action", null);
		action.setPlan(plan);
		final ChoreographerStep step = new ChoreographerStep();
		step.setName("step");
		step.setAction(action);
		step.setServiceDefinition("service");
		step.setMinVersion(1);
		step.setMaxVersion(2);
		final ChoreographerSession session = new ChoreographerSession();
		session.setId(1L);
		session.setPlan(plan);
		
		when(planDBService.getPlanById(2)).thenReturn(plan);
		when(sessionDBService.changeSessionStatus(eq(1L), eq(ChoreographerSessionStatus.RUNNING), isNull())).thenReturn(session);
		doNothing().when(sessionDBService).worklog(any(String.class), anyLong(), anyLong(), anyString(), isNull());
		when(sessionDBService.increaseExecutionNumber(eq(1L))).thenReturn(session);
		when(planDBService.collectStepsFromPlan(anyLong())).thenReturn(List.of(step));
		when(sessionDataStorage.put(eq(1L), any(SessionExecutorCache.class))).thenReturn(null);
		when(executorSelector.selectAndInit(eq(1L), any(ChoreographerStep.class), anySet(), eq(true), eq(true), eq(true))).thenReturn(null);
		
		try {
			testObject.receiveStartSessionMessage(payload);
		} catch (final Exception ex) {
			verify(planDBService, times(1)).getPlanById(2);
			verify(sessionDBService, times(1)).changeSessionStatus(eq(1L), eq(ChoreographerSessionStatus.RUNNING), isNull());
			verify(sessionDBService, times(1)).worklog(any(String.class), anyLong(), anyLong(), anyString(), isNull());
			verify(sessionDBService, times(1)).increaseExecutionNumber(eq(1L));
			verify(planDBService, times(1)).collectStepsFromPlan(anyLong());
			verify(sessionDataStorage, times(1)).put(eq(1L), any(SessionExecutorCache.class));
			verify(executorSelector, times(1)).selectAndInit(eq(1L), any(ChoreographerStep.class), anySet(), eq(true), eq(true), eq(true));
			
			Assert.assertEquals("Can't find properly working executor for step: plan.action.step", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ChoreographerSessionException.class)
	public void testReceiveStartSessionMessageManualSessionStepRegistering() {
		final ChoreographerStartSessionDTO payload = new ChoreographerStartSessionDTO(1, 2, false, false);
		final ChoreographerPlan plan = new ChoreographerPlan("plan");
		final ChoreographerAction action = new ChoreographerAction("action", null);
		action.setPlan(plan);
		final ChoreographerStep step = new ChoreographerStep();
		step.setId(1);
		step.setName("step");
		step.setAction(action);
		step.setServiceDefinition("service");
		step.setMinVersion(1);
		step.setMaxVersion(2);
		final ChoreographerSession session = new ChoreographerSession();
		session.setId(1L);
		session.setPlan(plan);
		
		final ChoreographerStep step2 = new ChoreographerStep();
		step2.setId(2);
		step2.setName("step2");
		step2.setAction(action);
		step2.setServiceDefinition("service");
		step2.setMinVersion(1);
		step2.setMaxVersion(2);
		
		when(planDBService.getPlanById(2)).thenReturn(plan);
		when(sessionDBService.changeSessionStatus(eq(1L), eq(ChoreographerSessionStatus.RUNNING), isNull())).thenReturn(session);
		doNothing().doThrow(new ArrowheadException("test")).when(sessionDBService).worklog(any(String.class), anyLong(), anyLong(), anyString(), isNull()); // second call throws an exception to end the test
		when(sessionDBService.increaseExecutionNumber(eq(1L))).thenReturn(session);
		when(planDBService.collectStepsFromPlan(anyLong())).thenReturn(List.of(step, step2));
		when(sessionDataStorage.put(eq(1L), any(SessionExecutorCache.class))).thenReturn(null);
		when(executorSelector.selectAndInit(eq(1L), any(ChoreographerStep.class), anySet(), eq(false), eq(false), eq(true))).thenReturn(new ExecutorData(new ChoreographerExecutor(), new SystemRequestDTO(), List.of(), false));
		when(sessionDBService.registerSessionStep(eq(1L), eq(2L), anyLong())).thenReturn(new ChoreographerSessionStep());
		
		try {
			testObject.receiveStartSessionMessage(payload);
		} catch (final Exception ex) {
			verify(planDBService, times(1)).getPlanById(2);
			verify(sessionDBService, times(1)).changeSessionStatus(eq(1L), eq(ChoreographerSessionStatus.RUNNING), isNull());
			verify(sessionDBService, times(2)).worklog(any(String.class), anyLong(), anyLong(), anyString(), isNull());
			verify(sessionDBService, times(1)).increaseExecutionNumber(eq(1L));
			verify(planDBService, times(1)).collectStepsFromPlan(anyLong());
			verify(sessionDataStorage, times(1)).put(eq(1L), any(SessionExecutorCache.class));
			verify(executorSelector, times(1)).selectAndInit(eq(1L), any(ChoreographerStep.class), anySet(), eq(false), eq(false) , eq(true));
			verify(sessionDBService, times(1)).registerSessionStep(eq(1L), eq(2L), anyLong());
			
			Assert.assertEquals("test", ex.getCause().getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ChoreographerSessionException.class)
	public void testReceiveStartSessionMessageCatchMethodFromParallelStream() {
		final ChoreographerStartSessionDTO payload = new ChoreographerStartSessionDTO(1, 2, false, false);
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
		session.setId(1L);
		session.setPlan(plan);
		
		when(planDBService.getPlanById(2)).thenReturn(plan);
		when(sessionDBService.changeSessionStatus(eq(1L), eq(ChoreographerSessionStatus.RUNNING), isNull())).thenReturn(session);
		doNothing().when(sessionDBService).worklog(any(String.class), anyLong(), anyLong(), anyString(), isNull());
		when(sessionDBService.increaseExecutionNumber(eq(1L))).thenReturn(session);
		when(planDBService.collectStepsFromPlan(anyLong())).thenReturn(List.of(step));
		when(sessionDataStorage.put(eq(1L), any(SessionExecutorCache.class))).thenReturn(null);
		when(executorSelector.selectAndInit(eq(1L), any(ChoreographerStep.class), anySet(), eq(false), eq(false), eq(true))).thenReturn(new ExecutorData(new ChoreographerExecutor(), new SystemRequestDTO(), List.of(), false));
		when(planDBService.getFirstSteps(anyLong())).thenReturn(List.of(step));
		when(sessionDataStorage.containsKey(eq(1L))).thenThrow(new ArrowheadException("test"));
		
		try {
			testObject.receiveStartSessionMessage(payload);
		} catch (final Exception ex) {
			verify(planDBService, times(1)).getPlanById(2);
			verify(sessionDBService, times(1)).changeSessionStatus(eq(1L), eq(ChoreographerSessionStatus.RUNNING), isNull());
			verify(sessionDBService, times(3)).worklog(any(String.class), anyLong(), anyLong(), anyString(), isNull());
			verify(sessionDBService, times(1)).increaseExecutionNumber(eq(1L));
			verify(planDBService, times(1)).collectStepsFromPlan(anyLong());
			verify(sessionDataStorage, times(1)).put(eq(1L), any(SessionExecutorCache.class));
			verify(executorSelector, times(1)).selectAndInit(eq(1L), any(ChoreographerStep.class), anySet(), eq(false), eq(false), eq(true));
			verify(sessionDBService, never()).registerSessionStep(eq(1L), anyLong(), anyLong());
			verify(planDBService, times(1)).getFirstSteps(anyLong());
			verify(sessionDataStorage, times(1)).containsKey(eq(1L));
			verify(sessionDBService, never()).changeSessionStepStatus(eq(1L), eq(step), eq(ChoreographerSessionStepStatus.RUNNING), anyString());

			Assert.assertEquals(1L, ((ChoreographerSessionException)ex).getSessionId());
			Assert.assertEquals("test", ex.getCause().getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testReceiveStartSessionMessageSessionAbortedMeanwhile() {
		final ChoreographerStartSessionDTO payload = new ChoreographerStartSessionDTO(1, 2, false, false);
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
		session.setId(1L);
		session.setPlan(plan);
		
		when(planDBService.getPlanById(2)).thenReturn(plan);
		when(sessionDBService.changeSessionStatus(eq(1L), eq(ChoreographerSessionStatus.RUNNING), isNull())).thenReturn(session);
		doNothing().when(sessionDBService).worklog(any(String.class), anyLong(), anyLong(), anyString(), isNull());
		when(sessionDBService.increaseExecutionNumber(eq(1L))).thenReturn(session);
		when(planDBService.collectStepsFromPlan(anyLong())).thenReturn(List.of(step));
		when(sessionDataStorage.put(eq(1L), any(SessionExecutorCache.class))).thenReturn(null);
		when(executorSelector.selectAndInit(eq(1L), any(ChoreographerStep.class), anySet(), eq(false), eq(false), eq(true))).thenReturn(new ExecutorData(new ChoreographerExecutor(), new SystemRequestDTO(), List.of(), false));
		when(planDBService.getFirstSteps(anyLong())).thenReturn(List.of(step));
		when(sessionDataStorage.containsKey(eq(1L))).thenReturn(false);
		
		testObject.receiveStartSessionMessage(payload);
		
		verify(planDBService, times(1)).getPlanById(2);
		verify(sessionDBService, times(1)).changeSessionStatus(eq(1L), eq(ChoreographerSessionStatus.RUNNING), isNull());
		verify(sessionDBService, times(3)).worklog(any(String.class), anyLong(), anyLong(), anyString(), isNull());
		verify(sessionDBService, times(1)).increaseExecutionNumber(eq(1L));
		verify(planDBService, times(1)).collectStepsFromPlan(anyLong());
		verify(sessionDataStorage, times(1)).put(eq(1L), any(SessionExecutorCache.class));
		verify(executorSelector, times(1)).selectAndInit(eq(1L), any(ChoreographerStep.class), anySet(), eq(false), eq(false), eq(true));
		verify(sessionDBService, never()).registerSessionStep(eq(1L), anyLong(), anyLong());
		verify(planDBService, times(1)).getFirstSteps(anyLong());
		verify(sessionDataStorage, times(1)).containsKey(eq(1L));
		verify(sessionDBService, never()).changeSessionStepStatus(eq(1L), eq(step), eq(ChoreographerSessionStepStatus.RUNNING), anyString());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ChoreographerSessionException.class)
	public void testReceiveStartSessionMessageChangeExecutorToTheCached() {
		final ChoreographerStartSessionDTO payload = new ChoreographerStartSessionDTO(1, 2, false, false);
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
		session.setId(1L);
		session.setPlan(plan);
		
		final ChoreographerExecutor oldExecutor = new ChoreographerExecutor();
		oldExecutor.setId(1);
		final ChoreographerSessionStep sessionStep = new ChoreographerSessionStep();
		sessionStep.setId(10);
		sessionStep.setExecutor(oldExecutor);

		final ChoreographerExecutor newExecutor = new ChoreographerExecutor();
		newExecutor.setId(2);
		final SessionExecutorCache cache = new SessionExecutorCache(false, false);
		cache.put("service", 1, 2, new ExecutorData(newExecutor, new SystemRequestDTO(), List.of(), false));
		
		when(planDBService.getPlanById(2)).thenReturn(plan);
		when(sessionDBService.changeSessionStatus(eq(1L), eq(ChoreographerSessionStatus.RUNNING), isNull())).thenReturn(session);
		doNothing().when(sessionDBService).worklog(any(String.class), anyLong(), anyLong(), anyString(), isNull());
		when(sessionDBService.increaseExecutionNumber(eq(1L))).thenReturn(session);
		when(planDBService.collectStepsFromPlan(anyLong())).thenReturn(List.of(step));
		when(sessionDataStorage.put(eq(1L), any(SessionExecutorCache.class))).thenReturn(null);
		when(executorSelector.selectAndInit(eq(1L), any(ChoreographerStep.class), anySet(), eq(false), eq(false), eq(true))).thenReturn(new ExecutorData(oldExecutor, new SystemRequestDTO(), List.of(), false));
		when(planDBService.getFirstSteps(anyLong())).thenReturn(List.of(step));
		when(sessionDataStorage.containsKey(eq(1L))).thenReturn(true);
		when(sessionDBService.changeSessionStepStatus(eq(1L), eq(step), eq(ChoreographerSessionStepStatus.RUNNING), anyString())).thenReturn(sessionStep);
		when(sessionDataStorage.get(eq(1L))).thenReturn(cache);
		when(sessionDBService.changeSessionStepExecutor(10L, 2L)).thenThrow(new ArrowheadException("early end")); // just for finish the test earlier
		
		try {
			testObject.receiveStartSessionMessage(payload);
		} catch (final Exception ex) {
			verify(planDBService, times(1)).getPlanById(2);
			verify(sessionDBService, times(1)).changeSessionStatus(eq(1L), eq(ChoreographerSessionStatus.RUNNING), isNull());
			verify(sessionDBService, times(3)).worklog(any(String.class), anyLong(), anyLong(), anyString(), isNull());
			verify(sessionDBService, times(1)).increaseExecutionNumber(eq(1L));
			verify(planDBService, times(1)).collectStepsFromPlan(anyLong());
			verify(sessionDataStorage, times(1)).put(eq(1L), any(SessionExecutorCache.class));
			verify(executorSelector, times(1)).selectAndInit(eq(1L), any(ChoreographerStep.class), anySet(), eq(false), eq(false), eq(true));
			verify(sessionDBService, never()).registerSessionStep(eq(1L), anyLong(), anyLong());
			verify(planDBService, times(1)).getFirstSteps(anyLong());
			verify(sessionDataStorage, times(1)).containsKey(eq(1L));
			verify(sessionDBService, times(1)).changeSessionStepStatus(eq(1L), eq(step), eq(ChoreographerSessionStepStatus.RUNNING), anyString());
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
		final ChoreographerStartSessionDTO payload = new ChoreographerStartSessionDTO(1, 2, false, false);
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

		final SessionExecutorCache cache = new SessionExecutorCache(false, false);
		final ExecutorData executorData = new ExecutorData(executor, new SystemRequestDTO(), List.of(new ChoreographerServiceQueryFormDTO()), false);
		cache.put("service", 1, 2, executorData);
		
		when(planDBService.getPlanById(2)).thenReturn(plan);
		when(sessionDBService.changeSessionStatus(eq(1L), eq(ChoreographerSessionStatus.RUNNING), isNull())).thenReturn(session);
		doNothing().when(sessionDBService).worklog(any(String.class), anyLong(), anyLong(), anyString(), isNull());
		when(sessionDBService.increaseExecutionNumber(eq(1L))).thenReturn(session);
		when(planDBService.collectStepsFromPlan(anyLong())).thenReturn(List.of(step));
		when(sessionDataStorage.put(eq(1L), any(SessionExecutorCache.class))).thenReturn(null);
		when(executorSelector.selectAndInit(eq(1L), any(ChoreographerStep.class), anySet(), eq(false), eq(false), eq(true))).thenReturn(executorData);
		when(planDBService.getFirstSteps(anyLong())).thenReturn(List.of(step));
		when(sessionDBService.changeSessionStepStatus(eq(1L), eq(step), eq(ChoreographerSessionStepStatus.RUNNING), anyString())).thenReturn(sessionStep);
		when(sessionDataStorage.containsKey(eq(1L))).thenReturn(true);
		when(sessionDataStorage.get(eq(1L))).thenReturn(cache);
		when(driver.queryOrchestrator(any(OrchestrationFormRequestDTO.class))).thenThrow(new ArrowheadException("orch problem"));
		when(executorSelector.selectAndInit(eq(1L), any(ChoreographerStep.class), anySet(), eq(false), eq(false), eq(false))).thenReturn(null);
		
		try {
			testObject.receiveStartSessionMessage(payload);
		} catch (final Exception ex) {
			verify(planDBService, times(1)).getPlanById(2);
			verify(sessionDBService, times(1)).changeSessionStatus(eq(1L), eq(ChoreographerSessionStatus.RUNNING), isNull());
			verify(sessionDBService, times(3)).worklog(any(String.class), anyLong(), anyLong(), anyString(), isNull());
			verify(sessionDBService, times(1)).increaseExecutionNumber(eq(1L));
			verify(planDBService, times(1)).collectStepsFromPlan(anyLong());
			verify(sessionDataStorage, times(1)).put(eq(1L), any(SessionExecutorCache.class));
			verify(executorSelector, times(1)).selectAndInit(eq(1L), any(ChoreographerStep.class), anySet(), eq(false), eq(false), eq(true));
			verify(sessionDBService, never()).registerSessionStep(eq(1L), anyLong(), anyLong());
			verify(planDBService, times(1)).getFirstSteps(anyLong());
			verify(sessionDBService, times(1)).changeSessionStepStatus(eq(1L), eq(step), eq(ChoreographerSessionStepStatus.RUNNING), anyString());
			verify(driver, times(1)).queryOrchestrator(any(OrchestrationFormRequestDTO.class));
			verify(sessionDataStorage, times(1)).containsKey(eq(1L));
			verify(sessionDataStorage, times(2)).get(eq(1L));
			verify(sessionDBService, never()).changeSessionStepExecutor(eq(10L), anyLong());
			verify(executorSelector, times(1)).selectAndInit(eq(1L), any(ChoreographerStep.class), anySet(), eq(false), eq(false), eq(false));
			verify(driver, never()).closeGatewayTunnels(anyList());

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
		final ChoreographerStartSessionDTO payload = new ChoreographerStartSessionDTO(1, 2, false, false);
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

		final SessionExecutorCache cache = new SessionExecutorCache(false, false);
		final ExecutorData executorData = new ExecutorData(executor, new SystemRequestDTO(), List.of(new ChoreographerServiceQueryFormDTO()), false);
		cache.put("service", 1, 2, executorData);
		
		when(planDBService.getPlanById(2)).thenReturn(plan);
		when(sessionDBService.changeSessionStatus(eq(1L), eq(ChoreographerSessionStatus.RUNNING), isNull())).thenReturn(session);
		doNothing().when(sessionDBService).worklog(any(String.class), anyLong(), anyLong(), anyString(), isNull());
		when(sessionDBService.increaseExecutionNumber(eq(1L))).thenReturn(session);
		when(planDBService.collectStepsFromPlan(anyLong())).thenReturn(List.of(step));
		when(sessionDataStorage.put(eq(1L), any(SessionExecutorCache.class))).thenReturn(null);
		when(executorSelector.selectAndInit(eq(1L), any(ChoreographerStep.class), anySet(), eq(false), eq(false), eq(true))).thenReturn(executorData);
		when(planDBService.getFirstSteps(anyLong())).thenReturn(List.of(step));
		when(sessionDBService.changeSessionStepStatus(eq(1L), eq(step), eq(ChoreographerSessionStepStatus.RUNNING), anyString())).thenReturn(sessionStep);
		when(driver.queryOrchestrator(any(OrchestrationFormRequestDTO.class))).thenReturn(new OrchestrationResponseDTO(List.of()));
		when(sessionDataStorage.containsKey(eq(1L))).thenReturn(true);
		when(sessionDataStorage.get(eq(1L))).thenReturn(cache);
		when(executorSelector.selectAndInit(eq(1L), any(ChoreographerStep.class), anySet(), eq(false), eq(false), eq(false))).thenReturn(null);
		
		try {
			testObject.receiveStartSessionMessage(payload);
		} catch (final Exception ex) {
			verify(planDBService, times(1)).getPlanById(2);
			verify(sessionDBService, times(1)).changeSessionStatus(eq(1L), eq(ChoreographerSessionStatus.RUNNING), isNull());
			verify(sessionDBService, times(3)).worklog(any(String.class), anyLong(), anyLong(), anyString(), isNull());
			verify(sessionDBService, times(1)).increaseExecutionNumber(eq(1L));
			verify(planDBService, times(1)).collectStepsFromPlan(anyLong());
			verify(sessionDataStorage, times(1)).put(eq(1L), any(SessionExecutorCache.class));
			verify(executorSelector, times(1)).selectAndInit(eq(1L), any(ChoreographerStep.class), anySet(), eq(false), eq(false), eq(true));
			verify(sessionDBService, never()).registerSessionStep(eq(1L), anyLong(), anyLong());
			verify(planDBService, times(1)).getFirstSteps(anyLong());
			verify(sessionDBService, times(1)).changeSessionStepStatus(eq(1L), eq(step), eq(ChoreographerSessionStepStatus.RUNNING), anyString());
			verify(driver, times(1)).queryOrchestrator(any(OrchestrationFormRequestDTO.class));
			verify(sessionDataStorage, times(1)).containsKey(eq(1L));
			verify(sessionDataStorage, times(2)).get(eq(1L));
			verify(sessionDBService, never()).changeSessionStepExecutor(eq(10L), anyLong());
			verify(executorSelector, times(1)).selectAndInit(eq(1L), any(ChoreographerStep.class), anySet(), eq(false), eq(false), eq(false));
			verify(driver, never()).closeGatewayTunnels(anyList());

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
		final ChoreographerStartSessionDTO payload = new ChoreographerStartSessionDTO(1, 2, false, false);
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

		final SessionExecutorCache cache = new SessionExecutorCache(false, false);
		final ExecutorData executorData = new ExecutorData(executor, new SystemRequestDTO(), List.of(new ChoreographerServiceQueryFormDTO()), false);
		cache.put("service", 1, 2, executorData);
		final ChoreographerExecutor executor2 = new ChoreographerExecutor();
		executor2.setId(2);
		executor2.setName("executor2");
		executor2.setAddress("localhost");
		executor2.setPort(1234);
		final ExecutorData executorData2 = new ExecutorData(executor2, new SystemRequestDTO(), List.of(new ChoreographerServiceQueryFormDTO()), false);

		final OrchestrationResultDTO orchResult = new OrchestrationResultDTO();
		
		when(planDBService.getPlanById(2)).thenReturn(plan);
		when(sessionDBService.changeSessionStatus(eq(1L), eq(ChoreographerSessionStatus.RUNNING), isNull())).thenReturn(session);
		doNothing().when(sessionDBService).worklog(any(String.class), anyLong(), anyLong(), anyString(), isNull());
		when(sessionDBService.increaseExecutionNumber(eq(1L))).thenReturn(session);
		when(planDBService.collectStepsFromPlan(anyLong())).thenReturn(List.of(step));
		when(sessionDataStorage.put(eq(1L), any(SessionExecutorCache.class))).thenReturn(null);
		when(executorSelector.selectAndInit(eq(1L), any(ChoreographerStep.class), anySet(), eq(false), eq(false), eq(true))).thenReturn(executorData);
		when(planDBService.getFirstSteps(anyLong())).thenReturn(List.of(step));
		when(sessionDBService.changeSessionStepStatus(eq(1L), eq(step), eq(ChoreographerSessionStepStatus.RUNNING), anyString())).thenReturn(sessionStep);
		when(driver.queryOrchestrator(any(OrchestrationFormRequestDTO.class))).thenReturn(new OrchestrationResponseDTO(List.of()), new OrchestrationResponseDTO(List.of(orchResult)))
																			  .thenThrow(new ArrowheadException("early end")); // just for finishing the test early
		when(sessionDataStorage.containsKey(eq(1L))).thenReturn(true);
		when(sessionDataStorage.get(eq(1L))).thenReturn(cache);
		when(executorSelector.selectAndInit(eq(1L), any(ChoreographerStep.class), anySet(), eq(false), eq(false), eq(false))).thenReturn(executorData2);
		when(sessionDBService.changeSessionStepExecutor(10L, 2L)).thenReturn(sessionStep);
		
		try {
			testObject.receiveStartSessionMessage(payload);
		} catch (final Exception ex) {
			verify(planDBService, times(1)).getPlanById(2);
			verify(sessionDBService, times(1)).changeSessionStatus(eq(1L), eq(ChoreographerSessionStatus.RUNNING), isNull());
			verify(sessionDBService, times(3)).worklog(any(String.class), anyLong(), anyLong(), anyString(), isNull());
			verify(sessionDBService, times(1)).increaseExecutionNumber(eq(1L));
			verify(planDBService, times(1)).collectStepsFromPlan(anyLong());
			verify(sessionDataStorage, times(1)).put(eq(1L), any(SessionExecutorCache.class));
			verify(executorSelector, times(1)).selectAndInit(eq(1L), any(ChoreographerStep.class), anySet(), eq(false), eq(false), eq(true));
			verify(sessionDBService, never()).registerSessionStep(eq(1L), anyLong(), anyLong());
			verify(planDBService, times(1)).getFirstSteps(anyLong());
			verify(sessionDBService, times(1)).changeSessionStepStatus(eq(1L), eq(step), eq(ChoreographerSessionStepStatus.RUNNING), anyString());
			verify(driver, times(3)).queryOrchestrator(any(OrchestrationFormRequestDTO.class));
			verify(sessionDataStorage, times(1)).containsKey(eq(1L));
			verify(sessionDataStorage, times(2)).get(eq(1L));
			verify(executorSelector, times(1)).selectAndInit(eq(1L), any(ChoreographerStep.class), anySet(), eq(false), eq(false), eq(false));
			verify(sessionDBService, times(1)).changeSessionStepExecutor(10L, 2L);

			Assert.assertEquals(1, cache.getExecutorCache().size());
			Assert.assertEquals(1, cache.getExclusions().size());
			Assert.assertEquals((Long) 1L, (Long) cache.getExclusions().iterator().next());
			
			Assert.assertEquals(1L, ((ChoreographerSessionException)ex).getSessionId());
			Assert.assertEquals("early end", ex.getCause().getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ChoreographerSessionException.class)
	public void testReceiveStartSessionMessageProblemDuringMainServiceOrchestration() { 
		final ChoreographerStartSessionDTO payload = new ChoreographerStartSessionDTO(1, 2, true, false);
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
		step.setSrTemplate("{}");
		
		final ChoreographerSession session = new ChoreographerSession();
		session.setId(1);
		final ChoreographerExecutor executor = new ChoreographerExecutor();
		executor.setId(1);
		final ChoreographerSessionStep sessionStep = new ChoreographerSessionStep();
		sessionStep.setId(10);
		sessionStep.setSession(session);
		sessionStep.setExecutor(executor);

		final SessionExecutorCache cache = new SessionExecutorCache(true, false);
		final ExecutorData executorData = new ExecutorData(executor, new SystemRequestDTO(), List.of(new ChoreographerServiceQueryFormDTO()), true);
		cache.put("service", 1, 2, executorData);
		
		final SystemResponseDTO foreignProvider = new SystemResponseDTO();
		foreignProvider.setSystemName("gateway");
		foreignProvider.setPort(8888);
		final OrchestrationResultDTO orchResult = new OrchestrationResultDTO();
		orchResult.getWarnings().add(OrchestratorWarnings.VIA_GATEWAY);
		orchResult.setProvider(foreignProvider);
		
		when(planDBService.getPlanById(2)).thenReturn(plan);
		when(sessionDBService.changeSessionStatus(eq(1L), eq(ChoreographerSessionStatus.RUNNING), isNull())).thenReturn(session);
		doNothing().when(sessionDBService).worklog(any(String.class), anyLong(), anyLong(), anyString(), isNull());
		when(sessionDBService.increaseExecutionNumber(eq(1L))).thenReturn(session);
		when(planDBService.collectStepsFromPlan(anyLong())).thenReturn(List.of(step));
		when(sessionDataStorage.put(eq(1L), any(SessionExecutorCache.class))).thenReturn(null);
		when(executorSelector.selectAndInit(eq(1L), any(ChoreographerStep.class), anySet(), eq(true), eq(false), eq(true))).thenReturn(executorData);
		when(planDBService.getFirstSteps(anyLong())).thenReturn(List.of(step));
		when(sessionDBService.changeSessionStepStatus(eq(1L), eq(step), eq(ChoreographerSessionStepStatus.RUNNING), anyString())).thenReturn(sessionStep);
		when(driver.queryOrchestrator(any(OrchestrationFormRequestDTO.class))).thenReturn(new OrchestrationResponseDTO(List.of(orchResult))).thenThrow(new ArrowheadException("orch problem")); 
		when(sessionDataStorage.containsKey(eq(1L))).thenReturn(true);
		when(sessionDataStorage.get(eq(1L))).thenReturn(cache);
		doNothing().when(driver).closeGatewayTunnels(anyList());
		
		try {
			testObject.receiveStartSessionMessage(payload);
		} catch (final Exception ex) {
			verify(planDBService, times(1)).getPlanById(2);
			verify(sessionDBService, times(1)).changeSessionStatus(eq(1L), eq(ChoreographerSessionStatus.RUNNING), isNull());
			verify(sessionDBService, times(3)).worklog(any(String.class), anyLong(), anyLong(), anyString(), isNull());
			verify(sessionDBService, times(1)).increaseExecutionNumber(eq(1L));
			verify(planDBService, times(1)).collectStepsFromPlan(anyLong());
			verify(sessionDataStorage, times(1)).put(eq(1L), any(SessionExecutorCache.class));
			verify(executorSelector, times(1)).selectAndInit(eq(1L), any(ChoreographerStep.class), anySet(), eq(true), eq(false), eq(true));
			verify(sessionDBService, never()).registerSessionStep(eq(1L), anyLong(), anyLong());
			verify(planDBService, times(1)).getFirstSteps(anyLong());
			verify(sessionDBService, times(1)).changeSessionStepStatus(eq(1L), eq(step), eq(ChoreographerSessionStepStatus.RUNNING), anyString());
			verify(driver, times(2)).queryOrchestrator(any(OrchestrationFormRequestDTO.class));
			verify(sessionDataStorage, times(1)).containsKey(eq(1L));
			verify(sessionDataStorage, times(2)).get(eq(1L));
			verify(driver, times(1)).closeGatewayTunnels(anyList());

			Assert.assertEquals(1, cache.getExecutorCache().size());
			Assert.assertEquals(0, cache.getExclusions().size());
			
			Assert.assertEquals(1L, ((ChoreographerSessionException)ex).getSessionId());
			Assert.assertTrue(ex.getMessage().startsWith("Problem occured while orchestration for step "));
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ChoreographerSessionException.class)
	public void testReceiveStartSessionMessageProviderNotFoundMainServiceOrchestration() { 
		final ChoreographerStartSessionDTO payload = new ChoreographerStartSessionDTO(1, 2, true, false);
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
		step.setSrTemplate("{}");
		
		final ChoreographerSession session = new ChoreographerSession();
		session.setId(1);
		final ChoreographerExecutor executor = new ChoreographerExecutor();
		executor.setId(1);
		final ChoreographerSessionStep sessionStep = new ChoreographerSessionStep();
		sessionStep.setId(10);
		sessionStep.setSession(session);
		sessionStep.setExecutor(executor);

		final SessionExecutorCache cache = new SessionExecutorCache(true, false);
		final ExecutorData executorData = new ExecutorData(executor, new SystemRequestDTO(), List.of(new ChoreographerServiceQueryFormDTO()), true);
		cache.put("service", 1, 2, executorData);
		
		final SystemResponseDTO foreignProvider = new SystemResponseDTO();
		foreignProvider.setSystemName("gateway");
		foreignProvider.setPort(8888);
		final OrchestrationResultDTO orchResult = new OrchestrationResultDTO();
		orchResult.getWarnings().add(OrchestratorWarnings.VIA_GATEWAY);
		orchResult.setProvider(foreignProvider);
		
		when(planDBService.getPlanById(2)).thenReturn(plan);
		when(sessionDBService.changeSessionStatus(eq(1L), eq(ChoreographerSessionStatus.RUNNING), isNull())).thenReturn(session);
		doNothing().when(sessionDBService).worklog(any(String.class), anyLong(), anyLong(), anyString(), isNull());
		when(sessionDBService.increaseExecutionNumber(eq(1L))).thenReturn(session);
		when(planDBService.collectStepsFromPlan(anyLong())).thenReturn(List.of(step));
		when(sessionDataStorage.put(eq(1L), any(SessionExecutorCache.class))).thenReturn(null);
		when(executorSelector.selectAndInit(eq(1L), any(ChoreographerStep.class), anySet(), eq(true), eq(false), eq(true))).thenReturn(executorData);
		when(planDBService.getFirstSteps(anyLong())).thenReturn(List.of(step));
		when(sessionDBService.changeSessionStepStatus(eq(1L), eq(step), eq(ChoreographerSessionStepStatus.RUNNING), anyString())).thenReturn(sessionStep);
		when(driver.queryOrchestrator(any(OrchestrationFormRequestDTO.class))).thenReturn(new OrchestrationResponseDTO(List.of(orchResult)), new OrchestrationResponseDTO(List.of()));
		when(sessionDataStorage.containsKey(eq(1L))).thenReturn(true);
		when(sessionDataStorage.get(eq(1L))).thenReturn(cache);
		doNothing().when(driver).closeGatewayTunnels(anyList());
		
		try {
			testObject.receiveStartSessionMessage(payload);
		} catch (final Exception ex) {
			verify(planDBService, times(1)).getPlanById(2);
			verify(sessionDBService, times(1)).changeSessionStatus(eq(1L), eq(ChoreographerSessionStatus.RUNNING), isNull());
			verify(sessionDBService, times(3)).worklog(any(String.class), anyLong(), anyLong(), anyString(), isNull());
			verify(sessionDBService, times(1)).increaseExecutionNumber(eq(1L));
			verify(planDBService, times(1)).collectStepsFromPlan(anyLong());
			verify(sessionDataStorage, times(1)).put(eq(1L), any(SessionExecutorCache.class));
			verify(executorSelector, times(1)).selectAndInit(eq(1L), any(ChoreographerStep.class), anySet(), eq(true), eq(false), eq(true));
			verify(sessionDBService, never()).registerSessionStep(eq(1L), anyLong(), anyLong());
			verify(planDBService, times(1)).getFirstSteps(anyLong());
			verify(sessionDBService, times(1)).changeSessionStepStatus(eq(1L), eq(step), eq(ChoreographerSessionStepStatus.RUNNING), anyString());
			verify(driver, times(2)).queryOrchestrator(any(OrchestrationFormRequestDTO.class));
			verify(sessionDataStorage, times(1)).containsKey(eq(1L));
			verify(sessionDataStorage, times(2)).get(eq(1L));
			verify(driver, times(1)).closeGatewayTunnels(anyList());

			Assert.assertEquals(1, cache.getExecutorCache().size());
			Assert.assertEquals(0, cache.getExclusions().size());
			
			Assert.assertEquals(1L, ((ChoreographerSessionException)ex).getSessionId());
			Assert.assertTrue(ex.getMessage().startsWith("No providers found for step: "));
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ChoreographerSessionException.class)
	public void testReceiveStartSessionMessageStartExecutorProblem() { 
		final ChoreographerStartSessionDTO payload = new ChoreographerStartSessionDTO(1, 2, true, false);
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
		step.setSrTemplate("{}");
		
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

		final SessionExecutorCache cache = new SessionExecutorCache(true, false);
		final ExecutorData executorData = new ExecutorData(executor, new SystemRequestDTO(), List.of(new ChoreographerServiceQueryFormDTO()), true);
		cache.put("service", 1, 2, executorData);
		
		final SystemResponseDTO foreignProvider = new SystemResponseDTO();
		foreignProvider.setSystemName("gateway");
		foreignProvider.setPort(8888);
		final OrchestrationResultDTO orchResult = new OrchestrationResultDTO();
		orchResult.getWarnings().add(OrchestratorWarnings.VIA_GATEWAY);
		orchResult.setProvider(foreignProvider);

		when(planDBService.getPlanById(2)).thenReturn(plan);
		when(sessionDBService.changeSessionStatus(eq(1L), eq(ChoreographerSessionStatus.RUNNING), isNull())).thenReturn(session);
		doNothing().when(sessionDBService).worklog(any(String.class), anyLong(), anyLong(), anyString(), isNull());
		when(sessionDBService.increaseExecutionNumber(eq(1L))).thenReturn(session);
		when(planDBService.collectStepsFromPlan(anyLong())).thenReturn(List.of(step));
		when(sessionDataStorage.put(eq(1L), any(SessionExecutorCache.class))).thenReturn(null);
		when(executorSelector.selectAndInit(eq(1L), any(ChoreographerStep.class), anySet(), eq(true), eq(false), eq(true))).thenReturn(executorData);
		when(planDBService.getFirstSteps(anyLong())).thenReturn(List.of(step));
		when(sessionDBService.changeSessionStepStatus(eq(1L), eq(step), eq(ChoreographerSessionStepStatus.RUNNING), anyString())).thenReturn(sessionStep);
		when(driver.queryOrchestrator(any(OrchestrationFormRequestDTO.class))).thenReturn(new OrchestrationResponseDTO(List.of(orchResult)), new OrchestrationResponseDTO(List.of(new OrchestrationResultDTO())));
		when(sessionDataStorage.containsKey(eq(1L))).thenReturn(true);
		when(sessionDataStorage.get(eq(1L))).thenReturn(cache);
		doThrow(new ArrowheadException("executor start problem")).when(driver).startExecutor(anyString(), anyInt(), anyString(), any(ChoreographerExecuteStepRequestDTO.class));
		doNothing().when(driver).closeGatewayTunnels(anyList());
		when(executorSelector.selectAndInit(eq(1L), any(ChoreographerStep.class), anySet(), eq(true), eq(false), eq(false))).thenReturn(null);
		
		try {
			testObject.receiveStartSessionMessage(payload);
		} catch (final Exception ex) {
			verify(planDBService, times(1)).getPlanById(2);
			verify(sessionDBService, times(1)).changeSessionStatus(eq(1L), eq(ChoreographerSessionStatus.RUNNING), isNull());
			verify(sessionDBService, times(3)).worklog(any(String.class), anyLong(), anyLong(), anyString(), isNull());
			verify(sessionDBService, times(1)).increaseExecutionNumber(eq(1L));
			verify(planDBService, times(1)).collectStepsFromPlan(anyLong());
			verify(sessionDataStorage, times(1)).put(eq(1L), any(SessionExecutorCache.class));
			verify(executorSelector, times(1)).selectAndInit(eq(1L), any(ChoreographerStep.class), anySet(), eq(true), eq(false), eq(true));
			verify(sessionDBService, never()).registerSessionStep(eq(1L), anyLong(), anyLong());
			verify(planDBService, times(1)).getFirstSteps(anyLong());
			verify(sessionDBService, times(2)).changeSessionStepStatus(eq(1L), eq(step), eq(ChoreographerSessionStepStatus.RUNNING), anyString());
			verify(driver, times(2)).queryOrchestrator(any(OrchestrationFormRequestDTO.class));
			verify(sessionDataStorage, times(2)).containsKey(eq(1L));
			verify(sessionDataStorage, times(3)).get(eq(1L));
			verify(sessionDBService, never()).changeSessionStepExecutor(eq(10L), anyLong());
			verify(driver, times(1)).startExecutor(anyString(), anyInt(), anyString(), any(ChoreographerExecuteStepRequestDTO.class));
			verify(driver, times(1)).closeGatewayTunnels(anyList());
			verify(executorSelector, times(1)).selectAndInit(eq(1L), any(ChoreographerStep.class), anySet(), eq(true), eq(false), eq(false));
			
			Assert.assertEquals(0, cache.getExecutorCache().size());
			Assert.assertEquals(1, cache.getExclusions().size());
			Assert.assertEquals(1L, ((ChoreographerSessionException)ex).getSessionId());
			Assert.assertEquals((Long) 10L, ((ChoreographerSessionException)ex).getSessionStepId());
			Assert.assertEquals("Can't find properly working executor for step: plan.action.step", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ChoreographerSessionException.class)
	public void testReceiveStartSessionMessageStartExecutorProblemSelectingAnotherExecutor() { 
		final ChoreographerStartSessionDTO payload = new ChoreographerStartSessionDTO(1, 2, true, false);
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
		step.setSrTemplate("{}");
		
		final ChoreographerSession session = new ChoreographerSession();
		session.setId(1);
		final ChoreographerExecutor executor = new ChoreographerExecutor();
		executor.setId(1);
		executor.setName("executor");
		executor.setAddress("localhost");
		executor.setPort(1234);
		executor.setBaseUri("");
		
		final ChoreographerExecutor otherExecutor = new ChoreographerExecutor();
		otherExecutor.setId(2);
		otherExecutor.setName("otherEecutor");
		otherExecutor.setAddress("localhost");
		otherExecutor.setPort(5678);
		otherExecutor.setBaseUri("");

		final ChoreographerSessionStep sessionStep = new ChoreographerSessionStep();
		sessionStep.setId(10);
		sessionStep.setSession(session);
		sessionStep.setExecutor(executor);

		final SessionExecutorCache cache = new SessionExecutorCache(true, false);
		final ExecutorData executorData = new ExecutorData(executor, new SystemRequestDTO(), List.of(new ChoreographerServiceQueryFormDTO()), true);
		cache.put("service", 1, 2, executorData);
		
		final SystemResponseDTO foreignProvider = new SystemResponseDTO();
		foreignProvider.setSystemName("gateway");
		foreignProvider.setPort(8888);
		final OrchestrationResultDTO orchResult = new OrchestrationResultDTO();
		orchResult.getWarnings().add(OrchestratorWarnings.VIA_GATEWAY);
		orchResult.setProvider(foreignProvider);

		when(planDBService.getPlanById(2)).thenReturn(plan);
		when(sessionDBService.changeSessionStatus(eq(1L), eq(ChoreographerSessionStatus.RUNNING), isNull())).thenReturn(session);
		doNothing().when(sessionDBService).worklog(any(String.class), anyLong(), anyLong(), anyString(), isNull());
		when(sessionDBService.increaseExecutionNumber(eq(1L))).thenReturn(session);
		when(planDBService.collectStepsFromPlan(anyLong())).thenReturn(List.of(step));
		when(sessionDataStorage.put(eq(1L), any(SessionExecutorCache.class))).thenReturn(null);
		when(executorSelector.selectAndInit(eq(1L), any(ChoreographerStep.class), anySet(), eq(true), eq(false), eq(true))).thenReturn(executorData);
		when(planDBService.getFirstSteps(anyLong())).thenReturn(List.of(step));
		when(sessionDBService.changeSessionStepStatus(eq(1L), eq(step), eq(ChoreographerSessionStepStatus.RUNNING), anyString())).thenReturn(sessionStep);
		when(driver.queryOrchestrator(any(OrchestrationFormRequestDTO.class))).thenReturn(new OrchestrationResponseDTO(List.of(orchResult)), new OrchestrationResponseDTO(List.of(new OrchestrationResultDTO())));
		when(sessionDataStorage.containsKey(eq(1L))).thenReturn(true);
		when(sessionDataStorage.get(eq(1L))).thenReturn(cache);
		doThrow(new ArrowheadException("executor start problem")).when(driver).startExecutor(anyString(), anyInt(), anyString(), any(ChoreographerExecuteStepRequestDTO.class));
		doNothing().when(driver).closeGatewayTunnels(anyList());
		when(executorSelector.selectAndInit(eq(1L), any(ChoreographerStep.class), anySet(), eq(true), eq(false), eq(false))).thenReturn(new ExecutorData(otherExecutor, new SystemRequestDTO(), List.of(), false));
		when(sessionDBService.changeSessionStepExecutor(10L, 2L)).thenThrow(new ArrowheadException("early end")); // to finish the test earlier
		
		try {
			testObject.receiveStartSessionMessage(payload);
		} catch (final Exception ex) {
			verify(planDBService, times(1)).getPlanById(2);
			verify(sessionDBService, times(1)).changeSessionStatus(eq(1L), eq(ChoreographerSessionStatus.RUNNING), isNull());
			verify(sessionDBService, times(3)).worklog(any(String.class), anyLong(), anyLong(), anyString(), isNull());
			verify(sessionDBService, times(1)).increaseExecutionNumber(eq(1L));
			verify(planDBService, times(1)).collectStepsFromPlan(anyLong());
			verify(sessionDataStorage, times(1)).put(eq(1L), any(SessionExecutorCache.class));
			verify(executorSelector, times(1)).selectAndInit(eq(1L), any(ChoreographerStep.class), anySet(), eq(true), eq(false), eq(true));
			verify(sessionDBService, never()).registerSessionStep(eq(1L), anyLong(), anyLong());
			verify(planDBService, times(1)).getFirstSteps(anyLong());
			verify(sessionDBService, times(2)).changeSessionStepStatus(eq(1L), eq(step), eq(ChoreographerSessionStepStatus.RUNNING), anyString());
			verify(driver, times(2)).queryOrchestrator(any(OrchestrationFormRequestDTO.class));
			verify(sessionDataStorage, times(2)).containsKey(eq(1L));
			verify(sessionDataStorage, times(3)).get(eq(1L));
			verify(driver, times(1)).startExecutor(anyString(), anyInt(), anyString(), any(ChoreographerExecuteStepRequestDTO.class));
			verify(driver, times(1)).closeGatewayTunnels(anyList());
			verify(executorSelector, times(1)).selectAndInit(eq(1L), any(ChoreographerStep.class), anySet(), eq(true), eq(false), eq(false));
			verify(sessionDBService, times(1)).changeSessionStepExecutor(10L, 2L);
			
			Assert.assertEquals(1, cache.getExecutorCache().size());
			Assert.assertEquals(1, cache.getExclusions().size());
			Assert.assertEquals(1L, ((ChoreographerSessionException)ex).getSessionId());
			Assert.assertEquals("early end", ex.getCause().getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testReceiveStartSessionMessageOk() { 
		final ChoreographerStartSessionDTO payload = new ChoreographerStartSessionDTO(1, 2, true, false);
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
		step.setSrTemplate("{}");
		
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

		final SessionExecutorCache cache = new SessionExecutorCache(true, false);
		final ExecutorData executorData = new ExecutorData(executor, new SystemRequestDTO(), List.of(new ChoreographerServiceQueryFormDTO()), true);
		cache.put("service", 1, 2, executorData);
		
		final SystemResponseDTO foreignProvider = new SystemResponseDTO();
		foreignProvider.setSystemName("gateway");
		foreignProvider.setPort(8888);
		final OrchestrationResultDTO orchResult = new OrchestrationResultDTO();
		orchResult.getWarnings().add(OrchestratorWarnings.VIA_GATEWAY);
		orchResult.setProvider(foreignProvider);

		when(planDBService.getPlanById(2)).thenReturn(plan);
		when(sessionDBService.changeSessionStatus(eq(1L), eq(ChoreographerSessionStatus.RUNNING), isNull())).thenReturn(session);
		doNothing().when(sessionDBService).worklog(any(String.class), anyLong(), anyLong(), anyString(), isNull());
		when(sessionDBService.increaseExecutionNumber(eq(1L))).thenReturn(session);
		when(planDBService.collectStepsFromPlan(anyLong())).thenReturn(List.of(step));
		when(sessionDataStorage.put(eq(1L), any(SessionExecutorCache.class))).thenReturn(null);
		when(executorSelector.selectAndInit(eq(1L), any(ChoreographerStep.class), anySet(), eq(true), eq(false), eq(true))).thenReturn(executorData);
		when(planDBService.getFirstSteps(anyLong())).thenReturn(List.of(step));
		when(sessionDBService.changeSessionStepStatus(eq(1L), eq(step), eq(ChoreographerSessionStepStatus.RUNNING), anyString())).thenReturn(sessionStep);
		when(driver.queryOrchestrator(any(OrchestrationFormRequestDTO.class))).thenReturn(new OrchestrationResponseDTO(List.of(orchResult)), new OrchestrationResponseDTO(List.of(new OrchestrationResultDTO())));
		when(sessionDataStorage.containsKey(eq(1L))).thenReturn(true);
		when(sessionDataStorage.get(eq(1L))).thenReturn(cache);
		doNothing().when(driver).startExecutor(anyString(), anyInt(), anyString(), any(ChoreographerExecuteStepRequestDTO.class));
		
		testObject.receiveStartSessionMessage(payload);
		
		verify(planDBService, times(1)).getPlanById(2);
		verify(sessionDBService, times(1)).changeSessionStatus(eq(1L), eq(ChoreographerSessionStatus.RUNNING), isNull());
		verify(sessionDBService, times(3)).worklog(any(String.class), anyLong(), anyLong(), anyString(), isNull());
		verify(sessionDBService, times(1)).increaseExecutionNumber(eq(1L));
		verify(planDBService, times(1)).collectStepsFromPlan(anyLong());
		verify(sessionDataStorage, times(1)).put(eq(1L), any(SessionExecutorCache.class));
		verify(executorSelector, times(1)).selectAndInit(eq(1L), any(ChoreographerStep.class), anySet(), eq(true), eq(false), eq(true));
		verify(sessionDBService, never()).registerSessionStep(eq(1L), anyLong(), anyLong());
		verify(planDBService, times(1)).getFirstSteps(anyLong());
		verify(sessionDBService, times(1)).changeSessionStepStatus(eq(1L), eq(step), eq(ChoreographerSessionStepStatus.RUNNING), anyString());
		verify(driver, times(2)).queryOrchestrator(any(OrchestrationFormRequestDTO.class));
		verify(sessionDataStorage, times(1)).containsKey(eq(1L));
		verify(sessionDataStorage, times(2)).get(eq(1L));
		verify(sessionDBService, never()).changeSessionStepExecutor(eq(10L), anyLong());
		verify(driver, times(1)).startExecutor(anyString(), anyInt(), anyString(), any(ChoreographerExecuteStepRequestDTO.class));
		
		Assert.assertEquals(1, cache.getExecutorCache().size());
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
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testReceiveSessionStepDoneMessageStatusNull() {
		try {
			final ChoreographerExecutedStepResultDTO payload = new ChoreographerExecutedStepResultDTO();
			payload.setSessionId(1L);
			payload.setSessionStepId(1L);
			
			testObject.receiveSessionStepDoneMessage(payload);
		} catch (final Exception ex) {
			Assert.assertEquals("Status is null.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testReceiveSessionStepDoneMessageErrorMessageNull() {
		try {
			final ChoreographerExecutedStepResultDTO payload = new ChoreographerExecutedStepResultDTO();
			payload.setSessionId(1L);
			payload.setSessionStepId(1L);
			payload.setStatus(ChoreographerExecutedStepStatus.ERROR);
			
			testObject.receiveSessionStepDoneMessage(payload);
		} catch (final Exception ex) {
			Assert.assertEquals("Message is null or blank.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testReceiveSessionStepDoneMessageErrorMessageEmpty() {
		try {
			final ChoreographerExecutedStepResultDTO payload = new ChoreographerExecutedStepResultDTO();
			payload.setSessionId(1L);
			payload.setSessionStepId(1L);
			payload.setStatus(ChoreographerExecutedStepStatus.FATAL_ERROR);
			payload.setMessage("");
			
			testObject.receiveSessionStepDoneMessage(payload);
		} catch (final Exception ex) {
			Assert.assertEquals("Message is null or blank.", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testReceiveSessionStepDoneMessageAbortedOk() {
		final ChoreographerExecutedStepResultDTO payload = new ChoreographerExecutedStepResultDTO();
		payload.setSessionId(1L);
		payload.setSessionStepId(1L);
		payload.setStatus(ChoreographerExecutedStepStatus.ABORTED);
		
		final ChoreographerPlan plan = new ChoreographerPlan();
		plan.setName("plan");
		final ChoreographerSession session = new ChoreographerSession();
		session.setId(1);
		session.setPlan(plan);
		final ChoreographerAction action = new ChoreographerAction();
		action.setName("action");
		final ChoreographerStep step = new ChoreographerStep();
		step.setName("step");
		step.setAction(action);
		final ChoreographerSessionStep sessionStep = new ChoreographerSessionStep();
		sessionStep.setId(1);
		sessionStep.setSession(session);
		sessionStep.setStep(step);
		
		final SessionExecutorCache cache = new SessionExecutorCache(true, false);
		cache.getGatewayTunnels().put(1L, List.of(12121));
		cache.aborted();
		
		when(sessionDBService.getSessionStepById(1)).thenReturn(sessionStep);
		when(sessionDataStorage.containsKey(eq(1L))).thenReturn(true);
		when(sessionDataStorage.get(1L)).thenReturn(cache);
		doNothing().when(driver).closeGatewayTunnels(anyList());
		doNothing().when(sessionDBService).worklog(eq("plan"), eq("action"), eq("step"), eq(1L), anyLong(), eq("The execution of this step has aborted successfully."), isNull());
		
		testObject.receiveSessionStepDoneMessage(payload);
		
		verify(sessionDBService, times(1)).getSessionStepById(1);
		verify(sessionDataStorage, times(2)).containsKey(1L);
		verify(sessionDataStorage, times(2)).get(1L);
		verify(sessionDataStorage, times(1)).remove(1L);
		verify(driver, times(1)).closeGatewayTunnels(anyList());
		verify(sessionDBService, times(1)).worklog(eq("plan"), eq("action"), eq("step"), eq(1L), anyLong(), eq("The execution of this step has aborted successfully."), isNull());
		
		Assert.assertEquals(0, cache.getGatewayTunnels().size());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testReceiveSessionStepDoneMessageFatalErrorCallAbortSessionMethod() { // we will test abortSession separately
		final ChoreographerExecutedStepResultDTO payload = new ChoreographerExecutedStepResultDTO();
		payload.setSessionId(1L);
		payload.setSessionStepId(1L);
		payload.setStatus(ChoreographerExecutedStepStatus.FATAL_ERROR);
		payload.setMessage("Fatal error");
		
		final ChoreographerSession session = new ChoreographerSession();
		
		when(sessionDBService.getSessionById(1)).thenReturn(session);
		when(sessionDBService.abortSession(1, "Fatal error")).thenThrow(new ArrowheadException("early end")); // to finish this test earlier
		
		try {
			testObject.receiveSessionStepDoneMessage(payload);
		} catch (final Exception ex) {
			verify(sessionDBService, times(1)).getSessionById(1);
			verify(sessionDBService, times(1)).abortSession(1, "Fatal error");
			
			Assert.assertEquals("early end", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testReceiveSessionStepDoneMessageExecutorErrorNoReplacementExecutorCallAbortSessionMethod() { // we will test abortSession separately
		final ChoreographerExecutedStepResultDTO payload = new ChoreographerExecutedStepResultDTO();
		payload.setSessionId(1L);
		payload.setSessionStepId(1L);
		payload.setStatus(ChoreographerExecutedStepStatus.ERROR);
		payload.setMessage("Executor error");
		
		final ChoreographerPlan plan = new ChoreographerPlan();
		plan.setName("plan");
		final ChoreographerSession session = new ChoreographerSession();
		session.setId(1);
		session.setPlan(plan);
		final ChoreographerAction action = new ChoreographerAction();
		action.setName("action");
		final ChoreographerStep step = new ChoreographerStep();
		step.setName("step");
		step.setAction(action);
		final ChoreographerExecutor executor = new ChoreographerExecutor();
		executor.setId(100);
		final ChoreographerSessionStep sessionStep = new ChoreographerSessionStep();
		sessionStep.setId(1);
		sessionStep.setSession(session);
		sessionStep.setStep(step);
		sessionStep.setExecutor(executor);
		
		final SessionExecutorCache cache = new SessionExecutorCache(true, false);
		cache.getGatewayTunnels().put(1L, List.of(22222));

		when(sessionDBService.getSessionStepById(1)).thenReturn(sessionStep);
		doNothing().when(sessionDBService).worklog(eq("plan"), eq("action"), eq("step"), eq(1L), anyLong(), anyString(), isNull());
		when(sessionDataStorage.get(eq(1L))).thenReturn(cache);
		when(executorSelector.selectAndInit(eq(1L), eq(step), anySet(), eq(true), eq(false), eq(false))).thenReturn(null);
		when(sessionDBService.getSessionById(1)).thenReturn(session);
		when(sessionDBService.abortSession(1, "Executor error")).thenThrow(new ArrowheadException("early end")); // to finish this test earlier
		
		try {
			testObject.receiveSessionStepDoneMessage(payload);
		} catch (final Exception ex) {
			verify(sessionDBService, times(1)).getSessionStepById(1);
			verify(sessionDBService, times(1)).worklog(eq("plan"), eq("action"), eq("step"), eq(1L), anyLong(), anyString(), isNull());
			verify(sessionDataStorage, times(1)).get(eq(1L));
			verify(executorSelector, times(1)).selectAndInit(eq(1L), eq(step), anySet(), eq(true), eq(false), eq(false));
			verify(sessionDBService, times(1)).getSessionById(1);
			verify(sessionDBService, times(1)).abortSession(1, "Executor error");
			verify(driver, never()).closeGatewayTunnels(anyList());
			
			Assert.assertEquals(1, cache.getExclusions().size());
			Assert.assertEquals((Long) 100L, cache.getExclusions().iterator().next());
			Assert.assertEquals("early end", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testReceiveSessionStepDoneMessageExecutorErrorReplacementExceutorCallExecuteStep() { // executeStep is already tested with receiveStartSessionMessage, so we just make sure it is called
		final ChoreographerExecutedStepResultDTO payload = new ChoreographerExecutedStepResultDTO();
		payload.setSessionId(1L);
		payload.setSessionStepId(1L);
		payload.setStatus(ChoreographerExecutedStepStatus.ERROR);
		payload.setMessage("Executor error");
		
		final ChoreographerPlan plan = new ChoreographerPlan();
		plan.setName("plan");
		final ChoreographerSession session = new ChoreographerSession();
		session.setId(1);
		session.setPlan(plan);
		final ChoreographerAction action = new ChoreographerAction();
		action.setName("action");
		action.setPlan(plan);
		final ChoreographerStep step = new ChoreographerStep();
		step.setName("step");
		step.setAction(action);
		final ChoreographerExecutor executor = new ChoreographerExecutor();
		executor.setId(100);
		final ChoreographerSessionStep sessionStep = new ChoreographerSessionStep();
		sessionStep.setId(1);
		sessionStep.setSession(session);
		sessionStep.setStep(step);
		sessionStep.setExecutor(executor);
		final ChoreographerExecutor executor2 = new ChoreographerExecutor();
		executor2.setId(200);
		
		final SessionExecutorCache cache = new SessionExecutorCache(true, false);
		cache.getGatewayTunnels().put(1L, List.of(22222));

		when(sessionDBService.getSessionStepById(1)).thenReturn(sessionStep);
		doNothing().when(sessionDBService).worklog(eq("plan"), eq("action"), eq("step"), eq(1L), anyLong(), anyString(), isNull());
		when(sessionDataStorage.containsKey(eq(1L))).thenReturn(true);
		when(sessionDataStorage.get(eq(1L))).thenReturn(cache);
		doNothing().when(driver).closeGatewayTunnels(anyList());
		when(executorSelector.selectAndInit(eq(1L), eq(step), anySet(), eq(true), eq(false), eq(false))).thenReturn(new ExecutorData(executor2, new SystemRequestDTO(), List.of(), false));
		when(sessionDBService.changeSessionStepExecutor(1, 200)).thenReturn(sessionStep);
		when(sessionDBService.changeSessionStepStatus(1, step, ChoreographerSessionStepStatus.RUNNING, "Running step: plan.action.step")).thenThrow(new ArrowheadException("early end")); // to finish the test earlier
		
		try {
			testObject.receiveSessionStepDoneMessage(payload);
		} catch (final Exception ex) {
			verify(sessionDBService, times(1)).getSessionStepById(1);
			verify(sessionDBService, times(1)).worklog(eq("plan"), eq("action"), eq("step"), eq(1L), anyLong(), anyString(), isNull());
			verify(sessionDataStorage, times(2)).containsKey(eq(1L));
			verify(sessionDataStorage, times(2)).get(eq(1L));
			verify(driver, times(1)).closeGatewayTunnels(anyList());
			verify(executorSelector, times(1)).selectAndInit(eq(1L), eq(step), anySet(), eq(true), eq(false), eq(false));
			verify(sessionDBService, times(1)).changeSessionStepExecutor(1, 200);
			verify(sessionDBService, times(1)).changeSessionStepStatus(1, step, ChoreographerSessionStepStatus.RUNNING, "Running step: plan.action.step");
			
			Assert.assertEquals(1, cache.getExecutorCache().size());
			Assert.assertEquals(200, cache.getExecutorCache().values().iterator().next().getExecutor().getId());
			Assert.assertEquals(1, cache.getExclusions().size());
			Assert.assertEquals((Long) 100L, cache.getExclusions().iterator().next());
			Assert.assertEquals("early end", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testReceiveSessionStepDoneMessageSessionDone() { 
		final ChoreographerExecutedStepResultDTO payload = new ChoreographerExecutedStepResultDTO();
		payload.setSessionId(1001L);
		payload.setSessionStepId(1L);
		payload.setStatus(ChoreographerExecutedStepStatus.SUCCESS);
		
		final ChoreographerPlan plan = new ChoreographerPlan();
		plan.setId(13);
		plan.setName("name");
		final ChoreographerSession session = new ChoreographerSession();
		session.setId(1001);
		session.setPlan(plan);
		session.setNotifyUri("http://localhost:2222/notify");
		session.setQuantityDone(1);
		session.setExecutionNumber(1);
		session.setStatus(ChoreographerSessionStatus.DONE);
		final ChoreographerAction action = new ChoreographerAction();
		action.setId(11);
		final ChoreographerStep step = new ChoreographerStep();
		step.setAction(action);
		final ChoreographerSessionStep sessionStep = new ChoreographerSessionStep();
		sessionStep.setId(1);
		sessionStep.setSession(session);
		sessionStep.setStep(step);
		final ChoreographerSessionStep prevStep = new ChoreographerSessionStep();
		prevStep.setStatus(ChoreographerSessionStepStatus.DONE);
		
		final SessionExecutorCache cache = new SessionExecutorCache(true, false);
		cache.getGatewayTunnels().put(1L, List.of(22222));
		
		when(sessionDBService.changeSessionStepStatus(1, ChoreographerSessionStepStatus.DONE, "Step finished successfully.")).thenReturn(sessionStep);
		when(sessionDataStorage.containsKey(1001L)).thenReturn(true);
		when(sessionDataStorage.get(1001L)).thenReturn(cache);
		doNothing().when(driver).closeGatewayTunnels(anyList());
		when(sessionDBService.getAllSessionStepBySessionIdAndActionId(1001, 11)).thenReturn(List.of(prevStep));
		when(sessionDBService.increaseSessionQuantityDone(eq(1001L))).thenReturn(session);
		when(sessionDBService.changeSessionStatus(eq(1001L), eq(ChoreographerSessionStatus.DONE), isNull())).thenReturn(session);
		when(sessionDBService.getSessionById(eq(1001L))).thenReturn(session);
		doNothing().when(driver).sendSessionNotification(eq(session.getNotifyUri()), any(ChoreographerNotificationDTO.class));
		
		testObject.receiveSessionStepDoneMessage(payload);
		
		verify(sessionDBService, times(1)).changeSessionStepStatus(1, ChoreographerSessionStepStatus.DONE, "Step finished successfully.");
		verify(sessionDataStorage, times(3)).containsKey(1001L);
		verify(sessionDataStorage, times(3)).get(1001L);
		verify(driver, times(1)).closeGatewayTunnels(anyList());
		verify(sessionDBService, times(1)).getAllSessionStepBySessionIdAndActionId(1001, 11);
		verify(sessionDBService, times(1)).increaseSessionQuantityDone(eq(1001L));
		verify(sessionDBService, times(1)).changeSessionStatus(eq(1001L), eq(ChoreographerSessionStatus.DONE), isNull());
		verify(sessionDBService, times(2)).getSessionById(eq(1001L));
		verify(driver, times(2)).sendSessionNotification(eq(session.getNotifyUri()), any(ChoreographerNotificationDTO.class));
		verify(sessionDataStorage, times(1)).remove(1001L);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testReceiveSessionStepDoneMessageTwoIteration() { // executeAction is already tested with receiveStartSessionMessage, so we just make sure it is called
		final ChoreographerExecutedStepResultDTO payload = new ChoreographerExecutedStepResultDTO();
		payload.setSessionId(1001L);
		payload.setSessionStepId(1L);
		payload.setStatus(ChoreographerExecutedStepStatus.SUCCESS);
		
		final ChoreographerAction action = new ChoreographerAction();
		action.setId(11);
		final ChoreographerPlan plan = new ChoreographerPlan();
		plan.setId(13);
		plan.setName("name");
		plan.setFirstAction(action);
		final ChoreographerSession sessionIteration1 = new ChoreographerSession();
		sessionIteration1.setId(1001);
		sessionIteration1.setPlan(plan);
		sessionIteration1.setNotifyUri("http://localhost:2222/notify");
		sessionIteration1.setQuantityDone(1);
		sessionIteration1.setQuantityGoal(2);
		sessionIteration1.setExecutionNumber(1);
		sessionIteration1.setStatus(ChoreographerSessionStatus.RUNNING);
		final ChoreographerSession sessionIteration2 = new ChoreographerSession();
		sessionIteration2.setId(1001);
		sessionIteration2.setPlan(plan);
		sessionIteration2.setNotifyUri("http://localhost:2222/notify");
		sessionIteration2.setQuantityDone(2);
		sessionIteration2.setQuantityGoal(2);
		sessionIteration2.setExecutionNumber(2);
		sessionIteration2.setStatus(ChoreographerSessionStatus.DONE);
		
		final ChoreographerStep step = new ChoreographerStep();
		step.setId(25);
		step.setAction(action);
		step.setServiceDefinition("service");
		step.setMaxVersion(1);
		step.setMaxVersion(2);
		final ChoreographerSessionStep sessionStep = new ChoreographerSessionStep();
		sessionStep.setId(1);
		sessionStep.setSession(sessionIteration1);
		sessionStep.setStep(step);
		final ChoreographerSessionStep prevStep = new ChoreographerSessionStep();
		prevStep.setStatus(ChoreographerSessionStepStatus.DONE);
		
		final ChoreographerExecutor executor = new ChoreographerExecutor();
		executor.setId(34);
		executor.setName("executor");
		executor.setAddress("localhost");
		executor.setPort(1234);
		executor.setBaseUri("");

		final SessionExecutorCache cache = new SessionExecutorCache(true, false);
		final ExecutorData executorData = new ExecutorData(executor, new SystemRequestDTO(), List.of(new ChoreographerServiceQueryFormDTO()), true);
		cache.getGatewayTunnels().put(1L, List.of(22222));
		cache.put("service", 1, 2, executorData);
		
		when(sessionDBService.changeSessionStepStatus(1, ChoreographerSessionStepStatus.DONE, "Step finished successfully.")).thenReturn(sessionStep);
		when(sessionDataStorage.containsKey(1001L)).thenReturn(true);
		when(sessionDataStorage.get(1001L)).thenReturn(cache);
		doNothing().when(driver).closeGatewayTunnels(anyList());
		when(sessionDBService.getAllSessionStepBySessionIdAndActionId(1001, 11)).thenReturn(List.of(prevStep));
		when(sessionDBService.increaseSessionQuantityDone(eq(1001L))).thenReturn(sessionIteration1).thenReturn(sessionIteration2);
		when(sessionDBService.increaseExecutionNumber(eq(1001L))).thenReturn(sessionIteration2);
		when(planDBService.collectStepsFromPlan(eq(13L))).thenReturn(List.of(step));
		when(sessionDBService.getSessionById(eq(1001L))).thenReturn(sessionIteration1).thenReturn(sessionIteration2);
		doNothing().when(driver).sendSessionNotification(eq(sessionIteration1.getNotifyUri()), any(ChoreographerNotificationDTO.class));
		when(planDBService.getFirstSteps(eq(11L))).thenThrow(new ArrowheadException("early end"));
		
		try {
			testObject.receiveSessionStepDoneMessage(payload);
			
		} catch (final ArrowheadException ex) {
			verify(sessionDBService, times(1)).changeSessionStepStatus(1, ChoreographerSessionStepStatus.DONE, "Step finished successfully.");
			verify(sessionDataStorage, times(1)).containsKey(1001L);
			verify(sessionDataStorage, times(2)).get(1001L);
			verify(driver, times(1)).closeGatewayTunnels(anyList());
			verify(sessionDBService, times(1)).getAllSessionStepBySessionIdAndActionId(1001, 11);
			verify(sessionDBService, times(1)).increaseSessionQuantityDone(eq(1001L));
			verify(sessionDBService, times(1)).increaseExecutionNumber(eq(1001L));
			verify(planDBService, times(1)).collectStepsFromPlan(eq(13L));
			verify(sessionDBService, times(1)).registerSessionStep(eq(1001L), eq(25L), eq(34L));
			verify(sessionDBService, times(2)).getSessionById(eq(1001L));
			verify(driver, times(2)).sendSessionNotification(eq(sessionIteration1.getNotifyUri()), any(ChoreographerNotificationDTO.class));
			verify(planDBService, times(1)).getFirstSteps(eq(11L));
			verify(sessionDBService, never()).changeSessionStatus(eq(1001L), eq(ChoreographerSessionStatus.DONE), isNull());
			verify(sessionDataStorage, never()).remove(1001L);

			throw ex;
		}		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testReceiveSessionStepDoneMessageSessionDoneButNotificationFails() { 
		final ChoreographerExecutedStepResultDTO payload = new ChoreographerExecutedStepResultDTO();
		payload.setSessionId(1L);
		payload.setSessionStepId(1L);
		payload.setStatus(ChoreographerExecutedStepStatus.SUCCESS);
		
		final ChoreographerPlan plan = new ChoreographerPlan();
		plan.setId(13);
		plan.setName("name");
		final ChoreographerSession session = new ChoreographerSession();
		session.setId(1);
		session.setPlan(plan);
		session.setNotifyUri("http://localhost:2222/notify");
		session.setQuantityDone(1);
		session.setExecutionNumber(1);
		session.setStatus(ChoreographerSessionStatus.DONE);
		final ChoreographerAction action = new ChoreographerAction();
		action.setId(11);
		final ChoreographerStep step = new ChoreographerStep();
		step.setAction(action);
		final ChoreographerSessionStep sessionStep = new ChoreographerSessionStep();
		sessionStep.setId(1);
		sessionStep.setSession(session);
		sessionStep.setStep(step);
		final ChoreographerSessionStep prevStep = new ChoreographerSessionStep();
		prevStep.setStatus(ChoreographerSessionStepStatus.DONE);
		
		when(sessionDBService.changeSessionStepStatus(1, ChoreographerSessionStepStatus.DONE, "Step finished successfully.")).thenReturn(sessionStep);
		when(sessionDataStorage.containsKey(1L)).thenReturn(true);
		when(sessionDataStorage.get(1L)).thenReturn(new SessionExecutorCache(false, false));
		when(sessionDBService.getAllSessionStepBySessionIdAndActionId(1, 11)).thenReturn(List.of(prevStep));
		when(sessionDBService.increaseSessionQuantityDone(eq(1L))).thenReturn(session);
		when(sessionDBService.changeSessionStatus(eq(1L), eq(ChoreographerSessionStatus.DONE), isNull())).thenReturn(session);
		when(sessionDBService.getSessionById(eq(1L))).thenReturn(session);
		doThrow(ArrowheadException.class).when(driver).sendSessionNotification(eq(session.getNotifyUri()), any(ChoreographerNotificationDTO.class));
		
		testObject.receiveSessionStepDoneMessage(payload);
		
		verify(sessionDBService, times(1)).changeSessionStepStatus(1, ChoreographerSessionStepStatus.DONE, "Step finished successfully.");
		verify(sessionDataStorage, times(3)).containsKey(1L);
		verify(sessionDataStorage, times(3)).get(1L);
		verify(driver, never()).closeGatewayTunnels(anyList());
		verify(sessionDBService, times(1)).getAllSessionStepBySessionIdAndActionId(1, 11);
		verify(sessionDBService, times(1)).increaseSessionQuantityDone(eq(1L));
		verify(sessionDBService, times(1)).changeSessionStatus(eq(1L), eq(ChoreographerSessionStatus.DONE), isNull());
		verify(sessionDBService, times(2)).getSessionById(eq(1L));
		verify(driver, times(2)).sendSessionNotification(eq(session.getNotifyUri()), any(ChoreographerNotificationDTO.class));
		verify(sessionDataStorage, times(1)).remove(1L);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testReceiveSessionStepDoneMessageSessionDoneNoNotification() { 
		final ChoreographerExecutedStepResultDTO payload = new ChoreographerExecutedStepResultDTO();
		payload.setSessionId(1L);
		payload.setSessionStepId(1L);
		payload.setStatus(ChoreographerExecutedStepStatus.SUCCESS);
		
		final ChoreographerPlan plan = new ChoreographerPlan();
		plan.setId(13);
		plan.setName("name");
		final ChoreographerSession session = new ChoreographerSession();
		session.setId(1);
		session.setPlan(plan);
		session.setQuantityDone(1);
		session.setExecutionNumber(1);
		session.setStatus(ChoreographerSessionStatus.DONE);
		final ChoreographerAction action = new ChoreographerAction();
		action.setId(11);
		final ChoreographerStep step = new ChoreographerStep();
		step.setAction(action);
		final ChoreographerSessionStep sessionStep = new ChoreographerSessionStep();
		sessionStep.setId(1);
		sessionStep.setSession(session);
		sessionStep.setStep(step);
		final ChoreographerSessionStep prevStep = new ChoreographerSessionStep();
		prevStep.setStatus(ChoreographerSessionStepStatus.DONE);
		
		when(sessionDBService.changeSessionStepStatus(1, ChoreographerSessionStepStatus.DONE, "Step finished successfully.")).thenReturn(sessionStep);
		when(sessionDataStorage.containsKey(1L)).thenReturn(true);
		when(sessionDataStorage.get(1L)).thenReturn(new SessionExecutorCache(false, false));
		when(sessionDBService.getAllSessionStepBySessionIdAndActionId(1, 11)).thenReturn(List.of(prevStep));
		when(sessionDBService.increaseSessionQuantityDone(eq(1L))).thenReturn(session);
		when(sessionDBService.changeSessionStatus(eq(1L), eq(ChoreographerSessionStatus.DONE), isNull())).thenReturn(session);
		when(sessionDBService.getSessionById(eq(1L))).thenReturn(session);
		
		testObject.receiveSessionStepDoneMessage(payload);
		
		verify(sessionDBService, times(1)).changeSessionStepStatus(1, ChoreographerSessionStepStatus.DONE, "Step finished successfully.");
		verify(sessionDataStorage, times(3)).containsKey(1L);
		verify(sessionDataStorage, times(3)).get(1L);
		verify(driver, never()).closeGatewayTunnels(anyList());
		verify(sessionDBService, times(1)).getAllSessionStepBySessionIdAndActionId(1, 11);
		verify(sessionDBService, times(1)).changeSessionStatus(eq(1L), eq(ChoreographerSessionStatus.DONE), isNull());
		verify(sessionDBService, times(2)).getSessionById(eq(1L));
		verify(driver, never()).sendSessionNotification(anyString(), any(ChoreographerNotificationDTO.class));
		verify(sessionDataStorage, times(1)).remove(1L);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testReceiveSessionStepDoneMessageNextActionStart() { // executeAction is already tested with receiveStartSessionMessage, so we just make sure it is called 
		final ChoreographerExecutedStepResultDTO payload = new ChoreographerExecutedStepResultDTO();
		payload.setSessionId(1L);
		payload.setSessionStepId(1L);
		payload.setStatus(ChoreographerExecutedStepStatus.SUCCESS);
		
		final ChoreographerSession session = new ChoreographerSession();
		session.setId(1);
		final ChoreographerAction nextAction = new ChoreographerAction();
		final ChoreographerAction action = new ChoreographerAction();
		action.setId(11);
		action.setNextAction(nextAction);
		final ChoreographerStep step = new ChoreographerStep();
		step.setAction(action);
		final ChoreographerSessionStep sessionStep = new ChoreographerSessionStep();
		sessionStep.setId(1);
		sessionStep.setSession(session);
		sessionStep.setStep(step);
		
		when(sessionDBService.changeSessionStepStatus(1, ChoreographerSessionStepStatus.DONE, "Step finished successfully.")).thenReturn(sessionStep);
		when(planDBService.getFirstSteps(anyLong())).thenThrow(new ArrowheadException("early end")); // to finish the test earlier
		
		try {
			testObject.receiveSessionStepDoneMessage(payload);
		} catch (final Exception ex) {
			verify(sessionDBService, times(1)).changeSessionStepStatus(1, ChoreographerSessionStepStatus.DONE, "Step finished successfully.");
			verify(driver, never()).closeGatewayTunnels(anyList());
			verify(planDBService, times(1)).getFirstSteps(anyLong());
			
			Assert.assertEquals("early end", ex.getMessage());

			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testReceiveSessionStepDoneCannotStartNextStep() {  
		final ChoreographerExecutedStepResultDTO payload = new ChoreographerExecutedStepResultDTO();
		payload.setSessionId(1L);
		payload.setSessionStepId(1L);
		payload.setStatus(ChoreographerExecutedStepStatus.SUCCESS);
		
		final ChoreographerSession session = new ChoreographerSession();
		session.setId(1);
		final ChoreographerAction action = new ChoreographerAction();
		action.setId(11);
		final ChoreographerStep nextStep = new ChoreographerStep();
		final ChoreographerStep otherPrevStep = new ChoreographerStep();
		final ChoreographerStep step = new ChoreographerStep();
		step.setAction(action);
		step.setNextStepConnections(Set.of(new ChoreographerStepNextStepConnection(step, nextStep)));
		
		nextStep.setPreviousStepConnections(Set.of(new ChoreographerStepNextStepConnection(step, nextStep),
												   new ChoreographerStepNextStepConnection(otherPrevStep, nextStep)));
		final ChoreographerSessionStep sessionStep = new ChoreographerSessionStep();
		sessionStep.setId(1);
		sessionStep.setStep(step);
		sessionStep.setSession(session);
		sessionStep.setStatus(ChoreographerSessionStepStatus.DONE);
		
		final ChoreographerSessionStep otherPrevSessionStep = new ChoreographerSessionStep();
		otherPrevSessionStep.setStatus(ChoreographerSessionStepStatus.RUNNING);
		
		when(sessionDBService.changeSessionStepStatus(1, ChoreographerSessionStepStatus.DONE, "Step finished successfully.")).thenReturn(sessionStep);
		when(sessionDBService.getSessionStepBySessionIdAndSteps(eq(1L), anySet())).thenReturn(List.of(sessionStep, otherPrevSessionStep));
		
		testObject.receiveSessionStepDoneMessage(payload);

		verify(sessionDBService, times(1)).changeSessionStepStatus(1, ChoreographerSessionStepStatus.DONE, "Step finished successfully.");
		verify(driver, never()).closeGatewayTunnels(anyList());
		verify(sessionDBService, times(1)).getSessionStepBySessionIdAndSteps(eq(1L), anySet());
		verify(sessionDBService, never()).changeSessionStepStatus(eq(1), any(ChoreographerStep.class), eq(ChoreographerSessionStepStatus.RUNNING), anyString());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ChoreographerSessionException.class)
	public void testReceiveSessionStepDoneStartNextStep() { // executeStep is already tested with receiveStartSessionMessage, so we just make sure it is called  
		final ChoreographerExecutedStepResultDTO payload = new ChoreographerExecutedStepResultDTO();
		payload.setSessionId(1L);
		payload.setSessionStepId(1L);
		payload.setStatus(ChoreographerExecutedStepStatus.SUCCESS);
		
		final ChoreographerPlan plan = new ChoreographerPlan();
		plan.setName("plan");
		final ChoreographerSession session = new ChoreographerSession();
		session.setId(1);
		final ChoreographerAction action = new ChoreographerAction();
		action.setId(11);
		action.setName("action");
		action.setPlan(plan);
		final ChoreographerStep nextStep = new ChoreographerStep();
		nextStep.setName("nextStep");
		nextStep.setAction(action);
		final ChoreographerStep otherPrevStep = new ChoreographerStep();
		final ChoreographerStep step = new ChoreographerStep();
		step.setAction(action);
		step.setNextStepConnections(Set.of(new ChoreographerStepNextStepConnection(step, nextStep)));
		
		nextStep.setPreviousStepConnections(Set.of(new ChoreographerStepNextStepConnection(step, nextStep),
												   new ChoreographerStepNextStepConnection(otherPrevStep, nextStep)));
		final ChoreographerSessionStep sessionStep = new ChoreographerSessionStep();
		sessionStep.setId(1);
		sessionStep.setSession(session);
		sessionStep.setStep(step);
		sessionStep.setStatus(ChoreographerSessionStepStatus.DONE);
		
		final ChoreographerSessionStep otherPrevSessionStep = new ChoreographerSessionStep();
		otherPrevSessionStep.setStatus(ChoreographerSessionStepStatus.DONE);
		
		when(sessionDBService.changeSessionStepStatus(1, ChoreographerSessionStepStatus.DONE, "Step finished successfully.")).thenReturn(sessionStep);
		when(sessionDataStorage.get(1L)).thenReturn(new SessionExecutorCache(false, false));
		when(sessionDBService.getSessionStepBySessionIdAndSteps(eq(1L), anySet())).thenReturn(List.of(sessionStep, otherPrevSessionStep));
		when(sessionDataStorage.containsKey(eq(1L))).thenReturn(true);
		when(sessionDBService.changeSessionStepStatus(eq(1L), any(ChoreographerStep.class), eq(ChoreographerSessionStepStatus.RUNNING), anyString())).thenThrow(new ArrowheadException("early end")); // to finish the test earlier
		
		try {
			testObject.receiveSessionStepDoneMessage(payload);
		} catch (final Exception ex) {
			verify(sessionDBService, times(1)).changeSessionStepStatus(1, ChoreographerSessionStepStatus.DONE, "Step finished successfully.");
			verify(sessionDataStorage, times(2)).containsKey(1L);
			verify(sessionDataStorage, times(1)).get(1L);
			verify(driver, never()).closeGatewayTunnels(anyList());
			verify(sessionDBService, times(1)).getSessionStepBySessionIdAndSteps(eq(1L), anySet());
			verify(sessionDBService, times(1)).changeSessionStepStatus(eq(1L), any(ChoreographerStep.class), eq(ChoreographerSessionStepStatus.RUNNING), anyString());
			
			Assert.assertEquals(1, ((ChoreographerSessionException)ex).getSessionId());
			Assert.assertEquals("early end", ex.getCause().getMessage());
			
			throw ex;
		}
	}
	

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testAbortSessionNoNeedToSendAbortMessageToExecutor() {
		final ChoreographerPlan plan = new ChoreographerPlan();
		plan.setId(123);
		plan.setName("plan");
		final ChoreographerSession session = new ChoreographerSession();
		session.setId(1);
		session.setPlan(plan);
		session.setNotifyUri("https://localhost:11111/notify");
		session.setStatus(ChoreographerSessionStatus.ABORTED);
		final ChoreographerSessionStep sessionStep = new ChoreographerSessionStep();
		sessionStep.setId(2);
		sessionStep.setSession(session);

		final SessionExecutorCache cache = new SessionExecutorCache(true, false);
		cache.getGatewayTunnels().put(2L, List.of(33333));
		
		when(sessionDBService.getSessionById(1L)).thenReturn(session);
		when(sessionDBService.abortSession(1L, "message")).thenReturn(List.of(sessionStep));
		when(sessionDataStorage.containsKey(eq(1L))).thenReturn(true);
		when(sessionDataStorage.get(1L)).thenReturn(cache);
		doNothing().when(driver).closeGatewayTunnels(anyList());
		when(sessionDataStorage.remove(eq(1L))).thenReturn(cache);
		doNothing().when(sessionDBService).worklog(eq("plan"), eq(1L), anyLong(), eq("Session is aborted"), isNull());
		doNothing().when(driver).sendSessionNotification(anyString(), any(ChoreographerNotificationDTO.class));
		
		testObject.abortSession(1L, 2L, "message");
		
		verify(sessionDBService, times(2)).getSessionById(1L);
		verify(sessionDBService, times(1)).abortSession(1L, "message");
		verify(sessionDataStorage, times(3)).containsKey(1L);
		verify(sessionDataStorage, times(3)).get(1L);
		verify(driver, times(1)).closeGatewayTunnels(anyList());
		verify(driver, never()).abortExecutor(anyString(), anyInt(), anyString(), any(ChoreographerAbortStepRequestDTO.class));
		verify(sessionDataStorage, times(1)).remove(eq(1L));
		verify(sessionDBService, times(1)).worklog(eq("plan"), eq(1L), anyLong(), eq("Session is aborted"), isNull());
		verify(driver, times(1)).sendSessionNotification(anyString(), any(ChoreographerNotificationDTO.class));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testAbortSessionSendAbortMessageToExecutor() {
		final ChoreographerPlan plan = new ChoreographerPlan();
		plan.setId(123);
		plan.setName("plan");
		final ChoreographerSession session = new ChoreographerSession();
		session.setId(1);
		session.setPlan(plan);
		session.setStatus(ChoreographerSessionStatus.ABORTED);
		final ChoreographerSessionStep sessionStep = new ChoreographerSessionStep();
		sessionStep.setId(2);
		sessionStep.setSession(session);
		final ChoreographerExecutor executor = new ChoreographerExecutor();
		executor.setAddress("localhost");
		executor.setPort(3333);
		executor.setBaseUri("/exe");
		final ChoreographerSessionStep otherSessionStep = new ChoreographerSessionStep();
		otherSessionStep.setId(22);
		otherSessionStep.setExecutor(executor);
		
		final SessionExecutorCache cache = new SessionExecutorCache(false, false);
		
		when(sessionDBService.getSessionById(1L)).thenReturn(session);
		when(sessionDBService.abortSession(1L, "message")).thenReturn(List.of(sessionStep, otherSessionStep));
		when(sessionDataStorage.containsKey(eq(1L))).thenReturn(true);
		when(sessionDataStorage.get(1L)).thenReturn(cache);
		doNothing().when(driver).abortExecutor(eq("localhost"), eq(3333), eq("/exe"), any(ChoreographerAbortStepRequestDTO.class));
		when(sessionDataStorage.remove(eq(1L))).thenReturn(cache);
		doNothing().when(sessionDBService).worklog(eq("plan"), eq(1L), anyLong(), eq("Session is aborted"), isNull());
		
		testObject.abortSession(1L, 2L, "message");
		
		verify(sessionDBService, times(2)).getSessionById(1L);
		verify(sessionDBService, times(1)).abortSession(1L, "message");
		verify(sessionDataStorage, times(3)).containsKey(1L);
		verify(sessionDataStorage, times(3)).get(1L);
		verify(driver, never()).closeGatewayTunnels(anyList());
		verify(driver, times(1)).abortExecutor(eq("localhost"), eq(3333), eq("/exe"), any(ChoreographerAbortStepRequestDTO.class));
		verify(sessionDataStorage, times(1)).remove(eq(1L));
		verify(sessionDBService, times(1)).worklog(eq("plan"), eq(1L), anyLong(), eq("Session is aborted"), isNull());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testAbortSessionSendAbortMessageToExecutorWhichFailed() {
		final ChoreographerPlan plan = new ChoreographerPlan();
		plan.setId(123);
		plan.setName("plan");
		final ChoreographerSession session = new ChoreographerSession();
		session.setId(1);
		session.setPlan(plan);
		session.setStatus(ChoreographerSessionStatus.ABORTED);
		final ChoreographerSessionStep sessionStep = new ChoreographerSessionStep();
		sessionStep.setId(2);
		sessionStep.setSession(session);
		final ChoreographerAction action = new ChoreographerAction();
		action.setName("action");
		final ChoreographerStep step = new ChoreographerStep();
		step.setName("step");
		step.setAction(action);
		final ChoreographerExecutor executor = new ChoreographerExecutor();
		executor.setAddress("localhost");
		executor.setPort(3333);
		executor.setBaseUri("/exe");
		final ChoreographerSessionStep otherSessionStep = new ChoreographerSessionStep();
		otherSessionStep.setId(22);
		otherSessionStep.setStep(step);
		otherSessionStep.setExecutor(executor);
		
		final SessionExecutorCache cache = new SessionExecutorCache(false, false);
		
		when(sessionDBService.getSessionById(1L)).thenReturn(session);
		when(sessionDBService.abortSession(1L, "message")).thenReturn(List.of(sessionStep, otherSessionStep));
		when(sessionDataStorage.containsKey(1L)).thenReturn(true);
		when(sessionDataStorage.get(1L)).thenReturn(cache);
		doThrow(new ArrowheadException("failed")).when(driver).abortExecutor(eq("localhost"), eq(3333), eq("/exe"), any(ChoreographerAbortStepRequestDTO.class));
		doNothing().when(sessionDBService).worklog(eq("plan"), eq("action"), eq("step"), eq(1L), anyLong(), eq("Unable to send abort message to the executor"), eq("failed"));
		when(sessionDataStorage.remove(eq(1L))).thenReturn(cache);
		doNothing().when(sessionDBService).worklog(eq("plan"), eq(1L), anyLong(), eq("Session is aborted"), isNull());
		
		testObject.abortSession(1L, 2L, "message");
		
		verify(sessionDBService, times(2)).getSessionById(1L);
		verify(sessionDBService, times(1)).abortSession(1L, "message");
		verify(sessionDataStorage, times(3)).get(1L);
		verify(sessionDataStorage, times(3)).get(1L);
		verify(driver, never()).closeGatewayTunnels(anyList());
		verify(driver, times(1)).abortExecutor(eq("localhost"), eq(3333), eq("/exe"), any(ChoreographerAbortStepRequestDTO.class));
		verify(sessionDBService, times(1)).worklog(eq("plan"), eq("action"), eq("step"), eq(1L), anyLong(), eq("Unable to send abort message to the executor"), eq("failed"));
		verify(sessionDataStorage, times(1)).remove(eq(1L));
		verify(sessionDBService, times(1)).worklog(eq("plan"), eq(1L), anyLong(), eq("Session is aborted"), isNull());
	}
}