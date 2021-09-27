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

import org.hibernate.HibernateException;
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
import eu.arrowhead.common.dto.internal.ChoreographerSessionStatus;
import eu.arrowhead.common.dto.internal.ChoreographerSessionStepStatus;
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
	
	//=================================================================================================
    // methods

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testInitiateSession() {
		final long planId = 1;
		final String notifyUri = "/notify";
		
		final ChoreographerPlan plan = new ChoreographerPlan("test-plan");		
		when(planRepository.findById(anyLong())).thenReturn(Optional.of(plan));
		final ArgumentCaptor<ChoreographerSession> sessionCaptor = ArgumentCaptor.forClass(ChoreographerSession.class);
		final ChoreographerSession expected = new ChoreographerSession(plan, ChoreographerSessionStatus.INITIATED, notifyUri);
		expected.setId(10);
		when(sessionRepository.saveAndFlush(sessionCaptor.capture())).thenReturn(expected);
		final ArgumentCaptor<ChoreographerWorklog> worklogCaptor = ArgumentCaptor.forClass(ChoreographerWorklog.class);
		when(worklogRepository.saveAndFlush(worklogCaptor.capture())).thenReturn(new ChoreographerWorklog());
		
		final ChoreographerSession result = dbService.initiateSession(planId, notifyUri);
		
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
	@Test(expected = InvalidParameterException.class)
	public void testInitiateSession_PlanNotExists() {
		final long planId = 1;
		final String notifyUri = "/notify";
		
		when(planRepository.findById(anyLong())).thenReturn(Optional.empty());
		final ArgumentCaptor<ChoreographerWorklog> worklogCaptor = ArgumentCaptor.forClass(ChoreographerWorklog.class);
		when(worklogRepository.saveAndFlush(worklogCaptor.capture())).thenReturn(new ChoreographerWorklog());
		
		try {
			dbService.initiateSession(planId, notifyUri);			
		} catch (final InvalidParameterException ex) {
			final ChoreographerWorklog workLogCaptured = worklogCaptor.getValue();
			assertNotNull(workLogCaptured.getMessage());
			assertNotNull(workLogCaptured.getException());
			
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
		final String notifyUri = "/notify";
		
		when(planRepository.findById(anyLong())).thenThrow(new HibernateException("test"));
		
		try {
			dbService.initiateSession(planId, notifyUri);			
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
}
