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
import eu.arrowhead.common.dto.shared.ChoreographerServiceQueryFormDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;

public class ExecutorData {
	
	//=================================================================================================
	// members
	
	private final ChoreographerExecutor executor;
	private final SystemRequestDTO executorSystem;
	private final List<ChoreographerServiceQueryFormDTO> dependencyForms = new ArrayList<>();
	private final boolean useOtherClouds;
	
	//-------------------------------------------------------------------------------------------------
	public ExecutorData(final ChoreographerExecutor executor, final SystemRequestDTO executorSystem, final List<ChoreographerServiceQueryFormDTO> dependencyForms, final boolean useOtherClouds) {
		this.executor = executor;
		this.executorSystem = executorSystem;
		if (dependencyForms != null) {
			this.dependencyForms.addAll(dependencyForms);
		}
		this.useOtherClouds = useOtherClouds;
	}

	//-------------------------------------------------------------------------------------------------
	public ChoreographerExecutor getExecutor() { return executor; }
	public SystemRequestDTO getExecutorSystem() { return executorSystem; }
	public List<ChoreographerServiceQueryFormDTO> getDependencyForms() { return dependencyForms; }
	public boolean getUseOtherClouds() { return useOtherClouds; }
}