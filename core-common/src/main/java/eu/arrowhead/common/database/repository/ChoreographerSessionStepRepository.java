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

package eu.arrowhead.common.database.repository;

import java.util.Set;

import org.springframework.stereotype.Repository;

import eu.arrowhead.common.database.entity.ChoreographerExecutor;
import eu.arrowhead.common.database.entity.ChoreographerSessionStep;
import eu.arrowhead.common.dto.internal.ChoreographerSessionStepStatus;

@Repository
public interface ChoreographerSessionStepRepository extends RefreshableRepository<ChoreographerSessionStep,Long> {

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public boolean existsByExecutorAndStatusIn(final ChoreographerExecutor executor, final Set<ChoreographerSessionStepStatus> statuses);
}
