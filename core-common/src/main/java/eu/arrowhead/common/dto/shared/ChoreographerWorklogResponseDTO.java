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

public class ChoreographerWorklogResponseDTO implements Serializable {

	//=================================================================================================
    // members

	private static final long serialVersionUID = -1278815996356098724L;
	
	private long id;
    private String entryDate;
    private String planName;
    private String actionName;
    private String stepName;
    private Long sessionId;
    private Long executionNumber;
    private String message;
    private String exception;
    
    //=================================================================================================
    // methods

    //-------------------------------------------------------------------------------------------------
    public ChoreographerWorklogResponseDTO() {}
    
    //-------------------------------------------------------------------------------------------------
	public ChoreographerWorklogResponseDTO(final long id, final String entryDate, final String planName, final String actionName, final String stepName, final Long sessionId,
										   final Long executionNumber, final String message, final String exception) {
		this.id = id;
		this.entryDate = entryDate;
		this.planName = planName;
		this.actionName = actionName;
		this.stepName = stepName;
		this.sessionId = sessionId;
		this.executionNumber = executionNumber;
		this.message = message;
		this.exception = exception;
	}

	//-------------------------------------------------------------------------------------------------
	public long getId() { return id; }
	public String getEntryDate() { return entryDate; }
	public String getPlanName() { return planName; }
	public String getActionName() { return actionName; }
	public String getStepName() { return stepName; }
	public Long getSessionId() { return sessionId; }
	public Long getExecutionNumber() { return executionNumber; }
	public String getMessage() { return message; }
	public String getException() { return exception; }
	
	//-------------------------------------------------------------------------------------------------
	public void setId(final long id) { this.id = id; }
	public void setEntryDate(final String entryDate) { this.entryDate = entryDate; }
	public void setPlanName(final String planName) { this.planName = planName; }
	public void setActionName(final String actionName) { this.actionName = actionName; }
	public void setStepName(final String stepName) { this.stepName = stepName; }
	public void setSessionId(final Long sessionId) { this.sessionId = sessionId; }
	public void setExecutionNumber(final Long executionNumber) { this.executionNumber = executionNumber; }
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