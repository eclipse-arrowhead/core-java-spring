package eu.arrowhead.common.database.entity;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import java.time.ZonedDateTime;

@Entity
@Table
public class ChoreographerExecutorServiceDefinitionConnection {

    //=================================================================================================
    // members

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "executorId", referencedColumnName = "id", nullable = false)
    private ChoreographerExecutor executorEntry;

    @ManyToOne (fetch = FetchType.EAGER)
    @JoinColumn (name = "serviceDefinitionId", referencedColumnName = "id", nullable = false)
    private ChoreographerExecutorServiceDefinition serviceDefinitionEntry;

    @Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private ZonedDateTime createdAt;

    @Column (nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private ZonedDateTime updatedAt;

    //=================================================================================================
    // methods

    public ChoreographerExecutorServiceDefinitionConnection() {
    }

    public ChoreographerExecutorServiceDefinitionConnection(ChoreographerExecutor executorEntry, ChoreographerExecutorServiceDefinition serviceDefinitionEntry) {
        this.executorEntry = executorEntry;
        this.serviceDefinitionEntry = serviceDefinitionEntry;
    }

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

    //-------------------------------------------------------------------------------------------------
    public long getId() { return id; }
    public ChoreographerExecutor getExecutorEntry() { return executorEntry; }
    public ChoreographerExecutorServiceDefinition getServiceDefinitionEntry() { return serviceDefinitionEntry; }
    public ZonedDateTime getCreatedAt() { return createdAt; }
    public ZonedDateTime getUpdatedAt() { return updatedAt; }

    //-------------------------------------------------------------------------------------------------
    public void setId(long id) { this.id = id; }
    public void setExecutorEntry(ChoreographerExecutor executorEntry) { this.executorEntry = executorEntry; }
    public void setServiceDefinitionEntry(ChoreographerExecutorServiceDefinition serviceDefinitionEntry) { this.serviceDefinitionEntry = serviceDefinitionEntry; }
    public void setCreatedAt(ZonedDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(ZonedDateTime updatedAt) { this.updatedAt = updatedAt; }

    //-------------------------------------------------------------------------------------------------
    @Override
    public String toString() {
        return "ChoreographerExecutorServiceDefinitionConnection [id = " + id + ", executorEntry = " + executorEntry + ", serviceDefinitionEntry = " + serviceDefinitionEntry + "]";
    }
}
