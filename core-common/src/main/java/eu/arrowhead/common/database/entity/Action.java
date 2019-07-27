package eu.arrowhead.common.database.entity;

import eu.arrowhead.common.Defaults;

import javax.persistence.*;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

// Formerly known as "Plan(s)"
@Entity
public class Action {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false, unique = true, length = Defaults.VARCHAR_BASIC)
    private String actionName;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "nextActionId", referencedColumnName = "id", nullable = true)
    private Action nextAction;

    @ManyToOne (fetch = FetchType.EAGER)
    @JoinColumn (name = "actionPlanId", referencedColumnName = "id", nullable = false)
    private ActionPlan actionPlan;

    @OneToMany(mappedBy = "action", fetch = FetchType.EAGER)
    private Set<ActionStep> actionSteps = new HashSet<>();

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

    public String getActionName() {
        return actionName;
    }

    public void setActionName(String actionName) {
        this.actionName = actionName;
    }

    public Action getNextAction() {
        return nextAction;
    }

    public void setNextAction(Action nextAction) {
        this.nextAction = nextAction;
    }

    public ActionPlan getActionPlan() {
        return actionPlan;
    }

    public void setActionPlan(ActionPlan actionPlan) {
        this.actionPlan = actionPlan;
    }

    public Set<ActionStep> getActionSteps() {
        return actionSteps;
    }

    public void setActionSteps(Set<ActionStep> actionSteps) {
        this.actionSteps = actionSteps;
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
