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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;

import eu.arrowhead.common.dto.shared.ChoreographerActionRequestDTO;
import eu.arrowhead.common.dto.shared.ChoreographerPlanRequestDTO;
import eu.arrowhead.common.dto.shared.ChoreographerServiceQueryFormDTO;
import eu.arrowhead.common.dto.shared.ChoreographerStepRequestDTO;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.core.choreographer.graph.StepGraph;
import eu.arrowhead.core.choreographer.graph.StepGraphUtils;

@RunWith(SpringRunner.class)
public class ChoreographerPlanValidatorTest {

	//=================================================================================================
	// members
	
	@InjectMocks
	private ChoreographerPlanValidator testObject;
	
	@Mock
	private ActionCircleDetector actionCircleDetector;
	
	@Mock
	private StepGraphUtils stepGraphUtils;
	
	@Mock
	private ActionUtils actionUtils;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	// Please note, we only test this method, because all other public methods either call this (without doing anything else) or being called by this.
	@Test(expected = InvalidParameterException.class)
	public void testValidateAndNormalizePlanRequestNull() {
		try {
			testObject.validateAndNormalizePlan(null, false);
		} catch (final Exception ex) {
			Assert.assertEquals("Request is null.", ex.getMessage());
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testValidateAndNormalizePlanPlanNameNull() {
		try {
			final ChoreographerPlanRequestDTO plan = new ChoreographerPlanRequestDTO();
			
			testObject.validateAndNormalizePlan(plan, true);
		} catch (final Exception ex) {
			Assert.assertEquals("Plan name is null or blank.", ex.getMessage());
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testValidateAndNormalizePlanPlanNameEmpty() {
		try {
			final ChoreographerPlanRequestDTO plan = new ChoreographerPlanRequestDTO("", null, null);
			
			testObject.validateAndNormalizePlan(plan, true);
		} catch (final Exception ex) {
			Assert.assertEquals("Plan name is null or blank.", ex.getMessage());
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testValidateAndNormalizePlanFirstActionNameNull() {
		try {
			final ChoreographerPlanRequestDTO plan = new ChoreographerPlanRequestDTO("plan", null, null);
			
			testObject.validateAndNormalizePlan(plan, true);
		} catch (final Exception ex) {
			Assert.assertEquals("First action is not specified.", ex.getMessage());
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testValidateAndNormalizePlanFirstActionNameEmpty() {
		try {
			final ChoreographerPlanRequestDTO plan = new ChoreographerPlanRequestDTO("plan", " ", null);
			
			testObject.validateAndNormalizePlan(plan, true);
		} catch (final Exception ex) {
			Assert.assertEquals("First action is not specified.", ex.getMessage());
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testValidateAndNormalizePlanActionListNull() {
		try {
			final ChoreographerPlanRequestDTO plan = new ChoreographerPlanRequestDTO("plan", "action", null);
			
			testObject.validateAndNormalizePlan(plan, true);
		} catch (final Exception ex) {
			Assert.assertEquals("Action list is null or empty.", ex.getMessage());
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testValidateAndNormalizePlanActionListEmpty() {
		try {
			final ChoreographerPlanRequestDTO plan = new ChoreographerPlanRequestDTO("plan", "action", List.of());
			
			testObject.validateAndNormalizePlan(plan, true);
		} catch (final Exception ex) {
			Assert.assertEquals("Action list is null or empty.", ex.getMessage());
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testValidateAndNormalizePlanActionNull() {
		try {
			final List<ChoreographerActionRequestDTO> actions = new ArrayList<>(1);
			actions.add(null);
			final ChoreographerPlanRequestDTO plan = new ChoreographerPlanRequestDTO("plan", "action", actions);
			
			testObject.validateAndNormalizePlan(plan, true);
		} catch (final Exception ex) {
			Assert.assertEquals("Action is null.", ex.getMessage());
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testValidateAndNormalizePlanActionNameNull() {
		try {
			final List<ChoreographerActionRequestDTO> actions = new ArrayList<>(1);
			actions.add(new ChoreographerActionRequestDTO());
			final ChoreographerPlanRequestDTO plan = new ChoreographerPlanRequestDTO("plan", "action", actions);
			
			testObject.validateAndNormalizePlan(plan, true);
		} catch (final Exception ex) {
			Assert.assertEquals("Action name is null or blank.", ex.getMessage());
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testValidateAndNormalizePlanActionNameEmpty() {
		try {
			final List<ChoreographerActionRequestDTO> actions = new ArrayList<>(1);
			actions.add(new ChoreographerActionRequestDTO(" ", null, null, null));
			final ChoreographerPlanRequestDTO plan = new ChoreographerPlanRequestDTO("plan", "action", actions);
			
			testObject.validateAndNormalizePlan(plan, true);
		} catch (final Exception ex) {
			Assert.assertEquals("Action name is null or blank.", ex.getMessage());
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testValidateAndNormalizePlanActionSelfReference() {
		try {
			final List<ChoreographerActionRequestDTO> actions = new ArrayList<>(1);
			actions.add(new ChoreographerActionRequestDTO(" action", "ACTION ", null, null));
			final ChoreographerPlanRequestDTO plan = new ChoreographerPlanRequestDTO("plan", "action", actions);
			
			testObject.validateAndNormalizePlan(plan, true);
		} catch (final Exception ex) {
			Assert.assertEquals("Action references itself as next action.", ex.getMessage());
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testValidateAndNormalizePlanFirstStepNameListNull() {
		try {
			final List<ChoreographerActionRequestDTO> actions = new ArrayList<>(1);
			actions.add(new ChoreographerActionRequestDTO(" action", null, null, null));
			final ChoreographerPlanRequestDTO plan = new ChoreographerPlanRequestDTO("plan", "action", actions);
			
			testObject.validateAndNormalizePlan(plan, true);
		} catch (final Exception ex) {
			Assert.assertEquals("First steps list is null or empty.", ex.getMessage());
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testValidateAndNormalizePlanFirstStepNameListEmpty() {
		try {
			final List<ChoreographerActionRequestDTO> actions = new ArrayList<>(1);
			actions.add(new ChoreographerActionRequestDTO(" action", null, List.of(), null));
			final ChoreographerPlanRequestDTO plan = new ChoreographerPlanRequestDTO("plan", "action", actions);
			
			testObject.validateAndNormalizePlan(plan, true);
		} catch (final Exception ex) {
			Assert.assertEquals("First steps list is null or empty.", ex.getMessage());
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testValidateAndNormalizePlanFirstStepNameNull() {
		try {
			final List<ChoreographerActionRequestDTO> actions = new ArrayList<>(1);
			final List<String> firstStepNames = new ArrayList<>(1);
			firstStepNames.add(null);
			actions.add(new ChoreographerActionRequestDTO(" action", null, firstStepNames , null));
			final ChoreographerPlanRequestDTO plan = new ChoreographerPlanRequestDTO("plan", "action", actions);
			
			testObject.validateAndNormalizePlan(plan, true);
		} catch (final Exception ex) {
			Assert.assertEquals("First step name is null or blank.", ex.getMessage());
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testValidateAndNormalizePlanFirstStepNameEmpty() {
		try {
			final List<ChoreographerActionRequestDTO> actions = new ArrayList<>(1);
			final List<String> firstStepNames = new ArrayList<>(1);
			firstStepNames.add(" ");
			actions.add(new ChoreographerActionRequestDTO(" action", null, firstStepNames , null));
			final ChoreographerPlanRequestDTO plan = new ChoreographerPlanRequestDTO("plan", "action", actions);
			
			testObject.validateAndNormalizePlan(plan, true);
		} catch (final Exception ex) {
			Assert.assertEquals("First step name is null or blank.", ex.getMessage());
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testValidateAndNormalizePlanStepListNull() {
		try {
			final List<ChoreographerActionRequestDTO> actions = new ArrayList<>(1);
			final List<String> firstStepNames = new ArrayList<>(1);
			firstStepNames.add("step");
			actions.add(new ChoreographerActionRequestDTO(" action", null, firstStepNames , null));
			final ChoreographerPlanRequestDTO plan = new ChoreographerPlanRequestDTO("plan", "action", actions);
			
			testObject.validateAndNormalizePlan(plan, true);
		} catch (final Exception ex) {
			Assert.assertEquals("Step list is null.", ex.getMessage());
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testValidateAndNormalizePlanNotEnoughSteps() {
		try {
			final List<ChoreographerActionRequestDTO> actions = new ArrayList<>(1);
			final List<String> firstStepNames = new ArrayList<>(1);
			firstStepNames.add("step");
			actions.add(new ChoreographerActionRequestDTO(" action", null, firstStepNames , List.of()));
			final ChoreographerPlanRequestDTO plan = new ChoreographerPlanRequestDTO("plan", "action", actions);
			
			testObject.validateAndNormalizePlan(plan, true);
		} catch (final Exception ex) {
			Assert.assertEquals("The size of the step list is lesser than the size of the first step list.", ex.getMessage());
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testValidateAndNormalizePlanStepNull() {
		try {
			final List<ChoreographerActionRequestDTO> actions = new ArrayList<>(1);
			final List<String> firstStepNames = new ArrayList<>(1);
			firstStepNames.add("step");
			final List<ChoreographerStepRequestDTO> steps = new ArrayList<>(1);
			steps.add(null);
			actions.add(new ChoreographerActionRequestDTO(" action", null, firstStepNames , steps));
			final ChoreographerPlanRequestDTO plan = new ChoreographerPlanRequestDTO("plan", "action", actions);
			
			testObject.validateAndNormalizePlan(plan, true);
		} catch (final Exception ex) {
			Assert.assertEquals("Step is null.", ex.getMessage());
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testValidateAndNormalizePlanStepNameNull() {
		try {
			final List<ChoreographerActionRequestDTO> actions = new ArrayList<>(1);
			final List<String> firstStepNames = new ArrayList<>(1);
			firstStepNames.add("step");
			final List<ChoreographerStepRequestDTO> steps = new ArrayList<>(1);
			steps.add(new ChoreographerStepRequestDTO());
			actions.add(new ChoreographerActionRequestDTO(" action", null, firstStepNames , steps));
			final ChoreographerPlanRequestDTO plan = new ChoreographerPlanRequestDTO("plan", "action", actions);
			
			testObject.validateAndNormalizePlan(plan, true);
		} catch (final Exception ex) {
			Assert.assertEquals("Step name is null or blank.", ex.getMessage());
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testValidateAndNormalizePlanStepNameEmpty() {
		try {
			final List<ChoreographerActionRequestDTO> actions = new ArrayList<>(1);
			final List<String> firstStepNames = new ArrayList<>(1);
			firstStepNames.add("step");
			final ChoreographerStepRequestDTO step = new ChoreographerStepRequestDTO();
			step.setName(" ");
			final List<ChoreographerStepRequestDTO> steps = new ArrayList<>(1);
			steps.add(step);
			actions.add(new ChoreographerActionRequestDTO(" action", null, firstStepNames , steps));
			final ChoreographerPlanRequestDTO plan = new ChoreographerPlanRequestDTO("plan", "action", actions);
			
			testObject.validateAndNormalizePlan(plan, true);
		} catch (final Exception ex) {
			Assert.assertEquals("Step name is null or blank.", ex.getMessage());
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testValidateAndNormalizePlanServiceRequirementNull() {
		try {
			final List<ChoreographerActionRequestDTO> actions = new ArrayList<>(1);
			final List<String> firstStepNames = new ArrayList<>(1);
			firstStepNames.add("step");
			final ChoreographerStepRequestDTO step = new ChoreographerStepRequestDTO();
			step.setName("step");
			final List<ChoreographerStepRequestDTO> steps = new ArrayList<>(1);
			steps.add(step);
			actions.add(new ChoreographerActionRequestDTO(" action", null, firstStepNames , steps));
			final ChoreographerPlanRequestDTO plan = new ChoreographerPlanRequestDTO("plan", "action", actions);
			
			testObject.validateAndNormalizePlan(plan, true);
		} catch (final Exception ex) {
			Assert.assertEquals("Service requirement is null.", ex.getMessage());
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testValidateAndNormalizePlanServiceDefinitionNull() {
		try {
			final List<ChoreographerActionRequestDTO> actions = new ArrayList<>(1);
			final List<String> firstStepNames = new ArrayList<>(1);
			firstStepNames.add("step");
			final ChoreographerStepRequestDTO step = new ChoreographerStepRequestDTO();
			step.setName("step");
			step.setServiceRequirement(new ChoreographerServiceQueryFormDTO());
			final List<ChoreographerStepRequestDTO> steps = new ArrayList<>(1);
			steps.add(step);
			actions.add(new ChoreographerActionRequestDTO(" action", null, firstStepNames , steps));
			final ChoreographerPlanRequestDTO plan = new ChoreographerPlanRequestDTO("plan", "action", actions);
			
			testObject.validateAndNormalizePlan(plan, true);
		} catch (final Exception ex) {
			Assert.assertEquals("Service definition is null or blank.", ex.getMessage());
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testValidateAndNormalizePlanServiceDefinitionEmpty() {
		try {
			final List<ChoreographerActionRequestDTO> actions = new ArrayList<>(1);
			final List<String> firstStepNames = new ArrayList<>(1);
			firstStepNames.add("step");
			final ChoreographerServiceQueryFormDTO serviceRequirement = new ChoreographerServiceQueryFormDTO();
			serviceRequirement.setServiceDefinitionRequirement(" ");
			final ChoreographerStepRequestDTO step = new ChoreographerStepRequestDTO();
			step.setName("step");
			step.setServiceRequirement(serviceRequirement);
			final List<ChoreographerStepRequestDTO> steps = new ArrayList<>(1);
			steps.add(step);
			actions.add(new ChoreographerActionRequestDTO(" action", null, firstStepNames , steps));
			final ChoreographerPlanRequestDTO plan = new ChoreographerPlanRequestDTO("plan", "action", actions);
			
			testObject.validateAndNormalizePlan(plan, true);
		} catch (final Exception ex) {
			Assert.assertEquals("Service definition is null or blank.", ex.getMessage());
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testValidateAndNormalizePlanVersionProblem() {
		try {
			final List<ChoreographerActionRequestDTO> actions = new ArrayList<>(1);
			final List<String> firstStepNames = new ArrayList<>(1);
			firstStepNames.add("step");
			final ChoreographerServiceQueryFormDTO serviceRequirement = new ChoreographerServiceQueryFormDTO();
			serviceRequirement.setServiceDefinitionRequirement("service");
			serviceRequirement.setMinVersionRequirement(10);
			serviceRequirement.setMaxVersionRequirement(8);
			final ChoreographerStepRequestDTO step = new ChoreographerStepRequestDTO();
			step.setName("step");
			step.setServiceRequirement(serviceRequirement);
			final List<ChoreographerStepRequestDTO> steps = new ArrayList<>(1);
			steps.add(step);
			actions.add(new ChoreographerActionRequestDTO(" action", null, firstStepNames , steps));
			final ChoreographerPlanRequestDTO plan = new ChoreographerPlanRequestDTO("plan", "action", actions);
			
			testObject.validateAndNormalizePlan(plan, true);
		} catch (final Exception ex) {
			Assert.assertEquals("Minimum version cannot be greater than maximum version.", ex.getMessage());
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testValidateAndNormalizePlanFirstStepNotFound() {
		try {
			final List<ChoreographerActionRequestDTO> actions = new ArrayList<>(1);
			final List<String> firstStepNames = new ArrayList<>(1);
			firstStepNames.add("firstStep");
			final ChoreographerServiceQueryFormDTO serviceRequirement = new ChoreographerServiceQueryFormDTO();
			serviceRequirement.setServiceDefinitionRequirement("service");
			final ChoreographerStepRequestDTO step = new ChoreographerStepRequestDTO();
			step.setName("step");
			step.setServiceRequirement(serviceRequirement);
			final List<ChoreographerStepRequestDTO> steps = new ArrayList<>(1);
			steps.add(step);
			actions.add(new ChoreographerActionRequestDTO(" action", null, firstStepNames , steps));
			final ChoreographerPlanRequestDTO plan = new ChoreographerPlanRequestDTO("plan", "action", actions);
			
			testObject.validateAndNormalizePlan(plan, true);
		} catch (final Exception ex) {
			Assert.assertEquals("Specified first step firststep is not found in the steps list.", ex.getMessage());
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testValidateAndNormalizePlanStepNameDuplication() {
		try {
			final List<ChoreographerActionRequestDTO> actions = new ArrayList<>(1);
			final List<String> firstStepNames = new ArrayList<>(1);
			firstStepNames.add("step");
			final ChoreographerServiceQueryFormDTO serviceRequirement = new ChoreographerServiceQueryFormDTO();
			serviceRequirement.setServiceDefinitionRequirement("service");
			final ChoreographerStepRequestDTO step = new ChoreographerStepRequestDTO();
			step.setName("step");
			step.setServiceRequirement(serviceRequirement);
			final ChoreographerStepRequestDTO step2 = new ChoreographerStepRequestDTO();
			step2.setName("step");
			step2.setServiceRequirement(serviceRequirement);
			final List<ChoreographerStepRequestDTO> steps = new ArrayList<>(2);
			steps.add(step);
			steps.add(step2);
			actions.add(new ChoreographerActionRequestDTO(" action", null, firstStepNames , steps));
			final ChoreographerPlanRequestDTO plan = new ChoreographerPlanRequestDTO("plan", "action", actions);
			
			testObject.validateAndNormalizePlan(plan, true);
		} catch (final Exception ex) {
			Assert.assertEquals("Step name duplication found.", ex.getMessage());
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testValidateAndNormalizePlanNextStepNotFound() {
		try {
			final List<ChoreographerActionRequestDTO> actions = new ArrayList<>(1);
			final List<String> firstStepNames = new ArrayList<>(1);
			firstStepNames.add("step");
			final ChoreographerServiceQueryFormDTO serviceRequirement = new ChoreographerServiceQueryFormDTO();
			serviceRequirement.setServiceDefinitionRequirement("service");
			final ChoreographerStepRequestDTO step = new ChoreographerStepRequestDTO();
			step.setName("step");
			step.setServiceRequirement(serviceRequirement);
			step.setNextStepNames(List.of("nextStep"));
			final List<ChoreographerStepRequestDTO> steps = new ArrayList<>(1);
			steps.add(step);
			actions.add(new ChoreographerActionRequestDTO(" action", null, firstStepNames , steps));
			final ChoreographerPlanRequestDTO plan = new ChoreographerPlanRequestDTO("plan", "action", actions);
			
			testObject.validateAndNormalizePlan(plan, true);
		} catch (final Exception ex) {
			Assert.assertEquals("Step not found: nextstep", ex.getMessage());
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testValidateAndNormalizePlanUnreachableStep() {
		try {
			final List<ChoreographerActionRequestDTO> actions = new ArrayList<>(1);
			final List<String> firstStepNames = new ArrayList<>(1);
			firstStepNames.add("step");
			final ChoreographerServiceQueryFormDTO serviceRequirement = new ChoreographerServiceQueryFormDTO();
			serviceRequirement.setServiceDefinitionRequirement("service");
			final ChoreographerStepRequestDTO step = new ChoreographerStepRequestDTO();
			step.setName("step");
			step.setServiceRequirement(serviceRequirement);
			final ChoreographerStepRequestDTO step2 = new ChoreographerStepRequestDTO();
			step2.setName("step2");
			step2.setServiceRequirement(serviceRequirement);
			final List<ChoreographerStepRequestDTO> steps = new ArrayList<>(2);
			steps.add(step);
			steps.add(step2);
			actions.add(new ChoreographerActionRequestDTO(" action", null, firstStepNames , steps));
			final ChoreographerPlanRequestDTO plan = new ChoreographerPlanRequestDTO("plan", "action", actions);
			
			testObject.validateAndNormalizePlan(plan, true);
		} catch (final Exception ex) {
			Assert.assertEquals("Step is unreachable: step2", ex.getMessage());
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testValidateAndNormalizePlanFirstActionNotFound() {
		try {
			final List<ChoreographerActionRequestDTO> actions = new ArrayList<>(1);
			final List<String> firstStepNames = new ArrayList<>(1);
			firstStepNames.add("step");
			final ChoreographerServiceQueryFormDTO serviceRequirement = new ChoreographerServiceQueryFormDTO();
			serviceRequirement.setServiceDefinitionRequirement("service");
			final ChoreographerStepRequestDTO step = new ChoreographerStepRequestDTO();
			step.setName("step");
			step.setServiceRequirement(serviceRequirement);
			final List<ChoreographerStepRequestDTO> steps = new ArrayList<>(1);
			steps.add(step);
			actions.add(new ChoreographerActionRequestDTO(" action", null, firstStepNames , steps));
			final ChoreographerPlanRequestDTO plan = new ChoreographerPlanRequestDTO("plan", "firstAction", actions);
			
			testObject.validateAndNormalizePlan(plan, true);
		} catch (final Exception ex) {
			Assert.assertEquals("Specified first action is not found in the actions list.", ex.getMessage());
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testValidateAndNormalizePlanActionNameDuplication() {
		try {
			final List<ChoreographerActionRequestDTO> actions = new ArrayList<>(2);
			final List<String> firstStepNames = new ArrayList<>(1);
			firstStepNames.add("step");
			final ChoreographerServiceQueryFormDTO serviceRequirement = new ChoreographerServiceQueryFormDTO();
			serviceRequirement.setServiceDefinitionRequirement("service");
			final ChoreographerStepRequestDTO step = new ChoreographerStepRequestDTO();
			step.setName("step");
			step.setServiceRequirement(serviceRequirement);
			final List<ChoreographerStepRequestDTO> steps = new ArrayList<>(1);
			steps.add(step);
			final List<String> firstStepNames2 = new ArrayList<>(1);
			firstStepNames2.add("step2");
			final ChoreographerServiceQueryFormDTO serviceRequirement2 = new ChoreographerServiceQueryFormDTO();
			serviceRequirement2.setServiceDefinitionRequirement("service");
			final ChoreographerStepRequestDTO step2 = new ChoreographerStepRequestDTO();
			step2.setName("step2");
			step2.setServiceRequirement(serviceRequirement2);
			final List<ChoreographerStepRequestDTO> steps2 = new ArrayList<>(1);
			steps2.add(step2);
			actions.add(new ChoreographerActionRequestDTO(" action", null, firstStepNames , steps));
			actions.add(new ChoreographerActionRequestDTO("action ", null, firstStepNames2 , steps2));
			final ChoreographerPlanRequestDTO plan = new ChoreographerPlanRequestDTO("plan", "action", actions);
			
			testObject.validateAndNormalizePlan(plan, true);
		} catch (final Exception ex) {
			Assert.assertEquals("Action name duplication found.", ex.getMessage());
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testValidateAndNormalizePlanNextActionNotFound() {
		try {
			final List<ChoreographerActionRequestDTO> actions = new ArrayList<>(1);
			final List<String> firstStepNames = new ArrayList<>(1);
			firstStepNames.add("step");
			final ChoreographerServiceQueryFormDTO serviceRequirement = new ChoreographerServiceQueryFormDTO();
			serviceRequirement.setServiceDefinitionRequirement("service");
			final ChoreographerStepRequestDTO step = new ChoreographerStepRequestDTO();
			step.setName("step");
			step.setServiceRequirement(serviceRequirement);
			final List<ChoreographerStepRequestDTO> steps = new ArrayList<>(1);
			steps.add(step);
			actions.add(new ChoreographerActionRequestDTO(" action", "action2", firstStepNames , steps));
			final ChoreographerPlanRequestDTO plan = new ChoreographerPlanRequestDTO("plan", "action", actions);
			
			testObject.validateAndNormalizePlan(plan, true);
		} catch (final Exception ex) {
			Assert.assertEquals("Action not found: action2", ex.getMessage());
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testValidateAndNormalizePlanUnreachableAction() {
		try {
			final List<ChoreographerActionRequestDTO> actions = new ArrayList<>(2);
			final List<String> firstStepNames = new ArrayList<>(1);
			firstStepNames.add("step");
			final ChoreographerServiceQueryFormDTO serviceRequirement = new ChoreographerServiceQueryFormDTO();
			serviceRequirement.setServiceDefinitionRequirement("service");
			final ChoreographerStepRequestDTO step = new ChoreographerStepRequestDTO();
			step.setName("step");
			step.setServiceRequirement(serviceRequirement);
			final List<ChoreographerStepRequestDTO> steps = new ArrayList<>(1);
			steps.add(step);
			final List<String> firstStepNames2 = new ArrayList<>(1);
			firstStepNames2.add("step2");
			final ChoreographerServiceQueryFormDTO serviceRequirement2 = new ChoreographerServiceQueryFormDTO();
			serviceRequirement2.setServiceDefinitionRequirement("service");
			final ChoreographerStepRequestDTO step2 = new ChoreographerStepRequestDTO();
			step2.setName("step2");
			step2.setServiceRequirement(serviceRequirement2);
			final List<ChoreographerStepRequestDTO> steps2 = new ArrayList<>(1);
			steps2.add(step2);
			actions.add(new ChoreographerActionRequestDTO(" action", null, firstStepNames , steps));
			actions.add(new ChoreographerActionRequestDTO("action2", null, firstStepNames2 , steps2));
			final ChoreographerPlanRequestDTO plan = new ChoreographerPlanRequestDTO("plan", "action", actions);
			
			testObject.validateAndNormalizePlan(plan, true);
		} catch (final Exception ex) {
			Assert.assertEquals("Unreachable action detected: action2", ex.getMessage());
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testValidateAndNormalizePlanActionCircle() {
		when(actionCircleDetector.hasCircle(any(ChoreographerPlanRequestDTO.class))).thenReturn(true);
		
		try {
			final List<ChoreographerActionRequestDTO> actions = new ArrayList<>(2);
			final List<String> firstStepNames = new ArrayList<>(1);
			firstStepNames.add("step");
			final ChoreographerServiceQueryFormDTO serviceRequirement = new ChoreographerServiceQueryFormDTO();
			serviceRequirement.setServiceDefinitionRequirement("service");
			final ChoreographerStepRequestDTO step = new ChoreographerStepRequestDTO();
			step.setName("step");
			step.setServiceRequirement(serviceRequirement);
			final List<ChoreographerStepRequestDTO> steps = new ArrayList<>(1);
			steps.add(step);
			final List<String> firstStepNames2 = new ArrayList<>(1);
			firstStepNames2.add("step2");
			final ChoreographerServiceQueryFormDTO serviceRequirement2 = new ChoreographerServiceQueryFormDTO();
			serviceRequirement2.setServiceDefinitionRequirement("service");
			final ChoreographerStepRequestDTO step2 = new ChoreographerStepRequestDTO();
			step2.setName("step2");
			step2.setServiceRequirement(serviceRequirement2);
			final List<ChoreographerStepRequestDTO> steps2 = new ArrayList<>(1);
			steps2.add(step2);
			actions.add(new ChoreographerActionRequestDTO(" action", "action2", firstStepNames , steps));
			actions.add(new ChoreographerActionRequestDTO("action2", "action", firstStepNames2 , steps2));
			final ChoreographerPlanRequestDTO plan = new ChoreographerPlanRequestDTO("plan", "action", actions);
			
			testObject.validateAndNormalizePlan(plan, true);
		} catch (final Exception ex) {
			verify(actionCircleDetector, times(1)).hasCircle(any(ChoreographerPlanRequestDTO.class));
			Assert.assertEquals("An action references a previous action as its next action (or referencing itself).", ex.getMessage());
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testValidateAndNormalizePlanStepCircle() {
		final StepGraph graph = new StepGraph();
		
		when(actionCircleDetector.hasCircle(any(ChoreographerPlanRequestDTO.class))).thenReturn(false);
		when(actionUtils.createStepGraphFromAction(any(ChoreographerActionRequestDTO.class))).thenReturn(graph);
		when(stepGraphUtils.hasCircle(any(StepGraph.class))).thenReturn(true);
		
		try {
			final List<ChoreographerActionRequestDTO> actions = new ArrayList<>(1);
			final List<String> firstStepNames = new ArrayList<>(1);
			firstStepNames.add("step");
			final ChoreographerServiceQueryFormDTO serviceRequirement = new ChoreographerServiceQueryFormDTO();
			serviceRequirement.setServiceDefinitionRequirement("service");
			final ChoreographerStepRequestDTO step = new ChoreographerStepRequestDTO();
			step.setName("step");
			step.setServiceRequirement(serviceRequirement);
			step.setNextStepNames(List.of("step2"));
			final ChoreographerStepRequestDTO step2 = new ChoreographerStepRequestDTO();
			step2.setName("step2");
			step2.setServiceRequirement(serviceRequirement);
			step2.setNextStepNames(List.of("step"));
			final List<ChoreographerStepRequestDTO> steps = new ArrayList<>(2);
			steps.add(step);
			steps.add(step2);
			actions.add(new ChoreographerActionRequestDTO(" action", null, firstStepNames , steps));
			final ChoreographerPlanRequestDTO plan = new ChoreographerPlanRequestDTO("plan", "action", actions);
			
			testObject.validateAndNormalizePlan(plan, true);
		} catch (final Exception ex) {
			verify(actionCircleDetector, times(1)).hasCircle(any(ChoreographerPlanRequestDTO.class));
			verify(actionUtils, times(1)).createStepGraphFromAction(any(ChoreographerActionRequestDTO.class));
			verify(stepGraphUtils, times(1)).hasCircle(graph);
			verify(stepGraphUtils, never()).normalizeStepGraph(graph);
			Assert.assertEquals("Circular reference detected between the steps of action: action", ex.getMessage());
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testValidateAndNormalizePlanOk() {
		final StepGraph graph = new StepGraph();
		
		when(actionCircleDetector.hasCircle(any(ChoreographerPlanRequestDTO.class))).thenReturn(false);
		when(actionUtils.createStepGraphFromAction(any(ChoreographerActionRequestDTO.class))).thenReturn(graph);
		when(stepGraphUtils.hasCircle(any(StepGraph.class))).thenReturn(false);
		when(stepGraphUtils.normalizeStepGraph(any(StepGraph.class))).thenReturn(graph);
		when(actionUtils.transformActionWithGraph(any(StepGraph.class), any(ChoreographerActionRequestDTO.class))).thenReturn(new ChoreographerActionRequestDTO("transformed", null, null, null));
		
		final List<ChoreographerActionRequestDTO> actions = new ArrayList<>(1);
		final List<String> firstStepNames = new ArrayList<>(1);
		firstStepNames.add("step");
		final ChoreographerServiceQueryFormDTO serviceRequirement = new ChoreographerServiceQueryFormDTO();
		serviceRequirement.setServiceDefinitionRequirement("service");
		final ChoreographerStepRequestDTO step = new ChoreographerStepRequestDTO();
		step.setName("step");
		step.setServiceRequirement(serviceRequirement);
		step.setNextStepNames(List.of("step2"));
		final ChoreographerStepRequestDTO step2 = new ChoreographerStepRequestDTO();
		step2.setName("step2");
		step2.setServiceRequirement(serviceRequirement);
		step2.setNextStepNames(List.of());
		final List<ChoreographerStepRequestDTO> steps = new ArrayList<>(2);
		steps.add(step);
		steps.add(step2);
		actions.add(new ChoreographerActionRequestDTO(" action", null, firstStepNames , steps));
		final ChoreographerPlanRequestDTO plan = new ChoreographerPlanRequestDTO("plan", "action", actions);
		
		final ChoreographerPlanRequestDTO result = testObject.validateAndNormalizePlan(plan, true);
		verify(actionCircleDetector, times(1)).hasCircle(any(ChoreographerPlanRequestDTO.class));
		verify(actionUtils, times(1)).createStepGraphFromAction(any(ChoreographerActionRequestDTO.class));
		verify(stepGraphUtils, times(1)).hasCircle(graph);
		verify(stepGraphUtils, times(1)).normalizeStepGraph(graph);
		verify(actionUtils, times(1)).transformActionWithGraph(eq(graph), any(ChoreographerActionRequestDTO.class));
		Assert.assertEquals("transformed", result.getActions().get(0).getName());
	}
}