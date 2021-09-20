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

package eu.arrowhead.common.dto.shared;

import java.io.Serializable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ChoreographerAbortStepRequestDTO implements Serializable {

	//=================================================================================================
    // members

	private static final long serialVersionUID = -4367804324253105575L;

	private long sessionId;
    private long sessionStepId;

    //=================================================================================================
    // methods

    //-------------------------------------------------------------------------------------------------
    public ChoreographerAbortStepRequestDTO() {}

    //-------------------------------------------------------------------------------------------------
    public ChoreographerAbortStepRequestDTO(final long sessionId, final long sessionStepId) {
    	this.sessionId = sessionId;
    	this.sessionStepId = sessionStepId;
    }
    
	//-------------------------------------------------------------------------------------------------
    public long getSessionId() { return sessionId; }
	public long getSessionStepId() { return sessionStepId; }

    //-------------------------------------------------------------------------------------------------
    public void setSessionId(final long sessionId) { this.sessionId = sessionId; }
	public void setSessionStepId(final long sessionStepId) { this.sessionStepId = sessionStepId; }
	
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