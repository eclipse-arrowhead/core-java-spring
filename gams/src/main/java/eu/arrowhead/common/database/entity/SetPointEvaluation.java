package eu.arrowhead.common.database.entity;

import java.util.Objects;
import java.util.StringJoiner;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import eu.arrowhead.core.gams.dto.AnalysisType;

@Entity
@Table(name = "gams_setpoint_analysis")
public class SetPointEvaluation extends AbstractEvaluation {

    @Column(nullable = false, length = 16)
    private String lowerSetPoint;

    @Column(nullable = true, length = 16)
    private String upperSetPoint;

    @Column(nullable = false)
    private Boolean inverse;

    public SetPointEvaluation() {
        super();
    }

    public SetPointEvaluation(final Sensor sensor, final String knowledgeName,
                              final String lowerSetPoint, final String upperSetPoint,
                              final Boolean inverse) {
        super(sensor, knowledgeName, AnalysisType.SET_POINT);
        this.lowerSetPoint = lowerSetPoint;
        this.upperSetPoint = upperSetPoint;
        this.inverse = inverse;
    }

    public String getLowerSetPoint() {
        return lowerSetPoint;
    }

    public void setLowerSetPoint(final String lowerSetPoint) {
        this.lowerSetPoint = lowerSetPoint;
    }

    public String getUpperSetPoint() {
        return upperSetPoint;
    }

    public void setUpperSetPoint(final String upperSetPoint) {
        this.upperSetPoint = upperSetPoint;
    }

    public Boolean getInverse() {
        return inverse;
    }

    public void setInverse(final Boolean inverse) {
        this.inverse = inverse;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) { return true; }
        if (!(o instanceof SetPointEvaluation)) { return false; }
        if (!super.equals(o)) { return false; }
        final SetPointEvaluation that = (SetPointEvaluation) o;
        return Objects.equals(lowerSetPoint, that.lowerSetPoint) &&
                Objects.equals(upperSetPoint, that.upperSetPoint) &&
                Objects.equals(inverse, that.inverse);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), lowerSetPoint, upperSetPoint, inverse);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", getClass().getSimpleName() + "[", "]")
                .add("id=" + id)
                .add("createdAt=" + createdAt)
                .add("updatedAt=" + updatedAt)
                .add("uid=" + uid)
                .add("type=" + type)
                .add("lowerSetPoint='" + lowerSetPoint + "'")
                .add("upperSetPoint='" + upperSetPoint + "'")
                .add("inverse=" + inverse)
                .add("sensor=" + sensor)
                .toString();
    }

    public String shortToString() {
        return new StringJoiner(", ", getClass().getSimpleName() + "[", "]")
                .add("id='" + id + "'")
                .add("sensor='" + sensor.getName() + "'")
                .add("lowerSetPoint='" + lowerSetPoint + "'")
                .add("upperSetPoint='" + upperSetPoint + "'")
                .add("inverse=" + inverse)
                .toString();
    }
}
