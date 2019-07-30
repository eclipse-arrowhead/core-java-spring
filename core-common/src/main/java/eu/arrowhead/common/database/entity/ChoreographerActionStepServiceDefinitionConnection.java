package eu.arrowhead.common.database.entity;

import javax.persistence.*;
import java.time.ZonedDateTime;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"actionStepId", "serviceDefinitionId"}))
public class ChoreographerActionStepServiceDefinitionConnection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn (name = "actionStepId", referencedColumnName = "id", nullable = false)
    private ChoreographerActionStep actionStepEntry;

    @ManyToOne (fetch = FetchType.EAGER)
    @JoinColumn (name = "serviceDefinitionId", referencedColumnName = "id", nullable = false)
    private ServiceDefinition serviceDefinitionEntry;

    @Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private ZonedDateTime createdAt;

    @Column (nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private ZonedDateTime updatedAt;

    public ChoreographerActionStepServiceDefinitionConnection() {}

    public ChoreographerActionStepServiceDefinitionConnection(ChoreographerActionStep actionStepEntry, ServiceDefinition serviceDefinitionEntry) {
        this.actionStepEntry = actionStepEntry;
        this.serviceDefinitionEntry = serviceDefinitionEntry;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public ChoreographerActionStep getActionStepEntry() {
        return actionStepEntry;
    }

    public void setActionStepEntry(ChoreographerActionStep actionStepEntry) {
        this.actionStepEntry = actionStepEntry;
    }

    public ServiceDefinition getServiceDefinitionEntry() {
        return serviceDefinitionEntry;
    }

    public void setServiceDefinitionEntry(ServiceDefinition serviceDefinitionEntry) {
        this.serviceDefinitionEntry = serviceDefinitionEntry;
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
