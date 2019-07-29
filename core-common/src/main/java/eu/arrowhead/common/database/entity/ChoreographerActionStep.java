package eu.arrowhead.common.database.entity;

import eu.arrowhead.common.Defaults;
import org.hibernate.annotations.ManyToAny;
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

    @Column(nullable = false, unique = true, length = Defaults.VARCHAR_BASIC)
    private String name;

    @ManyToMany (cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    @JoinTable(
            name = "ChoreographerActionStepServiceDefinitionConnection",
            joinColumns = @JoinColumn(name = "actionStepId"),
            inverseJoinColumns = @JoinColumn(name = "serviceDefinitionId")
    )
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Set<ServiceDefinition> usedServices = new HashSet<>();

    @ManyToMany (cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    @JoinTable(
            name = "ChoreographerNextActionStep",
            joinColumns = @JoinColumn(name = "actionStepId"),
            inverseJoinColumns = @JoinColumn(name = "nextActionId")
    )
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Set<ChoreographerActionStep> nextActions = new HashSet<>();

    @ManyToMany (cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    @JoinTable(
            name = "ChoreographerActionActionStepConnection",
            joinColumns = @JoinColumn(name = "actionStepId"),
            inverseJoinColumns = @JoinColumn(name = "actionId")
    )
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Set<ChoreographerAction> actions = new HashSet<>();

    @ManyToMany(mappedBy = "nextActions")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Set<ChoreographerActionStep> actionSteps = new HashSet<>();

    @Column (nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private ZonedDateTime createdAt;

    @Column (nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private ZonedDateTime updatedAt;

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

    public Set<ServiceDefinition> getUsedServices() {
        return usedServices;
    }

    public void setUsedServices(Set<ServiceDefinition> usedServices) {
        this.usedServices = usedServices;
    }

    public Set<ChoreographerActionStep> getNextActions() {
        return nextActions;
    }

    public void setNextActions(Set<ChoreographerActionStep> nextActions) {
        this.nextActions = nextActions;
    }

    public Set<ChoreographerActionStep> getPlanStep() {
        return actionSteps;
    }

    public void setPlanStep(Set<ChoreographerActionStep> actionSteps) {
        this.actionSteps = actionSteps;
    }

    public Set<ChoreographerAction> getActions() {
        return actions;
    }

    public void setActions(Set<ChoreographerAction> actions) {
        this.actions = actions;
    }

    public Set<ChoreographerActionStep> getActionSteps() {
        return actionSteps;
    }

    public void setActionSteps(Set<ChoreographerActionStep> actionSteps) {
        this.actionSteps = actionSteps;
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
