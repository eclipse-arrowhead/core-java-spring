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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ChoreographerStartSessionDTO implements Serializable {

    //=================================================================================================
    // members

    private static final long serialVersionUID = 205131759149025379L;
    
    private long sessionId;
    private long planId;

    //=================================================================================================
    // methods

    //-------------------------------------------------------------------------------------------------
    public ChoreographerStartSessionDTO() {}

    //-------------------------------------------------------------------------------------------------
    public ChoreographerStartSessionDTO(final long sessionId, final long planId) {
        this.sessionId = sessionId;
        this.planId = planId;
    }

    //-------------------------------------------------------------------------------------------------
    public long getSessionId() { return sessionId; }
    public long getPlanId() { return planId; }

    //-------------------------------------------------------------------------------------------------
    public void setSessionId(final long sessionId) { this.sessionId = sessionId; }
    public void setPlanId(final long planId) { this.planId = planId; }
    
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