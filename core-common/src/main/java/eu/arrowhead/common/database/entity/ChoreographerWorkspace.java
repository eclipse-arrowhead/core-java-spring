package eu.arrowhead.common.database.entity;

import eu.arrowhead.common.CoreDefaults;

import javax.persistence.*;
import java.time.ZonedDateTime;
import java.util.List;

@Entity
public class ChoreographerWorkspace {

    public static final List<String> SORTABLE_FIELDS_BY = List.of("id", "createdAt", "updatedAt");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false, unique = true, length = CoreDefaults.VARCHAR_BASIC)
    private String name;

    @Column(name = "x_coordinate", nullable = true)
    private double xCoordinate;

    @Column(name = "y_coordinate", nullable = true)
    private double yCoordinate;

    @Column(name = "z_coordinate", nullable = true)
    private double zCoordinate;

    @Column(name = "r_coordinate", nullable = true)
    private double rCoordinate;

    @Column(name = "latitude", nullable = true)
    private double latitude;

    @Column(name = "longitude", nullable = true)
    private double longitude;

    @Column (nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private ZonedDateTime createdAt;

    @Column (nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private ZonedDateTime updatedAt;

    public ChoreographerWorkspace() {}

    public ChoreographerWorkspace(String name, double xCoordinate, double yCoordinate, double zCoordinate, double rCoordinate) {
        this.name = name;
        this.xCoordinate = xCoordinate;
        this.yCoordinate = yCoordinate;
        this.zCoordinate = zCoordinate;
        this.rCoordinate = rCoordinate;
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

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getXCoordinate() { return xCoordinate; }
    public void setXCoordinate(double xCoordinate) { this.xCoordinate = xCoordinate; }

    public double getYCoordinate() { return yCoordinate; }
    public void setYCoordinate(double yCoordinate) { this.yCoordinate = yCoordinate; }

    public double getZCoordinate() { return zCoordinate; }
    public void setZCoordinate(double zCoordinate) { this.zCoordinate = zCoordinate; }

    public double getRCoordinate() { return rCoordinate; }
    public void setRCoordinate(double rCoordinate) { this.rCoordinate = rCoordinate; }

    public ZonedDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(ZonedDateTime createdAt) { this.createdAt = createdAt; }

    public ZonedDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(ZonedDateTime updatedAt) { this.updatedAt = updatedAt; }
}
