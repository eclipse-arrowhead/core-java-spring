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

package eu.arrowhead.core.choreographer.database.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.hibernate.HibernateException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.database.entity.ChoreographerAction;
import eu.arrowhead.common.database.entity.ChoreographerExecutor;
import eu.arrowhead.common.database.entity.ChoreographerPlan;
import eu.arrowhead.common.database.entity.ChoreographerSession;
import eu.arrowhead.common.database.entity.ChoreographerSessionStep;
import eu.arrowhead.common.database.entity.ChoreographerStep;
import eu.arrowhead.common.database.entity.ChoreographerWorklog;
import eu.arrowhead.common.database.repository.ChoreographerActionRepository;
import eu.arrowhead.common.database.repository.ChoreographerExecutorRepository;
import eu.arrowhead.common.database.repository.ChoreographerPlanRepository;
import eu.arrowhead.common.database.repository.ChoreographerSessionRepository;
import eu.arrowhead.common.database.repository.ChoreographerSessionStepRepository;
import eu.arrowhead.common.database.repository.ChoreographerStepRepository;
import eu.arrowhead.common.database.repository.ChoreographerWorklogRepository;
import eu.arrowhead.common.dto.internal.ChoreographerSessionStepStatus;
import eu.arrowhead.common.dto.shared.ChoreographerSessionStatus;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.InvalidParameterException;

@RunWith(SpringRunner.class)
public class ChoreographerSessionDBServiceTest {

	//=================================================================================================
	// members
	
	@InjectMocks
	private ChoreographerSessionDBService dbService;
	
	@Mock
	private ChoreographerPlanRepository planRepository;
	
	@Mock
	private ChoreographerActionRepository actionRepository;
	
	@Mock
	private ChoreographerSessionRepository sessionRepository;
	
	@Mock
	private ChoreographerStepRepository stepRepository;
	
	@Mock
	private ChoreographerSessionStepRepository sessionStepRepository;
	
	@Mock
	private ChoreographerExecutorRepository executorRepository;
	
	@Mock
	private ChoreographerWorklogRepository worklogRepository;
	
	private final long maxIteration = 2;
	
	//=================================================================================================
    // methods

	//-------------------------------------------------------------------------------------------------
	@Before
	public void setUp() {
		ReflectionTestUtils.setField(dbService, "maxIteration", maxIteration);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitiateSession() {
		final long planId = 1;
		final long quantityGoal = 1;
		final String notifyUri = "/notify";
		
		final ChoreographerPlan plan = new ChoreographerPlan("test-plan");		
		when(planRepository.findById(anyLong())).thenReturn(Optional.of(plan));
		final ArgumentCaptor<ChoreographerSession> sessionCaptor = ArgumentCaptor.forClass(ChoreographerSession.class);
		final ChoreographerSession expected = new ChoreographerSession(plan, ChoreographerSessionStatus.INITIATED, quantityGoal, notifyUri);
		expected.setId(10);
		when(sessionRepository.saveAndFlush(sessionCaptor.capture())).thenReturn(expected);
		final ArgumentCaptor<ChoreographerWorklog> worklogCaptor = ArgumentCaptor.forClass(ChoreographerWorklog.class);
		when(worklogRepository.saveAndFlush(worklogCaptor.capture())).thenReturn(new ChoreographerWorklog());
		
		final ChoreographerSession result = dbService.initiateSession(planId, quantityGoal, notifyUri);
		
		final ChoreographerSession sessionCaptured = sessionCaptor.getValue();
		assertEquals(expected.getPlan().getName(), sessionCaptured.getPlan().getName());
		assertEquals(expected.getStatus(), sessionCaptured.getStatus());
		assertEquals(expected.getNotifyUri(), sessionCaptured.getNotifyUri());
		
		assertEquals(expected.getPlan().getName(), result.getPlan().getName());
		assertEquals(expected.getStatus(), result.getStatus());
		assertEquals(expected.getNotifyUri(), result.getNotifyUri());
		
		final ChoreographerWorklog workLogCaptured = worklogCaptor.getValue();
		assertEquals(plan.getName(), workLogCaptured.getPlanName());
		assertEquals(expected.getId(), workLogCaptured.getSessionId().longValue());
		assertNull(workLogCaptured.getException());
		
		verify(planRepository, times(1)).findById(eq(planId));
		verify(worklogRepository, times(1)).saveAndFlush(any(ChoreographerWorklog.class));
		verify(sessionRepository, times(1)).saveAndFlush(any(ChoreographerSession.class));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testInitiateSession_ZeroQuantity() {
		try {
			dbService.initiateSession(1L, 0, "/notify");
			
		} catch (final IllegalArgumentException ex) {
			verify(planRepository, never()).findById(anyLong());
			verify(worklogRepository, never()).saveAndFlush(any());
			verify(sessionRepository, never()).saveAndFlush(any());
			assertEquals("quantity must be positive", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testInitiateSession_NegativeQuantity() {
		try {
			dbService.initiateSession(1L, -1, "/notify");
			
		} catch (final IllegalArgumentException ex) {
			verify(planRepository, never()).findById(anyLong());
			verify(worklogRepository, never()).saveAndFlush(any());
			verify(sessionRepository, never()).saveAndFlush(any());
			assertEquals("quantity must be positive", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testInitiateSession_PlanNotExists() {
		final long planId = 1;
		final long quantityGoal = 2;
		final String notifyUri = "/notify";
		
		when(planRepository.findById(anyLong())).thenReturn(Optional.empty());
		final ArgumentCaptor<ChoreographerWorklog> worklogCaptor = ArgumentCaptor.forClass(ChoreographerWorklog.class);
		when(worklogRepository.saveAndFlush(worklogCaptor.capture())).thenReturn(new ChoreographerWorklog());
		
		try {
			dbService.initiateSession(planId, quantityGoal, notifyUri);			
		} catch (final InvalidParameterException ex) {
			final ChoreographerWorklog workLogCaptured = worklogCaptor.getValue();
			assertEquals("Initiating plan has been failed", workLogCaptured.getMessage());
			assertEquals("InvalidParameterException: Plan with id 1 not exists", workLogCaptured.getException());
			
			verify(planRepository, times(1)).findById(eq(planId));
			verify(worklogRepository, times(1)).saveAndFlush(any(ChoreographerWorklog.class));
			verify(sessionRepository, never()).saveAndFlush(any());
			throw ex;
		}		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testInitiateSession_GreaterThanAllowedQuantiy() {
		final long planId = 1;
		final long quantityGoal = 3;
		final String notifyUri = "/notify";
		final ChoreographerPlan plan = new ChoreographerPlan("test-plan");
		
		when(planRepository.findById(anyLong())).thenReturn(Optional.of(plan));
		final ArgumentCaptor<ChoreographerWorklog> worklogCaptor = ArgumentCaptor.forClass(ChoreographerWorklog.class);
		when(worklogRepository.saveAndFlush(worklogCaptor.capture())).thenReturn(new ChoreographerWorklog());
		
		try {
			dbService.initiateSession(planId, quantityGoal, notifyUri);			
		} catch (final InvalidParameterException ex) {
			final ChoreographerWorklog workLogCaptured = worklogCaptor.getValue();
			assertEquals(plan.getName(), workLogCaptured.getPlanName());
			assertEquals("Initiating plan has been failed", workLogCaptured.getMessage());
			assertEquals("InvalidParameterException: Requested quantity (3) is more than allowed (2)", workLogCaptured.getException());
			
			verify(planRepository, times(1)).findById(eq(planId));
			verify(worklogRepository, times(1)).saveAndFlush(any(ChoreographerWorklog.class));
			verify(sessionRepository, never()).saveAndFlush(any());
			throw ex;
		}		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testInitiateSession_DatabaseException() {
		final long planId = 1;
		final long quantityGoal = 1;
		final String notifyUri = "/notify";
		
		when(planRepository.findById(anyLong())).thenThrow(new HibernateException("test"));
		
		try {
			dbService.initiateSession(planId, quantityGoal, notifyUri);			
		} catch (final ArrowheadException ex) {			
			verify(planRepository, times(1)).findById(eq(planId));
			verify(worklogRepository, never()).saveAndFlush(any());
			verify(sessionRepository, never()).saveAndFlush(any());
			throw ex;
		}		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testChangeSessionStatus() {
		final long sessionId = 1;
		final ChoreographerSessionStatus newStatus = ChoreographerSessionStatus.ABORTED ;
		final String errorMessage = "error";
		
		final ChoreographerSession session = new ChoreographerSession();
		session.setId(sessionId);
		session.setPlan(new ChoreographerPlan("test-plan"));
		session.setStatus(ChoreographerSessionStatus.INITIATED);
		session.setNotifyUri("/notify");
		when(sessionRepository.findById(anyLong())).thenReturn(Optional.of(session));
		final ArgumentCaptor<ChoreographerSession> sessionCaptor = ArgumentCaptor.forClass(ChoreographerSession.class);
		when(sessionRepository.saveAndFlush(sessionCaptor.capture())).thenReturn(session);
		final ArgumentCaptor<ChoreographerWorklog> worklogCaptor = ArgumentCaptor.forClass(ChoreographerWorklog.class);
		when(worklogRepository.saveAndFlush(worklogCaptor.capture())).thenReturn(new ChoreographerWorklog());
		
		final ChoreographerSession result = dbService.changeSessionStatus(sessionId, newStatus, errorMessage);
		
		assertEquals(session, sessionCaptor.getValue());
		assertEquals(session, result);
		assertEquals(newStatus.name(), result.getStatus().name());
		
		final ChoreographerWorklog workLogCaptured = worklogCaptor.getValue();
		assertEquals(session.getPlan().getName(), workLogCaptured.getPlanName());
		assertEquals(session.getId(), workLogCaptured.getSessionId().longValue());
		assertTrue(workLogCaptured.getMessage().contains(newStatus.name()));
		assertEquals(errorMessage, workLogCaptured.getException());
		
		verify(sessionRepository, times(1)).findById(eq(sessionId));
		verify(sessionRepository, times(1)).saveAndFlush(any(ChoreographerSession.class));
		verify(worklogRepository, times(1)).saveAndFlush(any(ChoreographerWorklog.class));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testChangeSessionStatus_SessionNotExists() {
		final long sessionId = 1;
		final ChoreographerSessionStatus newStatus = ChoreographerSessionStatus.ABORTED ;
		final String errorMessage = "error";
		
		when(sessionRepository.findById(anyLong())).thenReturn(Optional.empty());
		final ArgumentCaptor<ChoreographerWorklog> worklogCaptor = ArgumentCaptor.forClass(ChoreographerWorklog.class);
		when(worklogRepository.saveAndFlush(worklogCaptor.capture())).thenReturn(new ChoreographerWorklog());
		
		try {
			dbService.changeSessionStatus(sessionId, newStatus, errorMessage);			
			
		} catch (final InvalidParameterException ex) {
			final ChoreographerWorklog workLogCaptured = worklogCaptor.getValue();
			assertNotNull(workLogCaptured.getMessage());
			assertNotNull(workLogCaptured.getException());
			
			verify(sessionRepository, times(1)).findById(eq(sessionId));
			verify(sessionRepository, never()).saveAndFlush(any());
			verify(worklogRepository, times(1)).saveAndFlush(any(ChoreographerWorklog.class));
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testChangeSessionStatus_DatabaseException() {
		final long sessionId = 1;
		final ChoreographerSessionStatus newStatus = ChoreographerSessionStatus.ABORTED ;
		final String errorMessage = "error";
		
		when(sessionRepository.findById(anyLong())).thenThrow(new HibernateException("test"));
		
		try {
			dbService.changeSessionStatus(sessionId, newStatus, errorMessage);			
			
		} catch (final ArrowheadException ex) {
			
			verify(sessionRepository, times(1)).findById(eq(sessionId));
			verify(sessionRepository, never()).saveAndFlush(any());
			verify(worklogRepository, never()).saveAndFlush(any());
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testIncreaseSessionQuantityDone() {
		final long sessionId = 2;
		final ChoreographerSession session = new ChoreographerSession();
		session.setId(sessionId);
		session.setPlan(new ChoreographerPlan("test"));
		session.setQuantityGoal(1);
		
		when(sessionRepository.findById(eq(sessionId))).thenReturn(Optional.of(session));
		final ArgumentCaptor<ChoreographerSession> sessionCaptor = ArgumentCaptor.forClass(ChoreographerSession.class);
		when(sessionRepository.saveAndFlush(sessionCaptor.capture())).thenReturn(session);
		final ArgumentCaptor<ChoreographerWorklog> worklogCaptor = ArgumentCaptor.forClass(ChoreographerWorklog.class);
		when(worklogRepository.saveAndFlush(worklogCaptor.capture())).thenReturn(new ChoreographerWorklog());
		
		final ChoreographerSession result = dbService.increaseSessionQuantityDone(sessionId);
		
		assertEquals(session, sessionCaptor.getValue());
		assertEquals(session, result);
		assertEquals(1, result.getQuantityDone());
		
		final ChoreographerWorklog workLogCaptured = worklogCaptor.getValue();
		assertEquals(session.getPlan().getName(), workLogCaptured.getPlanName());
		assertEquals(session.getId(), workLogCaptured.getSessionId().longValue());
		assertEquals("Session quantityDone has been changed to 1", workLogCaptured.getMessage());
		assertNull(workLogCaptured.getException());
		
		verify(sessionRepository, times(1)).findById(eq(sessionId));
		verify(sessionRepository, times(1)).saveAndFlush(any(ChoreographerSession.class));
		verify(worklogRepository, times(1)).saveAndFlush(any(ChoreographerWorklog.class));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testIncreaseSessionQuantityDone_NoSession() {
		final long sessionId = 2;
		
		when(sessionRepository.findById(eq(sessionId))).thenReturn(Optional.empty());
		final ArgumentCaptor<ChoreographerWorklog> worklogCaptor = ArgumentCaptor.forClass(ChoreographerWorklog.class);
		when(worklogRepository.saveAndFlush(worklogCaptor.capture())).thenReturn(new ChoreographerWorklog());
		
		try {
			dbService.increaseSessionQuantityDone(sessionId);
			
		} catch (final InvalidParameterException ex) {
			final ChoreographerWorklog workLogCaptured = worklogCaptor.getValue();
			assertEquals("Session quantityDone change has been failed", workLogCaptured.getMessage());
			assertEquals("InvalidParameterException: Session with id 2 not exists", workLogCaptured.getException());
			
			verify(sessionRepository, times(1)).findById(eq(sessionId));
			verify(sessionRepository, never()).saveAndFlush(any());
			verify(worklogRepository, times(1)).saveAndFlush(any(ChoreographerWorklog.class));

			throw ex;
		}		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testIncreaseSessionQuantityDone_GreaterQuantityThanGoal() {
		final long sessionId = 2;
		final ChoreographerSession session = new ChoreographerSession();
		session.setId(sessionId);
		session.setPlan(new ChoreographerPlan("test"));
		session.setQuantityDone(1);
		session.setQuantityGoal(1);
		
		when(sessionRepository.findById(eq(sessionId))).thenReturn(Optional.of(session));
		final ArgumentCaptor<ChoreographerWorklog> worklogCaptor = ArgumentCaptor.forClass(ChoreographerWorklog.class);
		when(worklogRepository.saveAndFlush(worklogCaptor.capture())).thenReturn(new ChoreographerWorklog());
		
		try {
			dbService.increaseSessionQuantityDone(sessionId);
			
		} catch (final InvalidParameterException ex) {
			final ChoreographerWorklog workLogCaptured = worklogCaptor.getValue();
			assertEquals(session.getPlan().getName(), workLogCaptured.getPlanName());
			assertEquals(session.getId(), workLogCaptured.getSessionId().longValue());
			assertEquals("Session quantityDone is invalid", workLogCaptured.getMessage());
			assertEquals("InvalidParameterException: Session quantityDone is greater than quantityGoal", workLogCaptured.getException());
			
			verify(sessionRepository, times(1)).findById(eq(sessionId));
			verify(sessionRepository, never()).saveAndFlush(any(ChoreographerSession.class));
			verify(worklogRepository, times(1)).saveAndFlush(any(ChoreographerWorklog.class));

			throw ex;
		}		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testIncreaseSessionQuantityDone_DatabaseException() {
		when(sessionRepository.findById(anyLong())).thenThrow(new HibernateException("test"));		
		
		try {
			dbService.increaseSessionQuantityDone(2);
			
		} catch (final ArrowheadException ex) {
			verify(sessionRepository, times(1)).findById(eq(2L));
			verify(sessionRepository, never()).saveAndFlush(any());
			verify(worklogRepository, never()).saveAndFlush(any(ChoreographerWorklog.class));

			throw ex;
		}		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testIncreaseExecutionNumber() {
		final long sessionId = 2;
		final ChoreographerSession session = new ChoreographerSession();
		session.setId(sessionId);
		session.setPlan(new ChoreographerPlan("test"));
		session.setQuantityGoal(1);
		
		when(sessionRepository.findById(eq(sessionId))).thenReturn(Optional.of(session));
		final ArgumentCaptor<ChoreographerSession> sessionCaptor = ArgumentCaptor.forClass(ChoreographerSession.class);
		when(sessionRepository.saveAndFlush(sessionCaptor.capture())).thenReturn(session);
		final ArgumentCaptor<ChoreographerWorklog> worklogCaptor = ArgumentCaptor.forClass(ChoreographerWorklog.class);
		when(worklogRepository.saveAndFlush(worklogCaptor.capture())).thenReturn(new ChoreographerWorklog());
		
		final ChoreographerSession result = dbService.increaseExecutionNumber(sessionId);
		
		assertEquals(session, sessionCaptor.getValue());
		assertEquals(session, result);
		assertEquals(1, result.getExecutionNumber());
		
		final ChoreographerWorklog workLogCaptured = worklogCaptor.getValue();
		assertEquals(session.getPlan().getName(), workLogCaptured.getPlanName());
		assertEquals(session.getId(), workLogCaptured.getSessionId().longValue());
		assertEquals("Session executionNumber has been changed to 1", workLogCaptured.getMessage());
		assertNull(workLogCaptured.getException());
		
		verify(sessionRepository, times(1)).findById(eq(sessionId));
		verify(sessionRepository, times(1)).saveAndFlush(any(ChoreographerSession.class));
		verify(worklogRepository, times(1)).saveAndFlush(any(ChoreographerWorklog.class));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testIncreaseExecutionNumber_NoSession() {
		final long sessionId = 2;
		
		when(sessionRepository.findById(eq(sessionId))).thenReturn(Optional.empty());
		final ArgumentCaptor<ChoreographerWorklog> worklogCaptor = ArgumentCaptor.forClass(ChoreographerWorklog.class);
		when(worklogRepository.saveAndFlush(worklogCaptor.capture())).thenReturn(new ChoreographerWorklog());
		
		try {
			dbService.increaseExecutionNumber(sessionId);
			
		} catch (final InvalidParameterException ex) {
			final ChoreographerWorklog workLogCaptured = worklogCaptor.getValue();
			assertEquals("Session executionNumber change has been failed", workLogCaptured.getMessage());
			assertEquals("InvalidParameterException: Session with id 2 not exists", workLogCaptured.getException());
			
			verify(sessionRepository, times(1)).findById(eq(sessionId));
			verify(sessionRepository, never()).saveAndFlush(any());
			verify(worklogRepository, times(1)).saveAndFlush(any(ChoreographerWorklog.class));

			throw ex;
		}		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testIncreaseExecutionNumber_GreaterExecutionNumThanGoal() {
		final long sessionId = 2;
		final ChoreographerSession session = new ChoreographerSession();
		session.setId(sessionId);
		session.setPlan(new ChoreographerPlan("test"));
		session.setExecutionNumber(1);
		session.setQuantityGoal(1);
		
		when(sessionRepository.findById(eq(sessionId))).thenReturn(Optional.of(session));
		final ArgumentCaptor<ChoreographerWorklog> worklogCaptor = ArgumentCaptor.forClass(ChoreographerWorklog.class);
		when(worklogRepository.saveAndFlush(worklogCaptor.capture())).thenReturn(new ChoreographerWorklog());
		
		try {
			dbService.increaseExecutionNumber(sessionId);
			
		} catch (final InvalidParameterException ex) {
			final ChoreographerWorklog workLogCaptured = worklogCaptor.getValue();
			assertEquals(session.getPlan().getName(), workLogCaptured.getPlanName());
			assertEquals(session.getId(), workLogCaptured.getSessionId().longValue());
			assertEquals("Session executionNumber is invalid", workLogCaptured.getMessage());
			assertEquals("InvalidParameterException: Session executionNumber is greater than quantityGoal", workLogCaptured.getException());
			
			verify(sessionRepository, times(1)).findById(eq(sessionId));
			verify(sessionRepository, never()).saveAndFlush(any(ChoreographerSession.class));
			verify(worklogRepository, times(1)).saveAndFlush(any(ChoreographerWorklog.class));

			throw ex;
		}		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testIncreaseExecutionNumber_DatabaseException() {
		when(sessionRepository.findById(anyLong())).thenThrow(new HibernateException("test"));		
		
		try {
			dbService.increaseExecutionNumber(2);
			
		} catch (final ArrowheadException ex) {
			verify(sessionRepository, times(1)).findById(eq(2L));
			verify(sessionRepository, never()).saveAndFlush(any());
			verify(worklogRepository, never()).saveAndFlush(any(ChoreographerWorklog.class));

			throw ex;
		}		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testAbortSession() {
		final long sessionId = 1l;
		final String message = "error";
	
		final ChoreographerSession session = new ChoreographerSession();
		session.setId(sessionId);
		session.setPlan(new ChoreographerPlan("test-plan"));
		session.setStatus(ChoreographerSessionStatus.RUNNING);
		session.setNotifyUri("/notify");
		
		final ChoreographerAction action = new ChoreographerAction();
		action.setName("action");
		final ChoreographerStep step1 = new ChoreographerStep();
		step1.setName("step1");
		step1.setAction(action);
		final ChoreographerSessionStep sessionStep1 = new ChoreographerSessionStep();
		sessionStep1.setId(1);
		sessionStep1.setSession(session);
		sessionStep1.setStep(step1);
		sessionStep1.setStatus(ChoreographerSessionStepStatus.RUNNING);
		final ChoreographerStep step2 = new ChoreographerStep();
		step2.setName("step2");
		step2.setAction(action);
		final ChoreographerSessionStep sessionStep2 = new ChoreographerSessionStep();
		sessionStep2.setId(2);
		sessionStep2.setSession(session);
		sessionStep2.setStep(step2);
		sessionStep2.setStatus(ChoreographerSessionStepStatus.WAITING);
		
		when(sessionRepository.findById(anyLong())).thenReturn(Optional.of(session));
		final ArgumentCaptor<ChoreographerSession> sessionCaptor = ArgumentCaptor.forClass(ChoreographerSession.class);
		when(sessionRepository.saveAndFlush(sessionCaptor.capture())).thenReturn(session);
		when(worklogRepository.saveAndFlush(any())).thenReturn(new ChoreographerWorklog());
		when(sessionStepRepository.findAllBySession(any())).thenReturn(List.of(sessionStep1, sessionStep2));
		when(sessionStepRepository.findById(eq(sessionStep1.getId()))).thenReturn(Optional.of(sessionStep1));
		when(sessionStepRepository.findById(eq(sessionStep2.getId()))).thenReturn(Optional.of(sessionStep2));
		final ArgumentCaptor<ChoreographerSessionStep> sessionStepCaptor = ArgumentCaptor.forClass(ChoreographerSessionStep.class);
		when(sessionStepRepository.saveAndFlush(sessionStepCaptor.capture())).thenReturn(sessionStep1).thenReturn(sessionStep2);
		
		final List<ChoreographerSessionStep> result = dbService.abortSession(sessionId, message);
		
		final ChoreographerSession sessionCaptured = sessionCaptor.getValue();		
		assertEquals(session.getId(), sessionCaptured.getId());
		assertEquals(ChoreographerSessionStatus.ABORTED, sessionCaptured.getStatus());
		
		final List<ChoreographerSessionStep> sessionStepsCaptured = sessionStepCaptor.getAllValues();
		assertTrue(sessionStepsCaptured.size() == 2);
		assertEquals(sessionStep1.getId(), sessionStepsCaptured.get(0).getId());
		assertEquals(ChoreographerSessionStepStatus.ABORTED, sessionStepsCaptured.get(0).getStatus());
		assertEquals(sessionStep2.getId(), sessionStepsCaptured.get(1).getId());
		assertEquals(ChoreographerSessionStepStatus.ABORTED, sessionStepsCaptured.get(1).getStatus());
		
		assertTrue(result.size() == 1);
		assertEquals(sessionStep1.getId(), result.get(0).getId());
		
		verify(sessionRepository, times(2)).findById(eq(sessionId));
		verify(sessionRepository, times(1)).saveAndFlush(eq(session));
		verify(sessionStepRepository, times(1)).findAllBySession(eq(session));
		verify(sessionStepRepository, times(1)).findById(eq(sessionStep1.getId()));
		verify(sessionStepRepository, times(1)).findById(eq(sessionStep2.getId()));	
		verify(sessionStepRepository, times(1)).saveAndFlush(eq(sessionStep1));
		verify(sessionStepRepository, times(1)).saveAndFlush(eq(sessionStep2));
		verify(worklogRepository, times(4)).saveAndFlush(any(ChoreographerWorklog.class));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testAbortSession_SessionNotExists() {
		final long sessionId = 1l;
		final String message = "error";
		
		when(sessionRepository.findById(anyLong())).thenReturn(Optional.empty());
		
		try {
			dbService.abortSession(sessionId, message);			
		} catch (final InvalidParameterException ex) {
			verify(sessionRepository, times(1)).findById(eq(sessionId));
			verify(sessionRepository, never()).saveAndFlush(any());
			verify(sessionStepRepository, never()).findAllBySession(any());
			verify(sessionStepRepository, never()).findById(any());
			verify(sessionStepRepository, never()).findById(any());	
			verify(sessionStepRepository, never()).saveAndFlush(any());
			verify(sessionStepRepository, never()).saveAndFlush(any());
			verify(worklogRepository, times(1)).saveAndFlush(any(ChoreographerWorklog.class));
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testAbortSession_NoSessionSteps() {
		final long sessionId = 1l;
		final String message = "error";
	
		final ChoreographerSession session = new ChoreographerSession();
		session.setId(sessionId);
		session.setPlan(new ChoreographerPlan("test-plan"));
		session.setStatus(ChoreographerSessionStatus.RUNNING);
		session.setNotifyUri("/notify");
		
		when(sessionRepository.findById(anyLong())).thenReturn(Optional.of(session));
		final ArgumentCaptor<ChoreographerSession> sessionCaptor = ArgumentCaptor.forClass(ChoreographerSession.class);
		when(sessionRepository.saveAndFlush(sessionCaptor.capture())).thenReturn(session);
		when(worklogRepository.saveAndFlush(any())).thenReturn(new ChoreographerWorklog());
		when(sessionStepRepository.findAllBySession(any())).thenReturn(List.of());
		
		final List<ChoreographerSessionStep> result = dbService.abortSession(sessionId, message);
		
		final ChoreographerSession sessionCaptured = sessionCaptor.getValue();		
		assertEquals(session.getId(), sessionCaptured.getId());
		assertEquals(ChoreographerSessionStatus.ABORTED, sessionCaptured.getStatus());
		
		assertTrue(result.isEmpty());
		
		verify(sessionRepository, times(2)).findById(eq(sessionId));
		verify(sessionRepository, times(1)).saveAndFlush(eq(session));
		verify(sessionStepRepository, times(1)).findAllBySession(eq(session));
		verify(sessionStepRepository, never()).findById(any());	
		verify(sessionStepRepository, never()).saveAndFlush(any());
		verify(worklogRepository, times(2)).saveAndFlush(any(ChoreographerWorklog.class));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testAbortSession_DatabaseException() {
		final long sessionId = 1l;
		final String message = "error";
		
		when(sessionRepository.findById(anyLong())).thenThrow(new HibernateException("test"));
		
		try {
			dbService.abortSession(sessionId, message);			
		} catch (final ArrowheadException ex) {
			verify(sessionRepository, times(1)).findById(eq(sessionId));
			verify(sessionRepository, never()).saveAndFlush(any());
			verify(sessionStepRepository, never()).findAllBySession(any());
			verify(sessionStepRepository, never()).findById(any());
			verify(sessionStepRepository, never()).findById(any());	
			verify(sessionStepRepository, never()).saveAndFlush(any());
			verify(sessionStepRepository, never()).saveAndFlush(any());
			verify(worklogRepository, never()).saveAndFlush(any(ChoreographerWorklog.class));
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetSessionById() {
		final ChoreographerSession session = new ChoreographerSession();
		session.setId(1);
		
		when(sessionRepository.findById(anyLong())).thenReturn(Optional.of(session));
		
		final ChoreographerSession result = dbService.getSessionById(1);
		
		assertEquals(session.getId(), result.getId());
		verify(sessionRepository, times(1)).findById(eq(session.getId()));		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGetSessionById_SessionNotExists() {
		when(sessionRepository.findById(anyLong())).thenReturn(Optional.empty());
		
		try {
			dbService.getSessionById(1);
			
		} catch (final InvalidParameterException ex) {
			verify(sessionRepository, times(1)).findById(eq(1L));		
			throw ex;
		}
		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testGetSessionById_DatabaseException() {
		when(sessionRepository.findById(anyLong())).thenThrow(new HibernateException("test"));
		
		try {
			dbService.getSessionById(1);
			
		} catch (final ArrowheadException ex) {
			verify(sessionRepository, times(1)).findById(eq(1L));		
			throw ex;
		}		
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("unchecked")
	@Test
	public void testGetSessions() {
		final int page = 6;
		final int size = 50;
		final Direction direction = Direction.ASC;
		final String sortField = "test";
		final Long planId = 1L;
		final ChoreographerSessionStatus status = ChoreographerSessionStatus.DONE;
		
		final ChoreographerPlan plan = new ChoreographerPlan("test-plan");
		final ChoreographerSession session = new ChoreographerSession();
		session.setId(17);
		
		when(planRepository.findById(anyLong())).thenReturn(Optional.of(plan));
		final ArgumentCaptor<Example<ChoreographerSession>> exampleCaptor = ArgumentCaptor.forClass(Example.class);	
		final ArgumentCaptor<PageRequest> pageRequestCaptor = ArgumentCaptor.forClass(PageRequest.class);
		when(sessionRepository.findAll(exampleCaptor.capture(), pageRequestCaptor.capture())).thenReturn(new PageImpl<ChoreographerSession>(List.of(session)));
		
		final Page<ChoreographerSession> result = dbService.getSessions(page, size, direction, sortField, planId, status);
		
		final Example<ChoreographerSession> exampleCaptured = exampleCaptor.getValue();
		assertEquals(status, exampleCaptured.getProbe().getStatus());
		assertEquals(plan.getName(), exampleCaptured.getProbe().getPlan().getName());
		
		final PageRequest pageRequestCaptured = pageRequestCaptor.getValue();
		assertEquals(page, pageRequestCaptured.getPageNumber());
		assertEquals(size, pageRequestCaptured.getPageSize());
		assertEquals(direction, pageRequestCaptured.getSort().getOrderFor(sortField).getDirection());
		
		assertTrue(result.getContent().size() == 1);
		assertEquals(session.getId(), result.getContent().get(0).getId());
		
		verify(planRepository, times(1)).findById(eq(planId.longValue()));
		verify(sessionRepository, times(1)).findAll(any(Example.class), any(PageRequest.class));
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("unchecked")
	@Test
	public void testGetSessions_UndefinedParameters() {
		final int page = -1;
		final int size = -1;
		final Direction direction = null;
		final String sortField = null;
		final Long planId = null;
		final ChoreographerSessionStatus status = null;
		
		
		final ChoreographerSession session = new ChoreographerSession();
		session.setId(17);
		
		
		final ArgumentCaptor<Example<ChoreographerSession>> exampleCaptor = ArgumentCaptor.forClass(Example.class);	
		final ArgumentCaptor<PageRequest> pageRequestCaptor = ArgumentCaptor.forClass(PageRequest.class);
		when(sessionRepository.findAll(exampleCaptor.capture(), pageRequestCaptor.capture())).thenReturn(new PageImpl<ChoreographerSession>(List.of(session)));
		
		final Page<ChoreographerSession> result = dbService.getSessions(page, size, direction, sortField, planId, status);
		
		final Example<ChoreographerSession> exampleCaptured = exampleCaptor.getValue();
		assertNull(exampleCaptured.getProbe().getStatus());
		assertNull(exampleCaptured.getProbe().getPlan());
		
		final PageRequest pageRequestCaptured = pageRequestCaptor.getValue();
		assertEquals(0, pageRequestCaptured.getPageNumber());
		assertEquals(Integer.MAX_VALUE, pageRequestCaptured.getPageSize());
		assertEquals(Direction.ASC, pageRequestCaptured.getSort().getOrderFor(CoreCommonConstants.COMMON_FIELD_NAME_ID).getDirection());
		
		assertTrue(result.getContent().size() == 1);
		assertEquals(session.getId(), result.getContent().get(0).getId());
		
		verify(planRepository, never()).findById(anyLong());
		verify(sessionRepository, times(1)).findAll(any(Example.class), any(PageRequest.class));
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("unchecked")
	@Test(expected = InvalidParameterException.class)
	public void testGetSessions_PlanNotExists() {
		final int page = 6;
		final int size = 50;
		final Direction direction = Direction.ASC;
		final String sortField = "test";
		final Long planId = 1L;
		final ChoreographerSessionStatus status = ChoreographerSessionStatus.DONE;
		
		when(planRepository.findById(anyLong())).thenReturn(Optional.empty());		
		
		try {
			dbService.getSessions(page, size, direction, sortField, planId, status);
			
		} catch (final InvalidParameterException ex) {
			verify(planRepository, times(1)).findById(eq(planId.longValue()));
			verify(sessionRepository, never()).findAll(any(Example.class), any(PageRequest.class));
			throw ex;
		}				
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("unchecked")
	@Test(expected = ArrowheadException.class)
	public void testGetSessions_DatabaseException() {
		final int page = 6;
		final int size = 50;
		final Direction direction = Direction.ASC;
		final String sortField = "test";
		final Long planId = 1L;
		final ChoreographerSessionStatus status = ChoreographerSessionStatus.DONE;
		
		when(planRepository.findById(anyLong())).thenReturn(Optional.empty());		
		
		try {
			dbService.getSessions(page, size, direction, sortField, planId, status);
			
		} catch (final ArrowheadException ex) {
			verify(planRepository, times(1)).findById(eq(planId.longValue()));
			verify(sessionRepository, never()).findAll(any(Example.class), any(PageRequest.class));
			throw ex;
		}				
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testRegisterSessionStep() {
		final long sessionId = 5;
		final long stepId = 150;
		final long executorId = 4;
		
		final ChoreographerSession session = new ChoreographerSession();
		session.setId(sessionId);
		session.setPlan(new ChoreographerPlan("test-plan"));
		final ChoreographerStep step = new ChoreographerStep();
		step.setId(stepId);
		step.setName("test-step");
		step.setAction(new ChoreographerAction("test-action", null));
		final ChoreographerExecutor executor = new ChoreographerExecutor();
		executor.setId(executorId);
		executor.setName("test-executor");
		final ChoreographerSessionStep saved = new ChoreographerSessionStep();
		saved.setId(456);
		
		when(sessionRepository.findById(anyLong())).thenReturn(Optional.of(session));
		when(stepRepository.findById(anyLong())).thenReturn(Optional.of(step));
		when(executorRepository.findById(anyLong())).thenReturn(Optional.of(executor));
		final ArgumentCaptor<ChoreographerSessionStep> sessionStepCaptor = ArgumentCaptor.forClass(ChoreographerSessionStep.class);
		when(sessionStepRepository.saveAndFlush(sessionStepCaptor.capture())).thenReturn(saved);
		when(worklogRepository.saveAndFlush(any())).thenReturn(new ChoreographerWorklog());
		
		final ChoreographerSessionStep result = dbService.registerSessionStep(sessionId, stepId, executorId);
		
		final ChoreographerSessionStep sessionStepCaptured = sessionStepCaptor.getValue();
		assertEquals(sessionId, sessionStepCaptured.getSession().getId());
		assertEquals(stepId, sessionStepCaptured.getStep().getId());
		assertEquals(executorId, sessionStepCaptured.getExecutor().getId());
		
		assertEquals(saved.getId(), result.getId());
		
		verify(sessionRepository, times(1)).findById(eq(sessionId));
		verify(stepRepository, times(1)).findById(eq(stepId));
		verify(executorRepository, times(1)).findById(eq(executorId));
		verify(sessionStepRepository, times(1)).saveAndFlush(any(ChoreographerSessionStep.class));
		verify(worklogRepository, times(1)).saveAndFlush(any(ChoreographerWorklog.class));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testRegisterSessionStep_SessionNotExists() {
		final long sessionId = 5;
		final long stepId = 150;
		final long executorId = 4;
		
		when(sessionRepository.findById(anyLong())).thenReturn(Optional.empty());
		
		try {
			dbService.registerSessionStep(sessionId, stepId, executorId);
			
		} catch (final InvalidParameterException ex) {
			verify(sessionRepository, times(1)).findById(eq(sessionId));
			verify(stepRepository, never()).findById(any());
			verify(executorRepository, never()).findById(any());
			verify(sessionStepRepository, never()).saveAndFlush(any());
			verify(worklogRepository, times(1)).saveAndFlush(any(ChoreographerWorklog.class));
			throw ex;
		}		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testRegisterSessionStep_StepNotExists() {
		final long sessionId = 5;
		final long stepId = 150;
		final long executorId = 4;
		
		final ChoreographerSession session = new ChoreographerSession();
		session.setId(sessionId);
		session.setPlan(new ChoreographerPlan("test-plan"));
		
		when(sessionRepository.findById(anyLong())).thenReturn(Optional.of(session));
		when(stepRepository.findById(anyLong())).thenReturn(Optional.empty());
		
		try {
			dbService.registerSessionStep(sessionId, stepId, executorId);		
			
		} catch (final InvalidParameterException ex) {
			verify(sessionRepository, times(1)).findById(eq(sessionId));
			verify(stepRepository, times(1)).findById(eq(stepId));
			verify(executorRepository, never()).findById(any());
			verify(sessionStepRepository, never()).saveAndFlush(any());
			verify(worklogRepository, times(1)).saveAndFlush(any(ChoreographerWorklog.class));
			throw ex;
		}		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testRegisterSessionStep_ExecutorNotExists() {
		final long sessionId = 5;
		final long stepId = 150;
		final long executorId = 4;
		
		final ChoreographerSession session = new ChoreographerSession();
		session.setId(sessionId);
		session.setPlan(new ChoreographerPlan("test-plan"));
		final ChoreographerStep step = new ChoreographerStep();
		step.setId(stepId);
		step.setName("test-step");
		step.setAction(new ChoreographerAction("test-action", null));
		
		when(sessionRepository.findById(anyLong())).thenReturn(Optional.of(session));
		when(stepRepository.findById(anyLong())).thenReturn(Optional.of(step));
		when(executorRepository.findById(anyLong())).thenReturn(Optional.empty());
		
		try {
			dbService.registerSessionStep(sessionId, stepId, executorId);
			
		} catch (final InvalidParameterException ex) {
			verify(sessionRepository, times(1)).findById(eq(sessionId));
			verify(stepRepository, times(1)).findById(eq(stepId));
			verify(executorRepository, times(1)).findById(eq(executorId));
			verify(sessionStepRepository, never()).saveAndFlush(any());
			verify(worklogRepository, times(1)).saveAndFlush(any(ChoreographerWorklog.class));
			throw ex;
		}		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testRegisterSessionStep_DatabaseException() {
		final long sessionId = 5;
		final long stepId = 150;
		final long executorId = 4;
		
		when(sessionRepository.findById(anyLong())).thenThrow(new HibernateException("test"));
		
		try {
			dbService.registerSessionStep(sessionId, stepId, executorId);
			
		} catch (final ArrowheadException ex) {
			verify(sessionRepository, times(1)).findById(eq(sessionId));
			verify(stepRepository, never()).findById(any());
			verify(executorRepository, never()).findById(any());
			verify(sessionStepRepository, never()).saveAndFlush(any());
			verify(worklogRepository, never()).saveAndFlush(any(ChoreographerWorklog.class));
			throw ex;
		}		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testChangeSessionStepStatus_1() {
		final long sessionId = 5;
		final ChoreographerStep step = new ChoreographerStep();
		step.setName("test-step");
		step.setAction(new ChoreographerAction("test-action", null));		
		final ChoreographerSessionStepStatus newStatus = ChoreographerSessionStepStatus.DONE;
		final String message = "message";
		
		final ChoreographerSession session = new ChoreographerSession();
		session.setId(sessionId);
		
		final ChoreographerSessionStep sessionStep = new ChoreographerSessionStep();
		sessionStep.setId(54);
		sessionStep.setSession(session);
		sessionStep.setStep(step);
		session.setPlan(new ChoreographerPlan("test-plan"));
		sessionStep.setStatus(ChoreographerSessionStepStatus.RUNNING);
		
		when(sessionRepository.findById(anyLong())).thenReturn(Optional.of(session));
		when(sessionStepRepository.findBySessionAndStepAndExecutionNumber(any(), any(), anyLong())).thenReturn(Optional.of(sessionStep));
		when(sessionStepRepository.findById(anyLong())).thenReturn(Optional.of(sessionStep));
		final ArgumentCaptor<ChoreographerSessionStep> sessionStepCaptor = ArgumentCaptor.forClass(ChoreographerSessionStep.class);
		when(sessionStepRepository.saveAndFlush(sessionStepCaptor.capture())).thenReturn(sessionStep);
		
		final ChoreographerSessionStep result = dbService.changeSessionStepStatus(sessionId, step, newStatus, message);
		
		final ChoreographerSessionStep sessionStepCaptured = sessionStepCaptor.getValue();
		assertEquals(sessionStep.getId(), sessionStepCaptured.getId());
		assertEquals(newStatus, sessionStepCaptured.getStatus());
		assertEquals(message, sessionStepCaptured.getMessage());
		assertEquals(sessionStep.getId(), result.getId());
		assertEquals(newStatus, result.getStatus());
		assertEquals(message, result.getMessage());
		
		verify(sessionRepository, times(1)).findById(eq(sessionId));
		verify(sessionStepRepository, times(1)).findBySessionAndStepAndExecutionNumber(eq(session), eq(step), anyLong());
		verify(sessionStepRepository, times(1)).findById(eq(sessionStep.getId()));
		verify(sessionStepRepository, times(1)).saveAndFlush(eq(sessionStep));
		verify(worklogRepository, times(1)).saveAndFlush(any(ChoreographerWorklog.class));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testChangeSessionStepStatus_1_SessionNotExists() {
		final long sessionId = 5;
		final ChoreographerStep step = new ChoreographerStep();
		step.setName("test-step");
		step.setAction(new ChoreographerAction("test-action", null));		
		final ChoreographerSessionStepStatus newStatus = ChoreographerSessionStepStatus.DONE;
		final String message = "message";
		
		when(sessionRepository.findById(anyLong())).thenReturn(Optional.empty());
		
		try {
			dbService.changeSessionStepStatus(sessionId, step, newStatus, message);
			
		} catch (final InvalidParameterException ex) {
			verify(sessionRepository, times(1)).findById(eq(sessionId));
			verify(sessionStepRepository, never()).findBySessionAndStepAndExecutionNumber(any(), any(), anyLong());
			verify(sessionStepRepository, never()).findById(any());
			verify(sessionStepRepository, never()).saveAndFlush(any());
			verify(worklogRepository, times(1)).saveAndFlush(any(ChoreographerWorklog.class));
			throw ex;
		}				
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testChangeSessionStepStatus_1_SessionStepNotExists() {
		final long sessionId = 5;
		final ChoreographerStep step = new ChoreographerStep();
		step.setName("test-step");
		step.setAction(new ChoreographerAction("test-action", null));		
		final ChoreographerSessionStepStatus newStatus = ChoreographerSessionStepStatus.DONE;
		final String message = "message";
		
		final ChoreographerSession session = new ChoreographerSession();
		session.setId(sessionId);
		
		when(sessionRepository.findById(anyLong())).thenReturn(Optional.of(session));
		when(sessionStepRepository.findBySessionAndStepAndExecutionNumber(any(), any(), anyLong())).thenReturn(Optional.empty());
		
		try {
			dbService.changeSessionStepStatus(sessionId, step, newStatus, message);
			
		} catch (final InvalidParameterException ex) {
			verify(sessionRepository, times(1)).findById(eq(sessionId));
			verify(sessionStepRepository, times(1)).findBySessionAndStepAndExecutionNumber(eq(session), eq(step), anyLong());
			verify(sessionStepRepository, never()).findById(any());
			verify(sessionStepRepository, never()).saveAndFlush(any());
			verify(worklogRepository, times(1)).saveAndFlush(any(ChoreographerWorklog.class));
			throw ex;
		}		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testChangeSessionStepStatus_1_DatabaseException() {
		final long sessionId = 5;
		final ChoreographerStep step = new ChoreographerStep();
		step.setName("test-step");
		step.setAction(new ChoreographerAction("test-action", null));		
		final ChoreographerSessionStepStatus newStatus = ChoreographerSessionStepStatus.DONE;
		final String message = "message";
		
		when(sessionRepository.findById(anyLong())).thenThrow(new HibernateException("test"));
		
		try {
			dbService.changeSessionStepStatus(sessionId, step, newStatus, message);
			
		} catch (final ArrowheadException ex) {
			verify(sessionRepository, times(1)).findById(eq(sessionId));
			verify(sessionStepRepository, never()).findBySessionAndStepAndExecutionNumber(any(), any(), anyLong());
			verify(sessionStepRepository, never()).findById(any());
			verify(sessionStepRepository, never()).saveAndFlush(any());
			verify(worklogRepository, never()).saveAndFlush(any(ChoreographerWorklog.class));
			throw ex;
		}				
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testChangeSessionStepStatus_2() {
		final long sessionStepId = 2;
		final ChoreographerSessionStepStatus newStatus = ChoreographerSessionStepStatus.DONE;
		final String message = "message";
		
		final ChoreographerStep step = new ChoreographerStep();
		step.setName("test-step");
		step.setAction(new ChoreographerAction("test-action", null));
		final ChoreographerSession session = new ChoreographerSession();
		final ChoreographerSessionStep sessionStep = new ChoreographerSessionStep();
		sessionStep.setId(sessionStepId);
		sessionStep.setSession(session);
		sessionStep.setStep(step);
		session.setPlan(new ChoreographerPlan("test-plan"));
		sessionStep.setStatus(ChoreographerSessionStepStatus.RUNNING);
		
		when(sessionStepRepository.findById(anyLong())).thenReturn(Optional.of(sessionStep));
		final ArgumentCaptor<ChoreographerSessionStep> sessionStepCaptor = ArgumentCaptor.forClass(ChoreographerSessionStep.class);
		when(sessionStepRepository.saveAndFlush(sessionStepCaptor.capture())).thenReturn(sessionStep);
		
		final ChoreographerSessionStep result = dbService.changeSessionStepStatus(sessionStepId, newStatus, message);
		
		final ChoreographerSessionStep sessionStepCaptured = sessionStepCaptor.getValue();
		assertEquals(sessionStep.getId(), sessionStepCaptured.getId());
		assertEquals(newStatus, sessionStepCaptured.getStatus());
		assertEquals(message, sessionStepCaptured.getMessage());
		assertEquals(sessionStep.getId(), result.getId());
		assertEquals(newStatus, result.getStatus());
		assertEquals(message, result.getMessage());
		
		verify(sessionStepRepository, times(1)).findById(eq(sessionStep.getId()));
		verify(sessionStepRepository, times(1)).saveAndFlush(eq(sessionStep));
		verify(worklogRepository, times(1)).saveAndFlush(any(ChoreographerWorklog.class));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testChangeSessionStepStatus_2_SessionStep_NotExists() {
		final long sessionStepId = 2;
		final ChoreographerSessionStepStatus newStatus = ChoreographerSessionStepStatus.DONE;
		final String message = "message";
		
		when(sessionStepRepository.findById(anyLong())).thenReturn(Optional.empty());
		
		try {
			dbService.changeSessionStepStatus(sessionStepId, newStatus, message);
			
		} catch (final InvalidParameterException ex) {
			verify(sessionStepRepository, times(1)).findById(eq(sessionStepId));
			verify(sessionStepRepository, never()).saveAndFlush(any());
			verify(worklogRepository, times(1)).saveAndFlush(any(ChoreographerWorklog.class));
			throw ex;
		}		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testChangeSessionStepStatus_2_DatabaseException() {
		final long sessionStepId = 2;
		final ChoreographerSessionStepStatus newStatus = ChoreographerSessionStepStatus.DONE;
		final String message = "message";
		
		when(sessionStepRepository.findById(anyLong())).thenThrow(new HibernateException("test"));
		
		try {
			dbService.changeSessionStepStatus(sessionStepId, newStatus, message);
			
		} catch (final ArrowheadException ex) {
			verify(sessionStepRepository, times(1)).findById(eq(sessionStepId));
			verify(sessionStepRepository, never()).saveAndFlush(any());
			verify(worklogRepository, never()).saveAndFlush(any());
			throw ex;
		}		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetSessionStepBySessionIdAndStepId() {
		final long sessionId = 5;
		final long stepId = 7;
		
		final ChoreographerSession session = new ChoreographerSession();
		session.setId(sessionId);
		final ChoreographerStep step = new ChoreographerStep();
		step.setId(stepId);
		final ChoreographerSessionStep returnValue = new ChoreographerSessionStep();
		returnValue.setSession(session);
		returnValue.setStep(step);
		
		when(sessionRepository.findById(anyLong())).thenReturn(Optional.of(session));
		when(stepRepository.findById(anyLong())).thenReturn(Optional.of(step));
		when(sessionStepRepository.findBySessionAndStepAndExecutionNumber(any(), any(), anyLong())).thenReturn(Optional.of(returnValue));
		
		final ChoreographerSessionStep result = dbService.getSessionStepBySessionIdAndStepId(sessionId, stepId);
		
		assertEquals(returnValue.getSession().getId(), result.getSession().getId());
		assertEquals(returnValue.getStep().getId(), result.getStep().getId());
		
		verify(sessionRepository, times(1)).findById(eq(sessionId));
		verify(stepRepository, times(1)).findById(eq(stepId));
		verify(sessionStepRepository, times(1)).findBySessionAndStepAndExecutionNumber(eq(session), eq(step), anyLong());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGetSessionStepBySessionIdAndStepId_SessionNotExists() {
		final long sessionId = 5;
		final long stepId = 7;
		
		when(sessionRepository.findById(anyLong())).thenReturn(Optional.empty());
		
		try {
			dbService.getSessionStepBySessionIdAndStepId(sessionId, stepId);
			
		} catch (final InvalidParameterException ex) {
			verify(sessionRepository, times(1)).findById(eq(sessionId));
			verify(stepRepository, never()).findById(anyLong());
			verify(sessionStepRepository, never()).findBySessionAndStepAndExecutionNumber(any(), any(), anyLong());
			throw ex;
		}		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGetSessionStepBySessionIdAndStepId_StepNotExists() {
		final long sessionId = 5;
		final long stepId = 7;
		
		final ChoreographerSession session = new ChoreographerSession();
		session.setId(sessionId);
		
		when(sessionRepository.findById(anyLong())).thenReturn(Optional.of(session));
		when(stepRepository.findById(anyLong())).thenReturn(Optional.empty());
		
		try {
			dbService.getSessionStepBySessionIdAndStepId(sessionId, stepId);
			
		} catch (final InvalidParameterException ex) {
			verify(sessionRepository, times(1)).findById(eq(sessionId));
			verify(stepRepository, times(1)).findById(eq(stepId));
			verify(sessionStepRepository, never()).findBySessionAndStepAndExecutionNumber(any(), any(), anyLong());
			throw ex;
		}				
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGetSessionStepBySessionIdAndStepId_SessionStepNotExists() {
		final long sessionId = 5;
		final long stepId = 7;
		
		final ChoreographerSession session = new ChoreographerSession();
		session.setId(sessionId);
		final ChoreographerStep step = new ChoreographerStep();
		step.setId(stepId);
		
		when(sessionRepository.findById(anyLong())).thenReturn(Optional.of(session));
		when(stepRepository.findById(anyLong())).thenReturn(Optional.of(step));
		when(sessionStepRepository.findBySessionAndStepAndExecutionNumber(any(), any(), anyLong())).thenReturn(Optional.empty());
		
		try {
			dbService.getSessionStepBySessionIdAndStepId(sessionId, stepId);
			
		} catch (final InvalidParameterException ex) {
			verify(sessionRepository, times(1)).findById(eq(sessionId));
			verify(stepRepository, times(1)).findById(eq(stepId));
			verify(sessionStepRepository, times(1)).findBySessionAndStepAndExecutionNumber(any(), any(), anyLong());
			throw ex;
		}		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testGetSessionStepBySessionIdAndStepId_DatabaseException() {
		final long sessionId = 5;
		final long stepId = 7;
		
		when(sessionRepository.findById(anyLong())).thenThrow(new HibernateException("test"));
		
		try {
			dbService.getSessionStepBySessionIdAndStepId(sessionId, stepId);
			
		} catch (final ArrowheadException ex) {
			verify(sessionRepository, times(1)).findById(eq(sessionId));
			verify(stepRepository, never()).findById(anyLong());
			verify(sessionStepRepository, never()).findBySessionAndStepAndExecutionNumber(any(), any(), anyLong());
			throw ex;
		}		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetSessionStepBySessionIdAndSteps() {
		final long sessionId = 6;
		final ChoreographerSession session = new ChoreographerSession();
		session.setId(sessionId);
		final ChoreographerStep step = new ChoreographerStep();
		step.setId(3);
		final Set<ChoreographerStep> steps = Set.of(step);
		final ChoreographerSessionStep returnValue = new ChoreographerSessionStep();
		returnValue.setSession(session);
		returnValue.setStep(step);
		
		when(sessionRepository.findById(anyLong())).thenReturn(Optional.of(session));
		when(sessionStepRepository.findBySessionAndStepIn(any(), any())).thenReturn(List.of(returnValue));
		
		final List<ChoreographerSessionStep> result = dbService.getSessionStepBySessionIdAndSteps(sessionId, steps);
		
		assertEquals(returnValue.getSession().getId(), result.get(0).getSession().getId());
		assertEquals(returnValue.getStep().getId(), result.get(0).getStep().getId());
		
		verify(sessionRepository, times(1)).findById(eq(sessionId));
		verify(sessionStepRepository, times(1)).findBySessionAndStepIn(eq(session), eq(steps));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testGetSessionStepBySessionIdAndSteps_NullSteps() {
		try {
			dbService.getSessionStepBySessionIdAndSteps(7, null);
			
		} catch (final IllegalArgumentException ex) {
			verify(sessionRepository, never()).findById(anyLong());
			verify(sessionStepRepository, never()).findBySessionAndStepIn(any(), any());
			throw ex;
		}		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testGetSessionStepBySessionIdAndSteps_EmptySteps() {
		try {
			dbService.getSessionStepBySessionIdAndSteps(7, Set.of());
			
		} catch (final IllegalArgumentException ex) {
			verify(sessionRepository, never()).findById(anyLong());
			verify(sessionStepRepository, never()).findBySessionAndStepIn(any(), any());
			throw ex;
		}		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGetSessionStepBySessionIdAndSteps_SessionNotExists() {
		final long sessionId = 6;
		final ChoreographerStep step = new ChoreographerStep();
		step.setId(3);
		final Set<ChoreographerStep> steps = Set.of(step);

		when(sessionRepository.findById(anyLong())).thenReturn(Optional.empty());
		
		try {
			dbService.getSessionStepBySessionIdAndSteps(sessionId, steps);
			
		} catch (final InvalidParameterException ex) {
			verify(sessionRepository, times(1)).findById(eq(sessionId));
			verify(sessionStepRepository, never()).findBySessionAndStepIn(any(), any());
			throw ex;
		}		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testGetSessionStepBySessionIdAndSteps_DatabaseException() {
		final long sessionId = 6;
		final ChoreographerStep step = new ChoreographerStep();
		step.setId(3);
		final Set<ChoreographerStep> steps = Set.of(step);

		when(sessionRepository.findById(anyLong())).thenThrow(new HibernateException("test"));
		
		try {
			dbService.getSessionStepBySessionIdAndSteps(sessionId, steps);
			
		} catch (final ArrowheadException ex) {
			verify(sessionRepository, times(1)).findById(eq(sessionId));
			verify(sessionStepRepository, never()).findBySessionAndStepIn(any(), any());
			throw ex;
		}		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetSessionStepById() {
		final long sessionStepId = 5;
		
		final ChoreographerSessionStep sessionStep = new ChoreographerSessionStep();
		sessionStep.setId(sessionStepId);
		when(sessionStepRepository.findById(anyLong())).thenReturn(Optional.of(sessionStep));
		
		final ChoreographerSessionStep result = dbService.getSessionStepById(sessionStepId);
		
		assertEquals(sessionStepId, result.getId());
		verify(sessionStepRepository, times(1)).findById(eq(sessionStepId));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGetSessionStepById_SessionStepNotExists() {
		final long sessionStepId = 5;
		
		when(sessionStepRepository.findById(anyLong())).thenReturn(Optional.empty());
		
		try {
			dbService.getSessionStepById(sessionStepId);
			
		} catch (final InvalidParameterException ex) {
			verify(sessionStepRepository, times(1)).findById(eq(sessionStepId));
			throw ex;
		}		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testGetSessionStepById_DatabaseException() {
		final long sessionStepId = 5;
		
		when(sessionStepRepository.findById(anyLong())).thenThrow(new HibernateException("test"));
		
		try {
			dbService.getSessionStepById(sessionStepId);
			
		} catch (final ArrowheadException ex) {
			verify(sessionStepRepository, times(1)).findById(eq(sessionStepId));
			throw ex;
		}		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetAllSessionStepBySessionId() {
		final long sessionId = 4;
		
		final ChoreographerSession session = new ChoreographerSession();
		session.setId(sessionId);		
		final ChoreographerSessionStep sessionStep = new ChoreographerSessionStep();
		sessionStep.setId(6);
		
		when(sessionRepository.findById(anyLong())).thenReturn(Optional.of(session));
		when(sessionStepRepository.findAllBySession(any())).thenReturn(List.of(sessionStep));
		
		final List<ChoreographerSessionStep> result = dbService.getAllSessionStepBySessionId(sessionId);
		
		assertEquals(sessionStep.getId(), result.get(0).getId());
		verify(sessionRepository, times(1)).findById(eq(sessionId));
		verify(sessionStepRepository, times(1)).findAllBySession(eq(session));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGetAllSessionStepBySessionId_SessionNotExists() {
		final long sessionId = 4;
		
		when(sessionRepository.findById(anyLong())).thenReturn(Optional.empty());
		
		try {
			dbService.getAllSessionStepBySessionId(sessionId);
			
		} catch (final InvalidParameterException ex) {
			verify(sessionRepository, times(1)).findById(eq(sessionId));
			verify(sessionStepRepository, never()).findAllBySession(any());
			throw ex;
		}		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testGetAllSessionStepBySessionId_DatabaseException() {
		final long sessionId = 4;
		
		when(sessionRepository.findById(anyLong())).thenThrow(new HibernateException("test"));
		
		try {
			dbService.getAllSessionStepBySessionId(sessionId);
			
		} catch (final ArrowheadException ex) {
			verify(sessionRepository, times(1)).findById(eq(sessionId));
			verify(sessionStepRepository, never()).findAllBySession(any());
			throw ex;
		}		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetAllSessionStepBySessionIdAndActionId() {
		final long sessionId = 6;
		final long actionId = 4;
		
		final ChoreographerSession session = new ChoreographerSession();
		session.setId(sessionId);
		final ChoreographerAction action = new ChoreographerAction();
		action.setId(actionId);
		final ChoreographerStep step = new ChoreographerStep();
		step.setAction(action);
		final ChoreographerSessionStep sessionStep = new ChoreographerSessionStep();
		sessionStep.setSession(session);
		sessionStep.setStep(step);
		
		when(sessionRepository.findById(anyLong())).thenReturn(Optional.of(session));
		when(actionRepository.findById(anyLong())).thenReturn(Optional.of(action));
		when(sessionStepRepository.findAllBySessionAndStep_Action(any(), any())).thenReturn(List.of(sessionStep));
		
		final List<ChoreographerSessionStep> result = dbService.getAllSessionStepBySessionIdAndActionId(sessionId, actionId);
		
		assertEquals(sessionStep.getSession().getId(), result.get(0).getSession().getId());
		assertEquals(sessionStep.getStep().getAction().getId(), result.get(0).getStep().getAction().getId());
		
		verify(sessionRepository, times(1)).findById(eq(sessionId));
		verify(actionRepository, times(1)).findById(eq(actionId));
		verify(sessionStepRepository, times(1)).findAllBySessionAndStep_Action(eq(session),	eq(action));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGetAllSessionStepBySessionIdAndActionId_SessionNotExists() {
		final long sessionId = 6;
		final long actionId = 4;
		
		when(sessionRepository.findById(anyLong())).thenReturn(Optional.empty());
		
		try {
			dbService.getAllSessionStepBySessionIdAndActionId(sessionId, actionId);
			
		} catch (final InvalidParameterException ex) {
			verify(sessionRepository, times(1)).findById(eq(sessionId));
			verify(actionRepository, never()).findById(anyLong());
			verify(sessionStepRepository, never()).findAllBySessionAndStep_Action(any(), any());
			throw ex;
		}		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGetAllSessionStepBySessionIdAndActionId_ActionNotExists() {
		final long sessionId = 6;
		final long actionId = 4;
		
		final ChoreographerSession session = new ChoreographerSession();
		session.setId(sessionId);
		
		when(sessionRepository.findById(anyLong())).thenReturn(Optional.of(session));
		when(actionRepository.findById(anyLong())).thenReturn(Optional.empty());
		
		try {
			dbService.getAllSessionStepBySessionIdAndActionId(sessionId, actionId);
			
		} catch (final InvalidParameterException ex) {
			verify(sessionRepository, times(1)).findById(eq(sessionId));
			verify(actionRepository, times(1)).findById(eq(actionId));
			verify(sessionStepRepository, never()).findAllBySessionAndStep_Action(any(), any());
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testGetAllSessionStepBySessionIdAndActionId_DatabaseException() {
		final long sessionId = 6;
		final long actionId = 4;
		
		when(sessionRepository.findById(anyLong())).thenThrow(new HibernateException("test"));
		
		try {
			dbService.getAllSessionStepBySessionIdAndActionId(sessionId, actionId);
			
		} catch (final ArrowheadException ex) {
			verify(sessionRepository, times(1)).findById(eq(sessionId));
			verify(actionRepository, never()).findById(anyLong());
			verify(sessionStepRepository, never()).findAllBySessionAndStep_Action(any(), any());
			throw ex;
		}		
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("unchecked")
	@Test
	public void testGetSessionSteps() {
		final int page = 6;
		final int size = 50;
		final Direction direction = Direction.ASC;
		final String sortField = "test";
		final Long sessionId = 1L;
		final ChoreographerSessionStepStatus status = ChoreographerSessionStepStatus.DONE;
		
		final ChoreographerSession session = new ChoreographerSession();
		session.setId(sessionId);
		final ChoreographerSessionStep sessionStep = new ChoreographerSessionStep();
		sessionStep.setSession(session);
		sessionStep.setStatus(status);
		
		when(sessionRepository.findById(anyLong())).thenReturn(Optional.of(session));
		final ArgumentCaptor<Example<ChoreographerSessionStep>> exampleCaptor = ArgumentCaptor.forClass(Example.class);	
		final ArgumentCaptor<PageRequest> pageRequestCaptor = ArgumentCaptor.forClass(PageRequest.class);
		when(sessionStepRepository.findAll(exampleCaptor.capture(), pageRequestCaptor.capture())).thenReturn(new PageImpl<ChoreographerSessionStep>(List.of(sessionStep)));
		
		final Page<ChoreographerSessionStep> result = dbService.getSessionSteps(page, size, direction, sortField, sessionId, status);
		
		final Example<ChoreographerSessionStep> exampleCaptured = exampleCaptor.getValue();
		assertEquals(status, exampleCaptured.getProbe().getStatus());
		assertEquals(sessionId.longValue(), exampleCaptured.getProbe().getSession().getId());
		
		final PageRequest pageRequestCaptured = pageRequestCaptor.getValue();
		assertEquals(page, pageRequestCaptured.getPageNumber());
		assertEquals(size, pageRequestCaptured.getPageSize());
		assertEquals(direction, pageRequestCaptured.getSort().getOrderFor(sortField).getDirection());
		
		assertTrue(result.getContent().size() == 1);
		assertEquals(session.getId(), result.getContent().get(0).getSession().getId());
		
		verify(sessionRepository, times(1)).findById(eq(sessionId));
		verify(sessionStepRepository, times(1)).findAll(any(Example.class), any(PageRequest.class));
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("unchecked")
	@Test
	public void testGetSessionSteps_UndefinedPrameters() {
		final int page = -1;
		final int size = -1;
		final Direction direction = null;
		final String sortField = null;
		
		final ChoreographerSessionStep sessionStep = new ChoreographerSessionStep();
		sessionStep.setId(46);
		
		final ArgumentCaptor<Example<ChoreographerSessionStep>> exampleCaptor = ArgumentCaptor.forClass(Example.class);	
		final ArgumentCaptor<PageRequest> pageRequestCaptor = ArgumentCaptor.forClass(PageRequest.class);
		when(sessionStepRepository.findAll(exampleCaptor.capture(), pageRequestCaptor.capture())).thenReturn(new PageImpl<ChoreographerSessionStep>(List.of(sessionStep)));
		
		final Page<ChoreographerSessionStep> result = dbService.getSessionSteps(page, size, direction, sortField, null, null);
		
		final Example<ChoreographerSessionStep> exampleCaptured = exampleCaptor.getValue();
		assertNull(exampleCaptured.getProbe().getStatus());
		assertNull(exampleCaptured.getProbe().getSession());
		
		final PageRequest pageRequestCaptured = pageRequestCaptor.getValue();
		assertEquals(0, pageRequestCaptured.getPageNumber());
		assertEquals(Integer.MAX_VALUE, pageRequestCaptured.getPageSize());
		assertEquals(Direction.ASC, pageRequestCaptured.getSort().getOrderFor(CommonConstants.COMMON_FIELD_NAME_ID).getDirection());
		
		assertTrue(result.getContent().size() == 1);
		assertEquals(sessionStep.getId(), result.getContent().get(0).getId());
		
		verify(sessionRepository, never()).findById(anyLong());
		verify(sessionStepRepository, times(1)).findAll(any(Example.class), any(PageRequest.class));
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("unchecked")
	@Test (expected = InvalidParameterException.class)
	public void testGetSessionSteps_SessionNotExists() {
		final int page = 6;
		final int size = 50;
		final Direction direction = Direction.ASC;
		final String sortField = "test";
		final Long sessionId = 1L;
		final ChoreographerSessionStepStatus status = ChoreographerSessionStepStatus.DONE;
		
		when(sessionRepository.findById(anyLong())).thenReturn(Optional.empty());		
		
		try {
			dbService.getSessionSteps(page, size, direction, sortField, sessionId, status);
			
		} catch (final InvalidParameterException ex) {
			verify(sessionRepository, times(1)).findById(eq(sessionId));
			verify(sessionStepRepository, never()).findAll(any(Example.class), any(PageRequest.class));
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("unchecked")
	@Test (expected = ArrowheadException.class)
	public void testGetSessionSteps_DatabaseException() {
		final int page = 6;
		final int size = 50;
		final Direction direction = Direction.ASC;
		final String sortField = "test";
		final Long sessionId = 1L;
		final ChoreographerSessionStepStatus status = ChoreographerSessionStepStatus.DONE;
		
		when(sessionRepository.findById(anyLong())).thenThrow(new HibernateException("test"));		
		
		try {
			dbService.getSessionSteps(page, size, direction, sortField, sessionId, status);
			
		} catch (final ArrowheadException ex) {
			verify(sessionRepository, times(1)).findById(eq(sessionId));
			verify(sessionStepRepository, never()).findAll(any(Example.class), any(PageRequest.class));
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("unchecked")
	@Test
	public void testGetWorklogs() {
		final int page = 6;
		final int size = 50;
		final Direction direction = Direction.ASC;
		final String sortField = "test";
		final Long sessionId = 1L;
		final String planName = "test-plan";
		final String actionName = "test-action";
		final String stepName = "test-step";
		
		final ChoreographerWorklog worklog = new ChoreographerWorklog();
		worklog.setId(66);
		
		final ArgumentCaptor<Example<ChoreographerWorklog>> exampleCaptor = ArgumentCaptor.forClass(Example.class);	
		final ArgumentCaptor<PageRequest> pageRequestCaptor = ArgumentCaptor.forClass(PageRequest.class);
		when(worklogRepository.findAll(exampleCaptor.capture(), pageRequestCaptor.capture())).thenReturn(new PageImpl<ChoreographerWorklog>(List.of(worklog)));
		
		final Page<ChoreographerWorklog> result = dbService.getWorklogs(page, size, direction, sortField, sessionId, planName, actionName, stepName);
		
		final Example<ChoreographerWorklog> exampleCaptured = exampleCaptor.getValue();
		assertEquals(sessionId, exampleCaptured.getProbe().getSessionId());
		assertEquals(planName, exampleCaptured.getProbe().getPlanName());
		assertEquals(actionName, exampleCaptured.getProbe().getActionName());
		assertEquals(stepName, exampleCaptured.getProbe().getStepName());
		
		final PageRequest pageRequestCaptured = pageRequestCaptor.getValue();
		assertEquals(page, pageRequestCaptured.getPageNumber());
		assertEquals(size, pageRequestCaptured.getPageSize());
		assertEquals(direction, pageRequestCaptured.getSort().getOrderFor(sortField).getDirection());
		
		assertTrue(result.getContent().size() == 1);
		assertEquals(worklog.getId(), result.getContent().get(0).getId());
		
		verify(worklogRepository, times(1)).findAll(any(Example.class), any(PageRequest.class));
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("unchecked")
	@Test
	public void testGetWorklogs_UndefinedParameters() {
		final int page = -1;
		final int size = -1;
		final Direction direction = null;
		final String sortField = null;
		final Long sessionId = null;
		final String planName = null;
		final String actionName = null;
		final String stepName = null;
		
		final ChoreographerWorklog worklog = new ChoreographerWorklog();
		worklog.setId(66);
		
		final ArgumentCaptor<Example<ChoreographerWorklog>> exampleCaptor = ArgumentCaptor.forClass(Example.class);	
		final ArgumentCaptor<PageRequest> pageRequestCaptor = ArgumentCaptor.forClass(PageRequest.class);
		when(worklogRepository.findAll(exampleCaptor.capture(), pageRequestCaptor.capture())).thenReturn(new PageImpl<ChoreographerWorklog>(List.of(worklog)));
		
		final Page<ChoreographerWorklog> result = dbService.getWorklogs(page, size, direction, sortField, sessionId, planName, actionName, stepName);
		
		final Example<ChoreographerWorklog> exampleCaptured = exampleCaptor.getValue();
		assertNull(exampleCaptured.getProbe().getSessionId());
		assertNull(exampleCaptured.getProbe().getPlanName());
		assertNull(exampleCaptured.getProbe().getActionName());
		assertNull(exampleCaptured.getProbe().getStepName());
		
		final PageRequest pageRequestCaptured = pageRequestCaptor.getValue();
		assertEquals(0, pageRequestCaptured.getPageNumber());
		assertEquals(Integer.MAX_VALUE, pageRequestCaptured.getPageSize());
		assertEquals(Direction.ASC, pageRequestCaptured.getSort().getOrderFor(CommonConstants.COMMON_FIELD_NAME_ID).getDirection());
		
		assertTrue(result.getContent().size() == 1);
		assertEquals(worklog.getId(), result.getContent().get(0).getId());
		
		verify(worklogRepository, times(1)).findAll(any(Example.class), any(PageRequest.class));
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("unchecked")
	@Test(expected = ArrowheadException.class)
	public void testGetWorklogs_DatabaseException() {
		final int page = 6;
		final int size = 50;
		final Direction direction = Direction.ASC;
		final String sortField = "test";
		final Long sessionId = 1L;
		final String planName = "test-plan";
		final String actionName = "test-action";
		final String stepName = "test-step";
		
		when(worklogRepository.findAll(any(Example.class), any(PageRequest.class))).thenThrow(new HibernateException("test"));
		
		try {
			dbService.getWorklogs(page, size, direction, sortField, sessionId, planName, actionName, stepName);		
			
		} catch (final ArrowheadException ex) {
			verify(worklogRepository, times(1)).findAll(any(Example.class), any(PageRequest.class));
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testWorklog() {
		final String planName = "test-plan";
		final String actionName = "test-action";
		final String stepName = "test-step";
		final Long sessionId = 5L;
		final Long executionNum = 47L;
		final String message = "message";
		final String exception = "exception";
		
		final ArgumentCaptor<ChoreographerWorklog> captor = ArgumentCaptor.forClass(ChoreographerWorklog.class);
		when(worklogRepository.saveAndFlush(captor.capture())).thenReturn(new ChoreographerWorklog());
		
		dbService.worklog(planName, actionName, stepName, sessionId, executionNum, message, exception);
		
		final ChoreographerWorklog captured = captor.getValue();
		assertEquals(sessionId, captured.getSessionId());
		assertEquals(planName, captured.getPlanName());
		assertEquals(actionName, captured.getActionName());
		assertEquals(stepName, captured.getStepName());
		assertEquals(sessionId, captured.getSessionId());
		assertEquals(executionNum, captured.getExecutionNumber());
		assertEquals(exception, captured.getException());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testWorklogException() {
		final String planName = "test-plan";
		final String actionName = "test-action";
		final String stepName = "test-step";
		final Long sessionId = 5L;
		final Long executionNum = 47L;
		final String message = "message";
		final InvalidParameterException exception = new InvalidParameterException("exception");
		
		final ArgumentCaptor<ChoreographerWorklog> captor = ArgumentCaptor.forClass(ChoreographerWorklog.class);
		when(worklogRepository.saveAndFlush(captor.capture())).thenReturn(new ChoreographerWorklog());
		
		try {
			dbService.worklogException(planName, actionName, stepName, sessionId, executionNum, message, exception);
			
		} catch (final Exception ex) {
			final ChoreographerWorklog captured = captor.getValue();
			assertEquals(sessionId, captured.getSessionId());
			assertEquals(planName, captured.getPlanName());
			assertEquals(actionName, captured.getActionName());
			assertEquals(stepName, captured.getStepName());
			assertEquals(sessionId, captured.getSessionId());
			assertEquals(executionNum, captured.getExecutionNumber());
			assertTrue(captured.getException().contains(exception.getClass().getSimpleName()));
			assertTrue(captured.getException().contains(ex.getMessage()));
			assertTrue(ex instanceof InvalidParameterException);
		}	
	}
}
