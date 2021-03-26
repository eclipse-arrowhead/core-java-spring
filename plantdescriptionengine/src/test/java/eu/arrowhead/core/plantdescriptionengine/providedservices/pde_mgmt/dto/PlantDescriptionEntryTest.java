package eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PlantDescriptionEntryTest {

    final Instant now = Instant.now();

    @Test
    public void shouldFilterEntries() {
        final List<PlantDescriptionEntry> original = Arrays.asList(
            new PlantDescriptionEntryBuilder()
                .id(1)
                .plantDescription("Plant Description 1A")
                .createdAt(now)
                .updatedAt(now)
                .active(false)
                .build(),
            new PlantDescriptionEntryBuilder()
                .id(2)
                .plantDescription("Plant Description 1A")
                .createdAt(now)
                .updatedAt(now)
                .active(true)
                .build(),
            new PlantDescriptionEntryBuilder()
                .id(3)
                .plantDescription("Plant Description 1A")
                .createdAt(now)
                .updatedAt(now)
                .active(false)
                .build(),
            new PlantDescriptionEntryBuilder()
                .id(4)
                .plantDescription("Plant Description 1A")
                .createdAt(now)
                .updatedAt(now)
                .active(true)
                .build());

        final ArrayList<PlantDescriptionEntry> listA = new ArrayList<>(original);

        PlantDescriptionEntry.filterByActive(listA, true);
        assertEquals(2, listA.size());
        assertEquals(2, listA.get(0).id());
        assertEquals(4, listA.get(1).id());

        final ArrayList<PlantDescriptionEntry> listB = new ArrayList<>(original);

        PlantDescriptionEntry.filterByActive(listB, false);
        assertEquals(2, listB.size());
        assertEquals(1, listB.get(0).id());
        assertEquals(3, listB.get(1).id());
    }

    @Test
    public void shouldUpdateConnections() {

        final String consumerId = "system_1";
        final String producerId = "system_2";
        final String portName = "port_1";
        final String serviceDefinition = "service_a";
        final String serviceInterface = "HTTP-SECURE-JSON";

        final List<PortDto> consumerPorts = List.of(new PortBuilder()
            .portName(portName)
            .serviceInterface(serviceInterface)
            .serviceDefinition(serviceDefinition)
            .consumer(true)
            .build());

        final List<PortDto> producerPorts = List.of(new PortBuilder()
            .portName(portName)
            .serviceInterface(serviceInterface)
            .serviceDefinition(serviceDefinition)
            .consumer(false)
            .build());

        final PdeSystemDto consumerSystem = new PdeSystemBuilder()
            .systemId(consumerId)
            .ports(consumerPorts)
            .build();

        final PdeSystemDto producerSystem = new PdeSystemBuilder()
            .systemId(producerId)
            .ports(producerPorts)
            .build();

        final PlantDescriptionEntryDto entry = new PlantDescriptionEntryBuilder()
            .id(1)
            .plantDescription("Plant Description 1A")
            .createdAt(now)
            .updatedAt(now)
            .active(true)
            .include(new ArrayList<>())
            .systems(List.of(consumerSystem, producerSystem))
            .connections(List.of())
            .build();

        final List<ConnectionDto> newConnections = List.of(new ConnectionBuilder()
            .consumer(new SystemPortBuilder()
                .systemId(consumerId)
                .portName(portName)
                .build())
            .producer(new SystemPortBuilder()
                .systemId(producerId)
                .portName(portName)
                .build())
            .build());

        final PlantDescriptionUpdateDto newFields = new PlantDescriptionUpdateBuilder()
            .connections(newConnections)
            .build();
        final PlantDescriptionEntryDto updated = PlantDescriptionEntry.update(entry, newFields);
        assertEquals(1, updated.connections().size());
        final Connection connection = updated.connections().get(0);
        assertEquals(portName, connection.consumer().portName());
        assertEquals(consumerId, connection.consumer().systemId());
        assertEquals(portName, connection.producer().portName());
        assertEquals(producerId, connection.producer().systemId());
    }

    @Test
    public void shouldRemoveConnections() {

        final String consumerId = "system_1";
        final String producerId = "system_2";
        final String portName = "port_1";
        final String serviceDefinition = "service_a";
        final String serviceInterface = "HTTP-SECURE-JSON";

        final List<PortDto> consumerPorts = List.of(new PortBuilder()
            .portName(portName)
            .serviceInterface(serviceInterface)
            .serviceDefinition(serviceDefinition)
            .consumer(true)
            .build());

        final List<PortDto> producerPorts = List.of(new PortBuilder()
            .portName(portName)
            .serviceInterface(serviceInterface)
            .serviceDefinition(serviceDefinition)
            .consumer(false)
            .build());

        final PdeSystemDto consumerSystem = new PdeSystemBuilder()
            .systemId(consumerId)
            .ports(consumerPorts)
            .build();

        final PdeSystemDto producerSystem = new PdeSystemBuilder()
            .systemId(producerId)
            .ports(producerPorts)
            .build();

        final List<ConnectionDto> connections = List.of(new ConnectionBuilder()
            .consumer(new SystemPortBuilder()
                .systemId(consumerId)
                .portName(portName)
                .build())
            .producer(new SystemPortBuilder()
                .systemId(producerId)
                .portName(portName)
                .build())
            .build());

        final PlantDescriptionEntryDto entry = new PlantDescriptionEntryBuilder()
            .id(1)
            .plantDescription("Plant Description 1A")
            .createdAt(now)
            .updatedAt(now)
            .active(true)
            .include(new ArrayList<>())
            .systems(List.of(consumerSystem, producerSystem))
            .connections(connections)
            .build();

        final PlantDescriptionUpdateDto newFields = new PlantDescriptionUpdateBuilder()
            .connections(List.of())
            .build();
        final PlantDescriptionEntryDto updated = PlantDescriptionEntry.update(entry, newFields);
        assertEquals(0, updated.connections().size());
    }

    @Test
    public void shouldReplaceConnections() {

        final String consumerId = "system_1";
        final String producerId = "system_2";
        final String portNameA = "port_a";
        final String portNameB = "port_b";
        final String portNameC = "port_c";
        final String serviceDefinition = "service_a";
        final String serviceInterface = "HTTP-SECURE-JSON";

        final List<PortDto> consumerPorts = List.of(
            new PortBuilder().portName(portNameA)
                .serviceDefinition(serviceDefinition)
                .serviceInterface(serviceInterface)
                .consumer(true)
                .build(),
            new PortBuilder().portName(portNameB)
                .serviceDefinition(serviceDefinition)
                .serviceInterface(serviceInterface)
                .consumer(true)
                .build(),
            new PortBuilder().portName(portNameC)
                .serviceDefinition(serviceDefinition)
                .serviceInterface(serviceInterface)
                .consumer(true)
                .build());

        final List<PortDto> producerPorts = List.of(new PortBuilder()
            .portName(portNameA)
            .serviceInterface(serviceInterface)
            .serviceDefinition(serviceDefinition)
            .consumer(false)
            .build());

        final PdeSystemDto consumerSystem = new PdeSystemBuilder()
            .systemId(consumerId)
            .ports(consumerPorts)
            .build();

        final PdeSystemDto producerSystem = new PdeSystemBuilder()
            .systemId(producerId)
            .ports(producerPorts)
            .build();

        final List<ConnectionDto> connections = List.of(new ConnectionBuilder()
            .consumer(new SystemPortBuilder()
                .systemId(consumerId)
                .portName(portNameA)
                .build())
            .producer(new SystemPortBuilder()
                .systemId(producerId)
                .portName(portNameA)
                .build())
            .build());

        final PlantDescriptionEntryDto entry = new PlantDescriptionEntryBuilder()
            .id(1)
            .plantDescription("Plant Description 1A")
            .createdAt(now)
            .updatedAt(now)
            .active(true)
            .include(new ArrayList<>())
            .systems(List.of(consumerSystem, producerSystem))
            .connections(connections)
            .build();

        final List<ConnectionDto> newConnections = List.of(
            new ConnectionBuilder()
                .consumer(new SystemPortBuilder()
                    .systemId(consumerId)
                    .portName(portNameB)
                    .build())
                .producer(new SystemPortBuilder()
                    .systemId(producerId)
                    .portName(portNameA)
                    .build())
                .build(),
            new ConnectionBuilder()
                .consumer(new SystemPortBuilder()
                    .systemId(consumerId)
                    .portName(portNameC)
                    .build())
                .producer(new SystemPortBuilder()
                    .systemId(producerId)
                    .portName(portNameA)
                    .build())
                .build());

        final PlantDescriptionUpdateDto newFields = new PlantDescriptionUpdateBuilder()
            .connections(newConnections)
            .build();

        final PlantDescriptionEntryDto updated = PlantDescriptionEntry.update(entry, newFields);

        assertEquals(2, updated.connections().size());


        final Connection connectionB = updated.connections().get(0);
        final Connection connectionC = updated.connections().get(1);

        assertEquals(portNameB, connectionB.consumer().portName());
        assertEquals(portNameA, connectionB.producer().portName());

        assertEquals(portNameC, connectionC.consumer().portName());
        assertEquals(portNameA, connectionC.producer().portName());
    }

    @Test
    public void shouldSortCorrectly() {
        final int idA = 24;
        final int idB = 65;
        final int idC = 9;

        final Instant t1 = Instant.now();
        final Instant t2 = t1.plus(1, ChronoUnit.HOURS);
        final Instant t3 = t1.plus(2, ChronoUnit.HOURS);
        final Instant t4 = t1.plus(3, ChronoUnit.HOURS);

        final PlantDescriptionEntryDto entryA = new PlantDescriptionEntryBuilder()
            .id(idA)
            .plantDescription("A")
            .createdAt(t3)
            .updatedAt(t3)
            .active(false)
            .build();

        final PlantDescriptionEntryDto entryB = new PlantDescriptionEntryBuilder()
            .id(idB)
            .plantDescription("B")
            .createdAt(t1)
            .updatedAt(t1)
            .active(false)
            .build();
        final PlantDescriptionEntryDto entryC = new PlantDescriptionEntryBuilder()
            .id(idC)
            .plantDescription("C")
            .createdAt(t2)
            .updatedAt(t4)
            .active(false)
            .build();

        final List<PlantDescriptionEntry> entries = Arrays.asList(entryA, entryB, entryC);

        PlantDescriptionEntry.sortByCreatedAt(entries, true);
        assertEquals(idB, entries.get(0).id());
        assertEquals(idC, entries.get(1).id());
        assertEquals(idA, entries.get(2).id());

        PlantDescriptionEntry.sortByUpdatedAt(entries, true);
        assertEquals(idB, entries.get(0).id());
        assertEquals(idA, entries.get(1).id());
        assertEquals(idC, entries.get(2).id());

        PlantDescriptionEntry.sortById(entries, true);
        assertEquals(idC, entries.get(0).id());
        assertEquals(idA, entries.get(1).id());
        assertEquals(idB, entries.get(2).id());

        PlantDescriptionEntry.sortById(entries, false);
        assertEquals(idB, entries.get(0).id());
        assertEquals(idA, entries.get(1).id());
        assertEquals(idC, entries.get(2).id());
    }

    @Test
    public void shouldReturnDeactivatedCopy() {

        final String consumerPort = "port_a1";
        final String producerPortA = "port_a";
        final String producerPortB = "port_b";

        final String consumerSystemName = "Sys-A";
        final String producerSystemName = "Sys-B";

        final String serviceInterface = "HTTP-SECURE-JSON";

        final List<PortDto> consumerPorts = List.of(new PortBuilder()
            .portName(consumerPort)
            .serviceInterface(serviceInterface)
            .serviceDefinition("ABC")
            .consumer(true)
            .build());

        final List<PortDto> producerPorts = List.of(
            new PortBuilder()
                .portName(producerPortA)
                .serviceInterface(serviceInterface)
                .serviceDefinition("service_a")
                .consumer(false)
                .metadata(Map.of("x", "8"))
                .build(),
            new PortBuilder()
                .portName(producerPortB)
                .serviceInterface(serviceInterface)
                .serviceDefinition("service_b")
                .metadata(Map.of("y", "9"))
                .consumer(false)
                .build());

        final PdeSystemDto consumerSystem = new PdeSystemBuilder()
            .systemId(consumerSystemName)
            .ports(consumerPorts)
            .metadata(Map.of("a", "1"))
            .build();

        final PdeSystemDto producerSystem = new PdeSystemBuilder()
            .systemId(producerSystemName)
            .ports(producerPorts)
            .metadata(Map.of("b", "2"))
            .build();

        final List<ConnectionDto> connections = List.of(
            new ConnectionBuilder()
                .consumer(new SystemPortBuilder()
                    .systemId(consumerSystemName)
                    .portName(consumerPort)
                    .build())
                .producer(new SystemPortBuilder()
                    .systemId(producerSystemName)
                    .portName(producerPortA)
                    .build())
                .build(),
            new ConnectionBuilder()
                .consumer(new SystemPortBuilder()
                    .systemId(consumerSystemName)
                    .portName(consumerPort)
                    .build())
                .producer(new SystemPortBuilder()
                    .systemId(producerSystemName)
                    .portName(producerPortB)
                    .build())
                .build());

        final PlantDescriptionEntryDto entry = new PlantDescriptionEntryBuilder()
            .id(1)
            .plantDescription("Plant Description 1A")
            .createdAt(now)
            .updatedAt(now)
            .active(true)
            .systems(List.of(consumerSystem, producerSystem))
            .connections(connections)
            .build();

        final PlantDescriptionEntry deactivated = PlantDescriptionEntry.deactivated(entry);
        assertTrue(entry.active());
        assertFalse(deactivated.active());

        // Make sure that they are otherwise equal:
        assertEquals(entry.id(), deactivated.id());
        assertEquals(entry.plantDescription(), deactivated.plantDescription());
        assertEquals(entry.createdAt(), deactivated.createdAt());
        assertTrue(deactivated.updatedAt().isAfter(entry.updatedAt()));

        // The systems should be identical
        for (int i = 0; i < entry.systems().size(); i++) {
            final PdeSystem systemA = entry.systems().get(i);
            final PdeSystem systemB = entry.systems().get(i);
            assertEquals(systemA.toString(), systemB.toString());
        }
    }

    @Test
    public void shouldCreateFromDescription() {

        final String consumerId = "Sys-A";
        final String producerId = "Sys-B";
        final String consumerName = "System A";
        final String producerName = "System B";
        final String consumerPort = "PortA";
        final String producerPort = "PortB";

        final String serviceInterface = "HTTP-SECURE-JSON";

        final List<PortDto> consumerPorts = List.of(new PortBuilder()
            .portName(consumerPort)
            .serviceInterface(serviceInterface)
            .serviceDefinition("Monitorable")
            .consumer(true)
            .build());

        final List<PortDto> producerPorts = List.of(new PortBuilder()
            .portName(consumerPort)
            .serviceInterface(serviceInterface)
            .serviceDefinition("Monitorable")
            .consumer(false)
            .build());

        final PdeSystemDto consumerSystem = new PdeSystemBuilder()
            .systemId(consumerId)
            .systemName(consumerName)
            .ports(consumerPorts)
            .build();

        final PdeSystemDto producerSystem = new PdeSystemBuilder()
            .systemId(producerId)
            .systemName(producerName)
            .ports(producerPorts)
            .build();

        final List<ConnectionDto> connections = List.of(new ConnectionBuilder()
            .consumer(new SystemPortBuilder()
                .systemId(consumerId)
                .portName(consumerPort)
                .build())
            .producer(new SystemPortBuilder()
                .systemId(producerId)
                .portName(producerPort)
                .build())
            .build());

        final PlantDescriptionDto description = new PlantDescriptionBuilder()
            .plantDescription("Plant Description 1A")
            .active(true)
            .systems(List.of(consumerSystem, producerSystem))
            .connections(connections)
            .build();

        final int entryId = 123;
        final PlantDescriptionEntryDto entry = PlantDescriptionEntry.from(description, entryId);

        assertEquals(2, entry.systems().size());
        final PdeSystem copiedConsumer = entry.systems().get(0);
        final PdeSystem copiedProducer = entry.systems().get(1);
        assertEquals(consumerId, copiedConsumer.systemId());
        assertEquals(producerId, copiedProducer.systemId());
        assertEquals(consumerName, copiedConsumer.systemName().orElse(null));
        assertEquals(producerName, copiedProducer.systemName().orElse(null));
        assertEquals(1, consumerSystem.ports().size());
        assertEquals(1, producerSystem.ports().size());
        assertEquals(1, entry.connections().size());
        final Connection copiedConnection = entry.connections().get(0);
        assertEquals(consumerPort, copiedConnection.consumer().portName());
        assertEquals(consumerId, copiedConnection.consumer().systemId());
        assertEquals(producerPort, copiedConnection.producer().portName());
        assertEquals(producerId, copiedConnection.producer().systemId());
    }
}
