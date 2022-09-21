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

package eu.arrowhead.common.dto.shared;

public enum ChoreographerExecutedStepStatus {
	SUCCESS, ERROR, FATAL_ERROR, ABORTED;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public boolean isError() {
		return this == ERROR || this == FATAL_ERROR;
	}
	
	//-------------------------------------------------------------------------------------------------
	public boolean isFatal() {
		return this == FATAL_ERROR;
	}
}