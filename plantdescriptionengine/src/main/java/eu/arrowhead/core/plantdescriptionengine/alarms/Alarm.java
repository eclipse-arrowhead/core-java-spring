package eu.arrowhead.core.plantdescriptionengine.alarms;

import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitor.dto.PdeAlarmBuilder;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitor.dto.PdeAlarmDto;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Internal representation of a PDE Alarm.
 */
public class Alarm {

    // Integer for storing the next alarm ID to be used:
    private final static AtomicInteger nextId = new AtomicInteger();
    public final String systemName;
    public final String systemId;
    public final Map<String, String> metadata;
    public final AlarmCause cause;
    final int id;
    final Instant raisedAt;
    boolean acknowledged;
    Instant updatedAt;
    Instant clearedAt;
    Instant acknowledgedAt;

    Alarm(String systemId, String systemName, Map<String, String> metadata, AlarmCause cause) {

        Objects.requireNonNull(cause, "Expected an alarm cause.");

        this.id = nextId.getAndIncrement();
        this.systemId = systemId;
        this.systemName = systemName;
        this.metadata = metadata;
        this.cause = cause;
        this.acknowledged = false;
        this.acknowledgedAt = null;

        raisedAt = Instant.now();
        updatedAt = Instant.now();
        clearedAt = null;
    }

    protected String description() {
        String identifier = (systemName == null) ? "System with ID '" + systemId + "'" : "System named '" + systemName + "'";
        return cause.getDescription(identifier);
    }

    public boolean matches(String systemId, String systemName, Map<String, String> metadata, AlarmCause cause) {
        return cause == this.cause &&
            Objects.equals(systemId, this.systemId) &&
            Objects.equals(systemName, this.systemName) &&
            Objects.equals(metadata, this.metadata);
    }

    /**
     * @return A PdeAlarm DTO based on this alarm data.
     */
    public PdeAlarmDto toPdeAlarm() {
        AlarmSeverity severity = (clearedAt == null) ? AlarmSeverity.warning : AlarmSeverity.cleared;
        String systemId = this.systemId == null ? "Unknown" : this.systemId;
        return new PdeAlarmBuilder()
            .id(id)
            .systemId(systemId)
            .systemName(systemName)
            .acknowledged(acknowledged)
            .severity(severity.toString())
            .description(description())
            .raisedAt(raisedAt)
            .updatedAt(updatedAt)
            .clearedAt(clearedAt)
            .acknowledgedAt(acknowledgedAt)
            .build();
    }
}