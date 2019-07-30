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

    @OneToMany (mappedBy = "actionEntry", fetch = FetchType.LAZY, orphanRemoval = true)
    @OnDelete (action = OnDeleteAction.CASCADE)
    private Set<ChoreographerActionPlanActionConnection> actionPlanActionConnections = new HashSet<>();

    @OneToMany (mappedBy = "actionEntry", fetch = FetchType.EAGER, orphanRemoval = true)
    @OnDelete (action = OnDeleteAction.CASCADE)
    private Set<ChoreographerActionActionStepConnection> actionActionStepConnections = new HashSet<>();

    @Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private ZonedDateTime createdAt;

    @Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private ZonedDateTime updatedAt;

    public ChoreographerAction() {}

    public ChoreographerAction(String actionName, ChoreographerAction nextAction) {
        this.actionName = actionName;
        this.nextAction = nextAction;
    }

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

    public Set<ChoreographerActionPlanActionConnection> getActionPlanActionConnections() {
        return actionPlanActionConnections;
    }

    public void setActionPlanActionConnections(Set<ChoreographerActionPlanActionConnection> actionPlanActionConnections) {
        this.actionPlanActionConnections = actionPlanActionConnections;
    }

    public Set<ChoreographerActionActionStepConnection> getActionActionStepConnections() {
        return actionActionStepConnections;
    }

    public void setActionActionStepConnections(Set<ChoreographerActionActionStepConnection> actionActionStepConnections) {
        this.actionActionStepConnections = actionActionStepConnections;
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
