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

import eu.arrowhead.common.database.entity.ChoreographerExecutor;
import eu.arrowhead.common.dto.shared.ServiceQueryFormDTO;

public class ExecutorData {
	
	//=================================================================================================
	// members
	
	private final ChoreographerExecutor executor;
	private final List<ServiceQueryFormDTO> dependencyForms = new ArrayList<>();
	
	//-------------------------------------------------------------------------------------------------
	public ExecutorData(final ChoreographerExecutor executor, final List<ServiceQueryFormDTO> dependencyForms) {
		this.executor = executor;
		this.dependencyForms.addAll(dependencyForms);
	}

	//-------------------------------------------------------------------------------------------------
	public ChoreographerExecutor getExecutor() { return executor; }
	public List<ServiceQueryFormDTO> getDependencyForms() { return dependencyForms; }
}