package eu.arrowhead.common.database.entity;

import java.time.temporal.ChronoUnit;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

import eu.arrowhead.core.gams.dto.AggregationType;

@Entity
@Table(name = "gams_counting_aggregation")
public class CountingAggregation extends Aggregation {

    @Column(name = "timescale", nullable = true)
    private Integer timescale;

    @Column(name = "timescaleUnit", nullable = true)
    @Enumerated(EnumType.STRING)
    private ChronoUnit timescaleUnit = ChronoUnit.HOURS;

    public CountingAggregation() { super(); }


    public CountingAggregation(final Sensor sensor, final Integer timescale,
                               final ChronoUnit timescaleUnit) {
        super(sensor, AggregationType.COUNT);
        this.timescale = timescale;
        this.timescaleUnit = timescaleUnit;
    }

    public CountingAggregation(final Sensor sensor, final Integer quantity, final Integer timescale,
                               final ChronoUnit timescaleUnit) {
        super(sensor, AggregationType.COUNT, quantity);
        this.timescale = timescale;
        this.timescaleUnit = timescaleUnit;
    }

    public CountingAggregation(final Sensor sensor, final Integer validity,
                               final ChronoUnit validityTimeUnit, final Integer timescale, final ChronoUnit timescaleUnit) {
        super(sensor, AggregationType.COUNT, validity, validityTimeUnit);
        this.timescale = timescale;
        this.timescaleUnit = timescaleUnit;
    }

    public CountingAggregation(final Sensor sensor, final Integer quantity, final Integer validity,
                               final ChronoUnit validityTimeUnit, final Integer timescale, final ChronoUnit timescaleUnit) {
        super(sensor, AggregationType.COUNT, quantity, validity, validityTimeUnit);
        this.timescale = timescale;
        this.timescaleUnit = timescaleUnit;
    }

    public Integer getTimescale() {
        return timescale;
    }

    public void setTimescale(final Integer timescale) {
        this.timescale = timescale;
    }

    public ChronoUnit getTimescaleUnit() {
        return timescaleUnit;
    }

    public void setTimescaleUnit(final ChronoUnit timescaleUnit) {
        this.timescaleUnit = timescaleUnit;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) { return true; }
        if (!(o instanceof CountingAggregation)) { return false; }
        if (!super.equals(o)) { return false; }
        final CountingAggregation that = (CountingAggregation) o;
        return Objects.equals(timescale, that.timescale) &&
                timescaleUnit == that.timescaleUnit;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), timescale, timescaleUnit);
    }
}
