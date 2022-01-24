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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.hibernate.HibernateException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.test.context.junit4.SpringRunner;

import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.database.entity.ChoreographerAction;
import eu.arrowhead.common.database.entity.ChoreographerPlan;
import eu.arrowhead.common.database.entity.ChoreographerSession;
import eu.arrowhead.common.database.entity.ChoreographerStep;
import eu.arrowhead.common.database.entity.ChoreographerStepNextStepConnection;
import eu.arrowhead.common.database.repository.ChoreographerActionRepository;
import eu.arrowhead.common.database.repository.ChoreographerPlanRepository;
import eu.arrowhead.common.database.repository.ChoreographerSessionRepository;
import eu.arrowhead.common.database.repository.ChoreographerStepNextStepConnectionRepository;
import eu.arrowhead.common.database.repository.ChoreographerStepRepository;
import eu.arrowhead.common.dto.shared.ChoreographerActionRequestDTO;
import eu.arrowhead.common.dto.shared.ChoreographerPlanListResponseDTO;
import eu.arrowhead.common.dto.shared.ChoreographerPlanRequestDTO;
import eu.arrowhead.common.dto.shared.ChoreographerPlanResponseDTO;
import eu.arrowhead.common.dto.shared.ChoreographerServiceQueryFormDTO;
import eu.arrowhead.common.dto.shared.ChoreographerStepRequestDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryFormDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.core.choreographer.validation.ChoreographerPlanValidator;

@RunWith(SpringRunner.class)
public class ChoreographerPlanDBServiceTest {
	
	//=================================================================================================
	// members

    @InjectMocks
    private ChoreographerPlanDBService testObject;

	@Mock
	private ChoreographerPlanRepository choreographerPlanRepository;

	@Mock
	private ChoreographerActionRepository choreographerActionRepository;

	@Mock
	private ChoreographerStepRepository choreographerStepRepository;

	@Mock
	private ChoreographerStepNextStepConnectionRepository choreographerStepNextStepConnectionRepository;

	@Mock
	private ChoreographerSessionRepository choreographerSessionRepository;

	@Mock
	private ChoreographerPlanValidator planValidator;
    
    //=================================================================================================
	// methods

    //-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGetPlanEntriesWrongSortField() {
		try {
			testObject.getPlanEntries(0, 10, Direction.ASC, "firstAction");
		} catch (final Exception ex) {
			Assert.assertTrue(ex.getMessage().startsWith("Sortable field with reference "));
			
			verify(choreographerPlanRepository, never()).findAll(any(Pageable.class));
			
			throw ex;
		}
	}
	
    //-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testGetPlanEntriesDBException() {
		when(choreographerPlanRepository.findAll(any(Pageable.class))).thenThrow(new RuntimeException("db error"));
		
		try {
			testObject.getPlanEntries(0, 10, Direction.ASC, "id");
		} catch (final Exception ex) {
			Assert.assertEquals("Database operation exception", ex.getMessage());
			
			verify(choreographerPlanRepository, times(1)).findAll(any(Pageable.class));
			
			throw ex;
		}
	}
	
    //-------------------------------------------------------------------------------------------------
	@Test
	public void testGetPlanEntriesOk() {
		when(choreographerPlanRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));
		
		final Page<ChoreographerPlan> page = testObject.getPlanEntries(0, 10, Direction.ASC, "id");

		Assert.assertEquals(0, page.getContent().size());
		verify(choreographerPlanRepository, times(1)).findAll(any(Pageable.class));
	}
	
    //-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testGetPlanEntriesResponseDBException1() {
		final ChoreographerPlan plan = new ChoreographerPlan();
		
		when(choreographerPlanRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(plan)));
		when(choreographerActionRepository.findByPlan(plan)).thenThrow(new RuntimeException("db error"));
		
		try {
			testObject.getPlanEntriesResponse(0, 10, Direction.ASC, "id");
		} catch (final Exception ex) {
			Assert.assertEquals("Database operation exception", ex.getMessage());
			
			verify(choreographerPlanRepository, times(1)).findAll(any(Pageable.class));
			verify(choreographerActionRepository, times(1)).findByPlan(plan);
			verify(choreographerStepRepository, never()).findByAction(any(ChoreographerAction.class));
			
			throw ex;
		}
	}
	
    //-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testGetPlanEntriesResponseDBException2() {
		final ChoreographerPlan plan = new ChoreographerPlan();
		final ChoreographerAction action = new ChoreographerAction();
		
		when(choreographerPlanRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(plan)));
		when(choreographerActionRepository.findByPlan(plan)).thenReturn(List.of(action));
		when(choreographerStepRepository.findByAction(action)).thenThrow(new RuntimeException("db error"));
		
		try {
			testObject.getPlanEntriesResponse(0, 10, Direction.ASC, "id");
		} catch (final Exception ex) {
			Assert.assertEquals("Database operation exception", ex.getMessage());
			
			verify(choreographerPlanRepository, times(1)).findAll(any(Pageable.class));
			verify(choreographerActionRepository, times(1)).findByPlan(plan);
			verify(choreographerStepRepository, times(1)).findByAction(action);
			
			throw ex;
		}
	}
	
    //-------------------------------------------------------------------------------------------------
	@Test
	public void testGetPlanEntriesResponseOk() {
		final ChoreographerPlan plan = new ChoreographerPlan();
		plan.setId(1);
		plan.setName("plan");
		plan.setCreatedAt(ZonedDateTime.now());
		plan.setUpdatedAt(ZonedDateTime.now());
		final ChoreographerAction action = new ChoreographerAction();
		action.setId(2);
		action.setName("action");
		action.setPlan(plan);
		action.setFirstAction(true);
		action.setCreatedAt(ZonedDateTime.now());
		action.setUpdatedAt(ZonedDateTime.now());
		final ChoreographerStep step = new ChoreographerStep();
		step.setId(11);
		step.setName("step");
		step.setFirstStep(true);
		step.setAction(action);
		step.setServiceDefinition("service");
		step.setMinVersion(1);
		step.setMaxVersion(1);
		step.setCreatedAt(ZonedDateTime.now());
		step.setUpdatedAt(ZonedDateTime.now());
		plan.setFirstAction(action);
		
		when(choreographerPlanRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(plan)));
		when(choreographerActionRepository.findByPlan(plan)).thenReturn(List.of(action));
		when(choreographerStepRepository.findByAction(action)).thenReturn(List.of(step));

		final ChoreographerPlanListResponseDTO result = testObject.getPlanEntriesResponse(0, 10, Direction.ASC, "id");
		
		Assert.assertEquals(1, result.getCount());
		Assert.assertEquals("plan", result.getData().get(0).getName());
		Assert.assertEquals("action", result.getData().get(0).getActions().get(0).getName());
		Assert.assertEquals("step", result.getData().get(0).getActions().get(0).getSteps().get(0).getName());

		verify(choreographerPlanRepository, times(1)).findAll(any(Pageable.class));
		verify(choreographerActionRepository, times(1)).findByPlan(plan);
		verify(choreographerStepRepository, times(1)).findByAction(action);
	}
	
    //-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGetPlanByIdNotFound() {
		when(choreographerPlanRepository.findById(1L)).thenReturn(Optional.empty());
		
		try {
			testObject.getPlanById(1);
		} catch (final Exception ex) {
			Assert.assertTrue(ex.getMessage().startsWith("Choreographer Plan with id of "));
			
			verify(choreographerPlanRepository, times(1)).findById(1L);
			
			throw ex;
		}
	}
	
    //-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testGetPlanByIdDBException() {
		when(choreographerPlanRepository.findById(1L)).thenThrow(new RuntimeException("db error"));
		
		try {
			testObject.getPlanById(1);
		} catch (final Exception ex) {
			Assert.assertEquals("Database operation exception", ex.getMessage());
			
			verify(choreographerPlanRepository, times(1)).findById(1L);
			
			throw ex;
		}
	}
	
    //-------------------------------------------------------------------------------------------------
	@Test
	public void testGetPlanByIdOk() {
		when(choreographerPlanRepository.findById(1L)).thenReturn(Optional.of(new ChoreographerPlan("plan")));
		
		final ChoreographerPlan plan = testObject.getPlanById(1);
		
		Assert.assertEquals("plan", plan.getName());
			
		verify(choreographerPlanRepository, times(1)).findById(1L);
	}
	
    //-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testGetPlanByIdResponseDBException1() {
		final ChoreographerPlan plan = new ChoreographerPlan();
		
		when(choreographerPlanRepository.findById(1L)).thenReturn(Optional.of(plan));
		when(choreographerActionRepository.findByPlan(plan)).thenThrow(new RuntimeException("db error"));
		
		try {
			testObject.getPlanByIdResponse(1L);
		} catch (final Exception ex) {
			Assert.assertEquals("Database operation exception", ex.getMessage());
			
			verify(choreographerPlanRepository, times(1)).findById(1L);
			verify(choreographerActionRepository, times(1)).findByPlan(plan);
			verify(choreographerStepRepository, never()).findByAction(any(ChoreographerAction.class));
			
			throw ex;
		}
	}
	
    //-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testGetPlanByIdResponseDBException2() {
		final ChoreographerPlan plan = new ChoreographerPlan();
		final ChoreographerAction action = new ChoreographerAction();
		
		when(choreographerPlanRepository.findById(1L)).thenReturn(Optional.of(plan));
		when(choreographerActionRepository.findByPlan(plan)).thenReturn(List.of(action));
		when(choreographerStepRepository.findByAction(action)).thenThrow(new RuntimeException("db error"));
		
		try {
			testObject.getPlanByIdResponse(1L);
		} catch (final Exception ex) {
			Assert.assertEquals("Database operation exception", ex.getMessage());
			
			verify(choreographerPlanRepository, times(1)).findById(1L);
			verify(choreographerActionRepository, times(1)).findByPlan(plan);
			verify(choreographerStepRepository, times(1)).findByAction(action);
			
			throw ex;
		}
	}
	
    //-------------------------------------------------------------------------------------------------
	@Test
	public void testGetPlanByIdResponseOk() {
		final ChoreographerPlan plan = new ChoreographerPlan();
		plan.setId(1);
		plan.setName("plan");
		plan.setCreatedAt(ZonedDateTime.now());
		plan.setUpdatedAt(ZonedDateTime.now());
		final ChoreographerAction action = new ChoreographerAction();
		action.setId(2);
		action.setName("action");
		action.setPlan(plan);
		action.setFirstAction(true);
		action.setCreatedAt(ZonedDateTime.now());
		action.setUpdatedAt(ZonedDateTime.now());
		final ChoreographerStep step = new ChoreographerStep();
		step.setId(11);
		step.setName("step");
		step.setFirstStep(true);
		step.setAction(action);
		step.setServiceDefinition("service");
		step.setMinVersion(1);
		step.setMaxVersion(1);
		step.setCreatedAt(ZonedDateTime.now());
		step.setUpdatedAt(ZonedDateTime.now());
		plan.setFirstAction(action);
		
		when(choreographerPlanRepository.findById(1L)).thenReturn(Optional.of(plan));
		when(choreographerActionRepository.findByPlan(plan)).thenReturn(List.of(action));
		when(choreographerStepRepository.findByAction(action)).thenReturn(List.of(step));

		final ChoreographerPlanResponseDTO planDTO = testObject.getPlanByIdResponse(1L);
		
		Assert.assertEquals("plan", planDTO.getName());
		Assert.assertEquals("action", planDTO.getFirstActionName());
		Assert.assertEquals("step", planDTO.getActions().get(0).getSteps().get(0).getName());

		verify(choreographerPlanRepository, times(1)).findById(1L);
		verify(choreographerActionRepository, times(1)).findByPlan(plan);
		verify(choreographerStepRepository, times(1)).findByAction(action);
	}
	
    //-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testRemovePlanByIdNotFound() {
		when(choreographerPlanRepository.findById(1L)).thenReturn(Optional.empty());
		
		try {
			testObject.removePlanEntryById(1);
		} catch (final Exception ex) {
			Assert.assertTrue(ex.getMessage().startsWith("Choreographer Plan with id of "));
			
			verify(choreographerPlanRepository, times(1)).findById(1L);
			
			throw ex;
		}
	}
	
    //-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testRemovePlanByIdPlanIsCurrentlyExecuted() {
		final ChoreographerPlan plan = new ChoreographerPlan();
		
		when(choreographerPlanRepository.findById(1L)).thenReturn(Optional.of(plan));
		when(choreographerSessionRepository.findByPlanAndStatusIn(eq(plan), anyList())).thenReturn(List.of(new ChoreographerSession()));
		
		try {
			testObject.removePlanEntryById(1);
		} catch (final Exception ex) {
			Assert.assertEquals("Choreographer Plan cannot be deleted, because it is currently executed.", ex.getMessage());
			
			verify(choreographerPlanRepository, times(1)).findById(1L);
			verify(choreographerSessionRepository, times(1)).findByPlanAndStatusIn(eq(plan), anyList());
			
			throw ex;
		}
	}
	
    //-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testRemovePlanByIdDBException() {
		final ChoreographerPlan plan = new ChoreographerPlan();
		
		when(choreographerPlanRepository.findById(1L)).thenReturn(Optional.of(plan));
		when(choreographerSessionRepository.findByPlanAndStatusIn(eq(plan), anyList())).thenReturn(List.of());
		doThrow(new RuntimeException("db error")).when(choreographerPlanRepository).deleteById(1L);
		
		try {
			testObject.removePlanEntryById(1);
		} catch (final Exception ex) {
			Assert.assertEquals("Database operation exception", ex.getMessage());
			
			verify(choreographerPlanRepository, times(1)).findById(1L);
			verify(choreographerSessionRepository, times(1)).findByPlanAndStatusIn(eq(plan), anyList());
			verify(choreographerPlanRepository, times(1)).deleteById(1L);
			verify(choreographerPlanRepository, never()).flush();
			
			throw ex;
		}
	}
	
    //-------------------------------------------------------------------------------------------------
	@Test
	public void testRemovePlanByIdOk() {
		final ChoreographerPlan plan = new ChoreographerPlan();
		
		when(choreographerPlanRepository.findById(1L)).thenReturn(Optional.of(plan));
		when(choreographerSessionRepository.findByPlanAndStatusIn(eq(plan), anyList())).thenReturn(List.of());
		doNothing().when(choreographerPlanRepository).deleteById(1L);
		doNothing().when(choreographerPlanRepository).flush();
		
		testObject.removePlanEntryById(1);
		
		verify(choreographerPlanRepository, times(1)).findById(1L);
		verify(choreographerSessionRepository, times(1)).findByPlanAndStatusIn(eq(plan), anyList());
		verify(choreographerPlanRepository, times(1)).deleteById(1L);
		verify(choreographerPlanRepository, times(1)).flush();
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCreatePlanRequestNull() {
		try {
			testObject.createPlan(null);
		} catch (final Exception ex) {
			Assert.assertEquals("Request is null!", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCreatePlanNameNull() {
		try {
			testObject.createPlan(new ChoreographerPlanRequestDTO());
		} catch (final Exception ex) {
			Assert.assertEquals("Plan name is null or blank!", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCreatePlanNameEmpty() {
		final ChoreographerPlanRequestDTO request = new ChoreographerPlanRequestDTO();
		request.setName(" ");
		
		try {
			testObject.createPlan(request);
		} catch (final Exception ex) {
			Assert.assertEquals("Plan name is null or blank!", ex.getMessage());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testCreatePlanDBException() {
		final ChoreographerPlanRequestDTO request = new ChoreographerPlanRequestDTO();
		request.setName("plan");
		
		when(choreographerPlanRepository.findByName("plan")).thenThrow(new RuntimeException("db error"));
		
		try {
			testObject.createPlan(request);
		} catch (final Exception ex) {
			Assert.assertEquals("Database operation exception", ex.getMessage());
			
			verify(choreographerPlanRepository, times(1)).findByName("plan");
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCreatePlanNameIsTaken() {
		final ChoreographerPlanRequestDTO request = new ChoreographerPlanRequestDTO();
		request.setName("plan");
		
		when(choreographerPlanRepository.findByName("plan")).thenReturn(Optional.of(new ChoreographerPlan()));
		
		try {
			testObject.createPlan(request);
		} catch (final Exception ex) {
			Assert.assertEquals("Plan with specified name already exists.", ex.getMessage());
			
			verify(choreographerPlanRepository, times(1)).findByName("plan");
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCreatePlanValidationFailed() {
		final ChoreographerPlanRequestDTO request = new ChoreographerPlanRequestDTO();
		request.setName("plan");
		
		when(choreographerPlanRepository.findByName("plan")).thenReturn(Optional.empty());
		when(planValidator.validateAndNormalizePlan(request)).thenThrow(new InvalidParameterException("First action is not specified."));
		
		try {
			testObject.createPlan(request);
		} catch (final Exception ex) {
			Assert.assertEquals("First action is not specified.", ex.getMessage());
			
			verify(choreographerPlanRepository, times(1)).findByName("plan");
			verify(planValidator, times(1)).validateAndNormalizePlan(request);
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCreatePlanOk() {
		final ChoreographerPlanRequestDTO request = getPlanRequest();
		
		final ChoreographerPlan plan = new ChoreographerPlan("plan");
		final ChoreographerAction action1 = new ChoreographerAction();
		action1.setPlan(plan);
		action1.setName("action1");
		final ChoreographerAction action2 = new ChoreographerAction();
		action2.setPlan(plan);
		action2.setName("action2");
		final ChoreographerStep step1 = new ChoreographerStep("step1",
															  action1,
															  "service1",
															  1,
															  2,
															  "{}", // omit for simplicity
															  "key=value",
															  2);
		final ChoreographerStep step2 = new ChoreographerStep("step2",
															  action1,
															  "service2",
															  3,
															  3,
															  "{}", // omit for simplicity
															  "key2=value2",
															  1);
		final ChoreographerStep step3 = new ChoreographerStep("step3",
															  action2,
															  "service3",
															  1,
															  10,
															  "{}", // omit for simplicity
															  "key3=value3",
															  1);
		final ChoreographerStepNextStepConnection stepConnection = new ChoreographerStepNextStepConnection(step1, step2);
		
		when(choreographerPlanRepository.findByName("plan")).thenReturn(Optional.empty());
		when(planValidator.validateAndNormalizePlan(request)).thenReturn(request);
		when(choreographerPlanRepository.save(any(ChoreographerPlan.class))).thenReturn(plan);
		when(choreographerActionRepository.save(any(ChoreographerAction.class))).thenReturn(action1, action2);
		when(choreographerStepRepository.saveAndFlush(any(ChoreographerStep.class))).thenReturn(step1, step2, step1, step1, step3, step3);
		when(choreographerStepRepository.findByNameAndAction("step1", action1)).thenReturn(Optional.of(step1));
		when(choreographerStepRepository.findByNameAndAction("step2", action1)).thenReturn(Optional.of(step2));
		when(choreographerStepNextStepConnectionRepository.saveAll(anyList())).thenReturn(List.of(stepConnection));
		doNothing().when(choreographerStepNextStepConnectionRepository).flush();
		when(choreographerStepRepository.saveAll(anyList())).thenReturn(List.of(step2));
		when(choreographerActionRepository.saveAndFlush(any(ChoreographerAction.class))).thenReturn(action1, action2, action1, action1);
		when(choreographerStepRepository.findByNameAndAction("step3", action2)).thenReturn(Optional.of(step3));
		when(choreographerActionRepository.findByNameAndPlan("action1", plan)).thenReturn(Optional.of(action1));
		when(choreographerActionRepository.findByNameAndPlan("action2", plan)).thenReturn(Optional.of(action2));
		when(choreographerPlanRepository.saveAndFlush(plan)).thenReturn(plan);
		
		final ChoreographerPlan savedPlan = testObject.createPlan(request);
		
		Assert.assertEquals("plan", savedPlan.getName());
		Assert.assertEquals("action1", savedPlan.getFirstAction().getName());
		Assert.assertEquals("action2", savedPlan.getFirstAction().getNextAction().getName());
		Assert.assertNull(savedPlan.getFirstAction().getNextAction().getNextAction());
			
		verify(choreographerPlanRepository, times(1)).findByName("plan");
		verify(planValidator, times(1)).validateAndNormalizePlan(request);
		verify(choreographerPlanRepository, times(1)).save(any(ChoreographerPlan.class));
		verify(choreographerActionRepository, times(2)).save(any(ChoreographerAction.class));
		verify(choreographerStepRepository, times(6)).saveAndFlush(any(ChoreographerStep.class));
		verify(choreographerStepRepository, times(2)).findByNameAndAction("step1", action1);
		verify(choreographerStepRepository, times(1)).findByNameAndAction("step2", action1);
		verify(choreographerStepNextStepConnectionRepository, times(1)).saveAll(anyList());
		verify(choreographerStepNextStepConnectionRepository, times(1)).flush();
		verify(choreographerStepRepository, times(1)).saveAll(anyList());
		verify(choreographerActionRepository, times(4)).saveAndFlush(any(ChoreographerAction.class));
		verify(choreographerStepRepository, times(1)).findByNameAndAction("step3", action2);
		verify(choreographerActionRepository, times(2)).findByNameAndPlan("action1", plan);
		verify(choreographerActionRepository, times(1)).findByNameAndPlan("action2", plan);
		verify(choreographerPlanRepository, times(1)).saveAndFlush(plan);
	}

	// testCreatePlanResponse is basically just createPlan() and getPlanDetails() method callings, both are tested previously
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGetStepByIdNotFound() {
		when(choreographerStepRepository.findById(1L)).thenReturn(Optional.empty());
		
		try {
			testObject.getStepById(1);
		} catch (final Exception ex) {
			Assert.assertTrue(ex.getMessage().startsWith("Step with id of "));
			
			verify(choreographerStepRepository, times(1)).findById(1L);
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testGetStepByIdDBException() {
		when(choreographerStepRepository.findById(1L)).thenThrow(new RuntimeException("db error"));
		
		try {
			testObject.getStepById(1);
		} catch (final Exception ex) {
			Assert.assertEquals("Database operation exception", ex.getMessage());
			
			verify(choreographerStepRepository, times(1)).findById(1L);
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetStepByIdOk() {
		final ChoreographerStep step = new ChoreographerStep();
		step.setName("step");
		
		when(choreographerStepRepository.findById(1L)).thenReturn(Optional.of(step));
		
		final ChoreographerStep result = testObject.getStepById(1);

		Assert.assertEquals("step", result.getName());
			
		verify(choreographerStepRepository, times(1)).findById(1L);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCollectStepsFromPlanNoPlan() {
		final long planId = 5l;
		
		when(choreographerPlanRepository.findById(eq(planId))).thenReturn(Optional.empty());
		
		try {
			testObject.collectStepsFromPlan(planId);
			
		} catch (final InvalidParameterException ex) {			
			verify(choreographerPlanRepository, times(1)).findById(eq(planId));
			verify(choreographerActionRepository, never()).findByPlan(any());
			verify(choreographerStepRepository, never()).findByActionIn(anyList());
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCollectStepsFromPlanNoActions() {
		final long planId = 5l;
		final ChoreographerPlan plan = new ChoreographerPlan();
		plan.setId(planId);
		
		when(choreographerPlanRepository.findById(eq(planId))).thenReturn(Optional.of(plan));
		when(choreographerActionRepository.findByPlan(eq(plan))).thenReturn(List.of());
		
		final List<ChoreographerStep> result = testObject.collectStepsFromPlan(planId);

		Assert.assertEquals(0, result.size());
			
		verify(choreographerPlanRepository, times(1)).findById(eq(planId));
		verify(choreographerActionRepository, times(1)).findByPlan(eq(plan));
		verify(choreographerStepRepository, never()).findByActionIn(anyList());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCollectStepsFromPlanOk() {
		final long planId = 5l;
		final ChoreographerPlan plan = new ChoreographerPlan();
		plan.setId(planId);
		final ChoreographerAction action = new ChoreographerAction();
		final List<ChoreographerAction> actionList = List.of(action);

		when(choreographerPlanRepository.findById(eq(planId))).thenReturn(Optional.of(plan));
		when(choreographerActionRepository.findByPlan(eq(plan))).thenReturn(actionList);
		when(choreographerStepRepository.findByActionIn(actionList)).thenReturn(List.of(new ChoreographerStep()));
		
		final List<ChoreographerStep> result = testObject.collectStepsFromPlan(planId);

		Assert.assertEquals(1, result.size());
			
		verify(choreographerPlanRepository, times(1)).findById(eq(planId));
		verify(choreographerActionRepository, times(1)).findByPlan(eq(plan));
		verify(choreographerStepRepository, times(1)).findByActionIn(eq(actionList));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testGetFirstStepsNoAction() {
		when(choreographerActionRepository.findById(anyLong())).thenReturn(Optional.empty());
		
		try {
			testObject.getFirstSteps(1);
		} catch (final InvalidParameterException ex) {
			Assert.assertEquals("Action with id of 1 doesn't exists", ex.getMessage());
			
			verify(choreographerActionRepository, times(1)).findById(eq(1L));
			verify(choreographerStepRepository, never()).findByActionAndFirstStep(any(), anyBoolean());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testGetFirstStepsDBException() {
		doThrow(new HibernateException("test")).when(choreographerActionRepository).findById(anyLong());
		
		try {
			testObject.getFirstSteps(1);
		} catch (final Exception ex) {
			Assert.assertEquals(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG, ex.getMessage());
			
			verify(choreographerActionRepository, times(1)).findById(eq(1L));
			verify(choreographerStepRepository, never()).findByActionAndFirstStep(any(), anyBoolean());
			
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetFirstStepsOk() {
		final long actionId = 5l;
		final ChoreographerAction action = new ChoreographerAction();
		action.setId(actionId);

		when(choreographerActionRepository.findById(eq(actionId))).thenReturn(Optional.of(action));
		when(choreographerStepRepository.findByActionAndFirstStep(eq(action), eq(true))).thenReturn(List.of(new ChoreographerStep()));
		
		final List<ChoreographerStep> result = testObject.getFirstSteps(actionId);

		Assert.assertEquals(1, result.size());
			
		verify(choreographerActionRepository, times(1)).findById(eq(actionId));
		verify(choreographerStepRepository, times(1)).findByActionAndFirstStep(eq(action), eq(true));
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private ChoreographerPlanRequestDTO getPlanRequest() {
		final ChoreographerStepRequestDTO step1 = new ChoreographerStepRequestDTO();
		step1.setName("step1");
		step1.setQuantity(2);
		step1.setStaticParameters(Map.of("key", "value"));
		step1.setNextStepNames(List.of("step2"));
		step1.setServiceRequirement(new ChoreographerServiceQueryFormDTO(new ServiceQueryFormDTO.Builder("service1").version(1, 2).build(), false));
		
		final ChoreographerStepRequestDTO step2 = new ChoreographerStepRequestDTO();
		step2.setName("step2");
		step2.setQuantity(1);
		step2.setStaticParameters(Map.of("key2", "value2"));
		step2.setNextStepNames(List.of());
		step2.setServiceRequirement(new ChoreographerServiceQueryFormDTO(new ServiceQueryFormDTO.Builder("service2").version(3).build(), false));
		
		final ChoreographerStepRequestDTO step3 = new ChoreographerStepRequestDTO();
		step3.setName("step3");
		step3.setQuantity(1);
		step3.setStaticParameters(Map.of("key3", "value3"));
		step3.setNextStepNames(List.of());
		step3.setServiceRequirement(new ChoreographerServiceQueryFormDTO(new ServiceQueryFormDTO.Builder("service3").version(1, 10).build(), false));

		final ChoreographerActionRequestDTO action1 = new ChoreographerActionRequestDTO();
		action1.setName("action1");
		action1.setNextActionName("action2");
		action1.setFirstStepNames(List.of("step1"));
		action1.setSteps(List.of(step1, step2));
		
		final ChoreographerActionRequestDTO action2 = new ChoreographerActionRequestDTO();
		action2.setName("action2");
		action2.setFirstStepNames(List.of("step3"));
		action2.setSteps(List.of(step3));
		
		final ChoreographerPlanRequestDTO request = new ChoreographerPlanRequestDTO();
		request.setName("plan");
		request.setFirstActionName("action1");
		request.setActions(List.of(action1, action2));

		return request;
	}
}