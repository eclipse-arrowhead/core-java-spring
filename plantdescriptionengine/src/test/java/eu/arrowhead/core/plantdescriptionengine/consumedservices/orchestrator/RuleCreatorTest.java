package eu.arrowhead.core.plantdescriptionengine.consumedservices.orchestrator;

import eu.arrowhead.core.plantdescriptionengine.consumedservices.orchestrator.dto.StoreRule;
import eu.arrowhead.core.plantdescriptionengine.pdtracker.PlantDescriptionTracker;
import eu.arrowhead.core.plantdescriptionengine.pdtracker.backingstore.InMemoryPdStore;
import eu.arrowhead.core.plantdescriptionengine.pdtracker.backingstore.PdStore;
import eu.arrowhead.core.plantdescriptionengine.pdtracker.backingstore.PdStoreException;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.ConnectionDto;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.PdeSystemDto;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.PlantDescriptionEntryDto;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.PortDto;
import eu.arrowhead.core.plantdescriptionengine.providedservices.pde_mgmt.dto.SystemPortDto;
import org.junit.Before;
import org.junit.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class RuleCreatorTest {

    final Instant now = Instant.now();
    private PlantDescriptionTracker pdTracker;

    @Before
    public void initEach() throws PdStoreException {
        final PdStore pdStore = new InMemoryPdStore();
        pdTracker = new PlantDescriptionTracker(pdStore);
    }

    @Test
    public void shouldCreateRule() {

        final String consumerId = "system_1";
        final String producerId = "system_2";

        final String consumerName = "System 1";
        final String producerName = "System 2";

        final String consumerPort = "port_1";
        final String producerPort = "port_2";

        final int priority = 3;

        final String serviceDefinitionA = "service_a";
        final String serviceInterface = "HTTP-SECURE-JSON";

        final List<PortDto> consumerPorts = List.of(
            new PortDto.Builder()
                .portName(consumerPort)
                .serviceInterface(serviceInterface)
                .serviceDefinition(serviceDefinitionA)
                .consumer(true)
                .build());
        final List<PortDto> producerPorts = List.of(
            new PortDto.Builder()
                .portName(producerPort)
                .serviceInterface(serviceInterface)
                .serviceDefinition(serviceDefinitionA)
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
            .priority(priority)
            .build());

        final PlantDescriptionEntryDto entry = new PlantDescriptionEntryDto.Builder().id(0)
            .plantDescription("Plant Description 1A")
            .createdAt(now)
            .updatedAt(now)
            .active(true)
            .systems(List.of(consumerSystem, producerSystem))
            .connections(connections)
            .build();

        pdTracker.put(entry)
            .ifSuccess(result -> {
                final RuleCreator ruleCreator = new RuleCreator(pdTracker);
                final StoreRule rule = ruleCreator.createRule(entry.connections().get(0));
                assertEquals(consumerName, rule.consumerSystem().systemName().orElse(null));
                assertEquals(producerName, rule.providerSystem().systemName().orElse(null));
                assertEquals(producerName, rule.providerSystem().systemName().orElse(null));
                assertEquals(producerSystem.ports().get(0).serviceDefinition(), rule.serviceDefinitionName());
                assertEquals(serviceInterface, rule.serviceInterfaceName());
                assertEquals(priority, (int) rule.priority().orElse(-1));
            })
            .onFailure(e -> fail());
    }

    /**
     * Two plant descriptions are created, one including the other. The active
     * plant description contains a connection between its own system and a
     * system in the included Plant Description.
     */
    @Test
    public void shouldAllowConnectionsToSystemInIncludedEntry() {

        final String serviceDefinitionX = "servicex";
        final String serviceDefinitionY = "servicez";

        final String consumerId = "Cons-Id";
        final String consumerPortA = "Cons-Port-A";
        final String consumerPortB = "Cons-Port-B";
        final String consumerName = "Consumer System";
        final String serviceInterface = "HTTP-INSECURE-JSON";
        final List<PortDto> consumerPorts = List.of(
            new PortDto.Builder().portName(consumerPortA)
                .serviceInterface(serviceInterface)
                .serviceDefinition(serviceDefinitionX)
                .consumer(true)
                .build(),
            new PortDto.Builder().portName(consumerPortB)
                .serviceInterface(serviceInterface)
                .serviceDefinition(serviceDefinitionY)
                .consumer(true)
                .build()
        );

        final PdeSystemDto consumerSystemA = new PdeSystemDto.Builder()
            .systemId(consumerId)
            .systemName(consumerName)
            .ports(consumerPorts)
            .build();

        final PlantDescriptionEntryDto entryA = new PlantDescriptionEntryDto.Builder()
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
            new PortDto.Builder()
                .portName(producerAPort)
                .serviceInterface(serviceInterface)
                .serviceDefinition(serviceDefinitionX)
                .consumer(false)
                .build());

        final List<PortDto> producerBPorts = List.of(
            new PortDto.Builder()
                .portName(producerBPort)
                .serviceInterface(serviceInterface)
                .serviceDefinition(serviceDefinitionY)
                .consumer(false)
                .build());

        final PdeSystemDto producerSystemA = new PdeSystemDto.Builder()
            .systemId(producerAId)
            .systemName(producerAName)
            .ports(producerAPorts)
            .build();

        final PdeSystemDto producerSystemB = new PdeSystemDto.Builder()
            .systemId(producerBId)
            .systemName(producerBName)
            .ports(producerBPorts)
            .build();

        final ConnectionDto connectionA = new ConnectionDto.Builder()
            .consumer(new SystemPortDto.Builder()
                .systemId(consumerId)
                .portName(consumerPortA)
                .build())
            .producer(new SystemPortDto.Builder()
                .systemId(producerAId)
                .portName(producerAPort)
                .build())
            .build();

        final ConnectionDto connectionB = new ConnectionDto.Builder()
            .consumer(new SystemPortDto.Builder()
                .systemId(consumerId)
                .portName(consumerPortB)
                .build())
            .producer(new SystemPortDto.Builder()
                .systemId(producerBId)
                .portName(producerBPort)
                .build())
            .build();

        final PlantDescriptionEntryDto entryB = new PlantDescriptionEntryDto.Builder()
            .id(1)
            .plantDescription("Plant Description B")
            .createdAt(now)
            .updatedAt(now)
            .active(true)
            .include(List.of(entryA.id()))
            .systems(List.of(producerSystemA, producerSystemB))
            .connections(List.of(connectionA, connectionB))
            .build();

        pdTracker.put(entryA)
            .flatMap(result -> pdTracker.put(entryB))
            .ifSuccess(result -> {
                final RuleCreator ruleCreator = new RuleCreator(pdTracker);

                final StoreRule ruleA = ruleCreator.createRule(connectionA);
                final StoreRule ruleB = ruleCreator.createRule(connectionB);

                assertEquals(consumerName, ruleA.consumerSystem().systemName().orElse(null));
                assertEquals(producerAName, ruleA.providerSystem().systemName().orElse(null));

                assertEquals(consumerName, ruleB.consumerSystem().systemName().orElse(null));
                assertEquals(producerBName, ruleB.providerSystem().systemName().orElse(null));
            })
            .onFailure(e -> fail());
    }

    @Test
    public void shouldAddMetadata() {
        final String serviceDefinition = "service_a";
        final String consumerId = "Cons-A-Id";
        final String consumerPort = "Cons-A-Port";
        final Map<String, String> consumerMetadata = Map.of("x", "y");
        final String serviceInterface = "HTTP-INSECURE-JSON";

        final List<PortDto> consumerAPorts = List.of(
            new PortDto.Builder()
                .portName(consumerPort)
                .serviceInterface(serviceInterface)
                .serviceDefinition(serviceDefinition)
                .consumer(true)
                .build());

        final PdeSystemDto consumerSystem = new PdeSystemDto.Builder()
            .systemId(consumerId)
            .metadata(consumerMetadata)
            .ports(consumerAPorts)
            .build();

        final String producerId = "Prod-Id";
        final String producerPort = "Prod-Port";
        final Map<String, String> producerMetadata = Map.of("a", "1");
        final Map<String, String> producerPortMetadata = Map.of("b", "2");

        final List<PortDto> producerPorts = List.of(
            new PortDto.Builder()
                .portName(producerPort)
                .metadata(producerPortMetadata)
                .serviceInterface(serviceInterface)
                .serviceDefinition(serviceDefinition)
                .consumer(false)
                .build());

        final PdeSystemDto producerSystem = new PdeSystemDto.Builder()
            .systemId(producerId)
            .metadata(producerMetadata)
            .ports(producerPorts)
            .build();

        final ConnectionDto connection = new ConnectionDto.Builder()
            .consumer(new SystemPortDto.Builder()
                .systemId(consumerId)
                .portName(consumerPort)
                .build())
            .producer(new SystemPortDto.Builder()
                .systemId(producerId)
                .portName(producerPort)
                .build())
            .build();

        final PlantDescriptionEntryDto entry = new PlantDescriptionEntryDto.Builder()
            .id(0)
            .plantDescription("Plant Description A")
            .createdAt(now)
            .updatedAt(now)
            .active(true)
            .systems(List.of(consumerSystem, producerSystem))
            .build();

        pdTracker.put(entry)
            .ifSuccess(result -> {
                final RuleCreator ruleCreator = new RuleCreator(pdTracker);
                final StoreRule rule = ruleCreator.createRule(connection);

                assertTrue(rule.consumerSystem().systemName().isEmpty());
                assertTrue(rule.providerSystem().systemName().isEmpty());

                final Map<String, String> expectedConsumerMetadata = Map.of("x", "y");
                assertEquals(expectedConsumerMetadata, rule.consumerSystem().metadata());

                final Map<String, String> expectedSystemMetadata = Map.of("a", "1");
                final Map<String, String> expectedServiceMetadata = Map.of("b", "2");
                assertEquals(expectedSystemMetadata, rule.providerSystem().metadata());
                assertEquals(expectedServiceMetadata, rule.serviceMetadata());
                assertEquals(serviceInterface, rule.serviceInterfaceName());
            })
            .onFailure(e -> fail());
    }

}
