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
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

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
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
public class ChoreographerSession {

    //=================================================================================================
    // members

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "planId", referencedColumnName = "id", nullable = false)
    private ChoreographerPlan plan;

    @Column(nullable = false, columnDefinition = "varchar(" + CoreDefaults.VARCHAR_BASIC + ")")
    @Enumerated(EnumType.STRING)
    private ChoreographerStatusType status;

    @Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private ZonedDateTime startedAt;

    @Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private ZonedDateTime updatedAt;

    @OneToMany(mappedBy = "session", fetch = FetchType.LAZY, orphanRemoval = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Set<ChoreographerRunningStep> runningSteps = new HashSet<>();

    //=================================================================================================
    // methods

    //=================================================================================================
    public ChoreographerSession() {}

    //=================================================================================================
    public ChoreographerSession(ChoreographerPlan plan, ChoreographerStatusType status) {
        this.plan = plan;
        this.status = status;
    }

    //=================================================================================================
    public long getId() { return id; }
    public ChoreographerPlan getPlan() { return plan; }
    public ChoreographerStatusType getStatus() { return status; }
    public ZonedDateTime getStartedAt() { return startedAt; }
    public Set<ChoreographerRunningStep> getRunningSteps() { return runningSteps; }

    //=================================================================================================
    public void setId(long id) { this.id = id; }
    public void setPlan(ChoreographerPlan plan) { this.plan = plan; }
    public void setStatus(ChoreographerStatusType status) { this.status = status; }
    public void setStartedAt(ZonedDateTime startedAt) { this.startedAt = startedAt; }
    public void setRunningSteps(Set<ChoreographerRunningStep> runningSteps) { this.runningSteps = runningSteps; }

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
