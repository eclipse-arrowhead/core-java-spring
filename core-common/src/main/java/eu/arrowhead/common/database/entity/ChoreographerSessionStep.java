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
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import eu.arrowhead.common.CoreDefaults;
import eu.arrowhead.common.dto.internal.ChoreographerSessionStepStatus;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"sessionId", "stepId", "executionNumber"}))
public class ChoreographerSessionStep {

    //=================================================================================================
    // members

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sessionId", referencedColumnName = "id", nullable = false)
    private ChoreographerSession session;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "stepId", referencedColumnName = "id", nullable = false)
    private ChoreographerStep step;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "executorId", referencedColumnName = "id", nullable = false)
    private ChoreographerExecutor executor;
    
    @Column(nullable = false, columnDefinition = "varchar(" + CoreDefaults.VARCHAR_BASIC + ")")
    @Enumerated(EnumType.STRING)
    private ChoreographerSessionStepStatus status;
    
    @Column(nullable = false)
    private long executionNumber;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private ZonedDateTime startedAt;

    @Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private ZonedDateTime updatedAt;

    //=================================================================================================
    // methods

    //-------------------------------------------------------------------------------------------------
    public ChoreographerSessionStep() {}

    //-------------------------------------------------------------------------------------------------
	public ChoreographerSessionStep(final ChoreographerSession session, final ChoreographerStep step, final ChoreographerExecutor executor, final ChoreographerSessionStepStatus status,
									final long executionNumber, final String message) {
    	this.session = session;
    	this.step = step;
    	this.executor = executor;
        this.status = status;
        this.executionNumber = executionNumber;
        this.message = message;
    }

    //-------------------------------------------------------------------------------------------------
	public long getId() { return id; }
    public ChoreographerSession getSession() { return session; }
    public ChoreographerStep getStep() { return step; }
    public ChoreographerExecutor getExecutor() { return executor; }
    public ChoreographerSessionStepStatus getStatus() { return status; }
    public long getExecutionNumber() { return executionNumber; }
	public String getMessage() { return message; }
    public ZonedDateTime getStartedAt() { return startedAt; }
    public ZonedDateTime getUpdatedAt() { return updatedAt; }

    //-------------------------------------------------------------------------------------------------
    public void setId(final long id) { this.id = id; }
    public void setSession(final ChoreographerSession session) { this.session = session; }
    public void setStep(final ChoreographerStep step) { this.step = step; }
    public void setExecutor(final ChoreographerExecutor executor) { this.executor = executor;	}
    public void setStatus(final ChoreographerSessionStepStatus status) { this.status = status; }
    public void setExecutionNumber(final long executionNumber) { this.executionNumber = executionNumber; }
    public void setMessage(final String message) { this.message = message; }
    public void setStartedAt(final ZonedDateTime startedAt) { this.startedAt = startedAt; }
    public void setUpdatedAt(final ZonedDateTime updatedAt) { this.updatedAt = updatedAt; }

    //-------------------------------------------------------------------------------------------------
    @PrePersist
    public void onCreate() {
        this.startedAt = ZonedDateTime.now();
        this.setUpdatedAt(this.startedAt);
    }

    //-------------------------------------------------------------------------------------------------
    @PreUpdate
    public void onUpdate() {
        this.setUpdatedAt(ZonedDateTime.now());
    }
}