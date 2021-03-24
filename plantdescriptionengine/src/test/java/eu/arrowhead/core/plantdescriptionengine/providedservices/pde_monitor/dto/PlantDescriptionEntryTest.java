package eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitor.dto;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PlantDescriptionEntryTest {

    final Instant now = Instant.now();

    @Test
    public void shouldSortCorrectly() {
        int idA = 24;
        int idB = 65;
        int idC = 9;

        Instant t1 = Instant.now();
        Instant t2 = t1.plus(1, ChronoUnit.HOURS);
        Instant t3 = t1.plus(2, ChronoUnit.HOURS);
        Instant t4 = t1.plus(3, ChronoUnit.HOURS);

        final var entryA = new PlantDescriptionEntryBuilder()
            .id(idA)
            .plantDescription("A")
            .createdAt(t3)
            .updatedAt(t3)
            .active(false)
            .build();

        final var entryB = new PlantDescriptionEntryBuilder()
            .id(idB)
            .plantDescription("B")
            .createdAt(t1)
            .updatedAt(t1)
            .active(false)
            .build();
        final var entryC = new PlantDescriptionEntryBuilder()
            .id(idC)
            .plantDescription("C")
            .createdAt(t2)
            .updatedAt(t4)
            .active(false)
            .build();

        List<PlantDescriptionEntry> entries = Arrays.asList(entryA, entryB, entryC);

        PlantDescriptionEntry.sort(entries, "createdAt", true);
        assertEquals(idB, entries.get(0).id());
        assertEquals(idC, entries.get(1).id());
        assertEquals(idA, entries.get(2).id());

        PlantDescriptionEntry.sort(entries, "updatedAt", true);
        assertEquals(idB, entries.get(0).id());
        assertEquals(idA, entries.get(1).id());
        assertEquals(idC, entries.get(2).id());

        PlantDescriptionEntry.sort(entries, "id", true);
        assertEquals(idC, entries.get(0).id());
        assertEquals(idA, entries.get(1).id());
        assertEquals(idB, entries.get(2).id());

        PlantDescriptionEntry.sort(entries, "id", false);
        assertEquals(idB, entries.get(0).id());
        assertEquals(idA, entries.get(1).id());
        assertEquals(idC, entries.get(2).id());
    }

    @Test
    public void shouldDisallowIncorrectSortField() {
        final var entry = new PlantDescriptionEntryBuilder()
            .id(1)
            .plantDescription("ABC")
            .createdAt(now)
            .updatedAt(now)
            .active(true)
            .build();
        List<PlantDescriptionEntry> entries = Collections.singletonList(entry);

        Exception exception = assertThrows(IllegalArgumentException.class,
            () -> PlantDescriptionEntry.sort(entries, "Nonexistent", true));
        assertEquals("'Nonexistent' is not a valid sort field for Plant Description Entries.", exception.getMessage());
    }

}
