package eu.arrowhead.common.database.entity;

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

import eu.arrowhead.core.gams.dto.AggregationType;

@Entity
@Table(name = "gams_aggregation")
public class Aggregation extends ConfigurationEntity {

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sensorId", referencedColumnName = "id", nullable = false)
    private Sensor sensor;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 8)
    private AggregationType type;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    public Aggregation() { super(); }

    public Aggregation(final Sensor sensor, final Integer quantity, final AggregationType type) {
        super();
        this.sensor = sensor;
        this.quantity = quantity;
        this.type = type;
    }

    public Sensor getSensor() {
        return sensor;
    }

    public void setSensor(final Sensor sensor) {
        this.sensor = sensor;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(final Integer quantity) {
        this.quantity = quantity;
    }

    public AggregationType getType() {
        return type;
    }

    public void setType(final AggregationType type) {
        this.type = type;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) { return true; }
        if (!(o instanceof Aggregation)) { return false; }
        if (!super.equals(o)) { return false; }
        final Aggregation that = (Aggregation) o;
        return Objects.equals(sensor, that.sensor) &&
                Objects.equals(quantity, that.quantity) &&
                type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), sensor, quantity, type);
    }

    public String shortToString() {
        return new StringJoiner(", ", Aggregation.class.getSimpleName() + "[", "]")
                .add("id=" + id)
                .add("sensor=" + sensor.shortToString())
                .add("type=" + type)
                .add("quantity=" + quantity)
                .toString();
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Aggregation.class.getSimpleName() + "[", "]")
                .add("type=" + type)
                .add("sensor=" + sensor)
                .add("quantity=" + quantity)
                .add("id=" + id)
                .add("createdAt=" + createdAt)
                .add("updatedAt=" + updatedAt)
                .add("uid=" + uid)
                .toString();
    }
}
