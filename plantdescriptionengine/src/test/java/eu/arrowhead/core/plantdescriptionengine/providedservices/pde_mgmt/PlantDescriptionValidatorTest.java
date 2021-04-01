package eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt;

import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.ConnectionBuilder;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.ConnectionDto;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.PdeSystemBuilder;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.PdeSystemDto;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.PlantDescriptionEntry;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.PlantDescriptionEntryBuilder;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.PlantDescriptionEntryDto;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.PortBuilder;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.PortDto;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.SystemPortBuilder;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PlantDescriptionValidatorTest {

    final Instant now = Instant.now();

    @Test
    public void shouldNotReportErrors() {

        // First entry
        final int entryIdA = 0;
        final String producerIdA = "Prod-A";
        final String consumerIdA = "Cons-A";
        final String consumerNameA = "Consumer A";
        final String producerNameA = "Producer A";
        final String consumerPortA = "Cons-Port-A";
        final String producerPortA = "Prod-Port-A";
        final String serviceInterface = "HTTP-SECURE-JSON";

        final List<PortDto> consumerPortsA = List.of(new PortBuilder()
            .portName(consumerPortA)
            .serviceInterface(serviceInterface)
            .serviceDefinition("Monitorable")
            .consumer(true)
            .build());

        final List<PortDto> producerPortsA = List.of(new PortBuilder()
            .portName(producerPortA)
            .serviceInterface("HTTP-SECURE-JSON")
            .serviceDefinition("Monitorable")
            .consumer(false)
            .build());

        final PdeSystemDto consumerSystemA = new PdeSystemBuilder().systemId(consumerIdA)
            .systemName(consumerNameA)
            .ports(consumerPortsA)
            .build();

        final PdeSystemDto producerSystemA = new PdeSystemBuilder()
            .systemId(producerIdA)
            .systemName(producerNameA)
            .ports(producerPortsA)
            .build();

        final List<ConnectionDto> connectionsA = List.of(new ConnectionBuilder()
            .priority(1)
            .consumer(new SystemPortBuilder()
                .systemId(consumerIdA)
                .portName(consumerPortA)
                .build())
            .producer(new SystemPortBuilder()
                .systemId(producerIdA)
                .portName(producerPortA)
                .build())
            .build());

        final PlantDescriptionEntryDto entryA = new PlantDescriptionEntryBuilder()
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
        final String consumerNameB = "Consumer B";
        final String consumerPortB = "Cons-Port-B";

        final List<PortDto> consumerPortsB = List.of(new PortBuilder()
            .portName(consumerPortB)
            .serviceInterface(serviceInterface)
            .serviceDefinition("Monitorable")
            .consumer(true)
            .build());

        final PdeSystemDto consumerSystemB = new PdeSystemBuilder()
            .systemId(consumerIdB)
            .systemName(consumerNameB)
            .ports(consumerPortsB)
            .build();

        final List<ConnectionDto> connectionsB = List.of(new ConnectionBuilder()
            .consumer(new SystemPortBuilder()
                .systemId(consumerIdB)
                .portName(consumerPortB)
                .build())
            .producer(new SystemPortBuilder()
                .systemId(producerIdA)
                .portName(producerPortA)
                .build())
            .build());

        final PlantDescriptionEntryDto entryB = new PlantDescriptionEntryBuilder()
            .id(entryIdB)
            .plantDescription("Plant Description B")
            .createdAt(now)
            .updatedAt(now)
            .active(true)
            .include(List.of(entryIdA))
            .systems(List.of(consumerSystemB))
            .connections(connectionsB)
            .build();

        final Map<Integer, PlantDescriptionEntry> entries = Map.of(entryIdA, entryA, entryIdB, entryB);
        final PlantDescriptionValidator validator = new PlantDescriptionValidator(entries);
        assertFalse(validator.hasError());
    }

    @Test
    public void shouldReportProducerConsumerMismatch() {

        final int entryId = 123;
        final String producerId = "Prod-A";
        final String consumerId = "Cons-A";
        final String consumerNameA = "Consumer A";
        final String producerNameA = "Producer A";
        final String consumerPort = "Cons-Port-A";
        final String producerPort = "Prod-Port-A";
        final String serviceInterface = "HTTP-SECURE-JSON";

        final List<PortDto> consumerPortsA = List.of(new PortBuilder()
            .portName(consumerPort)
            .serviceInterface(serviceInterface)
            .serviceDefinition("Monitorable")
            .consumer(true)
            .build());

        final List<PortDto> producerPortsA = List.of(new PortBuilder()
            .portName(producerPort)
            .serviceInterface(serviceInterface)
            .serviceDefinition("Monitorable")
            .consumer(false)
            .build());

        final PdeSystemDto consumerSystem = new PdeSystemBuilder()
            .systemId(consumerId)
            .systemName(consumerNameA)
            .ports(consumerPortsA)
            .build();

        final PdeSystemDto producerSystem = new PdeSystemBuilder()
            .systemId(producerId)
            .systemName(producerNameA)
            .ports(producerPortsA)
            .build();

        final List<ConnectionDto> connections = List.of(new ConnectionBuilder()
            .priority(1)
            .consumer(new SystemPortBuilder()
                .systemId(producerId)
                .portName(producerPort)
                .build())
            .producer(new SystemPortBuilder()
                .systemId(consumerId)
                .portName(consumerPort)
                .build())
            .build());

        final PlantDescriptionEntryDto entry = new PlantDescriptionEntryBuilder()
            .id(entryId)
            .plantDescription("Plant Description A")
            .createdAt(now)
            .updatedAt(now)
            .active(false)
            .systems(List.of(consumerSystem, producerSystem))
            .connections(connections)
            .build();

        final Map<Integer, PlantDescriptionEntry> entries = Map.of(entryId, entry);
        final PlantDescriptionValidator validator = new PlantDescriptionValidator(entries);
        assertTrue(validator.hasError());
        final String errorMessage = "<Invalid connection, '" + consumerPort +
            "' is not a producer port.>, <Invalid connection, '" +
            producerPort + "' is not a consumer port.>";
        assertEquals(errorMessage, validator.getErrorMessage());
    }

    @Test
    public void shouldReportServiceInterfaceMismatch() {

        final int entryId = 332;
        final String producerId = "Prod-A";
        final String consumerId = "Cons-A";
        final String consumerNameA = "Consumer A";
        final String producerNameA = "Producer A";
        final String consumerPort = "Cons-Port-A";
        final String producerPort = "Prod-Port-A";
        final String serviceInterfaceA = "HTTP-SECURE-JSON";
        final String serviceInterfaceB = "HTTP-INSECURE-JSON";

        final List<PortDto> consumerPortsA = List.of(new PortBuilder()
            .portName(consumerPort)
            .serviceInterface(serviceInterfaceA)
            .serviceDefinition("Monitorable")
            .consumer(true)
            .build());

        final List<PortDto> producerPortsA = List.of(new PortBuilder()
            .portName(producerPort)
            .serviceInterface(serviceInterfaceB)
            .serviceDefinition("Monitorable")
            .consumer(false)
            .build());

        final PdeSystemDto consumerSystem = new PdeSystemBuilder()
            .systemId(consumerId)
            .systemName(consumerNameA)
            .ports(consumerPortsA)
            .build();

        final PdeSystemDto producerSystem = new PdeSystemBuilder()
            .systemId(producerId)
            .systemName(producerNameA)
            .ports(producerPortsA)
            .build();

        final List<ConnectionDto> connections = List.of(new ConnectionBuilder()
            .priority(1)
            .consumer(new SystemPortBuilder()
                .systemId(consumerId)
                .portName(consumerPort)
                .build())
            .producer(new SystemPortBuilder()
                .systemId(producerId)
                .portName(producerPort)
                .build())
            .build());

        final PlantDescriptionEntryDto entry = new PlantDescriptionEntryBuilder()
            .id(entryId)
            .plantDescription("Plant Description A")
            .createdAt(now)
            .updatedAt(now)
            .active(false)
            .systems(List.of(consumerSystem, producerSystem))
            .connections(connections)
            .build();

        final Map<Integer, PlantDescriptionEntry> entries = Map.of(entryId, entry);
        final PlantDescriptionValidator validator = new PlantDescriptionValidator(entries);
        assertTrue(validator.hasError());
        final String errorMessage = "<The service interfaces of ports '" +
            consumerPort + "' and '" + producerPort +
            "' do not match.>";
        assertEquals(errorMessage, validator.getErrorMessage());
    }

    @Test
    public void shouldReportServiceDefinitionMismatch() {

        final int entryId = 332;
        final String producerId = "Prod-A";
        final String consumerId = "Cons-A";
        final String consumerNameA = "Consumer A";
        final String producerNameA = "Producer A";
        final String consumerPort = "Cons-Port-A";
        final String producerPort = "Prod-Port-A";
        final String serviceInterface = "HTTP-SECURE-JSON";
        final String serviceDefinitionA = "Service-A";
        final String serviceDefinitionB = "Service-B";

        final List<PortDto> consumerPortsA = List.of(new PortBuilder()
            .portName(consumerPort)
            .serviceInterface(serviceInterface)
            .serviceDefinition(serviceDefinitionA)
            .consumer(true)
            .build());

        final List<PortDto> producerPortsA = List.of(new PortBuilder()
            .portName(producerPort)
            .serviceInterface(serviceInterface)
            .serviceDefinition(serviceDefinitionB)
            .consumer(false)
            .build());

        final PdeSystemDto consumerSystem = new PdeSystemBuilder()
            .systemId(consumerId)
            .systemName(consumerNameA)
            .ports(consumerPortsA)
            .build();

        final PdeSystemDto producerSystem = new PdeSystemBuilder()
            .systemId(producerId)
            .systemName(producerNameA)
            .ports(producerPortsA)
            .build();

        final List<ConnectionDto> connections = List.of(new ConnectionBuilder()
            .priority(1)
            .consumer(new SystemPortBuilder()
                .systemId(consumerId)
                .portName(consumerPort)
                .build())
            .producer(new SystemPortBuilder()
                .systemId(producerId)
                .portName(producerPort)
                .build())
            .build());

        final PlantDescriptionEntryDto entry = new PlantDescriptionEntryBuilder()
            .id(entryId)
            .plantDescription("Plant Description A")
            .createdAt(now)
            .updatedAt(now)
            .active(false)
            .systems(List.of(consumerSystem, producerSystem))
            .connections(connections)
            .build();

        final Map<Integer, PlantDescriptionEntry> entries = Map.of(entryId, entry);
        final PlantDescriptionValidator validator = new PlantDescriptionValidator(entries);
        assertTrue(validator.hasError());
        final String errorMessage = "<The service definitions of ports '" +
            consumerPort + "' and '" + producerPort +
            "' do not match.>";
        assertEquals(errorMessage, validator.getErrorMessage());
    }

    @Test
    public void shouldReportDuplicatePorts() {

        final String systemId = "system_a";
        final String portName = "port_a";
        final String serviceInterface = "HTTP-SECURE-JSON";

        final List<PortDto> consumerPorts = List.of(
            new PortBuilder().portName(portName)
                .serviceInterface(serviceInterface)
                .serviceDefinition("service_a")
                .consumer(true)
                .build(),
            new PortBuilder().portName(portName)
                .serviceInterface(serviceInterface)
                .serviceDefinition("service_b")
                .consumer(true)
                .build());

        final PdeSystemDto consumerSystem = new PdeSystemBuilder()
            .systemId(systemId)
            .systemName("System XYZ")
            .ports(consumerPorts)
            .build();

        final PlantDescriptionEntryDto entry = new PlantDescriptionEntryBuilder()
            .plantDescription("Plant Description 1A")
            .id(123)
            .active(true)
            .createdAt(now)
            .updatedAt(now)
            .systems(List.of(consumerSystem))
            .build();

        final Map<Integer, PlantDescriptionEntry> entries = Map.of(entry.id(), entry);
        final PlantDescriptionValidator validator = new PlantDescriptionValidator(entries);
        assertTrue(validator.hasError());

        final String expectedErrorMessage = "<Duplicate port name '" + portName + "' in system '" + systemId + "'>";
        assertEquals(expectedErrorMessage, validator.getErrorMessage());
    }

    @Test
    public void shouldRequireMetadataToDifferentiateBetweenPorts() {

        final String systemId = "system_1";
        final String portNameA = "consumerPortA";
        final String serviceDefinition = "service_a";
        final String serviceInterface = "HTTP-SECURE-JSON";

        final List<PortDto> ports = List.of(
            new PortBuilder()
                .portName(portNameA)
                .serviceInterface(serviceInterface)
                .serviceDefinition(serviceDefinition)
                .metadata(Map.of("a", "1"))
                .build(),
            new PortBuilder()
                .portName("port_b")
                .serviceInterface(serviceInterface)
                .serviceDefinition(serviceDefinition)
                .build(),
            new PortBuilder()
                .portName("port_c")
                .serviceInterface(serviceInterface)
                .serviceDefinition(serviceDefinition)
                .build());

        final PdeSystemDto system = new PdeSystemBuilder()
            .systemId(systemId)
            .systemName("X")
            .ports(ports)
            .build();

        final PlantDescriptionEntryDto entry = new PlantDescriptionEntryBuilder()
            .id(789)
            .plantDescription("Plant Description 1A")
            .active(true)
            .systems(List.of(system))
            .createdAt(now)
            .updatedAt(now)
            .build();

        final Map<Integer, PlantDescriptionEntry> entries = Map.of(entry.id(), entry);
        final PlantDescriptionValidator validator = new PlantDescriptionValidator(entries);
        assertTrue(validator.hasError());

        final String expectedErrorMessage = "<" + systemId + " has multiple ports with service definition '"
            + serviceDefinition + "' without metadata.>";
        assertEquals(expectedErrorMessage, validator.getErrorMessage());
    }

    @Test
    public void shouldReportInvalidProducerPort() {

        final String consumerId = "system_1";
        final String producerId = "system_2";
        final String consumerPort = "port_1";
        final String producerPort = "port_2";
        final String invalidPort = "no_such_port";
        final String serviceDefinition = "service_a";
        final String serviceInterface = "HTTP-SECURE-JSON";

        final List<PortDto> consumerPorts = List.of(new PortBuilder()
            .portName(consumerPort)
            .serviceInterface(serviceInterface)
            .serviceDefinition(serviceDefinition)
            .consumer(true)
            .build());

        final List<PortDto> producerPorts = List.of(new PortBuilder()
            .portName(producerPort)
            .serviceInterface(serviceInterface)
            .serviceDefinition(serviceDefinition)
            .consumer(false)
            .build());

        final PdeSystemDto consumerSystem = new PdeSystemBuilder()
            .systemId(consumerId)
            .systemName("Consumer X")
            .ports(consumerPorts)
            .build();

        final PdeSystemDto producerSystem = new PdeSystemBuilder()
            .systemId(producerId)
            .systemName("Producer Y")
            .ports(producerPorts)
            .build();

        final List<ConnectionDto> connections = List.of(new ConnectionBuilder()
            .consumer(new SystemPortBuilder()
                .systemId(consumerId)
                .portName(consumerPort)
                .build())
            .producer(new SystemPortBuilder()
                .systemId(producerId)
                .portName(invalidPort)
                .build())
            .build());
        final PlantDescriptionEntryDto entry = new PlantDescriptionEntryBuilder()
            .id(42)
            .plantDescription("Plant Description 1A")
            .active(true)
            .systems(List.of(consumerSystem, producerSystem))
            .connections(connections)
            .createdAt(now)
            .updatedAt(now)
            .build();

        final Map<Integer, PlantDescriptionEntry> entries = Map.of(entry.id(), entry);
        final PlantDescriptionValidator validator = new PlantDescriptionValidator(entries);
        assertTrue(validator.hasError());

        final String expectedErrorMessage = "<Connection refers to the missing producer port '" + invalidPort + "'>";
        assertEquals(expectedErrorMessage, validator.getErrorMessage());
    }

    @Test
    public void shouldReportNegativePriority() {

        // First entry
        final int entryIdA = 0;
        final String consumerIdA = "Cons-A";
        final String consumerNameA = "Consumer A";
        final String producerNameA = "Producer A";
        final String consumerPortA = "Cons-Port-A";
        final String producerPortA = "Prod-Port-A";
        final String producerIdA = "Prod-A";
        final String serviceInterface = "HTTP-SECURE-JSON";

        final List<PortDto> consumerPortsA = List.of(new PortBuilder()
            .portName(consumerPortA)
            .serviceInterface(serviceInterface)
            .serviceDefinition("Monitorable")
            .consumer(true)
            .build());

        final List<PortDto> producerPortsA = List.of(new PortBuilder()
            .portName(producerPortA)
            .serviceInterface(serviceInterface)
            .serviceDefinition("Monitorable")
            .consumer(false)
            .build());

        final PdeSystemDto consumerSystemA = new PdeSystemBuilder()
            .systemId(consumerIdA)
            .systemName(consumerNameA)
            .ports(consumerPortsA)
            .build();

        final PdeSystemDto producerSystemA = new PdeSystemBuilder()
            .systemId(producerIdA)
            .systemName(producerNameA)
            .ports(producerPortsA)
            .build();

        final List<ConnectionDto> connections = List.of(new ConnectionBuilder()
            .priority(-1)
            .consumer(new SystemPortBuilder()
                .systemId(consumerIdA)
                .portName(consumerPortA)
                .build())
            .producer(new SystemPortBuilder()
                .systemId(producerIdA)
                .portName(producerPortA)
                .build())
            .build());

        final PlantDescriptionEntryDto entryA = new PlantDescriptionEntryBuilder()
            .id(entryIdA)
            .plantDescription("Plant Description A")
            .createdAt(now)
            .updatedAt(now)
            .active(false)
            .systems(List.of(consumerSystemA, producerSystemA))
            .connections(connections)
            .build();

        // Second entry
        final int entryIdB = 1;
        final String consumerIdB = "Cons-B";
        final String consumerNameB = "Consumer B";
        final String consumerPortB = "Cons-Port-B";

        final List<PortDto> consumerPortsB = List.of(new PortBuilder()
            .portName(consumerPortB)
            .serviceInterface(serviceInterface)
            .serviceDefinition("Monitorable")
            .consumer(true)
            .build());

        final PdeSystemDto consumerSystemB = new PdeSystemBuilder()
            .systemId(consumerIdB)
            .systemName(consumerNameB)
            .ports(consumerPortsB)
            .build();

        final List<ConnectionDto> connectionsB = List.of(new ConnectionBuilder()
            .consumer(new SystemPortBuilder()
                .systemId(consumerIdB)
                .portName(consumerPortB)
                .build())
            .producer(new SystemPortBuilder()
                .systemId(producerIdA)
                .portName(producerPortA)
                .build())
            .build());

        final PlantDescriptionEntryDto entryB = new PlantDescriptionEntryBuilder()
            .id(entryIdB)
            .plantDescription("Plant Description B")
            .createdAt(now)
            .updatedAt(now)
            .active(true)
            .include(List.of(entryIdA))
            .systems(List.of(consumerSystemB))
            .connections(connectionsB)
            .build();

        final Map<Integer, PlantDescriptionEntry> entries = Map.of(entryIdA, entryA, entryIdB, entryB);
        final PlantDescriptionValidator validator = new PlantDescriptionValidator(entries);
        assertTrue(validator.hasError());
        assertEquals("<A connection has a negative priority.>", validator.getErrorMessage());
    }


    @Test
    public void shouldReportInvalidConsumerPort() {

        final String consumerId = "system_1";
        final String producerId = "system_2";
        final String consumerPort = "port_1";
        final String producerPort = "port_2";
        final String invalidPort = "no_such_port";
        final String serviceDefinition = "service_a";
        final String serviceInterface = "HTTP-SECURE-JSON";

        final List<PortDto> consumerPorts = List.of(new PortBuilder()
            .portName(consumerPort)
            .serviceInterface(serviceInterface)
            .serviceDefinition(serviceDefinition)
            .consumer(true)
            .build());

        final List<PortDto> producerPorts = List.of(new PortBuilder()
            .portName(producerPort)
            .serviceInterface(serviceInterface)
            .serviceDefinition(serviceDefinition)
            .consumer(false)
            .build());

        final PdeSystemDto consumerSystem = new PdeSystemBuilder()
            .systemId(consumerId)
            .systemName("Consumer A")
            .ports(consumerPorts)
            .build();

        final PdeSystemDto producerSystem = new PdeSystemBuilder()
            .systemId(producerId)
            .systemName("Consumer B")
            .ports(producerPorts)
            .build();

        final List<ConnectionDto> connections = List.of(new ConnectionBuilder()
            .consumer(new SystemPortBuilder()
                .systemId(consumerId)
                .portName(invalidPort)
                .build())
            .producer(new SystemPortBuilder()
                .systemId(producerId)
                .portName(producerPort)
                .build())
            .build());

        final PlantDescriptionEntryDto entry = new PlantDescriptionEntryBuilder()
            .id(89)
            .plantDescription("Plant Description 1A")
            .active(true)
            .systems(List.of(consumerSystem, producerSystem))
            .connections(connections)
            .createdAt(now)
            .updatedAt(now)
            .build();

        final Map<Integer, PlantDescriptionEntry> entries = Map.of(entry.id(), entry);
        final PlantDescriptionValidator validator = new PlantDescriptionValidator(entries);
        assertTrue(validator.hasError());

        final String expectedErrorMessage = "<Connection refers to the missing consumer port '" + invalidPort + "'>";
        assertEquals(expectedErrorMessage, validator.getErrorMessage());
    }

    @Test
    public void shouldReportMissingConsumer() {

        final String consumerId = "system_1";
        final String producerId = "system_2";
        final String missingId = "garbage_string";
        final String consumerPort = "port_1";
        final String producerPort = "port_2";
        final String serviceDefinition = "service_a";
        final String serviceInterface = "HTTP-SECURE-JSON";

        final List<PortDto> consumerPorts = List.of(new PortBuilder()
            .portName(consumerPort)
            .serviceInterface(serviceInterface)
            .serviceDefinition(serviceDefinition)
            .consumer(true)
            .build());

        final List<PortDto> producerPorts = List.of(new PortBuilder()
            .portName(producerPort)
            .serviceInterface(serviceInterface)
            .serviceDefinition(serviceDefinition)
            .consumer(false)
            .build());

        final PdeSystemDto consumerSystem = new PdeSystemBuilder()
            .systemId(consumerId)
            .systemName("Consumer I")
            .ports(consumerPorts)
            .build();

        final PdeSystemDto producerSystem = new PdeSystemBuilder()
            .systemId(producerId)
            .systemName("Producer J")
            .ports(producerPorts)
            .build();

        final List<ConnectionDto> connections = List.of(
            new ConnectionBuilder()
                .consumer(new SystemPortBuilder()
                    .systemId(consumerId)
                    .portName(consumerPort)
                    .build())
                .producer(new SystemPortBuilder().systemId(producerId)
                    .portName(producerPort)
                    .build())
                .build(),
            new ConnectionBuilder()
                .consumer(new SystemPortBuilder()
                    .systemId(missingId)
                    .portName(consumerPort)
                    .build())
                .producer(new SystemPortBuilder().systemId(producerId)
                    .portName(producerPort)
                    .build())
                .build()
        );
        final PlantDescriptionEntryDto entry = new PlantDescriptionEntryBuilder()
            .id(23)
            .plantDescription("Plant Description 1A")
            .active(true)
            .systems(List.of(consumerSystem, producerSystem))
            .connections(connections)
            .createdAt(now)
            .updatedAt(now)
            .build();

        final Map<Integer, PlantDescriptionEntry> entries = Map.of(entry.id(), entry);
        final PlantDescriptionValidator validator = new PlantDescriptionValidator(entries);
        assertTrue(validator.hasError());

        final String expectedErrorMessage = "<A connection refers to the missing system '" + missingId + "'>";
        assertEquals(expectedErrorMessage, validator.getErrorMessage());
    }

    @Test
    public void shouldReportMissingProvider() {

        final String consumerId = "system_1";
        final String producerId = "system_2";
        final String missingId = "garbage_string";
        final String consumerPort = "port_1";
        final String producerPort = "port_2";
        final String serviceDefinition = "service_a";
        final String serviceInterface = "HTTP-SECURE-JSON";

        final List<PortDto> consumerPorts = List.of(new PortBuilder()
            .portName(consumerPort)
            .serviceInterface(serviceInterface)
            .serviceDefinition(serviceDefinition)
            .consumer(true)
            .build());

        final List<PortDto> producerPorts = List.of(new PortBuilder()
            .portName(producerPort)
            .serviceInterface(serviceInterface)
            .serviceDefinition(serviceDefinition)
            .consumer(false)
            .build());

        final PdeSystemDto consumerSystem = new PdeSystemBuilder()
            .systemId(consumerId)
            .systemName("Consumer A")
            .ports(consumerPorts)
            .build();

        final PdeSystemDto producerSystem = new PdeSystemBuilder()
            .systemId(producerId)
            .systemName("Producer B")
            .ports(producerPorts)
            .build();

        final List<ConnectionDto> connections = List.of(new ConnectionBuilder()
            .consumer(new SystemPortBuilder()
                .systemId(consumerId)
                .portName(consumerPort)
                .build())
            .producer(new SystemPortBuilder()
                .systemId(missingId)
                .portName(producerPort)
                .build())
            .build());
        final PlantDescriptionEntryDto entry = new PlantDescriptionEntryBuilder()
            .id(23)
            .plantDescription("Plant Description 1A")
            .active(true)
            .systems(List.of(consumerSystem, producerSystem))
            .connections(connections)
            .createdAt(now)
            .updatedAt(now)
            .build();

        final Map<Integer, PlantDescriptionEntry> entries = Map.of(entry.id(), entry);
        final PlantDescriptionValidator validator = new PlantDescriptionValidator(entries);
        assertTrue(validator.hasError());

        final String expectedErrorMessage = "<A connection refers to the missing system '" + missingId + "'>";
        assertEquals(expectedErrorMessage, validator.getErrorMessage());
    }

    @Test
    public void shouldReportNonUniqueMetadata() {

        final String systemId = "system_x";
        final String serviceDefinition = "service_a";
        final Map<String, String> sharedMetadata = Map.of("x", "y");
        final String serviceInterface = "HTTP-SECURE-JSON";

        final List<PortDto> ports = List.of(
            new PortBuilder()
                .portName("port_1")
                .serviceInterface(serviceInterface)
                .serviceDefinition(serviceDefinition)
                .metadata(sharedMetadata)
                .build(),
            new PortBuilder()
                .portName("port_b")
                .serviceInterface(serviceInterface)
                .serviceDefinition(serviceDefinition)
                .metadata(sharedMetadata)
                .build());

        final PdeSystemDto producerSystem = new PdeSystemBuilder()
            .systemId(systemId)
            .systemName("Producer Y")
            .ports(ports)
            .build();

        final PlantDescriptionEntryDto entry = new PlantDescriptionEntryBuilder()
            .id(23)
            .plantDescription("Plant Description 1A")
            .active(true)
            .systems(List.of(producerSystem))
            .createdAt(now)
            .updatedAt(now)
            .build();

        final Map<Integer, PlantDescriptionEntry> entries = Map.of(entry.id(), entry);
        final PlantDescriptionValidator validator = new PlantDescriptionValidator(entries);
        assertTrue(validator.hasError());

        final String expectedErrorMessage = "<" + systemId + " has duplicate metadata for ports with service definition '"
            + serviceDefinition + "'>";
        assertEquals(expectedErrorMessage, validator.getErrorMessage());
    }

    @Test
    public void shouldReportDuplicateInclusions() {

        final int entryIdA = 0;
        final int entryIdB = 1;
        final int entryIdC = 2;

        final PlantDescriptionEntryDto entryA = new PlantDescriptionEntryBuilder()
            .id(entryIdA)
            .plantDescription("Plant Description A")
            .createdAt(now)
            .updatedAt(now)
            .active(false)
            .build();

        final PlantDescriptionEntryDto entryB = new PlantDescriptionEntryBuilder()
            .id(entryIdB)
            .plantDescription("Plant Description B")
            .createdAt(now)
            .updatedAt(now)
            .active(false)
            .build();

        final PlantDescriptionEntryDto entryC = new PlantDescriptionEntryBuilder()
            .id(entryIdC)
            .plantDescription("Plant Description C")
            .createdAt(now)
            .updatedAt(now)
            .active(false)
            .include(List.of(entryIdA, entryIdA, entryIdB, entryIdB))
            .build();

        final Map<Integer, PlantDescriptionEntry> entries = Map.of(entryIdA, entryA, entryIdB, entryB, entryIdC, entryC);
        final PlantDescriptionValidator validator = new PlantDescriptionValidator(entries);

        assertTrue(validator.hasError());

        final String expectedErrorMessage = "<Entry with ID '" + entryIdA + "' is included more than once.>, "
            + "<Entry with ID '" + entryIdB + "' is included more than once.>";
        assertEquals(expectedErrorMessage, validator.getErrorMessage());
    }

    @Test
    public void shouldReportSelfInclusion() {

        final int entryId = 344;

        final PlantDescriptionEntryDto entry = new PlantDescriptionEntryBuilder()
            .id(entryId)
            .plantDescription("Plant Description A")
            .createdAt(now)
            .updatedAt(now)
            .active(false)
            .include(List.of(entryId))
            .build();

        final Map<Integer, PlantDescriptionEntry> entries = Map.of(entryId, entry);
        final PlantDescriptionValidator validator = new PlantDescriptionValidator(entries);

        assertTrue(validator.hasError());
        assertEquals("<Entry includes itself.>", validator.getErrorMessage());
    }

    @Test
    public void shouldReportNonexistentInclusions() {

        final int nonExistentA = 23;
        final int nonExistentB = 34;
        final int entryId = 44;

        final PlantDescriptionEntryDto entry = new PlantDescriptionEntryBuilder()
            .id(entryId)
            .plantDescription("Plant Description A")
            .createdAt(now)
            .updatedAt(now)
            .active(false)
            .include(List.of(nonExistentA, nonExistentB))
            .build();

        final Map<Integer, PlantDescriptionEntry> entries = Map.of(entry.id(), entry);
        final PlantDescriptionValidator validator = new PlantDescriptionValidator(entries);

        assertTrue(validator.hasError());
        final String expectedErrorMessage = "<Error in include list: Entry '" + nonExistentA + "' is required by entry '"
            + entryId + "'.>, " + "<Error in include list: Entry '" + nonExistentB + "' is required by entry '"
            + entryId + "'.>";
        assertEquals(expectedErrorMessage, validator.getErrorMessage());
    }

    @Test
    public void shouldReportIncludeCycles() {

        final int entryIdA = 0;
        final int entryIdB = 1;
        final int entryIdC = 2;
        final int entryIdD = 3;

        final PlantDescriptionEntryDto entryA = new PlantDescriptionEntryBuilder()
            .id(entryIdA)
            .plantDescription("Plant Description A")
            .createdAt(now)
            .updatedAt(now)
            .active(false)
            .build();

        final PlantDescriptionEntryDto entryB = new PlantDescriptionEntryBuilder()
            .id(entryIdB)
            .plantDescription("Plant Description B")
            .createdAt(now)
            .updatedAt(now)
            .active(false)
            .build();

        final PlantDescriptionEntryDto entryC = new PlantDescriptionEntryBuilder()
            .id(entryIdC)
            .plantDescription("Plant Description C")
            .createdAt(now)
            .updatedAt(now)
            .active(false)
            .include(List.of(entryIdA, entryIdB))
            .build();

        final PlantDescriptionEntryDto entryD = new PlantDescriptionEntryBuilder()
            .id(entryIdD)
            .plantDescription("Plant Description C")
            .createdAt(now)
            .updatedAt(now)
            .active(false)
            .include(List.of(entryIdB, entryIdC))
            .build();

        final Map<Integer, PlantDescriptionEntry> entries = Map.of(entryIdA, entryA, entryIdB, entryB, entryIdC, entryC,
            entryIdD, entryD);
        final PlantDescriptionValidator validator = new PlantDescriptionValidator(entries);
        assertTrue(validator.hasError());

        final String expectedErrorMessage = "<Contains cycle.>";
        assertEquals(expectedErrorMessage, validator.getErrorMessage());
    }

    @Test
    public void shouldReportInvalidSystemId() {

        final PlantDescriptionEntryDto entry = new PlantDescriptionEntryBuilder()
            .id(9)
            .plantDescription("Plant Description A")
            .createdAt(now)
            .updatedAt(now)
            .active(false)
            .systems(List.of(
                new PdeSystemBuilder()
                    .systemId("Unknown")
                    .systemName("System XYZ")
                    .build()
            ))
            .build();

        final Map<Integer, PlantDescriptionEntry> entries = Map.of(entry.id(), entry);
        final PlantDescriptionValidator validator = new PlantDescriptionValidator(entries);
        assertTrue(validator.hasError());
        final String expectedErrorMessage = "<'Unknown' is not a valid system ID.>";
        assertEquals(expectedErrorMessage, validator.getErrorMessage());
    }

    @Test
    public void shouldRequireNameOrMetadata() {

        final PlantDescriptionEntryDto entry = new PlantDescriptionEntryBuilder()
            .id(22)
            .plantDescription("Plant Description A")
            .createdAt(now)
            .updatedAt(now)
            .active(false)
            .systems(List.of(
                new PdeSystemBuilder()
                    .systemId("xyz")
                    .build()
            ))
            .build();

        final Map<Integer, PlantDescriptionEntry> entries = Map.of(entry.id(), entry);
        final PlantDescriptionValidator validator = new PlantDescriptionValidator(entries);
        assertTrue(validator.hasError());
        final String expectedErrorMessage = "<Contains a system with neither a name nor metadata to identify it.>";
        assertEquals(expectedErrorMessage, validator.getErrorMessage());
    }

    @Test
    public void shouldReportNonUniqueSystem() {
        final String systemIdB = "Sys-B";
        final String systemName = "XYZ";
        final Map<String, String> metadata = Map.of("a", "1");
        final PlantDescriptionEntryDto entry = new PlantDescriptionEntryBuilder()
            .id(22)
            .plantDescription("Plant Description A")
            .createdAt(now)
            .updatedAt(now)
            .active(false)
            .systems(List.of(
                new PdeSystemBuilder()
                    .systemId("Sys-A")
                    .systemName(systemName)
                    .metadata(metadata)
                    .build(),
                new PdeSystemBuilder()
                    .systemId(systemIdB)
                    .systemName(systemName)
                    .metadata(metadata)
                    .build()
            ))
            .build();

        final Map<Integer, PlantDescriptionEntry> entries = Map.of(entry.id(), entry);
        final PlantDescriptionValidator validator = new PlantDescriptionValidator(entries);
        assertTrue(validator.hasError());
        final String expectedErrorMessage = "<System with ID '" + systemIdB +
            "' cannot be uniquely identified by its name/metadata combination.>";
        assertEquals(expectedErrorMessage, validator.getErrorMessage());
    }

    @Test
    public void shouldTreatEmptyMetadataAsNull() {

        final PlantDescriptionEntryDto entry = new PlantDescriptionEntryBuilder()
            .id(22)
            .plantDescription("Plant Description A")
            .createdAt(now)
            .updatedAt(now)
            .active(false)
            .systems(List.of(
                new PdeSystemBuilder()
                    .systemId("xyz")
                    .metadata(Map.of())
                    .build()
            ))
            .build();

        final Map<Integer, PlantDescriptionEntry> entries = Map.of(entry.id(), entry);
        final PlantDescriptionValidator validator = new PlantDescriptionValidator(entries);
        assertTrue(validator.hasError());
        final String expectedErrorMessage = "<Contains a system with neither a name nor metadata to identify it.>";
        assertEquals(expectedErrorMessage, validator.getErrorMessage());
    }

    @Test
    public void shouldAcceptSystemWithOnlyMetadata() {

        final PlantDescriptionEntryDto entry = new PlantDescriptionEntryBuilder()
            .id(22)
            .plantDescription("Plant Description A")
            .createdAt(now)
            .updatedAt(now)
            .active(false)
            .systems(List.of(
                new PdeSystemBuilder()
                    .systemId("X")
                    .metadata(Map.of("x", "y"))
                    .build()
            ))
            .build();

        final Map<Integer, PlantDescriptionEntry> entries = Map.of(entry.id(), entry);
        final PlantDescriptionValidator validator = new PlantDescriptionValidator(entries);
        assertFalse(validator.hasError());
    }

    @Test
    public void shouldNotAcceptConsumerMetadata() {
        final String portNameB = "Port-B";
        final String serviceInterface = "HTTP-SECURE-JSON";

        final List<PortDto> ports = List.of(
            new PortBuilder()
                .portName("PortA")
                .serviceInterface(serviceInterface)
                .serviceDefinition("Monitorable")
                .consumer(true)
                .metadata(Map.of()) // An empty metadata object is okay.
                .build(),
            new PortBuilder()
                .portName(portNameB)
                .serviceInterface(serviceInterface)
                .serviceDefinition("Monitorable")
                .consumer(true)
                .metadata(Map.of("x", "y")) // Not okay on a consumer.
                .build()
        );

        final PlantDescriptionEntryDto entry = new PlantDescriptionEntryBuilder()
            .id(22)
            .plantDescription("Plant Description A")
            .createdAt(now)
            .updatedAt(now)
            .active(false)
            .systems(List.of(
                new PdeSystemBuilder()
                    .systemId("x")
                    .systemName("X")
                    .ports(ports)
                    .build()
            ))
            .build();

        final Map<Integer, PlantDescriptionEntry> entries = Map.of(entry.id(), entry);
        final PlantDescriptionValidator validator = new PlantDescriptionValidator(entries);
        assertTrue(validator.hasError());
        final String expectedErrorMessage = "<Port '" + portNameB + "' is a consumer port, it must not have any metadata.>";
        assertEquals(expectedErrorMessage, validator.getErrorMessage());
    }

}
