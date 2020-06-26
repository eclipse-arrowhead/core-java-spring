package eu.arrowhead.common.database.entity;

import eu.arrowhead.common.CoreDefaults;

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
public class ChoreographerStepDetail {

    //=================================================================================================
    // members

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false, length = CoreDefaults.VARCHAR_BASIC)
    private String serviceDefinition;

    @Column
    private Integer version;

    @Column(nullable = false, length = CoreDefaults.VARCHAR_BASIC)
    private String type;

    @Column
    private Integer minVersion;

    @Column
    private Integer maxVersion;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String dto;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "stepId", referencedColumnName = "id", nullable = false)
    private ChoreographerStep step;

    @Column (nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private ZonedDateTime createdAt;

    @Column (nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private ZonedDateTime updatedAt;

    //=================================================================================================
    // methods

    //-------------------------------------------------------------------------------------------------
    public ChoreographerStepDetail() { }

    //-------------------------------------------------------------------------------------------------
    public ChoreographerStepDetail(final String serviceDefinition, final String dto, final Integer version, final ChoreographerStep step) {
        this.serviceDefinition = serviceDefinition;
        this.dto = dto;
        this.version = version;
        this.step = step;
    }

    //-------------------------------------------------------------------------------------------------
    public ChoreographerStepDetail(final String serviceDefinition, final String dto, final Integer minVersion, final Integer maxVersion, final ChoreographerStep step) {
        this.serviceDefinition = serviceDefinition;
        this.dto = dto;
        this.minVersion = minVersion;
        this.maxVersion = maxVersion;
        this.step = step;
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
    public String getServiceDefinition() { return serviceDefinition; }
    public Integer getVersion() { return version; }
    public String getType() { return type; }
    public Integer getMinVersion() { return minVersion; }
    public Integer getMaxVersion() { return maxVersion; }
    public String getDto() { return dto; }
    public ChoreographerStep getStep() { return step; }
    public ZonedDateTime getCreatedAt() { return createdAt; }
    public ZonedDateTime getUpdatedAt() { return updatedAt; }

    //-------------------------------------------------------------------------------------------------
    public void setId(long id) { this.id = id; }
    public void setServiceDefinition(String serviceDefinition) { this.serviceDefinition = serviceDefinition; }
    public void setVersion(Integer version) { this.version = version; }
    public void setType(String type) { this.type = type; }
    public void setMinVersion(Integer minVersion) { this.minVersion = minVersion; }
    public void setMaxVersion(Integer maxVersion) { this.maxVersion = maxVersion; }
    public void setDto(String dto) { this.dto = dto; }
    public void setStep(ChoreographerStep step) { this.step = step; }
    public void setCreatedAt(ZonedDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(ZonedDateTime updatedAt) { this.updatedAt = updatedAt; }

    //-------------------------------------------------------------------------------------------------
    @Override
    public String toString() {
        return "StepDetail [id = " + id + ", serviceDefinition = " + serviceDefinition + ", version = " + version + ", type = " + type +
                ", minVersion = " + minVersion + ", maxVersion = " + maxVersion + ", dto = " + dto + ", step = " + step + ", createdAt = " + createdAt + ", updatedAt = " + updatedAt + "]";
    }
}
