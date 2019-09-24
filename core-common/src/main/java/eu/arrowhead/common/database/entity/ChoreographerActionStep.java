package eu.arrowhead.common.database.entity;

import eu.arrowhead.common.CoreDefaults;
import eu.arrowhead.common.Defaults;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
public class ChoreographerActionStep {

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

    public ChoreographerActionStep() {}

    public ChoreographerActionStep(String name) {
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public ZonedDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(ZonedDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Set<ChoreographerActionActionStepConnection> getActionActionStepConnections() {
        return actionActionStepConnections;
    }

    public void setActionActionStepConnections(Set<ChoreographerActionActionStepConnection> actionActionStepConnections) {
        this.actionActionStepConnections = actionActionStepConnections;
    }

    public Set<ChoreographerNextActionStep> getNextActionSteps() {
        return nextActionSteps;
    }

    public void setNextActionSteps(Set<ChoreographerNextActionStep> nextActionSteps) {
        this.nextActionSteps = nextActionSteps;
    }

    public Set<ChoreographerNextActionStep> getActionSteps() {
        return actionSteps;
    }


    public void setActionSteps(Set<ChoreographerNextActionStep> actionSteps) {
        this.actionSteps = actionSteps;
    }

    public Set<ChoreographerActionStepServiceDefinitionConnection> getActionStepServiceDefinitionConnections() {
        return actionStepServiceDefinitionConnections;
    }

    public void setActionStepServiceDefinitionConnections(Set<ChoreographerActionStepServiceDefinitionConnection> actionStepServiceDefinitionConnections) {
        this.actionStepServiceDefinitionConnections = actionStepServiceDefinitionConnections;
    }

    @PrePersist
    public void onCreate() {
        this.createdAt = ZonedDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = ZonedDateTime.now();
    }
}
