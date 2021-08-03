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
import java.util.stream.Collectors;

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

    @Column(nullable = false, length = CoreDefaults.VARCHAR_BASIC)
    private String name;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "actionId", referencedColumnName = "id", nullable = false)
    private ChoreographerAction action;
    
    private boolean firstStep = false;
    
    @Column(nullable = false, length = CoreDefaults.VARCHAR_BASIC)
    private String serviceDefinition;

    @Column
    private Integer minVersion;

    @Column
    private Integer maxVersion;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String orchTemplate;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String staticParameters;

    @Column(nullable = false)
    private int quantity;

    @Column (nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private ZonedDateTime createdAt;

    @Column (nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private ZonedDateTime updatedAt;

    @OneToMany (mappedBy = "from", fetch = FetchType.EAGER, orphanRemoval = true)
    @OnDelete (action = OnDeleteAction.CASCADE)
    private Set<ChoreographerStepNextStepConnection> nextStepsConnections = new HashSet<>();

    @OneToMany (mappedBy = "to", fetch = FetchType.EAGER, orphanRemoval = true)
    @OnDelete (action = OnDeleteAction.CASCADE)
    private Set<ChoreographerStepNextStepConnection> previousStepsConnections = new HashSet<>();

    //=================================================================================================
	// methods

    //-------------------------------------------------------------------------------------------------
	public ChoreographerStep() {}

    //-------------------------------------------------------------------------------------------------
    public ChoreographerStep(final String name, final ChoreographerAction action, final String serviceDefinition, final Integer minVersion, final Integer maxVersion, final String orchTemplate, final String staticParameters, final int quantity) {
        this.name = name;
        this.action = action;
        this.serviceDefinition = serviceDefinition;
        this.minVersion = minVersion;
        this.maxVersion = maxVersion;
        this.orchTemplate = orchTemplate;
        this.staticParameters = staticParameters;
        this.quantity = quantity;
    }

    //-------------------------------------------------------------------------------------------------
    public long getId() { return id; }
    public String getName() { return name; }
    public ChoreographerAction getAction() { return action; }
    public String getServiceDefinition() { return serviceDefinition; }
    public Integer getMinVersion() { return minVersion; }
    public Integer getMaxVersion() { return maxVersion; }
    public String getOrchTemplate() { return orchTemplate; }
    public String getStaticParameters() { return staticParameters; }
    public int getQuantity() { return quantity; }
    public ZonedDateTime getCreatedAt() { return createdAt; }
    public ZonedDateTime getUpdatedAt() { return updatedAt; }
    public Set<ChoreographerStepNextStepConnection> getNextStepsConnections() { return nextStepsConnections; }
    public Set<ChoreographerStepNextStepConnection> getPreviousStepsConnections() { return previousStepsConnections; }
    
    //-------------------------------------------------------------------------------------------------
	public Set<ChoreographerStep> getNextSteps() {
		return nextStepsConnections.stream().map(c -> c.getTo()).collect(Collectors.toSet());
	}
	
	//-------------------------------------------------------------------------------------------------
	public Set<ChoreographerStep> getPreviousSteps() {
		return previousStepsConnections.stream().map(c -> c.getFrom()).collect(Collectors.toSet());
	}

    //-------------------------------------------------------------------------------------------------
    public void setId(final long id) { this.id = id; }
    public void setName(final String name) { this.name = name; }
    public void setAction(final ChoreographerAction action) { this.action = action; }
    public void setServiceDefinition(final String serviceDefinition) { this.serviceDefinition = serviceDefinition; }
    public void setMinVersion(final Integer minVersion) { this.minVersion = minVersion; }
    public void setMaxVersion(final Integer maxVersion) { this.maxVersion = maxVersion; }
    public void setOrchTemplate(final String orchTemplate) { this.orchTemplate = orchTemplate; }
    public void setStaticParameters(final String parameters) { this.staticParameters = parameters; }
    public void setQuantity(final int quantity) { this.quantity = quantity; }
    public void setCreatedAt(final ZonedDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(final ZonedDateTime updatedAt) { this.updatedAt = updatedAt; }
    public void setNextStepsConnections(final Set<ChoreographerStepNextStepConnection> nextStepsConnections) { this.nextStepsConnections = nextStepsConnections; }
    public void setPreviousStepsConnections(final Set<ChoreographerStepNextStepConnection> previousStepsConnections) { this.previousStepsConnections = previousStepsConnections; }

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

	public boolean isFirstStep() {
		return firstStep;
	}

	public void setFirstStep(final boolean firstStep) {
		this.firstStep = firstStep;
	}
}