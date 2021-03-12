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
import java.util.ArrayList;
import java.util.List;

import eu.arrowhead.common.dto.shared.OrchestrationResultDTO;

public class QoSTemporaryLockResponseDTO implements Serializable {
	
	//=================================================================================================
	// members
	
	private static final long serialVersionUID = 2279611306510520063L;
	
	protected List<OrchestrationResultDTO> response = new ArrayList<>();
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public QoSTemporaryLockResponseDTO() {}

	//-------------------------------------------------------------------------------------------------
	public QoSTemporaryLockResponseDTO(final List<OrchestrationResultDTO> response) {
		this.response = response != null ? response : List.of();
	}

	//-------------------------------------------------------------------------------------------------
	public List<OrchestrationResultDTO> getResponse() { return response; }

	//-------------------------------------------------------------------------------------------------
	public void setResponse(final List<OrchestrationResultDTO> response) {
		if (response != null) {
			this.response = response;
		}
	}

}
