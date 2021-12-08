package eu.arrowhead.core.plantdescriptionengine.utils;

import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.PlantDescriptionEntryDto;

import java.time.Instant;

public final class TestUtils {

    private static final String defaultName = "Plant Description 1A";

    private TestUtils() {
        throw new AssertionError();
    }

    public static PlantDescriptionEntryDto createEntry(final int id, final String name, final boolean active) {
        final Instant now = Instant.now();
        return new PlantDescriptionEntryDto.Builder()
            .id(id)
            .plantDescription(name)
            .active(active)
            .createdAt(now)
            .updatedAt(now)
            .build();
    }

    public static PlantDescriptionEntryDto createEntry(final int id, final boolean active) {
        return createEntry(id, defaultName, active);
    }

    public static PlantDescriptionEntryDto createEntry(final int id) {
        return createEntry(id, defaultName, true);
    }

}