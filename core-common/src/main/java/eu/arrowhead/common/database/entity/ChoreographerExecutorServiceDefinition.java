package eu.arrowhead.common.database.entity;

import eu.arrowhead.common.CoreDefaults;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"serviceDefinitionName", "version"}))
public class ChoreographerExecutorServiceDefinition {

    //=================================================================================================
    // members

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false, length = CoreDefaults.VARCHAR_BASIC)
    private String serviceDefinitionName;

    @Column(nullable = false)
    private Integer version;

    @Column (nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private ZonedDateTime createdAt;

    @Column (nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private ZonedDateTime updatedAt;

    @OneToMany(mappedBy = "serviceDefinitionEntry", fetch = FetchType.LAZY, orphanRemoval = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Set<ChoreographerExecutorServiceDefinitionConnection> executorConnections = new HashSet<>();

    //=================================================================================================
    // methods

    //-------------------------------------------------------------------------------------------------
    public ChoreographerExecutorServiceDefinition() {
    }

    //-------------------------------------------------------------------------------------------------
    public ChoreographerExecutorServiceDefinition(String serviceDefinitionName, Integer version) {
        this.serviceDefinitionName = serviceDefinitionName;
        this.version = version;
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
    public String getServiceDefinitionName() { return serviceDefinitionName; }
    public Integer getVersion() { return version; }
    public Set<ChoreographerExecutorServiceDefinitionConnection> getExecutorConnections() { return executorConnections; }
    public ZonedDateTime getCreatedAt() { return createdAt; }
    public ZonedDateTime getUpdatedAt() { return updatedAt; }

    //-------------------------------------------------------------------------------------------------
    public void setId(long id) { this.id = id; }
    public void setServiceDefinitionName(String serviceDefinitionName) { this.serviceDefinitionName = serviceDefinitionName; }
    public void setVersion(Integer version) { this.version = version; }
    public void setExecutorConnections(Set<ChoreographerExecutorServiceDefinitionConnection> executorConnections) { this.executorConnections = executorConnections; }
    public void setCreatedAt(ZonedDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(ZonedDateTime updatedAt) { this.updatedAt = updatedAt; }

    //-------------------------------------------------------------------------------------------------
    @Override
    public String toString() {
        return "ExecutorServiceDefinition [id = " + id + ", serviceDefinitionName = " + serviceDefinitionName + ", version = " + "]";
    }
}
