package eu.arrowhead.core.plantdescriptionengine.alarms;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

public class AlarmTest {

    @Test
    public void shouldHaveProperDescriptions() {
        String systemName = "A";
        String systemId = "123";
        Alarm alarmFromNamedSystem = new Alarm(null, systemName, null, AlarmCause.systemNotRegistered);
        Alarm alarmFromUnnamedSystem = new Alarm(systemId, null, null, AlarmCause.systemNotInDescription);

        assertEquals("System named '" + systemName + "' cannot be found in the Service Registry.",
            alarmFromNamedSystem.description());
        assertEquals("System with ID '" + systemId + "' is not present in the active Plant Description.",
            alarmFromUnnamedSystem.description());
    }

    @Test
    public void shouldRejectNullAlarmCause() {
        Exception exception = assertThrows(RuntimeException.class, () -> new Alarm("XYZ", null, null, null));
        assertEquals("Expected an alarm cause.", exception.getMessage());
    }

    @Test
    public void shouldMatch() {
        final String systemName = "ABC";
        final String systemId = "123";

        Alarm alarmA = new Alarm(null, systemName, null, AlarmCause.systemNotRegistered);
        Alarm alarmB = new Alarm(systemId, null, null, AlarmCause.systemNotRegistered);
        Alarm alarmC = new Alarm(systemId, systemName, null, AlarmCause.systemNotRegistered);
        Alarm alarmD = new Alarm(systemId, systemName, Map.of("a", "1"), AlarmCause.systemNotRegistered);

        assertTrue(alarmA.matches(null, systemName, null, AlarmCause.systemNotRegistered));
        assertTrue(alarmB.matches(systemId, null, null, AlarmCause.systemNotRegistered));
        assertTrue(alarmC.matches(systemId, systemName, null, AlarmCause.systemNotRegistered));
        assertTrue(alarmD.matches(systemId, systemName, Map.of("a", "1"), AlarmCause.systemNotRegistered));
    }

    @Test
    public void shouldNotMatch() {
        final String systemName = "ABC";
        final String systemId = "123";

        Alarm alarmA = new Alarm(null, systemName, null, AlarmCause.systemNotRegistered);
        Alarm alarmB = new Alarm(systemId, null, null, AlarmCause.systemNotRegistered);
        Alarm alarmC = new Alarm(systemId, systemName, null, AlarmCause.systemNotRegistered);
        Alarm alarmD = new Alarm(systemId, systemName, Map.of("x", "y"), AlarmCause.systemNotRegistered);

        assertFalse(alarmA.matches("Incorrect name", systemName, null, AlarmCause.systemNotRegistered));
        assertFalse(alarmB.matches(systemId, "Incorrect ID", null, AlarmCause.systemNotRegistered));
        assertFalse(alarmC.matches(systemId, systemName, null, AlarmCause.systemNotInDescription));
        assertFalse(alarmC.matches(null, null, null, AlarmCause.systemNotRegistered));
        assertFalse(alarmD.matches(null, null, null, AlarmCause.systemNotRegistered));
        assertFalse(alarmD.matches(null, null, Map.of("x", "z"), AlarmCause.systemNotRegistered));
    }


}