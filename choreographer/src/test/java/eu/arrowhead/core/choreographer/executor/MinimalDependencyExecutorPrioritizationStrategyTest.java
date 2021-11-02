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

package eu.arrowhead.core.choreographer.executor;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import eu.arrowhead.common.database.entity.ChoreographerExecutor;
import eu.arrowhead.common.dto.shared.ChoreographerExecutorServiceInfoResponseDTO;
import eu.arrowhead.common.dto.shared.ChoreographerServiceQueryFormDTO;

@RunWith(SpringRunner.class)
public class MinimalDependencyExecutorPrioritizationStrategyTest {
	
	//=================================================================================================
	// members
	
	private MinimalDependencyExecutorPrioritizationStrategy strategy = new MinimalDependencyExecutorPrioritizationStrategy();
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testPrioritize() {
		final List<ChoreographerExecutor> executors = new ArrayList<>(3);
		final Map<Long,ChoreographerExecutorServiceInfoResponseDTO> executorServiceInfos = new HashMap<>(3);
		
		final ChoreographerExecutor executor1 = new ChoreographerExecutor();
		executor1.setId(1);
		executors.add(executor1);
		executorServiceInfos.put(executor1.getId(), createServiceInfoForExecutor(5));
		final ChoreographerExecutor executor2 = new ChoreographerExecutor();
		executor2.setId(2);
		executors.add(executor2);
		executorServiceInfos.put(executor2.getId(), createServiceInfoForExecutor(3));
		final ChoreographerExecutor executor3 = new ChoreographerExecutor();
		executor3.setId(3);
		executors.add(executor3);
		executorServiceInfos.put(executor3.getId(), createServiceInfoForExecutor(0));
		
		final List<ChoreographerExecutor> result = strategy.prioritize(executors, executorServiceInfos);
		
		assertTrue(result.get(0).getId() == 3);
		assertTrue(result.get(1).getId() == 2);
		assertTrue(result.get(2).getId() == 1);
	}
	
	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	private ChoreographerExecutorServiceInfoResponseDTO createServiceInfoForExecutor(final int numOfDependency) {
		final ChoreographerExecutorServiceInfoResponseDTO serviceInfo = new ChoreographerExecutorServiceInfoResponseDTO();
		serviceInfo.setDependencies(new ArrayList<>(numOfDependency));
		for (int i = 0; i < numOfDependency; i++) {
			serviceInfo.getDependencies().add(new ChoreographerServiceQueryFormDTO());
		}
		return serviceInfo;
	}
}
