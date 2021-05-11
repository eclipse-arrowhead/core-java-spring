package eu.arrowhead.core.plantdescriptionengine.pdtracker.backingstore;

import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.PlantDescriptionEntryDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Plant Description backing store that only stores data in memory.
 * <p>
 * Created for development purposes, not to be used in production.
 */
public class InMemoryPdStore implements PdStore {

    // ID-to-entry map:
    private final Map<Integer, PlantDescriptionEntryDto> entries = new ConcurrentHashMap<>();

    @Override
    public List<PlantDescriptionEntryDto> readEntries() {
        return new ArrayList<>(entries.values());
    }

    @Override
    public void write(final PlantDescriptionEntryDto entry) {
        Objects.requireNonNull(entry, "Expected entry");
        entries.put(entry.id(), entry);
    }

    @Override
    public void remove(final int id) {
        entries.remove(id);
    }

}