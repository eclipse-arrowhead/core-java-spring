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

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import eu.arrowhead.common.dto.shared.ChoreographerActionRequestDTO;
import eu.arrowhead.common.dto.shared.ChoreographerPlanRequestDTO;

@RunWith(SpringRunner.class)
public class ActionCircleDetectorTest {
	
	//=================================================================================================
	// members
	
	private ActionCircleDetector detector = new ActionCircleDetector();
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void hasCircleSelfReferencing() {
		final ChoreographerPlanRequestDTO plan = createPlanWithSelfReferencingAction();
		final boolean hasCircle = detector.hasCircle(plan);
		
		Assert.assertTrue(hasCircle);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void hasCircleNormalCircle() {
		final ChoreographerPlanRequestDTO plan = createPlanWithActionCircle();
		final boolean hasCircle = detector.hasCircle(plan);
		
		Assert.assertTrue(hasCircle);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void hasCircleNoCircle() {
		final ChoreographerPlanRequestDTO plan = createPlanWithoutActionCircle();
		final boolean hasCircle = detector.hasCircle(plan);
		
		Assert.assertFalse(hasCircle);
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private ChoreographerPlanRequestDTO createPlanWithSelfReferencingAction() {
		final ChoreographerActionRequestDTO onlyAction = new ChoreographerActionRequestDTO("onlyAction", "onlyAction", null, null);
		
		return new ChoreographerPlanRequestDTO("plan", "onlyAction", List.of(onlyAction));
	}
	
	//-------------------------------------------------------------------------------------------------
	private ChoreographerPlanRequestDTO createPlanWithActionCircle() {
		final ChoreographerActionRequestDTO actionA = new ChoreographerActionRequestDTO("actionA", "actionB", null, null);
		final ChoreographerActionRequestDTO actionB = new ChoreographerActionRequestDTO("actionB", "actionC", null, null);
		final ChoreographerActionRequestDTO actionC = new ChoreographerActionRequestDTO("actionC", "actionA", null, null);
		
		return new ChoreographerPlanRequestDTO("plan", "actionA", List.of(actionA, actionB, actionC));
	}
	
	//-------------------------------------------------------------------------------------------------
	private ChoreographerPlanRequestDTO createPlanWithoutActionCircle() {
		final ChoreographerActionRequestDTO actionA = new ChoreographerActionRequestDTO("actionA", "actionB", null, null);
		final ChoreographerActionRequestDTO actionB = new ChoreographerActionRequestDTO("actionB", "actionC", null, null);
		final ChoreographerActionRequestDTO actionC = new ChoreographerActionRequestDTO("actionC", null, null, null);
		
		return new ChoreographerPlanRequestDTO("plan", "actionA", List.of(actionA, actionB, actionC));
	}
}