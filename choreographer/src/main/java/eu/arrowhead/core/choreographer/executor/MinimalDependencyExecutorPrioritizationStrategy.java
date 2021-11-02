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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.arrowhead.common.database.entity.ChoreographerExecutor;
import eu.arrowhead.common.dto.shared.ChoreographerExecutorServiceInfoResponseDTO;

public class MinimalDependencyExecutorPrioritizationStrategy implements ExecutorPrioritizationStrategy {

	//=================================================================================================
	// members
	
	private static final Logger logger = LogManager.getLogger(MinimalDependencyExecutorPrioritizationStrategy.class);
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@Override
	public List<ChoreographerExecutor> prioritize(final List<ChoreographerExecutor> executors, final Map<Long,ChoreographerExecutorServiceInfoResponseDTO> executorServiceInfos) {
		logger.debug("priorize started");
		
		if (executors.isEmpty()) {
			return new ArrayList<>();
		}
		
		//Ascending sort by num of dependencies
		executors.sort((final ChoreographerExecutor e1, final ChoreographerExecutor e2) ->
						executorServiceInfos.get(e1.getId()).getDependencies().size() - executorServiceInfos.get(e2.getId()).getDependencies().size());
		return executors;
	}
}
