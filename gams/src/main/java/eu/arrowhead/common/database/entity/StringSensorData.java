package eu.arrowhead.common.database.entity;

import java.time.ZonedDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

@Entity
@Table(name = "gams_sensor_data_string")
@PrimaryKeyJoinColumn(name = "id", referencedColumnName = "id")
public class StringSensorData extends AbstractSensorData<String> {

    @Column(nullable = false, updatable = false, length = 64)
    private String data;

    public StringSensorData() {
        super();
    }

    public StringSensorData(final Sensor sensor, final ZonedDateTime timestamp, final String data) {
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

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

}
