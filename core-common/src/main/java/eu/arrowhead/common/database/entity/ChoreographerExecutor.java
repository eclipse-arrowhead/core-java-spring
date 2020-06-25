package eu.arrowhead.common.database.entity;

import eu.arrowhead.common.CoreDefaults;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.time.ZonedDateTime;
import java.util.List;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"serviceDefinitionName", "version"}))
public class ChoreographerExecutor {

    //=================================================================================================
    // members

    public static final List<String> SORTABLE_FIELDS_BY = List.of("id", "updatedAt", "createdAt", "name", "serviceDefinitionName"); //NOSONAR

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false, length = CoreDefaults.VARCHAR_BASIC)
    private String name;

    @Column(nullable = false, length = CoreDefaults.VARCHAR_BASIC)
    private String address;

    @Column(nullable = false)
    private int port;

    @Column(nullable = true, length = CoreDefaults.VARCHAR_BASIC)
    private String baseUri;

    @Column(nullable = false, length = CoreDefaults.VARCHAR_BASIC)
    private String serviceDefinitionName;

    @Column(nullable = false)
    private Integer version;

    @Column (nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private ZonedDateTime createdAt;

    @Column (nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private ZonedDateTime updatedAt;

    //=================================================================================================
    // methods

    //-------------------------------------------------------------------------------------------------
    public ChoreographerExecutor() { }

    //-------------------------------------------------------------------------------------------------
    public ChoreographerExecutor(final String name, final String address, final int port, final String serviceDefinitionName, final Integer version) {
        this.name = name;
        this.address = address;
        this.port = port;
        this.serviceDefinitionName = serviceDefinitionName;
        this.version = version;
    }

    //-------------------------------------------------------------------------------------------------
    public ChoreographerExecutor(final String name, final String address, final int port, final String baseUri, final String serviceDefinitionName, final Integer version) {
        this.name = name;
        this.address = address;
        this.port = port;
        this.baseUri = baseUri;
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
    public String getName() { return name; }
    public String getAddress() { return address; }
    public int getPort() { return port; }
    public String getBaseUri() { return baseUri; }
    public String getServiceDefinitionName() { return serviceDefinitionName; }
    public Integer getVersion() { return version; }
    public ZonedDateTime getCreatedAt() { return createdAt; }
    public ZonedDateTime getUpdatedAt() { return updatedAt; }

    //-------------------------------------------------------------------------------------------------
    public void setId(long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setAddress(String address) { this.address = address; }
    public void setPort(int port) { this.port = port; }
    public void setBaseUri(String baseUri) { this.baseUri = baseUri; }
    public void setServiceDefinitionName(String serviceDefinitionName) { this.serviceDefinitionName = serviceDefinitionName; }
    public void setVersion(Integer version) { this.version = version; }
    public void setCreatedAt(ZonedDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(ZonedDateTime updatedAt) { this.updatedAt = updatedAt; }

    //-------------------------------------------------------------------------------------------------
    @Override
    public String toString() {
        return "Executor [id = " + id + ", name = " + name + ", address = " + address + ", port = " + port + ", baseUri = " + baseUri + ", serviceDefinitionName = " + serviceDefinitionName + ", version = " + version + "]";
    }
}
