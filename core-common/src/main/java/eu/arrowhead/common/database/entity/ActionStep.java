package eu.arrowhead.common.database.entity;

import eu.arrowhead.common.Defaults;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
public class ActionStep {

    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false, unique = true, length = Defaults.VARCHAR_BASIC)
    private String name;

    @ManyToOne (fetch = FetchType.EAGER)
    @JoinColumn (name = "actionId", referencedColumnName = "id", nullable = false)
    private Action action;

    @ManyToMany (cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    @JoinTable(
            name = "actionStep_service",
            joinColumns = @JoinColumn(name = "actionStepId"),
            inverseJoinColumns = @JoinColumn(name = "serviceDefinitionId")
    )
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Set<ServiceDefinition> usedServices = new HashSet<>();

    @ManyToMany (cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    @JoinTable(
            name = "nextAction",
            joinColumns = @JoinColumn(name = "actionStepId"),
            inverseJoinColumns = @JoinColumn(name = "nextActionId")
    )
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Set<ActionStep> nextActions = new HashSet<>();

    @ManyToMany(mappedBy = "nextActions")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Set<ActionStep> planStep = new HashSet<>();

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

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
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

    public Set<ActionStep> getNextActions() {
        return nextActions;
    }

    public void setNextActions(Set<ActionStep> nextActions) {
        this.nextActions = nextActions;
    }

    public Set<ActionStep> getPlanStep() {
        return planStep;
    }

    public void setPlanStep(Set<ActionStep> planStep) {
        this.planStep = planStep;
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
