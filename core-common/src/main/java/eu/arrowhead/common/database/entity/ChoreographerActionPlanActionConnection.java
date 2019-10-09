package eu.arrowhead.common.database.entity;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.time.ZonedDateTime;

@Entity
@Table (uniqueConstraints = @UniqueConstraint(columnNames = {"actionPlanId", "actionId"}))
public class ChoreographerActionPlanActionConnection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne (fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn (name = "actionPlanId", referencedColumnName = "id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private ChoreographerActionPlan actionPlanEntry;

    @ManyToOne (fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn (name = "actionId", referencedColumnName = "id", nullable = false)
    @OnDelete (action = OnDeleteAction.CASCADE)
    private ChoreographerAction actionEntry;

    @Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private ZonedDateTime createdAt;

    @Column (nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private ZonedDateTime updatedAt;

    public ChoreographerActionPlanActionConnection() {}

    public ChoreographerActionPlanActionConnection(final ChoreographerActionPlan actionPlanEntry, final ChoreographerAction actionEntry) {
        this.actionPlanEntry = actionPlanEntry;
        this.actionEntry = actionEntry;
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

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public ChoreographerActionPlan getActionPlanEntry() {
        return actionPlanEntry;
    }

    public void setActionPlanEntry(ChoreographerActionPlan actionPlanEntry) {
        this.actionPlanEntry = actionPlanEntry;
    }

    public ChoreographerAction getActionEntry() {
        return actionEntry;
    }

    public void setActionEntry(ChoreographerAction actionEntry) {
        this.actionEntry = actionEntry;
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
}
