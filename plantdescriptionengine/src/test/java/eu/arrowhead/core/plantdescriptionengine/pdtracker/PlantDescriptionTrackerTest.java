package eu.arrowhead.core.plantdescriptionengine.pdtracker;

import eu.arrowhead.core.plantdescriptionengine.pdtracker.backingstore.InMemoryPdStore;
import eu.arrowhead.core.plantdescriptionengine.pdtracker.backingstore.PdStore;
import eu.arrowhead.core.plantdescriptionengine.pdtracker.backingstore.PdStoreException;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.Connection;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.ConnectionDto;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.PdeSystem;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.PdeSystemDto;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.PlantDescriptionEntry;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.PlantDescriptionEntryDto;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.PlantDescriptionEntryListDto;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.PortDto;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.SystemPortDto;
import eu.arrowhead.core.plantdescriptionengine.utils.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit test for the {@link eu.arrowhead.core.plantdescriptionengine.pdtracker.PlantDescriptionTracker}
 * class.
 */
public class PlantDescriptionTrackerTest {

    final Instant now = Instant.now();

    PdStore store;
    PlantDescriptionTracker pdTracker;

    @BeforeEach
    public void initEach() throws PdStoreException {
        store = new InMemoryPdStore();
        pdTracker = new PlantDescriptionTracker(store);
    }

    @Test
    public void shouldReadEntriesFromBackingStore() throws PdStoreException {
        final List<Integer> entryIds = List.of(1, 2, 3);

        for (final int id : entryIds) {
            store.write(TestUtils.createEntry(id));
        }

        final PlantDescriptionTracker newPdTracker = new PlantDescriptionTracker(store);
        final List<PlantDescriptionEntryDto> storedEntries = newPdTracker.getEntries();

        assertEquals(3, storedEntries.size());
    }

    @Test
    public void shouldWriteEntriesToBackingStore() throws PdStoreException {
        final List<Integer> entryIds = List.of(23, 42, 888);

        for (final int id : entryIds) {
            pdTracker.put(TestUtils.createEntry(id));
        }

        final List<PlantDescriptionEntryDto> entries = store.readEntries();

        for (final int id : entryIds) {
            final var entry = entries.stream().filter(e -> e.id() == id).findAny();
            assertTrue(entry.isPresent());
        }


    }

    @Test
    public void shouldReturnEntryById() throws PdStoreException {
        final int entryId = 16;
        final PlantDescriptionEntryDto entry = TestUtils.createEntry(entryId);
        pdTracker.put(entry);

        final PlantDescriptionEntryDto storedEntry = pdTracker.get(entryId);

        assertNotNull(storedEntry);
        assertEquals(entryId, storedEntry.id(), 0);
    }

    @Test
    public void shouldReturnAllEntries() throws PdStoreException {

        final List<Integer> entryIds = List.of(16, 39, 244);

        for (final int id : entryIds) {
            pdTracker.put(TestUtils.createEntry(id));
        }

        final List<PlantDescriptionEntryDto> storedEntries = pdTracker.getEntries();

        assertEquals(entryIds.size(), storedEntries.size());

        for (final PlantDescriptionEntryDto entry : storedEntries) {
            assertTrue(entryIds.contains(entry.id()));
        }
    }

    @Test
    public void shouldReturnListDto() throws PdStoreException {

        final List<Integer> entryIds = List.of(16, 39, 244);
        for (final int id : entryIds) {
            pdTracker.put(TestUtils.createEntry(id));
        }

        final PlantDescriptionEntryListDto storedEntries = pdTracker.getListDto();

        assertEquals(entryIds.size(), storedEntries.data().size());

        for (final PlantDescriptionEntry entry : storedEntries.data()) {
            assertTrue(entryIds.contains(entry.id()));
        }
    }

    @Test
    public void shouldRemoveEntries() throws PdStoreException {
        final int entryId = 24;
        final PlantDescriptionEntryDto entry = TestUtils.createEntry(entryId);
        pdTracker.put(entry);
        pdTracker.remove(entryId);
        final PlantDescriptionEntry storedEntry = pdTracker.get(entryId);

        assertNull(storedEntry);
    }

    @Test
    public void shouldTrackActiveEntry() throws PdStoreException {
        final PlantDescriptionEntryDto.Builder builder = new PlantDescriptionEntryDto.Builder()
            .createdAt(now)
            .updatedAt(now);
        final PlantDescriptionEntryDto activeEntry = builder
            .id(1)
            .plantDescription("Plant Description A")
            .active(true)
            .build();
        final PlantDescriptionEntryDto inactiveEntry = builder
            .id(2)
            .plantDescription("Plant Description B")
            .active(false)
            .build();

        pdTracker.put(activeEntry);
        pdTracker.put(inactiveEntry);

        assertNotNull(pdTracker.activeEntry());
        assertEquals(activeEntry.id(), pdTracker.activeEntry().id(), 0);

        pdTracker.remove(activeEntry.id());

        assertNull(pdTracker.activeEntry());
    }

    @Test
    public void shouldDeactivateEntry() throws PdStoreException {
        final int idA = 1;
        final int idB = 2;
        final PlantDescriptionEntryDto.Builder builder = new PlantDescriptionEntryDto.Builder()
            .active(true)
            .createdAt(now)
            .updatedAt(now);
        final PlantDescriptionEntryDto entryA = builder.id(idA)
            .plantDescription("Plant Description A")
            .build();
        final PlantDescriptionEntryDto entryB = builder.id(idB)
            .plantDescription("Plant Description B")
            .build();

        pdTracker.put(entryA);
        assertTrue(pdTracker.get(idA).active());

        pdTracker.put(entryB);
        assertFalse(pdTracker.get(idA).active());
        assertTrue(pdTracker.get(idB).active());
    }

    @Test
    public void shouldGenerateUniqueIds() throws PdStoreException {

        final List<PlantDescriptionEntryDto> entries = List.of(TestUtils.createEntry(pdTracker.getUniqueId()),
            TestUtils.createEntry(pdTracker.getUniqueId()), TestUtils.createEntry(pdTracker.getUniqueId()));

        for (final PlantDescriptionEntryDto entry : entries) {
            pdTracker.put(entry);
        }

        final int uid = pdTracker.getUniqueId();

        for (final PlantDescriptionEntryDto entry : entries) {
            assertNotEquals(uid, entry.id());
        }
    }

    @Test
    public void shouldNotifyOnAdd() throws PdStoreException {

        final Listener listener = new Listener();

        final int idA = 16;
        final int idB = 32;

        pdTracker.addListener(listener);
        pdTracker.put(TestUtils.createEntry(idA));
        pdTracker.put(TestUtils.createEntry(idB));

        assertEquals(idB, listener.lastAdded.id());
        assertEquals(2, listener.numAdded);
    }

    @Test
    public void shouldNotifyOnDelete() throws PdStoreException {

        final Listener listener = new Listener();

        final int idA = 5;
        final int idB = 12;

        pdTracker.addListener(listener);
        pdTracker.put(TestUtils.createEntry(idA));
        pdTracker.put(TestUtils.createEntry(idB));
        pdTracker.remove(idA);

        assertEquals(idA, listener.lastRemoved.id());
        assertEquals(1, listener.numRemoved);
    }

    @Test
    public void shouldNotifyOnUpdate() throws PdStoreException {

        final Listener listener = new Listener();

        final int idA = 93;

        pdTracker.addListener(listener);
        pdTracker.put(TestUtils.createEntry(idA));

        // "Update" the entry by putting an identical copy in the tracker.
        pdTracker.put(TestUtils.createEntry(idA));

        assertEquals(idA, listener.lastUpdated.id());
        assertEquals(1, listener.numUpdated);
    }

    @Test
    public void shouldNotifyWhenActiveEntryChanges() throws PdStoreException {

        final Listener listener = new Listener();

        final int idA = 2;
        final int idB = 8;

        pdTracker.addListener(listener);

        // Add an active entry.
        pdTracker.put(TestUtils.createEntry(idA, true));
        assertEquals(idA, listener.lastAdded.id());
        assertTrue(listener.lastAdded.active());

        // Add another active entry.
        pdTracker.put(TestUtils.createEntry(idB, true));

        // Listeners should have been notified that the old one was deactivated.
        assertEquals(idA, listener.lastUpdated.id());
        assertFalse(listener.lastUpdated.active());

        assertEquals(1, listener.numUpdated);
    }

    @Test
    public void shouldReturnTheCorrectSystem() throws PdStoreException {
        final String idA = "Sys-A";
        final String idB = "Sys-B";
        final String idC = "Sys-C";

        final PdeSystemDto systemA = new PdeSystemDto.Builder()
            .systemId(idA)
            .build();

        final PdeSystemDto systemB = new PdeSystemDto.Builder()
            .systemId(idB)
            .build();

        final PdeSystemDto systemC = new PdeSystemDto.Builder()
            .systemId(idC)
            .build();

        final PlantDescriptionEntryDto entry = new PlantDescriptionEntryDto.Builder()
            .id(1)
            .plantDescription("ABC")
            .createdAt(now)
            .updatedAt(now)
            .active(true)
            .systems(List.of(systemA, systemB, systemC))
            .build();

        pdTracker.put(entry);

        assertEquals(idA, pdTracker.getSystem(idA).systemId());
        assertEquals(idB, pdTracker.getSystem(idB).systemId());
        assertEquals(idC, pdTracker.getSystem(idC).systemId());
    }

    @Test
    public void shouldThrowWhenNoEntryIsActive() throws PdStoreException {

        final PlantDescriptionEntryDto entry = new PlantDescriptionEntryDto.Builder()
            .id(1)
            .plantDescription("ABC")
            .createdAt(now)
            .updatedAt(now)
            .active(false)
            .build();

        pdTracker.put(entry);

        final Exception exception = assertThrows(IllegalStateException.class,
            () -> pdTracker.getSystem("ABC"));
        assertEquals("No active Plant Description.", exception.getMessage());
    }

    @Test
    public void shouldReturnSystemFromIncludedEntry() throws PdStoreException {

        final int entryIdA = 32;
        final int entryIdB = 8;
        final int entryIdC = 58;
        final int entryIdX = 97;

        final String systemIdA = "Sys-A";
        final String systemIdB = "Sys-B";
        final String systemIdC = "Sys-C";

        final PdeSystemDto systemA = new PdeSystemDto.Builder()
            .systemId(systemIdA)
            .build();

        final PdeSystemDto systemB = new PdeSystemDto.Builder()
            .systemId(systemIdB)
            .build();

        final PdeSystemDto systemC = new PdeSystemDto.Builder()
            .systemId(systemIdC)
            .build();

        final PlantDescriptionEntryDto entryA = new PlantDescriptionEntryDto.Builder()
            .id(entryIdA)
            .plantDescription("A")
            .createdAt(now)
            .updatedAt(now)
            .active(false)
            .systems(List.of(systemA, systemB))
            .build();

        final PlantDescriptionEntryDto entryB = new PlantDescriptionEntryDto.Builder()
            .id(entryIdB)
            .plantDescription("B")
            .createdAt(now)
            .updatedAt(now)
            .active(false)
            .include(List.of(entryIdA))
            .build();

        final PlantDescriptionEntryDto entryX = new PlantDescriptionEntryDto.Builder()
            .id(entryIdX)
            .plantDescription("X")
            .createdAt(now)
            .updatedAt(now)
            .active(false)
            .build();

        final PlantDescriptionEntryDto entryC = new PlantDescriptionEntryDto.Builder()
            .id(entryIdC)
            .plantDescription("C")
            .createdAt(now)
            .updatedAt(now)
            .active(true)
            .include(List.of(entryIdX, entryIdB))
            .systems(List.of(systemC))
            .build();

        pdTracker.put(entryA);
        pdTracker.put(entryB);
        pdTracker.put(entryC);
        pdTracker.put(entryX);

        assertEquals(systemIdA, pdTracker.getSystem(systemIdA)
            .systemId());
    }

    @Test
    public void shouldReturnAllSystems() throws PdStoreException {

        final int entryIdA = 32;
        final int entryIdB = 8;
        final int entryIdC = 58;

        final String systemIdA = "Sys-A";
        final String systemIdB = "Sys-B";
        final String systemIdC = "Sys-C";

        final PdeSystemDto systemA = new PdeSystemDto.Builder()
            .systemId(systemIdA)
            .build();

        final PdeSystemDto systemB = new PdeSystemDto.Builder()
            .systemId(systemIdB)
            .build();

        final PdeSystemDto systemC = new PdeSystemDto.Builder()
            .systemId(systemIdC)
            .build();

        final PlantDescriptionEntryDto entryA = new PlantDescriptionEntryDto.Builder()
            .id(entryIdA)
            .plantDescription("A")
            .createdAt(now)
            .updatedAt(now)
            .active(false)
            .systems(List.of(systemA, systemB))
            .build();

        final PlantDescriptionEntryDto entryB = new PlantDescriptionEntryDto.Builder().id(entryIdB)
            .plantDescription("B")
            .createdAt(now)
            .updatedAt(now)
            .active(false)
            .include(List.of(entryIdA))
            .build();

        final PlantDescriptionEntryDto entryC = new PlantDescriptionEntryDto.Builder()
            .id(entryIdC)
            .plantDescription("C")
            .createdAt(now)
            .updatedAt(now)
            .active(true)
            .include(List.of(entryIdB))
            .systems(List.of(systemC))
            .build();

        pdTracker.put(entryA);
        pdTracker.put(entryB);
        pdTracker.put(entryC);

        final List<PdeSystem> systems = pdTracker.getActiveSystems();

        assertEquals(3, systems.size());

        final PdeSystem retrievedA = systems.stream()
            .filter(system -> system.systemId().equals(systemIdA))
            .findFirst()
            .orElse(null);
        final PdeSystem retrievedB = systems.stream()
            .filter(system -> system.systemId().equals(systemIdB))
            .findFirst()
            .orElse(null);
        final PdeSystem retrievedC = systems.stream()
            .filter(system -> system.systemId().equals(systemIdC))
            .findFirst()
            .orElse(null);

        assertNotNull(retrievedA);
        assertNotNull(retrievedB);
        assertNotNull(retrievedC);
    }

    @Test
    public void shouldReturnAllConnections() throws PdStoreException {

        // First entry
        final int entryIdA = 0;
        final String consumerIdA = "Cons-A";
        final String consumerNameA = "consa";
        final String producerNameA = "proda";
        final String consumerPortA = "Cons-Port-A";
        final String producerPortA = "Prod-Port-A";
        final String producerIdA = "Prod-A";
        final String serviceDefinition = "monitorable";

        final List<PortDto> consumerPortsA = List.of(new PortDto.Builder()
            .portName(consumerPortA)
            .serviceInterface("HTTP-SECURE-JSON")
            .serviceDefinition(serviceDefinition)
            .consumer(true)
            .build());

        final List<PortDto> producerPortsA = List.of(new PortDto.Builder()
            .portName(producerPortA)
            .serviceInterface("HTTP-SECURE-JSON")
            .serviceDefinition(serviceDefinition)
            .consumer(false)
            .build());

        final PdeSystemDto consumerSystemA = new PdeSystemDto.Builder()
            .systemId(consumerIdA)
            .systemName(consumerNameA)
            .ports(consumerPortsA)
            .build();

        final PdeSystemDto producerSystemA = new PdeSystemDto.Builder().systemId(producerIdA)
            .systemName(producerNameA)
            .ports(producerPortsA)
            .build();

        final List<ConnectionDto> connectionsA = List.of(new ConnectionDto.Builder()
            .consumer(new SystemPortDto.Builder()
                .systemId(consumerIdA)
                .portName(consumerPortA)
                .build())
            .producer(new SystemPortDto.Builder()
                .systemId(producerIdA)
                .portName(producerPortA)
                .build())
            .build());

        final PlantDescriptionEntryDto entryA = new PlantDescriptionEntryDto.Builder()
            .id(entryIdA)
            .plantDescription("Plant Description A")
            .createdAt(now)
            .updatedAt(now)
            .active(false)
            .systems(List.of(consumerSystemA, producerSystemA))
            .connections(connectionsA)
            .build();

        // Second entry
        final int entryIdB = 1;
        final String consumerIdB = "Cons-B";
        final String consumerNameB = "consb";
        final String consumerPortB = "Cons-Port-B";

        final List<PortDto> consumerPortsB = List.of(new PortDto.Builder()
            .portName(consumerPortB)
            .serviceInterface("HTTP-SECURE-JSON")
            .serviceDefinition("monitorable")
            .consumer(true)
            .build());

        final PdeSystemDto consumerSystemB = new PdeSystemDto.Builder()
            .systemId(consumerIdB)
            .systemName(consumerNameB)
            .ports(consumerPortsB)
            .build();

        final List<ConnectionDto> connectionsB = List.of(new ConnectionDto.Builder()
            .consumer(new SystemPortDto.Builder()
                .systemId(consumerIdB)
                .portName(consumerPortB)
                .build())
            .producer(new SystemPortDto.Builder()
                .systemId(producerIdA)
                .portName(producerPortA)
                .build())
            .build());

        final PlantDescriptionEntryDto entryB = new PlantDescriptionEntryDto.Builder()
            .id(entryIdB)
            .plantDescription("Plant Description B")
            .createdAt(now)
            .updatedAt(now)
            .active(true)
            .include(List.of(entryIdA))
            .systems(List.of(consumerSystemB))
            .connections(connectionsB)
            .build();

        pdTracker.put(entryA);
        pdTracker.put(entryB);

        final List<Connection> retrievedConnections = pdTracker.getActiveConnections();
        assertEquals(2, retrievedConnections.size());

        final Connection connectionA = retrievedConnections.get(1);
        final Connection connectionB = retrievedConnections.get(0);

        assertEquals(consumerIdA, connectionA.consumer().systemId());
        assertEquals(consumerIdB, connectionB.consumer().systemId());
    }

    @Test
    public void shouldReturnEmptyListWhenNoEntryIsActive() {
        final List<Connection> connections = pdTracker.getActiveConnections();
        assertEquals(0, connections.size());
    }

    @Test
    public void shouldReturnServiceDefinition() throws PdStoreException {

        // First entry
        final int entryIdA = 0;
        final String consumerIdA = "Cons-A";
        final String consumerNameA = "consa";
        final String producerNameA = "proda";
        final String consumerPortA = "Cons-Port-A";
        final String producerPortA = "Prod-Port-A";
        final String producerIdA = "Prod-A";
        final String serviceDefinitionA = "SD-A";

        final List<PortDto> consumerPortsA = List.of(new PortDto.Builder()
            .portName(consumerPortA)
            .serviceInterface("HTTP-SECURE-JSON")
            .serviceDefinition("Service-XYZ")
            .consumer(true)
            .build());

        final List<PortDto> producerPortsA = List.of(new PortDto.Builder()
            .portName(producerPortA)
            .serviceInterface("HTTP-SECURE-JSON")
            .serviceDefinition(serviceDefinitionA)
            .consumer(false)
            .build());

        final PdeSystemDto consumerSystemA = new PdeSystemDto.Builder()
            .systemId(consumerIdA)
            .systemName(consumerNameA)
            .ports(consumerPortsA)
            .build();

        final PdeSystemDto producerSystemA = new PdeSystemDto.Builder()
            .systemId(producerIdA)
            .systemName(producerNameA)
            .ports(producerPortsA)
            .build();

        final PlantDescriptionEntryDto entryA = new PlantDescriptionEntryDto.Builder()
            .id(entryIdA)
            .plantDescription("Plant Description A")
            .createdAt(now)
            .updatedAt(now)
            .active(false)
            .systems(List.of(consumerSystemA, producerSystemA))
            .build();

        // Second entry
        final int entryIdB = 1;
        final String consumerIdB = "Cons-B";
        final String consumerNameB = "consb";
        final String consumerPortB = "Cons-Port-B";

        final List<PortDto> consumerPortsB = List.of(new PortDto.Builder()
            .portName(consumerPortB)
            .serviceInterface("HTTP-SECURE-JSON")
            .serviceDefinition("temperature")
            .consumer(true)
            .build());

        final PdeSystemDto consumerSystemB = new PdeSystemDto.Builder()
            .systemId(consumerIdB)
            .systemName(consumerNameB)
            .ports(consumerPortsB)
            .build();

        final PlantDescriptionEntryDto entryB = new PlantDescriptionEntryDto.Builder()
            .id(entryIdB)
            .plantDescription("Plant Description B")
            .createdAt(now)
            .updatedAt(now)
            .active(true)
            .include(List.of(entryIdA))
            .systems(List.of(consumerSystemB))
            .build();

        pdTracker.put(entryA);
        pdTracker.put(entryB);

        final String serviceDefinition = pdTracker.getServiceDefinition(producerPortA);
        assertEquals(serviceDefinitionA, serviceDefinition);
    }

    @Test
    public void shouldThrowWhenGettingSdFromNonexistentPort() throws PdStoreException {
        final int entryId = 98;
        final String nonexistentPort = "qwerty";

        final PlantDescriptionEntryDto entry = new PlantDescriptionEntryDto.Builder()
            .id(entryId)
            .plantDescription("Plant Description A")
            .createdAt(now)
            .updatedAt(now)
            .active(true)
            .build();

        pdTracker.put(entry);

        final Exception exception = assertThrows(IllegalArgumentException.class,
            () -> pdTracker.getServiceDefinition(nonexistentPort));
        assertEquals("No port named '" + nonexistentPort + "' could be found in the Plant Description Tracker.",
            exception.getMessage());
    }

    @Test
    public void shouldThrowWhenGettingSdNoActiveEntry() {
        final String nonexistentPort = "qwerty";
        final Exception exception = assertThrows(IllegalStateException.class,
            () -> pdTracker.getServiceDefinition(nonexistentPort));

        assertEquals("No entry is currently active.", exception.getMessage());
    }

    @Test
    public void shouldThrowWhenRetrievingMissingSystem() throws PdStoreException {
        final PlantDescriptionEntryDto entry = new PlantDescriptionEntryDto.Builder()
            .id(123)
            .plantDescription("Plant Description A")
            .createdAt(now)
            .updatedAt(now)
            .active(true)
            .build();
        final String systemId = "Nonexistent";

        pdTracker.put(entry);
        final Exception exception = assertThrows(IllegalArgumentException.class, () -> pdTracker.getSystem(systemId));

        assertEquals("Could not find system with ID '" + systemId + "'.", exception.getMessage());
    }

    @Test
    public void shouldReturnUniqueId() throws PdStoreException {
        final int idA = 33;
        final int idB = 65;
        final int idC = idB + 1;
        store = new InMemoryPdStore();
        store.write(TestUtils.createEntry(idA));
        store.write(TestUtils.createEntry(idB));
        pdTracker = new PlantDescriptionTracker(store);
        pdTracker.put(TestUtils.createEntry(idC));

        // An earlier, naive implementation of getUniqueId would return idC at
        // this point.
        final int nextId = pdTracker.getUniqueId();

        assertEquals(idC + 1, nextId);
    }

    static final class Listener implements PlantDescriptionUpdateListener {

        int numAdded;
        int numUpdated;
        int numRemoved;

        // Store the IDs of entries:
        PlantDescriptionEntry lastAdded;
        PlantDescriptionEntry lastUpdated;
        PlantDescriptionEntry lastRemoved;

        @Override
        public void onPlantDescriptionAdded(final PlantDescriptionEntry entry) {
            lastAdded = entry;
            numAdded++;
        }

        @Override
        public void onPlantDescriptionRemoved(final PlantDescriptionEntry entry) {
            lastRemoved = entry;
            numRemoved++;
        }

        @Override
        public void onPlantDescriptionUpdated(
            PlantDescriptionEntry updatedEntry,
            PlantDescriptionEntry oldEntry
        ) {
            lastUpdated = updatedEntry;
            numUpdated++;
        }
    }

}
