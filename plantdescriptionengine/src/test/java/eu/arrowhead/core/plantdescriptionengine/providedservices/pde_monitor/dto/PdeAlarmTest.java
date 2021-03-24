package eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitor.dto;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PdeAlarmTest {

    final Instant now = Instant.now();

    @Test
    public void shouldSortAlarms() {

        final int idA = 23;
        final int idB = 99;
        final int idC = 4;
        final int idD = 101;

        Instant t1 = Instant.now();
        Instant t2 = t1.plus(1, ChronoUnit.HOURS);
        Instant t3 = t1.plus(2, ChronoUnit.HOURS);
        Instant t4 = t1.plus(3, ChronoUnit.HOURS);
        Instant t5 = t1.plus(4, ChronoUnit.HOURS);

        final PdeAlarm alarmA = new PdeAlarmBuilder()
            .id(idA)
            .systemId("a")
            .acknowledged(false)
            .severity("warning")
            .description("Something went wrong")
            .raisedAt(t3)
            .updatedAt(t3)
            .clearedAt(t4)
            .build();
        final PdeAlarm alarmB = new PdeAlarmBuilder()
            .id(idB)
            .systemId("b")
            .acknowledged(false)
            .severity("warning")
            .description("Something went wrong")
            .raisedAt(t1)
            .updatedAt(t1)
            .clearedAt(t5)
            .build();
        final PdeAlarm alarmC = new PdeAlarmBuilder()
            .id(idC)
            .systemId("c")
            .acknowledged(false)
            .severity("warning")
            .description("Something went wrong")
            .raisedAt(t2)
            .updatedAt(t4)
            .build();

        final PdeAlarm alarmD = new PdeAlarmBuilder()
            .id(idD)
            .systemId("d")
            .acknowledged(false)
            .severity("warning")
            .description("Something went wrong")
            .raisedAt(t4)
            .updatedAt(t4)
            .build();

        List<PdeAlarm> alarms = Arrays.asList(alarmA, alarmB, alarmC, alarmD);

        PdeAlarm.sort(alarms, "id", true);
        assertEquals(idC, alarms.get(0).id());
        assertEquals(idA, alarms.get(1).id());
        assertEquals(idB, alarms.get(2).id());
        assertEquals(idD, alarms.get(3).id());

        PdeAlarm.sort(alarms, "raisedAt", true);
        assertEquals(idB, alarms.get(0).id());
        assertEquals(idC, alarms.get(1).id());
        assertEquals(idA, alarms.get(2).id());
        assertEquals(idD, alarms.get(3).id());

        PdeAlarm.sort(alarms, "updatedAt", true);
        assertEquals(idB, alarms.get(0).id());
        assertEquals(idA, alarms.get(1).id());
        assertEquals(idC, alarms.get(2).id());
        assertEquals(idD, alarms.get(3).id());

        PdeAlarm.sort(alarms, "clearedAt", true);
        assertEquals(idA, alarms.get(0).id());
        assertEquals(idB, alarms.get(1).id());
        assertEquals(idC, alarms.get(2).id());
        assertEquals(idD, alarms.get(3).id());

        PdeAlarm.sort(alarms, "clearedAt", false);
        assertEquals(idC, alarms.get(0).id());
        assertEquals(idD, alarms.get(1).id());
        assertEquals(idB, alarms.get(2).id());
        assertEquals(idA, alarms.get(3).id());
    }

    @Test
    public void shouldDisallowIncorrectSortField() {
        final PdeAlarm alarm = new PdeAlarmBuilder()
            .id(6)
            .acknowledged(false)
            .systemId("Some-ID")
            .severity("warning")
            .description("Something went wrong")
            .raisedAt(now)
            .updatedAt(now)
            .build();

        List<PdeAlarm> alarms = Collections.singletonList(alarm);

        Exception exception = assertThrows(IllegalArgumentException.class,
            () -> PdeAlarm.sort(alarms, "Illegal", true));
        assertEquals("'Illegal' is not a valid sort field for PDE Alarms.", exception.getMessage());
    }

    @Test
    public void shouldFilterBySeverity() {

        final int idA = 1;
        final int idB = 2;
        final int idC = 3;
        final int idD = 4;

        final PdeAlarm alarmA = new PdeAlarmBuilder()
            .id(idA)
            .acknowledged(false)
            .systemId("A")
            .severity("warning")
            .description("Something went wrong")
            .raisedAt(now)
            .updatedAt(now)
            .build();
        final PdeAlarm alarmB = new PdeAlarmBuilder()
            .id(idB)
            .acknowledged(false)
            .systemId("B")
            .severity("danger")
            .description("Something went wrong")
            .raisedAt(now)
            .updatedAt(now)
            .build();
        final PdeAlarm alarmC = new PdeAlarmBuilder()
            .id(idC)
            .acknowledged(false)
            .systemId("C")
            .severity("cleared")
            .description("Something went wrong")
            .raisedAt(now)
            .updatedAt(now)
            .clearedAt(now.plus(1, ChronoUnit.HOURS))
            .build();
        final PdeAlarm alarmD = new PdeAlarmBuilder()
            .id(idD)
            .acknowledged(false)
            .systemId("D")
            .severity("warning")
            .description("Something went wrong")
            .raisedAt(now)
            .updatedAt(now)
            .build();

        List<PdeAlarm> alarms = new ArrayList<>();
        alarms.add(alarmA);
        alarms.add(alarmB);
        alarms.add(alarmC);
        alarms.add(alarmD);

        PdeAlarm.filterBySeverity(alarms, "warning");

        assertEquals(2, alarms.size());
        assertEquals(idA, alarms.get(0).id());
        assertEquals(idD, alarms.get(1).id());
    }

    @Test
    public void shouldFilterCleared() {

        final int idA = 1;
        final int idB = 2;
        final int idC = 3;
        final int idD = 4;

        final PdeAlarm alarmA = new PdeAlarmBuilder()
            .id(idA)
            .systemId("a")
            .acknowledged(false)
            .severity("warning")
            .description("Something went wrong")
            .raisedAt(now)
            .updatedAt(now)
            .build();
        final PdeAlarm alarmB = new PdeAlarmBuilder()
            .id(idB)
            .systemId("b")
            .acknowledged(false)
            .severity("danger")
            .description("Something went wrong")
            .raisedAt(now)
            .updatedAt(now)
            .build();
        final PdeAlarm alarmC = new PdeAlarmBuilder()
            .id(idC)
            .systemId("c")
            .acknowledged(false)
            .severity("cleared")
            .description("Something went wrong")
            .raisedAt(now)
            .updatedAt(now)
            .clearedAt(now.plus(1, ChronoUnit.HOURS))
            .build();
        final PdeAlarm alarmD = new PdeAlarmBuilder()
            .id(idD)
            .systemId("d")
            .acknowledged(false)
            .severity("warning")
            .description("Something went wrong")
            .raisedAt(now)
            .updatedAt(now)
            .build();

        List<PdeAlarm> alarms = new ArrayList<>();
        alarms.add(alarmA);
        alarms.add(alarmB);
        alarms.add(alarmC);
        alarms.add(alarmD);

        PdeAlarm.filterBySeverity(alarms, "not_cleared");

        assertEquals(3, alarms.size());
        assertEquals(idA, alarms.get(0).id());
        assertEquals(idB, alarms.get(1).id());
        assertEquals(idD, alarms.get(2).id());
    }

    @Test
    public void shouldFilterAcknowledged() {

        final int idA = 43;
        final int idB = 12;
        final int idC = 6;

        final PdeAlarm alarmA = new PdeAlarmBuilder()
            .id(idA)
            .systemId("A")
            .acknowledged(false)
            .severity("warning")
            .description("Something went wrong")
            .raisedAt(now)
            .updatedAt(now)
            .build();

        final PdeAlarm alarmB = new PdeAlarmBuilder()
            .id(idB)
            .systemId("B")
            .acknowledged(true)
            .severity("warning")
            .description("Something went wrong")
            .raisedAt(now)
            .updatedAt(now)
            .build();

        final PdeAlarm alarmC = new PdeAlarmBuilder()
            .id(idC)
            .systemId("C")
            .acknowledged(false)
            .severity("warning")
            .description("Something went wrong")
            .raisedAt(now)
            .updatedAt(now)
            .build();

        List<PdeAlarm> alarms = new ArrayList<>();
        alarms.add(alarmA);
        alarms.add(alarmB);
        alarms.add(alarmC);

        PdeAlarm.filterAcknowledged(alarms, true);
        assertEquals(1, alarms.size());
        assertEquals(idB, alarms.get(0).id());
    }

    @Test
    public void shouldFilterUnacknowledged() {

        final int idA = 43;
        final int idB = 12;
        final int idC = 6;

        final PdeAlarm alarmA = new PdeAlarmBuilder()
            .id(idA)
            .systemId("a")
            .acknowledged(false)
            .severity("warning")
            .description("Something went wrong")
            .raisedAt(now)
            .updatedAt(now)
            .build();

        final PdeAlarm alarmB = new PdeAlarmBuilder()
            .id(idB)
            .systemId("b")
            .acknowledged(true)
            .severity("warning")
            .description("Something went wrong")
            .raisedAt(now)
            .updatedAt(now)
            .build();

        final PdeAlarm alarmC = new PdeAlarmBuilder()
            .id(idC)
            .systemId("c")
            .acknowledged(false)
            .severity("warning")
            .description("Something went wrong")
            .raisedAt(now)
            .updatedAt(now)
            .build();

        List<PdeAlarm> alarms = new ArrayList<>();
        alarms.add(alarmA);
        alarms.add(alarmB);
        alarms.add(alarmC);

        PdeAlarm.filterAcknowledged(alarms, false);
        assertEquals(2, alarms.size());
        assertEquals(idA, alarms.get(0).id());
        assertEquals(idC, alarms.get(1).id());
    }

    @Test
    public void shouldFilterBySystemName() {

        final int idA = 43;
        final int idB = 12;
        final int idC = 6;
        final int idD = 9;

        final String systemA = "Sys-A";

        final PdeAlarm alarmA = new PdeAlarmBuilder()
            .id(idA)
            .systemName(systemA)
            .systemId("A")
            .acknowledged(false)
            .severity("warning")
            .description("Something went wrong")
            .raisedAt(now)
            .updatedAt(now)
            .build();

        final PdeAlarm alarmB = new PdeAlarmBuilder()
            .id(idB)
            .systemName(systemA)
            .systemId("A")
            .acknowledged(false)
            .severity("warning")
            .description("Something went wrong")
            .raisedAt(now)
            .updatedAt(now)
            .build();

        final PdeAlarm alarmC = new PdeAlarmBuilder()
            .id(idC)
            .systemName("Sys-B")
            .systemId("B")
            .acknowledged(false)
            .severity("warning")
            .description("Something went wrong")
            .raisedAt(now)
            .updatedAt(now)
            .build();

        final PdeAlarm alarmD = new PdeAlarmBuilder()
            .id(idD)
            .systemId("D")
            .acknowledged(false)
            .severity("warning")
            .description("Something went wrong")
            .raisedAt(now)
            .updatedAt(now)
            .build();

        List<PdeAlarm> alarms = new ArrayList<>();
        alarms.add(alarmA);
        alarms.add(alarmB);
        alarms.add(alarmC);
        alarms.add(alarmD);

        PdeAlarm.filterBySystemName(alarms, systemA);
        assertEquals(2, alarms.size());
        assertEquals(idA, alarms.get(0).id());
        assertEquals(idB, alarms.get(1).id());
    }

}
