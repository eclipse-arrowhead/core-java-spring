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
    private String srTemplate;

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
    private Set<ChoreographerStepNextStepConnection> nextStepConnections = new HashSet<>();

    @OneToMany (mappedBy = "to", fetch = FetchType.EAGER, orphanRemoval = true)
    @OnDelete (action = OnDeleteAction.CASCADE)
    private Set<ChoreographerStepNextStepConnection> previousStepConnections = new HashSet<>();

    //=================================================================================================
	// methods

    //-------------------------------------------------------------------------------------------------
	public ChoreographerStep() {}

    //-------------------------------------------------------------------------------------------------
    public ChoreographerStep(final String name, final ChoreographerAction action, final String serviceDefinition, final Integer minVersion, final Integer maxVersion, final String srTemplate, final String staticParameters, final int quantity) {
        this.name = name;
        this.action = action;
        this.serviceDefinition = serviceDefinition;
        this.minVersion = minVersion;
        this.maxVersion = maxVersion;
        this.srTemplate = srTemplate;
        this.staticParameters = staticParameters;
        this.quantity = quantity;
    }

    //-------------------------------------------------------------------------------------------------
    public long getId() { return id; }
    public String getName() { return name; }
    public ChoreographerAction getAction() { return action; }
    public boolean isFirstStep() { return firstStep; }
    public String getServiceDefinition() { return serviceDefinition; }
    public Integer getMinVersion() { return minVersion; }
    public Integer getMaxVersion() { return maxVersion; }
    public String getSrTemplate() { return srTemplate; }
    public String getStaticParameters() { return staticParameters; }
    public int getQuantity() { return quantity; }
    public ZonedDateTime getCreatedAt() { return createdAt; }
    public ZonedDateTime getUpdatedAt() { return updatedAt; }
    public Set<ChoreographerStepNextStepConnection> getNextStepConnections() { return nextStepConnections; }
    public Set<ChoreographerStepNextStepConnection> getPreviousStepConnections() { return previousStepConnections; }
    
    //-------------------------------------------------------------------------------------------------
	public Set<ChoreographerStep> getNextSteps() {
		return nextStepConnections.stream().map(c -> c.getTo()).collect(Collectors.toSet());
	}
	
	//-------------------------------------------------------------------------------------------------
	public Set<ChoreographerStep> getPreviousSteps() {
		return previousStepConnections.stream().map(c -> c.getFrom()).collect(Collectors.toSet());
	}

    //-------------------------------------------------------------------------------------------------
    public void setId(final long id) { this.id = id; }
    public void setName(final String name) { this.name = name; }
    public void setAction(final ChoreographerAction action) { this.action = action; }
    public void setFirstStep(final boolean firstStep) { this.firstStep = firstStep; }
    public void setServiceDefinition(final String serviceDefinition) { this.serviceDefinition = serviceDefinition; }
    public void setMinVersion(final Integer minVersion) { this.minVersion = minVersion; }
    public void setMaxVersion(final Integer maxVersion) { this.maxVersion = maxVersion; }
    public void setSrTemplate(final String srTemplate) { this.srTemplate = srTemplate; }
    public void setStaticParameters(final String parameters) { this.staticParameters = parameters; }
    public void setQuantity(final int quantity) { this.quantity = quantity; }
    public void setCreatedAt(final ZonedDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(final ZonedDateTime updatedAt) { this.updatedAt = updatedAt; }
    public void setNextStepConnections(final Set<ChoreographerStepNextStepConnection> nextStepConnections) { this.nextStepConnections = nextStepConnections; }
    public void setPreviousStepConnections(final Set<ChoreographerStepNextStepConnection> previousStepConnections) { this.previousStepConnections = previousStepConnections; }

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