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

    @Column(name = "relative_x_coordinate", nullable = false, precision = 10, scale = 2)
    private double relativeXCoordinate;

    @Column(name = "relative_y_coordinate", nullable = false, precision = 10, scale = 2)
    private double relativeYCoordinate;

    @Column(name = "relative_z_coordinate", nullable = false, precision = 10, scale = 2)
    private double relativeZCoordinate;

    @Column(name = "relative_r_coordinate", nullable = true, precision = 10, scale = 2)
    private double relativeRCoordinate;

    @Column (nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private ZonedDateTime createdAt;

    @Column (nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private ZonedDateTime updatedAt;

    public ChoreographerWorkspace() {}

    public ChoreographerWorkspace(String name, double relativeXCoordinate, double relativeYCoordinate, double relativeZCoordinate, double relativeRCoordinate) {
        this.name = name;
        this.relativeXCoordinate = relativeXCoordinate;
        this.relativeYCoordinate = relativeYCoordinate;
        this.relativeZCoordinate = relativeZCoordinate;
        this.relativeRCoordinate = relativeRCoordinate;
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

    public double getRelativeXCoordinate() { return relativeXCoordinate; }
    public void setRelativeXCoordinate(double relativeXCoordinate) { this.relativeXCoordinate = relativeXCoordinate; }

    public double getRelativeYCoordinate() { return relativeYCoordinate; }
    public void setRelativeYCoordinate(double relativeYCoordinate) { this.relativeYCoordinate = relativeYCoordinate; }

    public double getRelativeZCoordinate() { return relativeZCoordinate; }
    public void setRelativeZCoordinate(double relativeZCoordinate) { this.relativeZCoordinate = relativeZCoordinate; }

    public double getRelativeRCoordinate() { return relativeRCoordinate; }
    public void setRelativeRCoordinate(double relativeRCoordinate) { this.relativeRCoordinate = relativeRCoordinate; }

    public ZonedDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(ZonedDateTime createdAt) { this.createdAt = createdAt; }

    public ZonedDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(ZonedDateTime updatedAt) { this.updatedAt = updatedAt; }
}
