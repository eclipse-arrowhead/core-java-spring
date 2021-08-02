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
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import se.arkalix.util.concurrent.Future;
import se.arkalix.util.concurrent.Futures;

import java.time.Instant;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Unit test for the {@link eu.arrowhead.core.plantdescriptionengine.pdtracker.PlantDescriptionTracker}
 * class.
 */
public class PlantDescriptionTrackerTest {

    final Instant now = Instant.now();
    @Rule
    public ExpectedException thrown = ExpectedException.none();
    PdStore store;
    PlantDescriptionTracker pdTracker;

    @Before
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
    public void shouldWriteEntriesToBackingStore() {
        final List<Integer> entryIds = List.of(23, 42, 888);

        for (final int id : entryIds) {
            pdTracker.put(TestUtils.createEntry(id));
        }

        Futures.serialize(
            entryIds.stream().map(id -> pdTracker.put(TestUtils.createEntry(id)))
        )
            .ifSuccess(result -> {
                final List<PlantDescriptionEntryDto> entries = store.readEntries();
                for (final int id : entryIds) {
                    final var entry = entries.stream().filter(e -> e.id() == id).findAny();
                    assertTrue(entry.isPresent());
                }
            })
            .onFailure(e -> fail());
    }

    @Test
    public void shouldReturnEntryById() {
        final int entryId = 16;
        final PlantDescriptionEntryDto entry = TestUtils.createEntry(entryId);
        pdTracker.put(entry)
            .ifSuccess(result -> {
                final PlantDescriptionEntryDto storedEntry = pdTracker.get(entryId);
                assertNotNull(storedEntry);
                assertEquals(entryId, storedEntry.id());
            })
            .onFailure(e -> fail());
    }

    @Test
    public void shouldReturnAllEntries() {

        final List<Integer> entryIds = List.of(16, 39, 244);

        Futures.serialize(
            entryIds.stream().map(id -> pdTracker.put(TestUtils.createEntry(id)))
        )
            .ifSuccess(result -> {
                final List<PlantDescriptionEntryDto> storedEntries = pdTracker.getEntries();

                assertEquals(entryIds.size(), storedEntries.size());
                for (final PlantDescriptionEntryDto entry : storedEntries) {
                    assertTrue(entryIds.contains(entry.id()));
                }
            })
            .onFailure(e -> fail());
    }

    @Test
    public void shouldReturnListDto() {

        final List<Integer> entryIds = List.of(16, 39, 244);

        Futures.serialize(
            entryIds.stream().map(id -> pdTracker.put(TestUtils.createEntry(id)))
        )
            .ifSuccess(result -> {
                final PlantDescriptionEntryListDto storedEntries = pdTracker.getListDto();
                assertEquals(entryIds.size(), storedEntries.data().size());
                for (final PlantDescriptionEntry entry : storedEntries.data()) {
                    assertTrue(entryIds.contains(entry.id()));
                }
            })
            .onFailure(e -> fail());
    }

    @Test
    public void shouldRemoveEntries() {
        final int entryId = 24;
        final PlantDescriptionEntryDto entry = TestUtils.createEntry(entryId);
        pdTracker.put(entry)
            .flatMap(result -> pdTracker.remove(entryId))
            .ifSuccess(result -> {
                final PlantDescriptionEntry storedEntry = pdTracker.get(entryId);
                assertNull(storedEntry);
            });
    }

    @Test
    public void shouldTrackActiveEntry() {
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

        pdTracker.put(activeEntry)
            .flatMap(result -> pdTracker.put(inactiveEntry))
            .flatMap(result -> {
                assertNotNull(pdTracker.activeEntry());
                assertEquals(activeEntry.id(), pdTracker.activeEntry().id());
                return pdTracker.remove(activeEntry.id());
            })
            .ifSuccess(result -> assertNull(pdTracker.activeEntry()))
            .onFailure(e -> fail());
    }

    @Test
    public void shouldDeactivateEntry() {
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

        pdTracker.put(entryA)
            .flatMap(result -> {
                assertTrue(pdTracker.get(idA).active());
                return pdTracker.put(entryB);
            })
            .ifSuccess(result -> {
                assertFalse(pdTracker.get(idA).active());
                assertTrue(pdTracker.get(idB).active());
            })
            .onFailure(e -> fail());
    }

    @Test
    public void shouldGenerateUniqueIds() {

        final List<PlantDescriptionEntryDto> entries = List.of(
            TestUtils.createEntry(pdTracker.getUniqueId()),
            TestUtils.createEntry(pdTracker.getUniqueId()),
            TestUtils.createEntry(pdTracker.getUniqueId())
        );

        Futures.serialize(
            entries.stream().map(entry -> pdTracker.put(entry))
        )
            .ifSuccess(result -> {
                final int uid = pdTracker.getUniqueId();
                for (final PlantDescriptionEntryDto entry : entries) {
                    assertNotEquals(uid, entry.id());
                }
            })
            .onFailure(e -> fail());
    }

    @Test
    public void shouldNotifyOnAdd() {

        final Listener listener = new Listener();

        final int idA = 16;
        final int idB = 32;

        pdTracker.addListener(listener);
        pdTracker.put(TestUtils.createEntry(idA))
            .flatMap(result -> pdTracker.put(TestUtils.createEntry(idB)))
            .ifSuccess(result -> {
                assertEquals(idB, listener.lastAdded.id());
                assertEquals(2, listener.numAdded);
            })
            .onFailure(e -> fail());
    }

    @Test
    public void shouldNotifyOnDelete() {

        final Listener listener = new Listener();

        final int idA = 5;
        final int idB = 12;

        pdTracker.addListener(listener);
        pdTracker.put(TestUtils.createEntry(idA))
            .flatMap(result -> pdTracker.put(TestUtils.createEntry(idB)))
            .flatMap(result -> pdTracker.remove(idA))
            .ifSuccess(result -> {
                assertEquals(idA, listener.lastRemoved.id());
                assertEquals(1, listener.numRemoved);
            })
            .onFailure(e -> {
                e.printStackTrace();
                fail();
            });
    }

    @Test
    public void shouldNotifyOnUpdate() {

        final Listener listener = new Listener();

        final int idA = 93;

        pdTracker.addListener(listener);
        pdTracker.put(TestUtils.createEntry(idA))
            // "Update" the entry by putting an identical copy in the tracker.
            .flatMap(result -> pdTracker.put(TestUtils.createEntry(idA)))
            .ifSuccess(result -> {
                assertEquals(idA, listener.lastUpdated.id());
                assertEquals(1, listener.numUpdated);
            })
            .onFailure(e -> fail());
    }

    @Test
    public void shouldNotifyWhenActiveEntryChanges() {

        final Listener listener = new Listener();

        final int idA = 2;
        final int idB = 8;

        pdTracker.addListener(listener);

        // Add an active entry.
        pdTracker.put(TestUtils.createEntry(idA, true))
            .flatMap(result -> {
                assertEquals(idA, listener.lastAdded.id());
                assertTrue(listener.lastAdded.active());

                // Add another active entry.
                return pdTracker.put(TestUtils.createEntry(idB, true));
            })
            .ifSuccess(result -> {
                // Listeners should have been notified that the old one was deactivated.
                assertEquals(idA, listener.lastUpdated.id());
                assertFalse(listener.lastUpdated.active());
                assertEquals(1, listener.numUpdated);
            })
            .onFailure(e -> fail());
    }

    @Test
    public void shouldReturnTheCorrectSystem() {
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

        pdTracker.put(entry)
            .ifSuccess(result -> {
                assertEquals(idA, pdTracker.getSystem(idA).systemId());
                assertEquals(idB, pdTracker.getSystem(idB).systemId());
                assertEquals(idC, pdTracker.getSystem(idC).systemId());
            })
            .onFailure(e -> fail());
    }

    @Test
    public void shouldThrowWhenNoEntryIsActive() {

        final PlantDescriptionEntryDto entry = new PlantDescriptionEntryDto.Builder()
            .id(1)
            .plantDescription("ABC")
            .createdAt(now)
            .updatedAt(now)
            .active(false)
            .build();

        pdTracker.put(entry)
            .map(result -> pdTracker.getSystem("ABC"))
            .ifSuccess(result -> fail())
            .onFailure(e -> assertEquals("No active Plant Description.", e.getMessage()));
    }

    @Test
    public void shouldReturnSystemFromIncludedEntry() {

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

        pdTracker.put(entryA)
            .flatMap(result -> pdTracker.put(entryB))
            .flatMap(result -> pdTracker.put(entryC))
            .flatMap(result -> pdTracker.put(entryX))
            .ifSuccess(result -> assertEquals(systemIdA, pdTracker.getSystem(systemIdA).systemId()))
            .onFailure(e -> fail());
    }

    @Test
    public void shouldReturnAllSystems() {

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

        pdTracker.put(entryA)
            .flatMap(result -> pdTracker.put(entryB))
            .flatMap(result -> pdTracker.put(entryC))
            .ifSuccess(result -> {
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

            })
            .onFailure(e -> fail());
    }

    @Test
    public void shouldReturnAllConnections() {

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

        pdTracker.put(entryA)
            .flatMap(result -> pdTracker.put(entryB))
            .ifSuccess(result -> {
                final List<Connection> retrievedConnections = pdTracker.getActiveConnections();
                assertEquals(2, retrievedConnections.size());

                final Connection connectionA = retrievedConnections.get(1);
                final Connection connectionB = retrievedConnections.get(0);

                assertEquals(consumerIdA, connectionA.consumer().systemId());
                assertEquals(consumerIdB, connectionB.consumer().systemId());
            })
            .onFailure(e -> fail());
    }

    @Test
    public void shouldReturnEmptyListWhenNoEntryIsActive() {
        final List<Connection> connections = pdTracker.getActiveConnections();
        assertEquals(0, connections.size());
    }

    @Test
    public void shouldReturnServiceDefinition() {

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

        pdTracker.put(entryA)
            .flatMap(result -> pdTracker.put(entryB))
            .ifSuccess(result -> {
                final String serviceDefinition = pdTracker.getServiceDefinition(producerPortA);
                assertEquals(serviceDefinitionA, serviceDefinition);
            })
            .onFailure(e -> fail());
    }

    @Test
    public void shouldThrowWhenGettingSdFromNonexistentPort() {
        final int entryId = 98;
        final String nonexistentPort = "qwerty";

        final PlantDescriptionEntryDto entry = new PlantDescriptionEntryDto.Builder()
            .id(entryId)
            .plantDescription("Plant Description A")
            .createdAt(now)
            .updatedAt(now)
            .active(true)
            .build();

        pdTracker.put(entry)
            .map(result -> pdTracker.getServiceDefinition(nonexistentPort))
            .ifSuccess(result -> fail())
            .onFailure(e -> {
                final String expectedMessage = "No port named '" + nonexistentPort +
                    "' could be found in the Plant Description Tracker.";
                assertEquals(expectedMessage, e.getMessage());
            });
    }

    @Test
    public void shouldThrowWhenGettingSdNoActiveEntry() {
        final String nonexistentPort = "qwerty";
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("No entry is currently active.");
        pdTracker.getServiceDefinition(nonexistentPort);
    }

    @Test
    public void shouldThrowWhenRetrievingMissingSystem() {
        final PlantDescriptionEntryDto entry = new PlantDescriptionEntryDto.Builder()
            .id(123)
            .plantDescription("Plant Description A")
            .createdAt(now)
            .updatedAt(now)
            .active(true)
            .build();
        final String systemId = "Nonexistent";

        pdTracker.put(entry)
            .map(result -> pdTracker.getSystem(systemId))
            .ifSuccess(result -> fail())
            .onFailure(e -> assertEquals("Could not find system with ID '" + systemId + "'.", e.getMessage()));
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
        pdTracker.put(TestUtils.createEntry(idC))
            .ifSuccess(result -> {
                // An earlier, naive implementation of getUniqueId would return
                // idC at this point.
                final int nextId = pdTracker.getUniqueId();
                assertEquals(idC + 1, nextId);
            })
            .onFailure(e -> fail());

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
        public Future<Void> onPlantDescriptionAdded(final PlantDescriptionEntry entry) {
            lastAdded = entry;
            numAdded++;
            return Future.done();
        }

        @Override
        public Future<Void> onPlantDescriptionRemoved(final PlantDescriptionEntry entry) {
            lastRemoved = entry;
            numRemoved++;
            return Future.done();
        }

        @Override
        public Future<Void> onPlantDescriptionUpdated(
            PlantDescriptionEntry updatedEntry,
            PlantDescriptionEntry oldEntry
        ) {
            lastUpdated = updatedEntry;
            numUpdated++;
            return Future.done();
        }
    }

}
