package eu.arrowhead.common.database.entity;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.*;

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

    @OneToMany (mappedBy = "stepEntry", fetch = FetchType.LAZY, orphanRemoval = true)
    @OnDelete (action = OnDeleteAction.CASCADE)
    private Set<ChoreographerStepNextStepConnection> nextSteps = new HashSet<>();

    @OneToMany (mappedBy = "nextStepEntry", fetch = FetchType.EAGER, orphanRemoval = true)
    @OnDelete (action = OnDeleteAction.CASCADE)
    private Set<ChoreographerStepNextStepConnection> steps = new HashSet<>();

    @OneToMany (mappedBy = "stepEntry", fetch = FetchType.EAGER, orphanRemoval = true)
    @OnDelete (action = OnDeleteAction.CASCADE)
    private Set<ChoreographerStepServiceDefinitionConnection> stepServiceDefinitionConnections = new HashSet<>();

    @OneToMany(mappedBy = "step", fetch = FetchType.EAGER, orphanRemoval = true)
    @OnDelete (action = OnDeleteAction.CASCADE)
    private Set<ChoreographerRunningStep> runningSteps = new HashSet<>();

    //=================================================================================================
	// methods

    //-------------------------------------------------------------------------------------------------
	public ChoreographerStep() {}

    //-------------------------------------------------------------------------------------------------
	public ChoreographerStep(final String name) {
        this.name = name;
    }

    //-------------------------------------------------------------------------------------------------

    public long getId() { return id; }
    public String getName() { return name; }
    public ZonedDateTime getCreatedAt() { return createdAt; }
    public ZonedDateTime getUpdatedAt() { return updatedAt; }
    public ChoreographerAction getActionFirstStep() { return actionFirstStep; }
    public ChoreographerAction getAction() { return action; }
    public Set<ChoreographerStepNextStepConnection> getNextSteps() { return nextSteps; }
    public Set<ChoreographerStepNextStepConnection> getSteps() { return steps; }
    public Set<ChoreographerStepServiceDefinitionConnection> getStepServiceDefinitionConnections() { return stepServiceDefinitionConnections; }
    public Set<ChoreographerRunningStep> getRunningSteps() { return runningSteps; }
    //-------------------------------------------------------------------------------------------------

    public void setId(long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setCreatedAt(ZonedDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(ZonedDateTime updatedAt) { this.updatedAt = updatedAt; }
    public void setActionFirstStep(ChoreographerAction actionFirstStep) { this.actionFirstStep = actionFirstStep; }
    public void setAction(ChoreographerAction action) { this.action = action; }
    public void setNextSteps(Set<ChoreographerStepNextStepConnection> nextSteps) { this.nextSteps = nextSteps; }
    public void setSteps(Set<ChoreographerStepNextStepConnection> actionSteps) { this.steps = actionSteps; }
    public void setStepServiceDefinitionConnections(Set<ChoreographerStepServiceDefinitionConnection> actionStepServiceDefinitionConnections) {
	    this.stepServiceDefinitionConnections = actionStepServiceDefinitionConnections;
	}
    public void setRunningSteps(Set<ChoreographerRunningStep> runningSteps) { this.runningSteps = runningSteps; }

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