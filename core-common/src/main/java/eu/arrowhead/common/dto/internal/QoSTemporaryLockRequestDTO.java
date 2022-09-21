/********************************************************************************
 * Copyright (c) 2020 AITIA
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
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.arrowhead.common.dto.shared.OrchestrationResultDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;

public class QoSTemporaryLockRequestDTO implements Serializable {

	//=================================================================================================
	// members
	
	private static final long serialVersionUID = -7584022504960220540L;
	
	private SystemRequestDTO requester;
	private List<OrchestrationResultDTO> orList;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public QoSTemporaryLockRequestDTO() {}
	
	//-------------------------------------------------------------------------------------------------
	public QoSTemporaryLockRequestDTO(final SystemRequestDTO requester, final List<OrchestrationResultDTO> orList) {		
		this.requester = requester;
		this.orList = orList;
	}
	
	//-------------------------------------------------------------------------------------------------
	public SystemRequestDTO getRequester() { return requester; }
	public List<OrchestrationResultDTO> getOrList() { return orList; }
	
	//-------------------------------------------------------------------------------------------------
	public void setRequester(final SystemRequestDTO requester) { this.requester = requester; }
	public void setOrList(final List<OrchestrationResultDTO> orList) { this.orList = orList; }
	
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