package eu.arrowhead.common.database.entity;

import java.time.temporal.ChronoUnit;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

import eu.arrowhead.core.gams.dto.AnalysisType;

@Entity
@Table(name = "gams_counting_analysis")
public class CountingAnalysis extends AbstractAnalysis {

    @Column(nullable = false)
    private Integer count;

    @Column(nullable = false, unique = false, columnDefinition = "DEFAULT 24")
    private Long timeValue = 24L;

    @Column(nullable = false, unique = false, columnDefinition = "DEFAULT HOURS")
    @Enumerated(EnumType.STRING)
    private ChronoUnit timeUnit = ChronoUnit.HOURS;

    public CountingAnalysis() {
        super();
    }

    public CountingAnalysis(final Sensor sensor, final Integer count, final String knowledgeName, final Long timeValue, final ChronoUnit timeUnit) {
        super(sensor, knowledgeName, AnalysisType.COUNTING);
        this.count = count;
        this.timeValue = timeValue;
        this.timeUnit = timeUnit;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(final Integer count) {
        this.count = count;
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
        if (!(o instanceof CountingAnalysis)) { return false; }
        if (!super.equals(o)) { return false; }
        final CountingAnalysis that = (CountingAnalysis) o;
        return Objects.equals(count, that.count) &&
                Objects.equals(timeValue, that.timeValue) &&
                timeUnit == that.timeUnit;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), count, timeValue, timeUnit);
    }


}
