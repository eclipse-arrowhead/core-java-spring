package eu.arrowhead.core.gams.database.entities;

import eu.arrowhead.core.gams.rest.dto.SensorType;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.UUID;

@Entity
@Table(name = "gams_sensor",
        uniqueConstraints = @UniqueConstraint(name = "u_sensor_name", columnNames = {"instance", "name"}))
public class Sensor {

    //=================================================================================================
    // members

    public static final List<String> SORTABLE_FIELDS_BY = List.of("id", "updatedAt", "createdAt", "uid", "name", "type"); //NOSONAR

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "instanceId", referencedColumnName = "id", nullable = false)
    private GamsInstance instance;

    @Column(nullable = false, unique = false, length = 32)
    private String name;

    @Column(nullable = false, unique = true)
    private UUID uid;

    @Column(nullable = false, unique = false, length = 32)
    @Enumerated(EnumType.STRING)
    private SensorType type;

    @Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private ZonedDateTime createdAt;

    @Column(nullable = false, updatable = true, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private ZonedDateTime updatedAt;


    //-------------------------------------------------------------------------------------------------
    public Sensor() {
        super();
    }

    public Sensor(final GamsInstance instance, final String name, final SensorType type) {
        this.instance = instance;
        this.name = name;
        this.type = type;
    }

    public Sensor(final GamsInstance instance, final String name, final UUID uid, final SensorType type) {
        this.instance = instance;
        this.name = name;
        this.uid = uid;
        this.type = type;
    }

    //-------------------------------------------------------------------------------------------------
    @PrePersist
    public void onCreate() {
        this.createdAt = ZonedDateTime.now();
        if(Objects.isNull(uid)) uid = UUID.randomUUID();
        this.updatedAt = this.createdAt;
    }

    //-------------------------------------------------------------------------------------------------
    @PreUpdate
    public void onUpdate() {
        this.updatedAt = ZonedDateTime.now();
    }

    //-------------------------------------------------------------------------------------------------
    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public GamsInstance getInstance() {
        return instance;
    }

    public void setInstance(GamsInstance instance) {
        this.instance = instance;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public UUID getUid() {
        return uid;
    }

    public void setUid(final UUID uid) {
        this.uid = uid;
    }

    public SensorType getType() {
        return type;
    }

    public void setType(SensorType type) {
        this.type = type;
    }

    public String getUidAsString() {
        return (Objects.nonNull(uid) ? uid.toString() : null);
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(final ZonedDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public ZonedDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(final ZonedDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    //-------------------------------------------------------------------------------------------------

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Sensor sensor = (Sensor) o;
        return id == sensor.id &&
                Objects.equals(instance, sensor.instance) &&
                Objects.equals(name, sensor.name) &&
                Objects.equals(uid, sensor.uid) &&
                Objects.equals(type, sensor.type) &&
                Objects.equals(createdAt, sensor.createdAt) &&
                Objects.equals(updatedAt, sensor.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, instance, name, uid, type, createdAt, updatedAt);
    }


    //-------------------------------------------------------------------------------------------------

    @Override
    public String toString() {
        return new StringJoiner(", ", Sensor.class.getSimpleName() + "[", "]")
                .add("id=" + id)
                .add("instance='" + instance + "'")
                .add("name='" + name + "'")
                .add("uid=" + uid)
                .add("type=" + type)
                .add("createdAt=" + createdAt)
                .add("updatedAt=" + updatedAt)
                .toString();
    }
}