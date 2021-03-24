package eu.arrowhead.core.plantdescriptionengine.consumedservices.orchestrator;

import eu.arrowhead.core.plantdescriptionengine.pdtracker.PlantDescriptionTracker;
import eu.arrowhead.core.plantdescriptionengine.pdtracker.backingstore.InMemoryPdStore;
import eu.arrowhead.core.plantdescriptionengine.pdtracker.backingstore.PdStore;
import eu.arrowhead.core.plantdescriptionengine.pdtracker.backingstore.PdStoreException;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class RuleCreatorTest {

    final Instant now = Instant.now();
    private PlantDescriptionTracker pdTracker;

    @BeforeEach
    public void initEach() throws PdStoreException {
        PdStore pdStore = new InMemoryPdStore();
        pdTracker = new PlantDescriptionTracker(pdStore);
    }

    @Test
    public void shouldCreateRule() throws PdStoreException {

        final String consumerId = "system_1";
        final String producerId = "system_2";

        final String consumerName = "System 1";
        final String producerName = "System 2";

        final String consumerPort = "port_1";
        final String producerPort = "port_2";

        final String serviceDefinitionA = "service_a";
        final String serviceInterface = "HTTP-SECURE-JSON";

        final List<PortDto> consumerPorts = List.of(
            new PortBuilder()
                .portName(consumerPort)
                .serviceInterface(serviceInterface)
                .serviceDefinition(serviceDefinitionA)
                .consumer(true)
                .build());
        final List<PortDto> producerPorts = List.of(
            new PortBuilder()
                .portName(producerPort)
                .serviceInterface(serviceInterface)
                .serviceDefinition(serviceDefinitionA)
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

        final var entry = new PlantDescriptionEntryBuilder().id(0).plantDescription("Plant Description 1A")
            .createdAt(now)
            .updatedAt(now)
            .active(true)
            .include(new ArrayList<>())
            .systems(List.of(consumerSystem, producerSystem))
            .connections(connections)
            .build();

        pdTracker.put(entry);
        final var ruleCreator = new RuleCreator(pdTracker);
        var rule = ruleCreator.createRule(entry.connections().get(0));
        assertEquals(consumerName, rule.consumerSystem().systemName().orElse(null));
        assertEquals(producerName, rule.providerSystem().systemName().orElse(null));
        assertEquals(producerName, rule.providerSystem().systemName().orElse(null));
        assertEquals(producerSystem.ports().get(0).serviceDefinition(), rule.serviceDefinitionName());
        assertEquals(serviceInterface, rule.serviceInterfaceName());
    }

    /**
     * Two plant descriptions are created, one including the other. The active
     * plant description contains a connection between its own system and a
     * system in the included Plant Description.
     */
    @Test
    public void shouldAllowConnectionsToSystemInIncludedEntry() throws PdStoreException {

        final String serviceDefinitionX = "service_x";
        final String serviceDefinitionY = "service_z";

        final String consumerId = "Cons-Id";
        final String consumerPortA = "Cons-Port-A";
        final String consumerPortB = "Cons-Port-B";
        final String consumerName = "Consumer System";
        final String serviceInterface = "HTTP-INSECURE-JSON";
        final List<PortDto> consumerPorts = List.of(
            new PortBuilder().portName(consumerPortA)
                .serviceInterface(serviceInterface)
                .serviceDefinition(serviceDefinitionX)
                .consumer(true)
                .build(),
            new PortBuilder().portName(consumerPortB)
                .serviceInterface(serviceInterface)
                .serviceDefinition(serviceDefinitionY)
                .consumer(true)
                .build()
        );

        final PdeSystemDto consumerSystemA = new PdeSystemBuilder()
            .systemId(consumerId)
            .systemName(consumerName)
            .ports(consumerPorts)
            .build();

        final var entryA = new PlantDescriptionEntryBuilder()
            .id(0)
            .plantDescription("Plant Description A")
            .createdAt(now)
            .updatedAt(now)
            .active(true)
            .systems(List.of(consumerSystemA))
            .build();

        final String producerAId = "Prod-A-Id";
        final String producerBId = "Prod-B-Id";
        final String producerAPort = "Prod-A-Port";
        final String producerBPort = "Prod-B-Port";
        final String producerAName = "Prod-A-Name";
        final String producerBName = "Prod-B-Name";

        final List<PortDto> producerAPorts = List.of(
            new PortBuilder()
                .portName(producerAPort)
                .serviceInterface(serviceInterface)
                .serviceDefinition(serviceDefinitionX)
                .consumer(false)
                .build());

        final List<PortDto> producerBPorts = List.of(
            new PortBuilder()
                .portName(producerBPort)
                .serviceInterface(serviceInterface)
                .serviceDefinition(serviceDefinitionY)
                .consumer(false)
                .build());

        final PdeSystemDto producerSystemA = new PdeSystemBuilder()
            .systemId(producerAId)
            .systemName(producerAName)
            .ports(producerAPorts)
            .build();

        final PdeSystemDto producerSystemB = new PdeSystemBuilder()
            .systemId(producerBId)
            .systemName(producerBName)
            .ports(producerBPorts)
            .build();

        final ConnectionDto connectionA = new ConnectionBuilder()
            .consumer(new SystemPortBuilder()
                .systemId(consumerId)
                .portName(consumerPortA)
                .build())
            .producer(new SystemPortBuilder()
                .systemId(producerAId)
                .portName(producerAPort)
                .build())
            .build();

        final ConnectionDto connectionB = new ConnectionBuilder()
            .consumer(new SystemPortBuilder()
                .systemId(consumerId)
                .portName(consumerPortB)
                .build())
            .producer(new SystemPortBuilder()
                .systemId(producerBId)
                .portName(producerBPort)
                .build())
            .build();

        final var entryB = new PlantDescriptionEntryBuilder()
            .id(1)
            .plantDescription("Plant Description B")
            .createdAt(now)
            .updatedAt(now)
            .active(true)
            .include(List.of(entryA.id()))
            .systems(List.of(producerSystemA, producerSystemB))
            .connections(List.of(connectionA, connectionB))
            .build();

        pdTracker.put(entryA);
        pdTracker.put(entryB);

        final var ruleCreator = new RuleCreator(pdTracker);

        var ruleA = ruleCreator.createRule(connectionA);
        var ruleB = ruleCreator.createRule(connectionB);

        assertEquals(consumerName, ruleA.consumerSystem().systemName().orElse(null));
        assertEquals(producerAName, ruleA.providerSystem().systemName().orElse(null));

        assertEquals(consumerName, ruleB.consumerSystem().systemName().orElse(null));
        assertEquals(producerBName, ruleB.providerSystem().systemName().orElse(null));
    }

    @Test
    public void shouldAddMetadata() throws PdStoreException {
        final String serviceDefinition = "service_a";
        final String consumerId = "Cons-A-Id";
        final String consumerPort = "Cons-A-Port";
        final Map<String, String> consumerMetadata = Map.of("x", "y");
        final String serviceInterface = "HTTP-INSECURE-JSON";

        final List<PortDto> consumerAPorts = List.of(
            new PortBuilder()
                .portName(consumerPort)
                .serviceInterface(serviceInterface)
                .serviceDefinition(serviceDefinition)
                .consumer(true)
                .build());

        final PdeSystemDto consumerSystem = new PdeSystemBuilder()
            .systemId(consumerId)
            .metadata(consumerMetadata)
            .ports(consumerAPorts)
            .build();

        final String producerId = "Prod-Id";
        final String producerPort = "Prod-Port";
        final Map<String, String> producerMetadata = Map.of("a", "1");
        final Map<String, String> producerPortMetadata = Map.of("b", "2");

        final List<PortDto> producerPorts = List.of(
            new PortBuilder()
                .portName(producerPort)
                .metadata(producerPortMetadata)
                .serviceInterface(serviceInterface)
                .serviceDefinition(serviceDefinition)
                .consumer(false)
                .build());

        final PdeSystemDto producerSystem = new PdeSystemBuilder()
            .systemId(producerId)
            .metadata(producerMetadata)
            .ports(producerPorts)
            .build();

        final ConnectionDto connection = new ConnectionBuilder()
            .consumer(new SystemPortBuilder()
                .systemId(consumerId)
                .portName(consumerPort)
                .build())
            .producer(new SystemPortBuilder()
                .systemId(producerId)
                .portName(producerPort)
                .build())
            .build();

        final var entry = new PlantDescriptionEntryBuilder()
            .id(0)
            .plantDescription("Plant Description A")
            .createdAt(now)
            .updatedAt(now)
            .active(true)
            .systems(List.of(consumerSystem, producerSystem))
            .build();

        pdTracker.put(entry);

        final var ruleCreator = new RuleCreator(pdTracker);
        var rule = ruleCreator.createRule(connection);

        assertTrue(rule.consumerSystem().systemName().isEmpty());
        assertTrue(rule.providerSystem().systemName().isEmpty());

        final var expectedConsumerMetadata = Map.of("x", "y");
        assertEquals(expectedConsumerMetadata, rule.consumerSystem().metadata().orElse(null));

        final var expectedProducerMetadata = Map.of("a", "1", "b", "2");
        assertEquals(expectedProducerMetadata, rule.providerSystem().metadata().orElse(null));
        assertEquals(serviceInterface, rule.serviceInterfaceName());
    }

}
