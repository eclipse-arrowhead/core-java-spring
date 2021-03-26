/********************************************************************************
 * Copyright (c) 2019 AITIA
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

import java.util.Optional;

import org.springframework.stereotype.Repository;

import eu.arrowhead.common.database.entity.ChoreographerPlan;

@Repository
public interface ChoreographerPlanRepository extends RefreshableRepository<ChoreographerPlan,Long> {
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
    public Optional<ChoreographerPlan> findByName(final String name);
}