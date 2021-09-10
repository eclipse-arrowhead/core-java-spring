package eu.arrowhead.common.database.entity;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
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

import eu.arrowhead.core.gams.dto.EventType;
import eu.arrowhead.core.gams.dto.GamsPhase;
import org.apache.logging.log4j.Marker;
import org.springframework.util.Assert;


@Entity
@Table(name = "gams_event")
public class Event extends ProcessableEntity {

    @Column(nullable = false, updatable = false)
    protected ZonedDateTime validFrom;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sensorId", referencedColumnName = "id", nullable = false)
    private Sensor sensor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false, length = 16)
    private GamsPhase phase;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false, length = 16)
    private EventType type;

    @Column(nullable = true, updatable = false, length = 64)
    private String source;

    @Column(nullable = true, updatable = false, length = 64)
    private String data;
    //-------------------------------------------------------------------------------------------------

    public Event() {
        this(null, null, null);
    }

    public Event(final Sensor sensor, final GamsPhase phase, final EventType type) {
        this(sensor, phase, type, 0, ChronoUnit.SECONDS);
    }

    public Event(final Sensor sensor, final GamsPhase phase, final EventType type, final long startDelay, final ChronoUnit timeUnit) {
        this(sensor, phase, type, startDelay, timeUnit, DEFAULT_RETENTION_TIME, DEFAULT_RETENTION_TIME_UNIT);
    }

    public Event(final Sensor sensor, final GamsPhase phase, final EventType type,
                 final long startDelay, final ChronoUnit startDelayUnit,
                 final long validityDelay, final ChronoUnit validityUnit) {
        super();
        this.sensor = sensor;
        this.phase = phase;
        this.type = type;
        updateValidityTimes(startDelay, startDelayUnit, validityDelay, validityUnit);
    }

    //-------------------------------------------------------------------------------------------------

    public ZonedDateTime getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(final ZonedDateTime validFrom) {
        this.validFrom = validFrom;
    }

    public GamsPhase getPhase() {
        return phase;
    }

    public void setPhase(final GamsPhase phase) {
        this.phase = phase;
    }

    public EventType getType() {
        return type;
    }

    public void setType(final EventType type) {
        this.type = type;
    }

    public Sensor getSensor() {
        return sensor;
    }

    public void setSensor(final Sensor sensor) {
        this.sensor = sensor;
    }

    public String getSource() {
        return source;
    }

    public void setSource(final String source) {
        this.source = source;
    }

    public String getData() {
        return data;
    }

    public void setData(final String data) {
        this.data = data;
    }

    public Marker getMarker() {
        return getPhase().getMarker();
    }

    // ----------------------------------------------------------------------------------------------
    @Override
    public boolean equals(final Object o) {
        if (this == o) { return true; }
        if (!(o instanceof Event)) { return false; }
        if (!super.equals(o)) { return false; }
        final Event event = (Event) o;
        return phase == event.phase &&
                type == event.type &&
                Objects.equals(sensor, event.sensor) &&
                Objects.equals(source, event.source);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), phase, type, sensor, source);
    }

    public String shortToString() {
        return new StringJoiner(", ", Event.class.getSimpleName() + "[", "]")
                .add("id=" + id)
                .add("phase=" + phase)
                .add("type=" + type)
                .add("state=" + state)
                .add("data='" + data + "'")
                .add("sensor=" + sensor.shortToString())
                .toString();
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Event.class.getSimpleName() + "[", "]")
                .add("id=" + id)
                .add("phase=" + phase)
                .add("type=" + type)
                .add("state=" + state)
                .add("sensor=" + sensor.shortToString())
                .add("source='" + source + "'")
                .add("data='" + data + "'")
                .add("createdAt=" + createdAt)
                .add("validFrom=" + validFrom)
                .add("validTill=" + validTill)
                .toString();
    }

    // ----------------------------------------------------------------------------------------------
    protected void updateValidityTimes(final long startDelay, final ChronoUnit startDelayUnit, final long validity, final ChronoUnit validityUnit) {
        Assert.isTrue(startDelay >= 0, "StartDelay must not be negative");
        Assert.notNull(startDelayUnit, "StartDelayTimeUnit must not be null");
        Assert.isTrue(validity >= 0, "Validity must not be negative");
        Assert.notNull(validityUnit, "ValidityTimeUnit must not be null");

        if (Objects.isNull(createdAt)) { this.createdAt = ZonedDateTime.now(); }
        if (Objects.isNull(validFrom)) { this.validFrom = calculateValidityTime(createdAt, startDelay, startDelayUnit); }
        if (Objects.isNull(validTill)) { this.validTill = calculateValidityTime(validFrom, validity, validityUnit); }
    }
}
