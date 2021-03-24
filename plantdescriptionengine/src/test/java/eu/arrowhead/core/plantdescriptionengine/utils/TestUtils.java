package eu.arrowhead.core.plantdescriptionengine.utils;

import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.PlantDescriptionEntryBuilder;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.PlantDescriptionEntryDto;

import java.time.Instant;

public class TestUtils {

    public static PlantDescriptionEntryDto createEntry(int id, boolean active) {
        final Instant now = Instant.now();
        return new PlantDescriptionEntryBuilder()
            .id(id)
            .plantDescription("Plant Description 1A")
            .active(active)
            .createdAt(now)
            .updatedAt(now)
            .build();
    }

    public static PlantDescriptionEntryDto createEntry(int id) {
        return createEntry(id, true);
    }

}