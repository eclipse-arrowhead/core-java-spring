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

import eu.arrowhead.common.CoreDefaults;
import eu.arrowhead.common.dto.shared.ChoreographerSessionStatus;

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
    private ChoreographerSessionStatus status;
    
    @Column(nullable = false)
    private long quantityDone = 0;
    
    @Column(nullable = false)
    private long quantityGoal = 1;
    
    @Column(nullable = false)
    private long executionNumber = 0;
    
    private String notifyUri;

    @Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private ZonedDateTime startedAt;

    @Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private ZonedDateTime updatedAt;

    //=================================================================================================
    // methods

    //-------------------------------------------------------------------------------------------------
    public ChoreographerSession() {}

    //-------------------------------------------------------------------------------------------------
    public ChoreographerSession(final ChoreographerPlan plan, final ChoreographerSessionStatus status, final long quantityGoal, final String notifyUri) {
        this.plan = plan;
        this.status = status;
        this.quantityGoal = quantityGoal;
        this.notifyUri = notifyUri;
    }

    //-------------------------------------------------------------------------------------------------
    public long getId() { return id; }
    public ChoreographerPlan getPlan() { return plan; }
    public ChoreographerSessionStatus getStatus() { return status; }
    public long getQuantityDone() { return quantityDone; }
    public long getQuantityGoal() { return quantityGoal; }
	public long getExecutionNumber() { return executionNumber; }
	public String getNotifyUri() { return notifyUri; }
    public ZonedDateTime getStartedAt() { return startedAt; }
    public ZonedDateTime getUpdatedAt() { return updatedAt; }

    //-------------------------------------------------------------------------------------------------
    public void setId(final long id) { this.id = id; }
    public void setPlan(final ChoreographerPlan plan) { this.plan = plan; }
    public void setStatus(final ChoreographerSessionStatus status) { this.status = status; }
    public void setQuantityDone(final long quantityDone) { this.quantityDone = quantityDone; }
    public void setQuantityGoal(final long quantityGoal) { this.quantityGoal = quantityGoal; }
    public void setExecutionNumber(final long executionNumber) { this.executionNumber = executionNumber; }
    public void setNotifyUri(final String notifyUri) { this.notifyUri = notifyUri; }
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