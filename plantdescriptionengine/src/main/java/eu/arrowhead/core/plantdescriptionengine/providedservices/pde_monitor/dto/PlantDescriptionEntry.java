package eu.arrowhead.core.plantdescriptionengine.providedservices.pde_monitor.dto;

import se.arkalix.dto.DtoReadableAs;
import se.arkalix.dto.DtoToString;
import se.arkalix.dto.DtoWritableAs;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

import static se.arkalix.dto.DtoEncoding.JSON;

/**
 * Data Transfer Object (DTO) interface for plant descriptions.
 */
@DtoReadableAs(JSON)
@DtoWritableAs(JSON)
@DtoToString
public interface PlantDescriptionEntry {

    Comparator<PlantDescriptionEntry> ID_COMPARATOR = Comparator.comparingInt(PlantDescriptionEntry::id);

    Comparator<PlantDescriptionEntry> CREATED_AT_COMPARATOR = Comparator.comparing(PlantDescriptionEntry::createdAt);

    Comparator<PlantDescriptionEntry> UPDATED_AT_COMPARATOR = Comparator.comparing(PlantDescriptionEntry::updatedAt);

    static void sort(List<? extends PlantDescriptionEntry> entries, String sortField, boolean sortAscending) {

        Comparator<PlantDescriptionEntry> comparator;
        switch (sortField) {
            case "id":
                comparator = ID_COMPARATOR;
                break;
            case "createdAt":
                comparator = CREATED_AT_COMPARATOR;
                break;
            case "updatedAt":
                comparator = UPDATED_AT_COMPARATOR;
                break;
            default:
                throw new IllegalArgumentException(
                    "'" + sortField + "' is not a valid sort field for Plant Description Entries.");
        }

        if (sortAscending) {
            entries.sort(comparator);
        } else {
            entries.sort(comparator.reversed());
        }
    }

    int id();

    String plantDescription();

    boolean active();

    List<Integer> include();

    List<SystemEntry> systems();

    List<Connection> connections();

    Instant createdAt();

    Instant updatedAt();

}