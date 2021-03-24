package eu.arrowhead.core.plantdescriptionengine.pdtracker;

import eu.arrowhead.core.plantdescriptionengine.pdtracker.backingstore.PdStore;
import eu.arrowhead.core.plantdescriptionengine.pdtracker.backingstore.PdStoreException;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.*;

import java.util.*;
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
    public PlantDescriptionTracker(PdStore backingStore) throws PdStoreException {
        Objects.requireNonNull(backingStore, "Expected backing store");
        this.backingStore = backingStore;

        // Read entries from non-volatile storage and
        // calculate the next free Plant Description Entry ID:
        int maxId = -1;
        for (var entry : backingStore.readEntries()) {
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
     * @throws PdStoreException If the entry is not successfully stored in permanent
     *                          storage. In this case, the entry will not be stored
     *                          in memory either, and no listeners will be notified.
     */
    public void put(final PlantDescriptionEntryDto entry) throws PdStoreException {
        backingStore.write(entry);

        final boolean isNew = !entries.containsKey(entry.id());
        final var currentlyActive = activeEntry();

        boolean anotherEntryIsActive = (currentlyActive != null && currentlyActive.id() != entry.id());
        if (entry.active() && anotherEntryIsActive) {
            // Deactivate the currently active entry:
            final var deactivatedEntry = PlantDescriptionEntry.deactivated(currentlyActive);
            entries.put(deactivatedEntry.id(), deactivatedEntry);
            for (var listener : listeners) {
                listener.onPlantDescriptionUpdated(deactivatedEntry);
            }
        }

        entries.put(entry.id(), entry);

        for (var listener : listeners) {
            if (isNew) {
                listener.onPlantDescriptionAdded(entry);
            } else {
                listener.onPlantDescriptionUpdated(entry);
            }
        }
    }

    public PlantDescriptionEntryDto get(int id) {
        return entries.get(id);
    }

    /**
     * Removes the specified Plant Description Entry. The entry is removed from
     * memory and from the backing store.
     *
     * @param id ID of the entry to remove.
     * @throws PdStoreException If the entry is not successfully removed from
     *                          permanent storage. In this case, the entry will not
     *                          be stored in memory either, and no listeners will be
     *                          notified.
     */
    public void remove(int id) throws PdStoreException {
        backingStore.remove(id);
        var entry = entries.remove(id);

        // Notify listeners:
        for (var listener : listeners) {
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
        var data = new ArrayList<>(entries.values());
        return new PlantDescriptionEntryListBuilder()
            .data(data)
            .count(data.size())
            .build();
    }

    /**
     * Registers another object to be notified whenever a Plant Description Entry is
     * added, updated or deleted.
     *
     * @param listener A Plant Description update listener.
     */
    public void addListener(PlantDescriptionUpdateListener listener) {
        listeners.add(listener);
    }

    /**
     * @return The currently active entry, if any. Null there is none.
     */
    public PlantDescriptionEntry activeEntry() {
        for (var entry : entries.values()) {
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
     * Description entry, or its chain of included entries. If the system is
     * not present, null is returned.
     */
    private PdeSystem getSystem(PlantDescriptionEntry entry, String systemId) {

        PdeSystem result = entry.systems()
            .stream()
            .filter(system -> system.systemId()
                .equals(systemId))
            .findAny()
            .orElse(null);

        if (result != null) {
            return result;
        }

        // TODO: In what order do we want entries to be investigated? If we
        // don't allow duplicate systems, which we probably shouldn't, the order
        // doesn't matter.
        for (int i : entry.include()) {
            final var includedEntry = get(i);
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
     * Description entry, or its chain of included entries. If the system is
     * not present, an {@code IllegalArgumentException} is thrown.
     * If no entry is currently active, an {@code IllegalStateException} is
     * thrown.
     */
    public PdeSystem getSystem(String systemId) {
        final var activeEntry = activeEntry();
        if (activeEntry == null) {
            throw new IllegalStateException("No active Plant Description.");
        }

        final var result = getSystem(activeEntry, systemId);
        if (result == null) {
            throw new IllegalArgumentException("Could not find system with ID '" + systemId + "'.");
        }
        return result;
    }

    /**
     * @param entry A Plant Description Entry.
     * @return A list of all systems in the specified entry, as well as all systems
     * in its chain of included entries.
     */
    private List<PdeSystem> getSystems(PlantDescriptionEntry entry) {
        Objects.requireNonNull(entry, "Expected Plant Description Entry");

        List<PdeSystem> systems = new ArrayList<>(entry.systems());

        for (int i : entry.include()) {
            final var includedEntry = get(i);
            systems.addAll(getSystems(includedEntry));
        }

        return systems;
    }

    /**
     * @return A list of all systems in the active entry, as well as all systems in
     * its chain of included entries.
     */
    public List<PdeSystem> getActiveSystems() {
        return getSystems(activeEntry());
    }

    /**
     * @param entryId ID of a Plant Description Entry.
     * @return All connections in the specified Plant Description Entry and its
     * chain of included entries.
     */
    private List<Connection> getAllConnections(int entryId) {
        final var entry = get(entryId);

        Objects.requireNonNull(
            entry, "Plant Description with ID " + entryId + " is not present in the Plant Description Tracker."
        );

        List<Connection> connections = new ArrayList<>(entry.connections());

        for (int i : entry.include()) {
            connections.addAll(getAllConnections(i));
        }

        return connections;
    }

    /**
     * @return All connections in the active Plant Description Entry and its chain
     * of included entries.
     */
    public List<Connection> getActiveConnections() {
        final var activeEntry = activeEntry();
        if (activeEntry == null) {
            return new ArrayList<>();
        }
        return getAllConnections(activeEntry().id());
    }

    /**
     * @param portName The name of a system port.
     * @return The service definition of the specified port, if it is present among
     * the systems of the active entry or its chain of included entries.
     */
    public String getServiceDefinition(String portName) {
        final var activeEntry = activeEntry();

        if (activeEntry == null) {
            throw new IllegalStateException("No entry is currently active.");
        }

        List<PdeSystem> systems = getActiveSystems();
        for (var system : systems) {
            for (var port : system.ports()) {
                if (portName.equals(port.portName())) {
                    return port.serviceDefinition();
                }
            }
        }
        throw new IllegalArgumentException(
            "No port named '" + portName + "' could be found in the Plant Description Tracker.");
    }

}