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

import java.io.Serializable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ChoreographerExecutedStepResultDTO implements Serializable {
	
	//=================================================================================================
	// members
	
	private static final long serialVersionUID = -8977335371238750835L;

	private Long sessionId;
    private Long sessionStepId;
    private ChoreographerExecutedStepStatus status;
    private String message;
    private String exception;
	
    //=================================================================================================
	// methods
    
    //-------------------------------------------------------------------------------------------------
	public Long getSessionId() { return sessionId; }
	public Long getSessionStepId() { return sessionStepId; }
	public ChoreographerExecutedStepStatus getStatus() { return status; }
	public String getMessage() { return message; }
	public String getException() { return exception; }
	
	//-------------------------------------------------------------------------------------------------
	public void setSessionId(final Long sessionId) { this.sessionId = sessionId; }
	public void setSessionStepId(final Long sessionStepId) { this.sessionStepId = sessionStepId; }
	public void setStatus(final ChoreographerExecutedStepStatus status) { this.status = status; }
	public void setMessage(final String message) { this.message = message; }
	public void setException(final String exception) { this.exception = exception; }
	
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