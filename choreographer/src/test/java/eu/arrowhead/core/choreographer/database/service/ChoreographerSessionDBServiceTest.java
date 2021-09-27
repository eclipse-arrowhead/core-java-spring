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
import org.springframework.test.context.junit4.SpringRunner;

import eu.arrowhead.common.database.entity.ChoreographerAction;
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
		session.setStatus(ChoreographerSessionStatus.INITIATED);
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
		session.setStatus(ChoreographerSessionStatus.INITIATED);
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
}
