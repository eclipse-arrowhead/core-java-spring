package eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto;

import org.junit.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PlantDescriptionEntryTest {

    final Instant now = Instant.now();

    @Test
    public void shouldFilterEntries() {
        final List<PlantDescriptionEntry> original = Arrays.asList(
            new PlantDescriptionEntryDto.Builder()
                .id(1)
                .plantDescription("Plant Description 1A")
                .createdAt(now)
                .updatedAt(now)
                .active(false)
                .build(),
            new PlantDescriptionEntryDto.Builder()
                .id(2)
                .plantDescription("Plant Description 1A")
                .createdAt(now)
                .updatedAt(now)
                .active(true)
                .build(),
            new PlantDescriptionEntryDto.Builder()
                .id(3)
                .plantDescription("Plant Description 1A")
                .createdAt(now)
                .updatedAt(now)
                .active(false)
                .build(),
            new PlantDescriptionEntryDto.Builder()
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

        final List<PortDto> consumerPorts = List.of(new PortDto.Builder()
            .portName(portName)
            .serviceInterface(serviceInterface)
            .serviceDefinition(serviceDefinition)
            .consumer(true)
            .build());

        final List<PortDto> producerPorts = List.of(new PortDto.Builder()
            .portName(portName)
            .serviceInterface(serviceInterface)
            .serviceDefinition(serviceDefinition)
            .consumer(false)
            .build());

        final PdeSystemDto consumerSystem = new PdeSystemDto.Builder()
            .systemId(consumerId)
            .ports(consumerPorts)
            .build();

        final PdeSystemDto producerSystem = new PdeSystemDto.Builder()
            .systemId(producerId)
            .ports(producerPorts)
            .build();

        final PlantDescriptionEntryDto entry = new PlantDescriptionEntryDto.Builder()
            .id(1)
            .plantDescription("Plant Description 1A")
            .createdAt(now)
            .updatedAt(now)
            .active(true)
            .systems(List.of(consumerSystem, producerSystem))
            .connections(List.of())
            .build();

        final List<ConnectionDto> newConnections = List.of(new ConnectionDto.Builder()
            .consumer(new SystemPortDto.Builder()
                .systemId(consumerId)
                .portName(portName)
                .build())
            .producer(new SystemPortDto.Builder()
                .systemId(producerId)
                .portName(portName)
                .build())
            .build());

        final PlantDescriptionUpdateDto newFields = new PlantDescriptionUpdateDto.Builder()
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

        final List<PortDto> consumerPorts = List.of(new PortDto.Builder()
            .portName(portName)
            .serviceInterface(serviceInterface)
            .serviceDefinition(serviceDefinition)
            .consumer(true)
            .build());

        final List<PortDto> producerPorts = List.of(new PortDto.Builder()
            .portName(portName)
            .serviceInterface(serviceInterface)
            .serviceDefinition(serviceDefinition)
            .consumer(false)
            .build());

        final PdeSystemDto consumerSystem = new PdeSystemDto.Builder()
            .systemId(consumerId)
            .ports(consumerPorts)
            .build();

        final PdeSystemDto producerSystem = new PdeSystemDto.Builder()
            .systemId(producerId)
            .ports(producerPorts)
            .build();

        final List<ConnectionDto> connections = List.of(new ConnectionDto.Builder()
            .consumer(new SystemPortDto.Builder()
                .systemId(consumerId)
                .portName(portName)
                .build())
            .producer(new SystemPortDto.Builder()
                .systemId(producerId)
                .portName(portName)
                .build())
            .build());

        final PlantDescriptionEntryDto entry = new PlantDescriptionEntryDto.Builder()
            .id(1)
            .plantDescription("Plant Description 1A")
            .createdAt(now)
            .updatedAt(now)
            .active(true)
            .systems(List.of(consumerSystem, producerSystem))
            .connections(connections)
            .build();

        final PlantDescriptionUpdateDto newFields = new PlantDescriptionUpdateDto.Builder()
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
            new PortDto.Builder().portName(portNameA)
                .serviceDefinition(serviceDefinition)
                .serviceInterface(serviceInterface)
                .consumer(true)
                .build(),
            new PortDto.Builder().portName(portNameB)
                .serviceDefinition(serviceDefinition)
                .serviceInterface(serviceInterface)
                .consumer(true)
                .build(),
            new PortDto.Builder().portName(portNameC)
                .serviceDefinition(serviceDefinition)
                .serviceInterface(serviceInterface)
                .consumer(true)
                .build());

        final List<PortDto> producerPorts = List.of(new PortDto.Builder()
            .portName(portNameA)
            .serviceInterface(serviceInterface)
            .serviceDefinition(serviceDefinition)
            .consumer(false)
            .build());

        final PdeSystemDto consumerSystem = new PdeSystemDto.Builder()
            .systemId(consumerId)
            .ports(consumerPorts)
            .build();

        final PdeSystemDto producerSystem = new PdeSystemDto.Builder()
            .systemId(producerId)
            .ports(producerPorts)
            .build();

        final List<ConnectionDto> connections = List.of(new ConnectionDto.Builder()
            .consumer(new SystemPortDto.Builder()
                .systemId(consumerId)
                .portName(portNameA)
                .build())
            .producer(new SystemPortDto.Builder()
                .systemId(producerId)
                .portName(portNameA)
                .build())
            .build());

        final PlantDescriptionEntryDto entry = new PlantDescriptionEntryDto.Builder()
            .id(1)
            .plantDescription("Plant Description 1A")
            .createdAt(now)
            .updatedAt(now)
            .active(true)
            .systems(List.of(consumerSystem, producerSystem))
            .connections(connections)
            .build();

        final List<ConnectionDto> newConnections = List.of(
            new ConnectionDto.Builder()
                .consumer(new SystemPortDto.Builder()
                    .systemId(consumerId)
                    .portName(portNameB)
                    .build())
                .producer(new SystemPortDto.Builder()
                    .systemId(producerId)
                    .portName(portNameA)
                    .build())
                .build(),
            new ConnectionDto.Builder()
                .consumer(new SystemPortDto.Builder()
                    .systemId(consumerId)
                    .portName(portNameC)
                    .build())
                .producer(new SystemPortDto.Builder()
                    .systemId(producerId)
                    .portName(portNameA)
                    .build())
                .build());

        final PlantDescriptionUpdateDto newFields = new PlantDescriptionUpdateDto.Builder()
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

        final PlantDescriptionEntryDto entryA = new PlantDescriptionEntryDto.Builder()
            .id(idA)
            .plantDescription("A")
            .createdAt(t3)
            .updatedAt(t3)
            .active(false)
            .build();

        final PlantDescriptionEntryDto entryB = new PlantDescriptionEntryDto.Builder()
            .id(idB)
            .plantDescription("B")
            .createdAt(t1)
            .updatedAt(t1)
            .active(false)
            .build();
        final PlantDescriptionEntryDto entryC = new PlantDescriptionEntryDto.Builder()
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

        final List<PortDto> consumerPorts = List.of(new PortDto.Builder()
            .portName(consumerPort)
            .serviceInterface(serviceInterface)
            .serviceDefinition("ABC")
            .consumer(true)
            .build());

        final List<PortDto> producerPorts = List.of(
            new PortDto.Builder()
                .portName(producerPortA)
                .serviceInterface(serviceInterface)
                .serviceDefinition("service_a")
                .consumer(false)
                .metadata(Map.of("x", "8"))
                .build(),
            new PortDto.Builder()
                .portName(producerPortB)
                .serviceInterface(serviceInterface)
                .serviceDefinition("service_b")
                .metadata(Map.of("y", "9"))
                .consumer(false)
                .build());

        final PdeSystemDto consumerSystem = new PdeSystemDto.Builder()
            .systemId(consumerSystemName)
            .ports(consumerPorts)
            .metadata(Map.of("a", "1"))
            .build();

        final PdeSystemDto producerSystem = new PdeSystemDto.Builder()
            .systemId(producerSystemName)
            .ports(producerPorts)
            .metadata(Map.of("b", "2"))
            .build();

        final List<ConnectionDto> connections = List.of(
            new ConnectionDto.Builder()
                .consumer(new SystemPortDto.Builder()
                    .systemId(consumerSystemName)
                    .portName(consumerPort)
                    .build())
                .producer(new SystemPortDto.Builder()
                    .systemId(producerSystemName)
                    .portName(producerPortA)
                    .build())
                .build(),
            new ConnectionDto.Builder()
                .consumer(new SystemPortDto.Builder()
                    .systemId(consumerSystemName)
                    .portName(consumerPort)
                    .build())
                .producer(new SystemPortDto.Builder()
                    .systemId(producerSystemName)
                    .portName(producerPortB)
                    .build())
                .build());

        final Instant t1 = now.minus(1, ChronoUnit.MINUTES);
        final PlantDescriptionEntryDto entry = new PlantDescriptionEntryDto.Builder()
            .id(1)
            .plantDescription("Plant Description 1A")
            .createdAt(t1)
            .updatedAt(t1)
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
        final String consumerName = "sysa";
        final String producerName = "sysb";
        final String consumerPort = "PortA";
        final String producerPort = "PortB";
        final String serviceDefinition = "monitorable";
        final String serviceInterface = "HTTP-SECURE-JSON";

        final List<PortDto> consumerPorts = List.of(new PortDto.Builder()
            .portName(consumerPort)
            .serviceInterface(serviceInterface)
            .serviceDefinition(serviceDefinition)
            .consumer(true)
            .build());

        final List<PortDto> producerPorts = List.of(new PortDto.Builder()
            .portName(consumerPort)
            .serviceInterface(serviceInterface)
            .serviceDefinition(serviceDefinition)
            .consumer(false)
            .build());

        final PdeSystemDto consumerSystem = new PdeSystemDto.Builder()
            .systemId(consumerId)
            .systemName(consumerName)
            .ports(consumerPorts)
            .build();

        final PdeSystemDto producerSystem = new PdeSystemDto.Builder()
            .systemId(producerId)
            .systemName(producerName)
            .ports(producerPorts)
            .build();

        final List<ConnectionDto> connections = List.of(new ConnectionDto.Builder()
            .consumer(new SystemPortDto.Builder()
                .systemId(consumerId)
                .portName(consumerPort)
                .build())
            .producer(new SystemPortDto.Builder()
                .systemId(producerId)
                .portName(producerPort)
                .build())
            .build());

        final PlantDescriptionDto description = new PlantDescriptionDto.Builder()
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
