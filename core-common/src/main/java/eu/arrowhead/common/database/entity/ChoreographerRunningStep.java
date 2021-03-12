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

import eu.arrowhead.common.CoreDefaults;
import eu.arrowhead.common.dto.internal.ChoreographerStatusType;

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

import java.time.ZonedDateTime;

@Entity
public class ChoreographerRunningStep {

    //=================================================================================================
    // members

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false, columnDefinition = "varchar(" + CoreDefaults.VARCHAR_BASIC + ")")
    @Enumerated(EnumType.STRING)
    private ChoreographerStatusType status;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private ZonedDateTime startedAt;

    @Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private ZonedDateTime updatedAt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "stepId", referencedColumnName = "id", nullable = false)
    private ChoreographerStep step;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sessionId", referencedColumnName = "id", nullable = false)
    private ChoreographerSession session;

    //=================================================================================================
    // methods

    //=================================================================================================
    public ChoreographerRunningStep() {}

    //=================================================================================================
    public ChoreographerRunningStep(ChoreographerStatusType status, String message, ChoreographerStep step, ChoreographerSession session) {
        this.status = status;
        this.message = message;
        this.step = step;
        this.session = session;
    }

    //=================================================================================================
    public long getId() { return id; }
    public ChoreographerStatusType getStatus() { return status; }
    public String getMessage() { return message; }
    public ZonedDateTime getStartedAt() { return startedAt; }
    public ChoreographerStep getStep() { return step; }
    public ChoreographerSession getSession() { return session; }

    //=================================================================================================

    public void setId(long id) { this.id = id; }
    public void setStatus(ChoreographerStatusType status) { this.status = status; }
    public void setMessage(String message) { this.message = message; }
    public void setStartedAt(ZonedDateTime startedAt) { this.startedAt = startedAt; }
    public void setStep(ChoreographerStep step) { this.step = step; }
    public void setSession(ChoreographerSession session) { this.session = session; }

    //-------------------------------------------------------------------------------------------------
    @PrePersist
    public void onCreate() {
        this.startedAt = ZonedDateTime.now();
        this.updatedAt = this.startedAt;
    }

    //-------------------------------------------------------------------------------------------------
    @PreUpdate
    public void onUpdate() {
        this.updatedAt = ZonedDateTime.now();
    }
}
