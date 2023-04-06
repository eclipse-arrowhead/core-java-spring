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

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import eu.arrowhead.common.dto.shared.ChoreographerActionRequestDTO;
import eu.arrowhead.common.dto.shared.ChoreographerStepRequestDTO;
import eu.arrowhead.core.choreographer.graph.Node;
import eu.arrowhead.core.choreographer.graph.StepGraph;

@RunWith(SpringRunner.class)
public class ActionUtilsTest {

	//=================================================================================================
	// members
	
	private ActionUtils util = new ActionUtils();
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCreateStepGraphFromActionNullInput() {
		final StepGraph result = util.createStepGraphFromAction(null);
		Assert.assertNull(result);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCreateStepGraphFromAction() {
		final StepGraph result = util.createStepGraphFromAction(createAction());
		final StepGraph expected = createDiamondGraph();
		
		Assert.assertEquals(expected, result);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testTransformActionWithGraphNullAction() {
		final ChoreographerActionRequestDTO resultAction = util.transformActionWithGraph(new StepGraph(), null);
		Assert.assertNull(resultAction);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testTransformActionWithGraphNullGraph() {
		try {
			util.transformActionWithGraph(null, new ChoreographerActionRequestDTO());
		} catch (final Exception ex) {
			Assert.assertEquals("Graph is null.", ex.getMessage());
			throw ex;
		}
	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testTransformActionWithGraphNullActionSteps() {
		try {
			util.transformActionWithGraph(new StepGraph(), new ChoreographerActionRequestDTO());
		} catch (final Exception ex) {
			Assert.assertEquals("Action has no steps.", ex.getMessage());
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testTransformActionWithGraphIncompatibleGraphAndAction() {
		try {
			final StepGraph graph = new StepGraph();
			graph.getSteps().add(new Node("node"));
			final ChoreographerActionRequestDTO action = new ChoreographerActionRequestDTO();
			action.setSteps(List.of());
			util.transformActionWithGraph(graph, action);
		} catch (final Exception ex) {
			Assert.assertEquals("Graph is incompatible with action.", ex.getMessage());
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testTransformActionWithGraphIncompatibleGraphAndAction2() {
		try {
			final StepGraph graph = new StepGraph();
			graph.getSteps().add(new Node("node"));
			final ChoreographerStepRequestDTO step = new ChoreographerStepRequestDTO();
			step.setName("step");
			final ChoreographerActionRequestDTO action = new ChoreographerActionRequestDTO();
			action.setSteps(List.of(step));
			util.transformActionWithGraph(graph, action);
		} catch (final Exception ex) {
			Assert.assertEquals("Graph is incompatible with action.", ex.getMessage());
			throw ex;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testTransformActionWithGraphOk() {
		final StepGraph graph = createDiamondGraph();
		final ChoreographerActionRequestDTO expected = createAction();
		final ChoreographerActionRequestDTO action = createAction();
		resetAction(action);
		
		final ChoreographerActionRequestDTO result = util.transformActionWithGraph(graph, action);
		
		Assert.assertEquals(expected.getFirstStepNames(), result.getFirstStepNames());
		for (final ChoreographerStepRequestDTO step : expected.getSteps()) {
			final ChoreographerStepRequestDTO resultStep = findStep(result.getSteps(), step.getName());
			Assert.assertNotNull(resultStep);
			if (step.getNextStepNames() == null) {
				Assert.assertNull(resultStep.getNextStepNames());
			} else {
				Assert.assertTrue(step.getNextStepNames().containsAll(resultStep.getNextStepNames()));
				Assert.assertTrue(resultStep.getNextStepNames().containsAll(step.getNextStepNames()));
			}
		}
	}

	//=================================================================================================
	// assistant method
	
	//-------------------------------------------------------------------------------------------------
	private ChoreographerActionRequestDTO createAction() {
		final ChoreographerStepRequestDTO stepA = new ChoreographerStepRequestDTO();
		stepA.setName("stepA");
		stepA.setNextStepNames(List.of("stepB", "stepC"));
		
		final ChoreographerStepRequestDTO stepB = new ChoreographerStepRequestDTO();
		stepB.setName("stepB");
		stepB.setNextStepNames(List.of("stepD"));

		final ChoreographerStepRequestDTO stepC = new ChoreographerStepRequestDTO();
		stepC.setName("stepC");
		stepC.setNextStepNames(List.of("stepD"));

		final ChoreographerStepRequestDTO stepD = new ChoreographerStepRequestDTO();
		stepD.setName("stepD");
		stepD.setNextStepNames(null);
		
		return new ChoreographerActionRequestDTO("action", null, List.of("stepA"), List.of(stepA, stepB, stepC, stepD));
	}
	
	//-------------------------------------------------------------------------------------------------
	private StepGraph createDiamondGraph() {
		final Node nodeA = new Node("stepA");
		final Node nodeB = new Node("stepB");
		final Node nodeC = new Node("stepC");
		final Node nodeD = new Node("stepD");

		nodeA.getNextNodes().add(nodeB);
		nodeA.getNextNodes().add(nodeC);
		
		nodeB.getNextNodes().add(nodeD);
		nodeB.getPrevNodes().add(nodeA);

		nodeC.getNextNodes().add(nodeD);
		nodeC.getPrevNodes().add(nodeA);
		
		nodeD.getPrevNodes().add(nodeB);
		nodeD.getPrevNodes().add(nodeC);
		
		final StepGraph diamond = new StepGraph();
		diamond.getSteps().addAll(List.of(nodeA, nodeB, nodeC, nodeD));
		
		return diamond;
	}
	
	//-------------------------------------------------------------------------------------------------
	private void resetAction(final ChoreographerActionRequestDTO action) {
		action.setFirstStepNames(null);
		for (final ChoreographerStepRequestDTO step : action.getSteps()) {
			if (step.getNextStepNames() != null) {
				step.setNextStepNames(new ArrayList<>());
			}
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private ChoreographerStepRequestDTO findStep(final List<ChoreographerStepRequestDTO> steps, final String name) {
		for (final ChoreographerStepRequestDTO step : steps) {
			if (name.equals(step.getName())) {
				return step;
			}
		}
		
		return null;
	}
}