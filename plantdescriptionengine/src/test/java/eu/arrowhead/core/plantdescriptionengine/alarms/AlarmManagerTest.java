package eu.arrowhead.core.plantdescriptionengine.alarms;

import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitor.dto.PdeAlarm;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitor.dto.PdeAlarmDto;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AlarmManagerTest {

    @Test
    public void shouldRaiseNotRegistered() {
        final String systemId = "Sys-A";
        final String systemName = "System A";
        final AlarmManager alarmManager = new AlarmManager();

        alarmManager.raiseSystemNotRegistered(systemId, systemName, null);
        final PdeAlarm alarm = alarmManager.getAlarms().get(0);

        assertEquals(systemName, alarm.systemName().orElse(null));
        assertEquals("System named '" + systemName + "' cannot be found in the Service Registry.", alarm.description());
        assertFalse(alarm.acknowledged());
    }

    @Test
    public void shouldNotRaiseDuplicateAlarms() {
        final String systemName = "System A";
        final AlarmManager alarmManager = new AlarmManager();

        alarmManager.raiseNoPingResponse(systemName);
        alarmManager.raiseNoPingResponse(systemName);

        final List<PdeAlarmDto> alarms = alarmManager.getAlarms();
        final PdeAlarmDto alarm = alarms.get(0);

        assertEquals(1, alarms.size());
        assertEquals(systemName, alarm.systemName().orElse(null));
        assertEquals("Unknown", alarm.systemId());
    }

    @Test
    public void shouldClearAlarmBySystemName() {
        final String systemNameA = "System A";
        final String systemNameB = "System B";

        final AlarmManager alarmManager = new AlarmManager();

        alarmManager.raiseNoPingResponse(systemNameA);
        alarmManager.raiseNoPingResponse(systemNameB);

        assertEquals(2, alarmManager.getAlarms().size());
        for (final PdeAlarmDto alarm : alarmManager.getAlarms()) {
            assertTrue(alarm.clearedAt().isEmpty());
        }

        alarmManager.clearNoPingResponse(systemNameA);

        final List<PdeAlarmDto> alarms = alarmManager.getAlarms();
        assertEquals(2, alarms.size());
        int numCleared = 0;
        for (final PdeAlarmDto alarm : alarmManager.getAlarms()) {
            if (alarm.clearedAt().isPresent()) {
                numCleared++;
                assertEquals(systemNameA, alarm.systemName().orElse(null));
            } else {
                assertEquals(systemNameB, alarm.systemName().orElse(null));
            }
        }
        assertEquals(1, numCleared);
    }

    @Test
    public void shouldNotAllowSetAcknowledgedOnNonexistent() {
        final int nonexistentId = 32;
        final AlarmManager alarmManager = new AlarmManager();
        final Exception exception = assertThrows(IllegalArgumentException.class,
            () -> alarmManager.acknowledge(nonexistentId));
        final String expectedMessage = "There is no alarm with ID " + nonexistentId + ".";
        assertEquals(expectedMessage, exception.getMessage());
    }

}