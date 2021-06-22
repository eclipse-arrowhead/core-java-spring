package eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto;

import se.arkalix.dto.DtoReadableAs;
import se.arkalix.dto.DtoToString;
import se.arkalix.dto.DtoWritableAs;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static se.arkalix.dto.DtoCodec.JSON;

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

    /**
     * @param description The plant description to base this entry on.
     * @param id          Identifier to be used for the new entry.
     * @return A new plant entry based on the given description.
     */
    static PlantDescriptionEntryDto from(final PlantDescriptionDto description, final int id) {
        final List<PdeSystemDto> systems = new ArrayList<>();
        final List<ConnectionDto> connections = new ArrayList<>();

        for (final PdeSystem system : description.systems()) {
            systems.add((PdeSystemDto) system);
        }

        for (final Connection connection : description.connections()) {
            connections.add((ConnectionDto) connection);
        }

        final Instant now = Instant.now();

        return new PlantDescriptionEntryDto.Builder()
            .id(id)
            .plantDescription(description.plantDescription())
            .active(description.active()
                .orElse(false))
            .include(description.include())
            .systems(systems)
            .connections(connections)
            .createdAt(now)
            .updatedAt(now)
            .build();
    }

    /**
     * @param entry A Plant Description Entry.
     * @return A copy of the given entry, with 'active' set to false.
     */
    static PlantDescriptionEntryDto deactivated(final PlantDescriptionEntry entry) {
        final List<PdeSystemDto> systems = new ArrayList<>();
        final List<ConnectionDto> connections = new ArrayList<>();

        for (final PdeSystem system : entry.systems()) {
            systems.add((PdeSystemDto) system);
        }

        for (final Connection connection : entry.connections()) {
            connections.add((ConnectionDto) connection);
        }

        return new PlantDescriptionEntryDto.Builder()
            .id(entry.id())
            .plantDescription(entry.plantDescription())
            .active(false)
            .include(entry.include())
            .systems(systems)
            .connections(connections)
            .createdAt(entry.createdAt())
            .updatedAt(Instant.now())
            .build();
    }

    /**
     * @param oldEntry  Target plant description entry to update.
     * @param newFields A plant description update.
     * @return A copy of the target plant description updated with the fields
     * specified in newFields.
     */
    static PlantDescriptionEntryDto update(
        final PlantDescriptionEntryDto oldEntry,
        final PlantDescriptionUpdateDto newFields
    ) {

        final PlantDescriptionEntryDto.Builder builder = new PlantDescriptionEntryDto.Builder()
            .id(oldEntry.id())
            .plantDescription(newFields.plantDescription()
                .orElse(oldEntry.plantDescription()))
            .active(newFields.active()
                .orElse(oldEntry.active()))
            .include(newFields.include()
                .orElse(oldEntry.include()))
            .createdAt(oldEntry.createdAt())
            .updatedAt(Instant.now());

        // The methods 'systems' and 'connections' return instances of
        // PdeSystem and Connection. This must be cast to their runtime-types
        // before they can be added to the old entry:
        final List<PdeSystemDto> systems = new ArrayList<>();
        for (final PdeSystem system : newFields.systems().orElse(oldEntry.systems())) {
            systems.add((PdeSystemDto) system);
        }
        builder.systems(systems);

        final List<ConnectionDto> connections = new ArrayList<>();
        for (final Connection connection : newFields.connections().orElse(oldEntry.connections())) {
            connections.add((ConnectionDto) connection);
        }
        builder.connections(connections);

        return builder.build();
    }

    private static void sort(
        final List<? extends PlantDescriptionEntry> entries,
        final Comparator<? super PlantDescriptionEntry> comparator,
        final boolean ascending
    ) {
        if (ascending) {
            entries.sort(comparator);
        } else {
            entries.sort(comparator.reversed());
        }
    }

    static void sortById(final List<? extends PlantDescriptionEntry> entries, final boolean ascending) {
        sort(entries, ID_COMPARATOR, ascending);
    }

    static void sortByCreatedAt(final List<? extends PlantDescriptionEntry> entries, final boolean ascending) {
        sort(entries, CREATED_AT_COMPARATOR, ascending);
    }

    static void sortByUpdatedAt(final List<? extends PlantDescriptionEntry> entries, final boolean ascending) {
        sort(entries, UPDATED_AT_COMPARATOR, ascending);
    }

    /**
     * Filters the given list based on the elements' 'active' values.
     *
     * @param entries A list of Plant Description entries.
     * @param active  If true, active entries are removed. If false, inactive
     *                entries are removed.
     */
    static void filterByActive(final List<? extends PlantDescriptionEntry> entries, final boolean active) {
        if (active) {
            entries.removeIf(entry -> !entry.active());
        } else {
            entries.removeIf(PlantDescriptionEntry::active);
        }
    }

    int id();

    String plantDescription();

    boolean active();

    List<Integer> include();

    List<PdeSystem> systems();

    List<Connection> connections();

    Instant createdAt();

    Instant updatedAt();

}