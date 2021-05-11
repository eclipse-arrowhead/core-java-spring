package eu.arrowhead.core.plantdescriptionengine.pdtracker;

import eu.arrowhead.core.plantdescriptionengine.pdtracker.backingstore.PdStore;
import eu.arrowhead.core.plantdescriptionengine.pdtracker.backingstore.PdStoreException;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.Connection;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.PdeSystem;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.PlantDescriptionEntry;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.PlantDescriptionEntryDto;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.PlantDescriptionEntryListDto;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.Port;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Object used to keep track of Plant Description entries. It keeps a reference
 * to a {@link eu.arrowhead.core.plantdescriptionengine.pdtracker.backingstore},
 * which is used to store Plant Description Entries in some permanent storage
 * (e.g. to file or a database).
 */
public class PlantDescriptionTracker {

    // List of instances that need to be informed of any changes to Plant
    // Description Entries.
    final List<PlantDescriptionUpdateListener> listeners = new ArrayList<>();

    // ID-to-entry mapping
    private final Map<Integer, PlantDescriptionEntryDto> entries = new ConcurrentHashMap<>();

    // Non-volatile storage for entries:
    private final PdStore backingStore;

    // Integer for storing the next plant description entry ID to be used:
    private final AtomicInteger nextId = new AtomicInteger();

    /**
     * Class constructor.
     *
     * @param backingStore Non-volatile storage for entries.
     * @throws PdStoreException If backing store operations fail.
     */
    public PlantDescriptionTracker(final PdStore backingStore) throws PdStoreException {
        Objects.requireNonNull(backingStore, "Expected backing store");
        this.backingStore = backingStore;

        // Read entries from non-volatile storage and
        // calculate the next free Plant Description Entry ID:
        int maxId = -1;
        for (final PlantDescriptionEntryDto entry : backingStore.readEntries()) {
            maxId = Math.max(maxId, entry.id());
            entries.put(entry.id(), entry);
        }
        nextId.set(Math.round(maxId + 1));
    }

    /**
     * @return An unused Plant Description Entry ID.
     */
    public int getUniqueId() {
        int result;
        do {
            result = nextId.getAndIncrement();
        } while (entries.containsKey(result));
        return result;
    }

    /**
     * Stores the given entry in memory and in the backing store. Any registered
     * {@code PlantDescriptionUpdateListener} are notified.
     *
     * @param entry Entry to store in the map.
     * @throws PdStoreException If the entry is not successfully stored in
     *                          permanent storage. In this case, the entry will
     *                          not be stored in memory either, and no listeners
     *                          will be notified.
     */
    public void put(final PlantDescriptionEntryDto entry) throws PdStoreException {
        Objects.requireNonNull(entry, "Expected entry.");

        final PlantDescriptionEntry previouslyActiveEntry;
        final boolean anotherEntryWasActive;
        final boolean isNew;
        final PlantDescriptionEntry oldEntry;
        final PlantDescriptionEntryDto deactivatedEntry;

        synchronized (this) {

            backingStore.write(entry);

            previouslyActiveEntry = activeEntry();
            anotherEntryWasActive = previouslyActiveEntry != null && previouslyActiveEntry.id() != entry.id();
            isNew = !entries.containsKey(entry.id());
            oldEntry = isNew ? null : entries.get(entry.id());
            entries.put(entry.id(), entry);

            if (entry.active() && anotherEntryWasActive) {
                deactivatedEntry = PlantDescriptionEntry.deactivated(previouslyActiveEntry);
                entries.put(deactivatedEntry.id(), deactivatedEntry);
                backingStore.write(deactivatedEntry);
            } else {
                deactivatedEntry = null;
            }
        }

        if (deactivatedEntry != null) {
            for (final PlantDescriptionUpdateListener listener : listeners) {
                listener.onPlantDescriptionUpdated(deactivatedEntry, previouslyActiveEntry);
            }
        }

        for (final PlantDescriptionUpdateListener listener : listeners) {
            if (isNew) {
                listener.onPlantDescriptionAdded(entry);
            } else {
                listener.onPlantDescriptionUpdated(entry, oldEntry);
            }
        }
    }

    public PlantDescriptionEntryDto get(final int id) {
        return entries.get(id);
    }

    /**
     * Removes the specified Plant Description Entry. The entry is removed from
     * memory and from the backing store.
     *
     * @param id ID of the entry to remove.
     * @throws PdStoreException If the entry is not successfully removed from
     *                          permanent storage. In this case, the entry will
     *                          not be stored in memory either, and no listeners
     *                          will be notified.
     */
    public void remove(final int id) throws PdStoreException {
        backingStore.remove(id);
        final PlantDescriptionEntry entry = entries.remove(id);

        // Notify listeners:
        for (final PlantDescriptionUpdateListener listener : listeners) {
            listener.onPlantDescriptionRemoved(entry);
        }
    }

    /**
     * @return A list of current Plant Description Entries.
     */
    public List<PlantDescriptionEntryDto> getEntries() {
        return new ArrayList<>(entries.values());
    }

    /**
     * @return An object mapping entry ID:s to entries.
     */
    public Map<Integer, PlantDescriptionEntry> getEntryMap() {
        return new HashMap<>(entries);
    }

    /**
     * @return A data transfer object representing the current list of Plant
     * Description entries.
     */
    public PlantDescriptionEntryListDto getListDto() {
        final List<PlantDescriptionEntryDto> data = new ArrayList<>(entries.values());
        return new PlantDescriptionEntryListDto.Builder()
            .data(data)
            .count(data.size())
            .build();
    }

    /**
     * Registers another object to be notified whenever a Plant Description
     * Entry is added, updated or deleted.
     *
     * @param listener A Plant Description update listener.
     */
    public void addListener(final PlantDescriptionUpdateListener listener) {
        Objects.requireNonNull(listener, "Expected listener.");
        listeners.add(listener);
    }

    /**
     * @return The currently active entry, if any. Null there is none.
     */
    public PlantDescriptionEntry activeEntry() {
        for (final PlantDescriptionEntry entry : entries.values()) {
            if (entry.active()) {
                return entry;
            }
        }
        return null;
    }

    /**
     * @param entry    A Plant Description Entry.
     * @param systemId The ID of a system.
     * @return The system with the given ID, if it exists in the specified Plant
     * Description entry, or its chain of included entries. If the system is not
     * present, null is returned.
     */
    private PdeSystem getSystem(final PlantDescriptionEntry entry, final String systemId) {

        PdeSystem result = entry.systems()
            .stream()
            .filter(system -> system.systemId()
                .equals(systemId))
            .findAny()
            .orElse(null);

        if (result != null) {
            return result;
        }

        for (final int i : entry.include()) {
            final PlantDescriptionEntry includedEntry = get(i);
            result = getSystem(includedEntry, systemId);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    /**
     * @param systemId The ID of a system.
     * @return The system with the given ID, if it exists in the active Plant
     * Description entry, or its chain of included entries. If the system is not
     * present, an {@code IllegalArgumentException} is thrown. If no entry is
     * currently active, an {@code IllegalStateException} is thrown.
     */
    public PdeSystem getSystem(final String systemId) {
        Objects.requireNonNull(systemId, "Expected system ID.");

        final PlantDescriptionEntry activeEntry = activeEntry();
        if (activeEntry == null) {
            throw new IllegalStateException("No active Plant Description.");
        }

        final PdeSystem result = getSystem(activeEntry, systemId);
        if (result == null) {
            throw new IllegalArgumentException("Could not find system with ID '" + systemId + "'.");
        }
        return result;
    }

    /**
     * @param entry A Plant Description Entry.
     * @return A list of all systems in the specified entry, as well as all
     * systems in its chain of included entries.
     */
    private List<PdeSystem> getSystems(final PlantDescriptionEntry entry) {
        Objects.requireNonNull(entry, "Expected Plant Description Entry");

        final List<PdeSystem> systems = new ArrayList<>(entry.systems());

        for (final int i : entry.include()) {
            final PlantDescriptionEntry includedEntry = get(i);
            systems.addAll(getSystems(includedEntry));
        }

        return systems;
    }

    /**
     * @return A list of all systems in the active entry, as well as all systems
     * in its chain of included entries.
     */
    public List<PdeSystem> getActiveSystems() {
        return getSystems(activeEntry());
    }

    /**
     * @param entryId ID of a Plant Description Entry.
     * @return All connections in the specified Plant Description Entry and its
     * chain of included entries.
     */
    private List<Connection> getAllConnections(final int entryId) {
        final PlantDescriptionEntry entry = get(entryId);

        Objects.requireNonNull(
            entry, "Plant Description with ID " + entryId
                + " is not present in the Plant Description Tracker.");

        final List<Connection> connections = new ArrayList<>(entry.connections());

        for (final int i : entry.include()) {
            connections.addAll(getAllConnections(i));
        }

        return connections;
    }

    /**
     * @return All connections in the active Plant Description Entry and its
     * chain of included entries.
     */
    public List<Connection> getActiveConnections() {
        final PlantDescriptionEntry activeEntry = activeEntry();
        if (activeEntry == null) {
            return new ArrayList<>();
        }
        return getAllConnections(activeEntry().id());
    }

    /**
     * @param portName The name of a system port.
     * @return The service definition of the specified port, if it is present
     * among the systems of the active entry or its chain of included entries.
     */
    public String getServiceDefinition(final String portName) {
        Objects.requireNonNull(portName, "Expected port name.");

        final PlantDescriptionEntry activeEntry = activeEntry();

        if (activeEntry == null) {
            throw new IllegalStateException("No entry is currently active.");
        }

        final List<PdeSystem> systems = getActiveSystems();
        for (final PdeSystem system : systems) {
            for (final Port port : system.ports()) {
                if (portName.equals(port.portName())) {
                    return port.serviceDefinition();
                }
            }
        }
        throw new IllegalArgumentException(
            "No port named '" + portName + "' could be found in the Plant Description Tracker."
        );
    }

}