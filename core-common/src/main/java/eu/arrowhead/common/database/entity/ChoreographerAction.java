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

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import eu.arrowhead.common.CoreDefaults;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"name", "planId"}))
public class ChoreographerAction {
	
	//=================================================================================================
	// members

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(length = CoreDefaults.VARCHAR_BASIC, nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "planId", referencedColumnName = "id", nullable = false)
    private ChoreographerPlan plan;
    
    private boolean firstAction = false;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "nextActionId", referencedColumnName = "id", nullable = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private ChoreographerAction nextAction;

    @Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private ZonedDateTime createdAt;

    @Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private ZonedDateTime updatedAt;

    //=================================================================================================
	// methods

    //-------------------------------------------------------------------------------------------------
    public ChoreographerAction() {}

    //-------------------------------------------------------------------------------------------------
	public ChoreographerAction(final String name, final ChoreographerAction nextAction) {
        this.name = name;
        this.nextAction = nextAction;
    }

    //-------------------------------------------------------------------------------------------------

    public long getId() { return id; }
    public String getName() { return name; }
    public ChoreographerPlan getPlan() { return plan; }
    public boolean isFirstAction() { return firstAction; }
    public ChoreographerAction getNextAction() { return nextAction; }
    public ZonedDateTime getCreatedAt() { return createdAt; }
    public ZonedDateTime getUpdatedAt() { return updatedAt; }

    //-------------------------------------------------------------------------------------------------

    public void setId(final long id) { this.id = id; }
    public void setName(final String name) { this.name = name; }
    public void setPlan(final ChoreographerPlan plan) { this.plan = plan; }
    public void setFirstAction(final boolean firstAction) { this.firstAction = firstAction; }
    public void setNextAction(final ChoreographerAction nextAction) { this.nextAction = nextAction; }
    public void setCreatedAt(final ZonedDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(final ZonedDateTime updatedAt) { this.updatedAt = updatedAt; }

    //-------------------------------------------------------------------------------------------------
	@PrePersist
    public void onCreate() {
        this.createdAt = ZonedDateTime.now();
        this.updatedAt = this.createdAt;
    }

    //-------------------------------------------------------------------------------------------------
	@PreUpdate
    public void onUpdate() {
        this.updatedAt = ZonedDateTime.now();
    }

	//-------------------------------------------------------------------------------------------------
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		return result;
	}

	//-------------------------------------------------------------------------------------------------
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		
		final ChoreographerAction other = (ChoreographerAction) obj;
		if (id != other.id) {
			return false;
		}
		
		return true;
	}
}