package eu.arrowhead.core.plantdescriptionengine.alarms;

import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitor.dto.PdeAlarmDto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class AlarmManager {

    private final List<Alarm> activeAlarms = new ArrayList<>();
    private final List<Alarm> clearedAlarms = new ArrayList<>();

    /**
     * @param id The ID of a PDE Alarm.
     * @return Data describing the alarm with the given ID if it exists, null
     * otherwise.
     */
    private Alarm getAlarmData(final int id) {
        for (final Alarm alarm : activeAlarms) {
            if (alarm.getId() == id) {
                return alarm;
            }
        }
        return null;
    }

    /**
     * @return A list containing all PDE alarms.
     */
    public List<PdeAlarmDto> getAlarms() {

        int size = activeAlarms.size() + clearedAlarms.size();
        final List<PdeAlarmDto> result = new ArrayList<>(size);
        final List<Alarm> allAlarms = new ArrayList<>(size);
        allAlarms.addAll(activeAlarms);
        allAlarms.addAll(clearedAlarms);

        for (final Alarm alarm : allAlarms) {
            result.add(alarm.toPdeAlarm());
        }

        return result;
    }

    /**
     * @param cause An alarm cause.
     * @return A list of raw data describing currently active alarms with the
     * given alarm cause.
     */
    public List<Alarm> getActiveAlarmData(final AlarmCause cause) {
        Objects.requireNonNull(cause, "Expected alarm cause.");
        return activeAlarms.stream()
            .filter(alarm -> cause == alarm.getCause())
            .collect(Collectors.toList());
    }

    /**
     * @param causes A list of alarm causes.
     * @return A list of raw data describing currently active alarms whose alarm
     * cause is any of the causes provided.
     */
    public List<Alarm> getActiveAlarmData(final List<AlarmCause> causes) {
        Objects.requireNonNull(causes, "Expected alarm causes.");
        return activeAlarms.stream()
            .filter(alarm -> causes.contains(alarm.getCause()))
            .collect(Collectors.toList());
    }

    /**
     * @param id The ID of a PDE Alarm.
     * @return The PDE Alarm with the given ID if it exists, null otherwise.
     */
    public PdeAlarmDto getAlarmDto(final int id) {
        final Alarm alarmData = getAlarmData(id);
        if (alarmData != null) {
            return alarmData.toPdeAlarm();
        }
        return null;
    }

    /**
     * Marks the specified alarm as acknowledged.
     *
     * @param id ID of an alarm.
     */
    public void acknowledge(final int id) throws IllegalArgumentException {
        final Alarm alarm = getAlarmData(id);
        if (alarm == null) {
            throw new IllegalArgumentException("There is no alarm with ID " + id + ".");
        }
        alarm.acknowledge();
    }

    /**
     * Raises an alarm with the given info, unless an alarm with the exact same
     * parameters is already active.
     */
    private void raiseAlarm(
        final String systemId,
        final String systemName,
        final Map<String, String> metadata,
        final AlarmCause cause
    ) {
        synchronized (this) {
            final Map<String, String> nonNullMetadata = metadata == null ? Collections.emptyMap() : metadata;

            // Check if this alarm has already been raised:
            for (final Alarm alarm : activeAlarms) {
                if (alarm.matches(systemId, systemName, nonNullMetadata, cause)) {
                    return;
                }
            }
            activeAlarms.add(new Alarm(systemId, systemName, nonNullMetadata, cause));
        }
    }

    public void clearAlarm(final Alarm alarm) {
        Objects.requireNonNull(alarm, "Expected alarm.");

        // TODO: Find the alarm from active alarms instead. That way, a copy can
        // be passed instead of the actual instance.

        alarm.setCleared();

        clearedAlarms.add(alarm);
        activeAlarms.remove(alarm);
    }

    public void clearAlarm(final int id) {
        Alarm alarm = getAlarmData(id);
        if (alarm != null) {
            alarm.setCleared();
            clearedAlarms.add(alarm);
            activeAlarms.remove(alarm);
        }
    }

    /**
     * Raise an alarm indicating that a system required by the active Plant
     * Description was not found in the Service Registry.
     *
     * @param systemId   ID of a Plant Description system.
     * @param systemName Name of a Plant Description system, or null.
     * @param metadata   Metadata of a Plant Description system, or null.
     */
    public void raiseSystemNotRegistered(
        final String systemId,
        final String systemName,
        final Map<String, String> metadata
    ) {
        Objects.requireNonNull(systemId, "Expected system ID");
        raiseAlarm(systemId, systemName, metadata, AlarmCause.SYSTEM_NOT_REGISTERED);
    }

    /**
     * Raise an alarm indicating that a system required by the active Plant
     * Description cannot be uniquely identified in the Service Registry.
     *
     * @param systemId   ID of a Plant Description system.
     * @param systemName Name of a Plant Description system, or null.
     * @param metadata   Metadata of a Plant Description system, or null.
     */
    public void raiseMultipleMatches(
        final String systemId,
        final String systemName,
        final Map<String, String> metadata
    ) {
        Objects.requireNonNull(systemId, "Expected system ID");
        raiseAlarm(systemId, systemName, metadata, AlarmCause.MULTIPLE_MATCHES);
    }

    /**
     * Raise an alarm indicating that a supposedly monitorable system does not
     * seem to provide a 'Monitorable' service.
     *
     * @param systemId   ID of a Plant Description system.
     * @param systemName Name of a Plant Description system, or null.
     * @param metadata   Metadata of a Plant Description system, or null.
     */
    public void raiseSystemNotMonitorable(
        final String systemId,
        final String systemName,
        final Map<String, String> metadata
    ) {
        Objects.requireNonNull(systemId, "Expected system ID");
        raiseAlarm(systemId, systemName, metadata, AlarmCause.NOT_MONITORABLE);
    }

    /**
     * Clear any alarms indicating that the system matching the given arguments
     * does not provide a 'Monitorable' service.
     *
     * @param systemId   ID of a Plant Description system.
     * @param systemName Name of a Plant Description system, or null.
     * @param metadata   Metadata of a Plant Description system, or null.
     */
    public void clearSystemNotMonitorable(
        final String systemId,
        final String systemName,
        final Map<String, String> metadata
    ) {
        Objects.requireNonNull(systemId, "Expected system ID");
        final List<Alarm> cleared = activeAlarms.stream()
            .filter(alarm ->
                alarm.getCause() == AlarmCause.NOT_MONITORABLE &&
                    Objects.equals(systemId, alarm.getSystemId()) &&
                    Objects.equals(systemName, alarm.getSystemName()) &&
                    Objects.equals(metadata, alarm.getMetadata())
            )
            .collect(Collectors.toList());

        for (final Alarm alarm : cleared) {
            clearAlarm(alarm);
        }
    }

    /**
     * Raise an alarm indicating that a monitorable system did not respond to
     * a ping request.
     *
     * @param systemName Name of a Plant Description system.
     */
    public void raiseNoPingResponse(final String systemName) {
        Objects.requireNonNull(systemName, "Expected system name.");
        raiseAlarm(null, systemName, Collections.emptyMap(), AlarmCause.NO_PING_RESPONSE);
    }

    /**
     * Clear any alarms indicating that the given system did not respond to a
     * ping request.
     *
     * @param systemName Name of a Plant Description system.
     */
    public void clearNoPingResponse(final String systemName) {
        Objects.requireNonNull(systemName, "Expected system name.");

        final List<Alarm> cleared = activeAlarms.stream()
            .filter(alarm ->
                alarm.getCause() == AlarmCause.NO_PING_RESPONSE &&
                    systemName.equals(alarm.getSystemName())
            )
            .collect(Collectors.toList());

        for (final Alarm alarm : cleared) {
            clearAlarm(alarm);
        }
    }

    /**
     * Raise an alarm indicating that a system found in the Service Registry is
     * missing from the active Plant Description.
     *
     * @param systemName Name of a Plant Description system.
     * @param metadata   Metadata of a system, or null.
     */
    public void raiseSystemNotInDescription(final String systemName, final Map<String, String> metadata) {
        Objects.requireNonNull(systemName, "Expected system name.");
        raiseAlarm(null, systemName, metadata, AlarmCause.SYSTEM_NOT_IN_DESCRIPTION);
    }
}