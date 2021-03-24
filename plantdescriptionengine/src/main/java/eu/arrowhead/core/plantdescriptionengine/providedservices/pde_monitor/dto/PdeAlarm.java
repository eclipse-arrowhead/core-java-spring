package eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitor.dto;

import se.arkalix.dto.DtoReadableAs;
import se.arkalix.dto.DtoToString;
import se.arkalix.dto.DtoWritableAs;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static se.arkalix.dto.DtoEncoding.JSON;

/**
 * Data Transfer Object (DTO) interface for PDE alarms.
 */
@DtoReadableAs(JSON)
@DtoWritableAs(JSON)
@DtoToString
public interface PdeAlarm {

    Comparator<PdeAlarm> ID_COMPARATOR = Comparator.comparingInt(PdeAlarm::id);

    Comparator<PdeAlarm> RAISED_AT_COMPARATOR = Comparator.comparing(PdeAlarm::raisedAt);

    Comparator<PdeAlarm> UPDATED_AT_COMPARATOR = Comparator.comparing(PdeAlarm::updatedAt);

    Comparator<PdeAlarm> CLEARED_AT_COMPARATOR = (a1, a2) -> {
        Optional<Instant> cleared1 = a1.clearedAt();
        Optional<Instant> cleared2 = a2.clearedAt();

        if (cleared1.isEmpty()) {
            return cleared2.isEmpty() ? 0 : 1;
        }

        if (cleared2.isEmpty()) {
            return -1;
        }

        return cleared1.get().compareTo(cleared2.get());
    };

    /**
     * Filters out cleared/uncleared alarms from the given list.
     *
     * @param alarms       A list of PDE alarms.
     * @param acknowledged If true, unacknowledged alarms are removed. If false,
     *                     acknowledged alarms are removed.
     */
    static void filterAcknowledged(List<? extends PdeAlarm> alarms, boolean acknowledged) {
        if (acknowledged) {
            alarms.removeIf(alarm -> !alarm.acknowledged());
        } else {
            alarms.removeIf(PdeAlarm::acknowledged);
        }
    }

    /**
     * Filters the given list of alarms by system name.
     *
     * @param alarms     A list of PDE alarms.
     * @param systemName Only alarms with this system name are kept in the list.
     */
    static void filterBySystemName(List<? extends PdeAlarm> alarms, String systemName) {
        alarms.removeIf(alarm -> alarm.systemName().isEmpty() || !alarm.systemName().get().equals(systemName));
    }

    /**
     * Filters the given list of alarms based on their severity.
     *
     * @param alarms   A list of PDE alarms.
     * @param severity Only alarms with this severity are kept in the list.
     */
    static void filterBySeverity(List<? extends PdeAlarm> alarms, String severity) {
        if (severity.equals("not_cleared")) {
            alarms.removeIf(alarm -> alarm.severity().equals("cleared"));
            return;
        }
        alarms.removeIf(alarm -> !alarm.severity().equals(severity));
    }

    static void sort(List<? extends PdeAlarm> alarms, String sortField, boolean sortAscending) {
        Comparator<PdeAlarm> comparator;
        switch (sortField) {
            case "id":
                comparator = ID_COMPARATOR;
                break;
            case "raisedAt":
                comparator = RAISED_AT_COMPARATOR;
                break;
            case "updatedAt":
                comparator = UPDATED_AT_COMPARATOR;
                break;
            case "clearedAt":
                comparator = CLEARED_AT_COMPARATOR;
                break;
            default:
                throw new IllegalArgumentException("'" + sortField + "' is not a valid sort field for PDE Alarms.");

        }

        if (sortAscending) {
            alarms.sort(comparator);
        } else {
            alarms.sort(comparator.reversed());
        }
    }

    int id();

    Optional<String> systemName();

    String systemId();

    boolean acknowledged();

    String severity();

    String description();

    Instant raisedAt();

    Instant updatedAt();

    Optional<Instant> clearedAt();

    Optional<Instant> acknowledgedAt();
}