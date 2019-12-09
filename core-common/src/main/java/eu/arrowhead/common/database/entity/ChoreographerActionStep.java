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
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import eu.arrowhead.common.CoreDefaults;

@Entity
public class ChoreographerActionStep {
	
	//=================================================================================================
	// members

    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false, unique = true, length = CoreDefaults.VARCHAR_BASIC)
    private String name;

    @Column (nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private ZonedDateTime createdAt;

    @Column (nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private ZonedDateTime updatedAt;

    @OneToMany (mappedBy = "actionStepEntry", fetch = FetchType.LAZY, orphanRemoval = true)
    @OnDelete (action = OnDeleteAction.CASCADE)
    private Set<ChoreographerActionActionStepConnection> actionActionStepConnections = new HashSet<>();

    @OneToMany (mappedBy = "actionStepEntry", fetch = FetchType.LAZY, orphanRemoval = true)
    @OnDelete (action = OnDeleteAction.CASCADE)
    private Set<ChoreographerNextActionStep> nextActionSteps = new HashSet<>();

    @OneToMany (mappedBy = "nextActionStepEntry", fetch = FetchType.EAGER, orphanRemoval = true)
    @OnDelete (action = OnDeleteAction.CASCADE)
    private Set<ChoreographerNextActionStep> actionSteps = new HashSet<>();

    @OneToMany (mappedBy = "actionStepEntry", fetch = FetchType.EAGER, orphanRemoval = true)
    @OnDelete (action = OnDeleteAction.CASCADE)
    private Set<ChoreographerActionStepServiceDefinitionConnection> actionStepServiceDefinitionConnections = new HashSet<>();

    //=================================================================================================
	// methods
    
    //-------------------------------------------------------------------------------------------------
	public ChoreographerActionStep() {}

    //-------------------------------------------------------------------------------------------------
	public ChoreographerActionStep(final String name) {
        this.name = name;
    }

    //-------------------------------------------------------------------------------------------------
	public long getId() { return id; }
	public String getName() { return name; }
	public ZonedDateTime getCreatedAt() { return createdAt; }
	public ZonedDateTime getUpdatedAt() { return updatedAt; }
	public Set<ChoreographerActionActionStepConnection> getActionActionStepConnections() { return actionActionStepConnections; }
	public Set<ChoreographerNextActionStep> getNextActionSteps() { return nextActionSteps; }
	public Set<ChoreographerNextActionStep> getActionSteps() { return actionSteps; }
	public Set<ChoreographerActionStepServiceDefinitionConnection> getActionStepServiceDefinitionConnections() { return actionStepServiceDefinitionConnections; }
    
	//-------------------------------------------------------------------------------------------------
	public void setId(final long id) { this.id = id; }
    public void setName(final String name) { this.name = name; }
    public void setCreatedAt(final ZonedDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(final ZonedDateTime updatedAt) { this.updatedAt = updatedAt; }
    public void setActionActionStepConnections(final Set<ChoreographerActionActionStepConnection> actionActionStepConnections) { this.actionActionStepConnections = actionActionStepConnections; }
    public void setNextActionSteps(final Set<ChoreographerNextActionStep> nextActionSteps) { this.nextActionSteps = nextActionSteps; }
    public void setActionSteps(final Set<ChoreographerNextActionStep> actionSteps) { this.actionSteps = actionSteps; }
    public void setActionStepServiceDefinitionConnections(final Set<ChoreographerActionStepServiceDefinitionConnection> actionStepServiceDefinitionConnections) {
    	this.actionStepServiceDefinitionConnections = actionStepServiceDefinitionConnections;
    }

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