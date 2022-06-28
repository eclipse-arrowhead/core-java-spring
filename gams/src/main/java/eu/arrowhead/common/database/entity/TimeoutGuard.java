package eu.arrowhead.common.database.entity;

import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.StringJoiner;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "gams_timeout_guard")
public class TimeoutGuard extends ConfigurationEntity {

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sensorId", referencedColumnName = "id", nullable = false)
    protected Sensor sensor;

    @Column(nullable = false, unique = false)
    private Long timeValue = 24L;

    @Column(nullable = false, unique = false)
    @Enumerated(EnumType.STRING)
    private ChronoUnit timeUnit = ChronoUnit.HOURS;

    public TimeoutGuard() { super(); }

    public TimeoutGuard(final Sensor sensor, final Long timeValue, final ChronoUnit timeUnit) {
        this.sensor = sensor;
        this.timeValue = timeValue;
        this.timeUnit = timeUnit;
    }

    public Sensor getSensor() {
        return sensor;
    }

    public void setSensor(final Sensor sensor) {
        this.sensor = sensor;
    }

    public Long getTimeValue() {
        return timeValue;
    }

    public void setTimeValue(final Long timeValue) {
        this.timeValue = timeValue;
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
        if (!(o instanceof TimeoutGuard)) { return false; }
        if (!super.equals(o)) { return false; }
        final TimeoutGuard that = (TimeoutGuard) o;
        return Objects.equals(sensor, that.sensor) &&
                Objects.equals(timeValue, that.timeValue) &&
                timeUnit == that.timeUnit;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), sensor, timeValue, timeUnit);
    }

    public String shortToString() {
        return new StringJoiner(", ", getClass().getSimpleName() + "[", "]")
                .add("id=" + id)
                .add("sensor=" + sensor.getName())
                .add("value=" + timeValue + " " + timeUnit)
                .toString();
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", getClass().getSimpleName() + "[", "]")
                .add("sensor=" + sensor.shortToString())
                .add("value=" + timeValue + " " + timeUnit)
                .add("id=" + id)
                .add("createdAt=" + createdAt)
                .add("updatedAt=" + updatedAt)
                .add("uid=" + uid)
                .toString();
    }
}
