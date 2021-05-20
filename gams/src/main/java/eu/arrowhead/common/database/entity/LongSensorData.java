package eu.arrowhead.common.database.entity;

import java.time.ZonedDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

@Entity
@Table(name = "gams_sensor_data_long")
@PrimaryKeyJoinColumn(name = "id", referencedColumnName = "id")
public class LongSensorData extends AbstractSensorData<Long> {

    @Column(nullable = false, updatable = false)
    private Long data;

    public LongSensorData() {
        super();
    }

    public LongSensorData(final Sensor sensor, final ZonedDateTime timestamp, final Long data) {
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

    public Long getData() {
        return data;
    }

    public void setData(final Long data) {
        this.data = data;
    }

}
