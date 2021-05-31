package eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitor.dto;

import org.junit.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class PdeAlarmTest {

    final Instant now = Instant.now();

    @Test
    public void shouldSortAlarms() {

        final int idA = 23;
        final int idB = 99;
        final int idC = 4;
        final int idD = 101;

        final Instant t1 = Instant.now();
        final Instant t2 = t1.plus(1, ChronoUnit.HOURS);
        final Instant t3 = t1.plus(2, ChronoUnit.HOURS);
        final Instant t4 = t1.plus(3, ChronoUnit.HOURS);
        final Instant t5 = t1.plus(4, ChronoUnit.HOURS);

        final PdeAlarm alarmA = new PdeAlarmDto.Builder()
            .id(idA)
            .systemId("a")
            .acknowledged(false)
            .severity("warning")
            .description("Something went wrong")
            .raisedAt(t3)
            .updatedAt(t3)
            .clearedAt(t4)
            .build();
        final PdeAlarm alarmB = new PdeAlarmDto.Builder()
            .id(idB)
            .systemId("b")
            .acknowledged(false)
            .severity("warning")
            .description("Something went wrong")
            .raisedAt(t1)
            .updatedAt(t1)
            .clearedAt(t5)
            .build();
        final PdeAlarm alarmC = new PdeAlarmDto.Builder()
            .id(idC)
            .systemId("c")
            .acknowledged(false)
            .severity("warning")
            .description("Something went wrong")
            .raisedAt(t2)
            .updatedAt(t4)
            .build();

        final PdeAlarm alarmD = new PdeAlarmDto.Builder()
            .id(idD)
            .systemId("d")
            .acknowledged(false)
            .severity("warning")
            .description("Something went wrong")
            .raisedAt(t4)
            .updatedAt(t4)
            .build();

        final List<PdeAlarm> alarms = Arrays.asList(alarmA, alarmB, alarmC, alarmD);

        PdeAlarm.sortById(alarms, true);
        assertEquals(idC, alarms.get(0).id());
        assertEquals(idA, alarms.get(1).id());
        assertEquals(idB, alarms.get(2).id());
        assertEquals(idD, alarms.get(3).id());

        PdeAlarm.sortByRaisedAt(alarms, true);
        assertEquals(idB, alarms.get(0).id());
        assertEquals(idC, alarms.get(1).id());
        assertEquals(idA, alarms.get(2).id());
        assertEquals(idD, alarms.get(3).id());

        PdeAlarm.sortByUpdatedAt(alarms, true);
        assertEquals(idB, alarms.get(0).id());
        assertEquals(idA, alarms.get(1).id());
        assertEquals(idC, alarms.get(2).id());
        assertEquals(idD, alarms.get(3).id());

        PdeAlarm.sortByClearedAt(alarms, true);
        assertEquals(idA, alarms.get(0).id());
        assertEquals(idB, alarms.get(1).id());
        assertEquals(idC, alarms.get(2).id());
        assertEquals(idD, alarms.get(3).id());

        PdeAlarm.sortByClearedAt(alarms, false);
        assertEquals(idC, alarms.get(0).id());
        assertEquals(idD, alarms.get(1).id());
        assertEquals(idB, alarms.get(2).id());
        assertEquals(idA, alarms.get(3).id());
    }

    @Test
    public void shouldFilterBySeverity() {

        final int idA = 1;
        final int idB = 2;
        final int idC = 3;
        final int idD = 4;

        final PdeAlarm alarmA = new PdeAlarmDto.Builder()
            .id(idA)
            .acknowledged(false)
            .systemId("A")
            .severity("warning")
            .description("Something went wrong")
            .raisedAt(now)
            .updatedAt(now)
            .build();
        final PdeAlarm alarmB = new PdeAlarmDto.Builder()
            .id(idB)
            .acknowledged(false)
            .systemId("B")
            .severity("danger")
            .description("Something went wrong")
            .raisedAt(now)
            .updatedAt(now)
            .build();
        final PdeAlarm alarmC = new PdeAlarmDto.Builder()
            .id(idC)
            .acknowledged(false)
            .systemId("C")
            .severity("cleared")
            .description("Something went wrong")
            .raisedAt(now)
            .updatedAt(now)
            .clearedAt(now.plus(1, ChronoUnit.HOURS))
            .build();
        final PdeAlarm alarmD = new PdeAlarmDto.Builder()
            .id(idD)
            .acknowledged(false)
            .systemId("D")
            .severity("warning")
            .description("Something went wrong")
            .raisedAt(now)
            .updatedAt(now)
            .build();

        final List<PdeAlarm> alarms = new ArrayList<>();
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

        final PdeAlarm alarmA = new PdeAlarmDto.Builder()
            .id(idA)
            .systemId("a")
            .acknowledged(false)
            .severity("warning")
            .description("Something went wrong")
            .raisedAt(now)
            .updatedAt(now)
            .build();
        final PdeAlarm alarmB = new PdeAlarmDto.Builder()
            .id(idB)
            .systemId("b")
            .acknowledged(false)
            .severity("danger")
            .description("Something went wrong")
            .raisedAt(now)
            .updatedAt(now)
            .build();
        final PdeAlarm alarmC = new PdeAlarmDto.Builder()
            .id(idC)
            .systemId("c")
            .acknowledged(false)
            .severity("cleared")
            .description("Something went wrong")
            .raisedAt(now)
            .updatedAt(now)
            .clearedAt(now.plus(1, ChronoUnit.HOURS))
            .build();
        final PdeAlarm alarmD = new PdeAlarmDto.Builder()
            .id(idD)
            .systemId("d")
            .acknowledged(false)
            .severity("warning")
            .description("Something went wrong")
            .raisedAt(now)
            .updatedAt(now)
            .build();

        final List<PdeAlarm> alarms = new ArrayList<>();
        alarms.add(alarmA);
        alarms.add(alarmB);
        alarms.add(alarmC);
        alarms.add(alarmD);

        PdeAlarm.filterBySeverity(alarms, PdeAlarm.NOT_CLEARED);

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

        final PdeAlarm alarmA = new PdeAlarmDto.Builder()
            .id(idA)
            .systemId("A")
            .acknowledged(false)
            .severity("warning")
            .description("Something went wrong")
            .raisedAt(now)
            .updatedAt(now)
            .build();

        final PdeAlarm alarmB = new PdeAlarmDto.Builder()
            .id(idB)
            .systemId("B")
            .acknowledged(true)
            .severity("warning")
            .description("Something went wrong")
            .raisedAt(now)
            .updatedAt(now)
            .build();

        final PdeAlarm alarmC = new PdeAlarmDto.Builder()
            .id(idC)
            .systemId("C")
            .acknowledged(false)
            .severity("warning")
            .description("Something went wrong")
            .raisedAt(now)
            .updatedAt(now)
            .build();

        final List<PdeAlarm> alarms = new ArrayList<>();
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

        final PdeAlarm alarmA = new PdeAlarmDto.Builder()
            .id(idA)
            .systemId("a")
            .acknowledged(false)
            .severity("warning")
            .description("Something went wrong")
            .raisedAt(now)
            .updatedAt(now)
            .build();

        final PdeAlarm alarmB = new PdeAlarmDto.Builder()
            .id(idB)
            .systemId("b")
            .acknowledged(true)
            .severity("warning")
            .description("Something went wrong")
            .raisedAt(now)
            .updatedAt(now)
            .build();

        final PdeAlarm alarmC = new PdeAlarmDto.Builder()
            .id(idC)
            .systemId("c")
            .acknowledged(false)
            .severity("warning")
            .description("Something went wrong")
            .raisedAt(now)
            .updatedAt(now)
            .build();

        final List<PdeAlarm> alarms = new ArrayList<>();
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

        final PdeAlarm alarmA = new PdeAlarmDto.Builder()
            .id(idA)
            .systemName(systemA)
            .systemId("A")
            .acknowledged(false)
            .severity("warning")
            .description("Something went wrong")
            .raisedAt(now)
            .updatedAt(now)
            .build();

        final PdeAlarm alarmB = new PdeAlarmDto.Builder()
            .id(idB)
            .systemName(systemA)
            .systemId("A")
            .acknowledged(false)
            .severity("warning")
            .description("Something went wrong")
            .raisedAt(now)
            .updatedAt(now)
            .build();

        final PdeAlarm alarmC = new PdeAlarmDto.Builder()
            .id(idC)
            .systemName("Sys-B")
            .systemId("B")
            .acknowledged(false)
            .severity("warning")
            .description("Something went wrong")
            .raisedAt(now)
            .updatedAt(now)
            .build();

        final PdeAlarm alarmD = new PdeAlarmDto.Builder()
            .id(idD)
            .systemId("D")
            .acknowledged(false)
            .severity("warning")
            .description("Something went wrong")
            .raisedAt(now)
            .updatedAt(now)
            .build();

        final List<PdeAlarm> alarms = new ArrayList<>();
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
