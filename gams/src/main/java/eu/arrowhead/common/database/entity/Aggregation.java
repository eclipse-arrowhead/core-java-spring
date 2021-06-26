package eu.arrowhead.common.database.entity;

import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.StringJoiner;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import eu.arrowhead.core.gams.dto.AggregationType;

@Entity
@Table(name = "gams_aggregation")
@Inheritance(strategy = InheritanceType.JOINED)
public class Aggregation extends ConfigurationEntity {

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sensorId", referencedColumnName = "id", nullable = false)
    private Sensor sensor;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 8)
    private AggregationType type;

    @Column(name = "quantity", nullable = true)
    private Integer quantity;

    @Column(name = "validity", nullable = true)
    private Integer validity;

    @Column(name = "validityUnit", nullable = true)
    @Enumerated(EnumType.STRING)
    private ChronoUnit validityTimeUnit = ChronoUnit.SECONDS;

    public Aggregation() { super(); }

    public Aggregation(final Sensor sensor, final AggregationType type) {
        super();
        this.sensor = sensor;
        this.type = type;
    }

    public Aggregation(final Sensor sensor, final AggregationType type, final Integer quantity) {
        super();
        this.sensor = sensor;
        this.type = type;
        this.quantity = quantity;
    }

    public Aggregation(final Sensor sensor, final AggregationType type, final Integer validity, final ChronoUnit validityTimeUnit) {
        super();
        this.sensor = sensor;
        this.type = type;
        this.validity = validity;
        this.validityTimeUnit = validityTimeUnit;
    }

    public Aggregation(final Sensor sensor, final AggregationType type, final Integer quantity, final Integer validity, final ChronoUnit validityTimeUnit) {
        super();
        this.sensor = sensor;
        this.type = type;
        this.quantity = quantity;
        this.validity = validity;
        this.validityTimeUnit = validityTimeUnit;
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

    public Integer getValidity() {
        return validity;
    }

    public void setValidity(final Integer validity) {
        this.validity = validity;
    }

    public ChronoUnit getValidityTimeUnit() {
        return validityTimeUnit;
    }

    public void setValidityTimeUnit(final ChronoUnit validityTimeUnit) {
        this.validityTimeUnit = validityTimeUnit;
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
                .add("validity=" + validity + " " + validityTimeUnit)
                .toString();
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Aggregation.class.getSimpleName() + "[", "]")
                .add("type=" + type)
                .add("sensor=" + sensor)
                .add("quantity=" + quantity)
                .add("validity=" + validity + " " + validityTimeUnit)
                .add("id=" + id)
                .add("createdAt=" + createdAt)
                .add("updatedAt=" + updatedAt)
                .add("uid=" + uid)
                .toString();
    }
}
