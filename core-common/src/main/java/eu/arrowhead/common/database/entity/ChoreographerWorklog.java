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

package eu.arrowhead.common.database.entity;

import java.time.ZonedDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;

@Entity
public class ChoreographerWorklog {

    //=================================================================================================
    // members

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private ZonedDateTime entryDate;

    private String planName;
    private String actionName;
    private String stepName;
    private Long sessionId;
    private Long executionNumber;

    @Column(nullable = false, columnDefinition = "MEDIUMTEXT")
    private String message;

    @Column(columnDefinition = "MEDIUMTEXT")
    private String exception;

    //=================================================================================================
    // methods

    //-------------------------------------------------------------------------------------------------
    public ChoreographerWorklog() {}

    //-------------------------------------------------------------------------------------------------
    public ChoreographerWorklog(final String planName, final String actionName, final String stepName, final Long sessionId, final Long executionNumber, final String message, final String exception) {
    	this.planName = planName;
    	this.actionName = actionName;
    	this.stepName = stepName;
    	this.sessionId = sessionId;
    	this.executionNumber = executionNumber;
    	this.message = message;
    	this.exception = exception;
    }
    

    //-------------------------------------------------------------------------------------------------
    public ChoreographerWorklog(final String planName, final String actionName, final Long sessionId, final Long executionNumber, final String message, final String exception) {
    	this(planName, actionName, null, sessionId, executionNumber, message, exception);
    }
    

	//-------------------------------------------------------------------------------------------------
    public ChoreographerWorklog(final String planName, final String message, final String exception) {
    	this(planName, null, null, null, null, message, exception);
    }

    //-------------------------------------------------------------------------------------------------
    public long getId() { return id; }
    public ZonedDateTime getEntryDate() { return entryDate; }
    public String getPlanName() { return planName; }
    public String getActionName() { return actionName; }
    public String getStepName() { return stepName; }
    public Long getSessionId() { return sessionId; }
	public Long getExecutionNumber() { return executionNumber; 	}
	public String getMessage() { return message; }
    public String getException() { return exception; }

    //-------------------------------------------------------------------------------------------------
    public void setId(final long id) { this.id = id; }
    public void setPlanName(final String planName) { this.planName = planName; }
    public void setActionName(final String actionName) { this.actionName = actionName; }
    public void setStepName(final String stepName) { this.stepName = stepName; }
    public void setEntryDate(final ZonedDateTime entryDate) { this.entryDate = entryDate; }
    public void setSessionId(final Long sessionId) { this.sessionId = sessionId; }
    public void setExecutionNumber(final Long executionNumber) { this.executionNumber = executionNumber; }
	public void setMessage(final String message) { this.message = message; }
    public void setException(final String exception) { this.exception = exception; }

    //-------------------------------------------------------------------------------------------------
    @PrePersist
    public void onCreate() { this.entryDate = ZonedDateTime.now(); }
}