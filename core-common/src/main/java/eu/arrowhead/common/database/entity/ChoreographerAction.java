package eu.arrowhead.common.database.entity;

import eu.arrowhead.common.Defaults;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

// Formerly known as "Plan(s)"
@Entity
public class ChoreographerAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false, unique = true, length = Defaults.VARCHAR_BASIC)
    private String actionName;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "nextActionId", referencedColumnName = "id", nullable = true)
    private ChoreographerAction nextAction;

    @Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private ZonedDateTime createdAt;

    @Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private ZonedDateTime updatedAt;

    @ManyToMany(mappedBy = "actions")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Set<ChoreographerActionStep> actionSteps = new HashSet<>();

    @ManyToMany (cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    @JoinTable(
            name = "ChoreographerActionPlanActionConnection",
            joinColumns = @JoinColumn(name = "actionId"),
            inverseJoinColumns = @JoinColumn(name = "actionPlanId")
    )
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Set<ChoreographerActionPlan> actionPlans = new HashSet<>();

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getActionName() {
        return actionName;
    }

    public void setActionName(String actionName) {
        this.actionName = actionName;
    }

    public ChoreographerAction getNextAction() {
        return nextAction;
    }

    public void setNextAction(ChoreographerAction nextAction) {
        this.nextAction = nextAction;
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

    public Set<ChoreographerActionStep> getActionSteps() {
        return actionSteps;
    }

    public void setActionSteps(Set<ChoreographerActionStep> actionSteps) {
        this.actionSteps = actionSteps;
    }

    public Set<ChoreographerActionPlan> getActionPlans() {
        return actionPlans;
    }

    public void setActionPlans(Set<ChoreographerActionPlan> actionPlans) {
        this.actionPlans = actionPlans;
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
