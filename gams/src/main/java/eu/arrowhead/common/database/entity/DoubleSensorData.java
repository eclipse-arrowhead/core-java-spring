package eu.arrowhead.common.database.entity;

import java.time.ZonedDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

@Entity
@Table(name = "gams_sensor_data_double")
@PrimaryKeyJoinColumn(name = "id", referencedColumnName = "id")
public class DoubleSensorData extends AbstractSensorData<Double> {

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

}
