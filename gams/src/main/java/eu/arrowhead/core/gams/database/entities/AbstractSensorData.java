package eu.arrowhead.core.gams.database.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

@Entity
@Table(name = "gams_sensor_data")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class AbstractSensorData {

    //=================================================================================================
    // members

    public static final List<String> SORTABLE_FIELDS_BY = List.of("id", "timestamp", "type"); //NOSONAR

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sensorId", referencedColumnName = "id", nullable = false)
    private Sensor sensor;

    @Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private ZonedDateTime timestamp;

    //=================================================================================================
    // methods

    //-------------------------------------------------------------------------------------------------
    public AbstractSensorData() {}

    public AbstractSensorData(final Sensor sensor, final ZonedDateTime timestamp) {
        this.sensor = sensor;
        this.timestamp = timestamp;
    }

    //-------------------------------------------------------------------------------------------------
    @PrePersist
    public void onCreate() {
        this.timestamp = ZonedDateTime.now();
    }

    //-------------------------------------------------------------------------------------------------
    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public Sensor getSensor() {
        return sensor;
    }

    public void setSensor(Sensor sensor) {
        this.sensor = sensor;
    }

    public ZonedDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(ZonedDateTime timestamp) {
        this.timestamp = timestamp;
    }

    //-------------------------------------------------------------------------------------------------

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractSensorData that = (AbstractSensorData) o;
        return id == that.id &&
                Objects.equals(sensor, that.sensor) &&
                Objects.equals(timestamp, that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, sensor, timestamp);
    }

    //-------------------------------------------------------------------------------------------------


    @Override
    public String toString() {
        return new StringJoiner(", ", AbstractSensorData.class.getSimpleName() + "[", "]")
                .add("id=" + id)
                .add("sensor=" + sensor)
                .add("timestamp=" + timestamp)
                .toString();
    }

    public abstract Object getData();
}