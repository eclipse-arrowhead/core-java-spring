package eu.arrowhead.core.plantdescriptionengine.pdtracker.backingstore;

import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.Connection;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.ConnectionDto;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.PdeSystem;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.PdeSystemDto;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.PlantDescriptionEntryDto;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.Port;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.PortDto;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.SystemPortDto;
import eu.arrowhead.core.plantdescriptionengine.utils.TestUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit test for the {@link eu.arrowhead.core.plantdescriptionengine.pdtracker.backingstore.SqlPdStore}
 * class.
 */
public class SqlPdStoreTest {

    private final String DRIVER_CLASS_NAME = "org.h2.Driver";
    private final String CONNECTION_URL = "jdbc:h2:mem:testdb";
    private final String USERNAME = "root";
    private final String PASSWORD = "password";
    private final int maxPdBytes = 200000;
    @Rule
    public ExpectedException thrown = ExpectedException.none();
    SqlPdStore store;

    @Before
    public void createStore() throws PdStoreException {
        store = new SqlPdStore(maxPdBytes);
        store.init(DRIVER_CLASS_NAME, CONNECTION_URL, USERNAME, PASSWORD);
    }

    @After
    public void clearStore() throws PdStoreException {
        store.removeAll();
    }

    @Test
    public void shouldRequireInitialization() throws PdStoreException {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("SqlPdStore has not been initialized.");
        new SqlPdStore(maxPdBytes).readEntries();
    }

    @Test
    public void shouldReadWithoutEntries() throws PdStoreException {
        final List<PlantDescriptionEntryDto> storedEntries = store.readEntries();
        assertTrue(storedEntries.isEmpty());
    }

    @Test
    public void shouldWriteAndReadEntries() throws PdStoreException {

        final List<Integer> entryIds = List.of(19, 22, 309);
        final String baseName = "Entry-";

        for (final int id : entryIds) {
            store.write(TestUtils.createEntry(id, baseName + id, false));
        }

        final List<PlantDescriptionEntryDto> storedEntries = store.readEntries();

        assertEquals(storedEntries.size(), entryIds.size());

        for (int i = 0; i < entryIds.size(); i++) {
            final PlantDescriptionEntryDto entry = storedEntries.get(i);
            int entryId = entryIds.get(i);
            assertEquals(entryId, entry.id());
            final String expectedEntryName = baseName + entryId;
            assertEquals(expectedEntryName, entry.plantDescription());
        }
    }

    @Test
    public void shouldWriteAndReadComplexEntries() throws PdStoreException {
        final Instant updatedAt = Instant.now();
        final Instant createdAt = updatedAt.minus(1, ChronoUnit.HOURS);

        final int entryId = 3;
        final String entryName = "Plant Description 1A";

        final String consumerId = "system_1";
        final String producerId = "system_2";

        final String consumerName = "System 1";
        final String producerName = "System 2";

        final String consumerPort = "port_1";
        final String producerPort = "port_2";

        final Map<String, String> consumerPortMetadata = Map.of("x", "1", "y", "2");

        final String serviceDefinition = "Service-X";
        final String serviceInterface = "HTTP-SECURE-JSON";

        final List<Integer> include = List.of(27, 31, 19);

        final List<PdeSystemDto> systems = List.of(
            new PdeSystemDto.Builder()
                .systemId(consumerId)
                .systemName(consumerName)
                .ports(new PortDto.Builder()
                    .portName(consumerPort)
                    .consumer(true)
                    .metadata(consumerPortMetadata)
                    .serviceDefinition(serviceDefinition)
                    .serviceInterface(serviceInterface)
                    .build())
                .build(),
            new PdeSystemDto.Builder()
                .systemId(producerId)
                .systemName(producerName)
                .ports(new PortDto.Builder()
                    .portName(producerPort)
                    .serviceDefinition(serviceDefinition)
                    .serviceInterface(serviceInterface)
                    .build())
                .build()
        );

        final List<ConnectionDto> connections = List.of(new ConnectionDto.Builder()
            .consumer(new SystemPortDto.Builder()
                .systemId(consumerId)
                .portName(consumerPort)
                .build())
            .producer(new SystemPortDto.Builder().systemId(producerId)
                .portName(producerPort)
                .build())
            .build());

        final PlantDescriptionEntryDto entry = new PlantDescriptionEntryDto.Builder()
            .id(entryId)
            .plantDescription(entryName)
            .active(false)
            .createdAt(createdAt)
            .updatedAt(updatedAt)
            .include(include)
            .systems(systems)
            .connections(connections)
            .build();

        store.write(entry);

        final PlantDescriptionEntryDto retrievedEntry = store.readEntries().get(0);
        assertEquals(entry.active(), retrievedEntry.active());
        assertEquals(entryName, retrievedEntry.plantDescription());
        assertEquals(createdAt, retrievedEntry.createdAt());
        assertEquals(updatedAt, retrievedEntry.updatedAt());
        assertEquals(include, entry.include());
        assertEquals(1, retrievedEntry.connections().size());

        final Connection connection = retrievedEntry.connections().get(0);
        assertEquals(producerId, connection.producer().systemId());
        assertEquals(producerPort, connection.producer().portName());
        assertEquals(consumerId, connection.consumer().systemId());
        assertEquals(consumerPort, connection.consumer().portName());

        assertEquals(2, retrievedEntry.systems().size());

        final PdeSystem retrievedConsumer = retrievedEntry.systems().get(0);
        assertEquals(consumerName, retrievedConsumer.systemName().orElse(null));
        assertEquals(consumerId, retrievedConsumer.systemId());
        assertEquals(1, retrievedConsumer.ports().size());

        final Port retrievedConsumerPort = retrievedConsumer.ports().get(0);
        assertEquals(consumerPort, retrievedConsumerPort.portName());
        assertEquals(true, retrievedConsumerPort.consumer().orElse(false));
        assertEquals(consumerPortMetadata, retrievedConsumerPort.metadata());

        final PdeSystem retrievedProducer = retrievedEntry.systems().get(1);
        assertEquals(producerName, retrievedProducer.systemName().orElse(null));
        assertEquals(producerId, retrievedProducer.systemId());
        assertEquals(1, retrievedProducer.ports().size());

        final Port retrievedProducerPort = retrievedProducer.ports().get(0);
        assertEquals(producerPort, retrievedProducerPort.portName());
        assertEquals(false, retrievedProducerPort.consumer().orElse(false));
        assertTrue(retrievedProducerPort.metadata().isEmpty());

    }

    @Test
    public void shouldRemoveEntries() throws PdStoreException {
        final List<Integer> entryIds = List.of(1, 2, 3);

        for (final int id : entryIds) {
            store.write(TestUtils.createEntry(id));
        }

        final int id0 = entryIds.get(0);
        store.remove(id0);

        final List<PlantDescriptionEntryDto> storedEntries = store.readEntries();
        assertEquals(storedEntries.size(), entryIds.size() - 1);

        for (final PlantDescriptionEntryDto entry : storedEntries) {
            assertTrue(entryIds.contains(entry.id()));
            assertNotEquals(entry.id(), id0);
        }
    }
}