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

import eu.arrowhead.core.gams.dto.AnalysisType;

@Entity
@Table(name = "gams_analysis")
public abstract class AbstractAnalysis extends ConfigurationEntity {

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sensorId", referencedColumnName = "id", nullable = false)
    protected Sensor sensor;

    @Column(nullable = false, unique = false)
    private String knowledgeName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 8)
    protected AnalysisType type;

    public AbstractAnalysis() { super(); }

    public AbstractAnalysis(final Sensor sensor, final String knowledgeName, final AnalysisType type) {
        super();
        this.sensor = sensor;
        this.knowledgeName = knowledgeName;
        this.type = type;
    }

    public Sensor getSensor() {
        return sensor;
    }

    public void setSensor(final Sensor sensor) {
        this.sensor = sensor;
    }

    public AnalysisType getType() {
        return type;
    }

    public void setType(final AnalysisType type) {
        this.type = type;
    }

    public String getKnowledgeName() {
        return knowledgeName;
    }

    public void setKnowledgeName(final String knowledgeName) {
        this.knowledgeName = knowledgeName;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) { return true; }
        if (!(o instanceof AbstractAnalysis)) { return false; }
        if (!super.equals(o)) { return false; }
        final AbstractAnalysis that = (AbstractAnalysis) o;
        return Objects.equals(sensor, that.sensor) &&
                Objects.equals(knowledgeName, that.knowledgeName) &&
                type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), sensor, knowledgeName, type);
    }

    public String shortToString() {
        return new StringJoiner(", ", getClass().getSimpleName() + "[", "]")
                .add("id=" + id)
                .add("sensor=" + sensor.shortToString())
                .add("type=" + type)
                .toString();
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", getClass().getSimpleName() + "[", "]")
                .add("type=" + type)
                .add("sensor=" + sensor)
                .add("id=" + id)
                .add("createdAt=" + createdAt)
                .add("updatedAt=" + updatedAt)
                .add("uid=" + uid)
                .toString();
    }
}
