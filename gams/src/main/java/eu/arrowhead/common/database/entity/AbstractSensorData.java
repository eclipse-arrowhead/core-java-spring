package eu.arrowhead.common.database.entity;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.StringJoiner;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.Table;

@Entity
@Table(name = "gams_sensor_data")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class AbstractSensorData<T> extends ProcessableEntity {

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sensorId", referencedColumnName = "id", nullable = false)
    protected Sensor sensor;

    @Column(nullable = true, updatable = false, length = 16)
    protected String address;

    public AbstractSensorData() {
        super();
    }

    public AbstractSensorData(final Sensor sensor, final ZonedDateTime createdAt) {
        this(sensor, null, createdAt);
    }

    public AbstractSensorData(final Sensor sensor, final String address, final ZonedDateTime createdAt) {
        super(createdAt);
        this.sensor = sensor;
        this.address = address;
        this.validTill = sensor.calculateValidityTime(createdAt);
    }

    //-------------------------------------------------------------------------------------------------
    @PrePersist
    public void onCreate() {
        super.onCreate();
        Objects.requireNonNull(sensor,"Sensor must not be null");
        this.validTill = sensor.calculateValidityTime(createdAt);
    }

    //-------------------------------------------------------------------------------------------------

    public abstract T getData();

    public Sensor getSensor() {
        return sensor;
    }

    public void setSensor(Sensor sensor) {
        this.sensor = sensor;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(final String address) {
        this.address = address;
    }

    //-------------------------------------------------------------------------------------------------

    @Override
    public boolean equals(final Object o) {
        if (this == o) { return true; }
        if (!(o instanceof AbstractSensorData)) { return false; }
        if (!super.equals(o)) { return false; }
        final AbstractSensorData<?> that = (AbstractSensorData<?>) o;
        return Objects.equals(sensor, that.sensor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), sensor);
    }

    public String shortToString() {
        return new StringJoiner(", ", getClass().getSimpleName() + "[", "]")
                .add("id=" + id)
                .add("state=" + state)
                .add("data=" + getData())
                .add("sensor=" + sensor.shortToString())
                .toString();
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", getClass().getSimpleName() + "[", "]")
                .add("id=" + id)
                .add("createdAt=" + createdAt)
                .add("data=" + getData())
                .add("state=" + state)
                .add("validTill=" + validTill)
                .add("sensor=" + sensor.shortToString())
                .toString();
    }
}