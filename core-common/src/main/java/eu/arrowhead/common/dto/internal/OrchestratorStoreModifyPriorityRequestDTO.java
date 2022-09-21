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

package eu.arrowhead.common.dto.internal;

import java.io.Serializable;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class OrchestratorStoreModifyPriorityRequestDTO implements Serializable {

	//=================================================================================================
	// members
	
	private static final long serialVersionUID = 9141560687987073900L;

	private Map<Long,Integer> priorityMap;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public OrchestratorStoreModifyPriorityRequestDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public OrchestratorStoreModifyPriorityRequestDTO(final Map<Long,Integer> priorityMap) {
		this.priorityMap = priorityMap;
	}
	
	//-------------------------------------------------------------------------------------------------
	public Map<Long,Integer> getPriorityMap() { return priorityMap; }

	//-------------------------------------------------------------------------------------------------
	public void setPriorityMap(final Map<Long,Integer> priorityMap) { this.priorityMap = priorityMap; }
	
	//-------------------------------------------------------------------------------------------------
	@Override
	public String toString() {
		try {
			return new ObjectMapper().writeValueAsString(this);
		} catch (final JsonProcessingException ex) {
			return "toString failure";
		}
	}
}