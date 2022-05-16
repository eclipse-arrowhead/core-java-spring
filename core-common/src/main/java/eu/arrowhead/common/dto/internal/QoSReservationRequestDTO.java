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

import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.arrowhead.common.dto.shared.OrchestrationResultDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;

public class QoSReservationRequestDTO extends QoSTemporaryLockRequestDTO {

	//=================================================================================================
	// members
	
	private static final long serialVersionUID = -6108159091117829133L;
	
	private OrchestrationResultDTO selected;

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public QoSReservationRequestDTO(final OrchestrationResultDTO selected, final SystemRequestDTO requester, final List<OrchestrationResultDTO> orList) {
		super(requester, orList);
		this.selected = selected;
	}

	//-------------------------------------------------------------------------------------------------
	public OrchestrationResultDTO getSelected() { return selected; }
	
	//-------------------------------------------------------------------------------------------------
	public void setSelected(final OrchestrationResultDTO selected) { this.selected = selected; }
	
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