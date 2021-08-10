package eu.arrowhead.common.database.entity;

import java.time.ZonedDateTime;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import eu.arrowhead.common.CoreDefaults;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"address", "port", "baseUri"}))
public class ChoreographerExecutor {

    //=================================================================================================
    // members

    public static final List<String> SORTABLE_FIELDS_BY = List.of("id", "updatedAt", "createdAt", "name"); //NOSONAR

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false, length = CoreDefaults.VARCHAR_BASIC, unique = true)
    private String name;

    @Column(nullable = false, length = CoreDefaults.VARCHAR_BASIC)
    private String address;

    @Column(nullable = false)
    private int port;

    @Column(nullable = false, length = CoreDefaults.VARCHAR_BASIC)
    private String baseUri = "";
    
    private boolean locked = false;

    @Column (nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private ZonedDateTime createdAt;

    @Column (nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private ZonedDateTime updatedAt;

    //=================================================================================================
    // methods

    //-------------------------------------------------------------------------------------------------
    public ChoreographerExecutor() { }

    //-------------------------------------------------------------------------------------------------
    public ChoreographerExecutor(final String name, final String address, final int port) {
        this.name = name;
        this.address = address;
        this.port = port;
    }

    //-------------------------------------------------------------------------------------------------
    public ChoreographerExecutor(final String name, final String address, final int port, final String baseUri) {
        this.name = name;
        this.address = address;
        this.port = port;
        this.baseUri = baseUri;
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
    public boolean isLocked() { return locked; }
	public ZonedDateTime getCreatedAt() { return createdAt; }
    public ZonedDateTime getUpdatedAt() { return updatedAt; }

    //-------------------------------------------------------------------------------------------------
    public void setId(final long id) { this.id = id; }
    public void setName(final String name) { this.name = name; }
    public void setAddress(final String address) { this.address = address; }
    public void setPort(final int port) { this.port = port; }
    public void setBaseUri(final String baseUri) { this.baseUri = baseUri; }
    public void setLocked(final boolean locked) { this.locked = locked; }
	public void setCreatedAt(final ZonedDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(final ZonedDateTime updatedAt) { this.updatedAt = updatedAt; }

    //-------------------------------------------------------------------------------------------------
    @Override
    public String toString() {
        return "Executor [id = " + id + ", name = " + name + ", address = " + address + ", port = " + port + ", baseUri = " + baseUri + "locked = " + locked + "]";
    }
}
