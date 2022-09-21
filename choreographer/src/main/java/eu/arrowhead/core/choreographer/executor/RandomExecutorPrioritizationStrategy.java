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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.arrowhead.common.database.entity.ChoreographerExecutor;
import eu.arrowhead.common.dto.shared.ChoreographerExecutorServiceInfoResponseDTO;


public class RandomExecutorPrioritizationStrategy implements ExecutorPrioritizationStrategy {

	//=================================================================================================
	// members
	
	private Random rng;
	
	private static final Logger logger = LogManager.getLogger(ExecutorPrioritizationStrategy.class);
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@Override
	public List<ChoreographerExecutor> prioritize(final List<ChoreographerExecutor> executors, final Map<Long,ChoreographerExecutorServiceInfoResponseDTO> executorServiceInfos) {
		logger.debug("priorize started");
		
		if (executors.isEmpty()) {
			return new ArrayList<>();
		}
		
		if (rng == null) {
			rng = new Random(System.currentTimeMillis());
		}
		
		Collections.shuffle(executors, rng);
		return executors;
	}
}
