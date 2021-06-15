package eu.arrowhead.core.plantdescriptionengine.pdtracker.backingstore;

import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.PlantDescriptionEntryDto;

import java.util.List;

/**
 * Interface for objects that read and write Plant Description Entries to
 * permanent storage, e.g. to file or to a database.
 */
public interface PdStore {

    /**
     * @return A list of all entries currently in the backing store.
     */
    List<PlantDescriptionEntryDto> readEntries() throws PdStoreException;

    /**
     * Writes a single entry to backing store.
     *
     * @param entry An entry to store.
     */
    void write(final PlantDescriptionEntryDto entry) throws PdStoreException;

    /**
     * Delete the specified entry from the backing store.
     *
     * @param id ID of the entry to delete.
     */
    void remove(int id) throws PdStoreException;

}