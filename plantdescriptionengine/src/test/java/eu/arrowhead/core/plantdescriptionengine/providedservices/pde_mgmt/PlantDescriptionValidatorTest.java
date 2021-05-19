package eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt;

import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.ConnectionDto;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.PdeSystemDto;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.PlantDescriptionEntryDto;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.PortDto;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.SystemPortDto;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PlantDescriptionValidatorTest {

    final Instant now = Instant.now();

    // TODO: Write a test that check that identical systems can be present in
    // separate entries, as long as they are not both active.

    // TODO: Write a test that checks that connections are only allowed between
    // the active entry and its included entries.

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

        final List<PortDto> consumerPortsA = List.of(new PortDto.Builder()
            .portName(consumerPortA)
            .serviceInterface(serviceInterface)
            .serviceDefinition("Monitorable")
            .consumer(true)
            .build());

        final List<PortDto> producerPortsA = List.of(new PortDto.Builder()
            .portName(producerPortA)
            .serviceInterface("HTTP-SECURE-JSON")
            .serviceDefinition("Monitorable")
            .consumer(false)
            .build());

        final PdeSystemDto consumerSystemA = new PdeSystemDto.Builder().systemId(consumerIdA)
            .systemName(consumerNameA)
            .ports(consumerPortsA)
            .build();

        final PdeSystemDto producerSystemA = new PdeSystemDto.Builder()
            .systemId(producerIdA)
            .systemName(producerNameA)
            .ports(producerPortsA)
            .build();

        final List<ConnectionDto> connectionsA = List.of(new ConnectionDto.Builder()
            .priority(1)
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
        final String consumerNameB = "Consumer B";
        final String consumerPortB = "Cons-Port-B";

        final List<PortDto> consumerPortsB = List.of(new PortDto.Builder()
            .portName(consumerPortB)
            .serviceInterface(serviceInterface)
            .serviceDefinition("Monitorable")
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

        final PlantDescriptionValidator validator = new PlantDescriptionValidator(entryA, entryB);
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

        final List<PortDto> consumerPortsA = List.of(new PortDto.Builder()
            .portName(consumerPort)
            .serviceInterface(serviceInterface)
            .serviceDefinition("Monitorable")
            .consumer(true)
            .build());

        final List<PortDto> producerPortsA = List.of(new PortDto.Builder()
            .portName(producerPort)
            .serviceInterface(serviceInterface)
            .serviceDefinition("Monitorable")
            .consumer(false)
            .build());

        final PdeSystemDto consumerSystem = new PdeSystemDto.Builder()
            .systemId(consumerId)
            .systemName(consumerNameA)
            .ports(consumerPortsA)
            .build();

        final PdeSystemDto producerSystem = new PdeSystemDto.Builder()
            .systemId(producerId)
            .systemName(producerNameA)
            .ports(producerPortsA)
            .build();

        final List<ConnectionDto> connections = List.of(new ConnectionDto.Builder()
            .priority(1)
            .consumer(new SystemPortDto.Builder()
                .systemId(producerId)
                .portName(producerPort)
                .build())
            .producer(new SystemPortDto.Builder()
                .systemId(consumerId)
                .portName(consumerPort)
                .build())
            .build());

        final PlantDescriptionEntryDto entry = new PlantDescriptionEntryDto.Builder()
            .id(entryId)
            .plantDescription("Plant Description A")
            .createdAt(now)
            .updatedAt(now)
            .active(true)
            .systems(List.of(consumerSystem, producerSystem))
            .connections(connections)
            .build();
        final PlantDescriptionValidator validator = new PlantDescriptionValidator(entry);
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

        final List<PortDto> consumerPortsA = List.of(new PortDto.Builder()
            .portName(consumerPort)
            .serviceInterface(serviceInterfaceA)
            .serviceDefinition("Monitorable")
            .consumer(true)
            .build());

        final List<PortDto> producerPortsA = List.of(new PortDto.Builder()
            .portName(producerPort)
            .serviceInterface(serviceInterfaceB)
            .serviceDefinition("Monitorable")
            .consumer(false)
            .build());

        final PdeSystemDto consumerSystem = new PdeSystemDto.Builder()
            .systemId(consumerId)
            .systemName(consumerNameA)
            .ports(consumerPortsA)
            .build();

        final PdeSystemDto producerSystem = new PdeSystemDto.Builder()
            .systemId(producerId)
            .systemName(producerNameA)
            .ports(producerPortsA)
            .build();

        final List<ConnectionDto> connections = List.of(new ConnectionDto.Builder()
            .priority(1)
            .consumer(new SystemPortDto.Builder()
                .systemId(consumerId)
                .portName(consumerPort)
                .build())
            .producer(new SystemPortDto.Builder()
                .systemId(producerId)
                .portName(producerPort)
                .build())
            .build());

        final PlantDescriptionEntryDto entry = new PlantDescriptionEntryDto.Builder()
            .id(entryId)
            .plantDescription("Plant Description A")
            .createdAt(now)
            .updatedAt(now)
            .active(true)
            .systems(List.of(consumerSystem, producerSystem))
            .connections(connections)
            .build();

        final PlantDescriptionValidator validator = new PlantDescriptionValidator(entry);
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

        final List<PortDto> consumerPortsA = List.of(new PortDto.Builder()
            .portName(consumerPort)
            .serviceInterface(serviceInterface)
            .serviceDefinition(serviceDefinitionA)
            .consumer(true)
            .build());

        final List<PortDto> producerPortsA = List.of(new PortDto.Builder()
            .portName(producerPort)
            .serviceInterface(serviceInterface)
            .serviceDefinition(serviceDefinitionB)
            .consumer(false)
            .build());

        final PdeSystemDto consumerSystem = new PdeSystemDto.Builder()
            .systemId(consumerId)
            .systemName(consumerNameA)
            .ports(consumerPortsA)
            .build();

        final PdeSystemDto producerSystem = new PdeSystemDto.Builder()
            .systemId(producerId)
            .systemName(producerNameA)
            .ports(producerPortsA)
            .build();

        final List<ConnectionDto> connections = List.of(new ConnectionDto.Builder()
            .priority(1)
            .consumer(new SystemPortDto.Builder()
                .systemId(consumerId)
                .portName(consumerPort)
                .build())
            .producer(new SystemPortDto.Builder()
                .systemId(producerId)
                .portName(producerPort)
                .build())
            .build());

        final PlantDescriptionEntryDto entry = new PlantDescriptionEntryDto.Builder()
            .id(entryId)
            .plantDescription("Plant Description A")
            .createdAt(now)
            .updatedAt(now)
            .active(true)
            .systems(List.of(consumerSystem, producerSystem))
            .connections(connections)
            .build();

        final PlantDescriptionValidator validator = new PlantDescriptionValidator(entry);
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
            new PortDto.Builder()
                .portName(portName)
                .serviceInterface(serviceInterface)
                .serviceDefinition("service_a")
                .consumer(true)
                .build(),
            new PortDto.Builder()
                .portName(portName) // Error! Same name!
                .serviceInterface(serviceInterface)
                .serviceDefinition("service_b")
                .consumer(true)
                .build());

        final PdeSystemDto consumerSystem = new PdeSystemDto.Builder()
            .systemId(systemId)
            .systemName("System XYZ")
            .ports(consumerPorts)
            .build();

        final PlantDescriptionEntryDto entry = new PlantDescriptionEntryDto.Builder()
            .plantDescription("Plant Description 1A")
            .id(123)
            .active(false)
            .createdAt(now)
            .updatedAt(now)
            .systems(List.of(consumerSystem))
            .build();

        final PlantDescriptionValidator validator = new PlantDescriptionValidator(entry);
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
            new PortDto.Builder()
                .portName(portNameA)
                .serviceInterface(serviceInterface)
                .serviceDefinition(serviceDefinition)
                .metadata(Map.of("a", "1"))
                .build(),
            new PortDto.Builder()
                .portName("port_b")
                .serviceInterface(serviceInterface)
                .serviceDefinition(serviceDefinition)
                .build(),
            new PortDto.Builder()
                .portName("port_c")
                .serviceInterface(serviceInterface)
                .serviceDefinition(serviceDefinition)
                .build());

        final PdeSystemDto system = new PdeSystemDto.Builder()
            .systemId(systemId)
            .systemName("X")
            .ports(ports)
            .build();

        final PlantDescriptionEntryDto entry = new PlantDescriptionEntryDto.Builder()
            .id(789)
            .plantDescription("Plant Description 1A")
            .active(true)
            .systems(List.of(system))
            .createdAt(now)
            .updatedAt(now)
            .build();

        final PlantDescriptionValidator validator = new PlantDescriptionValidator(entry);
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

        final List<PortDto> consumerPorts = List.of(new PortDto.Builder()
            .portName(consumerPort)
            .serviceInterface(serviceInterface)
            .serviceDefinition(serviceDefinition)
            .consumer(true)
            .build());

        final List<PortDto> producerPorts = List.of(new PortDto.Builder()
            .portName(producerPort)
            .serviceInterface(serviceInterface)
            .serviceDefinition(serviceDefinition)
            .consumer(false)
            .build());

        final PdeSystemDto consumerSystem = new PdeSystemDto.Builder()
            .systemId(consumerId)
            .systemName("Consumer X")
            .ports(consumerPorts)
            .build();

        final PdeSystemDto producerSystem = new PdeSystemDto.Builder()
            .systemId(producerId)
            .systemName("Producer Y")
            .ports(producerPorts)
            .build();

        final List<ConnectionDto> connections = List.of(new ConnectionDto.Builder()
            .consumer(new SystemPortDto.Builder()
                .systemId(consumerId)
                .portName(consumerPort)
                .build())
            .producer(new SystemPortDto.Builder()
                .systemId(producerId)
                .portName(invalidPort)
                .build())
            .build());
        final PlantDescriptionEntryDto entry = new PlantDescriptionEntryDto.Builder()
            .id(42)
            .plantDescription("Plant Description 1A")
            .active(true)
            .systems(List.of(consumerSystem, producerSystem))
            .connections(connections)
            .createdAt(now)
            .updatedAt(now)
            .build();

        final PlantDescriptionValidator validator = new PlantDescriptionValidator(entry);
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

        final List<PortDto> consumerPortsA = List.of(new PortDto.Builder()
            .portName(consumerPortA)
            .serviceInterface(serviceInterface)
            .serviceDefinition("Monitorable")
            .consumer(true)
            .build());

        final List<PortDto> producerPortsA = List.of(new PortDto.Builder()
            .portName(producerPortA)
            .serviceInterface(serviceInterface)
            .serviceDefinition("Monitorable")
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

        final List<ConnectionDto> connectionsA = List.of(new ConnectionDto.Builder()
            .priority(-1)
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
        final String consumerNameB = "Consumer B";
        final String consumerPortB = "Cons-Port-B";

        final List<PortDto> consumerPortsB = List.of(new PortDto.Builder()
            .portName(consumerPortB)
            .serviceInterface(serviceInterface)
            .serviceDefinition("Monitorable")
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

        final PlantDescriptionValidator validator = new PlantDescriptionValidator(entryB, entryA);
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

        final List<PortDto> consumerPorts = List.of(new PortDto.Builder()
            .portName(consumerPort)
            .serviceInterface(serviceInterface)
            .serviceDefinition(serviceDefinition)
            .consumer(true)
            .build());

        final List<PortDto> producerPorts = List.of(new PortDto.Builder()
            .portName(producerPort)
            .serviceInterface(serviceInterface)
            .serviceDefinition(serviceDefinition)
            .consumer(false)
            .build());

        final PdeSystemDto consumerSystem = new PdeSystemDto.Builder()
            .systemId(consumerId)
            .systemName("Consumer A")
            .ports(consumerPorts)
            .build();

        final PdeSystemDto producerSystem = new PdeSystemDto.Builder()
            .systemId(producerId)
            .systemName("Consumer B")
            .ports(producerPorts)
            .build();

        final List<ConnectionDto> connections = List.of(new ConnectionDto.Builder()
            .consumer(new SystemPortDto.Builder()
                .systemId(consumerId)
                .portName(invalidPort)
                .build())
            .producer(new SystemPortDto.Builder()
                .systemId(producerId)
                .portName(producerPort)
                .build())
            .build());

        final PlantDescriptionEntryDto entry = new PlantDescriptionEntryDto.Builder()
            .id(89)
            .plantDescription("Plant Description 1A")
            .active(true)
            .systems(List.of(consumerSystem, producerSystem))
            .connections(connections)
            .createdAt(now)
            .updatedAt(now)
            .build();

        final PlantDescriptionValidator validator = new PlantDescriptionValidator(entry);
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

        final List<PortDto> consumerPorts = List.of(new PortDto.Builder()
            .portName(consumerPort)
            .serviceInterface(serviceInterface)
            .serviceDefinition(serviceDefinition)
            .consumer(true)
            .build());

        final List<PortDto> producerPorts = List.of(new PortDto.Builder()
            .portName(producerPort)
            .serviceInterface(serviceInterface)
            .serviceDefinition(serviceDefinition)
            .consumer(false)
            .build());

        final PdeSystemDto consumerSystem = new PdeSystemDto.Builder()
            .systemId(consumerId)
            .systemName("Consumer I")
            .ports(consumerPorts)
            .build();

        final PdeSystemDto producerSystem = new PdeSystemDto.Builder()
            .systemId(producerId)
            .systemName("Producer J")
            .ports(producerPorts)
            .build();

        final List<ConnectionDto> connections = List.of(
            new ConnectionDto.Builder()
                .consumer(new SystemPortDto.Builder()
                    .systemId(consumerId)
                    .portName(consumerPort)
                    .build())
                .producer(new SystemPortDto.Builder().systemId(producerId)
                    .portName(producerPort)
                    .build())
                .build(),
            new ConnectionDto.Builder()
                .consumer(new SystemPortDto.Builder()
                    .systemId(missingId)
                    .portName(consumerPort)
                    .build())
                .producer(new SystemPortDto.Builder().systemId(producerId)
                    .portName(producerPort)
                    .build())
                .build()
        );
        final PlantDescriptionEntryDto entry = new PlantDescriptionEntryDto.Builder()
            .id(23)
            .plantDescription("Plant Description 1A")
            .active(true)
            .systems(List.of(consumerSystem, producerSystem))
            .connections(connections)
            .createdAt(now)
            .updatedAt(now)
            .build();

        final PlantDescriptionValidator validator = new PlantDescriptionValidator(entry);
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

        final List<PortDto> consumerPorts = List.of(new PortDto.Builder()
            .portName(consumerPort)
            .serviceInterface(serviceInterface)
            .serviceDefinition(serviceDefinition)
            .consumer(true)
            .build());

        final List<PortDto> producerPorts = List.of(new PortDto.Builder()
            .portName(producerPort)
            .serviceInterface(serviceInterface)
            .serviceDefinition(serviceDefinition)
            .consumer(false)
            .build());

        final PdeSystemDto consumerSystem = new PdeSystemDto.Builder()
            .systemId(consumerId)
            .systemName("Consumer A")
            .ports(consumerPorts)
            .build();

        final PdeSystemDto producerSystem = new PdeSystemDto.Builder()
            .systemId(producerId)
            .systemName("Producer B")
            .ports(producerPorts)
            .build();

        final List<ConnectionDto> connections = List.of(new ConnectionDto.Builder()
            .consumer(new SystemPortDto.Builder()
                .systemId(consumerId)
                .portName(consumerPort)
                .build())
            .producer(new SystemPortDto.Builder()
                .systemId(missingId)
                .portName(producerPort)
                .build())
            .build());
        final PlantDescriptionEntryDto entry = new PlantDescriptionEntryDto.Builder()
            .id(23)
            .plantDescription("Plant Description 1A")
            .active(true)
            .systems(List.of(consumerSystem, producerSystem))
            .connections(connections)
            .createdAt(now)
            .updatedAt(now)
            .build();

        final PlantDescriptionValidator validator = new PlantDescriptionValidator(entry);
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
            new PortDto.Builder()
                .portName("port_1")
                .serviceInterface(serviceInterface)
                .serviceDefinition(serviceDefinition)
                .metadata(sharedMetadata)
                .build(),
            new PortDto.Builder()
                .portName("port_b")
                .serviceInterface(serviceInterface)
                .serviceDefinition(serviceDefinition)
                .metadata(sharedMetadata)
                .build());

        final PdeSystemDto producerSystem = new PdeSystemDto.Builder()
            .systemId(systemId)
            .systemName("Producer Y")
            .ports(ports)
            .build();

        final PlantDescriptionEntryDto entry = new PlantDescriptionEntryDto.Builder()
            .id(23)
            .plantDescription("Plant Description 1A")
            .active(true)
            .systems(List.of(producerSystem))
            .createdAt(now)
            .updatedAt(now)
            .build();

        final PlantDescriptionValidator validator = new PlantDescriptionValidator(entry);
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

        final PlantDescriptionEntryDto entryA = new PlantDescriptionEntryDto.Builder()
            .id(entryIdA)
            .plantDescription("Plant Description A")
            .createdAt(now)
            .updatedAt(now)
            .active(false)
            .build();

        final PlantDescriptionEntryDto entryB = new PlantDescriptionEntryDto.Builder()
            .id(entryIdB)
            .plantDescription("Plant Description B")
            .createdAt(now)
            .updatedAt(now)
            .active(false)
            .build();

        final PlantDescriptionEntryDto entryC = new PlantDescriptionEntryDto.Builder()
            .id(entryIdC)
            .plantDescription("Plant Description C")
            .createdAt(now)
            .updatedAt(now)
            .active(true)
            .include(List.of(entryIdA, entryIdA, entryIdB, entryIdB))
            .build();

        final PlantDescriptionValidator validator = new PlantDescriptionValidator(entryA, entryB, entryC);

        assertTrue(validator.hasError());

        final String expectedErrorMessage = "<Entry with ID '" + entryIdA + "' is included more than once.>, "
            + "<Entry with ID '" + entryIdB + "' is included more than once.>";
        assertEquals(expectedErrorMessage, validator.getErrorMessage());
    }

    @Test
    public void shouldReportSelfInclusion() {

        final int entryId = 344;

        final PlantDescriptionEntryDto entry = new PlantDescriptionEntryDto.Builder()
            .id(entryId)
            .plantDescription("Plant Description A")
            .createdAt(now)
            .updatedAt(now)
            .active(false)
            .include(List.of(entryId))
            .build();

        final PlantDescriptionValidator validator = new PlantDescriptionValidator(entry);

        assertTrue(validator.hasError());
        assertEquals("<Entry includes itself.>", validator.getErrorMessage());
    }

    @Test
    public void shouldReportNonexistentInclusions() {

        final int nonExistentA = 23;
        final int entryId = 44;

        final PlantDescriptionEntryDto entry = new PlantDescriptionEntryDto.Builder()
            .id(entryId)
            .plantDescription("Plant Description A")
            .createdAt(now)
            .updatedAt(now)
            .active(true)
            .include(List.of(nonExistentA))
            .build();

        final PlantDescriptionValidator validator = new PlantDescriptionValidator(entry);

        assertTrue(validator.hasError());
        final String expectedErrorMessage = "<Error in include list: Entry '" + nonExistentA + "' is required by entry '"
            + entryId + "'.>";
        assertEquals(expectedErrorMessage, validator.getErrorMessage());
    }

    @Test
    public void shouldReportIncludeCycles() {

        final int entryIdA = 0;
        final int entryIdB = 1;
        final int entryIdC = 2;
        final int entryIdD = 3;

        final PlantDescriptionEntryDto entryA = new PlantDescriptionEntryDto.Builder()
            .id(entryIdA)
            .plantDescription("Plant Description A")
            .createdAt(now)
            .updatedAt(now)
            .active(false)
            .build();

        final PlantDescriptionEntryDto entryB = new PlantDescriptionEntryDto.Builder()
            .id(entryIdB)
            .plantDescription("Plant Description B")
            .createdAt(now)
            .updatedAt(now)
            .active(false)
            .build();

        final PlantDescriptionEntryDto entryC = new PlantDescriptionEntryDto.Builder()
            .id(entryIdC)
            .plantDescription("Plant Description C")
            .createdAt(now)
            .updatedAt(now)
            .active(false)
            .include(List.of(entryIdA, entryIdB))
            .build();

        final PlantDescriptionEntryDto entryD = new PlantDescriptionEntryDto.Builder()
            .id(entryIdD)
            .plantDescription("Plant Description C")
            .createdAt(now)
            .updatedAt(now)
            .active(true) // TODO: This test should work when active is false as well.
            .include(List.of(entryIdB, entryIdC))
            .build();

        final PlantDescriptionValidator validator = new PlantDescriptionValidator(entryA, entryB, entryC, entryD);
        assertTrue(validator.hasError());

        final String expectedErrorMessage = "<Error in include list: Cycle detected.>";
        assertEquals(expectedErrorMessage, validator.getErrorMessage());
    }

    @Test
    public void shouldReportInvalidSystemId() {
        final PlantDescriptionEntryDto entry = new PlantDescriptionEntryDto.Builder()
            .id(9)
            .plantDescription("Plant Description A")
            .createdAt(now)
            .updatedAt(now)
            .active(true)
            .systems(List.of(
                new PdeSystemDto.Builder()
                    .systemId("Unknown")
                    .systemName("System XYZ")
                    .build()
            ))
            .build();

        final PlantDescriptionValidator validator = new PlantDescriptionValidator(entry);
        assertTrue(validator.hasError());
        final String expectedErrorMessage = "<'Unknown' is not a valid system ID.>";
        assertEquals(expectedErrorMessage, validator.getErrorMessage());
    }

    @Test
    public void shouldRequireNameOrMetadata() {

        final PlantDescriptionEntryDto entry = new PlantDescriptionEntryDto.Builder()
            .id(22)
            .plantDescription("Plant Description A")
            .createdAt(now)
            .updatedAt(now)
            .active(true) // TODO: This test should have the same result when active is set to false
            .systems(List.of(
                new PdeSystemDto.Builder()
                    .systemId("xyz")
                    .build()
            ))
            .build();

        final PlantDescriptionValidator validator = new PlantDescriptionValidator(entry);
        assertTrue(validator.hasError());
        final String expectedErrorMessage = "<Contains a system with neither a name nor metadata to identify it.>";
        assertEquals(expectedErrorMessage, validator.getErrorMessage());
    }

    @Test
    public void shouldReportNonUniqueSystem() {
        final String systemIdB = "Sys-B";
        final String systemName = "XYZ";
        final Map<String, String> metadata = Map.of("a", "1");
        final PlantDescriptionEntryDto entry = new PlantDescriptionEntryDto.Builder()
            .id(22)
            .plantDescription("Plant Description A")
            .createdAt(now)
            .updatedAt(now)
            .active(true)
            .systems(List.of(
                new PdeSystemDto.Builder()
                    .systemId("Sys-A")
                    .systemName(systemName)
                    .metadata(metadata)
                    .build(),
                new PdeSystemDto.Builder()
                    .systemId(systemIdB)
                    .systemName(systemName)
                    .metadata(metadata)
                    .build()
            ))
            .build();

        final PlantDescriptionValidator validator = new PlantDescriptionValidator(entry);
        assertTrue(validator.hasError());
        final String expectedErrorMessage = "<System with ID '" + systemIdB +
            "' cannot be uniquely identified by its name/metadata combination.>";
        assertEquals(expectedErrorMessage, validator.getErrorMessage());
    }

    @Test
    public void shouldTreatEmptyMetadataAsNull() {
        final PlantDescriptionEntryDto entry = new PlantDescriptionEntryDto.Builder()
            .id(22)
            .plantDescription("Plant Description A")
            .createdAt(now)
            .updatedAt(now)
            .active(true) // TODO: This test should work when active is set to false
            .systems(List.of(
                new PdeSystemDto.Builder()
                    .systemId("xyz")
                    .metadata(Map.of())
                    .build()
            ))
            .build();

        final PlantDescriptionValidator validator = new PlantDescriptionValidator(entry);
        assertTrue(validator.hasError());
        final String expectedErrorMessage = "<Contains a system with neither a name nor metadata to identify it.>";
        assertEquals(expectedErrorMessage, validator.getErrorMessage());
    }

    @Test
    public void shouldAcceptSystemWithOnlyMetadata() {

        final PlantDescriptionEntryDto entry = new PlantDescriptionEntryDto.Builder()
            .id(22)
            .plantDescription("Plant Description A")
            .createdAt(now)
            .updatedAt(now)
            .active(true)
            .systems(List.of(
                new PdeSystemDto.Builder()
                    .systemId("X")
                    .metadata(Map.of("x", "y"))
                    .build()
            ))
            .build();

        final PlantDescriptionValidator validator = new PlantDescriptionValidator(entry);
        assertFalse(validator.hasError());
    }

    @Test
    public void shouldNotAcceptConsumerMetadata() {
        final String portNameB = "Port-B";
        final String serviceInterface = "HTTP-SECURE-JSON";

        final List<PortDto> ports = List.of(
            new PortDto.Builder()
                .portName("PortA")
                .serviceInterface(serviceInterface)
                .serviceDefinition("Monitorable")
                .consumer(true)
                .metadata(Map.of()) // An empty metadata object is okay.
                .build(),
            new PortDto.Builder()
                .portName(portNameB)
                .serviceInterface(serviceInterface)
                .serviceDefinition("Monitorable")
                .consumer(true)
                .metadata(Map.of("x", "y")) // Not okay on a consumer.
                .build()
        );

        final PlantDescriptionEntryDto entry = new PlantDescriptionEntryDto.Builder()
            .id(22)
            .plantDescription("Plant Description A")
            .createdAt(now)
            .updatedAt(now)
            .active(false)
            .systems(List.of(
                new PdeSystemDto.Builder()
                    .systemId("x")
                    .systemName("X")
                    .ports(ports)
                    .build()
            ))
            .build();

        final PlantDescriptionValidator validator = new PlantDescriptionValidator(entry);
        assertTrue(validator.hasError());
        final String expectedErrorMessage = "<Port '" + portNameB +
            "' is a consumer port, it must not have any metadata.>";
        assertEquals(expectedErrorMessage, validator.getErrorMessage());
    }

}
