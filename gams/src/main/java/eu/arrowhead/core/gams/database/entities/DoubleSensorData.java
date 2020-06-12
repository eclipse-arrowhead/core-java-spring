package eu.arrowhead.core.gams.database.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.StringJoiner;

@Entity
@Table(name = "gams_sensor_data_double")
@PrimaryKeyJoinColumn(name = "id", referencedColumnName = "id")
public class DoubleSensorData extends AbstractSensorData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false, updatable = false)
    private Double data;

    public DoubleSensorData() {
        super();
    }

    public DoubleSensorData(final Sensor sensor, final ZonedDateTime timestamp, final Double data) {
        super(sensor, timestamp);
        this.data = data;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public void setId(long id) {
        this.id = id;
    }

    public Double getData() {
        return data;
    }

    public void setData(Double data) {
        this.data = data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        DoubleSensorData that = (DoubleSensorData) o;
        return id == that.id &&
                Objects.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id, data);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", DoubleSensorData.class.getSimpleName() + "[", "]")
                .add("id=" + id)
                .add("sensor='" + super.getSensor() + "'")
                .add("timestamp='" + super.getTimestamp() + "'")
                .add("data='" + data + "'")
                .toString();
    }
}
