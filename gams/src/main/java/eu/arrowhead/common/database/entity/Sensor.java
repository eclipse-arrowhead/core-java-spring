package eu.arrowhead.common.database.entity;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import eu.arrowhead.core.gams.rest.dto.SensorType;

@Entity
@Table(name = "gams_sensor",
        uniqueConstraints = @UniqueConstraint(name = "u_sensor_name", columnNames = {"instanceId", "name"}))
public class Sensor extends NamedEntity {

    public static final List<String> SORTABLE_FIELDS_BY = List.of("id", "updatedAt", "createdAt", "name", "address", "type"); //NOSONAR

    @Column(nullable = true, unique = false, length = 16)
    private String address;

    @Column(nullable = false, unique = false, length = 32)
    @Enumerated(EnumType.STRING)
    private SensorType type;

    @Column(nullable = false, unique = false)
    private Long retentionTime = 24L;

    @Column(nullable = false, unique = false, length = 16)
    @Enumerated(EnumType.STRING)
    private ChronoUnit timeUnit = ChronoUnit.HOURS;


    //-------------------------------------------------------------------------------------------------
    public Sensor() {
        super();
    }

    public Sensor(final GamsInstance instance, final String name, final SensorType type) {
        this(instance, name, type, null, null);
    }

    public Sensor(final GamsInstance instance, final String name, final UUID uid, final SensorType type) {
        this(instance, name, type, null, null);
        setUid(uid);
    }

    public Sensor(final GamsInstance instance, final String name, final SensorType type, final Long retentionTime, final ChronoUnit timeUnit) {
        this.instance = instance;
        this.name = name;
        this.type = type;
        if(Objects.nonNull(retentionTime)) {
            this.retentionTime = retentionTime;
        }
        if(Objects.nonNull(timeUnit)) {
            this.timeUnit = timeUnit;
        }
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(final String address) {
        this.address = address;
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

    public Long getRetentionTime() {
        return retentionTime;
    }

    public void setRetentionTime(final Long retentionTime) {
        this.retentionTime = retentionTime;
    }

    public ChronoUnit getTimeUnit() {
        return timeUnit;
    }

    public void setTimeUnit(final ChronoUnit timeUnit) {
        this.timeUnit = timeUnit;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        if (!super.equals(o)) { return false; }
        final Sensor sensor = (Sensor) o;
        return Objects.equals(instance, sensor.instance) &&
                Objects.equals(name, sensor.name) &&
                type == sensor.type &&
                Objects.equals(retentionTime, sensor.retentionTime) &&
                timeUnit == sensor.timeUnit;
    }

    //-------------------------------------------------------------------------------------------------


    //-------------------------------------------------------------------------------------------------

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), instance, name, type, retentionTime, timeUnit);
    }

    public String shortToString() {
        return new StringJoiner(", ", Sensor.class.getSimpleName() + "[", "]")
                .add("id=" + id)
                .add("instance='" + instance.getName() + "'")
                .add("name='" + name + "'")
                .add("type=" + type)
                .toString();
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Sensor.class.getSimpleName() + "[", "]")
                .add("id=" + id)
                .add("instance=" + instance.shortToString())
                .add("name='" + name + "'")
                .add("uid=" + uid)
                .add("type=" + type)
                .add("retentionTime=" + retentionTime)
                .add("timeUnit=" + timeUnit)
                .add("createdAt=" + createdAt)
                .add("updatedAt=" + updatedAt)
                .toString();
    }

    protected ZonedDateTime calculateValidityTime(final ZonedDateTime time) {
        if (Objects.isNull(time)) { return ZonedDateTime.now().plus(retentionTime, timeUnit); } else { return time.plus(retentionTime, timeUnit); }
    }
}