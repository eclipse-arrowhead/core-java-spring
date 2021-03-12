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
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import eu.arrowhead.common.CoreDefaults;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"name", "actionId"}))
public class ChoreographerStep {
	
	//=================================================================================================
	// members

    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false, unique = true, length = CoreDefaults.VARCHAR_BASIC)
    private String name;

    @Column(nullable = false, unique = true, length = CoreDefaults.VARCHAR_BASIC)
    private String serviceName;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String metadata;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String parameters;

    @Column(nullable = false)
    private int quantity;

    @Column (nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private ZonedDateTime createdAt;

    @Column (nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private ZonedDateTime updatedAt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "actionFirstStepId", referencedColumnName = "id")
    private ChoreographerAction actionFirstStep;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "actionId", referencedColumnName = "id", nullable = false)
    private ChoreographerAction action;

    // SHOULD BE LAZY
    @OneToMany (mappedBy = "stepEntry", fetch = FetchType.EAGER, orphanRemoval = true)
    @OnDelete (action = OnDeleteAction.CASCADE)
    private Set<ChoreographerStepNextStepConnection> nextSteps = new HashSet<>();

    @OneToMany (mappedBy = "nextStepEntry", fetch = FetchType.EAGER, orphanRemoval = true)
    @OnDelete (action = OnDeleteAction.CASCADE)
    private Set<ChoreographerStepNextStepConnection> steps = new HashSet<>();

    @OneToMany(mappedBy = "step", fetch = FetchType.EAGER, orphanRemoval = true)
    @OnDelete (action = OnDeleteAction.CASCADE)
    private Set<ChoreographerRunningStep> runningSteps = new HashSet<>();

    //=================================================================================================
	// methods

    //-------------------------------------------------------------------------------------------------
	public ChoreographerStep() {}

    //-------------------------------------------------------------------------------------------------

    public ChoreographerStep(String name, String serviceName, String metadata, String parameters, ChoreographerAction action, int quantity) {
        this.name = name;
        this.serviceName = serviceName;
        this.metadata = metadata;
        this.parameters = parameters;
        this.action = action;
        this.quantity = quantity;
    }

    //-------------------------------------------------------------------------------------------------

    public long getId() { return id; }
    public String getName() { return name; }
    public ZonedDateTime getCreatedAt() { return createdAt; }
    public ZonedDateTime getUpdatedAt() { return updatedAt; }
    public ChoreographerAction getActionFirstStep() { return actionFirstStep; }
    public ChoreographerAction getAction() { return action; }
    public String getServiceName() { return serviceName; }
    public String getMetadata() { return metadata; }
    public String getParameters() { return parameters; }
    public Set<ChoreographerStepNextStepConnection> getNextSteps() { return nextSteps; }
    public Set<ChoreographerStepNextStepConnection> getSteps() { return steps; }
    public Set<ChoreographerRunningStep> getRunningSteps() { return runningSteps; }
    public int getQuantity() { return quantity; }
    //-------------------------------------------------------------------------------------------------

    public void setId(long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setCreatedAt(ZonedDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(ZonedDateTime updatedAt) { this.updatedAt = updatedAt; }
    public void setActionFirstStep(ChoreographerAction actionFirstStep) { this.actionFirstStep = actionFirstStep; }
    public void setAction(ChoreographerAction action) { this.action = action; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }
    public void setMetadata(String metadata) { this.metadata = metadata; }
    public void setParameters(String parameters) { this.parameters = parameters; }
    public void setNextSteps(Set<ChoreographerStepNextStepConnection> nextSteps) { this.nextSteps = nextSteps; }
    public void setSteps(Set<ChoreographerStepNextStepConnection> actionSteps) { this.steps = actionSteps; }
    public void setRunningSteps(Set<ChoreographerRunningStep> runningSteps) { this.runningSteps = runningSteps; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

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