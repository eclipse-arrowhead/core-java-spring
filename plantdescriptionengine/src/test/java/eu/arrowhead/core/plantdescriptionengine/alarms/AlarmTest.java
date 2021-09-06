package eu.arrowhead.core.plantdescriptionengine.alarms;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AlarmTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void shouldHaveProperDescriptions() {
        final String systemName = "A";
        final String systemId = "123";
        final Map<String, String> metadata = Collections.emptyMap();
        final Alarm alarmFromNamedSystem = new Alarm(null, systemName, metadata, AlarmCause.SYSTEM_NOT_REGISTERED);
        final Alarm alarmFromUnnamedSystem = new Alarm(systemId, null, metadata, AlarmCause.SYSTEM_NOT_IN_DESCRIPTION);

        assertEquals(
            "System named '" + systemName + "' cannot be found in the Service Registry.",
            alarmFromNamedSystem.getDescription()
        );
        assertEquals(
            "System with ID '" + systemId + "' is not present in the active Plant Description.",
            alarmFromUnnamedSystem.getDescription()
        );
    }

    @Test
    public void shouldRejectNullAlarmCause() {
        thrown.expect(RuntimeException.class);
        thrown.expectMessage("Expected an alarm cause.");
        new Alarm("XYZ", null, null, null);
    }

    @Test
    public void shouldMatch() {
        final String systemName = "ABC";
        final String systemId = "123";

        final Alarm alarmA = new Alarm(null, systemName, Collections.emptyMap(), AlarmCause.SYSTEM_NOT_REGISTERED);
        final Alarm alarmB = new Alarm(systemId, null, Collections.emptyMap(), AlarmCause.SYSTEM_NOT_REGISTERED);
        final Alarm alarmC = new Alarm(systemId, systemName, Collections.emptyMap(), AlarmCause.SYSTEM_NOT_REGISTERED);
        final Alarm alarmD = new Alarm(systemId, systemName, Map.of("a", "1"), AlarmCause.SYSTEM_NOT_REGISTERED);

        assertTrue(alarmA.matches(null, systemName, null, AlarmCause.SYSTEM_NOT_REGISTERED));
        assertTrue(alarmB.matches(systemId, null, Collections.emptyMap(), AlarmCause.SYSTEM_NOT_REGISTERED));
        assertTrue(alarmB.matches(systemId, null, null, AlarmCause.SYSTEM_NOT_REGISTERED));
        assertTrue(alarmC.matches(systemId, systemName, null, AlarmCause.SYSTEM_NOT_REGISTERED));
        assertTrue(alarmD.matches(systemId, systemName, Map.of("a", "1"), AlarmCause.SYSTEM_NOT_REGISTERED));
    }

    @Test
    public void shouldNotMatch() {
        final String systemName = "ABC";
        final String systemId = "123";
        final Map<String, String> metadata = Map.of("x", "1", "y", "2");

        final String wrongName = "Incorrect name";
        final String incorrectId = "Incorrect ID";
        final Map<String, String> incorrectMetadata = Map.of("x", "1", "y", "3");

        final Alarm alarmA = new Alarm(null, systemName, Collections.emptyMap(), AlarmCause.SYSTEM_NOT_REGISTERED);
        final Alarm alarmB = new Alarm(systemId, null, Collections.emptyMap(), AlarmCause.SYSTEM_NOT_REGISTERED);
        final Alarm alarmC = new Alarm(systemId, systemName, Collections.emptyMap(), AlarmCause.SYSTEM_NOT_REGISTERED);
        final Alarm alarmD = new Alarm(systemId, systemName, metadata, AlarmCause.SYSTEM_NOT_REGISTERED);

        assertFalse(alarmA.matches(wrongName, systemName, null, AlarmCause.SYSTEM_NOT_REGISTERED));
        assertFalse(alarmB.matches(systemId, incorrectId, null, AlarmCause.SYSTEM_NOT_REGISTERED));
        assertFalse(alarmC.matches(systemId, systemName, null, AlarmCause.SYSTEM_NOT_IN_DESCRIPTION));
        assertFalse(alarmC.matches(null, null, null, AlarmCause.SYSTEM_NOT_REGISTERED));
        assertFalse(alarmD.matches(null, null, null, AlarmCause.SYSTEM_NOT_REGISTERED));
        assertFalse(alarmD.matches(null, null, metadata, AlarmCause.SYSTEM_NOT_REGISTERED));
        assertFalse(alarmD.matches(systemId, systemName, incorrectMetadata, AlarmCause.SYSTEM_NOT_REGISTERED));
    }

}