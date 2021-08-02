package eu.arrowhead.core.plantdescriptionengine.pdtracker;

import eu.arrowhead.core.plantdescriptionengine.pdtracker.backingstore.PdStore;
import eu.arrowhead.core.plantdescriptionengine.pdtracker.backingstore.PdStoreException;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.Connection;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.PdeSystem;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.PlantDescriptionEntry;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.PlantDescriptionEntryDto;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.PlantDescriptionEntryListDto;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.Port;
import se.arkalix.util.concurrent.Future;
import se.arkalix.util.concurrent.Futures;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

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

    private final Semaphore semaphore = new Semaphore(1);


    /**
     * Class constructor.
     *
     * @param backingStore Non-volatile storage for entries.
     * @throws PdStoreException If backing store operations fail.
     */
    public PlantDescriptionTracker(final PdStore backingStore) throws PdStoreException {
        Objects.requireNonNull(backingStore, "Expected backing store");
        this.backingStore = backingStore;
        readEntriesFromBackingStore();
    }

    private void readEntriesFromBackingStore() throws PdStoreException {
        int maxId = -1;
        entries.clear();
        for (final PlantDescriptionEntryDto entry : backingStore.readEntries()) {
            maxId = Math.max(maxId, entry.id());
            entries.put(entry.id(), entry);
        }
        nextId.set(maxId + 1);
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
     */
    public Future<?> put(final PlantDescriptionEntryDto entry) {
        Objects.requireNonNull(entry, "Expected entry.");

        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            return Future.failure(e);
        }

        try {
            final PlantDescriptionEntry previouslyActiveEntry = activeEntry();
            final boolean anotherEntryWasActive = previouslyActiveEntry != null && previouslyActiveEntry.id() != entry.id();
            final boolean isNew = !entries.containsKey(entry.id());
            final PlantDescriptionEntry oldEntry = isNew ? null : entries.get(entry.id());
            final PlantDescriptionEntryDto deactivatedEntry;

            entries.put(entry.id(), entry);

            if (entry.active() && anotherEntryWasActive) {
                deactivatedEntry = PlantDescriptionEntry.deactivated(previouslyActiveEntry);
                entries.put(deactivatedEntry.id(), deactivatedEntry);
                backingStore.write(deactivatedEntry);
            } else {
                deactivatedEntry = null;
            }

            Future<?> informListenersOfDeactivation = (deactivatedEntry == null)
                ? Future.done()
                : runOnUpdateListeners(deactivatedEntry, previouslyActiveEntry);

            return informListenersOfDeactivation
                .flatMap(result -> {
                    Future<?> informListenersOfUpdate = (isNew)
                        ? runOnAddedListeners(entry)
                        : runOnUpdateListeners(entry, oldEntry);
                    return informListenersOfUpdate;
                })
                .mapFault(Throwable.class, e -> {
                    // Something went wrong, restore to previous state:
                    readEntriesFromBackingStore();
                    return e;
                })
                .map(result -> {
                    backingStore.write(entry);
                    return null;
                })
                .always(result -> tryReleaseSemaphore());

        } catch (Exception e) {
            return Future.failure(e);
        } finally {
            tryReleaseSemaphore();
        }
    }

    private Future<Void> tryAcquireSemaphore() {
        try {
            semaphore.acquire();
            return Future.done();
        } catch (InterruptedException e) {
            return Future.failure(e);
        }
    }

    private void tryReleaseSemaphore() {
        if (semaphore.availablePermits() == 0) {
            semaphore.release();
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
     * @return A {@code Future} that will complete when the entry is removed.
     */
    public Future<?> remove(final int id) {
        return tryAcquireSemaphore()
            .flatMap(result -> {
                final PlantDescriptionEntry entry = entries.remove(id);
                return runOnRemoveListeners(entry);
            })
            .flatMapFault(Throwable.class, e -> {
                // Something went wrong, restore to previous state:
                readEntriesFromBackingStore();
                return Future.failure(e);
            })
            .flatMap(result -> {
                backingStore.remove(id);
                return Future.done();
            })
            .always(result -> semaphore.release());
    }

    private Future<?> runOnRemoveListeners(final PlantDescriptionEntry entry) {
        return Futures.serialize(listeners.stream()
            .map(listener -> listener.onPlantDescriptionRemoved(entry))
            .collect(Collectors.toList()));
    }

    private Future<?> runOnUpdateListeners(final PlantDescriptionEntry newState, final PlantDescriptionEntry oldState) {
        return Futures.serialize(listeners.stream()
            .map(listener -> listener.onPlantDescriptionUpdated(newState, oldState))
            .collect(Collectors.toList()));
    }

    private Future<?> runOnAddedListeners(final PlantDescriptionEntry entry) {
        return Futures.serialize(listeners.stream()
            .map(listener -> listener.onPlantDescriptionAdded(entry))
            .collect(Collectors.toList()));
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
    public List<PdeSystem> getAllSystems(final PlantDescriptionEntry entry) { // TODO: Pass ID instead?
        Objects.requireNonNull(entry, "Expected Plant Description Entry");

        final List<PdeSystem> systems = new ArrayList<>(entry.systems());

        for (final int i : entry.include()) {
            final PlantDescriptionEntry includedEntry = get(i);
            systems.addAll(getAllSystems(includedEntry));
        }

        return systems;
    }

    /**
     * @return A list of all systems in the active entry, as well as all systems
     * in its chain of included entries.
     */
    public List<PdeSystem> getActiveSystems() {
        return getAllSystems(activeEntry());
    }

    /**
     * @param entryId ID of a Plant Description Entry.
     * @return All connections in the specified Plant Description Entry and its
     * chain of included entries.
     */
    public List<Connection> getAllConnections(final int entryId) {
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