package eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitor.dto;

import eu.arrowhead.core.plantdescriptionengine.alarms.AlarmSeverity;
import se.arkalix.dto.DtoReadableAs;
import se.arkalix.dto.DtoToString;
import se.arkalix.dto.DtoWritableAs;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static se.arkalix.dto.DtoCodec.JSON;

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

        Objects.requireNonNull(a1, "Expected an alarm as first argument.");
        Objects.requireNonNull(a2, "Expected an alarm as second argument.");

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

    String NOT_CLEARED = "not_cleared";

    /**
     * Filters out cleared/uncleared alarms from the given list.
     *
     * @param alarms       A list of PDE alarms.
     * @param acknowledged If true, unacknowledged alarms are removed. If false,
     *                     acknowledged alarms are removed.
     */
    static void filterAcknowledged(final List<? extends PdeAlarm> alarms, final boolean acknowledged) {
        Objects.requireNonNull(alarms, "Expected alarms.");

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
     * @param systemName Only alarms with this system name are kept in the
     *                   list.
     */
    static void filterBySystemName(final List<? extends PdeAlarm> alarms, final String systemName) {
        Objects.requireNonNull(alarms, "Expected alarms.");
        Objects.requireNonNull(systemName, "Expected systemName.");

        alarms.removeIf(alarm -> alarm.systemName().isEmpty() || !alarm.systemName().get().equals(systemName));
    }

    /**
     * Filters the given list of alarms based on their severity.
     *
     * @param alarms   A list of PDE alarms.
     * @param severity Only alarms with this severity are kept in the list.
     */
    static void filterBySeverity(final List<? extends PdeAlarm> alarms, final String severity) {
        Objects.requireNonNull(alarms, "Expected alarms.");
        Objects.requireNonNull(severity, "Expected severity.");

        if (NOT_CLEARED.equals(severity)) {
            final String cleared = AlarmSeverity.CLEARED.toString().toLowerCase();
            alarms.removeIf(alarm -> cleared.equals(alarm.severity()));
            return;
        }
        alarms.removeIf(alarm -> !alarm.severity().equals(severity));
    }

    private static void sort(
        final List<? extends PdeAlarm> alarms,
        final Comparator<? super PdeAlarm> comparator,
        final boolean sortAscending
    ) {
        if (sortAscending) {
            alarms.sort(comparator);
        } else {
            alarms.sort(comparator.reversed());
        }
    }

    static void sortById(final List<? extends PdeAlarm> alarms, final boolean ascending) {
        sort(alarms, ID_COMPARATOR, ascending);
    }

    static void sortByRaisedAt(final List<? extends PdeAlarm> alarms, final boolean ascending) {
        sort(alarms, RAISED_AT_COMPARATOR, ascending);
    }

    static void sortByUpdatedAt(final List<? extends PdeAlarm> alarms, final boolean ascending) {
        sort(alarms, UPDATED_AT_COMPARATOR, ascending);
    }

    static void sortByClearedAt(final List<? extends PdeAlarm> alarms, final boolean ascending) {
        sort(alarms, CLEARED_AT_COMPARATOR, ascending);
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