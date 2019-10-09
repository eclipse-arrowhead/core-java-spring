package eu.arrowhead.common.database.entity;

import eu.arrowhead.common.CoreDefaults;
import eu.arrowhead.common.Defaults;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
public class ChoreographerActionPlan {

    public static final List<String> SORTABLE_FIELDS_BY = List.of("id", "updatedAt", "createdAt");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false, unique = true, length = CoreDefaults.VARCHAR_BASIC)
    private String actionPlanName;

    @Column (nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private ZonedDateTime createdAt;

    @Column (nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private ZonedDateTime updatedAt;

    @OneToMany (mappedBy = "actionPlanEntry", fetch = FetchType.EAGER, orphanRemoval = true, cascade = CascadeType.ALL)
    @OnDelete (action = OnDeleteAction.CASCADE)
    private Set<ChoreographerActionPlanActionConnection> actionPlanActionConnections = new HashSet<>();

    public ChoreographerActionPlan() {}

    public ChoreographerActionPlan(String actionPlanName) {
        this.actionPlanName = actionPlanName;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getActionPlanName() {
        return actionPlanName;
    }

    public void setActionPlanName(String actionPlanName) {
        this.actionPlanName = actionPlanName;
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
