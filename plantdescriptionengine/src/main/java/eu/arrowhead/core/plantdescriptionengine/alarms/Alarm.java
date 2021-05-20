package eu.arrowhead.core.plantdescriptionengine.alarms;

import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitor.dto.PdeAlarmDto;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Internal representation of a PDE Alarm.
 */
public class Alarm {

    // Integer for storing the next alarm ID to be used:
    private static final AtomicInteger nextId = new AtomicInteger();
    private static final String unknownId = "Unknown";

    private final int id;
    private final String systemName;
    private final String systemId;
    private final AlarmCause cause;

    private final Map<String, String> metadata;
    private final Instant raisedAt;
    private boolean acknowledged;
    private Instant updatedAt;
    private Instant clearedAt;
    private Instant acknowledgedAt;

    Alarm(final String systemId, final String systemName, final Map<String, String> metadata, final AlarmCause cause) {

        Objects.requireNonNull(cause, "Expected an alarm cause.");
        Objects.requireNonNull(metadata, "Expected metadata.");

        this.systemId = systemId;
        this.systemName = systemName;
        this.metadata = metadata;
        this.cause = cause;

        id = nextId.getAndIncrement();
        acknowledged = false;
        raisedAt = Instant.now();
        updatedAt = raisedAt;
    }

    /**
     * @param systemId   ID of a system or null.
     * @param systemName Name of a system or null.
     * @param metadata   A system's metadata, or null.
     * @param cause      An alarm cause.
     * @return True if the provided arguments match the data stored in this
     * alarm instance.
     */
    public boolean matches(
        final String systemId,
        final String systemName,
        final Map<String, String> metadata,
        final AlarmCause cause
    ) {
        Objects.requireNonNull(cause, "Expected alarm cause.");
        final Map<String, String> nonNullMetadata = metadata == null ? Collections.emptyMap() : metadata;
        return cause == this.cause &&
            Objects.equals(systemId, this.systemId) &&
            Objects.equals(systemName, this.systemName) &&
            Objects.equals(nonNullMetadata, this.metadata);
    }

    /**
     * @return A PdeAlarm DTO based on this alarm data.
     */
    public PdeAlarmDto toPdeAlarm() {
        final AlarmSeverity severity = (clearedAt == null) ? AlarmSeverity.WARNING : AlarmSeverity.CLEARED;
        final String systemId = this.systemId == null ? unknownId : this.systemId;
        return new PdeAlarmDto.Builder()
            .id(id)
            .systemId(systemId)
            .systemName(systemName)
            .acknowledged(acknowledged)
            .severity(severity.toString().toLowerCase())
            .description(getDescription())
            .raisedAt(raisedAt)
            .updatedAt(updatedAt)
            .clearedAt(clearedAt)
            .acknowledgedAt(acknowledgedAt)
            .build();
    }

    /**
     * @return A description of the alarm.
     */
    protected String getDescription() {
        final String identifier = (systemName == null) ? "System with ID '" + systemId + "'" : "System named '" + systemName + "'";
        return cause.getDescription(identifier);
    }

    /**
     * Marks the alarm as acknowledged, noting the time at which this is done.
     */
    public void acknowledge() {
        this.acknowledged = true;
        acknowledgedAt = Instant.now();
        updatedAt = acknowledgedAt;
    }

    /**
     * Marks the alarm as cleared, and notes the time at which this is done.
     */
    public void setCleared() {
        clearedAt = Instant.now();
        updatedAt = clearedAt;
    }

    public int getId() {
        return id;
    }

    public String getSystemName() {
        return systemName;
    }

    public String getSystemId() {
        return systemId;
    }

    public AlarmCause getCause() {
        return cause;
    }

    /**
     * @return Metadata of the system that this alarm refers to. If no metadata
     * is present, an empty Map is returned; never null.
     */
    public Map<String, String> getMetadata() {
        return metadata;
    }
}