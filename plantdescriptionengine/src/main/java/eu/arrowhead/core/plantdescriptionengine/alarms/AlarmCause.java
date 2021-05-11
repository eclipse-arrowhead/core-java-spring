package eu.arrowhead.core.plantdescriptionengine.alarms;

import java.util.Objects;

public enum AlarmCause {
    NOT_MONITORABLE("cannot be monitored."),
    NO_PING_RESPONSE("does not respond when pinged."),
    SYSTEM_NOT_REGISTERED("cannot be found in the Service Registry."),
    SYSTEM_NOT_IN_DESCRIPTION("is not present in the active Plant Description."),
    MULTIPLE_MATCHES("cannot be uniquely identified in the Service Registry.");

    private final String description;

    AlarmCause(final String description) {
        this.description = description;
    }

    public String getDescription(final String systemIdentifier) {
        Objects.requireNonNull(systemIdentifier, "Expected system identifier.");
        return systemIdentifier + " " + description;
    }
}