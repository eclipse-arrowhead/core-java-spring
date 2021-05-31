package eu.arrowhead.core.plantdescriptionengine.alarms;

import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitor.dto.PdeAlarmDto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class AlarmManager {

    private final List<Alarm> activeAlarms = new ArrayList<>();
    private final List<Alarm> clearedAlarms = new ArrayList<>();

    private final Object lock = new Object();

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
     * Marks the specified alarm as acknowledged, noting the time at which this
     * was done.
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
     * Raises the given alarm, unless one with the same parameters is already
     * active.
     */
    public void raise(final Alarm alarm) {
        synchronized (lock) {
            if (!alreadyRaised(alarm)) {
                activeAlarms.add(alarm);
            }
        }
    }

    /**
     * Raises each of the given alarms, unless one with the same exact
     * parameters is already active.
     */
    public void raise(final List<Alarm> alarms) {
        alarms.forEach(this::raise);
    }

    private boolean alreadyRaised(Alarm alarm) {
        return activeAlarms.stream().anyMatch(alarm::matches);
    }

    private void clearAlarm(final Alarm alarm) {
        Objects.requireNonNull(alarm, "Expected alarm.");

        alarm.setCleared();

        clearedAlarms.add(alarm);
        activeAlarms.remove(alarm);
    }


    /**
     * Raise an alarm indicating that a monitorable system did not respond to
     * a ping request.
     *
     * @param systemName Name of a Plant Description system.
     */
    public void raiseNoPingResponse(final String systemName) {
        Objects.requireNonNull(systemName, "Expected system name.");
        raise(new Alarm(null, systemName, Collections.emptyMap(), AlarmCause.NO_PING_RESPONSE));
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
     * Clears all alarms with the specified alarm cause, except for the ones
     * matching alarms in {@code currentlyDetectedAlarms}.
     */
    private void clearOldAlarms(final List<Alarm> currentlyDetectedAlarms, final AlarmCause... causes) {

        final var currentlyActiveAlarms = getActiveAlarmData(Arrays.asList(causes));
        for (final Alarm previouslyDetectedAlarm : currentlyActiveAlarms) {
            if (currentlyDetectedAlarms.stream().noneMatch(alarm -> alarm.matches(previouslyDetectedAlarm))) {
                clearAlarm(previouslyDetectedAlarm);
            }
        }
    }

    /**
     * Replace any active alarms with the given causes with the provided list
     * of alarms.
     */
    public void replaceAlarms(final List<Alarm> newAlarms, final AlarmCause... notMonitorable) {
        clearOldAlarms(newAlarms, notMonitorable);
        raise(newAlarms);
    }

    public void clearAlarm(final int id) {
        Alarm alarm = getAlarmData(id);
        if (alarm != null) {
            clearAlarm(alarm);
        }
    }
}