package eu.arrowhead.core.plantdescriptionengine.alarms;

import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitor.dto.PdeAlarmDto;

import java.time.Instant;
import java.util.ArrayList;
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
    private Alarm getAlarmData(int id) {
        for (final var alarm : activeAlarms) {
            if (alarm.id == id) {
                return alarm;
            }
        }
        return null;
    }

    /**
     * @return A list containing all PDE alarms.
     */
    public List<PdeAlarmDto> getAlarms() {
        final List<PdeAlarmDto> result = new ArrayList<>();

        final List<Alarm> allAlarms = new ArrayList<>();
        allAlarms.addAll(activeAlarms);
        allAlarms.addAll(clearedAlarms);

        for (final var alarm : allAlarms) {
            result.add(alarm.toPdeAlarm());
        }

        return result;
    }

    /**
     * @param cause An alarm cause.
     * @return A list of raw data describing currently active alarms with the
     * given alarm cause.
     */
    public List<Alarm> getActiveAlarmData(AlarmCause cause) {
        return activeAlarms.stream()
            .filter(alarm -> cause.equals(alarm.cause))
            .collect(Collectors.toList());
    }

    /**
     * @param cause A list of alarm causes.
     * @return A list of raw data describing currently active alarms whose alarm
     * cause is any of the causes provided.
     */
    public List<Alarm> getActiveAlarmData(List<AlarmCause> causes) {
        return activeAlarms.stream()
            .filter(alarm -> causes.contains(alarm.cause))
            .collect(Collectors.toList());
    }

    /**
     * @param id The ID of a PDE Alarm.
     * @return The PDE Alarm with the given ID if it exists, null otherwise.
     */
    public PdeAlarmDto getAlarmDto(int id) {
        final Alarm alarmData = getAlarmData(id);
        if (alarmData != null) {
            return alarmData.toPdeAlarm();
        }
        return null;
    }

    /**
     * @param id           ID of an alarm.
     * @param acknowledged The new value to assign to the alarm's acknowledged
     *                     field.
     */
    public void setAcknowledged(int id, boolean acknowledged) throws IllegalArgumentException {
        final Alarm alarm = getAlarmData(id);
        if (alarm == null) {
            throw new IllegalArgumentException("There is no alarm with ID " + id + ".");
        }
        alarm.acknowledged = acknowledged;
        alarm.acknowledgedAt = Instant.now();
    }

    private void raiseAlarm(String systemId, String systemName, Map<String, String> metadata, AlarmCause cause) {
        // TODO: Concurrency handling

        // Check if this alarm has already been raised:
        for (final var alarm : activeAlarms) {
            if (alarm.matches(systemId, systemName, metadata, cause)) {
                return;
            }
        }
        activeAlarms.add(new Alarm(systemId, systemName, metadata, cause));
    }

    public void clearAlarm(Alarm alarm) {

        // TODO: Find the alarm from active alarms instead. That way, a copy can
        // be passed instead of the actual instance.

        alarm.clearedAt = Instant.now();
        alarm.updatedAt = Instant.now();

        clearedAlarms.add(alarm);
        activeAlarms.remove(alarm);
    }

    public void raiseSystemNotRegistered(String systemId, String systemName, Map<String, String> metadata) {
        Objects.requireNonNull(systemId, "Expected system ID");
        raiseAlarm(systemId, systemName, metadata, AlarmCause.systemNotRegistered);
    }

    public void raiseMultipleMatches(String systemId, String systemName, Map<String, String> metadata) {
        Objects.requireNonNull(systemId, "Expected system ID");
        raiseAlarm(systemId, systemName, metadata, AlarmCause.multipleMatches);
    }

    public void raiseSystemInactive(String systemName) {
        raiseAlarm(null, systemName, null, AlarmCause.systemInactive);
    }

    public void clearSystemInactive(String systemName) {

        final List<Alarm> cleared = activeAlarms.stream()
            .filter(alarm ->
                alarm.cause == AlarmCause.systemInactive &&
                    alarm.systemName == systemName
            )
            .collect(Collectors.toList());

        for (var alarm : cleared) {
            clearAlarm(alarm);
        }
    }

    public void raiseSystemNotInDescription(String systemName, Map<String, String> metadata) {
        raiseAlarm(null, systemName, metadata, AlarmCause.systemNotInDescription);
    }
}