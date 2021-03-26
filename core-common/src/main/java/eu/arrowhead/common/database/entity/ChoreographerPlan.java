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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

import eu.arrowhead.common.CoreDefaults;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
public class ChoreographerPlan {
	
	//=================================================================================================
	// members

    public static final List<String> SORTABLE_FIELDS_BY = List.of("id", "updatedAt", "createdAt", "name");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false, length = CoreDefaults.VARCHAR_BASIC)
    private String name;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "firstActionId", referencedColumnName = "id")
    private ChoreographerAction firstAction;

    @OneToMany(mappedBy = "plan", fetch = FetchType.LAZY, orphanRemoval = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Set<ChoreographerAction> actions = new HashSet<>();

    @OneToMany(mappedBy = "plan", fetch = FetchType.LAZY, orphanRemoval = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Set<ChoreographerSession> sessions = new HashSet<>();

    @Column (nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private ZonedDateTime createdAt;

    @Column (nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private ZonedDateTime updatedAt;

    //=================================================================================================
	// methods

    //-------------------------------------------------------------------------------------------------
	public ChoreographerPlan() {}

    //-------------------------------------------------------------------------------------------------
	public ChoreographerPlan(final String name) {
	    this.name = name;
    }

    //-------------------------------------------------------------------------------------------------
	public long getId() { return id; }
	public String getName() { return name; }
	public ZonedDateTime getCreatedAt() { return createdAt; }
	public ZonedDateTime getUpdatedAt() { return updatedAt; }
    public ChoreographerAction getFirstAction() { return firstAction; }
    public Set<ChoreographerAction> getActions() { return actions; }
    public Set<ChoreographerSession> getSessions() { return sessions; }

    //-------------------------------------------------------------------------------------------------
	public void setId(final long id) { this.id = id; }
    public void setName(final String name) { this.name = name; }
    public void setCreatedAt(final ZonedDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(final ZonedDateTime updatedAt) { this.updatedAt = updatedAt; }
    public void setFirstAction(ChoreographerAction firstAction) { this.firstAction = firstAction; }
    public void setActions(Set<ChoreographerAction> actions) { this.actions = actions; }
    public void setSessions(Set<ChoreographerSession> sessions) { this.sessions = sessions; }

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
}