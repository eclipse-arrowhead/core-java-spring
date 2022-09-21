package eu.arrowhead.common.database.entity;

import java.time.ZonedDateTime;

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
import javax.persistence.UniqueConstraint;

import eu.arrowhead.common.CoreDefaults;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "executorId", "serviceDefinition"}))
public class ChoreographerExecutorServiceDefinition {

    //=================================================================================================
    // members

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "executorId", referencedColumnName = "id", nullable = false)
    private ChoreographerExecutor executor;

    @Column(nullable = false, length = CoreDefaults.VARCHAR_BASIC)
    private String serviceDefinition;

    private Integer minVersion;
    private Integer maxVersion;

    @Column (nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private ZonedDateTime createdAt;

    @Column (nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private ZonedDateTime updatedAt;

    //=================================================================================================
    // methods

    //-------------------------------------------------------------------------------------------------
    public ChoreographerExecutorServiceDefinition() {
    }

    //-------------------------------------------------------------------------------------------------
    public ChoreographerExecutorServiceDefinition(final ChoreographerExecutor executor, final String serviceDefinition, final Integer minVersion, final Integer maxVersion) {
    	this.setExecutor(executor);
        this.setServiceDefinition(serviceDefinition);
        this.minVersion = minVersion;
        this.maxVersion = maxVersion;
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
    public ChoreographerExecutor getExecutor() { return executor; }
    public String getServiceDefinition() { return serviceDefinition; }
    public Integer getMinVersion() { return minVersion; }
    public Integer getMaxVersion() { return maxVersion; }
    public ZonedDateTime getCreatedAt() { return createdAt; }
    public ZonedDateTime getUpdatedAt() { return updatedAt; }

    //-------------------------------------------------------------------------------------------------
    public void setId(final long id) { this.id = id; }
    public void setExecutor(final ChoreographerExecutor executor) { this.executor = executor; }
    public void setServiceDefinition(final String serviceDefinition) { this.serviceDefinition = serviceDefinition; }
    public void setMinVersion(final Integer minVersion) { this.minVersion = minVersion; }
    public void setMaxVersion(final Integer maxVersion) { this.maxVersion = maxVersion; }
    public void setCreatedAt(final ZonedDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(final ZonedDateTime updatedAt) { this.updatedAt = updatedAt; }
}