package eu.arrowhead.common.database.entity;

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

import eu.arrowhead.core.gams.dto.PolicyType;

@Entity
@Table(name = "gams_policy")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class AbstractPolicy extends ConfigurationEntity {

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sensorId", referencedColumnName = "id", nullable = false)
    protected Sensor sensor;

    @Column(nullable = false, unique = false)
    private String sourceKnowledge;

    @Column(nullable = false, unique = false)
    private String targetKnowledge;

    @Enumerated(EnumType.STRING)
    @Column(name = "policyType", nullable = false, length = 8)
    protected PolicyType type;

    public AbstractPolicy() { super(); }

    public AbstractPolicy(final Sensor sensor, final String sourceKnowledge, final String targetKnowledge, final PolicyType type) {
        super();
        this.sensor = sensor;
        this.sourceKnowledge = sourceKnowledge;
        this.targetKnowledge = targetKnowledge;
        this.type = type;
    }

    public Sensor getSensor() {
        return sensor;
    }

    public void setSensor(final Sensor sensor) {
        this.sensor = sensor;
    }

    public PolicyType getType() {
        return type;
    }

    public void setType(final PolicyType type) {
        this.type = type;
    }

    public String getSourceKnowledge() {
        return sourceKnowledge;
    }

    public void setSourceKnowledge(final String sourceKnowledge) {
        this.sourceKnowledge = sourceKnowledge;
    }

    public String getTargetKnowledge() {
        return targetKnowledge;
    }

    public void setTargetKnowledge(final String targetKnowledge) {
        this.targetKnowledge = targetKnowledge;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) { return true; }
        if (!(o instanceof AbstractPolicy)) { return false; }
        if (!super.equals(o)) { return false; }
        final AbstractPolicy that = (AbstractPolicy) o;
        return Objects.equals(sensor, that.sensor) &&
                Objects.equals(sourceKnowledge, that.sourceKnowledge) &&
                Objects.equals(targetKnowledge, that.targetKnowledge) &&
                type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), sensor, sourceKnowledge, targetKnowledge, type);
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
